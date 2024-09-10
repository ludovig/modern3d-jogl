package com.mundoludo.modern3d.tut08;

import com.jogamp.newt.event.*;
import com.jogamp.opengl.GL3;
import com.jogamp.opengl.util.GLBuffers;

import glm_.Java;
import glm_.mat4x4.Mat4;
import glm_.vec3.Vec3;
import glm_.quat.Quat;

import java.nio.FloatBuffer;
import java.util.ArrayList;

import javax.xml.parsers.ParserConfigurationException;
import java.io.IOException;
import java.net.URISyntaxException;
import org.xml.sax.SAXException;

import static com.jogamp.opengl.GL.GL_BACK;
import static com.jogamp.opengl.GL.GL_CULL_FACE;
import static com.jogamp.opengl.GL.GL_CW;
import static com.jogamp.opengl.GL2ES2.*;

import com.mundoludo.modern3d.framework.Framework;
import com.mundoludo.modern3d.framework.component.Mesh;
import com.mundoludo.modern3d.framework.MatrixStack;

public class CameraRelative extends Framework {

    public static void main(String[] args) {
        new CameraRelative().setup("Tutorial 08 - Camera Relative");
    }

    private int theProgram;

    private int modelToCameraMatrixUnif, cameraToClipMatrixUnif, baseColorUnif;

    private Mat4 cameraToClipMatrix = new Mat4(0.0f);

    private float calcFrustumScale(float fFovDeg) {
        final float degToRad = (float) Math.PI * 2.0f / 360.0f;
        float fFovRad = fFovDeg * degToRad;
        return 1.0f / (float) Math.tan(fFovRad / 2.0f);
    }

    private float fFrustumScale = calcFrustumScale(20.0f);

    private void initializeProgram(GL3 gl) {
        ArrayList<Integer> shaderList = new ArrayList<>();

        String vertShader = findResource("tut08/pos_color_local_transform.vert");
        String fragShader = findResource("tut08/color_mult_uniform.frag");

        shaderList.add(createShader(gl, GL_VERTEX_SHADER, vertShader));
        shaderList.add(createShader(gl, GL_FRAGMENT_SHADER, fragShader));

        theProgram = createProgram(gl, shaderList);
        shaderList.forEach(gl::glDeleteShader);

        modelToCameraMatrixUnif = gl.glGetUniformLocation(theProgram, "modelToCameraMatrix");
        cameraToClipMatrixUnif = gl.glGetUniformLocation(theProgram, "cameraToClipMatrix");
        baseColorUnif = gl.glGetUniformLocation(theProgram, "baseColor");

        float fzNear = 1f;
        float fzFar = 600.0f;

        cameraToClipMatrix.v00(fFrustumScale);
        cameraToClipMatrix.v11(fFrustumScale);
        cameraToClipMatrix.v22((fzFar + fzNear) / (fzNear - fzFar));
        cameraToClipMatrix.v23(-1.0f);
        cameraToClipMatrix.v32((2 * fzFar * fzNear) / (fzNear - fzFar));
        cameraToClipMatrix.to(MatBuffer);

        gl.glUseProgram(theProgram);
        gl.glUniformMatrix4fv(cameraToClipMatrixUnif, 1, false, MatBuffer);
        gl.glUseProgram(0);
    }

    public static FloatBuffer MatBuffer = GLBuffers.newDirectFloatBuffer(16);

    Mesh g_pShip;
    Mesh g_pPlane;

    @Override
    public void init(GL3 gl) {
        initializeProgram(gl);

        try {
            g_pShip = new Mesh(gl, getClass(), "tut08/Ship.xml");
            g_pPlane = new Mesh(gl, getClass(), "tut08/UnitPlane.xml");
        } catch (ParserConfigurationException | SAXException | IOException | URISyntaxException ex) {
            System.err.println("Mesh loading failure in " + CameraRelative.class.getName());
            System.err.println(ex);
        }

        gl.glEnable(GL_CULL_FACE);
        gl.glCullFace(GL_BACK);
        gl.glFrontFace(GL_CW);

        gl.glEnable(GL_DEPTH_TEST);
        gl.glDepthMask(true);
        gl.glDepthFunc(GL_LEQUAL);
        gl.glDepthRangef(0.0f, 1.0f);
    }

    Vec3 g_camTarget = new Vec3(0.0f, 10.0f, 0.0f);
    Quat g_orientation = new Quat(1.0f, 0.0f, 0.0f, 0.0f);

    //In spherical coordinates.
    Vec3 g_sphereCamRelPos = new Vec3(90.0f, 0.0f, 66.0f);

    private Vec3 ResolveCamPosition()
    {
        float phi = Framework.degToRad(g_sphereCamRelPos.getX());
        float theta = Framework.degToRad(g_sphereCamRelPos.getY() + 90.0f);

        float fSinTheta = (float) Math.sin(theta);
        float fCosTheta = (float) Math.cos(theta);
        float fCosPhi = (float) Math.cos(phi);
        float fSinPhi = (float) Math.sin(phi);

        Vec3 dirToCamera = new Vec3(fSinTheta * fCosPhi, fCosTheta, fSinTheta * fSinPhi);

        dirToCamera.timesAssign(g_sphereCamRelPos.getZ());
        dirToCamera.plusAssign(g_camTarget);

        return dirToCamera;
    }

    Mat4 CalcLookAtMatrix(Vec3 cameraPt, Vec3 lookPt, Vec3 upPt)
    {
        Vec3 lookDir = lookPt.minus(cameraPt).normalize();
        Vec3 upDir = upPt.normalize();

        Vec3 rightDir = lookDir.cross(upDir).normalize();
        Vec3 perpUpDir = rightDir.cross(lookDir);

        Mat4 rotMat = new Mat4(1.0f);
        rotMat.set(0, rightDir, 0.0f);
        rotMat.set(1, perpUpDir, 0.0f);
        rotMat.set(2, lookDir.negate(), 0.0f);

        rotMat = rotMat.transpose();

        Mat4 transMat = new Mat4(1.0f);
        transMat.set(3, cameraPt.negate(), 1.0f);

        rotMat.timesAssign(transMat);

        return rotMat;
    }

    @Override
    public void display(GL3 gl) {

        gl.glClearColor(0, 0, 0, 0);
        gl.glClearDepth(1.0f);
        gl.glClear(GL3.GL_COLOR_BUFFER_BIT | GL3.GL_DEPTH_BUFFER_BIT);

        MatrixStack currMatrix = new MatrixStack();
        Vec3 camPos = ResolveCamPosition();
        currMatrix.setMatrix(CalcLookAtMatrix(camPos, g_camTarget, new Vec3(0.0f, 1.0f, 0.0f)));

        gl.glUseProgram(theProgram);
        {
            currMatrix.push();
            currMatrix.scale(new Vec3(100.0f, 1.0f, 100.0f));

            gl.glUniform4f(baseColorUnif, 0.2f, 0.5f, 0.2f, 1.0f);
            gl.glUniformMatrix4fv(modelToCameraMatrixUnif, 1, false, currMatrix.to(MatBuffer));

            g_pPlane.render(gl);

            currMatrix.pop();
        }

        {
            currMatrix.push();
            currMatrix.translate(g_camTarget);
            currMatrix.applyMatrix(g_orientation.toMat4());
            currMatrix.rotateX(-90.0f);

            //Set the base color for this object.
            gl.glUniform4f(baseColorUnif, 1.0f, 1.0f, 1.0f, 1.0f);
            gl.glUniformMatrix4fv(modelToCameraMatrixUnif, 1, false, currMatrix.to(MatBuffer));

            g_pShip.render(gl, "tint");
            currMatrix.pop();
        }

        gl.glUseProgram(0);
    }

    @Override
    protected void reshape(GL3 gl, int w, int h) {
        cameraToClipMatrix.v00(fFrustumScale * (h / (float) w));
        cameraToClipMatrix.v11(fFrustumScale);

        gl.glUseProgram(theProgram);
        gl.glUniformMatrix4fv(cameraToClipMatrixUnif, 1, false, cameraToClipMatrix.to(MatBuffer));
        gl.glUseProgram(0);

        gl.glViewport(0, 0, w, h);
    }

    /**
     * Called at the end, here you want to clean all the resources.
     *
     * @param gl
     */
    @Override
    protected void end(GL3 gl) {

        gl.glDeleteProgram(theProgram);

        g_pPlane.dispose(gl);
        g_pShip.dispose(gl);
    }


    private interface OffsetRelative {
        int MODEL_RELATIVE = 0;
        int WORLD_RELATIVE = 1;
        int CAMERA_RELATIVE = 2;
        int NUM_RELATIVES = 3;
    }

    private int g_iOffset = OffsetRelative.MODEL_RELATIVE;

    private void OffsetOrientation(Vec3 _axis, float fAngDeg) {
        float fAngRad = degToRad(fAngDeg);

        Vec3 axis = new Vec3();
        Java.glm.normalize(_axis, axis);

        axis.timesAssign((float) Math.sin(fAngRad / 2.0f));
        float scalar = (float) Math.cos(fAngRad / 2.0f);

        Quat offset = new Quat(scalar, axis.getX(), axis.getY(), axis.getZ());

        switch(g_iOffset)
        {
            case OffsetRelative.MODEL_RELATIVE:
                g_orientation.timesAssign(offset);
                break;
            case OffsetRelative.WORLD_RELATIVE:
                g_orientation = offset.times(g_orientation);
                break;
            case OffsetRelative.CAMERA_RELATIVE:
                {
                    Vec3 camPos = ResolveCamPosition();
                    Mat4 camMat = CalcLookAtMatrix(camPos, g_camTarget, new Vec3(0.0f, 1.0f, 0.0f));

                    Quat viewQuat = camMat.toQuat();
                    Quat invViewQuat = viewQuat.conjugate();

                    Quat worldQuat = invViewQuat
                        .times(offset)
                        .times(viewQuat)
                        ;
                    g_orientation = worldQuat.times(g_orientation);
                }
                break;
        }

        Java.glm.normalize(g_orientation, g_orientation);
    }

    @Override
    public void keyPressed(KeyEvent keyEvent) {
        super.keyPressed(keyEvent);

        float SMALL_ANGLE_INCREMENT = 9.0f;

        switch (keyEvent.getKeyCode()) {
            case KeyEvent.VK_J:
                OffsetOrientation(new Vec3(1.0f, 0.0f, 0.0f), SMALL_ANGLE_INCREMENT);
                break;
            case KeyEvent.VK_K:
                OffsetOrientation(new Vec3(1.0f, 0.0f, 0.0f), -SMALL_ANGLE_INCREMENT);
                break;

            case KeyEvent.VK_L:
                OffsetOrientation(new Vec3(0.0f, 0.0f, 1.0f), SMALL_ANGLE_INCREMENT);
                break;
            case KeyEvent.VK_H:
                OffsetOrientation(new Vec3(0.0f, 0.0f, 1.0f), -SMALL_ANGLE_INCREMENT);
                break;

            case KeyEvent.VK_N:
                OffsetOrientation(new Vec3(0.0f, 1.0f, 0.0f), SMALL_ANGLE_INCREMENT);
                break;
            case KeyEvent.VK_F:
                OffsetOrientation(new Vec3(0.0f, 1.0f, 0.0f), -SMALL_ANGLE_INCREMENT);
                break;

            case KeyEvent.VK_SPACE:
                g_iOffset += 1;
                g_iOffset = g_iOffset % OffsetRelative.NUM_RELATIVES;
                {
                    switch(g_iOffset)
                    {
                        case OffsetRelative.MODEL_RELATIVE: System.out.println("Model Relative"); break;
                        case OffsetRelative.WORLD_RELATIVE: System.out.println("World Relative"); break;
                        case OffsetRelative.CAMERA_RELATIVE: System.out.println("Camera Relative"); break;
                    }
                }
                break;

            case KeyEvent.VK_I:
                g_sphereCamRelPos.setY(g_sphereCamRelPos.getY() - (keyEvent.isShiftDown() ? 1.125f : 11.25f));
                break;
            case KeyEvent.VK_A:
                g_sphereCamRelPos.setY(g_sphereCamRelPos.getY() + (keyEvent.isShiftDown() ? 1.125f : 11.25f));
                break;
            case KeyEvent.VK_S:
                g_sphereCamRelPos.setX(g_sphereCamRelPos.getX() - (keyEvent.isShiftDown() ? 1.125f : 11.25f));
                break;
            case KeyEvent.VK_E:
                g_sphereCamRelPos.setX(g_sphereCamRelPos.getX() + (keyEvent.isShiftDown() ? 1.125f : 11.25f));
                break;
            case KeyEvent.VK_X:
                g_sphereCamRelPos.setZ(g_sphereCamRelPos.getZ() - (keyEvent.isShiftDown() ? 1.125f : 11.25f));
                break;
            case KeyEvent.VK_C:
                g_sphereCamRelPos.setZ(g_sphereCamRelPos.getZ() + (keyEvent.isShiftDown() ? 1.125f : 11.25f));
                break;
        }

        g_sphereCamRelPos.setY(Java.glm.clamp(g_sphereCamRelPos.getY(), -78.75f, 10.0f));
    }
}

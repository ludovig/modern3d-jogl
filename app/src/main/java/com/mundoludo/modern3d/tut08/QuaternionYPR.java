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

public class QuaternionYPR extends Framework {

    public static void main(String[] args) {
        new QuaternionYPR().setup("Tutorial 08 - Quaternion YPR");
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
    public static FloatBuffer VecBuffer = GLBuffers.newDirectFloatBuffer(4);

    Mesh g_pShip;

    @Override
    public void init(GL3 gl) {
        initializeProgram(gl);

        try {
            g_pShip = new Mesh(gl, getClass(), "tut08/Ship.xml");
        } catch (ParserConfigurationException | SAXException | IOException | URISyntaxException ex) {
            System.err.println("Mesh loading failure in " + GimbalLock.class.getName());
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

    class GimbalAngles {
        public float fAngleX;
        public float fAngleY;
        public float fAngleZ;

        GimbalAngles() {
            fAngleX = 0.0f;
            fAngleY = 0.0f;
            fAngleZ = 0.0f;
        }
    }

    GimbalAngles g_angles = new GimbalAngles();
    Quat g_orientation = new Quat(1.0f, 0.0f, 0.0f, 0.0f);

    @Override
    public void display(GL3 gl) {

        gl.glClearColor(0, 0, 0, 0);
        gl.glClearDepth(1.0f);
        gl.glClear(GL3.GL_COLOR_BUFFER_BIT | GL3.GL_DEPTH_BUFFER_BIT);

        MatrixStack currMatrix = new MatrixStack();
        currMatrix.translate(new Vec3(0.0f, 0.0f, -200.0f));
        currMatrix.applyMatrix(g_orientation.toMat4());

        gl.glUseProgram(theProgram);
        currMatrix.scale(new Vec3(3.0f, 3.0f, 3.0f));
        currMatrix.rotateX(-90);
        //Set the base color for this object.
        gl.glUniform4f(baseColorUnif, 1.0f, 1.0f, 1.0f, 1.0f);
        gl.glUniformMatrix4fv(modelToCameraMatrixUnif, 1, false, currMatrix.to(MatBuffer));

        g_pShip.render(gl, "tint");

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

        g_pShip.dispose(gl);
    }

    private boolean g_bRightMultiply = true;

    private void OffsetOrientation(Vec3 _axis, float fAngDeg) {
        float fAngRad = degToRad(fAngDeg);

        Vec3 axis = new Vec3();
        Java.glm.normalize(_axis, axis);

        axis.timesAssign((float) Math.sin(fAngRad / 2.0f));
        float scalar = (float) Math.cos(fAngRad / 2.0f);

        Quat offset = new Quat(scalar, axis.getX(), axis.getY(), axis.getZ());

        if(g_bRightMultiply)
            g_orientation.timesAssign(offset);
        else
            g_orientation = offset.times(g_orientation);

        Java.glm.normalize(g_orientation, g_orientation);
    }

    @Override
    public void keyPressed(KeyEvent keyEvent) {
        super.keyPressed(keyEvent);

        float SMALL_ANGLE_INCREMENT = 9.0f;

        switch (keyEvent.getKeyCode()) {
            case KeyEvent.VK_W:
                OffsetOrientation(new Vec3(1.0f, 0.0f, 0.0f), SMALL_ANGLE_INCREMENT);
                break;
            case KeyEvent.VK_S:
                OffsetOrientation(new Vec3(1.0f, 0.0f, 0.0f), -SMALL_ANGLE_INCREMENT);
                break;

            case KeyEvent.VK_A:
                OffsetOrientation(new Vec3(0.0f, 0.0f, 1.0f), SMALL_ANGLE_INCREMENT);
                break;
            case KeyEvent.VK_D:
                OffsetOrientation(new Vec3(0.0f, 0.0f, 1.0f), -SMALL_ANGLE_INCREMENT);
                break;

            case KeyEvent.VK_Q:
                OffsetOrientation(new Vec3(0.0f, 1.0f, 0.0f), SMALL_ANGLE_INCREMENT);
                break;
            case KeyEvent.VK_E:
                OffsetOrientation(new Vec3(0.0f, 1.0f, 0.0f), -SMALL_ANGLE_INCREMENT);
                break;

            case KeyEvent.VK_SPACE:
                g_bRightMultiply = !g_bRightMultiply;
                System.out.println(g_bRightMultiply ? "Right-multiply" : "Left-multiply");
                break;
        }
    }
}

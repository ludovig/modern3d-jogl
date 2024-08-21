package com.mundoludo.modern3d.tut08;

import com.jogamp.newt.event.*;
import com.jogamp.opengl.GL3;
import com.jogamp.opengl.util.GLBuffers;

import glm_.mat4x4.Mat4;
import glm_.vec3.Vec3;
import glm_.vec4.Vec4;

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

public class GimbalLock extends Framework {

    public static void main(String[] args) {
        new GimbalLock().setup("Tutorial 08 - Gimbal Lock");
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

    private enum GimbalAxis {
        GIMBAL_X_AXIS,
        GIMBAL_Y_AXIS,
        GIMBAL_Z_AXIS,
    }

    private Mesh[] g_Gimbals = new Mesh[3];
    Mesh g_pObject;

    private final String[] g_strGimbalNames = {
        "LargeGimbal.xml",
        "MediumGimbal.xml",
        "SmallGimbal.xml"
    };

    private boolean g_bDrawGimbals = true;

    void DrawGimbal(GL3 gl, MatrixStack currMatrix, GimbalAxis eAxis, Vec4 baseColor) {
        if (!g_bDrawGimbals) {
            return;
        }

        currMatrix.push();

        switch (eAxis) {
            case GIMBAL_X_AXIS:
                break;
            case GIMBAL_Y_AXIS:
                currMatrix.rotateZ(90.0f);
                currMatrix.rotateX(90.0f);
                break;
            case GIMBAL_Z_AXIS:
                currMatrix.rotateY(90.0f);
                currMatrix.rotateX(90.0f);
                break;
        }


        gl.glUseProgram(theProgram);
        //Set the base color for this object.
        gl.glUniform4fv(baseColorUnif, 1, baseColor.to(VecBuffer));
        gl.glUniformMatrix4fv(modelToCameraMatrixUnif, 1, false, currMatrix.to(MatBuffer));

        g_Gimbals[eAxis.ordinal()].render(gl);
        gl.glUseProgram(0);

        currMatrix.pop();
    }

    @Override
    public void init(GL3 gl) {
        initializeProgram(gl);

        try {
            for (int iLoop = 0; iLoop < 3; iLoop++) {
                g_Gimbals[iLoop] = new Mesh(gl, getClass(), "tut08/" + g_strGimbalNames[iLoop]);
            }
            g_pObject = new Mesh(gl, getClass(), "tut08/Ship.xml");
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

    @Override
    public void display(GL3 gl) {

        gl.glClearColor(0, 0, 0, 0);
        gl.glClearDepth(1.0f);
        gl.glClear(GL3.GL_COLOR_BUFFER_BIT | GL3.GL_DEPTH_BUFFER_BIT);

        MatrixStack currMatrix = new MatrixStack();
        currMatrix.translate(new Vec3(0.0f, 0.0f, -200.0f));
        currMatrix.rotateX(g_angles.fAngleX);
        DrawGimbal(gl, currMatrix, GimbalAxis.GIMBAL_X_AXIS, new Vec4(0.4f, 0.4f, 1.0f, 1.0f));
        currMatrix.rotateY(g_angles.fAngleY);
        DrawGimbal(gl, currMatrix, GimbalAxis.GIMBAL_Y_AXIS, new Vec4(0.0f, 1.0f, 0.0f, 1.0f));
        currMatrix.rotateZ(g_angles.fAngleZ);
        DrawGimbal(gl, currMatrix, GimbalAxis.GIMBAL_Z_AXIS, new Vec4(1.0f, 0.3f, 0.3f, 1.0f));

        gl.glUseProgram(theProgram);
        currMatrix.scale(new Vec3(3.0f, 3.0f, 3.0f));
        currMatrix.rotateX(-90);
        //Set the base color for this object.
        gl.glUniform4f(baseColorUnif, 1.0f, 1.0f, 1.0f, 1.0f);
        gl.glUniformMatrix4fv(modelToCameraMatrixUnif, 1, false, currMatrix.to(MatBuffer));

        g_pObject.render(gl, "tint");

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

        g_pObject.dispose(gl);
        for (int i = 0; i < 3; i++) {
            g_Gimbals[i].dispose(gl);
        }
    }

    @Override
    public void keyPressed(KeyEvent keyEvent) {
        super.keyPressed(keyEvent);

        float SMALL_ANGLE_INCREMENT = 9.0f;

        switch (keyEvent.getKeyCode()) {
            case KeyEvent.VK_W:
                g_angles.fAngleX += SMALL_ANGLE_INCREMENT;
                break;
            case KeyEvent.VK_S:
                g_angles.fAngleX -= SMALL_ANGLE_INCREMENT;
                break;

            case KeyEvent.VK_A:
                g_angles.fAngleY += SMALL_ANGLE_INCREMENT;
                break;
            case KeyEvent.VK_D:
                g_angles.fAngleY -= SMALL_ANGLE_INCREMENT;
                break;

            case KeyEvent.VK_Q:
                g_angles.fAngleZ += SMALL_ANGLE_INCREMENT;
                break;
            case KeyEvent.VK_E:
                g_angles.fAngleZ -= SMALL_ANGLE_INCREMENT;
                break;

            case KeyEvent.VK_SPACE:
                g_bDrawGimbals = !g_bDrawGimbals;
                break;
        }
    }
}

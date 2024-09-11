package com.mundoludo.modern3d.tut08;

import com.jogamp.newt.event.*;
import com.jogamp.opengl.GL3;
import com.jogamp.opengl.util.GLBuffers;

import glm_.Java;
import glm_.mat4x4.Mat4;
import glm_.vec3.Vec3;
import glm_.vec4.Vec4;
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
import com.mundoludo.modern3d.framework.Timer;

public class Interpolation extends Framework {

    public static void main(String[] args) {
        new Interpolation().setup("Tutorial 08 - Interpolation");
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

    private final Quat[] g_Orients = {
        new Quat(0.7071f, 0.7071f, 0.0f, 0.0f),
        new Quat(0.5f, 0.5f, -0.5f, 0.5f),
        new Quat(-0.4895f, -0.7892f, -0.3700f, -0.02514f),
        new Quat(0.4895f, 0.7892f, 0.3700f, 0.02514f),

        new Quat(0.3840f, -0.1591f, -0.7991f, -0.4344f),
        new Quat(0.5537f, 0.5208f, 0.6483f, 0.0410f),
        new Quat(0.0f, 0.0f, 1.0f, 0.0f)
    };

    private final Short[] g_OrientKeys = {
        KeyEvent.VK_I,
        KeyEvent.VK_A,
        KeyEvent.VK_S,
        KeyEvent.VK_E,

        KeyEvent.VK_J,
        KeyEvent.VK_K,
        KeyEvent.VK_L
    };

    @Override
    public void init(GL3 gl) {
        initializeProgram(gl);

        try {
            g_pShip = new Mesh(gl, getClass(), "tut08/Ship.xml");
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

    Quat Lerp(Quat v0, Quat v1, float alpha) {
        Vec4 start = new Vec4();
        v0.vectorize(start);
        Vec4 end = new Vec4();
        v1.vectorize(end);
        Vec4 interp = Java.glm.mix(start, end, alpha);

        System.out.printf("alpha: %f, (%f, %f, %f, %f)\n", alpha, interp.getW(), interp.getX(), interp.getY(), interp.getZ());

        interp = interp.normalize();

        return new Quat(interp.getW(), interp.getX(), interp.getY(), interp.getZ());
    }

    Quat Slerp(Quat v0, Quat v1, float alpha)
    {
        float dot = Java.glm.dot(v0, v1);

        final float DOT_THRESHOLD = 0.9995f;
        if (dot > DOT_THRESHOLD)
            return Lerp(v0, v1, alpha);

        Java.glm.clamp(dot, -1.0f, 1.0f);
        float theta_0 = (float) Math.acos(dot);
        float theta = theta_0 * alpha;

        Quat v2 = v1.minus(v0.times(dot));
        v2 = v2.normalize();

        return v0.times((float)Math.cos(theta)).plus(v2.times((float)Math.sin(theta)));
    }

    public class Orientation {
        private boolean m_bIsAnimating = false;
        private int m_ixCurrOrient = 0;
        private boolean m_bSlerp = false;

        private final Animation m_anim = new Animation();

        public boolean ToggleSlerp() {
            m_bSlerp = !m_bSlerp;
            return m_bSlerp;
        }

        public Quat GetOrient() {
            if (m_bIsAnimating)
                return m_anim.GetOrient(g_Orients[m_ixCurrOrient], m_bSlerp);
            else
                return g_Orients[m_ixCurrOrient];
        }

        public boolean IsAnimating() {
            return m_bIsAnimating;
        }

        public void UpdateTime() {
            if (m_bIsAnimating) {
                boolean bIsFinished = m_anim.UpdateTime();
                if (bIsFinished) {
                    m_bIsAnimating = false;
                    m_ixCurrOrient = m_anim.GetFinalIx();
                }
            }
        }

        public void AnimateToOrient(int ixDestination) {
            if (m_ixCurrOrient == ixDestination)
                return;

            m_anim.StartAnimation(ixDestination, 1.0f);
            m_bIsAnimating = true;
        }

        private class Animation {
            private int m_ixFinalOrient;
            private Timer m_currTimer;

            //Returns true if the animation is over.
            public boolean UpdateTime() {
                return m_currTimer.update();
            }

            Quat GetOrient(Quat initial, boolean bSlerp) {
                if (bSlerp) {
                    return Slerp(initial, g_Orients[m_ixFinalOrient], m_currTimer.getAlpha());
                } else {
                    return Lerp(initial, g_Orients[m_ixFinalOrient], m_currTimer.getAlpha());
                }
            }

            void StartAnimation(int ixDestination, float fDuration) {
                m_ixFinalOrient = ixDestination;
                m_currTimer = new Timer(fDuration);
            }

            int GetFinalIx() {
                return m_ixFinalOrient;
            }
        }
    }

    private Orientation g_orient = new Orientation();

    @Override
    public void display(GL3 gl) {

        g_orient.UpdateTime();

        gl.glClearColor(0, 0, 0, 0);
        gl.glClearDepth(1.0f);
        gl.glClear(GL3.GL_COLOR_BUFFER_BIT | GL3.GL_DEPTH_BUFFER_BIT);

        MatrixStack currMatrix = new MatrixStack();
        currMatrix.translate(new Vec3(0.0f, 0.0f, -200.0f));
        currMatrix.applyMatrix(g_orient.GetOrient().toMat4());

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

    void ApplyOrientation(int iIndex)
    {
        if(!g_orient.IsAnimating())
            g_orient.AnimateToOrient(iIndex);
    }

    @Override
    public void keyPressed(KeyEvent keyEvent) {
        super.keyPressed(keyEvent);

        switch (keyEvent.getKeyCode()) {
            case KeyEvent.VK_SPACE:
                boolean bSlerp = g_orient.ToggleSlerp();
                System.out.println(bSlerp ? "Slerp" : "Lerp");
                break;
        }

        for (int iOrient = 0; iOrient < g_OrientKeys.length; iOrient++) {
            if (keyEvent.getKeyCode() == g_OrientKeys[iOrient])
                ApplyOrientation(iOrient);
        }

    }
}

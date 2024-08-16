package com.mundoludo.modern3d.framework;

import com.jogamp.newt.event.*;

import com.jogamp.opengl.GL;
import com.jogamp.opengl.GL3;
import com.jogamp.opengl.GLAutoDrawable;
import com.jogamp.opengl.GLEventListener;
import com.jogamp.opengl.GLProfile;
import com.jogamp.opengl.GLCapabilities;
import com.jogamp.newt.opengl.GLWindow;

import com.jogamp.opengl.util.*;

import java.util.ArrayList;

public class Framework implements GLEventListener, KeyListener {

    protected GLWindow window;
    protected Animator animator;

    private static int width=1920;
    private static int height=1080;

    public Framework(String title) {
        setup(title);
    }

    public Framework() {
    }

    public void setup(String title) {
        GLProfile glProfile = GLProfile.get(GLProfile.GL3);
        GLCapabilities glCapabilities = new GLCapabilities(glProfile);
        window = GLWindow.create(glCapabilities);

        window.setTitle(title);
        window.setSize(width, height);
        window.setUndecorated(false);
        window.setAlwaysOnTop(false);
        window.setFullscreen(false);
        window.setPointerVisible(true);
        window.confinePointer(false);
        window.setVisible(true);

        window.addGLEventListener(this);
        window.addKeyListener(this);

        Animator animator = new Animator();
        animator.add(window);
        animator.start();

        window.addWindowListener(new WindowAdapter() {
            @Override
            public void windowDestroyed(WindowEvent e) {
                new Thread(new Runnable() {
                    public void run() {

                        //stop the animator thread when user close the window
                        animator.stop();
                    }
                }).start();
            }
        });
    }

    @Override
    public final void init(GLAutoDrawable drawable) {
        GL3 gl = drawable.getGL().getGL3();
        System.err.println("Chosen GLCapabilities: " + drawable.getChosenGLCapabilities());
        System.err.println("INIT GL IS: " + gl.getClass().getName());
        System.err.println("GL_VENDOR: " + gl.glGetString(GL.GL_VENDOR));
        System.err.println("GL_RENDERER: " + gl.glGetString(GL.GL_RENDERER));
        System.err.println("GL_VERSION: " + gl.glGetString(GL.GL_VERSION));

        init(gl);
    }

    protected String findResource(String fileName) {
        ClassLoader classLoader = getClass().getClassLoader();

        return TextResourceReader.readTextFileFromResource(classLoader, fileName);
    }

    protected int createShader(GL3 gl, int shaderType, String shaderFile) {
        return ShaderUtil.createShader(gl, shaderType, shaderFile);
    }

    protected int createProgram(GL3 gl, ArrayList<Integer> shaderList) {
        return ShaderUtil.createProgram(gl, shaderList);
    }

    protected void init(GL3 gl) {
    }

    @Override
    public final void display(GLAutoDrawable drawable) {
        display(drawable.getGL().getGL3());
    }

    protected void display(GL3 gl) {
    }

    @Override
    public final void reshape(GLAutoDrawable drawable, int x, int y, int z, int h) {
        System.out.println("Window resized to width=" + z + " height=" + h);
        width = z;
        height = h;

        // Get gl
        GL3 gl = drawable.getGL().getGL3();

        reshape(gl, width, height);
    }

    protected void reshape(GL3 gl, int width, int height) {
    }

    @Override
    public void dispose(GLAutoDrawable drawable) {
        System.out.println("cleanup, remember to release shaders");
        GL3 gl = drawable.getGL().getGL3();

        end(gl);
    }

    protected void end(GL3 gl) {
    }

    @Override
    public void keyPressed(KeyEvent keyEvent) {

        switch (keyEvent.getKeyCode()) {
            case KeyEvent.VK_ESCAPE:
                quit();
                break;
        }
    }

    @Override
    public void keyReleased(KeyEvent e) {
    }

    /**
     * Note: calling System.exit() synchronously inside the draw, reshape or init callbacks can lead to deadlocks on
     * certain platforms (in particular, X11) because the JAWT's locking routines cause a global AWT lock to be grabbed.
     * Instead run the exit routine in another thread.
     */
    protected void quit() {
        new Thread(new Runnable() {
            public void run() {
                window.destroy();
            }
        }).start();
    }

    public static float degToRad(float angDeg)
    {
        final float degToRad = (float) Math.PI * 2.0f / 360.0f;
        return angDeg * degToRad;
    }

    public static float clamp(float fValue, float fMinValue, float fMaxValue)
    {
        if(fValue < fMinValue)
            return fMinValue;

        if(fValue > fMaxValue)
            return fMaxValue;

        return fValue;
    }
}

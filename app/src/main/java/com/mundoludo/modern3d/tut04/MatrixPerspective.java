package com.mundoludo.modern3d.tut04;

import com.jogamp.newt.event.KeyEvent;
import com.jogamp.opengl.GL;
import com.jogamp.opengl.GL3;
import com.jogamp.opengl.GL2ES2;
import com.jogamp.newt.opengl.GLWindow;
import com.jogamp.opengl.util.GLBuffers;

import java.nio.ByteBuffer;
import java.nio.FloatBuffer;
import java.nio.IntBuffer;
import java.util.ArrayList;

import static com.jogamp.opengl.GL.GL_ARRAY_BUFFER;
import static com.jogamp.opengl.GL.GL_CULL_FACE;
import static com.jogamp.opengl.GL.GL_BACK;
import static com.jogamp.opengl.GL.GL_CW;
import static com.jogamp.opengl.GL.GL_FALSE;
import static com.jogamp.opengl.GL.GL_FLOAT;
import static com.jogamp.opengl.GL.GL_TRIANGLES;
import static com.jogamp.opengl.GL.GL_STATIC_DRAW;
import static com.jogamp.opengl.GL2ES2.*;
import static com.jogamp.opengl.GL2ES3.GL_COLOR;
import static com.jogamp.opengl.GL3ES3.GL_GEOMETRY_SHADER;

import com.mundoludo.modern3d.framework.Framework;

public class MatrixPerspective extends Framework {

    public static void main(String[] args) {
        new MatrixPerspective().setup("Tutorial 04 - Matrix Perspective");
    }

    private int theProgram;
    private int offsetUniform;

    private float[] vertexData = {
        +0.25f, +0.25f, -1.25f, 1.0f,
        +0.25f, -0.25f, -1.25f, 1.0f,
        -0.25f, +0.25f, -1.25f, 1.0f,

        +0.25f, -0.25f, -1.25f, 1.0f,
        -0.25f, -0.25f, -1.25f, 1.0f,
        -0.25f, +0.25f, -1.25f, 1.0f,

        +0.25f, +0.25f, -2.75f, 1.0f,
        -0.25f, +0.25f, -2.75f, 1.0f,
        +0.25f, -0.25f, -2.75f, 1.0f,

        +0.25f, -0.25f, -2.75f, 1.0f,
        -0.25f, +0.25f, -2.75f, 1.0f,
        -0.25f, -0.25f, -2.75f, 1.0f,

        -0.25f, +0.25f, -1.25f, 1.0f,
        -0.25f, -0.25f, -1.25f, 1.0f,
        -0.25f, -0.25f, -2.75f, 1.0f,

        -0.25f, +0.25f, -1.25f, 1.0f,
        -0.25f, -0.25f, -2.75f, 1.0f,
        -0.25f, +0.25f, -2.75f, 1.0f,

        +0.25f, +0.25f, -1.25f, 1.0f,
        +0.25f, -0.25f, -2.75f, 1.0f,
        +0.25f, -0.25f, -1.25f, 1.0f,

        +0.25f, +0.25f, -1.25f, 1.0f,
        +0.25f, +0.25f, -2.75f, 1.0f,
        +0.25f, -0.25f, -2.75f, 1.0f,

        +0.25f, +0.25f, -2.75f, 1.0f,
        +0.25f, +0.25f, -1.25f, 1.0f,
        -0.25f, +0.25f, -1.25f, 1.0f,

        +0.25f, +0.25f, -2.75f, 1.0f,
        -0.25f, +0.25f, -1.25f, 1.0f,
        -0.25f, +0.25f, -2.75f, 1.0f,

        +0.25f, -0.25f, -2.75f, 1.0f,
        -0.25f, -0.25f, -1.25f, 1.0f,
        +0.25f, -0.25f, -1.25f, 1.0f,

        +0.25f, -0.25f, -2.75f, 1.0f,
        -0.25f, -0.25f, -2.75f, 1.0f,
        -0.25f, -0.25f, -1.25f, 1.0f,


        0.0f, 0.0f, 1.0f, 1.0f,
        0.0f, 0.0f, 1.0f, 1.0f,
        0.0f, 0.0f, 1.0f, 1.0f,

        0.0f, 0.0f, 1.0f, 1.0f,
        0.0f, 0.0f, 1.0f, 1.0f,
        0.0f, 0.0f, 1.0f, 1.0f,

        0.8f, 0.8f, 0.8f, 1.0f,
        0.8f, 0.8f, 0.8f, 1.0f,
        0.8f, 0.8f, 0.8f, 1.0f,

        0.8f, 0.8f, 0.8f, 1.0f,
        0.8f, 0.8f, 0.8f, 1.0f,
        0.8f, 0.8f, 0.8f, 1.0f,

        0.0f, 1.0f, 0.0f, 1.0f,
        0.0f, 1.0f, 0.0f, 1.0f,
        0.0f, 1.0f, 0.0f, 1.0f,

        0.0f, 1.0f, 0.0f, 1.0f,
        0.0f, 1.0f, 0.0f, 1.0f,
        0.0f, 1.0f, 0.0f, 1.0f,

        0.5f, 0.5f, 0.0f, 1.0f,
        0.5f, 0.5f, 0.0f, 1.0f,
        0.5f, 0.5f, 0.0f, 1.0f,

        0.5f, 0.5f, 0.0f, 1.0f,
        0.5f, 0.5f, 0.0f, 1.0f,
        0.5f, 0.5f, 0.0f, 1.0f,

        1.0f, 0.0f, 0.0f, 1.0f,
        1.0f, 0.0f, 0.0f, 1.0f,
        1.0f, 0.0f, 0.0f, 1.0f,

        1.0f, 0.0f, 0.0f, 1.0f,
        1.0f, 0.0f, 0.0f, 1.0f,
        1.0f, 0.0f, 0.0f, 1.0f,

        0.0f, 1.0f, 1.0f, 1.0f,
        0.0f, 1.0f, 1.0f, 1.0f,
        0.0f, 1.0f, 1.0f, 1.0f,

        0.0f, 1.0f, 1.0f, 1.0f,
        0.0f, 1.0f, 1.0f, 1.0f,
        0.0f, 1.0f, 1.0f, 1.0f,
    };

    private long startingTime;

    private IntBuffer positionBufferObject = GLBuffers.newDirectIntBuffer(1);

    @Override
    public void init(GL3 gl) {
        initializeProgram(gl);

        initializeVertexBuffer(gl);

        gl.glEnable(GL_CULL_FACE);
	    gl.glCullFace(GL_BACK);
        gl.glFrontFace(GL_CW);
    }

    private void initializeProgram(GL3 gl) {
        ArrayList<Integer> shaderList = new ArrayList<>();

        String vertShader = findResource("tut04/matrix_perspective.vert");
        String fragShader = findResource("tut04/standard_colors.frag");

        shaderList.add(createShader(gl, GL_VERTEX_SHADER, vertShader));
        shaderList.add(createShader(gl, GL_FRAGMENT_SHADER, fragShader));

        theProgram = createProgram(gl, shaderList);
        shaderList.forEach(gl::glDeleteShader);

        offsetUniform = gl.glGetUniformLocation(theProgram, "offset");
        int perspectiveMatrixUnif = gl.glGetUniformLocation(theProgram, "perspectiveMatrix");

        float frustrumScale = 1.0f;
        float zNear = 1.0f;
        float zFar = 3.0f;

        FloatBuffer theMatrix = GLBuffers.newDirectFloatBuffer(16);

        theMatrix.put(0, frustrumScale);
        theMatrix.put(5, frustrumScale);
        theMatrix.put(10, (zFar + zNear) / (zNear - zFar));
        theMatrix.put(14, (2 * zFar * zNear) / (zNear - zFar));
        theMatrix.put(11, -1.0f);

        gl.glUseProgram(theProgram);
        gl.glUniformMatrix4fv(perspectiveMatrixUnif, 1, false, theMatrix);
        gl.glUseProgram(0);
    }

    private void initializeVertexBuffer(GL3 gl)
    {
        FloatBuffer vertexBuffer = GLBuffers.newDirectFloatBuffer(vertexData);

        gl.glGenBuffers(1, positionBufferObject);
        gl.glBindBuffer(GL_ARRAY_BUFFER, positionBufferObject.get(0));
        int numBytes = vertexData.length * Float.BYTES;
        gl.glBufferData(GL_ARRAY_BUFFER, numBytes, vertexBuffer, GL_STATIC_DRAW);
        gl.glBindBuffer(GL_ARRAY_BUFFER, 0);
    }

    @Override
    public void display(GL3 gl) {

        gl.glClearColor(0, 0, 0, 0);
        gl.glClear(GL3.GL_COLOR_BUFFER_BIT);

        gl.glUseProgram(theProgram);

        gl.glUniform2f(offsetUniform, 0.5f, 0.5f);

        int colorData = vertexData.length * Float.BYTES / 2;
        gl.glBindBuffer(GL_ARRAY_BUFFER, positionBufferObject.get(0));
        gl.glEnableVertexAttribArray(0);
        gl.glEnableVertexAttribArray(1);
        gl.glVertexAttribPointer(0, 4, GL_FLOAT, false, 0, 0);
        gl.glVertexAttribPointer(1, 4, GL_FLOAT, false, 0, colorData);

        gl.glDrawArrays(GL_TRIANGLES, 0, 36);

        gl.glDisableVertexAttribArray(0);
        gl.glDisableVertexAttribArray(1);
        gl.glUseProgram(0);
    }

    /**
     * Called whenever the window is resized. The new window size is given, in
     * pixels. This is an opportunity to call glViewport or glScissor to keep up
     * with the change in size.
     *
     * @param gl
     * @param w
     * @param h
     */
    @Override
    public void reshape(GL3 gl, int w, int h) {
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
        gl.glDeleteBuffers(1, positionBufferObject);
    }
}

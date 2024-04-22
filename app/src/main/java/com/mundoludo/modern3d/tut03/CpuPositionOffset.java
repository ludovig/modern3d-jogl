package com.mundoludo.modern3d.tut03;

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
import static com.jogamp.opengl.GL.GL_FALSE;
import static com.jogamp.opengl.GL.GL_FLOAT;
import static com.jogamp.opengl.GL.GL_TRIANGLES;
import static com.jogamp.opengl.GL2.GL_STREAM_DRAW;
import static com.jogamp.opengl.GL2ES2.*;
import static com.jogamp.opengl.GL2ES3.GL_COLOR;
import static com.jogamp.opengl.GL3ES3.GL_GEOMETRY_SHADER;

import com.mundoludo.modern3d.framework.Framework;

public class CpuPositionOffset extends Framework {

    public static void main(String[] args) {
        new CpuPositionOffset().setup("Tutorial 03 - Cpu Position Offset");
    }

    private int theProgram;

    private float[] vertexPositions = {
        +0.25f, +0.25f, 0.0f, 1.0f,
        +0.25f, -0.25f, 0.0f, 1.0f,
        -0.25f, -0.25f, 0.0f, 1.0f
    };

    private long startingTime;

    private IntBuffer positionBufferObject = GLBuffers.newDirectIntBuffer(1);

    @Override
    public void init(GL3 gl) {
        initializeProgram(gl);

        initializeVertexBuffer(gl);

        startingTime = System.currentTimeMillis();
    }

    private void initializeProgram(GL3 gl) {
        ArrayList<Integer> shaderList = new ArrayList<>();

        String vertShader = findResource("tut03/standard.vert");
        String fragShader = findResource("tut03/standard.frag");

        shaderList.add(createShader(gl, GL_VERTEX_SHADER, vertShader));
        shaderList.add(createShader(gl, GL_FRAGMENT_SHADER, fragShader));

        theProgram = createProgram(gl, shaderList);

        shaderList.forEach(gl::glDeleteShader);
    }

    private void initializeVertexBuffer(GL3 gl)
    {
        FloatBuffer vertexBuffer = GLBuffers.newDirectFloatBuffer(vertexPositions);

        gl.glGenBuffers(1, positionBufferObject);
        gl.glBindBuffer(GL_ARRAY_BUFFER, positionBufferObject.get(0));
        int numBytes = vertexPositions.length * Float.BYTES;
        gl.glBufferData(GL_ARRAY_BUFFER, numBytes, vertexBuffer, GL_STREAM_DRAW);
        gl.glBindBuffer(GL_ARRAY_BUFFER, 0);
    }

    public float[] computePositionOffset() {
        float Xoffset;
        float Yoffset;

        float loopDuration = 5.0f;
        float scale = (float) Math.PI * 2.0f / loopDuration;

        float elapsedTime = (System.currentTimeMillis() - startingTime) / 1_000.0f;

        float currTimeThroughLoop = elapsedTime % loopDuration;

        Xoffset = (float) Math.cos(currTimeThroughLoop * scale) * 0.5f;
        Yoffset = (float) Math.sin(currTimeThroughLoop * scale) * 0.5f;

        float[] vec2 = {Xoffset, Yoffset};
        return vec2;
    }

    public void AdjustVertexData(GL3 gl, float[] vec2) {
        float Xoffset = vec2[0];
        float Yoffset = vec2[1];
        float[] newData = new float[vertexPositions.length];
        System.arraycopy(vertexPositions, 0, newData, 0, vertexPositions.length);

        for (int iVertex = 0; iVertex < vertexPositions.length; iVertex += 4) {
            newData[iVertex] += Xoffset;
            newData[iVertex + 1] += Yoffset;
        }

        FloatBuffer buffer = GLBuffers.newDirectFloatBuffer(newData);

        gl.glBindBuffer(GL_ARRAY_BUFFER, positionBufferObject.get(0));
        int numBytes = vertexPositions.length * Float.BYTES;
        gl.glBufferSubData(GL_ARRAY_BUFFER, 0, numBytes, buffer);
        gl.glBindBuffer(GL_ARRAY_BUFFER, 0);
    }

    @Override
    public void display(GL3 gl) {

        float[] vec2 = computePositionOffset();
        AdjustVertexData(gl, vec2);

        gl.glClearColor(0, 0, 0, 0);
        gl.glClear(GL3.GL_COLOR_BUFFER_BIT);

        gl.glUseProgram(theProgram);

        gl.glBindBuffer(GL_ARRAY_BUFFER, positionBufferObject.get(0));
        gl.glEnableVertexAttribArray(0);
        gl.glVertexAttribPointer(0, 4, GL_FLOAT, false, 0, 0);

        gl.glDrawArrays(GL_TRIANGLES, 0, 3);

        gl.glDisableVertexAttribArray(0);
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
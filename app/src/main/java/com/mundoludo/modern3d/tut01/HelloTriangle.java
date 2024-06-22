package com.mundoludo.modern3d.tut01;

import com.jogamp.opengl.GL3;
import com.jogamp.opengl.util.GLBuffers;

import java.nio.ByteBuffer;
import java.nio.FloatBuffer;
import java.nio.IntBuffer;
import java.util.ArrayList;

import static com.jogamp.opengl.GL.GL_ARRAY_BUFFER;
import static com.jogamp.opengl.GL.GL_FALSE;
import static com.jogamp.opengl.GL.GL_FLOAT;
import static com.jogamp.opengl.GL.GL_STATIC_DRAW;
import static com.jogamp.opengl.GL.GL_TRIANGLES;
import static com.jogamp.opengl.GL2ES2.*;
import static com.jogamp.opengl.GL3ES3.GL_GEOMETRY_SHADER;

import com.mundoludo.modern3d.framework.Framework;

public class HelloTriangle extends Framework {

    public static void main(String[] args) {
        new HelloTriangle().setup("Tutorial 01 - Hello Triangle");
    }

    private final String strVertexShader =
        "#version 330\n" +
        "#define POSITION 0\n" +
        "layout(location = POSITION) in vec4 position;\n" +
        "void main()\n" +
        "{\n" +
        "   gl_Position = position;\n" +
        "}\n";

    private final String strFragmentShader =
        "#version 330\n" +
        "out vec4 outputColor;\n" +
        "void main()\n" +
        "{\n" +
        "   outputColor = vec4(1.0f, 1.0f, 1.0f, 1.0f);\n" +
        "}\n";

    private int theProgram;

    private float[] vertexPositions = {
         0.75f,  0.75f, 0.0f, 1.0f,
         0.75f, -0.75f, 0.0f, 1.0f,
        -0.75f, -0.75f, 0.0f, 1.0f
    };

    private IntBuffer positionBufferObject = GLBuffers.newDirectIntBuffer(1);

    /**
     * Called after the window and OpenGL are initialized.
     * Called exactly once, before the main loop.
     */
    @Override
    public void init(GL3 gl) {
        initializeProgram(gl);

        initializeVertexBuffer(gl);
    }

    private void initializeProgram(GL3 gl) {
        ArrayList<Integer> shaderList = new ArrayList<>();

        shaderList.add(createShader(gl, GL_VERTEX_SHADER, strVertexShader));
        shaderList.add(createShader(gl, GL_FRAGMENT_SHADER, strFragmentShader));

        theProgram = createProgram(gl, shaderList);

        shaderList.forEach(gl::glDeleteShader);
    }

    @Override
    protected int createShader(GL3 gl, int shaderType, String shaderFile) {

        int shader = gl.glCreateShader(shaderType);
        String[] lines = {shaderFile};
        IntBuffer length = GLBuffers.newDirectIntBuffer(new int[]{lines[0].length()});
        gl.glShaderSource(shader, 1, lines, length);

        gl.glCompileShader(shader);

        IntBuffer status = GLBuffers.newDirectIntBuffer(1);
        gl.glGetShaderiv(shader, GL_COMPILE_STATUS, status);
        if (status.get(0) == GL_FALSE) {
            IntBuffer infoLogLength = GLBuffers.newDirectIntBuffer(1);
            gl.glGetShaderiv(shader, GL_INFO_LOG_LENGTH, infoLogLength);

            ByteBuffer bufferInfoLog = GLBuffers.newDirectByteBuffer(infoLogLength.get(0));
            gl.glGetShaderInfoLog(shader, infoLogLength.get(0), null, bufferInfoLog);
            byte[] bytes = new byte[infoLogLength.get(0)];
            bufferInfoLog.get(bytes);
            String strInfoLog = new String(bytes);

            String strShaderType = "";
            switch (shaderType) {
                case GL_VERTEX_SHADER:
                    strShaderType = "vertex";
                    break;
                case GL_GEOMETRY_SHADER:
                    strShaderType = "geometry";
                    break;
                case GL_FRAGMENT_SHADER:
                    strShaderType = "fragment";
                    break;
            }
            System.err.println("Compile failure in " + strShaderType + " shader: " + strInfoLog);

        }

        return shader;
    }

    @Override
    protected int createProgram(GL3 gl, ArrayList<Integer> shaderList) {

        int program = gl.glCreateProgram();

        shaderList.forEach(shader -> gl.glAttachShader(program, shader));

        gl.glLinkProgram(program);

        IntBuffer status = GLBuffers.newDirectIntBuffer(1);
        gl.glGetProgramiv(program, GL_LINK_STATUS, status);
        if (status.get(0) == GL_FALSE) {

            IntBuffer infoLogLength = GLBuffers.newDirectIntBuffer(1);
            gl.glGetProgramiv(program, GL_INFO_LOG_LENGTH, infoLogLength);

            ByteBuffer bufferInfoLog = GLBuffers.newDirectByteBuffer(infoLogLength.get(0));
            gl.glGetProgramInfoLog(program, infoLogLength.get(0), null, bufferInfoLog);
            byte[] bytes = new byte[infoLogLength.get(0)];
            bufferInfoLog.get(bytes);
            String strInfoLog = new String(bytes);

            System.err.println("Linker failure: " + strInfoLog);
        }

        shaderList.forEach(shader -> gl.glDetachShader(program, shader));

        return program;
    }

    private void initializeVertexBuffer(GL3 gl)
    {
        FloatBuffer vertexBuffer = GLBuffers.newDirectFloatBuffer(vertexPositions);

        gl.glGenBuffers(1, positionBufferObject);
        gl.glBindBuffer(GL_ARRAY_BUFFER, positionBufferObject.get(0));
        int numBytes = vertexPositions.length * Float.BYTES;
        gl.glBufferData(GL_ARRAY_BUFFER, numBytes, vertexBuffer, GL_STATIC_DRAW);
        gl.glBindBuffer(GL_ARRAY_BUFFER, 0);
    }

    /**
     * Called to update the display. You don't need to swap the buffers after
     * all of your rendering to display what you rendered, it is done
     * automatically.
     *
     * @param gl
     */
    @Override
    public void display(GL3 gl) {

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
     * Called whenever the window is resized. The new window size is given, in pixels.
     * This is an opportunity to call glViewport or glScissor to keep up with the change in size.
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

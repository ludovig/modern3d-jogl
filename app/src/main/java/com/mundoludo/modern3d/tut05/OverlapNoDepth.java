package com.mundoludo.modern3d.tut05;

import com.jogamp.opengl.GL3;
import com.jogamp.opengl.util.GLBuffers;

import java.nio.FloatBuffer;
import java.nio.IntBuffer;
import java.nio.ShortBuffer;
import java.util.ArrayList;

import static com.jogamp.opengl.GL.GL_ARRAY_BUFFER;
import static com.jogamp.opengl.GL.GL_CULL_FACE;
import static com.jogamp.opengl.GL.GL_BACK;
import static com.jogamp.opengl.GL.GL_CW;
import static com.jogamp.opengl.GL.GL_FLOAT;
import static com.jogamp.opengl.GL.GL_TRIANGLES;
import static com.jogamp.opengl.GL.GL_STATIC_DRAW;
import static com.jogamp.opengl.GL2ES2.*;

import com.mundoludo.modern3d.framework.Framework;

public class OverlapNoDepth extends Framework {

    public static void main(String[] args) {
        new OverlapNoDepth().setup("Tutorial 05 - Overlap No Depth");
    }

    private int theProgram;

    private int offsetUniform;
    private int perspectiveMatrixUnif;

    private float frustrumScale = 1.0f;
    private FloatBuffer perspectiveMatrix = GLBuffers.newDirectFloatBuffer(16);

    private IntBuffer vertexBufferObject = GLBuffers.newDirectIntBuffer(1);
    private IntBuffer indexBufferObject = GLBuffers.newDirectIntBuffer(1);

    private void initializeProgram(GL3 gl) {
        ArrayList<Integer> shaderList = new ArrayList<>();

        String vertShader = findResource("tut05/standard.vert");
        String fragShader = findResource("tut05/standard.frag");

        shaderList.add(createShader(gl, GL_VERTEX_SHADER, vertShader));
        shaderList.add(createShader(gl, GL_FRAGMENT_SHADER, fragShader));

        theProgram = createProgram(gl, shaderList);
        shaderList.forEach(gl::glDeleteShader);

        offsetUniform = gl.glGetUniformLocation(theProgram, "offset");
        perspectiveMatrixUnif = gl.glGetUniformLocation(theProgram, "perspectiveMatrix");

        float zNear = 1f;
        float zFar = 3.0f;

        perspectiveMatrix.put(0, frustrumScale);
        perspectiveMatrix.put(5, frustrumScale);
        perspectiveMatrix.put(10, (zFar + zNear) / (zNear - zFar));
        perspectiveMatrix.put(14, (2 * zFar * zNear) / (zNear - zFar));
        perspectiveMatrix.put(11, -1.0f);

        gl.glUseProgram(theProgram);
        gl.glUniformMatrix4fv(perspectiveMatrixUnif, 1, false, perspectiveMatrix);
        gl.glUseProgram(0);
    }

    private int numberOfVertices = 36;

    private float RIGHT_EXTENT = 0.8f;
    private float LEFT_EXTENT = -RIGHT_EXTENT;
    private float TOP_EXTENT = 0.20f;
    private float MIDDLE_EXTENT = 0.0f;
    private float BOTTOM_EXTENT = -TOP_EXTENT;
    private float FRONT_EXTENT = -1.25f;
    private float REAR_EXTENT = -1.75f;

    private float[] GREEN_COLOR = {0.75f, 0.75f, 1.0f, 1.0f};
    private float[] BLUE_COLOR = {0.0f, 0.5f, 0.0f, 1.0f};
    private float[] RED_COLOR = {1.0f, 0.0f, 0.0f, 1.0f};
    private float[] GREY_COLOR = {0.8f, 0.8f, 0.8f, 1.0f};
    private float[] BROWN_COLOR = {0.5f, 0.5f, 0.0f, 1.0f};

    private float[] vertexData = {
	//Object 1 positions
    LEFT_EXTENT,	TOP_EXTENT,	    REAR_EXTENT,
    LEFT_EXTENT,	MIDDLE_EXTENT,	FRONT_EXTENT,
    RIGHT_EXTENT,	MIDDLE_EXTENT,	FRONT_EXTENT,
    RIGHT_EXTENT,	TOP_EXTENT,	    REAR_EXTENT,

	LEFT_EXTENT,	BOTTOM_EXTENT,	REAR_EXTENT,
	LEFT_EXTENT,	MIDDLE_EXTENT,	FRONT_EXTENT,
	RIGHT_EXTENT,	MIDDLE_EXTENT,	FRONT_EXTENT,
	RIGHT_EXTENT,	BOTTOM_EXTENT,	REAR_EXTENT,

	LEFT_EXTENT,	TOP_EXTENT,	    REAR_EXTENT,
	LEFT_EXTENT,	MIDDLE_EXTENT,	FRONT_EXTENT,
	LEFT_EXTENT,	BOTTOM_EXTENT,	REAR_EXTENT,

	RIGHT_EXTENT,	TOP_EXTENT,	    REAR_EXTENT,
	RIGHT_EXTENT,	MIDDLE_EXTENT,	FRONT_EXTENT,
	RIGHT_EXTENT,	BOTTOM_EXTENT,	REAR_EXTENT,

	LEFT_EXTENT,	BOTTOM_EXTENT,	REAR_EXTENT,
	LEFT_EXTENT,	TOP_EXTENT,	    REAR_EXTENT,
	RIGHT_EXTENT,	TOP_EXTENT,	    REAR_EXTENT,
	RIGHT_EXTENT,	BOTTOM_EXTENT,	REAR_EXTENT,

    //	0, 2, 1,
    //	3, 2, 0,

	//Object 2 positions
	TOP_EXTENT,	RIGHT_EXTENT,	REAR_EXTENT,
	MIDDLE_EXTENT,	RIGHT_EXTENT,	FRONT_EXTENT,
	MIDDLE_EXTENT,	LEFT_EXTENT,	FRONT_EXTENT,
	TOP_EXTENT,	LEFT_EXTENT,	REAR_EXTENT,

	BOTTOM_EXTENT,	RIGHT_EXTENT,	REAR_EXTENT,
	MIDDLE_EXTENT,	RIGHT_EXTENT,	FRONT_EXTENT,
	MIDDLE_EXTENT,	LEFT_EXTENT,	FRONT_EXTENT,
	BOTTOM_EXTENT,	LEFT_EXTENT,	REAR_EXTENT,

	TOP_EXTENT,	RIGHT_EXTENT,	REAR_EXTENT,
	MIDDLE_EXTENT,	RIGHT_EXTENT,	FRONT_EXTENT,
	BOTTOM_EXTENT,	RIGHT_EXTENT,	REAR_EXTENT,

	TOP_EXTENT,	LEFT_EXTENT,	REAR_EXTENT,
	MIDDLE_EXTENT,	LEFT_EXTENT,	FRONT_EXTENT,
	BOTTOM_EXTENT,	LEFT_EXTENT,	REAR_EXTENT,

	BOTTOM_EXTENT,	RIGHT_EXTENT,	REAR_EXTENT,
	TOP_EXTENT,	RIGHT_EXTENT,	REAR_EXTENT,
	TOP_EXTENT,	LEFT_EXTENT,	REAR_EXTENT,
	BOTTOM_EXTENT,	LEFT_EXTENT,	REAR_EXTENT,

	//Object 1 colors
        GREEN_COLOR[0], GREEN_COLOR[1], GREEN_COLOR[2], GREEN_COLOR[3],
        GREEN_COLOR[0], GREEN_COLOR[1], GREEN_COLOR[2], GREEN_COLOR[3],
        GREEN_COLOR[0], GREEN_COLOR[1], GREEN_COLOR[2], GREEN_COLOR[3],
        GREEN_COLOR[0], GREEN_COLOR[1], GREEN_COLOR[2], GREEN_COLOR[3],

        BLUE_COLOR[0], BLUE_COLOR[1], BLUE_COLOR[2], BLUE_COLOR[3],
        BLUE_COLOR[0], BLUE_COLOR[1], BLUE_COLOR[2], BLUE_COLOR[3],
        BLUE_COLOR[0], BLUE_COLOR[1], BLUE_COLOR[2], BLUE_COLOR[3],
        BLUE_COLOR[0], BLUE_COLOR[1], BLUE_COLOR[2], BLUE_COLOR[3],

        RED_COLOR[0], RED_COLOR[1], RED_COLOR[2], RED_COLOR[3],
        RED_COLOR[0], RED_COLOR[1], RED_COLOR[2], RED_COLOR[3],
        RED_COLOR[0], RED_COLOR[1], RED_COLOR[2], RED_COLOR[3],

        GREY_COLOR[0], GREY_COLOR[1], GREY_COLOR[2], GREY_COLOR[3],
        GREY_COLOR[0], GREY_COLOR[1], GREY_COLOR[2], GREY_COLOR[3],
        GREY_COLOR[0], GREY_COLOR[1], GREY_COLOR[2], GREY_COLOR[3],

        BROWN_COLOR[0], BROWN_COLOR[1], BROWN_COLOR[2], BROWN_COLOR[3],
        BROWN_COLOR[0], BROWN_COLOR[1], BROWN_COLOR[2], BROWN_COLOR[3],
        BROWN_COLOR[0], BROWN_COLOR[1], BROWN_COLOR[2], BROWN_COLOR[3],
        BROWN_COLOR[0], BROWN_COLOR[1], BROWN_COLOR[2], BROWN_COLOR[3],


        //Object 2 colors
        RED_COLOR[0], RED_COLOR[1], RED_COLOR[2], RED_COLOR[3],
        RED_COLOR[0], RED_COLOR[1], RED_COLOR[2], RED_COLOR[3],
        RED_COLOR[0], RED_COLOR[1], RED_COLOR[2], RED_COLOR[3],
        RED_COLOR[0], RED_COLOR[1], RED_COLOR[2], RED_COLOR[3],

        BROWN_COLOR[0], BROWN_COLOR[1], BROWN_COLOR[2], BROWN_COLOR[3],
        BROWN_COLOR[0], BROWN_COLOR[1], BROWN_COLOR[2], BROWN_COLOR[3],
        BROWN_COLOR[0], BROWN_COLOR[1], BROWN_COLOR[2], BROWN_COLOR[3],
        BROWN_COLOR[0], BROWN_COLOR[1], BROWN_COLOR[2], BROWN_COLOR[3],

        BLUE_COLOR[0], BLUE_COLOR[1], BLUE_COLOR[2], BLUE_COLOR[3],
        BLUE_COLOR[0], BLUE_COLOR[1], BLUE_COLOR[2], BLUE_COLOR[3],
        BLUE_COLOR[0], BLUE_COLOR[1], BLUE_COLOR[2], BLUE_COLOR[3],
        BLUE_COLOR[0], BLUE_COLOR[1], BLUE_COLOR[2], BLUE_COLOR[3],

        GREEN_COLOR[0], GREEN_COLOR[1], GREEN_COLOR[2], GREEN_COLOR[3],
        GREEN_COLOR[0], GREEN_COLOR[1], GREEN_COLOR[2], GREEN_COLOR[3],
        GREEN_COLOR[0], GREEN_COLOR[1], GREEN_COLOR[2], GREEN_COLOR[3],

        GREY_COLOR[0], GREY_COLOR[1], GREY_COLOR[2], GREY_COLOR[3],
        GREY_COLOR[0], GREY_COLOR[1], GREY_COLOR[2], GREY_COLOR[3],
        GREY_COLOR[0], GREY_COLOR[1], GREY_COLOR[2], GREY_COLOR[3],
        GREY_COLOR[0], GREY_COLOR[1], GREY_COLOR[2], GREY_COLOR[3]
    };

    private short[] indexData = {
        0, 2, 1,
        3, 2, 0,

        4, 5, 6,
        6, 7, 4,

        8, 9, 10,
        11, 13, 12,

        14, 16, 15,
        17, 16, 14,
    };

    private IntBuffer vaoObject1 = GLBuffers.newDirectIntBuffer(1);
    private IntBuffer vaoObject2 = GLBuffers.newDirectIntBuffer(1);

    private void initializeVertexBuffer(GL3 gl)
    {
        FloatBuffer vertexBuffer = GLBuffers.newDirectFloatBuffer(vertexData);
        int dataNumBytes = vertexBuffer.capacity() * Float.BYTES;

        gl.glGenBuffers(1, vertexBufferObject);
        gl.glBindBuffer(GL_ARRAY_BUFFER, vertexBufferObject.get(0));
        gl.glBufferData(GL_ARRAY_BUFFER, dataNumBytes, vertexBuffer, GL_STATIC_DRAW);

        ShortBuffer indexBuffer = GLBuffers.newDirectShortBuffer(indexData);
        int indexNumBytes = indexBuffer.capacity() * Short.BYTES;
        gl.glGenBuffers(1, indexBufferObject);
        gl.glBindBuffer(GL_ELEMENT_ARRAY_BUFFER, indexBufferObject.get(0));
        gl.glBufferData(GL_ELEMENT_ARRAY_BUFFER, indexNumBytes, indexBuffer, GL_STATIC_DRAW);
        gl.glBindBuffer(GL_ELEMENT_ARRAY_BUFFER, 0);
    }

    public void initializeVertexArrayObjects(GL3 gl) {
        gl.glGenVertexArrays(1, vaoObject1);
        gl.glBindVertexArray(vaoObject1.get(0));

        int colorDataOffset = Float.BYTES * 3 * numberOfVertices;

        gl.glBindBuffer(GL_ARRAY_BUFFER, vertexBufferObject.get(0));
        gl.glEnableVertexAttribArray(0);
        gl.glEnableVertexAttribArray(1);
        gl.glVertexAttribPointer(0, 3, GL_FLOAT, false, 0, 0);
        gl.glVertexAttribPointer(1, 4, GL_FLOAT, false, 0, colorDataOffset);
        gl.glBindBuffer(GL_ELEMENT_ARRAY_BUFFER, indexBufferObject.get(0));

        gl.glBindVertexArray(0);

        gl.glGenVertexArrays(1, vaoObject2);
        gl.glBindVertexArray(vaoObject2.get(0));

        int posDataOffset = Float.BYTES * 3 * (numberOfVertices / 2);
        colorDataOffset += Float.BYTES * 4 * (numberOfVertices / 2);

        //Use the same buffer object previously bound to GL_ARRAY_BUFFER.
        gl.glEnableVertexAttribArray(0);
        gl.glEnableVertexAttribArray(1);
        gl.glVertexAttribPointer(0, 3, GL_FLOAT, false, 0, posDataOffset);
        gl.glVertexAttribPointer(1, 4, GL_FLOAT, false, 0, colorDataOffset);
        gl.glBindBuffer(GL_ELEMENT_ARRAY_BUFFER, indexBufferObject.get(0));

        gl.glBindVertexArray(0);
    }

    @Override
    public void init(GL3 gl) {
        initializeProgram(gl);
        initializeVertexBuffer(gl);
        initializeVertexArrayObjects(gl);

        gl.glEnable(GL_CULL_FACE);
        gl.glCullFace(GL_BACK);
        gl.glFrontFace(GL_CW);
    }


    @Override
    public void display(GL3 gl) {

        gl.glClearColor(0, 0, 0, 0);
        gl.glClear(GL3.GL_COLOR_BUFFER_BIT);

        gl.glUseProgram(theProgram);

        gl.glBindVertexArray(vaoObject1.get(0));
        gl.glUniform3f(offsetUniform, 0f, 0f, 0f);
        gl.glDrawElements(GL_TRIANGLES, indexData.length, GL_UNSIGNED_SHORT, 0);

        gl.glBindVertexArray(vaoObject2.get(0));
        gl.glUniform3f(offsetUniform, 0.0f, 0.0f, -1.0f);
        gl.glDrawElements(GL_TRIANGLES, indexData.length, GL_UNSIGNED_SHORT, 0);

        gl.glBindVertexArray(0);
        gl.glUseProgram(0);
    }

    @Override
    protected void reshape(GL3 gl, int w, int h) {
        perspectiveMatrix.put(0, frustrumScale / (w / (float) h));
        perspectiveMatrix.put(5, frustrumScale);

        gl.glUseProgram(theProgram);
        gl.glUniformMatrix4fv(perspectiveMatrixUnif, 1, false, perspectiveMatrix);
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
        gl.glDeleteBuffers(1, vertexBufferObject);
        gl.glDeleteBuffers(1, indexBufferObject);
        gl.glDeleteVertexArrays(1, vaoObject1);
        gl.glDeleteVertexArrays(1, vaoObject2);
    }
}

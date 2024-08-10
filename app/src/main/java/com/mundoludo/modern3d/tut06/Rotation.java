package com.mundoludo.modern3d.tut06;

import com.jogamp.opengl.GL3;
import com.jogamp.opengl.util.GLBuffers;

import glm_.mat4x4.Mat4;
import glm_.mat3x3.Mat3;
import glm_.vec3.Vec3;
import glm_.vec4.Vec4;

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

public class Rotation extends Framework {

    public static void main(String[] args) {
        new Rotation().setup("Tutorial 06 - Rotation");
    }

    private int theProgram;
    private int modelToCameraMatrixUnif, cameraToClipMatrixUnif;

    private Mat4 cameraToClipMatrix = new Mat4(0.0f);

    private float calcFrustumScale(float fFovDeg) {
        final float degToRad = (float) Math.PI * 2.0f / 360.0f;
        float fFovRad = fFovDeg * degToRad;
        return 1.0f / (float) Math.tan(fFovRad / 2.0f);
    }

    private float fFrustumScale = calcFrustumScale(45.0f);

    public static FloatBuffer MatBuffer = GLBuffers.newDirectFloatBuffer(16);

    private void initializeProgram(GL3 gl) {
        ArrayList<Integer> shaderList = new ArrayList<>();

        String vertShader = findResource("tut06/pos_color_local_transform.vert");
        String fragShader = findResource("tut06/color_passthrough.frag");

        shaderList.add(createShader(gl, GL_VERTEX_SHADER, vertShader));
        shaderList.add(createShader(gl, GL_FRAGMENT_SHADER, fragShader));

        theProgram = createProgram(gl, shaderList);
        shaderList.forEach(gl::glDeleteShader);

        modelToCameraMatrixUnif = gl.glGetUniformLocation(theProgram, "modelToCameraMatrix");
        cameraToClipMatrixUnif = gl.glGetUniformLocation(theProgram, "cameraToClipMatrix");

        float fzNear = 1f;
        float fzFar = 61.0f;

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

    private int numberOfVertices = 8;

    private float[] GREEN_COLOR = {0.0f, 1.0f, 0.0f, 1.0f};
    private float[] BLUE_COLOR = {0.0f, 0.0f, 1.0f, 1.0f};
    private float[] RED_COLOR = {1.0f, 0.0f, 0.0f, 1.0f};
    private float[] BROWN_COLOR = {0.5f, 0.5f, 0.0f, 1.0f};

    private float[] vertexData = {
            +1.0f, +1.0f, +1.0f,
            -1.0f, -1.0f, +1.0f,
            -1.0f, +1.0f, -1.0f,
            +1.0f, -1.0f, -1.0f,

            -1.0f, -1.0f, -1.0f,
            +1.0f, +1.0f, -1.0f,
            +1.0f, -1.0f, +1.0f,
            -1.0f, +1.0f, +1.0f,

            GREEN_COLOR[0], GREEN_COLOR[1], GREEN_COLOR[2], GREEN_COLOR[3],
            BLUE_COLOR[0], BLUE_COLOR[1], BLUE_COLOR[2], BLUE_COLOR[3],
            RED_COLOR[0], RED_COLOR[1], RED_COLOR[2], RED_COLOR[3],
            BROWN_COLOR[0], BROWN_COLOR[1], BROWN_COLOR[2], BROWN_COLOR[3],

            GREEN_COLOR[0], GREEN_COLOR[1], GREEN_COLOR[2], GREEN_COLOR[3],
            BLUE_COLOR[0], BLUE_COLOR[1], BLUE_COLOR[2], BLUE_COLOR[3],
            RED_COLOR[0], RED_COLOR[1], RED_COLOR[2], RED_COLOR[3],
            BROWN_COLOR[0], BROWN_COLOR[1], BROWN_COLOR[2], BROWN_COLOR[3],
    };

    private short[] indexData = {
            0, 1, 2,
            1, 0, 3,
            2, 3, 0,
            3, 2, 1,

            5, 4, 6,
            4, 5, 7,
            7, 6, 4,
            6, 7, 5,
    };

    private IntBuffer vertexBufferObject = GLBuffers.newDirectIntBuffer(1);
    private IntBuffer indexBufferObject = GLBuffers.newDirectIntBuffer(1);
    private IntBuffer vao = GLBuffers.newDirectIntBuffer(1);

    private long start;

    private void initializeVertexBuffer(GL3 gl) {
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
        gl.glGenVertexArrays(1, vao);
        gl.glBindVertexArray(vao.get(0));

        int colorDataOffset = Float.BYTES * 3 * numberOfVertices;

        gl.glBindBuffer(GL_ARRAY_BUFFER, vertexBufferObject.get(0));
        gl.glEnableVertexAttribArray(0);
        gl.glEnableVertexAttribArray(1);
        gl.glVertexAttribPointer(0, 3, GL_FLOAT, false, 0, 0);
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

        gl.glEnable(GL_DEPTH_TEST);
        gl.glDepthMask(true);
        gl.glDepthFunc(GL_LEQUAL);
        gl.glDepthRange(0.0f, 1.0f);

        start = System.currentTimeMillis();
    }


    @Override
    public void display(GL3 gl) {

        gl.glClearColor(0, 0, 0, 0);
        gl.glClearDepth(1.0f);
        gl.glClear(GL3.GL_COLOR_BUFFER_BIT | GL3.GL_DEPTH_BUFFER_BIT);

        gl.glUseProgram(theProgram);

        gl.glBindVertexArray(vao.get(0));

        float fElapsedTime = (System.currentTimeMillis() - start) / 1_000f;
        for (Instance instance : instanceList) {
            Mat4 transformMatrix = instance.ConstructMatrix(fElapsedTime);

            gl.glUniformMatrix4fv(modelToCameraMatrixUnif, 1, false, transformMatrix.to(MatBuffer));
            gl.glDrawElements(GL_TRIANGLES, indexData.length, GL_UNSIGNED_SHORT, 0);
        }

        gl.glBindVertexArray(0);
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
        gl.glDeleteBuffers(1, vertexBufferObject);
        gl.glDeleteBuffers(1, indexBufferObject);
        gl.glDeleteVertexArrays(1, vao);
    }

    private float calcLerpFactor(Float fElapsedTime, Float fLoopDuration) {
        float fValue = (fElapsedTime % fLoopDuration) / fLoopDuration;
        if (fValue > 0.5f) {
            fValue = 1.0f - fValue;
        }

        return fValue * 2.0f;
    }

    private float computeAngleRad(Float fElapsedTime, Float fLoopDuration) {
        float fScale = (float) Math.PI * 2 / fLoopDuration;
	float fCurrTimeThroughLoop = fElapsedTime % fLoopDuration;

	return fCurrTimeThroughLoop * fScale;
    }

    private RotationFunc NullRotation = (Float fElapsedTime) -> new Mat3(1f);

    private RotationFunc RotateX = (Float fElapsedTime) -> {
        float fAngRad = computeAngleRad(fElapsedTime, 3.0f);
        float fCos = (float) Math.cos(fAngRad);
        float fSin = (float) Math.sin(fAngRad);

        Mat3 theMat = new Mat3(1.0f);
        theMat.v11(fCos); theMat.v21(-fSin);
        theMat.v12(fSin); theMat.v22(fCos);

        return theMat;
    };

    private RotationFunc RotateY = (Float fElapsedTime) -> {
        float fAngRad = computeAngleRad(fElapsedTime, 3.0f);
        float fCos = (float) Math.cos(fAngRad);
        float fSin = (float) Math.sin(fAngRad);

        Mat3 theMat = new Mat3(1.0f);
        theMat.v00(fCos); theMat.v20(fSin);
        theMat.v02(-fSin); theMat.v22(fCos);

        return theMat;
    };

    private RotationFunc RotateZ = (Float fElapsedTime) -> {
        float fAngRad = computeAngleRad(fElapsedTime, 3.0f);
        float fCos = (float) Math.cos(fAngRad);
        float fSin = (float) Math.sin(fAngRad);

        Mat3 theMat = new Mat3(1.0f);
        theMat.v00(fCos); theMat.v10(-fSin);
        theMat.v01(fSin); theMat.v11(fCos);

        return theMat;
    };

    private RotationFunc RotateAxis = (Float fElapsedTime) -> {
        float fAngRad = computeAngleRad(fElapsedTime, 2.0f);
        float fCos = (float) Math.cos(fAngRad);
        float fInvCos = 1.0f - fCos;
        float fSin = (float) Math.sin(fAngRad);
        float fInvSin = 1.0f - fSin;

        Vec3 axis = new Vec3(1.0f, 1.0f, 1.0f).normalize();

        Mat3 theMat = new Mat3(1.0f);
        theMat.v00((axis.getX() * axis.getX()) + ((1 - axis.getX() * axis.getX()) * fCos));
        theMat.v10(axis.getX() * axis.getY() * (fInvCos) - (axis.getZ() * fSin));
        theMat.v20(axis.getX() * axis.getZ() * (fInvCos) + (axis.getY() * fSin));

        theMat.v01(axis.getX() * axis.getY() * (fInvCos) + (axis.getZ() * fSin));
        theMat.v11((axis.getY() * axis.getY()) + ((1 - axis.getY() * axis.getY()) * fCos));
        theMat.v21(axis.getY() * axis.getZ() * (fInvCos) - (axis.getX() * fSin));

        theMat.v02(axis.getX() * axis.getZ() * (fInvCos) - (axis.getY() * fSin));
        theMat.v12(axis.getY() * axis.getZ() * (fInvCos) + (axis.getX() * fSin));
        theMat.v22((axis.getZ() * axis.getZ()) + ((1 - axis.getZ() * axis.getZ()) * fCos));

        return theMat;
    };

    @FunctionalInterface
    private interface RotationFunc {
        Mat3 execute(Float fElapsedTime);
    }

    private class Instance {
        private RotationFunc calcRotation;
        private Vec3 offset;
        private Vec3 vec = new Vec3();

        Instance(RotationFunc RotationFunc, Vec3 offset) {
            this.calcRotation = RotationFunc;
            this.offset = offset;
        }

        Mat4 ConstructMatrix(float fElapsedTime) {
            Mat3 rotMat = calcRotation.execute(fElapsedTime);
            Mat4 theMat = new Mat4(rotMat);

            theMat.set(3, new Vec4(offset, 1.0f));

            return theMat;
        }
    }

    Instance[] instanceList = {
        new Instance(NullRotation, new Vec3(0.0f, 0.0f, -25.0f)),
        new Instance(RotateX, new Vec3(-5.0f, -5.0f, -25.0f)),
        new Instance(RotateY, new Vec3(-5.0f, 5.0f, -25.0f)),
        new Instance(RotateZ, new Vec3(5.0f, 5.0f, -25.0f)),
        new Instance(RotateAxis, new Vec3(5.0f, -5.0f, -25.0f))
    };
}

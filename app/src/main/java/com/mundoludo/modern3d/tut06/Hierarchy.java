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
import java.util.Stack;

import static com.jogamp.opengl.GL.GL_ARRAY_BUFFER;
import static com.jogamp.opengl.GL.GL_CULL_FACE;
import static com.jogamp.opengl.GL.GL_BACK;
import static com.jogamp.opengl.GL.GL_CW;
import static com.jogamp.opengl.GL.GL_FLOAT;
import static com.jogamp.opengl.GL.GL_TRIANGLES;
import static com.jogamp.opengl.GL.GL_STATIC_DRAW;
import static com.jogamp.opengl.GL2ES2.*;

import com.mundoludo.modern3d.framework.Framework;

public class Hierarchy extends Framework {

    public static void main(String[] args) {
        new Hierarchy().setup("Tutorial 06 - Hierarchy");
    }

    private int theProgram, positionAttrib, colorAttrib;
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

	positionAttrib = gl.glGetAttribLocation(theProgram, "position");
	colorAttrib = gl.glGetAttribLocation(theProgram, "color");

        modelToCameraMatrixUnif = gl.glGetUniformLocation(theProgram, "modelToCameraMatrix");
        cameraToClipMatrixUnif = gl.glGetUniformLocation(theProgram, "cameraToClipMatrix");

        float fzNear = 1f;
        float fzFar = 100.0f;

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

    private int numberOfVertices = 24;

    private float[] RED_COLOR = {1.0f, 0.0f, 0.0f, 1.0f};
    private float[] GREEN_COLOR = {0.0f, 1.0f, 0.0f, 1.0f};
    private float[] BLUE_COLOR = {0.0f, 0.0f, 1.0f, 1.0f};

    private float[] YELLOW_COLOR = {1.0f, 1.0f, 0.0f, 1.0f};
    private float[] CYAN_COLOR = {0.0f, 1.0f, 1.0f, 1.0f};
    private float[] MAGENTA_COLOR = {1.0f, 0.0f, 1.0f, 1.0f};

    private float[] vertexData = {
        //Front
        +1.0f, +1.0f, +1.0f,
        +1.0f, -1.0f, +1.0f,
        -1.0f, -1.0f, +1.0f,
        -1.0f, +1.0f, +1.0f,

        //Top
        +1.0f, +1.0f, +1.0f,
        -1.0f, +1.0f, +1.0f,
        -1.0f, +1.0f, -1.0f,
        +1.0f, +1.0f, -1.0f,

        //Left
        +1.0f, +1.0f, +1.0f,
        +1.0f, +1.0f, -1.0f,
        +1.0f, -1.0f, -1.0f,
        +1.0f, -1.0f, +1.0f,

        //Back
        +1.0f, +1.0f, -1.0f,
        -1.0f, +1.0f, -1.0f,
        -1.0f, -1.0f, -1.0f,
        +1.0f, -1.0f, -1.0f,

        //Bottom
        +1.0f, -1.0f, +1.0f,
        +1.0f, -1.0f, -1.0f,
        -1.0f, -1.0f, -1.0f,
        -1.0f, -1.0f, +1.0f,

        //Right
        -1.0f, +1.0f, +1.0f,
        -1.0f, -1.0f, +1.0f,
        -1.0f, -1.0f, -1.0f,
        -1.0f, +1.0f, -1.0f,

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
        RED_COLOR[0], RED_COLOR[1], RED_COLOR[2], RED_COLOR[3],

        YELLOW_COLOR[0], YELLOW_COLOR[1], YELLOW_COLOR[2], YELLOW_COLOR[3],
        YELLOW_COLOR[0], YELLOW_COLOR[1], YELLOW_COLOR[2], YELLOW_COLOR[3],
        YELLOW_COLOR[0], YELLOW_COLOR[1], YELLOW_COLOR[2], YELLOW_COLOR[3],
        YELLOW_COLOR[0], YELLOW_COLOR[1], YELLOW_COLOR[2], YELLOW_COLOR[3],

        CYAN_COLOR[0], CYAN_COLOR[1], CYAN_COLOR[2], CYAN_COLOR[3],
        CYAN_COLOR[0], CYAN_COLOR[1], CYAN_COLOR[2], CYAN_COLOR[3],
        CYAN_COLOR[0], CYAN_COLOR[1], CYAN_COLOR[2], CYAN_COLOR[3],
        CYAN_COLOR[0], CYAN_COLOR[1], CYAN_COLOR[2], CYAN_COLOR[3],

        MAGENTA_COLOR[0], MAGENTA_COLOR[1], MAGENTA_COLOR[2], MAGENTA_COLOR[3],
        MAGENTA_COLOR[0], MAGENTA_COLOR[1], MAGENTA_COLOR[2], MAGENTA_COLOR[3],
        MAGENTA_COLOR[0], MAGENTA_COLOR[1], MAGENTA_COLOR[2], MAGENTA_COLOR[3],
        MAGENTA_COLOR[0], MAGENTA_COLOR[1], MAGENTA_COLOR[2], MAGENTA_COLOR[3],
    };

    private short[] indexData = {
        0, 1, 2,
        2, 3, 0,

        4, 5, 6,
        6, 7, 4,

        8, 9, 10,
        10, 11, 8,

        12, 13, 14,
        14, 15, 12,

        16, 17, 18,
        18, 19, 16,

        20, 21, 22,
        22, 23, 20,
    };

    private IntBuffer vertexBufferObject = GLBuffers.newDirectIntBuffer(1);
    private IntBuffer indexBufferObject = GLBuffers.newDirectIntBuffer(1);
    private IntBuffer vao = GLBuffers.newDirectIntBuffer(1);

    private Armature g_armature = new Armature();

    private void initializeVAO(GL3 gl) {
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

        gl.glGenVertexArrays(1, vao);
        gl.glBindVertexArray(vao.get(0));

        int colorDataOffset = Float.BYTES * 3 * numberOfVertices;

        gl.glBindBuffer(GL_ARRAY_BUFFER, vertexBufferObject.get(0));
        gl.glEnableVertexAttribArray(positionAttrib);
        gl.glEnableVertexAttribArray(colorAttrib);
        gl.glVertexAttribPointer(positionAttrib, 3, GL_FLOAT, false, 0, 0);
        gl.glVertexAttribPointer(colorAttrib, 4, GL_FLOAT, false, 0, colorDataOffset);
        gl.glBindBuffer(GL_ELEMENT_ARRAY_BUFFER, indexBufferObject.get(0));

        gl.glBindVertexArray(0);
    }

    private float DegToRad(float fAngDeg)
    {
        final float fDegToRad = (float) Math.PI * 2.0f / 360.0f;
        return fAngDeg * fDegToRad;
    }

    private float Clamp(float fValue, float fMinValue, float fMaxValue)
    {
        if(fValue < fMinValue)
            return fMinValue;

        if(fValue > fMaxValue)
            return fMaxValue;

        return fValue;
    }

    private Mat3 RotateX(float fAngDeg)
    {
        float fAngRad = DegToRad(fAngDeg);
        float fCos = (float) Math.cos(fAngRad);
        float fSin = (float) Math.sin(fAngRad);

        Mat3 theMat = new Mat3(1.0f);
        theMat.v11(fCos); theMat.v21(-fSin);
        theMat.v12(fSin); theMat.v22(fCos);

        return theMat;
    };

    private Mat3 RotateY(float fAngDeg) {
        float fAngRad = DegToRad(fAngDeg);
        float fCos = (float) Math.cos(fAngRad);
        float fSin = (float) Math.sin(fAngRad);

        Mat3 theMat = new Mat3(1.0f);
        theMat.v00(fCos); theMat.v20(fSin);
        theMat.v02(-fSin); theMat.v22(fCos);

        return theMat;
    };

    private Mat3 RotateZ(float fAngDeg) {
        float fAngRad = DegToRad(fAngDeg);
        float fCos = (float) Math.cos(fAngRad);
        float fSin = (float) Math.sin(fAngRad);

        Mat3 theMat = new Mat3(1.0f);
        theMat.v00(fCos); theMat.v10(-fSin);
        theMat.v01(fSin); theMat.v11(fCos);

        return theMat;
    };

    private class MatrixStack {
        private Stack<Mat4> matrices = new Stack<>();
        private Mat4 currMat = new Mat4(1f);

        Mat4 Top() {
            return currMat;
        }

        void RotateX(float fAngDeg) {
            currMat.timesAssign(new Mat4(Hierarchy.this.RotateX(fAngDeg)));
        }

        void RotateY(float fAngDeg) {
            currMat.timesAssign(new Mat4(Hierarchy.this.RotateY(fAngDeg)));
        }

        void RotateZ(float fAngDeg) {
            currMat.timesAssign(new Mat4(Hierarchy.this.RotateZ(fAngDeg)));
        }

        void Scale(Vec3 offsetVec) {
            Mat4 scaleMat = new Mat4(offsetVec);
            currMat.timesAssign(scaleMat);
        }

        void Translate(Vec3 offsetVec) {
            Mat4 translateMat = new Mat4(1f);
            translateMat.set(3, new Vec4(offsetVec, 1f));
            currMat.timesAssign(translateMat);
        }

        void Push() {
            matrices.push(new Mat4(currMat));
        }

        void Pop() {
            currMat = matrices.pop();
        }

        FloatBuffer to(FloatBuffer buffer) {
            return currMat.to(buffer);
        }
    }

    class Armature {
        private Vec3 posBase = new Vec3(3.0f, -5.0f, -40.0f);
        private float angBase = -45.0f;
        private Vec3 posBaseLeft = new Vec3(2.0f, 0.0f, 0.0f);
        private Vec3 posBaseRight = new Vec3(-2.0f, 0.0f, 0.0f);
        private float scaleBaseZ = 3.0f;
        private float angUpperArm = -33.75f;
        private float sizeUpperArm = 9.0f;
        private Vec3 posLowerArm = new Vec3(0.0f, 0.0f, 8.0f);
        private float angLowerArm = 146.25f;
        private float lenLowerArm = 5.0f;
        private float widthLowerArm = 1.5f;
        private Vec3 posWrist = new Vec3(0.0f, 0.0f, 5.0f);
        private float angWristRoll = 0.0f;
        private float angWristPitch = 67.5f;
        private float lenWrist = 2.0f;
        private float widthWrist = 2.0f;
        private Vec3 posLeftFinger = new Vec3(1.0f, 0.0f, 1.0f);
        private Vec3 posRightFinger = new Vec3(-1.0f, 0.0f, 1.0f);
        private float angFingerOpen = 180.0f;
        private float lenFinger = 2.0f;
        private float widthFinger = 0.5f;
        private float angLowerFinger = 45.0f;

        void Draw(GL3 gl) {
            MatrixStack modelToCameraStack = new MatrixStack();
            gl.glUseProgram(theProgram);
            gl.glBindVertexArray(vao.get(0));

            modelToCameraStack.Translate(posBase);
            modelToCameraStack.RotateY(angBase);

            //Draw left base.
            {
                modelToCameraStack.Push();
                modelToCameraStack.Translate(posBaseLeft);
                modelToCameraStack.Scale(new Vec3(1.0f, 1.0f, scaleBaseZ));
                gl.glUniformMatrix4fv(modelToCameraMatrixUnif, 1, false, modelToCameraStack.to(MatBuffer));
                gl.glDrawElements(GL_TRIANGLES, indexData.length, GL_UNSIGNED_SHORT, 0);
                modelToCameraStack.Pop();
            }

            //Draw right base.
            {
                modelToCameraStack.Push();
                modelToCameraStack.Translate(posBaseRight);
                modelToCameraStack.Scale(new Vec3(1.0f, 1.0f, scaleBaseZ));
                gl.glUniformMatrix4fv(modelToCameraMatrixUnif, 1, false, modelToCameraStack.to(MatBuffer));
                gl.glDrawElements(GL_TRIANGLES, indexData.length, GL_UNSIGNED_SHORT, 0);
                modelToCameraStack.Pop();
            }

            //Draw main arm.
            DrawUpperArm(gl, modelToCameraStack);

            gl.glBindVertexArray(0);
            gl.glUseProgram(0);
        }

        private void DrawFingers(GL3 gl, MatrixStack modelToCameraStack)
        {
            //  Draw left finger
            modelToCameraStack.Push();
            modelToCameraStack.Translate(posLeftFinger);
            modelToCameraStack.RotateY(angFingerOpen);

            modelToCameraStack.Push();
            modelToCameraStack.Translate(new Vec3(0.0f, 0.0f, lenFinger / 2.0f));
            modelToCameraStack.Scale(new Vec3(widthFinger / 2.0f, widthFinger / 2.0f, lenFinger / 2.0f));
            gl.glUniformMatrix4fv(modelToCameraMatrixUnif, 1, false, modelToCameraStack.to(MatBuffer));
            gl.glDrawElements(GL_TRIANGLES, indexData.length, GL_UNSIGNED_SHORT, 0);
            modelToCameraStack.Pop();

            {
                //  Draw left lower finger
                modelToCameraStack.Push();
                modelToCameraStack.Translate(new Vec3(0.0f, 0.0f, lenFinger));
                modelToCameraStack.RotateY(-angLowerFinger);

                modelToCameraStack.Push();
                modelToCameraStack.Translate(new Vec3(0.0f, 0.0f, lenFinger / 2.0f));
                modelToCameraStack.Scale(new Vec3(widthFinger / 2.0f, widthFinger / 2.0f, lenFinger / 2.0f));
                gl.glUniformMatrix4fv(modelToCameraMatrixUnif, 1, false, modelToCameraStack.to(MatBuffer));
                gl.glDrawElements(GL_TRIANGLES, indexData.length, GL_UNSIGNED_SHORT, 0);
                modelToCameraStack.Pop();

                modelToCameraStack.Pop();
            }

            modelToCameraStack.Pop();

            //  Draw right finger
            modelToCameraStack.Push();
            modelToCameraStack.Translate(posRightFinger);
            modelToCameraStack.RotateY(-angFingerOpen);

            modelToCameraStack.Push();
            modelToCameraStack.Translate(new Vec3(0.0f, 0.0f, lenFinger / 2.0f));
            modelToCameraStack.Scale(new Vec3(widthFinger / 2.0f, widthFinger / 2.0f, lenFinger / 2.0f));
            gl.glUniformMatrix4fv(modelToCameraMatrixUnif, 1, false, modelToCameraStack.to(MatBuffer));
            gl.glDrawElements(GL_TRIANGLES, indexData.length, GL_UNSIGNED_SHORT, 0);
            modelToCameraStack.Pop();

            {
                //  Draw left lower finger
                modelToCameraStack.Push();
                modelToCameraStack.Translate(new Vec3(0.0f, 0.0f, lenFinger));
                modelToCameraStack.RotateY(angLowerFinger);

                modelToCameraStack.Push();
                modelToCameraStack.Translate(new Vec3(0.0f, 0.0f, lenFinger / 2.0f));
                modelToCameraStack.Scale(new Vec3(widthFinger / 2.0f, widthFinger / 2.0f, lenFinger / 2.0f));
                gl.glUniformMatrix4fv(modelToCameraMatrixUnif, 1, false, modelToCameraStack.to(MatBuffer));
                gl.glDrawElements(GL_TRIANGLES, indexData.length, GL_UNSIGNED_SHORT, 0);
                modelToCameraStack.Pop();

                modelToCameraStack.Pop();
            }

            modelToCameraStack.Pop();
        }

        private void DrawWrist(GL3 gl, MatrixStack modelToCameraStack)
        {
            modelToCameraStack.Push();
            modelToCameraStack.Translate(posWrist);
            modelToCameraStack.RotateZ(angWristRoll);
            modelToCameraStack.RotateX(angWristPitch);

            modelToCameraStack.Push();
            modelToCameraStack.Scale(new Vec3(widthWrist / 2.0f, widthWrist / 2.0f, lenWrist / 2.0f));
            gl.glUniformMatrix4fv(modelToCameraMatrixUnif, 1, false, modelToCameraStack.to(MatBuffer));
            gl.glDrawElements(GL_TRIANGLES, indexData.length, GL_UNSIGNED_SHORT, 0);
            modelToCameraStack.Pop();

            DrawFingers(gl, modelToCameraStack);

            modelToCameraStack.Pop();
        }

        private void DrawUpperArm(GL3 gl, MatrixStack modelToCameraStack)
        {
            modelToCameraStack.Push();
            modelToCameraStack.RotateX(angUpperArm);
            {
                modelToCameraStack.Push();
                modelToCameraStack.Translate(new Vec3(0.0f, 0.0f, (sizeUpperArm / 2.0f) - 1.0f));
                modelToCameraStack.Scale(new Vec3(1.0f, 1.0f, sizeUpperArm / 2.0f));
                gl.glUniformMatrix4fv(modelToCameraMatrixUnif, 1, false, modelToCameraStack.to(MatBuffer));
                gl.glDrawElements(GL_TRIANGLES, indexData.length, GL_UNSIGNED_SHORT, 0);
                modelToCameraStack.Pop();
            }

            DrawLowerArm(gl, modelToCameraStack);

            modelToCameraStack.Pop();
        }

        private void DrawLowerArm(GL3 gl, MatrixStack modelToCameraStack) {

            modelToCameraStack.Push();
            modelToCameraStack.Translate(posLowerArm);
            modelToCameraStack.RotateX(angLowerArm);

            modelToCameraStack.Push();
            modelToCameraStack.Translate(new Vec3(0.0f, 0.0f, lenLowerArm / 2.0f));
            modelToCameraStack.Scale(new Vec3(widthLowerArm / 2.0f, widthLowerArm / 2.0f, lenLowerArm / 2.0f));
            gl.glUniformMatrix4fv(modelToCameraMatrixUnif, 1, false, modelToCameraStack.to(MatBuffer));
            gl.glDrawElements(GL_TRIANGLES, indexData.length, GL_UNSIGNED_SHORT, 0);
            modelToCameraStack.Pop();

            DrawWrist(gl, modelToCameraStack);

            modelToCameraStack.Pop();
        }
    }

    @Override
    public void init(GL3 gl) {
        initializeProgram(gl);
        initializeVAO(gl);

        gl.glEnable(GL_CULL_FACE);
        gl.glCullFace(GL_BACK);
        gl.glFrontFace(GL_CW);

        gl.glEnable(GL_DEPTH_TEST);
        gl.glDepthMask(true);
        gl.glDepthFunc(GL_LEQUAL);
        gl.glDepthRange(0.0f, 1.0f);
    }


    @Override
    public void display(GL3 gl) {

        gl.glClearColor(0, 0, 0, 0);
        gl.glClearDepth(1.0f);
        gl.glClear(GL3.GL_COLOR_BUFFER_BIT | GL3.GL_DEPTH_BUFFER_BIT);

        g_armature.Draw(gl);
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
}

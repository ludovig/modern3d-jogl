package com.mundoludo.modern3d.framework;

import glm_.Java;
import glm_.mat4x4.Mat4;
import glm_.mat3x3.Mat3;
import glm_.vec3.Vec3;
import glm_.vec4.Vec4;

import java.nio.FloatBuffer;
import java.util.Stack;

public class MatrixStack {

    private Stack<Mat4> matrices = new Stack<>();
    private Mat4 currMat = new Mat4(1f);

    private Mat3 rotationX(float angDeg)
    {
        float angRad = Framework.degToRad(angDeg);
        float fCos = (float) Math.cos(angRad);
        float fSin = (float) Math.sin(angRad);

        Mat3 theMat = new Mat3(1.0f);
        theMat.v11(fCos); theMat.v21(-fSin);
        theMat.v12(fSin); theMat.v22(fCos);

        return theMat;
    }

    private Mat3 rotationY(float angDeg) {
        float angRad = Framework.degToRad(angDeg);
        float fCos = (float) Math.cos(angRad);
        float fSin = (float) Math.sin(angRad);

        Mat3 theMat = new Mat3(1.0f);
        theMat.v00(fCos); theMat.v20(fSin);
        theMat.v02(-fSin); theMat.v22(fCos);

        return theMat;
    }

    private Mat3 rotationZ(float angDeg) {
        float angRad = Framework.degToRad(angDeg);
        float fCos = (float) Math.cos(angRad);
        float fSin = (float) Math.sin(angRad);

        Mat3 theMat = new Mat3(1.0f);
        theMat.v00(fCos); theMat.v10(-fSin);
        theMat.v01(fSin); theMat.v11(fCos);

        return theMat;
    }

    public Mat4 top() {
        return currMat;
    }

    public Mat4 rotateX(float angDeg) {
        currMat.timesAssign(new Mat4(rotationX(angDeg)));
        return currMat;
    }

    public Mat4 rotateY(float angDeg) {
        currMat.timesAssign(new Mat4(rotationY(angDeg)));
        return currMat;
    }

    public Mat4 rotateZ(float angDeg) {
        currMat.timesAssign(new Mat4(rotationZ(angDeg)));
        return currMat;
    }

    public Mat4 scale(Vec3 offsetVec) {
        Mat4 scaleMat = new Mat4(offsetVec);
        currMat.timesAssign(scaleMat);
        return currMat;
    }

    public Mat4 translate(Vec3 offsetVec) {
        Mat4 translateMat = new Mat4(1f);
        translateMat.set(3, new Vec4(offsetVec, 1f));
        currMat.timesAssign(translateMat);
        return currMat;
    }

    public Mat4 perspective(float degFov, float aspectRatio, float zNear, float zFar) {
        float radFov = Framework.degToRad(degFov);
        Mat4 perspectiveMat = Java.glm.perspective(radFov, aspectRatio, zNear, zFar);
        currMat.timesAssign(perspectiveMat);
        return currMat;
    }

    public Mat4 applyMatrix(Mat4 theMatrix) {
        currMat.timesAssign(theMatrix);

        return currMat;
    }

    public void push() {
        matrices.push(new Mat4(currMat));
    }

    public void pop() {
        currMat = matrices.pop();
    }

    public FloatBuffer to(FloatBuffer buffer) {
        return currMat.to(buffer);
    }
}

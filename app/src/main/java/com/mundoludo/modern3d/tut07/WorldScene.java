package com.mundoludo.modern3d.tut07;

import com.jogamp.newt.event.*;
import com.jogamp.opengl.GL3;
import com.jogamp.opengl.util.GLBuffers;

import glm_.Java;
import glm_.mat4x4.Mat4;
import glm_.vec3.Vec3;

import java.nio.FloatBuffer;
import java.util.ArrayList;

import javax.xml.parsers.ParserConfigurationException;
import java.io.IOException;
import java.net.URISyntaxException;
import org.xml.sax.SAXException;

import static com.jogamp.opengl.GL.GL_BACK;
import static com.jogamp.opengl.GL.GL_CULL_FACE;
import static com.jogamp.opengl.GL.GL_CW;
import static com.jogamp.opengl.GL3.GL_DEPTH_CLAMP;
import static com.jogamp.opengl.GL2ES2.*;

import com.mundoludo.modern3d.framework.Framework;
import com.mundoludo.modern3d.framework.component.Mesh;
import com.mundoludo.modern3d.framework.MatrixStack;

public class WorldScene extends Framework {

    public static void main(String[] args) {
        new WorldScene().setup("Tutorial 07 - World Scene");
    }

    class ProgramData {
        int theProgram;
        int modelToWorldMatrixUnif;
        int worldToCameraMatrixUnif;
        int cameraToClipMatrixUnif;
        int baseColorUnif;

        public ProgramData(GL3 gl, String strVertexShader, String strFragmentShader) {
            ArrayList<Integer> shaderList = new ArrayList<>();

            String vertShader = findResource(strVertexShader);
            String fragShader = findResource(strFragmentShader);

            shaderList.add(createShader(gl, GL_VERTEX_SHADER, vertShader));
            shaderList.add(createShader(gl, GL_FRAGMENT_SHADER, fragShader));
            theProgram = createProgram(gl, shaderList);
            shaderList.forEach(gl::glDeleteShader);

            modelToWorldMatrixUnif = gl.glGetUniformLocation(theProgram, "modelToWorldMatrix");
            worldToCameraMatrixUnif = gl.glGetUniformLocation(theProgram, "worldToCameraMatrix");
            cameraToClipMatrixUnif = gl.glGetUniformLocation(theProgram, "cameraToClipMatrix");
            baseColorUnif = gl.glGetUniformLocation(theProgram, "baseColor");
        }
    }

    private float zNear = 1f;
    private float zFar = 1000.0f;

    private ProgramData UniformColor, ObjectColor, UniformColorTint;

    public static FloatBuffer MatBuffer = GLBuffers.newDirectFloatBuffer(16);

    private void initializeProgram(GL3 gl) {
	UniformColor = new ProgramData(gl,
            "tut07/pos_only_world_transform.vert",
            "tut07/color_uniform.frag"
        );
	ObjectColor = new ProgramData(gl,
            "tut07/pos_color_world_transform.vert",
            "tut07/color_passthrough.frag"
        );
	UniformColorTint = new ProgramData(gl,
            "tut07/pos_color_world_transform.vert",
            "tut07/color_uniform.frag"
        );
    }

    Mat4 CalcLookAtMatrix(Vec3 cameraPt, Vec3 lookPt, Vec3 upPt)
    {
        Vec3 lookDir = lookPt.minus(cameraPt).normalize();
        Vec3 upDir = upPt.normalize();

        Vec3 rightDir = lookDir.cross(upDir).normalize();
        Vec3 perpUpDir = rightDir.cross(lookDir);

        Mat4 rotMat = new Mat4(1.0f);
        rotMat.set(0, rightDir, 0.0f);
        rotMat.set(1, perpUpDir, 0.0f);
        rotMat.set(2, lookDir.negate(), 0.0f);

        rotMat = rotMat.transpose();

        Mat4 transMat = new Mat4(1.0f);
        transMat.set(3, cameraPt.negate(), 1.0f);

        rotMat.timesAssign(transMat);

        return rotMat;
    }

    Mesh g_pConeMesh;
    Mesh g_pCylinderMesh;
    Mesh g_pCubeTintMesh;
    Mesh g_pCubeColorMesh;
    Mesh g_pPlaneMesh;

    @Override
    public void init(GL3 gl) {
        initializeProgram(gl);

        try {
            g_pConeMesh = new Mesh(gl, getClass(), "tut07/UnitConeTint.xml");
            g_pCylinderMesh = new Mesh(gl, getClass(), "tut07/UnitCylinderTint.xml");
            g_pCubeTintMesh = new Mesh(gl, getClass(), "tut07/UnitCubeTint.xml");
            g_pCubeColorMesh = new Mesh(gl, getClass(), "tut07/UnitCubeColor.xml");
            g_pPlaneMesh = new Mesh(gl, getClass(), "tut07/UnitPlane.xml");
        } catch (ParserConfigurationException | SAXException | IOException | URISyntaxException ex) {
            System.err.println("Mesh loading failure in " + WorldScene.class.getName());
            System.err.println(ex);
        }

        gl.glEnable(GL_CULL_FACE);
        gl.glCullFace(GL_BACK);
        gl.glFrontFace(GL_CW);

        gl.glEnable(GL_DEPTH_TEST);
        gl.glDepthMask(true);
        gl.glDepthFunc(GL_LEQUAL);
        gl.glDepthRangef(0.0f, 1.0f);
        gl.glEnable(GL_DEPTH_CLAMP);
    }

    static float g_fYAngle = 0.0f;
    static float g_fXAngle = 0.0f;

    //Trees are 3x3 in X/Z, and fTrunkHeight+fConeHeight in the Y.
    void DrawTree(GL3 gl, MatrixStack modelMatrix, float fTrunkHeight, float fConeHeight) {
        //Draw trunk.
        {
            modelMatrix.push();

            modelMatrix.scale(new Vec3(1.0f, fTrunkHeight, 1.0f));
            modelMatrix.translate(new Vec3(0.0f, 0.5f, 0.0f));

            gl.glUseProgram(UniformColorTint.theProgram);
            gl.glUniformMatrix4fv(UniformColorTint.modelToWorldMatrixUnif, 1, false, modelMatrix.to(MatBuffer));
            gl.glUniform4f(UniformColorTint.baseColorUnif, 0.694f, 0.4f, 0.106f, 1.0f);
            g_pCylinderMesh.render(gl);
            gl.glUseProgram(0);


            modelMatrix.pop();
        }

        //Draw the treetop
        {
            modelMatrix.push();

            modelMatrix.translate(new Vec3(0.0f, fTrunkHeight, 0.0f));
            modelMatrix.scale(new Vec3(3.0f, fConeHeight, 3.0f));

            gl.glUseProgram(UniformColorTint.theProgram);
            gl.glUniformMatrix4fv(UniformColorTint.modelToWorldMatrixUnif, 1, false, modelMatrix.to(MatBuffer));
            gl.glUniform4f(UniformColorTint.baseColorUnif, 0.0f, 1.0f, 0.0f, 1.0f);
            g_pConeMesh.render(gl);
            gl.glUseProgram(0);

            modelMatrix.pop();
        }
    }

    void DrawForest(GL3 gl, MatrixStack modelMatrix)
    {
        for(TreeData currTree : g_forest)
        {
            modelMatrix.push();
            modelMatrix.translate(new Vec3(currTree.fXPos, 0.0f, currTree.fZPos));
            DrawTree(gl, modelMatrix, currTree.fTrunkHeight, currTree.fConeHeight);
            modelMatrix.pop();
        }
    }

    private float g_fColumnBaseHeight = 0.25f;

    //Columns are 1x1 in the X/Z, and fHieght units in the Y.
    void DrawColumn(GL3 gl, MatrixStack modelMatrix, float fHeight)
    {
        //Draw the bottom of the column.
        {
            modelMatrix.push();

            modelMatrix.scale(new Vec3(1.0f, g_fColumnBaseHeight, 1.0f));
            modelMatrix.translate(new Vec3(0.0f, 0.5f, 0.0f));

            gl.glUseProgram(UniformColorTint.theProgram);
            gl.glUniformMatrix4fv(UniformColorTint.modelToWorldMatrixUnif, 1, false, modelMatrix.to(MatBuffer));
            gl.glUniform4f(UniformColorTint.baseColorUnif, 1.0f, 1.0f, 1.0f, 1.0f);
            g_pCubeTintMesh.render(gl);
            gl.glUseProgram(0);

            modelMatrix.pop();
        }

        //Draw the top of the column.
        {
            modelMatrix.push();

            modelMatrix.translate(new Vec3(0.0f, fHeight - g_fColumnBaseHeight, 0.0f));
            modelMatrix.scale(new Vec3(1.0f, g_fColumnBaseHeight, 1.0f));
            modelMatrix.translate(new Vec3(0.0f, 0.5f, 0.0f));

            gl.glUseProgram(UniformColorTint.theProgram);
            gl.glUniformMatrix4fv(UniformColorTint.modelToWorldMatrixUnif, 1, false, modelMatrix.to(MatBuffer));
            gl.glUniform4f(UniformColorTint.baseColorUnif, 0.9f, 0.9f, 0.9f, 0.9f);
            g_pCubeTintMesh.render(gl);
            gl.glUseProgram(0);

            modelMatrix.pop();
        }

        //Draw the main column.
        {
            modelMatrix.push();

            modelMatrix.translate(new Vec3(0.0f, g_fColumnBaseHeight, 0.0f));
            modelMatrix.scale(new Vec3(0.8f, fHeight - (g_fColumnBaseHeight * 2.0f), 0.8f));
            modelMatrix.translate(new Vec3(0.0f, 0.5f, 0.0f));

            gl.glUseProgram(UniformColorTint.theProgram);
            gl.glUniformMatrix4fv(UniformColorTint.modelToWorldMatrixUnif, 1, false, modelMatrix.to(MatBuffer));
            gl.glUniform4f(UniformColorTint.baseColorUnif, 0.9f, 0.9f, 0.9f, 0.9f);
            g_pCylinderMesh.render(gl);
            gl.glUseProgram(0);

            modelMatrix.pop();
        }
    }

    void DrawParthenon(GL3 gl, MatrixStack modelMatrix)
    {
        final float g_fParthenonWidth = 14.0f;
        final float g_fParthenonLength = 20.0f;
        final float g_fParthenonColumnHeight = 5.0f;
        final float g_fParthenonBaseHeight = 1.0f;
        final float g_fParthenonTopHeight = 2.0f;

        //Draw base.
        {
            modelMatrix.push();

            modelMatrix.scale(new Vec3(g_fParthenonWidth, g_fParthenonBaseHeight, g_fParthenonLength));
            modelMatrix.translate(new Vec3(0.0f, 0.5f, 0.0f));

            gl.glUseProgram(UniformColorTint.theProgram);
            gl.glUniformMatrix4fv(UniformColorTint.modelToWorldMatrixUnif, 1, false, modelMatrix.to(MatBuffer));
            gl.glUniform4f(UniformColorTint.baseColorUnif, 0.9f, 0.9f, 0.9f, 0.9f);
            g_pCubeTintMesh.render(gl);
            gl.glUseProgram(0);

            modelMatrix.pop();
        }

        //Draw top.
        {
            modelMatrix.push();

            modelMatrix.translate(new Vec3(0.0f, g_fParthenonColumnHeight + g_fParthenonBaseHeight, 0.0f));
            modelMatrix.scale(new Vec3(g_fParthenonWidth, g_fParthenonTopHeight, g_fParthenonLength));
            modelMatrix.translate(new Vec3(0.0f, 0.5f, 0.0f));

            gl.glUseProgram(UniformColorTint.theProgram);
            gl.glUniformMatrix4fv(UniformColorTint.modelToWorldMatrixUnif, 1, false, modelMatrix.to(MatBuffer));
            gl.glUniform4f(UniformColorTint.baseColorUnif, 0.9f, 0.9f, 0.9f, 0.9f);
            g_pCubeTintMesh.render(gl);
            gl.glUseProgram(0);

            modelMatrix.pop();
        }

        //Draw columns.
        final float fFrontZVal = (g_fParthenonLength / 2.0f) - 1.0f;
        final float fRightXVal = (g_fParthenonWidth / 2.0f) - 1.0f;

        for(int iColumnNum = 0; iColumnNum < (int) (g_fParthenonWidth / 2.0f); iColumnNum++)
        {
            {
                modelMatrix.push();
                modelMatrix.translate(new Vec3(
                    (2.0f * iColumnNum) - (g_fParthenonWidth / 2.0f) + 1.0f,
                    g_fParthenonBaseHeight,
                    fFrontZVal
                ));

                DrawColumn(gl, modelMatrix, g_fParthenonColumnHeight);

                modelMatrix.pop();
            }
            {
                modelMatrix.push();
                modelMatrix.translate(new Vec3(
                    (2.0f * iColumnNum) - (g_fParthenonWidth / 2.0f) + 1.0f,
                    g_fParthenonBaseHeight,
                    -fFrontZVal
                ));

                DrawColumn(gl, modelMatrix, g_fParthenonColumnHeight);

                modelMatrix.pop();
            }
        }

        //Don't draw the first or last columns, since they've been drawn already.
        for(int iColumnNum = 1; iColumnNum < (int) ((g_fParthenonLength - 2.0f) / 2.0f); iColumnNum++)
        {
            {
                modelMatrix.push();
                modelMatrix.translate(new Vec3(
                    fRightXVal,
                    g_fParthenonBaseHeight,
                    (2.0f * iColumnNum) - (g_fParthenonLength / 2.0f) + 1.0f
                ));

                DrawColumn(gl, modelMatrix, g_fParthenonColumnHeight);

                modelMatrix.pop();
            }
            {
                modelMatrix.push();
                modelMatrix.translate(new Vec3(
                    -fRightXVal,
                    g_fParthenonBaseHeight,
                    (2.0f * iColumnNum) - (g_fParthenonLength / 2.0f) + 1.0f
                ));

                DrawColumn(gl, modelMatrix, g_fParthenonColumnHeight);

                modelMatrix.pop();
            }
        }

        //Draw interior.
        {
            modelMatrix.push();

            modelMatrix.translate(new Vec3(0.0f, 1.0f, 0.0f));
            modelMatrix.scale(new Vec3(
                g_fParthenonWidth - 6.0f,
                g_fParthenonColumnHeight,
                g_fParthenonLength - 6.0f
            ));
            modelMatrix.translate(new Vec3(0.0f, 0.5f, 0.0f));

            gl.glUseProgram(ObjectColor.theProgram);
            gl.glUniformMatrix4fv(ObjectColor.modelToWorldMatrixUnif, 1, false, modelMatrix.to(MatBuffer));
            g_pCubeColorMesh.render(gl);
            gl.glUseProgram(0);

            modelMatrix.pop();
        }

        //Draw headpiece.
        {
            modelMatrix.push();

            modelMatrix.translate(new Vec3(
                0.0f,
                g_fParthenonColumnHeight + g_fParthenonBaseHeight + (g_fParthenonTopHeight / 2.0f),
                g_fParthenonLength / 2.0f
            ));
            modelMatrix.rotateX(-135.0f);
            modelMatrix.rotateY(45.0f);

            gl.glUseProgram(ObjectColor.theProgram);
            gl.glUniformMatrix4fv(ObjectColor.modelToWorldMatrixUnif, 1, false, modelMatrix.to(MatBuffer));
            g_pCubeColorMesh.render(gl);
            gl.glUseProgram(0);

            modelMatrix.pop();
        }
    }

    private boolean g_bDrawLookatPoint = false;
    Vec3 g_camTarget = new Vec3(0.0f, 0.4f, 0.0f);
    //In spherical coordinates.
    Vec3 g_sphereCamRelPos = new Vec3(67.5f, -46.0f, 150.0f);

    private Vec3 ResolveCamPosition()
    {
        float phi = Framework.degToRad(g_sphereCamRelPos.getX());
        float theta = Framework.degToRad(g_sphereCamRelPos.getY() + 90.0f);

        float fSinTheta = (float) Math.sin(theta);
        float fCosTheta = (float) Math.cos(theta);
        float fCosPhi = (float) Math.cos(phi);
        float fSinPhi = (float) Math.sin(phi);

        Vec3 dirToCamera = new Vec3(fSinTheta * fCosPhi, fCosTheta, fSinTheta * fSinPhi);

        dirToCamera.timesAssign(g_sphereCamRelPos.getZ());
        dirToCamera.plusAssign(g_camTarget);

        return dirToCamera;
    }

    @Override
    public void display(GL3 gl) {

        gl.glClearColor(0, 0, 0, 0);
        gl.glClearDepth(1.0f);
        gl.glClear(GL3.GL_COLOR_BUFFER_BIT | GL3.GL_DEPTH_BUFFER_BIT);

        Vec3 camPos = ResolveCamPosition();

        CalcLookAtMatrix(camPos, g_camTarget, new Vec3(0.0f, 1.0f, 0.0f)).to(MatBuffer);

        gl.glUseProgram(UniformColor.theProgram);
        gl.glUniformMatrix4fv(UniformColor.worldToCameraMatrixUnif, 1, false, MatBuffer);
        gl.glUseProgram(ObjectColor.theProgram);
        gl.glUniformMatrix4fv(ObjectColor.worldToCameraMatrixUnif, 1, false, MatBuffer);
        gl.glUseProgram(UniformColorTint.theProgram);
        gl.glUniformMatrix4fv(UniformColorTint.worldToCameraMatrixUnif, 1, false, MatBuffer);
        gl.glUseProgram(0);

        MatrixStack modelMatrix = new MatrixStack();

        //Render the ground plane.
        {
            modelMatrix.push();
            modelMatrix.scale(new Vec3(100.0f, 1.0f, 100.0f));
            modelMatrix.to(MatBuffer);

            gl.glUseProgram(UniformColor.theProgram);
            gl.glUniformMatrix4fv(UniformColor.modelToWorldMatrixUnif, 1, false, MatBuffer);
            gl.glUniform4f(UniformColor.baseColorUnif, 0.302f, 0.416f, 0.0589f, 1.0f);
            g_pPlaneMesh.render(gl);
            gl.glUseProgram(0);

            modelMatrix.pop();
        }

        DrawForest(gl, modelMatrix);

        //Draw the building.
        {
            modelMatrix.push();
            modelMatrix.translate(new Vec3(20.0f, 0.0f, -10.0f));

            DrawParthenon(gl, modelMatrix);
        }

        if (g_bDrawLookatPoint) {
            gl.glDisable(GL_DEPTH_TEST);
            Mat4 identity = new Mat4(1.0f);

            Vec3 cameraAimVec = g_camTarget.minus(camPos);

            modelMatrix.push();
            modelMatrix.translate(new Vec3(0.0f, 0.0f, - Java.glm.length(cameraAimVec)));
            modelMatrix.scale(new Vec3(1.0f, 1.0f, 1.0f));

            gl.glUseProgram(ObjectColor.theProgram);
            gl.glUniformMatrix4fv(ObjectColor.modelToWorldMatrixUnif, 1, false, modelMatrix.to(MatBuffer));
            gl.glUniformMatrix4fv(ObjectColor.worldToCameraMatrixUnif, 1, false, identity.to(MatBuffer));
            g_pCubeColorMesh.render(gl);
            gl.glUseProgram(0);
            modelMatrix.pop();
            gl.glEnable(GL_DEPTH_TEST);
        }
    }

    @Override
    protected void reshape(GL3 gl, int w, int h) {
        MatrixStack persMatrix = new MatrixStack();
        persMatrix.perspective(45.0f, (w / (float) h), zNear, zFar);

        gl.glUseProgram(UniformColor.theProgram);
        gl.glUniformMatrix4fv(UniformColor.cameraToClipMatrixUnif, 1, false, persMatrix.to(MatBuffer));

        gl.glUseProgram(ObjectColor.theProgram);
        gl.glUniformMatrix4fv(ObjectColor.cameraToClipMatrixUnif, 1, false, persMatrix.to(MatBuffer));

        gl.glUseProgram(UniformColorTint.theProgram);
        gl.glUniformMatrix4fv(UniformColorTint.cameraToClipMatrixUnif, 1, false, persMatrix.to(MatBuffer));

        gl.glViewport(0, 0, w, h);
    }

    /**
     * Called at the end, here you want to clean all the resources.
     *
     * @param gl
     */
    @Override
    protected void end(GL3 gl) {

        gl.glDeleteProgram(UniformColor.theProgram);
        gl.glDeleteProgram(ObjectColor.theProgram);
        gl.glDeleteProgram(UniformColorTint.theProgram);

        g_pConeMesh.dispose(gl);
        g_pCylinderMesh.dispose(gl);
        g_pCubeTintMesh.dispose(gl);
        g_pCubeColorMesh.dispose(gl);
        g_pPlaneMesh.dispose(gl);
    }

    @Override
    public void keyPressed(KeyEvent keyEvent) {
        super.keyPressed(keyEvent);

        switch (keyEvent.getKeyCode()) {
            case KeyEvent.VK_W:
                g_camTarget.setZ(g_camTarget.getZ() - (keyEvent.isShiftDown() ?  0.4f :  4.0f));
                break;
            case KeyEvent.VK_S:
                g_camTarget.setZ(g_camTarget.getZ() + (keyEvent.isShiftDown() ? 0.4f : 4.0f));
                break;

            case KeyEvent.VK_D:
                g_camTarget.setX(g_camTarget.getX() + (keyEvent.isShiftDown() ? 0.4f : 4.0f));
                break;
            case KeyEvent.VK_A:
                g_camTarget.setX(g_camTarget.getX() - (keyEvent.isShiftDown() ? 0.4f : 4.0f));
                break;

            case KeyEvent.VK_E:
                g_camTarget.setY(g_camTarget.getY() - (keyEvent.isShiftDown() ? 0.4f : 4.0f));
                break;
            case KeyEvent.VK_Q:
                g_camTarget.setY(g_camTarget.getY() + (keyEvent.isShiftDown() ? 0.4f : 4.0f));
                break;

            case KeyEvent.VK_I:
                g_sphereCamRelPos.setY(g_sphereCamRelPos.getY() - (keyEvent.isShiftDown() ? 1.125f : 11.25f));
                break;
            case KeyEvent.VK_K:
                g_sphereCamRelPos.setY(g_sphereCamRelPos.getY() + (keyEvent.isShiftDown() ? 1.125f : 11.25f));
                break;

            case KeyEvent.VK_J:
                g_sphereCamRelPos.setX(g_sphereCamRelPos.getX() - (keyEvent.isShiftDown() ? 1.125f : 11.25f));
                break;
            case KeyEvent.VK_L:
                g_sphereCamRelPos.setX(g_sphereCamRelPos.getX() + (keyEvent.isShiftDown() ? 1.125f : 11.25f));
                break;

            case KeyEvent.VK_O:
                g_sphereCamRelPos.setZ(g_sphereCamRelPos.getZ() - (keyEvent.isShiftDown() ? 1.125f : 11.25f));
                break;
            case KeyEvent.VK_U:
                g_sphereCamRelPos.setZ(g_sphereCamRelPos.getZ() + (keyEvent.isShiftDown() ? 1.125f : 11.25f));
                break;
            case KeyEvent.VK_SPACE:
                g_bDrawLookatPoint = ! g_bDrawLookatPoint;
		System.out.printf("Target: %f, %f, %f\n", g_camTarget.getX(), g_camTarget.getY(), g_camTarget.getZ());
		System.out.printf("Position: %f, %f, %f\n", g_sphereCamRelPos.getX(), g_sphereCamRelPos.getY(), g_sphereCamRelPos.getZ());
                break;
        }

        g_sphereCamRelPos.setY(Framework.clamp(g_sphereCamRelPos.getY(), -78.75f, -1.0f));
        g_camTarget.setY(Framework.clamp(g_camTarget.getY(), 0.0f, g_camTarget.getY()));
        g_sphereCamRelPos.setZ(Framework.clamp(g_sphereCamRelPos.getZ(), 5.0f, g_sphereCamRelPos.getZ()));
    }

    class TreeData {
	float fXPos;
	float fZPos;
	float fTrunkHeight;
	float fConeHeight;

        TreeData(float fXPos, float fZPos, float fTrunkHeight, float fConeHeight) {
            this.fXPos = fXPos;
            this.fZPos = fZPos;
            this.fTrunkHeight = fTrunkHeight;
            this.fConeHeight = fConeHeight;
        }
    }

    private TreeData[] g_forest = {
	new TreeData(-45.0f, -40.0f, 2.0f, 3.0f),
	new TreeData(-42.0f, -35.0f, 2.0f, 3.0f),
	new TreeData(-39.0f, -29.0f, 2.0f, 4.0f),
	new TreeData(-44.0f, -26.0f, 3.0f, 3.0f),
	new TreeData(-40.0f, -22.0f, 2.0f, 4.0f),
	new TreeData(-36.0f, -15.0f, 3.0f, 3.0f),
	new TreeData(-41.0f, -11.0f, 2.0f, 3.0f),
	new TreeData(-37.0f, -6.0f, 3.0f, 3.0f),
	new TreeData(-45.0f, 0.0f, 2.0f, 3.0f),
	new TreeData(-39.0f, 4.0f, 3.0f, 4.0f),
	new TreeData(-36.0f, 8.0f, 2.0f, 3.0f),
	new TreeData(-44.0f, 13.0f, 3.0f, 3.0f),
	new TreeData(-42.0f, 17.0f, 2.0f, 3.0f),
	new TreeData(-38.0f, 23.0f, 3.0f, 4.0f),
	new TreeData(-41.0f, 27.0f, 2.0f, 3.0f),
	new TreeData(-39.0f, 32.0f, 3.0f, 3.0f),
	new TreeData(-44.0f, 37.0f, 3.0f, 4.0f),
	new TreeData(-36.0f, 42.0f, 2.0f, 3.0f),

	new TreeData(-32.0f, -45.0f, 2.0f, 3.0f),
	new TreeData(-30.0f, -42.0f, 2.0f, 4.0f),
	new TreeData(-34.0f, -38.0f, 3.0f, 5.0f),
	new TreeData(-33.0f, -35.0f, 3.0f, 4.0f),
	new TreeData(-29.0f, -28.0f, 2.0f, 3.0f),
	new TreeData(-26.0f, -25.0f, 3.0f, 5.0f),
	new TreeData(-35.0f, -21.0f, 3.0f, 4.0f),
	new TreeData(-31.0f, -17.0f, 3.0f, 3.0f),
	new TreeData(-28.0f, -12.0f, 2.0f, 4.0f),
	new TreeData(-29.0f, -7.0f, 3.0f, 3.0f),
	new TreeData(-26.0f, -1.0f, 2.0f, 4.0f),
	new TreeData(-32.0f, 6.0f, 2.0f, 3.0f),
	new TreeData(-30.0f, 10.0f, 3.0f, 5.0f),
	new TreeData(-33.0f, 14.0f, 2.0f, 4.0f),
	new TreeData(-35.0f, 19.0f, 3.0f, 4.0f),
	new TreeData(-28.0f, 22.0f, 2.0f, 3.0f),
	new TreeData(-33.0f, 26.0f, 3.0f, 3.0f),
	new TreeData(-29.0f, 31.0f, 3.0f, 4.0f),
	new TreeData(-32.0f, 38.0f, 2.0f, 3.0f),
	new TreeData(-27.0f, 41.0f, 3.0f, 4.0f),
	new TreeData(-31.0f, 45.0f, 2.0f, 4.0f),
	new TreeData(-28.0f, 48.0f, 3.0f, 5.0f),

	new TreeData(-25.0f, -48.0f, 2.0f, 3.0f),
	new TreeData(-20.0f, -42.0f, 3.0f, 4.0f),
	new TreeData(-22.0f, -39.0f, 2.0f, 3.0f),
	new TreeData(-19.0f, -34.0f, 2.0f, 3.0f),
	new TreeData(-23.0f, -30.0f, 3.0f, 4.0f),
	new TreeData(-24.0f, -24.0f, 2.0f, 3.0f),
	new TreeData(-16.0f, -21.0f, 2.0f, 3.0f),
	new TreeData(-17.0f, -17.0f, 3.0f, 3.0f),
	new TreeData(-25.0f, -13.0f, 2.0f, 4.0f),
	new TreeData(-23.0f, -8.0f, 2.0f, 3.0f),
	new TreeData(-17.0f, -2.0f, 3.0f, 3.0f),
	new TreeData(-16.0f, 1.0f, 2.0f, 3.0f),
	new TreeData(-19.0f, 4.0f, 3.0f, 3.0f),
	new TreeData(-22.0f, 8.0f, 2.0f, 4.0f),
	new TreeData(-21.0f, 14.0f, 2.0f, 3.0f),
	new TreeData(-16.0f, 19.0f, 2.0f, 3.0f),
	new TreeData(-23.0f, 24.0f, 3.0f, 3.0f),
	new TreeData(-18.0f, 28.0f, 2.0f, 4.0f),
	new TreeData(-24.0f, 31.0f, 2.0f, 3.0f),
	new TreeData(-20.0f, 36.0f, 2.0f, 3.0f),
	new TreeData(-22.0f, 41.0f, 3.0f, 3.0f),
	new TreeData(-21.0f, 45.0f, 2.0f, 3.0f),

	new TreeData(-12.0f, -40.0f, 2.0f, 4.0f),
	new TreeData(-11.0f, -35.0f, 3.0f, 3.0f),
	new TreeData(-10.0f, -29.0f, 1.0f, 3.0f),
	new TreeData(-9.0f, -26.0f, 2.0f, 2.0f),
	new TreeData(-6.0f, -22.0f, 2.0f, 3.0f),
	new TreeData(-15.0f, -15.0f, 1.0f, 3.0f),
	new TreeData(-8.0f, -11.0f, 2.0f, 3.0f),
	new TreeData(-14.0f, -6.0f, 2.0f, 4.0f),
	new TreeData(-12.0f, 0.0f, 2.0f, 3.0f),
	new TreeData(-7.0f, 4.0f, 2.0f, 2.0f),
	new TreeData(-13.0f, 8.0f, 2.0f, 2.0f),
	new TreeData(-9.0f, 13.0f, 1.0f, 3.0f),
	new TreeData(-13.0f, 17.0f, 3.0f, 4.0f),
	new TreeData(-6.0f, 23.0f, 2.0f, 3.0f),
	new TreeData(-12.0f, 27.0f, 1.0f, 2.0f),
	new TreeData(-8.0f, 32.0f, 2.0f, 3.0f),
	new TreeData(-10.0f, 37.0f, 3.0f, 3.0f),
	new TreeData(-11.0f, 42.0f, 2.0f, 2.0f),


	new TreeData(15.0f, 5.0f, 2.0f, 3.0f),
	new TreeData(15.0f, 10.0f, 2.0f, 3.0f),
	new TreeData(15.0f, 15.0f, 2.0f, 3.0f),
	new TreeData(15.0f, 20.0f, 2.0f, 3.0f),
	new TreeData(15.0f, 25.0f, 2.0f, 3.0f),
	new TreeData(15.0f, 30.0f, 2.0f, 3.0f),
	new TreeData(15.0f, 35.0f, 2.0f, 3.0f),
	new TreeData(15.0f, 40.0f, 2.0f, 3.0f),
	new TreeData(15.0f, 45.0f, 2.0f, 3.0f),

	new TreeData(25.0f, 5.0f, 2.0f, 3.0f),
	new TreeData(25.0f, 10.0f, 2.0f, 3.0f),
	new TreeData(25.0f, 15.0f, 2.0f, 3.0f),
	new TreeData(25.0f, 20.0f, 2.0f, 3.0f),
	new TreeData(25.0f, 25.0f, 2.0f, 3.0f),
	new TreeData(25.0f, 30.0f, 2.0f, 3.0f),
	new TreeData(25.0f, 35.0f, 2.0f, 3.0f),
	new TreeData(25.0f, 40.0f, 2.0f, 3.0f),
	new TreeData(25.0f, 45.0f, 2.0f, 3.0f),
    };
}

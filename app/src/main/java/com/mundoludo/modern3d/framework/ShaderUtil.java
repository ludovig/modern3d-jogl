package com.mundoludo.modern3d.framework;

import com.jogamp.newt.event.*;

import com.jogamp.opengl.GL;
import com.jogamp.opengl.GL3;

import com.jogamp.opengl.util.*;

import java.nio.ByteBuffer;
import java.nio.IntBuffer;
import java.util.ArrayList;

import static com.jogamp.opengl.GL.GL_FALSE;
import static com.jogamp.opengl.GL2ES2.*;
import static com.jogamp.opengl.GL3ES3.GL_GEOMETRY_SHADER;


public class ShaderUtil {

    public static int createShader(GL3 gl, int shaderType, String shaderFile) {
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
            System.err.println("Compiler failure in " + strShaderType + " shader: " + strInfoLog);

        }

        return shader;
    }

    public static int createProgram(GL3 gl, ArrayList<Integer> shaderList) {

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
}

package com.mundoludo.modern3d.framework.component;

import static com.jogamp.opengl.GL.*;

/**
 *
 * @author elect
 */
public class PrimitiveType {

    private String primitiveName;
    private int glPrimType;

    public PrimitiveType(String primitiveName, int glPrimType) {
        this.primitiveName = primitiveName;
        this.glPrimType = glPrimType;
    }

    public static PrimitiveType get(String type) {
        for (PrimitiveType primitiveType : allPrimitiveTypes) {
            if (type.equals(primitiveType.primitiveName)) {
                return primitiveType;
            }
        }
        throw new Error("Unknown 'cmd' field (" + type + ").");
    }

    private static PrimitiveType[] allPrimitiveTypes = {
        new PrimitiveType("triangles", GL_TRIANGLES),
        new PrimitiveType("tri-strip", GL_TRIANGLE_STRIP),
        new PrimitiveType("tri-fan", GL_TRIANGLE_FAN),
        new PrimitiveType("lines", GL_LINES),
        new PrimitiveType("line-strip", GL_LINE_STRIP),
        new PrimitiveType("line-loop", GL_LINE_LOOP),
        new PrimitiveType("points", GL_POINTS)};
    
    public int glPrimType() {
        return glPrimType;
    }
}

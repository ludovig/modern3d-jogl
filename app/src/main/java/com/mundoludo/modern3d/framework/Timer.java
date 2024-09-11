package com.mundoludo.modern3d.framework;

import glm_.Java;

public class Timer {
    private final float secDuration;
    private final double start;
    private float secAccumTime = 0f;

    public Timer(float secDuration) {
        this.secDuration = secDuration;
        this.start =  System.currentTimeMillis();
    }

    public boolean update() {
        secAccumTime = (float) (System.currentTimeMillis() - start) / 1_000f;

        return secAccumTime > secDuration;
    }

    public float getAlpha() {
        return Java.glm.clamp(secAccumTime / secDuration, 0.0f, 1.0f);
    }
}

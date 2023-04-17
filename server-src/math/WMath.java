/*
 * Decompiled with CFR 0.152.
 */
package com.wurmonline.math;

public final class WMath {
    private static final float pi = (float)Math.PI;
    public static final float pi2 = (float)Math.PI * 2;
    public static final float DEG_TO_RAD = (float)Math.PI / 180;
    public static final float RAD_TO_DEG = 57.295776f;
    public static final float FAR_AWAY = Float.MAX_VALUE;

    public static float atan2(float y, float x) {
        if (y == 0.0f) {
            return 0.0f;
        }
        float coeff_1 = 0.7853982f;
        float coeff_2 = 2.3561945f;
        float abs_y = Math.abs(y);
        float angle = 0.0f;
        if (x >= 0.0f) {
            float r = (x - abs_y) / (x + abs_y);
            angle = 0.7853982f - 0.7853982f * r;
        } else {
            float r = (x + abs_y) / (abs_y - x);
            angle = 2.3561945f - 0.7853982f * r;
        }
        if (y < 0.0f) {
            return -angle;
        }
        return angle;
    }

    public static int floor(float f) {
        return f > 0.0f ? (int)f : -((int)(-f));
    }

    public static float abs(float f) {
        return f >= 0.0f ? f : -f;
    }

    public static float getRadFromDeg(float deg) {
        return (float)Math.PI / 180 * deg;
    }

    public static float getDegFromRad(float rad) {
        return 57.295776f * rad;
    }
}


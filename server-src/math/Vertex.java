/*
 * Decompiled with CFR 0.152.
 */
package com.wurmonline.math;

public final class Vertex {
    public float[] point = new float[3];
    public byte flags;
    public float[] vertex = new float[3];
    public byte boneId;
    public byte refCount;
    public long lastRotateTime = 0L;
    public float[] rotatedVertex = new float[3];
    public float[] rotatedNormal = new float[3];
}


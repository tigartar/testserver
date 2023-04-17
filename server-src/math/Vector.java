/*
 * Decompiled with CFR 0.152.
 */
package com.wurmonline.math;

import com.wurmonline.math.Matrix;
import com.wurmonline.math.Position3D;

public final class Vector {
    public float[] vector = new float[4];

    public Vector() {
    }

    public Vector(float[] vector) {
        for (int i = 0; i < vector.length; ++i) {
            this.vector[i] = vector[i];
        }
    }

    public Vector(float v1, float v2, float v3) {
        this.vector[0] = v1;
        this.vector[1] = v2;
        this.vector[2] = v3;
    }

    public final Vector sub(Position3D p) {
        this.vector[0] = this.vector[0] - p.x;
        this.vector[1] = this.vector[1] - p.y;
        this.vector[2] = this.vector[2] - p.z;
        return this;
    }

    public final float[] getVector() {
        return this.vector;
    }

    public final float[] getVector3() {
        float[] v = new float[]{this.vector[0], this.vector[1], this.vector[2]};
        return v;
    }

    public final Vector transform(Matrix m) {
        float[] matrix = m.getMatrix();
        float vx = this.vector[0] * matrix[0] + this.vector[1] * matrix[4] + this.vector[2] * matrix[8] + matrix[12];
        float vy = this.vector[0] * matrix[1] + this.vector[1] * matrix[5] + this.vector[2] * matrix[9] + matrix[13];
        float vz = this.vector[0] * matrix[2] + this.vector[1] * matrix[6] + this.vector[2] * matrix[10] + matrix[14];
        float vw = this.vector[0] * matrix[3] + this.vector[1] * matrix[7] + this.vector[2] * matrix[11] + matrix[15];
        this.vector[0] = vx;
        this.vector[1] = vy;
        this.vector[2] = vz;
        this.vector[3] = vw;
        return this;
    }

    public final Vector transform3(Matrix m) {
        double[] v = new double[3];
        float[] matrix = m.getMatrix();
        v[0] = this.vector[0] * matrix[0] + this.vector[1] * matrix[4] + this.vector[2] * matrix[8];
        v[1] = this.vector[0] * matrix[1] + this.vector[1] * matrix[5] + this.vector[2] * matrix[9];
        v[2] = this.vector[0] * matrix[2] + this.vector[1] * matrix[6] + this.vector[2] * matrix[10];
        this.vector[0] = (float)v[0];
        this.vector[1] = (float)v[1];
        this.vector[2] = (float)v[2];
        this.vector[3] = 1.0f;
        return this;
    }

    public final Vector reset() {
        this.vector[0] = 0.0f;
        this.vector[1] = 0.0f;
        this.vector[2] = 0.0f;
        this.vector[3] = 1.0f;
        return this;
    }

    public final Vector set(float x, float y, float z) {
        this.vector[0] = x;
        this.vector[1] = y;
        this.vector[2] = z;
        return this;
    }

    public final Vector set(float x, float y, float z, float w) {
        this.vector[0] = x;
        this.vector[1] = y;
        this.vector[2] = z;
        this.vector[3] = w;
        return this;
    }

    public final Vector set(float[] values) {
        this.vector[0] = values[0];
        this.vector[1] = values[1];
        this.vector[2] = values[2];
        return this;
    }

    public final Vector set(Vector v) {
        this.vector[0] = v.vector[0];
        this.vector[1] = v.vector[1];
        this.vector[2] = v.vector[2];
        this.vector[3] = v.vector[3];
        return this;
    }

    public final Vector add(Vector v) {
        this.vector[0] = this.vector[0] + v.vector[0];
        this.vector[1] = this.vector[1] + v.vector[1];
        this.vector[2] = this.vector[2] + v.vector[2];
        this.vector[3] = this.vector[3] + v.vector[3];
        return this;
    }

    public final Vector add(Vector v1, Vector v2) {
        this.add(v1);
        return this.add(v2);
    }

    public final Vector scale(float scale) {
        this.vector[0] = this.vector[0] * scale;
        this.vector[1] = this.vector[1] * scale;
        this.vector[2] = this.vector[2] * scale;
        return this;
    }

    public final Vector normalize() {
        float len = this.length();
        this.vector[0] = this.vector[0] / len;
        this.vector[1] = this.vector[1] / len;
        this.vector[2] = this.vector[2] / len;
        return this;
    }

    public final float x() {
        return this.vector[0];
    }

    public final float y() {
        return this.vector[1];
    }

    public final float z() {
        return this.vector[2];
    }

    public final float w() {
        return this.vector[3];
    }

    public final Vector negate() {
        this.vector[0] = -this.vector[0];
        this.vector[1] = -this.vector[1];
        this.vector[2] = -this.vector[2];
        return this;
    }

    public final Vector cross(Vector v) {
        return this.set(this.vector[1] * v.vector[2] - this.vector[2] * v.vector[1], this.vector[2] * v.vector[0] - this.vector[0] * v.vector[2], this.vector[0] * v.vector[1] - this.vector[1] * v.vector[0]);
    }

    public final float length() {
        return (float)Math.sqrt(this.vector[0] * this.vector[0] + this.vector[1] * this.vector[1] + this.vector[2] * this.vector[2]);
    }

    public final float dot(Vector v) {
        return this.vector[0] * v.vector[0] + this.vector[1] * v.vector[1] + this.vector[2] * v.vector[2];
    }
}


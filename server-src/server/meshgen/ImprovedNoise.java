/*
 * Decompiled with CFR 0.152.
 */
package com.wurmonline.server.meshgen;

import java.util.Random;

public final class ImprovedNoise {
    private final int[] p = new int[512];

    public ImprovedNoise(long seed) {
        this.shuffle(seed);
    }

    public double noise(double x, double y, double z) {
        int X = (int)Math.floor(x) & 0xFF;
        int Y = (int)Math.floor(y) & 0xFF;
        int Z = (int)Math.floor(z) & 0xFF;
        x -= Math.floor(x);
        y -= Math.floor(y);
        z -= Math.floor(z);
        double u = this.fade(x);
        double v = this.fade(y);
        double w = this.fade(z);
        int A = this.p[X] + Y;
        int AA = this.p[A] + Z;
        int AB = this.p[A + 1] + Z;
        int B = this.p[X + 1] + Y;
        int BA = this.p[B] + Z;
        int BB = this.p[B + 1] + Z;
        return this.lerp(w, this.lerp(v, this.lerp(u, this.grad(this.p[AA], x, y, z), this.grad(this.p[BA], x - 1.0, y, z)), this.lerp(u, this.grad(this.p[AB], x, y - 1.0, z), this.grad(this.p[BB], x - 1.0, y - 1.0, z))), this.lerp(v, this.lerp(u, this.grad(this.p[AA + 1], x, y, z - 1.0), this.grad(this.p[BA + 1], x - 1.0, y, z - 1.0)), this.lerp(u, this.grad(this.p[AB + 1], x, y - 1.0, z - 1.0), this.grad(this.p[BB + 1], x - 1.0, y - 1.0, z - 1.0))));
    }

    double fade(double t) {
        return t * t * t * (t * (t * 6.0 - 15.0) + 10.0);
    }

    double lerp(double t, double a, double b) {
        return a + t * (b - a);
    }

    double grad(int hash, double x, double y, double z) {
        double u;
        int h = hash & 0xF;
        double d = u = h < 8 ? x : y;
        double v = h < 4 ? y : (h == 12 || h == 14 ? x : z);
        return ((h & 1) == 0 ? u : -u) + ((h & 2) == 0 ? v : -v);
    }

    public double perlinNoise(double x, double y) {
        double n = 0.0;
        for (int i = 0; i < 8; ++i) {
            double stepSize = 64.0 / (double)(1 << i);
            n += this.noise(x / stepSize, y / stepSize, 128.0) * 1.0 / (double)(1 << i);
        }
        return n;
    }

    public void shuffle(long seed) {
        int i;
        Random random = new Random(seed);
        int[] permutation = new int[256];
        for (i = 0; i < 256; ++i) {
            permutation[i] = i;
        }
        for (i = 0; i < 256; ++i) {
            int j = random.nextInt(256 - i) + i;
            int tmp = permutation[i];
            permutation[i] = permutation[j];
            permutation[j] = tmp;
            this.p[i] = permutation[i];
            this.p[i + 256] = permutation[i];
        }
    }
}


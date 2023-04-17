/*
 * Decompiled with CFR 0.152.
 */
package com.wurmonline.server.meshgen;

import com.wurmonline.server.meshgen.MeshGenGui;
import java.util.Random;

final class PerlinNoise {
    static float[] f_lut = new float[4096];
    float[][] noise;
    private float[][] noiseValues;
    private int level;
    private int width;
    private Random random;

    PerlinNoise(Random aRandom, int aLevel) {
        this.random = aRandom;
        this.width = 2 << aLevel;
        this.level = aLevel;
        if (this.width > 4096) {
            throw new IllegalArgumentException("Max size is 4096");
        }
        this.noise = new float[this.width][this.width];
        this.noiseValues = new float[this.width][this.width];
    }

    float[][] generatePerlinNoise(float persistence, int mode, MeshGenGui.Task task, int progressStart, int progressRange) {
        boolean highnoisesteps = true;
        boolean start = false;
        for (int x = 0; x < this.width; ++x) {
            for (int y = 0; y < this.width; ++y) {
                this.noiseValues[x][y] = 0.0f;
            }
        }
        for (int i = 0; i < this.level + 2; ++i) {
            int w = 1 << i;
            float perst = (float)Math.pow(0.999f, i - 0 + 1) * persistence;
            float amplitude = (float)Math.pow(perst, i - 0 + 1);
            if (i <= 1) {
                amplitude *= (float)(i * i) / 1.0f;
            }
            NoiseMap noiseMap = new NoiseMap(this.random, w, mode);
            for (int x = 0; x < this.width; ++x) {
                task.setNote(progressStart + (x + (i - 0) * this.width) / (this.level - 0 + 2));
                int xx = x * w / this.width;
                int xx2 = x * w % this.width * 4096 / this.width;
                int y = 0;
                while (y < this.width) {
                    int yy = y * w / this.width;
                    int yy2 = y * w % this.width * 4096 / this.width;
                    float[] fArray = this.noiseValues[x];
                    int n = y++;
                    fArray[n] = fArray[n] + noiseMap.getInterpolatedNoise(xx, yy, xx2, yy2) * amplitude;
                }
            }
        }
        return this.noiseValues;
    }

    void setRandom(Random aRandom) {
        this.random = aRandom;
    }

    static {
        for (int i = 0; i < f_lut.length; ++i) {
            double ft = (double)i / (double)f_lut.length * Math.PI;
            PerlinNoise.f_lut[i] = (float)((1.0 - Math.cos(ft)) * 0.5);
        }
    }

    private final class NoiseMap {
        private int lWidth;

        private NoiseMap(Random aRandom, int aWidth, int aMode) {
            this.lWidth = aWidth;
            if (aMode == 0) {
                for (int x = 0; x < aWidth; ++x) {
                    for (int y = 0; y < aWidth; ++y) {
                        PerlinNoise.this.noise[x][y] = (x == 0 || y == 0) && aMode < 3 ? 0.0f : aRandom.nextFloat();
                    }
                }
            } else {
                for (int x = 0; x < aWidth; ++x) {
                    for (int y = 0; y < aWidth; ++y) {
                        PerlinNoise.this.noise[x][y] = (x == 0 || y == 0) && aMode < 3 ? 0.0f : (aRandom.nextFloat() + aRandom.nextFloat()) / 2.0f;
                    }
                }
            }
        }

        private float getNoise(int x, int y) {
            return PerlinNoise.this.noise[x & this.lWidth - 1][y & this.lWidth - 1];
        }

        private float getInterpolatedNoise(int x, int y, int xFraction, int yFraction) {
            float v1 = this.getNoise(x, y);
            float v2 = this.getNoise(x + 1, y);
            float v3 = this.getNoise(x, y + 1);
            float v4 = this.getNoise(x + 1, y + 1);
            float i1 = this.interpolate(v1, v2, xFraction);
            float i2 = this.interpolate(v3, v4, xFraction);
            return this.interpolate(i1, i2, yFraction);
        }

        private final float interpolate(float a, float b, int x) {
            float f = f_lut[x];
            return a * (1.0f - f) + b * f;
        }
    }
}


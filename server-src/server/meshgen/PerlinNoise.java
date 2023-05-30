package com.wurmonline.server.meshgen;

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
      } else {
         this.noise = new float[this.width][this.width];
         this.noiseValues = new float[this.width][this.width];
      }
   }

   float[][] generatePerlinNoise(float persistence, int mode, MeshGenGui.Task task, int progressStart, int progressRange) {
      int highnoisesteps = 1;
      int start = 0;

      for(int x = 0; x < this.width; ++x) {
         for(int y = 0; y < this.width; ++y) {
            this.noiseValues[x][y] = 0.0F;
         }
      }

      for(int i = 0; i < this.level + 2; ++i) {
         int w = 1 << i;
         float perst = (float)Math.pow(0.999F, (double)(i - 0 + 1)) * persistence;
         float amplitude = (float)Math.pow((double)perst, (double)(i - 0 + 1));
         if (i <= 1) {
            amplitude *= (float)(i * i) / 1.0F;
         }

         PerlinNoise.NoiseMap noiseMap = new PerlinNoise.NoiseMap(this.random, w, mode);

         for(int x = 0; x < this.width; ++x) {
            task.setNote(progressStart + (x + (i - 0) * this.width) / (this.level - 0 + 2));
            int xx = x * w / this.width;
            int xx2 = x * w % this.width * 4096 / this.width;

            for(int y = 0; y < this.width; ++y) {
               int yy = y * w / this.width;
               int yy2 = y * w % this.width * 4096 / this.width;
               this.noiseValues[x][y] += noiseMap.getInterpolatedNoise(xx, yy, xx2, yy2) * amplitude;
            }
         }
      }

      return this.noiseValues;
   }

   void setRandom(Random aRandom) {
      this.random = aRandom;
   }

   static {
      for(int i = 0; i < f_lut.length; ++i) {
         double ft = (double)i / (double)f_lut.length * Math.PI;
         f_lut[i] = (float)((1.0 - Math.cos(ft)) * 0.5);
      }
   }

   private final class NoiseMap {
      private int lWidth;

      private NoiseMap(Random aRandom, int aWidth, int aMode) {
         this.lWidth = aWidth;
         if (aMode == 0) {
            for(int x = 0; x < aWidth; ++x) {
               for(int y = 0; y < aWidth; ++y) {
                  if ((x == 0 || y == 0) && aMode < 3) {
                     PerlinNoise.this.noise[x][y] = 0.0F;
                  } else {
                     PerlinNoise.this.noise[x][y] = aRandom.nextFloat();
                  }
               }
            }
         } else {
            for(int x = 0; x < aWidth; ++x) {
               for(int y = 0; y < aWidth; ++y) {
                  if ((x == 0 || y == 0) && aMode < 3) {
                     PerlinNoise.this.noise[x][y] = 0.0F;
                  } else {
                     PerlinNoise.this.noise[x][y] = (aRandom.nextFloat() + aRandom.nextFloat()) / 2.0F;
                  }
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
         float f = PerlinNoise.f_lut[x];
         return a * (1.0F - f) + b * f;
      }
   }
}

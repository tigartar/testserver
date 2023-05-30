package com.wurmonline.server.creatures;

import com.wurmonline.math.Vector3f;

public class BoxMatrix {
   public float[] mf = new float[16];

   public BoxMatrix(boolean identity) {
      if (identity) {
         this.identity();
      }
   }

   public final BoxMatrix multiply(BoxMatrix inM) {
      BoxMatrix result = new BoxMatrix(false);

      for(int i = 0; i < 16; i += 4) {
         for(int j = 0; j < 4; ++j) {
            result.mf[i + j] = this.mf[i + 0] * inM.mf[0 + j]
               + this.mf[i + 1] * inM.mf[4 + j]
               + this.mf[i + 2] * inM.mf[8 + j]
               + this.mf[i + 3] * inM.mf[12 + j];
         }
      }

      return result;
   }

   public final Vector3f multiply(Vector3f Point) {
      float x = Point.x * this.mf[0] + Point.y * this.mf[4] + Point.z * this.mf[8] + this.mf[12];
      float y = Point.x * this.mf[1] + Point.y * this.mf[5] + Point.z * this.mf[9] + this.mf[13];
      float z = Point.x * this.mf[2] + Point.y * this.mf[6] + Point.z * this.mf[10] + this.mf[14];
      return new Vector3f(x, y, z);
   }

   public void rotate(float degrees, boolean xRot, boolean yRot, boolean zRot) {
      BoxMatrix temp = new BoxMatrix(true);
      if (xRot) {
         temp.rotX(-degrees);
      }

      if (yRot) {
         temp.rotY(-degrees);
      }

      if (zRot) {
         temp.rotZ(-degrees);
      }

      this.mf = temp.multiply(this).mf;
   }

   public void scale(float sx, float sy, float sz) {
      for(int x = 0; x < 4; ++x) {
         this.mf[x] *= sx;
      }

      for(int var5 = 4; var5 < 8; ++var5) {
         this.mf[var5] *= sy;
      }

      for(int var6 = 8; var6 < 12; ++var6) {
         this.mf[var6] *= sz;
      }
   }

   public void translate(Vector3f test) {
      for(int j = 0; j < 4; ++j) {
         this.mf[12 + j] += test.x * this.mf[j] + test.y * this.mf[4 + j] + test.z * this.mf[8 + j];
      }
   }

   public final BoxMatrix rotationOnly() {
      BoxMatrix Temp = new BoxMatrix(true);
      Temp.mf = this.mf;
      Temp.mf[12] = 0.0F;
      Temp.mf[13] = 0.0F;
      Temp.mf[14] = 0.0F;
      return Temp;
   }

   private void rotX(float angle) {
      this.mf[5] = (float)Math.cos(Math.toRadians((double)angle));
      this.mf[6] = (float)Math.sin(Math.toRadians((double)angle));
      this.mf[9] = (float)(-Math.sin(Math.toRadians((double)angle)));
      this.mf[10] = (float)Math.cos(Math.toRadians((double)angle));
   }

   private void rotY(float angle) {
      this.mf[0] = (float)Math.cos(Math.toRadians((double)angle));
      this.mf[2] = (float)(-Math.sin(Math.toRadians((double)angle)));
      this.mf[8] = (float)Math.sin(Math.toRadians((double)angle));
      this.mf[10] = (float)Math.cos(Math.toRadians((double)angle));
   }

   private void rotZ(float angle) {
      this.mf[0] = (float)Math.cos(Math.toRadians((double)angle));
      this.mf[1] = (float)Math.sin(Math.toRadians((double)angle));
      this.mf[4] = (float)(-Math.sin(Math.toRadians((double)angle)));
      this.mf[5] = (float)Math.cos(Math.toRadians((double)angle));
   }

   public final BoxMatrix InvertSimple() {
      BoxMatrix R = new BoxMatrix(false);
      R.mf[0] = this.mf[0];
      R.mf[1] = this.mf[4];
      R.mf[2] = this.mf[8];
      R.mf[3] = 0.0F;
      R.mf[4] = this.mf[1];
      R.mf[5] = this.mf[5];
      R.mf[6] = this.mf[9];
      R.mf[7] = 0.0F;
      R.mf[8] = this.mf[2];
      R.mf[9] = this.mf[6];
      R.mf[10] = this.mf[10];
      R.mf[11] = 0.0F;
      R.mf[12] = -(this.mf[12] * this.mf[0]) - this.mf[13] * this.mf[1] - this.mf[14] * this.mf[2];
      R.mf[13] = -(this.mf[12] * this.mf[4]) - this.mf[13] * this.mf[5] - this.mf[14] * this.mf[6];
      R.mf[14] = -(this.mf[12] * this.mf[8]) - this.mf[13] * this.mf[9] - this.mf[14] * this.mf[10];
      R.mf[15] = 1.0F;
      return R;
   }

   public final BoxMatrix InvertRot() {
      BoxMatrix R = new BoxMatrix(false);
      R.mf[0] = this.mf[0];
      R.mf[1] = this.mf[4];
      R.mf[2] = this.mf[8];
      R.mf[3] = 0.0F;
      R.mf[4] = this.mf[1];
      R.mf[5] = this.mf[5];
      R.mf[6] = this.mf[9];
      R.mf[7] = 0.0F;
      R.mf[8] = this.mf[2];
      R.mf[9] = this.mf[6];
      R.mf[10] = this.mf[10];
      R.mf[11] = 0.0F;
      R.mf[12] = 0.0F;
      R.mf[13] = 0.0F;
      R.mf[14] = 0.0F;
      R.mf[15] = 1.0F;
      return R;
   }

   public void RotateMatrix(float fDegrees, float x, float y, float z) {
      this.identity();
      float cosA = (float)Math.cos(Math.toRadians((double)fDegrees));
      float sinA = (float)Math.sin(Math.toRadians((double)fDegrees));
      float m = 1.0F - cosA;
      this.mf[0] = cosA + x * x * m;
      this.mf[5] = cosA + y * y * m;
      this.mf[10] = cosA + z * z * m;
      float tmp1 = x * y * m;
      float tmp2 = z * sinA;
      this.mf[4] = tmp1 + tmp2;
      this.mf[1] = tmp1 - tmp2;
      tmp1 = x * z * m;
      tmp2 = y * sinA;
      this.mf[8] = tmp1 - tmp2;
      this.mf[2] = tmp1 + tmp2;
      tmp1 = y * z * m;
      tmp2 = x * sinA;
      this.mf[9] = tmp1 + tmp2;
      this.mf[6] = tmp1 - tmp2;
   }

   public final Vector3f getTranslate() {
      return new Vector3f(this.mf[12], this.mf[13], this.mf[14]);
   }

   void identity() {
      this.mf[0] = 1.0F;
      this.mf[1] = 0.0F;
      this.mf[2] = 0.0F;
      this.mf[3] = 0.0F;
      this.mf[4] = 0.0F;
      this.mf[5] = 1.0F;
      this.mf[6] = 0.0F;
      this.mf[7] = 0.0F;
      this.mf[8] = 0.0F;
      this.mf[9] = 0.0F;
      this.mf[10] = 1.0F;
      this.mf[11] = 0.0F;
      this.mf[12] = 0.0F;
      this.mf[13] = 0.0F;
      this.mf[14] = 0.0F;
      this.mf[15] = 1.0F;
   }
}

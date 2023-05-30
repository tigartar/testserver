package com.wurmonline.math;

public final class Quaternion {
   private float[] quat = new float[4];

   public Quaternion() {
   }

   public Quaternion(float[] angles) {
      this.fromAngles(angles);
   }

   public Quaternion(Quaternion q1, Quaternion q2, float interp) {
      this.slerp(q1, q2, interp);
   }

   public Quaternion mult(Quaternion q) {
      float x = this.quat[1] * q.quat[2] - this.quat[2] * q.quat[1] + this.quat[3] * q.quat[0] + this.quat[0] * q.quat[3];
      float y = this.quat[2] * q.quat[0] - this.quat[0] * q.quat[2] + this.quat[3] * q.quat[1] + this.quat[1] * q.quat[3];
      float z = this.quat[0] * q.quat[1] - this.quat[1] * q.quat[0] + this.quat[3] * q.quat[2] + this.quat[2] * q.quat[3];
      float s = this.quat[3] * q.quat[3] - (this.quat[0] * q.quat[0] + this.quat[1] * q.quat[1] + this.quat[2] * q.quat[2]);
      this.quat[0] = x;
      this.quat[1] = y;
      this.quat[2] = z;
      this.quat[3] = s;
      return this;
   }

   public final Quaternion fromAngles(float[] angles) {
      return this.fromAngles(angles[0], angles[1], angles[2]);
   }

   public final Quaternion fromAngles(float x, float y, float z) {
      float angle = z * 0.5F;
      double sy = Math.sin((double)angle);
      double cy = Math.cos((double)angle);
      angle = y * 0.5F;
      double sp = Math.sin((double)angle);
      double cp = Math.cos((double)angle);
      angle = x * 0.5F;
      double sr = Math.sin((double)angle);
      double cr = Math.cos((double)angle);
      double crcp = cr * cp;
      double srsp = sr * sp;
      this.quat[0] = (float)(sr * cp * cy - cr * sp * sy);
      this.quat[1] = (float)(cr * sp * cy + sr * cp * sy);
      this.quat[2] = (float)(crcp * sy - srsp * cy);
      this.quat[3] = (float)(crcp * cy + srsp * sy);
      return this;
   }

   public final Quaternion fromAxisAngle(Vector3f axis, float angle) {
      float halfangle = 0.5F * angle;
      float sinval = (float)Math.sin((double)halfangle);
      this.quat[0] = sinval * axis.x;
      this.quat[1] = sinval * axis.y;
      this.quat[2] = sinval * axis.z;
      this.quat[3] = (float)Math.cos((double)halfangle);
      return this.normalize();
   }

   public void slerp(Quaternion q1, Quaternion q2, float interp) {
      float a = 0.0F;
      float b = 0.0F;

      for(int i = 0; i < 4; ++i) {
         a += (q1.quat[i] - q2.quat[i]) * (q1.quat[i] - q2.quat[i]);
         b += (q1.quat[i] + q2.quat[i]) * (q1.quat[i] + q2.quat[i]);
      }

      if (a > b) {
         q2.negate();
      }

      float cosom = q1.quat[0] * q2.quat[0] + q1.quat[1] * q2.quat[1] + q1.quat[2] * q2.quat[2] + q1.quat[3] * q2.quat[3];
      if (1.0 + (double)cosom > 1.0E-8) {
         double sclq1;
         double sclq2;
         if (1.0 - (double)cosom > 1.0E-8) {
            double omega = Math.acos((double)cosom);
            double sinom = Math.sin(omega);
            sclq1 = Math.sin((1.0 - (double)interp) * omega) / sinom;
            sclq2 = Math.sin((double)interp * omega) / sinom;
         } else {
            sclq1 = 1.0 - (double)interp;
            sclq2 = (double)interp;
         }

         for(int var16 = 0; var16 < 4; ++var16) {
            this.quat[var16] = (float)(sclq1 * (double)q1.quat[var16] + sclq2 * (double)q2.quat[var16]);
         }
      } else {
         this.quat[0] = -q1.quat[1];
         this.quat[1] = q1.quat[0];
         this.quat[2] = -q1.quat[3];
         this.quat[3] = q1.quat[2];
         double sclq1 = Math.sin((1.0 - (double)interp) * 0.5 * Math.PI);
         double sclq2 = Math.sin((double)interp * 0.5 * Math.PI);

         for(int var17 = 0; var17 < 3; ++var17) {
            this.quat[var17] = (float)(sclq1 * (double)q1.quat[var17] + sclq2 * (double)this.quat[var17]);
         }
      }
   }

   public void negate() {
      this.quat[0] = -this.quat[0];
      this.quat[1] = -this.quat[1];
      this.quat[2] = -this.quat[2];
      this.quat[3] = -this.quat[3];
   }

   public void conjugate() {
      this.quat[0] = -this.quat[0];
      this.quat[1] = -this.quat[1];
      this.quat[2] = -this.quat[2];
      this.quat[3] = this.quat[3];
   }

   public final void identity() {
      this.quat[0] = 0.0F;
      this.quat[1] = 0.0F;
      this.quat[2] = 0.0F;
      this.quat[3] = 1.0F;
   }

   public final Quaternion normalize() {
      float norm = this.quat[0] * this.quat[0] + this.quat[1] * this.quat[1] + this.quat[2] * this.quat[2] + this.quat[3] * this.quat[3];
      float invscale = 1.0F / (float)Math.sqrt((double)norm);
      this.quat[0] *= invscale;
      this.quat[1] *= invscale;
      this.quat[2] *= invscale;
      this.quat[3] *= invscale;
      return this;
   }

   public final Quaternion fromMatrix(Matrix m) {
      float[] mat = m.getMatrix();
      float trace = mat[0] + mat[5] + mat[10];
      if (trace > 0.0F) {
         float root = (float)Math.sqrt((double)(trace + 1.0F));
         this.quat[3] = 0.5F * root;
         root = 0.5F / root;
         this.quat[0] = (mat[6] - mat[9]) * root;
         this.quat[1] = (mat[8] - mat[2]) * root;
         this.quat[2] = (mat[1] - mat[4]) * root;
      } else {
         int i = 0;
         int j = 1;
         int k = 2;
         if (mat[5] > mat[0]) {
            i = 1;
            j = 2;
            k = 0;
         } else if (mat[10] > mat[i + 4 * i]) {
            i = 2;
            j = 0;
            k = 1;
         }

         float root = (float)Math.sqrt((double)(mat[i + 4 * i] - mat[j + 4 * j] - mat[k + 4 * k] + 1.0F));
         this.quat[i] = 0.5F * root;
         root = 0.5F / root;
         this.quat[j] = (mat[j + 4 * i] + mat[i + 4 * j]) * root;
         this.quat[k] = (mat[k + 4 * i] + mat[i + 4 * k]) * root;
         this.quat[3] = (mat[k + 4 * j] - mat[j + 4 * k]) * root;
      }

      return this.normalize();
   }

   public final Vector3f rotate(Vector3f v, Vector3f result) {
      if (result == null) {
         result = new Vector3f();
      }

      float v1x = this.quat[1] * v.z - this.quat[2] * v.y + this.quat[3] * v.x;
      float v1y = this.quat[2] * v.x - this.quat[0] * v.z + this.quat[3] * v.y;
      float v1z = this.quat[0] * v.y - this.quat[1] * v.x + this.quat[3] * v.z;
      float dotv = this.quat[0] * v.x + this.quat[1] * v.y + this.quat[2] * v.z;
      result.x = this.quat[0] * dotv + this.quat[3] * v1x - (v1y * this.quat[2] - v1z * this.quat[1]);
      result.y = this.quat[1] * dotv + this.quat[3] * v1y - (v1z * this.quat[0] - v1x * this.quat[2]);
      result.z = this.quat[2] * dotv + this.quat[3] * v1z - (v1x * this.quat[1] - v1y * this.quat[0]);
      return result;
   }

   public final Vector rotate(Vector v) {
      float vx = v.x();
      float vy = v.y();
      float vz = v.z();
      float v1x = this.quat[1] * vz - this.quat[2] * vy + this.quat[3] * vx;
      float v1y = this.quat[2] * vx - this.quat[0] * vz + this.quat[3] * vy;
      float v1z = this.quat[0] * vy - this.quat[1] * vx + this.quat[3] * vz;
      float dotv = this.quat[0] * vx + this.quat[1] * vy + this.quat[2] * vz;
      return v.set(
         this.quat[0] * dotv + this.quat[3] * v1x - (v1y * this.quat[2] - v1z * this.quat[1]),
         this.quat[1] * dotv + this.quat[3] * v1y - (v1z * this.quat[0] - v1x * this.quat[2]),
         this.quat[2] * dotv + this.quat[3] * v1z - (v1x * this.quat[1] - v1y * this.quat[0])
      );
   }

   public final float[] getQuat() {
      return this.quat;
   }

   public final void setQuat(float[] quat) {
      this.quat = quat;
   }

   public final void set(Quaternion q) {
      this.quat[0] = q.quat[0];
      this.quat[1] = q.quat[1];
      this.quat[2] = q.quat[2];
      this.quat[3] = q.quat[3];
   }
}

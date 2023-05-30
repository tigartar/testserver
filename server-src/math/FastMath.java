package com.wurmonline.math;

import java.util.Random;

public final class FastMath {
   public static final double DBL_EPSILON = 2.220446E-16F;
   public static final float FLT_EPSILON = 1.1920929E-7F;
   public static final float ZERO_TOLERANCE = 1.0E-4F;
   public static final float ONE_THIRD = 0.33333334F;
   public static final float PI = (float) Math.PI;
   public static final float TWO_PI = (float) (Math.PI * 2);
   public static final float HALF_PI = (float) (Math.PI / 2);
   public static final float QUARTER_PI = (float) (Math.PI / 4);
   public static final float INV_PI = 0.31830987F;
   public static final float INV_TWO_PI = 0.15915494F;
   public static final float DEG_TO_RAD = (float) (Math.PI / 180.0);
   public static final float RAD_TO_DEG = 180.0F / (float)Math.PI;
   public static final Random rand = new Random(System.currentTimeMillis());

   private FastMath() {
   }

   public static boolean isPowerOfTwo(int number) {
      return number > 0 && (number & number - 1) == 0;
   }

   public static int nearestPowerOfTwo(int number) {
      return (int)Math.pow(2.0, Math.ceil(Math.log((double)number) / Math.log(2.0)));
   }

   public static float lERP(float percent, float startValue, float endValue) {
      return startValue == endValue ? startValue : (1.0F - percent) * startValue + percent * endValue;
   }

   public static float acos(float fValue) {
      if (-1.0F < fValue) {
         return fValue < 1.0F ? (float)Math.acos((double)fValue) : 0.0F;
      } else {
         return (float) Math.PI;
      }
   }

   public static float asin(float fValue) {
      if (-1.0F < fValue) {
         return fValue < 1.0F ? (float)Math.asin((double)fValue) : (float) (Math.PI / 2);
      } else {
         return (float) (-Math.PI / 2);
      }
   }

   public static float atan(float fValue) {
      return (float)Math.atan((double)fValue);
   }

   public static float atan2(float fY, float fX) {
      return (float)Math.atan2((double)fY, (double)fX);
   }

   public static float ceil(float fValue) {
      return (float)Math.ceil((double)fValue);
   }

   public static float reduceSinAngle(float radians) {
      radians %= (float) (Math.PI * 2);
      if (Math.abs(radians) > (float) Math.PI) {
         radians -= (float) (Math.PI * 2);
      }

      if (Math.abs(radians) > (float) (Math.PI / 2)) {
         radians = (float) Math.PI - radians;
      }

      return radians;
   }

   public static float sin(float fValue) {
      fValue = reduceSinAngle(fValue);
      return (double)Math.abs(fValue) <= Math.PI / 4 ? (float)Math.sin((double)fValue) : (float)Math.cos((Math.PI / 2) - (double)fValue);
   }

   public static float cos(float fValue) {
      return sin(fValue + (float) (Math.PI / 2));
   }

   public static float exp(float fValue) {
      return (float)Math.exp((double)fValue);
   }

   public static float abs(float fValue) {
      return fValue < 0.0F ? -fValue : fValue;
   }

   public static float floor(float fValue) {
      return (float)Math.floor((double)fValue);
   }

   public static float invSqrt(float fValue) {
      return (float)(1.0 / Math.sqrt((double)fValue));
   }

   public static float log(float fValue) {
      return (float)Math.log((double)fValue);
   }

   public static float log(float value, float base) {
      return (float)(Math.log((double)value) / Math.log((double)base));
   }

   public static float pow(float fBase, float fExponent) {
      return (float)Math.pow((double)fBase, (double)fExponent);
   }

   public static float sqr(float fValue) {
      return fValue * fValue;
   }

   public static float sqrt(float fValue) {
      return (float)Math.sqrt((double)fValue);
   }

   public static float tan(float fValue) {
      return (float)Math.tan((double)fValue);
   }

   public static int sign(int iValue) {
      if (iValue > 0) {
         return 1;
      } else {
         return iValue < 0 ? -1 : 0;
      }
   }

   public static float sign(float fValue) {
      return Math.signum(fValue);
   }

   public static float determinant(
      double m00,
      double m01,
      double m02,
      double m03,
      double m10,
      double m11,
      double m12,
      double m13,
      double m20,
      double m21,
      double m22,
      double m23,
      double m30,
      double m31,
      double m32,
      double m33
   ) {
      double det01 = m20 * m31 - m21 * m30;
      double det02 = m20 * m32 - m22 * m30;
      double det03 = m20 * m33 - m23 * m30;
      double det12 = m21 * m32 - m22 * m31;
      double det13 = m21 * m33 - m23 * m31;
      double det23 = m22 * m33 - m23 * m32;
      return (float)(
         m00 * (m11 * det23 - m12 * det13 + m13 * det12)
            - m01 * (m10 * det23 - m12 * det03 + m13 * det02)
            + m02 * (m10 * det13 - m11 * det03 + m13 * det01)
            - m03 * (m10 * det12 - m11 * det02 + m12 * det01)
      );
   }

   public static float nextRandomFloat() {
      return rand.nextFloat();
   }

   public static int nextRandomInt(int min, int max) {
      return (int)(nextRandomFloat() * (float)(max - min + 1)) + min;
   }

   public static int nextRandomInt() {
      return rand.nextInt();
   }

   public static Vector3f sphericalToCartesian(Vector3f sphereCoords, Vector3f store) {
      store.y = sphereCoords.x * sin(sphereCoords.z);
      float a = sphereCoords.x * cos(sphereCoords.z);
      store.x = a * cos(sphereCoords.y);
      store.z = a * sin(sphereCoords.y);
      return store;
   }

   public static Vector3f cartesianToSpherical(Vector3f cartCoords, Vector3f store) {
      if (cartCoords.x == 0.0F) {
         cartCoords.x = 1.1920929E-7F;
      }

      store.x = sqrt(cartCoords.x * cartCoords.x + cartCoords.y * cartCoords.y + cartCoords.z * cartCoords.z);
      store.y = atan(cartCoords.z / cartCoords.x);
      if (cartCoords.x < 0.0F) {
         store.y += (float) Math.PI;
      }

      store.z = asin(cartCoords.y / store.x);
      return store;
   }

   public static Vector3f sphericalToCartesianZ(Vector3f sphereCoords, Vector3f store) {
      store.z = sphereCoords.x * sin(sphereCoords.z);
      float a = sphereCoords.x * cos(sphereCoords.z);
      store.x = a * cos(sphereCoords.y);
      store.y = a * sin(sphereCoords.y);
      return store;
   }

   public static Vector3f cartesianZToSpherical(Vector3f cartCoords, Vector3f store) {
      if (cartCoords.x == 0.0F) {
         cartCoords.x = 1.1920929E-7F;
      }

      store.x = sqrt(cartCoords.x * cartCoords.x + cartCoords.y * cartCoords.y + cartCoords.z * cartCoords.z);
      store.z = atan(cartCoords.z / cartCoords.x);
      if (cartCoords.x < 0.0F) {
         store.z += (float) Math.PI;
      }

      store.y = asin(cartCoords.y / store.x);
      return store;
   }

   public static float normalize(float val, float min, float max) {
      if (!Float.isInfinite(val) && !Float.isNaN(val)) {
         float range = max - min;

         while(val > max) {
            val -= range;
         }

         while(val < min) {
            val += range;
         }

         return val;
      } else {
         return 0.0F;
      }
   }

   public static float copysign(float x, float y) {
      if (y >= 0.0F && x <= 0.0F) {
         return -x;
      } else {
         return y < 0.0F && x >= 0.0F ? -x : x;
      }
   }

   public static float clamp(float input, float min, float max) {
      return input < min ? min : (input > max ? max : input);
   }
}

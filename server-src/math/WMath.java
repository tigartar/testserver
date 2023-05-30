package com.wurmonline.math;

public final class WMath {
   private static final float pi = (float) Math.PI;
   public static final float pi2 = (float) (Math.PI * 2);
   public static final float DEG_TO_RAD = (float) (Math.PI / 180.0);
   public static final float RAD_TO_DEG = 180.0F / (float)Math.PI;
   public static final float FAR_AWAY = Float.MAX_VALUE;

   public static float atan2(float y, float x) {
      if (y == 0.0F) {
         return 0.0F;
      } else {
         float coeff_1 = (float) (Math.PI / 4);
         float coeff_2 = (float) (Math.PI * 3.0 / 4.0);
         float abs_y = Math.abs(y);
         float angle = 0.0F;
         if (x >= 0.0F) {
            float r = (x - abs_y) / (x + abs_y);
            angle = (float) (Math.PI / 4) - (float) (Math.PI / 4) * r;
         } else {
            float r = (x + abs_y) / (abs_y - x);
            angle = (float) (Math.PI * 3.0 / 4.0) - (float) (Math.PI / 4) * r;
         }

         return y < 0.0F ? -angle : angle;
      }
   }

   public static int floor(float f) {
      return f > 0.0F ? (int)f : -((int)(-f));
   }

   public static float abs(float f) {
      return f >= 0.0F ? f : -f;
   }

   public static float getRadFromDeg(float deg) {
      return (float) (Math.PI / 180.0) * deg;
   }

   public static float getDegFromRad(float rad) {
      return (180.0F / (float)Math.PI) * rad;
   }
}

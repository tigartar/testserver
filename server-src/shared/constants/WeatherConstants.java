package com.wurmonline.shared.constants;

public final class WeatherConstants {
   private static final double DEGS_TO_RADS = Math.PI / 180.0;

   private WeatherConstants() {
   }

   public static final float getNormalizedWindX(float windRotation) {
      return -((float)Math.sin((double)windRotation * (Math.PI / 180.0)));
   }

   public static final float getNormalizedWindY(float windRotation) {
      return (float)Math.cos((double)windRotation * (Math.PI / 180.0));
   }

   public static final float getWindX(float windRotation, float windPower) {
      return -((float)Math.sin((double)windRotation * (Math.PI / 180.0))) * Math.abs(windPower);
   }

   public static final float getWindY(float windRotation, float windPower) {
      return (float)Math.cos((double)windRotation * (Math.PI / 180.0)) * Math.abs(windPower);
   }
}

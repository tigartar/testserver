package com.wurmonline.shared.util;

import java.util.Random;

public final class TerrainUtilities {
   private static final Random random = new Random();

   private TerrainUtilities() {
   }

   public static float getTreePosX(int xTile, int yTile) {
      random.setSeed((long)xTile * 31273612L + (long)yTile * 4327864168313L);
      return random.nextFloat() * 0.75F + 0.125F;
   }

   public static float getTreePosY(int xTile, int yTile) {
      random.setSeed((long)xTile * 31273612L + (long)yTile * 4327864168314L);
      return random.nextFloat() * 0.75F + 0.125F;
   }

   public static float getTreeRotation(int xTile, int yTile) {
      random.setSeed((long)xTile * 31273612L + (long)yTile * 4327864168315L);
      return random.nextFloat() * 360.0F;
   }
}

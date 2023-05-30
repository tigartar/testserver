package com.wurmonline.server.creatures;

public final class CreatureMove {
   public long timestamp = 0L;
   public float diffX;
   public float diffY;
   public float diffZ;
   public int rotation;

   public void resetXYZ() {
      this.diffX = 0.0F;
      this.diffY = 0.0F;
      this.diffZ = 0.0F;
   }
}

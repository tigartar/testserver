package com.wurmonline.server.zones;

public class CropTile implements Comparable<CropTile> {
   private int data;
   private int x;
   private int y;
   private int cropType;
   private boolean onSurface;

   public CropTile(int tileData, int tileX, int tileY, int typeOfCrop, boolean surface) {
      this.data = tileData;
      this.x = tileX;
      this.y = tileY;
      this.cropType = typeOfCrop;
      this.onSurface = surface;
   }

   public final int getData() {
      return this.data;
   }

   public final int getX() {
      return this.x;
   }

   public final int getY() {
      return this.y;
   }

   public final int getCropType() {
      return this.cropType;
   }

   public final boolean isOnSurface() {
      return this.onSurface;
   }

   @Override
   public boolean equals(Object obj) {
      if (!(obj instanceof CropTile)) {
         return false;
      } else {
         CropTile c = (CropTile)obj;
         return c.getCropType() == this.getCropType()
            && c.getData() == this.getData()
            && c.getX() == this.getX()
            && c.getY() == this.getY()
            && c.isOnSurface() == this.isOnSurface();
      }
   }

   public int compareTo(CropTile o) {
      int EQUAL = 0;
      int AFTER = 1;
      int BEFORE = -1;
      if (o.equals(this)) {
         return 0;
      } else {
         return o.getCropType() > this.getCropType() ? 1 : -1;
      }
   }
}

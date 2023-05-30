package com.wurmonline.server.zones;

import com.wurmonline.server.items.Item;

public abstract class GenericZone {
   private Item zoneOwner;
   private float cachedQL;
   private int startX;
   private int startY;
   private int endX;
   private int endY;

   public GenericZone(Item i) {
      this.zoneOwner = i;
      this.updateZone();
   }

   public abstract float getStrengthForTile(int var1, int var2, boolean var3);

   public boolean containsTile(int tileX, int tileY) {
      if (this.zoneOwner == null) {
         return false;
      } else {
         if (this.cachedQL != this.getCurrentQL()) {
            this.updateZone();
         }

         return tileX >= this.startX && tileX <= this.endX && tileY >= this.startY && tileY <= this.endY;
      }
   }

   public abstract void updateZone();

   public Item getZoneItem() {
      return this.zoneOwner;
   }

   public void setZoneItem(Item i) {
      this.zoneOwner = i;
   }

   public void setCachedQL(float ql) {
      this.cachedQL = ql;
   }

   public void setBounds(int sx, int sy, int ex, int ey) {
      this.startX = sx;
      this.startY = sy;
      this.endX = ex;
      this.endY = ey;
   }

   protected abstract float getCurrentQL();
}

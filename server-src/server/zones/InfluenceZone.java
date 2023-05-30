package com.wurmonline.server.zones;

import com.wurmonline.server.items.Item;

public class InfluenceZone extends GenericZone {
   public InfluenceZone(Item i) {
      super(i);
   }

   @Override
   public float getStrengthForTile(int tileX, int tileY, boolean surfaced) {
      if (this.getZoneItem() == null) {
         return 0.0F;
      } else {
         int xDiff = Math.abs(this.getZoneItem().getTileX() - tileX);
         int yDiff = Math.abs(this.getZoneItem().getTileY() - tileY);
         return this.getCurrentQL() - (float)Math.max(xDiff, yDiff);
      }
   }

   @Override
   public void updateZone() {
      if (this.getZoneItem() == null) {
         this.setCachedQL(0.0F);
      } else {
         int dist = (int)this.getZoneItem().getCurrentQualityLevel();
         this.setBounds(
            this.getZoneItem().getTileX() - dist,
            this.getZoneItem().getTileY() - dist,
            this.getZoneItem().getTileX() + dist,
            this.getZoneItem().getTileY() + dist
         );
         this.setCachedQL(this.getZoneItem().getCurrentQualityLevel());
      }
   }

   @Override
   protected float getCurrentQL() {
      return this.getZoneItem() == null ? 0.0F : this.getZoneItem().getCurrentQualityLevel();
   }
}

package com.wurmonline.server.zones;

import com.wurmonline.server.items.Item;

public class TurretZone extends GenericZone {
   public static final float DISTMOD_QLMULTIPLIER = 5.0F;
   public static final int DISTMOD_TURRET = 3;
   public static final int DISTMOD_ARCHERYTOWER = 5;

   public TurretZone(Item i) {
      super(i);
      this.updateZone();
   }

   @Override
   public float getStrengthForTile(int tileX, int tileY, boolean surfaced) {
      if (this.getZoneItem() == null) {
         return 0.0F;
      } else if (this.getZoneItem().getTemplateId() == 934) {
         return 0.0F;
      } else if (this.getZoneItem().isOnSurface() != surfaced) {
         return 0.0F;
      } else if (!this.containsTile(tileX, tileY)) {
         return 0.0F;
      } else {
         int xDiff = Math.abs(tileX - this.getZoneItem().getTileX()) * 4;
         int yDiff = Math.abs(tileY - this.getZoneItem().getTileY()) * 4;
         float actDist = (float)Math.sqrt((double)(xDiff * xDiff + yDiff * yDiff));
         return this.getCurrentQL() - actDist;
      }
   }

   @Override
   public void updateZone() {
      if (this.getZoneItem() != null) {
         float ql = this.getCurrentQL();
         float distanceModifier = ql / 100.0F * 5.0F;
         int dist = (int)((float)(this.getZoneItem().isEnchantedTurret() ? 3 : 5) * distanceModifier);
         this.setBounds(
            this.getZoneItem().getTileX() - dist,
            this.getZoneItem().getTileY() - dist,
            this.getZoneItem().getTileX() + dist,
            this.getZoneItem().getTileY() + dist
         );
         this.setCachedQL(ql);
      }
   }

   @Override
   protected float getCurrentQL() {
      if (this.getZoneItem() == null) {
         return 0.0F;
      } else {
         return this.getZoneItem().isEnchantedTurret() && !this.getZoneItem().isPlanted() ? 0.0F : this.getZoneItem().getCurrentQualityLevel();
      }
   }
}

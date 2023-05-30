package com.wurmonline.server.structures;

import com.wurmonline.mesh.Tiles;
import com.wurmonline.server.Items;
import com.wurmonline.server.items.Item;
import com.wurmonline.shared.constants.StructureConstantsEnum;
import com.wurmonline.shared.constants.StructureStateEnum;
import java.io.IOException;

public final class TempFence extends Fence {
   private Item fenceItem;

   public TempFence(
      StructureConstantsEnum aType, int aTileX, int aTileY, int aHeightOffset, Item item, Tiles.TileBorderDirection aDir, int aZoneId, int aLayer
   ) {
      super(aType, aTileX, aTileY, aHeightOffset, item.getQualityLevel(), aDir, aZoneId, aLayer);
      this.fenceItem = item;
      this.state = StructureStateEnum.FINISHED;
   }

   @Override
   public void setZoneId(int zid) {
      this.zoneId = zid;
   }

   @Override
   public void save() throws IOException {
   }

   @Override
   void load() throws IOException {
   }

   @Override
   public float getQualityLevel() {
      return this.fenceItem.getQualityLevel();
   }

   @Override
   public float getOriginalQualityLevel() {
      return this.fenceItem.getOriginalQualityLevel();
   }

   @Override
   public float getDamage() {
      return this.fenceItem.getDamage();
   }

   @Override
   public boolean setDamage(float newDam) {
      return this.fenceItem.setDamage(this.fenceItem.getDamage() + newDam);
   }

   @Override
   public boolean isTemporary() {
      return true;
   }

   @Override
   public boolean setQualityLevel(float newQl) {
      return this.fenceItem.setQualityLevel(newQl);
   }

   @Override
   public void improveOrigQualityLevel(float newQl) {
      this.fenceItem.setOriginalQualityLevel(newQl);
   }

   @Override
   public void delete() {
      Items.destroyItem(this.fenceItem.getWurmId());
   }

   @Override
   public void setLastUsed(long aLastUsed) {
      this.fenceItem.setLastMaintained(aLastUsed);
   }

   @Override
   public final long getTempId() {
      return this.fenceItem.getWurmId();
   }

   @Override
   public void savePermissions() {
   }

   @Override
   boolean changeColor(int aNewcolor) {
      return false;
   }
}

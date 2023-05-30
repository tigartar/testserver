package com.wurmonline.server.structures;

import com.wurmonline.server.items.Item;
import com.wurmonline.server.zones.VolaTile;

public interface IFloor extends StructureSupport {
   float getDamageModifierForItem(Item var1);

   long getStructureId();

   VolaTile getTile();

   boolean isOnPvPServer();

   int getTileX();

   int getTileY();

   float getCurrentQualityLevel();

   float getDamage();

   boolean setDamage(float var1);

   float getQualityLevel();

   void destroyOrRevertToPlan();

   boolean isAPlan();

   boolean isThatch();

   boolean isStone();

   boolean isSandstone();

   boolean isSlate();

   boolean isMarble();

   boolean isMetal();

   boolean isWood();

   boolean isFinished();

   int getRepairItemTemplate();

   boolean setQualityLevel(float var1);

   void setLastUsed(long var1);

   boolean isOnSurface();

   @Override
   boolean equals(StructureSupport var1);
}

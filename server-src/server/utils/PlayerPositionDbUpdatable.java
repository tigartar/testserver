package com.wurmonline.server.utils;

public final class PlayerPositionDbUpdatable implements WurmDbUpdatable {
   static final String SAVE_PLAYER_POSITION = "update POSITION set POSX=?, POSY=?, POSZ=?, ROTATION=?,ZONEID=?,LAYER=?,ONBRIDGE=? where WURMID=?";
   private final long id;
   private final float positionX;
   private final float positionY;
   private final float positionZ;
   private final float rotation;
   private final int zoneid;
   private final int layer;
   private final long bridgeId;

   public PlayerPositionDbUpdatable(long aId, float aPositionX, float aPositionY, float aPositionZ, float aRotation, int aZoneid, int aLayer, long bridgeid) {
      this.id = aId;
      this.positionX = aPositionX;
      this.positionY = aPositionY;
      this.positionZ = aPositionZ;
      this.rotation = aRotation;
      this.zoneid = aZoneid;
      this.layer = aLayer;
      this.bridgeId = bridgeid;
   }

   @Override
   public String getDatabaseUpdateStatement() {
      return "update POSITION set POSX=?, POSY=?, POSZ=?, ROTATION=?,ZONEID=?,LAYER=?,ONBRIDGE=? where WURMID=?";
   }

   long getId() {
      return this.id;
   }

   float getPositionX() {
      return this.positionX;
   }

   float getPositionY() {
      return this.positionY;
   }

   float getPositionZ() {
      return this.positionZ;
   }

   float getRotation() {
      return this.rotation;
   }

   int getZoneid() {
      return this.zoneid;
   }

   int getLayer() {
      return this.layer;
   }

   public long getBridgeId() {
      return this.bridgeId;
   }

   @Override
   public String toString() {
      return "PlayerPositionDbUpdatable [id="
         + this.id
         + ", positionX="
         + this.positionX
         + ", positionY="
         + this.positionY
         + ", positionZ="
         + this.positionZ
         + ", rotation="
         + this.rotation
         + ", zoneid="
         + this.zoneid
         + ", layer="
         + this.layer
         + ", bridge="
         + this.bridgeId
         + "]";
   }
}

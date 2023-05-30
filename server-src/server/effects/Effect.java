package com.wurmonline.server.effects;

import com.wurmonline.server.zones.NoSuchZoneException;
import com.wurmonline.server.zones.VolaTile;
import com.wurmonline.server.zones.Zone;
import com.wurmonline.server.zones.Zones;
import java.io.IOException;
import java.io.Serializable;
import java.util.logging.Level;
import java.util.logging.Logger;

public abstract class Effect implements Serializable {
   private static final long serialVersionUID = -7768268294902679751L;
   private long owner;
   private short type;
   private float posX;
   private float posY;
   private float posZ;
   private boolean surfaced;
   private int id = 0;
   private static final Logger logger = Logger.getLogger(Effect.class.getName());
   private long startTime;
   private String effectString;
   private float timeout = -1.0F;
   private float rotationOffset = 0.0F;

   Effect(long aOwner, short aType, float aPosX, float aPosY, float aPosZ, boolean aSurfaced) {
      this.owner = aOwner;
      this.type = aType;
      this.posX = aPosX;
      this.posY = aPosY;
      this.posZ = aPosZ;
      this.surfaced = aSurfaced;
      this.startTime = System.currentTimeMillis();

      try {
         this.save();
      } catch (IOException var9) {
         logger.log(Level.WARNING, "Failed to save effect", (Throwable)var9);
      }
   }

   Effect(int num, long ownerid, short typ, float posx, float posy, float posz, long stime) {
      this.id = num;
      this.owner = ownerid;
      this.type = typ;
      this.posX = posx;
      this.posY = posy;
      this.posZ = posz;
      this.startTime = stime;
   }

   Effect(long aOwner, int aNumber) throws IOException {
      this.owner = aOwner;
      this.id = aNumber;
      this.load();
   }

   public final long getStartTime() {
      return this.startTime;
   }

   final void setStartTime(long aStartTime) {
      this.startTime = aStartTime;
   }

   public final int getId() {
      return this.id;
   }

   final void setId(int aId) {
      this.id = aId;
   }

   public final int getTileX() {
      return (int)this.posX >> 2;
   }

   public final int getTileY() {
      return (int)this.posY >> 2;
   }

   public final float getPosX() {
      return this.posX;
   }

   public final void setPosX(float positionX) {
      this.posX = positionX;
   }

   public final void setPosY(float positionY) {
      this.posY = positionY;
   }

   public final float getPosY() {
      return this.posY;
   }

   public final float getPosZ() {
      return this.posZ;
   }

   public final void setPosZ(float aPosZ) {
      this.posZ = aPosZ;
   }

   public final float calculatePosZ(VolaTile tile) {
      return Zones.calculatePosZ(this.getPosX(), this.getPosY(), tile, this.isOnSurface(), false, this.getPosZ(), null, -10L);
   }

   public final long getOwner() {
      return this.owner;
   }

   final void setOwner(long aOwner) {
      this.owner = aOwner;
   }

   public final short getType() {
      return this.type;
   }

   public final boolean isGlobal() {
      return this.type == 2 || this.type == 3 || this.type == 16 || this.type == 19 || this.type == 4 || this.type == 25;
   }

   final void setType(short aType) {
      this.type = aType;
   }

   public final void setSurfaced(boolean aSurfaced) {
      this.surfaced = aSurfaced;
   }

   public final boolean isOnSurface() {
      return this.surfaced;
   }

   public final byte getLayer() {
      return (byte)(this.surfaced ? 0 : -1);
   }

   public final void setEffectString(String effString) {
      this.effectString = effString;
   }

   public final String getEffectString() {
      return this.effectString;
   }

   public final void setTimeout(float timeout) {
      this.timeout = timeout;
   }

   public final float getTimeout() {
      return this.timeout;
   }

   public final void setRotationOffset(float rotationOffset) {
      this.rotationOffset = rotationOffset;
   }

   public final float getRotationOffset() {
      return this.rotationOffset;
   }

   public void setPosXYZ(float posX, float posY, float posZ, boolean sendUpdate) {
      if (posX != this.getPosX() || posY != this.getPosY() || posZ != this.getPosZ()) {
         try {
            if (sendUpdate) {
               Zone zone = Zones.getZone((int)(this.getPosX() / 4.0F), (int)(this.getPosY() / 4.0F), this.isOnSurface());
               zone.removeEffect(this);
            }

            this.setPosX(posX);
            this.setPosY(posY);
            this.setPosZ(posZ);
            if (sendUpdate) {
               Zone zone = Zones.getZone((int)(this.getPosX() / 4.0F), (int)(this.getPosY() / 4.0F), this.isOnSurface());
               zone.addEffect(this, false);
            }
         } catch (NoSuchZoneException var6) {
         }
      }
   }

   public abstract void save() throws IOException;

   abstract void load() throws IOException;

   abstract void delete();
}

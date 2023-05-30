package com.wurmonline.server.behaviours;

import com.wurmonline.server.MiscConstants;
import com.wurmonline.server.NoSuchPlayerException;
import com.wurmonline.server.Server;
import com.wurmonline.server.creatures.Creature;
import com.wurmonline.server.creatures.NoSuchCreatureException;
import com.wurmonline.server.utils.StringUtil;
import java.util.logging.Logger;

public final class Seat implements MiscConstants {
   private static Logger logger = Logger.getLogger(Seat.class.getName());
   public static final byte TYPE_DRIVER = 0;
   public static final byte TYPE_PASSENGER = 1;
   public static final byte TYPE_HITCHED = 2;
   public final byte type;
   public long occupant = -10L;
   public float offx = 0.0F;
   public float offy = 0.0F;
   public float offz = 0.0F;
   private float altOffz = 0.0F;
   public float cover = 0.5F;
   public float manouvre = 0.5F;
   private static int allids = 0;
   int id = 0;

   Seat(byte _type) {
      this.id = allids++;
      this.type = _type;
   }

   public boolean isOccupied() {
      return this.occupant != -10L;
   }

   public boolean occupy(Vehicle vehicle, Creature creature) {
      if (this.occupant == -10L) {
         if (creature == null) {
            logger.warning("A null Creature cannot occupy a seat (" + this + ") in a Vehicle (" + vehicle + ')');
            return false;
         } else {
            this.occupant = creature.getWurmId();
            String vehicleName = Vehicle.getVehicleName(vehicle);
            if (this.type == 0) {
               creature.getCommunicator()
                  .sendNormalServerMessage(StringUtil.format("You %s on the %s as the %s.", vehicle.embarkString, vehicleName, vehicle.pilotName));
               Server.getInstance()
                  .broadCastAction(
                     StringUtil.format("%s %s on the %s as the %s.", creature.getName(), vehicle.embarksString, vehicleName, vehicle.pilotName), creature, 5
                  );
            } else if (!vehicle.isChair() && !vehicle.isBed()) {
               creature.getCommunicator().sendNormalServerMessage(StringUtil.format("You %s on the %s as a passenger.", vehicle.embarkString, vehicleName));
               Server.getInstance()
                  .broadCastAction(StringUtil.format("%s %s on the %s as a passenger.", creature.getName(), vehicle.embarksString, vehicleName), creature, 5);
            } else {
               creature.getCommunicator().sendNormalServerMessage(StringUtil.format("You %s on the %s.", vehicle.embarkString, vehicleName));
               Server.getInstance()
                  .broadCastAction(StringUtil.format("%s %s on the %s.", creature.getName(), vehicle.embarksString, vehicleName), creature, 5);
            }

            return true;
         }
      } else {
         return false;
      }
   }

   boolean leave(Vehicle vehicle) {
      if (this.occupant != -10L) {
         try {
            Creature cret = Server.getInstance().getCreature(this.occupant);
            cret.disembark(true);
         } catch (NoSuchPlayerException var3) {
         } catch (NoSuchCreatureException var4) {
         }

         this.occupant = -10L;
         return true;
      } else {
         return false;
      }
   }

   public float getCover() {
      return this.cover;
   }

   public void setCover(float aCover) {
      this.cover = aCover;
   }

   public float getManouvre() {
      return this.manouvre;
   }

   public void setManouvre(float aManouvre) {
      this.manouvre = aManouvre;
   }

   public int getId() {
      return this.id;
   }

   public void setId(int aId) {
      this.id = aId;
   }

   public byte getType() {
      return this.type;
   }

   public long getOccupant() {
      return this.occupant;
   }

   void setOccupant(long aOccupant) {
      this.occupant = aOccupant;
   }

   float getOffx() {
      return this.offx;
   }

   void setOffx(float aOffx) {
      this.offx = aOffx;
   }

   float getOffy() {
      return this.offy;
   }

   void setOffy(float aOffy) {
      this.offy = aOffy;
   }

   float getOffz() {
      return this.offz;
   }

   void setOffz(float aOffz) {
      this.offz = aOffz;
   }

   @Override
   public String toString() {
      StringBuilder lBuilder = new StringBuilder(200);
      lBuilder.append("Seat [Type: ").append(this.type);
      lBuilder.append(", Occupant: ").append(this.occupant);
      lBuilder.append(", OffsetX: ").append(this.offx);
      lBuilder.append(", OffsetY: ").append(this.offy);
      lBuilder.append(", OffsetZ: ").append(this.offz);
      lBuilder.append(']');
      return lBuilder.toString();
   }

   public float getAltOffz() {
      return this.altOffz;
   }

   public void setAltOffz(float aAltOffz) {
      this.altOffz = aAltOffz;
   }
}

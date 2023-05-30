package com.wurmonline.server.behaviours;

import com.wurmonline.mesh.Tiles;
import com.wurmonline.server.Items;
import com.wurmonline.server.MiscConstants;
import com.wurmonline.server.NoSuchItemException;
import com.wurmonline.server.NoSuchPlayerException;
import com.wurmonline.server.Players;
import com.wurmonline.server.Server;
import com.wurmonline.server.ServerEntry;
import com.wurmonline.server.Servers;
import com.wurmonline.server.TimeConstants;
import com.wurmonline.server.creatures.Creature;
import com.wurmonline.server.creatures.Creatures;
import com.wurmonline.server.creatures.NoSuchCreatureException;
import com.wurmonline.server.items.Item;
import com.wurmonline.server.items.RuneUtilities;
import com.wurmonline.server.kingdom.Kingdoms;
import com.wurmonline.server.players.Achievements;
import com.wurmonline.server.players.Player;
import com.wurmonline.server.players.PlayerInfo;
import com.wurmonline.server.players.PlayerInfoFactory;
import com.wurmonline.server.questions.QuestionParser;
import com.wurmonline.server.spells.Cooldowns;
import com.wurmonline.server.spells.RiteEvent;
import com.wurmonline.server.structures.Blocker;
import com.wurmonline.server.structures.Blocking;
import com.wurmonline.server.structures.BlockingResult;
import com.wurmonline.server.utils.StringUtil;
import com.wurmonline.server.zones.Zones;
import com.wurmonline.shared.constants.ProtoConstants;
import java.util.HashSet;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

public final class Vehicle implements MiscConstants, ProtoConstants, TimeConstants {
   private static final Logger logger = Logger.getLogger(Vehicle.class.getName());
   static final Seat[] EMPTYSEATS = new Seat[0];
   public Seat[] seats = EMPTYSEATS;
   public Seat[] hitched = EMPTYSEATS;
   private float maxSpeed = 1.0F;
   private byte windImpact = 0;
   public boolean creature = false;
   String pilotName = "driver";
   public long pilotId = -10L;
   String embarkString = "embark";
   String embarksString = "embarks";
   public String name = "vehicle";
   public final long wurmid;
   public float maxDepth = -2500.0F;
   public float maxHeight = 2500.0F;
   public float maxHeightDiff = 2000.0F;
   public float skillNeeded = 20.1F;
   private int maxAllowedLoadDistance = 4;
   private boolean unmountable = false;
   private byte maxPassengers = 0;
   public Set<Creature> draggers = null;
   private boolean chair = false;
   private boolean bed = false;
   public byte commandType = 0;
   boolean canHaveEquipment = false;
   private ServerEntry destinationServer;
   public static final long plotCoursePvPCooldown = 1800000L;

   Vehicle(long aWurmId) {
      this.wurmid = aWurmId;
   }

   public boolean addDragger(Creature aCreature) {
      if (this.hitched.length > 0) {
         if (this.draggers == null) {
            this.draggers = new HashSet<>();
         }

         if (this.draggers.size() < this.hitched.length) {
            if (this.draggers.add(aCreature)) {
               for(int x = 0; x < this.hitched.length; ++x) {
                  if (this.hitched[x].occupant == -10L) {
                     this.hitched[x].setOccupant(aCreature.getWurmId());
                     if (this.getPilotId() > -10L) {
                        try {
                           Creature c = Server.getInstance().getCreature(this.getPilotId());
                           c.getMovementScheme().addMountSpeed((short)this.calculateNewVehicleSpeed(true));
                        } catch (Exception var4) {
                        }
                     }

                     return true;
                  }
               }

               logger.log(Level.WARNING, "error when adding to hitched seat - no free space.");
               this.draggers.remove(aCreature);
            }
         } else {
            logger.log(Level.WARNING, "draggers.size=" + this.draggers.size() + ", hitched.length=" + this.hitched.length + " - no space");
         }
      }

      return false;
   }

   public void purgeDraggers() {
      if (this.draggers != null) {
         for(Creature dragger : this.draggers) {
            for(int x = 0; x < this.hitched.length; ++x) {
               if (this.hitched[x].occupant == dragger.getWurmId()) {
                  this.hitched[x].setOccupant(-10L);
                  break;
               }
            }

            dragger.setHitched(null, false);
            Server.getInstance()
               .broadCastMessage(
                  dragger.getName() + " stops dragging a " + this.getName() + ".", dragger.getTileX(), dragger.getTileY(), dragger.isOnSurface(), 5
               );
         }

         if (this.getPilotId() > -10L) {
            try {
               Creature c = Server.getInstance().getCreature(this.getPilotId());
               c.getMovementScheme().addMountSpeed((short)this.calculateNewVehicleSpeed(true));
            } catch (Exception var4) {
            }
         }
      }
   }

   public boolean removeDragger(Creature aCreature) {
      if (this.hitched.length > 0 && this.draggers != null && this.draggers.remove(aCreature)) {
         for(int x = 0; x < this.hitched.length; ++x) {
            if (this.hitched[x].occupant == aCreature.getWurmId()) {
               this.hitched[x].setOccupant(-10L);
               break;
            }
         }

         aCreature.setHitched(null, false);
         String hitchedType = "stop dragging";
         if (!this.creature) {
            try {
               Item dragged = Items.getItem(this.getWurmid());
               if (dragged.isTent()) {
                  hitchedType = "is no longer hitched to";
               }
            } catch (NoSuchItemException var5) {
            }
         }

         Server.getInstance()
            .broadCastMessage(
               aCreature.getName() + " " + hitchedType + " a " + this.getName() + ".", aCreature.getTileX(), aCreature.getTileY(), aCreature.isOnSurface(), 5
            );
         if (this.getPilotId() > -10L) {
            try {
               Creature c = Server.getInstance().getCreature(this.getPilotId());
               c.getMovementScheme().addMountSpeed((short)this.calculateNewVehicleSpeed(true));
            } catch (Exception var4) {
            }
         }

         return true;
      } else {
         return false;
      }
   }

   public void updateDraggedSpeed(boolean hitching) {
      if (this.hitched.length > 0 && this.draggers != null && this.getPilotId() > -10L) {
         try {
            Creature c = Server.getInstance().getCreature(this.getPilotId());
            c.getMovementScheme().addMountSpeed((short)this.calculateNewVehicleSpeed(hitching));
         } catch (Exception var3) {
         }
      }
   }

   public Seat[] getHitched() {
      return this.hitched;
   }

   public void setHitched(Seat[] aHitched) {
      this.hitched = aHitched;
   }

   public float getMaxDepth() {
      return this.maxDepth;
   }

   public void setMaxDepth(float aMaxDepth) {
      this.maxDepth = aMaxDepth;
   }

   public float getMaxHeight() {
      return this.maxHeight;
   }

   public void setMaxHeight(float aMaxHeight) {
      this.maxHeight = aMaxHeight;
   }

   public float getMaxHeightDiff() {
      return this.maxHeightDiff;
   }

   public void setMaxHeightDiff(float aMaxHeightDiff) {
      this.maxHeightDiff = aMaxHeightDiff;
   }

   public float getSkillNeeded() {
      return this.skillNeeded;
   }

   public boolean getCanHaveEquipment() {
      return this.canHaveEquipment;
   }

   public void setSkillNeeded(float aSkillNeeded) {
      this.skillNeeded = aSkillNeeded;
   }

   public Set<Creature> getDraggers() {
      return this.draggers;
   }

   public boolean isDragger(Creature aCreature) {
      return this.hitched.length > 0 && this.draggers != null ? this.draggers.contains(aCreature) : false;
   }

   public boolean hasHumanDragger() {
      if (this.draggers != null) {
         for(Creature dragger : this.draggers) {
            if (dragger.isPlayer()) {
               return true;
            }
         }
      }

      return false;
   }

   public boolean mayAddDragger() {
      return this.hitched.length > 0 && (this.draggers == null || this.draggers.size() < this.hitched.length);
   }

   public void addHitchSeats(Seat[] hitchSeats) {
      if (hitchSeats == null) {
         this.hitched = EMPTYSEATS;
      } else {
         this.hitched = hitchSeats;
      }
   }

   void createPassengerSeats(int aNumber) {
      this.maxPassengers = (byte)aNumber;
      if (aNumber >= 0) {
         this.seats = new Seat[aNumber + 1];
         this.seats[0] = new Seat((byte)0);

         for(int x = 1; x <= aNumber; ++x) {
            this.seats[x] = new Seat((byte)1);
         }
      } else {
         logger.warning("Can only create a positive number of seats not " + aNumber);
      }
   }

   void createOnlyPassengerSeats(int aNumber) {
      if (aNumber >= 0) {
         this.seats = new Seat[aNumber];

         for(int x = 0; x < aNumber; ++x) {
            this.seats[x] = new Seat((byte)1);
         }
      } else {
         logger.warning("Can only create a positive number of seats not " + aNumber);
      }
   }

   public byte getMaxPassengers() {
      return this.maxPassengers;
   }

   public boolean setSeatOffset(int aNumber, float aOffx, float aOffy, float aOffz) {
      if (aNumber <= this.seats.length - 1 && aNumber >= 0) {
         this.seats[aNumber].offx = aOffx;
         this.seats[aNumber].offy = aOffy;
         this.seats[aNumber].offz = aOffz;
         return true;
      } else {
         return false;
      }
   }

   public boolean setSeatOffset(int aNumber, float aOffx, float aOffy, float aOffz, float aAltOffz) {
      if (aNumber <= this.seats.length - 1 && aNumber >= 0) {
         this.seats[aNumber].offx = aOffx;
         this.seats[aNumber].offy = aOffy;
         this.seats[aNumber].offz = aOffz;
         this.seats[aNumber].setAltOffz(aAltOffz);
         return true;
      } else {
         return false;
      }
   }

   public boolean setSeatFightMod(int aNumber, float aCover, float aManouvre) {
      if (aNumber <= this.seats.length - 1 && aNumber >= 0) {
         this.seats[aNumber].cover = aCover;
         this.seats[aNumber].manouvre = aManouvre;
         return true;
      } else {
         return false;
      }
   }

   public Seat getPilotSeat() {
      return this.seats.length != 0 && this.seats[0].type == 0 ? this.seats[0] : null;
   }

   public Seat getSeatFor(long aCreatureId) {
      for(int x = 0; x < this.seats.length; ++x) {
         if (this.seats[x].occupant == aCreatureId) {
            return this.seats[x];
         }
      }

      return null;
   }

   public final int getSeatNumberFor(Seat seat) {
      for(int i = 0; i < this.seats.length; ++i) {
         if (this.seats[i].getId() == seat.getId()) {
            return i;
         }
      }

      return -1;
   }

   public Seat getHitchSeatFor(long aCreatureId) {
      for(int x = 0; x < this.hitched.length; ++x) {
         if (this.hitched[x].occupant == aCreatureId) {
            return this.hitched[x];
         }
      }

      return null;
   }

   public void kickAll() {
      for(int x = 0; x < this.seats.length; ++x) {
         this.seats[x].leave(this);
      }

      this.pilotId = -10L;
      this.pilotName = "";
   }

   public Seat[] getSeats() {
      return this.seats;
   }

   void setSeats(Seat[] aSeats) {
      this.seats = aSeats;
   }

   public final boolean isAnySeatOccupied() {
      return this.isAnySeatOccupied(true);
   }

   public final boolean isAnySeatOccupied(boolean countOffline) {
      if (this.seats != null) {
         for(int i = 0; i < this.seats.length; ++i) {
            if (this.seats[i].isOccupied()) {
               if (countOffline) {
                  return true;
               }

               try {
                  long occupantId = this.seats[i].getOccupant();
                  Player p = Players.getInstance().getPlayer(occupantId);
                  if (p.isOffline()) {
                  }
               } catch (NoSuchPlayerException var6) {
               }
            }
         }
      }

      return false;
   }

   public final boolean isAnythingHitched() {
      if (this.hitched != null) {
         for(int i = 0; i < this.hitched.length; ++i) {
            if (this.hitched[i].isOccupied()) {
               return true;
            }
         }
      }

      return false;
   }

   public byte calculateNewBoatSpeed(boolean disembarking) {
      int numsOccupied = 0;
      float qlMod = 0.0F;

      for(int x = 0; x < this.seats.length; ++x) {
         if (this.seats[x].isOccupied()) {
            ++numsOccupied;

            try {
               long occupantId = this.seats[x].getOccupant();
               Player p = Players.getInstance().getPlayer(occupantId);
               if (p.isOffline()) {
                  --numsOccupied;
               }
            } catch (NoSuchPlayerException var8) {
               --numsOccupied;
            }
         }
      }

      try {
         Item itemVehicle = Items.getItem(this.wurmid);
         numsOccupied = Math.min(this.seats.length, numsOccupied + itemVehicle.getRarity());
         qlMod = Math.max(0.0F, itemVehicle.getCurrentQualityLevel() - 10.0F) / 90.0F;
         if (qlMod > 0.0F) {
            ++qlMod;
         }
      } catch (NoSuchItemException var9) {
         return 0;
      }

      if (disembarking) {
         --numsOccupied;
      }

      float percentOccupied = 1.0F;
      percentOccupied = 1.0F + (float)numsOccupied / (float)this.seats.length;
      float maxSpeed = this.getMaxSpeed();
      if (RiteEvent.isActive(403)) {
         maxSpeed *= 2.0F;
      }

      return Servers.localServer.PVPSERVER
         ? (byte)((int)Math.min(127.0F, percentOccupied * 9.0F * maxSpeed + qlMod * 3.0F * maxSpeed))
         : (byte)((int)Math.min(127.0F, percentOccupied * 3.0F * maxSpeed + qlMod * 9.0F * maxSpeed));
   }

   private final int getMinimumDraggers(Item vehicleItem) {
      if (vehicleItem == null) {
         return 0;
      } else if (vehicleItem.getTemplateId() == 850) {
         return 2;
      } else {
         return !vehicleItem.isBoat() ? 1 : 0;
      }
   }

   public byte calculateNewVehicleSpeed(boolean hitching) {
      if (this.isChair()) {
         return 0;
      } else if (this.hitched.length <= 0) {
         return (byte)((int)Math.min(127.0F, 10.0F * this.getMaxSpeed()));
      } else {
         boolean isWagon = false;
         int bisonCount = 0;
         if (this.draggers == null) {
            return 0;
         } else {
            double strength = 0.0;

            try {
               Item itemVehicle = Items.getItem(this.wurmid);
               strength = (double)((float)itemVehicle.getRarity() * 0.1F);
               if (this.getDraggers().size() < this.getMinimumDraggers(itemVehicle)) {
                  return 0;
               }

               if (itemVehicle.getTemplateId() == 850) {
                  isWagon = true;
               }
            } catch (NoSuchItemException var8) {
               return 0;
            }

            for(Creature next : this.draggers) {
               if (isWagon && next.getTemplate().getTemplateId() == 82) {
                  ++bisonCount;
               }

               strength += next.getStrengthSkill() / (double)(this.hitched.length * 10) * (double)next.getMountSpeedPercent(hitching);
            }

            return (byte)((int)Math.min(127.0, 10.0 * strength * (double)this.getMaxSpeed() + (double)((float)(1 * bisonCount) * this.getMaxSpeed())));
         }
      }
   }

   public byte calculateNewMountSpeed(Creature mount, boolean mounting) {
      double strength = (double)mount.getMountSpeedPercent(mounting);
      if (mount.getTemplateId() == 64 && strength * (double)this.getMaxSpeed() >= 42.0 && this.getPilotId() != -10L) {
         Achievements.triggerAchievement(this.getPilotId(), 584);
      }

      return (byte)((int)Math.max(0.0, Math.min(127.0, strength * (double)this.getMaxSpeed())));
   }

   float getMaxSpeed() {
      try {
         Item itemVehicle = Items.getItem(this.wurmid);
         if (itemVehicle != null && itemVehicle.getSpellEffects() != null) {
            float modifier = itemVehicle.getSpellEffects().getRuneEffect(RuneUtilities.ModifierEffect.ENCH_VEHCSPEED);
            return this.maxSpeed * modifier;
         }
      } catch (NoSuchItemException var3) {
         return this.maxSpeed;
      }

      return this.maxSpeed;
   }

   void setMaxSpeed(float aMaxSpeed) {
      this.maxSpeed = aMaxSpeed;
   }

   public byte getWindImpact() {
      float modifier = 1.0F;

      try {
         Item itemVehicle = Items.getItem(this.wurmid);
         if (itemVehicle != null && itemVehicle.getSpellEffects() != null) {
            modifier = itemVehicle.getSpellEffects().getRuneEffect(RuneUtilities.ModifierEffect.ENCH_WIND);
         }
      } catch (NoSuchItemException var3) {
      }

      return (byte)((int)Math.min(127.0F, (float)this.windImpact * modifier));
   }

   void setWindImpact(byte impact) {
      this.windImpact = (byte)Math.min(127, impact);
   }

   public boolean isCreature() {
      return this.creature;
   }

   void setCreature(boolean aCreature) {
      this.creature = aCreature;
   }

   String getPilotName() {
      return this.pilotName;
   }

   void setPilotName(String aPilotName) {
      this.pilotName = aPilotName;
   }

   public long getPilotId() {
      return this.pilotId;
   }

   void setPilotId(long aPilotId) {
      this.pilotId = aPilotId;
   }

   String getEmbarkString() {
      return this.embarkString;
   }

   void setEmbarkString(String aEmbarkString) {
      this.embarkString = aEmbarkString;
   }

   String getName() {
      return this.name;
   }

   public static final String getVehicleName(Vehicle vehicle) {
      if (vehicle.isCreature()) {
         try {
            Creature mount = Creatures.getInstance().getCreature(vehicle.getWurmid());
            return mount.getName();
         } catch (NoSuchCreatureException var3) {
            logger.log(Level.WARNING, StringUtil.format("Unable to find creature with id: %d.", vehicle.getWurmid()), (Throwable)var3);
         }
      }

      if (vehicle.isChair()) {
         try {
            Item chair = Items.getItem(vehicle.getWurmid());
            return chair.getName();
         } catch (NoSuchItemException var2) {
            logger.log(Level.WARNING, StringUtil.format("Unable to find item with id: %d.", vehicle.getWurmid()), (Throwable)var2);
         }
      }

      return vehicle.getName();
   }

   void setName(String aName) {
      this.name = aName;
   }

   long getWurmid() {
      return this.wurmid;
   }

   @Override
   public String toString() {
      StringBuilder lBuilder = new StringBuilder(200);
      lBuilder.append("Vehicle [id: ").append(this.wurmid);
      lBuilder.append(", Name: ").append(this.name);
      lBuilder.append(", PilotId: ").append(this.pilotId);
      lBuilder.append(", PilotName: ").append(this.pilotName);
      lBuilder.append(", MaxSpeed: ").append(this.getMaxSpeed());
      lBuilder.append(", EmbarkString: ").append(this.embarkString);
      lBuilder.append(", Creature: ").append(this.creature);
      lBuilder.append(']');
      return lBuilder.toString();
   }

   public int getSeatPosForPassenger(long _wurmid) {
      for(int x = 0; x < this.seats.length; ++x) {
         if (this.seats[x].occupant == _wurmid) {
            return x;
         }
      }

      return -1;
   }

   public final int getFloorLevel() {
      if (this.creature) {
         try {
            return Server.getInstance().getCreature(this.wurmid).getFloorLevel();
         } catch (Exception var2) {
            return 0;
         }
      } else {
         try {
            return Items.getItem(this.wurmid).getFloorLevel();
         } catch (NoSuchItemException var3) {
            return 0;
         }
      }
   }

   public final float getPosZ() {
      if (this.creature) {
         try {
            return Server.getInstance().getCreature(this.wurmid).getPositionZ();
         } catch (Exception var2) {
            return 0.0F;
         }
      } else {
         try {
            return Items.getItem(this.wurmid).getPosZ();
         } catch (NoSuchItemException var3) {
            return 0.0F;
         }
      }
   }

   public boolean positionDragger(Creature dragger, Creature performer) {
      Item itemVehicle = null;

      try {
         itemVehicle = Items.getItem(this.wurmid);
      } catch (NoSuchItemException var24) {
         return false;
      }

      for(int x = 0; x < this.hitched.length; ++x) {
         if (this.hitched[x].type == 2 && this.hitched[x].getOccupant() == dragger.getWurmId()) {
            float r = (-itemVehicle.getRotation() + 180.0F) * (float) Math.PI / 180.0F;
            float s = (float)Math.sin((double)r);
            float c = (float)Math.cos((double)r);
            Seat pilotSeat = this.getPilotSeat();
            float xo2 = pilotSeat == null ? 0.0F : s * -pilotSeat.offx - c * -pilotSeat.offy;
            float yo2 = pilotSeat == null ? 0.0F : c * -pilotSeat.offx + s * -pilotSeat.offy;
            float origposx = itemVehicle.getPosX() + xo2;
            float origposy = itemVehicle.getPosY() + yo2;
            origposx = Math.max(3.0F, origposx);
            origposx = Math.min(Zones.worldMeterSizeX - 3.0F, origposx);
            origposy = Math.max(3.0F, origposy);
            origposy = Math.min(Zones.worldMeterSizeY - 3.0F, origposy);
            float xo = s * -this.hitched[x].offx - c * -this.hitched[x].offy;
            float yo = c * -this.hitched[x].offx + s * -this.hitched[x].offy;
            float newposx = itemVehicle.getPosX() + xo;
            float newposy = itemVehicle.getPosY() + yo;
            if (itemVehicle.isTent()) {
               newposx = performer.getPosX();
               newposy = performer.getPosY();
            }

            newposx = Math.max(3.0F, newposx);
            newposx = Math.min(Zones.worldMeterSizeX - 3.0F, newposx);
            newposy = Math.max(3.0F, newposy);
            newposy = Math.min(Zones.worldMeterSizeY - 3.0F, newposy);
            int diffx = ((int)newposx >> 2) - ((int)origposx >> 2);
            int diffy = ((int)newposy >> 2) - ((int)origposy >> 2);
            boolean move = true;
            if (!itemVehicle.isTent() && (diffy != 0 || diffx != 0)) {
               BlockingResult result = Blocking.getBlockerBetween(
                  dragger,
                  origposx,
                  origposy,
                  newposx,
                  newposy,
                  dragger.getPositionZ(),
                  dragger.getPositionZ(),
                  dragger.getLayer() >= 0,
                  dragger.getLayer() >= 0,
                  false,
                  6,
                  -1L,
                  itemVehicle.getBridgeId(),
                  itemVehicle.getBridgeId(),
                  false
               );
               if (result != null) {
                  Blocker first = result.getFirstBlocker();
                  if (!first.isDoor() || !first.canBeOpenedBy(dragger, false)) {
                     move = false;
                  }
               }

               if (move && dragger.getLayer() < 0 && Tiles.isSolidCave(Tiles.decodeType(Server.caveMesh.getTile((int)newposx >> 2, (int)newposy >> 2)))) {
                  move = false;
               }
            }

            if (!move) {
               newposx = origposx;
               newposy = origposy;
            }

            try {
               Zones.getZone(dragger.getCurrentTile().tilex, dragger.getCurrentTile().tiley, dragger.isOnSurface()).removeCreature(dragger, true, false);
            } catch (Exception var23) {
               logger.log(Level.WARNING, dragger.getWurmId() + "," + var23.getMessage(), (Throwable)var23);
            }

            dragger.getStatus().setPositionX(newposx);
            dragger.getStatus().setPositionY(newposy);
            dragger.setBridgeId(itemVehicle.getBridgeId());
            float z = Zones.calculatePosZ(
               newposx,
               newposy,
               Zones.getTileOrNull(dragger.getTilePos(), dragger.isOnSurface()),
               dragger.isOnSurface(),
               false,
               dragger.getStatus().getPositionZ(),
               dragger,
               dragger.getBridgeId()
            );
            dragger.getMovementScheme()
               .setPosition(dragger.getStatus().getPositionX(), dragger.getStatus().getPositionY(), z, dragger.getStatus().getRotation(), dragger.getLayer());
            dragger.destroyVisionArea();

            try {
               Zones.getZone(dragger.getTileX(), dragger.getTileY(), dragger.isOnSurface()).addCreature(dragger.getWurmId());
               dragger.createVisionArea();
            } catch (Exception var22) {
               logger.log(Level.WARNING, dragger.getWurmId() + "," + var22.getMessage(), (Throwable)var22);
            }

            return true;
         }
      }

      return false;
   }

   public boolean isUnmountable() {
      return this.unmountable;
   }

   public void setUnmountable(boolean aUnmountable) {
      this.unmountable = aUnmountable;
   }

   public boolean isChair() {
      return this.chair;
   }

   public void setChair(boolean isChair) {
      this.chair = isChair;
   }

   public boolean isBed() {
      return this.bed;
   }

   public void setBed(boolean isBed) {
      this.bed = isBed;
   }

   public int getMaxAllowedLoadDistance() {
      return this.maxAllowedLoadDistance;
   }

   public void setMaxAllowedLoadDistance(int newMaxDist) {
      this.maxAllowedLoadDistance = newMaxDist;
   }

   public ServerEntry getDestinationServer() {
      return this.destinationServer;
   }

   public boolean hasDestinationSet() {
      return this.destinationServer != null;
   }

   public void setDestination(ServerEntry entry) {
      this.destinationServer = entry;
   }

   public void clearDestination() {
      this.destinationServer = null;
   }

   public void alertPassengerOfEnemies(Creature performer, ServerEntry entry, boolean embarking) {
      if (entry.PVPSERVER && (!entry.EPIC || Server.getInstance().isPS()) || entry.isChaosServer()) {
         byte pKingdom = ((Player)performer).getSaveFile().getChaosKingdom() == 0 ? 4 : ((Player)performer).getSaveFile().getChaosKingdom();

         for(Seat lSeat : this.seats) {
            PlayerInfo oInfo = PlayerInfoFactory.getPlayerInfoWithWurmId(lSeat.getOccupant());
            if (oInfo != null) {
               byte oKingdom = oInfo.getChaosKingdom() == 0 ? 4 : oInfo.getChaosKingdom();
               if (oKingdom != pKingdom) {
                  performer.getCommunicator()
                     .sendAlertServerMessage("Warning: " + oInfo.getName() + " will be an enemy when you cross into " + entry.getName() + "!");
                  if (embarking) {
                     try {
                        Player oPlayer = Players.getInstance().getPlayer(oInfo.wurmId);
                        oPlayer.getCommunicator()
                           .sendAlertServerMessage("Warning: " + performer.getName() + " will be an enemy when you cross into " + entry.getName() + "!");
                     } catch (NoSuchPlayerException var12) {
                     }
                  }
               }
            }
         }
      }
   }

   public void alertAllPassengersOfEnemies(ServerEntry entry) {
      for(Seat lSeat : this.seats) {
         PlayerInfo oInfo = PlayerInfoFactory.getPlayerInfoWithWurmId(lSeat.getOccupant());
         if (oInfo != null) {
            try {
               Player oPlayer = Players.getInstance().getPlayer(oInfo.wurmId);
               this.alertPassengerOfEnemies(oPlayer, entry, false);
            } catch (NoSuchPlayerException var8) {
            }
         }
      }
   }

   public void notifyAllPassengers(String message, boolean includeDriver, boolean alert) {
      for(Seat lSeat : this.seats) {
         if (includeDriver || lSeat != this.getPilotSeat()) {
            PlayerInfo oInfo = PlayerInfoFactory.getPlayerInfoWithWurmId(lSeat.getOccupant());
            if (oInfo != null) {
               try {
                  Player oPlayer = Players.getInstance().getPlayer(oInfo.wurmId);
                  if (alert) {
                     oPlayer.getCommunicator().sendAlertServerMessage(message);
                  } else {
                     oPlayer.getCommunicator().sendNormalServerMessage(message);
                  }
               } catch (NoSuchPlayerException var10) {
               }
            }
         }
      }
   }

   public void alertPassengersOfKingdom(ServerEntry entry, boolean includeDriver) {
      for(Seat lSeat : this.seats) {
         if (includeDriver || lSeat != this.getPilotSeat()) {
            PlayerInfo oInfo = PlayerInfoFactory.getPlayerInfoWithWurmId(lSeat.getOccupant());
            if (oInfo != null) {
               byte oKingdom = oInfo.getChaosKingdom() == 0 ? 4 : oInfo.getChaosKingdom();

               try {
                  Player oPlayer = Players.getInstance().getPlayer(oInfo.wurmId);
                  if ((Server.getInstance().isPS() || !entry.isChaosServer()) && (!entry.PVPSERVER || Servers.localServer.PVPSERVER)) {
                     if (!Server.getInstance().isPS() && Servers.localServer.isChaosServer()
                        || Servers.localServer.PVPSERVER && entry.HOMESERVER && !entry.PVPSERVER) {
                        String kingdomMsg = "This course will take you into friendly territory";
                        if (oKingdom != entry.getKingdom()) {
                           kingdomMsg = kingdomMsg + ", and you will join the " + Kingdoms.getNameFor(entry.getKingdom()) + " kingdom until you return";
                        }

                        oPlayer.getCommunicator().sendNormalServerMessage(kingdomMsg + ".");
                     }
                  } else {
                     String kingdomMsg = "This course will take you into hostile territory";
                     if (oKingdom != oPlayer.getKingdomId()) {
                        kingdomMsg = kingdomMsg + ", and you will join the " + Kingdoms.getNameFor(oKingdom) + " kingdom until you return";
                     }

                     oPlayer.getCommunicator().sendAlertServerMessage(kingdomMsg + ".");
                  }

                  if (entry.PVPSERVER
                     && !Servers.localServer.PVPSERVER
                     && oPlayer.getDeity() != null
                     && !QuestionParser.doesKingdomTemplateAcceptDeity(Kingdoms.getKingdomTemplateFor(oKingdom), oPlayer.getDeity())) {
                     oPlayer.getCommunicator()
                        .sendAlertServerMessage(
                           "Warning: "
                              + oPlayer.getDeity().getName()
                              + " does not align with your kingdom of "
                              + Kingdoms.getNameFor(oKingdom)
                              + ". If you continue travel to "
                              + entry.getName()
                              + " you will lose all faith and abilities granted by your deity."
                        );
                  }
               } catch (NoSuchPlayerException var11) {
               }
            }
         }
      }
   }

   public boolean checkPassengerPermissions(Creature performer) {
      boolean toReturn = false;
      if (!Servers.localServer.PVPSERVER) {
         try {
            Item ivehic = Items.getItem(this.wurmid);
            if (ivehic.isGuest(performer) && ivehic.mayCommand(performer)) {
               for(Seat seat : this.getSeats()) {
                  if (seat.isOccupied() && seat.type == 1 && !ivehic.isGuest(seat.getOccupant())) {
                     try {
                        Creature c = Server.getInstance().getCreature(seat.occupant);
                        if (!ivehic.mayPassenger(c)) {
                           performer.getCommunicator()
                              .sendNormalServerMessage(
                                 "You may not leave the server with this boat as one of your passengers will not have passenger permission on new server."
                              );
                           toReturn = true;
                           break;
                        }
                     } catch (NoSuchPlayerException | NoSuchCreatureException var9) {
                     }
                  }
               }
            } else {
               performer.getCommunicator()
                  .sendNormalServerMessage("You may not leave the server with this boat. You need to be explicitly specified in the boat's permissions.");
               toReturn = true;
            }
         } catch (NoSuchItemException var10) {
         }
      }

      return !toReturn;
   }

   public void touchPlotCourseCooldowns() {
      this.touchPlotCourseCooldowns(1800000L);
   }

   public void touchPlotCourseCooldowns(long cooldown) {
      for(Seat seat : this.getSeats()) {
         Cooldowns cd = Cooldowns.getCooldownsFor(seat.getOccupant(), true);
         cd.addCooldown(717, System.currentTimeMillis() + cooldown, false);
      }
   }

   public long getPlotCourseCooldowns() {
      long currentTimer = 0L;

      for(Seat seat : this.getSeats()) {
         Cooldowns cd = Cooldowns.getCooldownsFor(seat.getOccupant(), false);
         if (cd != null) {
            long remain = cd.isAvaibleAt(717);
            if (remain > currentTimer) {
               currentTimer = remain;
            }
         }
      }

      return currentTimer;
   }

   public String checkCourseRestrictions() {
      long currentTimer = 0L;

      for(Seat seat : this.getSeats()) {
         try {
            Player p = Players.getInstance().getPlayer(seat.getOccupant());
            if ((p.isFighting() || p.getEnemyPresense() > 0) && p.getSecondsPlayed() > 300.0F) {
               return "There are enemies in the vicinity. You fail to focus on a course.";
            }
         } catch (NoSuchPlayerException var10) {
         }

         Cooldowns cd = Cooldowns.getCooldownsFor(seat.getOccupant(), false);
         if (cd != null) {
            long remain = cd.isAvaibleAt(717);
            if (remain > currentTimer) {
               currentTimer = remain;
            }
         }
      }

      return currentTimer > 0L ? "You must wait another " + Server.getTimeFor(currentTimer) + " to plot a course." : "";
   }

   public boolean isPvPBlocking() {
      for(Seat lSeat : this.seats) {
         try {
            Player oPlayer = Players.getInstance().getPlayer(lSeat.getOccupant());
            if (oPlayer.isBlockingPvP()) {
               return true;
            }
         } catch (NoSuchPlayerException var6) {
         }
      }

      return false;
   }
}

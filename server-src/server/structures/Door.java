package com.wurmonline.server.structures;

import com.wurmonline.server.Items;
import com.wurmonline.server.MiscConstants;
import com.wurmonline.server.NoSuchItemException;
import com.wurmonline.server.Server;
import com.wurmonline.server.TimeConstants;
import com.wurmonline.server.creatures.AnimalSettings;
import com.wurmonline.server.creatures.Creature;
import com.wurmonline.server.items.Item;
import com.wurmonline.server.players.Permissions;
import com.wurmonline.server.players.PermissionsHistories;
import com.wurmonline.server.players.PermissionsPlayerList;
import com.wurmonline.server.players.Player;
import com.wurmonline.server.sounds.SoundPlayer;
import com.wurmonline.server.tutorial.MissionTriggers;
import com.wurmonline.server.zones.NoSuchTileException;
import com.wurmonline.server.zones.NoSuchZoneException;
import com.wurmonline.server.zones.VirtualZone;
import com.wurmonline.server.zones.VolaTile;
import com.wurmonline.server.zones.Zones;
import com.wurmonline.shared.constants.SoundNames;
import java.io.IOException;
import java.util.HashSet;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

public abstract class Door implements MiscConstants, SoundNames, TimeConstants, PermissionsPlayerList.ISettings {
   Wall wall;
   private static Logger logger = Logger.getLogger(Door.class.getName());
   long lock = -10L;
   long structure = -10L;
   boolean open = false;
   VolaTile outerTile;
   VolaTile innerTile;
   protected int startx;
   protected int starty;
   protected int endx;
   protected int endy;
   Set<Creature> creatures;
   Set<VirtualZone> watchers;
   short lockCounter = 0;
   boolean preAlertLockedStatus = false;
   String name = "";

   Door() {
   }

   Door(Wall _wall) {
      this.wall = _wall;
   }

   public final void setLockCounter(short newcounter) {
      if (this.lockCounter <= 0 || newcounter <= 0) {
         this.playLockSound();
      }

      if (newcounter > this.lockCounter) {
         this.lockCounter = newcounter;
      }

      if (this.lockCounter > 0 && this.getInnerTile() != null) {
         this.getInnerTile().addUnlockedDoor(this);
      }
   }

   public short getLockCounter() {
      return this.lockCounter;
   }

   public String getLockCounterTime() {
      int m = this.lockCounter / 120;
      int s = this.lockCounter % 120 / 2;
      return m > 0 ? m + " minutes and " + s + " seconds." : s + " seconds.";
   }

   private void playLockSound() {
      if (this.innerTile != null) {
         SoundPlayer.playSound("sound.object.lockunlock", this.innerTile.tilex, this.innerTile.tiley, this.innerTile.isOnSurface(), 1.0F);
         Server.getInstance().broadCastMessage("A loud *click* is heard.", this.innerTile.tilex, this.innerTile.tiley, this.innerTile.isOnSurface(), 5);
      } else if (this.outerTile != null) {
         SoundPlayer.playSound("sound.object.lockunlock", this.outerTile.tilex, this.outerTile.tiley, this.outerTile.isOnSurface(), 1.0F);
         Server.getInstance().broadCastMessage("A loud *click* is heard.", this.outerTile.tilex, this.outerTile.tiley, this.outerTile.isOnSurface(), 5);
      }
   }

   public final void setStructureId(long structureId) {
      this.structure = structureId;
   }

   public final long getStructureId() {
      return this.structure;
   }

   public final void addWatcher(VirtualZone watcher) {
      if (this.watchers == null) {
         this.watchers = new HashSet<>();
      }

      if (!this.watchers.contains(watcher)) {
         this.watchers.add(watcher);
      }
   }

   public final void removeWatcher(VirtualZone watcher) {
      if (this.watchers != null && this.watchers.contains(watcher)) {
         this.watchers.remove(watcher);
      }
   }

   public final String getName() {
      return this.name;
   }

   public final VolaTile getOuterTile() {
      return this.outerTile;
   }

   public final VolaTile getInnerTile() {
      return this.innerTile;
   }

   public int getTileX() {
      if (this.outerTile != null) {
         return this.outerTile.tilex;
      } else {
         return this.innerTile != null ? this.innerTile.tilex : -1;
      }
   }

   public int getTileY() {
      if (this.outerTile != null) {
         return this.outerTile.tiley;
      } else {
         return this.innerTile != null ? this.innerTile.tiley : -1;
      }
   }

   public final Wall getWall() throws NoSuchWallException {
      if (this.wall == null) {
         throw new NoSuchWallException("null inner wall for tilex=" + this.getTileX() + ", tiley=" + this.getTileY() + " structure=" + this.getStructureId());
      } else {
         return this.wall;
      }
   }

   final void calculateArea() {
      int innerTileStartX = this.innerTile.getTileX();
      int outerTileStartX = this.outerTile.getTileX();
      int innerTileStartY = this.innerTile.getTileY();
      int outerTileStartY = this.outerTile.getTileY();
      if (innerTileStartX == outerTileStartX) {
         this.starty = (Math.min(innerTileStartY, outerTileStartY) << 2) + 2;
         this.endy = (Math.max(innerTileStartY, outerTileStartY) << 2) + 2;
         this.startx = innerTileStartX << 2;
         this.endx = innerTileStartX + 1 << 2;
      } else {
         this.starty = innerTileStartY << 2;
         this.endy = innerTileStartY + 1 << 2;
         this.startx = (Math.min(innerTileStartX, outerTileStartX) << 2) + 2;
         this.endx = (Math.max(innerTileStartX, outerTileStartX) << 2) + 2;
      }
   }

   public final boolean isTransition() {
      return this.innerTile.isTransition() || this.outerTile.isTransition();
   }

   public boolean covers(float x, float y, float posz, int floorLevel, boolean followGround) {
      return (this.wall != null && this.wall.isWithinZ(posz + 1.0F, posz, followGround) || this.isTransition() && floorLevel <= 0)
         && x >= (float)this.startx
         && x <= (float)this.endx
         && y >= (float)this.starty
         && y <= (float)this.endy;
   }

   public void addToTiles() {
      try {
         Structure struct = Structures.getStructure(this.structure);
         this.outerTile = this.wall.getOrCreateOuterTile(struct.isSurfaced());
         this.outerTile.addDoor(this);
         this.innerTile = this.wall.getOrCreateInnerTile(struct.isSurfaced());
         this.innerTile.addDoor(this);
         this.calculateArea();
      } catch (NoSuchStructureException var2) {
         logger.log(Level.WARNING, "No such structure? structure: " + this.structure, (Throwable)var2);
      } catch (NoSuchZoneException var3) {
         logger.log(Level.WARNING, "No such zone - wall: " + this.wall + " - " + var3.getMessage(), (Throwable)var3);
      } catch (NoSuchTileException var4) {
         logger.log(Level.WARNING, "No such tile - wall: " + this.wall + " - " + var4.getMessage(), (Throwable)var4);
      }
   }

   public final void removeFromTiles() {
      if (this.outerTile != null) {
         this.outerTile.removeDoor(this);
      }

      if (this.innerTile != null) {
         this.innerTile.removeDoor(this);
      }
   }

   public boolean canBeOpenedBy(Creature creature, boolean wentThroughDoor) {
      if (creature == null) {
         return false;
      } else if (!creature.isKingdomGuard() && !creature.isGhost()) {
         if (MissionTriggers.isDoorOpen(creature, this.wall.getId(), 1)) {
            return true;
         } else if (creature.getPower() > 0) {
            return true;
         } else if (creature.getLeader() != null && this.canBeOpenedBy(creature.getLeader(), false)) {
            return true;
         } else {
            return !creature.canOpenDoors() ? false : this.canBeUnlockedBy(creature);
         }
      } else {
         return true;
      }
   }

   public boolean canBeUnlockedByKey(Item key) {
      Item doorlock = null;

      try {
         doorlock = Items.getItem(this.lock);
      } catch (NoSuchItemException var4) {
         return false;
      }

      if (doorlock.isLocked()) {
         return doorlock.isUnlockedBy(key.getWurmId());
      } else {
         return false;
      }
   }

   public boolean canBeUnlockedBy(Creature creature) {
      return this.mayPass(creature);
   }

   public void creatureMoved(Creature creature, int diffTileX, int diffTileY) {
      if (this.covers(
         creature.getStatus().getPositionX(), creature.getStatus().getPositionY(), creature.getPositionZ(), creature.getFloorLevel(), creature.followsGround()
      )) {
         if (!this.addCreature(creature) && (diffTileX != 0 || diffTileY != 0)) {
            if (!this.canBeOpenedBy(creature, true)) {
               try {
                  int tilex = creature.getTileX();
                  int tiley = creature.getTileY();
                  VolaTile tile = Zones.getZone(tilex, tiley, creature.isOnSurface()).getTileOrNull(tilex, tiley);
                  if (tile != null) {
                     if (tile == this.innerTile) {
                        int oldTileX = tilex - diffTileX;
                        int oldTileY = tiley - diffTileY;
                        if (creature instanceof Player) {
                           creature.getCommunicator().sendAlertServerMessage("You cannot enter that building.");
                           logger.log(
                              Level.WARNING,
                              creature.getName()
                                 + " a cheater? Passed through door at "
                                 + creature.getStatus().getPositionX()
                                 + ", "
                                 + creature.getStatus().getPositionY()
                                 + ", z="
                                 + creature.getPositionZ()
                                 + ", minZ="
                                 + this.wall.getMinZ()
                           );
                           creature.setTeleportPoints((short)oldTileX, (short)oldTileY, creature.getLayer(), creature.getFloorLevel());
                           creature.startTeleporting();
                           creature.getCommunicator().sendTeleport(false);
                        }
                     }
                  } else {
                     logger.log(
                        Level.WARNING,
                        "A door on no tile at "
                           + creature.getStatus().getPositionX()
                           + ", "
                           + creature.getStatus().getPositionY()
                           + ", structure: "
                           + this.structure
                     );
                  }
               } catch (NoSuchZoneException var11) {
                  logger.log(
                     Level.WARNING,
                     "A door in no zone at "
                        + creature.getStatus().getPositionX()
                        + ", "
                        + creature.getStatus().getPositionY()
                        + ", structure: "
                        + this.structure
                        + " - "
                        + var11
                  );
               }
            } else if (this.structure != -10L && creature.isPlayer()) {
               int tilex = creature.getTileX();
               int tiley = creature.getTileY();
               VolaTile tile = Zones.getTileOrNull(tilex, tiley, creature.isOnSurface());
               if (tile == this.innerTile) {
                  if (creature.getEnemyPresense() > 0 && tile.getVillage() == null) {
                     this.setLockCounter((short)120);
                  }
               } else if (tile == this.outerTile) {
                  int oldTileX = tilex - diffTileX;
                  int oldTileY = tiley - diffTileY;

                  try {
                     VolaTile oldtile = Zones.getZone(oldTileX, oldTileY, creature.isOnSurface()).getTileOrNull(oldTileX, oldTileY);
                     if (oldtile != null && oldtile == this.innerTile && creature.getEnemyPresense() > 0 && oldtile.getVillage() == null) {
                        this.setLockCounter((short)120);
                     }
                  } catch (NoSuchZoneException var10) {
                     logger.log(
                        Level.WARNING,
                        "A door in no zone at "
                           + creature.getStatus().getPositionX()
                           + ", "
                           + creature.getStatus().getPositionY()
                           + ", structure: "
                           + this.structure
                           + " - "
                           + var10
                     );
                  }
               }
            }
         }
      } else {
         this.removeCreature(creature);
      }
   }

   public void updateDoor(Creature creature, Item key, boolean removedKey) {
      boolean isOpenToCreature = this.canBeOpenedBy(creature, false);
      if (removedKey) {
         if (this.creatures != null) {
            if (this.creatures.contains(creature) && !isOpenToCreature && this.canBeUnlockedByKey(key) && this.isOpen()) {
               boolean close = true;

               for(Creature checked : this.creatures) {
                  if (this.canBeOpenedBy(checked, false)) {
                     close = false;
                  }
               }

               if (close && creature.isVisible() && !creature.isGhost()) {
                  this.close();
               }
            }

            if (this.creatures.size() == 0) {
               this.creatures = null;
            }
         }
      } else if (this.creatures != null && this.creatures.contains(creature) && isOpenToCreature && this.canBeUnlockedByKey(key)) {
         if (!this.isOpen() && creature.isVisible() && !creature.isGhost()) {
            this.open();
         }

         creature.getCommunicator().sendPassable(true, this);
      }
   }

   public void removeCreature(Creature creature) {
      if (this.creatures != null) {
         if (this.creatures.contains(creature)) {
            this.creatures.remove(creature);
            creature.setCurrentDoor(null);
            creature.getCommunicator().sendPassable(false, this);
            if (this.isOpen()) {
               boolean close = true;

               for(Creature checked : this.creatures) {
                  if (this.canBeOpenedBy(checked, false)) {
                     close = false;
                  }
               }

               if (close && creature.isVisible() && !creature.isGhost()) {
                  this.close();
               }
            }
         }

         if (this.creatures.size() == 0) {
            this.creatures = null;
         }
      }
   }

   public boolean addCreature(Creature creature) {
      if (this.creatures == null) {
         this.creatures = new HashSet<>();
      }

      if (!this.creatures.contains(creature)) {
         this.creatures.add(creature);
         creature.setCurrentDoor(this);
         if (this.canBeOpenedBy(creature, false)) {
            if (!this.isOpen() && creature.isVisible() && !creature.isGhost()) {
               this.open();
            }

            creature.getCommunicator().sendPassable(true, this);
         }

         return true;
      } else {
         return false;
      }
   }

   public void setLock(long lockid) {
      this.lock = lockid;

      try {
         this.save();
      } catch (IOException var4) {
         logger.log(Level.WARNING, "Failed to save door for structure with id " + this.structure);
      }
   }

   public final long getLockId() throws NoSuchLockException {
      if (this.lock == -10L) {
         throw new NoSuchLockException("No ID");
      } else {
         return this.lock;
      }
   }

   boolean keyFits(long keyId) throws NoSuchLockException {
      if (this.lock == -10L) {
         throw new NoSuchLockException("No ID");
      } else {
         try {
            Structure struct = Structures.getStructure(this.structure);
            if (struct.getWritId() == keyId) {
               return true;
            }
         } catch (NoSuchStructureException var5) {
            logger.log(
               Level.WARNING,
               "This door's structure does not exist! "
                  + this.startx
                  + ", "
                  + this.starty
                  + "-"
                  + this.endx
                  + ", "
                  + this.endy
                  + ", structure: "
                  + this.structure
                  + " - "
                  + var5,
               (Throwable)var5
            );
         }

         try {
            Item doorlock = Items.getItem(this.lock);
            return doorlock.isUnlockedBy(keyId);
         } catch (NoSuchItemException var4) {
            logger.log(Level.INFO, "Lock has decayed? Id was " + this.lock + ", structure: " + this.structure + " - " + var4);
            return false;
         }
      }
   }

   public final boolean isOpen() {
      return this.open;
   }

   void close() {
      if (this.wall != null && this.wall.isFinished()) {
         if (this.wall.isAlwaysOpen()) {
            return;
         }

         if (this.innerTile != null) {
            SoundPlayer.playSound("sound.door.close", this.innerTile.tilex, this.innerTile.tiley, this.innerTile.isOnSurface(), 1.0F);
         } else if (this.outerTile != null) {
            SoundPlayer.playSound("sound.door.close", this.outerTile.tilex, this.outerTile.tiley, this.outerTile.isOnSurface(), 1.0F);
         }
      }

      this.open = false;
      if (this.watchers != null) {
         for(VirtualZone z : this.watchers) {
            z.closeDoor(this);
         }
      }
   }

   void open() {
      if (this.wall != null && this.wall.isFinished()) {
         if (this.wall.isAlwaysOpen()) {
            return;
         }

         if (this.innerTile != null) {
            SoundPlayer.playSound("sound.door.open", this.innerTile.tilex, this.innerTile.tiley, this.innerTile.isOnSurface(), 1.0F);
         } else if (this.outerTile != null) {
            SoundPlayer.playSound("sound.door.open", this.outerTile.tilex, this.outerTile.tiley, this.outerTile.isOnSurface(), 1.0F);
         }
      }

      this.open = true;
      if (this.watchers != null) {
         for(VirtualZone z : this.watchers) {
            z.openDoor(this);
         }
      }
   }

   public float getQualityLevel() {
      return this.wall.getCurrentQualityLevel();
   }

   public final Item getLock() throws NoSuchLockException {
      if (this.lock == -10L) {
         throw new NoSuchLockException("No ID");
      } else {
         try {
            return Items.getItem(this.lock);
         } catch (NoSuchItemException var2) {
            throw new NoSuchLockException(var2);
         }
      }
   }

   public final boolean pollUnlocked() {
      if (this.lockCounter > 0) {
         --this.lockCounter;
         if (this.lockCounter == 0) {
            this.playLockSound();
            return true;
         }
      }

      return false;
   }

   public final boolean startAlert(boolean playSound) {
      this.preAlertLockedStatus = this.isLocked();
      if (!this.preAlertLockedStatus) {
         this.lock(playSound);
         return true;
      } else {
         return false;
      }
   }

   public final boolean endAlert(boolean playSound) {
      if (!this.preAlertLockedStatus) {
         this.unlock(playSound);
         return true;
      } else {
         return false;
      }
   }

   public final void lock(boolean playSound) {
      try {
         Item lLock = this.getLock();
         lLock.lock();
         if (playSound) {
            this.playLockSound();
         }
      } catch (NoSuchLockException var3) {
      }
   }

   public final void unlock(boolean playSound) {
      try {
         Item lLock = this.getLock();
         lLock.unlock();
         if (playSound) {
            this.playLockSound();
         }
      } catch (NoSuchLockException var3) {
      }
   }

   public final boolean isUnlocked() {
      return !this.isLocked();
   }

   public final boolean isLocked() {
      if (this.lockCounter > 0) {
         return false;
      } else {
         try {
            Item lLock = this.getLock();
            return lLock.isLocked();
         } catch (NoSuchLockException var2) {
            return false;
         }
      }
   }

   public void setNewName(String newname) {
      this.name = newname;
      this.innerTile.updateWall(this.wall);
      this.outerTile.updateWall(this.wall);
   }

   @Override
   public abstract void save() throws IOException;

   abstract void load() throws IOException;

   public abstract void delete();

   public abstract void setName(String var1);

   public int getFloorLevel() {
      return this.wall.getFloorLevel();
   }

   @Override
   public int getMaxAllowed() {
      return AnimalSettings.getMaxAllowed();
   }

   @Override
   public long getWurmId() {
      return this.wall.getId();
   }

   @Override
   public int getTemplateId() {
      return -10;
   }

   @Override
   public String getObjectName() {
      return this.getName();
   }

   @Override
   public boolean setObjectName(String aNewName, Creature aCreature) {
      this.setName(aNewName);
      return true;
   }

   @Override
   public boolean isActualOwner(long playerId) {
      return this.isOwner(playerId);
   }

   @Override
   public boolean isOwner(Creature creature) {
      return this.isOwner(creature.getWurmId());
   }

   @Override
   public boolean isOwner(long playerId) {
      try {
         Structure struct = Structures.getStructure(this.structure);
         return struct.isOwner(playerId);
      } catch (NoSuchStructureException var4) {
         return false;
      }
   }

   @Override
   public boolean canChangeName(Creature creature) {
      return this.isOwner(creature) || creature.getPower() > 1;
   }

   @Override
   public boolean canChangeOwner(Creature creature) {
      return false;
   }

   private boolean showWarning() {
      try {
         this.getLock();
         return false;
      } catch (NoSuchLockException var2) {
         return true;
      }
   }

   @Override
   public String getWarning() {
      return this.showWarning() ? "NEEDS TO HAVE A LOCK!" : "";
   }

   @Override
   public PermissionsPlayerList getPermissionsPlayerList() {
      return DoorSettings.getPermissionsPlayerList(this.getWurmId());
   }

   @Override
   public boolean isManaged() {
      return this.wall == null ? false : this.wall.getSettings().hasPermission(Permissions.Allow.SETTLEMENT_MAY_MANAGE.getBit());
   }

   @Override
   public boolean isManageEnabled(Player player) {
      return this.mayManage(player) || player.getPower() > 1;
   }

   @Override
   public void setIsManaged(boolean newIsManaged, Player player) {
      if (this.wall != null) {
         if (newIsManaged && DoorSettings.exists(this.getWurmId())) {
            DoorSettings.remove(this.getWurmId());
            if (player != null) {
               PermissionsHistories.addHistoryEntry(
                  this.getWurmId(), System.currentTimeMillis(), player.getWurmId(), player.getName(), "Removed all permissions"
               );
            }
         }

         this.wall.getSettings().setPermissionBit(Permissions.Allow.SETTLEMENT_MAY_MANAGE.getBit(), newIsManaged);
         this.wall.savePermissions();
      }
   }

   @Override
   public void addDefaultCitizenPermissions() {
   }

   @Override
   public boolean isCitizen(Creature aCreature) {
      try {
         Structure struct = Structures.getStructure(this.structure);
         return struct.isCitizen(aCreature);
      } catch (NoSuchStructureException var3) {
         return false;
      }
   }

   @Override
   public boolean isAllied(Creature aCreature) {
      try {
         Structure struct = Structures.getStructure(this.structure);
         return struct.isAllied(aCreature);
      } catch (NoSuchStructureException var3) {
         return false;
      }
   }

   @Override
   public boolean isSameKingdom(Creature aCreature) {
      try {
         Structure struct = Structures.getStructure(this.structure);
         return struct.isSameKingdom(aCreature);
      } catch (NoSuchStructureException var3) {
         return false;
      }
   }

   @Override
   public boolean isGuest(Creature creature) {
      return this.isGuest(creature.getWurmId());
   }

   @Override
   public boolean isGuest(long playerId) {
      return DoorSettings.isGuest(this, playerId);
   }

   @Override
   public void addGuest(long guestId, int aSettings) {
      DoorSettings.addPlayer(this.getWurmId(), guestId, aSettings);
   }

   @Override
   public void removeGuest(long guestId) {
      DoorSettings.removePlayer(this.getWurmId(), guestId);
   }

   @Override
   public boolean canHavePermissions() {
      return this.isLocked();
   }

   @Override
   public boolean mayShowPermissions(Creature creature) {
      return !this.isManaged() && this.hasLock() && this.mayManage(creature);
   }

   public boolean canManage(Creature creature) {
      if (this.wall == null) {
         return false;
      } else {
         Structure structure = Structures.getStructureOrNull(this.wall.getStructureId());
         return structure == null ? false : structure.canManage(creature);
      }
   }

   public boolean mayManage(Creature creature) {
      return creature.getPower() > 1 ? true : this.canManage(creature);
   }

   public boolean mayPass(Creature creature) {
      if (creature.getPower() > 1) {
         return true;
      } else if (this.wall == null) {
         return true;
      } else {
         Structure structure = Structures.getStructureOrNull(this.wall.getStructureId());
         if (structure == null) {
            return true;
         } else if (!this.isLocked()) {
            return true;
         } else if (structure.isExcluded(creature)) {
            return false;
         } else if (this.isManaged()) {
            return structure.mayPass(creature);
         } else {
            return DoorSettings.isExcluded(this, creature) ? false : DoorSettings.mayPass(this, creature);
         }
      }
   }

   public boolean mayLock(Creature creature) {
      if (creature.getPower() > 1) {
         return true;
      } else if (this.wall == null) {
         return true;
      } else {
         Structure structure = Structures.getStructureOrNull(this.wall.getStructureId());
         if (structure == null) {
            return true;
         } else if (structure.isExcluded(creature)) {
            return false;
         } else {
            return this.isManaged() ? structure.mayModify(creature) : false;
         }
      }
   }

   public boolean hasLock() {
      try {
         this.getLock();
      } catch (NoSuchLockException var2) {
         this.lock = -10L;
      }

      return this.lock != -10L;
   }

   @Override
   public String getTypeName() {
      return this.wall == null ? "No Wall!" : this.wall.getTypeName();
   }

   @Override
   public String mayManageText(Player aPlayer) {
      return "Controlled By Building";
   }

   @Override
   public String mayManageHover(Player aPlayer) {
      return "If ticked, then building controls entry.";
   }

   @Override
   public String messageOnTick() {
      return "This will allow the building to Control this door.";
   }

   @Override
   public String questionOnTick() {
      return "Are you sure?";
   }

   @Override
   public String messageUnTick() {
      return "This will allow the door to be independant of the building 'May Enter' setting.";
   }

   @Override
   public String questionUnTick() {
      return "Are you sure?";
   }

   @Override
   public String getSettlementName() {
      try {
         Structure struct = Structures.getStructure(this.structure);
         return struct.getSettlementName();
      } catch (NoSuchStructureException var2) {
         return "";
      }
   }

   @Override
   public String getAllianceName() {
      try {
         Structure struct = Structures.getStructure(this.structure);
         return struct.getAllianceName();
      } catch (NoSuchStructureException var2) {
         return "";
      }
   }

   @Override
   public String getKingdomName() {
      try {
         Structure struct = Structures.getStructure(this.structure);
         return struct.getKingdomName();
      } catch (NoSuchStructureException var2) {
         return "";
      }
   }

   @Override
   public boolean canAllowEveryone() {
      return false;
   }

   @Override
   public String getRolePermissionName() {
      return "";
   }

   @Override
   public boolean setNewOwner(long playerId) {
      return false;
   }

   @Override
   public String getOwnerName() {
      return "";
   }

   public boolean isNotLockable() {
      return this.wall.isNotLockable();
   }

   public boolean isNotLockpickable() {
      return this.wall.isNotLockpickable();
   }

   public byte getLayer() {
      return this.wall.getLayer();
   }
}

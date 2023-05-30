package com.wurmonline.server.structures;

import com.wurmonline.math.TilePos;
import com.wurmonline.math.Vector3f;
import com.wurmonline.mesh.Tiles;
import com.wurmonline.server.MiscConstants;
import com.wurmonline.server.Server;
import com.wurmonline.server.Servers;
import com.wurmonline.server.TimeConstants;
import com.wurmonline.server.creatures.Creature;
import com.wurmonline.server.items.Item;
import com.wurmonline.server.players.Permissions;
import com.wurmonline.server.skills.NoSuchSkillException;
import com.wurmonline.server.skills.Skill;
import com.wurmonline.server.skills.Skills;
import com.wurmonline.server.villages.Village;
import com.wurmonline.server.zones.NoSuchZoneException;
import com.wurmonline.server.zones.VolaTile;
import com.wurmonline.server.zones.Zone;
import com.wurmonline.server.zones.Zones;
import com.wurmonline.shared.constants.CounterTypes;
import com.wurmonline.shared.constants.FenceConstants;
import com.wurmonline.shared.constants.ItemMaterials;
import com.wurmonline.shared.constants.StructureConstantsEnum;
import com.wurmonline.shared.constants.StructureStateEnum;
import com.wurmonline.shared.constants.WallConstants;
import java.io.IOException;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Level;
import java.util.logging.Logger;

public abstract class Fence implements MiscConstants, FenceConstants, TimeConstants, Blocker, ItemMaterials, StructureSupport, Permissions.IAllow, CounterTypes {
   public static final byte DIR_HORIZ = 0;
   public static final byte DIR_DOWNRIGHT = 1;
   public static final byte DIR_DOWN = 2;
   public static final byte DIR_DOWNLEFT = 3;
   private static final long lowHedgeGrowthInterval = 3L * (Servers.localServer.testServer ? 60000L : 86400000L);
   private static final long mediumHedgeGrowthInterval = 10L * (Servers.localServer.testServer ? 60000L : 86400000L);
   private final boolean deityFence;
   int number = -10;
   private static final Logger logger = Logger.getLogger(Fence.class.getName());
   float originalQL;
   float currentQL;
   float damage;
   StructureConstantsEnum type = StructureConstantsEnum.FENCE_PLAN_WOODEN;
   int tilex;
   int tiley;
   long lastUsed;
   StructureStateEnum state = StructureStateEnum.UNINITIALIZED;
   byte dir;
   int zoneId;
   private boolean surfaced = true;
   protected int color = -1;
   public static final float FENCE_DAMAGE_STATE_DIVIDER = 60.0F;
   int heightOffset = 0;
   int layer = 0;
   private static final Vector3f normalHoriz = new Vector3f(0.0F, 1.0F, 0.0F);
   private static final Vector3f normalVertical = new Vector3f(1.0F, 0.0F, 0.0F);
   private final Vector3f centerPoint;
   private int floorLevel = 0;
   Permissions permissions = new Permissions();
   private static final ConcurrentHashMap<Long, Fence> rubbleFences = new ConcurrentHashMap<>();

   public Fence(
      StructureConstantsEnum aType, int aTileX, int aTileY, int aHeightOffset, float aQualityLevel, Tiles.TileBorderDirection aDir, int aZoneId, int aLayer
   ) {
      this.tilex = aTileX;
      this.tiley = aTileY;
      this.currentQL = aQualityLevel;
      this.originalQL = aQualityLevel;
      this.lastUsed = System.currentTimeMillis();
      this.type = aType;
      this.zoneId = aZoneId;
      this.dir = aDir.getCode();
      this.heightOffset = aHeightOffset;
      this.layer = aLayer;

      try {
         Zone zone = Zones.getZone(aZoneId);
         this.surfaced = zone.isOnSurface();
      } catch (NoSuchZoneException var10) {
         logger.log(Level.WARNING, "ZoneId: " + aZoneId + ", fence: " + this.number + " - " + var10.getMessage(), (Throwable)var10);
      }

      this.deityFence = Servers.localServer.entryServer && this.originalQL > 99.0F;
      this.setFloorLevel();
      this.centerPoint = this.calculateCenterPoint();
      if (this.type == StructureConstantsEnum.FENCE_RUBBLE) {
         rubbleFences.put(this.getId(), this);
      }
   }

   private final Vector3f calculateCenterPoint() {
      return new Vector3f(
         this.isHorizontal() ? (float)(this.tilex * 4 + 2) : (float)(this.tilex * 4),
         this.isHorizontal() ? (float)(this.tiley * 4) : (float)(this.tiley * 4 + 2),
         this.getMinZ() + 1.5F
      );
   }

   public Fence(
      int aNum,
      StructureConstantsEnum aType,
      StructureStateEnum aState,
      int aColor,
      int aTileX,
      int aTileY,
      int aHeightOffset,
      float aQualityLevel,
      float aOrigQl,
      long aLastUsed,
      Tiles.TileBorderDirection aDir,
      int aZoneId,
      boolean aSurface,
      float aDamage,
      int aLayer,
      int aSettings
   ) {
      this.number = aNum;
      this.state = aState;
      this.tilex = aTileX;
      this.tiley = aTileY;
      this.currentQL = aQualityLevel;
      this.originalQL = aOrigQl;
      this.lastUsed = aLastUsed;
      this.type = aType;
      this.zoneId = aZoneId;
      this.dir = aDir.getCode();
      this.color = aColor;
      this.surfaced = aSurface;
      this.damage = aDamage;
      this.layer = aLayer;
      this.deityFence = Servers.localServer.entryServer && this.originalQL > 99.0F;
      this.centerPoint = this.calculateCenterPoint();
      this.heightOffset = aHeightOffset;
      this.setSettings(aSettings);
      this.setFloorLevel();
      if (this.type == StructureConstantsEnum.FENCE_RUBBLE) {
         rubbleFences.put(this.getId(), this);
      }
   }

   @Override
   public final boolean isFence() {
      return true;
   }

   @Override
   public final boolean isTile() {
      return false;
   }

   @Override
   public final boolean isWall() {
      return false;
   }

   @Override
   public final boolean isFloor() {
      return false;
   }

   @Override
   public final boolean isRoof() {
      return false;
   }

   @Override
   public final boolean isStair() {
      return false;
   }

   @Override
   public final Vector3f getNormal() {
      return this.isHorizontal() ? normalHoriz : normalVertical;
   }

   @Override
   public final Vector3f getCenterPoint() {
      return this.centerPoint;
   }

   @Override
   public final Vector3f isBlocking(Creature creature, Vector3f startPos, Vector3f endPos, Vector3f normal, int blockType, long target, boolean followGround) {
      if (target == this.getId()) {
         return null;
      } else if (!this.isFinished()) {
         return null;
      } else {
         return this.isBlocking(blockType, creature) ? this.getIntersectionPoint(startPos, endPos, normal, creature, blockType, followGround) : null;
      }
   }

   public final Vector3f getIntersectionPoint(Vector3f startPos, Vector3f endPos, Vector3f normal, Creature c, int blockType, boolean followGround) {
      Vector3f spcopy = startPos.clone();
      Vector3f epcopy = endPos.clone();
      if (this.getFloorLevel() == 0 && (followGround || spcopy.z <= this.getMinZ())) {
         spcopy.z = this.getMinZ() + 0.5F;
         if (followGround) {
            epcopy.z = this.getMinZ() + 0.5F;
         }
      }

      float u = this.getNormal().dot(this.getCenterPoint().subtract(spcopy)) / this.getNormal().dot(epcopy.subtract(spcopy));
      if (u >= 0.0F && u <= 1.0F) {
         Vector3f diff = this.getCenterPoint().subtract(spcopy);
         if (this.isHorizontal()) {
            float steps = diff.y / normal.y;
            Vector3f intersection = spcopy.add(normal.mult(steps));
            if (this.isWithinBounds(intersection, c, followGround)) {
               return intersection;
            }
         } else {
            float steps = diff.x / normal.x;
            Vector3f intersection = spcopy.add(normal.mult(steps));
            if (this.isWithinBounds(intersection, c, followGround)) {
               return intersection;
            }
         }
      }

      return null;
   }

   private final boolean isWithinBounds(Vector3f pointToCheck, Creature c, boolean followGround) {
      if (this.isHorizontal()) {
         if (pointToCheck.getY() >= (float)(this.tiley * 4) - 0.1F
            && pointToCheck.getY() <= (float)(this.tiley * 4) + 0.1F
            && pointToCheck.getX() >= (float)(this.tilex * 4)
            && pointToCheck.getX() <= (float)(this.tilex * 4 + 4)
            && (followGround && this.getFloorLevel() == 0 || pointToCheck.getZ() >= this.getMinZ() && pointToCheck.getZ() <= this.getMaxZ())) {
            return true;
         }
      } else if (pointToCheck.getX() >= (float)(this.tilex * 4) - 0.1F
         && pointToCheck.getX() <= (float)(this.tilex * 4) + 0.1F
         && pointToCheck.getY() >= (float)(this.tiley * 4)
         && pointToCheck.getY() <= (float)(this.tiley * 4 + 4)
         && (followGround && this.getFloorLevel() == 0 || pointToCheck.getZ() >= this.getMinZ() && pointToCheck.getZ() <= this.getMaxZ())) {
         return true;
      }

      return false;
   }

   public boolean isLowHedge() {
      switch(this.type) {
         case HEDGE_FLOWER1_LOW:
         case HEDGE_FLOWER2_LOW:
         case HEDGE_FLOWER3_LOW:
         case HEDGE_FLOWER4_LOW:
         case HEDGE_FLOWER5_LOW:
         case HEDGE_FLOWER6_LOW:
         case HEDGE_FLOWER7_LOW:
            return true;
         default:
            return false;
      }
   }

   public boolean isWalkthrough() {
      switch(this.type) {
         case FENCE_CURB:
         case FENCE_WOVEN:
         case FENCE_ROPE_LOW:
         case FENCE_RUBBLE:
            return true;
         default:
            return this.isFlowerbed();
      }
   }

   public boolean isFlowerbed() {
      switch(this.type) {
         case FLOWERBED_BLUE:
         case FLOWERBED_GREENISH_YELLOW:
         case FLOWERBED_ORANGE_RED:
         case FLOWERBED_PURPLE:
         case FLOWERBED_WHITE:
         case FLOWERBED_WHITE_DOTTED:
         case FLOWERBED_YELLOW:
            return true;
         default:
            return false;
      }
   }

   public boolean isLowFence() {
      switch(this.type) {
         case FENCE_CURB:
         case FENCE_WOVEN:
         case FENCE_ROPE_LOW:
         case FENCE_RUBBLE:
         case FENCE_GARDESGARD_LOW:
         case FENCE_IRON:
         case FENCE_STONEWALL:
         case FENCE_WOODEN:
         case FENCE_STONE_PARAPET:
         case FENCE_WOODEN_PARAPET:
         case FENCE_STONE_IRON_PARAPET:
         case FENCE_ROPE_HIGH:
         case FENCE_PLAN_ROPE_HIGH:
         case FENCE_GARDESGARD_GATE:
         case FENCE_WOODEN_GATE:
         case FENCE_WOODEN_CRUDE_GATE:
         case FENCE_WOODEN_CRUDE:
         case FENCE_IRON_GATE:
         case FENCE_STONE:
         case FENCE_SANDSTONE_STONE_PARAPET:
         case FENCE_SLATE_STONE_PARAPET:
         case FENCE_ROUNDED_STONE_STONE_PARAPET:
         case FENCE_RENDERED_STONE_PARAPET:
         case FENCE_POTTERY_STONE_PARAPET:
         case FENCE_MARBLE_STONE_PARAPET:
         case FENCE_SLATE_CHAIN_FENCE:
         case FENCE_ROUNDED_STONE_CHAIN_FENCE:
         case FENCE_SANDSTONE_CHAIN_FENCE:
         case FENCE_RENDERED_CHAIN_FENCE:
         case FENCE_POTTERY_CHAIN_FENCE:
         case FENCE_MARBLE_CHAIN_FENCE:
            return true;
         case FLOWERBED_BLUE:
         case FLOWERBED_GREENISH_YELLOW:
         case FLOWERBED_ORANGE_RED:
         case FLOWERBED_PURPLE:
         case FLOWERBED_WHITE:
         case FLOWERBED_WHITE_DOTTED:
         case FLOWERBED_YELLOW:
         default:
            return false;
      }
   }

   public boolean isMediumHedge() {
      switch(this.type) {
         case HEDGE_FLOWER1_MEDIUM:
         case HEDGE_FLOWER2_MEDIUM:
         case HEDGE_FLOWER3_MEDIUM:
         case HEDGE_FLOWER4_MEDIUM:
         case HEDGE_FLOWER5_MEDIUM:
         case HEDGE_FLOWER6_MEDIUM:
         case HEDGE_FLOWER7_MEDIUM:
            return true;
         default:
            return false;
      }
   }

   public boolean isHighHedge() {
      switch(this.type) {
         case HEDGE_FLOWER1_HIGH:
         case HEDGE_FLOWER2_HIGH:
         case HEDGE_FLOWER3_HIGH:
         case HEDGE_FLOWER4_HIGH:
         case HEDGE_FLOWER5_HIGH:
         case HEDGE_FLOWER6_HIGH:
         case HEDGE_FLOWER7_HIGH:
            return true;
         default:
            return false;
      }
   }

   public boolean isHedge() {
      return isHedge(this.type);
   }

   public static boolean isHedge(StructureConstantsEnum fenceType) {
      return fenceType.value >= StructureConstantsEnum.HEDGE_FLOWER1_LOW.value && fenceType.value <= StructureConstantsEnum.HEDGE_FLOWER7_HIGH.value;
   }

   public boolean isMagic() {
      switch(this.type) {
         case FENCE_MAGIC_STONE:
         case FENCE_MAGIC_ICE:
         case FENCE_MAGIC_FIRE:
            return true;
         default:
            return false;
      }
   }

   public final int getColor() {
      return this.color;
   }

   public final Tiles.TileBorderDirection getDir() {
      return this.dir == 0 ? Tiles.TileBorderDirection.DIR_HORIZ : Tiles.TileBorderDirection.DIR_DOWN;
   }

   public final int getDirAsByte() {
      return this.dir;
   }

   public final int getZoneId() {
      return this.zoneId;
   }

   @Override
   public final int getTileX() {
      return this.tilex;
   }

   public final Village getVillage() {
      VolaTile t = this.getTile();
      if (t != null && t.getVillage() != null) {
         return t.getVillage();
      } else {
         t = this.getOtherTile();
         return t != null && t.getVillage() != null ? t.getVillage() : null;
      }
   }

   public final void pollMagicFences(long currTime) {
      if (this.isMagic() && currTime - this.lastUsed > 1000L) {
         if (this.type == StructureConstantsEnum.FENCE_MAGIC_ICE) {
            if (Server.rand.nextInt(20) == 0) {
               VolaTile t = this.getTile();
               if (t != null) {
                  Creature[] crets = t.getCreatures();

                  for(Creature defender : crets) {
                     if (!defender.isUnique()) {
                        float dam = Math.max(3000.0F, this.damage * 100.0F);
                        defender.addWoundOfType(null, (byte)8, 0, true, 1.0F, true, (double)dam, 0.0F, 0.0F, false, true);
                     }
                  }
               }

               t = this.getOtherTile();
               if (t != null) {
                  Creature[] crets = t.getCreatures();

                  for(Creature defender : crets) {
                     if (!defender.isUnique()) {
                        float dam = Math.max(3000.0F, this.damage * 100.0F);
                        defender.addWoundOfType(null, (byte)8, 0, true, 1.0F, true, (double)dam, 0.0F, 0.0F, false, true);
                     }
                  }
               }
            }
         } else if (this.type == StructureConstantsEnum.FENCE_MAGIC_FIRE && Server.rand.nextInt(20) == 0) {
            VolaTile t = this.getTile();
            if (t != null) {
               Creature[] crets = t.getCreatures();

               for(Creature defender : crets) {
                  if (!defender.isUnique()) {
                     float dam = Math.max(3000.0F, this.damage * 100.0F);
                     defender.addWoundOfType(null, (byte)4, 0, true, 1.0F, true, (double)dam, 0.0F, 0.0F, false, true);
                  }
               }
            }

            t = this.getOtherTile();
            if (t != null) {
               Creature[] crets = t.getCreatures();

               for(Creature defender : crets) {
                  if (!defender.isUnique()) {
                     float dam = Math.max(3000.0F, this.damage * 100.0F);
                     defender.addWoundOfType(null, (byte)4, 0, true, 1.0F, true, (double)dam, 0.0F, 0.0F, false, true);
                  }
               }
            }
         }

         this.setLastUsed(currTime);
         if (!this.setDamage(this.getDamage() + 2.0F * (100.0F - this.getQualityLevel()) / 100.0F)) {
            VolaTile tile = Zones.getTileOrNull(this.getTileX(), this.getTileY(), this.surfaced);
            if (tile != null) {
               tile.updateMagicalFence(this);
            }
         }
      }
   }

   public final void poll(long currTime) {
      float mod = 1.0F;
      if (this.isHedge()) {
         if (this.getDamage() > 0.0F) {
            this.setDamage(this.getDamage() - this.getQualityLevel() / 2.0F);
         }

         if (currTime - this.lastUsed > 86400000L) {
            if (this.isLowHedge() && this.type != StructureConstantsEnum.HEDGE_FLOWER1_LOW) {
               if (currTime - this.lastUsed > lowHedgeGrowthInterval && Server.rand.nextInt(10) < 7) {
                  this.setDamage(0.0F);
                  this.setType(StructureConstantsEnum.getEnumByValue((short)(this.type.value + 1)));

                  try {
                     this.save();
                     VolaTile tile = Zones.getTileOrNull(this.getTileX(), this.getTileY(), this.surfaced);
                     if (tile != null) {
                        tile.updateFence(this);
                     }
                  } catch (IOException var6) {
                     logger.log(Level.WARNING, "Fence: " + this.number + " - " + var6.getMessage(), (Throwable)var6);
                  }
               }
            } else if (this.isMediumHedge()
               && this.type != StructureConstantsEnum.HEDGE_FLOWER3_MEDIUM
               && currTime - this.lastUsed > mediumHedgeGrowthInterval
               && Server.rand.nextInt(10) < 7) {
               this.setType(StructureConstantsEnum.getEnumByValue((short)(this.type.value + 1)));

               try {
                  this.save();
                  VolaTile tile = Zones.getTileOrNull(this.getTileX(), this.getTileY(), this.surfaced);
                  if (tile != null) {
                     tile.updateFence(this);
                  }
               } catch (IOException var5) {
                  logger.log(Level.WARNING, "Fence: " + this.number + " - " + var5.getMessage(), (Throwable)var5);
               }
            }
         }
      } else if (this.getType() == StructureConstantsEnum.FENCE_RUBBLE) {
         this.setDamage(this.damage + 4.0F);
      } else {
         Village v = this.getVillage();
         if (v != null) {
            if (v.moreThanMonthLeft()) {
               return;
            }

            if (!v.lessThanWeekLeft()) {
               mod = this.isFlowerbed() ? 2.0F : 10.0F;
            }
         } else if (Zones.getKingdom(this.tilex, this.tiley) == 0) {
            mod = 0.5F;
         }

         if ((float)(currTime - this.lastUsed) > 8.64E7F * mod) {
            this.setLastUsed(currTime);
            if (!this.deityFence && !this.hasNoDecay()) {
               this.setDamage(this.damage + 0.1F * this.getDamageModifier());
            }
         }
      }
   }

   public final boolean isOnPvPServer() {
      if (this.isHorizontal()) {
         if (Zones.isOnPvPServer(this.tilex, this.tiley)) {
            return true;
         }

         if (Zones.isOnPvPServer(this.tilex, this.tiley - 1)) {
            return true;
         }
      } else {
         if (Zones.isOnPvPServer(this.tilex, this.tiley)) {
            return true;
         }

         if (Zones.isOnPvPServer(this.tilex - 1, this.tiley)) {
            return true;
         }
      }

      return false;
   }

   public final int getNumber() {
      return this.number;
   }

   public final float getRepairedDamage() {
      return this.type != StructureConstantsEnum.FENCE_WOODEN_GATE
            && this.type != StructureConstantsEnum.FENCE_PLAN_WOODEN_GATE
            && this.type != StructureConstantsEnum.FENCE_PLAN_WOODEN
            && this.type != StructureConstantsEnum.FENCE_WOODEN
            && this.type != StructureConstantsEnum.FENCE_WOODEN_CRUDE_GATE
            && this.type != StructureConstantsEnum.FENCE_PLAN_WOODEN_GATE_CRUDE
         ? 10.0F
         : 40.0F;
   }

   @Override
   public final int getTileY() {
      return this.tiley;
   }

   @Override
   public final float getDamageModifier() {
      if (this.isFlowerbed()) {
         float mod = 5.0F * (this.damage / 100.0F);
         return 100.0F / Math.max(1.0F, this.currentQL * (100.0F - this.damage) / 100.0F) + mod;
      } else {
         return 100.0F / Math.max(1.0F, this.currentQL * (100.0F - this.damage) / 100.0F);
      }
   }

   @Override
   public final boolean isHorizontal() {
      return this.dir == 0;
   }

   public float getOriginalQualityLevel() {
      return this.originalQL;
   }

   public final VolaTile getTile() {
      try {
         Zone zone = Zones.getZone(this.tilex, this.tiley, this.surfaced);
         VolaTile toReturn = zone.getTileOrNull(this.tilex, this.tiley);
         if (toReturn != null) {
            if (toReturn.isTransition()) {
               return Zones.getZone(this.tilex, this.tiley, false).getOrCreateTile(this.tilex, this.tiley);
            }

            return toReturn;
         }

         logger.log(Level.WARNING, "Tile not in zone, this shouldn't happen " + this.tilex + ", " + this.tiley + ", fence: " + this.number);
      } catch (NoSuchZoneException var3) {
         logger.log(
            Level.WARNING, "This shouldn't happen " + this.tilex + ", " + this.tiley + ", fence: " + this.number + " - " + var3.getMessage(), (Throwable)var3
         );
      }

      return null;
   }

   private VolaTile getOtherTile() {
      if (this.isHorizontal()) {
         VolaTile toReturn = Zones.getOrCreateTile(this.tilex, this.tiley - 1, this.surfaced);
         return toReturn;
      } else {
         return Zones.getOrCreateTile(this.tilex - 1, this.tiley, this.surfaced);
      }
   }

   @Override
   public final boolean equals(StructureSupport support) {
      if (this == support) {
         return true;
      } else if (support == null) {
         return false;
      } else {
         return support.getId() == this.getId();
      }
   }

   @Override
   public final boolean equals(Object other) {
      if (this == other) {
         return true;
      } else if (other == null) {
         return false;
      } else if (this.getClass() != other.getClass()) {
         return false;
      } else {
         Fence support = (Fence)other;
         return support.getId() == this.getId();
      }
   }

   @Override
   public int hashCode() {
      int prime = 31;
      int result = 1;
      return 31 * result + (int)this.getId();
   }

   @Override
   public final long getId() {
      return Tiles.getBorderObjectId(this.getTileX(), this.getTileY(), this.getHeightOffset(), this.getLayer(), this.dir, (byte)7);
   }

   public boolean isTemporary() {
      return false;
   }

   public final long getOldId() {
      return ((long)this.dir << 48) + ((long)this.tilex << 32) + (long)(this.tiley << 16) + 7L;
   }

   public final void setType(StructureConstantsEnum aType) {
      this.type = aType;
      this.lastUsed = System.currentTimeMillis();
   }

   public final StructureConstantsEnum getType() {
      return this.type;
   }

   public final StructureStateEnum getState() {
      return this.state;
   }

   public final boolean isFinished() {
      return this.state.state >= this.getFinishState().state;
   }

   @Override
   public final float getPositionX() {
      return !this.isHorizontal() ? (float)(this.tilex * 4) : (float)(this.tilex * 4 + (this.tilex + 1) * 4) / 2.0F;
   }

   @Override
   public final float getPositionY() {
      return this.isHorizontal() ? (float)(this.tiley * 4) : (float)(this.tiley * 4 + (this.tiley + 1) * 4) / 2.0F;
   }

   public final boolean isBlocking(int blockType, Creature creature) {
      if (blockType != 5 || !this.isLowHedge() && !this.isLowFence()) {
         if (blockType != 4 || !this.isWalkthrough() && !this.isLowHedge()) {
            if (blockType == 6 && !WallConstants.isBlocking(this.getType())) {
               return false;
            } else {
               if (blockType == 6 || blockType == 8) {
                  if (!this.isFinished()) {
                     return false;
                  }

                  if (this.isDoor()) {
                     FenceGate gate = FenceGate.getFenceGate(this.getId());
                     if (gate != null && gate.canBeOpenedBy(creature, true)) {
                        return false;
                     }
                  }
               }

               return this.isFinished();
            }
         } else {
            return false;
         }
      } else {
         return false;
      }
   }

   @Override
   public final boolean canBeOpenedBy(Creature creature, boolean wentThroughDoor) {
      if (this.isWalkthrough()) {
         return true;
      } else if (this.isFinished()) {
         if (this.isDoor()) {
            FenceGate gate = FenceGate.getFenceGate(this.getId());
            if (gate != null && gate.canBeOpenedBy(creature, true)) {
               return true;
            }
         }

         return false;
      } else {
         return true;
      }
   }

   public static final StructureStateEnum getFinishState(StructureConstantsEnum fenceType) {
      switch(fenceType) {
         case FENCE_CURB:
         case FENCE_PLAN_CURB:
            return StructureStateEnum.STATE_6_NEEDED;
         case FENCE_WOVEN:
         case FENCE_ROPE_LOW:
         case FENCE_RUBBLE:
         case FLOWERBED_BLUE:
         case FLOWERBED_GREENISH_YELLOW:
         case FLOWERBED_ORANGE_RED:
         case FLOWERBED_PURPLE:
         case FLOWERBED_WHITE:
         case FLOWERBED_WHITE_DOTTED:
         case FLOWERBED_YELLOW:
         case FENCE_GARDESGARD_LOW:
         case FENCE_IRON:
         case FENCE_WOODEN:
         case FENCE_ROPE_HIGH:
         case FENCE_GARDESGARD_GATE:
         case FENCE_WOODEN_GATE:
         case FENCE_WOODEN_CRUDE_GATE:
         case FENCE_WOODEN_CRUDE:
         case FENCE_IRON_GATE:
         case HEDGE_FLOWER1_MEDIUM:
         case HEDGE_FLOWER2_MEDIUM:
         case HEDGE_FLOWER3_MEDIUM:
         case HEDGE_FLOWER4_MEDIUM:
         case HEDGE_FLOWER5_MEDIUM:
         case HEDGE_FLOWER6_MEDIUM:
         case HEDGE_FLOWER7_MEDIUM:
         case HEDGE_FLOWER1_HIGH:
         case HEDGE_FLOWER2_HIGH:
         case HEDGE_FLOWER3_HIGH:
         case HEDGE_FLOWER4_HIGH:
         case HEDGE_FLOWER5_HIGH:
         case HEDGE_FLOWER6_HIGH:
         case HEDGE_FLOWER7_HIGH:
         case FENCE_MAGIC_STONE:
         case FENCE_MAGIC_ICE:
         case FENCE_MAGIC_FIRE:
         default:
            if (isIron(fenceType)) {
               return StructureStateEnum.STATE_11_NEEDED;
            } else {
               if (isWoven(fenceType)) {
                  return StructureStateEnum.STATE_10_NEEDED;
               }

               return StructureStateEnum.STATE_4_NEEDED;
            }
         case FENCE_STONEWALL:
         case FENCE_STONE:
         case FENCE_PLAN_STONEWALL:
         case FENCE_PLAN_STONE:
         case FENCE_PLAN_SLATE:
         case FENCE_SLATE:
         case FENCE_PLAN_ROUNDED_STONE:
         case FENCE_ROUNDED_STONE:
         case FENCE_PLAN_POTTERY:
         case FENCE_POTTERY:
         case FENCE_PLAN_SANDSTONE:
         case FENCE_SANDSTONE:
         case FENCE_PLAN_MARBLE:
         case FENCE_MARBLE:
            return StructureStateEnum.STATE_10_NEEDED;
         case FENCE_STONE_PARAPET:
         case FENCE_STONE_IRON_PARAPET:
         case FENCE_SANDSTONE_STONE_PARAPET:
         case FENCE_SLATE_STONE_PARAPET:
         case FENCE_ROUNDED_STONE_STONE_PARAPET:
         case FENCE_RENDERED_STONE_PARAPET:
         case FENCE_POTTERY_STONE_PARAPET:
         case FENCE_MARBLE_STONE_PARAPET:
         case FENCE_PLAN_STONE_PARAPET:
         case FENCE_PLAN_STONE_IRON_PARAPET:
         case FENCE_PLAN_SLATE_STONE_PARAPET:
         case FENCE_PLAN_ROUNDED_STONE_STONE_PARAPET:
         case FENCE_PLAN_RENDERED_STONE_PARAPET:
         case FENCE_PLAN_POTTERY_STONE_PARAPET:
         case FENCE_PLAN_MARBLE_STONE_PARAPET:
            return StructureStateEnum.STATE_15_NEEDED;
         case FENCE_WOODEN_PARAPET:
         case FENCE_PLAN_WOODEN_PARAPET:
            return StructureStateEnum.STATE_15_NEEDED;
         case FENCE_PLAN_ROPE_HIGH:
            return StructureStateEnum.STATE_6_NEEDED;
         case FENCE_SLATE_CHAIN_FENCE:
         case FENCE_ROUNDED_STONE_CHAIN_FENCE:
         case FENCE_SANDSTONE_CHAIN_FENCE:
         case FENCE_RENDERED_CHAIN_FENCE:
         case FENCE_POTTERY_CHAIN_FENCE:
         case FENCE_MARBLE_CHAIN_FENCE:
         case FENCE_PLAN_MEDIUM_CHAIN:
         case FENCE_PLAN_SLATE_CHAIN_FENCE:
         case FENCE_PLAN_ROUNDED_STONE_CHAIN_FENCE:
         case FENCE_PLAN_SANDSTONE_CHAIN_FENCE:
         case FENCE_PLAN_RENDERED_CHAIN_FENCE:
         case FENCE_PLAN_POTTERY_CHAIN_FENCE:
         case FENCE_PLAN_MARBLE_CHAIN_FENCE:
            return StructureStateEnum.STATE_16_NEEDED;
         case FENCE_PLAN_PALISADE:
         case FENCE_PLAN_PALISADE_GATE:
         case FENCE_PALISADE:
         case FENCE_PALISADE_GATE:
            return StructureStateEnum.STATE_10_NEEDED;
         case FENCE_PLAN_STONEWALL_HIGH:
         case FENCE_STONEWALL_HIGH:
         case FENCE_PLAN_PORTCULLIS:
         case FENCE_SLATE_TALL_STONE_WALL:
         case FENCE_ROUNDED_STONE_TALL_STONE_WALL:
         case FENCE_SANDSTONE_TALL_STONE_WALL:
         case FENCE_RENDERED_TALL_STONE_WALL:
         case FENCE_POTTERY_TALL_STONE_WALL:
         case FENCE_MARBLE_TALL_STONE_WALL:
         case FENCE_SLATE_PORTCULLIS:
         case FENCE_ROUNDED_STONE_PORTCULLIS:
         case FENCE_SANDSTONE_PORTCULLIS:
         case FENCE_RENDERED_PORTCULLIS:
         case FENCE_POTTERY_PORTCULLIS:
         case FENCE_MARBLE_PORTCULLIS:
         case FENCE_PLAN_SLATE_TALL_STONE_WALL:
         case FENCE_PLAN_ROUNDED_STONE_TALL_STONE_WALL:
         case FENCE_PLAN_SANDSTONE_TALL_STONE_WALL:
         case FENCE_PLAN_RENDERED_TALL_STONE_WALL:
         case FENCE_PLAN_POTTERY_TALL_STONE_WALL:
         case FENCE_PLAN_MARBLE_TALL_STONE_WALL:
         case FENCE_PLAN_SLATE_PORTCULLIS:
         case FENCE_PLAN_ROUNDED_STONE_PORTCULLIS:
         case FENCE_PLAN_SANDSTONE_PORTCULLIS:
         case FENCE_PLAN_RENDERED_PORTCULLIS:
         case FENCE_PLAN_POTTERY_PORTCULLIS:
         case FENCE_PLAN_MARBLE_PORTCULLIS:
            return StructureStateEnum.STATE_20_NEEDED;
         case FENCE_PLAN_ROPE_LOW:
            return StructureStateEnum.STATE_4_NEEDED;
         case FENCE_PLAN_IRON_HIGH:
         case FENCE_PLAN_IRON_GATE_HIGH:
         case FENCE_SLATE_HIGH_IRON_FENCE_GATE:
         case FENCE_ROUNDED_STONE_HIGH_IRON_FENCE_GATE:
         case FENCE_SANDSTONE_HIGH_IRON_FENCE_GATE:
         case FENCE_RENDERED_HIGH_IRON_FENCE_GATE:
         case FENCE_POTTERY_HIGH_IRON_FENCE_GATE:
         case FENCE_MARBLE_HIGH_IRON_FENCE_GATE:
         case FENCE_SLATE_HIGH_IRON_FENCE:
         case FENCE_ROUNDED_STONE_HIGH_IRON_FENCE:
         case FENCE_SANDSTONE_HIGH_IRON_FENCE:
         case FENCE_RENDERED_HIGH_IRON_FENCE:
         case FENCE_POTTERY_HIGH_IRON_FENCE:
         case FENCE_MARBLE_HIGH_IRON_FENCE:
         case FENCE_PLAN_SLATE_HIGH_IRON_FENCE_GATE:
         case FENCE_PLAN_ROUNDED_STONE_HIGH_IRON_FENCE_GATE:
         case FENCE_PLAN_SANDSTONE_HIGH_IRON_FENCE_GATE:
         case FENCE_PLAN_RENDERED_HIGH_IRON_FENCE_GATE:
         case FENCE_PLAN_POTTERY_HIGH_IRON_FENCE_GATE:
         case FENCE_PLAN_MARBLE_HIGH_IRON_FENCE_GATE:
         case FENCE_PLAN_SLATE_HIGH_IRON_FENCE:
         case FENCE_PLAN_ROUNDED_STONE_HIGH_IRON_FENCE:
         case FENCE_PLAN_SANDSTONE_HIGH_IRON_FENCE:
         case FENCE_PLAN_RENDERED_HIGH_IRON_FENCE:
         case FENCE_PLAN_POTTERY_HIGH_IRON_FENCE:
         case FENCE_PLAN_MARBLE_HIGH_IRON_FENCE:
            return StructureStateEnum.STATE_18_NEEDED;
         case FENCE_PLAN_SANDSTONE_STONE_PARAPET:
            return StructureStateEnum.STATE_15_NEEDED;
      }
   }

   public final StructureStateEnum getFinishState() {
      return getFinishState(this.type);
   }

   public final boolean isPalisadeGate() {
      return this.type == StructureConstantsEnum.FENCE_PLAN_PALISADE_GATE;
   }

   public final void setState(StructureStateEnum newState) {
      if (this.state.state < this.getFinishState().state || newState == StructureStateEnum.INITIALIZED) {
         this.state = newState;
         if (this.state.state >= this.getFinishState().state) {
            this.state = StructureStateEnum.FINISHED;
         }

         if (this.state.state < 0) {
            this.state = StructureStateEnum.FINISHED;
            logger.log(Level.WARNING, "Finish state set to " + newState + " at ", (Throwable)(new Exception()));
         }
      }
   }

   public final boolean isSlate() {
      return this.type == StructureConstantsEnum.FENCE_SLATE
         || this.type == StructureConstantsEnum.FENCE_SLATE_IRON
         || this.type == StructureConstantsEnum.FENCE_SLATE_IRON_GATE
         || this.type == StructureConstantsEnum.FENCE_PLAN_SLATE
         || this.type == StructureConstantsEnum.FENCE_PLAN_SLATE_IRON
         || this.type == StructureConstantsEnum.FENCE_PLAN_SLATE_IRON_GATE
         || this.type == StructureConstantsEnum.FENCE_PLAN_SLATE_TALL_STONE_WALL
         || this.type == StructureConstantsEnum.FENCE_PLAN_SLATE_PORTCULLIS
         || this.type == StructureConstantsEnum.FENCE_PLAN_SLATE_HIGH_IRON_FENCE
         || this.type == StructureConstantsEnum.FENCE_PLAN_SLATE_HIGH_IRON_FENCE_GATE
         || this.type == StructureConstantsEnum.FENCE_PLAN_SLATE_STONE_PARAPET
         || this.type == StructureConstantsEnum.FENCE_PLAN_SLATE_CHAIN_FENCE
         || this.type == StructureConstantsEnum.FENCE_SLATE_TALL_STONE_WALL
         || this.type == StructureConstantsEnum.FENCE_SLATE_PORTCULLIS
         || this.type == StructureConstantsEnum.FENCE_SLATE_HIGH_IRON_FENCE
         || this.type == StructureConstantsEnum.FENCE_SLATE_HIGH_IRON_FENCE_GATE
         || this.type == StructureConstantsEnum.FENCE_SLATE_STONE_PARAPET
         || this.type == StructureConstantsEnum.FENCE_SLATE_CHAIN_FENCE;
   }

   public final boolean isRoundedStone() {
      return this.type == StructureConstantsEnum.FENCE_ROUNDED_STONE
         || this.type == StructureConstantsEnum.FENCE_ROUNDED_STONE_IRON
         || this.type == StructureConstantsEnum.FENCE_ROUNDED_STONE_IRON_GATE
         || this.type == StructureConstantsEnum.FENCE_PLAN_ROUNDED_STONE
         || this.type == StructureConstantsEnum.FENCE_PLAN_ROUNDED_STONE_IRON
         || this.type == StructureConstantsEnum.FENCE_PLAN_ROUNDED_STONE_IRON_GATE
         || this.type == StructureConstantsEnum.FENCE_ROUNDED_STONE_TALL_STONE_WALL
         || this.type == StructureConstantsEnum.FENCE_ROUNDED_STONE_PORTCULLIS
         || this.type == StructureConstantsEnum.FENCE_ROUNDED_STONE_HIGH_IRON_FENCE
         || this.type == StructureConstantsEnum.FENCE_ROUNDED_STONE_HIGH_IRON_FENCE_GATE
         || this.type == StructureConstantsEnum.FENCE_ROUNDED_STONE_STONE_PARAPET
         || this.type == StructureConstantsEnum.FENCE_ROUNDED_STONE_CHAIN_FENCE
         || this.type == StructureConstantsEnum.FENCE_PLAN_ROUNDED_STONE_TALL_STONE_WALL
         || this.type == StructureConstantsEnum.FENCE_PLAN_ROUNDED_STONE_PORTCULLIS
         || this.type == StructureConstantsEnum.FENCE_PLAN_ROUNDED_STONE_HIGH_IRON_FENCE
         || this.type == StructureConstantsEnum.FENCE_PLAN_ROUNDED_STONE_HIGH_IRON_FENCE_GATE
         || this.type == StructureConstantsEnum.FENCE_PLAN_ROUNDED_STONE_STONE_PARAPET
         || this.type == StructureConstantsEnum.FENCE_PLAN_ROUNDED_STONE_CHAIN_FENCE;
   }

   public final boolean isPottery() {
      return this.type == StructureConstantsEnum.FENCE_POTTERY
         || this.type == StructureConstantsEnum.FENCE_POTTERY_IRON
         || this.type == StructureConstantsEnum.FENCE_POTTERY_IRON_GATE
         || this.type == StructureConstantsEnum.FENCE_PLAN_POTTERY
         || this.type == StructureConstantsEnum.FENCE_PLAN_POTTERY_IRON
         || this.type == StructureConstantsEnum.FENCE_PLAN_POTTERY_IRON_GATE
         || this.type == StructureConstantsEnum.FENCE_PLAN_POTTERY_TALL_STONE_WALL
         || this.type == StructureConstantsEnum.FENCE_PLAN_POTTERY_PORTCULLIS
         || this.type == StructureConstantsEnum.FENCE_PLAN_POTTERY_HIGH_IRON_FENCE
         || this.type == StructureConstantsEnum.FENCE_PLAN_POTTERY_HIGH_IRON_FENCE_GATE
         || this.type == StructureConstantsEnum.FENCE_PLAN_POTTERY_STONE_PARAPET
         || this.type == StructureConstantsEnum.FENCE_PLAN_POTTERY_CHAIN_FENCE
         || this.type == StructureConstantsEnum.FENCE_POTTERY_TALL_STONE_WALL
         || this.type == StructureConstantsEnum.FENCE_POTTERY_PORTCULLIS
         || this.type == StructureConstantsEnum.FENCE_POTTERY_HIGH_IRON_FENCE
         || this.type == StructureConstantsEnum.FENCE_POTTERY_HIGH_IRON_FENCE_GATE
         || this.type == StructureConstantsEnum.FENCE_POTTERY_STONE_PARAPET
         || this.type == StructureConstantsEnum.FENCE_POTTERY_CHAIN_FENCE;
   }

   public final boolean isSandstone() {
      return this.type == StructureConstantsEnum.FENCE_SANDSTONE
         || this.type == StructureConstantsEnum.FENCE_SANDSTONE_IRON
         || this.type == StructureConstantsEnum.FENCE_SANDSTONE_IRON_GATE
         || this.type == StructureConstantsEnum.FENCE_PLAN_SANDSTONE
         || this.type == StructureConstantsEnum.FENCE_PLAN_SANDSTONE_IRON
         || this.type == StructureConstantsEnum.FENCE_PLAN_SANDSTONE_IRON_GATE
         || this.type == StructureConstantsEnum.FENCE_SANDSTONE_TALL_STONE_WALL
         || this.type == StructureConstantsEnum.FENCE_SANDSTONE_PORTCULLIS
         || this.type == StructureConstantsEnum.FENCE_SANDSTONE_HIGH_IRON_FENCE
         || this.type == StructureConstantsEnum.FENCE_SANDSTONE_HIGH_IRON_FENCE_GATE
         || this.type == StructureConstantsEnum.FENCE_SANDSTONE_STONE_PARAPET
         || this.type == StructureConstantsEnum.FENCE_SANDSTONE_CHAIN_FENCE
         || this.type == StructureConstantsEnum.FENCE_PLAN_SANDSTONE_TALL_STONE_WALL
         || this.type == StructureConstantsEnum.FENCE_PLAN_SANDSTONE_PORTCULLIS
         || this.type == StructureConstantsEnum.FENCE_PLAN_SANDSTONE_HIGH_IRON_FENCE
         || this.type == StructureConstantsEnum.FENCE_PLAN_SANDSTONE_HIGH_IRON_FENCE_GATE
         || this.type == StructureConstantsEnum.FENCE_PLAN_SANDSTONE_STONE_PARAPET
         || this.type == StructureConstantsEnum.FENCE_PLAN_SANDSTONE_CHAIN_FENCE;
   }

   public final boolean isPlasteredFence() {
      return this.type == StructureConstantsEnum.FENCE_RENDERED
         || this.type == StructureConstantsEnum.FENCE_RENDERED_IRON
         || this.type == StructureConstantsEnum.FENCE_RENDERED_IRON_GATE
         || this.type == StructureConstantsEnum.FENCE_RENDERED_TALL_STONE_WALL
         || this.type == StructureConstantsEnum.FENCE_RENDERED_PORTCULLIS
         || this.type == StructureConstantsEnum.FENCE_RENDERED_HIGH_IRON_FENCE
         || this.type == StructureConstantsEnum.FENCE_RENDERED_HIGH_IRON_FENCE_GATE
         || this.type == StructureConstantsEnum.FENCE_RENDERED_STONE_PARAPET
         || this.type == StructureConstantsEnum.FENCE_RENDERED_CHAIN_FENCE;
   }

   public final boolean isPlastered() {
      return this.type == StructureConstantsEnum.FENCE_RENDERED
         || this.type == StructureConstantsEnum.FENCE_RENDERED_IRON
         || this.type == StructureConstantsEnum.FENCE_RENDERED_IRON_GATE
         || this.type == StructureConstantsEnum.FENCE_PLAN_RENDERED
         || this.type == StructureConstantsEnum.FENCE_PLAN_RENDERED_IRON
         || this.type == StructureConstantsEnum.FENCE_PLAN_RENDERED_IRON_GATE
         || this.type == StructureConstantsEnum.FENCE_RENDERED_TALL_STONE_WALL
         || this.type == StructureConstantsEnum.FENCE_RENDERED_PORTCULLIS
         || this.type == StructureConstantsEnum.FENCE_RENDERED_HIGH_IRON_FENCE
         || this.type == StructureConstantsEnum.FENCE_RENDERED_HIGH_IRON_FENCE_GATE
         || this.type == StructureConstantsEnum.FENCE_RENDERED_STONE_PARAPET
         || this.type == StructureConstantsEnum.FENCE_RENDERED_CHAIN_FENCE
         || this.type == StructureConstantsEnum.FENCE_PLAN_RENDERED_TALL_STONE_WALL
         || this.type == StructureConstantsEnum.FENCE_PLAN_RENDERED_PORTCULLIS
         || this.type == StructureConstantsEnum.FENCE_PLAN_RENDERED_HIGH_IRON_FENCE
         || this.type == StructureConstantsEnum.FENCE_PLAN_RENDERED_HIGH_IRON_FENCE_GATE
         || this.type == StructureConstantsEnum.FENCE_PLAN_RENDERED_STONE_PARAPET
         || this.type == StructureConstantsEnum.FENCE_PLAN_RENDERED_CHAIN_FENCE;
   }

   public final boolean isMarble() {
      return this.type == StructureConstantsEnum.FENCE_MARBLE
         || this.type == StructureConstantsEnum.FENCE_MARBLE_IRON
         || this.type == StructureConstantsEnum.FENCE_MARBLE_IRON_GATE
         || this.type == StructureConstantsEnum.FENCE_PLAN_MARBLE
         || this.type == StructureConstantsEnum.FENCE_PLAN_MARBLE_IRON
         || this.type == StructureConstantsEnum.FENCE_PLAN_MARBLE_IRON_GATE
         || this.type == StructureConstantsEnum.FENCE_MARBLE_TALL_STONE_WALL
         || this.type == StructureConstantsEnum.FENCE_MARBLE_PORTCULLIS
         || this.type == StructureConstantsEnum.FENCE_MARBLE_HIGH_IRON_FENCE
         || this.type == StructureConstantsEnum.FENCE_MARBLE_HIGH_IRON_FENCE_GATE
         || this.type == StructureConstantsEnum.FENCE_MARBLE_STONE_PARAPET
         || this.type == StructureConstantsEnum.FENCE_MARBLE_CHAIN_FENCE
         || this.type == StructureConstantsEnum.FENCE_PLAN_MARBLE_TALL_STONE_WALL
         || this.type == StructureConstantsEnum.FENCE_PLAN_MARBLE_PORTCULLIS
         || this.type == StructureConstantsEnum.FENCE_PLAN_MARBLE_HIGH_IRON_FENCE
         || this.type == StructureConstantsEnum.FENCE_PLAN_MARBLE_HIGH_IRON_FENCE_GATE
         || this.type == StructureConstantsEnum.FENCE_PLAN_MARBLE_STONE_PARAPET
         || this.type == StructureConstantsEnum.FENCE_PLAN_MARBLE_CHAIN_FENCE;
   }

   public final boolean isStoneFence() {
      return this.type == StructureConstantsEnum.FENCE_STONE
         || this.type == StructureConstantsEnum.FENCE_IRON
         || this.type == StructureConstantsEnum.FENCE_IRON_GATE
         || this.type == StructureConstantsEnum.FENCE_STONEWALL_HIGH
         || this.type == StructureConstantsEnum.FENCE_IRON_HIGH
         || this.type == StructureConstantsEnum.FENCE_IRON_GATE_HIGH
         || this.type == StructureConstantsEnum.FENCE_STONE_PARAPET
         || this.type == StructureConstantsEnum.FENCE_PORTCULLIS
         || this.type == StructureConstantsEnum.FENCE_MEDIUM_CHAIN;
   }

   @Override
   public final boolean isStone() {
      return isStone(this.type);
   }

   public final boolean isRubble() {
      return this.type == StructureConstantsEnum.FENCE_RUBBLE;
   }

   public static boolean isStone(StructureConstantsEnum fenceType) {
      switch(fenceType) {
         case FENCE_CURB:
         case FENCE_STONEWALL:
         case FENCE_STONE_PARAPET:
         case FENCE_STONE_IRON_PARAPET:
         case FENCE_STONE:
         case FENCE_PLAN_STONEWALL_HIGH:
         case FENCE_STONEWALL_HIGH:
         case FENCE_PLAN_PORTCULLIS:
         case FENCE_SLATE_TALL_STONE_WALL:
         case FENCE_ROUNDED_STONE_TALL_STONE_WALL:
         case FENCE_SANDSTONE_TALL_STONE_WALL:
         case FENCE_RENDERED_TALL_STONE_WALL:
         case FENCE_POTTERY_TALL_STONE_WALL:
         case FENCE_MARBLE_TALL_STONE_WALL:
         case FENCE_SLATE_PORTCULLIS:
         case FENCE_ROUNDED_STONE_PORTCULLIS:
         case FENCE_SANDSTONE_PORTCULLIS:
         case FENCE_RENDERED_PORTCULLIS:
         case FENCE_POTTERY_PORTCULLIS:
         case FENCE_MARBLE_PORTCULLIS:
         case FENCE_PLAN_SLATE_TALL_STONE_WALL:
         case FENCE_PLAN_ROUNDED_STONE_TALL_STONE_WALL:
         case FENCE_PLAN_SANDSTONE_TALL_STONE_WALL:
         case FENCE_PLAN_RENDERED_TALL_STONE_WALL:
         case FENCE_PLAN_POTTERY_TALL_STONE_WALL:
         case FENCE_PLAN_MARBLE_TALL_STONE_WALL:
         case FENCE_PLAN_SLATE_PORTCULLIS:
         case FENCE_PLAN_ROUNDED_STONE_PORTCULLIS:
         case FENCE_PLAN_SANDSTONE_PORTCULLIS:
         case FENCE_PLAN_RENDERED_PORTCULLIS:
         case FENCE_PLAN_POTTERY_PORTCULLIS:
         case FENCE_PLAN_MARBLE_PORTCULLIS:
         case FENCE_PLAN_STONEWALL:
         case FENCE_PLAN_STONE:
         case FENCE_SLATE:
         case FENCE_ROUNDED_STONE:
         case FENCE_POTTERY:
         case FENCE_SANDSTONE:
         case FENCE_MARBLE:
         case FENCE_PLAN_STONE_PARAPET:
         case FENCE_PLAN_STONE_IRON_PARAPET:
         case FENCE_PLAN_CURB:
         case FENCE_PORTCULLIS:
         case FENCE_RENDERED:
            return true;
         case FENCE_WOVEN:
         case FENCE_ROPE_LOW:
         case FENCE_RUBBLE:
         case FLOWERBED_BLUE:
         case FLOWERBED_GREENISH_YELLOW:
         case FLOWERBED_ORANGE_RED:
         case FLOWERBED_PURPLE:
         case FLOWERBED_WHITE:
         case FLOWERBED_WHITE_DOTTED:
         case FLOWERBED_YELLOW:
         case FENCE_GARDESGARD_LOW:
         case FENCE_IRON:
         case FENCE_WOODEN:
         case FENCE_WOODEN_PARAPET:
         case FENCE_ROPE_HIGH:
         case FENCE_PLAN_ROPE_HIGH:
         case FENCE_GARDESGARD_GATE:
         case FENCE_WOODEN_GATE:
         case FENCE_WOODEN_CRUDE_GATE:
         case FENCE_WOODEN_CRUDE:
         case FENCE_IRON_GATE:
         case FENCE_SANDSTONE_STONE_PARAPET:
         case FENCE_SLATE_STONE_PARAPET:
         case FENCE_ROUNDED_STONE_STONE_PARAPET:
         case FENCE_RENDERED_STONE_PARAPET:
         case FENCE_POTTERY_STONE_PARAPET:
         case FENCE_MARBLE_STONE_PARAPET:
         case FENCE_SLATE_CHAIN_FENCE:
         case FENCE_ROUNDED_STONE_CHAIN_FENCE:
         case FENCE_SANDSTONE_CHAIN_FENCE:
         case FENCE_RENDERED_CHAIN_FENCE:
         case FENCE_POTTERY_CHAIN_FENCE:
         case FENCE_MARBLE_CHAIN_FENCE:
         case HEDGE_FLOWER1_MEDIUM:
         case HEDGE_FLOWER2_MEDIUM:
         case HEDGE_FLOWER3_MEDIUM:
         case HEDGE_FLOWER4_MEDIUM:
         case HEDGE_FLOWER5_MEDIUM:
         case HEDGE_FLOWER6_MEDIUM:
         case HEDGE_FLOWER7_MEDIUM:
         case HEDGE_FLOWER1_HIGH:
         case HEDGE_FLOWER2_HIGH:
         case HEDGE_FLOWER3_HIGH:
         case HEDGE_FLOWER4_HIGH:
         case HEDGE_FLOWER5_HIGH:
         case HEDGE_FLOWER6_HIGH:
         case HEDGE_FLOWER7_HIGH:
         case FENCE_MAGIC_STONE:
         case FENCE_MAGIC_ICE:
         case FENCE_MAGIC_FIRE:
         case FENCE_PLAN_PALISADE:
         case FENCE_PLAN_PALISADE_GATE:
         case FENCE_PALISADE:
         case FENCE_PALISADE_GATE:
         case FENCE_PLAN_SLATE:
         case FENCE_PLAN_ROUNDED_STONE:
         case FENCE_PLAN_POTTERY:
         case FENCE_PLAN_SANDSTONE:
         case FENCE_PLAN_MARBLE:
         case FENCE_PLAN_WOODEN_PARAPET:
         case FENCE_PLAN_SLATE_STONE_PARAPET:
         case FENCE_PLAN_ROUNDED_STONE_STONE_PARAPET:
         case FENCE_PLAN_RENDERED_STONE_PARAPET:
         case FENCE_PLAN_POTTERY_STONE_PARAPET:
         case FENCE_PLAN_MARBLE_STONE_PARAPET:
         case FENCE_PLAN_MEDIUM_CHAIN:
         case FENCE_PLAN_SLATE_CHAIN_FENCE:
         case FENCE_PLAN_ROUNDED_STONE_CHAIN_FENCE:
         case FENCE_PLAN_SANDSTONE_CHAIN_FENCE:
         case FENCE_PLAN_RENDERED_CHAIN_FENCE:
         case FENCE_PLAN_POTTERY_CHAIN_FENCE:
         case FENCE_PLAN_MARBLE_CHAIN_FENCE:
         case FENCE_PLAN_ROPE_LOW:
         case FENCE_PLAN_IRON_HIGH:
         case FENCE_PLAN_IRON_GATE_HIGH:
         case FENCE_SLATE_HIGH_IRON_FENCE_GATE:
         case FENCE_ROUNDED_STONE_HIGH_IRON_FENCE_GATE:
         case FENCE_SANDSTONE_HIGH_IRON_FENCE_GATE:
         case FENCE_RENDERED_HIGH_IRON_FENCE_GATE:
         case FENCE_POTTERY_HIGH_IRON_FENCE_GATE:
         case FENCE_MARBLE_HIGH_IRON_FENCE_GATE:
         case FENCE_SLATE_HIGH_IRON_FENCE:
         case FENCE_ROUNDED_STONE_HIGH_IRON_FENCE:
         case FENCE_SANDSTONE_HIGH_IRON_FENCE:
         case FENCE_RENDERED_HIGH_IRON_FENCE:
         case FENCE_POTTERY_HIGH_IRON_FENCE:
         case FENCE_MARBLE_HIGH_IRON_FENCE:
         case FENCE_PLAN_SLATE_HIGH_IRON_FENCE_GATE:
         case FENCE_PLAN_ROUNDED_STONE_HIGH_IRON_FENCE_GATE:
         case FENCE_PLAN_SANDSTONE_HIGH_IRON_FENCE_GATE:
         case FENCE_PLAN_RENDERED_HIGH_IRON_FENCE_GATE:
         case FENCE_PLAN_POTTERY_HIGH_IRON_FENCE_GATE:
         case FENCE_PLAN_MARBLE_HIGH_IRON_FENCE_GATE:
         case FENCE_PLAN_SLATE_HIGH_IRON_FENCE:
         case FENCE_PLAN_ROUNDED_STONE_HIGH_IRON_FENCE:
         case FENCE_PLAN_SANDSTONE_HIGH_IRON_FENCE:
         case FENCE_PLAN_RENDERED_HIGH_IRON_FENCE:
         case FENCE_PLAN_POTTERY_HIGH_IRON_FENCE:
         case FENCE_PLAN_MARBLE_HIGH_IRON_FENCE:
         case FENCE_PLAN_SANDSTONE_STONE_PARAPET:
         default:
            return false;
      }
   }

   @Override
   public final boolean isWood() {
      return isWood(this.type);
   }

   public static final boolean isWood(StructureConstantsEnum fenceType) {
      switch(fenceType) {
         case FENCE_WOVEN:
         case FENCE_ROPE_LOW:
         case FENCE_GARDESGARD_LOW:
         case FENCE_WOODEN:
         case FENCE_WOODEN_PARAPET:
         case FENCE_ROPE_HIGH:
         case FENCE_PLAN_ROPE_HIGH:
         case FENCE_GARDESGARD_GATE:
         case FENCE_WOODEN_GATE:
         case FENCE_WOODEN_CRUDE_GATE:
         case FENCE_WOODEN_CRUDE:
         case FENCE_PLAN_PALISADE:
         case FENCE_PLAN_PALISADE_GATE:
         case FENCE_PALISADE:
         case FENCE_PALISADE_GATE:
         case FENCE_PLAN_WOODEN_PARAPET:
         case FENCE_PLAN_ROPE_LOW:
         case FENCE_PLAN_WOODEN:
         case FENCE_PLAN_WOODEN_GATE:
         case FENCE_PLAN_WOODEN_CRUDE:
         case FENCE_PLAN_WOODEN_GATE_CRUDE:
         case FENCE_PLAN_GARDESGARD_GATE:
         case FENCE_GARDESGARD_HIGH:
         case FENCE_PLAN_GARDESGARD_HIGH:
         case FENCE_PLAN_GARDESGARD_LOW:
         case FENCE_PLAN_WOVEN:
         case FENCE_SIEGEWALL:
            return true;
         default:
            return isHedge(fenceType);
      }
   }

   @Override
   public final boolean isMetal() {
      return this.type == StructureConstantsEnum.FENCE_IRON
         || this.type == StructureConstantsEnum.FENCE_IRON_GATE
         || this.type == StructureConstantsEnum.FENCE_IRON_HIGH
         || this.type == StructureConstantsEnum.FENCE_IRON_GATE_HIGH
         || this.type == StructureConstantsEnum.FENCE_MEDIUM_CHAIN
         || this.type == StructureConstantsEnum.FENCE_MARBLE_HIGH_IRON_FENCE
         || this.type == StructureConstantsEnum.FENCE_MARBLE_HIGH_IRON_FENCE_GATE
         || this.type == StructureConstantsEnum.FENCE_MARBLE_CHAIN_FENCE
         || this.type == StructureConstantsEnum.FENCE_SLATE_HIGH_IRON_FENCE
         || this.type == StructureConstantsEnum.FENCE_SLATE_HIGH_IRON_FENCE_GATE
         || this.type == StructureConstantsEnum.FENCE_SLATE_CHAIN_FENCE
         || this.type == StructureConstantsEnum.FENCE_POTTERY_HIGH_IRON_FENCE
         || this.type == StructureConstantsEnum.FENCE_POTTERY_HIGH_IRON_FENCE_GATE
         || this.type == StructureConstantsEnum.FENCE_POTTERY_CHAIN_FENCE
         || this.type == StructureConstantsEnum.FENCE_RENDERED_HIGH_IRON_FENCE
         || this.type == StructureConstantsEnum.FENCE_RENDERED_HIGH_IRON_FENCE_GATE
         || this.type == StructureConstantsEnum.FENCE_RENDERED_CHAIN_FENCE
         || this.type == StructureConstantsEnum.FENCE_ROUNDED_STONE_HIGH_IRON_FENCE
         || this.type == StructureConstantsEnum.FENCE_ROUNDED_STONE_HIGH_IRON_FENCE_GATE
         || this.type == StructureConstantsEnum.FENCE_ROUNDED_STONE_CHAIN_FENCE
         || this.type == StructureConstantsEnum.FENCE_SANDSTONE_HIGH_IRON_FENCE
         || this.type == StructureConstantsEnum.FENCE_SANDSTONE_HIGH_IRON_FENCE_GATE
         || this.type == StructureConstantsEnum.FENCE_SANDSTONE_CHAIN_FENCE;
   }

   public static final boolean isWoven(StructureConstantsEnum fenceType) {
      switch(fenceType) {
         case FENCE_WOVEN:
         case FENCE_PLAN_WOVEN:
            return true;
         default:
            return false;
      }
   }

   public final boolean isWoven() {
      return isWoven(this.type);
   }

   public static final short[] getAllLowHedgeTypes() {
      return new short[]{
         StructureConstantsEnum.HEDGE_FLOWER1_LOW.value,
         StructureConstantsEnum.HEDGE_FLOWER2_LOW.value,
         StructureConstantsEnum.HEDGE_FLOWER3_LOW.value,
         StructureConstantsEnum.HEDGE_FLOWER4_LOW.value,
         StructureConstantsEnum.HEDGE_FLOWER5_LOW.value,
         StructureConstantsEnum.HEDGE_FLOWER6_LOW.value,
         StructureConstantsEnum.HEDGE_FLOWER7_LOW.value
      };
   }

   public static final StructureConstantsEnum getLowHedgeType(byte treeMaterial) {
      switch(treeMaterial) {
         case 39:
            return StructureConstantsEnum.HEDGE_FLOWER6_LOW;
         case 40:
         case 42:
         case 43:
         case 44:
         case 45:
         case 49:
         default:
            return StructureConstantsEnum.FENCE_PLAN_WOODEN;
         case 41:
            return StructureConstantsEnum.HEDGE_FLOWER7_LOW;
         case 46:
            return StructureConstantsEnum.HEDGE_FLOWER1_LOW;
         case 47:
            return StructureConstantsEnum.HEDGE_FLOWER4_LOW;
         case 48:
            return StructureConstantsEnum.HEDGE_FLOWER5_LOW;
         case 50:
            return StructureConstantsEnum.HEDGE_FLOWER3_LOW;
         case 51:
            return StructureConstantsEnum.HEDGE_FLOWER2_LOW;
      }
   }

   public static final byte getMaterialForLowHedge(StructureConstantsEnum hedgeType) {
      switch(hedgeType) {
         case HEDGE_FLOWER1_LOW:
            return 46;
         case HEDGE_FLOWER2_LOW:
            return 51;
         case HEDGE_FLOWER3_LOW:
            return 50;
         case HEDGE_FLOWER4_LOW:
            return 47;
         case HEDGE_FLOWER5_LOW:
            return 48;
         case HEDGE_FLOWER6_LOW:
            return 39;
         case HEDGE_FLOWER7_LOW:
            return 41;
         default:
            return 46;
      }
   }

   public static final short[] getAllFlowerbeds() {
      return new short[]{
         StructureConstantsEnum.FLOWERBED_YELLOW.value,
         StructureConstantsEnum.FLOWERBED_ORANGE_RED.value,
         StructureConstantsEnum.FLOWERBED_PURPLE.value,
         StructureConstantsEnum.FLOWERBED_WHITE.value,
         StructureConstantsEnum.FLOWERBED_BLUE.value,
         StructureConstantsEnum.FLOWERBED_GREENISH_YELLOW.value,
         StructureConstantsEnum.FLOWERBED_WHITE_DOTTED.value
      };
   }

   public static final int getFlowerTypeByFlowerbedType(StructureConstantsEnum flowerbedType) {
      switch(flowerbedType) {
         case FLOWERBED_BLUE:
            return 502;
         case FLOWERBED_GREENISH_YELLOW:
            return 503;
         case FLOWERBED_ORANGE_RED:
            return 499;
         case FLOWERBED_PURPLE:
            return 500;
         case FLOWERBED_WHITE:
            return 501;
         case FLOWERBED_WHITE_DOTTED:
            return 504;
         case FLOWERBED_YELLOW:
            return 498;
         default:
            return 498;
      }
   }

   public static final StructureConstantsEnum getFlowerbedType(int templateId) {
      switch(templateId) {
         case 498:
            return StructureConstantsEnum.FLOWERBED_YELLOW;
         case 499:
            return StructureConstantsEnum.FLOWERBED_ORANGE_RED;
         case 500:
            return StructureConstantsEnum.FLOWERBED_PURPLE;
         case 501:
            return StructureConstantsEnum.FLOWERBED_WHITE;
         case 502:
            return StructureConstantsEnum.FLOWERBED_BLUE;
         case 503:
            return StructureConstantsEnum.FLOWERBED_GREENISH_YELLOW;
         case 504:
            return StructureConstantsEnum.FLOWERBED_WHITE_DOTTED;
         default:
            return StructureConstantsEnum.FENCE_PLAN_WOODEN;
      }
   }

   public static final StructureConstantsEnum getFencePlanType(int action) {
      if (action == 166) {
         return StructureConstantsEnum.FENCE_PLAN_WOODEN;
      } else if (action == 520) {
         return StructureConstantsEnum.FENCE_PLAN_WOODEN_CRUDE;
      } else if (action == 528) {
         return StructureConstantsEnum.FENCE_PLAN_WOODEN_GATE_CRUDE;
      } else if (action == 526) {
         return StructureConstantsEnum.FENCE_PLAN_GARDESGARD_LOW;
      } else if (action == 527) {
         return StructureConstantsEnum.FENCE_PLAN_GARDESGARD_HIGH;
      } else if (action == 529) {
         return StructureConstantsEnum.FENCE_PLAN_GARDESGARD_GATE;
      } else if (action == 165) {
         return StructureConstantsEnum.FENCE_PLAN_PALISADE;
      } else if (action == 163) {
         return StructureConstantsEnum.FENCE_PLAN_STONEWALL;
      } else if (action == 167) {
         return StructureConstantsEnum.FENCE_PLAN_PALISADE_GATE;
      } else if (action == 168) {
         return StructureConstantsEnum.FENCE_PLAN_WOODEN_GATE;
      } else if (action == 164) {
         return StructureConstantsEnum.FENCE_PLAN_STONEWALL_HIGH;
      } else if (action == 477) {
         return StructureConstantsEnum.FENCE_PLAN_IRON;
      } else if (action == 478) {
         return StructureConstantsEnum.FENCE_PLAN_WOVEN;
      } else if (action == 479) {
         return StructureConstantsEnum.FENCE_PLAN_IRON_GATE;
      } else if (action == 516) {
         return StructureConstantsEnum.FENCE_PLAN_WOODEN_PARAPET;
      } else if (action == 517) {
         return StructureConstantsEnum.FENCE_PLAN_STONE_PARAPET;
      } else if (action == 521) {
         return StructureConstantsEnum.FENCE_PLAN_STONE_IRON_PARAPET;
      } else if (action == 541) {
         return StructureConstantsEnum.FENCE_PLAN_STONE;
      } else if (action == 542) {
         return StructureConstantsEnum.FENCE_PLAN_CURB;
      } else if (action == 543) {
         return StructureConstantsEnum.FENCE_PLAN_ROPE_LOW;
      } else if (action == 544) {
         return StructureConstantsEnum.FENCE_PLAN_ROPE_HIGH;
      } else if (action == 545) {
         return StructureConstantsEnum.FENCE_PLAN_IRON_HIGH;
      } else if (action == 546) {
         return StructureConstantsEnum.FENCE_PLAN_IRON_GATE_HIGH;
      } else if (action == 611) {
         return StructureConstantsEnum.FENCE_PLAN_MEDIUM_CHAIN;
      } else if (action == 654) {
         return StructureConstantsEnum.FENCE_PLAN_PORTCULLIS;
      } else if (action == 832) {
         return StructureConstantsEnum.FENCE_PLAN_SLATE;
      } else if (action == 835) {
         return StructureConstantsEnum.FENCE_PLAN_ROUNDED_STONE;
      } else if (action == 838) {
         return StructureConstantsEnum.FENCE_PLAN_POTTERY;
      } else if (action == 841) {
         return StructureConstantsEnum.FENCE_PLAN_SANDSTONE;
      } else if (action == 844) {
         return StructureConstantsEnum.FENCE_PLAN_MARBLE;
      } else if (action == 833) {
         return StructureConstantsEnum.FENCE_PLAN_SLATE_IRON;
      } else if (action == 836) {
         return StructureConstantsEnum.FENCE_PLAN_ROUNDED_STONE_IRON;
      } else if (action == 839) {
         return StructureConstantsEnum.FENCE_PLAN_POTTERY_IRON;
      } else if (action == 842) {
         return StructureConstantsEnum.FENCE_PLAN_SANDSTONE_IRON;
      } else if (action == 845) {
         return StructureConstantsEnum.FENCE_PLAN_MARBLE_IRON;
      } else if (action == 834) {
         return StructureConstantsEnum.FENCE_PLAN_SLATE_IRON_GATE;
      } else if (action == 837) {
         return StructureConstantsEnum.FENCE_PLAN_ROUNDED_STONE_IRON_GATE;
      } else if (action == 840) {
         return StructureConstantsEnum.FENCE_PLAN_POTTERY_IRON_GATE;
      } else if (action == 843) {
         return StructureConstantsEnum.FENCE_PLAN_SANDSTONE_IRON_GATE;
      } else if (action == 846) {
         return StructureConstantsEnum.FENCE_PLAN_MARBLE_IRON_GATE;
      } else if (action == 870) {
         return StructureConstantsEnum.FENCE_PLAN_SLATE_TALL_STONE_WALL;
      } else if (action == 871) {
         return StructureConstantsEnum.FENCE_PLAN_SLATE_PORTCULLIS;
      } else if (action == 872) {
         return StructureConstantsEnum.FENCE_PLAN_SLATE_HIGH_IRON_FENCE;
      } else if (action == 873) {
         return StructureConstantsEnum.FENCE_PLAN_SLATE_HIGH_IRON_FENCE_GATE;
      } else if (action == 874) {
         return StructureConstantsEnum.FENCE_PLAN_SLATE_STONE_PARAPET;
      } else if (action == 875) {
         return StructureConstantsEnum.FENCE_PLAN_SLATE_CHAIN_FENCE;
      } else if (action == 876) {
         return StructureConstantsEnum.FENCE_PLAN_ROUNDED_STONE_TALL_STONE_WALL;
      } else if (action == 877) {
         return StructureConstantsEnum.FENCE_PLAN_ROUNDED_STONE_PORTCULLIS;
      } else if (action == 878) {
         return StructureConstantsEnum.FENCE_PLAN_ROUNDED_STONE_HIGH_IRON_FENCE;
      } else if (action == 879) {
         return StructureConstantsEnum.FENCE_PLAN_ROUNDED_STONE_HIGH_IRON_FENCE_GATE;
      } else if (action == 880) {
         return StructureConstantsEnum.FENCE_PLAN_ROUNDED_STONE_STONE_PARAPET;
      } else if (action == 881) {
         return StructureConstantsEnum.FENCE_PLAN_ROUNDED_STONE_CHAIN_FENCE;
      } else if (action == 882) {
         return StructureConstantsEnum.FENCE_PLAN_SANDSTONE_TALL_STONE_WALL;
      } else if (action == 883) {
         return StructureConstantsEnum.FENCE_PLAN_SANDSTONE_PORTCULLIS;
      } else if (action == 884) {
         return StructureConstantsEnum.FENCE_PLAN_SANDSTONE_HIGH_IRON_FENCE;
      } else if (action == 885) {
         return StructureConstantsEnum.FENCE_PLAN_SANDSTONE_HIGH_IRON_FENCE_GATE;
      } else if (action == 886) {
         return StructureConstantsEnum.FENCE_PLAN_SANDSTONE_STONE_PARAPET;
      } else if (action == 887) {
         return StructureConstantsEnum.FENCE_PLAN_SANDSTONE_CHAIN_FENCE;
      } else if (action == 888) {
         return StructureConstantsEnum.FENCE_PLAN_RENDERED_TALL_STONE_WALL;
      } else if (action == 889) {
         return StructureConstantsEnum.FENCE_PLAN_RENDERED_PORTCULLIS;
      } else if (action == 890) {
         return StructureConstantsEnum.FENCE_PLAN_RENDERED_HIGH_IRON_FENCE;
      } else if (action == 891) {
         return StructureConstantsEnum.FENCE_PLAN_RENDERED_HIGH_IRON_FENCE_GATE;
      } else if (action == 892) {
         return StructureConstantsEnum.FENCE_PLAN_RENDERED_STONE_PARAPET;
      } else if (action == 893) {
         return StructureConstantsEnum.FENCE_PLAN_RENDERED_CHAIN_FENCE;
      } else if (action == 894) {
         return StructureConstantsEnum.FENCE_PLAN_POTTERY_TALL_STONE_WALL;
      } else if (action == 895) {
         return StructureConstantsEnum.FENCE_PLAN_POTTERY_PORTCULLIS;
      } else if (action == 896) {
         return StructureConstantsEnum.FENCE_PLAN_POTTERY_HIGH_IRON_FENCE;
      } else if (action == 897) {
         return StructureConstantsEnum.FENCE_PLAN_POTTERY_HIGH_IRON_FENCE_GATE;
      } else if (action == 898) {
         return StructureConstantsEnum.FENCE_PLAN_POTTERY_STONE_PARAPET;
      } else if (action == 899) {
         return StructureConstantsEnum.FENCE_PLAN_POTTERY_CHAIN_FENCE;
      } else if (action == 900) {
         return StructureConstantsEnum.FENCE_PLAN_MARBLE_TALL_STONE_WALL;
      } else if (action == 901) {
         return StructureConstantsEnum.FENCE_PLAN_MARBLE_PORTCULLIS;
      } else if (action == 902) {
         return StructureConstantsEnum.FENCE_PLAN_MARBLE_HIGH_IRON_FENCE;
      } else if (action == 903) {
         return StructureConstantsEnum.FENCE_PLAN_MARBLE_HIGH_IRON_FENCE_GATE;
      } else if (action == 904) {
         return StructureConstantsEnum.FENCE_PLAN_MARBLE_STONE_PARAPET;
      } else if (action == 905) {
         return StructureConstantsEnum.FENCE_PLAN_MARBLE_CHAIN_FENCE;
      } else {
         logger.log(Level.WARNING, "Fence plan for action " + action + " is not found!", (Throwable)(new Exception()));
         return StructureConstantsEnum.FENCE_PLAN_WOODEN;
      }
   }

   public static final StructureConstantsEnum getFencePlanForType(StructureConstantsEnum type) {
      switch(type) {
         case FENCE_CURB:
            return StructureConstantsEnum.FENCE_PLAN_CURB;
         case FENCE_WOVEN:
            return StructureConstantsEnum.FENCE_PLAN_WOVEN;
         case FENCE_ROPE_LOW:
            return StructureConstantsEnum.FENCE_PLAN_ROPE_LOW;
         case FENCE_RUBBLE:
         case FLOWERBED_BLUE:
         case FLOWERBED_GREENISH_YELLOW:
         case FLOWERBED_ORANGE_RED:
         case FLOWERBED_PURPLE:
         case FLOWERBED_WHITE:
         case FLOWERBED_WHITE_DOTTED:
         case FLOWERBED_YELLOW:
         case FENCE_PLAN_ROPE_HIGH:
         case HEDGE_FLOWER1_MEDIUM:
         case HEDGE_FLOWER2_MEDIUM:
         case HEDGE_FLOWER3_MEDIUM:
         case HEDGE_FLOWER4_MEDIUM:
         case HEDGE_FLOWER5_MEDIUM:
         case HEDGE_FLOWER6_MEDIUM:
         case HEDGE_FLOWER7_MEDIUM:
         case HEDGE_FLOWER1_HIGH:
         case HEDGE_FLOWER2_HIGH:
         case HEDGE_FLOWER3_HIGH:
         case HEDGE_FLOWER4_HIGH:
         case HEDGE_FLOWER5_HIGH:
         case HEDGE_FLOWER6_HIGH:
         case HEDGE_FLOWER7_HIGH:
         case FENCE_MAGIC_STONE:
         case FENCE_MAGIC_ICE:
         case FENCE_MAGIC_FIRE:
         case FENCE_PLAN_PALISADE:
         case FENCE_PLAN_PALISADE_GATE:
         case FENCE_PLAN_STONEWALL_HIGH:
         case FENCE_PLAN_PORTCULLIS:
         case FENCE_PLAN_SLATE_TALL_STONE_WALL:
         case FENCE_PLAN_ROUNDED_STONE_TALL_STONE_WALL:
         case FENCE_PLAN_SANDSTONE_TALL_STONE_WALL:
         case FENCE_PLAN_RENDERED_TALL_STONE_WALL:
         case FENCE_PLAN_POTTERY_TALL_STONE_WALL:
         case FENCE_PLAN_MARBLE_TALL_STONE_WALL:
         case FENCE_PLAN_SLATE_PORTCULLIS:
         case FENCE_PLAN_ROUNDED_STONE_PORTCULLIS:
         case FENCE_PLAN_SANDSTONE_PORTCULLIS:
         case FENCE_PLAN_RENDERED_PORTCULLIS:
         case FENCE_PLAN_POTTERY_PORTCULLIS:
         case FENCE_PLAN_MARBLE_PORTCULLIS:
         case FENCE_PLAN_STONEWALL:
         case FENCE_PLAN_STONE:
         case FENCE_PLAN_SLATE:
         case FENCE_PLAN_ROUNDED_STONE:
         case FENCE_PLAN_POTTERY:
         case FENCE_PLAN_SANDSTONE:
         case FENCE_PLAN_MARBLE:
         case FENCE_PLAN_WOODEN_PARAPET:
         case FENCE_PLAN_STONE_PARAPET:
         case FENCE_PLAN_STONE_IRON_PARAPET:
         case FENCE_PLAN_SLATE_STONE_PARAPET:
         case FENCE_PLAN_ROUNDED_STONE_STONE_PARAPET:
         case FENCE_PLAN_RENDERED_STONE_PARAPET:
         case FENCE_PLAN_POTTERY_STONE_PARAPET:
         case FENCE_PLAN_MARBLE_STONE_PARAPET:
         case FENCE_PLAN_MEDIUM_CHAIN:
         case FENCE_PLAN_SLATE_CHAIN_FENCE:
         case FENCE_PLAN_ROUNDED_STONE_CHAIN_FENCE:
         case FENCE_PLAN_SANDSTONE_CHAIN_FENCE:
         case FENCE_PLAN_RENDERED_CHAIN_FENCE:
         case FENCE_PLAN_POTTERY_CHAIN_FENCE:
         case FENCE_PLAN_MARBLE_CHAIN_FENCE:
         case FENCE_PLAN_CURB:
         case FENCE_PLAN_ROPE_LOW:
         case FENCE_PLAN_IRON_HIGH:
         case FENCE_PLAN_IRON_GATE_HIGH:
         case FENCE_PLAN_SLATE_HIGH_IRON_FENCE_GATE:
         case FENCE_PLAN_ROUNDED_STONE_HIGH_IRON_FENCE_GATE:
         case FENCE_PLAN_SANDSTONE_HIGH_IRON_FENCE_GATE:
         case FENCE_PLAN_RENDERED_HIGH_IRON_FENCE_GATE:
         case FENCE_PLAN_POTTERY_HIGH_IRON_FENCE_GATE:
         case FENCE_PLAN_MARBLE_HIGH_IRON_FENCE_GATE:
         case FENCE_PLAN_SLATE_HIGH_IRON_FENCE:
         case FENCE_PLAN_ROUNDED_STONE_HIGH_IRON_FENCE:
         case FENCE_PLAN_SANDSTONE_HIGH_IRON_FENCE:
         case FENCE_PLAN_RENDERED_HIGH_IRON_FENCE:
         case FENCE_PLAN_POTTERY_HIGH_IRON_FENCE:
         case FENCE_PLAN_MARBLE_HIGH_IRON_FENCE:
         case FENCE_PLAN_SANDSTONE_STONE_PARAPET:
         case FENCE_PLAN_WOODEN:
         case FENCE_PLAN_WOODEN_GATE:
         case FENCE_PLAN_WOODEN_CRUDE:
         case FENCE_PLAN_WOODEN_GATE_CRUDE:
         case FENCE_PLAN_GARDESGARD_GATE:
         case FENCE_PLAN_GARDESGARD_HIGH:
         case FENCE_PLAN_GARDESGARD_LOW:
         case FENCE_PLAN_WOVEN:
         case FENCE_SIEGEWALL:
         default:
            logger.log(Level.WARNING, "Fence plan for type " + type + " is not found!", (Throwable)(new Exception()));
            return type;
         case FENCE_GARDESGARD_LOW:
            return StructureConstantsEnum.FENCE_PLAN_GARDESGARD_LOW;
         case FENCE_IRON:
            return StructureConstantsEnum.FENCE_PLAN_IRON;
         case FENCE_STONEWALL:
            return StructureConstantsEnum.FENCE_PLAN_STONEWALL;
         case FENCE_WOODEN:
            return StructureConstantsEnum.FENCE_PLAN_WOODEN;
         case FENCE_STONE_PARAPET:
            return StructureConstantsEnum.FENCE_PLAN_STONE_PARAPET;
         case FENCE_WOODEN_PARAPET:
            return StructureConstantsEnum.FENCE_PLAN_WOODEN_PARAPET;
         case FENCE_STONE_IRON_PARAPET:
            return StructureConstantsEnum.FENCE_PLAN_STONE_IRON_PARAPET;
         case FENCE_ROPE_HIGH:
            return StructureConstantsEnum.FENCE_PLAN_ROPE_HIGH;
         case FENCE_GARDESGARD_GATE:
            return StructureConstantsEnum.FENCE_PLAN_GARDESGARD_GATE;
         case FENCE_WOODEN_GATE:
            return StructureConstantsEnum.FENCE_PLAN_WOODEN_GATE;
         case FENCE_WOODEN_CRUDE_GATE:
            return StructureConstantsEnum.FENCE_PLAN_WOODEN_GATE_CRUDE;
         case FENCE_WOODEN_CRUDE:
            return StructureConstantsEnum.FENCE_PLAN_WOODEN_CRUDE;
         case FENCE_IRON_GATE:
            return StructureConstantsEnum.FENCE_PLAN_IRON_GATE;
         case FENCE_STONE:
            return StructureConstantsEnum.FENCE_PLAN_STONE;
         case FENCE_SANDSTONE_STONE_PARAPET:
            return StructureConstantsEnum.FENCE_PLAN_SANDSTONE_STONE_PARAPET;
         case FENCE_SLATE_STONE_PARAPET:
            return StructureConstantsEnum.FENCE_PLAN_SLATE_STONE_PARAPET;
         case FENCE_ROUNDED_STONE_STONE_PARAPET:
            return StructureConstantsEnum.FENCE_PLAN_ROUNDED_STONE_STONE_PARAPET;
         case FENCE_RENDERED_STONE_PARAPET:
            return StructureConstantsEnum.FENCE_PLAN_RENDERED_STONE_PARAPET;
         case FENCE_POTTERY_STONE_PARAPET:
            return StructureConstantsEnum.FENCE_PLAN_POTTERY_STONE_PARAPET;
         case FENCE_MARBLE_STONE_PARAPET:
            return StructureConstantsEnum.FENCE_PLAN_MARBLE_STONE_PARAPET;
         case FENCE_SLATE_CHAIN_FENCE:
            return StructureConstantsEnum.FENCE_PLAN_SLATE_CHAIN_FENCE;
         case FENCE_ROUNDED_STONE_CHAIN_FENCE:
            return StructureConstantsEnum.FENCE_PLAN_ROUNDED_STONE_CHAIN_FENCE;
         case FENCE_SANDSTONE_CHAIN_FENCE:
            return StructureConstantsEnum.FENCE_PLAN_SANDSTONE_CHAIN_FENCE;
         case FENCE_RENDERED_CHAIN_FENCE:
            return StructureConstantsEnum.FENCE_PLAN_RENDERED_CHAIN_FENCE;
         case FENCE_POTTERY_CHAIN_FENCE:
            return StructureConstantsEnum.FENCE_PLAN_POTTERY_CHAIN_FENCE;
         case FENCE_MARBLE_CHAIN_FENCE:
            return StructureConstantsEnum.FENCE_PLAN_MARBLE_CHAIN_FENCE;
         case FENCE_PALISADE:
            return StructureConstantsEnum.FENCE_PLAN_PALISADE;
         case FENCE_PALISADE_GATE:
            return StructureConstantsEnum.FENCE_PLAN_PALISADE_GATE;
         case FENCE_STONEWALL_HIGH:
            return StructureConstantsEnum.FENCE_PLAN_STONEWALL_HIGH;
         case FENCE_SLATE_TALL_STONE_WALL:
            return StructureConstantsEnum.FENCE_PLAN_SLATE_TALL_STONE_WALL;
         case FENCE_ROUNDED_STONE_TALL_STONE_WALL:
            return StructureConstantsEnum.FENCE_PLAN_ROUNDED_STONE_TALL_STONE_WALL;
         case FENCE_SANDSTONE_TALL_STONE_WALL:
            return StructureConstantsEnum.FENCE_PLAN_SANDSTONE_TALL_STONE_WALL;
         case FENCE_RENDERED_TALL_STONE_WALL:
            return StructureConstantsEnum.FENCE_PLAN_RENDERED_TALL_STONE_WALL;
         case FENCE_POTTERY_TALL_STONE_WALL:
            return StructureConstantsEnum.FENCE_PLAN_POTTERY_TALL_STONE_WALL;
         case FENCE_MARBLE_TALL_STONE_WALL:
            return StructureConstantsEnum.FENCE_PLAN_MARBLE_TALL_STONE_WALL;
         case FENCE_SLATE_PORTCULLIS:
            return StructureConstantsEnum.FENCE_PLAN_SLATE_PORTCULLIS;
         case FENCE_ROUNDED_STONE_PORTCULLIS:
            return StructureConstantsEnum.FENCE_PLAN_ROUNDED_STONE_PORTCULLIS;
         case FENCE_SANDSTONE_PORTCULLIS:
            return StructureConstantsEnum.FENCE_PLAN_SANDSTONE_PORTCULLIS;
         case FENCE_RENDERED_PORTCULLIS:
            return StructureConstantsEnum.FENCE_PLAN_RENDERED_PORTCULLIS;
         case FENCE_POTTERY_PORTCULLIS:
            return StructureConstantsEnum.FENCE_PLAN_POTTERY_PORTCULLIS;
         case FENCE_MARBLE_PORTCULLIS:
            return StructureConstantsEnum.FENCE_PLAN_MARBLE_PORTCULLIS;
         case FENCE_SLATE:
            return StructureConstantsEnum.FENCE_PLAN_SLATE;
         case FENCE_ROUNDED_STONE:
            return StructureConstantsEnum.FENCE_PLAN_ROUNDED_STONE;
         case FENCE_POTTERY:
            return StructureConstantsEnum.FENCE_PLAN_POTTERY;
         case FENCE_SANDSTONE:
            return StructureConstantsEnum.FENCE_PLAN_SANDSTONE;
         case FENCE_MARBLE:
            return StructureConstantsEnum.FENCE_PLAN_MARBLE;
         case FENCE_SLATE_HIGH_IRON_FENCE_GATE:
            return StructureConstantsEnum.FENCE_PLAN_SLATE_HIGH_IRON_FENCE_GATE;
         case FENCE_ROUNDED_STONE_HIGH_IRON_FENCE_GATE:
            return StructureConstantsEnum.FENCE_PLAN_ROUNDED_STONE_HIGH_IRON_FENCE_GATE;
         case FENCE_SANDSTONE_HIGH_IRON_FENCE_GATE:
            return StructureConstantsEnum.FENCE_PLAN_SANDSTONE_HIGH_IRON_FENCE_GATE;
         case FENCE_RENDERED_HIGH_IRON_FENCE_GATE:
            return StructureConstantsEnum.FENCE_PLAN_RENDERED_HIGH_IRON_FENCE_GATE;
         case FENCE_POTTERY_HIGH_IRON_FENCE_GATE:
            return StructureConstantsEnum.FENCE_PLAN_POTTERY_HIGH_IRON_FENCE_GATE;
         case FENCE_MARBLE_HIGH_IRON_FENCE_GATE:
            return StructureConstantsEnum.FENCE_PLAN_MARBLE_HIGH_IRON_FENCE_GATE;
         case FENCE_SLATE_HIGH_IRON_FENCE:
            return StructureConstantsEnum.FENCE_PLAN_SLATE_HIGH_IRON_FENCE;
         case FENCE_ROUNDED_STONE_HIGH_IRON_FENCE:
            return StructureConstantsEnum.FENCE_PLAN_ROUNDED_STONE_HIGH_IRON_FENCE;
         case FENCE_SANDSTONE_HIGH_IRON_FENCE:
            return StructureConstantsEnum.FENCE_PLAN_SANDSTONE_HIGH_IRON_FENCE;
         case FENCE_RENDERED_HIGH_IRON_FENCE:
            return StructureConstantsEnum.FENCE_PLAN_RENDERED_HIGH_IRON_FENCE;
         case FENCE_POTTERY_HIGH_IRON_FENCE:
            return StructureConstantsEnum.FENCE_PLAN_POTTERY_HIGH_IRON_FENCE;
         case FENCE_MARBLE_HIGH_IRON_FENCE:
            return StructureConstantsEnum.FENCE_PLAN_MARBLE_HIGH_IRON_FENCE;
         case FENCE_PORTCULLIS:
            return StructureConstantsEnum.FENCE_PLAN_PORTCULLIS;
         case FENCE_RENDERED:
            return StructureConstantsEnum.FENCE_PLAN_RENDERED;
         case FENCE_GARDESGARD_HIGH:
            return StructureConstantsEnum.FENCE_PLAN_GARDESGARD_HIGH;
         case FENCE_IRON_HIGH:
            return StructureConstantsEnum.FENCE_PLAN_IRON_HIGH;
         case FENCE_IRON_GATE_HIGH:
            return StructureConstantsEnum.FENCE_PLAN_IRON_GATE_HIGH;
         case FENCE_MEDIUM_CHAIN:
            return StructureConstantsEnum.FENCE_PLAN_MEDIUM_CHAIN;
         case FENCE_SLATE_IRON:
            return StructureConstantsEnum.FENCE_PLAN_SLATE_IRON;
         case FENCE_ROUNDED_STONE_IRON:
            return StructureConstantsEnum.FENCE_PLAN_ROUNDED_STONE_IRON;
         case FENCE_POTTERY_IRON:
            return StructureConstantsEnum.FENCE_PLAN_POTTERY_IRON;
         case FENCE_SANDSTONE_IRON:
            return StructureConstantsEnum.FENCE_PLAN_SANDSTONE_IRON;
         case FENCE_RENDERED_IRON:
            return StructureConstantsEnum.FENCE_PLAN_RENDERED_IRON;
         case FENCE_MARBLE_IRON:
            return StructureConstantsEnum.FENCE_PLAN_MARBLE_IRON;
         case FENCE_SLATE_IRON_GATE:
            return StructureConstantsEnum.FENCE_PLAN_SLATE_IRON_GATE;
         case FENCE_ROUNDED_STONE_IRON_GATE:
            return StructureConstantsEnum.FENCE_PLAN_ROUNDED_STONE_IRON_GATE;
         case FENCE_POTTERY_IRON_GATE:
            return StructureConstantsEnum.FENCE_PLAN_POTTERY_IRON_GATE;
         case FENCE_SANDSTONE_IRON_GATE:
            return StructureConstantsEnum.FENCE_PLAN_SANDSTONE_IRON_GATE;
         case FENCE_RENDERED_IRON_GATE:
            return StructureConstantsEnum.FENCE_PLAN_RENDERED_IRON_GATE;
         case FENCE_MARBLE_IRON_GATE:
            return StructureConstantsEnum.FENCE_PLAN_MARBLE_IRON_GATE;
      }
   }

   public static final StructureConstantsEnum getFenceForPlan(StructureConstantsEnum type) {
      switch(type) {
         case FENCE_PLAN_ROPE_HIGH:
            return StructureConstantsEnum.FENCE_ROPE_HIGH;
         case FENCE_GARDESGARD_GATE:
         case FENCE_WOODEN_GATE:
         case FENCE_WOODEN_CRUDE_GATE:
         case FENCE_WOODEN_CRUDE:
         case FENCE_IRON_GATE:
         case FENCE_STONE:
         case FENCE_SANDSTONE_STONE_PARAPET:
         case FENCE_SLATE_STONE_PARAPET:
         case FENCE_ROUNDED_STONE_STONE_PARAPET:
         case FENCE_RENDERED_STONE_PARAPET:
         case FENCE_POTTERY_STONE_PARAPET:
         case FENCE_MARBLE_STONE_PARAPET:
         case FENCE_SLATE_CHAIN_FENCE:
         case FENCE_ROUNDED_STONE_CHAIN_FENCE:
         case FENCE_SANDSTONE_CHAIN_FENCE:
         case FENCE_RENDERED_CHAIN_FENCE:
         case FENCE_POTTERY_CHAIN_FENCE:
         case FENCE_MARBLE_CHAIN_FENCE:
         case HEDGE_FLOWER1_MEDIUM:
         case HEDGE_FLOWER2_MEDIUM:
         case HEDGE_FLOWER3_MEDIUM:
         case HEDGE_FLOWER4_MEDIUM:
         case HEDGE_FLOWER5_MEDIUM:
         case HEDGE_FLOWER6_MEDIUM:
         case HEDGE_FLOWER7_MEDIUM:
         case HEDGE_FLOWER1_HIGH:
         case HEDGE_FLOWER2_HIGH:
         case HEDGE_FLOWER3_HIGH:
         case HEDGE_FLOWER4_HIGH:
         case HEDGE_FLOWER5_HIGH:
         case HEDGE_FLOWER6_HIGH:
         case HEDGE_FLOWER7_HIGH:
         case FENCE_MAGIC_STONE:
         case FENCE_MAGIC_ICE:
         case FENCE_MAGIC_FIRE:
         case FENCE_PALISADE:
         case FENCE_PALISADE_GATE:
         case FENCE_STONEWALL_HIGH:
         case FENCE_SLATE_TALL_STONE_WALL:
         case FENCE_ROUNDED_STONE_TALL_STONE_WALL:
         case FENCE_SANDSTONE_TALL_STONE_WALL:
         case FENCE_RENDERED_TALL_STONE_WALL:
         case FENCE_POTTERY_TALL_STONE_WALL:
         case FENCE_MARBLE_TALL_STONE_WALL:
         case FENCE_SLATE_PORTCULLIS:
         case FENCE_ROUNDED_STONE_PORTCULLIS:
         case FENCE_SANDSTONE_PORTCULLIS:
         case FENCE_RENDERED_PORTCULLIS:
         case FENCE_POTTERY_PORTCULLIS:
         case FENCE_MARBLE_PORTCULLIS:
         case FENCE_SLATE:
         case FENCE_ROUNDED_STONE:
         case FENCE_POTTERY:
         case FENCE_SANDSTONE:
         case FENCE_MARBLE:
         case FENCE_SLATE_HIGH_IRON_FENCE_GATE:
         case FENCE_ROUNDED_STONE_HIGH_IRON_FENCE_GATE:
         case FENCE_SANDSTONE_HIGH_IRON_FENCE_GATE:
         case FENCE_RENDERED_HIGH_IRON_FENCE_GATE:
         case FENCE_POTTERY_HIGH_IRON_FENCE_GATE:
         case FENCE_MARBLE_HIGH_IRON_FENCE_GATE:
         case FENCE_SLATE_HIGH_IRON_FENCE:
         case FENCE_ROUNDED_STONE_HIGH_IRON_FENCE:
         case FENCE_SANDSTONE_HIGH_IRON_FENCE:
         case FENCE_RENDERED_HIGH_IRON_FENCE:
         case FENCE_POTTERY_HIGH_IRON_FENCE:
         case FENCE_MARBLE_HIGH_IRON_FENCE:
         case FENCE_PORTCULLIS:
         case FENCE_RENDERED:
         case FENCE_GARDESGARD_HIGH:
         case FENCE_SIEGEWALL:
         case FENCE_IRON_HIGH:
         case FENCE_IRON_GATE_HIGH:
         case FENCE_MEDIUM_CHAIN:
         case FENCE_SLATE_IRON:
         case FENCE_ROUNDED_STONE_IRON:
         case FENCE_POTTERY_IRON:
         case FENCE_SANDSTONE_IRON:
         case FENCE_RENDERED_IRON:
         case FENCE_MARBLE_IRON:
         case FENCE_SLATE_IRON_GATE:
         case FENCE_ROUNDED_STONE_IRON_GATE:
         case FENCE_POTTERY_IRON_GATE:
         case FENCE_SANDSTONE_IRON_GATE:
         case FENCE_RENDERED_IRON_GATE:
         case FENCE_MARBLE_IRON_GATE:
         default:
            logger.log(Level.WARNING, "Fence for type " + type + " is not found!", (Throwable)(new Exception()));
            return type;
         case FENCE_PLAN_PALISADE:
            return StructureConstantsEnum.FENCE_PALISADE;
         case FENCE_PLAN_PALISADE_GATE:
            return StructureConstantsEnum.FENCE_PALISADE_GATE;
         case FENCE_PLAN_STONEWALL_HIGH:
            return StructureConstantsEnum.FENCE_STONEWALL_HIGH;
         case FENCE_PLAN_PORTCULLIS:
            return StructureConstantsEnum.FENCE_PORTCULLIS;
         case FENCE_PLAN_SLATE_TALL_STONE_WALL:
            return StructureConstantsEnum.FENCE_SLATE_TALL_STONE_WALL;
         case FENCE_PLAN_ROUNDED_STONE_TALL_STONE_WALL:
            return StructureConstantsEnum.FENCE_ROUNDED_STONE_TALL_STONE_WALL;
         case FENCE_PLAN_SANDSTONE_TALL_STONE_WALL:
            return StructureConstantsEnum.FENCE_SANDSTONE_TALL_STONE_WALL;
         case FENCE_PLAN_RENDERED_TALL_STONE_WALL:
            return StructureConstantsEnum.FENCE_RENDERED_TALL_STONE_WALL;
         case FENCE_PLAN_POTTERY_TALL_STONE_WALL:
            return StructureConstantsEnum.FENCE_POTTERY_TALL_STONE_WALL;
         case FENCE_PLAN_MARBLE_TALL_STONE_WALL:
            return StructureConstantsEnum.FENCE_MARBLE_TALL_STONE_WALL;
         case FENCE_PLAN_SLATE_PORTCULLIS:
            return StructureConstantsEnum.FENCE_SLATE_PORTCULLIS;
         case FENCE_PLAN_ROUNDED_STONE_PORTCULLIS:
            return StructureConstantsEnum.FENCE_ROUNDED_STONE_PORTCULLIS;
         case FENCE_PLAN_SANDSTONE_PORTCULLIS:
            return StructureConstantsEnum.FENCE_SANDSTONE_PORTCULLIS;
         case FENCE_PLAN_RENDERED_PORTCULLIS:
            return StructureConstantsEnum.FENCE_RENDERED_PORTCULLIS;
         case FENCE_PLAN_POTTERY_PORTCULLIS:
            return StructureConstantsEnum.FENCE_POTTERY_PORTCULLIS;
         case FENCE_PLAN_MARBLE_PORTCULLIS:
            return StructureConstantsEnum.FENCE_MARBLE_PORTCULLIS;
         case FENCE_PLAN_STONEWALL:
            return StructureConstantsEnum.FENCE_STONEWALL;
         case FENCE_PLAN_STONE:
            return StructureConstantsEnum.FENCE_STONE;
         case FENCE_PLAN_SLATE:
            return StructureConstantsEnum.FENCE_SLATE;
         case FENCE_PLAN_ROUNDED_STONE:
            return StructureConstantsEnum.FENCE_ROUNDED_STONE;
         case FENCE_PLAN_POTTERY:
            return StructureConstantsEnum.FENCE_POTTERY;
         case FENCE_PLAN_SANDSTONE:
            return StructureConstantsEnum.FENCE_SANDSTONE;
         case FENCE_PLAN_MARBLE:
            return StructureConstantsEnum.FENCE_MARBLE;
         case FENCE_PLAN_WOODEN_PARAPET:
            return StructureConstantsEnum.FENCE_WOODEN_PARAPET;
         case FENCE_PLAN_STONE_PARAPET:
            return StructureConstantsEnum.FENCE_STONE_PARAPET;
         case FENCE_PLAN_STONE_IRON_PARAPET:
            return StructureConstantsEnum.FENCE_STONE_IRON_PARAPET;
         case FENCE_PLAN_SLATE_STONE_PARAPET:
            return StructureConstantsEnum.FENCE_SLATE_STONE_PARAPET;
         case FENCE_PLAN_ROUNDED_STONE_STONE_PARAPET:
            return StructureConstantsEnum.FENCE_ROUNDED_STONE_STONE_PARAPET;
         case FENCE_PLAN_RENDERED_STONE_PARAPET:
            return StructureConstantsEnum.FENCE_RENDERED_STONE_PARAPET;
         case FENCE_PLAN_POTTERY_STONE_PARAPET:
            return StructureConstantsEnum.FENCE_POTTERY_STONE_PARAPET;
         case FENCE_PLAN_MARBLE_STONE_PARAPET:
            return StructureConstantsEnum.FENCE_MARBLE_STONE_PARAPET;
         case FENCE_PLAN_MEDIUM_CHAIN:
            return StructureConstantsEnum.FENCE_MEDIUM_CHAIN;
         case FENCE_PLAN_SLATE_CHAIN_FENCE:
            return StructureConstantsEnum.FENCE_SLATE_CHAIN_FENCE;
         case FENCE_PLAN_ROUNDED_STONE_CHAIN_FENCE:
            return StructureConstantsEnum.FENCE_ROUNDED_STONE_CHAIN_FENCE;
         case FENCE_PLAN_SANDSTONE_CHAIN_FENCE:
            return StructureConstantsEnum.FENCE_SANDSTONE_CHAIN_FENCE;
         case FENCE_PLAN_RENDERED_CHAIN_FENCE:
            return StructureConstantsEnum.FENCE_RENDERED_CHAIN_FENCE;
         case FENCE_PLAN_POTTERY_CHAIN_FENCE:
            return StructureConstantsEnum.FENCE_POTTERY_CHAIN_FENCE;
         case FENCE_PLAN_MARBLE_CHAIN_FENCE:
            return StructureConstantsEnum.FENCE_MARBLE_CHAIN_FENCE;
         case FENCE_PLAN_CURB:
            return StructureConstantsEnum.FENCE_CURB;
         case FENCE_PLAN_ROPE_LOW:
            return StructureConstantsEnum.FENCE_ROPE_LOW;
         case FENCE_PLAN_IRON_HIGH:
            return StructureConstantsEnum.FENCE_IRON_HIGH;
         case FENCE_PLAN_IRON_GATE_HIGH:
            return StructureConstantsEnum.FENCE_IRON_GATE_HIGH;
         case FENCE_PLAN_SLATE_HIGH_IRON_FENCE_GATE:
            return StructureConstantsEnum.FENCE_SLATE_HIGH_IRON_FENCE_GATE;
         case FENCE_PLAN_ROUNDED_STONE_HIGH_IRON_FENCE_GATE:
            return StructureConstantsEnum.FENCE_ROUNDED_STONE_HIGH_IRON_FENCE_GATE;
         case FENCE_PLAN_SANDSTONE_HIGH_IRON_FENCE_GATE:
            return StructureConstantsEnum.FENCE_SANDSTONE_HIGH_IRON_FENCE_GATE;
         case FENCE_PLAN_RENDERED_HIGH_IRON_FENCE_GATE:
            return StructureConstantsEnum.FENCE_RENDERED_HIGH_IRON_FENCE_GATE;
         case FENCE_PLAN_POTTERY_HIGH_IRON_FENCE_GATE:
            return StructureConstantsEnum.FENCE_POTTERY_HIGH_IRON_FENCE_GATE;
         case FENCE_PLAN_MARBLE_HIGH_IRON_FENCE_GATE:
            return StructureConstantsEnum.FENCE_MARBLE_HIGH_IRON_FENCE_GATE;
         case FENCE_PLAN_SLATE_HIGH_IRON_FENCE:
            return StructureConstantsEnum.FENCE_SLATE_HIGH_IRON_FENCE;
         case FENCE_PLAN_ROUNDED_STONE_HIGH_IRON_FENCE:
            return StructureConstantsEnum.FENCE_ROUNDED_STONE_HIGH_IRON_FENCE;
         case FENCE_PLAN_SANDSTONE_HIGH_IRON_FENCE:
            return StructureConstantsEnum.FENCE_SANDSTONE_HIGH_IRON_FENCE;
         case FENCE_PLAN_RENDERED_HIGH_IRON_FENCE:
            return StructureConstantsEnum.FENCE_RENDERED_HIGH_IRON_FENCE;
         case FENCE_PLAN_POTTERY_HIGH_IRON_FENCE:
            return StructureConstantsEnum.FENCE_POTTERY_HIGH_IRON_FENCE;
         case FENCE_PLAN_MARBLE_HIGH_IRON_FENCE:
            return StructureConstantsEnum.FENCE_MARBLE_HIGH_IRON_FENCE;
         case FENCE_PLAN_SANDSTONE_STONE_PARAPET:
            return StructureConstantsEnum.FENCE_SANDSTONE_STONE_PARAPET;
         case FENCE_PLAN_WOODEN:
            return StructureConstantsEnum.FENCE_WOODEN;
         case FENCE_PLAN_WOODEN_GATE:
            return StructureConstantsEnum.FENCE_WOODEN_GATE;
         case FENCE_PLAN_WOODEN_CRUDE:
            return StructureConstantsEnum.FENCE_WOODEN_CRUDE;
         case FENCE_PLAN_WOODEN_GATE_CRUDE:
            return StructureConstantsEnum.FENCE_WOODEN_CRUDE_GATE;
         case FENCE_PLAN_GARDESGARD_GATE:
            return StructureConstantsEnum.FENCE_GARDESGARD_GATE;
         case FENCE_PLAN_GARDESGARD_HIGH:
            return StructureConstantsEnum.FENCE_GARDESGARD_HIGH;
         case FENCE_PLAN_GARDESGARD_LOW:
            return StructureConstantsEnum.FENCE_GARDESGARD_LOW;
         case FENCE_PLAN_WOVEN:
            return StructureConstantsEnum.FENCE_WOVEN;
         case FENCE_PLAN_IRON:
            return StructureConstantsEnum.FENCE_IRON;
         case FENCE_PLAN_IRON_GATE:
            return StructureConstantsEnum.FENCE_IRON_GATE;
         case FENCE_PLAN_RENDERED:
            return StructureConstantsEnum.FENCE_RENDERED;
         case FENCE_PLAN_SLATE_IRON:
            return StructureConstantsEnum.FENCE_SLATE_IRON;
         case FENCE_PLAN_ROUNDED_STONE_IRON:
            return StructureConstantsEnum.FENCE_ROUNDED_STONE_IRON;
         case FENCE_PLAN_POTTERY_IRON:
            return StructureConstantsEnum.FENCE_POTTERY_IRON;
         case FENCE_PLAN_SANDSTONE_IRON:
            return StructureConstantsEnum.FENCE_SANDSTONE_IRON;
         case FENCE_PLAN_RENDERED_IRON:
            return StructureConstantsEnum.FENCE_RENDERED_IRON;
         case FENCE_PLAN_MARBLE_IRON:
            return StructureConstantsEnum.FENCE_MARBLE_IRON;
         case FENCE_PLAN_SLATE_IRON_GATE:
            return StructureConstantsEnum.FENCE_SLATE_IRON_GATE;
         case FENCE_PLAN_ROUNDED_STONE_IRON_GATE:
            return StructureConstantsEnum.FENCE_ROUNDED_STONE_IRON_GATE;
         case FENCE_PLAN_POTTERY_IRON_GATE:
            return StructureConstantsEnum.FENCE_POTTERY_IRON_GATE;
         case FENCE_PLAN_SANDSTONE_IRON_GATE:
            return StructureConstantsEnum.FENCE_SANDSTONE_IRON_GATE;
         case FENCE_PLAN_RENDERED_IRON_GATE:
            return StructureConstantsEnum.FENCE_RENDERED_IRON_GATE;
         case FENCE_PLAN_MARBLE_IRON_GATE:
            return StructureConstantsEnum.FENCE_MARBLE_IRON_GATE;
      }
   }

   public final int[] getItemTemplatesDealtForFence(StructureConstantsEnum aType, StructureStateEnum aState) {
      if (aState.state >= this.getFinishState().state) {
         if (aType == StructureConstantsEnum.FENCE_PALISADE || aType == StructureConstantsEnum.FENCE_PALISADE_GATE) {
            int[] toReturn = new int[10];

            for(int x = 0; x < toReturn.length; ++x) {
               toReturn[x] = 385;
            }

            return toReturn;
         }

         if (aType == StructureConstantsEnum.FENCE_STONEWALL) {
            int[] toReturn = new int[10];

            for(int x = 0; x < toReturn.length; ++x) {
               toReturn[x] = 146;
            }

            return toReturn;
         }

         if (aType == StructureConstantsEnum.FENCE_STONE_PARAPET) {
            int[] toReturn = new int[15];

            for(int x = 0; x < toReturn.length; ++x) {
               toReturn[x] = 132;
            }

            return toReturn;
         }

         if (aType != StructureConstantsEnum.FENCE_STONE_IRON_PARAPET) {
            if (aType == StructureConstantsEnum.FENCE_WOODEN_PARAPET) {
               int[] toReturn = new int[15];

               for(int x = 0; x < toReturn.length; ++x) {
                  toReturn[x] = 22;
               }

               return toReturn;
            }

            if (aType == StructureConstantsEnum.FENCE_STONEWALL_HIGH) {
               int[] toReturn = new int[20];

               for(int x = 0; x < toReturn.length; ++x) {
                  toReturn[x] = 132;
               }

               return toReturn;
            }

            if (aType != StructureConstantsEnum.FENCE_IRON && aType != StructureConstantsEnum.FENCE_IRON_GATE) {
               if (aType != StructureConstantsEnum.FENCE_WOVEN) {
                  if (aType == StructureConstantsEnum.FENCE_RUBBLE) {
                     return new int[]{169};
                  }

                  return new int[]{23, 22, 22};
               }

               int[] toReturn = new int[2];

               for(int x = 0; x < toReturn.length; ++x) {
                  toReturn[x] = 169;
               }

               return toReturn;
            }

            int[] toReturn = new int[1];

            for(int x = 0; x < toReturn.length; ++x) {
               toReturn[x] = 46;
            }

            return toReturn;
         }

         int[] toReturn = new int[15];

         for(int x = 0; x < toReturn.length; ++x) {
            toReturn[x] = 132;
         }
      }

      return EMPTY_INT_ARRAY;
   }

   public static final int[] getItemTemplatesNeededForFenceTotal(StructureConstantsEnum fenceType) {
      StructureStateEnum finishState = getFinishState(fenceType);
      switch(fenceType) {
         case FENCE_PLAN_ROPE_HIGH:
         case FENCE_PLAN_ROPE_LOW:
            return new int[]{23, 2, 319, finishState.state - 2};
         case FENCE_GARDESGARD_GATE:
         case FENCE_WOODEN_GATE:
         case FENCE_WOODEN_CRUDE_GATE:
         case FENCE_WOODEN_CRUDE:
         case FENCE_IRON_GATE:
         case FENCE_STONE:
         case FENCE_SANDSTONE_STONE_PARAPET:
         case FENCE_SLATE_STONE_PARAPET:
         case FENCE_ROUNDED_STONE_STONE_PARAPET:
         case FENCE_RENDERED_STONE_PARAPET:
         case FENCE_POTTERY_STONE_PARAPET:
         case FENCE_MARBLE_STONE_PARAPET:
         case FENCE_SLATE_CHAIN_FENCE:
         case FENCE_ROUNDED_STONE_CHAIN_FENCE:
         case FENCE_SANDSTONE_CHAIN_FENCE:
         case FENCE_RENDERED_CHAIN_FENCE:
         case FENCE_POTTERY_CHAIN_FENCE:
         case FENCE_MARBLE_CHAIN_FENCE:
         case HEDGE_FLOWER1_MEDIUM:
         case HEDGE_FLOWER2_MEDIUM:
         case HEDGE_FLOWER3_MEDIUM:
         case HEDGE_FLOWER4_MEDIUM:
         case HEDGE_FLOWER5_MEDIUM:
         case HEDGE_FLOWER6_MEDIUM:
         case HEDGE_FLOWER7_MEDIUM:
         case HEDGE_FLOWER1_HIGH:
         case HEDGE_FLOWER2_HIGH:
         case HEDGE_FLOWER3_HIGH:
         case HEDGE_FLOWER4_HIGH:
         case HEDGE_FLOWER5_HIGH:
         case HEDGE_FLOWER6_HIGH:
         case HEDGE_FLOWER7_HIGH:
         case FENCE_MAGIC_STONE:
         case FENCE_MAGIC_ICE:
         case FENCE_MAGIC_FIRE:
         case FENCE_PALISADE:
         case FENCE_PALISADE_GATE:
         case FENCE_STONEWALL_HIGH:
         case FENCE_SLATE_TALL_STONE_WALL:
         case FENCE_ROUNDED_STONE_TALL_STONE_WALL:
         case FENCE_SANDSTONE_TALL_STONE_WALL:
         case FENCE_RENDERED_TALL_STONE_WALL:
         case FENCE_POTTERY_TALL_STONE_WALL:
         case FENCE_MARBLE_TALL_STONE_WALL:
         case FENCE_SLATE_PORTCULLIS:
         case FENCE_ROUNDED_STONE_PORTCULLIS:
         case FENCE_SANDSTONE_PORTCULLIS:
         case FENCE_RENDERED_PORTCULLIS:
         case FENCE_POTTERY_PORTCULLIS:
         case FENCE_MARBLE_PORTCULLIS:
         case FENCE_PLAN_RENDERED_TALL_STONE_WALL:
         case FENCE_PLAN_RENDERED_PORTCULLIS:
         case FENCE_SLATE:
         case FENCE_ROUNDED_STONE:
         case FENCE_POTTERY:
         case FENCE_SANDSTONE:
         case FENCE_MARBLE:
         case FENCE_PLAN_RENDERED_STONE_PARAPET:
         case FENCE_PLAN_RENDERED_CHAIN_FENCE:
         case FENCE_SLATE_HIGH_IRON_FENCE_GATE:
         case FENCE_ROUNDED_STONE_HIGH_IRON_FENCE_GATE:
         case FENCE_SANDSTONE_HIGH_IRON_FENCE_GATE:
         case FENCE_RENDERED_HIGH_IRON_FENCE_GATE:
         case FENCE_POTTERY_HIGH_IRON_FENCE_GATE:
         case FENCE_MARBLE_HIGH_IRON_FENCE_GATE:
         case FENCE_SLATE_HIGH_IRON_FENCE:
         case FENCE_ROUNDED_STONE_HIGH_IRON_FENCE:
         case FENCE_SANDSTONE_HIGH_IRON_FENCE:
         case FENCE_RENDERED_HIGH_IRON_FENCE:
         case FENCE_POTTERY_HIGH_IRON_FENCE:
         case FENCE_MARBLE_HIGH_IRON_FENCE:
         case FENCE_PLAN_RENDERED_HIGH_IRON_FENCE_GATE:
         case FENCE_PLAN_RENDERED_HIGH_IRON_FENCE:
         case FENCE_PORTCULLIS:
         case FENCE_RENDERED:
         case FENCE_GARDESGARD_HIGH:
         case FENCE_SIEGEWALL:
         case FENCE_IRON_HIGH:
         case FENCE_IRON_GATE_HIGH:
         case FENCE_MEDIUM_CHAIN:
         case FENCE_SLATE_IRON:
         case FENCE_ROUNDED_STONE_IRON:
         case FENCE_POTTERY_IRON:
         case FENCE_SANDSTONE_IRON:
         case FENCE_RENDERED_IRON:
         case FENCE_MARBLE_IRON:
         case FENCE_SLATE_IRON_GATE:
         case FENCE_ROUNDED_STONE_IRON_GATE:
         case FENCE_POTTERY_IRON_GATE:
         case FENCE_SANDSTONE_IRON_GATE:
         case FENCE_RENDERED_IRON_GATE:
         case FENCE_MARBLE_IRON_GATE:
         default:
            return new int[]{-1};
         case FENCE_PLAN_PALISADE:
         case FENCE_PLAN_PALISADE_GATE:
            return new int[]{385, finishState.state};
         case FENCE_PLAN_STONEWALL_HIGH:
            return new int[]{132, finishState.state};
         case FENCE_PLAN_PORTCULLIS:
            return new int[]{132, 15, 681, 2, 187, 2, 559, 1};
         case FENCE_PLAN_SLATE_TALL_STONE_WALL:
            return new int[]{1123, finishState.state};
         case FENCE_PLAN_ROUNDED_STONE_TALL_STONE_WALL:
            return new int[]{1122, finishState.state};
         case FENCE_PLAN_SANDSTONE_TALL_STONE_WALL:
            return new int[]{1121, finishState.state};
         case FENCE_PLAN_POTTERY_TALL_STONE_WALL:
            return new int[]{776, finishState.state};
         case FENCE_PLAN_MARBLE_TALL_STONE_WALL:
            return new int[]{786, finishState.state};
         case FENCE_PLAN_SLATE_PORTCULLIS:
            return new int[]{1123, 15, 681, 2, 187, 2, 559, 1};
         case FENCE_PLAN_ROUNDED_STONE_PORTCULLIS:
            return new int[]{1122, 15, 681, 2, 187, 2, 559, 1};
         case FENCE_PLAN_SANDSTONE_PORTCULLIS:
            return new int[]{1121, 15, 681, 2, 187, 2, 559, 1};
         case FENCE_PLAN_POTTERY_PORTCULLIS:
            return new int[]{776, 15, 681, 2, 187, 2, 559, 1};
         case FENCE_PLAN_MARBLE_PORTCULLIS:
            return new int[]{786, 15, 681, 2, 187, 2, 559, 1};
         case FENCE_PLAN_STONEWALL:
            return new int[]{146, finishState.state};
         case FENCE_PLAN_STONE:
            return new int[]{132, finishState.state};
         case FENCE_PLAN_SLATE:
            return new int[]{1123, finishState.state};
         case FENCE_PLAN_ROUNDED_STONE:
            return new int[]{1122, finishState.state};
         case FENCE_PLAN_POTTERY:
            return new int[]{776, finishState.state};
         case FENCE_PLAN_SANDSTONE:
            return new int[]{1121, finishState.state};
         case FENCE_PLAN_MARBLE:
            return new int[]{786, finishState.state};
         case FENCE_PLAN_WOODEN_PARAPET:
            return new int[]{23, 2, 22, finishState.state - 2, 218, finishState.state - 2};
         case FENCE_PLAN_STONE_PARAPET:
            return new int[]{132, finishState.state};
         case FENCE_PLAN_STONE_IRON_PARAPET:
            return new int[]{132, finishState.state - 1, 681, 1};
         case FENCE_PLAN_SLATE_STONE_PARAPET:
            return new int[]{1123, finishState.state};
         case FENCE_PLAN_ROUNDED_STONE_STONE_PARAPET:
            return new int[]{1122, finishState.state};
         case FENCE_PLAN_POTTERY_STONE_PARAPET:
            return new int[]{776, finishState.state};
         case FENCE_PLAN_MARBLE_STONE_PARAPET:
            return new int[]{786, finishState.state};
         case FENCE_PLAN_MEDIUM_CHAIN:
            return new int[]{132, finishState.state - 6, 859, 6};
         case FENCE_PLAN_SLATE_CHAIN_FENCE:
            return new int[]{1123, finishState.state - 6, 859, 6};
         case FENCE_PLAN_ROUNDED_STONE_CHAIN_FENCE:
            return new int[]{1122, finishState.state - 6, 859, 6};
         case FENCE_PLAN_SANDSTONE_CHAIN_FENCE:
            return new int[]{1121, finishState.state - 6, 859, 6};
         case FENCE_PLAN_POTTERY_CHAIN_FENCE:
            return new int[]{776, finishState.state - 6, 859, 6};
         case FENCE_PLAN_MARBLE_CHAIN_FENCE:
            return new int[]{786, finishState.state - 6, 859, 6};
         case FENCE_PLAN_CURB:
            return new int[]{146, finishState.state};
         case FENCE_PLAN_IRON_HIGH:
         case FENCE_PLAN_IRON_GATE_HIGH:
            return new int[]{132, finishState.state - 2, 681, 2};
         case FENCE_PLAN_SLATE_HIGH_IRON_FENCE_GATE:
         case FENCE_PLAN_SLATE_HIGH_IRON_FENCE:
            return new int[]{1123, finishState.state - 2, 681, 2};
         case FENCE_PLAN_ROUNDED_STONE_HIGH_IRON_FENCE_GATE:
         case FENCE_PLAN_ROUNDED_STONE_HIGH_IRON_FENCE:
            return new int[]{1122, finishState.state - 2, 681, 2};
         case FENCE_PLAN_SANDSTONE_HIGH_IRON_FENCE_GATE:
         case FENCE_PLAN_SANDSTONE_HIGH_IRON_FENCE:
            return new int[]{1121, finishState.state - 2, 681, 2};
         case FENCE_PLAN_POTTERY_HIGH_IRON_FENCE_GATE:
         case FENCE_PLAN_POTTERY_HIGH_IRON_FENCE:
            return new int[]{776, finishState.state - 2, 681, 2};
         case FENCE_PLAN_MARBLE_HIGH_IRON_FENCE_GATE:
         case FENCE_PLAN_MARBLE_HIGH_IRON_FENCE:
            return new int[]{786, finishState.state - 2, 681, 2};
         case FENCE_PLAN_SANDSTONE_STONE_PARAPET:
            return new int[]{1121, finishState.state};
         case FENCE_PLAN_WOODEN:
         case FENCE_PLAN_WOODEN_GATE:
            return new int[]{23, 2, 22, finishState.state - 2, 218, 1};
         case FENCE_PLAN_WOODEN_CRUDE:
         case FENCE_PLAN_WOODEN_GATE_CRUDE:
            return new int[]{23, 2, 22, finishState.state - 2, 218, 1};
         case FENCE_PLAN_GARDESGARD_GATE:
         case FENCE_PLAN_GARDESGARD_HIGH:
            return new int[]{23, finishState.state, 218, 1};
         case FENCE_PLAN_GARDESGARD_LOW:
            return new int[]{23, finishState.state, 218, 1};
         case FENCE_PLAN_WOVEN:
            return new int[]{169, finishState.state};
         case FENCE_PLAN_IRON:
         case FENCE_PLAN_IRON_GATE:
            return new int[]{132, finishState.state - 1, 681, 1};
         case FENCE_PLAN_RENDERED:
            return new int[]{132, finishState.state, 130, finishState.state};
         case FENCE_PLAN_SLATE_IRON:
         case FENCE_PLAN_SLATE_IRON_GATE:
            return new int[]{1123, finishState.state - 1, 681, 1};
         case FENCE_PLAN_ROUNDED_STONE_IRON:
         case FENCE_PLAN_ROUNDED_STONE_IRON_GATE:
            return new int[]{1122, finishState.state - 1, 681, 1};
         case FENCE_PLAN_POTTERY_IRON:
         case FENCE_PLAN_POTTERY_IRON_GATE:
            return new int[]{776, finishState.state - 1, 681, 1};
         case FENCE_PLAN_SANDSTONE_IRON:
         case FENCE_PLAN_SANDSTONE_IRON_GATE:
            return new int[]{1121, finishState.state - 1, 681, 1};
         case FENCE_PLAN_RENDERED_IRON:
         case FENCE_PLAN_RENDERED_IRON_GATE:
            return new int[]{1121, finishState.state - 11, 130, finishState.state - 1, 681, 1};
         case FENCE_PLAN_MARBLE_IRON:
         case FENCE_PLAN_MARBLE_IRON_GATE:
            return new int[]{786, finishState.state - 1, 681, 1};
      }
   }

   public static final int[] getConstructionMaterialsNeededTotal(Fence fence) {
      if (fence.isFinished()) {
         return new int[]{-1};
      } else {
         StructureStateEnum maxState = fence.getFinishState();
         StructureStateEnum currentState = fence.getState();
         StructureConstantsEnum type = fence.getType();
         int needed = maxState.state - currentState.state;
         if (type == StructureConstantsEnum.FENCE_PLAN_PALISADE || type == StructureConstantsEnum.FENCE_PLAN_PALISADE_GATE) {
            return new int[]{385, needed};
         } else if (type == StructureConstantsEnum.FENCE_PLAN_STONEWALL) {
            return new int[]{146, needed};
         } else if (type != StructureConstantsEnum.FENCE_PLAN_WOODEN_CRUDE && type != StructureConstantsEnum.FENCE_PLAN_WOODEN_GATE_CRUDE) {
            if (type != StructureConstantsEnum.FENCE_PLAN_WOODEN && type != StructureConstantsEnum.FENCE_PLAN_WOODEN_GATE) {
               if (type == StructureConstantsEnum.FENCE_PLAN_WOODEN_PARAPET) {
                  if (currentState == StructureStateEnum.UNINITIALIZED) {
                     return new int[]{23, 1};
                  } else if (currentState == StructureStateEnum.INITIALIZED) {
                     return new int[]{23, 1, 22, needed - 1, 218, needed - 1};
                  } else {
                     return currentState.state >= StructureStateEnum.INITIALIZED.state + 1 ? new int[]{22, needed, 218, needed} : new int[]{22, needed};
                  }
               } else if (type == StructureConstantsEnum.FENCE_PLAN_STONE_PARAPET) {
                  return new int[]{132, needed};
               } else if (type == StructureConstantsEnum.FENCE_PLAN_SLATE_STONE_PARAPET) {
                  return new int[]{1123, needed};
               } else if (type == StructureConstantsEnum.FENCE_PLAN_ROUNDED_STONE_STONE_PARAPET) {
                  return new int[]{1122, needed};
               } else if (type == StructureConstantsEnum.FENCE_PLAN_SANDSTONE_STONE_PARAPET) {
                  return new int[]{1121, needed};
               } else if (type == StructureConstantsEnum.FENCE_PLAN_POTTERY_STONE_PARAPET) {
                  return new int[]{776, needed};
               } else if (type == StructureConstantsEnum.FENCE_PLAN_MARBLE_STONE_PARAPET) {
                  return new int[]{786, needed};
               } else if (type == StructureConstantsEnum.FENCE_PLAN_STONE_IRON_PARAPET) {
                  if (currentState == StructureStateEnum.UNINITIALIZED) {
                     return new int[]{132, 1};
                  } else if (currentState.state < maxState.state - 1) {
                     return new int[]{132, maxState.state - 1 - currentState.state, 681, 1};
                  } else {
                     return currentState.state == maxState.state - 1 ? new int[]{681, 1} : new int[]{132, needed};
                  }
               } else if (type == StructureConstantsEnum.FENCE_PLAN_GARDESGARD_LOW) {
                  if (currentState == StructureStateEnum.UNINITIALIZED || currentState == StructureStateEnum.INITIALIZED) {
                     return new int[]{23, needed, 218, 1};
                  } else {
                     return currentState.state >= StructureStateEnum.INITIALIZED.state + 1 ? new int[]{23, needed, 218, 1} : new int[]{23, needed};
                  }
               } else if (type != StructureConstantsEnum.FENCE_PLAN_GARDESGARD_HIGH && type != StructureConstantsEnum.FENCE_PLAN_GARDESGARD_GATE) {
                  if (type == StructureConstantsEnum.FENCE_PLAN_STONEWALL_HIGH) {
                     return new int[]{132, needed};
                  } else if (type == StructureConstantsEnum.FENCE_PLAN_SLATE_TALL_STONE_WALL) {
                     return new int[]{1123, needed};
                  } else if (type == StructureConstantsEnum.FENCE_PLAN_ROUNDED_STONE_TALL_STONE_WALL) {
                     return new int[]{1122, needed};
                  } else if (type == StructureConstantsEnum.FENCE_PLAN_SANDSTONE_TALL_STONE_WALL) {
                     return new int[]{1121, needed};
                  } else if (type == StructureConstantsEnum.FENCE_PLAN_POTTERY_TALL_STONE_WALL) {
                     return new int[]{776, needed};
                  } else if (type == StructureConstantsEnum.FENCE_PLAN_MARBLE_TALL_STONE_WALL) {
                     return new int[]{786, needed};
                  } else if (type != StructureConstantsEnum.FENCE_PLAN_IRON && type != StructureConstantsEnum.FENCE_PLAN_IRON_GATE) {
                     if (type != StructureConstantsEnum.FENCE_PLAN_SLATE_IRON && type != StructureConstantsEnum.FENCE_PLAN_SLATE_IRON_GATE) {
                        if (type != StructureConstantsEnum.FENCE_PLAN_ROUNDED_STONE_IRON && type != StructureConstantsEnum.FENCE_PLAN_ROUNDED_STONE_IRON_GATE) {
                           if (type != StructureConstantsEnum.FENCE_PLAN_POTTERY_IRON && type != StructureConstantsEnum.FENCE_PLAN_POTTERY_IRON_GATE) {
                              if (type != StructureConstantsEnum.FENCE_PLAN_SANDSTONE_IRON && type != StructureConstantsEnum.FENCE_PLAN_SANDSTONE_IRON_GATE) {
                                 if (type != StructureConstantsEnum.FENCE_PLAN_MARBLE_IRON && type != StructureConstantsEnum.FENCE_PLAN_MARBLE_IRON_GATE) {
                                    if (type == StructureConstantsEnum.FENCE_PLAN_IRON_HIGH || type == StructureConstantsEnum.FENCE_PLAN_IRON_GATE_HIGH) {
                                       return currentState.state < fence.getFinishState().state - 2
                                          ? new int[]{132, fence.getFinishState().state - 2 - currentState.state, 681, 2}
                                          : new int[]{681, needed};
                                    } else if (type == StructureConstantsEnum.FENCE_PLAN_SLATE_HIGH_IRON_FENCE
                                       || type == StructureConstantsEnum.FENCE_PLAN_SLATE_HIGH_IRON_FENCE_GATE) {
                                       return currentState.state < fence.getFinishState().state - 2
                                          ? new int[]{1123, fence.getFinishState().state - 2 - currentState.state, 681, 2}
                                          : new int[]{681, needed};
                                    } else if (type == StructureConstantsEnum.FENCE_PLAN_ROUNDED_STONE_HIGH_IRON_FENCE
                                       || type == StructureConstantsEnum.FENCE_PLAN_ROUNDED_STONE_HIGH_IRON_FENCE_GATE) {
                                       return currentState.state < fence.getFinishState().state - 2
                                          ? new int[]{1122, fence.getFinishState().state - 2 - currentState.state, 681, 2}
                                          : new int[]{681, needed};
                                    } else if (type == StructureConstantsEnum.FENCE_PLAN_SANDSTONE_HIGH_IRON_FENCE
                                       || type == StructureConstantsEnum.FENCE_PLAN_SANDSTONE_HIGH_IRON_FENCE_GATE) {
                                       return currentState.state < fence.getFinishState().state - 2
                                          ? new int[]{1121, fence.getFinishState().state - 2 - currentState.state, 681, 2}
                                          : new int[]{681, needed};
                                    } else if (type == StructureConstantsEnum.FENCE_PLAN_POTTERY_HIGH_IRON_FENCE
                                       || type == StructureConstantsEnum.FENCE_PLAN_POTTERY_HIGH_IRON_FENCE_GATE) {
                                       return currentState.state < fence.getFinishState().state - 2
                                          ? new int[]{776, fence.getFinishState().state - 2 - currentState.state, 681, 2}
                                          : new int[]{681, needed};
                                    } else if (type == StructureConstantsEnum.FENCE_PLAN_MARBLE_HIGH_IRON_FENCE
                                       || type == StructureConstantsEnum.FENCE_PLAN_MARBLE_HIGH_IRON_FENCE_GATE) {
                                       return currentState.state < fence.getFinishState().state - 2
                                          ? new int[]{786, fence.getFinishState().state - 2 - currentState.state, 681, 2}
                                          : new int[]{681, needed};
                                    } else if (type == StructureConstantsEnum.FENCE_PLAN_MEDIUM_CHAIN) {
                                       return currentState.state < fence.getFinishState().state - 6
                                          ? new int[]{132, fence.getFinishState().state - 6 - currentState.state, 859, 6}
                                          : new int[]{859, needed};
                                    } else if (type == StructureConstantsEnum.FENCE_PLAN_SLATE_CHAIN_FENCE) {
                                       return currentState.state < fence.getFinishState().state - 6
                                          ? new int[]{1123, fence.getFinishState().state - 6 - currentState.state, 859, 6}
                                          : new int[]{859, needed};
                                    } else if (type == StructureConstantsEnum.FENCE_PLAN_ROUNDED_STONE_CHAIN_FENCE) {
                                       return currentState.state < fence.getFinishState().state - 6
                                          ? new int[]{1122, fence.getFinishState().state - 6 - currentState.state, 859, 6}
                                          : new int[]{859, needed};
                                    } else if (type == StructureConstantsEnum.FENCE_PLAN_SANDSTONE_CHAIN_FENCE) {
                                       return currentState.state < fence.getFinishState().state - 6
                                          ? new int[]{1121, fence.getFinishState().state - 6 - currentState.state, 859, 6}
                                          : new int[]{859, needed};
                                    } else if (type == StructureConstantsEnum.FENCE_PLAN_POTTERY_CHAIN_FENCE) {
                                       return currentState.state < fence.getFinishState().state - 6
                                          ? new int[]{776, fence.getFinishState().state - 6 - currentState.state, 859, 6}
                                          : new int[]{859, needed};
                                    } else if (type == StructureConstantsEnum.FENCE_PLAN_MARBLE_CHAIN_FENCE) {
                                       return currentState.state < fence.getFinishState().state - 6
                                          ? new int[]{786, fence.getFinishState().state - 6 - currentState.state, 859, 6}
                                          : new int[]{859, needed};
                                    } else if (type == StructureConstantsEnum.FENCE_PLAN_PORTCULLIS) {
                                       if (currentState.state < fence.getFinishState().state - 5) {
                                          return new int[]{132, needed - 5, 681, 2, 187, 2, 559, 1};
                                       } else if (currentState.state < fence.getFinishState().state - 3) {
                                          return new int[]{681, needed - 3, 187, 2, 559, 1};
                                       } else {
                                          return currentState.state < fence.getFinishState().state - 1
                                             ? new int[]{187, needed - 1, 559, 1}
                                             : new int[]{559, needed};
                                       }
                                    } else if (type == StructureConstantsEnum.FENCE_PLAN_SLATE_PORTCULLIS) {
                                       if (currentState.state < fence.getFinishState().state - 5) {
                                          return new int[]{1123, needed - 5, 681, 2, 187, 2, 559, 1};
                                       } else if (currentState.state < fence.getFinishState().state - 3) {
                                          return new int[]{681, needed - 3, 187, 2, 559, 1};
                                       } else {
                                          return currentState.state < fence.getFinishState().state - 1
                                             ? new int[]{187, needed - 1, 559, 1}
                                             : new int[]{559, needed};
                                       }
                                    } else if (type == StructureConstantsEnum.FENCE_PLAN_ROUNDED_STONE_PORTCULLIS) {
                                       if (currentState.state < fence.getFinishState().state - 5) {
                                          return new int[]{1122, needed - 5, 681, 2, 187, 2, 559, 1};
                                       } else if (currentState.state < fence.getFinishState().state - 3) {
                                          return new int[]{681, needed - 3, 187, 2, 559, 1};
                                       } else {
                                          return currentState.state < fence.getFinishState().state - 1
                                             ? new int[]{187, needed - 1, 559, 1}
                                             : new int[]{559, needed};
                                       }
                                    } else if (type == StructureConstantsEnum.FENCE_PLAN_SANDSTONE_PORTCULLIS) {
                                       if (currentState.state < fence.getFinishState().state - 5) {
                                          return new int[]{1121, needed - 5, 681, 2, 187, 2, 559, 1};
                                       } else if (currentState.state < fence.getFinishState().state - 3) {
                                          return new int[]{681, needed - 3, 187, 2, 559, 1};
                                       } else {
                                          return currentState.state < fence.getFinishState().state - 1
                                             ? new int[]{187, needed - 1, 559, 1}
                                             : new int[]{559, needed};
                                       }
                                    } else if (type == StructureConstantsEnum.FENCE_PLAN_POTTERY_PORTCULLIS) {
                                       if (currentState.state < fence.getFinishState().state - 5) {
                                          return new int[]{776, needed - 5, 681, 2, 187, 2, 559, 1};
                                       } else if (currentState.state < fence.getFinishState().state - 3) {
                                          return new int[]{681, needed - 3, 187, 2, 559, 1};
                                       } else {
                                          return currentState.state < fence.getFinishState().state - 1
                                             ? new int[]{187, needed - 1, 559, 1}
                                             : new int[]{559, needed};
                                       }
                                    } else if (type == StructureConstantsEnum.FENCE_PLAN_MARBLE_PORTCULLIS) {
                                       if (currentState.state < fence.getFinishState().state - 5) {
                                          return new int[]{786, needed - 5, 681, 2, 187, 2, 559, 1};
                                       } else if (currentState.state < fence.getFinishState().state - 3) {
                                          return new int[]{681, needed - 3, 187, 2, 559, 1};
                                       } else {
                                          return currentState.state < fence.getFinishState().state - 1
                                             ? new int[]{187, needed - 1, 559, 1}
                                             : new int[]{559, needed};
                                       }
                                    } else if (type == StructureConstantsEnum.FENCE_PLAN_WOVEN) {
                                       return new int[]{169, needed};
                                    } else if (type == StructureConstantsEnum.FENCE_PLAN_STONE) {
                                       return new int[]{132, needed};
                                    } else if (type == StructureConstantsEnum.FENCE_PLAN_SLATE) {
                                       return new int[]{1123, needed};
                                    } else if (type == StructureConstantsEnum.FENCE_PLAN_ROUNDED_STONE) {
                                       return new int[]{1122, needed};
                                    } else if (type == StructureConstantsEnum.FENCE_PLAN_POTTERY) {
                                       return new int[]{776, needed};
                                    } else if (type == StructureConstantsEnum.FENCE_PLAN_SANDSTONE) {
                                       return new int[]{1121, needed};
                                    } else if (type == StructureConstantsEnum.FENCE_PLAN_RENDERED) {
                                       return new int[]{1122, needed, 130, needed};
                                    } else if (type == StructureConstantsEnum.FENCE_PLAN_MARBLE) {
                                       return new int[]{786, needed};
                                    } else if (type == StructureConstantsEnum.FENCE_PLAN_CURB) {
                                       return new int[]{146, needed};
                                    } else if (type != StructureConstantsEnum.FENCE_PLAN_ROPE_HIGH && type != StructureConstantsEnum.FENCE_PLAN_ROPE_LOW) {
                                       return new int[]{-1};
                                    } else if (currentState == StructureStateEnum.UNINITIALIZED) {
                                       return new int[]{23, 1};
                                    } else {
                                       return currentState == StructureStateEnum.INITIALIZED ? new int[]{23, 1, 319, needed - 1} : new int[]{319, needed};
                                    }
                                 } else {
                                    return currentState.state < 10 ? new int[]{786, 10 - currentState.state, 681, 1} : new int[]{681, 1};
                                 }
                              } else {
                                 return currentState.state < 10 ? new int[]{1121, 10 - currentState.state, 681, 1} : new int[]{681, 1};
                              }
                           } else {
                              return currentState.state < 10 ? new int[]{776, 10 - currentState.state, 681, 1} : new int[]{681, 1};
                           }
                        } else {
                           return currentState.state < 10 ? new int[]{1122, 10 - currentState.state, 681, 1} : new int[]{681, 1};
                        }
                     } else {
                        return currentState.state < 10 ? new int[]{1123, 10 - currentState.state, 681, 1} : new int[]{681, 1};
                     }
                  } else {
                     return currentState.state < 10 ? new int[]{132, 10 - currentState.state, 681, 1} : new int[]{681, 1};
                  }
               } else if (currentState == StructureStateEnum.UNINITIALIZED || currentState == StructureStateEnum.INITIALIZED) {
                  return new int[]{23, needed, 218, 1};
               } else {
                  return currentState.state >= StructureStateEnum.INITIALIZED.state + 1 ? new int[]{23, needed, 218, 1} : new int[]{23, needed};
               }
            } else if (currentState == StructureStateEnum.UNINITIALIZED) {
               return new int[]{23, 1};
            } else if (currentState == StructureStateEnum.INITIALIZED) {
               return new int[]{23, 1, 22, needed - 1, 218, 1};
            } else {
               return currentState.state == StructureStateEnum.INITIALIZED.state + 1 ? new int[]{22, needed, 218, 1} : new int[]{22, needed};
            }
         } else if (currentState == StructureStateEnum.UNINITIALIZED) {
            return new int[]{23, 1};
         } else if (currentState == StructureStateEnum.INITIALIZED) {
            return new int[]{23, 1, 22, needed - 1, 218, 1};
         } else {
            return currentState.state == StructureStateEnum.INITIALIZED.state + 1 ? new int[]{22, needed, 218, 1} : new int[]{22, needed};
         }
      }
   }

   public static final int[] getItemTemplatesNeededForFence(Fence fence) {
      StructureConstantsEnum type = fence.getType();
      if (fence.isFinished()) {
         return new int[]{-1};
      } else if (type == StructureConstantsEnum.FENCE_PLAN_PALISADE || type == StructureConstantsEnum.FENCE_PLAN_PALISADE_GATE) {
         return new int[]{385};
      } else if (type == StructureConstantsEnum.FENCE_PLAN_STONEWALL) {
         return new int[]{146};
      } else if (type == StructureConstantsEnum.FENCE_PLAN_MEDIUM_CHAIN) {
         StructureStateEnum state = fence.getState();
         return state.state < fence.getFinishState().state - 6 ? new int[]{132} : new int[]{859};
      } else if (type == StructureConstantsEnum.FENCE_PLAN_SLATE_CHAIN_FENCE) {
         StructureStateEnum state = fence.getState();
         return state.state < fence.getFinishState().state - 6 ? new int[]{1123} : new int[]{859};
      } else if (type == StructureConstantsEnum.FENCE_PLAN_ROUNDED_STONE_CHAIN_FENCE) {
         StructureStateEnum state = fence.getState();
         return state.state < fence.getFinishState().state - 6 ? new int[]{1122} : new int[]{859};
      } else if (type == StructureConstantsEnum.FENCE_PLAN_SANDSTONE_CHAIN_FENCE) {
         StructureStateEnum state = fence.getState();
         return state.state < fence.getFinishState().state - 6 ? new int[]{1121} : new int[]{859};
      } else if (type == StructureConstantsEnum.FENCE_PLAN_POTTERY_CHAIN_FENCE) {
         StructureStateEnum state = fence.getState();
         return state.state < fence.getFinishState().state - 6 ? new int[]{776} : new int[]{859};
      } else if (type == StructureConstantsEnum.FENCE_PLAN_MARBLE_CHAIN_FENCE) {
         StructureStateEnum state = fence.getState();
         return state.state < fence.getFinishState().state - 6 ? new int[]{786} : new int[]{859};
      } else if (type == StructureConstantsEnum.FENCE_PLAN_PORTCULLIS) {
         StructureStateEnum state = fence.getState();
         int brickStage = fence.getFinishState().state - 5;
         int barStage = fence.getFinishState().state - 3;
         int wheelStage = fence.getFinishState().state - 1;
         if (state.state < brickStage) {
            return new int[]{132};
         } else if (state.state < barStage) {
            return new int[]{681};
         } else {
            return state.state < wheelStage ? new int[]{187} : new int[]{559};
         }
      } else if (type == StructureConstantsEnum.FENCE_PLAN_SLATE_PORTCULLIS) {
         StructureStateEnum state = fence.getState();
         int brickStage = fence.getFinishState().state - 5;
         int barStage = fence.getFinishState().state - 3;
         int wheelStage = fence.getFinishState().state - 1;
         if (state.state < brickStage) {
            return new int[]{1123};
         } else if (state.state < barStage) {
            return new int[]{681};
         } else {
            return state.state < wheelStage ? new int[]{187} : new int[]{559};
         }
      } else if (type == StructureConstantsEnum.FENCE_PLAN_ROUNDED_STONE_PORTCULLIS) {
         StructureStateEnum state = fence.getState();
         int brickStage = fence.getFinishState().state - 5;
         int barStage = fence.getFinishState().state - 3;
         int wheelStage = fence.getFinishState().state - 1;
         if (state.state < brickStage) {
            return new int[]{1122};
         } else if (state.state < barStage) {
            return new int[]{681};
         } else {
            return state.state < wheelStage ? new int[]{187} : new int[]{559};
         }
      } else if (type == StructureConstantsEnum.FENCE_PLAN_SANDSTONE_PORTCULLIS) {
         StructureStateEnum state = fence.getState();
         int brickStage = fence.getFinishState().state - 5;
         int barStage = fence.getFinishState().state - 3;
         int wheelStage = fence.getFinishState().state - 1;
         if (state.state < brickStage) {
            return new int[]{1121};
         } else if (state.state < barStage) {
            return new int[]{681};
         } else {
            return state.state < wheelStage ? new int[]{187} : new int[]{559};
         }
      } else if (type == StructureConstantsEnum.FENCE_PLAN_POTTERY_PORTCULLIS) {
         StructureStateEnum state = fence.getState();
         int brickStage = fence.getFinishState().state - 5;
         int barStage = fence.getFinishState().state - 3;
         int wheelStage = fence.getFinishState().state - 1;
         if (state.state < brickStage) {
            return new int[]{776};
         } else if (state.state < barStage) {
            return new int[]{681};
         } else {
            return state.state < wheelStage ? new int[]{187} : new int[]{559};
         }
      } else if (type == StructureConstantsEnum.FENCE_PLAN_MARBLE_PORTCULLIS) {
         StructureStateEnum state = fence.getState();
         int brickStage = fence.getFinishState().state - 5;
         int barStage = fence.getFinishState().state - 3;
         int wheelStage = fence.getFinishState().state - 1;
         if (state.state < brickStage) {
            return new int[]{786};
         } else if (state.state < barStage) {
            return new int[]{681};
         } else {
            return state.state < wheelStage ? new int[]{187} : new int[]{559};
         }
      } else if (type == StructureConstantsEnum.FENCE_PLAN_WOODEN_CRUDE || type == StructureConstantsEnum.FENCE_PLAN_WOODEN_GATE_CRUDE) {
         StructureStateEnum state = fence.getState();
         if (state == StructureStateEnum.UNINITIALIZED || state == StructureStateEnum.INITIALIZED) {
            return new int[]{23};
         } else {
            return state.state == StructureStateEnum.INITIALIZED.state + 1 ? new int[]{22, 218} : new int[]{22};
         }
      } else if (type == StructureConstantsEnum.FENCE_PLAN_WOODEN || type == StructureConstantsEnum.FENCE_PLAN_WOODEN_GATE) {
         StructureStateEnum state = fence.getState();
         if (state == StructureStateEnum.UNINITIALIZED || state == StructureStateEnum.INITIALIZED) {
            return new int[]{23};
         } else {
            return state.state == StructureStateEnum.INITIALIZED.state + 1 ? new int[]{22, 218} : new int[]{22};
         }
      } else if (type == StructureConstantsEnum.FENCE_PLAN_WOODEN_PARAPET) {
         StructureStateEnum state = fence.getState();
         if (state == StructureStateEnum.UNINITIALIZED || state == StructureStateEnum.INITIALIZED) {
            return new int[]{23};
         } else {
            return state.state >= StructureStateEnum.INITIALIZED.state + 1 ? new int[]{22, 218} : new int[]{22};
         }
      } else if (type == StructureConstantsEnum.FENCE_PLAN_STONE_PARAPET) {
         return new int[]{132};
      } else if (type == StructureConstantsEnum.FENCE_PLAN_SLATE_STONE_PARAPET) {
         return new int[]{1123};
      } else if (type == StructureConstantsEnum.FENCE_PLAN_ROUNDED_STONE_STONE_PARAPET) {
         return new int[]{1122};
      } else if (type == StructureConstantsEnum.FENCE_PLAN_SANDSTONE_STONE_PARAPET) {
         return new int[]{1121};
      } else if (type == StructureConstantsEnum.FENCE_PLAN_POTTERY_STONE_PARAPET) {
         return new int[]{776};
      } else if (type == StructureConstantsEnum.FENCE_PLAN_MARBLE_STONE_PARAPET) {
         return new int[]{786};
      } else if (type == StructureConstantsEnum.FENCE_PLAN_STONE_IRON_PARAPET) {
         StructureStateEnum state = fence.getState();
         if (state == StructureStateEnum.UNINITIALIZED) {
            return new int[]{132};
         } else if (state.state < fence.getFinishState().state - 1) {
            return new int[]{132};
         } else {
            return state.state == fence.getFinishState().state - 1 ? new int[]{681} : new int[]{132};
         }
      } else if (type == StructureConstantsEnum.FENCE_PLAN_GARDESGARD_LOW) {
         StructureStateEnum state = fence.getState();
         if (state == StructureStateEnum.UNINITIALIZED || state == StructureStateEnum.INITIALIZED) {
            return new int[]{23};
         } else {
            return state.state == StructureStateEnum.INITIALIZED.state + 1 ? new int[]{23, 218} : new int[]{23};
         }
      } else if (type == StructureConstantsEnum.FENCE_PLAN_GARDESGARD_HIGH || type == StructureConstantsEnum.FENCE_PLAN_GARDESGARD_GATE) {
         StructureStateEnum state = fence.getState();
         if (state == StructureStateEnum.UNINITIALIZED || state == StructureStateEnum.INITIALIZED) {
            return new int[]{23};
         } else {
            return state.state == StructureStateEnum.INITIALIZED.state + 1 ? new int[]{23, 218} : new int[]{23};
         }
      } else if (type == StructureConstantsEnum.FENCE_PLAN_STONEWALL_HIGH) {
         return new int[]{132};
      } else if (type == StructureConstantsEnum.FENCE_PLAN_SLATE_TALL_STONE_WALL) {
         return new int[]{1123};
      } else if (type == StructureConstantsEnum.FENCE_PLAN_ROUNDED_STONE_TALL_STONE_WALL) {
         return new int[]{1122};
      } else if (type == StructureConstantsEnum.FENCE_PLAN_SANDSTONE_TALL_STONE_WALL) {
         return new int[]{1121};
      } else if (type == StructureConstantsEnum.FENCE_PLAN_POTTERY_TALL_STONE_WALL) {
         return new int[]{776};
      } else if (type == StructureConstantsEnum.FENCE_PLAN_MARBLE_TALL_STONE_WALL) {
         return new int[]{786};
      } else if (type == StructureConstantsEnum.FENCE_PLAN_IRON || type == StructureConstantsEnum.FENCE_PLAN_IRON_GATE) {
         StructureStateEnum state = fence.getState();
         return state.state < 10 ? new int[]{132} : new int[]{681};
      } else if (type == StructureConstantsEnum.FENCE_PLAN_SLATE_IRON || type == StructureConstantsEnum.FENCE_PLAN_SLATE_IRON_GATE) {
         StructureStateEnum state = fence.getState();
         return state.state < 10 ? new int[]{1123} : new int[]{681};
      } else if (type == StructureConstantsEnum.FENCE_PLAN_ROUNDED_STONE_IRON || type == StructureConstantsEnum.FENCE_PLAN_ROUNDED_STONE_IRON_GATE) {
         StructureStateEnum state = fence.getState();
         return state.state < 10 ? new int[]{1122} : new int[]{681};
      } else if (type == StructureConstantsEnum.FENCE_PLAN_POTTERY_IRON || type == StructureConstantsEnum.FENCE_PLAN_POTTERY_IRON_GATE) {
         StructureStateEnum state = fence.getState();
         return state.state < 10 ? new int[]{776} : new int[]{681};
      } else if (type == StructureConstantsEnum.FENCE_PLAN_SANDSTONE_IRON || type == StructureConstantsEnum.FENCE_PLAN_SANDSTONE_IRON_GATE) {
         StructureStateEnum state = fence.getState();
         return state.state < 10 ? new int[]{1121} : new int[]{681};
      } else if (type == StructureConstantsEnum.FENCE_PLAN_RENDERED_IRON || type == StructureConstantsEnum.FENCE_PLAN_RENDERED_IRON_GATE) {
         StructureStateEnum state = fence.getState();
         if (state.state < 10) {
            return new int[]{132};
         } else {
            return state.state < 20 ? new int[]{130} : new int[]{681};
         }
      } else if (type == StructureConstantsEnum.FENCE_PLAN_MARBLE_IRON || type == StructureConstantsEnum.FENCE_PLAN_MARBLE_IRON_GATE) {
         StructureStateEnum state = fence.getState();
         return state.state < 10 ? new int[]{786} : new int[]{681};
      } else if (type == StructureConstantsEnum.FENCE_PLAN_IRON_HIGH || type == StructureConstantsEnum.FENCE_PLAN_IRON_GATE_HIGH) {
         StructureStateEnum state = fence.getState();
         return state.state < fence.getFinishState().state - 2 ? new int[]{132} : new int[]{681};
      } else if (type == StructureConstantsEnum.FENCE_PLAN_SLATE_HIGH_IRON_FENCE || type == StructureConstantsEnum.FENCE_PLAN_SLATE_HIGH_IRON_FENCE_GATE) {
         StructureStateEnum state = fence.getState();
         return state.state < fence.getFinishState().state - 2 ? new int[]{1123} : new int[]{681};
      } else if (type == StructureConstantsEnum.FENCE_PLAN_ROUNDED_STONE_HIGH_IRON_FENCE
         || type == StructureConstantsEnum.FENCE_PLAN_ROUNDED_STONE_HIGH_IRON_FENCE_GATE) {
         StructureStateEnum state = fence.getState();
         return state.state < fence.getFinishState().state - 2 ? new int[]{1122} : new int[]{681};
      } else if (type == StructureConstantsEnum.FENCE_PLAN_SANDSTONE_HIGH_IRON_FENCE
         || type == StructureConstantsEnum.FENCE_PLAN_SANDSTONE_HIGH_IRON_FENCE_GATE) {
         StructureStateEnum state = fence.getState();
         return state.state < fence.getFinishState().state - 2 ? new int[]{1121} : new int[]{681};
      } else if (type == StructureConstantsEnum.FENCE_PLAN_POTTERY_HIGH_IRON_FENCE || type == StructureConstantsEnum.FENCE_PLAN_POTTERY_HIGH_IRON_FENCE_GATE) {
         StructureStateEnum state = fence.getState();
         return state.state < fence.getFinishState().state - 2 ? new int[]{776} : new int[]{681};
      } else if (type == StructureConstantsEnum.FENCE_PLAN_MARBLE_HIGH_IRON_FENCE || type == StructureConstantsEnum.FENCE_PLAN_MARBLE_HIGH_IRON_FENCE_GATE) {
         StructureStateEnum state = fence.getState();
         return state.state < fence.getFinishState().state - 2 ? new int[]{786} : new int[]{681};
      } else if (type == StructureConstantsEnum.FENCE_PLAN_WOVEN) {
         return new int[]{169};
      } else if (type == StructureConstantsEnum.FENCE_PLAN_STONE) {
         return new int[]{132};
      } else if (type == StructureConstantsEnum.FENCE_PLAN_SLATE) {
         return new int[]{1123};
      } else if (type == StructureConstantsEnum.FENCE_PLAN_ROUNDED_STONE) {
         return new int[]{1122};
      } else if (type == StructureConstantsEnum.FENCE_PLAN_POTTERY) {
         return new int[]{776};
      } else if (type == StructureConstantsEnum.FENCE_PLAN_SANDSTONE) {
         return new int[]{1121};
      } else if (type == StructureConstantsEnum.FENCE_PLAN_RENDERED) {
         return new int[]{132};
      } else if (type == StructureConstantsEnum.FENCE_PLAN_MARBLE) {
         return new int[]{786};
      } else if (type == StructureConstantsEnum.FENCE_PLAN_CURB) {
         return new int[]{146};
      } else if (type != StructureConstantsEnum.FENCE_PLAN_ROPE_HIGH && type != StructureConstantsEnum.FENCE_PLAN_ROPE_LOW) {
         logger.fine("hit default return");
         return new int[]{-1};
      } else {
         StructureStateEnum state = fence.getState();
         return state != StructureStateEnum.UNINITIALIZED && state != StructureStateEnum.INITIALIZED ? new int[]{319} : new int[]{23};
      }
   }

   @Override
   public final boolean isDoor() {
      switch(this.type) {
         case FENCE_GARDESGARD_GATE:
         case FENCE_WOODEN_GATE:
         case FENCE_WOODEN_CRUDE_GATE:
         case FENCE_IRON_GATE:
         case FENCE_PLAN_PALISADE_GATE:
         case FENCE_PALISADE_GATE:
         case FENCE_PLAN_PORTCULLIS:
         case FENCE_SLATE_PORTCULLIS:
         case FENCE_ROUNDED_STONE_PORTCULLIS:
         case FENCE_SANDSTONE_PORTCULLIS:
         case FENCE_RENDERED_PORTCULLIS:
         case FENCE_POTTERY_PORTCULLIS:
         case FENCE_MARBLE_PORTCULLIS:
         case FENCE_PLAN_IRON_GATE_HIGH:
         case FENCE_SLATE_HIGH_IRON_FENCE_GATE:
         case FENCE_ROUNDED_STONE_HIGH_IRON_FENCE_GATE:
         case FENCE_SANDSTONE_HIGH_IRON_FENCE_GATE:
         case FENCE_RENDERED_HIGH_IRON_FENCE_GATE:
         case FENCE_POTTERY_HIGH_IRON_FENCE_GATE:
         case FENCE_MARBLE_HIGH_IRON_FENCE_GATE:
         case FENCE_PORTCULLIS:
         case FENCE_PLAN_WOODEN_GATE:
         case FENCE_PLAN_WOODEN_GATE_CRUDE:
         case FENCE_PLAN_GARDESGARD_GATE:
         case FENCE_IRON_GATE_HIGH:
         case FENCE_SLATE_IRON_GATE:
         case FENCE_ROUNDED_STONE_IRON_GATE:
         case FENCE_POTTERY_IRON_GATE:
         case FENCE_SANDSTONE_IRON_GATE:
         case FENCE_RENDERED_IRON_GATE:
         case FENCE_MARBLE_IRON_GATE:
         case FENCE_PLAN_IRON_GATE:
            return true;
         default:
            return false;
      }
   }

   public final double getDifficulty() {
      switch(this.type) {
         case FENCE_WOVEN:
         case FENCE_WOODEN:
         case FENCE_PLAN_WOODEN:
         case FENCE_PLAN_WOVEN:
            return 1.0;
         case FENCE_ROPE_LOW:
         case FENCE_RUBBLE:
         case FLOWERBED_BLUE:
         case FLOWERBED_GREENISH_YELLOW:
         case FLOWERBED_ORANGE_RED:
         case FLOWERBED_PURPLE:
         case FLOWERBED_WHITE:
         case FLOWERBED_WHITE_DOTTED:
         case FLOWERBED_YELLOW:
         case FENCE_ROPE_HIGH:
         case FENCE_PLAN_ROPE_HIGH:
         case FENCE_WOODEN_CRUDE:
         case HEDGE_FLOWER1_MEDIUM:
         case HEDGE_FLOWER2_MEDIUM:
         case HEDGE_FLOWER3_MEDIUM:
         case HEDGE_FLOWER4_MEDIUM:
         case HEDGE_FLOWER5_MEDIUM:
         case HEDGE_FLOWER6_MEDIUM:
         case HEDGE_FLOWER7_MEDIUM:
         case HEDGE_FLOWER1_HIGH:
         case HEDGE_FLOWER2_HIGH:
         case HEDGE_FLOWER3_HIGH:
         case HEDGE_FLOWER4_HIGH:
         case HEDGE_FLOWER5_HIGH:
         case HEDGE_FLOWER6_HIGH:
         case HEDGE_FLOWER7_HIGH:
         case FENCE_MAGIC_STONE:
         case FENCE_MAGIC_ICE:
         case FENCE_MAGIC_FIRE:
         case FENCE_PLAN_PORTCULLIS:
         case FENCE_PLAN_MEDIUM_CHAIN:
         case FENCE_PLAN_CURB:
         case FENCE_PLAN_ROPE_LOW:
         case FENCE_PORTCULLIS:
         case FENCE_PLAN_WOODEN_CRUDE:
         case FENCE_SIEGEWALL:
         case FENCE_MEDIUM_CHAIN:
         default:
            if (this.isLowHedge()) {
               return 10.0;
            } else if (this.isMediumHedge()) {
               return 20.0;
            } else {
               if (this.isHighHedge()) {
                  return 40.0;
               }

               return 1.0;
            }
         case FENCE_GARDESGARD_LOW:
         case FENCE_IRON:
         case FENCE_STONEWALL:
         case FENCE_WOODEN_GATE:
         case FENCE_IRON_GATE:
         case FENCE_STONE:
         case FENCE_PLAN_PALISADE_GATE:
         case FENCE_PALISADE_GATE:
         case FENCE_PLAN_STONEWALL:
         case FENCE_PLAN_STONE:
         case FENCE_PLAN_SLATE:
         case FENCE_SLATE:
         case FENCE_PLAN_ROUNDED_STONE:
         case FENCE_ROUNDED_STONE:
         case FENCE_PLAN_POTTERY:
         case FENCE_POTTERY:
         case FENCE_PLAN_SANDSTONE:
         case FENCE_SANDSTONE:
         case FENCE_PLAN_MARBLE:
         case FENCE_MARBLE:
         case FENCE_RENDERED:
         case FENCE_PLAN_WOODEN_GATE:
         case FENCE_PLAN_GARDESGARD_LOW:
         case FENCE_SLATE_IRON:
         case FENCE_ROUNDED_STONE_IRON:
         case FENCE_POTTERY_IRON:
         case FENCE_SANDSTONE_IRON:
         case FENCE_RENDERED_IRON:
         case FENCE_MARBLE_IRON:
         case FENCE_SLATE_IRON_GATE:
         case FENCE_ROUNDED_STONE_IRON_GATE:
         case FENCE_POTTERY_IRON_GATE:
         case FENCE_SANDSTONE_IRON_GATE:
         case FENCE_RENDERED_IRON_GATE:
         case FENCE_MARBLE_IRON_GATE:
         case FENCE_PLAN_IRON:
         case FENCE_PLAN_IRON_GATE:
         case FENCE_PLAN_RENDERED:
            return 10.0;
         case FENCE_STONE_PARAPET:
         case FENCE_PLAN_STONE_PARAPET:
            return 40.0;
         case FENCE_WOODEN_PARAPET:
         case FENCE_SANDSTONE_STONE_PARAPET:
         case FENCE_SLATE_STONE_PARAPET:
         case FENCE_ROUNDED_STONE_STONE_PARAPET:
         case FENCE_RENDERED_STONE_PARAPET:
         case FENCE_POTTERY_STONE_PARAPET:
         case FENCE_MARBLE_STONE_PARAPET:
         case FENCE_SLATE_CHAIN_FENCE:
         case FENCE_ROUNDED_STONE_CHAIN_FENCE:
         case FENCE_SANDSTONE_CHAIN_FENCE:
         case FENCE_RENDERED_CHAIN_FENCE:
         case FENCE_POTTERY_CHAIN_FENCE:
         case FENCE_MARBLE_CHAIN_FENCE:
         case FENCE_PLAN_STONEWALL_HIGH:
         case FENCE_STONEWALL_HIGH:
         case FENCE_SLATE_TALL_STONE_WALL:
         case FENCE_ROUNDED_STONE_TALL_STONE_WALL:
         case FENCE_SANDSTONE_TALL_STONE_WALL:
         case FENCE_RENDERED_TALL_STONE_WALL:
         case FENCE_POTTERY_TALL_STONE_WALL:
         case FENCE_MARBLE_TALL_STONE_WALL:
         case FENCE_SLATE_PORTCULLIS:
         case FENCE_ROUNDED_STONE_PORTCULLIS:
         case FENCE_SANDSTONE_PORTCULLIS:
         case FENCE_RENDERED_PORTCULLIS:
         case FENCE_POTTERY_PORTCULLIS:
         case FENCE_MARBLE_PORTCULLIS:
         case FENCE_PLAN_SLATE_TALL_STONE_WALL:
         case FENCE_PLAN_ROUNDED_STONE_TALL_STONE_WALL:
         case FENCE_PLAN_SANDSTONE_TALL_STONE_WALL:
         case FENCE_PLAN_RENDERED_TALL_STONE_WALL:
         case FENCE_PLAN_POTTERY_TALL_STONE_WALL:
         case FENCE_PLAN_MARBLE_TALL_STONE_WALL:
         case FENCE_PLAN_SLATE_PORTCULLIS:
         case FENCE_PLAN_ROUNDED_STONE_PORTCULLIS:
         case FENCE_PLAN_SANDSTONE_PORTCULLIS:
         case FENCE_PLAN_RENDERED_PORTCULLIS:
         case FENCE_PLAN_POTTERY_PORTCULLIS:
         case FENCE_PLAN_MARBLE_PORTCULLIS:
         case FENCE_PLAN_WOODEN_PARAPET:
         case FENCE_PLAN_SLATE_STONE_PARAPET:
         case FENCE_PLAN_ROUNDED_STONE_STONE_PARAPET:
         case FENCE_PLAN_RENDERED_STONE_PARAPET:
         case FENCE_PLAN_POTTERY_STONE_PARAPET:
         case FENCE_PLAN_MARBLE_STONE_PARAPET:
         case FENCE_PLAN_SLATE_CHAIN_FENCE:
         case FENCE_PLAN_ROUNDED_STONE_CHAIN_FENCE:
         case FENCE_PLAN_SANDSTONE_CHAIN_FENCE:
         case FENCE_PLAN_RENDERED_CHAIN_FENCE:
         case FENCE_PLAN_POTTERY_CHAIN_FENCE:
         case FENCE_PLAN_MARBLE_CHAIN_FENCE:
         case FENCE_PLAN_IRON_HIGH:
         case FENCE_PLAN_IRON_GATE_HIGH:
         case FENCE_SLATE_HIGH_IRON_FENCE_GATE:
         case FENCE_ROUNDED_STONE_HIGH_IRON_FENCE_GATE:
         case FENCE_SANDSTONE_HIGH_IRON_FENCE_GATE:
         case FENCE_RENDERED_HIGH_IRON_FENCE_GATE:
         case FENCE_POTTERY_HIGH_IRON_FENCE_GATE:
         case FENCE_MARBLE_HIGH_IRON_FENCE_GATE:
         case FENCE_SLATE_HIGH_IRON_FENCE:
         case FENCE_ROUNDED_STONE_HIGH_IRON_FENCE:
         case FENCE_SANDSTONE_HIGH_IRON_FENCE:
         case FENCE_RENDERED_HIGH_IRON_FENCE:
         case FENCE_POTTERY_HIGH_IRON_FENCE:
         case FENCE_MARBLE_HIGH_IRON_FENCE:
         case FENCE_PLAN_SLATE_HIGH_IRON_FENCE_GATE:
         case FENCE_PLAN_ROUNDED_STONE_HIGH_IRON_FENCE_GATE:
         case FENCE_PLAN_SANDSTONE_HIGH_IRON_FENCE_GATE:
         case FENCE_PLAN_RENDERED_HIGH_IRON_FENCE_GATE:
         case FENCE_PLAN_POTTERY_HIGH_IRON_FENCE_GATE:
         case FENCE_PLAN_MARBLE_HIGH_IRON_FENCE_GATE:
         case FENCE_PLAN_SLATE_HIGH_IRON_FENCE:
         case FENCE_PLAN_ROUNDED_STONE_HIGH_IRON_FENCE:
         case FENCE_PLAN_SANDSTONE_HIGH_IRON_FENCE:
         case FENCE_PLAN_RENDERED_HIGH_IRON_FENCE:
         case FENCE_PLAN_POTTERY_HIGH_IRON_FENCE:
         case FENCE_PLAN_MARBLE_HIGH_IRON_FENCE:
         case FENCE_PLAN_SANDSTONE_STONE_PARAPET:
         case FENCE_IRON_HIGH:
         case FENCE_IRON_GATE_HIGH:
            return 20.0;
         case FENCE_STONE_IRON_PARAPET:
         case FENCE_PLAN_STONE_IRON_PARAPET:
            return 50.0;
         case FENCE_GARDESGARD_GATE:
         case FENCE_PLAN_GARDESGARD_GATE:
         case FENCE_GARDESGARD_HIGH:
         case FENCE_PLAN_GARDESGARD_HIGH:
            return 15.0;
         case FENCE_WOODEN_CRUDE_GATE:
         case FENCE_PLAN_PALISADE:
         case FENCE_PALISADE:
         case FENCE_PLAN_WOODEN_GATE_CRUDE:
            return 5.0;
      }
   }

   public static final boolean isIron(StructureConstantsEnum fenceType) {
      switch(fenceType) {
         case FENCE_IRON:
         case FENCE_IRON_GATE:
         case FENCE_SLATE_CHAIN_FENCE:
         case FENCE_ROUNDED_STONE_CHAIN_FENCE:
         case FENCE_SANDSTONE_CHAIN_FENCE:
         case FENCE_RENDERED_CHAIN_FENCE:
         case FENCE_POTTERY_CHAIN_FENCE:
         case FENCE_MARBLE_CHAIN_FENCE:
         case FENCE_PLAN_MEDIUM_CHAIN:
         case FENCE_PLAN_SLATE_CHAIN_FENCE:
         case FENCE_PLAN_ROUNDED_STONE_CHAIN_FENCE:
         case FENCE_PLAN_SANDSTONE_CHAIN_FENCE:
         case FENCE_PLAN_RENDERED_CHAIN_FENCE:
         case FENCE_PLAN_POTTERY_CHAIN_FENCE:
         case FENCE_PLAN_MARBLE_CHAIN_FENCE:
         case FENCE_PLAN_IRON_HIGH:
         case FENCE_PLAN_IRON_GATE_HIGH:
         case FENCE_SLATE_HIGH_IRON_FENCE_GATE:
         case FENCE_ROUNDED_STONE_HIGH_IRON_FENCE_GATE:
         case FENCE_SANDSTONE_HIGH_IRON_FENCE_GATE:
         case FENCE_RENDERED_HIGH_IRON_FENCE_GATE:
         case FENCE_POTTERY_HIGH_IRON_FENCE_GATE:
         case FENCE_MARBLE_HIGH_IRON_FENCE_GATE:
         case FENCE_SLATE_HIGH_IRON_FENCE:
         case FENCE_ROUNDED_STONE_HIGH_IRON_FENCE:
         case FENCE_SANDSTONE_HIGH_IRON_FENCE:
         case FENCE_RENDERED_HIGH_IRON_FENCE:
         case FENCE_POTTERY_HIGH_IRON_FENCE:
         case FENCE_MARBLE_HIGH_IRON_FENCE:
         case FENCE_PLAN_SLATE_HIGH_IRON_FENCE_GATE:
         case FENCE_PLAN_ROUNDED_STONE_HIGH_IRON_FENCE_GATE:
         case FENCE_PLAN_SANDSTONE_HIGH_IRON_FENCE_GATE:
         case FENCE_PLAN_RENDERED_HIGH_IRON_FENCE_GATE:
         case FENCE_PLAN_POTTERY_HIGH_IRON_FENCE_GATE:
         case FENCE_PLAN_MARBLE_HIGH_IRON_FENCE_GATE:
         case FENCE_PLAN_SLATE_HIGH_IRON_FENCE:
         case FENCE_PLAN_ROUNDED_STONE_HIGH_IRON_FENCE:
         case FENCE_PLAN_SANDSTONE_HIGH_IRON_FENCE:
         case FENCE_PLAN_RENDERED_HIGH_IRON_FENCE:
         case FENCE_PLAN_POTTERY_HIGH_IRON_FENCE:
         case FENCE_PLAN_MARBLE_HIGH_IRON_FENCE:
         case FENCE_IRON_HIGH:
         case FENCE_IRON_GATE_HIGH:
         case FENCE_MEDIUM_CHAIN:
         case FENCE_SLATE_IRON:
         case FENCE_ROUNDED_STONE_IRON:
         case FENCE_POTTERY_IRON:
         case FENCE_SANDSTONE_IRON:
         case FENCE_MARBLE_IRON:
         case FENCE_SLATE_IRON_GATE:
         case FENCE_ROUNDED_STONE_IRON_GATE:
         case FENCE_POTTERY_IRON_GATE:
         case FENCE_SANDSTONE_IRON_GATE:
         case FENCE_RENDERED_IRON_GATE:
         case FENCE_MARBLE_IRON_GATE:
         case FENCE_PLAN_IRON:
         case FENCE_PLAN_IRON_GATE:
         case FENCE_PLAN_SLATE_IRON:
         case FENCE_PLAN_ROUNDED_STONE_IRON:
         case FENCE_PLAN_POTTERY_IRON:
         case FENCE_PLAN_SANDSTONE_IRON:
         case FENCE_PLAN_MARBLE_IRON:
         case FENCE_PLAN_SLATE_IRON_GATE:
         case FENCE_PLAN_ROUNDED_STONE_IRON_GATE:
         case FENCE_PLAN_POTTERY_IRON_GATE:
         case FENCE_PLAN_SANDSTONE_IRON_GATE:
         case FENCE_PLAN_MARBLE_IRON_GATE:
            return true;
         case FENCE_STONEWALL:
         case FENCE_WOODEN:
         case FENCE_STONE_PARAPET:
         case FENCE_WOODEN_PARAPET:
         case FENCE_STONE_IRON_PARAPET:
         case FENCE_ROPE_HIGH:
         case FENCE_PLAN_ROPE_HIGH:
         case FENCE_GARDESGARD_GATE:
         case FENCE_WOODEN_GATE:
         case FENCE_WOODEN_CRUDE_GATE:
         case FENCE_WOODEN_CRUDE:
         case FENCE_STONE:
         case FENCE_SANDSTONE_STONE_PARAPET:
         case FENCE_SLATE_STONE_PARAPET:
         case FENCE_ROUNDED_STONE_STONE_PARAPET:
         case FENCE_RENDERED_STONE_PARAPET:
         case FENCE_POTTERY_STONE_PARAPET:
         case FENCE_MARBLE_STONE_PARAPET:
         case HEDGE_FLOWER1_MEDIUM:
         case HEDGE_FLOWER2_MEDIUM:
         case HEDGE_FLOWER3_MEDIUM:
         case HEDGE_FLOWER4_MEDIUM:
         case HEDGE_FLOWER5_MEDIUM:
         case HEDGE_FLOWER6_MEDIUM:
         case HEDGE_FLOWER7_MEDIUM:
         case HEDGE_FLOWER1_HIGH:
         case HEDGE_FLOWER2_HIGH:
         case HEDGE_FLOWER3_HIGH:
         case HEDGE_FLOWER4_HIGH:
         case HEDGE_FLOWER5_HIGH:
         case HEDGE_FLOWER6_HIGH:
         case HEDGE_FLOWER7_HIGH:
         case FENCE_MAGIC_STONE:
         case FENCE_MAGIC_ICE:
         case FENCE_MAGIC_FIRE:
         case FENCE_PLAN_PALISADE:
         case FENCE_PLAN_PALISADE_GATE:
         case FENCE_PALISADE:
         case FENCE_PALISADE_GATE:
         case FENCE_PLAN_STONEWALL_HIGH:
         case FENCE_STONEWALL_HIGH:
         case FENCE_PLAN_PORTCULLIS:
         case FENCE_SLATE_TALL_STONE_WALL:
         case FENCE_ROUNDED_STONE_TALL_STONE_WALL:
         case FENCE_SANDSTONE_TALL_STONE_WALL:
         case FENCE_RENDERED_TALL_STONE_WALL:
         case FENCE_POTTERY_TALL_STONE_WALL:
         case FENCE_MARBLE_TALL_STONE_WALL:
         case FENCE_SLATE_PORTCULLIS:
         case FENCE_ROUNDED_STONE_PORTCULLIS:
         case FENCE_SANDSTONE_PORTCULLIS:
         case FENCE_RENDERED_PORTCULLIS:
         case FENCE_POTTERY_PORTCULLIS:
         case FENCE_MARBLE_PORTCULLIS:
         case FENCE_PLAN_SLATE_TALL_STONE_WALL:
         case FENCE_PLAN_ROUNDED_STONE_TALL_STONE_WALL:
         case FENCE_PLAN_SANDSTONE_TALL_STONE_WALL:
         case FENCE_PLAN_RENDERED_TALL_STONE_WALL:
         case FENCE_PLAN_POTTERY_TALL_STONE_WALL:
         case FENCE_PLAN_MARBLE_TALL_STONE_WALL:
         case FENCE_PLAN_SLATE_PORTCULLIS:
         case FENCE_PLAN_ROUNDED_STONE_PORTCULLIS:
         case FENCE_PLAN_SANDSTONE_PORTCULLIS:
         case FENCE_PLAN_RENDERED_PORTCULLIS:
         case FENCE_PLAN_POTTERY_PORTCULLIS:
         case FENCE_PLAN_MARBLE_PORTCULLIS:
         case FENCE_PLAN_STONEWALL:
         case FENCE_PLAN_STONE:
         case FENCE_PLAN_SLATE:
         case FENCE_SLATE:
         case FENCE_PLAN_ROUNDED_STONE:
         case FENCE_ROUNDED_STONE:
         case FENCE_PLAN_POTTERY:
         case FENCE_POTTERY:
         case FENCE_PLAN_SANDSTONE:
         case FENCE_SANDSTONE:
         case FENCE_PLAN_MARBLE:
         case FENCE_MARBLE:
         case FENCE_PLAN_WOODEN_PARAPET:
         case FENCE_PLAN_STONE_PARAPET:
         case FENCE_PLAN_STONE_IRON_PARAPET:
         case FENCE_PLAN_SLATE_STONE_PARAPET:
         case FENCE_PLAN_ROUNDED_STONE_STONE_PARAPET:
         case FENCE_PLAN_RENDERED_STONE_PARAPET:
         case FENCE_PLAN_POTTERY_STONE_PARAPET:
         case FENCE_PLAN_MARBLE_STONE_PARAPET:
         case FENCE_PLAN_CURB:
         case FENCE_PLAN_ROPE_LOW:
         case FENCE_PLAN_SANDSTONE_STONE_PARAPET:
         case FENCE_PORTCULLIS:
         case FENCE_RENDERED:
         case FENCE_PLAN_WOODEN:
         case FENCE_PLAN_WOODEN_GATE:
         case FENCE_PLAN_WOODEN_CRUDE:
         case FENCE_PLAN_WOODEN_GATE_CRUDE:
         case FENCE_PLAN_GARDESGARD_GATE:
         case FENCE_GARDESGARD_HIGH:
         case FENCE_PLAN_GARDESGARD_HIGH:
         case FENCE_PLAN_GARDESGARD_LOW:
         case FENCE_PLAN_WOVEN:
         case FENCE_SIEGEWALL:
         case FENCE_RENDERED_IRON:
         case FENCE_PLAN_RENDERED:
         case FENCE_PLAN_RENDERED_IRON:
         case FENCE_PLAN_RENDERED_IRON_GATE:
         default:
            return false;
      }
   }

   public boolean isIron() {
      return isIron(this.type);
   }

   public static final int getSkillNumberNeededForFence(StructureConstantsEnum fenceType) {
      return isWood(fenceType) ? 1005 : 1013;
   }

   public static final Skill getSkillNeededForFence(Creature performer, Fence fence) {
      Skill toReturn = null;
      Skills skills = performer.getSkills();
      if (skills == null) {
         return null;
      } else {
         if (fence.isWood()) {
            try {
               toReturn = performer.getSkills().getSkill(1005);
            } catch (NoSuchSkillException var6) {
               toReturn = skills.learn(1005, 1.0F);
            }
         } else {
            try {
               toReturn = performer.getSkills().getSkill(1013);
            } catch (NoSuchSkillException var5) {
               toReturn = skills.learn(1013, 1.0F);
            }
         }

         return toReturn;
      }
   }

   public static final Fence[] getRubbleFences() {
      return rubbleFences.values().toArray(new Fence[rubbleFences.size()]);
   }

   public final void destroy() {
      try {
         if (this.type == StructureConstantsEnum.FENCE_RUBBLE) {
            rubbleFences.remove(this.getId());
         }

         Zone zone = Zones.getZone(this.getZoneId());
         zone.removeFence(this);
         this.delete();
      } catch (NoSuchZoneException var2) {
         logger.log(Level.WARNING, "Fence in nonexistant zone? Fence: " + this.number + " - " + var2.getMessage(), (Throwable)var2);
      }
   }

   public final float getDamageModifierForItem(Item item, boolean useForFence) {
      float mod = 0.0F;
      if (this.type != StructureConstantsEnum.FENCE_PALISADE_GATE
         && this.type != StructureConstantsEnum.FENCE_PLAN_PALISADE_GATE
         && this.type != StructureConstantsEnum.FENCE_PALISADE
         && this.type != StructureConstantsEnum.FENCE_PLAN_PALISADE) {
         if (this.type != StructureConstantsEnum.FENCE_WOODEN_GATE
            && this.type != StructureConstantsEnum.FENCE_PLAN_WOODEN_GATE
            && this.type != StructureConstantsEnum.FENCE_WOODEN_CRUDE_GATE
            && this.type != StructureConstantsEnum.FENCE_PLAN_WOODEN_GATE_CRUDE
            && this.type != StructureConstantsEnum.FENCE_GARDESGARD_GATE
            && this.type != StructureConstantsEnum.FENCE_PLAN_GARDESGARD_GATE) {
            if (this.isMagic()) {
               if (item.getTemplateId() == 20) {
                  mod = 0.07F;
               } else if (item.isWeaponCrush()) {
                  mod = 0.03F;
               } else if (item.isWeaponAxe()) {
                  mod = 0.015F;
               } else if (item.isWeaponSlash()) {
                  mod = 0.01F;
               } else if (item.isWeaponPierce()) {
                  mod = 0.01F;
               } else if (item.isWeaponMisc()) {
                  mod = 0.01F;
               }
            } else if (this.isHedge() || this.isFlowerbed()) {
               if (item.getTemplateId() == 7) {
                  mod = 0.04F;
               } else if (item.isWeaponCrush()) {
                  mod = 0.01F;
               } else if (item.isWeaponAxe()) {
                  mod = 0.04F;
               } else if (item.isWeaponSlash()) {
                  mod = 0.02F;
               } else if (item.isWeaponPierce()) {
                  mod = 0.01F;
               } else if (item.isWeaponMisc()) {
                  mod = 0.01F;
               }

               if (mod == 0.0F) {
                  return mod;
               }

               if (this.isLowHedge()) {
                  mod = (float)((double)mod + 0.01);
               } else if (this.isMediumHedge()) {
                  mod = (float)((double)mod + 0.005);
               } else if (this.isFlowerbed()) {
                  mod = (float)((double)mod + 0.01);
               }
            } else if (this.isStone()) {
               if (item.getTemplateId() == 20) {
                  mod = 0.02F;
               } else if (item.getTemplateId() == 493) {
                  mod = 0.01F;
               } else if (item.isWeaponCrush()) {
                  mod = 0.01F;
               } else if (item.isWeaponAxe()) {
                  mod = 0.005F;
               } else if (item.isWeaponSlash()) {
                  mod = 0.005F;
               } else if (item.isWeaponPierce()) {
                  mod = 0.002F;
               } else if (item.isWeaponMisc()) {
                  mod = 0.001F;
               }

               if (mod == 0.0F) {
                  return mod;
               }

               if (useForFence && (this.type == StructureConstantsEnum.FENCE_CURB || this.type == StructureConstantsEnum.FENCE_PLAN_CURB)) {
                  mod += 0.01F;
               }
            } else if (this.isIron()) {
               if (item.isWeaponCrush()) {
                  mod = 0.02F;
               } else if (item.isWeaponAxe()) {
                  mod = 0.01F;
               } else if (item.isWeaponSlash()) {
                  mod = 0.005F;
               } else if (item.isWeaponPierce()) {
                  mod = 0.002F;
               } else if (item.isWeaponMisc()) {
                  mod = 0.001F;
               }
            } else if (this.isWoven()) {
               if (item.isWeaponCrush()) {
                  mod = 0.01F;
               } else if (item.isWeaponAxe()) {
                  mod = 0.03F;
               } else if (item.isWeaponSlash()) {
                  mod = 0.02F;
               } else if (item.isWeaponPierce()) {
                  mod = 0.01F;
               } else if (item.isWeaponMisc()) {
                  mod = 0.007F;
               }
            } else if (this.type != StructureConstantsEnum.FENCE_ROPE_HIGH
               && this.type != StructureConstantsEnum.FENCE_PLAN_ROPE_HIGH
               && this.type != StructureConstantsEnum.FENCE_ROPE_LOW
               && this.type != StructureConstantsEnum.FENCE_PLAN_ROPE_LOW) {
               if (item.isWeaponAxe()) {
                  mod = 0.03F;
               } else if (item.isWeaponCrush()) {
                  mod = 0.02F;
               } else if (item.isWeaponSlash()) {
                  mod = 0.015F;
               } else if (item.isWeaponPierce()) {
                  mod = 0.01F;
               } else if (item.isWeaponMisc()) {
                  mod = 0.007F;
               }
            } else if (item.isWeaponCrush()) {
               mod = 0.01F;
            } else if (item.isWeaponAxe()) {
               mod = 0.03F;
            } else if (item.isWeaponSlash()) {
               mod = 0.02F;
            } else if (item.isWeaponPierce()) {
               mod = 0.01F;
            } else if (item.isWeaponMisc()) {
               mod = 0.007F;
            }
         } else if (item.isWeaponAxe()) {
            mod = 0.03F;
         } else if (item.isWeaponCrush()) {
            mod = 0.02F;
         } else if (item.isWeaponSlash()) {
            mod = 0.02F;
         } else if (item.isWeaponPierce()) {
            mod = 0.01F;
         } else if (item.isWeaponMisc()) {
            mod = 0.007F;
         }
      } else if (item.isWeaponAxe()) {
         mod = 0.02F;
      } else if (item.isWeaponCrush()) {
         mod = 0.007F;
      } else if (item.isWeaponSlash()) {
         mod = 0.01F;
      } else if (item.isWeaponPierce()) {
         mod = 0.005F;
      } else if (item.isWeaponMisc()) {
         mod = 0.002F;
      }

      return mod;
   }

   public final int getRepairItemTemplate() {
      int templateId = 22;
      if (this.type == StructureConstantsEnum.FENCE_PLAN_STONEWALL
         || this.type == StructureConstantsEnum.FENCE_STONEWALL
         || this.type == StructureConstantsEnum.FENCE_CURB) {
         templateId = 146;
      } else if (this.isMagic()) {
         templateId = 765;
      } else if (this.isSlate()) {
         templateId = 1123;
      } else if (this.isRoundedStone()) {
         templateId = 1122;
      } else if (this.isPottery()) {
         templateId = 776;
      } else if (this.isSandstone()) {
         templateId = 1121;
      } else if (this.isPlastered()) {
         templateId = 130;
      } else if (this.isMarble()) {
         templateId = 786;
      } else if (this.isStone() || this.isIron()) {
         templateId = 132;
      } else if (this.isWoven()) {
         templateId = 169;
      } else if (this.type == StructureConstantsEnum.FENCE_PALISADE
         || this.type == StructureConstantsEnum.FENCE_PALISADE_GATE
         || this.type == StructureConstantsEnum.FENCE_PLAN_PALISADE
         || this.type == StructureConstantsEnum.FENCE_PLAN_PALISADE_GATE) {
         templateId = 9;
      } else if (this.isHedge() || this.isFlowerbed()) {
         templateId = -1;
      }

      return templateId;
   }

   public final int getCover() {
      if (this.isFinished()) {
         if (this.type == StructureConstantsEnum.FENCE_STONEWALL) {
            return 30;
         } else if (this.type == StructureConstantsEnum.FENCE_STONE_PARAPET) {
            return 50;
         } else if (this.type == StructureConstantsEnum.FENCE_STONE_IRON_PARAPET) {
            return 50;
         } else if (this.isStone() || this.type == StructureConstantsEnum.FENCE_PALISADE || this.type == StructureConstantsEnum.FENCE_PALISADE_GATE) {
            return 100;
         } else if (this.type == StructureConstantsEnum.FENCE_WOODEN_PARAPET) {
            return 50;
         } else if (this.type == StructureConstantsEnum.FENCE_WOVEN) {
            return 0;
         } else if (this.isLowHedge()) {
            return 0;
         } else if (this.isMediumHedge()) {
            return 30;
         } else if (this.isHighHedge()) {
            return 100;
         } else {
            return this.isMagic() ? 100 : 20;
         }
      } else {
         return Math.max(0, this.getState().state);
      }
   }

   @Override
   public final float getBlockPercent(Creature creature) {
      if (this.isFinished()) {
         switch(this.type) {
            case HEDGE_FLOWER1_LOW:
            case HEDGE_FLOWER2_LOW:
            case HEDGE_FLOWER3_LOW:
            case HEDGE_FLOWER4_LOW:
            case HEDGE_FLOWER5_LOW:
            case HEDGE_FLOWER6_LOW:
            case HEDGE_FLOWER7_LOW:
            case FENCE_GARDESGARD_LOW:
            case FENCE_IRON:
            case FENCE_STONEWALL:
            case FENCE_ROPE_HIGH:
            case FENCE_GARDESGARD_GATE:
            case FENCE_IRON_GATE:
            case FENCE_STONE:
            case HEDGE_FLOWER1_MEDIUM:
            case HEDGE_FLOWER2_MEDIUM:
            case HEDGE_FLOWER3_MEDIUM:
            case HEDGE_FLOWER4_MEDIUM:
            case HEDGE_FLOWER5_MEDIUM:
            case HEDGE_FLOWER6_MEDIUM:
            case HEDGE_FLOWER7_MEDIUM:
            case FENCE_SLATE:
            case FENCE_ROUNDED_STONE:
            case FENCE_POTTERY:
            case FENCE_SANDSTONE:
            case FENCE_MARBLE:
            case FENCE_RENDERED:
            case FENCE_SLATE_IRON:
            case FENCE_ROUNDED_STONE_IRON:
            case FENCE_POTTERY_IRON:
            case FENCE_SANDSTONE_IRON:
            case FENCE_RENDERED_IRON:
            case FENCE_MARBLE_IRON:
            case FENCE_SLATE_IRON_GATE:
            case FENCE_ROUNDED_STONE_IRON_GATE:
            case FENCE_POTTERY_IRON_GATE:
            case FENCE_SANDSTONE_IRON_GATE:
            case FENCE_RENDERED_IRON_GATE:
            case FENCE_MARBLE_IRON_GATE:
               return 30.0F;
            case FENCE_CURB:
            case FENCE_WOVEN:
            case FENCE_ROPE_LOW:
            case FENCE_RUBBLE:
            case FLOWERBED_BLUE:
            case FLOWERBED_GREENISH_YELLOW:
            case FLOWERBED_ORANGE_RED:
            case FLOWERBED_PURPLE:
            case FLOWERBED_WHITE:
            case FLOWERBED_WHITE_DOTTED:
            case FLOWERBED_YELLOW:
            case FENCE_SLATE_CHAIN_FENCE:
            case FENCE_ROUNDED_STONE_CHAIN_FENCE:
            case FENCE_SANDSTONE_CHAIN_FENCE:
            case FENCE_RENDERED_CHAIN_FENCE:
            case FENCE_POTTERY_CHAIN_FENCE:
            case FENCE_MARBLE_CHAIN_FENCE:
            case FENCE_MEDIUM_CHAIN:
               return 0.0F;
            case FENCE_WOODEN:
            case FENCE_PLAN_ROPE_HIGH:
            case FENCE_WOODEN_GATE:
            case FENCE_WOODEN_CRUDE_GATE:
            case FENCE_WOODEN_CRUDE:
            case FENCE_PLAN_PALISADE:
            case FENCE_PLAN_PALISADE_GATE:
            case FENCE_PLAN_STONEWALL_HIGH:
            case FENCE_PLAN_PORTCULLIS:
            case FENCE_PLAN_SLATE_TALL_STONE_WALL:
            case FENCE_PLAN_ROUNDED_STONE_TALL_STONE_WALL:
            case FENCE_PLAN_SANDSTONE_TALL_STONE_WALL:
            case FENCE_PLAN_RENDERED_TALL_STONE_WALL:
            case FENCE_PLAN_POTTERY_TALL_STONE_WALL:
            case FENCE_PLAN_MARBLE_TALL_STONE_WALL:
            case FENCE_PLAN_SLATE_PORTCULLIS:
            case FENCE_PLAN_ROUNDED_STONE_PORTCULLIS:
            case FENCE_PLAN_SANDSTONE_PORTCULLIS:
            case FENCE_PLAN_RENDERED_PORTCULLIS:
            case FENCE_PLAN_POTTERY_PORTCULLIS:
            case FENCE_PLAN_MARBLE_PORTCULLIS:
            case FENCE_PLAN_STONEWALL:
            case FENCE_PLAN_STONE:
            case FENCE_PLAN_SLATE:
            case FENCE_PLAN_ROUNDED_STONE:
            case FENCE_PLAN_POTTERY:
            case FENCE_PLAN_SANDSTONE:
            case FENCE_PLAN_MARBLE:
            case FENCE_PLAN_WOODEN_PARAPET:
            case FENCE_PLAN_STONE_PARAPET:
            case FENCE_PLAN_STONE_IRON_PARAPET:
            case FENCE_PLAN_SLATE_STONE_PARAPET:
            case FENCE_PLAN_ROUNDED_STONE_STONE_PARAPET:
            case FENCE_PLAN_RENDERED_STONE_PARAPET:
            case FENCE_PLAN_POTTERY_STONE_PARAPET:
            case FENCE_PLAN_MARBLE_STONE_PARAPET:
            case FENCE_PLAN_MEDIUM_CHAIN:
            case FENCE_PLAN_SLATE_CHAIN_FENCE:
            case FENCE_PLAN_ROUNDED_STONE_CHAIN_FENCE:
            case FENCE_PLAN_SANDSTONE_CHAIN_FENCE:
            case FENCE_PLAN_RENDERED_CHAIN_FENCE:
            case FENCE_PLAN_POTTERY_CHAIN_FENCE:
            case FENCE_PLAN_MARBLE_CHAIN_FENCE:
            case FENCE_PLAN_CURB:
            case FENCE_PLAN_ROPE_LOW:
            case FENCE_PLAN_IRON_HIGH:
            case FENCE_PLAN_IRON_GATE_HIGH:
            case FENCE_PLAN_SLATE_HIGH_IRON_FENCE_GATE:
            case FENCE_PLAN_ROUNDED_STONE_HIGH_IRON_FENCE_GATE:
            case FENCE_PLAN_SANDSTONE_HIGH_IRON_FENCE_GATE:
            case FENCE_PLAN_RENDERED_HIGH_IRON_FENCE_GATE:
            case FENCE_PLAN_POTTERY_HIGH_IRON_FENCE_GATE:
            case FENCE_PLAN_MARBLE_HIGH_IRON_FENCE_GATE:
            case FENCE_PLAN_SLATE_HIGH_IRON_FENCE:
            case FENCE_PLAN_ROUNDED_STONE_HIGH_IRON_FENCE:
            case FENCE_PLAN_SANDSTONE_HIGH_IRON_FENCE:
            case FENCE_PLAN_RENDERED_HIGH_IRON_FENCE:
            case FENCE_PLAN_POTTERY_HIGH_IRON_FENCE:
            case FENCE_PLAN_MARBLE_HIGH_IRON_FENCE:
            case FENCE_PLAN_SANDSTONE_STONE_PARAPET:
            case FENCE_PLAN_WOODEN:
            case FENCE_PLAN_WOODEN_GATE:
            case FENCE_PLAN_WOODEN_CRUDE:
            case FENCE_PLAN_WOODEN_GATE_CRUDE:
            case FENCE_PLAN_GARDESGARD_GATE:
            case FENCE_PLAN_GARDESGARD_HIGH:
            case FENCE_PLAN_GARDESGARD_LOW:
            case FENCE_PLAN_WOVEN:
            case FENCE_SIEGEWALL:
            default:
               return 20.0F;
            case FENCE_STONE_PARAPET:
            case FENCE_WOODEN_PARAPET:
            case FENCE_STONE_IRON_PARAPET:
            case FENCE_SANDSTONE_STONE_PARAPET:
            case FENCE_SLATE_STONE_PARAPET:
            case FENCE_ROUNDED_STONE_STONE_PARAPET:
            case FENCE_RENDERED_STONE_PARAPET:
            case FENCE_POTTERY_STONE_PARAPET:
            case FENCE_MARBLE_STONE_PARAPET:
            case FENCE_SLATE_PORTCULLIS:
            case FENCE_ROUNDED_STONE_PORTCULLIS:
            case FENCE_SANDSTONE_PORTCULLIS:
            case FENCE_RENDERED_PORTCULLIS:
            case FENCE_POTTERY_PORTCULLIS:
            case FENCE_MARBLE_PORTCULLIS:
            case FENCE_SLATE_HIGH_IRON_FENCE_GATE:
            case FENCE_ROUNDED_STONE_HIGH_IRON_FENCE_GATE:
            case FENCE_SANDSTONE_HIGH_IRON_FENCE_GATE:
            case FENCE_RENDERED_HIGH_IRON_FENCE_GATE:
            case FENCE_POTTERY_HIGH_IRON_FENCE_GATE:
            case FENCE_MARBLE_HIGH_IRON_FENCE_GATE:
            case FENCE_SLATE_HIGH_IRON_FENCE:
            case FENCE_ROUNDED_STONE_HIGH_IRON_FENCE:
            case FENCE_SANDSTONE_HIGH_IRON_FENCE:
            case FENCE_RENDERED_HIGH_IRON_FENCE:
            case FENCE_POTTERY_HIGH_IRON_FENCE:
            case FENCE_MARBLE_HIGH_IRON_FENCE:
            case FENCE_PORTCULLIS:
            case FENCE_IRON_HIGH:
            case FENCE_IRON_GATE_HIGH:
               return 75.0F;
            case HEDGE_FLOWER1_HIGH:
            case HEDGE_FLOWER2_HIGH:
            case HEDGE_FLOWER3_HIGH:
            case HEDGE_FLOWER4_HIGH:
            case HEDGE_FLOWER5_HIGH:
            case HEDGE_FLOWER6_HIGH:
            case HEDGE_FLOWER7_HIGH:
            case FENCE_MAGIC_STONE:
            case FENCE_MAGIC_ICE:
            case FENCE_MAGIC_FIRE:
            case FENCE_PALISADE:
            case FENCE_PALISADE_GATE:
            case FENCE_STONEWALL_HIGH:
            case FENCE_SLATE_TALL_STONE_WALL:
            case FENCE_ROUNDED_STONE_TALL_STONE_WALL:
            case FENCE_SANDSTONE_TALL_STONE_WALL:
            case FENCE_RENDERED_TALL_STONE_WALL:
            case FENCE_POTTERY_TALL_STONE_WALL:
            case FENCE_MARBLE_TALL_STONE_WALL:
               return 100.0F;
            case FENCE_GARDESGARD_HIGH:
               return 40.0F;
         }
      } else {
         return (float)Math.max(0, this.getState().state);
      }
   }

   public final boolean isItemRepair(Item item) {
      return item.getTemplateId() == this.getRepairItemTemplate();
   }

   public final float getCurrentQualityLevel() {
      return this.currentQL * Math.max(1.0F, 100.0F - this.damage) / 100.0F;
   }

   public static final Fence getFence(long fenceId) {
      int x = Tiles.decodeTileX(fenceId);
      int y = Tiles.decodeTileY(fenceId);
      int layer = Tiles.decodeLayer(fenceId);
      VolaTile tile = Zones.getTileOrNull(x, y, layer == 0);
      if (tile != null) {
         for(Fence f : tile.getFences()) {
            if (f != null && f.getId() == fenceId) {
               return f;
            }
         }
      }

      return null;
   }

   public abstract void setZoneId(int var1);

   public abstract void save() throws IOException;

   abstract void load() throws IOException;

   public abstract void improveOrigQualityLevel(float var1);

   abstract boolean changeColor(int var1);

   public abstract void delete();

   public abstract void setLastUsed(long var1);

   @Override
   public final String toString() {
      return "Fence [Tile: "
         + this.tilex
         + ", "
         + this.tiley
         + ", dir: "
         + this.dir
         + ", surfaced: "
         + this.surfaced
         + ", QL: "
         + this.currentQL
         + ", DMG: "
         + this.damage
         + ", type: "
         + this.type
         + ", state: "
         + this.state
         + ']';
   }

   public final int getHeightOffset() {
      return this.heightOffset;
   }

   public final boolean isOnFloorLevel(int level) {
      return level == this.floorLevel;
   }

   @Override
   public final int getFloorLevel() {
      return this.floorLevel;
   }

   private final void setFloorLevel() {
      this.floorLevel = this.heightOffset / 30;
   }

   public final byte getLayer() {
      return (byte)this.layer;
   }

   @Override
   public boolean isOnSurface() {
      return this.layer == 0;
   }

   @Override
   public final boolean isWithinFloorLevels(int maxFloorLevel, int minFloorLevel) {
      return this.floorLevel <= maxFloorLevel && this.floorLevel >= minFloorLevel;
   }

   @Override
   public final int getStartX() {
      return this.getTileX();
   }

   @Override
   public final int getStartY() {
      return this.getTileY();
   }

   @Override
   public final int getMinX() {
      return this.getTileX();
   }

   @Override
   public final int getMinY() {
      return this.getTileY();
   }

   @Override
   public final int getEndX() {
      return this.isHorizontal() ? this.getStartX() + 1 : this.getStartX();
   }

   @Override
   public final int getEndY() {
      return this.isHorizontal() ? this.getStartY() : this.getStartY() + 1;
   }

   @Override
   public final boolean supports() {
      return !this.isHedge() && !this.isLowFence() && !this.isMagic() && !this.isFlowerbed();
   }

   @Override
   public float getFloorZ() {
      return (float)(this.heightOffset / 10);
   }

   @Override
   public float getMinZ() {
      return Math.min(
            Zones.getHeightForNode(this.getStartX(), this.getStartY(), this.getLayer()),
            Zones.getHeightForNode(this.getEndX(), this.getEndY(), this.getLayer())
         )
         + this.getFloorZ();
   }

   @Override
   public float getMaxZ() {
      return Math.max(
            Zones.getHeightForNode(this.getStartX(), this.getStartY(), this.getLayer()),
            Zones.getHeightForNode(this.getEndX(), this.getEndY(), this.getLayer())
         )
         + this.getFloorZ()
         + 3.0F;
   }

   @Override
   public boolean isWithinZ(float maxZ, float minZ, boolean followGround) {
      return this.getFloorLevel() == 0 && followGround || minZ <= this.getMaxZ() && maxZ >= this.getMinZ();
   }

   @Override
   public final boolean supports(StructureSupport support) {
      if (!this.supports()) {
         return false;
      } else {
         if (support.isFloor()) {
            if (this.getFloorLevel() == support.getFloorLevel() || this.getFloorLevel() == support.getFloorLevel() - 1) {
               if (this.isHorizontal()) {
                  if (this.getStartX() == support.getStartX() && (this.getStartY() == support.getStartY() || this.getStartY() == support.getEndY())) {
                     return true;
                  }
               } else if (this.getStartY() == support.getStartY() && (this.getStartX() == support.getStartX() || this.getStartX() == support.getEndX())) {
                  return true;
               }
            }
         } else {
            int levelMod = support.supports() ? -1 : 0;
            if (support.getFloorLevel() >= this.getFloorLevel() + levelMod
               && support.getFloorLevel() <= this.getFloorLevel() + 1
               && support.getMinX() == this.getMinX()
               && support.getMinY() == this.getMinY()
               && this.isHorizontal() == support.isHorizontal()) {
               return true;
            }
         }

         return false;
      }
   }

   @Override
   public boolean isSupportedByGround() {
      return this.getFloorLevel() == 0;
   }

   @Override
   public long getTempId() {
      return -10L;
   }

   public String getTypeName() {
      switch(this.type) {
         case FENCE_CURB:
         case FENCE_PLAN_CURB:
            return "curb";
         case FENCE_WOVEN:
         case FENCE_PLAN_WOVEN:
            return "woven fence";
         case FENCE_ROPE_LOW:
         case FENCE_PLAN_ROPE_LOW:
            return "low rope fence";
         case FENCE_RUBBLE:
            return "rubble";
         case FLOWERBED_BLUE:
            return "blue flowerbed";
         case FLOWERBED_GREENISH_YELLOW:
            return "greenish-yellow flowerbed";
         case FLOWERBED_ORANGE_RED:
            return "orange-red flowerbed";
         case FLOWERBED_PURPLE:
            return "purple flowerbed";
         case FLOWERBED_WHITE:
            return "white flowerbed";
         case FLOWERBED_WHITE_DOTTED:
            return "white-dotted flowerbed";
         case FLOWERBED_YELLOW:
            return "yellow flowerbed";
         case FENCE_GARDESGARD_LOW:
         case FENCE_PLAN_GARDESGARD_LOW:
            return "low roundpole fence";
         case FENCE_IRON:
         case FENCE_PLAN_IRON:
            return "iron fence";
         case FENCE_STONEWALL:
         case FENCE_PLAN_STONEWALL:
            return "low stone wall";
         case FENCE_WOODEN:
         case FENCE_PLAN_WOODEN:
            return "wooden fence";
         case FENCE_STONE_PARAPET:
         case FENCE_PLAN_STONE_PARAPET:
            return "stone parapet";
         case FENCE_WOODEN_PARAPET:
         case FENCE_PLAN_WOODEN_PARAPET:
            return "wooden parapet";
         case FENCE_STONE_IRON_PARAPET:
         case HEDGE_FLOWER1_MEDIUM:
         case HEDGE_FLOWER2_MEDIUM:
         case HEDGE_FLOWER3_MEDIUM:
         case HEDGE_FLOWER4_MEDIUM:
         case HEDGE_FLOWER5_MEDIUM:
         case HEDGE_FLOWER6_MEDIUM:
         case HEDGE_FLOWER7_MEDIUM:
         case HEDGE_FLOWER1_HIGH:
         case HEDGE_FLOWER2_HIGH:
         case HEDGE_FLOWER3_HIGH:
         case HEDGE_FLOWER4_HIGH:
         case HEDGE_FLOWER5_HIGH:
         case HEDGE_FLOWER6_HIGH:
         case HEDGE_FLOWER7_HIGH:
         case FENCE_MAGIC_STONE:
         case FENCE_MAGIC_ICE:
         case FENCE_MAGIC_FIRE:
         case FENCE_PLAN_STONE_IRON_PARAPET:
         default:
            if (this.isHedge()) {
               return "hedge";
            }

            return "fence";
         case FENCE_ROPE_HIGH:
         case FENCE_PLAN_ROPE_HIGH:
            return "high rope fence";
         case FENCE_GARDESGARD_GATE:
         case FENCE_PLAN_GARDESGARD_GATE:
            return "roundpole fence gate";
         case FENCE_WOODEN_GATE:
         case FENCE_PLAN_WOODEN_GATE:
            return "wooden fence gate";
         case FENCE_WOODEN_CRUDE_GATE:
         case FENCE_PLAN_WOODEN_GATE_CRUDE:
            return "crude wooden fence gate";
         case FENCE_WOODEN_CRUDE:
         case FENCE_PLAN_WOODEN_CRUDE:
            return "crude wooden fence";
         case FENCE_IRON_GATE:
         case FENCE_PLAN_IRON_GATE:
            return "iron fence gate";
         case FENCE_STONE:
         case FENCE_PLAN_STONE:
            return "stone fence";
         case FENCE_SANDSTONE_STONE_PARAPET:
         case FENCE_PLAN_SANDSTONE_STONE_PARAPET:
            return "sandstone parapet";
         case FENCE_SLATE_STONE_PARAPET:
         case FENCE_PLAN_SLATE_STONE_PARAPET:
            return "slate parapet";
         case FENCE_ROUNDED_STONE_STONE_PARAPET:
         case FENCE_PLAN_ROUNDED_STONE_STONE_PARAPET:
            return "rounded stone parapet";
         case FENCE_RENDERED_STONE_PARAPET:
         case FENCE_PLAN_RENDERED_STONE_PARAPET:
            return "rendered parapet";
         case FENCE_POTTERY_STONE_PARAPET:
         case FENCE_PLAN_POTTERY_STONE_PARAPET:
            return "pottery parapet";
         case FENCE_MARBLE_STONE_PARAPET:
         case FENCE_PLAN_MARBLE_STONE_PARAPET:
            return "marble parapet";
         case FENCE_SLATE_CHAIN_FENCE:
         case FENCE_PLAN_SLATE_CHAIN_FENCE:
            return "slate chain fence";
         case FENCE_ROUNDED_STONE_CHAIN_FENCE:
         case FENCE_PLAN_ROUNDED_STONE_CHAIN_FENCE:
            return "rounded stone chain fence";
         case FENCE_SANDSTONE_CHAIN_FENCE:
         case FENCE_PLAN_SANDSTONE_CHAIN_FENCE:
            return "sandstone chain fence";
         case FENCE_RENDERED_CHAIN_FENCE:
         case FENCE_PLAN_RENDERED_CHAIN_FENCE:
            return "rendered chain fence";
         case FENCE_POTTERY_CHAIN_FENCE:
         case FENCE_PLAN_POTTERY_CHAIN_FENCE:
            return "pottery chain fence";
         case FENCE_MARBLE_CHAIN_FENCE:
         case FENCE_PLAN_MARBLE_CHAIN_FENCE:
            return "marble chain fence";
         case FENCE_PLAN_PALISADE:
         case FENCE_PALISADE:
            return "wooden palisade";
         case FENCE_PLAN_PALISADE_GATE:
         case FENCE_PALISADE_GATE:
            return "wooden palisade gate";
         case FENCE_PLAN_STONEWALL_HIGH:
         case FENCE_STONEWALL_HIGH:
            return "tall stone wall";
         case FENCE_PLAN_PORTCULLIS:
         case FENCE_PORTCULLIS:
            return "portcullis";
         case FENCE_SLATE_TALL_STONE_WALL:
         case FENCE_PLAN_SLATE_TALL_STONE_WALL:
            return "tall slate wall";
         case FENCE_ROUNDED_STONE_TALL_STONE_WALL:
         case FENCE_PLAN_ROUNDED_STONE_TALL_STONE_WALL:
            return "tall rounded stone wall";
         case FENCE_SANDSTONE_TALL_STONE_WALL:
         case FENCE_PLAN_SANDSTONE_TALL_STONE_WALL:
            return "tall sandstone wall";
         case FENCE_RENDERED_TALL_STONE_WALL:
         case FENCE_PLAN_RENDERED_TALL_STONE_WALL:
            return "tall rendered wall";
         case FENCE_POTTERY_TALL_STONE_WALL:
         case FENCE_PLAN_POTTERY_TALL_STONE_WALL:
            return "tall pottery wall";
         case FENCE_MARBLE_TALL_STONE_WALL:
         case FENCE_PLAN_MARBLE_TALL_STONE_WALL:
            return "tall marble wall";
         case FENCE_SLATE_PORTCULLIS:
         case FENCE_PLAN_SLATE_PORTCULLIS:
            return "slate portcullis";
         case FENCE_ROUNDED_STONE_PORTCULLIS:
         case FENCE_PLAN_ROUNDED_STONE_PORTCULLIS:
            return "rounded stone portcullis";
         case FENCE_SANDSTONE_PORTCULLIS:
         case FENCE_PLAN_SANDSTONE_PORTCULLIS:
            return "sandstone portcullis";
         case FENCE_RENDERED_PORTCULLIS:
         case FENCE_PLAN_RENDERED_PORTCULLIS:
            return "rendered portcullis";
         case FENCE_POTTERY_PORTCULLIS:
         case FENCE_PLAN_POTTERY_PORTCULLIS:
            return "pottery portcullis";
         case FENCE_MARBLE_PORTCULLIS:
         case FENCE_PLAN_MARBLE_PORTCULLIS:
            return "marble portcullis";
         case FENCE_PLAN_SLATE:
         case FENCE_SLATE:
            return "slate fence";
         case FENCE_PLAN_ROUNDED_STONE:
         case FENCE_ROUNDED_STONE:
            return "rounded stone fence";
         case FENCE_PLAN_POTTERY:
         case FENCE_POTTERY:
            return "pottery fence";
         case FENCE_PLAN_SANDSTONE:
         case FENCE_SANDSTONE:
            return "sandstone fence";
         case FENCE_PLAN_MARBLE:
         case FENCE_MARBLE:
            return "marble fence";
         case FENCE_PLAN_MEDIUM_CHAIN:
         case FENCE_MEDIUM_CHAIN:
            return "chain fence";
         case FENCE_PLAN_IRON_HIGH:
         case FENCE_IRON_HIGH:
            return "high iron fence";
         case FENCE_PLAN_IRON_GATE_HIGH:
         case FENCE_IRON_GATE_HIGH:
            return "high iron fence gate";
         case FENCE_SLATE_HIGH_IRON_FENCE_GATE:
         case FENCE_PLAN_SLATE_HIGH_IRON_FENCE_GATE:
            return "slate high iron fence gate";
         case FENCE_ROUNDED_STONE_HIGH_IRON_FENCE_GATE:
         case FENCE_PLAN_ROUNDED_STONE_HIGH_IRON_FENCE_GATE:
            return "rounded stone high iron fence gate";
         case FENCE_SANDSTONE_HIGH_IRON_FENCE_GATE:
         case FENCE_PLAN_SANDSTONE_HIGH_IRON_FENCE_GATE:
            return "sandstone high iron fence gate";
         case FENCE_RENDERED_HIGH_IRON_FENCE_GATE:
         case FENCE_PLAN_RENDERED_HIGH_IRON_FENCE_GATE:
            return "rendered high iron fence gate";
         case FENCE_POTTERY_HIGH_IRON_FENCE_GATE:
         case FENCE_PLAN_POTTERY_HIGH_IRON_FENCE_GATE:
            return "pottery high iron fence gate";
         case FENCE_MARBLE_HIGH_IRON_FENCE_GATE:
         case FENCE_PLAN_MARBLE_HIGH_IRON_FENCE_GATE:
            return "marble high iron fence gate";
         case FENCE_SLATE_HIGH_IRON_FENCE:
         case FENCE_PLAN_SLATE_HIGH_IRON_FENCE:
            return "slate high iron fence";
         case FENCE_ROUNDED_STONE_HIGH_IRON_FENCE:
         case FENCE_PLAN_ROUNDED_STONE_HIGH_IRON_FENCE:
            return "rounded stone high iron fence";
         case FENCE_SANDSTONE_HIGH_IRON_FENCE:
         case FENCE_PLAN_SANDSTONE_HIGH_IRON_FENCE:
            return "sandstone high iron fence";
         case FENCE_RENDERED_HIGH_IRON_FENCE:
         case FENCE_PLAN_RENDERED_HIGH_IRON_FENCE:
            return "rendered high iron fence";
         case FENCE_POTTERY_HIGH_IRON_FENCE:
         case FENCE_PLAN_POTTERY_HIGH_IRON_FENCE:
            return "pottery high iron fence";
         case FENCE_MARBLE_HIGH_IRON_FENCE:
         case FENCE_PLAN_MARBLE_HIGH_IRON_FENCE:
            return "marble high iron fence";
         case FENCE_RENDERED:
         case FENCE_PLAN_RENDERED:
            return "Rendered fence";
         case FENCE_GARDESGARD_HIGH:
         case FENCE_PLAN_GARDESGARD_HIGH:
            return "high roundpole fence";
         case FENCE_SIEGEWALL:
            return "siege wall";
         case FENCE_SLATE_IRON:
         case FENCE_PLAN_SLATE_IRON:
            return "slate iron fence";
         case FENCE_ROUNDED_STONE_IRON:
         case FENCE_PLAN_ROUNDED_STONE_IRON:
            return "rounded stone iron fence";
         case FENCE_POTTERY_IRON:
         case FENCE_PLAN_POTTERY_IRON:
            return "pottery iron fence";
         case FENCE_SANDSTONE_IRON:
         case FENCE_PLAN_SANDSTONE_IRON:
            return "sandstone iron fence";
         case FENCE_RENDERED_IRON:
         case FENCE_PLAN_RENDERED_IRON:
            return "plastered iron fence";
         case FENCE_MARBLE_IRON:
         case FENCE_PLAN_MARBLE_IRON:
            return "marble iron fence";
         case FENCE_SLATE_IRON_GATE:
         case FENCE_PLAN_SLATE_IRON_GATE:
            return "slate iron gate";
         case FENCE_ROUNDED_STONE_IRON_GATE:
         case FENCE_PLAN_ROUNDED_STONE_IRON_GATE:
            return "rounded stone iron gate";
         case FENCE_POTTERY_IRON_GATE:
         case FENCE_PLAN_POTTERY_IRON_GATE:
            return "pottery iron gate";
         case FENCE_SANDSTONE_IRON_GATE:
         case FENCE_PLAN_SANDSTONE_IRON_GATE:
            return "sandstone iron gate";
         case FENCE_RENDERED_IRON_GATE:
         case FENCE_PLAN_RENDERED_IRON_GATE:
            return "plastered iron gate";
         case FENCE_MARBLE_IRON_GATE:
         case FENCE_PLAN_MARBLE_IRON_GATE:
            return "marble iron gate";
      }
   }

   public final void setColor(int newcolor) {
      this.changeColor(newcolor);
   }

   Permissions getSettings() {
      return this.permissions;
   }

   public boolean isGate() {
      switch(this.type) {
         case FENCE_GARDESGARD_GATE:
         case FENCE_WOODEN_GATE:
         case FENCE_WOODEN_CRUDE_GATE:
         case FENCE_IRON_GATE:
         case FENCE_PALISADE_GATE:
         case FENCE_SLATE_PORTCULLIS:
         case FENCE_ROUNDED_STONE_PORTCULLIS:
         case FENCE_SANDSTONE_PORTCULLIS:
         case FENCE_RENDERED_PORTCULLIS:
         case FENCE_POTTERY_PORTCULLIS:
         case FENCE_MARBLE_PORTCULLIS:
         case FENCE_SLATE_HIGH_IRON_FENCE_GATE:
         case FENCE_ROUNDED_STONE_HIGH_IRON_FENCE_GATE:
         case FENCE_SANDSTONE_HIGH_IRON_FENCE_GATE:
         case FENCE_RENDERED_HIGH_IRON_FENCE_GATE:
         case FENCE_POTTERY_HIGH_IRON_FENCE_GATE:
         case FENCE_MARBLE_HIGH_IRON_FENCE_GATE:
         case FENCE_PORTCULLIS:
         case FENCE_IRON_GATE_HIGH:
         case FENCE_SLATE_IRON_GATE:
         case FENCE_ROUNDED_STONE_IRON_GATE:
         case FENCE_POTTERY_IRON_GATE:
         case FENCE_SANDSTONE_IRON_GATE:
         case FENCE_RENDERED_IRON_GATE:
         case FENCE_MARBLE_IRON_GATE:
            return true;
         default:
            return false;
      }
   }

   void setSettings(int aSettings) {
      this.permissions.setPermissionBits(aSettings);
   }

   @Override
   public boolean canBeAlwaysLit() {
      return false;
   }

   @Override
   public boolean canBeAutoFilled() {
      return false;
   }

   @Override
   public boolean canBeAutoLit() {
      return false;
   }

   @Override
   public final boolean canBePeggedByPlayer() {
      return false;
   }

   @Override
   public boolean canBePlanted() {
      return false;
   }

   @Override
   public final boolean canBeSealedByPlayer() {
      return false;
   }

   @Override
   public boolean canChangeCreator() {
      return false;
   }

   @Override
   public boolean canDisableDecay() {
      return true;
   }

   @Override
   public boolean canDisableDestroy() {
      return true;
   }

   @Override
   public boolean canDisableDrag() {
      return false;
   }

   @Override
   public boolean canDisableDrop() {
      return false;
   }

   @Override
   public boolean canDisableEatAndDrink() {
      return false;
   }

   @Override
   public boolean canDisableImprove() {
      return false;
   }

   @Override
   public boolean canDisableLocking() {
      return this.isGate();
   }

   @Override
   public boolean canDisableLockpicking() {
      return this.isGate();
   }

   @Override
   public boolean canDisableMoveable() {
      return false;
   }

   @Override
   public final boolean canDisableOwnerMoveing() {
      return false;
   }

   @Override
   public final boolean canDisableOwnerTurning() {
      return false;
   }

   @Override
   public boolean canDisablePainting() {
      return !this.isHedge();
   }

   @Override
   public boolean canDisablePut() {
      return false;
   }

   @Override
   public boolean canDisableRepair() {
      return true;
   }

   @Override
   public boolean canDisableRuneing() {
      return false;
   }

   @Override
   public boolean canDisableSpellTarget() {
      return false;
   }

   @Override
   public boolean canDisableTake() {
      return false;
   }

   @Override
   public boolean canDisableTurning() {
      return false;
   }

   @Override
   public boolean canHaveCourier() {
      return false;
   }

   @Override
   public boolean canHaveDakrMessenger() {
      return false;
   }

   @Override
   public String getCreatorName() {
      return null;
   }

   @Override
   public float getDamage() {
      return this.damage;
   }

   @Override
   public final String getName() {
      return this.getTypeName();
   }

   @Override
   public float getQualityLevel() {
      return this.currentQL;
   }

   @Override
   public boolean hasCourier() {
      return this.permissions.hasPermission(Permissions.Allow.HAS_COURIER.getBit());
   }

   @Override
   public boolean hasDarkMessenger() {
      return this.permissions.hasPermission(Permissions.Allow.HAS_DARK_MESSENGER.getBit());
   }

   @Override
   public boolean hasNoDecay() {
      return this.permissions.hasPermission(Permissions.Allow.DECAY_DISABLED.getBit());
   }

   @Override
   public boolean isAlwaysLit() {
      return this.permissions.hasPermission(Permissions.Allow.ALWAYS_LIT.getBit());
   }

   @Override
   public boolean isAutoFilled() {
      return this.permissions.hasPermission(Permissions.Allow.AUTO_FILL.getBit());
   }

   @Override
   public boolean isAutoLit() {
      return this.permissions.hasPermission(Permissions.Allow.AUTO_LIGHT.getBit());
   }

   @Override
   public boolean isIndestructible() {
      return this.permissions.hasPermission(Permissions.Allow.NO_BASH.getBit());
   }

   @Override
   public boolean isNoDrag() {
      return this.permissions.hasPermission(Permissions.Allow.NO_DRAG.getBit());
   }

   @Override
   public boolean isNoDrop() {
      return this.permissions.hasPermission(Permissions.Allow.NO_DROP.getBit());
   }

   @Override
   public boolean isNoEatOrDrink() {
      return this.permissions.hasPermission(Permissions.Allow.NO_EAT_OR_DRINK.getBit());
   }

   @Override
   public boolean isNoImprove() {
      return this.permissions.hasPermission(Permissions.Allow.NO_IMPROVE.getBit());
   }

   @Override
   public boolean isNoMove() {
      return this.permissions.hasPermission(Permissions.Allow.NOT_MOVEABLE.getBit());
   }

   @Override
   public boolean isNoPut() {
      return this.permissions.hasPermission(Permissions.Allow.NO_PUT.getBit());
   }

   @Override
   public boolean isNoRepair() {
      return this.permissions.hasPermission(Permissions.Allow.NO_REPAIR.getBit());
   }

   @Override
   public boolean isNoTake() {
      return this.permissions.hasPermission(Permissions.Allow.NO_TAKE.getBit());
   }

   @Override
   public boolean isNotLockable() {
      return this.permissions.hasPermission(Permissions.Allow.NOT_LOCKABLE.getBit());
   }

   @Override
   public boolean isNotLockpickable() {
      return this.permissions.hasPermission(Permissions.Allow.NOT_LOCKPICKABLE.getBit());
   }

   @Override
   public boolean isNotPaintable() {
      return this.permissions.hasPermission(Permissions.Allow.NOT_PAINTABLE.getBit());
   }

   @Override
   public boolean isNotRuneable() {
      return true;
   }

   @Override
   public boolean isNotSpellTarget() {
      return this.permissions.hasPermission(Permissions.Allow.NO_SPELLS.getBit());
   }

   @Override
   public boolean isNotTurnable() {
      return this.permissions.hasPermission(Permissions.Allow.NOT_TURNABLE.getBit());
   }

   @Override
   public boolean isOwnerMoveable() {
      return this.permissions.hasPermission(Permissions.Allow.OWNER_MOVEABLE.getBit());
   }

   @Override
   public boolean isOwnerTurnable() {
      return this.permissions.hasPermission(Permissions.Allow.OWNER_TURNABLE.getBit());
   }

   @Override
   public boolean isPlanted() {
      return this.permissions.hasPermission(Permissions.Allow.PLANTED.getBit());
   }

   @Override
   public final boolean isSealedByPlayer() {
      return this.permissions.hasPermission(Permissions.Allow.SEALED_BY_PLAYER.getBit());
   }

   @Override
   public void setCreator(String aNewCreator) {
   }

   @Override
   public abstract boolean setDamage(float var1);

   @Override
   public void setHasCourier(boolean aCourier) {
      this.permissions.setPermissionBit(Permissions.Allow.HAS_COURIER.getBit(), aCourier);
   }

   @Override
   public void setHasDarkMessenger(boolean aDarkmessenger) {
      this.permissions.setPermissionBit(Permissions.Allow.HAS_DARK_MESSENGER.getBit(), aDarkmessenger);
   }

   @Override
   public void setHasNoDecay(boolean aNoDecay) {
      this.permissions.setPermissionBit(Permissions.Allow.DECAY_DISABLED.getBit(), aNoDecay);
   }

   @Override
   public void setIsAlwaysLit(boolean aAlwaysLit) {
      this.permissions.setPermissionBit(Permissions.Allow.ALWAYS_LIT.getBit(), aAlwaysLit);
   }

   @Override
   public void setIsAutoFilled(boolean aAutoFill) {
      this.permissions.setPermissionBit(Permissions.Allow.AUTO_FILL.getBit(), aAutoFill);
   }

   @Override
   public void setIsAutoLit(boolean aAutoLight) {
      this.permissions.setPermissionBit(Permissions.Allow.AUTO_LIGHT.getBit(), aAutoLight);
   }

   @Override
   public void setIsIndestructible(boolean aNoDestroy) {
      this.permissions.setPermissionBit(Permissions.Allow.NO_BASH.getBit(), aNoDestroy);
   }

   @Override
   public void setIsNoDrag(boolean aNoDrag) {
      this.permissions.setPermissionBit(Permissions.Allow.NO_DRAG.getBit(), aNoDrag);
   }

   @Override
   public void setIsNoDrop(boolean aNoDrop) {
      this.permissions.setPermissionBit(Permissions.Allow.NO_DROP.getBit(), aNoDrop);
   }

   @Override
   public void setIsNoEatOrDrink(boolean aNoEatOrDrink) {
      this.permissions.setPermissionBit(Permissions.Allow.NO_EAT_OR_DRINK.getBit(), aNoEatOrDrink);
   }

   @Override
   public void setIsNoImprove(boolean aNoImprove) {
      this.permissions.setPermissionBit(Permissions.Allow.NO_IMPROVE.getBit(), aNoImprove);
   }

   @Override
   public void setIsNoMove(boolean aNoMove) {
      this.permissions.setPermissionBit(Permissions.Allow.NOT_MOVEABLE.getBit(), aNoMove);
   }

   @Override
   public void setIsNoPut(boolean aNoPut) {
      this.permissions.setPermissionBit(Permissions.Allow.NO_PUT.getBit(), aNoPut);
   }

   @Override
   public void setIsNoRepair(boolean aNoRepair) {
      this.permissions.setPermissionBit(Permissions.Allow.NO_REPAIR.getBit(), aNoRepair);
   }

   @Override
   public void setIsNoTake(boolean aNoTake) {
      this.permissions.setPermissionBit(Permissions.Allow.NO_TAKE.getBit(), aNoTake);
   }

   @Override
   public void setIsNotLockable(boolean aNoLock) {
      this.permissions.setPermissionBit(Permissions.Allow.NOT_LOCKABLE.getBit(), aNoLock);
   }

   @Override
   public void setIsNotLockpickable(boolean aNoLockpick) {
      this.permissions.setPermissionBit(Permissions.Allow.NOT_LOCKPICKABLE.getBit(), aNoLockpick);
   }

   @Override
   public void setIsNotPaintable(boolean aNoPaint) {
      this.permissions.setPermissionBit(Permissions.Allow.NOT_PAINTABLE.getBit(), aNoPaint);
   }

   @Override
   public void setIsNotRuneable(boolean aNoRune) {
      this.permissions.setPermissionBit(Permissions.Allow.NOT_RUNEABLE.getBit(), aNoRune);
   }

   @Override
   public void setIsNotSpellTarget(boolean aNoSpells) {
      this.permissions.setPermissionBit(Permissions.Allow.NO_SPELLS.getBit(), aNoSpells);
   }

   @Override
   public void setIsNotTurnable(boolean aNoTurn) {
      this.permissions.setPermissionBit(Permissions.Allow.NOT_TURNABLE.getBit(), aNoTurn);
   }

   @Override
   public void setIsOwnerMoveable(boolean aOwnerMove) {
      this.permissions.setPermissionBit(Permissions.Allow.OWNER_MOVEABLE.getBit(), aOwnerMove);
   }

   @Override
   public void setIsOwnerTurnable(boolean aOwnerTurn) {
      this.permissions.setPermissionBit(Permissions.Allow.OWNER_TURNABLE.getBit(), aOwnerTurn);
   }

   @Override
   public void setIsPlanted(boolean aPlant) {
      this.permissions.setPermissionBit(Permissions.Allow.PLANTED.getBit(), aPlant);
   }

   @Override
   public void setIsSealedByPlayer(boolean aSealed) {
      this.permissions.setPermissionBit(Permissions.Allow.SEALED_BY_PLAYER.getBit(), aSealed);
   }

   @Override
   public abstract boolean setQualityLevel(float var1);

   @Override
   public void setOriginalQualityLevel(float newQL) {
   }

   @Override
   public abstract void savePermissions();

   @Override
   public final boolean isOnSouthBorder(TilePos pos) {
      return (this.getStartX() == pos.x || this.getEndX() == pos.x) && this.getEndY() == pos.y + 1 && this.getStartY() == pos.y + 1;
   }

   @Override
   public final boolean isOnNorthBorder(TilePos pos) {
      return (this.getStartX() == pos.x || this.getEndX() == pos.x) && this.getEndY() == pos.y && this.getStartY() == pos.y;
   }

   @Override
   public final boolean isOnWestBorder(TilePos pos) {
      return this.getStartX() == pos.x && this.getEndX() == pos.x && (this.getEndY() == pos.y || this.getStartY() == pos.y);
   }

   @Override
   public final boolean isOnEastBorder(TilePos pos) {
      return this.getStartX() == pos.x + 1 && this.getEndX() == pos.x + 1 && (this.getEndY() == pos.y || this.getStartY() == pos.y);
   }
}

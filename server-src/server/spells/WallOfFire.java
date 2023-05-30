package com.wurmonline.server.spells;

import com.wurmonline.mesh.Tiles;
import com.wurmonline.server.Server;
import com.wurmonline.server.creatures.Creature;
import com.wurmonline.server.skills.Skill;
import com.wurmonline.server.sounds.SoundPlayer;
import com.wurmonline.server.structures.DbFence;
import com.wurmonline.server.structures.Fence;
import com.wurmonline.server.structures.Wall;
import com.wurmonline.server.zones.NoSuchZoneException;
import com.wurmonline.server.zones.VolaTile;
import com.wurmonline.server.zones.Zone;
import com.wurmonline.server.zones.Zones;
import com.wurmonline.shared.constants.StructureConstantsEnum;

public class WallOfFire extends KarmaSpell {
   public static final int RANGE = 24;

   public WallOfFire() {
      super("Wall of Fire", 557, 10, 400, 10, 1, 0L);
      this.targetTileBorder = true;
      this.offensive = true;
      this.description = "creates a wall of fire on a tile border";
   }

   @Override
   boolean precondition(Skill castSkill, Creature performer, int tileBorderx, int tileBordery, int layer, int heightOffset, Tiles.TileBorderDirection dir) {
      VolaTile t = Zones.getTileOrNull(tileBorderx, tileBordery, layer == 0);
      if (t != null) {
         Wall[] walls = t.getWallsForLevel(heightOffset / 30);

         for(Wall wall : walls) {
            if (wall.isHorizontal() == (dir == Tiles.TileBorderDirection.DIR_HORIZ) && wall.getStartX() == tileBorderx && wall.getStartY() == tileBordery) {
               return false;
            }
         }

         Fence[] fences = t.getFencesForDir(dir);

         for(Fence f : fences) {
            if (f.getHeightOffset() == heightOffset) {
               return false;
            }
         }
      }

      if (dir == Tiles.TileBorderDirection.DIR_DOWN) {
         VolaTile t1 = Zones.getTileOrNull(tileBorderx, tileBordery, layer == 0);
         if (t1 != null) {
            for(Creature c : t1.getCreatures()) {
               if (c.isPlayer()) {
                  return false;
               }
            }
         }

         VolaTile t2 = Zones.getTileOrNull(tileBorderx - 1, tileBordery, layer == 0);
         if (t2 != null) {
            for(Creature c : t2.getCreatures()) {
               if (c.isPlayer()) {
                  return false;
               }
            }
         }
      } else {
         VolaTile t1 = Zones.getTileOrNull(tileBorderx, tileBordery, layer == 0);
         if (t1 != null) {
            for(Creature c : t1.getCreatures()) {
               if (c.isPlayer()) {
                  return false;
               }
            }
         }

         VolaTile t2 = Zones.getTileOrNull(tileBorderx, tileBordery - 1, layer == 0);
         if (t2 != null) {
            for(Creature c : t2.getCreatures()) {
               if (c.isPlayer()) {
                  return false;
               }
            }
         }
      }

      return true;
   }

   @Override
   void doEffect(Skill castSkill, double power, Creature performer, int tilex, int tiley, int layer, int heightOffset, Tiles.TileBorderDirection dir) {
      SoundPlayer.playSound("sound.religion.channel", tilex, tiley, performer.isOnSurface(), 0.0F);

      try {
         Zone zone = Zones.getZone(tilex, tiley, true);
         Fence fence = new DbFence(StructureConstantsEnum.FENCE_MAGIC_FIRE, tilex, tiley, heightOffset, (float)(1.0 + power / 5.0), dir, zone.getId(), layer);
         fence.setState(fence.getFinishState());
         fence.setQualityLevel((float)power);
         fence.improveOrigQualityLevel((float)power);
         zone.addFence(fence);
         performer.achievement(320);
         performer.getCommunicator().sendNormalServerMessage("You weave the source and create a wall.");
         Server.getInstance().broadCastAction(performer.getName() + " creates a wall.", performer, 5);
      } catch (NoSuchZoneException var12) {
      }
   }
}

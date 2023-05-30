package com.wurmonline.server.spells;

import com.wurmonline.mesh.GrassData;
import com.wurmonline.mesh.Tiles;
import com.wurmonline.mesh.TreeData;
import com.wurmonline.server.Players;
import com.wurmonline.server.Server;
import com.wurmonline.server.creatures.Creature;
import com.wurmonline.server.skills.Skill;
import com.wurmonline.server.zones.NoSuchZoneException;
import com.wurmonline.server.zones.VolaTile;
import com.wurmonline.server.zones.Zone;
import com.wurmonline.server.zones.Zones;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

public class SproutTrees extends KarmaSpell {
   private static final Logger logger = Logger.getLogger(SproutTrees.class.getName());
   public static final int RANGE = 24;

   public SproutTrees() {
      super("Sprout trees", 634, 30, 400, 32, 1, 300000L);
      this.description = "sprouts trees on grass or dirt tiles in the area.";
      this.offensive = false;
      this.targetTile = true;
   }

   @Override
   boolean precondition(Skill castSkill, Creature performer, int tilex, int tiley, int layer) {
      if (layer < 0) {
         performer.getCommunicator().sendNormalServerMessage("You need to be on the surface to cast this spell");
         return false;
      } else {
         try {
            Zone zone = Zones.getZone(tilex, tiley, true);
            VolaTile tile = zone.getOrCreateTile(tilex, tiley);
            if (tile.getVillage() != null) {
               if (performer.getCitizenVillage() == null) {
                  performer.getCommunicator().sendNormalServerMessage("You may not cast that spell on someone elses deed.");
                  return false;
               }

               if (performer.getCitizenVillage().getId() != tile.getVillage().getId()) {
                  performer.getCommunicator().sendNormalServerMessage("You may not cast that spell on someone elses deed.");
                  return false;
               }
            }

            return true;
         } catch (NoSuchZoneException var8) {
            performer.getCommunicator().sendNormalServerMessage("You fail to focus the spell on that area.");
            return false;
         }
      }
   }

   @Override
   void doEffect(Skill castSkill, double power, Creature performer, int tilex, int tiley, int layer, int heightOffset) {
      List<VolaTile> tiles = null;

      try {
         Zone zone = Zones.getZone(tilex, tiley, true);
         tiles = new ArrayList<>();

         for(int x = tilex - 2; x < tilex + 2; ++x) {
            for(int y = tiley - 2; y < tiley + 2; ++y) {
               VolaTile tile = zone.getOrCreateTile(x, y);
               if (tile.getVillage() == null || performer.getCitizenVillage() != null && performer.getCitizenVillage().getId() == tile.getVillage().getId()) {
                  tiles.add(tile);
               }
            }
         }
      } catch (NoSuchZoneException var18) {
         logger.log(Level.WARNING, "Unable to find zone for sprout trees.", (Throwable)var18);
      }

      if (tiles != null && tiles.size() != 0) {
         int treeType = Math.min(13, (int)(power / 7.5));

         for(int i = 0; i < tiles.size(); ++i) {
            VolaTile currTile = tiles.get(i);
            if (!this.needsToCheckSurrounding(treeType) || !this.isTreeNearby(currTile.tilex, currTile.tiley)) {
               int tileId = Server.surfaceMesh.getTile(currTile.tilex, currTile.tiley);
               byte type = Tiles.decodeType(tileId);
               if (type == 5 || type == 2 || type == 10) {
                  byte age = (byte)(7 + Server.rand.nextInt(8));
                  byte ttype;
                  if (type == 10) {
                     ttype = TreeData.TreeType.fromInt(type).asMyceliumTree();
                  } else {
                     ttype = TreeData.TreeType.fromInt(type).asNormalTree();
                  }

                  byte newData = Tiles.encodeTreeData(age, false, false, GrassData.GrowthTreeStage.SHORT);
                  Server.surfaceMesh.setTile(currTile.tilex, currTile.tiley, Tiles.encode(Tiles.decodeHeight(tileId), ttype, newData));
                  Players.getInstance().sendChangedTile(currTile.tilex, currTile.tiley, true, false);
               }
            }
         }
      } else {
         performer.getCommunicator().sendNormalServerMessage("You fail to focus the spell on that area.");
      }
   }

   private boolean needsToCheckSurrounding(int type) {
      return type == TreeData.TreeType.OAK.getTypeId() || type == TreeData.TreeType.WILLOW.getTypeId();
   }

   private boolean isTreeNearby(int tx, int ty) {
      for(int x = tx - 1; x < tx + 1; ++x) {
         for(int y = ty - 1; y < ty + 1; ++y) {
            if (x != tx || y != ty) {
               try {
                  int tileId = Server.surfaceMesh.getTile(x, y);
                  byte type = Tiles.decodeType(tileId);
                  Tiles.Tile tile = Tiles.getTile(type);
                  if (tile.isTree()) {
                     return true;
                  }
               } catch (Exception var8) {
                  logger.log(Level.FINEST, var8.getMessage(), (Throwable)var8);
               }
            }
         }
      }

      return false;
   }
}

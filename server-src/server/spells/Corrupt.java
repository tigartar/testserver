package com.wurmonline.server.spells;

import com.wurmonline.mesh.Tiles;
import com.wurmonline.server.Players;
import com.wurmonline.server.Server;
import com.wurmonline.server.Servers;
import com.wurmonline.server.creatures.Creature;
import com.wurmonline.server.skills.Skill;
import com.wurmonline.server.zones.FaithZone;
import com.wurmonline.server.zones.NoSuchZoneException;
import com.wurmonline.server.zones.VolaTile;
import com.wurmonline.server.zones.Zones;

public class Corrupt extends ReligiousSpell {
   public static final int RANGE = 4;

   public Corrupt() {
      super("Corrupt", 446, 30, 26, 30, 33, 0L);
      this.targetTile = true;
      this.description = "corrupts a small area of land with mycelium";
      this.type = 1;
   }

   @Override
   boolean precondition(Skill castSkill, Creature performer, int tilex, int tiley, int layer) {
      if (!Servers.localServer.PVPSERVER) {
         performer.getCommunicator().sendNormalServerMessage("This spell does not work here.", (byte)3);
         return false;
      } else if (performer.getLayer() < 0) {
         performer.getCommunicator().sendNormalServerMessage("This spell does not work below ground.", (byte)3);
         return false;
      } else if (Tiles.decodeHeight(Server.surfaceMesh.getTile(tilex, tiley)) < 0) {
         performer.getCommunicator().sendNormalServerMessage("This spell does not work below water.", (byte)3);
         return false;
      } else {
         return true;
      }
   }

   @Override
   void doEffect(Skill castSkill, double power, Creature performer, int tilex, int tiley, int layer, int heightOffset) {
      performer.getCommunicator()
         .sendNormalServerMessage("An invigorating energy flows through you into the ground and reaches the roots of plants and trees.");
      int sx = Zones.safeTileX(tilex - 1 - performer.getNumLinks());
      int sy = Zones.safeTileY(tiley - 1 - performer.getNumLinks());
      int ex = Zones.safeTileX(tilex + 1 + performer.getNumLinks());
      int ey = Zones.safeTileY(tiley + 1 + performer.getNumLinks());
      boolean blocked = false;

      for(int x = sx; x <= ex; ++x) {
         for(int y = sy; y <= ey; ++y) {
            try {
               FaithZone fz = Zones.getFaithZone(x, y, true);
               boolean ok = false;
               if (fz != null) {
                  if (fz.getCurrentRuler() == null || fz.getCurrentRuler() == performer.getDeity() || fz.getCurrentRuler().isHateGod()) {
                     ok = true;
                  }
               } else {
                  ok = true;
               }

               if (ok) {
                  VolaTile t = Zones.getOrCreateTile(x, y, true);
                  if (t != null && t.getVillage() != null && t.getVillage().kingdom != performer.getKingdomId()) {
                     blocked = true;
                  } else {
                     int tile = Server.surfaceMesh.getTile(x, y);
                     byte type = Tiles.decodeType(tile);
                     Tiles.Tile theTile = Tiles.getTile(type);
                     byte data = Tiles.decodeData(tile);
                     if (type == Tiles.Tile.TILE_FIELD.id
                        || type == Tiles.Tile.TILE_FIELD2.id
                        || type == Tiles.Tile.TILE_GRASS.id
                        || type == Tiles.Tile.TILE_REED.id
                        || type == Tiles.Tile.TILE_DIRT.id
                        || type == Tiles.Tile.TILE_LAWN.id
                        || type == Tiles.Tile.TILE_STEPPE.id
                        || theTile.isNormalTree()
                        || theTile.isEnchanted()
                        || theTile.isNormalBush()) {
                        if (theTile.isNormalTree()) {
                           Server.setSurfaceTile(x, y, Tiles.decodeHeight(tile), theTile.getTreeType(data).asMyceliumTree(), data);
                        } else if (theTile.isEnchantedTree()) {
                           Server.setSurfaceTile(x, y, Tiles.decodeHeight(tile), theTile.getTreeType(data).asNormalTree(), data);
                        } else if (theTile.isNormalBush()) {
                           Server.setSurfaceTile(x, y, Tiles.decodeHeight(tile), theTile.getBushType(data).asMyceliumBush(), data);
                        } else if (theTile.isEnchantedBush()) {
                           Server.setSurfaceTile(x, y, Tiles.decodeHeight(tile), theTile.getBushType(data).asNormalBush(), data);
                        } else if (type == Tiles.Tile.TILE_LAWN.id) {
                           Server.setSurfaceTile(x, y, Tiles.decodeHeight(tile), Tiles.Tile.TILE_MYCELIUM_LAWN.id, (byte)0);
                        } else {
                           Server.setSurfaceTile(x, y, Tiles.decodeHeight(tile), Tiles.Tile.TILE_MYCELIUM.id, (byte)0);
                        }

                        Players.getInstance().sendChangedTile(x, y, true, false);
                     }
                  }
               } else {
                  blocked = true;
               }
            } catch (NoSuchZoneException var23) {
            }
         }
      }

      if (blocked) {
         performer.getCommunicator().sendNormalServerMessage("The domain of another deity or settlement protects this area.", (byte)3);
      }
   }
}

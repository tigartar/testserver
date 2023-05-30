package com.wurmonline.server.behaviours;

import com.wurmonline.mesh.MeshIO;
import com.wurmonline.mesh.Tiles;
import com.wurmonline.server.Server;
import com.wurmonline.server.WurmCalendar;
import com.wurmonline.server.creatures.Creature;
import com.wurmonline.server.players.Player;

public enum ModifiedBy {
   NOTHING(0),
   NO_TREES(1),
   NEAR_TREE(2),
   NEAR_BUSH(3),
   NEAR_OAK(4),
   EASTER(5),
   HUNGER(6),
   WOUNDED(7),
   NEAR_WATER(8);

   private final int code;

   private ModifiedBy(int aCode) {
      this.code = aCode;
   }

   public float chanceModifier(Creature performer, int modifier, int tilex, int tiley) {
      if (this == NOTHING) {
         return 0.0F;
      } else if (this == EASTER) {
         return performer.isPlayer() && (!((Player)performer).isReallyPaying() || !WurmCalendar.isEaster() || ((Player)performer).isReimbursed())
            ? 0.0F
            : (float)modifier;
      } else if (this == HUNGER) {
         return performer.getStatus().getHunger() < 20 ? (float)modifier : 0.0F;
      } else if (this == WOUNDED) {
         return performer.getStatus().damage > 15 ? (float)modifier : 0.0F;
      } else {
         MeshIO mesh = Server.surfaceMesh;
         if (this.isAModifier(mesh.getTile(tilex, tiley))) {
            return this == NO_TREES ? 0.0F : (float)modifier;
         } else {
            for(int x = -1; x <= 1; ++x) {
               for(int y = -1; y <= 1; ++y) {
                  if ((x == -1 || x == 1 || y == -1 || y == 1) && this.isAModifier(mesh.getTile(tilex + x, tiley + y))) {
                     if (this == NO_TREES) {
                        return 0.0F;
                     }

                     return (float)(modifier / 2);
                  }
               }
            }

            for(int x = -2; x <= 2; ++x) {
               for(int y = -2; y <= 2; ++y) {
                  if ((x == -2 || x == 2 || y == -2 || y == 2) && this.isAModifier(mesh.getTile(tilex + x, tiley + y))) {
                     if (this == NO_TREES) {
                        return 0.0F;
                     }

                     return (float)(modifier / 3);
                  }
               }
            }

            for(int x = -5; x <= 5; ++x) {
               for(int y = -5; y <= 5; ++y) {
                  if ((x <= -3 || x >= 3 || y <= -3 || y >= 3) && this.isAModifier(mesh.getTile(tilex + x, tiley + y))) {
                     if (this == NO_TREES) {
                        return 0.0F;
                     }

                     return (float)(modifier / 4);
                  }
               }
            }

            return this == NO_TREES ? (float)modifier : 0.0F;
         }
      }
   }

   private boolean isAModifier(int tile) {
      if (this == NEAR_WATER) {
         return Tiles.decodeHeight(tile) < 5;
      } else {
         byte decodedType = Tiles.decodeType(tile);
         byte decodedData = Tiles.decodeData(tile);
         Tiles.Tile theTile = Tiles.getTile(decodedType);
         if (this == NEAR_OAK) {
            if (theTile.isNormalTree()) {
               return theTile.isOak(decodedData);
            }
         } else {
            if (this == NEAR_TREE || this == NO_TREES) {
               return theTile.isNormalTree();
            }

            if (this == NEAR_BUSH) {
               return theTile.isNormalBush();
            }
         }

         return false;
      }
   }
}

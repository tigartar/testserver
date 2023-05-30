package com.wurmonline.server.structures;

import com.wurmonline.math.Vector3f;
import com.wurmonline.mesh.Tiles;
import com.wurmonline.server.MiscConstants;
import com.wurmonline.server.Server;
import com.wurmonline.server.creatures.Creature;
import com.wurmonline.server.creatures.ai.PathTile;
import com.wurmonline.server.items.Item;
import com.wurmonline.server.players.Player;
import com.wurmonline.server.zones.VolaTile;
import com.wurmonline.server.zones.Zones;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.annotation.Nullable;

public final class Blocking implements MiscConstants {
   public static final double RADS_TO_DEGS = 180.0 / Math.PI;
   public static final float DEGS_TO_RADS = (float) (Math.PI / 180.0);
   private static final float MAXSTEP = 3.0F;
   private static final Logger logger = Logger.getLogger(Blocking.class.getName());

   private Blocking() {
   }

   public static final BlockingResult getRangedBlockerBetween(Creature performer, Item target) {
      return getBlockerBetween(
         performer,
         performer.getPosX(),
         performer.getPosY(),
         target.getPosX(),
         target.getPosY(),
         performer.getPositionZ(),
         target.getPosZ(),
         performer.isOnSurface(),
         target.isOnSurface(),
         true,
         5,
         target.getWurmId(),
         performer.getBridgeId(),
         target.getBridgeId(),
         performer.followsGround() || target.getFloorLevel() == 0 && target.getBridgeId() == -10L
      );
   }

   public static final BlockingResult getBlockerBetween(Creature performer, Item target, int blockingType) {
      return getBlockerBetween(
         performer,
         performer.getPosX(),
         performer.getPosY(),
         target.getPosX(),
         target.getPosY(),
         performer.getPositionZ(),
         target.getPosZ(),
         performer.isOnSurface(),
         target.isOnSurface(),
         false,
         blockingType,
         target.getWurmId(),
         performer.getBridgeId(),
         target.getBridgeId(),
         performer.followsGround() || target.getFloorLevel() == 0 && target.getBridgeId() == -10L
      );
   }

   public static final BlockingResult getBlockerBetween(Creature performer, Floor floor, int blockingType) {
      return getBlockerBetween(
         performer,
         performer.getPosX(),
         performer.getPosY(),
         (float)(2 + Tiles.decodeTileX(floor.getId()) * 4),
         (float)(2 + Tiles.decodeTileY(floor.getId()) * 4),
         performer.getPositionZ(),
         floor.getMinZ() - 0.25F,
         performer.isOnSurface(),
         floor.isOnSurface(),
         false,
         !performer.isOnSurface() ? 7 : blockingType,
         floor.getId(),
         performer.getBridgeId(),
         -10L,
         false
      );
   }

   public static final BlockingResult getBlockerBetween(
      Creature performer, long target, boolean targetSurfaced, int blockingType, long sourceBridgeId, long targetBridgeId
   ) {
      return getBlockerBetween(performer, target, targetSurfaced, blockingType, sourceBridgeId, targetBridgeId, 0);
   }

   public static final BlockingResult getBlockerBetween(
      Creature performer, long target, boolean targetSurfaced, int blockingType, long sourceBridgeId, long targetBridgeId, int ceilingHeight
   ) {
      return getBlockerBetween(
         performer,
         performer.getPosX(),
         performer.getPosY(),
         (float)(2 + Tiles.decodeTileX(target) * 4),
         (float)(2 + Tiles.decodeTileY(target) * 4),
         performer.getPositionZ(),
         Zones.getHeightForNode(Tiles.decodeTileX(target), Tiles.decodeTileY(target), targetSurfaced ? 0 : -1)
            + (float)(Tiles.decodeFloorLevel(target) * 3)
            + (float)ceilingHeight,
         performer.isOnSurface(),
         targetSurfaced,
         false,
         !performer.isOnSurface() && blockingType != 8 ? 7 : blockingType,
         target,
         sourceBridgeId,
         targetBridgeId,
         false
      );
   }

   public static final BlockingResult getBlockerBetween(Creature performer, Wall target, int blockingType) {
      float tpx = target.getPositionX();
      float tpy = target.getPositionY();
      if (target.getDir() == Tiles.TileBorderDirection.DIR_HORIZ) {
         if (tpy > (float)(performer.getTileY() * 4)) {
            tpy -= 4.0F;
         }
      } else if (tpx > (float)(performer.getTileX() * 4)) {
         tpx -= 4.0F;
      }

      return getBlockerBetween(
         performer,
         performer.getPosX(),
         performer.getPosY(),
         tpx,
         tpy,
         performer.getPositionZ(),
         target.getMinZ(),
         performer.isOnSurface(),
         target.isOnSurface(),
         false,
         blockingType,
         target.getId(),
         performer.getBridgeId(),
         -10L,
         false
      );
   }

   public static final BlockingResult getBlockerBetween(Creature performer, Fence target, int blockingType) {
      float tpx = target.getPositionX();
      float tpy = target.getPositionY();
      if (target.getDir() == Tiles.TileBorderDirection.DIR_HORIZ) {
         if (tpy > (float)(performer.getTileY() * 4)) {
            tpy -= 4.0F;
         }
      } else if (tpx > (float)(performer.getTileX() * 4)) {
         tpx -= 4.0F;
      }

      return getBlockerBetween(
         performer,
         performer.getPosX(),
         performer.getPosY(),
         tpx,
         tpy,
         performer.getPositionZ(),
         target.getMinZ(),
         performer.isOnSurface(),
         target.isOnSurface(),
         false,
         blockingType,
         target.getId(),
         performer.getBridgeId(),
         -10L,
         false
      );
   }

   public static final BlockingResult getRangedBlockerBetween(Creature performer, Creature target) {
      return getBlockerBetween(
         performer,
         performer.getPosX(),
         performer.getPosY(),
         target.getPosX(),
         target.getPosY(),
         performer.getPositionZ() + (float)performer.getHalfHeightDecimeters() / 10.0F,
         target.getPositionZ() + (float)target.getHalfHeightDecimeters() / 10.0F,
         performer.isOnSurface(),
         target.isOnSurface(),
         true,
         4,
         target.getWurmId(),
         performer.getBridgeId(),
         target.getBridgeId(),
         false
      );
   }

   public static final BlockingResult getBlockerBetween(Creature performer, Creature target, int blockingType) {
      return getBlockerBetween(
         performer,
         performer.getPosX(),
         performer.getPosY(),
         target.getPosX(),
         target.getPosY(),
         performer.getPositionZ(),
         target.getPositionZ(),
         performer.isOnSurface(),
         target.isOnSurface(),
         false,
         blockingType,
         target.getWurmId(),
         performer.getBridgeId(),
         target.getBridgeId(),
         performer.followsGround() || target.followsGround()
      );
   }

   public static final BlockingResult getBlockerBetween(
      @Nullable Creature creature,
      float startx,
      float starty,
      float endx,
      float endy,
      float startZ,
      float endZ,
      boolean surfaced,
      boolean targetSurfaced,
      boolean rangedAttack,
      int typeChecked,
      long target,
      long sourceBridgeId,
      long targetBridgeId,
      boolean followGround
   ) {
      return getBlockerBetween(
         creature,
         startx,
         starty,
         endx,
         endy,
         startZ,
         endZ,
         surfaced,
         targetSurfaced,
         rangedAttack,
         typeChecked,
         true,
         target,
         sourceBridgeId,
         targetBridgeId,
         followGround
      );
   }

   public static final boolean isSameFloorLevel(float startZ, float endZ) {
      return Math.abs(startZ - endZ) < 3.0F;
   }

   public static final BlockingResult getBlockerBetween(
      @Nullable Creature creature,
      float startx,
      float starty,
      float endx,
      float endy,
      float startZ,
      float endZ,
      boolean surfaced,
      boolean targetSurfaced,
      boolean rangedAttack,
      int typeChecked,
      boolean test,
      long target,
      long sourceBridgeId,
      long targetBridgeId,
      boolean followGround
   ) {
      int starttilex = Zones.safeTileX((int)startx >> 2);
      int starttiley = Zones.safeTileY((int)starty >> 2);
      int endtilex = Zones.safeTileX((int)endx >> 2);
      int endtiley = Zones.safeTileY((int)endy >> 2);
      int max = rangedAttack ? 100 : 50;
      if (starttilex == endtilex && starttiley == endtiley && isSameFloorLevel(startZ, endZ)) {
         return null;
      } else if (typeChecked == 0) {
         return null;
      } else {
         if (!rangedAttack && creature != null) {
            if (!creature.isPlayer()) {
               max = creature.getMaxHuntDistance() + 5;
            }

            Creature targetCret = creature.getTarget();
            if (targetCret != null && targetCret.getWurmId() == target) {
               creature.sendToLoggers(
                  "Now checking "
                     + starttilex
                     + ","
                     + starttiley
                     + " to "
                     + endtilex
                     + ","
                     + endtiley
                     + " startZ="
                     + startZ
                     + " endZ="
                     + endZ
                     + " surf="
                     + surfaced
                     + ","
                     + targetSurfaced
                     + " follow ground="
                     + followGround
               );
            }
         }

         int nextTileX = starttilex;
         int nextTileY = starttiley;
         boolean isTransition = false;
         if (creature != null && !creature.isOnSurface()) {
            isTransition = false;
            if (Tiles.decodeType(Server.caveMesh.getTile(starttilex, starttiley)) == Tiles.Tile.TILE_CAVE_EXIT.id) {
               isTransition = true;
            }

            if (creature.isPlayer() && typeChecked != 6) {
               Vector3f actualStart = ((Player)creature).getActualPosVehicle();
               int actualStartX = Zones.safeTileX((int)actualStart.x >> 2);
               int actualStartY = Zones.safeTileX((int)actualStart.y >> 2);
               int tile = Server.caveMesh.getTile(actualStartX, actualStartY);
               if (Tiles.isSolidCave(Tiles.decodeType(tile))) {
                  BlockingResult toReturn = new BlockingResult();
                  PathTile blocker = new PathTile(actualStartX, actualStartY, tile, false, -1);
                  toReturn.addBlocker(blocker, blocker.getCenterPoint(), 100.0F);
                  return toReturn;
               }
            }
         }

         Vector3f startPos = new Vector3f(startx, starty, startZ + 0.5F);
         Vector3f endPos = new Vector3f(endx, endy, endZ + 0.5F);
         Vector3f lastPos = new Vector3f(startPos);
         Vector3f nextPos = new Vector3f(startPos);
         Vector3f dir = new Vector3f(endPos.subtract(startPos)).normalize();
         BlockingResult result = null;
         boolean found = false;
         int debugChecks = 0;

         while(!found) {
            Vector3f remain = endPos.subtract(lastPos);
            if (remain.length() < 3.0F) {
               if (debugChecks++ > 60) {
                  found = true;
               } else if (remain.length() == 0.0F) {
                  found = true;
               }

               nextPos.addLocal(remain);
            } else {
               nextPos.addLocal(dir.mult(3.0F));
               if (debugChecks++ > 60) {
                  if (creature != null) {
                     logger.log(Level.INFO, creature.getName() + " checking " + 3.0F + " meters failed. Checks=" + debugChecks);
                  }

                  found = true;
               }
            }

            int lastTileY = nextTileY;
            int lastTileX = nextTileX;
            nextTileX = (int)nextPos.x >> 2;
            nextTileY = (int)nextPos.y >> 2;
            int diffX = nextTileX - lastTileX;
            int diffY = nextTileY - lastTileY;
            if (diffX == 0 && diffY == 0) {
               nextPos.z = endPos.z;
               lastPos.z = startPos.z;
            }

            if (diffX != 0 || diffY != 0 || !isSameFloorLevel(lastPos.z, nextPos.z)) {
               if (!surfaced && (!isTransition || !targetSurfaced) && typeChecked != 1 && typeChecked != 2 && typeChecked != 3) {
                  int t = Server.caveMesh.getTile(endtilex, endtiley);
                  if (Tiles.isSolidCave(Tiles.decodeType(t)) && typeChecked == 6) {
                     result = new BlockingResult();
                     PathTile blocker = new PathTile(endtilex, endtiley, t, false, -1);
                     result.addBlocker(blocker, blocker.getCenterPoint(), 100.0F);
                     return result;
                  }

                  result = isDiagonalRockBetween(creature, lastTileX, lastTileY, nextTileX, nextTileY);
                  if (result == null) {
                     result = isStraightRockBetween(creature, lastTileX, lastTileY, nextTileX, nextTileY);
                  }

                  if (result != null) {
                     if (typeChecked != 7 && typeChecked != 8
                        || result.getFirstBlocker().getTileX() != endtilex
                        || result.getFirstBlocker().getTileY() != endtiley) {
                        return result;
                     }

                     result = null;
                  }
               }

               VolaTile checkedTile = null;
               if (diffX >= 0 && diffY >= 0) {
                  for(int x = Math.min(0, diffX); x <= diffX; ++x) {
                     for(int y = Math.min(0, diffY); y <= diffY; ++y) {
                        checkedTile = Zones.getTileOrNull(lastTileX + x, lastTileY + y, surfaced);
                        if (checkedTile != null) {
                           result = returnIterativeCheck(
                              checkedTile,
                              result,
                              creature,
                              dir,
                              lastPos,
                              nextPos,
                              rangedAttack,
                              starttilex,
                              nextTileX,
                              starttiley,
                              nextTileY,
                              typeChecked,
                              target,
                              sourceBridgeId,
                              targetBridgeId,
                              followGround
                           );
                        }

                        if (surfaced != targetSurfaced) {
                           checkedTile = Zones.getTileOrNull(lastTileX + x, lastTileY + y, targetSurfaced);
                           if (checkedTile != null) {
                              result = returnIterativeCheck(
                                 checkedTile,
                                 result,
                                 creature,
                                 dir,
                                 lastPos,
                                 nextPos,
                                 rangedAttack,
                                 starttilex,
                                 nextTileX,
                                 starttiley,
                                 nextTileY,
                                 typeChecked,
                                 target,
                                 sourceBridgeId,
                                 targetBridgeId,
                                 followGround
                              );
                           }
                        }
                     }
                  }
               }

               if (diffX < 0 && diffY >= 0) {
                  for(int x = 0; x >= diffX; --x) {
                     for(int y = Math.min(0, diffY); y <= diffY; ++y) {
                        checkedTile = Zones.getTileOrNull(lastTileX + x, lastTileY + y, surfaced);
                        if (checkedTile != null) {
                           result = returnIterativeCheck(
                              checkedTile,
                              result,
                              creature,
                              dir,
                              lastPos,
                              nextPos,
                              rangedAttack,
                              starttilex,
                              nextTileX,
                              starttiley,
                              nextTileY,
                              typeChecked,
                              target,
                              sourceBridgeId,
                              targetBridgeId,
                              followGround
                           );
                        }

                        if (surfaced != targetSurfaced) {
                           checkedTile = Zones.getTileOrNull(lastTileX + x, lastTileY + y, targetSurfaced);
                           if (checkedTile != null) {
                              result = returnIterativeCheck(
                                 checkedTile,
                                 result,
                                 creature,
                                 dir,
                                 lastPos,
                                 nextPos,
                                 rangedAttack,
                                 starttilex,
                                 nextTileX,
                                 starttiley,
                                 nextTileY,
                                 typeChecked,
                                 target,
                                 sourceBridgeId,
                                 targetBridgeId,
                                 followGround
                              );
                           }
                        }
                     }
                  }
               }

               if (diffX >= 0 && diffY < 0) {
                  for(int x = Math.min(0, diffX); x <= diffX; ++x) {
                     for(int y = 0; y >= diffY; --y) {
                        checkedTile = Zones.getTileOrNull(lastTileX + x, lastTileY + y, surfaced);
                        if (checkedTile != null) {
                           result = returnIterativeCheck(
                              checkedTile,
                              result,
                              creature,
                              dir,
                              lastPos,
                              nextPos,
                              rangedAttack,
                              starttilex,
                              nextTileX,
                              starttiley,
                              nextTileY,
                              typeChecked,
                              target,
                              sourceBridgeId,
                              targetBridgeId,
                              followGround
                           );
                        }

                        if (surfaced != targetSurfaced) {
                           checkedTile = Zones.getTileOrNull(lastTileX + x, lastTileY + y, targetSurfaced);
                           if (checkedTile != null) {
                              result = returnIterativeCheck(
                                 checkedTile,
                                 result,
                                 creature,
                                 dir,
                                 lastPos,
                                 nextPos,
                                 rangedAttack,
                                 starttilex,
                                 nextTileX,
                                 starttiley,
                                 nextTileY,
                                 typeChecked,
                                 target,
                                 sourceBridgeId,
                                 targetBridgeId,
                                 followGround
                              );
                           }
                        }
                     }
                  }
               }

               if (diffX < 0 && diffY < 0) {
                  for(int x = 0; x >= diffX; --x) {
                     for(int y = 0; y >= diffY; --y) {
                        checkedTile = Zones.getTileOrNull(lastTileX + x, lastTileY + y, surfaced);
                        if (checkedTile != null) {
                           result = returnIterativeCheck(
                              checkedTile,
                              result,
                              creature,
                              dir,
                              lastPos,
                              nextPos,
                              rangedAttack,
                              starttilex,
                              nextTileX,
                              starttiley,
                              nextTileY,
                              typeChecked,
                              target,
                              sourceBridgeId,
                              targetBridgeId,
                              followGround
                           );
                        }

                        if (surfaced != targetSurfaced) {
                           checkedTile = Zones.getTileOrNull(lastTileX + x, lastTileY + y, targetSurfaced);
                           if (checkedTile != null) {
                              result = returnIterativeCheck(
                                 checkedTile,
                                 result,
                                 creature,
                                 dir,
                                 lastPos,
                                 nextPos,
                                 rangedAttack,
                                 starttilex,
                                 nextTileX,
                                 starttiley,
                                 nextTileY,
                                 typeChecked,
                                 target,
                                 sourceBridgeId,
                                 targetBridgeId,
                                 followGround
                              );
                           }
                        }
                     }
                  }
               }
            }

            lastPos.set(nextPos);
            if (found) {
               return result;
            }

            if (Math.abs(nextTileX - starttilex) > max || Math.abs(nextTileY - starttiley) > max) {
               return result;
            }
         }

         return result;
      }
   }

   private static final BlockingResult returnIterativeCheck(
      VolaTile checkedTile,
      BlockingResult result,
      Creature creature,
      Vector3f dir,
      Vector3f lastPos,
      Vector3f nextPos,
      boolean rangedAttack,
      int startTileX,
      int nextTileX,
      int startTileY,
      int nextTileY,
      int typeChecked,
      long targetId,
      long sourceBridgeId,
      long targetBridgeId,
      boolean followGround
   ) {
      Blocker[] blockers = null;
      Vector3f toCheck = lastPos.clone();
      if (typeChecked == 4 || typeChecked == 2 || typeChecked == 5 || typeChecked == 6 || typeChecked == 7) {
         Blocker[] var22 = checkedTile.getWalls();
         result = checkForResult(
            creature,
            result,
            var22,
            dir,
            toCheck,
            nextPos,
            rangedAttack,
            startTileX,
            nextTileX,
            startTileY,
            nextTileY,
            typeChecked,
            targetId,
            sourceBridgeId,
            targetBridgeId,
            followGround
         );
         if (result != null && result.getTotalCover() >= 100.0F) {
            return result;
         }
      }

      if (typeChecked == 4 || typeChecked == 1 || typeChecked == 5 || typeChecked == 6 || typeChecked == 7) {
         Blocker[] var23 = checkedTile.getFences();
         toCheck = lastPos.clone();
         result = checkForResult(
            creature,
            result,
            var23,
            dir,
            toCheck,
            nextPos,
            rangedAttack,
            startTileX,
            nextTileX,
            startTileY,
            nextTileY,
            typeChecked,
            targetId,
            sourceBridgeId,
            targetBridgeId,
            followGround
         );
         if (result != null && result.getTotalCover() >= 100.0F) {
            return result;
         }
      }

      if (typeChecked == 4 || typeChecked == 3 || typeChecked == 5 || typeChecked == 6 || typeChecked == 7) {
         Blocker[] var24 = checkedTile.getFloors();
         toCheck = lastPos.clone();
         result = checkForResult(
            creature,
            result,
            var24,
            dir,
            toCheck,
            nextPos,
            rangedAttack,
            startTileX,
            nextTileX,
            startTileY,
            nextTileY,
            typeChecked,
            targetId,
            sourceBridgeId,
            targetBridgeId,
            followGround
         );
         Blocker[] var25 = checkedTile.getBridgeParts();
         toCheck = lastPos.clone();
         result = checkForResult(
            creature,
            result,
            var25,
            dir,
            toCheck,
            nextPos,
            rangedAttack,
            startTileX,
            nextTileX,
            startTileY,
            nextTileY,
            typeChecked,
            targetId,
            sourceBridgeId,
            targetBridgeId,
            followGround
         );
         if (result != null && result.getTotalCover() >= 100.0F) {
            return result;
         }
      }

      return result;
   }

   private static final BlockingResult checkForResult(
      Creature creature,
      BlockingResult result,
      Blocker[] blockers,
      Vector3f dir,
      Vector3f startPos,
      Vector3f endPos,
      boolean rangedAttack,
      int starttilex,
      int currTileX,
      int starttiley,
      int currTileY,
      int blockType,
      long target,
      long sourceBridgeId,
      long targetBridgeId,
      boolean followGround
   ) {
      for(int w = 0; w < blockers.length; ++w) {
         if (blockers[w].isWithinZ(Math.max(startPos.z, endPos.z), Math.min(startPos.z, endPos.z), followGround)) {
            boolean skip = false;
            if (blockers[w] instanceof BridgePart) {
               BridgePart bp = (BridgePart)blockers[w];
               if (bp.getStructureId() == sourceBridgeId && (sourceBridgeId == targetBridgeId || blockType == 6)) {
                  skip = true;
               }
            }

            if (!skip) {
               Vector3f intersection = blockers[w].isBlocking(creature, startPos, endPos, dir, blockType, target, followGround);
               if (intersection != null) {
                  if (result == null) {
                     result = new BlockingResult();
                  }

                  if (!rangedAttack && blockType != 5) {
                     result.addBlocker(blockers[w], intersection, 100.0F);
                     return result;
                  }

                  float addedCover = blockers[w].getBlockPercent(creature);
                  if (Math.abs(starttilex - currTileX) <= 1 && Math.abs(starttiley - currTileY) <= 1 && !(addedCover >= 100.0F)) {
                     if (result.addBlocker(blockers[w], intersection, addedCover) >= 100.0F) {
                        return result;
                     }
                  } else {
                     if (addedCover >= 100.0F) {
                        result.addBlocker(blockers[w], intersection, 100.0F);
                        return result;
                     }

                     if (result.addBlocker(blockers[w], intersection, addedCover) >= 100.0F) {
                        return result;
                     }
                  }
               }
            }
         }
      }

      return result;
   }

   public static final BlockingResult isDiagonalRockBetween(Creature creature, int startx, int starty, int endx, int endy) {
      if (startx != endx && endy != starty) {
         int northTile = Server.caveMesh.getTile(Zones.safeTileX(startx), Zones.safeTileY(starty - 1));
         int southTile = Server.caveMesh.getTile(Zones.safeTileX(startx), Zones.safeTileY(starty + 1));
         int westTile = Server.caveMesh.getTile(Zones.safeTileX(startx - 1), Zones.safeTileY(starty));
         int eastTile = Server.caveMesh.getTile(Zones.safeTileX(startx + 1), Zones.safeTileY(starty));
         if (endx < startx) {
            if (endy < starty) {
               if (Tiles.isSolidCave(Tiles.decodeType(westTile)) && Tiles.isSolidCave(Tiles.decodeType(northTile))) {
                  BlockingResult result = new BlockingResult();
                  PathTile blocker = new PathTile(Zones.safeTileX(startx - 1), Zones.safeTileY(starty), westTile, false, -1);
                  result.addBlocker(blocker, blocker.getCenterPoint(), 100.0F);
                  PathTile blocker2 = new PathTile(Zones.safeTileX(startx), Zones.safeTileY(starty - 1), northTile, false, -1);
                  result.addBlocker(blocker2, blocker2.getCenterPoint(), 100.0F);
                  return result;
               }

               int nw = Server.caveMesh.getTile(Zones.safeTileX(startx - 1), Zones.safeTileY(starty - 1));
               if (Tiles.isSolidCave(Tiles.decodeType(nw))) {
                  BlockingResult result = new BlockingResult();
                  PathTile blocker = new PathTile(Zones.safeTileX(startx - 1), Zones.safeTileY(starty - 1), nw, false, -1);
                  result.addBlocker(blocker, blocker.getCenterPoint(), 100.0F);
                  return result;
               }
            }

            if (endy > starty) {
               if (Tiles.isSolidCave(Tiles.decodeType(westTile)) && Tiles.isSolidCave(Tiles.decodeType(southTile))) {
                  BlockingResult result = new BlockingResult();
                  PathTile blocker = new PathTile(Zones.safeTileX(startx - 1), Zones.safeTileY(starty), westTile, false, -1);
                  result.addBlocker(blocker, blocker.getCenterPoint(), 100.0F);
                  PathTile blocker2 = new PathTile(Zones.safeTileX(startx), Zones.safeTileY(starty + 1), southTile, false, -1);
                  result.addBlocker(blocker2, blocker2.getCenterPoint(), 100.0F);
                  return result;
               }

               int sw = Server.caveMesh.getTile(Zones.safeTileX(startx - 1), Zones.safeTileY(starty + 1));
               if (Tiles.isSolidCave(Tiles.decodeType(sw))) {
                  BlockingResult result = new BlockingResult();
                  PathTile blocker = new PathTile(Zones.safeTileX(startx - 1), Zones.safeTileY(starty + 1), sw, false, -1);
                  result.addBlocker(blocker, blocker.getCenterPoint(), 100.0F);
                  return result;
               }
            }
         } else {
            if (endy < starty) {
               if (Tiles.isSolidCave(Tiles.decodeType(eastTile)) && Tiles.isSolidCave(Tiles.decodeType(northTile))) {
                  BlockingResult result = new BlockingResult();
                  PathTile blocker = new PathTile(Zones.safeTileX(startx + 1), Zones.safeTileY(starty), eastTile, false, -1);
                  result.addBlocker(blocker, blocker.getCenterPoint(), 100.0F);
                  PathTile blocker2 = new PathTile(Zones.safeTileX(startx), Zones.safeTileY(starty - 1), northTile, false, -1);
                  result.addBlocker(blocker2, blocker2.getCenterPoint(), 100.0F);
                  return result;
               }

               int ne = Server.caveMesh.getTile(Zones.safeTileX(startx + 1), Zones.safeTileY(starty - 1));
               if (Tiles.isSolidCave(Tiles.decodeType(ne))) {
                  BlockingResult result = new BlockingResult();
                  PathTile blocker = new PathTile(Zones.safeTileX(startx + 1), Zones.safeTileY(starty - 1), ne, false, -1);
                  result.addBlocker(blocker, blocker.getCenterPoint(), 100.0F);
                  return result;
               }
            }

            if (endy > starty) {
               if (Tiles.isSolidCave(Tiles.decodeType(eastTile)) && Tiles.isSolidCave(Tiles.decodeType(southTile))) {
                  BlockingResult result = new BlockingResult();
                  PathTile blocker = new PathTile(Zones.safeTileX(startx + 1), Zones.safeTileY(starty), eastTile, false, -1);
                  result.addBlocker(blocker, blocker.getCenterPoint(), 100.0F);
                  PathTile blocker2 = new PathTile(Zones.safeTileX(startx), Zones.safeTileY(starty + 1), southTile, false, -1);
                  result.addBlocker(blocker2, blocker2.getCenterPoint(), 100.0F);
                  return result;
               }

               int se = Server.caveMesh.getTile(Zones.safeTileX(startx + 1), Zones.safeTileY(starty + 1));
               if (Tiles.isSolidCave(Tiles.decodeType(se))) {
                  BlockingResult result = new BlockingResult();
                  PathTile blocker = new PathTile(Zones.safeTileX(startx + 1), Zones.safeTileY(starty + 1), se, false, -1);
                  result.addBlocker(blocker, blocker.getCenterPoint(), 100.0F);
                  return result;
               }
            }
         }
      }

      return null;
   }

   public static final BlockingResult isStraightRockBetween(Creature creature, int startx, int starty, int endx, int endy) {
      if (startx == endx || endy == starty) {
         if (endx < startx) {
            if (endy == starty) {
               int tile = Server.caveMesh.getTile(Zones.safeTileX(startx - 1), Zones.safeTileY(starty));
               if (Tiles.isSolidCave(Tiles.decodeType(tile))) {
                  BlockingResult result = new BlockingResult();
                  PathTile blocker = new PathTile(Zones.safeTileX(startx - 1), Zones.safeTileY(starty), tile, false, -1);
                  result.addBlocker(blocker, blocker.getCenterPoint(), 100.0F);
                  return result;
               }
            }
         } else if (endx > startx) {
            if (endy == starty) {
               int tile = Server.caveMesh.getTile(Zones.safeTileX(startx + 1), Zones.safeTileY(starty));
               if (Tiles.isSolidCave(Tiles.decodeType(tile))) {
                  BlockingResult result = new BlockingResult();
                  PathTile blocker = new PathTile(Zones.safeTileX(startx + 1), Zones.safeTileY(starty), tile, false, -1);
                  result.addBlocker(blocker, blocker.getCenterPoint(), 100.0F);
                  return result;
               }
            }
         } else if (endy > starty) {
            if (endx == startx) {
               int tile = Server.caveMesh.getTile(Zones.safeTileX(startx), Zones.safeTileY(starty + 1));
               if (Tiles.isSolidCave(Tiles.decodeType(tile))) {
                  BlockingResult result = new BlockingResult();
                  PathTile blocker = new PathTile(Zones.safeTileX(startx), Zones.safeTileY(starty + 1), tile, false, -1);
                  result.addBlocker(blocker, blocker.getCenterPoint(), 100.0F);
                  return result;
               }
            }
         } else if (endy < starty && endx == startx) {
            int tile = Server.caveMesh.getTile(Zones.safeTileX(startx), Zones.safeTileY(starty - 1));
            if (Tiles.isSolidCave(Tiles.decodeType(tile))) {
               BlockingResult result = new BlockingResult();
               PathTile blocker = new PathTile(Zones.safeTileX(startx), Zones.safeTileY(starty - 1), tile, false, -1);
               result.addBlocker(blocker, blocker.getCenterPoint(), 100.0F);
               return result;
            }
         }
      }

      return null;
   }
}

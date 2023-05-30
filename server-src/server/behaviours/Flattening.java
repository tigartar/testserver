package com.wurmonline.server.behaviours;

import com.wurmonline.mesh.Tiles;
import com.wurmonline.server.Constants;
import com.wurmonline.server.FailedException;
import com.wurmonline.server.Features;
import com.wurmonline.server.Items;
import com.wurmonline.server.MiscConstants;
import com.wurmonline.server.NoSuchItemException;
import com.wurmonline.server.Players;
import com.wurmonline.server.Server;
import com.wurmonline.server.creatures.Creature;
import com.wurmonline.server.highways.MethodsHighways;
import com.wurmonline.server.items.Item;
import com.wurmonline.server.items.ItemFactory;
import com.wurmonline.server.items.ItemTemplateFactory;
import com.wurmonline.server.items.NoSuchTemplateException;
import com.wurmonline.server.skills.NoSuchSkillException;
import com.wurmonline.server.skills.Skill;
import com.wurmonline.server.skills.Skills;
import com.wurmonline.server.sounds.SoundPlayer;
import com.wurmonline.server.structures.Fence;
import com.wurmonline.server.utils.logging.TileEvent;
import com.wurmonline.server.villages.Village;
import com.wurmonline.server.zones.FocusZone;
import com.wurmonline.server.zones.NoSuchZoneException;
import com.wurmonline.server.zones.VolaTile;
import com.wurmonline.server.zones.Zone;
import com.wurmonline.server.zones.Zones;
import com.wurmonline.shared.constants.SoundNames;
import java.util.logging.Level;
import java.util.logging.Logger;

final class Flattening implements MiscConstants, SoundNames {
   private static final Logger logger = Logger.getLogger(Flattening.class.getName());
   private static int[][] flattenTiles = new int[4][4];
   private static int[][] rockTiles = new int[4][4];
   private static boolean[][] immutableTiles = new boolean[4][4];
   private static boolean[][] changedTiles = new boolean[4][4];
   private static int flattenSkill = 0;
   private static double skill = 0.0;
   private static int flattenRock = 0;
   private static int flattenDone = 0;
   private static int flattenImmutable = 0;
   private static int flattenSlope = 0;
   private static int needsDirt = 0;
   private static final float DIGGING_SKILL_MULT = 3.0F;
   private static final float FLATTENING_MAX_DEPTH = -7.0F;
   private static short newFHeight = 0;
   private static boolean raising = false;

   private Flattening() {
   }

   private static final void fillFlattenTiles(Creature performer, int tilex, int tiley) {
      for(int x = -1; x < 3; ++x) {
         for(int y = -1; y < 3; ++y) {
            try {
               flattenTiles[x + 1][y + 1] = Server.surfaceMesh.getTile(tilex + x, tiley + y);
               rockTiles[x + 1][y + 1] = Tiles.decodeHeight(Server.rockMesh.getTile(tilex + x, tiley + y));
               immutableTiles[x + 1][y + 1] = isTileUntouchable(performer, tilex + x, tiley + y);
            } catch (Exception var6) {
               immutableTiles[x + 1][y + 1] = true;
               flattenTiles[x + 1][y + 1] = -100;
            }
         }
      }
   }

   private static final boolean isTileUntouchable(Creature performer, int tilex, int tiley) {
      for(int x = 0; x >= -1; --x) {
         for(int y = 0; y >= -1; --y) {
            if (Zones.protectedTiles[tilex + x][tiley + y]) {
               return true;
            }

            if (Zones.isWithinDuelRing(tilex, tiley, true) != null) {
               return true;
            }

            if (Features.Feature.BLOCK_HOTA.isEnabled()) {
               for(FocusZone fz : FocusZone.getZonesAt(tilex, tiley)) {
                  if ((fz.isBattleCamp() || fz.isPvPHota() || fz.isNoBuild()) && fz.covers(tilex, tiley)) {
                     return true;
                  }
               }
            }

            VolaTile vtile = Zones.getOrCreateTile(tilex + x, tiley + y, performer.isOnSurface());
            if (vtile.getStructure() != null && performer.getPower() < 5) {
               return true;
            }

            Village village = vtile.getVillage();
            if (village != null) {
               int tile = Server.surfaceMesh.getTile(tilex + x, tiley + y);
               if (!village.isActionAllowed((short)144, performer, false, tile, 0)) {
                  return true;
               }
            }

            Fence[] fences = vtile.getFencesForLevel(0);
            if (fences.length > 0) {
               if (x == 0 && y == 0) {
                  return true;
               }

               if (x == -1 && y == 0) {
                  for(Fence f : fences) {
                     if (f.isHorizontal()) {
                        return true;
                     }
                  }
               } else if (y == -1 && x == 0) {
                  for(Fence f : fences) {
                     if (!f.isHorizontal()) {
                        return true;
                     }
                  }
               }
            }
         }
      }

      return false;
   }

   static final boolean flatten(Creature performer, Item source, int tile, int tilex, int tiley, float counter, Action act) {
      return flatten(-10L, performer, source, tile, tilex, tiley, 2, 2, 4, counter, act);
   }

   static final boolean flattenTileBorder(
      long borderId, Creature performer, Item source, int tilex, int tiley, Tiles.TileBorderDirection dir, float counter, Action act
   ) {
      int tile = Zones.getTileIntForTile(tilex, tiley, performer.isOnSurface() ? 0 : 1);
      return dir == Tiles.TileBorderDirection.DIR_DOWN
         ? flatten(borderId, performer, source, tile, tilex, tiley, 1, 2, 2, counter, act)
         : flatten(borderId, performer, source, tile, tilex, tiley, 2, 1, 2, counter, act);
   }

   private static final boolean flatten(
      long borderId, Creature performer, Item source, int tile, int tilex, int tiley, int endX, int endY, int numbCorners, float counter, Action act
   ) {
      boolean done = false;
      String verb = act.getActionEntry().getVerbString().toLowerCase();
      String action = act.getActionEntry().getActionString().toLowerCase();
      if (tilex - 2 >= 0 && tilex + 2 <= 1 << Constants.meshSize && tiley - 2 >= 0 && tiley + 2 <= 1 << Constants.meshSize) {
         VolaTile vt = performer.getCurrentTile();
         if (vt != null && vt.getStructure() != null && performer.getPower() < 5) {
            performer.getCommunicator().sendNormalServerMessage("You can not " + action + " from the inside.");
            return true;
         }

         byte type = Tiles.decodeType(tile);
         if (Terraforming.isRockTile(type)) {
            performer.getCommunicator().sendNormalServerMessage("You can not dig in the solid rock.");
            return true;
         }

         if (!performer.isOnSurface()) {
            performer.getCommunicator().sendNormalServerMessage("You can not " + action + " from the inside.");
            return true;
         }

         boolean isTooDeep = isTileTooDeep(tilex, tiley, endX, endY, numbCorners);
         if (act.getNumber() == 532 && !Terraforming.isFlat(performer.getTileX(), performer.getTileY(), performer.isOnSurface(), 0)) {
            performer.getCommunicator()
               .sendNormalServerMessage("You need to be " + (isTooDeep ? "above" : "standing on") + " flat ground to be able to level.");
            return true;
         }

         if (Terraforming.isNonDiggableTile(type)) {
            done = true;
            performer.getCommunicator().sendNormalServerMessage("You can't " + action + " that place, as " + source.getNameWithGenus() + " just won't do.");
         } else {
            boolean insta = source.isWand() && performer.getPower() >= 2;
            if (act.currentSecond() % 10 == 0 && act.getNumber() == 150 || act.currentSecond() % 5 == 0 && act.getNumber() != 150 || insta || counter == 1.0F) {
               flattenSkill = 0;
               flattenImmutable = 0;
               flattenRock = 0;
               flattenDone = 0;
               flattenSlope = 0;
               needsDirt = 0;
               skill = 0.0;
               fillFlattenTiles(performer, tilex, tiley);
               short maxHeight = -32768;
               short maxHeight2 = -32768;
               short minHeight = 32767;

               for(int x = 1; x <= endX; ++x) {
                  for(int y = 1; y <= endY; ++y) {
                     short ht = Tiles.decodeHeight(flattenTiles[x][y]);
                     if (ht > maxHeight) {
                        maxHeight2 = maxHeight;
                        maxHeight = ht;
                     } else if (ht > maxHeight2) {
                        maxHeight2 = ht;
                     }

                     if (ht < minHeight) {
                        minHeight = ht;
                     }

                     if (performer.getStrengthSkill() < 21.0 && Terraforming.isRoad(Tiles.decodeType(flattenTiles[x][y]))) {
                        performer.getCommunicator().sendNormalServerMessage("You need to be stronger in order to " + action + " near roads.");
                        return true;
                     }
                  }
               }

               short lAverageHeight = calcAverageHeight(performer, tile, tilex, tiley, endX, endY, numbCorners, act);
               if (isFlat(performer, lAverageHeight, endX, endY, numbCorners, act)) {
                  performer.getCommunicator().sendNormalServerMessage("Already flat!");
                  checkChangedTiles(tile, endX, endY, performer);
                  return true;
               }

               int ddone = 0;
               Skills skills = performer.getSkills();
               Skill digging = null;
               Skill shovel = null;
               if (!insta) {
                  if (!isTooDeep && !source.isDiggingtool() || isTooDeep && !source.isDredgingTool()) {
                     performer.getCommunicator().sendNormalServerMessage("You can't " + action + " with that.");
                     return true;
                  }

                  try {
                     digging = skills.getSkill(1009);
                  } catch (Exception var32) {
                     digging = skills.learn(1009, 1.0F);
                  }

                  try {
                     shovel = skills.getSkill(source.getPrimarySkill());
                  } catch (Exception var34) {
                     try {
                        shovel = skills.learn(source.getPrimarySkill(), 1.0F);
                     } catch (NoSuchSkillException var33) {
                        if (performer.getPower() <= 0) {
                           logger.log(Level.WARNING, performer.getName() + " trying to " + action + " with an item with no primary skill: " + source.getName());
                        }
                     }
                  }

                  skill = digging.getKnowledge(0.0);
               } else {
                  skill = 99.0;
               }

               if ((type == Tiles.Tile.TILE_CLAY.id || type == Tiles.Tile.TILE_TAR.id || type == Tiles.Tile.TILE_PEAT.id) && skill < 70.0) {
                  performer.getCommunicator().sendNormalServerMessage("You just can not work how to " + action + " here it seems.");
                  return true;
               }

               int tickTimes = 5;
               if (act.getNumber() == 150) {
                  tickTimes = 10;
               }

               if (type == Tiles.Tile.TILE_CLAY.id || type == Tiles.Tile.TILE_TAR.id || type == Tiles.Tile.TILE_PEAT.id) {
                  tickTimes = 30;
               }

               if (counter == 1.0F && !insta) {
                  float t;
                  if (act.getNumber() == 532) {
                     int difmax = Math.abs(maxHeight - lAverageHeight);
                     int difmax2 = Math.abs(maxHeight2 - lAverageHeight);
                     int difmin = Math.abs(minHeight - lAverageHeight);
                     if (minHeight < lAverageHeight) {
                        t = (float)(Math.max(difmin, difmax) * tickTimes * 10);
                     } else {
                        t = (float)((difmax + difmax2) * tickTimes * 10);
                     }
                  } else if (act.getNumber() == 865) {
                     t = (float)((maxHeight - minHeight) * tickTimes * 10);
                  } else if (act.getNumber() == 533) {
                     t = (float)((maxHeight - minHeight) / 4 * tickTimes * 10 + 50);
                  } else {
                     t = (float)((maxHeight - minHeight) / numbCorners * tickTimes * 10 + 100);
                  }

                  if (t > 65535.0F) {
                     t = 65535.0F;
                  }

                  int time = (int)Math.max(50.0F, t);
                  String ctype = "ground";
                  String btype = "the ground";
                  if (act.getNumber() == 533 || act.getNumber() == 865) {
                     ctype = "tile border";
                     btype = "a tile border";
                  }

                  performer.getCommunicator().sendNormalServerMessage("You start to " + action + " the " + ctype + ".");
                  Server.getInstance().broadCastAction(performer.getName() + " starts to " + action + " " + btype + ".", performer, 5);
                  performer.sendActionControl(action, true, time);
                  source.setDamage(source.getDamage() + 5.0E-4F * source.getDamageModifier());
               }

               if (act.currentSecond() % tickTimes == 0 || insta) {
                  if (Zones.protectedTiles[tilex][tiley]) {
                     performer.getCommunicator().sendNormalServerMessage("Your body goes limp and you find no strength to continue here. Weird.");
                     return true;
                  }

                  if (performer.getStatus().getStamina() < 5000) {
                     performer.getCommunicator().sendNormalServerMessage("You must rest.");
                     return true;
                  }

                  if (!insta) {
                     performer.getStatus().modifyStamina(-4000.0F);
                  }

                  String sstring = "sound.work.digging1";
                  int snd = Server.rand.nextInt(3);
                  if (snd == 0) {
                     sstring = "sound.work.digging2";
                  } else if (snd == 1) {
                     sstring = "sound.work.digging3";
                  }

                  SoundPlayer.playSound(sstring, performer, 0.0F);
                  resetChangedTiles();
                  if (act.getNumber() != 533 && act.getNumber() != 865) {
                     for(int xx = 1; xx <= endX; ++xx) {
                        for(int yy = 1; yy <= endY; ++yy) {
                           if (checkFlattenCorner(
                              performer, tilex, tiley, tilex + xx - 1, tiley + yy - 1, xx, yy, (int)Math.max(3.0, skill * 3.0), lAverageHeight, act
                           )) {
                              ++ddone;
                           }
                        }
                     }
                  } else {
                     if (checkBorderCorners(performer, tilex, tiley, tilex, tiley, 1, 1, endX, endY, (int)Math.max(3.0, skill * 3.0), lAverageHeight, act)) {
                        ++ddone;
                     }

                     if (checkBorderCorners(
                        performer, tilex, tiley, tilex + endX - 1, tiley + endY - 1, endX, endY, 1, 1, (int)Math.max(3.0, skill * 3.0), lAverageHeight, act
                     )) {
                        ++ddone;
                     }
                  }

                  checkChangedTiles(tile, tilex, tiley, performer);
                  if (ddone + flattenSkill + flattenImmutable + flattenRock + flattenSlope >= numbCorners) {
                     if (flattenSkill > 0) {
                        performer.getCommunicator().sendNormalServerMessage("Some slope is too steep for your skill level.");
                     }

                     if (flattenImmutable > 0) {
                        performer.getCommunicator().sendNormalServerMessage("Some corners can't be modified.");
                     }

                     if (flattenRock > 0) {
                        performer.getCommunicator().sendNormalServerMessage("You hit the rock in a corner.");
                     }

                     if (flattenDone > 0) {
                        performer.getCommunicator().sendNormalServerMessage("You have already flattened a corner.");
                     }

                     if (flattenSlope > 0) {
                        performer.getCommunicator().sendNormalServerMessage("The highway would become impassable.");
                     }

                     if (ddone == numbCorners) {
                        done = true;
                        if (flattenSkill == 0 && flattenImmutable == 0 && flattenSlope == 0) {
                           checkUseDirt(
                              tilex,
                              tiley,
                              endX,
                              endY,
                              performer,
                              source,
                              (int)Math.max(10.0, skill * 3.0),
                              lAverageHeight,
                              act,
                              insta && source.getAuxData() == 1
                           );
                        }

                        if (needsDirt > 0) {
                           performer.getCommunicator()
                              .sendNormalServerMessage("If you carried some dirt, it would be used to fill the " + needsDirt + " corners that need it.");
                        }

                        lAverageHeight = calcAverageHeight(performer, tile, tilex, tiley, endX, endY, numbCorners, act);
                        if (isFlat(performer, lAverageHeight, endX, endY, numbCorners, act)) {
                           performer.achievement(514);
                        } else if (needsDirt != 0 || act.getNumber() != 532 && act.getNumber() != 865) {
                           performer.getCommunicator().sendNormalServerMessage("You finish " + verb + ".");
                        } else {
                           done = false;
                        }
                     }

                     checkChangedTiles(tile, tilex, tiley, performer);
                     if ((act.getNumber() == 533 || act.getNumber() == 865) && performer.getVisionArea() != null && borderId != -10L) {
                        performer.getVisionArea().broadCastUpdateSelectBar(borderId);
                     }

                     if (done || flattenSkill + flattenImmutable + flattenRock + flattenSlope > 0) {
                        return true;
                     }
                  }

                  if (performer.getStatus().getStamina() < 5000) {
                     performer.getCommunicator().sendNormalServerMessage("You must rest.");
                     return true;
                  }

                  if (!insta) {
                     source.setDamage(source.getDamage() + 5.0E-4F * source.getDamageModifier());
                     float skilltimes = 10.0F;
                     if (act.getNumber() == 533) {
                        skilltimes = 5.0F;
                     }

                     if (act.getNumber() == 865) {
                        skilltimes = 2.5F;
                     }

                     if (act.getNumber() == 532) {
                        skilltimes = 1.5F;
                     }

                     if (shovel != null) {
                        shovel.skillCheck(30.0, source, 0.0, false, skilltimes);
                     }

                     digging.skillCheck(30.0, source, 0.0, false, skilltimes);
                  }
               }
            }
         }
      } else {
         performer.getCommunicator().sendNormalServerMessage("The water is too deep to " + action + ".");
         done = true;
      }

      return done;
   }

   public static void checkChangedTiles(int tile, int tilex, int tiley, Creature performer) {
      for(int x = 0; x < 4; ++x) {
         for(int y = 0; y < 4; ++y) {
            int modx = x - 1;
            int mody = y - 1;
            if (x < 3 && y < 3 && shouldBeRock(x, y)) {
               changedTiles[x][y] = true;
               flattenTiles[x][y] = Tiles.encode(Tiles.decodeHeight(flattenTiles[x][y]), Tiles.Tile.TILE_ROCK.id, (byte)0);
            }

            if (changedTiles[x][y]) {
               changedTiles[x][y] = false;
               Server.surfaceMesh.setTile(tilex + modx, tiley + mody, flattenTiles[x][y]);
               Server.setBotanizable(tilex + modx, tiley + mody, false);
               Server.setForagable(tilex + modx, tiley + mody, false);
               Server.isDirtHeightLower(tilex + modx, tiley + mody, Tiles.decodeHeight(flattenTiles[x][y]));

               try {
                  Zone toCheckForChange = Zones.getZone(tilex + modx, tiley + mody, performer.isOnSurface());
                  toCheckForChange.changeTile(tilex + modx, tiley + mody);
               } catch (NoSuchZoneException var12) {
                  logger.log(Level.INFO, "no such zone?: " + (tilex + modx) + ", " + (tiley + mody), (Throwable)var12);
               }

               performer.getMovementScheme().touchFreeMoveCounter();
               Players.getInstance().sendChangedTile(tilex + modx, tiley + mody, performer.isOnSurface(), true);
            }

            int lNewTile = Server.surfaceMesh.getTile(tilex + modx, tiley + mody);
            byte type = Tiles.decodeType(lNewTile);
            Tiles.Tile theTile = Tiles.getTile(type);
            if (theTile.isTree()) {
               byte data = Tiles.decodeData(lNewTile);
               Zones.reposWildHive(tilex + modx, tiley + mody, theTile, data);
            }
         }
      }
   }

   private static final void resetChangedTiles() {
      for(int x = 0; x < 4; ++x) {
         for(int y = 0; y < 4; ++y) {
            changedTiles[x][y] = false;
         }
      }
   }

   private static short calcAverageHeight(Creature performer, int tile, int tilex, int tiley, int endX, int endY, int numbCorners, Action act) {
      float lAverageHeight = 0.0F;
      if (act.getNumber() == 532) {
         int mytile = Server.surfaceMesh.getTile(performer.getTileX(), performer.getTileY());
         lAverageHeight = (float)Tiles.decodeHeight(mytile);
      } else if (act.getNumber() == 865) {
         float distX = performer.getPosX() - (float)(tilex << 2);
         float distY = performer.getPosY() - (float)(tiley << 2);
         double dist = Math.sqrt((double)(distX * distX + distY * distY));
         distX = performer.getPosX() - (float)(tilex + endX - 1 << 2);
         distY = performer.getPosY() - (float)(tiley + endY - 1 << 2);
         double dist2 = Math.sqrt((double)(distX * distX + distY * distY));
         if (dist < dist2) {
            lAverageHeight = (float)Tiles.decodeHeight(flattenTiles[1][1]);
         } else {
            lAverageHeight = (float)Tiles.decodeHeight(flattenTiles[endX][endY]);
         }
      } else {
         for(int x = 1; x <= endX; ++x) {
            for(int y = 1; y <= endY; ++y) {
               lAverageHeight += (float)Tiles.decodeHeight(flattenTiles[x][y]);
            }
         }

         lAverageHeight = lAverageHeight / (float)numbCorners + 1.0F / (float)numbCorners;
      }

      return (short)((int)lAverageHeight);
   }

   private static boolean isFlat(Creature performer, short requiredHeight, int endX, int endY, int numbCorners, Action act) {
      int ddone = 0;

      for(int x = 1; x <= endX; ++x) {
         for(int y = 1; y <= endY; ++y) {
            if (Tiles.decodeHeight(flattenTiles[x][y]) == requiredHeight) {
               ++ddone;
            }
         }
      }

      if (ddone != numbCorners) {
         return false;
      } else {
         String ctype = "ground";
         if (act.getNumber() == 533 || act.getNumber() == 865) {
            ctype = "tile border";
         }

         performer.getCommunicator().sendNormalServerMessage("The " + ctype + " is flat here.");
         return true;
      }
   }

   private static final boolean checkFlattenCorner(
      Creature performer, int initx, int inity, int tilex, int tiley, int x, int y, int maxDiff, int preferredHeight, Action act
   ) {
      boolean raise = false;
      if (Tiles.decodeHeight(flattenTiles[x][y]) < preferredHeight) {
         raise = true;
      } else if (Tiles.decodeHeight(flattenTiles[x][y]) == preferredHeight) {
         return true;
      }

      if (raise && !mayRaiseCorner(initx, inity, flattenTiles[x][y], x, y, maxDiff)) {
         return true;
      } else if (!raise && !mayLowerCorner(initx, inity, flattenTiles[x][y], rockTiles[x][y], x, y, maxDiff)) {
         return true;
      } else if (x == 1 && y == 1) {
         if (changeCorner(performer, tilex, tiley, x, y, x + 1, y, maxDiff, preferredHeight, raise, act)) {
            if (changeCorner(performer, tilex, tiley, x, y, x, y + 1, maxDiff, preferredHeight, raise, act)) {
               return changeCorner(performer, tilex, tiley, x, y, x + 1, y + 1, maxDiff, preferredHeight, raise, act)
                  ? true
                  : isCornerDone(x, y, preferredHeight);
            } else {
               return isCornerDone(x, y, preferredHeight);
            }
         } else {
            return isCornerDone(x, y, preferredHeight);
         }
      } else if (x == 2 && y == 1) {
         if (changeCorner(performer, tilex, tiley, x, y, x - 1, y, maxDiff, preferredHeight, raise, act)) {
            if (changeCorner(performer, tilex, tiley, x, y, x, y + 1, maxDiff, preferredHeight, raise, act)) {
               return changeCorner(performer, tilex, tiley, x, y, x - 1, y + 1, maxDiff, preferredHeight, raise, act)
                  ? true
                  : isCornerDone(x, y, preferredHeight);
            } else {
               return isCornerDone(x, y, preferredHeight);
            }
         } else {
            return isCornerDone(x, y, preferredHeight);
         }
      } else if (x == 1 && y == 2) {
         if (changeCorner(performer, tilex, tiley, x, y, x + 1, y, maxDiff, preferredHeight, raise, act)) {
            if (changeCorner(performer, tilex, tiley, x, y, x, y - 1, maxDiff, preferredHeight, raise, act)) {
               return changeCorner(performer, tilex, tiley, x, y, x + 1, y - 1, maxDiff, preferredHeight, raise, act)
                  ? true
                  : isCornerDone(x, y, preferredHeight);
            } else {
               return isCornerDone(x, y, preferredHeight);
            }
         } else {
            return isCornerDone(x, y, preferredHeight);
         }
      } else if (x == 2 && y == 2) {
         if (changeCorner(performer, tilex, tiley, x, y, x - 1, y, maxDiff, preferredHeight, raise, act)) {
            if (changeCorner(performer, tilex, tiley, x, y, x, y - 1, maxDiff, preferredHeight, raise, act)) {
               return changeCorner(performer, tilex, tiley, x, y, x - 1, y - 1, maxDiff, preferredHeight, raise, act)
                  ? true
                  : isCornerDone(x, y, preferredHeight);
            } else {
               return isCornerDone(x, y, preferredHeight);
            }
         } else {
            return isCornerDone(x, y, preferredHeight);
         }
      } else {
         return true;
      }
   }

   private static final boolean checkBorderCorners(
      Creature performer, int initx, int inity, int tilex, int tiley, int x1, int y1, int x2, int y2, int maxDiff, int preferredHeight, Action act
   ) {
      boolean raise = false;
      if (Tiles.decodeHeight(flattenTiles[x1][y1]) < preferredHeight) {
         raise = true;
      } else if (Tiles.decodeHeight(flattenTiles[x1][y1]) == preferredHeight) {
         return true;
      }

      if (raise && !mayRaiseCorner(initx, inity, flattenTiles[x1][y1], x1, y1, maxDiff)) {
         return true;
      } else if (!raise && !mayLowerCorner(initx, inity, flattenTiles[x1][y1], rockTiles[x1][y1], x1, y1, maxDiff)) {
         return true;
      } else {
         return changeCorner(performer, tilex, tiley, x1, y1, x2, y2, maxDiff, preferredHeight, raise, act) ? true : isCornerDone(x1, y1, preferredHeight);
      }
   }

   private static final boolean changeCorner(
      Creature performer, int tilex, int tiley, int x, int y, int targx, int targy, int maxDiff, int preferredHeight, boolean raise, Action act
   ) {
      if (raise) {
         if (Tiles.decodeHeight(flattenTiles[targx][targy]) > preferredHeight
            && mayLowerCorner(tilex, tiley, flattenTiles[targx][targy], rockTiles[targx][targy], targx, targy, maxDiff)
            && !changeFlattenCorner(performer, targx, targy, maxDiff, preferredHeight, false, act)) {
            return changeFlattenCorner(performer, x, y, maxDiff, preferredHeight, true, act);
         }
      } else if (Tiles.decodeHeight(flattenTiles[targx][targy]) < preferredHeight
         && mayRaiseCorner(tilex, tiley, flattenTiles[targx][targy], targx, targy, maxDiff)
         && !changeFlattenCorner(performer, targx, targy, maxDiff, preferredHeight, true, act)) {
         return changeFlattenCorner(performer, x, y, maxDiff, preferredHeight, false, act);
      }

      return true;
   }

   private static final boolean mayRaiseCorner(int tilex, int tiley, int tile, int x, int y, int maxDiff) {
      byte newType = Tiles.decodeType(tile);
      if (newType != Tiles.Tile.TILE_HOLE.id && !Tiles.isMineDoor(newType)) {
         if (immutableTiles[x][y]) {
            ++flattenImmutable;
            return false;
         } else if (Terraforming.isImmutableTile(Tiles.decodeType(tile)) || Tiles.decodeType(tile) == Tiles.Tile.TILE_HOLE.id) {
            ++flattenImmutable;
            return false;
         } else if (Terraforming.isImmutableTile(Tiles.decodeType(flattenTiles[x][y - 1]))
            || Tiles.decodeType(flattenTiles[x][y - 1]) == Tiles.Tile.TILE_HOLE.id) {
            ++flattenImmutable;
            return false;
         } else if (Terraforming.isImmutableTile(Tiles.decodeType(flattenTiles[x - 1][y]))
            || Tiles.decodeType(flattenTiles[x - 1][y]) == Tiles.Tile.TILE_HOLE.id) {
            ++flattenImmutable;
            return false;
         } else if (!Terraforming.isImmutableTile(Tiles.decodeType(flattenTiles[x - 1][y - 1]))
            && Tiles.decodeType(flattenTiles[x - 1][y - 1]) != Tiles.Tile.TILE_HOLE.id) {
            int htN = Tiles.decodeHeight(flattenTiles[x][y - 1]);
            int htE = Tiles.decodeHeight(flattenTiles[x + 1][y]);
            int htS = Tiles.decodeHeight(flattenTiles[x][y + 1]);
            int htW = Tiles.decodeHeight(flattenTiles[x - 1][y]);
            int ht = Tiles.decodeHeight(flattenTiles[x][y]);
            if (Math.abs(ht - htS) > maxDiff) {
               ++flattenSkill;
               return false;
            } else if (Math.abs(ht - htN) > maxDiff) {
               ++flattenSkill;
               return false;
            } else if (Math.abs(ht - htE) > maxDiff) {
               ++flattenSkill;
               return false;
            } else if (Math.abs(ht - htW) > maxDiff) {
               ++flattenSkill;
               return false;
            } else {
               int htNE = Tiles.decodeHeight(flattenTiles[x + 1][y - 1]);
               int htSE = Tiles.decodeHeight(flattenTiles[x + 1][y + 1]);
               int htSW = Tiles.decodeHeight(flattenTiles[x - 1][y + 1]);
               int htNW = Tiles.decodeHeight(flattenTiles[x - 1][y - 1]);
               boolean pC = Tiles.isRoadType(flattenTiles[x][y]);
               boolean pN = Tiles.isRoadType(flattenTiles[x][y - 1]);
               boolean pNW = Tiles.isRoadType(flattenTiles[x - 1][y - 1]);
               boolean pW = Tiles.isRoadType(flattenTiles[x - 1][y]);
               boolean hC = pC && MethodsHighways.onHighway(tilex + x - 1, tiley + y - 1, true);
               boolean hN = pN && MethodsHighways.onHighway(tilex + x - 1, tiley + y - 2, true);
               boolean hNW = pNW && MethodsHighways.onHighway(tilex + x - 2, tiley + y - 2, true);
               boolean hW = pW && MethodsHighways.onHighway(tilex + x - 2, tiley + y - 1, true);
               int dS = ht - htS;
               int dN = ht - htN;
               int dE = ht - htE;
               int dW = ht - htW;
               if (Features.Feature.WAGONER.isEnabled()) {
                  boolean wC = hC && MethodsHighways.onWagonerCamp(tilex + x - 1, tiley + y - 1, true);
                  boolean wN = hN && MethodsHighways.onWagonerCamp(tilex + x - 1, tiley + y - 2, true);
                  boolean wNW = hNW && MethodsHighways.onWagonerCamp(tilex + x - 2, tiley + y - 2, true);
                  boolean wW = hW && MethodsHighways.onWagonerCamp(tilex + x - 2, tiley + y - 1, true);
                  if ((wC || wN) && dE >= 0) {
                     ++flattenSlope;
                     return false;
                  } else if ((wC || wW) && dS >= 0) {
                     ++flattenSlope;
                     return false;
                  } else if ((wW || wNW) && dW >= 0) {
                     ++flattenSlope;
                     return false;
                  } else if ((wN || wNW) && dN >= 0) {
                     ++flattenSlope;
                     return false;
                  } else {
                     return true;
                  }
               } else {
                  int dNE = ht - htNE;
                  int dSE = ht - htSE;
                  int dSW = ht - htSW;
                  int dNW = ht - htNW;
                  if ((hC || hN) && dE >= 20) {
                     ++flattenSlope;
                     return false;
                  } else if ((hC || hW) && dS >= 20) {
                     ++flattenSlope;
                     return false;
                  } else if ((hW || hNW) && dW >= 20) {
                     ++flattenSlope;
                     return false;
                  } else if ((hN || hNW) && dN >= 20) {
                     ++flattenSlope;
                     return false;
                  } else if (hC && dSE >= 28) {
                     ++flattenSlope;
                     return false;
                  } else if (hW && dSW >= 28) {
                     ++flattenSlope;
                     return false;
                  } else if (hNW && dNW >= 28) {
                     ++flattenSlope;
                     return false;
                  } else if (hN && dNE >= 28) {
                     ++flattenSlope;
                     return false;
                  } else {
                     return true;
                  }
               }
            }
         } else {
            ++flattenImmutable;
            return false;
         }
      } else {
         ++flattenImmutable;
         return false;
      }
   }

   private static final boolean mayLowerCorner(int tilex, int tiley, int tile, int rocktile, int x, int y, int maxDiff) {
      if (immutableTiles[x][y]) {
         ++flattenImmutable;
         return false;
      } else {
         byte newType = Tiles.decodeType(tile);
         if (newType != Tiles.Tile.TILE_HOLE.id && newType != Tiles.Tile.TILE_CLIFF.id && !Tiles.isMineDoor(newType)) {
            if (Tiles.decodeHeight(tile) <= Tiles.decodeHeight(rocktile)) {
               ++flattenRock;
               return false;
            } else if (Tiles.decodeType(tile) == Tiles.Tile.TILE_HOLE.id) {
               ++flattenImmutable;
               return false;
            } else {
               int htN = Tiles.decodeHeight(flattenTiles[x][y - 1]);
               int htE = Tiles.decodeHeight(flattenTiles[x + 1][y]);
               int htS = Tiles.decodeHeight(flattenTiles[x][y + 1]);
               int htW = Tiles.decodeHeight(flattenTiles[x - 1][y]);
               int ht = Tiles.decodeHeight(flattenTiles[x][y]);
               if (Math.abs(htS - ht) > maxDiff) {
                  ++flattenSkill;
                  return false;
               } else if (Math.abs(htN - ht) > maxDiff) {
                  ++flattenSkill;
                  return false;
               } else if (Math.abs(htE - ht) > maxDiff) {
                  ++flattenSkill;
                  return false;
               } else if (Math.abs(htW - ht) > maxDiff) {
                  ++flattenSkill;
                  return false;
               } else {
                  int htNE = Tiles.decodeHeight(flattenTiles[x + 1][y - 1]);
                  int htSE = Tiles.decodeHeight(flattenTiles[x + 1][y + 1]);
                  int htSW = Tiles.decodeHeight(flattenTiles[x - 1][y + 1]);
                  int htNW = Tiles.decodeHeight(flattenTiles[x - 1][y - 1]);
                  boolean pC = Tiles.isRoadType(flattenTiles[x][y]);
                  boolean pN = Tiles.isRoadType(flattenTiles[x][y - 1]);
                  boolean pNW = Tiles.isRoadType(flattenTiles[x - 1][y - 1]);
                  boolean pW = Tiles.isRoadType(flattenTiles[x - 1][y]);
                  boolean hC = pC && MethodsHighways.onHighway(tilex + x - 1, tiley + y - 1, true);
                  boolean hN = pN && MethodsHighways.onHighway(tilex + x - 1, tiley + y - 2, true);
                  boolean hNW = pNW && MethodsHighways.onHighway(tilex + x - 2, tiley + y - 2, true);
                  boolean hW = pW && MethodsHighways.onHighway(tilex + x - 2, tiley + y - 1, true);
                  int dS = htS - ht;
                  int dN = htN - ht;
                  int dE = htE - ht;
                  int dW = htW - ht;
                  if (Features.Feature.WAGONER.isEnabled()) {
                     boolean wC = hC && MethodsHighways.onWagonerCamp(tilex + x - 1, tiley + y - 1, true);
                     boolean wN = hN && MethodsHighways.onWagonerCamp(tilex + x - 1, tiley + y - 2, true);
                     boolean wNW = hNW && MethodsHighways.onWagonerCamp(tilex + x - 2, tiley + y - 2, true);
                     boolean wW = hW && MethodsHighways.onWagonerCamp(tilex + x - 2, tiley + y - 1, true);
                     if ((wC || wN) && dE >= 0) {
                        ++flattenSlope;
                        return false;
                     } else if ((wC || wW) && dS >= 0) {
                        ++flattenSlope;
                        return false;
                     } else if ((wW || wNW) && dW >= 0) {
                        ++flattenSlope;
                        return false;
                     } else if ((wN || wNW) && dN >= 0) {
                        ++flattenSlope;
                        return false;
                     } else {
                        return true;
                     }
                  } else {
                     int dNE = htNE - ht;
                     int dSE = htSE - ht;
                     int dSW = htSW - ht;
                     int dNW = htNW - ht;
                     if ((hC || hN) && dE >= 20) {
                        ++flattenSlope;
                        return false;
                     } else if ((hC || hW) && dS >= 20) {
                        ++flattenSlope;
                        return false;
                     } else if ((hW || hNW) && dW >= 20) {
                        ++flattenSlope;
                        return false;
                     } else if ((hN || hNW) && dN >= 20) {
                        ++flattenSlope;
                        return false;
                     } else if (hC && dSE >= 28) {
                        ++flattenSlope;
                        return false;
                     } else if (hW && dSW >= 28) {
                        ++flattenSlope;
                        return false;
                     } else if (hNW && dNW >= 28) {
                        ++flattenSlope;
                        return false;
                     } else if (hN && dNE >= 28) {
                        ++flattenSlope;
                        return false;
                     } else {
                        return true;
                     }
                  }
               }
            }
         } else {
            ++flattenImmutable;
            return false;
         }
      }
   }

   public static boolean shouldBeRock(int x, int y) {
      int numberOfCornersAtRockHeight = 0;

      for(int xx = 0; xx <= 1; ++xx) {
         for(int yy = 0; yy <= 1; ++yy) {
            short tileHeight = Tiles.decodeHeight(flattenTiles[x + xx][y + yy]);
            short rockHeight = Tiles.decodeHeight(rockTiles[x + xx][y + yy]);
            if (tileHeight <= rockHeight) {
               ++numberOfCornersAtRockHeight;
            }
         }
      }

      if (numberOfCornersAtRockHeight == 4) {
         byte type = Tiles.decodeType(flattenTiles[x][y]);
         if (!Terraforming.isRockTile(type) && !Terraforming.isImmutableTile(type)) {
            return true;
         }
      }

      return false;
   }

   private static final boolean isCornerDone(int x, int y, int preferredHeight) {
      if (Tiles.decodeHeight(flattenTiles[x][y]) == Tiles.decodeHeight(rockTiles[x][y])) {
         return true;
      } else {
         return Tiles.decodeHeight(flattenTiles[x][y]) == preferredHeight;
      }
   }

   private static final boolean changeFlattenCorner(Creature performer, int x, int y, int maxDiff, int preferredHeight, boolean raise, Action act) {
      int newTile = flattenTiles[x][y];
      byte oldType = Tiles.decodeType(newTile);
      short newHeight = Tiles.decodeHeight(newTile);
      if (raise) {
         newHeight = (short)Math.min(32767, newHeight + 1);
      } else {
         newHeight = (short)Math.max(-32768, newHeight - 1);
      }

      for(int a = 0; a >= -1; --a) {
         for(int b = 0; b >= -1; --b) {
            int modx = x + a;
            int mody = y + b;
            byte newType;
            if (a == 0 && b == 0) {
               newFHeight = newHeight;
               newType = oldType;
               newTile = flattenTiles[x][y];
            } else {
               newFHeight = Tiles.decodeHeight(flattenTiles[modx][mody]);
               newType = Tiles.decodeType(flattenTiles[modx][mody]);
               newTile = flattenTiles[modx][mody];
            }

            if (Terraforming.isImmutableOrRoadTile(newType)) {
               if (a == 0 && b == 0 && !raise) {
                  if (immutableTiles[modx][mody]) {
                     logger.log(
                        Level.WARNING,
                        "Does this really change anything? Changing "
                           + modx
                           + ","
                           + mody
                           + " from "
                           + Tiles.decodeHeight(flattenTiles[modx][mody])
                           + " to "
                           + newFHeight
                           + " at  protected="
                           + immutableTiles[modx][mody]
                           + " xy="
                           + x
                           + ","
                           + y
                           + " ab="
                           + a
                           + ","
                           + b,
                        (Throwable)(new Exception())
                     );
                  }

                  changedTiles[modx][mody] = true;
                  flattenTiles[modx][mody] = Tiles.encode(newFHeight, newType, Tiles.decodeData(newTile));
               } else if (Tiles.isRoadType(newType)) {
                  changedTiles[modx][mody] = true;
                  flattenTiles[modx][mody] = Tiles.encode(newFHeight, newType, Tiles.decodeData(newTile));
               }
            } else {
               if (newType != Tiles.Tile.TILE_SAND.id && oldType != Tiles.Tile.TILE_SAND.id) {
                  newType = Tiles.Tile.TILE_DIRT.id;
               } else if (newType != Tiles.Tile.TILE_DIRT.id) {
                  newType = Tiles.Tile.TILE_SAND.id;
               }

               if (oldType != newType) {
                  TileEvent.log(performer.getTileX(), performer.getTileY(), 0, performer.getWurmId(), act.getNumber());
               }

               flattenTiles[modx][mody] = Tiles.encode(newFHeight, newType, (byte)0);
               changedTiles[modx][mody] = true;
            }
         }
      }

      return false;
   }

   private static final void checkUseDirt(
      int tilex, int tiley, int endX, int endY, Creature performer, Item source, int maxdiff, int preferredHeight, Action act, boolean quickLevel
   ) {
      int higherFlatten = 0;
      int lowerFlatten = 0;

      for(int x = 1; x <= endX; ++x) {
         for(int y = 1; y <= endY; ++y) {
            int diff = Tiles.decodeHeight(flattenTiles[x][y]) - preferredHeight;
            if (diff >= 1) {
               ++lowerFlatten;
            } else if (diff <= -1) {
               ++higherFlatten;
            }
         }
      }

      raising = false;
      if (lowerFlatten >= 2 && act.getNumber() == 150 || lowerFlatten >= 1 && act.getNumber() == 533) {
         if (performer.getCarriedItem(26) != null || performer.getCarriedItem(298) != null) {
            ++preferredHeight;
            raising = true;
         }
      } else if (higherFlatten > 0) {
         raising = true;
      }

      if (raising) {
         for(int x = 1; x <= endX; ++x) {
            for(int y = 1; y <= endY; ++y) {
               int diff = preferredHeight - Tiles.decodeHeight(flattenTiles[x][y]);
               if (diff >= 1 && mayRaiseCorner(tilex, tiley, flattenTiles[x][y], x, y, maxdiff)) {
                  useDirt(performer, x, y, maxdiff, preferredHeight, quickLevel, act);
               }
            }
         }

         if (needsDirt == 3 && act.getNumber() != 532) {
            int dirtX = -1;
            int dirtY = -1;

            for(int x = 1; x <= endX; ++x) {
               for(int y = 1; y <= endY; ++y) {
                  int diff = Tiles.decodeHeight(flattenTiles[x][y]) - preferredHeight + 1;
                  if (diff >= 1 && mayLowerCorner(tilex, tiley, flattenTiles[x][y], rockTiles[x][y], x, y, maxdiff)) {
                     if (dirtX == -1) {
                        dirtX = x;
                        dirtY = y;
                     } else if (Server.rand.nextInt(2) == 1) {
                        dirtX = x;
                        dirtY = y;
                     }
                  }
               }
            }

            if (dirtX > -1) {
               getDirt(performer, source, dirtX, dirtY, maxdiff, preferredHeight, quickLevel, act);
               if (needsDirt == 3) {
                  needsDirt = 0;
               }
            }
         }
      } else if (act.getNumber() == 532) {
         int dirtX = -1;
         int dirtY = -1;

         for(int x = 1; x <= endX; ++x) {
            for(int y = 1; y <= endY; ++y) {
               int diff = Tiles.decodeHeight(flattenTiles[x][y]) - preferredHeight;
               if (diff >= 1 && mayLowerCorner(tilex, tiley, flattenTiles[x][y], rockTiles[x][y], x, y, maxdiff)) {
                  if (dirtX == -1) {
                     dirtX = x;
                     dirtY = y;
                  } else if (Server.rand.nextInt(2) == 1) {
                     dirtX = x;
                     dirtY = y;
                  }
               }
            }
         }

         if (dirtX > -1) {
            getDirt(performer, source, dirtX, dirtY, maxdiff, preferredHeight, quickLevel, act);
         }
      } else {
         for(int x = 1; x <= endX; ++x) {
            for(int y = 1; y <= endY; ++y) {
               int diff = Tiles.decodeHeight(flattenTiles[x][y]) - preferredHeight;
               if (diff >= 1 && mayLowerCorner(tilex, tiley, flattenTiles[x][y], rockTiles[x][y], x, y, maxdiff)) {
                  getDirt(performer, source, x, y, maxdiff, preferredHeight, quickLevel, act);
               }
            }
         }
      }
   }

   private static final void useDirt(Creature performer, int x, int y, int maxDiff, int preferredHeight, boolean quickLevel, Action act) {
      Item dirt = performer.getCarriedItem(26);
      if (dirt == null) {
         dirt = performer.getCarriedItem(298);
      }

      if (dirt != null) {
         Items.destroyItem(dirt.getWurmId());

         try {
            Item dirtAfter = Items.getItem(dirt.getWurmId());
            if (quickLevel) {
               performer.getCommunicator().sendNormalServerMessage("You have a ghost " + dirt.getActualName() + " in inventory.");
               return;
            }

            performer.getCommunicator().sendNormalServerMessage("One corner could not be raised by a " + dirt.getActualName() + " you are carrying.");
            ++needsDirt;
            return;
         } catch (NoSuchItemException var9) {
            if (!changeFlattenCorner(performer, x, y, maxDiff, preferredHeight, true, act)) {
               performer.getCommunicator().sendNormalServerMessage("You use a " + dirt.getActualName() + " in one corner.");
            }
         }
      } else if (quickLevel) {
         changeFlattenCorner(performer, x, y, maxDiff, preferredHeight, true, act);
      } else {
         ++needsDirt;
      }
   }

   private static final void getDirt(Creature performer, Item source, int x, int y, int maxDiff, int preferredHeight, boolean quickLevel, Action act) {
      try {
         byte type = Tiles.decodeType(flattenTiles[x][y]);
         String dirtType = type == Tiles.Tile.TILE_SAND.id ? "heap of sand" : "dirt pile";
         Item ivehic = null;
         if (performer.getInventory().getNumItemsNotCoins() < 100 && performer.canCarry(ItemTemplateFactory.getInstance().getTemplate(26).getWeightGrams())) {
            try {
               ivehic = Items.getItem(performer.getVehicle());
            } catch (NoSuchItemException var14) {
               ivehic = null;
            }
         }

         if (source.isDredgingTool() && !source.isWand()) {
            int dirtVol = 1000;
            if (ivehic != null && ivehic.getFreeVolume() < dirtVol) {
               if (source.getFreeVolume() < dirtVol) {
                  --needsDirt;
                  performer.getCommunicator().sendNormalServerMessage("The " + ivehic.getName() + " and the " + source.getName() + " are both full.");
                  return;
               }
            } else if (ivehic == null && source.getFreeVolume() < dirtVol) {
               --needsDirt;
               performer.getCommunicator().sendNormalServerMessage("The " + source.getName() + " cannot fit anything more inside of it.");
               return;
            }
         }

         if (performer.getInventory().getNumItemsNotCoins() >= 100 || !performer.canCarry(ItemTemplateFactory.getInstance().getTemplate(26).getWeightGrams())) {
            --needsDirt;
            performer.getCommunicator().sendNormalServerMessage("You are not strong enough to carry one more " + dirtType + ".");
         } else if (!changeFlattenCorner(performer, x, y, maxDiff, preferredHeight, false, act)) {
            int template = 26;
            if (type == Tiles.Tile.TILE_SAND.id) {
               template = 298;
            }

            if (!quickLevel) {
               Item dirt = ItemFactory.createItem(template, Server.rand.nextFloat() * (float)maxDiff / 3.0F, performer.getName());
               if (source.isDredgingTool() && !source.isWand()) {
                  boolean addedToBoat = false;
                  if (performer.getVehicle() != -10L && ivehic != null && ivehic.isBoat() && ivehic.testInsertItem(dirt)) {
                     ivehic.insertItem(dirt);
                     performer.getCommunicator().sendNormalServerMessage("You put the " + dirt.getName() + " in the " + ivehic.getName() + ".");
                     addedToBoat = true;
                  }

                  if (!addedToBoat) {
                     source.insertItem(dirt, true);
                  }
               } else {
                  performer.getInventory().insertItem(dirt, true);
               }

               performer.getCommunicator().sendNormalServerMessage("You assemble some " + (template == 26 ? "dirt" : "sand") + " from a corner.");
            }
         }
      } catch (NoSuchTemplateException var15) {
         logger.log(Level.WARNING, performer.getName() + " No dirt template?", (Throwable)var15);
      } catch (FailedException var16) {
         logger.log(Level.WARNING, performer.getName() + " failed to create dirt?", (Throwable)var16);
      }
   }

   static boolean isTileTooDeep(int tilex, int tiley, int totalx, int totaly, int numbCorners) {
      int belowWater = 0;

      for(int x = tilex; x < tilex + totalx; ++x) {
         for(int y = tiley; y < tiley + totaly; ++y) {
            short ht = Tiles.decodeHeight(Server.surfaceMesh.getTile(x, y));
            if ((float)ht <= -7.0F) {
               ++belowWater;
            }
         }
      }

      return belowWater == numbCorners;
   }
}

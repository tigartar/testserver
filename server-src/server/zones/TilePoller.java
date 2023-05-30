package com.wurmonline.server.zones;

import com.wurmonline.mesh.BushData;
import com.wurmonline.mesh.FieldData;
import com.wurmonline.mesh.FoliageAge;
import com.wurmonline.mesh.GrassData;
import com.wurmonline.mesh.MeshIO;
import com.wurmonline.mesh.Tiles;
import com.wurmonline.mesh.TreeData;
import com.wurmonline.server.Constants;
import com.wurmonline.server.FailedException;
import com.wurmonline.server.Features;
import com.wurmonline.server.GeneralUtilities;
import com.wurmonline.server.Items;
import com.wurmonline.server.MiscConstants;
import com.wurmonline.server.Players;
import com.wurmonline.server.Point4f;
import com.wurmonline.server.Server;
import com.wurmonline.server.Servers;
import com.wurmonline.server.TimeConstants;
import com.wurmonline.server.WurmCalendar;
import com.wurmonline.server.behaviours.Crops;
import com.wurmonline.server.behaviours.MethodsItems;
import com.wurmonline.server.behaviours.Terraforming;
import com.wurmonline.server.creatures.MineDoorPermission;
import com.wurmonline.server.highways.HighwayPos;
import com.wurmonline.server.highways.MethodsHighways;
import com.wurmonline.server.items.Item;
import com.wurmonline.server.items.ItemFactory;
import com.wurmonline.server.items.NoSuchTemplateException;
import com.wurmonline.server.kingdom.Kingdoms;
import com.wurmonline.server.sounds.SoundPlayer;
import com.wurmonline.server.villages.Village;
import com.wurmonline.server.villages.Villages;
import com.wurmonline.shared.constants.SoundNames;
import java.util.Random;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.annotation.Nullable;

public final class TilePoller implements TimeConstants, SoundNames, MiscConstants {
   public static boolean logTilePolling = false;
   private static int MINIMUM_REED_HEIGHT = 0;
   private static int MINIMUM_KELP_HEIGHT = -30;
   private static int MAX_KELP_HEIGHT = -400;
   public static MeshIO currentMesh = null;
   public static int currentPollTile = 0;
   public static int pollModifier = 7;
   private static final Logger logger = Logger.getLogger(TilePoller.class.getName());
   public static boolean pollingSurface = true;
   private static int nthTick;
   private static int currTick;
   public static int rest;
   private static boolean manyPerTick = false;
   private static final long FORAGE_PRIME = 101531L;
   private static final long IRON_PRIME = 103591L;
   private static final long HERB_PRIME = 102563L;
   private static final long INVESTIGATE_PRIME = 104149L;
   private static final long SEARCH_PRIME = 103025L;
   private static int forageChance = 2;
   private static int herbChance = 2;
   private static int investigateChance = 2;
   private static int searchChance = 2;
   private static final Random r = new Random();
   private static final long HIVE_PRIME = 102700L + (long)WurmCalendar.getYear();
   private static final int hiveFactor = 500;
   public static int pollround = 0;
   private static boolean pollEruption = false;
   private static boolean createFlowers = false;
   public static int treeGrowth = 20;
   private static int flowerCounter = 0;
   public static boolean entryServer = false;
   private static final Random kelpRandom = new Random();
   public static int mask = -1;
   public static int sentTilePollMessages = 0;

   private TilePoller() {
   }

   public static void calcRest() {
      int ticksPerPeriod = 3456000;
      int tiles = (1 << Constants.meshSize) * (1 << Constants.meshSize);
      logger.log(Level.INFO, "Current polltile=" + currentPollTile + ", rest=" + rest + " pollmodifier=" + pollModifier + ", pollround=" + pollround);
      if (3456000 >= tiles) {
         nthTick = 3456000 / tiles;
         if (currentPollTile == 0) {
            rest = 3456000 % tiles;
         }
      } else {
         nthTick = tiles / 3456000;
         if (currentPollTile == 0) {
            rest = tiles % 3456000;
         }

         manyPerTick = true;
      }

      logger.log(
         Level.INFO,
         "tiles=" + tiles + ", mask=" + mask + " ticksperday=" + 3456000 + ", Nthick=" + nthTick + ", rest=" + rest + ", manypertick=" + manyPerTick
      );
   }

   public static void pollNext() {
      if (Constants.isGameServer) {
         if (manyPerTick) {
            for(int x = 0; x < nthTick; ++x) {
               pollingSurface = true;
               pollNextTile();
               pollingSurface = false;
               pollNextTile();
               checkPolltile();
            }

            if (rest > 0) {
               for(int x = 0; x < Math.min(rest, nthTick); ++x) {
                  pollingSurface = true;
                  pollNextTile();
                  pollingSurface = false;
                  pollNextTile();
                  checkPolltile();
                  --rest;
                  if (rest == 0) {
                     logger.log(Level.INFO, "...and THERE all rest-tiles are gone.");
                  }
               }
            }
         } else {
            ++currTick;
            if (currTick == nthTick) {
               pollingSurface = true;
               pollNextTile();
               pollingSurface = false;
               pollNextTile();
               checkPolltile();
               currTick = 0;
               if (rest > 0) {
                  for(int x = 0; x < Math.min(rest, nthTick); ++x) {
                     pollingSurface = true;
                     pollNextTile();
                     pollingSurface = false;
                     pollNextTile();
                     checkPolltile();
                     --rest;
                     if (rest == 0) {
                        logger.log(Level.INFO, "...and THERE all rest-tiles are gone.");
                     }
                  }
               }
            }
         }
      }
   }

   private static void pollNextTile() {
      if (pollingSurface) {
         currentMesh = Server.surfaceMesh;
      } else {
         currentMesh = Server.caveMesh;
      }

      if (pollingSurface) {
         try {
            int temptile1 = currentMesh.data[currentPollTile];
            int temptilex = currentPollTile & (1 << Constants.meshSize) - 1;
            int temptiley = currentPollTile >> Constants.meshSize;
            byte tempint1 = Tiles.decodeType(temptile1);
            byte temptile2 = Tiles.decodeData(temptile1);
            checkEffects(temptile1, temptilex, temptiley, tempint1, temptile2);
            VolaTile tempvtile1 = Zones.getTileOrNull(temptilex, temptiley, pollingSurface);
            if (tempvtile1 != null) {
               tempvtile1.pollStructures(System.currentTimeMillis());
            }
         } catch (Exception var6) {
            logger.severe("Indexoutofbounds: data array size: " + currentMesh.getData().length + " polltile: " + currentPollTile);
         }
      } else {
         int temptile1 = currentMesh.data[currentPollTile];
         byte tempbyte1 = Tiles.decodeType(temptile1);
         if (tempbyte1 == Tiles.Tile.TILE_CAVE.id || Tiles.isReinforcedFloor(tempbyte1) || tempbyte1 == Tiles.Tile.TILE_CAVE_EXIT.id) {
            int temptilex = currentPollTile & (1 << Constants.meshSize) - 1;
            int temptiley = currentPollTile >> Constants.meshSize;
            int data = Tiles.decodeData(temptile1) & 255;
            checkCaveDecay(temptile1, temptilex, temptiley, tempbyte1, data);
            VolaTile tempvtile1 = Zones.getTileOrNull(temptilex, temptiley, pollingSurface);
            if (tempvtile1 != null) {
               tempvtile1.pollStructures(System.currentTimeMillis());
            }
         } else if (tempbyte1 == Tiles.Tile.TILE_CAVE_WALL.id) {
            int temptilex = currentPollTile & (1 << Constants.meshSize) - 1;
            int temptiley = currentPollTile >> Constants.meshSize;
            byte state = Zones.getMiningState(temptilex, temptiley);
            if (state == 0) {
               r.setSeed((long)(temptilex + temptiley * Zones.worldTileSizeY) * 102533L);
               if (r.nextInt(100) == 0) {
                  Server.caveMesh
                     .setTile(
                        temptilex, temptiley, Tiles.encode(Tiles.decodeHeight(temptile1), Tiles.Tile.TILE_CAVE_WALL_ROCKSALT.id, Tiles.decodeData(temptile1))
                     );
                  Players.getInstance().sendChangedTile(temptilex, temptiley, false, true);
               }

               r.setSeed((long)(temptilex + temptiley * Zones.worldTileSizeY) * 123307L);
               if (r.nextInt(64) == 0) {
                  Server.caveMesh
                     .setTile(
                        temptilex, temptiley, Tiles.encode(Tiles.decodeHeight(temptile1), Tiles.Tile.TILE_CAVE_WALL_SANDSTONE.id, Tiles.decodeData(temptile1))
                     );
                  Players.getInstance().sendChangedTile(temptilex, temptiley, false, true);
               }
            }
         }
      }
   }

   private static void checkCaveDecay(int tile, int tilex, int tiley, byte type, int _data) {
      if (!Zones.protectedTiles[tilex][tiley]) {
         Village village = Villages.getVillage(tilex, tiley, true);
         if (village == null || !village.isPermanent) {
            HighwayPos highwayPos = MethodsHighways.getHighwayPos(tilex, tiley, false);
            if (highwayPos == null || !MethodsHighways.onHighway(highwayPos)) {
               boolean decay = false;
               if (Tiles.decodeType(tile) == Tiles.Tile.TILE_CAVE_EXIT.id) {
                  if (Server.rand.nextInt(60) == 0 && !Tiles.isMineDoor(Tiles.decodeType(Server.surfaceMesh.getTile(tilex, tiley)))) {
                     decay = true;
                     if (Tiles.decodeType(currentMesh.getTile(tilex, tiley - 1)) != Tiles.Tile.TILE_CAVE_WALL.id
                        && Tiles.decodeType(currentMesh.getTile(tilex, tiley - 1)) != Tiles.Tile.TILE_CAVE.id) {
                        decay = false;
                     }

                     if (decay
                        && Tiles.decodeType(currentMesh.getTile(tilex, tiley + 1)) != Tiles.Tile.TILE_CAVE_WALL.id
                        && Tiles.decodeType(currentMesh.getTile(tilex, tiley + 1)) != Tiles.Tile.TILE_CAVE.id) {
                        decay = false;
                     }

                     if (decay
                        && Tiles.decodeType(currentMesh.getTile(tilex + 1, tiley)) != Tiles.Tile.TILE_CAVE_WALL.id
                        && Tiles.decodeType(currentMesh.getTile(tilex + 1, tiley)) != Tiles.Tile.TILE_CAVE.id) {
                        decay = false;
                     }

                     if (decay
                        && Tiles.decodeType(currentMesh.getTile(tilex - 1, tiley)) != Tiles.Tile.TILE_CAVE_WALL.id
                        && Tiles.decodeType(currentMesh.getTile(tilex - 1, tiley)) != Tiles.Tile.TILE_CAVE.id) {
                        decay = false;
                     }
                  }
               } else {
                  int tempint1 = Server.rand.nextInt(4);
                  int tx = 0;
                  int ty = -1;
                  if (tempint1 == 1) {
                     tx = -1;
                     ty = 0;
                  } else if (tempint1 == 2) {
                     tx = 1;
                     ty = 0;
                  } else if (tempint1 == 3) {
                     tx = 0;
                     ty = 1;
                  }

                  int temptile1 = currentMesh.getTile(tilex + tx, tiley + ty);
                  if (Tiles.decodeType(temptile1) == Tiles.Tile.TILE_CAVE_WALL.id && Server.rand.nextFloat() <= 0.002F) {
                     decay = true;
                  }

                  if (decay && Tiles.isReinforcedFloor(Tiles.decodeType(tile)) && Server.rand.nextInt(10) < 8) {
                     decay = false;
                  }
               }

               if (decay) {
                  for(int x = -1; x <= 1; ++x) {
                     for(int y = -1; y <= 1; ++y) {
                        VolaTile tempvtile2 = Zones.getTileOrNull(tilex + x, tiley + y, false);
                        if (tempvtile2 != null && tempvtile2.getStructure() != null && tempvtile2.getStructure().isFinished()) {
                           return;
                        }
                     }
                  }

                  VolaTile tempvtile1 = Zones.getTileOrNull(tilex, tiley, false);
                  if (tempvtile1 != null) {
                     if (tempvtile1.getCreatures().length > 0) {
                        return;
                     }

                     tempvtile1.destroyEverything();
                  }

                  byte state = Zones.getMiningState(tilex, tiley);
                  if (type == Tiles.Tile.TILE_CAVE_EXIT.id) {
                     Terraforming.setAsRock(tilex, tiley, true);
                     if (state != 0) {
                        Zones.setMiningState(tilex, tiley, (byte)0, false);
                        Zones.deleteMiningTile(tilex, tiley);
                     }

                     if (logger.isLoggable(Level.FINER)) {
                        logger.finer("Caved in EXIT at " + tilex + ", " + tiley);
                     }
                  } else {
                     Terraforming.caveIn(tilex, tiley);
                     if (state != 0) {
                        Zones.setMiningState(tilex, tiley, (byte)0, false);
                        Zones.deleteMiningTile(tilex, tiley);
                     }

                     if (logger.isLoggable(Level.FINER)) {
                        logger.finer("Caved in " + tilex + ", " + tiley);
                     }
                  }
               }
            }
         }
      }
   }

   public static void checkEffects(int tile, int tilex, int tiley, byte type, byte aData) {
      if (logTilePolling && sentTilePollMessages < 10) {
         logger.log(Level.INFO, "Tile Polling is working for " + tilex + "," + tiley);
         ++sentTilePollMessages;
      }

      Tiles.Tile theTile = Tiles.getTile(type);
      if (!WurmCalendar.isSeasonWinter()) {
         Server.setGatherable(tilex, tiley, false);
         HiveZone hz = Zones.getHiveZoneAt(tilex, tiley, true);
         if (hz != null) {
            checkHoneyProduction(hz, tilex, tiley, type, aData, theTile);
         }
      } else {
         if (!containsStructure(tilex, tiley)) {
            Server.setGatherable(tilex, tiley, true);
         }

         Item[] hives = Zones.getActiveDomesticHives(tilex, tiley);
         if (hives != null) {
            for(Item hive : hives) {
               if (hive.hasTwoQueens() && hive.removeQueen() && Servers.isThisATestServer()) {
                  Players.getInstance().sendGmMessage(null, "System", "Debug: Removed queen @ " + tilex + "," + tiley + " Two:" + hive.hasTwoQueens(), false);
               }

               if (Server.rand.nextInt(10) == 0) {
                  Item sugar = hive.getSugar();
                  if (sugar != null) {
                     Items.destroyItem(sugar.getWurmId());
                  } else {
                     Item honey = hive.getHoney();
                     if (honey != null) {
                        if (honey.getWeightGrams() < 1000 && Server.rand.nextInt(10) == 0 && hive.removeQueen() && Servers.isThisATestServer()) {
                           Players.getInstance()
                              .sendGmMessage(null, "System", "Debug: Removed queen @ " + tilex + "," + tiley + " Two:" + hive.hasTwoQueens(), false);
                        }

                        honey.setWeight(Math.max(0, honey.getWeightGrams() - 10), true);
                     } else if (Server.rand.nextInt(3) == 0 && hive.removeQueen() && Servers.isThisATestServer()) {
                        Players.getInstance().sendGmMessage(null, "System", "Debug: Removed queen @ " + tilex + "," + tiley + " No Honey!", false);
                     }
                  }
               }
            }
         }
      }

      if (!Zones.protectedTiles[tilex][tiley]) {
         if (type == Tiles.Tile.TILE_GRASS.id || type == Tiles.Tile.TILE_KELP.id || type == Tiles.Tile.TILE_REED.id) {
            if (Server.rand.nextInt(10) < 1) {
               checkForMycelGrowth(tile, tilex, tiley, type, aData);
            } else if (Tiles.decodeHeight(tile) > 0) {
               checkForSeedGrowth(tile, tilex, tiley);
               checkForGrassGrowth(tile, tilex, tiley, type, aData, true);
            } else {
               checkForGrassGrowth(tile, tilex, tiley, type, aData, true);
            }
         } else if (type == Tiles.Tile.TILE_DIRT.id) {
            if (Server.rand.nextInt(10) < 1) {
               checkForMycelGrowth(tile, tilex, tiley, type, aData);
            } else if (Server.rand.nextInt(5) < 1) {
               checkForGrassSpread(tile, tilex, tiley, type, aData);
            } else if (Server.rand.nextInt(3) < 1) {
               checkGrubGrowth(tile, tilex, tiley);
            }
         } else if (type == Tiles.Tile.TILE_SAND.id) {
            if (Server.rand.nextInt(10) < 1) {
               checkGrubGrowth(tile, tilex, tiley);
            }
         } else if (type == Tiles.Tile.TILE_DIRT_PACKED.id) {
            if (Server.rand.nextInt(20) == 1) {
               checkForGrassSpread(tile, tilex, tiley, type, aData);
            }
         } else if (type == Tiles.Tile.TILE_LAWN.id) {
            if (Server.rand.nextInt(50) == 1) {
               Village v = Villages.getVillage(tilex, tiley, true);
               if (v == null && Server.rand.nextInt(100) > 75) {
                  currentMesh.setTile(tilex, tiley, Tiles.encode(Tiles.decodeHeight(tile), Tiles.Tile.TILE_GRASS.id, (byte)0));
                  Server.modifyFlagsByTileType(tilex, tiley, Tiles.Tile.TILE_GRASS.id);
                  Players.getInstance().sendChangedTile(tilex, tiley, pollingSurface, false);
               }
            } else if (Server.rand.nextInt(10) < 1) {
               checkForMycelGrowth(tile, tilex, tiley, type, aData);
            }
         } else if (type == Tiles.Tile.TILE_STEPPE.id) {
            if (Server.rand.nextInt(10) < 1) {
               checkForMycelGrowth(tile, tilex, tiley, type, aData);
            } else if (Tiles.decodeHeight(tile) > 0) {
               checkForSeedGrowth(tile, tilex, tiley);
            }
         } else if (type == Tiles.Tile.TILE_MOSS.id) {
            if (Server.rand.nextInt(10) < 1) {
               checkForMycelGrowth(tile, tilex, tiley, type, aData);
            } else if (Server.rand.nextInt(3) == 1 && Tiles.decodeHeight(tile) > 0) {
               checkForSeedGrowth(tile, tilex, tiley);
            }
         } else if (type == Tiles.Tile.TILE_PEAT.id) {
            if (Server.rand.nextInt(5) == 1 && Tiles.decodeHeight(tile) > 0) {
               checkForSeedGrowth(tile, tilex, tiley);
            }
         } else if (type == Tiles.Tile.TILE_TUNDRA.id) {
            if (Tiles.decodeHeight(tile) > 0) {
               if (Server.rand.nextInt(7) == 1) {
                  checkForSeedGrowth(tile, tilex, tiley);
               }

               if (Server.rand.nextInt(100) == 1) {
                  checkForLingonberryStart(tile, tilex, tiley);
               }
            }
         } else if (type == Tiles.Tile.TILE_MARSH.id) {
            if (Server.rand.nextInt(9) == 1 && Tiles.decodeHeight(tile) > -20) {
               checkForSeedGrowth(tile, tilex, tiley);
            }
         } else if (theTile.isNormalTree() || theTile.isEnchantedTree()) {
            checkForTreeGrowth(tile, tilex, tiley, type, aData);
            if (Server.rand.nextInt(10) < 1) {
               checkForMycelGrowth(tile, tilex, tiley, type, aData);
            } else {
               checkForSeedGrowth(tile, tilex, tiley);
               checkForTreeGrassGrowth(tile, tilex, tiley, type, aData);
            }

            if (Server.isCheckHive(tilex, tiley) && WurmCalendar.isSeasonAutumn() && Server.rand.nextInt(30) == 0) {
               Item wildHive = Zones.getWildHive(tilex, tiley);
               if (wildHive != null) {
                  Item honey = wildHive.getHoney();
                  if (honey == null) {
                     Server.setCheckHive(tilex, tiley, false);
                     if (Zones.removeWildHive(tilex, tiley) && Servers.isThisATestServer()) {
                        Players.getInstance()
                           .sendGmMessage(null, "System", "Debug: Removed wild hive due to no food in autumn @ " + tilex + "," + tiley, false);
                     }
                  }
               }
            }

            if (Server.isCheckHive(tilex, tiley) && WurmCalendar.isWinter()) {
               Server.setCheckHive(tilex, tiley, false);
               if (Zones.removeWildHive(tilex, tiley) && Servers.isThisATestServer()) {
                  Players.getInstance().sendGmMessage(null, "System", "Debug: Removed wild hive due to winter @ " + tilex + "," + tiley, false);
               }
            }

            if (!Server.isCheckHive(tilex, tiley) && WurmCalendar.isSpring() && Server.rand.nextInt(5) == 0) {
               Server.setCheckHive(tilex, tiley, true);
               addWildBeeHives(tilex, tiley, theTile, aData);
            }

            if (!Server.isCheckHive(tilex, tiley) && WurmCalendar.isSummer() && Server.rand.nextInt(15) == 0) {
               Server.setCheckHive(tilex, tiley, true);
               addWildBeeHives(tilex, tiley, theTile, aData);
            }
         } else if (theTile.isNormalBush() || theTile.isEnchantedBush()) {
            checkForTreeGrowth(tile, tilex, tiley, type, aData);
            if (Server.rand.nextInt(10) < 1) {
               checkForMycelGrowth(tile, tilex, tiley, type, aData);
            } else {
               checkForSeedGrowth(tile, tilex, tiley);
               checkForTreeGrassGrowth(tile, tilex, tiley, type, aData);
            }
         } else if (type != Tiles.Tile.TILE_FIELD.id && type != Tiles.Tile.TILE_FIELD2.id) {
            if (type == Tiles.Tile.TILE_MYCELIUM.id) {
               if (!Servers.isThisAPvpServer()) {
                  Server.setSurfaceTile(tilex, tiley, Tiles.decodeHeight(tile), Tiles.Tile.TILE_GRASS.id, aData);
                  Players.getInstance().sendChangedTile(tilex, tiley, true, false);
               } else if (Server.rand.nextInt(3) < 1) {
                  checkForGrassSpread(tile, tilex, tiley, type, aData);
                  checkForSeedGrowth(tile, tilex, tiley);
               } else {
                  checkForGrassGrowth(tile, tilex, tiley, type, aData, false);
                  checkGrubGrowth(tile, tilex, tiley);
               }
            } else if (type == Tiles.Tile.TILE_MYCELIUM_LAWN.id) {
               if (!Servers.isThisAPvpServer()) {
                  Server.setSurfaceTile(tilex, tiley, Tiles.decodeHeight(tile), Tiles.Tile.TILE_LAWN.id, aData);
                  Players.getInstance().sendChangedTile(tilex, tiley, true, false);
               } else if (Server.rand.nextInt(25) == 1) {
                  Village v = Villages.getVillage(tilex, tiley, pollingSurface);
                  if (v == null && Server.rand.nextInt(100) > 75) {
                     currentMesh.setTile(tilex, tiley, Tiles.encode(Tiles.decodeHeight(tile), Tiles.Tile.TILE_MYCELIUM.id, (byte)0));
                     Server.modifyFlagsByTileType(tilex, tiley, Tiles.Tile.TILE_MYCELIUM.id);
                     Players.getInstance().sendChangedTile(tilex, tiley, pollingSurface, false);
                  }
               } else if (Server.rand.nextInt(10) < 1) {
                  checkForGrassSpread(tile, tilex, tiley, type, aData);
               }
            } else if (!theTile.isMyceliumTree() && !theTile.isMyceliumBush()) {
               if (type == Tiles.Tile.TILE_LAVA.id) {
                  checkForLavaFlow(tile, tilex, tiley, type, aData);
               } else if (type == Tiles.Tile.TILE_PLANKS.id) {
                  if (!Zones.walkedTiles[tilex][tiley]) {
                     if (aData == 0 || Server.rand.nextInt(10) == 0) {
                        checkForDecayToDirt(tile, tilex, tiley, type, aData);
                     }
                  } else {
                     Zones.walkedTiles[tilex][tiley] = false;
                  }
               } else if (pollingSurface && (type == Tiles.Tile.TILE_ROCK.id || type == Tiles.Tile.TILE_CLIFF.id)) {
                  if (pollEruption) {
                     checkForEruption(tile, tilex, tiley, type, aData);
                  } else {
                     checkCreateIronRock(tilex, tiley);
                  }
               } else if (Tiles.isMineDoor(type)) {
                  decayMineDoor(tile, tilex, tiley);
               }
            } else if (!Servers.isThisAPvpServer()) {
               byte newType = (byte)Tiles.toNormal(type);
               currentMesh.setTile(tilex, tiley, Tiles.encode(Tiles.decodeHeight(tile), newType, aData));
               Players.getInstance().sendChangedTile(tilex, tiley, pollingSurface, false);
            } else {
               checkForTreeGrowth(tile, tilex, tiley, type, aData);
               if (Server.rand.nextInt(3) < 1) {
                  checkForGrassSpread(tile, tilex, tiley, type, aData);
               } else {
                  checkForGrassGrowth(tile, tilex, tiley, type, aData, false);
                  checkForSeedGrowth(tile, tilex, tiley);
               }
            }
         } else if (!Features.Feature.CROP_POLLER.isEnabled() && !Zones.protectedTiles[tilex][tiley]) {
            checkForFarmGrowth(tile, tilex, tiley, type, aData);
         }

         if (Tiles.isRoadType(type)
            || Tiles.isEnchanted(type)
            || type == Tiles.Tile.TILE_DIRT_PACKED.id
            || type == Tiles.Tile.TILE_DIRT.id
            || type == Tiles.Tile.TILE_LAWN.id
            || type == Tiles.Tile.TILE_MYCELIUM_LAWN.id) {
            checkInvestigateGrowth(tile, tilex, tiley);
         }
      }
   }

   private static void decayMineDoor(int tile, int tilex, int tiley) {
      if (pollingSurface && Server.rand.nextInt(3) == 0) {
         Village v = Villages.getVillage(tilex, tiley, true);
         if (v == null || v.lessThanWeekLeft()) {
            int currQl = Server.getWorldResource(tilex, tiley);
            currQl = Math.max(0, currQl - 100);
            Server.setWorldResource(tilex, tiley, currQl);
            if (currQl == 0) {
               Server.setSurfaceTile(tilex, tiley, Tiles.decodeHeight(tile), Tiles.Tile.TILE_HOLE.id, (byte)0);
               Players.getInstance().sendChangedTile(tilex, tiley, pollingSurface, true);
               MineDoorPermission perm = MineDoorPermission.getPermission(tilex, tiley);
               if (perm != null) {
                  MineDoorPermission.removePermission(perm);
               }

               MineDoorPermission.deleteMineDoor(tilex, tiley);
               Server.getInstance().broadCastMessage("A mine door crumbles and falls down with a crash.", tilex, tiley, true, 5);
            }
         }
      }
   }

   private static void checkPolltile() {
      if (currentPollTile == 0) {
         calcRest();
         WurmCalendar.checkSpring();
         createFlowers = Server.rand.nextInt(5) == 0;
         ++pollround;
         pollModifier = (Server.rand.nextInt(Math.min(30000, currentMesh.data.length)) + 1) * 2 - 1;
         logger.log(Level.INFO, "New pollModifier: " + pollModifier + " eruptions=" + pollEruption);
      }

      currentPollTile = currentPollTile + pollModifier & mask;
   }

   static boolean checkForGrassGrowth(int tile, int tilex, int tiley, byte type, byte aData, boolean andFlowers) {
      GrassData.GrowthStage growthStage = GrassData.GrowthStage.decodeTileData(aData);
      GrassData.FlowerType flowerType = GrassData.FlowerType.decodeTileData(aData);
      boolean dataHasChanged = false;
      short height = Tiles.decodeHeight(tile);
      int seed = 100;
      int growthRate;
      if (WurmCalendar.isWinter()) {
         growthRate = 15;
      } else if (WurmCalendar.isSummer()) {
         growthRate = 40;
      } else if (WurmCalendar.isAutumn()) {
         growthRate = 20;
      } else {
         growthRate = 30;
      }

      if (type == Tiles.Tile.TILE_MYCELIUM.id) {
         growthRate = 50 - growthRate;
      }

      int rnd = Server.rand.nextInt(100);
      if (growthRate >= rnd && !growthStage.isMax()) {
         growthStage = growthStage.getNextStage();
         dataHasChanged = true;
      }

      if (andFlowers && createFlowers) {
         GrassData.FlowerType newFlowerType = Terraforming.getRandomFlower(flowerType, false);
         if (flowerType != newFlowerType) {
            flowerType = newFlowerType;
            dataHasChanged = true;
         }
      }

      if (dataHasChanged) {
         if (logger.isLoggable(Level.FINER)) {
            logger.log(
               Level.FINER,
               "tile ["
                  + tilex
                  + ","
                  + tiley
                  + "] changed: "
                  + height
                  + " type="
                  + Tiles.getTile(type).getName()
                  + " stage="
                  + growthStage.toString()
                  + " flower="
                  + flowerType.toString().toLowerCase()
                  + "."
            );
         }

         byte tileData = GrassData.encodeGrassTileData(growthStage, flowerType);
         currentMesh.setTile(tilex, tiley, Tiles.encode(height, type, tileData));
         Server.modifyFlagsByTileType(tilex, tiley, type);
         Players.getInstance().sendChangedTile(tilex, tiley, pollingSurface, false);
         return true;
      } else {
         return false;
      }
   }

   static boolean checkForTreeGrassGrowth(int tile, int tilex, int tiley, byte type, byte aData) {
      GrassData.GrowthTreeStage growthStage = GrassData.GrowthTreeStage.decodeTileData(aData);
      boolean dataHasChanged = false;
      short height = Tiles.decodeHeight(tile);
      int seed = 100;
      int growthRate;
      if (WurmCalendar.isWinter()) {
         growthRate = 5;
      } else if (WurmCalendar.isSummer()) {
         growthRate = 15;
      } else if (WurmCalendar.isAutumn()) {
         growthRate = 10;
      } else {
         growthRate = 20;
      }

      if (Tiles.getTile(type).isMycelium()) {
         growthRate = 25 - growthRate;
      }

      int rnd = Server.rand.nextInt(100);
      if (growthRate >= rnd && !growthStage.isMax()) {
         if (growthStage == GrassData.GrowthTreeStage.LAWN) {
            Village v = Villages.getVillage(tilex, tiley, true);
            if (v == null && Server.rand.nextInt(100) > 75) {
               growthStage = GrassData.GrowthTreeStage.SHORT;
               dataHasChanged = true;
            }
         } else {
            growthStage = growthStage.getNextStage();
            dataHasChanged = true;
         }
      }

      if (dataHasChanged) {
         if (logger.isLoggable(Level.FINER)) {
            logger.log(
               Level.FINER,
               "tile [" + tilex + "," + tiley + "] changed: " + height + " type=" + Tiles.getTile(type).getName() + " stage=" + growthStage.toString() + "."
            );
         }

         FoliageAge tage = FoliageAge.getFoliageAge(aData);
         boolean hasFruit = TreeData.hasFruit(aData);
         boolean incentre = TreeData.isCentre(aData);
         byte tileData = Tiles.encodeTreeData(tage, hasFruit, incentre, growthStage);
         currentMesh.setTile(tilex, tiley, Tiles.encode(height, type, tileData));
         Server.modifyFlagsByTileType(tilex, tiley, type);
         Players.getInstance().sendChangedTile(tilex, tiley, pollingSurface, false);
         return true;
      } else {
         return false;
      }
   }

   static boolean checkForSeedGrowth(int tile, int tilex, int tiley) {
      byte type = Tiles.decodeType(tile);
      Tiles.Tile theTile = Tiles.getTile(type);
      r.setSeed((long)(tilex + tiley * Zones.worldTileSizeY) * 102563L);
      boolean canHaveHerb = r.nextInt(herbChance) == 0 && theTile.canBotanize();
      r.setSeed((long)(tilex + tiley * Zones.worldTileSizeY) * 101531L);
      boolean canHaveForage = r.nextInt(forageChance) == 0 && theTile.canForage();
      checkInvestigateGrowth(tile, tilex, tiley);
      if (containsStructure(tilex, tiley)) {
         return false;
      } else {
         if (canHaveForage || canHaveHerb) {
            boolean containsForage = Server.isForagable(tilex, tiley);
            boolean containsHerb = Server.isBotanizable(tilex, tiley);
            boolean changed = false;
            if (canHaveForage && !containsForage) {
               changed = true;
               containsForage = true;
            }

            if (canHaveHerb && !containsHerb) {
               changed = true;
               containsHerb = true;
            }

            if (changed) {
               setGrassHasSeeds(tilex, tiley, containsForage, containsHerb);
               return true;
            }
         }

         return false;
      }
   }

   static void checkInvestigateGrowth(int tile, int tilex, int tiley) {
      r.setSeed((long)(tilex + tiley * Zones.worldTileSizeY) * 104149L);
      boolean canHaveInvestigate = r.nextInt(investigateChance) == 0;
      if (!containsStructure(tilex, tiley)) {
         if (canHaveInvestigate && !Server.isInvestigatable(tilex, tiley)) {
            Server.setInvestigatable(tilex, tiley, true);
         }
      }
   }

   static void checkGrubGrowth(int tile, int tilex, int tiley) {
      r.setSeed((long)(tilex + tiley * Zones.worldTileSizeY) * 103025L);
      boolean canHaveGrub = r.nextInt(searchChance) == 0;
      if (!containsStructure(tilex, tiley)) {
         if (canHaveGrub) {
            Server.setGrubs(tilex, tiley, true);
         }
      }
   }

   public static void setGrassHasSeeds(int tilex, int tiley, boolean forageable, boolean botanizeable) {
      if (logger.isLoggable(Level.FINEST)) {
         if (forageable) {
            logger.finest(tilex + ", " + tiley + " setting forageable.");
         }

         if (botanizeable) {
            logger.finest(tilex + ", " + tiley + " setting botanizable.");
         }
      }

      Server.setForagable(tilex, tiley, forageable);
      Server.setBotanizable(tilex, tiley, botanizeable);
      Players.getInstance().sendChangedTile(tilex, tiley, true, false);
   }

   public static void checkForFarmGrowth(int tile, int tilex, int tiley, byte type, byte aData) {
      int tileAge = Crops.decodeFieldAge(aData);
      int crop = Crops.getCropNumber(type, aData);
      boolean farmed = Crops.decodeFieldState(aData);
      if (logTilePolling) {
         logger.log(Level.INFO, "Polling farm at " + tilex + "," + tiley + ", age=" + tileAge + ", crop=" + crop + ", farmed=" + farmed);
      }

      if (tileAge == 7) {
         if (Server.rand.nextInt(100) <= 10) {
            currentMesh.setTile(tilex, tiley, Tiles.encode(Tiles.decodeHeight(tile), Tiles.Tile.TILE_DIRT.id, (byte)0));
            Server.modifyFlagsByTileType(tilex, tiley, Tiles.Tile.TILE_DIRT.id);
            Players.getInstance().sendChangedTile(tilex, tiley, pollingSurface, false);
            if (logTilePolling) {
               logger.log(Level.INFO, "Set to dirt");
            }
         }

         if (WurmCalendar.isNight()) {
            SoundPlayer.playSound("sound.birdsong.bird1", tilex, tiley, pollingSurface, 2.0F);
         } else {
            SoundPlayer.playSound("sound.birdsong.bird3", tilex, tiley, pollingSurface, 2.0F);
         }
      } else if (tileAge < 7) {
         if ((tileAge == 5 || tileAge == 6) && Server.rand.nextInt(tileAge) != 0) {
            return;
         }

         ++tileAge;
         VolaTile tempvtile1 = Zones.getOrCreateTile(tilex, tiley, pollingSurface);
         if (tempvtile1.getStructure() != null) {
            currentMesh.setTile(tilex, tiley, Tiles.encode(Tiles.decodeHeight(tile), Tiles.Tile.TILE_DIRT.id, (byte)0));
            Server.modifyFlagsByTileType(tilex, tiley, Tiles.Tile.TILE_DIRT.id);
         } else {
            currentMesh.setTile(tilex, tiley, Tiles.encode(Tiles.decodeHeight(tile), type, Crops.encodeFieldData(false, tileAge, crop)));
            Server.modifyFlagsByTileType(tilex, tiley, type);
            if (logTilePolling) {
               logger.log(Level.INFO, "Changed the tile");
            }
         }

         if (WurmCalendar.isNight()) {
            SoundPlayer.playSound("sound.ambient.night.crickets", tilex, tiley, pollingSurface, 0.0F);
         } else {
            SoundPlayer.playSound("sound.birdsong.bird2", tilex, tiley, pollingSurface, 1.0F);
         }

         Players.getInstance().sendChangedTile(tilex, tiley, pollingSurface, false);
      } else {
         logger.log(
            Level.WARNING, "Strange, tile " + tilex + ", " + tiley + " is field but has age above 7:" + tileAge + " crop is " + crop + " farmed is " + farmed
         );
      }
   }

   private static boolean containsStructure(int tilex, int tiley) {
      try {
         Zone zone = Zones.getZone(tilex, tiley, pollingSurface);
         VolaTile tempvtile1 = zone.getTileOrNull(tilex, tiley);
         if (tempvtile1 != null) {
            if (containsHouse(tempvtile1)) {
               return true;
            }

            if (containsFences(tempvtile1)) {
               return true;
            }
         }
      } catch (NoSuchZoneException var4) {
         logger.log(Level.WARNING, "Weird, no zone for " + tilex + ", " + tiley + " surfaced=" + pollingSurface, (Throwable)var4);
      }

      return false;
   }

   private static boolean containsFences(VolaTile vtile) {
      if (vtile == null) {
         return false;
      } else {
         return vtile.getFences().length > 0;
      }
   }

   private static boolean containsHouse(VolaTile vtile) {
      if (vtile == null) {
         return false;
      } else {
         return vtile.getStructure() != null;
      }
   }

   private static boolean containsTracks(int tilex, int tiley) {
      try {
         Zone zone = Zones.getZone(tilex, tiley, pollingSurface);
         Track[] tracks = zone.getTracksFor(tilex, tiley);
         if (tracks.length > 0) {
            return true;
         }
      } catch (NoSuchZoneException var4) {
         logger.log(Level.WARNING, "Weird, no zone for " + tilex + ", " + tiley + " surfaced=" + pollingSurface, (Throwable)var4);
      }

      return false;
   }

   private static void checkForEruption(int tile, int tilex, int tiley, byte type, byte aData) {
      if (pollingSurface && pollEruption && Tiles.decodeHeight(tile) > 100) {
         for(int xx = -1; xx <= 1; ++xx) {
            for(int yy = -1; yy <= 1; ++yy) {
               if (tilex + xx >= 0 && tiley + yy >= 0 && tilex + xx < 1 << Constants.meshSize && tiley + yy < 1 << Constants.meshSize) {
                  int temptile1 = Server.surfaceMesh.getTile(tilex + xx, tiley + yy);
                  byte tempbyte1 = Tiles.decodeType(temptile1);
                  if (tempbyte1 != Tiles.Tile.TILE_LAVA.id && tempbyte1 != Tiles.Tile.TILE_HOLE.id && !Tiles.isMineDoor(tempbyte1)) {
                     int temptile2 = Server.caveMesh.getTile(tilex + xx, tiley + yy);
                     byte tempbyte2 = Tiles.decodeType(temptile2);
                     if (Tiles.isSolidCave(tempbyte2)) {
                        int tempint1 = Tiles.decodeHeight(temptile1);
                        tempint1 += 4;
                        int tempint2 = Tiles.encode((short)tempint1, Tiles.Tile.TILE_LAVA.id, (byte)0);

                        for(int xn = 0; xn <= 1; ++xn) {
                           for(int yn = 0; yn <= 1; ++yn) {
                              try {
                                 int tempint3 = Tiles.decodeHeight(Server.surfaceMesh.getTile(tilex + xx + xn, tiley + yy + yn));
                                 Server.rockMesh.setTile(tilex + xx + xn, tiley + yy + yn, Tiles.encode((short)tempint3, Tiles.Tile.TILE_ROCK.id, (byte)0));
                              } catch (Exception var16) {
                              }
                           }
                        }

                        Server.surfaceMesh.setTile(tilex + xx, tiley + yy, tempint2);
                        Server.modifyFlagsByTileType(tilex + xx, tiley + yy, Tiles.Tile.TILE_LAVA.id);
                        Server.caveMesh
                           .setTile(
                              tilex + xx,
                              tiley + yy,
                              Tiles.encode(Tiles.decodeHeight(temptile2), Tiles.Tile.TILE_CAVE_WALL_LAVA.id, Tiles.decodeData(temptile2))
                           );
                        Players.getInstance().sendChangedTile(tilex + xx, tiley + yy, false, true);
                        Players.getInstance().sendChangedTile(tilex + xx, tiley + yy, pollingSurface, true);
                        pollEruption = false;
                     }
                  }
               }
            }
         }

         if (!pollEruption) {
            logger.log(Level.INFO, "Eruption at " + tilex + ", " + tiley + "!");
         }
      }
   }

   public static final boolean checkCreateIronRock(int tilex, int tiley) {
      r.setSeed((long)(tilex + tiley * Zones.worldTileSizeY) * 103591L);
      boolean canHaveRock = r.nextInt(forageChance) == 0;
      if (canHaveRock) {
         if (containsStructure(tilex, tiley)) {
            return false;
         }

         boolean containsRock = Server.isForagable(tilex, tiley);
         if (!containsRock) {
            setGrassHasSeeds(tilex, tiley, true, false);
            return true;
         }
      }

      return false;
   }

   private static final void checkForLavaFlow(int tile, int tilex, int tiley, byte type, byte aData) {
      if (Server.rand.nextInt(30) == 0) {
         if ((Tiles.decodeData(tile) & 255) != 255) {
            int tempint2 = Tiles.decodeHeight(tile);
            int temptile1 = Tiles.encode((short)tempint2, Tiles.Tile.TILE_ROCK.id, (byte)0);
            checkCreateIronRock(tilex, tiley);
            currentMesh.setTile(tilex, tiley, temptile1);

            for(int xx = 0; xx <= 1; ++xx) {
               for(int yy = 0; yy <= 1; ++yy) {
                  try {
                     int tempint3 = Tiles.decodeHeight(Server.surfaceMesh.getTile(tilex + xx, tiley + yy));
                     Server.rockMesh.setTile(tilex + xx, tiley + yy, Tiles.encode((short)tempint3, Tiles.Tile.TILE_ROCK.id, (byte)0));
                  } catch (Exception var18) {
                  }
               }
            }

            Terraforming.caveIn(tilex, tiley);
         }
      } else if (Server.rand.nextInt(40) == 0) {
         boolean foundHole = false;
         if (Tiles.decodeHeight(tile) > 0 && pollingSurface) {
            int currHeight = Tiles.decodeHeight(Server.surfaceMesh.getTile(tilex, tiley));

            for(int xx = -1; xx <= 1; ++xx) {
               for(int yy = -1; yy <= 1; ++yy) {
                  if (xx == 0 && yy != 0 || yy == 0 && xx != 0) {
                     if (foundHole) {
                        break;
                     }

                     if (tilex + xx >= 0 && tiley + yy >= 0 && tilex + xx < 1 << Constants.meshSize && tiley + yy < 1 << Constants.meshSize) {
                        int tempint1 = Tiles.decodeHeight(currentMesh.getTile(tilex + xx, tiley + yy));
                        if (tempint1 < currHeight) {
                           int t = currentMesh.getTile(tilex + xx, tiley + yy);
                           byte type2 = Tiles.decodeType(t);
                           if (Server.rand.nextInt(2) == 0 && type2 != Tiles.Tile.TILE_LAVA.id && type2 != Tiles.Tile.TILE_HOLE.id && !Tiles.isMineDoor(type2)
                              )
                            {
                              int tempint2 = tempint1 + 4;
                              int temptile1 = Tiles.encode((short)tempint2, Tiles.Tile.TILE_LAVA.id, (byte)0);

                              for(int xn = 0; xn <= 1; ++xn) {
                                 for(int yn = 0; yn <= 1; ++yn) {
                                    try {
                                       int tempint3 = Tiles.decodeHeight(Server.surfaceMesh.getTile(tilex + xx + xn, tiley + yy + yn));
                                       Server.rockMesh
                                          .setTile(tilex + xx + xn, tiley + yy + yn, Tiles.encode((short)tempint3, Tiles.Tile.TILE_ROCK.id, (byte)0));
                                    } catch (Exception var17) {
                                    }
                                 }
                              }

                              currentMesh.setTile(tilex + xx, tiley + yy, temptile1);
                              Server.modifyFlagsByTileType(tilex + xx, tiley + yy, Tiles.Tile.TILE_LAVA.id);
                              Players.getInstance().sendChangedTile(tilex + xx, tiley + yy, pollingSurface, true);
                           }
                        } else if ((Tiles.decodeData(tile) & 255) == 255) {
                           int t = currentMesh.getTile(tilex + xx, tiley + yy);
                           byte type2 = Tiles.decodeType(t);
                           if (type2 == Tiles.Tile.TILE_ROCK.id) {
                              int temptile2 = Server.caveMesh.getTile(tilex + xx, tiley + yy);
                              byte tempbyte2 = Tiles.decodeType(temptile2);
                              if (Tiles.isSolidCave(tempbyte2)) {
                                 int tempint2 = tempint1 + 4;
                                 int temptile1 = Tiles.encode((short)tempint2, Tiles.Tile.TILE_LAVA.id, (byte)0);
                                 currentMesh.setTile(tilex + xx, tiley + yy, temptile1);
                                 Server.rockMesh.setTile(tilex + xx, tiley + yy, Tiles.encode((short)tempint2, Tiles.Tile.TILE_ROCK.id, (byte)0));
                                 Server.caveMesh
                                    .setTile(
                                       tilex + xx,
                                       tiley + yy,
                                       Tiles.encode(
                                          Tiles.decodeHeight(temptile2),
                                          Tiles.Tile.TILE_CAVE_WALL_LAVA.id,
                                          Tiles.decodeData(Server.caveMesh.getTile(tilex + yy, tiley + yy))
                                       )
                                    );
                                 Players.getInstance().sendChangedTile(tilex + xx, tiley + yy, false, true);
                                 Players.getInstance().sendChangedTile(tilex + xx, tiley + yy, true, true);
                              }
                           }
                        }
                     }
                  } else if (tilex + xx >= 0 && tiley + yy >= 0 && tilex + xx < 1 << Constants.meshSize && tiley + yy < 1 << Constants.meshSize) {
                     int t = currentMesh.getTile(tilex + xx, tiley + yy);
                     byte type2 = Tiles.decodeType(t);
                     if (type2 == Tiles.Tile.TILE_HOLE.id || Tiles.isMineDoor(type2)) {
                        foundHole = true;
                        break;
                     }
                  }
               }
            }
         }
      }
   }

   private static boolean checkForGrassSpread(int tile, int tilex, int tiley, byte type, byte aData) {
      short theight = Tiles.decodeHeight(tile);
      byte tileData = 0;
      boolean isATree = Tiles.isTree(type) || Tiles.isBush(type);
      if (isATree) {
         tileData = aData;
      }

      if (theight > MAX_KELP_HEIGHT && pollingSurface) {
         if (theight < 0) {
            kelpRandom.setSeed((long)(Servers.localServer.id * 25000 + tilex / 12 * tiley / 12));
            if (kelpRandom.nextInt(20) == 0) {
               kelpRandom.setSeed((long)(tilex * tiley));
               if (kelpRandom.nextBoolean() && theight <= MINIMUM_REED_HEIGHT) {
                  byte newType = Tiles.Tile.TILE_REED.id;
                  if (theight < MINIMUM_KELP_HEIGHT) {
                     newType = Tiles.Tile.TILE_KELP.id;
                  }

                  currentMesh.setTile(tilex, tiley, Tiles.encode(theight, newType, (byte)0));
                  Server.modifyFlagsByTileType(tilex, tiley, newType);
                  Players.getInstance().sendChangedTile(tilex, tiley, pollingSurface, false);
                  return true;
               }
            } else if (Server.rand.nextInt(10) == 1 && Tiles.isMycelium(type)) {
               if (Kingdoms.getKingdomTemplateFor(Zones.getKingdom(tilex, tiley)) == 3) {
                  return false;
               }

               byte newType = (byte)Tiles.toNormal(type);
               if (newType == Tiles.Tile.TILE_GRASS.id) {
                  newType = Tiles.Tile.TILE_DIRT.id;
                  if (Server.rand.nextInt(7) == 1) {
                     if (theight < MINIMUM_KELP_HEIGHT) {
                        newType = Tiles.Tile.TILE_KELP.id;
                     } else {
                        newType = Tiles.Tile.TILE_REED.id;
                     }
                  }
               }

               currentMesh.setTile(tilex, tiley, Tiles.encode(theight, newType, tileData));
               Server.modifyFlagsByTileType(tilex, tiley, newType);
               Players.getInstance().sendChangedTile(tilex, tiley, pollingSurface, false);
            }

            return false;
         }

         boolean checkMycel = Servers.isThisAPvpServer() && Kingdoms.getKingdomTemplateFor(Zones.getKingdom(tilex, tiley)) == 3;
         if (Tiles.isMycelium(type) && checkMycel) {
            return false;
         }

         try {
            Zone zone = Zones.getZone(tilex, tiley, pollingSurface);
            VolaTile tempvtile1 = zone.getTileOrNull(tilex, tiley);
            if (tempvtile1 != null && containsHouse(tempvtile1)) {
               return false;
            }
         } catch (NoSuchZoneException var17) {
            logger.log(Level.WARNING, "Weird, no zone for " + tilex + ", " + tiley + " surfaced=" + pollingSurface, (Throwable)var17);
         }

         if (containsTracks(tilex, tiley)) {
            return false;
         }

         if (type == Tiles.Tile.TILE_DIRT_PACKED.id && Villages.getVillage(tilex, tiley, pollingSurface) != null) {
            return false;
         }

         boolean foundGrass = false;
         boolean foundMycel = false;
         boolean foundSteppe = false;
         boolean foundMoss = false;
         int tundraCount = 0;

         for(int xx = -1; xx <= 1; ++xx) {
            for(int yy = -1; yy <= 1; ++yy) {
               if ((xx == 0 && yy != 0 || yy == 0 && xx != 0)
                  && tilex + xx >= 0
                  && tiley + yy >= 0
                  && tilex + xx < Zones.worldTileSizeX
                  && tiley + yy < Zones.worldTileSizeY) {
                  if (xx >= 0 && yy >= 0) {
                     float height = (float)Tiles.decodeHeight(currentMesh.getTile(tilex + xx, tiley + yy));
                     if (height < 0.0F) {
                        return false;
                     }
                  }

                  byte tempbyte2 = Tiles.decodeType(currentMesh.getTile(tilex + xx, tiley + yy));
                  if (tempbyte2 == Tiles.Tile.TILE_GRASS.id && !isATree) {
                     foundGrass = true;
                     if (flowerCounter++ == 10) {
                        tileData = (byte)(Tiles.decodeData(currentMesh.getTile(tilex + xx, tiley + yy)) & 15);
                        flowerCounter = 0;
                     }
                  } else if (Tiles.isNormal(tempbyte2)) {
                     foundGrass = true;
                  } else if (Tiles.isMycelium(tempbyte2) && checkMycel) {
                     foundMycel = true;
                  } else if (tempbyte2 == Tiles.Tile.TILE_STEPPE.id) {
                     foundSteppe = true;
                  } else if (tempbyte2 == Tiles.Tile.TILE_MOSS.id) {
                     foundMoss = true;
                  }
               }

               if ((xx != 0 || yy != 0) && tilex + xx >= 0 && tiley + yy >= 0 && tilex + xx < Zones.worldTileSizeX && tiley + yy < Zones.worldTileSizeY) {
                  byte tempbyte2 = Tiles.decodeType(currentMesh.getTile(tilex + xx, tiley + yy));
                  if (Tiles.isTundra(tempbyte2)) {
                     ++tundraCount;
                     if (xx == 0 || yy == 0) {
                        ++tundraCount;
                     }
                  }
               }
            }
         }

         if (!Tiles.isMycelium(type)) {
            if (foundMoss && Server.rand.nextInt(10) == 1) {
               currentMesh.setTile(tilex, tiley, Tiles.encode(theight, Tiles.Tile.TILE_MOSS.id, (byte)0));
               Server.modifyFlagsByTileType(tilex, tiley, Tiles.Tile.TILE_MOSS.id);
               Players.getInstance().sendChangedTile(tilex, tiley, pollingSurface, false);
               return true;
            }

            if (foundSteppe && Server.rand.nextInt(4) == 1) {
               currentMesh.setTile(tilex, tiley, Tiles.encode(theight, Tiles.Tile.TILE_STEPPE.id, (byte)0));
               Server.modifyFlagsByTileType(tilex, tiley, Tiles.Tile.TILE_STEPPE.id);
               Players.getInstance().sendChangedTile(tilex, tiley, pollingSurface, false);
               return true;
            }

            if (tundraCount > 1) {
               if (Server.rand.nextInt(15) == 1) {
                  currentMesh.setTile(tilex, tiley, Tiles.encode(theight, Tiles.Tile.TILE_TUNDRA.id, (byte)0));
                  Server.modifyFlagsByTileType(tilex, tiley, Tiles.Tile.TILE_TUNDRA.id);
                  Players.getInstance().sendChangedTile(tilex, tiley, pollingSurface, false);
                  return true;
               }

               if (foundGrass && Server.rand.nextInt(5) != 0) {
                  return true;
               }
            }
         }

         if (foundGrass || foundMycel) {
            if (Tiles.isMycelium(type)) {
               byte newTileType = (byte)Tiles.toNormal(type);
               if (type == Tiles.Tile.TILE_MYCELIUM_LAWN.id) {
                  newTileType = Tiles.Tile.TILE_LAWN.id;
               }

               currentMesh.setTile(tilex, tiley, Tiles.encode(theight, newTileType, tileData));
               Server.modifyFlagsByTileType(tilex, tiley, newTileType);
            } else {
               byte newTileType;
               if (theight < MINIMUM_KELP_HEIGHT) {
                  newTileType = Tiles.Tile.TILE_KELP.id;
               } else if (theight < 0) {
                  newTileType = Tiles.Tile.TILE_REED.id;
               } else {
                  newTileType = Tiles.Tile.TILE_GRASS.id;
               }

               currentMesh.setTile(tilex, tiley, Tiles.encode(theight, newTileType, tileData));
               Server.modifyFlagsByTileType(tilex, tiley, newTileType);
            }

            Players.getInstance().sendChangedTile(tilex, tiley, pollingSurface, false);
            return true;
         }
      } else if (pollingSurface && Server.rand.nextInt(10) == 1 && WurmCalendar.isMorning()) {
         SoundPlayer.playSound("sound.fish.splash", tilex, tiley, pollingSurface, 0.0F);
      }

      return false;
   }

   private static boolean checkForDecayToDirt(int tile, int tilex, int tiley, byte type, byte aData) {
      if (pollingSurface && Server.rand.nextInt(10) == 0) {
         try {
            Zone zone = Zones.getZone(tilex, tiley, pollingSurface);
            VolaTile tempvtile1 = zone.getTileOrNull(tilex, tiley);
            if (tempvtile1 != null && !containsHouse(tempvtile1) && tempvtile1.getVillage() == null) {
               boolean foundMarsh = false;

               for(int xx = -1; xx <= 1; ++xx) {
                  for(int yy = -1; yy <= 1; ++yy) {
                     if (tilex + xx >= 0
                        && tiley + yy >= 0
                        && tilex + xx < 1 << Constants.meshSize
                        && tiley + yy < 1 << Constants.meshSize
                        && Tiles.decodeType(currentMesh.getTile(tilex + xx, tiley + yy)) == Tiles.Tile.TILE_MARSH.id) {
                        foundMarsh = true;
                        break;
                     }
                  }
               }

               if (Tiles.decodeHeight(tile) > 0 && foundMarsh && Server.rand.nextInt(3) == 0) {
                  currentMesh.setTile(tilex, tiley, Tiles.encode(Tiles.decodeHeight(tile), Tiles.Tile.TILE_MARSH.id, (byte)0));
                  Server.modifyFlagsByTileType(tilex, tiley, Tiles.Tile.TILE_MARSH.id);
               } else {
                  currentMesh.setTile(tilex, tiley, Tiles.encode(Tiles.decodeHeight(tile), Tiles.Tile.TILE_DIRT.id, (byte)0));
                  Server.modifyFlagsByTileType(tilex, tiley, Tiles.Tile.TILE_DIRT.id);
               }

               Players.getInstance().sendChangedTile(tilex, tiley, pollingSurface, false);
            }
         } catch (NoSuchZoneException var10) {
            logger.log(Level.WARNING, "Weird, no zone for " + tilex + ", " + tiley + " surfaced=" + pollingSurface, (Throwable)var10);
         }
      }

      return true;
   }

   private static boolean checkForMycelGrowth(int tile, int tilex, int tiley, byte type, byte aData) {
      if (Tiles.decodeHeight(tile) > 0 && pollingSurface) {
         if (containsStructure(tilex, tiley)) {
            return true;
         }

         boolean checkMycel = Servers.isThisAPvpServer() && Kingdoms.getKingdomTemplateFor(Zones.getKingdom(tilex, tiley)) == 3;
         boolean foundMycel = false;
         boolean foundMoss = false;
         boolean foundSteppe = false;

         for(int xx = -1; xx <= 1; ++xx) {
            for(int yy = -1; yy <= 1; ++yy) {
               if ((xx == 0 && yy != 0 || yy == 0 && xx != 0)
                  && tilex + xx >= 0
                  && tiley + yy >= 0
                  && tilex + xx < 1 << Constants.meshSize
                  && tiley + yy < 1 << Constants.meshSize) {
                  if (xx >= 0 && yy >= 0) {
                     float height = (float)Tiles.decodeHeight(currentMesh.getTile(tilex + xx, tiley + yy));
                     if (height < 0.0F) {
                        return false;
                     }
                  }

                  byte type2 = Tiles.decodeType(currentMesh.getTile(tilex + xx, tiley + yy));
                  if (Tiles.isMycelium(type2) && checkMycel) {
                     foundMycel = true;
                  } else if (type2 == Tiles.Tile.TILE_MOSS.id) {
                     foundMoss = true;
                  } else if (type2 == Tiles.Tile.TILE_STEPPE.id) {
                     foundSteppe = true;
                  }
               }
            }
         }

         if (foundMycel) {
            if (type == Tiles.Tile.TILE_GRASS.id
               || type == Tiles.Tile.TILE_STEPPE.id
               || type == Tiles.Tile.TILE_DIRT.id
               || type == Tiles.Tile.TILE_LAWN.id
               || type == Tiles.Tile.TILE_MOSS.id) {
               byte newType = Tiles.Tile.TILE_MYCELIUM.id;
               if (type == Tiles.Tile.TILE_LAWN.id) {
                  newType = Tiles.Tile.TILE_MYCELIUM_LAWN.id;
               }

               currentMesh.setTile(tilex, tiley, Tiles.encode(Tiles.decodeHeight(tile), newType, (byte)0));
               Server.modifyFlagsByTileType(tilex, tiley, newType);
               Players.getInstance().sendChangedTile(tilex, tiley, pollingSurface, false);
            } else if (Tiles.isNormalTree(type) || Tiles.isNormalBush(type)) {
               byte newType = (byte)Tiles.toMycelium(type);
               currentMesh.setTile(tilex, tiley, Tiles.encode(Tiles.decodeHeight(tile), newType, aData));
               Server.modifyFlagsByTileType(tilex, tiley, newType);
               Players.getInstance().sendChangedTile(tilex, tiley, pollingSurface, false);
            }

            return foundMycel;
         }

         if (type == Tiles.Tile.TILE_GRASS.id) {
            byte newType = 0;
            if (foundMoss && Server.rand.nextInt(300) == 0) {
               newType = Tiles.Tile.TILE_MOSS.id;
            } else if (foundSteppe && Server.rand.nextInt(10) == 0) {
               newType = Tiles.Tile.TILE_STEPPE.id;
            }

            if (newType != 0) {
               currentMesh.setTile(tilex, tiley, Tiles.encode(Tiles.decodeHeight(tile), newType, (byte)0));
               Server.modifyFlagsByTileType(tilex, tiley, newType);
               Players.getInstance().sendChangedTile(tilex, tiley, pollingSurface, false);
               return true;
            }
         }
      } else if (pollingSurface && Server.rand.nextInt(10) == 1 && WurmCalendar.isMorning()) {
         SoundPlayer.playSound("sound.fish.splash", tilex, tiley, pollingSurface, 0.0F);
      }

      return false;
   }

   private static boolean checkForTreeSprout(int tilex, int tiley, int origtype, byte origdata) {
      int newtilex = tilex - 10 + Server.rand.nextInt(21);
      int newtiley = tiley - 10 + Server.rand.nextInt(21);
      if (!GeneralUtilities.isValidTileLocation(newtilex, newtiley)) {
         return true;
      } else {
         int newtile = currentMesh.getTile(newtilex, newtiley);
         if (Tiles.decodeHeight(newtile) > 0 && pollingSurface) {
            if (newtilex == tilex && newtiley == tiley) {
               return true;
            }

            if (containsStructure(newtilex, newtiley) || containsTracks(newtilex, newtiley)) {
               return true;
            }

            byte newtype = Tiles.decodeType(newtile);
            Tiles.Tile theNewTile = Tiles.getTile(newtype);
            Tiles.Tile theOrigTile = Tiles.getTile(origtype);
            short newHeight = Tiles.decodeHeight(newtile);
            if (theNewTile == Tiles.Tile.TILE_GRASS || theNewTile == Tiles.Tile.TILE_MYCELIUM) {
               int foundTrees = 0;

               for(int xx = -3; xx <= 3; ++xx) {
                  for(int yy = -3; yy <= 3; ++yy) {
                     if ((xx == 0 && yy != 0 || xx != 0 && yy == 0) && GeneralUtilities.isValidTileLocation(newtilex + xx, newtiley + yy)) {
                        int checkTile = currentMesh.getTile(newtilex + xx, newtiley + yy);
                        byte checktype = Tiles.decodeType(checkTile);
                        byte checkdata = Tiles.decodeData(checkTile);
                        Tiles.Tile theCheckTile = Tiles.getTile(checktype);
                        if (theCheckTile.isTree()) {
                           if (theCheckTile.isOak(checkdata) || theOrigTile.isOak(origdata)) {
                              ++foundTrees;
                              break;
                           }

                           if (!theCheckTile.isWillow(checkdata) && !theOrigTile.isWillow(origdata)) {
                              if (xx > -2 && xx < 2 && yy > -2 && yy < 2) {
                                 ++foundTrees;
                                 break;
                              }
                           } else if (xx > -3 && xx < 3 && yy > -3 && yy < 3) {
                              ++foundTrees;
                              break;
                           }
                        }
                     }
                  }
               }

               if (foundTrees < 1) {
                  byte newdata = 0;
                  boolean evil = false;
                  if (Kingdoms.getKingdomTemplateFor(Zones.getKingdom(tilex, tiley)) == 3) {
                     evil = true;
                  }

                  byte newType;
                  if (theOrigTile.isTree()) {
                     TreeData.TreeType treetype = theOrigTile.getTreeType(origdata);
                     if (evil && theOrigTile.isMycelium()) {
                        newType = treetype.asMyceliumTree();
                     } else {
                        newType = treetype.asNormalTree();
                     }
                  } else {
                     BushData.BushType bushtype = theOrigTile.getBushType(origdata);
                     if (evil && theOrigTile.isMycelium()) {
                        newType = bushtype.asMyceliumBush();
                     } else {
                        newType = bushtype.asNormalBush();
                     }
                  }

                  newdata = Tiles.encodeTreeData(FoliageAge.YOUNG_ONE, false, false, GrassData.GrowthTreeStage.SHORT);
                  currentMesh.setTile(newtilex, newtiley, Tiles.encode(newHeight, newType, newdata));
                  Server.modifyFlagsByTileType(newtilex, newtiley, newType);
                  Server.setWorldResource(newtilex, newtiley, 0);
                  if (WurmCalendar.isNight()) {
                     SoundPlayer.playSound("sound.birdsong.bird1", newtilex, newtiley, pollingSurface, 3.0F);
                  } else {
                     SoundPlayer.playSound("sound.birdsong.bird4", newtilex, newtiley, pollingSurface, 0.3F);
                  }

                  Players.getInstance().sendChangedTile(newtilex, newtiley, pollingSurface, false);
                  return true;
               }
            }
         } else if (pollingSurface && Server.rand.nextInt(10) == 1 && WurmCalendar.isMorning()) {
            SoundPlayer.playSound("sound.fish.splash", newtilex, newtiley, pollingSurface, 0.0F);
         }

         return false;
      }
   }

   private static void checkForTreeGrowth(int tile, int tilex, int tiley, byte type, byte aData) {
      if (pollingSurface) {
         Tiles.Tile theTile = Tiles.getTile(type);
         boolean underwater = Tiles.decodeHeight(tile) <= -5;
         int age = aData >> 4 & 15;
         if (age != 15) {
            if (treeGrowth == 0 && age <= 1) {
               currentMesh.setTile(tilex, tiley, Tiles.encode(Tiles.decodeHeight(tile), Tiles.Tile.TILE_GRASS.id, (byte)0));
               Server.modifyFlagsByTileType(tilex, tiley, Tiles.Tile.TILE_GRASS.id);
               Server.setWorldResource(tilex, tiley, 0);
               Players.getInstance().sendChangedTile(tilex, tiley, pollingSurface, false);
               return;
            }

            int chance = entryServer ? Server.rand.nextInt(20) : Server.rand.nextInt(225);
            if (chance <= (16 - age) * (16 - age)) {
               byte partdata = (byte)(aData & 15);
               boolean isOak = theTile.isOak(partdata);
               boolean isWillow = theTile.isWillow(partdata);
               if (!isOak || Server.rand.nextInt(5) == 0) {
                  if (theTile.isMycelium() && Kingdoms.getKingdomTemplateFor(Zones.getKingdom(tilex, tiley)) != 3 && Server.rand.nextInt(3) == 0) {
                     byte newData = (byte)(aData & 247);
                     byte newType;
                     if (theTile.isTree()) {
                        newType = theTile.getTreeType(partdata).asNormalTree();
                     } else {
                        newType = theTile.getBushType(partdata).asNormalBush();
                     }

                     currentMesh.setTile(tilex, tiley, Tiles.encode(Tiles.decodeHeight(tile), newType, newData));
                     Server.modifyFlagsByTileType(tilex, tiley, newType);
                     Players.getInstance().sendChangedTile(tilex, tiley, pollingSurface, false);
                     return;
                  }

                  if (underwater) {
                     age = FoliageAge.SHRIVELLED.getAgeId();
                  } else {
                     ++age;
                  }

                  if (chance > 8) {
                     if (WurmCalendar.isNight()) {
                        SoundPlayer.playSound("sound.birdsong.owl.short", tilex, tiley, pollingSurface, 4.0F);
                     } else {
                        SoundPlayer.playSound("sound.ambient.day.crickets", tilex, tiley, pollingSurface, 0.0F);
                     }
                  }

                  Server.setWorldResource(tilex, tiley, 0);
                  byte newData = (byte)((age << 4) + partdata & 0xFF);
                  byte newType = convertToNewType(theTile, newData);
                  currentMesh.setTile(tilex, tiley, Tiles.encode(Tiles.decodeHeight(tile), newType, newData));
                  Server.modifyFlagsByTileType(tilex, tiley, newType);
                  Players.getInstance().sendChangedTile(tilex, tiley, pollingSurface, false);
                  if (age >= 15) {
                     Zones.removeWildHive(tilex, tiley);
                  }
               }

               if ((isOak || isWillow) && age >= 7) {
                  int rad = 1;
                  if (age >= 10) {
                     rad = 2;
                  }

                  int maxX = Math.min(tilex + rad, currentMesh.getSize() - 1);
                  int maxY = Math.min(tiley + rad, currentMesh.getSize() - 1);

                  for(int x = Math.max(tilex - rad, 0); x <= maxX; ++x) {
                     for(int y = Math.max(tiley - rad, 0); y <= maxY; ++y) {
                        if (x != tilex || y != tiley) {
                           int tt = currentMesh.getTile(x, y);
                           byte ttyp = Tiles.decodeType(tt);
                           Tiles.Tile ttile = Tiles.getTile(ttyp);
                           byte newType = Tiles.Tile.TILE_GRASS.id;
                           if (ttile.isMyceliumTree() || ttile.isMyceliumBush()) {
                              newType = Tiles.Tile.TILE_MYCELIUM.id;
                           }

                           if (ttile.isTree() || ttile.isBush()) {
                              byte newData = 0;
                              Server.setWorldResource(x, y, 0);
                              currentMesh.setTile(x, y, Tiles.encode(Tiles.decodeHeight(tt), newType, (byte)0));
                              Server.modifyFlagsByTileType(x, y, newType);
                              Players.getInstance().sendChangedTile(x, y, pollingSurface, false);
                              Zones.removeWildHive(x, y);
                           }
                        }
                     }
                  }
               }

               Zones.reposWildHive(tilex, tiley, theTile, aData);
            }

            if (age < 15 && age > 8 && treeGrowth > 0 && !underwater && theTile != Tiles.Tile.TILE_BUSH_LINGONBERRY) {
               chance = Server.rand.nextInt(treeGrowth);
               if (chance < 1) {
                  checkForTreeSprout(tilex, tiley, type, aData);
               } else if (chance == 2) {
                  growMushroom(tilex, tiley);
               }
            }

            if (theTile.isTree() && age == 15) {
               Server.setGrubs(tilex, tiley, true);
            }

            if (theTile.isTree() && age == 14 && theTile == Tiles.Tile.TILE_TREE_BIRCH) {
               checkGrubGrowth(tile, tilex, tiley);
            }

            if (theTile.isBush()) {
               checkGrubGrowth(tile, tilex, tiley);
            }
         } else if (theTile == Tiles.Tile.TILE_BUSH_LINGONBERRY) {
            currentMesh.setTile(tilex, tiley, Tiles.encode(Tiles.decodeHeight(tile), Tiles.Tile.TILE_TUNDRA.id, (byte)0));
            Server.modifyFlagsByTileType(tilex, tiley, Tiles.Tile.TILE_TUNDRA.id);
            Server.setWorldResource(tilex, tiley, 0);
            Players.getInstance().sendChangedTile(tilex, tiley, pollingSurface, false);
         } else {
            int chance = Server.rand.nextInt(15);
            if (chance == 1) {
               Zones.removeWildHive(tilex, tiley);
               if (underwater) {
                  currentMesh.setTile(tilex, tiley, Tiles.encode(Tiles.decodeHeight(tile), Tiles.Tile.TILE_DIRT.id, (byte)0));
                  Server.modifyFlagsByTileType(tilex, tiley, Tiles.Tile.TILE_DIRT.id);
                  Server.setWorldResource(tilex, tiley, 0);
                  Players.getInstance().sendChangedTile(tilex, tiley, pollingSurface, false);
               } else {
                  byte newData = (byte)(aData & 15);
                  byte var24 = convertToNewType(theTile, aData);
                  boolean noChange = true;
                  Village v = Villages.getVillage(tilex, tiley, true);
                  if (v == null) {
                     noChange = Server.rand.nextInt(100) < 75;
                  }

                  boolean inCenter = TreeData.isCentre(newData) && noChange;
                  GrassData.GrowthTreeStage stage = TreeData.getGrassLength(newData);
                  newData = Tiles.encodeTreeData((byte)0, false, inCenter, stage);
                  Server.setWorldResource(tilex, tiley, 0);
                  currentMesh.setTile(tilex, tiley, Tiles.encode(Tiles.decodeHeight(tile), var24, newData));
                  Players.getInstance().sendChangedTile(tilex, tiley, pollingSurface, false);
               }
            } else {
               checkGrubGrowth(tile, tilex, tiley);
            }
         }
      }
   }

   private static void checkForLingonberryStart(int tile, int tilex, int tiley) {
      for(int x = tilex - 2; x < tilex + 2; ++x) {
         for(int y = tiley - 2; y < tiley + 2; ++y) {
            int tt = currentMesh.getTile(x, y);
            byte ttyp = Tiles.decodeType(tt);
            Tiles.Tile ttile = Tiles.getTile(ttyp);
            if (ttile != Tiles.Tile.TILE_TUNDRA && ttile != Tiles.Tile.TILE_GRASS && ttile != Tiles.Tile.TILE_DIRT) {
               return;
            }
         }
      }

      currentMesh.setTile(tilex, tiley, Tiles.encode(Tiles.decodeHeight(tile), Tiles.Tile.TILE_BUSH_LINGONBERRY.id, (byte)0));
      Server.modifyFlagsByTileType(tilex, tiley, Tiles.Tile.TILE_BUSH_LINGONBERRY.id);
      Server.setWorldResource(tilex, tiley, 0);
      Players.getInstance().sendChangedTile(tilex, tiley, pollingSurface, false);
   }

   private static byte convertToNewType(Tiles.Tile theTile, byte data) {
      if (theTile.isNormalTree()) {
         return theTile.getTreeType(data).asNormalTree();
      } else if (theTile.isMyceliumTree()) {
         return theTile.getTreeType(data).asMyceliumTree();
      } else if (theTile.isEnchantedTree()) {
         return theTile.getTreeType(data).asEnchantedTree();
      } else if (theTile.isNormalBush()) {
         return theTile.getBushType(data).asNormalBush();
      } else {
         return theTile.isMyceliumBush() ? theTile.getBushType(data).asMyceliumBush() : theTile.getBushType(data).asEnchantedBush();
      }
   }

   public static void growMushroom(int tilex, int tiley) {
      int num = tilex + tiley;
      if (num % 128 == 0) {
         if (logger.isLoggable(Level.FINEST)) {
            logger.finest("Creating mushrrom at tile " + tilex + ", " + tiley);
         }

         int chance = Server.rand.nextInt(100);
         int templ = 247;
         if (chance > 40) {
            if (chance < 60) {
               templ = 246;
            } else if (chance < 80) {
               templ = 248;
            } else if (chance < 90) {
               templ = 249;
            } else if (chance < 99) {
               templ = 251;
            } else {
               templ = 250;
            }
         }

         float posx = (float)(tilex << 2) + Server.rand.nextFloat() * 4.0F;
         float posy = (float)(tiley << 2) + Server.rand.nextFloat() * 4.0F;

         try {
            ItemFactory.createItem(
               templ, 80.0F + (float)Server.rand.nextInt(20), posx, posy, (float)Server.rand.nextInt(180), pollingSurface, (byte)0, -10L, null
            );
         } catch (FailedException var8) {
            logger.log(Level.WARNING, var8.getMessage(), (Throwable)var8);
         } catch (NoSuchTemplateException var9) {
            logger.log(Level.WARNING, var9.getMessage(), (Throwable)var9);
         }
      }
   }

   private static void addWildBeeHives(int tilex, int tiley, Tiles.Tile theTile, byte aData) {
      if (Zones.isFarFromAnyHive(tilex, tiley, true)) {
         byte age = FoliageAge.getAgeAsByte(aData);
         TreeData.TreeType treeType = theTile.getTreeType(aData);
         if (age > 7 && age < 13) {
            boolean canHaveHive = false;
            switch(treeType) {
               case BIRCH:
               case PINE:
               case CEDAR:
               case WILLOW:
               case MAPLE:
               case FIR:
               case LINDEN:
                  canHaveHive = true;
            }

            if (canHaveHive) {
               r.setSeed((long)(tilex + tiley * Zones.worldTileSizeY) * HIVE_PRIME);
               if (r.nextInt(500) == 0) {
                  boolean ok = true;

                  for(int x = -1; x <= 1; ++x) {
                     for(int y = -1; y <= 1; ++y) {
                        VolaTile vt = Zones.getTileOrNull(x, y, true);
                        if (vt != null && vt.getStructure() != null) {
                           ok = false;
                           break;
                        }
                     }
                  }

                  if (ok) {
                     Point4f p = MethodsItems.getHivePos(tilex, tiley, theTile, aData);
                     if (p.getPosZ() == 0.0F) {
                        return;
                     }

                     try {
                        int ql = 30 + Server.rand.nextInt(41);
                        Item wildHive = ItemFactory.createItem(1239, (float)ql, treeType.getMaterial(), (byte)0, null);
                        wildHive.setPos(p.getPosX(), p.getPosY(), p.getPosZ(), p.getRot(), -10L);
                        wildHive.setLastOwnerId(-10L);
                        wildHive.setAuxData((byte)1);
                        Zone zone = Zones.getZone(Zones.safeTileX(tilex), Zones.safeTileY(tiley), true);
                        zone.addItem(wildHive);
                        if (Servers.isThisATestServer()) {
                           Players.getInstance()
                              .sendGmMessage(
                                 null,
                                 "System",
                                 "Debug: Adding Hive ("
                                    + wildHive.getWurmId()
                                    + ") @ "
                                    + tilex
                                    + ","
                                    + tiley
                                    + " ql:"
                                    + ql
                                    + " rot:"
                                    + p.getRot()
                                    + " ht:"
                                    + p.getPosZ()
                                    + " material:"
                                    + treeType.getName(),
                                 false
                              );
                        }
                     } catch (FailedException var12) {
                        logger.log(Level.WARNING, var12.getMessage(), (Throwable)var12);
                     } catch (NoSuchTemplateException var13) {
                        logger.log(Level.WARNING, var13.getMessage(), (Throwable)var13);
                     } catch (NoSuchZoneException var14) {
                        logger.log(Level.WARNING, var14.getMessage(), (Throwable)var14);
                     }
                  }
               }
            }
         }
      }
   }

   private static void checkHoneyProduction(HiveZone hiveZone, int tilex, int tiley, byte type, byte data, Tiles.Tile theTile) {
      Item hive = hiveZone.getCurrentHive();
      if (!hive.isOnSurface()) {
         if (Servers.isThisATestServer()) {
            Players.getInstance()
               .sendGmMessage(
                  null,
                  "System",
                  "Debug: Queen bee died as hive is underground @ "
                     + hive.getTileX()
                     + ","
                     + hive.getTileY()
                     + " from "
                     + hive.getWurmId()
                     + " to "
                     + hive.getWurmId(),
                  false
               );
         }

         hive.removeQueen();
      } else {
         if (hiveZone.hasHive(tilex, tiley) && Server.rand.nextInt(5) == 0) {
            Item sugar = hive.getSugar();
            if (sugar != null) {
               Items.destroyItem(sugar.getWurmId());
            } else {
               Item honey = hive.getHoney();
               if (honey != null) {
                  honey.setWeight(Math.max(0, honey.getWeightGrams() - 10), true);
                  honey.setLastMaintained(WurmCalendar.currentTime);
               }
            }
         }

         if (hive.hasQueen()) {
            if (Server.rand.nextInt(3) == 0) {
               VolaTile vt = Zones.getTileOrNull(tilex, tiley, true);
               Item emptyHive = vt != null ? vt.findHive(1175, false) : null;
               if (emptyHive != null) {
                  boolean canMove = false;
                  if (hive.hasTwoQueens()) {
                     canMove = true;
                  } else if (hive.getTemplateId() != 1175 && emptyHive.getCurrentQualityLevel() > hive.getCurrentQualityLevel()) {
                     float distX = Math.abs(hive.getPosX() - emptyHive.getPosX());
                     float distY = Math.abs(hive.getPosY() - emptyHive.getPosY());
                     float dist = Math.max(distX, distY);
                     if (dist == 0.0F) {
                        logger.info("More than one hive on same tile " + hive.getPosX() + "," + hive.getPosY());
                     }

                     canMove = Server.rand.nextInt(Math.max(1, (int)dist)) == 0;
                  }

                  if (canMove) {
                     if (Servers.isThisATestServer()) {
                        Players.getInstance()
                           .sendGmMessage(
                              null,
                              "System",
                              "Debug: Queen bee migrated @ "
                                 + hive.getTileX()
                                 + ","
                                 + hive.getTileY()
                                 + " from "
                                 + hive.getWurmId()
                                 + " to "
                                 + emptyHive.getWurmId(),
                              false
                           );
                     }

                     hive.removeQueen();
                     emptyHive.addQueen();
                     if (!hive.hasQueen()) {
                        if (hive.getTemplateId() == 1239) {
                           for(Item item : hive.getItemsAsArray()) {
                              Items.destroyItem(item.getWurmId());
                           }

                           Items.destroyItem(hive.getWurmId());
                           return;
                        }

                        Zones.removeHive(hive, false);
                     }
                  }
               }
            }

            if (Server.rand.nextInt(3) == 0) {
               Item honey = addHoney(tilex, tiley, hive, type, data, theTile);
               if (hive.hasQueen()
                  && !hive.hasTwoQueens()
                  && (WurmCalendar.isSeasonSpring() || WurmCalendar.isSeasonSummer())
                  && hiveZone.hasHive(tilex, tiley)
                  && honey != null
                  && honey.getWeightGrams() > 1000
                  && Server.rand.nextInt(5) == 0) {
                  if (Servers.isThisATestServer()) {
                     Players.getInstance().sendGmMessage(null, "System", "Debug: Queen bee added @ " + hive.getTileX() + "," + hive.getTileY(), false);
                  }

                  hive.addQueen();
               }
            }
         } else {
            Zones.removeHive(hive, true);
         }
      }
   }

   @Nullable
   private static Item addHoney(int tilex, int tiley, Item hive, byte type, byte data, Tiles.Tile theTile) {
      float nectarProduced = 0.0F;
      int starfall = WurmCalendar.getStarfall();
      if (type == 7 || type == 43) {
         switch(FieldData.getAge(data)) {
            case 0:
               nectarProduced += 0.0F;
               break;
            case 1:
               nectarProduced += 4.0F;
               break;
            case 2:
               nectarProduced += 10.0F;
               break;
            case 3:
               nectarProduced += 15.0F;
               break;
            case 4:
               nectarProduced += 17.0F;
               break;
            case 5:
               nectarProduced += 8.0F;
               break;
            case 6:
               nectarProduced += 6.0F;
               break;
            case 7:
               nectarProduced += 0.0F;
         }

         int worldResource = Server.getWorldResource(tilex, tiley);
         int farmedCount = worldResource >>> 11;
         int farmedChance = worldResource & 2047;
         int newfarmedChance = Math.min(farmedChance + (int)(nectarProduced * 10.0F), 2047);
         Server.setWorldResource(tilex, tiley, (farmedCount << 11) + newfarmedChance);
         Players.getInstance().sendChangedTile(tilex, tiley, true, false);
      } else if (type == 2) {
         if (starfall < 1) {
            nectarProduced += 0.0F;
         } else if (starfall < 4) {
            nectarProduced += 5.0F;
         } else if (starfall < 9) {
            nectarProduced += 5.0F;
         } else {
            nectarProduced += 2.0F;
         }

         GrassData.FlowerType flowerType = GrassData.FlowerType.decodeTileData(data);
         if (flowerType != GrassData.FlowerType.NONE) {
            nectarProduced *= 1.0F + (float)flowerType.getEncodedData() / 2.0F;
         }
      } else if (theTile.isEnchanted()) {
         if (starfall < 1) {
            ++nectarProduced;
         } else if (starfall < 4) {
            nectarProduced += 6.0F;
         } else if (starfall < 9) {
            nectarProduced += 6.0F;
         } else {
            nectarProduced += 3.0F;
         }
      } else if (type == 22) {
         if (starfall < 1) {
            ++nectarProduced;
         } else if (starfall < 4) {
            nectarProduced += 9.0F;
         } else if (starfall < 9) {
            nectarProduced += 11.0F;
         } else {
            nectarProduced += 7.0F;
         }
      } else if (theTile.isNormalBush() || theTile.isNormalTree()) {
         int treeAge = FoliageAge.getAgeAsByte(data);
         if (treeAge < 4) {
            ++nectarProduced;
         } else if (treeAge < 8) {
            nectarProduced += 4.0F;
         } else if (treeAge < 12) {
            nectarProduced += 8.0F;
         } else if (treeAge < 14) {
            nectarProduced += 7.0F;
         } else if (treeAge < 15) {
            nectarProduced += 5.0F;
         }

         if (starfall < 1) {
            nectarProduced += 2.0F;
         } else if (starfall < 4) {
            nectarProduced += 8.0F;
         } else if (starfall < 9) {
            nectarProduced += 6.0F;
         } else {
            nectarProduced += 4.0F;
         }
      }

      if (starfall < 1) {
         nectarProduced *= 0.1F;
      } else if (starfall < 4) {
         nectarProduced *= 1.5F;
      } else if (starfall < 9) {
         nectarProduced *= 1.1F;
      } else {
         nectarProduced *= 0.7F;
      }

      Item honey = hive.getHoney();
      if (nectarProduced < 5.0F) {
         return honey;
      } else {
         int addedHoneyWeight = (int)nectarProduced - 4;
         int newHoneyWeight = addedHoneyWeight;
         if (addedHoneyWeight > 0 && hive.getFreeVolume() > addedHoneyWeight) {
            int waxcount = hive.getWaxCount();
            if (honey != null) {
               float totalQL = (float)honey.getWeightGrams() * honey.getCurrentQualityLevel() + (float)addedHoneyWeight * hive.getCurrentQualityLevel();
               newHoneyWeight = honey.getWeightGrams() + addedHoneyWeight;
               float newQL = totalQL / (float)newHoneyWeight;
               honey.setWeight(newHoneyWeight, true);
               honey.setQualityLevel(newQL);
               honey.setDamage(0.0F);
               honey.setLastOwnerId(0L);
            } else {
               try {
                  Item newhoney = ItemFactory.createItem(70, hive.getCurrentQualityLevel(), (byte)29, (byte)0, null);
                  newhoney.setWeight(newHoneyWeight, true);
                  newhoney.setLastOwnerId(0L);
                  hive.insertItem(newhoney);
               } catch (FailedException var16) {
                  logger.log(Level.WARNING, var16.getMessage(), (Throwable)var16);
               } catch (NoSuchTemplateException var17) {
                  logger.log(Level.WARNING, var17.getMessage(), (Throwable)var17);
               }
            }

            if (nectarProduced > 10.0F && waxcount < 20 && Server.rand.nextInt(40) == 0) {
               try {
                  Item newwax = ItemFactory.createItem(1254, hive.getCurrentQualityLevel(), (byte)29, (byte)0, null);
                  newwax.setLastOwnerId(0L);
                  if (hive.testInsertItem(newwax)) {
                     hive.insertItem(newwax);
                  } else {
                     Items.destroyItem(newwax.getWurmId());
                  }
               } catch (FailedException var14) {
                  logger.log(Level.WARNING, var14.getMessage(), (Throwable)var14);
               } catch (NoSuchTemplateException var15) {
                  logger.log(Level.WARNING, var15.getMessage(), (Throwable)var15);
               }
            }
         }

         return honey;
      }
   }
}

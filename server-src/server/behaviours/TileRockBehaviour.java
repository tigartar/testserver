package com.wurmonline.server.behaviours;

import com.wurmonline.mesh.MeshIO;
import com.wurmonline.mesh.Tiles;
import com.wurmonline.server.Constants;
import com.wurmonline.server.FailedException;
import com.wurmonline.server.GeneralUtilities;
import com.wurmonline.server.Items;
import com.wurmonline.server.LoginHandler;
import com.wurmonline.server.MeshTile;
import com.wurmonline.server.Players;
import com.wurmonline.server.Point;
import com.wurmonline.server.Server;
import com.wurmonline.server.Servers;
import com.wurmonline.server.creatures.Communicator;
import com.wurmonline.server.creatures.Creature;
import com.wurmonline.server.highways.HighwayPos;
import com.wurmonline.server.highways.MethodsHighways;
import com.wurmonline.server.items.Item;
import com.wurmonline.server.items.ItemFactory;
import com.wurmonline.server.items.ItemTemplate;
import com.wurmonline.server.items.ItemTemplateFactory;
import com.wurmonline.server.items.NoSuchTemplateException;
import com.wurmonline.server.items.RuneUtilities;
import com.wurmonline.server.kingdom.Kingdoms;
import com.wurmonline.server.players.Player;
import com.wurmonline.server.skills.NoSuchSkillException;
import com.wurmonline.server.skills.Skill;
import com.wurmonline.server.skills.Skills;
import com.wurmonline.server.sounds.SoundPlayer;
import com.wurmonline.server.structures.BridgePart;
import com.wurmonline.server.structures.Fence;
import com.wurmonline.server.structures.Structure;
import com.wurmonline.server.utils.logging.TileEvent;
import com.wurmonline.server.villages.NoSuchVillageException;
import com.wurmonline.server.villages.Villages;
import com.wurmonline.server.zones.FaithZone;
import com.wurmonline.server.zones.NoSuchZoneException;
import com.wurmonline.server.zones.Trap;
import com.wurmonline.server.zones.VolaTile;
import com.wurmonline.server.zones.Zone;
import com.wurmonline.server.zones.Zones;
import java.io.IOException;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Random;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.annotation.Nullable;

public final class TileRockBehaviour extends TileBehaviour {
   private static final Logger logger = Logger.getLogger(TileRockBehaviour.class.getName());
   static final Random rockRandom = new Random();
   private static final int worldSizeX = 1 << Constants.meshSize;
   private static final int mineZoneSize = 32;
   private static final int mineZoneDiv = worldSizeX / 32;
   private static final int minPrayingHeightDec = 400;
   private static final byte[][] minezones = new byte[mineZoneDiv + 1][mineZoneDiv + 1];
   static final long HUGE_PRIME = 789221L;
   static final long PROSPECT_PRIME = 181081L;
   public static final long SALT_PRIME = 102533L;
   public static final long SANDSTONE_PRIME = 123307L;
   static long SOURCE_PRIME = 786431L + (long)Server.rand.nextInt(10000);
   public static final int saltFactor = 100;
   public static final int sandstoneFactor = 64;
   static final int flintFactor = 200;
   static int sourceFactor = 1000;
   static final long FLINT_PRIME = 6883L;
   static final int MIN_QL = 20;
   static int MAX_QL = 100;
   static final int MAX_ROCK_QL = 100;
   static final long EMERALD_PRIME = 66083L;
   static final long OPAL_PRIME = 101333L;
   static final long RUBY_PRIME = 812341L;
   static final long DIAMOND_PRIME = 104711L;
   static final long SAPPHIRE_PRIME = 781661L;
   private static final short CAVE_DESCENT_RATE = 20;
   static final int MAX_CEIL = 255;
   static final int DIG_CEIL = 30;
   public static final int MIN_CEIL = 5;
   static final int DIG_CEIL_REACH = 60;
   static final short MIN_CAVE_FLOOR = -25;
   static final short MAX_SLOPE_DOWN = -40;
   static final short MIN_ROCK_UNDERWATER = -25;
   public static final short CAVE_INIT_HEIGHT = -100;
   private static final int ORE_ZONE_FACTOR = 4;
   private static int oreRand = 0;

   TileRockBehaviour() {
      super((short)9);
      sourceFactor = Servers.isThisAHomeServer() ? 100 : 50;
   }

   TileRockBehaviour(short type) {
      super(type);
   }

   @Override
   public List<ActionEntry> getBehavioursFor(Creature performer, int tilex, int tiley, boolean onSurface, int tile) {
      List<ActionEntry> toReturn = new LinkedList<>();
      toReturn.addAll(super.getBehavioursFor(performer, tilex, tiley, onSurface, tile));
      if (Tiles.decodeHeight(tile) > 400 && performer.getDeity() != null && performer.getDeity().isMountainGod()) {
         Methods.addActionIfAbsent(toReturn, Actions.actionEntrys[141]);
      }

      if (performer.getCultist() != null && performer.getCultist().maySpawnVolcano()) {
         HighwayPos highwaypos = MethodsHighways.getHighwayPos(tilex, tiley, onSurface);
         if (highwaypos == null || !MethodsHighways.onHighway(highwaypos)) {
            toReturn.add(new ActionEntry((short)78, "Erupt", "erupting"));
         }
      }

      toReturn.add(Actions.actionEntrys[642]);
      return toReturn;
   }

   @Override
   public List<ActionEntry> getBehavioursFor(Creature performer, Item subject, int tilex, int tiley, boolean onSurface, int tile) {
      List<ActionEntry> toReturn = new LinkedList<>();
      toReturn.addAll(super.getBehavioursFor(performer, subject, tilex, tiley, onSurface, tile));
      if (subject.isMiningtool()) {
         toReturn.add(new ActionEntry((short)-3, "Mining", "Mining options"));
         toReturn.add(Actions.actionEntrys[145]);
         toReturn.add(Actions.actionEntrys[156]);
         toReturn.add(Actions.actionEntrys[227]);
         if (performer.getPower() >= 4 && subject.getTemplateId() == 176) {
            toReturn.add(Actions.actionEntrys[518]);
         }
      } else if (subject.getTemplateId() == 782) {
         toReturn.add(Actions.actionEntrys[518]);
      }

      if (Tiles.decodeHeight(tile) > 400 && performer.getDeity() != null && performer.getDeity().isMountainGod()) {
         Methods.addActionIfAbsent(toReturn, Actions.actionEntrys[141]);
      }

      if (performer.getCultist() != null && performer.getCultist().maySpawnVolcano() || subject.getTemplateId() == 176 && performer.getPower() >= 5) {
         HighwayPos highwaypos = MethodsHighways.getHighwayPos(tilex, tiley, onSurface);
         if (highwaypos == null || !MethodsHighways.onHighway(highwaypos)) {
            toReturn.add(new ActionEntry((short)78, "Erupt", "erupting"));
         }
      }

      toReturn.add(Actions.actionEntrys[642]);
      return toReturn;
   }

   @Override
   public boolean action(Action act, Creature performer, int tilex, int tiley, boolean onSurface, int tile, short action, float counter) {
      boolean done = true;
      if (action == 1) {
         Communicator comm = performer.getCommunicator();
         comm.sendNormalServerMessage("You see hard rock.");
         sendVillageString(performer, tilex, tiley, true);
         Trap t = Trap.getTrap(tilex, tiley, performer.getLayer());
         if (performer.getPower() > 3) {
            comm.sendNormalServerMessage(
               "Your rot: "
                  + Creature.normalizeAngle(performer.getStatus().getRotation())
                  + ", Wind rot="
                  + Server.getWeather().getWindRotation()
                  + ", pow="
                  + Server.getWeather().getWindPower()
                  + " x="
                  + Server.getWeather().getXWind()
                  + ", y="
                  + Server.getWeather().getYWind()
            );
            comm.sendNormalServerMessage("Tile is spring=" + Zone.hasSpring(tilex, tiley));
            if (performer.getPower() >= 5) {
               comm.sendNormalServerMessage("tilex: " + tilex + ", tiley=" + tiley);
            }

            if (t != null) {
               String villageName = "none";
               if (t.getVillage() > 0) {
                  try {
                     villageName = Villages.getVillage(t.getVillage()).getName();
                  } catch (NoSuchVillageException var26) {
                  }
               }

               comm.sendNormalServerMessage(
                  "A "
                     + t.getName()
                     + ", ql="
                     + t.getQualityLevel()
                     + " kingdom="
                     + Kingdoms.getNameFor(t.getKingdom())
                     + ", vill="
                     + villageName
                     + ", rotdam="
                     + t.getRotDamage()
                     + " firedam="
                     + t.getFireDamage()
                     + " speed="
                     + t.getSpeedBon()
               );
            }
         } else if (t != null && (t.getKingdom() == performer.getKingdomId() || performer.getDetectDangerBonus() > 0.0F)) {
            String qlString = "average";
            if (t.getQualityLevel() < 20) {
               qlString = "low";
            } else if (t.getQualityLevel() > 80) {
               qlString = "deadly";
            } else if (t.getQualityLevel() > 50) {
               qlString = "high";
            }

            String villageName = ".";
            if (t.getVillage() > 0) {
               try {
                  villageName = " of " + Villages.getVillage(t.getVillage()).getName() + ".";
               } catch (NoSuchVillageException var25) {
               }
            }

            String rotDam = "";
            if (t.getRotDamage() > 0) {
               rotDam = " It has ugly black-green speckles.";
            }

            String fireDam = "";
            if (t.getFireDamage() > 0) {
               fireDam = " It has the rune of fire.";
            }

            StringBuilder buf = new StringBuilder();
            buf.append("You detect a ");
            buf.append(t.getName());
            buf.append(" here, of ");
            buf.append(qlString);
            buf.append(" quality.");
            buf.append(" It has been set by people from ");
            buf.append(Kingdoms.getNameFor(t.getKingdom()));
            buf.append(villageName);
            buf.append(rotDam);
            buf.append(fireDam);
            comm.sendNormalServerMessage(buf.toString());
         }
      } else if (action == 141) {
         if (Tiles.decodeHeight(tile) > 400 && performer.getDeity() != null && performer.getDeity().isMountainGod()) {
            done = MethodsReligion.pray(act, performer, counter);
         }
      } else if (action == 78) {
         HighwayPos highwaypos = MethodsHighways.getHighwayPos(tilex, tiley, onSurface);
         if (highwaypos != null && MethodsHighways.onHighway(highwaypos)) {
            return true;
         }

         boolean cultistSpawn = Methods.isActionAllowed(performer, (short)384) && performer.getCultist() != null && performer.getCultist().maySpawnVolcano();
         if (cultistSpawn || performer.getPower() >= 5) {
            if (cultistSpawn) {
               if (isHoleNear(tilex, tiley)) {
                  performer.getCommunicator().sendNormalServerMessage("A cave entrance is too close.");
                  return true;
               }

               if (Zones.getKingdom(tilex, tiley) != performer.getKingdomId()) {
                  performer.getCommunicator().sendNormalServerMessage("Nothing happens. Maybe you can not spawn lava too far from your own kingdom?");
                  return true;
               }

               try {
                  FaithZone fz = Zones.getFaithZone(tilex, tiley, performer.isOnSurface());
                  if (fz != null && fz.getCurrentRuler() != null && fz.getCurrentRuler().number != 2) {
                     performer.getCommunicator().sendNormalServerMessage("Nothing happens. Maybe you can not spawn lava too far from Magranon's domain?");
                     return true;
                  }
               } catch (NoSuchZoneException var27) {
                  performer.getCommunicator().sendNormalServerMessage("Nothing happens. Maybe you can not spawn lava too far from Magranon's domain?");
                  return true;
               }

               if (!Methods.isActionAllowed(performer, (short)547, tilex, tiley)) {
                  return true;
               }

               done = false;
               if (counter == 1.0F) {
                  int sx = Zones.safeTileX(tilex - 1);
                  int sy = Zones.safeTileX(tiley - 1);
                  int ey = Zones.safeTileX(tiley + 1);
                  int ex = Zones.safeTileX(tilex + 1);

                  for(int x = sx; x <= ex; ++x) {
                     for(int y = sy; y <= ey; ++y) {
                        VolaTile tt = Zones.getTileOrNull(x, y, onSurface);
                        if (tt != null) {
                           Item[] its = tt.getItems();

                           for(Item i : its) {
                              if (i.isNoTake()) {
                                 performer.getCommunicator().sendNormalServerMessage("The " + i.getName() + " blocks your efforts.");
                                 return true;
                              }
                           }
                        }
                     }
                  }

                  performer.getCommunicator().sendNormalServerMessage("You start concentrating on the rock.");
                  Server.getInstance().broadCastAction(performer.getName() + " starts to look intensely on the rock.", performer, 5);
                  if (cultistSpawn) {
                     performer.sendActionControl("Erupting", true, 400);
                  }
               }
            }

            if (!cultistSpawn || counter > 40.0F) {
               done = true;
               int caveTile = Server.caveMesh.getTile(tilex, tiley);
               byte type = Tiles.decodeType(caveTile);
               if (Tiles.isSolidCave(type) && !Tiles.getTile(type).isReinforcedCave()) {
                  performer.getCommunicator().sendNormalServerMessage("The rock starts to bubble with lava.");
                  Server.getInstance().broadCastAction(performer.getName() + " makes the rock boil with red hot lava.", performer, 5);
                  int height = Tiles.decodeHeight(tile);
                  TileEvent.log(tilex, tiley, 0, performer.getWurmId(), action);
                  int nh = height + 4;
                  if (cultistSpawn) {
                     performer.getCultist().touchCooldown2();
                  }

                  Server.setSurfaceTile(tilex, tiley, (short)nh, Tiles.Tile.TILE_LAVA.id, (byte)0);

                  for(int xx = 0; xx <= 1; ++xx) {
                     for(int yy = 0; yy <= 1; ++yy) {
                        try {
                           int tempint3 = Tiles.decodeHeight(Server.surfaceMesh.getTile(tilex + xx, tiley + yy));
                           Server.rockMesh.setTile(tilex + xx, tiley + yy, Tiles.encode((short)tempint3, Tiles.Tile.TILE_ROCK.id, (byte)0));
                        } catch (Exception var24) {
                        }
                     }
                  }

                  Terraforming.setAsRock(tilex, tiley, false, true);
               } else {
                  performer.getCommunicator().sendNormalServerMessage("Nothing happens.");
               }
            }
         }
      } else {
         done = super.action(act, performer, tilex, tiley, onSurface, tile, action, counter);
      }

      return done;
   }

   @Override
   public boolean action(
      Action act, Creature performer, Item source, int tilex, int tiley, boolean onSurface, int heightOffset, int tile, short action, float counter
   ) {
      boolean done = true;
      if (action != 518 || source.getTemplateId() != 782 && (performer.getPower() < 4 || source.getTemplateId() != 176)) {
         if (source.isMiningtool() && action == 227) {
            if (tilex < 0 || tilex > 1 << Constants.meshSize || tiley < 0 || tiley > 1 << Constants.meshSize) {
               performer.getCommunicator().sendNormalServerMessage("The water is too deep to mine.", (byte)3);
               return true;
            }

            if (Zones.isTileProtected(tilex, tiley)) {
               performer.getCommunicator().sendNormalServerMessage("This tile is protected by the gods. You can not mine here.", (byte)3);
               return true;
            }

            short h = Tiles.decodeHeight(tile);
            if (h > -24) {
               boolean makingWideTunnel = false;
               if (isHoleNear(tilex, tiley)) {
                  if (!canHaveWideEntrance(performer, tilex, tiley)) {
                     performer.getCommunicator().sendNormalServerMessage("Another tunnel is too close. It would collapse.");
                     return true;
                  }

                  makingWideTunnel = true;
               }

               Point lowestCorner = findLowestCorner(performer, tilex, tiley);
               if (lowestCorner == null) {
                  return true;
               }

               Point nextLowestCorner = findNextLowestCorner(performer, tilex, tiley, lowestCorner);
               if (nextLowestCorner == null) {
                  return true;
               }

               Point highestCorner = findHighestCorner(tilex, tiley);
               if (highestCorner == null) {
                  return false;
               }

               Point nextHighestCorner = findNextHighestCorner(tilex, tiley, highestCorner);
               if (nextHighestCorner == null) {
                  return false;
               }

               if (nextLowestCorner.getH() != lowestCorner.getH() && isStructureNear(nextLowestCorner.getX(), nextLowestCorner.getY())
                  || nextHighestCorner.getH() != highestCorner.getH() && isStructureNear(highestCorner.getX(), highestCorner.getY())) {
                  performer.getCommunicator().sendNormalServerMessage("Cannot create a tunnel here as there is a structure too close.", (byte)3);
                  return true;
               }

               for(int x = -1; x <= 1; ++x) {
                  for(int y = -1; y <= 1; ++y) {
                     VolaTile svt = Zones.getTileOrNull(tilex + x, tiley + y, true);
                     Structure ss = svt == null ? null : svt.getStructure();
                     if (ss != null && ss.isTypeBridge()) {
                        performer.getCommunicator().sendNormalServerMessage("You can't tunnel here, there is a bridge in the way.");
                        return true;
                     }

                     VolaTile cvt = Zones.getTileOrNull(tilex + x, tiley + y, false);
                     Structure cs = cvt == null ? null : cvt.getStructure();
                     if (cs != null && cs.isTypeBridge()) {
                        performer.getCommunicator().sendNormalServerMessage("You can't tunnel here, there is a bridge in the way.");
                        return true;
                     }
                  }
               }

               done = false;
               Skills skills = performer.getSkills();
               Skill mining = null;
               Skill tool = null;
               boolean insta = performer.getPower() >= 2 && source.isWand();

               try {
                  mining = skills.getSkill(1008);
               } catch (Exception var49) {
                  mining = skills.learn(1008, 1.0F);
               }

               try {
                  tool = skills.getSkill(source.getPrimarySkill());
               } catch (Exception var48) {
                  try {
                     tool = skills.learn(source.getPrimarySkill(), 1.0F);
                  } catch (NoSuchSkillException var47) {
                     logger.log(Level.WARNING, performer.getName() + " trying to mine with an item with no primary skill: " + source.getName());
                  }
               }

               int time = 0;
               if (counter == 1.0F) {
                  time = Actions.getStandardActionTime(performer, mining, source, 0.0);

                  try {
                     performer.getCurrentAction().setTimeLeft(time);
                  } catch (NoSuchActionException var46) {
                     logger.log(Level.INFO, "This action does not exist?", (Throwable)var46);
                  }

                  if (affectsHighway(tilex, tiley)) {
                     performer.getCommunicator().sendNormalServerMessage("A surface highway interferes with your tunneling operation.", (byte)3);
                     return true;
                  }

                  if (!this.isOutInTunnelOkay(performer, tilex, tiley, makingWideTunnel)) {
                     return true;
                  }

                  Server.getInstance().broadCastAction(performer.getName() + " starts tunneling.", performer, 5);
                  performer.getCommunicator().sendNormalServerMessage("You start to tunnel.");
                  performer.sendActionControl(Actions.actionEntrys[227].getVerbString(), true, time);
                  source.setDamage(source.getDamage() + 0.0015F * source.getDamageModifier());
                  performer.getStatus().modifyStamina(-1000.0F);
               } else {
                  try {
                     time = performer.getCurrentAction().getTimeLeft();
                  } catch (NoSuchActionException var45) {
                     logger.log(Level.INFO, "This action does not exist?", (Throwable)var45);
                  }

                  if (counter * 10.0F <= (float)time && !insta) {
                     if (act.currentSecond() % 5 == 0 || act.currentSecond() == 3 && time < 50) {
                        String sstring = "sound.work.mining1";
                        int x = Server.rand.nextInt(3);
                        if (x == 0) {
                           sstring = "sound.work.mining2";
                        } else if (x == 1) {
                           sstring = "sound.work.mining3";
                        }

                        SoundPlayer.playSound(sstring, tilex, tiley, performer.isOnSurface(), 0.0F);
                        source.setDamage(source.getDamage() + 0.0015F * source.getDamageModifier());
                        performer.getStatus().modifyStamina(-7000.0F);
                     }
                  } else {
                     if (act.getRarity() != 0) {
                        performer.playPersonalSound("sound.fx.drumroll");
                     }

                     double bonus = 0.0;
                     double power = 0.0;
                     done = true;
                     int itemTemplateCreated = 146;
                     float diff = 1.0F;
                     int mineDir = getTunnelExit(tilex, tiley);
                     if (mineDir == -1) {
                        performer.getCommunicator().sendNormalServerMessage("The topology here makes it impossible to mine in a good way.", (byte)3);
                        return true;
                     }

                     byte state = Zones.getMiningState(tilex, tiley);
                     if (state == -1) {
                        performer.getCommunicator().sendNormalServerMessage("You cannot keep mining here. The rock is unusually hard.", (byte)3);
                        return true;
                     }

                     if (affectsHighway(tilex, tiley)) {
                        performer.getCommunicator().sendNormalServerMessage("A surface highway interferes with your tunneling operation.", (byte)3);
                        return true;
                     }

                     if (state < Math.max(1, Servers.localServer.getTunnelingHits()) + Server.rand.nextInt(10) && !insta) {
                        if (!areAllTilesRockOrReinforcedRock(tilex, tiley, tile, mineDir, true, makingWideTunnel)) {
                           performer.getCommunicator()
                              .sendNormalServerMessage("The ground sounds strangely hollow and brittle. You have to abandon the mining operation.", (byte)3);
                           return true;
                        }

                        if (!this.isOutInTunnelOkay(performer, tilex, tiley, makingWideTunnel)) {
                           return true;
                        }
                     } else {
                        int t = Server.caveMesh.getTile(tilex, tiley);
                        if (Tiles.isReinforcedCave(Tiles.decodeType(t))) {
                           performer.getCommunicator().sendNormalServerMessage("You cannot keep mining here. The rock is unusually hard.", (byte)3);
                           return true;
                        }

                        Zones.deleteMiningTile(tilex, tiley);
                        if (!areAllTilesRockOrReinforcedRock(tilex, tiley, tile, mineDir, true, makingWideTunnel)) {
                           performer.getCommunicator()
                              .sendNormalServerMessage("The ground sounds strangely hollow and brittle. You have to abandon the mining operation.", (byte)3);
                           return true;
                        }

                        int drop = -20;
                        if (makingWideTunnel) {
                           MeshTile mTileCurrent = new MeshTile(Server.surfaceMesh, tilex, tiley);
                           MeshTile mCaveCurrent = new MeshTile(Server.caveMesh, tilex, tiley);
                           MeshTile mTileNorth = mTileCurrent.getNorthMeshTile();
                           if (mTileNorth.isHole()) {
                              MeshTile mCaveNorth = mCaveCurrent.getNorthMeshTile();
                              drop = -Math.abs(mCaveNorth.getSouthSlope());
                           }

                           MeshTile mTileWest = mTileCurrent.getWestMeshTile();
                           if (mTileWest.isHole()) {
                              MeshTile mCaveWest = mCaveCurrent.getWestMeshTile();
                              drop = -Math.abs(mCaveWest.getEastSlope());
                           }

                           MeshTile mTileSouth = mTileCurrent.getSouthMeshTile();
                           if (mTileSouth.isHole()) {
                              MeshTile mCaveSouth = mCaveCurrent.getSouthMeshTile();
                              drop = -Math.abs(mCaveSouth.getNorthSlope());
                           }

                           MeshTile mTileEast = mTileCurrent.getEastMeshTile();
                           if (mTileEast.isHole()) {
                              MeshTile mCaveEast = mCaveCurrent.getEastMeshTile();
                              drop = -Math.abs(mCaveEast.getWestSlope());
                           }
                        }

                        if (!createOutInTunnel(tilex, tiley, tile, performer, drop)) {
                           return true;
                        }
                     }

                     if (state > 10) {
                        int t = Server.caveMesh.getTile(tilex, tiley);
                        if (Tiles.isReinforcedCave(Tiles.decodeType(t))) {
                           performer.getCommunicator().sendNormalServerMessage("You cannot keep mining here. The rock is unusually hard.", (byte)3);
                           return true;
                        }
                     }

                     if (state < 76) {
                        Zones.setMiningState(tilex, tiley, ++state, false);
                        if (state > Servers.localServer.getTunnelingHits()) {
                           performer.getCommunicator().sendNormalServerMessage("You will soon create an entrance.");
                        }
                     }

                     if (tool != null) {
                        bonus = tool.skillCheck(1.0, source, 0.0, false, counter) / 5.0;
                     }

                     power = Math.max(1.0, mining.skillCheck(1.0, source, bonus, false, counter));
                     if (performer.getTutorialLevel() == 10 && !performer.skippedTutorial()) {
                        performer.missionFinished(true, true);
                     }

                     if (Server.rand.nextInt(5) == 0) {
                        try {
                           if (mining.getKnowledge(0.0) < power) {
                              power = mining.getKnowledge(0.0);
                           }

                           rockRandom.setSeed((long)(tilex + tiley * Zones.worldTileSizeY) * 789221L);
                           int m = 100;
                           double imbueEnhancement = 1.0 + 0.23047 * (double)source.getSkillSpellImprovement(1008) / 100.0;
                           float modifier = 1.0F;
                           if (source.getSpellEffects() != null) {
                              modifier *= source.getSpellEffects().getRuneEffect(RuneUtilities.ModifierEffect.ENCH_RESGATHERED);
                           }

                           int max = (int)Math.min(
                              100.0, 20.0 + (double)rockRandom.nextInt(80) * imbueEnhancement * (double)modifier + (double)source.getRarity()
                           );
                           power = Math.min(power, (double)max);
                           if (source.isCrude()) {
                              power = 1.0;
                           }

                           Item newItem = ItemFactory.createItem(
                              146,
                              (float)power,
                              performer.getPosX(),
                              performer.getPosY(),
                              Server.rand.nextFloat() * 360.0F,
                              performer.isOnSurface(),
                              act.getRarity(),
                              -10L,
                              null
                           );
                           newItem.setLastOwnerId(performer.getWurmId());
                           newItem.setDataXY(tilex, tiley);
                           performer.getCommunicator().sendNormalServerMessage("You mine some " + newItem.getName() + ".");
                           Server.getInstance().broadCastAction(performer.getName() + " mines some " + newItem.getName() + ".", performer, 5);
                           createGem(tilex, tiley, performer, power, true, act);
                        } catch (Exception var44) {
                           logger.log(Level.WARNING, "Factory failed to produce item", (Throwable)var44);
                        }
                     } else {
                        performer.getCommunicator().sendNormalServerMessage("You chip away at the rock.");
                     }
                  }
               }
            } else {
               performer.getCommunicator().sendNormalServerMessage("The water is too deep to mine.", (byte)3);
            }
         } else if (source.isMiningtool() && action == 145) {
            int digTilex = (int)performer.getStatus().getPositionX() + 2 >> 2;
            int digTiley = (int)performer.getStatus().getPositionY() + 2 >> 2;
            done = mine(act, performer, source, tilex, tiley, action, counter, digTilex, digTiley);
         } else if (source.isMiningtool() && action == 156) {
            if (tilex < 0 || tilex > 1 << Constants.meshSize || tiley < 0 || tiley > 1 << Constants.meshSize) {
               performer.getCommunicator().sendNormalServerMessage("The water is too deep to prospect.", (byte)3);
               return true;
            }

            float h = (float)Tiles.decodeHeight(tile);
            if (h > -25.0F) {
               Skills skills = performer.getSkills();
               Skill prospecting = null;
               done = false;

               try {
                  prospecting = skills.getSkill(10032);
               } catch (Exception var43) {
                  prospecting = skills.learn(10032, 1.0F);
               }

               int time = 0;
               if (counter == 1.0F) {
                  String sstring = "sound.work.prospecting1";
                  int x = Server.rand.nextInt(3);
                  if (x == 0) {
                     sstring = "sound.work.prospecting2";
                  } else if (x == 1) {
                     sstring = "sound.work.prospecting3";
                  }

                  SoundPlayer.playSound(sstring, tilex, tiley, performer.isOnSurface(), 1.0F);
                  time = (int)Math.max(30.0, 100.0 - prospecting.getKnowledge(source, 0.0));

                  try {
                     performer.getCurrentAction().setTimeLeft(time);
                  } catch (NoSuchActionException var42) {
                     logger.log(Level.INFO, "This action does not exist?", (Throwable)var42);
                  }

                  performer.getCommunicator().sendNormalServerMessage("You start to gather fragments of the rock.");
                  Server.getInstance().broadCastAction(performer.getName() + " starts gathering fragments of the rock.", performer, 5);
                  performer.sendActionControl(Actions.actionEntrys[156].getVerbString(), true, time);
               } else {
                  try {
                     time = performer.getCurrentAction().getTimeLeft();
                  } catch (NoSuchActionException var41) {
                     logger.log(Level.INFO, "This action does not exist?", (Throwable)var41);
                  }
               }

               if (counter * 10.0F > (float)time) {
                  performer.getStatus().modifyStamina(-3000.0F);
                  prospecting.skillCheck(1.0, source, 0.0, false, counter);
                  source.setDamage(source.getDamage() + 5.0E-4F * source.getDamageModifier());
                  done = true;
                  String findString = "only rock";
                  LinkedList<String> list = new LinkedList<>();
                  int m = 100;
                  boolean saltExists = false;
                  boolean flintExists = false;

                  for(int x = -3; x <= 3; ++x) {
                     for(int y = -3; y <= 3; ++y) {
                        int resource = Server.getCaveResource(tilex + x, tiley + y);
                        findString = "";
                        if (resource == 65535) {
                           resource = Server.rand.nextInt(10000);
                           Server.setCaveResource(tilex + x, tiley + y, resource);
                        }

                        int itemTemplate = getItemTemplateForTile(Tiles.decodeType(Server.caveMesh.getTile(tilex + x, tiley + y)));
                        if (itemTemplate != 146) {
                           try {
                              ItemTemplate t = ItemTemplateFactory.getInstance().getTemplate(itemTemplate);
                              String qlstring = "";
                              if (prospecting.getKnowledge(0.0) > 20.0) {
                                 rockRandom.setSeed((long)(tilex + x + (tiley + y) * Zones.worldTileSizeY) * 789221L);
                                 int max = Math.min(100, 20 + rockRandom.nextInt(80));
                                 qlstring = " (" + getShardQlDescription(max) + ")";
                              }

                              findString = t.getProspectName() + qlstring;
                           } catch (NoSuchTemplateException var40) {
                              logger.log(
                                 Level.WARNING,
                                 performer.getName() + " - " + var40.getMessage() + ": " + itemTemplate + " at " + tilex + ", " + tiley,
                                 (Throwable)var40
                              );
                           }
                        }

                        if (prospecting.getKnowledge(0.0) > 40.0) {
                           rockRandom.setSeed((long)(tilex + x + (tiley + y) * Zones.worldTileSizeY) * 102533L);
                           if (rockRandom.nextInt(100) == 0) {
                              saltExists = true;
                           }
                        }

                        if (prospecting.getKnowledge(0.0) > 20.0) {
                           rockRandom.setSeed((long)(tilex + x + (tiley + y) * Zones.worldTileSizeY) * 6883L);
                           if (rockRandom.nextInt(200) == 0) {
                              flintExists = true;
                           }
                        }

                        if (findString.length() > 0 && !list.contains(findString)) {
                           if (Server.rand.nextBoolean()) {
                              list.addFirst(findString);
                           } else {
                              list.addLast(findString);
                           }
                        }
                     }
                  }

                  if (list.isEmpty()) {
                     findString = "only rock";
                  } else {
                     int x = 0;

                     for(Iterator<String> it = list.iterator(); it.hasNext(); ++x) {
                        if (x == 0) {
                           findString = it.next();
                        } else if (x == list.size() - 1) {
                           findString = findString + " and " + (String)it.next();
                        } else {
                           findString = findString + ", " + (String)it.next();
                        }
                     }
                  }

                  performer.getCommunicator().sendNormalServerMessage("There is " + findString + " nearby.");
                  if (saltExists) {
                     performer.getCommunicator().sendNormalServerMessage("You will find salt here!");
                  }

                  if (flintExists) {
                     performer.getCommunicator().sendNormalServerMessage("You will find flint here!");
                  }
               }
            } else {
               performer.getCommunicator().sendNormalServerMessage("The water is too deep to prospect.");
            }
         } else if (action != 141 && action != 78) {
            done = super.action(act, performer, source, tilex, tiley, onSurface, heightOffset, tile, action, counter);
         } else {
            done = this.action(act, performer, tilex, tiley, onSurface, tile, action, counter);
         }
      } else {
         int digTileX = (int)performer.getStatus().getPositionX() + 2 >> 2;
         int digTileY = (int)performer.getStatus().getPositionY() + 2 >> 2;
         done = CaveTileBehaviour.raiseRockLevel(performer, source, digTileX, digTileY, counter, act);
      }

      return done;
   }

   private static int getTunnelExit(int tilex, int tiley) {
      int lowestX = 100000;
      int lowestY = 100000;
      float lowestHeight = 100000.0F;
      int sameX = lowestX;
      int sameY = lowestY;
      int lowerCount = 0;

      for(int x = 0; x <= 1; ++x) {
         for(int y = 0; y <= 1; ++y) {
            int rockTile = Server.rockMesh.getTile(tilex + x, tiley + y);
            short rockHeight = Tiles.decodeHeight(rockTile);
            if (lowestHeight == 32767.0F) {
               lowestHeight = (float)rockHeight;
               lowestX = tilex + x;
               lowestY = tiley + y;
               lowerCount = 1;
            } else if ((float)rockHeight < lowestHeight) {
               lowestHeight = (float)rockHeight;
               lowestX = tilex + x;
               lowestY = tiley + y;
               lowerCount = 1;
            } else if ((float)rockHeight == lowestHeight) {
               sameX = tilex + x;
               sameY = tiley + y;
               ++lowerCount;
            }
         }
      }

      if (lowerCount > 2) {
         logger.log(Level.WARNING, "Bad tile at " + tilex + ", " + tiley);
         return -1;
      } else if (lowerCount == 2 && sameX - lowestX != 0 && sameY - lowestY != 0) {
         logger.log(Level.WARNING, "Bad tile at " + tilex + ", " + tiley);
         return -1;
      } else {
         int nsY = tiley + (1 - (lowestY - tiley));
         int nsRockTile = Server.rockMesh.getTile(lowestX, nsY);
         short nsRockHeight = Tiles.decodeHeight(nsRockTile);
         float nextLowestHeight = (float)nsRockHeight;
         int nextLowestX = lowestX;
         int nextLowestY = nsY;
         int weX = tilex + (1 - (lowestX - tilex));
         int weRockTile = Server.rockMesh.getTile(weX, lowestY);
         short weRockHeight = Tiles.decodeHeight(weRockTile);
         if ((float)weRockHeight < nextLowestHeight) {
            nextLowestHeight = (float)weRockHeight;
            nextLowestX = weX;
            nextLowestY = lowestY;
         } else if ((float)weRockHeight == nextLowestHeight) {
            logger.log(Level.WARNING, "Bad tile at " + tilex + ", " + tiley);
            return -1;
         }

         if (lowestX == tilex + 0) {
            if (lowestY == tiley + 0) {
               if (nextLowestX == tilex + 1) {
                  if (nextLowestY == tiley + 0) {
                     return 3;
                  }
               } else if (nextLowestY == tiley + 1) {
                  return 2;
               }
            } else if (lowestY == tiley + 1) {
               if (nextLowestX == tilex + 1) {
                  if (nextLowestY == tiley + 1) {
                     return 5;
                  }
               } else if (nextLowestY == tiley + 0) {
                  return 2;
               }
            }
         } else if (lowestY == tiley + 0) {
            if (nextLowestX == tilex + 1) {
               if (nextLowestY == tiley + 1) {
                  return 4;
               }
            } else if (nextLowestY == tiley + 0) {
               return 3;
            }
         } else if (lowestY == tiley + 1) {
            if (nextLowestX == tilex + 1) {
               if (nextLowestY == tiley + 0) {
                  return 4;
               }
            } else if (nextLowestY == tiley + 1) {
               return 5;
            }
         }

         logger.log(Level.WARNING, "Bad tile at " + tilex + ", " + tiley);
         return -1;
      }
   }

   private static void setTileToTransition(int tilex, int tiley) {
      VolaTile t = Zones.getTileOrNull(tilex, tiley, true);
      if (t != null) {
         t.isTransition = true;
      }

      t = Zones.getTileOrNull(tilex, tiley, false);
      if (t != null) {
         t.isTransition = true;
      }
   }

   private boolean isOutInTunnelOkay(Creature performer, int tilex, int tiley, boolean makingWideTunnel) {
      for(int x = -1; x <= 1; ++x) {
         for(int y = -1; y <= 1; ++y) {
            int tileNew = Server.surfaceMesh.getTile(tilex + x, tiley + y);
            if (Tiles.decodeType(tileNew) == Tiles.Tile.TILE_HOLE.id && !makingWideTunnel) {
               performer.getCommunicator().sendNormalServerMessage("Another tunnel is too close. It would collapse.", (byte)3);
               return false;
            }

            if (Tiles.isMineDoor(Tiles.decodeType(tileNew))) {
               performer.getCommunicator().sendNormalServerMessage("Cannot make a tunnel next to a mine door.");
               return false;
            }

            if (x >= 0 && y >= 0) {
               int rockTile = Server.rockMesh.getTile(tilex + x, tiley + y);
               short rockHeight = Tiles.decodeHeight(rockTile);
               int caveTile = Server.caveMesh.getTile(tilex + x, tiley + y);
               short cheight = Tiles.decodeHeight(caveTile);
               if (!isNullWall(caveTile) && rockHeight - cheight >= 255) {
                  performer.getCommunicator().sendNormalServerMessage("Not enough rock height to make a tunnel there.");
                  return false;
               }
            }
         }
      }

      return true;
   }

   static boolean isHoleNear(int tilex, int tiley) {
      MeshIO surfMesh = Server.surfaceMesh;

      for(int x = -1; x <= 1; ++x) {
         for(int y = -1; y <= 1; ++y) {
            int tileNew = surfMesh.getTile(tilex + x, tiley + y);
            if (Tiles.decodeType(tileNew) == Tiles.Tile.TILE_HOLE.id) {
               return true;
            }
         }
      }

      return false;
   }

   static boolean canHaveWideEntrance(@Nullable Creature performer, int tilex, int tiley) {
      MeshIO surfMesh = Server.surfaceMesh;
      if (!hasValidNearbyEntrance(performer, surfMesh, tilex, tiley)) {
         return false;
      } else {
         MeshTile currentMT = new MeshTile(surfMesh, tilex, tiley);
         MeshTile mTileNorth = currentMT.getNorthMeshTile();
         if (mTileNorth.isHole()) {
            int dir = mTileNorth.getLowerLip();
            if (dir == 6) {
               if (currentMT.getWestSlope() != 0) {
                  if (performer != null) {
                     performer.getCommunicator()
                        .sendNormalServerMessage("Current tile needs a flat border to correspond to lower part of adjacent cave entrance.");
                  }

                  return false;
               }

               if (currentMT.getSouthSlope() <= 0) {
                  if (performer != null) {
                     performer.getCommunicator().sendNormalServerMessage("Current tile needs to be same orientation as adjacent cave entrance.");
                  }

                  return false;
               }

               return true;
            }

            if (dir == 2) {
               if (currentMT.getEastSlope() != 0) {
                  if (performer != null) {
                     performer.getCommunicator()
                        .sendNormalServerMessage("Current tile needs a flat border to correspond to lower part of adjacent cave entrance.");
                  }

                  return false;
               }

               if (currentMT.getSouthSlope() >= 0) {
                  if (performer != null) {
                     performer.getCommunicator().sendNormalServerMessage("Current tile needs to be same orientation as adjacent cave entrance.");
                  }

                  return false;
               }

               return true;
            }
         }

         MeshTile mTileWest = currentMT.getWestMeshTile();
         if (mTileWest.isHole()) {
            int dir = mTileWest.getLowerLip();
            if (dir == 0) {
               if (currentMT.getNorthSlope() != 0) {
                  if (performer != null) {
                     performer.getCommunicator()
                        .sendNormalServerMessage("Current tile needs a flat border to correspond to lower part of adjacent cave entrance.");
                  }

                  return false;
               }

               if (currentMT.getEastSlope() <= 0) {
                  if (performer != null) {
                     performer.getCommunicator().sendNormalServerMessage("Current tile needs to be same orientation as adjacent cave entrance.");
                  }

                  return false;
               }

               return true;
            }

            if (dir == 4) {
               if (currentMT.getSouthSlope() != 0) {
                  if (performer != null) {
                     performer.getCommunicator()
                        .sendNormalServerMessage("Current tile needs a flat border to correspond to lower part of adjacent cave entrance.");
                  }

                  return false;
               }

               if (currentMT.getEastSlope() >= 0) {
                  if (performer != null) {
                     performer.getCommunicator().sendNormalServerMessage("Current tile needs to be same orientation as adjacent cave entrance.");
                  }

                  return false;
               }

               return true;
            }
         }

         MeshTile mTileSouth = currentMT.getSouthMeshTile();
         if (mTileSouth.isHole()) {
            int dir = mTileSouth.getLowerLip();
            if (dir == 6) {
               if (currentMT.getWestSlope() != 0) {
                  if (performer != null) {
                     performer.getCommunicator()
                        .sendNormalServerMessage("Current tile needs a flat border to correspond to lower part of adjacent cave entrance.");
                  }

                  return false;
               }

               if (currentMT.getNorthSlope() <= 0) {
                  if (performer != null) {
                     performer.getCommunicator().sendNormalServerMessage("Current tile needs to be same orientation as adjacent cave entrance.");
                  }

                  return false;
               }

               return true;
            }

            if (dir == 2) {
               if (currentMT.getEastSlope() != 0) {
                  if (performer != null) {
                     performer.getCommunicator()
                        .sendNormalServerMessage("Current tile needs a flat border to correspond to lower part of adjacent cave entrance.");
                  }

                  return false;
               }

               if (currentMT.getNorthSlope() >= 0) {
                  if (performer != null) {
                     performer.getCommunicator().sendNormalServerMessage("Current tile needs to be same orientation as adjacent cave entrance.");
                  }

                  return false;
               }

               return true;
            }
         }

         MeshTile mTileEast = currentMT.getEastMeshTile();
         if (mTileEast.isHole()) {
            int dir = mTileEast.getLowerLip();
            if (dir == 0) {
               if (currentMT.getNorthSlope() != 0) {
                  if (performer != null) {
                     performer.getCommunicator()
                        .sendNormalServerMessage("Current tile needs a flat border to correspond to lower part of adjacent cave entrance.");
                  }

                  return false;
               }

               if (currentMT.getWestSlope() <= 0) {
                  if (performer != null) {
                     performer.getCommunicator().sendNormalServerMessage("Current tile needs to be same orientation as adjacent cave entrance.");
                  }

                  return false;
               }

               return true;
            }

            if (dir == 4) {
               if (currentMT.getSouthSlope() != 0) {
                  if (performer != null) {
                     performer.getCommunicator()
                        .sendNormalServerMessage("Current tile needs a flat border to correspond to lower part of adjacent cave entrance.");
                  }

                  return false;
               }

               if (currentMT.getWestSlope() >= 0) {
                  if (performer != null) {
                     performer.getCommunicator().sendNormalServerMessage("Current tile needs to be same orientation as adjacent cave entrance.");
                  }

                  return false;
               }

               return true;
            }
         }

         return false;
      }
   }

   private static boolean hasValidNearbyEntrance(@Nullable Creature performer, MeshIO surfMesh, int tilex, int tiley) {
      int holeX = -1;
      int holeY = -1;
      int holeXX = -1;
      int holeYY = -1;

      for(int x = -1; x <= 1; ++x) {
         for(int y = -1; y <= 1; ++y) {
            if (x != 0 && y != 0) {
               int tileNew = surfMesh.getTile(tilex + x, tiley + y);
               byte type = Tiles.decodeType(tileNew);
               if (type == Tiles.Tile.TILE_HOLE.id) {
                  if (performer != null) {
                     performer.getCommunicator().sendNormalServerMessage("Cannot have cave entrances meeting diagonally.");
                  }

                  return false;
               }
            }
         }
      }

      for(int x = -1; x <= 1; ++x) {
         for(int y = -1; y <= 1; ++y) {
            if (x != 0 || y != 0) {
               int tileNew = surfMesh.getTile(tilex + x, tiley + y);
               byte type = Tiles.decodeType(tileNew);
               if (Tiles.isMineDoor(type)) {
                  if (performer != null) {
                     performer.getCommunicator().sendNormalServerMessage("Cannot make a tunnel next to a mine door.");
                  }

                  return false;
               }
            }
         }
      }

      for(int x = -1; x <= 1; ++x) {
         for(int y = -1; y <= 1; ++y) {
            if ((x != 0 || y != 0) && (x == 0 || y == 0)) {
               int tileNew = surfMesh.getTile(tilex + x, tiley + y);
               if (Tiles.decodeType(tileNew) == Tiles.Tile.TILE_HOLE.id) {
                  if (holeX != -1) {
                     if (performer != null) {
                        performer.getCommunicator().sendNormalServerMessage("Can only make two or three tile wide cave entrances .");
                     }

                     return false;
                  }

                  holeX = tilex + x;
                  holeY = tiley + y;
               }
            }
         }
      }

      if (holeX == -1) {
         return true;
      } else {
         for(int xx = -1; xx <= 1; ++xx) {
            for(int yy = -1; yy <= 1; ++yy) {
               if (xx != 0 || yy != 0) {
                  int tileTwo = surfMesh.getTile(holeX + xx, holeY + yy);
                  if (Tiles.decodeType(tileTwo) == Tiles.Tile.TILE_HOLE.id) {
                     if (holeXX != -1) {
                        if (performer != null) {
                           performer.getCommunicator().sendNormalServerMessage("Can only make two or three tile wide cave entrances .");
                        }

                        return false;
                     }

                     holeXX = holeX + xx;
                     holeYY = holeY + yy;
                     if (tilex + xx + xx != holeXX || tiley + yy + yy != holeYY) {
                        if (performer != null) {
                           performer.getCommunicator().sendNormalServerMessage("Can only make two or three tile wide cave entrances .");
                        }

                        return false;
                     }
                  }
               }
            }
         }

         if (holeXX == -1) {
            return true;
         } else {
            for(int xxx = -1; xxx <= 1; ++xxx) {
               for(int yyy = -1; yyy <= 1; ++yyy) {
                  if (xxx != 0 || yyy != 0) {
                     int tileThree = surfMesh.getTile(holeXX + xxx, holeYY + yyy);
                     if (Tiles.decodeType(tileThree) == Tiles.Tile.TILE_HOLE.id && (holeXX + xxx != holeX || holeYY + yyy != holeY)) {
                        if (performer != null) {
                           performer.getCommunicator().sendNormalServerMessage("Can only make two or three tile wide cave entrances .");
                        }

                        return false;
                     }
                  }
               }
            }

            return true;
         }
      }
   }

   static boolean isStructureNear(int tilex, int tiley) {
      for(int x = -1; x <= 0; ++x) {
         for(int y = -1; y <= 0; ++y) {
            VolaTile vt = Zones.getTileOrNull(tilex + x, tiley + y, true);
            if (vt != null && vt.getStructure() != null) {
               return true;
            }

            VolaTile vtc = Zones.getTileOrNull(tilex + x, tiley + y, false);
            if (vtc != null && vtc.getStructure() != null) {
               return true;
            }
         }
      }

      return false;
   }

   static boolean createOutInTunnel(int tilex, int tiley, int tile, Creature performer, int mod) {
      MeshIO surfmesh = Server.surfaceMesh;
      MeshIO cavemesh = Server.caveMesh;
      VolaTile t = Zones.getTileOrNull(tilex, tiley, true);
      if (t != null) {
         Item[] items = t.getItems();

         for(Item lItem : items) {
            if (lItem.isDecoration()) {
               performer.getCommunicator()
                  .sendNormalServerMessage(LoginHandler.raiseFirstLetter(lItem.getNameWithGenus()) + " on the surface disturbs your operation.");
               return false;
            }
         }

         if (t.getStructure() != null) {
            performer.getCommunicator().sendNormalServerMessage("You can't tunnel here, there is a structure in the way.");
            return false;
         }
      }

      boolean makingWideTunnel = false;
      if (isHoleNear(tilex, tiley)) {
         if (!canHaveWideEntrance(performer, tilex, tiley)) {
            performer.getCommunicator().sendNormalServerMessage("Another tunnel is too close. It would collapse.");
            return false;
         }

         makingWideTunnel = true;
      }

      if (affectsHighway(tilex, tiley)) {
         performer.getCommunicator().sendNormalServerMessage("A surface highway interferes with your tunneling operation.", (byte)3);
         return false;
      } else {
         Point lowestCorner = findLowestCorner(performer, tilex, tiley);
         if (lowestCorner == null) {
            return false;
         } else {
            Point nextLowestCorner = findNextLowestCorner(performer, tilex, tiley, lowestCorner);
            if (nextLowestCorner == null) {
               return false;
            } else {
               Point highestCorner = findHighestCorner(tilex, tiley);
               if (highestCorner == null) {
                  return false;
               } else {
                  Point nextHighestCorner = findNextHighestCorner(tilex, tiley, highestCorner);
                  if (nextHighestCorner == null) {
                     return false;
                  } else if ((nextLowestCorner.getH() == lowestCorner.getH() || !isStructureNear(nextLowestCorner.getX(), nextLowestCorner.getY()))
                     && (nextHighestCorner.getH() == highestCorner.getH() || !isStructureNear(highestCorner.getX(), highestCorner.getY()))) {
                     for(int x = -1; x <= 1; ++x) {
                        for(int y = -1; y <= 1; ++y) {
                           VolaTile svt = Zones.getTileOrNull(tilex + x, tiley + y, true);
                           Structure ss = svt == null ? null : svt.getStructure();
                           if (ss != null && ss.isTypeBridge()) {
                              performer.getCommunicator().sendNormalServerMessage("You can't tunnel here, there is a bridge in the way.");
                              return false;
                           }

                           VolaTile cvt = Zones.getTileOrNull(tilex + x, tiley + y, false);
                           Structure cs = cvt == null ? null : cvt.getStructure();
                           if (cs != null && cs.isTypeBridge()) {
                              performer.getCommunicator().sendNormalServerMessage("You can't tunnel here, there is a bridge in the way.");
                              return false;
                           }
                        }
                     }

                     int nsY = tiley + (1 - (nextLowestCorner.getY() - tiley));
                     int weX = tilex + (1 - (nextLowestCorner.getX() - tilex));
                     int nsCorner = surfmesh.getTile(nextLowestCorner.getX(), nsY);
                     if (!mayLowerCornerOnSlope(lowestCorner.getH(), performer, nsCorner)) {
                        return false;
                     } else {
                        int weCorner = surfmesh.getTile(weX, nextLowestCorner.getY());
                        if (!mayLowerCornerOnSlope(lowestCorner.getH(), performer, weCorner)) {
                           return false;
                        } else if (Tiles.isReinforcedCave(Tiles.decodeType(cavemesh.getTile(tilex, tiley)))) {
                           return false;
                        } else {
                           if (makingWideTunnel) {
                              performer.getCommunicator().sendNormalServerMessage("You expand a tunnel entrance!");
                           } else {
                              performer.getCommunicator().sendNormalServerMessage("You create a tunnel entrance!");
                           }

                           short targetHeight = (short)lowestCorner.getH();

                           for(int x = tilex; x <= tilex + 1; ++x) {
                              for(int y = tiley; y <= tiley + 1; ++y) {
                                 int tileNew = cavemesh.getTile(x, y);
                                 int rockTile = Server.rockMesh.getTile(x, y);
                                 short rockHeight = Tiles.decodeHeight(rockTile);
                                 int surfTile = Server.surfaceMesh.getTile(x, y);
                                 short surfHeight = Tiles.decodeHeight(surfTile);
                                 if (x == tilex && y == tiley) {
                                    if ((x != lowestCorner.getX() || y != lowestCorner.getY())
                                       && (x != nextLowestCorner.getX() || y != nextLowestCorner.getY())) {
                                       int[] newfloorceil = getFloorAndCeiling(x, y, targetHeight, mod, false, true, performer);
                                       int newFloorHeight = newfloorceil[0];
                                       int newCeil = newfloorceil[1];
                                       if (Tiles.decodeType(tileNew) != Tiles.Tile.TILE_CAVE_WALL.id
                                          && Tiles.decodeType(tileNew) != Tiles.Tile.TILE_CAVE_WALL_ROCKSALT.id) {
                                          cavemesh.setTile(
                                             x, y, Tiles.encode((short)newFloorHeight, Tiles.decodeType(tileNew), (byte)(newCeil - newFloorHeight))
                                          );
                                       } else {
                                          cavemesh.setTile(
                                             x, y, Tiles.encode((short)newFloorHeight, Tiles.Tile.TILE_CAVE_EXIT.id, (byte)(newCeil - newFloorHeight))
                                          );
                                          VolaTile surft = Zones.getTileOrNull(x, y, true);
                                          if (surft != null) {
                                             surft.isTransition = true;
                                          }

                                          VolaTile cavet = Zones.getTileOrNull(x, y, false);
                                          if (cavet != null) {
                                             cavet.isTransition = true;
                                          }
                                       }
                                    } else {
                                       int[] newfloorceil = getFloorAndCeiling(x, y, targetHeight, 0, true, false, performer);
                                       int newFloorHeight = newfloorceil[0];
                                       cavemesh.setTile(x, y, Tiles.encode((short)newFloorHeight, Tiles.Tile.TILE_CAVE_EXIT.id, (byte)0));
                                       VolaTile surft = Zones.getTileOrNull(x, y, true);
                                       if (surft != null) {
                                          surft.isTransition = true;
                                       }

                                       VolaTile cavet = Zones.getTileOrNull(x, y, false);
                                       if (cavet != null) {
                                          cavet.isTransition = true;
                                       }

                                       if (rockHeight != newFloorHeight || surfHeight != newFloorHeight) {
                                          Server.rockMesh.setTile(x, y, Tiles.encode((short)newFloorHeight, (short)0));
                                          surfmesh.setTile(x, y, Tiles.encode((short)newFloorHeight, Tiles.decodeTileData(surfTile)));
                                          Players.getInstance().sendChangedTile(x, y, true, true);
                                       }
                                    }
                                 } else if (x == lowestCorner.getX() && y == lowestCorner.getY()
                                    || x == nextLowestCorner.getX() && y == nextLowestCorner.getY()) {
                                    int[] newfloorceil = getFloorAndCeiling(x, y, targetHeight, 0, true, false, performer);
                                    int newFloorHeight = newfloorceil[0];
                                    cavemesh.setTile(x, y, Tiles.encode((short)newFloorHeight, Tiles.decodeType(tileNew), (byte)0));
                                    if (rockHeight != newFloorHeight || surfHeight != newFloorHeight) {
                                       Server.rockMesh.setTile(x, y, Tiles.encode((short)newFloorHeight, (short)0));
                                       surfmesh.setTile(x, y, Tiles.encode((short)newFloorHeight, Tiles.decodeTileData(surfTile)));
                                       Players.getInstance().sendChangedTile(x, y, true, true);
                                    }
                                 } else {
                                    int[] newfloorceil = getFloorAndCeiling(x, y, targetHeight, mod, false, true, performer);
                                    int newFloorHeight = newfloorceil[0];
                                    int newCeil = newfloorceil[1];
                                    cavemesh.setTile(x, y, Tiles.encode((short)newFloorHeight, Tiles.decodeType(tileNew), (byte)(newCeil - newFloorHeight)));
                                 }

                                 Players.getInstance().sendChangedTile(x, y, false, true);

                                 for(int xx = -1; xx <= 0; ++xx) {
                                    for(int yy = -1; yy <= 0; ++yy) {
                                       try {
                                          Zone toCheckForChange = Zones.getZone(x + xx, y + yy, false);
                                          toCheckForChange.changeTile(x + xx, y + yy);
                                       } catch (NoSuchZoneException var30) {
                                          logger.log(Level.INFO, "no such zone?: " + (x + xx) + ", " + (y + yy), (Throwable)var30);
                                       }
                                    }
                                 }
                              }
                           }

                           setTileToTransition(tilex, tiley);
                           tile = Server.surfaceMesh.getTile(tilex, tiley);
                           surfmesh.setTile(tilex, tiley, Tiles.encode(Tiles.decodeHeight(tile), Tiles.Tile.TILE_HOLE.id, Tiles.decodeData(tile)));
                           short targetUpperHeight = (short)nextHighestCorner.getH();
                           short tileData = Tiles.decodeTileData(Server.surfaceMesh.getTile(highestCorner.getX(), highestCorner.getY()));
                           Server.surfaceMesh.setTile(highestCorner.getX(), highestCorner.getY(), Tiles.encode(targetUpperHeight, tileData));
                           tileData = Tiles.decodeTileData(Server.rockMesh.getTile(highestCorner.getX(), highestCorner.getY()));
                           Server.rockMesh.setTile(highestCorner.getX(), highestCorner.getY(), Tiles.encode(targetUpperHeight, tileData));
                           tileData = Tiles.decodeTileData(Server.caveMesh.getTile(highestCorner.getX(), highestCorner.getY()));
                           Players.getInstance().sendChangedTile(highestCorner.getX(), highestCorner.getY(), true, true);
                           Players.getInstance().sendChangedTile(highestCorner.getX(), highestCorner.getY(), false, true);
                           Players.getInstance().sendChangedTile(tilex, tiley, true, true);
                           VolaTile to = Zones.getOrCreateTile(tilex, tiley, true);
                           to.checkCaveOpening();
                           return true;
                        }
                     }
                  } else {
                     performer.getCommunicator().sendNormalServerMessage("Cannot create a tunnel here as there is a structure too close.", (byte)3);
                     return false;
                  }
               }
            }
         }
      }
   }

   @Nullable
   private static Point findLowestCorner(Creature performer, int tilex, int tiley) {
      int lowestX = 100000;
      int lowestY = 100000;
      short lowestHeight = 32767;

      for(int x = 0; x <= 1; ++x) {
         for(int y = 0; y <= 1; ++y) {
            int rockTile = Server.rockMesh.getTile(tilex + x, tiley + y);
            short rockHeight = Tiles.decodeHeight(rockTile);
            int caveTile = Server.caveMesh.getTile(tilex + x, tiley + y);
            short cheight = Tiles.decodeHeight(caveTile);
            if (!isNullWall(caveTile) && rockHeight - cheight >= 255) {
               performer.getCommunicator().sendNormalServerMessage("The mountainside would risk crumbling. You cannot tunnel here.");
               return null;
            }

            if (lowestHeight == 32767) {
               lowestHeight = rockHeight;
               lowestX = tilex + x;
               lowestY = tiley + y;
            } else if (rockHeight < lowestHeight) {
               lowestHeight = rockHeight;
               lowestX = tilex + x;
               lowestY = tiley + y;
            }
         }
      }

      return new Point(lowestX, lowestY, lowestHeight);
   }

   private static Point findNextLowestCorner(Creature performer, int tilex, int tiley, Point lowestCorner) {
      int nextLowestX = lowestCorner.getX();
      int nextLowestY = tiley + (1 - (lowestCorner.getY() - tiley));
      int nsRockTile = Server.rockMesh.getTile(nextLowestX, nextLowestY);
      short nextLowestHeight = Tiles.decodeHeight(nsRockTile);
      int weX = tilex + (1 - (lowestCorner.getX() - tilex));
      int weRockTile = Server.rockMesh.getTile(weX, lowestCorner.getY());
      short weRockHeight = Tiles.decodeHeight(weRockTile);
      if (weRockHeight < nextLowestHeight) {
         nextLowestHeight = weRockHeight;
         nextLowestX = weX;
         nextLowestY = lowestCorner.getY();
      }

      return new Point(nextLowestX, nextLowestY, nextLowestHeight);
   }

   public static Point findHighestCorner(int tilex, int tiley) {
      int highestX = 100000;
      int highestY = 100000;
      short highestHeight = 32767;

      for(int x = 0; x <= 1; ++x) {
         for(int y = 0; y <= 1; ++y) {
            int rockTile = Server.rockMesh.getTile(tilex + x, tiley + y);
            short rockHeight = Tiles.decodeHeight(rockTile);
            if (highestHeight == 32767) {
               highestHeight = rockHeight;
               highestX = tilex + x;
               highestY = tiley + y;
            } else if (rockHeight > highestHeight) {
               highestHeight = rockHeight;
               highestX = tilex + x;
               highestY = tiley + y;
            }
         }
      }

      return new Point(highestX, highestY, highestHeight);
   }

   public static Point findNextHighestCorner(int tilex, int tiley, Point highestCorner) {
      int nextHighestX = highestCorner.getX();
      int nextHighestY = tiley + (1 - (highestCorner.getY() - tiley));
      int nsRockTile = Server.rockMesh.getTile(nextHighestX, nextHighestY);
      short nextHighestHeight = Tiles.decodeHeight(nsRockTile);
      int weX = tilex + (1 - (highestCorner.getX() - tilex));
      int weRockTile = Server.rockMesh.getTile(weX, highestCorner.getY());
      short weRockHeight = Tiles.decodeHeight(weRockTile);
      if (weRockHeight > nextHighestHeight) {
         nextHighestHeight = weRockHeight;
         nextHighestX = weX;
         nextHighestY = highestCorner.getY();
      }

      return new Point(nextHighestX, nextHighestY, nextHighestHeight);
   }

   private static boolean mayLowerCornerOnSlope(int targetHeight, Creature performer, int checkedTile) {
      int nCHeight = Tiles.decodeHeight(checkedTile);
      if (nCHeight - targetHeight > 270) {
         performer.getCommunicator().sendNormalServerMessage("The mountainside would risk crumbling. You can't open a hole here.");
         return false;
      } else {
         return true;
      }
   }

   private static boolean areAllTilesRockOrReinforcedRock(int tilex, int tiley, int tile, int direction, boolean creatingExit, boolean makingWideTunnel) {
      boolean checkTile = false;
      int t = 0;
      byte type = 0;

      for(int x = -1; x <= 1; ++x) {
         for(int y = -1; y <= 1; ++y) {
            t = Server.caveMesh.getTile(tilex + x, tiley + y);
            type = Tiles.decodeType(t);
            if (direction == 3) {
               if (y <= 0) {
                  checkTile = true;
               }
            } else if (direction == 4) {
               if (x >= 0) {
                  checkTile = true;
               }
            } else if (direction == 5) {
               if (y >= 0) {
                  checkTile = true;
               }
            } else if (x <= 0) {
               checkTile = true;
            }

            if (checkTile
               && creatingExit
               && type != Tiles.Tile.TILE_CAVE_WALL.id
               && type != Tiles.Tile.TILE_CAVE_WALL_ROCKSALT.id
               && !Tiles.isReinforcedCave(type)
               && (type != Tiles.Tile.TILE_CAVE_EXIT.id || !makingWideTunnel)) {
               return false;
            }

            checkTile = false;
         }
      }

      return true;
   }

   static boolean isInsideTunnelOk(int tilex, int tiley, int tile, int action, int direction, Creature performer, boolean disintegrate) {
      if ((
            Tiles.decodeType(tile) == Tiles.Tile.TILE_CAVE_WALL.id
               || Tiles.decodeType(tile) == Tiles.Tile.TILE_CAVE_WALL_ROCKSALT.id
               || Server.getCaveResource(tilex, tiley) <= 0
               || disintegrate
         )
         && (Tiles.decodeHeight(tile) >= -25 || Tiles.decodeHeight(tile) == -100)) {
         int dir = 6;
         if (direction == 3) {
            dir = 0;
         } else if (direction == 5) {
            dir = 4;
         } else if (direction == 4) {
            dir = 2;
         }

         boolean[][] solids = new boolean[3][3];
         float minHeight = 1000000.0F;
         float maxHeight = 0.0F;
         float currHeight = 100000.0F;
         float currCeil = 0.0F;

         for(int x = -1; x <= 1; ++x) {
            for(int y = -1; y <= 1; ++y) {
               int t = Server.caveMesh.getTile(tilex + x, tiley + y);
               solids[x + 1][y + 1] = Tiles.isSolidCave(Tiles.decodeType(t));
               short height = Tiles.decodeHeight(t);
               int ceil = Tiles.decodeData(t) & 255;
               boolean setCurrHeight = false;
               boolean setExitheight = false;
               if (dir == 0) {
                  if ((x != 0 || y != 1) && (x != 1 || y != 1)) {
                     if (y == 0 && x >= 0) {
                        setExitheight = true;
                     }
                  } else {
                     setCurrHeight = true;
                  }
               } else if (dir == 2) {
                  if ((x != 0 || y != 0) && (x != 0 || y != 1)) {
                     if (x == 1 && y >= 0) {
                        setExitheight = true;
                     }
                  } else {
                     setCurrHeight = true;
                  }
               } else if (dir == 6) {
                  if ((x != 1 || y != 1) && (x != 1 || y != 0)) {
                     if (x == 0 && y >= 0) {
                        setExitheight = true;
                     }
                  } else {
                     setCurrHeight = true;
                  }
               } else if (dir == 4) {
                  if ((x != 0 || y != 0) && (x != 1 || y != 0)) {
                     if (y == 1 && x >= 0) {
                        setExitheight = true;
                     }
                  } else {
                     setCurrHeight = true;
                  }
               }

               if (setCurrHeight) {
                  if ((float)height < currHeight) {
                     currHeight = (float)height;
                  }

                  if ((float)(height + ceil) > currCeil) {
                     currCeil = (float)(height + ceil);
                  }
               }

               if (setExitheight && !isNullWall(t)) {
                  if ((float)height < minHeight) {
                     minHeight = (float)height;
                  }

                  if ((float)(height + ceil) > maxHeight) {
                     maxHeight = (float)(height + ceil);
                  }
               }
            }
         }

         if (!solids[0][0] && solids[1][0] && solids[0][1]) {
            performer.getCommunicator().sendNormalServerMessage("The cave walls sound hollow. A dangerous side shaft could emerge.");
            return false;
         } else if (!solids[2][0] && solids[2][1] && solids[1][0]) {
            performer.getCommunicator().sendNormalServerMessage("The cave walls sound hollow. A dangerous side shaft could emerge.");
            return false;
         } else if (!solids[0][2] && solids[1][2] && solids[0][1]) {
            performer.getCommunicator().sendNormalServerMessage("The cave walls sound hollow. A dangerous side shaft could emerge.");
            return false;
         } else if (!solids[2][2] && solids[1][2] && solids[2][1]) {
            performer.getCommunicator().sendNormalServerMessage("The cave walls sound hollow. A dangerous side shaft could emerge.");
            return false;
         } else {
            if (action == 147) {
               if (currHeight - 20.0F < minHeight) {
                  minHeight = currHeight - 20.0F;
               }
            } else if (action == 146 && currCeil + 20.0F > maxHeight) {
               maxHeight = currCeil + 20.0F;
            }

            if (maxHeight - minHeight > 254.0F) {
               performer.getCommunicator().sendNormalServerMessage("A dangerous crack is starting to form on the floor. You will have to find another way.");
               return false;
            } else {
               if (maxHeight - minHeight > 100.0F) {
                  performer.getCommunicator()
                     .sendNormalServerMessage("You hear falling rocks from the other side of the wall. A deep shaft will probably emerge.");
               }

               return true;
            }
         }
      } else {
         return false;
      }
   }

   private static boolean wouldPassThroughRockLayer(int tilex, int tiley, int tile, int action) {
      int maxCaveFloor = -100000;
      int minRockHeight = 100000;

      for(int x = 0; x <= 1; ++x) {
         for(int y = 0; y <= 1; ++y) {
            tile = Server.caveMesh.getTile(tilex + x, tiley + y);
            short ht = Tiles.decodeHeight(tile);
            boolean allSolid = true;
            if (ht != -100) {
               for(int xx = -1; xx <= 0 && allSolid; ++xx) {
                  for(int yy = -1; yy <= 0 && allSolid; ++yy) {
                     int encodedTile = Server.caveMesh.getTile(tilex + x + xx, tiley + y + yy);
                     byte type = Tiles.decodeType(encodedTile);
                     if (!Tiles.isSolidCave(type)) {
                        allSolid = false;
                     }
                  }
               }

               if (allSolid) {
                  ht = -100;
                  Server.caveMesh.setTile(tilex + x, tiley + y, Tiles.encode(ht, Tiles.decodeType(tile), (byte)0));
               }
            }

            if (ht > maxCaveFloor) {
               maxCaveFloor = ht;
            }
         }
      }

      for(int x = 0; x <= 1; ++x) {
         for(int y = 0; y <= 1; ++y) {
            tile = Server.rockMesh.getTile(tilex + x, tiley + y);
            short ht = Tiles.decodeHeight(tile);
            if (ht < minRockHeight) {
               minRockHeight = Tiles.decodeHeight(tile);
            }
         }
      }

      int mod = 0;
      if (action == 147) {
         mod = -20;
      } else if (action == 146) {
         mod = 20;
      }

      return maxCaveFloor + mod + 30 > minRockHeight;
   }

   public static boolean createInsideTunnel(
      int tilex, int tiley, int tile, Creature performer, int action, int direction, boolean disintegrate, @Nullable Action act
   ) {
      if (isInsideTunnelOk(tilex, tiley, tile, action, direction, performer, disintegrate)) {
         if (wouldPassThroughRockLayer(tilex, tiley, tile, action)) {
            int mineDir = getTunnelExit(tilex, tiley);
            if (mineDir == -1) {
               performer.getCommunicator().sendNormalServerMessage("The topology here makes it impossible to mine in a good way.");
               return false;
            }

            boolean makingWideTunnel = false;
            if (canHaveWideEntrance(performer, tilex, tiley)) {
               makingWideTunnel = true;
            }

            if (!areAllTilesRockOrReinforcedRock(tilex, tiley, tile, mineDir, true, makingWideTunnel)) {
               performer.getCommunicator().sendNormalServerMessage("The cave walls look very unstable. You cannot keep mining here.");
               return false;
            }

            int t = Server.surfaceMesh.getTile(tilex, tiley);
            if (Tiles.decodeType(t) != Tiles.Tile.TILE_ROCK.id && Tiles.decodeType(t) != Tiles.Tile.TILE_CLIFF.id) {
               performer.getCommunicator().sendNormalServerMessage("The cave walls look very unstable and dirt flows in. You would be buried alive.");
               return false;
            }

            if (!createOutInTunnel(tilex, tiley, tile, performer, 0)) {
               return false;
            }
         } else if (!createStandardTunnel(tilex, tiley, tile, performer, action, direction, disintegrate, act)) {
            return false;
         }

         TileEvent.log(tilex, tiley, -1, performer.getWurmId(), 227);
         return true;
      } else {
         return false;
      }
   }

   static final boolean allCornersAtRockHeight(int tilex, int tiley) {
      for(int x = 0; x <= 1; ++x) {
         for(int y = 0; y <= 1; ++y) {
            int cavet = Server.caveMesh.getTile(tilex + x, tiley + y);
            short caveheight = Tiles.decodeHeight(cavet);
            int ceil = Tiles.decodeData(cavet) & 255;
            short rockHeight = Tiles.decodeHeight(Server.rockMesh.getTile(tilex + x, tiley + y));
            if (caveheight + ceil != rockHeight) {
               return false;
            }
         }
      }

      return true;
   }

   public static final int getCurrentCeilingHeight(int tilex, int tiley) {
      int cavet = Server.caveMesh.getTile(tilex, tiley);
      return Tiles.decodeHeight(cavet) + (Tiles.decodeData(cavet) & 0xFF);
   }

   private static final int getRockHeight(int tilex, int tiley) {
      int rockTile = Server.rockMesh.getTile(tilex, tiley);
      return Tiles.decodeHeight(rockTile);
   }

   private static final boolean isNullWall(int tile) {
      byte cavetype = Tiles.decodeType(tile);
      if (!Tiles.isSolidCave(cavetype)) {
         return false;
      } else {
         return Tiles.decodeHeight(tile) == -100 && (Tiles.decodeData(tile) & 255) == 0;
      }
   }

   private static final int[] getFloorAndCeiling(
      int tilex, int tiley, int fromHeight, int mod, boolean tryZeroCeiling, boolean tryCeilingAtRockHeight, Creature performer
   ) {
      int targetFloor = fromHeight + mod;
      boolean fixedHeight = false;

      for(int x = -1; x <= 0; ++x) {
         for(int y = -1; y <= 0; ++y) {
            VolaTile vt = Zones.getTileOrNull(tilex + x, tiley + y, false);
            if (vt != null && vt.getStructure() != null) {
               fixedHeight = true;
               int tile = Server.caveMesh.getTile(tilex + x, tiley + y);
               targetFloor = Tiles.decodeHeight(tile);
            }
         }
      }

      int targetCeiling = targetFloor + 30;
      if (!tryZeroCeiling && !tryCeilingAtRockHeight && !fixedHeight) {
         if (Server.rand.nextInt(5) == 0) {
            targetCeiling = maybeAddExtraSlopes(performer, targetCeiling);
         }

         if (Server.rand.nextInt(5) == 0) {
            targetFloor = maybeAddExtraSlopes(performer, targetFloor);
         }
      } else if (tryZeroCeiling) {
         targetCeiling = targetFloor;
      }

      int rockHeight = getRockHeight(tilex, tiley);
      int tile = Server.caveMesh.getTile(tilex, tiley);
      int currentFloor = Tiles.decodeHeight(tile);
      int currentCeiling = currentFloor + (Tiles.decodeData(tile) & 255);
      if (targetFloor >= currentFloor && !isNullWall(tile)) {
         targetFloor = currentFloor;
      }

      if (targetCeiling <= currentCeiling) {
         targetCeiling = currentCeiling;
         if (mod > 0 && targetFloor < currentFloor && !isNullWall(tile)) {
            targetFloor = currentFloor;
         }
      }

      if (targetCeiling >= rockHeight || tryCeilingAtRockHeight) {
         targetCeiling = rockHeight;
      }

      if (targetFloor >= rockHeight) {
         targetFloor = rockHeight;
      }

      if (targetCeiling - targetFloor >= 255) {
         if (targetFloor < currentFloor) {
            targetFloor = currentCeiling - 255;
            targetCeiling = currentCeiling;
         } else {
            targetCeiling = Math.min(currentCeiling, targetFloor + 255);
         }
      }

      if (targetCeiling < 5 && !tryZeroCeiling) {
         targetCeiling = 5;
      }

      return new int[]{targetFloor, targetCeiling};
   }

   private static final int maybeAddExtraSlopes(Creature performer, int _previousValue) {
      if (performer.getPower() > 0) {
         return _previousValue;
      } else {
         int miningSkillMod;
         if (performer instanceof Player) {
            Player p = (Player)performer;
            Skill mine = null;

            try {
               Skills skills = p.getSkills();
               mine = skills.getSkill(1008);
            } catch (NoSuchSkillException var9) {
               logger.info(performer.getName() + ": No such skill for mining? " + var9);
            }

            double realKnowledge;
            if (mine == null) {
               realKnowledge = 1.0;
            } else {
               realKnowledge = mine.getKnowledge(0.0);
            }

            if (realKnowledge > 90.0) {
               return _previousValue;
            }

            if (realKnowledge > 70.0) {
               miningSkillMod = 1;
            } else if (realKnowledge > 50.0) {
               miningSkillMod = 2;
            } else {
               miningSkillMod = 3;
            }
         } else {
            miningSkillMod = 3;
         }

         int randVal = Server.rand.nextInt(miningSkillMod * 2 + 1);
         return _previousValue - miningSkillMod + randVal;
      }
   }

   private static final void maybeCreateSource(int tilex, int tiley, Creature performer) {
      if ((Server.rand.nextInt(10000) == 0 || Servers.localServer.testServer && performer.getPower() >= 5 && Server.rand.nextInt(10) == 0)
         && (!Servers.localServer.EPIC || !Servers.localServer.HOMESERVER)
         && Items.getSourceSprings().length > 0
         && Items.getSourceSprings().length < Zones.worldTileSizeX / 20) {
         try {
            Item target1 = ItemFactory.createItem(
               767, 100.0F, (float)(tilex * 4 + 2), (float)(tiley * 4 + 2), (float)Server.rand.nextInt(360), false, (byte)0, -10L, ""
            );
            target1.setSizes(
               target1.getSizeX() + Server.rand.nextInt(1), target1.getSizeY() + Server.rand.nextInt(2), target1.getSizeZ() + Server.rand.nextInt(3)
            );
            logger.log(
               Level.INFO,
               "Created "
                  + target1.getName()
                  + " at "
                  + target1.getTileX()
                  + " "
                  + target1.getTileY()
                  + " sizes "
                  + target1.getSizeX()
                  + ","
                  + target1.getSizeY()
                  + ","
                  + target1.getSizeZ()
                  + ")"
            );
            Items.addSourceSpring(target1);
            performer.getCommunicator().sendSafeServerMessage("You find a source spring!");
         } catch (FailedException var4) {
            logger.log(Level.WARNING, var4.getMessage(), (Throwable)var4);
         } catch (NoSuchTemplateException var5) {
            logger.log(Level.WARNING, var5.getMessage(), (Throwable)var5);
         }
      }
   }

   private static final boolean createStandardTunnel(
      int tilex, int tiley, int tile, Creature performer, int action, int direction, boolean disintegrate, @Nullable Action act
   ) {
      if (Tiles.decodeType(tile) == Tiles.Tile.TILE_CAVE_WALL.id
         || Tiles.decodeType(tile) == Tiles.Tile.TILE_CAVE_WALL_ROCKSALT.id
         || Server.getCaveResource(tilex, tiley) <= 0
         || disintegrate) {
         if (!areAllTilesRockOrReinforcedRock(tilex, tiley, tile, direction, false, false)) {
            return false;
         }

         if (Tiles.decodeHeight(tile) >= -25 || Tiles.decodeHeight(tile) == -100) {
            int dir = 6;
            if (direction == 3) {
               dir = 0;
            } else if (direction == 5) {
               dir = 4;
            } else if (direction == 4) {
               dir = 2;
            }

            int mod = 0;
            if (action == 147) {
               mod = -20;
            } else if (action == 146) {
               mod = 20;
            }

            if (disintegrate) {
               Server.setCaveResource(tilex, tiley, 0);
            }

            if (dir == 0) {
               int fromy = tiley + 1;
               int t = Server.caveMesh.getTile(tilex, fromy);
               short height = Tiles.decodeHeight(t);
               int fromx2 = tilex + 1;
               int fromy2 = tiley + 1;
               int t2 = Server.caveMesh.getTile(fromx2, fromy2);
               short height2 = Tiles.decodeHeight(t2);
               short avheight = (short)((height + height2) / 2);
               int[] newfloorceil = getFloorAndCeiling(tilex, tiley, avheight, mod, false, false, performer);
               int newFloorHeight = newfloorceil[0];
               if (newFloorHeight < -25) {
                  newFloorHeight = -25;
               }

               int newCeil = newfloorceil[1];
               Server.caveMesh.setTile(tilex, tiley, Tiles.encode((short)newFloorHeight, Tiles.Tile.TILE_CAVE.id, (byte)(newCeil - newFloorHeight)));
               maybeCreateSource(tilex, tiley, performer);
               t2 = Server.caveMesh.getTile(tilex + 1, tiley);
               newfloorceil = getFloorAndCeiling(tilex + 1, tiley, avheight, mod, false, false, performer);
               newFloorHeight = newfloorceil[0];
               if (newFloorHeight < -25) {
                  newFloorHeight = -25;
               }

               newCeil = newfloorceil[1];
               Server.caveMesh.setTile(tilex + 1, tiley, Tiles.encode((short)newFloorHeight, Tiles.decodeType(t2), (byte)(newCeil - newFloorHeight)));
               sendCaveTile(tilex, tiley, 0, 0);
            } else if (dir == 4) {
               int t = Server.caveMesh.getTile(tilex, tiley);
               short height = Tiles.decodeHeight(t);
               Server.caveMesh.setTile(tilex, tiley, Tiles.encode(height, Tiles.Tile.TILE_CAVE.id, Tiles.decodeData(t)));
               maybeCreateSource(tilex, tiley, performer);
               int fromx2 = tilex + 1;
               int t2 = Server.caveMesh.getTile(fromx2, tiley);
               short height2 = Tiles.decodeHeight(t2);
               short avheight = (short)((height + height2) / 2);
               t2 = Server.caveMesh.getTile(tilex, tiley + 1);
               int[] newfloorceil = getFloorAndCeiling(tilex, tiley + 1, avheight, mod, false, false, performer);
               int newFloorHeight = newfloorceil[0];
               if (newFloorHeight < -25) {
                  newFloorHeight = -25;
               }

               int newCeil = newfloorceil[1];
               Server.caveMesh.setTile(tilex, tiley + 1, Tiles.encode((short)newFloorHeight, Tiles.decodeType(t2), (byte)(newCeil - newFloorHeight)));
               t2 = Server.caveMesh.getTile(tilex + 1, tiley + 1);
               newfloorceil = getFloorAndCeiling(tilex + 1, tiley + 1, avheight, mod, false, false, performer);
               newFloorHeight = newfloorceil[0];
               if (newFloorHeight < -25) {
                  newFloorHeight = -25;
               }

               newCeil = newfloorceil[1];
               Server.caveMesh.setTile(tilex + 1, tiley + 1, Tiles.encode((short)newFloorHeight, Tiles.decodeType(t2), (byte)(newCeil - newFloorHeight)));
               sendCaveTile(tilex, tiley, 0, 0);
            } else if (dir == 2) {
               int t = Server.caveMesh.getTile(tilex, tiley);
               short height = Tiles.decodeHeight(t);
               Server.caveMesh.setTile(tilex, tiley, Tiles.encode(height, Tiles.Tile.TILE_CAVE.id, Tiles.decodeData(t)));
               maybeCreateSource(tilex, tiley, performer);
               int fromy2 = tiley + 1;
               int t2 = Server.caveMesh.getTile(tilex, fromy2);
               short height2 = Tiles.decodeHeight(t2);
               short avheight = (short)((height + height2) / 2);
               t2 = Server.caveMesh.getTile(tilex + 1, tiley);
               int[] newfloorceil = getFloorAndCeiling(tilex + 1, tiley, avheight, mod, false, false, performer);
               int newFloorHeight = newfloorceil[0];
               if (newFloorHeight < -25) {
                  newFloorHeight = -25;
               }

               int newCeil = newfloorceil[1];
               Server.caveMesh.setTile(tilex + 1, tiley, Tiles.encode((short)newFloorHeight, Tiles.decodeType(t2), (byte)(newCeil - newFloorHeight)));
               t2 = Server.caveMesh.getTile(tilex + 1, tiley + 1);
               newfloorceil = getFloorAndCeiling(tilex + 1, tiley + 1, avheight, mod, false, false, performer);
               newFloorHeight = newfloorceil[0];
               if (newFloorHeight < -25) {
                  newFloorHeight = -25;
               }

               newCeil = newfloorceil[1];
               Server.caveMesh.setTile(tilex + 1, tiley + 1, Tiles.encode((short)newFloorHeight, Tiles.decodeType(t2), (byte)(newCeil - newFloorHeight)));
               sendCaveTile(tilex, tiley, 0, 0);
            } else if (dir == 6) {
               int fromx = tilex + 1;
               int t = Server.caveMesh.getTile(fromx, tiley);
               short height = Tiles.decodeHeight(t);
               int fromx2 = tilex + 1;
               int fromy2 = tiley + 1;
               int t2 = Server.caveMesh.getTile(fromx2, fromy2);
               short height2 = Tiles.decodeHeight(t2);
               short avheight = (short)((height + height2) / 2);
               int[] newfloorceil = getFloorAndCeiling(tilex, tiley, avheight, mod, false, false, performer);
               int newFloorHeight = newfloorceil[0];
               if (newFloorHeight < -25) {
                  newFloorHeight = -25;
               }

               int newCeil = newfloorceil[1];
               Server.caveMesh.setTile(tilex, tiley, Tiles.encode((short)newFloorHeight, Tiles.Tile.TILE_CAVE.id, (byte)(newCeil - newFloorHeight)));
               maybeCreateSource(tilex, tiley, performer);
               t2 = Server.caveMesh.getTile(tilex, tiley + 1);
               newfloorceil = getFloorAndCeiling(tilex, tiley + 1, avheight, mod, false, false, performer);
               newFloorHeight = newfloorceil[0];
               if (newFloorHeight < -25) {
                  newFloorHeight = -25;
               }

               newCeil = newfloorceil[1];
               Server.caveMesh.setTile(tilex, tiley + 1, Tiles.encode((short)newFloorHeight, Tiles.decodeType(t2), (byte)(newCeil - newFloorHeight)));
               sendCaveTile(tilex, tiley, 0, 0);
            }

            if (!performer.isPlayer()) {
               Item gem = createGem(-1, -1, performer, (double)(Server.rand.nextFloat() * 100.0F), false, act);
               if (gem != null) {
                  performer.getInventory().insertItem(gem);
               }
            }
         }
      }

      return true;
   }

   public static final void sendCaveTile(int tilex, int tiley, int diffX, int diffY) {
      Players.getInstance().sendChangedTile(tilex + diffX, tiley + diffY, false, true);

      for(int x = -1; x <= 0; ++x) {
         for(int y = -1; y <= 0; ++y) {
            try {
               Zone toCheckForChange = Zones.getZone(tilex + diffX + x, tiley + diffY + y, false);
               toCheckForChange.changeTile(tilex + diffX + x, tiley + diffY + y);
            } catch (NoSuchZoneException var7) {
               logger.log(Level.INFO, "no such zone?: " + (tilex + diffX + x) + ", " + (tiley + diffY + y), (Throwable)var7);
            }
         }
      }
   }

   public static final boolean surroundedByWalls(int x, int y) {
      if (!Tiles.isSolidCave(Tiles.decodeType(Server.caveMesh.getTile(x - 1, y)))) {
         return false;
      } else if (!Tiles.isSolidCave(Tiles.decodeType(Server.caveMesh.getTile(x + 1, y)))) {
         return false;
      } else if (!Tiles.isSolidCave(Tiles.decodeType(Server.caveMesh.getTile(x, y - 1)))) {
         return false;
      } else {
         return Tiles.isSolidCave(Tiles.decodeType(Server.caveMesh.getTile(x, y + 1)));
      }
   }

   public static final void reProspect() {
      int numsChanged = 0;
      int numsUntouched = 0;

      for(int x = 0; x < (1 << Constants.meshSize) * (1 << Constants.meshSize); ++x) {
         int xx = x & (1 << Constants.meshSize) - 1;
         int yy = x >> Constants.meshSize;
         int old = Server.caveMesh.getTile(xx, yy);
         if (Tiles.isOreCave(Tiles.decodeType(old)) && xx > 5 && yy > 5 && xx < worldSizeX - 3 && yy < worldSizeX - 3) {
            if (surroundedByWalls(xx, yy)) {
               byte newType = prospect(xx, yy, true);
               Server.caveMesh.setTile(xx, yy, Tiles.encode(Tiles.decodeHeight(old), newType, Tiles.decodeData(old)));
               ++numsChanged;
            } else {
               ++numsUntouched;
            }
         }
      }

      logger.log(Level.INFO, "Reprospect finished. Changed=" + numsChanged + ", untouched=" + numsUntouched);

      try {
         Server.caveMesh.saveAllDirtyRows();
      } catch (IOException var7) {
         logger.log(Level.WARNING, var7.getMessage(), (Throwable)var7);
      }
   }

   public static final Item createRandomGem() {
      return createRandomGem(100.0F);
   }

   public static final Item createRandomGem(float maxql) {
      try {
         int rand = Server.rand.nextInt(300);
         int templateId = 349;
         float ql = Server.rand.nextFloat() * maxql;
         if (rand < 50) {
            templateId = 349;
         } else if (rand < 100) {
            templateId = 446;
         } else if (rand < 140) {
            templateId = 376;
            if (ql >= 99.0F) {
               templateId = 377;
            }
         } else if (rand < 180) {
            templateId = 374;
            if (ql >= 99.0F) {
               templateId = 375;
            }
         } else if (rand < 220) {
            templateId = 382;
            if (ql >= 99.0F) {
               templateId = 383;
            }
         } else if (rand < 260) {
            templateId = 378;
            if (ql >= 99.0F) {
               templateId = 379;
            }
         } else if (rand < 300) {
            templateId = 380;
            if (ql >= 99.0F) {
               templateId = 381;
            }
         }

         return ItemFactory.createItem(templateId, Server.rand.nextFloat() * ql, null);
      } catch (FailedException var4) {
         logger.log(Level.WARNING, var4.getMessage(), (Throwable)var4);
      } catch (NoSuchTemplateException var5) {
         logger.log(Level.WARNING, var5.getMessage(), (Throwable)var5);
      }

      return null;
   }

   static final Item createGem(int minedTilex, int minedTiley, Creature performer, double power, boolean surfaced, @Nullable Action act) {
      return createGem(minedTilex, minedTiley, minedTilex, minedTiley, performer, power, surfaced, act);
   }

   static final Item createGem(
      int tilex, int tiley, int createtilex, int createtiley, Creature performer, double power, boolean surfaced, @Nullable Action act
   ) {
      byte rarity = act != null ? act.getRarity() : 0;

      try {
         rockRandom.setSeed((long)(tilex + tiley * Zones.worldTileSizeY) * 102533L);
         if (rockRandom.nextInt(100) == 0 && Server.rand.nextInt(10) == 0) {
            if (tilex < 0 && tiley < 0) {
               Item gem = ItemFactory.createItem(349, (float)power, null);
               gem.setLastOwnerId(performer.getWurmId());
               return gem;
            }

            Item salt = ItemFactory.createItem(349, (float)power, rarity, null);
            salt.setLastOwnerId(performer.getWurmId());
            salt.putItemInfrontof(performer, 0.0F);
            performer.getCommunicator().sendNormalServerMessage("You mine some salt.");
         }

         rockRandom.setSeed((long)(tilex + tiley * Zones.worldTileSizeY) * SOURCE_PRIME);
         if (rockRandom.nextInt(sourceFactor) == 0) {
            boolean isVein = Tiles.isOreCave(Tiles.decodeType(Server.caveMesh.getTile(tilex, tiley)));
            if (Server.rand.nextInt(10) == 0 && !isVein) {
               if (tilex < 0 && tiley < 0) {
                  Item gem = ItemFactory.createItem(765, (float)power, null);
                  gem.setLastOwnerId(performer.getWurmId());
                  return gem;
               }

               Item crystal = ItemFactory.createItem(765, (float)power, rarity, null);
               crystal.setLastOwnerId(performer.getWurmId());
               crystal.putItemInfrontof(performer, 0.0F);
               performer.getCommunicator().sendNormalServerMessage("You mine some pink crystals.");
            }
         }

         rockRandom.setSeed((long)(tilex + tiley * Zones.worldTileSizeY) * 6883L);
         if (rockRandom.nextInt(200) == 0 && Server.rand.nextInt(40) == 0) {
            if (tilex < 0 && tiley < 0) {
               Item gem = ItemFactory.createItem(446, (float)power, null);
               gem.setLastOwnerId(performer.getWurmId());
               return gem;
            }

            Item flint = ItemFactory.createItem(446, (float)power, rarity, null);
            flint.setLastOwnerId(performer.getWurmId());
            flint.putItemInfrontof(performer, 0.0F);
            performer.getCommunicator().sendNormalServerMessage("You find flint!");
         }

         if (Server.rand.nextInt(1000) == 0) {
            int rand = Server.rand.nextInt(5);
            if (rand == 0) {
               int templateId = 376;
               float ql = Math.min((float)MAX_QL, Server.rand.nextFloat() * 100.0F);
               if (ql >= 99.0F) {
                  templateId = 377;
               }

               if (tilex < 0 && tiley < 0) {
                  Item gem = ItemFactory.createItem(templateId, (float)power, null);
                  gem.setLastOwnerId(performer.getWurmId());
                  return gem;
               }

               Item gem = ItemFactory.createItem(templateId, (float)power, rarity, null);
               gem.setLastOwnerId(performer.getWurmId());
               gem.putItemInfrontof(performer, 0.0F);
               if (ql >= 99.0F) {
                  performer.achievement(298);
               }

               if (gem.getQualityLevel() > 90.0F) {
                  performer.achievement(299);
               }

               if (rarity > 2) {
                  performer.achievement(334);
               }

               performer.getCommunicator().sendNormalServerMessage("You find " + gem.getNameWithGenus() + "!");
            } else if (rand == 1) {
               int templateId = 374;
               float ql = Math.min((float)MAX_QL, Server.rand.nextFloat() * 100.0F);
               if (ql >= 99.0F) {
                  templateId = 375;
               }

               if (tilex < 0 && tiley < 0) {
                  Item gem = ItemFactory.createItem(templateId, (float)power, null);
                  gem.setLastOwnerId(performer.getWurmId());
                  return gem;
               }

               Item gem = ItemFactory.createItem(templateId, (float)power, rarity, null);
               gem.setLastOwnerId(performer.getWurmId());
               gem.putItemInfrontof(performer, 0.0F);
               if (ql >= 99.0F) {
                  performer.achievement(298);
               }

               if (gem.getQualityLevel() > 90.0F) {
                  performer.achievement(299);
               }

               if (rarity > 2) {
                  performer.achievement(334);
               }

               performer.getCommunicator().sendNormalServerMessage("You find " + gem.getNameWithGenus() + "!");
            } else if (rand == 2) {
               int templateId = 382;
               float ql = Math.min((float)MAX_QL, Server.rand.nextFloat() * 100.0F);
               if (ql >= 99.0F) {
                  templateId = 383;
               }

               if (tilex < 0 && tiley < 0) {
                  Item gem = ItemFactory.createItem(templateId, (float)power, null);
                  gem.setLastOwnerId(performer.getWurmId());
                  return gem;
               }

               Item gem = ItemFactory.createItem(templateId, (float)power, rarity, null);
               gem.setLastOwnerId(performer.getWurmId());
               gem.putItemInfrontof(performer, 0.0F);
               if (ql >= 99.0F) {
                  performer.achievement(298);
               }

               if (gem.getQualityLevel() > 90.0F) {
                  performer.achievement(299);
               }

               if (rarity > 2) {
                  performer.achievement(334);
               }

               performer.getCommunicator().sendNormalServerMessage("You find " + gem.getNameWithGenus() + "!");
            } else if (rand == 3) {
               int templateId = 378;
               float ql = Math.min((float)MAX_QL, Server.rand.nextFloat() * 100.0F);
               if (ql >= 99.0F) {
                  templateId = 379;
               }

               if (tilex < 0 && tiley < 0) {
                  Item gem = ItemFactory.createItem(templateId, (float)power, null);
                  gem.setLastOwnerId(performer.getWurmId());
                  return gem;
               }

               Item gem = ItemFactory.createItem(templateId, (float)power, rarity, null);
               gem.setLastOwnerId(performer.getWurmId());
               gem.putItemInfrontof(performer, 0.0F);
               if (ql >= 99.0F) {
                  performer.achievement(298);
               }

               if (gem.getQualityLevel() > 90.0F) {
                  performer.achievement(299);
               }

               if (rarity > 2) {
                  performer.achievement(334);
               }

               performer.getCommunicator().sendNormalServerMessage("You find " + gem.getNameWithGenus() + "!");
            } else {
               int templateId = 380;
               float ql = Math.min((float)MAX_QL, Server.rand.nextFloat() * 100.0F);
               if (ql >= 99.0F) {
                  templateId = 381;
               }

               if (tilex < 0 && tiley < 0) {
                  Item gem = ItemFactory.createItem(templateId, (float)power, null);
                  gem.setLastOwnerId(performer.getWurmId());
                  return gem;
               }

               Item gem = ItemFactory.createItem(templateId, (float)power, rarity, null);
               gem.setLastOwnerId(performer.getWurmId());
               gem.putItemInfrontof(performer, 0.0F);
               if (ql >= 99.0F) {
                  performer.achievement(298);
               }

               if (gem.getQualityLevel() > 90.0F) {
                  performer.achievement(299);
               }

               if (rarity > 2) {
                  performer.achievement(334);
               }

               performer.getCommunicator().sendNormalServerMessage("You find " + gem.getNameWithGenus() + "!");
            }
         }
      } catch (FailedException var14) {
         logger.log(Level.WARNING, performer.getName() + ": " + var14.getMessage(), (Throwable)var14);
      } catch (NoSuchTemplateException var15) {
         logger.log(Level.WARNING, performer.getName() + ": no template", (Throwable)var15);
      } catch (Exception var16) {
         logger.log(Level.WARNING, "Factory failed to produce item", (Throwable)var16);
      }

      return null;
   }

   public static boolean cannotMineSlope(Creature performer, Skill mining, int digTilex, int digTiley) {
      int diff = Terraforming.getMaxSurfaceDifference(Server.surfaceMesh.getTile(digTilex, digTiley), digTilex, digTiley);
      int maxSlope = (int)(mining.getKnowledge(0.0) * (double)(Servers.localServer.PVPSERVER ? 1 : 3));
      if (Math.signum((float)diff) == 1.0F && diff > maxSlope) {
         performer.getCommunicator().sendNormalServerMessage("You are too unskilled to mine here.", (byte)3);
         return true;
      } else if (Math.signum((float)diff) == -1.0F && -1 - diff > maxSlope) {
         performer.getCommunicator().sendNormalServerMessage("You are too unskilled to mine here.", (byte)3);
         return true;
      } else {
         return false;
      }
   }

   public static final boolean mine(Action act, Creature performer, Item source, int tilex, int tiley, short action, float counter, int digTilex, int digTiley) {
      boolean done = true;
      int tile = Server.surfaceMesh.getTile(digTilex, digTiley);
      if (digTilex < 1 || digTilex > (1 << Constants.meshSize) - 1 || digTiley < 1 || digTiley > (1 << Constants.meshSize) - 1) {
         performer.getCommunicator().sendNormalServerMessage("The water is too deep to mine.", (byte)3);
         return true;
      } else if (Zones.isTileProtected(digTilex, digTiley)) {
         performer.getCommunicator().sendNormalServerMessage("This tile is protected by the gods. You can not mine here.", (byte)3);
         return true;
      } else {
         short h = Tiles.decodeHeight(tile);
         if (h <= -25) {
            performer.getCommunicator().sendNormalServerMessage("The water is too deep to mine.", (byte)3);
         } else {
            done = false;
            Skills skills = performer.getSkills();
            Skill mining = null;
            Skill tool = null;
            boolean insta = performer.getPower() > 3 && source.isWand();

            try {
               mining = skills.getSkill(1008);
            } catch (Exception var49) {
               mining = skills.learn(1008, 1.0F);
            }

            try {
               tool = skills.getSkill(source.getPrimarySkill());
            } catch (Exception var48) {
               try {
                  tool = skills.learn(source.getPrimarySkill(), 1.0F);
               } catch (NoSuchSkillException var47) {
                  logger.log(Level.WARNING, performer.getName() + " trying to mine with an item with no primary skill: " + source.getName());
               }
            }

            for(int x = -1; x <= 0; ++x) {
               for(int y = -1; y <= 0; ++y) {
                  byte decType = Tiles.decodeType(Server.surfaceMesh.getTile(digTilex + x, digTiley + y));
                  if (decType != Tiles.Tile.TILE_ROCK.id && decType != Tiles.Tile.TILE_CLIFF.id) {
                     performer.getCommunicator().sendNormalServerMessage("The surrounding area needs to be rock before you mine.", (byte)3);
                     return true;
                  }
               }
            }

            for(int x = 0; x >= -1; --x) {
               for(int y = 0; y >= -1; --y) {
                  VolaTile vt = Zones.getTileOrNull(digTilex + x, digTiley + y, true);
                  if (vt != null && vt.getStructure() != null) {
                     if (vt.getStructure().isTypeHouse()) {
                        if (x == 0 && y == 0) {
                           performer.getCommunicator().sendNormalServerMessage("You cannot mine in a building.", (byte)3);
                        } else {
                           performer.getCommunicator().sendNormalServerMessage("You cannot mine next to a building.", (byte)3);
                        }

                        return true;
                     }

                     for(BridgePart bp : vt.getBridgeParts()) {
                        if (bp.getType().isSupportType()) {
                           performer.getCommunicator().sendNormalServerMessage("The bridge support nearby prevents mining.");
                           return true;
                        }

                        if (x == -1 && bp.hasEastExit() || x == 0 && bp.hasWestExit() || y == -1 && bp.hasSouthExit() || y == 0 && bp.hasNorthExit()) {
                           performer.getCommunicator().sendNormalServerMessage("The end of the bridge nearby prevents mining.");
                           return true;
                        }
                     }
                  }
               }
            }

            VolaTile vt = Zones.getTileOrNull(digTilex, digTiley, true);
            if (vt != null && vt.getFencesForLevel(0).length > 0) {
               performer.getCommunicator().sendNormalServerMessage("You cannot mine next to a fence.", (byte)3);
               return true;
            }

            vt = Zones.getTileOrNull(digTilex, digTiley - 1, true);
            if (vt != null && vt.getFencesForLevel(0).length > 0) {
               for(Fence f : vt.getFencesForLevel(0)) {
                  if (!f.isHorizontal()) {
                     performer.getCommunicator().sendNormalServerMessage("You cannot mine next to a fence.", (byte)3);
                     return true;
                  }
               }
            }

            vt = Zones.getTileOrNull(digTilex - 1, digTiley, true);
            if (vt != null && vt.getFencesForLevel(0).length > 0) {
               for(Fence f : vt.getFencesForLevel(0)) {
                  if (f.isHorizontal()) {
                     performer.getCommunicator().sendNormalServerMessage("You cannot mine next to a fence.", (byte)3);
                     return true;
                  }
               }
            }

            int time = 0;
            VolaTile dropTile = Zones.getTileOrNull((int)performer.getPosX() >> 2, (int)performer.getPosY() >> 2, true);
            if (dropTile != null && dropTile.getNumberOfItems(performer.getFloorLevel()) > 99) {
               performer.getCommunicator().sendNormalServerMessage("There is no space to mine here. Clear the area first.", (byte)3);
               return true;
            }

            if (counter == 1.0F) {
               if (cannotMineSlope(performer, mining, digTilex, digTiley)) {
                  return true;
               }

               time = Actions.getStandardActionTime(performer, mining, source, 0.0);

               try {
                  performer.getCurrentAction().setTimeLeft(time);
               } catch (NoSuchActionException var46) {
                  logger.log(Level.INFO, "This action does not exist?", (Throwable)var46);
               }

               Server.getInstance().broadCastAction(performer.getName() + " starts mining.", performer, 5);
               performer.getCommunicator().sendNormalServerMessage("You start to mine.");
               performer.sendActionControl(Actions.actionEntrys[145].getVerbString(), true, time);
               source.setDamage(source.getDamage() + 0.0015F * source.getDamageModifier());
               performer.getStatus().modifyStamina(-1000.0F);
            } else {
               try {
                  time = performer.getCurrentAction().getTimeLeft();
               } catch (NoSuchActionException var45) {
                  logger.log(Level.INFO, "This action does not exist?", (Throwable)var45);
               }

               if (counter * 10.0F <= (float)time && !insta) {
                  if (act.currentSecond() % 5 == 0 || act.currentSecond() == 3 && time < 50) {
                     String sstring = "sound.work.mining1";
                     int x = Server.rand.nextInt(3);
                     if (x == 0) {
                        sstring = "sound.work.mining2";
                     } else if (x == 1) {
                        sstring = "sound.work.mining3";
                     }

                     SoundPlayer.playSound(sstring, digTilex, digTiley, performer.isOnSurface(), 0.0F);
                     source.setDamage(source.getDamage() + 0.0015F * source.getDamageModifier());
                     performer.getStatus().modifyStamina(-7000.0F);
                  }
               } else {
                  if (act.getRarity() != 0) {
                     performer.playPersonalSound("sound.fx.drumroll");
                  }

                  if (cannotMineSlope(performer, mining, digTilex, digTiley)) {
                     return true;
                  }

                  double bonus = 0.0;
                  double power = 0.0;
                  done = true;
                  int itemTemplateCreated = 146;
                  float diff = 1.0F;
                  int caveTile = Server.caveMesh.getTile(digTilex, digTiley);
                  short caveFloor = Tiles.decodeHeight(caveTile);
                  int caveCeilingHeight = caveFloor + (short)(Tiles.decodeData(caveTile) & 255);
                  MeshIO mesh = Server.surfaceMesh;
                  if (h - 1 <= caveCeilingHeight) {
                     performer.getCommunicator().sendNormalServerMessage("The rock sounds hollow. You need to tunnel to proceed.", (byte)3);
                     return true;
                  }

                  double imbueEnhancement = 1.0 + 0.23047 * (double)source.getSkillSpellImprovement(1008) / 100.0;
                  int lNewTile = mesh.getTile(digTilex - 1, digTiley);
                  short maxDiff = (short)((int)Math.max(10.0, mining.getKnowledge(0.0) * 3.0 * imbueEnhancement));
                  if (Terraforming.checkMineSurfaceTile(lNewTile, performer, h, maxDiff)) {
                     return true;
                  }

                  lNewTile = mesh.getTile(digTilex + 1, digTiley);
                  if (Terraforming.checkMineSurfaceTile(lNewTile, performer, h, maxDiff)) {
                     return true;
                  }

                  lNewTile = mesh.getTile(digTilex, digTiley - 1);
                  if (Terraforming.checkMineSurfaceTile(lNewTile, performer, h, maxDiff)) {
                     return true;
                  }

                  lNewTile = mesh.getTile(digTilex, digTiley + 1);
                  if (Terraforming.checkMineSurfaceTile(lNewTile, performer, h, maxDiff)) {
                     return true;
                  }

                  if (Terraforming.isAltarBlocking(performer, tilex, tiley)) {
                     performer.getCommunicator().sendSafeServerMessage("You cannot build here, since this is holy ground.", (byte)2);
                     return true;
                  }

                  if (performer.getTutorialLevel() == 10 && !performer.skippedTutorial()) {
                     performer.missionFinished(true, true);
                  }

                  if (tool != null) {
                     bonus = tool.skillCheck(1.0, source, 0.0, false, counter) / 5.0;
                  }

                  power = Math.max(1.0, mining.skillCheck(1.0, source, bonus, false, counter));
                  float chance = Math.max(0.2F, (float)mining.getKnowledge(0.0) / 200.0F);
                  if (Server.rand.nextFloat() < chance) {
                     try {
                        if (mining.getKnowledge(0.0) * imbueEnhancement < power) {
                           power = mining.getKnowledge(0.0) * imbueEnhancement;
                        }

                        rockRandom.setSeed((long)(digTilex + digTiley * Zones.worldTileSizeY) * 789221L);
                        int m = 100;
                        int max = Math.min(100, (int)(20.0 + (double)rockRandom.nextInt(80) * imbueEnhancement));
                        power = Math.min(power, (double)max);
                        if (source.isCrude()) {
                           power = 1.0;
                        }

                        float modifier = 1.0F;
                        if (source.getSpellEffects() != null) {
                           modifier *= source.getSpellEffects().getRuneEffect(RuneUtilities.ModifierEffect.ENCH_RESGATHERED);
                        }

                        float orePower = GeneralUtilities.calcOreRareQuality(power * (double)modifier, act.getRarity(), source.getRarity());
                        Item newItem = ItemFactory.createItem(
                           146,
                           orePower,
                           performer.getPosX(),
                           performer.getPosY(),
                           Server.rand.nextFloat() * 360.0F,
                           performer.isOnSurface(),
                           act.getRarity(),
                           -10L,
                           null
                        );
                        newItem.setLastOwnerId(performer.getWurmId());
                        newItem.setDataXY(tilex, tiley);
                        performer.getCommunicator().sendNormalServerMessage("You mine some " + newItem.getName() + ".");
                        Server.getInstance().broadCastAction(performer.getName() + " mines some " + newItem.getName() + ".", performer, 5);
                        TileEvent.log(digTilex, digTiley, 0, performer.getWurmId(), action);
                        short newHeight = (short)(h - 1);
                        mesh.setTile(digTilex, digTiley, Tiles.encode(newHeight, Tiles.Tile.TILE_ROCK.id, Tiles.decodeData(tile)));
                        Server.rockMesh.setTile(digTilex, digTiley, Tiles.encode(newHeight, (short)0));

                        for(int xx = 0; xx >= -1; --xx) {
                           for(int yy = 0; yy >= -1; --yy) {
                              performer.getMovementScheme().touchFreeMoveCounter();
                              Players.getInstance().sendChangedTile(digTilex + xx, digTiley + yy, performer.isOnSurface(), true);

                              try {
                                 Zone toCheckForChange = Zones.getZone(digTilex + xx, digTiley + yy, performer.isOnSurface());
                                 toCheckForChange.changeTile(digTilex + xx, digTiley + yy);
                              } catch (NoSuchZoneException var44) {
                                 logger.log(Level.INFO, "no such zone?: " + tilex + ", " + tiley, (Throwable)var44);
                              }
                           }
                        }
                     } catch (Exception var50) {
                        logger.log(Level.WARNING, "Factory failed to produce item", (Throwable)var50);
                     }
                  } else {
                     performer.getCommunicator().sendNormalServerMessage("You chip away at the rock.");
                  }
               }
            }
         }

         return done;
      }
   }

   public static final byte prospect(int x, int y, boolean reprospecting) {
      oreRand = Server.rand.nextInt(reprospecting ? 75 : 1000);
      if (oreRand < 74) {
         if (reprospecting) {
            if (minezones[x / 32][y / 32] != Tiles.Tile.TILE_CAVE_WALL.id) {
               return Server.rand.nextInt(5) == 0 ? getOreId(oreRand) : minezones[x / 32][y / 32];
            } else {
               return getOreId(oreRand);
            }
         } else {
            return Server.rand.nextInt(5) == 0 ? getOreId(oreRand) : minezones[x / 32][y / 32];
         }
      } else {
         return Tiles.Tile.TILE_CAVE_WALL.id;
      }
   }

   static boolean affectsHighway(int tilex, int tiley) {
      if (MethodsHighways.onHighway(tilex, tiley - 1, true)) {
         return true;
      } else if (MethodsHighways.onHighway(tilex + 1, tiley - 1, true)) {
         return true;
      } else if (MethodsHighways.onHighway(tilex + 1, tiley, true)) {
         return true;
      } else if (MethodsHighways.onHighway(tilex + 1, tiley + 1, true)) {
         return true;
      } else if (MethodsHighways.onHighway(tilex, tiley + 1, true)) {
         return true;
      } else if (MethodsHighways.onHighway(tilex - 1, tiley + 1, true)) {
         return true;
      } else if (MethodsHighways.onHighway(tilex - 1, tiley, true)) {
         return true;
      } else {
         return MethodsHighways.onHighway(tilex - 1, tiley - 1, true);
      }
   }

   private static byte getOreId(int num) {
      if (num < 2) {
         return Tiles.Tile.TILE_CAVE_WALL_ORE_GOLD.id;
      } else if (num < 6) {
         return Tiles.Tile.TILE_CAVE_WALL_ORE_SILVER.id;
      } else if (num < 10) {
         return Tiles.Tile.TILE_CAVE_WALL_ORE_COPPER.id;
      } else if (num < 14) {
         return Tiles.Tile.TILE_CAVE_WALL_ORE_ZINC.id;
      } else if (num < 18) {
         return Tiles.Tile.TILE_CAVE_WALL_ORE_LEAD.id;
      } else if (num < 22) {
         return Tiles.Tile.TILE_CAVE_WALL_ORE_TIN.id;
      } else if (num < 72) {
         return Tiles.Tile.TILE_CAVE_WALL_ORE_IRON.id;
      } else if (num < 73) {
         return Tiles.Tile.TILE_CAVE_WALL_MARBLE.id;
      } else {
         return num < 74 ? Tiles.Tile.TILE_CAVE_WALL_SLATE.id : Tiles.Tile.TILE_CAVE_WALL.id;
      }
   }

   static final int getItemTemplateForTile(byte type) {
      if (type == Tiles.Tile.TILE_CAVE_WALL_ORE_COPPER.id) {
         return 43;
      } else if (type == Tiles.Tile.TILE_CAVE_WALL_ORE_GOLD.id) {
         return 39;
      } else if (type == Tiles.Tile.TILE_CAVE_WALL_ORE_IRON.id) {
         return 38;
      } else if (type == Tiles.Tile.TILE_CAVE_WALL_ORE_LEAD.id) {
         return 41;
      } else if (type == Tiles.Tile.TILE_CAVE_WALL_ORE_SILVER.id) {
         return 40;
      } else if (type == Tiles.Tile.TILE_CAVE_WALL_ORE_TIN.id) {
         return 207;
      } else if (type == Tiles.Tile.TILE_CAVE_WALL_ORE_ZINC.id) {
         return 42;
      } else if (type == Tiles.Tile.TILE_CAVE_WALL_ORE_ADAMANTINE.id) {
         return 693;
      } else if (type == Tiles.Tile.TILE_CAVE_WALL_ORE_GLIMMERSTEEL.id) {
         return 697;
      } else if (type == Tiles.Tile.TILE_CAVE_WALL_MARBLE.id) {
         return 785;
      } else if (type == Tiles.Tile.TILE_CAVE_WALL_SLATE.id) {
         return 770;
      } else if (type == Tiles.Tile.TILE_CAVE_WALL_ROCKSALT.id) {
         return 1238;
      } else {
         return type == Tiles.Tile.TILE_CAVE_WALL_SANDSTONE.id ? 1116 : 146;
      }
   }

   static final int getDifficultyForTile(byte type) {
      if (type == Tiles.Tile.TILE_CAVE_WALL_ORE_COPPER.id || type == Tiles.Tile.TILE_CAVE_WALL_SLATE.id) {
         return 20;
      } else if (type == Tiles.Tile.TILE_CAVE_WALL_ORE_GOLD.id
         || type == Tiles.Tile.TILE_CAVE_WALL_REINFORCED.id
         || type == Tiles.Tile.TILE_CAVE_WALL_MARBLE.id) {
         return 40;
      } else if (type == Tiles.Tile.TILE_CAVE_WALL_ORE_IRON.id) {
         return 3;
      } else if (type == Tiles.Tile.TILE_CAVE_WALL_ORE_LEAD.id) {
         return 20;
      } else if (type == Tiles.Tile.TILE_CAVE_WALL_ORE_SILVER.id) {
         return 35;
      } else if (type == Tiles.Tile.TILE_CAVE_WALL_ORE_TIN.id) {
         return 10;
      } else if (type == Tiles.Tile.TILE_CAVE_WALL_ORE_ADAMANTINE.id) {
         return 60;
      } else if (type == Tiles.Tile.TILE_CAVE_WALL_ORE_GLIMMERSTEEL.id) {
         return 55;
      } else if (type == Tiles.Tile.TILE_CAVE_WALL_ROCKSALT.id) {
         return 30;
      } else {
         return type == Tiles.Tile.TILE_CAVE_WALL_SANDSTONE.id ? 45 : 2;
      }
   }

   static {
      Random prand = new Random();
      prand.setSeed(181081L + (long)Servers.getLocalServerId());
      Server.rand.setSeed(789221L);

      for(int x = 0; x <= mineZoneDiv; ++x) {
         for(int y = 0; y <= mineZoneDiv; ++y) {
            int num = Server.rand.nextInt(75);
            int prandnum = prand.nextInt(4);
            if (prandnum == 0) {
               minezones[x][y] = getOreId(num);
            } else {
               minezones[x][y] = Tiles.Tile.TILE_CAVE_WALL.id;
            }
         }
      }
   }
}

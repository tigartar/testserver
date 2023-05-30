package com.wurmonline.server.behaviours;

import com.wurmonline.mesh.FoliageAge;
import com.wurmonline.mesh.Tiles;
import com.wurmonline.server.Constants;
import com.wurmonline.server.FailedException;
import com.wurmonline.server.GeneralUtilities;
import com.wurmonline.server.Items;
import com.wurmonline.server.LoginHandler;
import com.wurmonline.server.MiscConstants;
import com.wurmonline.server.Players;
import com.wurmonline.server.Server;
import com.wurmonline.server.Servers;
import com.wurmonline.server.TimeConstants;
import com.wurmonline.server.WurmCalendar;
import com.wurmonline.server.bodys.Wound;
import com.wurmonline.server.bodys.Wounds;
import com.wurmonline.server.creatures.Brand;
import com.wurmonline.server.creatures.Creature;
import com.wurmonline.server.creatures.Creatures;
import com.wurmonline.server.creatures.DbCreatureStatus;
import com.wurmonline.server.creatures.MountAction;
import com.wurmonline.server.creatures.ai.NoPathException;
import com.wurmonline.server.creatures.ai.Path;
import com.wurmonline.server.creatures.ai.PathFinder;
import com.wurmonline.server.creatures.ai.PathTile;
import com.wurmonline.server.deities.Deities;
import com.wurmonline.server.endgames.EndGameItem;
import com.wurmonline.server.endgames.EndGameItems;
import com.wurmonline.server.items.Item;
import com.wurmonline.server.items.ItemFactory;
import com.wurmonline.server.items.ItemTemplate;
import com.wurmonline.server.items.ItemTemplateFactory;
import com.wurmonline.server.items.ItemTypes;
import com.wurmonline.server.items.NoSpaceException;
import com.wurmonline.server.items.NoSuchTemplateException;
import com.wurmonline.server.items.RuneUtilities;
import com.wurmonline.server.items.Trade;
import com.wurmonline.server.players.ItemBonus;
import com.wurmonline.server.players.Player;
import com.wurmonline.server.questions.AskKingdomQuestion;
import com.wurmonline.server.questions.PriestQuestion;
import com.wurmonline.server.questions.QuestionTypes;
import com.wurmonline.server.questions.RoyalChallenge;
import com.wurmonline.server.questions.SetKingdomQuestion;
import com.wurmonline.server.skills.NoSuchSkillException;
import com.wurmonline.server.skills.Skill;
import com.wurmonline.server.skills.SkillSystem;
import com.wurmonline.server.skills.Skills;
import com.wurmonline.server.sounds.SoundPlayer;
import com.wurmonline.server.structures.Blocking;
import com.wurmonline.server.structures.BlockingResult;
import com.wurmonline.server.structures.Structure;
import com.wurmonline.server.utils.StringUtil;
import com.wurmonline.server.villages.NoSuchVillageException;
import com.wurmonline.server.villages.Village;
import com.wurmonline.server.villages.VillageStatus;
import com.wurmonline.server.villages.Villages;
import com.wurmonline.server.zones.NoSuchZoneException;
import com.wurmonline.server.zones.Track;
import com.wurmonline.server.zones.VolaTile;
import com.wurmonline.server.zones.Zone;
import com.wurmonline.server.zones.Zones;
import com.wurmonline.shared.constants.AttitudeConstants;
import com.wurmonline.shared.constants.CounterTypes;
import com.wurmonline.shared.constants.ItemMaterials;
import com.wurmonline.shared.constants.SoundNames;
import java.io.IOException;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.annotation.Nullable;

public final class MethodsCreatures
   implements MiscConstants,
   QuestionTypes,
   ItemTypes,
   CounterTypes,
   ItemMaterials,
   SoundNames,
   VillageStatus,
   TimeConstants,
   AttitudeConstants {
   public static final String cvsversion = "$Id: MethodsCreatures.java,v 1.31 2007-04-19 23:05:18 root Exp $";
   private static final Logger logger = Logger.getLogger(MethodsCreatures.class.getName());

   private MethodsCreatures() {
   }

   static final void teleportSet(Creature performer, Item source, int tilex, int tiley) {
      if (source.getTemplateId() == 174) {
         VolaTile tile = performer.getCurrentTile();
         if (tile != null) {
            if (tile.getStructure() != null) {
               performer.getCommunicator().sendNormalServerMessage("You utter arcane words and make some strange gestures but nothing happens.");
               return;
            }

            tilex = tile.tilex;
            tiley = tile.tiley;
            performer.getCommunicator()
               .sendNormalServerMessage(
                  "You utter arcane words and make some strange gestures. You will teleport to where you are standing now using the wand."
               );
            Server.getInstance()
               .broadCastAction(
                  performer.getName() + " utters some arcane words and makes a few strange gestures with " + source.getNameWithGenus() + ".", performer, 5
               );
         }
      } else if (source.getTemplateId() == 525) {
         boolean ok = false;
         int tilenum = Server.caveMesh.getTile(tilex, tiley);
         if (performer.isOnPvPServer() && Zones.isWithinDuelRing(performer.getTileX(), performer.getTileY(), true) != null) {
            performer.getCommunicator()
               .sendNormalServerMessage("The magic of the duelling ring interferes. You can not use the " + source.getName() + " here.");
            return;
         }

         if (performer.isInPvPZone()) {
            performer.getCommunicator().sendNormalServerMessage("The magic of the pvp zone interferes. You can not use the " + source.getName() + " now.");
            return;
         }

         if (Servers.localServer.PVPSERVER && EndGameItems.getEvilAltar() != null) {
            EndGameItem egi = EndGameItems.getEvilAltar();
            if (performer.isWithinDistanceTo(egi.getItem().getPosX(), egi.getItem().getPosY(), egi.getItem().getPosZ(), 50.0F)) {
               performer.getCommunicator().sendNormalServerMessage("The magic of this place interferes. You can not use the " + source.getName() + " here.");
               return;
            }
         } else if (Servers.localServer.PVPSERVER && EndGameItems.getGoodAltar() != null) {
            EndGameItem egi = EndGameItems.getGoodAltar();
            if (performer.isWithinDistanceTo(egi.getItem().getPosX(), egi.getItem().getPosY(), egi.getItem().getPosZ(), 50.0F)) {
               performer.getCommunicator().sendNormalServerMessage("The magic of this place interferes. You can not use the " + source.getName() + " here.");
               return;
            }
         }

         if (Tiles.isOreCave(Tiles.decodeType(tilenum)) && !performer.isOnSurface()) {
            VolaTile tile = performer.getCurrentTile();
            if (tile != null && tile.getStructure() == null && (Math.abs(tilex - tile.tilex) <= 1 || Math.abs(tiley - tile.tiley) <= 1)) {
               tilex = tile.tilex;
               tiley = tile.tiley;
               ok = true;
               performer.getCommunicator()
                  .sendNormalServerMessage(
                     "You knock the " + source.getName() + " in the cave wall a few times. You will teleport to where you are standing now using the stone."
                  );
               Server.getInstance()
                  .broadCastAction(performer.getName() + " knocks with " + source.getNameWithGenus() + " in the cave wall a few times.", performer, 5);
            }
         }

         if (!ok) {
            performer.getCommunicator().sendNormalServerMessage("You knock the " + source.getName() + " in the cave wall but nothing happens.");
            Server.getInstance().broadCastAction(performer.getName() + " knocks with " + source.getNameWithGenus() + " a few times.", performer, 5);
            return;
         }
      } else if (source.getTemplateId() == 524) {
         boolean ok = false;
         if (performer.isOnPvPServer() && Zones.isWithinDuelRing(performer.getTileX(), performer.getTileY(), true) != null) {
            performer.getCommunicator()
               .sendNormalServerMessage("There magic of the duelling ring interferes. You can not use the " + source.getName() + " here.");
            return;
         }

         if (performer.isInPvPZone()) {
            performer.getCommunicator().sendNormalServerMessage("The magic of the pvp zone interferes. You can not use the " + source.getName() + " now.");
            return;
         }

         if (Servers.localServer.PVPSERVER && EndGameItems.getEvilAltar() != null) {
            EndGameItem egi = EndGameItems.getEvilAltar();
            if (performer.isWithinDistanceTo(egi.getItem().getPosX(), egi.getItem().getPosY(), egi.getItem().getPosZ(), 50.0F)) {
               performer.getCommunicator().sendNormalServerMessage("There magic of this place interferes. You can not use the " + source.getName() + " here.");
               return;
            }
         } else if (Servers.localServer.PVPSERVER && EndGameItems.getGoodAltar() != null) {
            EndGameItem egi = EndGameItems.getGoodAltar();
            if (performer.isWithinDistanceTo(egi.getItem().getPosX(), egi.getItem().getPosY(), egi.getItem().getPosZ(), 50.0F)) {
               performer.getCommunicator().sendNormalServerMessage("There magic of this place interferes. You can not use the " + source.getName() + " here.");
               return;
            }
         }

         if (performer.isOnSurface()) {
            VolaTile tile = performer.getCurrentTile();
            if (tile != null && tile.getStructure() == null && (Math.abs(tilex - tile.tilex) <= 1 || Math.abs(tiley - tile.tiley) <= 1)) {
               int tilenum = Server.surfaceMesh.getTile(tilex, tiley);
               if (Tiles.isTree(Tiles.decodeType(tilenum))) {
                  tilex = tile.tilex;
                  tiley = tile.tiley;
                  ok = true;
                  performer.getCommunicator()
                     .sendNormalServerMessage(
                        "You tap the " + source.getName() + " at the tree a few times. You will teleport to where you are standing now using the twig."
                     );
                  Server.getInstance().broadCastAction(performer.getName() + " taps " + source.getNameWithGenus() + " at a tree few times.", performer, 5);
               }
            }
         }

         if (!ok) {
            performer.getCommunicator().sendNormalServerMessage("You tap the " + source.getName() + " a few times but nothing happens.");
            Server.getInstance().broadCastAction(performer.getName() + " taps " + source.getNameWithGenus() + " a few times.", performer, 5);
            return;
         }
      } else {
         performer.getCommunicator()
            .sendNormalServerMessage("You utter arcane words and make some strange gestures. You will teleport to where you were pointing with the wand.");
         Server.getInstance()
            .broadCastAction(
               performer.getName() + " utters some arcane words and makes a few strange gestures with " + source.getNameWithGenus() + ".", performer, 5
            );
      }

      performer.setTeleportLayer(performer.getLayer());
      source.setData(tilex, tiley);
   }

   static final void teleportCreature(Creature performer, Item source) {
      if (source.getTemplateId() == 176
         || source.getTemplateId() == 315
         || source.getTemplateId() == 174
         || source.getTemplateId() == 1027
         || source.getTemplateId() == 525
         || source.getTemplateId() == 524) {
         if (performer.getVisionArea() != null && performer.getVisionArea().isInitialized()) {
            int tx = source.getData1();
            int ty = source.getData2();
            if (tx >= 0 && ty >= 0 && tx < 1 << Constants.meshSize && ty < 1 << Constants.meshSize) {
               long barid = -10L;
               if (performer.getPower() <= 0) {
                  Item[] inventoryItems = performer.getInventory().getAllItems(true);

                  for(Item lInventoryItem : inventoryItems) {
                     if (lInventoryItem.isArtifact()) {
                        performer.getCommunicator()
                           .sendNormalServerMessage(
                              "The " + lInventoryItem.getName() + " hums and disturbs the weave. You can not use the " + source.getName() + " now."
                           );
                        return;
                     }
                  }

                  Item[] bodyItems = performer.getBody().getBodyItem().getAllItems(true);

                  for(Item lInventoryItem : bodyItems) {
                     if (lInventoryItem.isArtifact()) {
                        performer.getCommunicator()
                           .sendNormalServerMessage(
                              "The " + lInventoryItem.getName() + " hums and disturbs the weave. You can not use the " + source.getName() + " now."
                           );
                        return;
                     }
                  }
               }

               int layer = performer.getTeleportLayer();
               int floorLevel = performer.getTeleportFloorLevel();
               if ((source.getTemplateId() != 176 && source.getTemplateId() != 315 || performer.getPower() < 2)
                  && (source.getTemplateId() != 174 && source.getTemplateId() != 1027 || performer.getPower() < 1)) {
                  if (source.getTemplateId() != 525 && source.getTemplateId() != 524) {
                     Item[] inventoryItems = performer.getInventory().getAllItems(false);

                     for(Item lInventoryItem : inventoryItems) {
                        if (lInventoryItem.getTemplateId() == 45 && lInventoryItem.getWeightGrams() >= lInventoryItem.getTemplate().getWeightGrams()) {
                           barid = lInventoryItem.getWurmId();
                           break;
                        }
                     }

                     if (barid == -10L) {
                        Item[] bodyItems = performer.getBody().getAllItems();

                        for(Item lBodyItem : bodyItems) {
                           if (lBodyItem.getTemplateId() == 45 && lBodyItem.getWeightGrams() >= lBodyItem.getTemplate().getWeightGrams()) {
                              barid = lBodyItem.getWurmId();
                              break;
                           }
                        }
                     }
                  } else {
                     if (performer.getEnemyPresense() > 0 || performer.isFighting()) {
                        performer.getCommunicator()
                           .sendNormalServerMessage("There is a blocking enemy presence nearby. You can not use the " + source.getName() + " now.");
                        return;
                     }

                     if (performer.isOnPvPServer() && Zones.isWithinDuelRing(performer.getTileX(), performer.getTileY(), true) != null) {
                        performer.getCommunicator()
                           .sendNormalServerMessage("The magic of the duelling ring interferes. You can not use the " + source.getName() + " now.");
                        return;
                     }

                     if (performer.isInPvPZone()) {
                        performer.getCommunicator()
                           .sendNormalServerMessage("The magic of the pvp zone interferes. You can not use the " + source.getName() + " now.");
                        return;
                     }

                     if (Servers.localServer.PVPSERVER && EndGameItems.getEvilAltar() != null) {
                        EndGameItem egi = EndGameItems.getEvilAltar();
                        if (performer.isWithinDistanceTo(egi.getItem().getPosX(), egi.getItem().getPosY(), egi.getItem().getPosZ(), 50.0F)) {
                           performer.getCommunicator()
                              .sendNormalServerMessage("There magic of this place interferes. You can not use the " + source.getName() + " now.");
                           return;
                        }
                     } else if (Servers.localServer.PVPSERVER && EndGameItems.getGoodAltar() != null) {
                        EndGameItem egi = EndGameItems.getGoodAltar();
                        if (performer.isWithinDistanceTo(egi.getItem().getPosX(), egi.getItem().getPosY(), egi.getItem().getPosZ(), 50.0F)) {
                           performer.getCommunicator()
                              .sendNormalServerMessage("There magic of this place interferes. You can not use the " + source.getName() + " now.");
                           return;
                        }
                     }

                     barid = 0L;
                     if (source.getTemplateId() == 525) {
                        layer = -1;
                     } else {
                        layer = 0;
                     }
                  }
               } else {
                  barid = 0L;
               }

               if (barid == -10L) {
                  performer.getCommunicator()
                     .sendNormalServerMessage("A standard sized silver lump will be consumed when teleporting. You need to acquire one.");
               } else {
                  boolean port = false;
                  if (source.getTemplateId() == 176 || source.getTemplateId() == 315 || source.getTemplateId() == 1027) {
                     port = true;
                  } else if (mayTelePortToTile(performer, tx, ty, layer == 0)) {
                     if (barid > 0L) {
                        Items.destroyItem(barid);
                     }

                     port = true;
                  } else {
                     performer.getCommunicator().sendNormalServerMessage("You can't teleport there. Please choose another destination.");
                  }

                  if (port) {
                     if (performer.getPower() > 0) {
                        if (performer.getLogger() != null) {
                           performer.getLogger().log(Level.INFO, "Teleporting to " + tx + ", " + ty);
                        }
                     } else if (logger.isLoggable(Level.FINE)) {
                        logger.fine(performer + " using a source item, " + source + " is teleporting to " + tx + ", " + ty);
                     }

                     performer.setTeleportPoints((short)tx, (short)ty, layer, floorLevel);
                     if (performer.startTeleporting()) {
                        if (source.getTemplateId() == 525 || source.getTemplateId() == 524) {
                           Items.destroyItem(source.getWurmId());
                           Server.getInstance().broadCastAction(performer.getName() + " uses a " + source.getName() + ".", performer, 5);
                        }

                        performer.getCommunicator().sendNormalServerMessage("You feel a slight tingle in your spine.");
                        performer.getCommunicator().sendTeleport(false);
                     }
                  }
               }
            } else {
               performer.getCommunicator().sendNormalServerMessage("You need to set the teleportation endpoint for the wand to reasonable values first.");
            }
         } else {
            performer.getCommunicator().sendNormalServerMessage("You cannot teleport right now.");
            logger.info(performer.getName() + " could not teleport due to VisionArea " + performer.getVisionArea());
         }
      }
   }

   protected static boolean mayTelePortToTile(Creature creature, int tilex, int tiley, boolean surfaced) {
      VolaTile tile = null;
      tile = Zones.getTileOrNull(tilex, tiley, surfaced);
      if (tile != null) {
         Structure struct = tile.getStructure();
         if (struct == null) {
            return true;
         } else {
            return struct.isFinished() && struct.mayPass(creature);
         }
      } else {
         return true;
      }
   }

   static void initiateTrade(Creature performer, Creature opponent) {
      Trade trade = new Trade(performer, opponent);
      performer.setTrade(trade);
      opponent.setTrade(trade);
      opponent.getCommunicator().sendStartTrading(performer);
      performer.getCommunicator().sendStartTrading(opponent);
      if (!opponent.isPlayer()) {
         opponent.addItemsToTrade();
      }
   }

   static String getDirectionStringFor(float rot, int dir) {
      int turnDir = 0;
      float degree = 22.5F;
      float lRot = Creature.normalizeAngle(rot);
      if (!((double)lRot >= 337.5) && !(lRot < 22.5F)) {
         for(int x = 0; x < 8; ++x) {
            if (lRot < 22.5F + (float)(45 * x)) {
               turnDir = x;
               break;
            }
         }
      } else {
         turnDir = 0;
      }

      String direction = "straight ahead";
      if (dir == turnDir + 1 || dir == turnDir - 7) {
         direction = "ahead to the right";
      } else if (dir == turnDir + 2 || dir == turnDir - 6) {
         direction = "right";
      } else if (dir == turnDir + 3 || dir == turnDir - 5) {
         direction = "back to the right";
      } else if (dir == turnDir + 4 || dir == turnDir - 4) {
         direction = "backwards";
      } else if (dir == turnDir + 5 || dir == turnDir - 3) {
         direction = "back to the left";
      } else if (dir == turnDir + 6 || dir == turnDir - 2) {
         direction = "to the left";
      } else if (dir == turnDir + 7 || dir == turnDir - 1) {
         direction = "ahead to the left";
      }

      return direction;
   }

   public static String getLocationStringFor(float rot, int dir, String performername) {
      int turnDir = 0;
      float degree = 22.5F;
      float lRot = Creature.normalizeAngle(rot);
      if (!((double)lRot >= 337.5) && !(lRot < 22.5F)) {
         for(int x = 0; x < 8; ++x) {
            if (lRot < 22.5F + (float)(45 * x)) {
               turnDir = x;
               break;
            }
         }
      } else {
         turnDir = 0;
      }

      String direction = "in front of " + performername;
      if (dir == turnDir + 1 || dir == turnDir - 7) {
         direction = "ahead of " + performername + " to the right";
      } else if (dir == turnDir + 2 || dir == turnDir - 6) {
         direction = "to the right of " + performername;
      } else if (dir == turnDir + 3 || dir == turnDir - 5) {
         direction = "behind " + performername + " to the right";
      } else if (dir == turnDir + 4 || dir == turnDir - 4) {
         direction = "behind " + performername;
      } else if (dir == turnDir + 5 || dir == turnDir - 3) {
         direction = "behind " + performername + " to the left";
      } else if (dir == turnDir + 6 || dir == turnDir - 2) {
         direction = "to the left of " + performername;
      } else if (dir == turnDir + 7 || dir == turnDir - 1) {
         direction = "ahead of " + performername + " to the left";
      }

      return direction;
   }

   static String getElapsedTimeString(int hours) {
      String timeString;
      if (hours > 48) {
         timeString = "more than two days ago.";
      } else if (hours > 24) {
         timeString = "more than a day ago.";
      } else if (hours > 12) {
         timeString = "more than half a day ago.";
      } else if (hours > 6) {
         timeString = "more than six hours ago.";
      } else if (hours > 3) {
         timeString = "more than three hours ago.";
      } else if (hours > 2) {
         timeString = "more than two hours ago.";
      } else if (hours > 1) {
         timeString = "more than an hour ago.";
      } else {
         timeString = "less than an hour ago.";
      }

      return timeString;
   }

   public static int getDir(Creature performer, int targetX, int targetY) {
      double newrot = Math.atan2(
         (double)((targetY << 2) + 2 - (int)performer.getStatus().getPositionY()), (double)((targetX << 2) + 2 - (int)performer.getStatus().getPositionX())
      );
      float attAngle = (float)(newrot * (180.0 / Math.PI)) + 90.0F;
      attAngle = Creature.normalizeAngle(attAngle);
      float degree = 22.5F;
      if (!((double)attAngle >= 337.5) && !(attAngle < 22.5F)) {
         for(int x = 0; x < 8; ++x) {
            if (attAngle < 22.5F + (float)(45 * x)) {
               return x;
            }
         }

         return 0;
      } else {
         return 0;
      }
   }

   static boolean track(Creature performer, int tilex, int tiley, int tile, float counter) {
      boolean done = false;
      int trackTilex = (int)performer.getStatus().getPositionX() >> 2;
      int trackTiley = (int)performer.getStatus().getPositionY() >> 2;
      tile = Server.surfaceMesh.getTile(trackTilex, trackTiley);
      if (!performer.isOnSurface()) {
         tile = Server.caveMesh.getTile(trackTilex, trackTiley);
      }

      if (Tiles.decodeHeight(tile) > 0) {
         try {
            Zone trackZone = Zones.getZone(trackTilex, trackTiley, performer.isOnSurface());
            int ownTracks = 0;
            Skill tracking = null;
            Skills skills = performer.getSkills();

            try {
               tracking = skills.getSkill(10018);
            } catch (Exception var25) {
               tracking = skills.learn(10018, 1.0F);
            }

            int time = Actions.getStandardActionTime(performer, tracking, null, 0.0);
            if (counter == 1.0F) {
               performer.getCommunicator().sendNormalServerMessage("You start to check the ground for tracks.");
               Server.getInstance().broadCastAction(performer.getName() + " starts to check the ground for tracks.", performer, 5);
               if (performer.getPower() == 0) {
                  performer.sendActionControl(Actions.actionEntrys[109].getVerbString(), true, time);
                  performer.getStatus().modifyStamina(-500.0F);
               }
            }

            if (counter * 10.0F > (float)time || performer.getPower() > 0) {
               tracking.skillCheck(tracking.getKnowledge(0.0), 0.0, false, counter / 2.0F);
               done = true;
               Track[] tracks = trackZone.getTracksFor(trackTilex, trackTiley, 3);
               if (tracks.length > 0) {
                  int max = Math.max(30, (int)tracking.getKnowledge(0.0));
                  int min = Math.min((int)tracking.getKnowledge(0.0), max);
                  if (performer.getPower() > 0) {
                     min = 100;
                  }

                  min = Math.min(min, tracks.length);

                  for(int x = 0; x < min; ++x) {
                     if (!tracks[x].getCreatureName().equals(performer.getName())) {
                        int dir = tracks[x].getDirection();
                        float rot = performer.getStatus().getRotation();
                        String direction = getDirectionStringFor(rot, dir);
                        long diff = System.currentTimeMillis() - tracks[x].getTime();
                        int gameSeconds = (int)(diff / 125L);
                        int hours = gameSeconds / 3600;
                        String timeString = getElapsedTimeString(hours);
                        if (performer.getPower() > 0) {
                           performer.getCommunicator()
                              .sendNormalServerMessage(tracks[x].getCreatureName() + " (" + tracks[x].getId() + " [" + direction + "] " + hours + " hrs");
                        } else {
                           performer.getCommunicator()
                              .sendNormalServerMessage("You find tracks of " + tracks[x].getCreatureName() + " leading " + direction + " done " + timeString);
                        }
                     } else {
                        ++ownTracks;
                     }
                  }

                  if (ownTracks == tracks.length) {
                     performer.getCommunicator().sendNormalServerMessage("You find only your own tracks.");
                  }
               } else {
                  performer.getCommunicator().sendNormalServerMessage("You find no tracks.");
               }
            }
         } catch (NoSuchZoneException var26) {
            done = true;
            performer.getCommunicator().sendNormalServerMessage("You find no tracks.");
            logger.log(Level.WARNING, "No zone for tiles " + trackTilex + ", " + trackTiley);
         }
      } else {
         done = true;
         performer.getCommunicator().sendNormalServerMessage("The water is too deep to track.");
      }

      return done;
   }

   static boolean absorb(Creature performer, int tilex, int tiley, int tile, float counter, Action act) {
      boolean done = false;
      tile = Server.surfaceMesh.getTile(tilex, tiley);
      byte type = Tiles.decodeType(tile);
      byte data = Tiles.decodeData(tile);
      Tiles.Tile theTile = Tiles.getTile(type);
      if (!theTile.isMycelium()) {
         performer.getCommunicator().sendNormalServerMessage("You can not absorb that.", (byte)3);
         return true;
      } else {
         Wounds wounds = performer.getBody().getWounds();
         if (performer.getDisease() != 0 || wounds != null && wounds.getWounds().length != 0) {
            if (performer.isPlayer() && ((Player)performer).myceliumHealCounter > 1) {
               performer.getCommunicator()
                  .sendNormalServerMessage(
                     "You are still full of energy, and must wait another " + ((Player)performer).myceliumHealCounter + " seconds to avoid dangerous effects."
                  );
               return true;
            } else {
               if (Tiles.decodeHeight(tile) > 0) {
                  int time = 100;
                  if (counter == 1.0F) {
                     performer.getCommunicator().sendNormalServerMessage("You focus on the mycelium. Your body starts to tremble.");
                     Server.getInstance().broadCastAction(performer.getName() + " starts to tremble feverishly.", performer, 5);
                     performer.sendActionControl(Actions.actionEntrys[347].getVerbString(), true, 100);
                     performer.getStatus().modifyStamina(-500.0F);
                  }

                  if (act.currentSecond() == 5) {
                     performer.getCommunicator().sendNormalServerMessage("A soothing warmth permeates your body.");
                     Server.getInstance().broadCastAction("Roots stretch out from the mycelium, entangling " + performer.getName() + ".", performer, 5);
                  } else if (counter > 10.0F) {
                     done = true;
                     if (wounds != null && wounds.getWounds().length > 0) {
                        Wound[] w = wounds.getWounds();
                        w[0].modifySeverity(-5000);
                     }

                     if (performer.getDisease() > 0 && Server.rand.nextInt(20) == 0) {
                        performer.setDisease((byte)0);
                     }

                     performer.getCommunicator().sendNormalServerMessage("You absorb the mycelium.");
                     Server.getInstance().broadCastAction("The mycelium disappears into the body of " + performer.getName() + ".", performer, 5);
                     if (performer.isPlayer()) {
                        ((Player)performer).myceliumHealCounter = 30;
                     }

                     if (type == Tiles.Tile.TILE_MYCELIUM.id) {
                        Server.setSurfaceTile(tilex, tiley, Tiles.decodeHeight(tile), Tiles.Tile.TILE_GRASS.id, (byte)0);
                     } else if (type == Tiles.Tile.TILE_MYCELIUM_LAWN.id) {
                        Server.setSurfaceTile(tilex, tiley, Tiles.decodeHeight(tile), Tiles.Tile.TILE_LAWN.id, (byte)0);
                     } else if (theTile.isMyceliumTree()) {
                        byte newType = theTile.getTreeType(data).asNormalTree();
                        byte newData = (byte)((data & 252) + 1);
                        Server.setSurfaceTile(tilex, tiley, Tiles.decodeHeight(tile), newType, newData);
                     } else if (theTile.isMyceliumBush()) {
                        byte newType = theTile.getBushType(data).asNormalBush();
                        byte newData = (byte)((data & 252) + 1);
                        Server.setSurfaceTile(tilex, tiley, Tiles.decodeHeight(tile), newType, newData);
                     }

                     Players.getInstance().sendChangedTile(tilex, tiley, true, true);
                  }
               } else {
                  done = true;
                  performer.getCommunicator().sendNormalServerMessage("The water is too deep. You can not absorb that.", (byte)3);
               }

               return done;
            }
         } else {
            performer.getCommunicator().sendNormalServerMessage("You focus on the mycelium, but nothing happens.", (byte)3);
            return true;
         }
      }
   }

   public static void destroyCreature(Creature target) {
      if (target != null) {
         Creatures.getInstance().setCreatureDead(target);
         Players.getInstance().setCreatureDead(target);
         target.destroy();
      }
   }

   static final boolean firstAid(Creature performer, Item bandaid, Wound wound, float counter, Action act) {
      boolean done = false;
      String bandage = "bandage";
      if (performer.isPlayer() && ((Player)performer).stuckCounter > 0) {
         performer.getCommunicator().sendNormalServerMessage("You are a bit dizzy. Try again in " + ((Player)performer).stuckCounter + " second/s.", (byte)3);
         return true;
      } else if (bandaid.isHollow() && !bandaid.isEmpty(true)) {
         performer.getCommunicator().sendNormalServerMessage("Empty the " + bandaid.getName() + " first.", (byte)3);
         return true;
      } else {
         if (bandaid.isHealingSalve() && (wound.isInternal() || wound.isBruise() || wound.isPoison())) {
            if (wound.isBandaged()) {
               performer.getCommunicator().sendNormalServerMessage("The wound has already been smeared.", (byte)3);
               done = true;
            }

            int nums = bandaid.getRarity() + act.getRarity() + bandaid.getWeightGrams() / 10;
            if (nums < wound.getNumBandagesNeeded() && performer instanceof Player) {
               performer.getCommunicator().sendNormalServerMessage("The " + bandaid.getName() + " will not cover the wound.", (byte)3);
               done = true;
            }

            bandage = "smear";
         } else if (!wound.isInternal()) {
            int nums = bandaid.getRarity() + act.getRarity() + bandaid.getWeightGrams() / 100;
            if (nums < wound.getNumBandagesNeeded() && performer instanceof Player) {
               performer.getCommunicator().sendNormalServerMessage("The " + bandaid.getName() + " does not cover the wound.", (byte)3);
               done = true;
            }

            if (wound.isBandaged()) {
               performer.getCommunicator().sendNormalServerMessage("The wound is already bandaged.", (byte)3);
               done = true;
            } else if (bandaid.getMaterial() != 17) {
               performer.getCommunicator().sendNormalServerMessage("You can't use " + bandaid.getNameWithGenus() + " to bind the wound!", (byte)3);
               done = true;
            }
         } else {
            performer.getCommunicator().sendNormalServerMessage("You can't use " + bandaid.getNameWithGenus() + " to bind the wound!", (byte)3);
            done = true;
         }

         if (wound.getCreature() == null) {
            done = true;
         } else if (!done) {
            if (wound.getCreature().isGhost()) {
               performer.getCommunicator().sendNormalServerMessage("You can't treat that wound. It is not physical.", (byte)3);
               return true;
            }

            Skill firstaid = null;
            int time = 1000;
            Skills skills = performer.getSkills();
            double power = 0.0;

            try {
               firstaid = skills.getSkill(10056);
            } catch (NoSuchSkillException var14) {
               firstaid = skills.learn(10056, 1.0F);
            }

            if (counter == 1.0F) {
               time = 50;
               if (wound.getSeverity() > 3275.0F) {
                  if (wound.getSeverity() < 9825.0F) {
                     time = 100;
                  } else if (wound.getSeverity() < 19650.0F) {
                     time = 150;
                  } else if (wound.getSeverity() < 29475.0F) {
                     time = 300;
                  } else {
                     time = 450;
                  }
               }

               act.setTimeLeft(time);
               String targetName = wound.getCreature().getNameWithGenus();
               if (wound.getCreature().isPlayer()) {
                  targetName = wound.getCreature().getName();
               }

               performer.sendActionControl(Actions.actionEntrys[196].getVerbString(), true, time);
               performer.getCommunicator().sendNormalServerMessage("You start to " + bandage + " the wound.");
               if (wound.getCreature() == performer) {
                  Server.getInstance().broadCastAction(performer.getName() + " starts tending " + performer.getHisHerItsString() + " wounds.", performer, 5);
               } else {
                  wound.getCreature().getCommunicator().sendNormalServerMessage(performer.getName() + " starts tending your wounds.");
                  Server.getInstance()
                     .broadCastAction(performer.getName() + " starts tending " + targetName + "'s wounds.", performer, wound.getCreature(), 5);
               }

               if (act.getRarity() != 0) {
                  performer.playPersonalSound("sound.fx.drumroll");
               }
            } else {
               time = act.getTimeLeft();
            }

            if (counter > 5.0F && counter * 10.0F > (float)time) {
               Methods.sendSound(performer, "sound.work.firstaid.bandage");
               power = firstaid.skillCheck((double)(wound.getSeverity() / 1000.0F), bandaid, 0.0, false, counter);
               if (act.getRarity() > 0) {
                  power = Math.max(power + (double)(act.getRarity() * 10), (double)(act.getRarity() * 10));
               }

               String targetName = wound.getCreature().getNameWithGenus();
               if (wound.getCreature().isPlayer()) {
                  targetName = wound.getCreature().getName();
               }

               int rarity = 1 + bandaid.getRarity() + act.getRarity();
               if (bandaid.isRoyal()) {
                  performer.getCommunicator().sendNormalServerMessage("The " + bandaid.getNameWithGenus() + " mends itself!");
               } else if (!bandaid.isCombine()) {
                  Items.destroyItem(bandaid.getWurmId());
               } else if (bandaid.isHealingSalve()) {
                  bandaid.setWeight(bandaid.getWeightGrams() - wound.getNumBandagesNeeded() * 10, true);
               } else {
                  bandaid.setWeight(bandaid.getWeightGrams() - wound.getNumBandagesNeeded() * 100, true);
               }

               if (!(power > 0.0)) {
                  if (!wound.isInternal() && !wound.isBruise() && power < -90.0 && bandaid.getRarity() == 0 && act.getRarity() == 0) {
                     performer.getCommunicator()
                        .sendNormalServerMessage("You try to " + bandage + " the wound with " + bandaid.getNameWithGenus() + " but only make it worse!");
                     if (wound.getCreature() == performer) {
                        Server.getInstance()
                           .broadCastAction(
                              performer.getName()
                                 + " tries to "
                                 + bandage
                                 + " "
                                 + performer.getHisHerItsString()
                                 + " wound with "
                                 + bandaid.getNameWithGenus()
                                 + " but only makes it worse.",
                              performer,
                              5
                           );
                     } else {
                        wound.getCreature()
                           .getCommunicator()
                           .sendNormalServerMessage(
                              performer.getName() + " tries to " + bandage + " the wound with " + bandaid.getNameWithGenus() + " but only makes it worse."
                           );
                        Server.getInstance()
                           .broadCastAction(
                              performer.getName()
                                 + " tries to "
                                 + bandage
                                 + " "
                                 + targetName
                                 + "'s wound with "
                                 + bandaid.getNameWithGenus()
                                 + " but only makes it worse.",
                              performer,
                              wound.getCreature(),
                              5
                           );
                     }

                     wound.modifySeverity(3275);
                  } else {
                     performer.getCommunicator()
                        .sendNormalServerMessage("You try to " + bandage + " the wound with " + bandaid.getNameWithGenus() + " but fail.");
                     if (wound.getCreature() == performer) {
                        Server.getInstance()
                           .broadCastAction(
                              performer.getName()
                                 + " tries to "
                                 + bandage
                                 + " "
                                 + performer.getHisHerItsString()
                                 + " wound with "
                                 + bandaid.getNameWithGenus()
                                 + " but fails.",
                              performer,
                              5
                           );
                     } else {
                        wound.getCreature()
                           .getCommunicator()
                           .sendNormalServerMessage(
                              performer.getName() + " tries to " + bandage + " the wound with " + bandaid.getNameWithGenus() + " but fails."
                           );
                        Server.getInstance()
                           .broadCastAction(
                              performer.getName() + " tries to " + bandage + " " + targetName + "'s wound with " + bandaid.getNameWithGenus() + " but fails.",
                              performer,
                              wound.getCreature(),
                              5
                           );
                     }
                  }
               } else {
                  performer.getCommunicator().sendNormalServerMessage("You " + bandage + " the wound with " + bandaid.getNameWithGenus() + ".");
                  if (wound.getCreature() == performer) {
                     Server.getInstance()
                        .broadCastAction(
                           performer.getName() + " " + bandage + "s " + performer.getHisHerItsString() + " wound with " + bandaid.getNameWithGenus() + ".",
                           performer,
                           5
                        );
                  } else {
                     wound.getCreature()
                        .getCommunicator()
                        .sendNormalServerMessage(performer.getName() + " " + bandage + "s the wound with " + bandaid.getNameWithGenus() + ".");
                     Server.getInstance()
                        .broadCastAction(
                           performer.getName() + " " + bandage + "s " + targetName + "'s wound with " + bandaid.getNameWithGenus() + ".",
                           performer,
                           wound.getCreature(),
                           5
                        );
                     if (wound.getCreature() instanceof Player && (performer.getDeity() == null || !performer.getDeity().isHateGod())) {
                        performer.maybeModifyAlignment(1.0F);
                     }
                  }

                  wound.setBandaged(true);
                  wound.modifySeverity(
                     (int)Math.min(
                        (double)(-wound.getSeverity() / 10.0F),
                        -(
                           (double)(3275.0F * (float)rarity)
                              + power * (double)(performer.getCultist() != null && performer.getCultist().healsFaster() ? 100.0F : 50.0F)
                        )
                     )
                  );
                  performer.achievement(536);
               }

               done = true;
            }
         }

         return done;
      }
   }

   static final boolean treat(Creature performer, Item cover, Wound wound, float counter, Action act) {
      boolean done = false;
      if (performer.isPlayer() && ((Player)performer).stuckCounter > 0) {
         performer.getCommunicator().sendNormalServerMessage("You are a bit dizzy. Try again in " + ((Player)performer).stuckCounter + " second/s.", (byte)3);
         return true;
      } else {
         if (wound.getCreature() == null) {
            done = true;
         } else if (!done) {
            if (!wound.isAcidWound() && cover.getTemplateId() != 481) {
               performer.getCommunicator().sendNormalServerMessage("You can't treat the wound with that.", (byte)3);
               return true;
            }

            if (cover.getTemplateId() != 481 && cover.getTemplateId() != 128) {
               performer.getCommunicator().sendNormalServerMessage("You can't treat the wound with that.", (byte)3);
               return true;
            }

            if (!wound.isAcidWound() && wound.getHealEff() > cover.getAuxData()) {
               performer.getCommunicator().sendNormalServerMessage("The wound already has a better cover.", (byte)3);
               return true;
            }

            if (wound.getCreature().isGhost()) {
               performer.getCommunicator().sendNormalServerMessage("You can't treat that wound. It is not physical.", (byte)3);
               return true;
            }

            Skill firstaid = null;
            int time = 1000;
            Skills skills = performer.getSkills();
            double power = 0.0;

            try {
               firstaid = skills.getSkill(10056);
            } catch (NoSuchSkillException var12) {
               firstaid = skills.learn(10056, 1.0F);
            }

            if (counter == 1.0F) {
               time = (int)(3.0 * ((double)(wound.getSeverity() / 2000.0F) + (100.0 - firstaid.getKnowledge(0.0) / 2.0)));
               act.setTimeLeft(time);
               performer.sendActionControl(Actions.actionEntrys[196].getVerbString(), true, time);
               performer.getCommunicator().sendNormalServerMessage("You start to treat the wound.");
               if (wound.getCreature() == performer) {
                  Server.getInstance().broadCastAction(performer.getName() + " starts treating " + performer.getHisHerItsString() + " wounds.", performer, 5);
               } else {
                  wound.getCreature().getCommunicator().sendNormalServerMessage(performer.getName() + " starts treating your wounds.");
                  Server.getInstance()
                     .broadCastAction(performer.getName() + " starts treating " + wound.getCreature().getNameWithGenus() + "'s wounds.", performer, 5);
               }

               if (act.getRarity() != 0) {
                  performer.playPersonalSound("sound.fx.drumroll");
               }
            } else {
               time = act.getTimeLeft();
            }

            if (counter > 5.0F && counter * 10.0F > (float)time) {
               Methods.sendSound(performer, "sound.work.firstaid.bandage");
               power = firstaid.skillCheck((double)(wound.getSeverity() / 1000.0F), cover, 0.0, false, counter);
               if (act.getPower() > 0.0F) {
                  power = Math.max(power + (double)(act.getRarity() * 10), (double)(act.getRarity() * 10));
               }

               if (power > 0.0 || Server.rand.nextInt(100) < 20) {
                  performer.getCommunicator().sendNormalServerMessage("You treat the wound with " + cover.getNameWithGenus() + ".");
                  if (wound.getCreature() == performer) {
                     Server.getInstance().broadCastAction(performer.getName() + " treats " + performer.getHisHerItsString() + " wound.", performer, 5);
                  } else {
                     wound.getCreature()
                        .getCommunicator()
                        .sendNormalServerMessage(
                           performer.getName() + " treats the wound with " + cover.getNameWithGenus() + " " + cover.getDescription().toLowerCase() + "."
                        );
                     Server.getInstance()
                        .broadCastAction(
                           performer.getName() + " treats " + wound.getCreature().getName() + "'s wound with " + cover.getNameWithGenus() + ".", performer, 5
                        );
                     if (wound.getCreature() instanceof Player && (performer.getDeity() == null || !performer.getDeity().isHateGod())) {
                        performer.maybeModifyAlignment(1.0F);
                     }
                  }

                  if (!wound.isAcidWound()) {
                     wound.setHealeff((byte)((int)((float)cover.getAuxData() + act.getPower() + (float)cover.getRarity())));
                  } else if (cover.getTemplateId() == 128) {
                     if ((float)cover.getWeightGrams() > wound.getSeverity() / 10.0F) {
                        wound.setType((byte)4);
                        wound.setHealeff((byte)((int)(18.0F + act.getPower() + (float)cover.getRarity())));
                     } else {
                        performer.getCommunicator().sendNormalServerMessage("You need more water! It buuurns!");
                     }
                  }
               } else if (power < -90.0 && !cover.isLiquid() && cover.getRarity() == 0 && act.getRarity() == 0) {
                  performer.getCommunicator()
                     .sendNormalServerMessage("You try to treat the wound with " + cover.getNameWithGenus() + " but only make it worse!");
                  if (wound.getCreature() == performer) {
                     Server.getInstance()
                        .broadCastAction(
                           performer.getName() + " tries to treat " + performer.getHisHerItsString() + " wound but only makes it worse.", performer, 5
                        );
                  } else {
                     wound.getCreature()
                        .getCommunicator()
                        .sendNormalServerMessage(performer.getName() + " tries to treat the wound with " + cover.getName() + " but only makes it worse.");
                     Server.getInstance()
                        .broadCastAction(
                           performer.getName()
                              + " tries to treat "
                              + wound.getCreature().getName()
                              + "'s wound with "
                              + cover.getNameWithGenus()
                              + " but only makes it worse.",
                           performer,
                           5
                        );
                  }

                  wound.modifySeverity(3275);
               } else {
                  performer.getCommunicator().sendNormalServerMessage("You try to treat the wound with " + cover.getNameWithGenus() + " but fail.");
                  if (wound.getCreature() == performer) {
                     Server.getInstance()
                        .broadCastAction(
                           performer.getName()
                              + " tries to treat "
                              + performer.getHisHerItsString()
                              + " wound with "
                              + cover.getNameWithGenus()
                              + " but fails.",
                           performer,
                           5
                        );
                  } else {
                     Server.getInstance()
                        .broadCastAction(
                           performer.getName()
                              + " tries to treat "
                              + wound.getCreature().getName()
                              + "'s wound with "
                              + cover.getNameWithGenus()
                              + " but fails.",
                           performer,
                           5
                        );
                  }
               }

               if (cover.getTemplateId() != 128) {
                  Items.destroyItem(cover.getWurmId());
               } else {
                  cover.setWeight(cover.getWeightGrams() - (int)(wound.getSeverity() / 10.0F), true);
               }

               done = true;
            }
         }

         return done;
      }
   }

   static boolean shear(Action act, Item source, Creature performer, Creature target, float counter) {
      if (!Methods.isActionAllowed(performer, (short)646)) {
         return true;
      } else if (target.isSheared()) {
         performer.getCommunicator().sendNormalServerMessage("The creature is already sheared.", (byte)3);
         return true;
      } else if (!performer.getInventory().mayCreatureInsertItem()) {
         performer.getCommunicator().sendNormalServerMessage("You have no space left in your inventory to put what you shear.");
         return true;
      } else if (source.getTemplateId() != 394) {
         performer.getCommunicator().sendNormalServerMessage("You are not sure what to do with this tool.", (byte)3);
         return true;
      } else {
         target.shouldStandStill = true;
         Skill animalHusbandry = null;
         int time = 100;
         if (counter == 1.0F) {
            try {
               animalHusbandry = performer.getSkills().getSkill(10085);
            } catch (NoSuchSkillException var19) {
               animalHusbandry = performer.getSkills().learn(10085, 1.0F);
            }

            time = Actions.getSlowActionTime(performer, animalHusbandry, source, 0.0);
            act.setTimeLeft(time);
            performer.sendActionControl(Actions.actionEntrys[646].getVerbString(), true, time);
            performer.getCommunicator().sendNormalServerMessage("You start shearing the " + target.getName() + ".");
            Server.getInstance().broadCastAction(performer.getName() + " starts shearing the " + target.getName() + ".", performer, 5);
         } else {
            time = act.getTimeLeft();
         }

         if (counter * 10.0F > (float)time) {
            if (act.getRarity() != 0) {
               performer.playPersonalSound("sound.fx.drumroll");
            }

            try {
               animalHusbandry = performer.getSkills().getSkill(10085);
            } catch (NoSuchSkillException var18) {
               animalHusbandry = performer.getSkills().learn(10085, 1.0F);
            }

            int numberOfWoolProduced = getWoolProduceForAge(target.getStatus().age);
            int knowledge = (int)animalHusbandry.getKnowledge(0.0);
            float diff = (float)getShearingDifficulty(target.getStatus().age, (double)knowledge);
            double power = animalHusbandry.skillCheck((double)diff, source, 0.0, false, counter);
            target.setSheared(true);
            float ql = (float)knowledge + (float)(100 - knowledge) * ((float)power / 100.0F);
            ql = GeneralUtilities.calcRareQuality((double)ql, act.getRarity(), source.getRarity(), 0);
            float modifier = 1.0F;
            if (source.getSpellEffects() != null) {
               modifier = source.getSpellEffects().getRuneEffect(RuneUtilities.ModifierEffect.ENCH_RESGATHERED);
            }

            try {
               ItemTemplate woolTemplate = ItemTemplateFactory.getInstance().getTemplate(921);
               Item wool = ItemFactory.createItem(921, Math.max(1.0F, Math.min(100.0F, ql * modifier)), (byte)69, act.getRarity(), null);
               wool.setLastOwnerId(performer.getWurmId());
               wool.setWeight(numberOfWoolProduced * woolTemplate.getWeightGrams(), true);
               performer.getInventory().insertItem(wool);
               source.setDamage(source.getDamage() + 0.0015F * source.getDamageModifier());
               performer.getCommunicator().sendNormalServerMessage("You finish shearing the " + target.getName() + ".");
               Server.getInstance().broadCastAction(performer.getName() + " shears the " + target.getName() + ".", performer, 5);
            } catch (FailedException var16) {
               logger.log(Level.WARNING, var16.getMessage(), (Throwable)var16);
            } catch (NoSuchTemplateException var17) {
               logger.log(Level.WARNING, var17.getMessage(), (Throwable)var17);
            }

            return true;
         } else {
            return false;
         }
      }
   }

   private static final int getWoolProduceForAge(int age) {
      if (age < 3) {
         return 1;
      } else if (age < 8) {
         return 2;
      } else if (age < 12) {
         return 3;
      } else if (age < 30) {
         return 4;
      } else {
         return age < 40 ? 5 : 4;
      }
   }

   private static final int getShearingDifficulty(int age, double currentSkill) {
      if (age < 3) {
         return (int)currentSkill - 30;
      } else if (age < 8) {
         return (int)currentSkill - 25;
      } else if (age < 12) {
         return (int)currentSkill - 20;
      } else if (age < 30) {
         return (int)currentSkill - 15;
      } else {
         return age < 40 ? (int)currentSkill - 10 : (int)currentSkill - 5;
      }
   }

   static boolean milk(Action act, Item tool, Creature performer, Creature target, float counter) {
      if (!Methods.isActionAllowed(performer, (short)345)) {
         return true;
      } else if (!tool.isContainerLiquid()) {
         performer.getCommunicator().sendNormalServerMessage("You cannot keep milk in that.");
         return true;
      } else {
         Item[] cont = tool.getAllItems(true);
         if (cont.length >= 2) {
            performer.getCommunicator().sendNormalServerMessage("The milk would be destroyed. Empty the " + tool.getName() + " first.");
            return true;
         } else {
            if (cont.length == 1) {
               if (cont[0].getTemplateId() != 142 && cont[0].getTemplateId() != 1012 && cont[0].getTemplateId() != 1013) {
                  performer.getCommunicator().sendNormalServerMessage("The milk would be destroyed. Empty the " + tool.getName() + " first.");
                  return true;
               }

               if ((cont[0].getTemplateId() != 142 || target.getTemplate().getTemplateId() != 3)
                  && (cont[0].getTemplateId() != 1012 || target.getTemplate().getTemplateId() != 96)
                  && (cont[0].getTemplateId() != 1013 || target.getTemplate().getTemplateId() != 82)) {
                  performer.getCommunicator().sendNormalServerMessage("You cannot mix types of milk. Empty the " + tool.getName() + " first.");
                  return true;
               }
            }

            if (tool.getFreeVolume() == 0) {
               performer.getCommunicator()
                  .sendNormalServerMessage("The " + tool.getName() + " is already full and therefore will not be able to contain any milk.");
               return true;
            } else if (target.isMilked() && counter == 1.0F) {
               performer.getCommunicator().sendNormalServerMessage("The creature is already milked.");
               return true;
            } else {
               target.shouldStandStill = true;
               boolean toReturn = false;
               int time = 150;
               Skill skill = performer.getSkills().getSkillOrLearn(10060);
               time = Actions.getStandardActionTime(performer, skill, tool, 0.0);
               if (counter == 1.0F) {
                  if (tool.getFreeVolume() < 1000) {
                     performer.getCommunicator().sendNormalServerMessage("The " + tool.getName() + " will not be able to contain all of the milk.");
                  }

                  int maxSearches = calcMaxPasses(target, skill.getKnowledge(0.0), tool);
                  time = Actions.getQuickActionTime(performer, skill, null, 0.0);
                  act.setNextTick((float)time);
                  act.setTickCount(1);
                  act.setData(0L);
                  float totalTime = (float)(time * maxSearches);
                  act.setTimeLeft((int)totalTime);
                  performer.getCommunicator().sendNormalServerMessage("You start to milk the " + target.getName() + ".");
                  Server.getInstance().broadCastAction(performer.getName() + " starts to milk a " + target.getName() + ".", performer, 5);
                  performer.sendActionControl(Actions.actionEntrys[345].getVerbString(), true, (int)totalTime);
                  performer.getStatus().modifyStamina(-500.0F);
               } else {
                  time = act.getTimeLeft();
               }

               if (tool != null && act.justTickedSecond()) {
                  tool.setDamage(tool.getDamage() + 3.0E-4F * tool.getDamageModifier());
               }

               if (counter * 10.0F >= act.getNextTick()) {
                  int searchCount = act.getTickCount();
                  int maxSearches = calcMaxPasses(target, skill.getKnowledge(0.0), tool);
                  act.incTickCount();
                  act.incNextTick((float)Actions.getQuickActionTime(performer, skill, null, 0.0));
                  int knowledge = (int)skill.getKnowledge(0.0);
                  int diff = getShearingDifficulty(target.getStatus().age, (double)knowledge);
                  performer.getStatus().modifyStamina((float)(-1500 * searchCount));
                  int milkType = 142;
                  if (target.getTemplate().getTemplateId() == 96) {
                     milkType = 1012;
                  }

                  if (target.getTemplate().getTemplateId() == 82) {
                     milkType = 1013;
                  }

                  if (searchCount >= maxSearches) {
                     toReturn = true;
                  }

                  act.setData(act.getData() + 1L);
                  double power = skill.skillCheck((double)diff, tool, 0.0, false, counter / (float)searchCount);

                  try {
                     Item milk = ItemFactory.createItem(milkType, 100.0F, null);
                     milk.setName(target.getNameWithoutPrefixes() + " milk");
                     if (target.isReborn()) {
                        milk.setIsZombiefied(true);
                     }

                     SoundPlayer.playSound("sound.work.milking", target.getTileX(), target.getTileY(), true, 3.0F);
                     if (searchCount == 1) {
                        target.setMilked(true);
                     }

                     MethodsItems.fillContainer(act, tool, milk, performer, false);
                     if (searchCount < maxSearches) {
                        if (tool.getFreeVolume() == 0) {
                           performer.getCommunicator().sendNormalServerMessage("The " + tool.getName() + " is full, and cannot fit anymore milk in.");
                           Server.getInstance().broadCastAction(performer.getName() + " milks the " + target.getName() + ".", performer, 5);
                           return true;
                        }

                        if (tool.getFreeVolume() < 1000) {
                           performer.getCommunicator().sendNormalServerMessage("The " + tool.getName() + " will not be able to contain all of the milk.");
                        }
                     }
                  } catch (NoSuchTemplateException var17) {
                     logger.log(Level.WARNING, "No template for " + milkType, (Throwable)var17);
                     performer.getCommunicator().sendNormalServerMessage("You fail to milk. You realize something is wrong with the world.");
                     return true;
                  } catch (FailedException var18) {
                     logger.log(Level.WARNING, var18.getMessage(), (Throwable)var18);
                     performer.getCommunicator().sendNormalServerMessage("You fail to milk. You realize something is wrong with the world.");
                     return true;
                  }

                  if (toReturn) {
                     performer.getCommunicator().sendNormalServerMessage("You finish milking the " + target.getName() + ".");
                     Server.getInstance().broadCastAction(performer.getName() + " milks the " + target.getName() + ".", performer, 5);
                  }
               }

               return toReturn;
            }
         }
      }
   }

   public static final boolean catchFlies(Creature performer, Creature target, Item jar, short action, Action act, float counter) {
      int MIN_VINEGAR = 2;
      int MIN_HONEY = 10;
      boolean toReturn = false;
      if (target.isGhost() || target.isPlayer() || !target.isDomestic()) {
         performer.getCommunicator().sendNormalServerMessage("There are no flies on " + target.getHimHerItString() + "!");
         return true;
      } else if (!Methods.isActionAllowed(performer, (short)398)) {
         return true;
      } else if (!jar.isFlyTrap()) {
         performer.getCommunicator().sendNormalServerMessage("You cannot catch flies with that! You need honey or vinegar in the " + jar.getName() + ".");
         return true;
      } else {
         if (!target.canBeGroomed() && !(counter > 1.0F)) {
            performer.getCommunicator().sendNormalServerMessage("The " + target.getName() + " is already well tended and therefore has no flies.", (byte)3);
            toReturn = true;
         } else {
            Skill breeding = performer.getSkills().getSkillOrLearn(10085);
            int time = 20;
            target.shouldStandStill = true;
            int maxSearches = (int)((breeding.getKnowledge(0.0) + 28.0) / 27.0) + 1;
            if (counter == 1.0F) {
               act.setNextTick((float)time);
               act.setTickCount(1);
               float totalTime = (float)(time * maxSearches);
               act.setTimeLeft((int)totalTime);
               Server.getInstance().broadCastAction(performer.getName() + " starts to check a " + target.getName() + " for flies.", performer, 5);
               performer.getCommunicator().sendNormalServerMessage("You start to check " + target.getName() + " for flies.");
               performer.sendActionControl(Actions.actionEntrys[398].getVerbString(), true, act.getTimeLeft());
               performer.getStatus().modifyStamina(-500.0F);
               return false;
            }

            time = act.getTimeLeft();
            Item vinegar = jar.getVinegar();
            Item honey = jar.getHoney();
            if (vinegar != null) {
               if (vinegar.getWeightGrams() < 2) {
                  performer.getCommunicator().sendNormalServerMessage("There is not enough " + vinegar.getName() + " to interest a fly!");
                  return true;
               }
            } else {
               if (honey == null) {
                  performer.getCommunicator().sendNormalServerMessage("There is nothing in there to interest a fly!");
                  return true;
               }

               if (honey.getWeightGrams() < 10) {
                  performer.getCommunicator().sendNormalServerMessage("There is not enough " + honey.getName() + " to interest a fly!");
                  return true;
               }
            }

            if (counter * 10.0F > act.getNextTick()) {
               if (act.getRarity() != 0) {
                  performer.playPersonalSound("sound.fx.drumroll");
               }

               int searchCount = act.getTickCount();
               act.incTickCount();
               act.incNextTick(20.0F);
               performer.getStatus().modifyStamina((float)(-200 * searchCount));
               if (searchCount >= maxSearches) {
                  toReturn = true;
               }

               if (breeding.skillCheck((double)target.getBaseCombatRating(), jar, 0.0, false, counter / 2.0F) > 0.0) {
                  try {
                     target.setLastGroomed(System.currentTimeMillis());
                     int knowledge = (int)breeding.getKnowledge(0.0);
                     float ql = (float)knowledge - Server.rand.nextFloat() * (float)(knowledge / 5);
                     Item fly = ItemFactory.createItem(1359, ql, act.getRarity(), null);
                     jar.insertItem(fly, true);
                     performer.getCommunicator().sendNormalServerMessage("You manage to catch a fly in the jar!");
                     Server.getInstance().broadCastAction(performer.getName() + " puts something in a jar.", performer, 5);
                     if (searchCount < maxSearches && !jar.mayCreatureInsertItem()) {
                        performer.getCommunicator().sendNormalServerMessage("The jar is now full. You would have no space to put another fly.");
                        toReturn = true;
                     }

                     if (vinegar != null) {
                        vinegar.setWeight(vinegar.getWeightGrams() - 2, true);
                     } else if (honey != null) {
                        honey.setWeight(honey.getWeightGrams() - 10, true);
                     }
                  } catch (FailedException var18) {
                     logger.log(Level.WARNING, var18.getMessage(), (Throwable)var18);
                     toReturn = true;
                  } catch (NoSuchTemplateException var19) {
                     logger.log(Level.WARNING, var19.getMessage(), (Throwable)var19);
                     toReturn = true;
                  }

                  if (searchCount < maxSearches && !toReturn) {
                     act.setRarity(performer.getRarity());
                  }
               } else {
                  performer.getCommunicator().sendNormalServerMessage("The " + target.getName() + " shys away and interrupts the action.", (byte)3);
                  toReturn = true;
               }
            }
         }

         return toReturn;
      }
   }

   private static final int getCreatureAgeFactor(Creature creature) {
      int age = creature.getStatus().age;
      if (age < 3) {
         return 1;
      } else if (age < 8) {
         return 2;
      } else if (age < 12) {
         return 3;
      } else if (age < 30) {
         return 4;
      } else {
         return age < 40 ? 5 : 4;
      }
   }

   private static int calcMaxPasses(Creature creature, double currentSkill, Item tool) {
      int bonus = 0;
      if (tool.getSpellEffects() != null) {
         float extraChance = tool.getSpellEffects().getRuneEffect(RuneUtilities.ModifierEffect.ENCH_FARMYIELD) - 1.0F;
         if (extraChance > 0.0F && Server.rand.nextFloat() < extraChance) {
            ++bonus;
         }
      }

      return Math.min(getCreatureAgeFactor(creature), (int)(currentSkill + 28.0) / 27 + bonus);
   }

   static final void sendSetKingdomQuestion(Creature performer, Item wand) {
      if ((wand.getTemplateId() == 315 || wand.getTemplateId() == 176) && performer.getPower() >= 2) {
         SetKingdomQuestion question = new SetKingdomQuestion(performer, "Set kingdom", "Which player should receive which kingdom?", wand.getWurmId());
         question.sendQuestion();
      } else {
         SetKingdomQuestion question = new SetKingdomQuestion(performer, "Set kingdom", "Leave current kingdom?", wand.getWurmId());
         question.sendQuestion();
      }
   }

   public static final void sendChallengeKingQuestion(Creature responder) {
      if (responder.isKing()) {
         RoyalChallenge question = new RoyalChallenge(responder);
         question.sendQuestion();
      }
   }

   static final void sendAskKingdomQuestion(Creature performer, Creature target) {
      if (performer.isPlayer() && ((Player)performer).lastSentQuestion > 0) {
         performer.getCommunicator()
            .sendNormalServerMessage("You must wait another " + ((Player)performer).lastSentQuestion + " seconds before asking again.");
      } else {
         AskKingdomQuestion question = new AskKingdomQuestion(target, "Change kingdom", "Do you wish to change kingdom?", performer.getWurmId());
         question.sendQuestion();
         if (performer.isPlayer()) {
            ((Player)performer).lastSentQuestion = 60;
         }
      }
   }

   static final void sendAskPriestQuestion(Creature performer, @Nullable Item altar, @Nullable Creature creature) {
      if (performer.getDeity() != null && (performer.getFaith() == 30.0F || performer.getPower() > 0) && !performer.isPriest()) {
         if (!performer.isPaying()) {
            performer.getCommunicator().sendAlertServerMessage("You need to be a premium player in order to become a priest.", (byte)2);
            return;
         }

         long targ = -10L;
         if (altar != null) {
            targ = altar.getWurmId();
         } else if (creature != null) {
            targ = creature.getWurmId();
         }

         PriestQuestion question = new PriestQuestion(performer, "Becoming a priest", "Do you wish to become a priest?", targ);
         question.sendQuestion();
      }
   }

   static final boolean tame(Action act, Creature performer, Creature target, Item food, float counter) {
      boolean done = false;
      if (target.isDominated() && target.getDominator() != performer) {
         performer.getCommunicator().sendNormalServerMessage("The " + target.getName() + " ignores your attempts.");
         return true;
      } else if (target.getLeader() != null && target.getLeader().isPlayer() && target.getLeader() != performer) {
         performer.getCommunicator()
            .sendNormalServerMessage("The " + target.getName() + " seems focused on " + target.getLeader().getName() + " and ignores your attempts.");
         return true;
      } else if (target.getHitched() != null) {
         performer.getCommunicator().sendNormalServerMessage("The " + target.getName() + " is too restrained and ignores your attempts.");
         return true;
      } else if (!target.isDominatable(performer) || !target.isAnimal()) {
         performer.getCommunicator().sendNormalServerMessage("You fail to connect with the " + target.getName() + ".");
         return true;
      } else if (target.isFighting() && target.opponent != performer) {
         performer.getCommunicator().sendNormalServerMessage("The " + target.getName() + " is busy fighting " + target.opponent.getName() + ".");
         return true;
      } else if (target.isReborn()) {
         performer.getCommunicator().sendNormalServerMessage("The " + target.getName() + " shows no affection.");
         return true;
      } else if (performer.getPositionZ() + performer.getAltOffZ() < 0.0F) {
         performer.getCommunicator().sendNormalServerMessage("You can't tame while standing in the water. You need to be more mobile.");
         return true;
      } else if (performer.getCurrentTile() != null
         && target.getCurrentTile() != null
         && performer.getCurrentTile().getStructure() != target.getCurrentTile().getStructure()) {
         performer.getCommunicator().sendNormalServerMessage("You can't reach " + target.getName() + ".");
         return true;
      } else if (performer.getCurrentVillage() != target.getCurrentVillage()) {
         performer.getCommunicator().sendNormalServerMessage("The " + target.getName() + " sniffs the air and looks too nervous to focus.");
         return true;
      } else if (!Methods.isActionAllowed(performer, (short)46, target.getTileX(), target.getTileY())) {
         return true;
      } else {
         if (!Servers.isThisAPvpServer()) {
            Village bVill = target.getBrandVillage();
            if (bVill != null && !bVill.isActionAllowed((short)46, performer)) {
               performer.getCommunicator().sendNormalServerMessage("The " + target.getName() + " rolls its eyes and looks too nervous to focus.");
               return true;
            }
         }

         if (!performer.isWithinDistanceTo(target.getPosX(), target.getPosY(), target.getPositionZ(), 4.0F)) {
            performer.getCommunicator().sendNormalServerMessage("You are too far away to do that.");
            return true;
         } else {
            Skill taming = null;
            int time = 10000;
            if (counter == 1.0F) {
               if (performer.getStatus().getStamina() < 16000) {
                  performer.getCommunicator().sendNormalServerMessage("Taming is exhausting, and you are too tired right now.");
                  return true;
               }

               if (performer.getPet() != null && DbCreatureStatus.getIsLoaded(performer.getPet().getWurmId()) == 1) {
                  performer.getCommunicator().sendNormalServerMessage("You have a pet in a cage, remove it first, to tame this one.", (byte)3);
                  return true;
               }

               try {
                  taming = performer.getSkills().getSkill(10078);
               } catch (NoSuchSkillException var21) {
                  taming = performer.getSkills().learn(10078, 1.0F);
               }

               target.turnTowardsCreature(performer);
               time = Actions.getSlowActionTime(performer, taming, food, 0.0) / 2;
               act.setTimeLeft(time);
               performer.sendActionControl(Actions.actionEntrys[46].getVerbString(), true, time);
               performer.getCommunicator().sendNormalServerMessage("You start to tame the " + target.getName() + ".");
               Server.getInstance().broadCastAction(performer.getName() + " starts to tame the " + target.getName() + ".", performer, 5);
               performer.getStatus().modifyStamina(-1000.0F);
            } else {
               time = act.getTimeLeft();
            }

            if (Server.rand.nextInt(10) == 0 && !target.isFighting() && !target.isDominated() && target.isAggHuman()) {
               target.setTarget(performer.getWurmId(), true);
               performer.getCommunicator().sendNormalServerMessage(target.getName() + " becomes aggravated and attacks you!");
            }

            if (act.currentSecond() % 5 == 0) {
               performer.getStatus().modifyStamina(-5000.0F);
            }

            if (counter * 10.0F > (float)time) {
               try {
                  taming = performer.getSkills().getSkill(10078);
               } catch (NoSuchSkillException var20) {
                  taming = performer.getSkills().learn(10078, 1.0F);
               }

               Skill soul = null;

               try {
                  soul = target.getSkills().getSkill(105);
               } catch (NoSuchSkillException var19) {
                  logger.log(Level.WARNING, target.getName() + " no soul strength when " + performer.getName() + " tames.");
                  performer.getCommunicator().sendNormalServerMessage(target.getName() + " seems untamable.");
                  return true;
               }

               done = true;
               if (target.eat(food) < 10) {
                  performer.getCommunicator().sendNormalServerMessage("The " + target.getName() + " doesn't seem to bother.");
                  return true;
               }

               Skill ownsoul = null;

               try {
                  ownsoul = performer.getSkills().getSkill(105);
               } catch (NoSuchSkillException var18) {
                  ownsoul = performer.getSkills().learn(105, 20.0F);
               }

               boolean dryrun = false;
               if (target.getLoyalty() > 99.0F) {
                  dryrun = true;
                  performer.getCommunicator().sendNormalServerMessage(target.getName() + " is already tame, so you don't learn anything new.");
               }

               float difficultyBonus = target.getBaseCombatRating();
               double bonus = Math.max(0.0, ownsoul.skillCheck(soul.getKnowledge() + 5.0, (double)(-difficultyBonus), dryrun, counter));
               if (performer.getDeity() == Deities.getDeity(1) && performer.getFaith() > 20.0F && performer.getFavor() >= 20.0F) {
                  bonus += 20.0;
               }

               double power = taming.skillCheck(soul.getKnowledge() + (double)difficultyBonus, bonus, dryrun, counter);
               if (power > 0.0) {
                  double maxpower = taming.getKnowledge(0.0);
                  tameEffect(performer, target, power, dryrun, maxpower);
               } else {
                  boolean aggressive = false;
                  if (power < -50.0) {
                     if (!target.isFighting()) {
                        aggressive = true;
                        target.setTarget(performer.getWurmId(), true);
                        performer.getCommunicator().sendNormalServerMessage(target.getName() + " becomes aggravated and attacks you!");
                     }

                     if (target.isDominated() && target.getDominator() == performer) {
                        target.modifyLoyalty(-3.0F);
                     }
                  }

                  if (!aggressive) {
                     performer.getCommunicator().sendNormalServerMessage(target.getName() + " ignores your commands.");
                  }
               }
            }

            return done;
         }
      }
   }

   static void tameEffect(Creature performer, Creature target, double power, boolean dryrun, double maxpower) {
      target.setTarget(-10L, true);
      target.stopFighting();
      if (performer.getTarget() == target) {
         performer.setTarget(-10L, true);
         performer.stopFighting();
      }

      if (target.opponent == performer) {
         target.setOpponent(null);
      }

      if (performer.opponent == target) {
         performer.setOpponent(null);
      }

      boolean setAsNew = false;
      if (performer.getPet() != null) {
         if (performer.getPet() != target) {
            performer.getCommunicator().sendNormalServerMessage("You can't keep control over " + performer.getPet().getNameWithGenus() + " as well.");
            performer.getPet().setDominator(-10L);
            setAsNew = true;
         }
      } else {
         setAsNew = true;
      }

      if (setAsNew) {
         try {
            target.setKingdomId(performer.getKingdomId());
         } catch (IOException var9) {
            logger.log(Level.WARNING, var9.getMessage(), (Throwable)var9);
         }

         target.setDominator(performer.getWurmId());
         performer.setPet(target.getWurmId());
         target.setLoyalty((float)Math.min(maxpower, power));
         target.getStatus().setLastPolledLoyalty();
         if (dryrun) {
            performer.getCommunicator().sendNormalServerMessage("Your taming does not seem to have any effect.");
         } else {
            performer.getCommunicator().sendNormalServerMessage(target.getName() + " is now submissive to your will.");
         }

         performer.achievement(526);
      } else if (dryrun) {
         performer.getCommunicator().sendNormalServerMessage("Your taming does not seem to have any effect.");
      } else {
         target.setLoyalty((float)Math.min(maxpower, (double)target.getLoyalty() + power / 10.0));
         target.getStatus().setLastPolledLoyalty();
         performer.getCommunicator().sendNormalServerMessage("You create a stronger bond with " + target.getName() + ".");
      }

      Server.getInstance().broadCastAction(performer.getName() + " tames " + target.getNameWithGenus() + ".", performer, 5);
   }

   static final void petGiveAway(Creature dominator, Creature receiver) {
      if (receiver instanceof Player) {
         if (dominator.getPet() != null && DbCreatureStatus.getIsLoaded(dominator.getPet().getWurmId()) == 1) {
            dominator.getCommunicator().sendNormalServerMessage("Your pet is in a cage you must first remove it to give it away.", (byte)3);
            return;
         }

         if (!receiver.acceptsInvitations()) {
            dominator.getCommunicator()
               .sendNormalServerMessage(
                  receiver.getName()
                     + " does not accept pets now. "
                     + LoginHandler.raiseFirstLetter(receiver.getHeSheItString())
                     + " needs to type /invitations in a chat window."
               );
            return;
         }

         if (dominator.getAttitude(receiver) != 2) {
            if (receiver.getPet() == null) {
               if (dominator.getPet() != null) {
                  try {
                     if (dominator.getPet().isReborn() || dominator.getPet().isMonster()) {
                        dominator.getCommunicator().sendNormalServerMessage("You control that creature with your will. It can not be given away.");
                        return;
                     }

                     Skill taming;
                     try {
                        taming = receiver.getSkills().getSkill(10078);
                     } catch (NoSuchSkillException var13) {
                        taming = receiver.getSkills().learn(10078, 1.0F);
                     }

                     Skill soul = null;

                     try {
                        soul = dominator.getPet().getSkills().getSkill(105);
                     } catch (NoSuchSkillException var12) {
                        logger.log(Level.WARNING, dominator.getPet().getName() + " no soul strength when " + receiver.getName() + " tames.");
                        receiver.getCommunicator().sendNormalServerMessage(dominator.getPet().getName() + " seems untamable.");
                        return;
                     }

                     Skill ownsoul = null;

                     try {
                        ownsoul = receiver.getSkills().getSkill(105);
                     } catch (NoSuchSkillException var11) {
                        ownsoul = receiver.getSkills().learn(105, 20.0F);
                     }

                     boolean dryrun = System.currentTimeMillis() - taming.lastUsed < 60000L;
                     float difficultyBonus = 0.0F;
                     if (receiver.getKingdomTemplateId() == 3) {
                        difficultyBonus = 20.0F;
                     }

                     double bonus = Math.max(0.0, ownsoul.skillCheck(soul.getKnowledge(0.0), 0.0, dryrun, 1.0F));
                     double power = taming.skillCheck(soul.getKnowledge(0.0) + (double)difficultyBonus, bonus, dryrun, 1.0F);
                     if (power > 0.0) {
                        dominator.getPet().setLeader(null);
                        receiver.setPet(dominator.getPet().getWurmId());
                        dominator.getPet().setDominator(receiver.getWurmId());
                        dominator.setPet(-10L);
                        dominator.getCommunicator().sendNormalServerMessage("You give away your pet to " + receiver.getName() + ".");
                        receiver.getCommunicator()
                           .sendNormalServerMessage(dominator.getName() + " gives you " + dominator.getHisHerItsString() + " " + receiver.getPet().getName());
                     } else if (power < -30.0) {
                        if (!dominator.getPet().isFighting()) {
                           dominator.getPet().setTarget(receiver.getWurmId(), true);
                           receiver.getCommunicator().sendNormalServerMessage(dominator.getPet().getName() + " becomes aggravated and attacks you!");
                        }

                        dominator.getCommunicator().sendAlertServerMessage(dominator.getPet().getName() + " is tame no more.");
                        dominator.getPet().setDominator(-10L);
                        dominator.setPet(-10L);
                     } else {
                        dominator.getCommunicator()
                           .sendAlertServerMessage(receiver.getName() + " fails to take control over the " + dominator.getPet().getName() + ".");
                     }
                  } catch (Exception var14) {
                     logger.log(Level.WARNING, dominator.getName() + " and " + receiver.getName() + ": " + var14.getMessage(), (Throwable)var14);
                  }
               } else {
                  dominator.getCommunicator().sendNormalServerMessage("You don't have a pet.");
               }
            } else {
               dominator.getCommunicator().sendNormalServerMessage(receiver.getName() + " already has a pet.");
            }
         } else {
            dominator.getCommunicator().sendNormalServerMessage(receiver.getName() + " is an enemy.");
         }
      } else {
         dominator.getCommunicator().sendNormalServerMessage(receiver.getName() + " can't control a pet.");
      }
   }

   static boolean findCaveExit(Creature performer, Creature orderer) {
      int tilePosX = performer.getCurrentTile().tilex;
      int tilePosY = performer.getCurrentTile().tiley;
      int[] tiles = new int[]{tilePosX, tilePosY};
      if (!performer.isOnSurface()) {
         if (performer.getCurrentTile().isTransition) {
            performer.setLayer(0, true);
         } else {
            tiles = performer.findRandomCaveExit(tiles);
            tilePosX = tiles[0];
            tilePosY = tiles[1];
            if (tilePosX == performer.getCurrentTile().tilex && tilePosY == performer.getCurrentTile().tiley) {
               orderer.getCommunicator().sendNormalServerMessage("Failed to find exit");
            } else {
               orderer.getCommunicator().sendNormalServerMessage(performer.getName() + " found cave exit at " + tilePosX + ", " + tilePosY);

               try {
                  PathFinder pf = new PathFinder();
                  Path path = pf.findPath(
                     performer, performer.getTileX(), (int)performer.getStatus().getPositionY() >> 2, tilePosX, tilePosY, performer.isOnSurface(), 10
                  );
                  if (path != null) {
                     while(!path.isEmpty()) {
                        try {
                           PathTile p = path.getFirst();
                           ItemFactory.createItem(
                              344,
                              100.0F,
                              (float)((p.getTileX() << 2) + 2),
                              (float)((p.getTileY() << 2) + 2),
                              180.0F,
                              performer.isOnSurface(),
                              (byte)0,
                              -10L,
                              null
                           );
                           path.removeFirst();
                        } catch (FailedException var8) {
                           logger.log(Level.INFO, performer.getName() + " " + var8.getMessage(), (Throwable)var8);
                           orderer.getCommunicator().sendNormalServerMessage("Failed to create marker.");
                        } catch (NoSuchTemplateException var9) {
                           logger.log(Level.INFO, performer.getName() + " " + var9.getMessage(), (Throwable)var9);
                           orderer.getCommunicator().sendNormalServerMessage("Failed to create marker.");
                        }
                     }
                  } else {
                     orderer.getCommunicator().sendNormalServerMessage("No path available.");
                  }
               } catch (NoPathException var10) {
                  orderer.getCommunicator().sendNormalServerMessage("Failed to find path: " + var10.getMessage());
               }
            }
         }
      }

      return true;
   }

   static final boolean stealth(Creature performer, float counter) {
      boolean done = true;
      if (Servers.localServer.PVPSERVER && performer.getBodyControlSkill().getRealKnowledge() < 21.0) {
         performer.getCommunicator().sendNormalServerMessage("You need 21 body control skill in order to stealth on PvP servers.", (byte)3);
         return done;
      } else if (performer.isUndead()) {
         performer.getCommunicator().sendNormalServerMessage("Hurr hfff sss.");
         return done;
      } else if (performer.isStealth()) {
         performer.setStealth(false);
         return true;
      } else {
         done = false;
         if (counter == 1.0F) {
            VolaTile t = performer.getCurrentTile();
            if (t != null) {
               for(VolaTile lVolaTile : t.getThisAndSurroundingTiles(1)) {
                  Creature[] crets = lVolaTile.getCreatures();

                  for(Creature lCret : crets) {
                     if (lCret != performer && lCret.getPower() <= 0) {
                        performer.getCommunicator().sendToggle(3, false);
                        performer.getCommunicator().sendNormalServerMessage("You are too close to other creatures now.", (byte)3);
                        return true;
                     }
                  }
               }
            }

            performer.sendActionControl(Actions.actionEntrys[136].getVerbString(), true, (int)Math.max(50.0, (35.0 - performer.getBodyControl()) * 10.0));
            performer.getCommunicator().sendNormalServerMessage("You try to hide.");
         } else if ((double)counter > Math.max(5.0, 35.0 - performer.getBodyControl())) {
            done = true;
            performer.getStatus().modifyStamina(-4000.0F);
            VolaTile t = performer.getCurrentTile();
            if (t != null) {
               double mod = (double)getStealthTerrainModifier(performer, t.tilex, t.tiley, performer.isOnSurface());
               if ((double)(Server.rand.nextFloat() * 100.0F) < mod) {
                  performer.setStealth(true);
                  performer.achievement(114);

                  try {
                     boolean found = false;
                     Item lhand = performer.getBody().getBodyPart(13);
                     Set<Item> wornl = lhand.getItems();
                     if (wornl != null) {
                        for(Item i : wornl) {
                           if (i.getTemplateId() == 297 && i.getMaterial() == 7) {
                              performer.achievement(93);
                              found = true;
                              break;
                           }
                        }
                     }

                     if (!found) {
                        Item rhand = performer.getBody().getBodyPart(14);
                        Set<Item> wornr = rhand.getItems();
                        if (wornr != null) {
                           for(Item i : wornr) {
                              if (i.getTemplateId() == 297 && i.getMaterial() == 7) {
                                 performer.achievement(93);
                                 break;
                              }
                           }
                        }
                     }
                  } catch (NoSpaceException var13) {
                  }
               } else {
                  performer.getCommunicator().sendToggle(3, false);
                  performer.getCommunicator().sendNormalServerMessage("You fail to hide.", (byte)3);
               }
            } else {
               performer.getCommunicator().sendToggle(3, false);
               performer.getCommunicator().sendNormalServerMessage("You may not hide here.", (byte)3);
            }
         }

         return done;
      }
   }

   static final boolean unbrand(Creature performer, Creature target, Item source, Action act, float counter) {
      if (performer == null || target == null) {
         return true;
      } else if (!performer.isPaying()) {
         performer.getCommunicator().sendNormalServerMessage("You need to have premium time left in order to remove a brand from creatures.", (byte)3);
         return true;
      } else if (!source.isOnFire()) {
         performer.getCommunicator()
            .sendNormalServerMessage("The " + source.getName() + " must be very hot if you're going to remove a brand with it.", (byte)3);
         return true;
      } else {
         if (target.isLeadable(performer)
            && performer.getCitizenVillage() != null
            && performer.isWithinTileDistanceTo(target.getTileX(), target.getTileY(), (int)(target.getStatus().getPositionZ() + target.getAltOffZ()) >> 2, 3)
            && target.getCurrentVillage() != null
            && target.getCurrentVillage() == performer.getCitizenVillage()) {
            if (!performer.getCitizenVillage().isActionAllowed((short)484, performer)) {
               return true;
            }

            Brand brand = Creatures.getInstance().getBrand(target.getWurmId());
            if (brand != null) {
               try {
                  Village villageBrand = Villages.getVillage((int)brand.getBrandId());
                  if (villageBrand == performer.getCitizenVillage()) {
                     if (counter == 1.0F) {
                        Server.getInstance()
                           .broadCastAction(performer.getName() + " prepares to remove brand on " + target.getNameWithGenus() + ".", performer, 5);
                        performer.getCommunicator().sendNormalServerMessage("You prepare to remove brand on the " + target.getName() + ".");
                        performer.sendActionControl(Actions.getVerbForAction((short)643), true, 100);
                        return false;
                     }

                     if (act.currentSecond() == 5) {
                        Server.getInstance()
                           .broadCastAction(
                              performer.getName() + " promptly raises " + performer.getHisHerItsString() + " " + source.getName() + ".", performer, 5
                           );
                        performer.getCommunicator().sendNormalServerMessage("Determined, you raise your " + source.getName() + ".");
                        return false;
                     }

                     if (counter > 10.0F) {
                        SoundPlayer.playSound(target.getDeathSound(), target, 1.0F);
                        Server.getInstance()
                           .broadCastAction(
                              "The " + target.getName() + " makes a horrible sound as " + performer.getName() + " removes the brand.", performer, 5
                           );
                        performer.getCommunicator()
                           .sendNormalServerMessage(
                              "The "
                                 + target.getName()
                                 + " makes a horrible sound as you press your "
                                 + source.getName()
                                 + " against it and remove the brand."
                           );
                        source.setQualityLevel(source.getQualityLevel() - 0.1F);
                        brand.deleteBrand();
                        if (target.getVisionArea() != null) {
                           target.getVisionArea().broadCastUpdateSelectBar(target.getWurmId());
                        }

                        return true;
                     }

                     return false;
                  }

                  performer.getCommunicator().sendNormalServerMessage("The " + target.getName() + " already has a brand.", (byte)3);
                  return true;
               } catch (NoSuchVillageException var7) {
                  performer.getCommunicator().sendNormalServerMessage("The " + target.getName() + " already has a brand.", (byte)3);
                  return true;
               }
            }
         }

         return true;
      }
   }

   static final boolean brand(Creature performer, Creature target, Item source, Action act, float counter) {
      boolean toReturn = true;
      if (performer == null || target == null) {
         return true;
      } else if (!performer.isPaying()) {
         performer.getCommunicator().sendNormalServerMessage("You need to have premium time left in order to brand creatures.", (byte)3);
         return true;
      } else if (!source.isOnFire()) {
         performer.getCommunicator().sendNormalServerMessage("The " + source.getName() + " must be very hot if you're going to brand with it.", (byte)3);
         return true;
      } else {
         if (target.isLeadable(performer)
            && performer.getCitizenVillage() != null
            && performer.isWithinTileDistanceTo(target.getTileX(), target.getTileY(), (int)(target.getStatus().getPositionZ() + target.getAltOffZ()) >> 2, 3)
            && target.getCurrentVillage() != null
            && target.getCurrentVillage() == performer.getCitizenVillage()) {
            if (!performer.getCitizenVillage().isActionAllowed((short)484, performer)) {
               return true;
            }

            Brand brand = Creatures.getInstance().getBrand(target.getWurmId());
            if (brand == null) {
               if (!Servers.isThisAPvpServer()
                  && target.getCurrentVillage().getMaxCitizens() <= Creatures.getInstance().getBranded((long)performer.getCurrentVillage().getId()).length) {
                  performer.getCommunicator().sendNormalServerMessage("Branding this animal would exceed your settlement's branded limit.", (byte)3);
                  return true;
               }

               toReturn = false;
               if (counter == 1.0F) {
                  Server.getInstance().broadCastAction(performer.getName() + " prepares to brand " + target.getNameWithGenus() + ".", performer, 5);
                  performer.getCommunicator().sendNormalServerMessage("You prepare to brand the " + target.getName() + ".");
                  performer.sendActionControl(Actions.getVerbForAction((short)484), true, 100);
               } else if (act.currentSecond() == 5) {
                  Server.getInstance()
                     .broadCastAction(performer.getName() + " promptly raises " + performer.getHisHerItsString() + " " + source.getName() + ".", performer, 5);
                  performer.getCommunicator().sendNormalServerMessage("Determined, you raise your " + source.getName() + ".");
               } else if (counter > 10.0F) {
                  toReturn = true;
                  SoundPlayer.playSound(target.getDeathSound(), target, 1.0F);
                  Server.getInstance()
                     .broadCastAction("The " + target.getName() + " makes a horrible sound as " + performer.getName() + " brands it.", performer, 5);
                  performer.getCommunicator()
                     .sendNormalServerMessage("The " + target.getName() + " makes a horrible sound as you press your " + source.getName() + " against it.");
                  source.setQualityLevel(source.getQualityLevel() - 0.1F);
                  Creatures.getInstance().setBrand(target.getWurmId(), (long)performer.getCitizenVillage().getId());
                  if (target.getVisionArea() != null) {
                     target.getVisionArea().broadCastUpdateSelectBar(target.getWurmId());
                  }
               }
            } else {
               toReturn = true;
            }
         }

         return toReturn;
      }
   }

   static final boolean naturalBreed(Creature breeder, Creature target, Action act, float counter) {
      boolean toReturn = true;
      if (breeder != null && target != null) {
         if (breeder.mayMate(target) && !target.isPregnant() && !breeder.isPregnant()) {
            if (!breeder.isWithinDistanceTo(target.getPosX(), target.getPosY(), target.getPositionZ() + target.getAltOffZ(), 3.0F, 0.0F)) {
               return true;
            }

            if (breeder.getEggTemplateId() != -1 || target.getEggTemplateId() != -1) {
               return true;
            }

            if (!breeder.isInTheMoodToBreed(false)) {
               return true;
            }

            toReturn = false;
            if (counter == 1.0F) {
               Server.getInstance().broadCastAction("The " + breeder.getName() + " and the " + target.getName() + " gets to it!", breeder, 5);
            } else if (counter > (float)(30 + Server.rand.nextInt(20))) {
               toReturn = true;
               if (Server.rand.nextInt(30) == 0) {
                  if (breeder.getSex() == 1) {
                     breeder.mate(target, null);
                     Server.getInstance()
                        .broadCastAction(
                           "Something seems to have happened between the " + breeder.getName() + " and the " + target.getName() + "!", breeder, 5
                        );
                  } else if (target.getSex() == 1) {
                     target.mate(breeder, null);
                     Server.getInstance()
                        .broadCastAction(
                           "Something seems to have happened between the " + breeder.getName() + " and the " + target.getName() + "!", breeder, 5
                        );
                  }
               } else {
                  Server.getInstance()
                     .broadCastAction("The " + breeder.getName() + " and the " + target.getName() + " stops whatever they were doing.", breeder, 5);
               }

               target.resetBreedCounter();
               breeder.resetBreedCounter();
            }
         }

         return toReturn;
      } else {
         return true;
      }
   }

   static final boolean breed(Creature performer, Creature target, short action, Action act, float counter) {
      boolean toReturn = true;
      if (target.isGhost()) {
         performer.getCommunicator().sendNormalServerMessage("The " + target.getName() + " can't get pregnant!", (byte)3);
         return true;
      } else if (performer.getPower() >= 5 && target.isPregnant()) {
         if (target.checkPregnancy(true)) {
            performer.getCommunicator().sendNormalServerMessage("The " + target.getName() + " gives birth!");
         } else {
            performer.getCommunicator().sendNormalServerMessage("The " + target.getName() + " fails to give birth!");
         }

         return true;
      } else {
         if (performer.getFollowers().length == 1) {
            Creature breeder = performer.getFollowers()[0];
            if (breeder == null) {
               performer.getCommunicator().sendNormalServerMessage("Which creature should breed with the " + target.getName() + "?", (byte)3);
               return true;
            }

            if (!Methods.isActionAllowed(performer, (short)379)) {
               return true;
            }

            if (breeder.mayMate(target)) {
               if (!target.isPregnant()) {
                  if (!breeder.isPregnant()) {
                     if (!breeder.isWithinDistanceTo(target.getPosX(), target.getPosY(), target.getPositionZ() + target.getAltOffZ(), 3.0F, 0.0F)) {
                        performer.getCommunicator().sendNormalServerMessage("The creatures are too far apart.");
                        performer.sendToLoggers(
                           breeder.getName()
                              + " at "
                              + breeder.getPosX()
                              + ","
                              + breeder.getPosY()
                              + ", "
                              + target.getName()
                              + " at "
                              + target.getPosX()
                              + ","
                              + target.getPosY()
                              + "."
                        );
                        performer.sendToLoggers(Math.abs(breeder.getStatus().getPositionX() - (target.getPosX() + 0.0F)) + " < " + 3 + " is false or");
                        performer.sendToLoggers(Math.abs(breeder.getStatus().getPositionY() - (target.getPosY() + 0.0F)) + " < " + 3 + " is false");
                        return true;
                     }

                     BlockingResult result = Blocking.getBlockerBetween(breeder, target, 4);
                     if (result != null) {
                        performer.getCommunicator()
                           .sendNormalServerMessage("The creatures are separated by the " + result.getFirstBlocker().getName() + ".", (byte)3);
                        return true;
                     }

                     if (breeder.getEggTemplateId() != -1 || target.getEggTemplateId() != -1) {
                        performer.getCommunicator().sendNormalServerMessage("You can't breed egglayers.", (byte)3);
                        return true;
                     }

                     if (!breeder.isInTheMoodToBreed(true) && performer.getPower() < 5) {
                        performer.getCommunicator()
                           .sendNormalServerMessage("The " + breeder.getName() + " doesn't seem to be in the mood right now.", (byte)3);
                        return true;
                     }

                     if (!target.isInTheMoodToBreed(true) && performer.getPower() < 5) {
                        performer.getCommunicator().sendNormalServerMessage("The " + target.getName() + " doesn't seem to be in the mood right now.", (byte)3);
                        return true;
                     }

                     toReturn = false;
                     target.shouldStandStill = true;
                     if (counter == 1.0F) {
                        act.setTimeLeft(100 + Server.rand.nextInt(300));
                        Server.getInstance()
                           .broadCastAction(
                              performer.getName()
                                 + " does "
                                 + performer.getHisHerItsString()
                                 + " job. The "
                                 + breeder.getName()
                                 + " and the "
                                 + target.getName()
                                 + " get intimate!",
                              performer,
                              5
                           );
                        performer.getCommunicator()
                           .sendNormalServerMessage(
                              "The "
                                 + breeder.getName()
                                 + " and the "
                                 + target.getName()
                                 + " get intimate."
                                 + (performer.getPower() >= 5 ? target.getBreedCounter() + ", " + breeder.getBreedCounter() : "")
                           );
                        if (performer.isPlayer()) {
                           performer.sendActionControl(Actions.actionEntrys[379].getVerbString(), true, act.getTimeLeft());
                        }
                     } else if (performer.getPower() >= 5 || counter > (float)(act.getTimeLeft() / 10)) {
                        toReturn = true;
                        Skill breeding = null;

                        try {
                           var11 = performer.getSkills().getSkill(10085);
                        } catch (NoSuchSkillException var9) {
                           var11 = performer.getSkills().learn(10085, 1.0F);
                        }

                        if (var11.skillCheck((double)breeder.getBaseCombatRating(), 0.0, false, performer.getPower() >= 5 ? 20.0F : counter) > 0.0) {
                           if (breeder.getSex() == 1) {
                              breeder.mate(target, performer);
                              performer.getCommunicator()
                                 .sendNormalServerMessage("The " + breeder.getName() + " will probably give birth in a while!", (byte)2);
                           } else if (target.getSex() == 1) {
                              target.mate(breeder, performer);
                              performer.getCommunicator()
                                 .sendNormalServerMessage("The " + target.getName() + " will probably give birth in a while!", (byte)2);
                           } else {
                              performer.getCommunicator()
                                 .sendNormalServerMessage(
                                    "Weird. You realize that since none of the creatures is female there will probably be no offspring..", (byte)3
                                 );
                           }

                           performer.achievement(531);
                        } else {
                           performer.getCommunicator().sendNormalServerMessage("The " + breeder.getName() + " shys away and interrupts the action.");
                        }

                        if (performer.getPower() < 5) {
                           target.resetBreedCounter();
                           breeder.resetBreedCounter();
                        }
                     }
                  } else {
                     performer.getCommunicator().sendNormalServerMessage("The " + breeder.getName() + " is already pregnant.", (byte)3);
                  }
               } else {
                  performer.getCommunicator().sendNormalServerMessage("The " + target.getName() + " is already pregnant.", (byte)3);
               }
            } else {
               performer.getCommunicator().sendNormalServerMessage("The " + breeder.getName() + " may not mate with " + target.getName() + ".", (byte)3);
            }
         } else {
            performer.getCommunicator().sendNormalServerMessage("Which creature should breed? You must select only one follower.", (byte)3);
         }

         return toReturn;
      }
   }

   static final boolean ride(Creature performer, Creature target, short action) {
      boolean addedPassenger = false;
      boolean addedDriver = false;
      if ((action == 331 || action == 332) && performer.getVehicle() == -10L) {
         if (target.getDominator() != null && performer.getKingdomId() != target.getDominator().getKingdomId()) {
            performer.getCommunicator().sendNormalServerMessage("The " + target.getName() + " refuses to let you embark.", (byte)3);
            return true;
         }

         if (target.getHitched() != null) {
            performer.getCommunicator().sendNormalServerMessage("The " + target.getName() + " is too occupied to be ridden at the moment.", (byte)3);
            return true;
         }

         if (!performer.isWithinDistanceTo(target.getPosX(), target.getPosY(), target.getPositionZ() + target.getAltOffZ(), 8.0F)) {
            performer.getCommunicator().sendNormalServerMessage("You are too far away now.", (byte)3);
         } else {
            if (target.getStrengthSkill() < 20.0) {
               performer.getCommunicator().sendNormalServerMessage("The " + target.getName() + " is too weak to ride.", (byte)3);
               return true;
            }

            if (target.isOnSurface() != performer.isOnSurface()) {
               if (performer.isOnSurface()) {
                  performer.getCommunicator().sendNormalServerMessage("You need to enter the cave first.", (byte)3);
               } else {
                  performer.getCommunicator().sendNormalServerMessage("You need to leave the cave first.", (byte)3);
               }

               return true;
            }

            if (!GeneralUtilities.isOnSameLevel(performer, target)) {
               performer.getCommunicator().sendNormalServerMessage("You need to be on the same floor level first.", (byte)3);
               return true;
            }

            if (performer.getBridgeId() != target.getBridgeId()) {
               performer.getCommunicator().sendNormalServerMessage("You need to be in the same structure as the " + target.getName() + ".", (byte)3);
               return true;
            }

            if (!target.canUseWithEquipment()) {
               performer.getCommunicator()
                  .sendNormalServerMessage("The " + target.getName() + " looks confused by its equipment and refuses to move.", (byte)3);
               return true;
            }

            Vehicle vehicle = Vehicles.getVehicle(target);
            if (vehicle == null) {
               performer.getCommunicator().sendNormalServerMessage("You can't embark on that right now.", (byte)3);
            } else {
               String vehicName = Vehicle.getVehicleName(vehicle);
               if (action != 331) {
                  if (action == 332) {
                     if (VehicleBehaviour.mayEmbarkVehicle(performer, target)) {
                        if (performer.getDraggedItem() == null) {
                           addedPassenger = false;
                           boolean wallInWay = false;

                           for(int x = 0; x < vehicle.seats.length; ++x) {
                              if (!vehicle.seats[x].isOccupied() && vehicle.seats[x].type == 1) {
                                 float r = -(target.getStatus().getRotation() + 180.0F) * (float) Math.PI / 180.0F;
                                 float s = (float)Math.sin((double)r);
                                 float c = (float)Math.cos((double)r);
                                 float xo = s * -vehicle.seats[x].offx - c * -vehicle.seats[x].offy;
                                 float yo = c * -vehicle.seats[x].offx + s * -vehicle.seats[x].offy;
                                 float newposx = target.getPosX() + xo;
                                 float newposy = target.getPosY() + yo;
                                 BlockingResult result = Blocking.getBlockerBetween(
                                    performer,
                                    performer.getPosX(),
                                    performer.getPosY(),
                                    newposx,
                                    newposy,
                                    performer.getPositionZ(),
                                    target.getPositionZ(),
                                    performer.isOnSurface(),
                                    target.isOnSurface(),
                                    false,
                                    4,
                                    -1L,
                                    performer.getBridgeId(),
                                    target.getBridgeId(),
                                    false
                                 );
                                 if (result == null) {
                                    addedPassenger = true;
                                    vehicle.seats[x].occupy(vehicle, performer);
                                    MountAction m = new MountAction(target, null, vehicle, x, false, vehicle.seats[x].offz);
                                    if (performer.getVisionArea() != null) {
                                       performer.getVisionArea().broadCastUpdateSelectBar(performer.getWurmId(), true);
                                    }

                                    performer.setMountAction(m);
                                    performer.setVehicle(target.getWurmId(), true, (byte)1);
                                    break;
                                 }

                                 wallInWay = true;
                              }
                           }

                           if (!addedPassenger) {
                              String text = "You may not %s the %s as a passenger right now.";
                              performer.getCommunicator()
                                 .sendNormalServerMessage(
                                    StringUtil.format("You may not %s the %s as a passenger right now.", vehicle.embarkString, vehicName)
                                 );
                              if (wallInWay) {
                                 performer.getCommunicator().sendNormalServerMessage("The wall is in the way. You can not reach a seat.", (byte)3);
                              } else {
                                 performer.getCommunicator().sendNormalServerMessage("The seats are all occupied.", (byte)3);
                              }
                           }
                        } else {
                           String text = "You may not %s the %s while dragging something.";
                           performer.getCommunicator()
                              .sendNormalServerMessage(
                                 StringUtil.format("You may not %s the %s while dragging something.", vehicle.embarkString, vehicName), (byte)3
                              );
                        }
                     } else {
                        String text = "You are not allowed to %s the %s.";
                        performer.getCommunicator()
                           .sendNormalServerMessage(StringUtil.format("You are not allowed to %s the %s.", vehicle.embarkString, vehicName), (byte)3);
                     }
                  }
               } else {
                  if (target.getHitched() != null) {
                     String text = "You can not control the %s while it is in front of the %s.";
                     performer.getCommunicator()
                        .sendNormalServerMessage(
                           StringUtil.format(
                              "You can not control the %s while it is in front of the %s.", target.getName(), Vehicle.getVehicleName(target.getHitched())
                           ),
                           (byte)3
                        );
                     return false;
                  }

                  if (performer.getDraggedItem() != null) {
                     String text = "You may not drag %s while trying to control the %s.";
                     performer.getCommunicator()
                        .sendNormalServerMessage(
                           StringUtil.format("You may not drag %s while trying to control the %s.", performer.getDraggedItem().getNameWithGenus(), vehicName),
                           (byte)3
                        );
                     return false;
                  }

                  if (!VehicleBehaviour.mayDriveVehicle(performer, target)) {
                     String text = "You are not allowed to %s the %s as a %s.";
                     performer.getCommunicator()
                        .sendNormalServerMessage(
                           StringUtil.format("You are not allowed to %s the %s as a %s.", vehicle.embarkString, vehicName, vehicle.pilotName), (byte)3
                        );
                  } else if (!VehicleBehaviour.canBeDriverOfVehicle(performer, vehicle)) {
                     String text = "You are not agile enough to control the %s. You need %.2f in %s.";
                     performer.getCommunicator()
                        .sendNormalServerMessage(
                           StringUtil.format(
                              "You are not agile enough to control the %s. You need %.2f in %s.", vehicName, vehicle.skillNeeded, SkillSystem.getNameFor(104)
                           ),
                           (byte)3
                        );
                  } else {
                     addedDriver = false;
                     int x = 0;

                     while(true) {
                        if (x < vehicle.seats.length) {
                           if (vehicle.seats[x].isOccupied() || vehicle.seats[x].type != 0) {
                              ++x;
                              continue;
                           }

                           float r = -(target.getStatus().getRotation() + 180.0F) * (float) Math.PI / 180.0F;
                           float s = (float)Math.sin((double)r);
                           float c = (float)Math.cos((double)r);
                           float xo = s * -vehicle.seats[x].offx - c * -vehicle.seats[x].offy;
                           float yo = c * -vehicle.seats[x].offx + s * -vehicle.seats[x].offy;
                           float newposx = target.getPosX() + xo;
                           float newposy = target.getPosY() + yo;
                           BlockingResult result = Blocking.getBlockerBetween(
                              performer,
                              performer.getPosX(),
                              performer.getPosY(),
                              newposx,
                              newposy,
                              performer.getPositionZ(),
                              target.getPositionZ(),
                              performer.isOnSurface(),
                              target.isOnSurface(),
                              false,
                              4,
                              -1L,
                              performer.getBridgeId(),
                              target.getBridgeId(),
                              false
                           );
                           if (result != null) {
                              performer.getCommunicator()
                                 .sendNormalServerMessage("The " + result.getFirstBlocker().getName() + " is in the way. You can not reach the seat.", (byte)3);
                              return true;
                           }

                           addedDriver = true;
                           vehicle.seats[x].occupy(vehicle, performer);
                           vehicle.pilotId = performer.getWurmId();
                           performer.setVehicleCommander(true);
                           MountAction m = new MountAction(target, null, vehicle, x, true, vehicle.seats[x].offz);
                           if (performer.getVisionArea() != null) {
                              performer.getVisionArea().broadCastUpdateSelectBar(performer.getWurmId(), true);
                           }

                           performer.setMountAction(m);
                           performer.setVehicle(target.getWurmId(), true, (byte)0);
                           if (target.getTemplateId() == 3) {
                              performer.achievement(521);
                           }

                           if (target.getTemplateId() == 64) {
                              performer.achievement(535);
                           }

                           if (target.getTemplateId() == 12 || target.getTemplateId() == 42 || target.getTemplateId() == 49) {
                              performer.achievement(545);
                           }

                           if (target.getTemplateId() == 21) {
                              performer.achievement(594);
                           }
                        }

                        if (!addedDriver) {
                           String text = "You may not %s the %s as a %s right now. The space is occupied.";
                           performer.getCommunicator()
                              .sendNormalServerMessage(
                                 StringUtil.format(
                                    "You may not %s the %s as a %s right now. The space is occupied.", vehicle.embarkString, vehicName, vehicle.pilotName
                                 ),
                                 (byte)3
                              );
                        }
                        break;
                     }
                  }
               }
            }
         }
      }

      return true;
   }

   public static final float getStealthTerrainModifier(Creature performer, int tilex, int tiley, boolean surfaced) {
      float mod = 0.0F;
      VolaTile t = null;

      for(int x = -1; x <= 1; ++x) {
         for(int y = -1; y <= 1; ++y) {
            t = Zones.getTileOrNull(tilex + x, tiley + y, surfaced);
            if (t != null) {
               if (t.getWalls().length > 0) {
                  mod += 5.0F;
               }

               if (t.getFences() != null) {
                  mod += 5.0F;
               }
            }

            if (surfaced && tilex > 0 && tilex < Zones.worldTileSizeX - 1 && tiley > 0 && tiley < Zones.worldTileSizeY - 1) {
               int tt = Server.surfaceMesh.getTile(tilex + x, tiley + y);
               byte type = Tiles.decodeType(tt);
               byte data = Tiles.decodeData(tt);
               Tiles.Tile theTile = Tiles.getTile(type);
               if (theTile.isTree() && FoliageAge.getAgeAsByte(data) > FoliageAge.MATURE_TWO.getAgeId()) {
                  mod += 5.0F;
               }
            }
         }
      }

      if (!surfaced) {
         mod = (float)((double)mod + 10.0 + performer.getBodyControl());
      } else if (WurmCalendar.isNight()) {
         mod = (float)((double)mod + 20.0 + performer.getBodyControl());
      } else if (WurmCalendar.isMorning()) {
         mod = (float)((double)mod + 5.0 + performer.getBodyControl() / 4.0);
      }

      if (performer.getBestLightsource() != null) {
         mod -= 10.0F;
      }

      if (performer.isRoyalExecutioner()) {
         mod += 10.0F;
      }

      return mod * ItemBonus.getStealthBonus(performer);
   }

   public static final boolean groom(Creature performer, Creature target, Item groomTool, short action, Action act, float counter) {
      boolean toReturn = true;
      if (target.isGhost()) {
         performer.getCommunicator().sendNormalServerMessage("The " + target.getName() + " can't be groomed!", (byte)3);
         return true;
      } else if (!Methods.isActionAllowed(performer, action)) {
         return true;
      } else {
         if (target.canBeGroomed()) {
            toReturn = false;
            Skill breeding = null;

            try {
               breeding = performer.getSkills().getSkill(10085);
            } catch (NoSuchSkillException var11) {
               breeding = performer.getSkills().learn(10085, 1.0F);
            }

            int time = act.getTimeLeft();
            target.shouldStandStill = true;
            if (counter == 1.0F) {
               time = Actions.getSlowActionTime(performer, breeding, groomTool, 0.0);
               act.setTimeLeft(time);
               Server.getInstance().broadCastAction(performer.getName() + " starts to tend to a " + target.getName() + ".", performer, 5);
               performer.getCommunicator().sendNormalServerMessage("You start to tend to " + target.getName() + ".");
               performer.sendActionControl(Actions.actionEntrys[398].getVerbString(), true, act.getTimeLeft());
               performer.getStatus().modifyStamina(-500.0F);
               return toReturn;
            }

            if (act.currentSecond() % 5 == 2) {
               SoundPlayer.playSound("sound.work.horse.groom", performer, 1.0F);
            }

            if (act.currentSecond() % 15 == 0) {
               int r = Server.rand.nextInt(10);
               String toSend = "You talk comfortingly to " + target.getName() + ".";
               if (r == 0) {
                  toSend = "You pat the " + target.getName() + " reassuringly.";
               }

               if (r == 1) {
                  toSend = "The " + target.getName() + " flinches.";
               }

               if (r == 2) {
                  toSend = "You remove some dirt from the " + target.getName() + ".";
               }

               if (r == 3) {
                  toSend = "You find a flea on " + target.getName() + " that you dispose of.";
               }

               if (r == 4) {
                  toSend = "You stroke " + target.getName() + " over the back.";
               }

               if (r == 6) {
                  toSend = "You check the feet of the " + target.getName() + " and make sure all is in order.";
               }

               if (r == 7) {
                  toSend = "The " + target.getName() + " gives you a nervous look. What did you do now?";
               }

               if (r == 8) {
                  toSend = "The " + target.getName() + " seems to really enjoy the treatment.";
               }

               if (r == 9) {
                  toSend = "You pick a tick from " + target.getName() + " and squish it. Slick!";
               }

               performer.getCommunicator().sendNormalServerMessage(toSend);
               performer.getStatus().modifyStamina(-1500.0F);
            } else if (performer.getPower() >= 5 || counter * 10.0F > (float)act.getTimeLeft()) {
               toReturn = true;
               if (breeding.skillCheck((double)target.getBaseCombatRating(), groomTool, 0.0, false, performer.getPower() >= 5 ? 20.0F : counter / 2.0F) > 0.0) {
                  target.setLastGroomed(System.currentTimeMillis());
                  Server.getInstance().broadCastAction(performer.getName() + " finishes tending to the " + target.getName() + ".", performer, 5);
                  performer.getCommunicator()
                     .sendNormalServerMessage("You have now tended to " + target.getName() + " and " + target.getHeSheItString() + " seems pleased.");
                  groomTool.setDamage(groomTool.getDamage() + 0.02F * groomTool.getDamageModifier());
               } else {
                  performer.getCommunicator().sendNormalServerMessage("The " + target.getName() + " shys away and interrupts the action.", (byte)3);
               }
            }
         } else {
            performer.getCommunicator().sendNormalServerMessage("The " + target.getName() + " is already well tended.", (byte)3);
         }

         return toReturn;
      }
   }
}

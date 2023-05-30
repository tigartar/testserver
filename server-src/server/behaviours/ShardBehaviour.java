package com.wurmonline.server.behaviours;

import com.wurmonline.mesh.Tiles;
import com.wurmonline.server.Constants;
import com.wurmonline.server.Server;
import com.wurmonline.server.creatures.Creature;
import com.wurmonline.server.items.Item;
import com.wurmonline.server.items.ItemTemplate;
import com.wurmonline.server.items.ItemTemplateFactory;
import com.wurmonline.server.items.NoSuchTemplateException;
import com.wurmonline.server.skills.Skill;
import com.wurmonline.server.skills.Skills;
import com.wurmonline.server.sounds.SoundPlayer;
import com.wurmonline.server.zones.Zones;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.annotation.Nullable;

public final class ShardBehaviour extends ItemBehaviour {
   private static final Logger logger = Logger.getLogger(MethodsItems.class.getName());
   private static final int TYPE_MULT = 17;
   private static final int TYPE_OFFSET = 17;
   private static final int QL_MULT = 18;
   private static final int QL_OFFSET = -3;

   ShardBehaviour() {
      super((short)46);
   }

   List<ActionEntry> getShardBehaviours(Creature performer, @Nullable Item source, Item target) {
      List<ActionEntry> toReturn = new LinkedList<>();
      if (target != null && (target.isShard() || target.isOre())) {
         toReturn.add(Actions.actionEntrys[536]);
      }

      return toReturn;
   }

   @Override
   public List<ActionEntry> getBehavioursFor(Creature performer, Item target) {
      List<ActionEntry> toReturn = super.getBehavioursFor(performer, target);
      toReturn.addAll(this.getShardBehaviours(performer, null, target));
      return toReturn;
   }

   @Override
   public List<ActionEntry> getBehavioursFor(Creature performer, Item source, Item target) {
      List<ActionEntry> toReturn = super.getBehavioursFor(performer, source, target);
      toReturn.addAll(this.getShardBehaviours(performer, source, target));
      return toReturn;
   }

   @Override
   public boolean action(Action act, Creature performer, Item target, short action, float counter) {
      return this.performShardAction(act, performer, null, target, action, counter);
   }

   @Override
   public boolean action(Action act, Creature performer, Item source, Item target, short action, float counter) {
      return this.performShardAction(act, performer, source, target, action, counter);
   }

   boolean performShardAction(Action act, Creature performer, @Nullable Item source, Item target, short action, float counter) {
      if (action != 536) {
         return source == null ? super.action(act, performer, target, action, counter) : super.action(act, performer, source, target, action, counter);
      } else {
         boolean done = false;
         if (target != null && (target.isShard() || target.isOre())) {
            if (target.getOwnerId() != performer.getWurmId()) {
               performer.getCommunicator().sendSafeServerMessage("You need to carry the " + target.getName() + " in order to analyse it.");
               return true;
            }

            int tilex = target.getDataX();
            int tiley = target.getDataY();
            String targetType = target.isOre() ? "ore" : "shard";
            Skills skills = performer.getSkills();
            Skill prospecting = null;

            try {
               prospecting = skills.getSkill(10032);
            } catch (Exception var27) {
               prospecting = skills.learn(10032, 1.0F);
            }

            if (prospecting.getKnowledge(0.0) <= 20.0) {
               performer.getCommunicator().sendNormalServerMessage("You are unable to work out how to analyse the " + targetType + ".");
               return true;
            }

            if (counter == 1.0F) {
               if (tilex > 0) {
                  target.setDataXY(-tilex, tiley);
               }
            } else {
               tilex = -tilex;
            }

            if (tilex == -1) {
               performer.getCommunicator()
                  .sendNormalServerMessage("The " + target.getName() + " looks too old for a decent analysis and therefore you decide not to analyse it.");
               return true;
            }

            if (tilex <= 0 || tiley <= 0) {
               performer.getCommunicator()
                  .sendNormalServerMessage("It looks like someone has tampered with the " + target.getName() + " and therefore you decide not to analyse it.");
               return true;
            }

            if (tilex < 1 || tilex > 1 << Constants.meshSize || tiley < 1 || tiley > 1 << Constants.meshSize) {
               performer.getCommunicator()
                  .sendNormalServerMessage("You are unable to determine the origin of the " + target.getName() + ", analysis would be futile.");
               return true;
            }

            if (counter == 1.0F) {
               String sstring = "sound.work.prospecting1";
               int x = Server.rand.nextInt(3);
               if (x == 0) {
                  sstring = "sound.work.prospecting2";
               } else if (x == 1) {
                  sstring = "sound.work.prospecting3";
               }

               SoundPlayer.playSound(sstring, performer.getTileX(), performer.getTileY(), performer.isOnSurface(), 1.0F);
               int maxRadius = calcMaxRadius(prospecting.getKnowledge(0.0));
               float time = calcTickTime(performer, prospecting);
               act.setNextTick(time);
               act.setTickCount(1);
               float totalTime = time * (float)maxRadius;

               try {
                  performer.getCurrentAction().setTimeLeft((int)totalTime);
               } catch (NoSuchActionException var26) {
                  logger.log(Level.INFO, "This action does not exist?", (Throwable)var26);
               }

               performer.getCommunicator().sendNormalServerMessage("You start to analyse the " + targetType + ".");
               Server.getInstance().broadCastAction(performer.getName() + " starts analysing the " + targetType + ".", performer, 5);
               performer.sendActionControl(Actions.actionEntrys[536].getVerbString(), true, (int)totalTime);
               performer.getCommunicator().sendNormalServerMessage(target.examine(performer));
            }

            if (counter * 10.0F >= act.getNextTick()) {
               int radius = act.getTickCount();
               int currentSkill = (int)prospecting.getKnowledge(0.0);
               int maxRadius = calcMaxRadius((double)currentSkill);
               int skillTypeOffset = currentSkill - (radius * 17 - 17);
               int skillQLOffset = currentSkill - (radius * 18 - -3);
               act.incTickCount();
               act.incNextTick(calcTickTime(performer, prospecting));
               performer.getStatus().modifyStamina((float)(-1500 * radius));
               prospecting.skillCheck((double)target.getCurrentQualityLevel(), null, 0.0, false, counter / (float)radius);
               if (radius >= maxRadius) {
                  done = true;
               }

               LinkedList<String> list = new LinkedList<>();

               for(int x = -radius; x <= radius; ++x) {
                  for(int y = -radius; y <= radius; ++y) {
                     if (x == -radius || x == radius || y == -radius || y == radius) {
                        String dir = "";
                        if (performer.getBestCompass() != null) {
                           if (y < 0) {
                              dir = "north";
                           } else if (y > 0) {
                              dir = "south";
                           }

                           String we = "";
                           if (x < 0) {
                              we = "west";
                           } else if (x > 0) {
                              we = "east";
                           }

                           if (dir.length() > 0) {
                              if (we.length() > 0) {
                                 if (Math.abs(x) == Math.abs(y)) {
                                    dir = dir + we;
                                 } else if (Math.abs(x) < Math.abs(y)) {
                                    dir = we + " of " + dir;
                                 } else {
                                    dir = dir + " of " + we;
                                 }
                              }
                           } else {
                              dir = we;
                           }

                           dir = " (" + dir + ")";
                        }

                        int resource = Server.getCaveResource(tilex + x, tiley + y);
                        if (resource == 65535) {
                           resource = Server.rand.nextInt(10000);
                           Server.setCaveResource(tilex + x, tiley + y, resource);
                        }

                        String foundString = checkTile(performer, tilex + x, tiley + y, radius, skillTypeOffset, skillQLOffset);
                        add2List(list, foundString, dir);
                        if (prospecting.getKnowledge(0.0) > 40.0) {
                           byte type = Tiles.decodeType(Server.caveMesh.getTile(tilex + x, tiley + y));
                           if (type != Tiles.Tile.TILE_CAVE_WALL_ROCKSALT.id) {
                              TileRockBehaviour.rockRandom.setSeed((long)(tilex + x + (tiley + y) * Zones.worldTileSizeY) * 102533L);
                              if (TileRockBehaviour.rockRandom.nextInt(100) == 0) {
                                 String foundSalt = checkSaltFlint(performer, tilex + x, tiley + y, radius, skillTypeOffset, true);
                                 add2List(list, foundSalt, dir);
                              }
                           }
                        }

                        TileRockBehaviour.rockRandom.setSeed((long)(tilex + x + (tiley + y) * Zones.worldTileSizeY) * 6883L);
                        if (TileRockBehaviour.rockRandom.nextInt(200) == 0) {
                           String foundFlint = checkSaltFlint(performer, tilex + x, tiley + y, radius, skillTypeOffset, false);
                           add2List(list, foundFlint, dir);
                        }
                     }
                  }
               }

               outputList(performer, list, radius);
               if (!done) {
                  furtherStudy(performer, radius + 1);
               }
            }

            if (done) {
               performer.getCommunicator().sendNormalServerMessage("You finish analysing the " + targetType + ".");
            }
         }

         return done;
      }
   }

   private static void add2List(LinkedList<String> list, String foundString, String dir) {
      if (foundString.length() > 0 && !list.contains(foundString + dir)) {
         if (Server.rand.nextBoolean()) {
            list.addFirst(foundString + dir);
         } else {
            list.addLast(foundString + dir);
         }
      }
   }

   private static String checkTile(Creature performer, int tilex, int tiley, int radius, int skillTypeOffset, int skillQLOffset) {
      String findString = "";
      int itemTemplate = TileRockBehaviour.getItemTemplateForTile(Tiles.decodeType(Server.caveMesh.getTile(tilex, tiley)));
      if (itemTemplate != 146) {
         try {
            int itemSkillOffset = itemToSkillOffset(itemTemplate);
            ItemTemplate t = ItemTemplateFactory.getInstance().getTemplate(itemTemplate);
            findString = "something, but cannot quite make it out";
            if (skillTypeOffset > itemSkillOffset) {
               findString = t.getProspectName();
               TileBehaviour.r.setSeed((long)(tilex + tiley * Zones.worldTileSizeY) * 789221L);
               int m = 100;
               int max = Math.min(100, 20 + TileBehaviour.r.nextInt(80));
               findString = TileBehaviour.getShardQlDescription(max) + " " + findString;
            }

            if (radius == 6) {
               return "an " + radToString(radius) + "trace of " + findString;
            }

            return "a " + radToString(radius) + "trace of " + findString;
         } catch (NoSuchTemplateException var12) {
            logger.log(Level.WARNING, performer.getName() + " - " + var12.getMessage() + ": " + itemTemplate + " at " + tilex + ", " + tiley, (Throwable)var12);
         }
      }

      return findString;
   }

   private static String checkSaltFlint(Creature performer, int tilex, int tiley, int radius, int skillTypeOffset, boolean salt) {
      String findString = "";
      int itemTemplate = salt ? 349 : 446;

      try {
         int itemSkillOffset = salt ? 5 : 2;
         ItemTemplate t = ItemTemplateFactory.getInstance().getTemplate(itemTemplate);
         findString = "something, but cannot quite make it out";
         if (skillTypeOffset > itemSkillOffset) {
            findString = t.getName();
         }

         return radius == 6 ? "an " + radToString(radius) + "trace of " + findString : "a " + radToString(radius) + "trace of " + findString;
      } catch (NoSuchTemplateException var10) {
         logger.log(Level.WARNING, performer.getName() + " - " + var10.getMessage() + ": " + itemTemplate + " at " + tilex + ", " + tiley, (Throwable)var10);
         return findString;
      }
   }

   private static String radToString(int radius) {
      switch(radius) {
         case 2:
            return "slight ";
         case 3:
            return "faint ";
         case 4:
            return "minuscule ";
         case 5:
            return "vague ";
         case 6:
            return "indistinct ";
         default:
            return "";
      }
   }

   private static void furtherStudy(Creature performer, int radius) {
      switch(radius) {
         case 2:
            performer.getCommunicator().sendNormalServerMessage("You take a closer look.");
            break;
         case 3:
            performer.getCommunicator().sendNormalServerMessage("You study it a bit more.");
            break;
         case 4:
            performer.getCommunicator().sendNormalServerMessage("You study it real hard.");
            break;
         case 5:
            performer.getCommunicator().sendNormalServerMessage("You peer at it.");
            break;
         default:
            performer.getCommunicator().sendNormalServerMessage("You go cross-eyed studying it.");
      }
   }

   public static int calcMaxRadius(double currentSkill) {
      return (int)(currentSkill + 17.0) / 17;
   }

   private static float calcTickTime(Creature performer, Skill prospecting) {
      return (float)(Actions.getQuickActionTime(performer, prospecting, null, 0.0) / 3 * 2);
   }

   private static void outputList(Creature performer, LinkedList<String> list, int radius) {
      if (list.isEmpty()) {
         int x = Server.rand.nextInt(3);
         if (x == 0) {
            performer.getCommunicator().sendNormalServerMessage("You do not notice any unusual " + radToString(radius) + "traces.");
         } else if (x == 1) {
            performer.getCommunicator().sendNormalServerMessage("You cannot see anything unusual.");
         } else {
            performer.getCommunicator().sendNormalServerMessage("You cannot see any unusual " + radToString(radius) + "traces of anything.");
         }
      } else {
         Iterator<String> it = list.iterator();

         while(it.hasNext()) {
            int x = Server.rand.nextInt(3);
            if (x == 0) {
               performer.getCommunicator().sendNormalServerMessage("You spot " + (String)it.next() + ".");
            } else if (x == 1) {
               performer.getCommunicator().sendNormalServerMessage("You notice " + (String)it.next() + ".");
            } else {
               performer.getCommunicator().sendNormalServerMessage("You see " + (String)it.next() + ".");
            }
         }
      }
   }

   private static int itemToSkillOffset(int itemTemplate) {
      switch(itemTemplate) {
         case 38:
            return 0;
         case 39:
            return 9;
         case 40:
            return 7;
         case 41:
            return 3;
         case 42:
            return 2;
         case 43:
            return 4;
         case 207:
            return 1;
         case 693:
            return 11;
         case 697:
            return 10;
         case 770:
            return 6;
         case 785:
            return 8;
         case 1116:
            return 12;
         default:
            return 0;
      }
   }
}

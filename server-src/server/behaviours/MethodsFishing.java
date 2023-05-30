package com.wurmonline.server.behaviours;

import com.wurmonline.server.FailedException;
import com.wurmonline.server.GeneralUtilities;
import com.wurmonline.server.Items;
import com.wurmonline.server.MiscConstants;
import com.wurmonline.server.Point;
import com.wurmonline.server.Point4f;
import com.wurmonline.server.Server;
import com.wurmonline.server.Servers;
import com.wurmonline.server.creatures.Creature;
import com.wurmonline.server.creatures.ai.scripts.FishAI;
import com.wurmonline.server.items.ContainerRestriction;
import com.wurmonline.server.items.Item;
import com.wurmonline.server.items.ItemFactory;
import com.wurmonline.server.items.ItemTemplate;
import com.wurmonline.server.items.NoSuchTemplateException;
import com.wurmonline.server.skills.Skill;
import com.wurmonline.server.zones.NoSuchZoneException;
import com.wurmonline.server.zones.VolaTile;
import com.wurmonline.server.zones.WaterType;
import com.wurmonline.server.zones.Zones;
import com.wurmonline.shared.constants.FishingEnums;
import java.awt.Color;
import java.util.ArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.annotation.Nullable;

public class MethodsFishing implements MiscConstants {
   private static final Logger logger = Logger.getLogger(MethodsFishing.class.getName());

   private MethodsFishing() {
   }

   static boolean fish(Creature performer, Item source, int tilex, int tiley, int tile, float counter, Action act) {
      if (!Terraforming.isTileUnderWater(tile, tilex, tiley, performer.isOnSurface())) {
         performer.getCommunicator().sendNormalServerMessage("The water is too shallow to fish.");
         return true;
      } else {
         Item[] fishingItems = source.getFishingItems();
         Item fishingReel = fishingItems[0];
         Item fishingLine = fishingItems[1];
         Item fishingFloat = fishingItems[2];
         Item fishingHook = fishingItems[3];
         Item fishingBait = fishingItems[4];
         Skill fishing = performer.getSkills().getSkillOrLearn(10033);
         int timeleft = 1800;
         int defaultTimer = 1800;
         byte startCommand = 0;
         if (source.getTemplateId() == 1343) {
            startCommand = 40;
            if (performer.getVehicle() != -10L) {
               performer.getCommunicator().sendNormalServerMessage("You cannot use a fishing net whilst on a vehicle.");
               return true;
            }
         } else if (source.getTemplateId() != 705 && source.getTemplateId() != 707) {
            startCommand = 0;
            boolean failed = false;
            if (source.getTemplateId() == 1344) {
               if (act.getCounterAsFloat() < act.getFailSecond()) {
                  if (fishingLine == null || fishingHook == null) {
                     performer.getCommunicator().sendNormalServerMessage("Fishing pole needs a line with a fishing hook to be able to catch fish.");
                     failed = true;
                  }

                  if (fishingFloat == null) {
                     performer.getCommunicator().sendNormalServerMessage("Fishing pole needs a float for you to be able to see when a fish can be hooked.");
                     failed = true;
                  }
               }

               if (failed) {
                  act.setFailSecond(act.getCounterAsFloat());
               }
            } else {
               if (act.getCounterAsFloat() < act.getFailSecond()) {
                  if (fishingReel == null || fishingLine == null || fishingHook == null) {
                     performer.getCommunicator().sendNormalServerMessage("Fishing rod needs a reel, line and a fishing hook to be able to catch fish.");
                     failed = true;
                  }

                  if (fishingFloat == null) {
                     performer.getCommunicator().sendNormalServerMessage("Fishing rod needs a float for you to be able to see when a fish can be hooked.");
                     failed = true;
                  }
               }

               if (failed) {
                  act.setFailSecond(act.getCounterAsFloat());
               }
            }

            if (failed) {
               if (counter == 1.0F || act.getCreature() == null) {
                  sendFishStop(performer, act);
                  return true;
               }

               byte currentPhase = (byte)act.getTickCount();
               if (currentPhase != 15) {
                  processFishMovedOn(performer, act, 2.2F);
                  act.setTickCount(15);
               }
            }
         } else {
            startCommand = 20;
            if (performer.getVehicle() != -10L) {
               performer.getCommunicator().sendNormalServerMessage("You cannot use a spear to fish whilst on a vehicle.");
               if (counter != 1.0F) {
                  sendSpearStop(performer, act);
               }

               return true;
            }
         }

         if (counter != 1.0F) {
            byte currentPhase = (byte)act.getTickCount();
            if (canTimeOut(act) && act.getSecond() * 10 > act.getTimeLeft()) {
               switch(startCommand) {
                  case 0:
                     switch(currentPhase) {
                        case 0:
                           performer.getCommunicator().sendNormalServerMessage("You never cast your line, so caught nothing.");
                           return sendFishStop(performer, act);
                        default:
                           performer.getCommunicator().sendNormalServerMessage("You did not catch anything!");
                           sendFishStop(performer, act);
                           return true;
                     }
                  case 20:
                     performer.getCommunicator().sendNormalServerMessage("You speared nothing!");
                     sendSpearStop(performer, act);
                     break;
                  case 40:
                     performer.getCommunicator().sendNormalServerMessage("You finish pulling in the net.");
               }

               return true;
            } else {
               if (currentPhase == 17) {
                  Creature fish = act.getCreature();
                  if (performer.isWithinDistanceTo(fish, 1.0F)) {
                     act.setTickCount(12);
                  } else if (!performer.isWithinDistanceTo(fish, 10.0F)) {
                     act.setTickCount(13);
                  }
               }

               timeleft = (int)act.getData();
               if (act.getSecond() * 10 > timeleft || isInstant(currentPhase)) {
                  switch(startCommand) {
                     case 0:
                        if (processFishPhase(performer, currentPhase, source, act, fishing)) {
                           return true;
                        }
                        break;
                     case 20:
                        if (processSpearPhase(performer, currentPhase, source, act, fishing)) {
                           return true;
                        }
                        break;
                     case 40:
                        if (processNetPhase(performer, currentPhase, source, act, tilex, tiley, fishing)) {
                           return true;
                        }
                  }
               }

               if (act.justTickedSecond()) {
                  if (act.getSecond() % 2 == 0 && fishingTestsFailed(performer, act.getPosX(), act.getPosY())) {
                     switch(startCommand) {
                        case 0:
                           sendFishStop(performer, act);
                           return true;
                        case 20:
                           sendSpearStop(performer, act);
                           return true;
                        case 40:
                           return true;
                        default:
                           return true;
                     }
                  }

                  Creature fish = act.getCreature();
                  if (fish != null && startCommand == 20) {
                     FishAI.FishAIData faid = (FishAI.FishAIData)fish.getCreatureAIData();
                     if (fish.getPosX() == faid.getTargetPosX() && fish.getPosY() == faid.getTargetPosY()) {
                        testMessage(performer, "Fish out of range? Swam away!", "");
                        act.setTickCount(28);
                     }
                  }
               }

               return false;
            }
         } else if (fishingTestsFailed(performer, act.getPosX(), act.getPosY())) {
            return true;
         } else {
            switch(startCommand) {
               case 0:
                  String ss = performer.getVehicle() == -10L ? "stand" : "sit";
                  performer.getCommunicator().sendNormalServerMessage("You " + ss + " still and get ready to cast.");
                  Server.getInstance().broadCastAction(performer.getName() + " gets ready to cast.", performer, 5);
                  float[] radi = getMinMaxRadius(source, fishingLine);
                  float minRadius = radi[0];
                  float maxRadius = radi[1];
                  byte rodType = getRodType(source, fishingReel, fishingLine);
                  byte rodMaterial = source.getMaterial();
                  FishEnums.ReelType reelType = FishEnums.ReelType.fromItem(fishingReel);
                  byte reelMaterial = fishingReel == null ? 0 : fishingReel.getMaterial();
                  FishEnums.FloatType floatType = FishEnums.FloatType.fromItem(fishingFloat);
                  FishEnums.HookType hookType = FishEnums.HookType.fromItem(fishingHook);
                  byte hookMaterial = fishingHook.getMaterial();
                  FishEnums.BaitType baitType = FishEnums.BaitType.fromItem(fishingBait);
                  performer.getCommunicator()
                     .sendFishStart(
                        minRadius, maxRadius, rodType, rodMaterial, reelType.getTypeId(), reelMaterial, floatType.getTypeId(), baitType.getTypeId()
                     );
                  remember(source, reelType, reelMaterial, floatType, hookType, hookMaterial, baitType);
                  timeleft = 300;
                  defaultTimer = timeleft;
                  act.setTickCount(0);
                  break;
               case 20:
                  performer.getCommunicator().sendNormalServerMessage("You stand still to wait for a passing fish, are they like buses?");
                  Server.getInstance().broadCastAction(performer.getName() + " stands still looking at the water.", performer, 5);
                  performer.getCommunicator().sendSpearStart();
                  timeleft = 100 + Server.rand.nextInt(250) - source.getRarity() * 10;
                  act.setTickCount(21);
                  break;
               case 40:
                  if (!source.isEmpty(false)) {
                     performer.getCommunicator().sendNormalServerMessage("The net is too unwieldly for you to cast, please empty it first!");
                     return true;
                  }

                  performer.getCommunicator()
                     .sendNormalServerMessage("You throw out the net and start slowly pulling it in, hoping to catch some small fish.");
                  Server.getInstance().broadCastAction(performer.getName() + " throws out a net and starts fishing.", performer, 5);
                  timeleft = 50 + Server.rand.nextInt(250);
                  defaultTimer = 600;
                  act.setTickCount(40);
            }

            act.setTimeLeft(defaultTimer);
            act.setData((long)timeleft);
            performer.sendActionControl(Actions.actionEntrys[160].getVerbString(), true, defaultTimer);
            return false;
         }
      }
   }

   private static boolean isInstant(byte currentPhase) {
      switch(currentPhase) {
         case 2:
         case 3:
         case 5:
         case 6:
         case 7:
         case 9:
         case 10:
         case 13:
         case 22:
         case 23:
         case 24:
         case 27:
         case 28:
            return true;
         case 4:
         case 8:
         case 11:
         case 12:
         case 14:
         case 15:
         case 16:
         case 17:
         case 18:
         case 19:
         case 20:
         case 21:
         case 25:
         case 26:
         default:
            return false;
      }
   }

   private static boolean canTimeOut(Action act) {
      Creature fish = act.getCreature();
      return fish == null;
   }

   private static void remember(
      Item rod,
      FishEnums.ReelType reelType,
      byte reelMaterial,
      FishEnums.FloatType floatType,
      FishEnums.HookType hookType,
      byte hookMaterial,
      FishEnums.BaitType baitType
   ) {
      int types = (reelType.getTypeId() << 12) + (floatType.getTypeId() << 8) + (hookType.getTypeId() << 4) + baitType.getTypeId();
      int materials = (reelMaterial << 8) + hookMaterial;
      rod.setData(types, materials);
   }

   public static void playerOutOfRange(Creature performer, Action act) {
      Item source = act.getSubject();
      int stid = source == null ? 0 : source.getTemplateId();
      switch(stid) {
         case 705:
         case 707:
            sendSpearStop(performer, act);
            break;
         case 1344:
         case 1346:
            sendFishStop(performer, act);
      }
   }

   private static boolean processNetPhase(Creature performer, byte currentPhase, Item net, Action act, int tilex, int tiley, Skill fishing) {
      float posx = (float)((tilex << 2) + 2);
      float posy = (float)((tiley << 2) + 2);
      MethodsFishing.FishRow fr = caughtFish(performer, posx, posy, net);
      if (fr != null) {
         int weight = makeDeadFish(performer, act, fishing, fr.getFishTypeId(), net, net);
         FishEnums.FishData fd = FishEnums.FishData.fromInt(fr.getFishTypeId());
         float damMod = (float)fd.getDamageMod() * Math.max(0.1F, (float)weight / 3000.0F);
         float additionalDamage = additionalDamage(net, damMod * 10.0F, false);
         if (additionalDamage > 0.0F) {
            float newDam = net.getDamage() + additionalDamage;
            if (newDam >= 100.0F && !net.isEmpty(false)) {
               performer.getCommunicator().sendNormalServerMessage("As the " + net.getName() + " disintegrates, the fish inside all swim away");
               destroyContents(net);
            }

            net.setDamage(newDam);
         }
      }

      int moreTime = 50 + Server.rand.nextInt(250);
      int timeleft = act.getSecond() * 10 + moreTime;
      act.setData((long)timeleft);
      return false;
   }

   private static boolean processSpearPhase(Creature performer, byte currentPhase, Item spear, Action act, Skill fishing) {
      switch(currentPhase) {
         case 21:
            return processSpearMove(performer, act, spear, fishing);
         case 22:
            return processSpearHit(performer, act, fishing, spear);
         case 23:
            return processSpearMissed(performer, act, spear);
         case 24:
            testMessage(performer, "You launch the spear at nothing!", "");
            act.setTickCount(21);
            return false;
         case 25:
         default:
            return false;
         case 26:
            Creature fish = act.getCreature();
            testMessage(performer, "You launch the spear at " + fish.getName() + ".", " @fx:" + (int)fish.getPosX() + " fy:" + (int)fish.getPosY());
            performer.getCommunicator().sendSpearStrike(fish.getPosX(), fish.getPosY());
            return processSpearStrike(performer, act, fishing, spear, fish.getPosX(), fish.getPosY());
         case 27:
            return processSpearCancel(performer, act);
         case 28:
            return processSpearSwamAway(performer, act, spear, 50);
      }
   }

   private static boolean processFishPhase(Creature performer, byte currentPhase, Item rod, Action act, Skill fishing) {
      switch(currentPhase) {
         case 1:
            return processFishBite(performer, act, rod, fishing);
         case 2:
            return processFishMovedOn(performer, act);
         case 3:
            return processFishHooked(performer, act, fishing, rod);
         case 4:
            return sendFishStop(performer, act);
         case 5:
            performer.getCommunicator().sendNormalServerMessage("You hooked nothing while reeling in your line.");
            return sendFishStop(performer, act);
         case 6:
         case 7:
            return processFishLineSnapped(performer, act, rod);
         case 8:
         case 9:
         default:
            return false;
         case 10:
            return processFishCancel(performer, act);
         case 11:
            return processFishStrike(performer, act, fishing, rod);
         case 12:
            return processFishCaught(performer, act, fishing, rod);
         case 13:
            Creature fish = act.getCreature();
            String fname = fish == null ? "fish" : fish.getName();
            performer.getCommunicator().sendNormalServerMessage("The " + fname + " managed to jump the fishing hook!");
            return sendFishStop(performer, act);
         case 14:
            return processFishSwamAway(performer, act, rod, 50);
         case 15:
            return sendFishStop(performer, act);
         case 16:
            if (processFishMove(performer, act, fishing, rod)) {
               sendFishStop(performer, act);
               return true;
            }

            return false;
         case 17:
            if (processFishPull(performer, act, fishing, rod, false)) {
               Creature fish = act.getCreature();
               String fname = fish == null ? "fish" : fish.getName();
               performer.getCommunicator().sendNormalServerMessage("The " + fname + " swims off.");
               sendFishStop(performer, act);
               return true;
            }

            return false;
         case 18:
            return processFishPause(performer, act);
         case 19:
            return processFishMovingOn(performer, act);
      }
   }

   private static boolean fishingTestsFailed(Creature performer, float posx, float posy) {
      int tilex = (int)posx >> 2;
      int tiley = (int)posy >> 2;
      if (!GeneralUtilities.isValidTileLocation(tilex, tiley)) {
         performer.getCommunicator().sendNormalServerMessage("A huge shadow moves beneath the waves, and you reel in the line in panic.");
         return true;
      } else if (performer.getInventory().getNumItemsNotCoins() >= 100) {
         performer.getCommunicator().sendNormalServerMessage("You wouldn't be able to carry the fish. Drop something first.");
         return true;
      } else if (!performer.canCarry(1000)) {
         performer.getCommunicator().sendNormalServerMessage("You are too heavily loaded. Drop something first.");
         return true;
      } else {
         int depth = FishEnums.getWaterDepth(performer.getPosX(), performer.getPosY(), performer.isOnSurface());
         if (performer.getBridgeId() == -10L && depth > 10 && performer.getVehicle() == -10L) {
            performer.getCommunicator().sendNormalServerMessage("You can't swim and fish at the same time.");
            return true;
         } else {
            return false;
         }
      }
   }

   static boolean destroyFishCreature(Action act) {
      Creature fish = act.getCreature();
      act.setCreature(null);
      if (fish != null) {
         fish.destroy();
      }

      return true;
   }

   private static boolean isFishSpotValid(Point point) {
      if (WaterType.getWaterType(Zones.safeTileX(point.getX()), Zones.safeTileY(point.getY()), true) == 4) {
         float ht = 0.0F;

         try {
            ht = Zones.calculateHeight((float)point.getX(), (float)point.getY(), true) * 10.0F;
         } catch (NoSuchZoneException var3) {
         }

         if (ht < -100.0F) {
            return true;
         }
      }

      return false;
   }

   public static Point[] getSpecialSpots(int tilex, int tiley, int season) {
      ArrayList<Point> specialSpots = new ArrayList<>();
      int zoneX = tilex / 128;
      int zoneY = tiley / 128;

      for(FishEnums.FishData fd : FishEnums.FishData.values()) {
         if (fd.isSpecialFish()) {
            Point tp = fd.getSpecialSpot(zoneX, zoneY, season);
            if (tilex == 0 && tiley == 0 || isFishSpotValid(tp)) {
               specialSpots.add(tp);
            }
         }
      }

      return specialSpots.toArray(new Point[specialSpots.size()]);
   }

   public static MethodsFishing.FishRow[] getFishTable(Creature performer, float posX, float posY, Item rod) {
      Skill fishing = performer.getSkills().getSkillOrLearn(10033);
      float knowledge = (float)fishing.getKnowledge();
      Item[] fishingItems = rod.getFishingItems();
      Item fishingReel = fishingItems[0];
      Item fishingLine = fishingItems[1];
      Item fishingFloat = fishingItems[2];
      Item fishingHook = fishingItems[3];
      Item fishingBait = fishingItems[4];
      float totalChances = 0.0F;
      MethodsFishing.FishRow[] fishTable = new MethodsFishing.FishRow[FishEnums.FishData.getLength()];

      for(FishEnums.FishData fd : FishEnums.FishData.values()) {
         fishTable[fd.getTypeId()] = new MethodsFishing.FishRow(fd.getTypeId(), fd.getName());
         if (fd.getTypeId() != FishEnums.FishData.NONE.getTypeId() && fd.getTypeId() != FishEnums.FishData.CLAM.getTypeId()) {
            float fishChance = fd.getChance(
               knowledge, rod, fishingReel, fishingLine, fishingFloat, fishingHook, fishingBait, posX, posY, performer.isOnSurface()
            );
            fishTable[fd.getTypeId()].setChance(fishChance);
            totalChances += fishChance;
         }
      }

      if (totalChances > 99.0F) {
         float percentageFactor = totalChances / 99.0F;
         totalChances = 0.0F;

         for(FishEnums.FishData fd : FishEnums.FishData.values()) {
            float chance = fishTable[fd.getTypeId()].getChance();
            if (chance > 0.0F) {
               fishTable[fd.getTypeId()].setChance(chance / percentageFactor);
            }

            totalChances += fishTable[fd.getTypeId()].getChance();
         }
      }

      if (totalChances < 100.0F) {
         fishTable[FishEnums.FishData.CLAM.getTypeId()].setChance(Math.min(2.0F, 100.0F - totalChances));
      }

      return fishTable;
   }

   public static boolean showFishTable(Creature performer, Item source, int tileX, int tileY, float counter, Action act) {
      int time = act.getTimeLeft();
      if (counter == 1.0F) {
         int var21 = 250;
         act.setTimeLeft(var21);
         performer.getCommunicator().sendNormalServerMessage("You start looking around for what fish might be in this area.");
         performer.sendActionControl(act.getActionString(), true, var21);
      } else if (act.justTickedSecond() && act.getSecond() == 5) {
         if (source.getTemplateId() == 1344) {
            Item[] fishingItems = source.getFishingItems();
            if (fishingItems[1] == null) {
               performer.getCommunicator().sendNormalServerMessage("Your pole is missing a line, float and fishing hook!");
               return true;
            }

            if (fishingItems[2] == null) {
               if (fishingItems[3] == null) {
                  performer.getCommunicator().sendNormalServerMessage("Your pole is missing a float and fishing hook!");
                  return true;
               }

               performer.getCommunicator().sendNormalServerMessage("Your pole is missing a float!");
               return true;
            }

            if (fishingItems[3] == null) {
               performer.getCommunicator().sendNormalServerMessage("Your pole is missing a fishing hook!");
               return true;
            }

            if (fishingItems[4] == null) {
               performer.getCommunicator().sendNormalServerMessage("Your pole looks all set, but you think catching something could be easier using bait.");
            } else {
               performer.getCommunicator().sendNormalServerMessage("Your pole looks all set.");
            }
         } else if (source.getTemplateId() == 1346) {
            Item[] fishingItems = source.getFishingItems();
            if (fishingItems[0] == null) {
               performer.getCommunicator().sendNormalServerMessage("Your rod is missing a reel, line, float and fishing hook!");
               return true;
            }

            if (fishingItems[1] == null) {
               performer.getCommunicator().sendNormalServerMessage("Your rod is missing a line, float and fishing hook!");
               return true;
            }

            if (fishingItems[2] == null) {
               if (fishingItems[3] == null) {
                  performer.getCommunicator().sendNormalServerMessage("Your rod is missing a float and fishing hook!");
                  return true;
               }

               performer.getCommunicator().sendNormalServerMessage("Your rod is missing a float!");
               return true;
            }

            if (fishingItems[3] == null) {
               performer.getCommunicator().sendNormalServerMessage("Your rod is missing a fishing hook!");
               return true;
            }

            if (fishingItems[4] == null) {
               performer.getCommunicator().sendNormalServerMessage("Your rod looks all set, but you think catching something could be easier using bait.");
            } else {
               performer.getCommunicator().sendNormalServerMessage("Your rod looks all set.");
            }
         }
      }

      double fishingSkill = performer.getSkills().getSkillOrLearn(10033).getKnowledge(0.0);
      float posx = (float)((tileX << 2) + 2);
      float posy = (float)((tileY << 2) + 2);
      String waterType = WaterType.getWaterTypeString(tileX, tileY, performer.isOnSurface()).toLowerCase();
      int waterDepth = FishEnums.getWaterDepth(posx, posy, performer.isOnSurface());
      if (act.justTickedSecond() && act.getSecond() == 10) {
         if (fishingSkill < 10.0) {
            performer.getCommunicator()
               .sendNormalServerMessage(
                  "You're not too sure what type of water you're standing in, perhaps with a bit more fishing knowledge you'll understand better."
               );
         } else if (fishingSkill < 40.0) {
            performer.getCommunicator().sendNormalServerMessage("You believe this area of water to be " + waterType + ".");
         } else {
            performer.getCommunicator()
               .sendNormalServerMessage("You believe this area of water to be " + waterType + " around a depth of " + waterDepth / 10 + "m.");
         }
      } else if (act.justTickedSecond() && act.getSecond() == 15) {
         MethodsFishing.FishRow[] fishChances = getFishTable(performer, posx, posy, source);
         MethodsFishing.FishRow topChance = null;

         for(MethodsFishing.FishRow fishChance : fishChances) {
            if (!FishEnums.FishData.fromName(fishChance.getName()).isSpecialFish()) {
               float chance = fishChance.getChance();
               if (chance > 0.0F && (topChance == null || topChance.getChance() < chance)) {
                  topChance = fishChance;
               }
            }
         }

         if (topChance == null) {
            performer.getCommunicator().sendNormalServerMessage("You can't find anything that you'll catch in this area with the " + source.getName() + ".");
            return true;
         }

         performer.getCommunicator()
            .sendNormalServerMessage(
               "The most common fish around here that you think you might catch with the " + source.getName() + " is a " + topChance.getName() + "."
            );
      } else if (act.justTickedSecond() && act.getSecond() == 25) {
         MethodsFishing.FishRow[] fishChances = getFishTable(performer, posx, posy, source);
         MethodsFishing.FishRow topChance = null;
         ArrayList<MethodsFishing.FishRow> otherChances = new ArrayList<>();

         for(MethodsFishing.FishRow fishChance : fishChances) {
            if (!FishEnums.FishData.fromName(fishChance.getName()).isSpecialFish()) {
               float chance = fishChance.getChance();
               if (chance > 0.0F) {
                  if (topChance == null || topChance.getChance() < chance) {
                     topChance = fishChance;
                  }

                  if ((double)chance > (100.0 - fishingSkill) / 5.0 + 5.0) {
                     otherChances.add(fishChance);
                  }
               }
            }
         }

         otherChances.remove(topChance);
         if (!otherChances.isEmpty() && !(fishingSkill < 20.0)) {
            String allOthers = "";

            for(int i = 0; i < otherChances.size(); ++i) {
               if (i > 0 && i == otherChances.size() - 1) {
                  allOthers = allOthers + " and ";
               } else if (i > 0) {
                  allOthers = allOthers + ", ";
               }

               allOthers = allOthers + otherChances.get(i).getName();
            }

            performer.getCommunicator()
               .sendNormalServerMessage("You also think the " + source.getName() + " will be useful to catch " + allOthers + " in this area.");
         } else {
            performer.getCommunicator().sendNormalServerMessage("You're not sure of what other fish you might find here.");
         }

         return true;
      }

      return false;
   }

   @Nullable
   private static MethodsFishing.FishRow caughtFish(Creature performer, float posX, float posY, Item rod) {
      MethodsFishing.FishRow[] fishChances = getFishTable(performer, posX, posY, rod);
      float rno = Server.rand.nextFloat() * 100.0F;
      float runningTotal = 0.0F;

      for(MethodsFishing.FishRow fishChance : fishChances) {
         if (fishChance.getChance() > 0.0F) {
            runningTotal += fishChance.getChance();
            if (runningTotal > rno) {
               return fishChance;
            }
         }
      }

      return null;
   }

   public static boolean processSpearStrike(Creature performer, Action act, Skill fishing, Item spear, float posX, float posY) {
      Creature fish = act.getCreature();
      if (fish == null) {
         act.setTickCount(24);
         return false;
      } else {
         FishAI.FishAIData faid = (FishAI.FishAIData)fish.getCreatureAIData();
         float dx = Math.abs(fish.getStatus().getPositionX() - posX);
         float dy = Math.abs(fish.getStatus().getPositionY() - posY);
         float dd = (float)Math.sqrt((double)(dx * dx + dy * dy));
         testMessage(
            performer,
            "Distance away was " + dd + " fish:" + fish.getStatus().getPositionX() + "," + fish.getStatus().getPositionY() + " strike:" + posX + "," + posY,
            ""
         );
         performer.sendSpearStrike(posX, posY);
         if (dd > 1.0F) {
            performer.getCommunicator().sendNormalServerMessage("You missed the " + faid.getNameWithSize() + "!");
            Server.getInstance().broadCastAction(performer.getName() + " attempted to spear a fish and failed!", performer, 5);
            act.setTickCount(23);
            return false;
         } else {
            float result = getDifficulty(performer, act, performer.getPosX(), performer.getPosY(), fishing, spear, null, null, null, null, null, 0.0F, 100.0F);
            if (result <= 0.0F) {
               if (result < -80.0F && performer.isWithinDistanceTo(posX, posY, 0.0F, 2.0F)) {
                  Skill bodyStr = performer.getSkills().getSkillOrLearn(102);
                  byte foot = (byte)(Server.rand.nextBoolean() ? 15 : 16);
                  performer.getCommunicator().sendNormalServerMessage("You miss the " + fish.getName() + " and hit your own foot!");
                  Server.getInstance().broadCastAction(performer.getName() + " spears their own foot.. how silly!", performer, 5);
                  performer.addWoundOfType(null, (byte)2, foot, false, 1.0F, true, 400.0 * bodyStr.getKnowledge(0.0), 0.0F, 0.0F, false, false);
               } else {
                  performer.getCommunicator().sendNormalServerMessage("You missed the " + fish.getName() + "!");
                  Server.getInstance().broadCastAction(performer.getName() + " attempted to spear a fish and failed!", performer, 5);
               }

               act.setTickCount(23);
            } else {
               act.setTickCount(22);
               performer.getCommunicator().sendNormalServerMessage("You managed to spear the " + fish.getName() + "!");
               Server.getInstance().broadCastAction(performer.getName() + " managed to spear a fish!", performer, 5);
               performer.achievement(559);
            }

            return false;
         }
      }
   }

   public static boolean processFishStrike(Creature performer, Action act, Skill fishing, Item rod) {
      Creature fish = act.getCreature();
      if (fish == null) {
         act.setTickCount(5);
         return false;
      } else if (rod == null) {
         act.setTickCount(7);
         return false;
      } else {
         FishAI.FishAIData faid = (FishAI.FishAIData)fish.getCreatureAIData();
         String extra = "";
         if (act.getTickCount() != 18) {
            if (act.getTickCount() != 4 && act.getTickCount() != 2 && act.getTickCount() != 19) {
               if (Servers.isThisATestServer() && (performer.getPower() > 1 || performer.hasFlag(51))) {
                  extra = " (Test only) Cmd:" + fromCommand((byte)act.getTickCount());
               }

               performer.getCommunicator().sendNormalServerMessage("You scare off the " + faid.getNameWithSize() + " before it starts feeding." + extra);
               processFishMovedOn(performer, act, 2.0F);
               act.setTickCount(4);
               performer.getCommunicator().sendFishSubCommand((byte)4, -1L);
            }

            return false;
         } else {
            Item[] fishingItems = rod.getFishingItems();
            Item fishingReel = fishingItems[0];
            Item fishingLine = fishingItems[1];
            Item fishingFloat = fishingItems[2];
            Item fishingHook = fishingItems[3];
            Item fishingBait = fishingItems[4];
            float result = getDifficulty(
               performer,
               act,
               performer.getPosX(),
               performer.getPosY(),
               fishing,
               rod,
               fishingReel,
               fishingLine,
               fishingFloat,
               fishingHook,
               fishingBait,
               0.0F,
               40.0F
            );
            if (Servers.isThisATestServer() && (performer.getPower() > 1 || performer.hasFlag(51))) {
               extra = " (Test only) Res:" + result;
            }

            act.setTickCount(3);
            return false;
         }
      }
   }

   private static float getDifficulty(
      Creature performer,
      Action act,
      float posx,
      float posy,
      Skill fishing,
      Item source,
      Item reel,
      Item line,
      Item fishingFloat,
      Item hook,
      Item bait,
      float bonus,
      float times
   ) {
      Creature fish = act.getCreature();
      FishAI.FishAIData faid = (FishAI.FishAIData)fish.getCreatureAIData();
      float difficulty = faid.getDifficulty();
      if (difficulty == -10.0F) {
         FishEnums.FishData fd = FishEnums.FishData.fromInt(faid.getFishTypeId());
         float knowledge = (float)fishing.getKnowledge();
         difficulty = fd.getDifficulty(knowledge, posx, posy, performer.isOnSurface(), source, reel, line, fishingFloat, hook, bait);
         faid.setDifficulty(difficulty);
         testMessage(performer, "", fd.getName() + " Dif:" + difficulty);
      }

      double result = fishing.skillCheck((double)difficulty, source, (double)bonus, false, times);
      return (float)result;
   }

   private static boolean processSpearCancel(Creature performer, Action act) {
      performer.getCommunicator().sendNormalServerMessage("You have cancelled your spearing!");
      return sendSpearStop(performer, act);
   }

   private static boolean processSpearMove(Creature performer, Action act, Item spear, Skill fishing) {
      if (act.getCreature() == null) {
         if (makeFish(performer, act, spear, fishing, (byte)20)) {
            return true;
         } else {
            act.setTickCount(21);
            return false;
         }
      } else {
         act.setTickCount(28);
         return false;
      }
   }

   private static boolean processSpearSwamAway(Creature performer, Action act, Item spear, int delay) {
      performer.getCommunicator().sendNormalServerMessage("The " + act.getCreature().getName() + " swims off.");
      destroyFishCreature(act);
      int moreTime = delay + Server.rand.nextInt(250) - spear.getRarity() * 10;
      int timeleft = act.getSecond() * 10 + moreTime;
      act.setData((long)timeleft);
      act.setTickCount(21);
      return false;
   }

   private static boolean processSpearMissed(Creature performer, Action act, Item spear) {
      Creature fish = act.getCreature();
      FishAI.FishAIData faid = (FishAI.FishAIData)fish.getCreatureAIData();
      faid.setRaceAway(true);
      int moreTime = (int)(faid.getTimeToTarget() / 2.0F);
      int timeleft = act.getSecond() * 10 + moreTime;
      act.setData((long)timeleft);
      act.setTickCount(21);
      FishEnums.FishData fd = faid.getFishData();
      float fdam = (float)fd.getDamageMod();
      float damMod = fdam * Math.max(0.1F, (float)(faid.getWeight() / 3000));
      float additionalDamage = additionalDamage(spear, damMod * 10.0F, true);
      return additionalDamage > 0.0F ? spear.setDamage(spear.getDamage() + additionalDamage) : false;
   }

   private static boolean processSpearHit(Creature performer, Action act, Skill fishing, Item spear) {
      Creature fish = act.getCreature();
      if (fish == null) {
         return true;
      } else {
         FishAI.FishAIData faid = (FishAI.FishAIData)fish.getCreatureAIData();
         byte fishTypeId = faid.getFishTypeId();
         performer.getCommunicator().sendSpearHit(fishTypeId, fish.getWurmId());
         makeDeadFish(performer, act, fishing, fishTypeId, spear, performer.getInventory());
         destroyFishCreature(act);
         FishEnums.FishData fd = faid.getFishData();
         float fdam = (float)fd.getDamageMod();
         float damMod = fdam * Math.max(0.1F, (float)(faid.getWeight() / 3000));
         float additionalDamage = additionalDamage(spear, damMod * 8.0F, true);
         return additionalDamage > 0.0F ? spear.setDamage(spear.getDamage() + additionalDamage) : false;
      }
   }

   private static boolean sendSpearStop(Creature performer, Action act) {
      destroyFishCreature(act);
      performer.getCommunicator().sendFishSubCommand((byte)29, -1L);
      return true;
   }

   private static boolean makeFish(Creature performer, Action act, Item source, Skill fishing, byte startCmd) {
      MethodsFishing.FishRow fr = caughtFish(performer, act.getPosX(), act.getPosY(), source);
      if (fr == null) {
         int moreTime = 100 + Server.rand.nextInt(250);
         int timeleft = act.getSecond() * 10 + moreTime;
         act.setData((long)timeleft);
         testMessage(performer, "No Fish!", " Next attempt in approx:" + moreTime / 10 + "s");
         return false;
      } else {
         float speedMod = 1.0F;
         float rot = performer.getStatus().getRotation();
         float angle;
         if (performer.getVehicle() == -10L) {
            int ang = Server.rand.nextInt(40);
            if (Server.rand.nextBoolean()) {
               angle = Creature.normalizeAngle(rot - 130.0F + (float)ang);
            } else {
               angle = Creature.normalizeAngle(rot + 130.0F - (float)ang);
            }
         } else {
            angle = (float)Server.rand.nextInt(360);
         }

         Point4f mid;
         Point4f start;
         Point4f end;
         if (startCmd == 20) {
            float dist = 0.05F + Server.rand.nextFloat();
            mid = calcSpot(act.getPosX(), act.getPosY(), performer.getStatus().getRotation(), dist);
            start = calcSpot(mid.getPosX(), mid.getPosY(), angle, 10.0F);
            end = calcSpot(start.getPosX(), start.getPosY(), start.getRot(), 20.0F);

            try {
               if (Zones.calculateHeight(start.getPosX(), start.getPosY(), performer.isOnSurface()) > 0.0F) {
                  Point4f temp = start;
                  start = end;
                  end = temp;
               }
            } catch (NoSuchZoneException var19) {
               logger.log(Level.WARNING, var19.getMessage(), (Throwable)var19);
            }

            speedMod = 0.5F;
         } else {
            mid = new Point4f(act.getPosX(), act.getPosY(), 0.0F, Creature.normalizeAngle(rot + 180.0F));
            start = calcSpot(mid.getPosX(), mid.getPosY(), angle, 10.0F);
            end = new Point4f(mid.getPosX(), mid.getPosY(), 0.0F, start.getRot());

            try {
               if (Zones.calculateHeight(start.getPosX(), start.getPosY(), performer.isOnSurface()) > 0.0F) {
                  Point4f temp = start;
                  start = end;
                  end = temp;
               }
            } catch (NoSuchZoneException var18) {
               logger.log(Level.WARNING, var18.getMessage(), (Throwable)var18);
            }

            speedMod = 1.2F;
         }

         double ql = getQL(performer, source, fishing, fr.getFishTypeId(), act.getPosX(), act.getPosY(), true);
         Creature fish = makeFishCreature(performer, start, fr.getName(), ql, fr.getFishTypeId(), speedMod, end);
         if (fish == null) {
            performer.getCommunicator().sendNormalServerMessage("You jump as you see a ghost of a fish, and stop fishing.");
            return true;
         } else {
            act.setCreature(fish);
            FishAI.FishAIData faid = (FishAI.FishAIData)fish.getCreatureAIData();
            int moreTime = (int)faid.getTimeToTarget();
            int timeleft = act.getSecond() * 10 + moreTime;
            act.setData((long)timeleft);
            if (Servers.isThisATestServer() && (performer.getPower() > 1 || performer.hasFlag(51))) {
               if (performer.getPower() >= 2) {
                  testMessage(
                     performer,
                     "",
                     performer.getName()
                        + " @px:"
                        + (int)performer.getPosX()
                        + ",py:"
                        + (int)performer.getPosY()
                        + ",pr:"
                        + (int)performer.getStatus().getRotation()
                  );
               }

               if (startCmd == 20) {
                  addMarker(performer, fish.getName() + " mid", mid);
               }

               addMarker(performer, fish.getName() + " start", start);
               addMarker(performer, fish.getName() + " end", end);
            }

            return false;
         }
      }
   }

   @Nullable
   public static Creature makeFishCreature(Creature performer, Point4f start, String fishName, double ql, byte fishTypeId, float speedMod, Point4f end) {
      try {
         Creature fish = Creature.doNew(
            119, start.getPosX(), start.getPosY(), start.getRot(), performer.getLayer(), fishName, (byte)(Server.rand.nextBoolean() ? 0 : 1), (byte)0
         );
         fish.setVisible(false);
         FishAI.FishAIData faid = (FishAI.FishAIData)fish.getCreatureAIData();
         faid.setFishTypeId(fishTypeId);
         faid.setQL(ql);
         faid.setMovementSpeedModifier(speedMod);
         faid.setTargetPos(end.getPosX(), end.getPosY());
         fish.setVisible(true);
         return fish;
      } catch (Exception var10) {
         logger.log(Level.WARNING, var10.getMessage(), (Throwable)var10);
         return null;
      }
   }

   public static void addMarker(Creature performer, String string, Point4f loc) {
      if (Servers.isThisATestServer()) {
         String sloc = "";
         if (performer.getPower() >= 2) {
            int depth = FishEnums.getWaterDepth(loc.getPosX(), loc.getPosY(), performer.isOnSurface());
            sloc = " @x:" + (int)loc.getPosX() + ", y:" + (int)loc.getPosY() + ", r:" + (int)loc.getRot() + ", d:" + depth;
            testMessage(performer, string, sloc);
         }

         if (performer.getPower() >= 5 && performer.hasFlag(51)) {
            VolaTile vtile = Zones.getOrCreateTile(loc.getTileX(), loc.getTileY(), performer.isOnSurface());

            try {
               Item marker = ItemFactory.createItem(344, 1.0F, null);
               marker.setSizes(10, 10, 100);
               marker.setPosXY(loc.getPosX(), loc.getPosY());
               marker.setRotation(loc.getRot());
               marker.setDescription(string + sloc);
               vtile.addItem(marker, false, false);
            } catch (FailedException var6) {
               logger.log(Level.WARNING, var6.getMessage(), (Throwable)var6);
            } catch (NoSuchTemplateException var7) {
               logger.log(Level.WARNING, var7.getMessage(), (Throwable)var7);
            }
         }
      }
   }

   public static Point4f calcSpot(float posx, float posy, float rot, float dist) {
      float r = rot * (float) Math.PI / 180.0F;
      float s = (float)Math.sin((double)r);
      float c = (float)Math.cos((double)r);
      float xo = s * dist;
      float yo = c * -dist;
      float newx = posx + xo;
      float newy = posy + yo;
      float angle = rot + 180.0F;
      return new Point4f(newx, newy, 0.0F, Creature.normalizeAngle(angle));
   }

   private static int makeDeadFish(Creature performer, Action act, Skill fishing, byte fishTypeId, Item source, Item container) {
      Creature fish = act.getCreature();
      double ql;
      FishEnums.FishData fd;
      int weight;
      if (fish == null) {
         if (source.getTemplateId() != 1343) {
            return 0;
         }

         fd = FishEnums.FishData.fromInt(fishTypeId);
         ql = getQL(performer, source, fishing, fishTypeId, act.getPosX(), act.getPosY(), false);
         ItemTemplate it = fd.getTemplate();
         if (it == null) {
            testMessage(performer, "", fd.getName() + " no template!");
            return 0;
         }

         weight = (int)((double)it.getWeightGrams() * (ql / 100.0));
      } else {
         FishAI.FishAIData faid = (FishAI.FishAIData)fish.getCreatureAIData();
         fd = faid.getFishData();
         ql = faid.getQL();
         weight = faid.getWeight();
      }

      if (weight == 0) {
         performer.getCommunicator().sendNormalServerMessage("You fumbled when trying to move the fish to " + container.getName() + ", and it got away.");
         return 0;
      } else {
         testMessage(performer, "", fd.getTemplate().getName() + ":" + weight + "g");
         if (weight >= fd.getMinWeight()) {
            try {
               if (act.getRarity() != 0) {
                  performer.playPersonalSound("sound.fx.drumroll");
               }

               Item deadFish = ItemFactory.createItem(fd.getTemplateId(), (float)ql, (byte)2, act.getRarity(), performer.getName());
               deadFish.setSizes(weight);
               deadFish.setWeight(weight, false);
               boolean inserted = container.insertItem(deadFish);
               if (inserted) {
                  if (source.getTemplateId() == 1343) {
                     performer.getCommunicator().sendNormalServerMessage("You catch " + deadFish.getNameWithGenus() + " in the " + source.getName() + ".");
                  }

                  if (fd.getTypeId() == FishEnums.FishData.CLAM.getTypeId()) {
                     performer.getCommunicator().sendNormalServerMessage("You will need to pry open the clam with a knife.");
                  }

                  performer.achievement(126);
                  if (weight > 3000) {
                     performer.achievement(542);
                  }

                  if (weight > 10000) {
                     performer.achievement(585);
                  }

                  if (weight > 175000) {
                     performer.achievement(297);
                  }
               } else {
                  performer.getCommunicator()
                     .sendNormalServerMessage(
                        "You fumbled when trying to move the " + deadFish.getName() + " to " + container.getName() + ", and it got away."
                     );
                  Items.destroyItem(deadFish.getWurmId());
               }

               act.setRarity(performer.getRarity());
            } catch (FailedException var13) {
               logger.log(Level.WARNING, var13.getMessage(), (Throwable)var13);
            } catch (NoSuchTemplateException var14) {
               logger.log(Level.WARNING, var14.getMessage(), (Throwable)var14);
            }

            return weight;
         } else {
            if (source.getTemplateId() == 1343) {
               performer.getCommunicator().sendNormalServerMessage("The " + fd.getTemplate().getName() + "  was so small, it swam through the net.");
            } else {
               performer.getCommunicator().sendNormalServerMessage("The " + fd.getTemplate().getName() + "  was so small, you threw it back in.");
               Server.getInstance().broadCastAction(performer.getName() + " throws the tiddler " + fd.getTemplate().getName() + " back in.", performer, 5);
            }

            return 0;
         }
      }
   }

   private static float additionalDamage(Item item, float damMod, boolean flexible) {
      if (item == null) {
         return 0.0F;
      } else {
         float typeMod = item.isWood() ? 2.0F : (item.isMetal() ? 1.0F : 0.5F);
         float newDam = typeMod * item.getDamageModifier(false, flexible) / (10.0F * Math.max(10.0F, item.getQualityLevel()));
         float qlMod = (100.0F - item.getCurrentQualityLevel()) / 50.0F;
         return Math.min(qlMod, Math.max(0.1F, newDam) * damMod);
      }
   }

   private static double getQL(Creature performer, Item source, Skill fishing, byte fishTypeId, float posx, float posy, boolean noSkill) {
      Item[] fishingItems = source.getFishingItems();
      Item fishingReel = fishingItems[0];
      Item fishingLine = fishingItems[1];
      Item fishingFloat = fishingItems[2];
      Item fishingHook = fishingItems[3];
      Item fishingBait = fishingItems[4];
      FishEnums.FishData fd = FishEnums.FishData.fromInt(fishTypeId);
      float knowledge = (float)fishing.getKnowledge();
      float difficulty = fd.getDifficulty(
         knowledge, posx, posy, performer.isOnSurface(), source, fishingReel, fishingLine, fishingFloat, fishingHook, fishingBait
      );
      double power = fishing.skillCheck((double)difficulty, null, 0.0, noSkill, 10.0F);
      return 10.0 + 0.9 * Math.max((double)(Server.rand.nextFloat() * 10.0F), power);
   }

   private static boolean autoCast(Creature performer, Action act, Item rod) {
      Item[] fishingItems = rod.getFishingItems();
      Item line = fishingItems[1];
      float[] radi = getMinMaxRadius(rod, line);
      float minRadius = radi[0];
      float maxRadius = radi[1];
      float half = (maxRadius + minRadius) / 2.0F;
      float rot = performer.getStatus().getRotation();
      float r = rot * (float) Math.PI / 180.0F;
      float s = (float)Math.sin((double)r);
      float c = (float)Math.cos((double)r);
      float xo = s * half;
      float yo = c * -half;
      float castX = performer.getPosX() + xo;
      float castY = performer.getPosY() + yo;
      int tilex = (int)castX >> 2;
      int tiley = (int)castY >> 2;
      int tile;
      if (performer.isOnSurface()) {
         tile = Server.surfaceMesh.getTile(tilex, tiley);
      } else {
         tile = Server.caveMesh.getTile(tilex, tiley);
      }

      if (!Terraforming.isTileUnderWater(tile, tilex, tiley, performer.isOnSurface())) {
         performer.getCommunicator().sendNormalServerMessage("The water is too shallow to fish.");
         testMessage(performer, "", performer.getTileX() + "=" + tilex + "," + performer.getTileY() + "=" + tiley);
         return true;
      } else {
         testMessage(performer, "Auto cast as you were too lazy to cast!", "");
         Server.getInstance().broadCastAction(performer.getName() + " casts and starts fishing.", performer, 5);
         act.setTickCount(9);
         return processFishCasted(performer, act, castX, castY, rod, false);
      }
   }

   private static boolean processFishCasted(Creature performer, Action act, float castX, float castY, Item rod, boolean manualMode) {
      act.setPosX(castX);
      act.setPosY(castY);
      int defaultTimer = 1800;
      act.setTimeLeft(1800);
      performer.sendActionControl(Actions.actionEntrys[160].getVerbString(), true, 1800);
      Item[] fishingItems = rod.getFishingItems();
      Item floatItem = fishingItems[2];
      performer.sendFishingLine(castX, castY, FishEnums.FloatType.fromItem(floatItem).getTypeId());
      act.setTickCount(16);
      int moreTime = getNextFishDelay(rod, 100, 600);
      int timeleft = act.getSecond() * 10 + moreTime;
      act.setData((long)timeleft);
      if (Servers.isThisATestServer() && (performer.getPower() > 1 || performer.hasFlag(51))) {
         Point4f cast = new Point4f(castX, castY, 0.0F, 0.0F);
         addMarker(performer, (manualMode ? "Manual" : "Auto") + " cast (" + moreTime / 10 + "s)", cast);
      }

      return false;
   }

   private static boolean processFishMove(Creature performer, Action act, Skill fishing, Item rod) {
      if (act.getCreature() == null) {
         if (makeFish(performer, act, rod, fishing, (byte)0)) {
            return true;
         } else if (act.getCreature() == null) {
            return false;
         } else {
            Creature fish = act.getCreature();
            FishAI.FishAIData faid = (FishAI.FishAIData)fish.getCreatureAIData();
            int moreTime = (int)faid.getTimeToTarget();
            int timeleft = act.getSecond() * 10 + moreTime;
            act.setData((long)timeleft);
            act.setTickCount(1);
            return false;
         }
      } else {
         act.setTickCount(14);
         return false;
      }
   }

   private static boolean processFishMovedOn(Creature performer, Action act) {
      performer.getCommunicator().sendFishSubCommand((byte)2, -1L);
      processFishMovedOn(performer, act, 1.2F);
      act.setTickCount(19);
      return false;
   }

   private static boolean processFishMovingOn(Creature performer, Action act) {
      act.setTickCount(16);
      return false;
   }

   private static boolean processFishMovedOn(Creature performer, Action act, float speed) {
      Creature fish = act.getCreature();
      testMessage(performer, fish.getName() + " Swims off.", "");
      performer.getCommunicator().sendNormalServerMessage("The " + fish.getName() + " swims off into the distance.");
      FishAI.FishAIData faid = (FishAI.FishAIData)fish.getCreatureAIData();
      Point4f end = calcSpot(fish.getPosX(), fish.getPosY(), performer.getStatus().getRotation(), 10.0F);
      faid.setMovementSpeedModifier(faid.getMovementSpeedModifier() * speed);
      faid.setTargetPos(end.getPosX(), end.getPosY());
      int moreTime = (int)faid.getTimeToTarget();
      int timeleft = act.getSecond() * 10 + moreTime;
      act.setData((long)timeleft);
      return false;
   }

   private static boolean processFishSwamAway(Creature performer, Action act, Item rod, int delay) {
      destroyFishCreature(act);
      int moreTime = getNextFishDelay(rod, delay, 200);
      int timeleft = act.getSecond() * 10 + moreTime;
      act.setData((long)timeleft);
      act.setTickCount(16);
      return false;
   }

   private static int getNextFishDelay(Item rod, int delay, int rnd) {
      Item[] fishingItems = rod.getFishingItems();
      Item bait = fishingItems[4];
      int bonus = bait == null ? 0 : bait.getRarity() * 10;
      Item hook = fishingItems[3];
      float fmod = hook == null ? 1.0F : hook.getMaterialFragrantModifier();
      return (int)((float)(delay + Server.rand.nextInt(rnd) - bonus) * fmod);
   }

   private static boolean processFishCancel(Creature performer, Action act) {
      performer.getCommunicator().sendNormalServerMessage("You have cancelled your fishing!");
      return sendFishStop(performer, act);
   }

   private static boolean processFishBite(Creature performer, Action act, Item rod, Skill fishing) {
      Creature fish = act.getCreature();
      FishAI.FishAIData faid = (FishAI.FishAIData)fish.getCreatureAIData();
      if (Servers.isThisATestServer() && (performer.getPower() > 1 || performer.hasFlag(51))) {
         String fpos = performer.getPower() >= 2
            ? " fx:" + (int)fish.getPosX() + ",fy:" + (int)fish.getPosY() + " cx:" + (int)act.getPosX() + ",cy:" + (int)act.getPosY()
            : "";
         testMessage(performer, fish.getName() + " takes a bite!", fpos);
      }

      Item[] fishingItems = rod.getFishingItems();
      Item fishingLine = fishingItems[1];
      Item fishingFloat = fishingItems[2];
      Item fishingHook = fishingItems[3];
      Item fishingBait = fishingItems[4];
      FishEnums.FishData fd = faid.getFishData();
      float damMod = (float)fd.getDamageMod() * Math.max(0.1F, (float)(faid.getWeight() / 3000));
      float additionalDamage = additionalDamage(fishingFloat, damMod * 5.0F, false);
      float newDamage = fishingFloat.getDamage() + additionalDamage;
      if (newDamage > 100.0F) {
      }

      if (additionalDamage > 0.0F) {
         fishingFloat.setDamage(newDamage);
      }

      additionalDamage = additionalDamage(fishingHook, damMod * 10.0F, false);
      newDamage = fishingHook.getDamage() + additionalDamage;
      if (newDamage > 100.0F) {
         destroyContents(fishingHook);
      }

      if (additionalDamage > 0.0F) {
         fishingHook.setDamage(newDamage);
      }

      fishingHook = fishingLine.getFishingHook();
      if (fishingHook != null) {
         fishingBait = fishingHook.getFishingBait();
         if (fishingBait != null) {
            float dam = getBaitDamage(fd, fishingBait);
            if (fishingBait.setDamage(fishingBait.getDamage() + dam)) {
            }
         }
      }

      Item[] newFishingItems = rod.getFishingItems();
      Item newFishingBait = newFishingItems[4];
      int bonus = newFishingBait == null ? 0 : newFishingBait.getRarity() * 10;
      int skillBonus = (int)(fishing.getKnowledge(0.0) / 3.0);
      int moreTime = 35 + skillBonus + Server.rand.nextInt(20) + bonus;
      int timeleft = act.getSecond() * 10 + moreTime;
      act.setData((long)timeleft);
      act.setTickCount(18);
      performer.getCommunicator().sendFishBite(faid.getFishTypeId(), fish.getWurmId(), -1L);
      performer.getCommunicator().sendNormalServerMessage("You feel something nibble on the line.");
      return false;
   }

   private static float getBaitDamage(FishEnums.FishData fd, Item bait) {
      float crumbles = FishEnums.BaitType.fromItem(bait).getCrumbleFactor();
      float dif = fd.getTemplateDifficulty();
      float base = dif + Server.rand.nextFloat() * 20.0F;
      float newDam = (base + 10.0F) / crumbles;
      return Math.max(20.0F, newDam);
   }

   private static float getRodDamageModifier(Item rod) {
      Item reel = rod.getFishingReel();
      if (reel == null) {
         return 5.0F;
      } else {
         switch(reel.getTemplateId()) {
            case 1372:
               return 4.5F;
            case 1373:
               return 4.0F;
            case 1374:
               return 3.5F;
            case 1375:
               return 2.5F;
            default:
               return 5.0F;
         }
      }
   }

   private static float getReelDamageModifier(Item reel) {
      if (reel == null) {
         return 0.0F;
      } else {
         switch(reel.getTemplateId()) {
            case 1372:
               return 0.1F;
            case 1373:
               return 0.07F;
            case 1374:
               return 0.05F;
            case 1375:
               return 0.02F;
            default:
               return 0.1F;
         }
      }
   }

   private static boolean autoReplace(Creature performer, int templateId, byte material, Item targetContainer) {
      Item tacklebox = performer.getBestTackleBox();
      if (tacklebox != null) {
         Item compartment = getBoxCompartment(tacklebox, templateId);
         if (compartment != null) {
            Item[] contents = compartment.getItemsAsArray();
            if (contents.length > 0) {
               for(Item item : contents) {
                  if (item.getTemplateId() == templateId && (material == 0 || item.getMaterial() == material)) {
                     targetContainer.insertItem(item);
                     item.sendUpdate();
                     return true;
                  }
               }
            }
         }
      }

      return false;
   }

   private static boolean doAutoReplace(Creature performer, Action act) {
      Item rod = act.getSubject();
      if (rod == null) {
         return true;
      } else {
         Skill fishing = performer.getSkills().getSkillOrLearn(10033);
         boolean hasTacklebox = performer.getBestTackleBox() != null;
         float knowledge = (float)fishing.getKnowledge(0.0);
         boolean replaced = false;
         Item reel = rod.getFishingReel();
         if (rod.getTemplateId() == 1346 && reel == null && hasTacklebox) {
            if (knowledge >= 90.0F) {
               FishEnums.ReelType reelType = FishEnums.ReelType.fromInt(rod.getData1() >> 12 & 15);
               byte reelMaterial = (byte)(rod.getData2() >> 8 & 0xFF);
               replaced = autoReplace(performer, reelType.getTemplateId(), reelMaterial, rod);
               if (replaced) {
                  reel = rod.getFishingReel();
                  performer.getCommunicator().sendNormalServerMessage("You managed to put another " + reel.getName() + " in the " + rod.getName() + "!");
               } else {
                  performer.getCommunicator().sendNormalServerMessage("No replacement reel found!");
               }
            } else {
               performer.getCommunicator().sendNormalServerMessage("You cannot remember what the reel was!");
            }

            if (!replaced) {
               performer.getCommunicator().sendNormalServerMessage("You are missing reel, line, float, fishing hook and bait!");
               return true;
            }
         }

         Item lineParent = rod.getTemplateId() == 1346 ? reel : rod;
         replaced = false;
         Item line = lineParent.getFishingLine();
         if (line == null && hasTacklebox) {
            if (!(knowledge > 70.0F) && reel == null) {
               performer.getCommunicator().sendNormalServerMessage("You cannot remember what the line was!");
            } else {
               int lineTemplateId = FishEnums.ReelType.fromItem(reel).getAssociatedLineTemplateId();
               replaced = autoReplace(performer, lineTemplateId, (byte)0, lineParent);
               if (replaced) {
                  line = lineParent.getFishingLine();
                  performer.getCommunicator()
                     .sendNormalServerMessage("You managed to put another " + line.getName() + " in the " + lineParent.getName() + "!");
               } else {
                  performer.getCommunicator().sendNormalServerMessage("No replacement line found!");
               }
            }

            if (!replaced) {
               performer.getCommunicator().sendNormalServerMessage("You are missing line, float, fishing hook and bait!");
               return true;
            }
         }

         Item afloat = line.getFishingFloat();
         if (afloat == null && hasTacklebox) {
            if (knowledge > 50.0F) {
               FishEnums.FloatType floatType = FishEnums.FloatType.fromInt(rod.getData1() >> 8 & 15);
               replaced = autoReplace(performer, floatType.getTemplateId(), (byte)0, line);
               if (replaced) {
                  afloat = line.getFishingFloat();
                  String floatName = afloat.getName();
                  performer.getCommunicator().sendNormalServerMessage("You managed to put another " + floatName + " on the " + line.getName() + "!");
               } else {
                  performer.getCommunicator().sendNormalServerMessage("No replacement float found!");
               }
            } else {
               performer.getCommunicator().sendNormalServerMessage("You cannot remember what the float was!");
            }
         }

         Item hook = line.getFishingHook();
         if (hook == null && hasTacklebox) {
            if (knowledge > 30.0F) {
               FishEnums.HookType hookType = FishEnums.HookType.fromInt(rod.getData1() >> 4 & 15);
               byte hookMaterial = (byte)(rod.getData2() & 0xFF);
               replaced = autoReplace(performer, hookType.getTemplateId(), hookMaterial, line);
               if (replaced) {
                  hook = line.getFishingHook();
                  performer.getCommunicator().sendNormalServerMessage("You managed to put another " + hook.getName() + " on the " + line.getName() + "!");
               } else {
                  performer.getCommunicator().sendNormalServerMessage("No replacement fishing hook found!");
               }
            } else {
               performer.getCommunicator().sendNormalServerMessage("You cannot remember what the fishing hook was!");
            }
         }

         Item bait = null;
         if (hook != null) {
            bait = hook.getFishingBait();
            if (bait == null && hasTacklebox) {
               if (knowledge > 10.0F) {
                  FishEnums.BaitType baitType = FishEnums.BaitType.fromInt(rod.getData1() & 15);
                  replaced = autoReplace(performer, baitType.getTemplateId(), (byte)0, hook);
                  if (replaced) {
                     bait = hook.getFishingBait();
                     performer.getCommunicator().sendNormalServerMessage("You managed to put another " + bait.getName() + " on the " + hook.getName() + "!");
                  } else {
                     performer.getCommunicator().sendNormalServerMessage("No replacement bait found!");
                  }
               } else {
                  performer.getCommunicator().sendNormalServerMessage("You cannot remember what the bait was!");
               }
            }
         }

         if (afloat == null && hook == null) {
            performer.getCommunicator().sendNormalServerMessage("You are missing a float, fishing hook and bait!");
            return true;
         } else if (afloat == null && bait == null) {
            performer.getCommunicator().sendNormalServerMessage("You are missing a float and bait!");
            return true;
         } else if (afloat == null) {
            performer.getCommunicator().sendNormalServerMessage("You are missing a float!");
            return true;
         } else if (hook == null) {
            performer.getCommunicator().sendNormalServerMessage("You are missing a fishing hook and bait!");
            return true;
         } else if (bait == null) {
            performer.getCommunicator().sendNormalServerMessage("You are missing a bait!");
            return true;
         } else {
            return true;
         }
      }
   }

   @Nullable
   private static Item getBoxCompartment(Item tacklebox, int templateId) {
      for(Item compartment : tacklebox.getItems()) {
         for(ContainerRestriction cRest : compartment.getTemplate().getContainerRestrictions()) {
            if (cRest.contains(templateId)) {
               return compartment;
            }
         }
      }

      return null;
   }

   private static boolean processFishPause(Creature performer, Action act) {
      act.setTickCount(2);
      return false;
   }

   private static boolean processFishHooked(Creature performer, Action act, Skill fishing, Item rod) {
      if (act.getCreature() == null) {
         act.setTickCount(5);
      } else {
         Creature fish = act.getCreature();
         FishAI.FishAIData faid = (FishAI.FishAIData)fish.getCreatureAIData();
         performer.getCommunicator().sendFishSubCommand((byte)3, -1L);
         performer.sendFishHooked(faid.getFishTypeId(), fish.getWurmId());
         if (processFishPull(performer, act, fishing, rod, true)) {
            sendFishStop(performer, act);
            return true;
         }
      }

      return false;
   }

   private static boolean processFishPull(Creature performer, Action act, Skill fishing, Item rod, boolean initial) {
      Item[] fishingItems = rod.getFishingItems();
      Item fishingReel = fishingItems[0];
      Item fishingLine = fishingItems[1];
      Item fishingFloat = fishingItems[2];
      Item fishingHook = fishingItems[3];
      Item fishingBait = fishingItems[4];
      Creature fish = act.getCreature();
      FishAI.FishAIData faid = (FishAI.FishAIData)fish.getCreatureAIData();
      boolean isClam = faid.getFishTypeId() == FishEnums.FishData.CLAM.getTypeId();
      if (initial) {
         performer.getCommunicator().sendNormalServerMessage("You hooked " + faid.getNameWithGenusAndSize() + "!");
         Server.getInstance().broadCastAction(performer.getName() + " hooks " + faid.getNameWithGenusAndSize() + ".", performer, 5);
      }

      float fstr = faid.getBodyStrength();
      float pstr = (float)performer.getSkills().getSkillOrLearn(102).getKnowledge(0.0);
      float strBonus = Math.max(1.0F, pstr - fstr);
      float stamBonus = faid.getBodyStamina() - 20.0F;
      float bonus = (strBonus + stamBonus) / 2.0F;
      float result = getDifficulty(
         performer, act, act.getPosX(), act.getPosY(), fishing, rod, fishingReel, fishingLine, fishingFloat, fishingHook, fishingBait, bonus, 10.0F
      );
      float adjusted = result;
      if (fishingReel != null) {
         adjusted = result + (float)(fishingReel.getRarity() * fishingReel.getRarity());
      }

      if (adjusted < 0.0F && !isClam) {
         if (result <= (float)(-(90 + rod.getRarity() * 3))) {
            act.setTickCount(7);
         } else if (result <= (float)(-(70 + fishingLine.getRarity() * 3))) {
            act.setTickCount(6);
         } else if (result <= (float)(-(50 + fishingHook.getRarity() * 3))) {
            act.setTickCount(13);
         } else {
            if (!initial) {
               if (result <= -30.0F) {
                  performer.getCommunicator().sendNormalServerMessage("The " + faid.getNameWithSize() + " pulls hard on the line!");
               } else if (result <= -15.0F) {
                  performer.getCommunicator().sendNormalServerMessage("The " + faid.getNameWithSize() + " pulls somewhat on the line!");
               } else {
                  performer.getCommunicator().sendNormalServerMessage("The " + faid.getNameWithSize() + " pulls a bit on the line!");
               }
            }

            testMessage(performer, faid.getNameWithSize() + " moving away", " Result:" + result);
            faid.decBodyStamina(strBonus / 2.0F + 2.0F);
            double lNewrot = Math.atan2((double)(performer.getPosY() - fish.getPosY()), (double)(performer.getPosX() - fish.getPosX()));
            float rot = (float)(lNewrot * (180.0 / Math.PI)) - 90.0F;
            float angle = rot + result * 2.0F + 90.0F;
            float dist = Math.min(2.0F, strBonus / 10.0F);
            Point4f end = calcSpot(fish.getPosX(), fish.getPosY(), Creature.normalizeAngle(angle), dist);
            float speedMod = dist / 2.0F;
            faid.setMovementSpeedModifier(speedMod);
            faid.setTargetPos(end.getPosX(), end.getPosY());
            int moreTime = (int)faid.getTimeToTarget();
            int timeleft = act.getSecond() * 10 + moreTime;
            act.setData((long)timeleft);
            act.setTickCount(17);
         }
      } else {
         if (!initial && !isClam) {
            if (result > 80.0F) {
               performer.getCommunicator().sendNormalServerMessage("You manage to easily reel in the " + faid.getNameWithSize() + "!");
            } else if (result > 50.0F) {
               performer.getCommunicator().sendNormalServerMessage("The " + faid.getNameWithSize() + " stands no chance!");
            } else if (result > 25.0F) {
               performer.getCommunicator().sendNormalServerMessage("The " + faid.getNameWithSize() + " takes a rest, so you reel it in a bit!");
            } else {
               performer.getCommunicator()
                  .sendNormalServerMessage("The " + faid.getNameWithSize() + " starts to get tired and you manage to reel it in a bit!");
            }
         }

         double lNewrot = Math.atan2((double)(performer.getPosY() - fish.getPosY()), (double)(performer.getPosX() - fish.getPosX()));
         float rot = (float)(lNewrot * (180.0 / Math.PI));
         Point4f end;
         float speedMod;
         if (isClam) {
            performer.getCommunicator().sendNormalServerMessage("You quickly reel in the " + faid.getNameWithSize() + "!");
            speedMod = 2.0F;
            end = new Point4f(performer.getPosX(), performer.getPosY(), 0.0F, Creature.normalizeAngle(rot + 180.0F));
         } else {
            testMessage(performer, faid.getNameWithSize() + " moving closer", " Result:" + result);
            faid.decBodyStamina(1.0F);
            float angle = rot + result + 40.0F;
            float dist = Math.min(2.5F, strBonus / 15.0F + Math.max(0.0F, result - 50.0F) / 15.0F);
            speedMod = 1.0F;
            end = calcSpot(fish.getPosX(), fish.getPosY(), Creature.normalizeAngle(angle), dist);
         }

         faid.setMovementSpeedModifier(speedMod);
         faid.setTargetPos(end.getPosX(), end.getPosY());
         int moreTime = (int)faid.getTimeToTarget();
         int timeleft = act.getSecond() * 10 + moreTime;
         act.setData((long)timeleft);
         act.setTickCount(17);
      }

      FishEnums.FishData fd = faid.getFishData();
      float fdam = (float)fd.getDamageMod();
      float damMod = (float)(
         (double)fdam * Math.max(0.1F, faid.getWeight() > 6000 ? Math.pow((double)(faid.getWeight() / 1000), 0.6) : (double)(faid.getWeight() / 3000))
      );
      float additionalDamage = additionalDamage(rod, damMod * getRodDamageModifier(rod), true);
      float newDamage = rod.getDamage() + additionalDamage;
      if (newDamage > 100.0F) {
         destroyContents(rod);
         return rod.setDamage(newDamage);
      } else {
         if (additionalDamage > 0.0F) {
            rod.setDamage(newDamage);
         }

         if (rod.getTemplateId() == 1344) {
            fishingLine = rod.getFishingLine();
         } else {
            fishingReel = rod.getFishingReel();
            additionalDamage = additionalDamage(fishingReel, damMod * getReelDamageModifier(fishingReel), true);
            newDamage = fishingReel.getDamage() + additionalDamage;
            if (newDamage > 100.0F) {
               destroyContents(fishingReel);
               return fishingReel.setDamage(newDamage);
            }

            if (additionalDamage > 0.0F) {
               fishingReel.setDamage(newDamage);
            }

            fishingLine = fishingReel.getFishingLine();
         }

         additionalDamage = additionalDamage(fishingLine, damMod * 0.3F, true);
         newDamage = fishingLine.getDamage() + additionalDamage;
         if (newDamage > 100.0F) {
            destroyContents(fishingLine);
            return fishingLine.setDamage(newDamage);
         } else {
            if (additionalDamage > 0.0F) {
               fishingLine.setDamage(newDamage);
            }

            return false;
         }
      }
   }

   private static boolean processFishCaught(Creature performer, Action act, Skill fishing, Item rod) {
      Creature fish = act.getCreature();
      performer.getCommunicator().sendFishSubCommand((byte)12, -1L);
      performer.sendFishingStopped();
      FishAI.FishAIData faid = (FishAI.FishAIData)fish.getCreatureAIData();
      performer.getCommunicator().sendNormalServerMessage("You catch " + faid.getNameWithGenusAndSize());
      Server.getInstance().broadCastAction(performer.getName() + " lands " + faid.getNameWithGenusAndSize() + ".", performer, 5);
      makeDeadFish(performer, act, fishing, faid.getFishTypeId(), rod, performer.getInventory());
      destroyFishCreature(act);
      return doAutoReplace(performer, act);
   }

   private static boolean processFishLineSnapped(Creature performer, Action act, Item rod) {
      performer.getCommunicator().sendNormalServerMessage("Your line snapped!!");
      Item[] fishingItems = rod.getFishingItems();
      Item fishingReel = fishingItems[0];
      Item fishingLine = fishingItems[1];
      destroyContents(fishingLine);
      byte currentPhase = (byte)act.getTickCount();
      boolean brokeRod = false;
      if (currentPhase == 7) {
         Creature fish = act.getCreature();
         FishAI.FishAIData faid = (FishAI.FishAIData)fish.getCreatureAIData();
         FishEnums.FishData fd = faid.getFishData();
         int weight = faid.getWeight();
         float fdam = (float)fd.getDamageMod() * 5.0F;
         float damMod = fdam * Math.max(0.1F, (float)(weight / 3000));
         float additionalDamage = additionalDamage(rod, damMod * getRodDamageModifier(rod) * 2.0F, true);
         float newDamage = rod.getDamage() + additionalDamage;
         if (newDamage > 100.0F) {
            destroyContents(rod);
         }

         if (additionalDamage > 0.0F) {
            brokeRod = rod.setDamage(newDamage);
         }

         if (!brokeRod && fishingReel != null) {
            additionalDamage = additionalDamage(fishingReel, damMod * getReelDamageModifier(fishingReel) * 2.0F, true);
            newDamage = fishingReel.getDamage() + additionalDamage;
            if (newDamage > 100.0F) {
               destroyContents(fishingReel);
            }

            if (additionalDamage > 0.0F) {
               fishingReel.setDamage(newDamage);
            }
         }
      }

      performer.getCommunicator().sendFishSubCommand((byte)6, -1L);
      processFishMovedOn(performer, act, 2.2F);
      act.setTickCount(15);
      return false;
   }

   private static void destroyContents(Item container) {
      for(Item item : container.getItemsAsArray()) {
         if (!item.isEmpty(false)) {
            destroyContents(item);
         }

         item.setDamage(100.0F);
      }
   }

   private static boolean sendFishStop(Creature performer, Action act) {
      destroyFishCreature(act);
      performer.getCommunicator().sendFishSubCommand((byte)15, -1L);
      performer.sendFishingStopped();
      return doAutoReplace(performer, act);
   }

   public static void fromClient(Creature performer, byte subCommand, float posX, float posY) {
      try {
         Action act = performer.getCurrentAction();
         if (act.getNumber() != 160) {
            testMessage(performer, "not fishing? ", "Action:" + act.getNumber());
            logger.log(Level.WARNING, "not fishing! " + act.getNumber());
            return;
         }

         byte phase = (byte)act.getTickCount();
         switch(subCommand) {
            case 9:
               if (phase != 0) {
                  testMessage(performer, "Incorrect fishing subcommand", " (" + fromCommand(subCommand) + ") for phase (" + fromCommand(phase) + ")");
                  logger.log(Level.WARNING, "Incorrect fishing subcommand (" + fromCommand(subCommand) + ") for phase (" + fromCommand(phase) + ")");
                  return;
               }

               Item rod = act.getSubject();
               if (rod == null) {
                  testMessage(performer, "", "Subject missing in action");
                  logger.log(Level.WARNING, "Subject missing in action");
               } else {
                  performer.getCommunicator().sendNormalServerMessage("You cast the line and start fishing.");
                  Server.getInstance().broadCastAction(performer.getName() + " casts and starts fishing.", performer, 5);
                  processFishCasted(performer, act, posX, posY, rod, true);
               }
               break;
            case 10:
               act.setTickCount(subCommand);
               break;
            case 11:
               Skill fishing = performer.getSkills().getSkillOrLearn(10033);
               Item rod = act.getSubject();
               if (rod == null) {
                  testMessage(performer, "", "Subject missing in action");
                  logger.log(Level.WARNING, "Subject missing in action");
               } else if (canStrike(phase)) {
                  processFishStrike(performer, act, fishing, rod);
               }
               break;
            case 26:
               if (phase != 21) {
                  testMessage(performer, "Incorrect fishing subcommand", " (" + fromCommand(subCommand) + ") for phase (" + fromCommand(phase) + ")");
                  logger.log(Level.WARNING, "Incorrect fishing subcommand (" + fromCommand(subCommand) + ") for phase (" + fromCommand(phase) + ")");
                  return;
               }

               act.setTickCount(subCommand);
               Skill fishing = performer.getSkills().getSkillOrLearn(10033);
               Item spear = act.getSubject();
               if (spear == null) {
                  testMessage(performer, "", "Subject missing in action");
                  logger.log(Level.WARNING, "Subject missing in action");
               } else {
                  processSpearStrike(performer, act, fishing, spear, posX, posY);
               }
               break;
            case 27:
               act.setTickCount(subCommand);
               break;
            default:
               testMessage(performer, "Bad fishing subcommand!", " (" + fromCommand(subCommand) + ")");
               logger.log(Level.WARNING, "Bad fishing subcommand! " + fromCommand(subCommand) + " (" + subCommand + ")");
               return;
         }
      } catch (NoSuchActionException var8) {
         testMessage(performer, "", "No current action, should be FISH.");
         logger.log(Level.WARNING, "No current action, should be FISH:" + var8.getMessage(), (Throwable)var8);
      }
   }

   private static boolean canStrike(byte phase) {
      switch(phase) {
         case 3:
         case 17:
            return false;
         default:
            return true;
      }
   }

   private static void testMessage(Creature performer, String message, String powerMessage) {
      if (Servers.isThisATestServer() && (performer.getPower() > 1 || performer.hasFlag(51))) {
         if (performer.getPower() >= 2) {
            performer.getCommunicator().sendNormalServerMessage("(test only) " + message + powerMessage);
         } else if (message.length() > 0) {
            performer.getCommunicator().sendNormalServerMessage("(test only) " + message);
         }
      }
   }

   public static String fromCommand(byte command) {
      switch(command) {
         case 0:
            return "FISH_START";
         case 1:
            return "FISH_BITE";
         case 2:
            return "FISH_MOVED_ON";
         case 3:
            return "FISH_HOOKED";
         case 4:
            return "FISH_MISSED";
         case 5:
            return "FISH_NO_FISH";
         case 6:
            return "FISH_LINE_SNAPPED";
         case 7:
            return "FISH_ROD_BROKE";
         case 8:
            return "FISH_TIME_OUT";
         case 9:
            return "FISH_CASTED";
         case 10:
            return "FISH_CANCEL";
         case 11:
            return "FISH_STRIKE";
         case 12:
            return "FISH_CAUGHT";
         case 13:
            return "FISH_GOT_AWAY";
         case 14:
            return "FISH_SWAM_AWAY";
         case 15:
            return "FISH_STOP";
         case 16:
            return "FISH_MOVE";
         case 17:
            return "FISH_PULL";
         case 18:
            return "FISH_PAUSE";
         case 19:
            return "FISH_MOVING_ON";
         case 20:
            return "SPEAR_START";
         case 21:
            return "SPEAR_MOVE";
         case 22:
            return "SPEAR_HIT";
         case 23:
            return "SPEAR_MISSED";
         case 24:
            return "SPEAR_NO_FISH";
         case 25:
            return "SPEAR_TIME_OUT";
         case 26:
            return "SPEAR_STRIKE";
         case 27:
            return "SPEAR_CANCEL";
         case 28:
            return "SPEAR_SWAM_AWAY";
         case 29:
            return "SPEAR_STOP";
         case 30:
         case 31:
         case 32:
         case 33:
         case 34:
         case 35:
         case 36:
         case 37:
         case 38:
         case 39:
         case 41:
         case 42:
         case 43:
         case 44:
         default:
            return "Unknown (" + command + ")";
         case 40:
            return "NET_START";
         case 45:
            return "SHOW_FISH_SPOTS";
      }
   }

   private static byte getRodType(Item rod, @Nullable Item reel, @Nullable Item line) {
      if (rod.getTemplateId() == 1344) {
         return FishingEnums.RodType.FISHING_POLE.getTypeId();
      } else if (rod.getTemplateId() != 1346 || reel == null) {
         return -1;
      } else if (line == null) {
         switch(reel.getTemplateId()) {
            case 1372:
               return FishingEnums.RodType.FISHING_ROD_BASIC.getTypeId();
            case 1373:
               return FishingEnums.RodType.FISHING_ROD_FINE.getTypeId();
            case 1374:
               return FishingEnums.RodType.FISHING_ROD_DEEP_WATER.getTypeId();
            case 1375:
               return FishingEnums.RodType.FISHING_ROD_DEEP_SEA.getTypeId();
            default:
               return -1;
         }
      } else {
         switch(reel.getTemplateId()) {
            case 1372:
               return FishingEnums.RodType.FISHING_ROD_BASIC_WITH_LINE.getTypeId();
            case 1373:
               return FishingEnums.RodType.FISHING_ROD_FINE_WITH_LINE.getTypeId();
            case 1374:
               return FishingEnums.RodType.FISHING_ROD_DEEP_WATER_WITH_LINE.getTypeId();
            case 1375:
               return FishingEnums.RodType.FISHING_ROD_DEEP_SEA_WITH_LINE.getTypeId();
            default:
               return -1;
         }
      }
   }

   private static float[] getMinMaxRadius(Item rod, Item line) {
      float min = rod.getTemplateId() == 1344 ? 2.0F : 4.0F;
      float linelength = (float)getSingleLineLength(line);
      float max = Math.min((linelength - min) / 2.0F, 8.0F) + min;
      return new float[]{min, max};
   }

   public static int getLineLength(Item line) {
      int lineTemplateWeight = line.getTemplate().getWeightGrams();
      int lineWeight = line.getWeightGrams();
      float comb = (float)(lineWeight / lineTemplateWeight);
      int slen = getSingleLineLength(line);
      return (int)(comb * (float)slen);
   }

   public static int getSingleLineLength(Item line) {
      switch(line.getTemplateId()) {
         case 1347:
            return 10;
         case 1348:
            return 12;
         case 1349:
            return 14;
         case 1350:
            return 16;
         case 1351:
            return 18;
         default:
            return 10;
      }
   }

   public static Color getBgColour(int season) {
      int[] bgRed = new int[]{181, 34, 183, 192};
      int[] bgGreen = new int[]{230, 177, 141, 192};
      int[] bgBlue = new int[]{29, 0, 76, 192};
      return new Color(bgRed[season], bgGreen[season], bgBlue[season]);
   }

   public static Point getSeasonOffset(int season) {
      int[] offsetXs = new int[]{0, 128, 0, 128};
      int[] offsetYs = new int[]{0, 0, 128, 128};
      return new Point(offsetXs[season], offsetYs[season]);
   }

   public static Color getFishColour(int templateId) {
      int red = 0;
      int green = 0;
      int blue = 0;
      switch(templateId) {
         case 569:
            red = 255;
            green = 255;
            break;
         case 570:
            red = 255;
            green = 127;
            break;
         case 571:
            red = 127;
            blue = 255;
         case 572:
         default:
            break;
         case 573:
            green = 255;
            blue = 255;
            break;
         case 574:
            green = 255;
            break;
         case 575:
            blue = 255;
            break;
         case 1336:
            red = 255;
      }

      return new Color(red, green, blue);
   }

   public static class FishRow {
      private final byte fishTypeId;
      private final String name;
      private float chance = 0.0F;

      public FishRow(int fishTypeId, String name) {
         this.fishTypeId = (byte)fishTypeId;
         this.name = name;
      }

      public byte getFishTypeId() {
         return this.fishTypeId;
      }

      public String getName() {
         return this.name;
      }

      public float getChance() {
         return this.chance;
      }

      public void setChance(float chance) {
         this.chance = chance;
      }

      @Override
      public String toString() {
         StringBuilder buf = new StringBuilder();
         buf.append("FishData [");
         buf.append("Name: ").append(this.name);
         buf.append(", Id: ").append(this.fishTypeId);
         buf.append(", Chance: ").append(this.chance);
         buf.append("]");
         return buf.toString();
      }
   }
}

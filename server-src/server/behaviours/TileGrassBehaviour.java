package com.wurmonline.server.behaviours;

import com.wurmonline.mesh.GrassData;
import com.wurmonline.mesh.Tiles;
import com.wurmonline.server.FailedException;
import com.wurmonline.server.Items;
import com.wurmonline.server.Players;
import com.wurmonline.server.Server;
import com.wurmonline.server.creatures.Communicator;
import com.wurmonline.server.creatures.Creature;
import com.wurmonline.server.items.Item;
import com.wurmonline.server.items.ItemFactory;
import com.wurmonline.server.items.ItemTemplateFactory;
import com.wurmonline.server.items.NoSuchTemplateException;
import com.wurmonline.server.items.RuneUtilities;
import com.wurmonline.server.kingdom.Kingdoms;
import com.wurmonline.server.skills.NoSuchSkillException;
import com.wurmonline.server.skills.Skill;
import com.wurmonline.server.utils.StringUtil;
import com.wurmonline.server.villages.NoSuchVillageException;
import com.wurmonline.server.villages.Villages;
import com.wurmonline.server.zones.NoSuchZoneException;
import com.wurmonline.server.zones.Trap;
import com.wurmonline.server.zones.VolaTile;
import com.wurmonline.server.zones.Zone;
import com.wurmonline.server.zones.Zones;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.logging.Level;
import java.util.logging.Logger;

final class TileGrassBehaviour extends TileBehaviour {
   private static final Logger logger = Logger.getLogger(TileBehaviour.class.getName());
   private static final Map<Integer, Byte> flowers = new HashMap<>();

   TileGrassBehaviour() {
      super((short)8);
   }

   static int getFlowerTypeFor(GrassData.FlowerType flowerType) {
      for(Entry<Integer, Byte> entry : flowers.entrySet()) {
         if (entry.getValue() == flowerType.getEncodedData()) {
            return entry.getKey();
         }
      }

      return -1;
   }

   static byte getDataForFlower(int flowerTemplate) {
      Byte b = flowers.get(flowerTemplate);
      return b == null ? 0 : b;
   }

   ActionEntry getGrassBehaviour(int tilex, int tiley, int tile, GrassData.GrowthStage growthStage) {
      byte tileType = Tiles.decodeType(tile);
      String actionString = "Gather";
      if (tileType == Tiles.Tile.TILE_GRASS.id && growthStage == GrassData.GrowthStage.SHORT) {
         return Actions.actionEntrys[644];
      } else {
         if (tileType == Tiles.Tile.TILE_GRASS.id) {
            actionString = "Cut grass";
         } else if (tileType == Tiles.Tile.TILE_REED.id) {
            actionString = "Gather reed";
         } else if (tileType == Tiles.Tile.TILE_KELP.id) {
            actionString = "Gather kelp";
         }

         return new ActionEntry((short)645, actionString, "gathering", new int[]{43});
      }
   }

   @Override
   public List<ActionEntry> getBehavioursFor(Creature performer, int tilex, int tiley, boolean onSurface, int tile) {
      List<ActionEntry> toReturn = new LinkedList<>();
      toReturn.addAll(super.getBehavioursFor(performer, tilex, tiley, onSurface, tile));
      if (Tiles.decodeType(tile) == Tiles.Tile.TILE_GRASS.id) {
         byte tileType = Tiles.decodeType(tile);
         byte tileData = Tiles.decodeData(tile);
         boolean canCollect = TileBehaviour.canCollectSnow(performer, tilex, tiley, tileType, tileData);
         int sz = -2;
         boolean enchant = false;
         if (performer.getCultist() != null && performer.getCultist().mayEnchantNature()) {
            --sz;
            enchant = true;
         }

         toReturn.add(new ActionEntry((short)sz, "Nature", "nature", emptyIntArr));
         toReturn.addAll(this.getBehavioursForForage(performer));
         toReturn.addAll(this.getBehavioursForBotanize(performer));
         if (enchant) {
            toReturn.add(Actions.actionEntrys[388]);
         }

         if (canCollect) {
            toReturn.add(Actions.actionEntrys[741]);
         }
      }

      return toReturn;
   }

   @Override
   public List<ActionEntry> getBehavioursFor(Creature performer, Item subject, int tilex, int tiley, boolean onSurface, int tile) {
      List<ActionEntry> toReturn = new LinkedList<>();
      toReturn.addAll(super.getBehavioursFor(performer, subject, tilex, tiley, onSurface, tile));
      List<ActionEntry> nature = new LinkedList<>();
      byte tileType = Tiles.decodeType(tile);
      byte tileData = Tiles.decodeData(tile);
      boolean isGrassTile = Tiles.isGrassType(tileType);
      if (tileType == Tiles.Tile.TILE_MYCELIUM.id) {
         if (subject.getTemplateId() == 394 || subject.getTemplateId() == 176) {
            nature.add(new ActionEntry((short)644, "Trim mycelium", "Triming"));
         }

         if (subject.getTemplateId() == 266) {
            nature.add(Actions.actionEntrys[186]);
            nature.add(Actions.actionEntrys[660]);
         }
      } else if (isGrassTile && tileType != Tiles.Tile.TILE_LAWN.id) {
         if (subject.getTemplateId() == 266) {
            nature.add(Actions.actionEntrys[186]);
            nature.add(Actions.actionEntrys[660]);
         } else if (subject.isFlower()) {
            toReturn.add(new ActionEntry((short)186, "Plant Flowers", "planting"));
         }

         GrassData.GrowthStage growthStage = GrassData.GrowthStage.decodeTileData(Tiles.decodeData(tile));
         if (growthStage != GrassData.GrowthStage.SHORT && subject.getTemplate().isSharp()) {
            nature.add(this.getGrassBehaviour(tilex, tiley, tile, growthStage));
         }

         if (growthStage == GrassData.GrowthStage.SHORT
            && tileType == Tiles.Tile.TILE_GRASS.id
            && (subject.getTemplateId() == 394 || subject.getTemplateId() == 176 && performer.getPower() >= 2)) {
            nature.add(this.getGrassBehaviour(tilex, tiley, tile, growthStage));
         }

         if ((subject.getTemplateId() == 267 || subject.getTemplateId() == 176 && performer.getPower() >= 2)
            && GrassData.FlowerType.decodeTileData(tileData) != GrassData.FlowerType.NONE
            && tileType == Tiles.Tile.TILE_GRASS.id) {
            nature.add(new ActionEntry((short)187, "Pick flowers", "picking", emptyIntArr));
         }

         if (subject.getTemplateId() == 176 && performer.getPower() >= 2) {
            nature.add(new ActionEntry((short)118, "Grow trees", "growing"));
         }

         if (tileType == Tiles.Tile.TILE_GRASS.id && performer.getCultist() != null && performer.getCultist().mayEnchantNature()) {
            nature.add(Actions.actionEntrys[388]);
         }

         if (subject.getTemplateId() == 526
            && (performer.getKingdomTemplateId() == 3 || GrassData.FlowerType.decodeTileData(tileData) == GrassData.FlowerType.NONE)) {
            nature.add(Actions.actionEntrys[118]);
         }

         byte data = Tiles.decodeData(tile);
         if (canCollectSnow(performer, tilex, tiley, tileType, data) && subject.getTemplateId() == 204) {
            toReturn.add(new ActionEntry((short)148, "Build snowman", "creating", emptyIntArr));
         }
      }

      if (tileType != Tiles.Tile.TILE_LAWN.id) {
         toReturn.addAll(this.getNatureMenu(performer, tilex, tiley, tileType, tileData, nature));
      }

      return toReturn;
   }

   @Override
   public boolean action(Action act, Creature performer, int tilex, int tiley, boolean onSurface, int tile, short action, float counter) {
      boolean done = true;
      byte tileType = Tiles.decodeType(tile);
      String tileName = Tiles.getTile(tileType).getName().toLowerCase();
      if (action == 1) {
         Communicator comm = performer.getCommunicator();
         if (Tiles.isGrassType(tileType)) {
            if (Tiles.decodeType(tile) == Tiles.Tile.TILE_LAWN.id) {
               comm.sendNormalServerMessage("You see a patch of well maintained lawn.");
            } else {
               comm.sendNormalServerMessage(
                  StringUtil.format(
                     "You see a patch of %s. The %s is %s and seems to like it here.",
                     tileName,
                     tileName,
                     StringUtil.toLowerCase(GrassData.GrowthStage.decodeTileData(Tiles.decodeData(tile)))
                  )
               );
            }
         } else if (Tiles.decodeType(tile) == Tiles.Tile.TILE_MYCELIUM.id) {
            comm.sendNormalServerMessage(
               "You see large entwined fungus roots on rotten grass. "
                  + StringUtil.format(
                     "The %s is %s and seems to thrive here.", tileName, StringUtil.toLowerCase(GrassData.GrowthStage.decodeTileData(Tiles.decodeData(tile)))
                  )
            );
         } else {
            comm.sendNormalServerMessage("You see large entwined fungus roots on rotten lawn.");
         }

         sendVillageString(performer, tilex, tiley, true);
         sendTileTransformationState(performer, tilex, tiley, tileType);
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
                  } catch (NoSuchVillageException var20) {
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

            if (Tiles.isGrassType(tileType)) {
               int tileData = Tiles.decodeData(tile);
               comm.sendNormalServerMessage("Type: " + Tiles.decodeType(tile) + " data=" + tileData);
               comm.sendNormalServerMessage(
                  "Grass is at: "
                     + GrassData.GrowthStage.decodeTileData(tileData).toString().toLowerCase()
                     + " "
                     + tileType
                     + ", flowers: "
                     + GrassData.FlowerType.decodeTileData(tileData).toString().toLowerCase()
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
               } catch (NoSuchVillageException var19) {
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
      } else if (action == 645) {
         performer.getCommunicator().sendNormalServerMessage("You need a tool to cut the grass.");
         done = true;
      } else {
         done = super.action(act, performer, tilex, tiley, onSurface, tile, action, counter);
      }

      return done;
   }

   private boolean cutGrass(Action act, Creature performer, Item source, int tilex, int tiley, int tile, short action, float counter) {
      float maxQLFromUsedTool = 5.0F;
      short yield = 2;
      int time = 0;
      Skill gardening = null;
      Skill toolskill = null;
      Item toolUsed = null;
      boolean toReturn = false;
      byte tileType = Tiles.decodeType(tile);
      byte tileData = Tiles.decodeData(tile);
      Tiles.Tile theTile = Tiles.getTile(tileType);
      String tileName = theTile.getName().toLowerCase();
      GrassData.GrowthStage growthStage = GrassData.GrowthStage.decodeTileData(tileData);
      GrassData.FlowerType flowerType = GrassData.FlowerType.decodeTileData(tileData);
      if (tileType == Tiles.Tile.TILE_MYCELIUM.id) {
         performer.getCommunicator().sendNormalServerMessage("You can see there is nothing to gather here.");
         byte data = GrassData.encodeGrassTileData(GrassData.GrowthStage.SHORT, flowerType);
         Server.surfaceMesh.setTile(tilex, tiley, Tiles.encode(Tiles.decodeHeight(tile), tileType, data));
         Players.getInstance().sendChangedTile(tilex, tiley, true, false);
         return true;
      } else {
         try {
            if (tileType == Tiles.Tile.TILE_KELP.id || tileType == Tiles.Tile.TILE_REED.id) {
               boolean ok = false;
               float pht = performer.getPositionZ() + performer.getAltOffZ();
               if (pht < this.getSurfaceHeight(tilex, tiley) + 2.0F) {
                  ok = true;
               } else if (pht < this.getSurfaceHeight(tilex + 1, tiley) + 2.0F) {
                  ok = true;
               } else if (pht < this.getSurfaceHeight(tilex + 1, tiley + 1) + 2.0F) {
                  ok = true;
               } else if (pht < this.getSurfaceHeight(tilex, tiley + 1) + 2.0F) {
                  ok = true;
               }

               if (!ok) {
                  performer.getCommunicator().sendNormalServerMessage("This " + tileName + " is growing out of your reach.");
                  return true;
               }
            }

            float tilexpos = (float)((tilex << 2) + 2);
            float tileypos = (float)((tiley << 2) + 2);
            float tilezposNW = Zones.calculateHeight(tilexpos, tileypos, true);
            if (!performer.isWithinDistanceTo(tilexpos, tileypos, tilezposNW, 40.0F)) {
               performer.getCommunicator().sendNormalServerMessage("This " + tileName + " is growing out of your reach.");
               return true;
            }
         } catch (NoSuchZoneException var36) {
            logger.log(
               Level.WARNING, " No such zone exception at " + tilex + "," + tiley + " when player tried to TileGrassBehaviour.cutGrass()", (Throwable)var36
            );
         }

         if (source == null) {
            performer.getCommunicator().sendNormalServerMessage("You need a tool to cut the " + tileName + ".");
            return true;
         } else {
            if (source.getTemplateId() == 267 || source.getTemplateId() == 268 || source.getTemplateId() == 176) {
               maxQLFromUsedTool = 100.0F;
            } else if (source.getTemplate().isSharp()) {
               maxQLFromUsedTool = 20.0F;
            } else {
               if (source.getTemplateId() != 14) {
                  performer.getCommunicator().sendNormalServerMessage("You can't cut " + tileName + " with " + source.getNameWithGenus() + ".");
                  return true;
               }

               maxQLFromUsedTool = 5.0F;
            }

            toolUsed = source;
            yield = GrassData.GrowthStage.getYield(growthStage);
            if (yield == 0) {
               performer.getCommunicator()
                  .sendNormalServerMessage(
                     "You try to cut some " + growthStage.toString().toLowerCase() + " " + tileName + " but you fail to get any significant amount."
                  );
               return true;
            } else {
               if (counter == 1.0F) {
                  double toolBonus = 0.0;

                  try {
                     int weight = 0;
                     if (tileType == Tiles.Tile.TILE_GRASS.id) {
                        weight = ItemTemplateFactory.getInstance().getTemplate(620).getWeightGrams() * yield;
                     } else if (tileType == Tiles.Tile.TILE_REED.id) {
                        weight = ItemTemplateFactory.getInstance().getTemplate(743).getWeightGrams() * yield;
                     } else if (tileType == Tiles.Tile.TILE_KELP.id) {
                        weight = ItemTemplateFactory.getInstance().getTemplate(755).getWeightGrams() * yield;
                     }

                     if (performer.getInventory().getNumItemsNotCoins() + 1 >= 100) {
                        performer.getCommunicator().sendNormalServerMessage("You would not be able to carry the grass. You need to drop something first.");
                        return true;
                     }

                     if (!performer.canCarry(weight)) {
                        performer.getCommunicator().sendNormalServerMessage("You would not be able to carry the grass. You need to drop some things first.");
                        return true;
                     }
                  } catch (NoSuchTemplateException var35) {
                     logger.log(Level.WARNING, var35.getLocalizedMessage(), (Throwable)var35);
                     return true;
                  }

                  try {
                     gardening = performer.getSkills().getSkill(10045);
                  } catch (NoSuchSkillException var31) {
                     gardening = performer.getSkills().learn(10045, 1.0F);
                  }

                  try {
                     toolskill = performer.getSkills().getSkill(source.getTemplateId());
                     toolBonus = toolskill.getKnowledge(0.0);
                  } catch (NoSuchSkillException var30) {
                  }

                  time = Actions.getStandardActionTime(performer, gardening, source, toolBonus);
                  performer.getCommunicator().sendNormalServerMessage("You start to gather " + growthStage.toString().toLowerCase() + " " + tileName + ".");
                  Server.getInstance().broadCastAction(performer.getName() + " starts to gather " + tileName + ".", performer, 5);
                  performer.sendActionControl("gathering " + tileName, true, time);
                  act.setTimeLeft(time);
                  toReturn = false;
               } else {
                  time = act.getTimeLeft();
               }

               if (act.mayPlaySound()) {
                  Methods.sendSound(performer, "sound.work.foragebotanize");
               }

               if (counter * 10.0F >= (float)time) {
                  try {
                     int weight = ItemTemplateFactory.getInstance().getTemplate(620).getWeightGrams() * yield;
                     if (!performer.canCarry(weight)) {
                        performer.getCommunicator()
                           .sendNormalServerMessage("You would not be able to carry the " + tileName + ". You need to drop some things first.");
                        return true;
                     }
                  } catch (NoSuchTemplateException var34) {
                     logger.log(Level.WARNING, var34.getLocalizedMessage(), (Throwable)var34);
                     return true;
                  }

                  source.setDamage(source.getDamage() + 0.003F * source.getDamageModifier());
                  double toolBonus = 0.0;
                  double power = 0.0;

                  try {
                     gardening = performer.getSkills().getSkill(10045);
                  } catch (NoSuchSkillException var29) {
                     gardening = performer.getSkills().learn(10045, 1.0F);
                  }

                  try {
                     toolskill = performer.getSkills().getSkill(source.getTemplateId());
                     toolBonus = Math.max(1.0, toolskill.skillCheck(1.0, toolUsed, 0.0, false, counter));
                  } catch (NoSuchSkillException var28) {
                  }

                  power = gardening.skillCheck(1.0, source, toolBonus, false, counter);
                  if (source.getSpellEffects() != null) {
                     power *= (double)source.getSpellEffects().getRuneEffect(RuneUtilities.ModifierEffect.ENCH_RESGATHERED);
                  }

                  power += (double)source.getRarity();

                  try {
                     Item yieldItem = null;

                     for(int i = 0; i < yield; ++i) {
                        maxQLFromUsedTool = Math.min(maxQLFromUsedTool, (float)Math.min(100.0, power));
                        if (tileType == Tiles.Tile.TILE_GRASS.id) {
                           yieldItem = ItemFactory.createItem(620, Math.max(1.0F, maxQLFromUsedTool), null);
                        } else if (tileType == Tiles.Tile.TILE_REED.id) {
                           yieldItem = ItemFactory.createItem(743, Math.max(1.0F, maxQLFromUsedTool), null);
                        } else if (tileType == Tiles.Tile.TILE_KELP.id) {
                           yieldItem = ItemFactory.createItem(755, Math.max(1.0F, maxQLFromUsedTool), null);
                        }

                        if (power < 0.0) {
                           yieldItem.setDamage((float)(-power) / 2.0F);
                        }

                        performer.getInventory().insertItem(yieldItem);
                     }

                     byte data = GrassData.encodeGrassTileData(GrassData.GrowthStage.SHORT, flowerType);
                     Server.surfaceMesh.setTile(tilex, tiley, Tiles.encode(Tiles.decodeHeight(tile), tileType, data));
                     Players.getInstance().sendChangedTile(tilex, tiley, true, false);
                     performer.getCommunicator().sendNormalServerMessage("You gather " + yield + " " + yieldItem.getName() + ".");
                     Server.getInstance().broadCastAction(performer.getName() + " gathers some " + yieldItem.getName() + ".", performer, 5);
                  } catch (NoSuchTemplateException var32) {
                     logger.log(Level.WARNING, "No template for grass type item!", (Throwable)var32);
                     performer.getCommunicator()
                        .sendNormalServerMessage("You fail to gather the grass. Your sensitive mind notices a wrongness in the fabric of space.");
                  } catch (FailedException var33) {
                     logger.log(Level.WARNING, var33.getMessage(), (Throwable)var33);
                     performer.getCommunicator()
                        .sendNormalServerMessage("You fail to gather the grass. Your sensitive mind notices a wrongness in the fabric of space.");
                  }

                  toReturn = true;
               }

               return toReturn;
            }
         }
      }
   }

   private float getSurfaceHeight(int tilex, int tiley) {
      int tileNW = Server.surfaceMesh.getTile(tilex, tiley);
      return Tiles.decodeHeightAsFloat(tileNW);
   }

   private boolean makeLawn(Action act, Creature performer, Item source, int tilex, int tiley, int tile, short action, float counter) {
      byte tileType = Tiles.decodeType(tile);
      String grass = "grass";
      if (tileType == Tiles.Tile.TILE_MYCELIUM.id) {
         grass = "mycelium";
      }

      int time = 0;
      Skill gardening = null;
      Skill toolskill = null;
      Item toolUsed = null;
      boolean toReturn = Terraforming.cannotMakeLawn(performer, tilex, tiley);
      if (toReturn) {
         return toReturn;
      } else {
         try {
            float tilexpos = (float)((tilex << 2) + 1);
            float tileypos = (float)((tiley << 2) + 1);
            float tilezpos = Zones.calculateHeight(tilexpos, tileypos, true);
            if (!performer.isWithinDistanceTo(tilexpos, tileypos, tilezpos, 20.0F)) {
               performer.getCommunicator().sendNormalServerMessage("This " + grass + " is growing out of your reach.");
               return true;
            }
         } catch (NoSuchZoneException var21) {
            logger.log(
               Level.WARNING, " No such zone exception at " + tilex + "," + tiley + " when player tried to TileGrassBehaviour.makeLawn()", (Throwable)var21
            );
         }

         if (source != null) {
            if (source.getTemplateId() != 394 && source.getTemplateId() != 176) {
               performer.getCommunicator().sendNormalServerMessage("You can't trim the " + grass + " with " + source.getNameWithGenus() + ".");
               return true;
            } else {
               toolUsed = source;
               if (counter == 1.0F) {
                  gardening = performer.getSkills().getSkillOrLearn(10045);
                  double toolBonus = 0.0;

                  try {
                     toolskill = performer.getSkills().getSkill(source.getTemplateId());
                     toolBonus = toolskill.getKnowledge(0.0);
                  } catch (NoSuchSkillException var20) {
                  }

                  time = Actions.getStandardActionTime(performer, gardening, source, toolBonus);
                  performer.getCommunicator().sendNormalServerMessage("You start to trim the " + grass + " to lawn length.");
                  Server.getInstance().broadCastAction(performer.getName() + " starts to trim the " + grass + ".", performer, 5);
                  performer.sendActionControl("trimming " + grass, true, time);
                  act.setTimeLeft(time);
                  toReturn = false;
               } else {
                  time = act.getTimeLeft();
               }

               if (act.mayPlaySound()) {
                  Methods.sendSound(performer, "sound.work.foragebotanize");
               }

               if (counter * 10.0F >= (float)time) {
                  source.setDamage(source.getDamage() + 0.003F * source.getDamageModifier());
                  double toolBonus = 0.0;
                  gardening = performer.getSkills().getSkillOrLearn(10045);

                  try {
                     toolskill = performer.getSkills().getSkill(source.getTemplateId());
                     toolBonus = Math.max(1.0, toolskill.skillCheck(1.0, toolUsed, 0.0, false, counter));
                  } catch (NoSuchSkillException var19) {
                  }

                  gardening.skillCheck(1.0, source, toolBonus, false, counter);
                  byte data = GrassData.encodeGrassTileData(GrassData.GrowthStage.SHORT, GrassData.FlowerType.NONE);
                  if (tileType == Tiles.Tile.TILE_MYCELIUM.id) {
                     Server.surfaceMesh.setTile(tilex, tiley, Tiles.encode(Tiles.decodeHeight(tile), Tiles.Tile.TILE_MYCELIUM_LAWN.id, data));
                  } else {
                     Server.surfaceMesh.setTile(tilex, tiley, Tiles.encode(Tiles.decodeHeight(tile), Tiles.Tile.TILE_LAWN.id, data));
                  }

                  Players.getInstance().sendChangedTile(tilex, tiley, true, false);
                  performer.getCommunicator().sendNormalServerMessage("You trim the " + grass + " to look like a lawn.");
                  Server.getInstance()
                     .broadCastAction(performer.getName() + " looks pleased that the " + grass + " is trimmed and now looks like a lawn.", performer, 5);
                  toReturn = true;
               }

               return toReturn;
            }
         } else {
            performer.getCommunicator().sendNormalServerMessage("You need a tool to trim the " + grass + ".");
            return true;
         }
      }
   }

   @Override
   public boolean action(
      Action act, Creature performer, Item source, int tilex, int tiley, boolean onSurface, int heightOffset, int tile, short action, float counter
   ) {
      boolean done = true;
      byte tileType = Tiles.decodeType(tile);
      byte tileData = Tiles.decodeData(tile);
      if (action == 1) {
         done = this.action(act, performer, tilex, tiley, onSurface, tile, action, counter);
      } else if (action == 186) {
         if (source.isNaturePlantable()) {
            done = Terraforming.plantFlower(performer, source, tilex, tiley, onSurface, tile, counter);
         } else {
            done = Terraforming.plantSprout(performer, source, tilex, tiley, onSurface, tile, counter, false);
         }
      } else if (action == 660) {
         done = Terraforming.plantSprout(performer, source, tilex, tiley, onSurface, tile, counter, true);
      } else if (action == 187) {
         if (source.getTemplateId() == 267 || source.getTemplateId() == 176) {
            done = Terraforming.pickFlower(performer, source, tilex, tiley, tile, counter, act);
         }
      } else if (action == 644) {
         if (Tiles.decodeType(tile) == Tiles.Tile.TILE_MYCELIUM.id) {
            if (source.getTemplateId() == 394 || source.getTemplateId() == 176 && performer.getPower() >= 2) {
               done = this.makeLawn(act, performer, source, tilex, tiley, tile, action, counter);
            }
         } else {
            if (Tiles.decodeType(tile) != Tiles.Tile.TILE_GRASS.id) {
               return true;
            }

            if (GrassData.GrowthStage.decodeTileData(Tiles.decodeData(tile)) == GrassData.GrowthStage.SHORT
               && (source.getTemplateId() == 394 || source.getTemplateId() == 176 && performer.getPower() >= 2)) {
               done = this.makeLawn(act, performer, source, tilex, tiley, tile, action, counter);
            }
         }
      } else if (action != 645
         || Tiles.decodeType(tile) != Tiles.Tile.TILE_GRASS.id
            && Tiles.decodeType(tile) != Tiles.Tile.TILE_KELP.id
            && Tiles.decodeType(tile) != Tiles.Tile.TILE_REED.id) {
         if (action != 188 || source.getTemplateId() != 315 && source.getTemplateId() != 176) {
            if (action == 118 && source.getTemplateId() == 526) {
               performer.getCommunicator().sendNormalServerMessage("You draw a circle in the air in front of you with " + source.getNameWithGenus() + ".");
               Server.getInstance()
                  .broadCastAction(
                     performer.getName()
                        + " draws a circle in the air in front of "
                        + performer.getHimHerItString()
                        + " with "
                        + source.getNameWithGenus()
                        + ".",
                     performer,
                     5
                  );
               done = true;
               if (source.getAuxData() > 0) {
                  if (performer.getKingdomTemplateId() == 3) {
                     Server.setSurfaceTile(tilex, tiley, Tiles.decodeHeight(tile), Tiles.Tile.TILE_MYCELIUM.id, (byte)0);
                  } else if (Server.rand.nextInt(2) == 0) {
                     Server.setSurfaceTile(tilex, tiley, Tiles.decodeHeight(tile), Tiles.Tile.TILE_GRASS.id, (byte)(Server.rand.nextInt(7) + 1));
                  } else {
                     Server.setSurfaceTile(
                        tilex,
                        tiley,
                        Tiles.decodeHeight(tile),
                        Tiles.Tile.TILE_TREE.id,
                        (byte)(Server.rand.nextInt(16) & 15 + (Server.rand.nextInt(13) << 4) & 0xFF)
                     );
                  }

                  Players.getInstance().sendChangedTile(tilex, tiley, onSurface, false);
                  source.setAuxData((byte)(source.getAuxData() - 1));
               } else {
                  performer.getCommunicator().sendNormalServerMessage("Nothing happens.");
               }
            } else if (action == 118 && source.getTemplateId() == 176 && performer.getPower() >= 2) {
               Terraforming.rampantGrowth(performer, tilex, tiley);
            } else if (action == 148 && canCollectSnow(performer, tilex, tiley, tileType, tileData) && source.getTemplateId() == 204) {
               VolaTile t = Zones.getTileOrNull(tilex, tiley, onSurface);
               if (t != null && t.getItems().length > 0) {
                  performer.getCommunicator().sendNormalServerMessage("Remove all obstructing items first.");
                  return true;
               }

               done = false;
               if (counter == 1.0F) {
                  performer.getCommunicator().sendNormalServerMessage("You start making a snowman.");
                  Server.getInstance().broadCastAction(performer.getName() + " starts making a snowman.", performer, 5);
                  performer.sendActionControl("creating", true, 600);
               } else if (act.currentSecond() == 10) {
                  performer.getCommunicator().sendNormalServerMessage("You have now rolled the large bottom ball.");
                  Server.getInstance().broadCastAction(performer.getName() + " has now rolled the large bottom ball.", performer, 5);
               } else if (act.currentSecond() == 20) {
                  performer.getCommunicator().sendNormalServerMessage("You have now rolled a smaller ball to use as the chest.");
                  Server.getInstance().broadCastAction(performer.getName() + " has now rolled a smaller ball to use as chest.", performer, 5);
               } else if (act.currentSecond() == 30) {
                  performer.getCommunicator().sendNormalServerMessage("You have now created the head ball.");
                  Server.getInstance().broadCastAction(performer.getName() + " has now created the head that goes on top.", performer, 5);
               } else if (act.currentSecond() == 40) {
                  performer.getCommunicator().sendNormalServerMessage("You use a couple of twigs for arms.");
                  Server.getInstance().broadCastAction(performer.getName() + " uses a couple of twigs for arms.", performer, 5);
               } else if (act.currentSecond() == 50) {
                  performer.getCommunicator().sendNormalServerMessage("You start to assemble the snowman.");
                  Server.getInstance().broadCastAction(performer.getName() + " starts to assemble the snowman.", performer, 5);
               } else if (act.currentSecond() == 60) {
                  if (act.getRarity() != 0) {
                     performer.playPersonalSound("sound.fx.drumroll");
                  }

                  done = true;
                  Server.setGatherable(tilex, tiley, false);
                  performer.getCommunicator().sendNormalServerMessage("As a final touch you put the charcoal as eyes and the snowman comes to life!");
                  Server.getInstance().broadCastAction(performer.getName() + " uses charcoal as eyes and finishes the snowman.", performer, 5);

                  try {
                     ItemFactory.createItem(
                        655,
                        90.0F,
                        (float)((tilex << 2) + 2),
                        (float)((tiley << 2) + 2),
                        performer.getStatus().getRotation(),
                        true,
                        act.getRarity(),
                        performer.getBridgeId(),
                        performer.getName()
                     );
                  } catch (FailedException var16) {
                  } catch (NoSuchTemplateException var17) {
                  }

                  Items.destroyItem(source.getWurmId());
               }
            } else {
               done = super.action(act, performer, source, tilex, tiley, onSurface, heightOffset, tile, action, counter);
            }
         } else {
            byte d = (byte)(tileData & 255);
            d = (byte)(d | GrassData.GrowthStage.WILD.getEncodedData() | GrassData.FlowerType.FLOWER_7.getEncodedData());
            Server.setSurfaceTile(tilex, tiley, Tiles.decodeHeight(tile), Tiles.Tile.TILE_GRASS.id, d);
            performer.getCommunicator()
               .sendNormalServerMessage(
                  "You create some "
                     + GrassData.GrowthStage.WILD.name().toLowerCase()
                     + " grass with some "
                     + GrassData.FlowerType.FLOWER_7.getDescription()
                     + "."
               );
            Players.getInstance().sendChangedTile(tilex, tiley, true, false);
         }
      } else {
         done = this.cutGrass(act, performer, source, tilex, tiley, tile, action, counter);
      }

      return done;
   }

   static {
      flowers.put(498, (byte)1);
      flowers.put(499, (byte)2);
      flowers.put(500, (byte)3);
      flowers.put(501, (byte)4);
      flowers.put(502, (byte)5);
      flowers.put(503, (byte)6);
      flowers.put(504, (byte)7);
   }
}

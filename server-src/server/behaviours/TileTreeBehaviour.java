package com.wurmonline.server.behaviours;

import com.wurmonline.mesh.BushData;
import com.wurmonline.mesh.FoliageAge;
import com.wurmonline.mesh.GrassData;
import com.wurmonline.mesh.Tiles;
import com.wurmonline.mesh.TreeData;
import com.wurmonline.server.FailedException;
import com.wurmonline.server.Players;
import com.wurmonline.server.PlonkData;
import com.wurmonline.server.Server;
import com.wurmonline.server.Servers;
import com.wurmonline.server.WurmHarvestables;
import com.wurmonline.server.creatures.Communicator;
import com.wurmonline.server.creatures.Creature;
import com.wurmonline.server.items.Item;
import com.wurmonline.server.items.ItemFactory;
import com.wurmonline.server.items.ItemTemplateFactory;
import com.wurmonline.server.items.NoSuchTemplateException;
import com.wurmonline.server.items.RuneUtilities;
import com.wurmonline.server.kingdom.Kingdoms;
import com.wurmonline.server.players.Player;
import com.wurmonline.server.skills.NoSuchSkillException;
import com.wurmonline.server.skills.Skill;
import com.wurmonline.server.villages.NoSuchVillageException;
import com.wurmonline.server.villages.Villages;
import com.wurmonline.server.zones.NoSuchZoneException;
import com.wurmonline.server.zones.Trap;
import com.wurmonline.server.zones.Zone;
import com.wurmonline.server.zones.Zones;
import java.util.LinkedList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.annotation.Nullable;

public class TileTreeBehaviour extends TileBehaviour {
   private static Logger logger = Logger.getLogger(TileTreeBehaviour.class.getName());
   private static final Logger cheatlogger = Logger.getLogger("Cheaters");

   public TileTreeBehaviour() {
      super((short)7);
   }

   public TileTreeBehaviour(short type) {
      super(type);
   }

   @Override
   public List<ActionEntry> getBehavioursFor(Creature performer, int tilex, int tiley, boolean onSurface, int tile) {
      PlonkData.TREE_ACTIONS.trigger(performer);
      List<ActionEntry> toReturn = new LinkedList<>();
      toReturn.addAll(super.getBehavioursFor(performer, tilex, tiley, onSurface, tile));
      byte type = Tiles.decodeType(tile);
      byte data = Tiles.decodeData(tile);
      Tiles.Tile theTile = Tiles.getTile(type);
      int age = data >> 4 & 15;
      if (theTile.isNormalTree() && performer.getDeity() != null && performer.getDeity().isForestGod() && isPrayingAge(age)) {
         toReturn.add(Actions.actionEntrys[141]);
      }

      List<ActionEntry> nature = this.getNatureActions(performer, null, tilex, tiley, theTile, data);
      toReturn.addAll(this.getNatureMenu(performer, tilex, tiley, type, data, nature));
      return toReturn;
   }

   @Override
   public List<ActionEntry> getBehavioursFor(Creature performer, Item subject, int tilex, int tiley, boolean onSurface, int tile) {
      PlonkData.TREE_ACTIONS.trigger(performer);
      List<ActionEntry> toReturn = new LinkedList<>();
      toReturn.addAll(super.getBehavioursFor(performer, subject, tilex, tiley, onSurface, tile));
      byte type = Tiles.decodeType(tile);
      byte data = Tiles.decodeData(tile);
      Tiles.Tile theTile = Tiles.getTile(type);
      int age = data >> 4 & 15;
      if (subject.isWeaponSlash() || subject.getTemplateId() == 24) {
         toReturn.add(Actions.actionEntrys[96]);
      } else if (subject.getTemplateId() == 526 && theTile.isNormalTree()) {
         toReturn.add(Actions.actionEntrys[118]);
      }

      if (theTile.isNormalTree() && performer.getDeity() != null && performer.getDeity().isForestGod() && isPrayingAge(age)) {
         toReturn.add(Actions.actionEntrys[141]);
      }

      List<ActionEntry> nature = this.getNatureActions(performer, subject, tilex, tiley, theTile, data);
      toReturn.addAll(this.getNatureMenu(performer, tilex, tiley, type, data, nature));
      return toReturn;
   }

   public List<ActionEntry> getNatureActions(Creature performer, @Nullable Item tool, int tilex, int tiley, Tiles.Tile theTile, byte data) {
      List<ActionEntry> toReturn = new LinkedList<>();
      int age = data >> 4 & 15;
      TreeData.TreeType treeType = theTile.getTreeType(data);
      if (tool != null) {
         if (tool.getTemplateId() == 267 && !theTile.isEnchanted()) {
            if (performer.isWithinTileDistanceTo(tilex, tiley, (int)(performer.getStatus().getPositionZ() + performer.getAltOffZ()) >> 2, 1)) {
               if (theTile != Tiles.Tile.TILE_BUSH_LINGONBERRY && isSproutingAge(age)) {
                  toReturn.add(Actions.actionEntrys[187]);
               }

               if (!theTile.isTree()) {
                  if (theTile.isBush()) {
                     if (hasFruit(performer, tilex, tiley, age)) {
                        toReturn.add(Actions.actionEntrys[152]);
                     } else if (Servers.isThisATestServer()) {
                        toReturn.add(Actions.actionEntrys[152]);
                     }

                     if (age == 3 || age == 4 || age == 13 || age == 14 || age == 15 && theTile.isThorn(data)) {
                        toReturn.add(Actions.actionEntrys[373]);
                     }
                  }
               } else {
                  if (hasFruit(performer, tilex, tiley, age) && treeType.isFruitTree()) {
                     toReturn.add(Actions.actionEntrys[152]);
                  } else if (treeType == TreeData.TreeType.CHESTNUT && hasFruit(performer, tilex, tiley, age)) {
                     toReturn.add(Actions.actionEntrys[152]);
                  } else if (treeType == TreeData.TreeType.WALNUT && hasFruit(performer, tilex, tiley, age)) {
                     toReturn.add(Actions.actionEntrys[152]);
                  } else if (treeType == TreeData.TreeType.PINE && hasFruit(performer, tilex, tiley, age)) {
                     toReturn.add(Actions.actionEntrys[152]);
                  } else if (treeType == TreeData.TreeType.OAK && hasFruit(performer, tilex, tiley, age)) {
                     toReturn.add(Actions.actionEntrys[152]);
                  }

                  if (age == 3 || age == 4 || age == 13 || age == 14) {
                     toReturn.add(Actions.actionEntrys[373]);
                  }
               }
            }
         } else if (tool.getTemplateId() == 421
            && theTile.isNormalTree()
            && !treeType.isFruitTree()
            && performer.isWithinTileDistanceTo(tilex, tiley, (int)(performer.getStatus().getPositionZ() + performer.getAltOffZ()) >> 2, 1)
            && theTile.isMaple(data)) {
            if (hasFruit(performer, tilex, tiley, age)) {
               toReturn.add(Actions.actionEntrys[152]);
            } else if (Servers.isThisATestServer()) {
               toReturn.add(Actions.actionEntrys[152]);
            }
         }

         if (performer.getPower() >= 2 && tool.getTemplateId() == 176) {
            toReturn.add(Actions.actionEntrys[188]);
         }

         GrassData.GrowthTreeStage growthStage = GrassData.GrowthTreeStage.decodeTileData(data);
         if (!theTile.isEnchanted() && growthStage != GrassData.GrowthTreeStage.LAWN && theTile != Tiles.Tile.TILE_BUSH_LINGONBERRY) {
            if (theTile.isMycelium()) {
               if (tool.getTemplateId() == 394 || tool.getTemplateId() == 176 && performer.getPower() >= 2) {
                  toReturn.add(new ActionEntry((short)644, "Trim mycelium", "Trimming mycelium"));
               }
            } else if (growthStage == GrassData.GrowthTreeStage.SHORT) {
               if (tool.getTemplateId() == 394 || tool.getTemplateId() == 176 && performer.getPower() >= 2) {
                  toReturn.add(new ActionEntry((short)644, "Trim grass", "Trimming grass"));
               }
            } else if (tool.getTemplate().isSharp()) {
               toReturn.add(new ActionEntry((short)645, "Cut grass", "Cutting grass"));
            }
         }

         if (theTile.isTree() && age == 15 && tool.getTemplateId() == 390) {
            toReturn.add(new ActionEntry((short)935, "Search for grubs", "searching"));
         }
      }

      if ((theTile.isNormalTree() || theTile.isNormalBush()) && performer.getCultist() != null && performer.getCultist().mayEnchantNature()) {
         toReturn.add(Actions.actionEntrys[388]);
      }

      boolean canGrub = Server.hasGrubs(tilex, tiley);
      if (theTile.isBush() && age == 14 && canGrub) {
         toReturn.add(new ActionEntry((short)935, "Search for twigs", "searching"));
      }

      if (theTile.isTree() && treeType.getTypeId() == TreeData.TreeType.BIRCH.getTypeId() && age == 14 && canGrub) {
         toReturn.add(new ActionEntry((short)935, "Search for loose bark", "searching"));
      }

      if (theTile.canBearFruit() && hasFruit(performer, tilex, tiley, age)) {
         toReturn.add(new ActionEntry((short)852, "Study", "making notes"));
      }

      return toReturn;
   }

   static boolean isPrayingAge(int age) {
      return age > FoliageAge.VERY_OLD_SPROUTING.getAgeId();
   }

   static boolean isSproutingAge(int age) {
      return age == FoliageAge.MATURE_SPROUTING.getAgeId()
         || age == FoliageAge.OLD_ONE_SPROUTING.getAgeId()
         || age == FoliageAge.OLD_TWO_SPROUTING.getAgeId()
         || age == FoliageAge.VERY_OLD_SPROUTING.getAgeId();
   }

   @Override
   public boolean action(Action act, Creature performer, int tilex, int tiley, boolean onSurface, int tile, short action, float counter) {
      boolean done = true;
      byte tiletype = Tiles.decodeType(tile);
      Tiles.Tile theTile = Tiles.getTile(tiletype);
      byte data = Tiles.decodeData(tile);
      int age = data >> 4 & 15;
      if (action == 1) {
         Communicator comm = performer.getCommunicator();
         String ageString = "";
         if (theTile.isMycelium()) {
            ageString = "an infected ";
         } else if (theTile.isEnchanted()) {
            ageString = "an enchanted ";
         } else if (age >= 8 && (age < 12 || age >= 14)) {
            ageString = "an ";
         } else {
            ageString = "a ";
         }

         if (age < 4) {
            ageString = ageString + "young";
         } else if (age < 8) {
            ageString = ageString + "mature";
         } else if (age < 12) {
            ageString = ageString + "old";
         } else if (age < 14) {
            ageString = ageString + "very old";
         } else if (age == 14) {
            ageString = ageString + "overaged";
         } else if (age == 15) {
            ageString = ageString + "old and shriveled";
         }

         if (performer.getPower() > 3) {
            ageString = ageString + " (" + age + ")";
         }

         int dam = Server.getWorldResource(tilex, tiley);
         if (dam == 65535) {
            Server.setWorldResource(tilex, tiley, 0);
            dam = 0;
         }

         String damage = "";
         if (dam > 0) {
            damage = " Damage=" + dam + ".";
         }

         String growthState = "foliage";
         String name = "unknown";
         if (theTile.isBush()) {
            BushData.BushType bushType = theTile.getBushType(data);
            name = bushType.getName();
            growthState = getGrowthState(performer, tilex, tiley, age, bushType, theTile.isMycelium());
         } else {
            TreeData.TreeType treeType = theTile.getTreeType(data);
            name = treeType.getName();
            growthState = getGrowthState(performer, tilex, tiley, age, treeType, theTile.isMycelium());
         }

         comm.sendNormalServerMessage("You see " + ageString + " " + name + "." + growthState + damage);
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
                  } catch (NoSuchVillageException var27) {
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
               } catch (NoSuchVillageException var26) {
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
      } else if (action == 141 && theTile.isNormal() && theTile.isTree() && performer.getDeity() != null && performer.getDeity().isForestGod()) {
         if (isPrayingAge(age)) {
            done = MethodsReligion.pray(act, performer, counter);
         } else {
            done = true;
         }
      } else if (action == 852 && theTile.canBearFruit() && hasFruit(performer, tilex, tiley, age)) {
         done = this.study(act, performer, tilex, tiley, tile, action, counter);
      } else if (action == 935 && theTile.isTree() && theTile.getTreeType(data).getTypeId() == TreeData.TreeType.BIRCH.getTypeId() && age == 14) {
         done = Terraforming.pickBark(act, performer, tilex, tiley, tile, theTile, counter);
      } else if (action == 935 && theTile.isBush() && age == 14) {
         done = Terraforming.findTwigs(act, performer, tilex, tiley, tile, theTile, counter);
      } else {
         done = super.action(act, performer, tilex, tiley, onSurface, tile, action, counter);
      }

      return done;
   }

   public static final String getTreenameForMaterial(byte material) {
      String treeString = "tree";
      switch(material) {
         case 14:
            treeString = "birch";
            break;
         case 15:
         case 16:
         case 17:
         case 18:
         case 19:
         case 20:
         case 21:
         case 22:
         case 23:
         case 24:
         case 25:
         case 26:
         case 27:
         case 28:
         case 29:
         case 30:
         case 31:
         case 32:
         case 33:
         case 34:
         case 35:
         case 36:
         case 52:
         case 53:
         case 54:
         case 55:
         case 56:
         case 57:
         case 58:
         case 59:
         case 60:
         case 61:
         case 62:
         case 67:
         case 68:
         case 69:
         case 70:
         case 72:
         case 73:
         case 74:
         case 75:
         case 76:
         case 77:
         case 78:
         case 79:
         case 80:
         case 81:
         case 82:
         case 83:
         case 84:
         case 85:
         case 86:
         case 87:
         case 89:
         default:
            treeString = "tree";
            break;
         case 37:
            treeString = "pine";
            break;
         case 38:
            treeString = "oak";
            break;
         case 39:
            treeString = "cedar";
            break;
         case 40:
            treeString = "willow";
            break;
         case 41:
            treeString = "maple";
            break;
         case 42:
            treeString = "appletree";
            break;
         case 43:
            treeString = "lemontree";
            break;
         case 44:
            treeString = "olivetree";
            break;
         case 45:
            treeString = "cherrytree";
            break;
         case 46:
            treeString = "lavenderbush";
            break;
         case 47:
            treeString = "rosebush";
            break;
         case 48:
            treeString = "thornbush";
            break;
         case 49:
            treeString = "grapebush";
            break;
         case 50:
            treeString = "camelliabush";
            break;
         case 51:
            treeString = "oleanderbush";
            break;
         case 63:
            treeString = "chestnut";
            break;
         case 64:
            treeString = "walnut";
            break;
         case 65:
            treeString = "fir";
            break;
         case 66:
            treeString = "linden";
            break;
         case 71:
            treeString = "hazelbush";
            break;
         case 88:
            treeString = "orangetree";
            break;
         case 90:
            treeString = "raspberrybush";
            break;
         case 91:
            treeString = "blueberrybush";
            break;
         case 92:
            treeString = "lingonberrybush";
      }

      return treeString;
   }

   @Override
   public boolean action(
      Action act, Creature performer, Item source, int tilex, int tiley, boolean onSurface, int heightOffset, int tile, short action, float counter
   ) {
      boolean done = true;
      byte tiletype = Tiles.decodeType(tile);
      byte data = Tiles.decodeData(tile);
      int age = data >> 4 & 15;
      Tiles.Tile theTile = Tiles.getTile(tiletype);
      GrassData.GrowthTreeStage growth = GrassData.GrowthTreeStage.decodeTileData(Tiles.decodeData(tile));
      if ((source.isWeaponSlash() || source.getTemplateId() == 24) && action == 96) {
         done = Terraforming.handleChopAction(act, performer, source, tilex, tiley, onSurface, heightOffset, tile, action, counter);
      } else if (action == 1 || action == 34) {
         done = this.action(act, performer, tilex, tiley, onSurface, tile, action, counter);
      } else if (!theTile.isEnchanted() && theTile != Tiles.Tile.TILE_BUSH_LINGONBERRY && action == 187 && isSproutingAge(age)) {
         done = Terraforming.pickSprout(performer, source, tilex, tiley, tile, theTile, counter, act);
      } else if (action == 188 && performer.getPower() >= 2 && source.getTemplateId() == 176) {
         int type = data & 15;
         int newAge = age + 1;
         int newData = (newAge << 4) + type & 0xFF;
         Server.surfaceMesh.setTile(tilex, tiley, Tiles.encode(Tiles.decodeHeight(tile), Tiles.decodeType(tile), (byte)newData));
         Players.getInstance().sendChangedTile(tilex, tiley, true, false);
      } else if (!theTile.isEnchanted() && action == 141) {
         done = this.action(act, performer, tilex, tiley, onSurface, tile, action, counter);
      } else if (!theTile.isEnchanted() && action == 152) {
         if (theTile.isNormalTree()) {
            done = Terraforming.harvestTree(act, performer, source, tilex, tiley, tile, theTile, counter);
         } else if (theTile.isNormalBush()) {
            done = Terraforming.harvestBush(act, performer, source, tilex, tiley, tile, theTile, counter);
         } else {
            done = true;
         }
      } else if (action == 373 && !theTile.isEnchanted()) {
         if (!theTile.isTree() && !theTile.isBush()) {
            done = true;
         } else {
            done = Terraforming.prune(act, performer, source, tilex, tiley, tile, theTile, counter);
         }
      } else if (action == 935 && theTile.isTree() && source.getTemplateId() == 390 && age == 15) {
         done = Terraforming.pickGrubs(act, performer, source, tilex, tiley, tile, theTile, counter);
      } else if (action == 935 && theTile.isTree() && theTile.getTreeType(data).getTypeId() == TreeData.TreeType.BIRCH.getTypeId() && age == 14) {
         done = Terraforming.pickBark(act, performer, tilex, tiley, tile, theTile, counter);
      } else if (action == 935 && theTile.isBush() && age == 14) {
         done = Terraforming.findTwigs(act, performer, tilex, tiley, tile, theTile, counter);
      } else if (action == 118 && source.getTemplateId() == 526 && !theTile.isEnchanted()) {
         performer.getCommunicator().sendNormalServerMessage("You draw a circle in the air in front of you with " + source.getNameWithGenus() + ".");
         Server.getInstance()
            .broadCastAction(
               performer.getName() + " draws a circle in the air in front of " + performer.getHimHerItString() + " with " + source.getNameWithGenus() + ".",
               performer,
               5
            );
         done = true;
         byte cdata = (byte)(Tiles.decodeData(tile) & 15 + (FoliageAge.VERY_OLD_SPROUTING.getAgeId() << 4) & 0xFF);
         byte newTreeType = 0;
         if (theTile.isNormal() && performer.getKingdomTemplateId() == 3) {
            if (theTile.isTree()) {
               TreeData.TreeType ttype = theTile.getTreeType(cdata);
               newTreeType = ttype.asMyceliumTree();
            } else {
               BushData.BushType btype = theTile.getBushType(cdata);
               newTreeType = btype.asMyceliumBush();
            }
         } else if (theTile.isMycelium() && performer.getKingdomTemplateId() != 3) {
            if (theTile.isTree()) {
               TreeData.TreeType ttype = theTile.getTreeType(cdata);
               newTreeType = ttype.asNormalTree();
            } else {
               BushData.BushType btype = theTile.getBushType(cdata);
               newTreeType = btype.asNormalBush();
            }
         }

         if (newTreeType != 0 && source.getAuxData() > 0) {
            Server.surfaceMesh.setTile(tilex, tiley, Tiles.encode(Tiles.decodeHeight(tile), newTreeType, cdata));
            Players.getInstance().sendChangedTile(tilex, tiley, onSurface, true);
            source.setAuxData((byte)(source.getAuxData() - 1));

            try {
               Zone z = Zones.getZone(tilex, tiley, true);
               z.changeTile(tilex, tiley);
            } catch (NoSuchZoneException var20) {
            }
         } else {
            performer.getCommunicator().sendNormalServerMessage("Nothing happens.");
         }
      } else if (action == 852 && theTile.canBearFruit() && hasFruit(performer, tilex, tiley, age)) {
         done = this.study(act, performer, tilex, tiley, tile, action, counter);
      } else if (action == 644) {
         if ((growth == GrassData.GrowthTreeStage.SHORT || theTile.isMycelium())
            && (source.getTemplateId() == 394 || source.getTemplateId() == 176 && performer.getPower() >= 2)) {
            done = this.makeLawn(act, performer, source, tilex, tiley, tile, action, counter);
         }
      } else if (action == 645 && theTile.isNormal()) {
         if (growth != GrassData.GrowthTreeStage.LAWN
            && growth != GrassData.GrowthTreeStage.SHORT
            && (source.getTemplate().isSharp() || source.getTemplateId() == 176 && performer.getPower() >= 2)) {
            done = this.cutGrass(act, performer, source, tilex, tiley, tile, action, counter);
         }
      } else if (act.isQuick()) {
         done = super.action(act, performer, tilex, tiley, onSurface, tile, action, counter);
      } else {
         done = super.action(act, performer, source, tilex, tiley, onSurface, heightOffset, tile, action, counter);
      }

      return done;
   }

   static boolean hasFruit(Creature performer, int tilex, int tiley, int age) {
      int encodedTile = Server.surfaceMesh.getTile(tilex, tiley);
      byte data = Tiles.decodeData(encodedTile);
      if (age > FoliageAge.YOUNG_FOUR.getAgeId() && age < FoliageAge.OVERAGED.getAgeId()) {
         return Servers.isThisATestServer() && performer.getPower() > 1 ? true : TreeData.hasFruit(data);
      } else {
         return false;
      }
   }

   static final boolean isAlmostRipe(int age, BushData.BushType type) {
      if (age > FoliageAge.YOUNG_FOUR.getAgeId() && age < FoliageAge.OVERAGED.getAgeId()) {
         switch(type) {
            case LAVENDER:
               return WurmHarvestables.Harvestable.LAVENDER.isAlmostRipe();
            case ROSE:
               return WurmHarvestables.Harvestable.ROSE.isAlmostRipe();
            case GRAPE:
               return WurmHarvestables.Harvestable.GRAPE.isAlmostRipe();
            case CAMELLIA:
               return WurmHarvestables.Harvestable.CAMELLIA.isAlmostRipe();
            case OLEANDER:
               return WurmHarvestables.Harvestable.OLEANDER.isAlmostRipe();
            case HAZELNUT:
               return WurmHarvestables.Harvestable.HAZEL.isAlmostRipe();
            case RASPBERRY:
               return WurmHarvestables.Harvestable.RASPBERRY.isAlmostRipe();
            case BLUEBERRY:
               return WurmHarvestables.Harvestable.BLUEBERRY.isAlmostRipe();
            case LINGONBERRY:
               return WurmHarvestables.Harvestable.LINGONBERRY.isAlmostRipe();
            default:
               return false;
         }
      } else {
         return false;
      }
   }

   static final boolean isAlmostRipe(int age, TreeData.TreeType type) {
      if (age > FoliageAge.YOUNG_FOUR.getAgeId() && age < FoliageAge.OVERAGED.getAgeId()) {
         switch(type) {
            case MAPLE:
               return WurmHarvestables.Harvestable.MAPLE.isAlmostRipe();
            case APPLE:
               return WurmHarvestables.Harvestable.APPLE.isAlmostRipe();
            case LEMON:
               return WurmHarvestables.Harvestable.LEMON.isAlmostRipe();
            case OLIVE:
               return WurmHarvestables.Harvestable.OLIVE.isAlmostRipe();
            case CHERRY:
               return WurmHarvestables.Harvestable.CHERRY.isAlmostRipe();
            case CHESTNUT:
               return WurmHarvestables.Harvestable.CHESTNUT.isAlmostRipe();
            case WALNUT:
               return WurmHarvestables.Harvestable.WALNUT.isAlmostRipe();
            case PINE:
               return WurmHarvestables.Harvestable.PINE.isAlmostRipe();
            case OAK:
               return WurmHarvestables.Harvestable.OAK.isAlmostRipe();
            case ORANGE:
               return WurmHarvestables.Harvestable.ORANGE.isAlmostRipe();
            default:
               return false;
         }
      } else {
         return false;
      }
   }

   static final int getItem(int tilex, int tiley, int age, BushData.BushType type) {
      if (age > FoliageAge.YOUNG_FOUR.getAgeId() && age < FoliageAge.OVERAGED.getAgeId()) {
         switch(type) {
            case LAVENDER:
               return 424;
            case ROSE:
               return 426;
            case GRAPE:
               if (tiley > Zones.worldTileSizeY / 2) {
                  return 411;
               }

               return 414;
            case CAMELLIA:
               return 422;
            case OLEANDER:
               return 423;
            case HAZELNUT:
               return 134;
            case RASPBERRY:
               return 1196;
            case BLUEBERRY:
               return 364;
            case LINGONBERRY:
               return 367;
            default:
               return -10;
         }
      } else {
         return -10;
      }
   }

   static final int getItem(int tilex, int tiley, int age, TreeData.TreeType type) {
      if (age > FoliageAge.YOUNG_FOUR.getAgeId() && age < FoliageAge.OVERAGED.getAgeId()) {
         switch(type) {
            case MAPLE:
               return 416;
            case APPLE:
               return 6;
            case LEMON:
               return 410;
            case OLIVE:
               return 412;
            case CHERRY:
               return 409;
            case CHESTNUT:
               return 833;
            case WALNUT:
               return 832;
            case PINE:
               return 1184;
            case OAK:
               return 436;
            case ORANGE:
               return 1283;
            default:
               return -10;
         }
      } else {
         return -10;
      }
   }

   private static final String getGrowthState(Creature performer, int tilex, int tiley, int age, TreeData.TreeType treeType, boolean infected) {
      String toReturn = "";
      if (age > FoliageAge.YOUNG_FOUR.getAgeId() && age < FoliageAge.OVERAGED.getAgeId()) {
         switch(treeType) {
            case MAPLE:
               if (hasFruit(performer, tilex, tiley, age)) {
                  if (infected) {
                     toReturn = " The maple is infected and the sap is useless.";
                  } else {
                     toReturn = " The maple is brimming with sap.";
                  }
               } else if (isAlmostRipe(age, treeType)) {
                  if (infected) {
                     toReturn = " The maple is infected and the sap will be useless.";
                  } else {
                     toReturn = " The maple will start to produce sap soon.";
                  }
               } else if (hasBeenPicked(tilex, tiley)) {
                  toReturn = " The maple has no sap left.";
               }
               break;
            case APPLE:
               if (hasFruit(performer, tilex, tiley, age)) {
                  if (infected) {
                     toReturn = " No apples grew to mature state, and they have weird brown spots.";
                  } else {
                     toReturn = " The tree has some fine green apples.";
                  }
               } else if (isAlmostRipe(age, treeType)) {
                  if (infected) {
                     toReturn = " The tree will not produce any healthy apples.";
                  } else {
                     toReturn = " The apples will soon be ripe.";
                  }
               } else if (hasBeenPicked(tilex, tiley)) {
                  toReturn = " The tree has been picked clean of any apples.";
               }
               break;
            case LEMON:
               if (hasFruit(performer, tilex, tiley, age)) {
                  if (infected) {
                     toReturn = " No lemons grew to mature state, and they have weird brown spots.";
                  } else {
                     toReturn = " The tree has some fine yellow lemons.";
                  }
               } else if (isAlmostRipe(age, treeType)) {
                  if (infected) {
                     toReturn = " The tree will not produce any healthy lemons.";
                  } else {
                     toReturn = " The lemons will soon be ripe.";
                  }
               } else if (hasBeenPicked(tilex, tiley)) {
                  toReturn = " The tree has been picked clean of its lemons.";
               }
               break;
            case OLIVE:
               if (hasFruit(performer, tilex, tiley, age)) {
                  if (infected) {
                     toReturn = " No olives grew to mature state, and they have weird white spots.";
                  } else {
                     toReturn = " The tree has some fine black olives.";
                  }
               } else if (isAlmostRipe(age, treeType)) {
                  if (infected) {
                     toReturn = " The tree will not produce any healthy olives.";
                  } else {
                     toReturn = " The olives will soon be ripe.";
                  }
               } else if (hasBeenPicked(tilex, tiley)) {
                  toReturn = " The tree has been picked clean of any olives.";
               }
               break;
            case CHERRY:
               if (hasFruit(performer, tilex, tiley, age)) {
                  if (infected) {
                     toReturn = " No cherries grew to mature state, and they have weird brown spots.";
                  } else {
                     toReturn = " The tree has some juicy red cherries.";
                  }
               } else if (isAlmostRipe(age, treeType)) {
                  if (infected) {
                     toReturn = " The tree will not produce any healthy cherries.";
                  } else {
                     toReturn = " The cherries will soon be ripe.";
                  }
               } else if (hasBeenPicked(tilex, tiley)) {
                  toReturn = " The tree has been picked clean of any cherries.";
               }
               break;
            case CHESTNUT:
               if (hasFruit(performer, tilex, tiley, age)) {
                  if (infected) {
                     toReturn = " No chestnuts grew to mature state, and they have weird yellow spots.";
                  } else {
                     toReturn = " The tree has some interesting chestnuts.";
                  }
               } else if (isAlmostRipe(age, treeType)) {
                  if (infected) {
                     toReturn = " The tree will not produce any healthy chestnuts.";
                  } else {
                     toReturn = " The chestnuts will soon be ripe.";
                  }
               } else if (hasBeenPicked(tilex, tiley)) {
                  toReturn = " The tree has been picked clean of any chestnuts.";
               }
               break;
            case WALNUT:
               if (hasFruit(performer, tilex, tiley, age)) {
                  if (infected) {
                     toReturn = " No walnuts grew to mature state, and they have weird brown spots.";
                  } else {
                     toReturn = " The tree has some juicy walnuts.";
                  }
               } else if (isAlmostRipe(age, treeType)) {
                  if (infected) {
                     toReturn = " The tree will not produce any healthy walnuts.";
                  } else {
                     toReturn = " The walnuts will soon be ripe.";
                  }
               } else if (hasBeenPicked(tilex, tiley)) {
                  toReturn = " The tree has been picked clean of any walnuts.";
               }
               break;
            case PINE:
               if (hasFruit(performer, tilex, tiley, age)) {
                  if (infected) {
                     toReturn = " No pinenuts grew to mature state, and they have weird brown spots.";
                  } else {
                     toReturn = " The tree has some fine pinenuts.";
                  }
               } else if (isAlmostRipe(age, treeType)) {
                  if (infected) {
                     toReturn = " The tree will not produce any healthy pinenuts.";
                  } else {
                     toReturn = " The pinenuts will soon be ready.";
                  }
               } else if (hasBeenPicked(tilex, tiley)) {
                  toReturn = " The tree has been picked clean of any pinenuts.";
               }
               break;
            case OAK:
               if (hasFruit(performer, tilex, tiley, age)) {
                  if (infected) {
                     toReturn = " No acorns grew to mature state, and they have weird brown spots.";
                  } else {
                     toReturn = " The tree has some fine acorns.";
                  }
               } else if (isAlmostRipe(age, treeType)) {
                  if (infected) {
                     toReturn = " The tree will not produce any healthy acorns.";
                  } else {
                     toReturn = " The acorns will soon be ready.";
                  }
               } else if (hasBeenPicked(tilex, tiley)) {
                  toReturn = " The tree has been picked clean of any acorns.";
               }
               break;
            case ORANGE:
               if (hasFruit(performer, tilex, tiley, age)) {
                  if (infected) {
                     toReturn = " No oranges grew to mature state, and they have weird brown spots.";
                  } else {
                     toReturn = " The tree has some fine oranges.";
                  }
               } else if (isAlmostRipe(age, treeType)) {
                  if (infected) {
                     toReturn = " The tree will not produce any healthy oranges.";
                  } else {
                     toReturn = " The oranges will soon be ripe.";
                  }
               } else if (hasBeenPicked(tilex, tiley)) {
                  toReturn = " The tree has been picked clean of its oranges.";
               }
         }
      }

      return toReturn;
   }

   private static final String getGrowthState(Creature performer, int tilex, int tiley, int age, BushData.BushType bushType, boolean infected) {
      String toReturn = "";
      if (age > FoliageAge.YOUNG_FOUR.getAgeId() && age < FoliageAge.OVERAGED.getAgeId()) {
         switch(bushType) {
            case LAVENDER:
               if (hasFruit(performer, tilex, tiley, age)) {
                  if (infected) {
                     toReturn = " The flowers are ugly and sick, with stinking ooze dripping from its petals.";
                  } else {
                     toReturn = " The bush has some beautiful flowers.";
                  }
               } else if (isAlmostRipe(age, bushType)) {
                  if (infected) {
                     toReturn = " The buds look sick.";
                  } else {
                     toReturn = " The bush has a couple of buds.";
                  }
               } else if (hasBeenPicked(tilex, tiley)) {
                  toReturn = " The bush has no flowers left; all have been picked.";
               }
               break;
            case ROSE:
               if (hasFruit(performer, tilex, tiley, age)) {
                  if (infected) {
                     toReturn = " The flowers are ugly and sick, with stinking ooze dripping from its petals.";
                  } else {
                     toReturn = " The bush has some beautiful flowers.";
                  }
               } else if (isAlmostRipe(age, bushType)) {
                  if (infected) {
                     toReturn = " The buds look sick.";
                  } else {
                     toReturn = " The bush has a couple of promising buds.";
                  }
               } else if (hasBeenPicked(tilex, tiley)) {
                  toReturn = " The bush has no flowers left; all have been picked.";
               }
               break;
            case GRAPE:
               if (tiley > Zones.worldTileSizeY / 2) {
                  if (hasFruit(performer, tilex, tiley, age)) {
                     if (infected) {
                        toReturn = " No grapes grew to mature state, and they have weird black spots.";
                     } else {
                        toReturn = " The bush has some juicy blue grapes.";
                     }
                  } else if (isAlmostRipe(age, bushType)) {
                     if (infected) {
                        toReturn = " The bush will not produce any healthy grapes.";
                     } else {
                        toReturn = " The bush has a couple of immature blue grapes.";
                     }
                  } else if (hasBeenPicked(tilex, tiley)) {
                     toReturn = " The bush has no grapes left; all have been picked.";
                  }
               } else if (hasFruit(performer, tilex, tiley, age)) {
                  if (infected) {
                     toReturn = " No grapes grew to mature state, and they have weird black spots.";
                  } else {
                     toReturn = " The bush has some juicy green grapes.";
                  }
               } else if (isAlmostRipe(age, bushType)) {
                  if (infected) {
                     toReturn = " The bush will not produce any healthy grapes.";
                  } else {
                     toReturn = " The bush has a couple of immature green grapes.";
                  }
               } else if (hasBeenPicked(tilex, tiley)) {
                  toReturn = " The bush has no grapes left; all have been picked.";
               }
               break;
            case CAMELLIA:
               if (hasFruit(performer, tilex, tiley, age)) {
                  if (infected) {
                     toReturn = " The leaves are infected with disease. It looks as if someone sprinkled white flour on them.";
                  } else {
                     toReturn = " The bush has a number of leaves that look and smell perfect.";
                  }
               } else if (isAlmostRipe(age, bushType)) {
                  if (infected) {
                     toReturn = " The leaves are infected with disease. It looks as if someone sprinkled white flour on them.";
                  } else {
                     toReturn = " The bush has started to give off an interesting scent.";
                  }
               }
               break;
            case OLEANDER:
               if (hasFruit(performer, tilex, tiley, age)) {
                  if (infected) {
                     toReturn = " The leaves are infected with disease. It looks as if someone sprinkled white flour on them.";
                  } else {
                     toReturn = " The bush has a number of strong smelling leaves.";
                  }
               } else if (isAlmostRipe(age, bushType)) {
                  if (infected) {
                     toReturn = " The leaves are infected with disease. It looks as if someone sprinkled white flour on them.";
                  } else {
                     toReturn = " The bush has started to smell rather badly.";
                  }
               }
               break;
            case HAZELNUT:
               if (hasFruit(performer, tilex, tiley, age)) {
                  if (infected) {
                     toReturn = " The nuts are infected with disease. It looks as if someone sprinkled white flour on them.";
                  } else {
                     toReturn = " The bush has a number of strong smelling nuts.";
                  }
               } else if (isAlmostRipe(age, bushType)) {
                  if (infected) {
                     toReturn = " The nuts are infected with disease. It looks as if someone sprinkled white flour on them.";
                  } else {
                     toReturn = " The bush has started to smell rather odd.";
                  }
               } else if (hasBeenPicked(tilex, tiley)) {
                  toReturn = " The bush has no nuts left; all have been picked.";
               }
               break;
            case RASPBERRY:
               if (hasFruit(performer, tilex, tiley, age)) {
                  if (infected) {
                     toReturn = " No raspberries grew to mature state, and they have weird black spots.";
                  } else {
                     toReturn = " The bush has some juicy raspberries.";
                  }
               } else if (isAlmostRipe(age, bushType)) {
                  if (infected) {
                     toReturn = " The bush will not produce any healthy raspberries.";
                  } else {
                     toReturn = " The bush has a couple of immature raspberries.";
                  }
               } else if (hasBeenPicked(tilex, tiley)) {
                  toReturn = " The bush has no raspberries left; all have been picked.";
               }
               break;
            case BLUEBERRY:
               if (hasFruit(performer, tilex, tiley, age)) {
                  if (infected) {
                     toReturn = " No blueberries grew to mature state, and they have weird black spots.";
                  } else {
                     toReturn = " The bush has some juicy blueberries.";
                  }
               } else if (isAlmostRipe(age, bushType)) {
                  if (infected) {
                     toReturn = " The bush will not produce any healthy blueberries.";
                  } else {
                     toReturn = " The bush has a couple of immature blueberries.";
                  }
               } else if (hasBeenPicked(tilex, tiley)) {
                  toReturn = " The bush has no blueberries left; all have been picked.";
               }
               break;
            case LINGONBERRY:
               if (hasFruit(performer, tilex, tiley, age)) {
                  if (infected) {
                     toReturn = " No lingonberries grew to mature state, and they have weird black spots.";
                  } else {
                     toReturn = " The bush has some juicy lingonberries.";
                  }
               } else if (isAlmostRipe(age, bushType)) {
                  if (infected) {
                     toReturn = " The bush will not produce any healthy lingonberries.";
                  } else {
                     toReturn = " The bush has a couple of immature lingonberries.";
                  }
               } else if (hasBeenPicked(tilex, tiley)) {
                  toReturn = " The bush has no lingonberries left; all have been picked.";
               }
               break;
            default:
               toReturn = "";
         }
      }

      return toReturn;
   }

   static boolean hasBeenPicked(int tilex, int tiley) {
      int encodedTile = Server.surfaceMesh.getTile(tilex, tiley);
      byte type = Tiles.decodeType(encodedTile);
      switch(type) {
         case -114:
            if (!WurmHarvestables.Harvestable.LAVENDER.isHarvestable()) {
               return false;
            }
            break;
         case -113:
            if (!WurmHarvestables.Harvestable.ROSE.isHarvestable()) {
               return false;
            }
            break;
         case -111:
            if (!WurmHarvestables.Harvestable.GRAPE.isHarvestable()) {
               return false;
            }
            break;
         case -110:
            if (!WurmHarvestables.Harvestable.CAMELLIA.isHarvestable()) {
               return false;
            }
            break;
         case -109:
            if (!WurmHarvestables.Harvestable.OLEANDER.isHarvestable()) {
               return false;
            }
            break;
         case -96:
            if (!WurmHarvestables.Harvestable.HAZEL.isHarvestable()) {
               return false;
            }
            break;
         case -93:
            if (!WurmHarvestables.Harvestable.ORANGE.isHarvestable()) {
               return false;
            }
            break;
         case -90:
            if (!WurmHarvestables.Harvestable.RASPBERRY.isHarvestable()) {
               return false;
            }
            break;
         case -87:
            if (!WurmHarvestables.Harvestable.BLUEBERRY.isHarvestable()) {
               return false;
            }
            break;
         case -84:
            if (!WurmHarvestables.Harvestable.LINGONBERRY.isHarvestable()) {
               return false;
            }
            break;
         case 101:
            if (!WurmHarvestables.Harvestable.PINE.isHarvestable()) {
               return false;
            }
            break;
         case 102:
            if (!WurmHarvestables.Harvestable.OAK.isHarvestable()) {
               return false;
            }
            break;
         case 105:
            if (!WurmHarvestables.Harvestable.MAPLE.isHarvestable()) {
               return false;
            }
            break;
         case 106:
            if (!WurmHarvestables.Harvestable.APPLE.isHarvestable()) {
               return false;
            }
            break;
         case 107:
            if (!WurmHarvestables.Harvestable.LEMON.isHarvestable()) {
               return false;
            }
            break;
         case 108:
            if (!WurmHarvestables.Harvestable.OLIVE.isHarvestable()) {
               return false;
            }
            break;
         case 109:
            if (!WurmHarvestables.Harvestable.CHERRY.isHarvestable()) {
               return false;
            }
            break;
         case 110:
            if (!WurmHarvestables.Harvestable.CHESTNUT.isHarvestable()) {
               return false;
            }
            break;
         case 111:
            if (!WurmHarvestables.Harvestable.WALNUT.isHarvestable()) {
               return false;
            }
            break;
         default:
            return false;
      }

      byte data = Tiles.decodeData(encodedTile);
      return !TreeData.hasFruit(data);
   }

   static void pick(int tilex, int tiley) {
      int encodedTile = Server.surfaceMesh.getTile(tilex, tiley);
      byte tileType = Tiles.decodeType(encodedTile);
      short newHeight = Tiles.decodeHeight(encodedTile);
      byte tileData = Tiles.decodeData(encodedTile);
      tileData = (byte)(tileData & 247);
      Server.setSurfaceTile(tilex, tiley, newHeight, tileType, tileData);
      Players.getInstance().sendChangedTile(tilex, tiley, true, false);
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
      GrassData.GrowthTreeStage growthStage = GrassData.GrowthTreeStage.decodeTileData(tileData);

      try {
         float tilexpos = (float)((tilex << 2) + 1);
         float tileypos = (float)((tiley << 2) + 1);
         float tilezpos = Zones.calculateHeight(tilexpos, tileypos, true);
         if (!performer.isWithinDistanceTo(tilexpos, tileypos, tilezpos, 20.0F)) {
            performer.getCommunicator().sendNormalServerMessage("The grass is growing out of your reach.");
            return true;
         }
      } catch (NoSuchZoneException var27) {
         logger.log(
            Level.WARNING, " No such zone exception at " + tilex + "," + tiley + " when player tried to TileTreeBehaviour.cutGrass()", (Throwable)var27
         );
      }

      if (source == null) {
         performer.getCommunicator().sendNormalServerMessage("You need a tool to cut the grass.");
         return true;
      } else {
         if (source.getTemplateId() == 267 || source.getTemplateId() == 268 || source.getTemplateId() == 176) {
            maxQLFromUsedTool = 100.0F;
         } else if (source.getTemplate().isSharp()) {
            maxQLFromUsedTool = 20.0F;
         } else {
            if (source.getTemplateId() != 14) {
               performer.getCommunicator().sendNormalServerMessage("You can't cut grass with " + source.getNameWithGenus() + ".");
               return true;
            }

            maxQLFromUsedTool = 5.0F;
         }

         toolUsed = source;
         yield = GrassData.GrowthTreeStage.getYield(growthStage);
         if (yield == 0) {
            performer.getCommunicator()
               .sendNormalServerMessage("You try to cut some " + growthStage.toString().toLowerCase() + " grass but you fail to get any significant amount.");
            return true;
         } else {
            if (counter == 1.0F) {
               double toolBonus = 0.0;

               try {
                  int weight = ItemTemplateFactory.getInstance().getTemplate(620).getWeightGrams() * yield;
                  if (performer.getInventory().getNumItemsNotCoins() + 1 >= 100) {
                     performer.getCommunicator().sendNormalServerMessage("You would not be able to carry the grass. You need to drop something first.");
                     return true;
                  }

                  if (!performer.canCarry(weight)) {
                     performer.getCommunicator().sendNormalServerMessage("You would not be able to carry the grass. You need to drop some things first.");
                     return true;
                  }
               } catch (NoSuchTemplateException var31) {
                  logger.log(Level.WARNING, var31.getLocalizedMessage(), (Throwable)var31);
                  return true;
               }

               gardening = performer.getSkills().getSkillOrLearn(10045);

               try {
                  toolskill = performer.getSkills().getSkill(source.getTemplateId());
                  toolBonus = toolskill.getKnowledge(0.0);
               } catch (NoSuchSkillException var26) {
               }

               time = Actions.getStandardActionTime(performer, gardening, source, toolBonus);
               performer.getCommunicator().sendNormalServerMessage("You start to gather " + growthStage.toString().toLowerCase() + " grass.");
               Server.getInstance().broadCastAction(performer.getName() + " starts to gather grass.", performer, 5);
               performer.sendActionControl("gathering grass", true, time);
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
                     performer.getCommunicator().sendNormalServerMessage("You would not be able to carry the grass. You need to drop some things first.");
                     return true;
                  }
               } catch (NoSuchTemplateException var30) {
                  logger.log(Level.WARNING, var30.getLocalizedMessage(), (Throwable)var30);
                  return true;
               }

               source.setDamage(source.getDamage() + 0.003F * source.getDamageModifier());
               double toolBonus = 0.0;
               double power = 0.0;
               gardening = performer.getSkills().getSkillOrLearn(10045);

               try {
                  toolskill = performer.getSkills().getSkill(source.getTemplateId());
                  toolBonus = Math.max(1.0, toolskill.skillCheck(1.0, toolUsed, 0.0, false, counter));
               } catch (NoSuchSkillException var25) {
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
                     yieldItem = ItemFactory.createItem(620, Math.max(1.0F, maxQLFromUsedTool), null);
                     if (power < 0.0) {
                        yieldItem.setDamage((float)(-power) / 2.0F);
                     }

                     performer.getInventory().insertItem(yieldItem);
                  }

                  byte newdata = (byte)((tileData & 252) + GrassData.GrowthTreeStage.SHORT.getCode());
                  Server.surfaceMesh.setTile(tilex, tiley, Tiles.encode(Tiles.decodeHeight(tile), tileType, newdata));
                  Players.getInstance().sendChangedTile(tilex, tiley, true, false);
                  performer.getCommunicator().sendNormalServerMessage("You gather " + yield + " " + yieldItem.getName() + ".");
                  Server.getInstance().broadCastAction(performer.getName() + " gathers some " + yieldItem.getName() + ".", performer, 5);
               } catch (NoSuchTemplateException var28) {
                  logger.log(Level.WARNING, "No template for grass type item!", (Throwable)var28);
                  performer.getCommunicator()
                     .sendNormalServerMessage("You fail to gather the grass. Your sensitive mind notices a wrongness in the fabric of space.");
               } catch (FailedException var29) {
                  logger.log(Level.WARNING, var29.getMessage(), (Throwable)var29);
                  performer.getCommunicator()
                     .sendNormalServerMessage("You fail to gather the grass. Your sensitive mind notices a wrongness in the fabric of space.");
               }

               toReturn = true;
            }

            return toReturn;
         }
      }
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
      byte tileData = Tiles.decodeData(tile);
      boolean toReturn = Terraforming.cannotMakeLawn(performer, tilex, tiley);
      if (toReturn) {
         return toReturn;
      } else {
         try {
            float tilexpos = (float)((tilex << 2) + 1);
            float tileypos = (float)((tiley << 2) + 1);
            float tilezpos = Zones.calculateHeight(tilexpos, tileypos, true);
            if (!performer.isWithinDistanceTo(tilexpos, tileypos, tilezpos, 20.0F)) {
               performer.getCommunicator().sendNormalServerMessage("The " + grass + " is growing out of your reach.");
               return true;
            }
         } catch (NoSuchZoneException var22) {
            logger.log(
               Level.WARNING, " No such zone exception at " + tilex + "," + tiley + " when player tried to TileTreeBehaviour.makeLawn()", (Throwable)var22
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
                  } catch (NoSuchSkillException var21) {
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
                  } catch (NoSuchSkillException var20) {
                  }

                  gardening.skillCheck(1.0, source, toolBonus, false, counter);
                  byte newdata = (byte)((tileData & 252) + GrassData.GrowthTreeStage.LAWN.getCode());
                  Server.surfaceMesh.setTile(tilex, tiley, Tiles.encode(Tiles.decodeHeight(tile), tileType, newdata));
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

   private boolean study(Action act, Creature performer, int tilex, int tiley, int tile, short action, float counter) {
      byte tileType = Tiles.decodeType(tile);
      Tiles.Tile theTile = Tiles.getTile(tileType);
      int harvestableId = WurmHarvestables.getHarvestableIdFromTile(tileType);
      WurmHarvestables.Harvestable harvestable = WurmHarvestables.getHarvestable(harvestableId);
      if (harvestable == null) {
         performer.getCommunicator()
            .sendNormalServerMessage("You decide not to study the " + theTile.getName() + " as it doesn't seem to ever be harvestable.");
         return true;
      } else {
         int time = 0;
         if (counter == 1.0F) {
            int var14 = 600;
            performer.getCommunicator().sendNormalServerMessage("You start to study the " + theTile.getName() + ".");
            Server.getInstance().broadCastAction(performer.getName() + " starts to study the " + theTile.getName() + ".", performer, 5);
            performer.sendActionControl("studying " + theTile.getName(), true, var14);
            act.setTimeLeft(var14);
            return false;
         } else {
            time = act.getTimeLeft();
            if (act.mayPlaySound()) {
               Methods.sendSound(performer, "sound.work.foragebotanize");
            }

            sendStudyMessages(performer, harvestable, act.currentSecond());
            if (counter * 10.0F >= (float)time) {
               if (performer.getPower() < 2) {
                  pick(tilex, tiley);
               }

               ((Player)performer).setStudied(harvestableId);
               performer.getCommunicator()
                  .sendNormalServerMessage("You finish studying the " + theTile.getName() + ". You now need to record the study results.");
               Server.getInstance()
                  .broadCastAction(performer.getName() + " looks pleased with " + performer.getHisHerItsString() + " study results.", performer, 5);
               performer.achievement(553);
               return true;
            } else {
               return false;
            }
         }
      }
   }

   static void sendStudyMessages(Creature performer, WurmHarvestables.Harvestable harvestable, int currentSecond) {
      if (currentSecond == 5) {
         performer.getCommunicator().sendNormalServerMessage("You pick a leaf.");
      } else if (currentSecond == 10) {
         performer.getCommunicator().sendNormalServerMessage("You make a mental note of the shape of the leaf.");
      } else if (currentSecond == 15) {
         performer.getCommunicator().sendNormalServerMessage("You check the underside of the leaf for any unusual markings.");
      } else if (currentSecond == 20) {
         performer.getCommunicator().sendNormalServerMessage("You rub the leaf between your thumb and index finger to see what aroma comes from it.");
      } else if (currentSecond == 25) {
         performer.getCommunicator().sendNormalServerMessage("You look up the default harvest times in Wurmpedia.");
      } else if (currentSecond == 30) {
         performer.getCommunicator().sendNormalServerMessage("You throw away the damaged leaf.");
      } else if (harvestable.isSap()) {
         if (currentSecond == 35) {
            performer.getCommunicator().sendNormalServerMessage("You make a small hole in the bark.");
         } else if (currentSecond == 40) {
            performer.getCommunicator().sendNormalServerMessage("You wait for the sap to start flowing.");
         } else if (currentSecond == 45) {
            performer.getCommunicator().sendNormalServerMessage("You drain off " + harvestable.getFruitWithGenus() + ".");
         } else if (currentSecond == 50) {
            performer.getCommunicator().sendNormalServerMessage("You rub the " + harvestable.getFruit() + " between your thumb and forefinger.");
         } else if (currentSecond == 55) {
            performer.getCommunicator().sendNormalServerMessage("You pour the " + harvestable.getFruit() + " away.");
         }
      } else if (harvestable.isLeaf()) {
         if (currentSecond == 35) {
            performer.getCommunicator().sendNormalServerMessage("You pick another " + harvestable.getFruit() + ".");
         } else if (currentSecond == 40) {
            performer.getCommunicator().sendNormalServerMessage("You detect a slight oilyness on the skin.");
         } else if (currentSecond == 45) {
            performer.getCommunicator().sendNormalServerMessage("You wonder what would happen if it was infused in water.");
         } else if (currentSecond == 50) {
            performer.getCommunicator().sendNormalServerMessage("You crush the " + harvestable.getFruit() + " between your thumb and forefinger.");
         } else if (currentSecond == 55) {
            performer.getCommunicator().sendNormalServerMessage("You throw the crushed " + harvestable.getFruit() + " away.");
         }
      } else if (harvestable.isFlower()) {
         if (currentSecond == 35) {
            performer.getCommunicator().sendNormalServerMessage("You carefully pick " + harvestable.getFruitWithGenus() + ".");
         } else if (currentSecond == 40) {
            performer.getCommunicator().sendNormalServerMessage("You count the number of petals on the " + harvestable.getFruit() + ".");
         } else if (currentSecond == 45) {
            performer.getCommunicator().sendNormalServerMessage("You try to gauge the colour of the " + harvestable.getFruit() + ".");
         } else if (currentSecond == 50) {
            performer.getCommunicator().sendNormalServerMessage("You roll the " + harvestable.getFruit() + " between your thumb and forefinger.");
         } else if (currentSecond == 55) {
            performer.getCommunicator().sendNormalServerMessage("You throw the rolled " + harvestable.getFruit() + " away.");
         }
      } else if (harvestable.isNut()) {
         if (currentSecond == 35) {
            performer.getCommunicator().sendNormalServerMessage("You carefully pick " + harvestable.getFruitWithGenus() + ".");
         } else if (currentSecond == 39) {
            performer.getCommunicator().sendNormalServerMessage("You inspect the outside of the " + harvestable.getFruit() + ".");
         } else if (currentSecond == 43) {
            performer.getCommunicator().sendNormalServerMessage("You break open the " + harvestable.getFruit() + ".");
         } else if (currentSecond == 47) {
            performer.getCommunicator()
               .sendNormalServerMessage("You study the " + harvestable.getFruit() + " to better understand just how old it really is.");
         } else if (currentSecond == 51) {
            performer.getCommunicator().sendNormalServerMessage("You taste the " + harvestable.getFruit() + ".");
         } else if (currentSecond == 55) {
            performer.getCommunicator().sendNormalServerMessage("You discard the " + harvestable.getFruit() + ".");
         }
      } else if (harvestable.isFruit()) {
         if (currentSecond == 35) {
            performer.getCommunicator().sendNormalServerMessage("You carefully pick " + harvestable.getFruitWithGenus() + ".");
         } else if (currentSecond == 40) {
            performer.getCommunicator().sendNormalServerMessage("You inspect the " + harvestable.getFruit() + ".");
         } else if (currentSecond == 45) {
            performer.getCommunicator()
               .sendNormalServerMessage("You study the " + harvestable.getFruit() + " to better understand just how old it really is.");
         } else if (currentSecond == 50) {
            performer.getCommunicator().sendNormalServerMessage("You break open the " + harvestable.getFruit() + " to check for pips.");
         } else if (currentSecond == 55) {
            performer.getCommunicator().sendNormalServerMessage("You discard the " + harvestable.getFruit() + ".");
         }
      } else if (harvestable.isHops()) {
         if (currentSecond == 35) {
            performer.getCommunicator().sendNormalServerMessage("You carefully pick " + harvestable.getFruitWithGenus() + ".");
         } else if (currentSecond == 40) {
            performer.getCommunicator().sendNormalServerMessage("You inspect the " + harvestable.getFruit() + ".");
         } else if (currentSecond == 45) {
            performer.getCommunicator().sendNormalServerMessage("You squeeze the " + harvestable.getFruit() + " to see how firm they are.");
         } else if (currentSecond == 50) {
            performer.getCommunicator().sendNormalServerMessage("You sniff the " + harvestable.getFruit() + " to check their aroma.");
         } else if (currentSecond == 55) {
            performer.getCommunicator().sendNormalServerMessage("You discard the " + harvestable.getFruit() + ".");
         }
      } else if (harvestable.isBerry()) {
         if (currentSecond == 35) {
            performer.getCommunicator().sendNormalServerMessage("You carefully pick " + harvestable.getFruitWithGenus() + ".");
         } else if (currentSecond == 40) {
            performer.getCommunicator()
               .sendNormalServerMessage("You study the " + harvestable.getFruit() + " to better understand just how old it really is.");
         } else if (currentSecond == 45) {
            performer.getCommunicator().sendNormalServerMessage("You dissect the " + harvestable.getFruit() + ".");
         } else if (currentSecond == 50) {
            performer.getCommunicator().sendNormalServerMessage("You taste the " + harvestable.getFruit() + " for sweetness.");
         } else if (currentSecond == 55) {
            performer.getCommunicator().sendNormalServerMessage("You discard the " + harvestable.getFruit() + ".");
         }
      } else if (currentSecond == 35) {
         performer.getCommunicator().sendNormalServerMessage("You carefully pick " + harvestable.getFruitWithGenus() + ".");
      } else if (currentSecond == 40) {
         performer.getCommunicator().sendNormalServerMessage("You inspect the outside of the " + harvestable.getFruit() + ".");
      } else if (currentSecond == 45) {
         performer.getCommunicator().sendNormalServerMessage("You dissect the " + harvestable.getFruit() + ".");
      } else if (currentSecond == 50) {
         performer.getCommunicator()
            .sendNormalServerMessage("You study the inside of the " + harvestable.getFruit() + " to better understand just how old it really is.");
      } else if (currentSecond == 55) {
         performer.getCommunicator().sendNormalServerMessage("You discard the " + harvestable.getFruit() + ".");
      }
   }
}

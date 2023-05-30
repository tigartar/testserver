package com.wurmonline.server.behaviours;

import com.wurmonline.mesh.Tiles;
import com.wurmonline.server.Items;
import com.wurmonline.server.Server;
import com.wurmonline.server.Servers;
import com.wurmonline.server.creatures.Creature;
import com.wurmonline.server.highways.MethodsHighways;
import com.wurmonline.server.items.Item;
import com.wurmonline.server.items.ItemTemplate;
import com.wurmonline.server.items.ItemTemplateFactory;
import com.wurmonline.server.items.NoSuchTemplateException;
import com.wurmonline.server.skills.NoSuchSkillException;
import com.wurmonline.server.skills.Skill;
import com.wurmonline.server.sounds.SoundPlayer;
import com.wurmonline.server.structures.Floor;
import com.wurmonline.server.structures.NoSuchStructureException;
import com.wurmonline.server.structures.RoofFloorEnum;
import com.wurmonline.server.structures.Structure;
import com.wurmonline.server.structures.Structures;
import com.wurmonline.server.zones.VolaTile;
import com.wurmonline.server.zones.Zones;
import com.wurmonline.shared.constants.StructureConstants;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

public final class FloorBehaviour extends TileBehaviour {
   private static final Logger logger = Logger.getLogger(FloorBehaviour.class.getName());

   FloorBehaviour() {
      super((short)45);
   }

   @Override
   public List<ActionEntry> getBehavioursFor(Creature performer, boolean onSurface, Floor floor) {
      List<ActionEntry> toReturn = new LinkedList<>();
      if (!floor.isFinished()) {
         toReturn.add(Actions.actionEntrys[607]);
      }

      toReturn.addAll(Actions.getDefaultTileActions());
      toReturn.addAll(super.getTileAndFloorBehavioursFor(performer, null, floor.getTileX(), floor.getTileY(), Tiles.Tile.TILE_DIRT.id));
      if (floor.getType() == StructureConstants.FloorType.OPENING) {
         if (floor.isFinished()) {
            if (floor.getFloorLevel() == performer.getFloorLevel()) {
               toReturn.add(Actions.actionEntrys[523]);
            } else if (floor.getFloorLevel() == performer.getFloorLevel() + 1) {
               toReturn.add(Actions.actionEntrys[522]);
            }
         } else if (floor.getFloorLevel() == performer.getFloorLevel()) {
            toReturn.add(Actions.actionEntrys[523]);
         }
      }

      VolaTile floorTile = Zones.getOrCreateTile(floor.getTileX(), floor.getTileY(), floor.getLayer() >= 0);
      Structure structure = null;

      try {
         structure = Structures.getStructure(floor.getStructureId());
      } catch (NoSuchStructureException var8) {
         logger.log(Level.WARNING, var8.getMessage(), (Throwable)var8);
         return toReturn;
      }

      if (MethodsStructure.mayModifyStructure(performer, structure, floorTile, (short)177)) {
         toReturn.add(new ActionEntry((short)-2, "Rotate", "rotating"));
         toReturn.add(new ActionEntry((short)177, "Turn clockwise", "turning"));
         toReturn.add(new ActionEntry((short)178, "Turn counterclockwise", "turning"));
      }

      return toReturn;
   }

   public static final List<ActionEntry> getCompletedFloorsBehaviour(boolean andStaircases, boolean onSurface) {
      List<ActionEntry> plantypes = new ArrayList<>();
      plantypes.add(Actions.actionEntrys[508]);
      plantypes.add(Actions.actionEntrys[515]);
      if (andStaircases) {
         plantypes.add(Actions.actionEntrys[659]);
         plantypes.add(Actions.actionEntrys[704]);
         plantypes.add(Actions.actionEntrys[713]);
         plantypes.add(Actions.actionEntrys[714]);
         plantypes.add(Actions.actionEntrys[715]);
         plantypes.add(Actions.actionEntrys[705]);
         plantypes.add(Actions.actionEntrys[706]);
         plantypes.add(Actions.actionEntrys[709]);
         plantypes.add(Actions.actionEntrys[710]);
         plantypes.add(Actions.actionEntrys[711]);
         plantypes.add(Actions.actionEntrys[712]);
      }

      plantypes.add(Actions.actionEntrys[509]);
      if (onSurface) {
         plantypes.add(Actions.actionEntrys[507]);
      }

      Collections.sort(plantypes);
      List<ActionEntry> toReturn = new ArrayList<>(5);
      toReturn.add(new ActionEntry((short)(-plantypes.size()), "Plan", "planning"));
      toReturn.addAll(plantypes);
      return toReturn;
   }

   @Override
   public List<ActionEntry> getBehavioursFor(Creature performer, Item source, boolean onSurface, Floor floor) {
      List<ActionEntry> toReturn = new LinkedList<>();
      if (!floor.isFinished()) {
         toReturn.add(Actions.actionEntrys[607]);
      }

      toReturn.addAll(super.getTileAndFloorBehavioursFor(performer, source, floor.getTileX(), floor.getTileY(), Tiles.Tile.TILE_DIRT.id));
      if (floor.getType() == StructureConstants.FloorType.OPENING) {
         if (floor.isFinished()) {
            if (floor.getFloorLevel() == performer.getFloorLevel()) {
               toReturn.add(Actions.actionEntrys[523]);
            } else if (floor.getFloorLevel() == performer.getFloorLevel() + 1) {
               toReturn.add(Actions.actionEntrys[522]);
            }
         } else if (floor.getFloorLevel() == performer.getFloorLevel()) {
            toReturn.add(Actions.actionEntrys[523]);
         }
      }

      VolaTile floorTile = Zones.getOrCreateTile(floor.getTileX(), floor.getTileY(), floor.isOnSurface());
      Structure structure = null;

      try {
         structure = Structures.getStructure(floor.getStructureId());
      } catch (NoSuchStructureException var11) {
         logger.log(Level.WARNING, var11.getMessage(), (Throwable)var11);
         toReturn.addAll(Actions.getDefaultItemActions());
         return toReturn;
      }

      if (MethodsStructure.mayModifyStructure(performer, structure, floorTile, (short)169)) {
         switch(floor.getFloorState()) {
            case BUILDING:
               toReturn.add(new ActionEntry((short)169, "Continue building", "building"));
               break;
            case PLANNING:
               if (floor.getType() == StructureConstants.FloorType.ROOF) {
                  List<RoofFloorEnum> list = RoofFloorEnum.getRoofsByTool(source);
                  if (list.size() <= 0) {
                     break;
                  }

                  toReturn.add(new ActionEntry((short)(-list.size()), "Build", "building"));

                  for(RoofFloorEnum en : list) {
                     toReturn.add(en.createActionEntry());
                  }
               } else {
                  List<RoofFloorEnum> list = RoofFloorEnum.getFloorByToolAndType(source, floor.getType());
                  if (list.size() <= 0) {
                     break;
                  }

                  toReturn.add(new ActionEntry((short)(-list.size()), "Build", "building"));

                  for(RoofFloorEnum en : list) {
                     toReturn.add(en.createActionEntry());
                  }
               }
               break;
            case COMPLETED:
               if (floor.getType() != StructureConstants.FloorType.ROOF) {
                  toReturn.addAll(getCompletedFloorsBehaviour(true, floor.isOnSurface()));
               }
         }

         toReturn.add(new ActionEntry((short)-2, "Rotate", "rotating"));
         toReturn.add(new ActionEntry((short)177, "Turn clockwise", "turning"));
         toReturn.add(new ActionEntry((short)178, "Turn counterclockwise", "turning"));
      }

      if (!source.isTraded()) {
         if (source.getTemplateId() == floor.getRepairItemTemplate()) {
            if (floor.getDamage() > 0.0F) {
               if ((!Servers.localServer.challengeServer || performer.getEnemyPresense() <= 0) && !floor.isNoRepair()) {
                  toReturn.add(Actions.actionEntrys[193]);
               }
            } else if (floor.getQualityLevel() < 100.0F && !floor.isNoImprove()) {
               toReturn.add(Actions.actionEntrys[192]);
            }
         }

         toReturn.addAll(Actions.getDefaultItemActions());
         if (!floor.isIndestructible()) {
            if (floor.getType() == StructureConstants.FloorType.ROOF) {
               toReturn.add(Actions.actionEntrys[525]);
            } else if (!MethodsHighways.onHighway(floor)) {
               toReturn.add(Actions.actionEntrys[524]);
            }
         }

         if ((source.getTemplateId() == 315 || source.getTemplateId() == 176) && performer.getPower() >= 2) {
            toReturn.add(Actions.actionEntrys[684]);
         }
      }

      return toReturn;
   }

   private boolean buildAction(Action act, Creature performer, Item source, Floor floor, short action, float counter) {
      switch(floor.getFloorState()) {
         case BUILDING:
            if (floorBuilding(act, performer, source, floor, action, counter)) {
               performer.getCommunicator().sendAddFloorRoofToCreationWindow(floor, floor.getId());
               return true;
            }

            return false;
         case PLANNING:
            boolean autoAdvance = performer.getPower() >= 2 && source.getTemplateId() == 176;
            Skill craftSkill = null;

            try {
               craftSkill = performer.getSkills().getSkill(1005);
            } catch (NoSuchSkillException var15) {
               craftSkill = performer.getSkills().learn(1005, 1.0F);
            }

            StructureConstants.FloorMaterial newMaterial = StructureConstants.FloorMaterial.fromByte((byte)(action - 20000));
            StructureConstants.FloorMaterial oldMaterial = floor.getMaterial();
            floor.setMaterial(newMaterial);
            if (!isOkToBuild(performer, source, floor, floor.getFloorLevel(), floor.isRoof())) {
               floor.setMaterial(oldMaterial);
               return true;
            } else if (!autoAdvance && !advanceNextState(performer, floor, act, true)) {
               String message = buildRequiredMaterialString(floor, false);
               performer.getCommunicator().sendNormalServerMessage("You need " + message + " to start building that.");
               floor.setMaterial(oldMaterial);
               return true;
            } else {
               floor.setFloorState(StructureConstants.FloorState.BUILDING);
               float oldql = floor.getQualityLevel();
               float qlevel = MethodsStructure.calculateNewQualityLevel(act.getPower(), craftSkill.getKnowledge(0.0), oldql, getTotalMaterials(floor));
               floor.setQualityLevel(qlevel);

               try {
                  floor.save();
               } catch (IOException var14) {
                  logger.log(Level.WARNING, var14.getMessage(), (Throwable)var14);
               }

               if (floor.getMaterial() == StructureConstants.FloorMaterial.STANDALONE) {
                  performer.getCommunicator().sendNormalServerMessage("You plan a " + floor.getName() + ".");
               } else if (floor.getType() == StructureConstants.FloorType.ROOF) {
                  performer.getCommunicator().sendNormalServerMessage("You plan a " + floor.getName() + " made of " + getMaterialDescription(floor) + ".");
               } else {
                  performer.getCommunicator()
                     .sendNormalServerMessage("You plan a " + floor.getName() + " made of " + floor.getMaterial().getName().toLowerCase() + ".");
               }

               return floorBuilding(act, performer, source, floor, action, counter);
            }
         case COMPLETED:
            logger.log(Level.WARNING, "FloorBehaviour buildAction on a completed floor, it should not happen?!");
            performer.getCommunicator().sendNormalServerMessage("You failed to find anything to do with that.");
            return true;
         default:
            logger.log(Level.WARNING, "Enum value added to FloorState but not to a switch statement in method FloorBehaviour.action()");
            return false;
      }
   }

   static boolean advanceNextState(Creature performer, Floor floor, Action act, boolean justCheckIfItemsArePresent) {
      List<BuildMaterial> mats = getRequiredMaterialsAtState(floor);
      if (takeItemsFromCreature(performer, floor, act, mats, justCheckIfItemsArePresent)) {
         return true;
      } else if (performer.getPower() >= 4 && !justCheckIfItemsArePresent) {
         performer.getCommunicator().sendNormalServerMessage("You magically summon some necessary materials.");
         return true;
      } else {
         return false;
      }
   }

   static boolean takeItemsFromCreature(Creature performer, Floor floor, Action act, List<BuildMaterial> mats, boolean justCheckIfItemsArePresent) {
      Item[] inventoryItems = performer.getInventory().getAllItems(false);
      Item[] bodyItems = performer.getBody().getAllItems();
      List<Item> takeItemsOnSuccess = new ArrayList<>();

      for(Item item : inventoryItems) {
         for(BuildMaterial mat : mats) {
            if (mat.getNeededQuantity() > 0 && item.getTemplateId() == mat.getTemplateId() && item.getWeightGrams() >= mat.getWeightGrams()) {
               takeItemsOnSuccess.add(item);
               mat.setNeededQuantity(0);
               break;
            }
         }
      }

      for(Item item : bodyItems) {
         for(BuildMaterial mat : mats) {
            if (mat.getNeededQuantity() > 0 && item.getTemplateId() == mat.getTemplateId() && item.getWeightGrams() >= mat.getWeightGrams()) {
               takeItemsOnSuccess.add(item);
               mat.setNeededQuantity(0);
               break;
            }
         }
      }

      float divider = 1.0F;

      for(BuildMaterial mat : mats) {
         divider += (float)mat.getTotalQuantityRequired();
         if (mat.getNeededQuantity() > 0) {
            return false;
         }
      }

      float qlevel = 0.0F;
      if (!justCheckIfItemsArePresent) {
         for(Item item : takeItemsOnSuccess) {
            act.setPower(item.getCurrentQualityLevel() / divider);
            performer.sendToLoggers("Adding " + item.getCurrentQualityLevel() + ", divider=" + divider + "=" + act.getPower());
            qlevel += item.getCurrentQualityLevel() / 21.0F;
            if (item.isCombine()) {
               item.setWeight(item.getWeightGrams() - item.getTemplate().getWeightGrams(), true);
            } else {
               Items.destroyItem(item.getWurmId());
            }
         }
      }

      act.setPower(qlevel);
      return true;
   }

   public static int getSkillForRoof(StructureConstants.FloorMaterial material) {
      switch(material) {
         case WOOD:
            return 1005;
         case CLAY_BRICK:
            return 1013;
         case SLATE_SLAB:
            return 1013;
         case STONE_BRICK:
            return 1013;
         case SANDSTONE_SLAB:
            return 1013;
         case STONE_SLAB:
            return 1013;
         case MARBLE_SLAB:
            return 1013;
         case THATCH:
            return 10092;
         case METAL_IRON:
            return 10015;
         case METAL_COPPER:
            return 10015;
         case METAL_STEEL:
            return 10015;
         case METAL_SILVER:
            return 10015;
         case METAL_GOLD:
            return 10015;
         case STANDALONE:
            return 1005;
         default:
            return 1005;
      }
   }

   public static int getSkillForFloor(StructureConstants.FloorMaterial material) {
      switch(material) {
         case WOOD:
            return 1005;
         case CLAY_BRICK:
            return 10031;
         case SLATE_SLAB:
            return 10031;
         case STONE_BRICK:
            return 10031;
         case SANDSTONE_SLAB:
            return 10031;
         case STONE_SLAB:
            return 10031;
         case MARBLE_SLAB:
            return 10031;
         case THATCH:
            return 10092;
         case METAL_IRON:
            return 10015;
         case METAL_COPPER:
            return 10015;
         case METAL_STEEL:
            return 10015;
         case METAL_SILVER:
            return 10015;
         case METAL_GOLD:
            return 10015;
         case STANDALONE:
            return 1005;
         default:
            return 1005;
      }
   }

   static byte getFinishedState(Floor floor) {
      byte numStates = 0;

      for(BuildMaterial mat : getRequiredMaterialsFor(floor)) {
         if (numStates < mat.getTotalQuantityRequired()) {
            numStates = (byte)mat.getTotalQuantityRequired();
         }
      }

      if (numStates <= 0) {
         numStates = 1;
      }

      return numStates;
   }

   static byte getTotalMaterials(Floor floor) {
      int total = 0;

      for(BuildMaterial mat : getRequiredMaterialsFor(floor)) {
         int totalReq = mat.getTotalQuantityRequired();
         if (totalReq > total) {
            total = totalReq;
         }
      }

      return (byte)total;
   }

   public static final List<BuildMaterial> getRequiredMaterialsForRoof(StructureConstants.FloorMaterial material) {
      List<BuildMaterial> toReturn = new ArrayList<>();

      try {
         switch(material) {
            case WOOD:
               toReturn.add(new BuildMaterial(790, 10));
               toReturn.add(new BuildMaterial(218, 2));
               break;
            case CLAY_BRICK:
               toReturn.add(new BuildMaterial(778, 10));
               toReturn.add(new BuildMaterial(492, 10));
               break;
            case SLATE_SLAB:
               toReturn.add(new BuildMaterial(784, 10));
               toReturn.add(new BuildMaterial(492, 5));
               break;
            case STONE_BRICK:
               toReturn.add(new BuildMaterial(132, 10));
               toReturn.add(new BuildMaterial(492, 10));
               break;
            case SANDSTONE_SLAB:
            case STONE_SLAB:
            case MARBLE_SLAB:
            default:
               logger.log(Level.WARNING, "Someone tried to make a roof but the material choice was not supported (" + material.toString() + ")");
               break;
            case THATCH:
               toReturn.add(new BuildMaterial(756, 10));
               toReturn.add(new BuildMaterial(444, 10));
         }
      } catch (NoSuchTemplateException var3) {
         logger.log(Level.WARNING, "FloorBehaviour.getRequiredMaterialsAtState trying to use material that have a non existing template.", (Throwable)var3);
      }

      return toReturn;
   }

   public static List<BuildMaterial> getRequiredMaterialsForFloor(StructureConstants.FloorType type, StructureConstants.FloorMaterial material) {
      List<BuildMaterial> toReturn = new ArrayList<>();

      try {
         if (type == StructureConstants.FloorType.OPENING) {
            switch(material) {
               case WOOD:
                  toReturn.add(new BuildMaterial(22, 5));
                  toReturn.add(new BuildMaterial(218, 1));
                  break;
               case CLAY_BRICK:
                  toReturn.add(new BuildMaterial(776, 5));
                  toReturn.add(new BuildMaterial(492, 5));
                  break;
               case SLATE_SLAB:
                  toReturn.add(new BuildMaterial(771, 2));
                  toReturn.add(new BuildMaterial(492, 10));
                  break;
               case STONE_BRICK:
                  toReturn.add(new BuildMaterial(132, 5));
                  toReturn.add(new BuildMaterial(492, 5));
                  break;
               case SANDSTONE_SLAB:
                  toReturn.add(new BuildMaterial(1124, 2));
                  toReturn.add(new BuildMaterial(492, 10));
                  break;
               case STONE_SLAB:
                  toReturn.add(new BuildMaterial(406, 2));
                  toReturn.add(new BuildMaterial(492, 10));
                  break;
               case MARBLE_SLAB:
                  toReturn.add(new BuildMaterial(787, 2));
                  toReturn.add(new BuildMaterial(492, 10));
                  break;
               case THATCH:
               case METAL_IRON:
               case METAL_COPPER:
               case METAL_STEEL:
               case METAL_SILVER:
               case METAL_GOLD:
               default:
                  logger.log(
                     Level.WARNING, "Someone tried to make a floor with an opening but the material choice was not supported (" + material.toString() + ")"
                  );
                  break;
               case STANDALONE:
                  toReturn.add(new BuildMaterial(23, 2));
                  toReturn.add(new BuildMaterial(218, 1));
            }
         } else if (type == StructureConstants.FloorType.WIDE_STAIRCASE) {
            toReturn.add(new BuildMaterial(22, 30));
            toReturn.add(new BuildMaterial(217, 2));
         } else if (type == StructureConstants.FloorType.WIDE_STAIRCASE_RIGHT || type == StructureConstants.FloorType.WIDE_STAIRCASE_LEFT) {
            toReturn.add(new BuildMaterial(22, 30));
            toReturn.add(new BuildMaterial(23, 5));
            toReturn.add(new BuildMaterial(218, 1));
            toReturn.add(new BuildMaterial(217, 2));
         } else if (type == StructureConstants.FloorType.WIDE_STAIRCASE_BOTH) {
            toReturn.add(new BuildMaterial(22, 30));
            toReturn.add(new BuildMaterial(23, 10));
            toReturn.add(new BuildMaterial(218, 1));
            toReturn.add(new BuildMaterial(217, 2));
         } else if (type == StructureConstants.FloorType.STAIRCASE
            || type == StructureConstants.FloorType.RIGHT_STAIRCASE
            || type == StructureConstants.FloorType.LEFT_STAIRCASE) {
            switch(material) {
               case WOOD:
                  toReturn.add(new BuildMaterial(22, 20));
                  toReturn.add(new BuildMaterial(23, 10));
                  toReturn.add(new BuildMaterial(218, 2));
                  toReturn.add(new BuildMaterial(217, 1));
                  break;
               case CLAY_BRICK:
                  toReturn.add(new BuildMaterial(776, 5));
                  toReturn.add(new BuildMaterial(492, 5));
                  toReturn.add(new BuildMaterial(22, 15));
                  toReturn.add(new BuildMaterial(23, 10));
                  toReturn.add(new BuildMaterial(218, 1));
                  toReturn.add(new BuildMaterial(217, 1));
                  break;
               case SLATE_SLAB:
                  toReturn.add(new BuildMaterial(771, 2));
                  toReturn.add(new BuildMaterial(492, 10));
                  toReturn.add(new BuildMaterial(22, 15));
                  toReturn.add(new BuildMaterial(23, 10));
                  toReturn.add(new BuildMaterial(218, 1));
                  toReturn.add(new BuildMaterial(217, 1));
                  break;
               case STONE_BRICK:
                  toReturn.add(new BuildMaterial(132, 5));
                  toReturn.add(new BuildMaterial(492, 5));
                  toReturn.add(new BuildMaterial(22, 15));
                  toReturn.add(new BuildMaterial(23, 10));
                  toReturn.add(new BuildMaterial(218, 1));
                  toReturn.add(new BuildMaterial(217, 1));
                  break;
               case SANDSTONE_SLAB:
                  toReturn.add(new BuildMaterial(1124, 2));
                  toReturn.add(new BuildMaterial(492, 10));
                  toReturn.add(new BuildMaterial(22, 15));
                  toReturn.add(new BuildMaterial(23, 10));
                  toReturn.add(new BuildMaterial(218, 1));
                  toReturn.add(new BuildMaterial(217, 1));
                  break;
               case STONE_SLAB:
                  toReturn.add(new BuildMaterial(406, 2));
                  toReturn.add(new BuildMaterial(492, 10));
                  toReturn.add(new BuildMaterial(22, 15));
                  toReturn.add(new BuildMaterial(23, 10));
                  toReturn.add(new BuildMaterial(218, 1));
                  toReturn.add(new BuildMaterial(217, 1));
                  break;
               case MARBLE_SLAB:
                  toReturn.add(new BuildMaterial(787, 2));
                  toReturn.add(new BuildMaterial(492, 10));
                  toReturn.add(new BuildMaterial(22, 15));
                  toReturn.add(new BuildMaterial(23, 10));
                  toReturn.add(new BuildMaterial(218, 1));
                  toReturn.add(new BuildMaterial(217, 1));
                  break;
               case THATCH:
               case METAL_IRON:
               case METAL_COPPER:
               case METAL_STEEL:
               case METAL_SILVER:
               case METAL_GOLD:
               default:
                  logger.log(
                     Level.WARNING,
                     "Someone tried to make a staircase with an opening but the material choice was not supported (" + material.toString() + ")"
                  );
                  break;
               case STANDALONE:
                  toReturn.add(new BuildMaterial(22, 15));
                  toReturn.add(new BuildMaterial(23, 10));
                  toReturn.add(new BuildMaterial(218, 1));
                  toReturn.add(new BuildMaterial(217, 1));
            }
         } else if (type == StructureConstants.FloorType.CLOCKWISE_STAIRCASE || type == StructureConstants.FloorType.ANTICLOCKWISE_STAIRCASE) {
            switch(material) {
               case WOOD:
                  toReturn.add(new BuildMaterial(22, 20));
                  toReturn.add(new BuildMaterial(218, 2));
                  toReturn.add(new BuildMaterial(217, 1));
                  toReturn.add(new BuildMaterial(132, 20));
                  toReturn.add(new BuildMaterial(492, 20));
                  break;
               case CLAY_BRICK:
                  toReturn.add(new BuildMaterial(776, 5));
                  toReturn.add(new BuildMaterial(22, 15));
                  toReturn.add(new BuildMaterial(218, 1));
                  toReturn.add(new BuildMaterial(217, 1));
                  toReturn.add(new BuildMaterial(132, 20));
                  toReturn.add(new BuildMaterial(492, 25));
                  break;
               case SLATE_SLAB:
                  toReturn.add(new BuildMaterial(771, 2));
                  toReturn.add(new BuildMaterial(22, 15));
                  toReturn.add(new BuildMaterial(218, 1));
                  toReturn.add(new BuildMaterial(217, 1));
                  toReturn.add(new BuildMaterial(132, 20));
                  toReturn.add(new BuildMaterial(492, 30));
                  break;
               case STONE_BRICK:
                  toReturn.add(new BuildMaterial(22, 15));
                  toReturn.add(new BuildMaterial(218, 1));
                  toReturn.add(new BuildMaterial(217, 1));
                  toReturn.add(new BuildMaterial(132, 25));
                  toReturn.add(new BuildMaterial(492, 25));
                  break;
               case SANDSTONE_SLAB:
                  toReturn.add(new BuildMaterial(1124, 2));
                  toReturn.add(new BuildMaterial(22, 15));
                  toReturn.add(new BuildMaterial(218, 1));
                  toReturn.add(new BuildMaterial(217, 1));
                  toReturn.add(new BuildMaterial(132, 20));
                  toReturn.add(new BuildMaterial(492, 30));
                  break;
               case STONE_SLAB:
                  toReturn.add(new BuildMaterial(406, 2));
                  toReturn.add(new BuildMaterial(22, 15));
                  toReturn.add(new BuildMaterial(218, 1));
                  toReturn.add(new BuildMaterial(217, 1));
                  toReturn.add(new BuildMaterial(132, 20));
                  toReturn.add(new BuildMaterial(492, 30));
                  break;
               case MARBLE_SLAB:
                  toReturn.add(new BuildMaterial(787, 2));
                  toReturn.add(new BuildMaterial(22, 15));
                  toReturn.add(new BuildMaterial(218, 1));
                  toReturn.add(new BuildMaterial(217, 1));
                  toReturn.add(new BuildMaterial(132, 20));
                  toReturn.add(new BuildMaterial(492, 30));
                  break;
               default:
                  logger.log(Level.WARNING, "Someone tried to make a spiral staircase but the material choice was not supported (" + material.toString() + ")");
            }
         } else if (type != StructureConstants.FloorType.CLOCKWISE_STAIRCASE_WITH && type != StructureConstants.FloorType.ANTICLOCKWISE_STAIRCASE_WITH) {
            switch(material) {
               case WOOD:
                  toReturn.add(new BuildMaterial(22, 10));
                  toReturn.add(new BuildMaterial(218, 2));
                  break;
               case CLAY_BRICK:
                  toReturn.add(new BuildMaterial(776, 10));
                  toReturn.add(new BuildMaterial(492, 10));
                  break;
               case SLATE_SLAB:
                  toReturn.add(new BuildMaterial(771, 2));
                  toReturn.add(new BuildMaterial(492, 10));
                  break;
               case STONE_BRICK:
                  toReturn.add(new BuildMaterial(132, 10));
                  toReturn.add(new BuildMaterial(492, 10));
                  break;
               case SANDSTONE_SLAB:
                  toReturn.add(new BuildMaterial(1124, 2));
                  toReturn.add(new BuildMaterial(492, 10));
                  break;
               case STONE_SLAB:
                  toReturn.add(new BuildMaterial(406, 2));
                  toReturn.add(new BuildMaterial(492, 10));
                  break;
               case MARBLE_SLAB:
                  toReturn.add(new BuildMaterial(787, 2));
                  toReturn.add(new BuildMaterial(492, 10));
                  break;
               default:
                  logger.log(Level.WARNING, "Someone tried to make a floor but the material choice was not supported (" + material.toString() + ")");
            }
         } else {
            switch(material) {
               case WOOD:
                  toReturn.add(new BuildMaterial(22, 20));
                  toReturn.add(new BuildMaterial(23, 15));
                  toReturn.add(new BuildMaterial(218, 5));
                  toReturn.add(new BuildMaterial(217, 1));
                  toReturn.add(new BuildMaterial(132, 20));
                  toReturn.add(new BuildMaterial(492, 20));
                  break;
               case CLAY_BRICK:
                  toReturn.add(new BuildMaterial(776, 5));
                  toReturn.add(new BuildMaterial(22, 15));
                  toReturn.add(new BuildMaterial(23, 15));
                  toReturn.add(new BuildMaterial(218, 4));
                  toReturn.add(new BuildMaterial(217, 1));
                  toReturn.add(new BuildMaterial(132, 20));
                  toReturn.add(new BuildMaterial(492, 25));
                  break;
               case SLATE_SLAB:
                  toReturn.add(new BuildMaterial(771, 2));
                  toReturn.add(new BuildMaterial(22, 15));
                  toReturn.add(new BuildMaterial(23, 15));
                  toReturn.add(new BuildMaterial(218, 4));
                  toReturn.add(new BuildMaterial(217, 1));
                  toReturn.add(new BuildMaterial(132, 20));
                  toReturn.add(new BuildMaterial(492, 30));
                  break;
               case STONE_BRICK:
                  toReturn.add(new BuildMaterial(22, 15));
                  toReturn.add(new BuildMaterial(23, 15));
                  toReturn.add(new BuildMaterial(218, 4));
                  toReturn.add(new BuildMaterial(217, 1));
                  toReturn.add(new BuildMaterial(132, 25));
                  toReturn.add(new BuildMaterial(492, 25));
                  break;
               case SANDSTONE_SLAB:
                  toReturn.add(new BuildMaterial(1124, 2));
                  toReturn.add(new BuildMaterial(22, 15));
                  toReturn.add(new BuildMaterial(23, 15));
                  toReturn.add(new BuildMaterial(218, 4));
                  toReturn.add(new BuildMaterial(217, 1));
                  toReturn.add(new BuildMaterial(132, 20));
                  toReturn.add(new BuildMaterial(492, 30));
                  break;
               case STONE_SLAB:
                  toReturn.add(new BuildMaterial(406, 2));
                  toReturn.add(new BuildMaterial(22, 15));
                  toReturn.add(new BuildMaterial(23, 15));
                  toReturn.add(new BuildMaterial(218, 4));
                  toReturn.add(new BuildMaterial(217, 1));
                  toReturn.add(new BuildMaterial(132, 20));
                  toReturn.add(new BuildMaterial(492, 30));
                  break;
               case MARBLE_SLAB:
                  toReturn.add(new BuildMaterial(787, 2));
                  toReturn.add(new BuildMaterial(22, 15));
                  toReturn.add(new BuildMaterial(23, 15));
                  toReturn.add(new BuildMaterial(218, 4));
                  toReturn.add(new BuildMaterial(217, 1));
                  toReturn.add(new BuildMaterial(132, 20));
                  toReturn.add(new BuildMaterial(492, 30));
                  break;
               default:
                  logger.log(
                     Level.WARNING,
                     "Someone tried to make a staircase with an opening but the material choice was not supported (" + material.toString() + ")"
                  );
            }
         }
      } catch (NoSuchTemplateException var4) {
         logger.log(Level.WARNING, "FloorBehaviour.getRequiredMaterialsAtState trying to use material that have a non existing template.", (Throwable)var4);
      }

      return toReturn;
   }

   static List<BuildMaterial> getRequiredMaterialsFor(Floor floor) {
      return floor.getType() == StructureConstants.FloorType.ROOF
         ? getRequiredMaterialsForRoof(floor.getMaterial())
         : getRequiredMaterialsForFloor(floor.getType(), floor.getMaterial());
   }

   static List<BuildMaterial> getRequiredMaterialsForFloor(Floor floor) {
      return getRequiredMaterialsForFloor(floor.getType(), floor.getMaterial());
   }

   public static List<BuildMaterial> getRequiredMaterialsAtState(Floor floor) {
      return floor.getType() == StructureConstants.FloorType.ROOF ? getRequiredMaterialsAtStateForRoof(floor) : getRequiredMaterialsAtStateForFloor(floor);
   }

   public static List<BuildMaterial> getRequiredMaterialsAtStateForRoof(Floor floor) {
      List<BuildMaterial> mats = getRequiredMaterialsForRoof(floor.getMaterial());

      for(BuildMaterial mat : mats) {
         int qty = mat.getTotalQuantityRequired();
         if (floor.getState() > 0) {
            qty -= floor.getState();
         } else if (qty < 0) {
            qty = 0;
         }

         mat.setNeededQuantity(qty);
      }

      return mats;
   }

   public static List<BuildMaterial> getRequiredMaterialsAtStateForFloor(Floor floor) {
      List<BuildMaterial> mats = getRequiredMaterialsForFloor(floor.getType(), floor.getMaterial());

      for(BuildMaterial mat : mats) {
         int qty = mat.getTotalQuantityRequired();
         if (floor.getState() > 0) {
            qty -= floor.getState();
         } else if (qty < 0) {
            qty = 0;
         }

         mat.setNeededQuantity(qty);
      }

      return mats;
   }

   static final boolean isOkToBuild(Creature performer, Item tool, Floor floor, int floorLevel, boolean roof) {
      if (tool == null) {
         performer.getCommunicator().sendNormalServerMessage("You need to activate a building tool if you want to build something.");
         return false;
      } else if (floor == null) {
         performer.getCommunicator().sendNormalServerMessage("You fail to focus, and cannot find that floor.");
         return false;
      } else {
         StructureConstants.FloorMaterial floorMaterial = floor.getMaterial();
         String nameOfWhatIsBeingBuilt = floor.getName();
         if (!hasValidTool(floor.getMaterial(), tool)) {
            performer.getCommunicator().sendNormalServerMessage("You need to activate the correct building tool if you want to build that.");
            return false;
         } else {
            Skill buildSkill = getBuildSkill(floor.getType(), floorMaterial, performer);
            if (!mayPlanAtLevel(performer, floorLevel, buildSkill, roof)) {
               return false;
            } else if (buildSkill.getKnowledge(0.0) < (double)getRequiredBuildSkillForFloorType(floorMaterial)) {
               if (floor.getMaterial() == StructureConstants.FloorMaterial.STANDALONE) {
                  performer.getCommunicator()
                     .sendNormalServerMessage(
                        "You need higher "
                           + buildSkill.getName()
                           + " skill to build "
                           + nameOfWhatIsBeingBuilt
                           + " with "
                           + floor.getMaterial().getName()
                           + "."
                     );
               } else {
                  performer.getCommunicator()
                     .sendNormalServerMessage(
                        "You need higher " + buildSkill.getName() + " skill to build " + floor.getMaterial().getName() + " " + nameOfWhatIsBeingBuilt + "."
                     );
               }

               return false;
            } else {
               return true;
            }
         }
      }
   }

   public static final boolean mayPlanAtLevel(Creature performer, int floorLevel, Skill buildSkill, boolean roof) {
      return mayPlanAtLevel(performer, floorLevel, buildSkill, roof, true);
   }

   public static final boolean mayPlanAtLevel(Creature performer, int floorLevel, Skill buildSkill, boolean roof, boolean sendMessage) {
      if (buildSkill.getKnowledge(0.0) < (double)getRequiredBuildSkillForFloorLevel(floorLevel, roof)) {
         if (sendMessage) {
            performer.getCommunicator().sendNormalServerMessage("You need higher " + buildSkill.getName() + " skill to build at that height.");
         }

         return false;
      } else {
         return true;
      }
   }

   private static final boolean floorBuilding(Action act, Creature performer, Item source, Floor floor, short action, float counter) {
      if (performer.isFighting()) {
         performer.getCommunicator().sendNormalServerMessage("You cannot do that while in combat.");
         return true;
      } else if (!isOkToBuild(performer, source, floor, floor.getFloorLevel(), floor.isRoof())) {
         performer.getCommunicator().sendActionResult(false);
         return true;
      } else {
         int time = 10;
         boolean insta = (Servers.isThisATestServer() || performer.getPower() >= 4)
            && performer.getPower() > 1
            && (source.getTemplateId() == 315 || source.getTemplateId() == 176);
         if (floor.isFinished()) {
            performer.getCommunicator().sendNormalServerMessage("The " + floor.getName() + " is finished already.");
            performer.getCommunicator().sendActionResult(false);
            return true;
         } else if (!Methods.isActionAllowed(performer, (short)116, floor.getTileX(), floor.getTileY())) {
            return true;
         } else {
            if (counter == 1.0F) {
               Structure structure;
               try {
                  structure = Structures.getStructure(floor.getStructureId());
               } catch (NoSuchStructureException var20) {
                  logger.log(Level.WARNING, var20.getMessage(), (Throwable)var20);
                  performer.getCommunicator().sendNormalServerMessage("Your sensitive mind notices a wrongness in the fabric of space.");
                  performer.getCommunicator().sendActionResult(false);
                  return true;
               }

               if (!MethodsStructure.mayModifyStructure(performer, structure, floor.getTile(), action)) {
                  performer.getCommunicator().sendNormalServerMessage("You need permission in order to make modifications to this structure.");
                  performer.getCommunicator().sendActionResult(false);
                  return true;
               }

               if (!advanceNextState(performer, floor, act, true) && !insta) {
                  String message = buildRequiredMaterialString(floor, false);
                  if (floor.getType() == StructureConstants.FloorType.WIDE_STAIRCASE) {
                     performer.getCommunicator()
                        .sendNormalServerMessage(
                           "You need " + message + " to start building the " + floor.getName() + " with " + floor.getMaterial().getName().toLowerCase() + "."
                        );
                  } else {
                     performer.getCommunicator()
                        .sendNormalServerMessage(
                           "You need " + message + " to start building the " + floor.getName() + " of " + floor.getMaterial().getName().toLowerCase() + "."
                        );
                  }

                  performer.getCommunicator().sendActionResult(false);
                  return true;
               }

               Skill buildSkill = getBuildSkill(floor.getType(), floor.getMaterial(), performer);
               time = Actions.getSlowActionTime(performer, buildSkill, source, 0.0);
               act.setTimeLeft(time);
               performer.getStatus().modifyStamina(-1000.0F);
               damageTool(performer, floor, source);
               Server.getInstance().broadCastAction(performer.getName() + " continues to build a " + floor.getName() + ".", performer, 5);
               performer.getCommunicator().sendNormalServerMessage("You continue to build a " + floor.getName() + ".");
               if (!insta) {
                  performer.sendActionControl("Building a " + floor.getName(), true, time);
               }
            } else {
               time = act.getTimeLeft();
               if (act.currentSecond() % 5 == 0) {
                  SoundPlayer.playSound(getBuildSound(floor), floor.getTileX(), floor.getTileY(), performer.isOnSurface(), 1.6F);
                  performer.getStatus().modifyStamina(-1000.0F);
                  damageTool(performer, floor, source);
               }
            }

            if (!(counter * 10.0F > (float)time) && !insta) {
               return false;
            } else {
               String message = buildRequiredMaterialString(floor, false);
               if (!advanceNextState(performer, floor, act, false) && !insta) {
                  if (floor.getType() == StructureConstants.FloorType.WIDE_STAIRCASE) {
                     performer.getCommunicator()
                        .sendNormalServerMessage(
                           "You need " + message + " to build the " + floor.getName() + " with " + floor.getMaterial().getName().toLowerCase() + "."
                        );
                  } else {
                     performer.getCommunicator()
                        .sendNormalServerMessage(
                           "You need " + message + " to build the " + floor.getName() + " of " + floor.getMaterial().getName().toLowerCase() + "."
                        );
                  }

                  performer.getCommunicator().sendActionResult(false);
                  return true;
               } else {
                  double bonus = 0.0;
                  Skill toolSkill = getToolSkill(floor, performer, source);
                  if (toolSkill != null) {
                     toolSkill.skillCheck(10.0, source, 0.0, false, counter);
                     bonus = toolSkill.getKnowledge(source, 0.0) / 10.0;
                  }

                  Skill buildSkill = getBuildSkill(floor.getType(), floor.getMaterial(), performer);
                  double check = buildSkill.skillCheck(buildSkill.getRealKnowledge(), source, bonus, false, counter);
                  floor.buildProgress(1);
                  if (WurmPermissions.mayUseGMWand(performer)
                     && (source.getTemplateId() == 315 || source.getTemplateId() == 176)
                     && (Servers.isThisATestServer() || performer.getPower() >= 4)) {
                     if (!Servers.isThisATestServer()) {
                        performer.sendToLoggers(
                           "Building floor with GM powers at ["
                              + floor.getTile().getTileX()
                              + ","
                              + floor.getTile().getTileY()
                              + "] at floor level "
                              + floor.getFloorLevel()
                        );
                     }

                     floor.setFloorState(StructureConstants.FloorState.COMPLETED);
                  }

                  Server.getInstance().broadCastAction(performer.getName() + " attaches " + message + " to a " + floor.getName() + ".", performer, 5);
                  performer.getCommunicator().sendNormalServerMessage("You attach " + message + " to a " + floor.getName() + ".");
                  float oldql = floor.getQualityLevel();
                  float qlevel = MethodsStructure.calculateNewQualityLevel(act.getPower(), buildSkill.getKnowledge(0.0), oldql, getTotalMaterials(floor));
                  qlevel = Math.max(1.0F, qlevel);
                  floor.setQualityLevel(qlevel);
                  if (floor.getState() >= getFinishedState(floor)) {
                     if (insta) {
                        floor.setQualityLevel(80.0F);
                     }

                     floor.setFloorState(StructureConstants.FloorState.COMPLETED);
                     VolaTile floorTile = Zones.getOrCreateTile(floor.getTileX(), floor.getTileY(), floor.getLayer() >= 0);
                     floorTile.updateFloor(floor);
                     String floorName = Character.toUpperCase(floor.getName().charAt(0)) + floor.getName().substring(1);
                     performer.getCommunicator().sendNormalServerMessage(floorName + " completed!");
                  }

                  try {
                     floor.save();
                  } catch (IOException var19) {
                     logger.log(Level.WARNING, var19.getMessage(), (Throwable)var19);
                  }

                  if (floor.isFinished()) {
                     performer.getCommunicator().sendRemoveFromCreationWindow(floor.getId());
                  }

                  performer.getCommunicator().sendActionResult(true);
                  return true;
               }
            }
         }
      }
   }

   public static final float getRequiredBuildSkillForFloorLevel(int floorLevel, boolean roof) {
      int fLevel = roof ? floorLevel - 1 : floorLevel;
      if (fLevel <= 0) {
         return 5.0F;
      } else {
         switch(fLevel) {
            case 1:
               return 21.0F;
            case 2:
               return 30.0F;
            case 3:
               return 39.0F;
            case 4:
               return 47.0F;
            case 5:
               return 55.0F;
            case 6:
               return 63.0F;
            case 7:
               return 70.0F;
            case 8:
               return 77.0F;
            case 9:
               return 83.0F;
            case 10:
               return 88.0F;
            case 11:
               return 92.0F;
            case 12:
               return 95.0F;
            case 13:
               return 97.0F;
            case 14:
               return 98.0F;
            case 15:
               return 99.0F;
            default:
               return 200.0F;
         }
      }
   }

   public static final float getRequiredBuildSkillForFloorType(StructureConstants.FloorMaterial floorMaterial) {
      switch(floorMaterial) {
         case WOOD:
         case STANDALONE:
            return 5.0F;
         case CLAY_BRICK:
            return 25.0F;
         case SLATE_SLAB:
            return 30.0F;
         case STONE_BRICK:
            return 21.0F;
         case SANDSTONE_SLAB:
            return 21.0F;
         case STONE_SLAB:
            return 21.0F;
         case MARBLE_SLAB:
            return 40.0F;
         case THATCH:
            return 21.0F;
         case METAL_IRON:
         case METAL_COPPER:
         case METAL_STEEL:
         case METAL_SILVER:
         case METAL_GOLD:
            return 99.0F;
         default:
            return 1.0F;
      }
   }

   public static final Skill getBuildSkill(StructureConstants.FloorType floorType, StructureConstants.FloorMaterial floorMaterial, Creature performer) {
      int primSkillTemplate;
      if (floorType == StructureConstants.FloorType.ROOF) {
         primSkillTemplate = getSkillForRoof(floorMaterial);
      } else {
         primSkillTemplate = getSkillForFloor(floorMaterial);
      }

      Skill workSkill = null;

      try {
         workSkill = performer.getSkills().getSkill(primSkillTemplate);
      } catch (NoSuchSkillException var6) {
         workSkill = performer.getSkills().learn(primSkillTemplate, 1.0F);
      }

      return workSkill;
   }

   public static boolean hasValidTool(StructureConstants.FloorMaterial floorMaterial, Item source) {
      if (source != null && floorMaterial != null) {
         int tid = source.getTemplateId();
         boolean hasRightTool = false;
         switch(floorMaterial) {
            case WOOD:
               hasRightTool = tid == 62 || tid == 63;
               break;
            case CLAY_BRICK:
               hasRightTool = tid == 493;
               break;
            case SLATE_SLAB:
               hasRightTool = tid == 493;
               break;
            case STONE_BRICK:
               hasRightTool = tid == 493;
               break;
            case SANDSTONE_SLAB:
               hasRightTool = tid == 493;
               break;
            case STONE_SLAB:
               hasRightTool = tid == 493;
               break;
            case MARBLE_SLAB:
               hasRightTool = tid == 493;
               break;
            case THATCH:
               hasRightTool = tid == 62 || tid == 63;
               break;
            case METAL_IRON:
               hasRightTool = tid == 62 || tid == 63;
               break;
            case METAL_COPPER:
               hasRightTool = tid == 62 || tid == 63;
               break;
            case METAL_STEEL:
               hasRightTool = tid == 62 || tid == 63;
               break;
            case METAL_SILVER:
               hasRightTool = tid == 62 || tid == 63;
               break;
            case METAL_GOLD:
               hasRightTool = tid == 62 || tid == 63;
               break;
            case STANDALONE:
               hasRightTool = tid == 62 || tid == 63;
               break;
            default:
               logger.log(
                  Level.WARNING,
                  "Enum value '" + floorMaterial.toString() + "' added to FloorMaterial but not to a switch statement in method FloorBehaviour.hasValidTool()"
               );
         }

         if (tid == 315) {
            return true;
         } else {
            return tid == 176 ? true : hasRightTool;
         }
      } else {
         return false;
      }
   }

   public static boolean actionDestroyFloor(Action act, Creature performer, Item source, Floor floor, short action, float counter) {
      if (source.getTemplateId() == 824 || source.getTemplateId() == 0) {
         performer.getCommunicator().sendNormalServerMessage("You will not do any damage to the floor with that.");
         return true;
      } else if (floor.isIndestructible()) {
         performer.getCommunicator().sendNormalServerMessage("That " + floor.getName() + " looks indestructable.");
         return true;
      } else if (!Methods.isActionAllowed(performer, act.getNumber(), floor.getTileX(), floor.getTileY())) {
         return true;
      } else if (action == 524 && MethodsHighways.onHighway(floor)) {
         performer.getCommunicator().sendNormalServerMessage("That floor is protected by the highway.");
         return true;
      } else if (floor.getFloorState() == StructureConstants.FloorState.BUILDING) {
         if ((!WurmPermissions.mayUseDeityWand(performer) || source.getTemplateId() != 176)
            && (!WurmPermissions.mayUseGMWand(performer) || source.getTemplateId() != 315)) {
            return MethodsStructure.destroyFloor(action, performer, source, floor, counter);
         } else {
            floor.destroyOrRevertToPlan();
            performer.getCommunicator().sendNormalServerMessage("You remove a " + floor.getName() + " with your magic wand.");
            Server.getInstance().broadCastAction(performer.getName() + " effortlessly removes a " + floor.getName() + " with a magic wand.", performer, 3);
            return true;
         }
      } else if (floor.getFloorState() != StructureConstants.FloorState.COMPLETED) {
         if (floor.getFloorState() == StructureConstants.FloorState.PLANNING) {
            VolaTile vtile = Zones.getOrCreateTile(floor.getTileX(), floor.getTileY(), floor.getLayer() >= 0);
            Structure structure = vtile.getStructure();
            if (structure.wouldCreateFlyingStructureIfRemoved(floor)) {
               performer.getCommunicator().sendNormalServerMessage("Removing that would cause a collapsing section.");
               return true;
            } else {
               floor.destroy();
               performer.getCommunicator().sendNormalServerMessage("You remove a plan for a new floor.");
               Server.getInstance().broadCastAction(performer.getName() + " removes a plan for a new floor.", performer, 3);
               return true;
            }
         } else {
            return true;
         }
      } else if ((!WurmPermissions.mayUseDeityWand(performer) || source.getTemplateId() != 176)
         && (!WurmPermissions.mayUseGMWand(performer) || source.getTemplateId() != 315)) {
         return MethodsStructure.destroyFloor(action, performer, source, floor, counter);
      } else {
         floor.destroyOrRevertToPlan();
         performer.getCommunicator().sendNormalServerMessage("You remove a " + floor.getName() + " with your magic wand.");
         Server.getInstance().broadCastAction(performer.getName() + " effortlessly removes a " + floor.getName() + " with a magic wand.", performer, 3);
         return true;
      }
   }

   @Override
   public boolean action(Action act, Creature performer, boolean onSurface, Floor floor, int encodedTile, short action, float counter) {
      if (action == 523 || action == 522) {
         boolean done = false;
         if (floor.getType() == StructureConstants.FloorType.OPENING && (floor.isFinished() || action == 523)) {
            if (floor.getFloorLevel() == performer.getFloorLevel()) {
               if (action != 523) {
                  return true;
               }
            } else {
               if (floor.getFloorLevel() != performer.getFloorLevel() + 1) {
                  return true;
               }

               if (action != 522) {
                  return true;
               }
            }

            if (performer.getVehicle() != -10L) {
               performer.getCommunicator().sendNormalServerMessage("You can't climb right now.");
               return true;
            } else {
               if (performer.getFollowers().length > 0) {
                  performer.getCommunicator().sendNormalServerMessage("You stop leading.", (byte)3);
                  performer.stopLeading();
               }

               if (counter == 1.0F) {
                  float qx = performer.getPosX() % 4.0F;
                  float qy = performer.getPosY() % 4.0F;
                  boolean getCloser = false;
                  if (performer.getTileX() != floor.getTileX() || performer.getTileY() != floor.getTileY()) {
                     performer.getCommunicator().sendNormalServerMessage("You are too far away to climb that.", (byte)3);
                     return true;
                  }

                  if (floor.getMaterial() == StructureConstants.FloorMaterial.STANDALONE) {
                     switch(floor.getDir()) {
                        case 0:
                           getCloser = qx < 1.0F || qx > 3.0F || qy < 3.0F;
                           break;
                        case 1:
                        case 3:
                        case 5:
                        default:
                           getCloser = true;
                           break;
                        case 2:
                           getCloser = qx > 1.0F || qy < 1.0F || qy > 3.0F;
                           break;
                        case 4:
                           getCloser = qx < 1.0F || qx > 3.0F || qy > 1.0F;
                           break;
                        case 6:
                           getCloser = qx < 1.0F || qy < 1.0F || qy > 3.0F;
                     }
                  } else {
                     getCloser = qx < 1.0F || qx > 3.0F || qy < 1.0F || qy > 3.0F;
                  }

                  if (getCloser) {
                     performer.getCommunicator().sendNormalServerMessage("Move a little bit closer to the ladder.", (byte)3);
                     return true;
                  }

                  performer.sendActionControl("Climbing", true, 22);
                  if (action == 523) {
                     int groundoffset = 3;
                     if (performer.getFloorLevel() - 1 == 0) {
                        VolaTile t = performer.getCurrentTile();
                        if (t.getFloors(-10, 10).length == 0) {
                           groundoffset = 0;
                        }
                     } else {
                        VolaTile t = performer.getCurrentTile();
                        int tfloor = (performer.getFloorLevel() - 1) * 30;
                        if (t.getFloors(tfloor - 10, tfloor + 10).length == 0) {
                           performer.getCommunicator().sendNormalServerMessage("You can't climb down there.", (byte)3);
                           return true;
                        }
                     }

                     performer.getCommunicator().setGroundOffset(groundoffset + (performer.getFloorLevel() - 1) * 30, false);
                  } else if (action == 522) {
                     performer.getCommunicator().setGroundOffset((performer.getFloorLevel() + 1) * 30, false);
                  }
               } else if (counter > 2.0F) {
                  done = true;
               }

               return done;
            }
         } else {
            performer.getCommunicator().sendNormalServerMessage("Move a little bit closer to the ladder.", (byte)3);
            return true;
         }
      } else if (action != 177 && action != 178) {
         if (action == 607) {
            if (!floor.isFinished()) {
               performer.getCommunicator().sendAddFloorRoofToCreationWindow(floor, -10L);
            }

            return true;
         } else {
            return super.action(
               act, performer, floor.getTileX(), floor.getTileY(), onSurface, Zones.getTileIntForTile(floor.getTileX(), floor.getTileY(), 0), action, counter
            );
         }
      } else if (!floor.isNotTurnable()) {
         return MethodsStructure.rotateFloor(performer, floor, counter, act);
      } else {
         performer.getCommunicator().sendNormalServerMessage("Looks like that floor is stuck in place.");
         return true;
      }
   }

   @Override
   public boolean action(Action act, Creature performer, Item source, boolean onSurface, Floor floor, int encodedTile, short action, float counter) {
      if (action == 1) {
         return this.examine(performer, floor);
      } else if (action == 607) {
         if (!floor.isFinished()) {
            performer.getCommunicator().sendAddFloorRoofToCreationWindow(floor, -10L);
         }

         return true;
      } else if (action == 523 || action == 522 || action == 177 || action == 178) {
         return this.action(act, performer, onSurface, floor, encodedTile, action, counter);
      } else if (source == null) {
         return super.action(
            act, performer, floor.getTileX(), floor.getTileY(), onSurface, Zones.getTileIntForTile(floor.getTileX(), floor.getTileY(), 0), action, counter
         );
      } else if (action == 179) {
         if ((source.getTemplateId() == 176 || source.getTemplateId() == 315) && WurmPermissions.mayUseGMWand(performer)) {
            Methods.sendSummonQuestion(performer, source, floor.getTileX(), floor.getTileY(), floor.getStructureId());
         }

         return true;
      } else if (action == 684) {
         if ((source.getTemplateId() == 315 || source.getTemplateId() == 176) && performer.getPower() >= 2) {
            Methods.sendItemRestrictionManagement(performer, floor, floor.getId());
         } else {
            logger.log(
               Level.WARNING, performer.getName() + " hacking the protocol by trying to set the restrictions of " + floor + ", counter: " + counter + '!'
            );
         }

         return true;
      } else if (!isConstructionAction(action)) {
         return super.action(
            act,
            performer,
            source,
            floor.getTileX(),
            floor.getTileY(),
            onSurface,
            floor.getHeightOffset(),
            Zones.getTileIntForTile(floor.getTileX(), floor.getTileY(), 0),
            action,
            counter
         );
      } else if (action == 524 || action == 525) {
         return actionDestroyFloor(act, performer, source, floor, action, counter);
      } else if (action != 193) {
         if (action == 192) {
            return !floor.isNoImprove() ? MethodsStructure.improveFloor(performer, source, floor, counter, act) : true;
         } else if (action == 169) {
            if (floor.getFloorState() != StructureConstants.FloorState.BUILDING) {
               performer.getCommunicator().sendNormalServerMessage("The floor is in an invalid state to be continued.");
               performer.getCommunicator().sendActionResult(false);
               return true;
            } else if (floorBuilding(act, performer, source, floor, action, counter)) {
               performer.getCommunicator().sendAddFloorRoofToCreationWindow(floor, floor.getId());
               return true;
            } else {
               return false;
            }
         } else if (action == 508) {
            if (floor.isRoof()) {
               performer.getCommunicator().sendNormalServerMessage("You can't plan above the " + floor.getName() + ".");
               return true;
            } else {
               return MethodsStructure.floorPlanAbove(
                  performer, source, floor.getTileX(), floor.getTileY(), encodedTile, performer.getLayer(), counter, act, StructureConstants.FloorType.FLOOR
               );
            }
         } else if (action == 507) {
            return MethodsStructure.floorPlanRoof(performer, source, floor.getTileX(), floor.getTileY(), encodedTile, floor.getLayer(), counter, act);
         } else if (action == 514) {
            return MethodsStructure.floorPlanAbove(
               performer, source, floor.getTileX(), floor.getTileY(), encodedTile, performer.getLayer(), counter, act, StructureConstants.FloorType.DOOR
            );
         } else if (action == 515) {
            if (floor.isRoof()) {
               performer.getCommunicator().sendNormalServerMessage("You can't plan above the " + floor.getName() + ".");
               return true;
            } else {
               return MethodsStructure.floorPlanAbove(
                  performer, source, floor.getTileX(), floor.getTileY(), encodedTile, performer.getLayer(), counter, act, StructureConstants.FloorType.OPENING
               );
            }
         } else if (action != 659
            && action != 704
            && action != 705
            && action != 706
            && action != 709
            && action != 710
            && action != 711
            && action != 712
            && action != 713
            && action != 714
            && action != 715) {
            return action - 20000 >= 0 ? this.buildAction(act, performer, source, floor, action, counter) : true;
         } else if (floor.isRoof()) {
            performer.getCommunicator().sendNormalServerMessage("You can't plan above the " + floor.getName() + ".");
            return true;
         } else {
            StructureConstants.FloorType ft;
            if (action == 704) {
               ft = StructureConstants.FloorType.WIDE_STAIRCASE;
            } else if (action == 705) {
               ft = StructureConstants.FloorType.RIGHT_STAIRCASE;
            } else if (action == 706) {
               ft = StructureConstants.FloorType.LEFT_STAIRCASE;
            } else if (action == 709) {
               ft = StructureConstants.FloorType.CLOCKWISE_STAIRCASE;
            } else if (action == 710) {
               ft = StructureConstants.FloorType.ANTICLOCKWISE_STAIRCASE;
            } else if (action == 711) {
               ft = StructureConstants.FloorType.CLOCKWISE_STAIRCASE_WITH;
            } else if (action == 712) {
               ft = StructureConstants.FloorType.ANTICLOCKWISE_STAIRCASE_WITH;
            } else if (action == 713) {
               ft = StructureConstants.FloorType.WIDE_STAIRCASE_RIGHT;
            } else if (action == 714) {
               ft = StructureConstants.FloorType.WIDE_STAIRCASE_LEFT;
            } else if (action == 715) {
               ft = StructureConstants.FloorType.WIDE_STAIRCASE_BOTH;
            } else {
               ft = StructureConstants.FloorType.STAIRCASE;
            }

            return MethodsStructure.floorPlanAbove(performer, source, floor.getTileX(), floor.getTileY(), encodedTile, performer.getLayer(), counter, act, ft);
         }
      } else {
         return (!Servers.localServer.challengeServer || performer.getEnemyPresense() <= 0) && !floor.isNoRepair()
            ? MethodsStructure.repairFloor(performer, source, floor, counter, act)
            : true;
      }
   }

   public static boolean isConstructionAction(short action) {
      if (action - 20000 >= 0) {
         return true;
      } else {
         switch(action) {
            case 169:
            case 192:
            case 193:
            case 507:
            case 508:
            case 514:
            case 515:
            case 524:
            case 525:
            case 659:
            case 704:
            case 705:
            case 706:
            case 709:
            case 710:
            case 711:
            case 712:
            case 713:
            case 714:
            case 715:
               return true;
            default:
               return false;
         }
      }
   }

   private boolean examine(Creature performer, Floor floor) {
      String materials = "";
      if (floor.getFloorState() == StructureConstants.FloorState.BUILDING) {
         materials = buildRequiredMaterialString(floor, true);
         performer.getCommunicator()
            .sendNormalServerMessage(
               "You see a " + floor.getName() + " under construction. The " + floor.getName() + " requires " + materials + " to be finished."
            );
      } else {
         if (floor.getFloorState() == StructureConstants.FloorState.PLANNING) {
            performer.getCommunicator().sendNormalServerMessage("You see plans for a " + floor.getName() + ".");
            return true;
         }

         performer.getCommunicator()
            .sendNormalServerMessage("It is a normal " + floor.getName() + " made of " + getMaterialDescription(floor).toLowerCase() + ".");
      }

      sendQlString(performer, floor);
      return true;
   }

   private static final String getMaterialDescription(Floor floor) {
      if (floor.getType() == StructureConstants.FloorType.ROOF) {
         switch(floor.getMaterial()) {
            case WOOD:
               return "wood shingles";
            case CLAY_BRICK:
               return "pottery shingles";
            case SLATE_SLAB:
               return "slate shingles";
            default:
               return floor.getMaterial().getName().toLowerCase();
         }
      } else {
         return floor.getMaterial().getName();
      }
   }

   static final void sendQlString(Creature performer, Floor floor) {
      performer.getCommunicator().sendNormalServerMessage("QL = " + floor.getCurrentQL() + ", dam=" + floor.getDamage() + ".");
      if (performer.getPower() > 0) {
         performer.getCommunicator()
            .sendNormalServerMessage(
               "id: "
                  + floor.getId()
                  + " "
                  + floor.getTileX()
                  + ","
                  + floor.getTileY()
                  + " height: "
                  + floor.getHeightOffset()
                  + " "
                  + floor.getMaterial().getName()
                  + " "
                  + floor.getType().getName()
                  + " ("
                  + floor.getFloorState().toString().toLowerCase()
                  + ")."
            );
      }
   }

   private static final String buildRequiredMaterialString(Floor floor, boolean detailed) {
      String description = new String();
      int numMats = 0;
      List<BuildMaterial> billOfMaterial = getRequiredMaterialsAtState(floor);
      int maxMats = 0;

      for(BuildMaterial mat : billOfMaterial) {
         if (mat.getNeededQuantity() > 0) {
            ++maxMats;
         }
      }

      for(BuildMaterial mat : billOfMaterial) {
         if (mat.getNeededQuantity() > 0) {
            ++numMats;
            ItemTemplate template = null;

            try {
               template = ItemTemplateFactory.getInstance().getTemplate(mat.getTemplateId());
            } catch (NoSuchTemplateException var10) {
               logger.log(Level.WARNING, var10.getMessage(), (Throwable)var10);
            }

            if (numMats > 1) {
               if (numMats < maxMats) {
                  description = description + ", ";
               } else {
                  description = description + " and ";
               }
            }

            if (template != null) {
               if (detailed) {
                  description = description + mat.getNeededQuantity() + " ";
               }

               if (template.sizeString.length() > 0) {
                  description = description + template.sizeString;
               }

               description = description + (mat.getNeededQuantity() > 1 ? template.getPlural() : template.getName());
            }

            if (description.length() == 0) {
               description = "unknown quantities of unknown materials";
            }
         }
      }

      if (description.length() == 0) {
         description = "no materials";
      }

      return description;
   }

   private static String getBuildSound(Floor floor) {
      String soundToPlay = "";
      switch(floor.getMaterial()) {
         case WOOD:
         case THATCH:
         default:
            soundToPlay = Server.rand.nextInt(2) == 0 ? "sound.work.carpentry.mallet1" : "sound.work.carpentry.mallet2";
            break;
         case CLAY_BRICK:
         case SLATE_SLAB:
         case STONE_BRICK:
         case SANDSTONE_SLAB:
         case STONE_SLAB:
         case MARBLE_SLAB:
            soundToPlay = "sound.work.masonry";
            break;
         case METAL_IRON:
         case METAL_COPPER:
         case METAL_STEEL:
         case METAL_SILVER:
         case METAL_GOLD:
            soundToPlay = "sound.work.smithing.metal";
      }

      return soundToPlay;
   }

   private static void damageTool(Creature performer, Floor floor, Item source) {
      if (source.getTemplateId() == 63) {
         source.setDamage(source.getDamage() + 0.0015F * source.getDamageModifier());
      } else if (source.getTemplateId() == 62) {
         source.setDamage(source.getDamage() + 3.0E-4F * source.getDamageModifier());
      } else if (source.getTemplateId() == 493) {
         source.setDamage(source.getDamage() + 5.0E-4F * source.getDamageModifier());
      }
   }

   private static Skill getToolSkill(Floor floor, Creature performer, Item source) {
      Skill toolSkill = null;

      try {
         toolSkill = performer.getSkills().getSkill(source.getPrimarySkill());
      } catch (NoSuchSkillException var7) {
         try {
            toolSkill = performer.getSkills().learn(source.getPrimarySkill(), 1.0F);
         } catch (NoSuchSkillException var6) {
         }
      }

      return toolSkill;
   }
}

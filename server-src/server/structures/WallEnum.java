package com.wurmonline.server.structures;

import com.wurmonline.server.Servers;
import com.wurmonline.server.behaviours.ActionEntry;
import com.wurmonline.server.behaviours.FloorBehaviour;
import com.wurmonline.server.behaviours.WurmPermissions;
import com.wurmonline.server.creatures.Creature;
import com.wurmonline.server.items.Item;
import com.wurmonline.server.skills.Skill;
import com.wurmonline.shared.constants.StructureMaterialEnum;
import com.wurmonline.shared.constants.StructureTypeEnum;
import java.util.ArrayList;
import java.util.List;

public enum WallEnum {
   WALL_WOOD((short)612, StructureTypeEnum.SOLID, StructureMaterialEnum.WOOD, "Wooden wall", "building wall", (short)60, false),
   WALL_STONE((short)617, StructureTypeEnum.SOLID, StructureMaterialEnum.STONE, "Stone wall", "building wall", (short)60, false),
   WALL_PLAIN_STONE((short)648, StructureTypeEnum.SOLID, StructureMaterialEnum.PLAIN_STONE, "Plain stone wall", "building wall", (short)60, false),
   WALL_TIMBER_FRAMED((short)622, StructureTypeEnum.SOLID, StructureMaterialEnum.TIMBER_FRAMED, "Timber framed wall", "building wall", (short)60, false),
   WALL_SLATE((short)772, StructureTypeEnum.SOLID, StructureMaterialEnum.SLATE, "Slate wall", "building wall", (short)60, false),
   WALL_ROUNDED_STONE((short)784, StructureTypeEnum.SOLID, StructureMaterialEnum.ROUNDED_STONE, "Rounded stone wall", "building wall", (short)60, false),
   WALL_POTTERY((short)796, StructureTypeEnum.SOLID, StructureMaterialEnum.POTTERY, "Pottery wall", "building wall", (short)60, false),
   WALL_SANDSTONE((short)808, StructureTypeEnum.SOLID, StructureMaterialEnum.SANDSTONE, "Sandstone wall", "building wall", (short)60, false),
   WALL_MARBLE((short)820, StructureTypeEnum.SOLID, StructureMaterialEnum.MARBLE, "Marble wall", "building wall", (short)60, false),
   WINDOW_WOOD((short)613, StructureTypeEnum.WINDOW, StructureMaterialEnum.WOOD, "Wooden window", "building window", (short)60, false),
   WINDOW_STONE((short)618, StructureTypeEnum.WINDOW, StructureMaterialEnum.STONE, "Stone window", "building window", (short)60, false),
   WINDOW_PLAIN_STONE((short)649, StructureTypeEnum.WINDOW, StructureMaterialEnum.PLAIN_STONE, "Plain stone window", "building window", (short)60, false),
   WINDOW_TIMBER_FRAMED((short)623, StructureTypeEnum.WINDOW, StructureMaterialEnum.TIMBER_FRAMED, "Timber framed window", "building window", (short)60, false),
   WINDOW_SLATE((short)773, StructureTypeEnum.WINDOW, StructureMaterialEnum.SLATE, "Slate window", "building window", (short)60, false),
   WINDOW_ROUNDED_STONE((short)785, StructureTypeEnum.WINDOW, StructureMaterialEnum.ROUNDED_STONE, "Rounded stone window", "building window", (short)60, false),
   WINDOW_POTTERY((short)797, StructureTypeEnum.WINDOW, StructureMaterialEnum.POTTERY, "Pottery window", "building window", (short)60, false),
   WINDOW_SANDSTONE((short)809, StructureTypeEnum.WINDOW, StructureMaterialEnum.SANDSTONE, "Sandstone window", "building window", (short)60, false),
   WINDOW_MARBLE((short)821, StructureTypeEnum.WINDOW, StructureMaterialEnum.MARBLE, "Marble window", "building window", (short)60, false),
   NARROW_WINDOW_PLAIN_STONE(
      (short)650, StructureTypeEnum.NARROW_WINDOW, StructureMaterialEnum.PLAIN_STONE, "Plain stone window narrow", "building narrow window", (short)60, false
   ),
   NARROW_WINDOW_SLATE(
      (short)774, StructureTypeEnum.NARROW_WINDOW, StructureMaterialEnum.SLATE, "Slate window narrow", "building narrow window", (short)60, false
   ),
   NARROW_WINDOW_ROUNDED_STONE(
      (short)786,
      StructureTypeEnum.NARROW_WINDOW,
      StructureMaterialEnum.ROUNDED_STONE,
      "Rounded stone window narrow",
      "building narrow window",
      (short)60,
      false
   ),
   NARROW_WINDOW_POTTERY(
      (short)798, StructureTypeEnum.NARROW_WINDOW, StructureMaterialEnum.POTTERY, "Pottery window narrow", "building narrow window", (short)60, false
   ),
   NARROW_WINDOW_SANDSTONE(
      (short)810, StructureTypeEnum.NARROW_WINDOW, StructureMaterialEnum.SANDSTONE, "Sandstone window narrow", "building narrow window", (short)60, false
   ),
   NARROW_WINDOW_MARBLE(
      (short)822, StructureTypeEnum.NARROW_WINDOW, StructureMaterialEnum.MARBLE, "Marble window narrow", "building narrow window", (short)60, false
   ),
   WIDE_WINDOW_WOOD((short)680, StructureTypeEnum.WIDE_WINDOW, StructureMaterialEnum.WOOD, "Wooden window wide", "building wide window", (short)60, false),
   DOOR_WOOD((short)614, StructureTypeEnum.DOOR, StructureMaterialEnum.WOOD, "Wooden door", "building door", (short)60, true),
   DOOR_STONE((short)619, StructureTypeEnum.DOOR, StructureMaterialEnum.STONE, "Stone door", "building door", (short)60, true),
   DOOR_PLAIN_STONE((short)651, StructureTypeEnum.DOOR, StructureMaterialEnum.PLAIN_STONE, "Plain stone door", "building door", (short)60, true),
   DOOR_TIMBER_FRAMED((short)624, StructureTypeEnum.DOOR, StructureMaterialEnum.TIMBER_FRAMED, "Timber framed door", "building door", (short)60, true),
   DOOR_SLATE((short)775, StructureTypeEnum.DOOR, StructureMaterialEnum.SLATE, "Slate door", "building door", (short)60, true),
   DOOR_ROUNDED_STONE((short)787, StructureTypeEnum.DOOR, StructureMaterialEnum.ROUNDED_STONE, "Rounded stone door", "building door", (short)60, true),
   DOOR_POTTERY((short)799, StructureTypeEnum.DOOR, StructureMaterialEnum.POTTERY, "Pottery door", "building door", (short)60, true),
   DOOR_SANDSTONE((short)811, StructureTypeEnum.DOOR, StructureMaterialEnum.SANDSTONE, "Sandstone door", "building door", (short)60, true),
   DOOR_MARBLE((short)823, StructureTypeEnum.DOOR, StructureMaterialEnum.MARBLE, "Marble door", "building door", (short)60, true),
   DOUBLE_DOOR_WOOD((short)615, StructureTypeEnum.DOUBLE_DOOR, StructureMaterialEnum.WOOD, "Wooden door double", "building double door", (short)60, true),
   DOUBLE_DOOR_STONE((short)620, StructureTypeEnum.DOUBLE_DOOR, StructureMaterialEnum.STONE, "Stone door double", "building double door", (short)60, true),
   DOUBLE_DOOR_PLAIN_STONE(
      (short)652, StructureTypeEnum.DOUBLE_DOOR, StructureMaterialEnum.PLAIN_STONE, "Plain stone door double", "building double door", (short)60, true
   ),
   DOUBLE_DOOR_TIMBER_FRAMED(
      (short)625, StructureTypeEnum.DOUBLE_DOOR, StructureMaterialEnum.TIMBER_FRAMED, "Timber framed door double", "building double door", (short)60, true
   ),
   DOUBLE_DOOR_SLATE((short)776, StructureTypeEnum.DOUBLE_DOOR, StructureMaterialEnum.SLATE, "Slate door double", "building double door", (short)60, true),
   DOUBLE_DOOR_ROUNDED_STONE(
      (short)788, StructureTypeEnum.DOUBLE_DOOR, StructureMaterialEnum.ROUNDED_STONE, "Rounded stone door double", "building double door", (short)60, true
   ),
   DOUBLE_DOOR_POTTERY(
      (short)800, StructureTypeEnum.DOUBLE_DOOR, StructureMaterialEnum.POTTERY, "Pottery door double", "building double door", (short)60, true
   ),
   DOUBLE_DOOR_SANDSTONE(
      (short)812, StructureTypeEnum.DOUBLE_DOOR, StructureMaterialEnum.SANDSTONE, "Sandstone door double", "building double door", (short)60, true
   ),
   DOUBLE_DOOR_MARBLE((short)824, StructureTypeEnum.DOUBLE_DOOR, StructureMaterialEnum.MARBLE, "Marble door double", "building arch", (short)60, true),
   ARCHED_WOOD((short)616, StructureTypeEnum.ARCHED, StructureMaterialEnum.WOOD, "Wooden arch", "building arch", (short)60, true),
   ARCHED_STONE((short)621, StructureTypeEnum.ARCHED, StructureMaterialEnum.STONE, "Stone arch", "building arch", (short)60, true),
   ARCHED_PLAIN_STONE((short)653, StructureTypeEnum.ARCHED, StructureMaterialEnum.PLAIN_STONE, "Plain stone arch", "building arch", (short)60, true),
   ARCHED_TIMBER_FRAMED((short)626, StructureTypeEnum.ARCHED, StructureMaterialEnum.TIMBER_FRAMED, "Timber framed arch", "building arch", (short)60, true),
   ARCHED_SLATE((short)777, StructureTypeEnum.ARCHED, StructureMaterialEnum.SLATE, "Slate arch", "building arch", (short)60, true),
   ARCHED_ROUNDED_STONE((short)789, StructureTypeEnum.ARCHED, StructureMaterialEnum.ROUNDED_STONE, "Rounded stone arch", "building arch", (short)60, true),
   ARCHED_POTTERY((short)801, StructureTypeEnum.ARCHED, StructureMaterialEnum.POTTERY, "Pottery arch", "building arch", (short)60, true),
   ARCHED_SANDSTONE((short)813, StructureTypeEnum.ARCHED, StructureMaterialEnum.SANDSTONE, "Sandstone arch", "building arch", (short)60, true),
   ARCHED_MARBLE((short)825, StructureTypeEnum.ARCHED, StructureMaterialEnum.MARBLE, "Marble arch", "building arch", (short)60, true),
   LEFT_ARCHED_WOOD((short)760, StructureTypeEnum.ARCHED_LEFT, StructureMaterialEnum.WOOD, "Wooden arch left", "building arch left", (short)60, true),
   LEFT_ARCHED_STONE((short)763, StructureTypeEnum.ARCHED_LEFT, StructureMaterialEnum.STONE, "Stone arch left", "building arch left", (short)60, true),
   LEFT_ARCHED_PLAIN_STONE(
      (short)769, StructureTypeEnum.ARCHED_LEFT, StructureMaterialEnum.PLAIN_STONE, "Plain stone arch left", "building arch left", (short)60, true
   ),
   LEFT_ARCHED_TIMBER_FRAMED(
      (short)766, StructureTypeEnum.ARCHED_LEFT, StructureMaterialEnum.TIMBER_FRAMED, "Timber framed arch left", "building arch left", (short)60, true
   ),
   LEFT_ARCHED_SLATE((short)781, StructureTypeEnum.ARCHED_LEFT, StructureMaterialEnum.SLATE, "Slate arch left", "building arch left", (short)60, true),
   LEFT_ARCHED_ROUNDED_STONE(
      (short)793, StructureTypeEnum.ARCHED_LEFT, StructureMaterialEnum.ROUNDED_STONE, "Rounded stone arch left", "building arch left", (short)60, true
   ),
   LEFT_ARCHED_POTTERY((short)805, StructureTypeEnum.ARCHED_LEFT, StructureMaterialEnum.POTTERY, "Pottery arch left", "building arch left", (short)60, true),
   LEFT_ARCHED_SANDSTONE(
      (short)817, StructureTypeEnum.ARCHED_LEFT, StructureMaterialEnum.SANDSTONE, "Sandstone arch left", "building arch left", (short)60, true
   ),
   LEFT_ARCHED_MARBLE((short)829, StructureTypeEnum.ARCHED_LEFT, StructureMaterialEnum.MARBLE, "Marble arch left", "building arch left", (short)60, true),
   RIGHT_ARCHED_WOOD((short)761, StructureTypeEnum.ARCHED_RIGHT, StructureMaterialEnum.WOOD, "Wooden arch right", "building arch right", (short)60, true),
   RIGHT_ARCHED_STONE((short)764, StructureTypeEnum.ARCHED_RIGHT, StructureMaterialEnum.STONE, "Stone arch right", "building arch right", (short)60, true),
   RIGHT_ARCHED_PLAIN_STONE(
      (short)770, StructureTypeEnum.ARCHED_RIGHT, StructureMaterialEnum.PLAIN_STONE, "Plain stone arch right", "building arch right", (short)60, true
   ),
   RIGHT_ARCHED_TIMBER_FRAMED(
      (short)767, StructureTypeEnum.ARCHED_RIGHT, StructureMaterialEnum.TIMBER_FRAMED, "Timber framed arch right", "building arch right", (short)60, true
   ),
   RIGHT_ARCHED_SLATE((short)782, StructureTypeEnum.ARCHED_RIGHT, StructureMaterialEnum.SLATE, "Slate arch right", "building arch right", (short)60, true),
   RIGHT_ARCHED_ROUNDED_STONE(
      (short)794, StructureTypeEnum.ARCHED_RIGHT, StructureMaterialEnum.ROUNDED_STONE, "Rounded stone arch right", "building arch right", (short)60, true
   ),
   RIGHT_ARCHED_POTTERY(
      (short)806, StructureTypeEnum.ARCHED_RIGHT, StructureMaterialEnum.POTTERY, "Pottery arch right", "building arch right", (short)60, true
   ),
   RIGHT_ARCHED_SANDSTONE(
      (short)818, StructureTypeEnum.ARCHED_RIGHT, StructureMaterialEnum.SANDSTONE, "Sandstone arch right", "building arch right", (short)60, true
   ),
   RIGHT_ARCHED_MARBLE((short)830, StructureTypeEnum.ARCHED_RIGHT, StructureMaterialEnum.MARBLE, "Marble arch right", "building arch right", (short)60, true),
   T_ARCHED_WOOD((short)762, StructureTypeEnum.ARCHED_T, StructureMaterialEnum.WOOD, "Wooden arch T", "building arch T", (short)60, true),
   T_ARCHED_STONE((short)765, StructureTypeEnum.ARCHED_T, StructureMaterialEnum.STONE, "Stone arch T", "building arch T", (short)60, true),
   T_ARCHED_PLAIN_STONE((short)771, StructureTypeEnum.ARCHED_T, StructureMaterialEnum.PLAIN_STONE, "Plain stone arch T", "building arch T", (short)60, true),
   T_ARCHED_TIMBER_FRAMED(
      (short)768, StructureTypeEnum.ARCHED_T, StructureMaterialEnum.TIMBER_FRAMED, "Timber framed arch T", "building arch T", (short)60, true
   ),
   T_ARCHED_SLATE((short)783, StructureTypeEnum.ARCHED_T, StructureMaterialEnum.SLATE, "Slate arch T", "building arch T", (short)60, true),
   T_ARCHED_ROUNDED_STONE(
      (short)795, StructureTypeEnum.ARCHED_T, StructureMaterialEnum.ROUNDED_STONE, "Rounded stone arch T", "building arch T", (short)60, true
   ),
   T_ARCHED_POTTERY((short)807, StructureTypeEnum.ARCHED_T, StructureMaterialEnum.POTTERY, "Pottery arch T", "building arch T", (short)60, true),
   T_ARCHED_SANDSTONE((short)819, StructureTypeEnum.ARCHED_T, StructureMaterialEnum.SANDSTONE, "Sandstone arch T", "building arch T", (short)60, true),
   T_ARCHED_MARBLE((short)831, StructureTypeEnum.ARCHED_T, StructureMaterialEnum.MARBLE, "Marble arch T", "building arch T", (short)60, true),
   WALL_PLAN((short)0, StructureTypeEnum.PLAN, StructureMaterialEnum.WOOD, "Wall plan", "planning", (short)60, false),
   PORTCULLIS_PLAIN_STONE(
      (short)655, StructureTypeEnum.PORTCULLIS, StructureMaterialEnum.PLAIN_STONE, "Plain stone portcullis", "building portcullis", (short)60, true
   ),
   PORTCULLIS_STONE((short)657, StructureTypeEnum.PORTCULLIS, StructureMaterialEnum.STONE, "Stone portcullis", "building portcullis", (short)60, true),
   PORTCULLIS_WOOD((short)658, StructureTypeEnum.PORTCULLIS, StructureMaterialEnum.WOOD, "Wooden portcullis", "building portcullis", (short)60, true),
   PORTCULLIS_SLATE((short)778, StructureTypeEnum.PORTCULLIS, StructureMaterialEnum.SLATE, "Slate portcullis", "building portcullis", (short)60, true),
   PORTCULLIS_ROUNDED_STONE(
      (short)790, StructureTypeEnum.PORTCULLIS, StructureMaterialEnum.ROUNDED_STONE, "Rounded stone portcullis", "building portcullis", (short)60, true
   ),
   PORTCULLIS_POTTERY((short)802, StructureTypeEnum.PORTCULLIS, StructureMaterialEnum.POTTERY, "Pottery portcullis", "building portcullis", (short)60, true),
   PORTCULLIS_SANDSTONE(
      (short)814, StructureTypeEnum.PORTCULLIS, StructureMaterialEnum.SANDSTONE, "Sandstone portcullis", "building portcullis", (short)60, true
   ),
   PORTCULLIS_MARBLE((short)826, StructureTypeEnum.PORTCULLIS, StructureMaterialEnum.MARBLE, "Marble portcullis", "building portcullis", (short)60, true),
   BARRED_PLAIN_STONE(
      (short)656, StructureTypeEnum.BARRED, StructureMaterialEnum.PLAIN_STONE, "Plain stone wall barred", "building wall barred", (short)60, false
   ),
   BARRED_SLATE((short)779, StructureTypeEnum.BARRED, StructureMaterialEnum.SLATE, "Slate wall barred", "building wall barred", (short)60, true),
   BARRED_ROUNDED_STONE(
      (short)791, StructureTypeEnum.BARRED, StructureMaterialEnum.ROUNDED_STONE, "Rounded stone wall barred", "building wall barred", (short)60, true
   ),
   BARRED_POTTERY((short)803, StructureTypeEnum.BARRED, StructureMaterialEnum.POTTERY, "Pottery wall barred", "building wall barred", (short)60, true),
   BARRED_SANDSTONE((short)815, StructureTypeEnum.BARRED, StructureMaterialEnum.SANDSTONE, "Sandstone wall barred", "building wall barred", (short)60, true),
   BARRED_MARBLE((short)827, StructureTypeEnum.BARRED, StructureMaterialEnum.MARBLE, "Marble wall barred", "building wall barred", (short)60, true),
   TIMBER_FRAMED_JETTY((short)677, StructureTypeEnum.JETTY, StructureMaterialEnum.TIMBER_FRAMED, "Timber framed jetty", "building jetty", (short)60, true),
   ORIEL_DECORATED_STONE((short)678, StructureTypeEnum.ORIEL, StructureMaterialEnum.STONE, "Stone oriel", "building oriel", (short)60, true),
   ORIEL_PLAIN_STONE((short)681, StructureTypeEnum.ORIEL, StructureMaterialEnum.PLAIN_STONE, "Plain stone oriel", "building oriel", (short)60, true),
   ORIEL_SLATE((short)780, StructureTypeEnum.ORIEL, StructureMaterialEnum.SLATE, "Slate oriel", "building oriel", (short)60, true),
   ORIEL_ROUNDED_STONE((short)792, StructureTypeEnum.ORIEL, StructureMaterialEnum.ROUNDED_STONE, "Rounded stone oriel", "building oriel", (short)60, true),
   ORIEL_POTTERY((short)804, StructureTypeEnum.ORIEL, StructureMaterialEnum.POTTERY, "Pottery oriel", "building oriel", (short)60, true),
   ORIEL_SANDSTONE((short)816, StructureTypeEnum.ORIEL, StructureMaterialEnum.SANDSTONE, "Sandstone oriel", "building oriel", (short)60, true),
   ORIEL_MARBLE((short)828, StructureTypeEnum.ORIEL, StructureMaterialEnum.MARBLE, "Marble oriel", "building oriel", (short)60, true),
   CANOPY_WOOD((short)679, StructureTypeEnum.CANOPY_DOOR, StructureMaterialEnum.WOOD, "Wooden canopy", "building canopy", (short)60, true),
   TIMBER_FRAMED_BALCONY(
      (short)676, StructureTypeEnum.BALCONY, StructureMaterialEnum.TIMBER_FRAMED, "Timber framed balcony", "building balcony", (short)60, true
   );

   private final short actionId;
   private final short icon;
   private final StructureTypeEnum type;
   private final StructureMaterialEnum material;
   private final String name;
   private final String actionString;
   private final boolean isDoor;
   private static int[] emptyArr = new int[0];

   private WallEnum(short actionId, StructureTypeEnum type, StructureMaterialEnum material, String name, String actionString, short icon, boolean isDoor) {
      this.type = type;
      this.icon = icon;
      this.material = material;
      this.name = name;
      this.actionString = actionString;
      this.isDoor = isDoor;
      this.actionId = actionId;
   }

   public static List<WallEnum> getWallsByTool(Creature performer, Item tool, boolean needsDoor, boolean hasAFence) {
      StructureMaterialEnum[] mats = getMaterialsFromToolType(tool, performer);
      List<WallEnum> list = new ArrayList<>();

      for(WallEnum en : values()) {
         if (en.getType() != StructureTypeEnum.PLAN && (!hasAFence || Wall.isArched(en.getType()))) {
            for(StructureMaterialEnum mat : mats) {
               if (needsDoor) {
                  if (en.getMaterial() == mat && en.isDoor()) {
                     list.add(en);
                  }
               } else if (en.getMaterial() == mat) {
                  list.add(en);
               }
            }
         }
      }

      return list;
   }

   public static List<WallEnum> getWallsByToolAndMaterial(Creature performer, Item tool, boolean needsDoor, boolean hasAFence, StructureMaterialEnum material) {
      StructureMaterialEnum[] mats = getMaterialsFromToolType(tool, performer);
      List<WallEnum> list = new ArrayList<>();

      for(WallEnum en : values()) {
         if (en.getType() != StructureTypeEnum.PLAN && en.getMaterial() == material && (!hasAFence || Wall.isArched(en.getType()))) {
            for(StructureMaterialEnum mat : mats) {
               if (needsDoor) {
                  if (en.getMaterial() == mat && en.isDoor()) {
                     list.add(en);
                  }
               } else if (en.getMaterial() == mat) {
                  list.add(en);
               }
            }
         }
      }

      return list;
   }

   public static final boolean canBuildWall(Wall wall, StructureMaterialEnum material, Creature performer) {
      int skillNumber = getSkillNumber(material);
      Skill skill = performer.getSkills().getSkillOrLearn(skillNumber);
      if (skill == null) {
         return false;
      } else if (skillNumber == 1013 && skill.getKnowledge(0.0) < 30.0) {
         return false;
      } else {
         return !((double)FloorBehaviour.getRequiredBuildSkillForFloorLevel(wall.getFloorLevel(), false) > skill.getKnowledge(0.0));
      }
   }

   public static final boolean canBuildWall(Wall wall, Creature performer) {
      return canBuildWall(wall, wall.getMaterial(), performer);
   }

   public final int getSkillNumber() {
      return getSkillNumber(this.material);
   }

   public static final int getSkillNumber(StructureMaterialEnum material) {
      return material != StructureMaterialEnum.STONE
            && material != StructureMaterialEnum.PLAIN_STONE
            && material != StructureMaterialEnum.SLATE
            && material != StructureMaterialEnum.ROUNDED_STONE
            && material != StructureMaterialEnum.POTTERY
            && material != StructureMaterialEnum.SANDSTONE
            && material != StructureMaterialEnum.RENDERED
            && material != StructureMaterialEnum.MARBLE
         ? 1005
         : 1013;
   }

   public static WallEnum getWall(StructureTypeEnum type, StructureMaterialEnum material) {
      for(WallEnum en : values()) {
         if (en.getType() == type && en.getMaterial() == material) {
            return en;
         }
      }

      return WALL_PLAN;
   }

   public static WallEnum getWallByActionId(short actionId) {
      for(WallEnum en : values()) {
         if (en.getActionId() == actionId) {
            return en;
         }
      }

      return WALL_PLAN;
   }

   public static int[] getTotalMaterialsNeeded(WallEnum en) {
      return new int[0];
   }

   public static int[] getMaterialsNeeded(Wall wall) {
      int needed = wall.getFinalState().state - wall.getState().state;
      if (wall.isHalfArch() && wall.isWood() && wall.getState().state <= 1) {
         return new int[]{217, 1, 22, needed - 1};
      } else if (wall.isWood()) {
         return new int[]{22, needed};
      } else if (wall.isTimberFramed()) {
         if (needed > 20) {
            return new int[]{860, needed - 20, 620, 10, 130, 20};
         } else {
            return needed > 10 ? new int[]{620, needed - 10, 130, needed - 10 + 10} : new int[]{130, needed};
         }
      } else {
         return new int[]{wall.getBrickFromType(), needed, 492, needed};
      }
   }

   public final int[] getTotalMaterialsNeeded() {
      if (Wall.isHalfArch(this.type)) {
         if (this.material == StructureMaterialEnum.WOOD) {
            return new int[]{860, 1, 22, 19, 217, 1};
         } else {
            return this.material == StructureMaterialEnum.TIMBER_FRAMED
               ? new int[]{860, 6, 620, 10, 130, 20}
               : new int[]{681, 1, Wall.getBrickFromType(this.material), 20, 492, 20};
         }
      } else if (this.material == StructureMaterialEnum.WOOD) {
         return new int[]{22, 20, 217, 1};
      } else {
         return this.material == StructureMaterialEnum.TIMBER_FRAMED
            ? new int[]{860, 5, 620, 10, 130, 20}
            : new int[]{Wall.getBrickFromType(this.material), 20, 492, 20};
      }
   }

   public static boolean isCorrectTool(WallEnum wall, Creature performer, Item tool) {
      if (tool == null) {
         return false;
      } else {
         for(Integer t : getToolsForWall(wall, performer)) {
            if (t == tool.getTemplateId()) {
               return true;
            }
         }

         return false;
      }
   }

   public final boolean isCorrectToolForType(Item tool, Creature performer) {
      List<Integer> list = getToolsForWall(this, performer);
      return list.contains(tool.getTemplateId());
   }

   public static List<Integer> getToolsForWall(WallEnum wall, Creature performer) {
      List<Integer> list = new ArrayList<>();
      if (wall.getType() == StructureTypeEnum.PLAN) {
         list.add(493);
         list.add(62);
         list.add(63);
         if (performer != null) {
            if (performer.getPower() >= 2 && Servers.isThisATestServer()) {
               list.add(315);
            }

            if (WurmPermissions.mayUseGMWand(performer)) {
               list.add(176);
            }
         }
      } else if (wall.getMaterial() != StructureMaterialEnum.WOOD && wall.getMaterial() != StructureMaterialEnum.TIMBER_FRAMED) {
         list.add(493);
         if (performer != null && WurmPermissions.mayUseGMWand(performer)) {
            list.add(176);
         }
      } else {
         list.add(62);
         list.add(63);
         if (performer != null) {
            if (performer.getPower() >= 2 && Servers.isThisATestServer()) {
               list.add(315);
            }

            if (WurmPermissions.mayUseGMWand(performer)) {
               list.add(176);
            }
         }
      }

      return list;
   }

   public static StructureMaterialEnum[] getMaterialsFromToolType(Item tool, Creature performer) {
      switch(tool.getTemplateId()) {
         case 62:
         case 63:
            return new StructureMaterialEnum[]{StructureMaterialEnum.TIMBER_FRAMED, StructureMaterialEnum.WOOD};
         case 176:
            if (WurmPermissions.mayUseGMWand(performer)) {
               return new StructureMaterialEnum[]{
                  StructureMaterialEnum.MARBLE,
                  StructureMaterialEnum.PLAIN_STONE,
                  StructureMaterialEnum.RENDERED,
                  StructureMaterialEnum.POTTERY,
                  StructureMaterialEnum.ROUNDED_STONE,
                  StructureMaterialEnum.SANDSTONE,
                  StructureMaterialEnum.SLATE,
                  StructureMaterialEnum.STONE,
                  StructureMaterialEnum.TIMBER_FRAMED,
                  StructureMaterialEnum.WOOD
               };
            }

            return new StructureMaterialEnum[0];
         case 315:
            if (performer.getPower() >= 2 && Servers.isThisATestServer()) {
               return new StructureMaterialEnum[]{StructureMaterialEnum.WOOD};
            }

            return new StructureMaterialEnum[0];
         case 493:
            return new StructureMaterialEnum[]{
               StructureMaterialEnum.MARBLE,
               StructureMaterialEnum.PLAIN_STONE,
               StructureMaterialEnum.RENDERED,
               StructureMaterialEnum.POTTERY,
               StructureMaterialEnum.ROUNDED_STONE,
               StructureMaterialEnum.SANDSTONE,
               StructureMaterialEnum.SLATE,
               StructureMaterialEnum.STONE
            };
         default:
            return new StructureMaterialEnum[0];
      }
   }

   public final boolean isDoor() {
      return this.isDoor;
   }

   public final boolean isTimber() {
      return this.material == StructureMaterialEnum.TIMBER_FRAMED;
   }

   public final boolean isUndecoratedStone() {
      return this.material == StructureMaterialEnum.PLAIN_STONE;
   }

   public final StructureTypeEnum getType() {
      return this.type;
   }

   public final StructureMaterialEnum getMaterial() {
      return this.material;
   }

   public final String getName() {
      return this.name;
   }

   public final short getIcon() {
      return this.icon;
   }

   public final short getActionId() {
      return this.actionId;
   }

   public final ActionEntry createActionEntry() {
      return ActionEntry.createEntry(this.actionId, this.name, this.actionString, emptyArr);
   }
}

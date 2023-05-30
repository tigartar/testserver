package com.wurmonline.shared.constants;

public final class WallConstants {
   public static final byte DECAY_VALUE = 60;

   private WallConstants() {
   }

   public static final short translateWallType(StructureTypeEnum type, String material) {
      boolean isStone = "stone".equals(material);
      boolean isPlainStone = "plain stone".equals(material);
      boolean isTimber = "timber framed".equals(material);
      boolean isSlate = "slate".equals(material);
      boolean isRoundedStone = "rounded stone".equals(material);
      boolean isPottery = "pottery".equals(material);
      boolean isSandstone = "sandstone".equals(material);
      boolean isRendered = "rendered".equals(material);
      boolean isMarble = "marble".equals(material);
      switch(type) {
         case RUBBLE:
            return StructureConstantsEnum.WALL_RUBBLE.value;
         case DOOR:
            if (isStone) {
               return StructureConstantsEnum.WALL_DOOR_STONE_DECORATED.value;
            } else if (isTimber) {
               return StructureConstantsEnum.WALL_DOOR_TIMBER_FRAMED.value;
            } else if (isPlainStone) {
               return StructureConstantsEnum.WALL_DOOR_STONE.value;
            } else if (isSlate) {
               return StructureConstantsEnum.WALL_DOOR_SLATE.value;
            } else if (isRoundedStone) {
               return StructureConstantsEnum.WALL_DOOR_ROUNDED_STONE.value;
            } else if (isPottery) {
               return StructureConstantsEnum.WALL_DOOR_POTTERY.value;
            } else if (isSandstone) {
               return StructureConstantsEnum.WALL_DOOR_SANDSTONE.value;
            } else if (isRendered) {
               return StructureConstantsEnum.WALL_DOOR_RENDERED.value;
            } else {
               if (isMarble) {
                  return StructureConstantsEnum.WALL_DOOR_MARBLE.value;
               }

               return StructureConstantsEnum.WALL_DOOR_WOODEN.value;
            }
         case WINDOW:
            if (isStone) {
               return StructureConstantsEnum.WALL_WINDOW_STONE_DECORATED.value;
            } else if (isTimber) {
               return StructureConstantsEnum.WALL_WINDOW_TIMBER_FRAMED.value;
            } else if (isPlainStone) {
               return StructureConstantsEnum.WALL_WINDOW_STONE.value;
            } else if (isSlate) {
               return StructureConstantsEnum.WALL_WINDOW_SLATE.value;
            } else if (isRoundedStone) {
               return StructureConstantsEnum.WALL_WINDOW_ROUNDED_STONE.value;
            } else if (isPottery) {
               return StructureConstantsEnum.WALL_WINDOW_POTTERY.value;
            } else if (isSandstone) {
               return StructureConstantsEnum.WALL_WINDOW_SANDSTONE.value;
            } else if (isRendered) {
               return StructureConstantsEnum.WALL_WINDOW_RENDERED.value;
            } else {
               if (isMarble) {
                  return StructureConstantsEnum.WALL_WINDOW_MARBLE.value;
               }

               return StructureConstantsEnum.WALL_WINDOW_WOODEN.value;
            }
         case WIDE_WINDOW:
            return StructureConstantsEnum.WALL_WINDOW_WIDE_WOODEN.value;
         case SOLID:
            if (isStone) {
               return StructureConstantsEnum.WALL_SOLID_STONE_DECORATED.value;
            } else if (isPlainStone) {
               return StructureConstantsEnum.WALL_SOLID_STONE.value;
            } else if (isTimber) {
               return StructureConstantsEnum.WALL_SOLID_TIMBER_FRAMED.value;
            } else if (isSlate) {
               return StructureConstantsEnum.WALL_SOLID_SLATE.value;
            } else if (isRoundedStone) {
               return StructureConstantsEnum.WALL_SOLID_ROUNDED_STONE.value;
            } else if (isPottery) {
               return StructureConstantsEnum.WALL_SOLID_POTTERY.value;
            } else if (isSandstone) {
               return StructureConstantsEnum.WALL_SOLID_SANDSTONE.value;
            } else if (isRendered) {
               return StructureConstantsEnum.WALL_SOLID_RENDERED.value;
            } else {
               if (isMarble) {
                  return StructureConstantsEnum.WALL_SOLID_MARBLE.value;
               }

               return StructureConstantsEnum.WALL_SOLID_WOODEN.value;
            }
         case DOUBLE_DOOR:
            if (isStone) {
               return StructureConstantsEnum.WALL_DOUBLE_DOOR_STONE_DECORATED.value;
            } else if (isTimber) {
               return StructureConstantsEnum.WALL_DOUBLE_DOOR_TIMBER_FRAMED.value;
            } else if (isPlainStone) {
               return StructureConstantsEnum.WALL_DOUBLE_DOOR_STONE.value;
            } else if (isSlate) {
               return StructureConstantsEnum.WALL_DOUBLE_DOOR_SLATE.value;
            } else if (isRoundedStone) {
               return StructureConstantsEnum.WALL_DOUBLE_DOOR_ROUNDED_STONE.value;
            } else if (isPottery) {
               return StructureConstantsEnum.WALL_DOUBLE_DOOR_POTTERY.value;
            } else if (isSandstone) {
               return StructureConstantsEnum.WALL_DOUBLE_DOOR_SANDSTONE.value;
            } else if (isRendered) {
               return StructureConstantsEnum.WALL_DOUBLE_DOOR_RENDERED.value;
            } else {
               if (isMarble) {
                  return StructureConstantsEnum.WALL_DOUBLE_DOOR_MARBLE.value;
               }

               return StructureConstantsEnum.WALL_DOUBLE_DOOR_WOODEN.value;
            }
         case ARCHED:
            if (isStone) {
               return StructureConstantsEnum.WALL_DOOR_ARCHED_STONE_DECORATED.value;
            } else if (isTimber) {
               return StructureConstantsEnum.WALL_DOOR_ARCHED_TIMBER_FRAMED.value;
            } else if (isPlainStone) {
               return StructureConstantsEnum.WALL_DOOR_ARCHED_STONE.value;
            } else if (isSlate) {
               return StructureConstantsEnum.WALL_ARCHED_SLATE.value;
            } else if (isRoundedStone) {
               return StructureConstantsEnum.WALL_ARCHED_ROUNDED_STONE.value;
            } else if (isPottery) {
               return StructureConstantsEnum.WALL_ARCHED_POTTERY.value;
            } else if (isSandstone) {
               return StructureConstantsEnum.WALL_ARCHED_SANDSTONE.value;
            } else if (isRendered) {
               return StructureConstantsEnum.WALL_ARCHED_RENDERED.value;
            } else {
               if (isMarble) {
                  return StructureConstantsEnum.WALL_ARCHED_MARBLE.value;
               }

               return StructureConstantsEnum.WALL_DOOR_ARCHED_WOODEN.value;
            }
         case NARROW_WINDOW:
            if (isPlainStone) {
               return StructureConstantsEnum.WALL_PLAIN_NARROW_WINDOW.value;
            } else if (isSlate) {
               return StructureConstantsEnum.WALL_NARROW_WINDOW_SLATE.value;
            } else if (isRoundedStone) {
               return StructureConstantsEnum.WALL_NARROW_WINDOW_ROUNDED_STONE.value;
            } else if (isPottery) {
               return StructureConstantsEnum.WALL_NARROW_WINDOW_POTTERY.value;
            } else if (isSandstone) {
               return StructureConstantsEnum.WALL_NARROW_WINDOW_SANDSTONE.value;
            } else if (isRendered) {
               return StructureConstantsEnum.WALL_NARROW_WINDOW_RENDERED.value;
            } else {
               if (isMarble) {
                  return StructureConstantsEnum.WALL_NARROW_WINDOW_MARBLE.value;
               }

               return StructureConstantsEnum.WALL_PLAIN_NARROW_WINDOW.value;
            }
         case PLAN:
            if (isStone) {
               return StructureConstantsEnum.WALL_SOLID_STONE_PLAN.value;
            } else if (isTimber) {
               return StructureConstantsEnum.WALL_SOLID_TIMBER_FRAMED_PLAN.value;
            } else if (isSlate) {
               return StructureConstantsEnum.WALL_SOLID_STONE_PLAN.value;
            } else if (isRoundedStone) {
               return StructureConstantsEnum.WALL_SOLID_STONE_PLAN.value;
            } else if (isPottery) {
               return StructureConstantsEnum.WALL_SOLID_STONE_PLAN.value;
            } else if (isSandstone) {
               return StructureConstantsEnum.WALL_SOLID_STONE_PLAN.value;
            } else if (isRendered) {
               return StructureConstantsEnum.WALL_SOLID_STONE_PLAN.value;
            } else {
               if (isMarble) {
                  return StructureConstantsEnum.WALL_SOLID_STONE_PLAN.value;
               }

               return StructureConstantsEnum.WALL_SOLID_WOODEN_PLAN.value;
            }
         case PORTCULLIS:
            if (isPlainStone) {
               return StructureConstantsEnum.WALL_PORTCULLIS_STONE.value;
            } else if (isStone) {
               return StructureConstantsEnum.WALL_PORTCULLIS_STONE_DECORATED.value;
            } else if (isSlate) {
               return StructureConstantsEnum.WALL_PORTCULLIS_SLATE.value;
            } else if (isRoundedStone) {
               return StructureConstantsEnum.WALL_PORTCULLIS_ROUNDED_STONE.value;
            } else if (isPottery) {
               return StructureConstantsEnum.WALL_PORTCULLIS_POTTERY.value;
            } else if (isSandstone) {
               return StructureConstantsEnum.WALL_PORTCULLIS_SANDSTONE.value;
            } else if (isRendered) {
               return StructureConstantsEnum.WALL_PORTCULLIS_RENDERED.value;
            } else {
               if (isMarble) {
                  return StructureConstantsEnum.WALL_PORTCULLIS_MARBLE.value;
               }

               return StructureConstantsEnum.WALL_PORTCULLIS_WOOD.value;
            }
         case BARRED:
            if (isPlainStone) {
               return StructureConstantsEnum.WALL_BARRED_STONE.value;
            } else if (isSlate) {
               return StructureConstantsEnum.WALL_BARRED_SLATE.value;
            } else if (isRoundedStone) {
               return StructureConstantsEnum.WALL_BARRED_ROUNDED_STONE.value;
            } else if (isPottery) {
               return StructureConstantsEnum.WALL_BARRED_POTTERY.value;
            } else if (isSandstone) {
               return StructureConstantsEnum.WALL_BARRED_SANDSTONE.value;
            } else if (isRendered) {
               return StructureConstantsEnum.WALL_BARRED_RENDERED.value;
            } else {
               if (isMarble) {
                  return StructureConstantsEnum.WALL_BARRED_MARBLE.value;
               }

               return StructureConstantsEnum.WALL_BARRED_STONE.value;
            }
         case BALCONY:
            return StructureConstantsEnum.WALL_BALCONY_TIMBER_FRAMED.value;
         case JETTY:
            return StructureConstantsEnum.WALL_JETTY_TIMBER_FRAMED.value;
         case ORIEL:
            if (isPlainStone) {
               return StructureConstantsEnum.WALL_ORIEL_STONE_PLAIN.value;
            } else if (isStone) {
               return StructureConstantsEnum.WALL_ORIEL_STONE_DECORATED.value;
            } else if (isSlate) {
               return StructureConstantsEnum.WALL_ORIEL_SLATE.value;
            } else if (isRoundedStone) {
               return StructureConstantsEnum.WALL_ORIEL_ROUNDED_STONE.value;
            } else if (isPottery) {
               return StructureConstantsEnum.WALL_ORIEL_POTTERY.value;
            } else if (isSandstone) {
               return StructureConstantsEnum.WALL_ORIEL_SANDSTONE.value;
            } else if (isRendered) {
               return StructureConstantsEnum.WALL_ORIEL_RENDERED.value;
            } else {
               if (isMarble) {
                  return StructureConstantsEnum.WALL_ORIEL_MARBLE.value;
               }

               return StructureConstantsEnum.WALL_ORIEL_STONE_DECORATED.value;
            }
         case CANOPY_DOOR:
            return StructureConstantsEnum.WALL_CANOPY_WOODEN.value;
         case SCAFFOLDING:
            return StructureConstantsEnum.WALL_DOOR_ARCHED_WOODEN.value;
         case ARCHED_LEFT:
            if (isStone) {
               return StructureConstantsEnum.WALL_LEFT_ARCH_STONE_DECORATED.value;
            } else if (isTimber) {
               return StructureConstantsEnum.WALL_LEFT_ARCH_TIMBER_FRAMED.value;
            } else if (isPlainStone) {
               return StructureConstantsEnum.WALL_LEFT_ARCH_STONE.value;
            } else if (isSlate) {
               return StructureConstantsEnum.WALL_LEFT_ARCH_SLATE.value;
            } else if (isRoundedStone) {
               return StructureConstantsEnum.WALL_LEFT_ARCH_ROUNDED_STONE.value;
            } else if (isPottery) {
               return StructureConstantsEnum.WALL_LEFT_ARCH_POTTERY.value;
            } else if (isSandstone) {
               return StructureConstantsEnum.WALL_LEFT_ARCH_SANDSTONE.value;
            } else if (isRendered) {
               return StructureConstantsEnum.WALL_LEFT_ARCH_RENDERED.value;
            } else {
               if (isMarble) {
                  return StructureConstantsEnum.WALL_LEFT_ARCH_MARBLE.value;
               }

               return StructureConstantsEnum.WALL_LEFT_ARCH_WOODEN.value;
            }
         case ARCHED_RIGHT:
            if (isStone) {
               return StructureConstantsEnum.WALL_RIGHT_ARCH_STONE_DECORATED.value;
            } else if (isTimber) {
               return StructureConstantsEnum.WALL_RIGHT_ARCH_TIMBER_FRAMED.value;
            } else if (isPlainStone) {
               return StructureConstantsEnum.WALL_RIGHT_ARCH_STONE.value;
            } else if (isSlate) {
               return StructureConstantsEnum.WALL_RIGHT_ARCH_SLATE.value;
            } else if (isRoundedStone) {
               return StructureConstantsEnum.WALL_RIGHT_ARCH_ROUNDED_STONE.value;
            } else if (isPottery) {
               return StructureConstantsEnum.WALL_RIGHT_ARCH_POTTERY.value;
            } else if (isSandstone) {
               return StructureConstantsEnum.WALL_RIGHT_ARCH_SANDSTONE.value;
            } else if (isRendered) {
               return StructureConstantsEnum.WALL_RIGHT_ARCH_RENDERED.value;
            } else {
               if (isMarble) {
                  return StructureConstantsEnum.WALL_RIGHT_ARCH_MARBLE.value;
               }

               return StructureConstantsEnum.WALL_RIGHT_ARCH_WOODEN.value;
            }
         case ARCHED_T:
            if (isStone) {
               return StructureConstantsEnum.WALL_T_ARCH_STONE_DECORATED.value;
            } else if (isTimber) {
               return StructureConstantsEnum.WALL_T_ARCH_TIMBER_FRAMED.value;
            } else if (isPlainStone) {
               return StructureConstantsEnum.WALL_T_ARCH_STONE.value;
            } else if (isSlate) {
               return StructureConstantsEnum.WALL_T_ARCH_SLATE.value;
            } else if (isRoundedStone) {
               return StructureConstantsEnum.WALL_T_ARCH_ROUNDED_STONE.value;
            } else if (isPottery) {
               return StructureConstantsEnum.WALL_T_ARCH_POTTERY.value;
            } else if (isSandstone) {
               return StructureConstantsEnum.WALL_T_ARCH_SANDSTONE.value;
            } else if (isRendered) {
               return StructureConstantsEnum.WALL_T_ARCH_RENDERED.value;
            } else {
               if (isMarble) {
                  return StructureConstantsEnum.WALL_T_ARCH_MARBLE.value;
               }

               return StructureConstantsEnum.WALL_T_ARCH_WOODEN.value;
            }
         default:
            System.out.println("Not a legacy wall type: " + type);
            return 0;
      }
   }

   @Deprecated
   public static final short translateFenceType(short id) {
      return id;
   }

   public static final String getModelName(StructureConstantsEnum type, byte damageState, int pos) {
      return getModelName(type, damageState, pos, false);
   }

   public static final String getModelName(StructureConstantsEnum type, byte damageState, int pos, boolean initializing) {
      if (!initializing) {
         String modelName = type.getModelPath();
         if (pos > 0 && type.structureType == BuildingTypesEnum.HOUSE) {
            modelName = modelName + ".upper";
         }

         if (damageState >= 60) {
            modelName = modelName + ".decayed";
         }

         return modelName;
      } else {
         String modelName;
         switch(type) {
            case FENCE_WOODEN:
               modelName = "model.structure.wall.fence.fence";
               break;
            case FENCE_WOODEN_CRUDE:
               modelName = "model.structure.wall.fence.crude";
               break;
            case FENCE_WOODEN_CRUDE_GATE:
               modelName = "model.structure.wall.fence.gate.crude";
               break;
            case FENCE_PALISADE:
               modelName = "model.structure.wall.fence.palisade";
               break;
            case FENCE_STONEWALL:
               modelName = "model.structure.wall.fence.stonewall.short";
               break;
            case FENCE_WOODEN_GATE:
               modelName = "model.structure.wall.fence.gate.fence";
               break;
            case FENCE_PALISADE_GATE:
               modelName = "model.structure.wall.fence.gate.palisade";
               break;
            case FENCE_STONEWALL_HIGH:
               modelName = "model.structure.wall.fence.stonewall.tall";
               break;
            case FENCE_IRON:
               modelName = "model.structure.wall.fence.iron";
               break;
            case FENCE_SLATE_IRON:
               modelName = "model.structure.wall.fence.iron.slate";
               break;
            case FENCE_ROUNDED_STONE_IRON:
               modelName = "model.structure.wall.fence.iron.roundedstone";
               break;
            case FENCE_POTTERY_IRON:
               modelName = "model.structure.wall.fence.iron.pottery";
               break;
            case FENCE_SANDSTONE_IRON:
               modelName = "model.structure.wall.fence.iron.sandstone";
               break;
            case FENCE_RENDERED_IRON:
               modelName = "model.structure.wall.fence.iron.rendered";
               break;
            case FENCE_MARBLE_IRON:
               modelName = "model.structure.wall.fence.iron.marble";
               break;
            case FENCE_IRON_HIGH:
               modelName = "model.structure.wall.fence.iron.high";
               break;
            case FENCE_IRON_GATE:
               modelName = "model.structure.wall.fence.gate.iron";
               break;
            case FENCE_SLATE_IRON_GATE:
               modelName = "model.structure.wall.fence.gate.iron.slate";
               break;
            case FENCE_ROUNDED_STONE_IRON_GATE:
               modelName = "model.structure.wall.fence.gate.iron.roundedstone";
               break;
            case FENCE_POTTERY_IRON_GATE:
               modelName = "model.structure.wall.fence.gate.iron.pottery";
               break;
            case FENCE_SANDSTONE_IRON_GATE:
               modelName = "model.structure.wall.fence.gate.iron.sandstone";
               break;
            case FENCE_RENDERED_IRON_GATE:
               modelName = "model.structure.wall.fence.gate.iron.rendered";
               break;
            case FENCE_MARBLE_IRON_GATE:
               modelName = "model.structure.wall.fence.gate.iron.marble";
               break;
            case FENCE_IRON_GATE_HIGH:
               modelName = "model.structure.wall.fence.gate.iron.high";
               break;
            case FENCE_WOVEN:
               modelName = "model.structure.wall.fence.woven";
               break;
            case FENCE_PLAN_WOODEN:
               modelName = "model.structure.wall.fence.plan.fence";
               break;
            case FENCE_PLAN_WOODEN_CRUDE:
               modelName = "model.structure.wall.fence.crude.plan";
               break;
            case FENCE_PLAN_WOODEN_GATE_CRUDE:
               modelName = "model.structure.wall.fence.crude.plan";
               break;
            case FENCE_PLAN_PALISADE:
               modelName = "model.structure.wall.fence.plan.palisade";
               break;
            case FENCE_PLAN_STONEWALL:
               modelName = "model.structure.wall.fence.plan.stonewall.short";
               break;
            case FENCE_PLAN_PALISADE_GATE:
               modelName = "model.structure.wall.fence.plan.gate.palisade";
               break;
            case FENCE_PLAN_WOODEN_GATE:
               modelName = "model.structure.wall.fence.plan.gate.fence";
               break;
            case FENCE_PLAN_STONEWALL_HIGH:
               modelName = "model.structure.wall.fence.plan.stonewall.tall";
               break;
            case FENCE_PLAN_IRON:
               modelName = "model.structure.wall.fence.plan.iron";
               break;
            case FENCE_PLAN_SLATE_IRON:
               modelName = "model.structure.wall.fence.plan.slate.iron";
               break;
            case FENCE_PLAN_ROUNDED_STONE_IRON:
               modelName = "model.structure.wall.fence.plan.roundedstone.iron";
               break;
            case FENCE_PLAN_POTTERY_IRON:
               modelName = "model.structure.wall.fence.plan.pottery.iron";
               break;
            case FENCE_PLAN_SANDSTONE_IRON:
               modelName = "model.structure.wall.fence.plan.sandstone.iron";
               break;
            case FENCE_PLAN_RENDERED_IRON:
               modelName = "model.structure.wall.fence.plan.rendered.iron";
               break;
            case FENCE_PLAN_MARBLE_IRON:
               modelName = "model.structure.wall.fence.plan.marble.iron";
               break;
            case FENCE_PLAN_IRON_HIGH:
               modelName = "model.structure.wall.fence.plan.iron.high";
               break;
            case FENCE_PLAN_IRON_GATE:
               modelName = "model.structure.wall.fence.plan.gate.iron";
               break;
            case FENCE_PLAN_SLATE_IRON_GATE:
               modelName = "model.structure.wall.fence.plan.slate.gate.iron";
               break;
            case FENCE_PLAN_ROUNDED_STONE_IRON_GATE:
               modelName = "model.structure.wall.fence.plan.roundedstone.gate.iron";
               break;
            case FENCE_PLAN_POTTERY_IRON_GATE:
               modelName = "model.structure.wall.fence.plan.pottery.gate.iron";
               break;
            case FENCE_PLAN_SANDSTONE_IRON_GATE:
               modelName = "model.structure.wall.fence.plan.sandstone.gate.iron";
               break;
            case FENCE_PLAN_RENDERED_IRON_GATE:
               modelName = "model.structure.wall.fence.plan.rendered.gate.iron";
               break;
            case FENCE_PLAN_MARBLE_IRON_GATE:
               modelName = "model.structure.wall.fence.plan.marble.gate.iron";
               break;
            case FENCE_PLAN_IRON_GATE_HIGH:
               modelName = "model.structure.wall.fence.plan.gate.iron.high";
               break;
            case FENCE_PLAN_WOVEN:
               modelName = "model.structure.wall.fence.plan.woven";
               break;
            case FENCE_STONE_PARAPET:
               modelName = "model.structure.wall.fence.parapet.stone";
               break;
            case FENCE_PLAN_STONE_PARAPET:
               modelName = "model.structure.wall.fence.plan.parapet.stone";
               break;
            case FENCE_STONE_IRON_PARAPET:
               modelName = "model.structure.wall.fence.parapet.stoneiron";
               break;
            case FENCE_PLAN_STONE_IRON_PARAPET:
               modelName = "model.structure.wall.fence.plan.parapet.stoneiron";
               break;
            case FENCE_WOODEN_PARAPET:
               modelName = "model.structure.wall.fence.parapet.wooden";
               break;
            case FENCE_PLAN_WOODEN_PARAPET:
               modelName = "model.structure.wall.fence.plan.parapet.wooden";
               break;
            case FENCE_ROPE_LOW:
               modelName = "model.structure.wall.fence.rope.low";
               break;
            case FENCE_GARDESGARD_LOW:
               modelName = "model.structure.wall.fence.garde.low";
               break;
            case FENCE_GARDESGARD_HIGH:
               modelName = "model.structure.wall.fence.garde.high";
               break;
            case FENCE_GARDESGARD_GATE:
               modelName = "model.structure.wall.fence.gate.garde";
               break;
            case FENCE_CURB:
               modelName = "model.structure.wall.fence.curb";
               break;
            case FENCE_ROPE_HIGH:
               modelName = "model.structure.wall.fence.rope.high";
               break;
            case FENCE_STONE:
               modelName = "model.structure.wall.fence.stone";
               break;
            case FENCE_SLATE:
               modelName = "model.structure.wall.fence.slate";
               break;
            case FENCE_ROUNDED_STONE:
               modelName = "model.structure.wall.fence.roundedstone";
               break;
            case FENCE_POTTERY:
               modelName = "model.structure.wall.fence.pottery";
               break;
            case FENCE_SANDSTONE:
               modelName = "model.structure.wall.fence.sandstone";
               break;
            case FENCE_RENDERED:
               modelName = "model.structure.wall.fence.rendered";
               break;
            case FENCE_MARBLE:
               modelName = "model.structure.wall.fence.marble";
               break;
            case FENCE_PLAN_STONE:
               modelName = "model.structure.wall.fence.plan.stone";
               break;
            case FENCE_PLAN_SLATE:
               modelName = "model.structure.wall.fence.plan.slate";
               break;
            case FENCE_PLAN_ROUNDED_STONE:
               modelName = "model.structure.wall.fence.plan.roundedstone";
               break;
            case FENCE_PLAN_POTTERY:
               modelName = "model.structure.wall.fence.plan.pottery";
               break;
            case FENCE_PLAN_SANDSTONE:
               modelName = "model.structure.wall.fence.plan.sandstone";
               break;
            case FENCE_PLAN_RENDERED:
               modelName = "model.structure.wall.fence.plan.rendered";
               break;
            case FENCE_PLAN_MARBLE:
               modelName = "model.structure.wall.fence.plan.marble";
               break;
            case FENCE_PLAN_ROPE_LOW:
               modelName = "model.structure.wall.fence.plan.rope.low";
               break;
            case FENCE_PLAN_GARDESGARD_LOW:
               modelName = "model.structure.wall.fence.plan.garde.low";
               break;
            case FENCE_PLAN_GARDESGARD_HIGH:
               modelName = "model.structure.wall.fence.plan.garde.high";
               break;
            case FENCE_PLAN_GARDESGARD_GATE:
               modelName = "model.structure.wall.fence.plan.garde.gate";
               break;
            case FENCE_PLAN_CURB:
               modelName = "model.structure.wall.fence.plan.curb";
               break;
            case FENCE_PLAN_ROPE_HIGH:
               modelName = "model.structure.wall.fence.plan.rope.high";
               break;
            case HEDGE_FLOWER1_LOW:
               modelName = "model.structure.wall.hedge.1.low";
               break;
            case HEDGE_FLOWER1_MEDIUM:
               modelName = "model.structure.wall.hedge.1.medium";
               break;
            case HEDGE_FLOWER1_HIGH:
               modelName = "model.structure.wall.hedge.1.high";
               break;
            case HEDGE_FLOWER2_LOW:
               modelName = "model.structure.wall.hedge.2.low";
               break;
            case HEDGE_FLOWER2_MEDIUM:
               modelName = "model.structure.wall.hedge.2.medium";
               break;
            case HEDGE_FLOWER2_HIGH:
               modelName = "model.structure.wall.hedge.2.high";
               break;
            case HEDGE_FLOWER3_LOW:
               modelName = "model.structure.wall.hedge.3.low";
               break;
            case HEDGE_FLOWER3_MEDIUM:
               modelName = "model.structure.wall.hedge.3.medium";
               break;
            case HEDGE_FLOWER3_HIGH:
               modelName = "model.structure.wall.hedge.3.high";
               break;
            case HEDGE_FLOWER4_LOW:
               modelName = "model.structure.wall.hedge.4.low";
               break;
            case HEDGE_FLOWER4_MEDIUM:
               modelName = "model.structure.wall.hedge.4.medium";
               break;
            case HEDGE_FLOWER4_HIGH:
               modelName = "model.structure.wall.hedge.4.high";
               break;
            case HEDGE_FLOWER5_LOW:
               modelName = "model.structure.wall.hedge.5.low";
               break;
            case HEDGE_FLOWER5_MEDIUM:
               modelName = "model.structure.wall.hedge.5.medium";
               break;
            case HEDGE_FLOWER5_HIGH:
               modelName = "model.structure.wall.hedge.5.high";
               break;
            case HEDGE_FLOWER6_LOW:
               modelName = "model.structure.wall.hedge.6.low";
               break;
            case HEDGE_FLOWER6_MEDIUM:
               modelName = "model.structure.wall.hedge.6.medium";
               break;
            case HEDGE_FLOWER6_HIGH:
               modelName = "model.structure.wall.hedge.6.high";
               break;
            case HEDGE_FLOWER7_LOW:
               modelName = "model.structure.wall.hedge.7.low";
               break;
            case HEDGE_FLOWER7_MEDIUM:
               modelName = "model.structure.wall.hedge.7.medium";
               break;
            case HEDGE_FLOWER7_HIGH:
               modelName = "model.structure.wall.hedge.7.high";
               break;
            case FENCE_MAGIC_STONE:
               modelName = "model.structure.wall.fence.magic.stone";
               break;
            case FENCE_MAGIC_FIRE:
               modelName = "model.structure.wall.fence.magic.fire";
               break;
            case FENCE_MAGIC_ICE:
               modelName = "model.structure.wall.fence.magic.ice";
               break;
            case FENCE_RUBBLE:
               modelName = "model.structure.wall.fence.rubble";
               break;
            case FENCE_SIEGEWALL:
               modelName = "model.structure.invismantletfence";
               break;
            case FLOWERBED_BLUE:
               modelName = "model.structure.wall.flowerbed.blue";
               break;
            case FLOWERBED_GREENISH_YELLOW:
               modelName = "model.structure.wall.flowerbed.greenish";
               break;
            case FLOWERBED_ORANGE_RED:
               modelName = "model.structure.wall.flowerbed.orange";
               break;
            case FLOWERBED_PURPLE:
               modelName = "model.structure.wall.flowerbed.purple";
               break;
            case FLOWERBED_WHITE:
               modelName = "model.structure.wall.flowerbed.white";
               break;
            case FLOWERBED_WHITE_DOTTED:
               modelName = "model.structure.wall.flowerbed.white.dotted";
               break;
            case FLOWERBED_YELLOW:
               modelName = "model.structure.wall.flowerbed.yellow";
               break;
            case WALL_RUBBLE:
               modelName = "model.structure.wall.rubble";
               break;
            case WALL_SOLID_WOODEN:
               modelName = "model.structure.wall.house.wood";
               break;
            case WALL_CANOPY_WOODEN:
               modelName = "model.structure.wall.house.canopy.wood";
               break;
            case WALL_WINDOW_WOODEN:
               modelName = "model.structure.wall.house.window.wood";
               break;
            case WALL_WINDOW_WIDE_WOODEN:
               modelName = "model.structure.wall.house.widewindow.wood";
               break;
            case WALL_DOOR_WOODEN:
               modelName = "model.structure.wall.house.door.wood";
               break;
            case WALL_DOUBLE_DOOR_WOODEN:
               modelName = "model.structure.wall.house.doubledoor.wood";
               break;
            case WALL_SOLID_STONE_DECORATED:
               modelName = "model.structure.wall.house.stone";
               break;
            case WALL_ORIEL_STONE_DECORATED:
               modelName = "model.structure.wall.house.oriel1.stone";
               break;
            case WALL_ORIEL_STONE_PLAIN:
               modelName = "model.structure.wall.house.oriel2.stoneplain";
               break;
            case WALL_SOLID_STONE:
               modelName = "model.structure.wall.house.stoneplain";
               break;
            case WALL_BARRED_STONE:
               modelName = "model.structure.wall.house.bars1.stoneplain";
               break;
            case WALL_WINDOW_STONE:
               modelName = "model.structure.wall.house.window.stoneplain";
               break;
            case WALL_PLAIN_NARROW_WINDOW:
               modelName = "model.structure.wall.house.windownarrow.stoneplain";
               break;
            case WALL_DOOR_STONE:
               modelName = "model.structure.wall.house.door.stoneplain";
               break;
            case WALL_WINDOW_STONE_DECORATED:
               modelName = "model.structure.wall.house.window.stone";
               break;
            case WALL_DOOR_STONE_DECORATED:
               modelName = "model.structure.wall.house.door.stone";
               break;
            case WALL_DOUBLE_DOOR_STONE_DECORATED:
               modelName = "model.structure.wall.house.doubledoor.stone";
               break;
            case WALL_PLAIN_NARROW_WINDOW_PLAN:
               modelName = "model.structure.wall";
               break;
            case WALL_PORTCULLIS_STONE:
               modelName = "model.structure.wall.house.portcullis.stoneplain";
               break;
            case WALL_PORTCULLIS_WOOD:
               modelName = "model.structure.wall.house.portcullis.wood";
               break;
            case WALL_PORTCULLIS_STONE_DECORATED:
               modelName = "model.structure.wall.house.portcullis.stone";
               break;
            case WALL_DOUBLE_DOOR_STONE:
               modelName = "model.structure.wall.house.doubledoor.stoneplain";
               break;
            case WALL_DOOR_ARCHED_WOODEN:
               modelName = "model.structure.wall.house.arched.wood";
               break;
            case WALL_LEFT_ARCH_WOODEN:
               modelName = "model.structure.wall.house.archleft.wood";
               break;
            case WALL_RIGHT_ARCH_WOODEN:
               modelName = "model.structure.wall.house.archright.wood";
               break;
            case WALL_T_ARCH_WOODEN:
               modelName = "model.structure.wall.house.archt.wood";
               break;
            case WALL_DOUBLE_DOOR_WOODEN_PLAN:
               modelName = "model.structure.wall";
               break;
            case WALL_DOOR_ARCHED_WOODEN_PLAN:
               modelName = "model.structure.wall.house.arched.plan.wood";
               break;
            case WALL_DOUBLE_DOOR_STONE_PLAN:
               modelName = "model.structure.wall";
               break;
            case WALL_DOOR_ARCHED_PLAN:
               modelName = "model.structure.wall.house.arched.plan.stone";
               break;
            case WALL_DOOR_ARCHED_STONE_DECORATED:
               modelName = "model.structure.wall.house.arched.stone";
               break;
            case WALL_LEFT_ARCH_STONE_DECORATED:
               modelName = "model.structure.wall.house.archleft.stone";
               break;
            case WALL_RIGHT_ARCH_STONE_DECORATED:
               modelName = "model.structure.wall.house.archright.stone";
               break;
            case WALL_T_ARCH_STONE_DECORATED:
               modelName = "model.structure.wall.house.archt.stone";
               break;
            case WALL_DOOR_ARCHED_STONE:
               modelName = "model.structure.wall.house.arched.stoneplain";
               break;
            case WALL_LEFT_ARCH_STONE:
               modelName = "model.structure.wall.house.archleft.stoneplain";
               break;
            case WALL_RIGHT_ARCH_STONE:
               modelName = "model.structure.wall.house.archright.stoneplain";
               break;
            case WALL_T_ARCH_STONE:
               modelName = "model.structure.wall.house.archt.stoneplain";
               break;
            case WALL_SOLID_WOODEN_PLAN:
               modelName = "model.structure.wall.house.plan.wood";
               break;
            case WALL_WINDOW_WOODEN_PLAN:
               modelName = "model.structure.wall.house.plan.wood";
               break;
            case WALL_DOOR_WOODEN_PLAN:
               modelName = "model.structure.wall.house.plan.wood";
               break;
            case WALL_CANOPY_WOODEN_PLAN:
               modelName = "model.structure.wall.house.plan.wood";
               break;
            case WALL_SOLID_STONE_PLAN:
               modelName = "model.structure.wall.house.plan.stone";
               break;
            case WALL_ORIEL_STONE_DECORATED_PLAN:
               modelName = "model.structure.wall.house.plan.stone";
               break;
            case WALL_WINDOW_STONE_PLAN:
               modelName = "model.structure.wall.house.plan.stone";
               break;
            case WALL_DOOR_STONE_PLAN:
               modelName = "model.structure.wall.house.plan.stone";
               break;
            case WALL_SOLID_TIMBER_FRAMED:
               modelName = "model.structure.wall.house.timber";
               break;
            case WALL_WINDOW_TIMBER_FRAMED:
               modelName = "model.structure.wall.house.window.timber";
               break;
            case WALL_DOOR_TIMBER_FRAMED:
               modelName = "model.structure.wall.house.door.timber";
               break;
            case WALL_DOUBLE_DOOR_TIMBER_FRAMED:
               modelName = "model.structure.wall.house.doubledoor.timber";
               break;
            case WALL_DOOR_ARCHED_TIMBER_FRAMED:
               modelName = "model.structure.wall.house.arched.timber";
               break;
            case WALL_LEFT_ARCH_TIMBER_FRAMED:
               modelName = "model.structure.wall.house.archleft.timber";
               break;
            case WALL_RIGHT_ARCH_TIMBER_FRAMED:
               modelName = "model.structure.wall.house.archright.timber";
               break;
            case WALL_T_ARCH_TIMBER_FRAMED:
               modelName = "model.structure.wall.house.archt.timber";
               break;
            case WALL_BALCONY_TIMBER_FRAMED:
               modelName = "model.structure.wall.house.balcony.timber";
               break;
            case WALL_JETTY_TIMBER_FRAMED:
               modelName = "model.structure.wall.house.jetty.timber";
               break;
            case WALL_SOLID_TIMBER_FRAMED_PLAN:
            case WALL_BALCONY_TIMBER_FRAMED_PLAN:
            case WALL_JETTY_TIMBER_FRAMED_PLAN:
            case WALL_WINDOW_TIMBER_FRAMED_PLAN:
            case WALL_DOOR_TIMBER_FRAMED_PLAN:
            case WALL_DOUBLE_DOOR_TIMBER_FRAMED_PLAN:
            case WALL_DOOR_ARCHED_TIMBER_FRAMED_PLAN:
               modelName = "model.structure.wall.house.plan.wood";
               break;
            case WALL_SOLID_SLATE:
               modelName = "model.structure.wall.house.slate";
               break;
            case WALL_WINDOW_SLATE:
               modelName = "model.structure.wall.house.window.slate";
               break;
            case WALL_NARROW_WINDOW_SLATE:
               modelName = "model.structure.wall.house.windownarrow.slate";
               break;
            case WALL_DOOR_SLATE:
               modelName = "model.structure.wall.house.door.slate";
               break;
            case WALL_DOUBLE_DOOR_SLATE:
               modelName = "model.structure.wall.house.doubledoor.slate";
               break;
            case WALL_ARCHED_SLATE:
               modelName = "model.structure.wall.house.arched.slate";
               break;
            case WALL_PORTCULLIS_SLATE:
               modelName = "model.structure.wall.house.portcullis.slate";
               break;
            case WALL_BARRED_SLATE:
               modelName = "model.structure.wall.house.bars1.slate";
               break;
            case WALL_ORIEL_SLATE:
               modelName = "model.structure.wall.house.oriel2.slate";
               break;
            case WALL_LEFT_ARCH_SLATE:
               modelName = "model.structure.wall.house.archleft.slate";
               break;
            case WALL_RIGHT_ARCH_SLATE:
               modelName = "model.structure.wall.house.archright.slate";
               break;
            case WALL_T_ARCH_SLATE:
               modelName = "model.structure.wall.house.archt.slate";
               break;
            case WALL_SOLID_ROUNDED_STONE:
               modelName = "model.structure.wall.house.roundedstone";
               break;
            case WALL_WINDOW_ROUNDED_STONE:
               modelName = "model.structure.wall.house.window.roundedstone";
               break;
            case WALL_NARROW_WINDOW_ROUNDED_STONE:
               modelName = "model.structure.wall.house.windownarrow.roundedstone";
               break;
            case WALL_DOOR_ROUNDED_STONE:
               modelName = "model.structure.wall.house.door.roundedstone";
               break;
            case WALL_DOUBLE_DOOR_ROUNDED_STONE:
               modelName = "model.structure.wall.house.doubledoor.roundedstone";
               break;
            case WALL_ARCHED_ROUNDED_STONE:
               modelName = "model.structure.wall.house.arched.roundedstone";
               break;
            case WALL_PORTCULLIS_ROUNDED_STONE:
               modelName = "model.structure.wall.house.portcullis.roundedstone";
               break;
            case WALL_BARRED_ROUNDED_STONE:
               modelName = "model.structure.wall.house.bars1.roundedstone";
               break;
            case WALL_ORIEL_ROUNDED_STONE:
               modelName = "model.structure.wall.house.oriel2.roundedstone";
               break;
            case WALL_LEFT_ARCH_ROUNDED_STONE:
               modelName = "model.structure.wall.house.archleft.roundedstone";
               break;
            case WALL_RIGHT_ARCH_ROUNDED_STONE:
               modelName = "model.structure.wall.house.archright.roundedstone";
               break;
            case WALL_T_ARCH_ROUNDED_STONE:
               modelName = "model.structure.wall.house.archt.roundedstone";
               break;
            case WALL_SOLID_POTTERY:
               modelName = "model.structure.wall.house.pottery";
               break;
            case WALL_WINDOW_POTTERY:
               modelName = "model.structure.wall.house.window.pottery";
               break;
            case WALL_NARROW_WINDOW_POTTERY:
               modelName = "model.structure.wall.house.windownarrow.pottery";
               break;
            case WALL_DOOR_POTTERY:
               modelName = "model.structure.wall.house.door.pottery";
               break;
            case WALL_DOUBLE_DOOR_POTTERY:
               modelName = "model.structure.wall.house.doubledoor.pottery";
               break;
            case WALL_ARCHED_POTTERY:
               modelName = "model.structure.wall.house.arched.pottery";
               break;
            case WALL_PORTCULLIS_POTTERY:
               modelName = "model.structure.wall.house.portcullis.pottery";
               break;
            case WALL_BARRED_POTTERY:
               modelName = "model.structure.wall.house.bars1.pottery";
               break;
            case WALL_ORIEL_POTTERY:
               modelName = "model.structure.wall.house.oriel2.pottery";
               break;
            case WALL_LEFT_ARCH_POTTERY:
               modelName = "model.structure.wall.house.archleft.pottery";
               break;
            case WALL_RIGHT_ARCH_POTTERY:
               modelName = "model.structure.wall.house.archright.pottery";
               break;
            case WALL_T_ARCH_POTTERY:
               modelName = "model.structure.wall.house.archt.pottery";
               break;
            case WALL_SOLID_SANDSTONE:
               modelName = "model.structure.wall.house.sandstone";
               break;
            case WALL_WINDOW_SANDSTONE:
               modelName = "model.structure.wall.house.window.sandstone";
               break;
            case WALL_NARROW_WINDOW_SANDSTONE:
               modelName = "model.structure.wall.house.windownarrow.sandstone";
               break;
            case WALL_DOOR_SANDSTONE:
               modelName = "model.structure.wall.house.door.sandstone";
               break;
            case WALL_DOUBLE_DOOR_SANDSTONE:
               modelName = "model.structure.wall.house.doubledoor.sandstone";
               break;
            case WALL_ARCHED_SANDSTONE:
               modelName = "model.structure.wall.house.arched.sandstone";
               break;
            case WALL_PORTCULLIS_SANDSTONE:
               modelName = "model.structure.wall.house.portcullis.sandstone";
               break;
            case WALL_BARRED_SANDSTONE:
               modelName = "model.structure.wall.house.bars1.sandstone";
               break;
            case WALL_ORIEL_SANDSTONE:
               modelName = "model.structure.wall.house.oriel2.sandstone";
               break;
            case WALL_LEFT_ARCH_SANDSTONE:
               modelName = "model.structure.wall.house.archleft.sandstone";
               break;
            case WALL_RIGHT_ARCH_SANDSTONE:
               modelName = "model.structure.wall.house.archright.sandstone";
               break;
            case WALL_T_ARCH_SANDSTONE:
               modelName = "model.structure.wall.house.archt.sandstone";
               break;
            case WALL_SOLID_RENDERED:
               modelName = "model.structure.wall.house.rendered";
               break;
            case WALL_WINDOW_RENDERED:
               modelName = "model.structure.wall.house.window.rendered";
               break;
            case WALL_NARROW_WINDOW_RENDERED:
               modelName = "model.structure.wall.house.windownarrow.rendered";
               break;
            case WALL_DOOR_RENDERED:
               modelName = "model.structure.wall.house.door.rendered";
               break;
            case WALL_DOUBLE_DOOR_RENDERED:
               modelName = "model.structure.wall.house.doubledoor.rendered";
               break;
            case WALL_ARCHED_RENDERED:
               modelName = "model.structure.wall.house.arched.rendered";
               break;
            case WALL_PORTCULLIS_RENDERED:
               modelName = "model.structure.wall.house.portcullis.rendered";
               break;
            case WALL_BARRED_RENDERED:
               modelName = "model.structure.wall.house.bars1.rendered";
               break;
            case WALL_ORIEL_RENDERED:
               modelName = "model.structure.wall.house.oriel2.rendered";
               break;
            case WALL_LEFT_ARCH_RENDERED:
               modelName = "model.structure.wall.house.archleft.rendered";
               break;
            case WALL_RIGHT_ARCH_RENDERED:
               modelName = "model.structure.wall.house.archright.rendered";
               break;
            case WALL_T_ARCH_RENDERED:
               modelName = "model.structure.wall.house.archt.rendered";
               break;
            case WALL_SOLID_MARBLE:
               modelName = "model.structure.wall.house.marble";
               break;
            case WALL_WINDOW_MARBLE:
               modelName = "model.structure.wall.house.window.marble";
               break;
            case WALL_NARROW_WINDOW_MARBLE:
               modelName = "model.structure.wall.house.windownarrow.marble";
               break;
            case WALL_DOOR_MARBLE:
               modelName = "model.structure.wall.house.door.marble";
               break;
            case WALL_DOUBLE_DOOR_MARBLE:
               modelName = "model.structure.wall.house.doubledoor.marble";
               break;
            case WALL_ARCHED_MARBLE:
               modelName = "model.structure.wall.house.arched.marble";
               break;
            case WALL_PORTCULLIS_MARBLE:
               modelName = "model.structure.wall.house.portcullis.marble";
               break;
            case WALL_BARRED_MARBLE:
               modelName = "model.structure.wall.house.bars1.marble";
               break;
            case WALL_ORIEL_MARBLE:
               modelName = "model.structure.wall.house.oriel2.marble";
               break;
            case WALL_LEFT_ARCH_MARBLE:
               modelName = "model.structure.wall.house.archleft.marble";
               break;
            case WALL_RIGHT_ARCH_MARBLE:
               modelName = "model.structure.wall.house.archright.marble";
               break;
            case WALL_T_ARCH_MARBLE:
               modelName = "model.structure.wall.house.archt.marble";
               break;
            case NO_WALL:
               modelName = "model.structure.wall.house.plan.incomplete";
               break;
            case FENCE_MEDIUM_CHAIN:
               modelName = "model.structure.wall.fence.chains";
               break;
            case FENCE_PLAN_MEDIUM_CHAIN:
               modelName = "model.structure.wall.fence.plan.chains";
               break;
            case FENCE_PLAN_PORTCULLIS:
               modelName = "model.structure.wall.fence.plan.stonewallPortcullis";
               break;
            case FENCE_PORTCULLIS:
               modelName = "model.structure.wall.fence.stonewallPortcullis";
               break;
            case FENCE_SLATE_TALL_STONE_WALL:
               modelName = "model.structure.wall.fence.stonewall.tall.slate";
               break;
            case FENCE_SLATE_PORTCULLIS:
               modelName = "model.structure.wall.fence.stonewallPortcullis.slate";
               break;
            case FENCE_SLATE_HIGH_IRON_FENCE:
               modelName = "model.structure.wall.fence.iron.high.slate";
               break;
            case FENCE_SLATE_HIGH_IRON_FENCE_GATE:
               modelName = "model.structure.wall.fence.gate.iron.high.slate";
               break;
            case FENCE_SLATE_STONE_PARAPET:
               modelName = "model.structure.wall.fence.parapet.slate";
               break;
            case FENCE_SLATE_CHAIN_FENCE:
               modelName = "model.structure.wall.fence.chains.slate";
               break;
            case FENCE_ROUNDED_STONE_TALL_STONE_WALL:
               modelName = "model.structure.wall.fence.stonewall.tall.roundedstone";
               break;
            case FENCE_ROUNDED_STONE_PORTCULLIS:
               modelName = "model.structure.wall.fence.stonewallPortcullis.roundedstone";
               break;
            case FENCE_ROUNDED_STONE_HIGH_IRON_FENCE:
               modelName = "model.structure.wall.fence.iron.high.roundedstone";
               break;
            case FENCE_ROUNDED_STONE_HIGH_IRON_FENCE_GATE:
               modelName = "model.structure.wall.fence.gate.iron.high.roundedstone";
               break;
            case FENCE_ROUNDED_STONE_STONE_PARAPET:
               modelName = "model.structure.wall.fence.parapet.roundedstone";
               break;
            case FENCE_ROUNDED_STONE_CHAIN_FENCE:
               modelName = "model.structure.wall.fence.chains.roundedstone";
               break;
            case FENCE_SANDSTONE_TALL_STONE_WALL:
               modelName = "model.structure.wall.fence.stonewall.tall.sandstone";
               break;
            case FENCE_SANDSTONE_PORTCULLIS:
               modelName = "model.structure.wall.fence.stonewallPortcullis.sandstone";
               break;
            case FENCE_SANDSTONE_HIGH_IRON_FENCE:
               modelName = "model.structure.wall.fence.iron.high.sandstone";
               break;
            case FENCE_SANDSTONE_HIGH_IRON_FENCE_GATE:
               modelName = "model.structure.wall.fence.gate.iron.high.sandstone";
               break;
            case FENCE_SANDSTONE_STONE_PARAPET:
               modelName = "model.structure.wall.fence.parapet.sandstone";
               break;
            case FENCE_SANDSTONE_CHAIN_FENCE:
               modelName = "model.structure.wall.fence.chains.sandstone";
               break;
            case FENCE_RENDERED_TALL_STONE_WALL:
               modelName = "model.structure.wall.fence.stonewall.tall.rendered";
               break;
            case FENCE_RENDERED_PORTCULLIS:
               modelName = "model.structure.wall.fence.stonewallPortcullis.rendered";
               break;
            case FENCE_RENDERED_HIGH_IRON_FENCE:
               modelName = "model.structure.wall.fence.iron.high.rendered";
               break;
            case FENCE_RENDERED_HIGH_IRON_FENCE_GATE:
               modelName = "model.structure.wall.fence.gate.iron.high.rendered";
               break;
            case FENCE_RENDERED_STONE_PARAPET:
               modelName = "model.structure.wall.fence.parapet.rendered";
               break;
            case FENCE_RENDERED_CHAIN_FENCE:
               modelName = "model.structure.wall.fence.chains.rendered";
               break;
            case FENCE_POTTERY_TALL_STONE_WALL:
               modelName = "model.structure.wall.fence.stonewall.tall.pottery";
               break;
            case FENCE_POTTERY_PORTCULLIS:
               modelName = "model.structure.wall.fence.stonewallPortcullis.pottery";
               break;
            case FENCE_POTTERY_HIGH_IRON_FENCE:
               modelName = "model.structure.wall.fence.iron.high.pottery";
               break;
            case FENCE_POTTERY_HIGH_IRON_FENCE_GATE:
               modelName = "model.structure.wall.fence.gate.iron.high.pottery";
               break;
            case FENCE_POTTERY_STONE_PARAPET:
               modelName = "model.structure.wall.fence.parapet.pottery";
               break;
            case FENCE_POTTERY_CHAIN_FENCE:
               modelName = "model.structure.wall.fence.chains.pottery";
               break;
            case FENCE_MARBLE_TALL_STONE_WALL:
               modelName = "model.structure.wall.fence.stonewall.tall.marble";
               break;
            case FENCE_MARBLE_PORTCULLIS:
               modelName = "model.structure.wall.fence.stonewallPortcullis.marble";
               break;
            case FENCE_MARBLE_HIGH_IRON_FENCE:
               modelName = "model.structure.wall.fence.iron.high.marble";
               break;
            case FENCE_MARBLE_HIGH_IRON_FENCE_GATE:
               modelName = "model.structure.wall.fence.gate.iron.high.marble";
               break;
            case FENCE_MARBLE_STONE_PARAPET:
               modelName = "model.structure.wall.fence.parapet.marble";
               break;
            case FENCE_MARBLE_CHAIN_FENCE:
               modelName = "model.structure.wall.fence.chains.marble";
               break;
            case FENCE_PLAN_SLATE_TALL_STONE_WALL:
               modelName = "model.structure.wall.fence.plan.stonewall.tall.slate";
               break;
            case FENCE_PLAN_SLATE_PORTCULLIS:
               modelName = "model.structure.wall.fence.plan.stonewallPortcullis.slate";
               break;
            case FENCE_PLAN_SLATE_HIGH_IRON_FENCE:
               modelName = "model.structure.wall.fence.plan.iron.high.slate";
               break;
            case FENCE_PLAN_SLATE_HIGH_IRON_FENCE_GATE:
               modelName = "model.structure.wall.fence.plan.gate.iron.high.slate";
               break;
            case FENCE_PLAN_SLATE_STONE_PARAPET:
               modelName = "model.structure.wall.fence.plan.parapet.slate";
               break;
            case FENCE_PLAN_SLATE_CHAIN_FENCE:
               modelName = "model.structure.wall.fence.plan.chains.slate";
               break;
            case FENCE_PLAN_ROUNDED_STONE_TALL_STONE_WALL:
               modelName = "model.structure.wall.fence.plan.stonewall.tall.roundedstone";
               break;
            case FENCE_PLAN_ROUNDED_STONE_PORTCULLIS:
               modelName = "model.structure.wall.fence.plan.stonewallPortcullis.roundedstone";
               break;
            case FENCE_PLAN_ROUNDED_STONE_HIGH_IRON_FENCE:
               modelName = "model.structure.wall.fence.plan.iron.high.roundedstone";
               break;
            case FENCE_PLAN_ROUNDED_STONE_HIGH_IRON_FENCE_GATE:
               modelName = "model.structure.wall.fence.plan.gate.iron.high.roundedstone";
               break;
            case FENCE_PLAN_ROUNDED_STONE_STONE_PARAPET:
               modelName = "model.structure.wall.fence.plan.parapet.roundedstone";
               break;
            case FENCE_PLAN_ROUNDED_STONE_CHAIN_FENCE:
               modelName = "model.structure.wall.fence.plan.chains.roundedstone";
               break;
            case FENCE_PLAN_SANDSTONE_TALL_STONE_WALL:
               modelName = "model.structure.wall.fence.plan.stonewall.tall.sandstone";
               break;
            case FENCE_PLAN_SANDSTONE_PORTCULLIS:
               modelName = "model.structure.wall.fence.plan.stonewallPortcullis.sandstone";
               break;
            case FENCE_PLAN_SANDSTONE_HIGH_IRON_FENCE:
               modelName = "model.structure.wall.fence.plan.iron.high.sandstone";
               break;
            case FENCE_PLAN_SANDSTONE_HIGH_IRON_FENCE_GATE:
               modelName = "model.structure.wall.fence.plan.gate.iron.high.sandstone";
               break;
            case FENCE_PLAN_SANDSTONE_STONE_PARAPET:
               modelName = "model.structure.wall.fence.plan.parapet.sandstone";
               break;
            case FENCE_PLAN_SANDSTONE_CHAIN_FENCE:
               modelName = "model.structure.wall.fence.plan.chains.sandstone";
               break;
            case FENCE_PLAN_RENDERED_TALL_STONE_WALL:
               modelName = "model.structure.wall.fence.plan.stonewall.tall.rendered";
               break;
            case FENCE_PLAN_RENDERED_PORTCULLIS:
               modelName = "model.structure.wall.fence.plan.stonewallPortcullis.rendered";
               break;
            case FENCE_PLAN_RENDERED_HIGH_IRON_FENCE:
               modelName = "model.structure.wall.fence.plan.iron.high.rendered";
               break;
            case FENCE_PLAN_RENDERED_HIGH_IRON_FENCE_GATE:
               modelName = "model.structure.wall.fence.plan.gate.iron.high.rendered";
               break;
            case FENCE_PLAN_RENDERED_STONE_PARAPET:
               modelName = "model.structure.wall.fence.plan.parapet.rendered";
               break;
            case FENCE_PLAN_RENDERED_CHAIN_FENCE:
               modelName = "model.structure.wall.fence.plan.chains.rendered";
               break;
            case FENCE_PLAN_POTTERY_TALL_STONE_WALL:
               modelName = "model.structure.wall.fence.plan.stonewall.tall.pottery";
               break;
            case FENCE_PLAN_POTTERY_PORTCULLIS:
               modelName = "model.structure.wall.fence.plan.stonewallPortcullis.pottery";
               break;
            case FENCE_PLAN_POTTERY_HIGH_IRON_FENCE:
               modelName = "model.structure.wall.fence.plan.iron.high.pottery";
               break;
            case FENCE_PLAN_POTTERY_HIGH_IRON_FENCE_GATE:
               modelName = "model.structure.wall.fence.plan.gate.iron.high.pottery";
               break;
            case FENCE_PLAN_POTTERY_STONE_PARAPET:
               modelName = "model.structure.wall.fence.plan.parapet.pottery";
               break;
            case FENCE_PLAN_POTTERY_CHAIN_FENCE:
               modelName = "model.structure.wall.fence.plan.chains.pottery";
               break;
            case FENCE_PLAN_MARBLE_TALL_STONE_WALL:
               modelName = "model.structure.wall.fence.plan.stonewall.tall.marble";
               break;
            case FENCE_PLAN_MARBLE_PORTCULLIS:
               modelName = "model.structure.wall.fence.plan.stonewallPortcullis.marble";
               break;
            case FENCE_PLAN_MARBLE_HIGH_IRON_FENCE:
               modelName = "model.structure.wall.fence.plan.iron.high.marble";
               break;
            case FENCE_PLAN_MARBLE_HIGH_IRON_FENCE_GATE:
               modelName = "model.structure.wall.fence.plan.gate.iron.high.marble";
               break;
            case FENCE_PLAN_MARBLE_STONE_PARAPET:
               modelName = "model.structure.wall.fence.plan.parapet.marble";
               break;
            case FENCE_PLAN_MARBLE_CHAIN_FENCE:
               modelName = "model.structure.wall.fence.plan.chains.marble";
               break;
            default:
               modelName = "model.structure.wall";
         }

         if (pos > 0 && type.structureType == BuildingTypesEnum.HOUSE) {
            modelName = modelName + ".upper";
         }

         if (damageState >= 60) {
            modelName = modelName + ".decayed";
         }

         return modelName;
      }
   }

   public static final String getTextureName(StructureConstantsEnum type) {
      return getTextureName(type, false);
   }

   public static final String getTextureName(StructureConstantsEnum type, boolean initializing) {
      if (!initializing) {
         return type.getTexturePath();
      } else {
         switch(type) {
            case FENCE_WOODEN:
               return "img.texture.fence.fence";
            case FENCE_WOODEN_CRUDE:
               return "img.texture.fence.crude";
            case FENCE_WOODEN_CRUDE_GATE:
               return "img.texture.fence.gate.crude";
            case FENCE_PALISADE:
               return "img.texture.fence.palisade";
            case FENCE_STONEWALL:
               return "img.texture.fence.stonewall.short";
            case FENCE_WOODEN_GATE:
               return "img.texture.fence.gate.fence";
            case FENCE_PALISADE_GATE:
               return "img.texture.fence.gate.palisade";
            case FENCE_STONEWALL_HIGH:
               return "img.texture.fence.stonewall.tall";
            case FENCE_IRON:
               return "img.texture.fence.iron";
            case FENCE_SLATE_IRON:
               return "img.texture.fence.slate.iron";
            case FENCE_ROUNDED_STONE_IRON:
               return "img.texture.fence.roundedstone.iron";
            case FENCE_POTTERY_IRON:
               return "img.texture.fence.pottery.iron";
            case FENCE_SANDSTONE_IRON:
               return "img.texture.fence.sandstone.iron";
            case FENCE_RENDERED_IRON:
               return "img.texture.fence.rendered.iron";
            case FENCE_MARBLE_IRON:
               return "img.texture.fence.marble.iron";
            case FENCE_IRON_HIGH:
               return "img.texture.fence.iron.high";
            case FENCE_IRON_GATE:
               return "img.texture.fence.gate.iron";
            case FENCE_SLATE_IRON_GATE:
               return "img.texture.fence.slate.iron.gate";
            case FENCE_ROUNDED_STONE_IRON_GATE:
               return "img.texture.fence.roundedstone.iron.gate";
            case FENCE_POTTERY_IRON_GATE:
               return "img.texture.fence.pottery.iron.gate";
            case FENCE_SANDSTONE_IRON_GATE:
               return "img.texture.fence.sandstone.iron.gate";
            case FENCE_RENDERED_IRON_GATE:
               return "img.texture.fence.rendered.iron.gate";
            case FENCE_MARBLE_IRON_GATE:
               return "img.texture.fence.marble.iron.gate";
            case FENCE_IRON_GATE_HIGH:
               return "img.texture.fence.gate.iron.high";
            case FENCE_WOVEN:
               return "img.texture.fence.woven";
            case FENCE_PLAN_WOODEN:
               return "img.texture.fence.plan.fence";
            case FENCE_PLAN_WOODEN_CRUDE:
               return "img.texture.fence.plan.crude";
            case FENCE_PLAN_WOODEN_GATE_CRUDE:
               return "img.texture.fence.plan.gate.crude";
            case FENCE_PLAN_PALISADE:
               return "img.texture.fence.plan.palisade";
            case FENCE_PLAN_STONEWALL:
               return "img.texture.fence.plan.stonewall.short";
            case FENCE_PLAN_PALISADE_GATE:
               return "img.texture.fence.plan.gate.palisade";
            case FENCE_PLAN_WOODEN_GATE:
               return "img.texture.fence.plan.gate.fence";
            case FENCE_PLAN_STONEWALL_HIGH:
               return "img.texture.fence.plan.stonewall.tall";
            case FENCE_PLAN_IRON:
            case FENCE_PLAN_SLATE_IRON:
            case FENCE_PLAN_ROUNDED_STONE_IRON:
            case FENCE_PLAN_POTTERY_IRON:
            case FENCE_PLAN_SANDSTONE_IRON:
            case FENCE_PLAN_MARBLE_IRON:
               return "img.texture.fence.plan.iron";
            case FENCE_PLAN_RENDERED_IRON:
            case FENCE_PLAN_RENDERED_IRON_GATE:
            case FENCE_PLAN_RENDERED_TALL_STONE_WALL:
            case FENCE_PLAN_RENDERED_PORTCULLIS:
            case FENCE_PLAN_RENDERED_HIGH_IRON_FENCE:
            case FENCE_PLAN_RENDERED_HIGH_IRON_FENCE_GATE:
            case FENCE_PLAN_RENDERED_STONE_PARAPET:
            case FENCE_PLAN_RENDERED_CHAIN_FENCE:
            default:
               return "img.texture.house.wall";
            case FENCE_PLAN_IRON_HIGH:
               return "img.texture.fence.plan.iron.high";
            case FENCE_PLAN_IRON_GATE:
            case FENCE_PLAN_SLATE_IRON_GATE:
            case FENCE_PLAN_ROUNDED_STONE_IRON_GATE:
            case FENCE_PLAN_POTTERY_IRON_GATE:
            case FENCE_PLAN_SANDSTONE_IRON_GATE:
            case FENCE_PLAN_MARBLE_IRON_GATE:
               return "img.texture.fence.plan.gate.iron";
            case FENCE_PLAN_IRON_GATE_HIGH:
               return "img.texture.fence.plan.gate.iron.high";
            case FENCE_PLAN_WOVEN:
               return "img.texture.fence.plan.woven";
            case FENCE_STONE_PARAPET:
               return "img.texture.fence.parapet.stone";
            case FENCE_PLAN_STONE_PARAPET:
               return "img.texture.fence.plan.parapet.stone";
            case FENCE_STONE_IRON_PARAPET:
               return "img.texture.fence.parapet.stoneiron";
            case FENCE_PLAN_STONE_IRON_PARAPET:
               return "img.texture.fence.plan.parapet.stoneiron";
            case FENCE_WOODEN_PARAPET:
               return "img.texture.fence.parapet.wooden";
            case FENCE_PLAN_WOODEN_PARAPET:
               return "img.texture.fence.plan.parapet.wooden";
            case FENCE_ROPE_LOW:
               return "img.texture.fence.rope.low";
            case FENCE_GARDESGARD_LOW:
               return "img.texture.fence.garde.low";
            case FENCE_GARDESGARD_HIGH:
               return "img.texture.fence.garde.high";
            case FENCE_GARDESGARD_GATE:
               return "img.texture.fence.garde.gate";
            case FENCE_CURB:
               return "img.texture.fence.curb";
            case FENCE_ROPE_HIGH:
               return "img.texture.fence.rope.high";
            case FENCE_STONE:
               return "img.texture.fence.stone";
            case FENCE_SLATE:
               return "img.texture.fence.slate";
            case FENCE_ROUNDED_STONE:
               return "img.texture.fence.roundedstone";
            case FENCE_POTTERY:
               return "img.texture.fence.pottery";
            case FENCE_SANDSTONE:
               return "img.texture.fence.sandstone";
            case FENCE_RENDERED:
               return "img.texture.fence.rendered";
            case FENCE_MARBLE:
               return "img.texture.fence.marble";
            case FENCE_PLAN_STONE:
            case FENCE_PLAN_SLATE:
            case FENCE_PLAN_ROUNDED_STONE:
            case FENCE_PLAN_POTTERY:
            case FENCE_PLAN_SANDSTONE:
            case FENCE_PLAN_RENDERED:
            case FENCE_PLAN_MARBLE:
               return "img.texture.fence.plan.stone";
            case FENCE_PLAN_ROPE_LOW:
               return "img.texture.fence.plan.rope.low";
            case FENCE_PLAN_GARDESGARD_LOW:
               return "img.texture.fence.plan.garde.low";
            case FENCE_PLAN_GARDESGARD_HIGH:
               return "img.texture.fence.plan.garde.high";
            case FENCE_PLAN_GARDESGARD_GATE:
               return "img.texture.fence.plan.garde.gate";
            case FENCE_PLAN_CURB:
               return "img.texture.fence.plan.curb";
            case FENCE_PLAN_ROPE_HIGH:
               return "img.texture.fence.plan.rope.high";
            case HEDGE_FLOWER1_LOW:
               return "img.texture.hedge.1.low";
            case HEDGE_FLOWER1_MEDIUM:
               return "img.texture.hedge.1.medium";
            case HEDGE_FLOWER1_HIGH:
               return "img.texture.hedge.1.high";
            case HEDGE_FLOWER2_LOW:
               return "img.texture.hedge.2.low";
            case HEDGE_FLOWER2_MEDIUM:
               return "img.texture.hedge.2.medium";
            case HEDGE_FLOWER2_HIGH:
               return "img.texture.hedge.2.high";
            case HEDGE_FLOWER3_LOW:
               return "img.texture.hedge.3.low";
            case HEDGE_FLOWER3_MEDIUM:
               return "img.texture.hedge.3.medium";
            case HEDGE_FLOWER3_HIGH:
               return "img.texture.hedge.3.high";
            case HEDGE_FLOWER4_LOW:
               return "img.texture.hedge.4.low";
            case HEDGE_FLOWER4_MEDIUM:
               return "img.texture.hedge.4.medium";
            case HEDGE_FLOWER4_HIGH:
               return "img.texture.hedge.4.high";
            case HEDGE_FLOWER5_LOW:
               return "img.texture.hedge.5.low";
            case HEDGE_FLOWER5_MEDIUM:
               return "img.texture.hedge.5.medium";
            case HEDGE_FLOWER5_HIGH:
               return "img.texture.hedge.5.high";
            case HEDGE_FLOWER6_LOW:
               return "img.texture.hedge.6.low";
            case HEDGE_FLOWER6_MEDIUM:
               return "img.texture.hedge.6.medium";
            case HEDGE_FLOWER6_HIGH:
               return "img.texture.hedge.6.high";
            case HEDGE_FLOWER7_LOW:
               return "img.texture.hedge.7.low";
            case HEDGE_FLOWER7_MEDIUM:
               return "img.texture.hedge.7.medium";
            case HEDGE_FLOWER7_HIGH:
               return "img.texture.hedge.7.high";
            case FENCE_MAGIC_STONE:
               return "img.texture.fence.magic.stone";
            case FENCE_MAGIC_FIRE:
               return "img.texture.fence.magic.fire";
            case FENCE_MAGIC_ICE:
               return "img.texture.fence.magic.ice";
            case FENCE_RUBBLE:
               return "img.texture.fence.rubble";
            case FENCE_SIEGEWALL:
               return "img.texture.fence.plan.curb";
            case FLOWERBED_BLUE:
               return "img.texture.flowerbed.blue";
            case FLOWERBED_GREENISH_YELLOW:
               return "img.texture.flowerbed.greenish";
            case FLOWERBED_ORANGE_RED:
               return "img.texture.flowerbed.orange";
            case FLOWERBED_PURPLE:
               return "img.texture.flowerbed.purple";
            case FLOWERBED_WHITE:
               return "img.texture.flowerbed.white";
            case FLOWERBED_WHITE_DOTTED:
               return "img.texture.flowerbed.white.dotted";
            case FLOWERBED_YELLOW:
               return "img.texture.flowerbed.yellow";
            case WALL_RUBBLE:
               return "img.texture.wall.rubble";
            case WALL_SOLID_WOODEN:
               return "img.texture.house.wall.solidwood";
            case WALL_CANOPY_WOODEN:
               return "img.texture.house.wall.canopywood";
            case WALL_WINDOW_WOODEN:
               return "img.texture.house.wall.windowwood";
            case WALL_WINDOW_WIDE_WOODEN:
               return "img.texture.house.wall.widewindowwood";
            case WALL_DOOR_WOODEN:
               return "img.texture.house.wall.doorwood";
            case WALL_DOUBLE_DOOR_WOODEN:
               return "img.texture.house.wall.doubledoorwood";
            case WALL_SOLID_STONE_DECORATED:
               return "img.texture.house.wall.solidstone";
            case WALL_ORIEL_STONE_DECORATED:
               return "img.texture.house.wall.solidstoneoriel1";
            case WALL_ORIEL_STONE_PLAIN:
               return "img.texture.house.wall.oriel2stoneplain";
            case WALL_SOLID_STONE:
               return "img.texture.house.wall.solidstoneplain";
            case WALL_BARRED_STONE:
               return "img.texture.house.wall.bars1stoneplain";
            case WALL_WINDOW_STONE:
               return "img.texture.house.wall.windowstoneplain";
            case WALL_PLAIN_NARROW_WINDOW:
               return "img.texture.house.wall.windownarrowstoneplain";
            case WALL_DOOR_STONE:
               return "img.texture.house.wall.doorstoneplain";
            case WALL_WINDOW_STONE_DECORATED:
               return "img.texture.house.wall.windowstone";
            case WALL_DOOR_STONE_DECORATED:
               return "img.texture.house.wall.doorstone";
            case WALL_DOUBLE_DOOR_STONE_DECORATED:
               return "img.texture.house.wall.doubledoorstone";
            case WALL_PLAIN_NARROW_WINDOW_PLAN:
               return "img.texture.wall.house.plan";
            case WALL_PORTCULLIS_STONE:
               return "img.texture.house.wall.portcullisstoneplain";
            case WALL_PORTCULLIS_WOOD:
               return "img.texture.house.wall.woodwallPortcullis";
            case WALL_PORTCULLIS_STONE_DECORATED:
               return "img.texture.house.wall.solidstonePortcullis";
            case WALL_DOUBLE_DOOR_STONE:
               return "img.texture.house.wall.doubledoorstoneplain";
            case WALL_DOOR_ARCHED_WOODEN:
               return "img.texture.house.wall.archedwooden";
            case WALL_LEFT_ARCH_WOODEN:
               return "img.texture.house.wall.archleftwooden";
            case WALL_RIGHT_ARCH_WOODEN:
               return "img.texture.house.wall.archrightwooden";
            case WALL_T_ARCH_WOODEN:
               return "img.texture.house.wall.archtwooden";
            case WALL_DOUBLE_DOOR_WOODEN_PLAN:
            case WALL_DOUBLE_DOOR_STONE_PLAN:
               return "img.texture.house.wall";
            case WALL_DOOR_ARCHED_WOODEN_PLAN:
               return "img.texture.house.wall.plan.archedwooden";
            case WALL_DOOR_ARCHED_PLAN:
               return "img.texture.house.wall.plan.archedstone";
            case WALL_DOOR_ARCHED_STONE_DECORATED:
               return "img.texture.house.wall.archedstone";
            case WALL_LEFT_ARCH_STONE_DECORATED:
               return "img.texture.house.wall.archleftstone";
            case WALL_RIGHT_ARCH_STONE_DECORATED:
               return "img.texture.house.wall.archrightstone";
            case WALL_T_ARCH_STONE_DECORATED:
               return "img.texture.house.wall.archtstone";
            case WALL_DOOR_ARCHED_STONE:
               return "img.texture.house.wall.archedstoneplain";
            case WALL_LEFT_ARCH_STONE:
               return "img.texture.house.wall.archleftstoneplain";
            case WALL_RIGHT_ARCH_STONE:
               return "img.texture.house.wall.archrightstoneplain";
            case WALL_T_ARCH_STONE:
               return "img.texture.house.wall.archtstoneplain";
            case WALL_SOLID_WOODEN_PLAN:
               return "img.texture.house.wall.outlinewood";
            case WALL_WINDOW_WOODEN_PLAN:
               return "img.texture.house.wall.outlinewood";
            case WALL_DOOR_WOODEN_PLAN:
               return "img.texture.house.wall.outlinewood";
            case WALL_CANOPY_WOODEN_PLAN:
               return "img.texture.house.wall.outlinewood";
            case WALL_SOLID_STONE_PLAN:
               return "img.texture.house.wall.outlinestone";
            case WALL_ORIEL_STONE_DECORATED_PLAN:
               return "img.texture.house.wall.outlinestone";
            case WALL_WINDOW_STONE_PLAN:
               return "img.texture.house.wall.outlinestone";
            case WALL_DOOR_STONE_PLAN:
               return "img.texture.house.wall.outlinestone";
            case WALL_SOLID_TIMBER_FRAMED:
               return "img.texture.house.wall.solidtimber";
            case WALL_WINDOW_TIMBER_FRAMED:
               return "img.texture.house.wall.windowtimber";
            case WALL_DOOR_TIMBER_FRAMED:
               return "img.texture.house.wall.doortimber";
            case WALL_DOUBLE_DOOR_TIMBER_FRAMED:
               return "img.texture.house.wall.doubledoortimber";
            case WALL_DOOR_ARCHED_TIMBER_FRAMED:
               return "img.texture.house.wall.archedtimber";
            case WALL_LEFT_ARCH_TIMBER_FRAMED:
               return "img.texture.house.wall.archlefttimber";
            case WALL_RIGHT_ARCH_TIMBER_FRAMED:
               return "img.texture.house.wall.archrighttimber";
            case WALL_T_ARCH_TIMBER_FRAMED:
               return "img.texture.house.wall.archttimber";
            case WALL_BALCONY_TIMBER_FRAMED:
               return "img.texture.house.wall.balconytimber";
            case WALL_JETTY_TIMBER_FRAMED:
               return "img.texture.house.wall.jettytimber";
            case WALL_SOLID_TIMBER_FRAMED_PLAN:
            case WALL_BALCONY_TIMBER_FRAMED_PLAN:
            case WALL_JETTY_TIMBER_FRAMED_PLAN:
            case WALL_WINDOW_TIMBER_FRAMED_PLAN:
            case WALL_DOOR_TIMBER_FRAMED_PLAN:
            case WALL_DOUBLE_DOOR_TIMBER_FRAMED_PLAN:
            case WALL_DOOR_ARCHED_TIMBER_FRAMED_PLAN:
               return "img.texture.house.wall.outlinewood";
            case WALL_SOLID_SLATE:
               return "img.texture.house.wall.solidslate";
            case WALL_WINDOW_SLATE:
               return "img.texture.house.wall.windowslate";
            case WALL_NARROW_WINDOW_SLATE:
               return "img.texture.house.wall.windownarrowslate";
            case WALL_DOOR_SLATE:
               return "img.texture.house.wall.doorslate";
            case WALL_DOUBLE_DOOR_SLATE:
               return "img.texture.house.wall.doubledoorslate";
            case WALL_ARCHED_SLATE:
               return "img.texture.house.wall.archedslate";
            case WALL_PORTCULLIS_SLATE:
               return "img.texture.house.wall.portcullisslate";
            case WALL_BARRED_SLATE:
               return "img.texture.house.wall.bars1slate";
            case WALL_ORIEL_SLATE:
               return "img.texture.house.wall.oriel2slate";
            case WALL_LEFT_ARCH_SLATE:
               return "img.texture.house.wall.archleftslate";
            case WALL_RIGHT_ARCH_SLATE:
               return "img.texture.house.wall.archrightslate";
            case WALL_T_ARCH_SLATE:
               return "img.texture.house.wall.archtslate";
            case WALL_SOLID_ROUNDED_STONE:
               return "img.texture.house.wall.solidroundedstone";
            case WALL_WINDOW_ROUNDED_STONE:
               return "img.texture.house.wall.windowroundedstone";
            case WALL_NARROW_WINDOW_ROUNDED_STONE:
               return "img.texture.house.wall.windownarrowroundedstone";
            case WALL_DOOR_ROUNDED_STONE:
               return "img.texture.house.wall.doorroundedstone";
            case WALL_DOUBLE_DOOR_ROUNDED_STONE:
               return "img.texture.house.wall.doubledoorroundedstone";
            case WALL_ARCHED_ROUNDED_STONE:
               return "img.texture.house.wall.archedroundedstone";
            case WALL_PORTCULLIS_ROUNDED_STONE:
               return "img.texture.house.wall.portcullisroundedstone";
            case WALL_BARRED_ROUNDED_STONE:
               return "img.texture.house.wall.bars1roundedstone";
            case WALL_ORIEL_ROUNDED_STONE:
               return "img.texture.house.wall.oriel2roundedstone";
            case WALL_LEFT_ARCH_ROUNDED_STONE:
               return "img.texture.house.wall.archleftroundedstone";
            case WALL_RIGHT_ARCH_ROUNDED_STONE:
               return "img.texture.house.wall.archrightroundedstone";
            case WALL_T_ARCH_ROUNDED_STONE:
               return "img.texture.house.wall.archtroundedstone";
            case WALL_SOLID_POTTERY:
               return "img.texture.house.wall.solidpottery";
            case WALL_WINDOW_POTTERY:
               return "img.texture.house.wall.windowpottery";
            case WALL_NARROW_WINDOW_POTTERY:
               return "img.texture.house.wall.windownarrowpottery";
            case WALL_DOOR_POTTERY:
               return "img.texture.house.wall.doorpottery";
            case WALL_DOUBLE_DOOR_POTTERY:
               return "img.texture.house.wall.doubledoorpottery";
            case WALL_ARCHED_POTTERY:
               return "img.texture.house.wall.archedpottery";
            case WALL_PORTCULLIS_POTTERY:
               return "img.texture.house.wall.portcullispottery";
            case WALL_BARRED_POTTERY:
               return "img.texture.house.wall.bars1pottery";
            case WALL_ORIEL_POTTERY:
               return "img.texture.house.wall.oriel2pottery";
            case WALL_LEFT_ARCH_POTTERY:
               return "img.texture.house.wall.archleftpottery";
            case WALL_RIGHT_ARCH_POTTERY:
               return "img.texture.house.wall.archrightpottery";
            case WALL_T_ARCH_POTTERY:
               return "img.texture.house.wall.archtpottery";
            case WALL_SOLID_SANDSTONE:
               return "img.texture.house.wall.solidsandstone";
            case WALL_WINDOW_SANDSTONE:
               return "img.texture.house.wall.windowsandstone";
            case WALL_NARROW_WINDOW_SANDSTONE:
               return "img.texture.house.wall.windownarrowsandstone";
            case WALL_DOOR_SANDSTONE:
               return "img.texture.house.wall.doorsandstone";
            case WALL_DOUBLE_DOOR_SANDSTONE:
               return "img.texture.house.wall.doubledoorsandstone";
            case WALL_ARCHED_SANDSTONE:
               return "img.texture.house.wall.archedsandstone";
            case WALL_PORTCULLIS_SANDSTONE:
               return "img.texture.house.wall.portcullissandstone";
            case WALL_BARRED_SANDSTONE:
               return "img.texture.house.wall.bars1sandstone";
            case WALL_ORIEL_SANDSTONE:
               return "img.texture.house.wall.oriel2sandstone";
            case WALL_LEFT_ARCH_SANDSTONE:
               return "img.texture.house.wall.archleftsandstone";
            case WALL_RIGHT_ARCH_SANDSTONE:
               return "img.texture.house.wall.archrightsandstone";
            case WALL_T_ARCH_SANDSTONE:
               return "img.texture.house.wall.archtsandstone";
            case WALL_SOLID_RENDERED:
               return "img.texture.house.wall.solidrendered";
            case WALL_WINDOW_RENDERED:
               return "img.texture.house.wall.windowrendered";
            case WALL_NARROW_WINDOW_RENDERED:
               return "img.texture.house.wall.windownarrowrendered";
            case WALL_DOOR_RENDERED:
               return "img.texture.house.wall.doorrendered";
            case WALL_DOUBLE_DOOR_RENDERED:
               return "img.texture.house.wall.doubledoorrendered";
            case WALL_ARCHED_RENDERED:
               return "img.texture.house.wall.archedrendered";
            case WALL_PORTCULLIS_RENDERED:
               return "img.texture.house.wall.portcullisrendered";
            case WALL_BARRED_RENDERED:
               return "img.texture.house.wall.bars1rendered";
            case WALL_ORIEL_RENDERED:
               return "img.texture.house.wall.oriel2rendered";
            case WALL_LEFT_ARCH_RENDERED:
               return "img.texture.house.wall.archleftrendered";
            case WALL_RIGHT_ARCH_RENDERED:
               return "img.texture.house.wall.archrightrendered";
            case WALL_T_ARCH_RENDERED:
               return "img.texture.house.wall.archtrendered";
            case WALL_SOLID_MARBLE:
               return "img.texture.house.wall.solidmarble";
            case WALL_WINDOW_MARBLE:
               return "img.texture.house.wall.windowmarble";
            case WALL_NARROW_WINDOW_MARBLE:
               return "img.texture.house.wall.windownarrowmarble";
            case WALL_DOOR_MARBLE:
               return "img.texture.house.wall.doormarble";
            case WALL_DOUBLE_DOOR_MARBLE:
               return "img.texture.house.wall.doubledoormarble";
            case WALL_ARCHED_MARBLE:
               return "img.texture.house.wall.archedmarble";
            case WALL_PORTCULLIS_MARBLE:
               return "img.texture.house.wall.portcullismarble";
            case WALL_BARRED_MARBLE:
               return "img.texture.house.wall.bars1marble";
            case WALL_ORIEL_MARBLE:
               return "img.texture.house.wall.oriel2marble";
            case WALL_LEFT_ARCH_MARBLE:
               return "img.texture.house.wall.archleftmarble";
            case WALL_RIGHT_ARCH_MARBLE:
               return "img.texture.house.wall.archrightmarble";
            case WALL_T_ARCH_MARBLE:
               return "img.texture.house.wall.archtmarble";
            case NO_WALL:
               return "img.texture.house.wall.outline";
            case FENCE_MEDIUM_CHAIN:
               return "img.texture.fence.chain";
            case FENCE_PLAN_MEDIUM_CHAIN:
               return "img.texture.fence.plan.chain";
            case FENCE_PLAN_PORTCULLIS:
               return "img.texture.fence.plan.stonewallPortcullis";
            case FENCE_PORTCULLIS:
               return "img.texture.fence.stonewallPortcullis";
            case FENCE_SLATE_TALL_STONE_WALL:
               return "img.texture.fence.stonewall.tall.slate";
            case FENCE_SLATE_PORTCULLIS:
               return "img.texture.fence.stonewallPortcullis.slate";
            case FENCE_SLATE_HIGH_IRON_FENCE:
               return "img.texture.fence.gate.iron.high.slate";
            case FENCE_SLATE_HIGH_IRON_FENCE_GATE:
               return "img.texture.fence.iron.high.slate";
            case FENCE_SLATE_STONE_PARAPET:
               return "img.texture.fence.parapet.stone.slate";
            case FENCE_SLATE_CHAIN_FENCE:
               return "img.texture.fence.chain.slate";
            case FENCE_ROUNDED_STONE_TALL_STONE_WALL:
               return "img.texture.fence.stonewall.tall.roundedstone";
            case FENCE_ROUNDED_STONE_PORTCULLIS:
               return "img.texture.fence.stonewallPortcullis.roundedstone";
            case FENCE_ROUNDED_STONE_HIGH_IRON_FENCE:
               return "img.texture.fence.iron.high.roundedstone";
            case FENCE_ROUNDED_STONE_HIGH_IRON_FENCE_GATE:
               return "img.texture.fence.gate.iron.high.roundedstone";
            case FENCE_ROUNDED_STONE_STONE_PARAPET:
               return "img.texture.fence.parapet.stone.roundedstone";
            case FENCE_ROUNDED_STONE_CHAIN_FENCE:
               return "img.texture.fence.chain.roundedstone";
            case FENCE_SANDSTONE_TALL_STONE_WALL:
               return "img.texture.fence.stonewall.tall.sandstone";
            case FENCE_SANDSTONE_PORTCULLIS:
               return "img.texture.fence.stonewallPortcullis.sandstone";
            case FENCE_SANDSTONE_HIGH_IRON_FENCE:
               return "img.texture.fence.iron.high.sandstone";
            case FENCE_SANDSTONE_HIGH_IRON_FENCE_GATE:
               return "img.texture.fence.gate.iron.high.sandstone";
            case FENCE_SANDSTONE_STONE_PARAPET:
               return "img.texture.fence.parapet.stone.sandstone";
            case FENCE_SANDSTONE_CHAIN_FENCE:
               return "img.texture.fence.plan.chain.sandstone";
            case FENCE_RENDERED_TALL_STONE_WALL:
               return "img.texture.fence.stonewall.tall.rendered";
            case FENCE_RENDERED_PORTCULLIS:
               return "img.texture.fence.stonewallPortcullis.rendered";
            case FENCE_RENDERED_HIGH_IRON_FENCE:
               return "img.texture.fence.iron.high.rendered";
            case FENCE_RENDERED_HIGH_IRON_FENCE_GATE:
               return "img.texture.fence.gate.iron.high.rendered";
            case FENCE_RENDERED_STONE_PARAPET:
               return "img.texture.fence.parapet.stone.rendered";
            case FENCE_RENDERED_CHAIN_FENCE:
               return "img.texture.fence.parapet.stone.rendered";
            case FENCE_POTTERY_TALL_STONE_WALL:
               return "img.texture.fence.stonewall.tall.pottery";
            case FENCE_POTTERY_PORTCULLIS:
               return "img.texture.fence.stonewallPortcullis.pottery";
            case FENCE_POTTERY_HIGH_IRON_FENCE:
               return "img.texture.fence.iron.high.pottery";
            case FENCE_POTTERY_HIGH_IRON_FENCE_GATE:
               return "img.texture.fence.gate.iron.high.pottery";
            case FENCE_POTTERY_STONE_PARAPET:
               return "img.texture.fence.parapet.stone.pottery";
            case FENCE_POTTERY_CHAIN_FENCE:
               return "img.texture.fence.chain.pottery";
            case FENCE_MARBLE_TALL_STONE_WALL:
               return "img.texture.fence.stonewall.tall.marble ";
            case FENCE_MARBLE_PORTCULLIS:
               return "img.texture.fence.stonewallPortcullis.marble";
            case FENCE_MARBLE_HIGH_IRON_FENCE:
               return "img.texture.fence.iron.high.marble";
            case FENCE_MARBLE_HIGH_IRON_FENCE_GATE:
               return "img.texture.fence.gate.iron.high.marble";
            case FENCE_MARBLE_STONE_PARAPET:
               return "img.texture.fence.parapet.stone.marble";
            case FENCE_MARBLE_CHAIN_FENCE:
               return "img.texture.fence.chain.marble";
            case FENCE_PLAN_SLATE_TALL_STONE_WALL:
               return "img.texture.fence.plan.stonewall.tall.slate";
            case FENCE_PLAN_SLATE_PORTCULLIS:
               return "img.texture.fence.plan.stonewallPortcullis.slate";
            case FENCE_PLAN_SLATE_HIGH_IRON_FENCE:
               return "img.texture.fence.plan.iron.high.slate";
            case FENCE_PLAN_SLATE_HIGH_IRON_FENCE_GATE:
               return "img.texture.fence.plan.gate.iron.high.slate";
            case FENCE_PLAN_SLATE_STONE_PARAPET:
               return "img.texture.fence.plan.parapet.stone.slate";
            case FENCE_PLAN_SLATE_CHAIN_FENCE:
               return "img.texture.fence.plan.chain.slate";
            case FENCE_PLAN_ROUNDED_STONE_TALL_STONE_WALL:
               return "img.texture.fence.stonewall.tall.roundedstone";
            case FENCE_PLAN_ROUNDED_STONE_PORTCULLIS:
               return "img.texture.fence.plan.stonewallPortcullis.roundedstone";
            case FENCE_PLAN_ROUNDED_STONE_HIGH_IRON_FENCE:
               return "img.texture.fence.plan.iron.high.roundedstone";
            case FENCE_PLAN_ROUNDED_STONE_HIGH_IRON_FENCE_GATE:
               return "img.texture.fence.plan.gate.iron.high.roundedstone";
            case FENCE_PLAN_ROUNDED_STONE_STONE_PARAPET:
               return "img.texture.fence.plan.parapet.stone.roundedstone";
            case FENCE_PLAN_ROUNDED_STONE_CHAIN_FENCE:
               return "img.texture.fence.parapet.stone.roundedstone";
            case FENCE_PLAN_SANDSTONE_TALL_STONE_WALL:
               return "img.texture.fence.plan.stonewall.tall.sandstone";
            case FENCE_PLAN_SANDSTONE_PORTCULLIS:
               return "img.texture.fence.plan.stonewallPortcullis.sandstone";
            case FENCE_PLAN_SANDSTONE_HIGH_IRON_FENCE:
               return "img.texture.fence.plan.iron.high.sandstone";
            case FENCE_PLAN_SANDSTONE_HIGH_IRON_FENCE_GATE:
               return "img.texture.fence.plan.gate.iron.high.sandstone";
            case FENCE_PLAN_SANDSTONE_STONE_PARAPET:
               return "img.texture.fence.plan.parapet.stone.sandstone";
            case FENCE_PLAN_SANDSTONE_CHAIN_FENCE:
               return "img.texture.fence.parapet.stone.sandstone";
            case FENCE_PLAN_POTTERY_TALL_STONE_WALL:
               return "img.texture.fence.stonewall.tall.pottery";
            case FENCE_PLAN_POTTERY_PORTCULLIS:
               return "img.texture.fence.plan.stonewallPortcullis.pottery";
            case FENCE_PLAN_POTTERY_HIGH_IRON_FENCE:
               return "img.texture.fence.plan.iron.high.pottery";
            case FENCE_PLAN_POTTERY_HIGH_IRON_FENCE_GATE:
               return "img.texture.fence.plan.gate.iron.high.pottery";
            case FENCE_PLAN_POTTERY_STONE_PARAPET:
               return "img.texture.fence.plan.parapet.stone.pottery";
            case FENCE_PLAN_POTTERY_CHAIN_FENCE:
               return "img.texture.fence.plan.chain.pottery";
            case FENCE_PLAN_MARBLE_TALL_STONE_WALL:
               return "img.texture.fence.plan.stonewall.tall.marble";
            case FENCE_PLAN_MARBLE_PORTCULLIS:
               return "img.texture.fence.plan.stonewallPortcullis.marble";
            case FENCE_PLAN_MARBLE_HIGH_IRON_FENCE:
               return "img.texture.fence.plan.iron.high.marble";
            case FENCE_PLAN_MARBLE_HIGH_IRON_FENCE_GATE:
               return "img.texture.fence.plan.gate.iron.high.marble";
            case FENCE_PLAN_MARBLE_STONE_PARAPET:
               return "img.texture.fence.plan.parapet.stone.marble";
            case FENCE_PLAN_MARBLE_CHAIN_FENCE:
               return "img.texture.fence.plan.chain.marble";
            case WALL_SCAFFOLDING:
               return "img.texture.house.wall";
         }
      }
   }

   public static final String getName(StructureConstantsEnum type) {
      switch(type) {
         case FENCE_WOODEN:
            return "Wooden fence";
         case FENCE_WOODEN_CRUDE:
            return "Crude wooden fence";
         case FENCE_WOODEN_CRUDE_GATE:
            return "Crude wooden fence gate";
         case FENCE_PALISADE:
            return "Palisade";
         case FENCE_STONEWALL:
            return "Stone wall";
         case FENCE_WOODEN_GATE:
            return "Wooden fence gate";
         case FENCE_PALISADE_GATE:
            return "Palisade gate";
         case FENCE_STONEWALL_HIGH:
            return "Tall stone wall";
         case FENCE_IRON:
            return "Iron fence";
         case FENCE_SLATE_IRON:
            return "Slate iron fence";
         case FENCE_ROUNDED_STONE_IRON:
            return "Rounded stone iron fence";
         case FENCE_POTTERY_IRON:
            return "Pottery iron fence";
         case FENCE_SANDSTONE_IRON:
            return "Sandstone iron fence";
         case FENCE_RENDERED_IRON:
            return "Rendered iron fence";
         case FENCE_MARBLE_IRON:
            return "Marble iron fence";
         case FENCE_IRON_HIGH:
            return "High iron fence";
         case FENCE_IRON_GATE:
            return "Iron fence gate";
         case FENCE_SLATE_IRON_GATE:
            return "Slate iron fence gate";
         case FENCE_ROUNDED_STONE_IRON_GATE:
            return "Rounded stone iron fence gate";
         case FENCE_POTTERY_IRON_GATE:
            return "Pottery iron fence gate";
         case FENCE_SANDSTONE_IRON_GATE:
            return "Sandstone iron fence gate";
         case FENCE_RENDERED_IRON_GATE:
            return "Rendered iron fence gate";
         case FENCE_MARBLE_IRON_GATE:
            return "Marble fence gate";
         case FENCE_IRON_GATE_HIGH:
            return "High iron fence gate";
         case FENCE_WOVEN:
            return "Woven fence";
         case FENCE_PLAN_WOODEN:
            return "Incomplete wooden fence";
         case FENCE_PLAN_WOODEN_CRUDE:
            return "Incomplete crude wooden fence";
         case FENCE_PLAN_WOODEN_GATE_CRUDE:
            return "Incomplete crude wooden fence gate";
         case FENCE_PLAN_PALISADE:
            return "Incomplete palisade";
         case FENCE_PLAN_STONEWALL:
            return "Incomplete stone wall";
         case FENCE_PLAN_PALISADE_GATE:
            return "Incomplete palisade gate";
         case FENCE_PLAN_WOODEN_GATE:
            return "Incomplete wooden fence gate";
         case FENCE_PLAN_STONEWALL_HIGH:
            return "Incomplete tall stone wall";
         case FENCE_PLAN_IRON:
            return "Incomplete iron fence";
         case FENCE_PLAN_SLATE_IRON:
            return "Incomplete slate iron fence";
         case FENCE_PLAN_ROUNDED_STONE_IRON:
            return "Incomplete rounded stone iron fence";
         case FENCE_PLAN_POTTERY_IRON:
            return "Incomplete pottery iron fence";
         case FENCE_PLAN_SANDSTONE_IRON:
            return "Incomplete sandstone iron fence";
         case FENCE_PLAN_RENDERED_IRON:
            return "Incomplete rencered iron fence";
         case FENCE_PLAN_MARBLE_IRON:
            return "Incomplete marble iron fence";
         case FENCE_PLAN_IRON_HIGH:
            return "Incomplete high iron fence";
         case FENCE_PLAN_IRON_GATE:
            return "Incomplete iron fence gate";
         case FENCE_PLAN_SLATE_IRON_GATE:
            return "Incomplete slate iron fence gate";
         case FENCE_PLAN_ROUNDED_STONE_IRON_GATE:
            return "Incomplete rounded stone iron fence gate";
         case FENCE_PLAN_POTTERY_IRON_GATE:
            return "Incomplete pottery iron fence gate";
         case FENCE_PLAN_SANDSTONE_IRON_GATE:
            return "Incomplete sandstone iron fence gate";
         case FENCE_PLAN_RENDERED_IRON_GATE:
            return "Incomplete rencered iron gate";
         case FENCE_PLAN_MARBLE_IRON_GATE:
            return "Incomplete marble iron fence gate";
         case FENCE_PLAN_IRON_GATE_HIGH:
            return "Incomplete high iron fence gate";
         case FENCE_PLAN_WOVEN:
            return "Incomplete woven fence";
         case FENCE_STONE_PARAPET:
            return "Stone parapet";
         case FENCE_PLAN_STONE_PARAPET:
            return "Incomplete stone parapet";
         case FENCE_STONE_IRON_PARAPET:
            return "Stone and iron parapet";
         case FENCE_PLAN_STONE_IRON_PARAPET:
            return "Incomplete stone and iron parapet";
         case FENCE_WOODEN_PARAPET:
            return "Wooden parapet";
         case FENCE_PLAN_WOODEN_PARAPET:
            return "Incomplete wooden parapet";
         case FENCE_ROPE_LOW:
            return "Low rope fence";
         case FENCE_GARDESGARD_LOW:
            return "Low roundpole fence";
         case FENCE_GARDESGARD_HIGH:
            return "High roundpole fence";
         case FENCE_GARDESGARD_GATE:
            return "Roundpole fence gate";
         case FENCE_CURB:
            return "Curb";
         case FENCE_ROPE_HIGH:
            return "High rope fence";
         case FENCE_STONE:
            return "Stone fence";
         case FENCE_SLATE:
            return "Slate fence";
         case FENCE_ROUNDED_STONE:
            return "Rounded stone fence";
         case FENCE_POTTERY:
            return "Pottery fence";
         case FENCE_SANDSTONE:
            return "Sandstone fence";
         case FENCE_RENDERED:
            return "Rendered fence";
         case FENCE_MARBLE:
            return "Marble fence";
         case FENCE_PLAN_STONE:
            return "Incomplete stone fence";
         case FENCE_PLAN_SLATE:
            return "Incomplete slate fence";
         case FENCE_PLAN_ROUNDED_STONE:
            return "Incomplete rounded stone fence";
         case FENCE_PLAN_POTTERY:
            return "Incomplete pottery fence";
         case FENCE_PLAN_SANDSTONE:
            return "Incomplete sandstone fence";
         case FENCE_PLAN_RENDERED:
            return "Incomplete rendered fence";
         case FENCE_PLAN_MARBLE:
            return "Incomplete marble fence";
         case FENCE_PLAN_ROPE_LOW:
            return "Incomplete low rope fence";
         case FENCE_PLAN_GARDESGARD_LOW:
            return "Incomplete low roundpole fence";
         case FENCE_PLAN_GARDESGARD_HIGH:
            return "Incomplete high roundpole fence";
         case FENCE_PLAN_GARDESGARD_GATE:
            return "Incomplete roundpole gate";
         case FENCE_PLAN_CURB:
            return "Incomplete curb";
         case FENCE_PLAN_ROPE_HIGH:
            return "Incomplete high rope fence";
         case HEDGE_FLOWER1_LOW:
            return "Lavender plantation";
         case HEDGE_FLOWER1_MEDIUM:
            return "Lavender plantation";
         case HEDGE_FLOWER1_HIGH:
            return "Lavender plantation";
         case HEDGE_FLOWER2_LOW:
            return "Oleander hedge";
         case HEDGE_FLOWER2_MEDIUM:
            return "Oleander hedge";
         case HEDGE_FLOWER2_HIGH:
            return "Oleander hedge";
         case HEDGE_FLOWER3_LOW:
            return "Camellia hedge";
         case HEDGE_FLOWER3_MEDIUM:
            return "Camellia hedge";
         case HEDGE_FLOWER3_HIGH:
            return "Camellia hedge";
         case HEDGE_FLOWER4_LOW:
            return "Rose hedge";
         case HEDGE_FLOWER4_MEDIUM:
            return "Rose hedge";
         case HEDGE_FLOWER4_HIGH:
            return "Rose hedge";
         case HEDGE_FLOWER5_LOW:
            return "Thorn hedge";
         case HEDGE_FLOWER5_MEDIUM:
            return "Thorn hedge";
         case HEDGE_FLOWER5_HIGH:
            return "Thorn hedge";
         case HEDGE_FLOWER6_LOW:
            return "Cedar hedge";
         case HEDGE_FLOWER6_MEDIUM:
            return "Cedar hedge";
         case HEDGE_FLOWER6_HIGH:
            return "Cedar hedge";
         case HEDGE_FLOWER7_LOW:
            return "Maple hedge";
         case HEDGE_FLOWER7_MEDIUM:
            return "Maple hedge";
         case HEDGE_FLOWER7_HIGH:
            return "Maple hedge";
         case FENCE_MAGIC_STONE:
            return "Magic stone wall";
         case FENCE_MAGIC_FIRE:
            return "Magic wall of fire";
         case FENCE_MAGIC_ICE:
            return "Magic wall of ice";
         case FENCE_RUBBLE:
            return "Debris";
         case FENCE_SIEGEWALL:
            return "Siege Wall";
         case FLOWERBED_BLUE:
            return "Blue flowerbed";
         case FLOWERBED_GREENISH_YELLOW:
            return "Greenish-yellow flowerbed";
         case FLOWERBED_ORANGE_RED:
            return "Orange-red flowerbed";
         case FLOWERBED_PURPLE:
            return "Purple flowerbed";
         case FLOWERBED_WHITE:
            return "White flowerbed";
         case FLOWERBED_WHITE_DOTTED:
            return "White-dotted flowerbed";
         case FLOWERBED_YELLOW:
            return "Yellow flowerbed";
         case WALL_RUBBLE:
            return "Debris";
         case WALL_SOLID_WOODEN:
            return "Wooden wall";
         case WALL_CANOPY_WOODEN:
            return "Wooden canopy";
         case WALL_WINDOW_WOODEN:
            return "Wooden window";
         case WALL_WINDOW_WIDE_WOODEN:
            return "Wooden wide window";
         case WALL_DOOR_WOODEN:
            return "Wooden door";
         case WALL_DOUBLE_DOOR_WOODEN:
            return "Wooden door";
         case WALL_SOLID_STONE_DECORATED:
            return "Stone wall";
         case WALL_ORIEL_STONE_DECORATED:
            return "Stone oriel";
         case WALL_ORIEL_STONE_PLAIN:
            return "Plain stone oriel";
         case WALL_SOLID_STONE:
            return "Plain stone wall";
         case WALL_BARRED_STONE:
            return "Plain barred wall";
         case WALL_WINDOW_STONE:
            return "Plain stone window";
         case WALL_PLAIN_NARROW_WINDOW:
            return "Plain narrow stone window";
         case WALL_DOOR_STONE:
            return "Plain stone door";
         case WALL_WINDOW_STONE_DECORATED:
            return "Stone window";
         case WALL_DOOR_STONE_DECORATED:
            return "Stone door";
         case WALL_DOUBLE_DOOR_STONE_DECORATED:
            return "Stone door";
         case WALL_PLAIN_NARROW_WINDOW_PLAN:
            return "Incomplete plain narrow window";
         case WALL_PORTCULLIS_STONE:
            return "Plain stone portcullis";
         case WALL_PORTCULLIS_WOOD:
            return "Wooden portcullis";
         case WALL_PORTCULLIS_STONE_DECORATED:
            return "Stone portcullis";
         case WALL_DOUBLE_DOOR_STONE:
            return "Plain stone double door";
         case WALL_DOOR_ARCHED_WOODEN:
            return "Wooden arched wall";
         case WALL_LEFT_ARCH_WOODEN:
            return "Wooden left arch";
         case WALL_RIGHT_ARCH_WOODEN:
            return "Wooden right arch";
         case WALL_T_ARCH_WOODEN:
            return "Wooden T arch";
         case WALL_DOUBLE_DOOR_WOODEN_PLAN:
            return "Unknown wall type " + type;
         case WALL_DOOR_ARCHED_WOODEN_PLAN:
            return "Wooden arched wall plan";
         case WALL_DOUBLE_DOOR_STONE_PLAN:
            return "Incomplete Plain stone double door";
         case WALL_DOOR_ARCHED_PLAN:
            return "Stone arch plan";
         case WALL_DOOR_ARCHED_STONE_DECORATED:
            return "Stone arch wall";
         case WALL_LEFT_ARCH_STONE_DECORATED:
            return "Stone left arch";
         case WALL_RIGHT_ARCH_STONE_DECORATED:
            return "Stone right arch";
         case WALL_T_ARCH_STONE_DECORATED:
            return "Stone T arch";
         case WALL_DOOR_ARCHED_STONE:
            return "Plain stone arch wall";
         case WALL_LEFT_ARCH_STONE:
            return "Plain stone left arch";
         case WALL_RIGHT_ARCH_STONE:
            return "Plain stone right arch";
         case WALL_T_ARCH_STONE:
            return "Plain stone T arch";
         case WALL_SOLID_WOODEN_PLAN:
            return "Wooden wall plan";
         case WALL_WINDOW_WOODEN_PLAN:
            return "Wooden window plan";
         case WALL_DOOR_WOODEN_PLAN:
            return "Wooden door plan";
         case WALL_CANOPY_WOODEN_PLAN:
            return "Wooden canopy door plan";
         case WALL_SOLID_STONE_PLAN:
            return "Stone wall plan";
         case WALL_ORIEL_STONE_DECORATED_PLAN:
            return "Stone oriel plan";
         case WALL_WINDOW_STONE_PLAN:
            return "Stone window plan";
         case WALL_DOOR_STONE_PLAN:
            return "Stone door plan";
         case WALL_SOLID_TIMBER_FRAMED:
            return "Timber framed wall";
         case WALL_WINDOW_TIMBER_FRAMED:
            return "Timber framed window";
         case WALL_DOOR_TIMBER_FRAMED:
            return "Timber framed door";
         case WALL_DOUBLE_DOOR_TIMBER_FRAMED:
            return "Timber framed double door";
         case WALL_DOOR_ARCHED_TIMBER_FRAMED:
            return "Timber framed arched wall";
         case WALL_LEFT_ARCH_TIMBER_FRAMED:
            return "Timber framed left arch";
         case WALL_RIGHT_ARCH_TIMBER_FRAMED:
            return "Timber framed right arch";
         case WALL_T_ARCH_TIMBER_FRAMED:
            return "Timber framed T arch";
         case WALL_BALCONY_TIMBER_FRAMED:
            return "Timber framed balcony";
         case WALL_JETTY_TIMBER_FRAMED:
            return "Timber framed jetty";
         case WALL_SOLID_TIMBER_FRAMED_PLAN:
            return "Timber framed wall plan";
         case WALL_BALCONY_TIMBER_FRAMED_PLAN:
            return "Timber framed balcony plan";
         case WALL_JETTY_TIMBER_FRAMED_PLAN:
            return "Timber framed jetty plan";
         case WALL_WINDOW_TIMBER_FRAMED_PLAN:
            return "Timber framed window plan";
         case WALL_DOOR_TIMBER_FRAMED_PLAN:
            return "Timber framed door plan";
         case WALL_DOUBLE_DOOR_TIMBER_FRAMED_PLAN:
            return "Timber framed double door plan";
         case WALL_DOOR_ARCHED_TIMBER_FRAMED_PLAN:
            return "Timber framed arch plan";
         case WALL_SOLID_SLATE:
            return "Slate wall";
         case WALL_WINDOW_SLATE:
            return "Slate window";
         case WALL_NARROW_WINDOW_SLATE:
            return "Slate narrow window";
         case WALL_DOOR_SLATE:
            return "Slate door";
         case WALL_DOUBLE_DOOR_SLATE:
            return "Slate double door";
         case WALL_ARCHED_SLATE:
            return "Slate arched wall";
         case WALL_PORTCULLIS_SLATE:
            return "Slate portcullis";
         case WALL_BARRED_SLATE:
            return "Slate barred wall";
         case WALL_ORIEL_SLATE:
            return "Slate oriel window";
         case WALL_LEFT_ARCH_SLATE:
            return "Slate left arch";
         case WALL_RIGHT_ARCH_SLATE:
            return "Slate right arch";
         case WALL_T_ARCH_SLATE:
            return "Slate T arch";
         case WALL_SOLID_ROUNDED_STONE:
            return "Rounded stone wall";
         case WALL_WINDOW_ROUNDED_STONE:
            return "Rounded stone window";
         case WALL_NARROW_WINDOW_ROUNDED_STONE:
            return "Rounded stone narrow window";
         case WALL_DOOR_ROUNDED_STONE:
            return "Rounded stone door";
         case WALL_DOUBLE_DOOR_ROUNDED_STONE:
            return "Rounded stone double door";
         case WALL_ARCHED_ROUNDED_STONE:
            return "Rounded stone arched wall";
         case WALL_PORTCULLIS_ROUNDED_STONE:
            return "Rounded stone portcullis";
         case WALL_BARRED_ROUNDED_STONE:
            return "Rounded stone barred wall";
         case WALL_ORIEL_ROUNDED_STONE:
            return "Rounded stone oriel window";
         case WALL_LEFT_ARCH_ROUNDED_STONE:
            return "Rounded stone left arch";
         case WALL_RIGHT_ARCH_ROUNDED_STONE:
            return "Rounded stone right arch";
         case WALL_T_ARCH_ROUNDED_STONE:
            return "Rounded stone T arch";
         case WALL_SOLID_POTTERY:
            return "Pottery wall";
         case WALL_WINDOW_POTTERY:
            return "Pottery window";
         case WALL_NARROW_WINDOW_POTTERY:
            return "Pottery narrow window";
         case WALL_DOOR_POTTERY:
            return "Pottery door";
         case WALL_DOUBLE_DOOR_POTTERY:
            return "Pottery double door";
         case WALL_ARCHED_POTTERY:
            return "Pottery arched wall";
         case WALL_PORTCULLIS_POTTERY:
            return "Pottery portcullis";
         case WALL_BARRED_POTTERY:
            return "Pottery barred wall";
         case WALL_ORIEL_POTTERY:
            return "Pottery oriel window";
         case WALL_LEFT_ARCH_POTTERY:
            return "Pottery left arch";
         case WALL_RIGHT_ARCH_POTTERY:
            return "Pottery right arch";
         case WALL_T_ARCH_POTTERY:
            return "Pottery T arch";
         case WALL_SOLID_SANDSTONE:
            return "Sandstone wall";
         case WALL_WINDOW_SANDSTONE:
            return "Sandstone window";
         case WALL_NARROW_WINDOW_SANDSTONE:
            return "Sandstone narrow window";
         case WALL_DOOR_SANDSTONE:
            return "Sandstone door";
         case WALL_DOUBLE_DOOR_SANDSTONE:
            return "Sandstone double door";
         case WALL_ARCHED_SANDSTONE:
            return "Sandstone arched wall";
         case WALL_PORTCULLIS_SANDSTONE:
            return "Sandstone portcullis";
         case WALL_BARRED_SANDSTONE:
            return "Sandstone barred wall";
         case WALL_ORIEL_SANDSTONE:
            return "Sandstone oriel window";
         case WALL_LEFT_ARCH_SANDSTONE:
            return "Sandstone left arch";
         case WALL_RIGHT_ARCH_SANDSTONE:
            return "Sandstone right arch";
         case WALL_T_ARCH_SANDSTONE:
            return "Sandstone T arch";
         case WALL_SOLID_RENDERED:
            return "Rendered wall";
         case WALL_WINDOW_RENDERED:
            return "Rendered window";
         case WALL_NARROW_WINDOW_RENDERED:
            return "Rendered narrow window";
         case WALL_DOOR_RENDERED:
            return "Rendered door";
         case WALL_DOUBLE_DOOR_RENDERED:
            return "Rendered double door";
         case WALL_ARCHED_RENDERED:
            return "Rendered arched wall";
         case WALL_PORTCULLIS_RENDERED:
            return "Rendered portcullis";
         case WALL_BARRED_RENDERED:
            return "Rendered barred wall";
         case WALL_ORIEL_RENDERED:
            return "Rendered oriel window";
         case WALL_LEFT_ARCH_RENDERED:
            return "Rendered left arch";
         case WALL_RIGHT_ARCH_RENDERED:
            return "Rendered right arch";
         case WALL_T_ARCH_RENDERED:
            return "Rendered T arch";
         case WALL_SOLID_MARBLE:
            return "Marble wall";
         case WALL_WINDOW_MARBLE:
            return "Marble window";
         case WALL_NARROW_WINDOW_MARBLE:
            return "Marble narrow window";
         case WALL_DOOR_MARBLE:
            return "Marble door";
         case WALL_DOUBLE_DOOR_MARBLE:
            return "Marble double door";
         case WALL_ARCHED_MARBLE:
            return "Marble arched wall";
         case WALL_PORTCULLIS_MARBLE:
            return "Marble portcullis";
         case WALL_BARRED_MARBLE:
            return "Marble barred wall";
         case WALL_ORIEL_MARBLE:
            return "Marble oriel window";
         case WALL_LEFT_ARCH_MARBLE:
            return "Marble left arch";
         case WALL_RIGHT_ARCH_MARBLE:
            return "Marble right arch";
         case WALL_T_ARCH_MARBLE:
            return "Marble T arch";
         case NO_WALL:
            return "Missing wall";
         case FENCE_MEDIUM_CHAIN:
            return "Chain fence";
         case FENCE_PLAN_MEDIUM_CHAIN:
            return "Incomplete chain fence";
         case FENCE_PLAN_PORTCULLIS:
            return "Incomplete portcullis";
         case FENCE_PORTCULLIS:
            return "Portcullis";
         case FENCE_SLATE_TALL_STONE_WALL:
            return "Tall slate stone wall";
         case FENCE_SLATE_PORTCULLIS:
            return "Slate portcullis";
         case FENCE_SLATE_HIGH_IRON_FENCE:
            return "Slate high iron fence";
         case FENCE_SLATE_HIGH_IRON_FENCE_GATE:
            return "Slate high iron fence gate";
         case FENCE_SLATE_STONE_PARAPET:
            return "Slate stone parapet";
         case FENCE_SLATE_CHAIN_FENCE:
            return "Slate chain fence";
         case FENCE_ROUNDED_STONE_TALL_STONE_WALL:
            return "Tall rounded stone wall";
         case FENCE_ROUNDED_STONE_PORTCULLIS:
            return "Rounded stone portcullis";
         case FENCE_ROUNDED_STONE_HIGH_IRON_FENCE:
            return "Rounded stone high iron fence";
         case FENCE_ROUNDED_STONE_HIGH_IRON_FENCE_GATE:
            return "Rounded stone high iron fence gate";
         case FENCE_ROUNDED_STONE_STONE_PARAPET:
            return "Rounded stone parapet";
         case FENCE_ROUNDED_STONE_CHAIN_FENCE:
            return "Rounded stone chain fence";
         case FENCE_SANDSTONE_TALL_STONE_WALL:
            return "Tall sandstone wall";
         case FENCE_SANDSTONE_PORTCULLIS:
            return "Sandstone portcullis";
         case FENCE_SANDSTONE_HIGH_IRON_FENCE:
            return "Sandstone high iron fence";
         case FENCE_SANDSTONE_HIGH_IRON_FENCE_GATE:
            return "Sandstone high iron fence gate";
         case FENCE_SANDSTONE_STONE_PARAPET:
            return "Sandstone parapet";
         case FENCE_SANDSTONE_CHAIN_FENCE:
            return "Sandstone chain fence";
         case FENCE_RENDERED_TALL_STONE_WALL:
            return "Tall rendered stone wall";
         case FENCE_RENDERED_PORTCULLIS:
            return "Rendered portcullis";
         case FENCE_RENDERED_HIGH_IRON_FENCE:
            return "Rendered high iron fence";
         case FENCE_RENDERED_HIGH_IRON_FENCE_GATE:
            return "Rendered high iron fence gate";
         case FENCE_RENDERED_STONE_PARAPET:
            return "Rendered stone parapet";
         case FENCE_RENDERED_CHAIN_FENCE:
            return "Rendered chain fence";
         case FENCE_POTTERY_TALL_STONE_WALL:
            return "Tall pottery wall";
         case FENCE_POTTERY_PORTCULLIS:
            return "Pottery portcullis";
         case FENCE_POTTERY_HIGH_IRON_FENCE:
            return "Pottery high iron fence";
         case FENCE_POTTERY_HIGH_IRON_FENCE_GATE:
            return "Pottery high iron fence gate";
         case FENCE_POTTERY_STONE_PARAPET:
            return "Pottery parapet";
         case FENCE_POTTERY_CHAIN_FENCE:
            return "Pottery chain fence";
         case FENCE_MARBLE_TALL_STONE_WALL:
            return "Tall marble wall";
         case FENCE_MARBLE_PORTCULLIS:
            return "Marble portcullis";
         case FENCE_MARBLE_HIGH_IRON_FENCE:
            return "Marble high iron fence";
         case FENCE_MARBLE_HIGH_IRON_FENCE_GATE:
            return "Marble high iron fence gate";
         case FENCE_MARBLE_STONE_PARAPET:
            return "Marble parapet";
         case FENCE_MARBLE_CHAIN_FENCE:
            return "Marble chain fence";
         case FENCE_PLAN_SLATE_TALL_STONE_WALL:
            return "Incomplete tall slate wall";
         case FENCE_PLAN_SLATE_PORTCULLIS:
            return "Incomplete slate portcullis";
         case FENCE_PLAN_SLATE_HIGH_IRON_FENCE:
            return "Incomplete slate high iron fence";
         case FENCE_PLAN_SLATE_HIGH_IRON_FENCE_GATE:
            return "Incomplete slate high iron fence gate";
         case FENCE_PLAN_SLATE_STONE_PARAPET:
            return "Incomplete slate parapet";
         case FENCE_PLAN_SLATE_CHAIN_FENCE:
            return "Incomplete slate chain fence";
         case FENCE_PLAN_ROUNDED_STONE_TALL_STONE_WALL:
            return "Incomplete tall rounded stone wall";
         case FENCE_PLAN_ROUNDED_STONE_PORTCULLIS:
            return "Incomplete rounded stone portcullis";
         case FENCE_PLAN_ROUNDED_STONE_HIGH_IRON_FENCE:
            return "Incomplete rounded stone high iron fence";
         case FENCE_PLAN_ROUNDED_STONE_HIGH_IRON_FENCE_GATE:
            return "Incomplete rounded stone high iron fence gate";
         case FENCE_PLAN_ROUNDED_STONE_STONE_PARAPET:
            return "Incomplete rounded stone parapet";
         case FENCE_PLAN_ROUNDED_STONE_CHAIN_FENCE:
            return "Incomplete rounded stone chain fence";
         case FENCE_PLAN_SANDSTONE_TALL_STONE_WALL:
            return "Incomplete tall sandstone wall";
         case FENCE_PLAN_SANDSTONE_PORTCULLIS:
            return "Incomplete sandstone portcullis";
         case FENCE_PLAN_SANDSTONE_HIGH_IRON_FENCE:
            return "Incomplete sandstone high iron fence";
         case FENCE_PLAN_SANDSTONE_HIGH_IRON_FENCE_GATE:
            return "Incomplete sandstone high iron fence gate";
         case FENCE_PLAN_SANDSTONE_STONE_PARAPET:
            return "Incomplete sandstone parapet";
         case FENCE_PLAN_SANDSTONE_CHAIN_FENCE:
            return "Incomplete sandstone chain fence";
         case FENCE_PLAN_RENDERED_TALL_STONE_WALL:
            return "Incomplete tall rendered stone wall";
         case FENCE_PLAN_RENDERED_PORTCULLIS:
            return "Incomplete rendered portcullis";
         case FENCE_PLAN_RENDERED_HIGH_IRON_FENCE:
            return "Incomplete rendered hign iron fence";
         case FENCE_PLAN_RENDERED_HIGH_IRON_FENCE_GATE:
            return "Incomplete rendered high iron fence gate";
         case FENCE_PLAN_RENDERED_STONE_PARAPET:
            return "Incomplete rendered parapet";
         case FENCE_PLAN_RENDERED_CHAIN_FENCE:
            return "Incomplete rendered chain fence";
         case FENCE_PLAN_POTTERY_TALL_STONE_WALL:
            return "Incomplete tall pottery wall";
         case FENCE_PLAN_POTTERY_PORTCULLIS:
            return "Incomplete pottery portcullis";
         case FENCE_PLAN_POTTERY_HIGH_IRON_FENCE:
            return "Incomplete pottery high iron fence";
         case FENCE_PLAN_POTTERY_HIGH_IRON_FENCE_GATE:
            return "Incomplete pottery high iron fence gate";
         case FENCE_PLAN_POTTERY_STONE_PARAPET:
            return "Incomplete pottery parapet";
         case FENCE_PLAN_POTTERY_CHAIN_FENCE:
            return "incomplete pottery chain fence";
         case FENCE_PLAN_MARBLE_TALL_STONE_WALL:
            return "Incomplete tall marble wall";
         case FENCE_PLAN_MARBLE_PORTCULLIS:
            return "Incomplete marble portcullis";
         case FENCE_PLAN_MARBLE_HIGH_IRON_FENCE:
            return "Incomplete marble high iron fence";
         case FENCE_PLAN_MARBLE_HIGH_IRON_FENCE_GATE:
            return "Incomplete marble high iron fence gate";
         case FENCE_PLAN_MARBLE_STONE_PARAPET:
            return "Incomplete marble parapet";
         case FENCE_PLAN_MARBLE_CHAIN_FENCE:
            return "incomplete marble chain fence";
         case WALL_SCAFFOLDING:
            return "Wooden scaffolding";
         default:
            return "Unknown wall type " + type;
      }
   }

   public static final int getIconId(StructureConstantsEnum type) {
      return getIconId(type, false);
   }

   public static final int getIconId(StructureConstantsEnum type, boolean initializing) {
      if (!initializing) {
         return type.getIconId();
      } else {
         switch(type) {
            case FENCE_WOODEN:
               return 60;
            case FENCE_WOODEN_CRUDE:
               return 60;
            case FENCE_WOODEN_CRUDE_GATE:
               return 60;
            case FENCE_PALISADE:
               return 60;
            case FENCE_STONEWALL:
               return 60;
            case FENCE_WOODEN_GATE:
               return 60;
            case FENCE_PALISADE_GATE:
               return 60;
            case FENCE_STONEWALL_HIGH:
               return 60;
            case FENCE_IRON:
               return 60;
            case FENCE_SLATE_IRON:
            case FENCE_ROUNDED_STONE_IRON:
            case FENCE_POTTERY_IRON:
            case FENCE_SANDSTONE_IRON:
            case FENCE_RENDERED_IRON:
            case FENCE_MARBLE_IRON:
            case FENCE_SLATE_IRON_GATE:
            case FENCE_ROUNDED_STONE_IRON_GATE:
            case FENCE_POTTERY_IRON_GATE:
            case FENCE_SANDSTONE_IRON_GATE:
            case FENCE_RENDERED_IRON_GATE:
            case FENCE_MARBLE_IRON_GATE:
            case FENCE_PLAN_SLATE_IRON:
            case FENCE_PLAN_ROUNDED_STONE_IRON:
            case FENCE_PLAN_POTTERY_IRON:
            case FENCE_PLAN_SANDSTONE_IRON:
            case FENCE_PLAN_RENDERED_IRON:
            case FENCE_PLAN_MARBLE_IRON:
            case FENCE_PLAN_SLATE_IRON_GATE:
            case FENCE_PLAN_ROUNDED_STONE_IRON_GATE:
            case FENCE_PLAN_POTTERY_IRON_GATE:
            case FENCE_PLAN_SANDSTONE_IRON_GATE:
            case FENCE_PLAN_RENDERED_IRON_GATE:
            case FENCE_PLAN_MARBLE_IRON_GATE:
            case FENCE_SLATE:
            case FENCE_ROUNDED_STONE:
            case FENCE_POTTERY:
            case FENCE_SANDSTONE:
            case FENCE_RENDERED:
            case FENCE_MARBLE:
            case FENCE_PLAN_SLATE:
            case FENCE_PLAN_ROUNDED_STONE:
            case FENCE_PLAN_POTTERY:
            case FENCE_PLAN_SANDSTONE:
            case FENCE_PLAN_RENDERED:
            case FENCE_PLAN_MARBLE:
            case WALL_PLAIN_NARROW_WINDOW_PLAN:
            case WALL_DOUBLE_DOOR_WOODEN_PLAN:
            case WALL_DOUBLE_DOOR_STONE_PLAN:
            case WALL_LEFT_ARCH_STONE_DECORATED:
            case WALL_RIGHT_ARCH_STONE_DECORATED:
            case WALL_T_ARCH_STONE_DECORATED:
            default:
               return 60;
            case FENCE_IRON_HIGH:
               return 60;
            case FENCE_IRON_GATE:
               return 60;
            case FENCE_IRON_GATE_HIGH:
               return 60;
            case FENCE_WOVEN:
               return 60;
            case FENCE_PLAN_WOODEN:
               return 60;
            case FENCE_PLAN_WOODEN_CRUDE:
               return 60;
            case FENCE_PLAN_WOODEN_GATE_CRUDE:
               return 60;
            case FENCE_PLAN_PALISADE:
               return 60;
            case FENCE_PLAN_STONEWALL:
               return 60;
            case FENCE_PLAN_PALISADE_GATE:
               return 60;
            case FENCE_PLAN_WOODEN_GATE:
               return 60;
            case FENCE_PLAN_STONEWALL_HIGH:
               return 60;
            case FENCE_PLAN_IRON:
               return 60;
            case FENCE_PLAN_IRON_HIGH:
               return 60;
            case FENCE_PLAN_IRON_GATE:
               return 60;
            case FENCE_PLAN_IRON_GATE_HIGH:
               return 60;
            case FENCE_PLAN_WOVEN:
               return 60;
            case FENCE_STONE_PARAPET:
               return 60;
            case FENCE_PLAN_STONE_PARAPET:
               return 60;
            case FENCE_STONE_IRON_PARAPET:
               return 60;
            case FENCE_PLAN_STONE_IRON_PARAPET:
               return 60;
            case FENCE_WOODEN_PARAPET:
               return 60;
            case FENCE_PLAN_WOODEN_PARAPET:
               return 60;
            case FENCE_ROPE_LOW:
               return 60;
            case FENCE_GARDESGARD_LOW:
               return 60;
            case FENCE_GARDESGARD_HIGH:
               return 60;
            case FENCE_GARDESGARD_GATE:
               return 60;
            case FENCE_CURB:
               return 60;
            case FENCE_ROPE_HIGH:
               return 60;
            case FENCE_STONE:
               return 60;
            case FENCE_PLAN_STONE:
               return 60;
            case FENCE_PLAN_ROPE_LOW:
               return 60;
            case FENCE_PLAN_GARDESGARD_LOW:
               return 60;
            case FENCE_PLAN_GARDESGARD_HIGH:
               return 60;
            case FENCE_PLAN_GARDESGARD_GATE:
               return 60;
            case FENCE_PLAN_CURB:
               return 60;
            case FENCE_PLAN_ROPE_HIGH:
               return 60;
            case HEDGE_FLOWER1_LOW:
               return 60;
            case HEDGE_FLOWER1_MEDIUM:
               return 60;
            case HEDGE_FLOWER1_HIGH:
               return 60;
            case HEDGE_FLOWER2_LOW:
               return 60;
            case HEDGE_FLOWER2_MEDIUM:
               return 60;
            case HEDGE_FLOWER2_HIGH:
               return 60;
            case HEDGE_FLOWER3_LOW:
               return 60;
            case HEDGE_FLOWER3_MEDIUM:
               return 60;
            case HEDGE_FLOWER3_HIGH:
               return 60;
            case HEDGE_FLOWER4_LOW:
               return 60;
            case HEDGE_FLOWER4_MEDIUM:
               return 60;
            case HEDGE_FLOWER4_HIGH:
               return 60;
            case HEDGE_FLOWER5_LOW:
               return 60;
            case HEDGE_FLOWER5_MEDIUM:
               return 60;
            case HEDGE_FLOWER5_HIGH:
               return 60;
            case HEDGE_FLOWER6_LOW:
               return 60;
            case HEDGE_FLOWER6_MEDIUM:
               return 60;
            case HEDGE_FLOWER6_HIGH:
               return 60;
            case HEDGE_FLOWER7_LOW:
               return 60;
            case HEDGE_FLOWER7_MEDIUM:
               return 60;
            case HEDGE_FLOWER7_HIGH:
               return 60;
            case FENCE_MAGIC_STONE:
               return 60;
            case FENCE_MAGIC_FIRE:
               return 60;
            case FENCE_MAGIC_ICE:
               return 60;
            case FENCE_RUBBLE:
               return 60;
            case FENCE_SIEGEWALL:
               return 60;
            case FLOWERBED_BLUE:
               return 60;
            case FLOWERBED_GREENISH_YELLOW:
               return 60;
            case FLOWERBED_ORANGE_RED:
               return 60;
            case FLOWERBED_PURPLE:
               return 60;
            case FLOWERBED_WHITE:
               return 60;
            case FLOWERBED_WHITE_DOTTED:
               return 60;
            case FLOWERBED_YELLOW:
               return 60;
            case WALL_RUBBLE:
               return 60;
            case WALL_SOLID_WOODEN:
               return 60;
            case WALL_CANOPY_WOODEN:
               return 60;
            case WALL_WINDOW_WOODEN:
               return 60;
            case WALL_WINDOW_WIDE_WOODEN:
               return 60;
            case WALL_DOOR_WOODEN:
               return 60;
            case WALL_DOUBLE_DOOR_WOODEN:
               return 60;
            case WALL_SOLID_STONE_DECORATED:
               return 60;
            case WALL_ORIEL_STONE_DECORATED:
               return 60;
            case WALL_ORIEL_STONE_PLAIN:
               return 60;
            case WALL_SOLID_STONE:
               return 60;
            case WALL_BARRED_STONE:
               return 60;
            case WALL_WINDOW_STONE:
               return 60;
            case WALL_PLAIN_NARROW_WINDOW:
               return 60;
            case WALL_DOOR_STONE:
               return 60;
            case WALL_WINDOW_STONE_DECORATED:
               return 60;
            case WALL_DOOR_STONE_DECORATED:
               return 60;
            case WALL_DOUBLE_DOOR_STONE_DECORATED:
               return 60;
            case WALL_PORTCULLIS_STONE:
               return 60;
            case WALL_PORTCULLIS_WOOD:
               return 60;
            case WALL_PORTCULLIS_STONE_DECORATED:
               return 60;
            case WALL_DOUBLE_DOOR_STONE:
               return 60;
            case WALL_DOOR_ARCHED_WOODEN:
               return 60;
            case WALL_LEFT_ARCH_WOODEN:
               return 60;
            case WALL_RIGHT_ARCH_WOODEN:
               return 60;
            case WALL_T_ARCH_WOODEN:
               return 60;
            case WALL_DOOR_ARCHED_WOODEN_PLAN:
               return 60;
            case WALL_DOOR_ARCHED_PLAN:
               return 60;
            case WALL_DOOR_ARCHED_STONE_DECORATED:
               return 60;
            case WALL_DOOR_ARCHED_STONE:
               return 60;
            case WALL_LEFT_ARCH_STONE:
               return 60;
            case WALL_RIGHT_ARCH_STONE:
               return 60;
            case WALL_T_ARCH_STONE:
               return 60;
            case WALL_SOLID_WOODEN_PLAN:
               return 60;
            case WALL_WINDOW_WOODEN_PLAN:
               return 60;
            case WALL_DOOR_WOODEN_PLAN:
               return 60;
            case WALL_CANOPY_WOODEN_PLAN:
               return 60;
            case WALL_SOLID_STONE_PLAN:
               return 60;
            case WALL_ORIEL_STONE_DECORATED_PLAN:
               return 60;
            case WALL_WINDOW_STONE_PLAN:
               return 60;
            case WALL_DOOR_STONE_PLAN:
               return 60;
            case WALL_SOLID_TIMBER_FRAMED:
               return 60;
            case WALL_WINDOW_TIMBER_FRAMED:
               return 60;
            case WALL_DOOR_TIMBER_FRAMED:
               return 60;
            case WALL_DOUBLE_DOOR_TIMBER_FRAMED:
               return 60;
            case WALL_DOOR_ARCHED_TIMBER_FRAMED:
               return 60;
            case WALL_LEFT_ARCH_TIMBER_FRAMED:
               return 60;
            case WALL_RIGHT_ARCH_TIMBER_FRAMED:
               return 60;
            case WALL_T_ARCH_TIMBER_FRAMED:
               return 60;
            case WALL_BALCONY_TIMBER_FRAMED:
               return 60;
            case WALL_JETTY_TIMBER_FRAMED:
               return 60;
            case WALL_SOLID_TIMBER_FRAMED_PLAN:
               return 60;
            case WALL_BALCONY_TIMBER_FRAMED_PLAN:
               return 60;
            case WALL_JETTY_TIMBER_FRAMED_PLAN:
               return 60;
            case WALL_WINDOW_TIMBER_FRAMED_PLAN:
               return 60;
            case WALL_DOOR_TIMBER_FRAMED_PLAN:
               return 60;
            case WALL_DOUBLE_DOOR_TIMBER_FRAMED_PLAN:
               return 60;
            case WALL_DOOR_ARCHED_TIMBER_FRAMED_PLAN:
               return 60;
            case WALL_SOLID_SLATE:
               return 60;
            case WALL_WINDOW_SLATE:
               return 60;
            case WALL_NARROW_WINDOW_SLATE:
               return 60;
            case WALL_DOOR_SLATE:
               return 60;
            case WALL_DOUBLE_DOOR_SLATE:
               return 60;
            case WALL_ARCHED_SLATE:
               return 60;
            case WALL_PORTCULLIS_SLATE:
               return 60;
            case WALL_BARRED_SLATE:
               return 60;
            case WALL_ORIEL_SLATE:
               return 60;
            case WALL_LEFT_ARCH_SLATE:
               return 60;
            case WALL_RIGHT_ARCH_SLATE:
               return 60;
            case WALL_T_ARCH_SLATE:
               return 60;
            case WALL_SOLID_ROUNDED_STONE:
               return 60;
            case WALL_WINDOW_ROUNDED_STONE:
               return 60;
            case WALL_NARROW_WINDOW_ROUNDED_STONE:
               return 60;
            case WALL_DOOR_ROUNDED_STONE:
               return 60;
            case WALL_DOUBLE_DOOR_ROUNDED_STONE:
               return 60;
            case WALL_ARCHED_ROUNDED_STONE:
               return 60;
            case WALL_PORTCULLIS_ROUNDED_STONE:
               return 60;
            case WALL_BARRED_ROUNDED_STONE:
               return 60;
            case WALL_ORIEL_ROUNDED_STONE:
               return 60;
            case WALL_LEFT_ARCH_ROUNDED_STONE:
               return 60;
            case WALL_RIGHT_ARCH_ROUNDED_STONE:
               return 60;
            case WALL_T_ARCH_ROUNDED_STONE:
               return 60;
            case WALL_SOLID_POTTERY:
               return 60;
            case WALL_WINDOW_POTTERY:
               return 60;
            case WALL_NARROW_WINDOW_POTTERY:
               return 60;
            case WALL_DOOR_POTTERY:
               return 60;
            case WALL_DOUBLE_DOOR_POTTERY:
               return 60;
            case WALL_ARCHED_POTTERY:
               return 60;
            case WALL_PORTCULLIS_POTTERY:
               return 60;
            case WALL_BARRED_POTTERY:
               return 60;
            case WALL_ORIEL_POTTERY:
               return 60;
            case WALL_LEFT_ARCH_POTTERY:
               return 60;
            case WALL_RIGHT_ARCH_POTTERY:
               return 60;
            case WALL_T_ARCH_POTTERY:
               return 60;
            case WALL_SOLID_SANDSTONE:
               return 60;
            case WALL_WINDOW_SANDSTONE:
               return 60;
            case WALL_NARROW_WINDOW_SANDSTONE:
               return 60;
            case WALL_DOOR_SANDSTONE:
               return 60;
            case WALL_DOUBLE_DOOR_SANDSTONE:
               return 60;
            case WALL_ARCHED_SANDSTONE:
               return 60;
            case WALL_PORTCULLIS_SANDSTONE:
               return 60;
            case WALL_BARRED_SANDSTONE:
               return 60;
            case WALL_ORIEL_SANDSTONE:
               return 60;
            case WALL_LEFT_ARCH_SANDSTONE:
               return 60;
            case WALL_RIGHT_ARCH_SANDSTONE:
               return 60;
            case WALL_T_ARCH_SANDSTONE:
               return 60;
            case WALL_SOLID_RENDERED:
               return 60;
            case WALL_WINDOW_RENDERED:
               return 60;
            case WALL_NARROW_WINDOW_RENDERED:
               return 60;
            case WALL_DOOR_RENDERED:
               return 60;
            case WALL_DOUBLE_DOOR_RENDERED:
               return 60;
            case WALL_ARCHED_RENDERED:
               return 60;
            case WALL_PORTCULLIS_RENDERED:
               return 60;
            case WALL_BARRED_RENDERED:
               return 60;
            case WALL_ORIEL_RENDERED:
               return 60;
            case WALL_LEFT_ARCH_RENDERED:
               return 60;
            case WALL_RIGHT_ARCH_RENDERED:
               return 60;
            case WALL_T_ARCH_RENDERED:
               return 60;
            case WALL_SOLID_MARBLE:
               return 60;
            case WALL_WINDOW_MARBLE:
               return 60;
            case WALL_NARROW_WINDOW_MARBLE:
               return 60;
            case WALL_DOOR_MARBLE:
               return 60;
            case WALL_DOUBLE_DOOR_MARBLE:
               return 60;
            case WALL_ARCHED_MARBLE:
               return 60;
            case WALL_PORTCULLIS_MARBLE:
               return 60;
            case WALL_BARRED_MARBLE:
               return 60;
            case WALL_ORIEL_MARBLE:
               return 60;
            case WALL_LEFT_ARCH_MARBLE:
               return 60;
            case WALL_RIGHT_ARCH_MARBLE:
               return 60;
            case WALL_T_ARCH_MARBLE:
               return 60;
            case NO_WALL:
               return 60;
            case FENCE_MEDIUM_CHAIN:
               return 60;
            case FENCE_PLAN_MEDIUM_CHAIN:
               return 60;
            case FENCE_PLAN_PORTCULLIS:
               return 60;
            case FENCE_PORTCULLIS:
               return 60;
         }
      }
   }

   public static final float getCollisionWidth(StructureConstantsEnum type) {
      switch(type) {
         case FENCE_WOODEN:
         case FENCE_WOODEN_CRUDE_GATE:
         case FENCE_WOODEN_GATE:
         case FENCE_IRON:
         case FENCE_SLATE_IRON:
         case FENCE_ROUNDED_STONE_IRON:
         case FENCE_POTTERY_IRON:
         case FENCE_SANDSTONE_IRON:
         case FENCE_RENDERED_IRON:
         case FENCE_MARBLE_IRON:
         case FENCE_IRON_GATE:
         case FENCE_SLATE_IRON_GATE:
         case FENCE_ROUNDED_STONE_IRON_GATE:
         case FENCE_POTTERY_IRON_GATE:
         case FENCE_SANDSTONE_IRON_GATE:
         case FENCE_RENDERED_IRON_GATE:
         case FENCE_MARBLE_IRON_GATE:
         case FENCE_GARDESGARD_LOW:
         case FENCE_GARDESGARD_HIGH:
         case FENCE_GARDESGARD_GATE:
         case FENCE_SLATE_HIGH_IRON_FENCE:
         case FENCE_SLATE_HIGH_IRON_FENCE_GATE:
         case FENCE_ROUNDED_STONE_HIGH_IRON_FENCE:
         case FENCE_ROUNDED_STONE_HIGH_IRON_FENCE_GATE:
         case FENCE_SANDSTONE_HIGH_IRON_FENCE:
         case FENCE_SANDSTONE_HIGH_IRON_FENCE_GATE:
         case FENCE_RENDERED_HIGH_IRON_FENCE:
         case FENCE_RENDERED_HIGH_IRON_FENCE_GATE:
         case FENCE_POTTERY_HIGH_IRON_FENCE:
         case FENCE_POTTERY_HIGH_IRON_FENCE_GATE:
         case FENCE_MARBLE_HIGH_IRON_FENCE:
         case FENCE_MARBLE_HIGH_IRON_FENCE_GATE:
            return 0.055F;
         case FENCE_WOODEN_CRUDE:
         case FENCE_PLAN_WOODEN:
         case FENCE_PLAN_WOODEN_CRUDE:
         case FENCE_PLAN_WOODEN_GATE_CRUDE:
         case FENCE_PLAN_PALISADE:
         case FENCE_PLAN_STONEWALL:
         case FENCE_PLAN_PALISADE_GATE:
         case FENCE_PLAN_WOODEN_GATE:
         case FENCE_PLAN_STONEWALL_HIGH:
         case FENCE_PLAN_IRON:
         case FENCE_PLAN_SLATE_IRON:
         case FENCE_PLAN_ROUNDED_STONE_IRON:
         case FENCE_PLAN_POTTERY_IRON:
         case FENCE_PLAN_SANDSTONE_IRON:
         case FENCE_PLAN_RENDERED_IRON:
         case FENCE_PLAN_MARBLE_IRON:
         case FENCE_PLAN_IRON_HIGH:
         case FENCE_PLAN_IRON_GATE:
         case FENCE_PLAN_SLATE_IRON_GATE:
         case FENCE_PLAN_ROUNDED_STONE_IRON_GATE:
         case FENCE_PLAN_POTTERY_IRON_GATE:
         case FENCE_PLAN_SANDSTONE_IRON_GATE:
         case FENCE_PLAN_RENDERED_IRON_GATE:
         case FENCE_PLAN_MARBLE_IRON_GATE:
         case FENCE_PLAN_IRON_GATE_HIGH:
         case FENCE_PLAN_WOVEN:
         case FENCE_PLAN_STONE_PARAPET:
         case FENCE_PLAN_STONE_IRON_PARAPET:
         case FENCE_PLAN_WOODEN_PARAPET:
         case FENCE_PLAN_STONE:
         case FENCE_PLAN_SLATE:
         case FENCE_PLAN_ROUNDED_STONE:
         case FENCE_PLAN_POTTERY:
         case FENCE_PLAN_SANDSTONE:
         case FENCE_PLAN_RENDERED:
         case FENCE_PLAN_MARBLE:
         case FENCE_PLAN_ROPE_LOW:
         case FENCE_PLAN_GARDESGARD_LOW:
         case FENCE_PLAN_GARDESGARD_HIGH:
         case FENCE_PLAN_GARDESGARD_GATE:
         case FENCE_PLAN_CURB:
         case FENCE_PLAN_ROPE_HIGH:
         case FENCE_MAGIC_FIRE:
         case FENCE_RUBBLE:
         case FLOWERBED_BLUE:
         case FLOWERBED_GREENISH_YELLOW:
         case FLOWERBED_ORANGE_RED:
         case FLOWERBED_PURPLE:
         case FLOWERBED_WHITE:
         case FLOWERBED_WHITE_DOTTED:
         case FLOWERBED_YELLOW:
         case WALL_RUBBLE:
         case WALL_PLAIN_NARROW_WINDOW_PLAN:
         case WALL_DOUBLE_DOOR_WOODEN_PLAN:
         case WALL_DOUBLE_DOOR_STONE_PLAN:
         case WALL_DOOR_ARCHED_PLAN:
         case WALL_SOLID_WOODEN_PLAN:
         case WALL_WINDOW_WOODEN_PLAN:
         case WALL_DOOR_WOODEN_PLAN:
         case WALL_CANOPY_WOODEN_PLAN:
         case WALL_SOLID_STONE_PLAN:
         case WALL_ORIEL_STONE_DECORATED_PLAN:
         case WALL_WINDOW_STONE_PLAN:
         case WALL_DOOR_STONE_PLAN:
         case WALL_SOLID_TIMBER_FRAMED_PLAN:
         case WALL_BALCONY_TIMBER_FRAMED_PLAN:
         case WALL_JETTY_TIMBER_FRAMED_PLAN:
         case WALL_WINDOW_TIMBER_FRAMED_PLAN:
         case WALL_DOOR_TIMBER_FRAMED_PLAN:
         case WALL_DOUBLE_DOOR_TIMBER_FRAMED_PLAN:
         case WALL_DOOR_ARCHED_TIMBER_FRAMED_PLAN:
         case WALL_ARCHED_SLATE:
         case WALL_ARCHED_ROUNDED_STONE:
         case WALL_ARCHED_POTTERY:
         case WALL_ARCHED_SANDSTONE:
         case WALL_ARCHED_RENDERED:
         case WALL_ARCHED_MARBLE:
         case NO_WALL:
         case FENCE_PLAN_MEDIUM_CHAIN:
         case FENCE_PLAN_PORTCULLIS:
         default:
            return 0.0F;
         case FENCE_PALISADE:
         case FENCE_PALISADE_GATE:
         case FENCE_WOVEN:
            return 0.055F;
         case FENCE_STONEWALL:
         case FENCE_IRON_HIGH:
         case FENCE_IRON_GATE_HIGH:
         case FENCE_ROPE_LOW:
         case FENCE_CURB:
         case FENCE_MEDIUM_CHAIN:
         case FENCE_SLATE_TALL_STONE_WALL:
         case FENCE_SLATE_CHAIN_FENCE:
         case FENCE_ROUNDED_STONE_TALL_STONE_WALL:
         case FENCE_ROUNDED_STONE_CHAIN_FENCE:
         case FENCE_SANDSTONE_TALL_STONE_WALL:
         case FENCE_SANDSTONE_CHAIN_FENCE:
         case FENCE_RENDERED_TALL_STONE_WALL:
         case FENCE_RENDERED_CHAIN_FENCE:
         case FENCE_POTTERY_TALL_STONE_WALL:
         case FENCE_POTTERY_CHAIN_FENCE:
         case FENCE_MARBLE_TALL_STONE_WALL:
         case FENCE_MARBLE_CHAIN_FENCE:
            return 0.155F;
         case FENCE_STONEWALL_HIGH:
         case FENCE_STONE:
         case FENCE_SLATE:
         case FENCE_ROUNDED_STONE:
         case FENCE_POTTERY:
         case FENCE_SANDSTONE:
         case FENCE_RENDERED:
         case FENCE_MARBLE:
         case HEDGE_FLOWER1_LOW:
         case HEDGE_FLOWER1_MEDIUM:
         case HEDGE_FLOWER1_HIGH:
         case HEDGE_FLOWER2_LOW:
         case HEDGE_FLOWER2_MEDIUM:
         case HEDGE_FLOWER2_HIGH:
         case HEDGE_FLOWER3_LOW:
         case HEDGE_FLOWER3_MEDIUM:
         case HEDGE_FLOWER3_HIGH:
         case HEDGE_FLOWER4_LOW:
         case HEDGE_FLOWER4_MEDIUM:
         case HEDGE_FLOWER4_HIGH:
         case HEDGE_FLOWER5_LOW:
         case HEDGE_FLOWER5_MEDIUM:
         case HEDGE_FLOWER5_HIGH:
         case HEDGE_FLOWER6_LOW:
         case HEDGE_FLOWER6_MEDIUM:
         case HEDGE_FLOWER6_HIGH:
         case HEDGE_FLOWER7_LOW:
         case HEDGE_FLOWER7_MEDIUM:
         case HEDGE_FLOWER7_HIGH:
         case FENCE_MAGIC_STONE:
         case FENCE_MAGIC_ICE:
         case FENCE_SIEGEWALL:
            return 0.105F;
         case FENCE_STONE_PARAPET:
         case FENCE_STONE_IRON_PARAPET:
         case FENCE_SLATE_STONE_PARAPET:
         case FENCE_ROUNDED_STONE_STONE_PARAPET:
         case FENCE_SANDSTONE_STONE_PARAPET:
         case FENCE_RENDERED_STONE_PARAPET:
         case FENCE_POTTERY_STONE_PARAPET:
         case FENCE_MARBLE_STONE_PARAPET:
            return 0.04F;
         case FENCE_WOODEN_PARAPET:
            return 0.04F;
         case FENCE_ROPE_HIGH:
            return 0.005F;
         case WALL_SOLID_WOODEN:
         case WALL_CANOPY_WOODEN:
         case WALL_WINDOW_WOODEN:
         case WALL_WINDOW_WIDE_WOODEN:
         case WALL_DOOR_WOODEN:
         case WALL_DOUBLE_DOOR_WOODEN:
         case WALL_PORTCULLIS_WOOD:
         case WALL_SOLID_TIMBER_FRAMED:
         case WALL_WINDOW_TIMBER_FRAMED:
         case WALL_DOOR_TIMBER_FRAMED:
         case WALL_DOUBLE_DOOR_TIMBER_FRAMED:
         case WALL_BALCONY_TIMBER_FRAMED:
         case WALL_JETTY_TIMBER_FRAMED:
            return 0.045F;
         case WALL_SOLID_STONE_DECORATED:
         case WALL_ORIEL_STONE_DECORATED:
         case WALL_ORIEL_STONE_PLAIN:
         case WALL_SOLID_STONE:
         case WALL_BARRED_STONE:
         case WALL_WINDOW_STONE:
         case WALL_PLAIN_NARROW_WINDOW:
         case WALL_DOOR_STONE:
         case WALL_WINDOW_STONE_DECORATED:
         case WALL_DOOR_STONE_DECORATED:
         case WALL_DOUBLE_DOOR_STONE_DECORATED:
         case WALL_PORTCULLIS_STONE:
         case WALL_PORTCULLIS_STONE_DECORATED:
         case WALL_DOUBLE_DOOR_STONE:
         case WALL_SOLID_SLATE:
         case WALL_WINDOW_SLATE:
         case WALL_NARROW_WINDOW_SLATE:
         case WALL_DOOR_SLATE:
         case WALL_DOUBLE_DOOR_SLATE:
         case WALL_PORTCULLIS_SLATE:
         case WALL_BARRED_SLATE:
         case WALL_ORIEL_SLATE:
         case WALL_SOLID_ROUNDED_STONE:
         case WALL_WINDOW_ROUNDED_STONE:
         case WALL_NARROW_WINDOW_ROUNDED_STONE:
         case WALL_DOOR_ROUNDED_STONE:
         case WALL_DOUBLE_DOOR_ROUNDED_STONE:
         case WALL_PORTCULLIS_ROUNDED_STONE:
         case WALL_BARRED_ROUNDED_STONE:
         case WALL_ORIEL_ROUNDED_STONE:
         case WALL_SOLID_POTTERY:
         case WALL_WINDOW_POTTERY:
         case WALL_NARROW_WINDOW_POTTERY:
         case WALL_DOOR_POTTERY:
         case WALL_DOUBLE_DOOR_POTTERY:
         case WALL_PORTCULLIS_POTTERY:
         case WALL_BARRED_POTTERY:
         case WALL_ORIEL_POTTERY:
         case WALL_SOLID_SANDSTONE:
         case WALL_WINDOW_SANDSTONE:
         case WALL_NARROW_WINDOW_SANDSTONE:
         case WALL_DOOR_SANDSTONE:
         case WALL_DOUBLE_DOOR_SANDSTONE:
         case WALL_PORTCULLIS_SANDSTONE:
         case WALL_BARRED_SANDSTONE:
         case WALL_ORIEL_SANDSTONE:
         case WALL_SOLID_RENDERED:
         case WALL_WINDOW_RENDERED:
         case WALL_NARROW_WINDOW_RENDERED:
         case WALL_DOOR_RENDERED:
         case WALL_DOUBLE_DOOR_RENDERED:
         case WALL_PORTCULLIS_RENDERED:
         case WALL_BARRED_RENDERED:
         case WALL_ORIEL_RENDERED:
         case WALL_SOLID_MARBLE:
         case WALL_WINDOW_MARBLE:
         case WALL_NARROW_WINDOW_MARBLE:
         case WALL_DOOR_MARBLE:
         case WALL_DOUBLE_DOOR_MARBLE:
         case WALL_PORTCULLIS_MARBLE:
         case WALL_BARRED_MARBLE:
         case WALL_ORIEL_MARBLE:
         case FENCE_PORTCULLIS:
         case FENCE_SLATE_PORTCULLIS:
         case FENCE_ROUNDED_STONE_PORTCULLIS:
         case FENCE_SANDSTONE_PORTCULLIS:
         case FENCE_RENDERED_PORTCULLIS:
         case FENCE_POTTERY_PORTCULLIS:
         case FENCE_MARBLE_PORTCULLIS:
            return 0.06F;
         case WALL_DOOR_ARCHED_WOODEN:
         case WALL_LEFT_ARCH_WOODEN:
         case WALL_RIGHT_ARCH_WOODEN:
         case WALL_T_ARCH_WOODEN:
         case WALL_DOOR_ARCHED_WOODEN_PLAN:
         case WALL_DOOR_ARCHED_STONE_DECORATED:
         case WALL_LEFT_ARCH_STONE_DECORATED:
         case WALL_RIGHT_ARCH_STONE_DECORATED:
         case WALL_T_ARCH_STONE_DECORATED:
         case WALL_DOOR_ARCHED_STONE:
         case WALL_LEFT_ARCH_STONE:
         case WALL_RIGHT_ARCH_STONE:
         case WALL_T_ARCH_STONE:
         case WALL_DOOR_ARCHED_TIMBER_FRAMED:
         case WALL_LEFT_ARCH_TIMBER_FRAMED:
         case WALL_RIGHT_ARCH_TIMBER_FRAMED:
         case WALL_T_ARCH_TIMBER_FRAMED:
         case WALL_LEFT_ARCH_SLATE:
         case WALL_RIGHT_ARCH_SLATE:
         case WALL_T_ARCH_SLATE:
         case WALL_LEFT_ARCH_ROUNDED_STONE:
         case WALL_RIGHT_ARCH_ROUNDED_STONE:
         case WALL_T_ARCH_ROUNDED_STONE:
         case WALL_LEFT_ARCH_POTTERY:
         case WALL_RIGHT_ARCH_POTTERY:
         case WALL_T_ARCH_POTTERY:
         case WALL_LEFT_ARCH_SANDSTONE:
         case WALL_RIGHT_ARCH_SANDSTONE:
         case WALL_T_ARCH_SANDSTONE:
         case WALL_LEFT_ARCH_RENDERED:
         case WALL_RIGHT_ARCH_RENDERED:
         case WALL_T_ARCH_RENDERED:
         case WALL_LEFT_ARCH_MARBLE:
         case WALL_RIGHT_ARCH_MARBLE:
         case WALL_T_ARCH_MARBLE:
            return 0.055F;
      }
   }

   public static final float getCollisionThickness(StructureConstantsEnum type) {
      switch(type) {
         case FENCE_WOODEN:
         case FENCE_WOODEN_CRUDE:
         case FENCE_WOODEN_CRUDE_GATE:
         case FENCE_WOODEN_GATE:
         case FENCE_GARDESGARD_LOW:
         case FENCE_GARDESGARD_HIGH:
         case FENCE_MEDIUM_CHAIN:
         case FENCE_SLATE_CHAIN_FENCE:
         case FENCE_ROUNDED_STONE_CHAIN_FENCE:
         case FENCE_SANDSTONE_CHAIN_FENCE:
         case FENCE_RENDERED_CHAIN_FENCE:
         case FENCE_POTTERY_CHAIN_FENCE:
         case FENCE_MARBLE_CHAIN_FENCE:
            return 0.165F;
         case FENCE_PALISADE:
         case FENCE_PALISADE_GATE:
         case FENCE_WOVEN:
            return 0.55F;
         case FENCE_STONEWALL:
         case FENCE_SLATE:
         case FENCE_ROUNDED_STONE:
         case FENCE_POTTERY:
         case FENCE_SANDSTONE:
         case FENCE_RENDERED:
         case FENCE_MARBLE:
            return 0.45F;
         case FENCE_STONEWALL_HIGH:
         case FENCE_SLATE_TALL_STONE_WALL:
         case FENCE_ROUNDED_STONE_TALL_STONE_WALL:
         case FENCE_SANDSTONE_TALL_STONE_WALL:
         case FENCE_RENDERED_TALL_STONE_WALL:
         case FENCE_POTTERY_TALL_STONE_WALL:
         case FENCE_MARBLE_TALL_STONE_WALL:
            return 0.55F;
         case FENCE_IRON:
         case FENCE_SLATE_IRON:
         case FENCE_ROUNDED_STONE_IRON:
         case FENCE_POTTERY_IRON:
         case FENCE_SANDSTONE_IRON:
         case FENCE_RENDERED_IRON:
         case FENCE_MARBLE_IRON:
         case FENCE_IRON_HIGH:
         case FENCE_IRON_GATE:
         case FENCE_SLATE_IRON_GATE:
         case FENCE_ROUNDED_STONE_IRON_GATE:
         case FENCE_POTTERY_IRON_GATE:
         case FENCE_SANDSTONE_IRON_GATE:
         case FENCE_RENDERED_IRON_GATE:
         case FENCE_MARBLE_IRON_GATE:
         case FENCE_IRON_GATE_HIGH:
         case FENCE_SIEGEWALL:
         case FENCE_SLATE_HIGH_IRON_FENCE:
         case FENCE_SLATE_HIGH_IRON_FENCE_GATE:
         case FENCE_ROUNDED_STONE_HIGH_IRON_FENCE:
         case FENCE_ROUNDED_STONE_HIGH_IRON_FENCE_GATE:
         case FENCE_SANDSTONE_HIGH_IRON_FENCE:
         case FENCE_SANDSTONE_HIGH_IRON_FENCE_GATE:
         case FENCE_RENDERED_HIGH_IRON_FENCE:
         case FENCE_RENDERED_HIGH_IRON_FENCE_GATE:
         case FENCE_POTTERY_HIGH_IRON_FENCE:
         case FENCE_POTTERY_HIGH_IRON_FENCE_GATE:
         case FENCE_MARBLE_HIGH_IRON_FENCE:
         case FENCE_MARBLE_HIGH_IRON_FENCE_GATE:
            return 0.33F;
         case FENCE_PLAN_WOODEN:
         case FENCE_PLAN_WOODEN_CRUDE:
         case FENCE_PLAN_WOODEN_GATE_CRUDE:
         case FENCE_PLAN_PALISADE:
         case FENCE_PLAN_STONEWALL:
         case FENCE_PLAN_PALISADE_GATE:
         case FENCE_PLAN_WOODEN_GATE:
         case FENCE_PLAN_STONEWALL_HIGH:
         case FENCE_PLAN_IRON:
         case FENCE_PLAN_SLATE_IRON:
         case FENCE_PLAN_ROUNDED_STONE_IRON:
         case FENCE_PLAN_POTTERY_IRON:
         case FENCE_PLAN_SANDSTONE_IRON:
         case FENCE_PLAN_RENDERED_IRON:
         case FENCE_PLAN_MARBLE_IRON:
         case FENCE_PLAN_IRON_HIGH:
         case FENCE_PLAN_IRON_GATE:
         case FENCE_PLAN_SLATE_IRON_GATE:
         case FENCE_PLAN_ROUNDED_STONE_IRON_GATE:
         case FENCE_PLAN_POTTERY_IRON_GATE:
         case FENCE_PLAN_SANDSTONE_IRON_GATE:
         case FENCE_PLAN_RENDERED_IRON_GATE:
         case FENCE_PLAN_MARBLE_IRON_GATE:
         case FENCE_PLAN_IRON_GATE_HIGH:
         case FENCE_PLAN_WOVEN:
         case FENCE_PLAN_STONE_PARAPET:
         case FENCE_PLAN_STONE_IRON_PARAPET:
         case FENCE_PLAN_WOODEN_PARAPET:
         case FENCE_GARDESGARD_GATE:
         case FENCE_PLAN_STONE:
         case FENCE_PLAN_SLATE:
         case FENCE_PLAN_ROUNDED_STONE:
         case FENCE_PLAN_POTTERY:
         case FENCE_PLAN_SANDSTONE:
         case FENCE_PLAN_RENDERED:
         case FENCE_PLAN_MARBLE:
         case FENCE_PLAN_ROPE_LOW:
         case FENCE_PLAN_GARDESGARD_LOW:
         case FENCE_PLAN_GARDESGARD_HIGH:
         case FENCE_PLAN_GARDESGARD_GATE:
         case FENCE_PLAN_CURB:
         case FENCE_PLAN_ROPE_HIGH:
         case FENCE_MAGIC_FIRE:
         case FENCE_RUBBLE:
         case FLOWERBED_BLUE:
         case FLOWERBED_GREENISH_YELLOW:
         case FLOWERBED_ORANGE_RED:
         case FLOWERBED_PURPLE:
         case FLOWERBED_WHITE:
         case FLOWERBED_WHITE_DOTTED:
         case FLOWERBED_YELLOW:
         case WALL_RUBBLE:
         case WALL_WINDOW_WIDE_WOODEN:
         case WALL_PLAIN_NARROW_WINDOW_PLAN:
         case WALL_DOUBLE_DOOR_WOODEN_PLAN:
         case WALL_DOUBLE_DOOR_STONE_PLAN:
         case WALL_SOLID_WOODEN_PLAN:
         case WALL_WINDOW_WOODEN_PLAN:
         case WALL_DOOR_WOODEN_PLAN:
         case WALL_CANOPY_WOODEN_PLAN:
         case WALL_SOLID_STONE_PLAN:
         case WALL_ORIEL_STONE_DECORATED_PLAN:
         case WALL_WINDOW_STONE_PLAN:
         case WALL_DOOR_STONE_PLAN:
         case WALL_SOLID_TIMBER_FRAMED_PLAN:
         case WALL_BALCONY_TIMBER_FRAMED_PLAN:
         case WALL_JETTY_TIMBER_FRAMED_PLAN:
         case WALL_WINDOW_TIMBER_FRAMED_PLAN:
         case WALL_DOOR_TIMBER_FRAMED_PLAN:
         case WALL_DOUBLE_DOOR_TIMBER_FRAMED_PLAN:
         case WALL_DOOR_ARCHED_TIMBER_FRAMED_PLAN:
         case WALL_ARCHED_SLATE:
         case WALL_ORIEL_SLATE:
         case WALL_ARCHED_ROUNDED_STONE:
         case WALL_ORIEL_ROUNDED_STONE:
         case WALL_ARCHED_POTTERY:
         case WALL_ORIEL_POTTERY:
         case WALL_ARCHED_SANDSTONE:
         case WALL_ORIEL_SANDSTONE:
         case WALL_ARCHED_RENDERED:
         case WALL_ORIEL_RENDERED:
         case WALL_ARCHED_MARBLE:
         case WALL_ORIEL_MARBLE:
         case NO_WALL:
         case FENCE_PLAN_MEDIUM_CHAIN:
         case FENCE_PLAN_PORTCULLIS:
         default:
            return 0.0F;
         case FENCE_STONE_PARAPET:
         case FENCE_STONE_IRON_PARAPET:
         case FENCE_SLATE_STONE_PARAPET:
         case FENCE_ROUNDED_STONE_STONE_PARAPET:
         case FENCE_SANDSTONE_STONE_PARAPET:
         case FENCE_RENDERED_STONE_PARAPET:
         case FENCE_POTTERY_STONE_PARAPET:
         case FENCE_MARBLE_STONE_PARAPET:
            return 0.33F;
         case FENCE_WOODEN_PARAPET:
         case FENCE_MAGIC_STONE:
         case FENCE_MAGIC_ICE:
            return 0.33F;
         case FENCE_ROPE_LOW:
         case FENCE_STONE:
            return 0.385F;
         case FENCE_CURB:
         case HEDGE_FLOWER1_LOW:
         case HEDGE_FLOWER1_MEDIUM:
         case HEDGE_FLOWER1_HIGH:
         case HEDGE_FLOWER2_LOW:
         case HEDGE_FLOWER2_MEDIUM:
         case HEDGE_FLOWER2_HIGH:
         case HEDGE_FLOWER3_LOW:
         case HEDGE_FLOWER3_MEDIUM:
         case HEDGE_FLOWER3_HIGH:
         case HEDGE_FLOWER4_LOW:
         case HEDGE_FLOWER4_MEDIUM:
         case HEDGE_FLOWER4_HIGH:
         case HEDGE_FLOWER5_LOW:
         case HEDGE_FLOWER5_MEDIUM:
         case HEDGE_FLOWER5_HIGH:
         case HEDGE_FLOWER6_LOW:
         case HEDGE_FLOWER6_MEDIUM:
         case HEDGE_FLOWER6_HIGH:
         case HEDGE_FLOWER7_LOW:
         case HEDGE_FLOWER7_MEDIUM:
         case HEDGE_FLOWER7_HIGH:
            return 0.8F;
         case FENCE_ROPE_HIGH:
            return 0.11F;
         case WALL_SOLID_WOODEN:
         case WALL_CANOPY_WOODEN:
         case WALL_WINDOW_WOODEN:
         case WALL_DOOR_WOODEN:
         case WALL_DOUBLE_DOOR_WOODEN:
         case WALL_PORTCULLIS_WOOD:
         case WALL_SOLID_TIMBER_FRAMED:
         case WALL_WINDOW_TIMBER_FRAMED:
         case WALL_DOOR_TIMBER_FRAMED:
         case WALL_DOUBLE_DOOR_TIMBER_FRAMED:
         case WALL_BALCONY_TIMBER_FRAMED:
         case WALL_JETTY_TIMBER_FRAMED:
            return 0.275F;
         case WALL_SOLID_STONE_DECORATED:
         case WALL_ORIEL_STONE_DECORATED:
         case WALL_ORIEL_STONE_PLAIN:
         case WALL_SOLID_STONE:
         case WALL_BARRED_STONE:
         case WALL_WINDOW_STONE:
         case WALL_PLAIN_NARROW_WINDOW:
         case WALL_DOOR_STONE:
         case WALL_WINDOW_STONE_DECORATED:
         case WALL_DOOR_STONE_DECORATED:
         case WALL_DOUBLE_DOOR_STONE_DECORATED:
         case WALL_PORTCULLIS_STONE:
         case WALL_PORTCULLIS_STONE_DECORATED:
         case WALL_DOUBLE_DOOR_STONE:
         case WALL_SOLID_SLATE:
         case WALL_WINDOW_SLATE:
         case WALL_NARROW_WINDOW_SLATE:
         case WALL_DOOR_SLATE:
         case WALL_DOUBLE_DOOR_SLATE:
         case WALL_PORTCULLIS_SLATE:
         case WALL_BARRED_SLATE:
         case WALL_SOLID_ROUNDED_STONE:
         case WALL_WINDOW_ROUNDED_STONE:
         case WALL_NARROW_WINDOW_ROUNDED_STONE:
         case WALL_DOOR_ROUNDED_STONE:
         case WALL_DOUBLE_DOOR_ROUNDED_STONE:
         case WALL_PORTCULLIS_ROUNDED_STONE:
         case WALL_BARRED_ROUNDED_STONE:
         case WALL_SOLID_POTTERY:
         case WALL_WINDOW_POTTERY:
         case WALL_NARROW_WINDOW_POTTERY:
         case WALL_DOOR_POTTERY:
         case WALL_DOUBLE_DOOR_POTTERY:
         case WALL_PORTCULLIS_POTTERY:
         case WALL_BARRED_POTTERY:
         case WALL_SOLID_SANDSTONE:
         case WALL_WINDOW_SANDSTONE:
         case WALL_NARROW_WINDOW_SANDSTONE:
         case WALL_DOOR_SANDSTONE:
         case WALL_DOUBLE_DOOR_SANDSTONE:
         case WALL_PORTCULLIS_SANDSTONE:
         case WALL_BARRED_SANDSTONE:
         case WALL_SOLID_RENDERED:
         case WALL_WINDOW_RENDERED:
         case WALL_NARROW_WINDOW_RENDERED:
         case WALL_DOOR_RENDERED:
         case WALL_DOUBLE_DOOR_RENDERED:
         case WALL_PORTCULLIS_RENDERED:
         case WALL_BARRED_RENDERED:
         case WALL_SOLID_MARBLE:
         case WALL_WINDOW_MARBLE:
         case WALL_NARROW_WINDOW_MARBLE:
         case WALL_DOOR_MARBLE:
         case WALL_DOUBLE_DOOR_MARBLE:
         case WALL_PORTCULLIS_MARBLE:
         case WALL_BARRED_MARBLE:
         case FENCE_PORTCULLIS:
         case FENCE_SLATE_PORTCULLIS:
         case FENCE_ROUNDED_STONE_PORTCULLIS:
         case FENCE_SANDSTONE_PORTCULLIS:
         case FENCE_RENDERED_PORTCULLIS:
         case FENCE_POTTERY_PORTCULLIS:
         case FENCE_MARBLE_PORTCULLIS:
            return 0.66F;
         case WALL_DOOR_ARCHED_WOODEN:
         case WALL_LEFT_ARCH_WOODEN:
         case WALL_RIGHT_ARCH_WOODEN:
         case WALL_T_ARCH_WOODEN:
         case WALL_DOOR_ARCHED_WOODEN_PLAN:
         case WALL_DOOR_ARCHED_PLAN:
         case WALL_DOOR_ARCHED_STONE_DECORATED:
         case WALL_LEFT_ARCH_STONE_DECORATED:
         case WALL_RIGHT_ARCH_STONE_DECORATED:
         case WALL_T_ARCH_STONE_DECORATED:
         case WALL_DOOR_ARCHED_STONE:
         case WALL_LEFT_ARCH_STONE:
         case WALL_RIGHT_ARCH_STONE:
         case WALL_T_ARCH_STONE:
         case WALL_DOOR_ARCHED_TIMBER_FRAMED:
         case WALL_LEFT_ARCH_TIMBER_FRAMED:
         case WALL_RIGHT_ARCH_TIMBER_FRAMED:
         case WALL_T_ARCH_TIMBER_FRAMED:
         case WALL_LEFT_ARCH_SLATE:
         case WALL_RIGHT_ARCH_SLATE:
         case WALL_T_ARCH_SLATE:
         case WALL_LEFT_ARCH_ROUNDED_STONE:
         case WALL_RIGHT_ARCH_ROUNDED_STONE:
         case WALL_T_ARCH_ROUNDED_STONE:
         case WALL_LEFT_ARCH_POTTERY:
         case WALL_RIGHT_ARCH_POTTERY:
         case WALL_T_ARCH_POTTERY:
         case WALL_LEFT_ARCH_SANDSTONE:
         case WALL_RIGHT_ARCH_SANDSTONE:
         case WALL_T_ARCH_SANDSTONE:
         case WALL_LEFT_ARCH_RENDERED:
         case WALL_RIGHT_ARCH_RENDERED:
         case WALL_T_ARCH_RENDERED:
         case WALL_LEFT_ARCH_MARBLE:
         case WALL_RIGHT_ARCH_MARBLE:
         case WALL_T_ARCH_MARBLE:
            return 0.44F;
      }
   }

   public static final float getOpening(StructureConstantsEnum type) {
      switch(type) {
         case FENCE_WOODEN_CRUDE_GATE:
         case FENCE_WOODEN_GATE:
         case FENCE_PALISADE_GATE:
            return 1.0F;
         case FENCE_PALISADE:
         case FENCE_STONEWALL:
         case FENCE_STONEWALL_HIGH:
         case FENCE_IRON:
         case FENCE_SLATE_IRON:
         case FENCE_ROUNDED_STONE_IRON:
         case FENCE_POTTERY_IRON:
         case FENCE_SANDSTONE_IRON:
         case FENCE_RENDERED_IRON:
         case FENCE_MARBLE_IRON:
         case FENCE_IRON_HIGH:
         case FENCE_WOVEN:
         case FENCE_PLAN_WOODEN:
         case FENCE_PLAN_WOODEN_CRUDE:
         case FENCE_PLAN_WOODEN_GATE_CRUDE:
         case FENCE_PLAN_PALISADE:
         case FENCE_PLAN_STONEWALL:
         case FENCE_PLAN_PALISADE_GATE:
         case FENCE_PLAN_WOODEN_GATE:
         case FENCE_PLAN_STONEWALL_HIGH:
         case FENCE_PLAN_IRON:
         case FENCE_PLAN_SLATE_IRON:
         case FENCE_PLAN_ROUNDED_STONE_IRON:
         case FENCE_PLAN_POTTERY_IRON:
         case FENCE_PLAN_SANDSTONE_IRON:
         case FENCE_PLAN_RENDERED_IRON:
         case FENCE_PLAN_MARBLE_IRON:
         case FENCE_PLAN_IRON_HIGH:
         case FENCE_PLAN_IRON_GATE:
         case FENCE_PLAN_SLATE_IRON_GATE:
         case FENCE_PLAN_ROUNDED_STONE_IRON_GATE:
         case FENCE_PLAN_POTTERY_IRON_GATE:
         case FENCE_PLAN_SANDSTONE_IRON_GATE:
         case FENCE_PLAN_RENDERED_IRON_GATE:
         case FENCE_PLAN_MARBLE_IRON_GATE:
         case FENCE_PLAN_IRON_GATE_HIGH:
         case FENCE_PLAN_WOVEN:
         case FENCE_STONE_PARAPET:
         case FENCE_PLAN_STONE_PARAPET:
         case FENCE_STONE_IRON_PARAPET:
         case FENCE_PLAN_STONE_IRON_PARAPET:
         case FENCE_WOODEN_PARAPET:
         case FENCE_PLAN_WOODEN_PARAPET:
         case FENCE_ROPE_LOW:
         case FENCE_GARDESGARD_LOW:
         case FENCE_GARDESGARD_HIGH:
         case FENCE_CURB:
         case FENCE_ROPE_HIGH:
         case FENCE_STONE:
         case FENCE_SLATE:
         case FENCE_ROUNDED_STONE:
         case FENCE_POTTERY:
         case FENCE_SANDSTONE:
         case FENCE_RENDERED:
         case FENCE_MARBLE:
         case FENCE_PLAN_STONE:
         case FENCE_PLAN_SLATE:
         case FENCE_PLAN_ROUNDED_STONE:
         case FENCE_PLAN_POTTERY:
         case FENCE_PLAN_SANDSTONE:
         case FENCE_PLAN_RENDERED:
         case FENCE_PLAN_MARBLE:
         case FENCE_PLAN_ROPE_LOW:
         case FENCE_PLAN_GARDESGARD_LOW:
         case FENCE_PLAN_GARDESGARD_HIGH:
         case FENCE_PLAN_GARDESGARD_GATE:
         case FENCE_PLAN_CURB:
         case FENCE_PLAN_ROPE_HIGH:
         case HEDGE_FLOWER1_LOW:
         case HEDGE_FLOWER1_MEDIUM:
         case HEDGE_FLOWER1_HIGH:
         case HEDGE_FLOWER2_LOW:
         case HEDGE_FLOWER2_MEDIUM:
         case HEDGE_FLOWER2_HIGH:
         case HEDGE_FLOWER3_LOW:
         case HEDGE_FLOWER3_MEDIUM:
         case HEDGE_FLOWER3_HIGH:
         case HEDGE_FLOWER4_LOW:
         case HEDGE_FLOWER4_MEDIUM:
         case HEDGE_FLOWER4_HIGH:
         case HEDGE_FLOWER5_LOW:
         case HEDGE_FLOWER5_MEDIUM:
         case HEDGE_FLOWER5_HIGH:
         case HEDGE_FLOWER6_LOW:
         case HEDGE_FLOWER6_MEDIUM:
         case HEDGE_FLOWER6_HIGH:
         case HEDGE_FLOWER7_LOW:
         case HEDGE_FLOWER7_MEDIUM:
         case HEDGE_FLOWER7_HIGH:
         case FENCE_MAGIC_STONE:
         case FENCE_MAGIC_FIRE:
         case FENCE_MAGIC_ICE:
         case FENCE_RUBBLE:
         case FENCE_SIEGEWALL:
         case FLOWERBED_BLUE:
         case FLOWERBED_GREENISH_YELLOW:
         case FLOWERBED_ORANGE_RED:
         case FLOWERBED_PURPLE:
         case FLOWERBED_WHITE:
         case FLOWERBED_WHITE_DOTTED:
         case FLOWERBED_YELLOW:
         case WALL_RUBBLE:
         case WALL_SOLID_WOODEN:
         case WALL_WINDOW_WOODEN:
         case WALL_WINDOW_WIDE_WOODEN:
         case WALL_SOLID_STONE_DECORATED:
         case WALL_ORIEL_STONE_DECORATED:
         case WALL_ORIEL_STONE_PLAIN:
         case WALL_SOLID_STONE:
         case WALL_BARRED_STONE:
         case WALL_WINDOW_STONE:
         case WALL_PLAIN_NARROW_WINDOW:
         case WALL_WINDOW_STONE_DECORATED:
         case WALL_PLAIN_NARROW_WINDOW_PLAN:
         case WALL_DOUBLE_DOOR_WOODEN_PLAN:
         case WALL_DOOR_ARCHED_WOODEN_PLAN:
         case WALL_DOUBLE_DOOR_STONE_PLAN:
         case WALL_DOOR_ARCHED_PLAN:
         case WALL_SOLID_WOODEN_PLAN:
         case WALL_WINDOW_WOODEN_PLAN:
         case WALL_DOOR_WOODEN_PLAN:
         case WALL_CANOPY_WOODEN_PLAN:
         case WALL_SOLID_STONE_PLAN:
         case WALL_ORIEL_STONE_DECORATED_PLAN:
         case WALL_WINDOW_STONE_PLAN:
         case WALL_DOOR_STONE_PLAN:
         case WALL_SOLID_TIMBER_FRAMED:
         case WALL_WINDOW_TIMBER_FRAMED:
         case WALL_BALCONY_TIMBER_FRAMED:
         case WALL_JETTY_TIMBER_FRAMED:
         case WALL_SOLID_TIMBER_FRAMED_PLAN:
         case WALL_BALCONY_TIMBER_FRAMED_PLAN:
         case WALL_JETTY_TIMBER_FRAMED_PLAN:
         case WALL_WINDOW_TIMBER_FRAMED_PLAN:
         case WALL_DOOR_TIMBER_FRAMED_PLAN:
         case WALL_DOUBLE_DOOR_TIMBER_FRAMED_PLAN:
         case WALL_DOOR_ARCHED_TIMBER_FRAMED_PLAN:
         case WALL_SOLID_SLATE:
         case WALL_WINDOW_SLATE:
         case WALL_NARROW_WINDOW_SLATE:
         case WALL_BARRED_SLATE:
         case WALL_ORIEL_SLATE:
         case WALL_SOLID_ROUNDED_STONE:
         case WALL_WINDOW_ROUNDED_STONE:
         case WALL_NARROW_WINDOW_ROUNDED_STONE:
         case WALL_BARRED_ROUNDED_STONE:
         case WALL_ORIEL_ROUNDED_STONE:
         case WALL_SOLID_POTTERY:
         case WALL_WINDOW_POTTERY:
         case WALL_NARROW_WINDOW_POTTERY:
         case WALL_BARRED_POTTERY:
         case WALL_ORIEL_POTTERY:
         case WALL_SOLID_SANDSTONE:
         case WALL_WINDOW_SANDSTONE:
         case WALL_NARROW_WINDOW_SANDSTONE:
         case WALL_BARRED_SANDSTONE:
         case WALL_ORIEL_SANDSTONE:
         case WALL_SOLID_RENDERED:
         case WALL_WINDOW_RENDERED:
         case WALL_NARROW_WINDOW_RENDERED:
         case WALL_BARRED_RENDERED:
         case WALL_ORIEL_RENDERED:
         case WALL_SOLID_MARBLE:
         case WALL_WINDOW_MARBLE:
         case WALL_NARROW_WINDOW_MARBLE:
         case WALL_BARRED_MARBLE:
         case WALL_ORIEL_MARBLE:
         case NO_WALL:
         case FENCE_MEDIUM_CHAIN:
         case FENCE_PLAN_MEDIUM_CHAIN:
         case FENCE_PLAN_PORTCULLIS:
         case FENCE_SLATE_TALL_STONE_WALL:
         case FENCE_SLATE_PORTCULLIS:
         case FENCE_SLATE_HIGH_IRON_FENCE:
         case FENCE_SLATE_STONE_PARAPET:
         case FENCE_SLATE_CHAIN_FENCE:
         case FENCE_ROUNDED_STONE_TALL_STONE_WALL:
         case FENCE_ROUNDED_STONE_HIGH_IRON_FENCE:
         case FENCE_ROUNDED_STONE_STONE_PARAPET:
         case FENCE_ROUNDED_STONE_CHAIN_FENCE:
         case FENCE_SANDSTONE_TALL_STONE_WALL:
         case FENCE_SANDSTONE_HIGH_IRON_FENCE:
         case FENCE_SANDSTONE_STONE_PARAPET:
         case FENCE_SANDSTONE_CHAIN_FENCE:
         case FENCE_RENDERED_TALL_STONE_WALL:
         case FENCE_RENDERED_HIGH_IRON_FENCE:
         case FENCE_RENDERED_STONE_PARAPET:
         case FENCE_RENDERED_CHAIN_FENCE:
         case FENCE_POTTERY_TALL_STONE_WALL:
         case FENCE_POTTERY_HIGH_IRON_FENCE:
         case FENCE_POTTERY_STONE_PARAPET:
         case FENCE_POTTERY_CHAIN_FENCE:
         case FENCE_MARBLE_TALL_STONE_WALL:
         case FENCE_MARBLE_HIGH_IRON_FENCE:
         default:
            return 0.0F;
         case FENCE_IRON_GATE:
         case FENCE_SLATE_IRON_GATE:
         case FENCE_ROUNDED_STONE_IRON_GATE:
         case FENCE_POTTERY_IRON_GATE:
         case FENCE_SANDSTONE_IRON_GATE:
         case FENCE_RENDERED_IRON_GATE:
         case FENCE_MARBLE_IRON_GATE:
         case FENCE_IRON_GATE_HIGH:
         case WALL_DOUBLE_DOOR_WOODEN:
         case WALL_DOUBLE_DOOR_STONE_DECORATED:
         case WALL_PORTCULLIS_STONE:
         case WALL_PORTCULLIS_WOOD:
         case WALL_PORTCULLIS_STONE_DECORATED:
         case WALL_DOUBLE_DOOR_STONE:
         case WALL_DOUBLE_DOOR_TIMBER_FRAMED:
         case WALL_DOUBLE_DOOR_SLATE:
         case WALL_PORTCULLIS_SLATE:
         case WALL_DOUBLE_DOOR_ROUNDED_STONE:
         case WALL_PORTCULLIS_ROUNDED_STONE:
         case WALL_DOUBLE_DOOR_POTTERY:
         case WALL_PORTCULLIS_POTTERY:
         case WALL_DOUBLE_DOOR_SANDSTONE:
         case WALL_PORTCULLIS_SANDSTONE:
         case WALL_DOUBLE_DOOR_RENDERED:
         case WALL_PORTCULLIS_RENDERED:
         case WALL_DOUBLE_DOOR_MARBLE:
         case WALL_PORTCULLIS_MARBLE:
         case FENCE_PORTCULLIS:
         case FENCE_SLATE_HIGH_IRON_FENCE_GATE:
         case FENCE_ROUNDED_STONE_PORTCULLIS:
         case FENCE_ROUNDED_STONE_HIGH_IRON_FENCE_GATE:
         case FENCE_SANDSTONE_PORTCULLIS:
         case FENCE_SANDSTONE_HIGH_IRON_FENCE_GATE:
         case FENCE_RENDERED_PORTCULLIS:
         case FENCE_RENDERED_HIGH_IRON_FENCE_GATE:
         case FENCE_POTTERY_PORTCULLIS:
         case FENCE_POTTERY_HIGH_IRON_FENCE_GATE:
         case FENCE_MARBLE_PORTCULLIS:
         case FENCE_MARBLE_HIGH_IRON_FENCE_GATE:
            return 0.66F;
         case FENCE_GARDESGARD_GATE:
            return 0.4F;
         case WALL_CANOPY_WOODEN:
         case WALL_DOOR_WOODEN:
         case WALL_DOOR_STONE:
         case WALL_DOOR_STONE_DECORATED:
         case WALL_DOOR_TIMBER_FRAMED:
         case WALL_DOOR_SLATE:
         case WALL_DOOR_ROUNDED_STONE:
         case WALL_DOOR_POTTERY:
         case WALL_DOOR_SANDSTONE:
         case WALL_DOOR_RENDERED:
         case WALL_DOOR_MARBLE:
            return 0.33F;
         case WALL_DOOR_ARCHED_WOODEN:
         case WALL_LEFT_ARCH_WOODEN:
         case WALL_RIGHT_ARCH_WOODEN:
         case WALL_T_ARCH_WOODEN:
         case WALL_DOOR_ARCHED_TIMBER_FRAMED:
         case WALL_LEFT_ARCH_TIMBER_FRAMED:
         case WALL_RIGHT_ARCH_TIMBER_FRAMED:
         case WALL_T_ARCH_TIMBER_FRAMED:
            return 0.95F;
         case WALL_DOOR_ARCHED_STONE_DECORATED:
         case WALL_LEFT_ARCH_STONE_DECORATED:
         case WALL_RIGHT_ARCH_STONE_DECORATED:
         case WALL_T_ARCH_STONE_DECORATED:
         case WALL_DOOR_ARCHED_STONE:
         case WALL_LEFT_ARCH_STONE:
         case WALL_RIGHT_ARCH_STONE:
         case WALL_T_ARCH_STONE:
         case WALL_ARCHED_SLATE:
         case WALL_LEFT_ARCH_SLATE:
         case WALL_RIGHT_ARCH_SLATE:
         case WALL_T_ARCH_SLATE:
         case WALL_ARCHED_ROUNDED_STONE:
         case WALL_LEFT_ARCH_ROUNDED_STONE:
         case WALL_RIGHT_ARCH_ROUNDED_STONE:
         case WALL_T_ARCH_ROUNDED_STONE:
         case WALL_ARCHED_POTTERY:
         case WALL_LEFT_ARCH_POTTERY:
         case WALL_RIGHT_ARCH_POTTERY:
         case WALL_T_ARCH_POTTERY:
         case WALL_ARCHED_SANDSTONE:
         case WALL_LEFT_ARCH_SANDSTONE:
         case WALL_RIGHT_ARCH_SANDSTONE:
         case WALL_T_ARCH_SANDSTONE:
         case WALL_ARCHED_RENDERED:
         case WALL_LEFT_ARCH_RENDERED:
         case WALL_RIGHT_ARCH_RENDERED:
         case WALL_T_ARCH_RENDERED:
         case WALL_ARCHED_MARBLE:
         case WALL_LEFT_ARCH_MARBLE:
         case WALL_RIGHT_ARCH_MARBLE:
         case WALL_T_ARCH_MARBLE:
            return 0.9F;
      }
   }

   public static final boolean isGate(StructureConstantsEnum type) {
      switch(type) {
         case FENCE_WOODEN_CRUDE_GATE:
         case FENCE_WOODEN_GATE:
         case FENCE_PALISADE_GATE:
         case FENCE_IRON_GATE:
         case FENCE_SLATE_IRON_GATE:
         case FENCE_ROUNDED_STONE_IRON_GATE:
         case FENCE_POTTERY_IRON_GATE:
         case FENCE_SANDSTONE_IRON_GATE:
         case FENCE_RENDERED_IRON_GATE:
         case FENCE_MARBLE_IRON_GATE:
         case FENCE_IRON_GATE_HIGH:
         case FENCE_GARDESGARD_GATE:
         case WALL_CANOPY_WOODEN:
         case WALL_DOOR_WOODEN:
         case WALL_DOUBLE_DOOR_WOODEN:
         case WALL_DOOR_STONE:
         case WALL_DOOR_STONE_DECORATED:
         case WALL_DOUBLE_DOOR_STONE_DECORATED:
         case WALL_PORTCULLIS_STONE:
         case WALL_PORTCULLIS_WOOD:
         case WALL_PORTCULLIS_STONE_DECORATED:
         case WALL_DOUBLE_DOOR_STONE:
         case WALL_DOOR_TIMBER_FRAMED:
         case WALL_DOUBLE_DOOR_TIMBER_FRAMED:
         case WALL_DOOR_SLATE:
         case WALL_DOUBLE_DOOR_SLATE:
         case WALL_PORTCULLIS_SLATE:
         case WALL_DOOR_ROUNDED_STONE:
         case WALL_DOUBLE_DOOR_ROUNDED_STONE:
         case WALL_PORTCULLIS_ROUNDED_STONE:
         case WALL_DOOR_POTTERY:
         case WALL_DOUBLE_DOOR_POTTERY:
         case WALL_PORTCULLIS_POTTERY:
         case WALL_DOOR_SANDSTONE:
         case WALL_DOUBLE_DOOR_SANDSTONE:
         case WALL_PORTCULLIS_SANDSTONE:
         case WALL_DOOR_RENDERED:
         case WALL_DOUBLE_DOOR_RENDERED:
         case WALL_PORTCULLIS_RENDERED:
         case WALL_DOOR_MARBLE:
         case WALL_DOUBLE_DOOR_MARBLE:
         case WALL_PORTCULLIS_MARBLE:
         case FENCE_PORTCULLIS:
         case FENCE_SLATE_PORTCULLIS:
         case FENCE_SLATE_HIGH_IRON_FENCE_GATE:
         case FENCE_ROUNDED_STONE_PORTCULLIS:
         case FENCE_ROUNDED_STONE_HIGH_IRON_FENCE_GATE:
         case FENCE_SANDSTONE_PORTCULLIS:
         case FENCE_SANDSTONE_HIGH_IRON_FENCE_GATE:
         case FENCE_RENDERED_PORTCULLIS:
         case FENCE_RENDERED_HIGH_IRON_FENCE_GATE:
         case FENCE_POTTERY_PORTCULLIS:
         case FENCE_POTTERY_HIGH_IRON_FENCE_GATE:
         case FENCE_MARBLE_PORTCULLIS:
         case FENCE_MARBLE_HIGH_IRON_FENCE_GATE:
            return true;
         default:
            return false;
      }
   }

   public static final float getCollisionHeight(StructureConstantsEnum type) {
      switch(type) {
         case FENCE_WOODEN:
         case FENCE_WOODEN_GATE:
            return 1.1F;
         case FENCE_WOODEN_CRUDE:
         case FENCE_WOODEN_CRUDE_GATE:
            return 1.1F;
         case FENCE_PALISADE:
            return 4.9F;
         case FENCE_STONEWALL:
            return 1.0F;
         case FENCE_PALISADE_GATE:
            return 5.0F;
         case FENCE_STONEWALL_HIGH:
         case FENCE_SLATE_TALL_STONE_WALL:
         case FENCE_ROUNDED_STONE_TALL_STONE_WALL:
         case FENCE_SANDSTONE_TALL_STONE_WALL:
         case FENCE_RENDERED_TALL_STONE_WALL:
         case FENCE_POTTERY_TALL_STONE_WALL:
         case FENCE_MARBLE_TALL_STONE_WALL:
            return 3.5F;
         case FENCE_IRON:
         case FENCE_SLATE_IRON:
         case FENCE_ROUNDED_STONE_IRON:
         case FENCE_POTTERY_IRON:
         case FENCE_SANDSTONE_IRON:
         case FENCE_RENDERED_IRON:
         case FENCE_MARBLE_IRON:
         case FENCE_IRON_GATE:
         case FENCE_SLATE_IRON_GATE:
         case FENCE_ROUNDED_STONE_IRON_GATE:
         case FENCE_POTTERY_IRON_GATE:
         case FENCE_SANDSTONE_IRON_GATE:
         case FENCE_RENDERED_IRON_GATE:
         case FENCE_MARBLE_IRON_GATE:
            return 1.5F;
         case FENCE_IRON_HIGH:
         case FENCE_IRON_GATE_HIGH:
         case FENCE_SLATE_HIGH_IRON_FENCE:
         case FENCE_SLATE_HIGH_IRON_FENCE_GATE:
         case FENCE_ROUNDED_STONE_HIGH_IRON_FENCE:
         case FENCE_ROUNDED_STONE_HIGH_IRON_FENCE_GATE:
         case FENCE_SANDSTONE_HIGH_IRON_FENCE:
         case FENCE_SANDSTONE_HIGH_IRON_FENCE_GATE:
         case FENCE_RENDERED_HIGH_IRON_FENCE:
         case FENCE_RENDERED_HIGH_IRON_FENCE_GATE:
         case FENCE_POTTERY_HIGH_IRON_FENCE:
         case FENCE_POTTERY_HIGH_IRON_FENCE_GATE:
         case FENCE_MARBLE_HIGH_IRON_FENCE:
         case FENCE_MARBLE_HIGH_IRON_FENCE_GATE:
            return 3.0F;
         case FENCE_WOVEN:
         case FENCE_ROPE_LOW:
         case FENCE_CURB:
         case FENCE_MEDIUM_CHAIN:
         case FENCE_SLATE_CHAIN_FENCE:
         case FENCE_ROUNDED_STONE_CHAIN_FENCE:
         case FENCE_SANDSTONE_CHAIN_FENCE:
         case FENCE_RENDERED_CHAIN_FENCE:
         case FENCE_POTTERY_CHAIN_FENCE:
         case FENCE_MARBLE_CHAIN_FENCE:
            return 2.0F;
         case FENCE_PLAN_WOODEN:
         case FENCE_PLAN_WOODEN_CRUDE:
         case FENCE_PLAN_WOODEN_GATE_CRUDE:
         case FENCE_PLAN_PALISADE:
         case FENCE_PLAN_STONEWALL:
         case FENCE_PLAN_PALISADE_GATE:
         case FENCE_PLAN_WOODEN_GATE:
         case FENCE_PLAN_STONEWALL_HIGH:
         case FENCE_PLAN_IRON:
         case FENCE_PLAN_SLATE_IRON:
         case FENCE_PLAN_ROUNDED_STONE_IRON:
         case FENCE_PLAN_POTTERY_IRON:
         case FENCE_PLAN_SANDSTONE_IRON:
         case FENCE_PLAN_RENDERED_IRON:
         case FENCE_PLAN_MARBLE_IRON:
         case FENCE_PLAN_IRON_HIGH:
         case FENCE_PLAN_IRON_GATE:
         case FENCE_PLAN_SLATE_IRON_GATE:
         case FENCE_PLAN_ROUNDED_STONE_IRON_GATE:
         case FENCE_PLAN_POTTERY_IRON_GATE:
         case FENCE_PLAN_SANDSTONE_IRON_GATE:
         case FENCE_PLAN_RENDERED_IRON_GATE:
         case FENCE_PLAN_MARBLE_IRON_GATE:
         case FENCE_PLAN_IRON_GATE_HIGH:
         case FENCE_PLAN_WOVEN:
         case FENCE_PLAN_STONE_PARAPET:
         case FENCE_PLAN_STONE_IRON_PARAPET:
         case FENCE_PLAN_WOODEN_PARAPET:
         case FENCE_PLAN_STONE:
         case FENCE_PLAN_SLATE:
         case FENCE_PLAN_ROUNDED_STONE:
         case FENCE_PLAN_POTTERY:
         case FENCE_PLAN_SANDSTONE:
         case FENCE_PLAN_RENDERED:
         case FENCE_PLAN_MARBLE:
         case FENCE_PLAN_ROPE_LOW:
         case FENCE_PLAN_GARDESGARD_LOW:
         case FENCE_PLAN_GARDESGARD_HIGH:
         case FENCE_PLAN_GARDESGARD_GATE:
         case FENCE_PLAN_CURB:
         case FENCE_PLAN_ROPE_HIGH:
         case FENCE_MAGIC_FIRE:
         case FENCE_RUBBLE:
         case FLOWERBED_BLUE:
         case FLOWERBED_GREENISH_YELLOW:
         case FLOWERBED_ORANGE_RED:
         case FLOWERBED_PURPLE:
         case FLOWERBED_WHITE:
         case FLOWERBED_WHITE_DOTTED:
         case FLOWERBED_YELLOW:
         case WALL_RUBBLE:
         case WALL_PLAIN_NARROW_WINDOW_PLAN:
         case WALL_DOOR_ARCHED_WOODEN:
         case WALL_LEFT_ARCH_WOODEN:
         case WALL_RIGHT_ARCH_WOODEN:
         case WALL_T_ARCH_WOODEN:
         case WALL_DOUBLE_DOOR_WOODEN_PLAN:
         case WALL_DOOR_ARCHED_WOODEN_PLAN:
         case WALL_DOUBLE_DOOR_STONE_PLAN:
         case WALL_DOOR_ARCHED_PLAN:
         case WALL_DOOR_ARCHED_STONE_DECORATED:
         case WALL_LEFT_ARCH_STONE_DECORATED:
         case WALL_RIGHT_ARCH_STONE_DECORATED:
         case WALL_T_ARCH_STONE_DECORATED:
         case WALL_DOOR_ARCHED_STONE:
         case WALL_LEFT_ARCH_STONE:
         case WALL_RIGHT_ARCH_STONE:
         case WALL_T_ARCH_STONE:
         case WALL_SOLID_WOODEN_PLAN:
         case WALL_WINDOW_WOODEN_PLAN:
         case WALL_DOOR_WOODEN_PLAN:
         case WALL_CANOPY_WOODEN_PLAN:
         case WALL_SOLID_STONE_PLAN:
         case WALL_ORIEL_STONE_DECORATED_PLAN:
         case WALL_WINDOW_STONE_PLAN:
         case WALL_DOOR_STONE_PLAN:
         case WALL_DOOR_ARCHED_TIMBER_FRAMED:
         case WALL_LEFT_ARCH_TIMBER_FRAMED:
         case WALL_RIGHT_ARCH_TIMBER_FRAMED:
         case WALL_T_ARCH_TIMBER_FRAMED:
         case WALL_SOLID_TIMBER_FRAMED_PLAN:
         case WALL_BALCONY_TIMBER_FRAMED_PLAN:
         case WALL_JETTY_TIMBER_FRAMED_PLAN:
         case WALL_WINDOW_TIMBER_FRAMED_PLAN:
         case WALL_DOOR_TIMBER_FRAMED_PLAN:
         case WALL_DOUBLE_DOOR_TIMBER_FRAMED_PLAN:
         case WALL_DOOR_ARCHED_TIMBER_FRAMED_PLAN:
         case WALL_ARCHED_SLATE:
         case WALL_LEFT_ARCH_SLATE:
         case WALL_RIGHT_ARCH_SLATE:
         case WALL_T_ARCH_SLATE:
         case WALL_ARCHED_ROUNDED_STONE:
         case WALL_LEFT_ARCH_ROUNDED_STONE:
         case WALL_RIGHT_ARCH_ROUNDED_STONE:
         case WALL_T_ARCH_ROUNDED_STONE:
         case WALL_ARCHED_POTTERY:
         case WALL_LEFT_ARCH_POTTERY:
         case WALL_RIGHT_ARCH_POTTERY:
         case WALL_T_ARCH_POTTERY:
         case WALL_ARCHED_SANDSTONE:
         case WALL_LEFT_ARCH_SANDSTONE:
         case WALL_RIGHT_ARCH_SANDSTONE:
         case WALL_T_ARCH_SANDSTONE:
         case WALL_ARCHED_RENDERED:
         case WALL_LEFT_ARCH_RENDERED:
         case WALL_RIGHT_ARCH_RENDERED:
         case WALL_T_ARCH_RENDERED:
         case WALL_ARCHED_MARBLE:
         case WALL_LEFT_ARCH_MARBLE:
         case WALL_RIGHT_ARCH_MARBLE:
         case WALL_T_ARCH_MARBLE:
         case NO_WALL:
         case FENCE_PLAN_MEDIUM_CHAIN:
         case FENCE_PLAN_PORTCULLIS:
         default:
            return 0.0F;
         case FENCE_STONE_PARAPET:
         case FENCE_STONE_IRON_PARAPET:
         case FENCE_SLATE_STONE_PARAPET:
         case FENCE_ROUNDED_STONE_STONE_PARAPET:
         case FENCE_SANDSTONE_STONE_PARAPET:
         case FENCE_RENDERED_STONE_PARAPET:
         case FENCE_POTTERY_STONE_PARAPET:
         case FENCE_MARBLE_STONE_PARAPET:
            return 2.0F;
         case FENCE_WOODEN_PARAPET:
            return 2.0F;
         case FENCE_GARDESGARD_LOW:
            return 1.0F;
         case FENCE_GARDESGARD_HIGH:
            return 1.5F;
         case FENCE_GARDESGARD_GATE:
            return 1.2F;
         case FENCE_ROPE_HIGH:
            return 1.3F;
         case FENCE_STONE:
         case FENCE_SLATE:
         case FENCE_ROUNDED_STONE:
         case FENCE_POTTERY:
         case FENCE_SANDSTONE:
         case FENCE_RENDERED:
         case FENCE_MARBLE:
            return 1.6F;
         case HEDGE_FLOWER1_LOW:
         case HEDGE_FLOWER1_MEDIUM:
         case HEDGE_FLOWER1_HIGH:
         case HEDGE_FLOWER2_LOW:
         case HEDGE_FLOWER2_MEDIUM:
         case HEDGE_FLOWER2_HIGH:
         case HEDGE_FLOWER3_LOW:
         case HEDGE_FLOWER3_MEDIUM:
         case HEDGE_FLOWER3_HIGH:
         case HEDGE_FLOWER4_LOW:
         case HEDGE_FLOWER4_MEDIUM:
         case HEDGE_FLOWER4_HIGH:
         case HEDGE_FLOWER5_LOW:
         case HEDGE_FLOWER5_MEDIUM:
         case HEDGE_FLOWER5_HIGH:
         case HEDGE_FLOWER6_LOW:
         case HEDGE_FLOWER6_MEDIUM:
         case HEDGE_FLOWER6_HIGH:
         case HEDGE_FLOWER7_LOW:
         case HEDGE_FLOWER7_MEDIUM:
         case HEDGE_FLOWER7_HIGH:
         case FENCE_MAGIC_STONE:
         case FENCE_MAGIC_ICE:
            return 2.0F;
         case FENCE_SIEGEWALL:
            return 3.0F;
         case WALL_SOLID_WOODEN:
         case WALL_CANOPY_WOODEN:
         case WALL_WINDOW_WOODEN:
         case WALL_WINDOW_WIDE_WOODEN:
         case WALL_DOOR_WOODEN:
         case WALL_DOUBLE_DOOR_WOODEN:
         case WALL_PORTCULLIS_WOOD:
            return 3.0F;
         case WALL_SOLID_STONE_DECORATED:
         case WALL_ORIEL_STONE_DECORATED:
         case WALL_ORIEL_STONE_PLAIN:
         case WALL_SOLID_STONE:
         case WALL_BARRED_STONE:
         case WALL_WINDOW_STONE:
         case WALL_PLAIN_NARROW_WINDOW:
         case WALL_DOOR_STONE:
         case WALL_WINDOW_STONE_DECORATED:
         case WALL_DOOR_STONE_DECORATED:
         case WALL_DOUBLE_DOOR_STONE_DECORATED:
         case WALL_PORTCULLIS_STONE:
         case WALL_PORTCULLIS_STONE_DECORATED:
         case WALL_DOUBLE_DOOR_STONE:
         case WALL_SOLID_TIMBER_FRAMED:
         case WALL_WINDOW_TIMBER_FRAMED:
         case WALL_DOOR_TIMBER_FRAMED:
         case WALL_DOUBLE_DOOR_TIMBER_FRAMED:
         case WALL_BALCONY_TIMBER_FRAMED:
         case WALL_JETTY_TIMBER_FRAMED:
         case WALL_SOLID_SLATE:
         case WALL_WINDOW_SLATE:
         case WALL_NARROW_WINDOW_SLATE:
         case WALL_DOOR_SLATE:
         case WALL_DOUBLE_DOOR_SLATE:
         case WALL_PORTCULLIS_SLATE:
         case WALL_BARRED_SLATE:
         case WALL_ORIEL_SLATE:
         case WALL_SOLID_ROUNDED_STONE:
         case WALL_WINDOW_ROUNDED_STONE:
         case WALL_NARROW_WINDOW_ROUNDED_STONE:
         case WALL_DOOR_ROUNDED_STONE:
         case WALL_DOUBLE_DOOR_ROUNDED_STONE:
         case WALL_PORTCULLIS_ROUNDED_STONE:
         case WALL_BARRED_ROUNDED_STONE:
         case WALL_ORIEL_ROUNDED_STONE:
         case WALL_SOLID_POTTERY:
         case WALL_WINDOW_POTTERY:
         case WALL_NARROW_WINDOW_POTTERY:
         case WALL_DOOR_POTTERY:
         case WALL_DOUBLE_DOOR_POTTERY:
         case WALL_PORTCULLIS_POTTERY:
         case WALL_BARRED_POTTERY:
         case WALL_ORIEL_POTTERY:
         case WALL_SOLID_SANDSTONE:
         case WALL_WINDOW_SANDSTONE:
         case WALL_NARROW_WINDOW_SANDSTONE:
         case WALL_DOOR_SANDSTONE:
         case WALL_DOUBLE_DOOR_SANDSTONE:
         case WALL_PORTCULLIS_SANDSTONE:
         case WALL_BARRED_SANDSTONE:
         case WALL_ORIEL_SANDSTONE:
         case WALL_SOLID_RENDERED:
         case WALL_WINDOW_RENDERED:
         case WALL_NARROW_WINDOW_RENDERED:
         case WALL_DOOR_RENDERED:
         case WALL_DOUBLE_DOOR_RENDERED:
         case WALL_PORTCULLIS_RENDERED:
         case WALL_BARRED_RENDERED:
         case WALL_ORIEL_RENDERED:
         case WALL_SOLID_MARBLE:
         case WALL_WINDOW_MARBLE:
         case WALL_NARROW_WINDOW_MARBLE:
         case WALL_DOOR_MARBLE:
         case WALL_DOUBLE_DOOR_MARBLE:
         case WALL_PORTCULLIS_MARBLE:
         case WALL_BARRED_MARBLE:
         case WALL_ORIEL_MARBLE:
         case FENCE_PORTCULLIS:
         case FENCE_SLATE_PORTCULLIS:
         case FENCE_ROUNDED_STONE_PORTCULLIS:
         case FENCE_SANDSTONE_PORTCULLIS:
         case FENCE_RENDERED_PORTCULLIS:
         case FENCE_POTTERY_PORTCULLIS:
         case FENCE_MARBLE_PORTCULLIS:
            return 3.0F;
      }
   }

   public static final boolean isBlocking(StructureConstantsEnum type) {
      switch(type) {
         case FENCE_WOVEN:
            return false;
         case FENCE_PLAN_WOODEN:
         case FENCE_PLAN_WOODEN_CRUDE:
         case FENCE_PLAN_WOODEN_GATE_CRUDE:
         case FENCE_PLAN_PALISADE:
         case FENCE_PLAN_STONEWALL:
         case FENCE_PLAN_PALISADE_GATE:
         case FENCE_PLAN_WOODEN_GATE:
         case FENCE_PLAN_STONEWALL_HIGH:
         case FENCE_PLAN_IRON:
         case FENCE_PLAN_IRON_HIGH:
         case FENCE_PLAN_IRON_GATE:
         case FENCE_PLAN_IRON_GATE_HIGH:
         case FENCE_PLAN_WOVEN:
         case FENCE_PLAN_STONE_PARAPET:
         case FENCE_PLAN_WOODEN_PARAPET:
         case FENCE_ROPE_LOW:
         case FENCE_CURB:
         case FENCE_PLAN_STONE:
         case FENCE_PLAN_ROPE_LOW:
         case FENCE_PLAN_GARDESGARD_LOW:
         case FENCE_PLAN_GARDESGARD_HIGH:
         case FENCE_PLAN_GARDESGARD_GATE:
         case FENCE_PLAN_CURB:
         case FENCE_PLAN_ROPE_HIGH:
         case HEDGE_FLOWER1_LOW:
         case HEDGE_FLOWER1_MEDIUM:
         case HEDGE_FLOWER1_HIGH:
         case HEDGE_FLOWER2_LOW:
         case HEDGE_FLOWER3_LOW:
         case HEDGE_FLOWER4_LOW:
         case HEDGE_FLOWER5_LOW:
         case HEDGE_FLOWER6_LOW:
         case HEDGE_FLOWER7_LOW:
         case FENCE_MAGIC_FIRE:
         case FENCE_RUBBLE:
         case FLOWERBED_BLUE:
         case FLOWERBED_GREENISH_YELLOW:
         case FLOWERBED_ORANGE_RED:
         case FLOWERBED_PURPLE:
         case FLOWERBED_WHITE:
         case FLOWERBED_WHITE_DOTTED:
         case FLOWERBED_YELLOW:
         case WALL_JETTY_TIMBER_FRAMED_PLAN:
         case FENCE_PLAN_MEDIUM_CHAIN:
            return false;
         case FENCE_PLAN_SLATE_IRON:
         case FENCE_PLAN_ROUNDED_STONE_IRON:
         case FENCE_PLAN_POTTERY_IRON:
         case FENCE_PLAN_SANDSTONE_IRON:
         case FENCE_PLAN_RENDERED_IRON:
         case FENCE_PLAN_MARBLE_IRON:
         case FENCE_PLAN_SLATE_IRON_GATE:
         case FENCE_PLAN_ROUNDED_STONE_IRON_GATE:
         case FENCE_PLAN_POTTERY_IRON_GATE:
         case FENCE_PLAN_SANDSTONE_IRON_GATE:
         case FENCE_PLAN_RENDERED_IRON_GATE:
         case FENCE_PLAN_MARBLE_IRON_GATE:
         case FENCE_STONE_PARAPET:
         case FENCE_STONE_IRON_PARAPET:
         case FENCE_PLAN_STONE_IRON_PARAPET:
         case FENCE_WOODEN_PARAPET:
         case FENCE_GARDESGARD_LOW:
         case FENCE_GARDESGARD_HIGH:
         case FENCE_GARDESGARD_GATE:
         case FENCE_ROPE_HIGH:
         case FENCE_STONE:
         case FENCE_SLATE:
         case FENCE_ROUNDED_STONE:
         case FENCE_POTTERY:
         case FENCE_SANDSTONE:
         case FENCE_RENDERED:
         case FENCE_MARBLE:
         case FENCE_PLAN_SLATE:
         case FENCE_PLAN_ROUNDED_STONE:
         case FENCE_PLAN_POTTERY:
         case FENCE_PLAN_SANDSTONE:
         case FENCE_PLAN_RENDERED:
         case FENCE_PLAN_MARBLE:
         case HEDGE_FLOWER2_MEDIUM:
         case HEDGE_FLOWER2_HIGH:
         case HEDGE_FLOWER3_MEDIUM:
         case HEDGE_FLOWER3_HIGH:
         case HEDGE_FLOWER4_MEDIUM:
         case HEDGE_FLOWER4_HIGH:
         case HEDGE_FLOWER5_MEDIUM:
         case HEDGE_FLOWER5_HIGH:
         case HEDGE_FLOWER6_MEDIUM:
         case HEDGE_FLOWER6_HIGH:
         case HEDGE_FLOWER7_MEDIUM:
         case HEDGE_FLOWER7_HIGH:
         case FENCE_MAGIC_STONE:
         case FENCE_MAGIC_ICE:
         case FENCE_SIEGEWALL:
         case WALL_SOLID_WOODEN:
         case WALL_CANOPY_WOODEN:
         case WALL_WINDOW_WOODEN:
         case WALL_WINDOW_WIDE_WOODEN:
         case WALL_DOOR_WOODEN:
         case WALL_DOUBLE_DOOR_WOODEN:
         case WALL_SOLID_STONE_DECORATED:
         case WALL_ORIEL_STONE_DECORATED:
         case WALL_ORIEL_STONE_PLAIN:
         case WALL_SOLID_STONE:
         case WALL_BARRED_STONE:
         case WALL_WINDOW_STONE:
         case WALL_PLAIN_NARROW_WINDOW:
         case WALL_DOOR_STONE:
         case WALL_WINDOW_STONE_DECORATED:
         case WALL_DOOR_STONE_DECORATED:
         case WALL_DOUBLE_DOOR_STONE_DECORATED:
         case WALL_PLAIN_NARROW_WINDOW_PLAN:
         case WALL_PORTCULLIS_STONE:
         case WALL_PORTCULLIS_WOOD:
         case WALL_PORTCULLIS_STONE_DECORATED:
         case WALL_DOUBLE_DOOR_STONE:
         case WALL_SOLID_TIMBER_FRAMED:
         case WALL_WINDOW_TIMBER_FRAMED:
         case WALL_DOOR_TIMBER_FRAMED:
         case WALL_DOUBLE_DOOR_TIMBER_FRAMED:
         case WALL_BALCONY_TIMBER_FRAMED:
         case WALL_JETTY_TIMBER_FRAMED:
         case WALL_SOLID_SLATE:
         case WALL_WINDOW_SLATE:
         case WALL_NARROW_WINDOW_SLATE:
         case WALL_DOOR_SLATE:
         case WALL_DOUBLE_DOOR_SLATE:
         case WALL_PORTCULLIS_SLATE:
         case WALL_BARRED_SLATE:
         case WALL_ORIEL_SLATE:
         case WALL_SOLID_ROUNDED_STONE:
         case WALL_WINDOW_ROUNDED_STONE:
         case WALL_NARROW_WINDOW_ROUNDED_STONE:
         case WALL_DOOR_ROUNDED_STONE:
         case WALL_DOUBLE_DOOR_ROUNDED_STONE:
         case WALL_PORTCULLIS_ROUNDED_STONE:
         case WALL_BARRED_ROUNDED_STONE:
         case WALL_ORIEL_ROUNDED_STONE:
         case WALL_SOLID_POTTERY:
         case WALL_WINDOW_POTTERY:
         case WALL_NARROW_WINDOW_POTTERY:
         case WALL_DOOR_POTTERY:
         case WALL_DOUBLE_DOOR_POTTERY:
         case WALL_PORTCULLIS_POTTERY:
         case WALL_BARRED_POTTERY:
         case WALL_ORIEL_POTTERY:
         case WALL_SOLID_SANDSTONE:
         case WALL_WINDOW_SANDSTONE:
         case WALL_NARROW_WINDOW_SANDSTONE:
         case WALL_DOOR_SANDSTONE:
         case WALL_DOUBLE_DOOR_SANDSTONE:
         case WALL_PORTCULLIS_SANDSTONE:
         case WALL_BARRED_SANDSTONE:
         case WALL_ORIEL_SANDSTONE:
         case WALL_SOLID_RENDERED:
         case WALL_WINDOW_RENDERED:
         case WALL_NARROW_WINDOW_RENDERED:
         case WALL_DOOR_RENDERED:
         case WALL_DOUBLE_DOOR_RENDERED:
         case WALL_PORTCULLIS_RENDERED:
         case WALL_BARRED_RENDERED:
         case WALL_ORIEL_RENDERED:
         case WALL_SOLID_MARBLE:
         case WALL_WINDOW_MARBLE:
         case WALL_NARROW_WINDOW_MARBLE:
         case WALL_DOOR_MARBLE:
         case WALL_DOUBLE_DOOR_MARBLE:
         case WALL_PORTCULLIS_MARBLE:
         case WALL_BARRED_MARBLE:
         case WALL_ORIEL_MARBLE:
         case FENCE_MEDIUM_CHAIN:
         default:
            return true;
         case WALL_RUBBLE:
         case WALL_DOUBLE_DOOR_WOODEN_PLAN:
         case WALL_DOUBLE_DOOR_STONE_PLAN:
         case WALL_SOLID_WOODEN_PLAN:
         case WALL_WINDOW_WOODEN_PLAN:
         case WALL_DOOR_WOODEN_PLAN:
         case WALL_CANOPY_WOODEN_PLAN:
         case WALL_SOLID_STONE_PLAN:
         case WALL_ORIEL_STONE_DECORATED_PLAN:
         case WALL_WINDOW_STONE_PLAN:
         case WALL_DOOR_STONE_PLAN:
         case WALL_SOLID_TIMBER_FRAMED_PLAN:
         case WALL_BALCONY_TIMBER_FRAMED_PLAN:
         case WALL_WINDOW_TIMBER_FRAMED_PLAN:
         case WALL_DOOR_TIMBER_FRAMED_PLAN:
         case WALL_DOUBLE_DOOR_TIMBER_FRAMED_PLAN:
         case WALL_DOOR_ARCHED_TIMBER_FRAMED_PLAN:
            return false;
         case WALL_DOOR_ARCHED_WOODEN:
         case WALL_LEFT_ARCH_WOODEN:
         case WALL_RIGHT_ARCH_WOODEN:
         case WALL_T_ARCH_WOODEN:
         case WALL_DOOR_ARCHED_WOODEN_PLAN:
         case WALL_DOOR_ARCHED_PLAN:
         case WALL_DOOR_ARCHED_STONE_DECORATED:
         case WALL_LEFT_ARCH_STONE_DECORATED:
         case WALL_RIGHT_ARCH_STONE_DECORATED:
         case WALL_T_ARCH_STONE_DECORATED:
         case WALL_DOOR_ARCHED_STONE:
         case WALL_LEFT_ARCH_STONE:
         case WALL_RIGHT_ARCH_STONE:
         case WALL_T_ARCH_STONE:
         case WALL_DOOR_ARCHED_TIMBER_FRAMED:
         case WALL_LEFT_ARCH_TIMBER_FRAMED:
         case WALL_RIGHT_ARCH_TIMBER_FRAMED:
         case WALL_T_ARCH_TIMBER_FRAMED:
         case WALL_ARCHED_SLATE:
         case WALL_LEFT_ARCH_SLATE:
         case WALL_RIGHT_ARCH_SLATE:
         case WALL_T_ARCH_SLATE:
         case WALL_ARCHED_ROUNDED_STONE:
         case WALL_LEFT_ARCH_ROUNDED_STONE:
         case WALL_RIGHT_ARCH_ROUNDED_STONE:
         case WALL_T_ARCH_ROUNDED_STONE:
         case WALL_ARCHED_POTTERY:
         case WALL_LEFT_ARCH_POTTERY:
         case WALL_RIGHT_ARCH_POTTERY:
         case WALL_T_ARCH_POTTERY:
         case WALL_ARCHED_SANDSTONE:
         case WALL_LEFT_ARCH_SANDSTONE:
         case WALL_RIGHT_ARCH_SANDSTONE:
         case WALL_T_ARCH_SANDSTONE:
         case WALL_ARCHED_RENDERED:
         case WALL_LEFT_ARCH_RENDERED:
         case WALL_RIGHT_ARCH_RENDERED:
         case WALL_T_ARCH_RENDERED:
         case WALL_ARCHED_MARBLE:
         case WALL_LEFT_ARCH_MARBLE:
         case WALL_RIGHT_ARCH_MARBLE:
         case WALL_T_ARCH_MARBLE:
            return false;
         case NO_WALL:
         case FENCE_PLAN_PORTCULLIS:
            return false;
      }
   }
}

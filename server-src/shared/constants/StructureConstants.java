package com.wurmonline.shared.constants;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

public interface StructureConstants {
   byte STRUCTURE_HOUSE = 0;
   byte STRUCTURE_BRIDGE = 1;

   public static final class FloorMappings {
      public static final Map<StructureConstants.Pair<StructureConstants.FloorType, StructureConstants.FloorMaterial>, String> mappings = new HashMap<>();

      private FloorMappings() {
      }

      public static final String getMapping(StructureConstants.FloorType t, StructureConstants.FloorMaterial m) {
         StructureConstants.Pair<StructureConstants.FloorType, StructureConstants.FloorMaterial> p = new StructureConstants.Pair<>(t, m);
         return mappings.get(p);
      }

      static {
         for(StructureConstants.FloorType t : StructureConstants.FloorType.values()) {
            for(StructureConstants.FloorMaterial m : StructureConstants.FloorMaterial.values()) {
               String mapping = "img.texture.floor." + t.toString().toLowerCase() + "." + m.toString().toLowerCase();
               StructureConstants.Pair<StructureConstants.FloorType, StructureConstants.FloorMaterial> p = new StructureConstants.Pair<>(t, m);
               mappings.put(p, mapping);
            }
         }
      }
   }

   public static enum FloorMaterial {
      WOOD((byte)0, "Wood", "wood"),
      STONE_BRICK((byte)1, "Stone brick", "stone_brick"),
      SANDSTONE_SLAB((byte)2, "Sandstone slab", "sandstone_slab"),
      SLATE_SLAB((byte)3, "Slate slab", "slate_slab"),
      THATCH((byte)4, "Thatch", "thatch"),
      METAL_IRON((byte)5, "Iron", "metal_iron"),
      METAL_STEEL((byte)6, "Steel", "metal_steel"),
      METAL_COPPER((byte)7, "Copper", "metal_copper"),
      CLAY_BRICK((byte)8, "Clay brick", "clay_brick"),
      METAL_GOLD((byte)9, "Gold", "metal_gold"),
      METAL_SILVER((byte)10, "Silver", "metal_silver"),
      MARBLE_SLAB((byte)11, "Marble slab", "marble_slab"),
      STANDALONE((byte)12, "Standalone", "standalone"),
      STONE_SLAB((byte)13, "Stone slab", "stone_slab");

      private byte material;
      private String name;
      private String modelName;
      private static final StructureConstants.FloorMaterial[] types = values();

      private FloorMaterial(byte newMaterial, String newName, String newModelName) {
         this.material = newMaterial;
         this.name = newName;
         this.modelName = newModelName;
      }

      public byte getCode() {
         return this.material;
      }

      public static StructureConstants.FloorMaterial fromByte(byte typeByte) {
         for(int i = 0; i < types.length; ++i) {
            if (typeByte == types[i].getCode()) {
               return types[i];
            }
         }

         return null;
      }

      public final String getName() {
         return this.name;
      }

      public final String getModelName() {
         return this.modelName;
      }

      public static final String getTextureName(StructureConstants.FloorType type, StructureConstants.FloorMaterial material) {
         return StructureConstants.FloorMappings.getMapping(type, material);
      }
   }

   public static enum FloorState {
      PLANNING((byte)-1),
      BUILDING((byte)0),
      COMPLETED((byte)127);

      private byte state;
      private static final StructureConstants.FloorState[] types = values();

      private FloorState(byte newState) {
         this.state = newState;
      }

      public byte getCode() {
         return this.state;
      }

      public static StructureConstants.FloorState fromByte(byte floorStateByte) {
         for(int i = 0; i < types.length; ++i) {
            if (floorStateByte == types[i].getCode()) {
               return types[i];
            }
         }

         return BUILDING;
      }
   }

   public static enum FloorType {
      UNKNOWN((byte)100, false, "unknown"),
      FLOOR((byte)10, false, "floor"),
      DOOR((byte)11, false, "hatch"),
      OPENING((byte)12, false, "opening"),
      ROOF((byte)13, false, "roof"),
      SOLID((byte)14, false, "solid"),
      STAIRCASE((byte)15, true, "staircase"),
      WIDE_STAIRCASE((byte)16, true, "staircase, wide"),
      RIGHT_STAIRCASE((byte)17, true, "staircase, right"),
      LEFT_STAIRCASE((byte)18, true, "staircase, left"),
      WIDE_STAIRCASE_RIGHT((byte)19, true, "staircase, wide with right banisters"),
      WIDE_STAIRCASE_LEFT((byte)20, true, "staircase, wide with left banisters"),
      WIDE_STAIRCASE_BOTH((byte)21, true, "staircase, wide with both banisters"),
      CLOCKWISE_STAIRCASE((byte)22, true, "staircase, clockwise spiral"),
      ANTICLOCKWISE_STAIRCASE((byte)23, true, "staircase, counter clockwise spiral"),
      CLOCKWISE_STAIRCASE_WITH((byte)24, true, "staircase, clockwise spiral with banisters"),
      ANTICLOCKWISE_STAIRCASE_WITH((byte)25, true, "staircase, counter clockwise spiral with banisters");

      private byte type;
      private String name;
      private boolean isStair;
      private static final StructureConstants.FloorType[] types = values();

      private FloorType(byte newType, boolean newIsStair, String newName) {
         this.type = newType;
         this.name = newName;
         this.isStair = newIsStair;
      }

      public byte getCode() {
         return this.type;
      }

      public boolean isStair() {
         return this.isStair;
      }

      public static StructureConstants.FloorType fromByte(byte typeByte) {
         for(int i = 0; i < types.length; ++i) {
            if (typeByte == types[i].getCode()) {
               return types[i];
            }
         }

         return UNKNOWN;
      }

      public final String getName() {
         return this.name;
      }

      public static final String getModelName(
         StructureConstants.FloorType type, StructureConstants.FloorMaterial material, StructureConstants.FloorState state
      ) {
         if (type == STAIRCASE) {
            if (state == StructureConstants.FloorState.PLANNING) {
               return "model.structure.staircase.plan";
            } else {
               return state == StructureConstants.FloorState.BUILDING
                  ? "model.structure.staircase.plan." + material.toString().toLowerCase(Locale.ENGLISH)
                  : "model.structure.staircase." + material.toString().toLowerCase(Locale.ENGLISH);
            }
         } else if (type == CLOCKWISE_STAIRCASE) {
            if (state == StructureConstants.FloorState.PLANNING) {
               return "model.structure.staircase.clockwise.none.plan";
            } else {
               return state == StructureConstants.FloorState.BUILDING
                  ? "model.structure.staircase.clockwise.none.plan." + material.toString().toLowerCase(Locale.ENGLISH)
                  : "model.structure.staircase.clockwise.none." + material.toString().toLowerCase(Locale.ENGLISH);
            }
         } else if (type == CLOCKWISE_STAIRCASE_WITH) {
            if (state == StructureConstants.FloorState.PLANNING) {
               return "model.structure.staircase.clockwise.with.plan";
            } else {
               return state == StructureConstants.FloorState.BUILDING
                  ? "model.structure.staircase.clockwise.with.plan." + material.toString().toLowerCase(Locale.ENGLISH)
                  : "model.structure.staircase.clockwise.with." + material.toString().toLowerCase(Locale.ENGLISH);
            }
         } else if (type == ANTICLOCKWISE_STAIRCASE) {
            if (state == StructureConstants.FloorState.PLANNING) {
               return "model.structure.staircase.anticlockwise.none.plan";
            } else {
               return state == StructureConstants.FloorState.BUILDING
                  ? "model.structure.staircase.anticlockwise.none.plan." + material.toString().toLowerCase(Locale.ENGLISH)
                  : "model.structure.staircase.anticlockwise.none." + material.toString().toLowerCase(Locale.ENGLISH);
            }
         } else if (type == ANTICLOCKWISE_STAIRCASE_WITH) {
            if (state == StructureConstants.FloorState.PLANNING) {
               return "model.structure.staircase.anticlockwise.with.plan";
            } else {
               return state == StructureConstants.FloorState.BUILDING
                  ? "model.structure.staircase.anticlockwise.with.plan." + material.toString().toLowerCase(Locale.ENGLISH)
                  : "model.structure.staircase.anticlockwise.with." + material.toString().toLowerCase(Locale.ENGLISH);
            }
         } else if (type == WIDE_STAIRCASE) {
            if (state == StructureConstants.FloorState.PLANNING) {
               return "model.structure.staircase.wide.none.plan";
            } else {
               return state == StructureConstants.FloorState.BUILDING
                  ? "model.structure.staircase.wide.none.plan." + material.toString().toLowerCase(Locale.ENGLISH)
                  : "model.structure.staircase.wide.none." + material.toString().toLowerCase(Locale.ENGLISH);
            }
         } else if (type == WIDE_STAIRCASE_LEFT) {
            if (state == StructureConstants.FloorState.PLANNING) {
               return "model.structure.staircase.wide.left.plan";
            } else {
               return state == StructureConstants.FloorState.BUILDING
                  ? "model.structure.staircase.wide.left.plan." + material.toString().toLowerCase(Locale.ENGLISH)
                  : "model.structure.staircase.wide.left." + material.toString().toLowerCase(Locale.ENGLISH);
            }
         } else if (type == WIDE_STAIRCASE_RIGHT) {
            if (state == StructureConstants.FloorState.PLANNING) {
               return "model.structure.staircase.wide.right.plan";
            } else {
               return state == StructureConstants.FloorState.BUILDING
                  ? "model.structure.staircase.wide.right.plan." + material.toString().toLowerCase(Locale.ENGLISH)
                  : "model.structure.staircase.wide.right." + material.toString().toLowerCase(Locale.ENGLISH);
            }
         } else if (type == WIDE_STAIRCASE_BOTH) {
            if (state == StructureConstants.FloorState.PLANNING) {
               return "model.structure.staircase.wide.both.plan";
            } else {
               return state == StructureConstants.FloorState.BUILDING
                  ? "model.structure.staircase.wide.both.plan." + material.toString().toLowerCase(Locale.ENGLISH)
                  : "model.structure.staircase.wide.both." + material.toString().toLowerCase(Locale.ENGLISH);
            }
         } else if (type == RIGHT_STAIRCASE) {
            if (state == StructureConstants.FloorState.PLANNING) {
               return "model.structure.staircase.right.plan";
            } else {
               return state == StructureConstants.FloorState.BUILDING
                  ? "model.structure.staircase.right.plan." + material.toString().toLowerCase(Locale.ENGLISH)
                  : "model.structure.staircase.right." + material.toString().toLowerCase(Locale.ENGLISH);
            }
         } else if (type == LEFT_STAIRCASE) {
            if (state == StructureConstants.FloorState.PLANNING) {
               return "model.structure.staircase.left.plan";
            } else {
               return state == StructureConstants.FloorState.BUILDING
                  ? "model.structure.staircase.left.plan." + material.toString().toLowerCase(Locale.ENGLISH)
                  : "model.structure.staircase.left." + material.toString().toLowerCase(Locale.ENGLISH);
            }
         } else if (type == OPENING) {
            if (state == StructureConstants.FloorState.PLANNING) {
               return "model.structure.floor.opening.plan";
            } else {
               return state == StructureConstants.FloorState.BUILDING
                  ? "model.structure.floor.opening.plan." + material.toString().toLowerCase(Locale.ENGLISH)
                  : "model.structure.floor.opening." + material.toString().toLowerCase(Locale.ENGLISH);
            }
         } else {
            String modelName;
            if (state == StructureConstants.FloorState.PLANNING) {
               modelName = "model.structure.floor.plan";
            } else if (state == StructureConstants.FloorState.BUILDING) {
               modelName = "model.structure.floor.plan." + material.toString().toLowerCase(Locale.ENGLISH);
            } else if (type == ROOF) {
               modelName = "model.structure.roof." + material.toString().toLowerCase(Locale.ENGLISH);
            } else {
               modelName = "model.structure.floor." + material.toString().toLowerCase(Locale.ENGLISH);
            }

            if (type == UNKNOWN) {
               modelName = "model.structure.floor.plan";
            }

            return modelName;
         }
      }

      public static final int getIconId(StructureConstants.FloorType type, StructureConstants.FloorMaterial material, StructureConstants.FloorState state) {
         if (state == StructureConstants.FloorState.PLANNING || state == StructureConstants.FloorState.BUILDING) {
            return 60;
         } else {
            return type == ROOF ? getRoofIconId(material) : getFloorIconId(material);
         }
      }

      private static int getFloorIconId(StructureConstants.FloorMaterial material) {
         int returnId = 60;
         byte var2;
         switch(material) {
            case WOOD:
               var2 = 60;
               break;
            case STONE_BRICK:
               var2 = 60;
               break;
            case CLAY_BRICK:
               var2 = 60;
               break;
            case SLATE_SLAB:
               var2 = 60;
               break;
            case STONE_SLAB:
               var2 = 60;
               break;
            case THATCH:
               var2 = 60;
               break;
            case METAL_IRON:
               var2 = 60;
               break;
            case METAL_STEEL:
               var2 = 60;
               break;
            case METAL_COPPER:
               var2 = 60;
               break;
            case METAL_GOLD:
               var2 = 60;
               break;
            case METAL_SILVER:
               var2 = 60;
               break;
            case SANDSTONE_SLAB:
               var2 = 60;
               break;
            case MARBLE_SLAB:
               var2 = 60;
               break;
            case STANDALONE:
               var2 = 60;
               break;
            default:
               var2 = 60;
         }

         return var2;
      }

      private static int getRoofIconId(StructureConstants.FloorMaterial material) {
         int returnId = 60;
         byte var2;
         switch(material) {
            case WOOD:
               var2 = 60;
               break;
            case STONE_BRICK:
               var2 = 60;
               break;
            case CLAY_BRICK:
               var2 = 60;
               break;
            case SLATE_SLAB:
               var2 = 60;
               break;
            case STONE_SLAB:
               var2 = 60;
               break;
            case THATCH:
               var2 = 60;
               break;
            case METAL_IRON:
               var2 = 60;
               break;
            case METAL_STEEL:
               var2 = 60;
               break;
            case METAL_COPPER:
               var2 = 60;
               break;
            case METAL_GOLD:
               var2 = 60;
               break;
            case METAL_SILVER:
               var2 = 60;
               break;
            case SANDSTONE_SLAB:
               var2 = 60;
               break;
            case MARBLE_SLAB:
               var2 = 60;
               break;
            default:
               var2 = 60;
         }

         return var2;
      }
   }

   public static class Pair<K, V> {
      private final K key;
      private final V value;

      public Pair(K key, V value) {
         this.key = key;
         this.value = value;
      }

      public final K getKey() {
         return this.key;
      }

      public final V getValue() {
         return this.value;
      }

      @Override
      public int hashCode() {
         return this.key.hashCode() ^ this.value.hashCode();
      }

      @Override
      public boolean equals(Object o) {
         if (o == null) {
            return false;
         } else if (!(o instanceof StructureConstants.Pair)) {
            return false;
         } else {
            StructureConstants.Pair mapping = (StructureConstants.Pair)o;
            return this.key.equals(mapping.getKey()) && this.value.equals(mapping.getValue());
         }
      }
   }
}

/*
 * Decompiled with CFR 0.152.
 */
package com.wurmonline.shared.constants;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

public interface StructureConstants {
    public static final byte STRUCTURE_HOUSE = 0;
    public static final byte STRUCTURE_BRIDGE = 1;

    public static final class FloorMappings {
        public static final Map<Pair<FloorType, FloorMaterial>, String> mappings = new HashMap<Pair<FloorType, FloorMaterial>, String>();

        private FloorMappings() {
        }

        public static final String getMapping(FloorType t, FloorMaterial m) {
            Pair<FloorType, FloorMaterial> p = new Pair<FloorType, FloorMaterial>(t, m);
            return mappings.get(p);
        }

        static {
            for (FloorType t : FloorType.values()) {
                for (FloorMaterial m : FloorMaterial.values()) {
                    String mapping = "img.texture.floor." + t.toString().toLowerCase() + "." + m.toString().toLowerCase();
                    Pair<FloorType, FloorMaterial> p = new Pair<FloorType, FloorMaterial>(t, m);
                    mappings.put(p, mapping);
                }
            }
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

        public int hashCode() {
            return this.key.hashCode() ^ this.value.hashCode();
        }

        public boolean equals(Object o) {
            if (o == null) {
                return false;
            }
            if (!(o instanceof Pair)) {
                return false;
            }
            Pair mapping = (Pair)o;
            return this.key.equals(mapping.getKey()) && this.value.equals(mapping.getValue());
        }
    }

    public static enum FloorState {
        PLANNING(-1),
        BUILDING(0),
        COMPLETED(127);

        private byte state;
        private static final FloorState[] types;

        private FloorState(byte newState) {
            this.state = newState;
        }

        public byte getCode() {
            return this.state;
        }

        public static FloorState fromByte(byte floorStateByte) {
            for (int i = 0; i < types.length; ++i) {
                if (floorStateByte != types[i].getCode()) continue;
                return types[i];
            }
            return BUILDING;
        }

        static {
            types = FloorState.values();
        }
    }

    public static enum FloorType {
        UNKNOWN(100, false, "unknown"),
        FLOOR(10, false, "floor"),
        DOOR(11, false, "hatch"),
        OPENING(12, false, "opening"),
        ROOF(13, false, "roof"),
        SOLID(14, false, "solid"),
        STAIRCASE(15, true, "staircase"),
        WIDE_STAIRCASE(16, true, "staircase, wide"),
        RIGHT_STAIRCASE(17, true, "staircase, right"),
        LEFT_STAIRCASE(18, true, "staircase, left"),
        WIDE_STAIRCASE_RIGHT(19, true, "staircase, wide with right banisters"),
        WIDE_STAIRCASE_LEFT(20, true, "staircase, wide with left banisters"),
        WIDE_STAIRCASE_BOTH(21, true, "staircase, wide with both banisters"),
        CLOCKWISE_STAIRCASE(22, true, "staircase, clockwise spiral"),
        ANTICLOCKWISE_STAIRCASE(23, true, "staircase, counter clockwise spiral"),
        CLOCKWISE_STAIRCASE_WITH(24, true, "staircase, clockwise spiral with banisters"),
        ANTICLOCKWISE_STAIRCASE_WITH(25, true, "staircase, counter clockwise spiral with banisters");

        private byte type;
        private String name;
        private boolean isStair;
        private static final FloorType[] types;

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

        public static FloorType fromByte(byte typeByte) {
            for (int i = 0; i < types.length; ++i) {
                if (typeByte != types[i].getCode()) continue;
                return types[i];
            }
            return UNKNOWN;
        }

        public final String getName() {
            return this.name;
        }

        public static final String getModelName(FloorType type, FloorMaterial material, FloorState state) {
            if (type == STAIRCASE) {
                if (state == FloorState.PLANNING) {
                    return "model.structure.staircase.plan";
                }
                if (state == FloorState.BUILDING) {
                    return "model.structure.staircase.plan." + material.toString().toLowerCase(Locale.ENGLISH);
                }
                return "model.structure.staircase." + material.toString().toLowerCase(Locale.ENGLISH);
            }
            if (type == CLOCKWISE_STAIRCASE) {
                if (state == FloorState.PLANNING) {
                    return "model.structure.staircase.clockwise.none.plan";
                }
                if (state == FloorState.BUILDING) {
                    return "model.structure.staircase.clockwise.none.plan." + material.toString().toLowerCase(Locale.ENGLISH);
                }
                return "model.structure.staircase.clockwise.none." + material.toString().toLowerCase(Locale.ENGLISH);
            }
            if (type == CLOCKWISE_STAIRCASE_WITH) {
                if (state == FloorState.PLANNING) {
                    return "model.structure.staircase.clockwise.with.plan";
                }
                if (state == FloorState.BUILDING) {
                    return "model.structure.staircase.clockwise.with.plan." + material.toString().toLowerCase(Locale.ENGLISH);
                }
                return "model.structure.staircase.clockwise.with." + material.toString().toLowerCase(Locale.ENGLISH);
            }
            if (type == ANTICLOCKWISE_STAIRCASE) {
                if (state == FloorState.PLANNING) {
                    return "model.structure.staircase.anticlockwise.none.plan";
                }
                if (state == FloorState.BUILDING) {
                    return "model.structure.staircase.anticlockwise.none.plan." + material.toString().toLowerCase(Locale.ENGLISH);
                }
                return "model.structure.staircase.anticlockwise.none." + material.toString().toLowerCase(Locale.ENGLISH);
            }
            if (type == ANTICLOCKWISE_STAIRCASE_WITH) {
                if (state == FloorState.PLANNING) {
                    return "model.structure.staircase.anticlockwise.with.plan";
                }
                if (state == FloorState.BUILDING) {
                    return "model.structure.staircase.anticlockwise.with.plan." + material.toString().toLowerCase(Locale.ENGLISH);
                }
                return "model.structure.staircase.anticlockwise.with." + material.toString().toLowerCase(Locale.ENGLISH);
            }
            if (type == WIDE_STAIRCASE) {
                if (state == FloorState.PLANNING) {
                    return "model.structure.staircase.wide.none.plan";
                }
                if (state == FloorState.BUILDING) {
                    return "model.structure.staircase.wide.none.plan." + material.toString().toLowerCase(Locale.ENGLISH);
                }
                return "model.structure.staircase.wide.none." + material.toString().toLowerCase(Locale.ENGLISH);
            }
            if (type == WIDE_STAIRCASE_LEFT) {
                if (state == FloorState.PLANNING) {
                    return "model.structure.staircase.wide.left.plan";
                }
                if (state == FloorState.BUILDING) {
                    return "model.structure.staircase.wide.left.plan." + material.toString().toLowerCase(Locale.ENGLISH);
                }
                return "model.structure.staircase.wide.left." + material.toString().toLowerCase(Locale.ENGLISH);
            }
            if (type == WIDE_STAIRCASE_RIGHT) {
                if (state == FloorState.PLANNING) {
                    return "model.structure.staircase.wide.right.plan";
                }
                if (state == FloorState.BUILDING) {
                    return "model.structure.staircase.wide.right.plan." + material.toString().toLowerCase(Locale.ENGLISH);
                }
                return "model.structure.staircase.wide.right." + material.toString().toLowerCase(Locale.ENGLISH);
            }
            if (type == WIDE_STAIRCASE_BOTH) {
                if (state == FloorState.PLANNING) {
                    return "model.structure.staircase.wide.both.plan";
                }
                if (state == FloorState.BUILDING) {
                    return "model.structure.staircase.wide.both.plan." + material.toString().toLowerCase(Locale.ENGLISH);
                }
                return "model.structure.staircase.wide.both." + material.toString().toLowerCase(Locale.ENGLISH);
            }
            if (type == RIGHT_STAIRCASE) {
                if (state == FloorState.PLANNING) {
                    return "model.structure.staircase.right.plan";
                }
                if (state == FloorState.BUILDING) {
                    return "model.structure.staircase.right.plan." + material.toString().toLowerCase(Locale.ENGLISH);
                }
                return "model.structure.staircase.right." + material.toString().toLowerCase(Locale.ENGLISH);
            }
            if (type == LEFT_STAIRCASE) {
                if (state == FloorState.PLANNING) {
                    return "model.structure.staircase.left.plan";
                }
                if (state == FloorState.BUILDING) {
                    return "model.structure.staircase.left.plan." + material.toString().toLowerCase(Locale.ENGLISH);
                }
                return "model.structure.staircase.left." + material.toString().toLowerCase(Locale.ENGLISH);
            }
            if (type == OPENING) {
                if (state == FloorState.PLANNING) {
                    return "model.structure.floor.opening.plan";
                }
                if (state == FloorState.BUILDING) {
                    return "model.structure.floor.opening.plan." + material.toString().toLowerCase(Locale.ENGLISH);
                }
                return "model.structure.floor.opening." + material.toString().toLowerCase(Locale.ENGLISH);
            }
            String modelName = state == FloorState.PLANNING ? "model.structure.floor.plan" : (state == FloorState.BUILDING ? "model.structure.floor.plan." + material.toString().toLowerCase(Locale.ENGLISH) : (type == ROOF ? "model.structure.roof." + material.toString().toLowerCase(Locale.ENGLISH) : "model.structure.floor." + material.toString().toLowerCase(Locale.ENGLISH)));
            if (type == UNKNOWN) {
                modelName = "model.structure.floor.plan";
            }
            return modelName;
        }

        public static final int getIconId(FloorType type, FloorMaterial material, FloorState state) {
            if (state == FloorState.PLANNING || state == FloorState.BUILDING) {
                return 60;
            }
            if (type == ROOF) {
                return FloorType.getRoofIconId(material);
            }
            return FloorType.getFloorIconId(material);
        }

        private static int getFloorIconId(FloorMaterial material) {
            int returnId = 60;
            switch (material) {
                case WOOD: {
                    returnId = 60;
                    break;
                }
                case STONE_BRICK: {
                    returnId = 60;
                    break;
                }
                case CLAY_BRICK: {
                    returnId = 60;
                    break;
                }
                case SLATE_SLAB: {
                    returnId = 60;
                    break;
                }
                case STONE_SLAB: {
                    returnId = 60;
                    break;
                }
                case THATCH: {
                    returnId = 60;
                    break;
                }
                case METAL_IRON: {
                    returnId = 60;
                    break;
                }
                case METAL_STEEL: {
                    returnId = 60;
                    break;
                }
                case METAL_COPPER: {
                    returnId = 60;
                    break;
                }
                case METAL_GOLD: {
                    returnId = 60;
                    break;
                }
                case METAL_SILVER: {
                    returnId = 60;
                    break;
                }
                case SANDSTONE_SLAB: {
                    returnId = 60;
                    break;
                }
                case MARBLE_SLAB: {
                    returnId = 60;
                    break;
                }
                case STANDALONE: {
                    returnId = 60;
                    break;
                }
                default: {
                    returnId = 60;
                }
            }
            return returnId;
        }

        private static int getRoofIconId(FloorMaterial material) {
            int returnId = 60;
            switch (material) {
                case WOOD: {
                    returnId = 60;
                    break;
                }
                case STONE_BRICK: {
                    returnId = 60;
                    break;
                }
                case CLAY_BRICK: {
                    returnId = 60;
                    break;
                }
                case SLATE_SLAB: {
                    returnId = 60;
                    break;
                }
                case STONE_SLAB: {
                    returnId = 60;
                    break;
                }
                case THATCH: {
                    returnId = 60;
                    break;
                }
                case METAL_IRON: {
                    returnId = 60;
                    break;
                }
                case METAL_STEEL: {
                    returnId = 60;
                    break;
                }
                case METAL_COPPER: {
                    returnId = 60;
                    break;
                }
                case METAL_GOLD: {
                    returnId = 60;
                    break;
                }
                case METAL_SILVER: {
                    returnId = 60;
                    break;
                }
                case MARBLE_SLAB: {
                    returnId = 60;
                    break;
                }
                case SANDSTONE_SLAB: {
                    returnId = 60;
                    break;
                }
                default: {
                    returnId = 60;
                }
            }
            return returnId;
        }

        static {
            types = FloorType.values();
        }
    }

    public static enum FloorMaterial {
        WOOD(0, "Wood", "wood"),
        STONE_BRICK(1, "Stone brick", "stone_brick"),
        SANDSTONE_SLAB(2, "Sandstone slab", "sandstone_slab"),
        SLATE_SLAB(3, "Slate slab", "slate_slab"),
        THATCH(4, "Thatch", "thatch"),
        METAL_IRON(5, "Iron", "metal_iron"),
        METAL_STEEL(6, "Steel", "metal_steel"),
        METAL_COPPER(7, "Copper", "metal_copper"),
        CLAY_BRICK(8, "Clay brick", "clay_brick"),
        METAL_GOLD(9, "Gold", "metal_gold"),
        METAL_SILVER(10, "Silver", "metal_silver"),
        MARBLE_SLAB(11, "Marble slab", "marble_slab"),
        STANDALONE(12, "Standalone", "standalone"),
        STONE_SLAB(13, "Stone slab", "stone_slab");

        private byte material;
        private String name;
        private String modelName;
        private static final FloorMaterial[] types;

        private FloorMaterial(byte newMaterial, String newName, String newModelName) {
            this.material = newMaterial;
            this.name = newName;
            this.modelName = newModelName;
        }

        public byte getCode() {
            return this.material;
        }

        public static FloorMaterial fromByte(byte typeByte) {
            for (int i = 0; i < types.length; ++i) {
                if (typeByte != types[i].getCode()) continue;
                return types[i];
            }
            return null;
        }

        public final String getName() {
            return this.name;
        }

        public final String getModelName() {
            return this.modelName;
        }

        public static final String getTextureName(FloorType type, FloorMaterial material) {
            return FloorMappings.getMapping(type, material);
        }

        static {
            types = FloorMaterial.values();
        }
    }
}


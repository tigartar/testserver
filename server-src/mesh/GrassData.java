/*
 * Decompiled with CFR 0.152.
 */
package com.wurmonline.mesh;

public final class GrassData {
    private GrassData() {
    }

    private static String getFlowerName(FlowerType flowerType) {
        switch (flowerType) {
            case NONE: {
                return "";
            }
            case FLOWER_1: {
                return "Yellow flowers";
            }
            case FLOWER_2: {
                return "Orange-red flowers";
            }
            case FLOWER_3: {
                return "Purple flowers";
            }
            case FLOWER_4: {
                return "White flowers";
            }
            case FLOWER_5: {
                return "Blue flowers";
            }
            case FLOWER_6: {
                return "Greenish-yellow flowers";
            }
            case FLOWER_7: {
                return "White-dotted flowers";
            }
        }
        return "Unknown grass";
    }

    public static String getModelResourceName(FlowerType flowerType) {
        switch (flowerType) {
            default: 
        }
        return "model.flower.unknown";
    }

    public static String getHelpSubject(int type) {
        return "Terrain:" + GrassType.values()[type].name().replace(' ', '_');
    }

    public static int getFlowerType(byte data) {
        return FlowerType.decodeTileData(data).getType() & 0xFFFF;
    }

    public static String getFlowerTypeName(byte data) {
        return GrassData.getFlowerName(FlowerType.decodeTileData(data));
    }

    public static byte encodeGrassTileData(GrowthStage growthStage, GrassType grassType, FlowerType flowerType) {
        return (byte)(growthStage.getEncodedData() | grassType.getEncodedData() | flowerType.getEncodedData());
    }

    public static byte encodeGrassTileData(GrowthStage growthStage, FlowerType flowerType) {
        return (byte)(growthStage.getEncodedData() | flowerType.getEncodedData());
    }

    public static String getHover(byte data) {
        return GrassType.decodeTileData(data).getName();
    }

    public static int getGrowthRateFor(GrassType grassType, GrowthSeason season) {
        return grassType.getGrowthRateInSeason(season);
    }

    public static enum FlowerType {
        NONE(0),
        FLOWER_1(1),
        FLOWER_2(2),
        FLOWER_3(3),
        FLOWER_4(4),
        FLOWER_5(5),
        FLOWER_6(6),
        FLOWER_7(7),
        FLOWER_8(8),
        FLOWER_9(9),
        FLOWER_10(10),
        FLOWER_11(11),
        FLOWER_12(12),
        FLOWER_13(13),
        FLOWER_14(14),
        FLOWER_15(15);

        private byte type;
        private static final FlowerType[] types;

        private FlowerType(byte type) {
            this.type = type;
        }

        public byte getType() {
            return this.type;
        }

        public byte getEncodedData() {
            return (byte)(this.type & 0xFF);
        }

        public static FlowerType fromInt(int i) {
            return types[i];
        }

        public static FlowerType decodeTileData(int tileData) {
            return FlowerType.fromInt(tileData & 0xF);
        }

        public String getDescription() {
            return GrassData.getFlowerName(this);
        }

        static {
            types = FlowerType.values();
        }
    }

    public static enum GrassType {
        GRASS(0),
        REED(1),
        KELP(2),
        UNUSED(3);

        private byte type;
        private static final GrassType[] types;

        private GrassType(byte type) {
            this.type = type;
        }

        public byte getType() {
            return this.type;
        }

        public byte getEncodedData() {
            return (byte)(this.type << 4 & 0x30);
        }

        public static GrassType fromInt(int i) {
            return types[i];
        }

        public static GrassType decodeTileData(int tile) {
            return GrassType.fromInt(tile >> 4 & 3);
        }

        public String getName() {
            switch (this) {
                case GRASS: {
                    return "Grass";
                }
                case KELP: {
                    return "Kelp";
                }
                case REED: {
                    return "Reed";
                }
            }
            return "Unknown";
        }

        public int getGrowthRateInSeason(GrowthSeason season) {
            switch (season) {
                case WINTER: {
                    return 15;
                }
                case SUMMER: {
                    return 40;
                }
                case AUTUMN: {
                    return 30;
                }
                case SPRING: {
                    return 20;
                }
            }
            return 5;
        }

        static {
            types = GrassType.values();
        }
    }

    public static enum GrowthTreeStage {
        LAWN(0),
        SHORT(1),
        MEDIUM(2),
        TALL(3);

        private byte code;
        private static final int NUMBER_OF_STAGES;
        private static final GrowthTreeStage[] stages;

        private GrowthTreeStage(byte code) {
            this.code = code;
        }

        public byte getCode() {
            return this.code;
        }

        public byte getEncodedData() {
            return (byte)(this.code & 3);
        }

        public static GrowthTreeStage fromInt(int i) {
            return stages[i];
        }

        public static GrowthTreeStage decodeTileData(int tileData) {
            return GrowthTreeStage.fromInt(tileData & 3);
        }

        public static short getYield(GrowthTreeStage growthStage) {
            short yield;
            switch (growthStage) {
                case SHORT: {
                    yield = 0;
                    break;
                }
                case MEDIUM: {
                    yield = 1;
                    break;
                }
                case TALL: {
                    yield = 2;
                    break;
                }
                default: {
                    yield = 0;
                }
            }
            return yield;
        }

        public GrowthTreeStage getNextStage() {
            int num = this.ordinal();
            num = Math.min(num + 1, NUMBER_OF_STAGES - 1);
            return GrowthTreeStage.fromInt(num);
        }

        public final boolean isMax() {
            return this.ordinal() >= NUMBER_OF_STAGES - 1;
        }

        public GrowthTreeStage getPreviousStage() {
            int num = this.ordinal();
            num = Math.max(num - 1, 1);
            return GrowthTreeStage.fromInt(num);
        }

        static {
            NUMBER_OF_STAGES = GrowthTreeStage.values().length;
            stages = GrowthTreeStage.values();
        }
    }

    public static enum GrowthStage {
        SHORT(0),
        MEDIUM(1),
        TALL(2),
        WILD(3);

        private byte code;
        private static final int NUMBER_OF_STAGES;
        private static final GrowthStage[] stages;

        private GrowthStage(byte code) {
            this.code = code;
        }

        public byte getCode() {
            return this.code;
        }

        public byte getEncodedData() {
            return (byte)(this.code << 6 & 0xC0);
        }

        public static GrowthStage fromInt(int i) {
            return stages[i];
        }

        public static GrowthStage decodeTileData(int tileData) {
            return GrowthStage.fromInt(tileData >> 6 & 3);
        }

        public static GrowthStage decodeTreeData(int tileData) {
            int len = Math.max((tileData & 3) - 1, 0);
            return GrowthStage.fromInt(len);
        }

        public static short getYield(GrowthStage growthStage) {
            short yield;
            switch (growthStage) {
                case SHORT: {
                    yield = 0;
                    break;
                }
                case MEDIUM: {
                    yield = 1;
                    break;
                }
                case TALL: {
                    yield = 2;
                    break;
                }
                case WILD: {
                    yield = 3;
                    break;
                }
                default: {
                    yield = 0;
                }
            }
            return yield;
        }

        public GrowthStage getNextStage() {
            int num = this.ordinal();
            num = Math.min(num + 1, NUMBER_OF_STAGES - 1);
            return GrowthStage.fromInt(num);
        }

        public final boolean isMax() {
            return this.ordinal() >= NUMBER_OF_STAGES - 1;
        }

        public GrowthStage getPreviousStage() {
            int num = this.ordinal();
            num = Math.max(num - 1, 0);
            return GrowthStage.fromInt(num);
        }

        static {
            NUMBER_OF_STAGES = GrowthStage.values().length;
            stages = GrowthStage.values();
        }
    }

    public static enum GrowthSeason {
        WINTER,
        SPRING,
        SUMMER,
        AUTUMN;

    }
}


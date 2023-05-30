package com.wurmonline.mesh;

public final class GrassData {
   private GrassData() {
   }

   private static String getFlowerName(GrassData.FlowerType flowerType) {
      switch(flowerType) {
         case NONE:
            return "";
         case FLOWER_1:
            return "Yellow flowers";
         case FLOWER_2:
            return "Orange-red flowers";
         case FLOWER_3:
            return "Purple flowers";
         case FLOWER_4:
            return "White flowers";
         case FLOWER_5:
            return "Blue flowers";
         case FLOWER_6:
            return "Greenish-yellow flowers";
         case FLOWER_7:
            return "White-dotted flowers";
         default:
            return "Unknown grass";
      }
   }

   public static String getModelResourceName(GrassData.FlowerType flowerType) {
      switch(flowerType) {
         case NONE:
         case FLOWER_1:
         case FLOWER_2:
         case FLOWER_3:
         case FLOWER_4:
         case FLOWER_5:
         case FLOWER_6:
         case FLOWER_7:
         default:
            return "model.flower.unknown";
      }
   }

   public static String getHelpSubject(int type) {
      return "Terrain:" + GrassData.GrassType.values()[type].name().replace(' ', '_');
   }

   public static int getFlowerType(byte data) {
      return GrassData.FlowerType.decodeTileData(data).getType() & 65535;
   }

   public static String getFlowerTypeName(byte data) {
      return getFlowerName(GrassData.FlowerType.decodeTileData(data));
   }

   public static byte encodeGrassTileData(GrassData.GrowthStage growthStage, GrassData.GrassType grassType, GrassData.FlowerType flowerType) {
      return (byte)(growthStage.getEncodedData() | grassType.getEncodedData() | flowerType.getEncodedData());
   }

   public static byte encodeGrassTileData(GrassData.GrowthStage growthStage, GrassData.FlowerType flowerType) {
      return (byte)(growthStage.getEncodedData() | flowerType.getEncodedData());
   }

   public static String getHover(byte data) {
      return GrassData.GrassType.decodeTileData(data).getName();
   }

   public static int getGrowthRateFor(GrassData.GrassType grassType, GrassData.GrowthSeason season) {
      return grassType.getGrowthRateInSeason(season);
   }

   public static enum FlowerType {
      NONE((byte)0),
      FLOWER_1((byte)1),
      FLOWER_2((byte)2),
      FLOWER_3((byte)3),
      FLOWER_4((byte)4),
      FLOWER_5((byte)5),
      FLOWER_6((byte)6),
      FLOWER_7((byte)7),
      FLOWER_8((byte)8),
      FLOWER_9((byte)9),
      FLOWER_10((byte)10),
      FLOWER_11((byte)11),
      FLOWER_12((byte)12),
      FLOWER_13((byte)13),
      FLOWER_14((byte)14),
      FLOWER_15((byte)15);

      private byte type;
      private static final GrassData.FlowerType[] types = values();

      private FlowerType(byte type) {
         this.type = type;
      }

      public byte getType() {
         return this.type;
      }

      public byte getEncodedData() {
         return (byte)(this.type & 255);
      }

      public static GrassData.FlowerType fromInt(int i) {
         return types[i];
      }

      public static GrassData.FlowerType decodeTileData(int tileData) {
         return fromInt(tileData & 15);
      }

      public String getDescription() {
         return GrassData.getFlowerName(this);
      }
   }

   public static enum GrassType {
      GRASS((byte)0),
      REED((byte)1),
      KELP((byte)2),
      UNUSED((byte)3);

      private byte type;
      private static final GrassData.GrassType[] types = values();

      private GrassType(byte type) {
         this.type = type;
      }

      public byte getType() {
         return this.type;
      }

      public byte getEncodedData() {
         return (byte)(this.type << 4 & 48);
      }

      public static GrassData.GrassType fromInt(int i) {
         return types[i];
      }

      public static GrassData.GrassType decodeTileData(int tile) {
         return fromInt(tile >> 4 & 3);
      }

      public String getName() {
         switch(this) {
            case GRASS:
               return "Grass";
            case KELP:
               return "Kelp";
            case REED:
               return "Reed";
            default:
               return "Unknown";
         }
      }

      public int getGrowthRateInSeason(GrassData.GrowthSeason season) {
         switch(season) {
            case WINTER:
               return 15;
            case SUMMER:
               return 40;
            case AUTUMN:
               return 30;
            case SPRING:
               return 20;
            default:
               return 5;
         }
      }
   }

   public static enum GrowthSeason {
      WINTER,
      SPRING,
      SUMMER,
      AUTUMN;
   }

   public static enum GrowthStage {
      SHORT((byte)0),
      MEDIUM((byte)1),
      TALL((byte)2),
      WILD((byte)3);

      private byte code;
      private static final int NUMBER_OF_STAGES = values().length;
      private static final GrassData.GrowthStage[] stages = values();

      private GrowthStage(byte code) {
         this.code = code;
      }

      public byte getCode() {
         return this.code;
      }

      public byte getEncodedData() {
         return (byte)(this.code << 6 & 192);
      }

      public static GrassData.GrowthStage fromInt(int i) {
         return stages[i];
      }

      public static GrassData.GrowthStage decodeTileData(int tileData) {
         return fromInt(tileData >> 6 & 3);
      }

      public static GrassData.GrowthStage decodeTreeData(int tileData) {
         int len = Math.max((tileData & 3) - 1, 0);
         return fromInt(len);
      }

      public static short getYield(GrassData.GrowthStage growthStage) {
         short yield;
         switch(growthStage) {
            case SHORT:
               yield = 0;
               break;
            case MEDIUM:
               yield = 1;
               break;
            case TALL:
               yield = 2;
               break;
            case WILD:
               yield = 3;
               break;
            default:
               yield = 0;
         }

         return yield;
      }

      public GrassData.GrowthStage getNextStage() {
         int num = this.ordinal();
         num = Math.min(num + 1, NUMBER_OF_STAGES - 1);
         return fromInt(num);
      }

      public final boolean isMax() {
         return this.ordinal() >= NUMBER_OF_STAGES - 1;
      }

      public GrassData.GrowthStage getPreviousStage() {
         int num = this.ordinal();
         num = Math.max(num - 1, 0);
         return fromInt(num);
      }
   }

   public static enum GrowthTreeStage {
      LAWN((byte)0),
      SHORT((byte)1),
      MEDIUM((byte)2),
      TALL((byte)3);

      private byte code;
      private static final int NUMBER_OF_STAGES = values().length;
      private static final GrassData.GrowthTreeStage[] stages = values();

      private GrowthTreeStage(byte code) {
         this.code = code;
      }

      public byte getCode() {
         return this.code;
      }

      public byte getEncodedData() {
         return (byte)(this.code & 3);
      }

      public static GrassData.GrowthTreeStage fromInt(int i) {
         return stages[i];
      }

      public static GrassData.GrowthTreeStage decodeTileData(int tileData) {
         return fromInt(tileData & 3);
      }

      public static short getYield(GrassData.GrowthTreeStage growthStage) {
         short yield;
         switch(growthStage) {
            case SHORT:
               yield = 0;
               break;
            case MEDIUM:
               yield = 1;
               break;
            case TALL:
               yield = 2;
               break;
            default:
               yield = 0;
         }

         return yield;
      }

      public GrassData.GrowthTreeStage getNextStage() {
         int num = this.ordinal();
         num = Math.min(num + 1, NUMBER_OF_STAGES - 1);
         return fromInt(num);
      }

      public final boolean isMax() {
         return this.ordinal() >= NUMBER_OF_STAGES - 1;
      }

      public GrassData.GrowthTreeStage getPreviousStage() {
         int num = this.ordinal();
         num = Math.max(num - 1, 1);
         return fromInt(num);
      }
   }
}

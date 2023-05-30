package com.wurmonline.mesh;

public final class FieldData {
   public static final int BARLEY = 0;
   public static final int WHEAT = 1;
   public static final int RYE = 2;
   public static final int OAT = 3;
   public static final int CORN = 4;
   public static final int PUMPKIN = 5;
   public static final int POTATO = 6;
   public static final int COTTON = 7;
   public static final int WEMP = 8;
   public static final int GARLIC = 9;
   public static final int ONION = 10;
   public static final int REED = 11;
   public static final int RICE = 12;
   public static final int STRAWBERRIES = 13;
   public static final int CARROTS = 14;
   public static final int CABBAGE = 15;
   public static final int TOMATOS = 16;
   public static final int SUGAR_BEET = 17;
   public static final int LETTUCE = 18;
   public static final int PEAPODS = 19;
   public static final int CUCUMBER = 20;
   private static final String[] fieldAges = new String[]{"freshly sown", "sprouting", "growing", "halfway", "almost ripe", "ripe", "ripe", "only weeds"};

   private FieldData() {
   }

   public static String getFieldName(int fieldType) {
      switch(fieldType) {
         case 0:
            return "Barley";
         case 1:
            return "Wheat";
         case 2:
            return "Rye";
         case 3:
            return "Oat";
         case 4:
            return "Corn";
         case 5:
            return "Pumpkin";
         case 6:
            return "Potato";
         case 7:
            return "Cotton";
         case 8:
            return "Wemp";
         case 9:
            return "Garlic";
         case 10:
            return "Onion";
         case 11:
            return "Reed";
         case 12:
            return "Rice";
         case 13:
            return "Strawberries";
         case 14:
            return "Carrots";
         case 15:
            return "Cabbage";
         case 16:
            return "Tomatoes";
         case 17:
            return "Sugar beet";
         case 18:
            return "Lettuce";
         case 19:
            return "Pea pods";
         case 20:
            return "Cucumber";
         default:
            return "Unknown crop";
      }
   }

   public static String getModelResourceName(int fieldType) {
      return "img.texture.crop." + getFieldName(fieldType).toLowerCase().replace(" ", "");
   }

   public static String getHelpSubject(int fieldType) {
      return "Terrain:" + getFieldName(fieldType).replace(' ', '_');
   }

   public static int getType(Tiles.Tile fieldType, byte data) {
      return fieldType == Tiles.Tile.TILE_FIELD ? data & 15 : 16 + (data & 15);
   }

   public static int getAge(byte data) {
      return (data & 112) >> 4;
   }

   public static boolean isTended(byte data) {
      return (data & 128) != 0;
   }

   public static String getTypeName(Tiles.Tile fieldType, byte data) {
      return getFieldName(getType(fieldType, data));
   }

   public static String getAgeName(byte data) {
      return fieldAges[getAge(data)];
   }
}

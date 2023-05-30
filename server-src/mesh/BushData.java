package com.wurmonline.mesh;

import com.wurmonline.shared.util.StringUtilities;

public final class BushData {
   private BushData() {
   }

   public static boolean isBush(int treeId) {
      return true;
   }

   public static boolean isTree(int treeId) {
      return false;
   }

   public static String getHelpSubject(byte type, boolean infected) {
      return "Terrain:" + getTypeName(type).replace(' ', '_');
   }

   public static boolean isValidBush(int treeId) {
      return treeId < BushData.BushType.getLength();
   }

   public static int getType(byte data) {
      return data & 15;
   }

   public static String getTypeName(byte data) {
      return BushData.BushType.fromTileData(getType(data)).getName();
   }

   public static enum BushType {
      LAVENDER(0, (byte)46, 4, 142, 148, 154, 1.0F, 1.0F, 0.0F, "model.bush.lavendel", 0, 0, true),
      ROSE(1, (byte)47, 5, 143, 149, 155, 2.0F, 1.0F, 0.0F, "model.bush.rose", 1, 0, true),
      THORN(2, (byte)48, 15, 144, 150, 156, 0.5F, 0.5F, 0.0F, "model.bush.thorn", 2, 0, false),
      GRAPE(3, (byte)49, 5, 145, 151, 157, 1.4F, 1.2F, 0.0F, "model.bush.grape", 3, 0, true),
      CAMELLIA(4, (byte)50, 3, 146, 152, 158, 1.6F, 1.25F, 0.0F, "model.bush.camellia", 0, 1, true),
      OLEANDER(5, (byte)51, 2, 147, 153, 159, 1.55F, 1.45F, 0.0F, "model.bush.oleander", 1, 1, true),
      HAZELNUT(6, (byte)71, 2, 160, 161, 162, 1.7F, 1.32F, 0.0F, "model.bush.hazelnut", 2, 1, true),
      RASPBERRY(7, (byte)90, 2, 166, 167, 168, 1.7F, 1.32F, 0.0F, "model.bush.raspberry", 3, 1, true),
      BLUEBERRY(8, (byte)91, 2, 169, 170, 171, 1.7F, 1.32F, 0.0F, "model.bush.blueberry", 0, 2, true),
      LINGONBERRY(9, (byte)92, 2, 172, 172, 172, 1.7F, 1.32F, 0.0F, "model.bush.lingonberry", 1, 2, true);

      private final int typeId;
      private final byte materialId;
      private final int woodDifficulty;
      private final byte normalBush;
      private final byte myceliumBush;
      private final byte enchantedBush;
      private final float width;
      private final float height;
      private final float radius;
      private final String modelName;
      private final int posX;
      private final int posY;
      private final boolean canBearFruit;
      private static final BushData.BushType[] types = values();

      private BushType(
         int id,
         byte material,
         int woodDifficulty,
         int normalBush,
         int myceliumBush,
         int enchantedBush,
         float width,
         float height,
         float radius,
         String modelName,
         int posX,
         int posY,
         boolean canBearFruit
      ) {
         this.typeId = id;
         this.materialId = material;
         this.woodDifficulty = woodDifficulty;
         this.normalBush = (byte)normalBush;
         this.myceliumBush = (byte)myceliumBush;
         this.enchantedBush = (byte)enchantedBush;
         this.width = width;
         this.height = height;
         this.radius = radius;
         this.modelName = modelName;
         this.posX = posX;
         this.posY = posY;
         this.canBearFruit = canBearFruit;
      }

      public int getTypeId() {
         return this.typeId;
      }

      public String getName() {
         String name = fromInt(this.typeId).toString() + " bush";
         return StringUtilities.raiseFirstLetter(name);
      }

      public byte getMaterial() {
         return this.materialId;
      }

      public byte asNormalBush() {
         return this.normalBush;
      }

      public byte asMyceliumBush() {
         return this.myceliumBush;
      }

      public byte asEnchantedBush() {
         return this.enchantedBush;
      }

      public int getDifficulty() {
         return this.woodDifficulty;
      }

      public float getWidth() {
         return this.width;
      }

      public float getHeight() {
         return this.height;
      }

      public float getRadius() {
         return this.radius;
      }

      String getModelName() {
         return this.modelName;
      }

      public String getModelResourceName(int treeAge) {
         if (treeAge < 4) {
            return this.getModelName() + ".young";
         } else {
            return treeAge == 15 ? this.getModelName() + ".shrivelled" : this.getModelName();
         }
      }

      public int getTexturPosX() {
         return this.posX;
      }

      public int getTexturPosY() {
         return this.posY;
      }

      public boolean canBearFruit() {
         return this.canBearFruit;
      }

      public static final int getLength() {
         return types.length;
      }

      public static BushData.BushType fromTileData(int tileData) {
         return fromInt(tileData & 15);
      }

      public static BushData.BushType fromInt(int i) {
         return i >= getLength() ? types[0] : types[i & 0xFF];
      }

      public static BushData.BushType decodeTileData(int tileData) {
         return fromInt(tileData & 15);
      }

      public static int encodeTileData(int tage, int ttype) {
         ttype = Math.min(ttype, types.length - 1);
         ttype = Math.max(ttype, 0);
         return (tage & 15) << 4 | ttype & 15;
      }
   }
}

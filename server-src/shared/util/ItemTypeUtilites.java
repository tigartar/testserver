package com.wurmonline.shared.util;

public final class ItemTypeUtilites {
   public static final int ITEMTYPE_ITEM = 0;
   public static final int ITEMTYPE_WOUND = 1;
   public static final int ITEMTYPE_BODYPART = 2;
   public static final int ITEMTYPE_CONTAINER = 3;
   public static final int ITEMTYPE_NODROP = 4;
   public static final int ITEMTYPE_IS_TWO_HANDER = 5;
   public static final int ITEMTYPE_INVENTORY_GROUP = 6;
   public static final int ITEMTYPE_SHOWSLOPES = 7;
   public static final int ITEMTYPE_TOOLBELT_IGNORE_CONTENTS = 8;

   private ItemTypeUtilites() {
   }

   public static short calcProfile(
      boolean wound,
      boolean bodypart,
      boolean container,
      boolean nodrop,
      boolean twoHanded,
      boolean inventoryGroup,
      boolean showSlopes,
      boolean toolbeltIgnoreContents
   ) {
      short toReturn = 0;
      if (wound) {
         toReturn = (short)(toReturn + 2);
      }

      if (bodypart) {
         toReturn = (short)(toReturn + 4);
      }

      if (container) {
         toReturn = (short)(toReturn + 8);
      }

      if (nodrop) {
         toReturn = (short)(toReturn + 16);
      }

      if (twoHanded) {
         toReturn = (short)(toReturn + 32);
      }

      if (inventoryGroup) {
         toReturn = (short)(toReturn + 64);
      }

      if (showSlopes) {
         toReturn = (short)(toReturn + 128);
      }

      if (toolbeltIgnoreContents) {
         toReturn = (short)(toReturn + 256);
      }

      return toReturn;
   }

   public static boolean isWound(short profile) {
      return (profile >> 1 & 1) == 1;
   }

   public static boolean isBodypart(short profile) {
      return (profile >> 2 & 1) == 1;
   }

   public static boolean isContainer(short profile) {
      return (profile >> 3 & 1) == 1;
   }

   public static boolean isNodrop(short profile) {
      return (profile >> 4 & 1) == 1;
   }

   public static boolean isTwoHanded(short profile) {
      return (profile >> 5 & 1) == 1;
   }

   public static boolean isInventoryGroup(short profile) {
      return (profile >> 6 & 1) == 1;
   }

   public static boolean doesShowSlopes(short profile) {
      return (profile >> 7 & 1) == 1;
   }

   public static boolean toolbeltIgnoreContents(short profile) {
      return (profile >> 8 & 1) == 1;
   }
}

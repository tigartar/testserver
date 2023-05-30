package com.wurmonline.server.economy;

import com.wurmonline.server.MiscConstants;

public final class Change implements MonetaryConstants, MiscConstants {
   public static final String NOTHING = "0 irons";
   public long ironCoins;
   public long goldCoins;
   public long silverCoins;
   public long copperCoins;
   private static final String AND_SEPARATOR = " and ";
   private static final String COMMA_SEPARATOR = ", ";
   private static final String IRON_STRING = " iron";
   private static final String IRON_SHORT_STRING = "i";
   private static final String COPPER_STRING = " copper";
   private static final String COPPER_SHORT_STRING = "c";
   private static final String SILVER_STRING = " silver";
   private static final String SILVER_SHORT_STRING = "s";
   private static final String GOLD_STRING = " gold";
   private static final String GOLD_SHORT_STRING = "g";

   public Change(long ironValue) {
      this.goldCoins = ironValue / 1000000L;
      long rest = ironValue % 1000000L;
      this.silverCoins = rest / 10000L;
      rest = ironValue % 10000L;
      this.copperCoins = rest / 100L;
      rest = ironValue % 100L;
      this.ironCoins = rest;
   }

   public long getGoldCoins() {
      return this.goldCoins;
   }

   public long getSilverCoins() {
      return this.silverCoins;
   }

   public long getCopperCoins() {
      return this.copperCoins;
   }

   public long getIronCoins() {
      return this.ironCoins;
   }

   public String getChangeString() {
      String toSend = "";
      if (this.goldCoins > 0L) {
         toSend = toSend + this.goldCoins + " gold";
      }

      if (this.silverCoins > 0L) {
         if (this.goldCoins > 0L) {
            if (this.copperCoins <= 0L && this.ironCoins <= 0L) {
               toSend = toSend + " and ";
            } else {
               toSend = toSend + ", ";
            }
         }

         toSend = toSend + this.silverCoins + " silver";
      }

      if (this.copperCoins > 0L) {
         if (this.silverCoins > 0L || this.goldCoins > 0L) {
            if (this.ironCoins > 0L) {
               toSend = toSend + ", ";
            } else {
               toSend = toSend + " and ";
            }
         }

         toSend = toSend + this.copperCoins + " copper";
      }

      if (this.ironCoins > 0L) {
         if (this.silverCoins > 0L || this.goldCoins > 0L || this.copperCoins > 0L) {
            toSend = toSend + " and ";
         }

         toSend = toSend + this.ironCoins + " iron";
      }

      return toSend.length() == 0 ? "0 irons" : toSend;
   }

   public String getChangeShortString() {
      StringBuilder toSend = new StringBuilder();
      if (this.goldCoins > 0L) {
         toSend.append(this.goldCoins).append("g");
      }

      if (this.silverCoins > 0L) {
         if (this.goldCoins > 0L) {
            toSend.append(", ");
         }

         toSend.append(this.silverCoins).append("s");
      }

      if (this.copperCoins > 0L) {
         if (this.silverCoins > 0L || this.goldCoins > 0L) {
            toSend.append(", ");
         }

         toSend.append(this.copperCoins).append("c");
      }

      if (this.ironCoins > 0L) {
         if (this.silverCoins > 0L || this.goldCoins > 0L || this.copperCoins > 0L) {
            toSend.append(", ");
         }

         toSend.append(this.ironCoins).append("i");
      }

      return toSend.toString();
   }
}

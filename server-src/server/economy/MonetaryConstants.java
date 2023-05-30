package com.wurmonline.server.economy;

public interface MonetaryConstants {
   int COIN_IRON = 1;
   int COIN_COPPER = 100;
   int COIN_SILVER = 10000;
   int COIN_GOLD = 1000000;
   int MAX_DISCARDMONEY_HOUR = 500;

   public static enum TransactionReason {
      Banked,
      Charged,
      Destroyed,
      Notrade,
      PersonalShop,
      Sacrificed,
      TraderShop;
   }
}

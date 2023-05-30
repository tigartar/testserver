package com.wurmonline.server.spells;

public class Excel extends CreatureEnchantment {
   public static final int RANGE = 4;

   public Excel() {
      super("Excel", 442, 20, 20, 30, 35, 0L);
      this.enchantment = 28;
      this.effectdesc = "increased defensive combat capabilities.";
      this.description = "increases defensive combat rating";
      this.type = 1;
   }
}

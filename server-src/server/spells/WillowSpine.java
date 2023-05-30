package com.wurmonline.server.spells;

public class WillowSpine extends CreatureEnchantment {
   public static final int RANGE = 4;

   public WillowSpine() {
      super("Willowspine", 405, 10, 20, 29, 35, 30000L);
      this.enchantment = 23;
      this.effectdesc = "increased chance to dodge.";
      this.description = "increases chance to dodge attacks";
      this.type = 2;
   }
}

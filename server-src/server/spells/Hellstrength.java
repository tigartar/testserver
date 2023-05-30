package com.wurmonline.server.spells;

public class Hellstrength extends CreatureEnchantment {
   public static final int RANGE = 4;

   public Hellstrength() {
      super("Hell Strength", 427, 10, 60, 40, 45, 30000L);
      this.enchantment = 40;
      this.effectdesc = "increased body strength and soul strength.";
      this.description = "increases body strength and soul strength";
      this.type = 2;
   }
}

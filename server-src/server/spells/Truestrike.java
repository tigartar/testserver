package com.wurmonline.server.spells;

public class Truestrike extends KarmaEnchantment {
   public static final int RANGE = 24;

   public Truestrike() {
      super("True Strike", 555, 5, 500, 20, 1, 180000L);
      this.targetCreature = true;
      this.enchantment = 67;
      this.effectdesc = "one critical hit coming up.";
      this.description = "your next hit will be a critical strike";
      this.durationModifier = 3.0F;
   }
}

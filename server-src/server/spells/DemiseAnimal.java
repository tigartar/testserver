package com.wurmonline.server.spells;

import com.wurmonline.server.creatures.Creature;
import com.wurmonline.server.items.Item;
import com.wurmonline.server.skills.Skill;

public final class DemiseAnimal extends ReligiousSpell {
   public static final int RANGE = 4;

   DemiseAnimal() {
      super("Animal Demise", 269, 30, 40, 50, 43, 0L);
      this.targetWeapon = true;
      this.enchantment = 11;
      this.effectdesc = "will deal increased damage to animals.";
      this.description = "increases damage dealt to animals";
      this.singleItemEnchant = true;
      this.type = 1;
   }

   @Override
   boolean precondition(Skill castSkill, Creature performer, Item target) {
      return EnchantUtil.canEnchantDemise(performer, target);
   }

   @Override
   boolean precondition(Skill castSkill, Creature performer, Creature target) {
      return false;
   }

   @Override
   void doEffect(Skill castSkill, double power, Creature performer, Item target) {
      target.enchant((byte)11);
      performer.getCommunicator().sendNormalServerMessage("The " + target.getName() + " will now be effective against animals.", (byte)2);
   }
}

package com.wurmonline.server.spells;

import com.wurmonline.server.creatures.Creature;
import com.wurmonline.server.items.Item;
import com.wurmonline.server.skills.Skill;

public class ItemEnchantment extends ReligiousSpell {
   public static final int RANGE = 4;

   ItemEnchantment(String aName, int aNum, int aCastingTime, int aCost, int aDifficulty, int aLevel, long aCooldown) {
      super(aName, aNum, aCastingTime, aCost, aDifficulty, aLevel, aCooldown);
   }

   @Override
   boolean precondition(Skill castSkill, Creature performer, Item target) {
      if (!mayBeEnchanted(target)) {
         EnchantUtil.sendCannotBeEnchantedMessage(performer);
         return false;
      } else {
         SpellEffect negatingEffect = EnchantUtil.hasNegatingEffect(target, this.getEnchantment());
         if (negatingEffect != null) {
            EnchantUtil.sendNegatingEffectMessage(this.getName(), performer, target, negatingEffect);
            return false;
         } else {
            return true;
         }
      }
   }

   @Override
   boolean precondition(Skill castSkill, Creature performer, Creature target) {
      return false;
   }

   @Override
   void doEffect(Skill castSkill, double power, Creature performer, Item target) {
      this.enchantItem(performer, target, this.getEnchantment(), (float)power);
   }

   @Override
   void doNegativeEffect(Skill castSkill, double power, Creature performer, Item target) {
      this.checkDestroyItem(power, performer, target);
   }
}

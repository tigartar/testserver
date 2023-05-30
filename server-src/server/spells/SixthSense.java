package com.wurmonline.server.spells;

import com.wurmonline.server.creatures.Creature;
import com.wurmonline.server.skills.Skill;

public class SixthSense extends CreatureEnchantment {
   public static final int RANGE = 4;

   SixthSense() {
      super("Sixth Sense", 376, 10, 15, 20, 6, 0L);
      this.targetCreature = true;
      this.enchantment = 21;
      this.effectdesc = "detect hidden dangers.";
      this.description = "detect hidden creatures and traps";
      this.type = 0;
   }

   @Override
   boolean precondition(Skill castSkill, Creature performer, Creature target) {
      if (!target.isPlayer()) {
         performer.getCommunicator().sendNormalServerMessage("You can only cast that on a person.");
         return false;
      } else if (target.isReborn()) {
         return false;
      } else if (!target.equals(performer)) {
         if (performer.getDeity() != null) {
            if (target.getDeity() != null) {
               return target.getDeity().isHateGod() ? performer.isFaithful() : true;
            } else {
               return true;
            }
         } else {
            return true;
         }
      } else {
         return true;
      }
   }
}

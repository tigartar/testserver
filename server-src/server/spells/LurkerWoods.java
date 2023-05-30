package com.wurmonline.server.spells;

import com.wurmonline.server.creatures.Creature;
import com.wurmonline.server.items.Item;
import com.wurmonline.server.skills.Skill;

public class LurkerWoods extends ItemEnchantment {
   public static final int RANGE = 4;

   public LurkerWoods() {
      super("Lurker in the Woods", 458, 20, 30, 60, 31, 0L);
      this.targetPendulum = true;
      this.enchantment = 49;
      this.effectdesc = "will locate rare creatures.";
      this.description = "locates rare creatures";
   }

   @Override
   boolean precondition(Skill castSkill, Creature performer, Item target) {
      if (target.getTemplateId() != 233) {
         performer.getCommunicator().sendNormalServerMessage("This would work well on a pendulum.", (byte)3);
         return false;
      } else {
         return mayBeEnchanted(target);
      }
   }
}

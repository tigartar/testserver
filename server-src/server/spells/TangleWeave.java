package com.wurmonline.server.spells;

import com.wurmonline.server.behaviours.Action;
import com.wurmonline.server.behaviours.NoSuchActionException;
import com.wurmonline.server.creatures.Creature;
import com.wurmonline.server.skills.Skill;

public class TangleWeave extends CreatureEnchantment {
   public static final int RANGE = 50;

   public TangleWeave() {
      super("Tangleweave", 641, 3, 15, 30, 10, 30000L);
      this.enchantment = 93;
      this.offensive = true;
      this.effectdesc = "interrupts and slow casting.";
      this.description = "interrupts an enemy spell caster and slows future spells";
      this.durationModifier = 0.5F;
      this.type = 0;
   }

   @Override
   public boolean precondition(Skill castSkill, Creature performer, Creature target) {
      return true;
   }

   @Override
   void doEffect(Skill castSkill, double power, Creature performer, Creature target) {
      super.doEffect(castSkill, power, performer, target);

      try {
         Action act = target.getCurrentAction();
         if (act.isSpell()) {
            performer.getCommunicator().sendCombatNormalMessage(String.format("You interrupt %s from %s.", target.getName(), act.getActionString()));
            String toSend = target.getActions().stopCurrentAction(false);
            if (toSend.length() > 0) {
               target.getCommunicator().sendNormalServerMessage(toSend);
            }

            target.sendActionControl("", false, 0);
            return;
         }
      } catch (NoSuchActionException var8) {
      }

      performer.getCommunicator().sendCombatNormalMessage("You failed to interrupt " + target.getName() + ".");
   }
}

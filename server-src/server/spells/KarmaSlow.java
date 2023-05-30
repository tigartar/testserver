package com.wurmonline.server.spells;

import com.wurmonline.server.Server;
import com.wurmonline.server.creatures.Creature;
import com.wurmonline.server.creatures.SpellEffects;
import com.wurmonline.server.skills.Skill;

public class KarmaSlow extends KarmaEnchantment {
   public static final int RANGE = 24;

   public KarmaSlow() {
      super("Karma Slow", 554, 10, 500, 20, 1, 120000L);
      this.targetCreature = true;
      this.offensive = true;
      this.enchantment = 66;
      this.effectdesc = "slower attack speed.";
      this.description = "slows down attacks";
      this.durationModifier = 1.0F;
   }

   @Override
   void doEffect(Skill castSkill, double power, Creature performer, Creature target) {
      SpellEffects effs = target.getSpellEffects();
      if (effs == null) {
         effs = target.createSpellEffects();
      }

      SpellEffect eff = effs.getSpellEffect(this.enchantment);
      int duration = (int)(30.0 + 270.0 * (power / 100.0));
      if (eff == null) {
         if (target != performer) {
            performer.getCommunicator().sendNormalServerMessage(target.getName() + " now has " + this.effectdesc, (byte)2);
         }

         target.getCommunicator().sendNormalServerMessage("You now have " + this.effectdesc, (byte)2);
         eff = new SpellEffect(target.getWurmId(), this.enchantment, (float)power, duration, (byte)9, (byte)1, true);
         effs.addSpellEffect(eff);
         Server.getInstance().broadCastAction(performer.getName() + " looks pleased.", performer, 5);
      } else if ((double)eff.getPower() > power) {
         performer.getCommunicator().sendNormalServerMessage("You frown as you fail to improve the power.", (byte)3);
         Server.getInstance().broadCastAction(performer.getName() + " frowns.", performer, 5);
      } else {
         if (target != performer) {
            performer.getCommunicator().sendNormalServerMessage("You succeed in improving the power of the " + this.name + ".", (byte)2);
         }

         target.getCommunicator().sendNormalServerMessage("You will now receive improved " + this.effectdesc, (byte)2);
         eff.setPower((float)power);
         eff.setTimeleft(Math.max(eff.timeleft, duration));
         target.sendUpdateSpellEffect(eff);
         Server.getInstance().broadCastAction(performer.getName() + " looks pleased.", performer, 5);
      }
   }
}

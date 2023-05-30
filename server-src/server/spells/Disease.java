package com.wurmonline.server.spells;

import com.wurmonline.server.Servers;
import com.wurmonline.server.creatures.Creature;
import com.wurmonline.server.skills.Skill;
import com.wurmonline.server.zones.VolaTile;
import com.wurmonline.server.zones.Zones;

public class Disease extends KarmaSpell {
   public static final int RANGE = 24;

   public Disease() {
      super("Disease", 547, 10, 1000, 10, 10, 300000L);
      this.targetCreature = true;
      this.offensive = true;
      this.description = "diseases creatures and players";
   }

   @Override
   boolean precondition(Skill castSkill, Creature performer, Creature target) {
      if ((target.isHuman() || target.isDominated())
         && target.getAttitude(performer) != 2
         && !performer.getDeity().isLibila()
         && performer.faithful
         && !performer.isDuelOrSpar(target)) {
         performer.getCommunicator()
            .sendNormalServerMessage(performer.getDeity().getName() + " would never accept your attack on " + target.getNameWithGenus() + ".", (byte)3);
         return false;
      } else {
         return true;
      }
   }

   @Override
   void doEffect(Skill castSkill, double power, Creature performer, Creature target) {
      int baseDamage = 12000;
      int damage = (int)(12000.0 + 8000.0 * (power / 100.0));
      int diseaseGained = (int)Math.max(power / 2.0, 10.0);
      if (target.isUnique() && !(power > 99.0)) {
         performer.getCommunicator().sendNormalServerMessage("You try to disease " + target.getNameWithGenus() + " but fail.", (byte)3);
         target.getCommunicator().sendNormalServerMessage(performer.getNameWithGenus() + " tries to disease you but fails.", (byte)4);
      } else {
         if ((target.isHuman() || target.isDominated())
            && target.getAttitude(performer) != 2
            && !performer.getDeity().isLibila()
            && !performer.isDuelOrSpar(target)) {
            performer.modifyFaith(-(100.0F - performer.getFaith()) / 50.0F);
         }

         target.setDisease((byte)Math.min(90, target.getDisease() + diseaseGained));
         target.getCommunicator().sendNormalServerMessage(performer.getNameWithGenus() + " diseases you.", (byte)4);
         if (target.isPlayer()) {
            double defensiveRoll = target.getSoulStrength().skillCheck(power, 0.0, false, 10.0F);
            if (defensiveRoll <= 50.0) {
               if (defensiveRoll > 0.0) {
                  damage = (int)((float)damage * 0.5F);
               }

               if (performer.getPower() > 1 && Servers.isThisATestServer()) {
                  performer.getCommunicator().sendNormalServerMessage("Damage done: " + damage, (byte)2);
               }

               target.addAttacker(performer);
               target.addWoundOfType(performer, (byte)6, 1, true, 1.0F, false, (double)damage, (float)diseaseGained / 2.5F, 0.0F, false, true);
            } else {
               String tMessage = "You resisted the '" + this.name + "' spell.";
               String pMessage = target.getNameWithGenus() + " resisted your '" + this.name + "' spell.";
               target.getCommunicator().sendCombatNormalMessage(tMessage, (byte)4);
               performer.getCommunicator().sendCombatNormalMessage(pMessage, (byte)4);
            }
         }

         VolaTile targetVolaTile = Zones.getTileOrNull(target.getTileX(), target.getTileY(), target.isOnSurface());
         if (targetVolaTile != null) {
            targetVolaTile.sendAttachCreatureEffect(target, (byte)12, (byte)0, (byte)0, (byte)0, (byte)0);
         }
      }
   }

   @Override
   void doNegativeEffect(Skill castSkill, double power, Creature performer, Creature target) {
      performer.getCommunicator().sendNormalServerMessage("You try to cast karma disease on " + target.getNameWithGenus() + " but fail.", (byte)3);
      target.getCommunicator().sendNormalServerMessage(performer.getNameWithGenus() + " tries to cast karma disease on you but fails.", (byte)4);
   }
}

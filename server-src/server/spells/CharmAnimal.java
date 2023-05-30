package com.wurmonline.server.spells;

import com.wurmonline.server.HistoryManager;
import com.wurmonline.server.creatures.Creature;
import com.wurmonline.server.creatures.DbCreatureStatus;
import com.wurmonline.server.skills.Skill;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

public final class CharmAnimal extends ReligiousSpell {
   private static final Logger logger = Logger.getLogger(CharmAnimal.class.getName());
   public static final int RANGE = 6;

   CharmAnimal() {
      super("Charm Animal", 275, 20, 40, 35, 30, 0L);
      this.targetCreature = true;
      this.offensive = true;
      this.description = "makes a tamable creature become your loyal companion";
   }

   @Override
   boolean precondition(Skill castSkill, Creature performer, Creature target) {
      if (performer.getPet() != null && DbCreatureStatus.getIsLoaded(performer.getPet().getWurmId()) == 1) {
         performer.getCommunicator().sendNormalServerMessage("You have a pet in a cage, remove it first, to charm this one.", (byte)3);
         return false;
      } else if (target.isDominatable(performer) && target.isAnimal() && !target.isUnique() && !target.isReborn()) {
         return true;
      } else {
         performer.getCommunicator().sendNormalServerMessage("You fail to connect with the " + target.getName() + ".", (byte)3);
         return false;
      }
   }

   @Override
   void doEffect(Skill castSkill, double power, Creature performer, Creature target) {
      if (power > 0.0) {
         if (performer.getPet() != null && performer.getPet() != target) {
            performer.getCommunicator().sendNormalServerMessage(performer.getPet().getNameWithGenus() + " stops following you.", (byte)2);
            if (performer.getPet().getLeader() == performer) {
               performer.getPet().setLeader(null);
            }

            performer.getPet().setDominator(-10L);
            performer.setPet(-10L);
         }

         boolean newpet = false;
         if (target.dominator != performer.getWurmId()) {
            newpet = true;
         }

         target.setTarget(-10L, true);
         target.stopFighting();
         if (performer.getTarget() == target) {
            performer.stopFighting();
            performer.setTarget(-10L, true);
         }

         if (target.opponent == performer) {
            target.setOpponent(null);
         }

         if (performer.opponent == target) {
            performer.setOpponent(null);
         }

         try {
            target.setKingdomId(performer.getKingdomId());
         } catch (IOException var8) {
            logger.log(Level.WARNING, var8.getMessage(), (Throwable)var8);
         }

         target.setDominator(performer.getWurmId());
         if (newpet) {
            target.setLoyalty((float)Math.max(10.0, power));
            performer.setPet(target.getWurmId());
            target.getCommunicator().sendNormalServerMessage(performer.getNameWithGenus() + " charms you!");
            performer.getCommunicator().sendNormalServerMessage("You overwhelm " + target.getNameWithGenus() + " with love and trust.", (byte)2);
            if (target.isUnique()) {
               HistoryManager.addHistory(performer.getName(), "charms " + target.getName());
            }
         } else {
            target.setLoyalty((float)Math.min(99.0, (double)target.getLoyalty() + power / 10.0));
            target.getCommunicator().sendNormalServerMessage(performer.getNameWithGenus() + " charms you!");
            performer.getCommunicator().sendNormalServerMessage("You strengthen " + target.getNameWithGenus() + "'s love in you.", (byte)2);
         }

         target.getStatus().setLastPolledLoyalty();
      } else {
         performer.getCommunicator().sendNormalServerMessage("You fail to convey your love and trust to " + target.getNameWithGenus() + ".", (byte)3);
      }
   }
}

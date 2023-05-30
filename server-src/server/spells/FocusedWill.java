package com.wurmonline.server.spells;

import com.wurmonline.server.bodys.Wound;
import com.wurmonline.server.bodys.Wounds;
import com.wurmonline.server.creatures.Creature;
import com.wurmonline.server.skills.Skill;
import com.wurmonline.server.zones.VolaTile;
import com.wurmonline.server.zones.Zones;
import java.util.logging.Level;
import java.util.logging.Logger;

public final class FocusedWill extends ReligiousSpell {
   private static final Logger logger = Logger.getLogger(FocusedWill.class.getName());
   public static final int RANGE = 12;

   FocusedWill() {
      super("Focused Will", 929, 10, 10, 5, 31, 0L);
      this.targetWound = true;
      this.targetCreature = true;
      this.healing = true;
      this.description = "heals a small amount of damage on a single wound in exchange for your stamina";
      this.type = 0;
   }

   @Override
   boolean precondition(Skill castSkill, Creature performer, Wound target) {
      if (target.getCreature() == null) {
         performer.getCommunicator().sendNormalServerMessage("You cannot heal that wound.", (byte)3);
         return false;
      } else {
         Creature tCret = target.getCreature();
         if (tCret.isReborn()) {
            performer.getCommunicator().sendNormalServerMessage("You cannot heal the undead.", (byte)3);
            return false;
         } else if (!tCret.isPlayer() || target.getCreature() == performer) {
            return true;
         } else if (tCret.isFriendlyKingdom(performer.getKingdomId())) {
            return true;
         } else if (performer.faithful) {
            performer.getCommunicator().sendNormalServerMessage(performer.getDeity().getName() + " would never accept that.", (byte)3);
            return false;
         } else {
            return true;
         }
      }
   }

   @Override
   boolean precondition(Skill castSkill, Creature performer, Creature target) {
      Wounds tWounds = target.getBody().getWounds();
      if (tWounds != null && tWounds.getWounds().length > 0) {
         return this.precondition(castSkill, performer, tWounds.getWounds()[0]);
      } else {
         performer.getCommunicator().sendNormalServerMessage(target.getName() + " has no wounds to heal.");
         return false;
      }
   }

   @Override
   void doEffect(Skill castSkill, double power, Creature performer, Creature target) {
      Wounds tWounds = target.getBody().getWounds();
      if (tWounds != null && tWounds.getWounds().length > 0) {
         Wound highestWound = tWounds.getWounds()[0];
         float highestSeverity = highestWound.getSeverity();

         for(Wound w : tWounds.getWounds()) {
            if (w.getSeverity() > highestSeverity) {
               highestWound = w;
               highestSeverity = w.getSeverity();
            }
         }

         this.doEffect(castSkill, power, performer, highestWound);
      }
   }

   @Override
   void doEffect(Skill castSkill, double power, Creature performer, Wound target) {
      Creature tCret = target.getCreature();
      if (tCret.isPlayer()
         && target.getCreature() != performer
         && performer.getDeity() != null
         && tCret.getDeity() != null
         && !target.getCreature().isFriendlyKingdom(performer.getKingdomId())) {
         performer.getCommunicator()
            .sendNormalServerMessage(
               performer.getDeity().getName() + " becomes very upset at the way you abuse " + performer.getDeity().getHisHerItsString() + " powers!", (byte)3
            );

         try {
            performer.setFaith(performer.getFaith() / 2.0F);
         } catch (Exception var12) {
            logger.log(Level.WARNING, var12.getMessage(), (Throwable)var12);
         }
      }

      double resistance = SpellResist.getSpellResistance(tCret, this.getNumber());
      double toHeal = 3275.0;
      toHeal += 9825.0 * (power / 100.0);
      if (performer.getCultist() != null && performer.getCultist().healsFaster()) {
         toHeal *= 2.0;
      }

      toHeal *= resistance;
      VolaTile t = Zones.getTileOrNull(target.getCreature().getTileX(), target.getCreature().getTileY(), target.getCreature().isOnSurface());
      if (t != null) {
         t.sendAttachCreatureEffect(target.getCreature(), (byte)11, (byte)0, (byte)0, (byte)0, (byte)0);
      }

      if ((double)target.getSeverity() <= toHeal) {
         SpellResist.addSpellResistance(tCret, this.getNumber(), (double)target.getSeverity());
         performer.getStatus().modifyStamina(-target.getSeverity());
         target.heal();
         performer.getCommunicator().sendNormalServerMessage("You manage to heal the wound.", (byte)2);
         if (performer != tCret) {
            tCret.getCommunicator().sendNormalServerMessage(performer.getName() + " completely heals your wound.", (byte)2);
         }
      } else {
         SpellResist.addSpellResistance(tCret, this.getNumber(), toHeal);
         performer.getStatus().modifyStamina((float)(-toHeal));
         target.modifySeverity((int)(-toHeal));
         performer.getCommunicator().sendNormalServerMessage("You cure the wound a bit.", (byte)2);
         if (performer != tCret) {
            tCret.getCommunicator().sendNormalServerMessage(performer.getName() + " partially heals your wound.", (byte)2);
         }
      }
   }
}

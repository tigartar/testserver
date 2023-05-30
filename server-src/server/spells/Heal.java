package com.wurmonline.server.spells;

import com.wurmonline.server.Server;
import com.wurmonline.server.bodys.Wound;
import com.wurmonline.server.bodys.Wounds;
import com.wurmonline.server.creatures.Creature;
import com.wurmonline.server.skills.Skill;
import com.wurmonline.server.zones.VolaTile;
import com.wurmonline.server.zones.Zones;
import java.util.logging.Level;
import java.util.logging.Logger;

public final class Heal extends ReligiousSpell {
   private static Logger logger = Logger.getLogger(Heal.class.getName());
   public static final int RANGE = 12;

   Heal() {
      super("Heal", 249, 30, 40, 30, 40, 10000L);
      this.targetCreature = true;
      this.healing = true;
      this.description = "heals an extreme amount of damage";
      this.type = 0;
   }

   @Override
   boolean precondition(Skill castSkill, Creature performer, Creature target) {
      if (target.getBody() == null || target.getBody().getWounds() == null) {
         performer.getCommunicator().sendNormalServerMessage(target.getNameWithGenus() + " has no wounds to heal.", (byte)3);
         return false;
      } else if (target.isReborn()) {
         return true;
      } else if (target.equals(performer)) {
         return true;
      } else if (!target.isPlayer() || performer.getDeity() == null) {
         return true;
      } else if (target.isFriendlyKingdom(performer.getKingdomId())) {
         return true;
      } else if (performer.faithful) {
         performer.getCommunicator().sendNormalServerMessage(performer.getDeity().getName() + " would never accept that.", (byte)3);
         return false;
      } else {
         return true;
      }
   }

   @Override
   void doEffect(Skill castSkill, double power, Creature performer, Creature target) {
      boolean doeff = true;
      if (target.isReborn()) {
         doeff = false;
         performer.getCommunicator().sendNormalServerMessage("You slay " + target.getNameWithGenus() + ".", (byte)4);
         Server.getInstance().broadCastAction(performer.getName() + " slays " + target.getNameWithGenus() + "!", performer, 5);
         target.addAttacker(performer);
         target.die(false, "Heal cast on Reborn");
      } else if (!target.equals(performer) && target.isPlayer() && performer.getDeity() != null && !target.isFriendlyKingdom(performer.getKingdomId())) {
         performer.getCommunicator()
            .sendNormalServerMessage(
               performer.getDeity().getName() + " becomes very upset at the way you abuse " + performer.getDeity().getHisHerItsString() + " powers!", (byte)3
            );

         try {
            performer.setFaith(performer.getFaith() / 2.0F);
         } catch (Exception var16) {
            logger.log(Level.WARNING, var16.getMessage(), (Throwable)var16);
         }
      }

      if (doeff) {
         Wounds tWounds = target.getBody().getWounds();
         if (tWounds == null) {
            performer.getCommunicator().sendNormalServerMessage(target.getName() + " has no wounds to heal.", (byte)3);
            return;
         }

         double resistance = SpellResist.getSpellResistance(target, this.getNumber());
         double healingPool = Math.max(20.0, power) / 100.0 * 65535.0 * 2.0;
         if (performer.getCultist() != null && performer.getCultist().healsFaster()) {
            healingPool *= 2.0;
         }

         healingPool *= resistance;

         for(Wound w : tWounds.getWounds()) {
            if ((double)w.getSeverity() <= healingPool) {
               healingPool -= (double)w.getSeverity();
               SpellResist.addSpellResistance(target, this.getNumber(), (double)w.getSeverity());
               w.heal();
            }
         }

         if (tWounds.getWounds().length > 0 && healingPool > 0.0) {
            SpellResist.addSpellResistance(target, this.getNumber(), healingPool);
            tWounds.getWounds()[Server.rand.nextInt(tWounds.getWounds().length)].modifySeverity((int)(-healingPool));
         }

         if (tWounds.getWounds().length > 0) {
            performer.getCommunicator().sendNormalServerMessage("You heal some of " + target.getNameWithGenus() + "'s wounds.", (byte)4);
            target.getCommunicator().sendNormalServerMessage(performer.getNameWithGenus() + " heals some of your wounds.", (byte)4);
         } else {
            performer.getCommunicator().sendNormalServerMessage("You fully heal " + target.getNameWithGenus() + ".", (byte)4);
            target.getCommunicator().sendNormalServerMessage(performer.getNameWithGenus() + " heals your wounds.", (byte)4);
         }

         VolaTile t = Zones.getTileOrNull(target.getTileX(), target.getTileY(), target.isOnSurface());
         if (t != null) {
            t.sendAttachCreatureEffect(target, (byte)11, (byte)0, (byte)0, (byte)0, (byte)0);
         }
      }
   }
}

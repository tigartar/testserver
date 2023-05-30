package com.wurmonline.server.spells;

import com.wurmonline.server.MessageServer;
import com.wurmonline.server.Server;
import com.wurmonline.server.creatures.Creature;
import com.wurmonline.server.creatures.SpellEffects;
import com.wurmonline.server.creatures.ai.scripts.UtilitiesAOE;
import com.wurmonline.server.skills.Skill;
import com.wurmonline.server.utils.CreatureLineSegment;
import com.wurmonline.shared.util.MulticolorLineSegment;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;

public class Phantasms extends ReligiousSpell {
   public static final int RANGE = 50;

   public Phantasms() {
      super("Phantasms", 426, 10, 10, 10, 20, 0L);
      this.targetCreature = true;
      this.enchantment = 43;
      this.offensive = true;
      this.effectdesc = "confusion and muddled thoughts.";
      this.description = "confuses the target and may make them attack something else";
      this.type = 2;
   }

   public static final void doImmediateEffect(double power, Creature target) {
      Spell ph = Spells.getSpell(426);
      SpellEffects effs = target.getSpellEffects();
      if (effs == null) {
         effs = target.createSpellEffects();
      }

      SpellEffect eff = effs.getSpellEffect(ph.getEnchantment());
      if (eff == null) {
         eff = new SpellEffect(target.getWurmId(), ph.getEnchantment(), (float)power, 310, (byte)9, (byte)1, true);
         effs.addSpellEffect(eff);
      } else if ((double)eff.getPower() < power) {
         eff.setPower((float)power);
         eff.setTimeleft(310);
         target.sendUpdateSpellEffect(eff);
      }
   }

   @Override
   void doEffect(Skill castSkill, double power, Creature performer, Creature target) {
      if (target.isPlayer()) {
         SpellEffects effs = target.getSpellEffects();
         if (effs == null) {
            effs = target.createSpellEffects();
         }

         SpellEffect eff = effs.getSpellEffect(this.enchantment);
         if (eff == null) {
            performer.getCommunicator().sendNormalServerMessage(target.getName() + " will now receive " + this.effectdesc, (byte)2);
            eff = new SpellEffect(target.getWurmId(), this.enchantment, (float)power, 300 + performer.getNumLinks() * 10, (byte)9, (byte)1, true);
            effs.addSpellEffect(eff);
         } else if ((double)eff.getPower() > power) {
            performer.getCommunicator().sendNormalServerMessage("You frown as you fail to improve the power.", (byte)3);
         } else {
            performer.getCommunicator().sendNormalServerMessage("You succeed in improving the power of the " + this.name + ".", (byte)2);
            eff.setPower((float)power);
            eff.setTimeleft(Math.max(eff.timeleft, 300 + performer.getNumLinks() * 10));
            target.sendUpdateSpellEffect(eff);
         }
      } else {
         HashSet<Creature> nearbyCreatures = UtilitiesAOE.getRadialAreaCreatures(target, (float)(target.getTemplate().getVision() * 4));
         Creature testCret = null;

         for(Iterator<Creature> it = nearbyCreatures.iterator(); it.hasNext(); testCret = it.next()) {
            if (testCret != null) {
               if (testCret.getPower() > 0 || testCret.isUnique()) {
                  it.remove();
               } else if (testCret != target.getTarget() && testCret != performer && !testCret.isRidden()) {
                  byte att = target.getAttitude(testCret);
                  if (att != 1 && att != 7 && att != 5) {
                     byte casterAtt = performer.getAttitude(testCret);
                     if (casterAtt == 1 || casterAtt == 7 || casterAtt == 5) {
                        it.remove();
                     }
                  } else {
                     it.remove();
                  }
               } else {
                  it.remove();
               }
            }
         }

         if (nearbyCreatures.size() > 0) {
            int currentCret = 0;
            int targetCret = Server.rand.nextInt(nearbyCreatures.size());

            for(Creature c : nearbyCreatures) {
               if (currentCret == targetCret) {
                  target.removeTarget(target.target);
                  target.setTarget(c.getWurmId(), true);
                  target.setOpponent(c);
                  performer.getCommunicator()
                     .sendNormalServerMessage(target.getName() + " starts to see phantasms  and turns towards " + c.getName() + " in anger.", (byte)2);
                  ArrayList<MulticolorLineSegment> segments = new ArrayList<>();
                  segments.add(new CreatureLineSegment(target));
                  segments.add(new MulticolorLineSegment(" starts to see phantasms and turns to attack ", (byte)0));
                  segments.add(new CreatureLineSegment(c));
                  segments.add(new MulticolorLineSegment(" in anger.", (byte)0));
                  MessageServer.broadcastColoredAction(segments, performer, target, 5, true);
               } else {
                  ++currentCret;
               }
            }
         } else if (target.getTarget() == null) {
            performer.getCommunicator().sendNormalServerMessage(target.getName() + " starts to see phantasms  but cannot find the source of them.", (byte)2);
         } else if (target.getTarget() == performer) {
            performer.getCommunicator()
               .sendNormalServerMessage(target.getName() + " starts to see phantasms  but can only see you as being the cause.", (byte)2);
         } else {
            performer.getCommunicator()
               .sendNormalServerMessage(
                  target.getName() + " starts to see phantasms  but can only see " + target.getTarget().getName() + " as being the cause.", (byte)2
               );
         }
      }
   }
}

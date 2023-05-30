package com.wurmonline.server.spells;

import com.wurmonline.server.Server;
import com.wurmonline.server.creatures.Creature;
import com.wurmonline.server.items.Item;
import com.wurmonline.server.items.ItemSpellEffects;
import com.wurmonline.server.skills.Skill;
import com.wurmonline.server.zones.Zones;

public final class Courier extends ReligiousSpell {
   public static final int RANGE = 4;

   Courier() {
      super("Courier", 338, 30, 30, 20, 30, 0L);
      this.targetItem = true;
      this.enchantment = 20;
      this.effectdesc = "is possessed by some messenger spirits.";
      this.description = "tempts messenger spirits to inhabit the target and work for you";
      this.type = 1;
   }

   @Override
   boolean precondition(Skill castSkill, Creature performer, Item target) {
      if (!target.isMailBox() && !target.isSpringFilled() && !target.isUnenchantedTurret() && !target.isPuppet() && !target.isEnchantedTurret()) {
         performer.getCommunicator().sendNormalServerMessage("The spell will not work on that.", (byte)3);
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
      if ((target.isMailBox() || target.isSpringFilled() || target.isPuppet() || target.isUnenchantedTurret() || target.isEnchantedTurret())
         && (!target.hasDarkMessenger() || target.isEnchantedTurret())) {
         if (target.isUnenchantedTurret() || target.isEnchantedTurret()) {
            int spirit = Zones.getSpiritsForTile(performer.getTileX(), performer.getTileY(), performer.isOnSurface());
            String sname = "no spirits";
            int templateId = 934;
            if (spirit == 4) {
               templateId = 942;
               sname = "There are plenty of air spirits at this height.";
            }

            if (spirit == 2) {
               templateId = 968;
               sname = "Some water spirits were closeby.";
            }

            if (spirit == 3) {
               templateId = 940;
               sname = "Earth spirits are everywhere below ground.";
            }

            if (spirit == 1) {
               sname = "Some nearby fire spirits are drawn to your contraption.";
               templateId = 941;
            }

            if (templateId == 934) {
               performer.getCommunicator().sendAlertServerMessage("There are no spirits nearby. Nothing happens.", (byte)3);
               return;
            }

            if (target.isUnenchantedTurret()) {
               performer.getCommunicator().sendSafeServerMessage(sname);
               target.setTemplateId(templateId);
               target.setAuxData(performer.getKingdomId());
            } else if (target.isEnchantedTurret()) {
               if (target.getTemplateId() != templateId) {
                  performer.getCommunicator().sendAlertServerMessage("The nearby spirits ignore your contraption. Nothing happens.", (byte)3);
                  return;
               }

               performer.getCommunicator().sendSafeServerMessage(sname);
            }
         }

         ItemSpellEffects effs = target.getSpellEffects();
         if (effs == null) {
            effs = new ItemSpellEffects(target.getWurmId());
         }

         SpellEffect eff = effs.getSpellEffect(this.enchantment);
         if (eff == null) {
            performer.getCommunicator().sendNormalServerMessage("You summon nearby spirits into the " + target.getName() + ".", (byte)2);
            eff = new SpellEffect(target.getWurmId(), this.enchantment, (float)power, 20000000);
            effs.addSpellEffect(eff);
            Server.getInstance()
               .broadCastAction(
                  performer.getName() + " looks pleased as " + performer.getHeSheItString() + " summons some spirits into the " + target.getName() + ".",
                  performer,
                  5
               );
            if (!target.isEnchantedTurret()) {
               target.setHasCourier(true);
            }
         } else if ((double)eff.getPower() > power) {
            performer.getCommunicator().sendNormalServerMessage("You frown as you fail to summon more spirits into the " + target.getName() + ".", (byte)3);
            Server.getInstance().broadCastAction(performer.getName() + " frowns.", performer, 5);
         } else {
            performer.getCommunicator().sendNormalServerMessage("You succeed in summoning more spirits into the " + this.name + ".", (byte)2);
            eff.improvePower(performer, (float)power);
            if (!target.isEnchantedTurret()) {
               target.setHasCourier(true);
            }

            Server.getInstance()
               .broadCastAction(
                  performer.getName() + " looks pleased as " + performer.getHeSheItString() + " summons some spirits into the " + target.getName() + ".",
                  performer,
                  5
               );
         }
      } else {
         performer.getCommunicator().sendNormalServerMessage("The spell fizzles.", (byte)3);
      }
   }

   @Override
   void doNegativeEffect(Skill castSkill, double power, Creature performer, Item target) {
      performer.getCommunicator().sendNormalServerMessage("The " + target.getName() + " emits a deep worrying sound of resonance, but stays intact.", (byte)3);
   }
}

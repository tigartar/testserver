/*
 * Decompiled with CFR 0.152.
 */
package com.wurmonline.server.spells;

import com.wurmonline.server.Server;
import com.wurmonline.server.creatures.Creature;
import com.wurmonline.server.creatures.SpellEffects;
import com.wurmonline.server.skills.Skill;
import com.wurmonline.server.spells.KarmaEnchantment;
import com.wurmonline.server.spells.SpellEffect;

public class Incinerate
extends KarmaEnchantment {
    public static final int RANGE = 24;

    public Incinerate() {
        super("Incinerate", 686, 15, 750, 10, 1, 60000L);
        this.targetCreature = true;
        this.offensive = true;
        this.enchantment = (byte)94;
        this.description = "creates a heat wave around the target";
    }

    @Override
    boolean precondition(Skill castSkill, Creature performer, Creature target) {
        if ((target.isHuman() || target.isDominated()) && target.getAttitude(performer) != 2 && !performer.getDeity().isHateGod() && performer.faithful && !performer.isDuelOrSpar(target)) {
            performer.getCommunicator().sendNormalServerMessage(performer.getDeity().getName() + " would never accept your attack on " + target.getName() + ".", (byte)3);
            return false;
        }
        return true;
    }

    @Override
    void doEffect(Skill castSkill, double power, Creature performer, Creature target) {
        if (!target.isUnique() || power > 99.0) {
            SpellEffect eff;
            SpellEffects effs;
            if ((target.isHuman() || target.isDominated()) && target.getAttitude(performer) != 2 && !performer.getDeity().isHateGod() && !performer.isDuelOrSpar(target)) {
                performer.modifyFaith(-(100.0f - performer.getFaith()) / 50.0f);
            }
            if ((effs = target.getSpellEffects()) == null) {
                effs = target.createSpellEffects();
            }
            if ((eff = effs.getSpellEffect(this.enchantment)) == null) {
                if (target != performer) {
                    performer.getCommunicator().sendNormalServerMessage(target.getNameWithGenus() + " is engulfed in a wave of extreme heat.", (byte)4);
                }
                target.getCommunicator().sendAlertServerMessage("You are engulfed in a wave of extreme heat!", (byte)4);
                eff = new SpellEffect(target.getWurmId(), this.enchantment, (float)power, 60, 9, 1, true);
                effs.addSpellEffect(eff);
                Server.getInstance().broadCastAction(performer.getNameWithGenus() + " looks pleased as " + performer.getHeSheItString() + " engulfs " + target.getNameWithGenus() + " in a wave of heat.", performer, 5);
            } else if ((double)eff.getPower() > power) {
                performer.getCommunicator().sendNormalServerMessage("You frown as you fail to improve the heat.", (byte)3);
                Server.getInstance().broadCastAction(performer.getNameWithGenus() + " frowns.", performer, 5);
            } else {
                if (target != performer) {
                    performer.getCommunicator().sendNormalServerMessage("You succeed in improving the heat around " + target.getNameWithGenus() + ".", (byte)2);
                }
                target.getCommunicator().sendAlertServerMessage("The heat around you increases. The pain is excruciating!", (byte)4);
                eff.setPower((float)power);
                eff.setTimeleft(60);
                target.sendUpdateSpellEffect(eff);
                Server.getInstance().broadCastAction(performer.getNameWithGenus() + " looks pleased as " + performer.getHeSheItString() + " increases the heat around " + target.getNameWithGenus() + ".", performer, 5);
            }
        } else {
            performer.getCommunicator().sendNormalServerMessage("You try to incinerate " + target.getNameWithGenus() + " but fail.", (byte)3);
            target.getCommunicator().sendNormalServerMessage(performer.getNameWithGenus() + " tries to incinerate you but fails.", (byte)4);
        }
    }

    @Override
    void doNegativeEffect(Skill castSkill, double power, Creature performer, Creature target) {
        performer.getCommunicator().sendNormalServerMessage("You try to incinerate " + target.getNameWithGenus() + " but fail.", (byte)3);
        target.getCommunicator().sendNormalServerMessage(performer.getNameWithGenus() + " tries to incinerate you but fails.", (byte)4);
    }
}


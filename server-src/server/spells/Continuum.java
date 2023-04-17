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

public class Continuum
extends KarmaEnchantment {
    public static final int RANGE = 24;

    public Continuum() {
        super("Continuum", 552, 20, 650, 20, 1, 240000L);
        this.targetCreature = true;
        this.enchantment = (byte)69;
        this.effectdesc = "20% general damage reduction.";
        this.description = "gives you 20% protection against most damage";
        this.durationModifier = 3.0f;
    }

    @Override
    void doEffect(Skill castSkill, double power, Creature performer, Creature target) {
        SpellEffects effs = target.getSpellEffects();
        if (effs == null) {
            effs = target.createSpellEffects();
        }
        SpellEffect eff = effs.getSpellEffect(this.enchantment);
        int duration = (int)(900.0 + 1800.0 * (power / 100.0));
        if (eff == null) {
            performer.getCommunicator().sendNormalServerMessage("Duration: " + duration, (byte)2);
            if (target != performer) {
                performer.getCommunicator().sendNormalServerMessage(target.getNameWithGenus() + " now has " + this.effectdesc);
            }
            target.getCommunicator().sendNormalServerMessage("You now have " + this.effectdesc);
            eff = new SpellEffect(target.getWurmId(), this.enchantment, (float)power, duration, 9, 0, true);
            effs.addSpellEffect(eff);
            Server.getInstance().broadCastAction(performer.getName() + " looks pleased.", performer, 5);
        } else if ((double)eff.getPower() > power) {
            performer.getCommunicator().sendNormalServerMessage("You frown as you fail to improve the power.", (byte)3);
            Server.getInstance().broadCastAction(performer.getName() + " frowns.", performer, 5);
        } else {
            if (target != performer) {
                performer.getCommunicator().sendNormalServerMessage("You succeed in improving the power of the " + this.name + ".", (byte)2);
            }
            target.getCommunicator().sendNormalServerMessage("You will now receive improved " + this.effectdesc);
            eff.setPower((float)power);
            eff.setTimeleft(Math.max(eff.timeleft, duration));
            target.sendUpdateSpellEffect(eff);
            Server.getInstance().broadCastAction(performer.getName() + " looks pleased.", performer, 5);
        }
    }
}


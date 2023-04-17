/*
 * Decompiled with CFR 0.152.
 */
package com.wurmonline.server.spells;

import com.wurmonline.server.Server;
import com.wurmonline.server.creatures.Creature;
import com.wurmonline.server.items.Item;
import com.wurmonline.server.items.ItemSpellEffects;
import com.wurmonline.server.skills.Skill;
import com.wurmonline.server.spells.ItemEnchantment;
import com.wurmonline.server.spells.SpellEffect;

public class Bloodthirst
extends ItemEnchantment {
    public static final int RANGE = 4;

    public Bloodthirst() {
        super("Bloodthirst", 454, 20, 50, 60, 31, 0L);
        this.targetWeapon = true;
        this.enchantment = (byte)45;
        this.effectdesc = "will do more damage the more you hit creatures or players.";
        this.description = "increases damage dealt with weapons, improves power with usage";
    }

    @Override
    void doEffect(Skill castSkill, double power, Creature performer, Item target) {
        SpellEffect eff;
        ItemSpellEffects effs = target.getSpellEffects();
        if (effs == null) {
            effs = new ItemSpellEffects(target.getWurmId());
        }
        if ((eff = effs.getSpellEffect(this.enchantment)) == null) {
            performer.getCommunicator().sendNormalServerMessage("The " + target.getName() + " will now do more and more damage when you hit creatures or other players.", (byte)2);
            eff = new SpellEffect(target.getWurmId(), this.enchantment, (float)power, 20000000);
            effs.addSpellEffect(eff);
            Server.getInstance().broadCastAction(performer.getName() + " looks pleased.", performer, 5);
        } else if ((double)(eff.getPower() / 10.0f) > power) {
            performer.getCommunicator().sendNormalServerMessage("You frown as you fail to improve the power.", (byte)3);
            Server.getInstance().broadCastAction(performer.getName() + " frowns.", performer, 5);
        } else {
            performer.getCommunicator().sendNormalServerMessage("You succeed in improving the power of the " + this.name + ".", (byte)2);
            eff.setPower(eff.getPower() + (float)Math.min((double)((float)power), (double)(10000.0f - eff.getPower()) * 0.1));
            Server.getInstance().broadCastAction(performer.getName() + " looks pleased.", performer, 5);
        }
    }
}


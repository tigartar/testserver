/*
 * Decompiled with CFR 0.152.
 */
package com.wurmonline.server.spells;

import com.wurmonline.server.creatures.Creature;
import com.wurmonline.server.items.Item;
import com.wurmonline.server.skills.Skill;
import com.wurmonline.server.spells.EnchantUtil;
import com.wurmonline.server.spells.ReligiousSpell;

public final class DemiseMonster
extends ReligiousSpell {
    public static final int RANGE = 4;

    DemiseMonster() {
        super("Monster Demise", 268, 30, 40, 40, 41, 0L);
        this.targetWeapon = true;
        this.enchantment = (byte)10;
        this.effectdesc = "will deal increased damage to monstrous creatures.";
        this.description = "increases damage dealt to monstrous creatures";
        this.singleItemEnchant = true;
        this.type = 1;
    }

    @Override
    boolean precondition(Skill castSkill, Creature performer, Item target) {
        return EnchantUtil.canEnchantDemise(performer, target);
    }

    @Override
    boolean precondition(Skill castSkill, Creature performer, Creature target) {
        return false;
    }

    @Override
    void doEffect(Skill castSkill, double power, Creature performer, Item target) {
        target.enchant((byte)10);
        performer.getCommunicator().sendNormalServerMessage("The " + target.getName() + " will now be effective against monsters.", (byte)2);
    }
}


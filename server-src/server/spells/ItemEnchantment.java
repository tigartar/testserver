/*
 * Decompiled with CFR 0.152.
 */
package com.wurmonline.server.spells;

import com.wurmonline.server.creatures.Creature;
import com.wurmonline.server.items.Item;
import com.wurmonline.server.skills.Skill;
import com.wurmonline.server.spells.EnchantUtil;
import com.wurmonline.server.spells.ReligiousSpell;
import com.wurmonline.server.spells.SpellEffect;

public class ItemEnchantment
extends ReligiousSpell {
    public static final int RANGE = 4;

    ItemEnchantment(String aName, int aNum, int aCastingTime, int aCost, int aDifficulty, int aLevel, long aCooldown) {
        super(aName, aNum, aCastingTime, aCost, aDifficulty, aLevel, aCooldown);
    }

    @Override
    boolean precondition(Skill castSkill, Creature performer, Item target) {
        if (!ItemEnchantment.mayBeEnchanted(target)) {
            EnchantUtil.sendCannotBeEnchantedMessage(performer);
            return false;
        }
        SpellEffect negatingEffect = EnchantUtil.hasNegatingEffect(target, this.getEnchantment());
        if (negatingEffect != null) {
            EnchantUtil.sendNegatingEffectMessage(this.getName(), performer, target, negatingEffect);
            return false;
        }
        return true;
    }

    @Override
    boolean precondition(Skill castSkill, Creature performer, Creature target) {
        return false;
    }

    @Override
    void doEffect(Skill castSkill, double power, Creature performer, Item target) {
        this.enchantItem(performer, target, this.getEnchantment(), (float)power);
    }

    @Override
    void doNegativeEffect(Skill castSkill, double power, Creature performer, Item target) {
        this.checkDestroyItem(power, performer, target);
    }
}


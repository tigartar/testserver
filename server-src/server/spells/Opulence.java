/*
 * Decompiled with CFR 0.152.
 */
package com.wurmonline.server.spells;

import com.wurmonline.server.creatures.Creature;
import com.wurmonline.server.items.Item;
import com.wurmonline.server.skills.Skill;
import com.wurmonline.server.spells.ItemEnchantment;

public final class Opulence
extends ItemEnchantment {
    public static final int RANGE = 4;

    Opulence() {
        super("Opulence", 280, 20, 10, 10, 15, 0L);
        this.targetItem = true;
        this.enchantment = (byte)15;
        this.effectdesc = "will feed you more.";
        this.description = "causes a food item to be more effective at filling you up";
        this.type = 1;
    }

    @Override
    boolean precondition(Skill castSkill, Creature performer, Item target) {
        if (!Opulence.mayBeEnchanted(target) || !target.isFood()) {
            performer.getCommunicator().sendNormalServerMessage("The spell will not work on that.", (byte)3);
            return false;
        }
        return true;
    }
}


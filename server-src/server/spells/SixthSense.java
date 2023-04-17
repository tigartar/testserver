/*
 * Decompiled with CFR 0.152.
 */
package com.wurmonline.server.spells;

import com.wurmonline.server.creatures.Creature;
import com.wurmonline.server.skills.Skill;
import com.wurmonline.server.spells.CreatureEnchantment;

public class SixthSense
extends CreatureEnchantment {
    public static final int RANGE = 4;

    SixthSense() {
        super("Sixth Sense", 376, 10, 15, 20, 6, 0L);
        this.targetCreature = true;
        this.enchantment = (byte)21;
        this.effectdesc = "detect hidden dangers.";
        this.description = "detect hidden creatures and traps";
        this.type = 0;
    }

    @Override
    boolean precondition(Skill castSkill, Creature performer, Creature target) {
        if (!target.isPlayer()) {
            performer.getCommunicator().sendNormalServerMessage("You can only cast that on a person.");
            return false;
        }
        if (target.isReborn()) {
            return false;
        }
        if (!target.equals(performer)) {
            if (performer.getDeity() != null) {
                if (target.getDeity() != null) {
                    if (target.getDeity().isHateGod()) {
                        return performer.isFaithful();
                    }
                    return true;
                }
                return true;
            }
            return true;
        }
        return true;
    }
}


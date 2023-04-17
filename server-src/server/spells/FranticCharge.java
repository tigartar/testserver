/*
 * Decompiled with CFR 0.152.
 */
package com.wurmonline.server.spells;

import com.wurmonline.server.Servers;
import com.wurmonline.server.creatures.Creature;
import com.wurmonline.server.skills.Skill;
import com.wurmonline.server.spells.CreatureEnchantment;

public class FranticCharge
extends CreatureEnchantment {
    public static final int RANGE = 4;

    public FranticCharge() {
        super("Frantic Charge", 423, 5, 20, 30, 30, 0L);
        this.targetCreature = true;
        this.enchantment = (byte)39;
        this.effectdesc = "faster attack and movement speed.";
        this.description = "increases attack and movement speed of a player";
    }

    @Override
    boolean precondition(Skill castSkill, Creature performer, Creature target) {
        if (super.precondition(castSkill, performer, target)) {
            if (Servers.isThisAPvpServer() && !target.isPlayer()) {
                performer.getCommunicator().sendNormalServerMessage("You cannot cast " + this.getName() + " on " + target.getNameWithGenus());
                return false;
            }
        } else {
            return false;
        }
        return true;
    }
}


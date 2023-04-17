/*
 * Decompiled with CFR 0.152.
 */
package com.wurmonline.server.spells;

import com.wurmonline.server.creatures.Creature;
import com.wurmonline.server.skills.Skill;
import com.wurmonline.server.spells.ReligiousSpell;

public final class DrainStamina
extends ReligiousSpell {
    public static final int RANGE = 12;

    DrainStamina() {
        super("Drain Stamina", 254, 9, 20, 20, 10, 0L);
        this.targetCreature = true;
        this.offensive = true;
        this.description = "drains stamina from a creature and returns it to you";
        this.type = (byte)2;
    }

    @Override
    boolean precondition(Skill castSkill, Creature performer, Creature target) {
        if (target.isReborn()) {
            performer.getCommunicator().sendNormalServerMessage("You can not drain stamina from the " + target.getNameWithGenus() + ".", (byte)3);
            return false;
        }
        if (target.equals(performer)) {
            return false;
        }
        if (target.getStatus().getStamina() < 200) {
            performer.getCommunicator().sendNormalServerMessage(target.getNameWithGenus() + " does not have enough stamina to drain.", (byte)3);
            return false;
        }
        return true;
    }

    @Override
    void doEffect(Skill castSkill, double power, Creature performer, Creature target) {
        int stam = target.getStatus().getStamina();
        int staminaGained = (int)(Math.max(power, 20.0) / 200.0 * (double)stam * (double)target.addSpellResistance((short)254));
        if (staminaGained > 1) {
            performer.getStatus().modifyStamina((int)(0.75 * (double)staminaGained));
            target.getStatus().modifyStamina(-staminaGained);
            performer.getCommunicator().sendNormalServerMessage("You drain some stamina from " + target.getNameWithGenus() + ".", (byte)4);
            target.getCommunicator().sendNormalServerMessage(performer.getNameWithGenus() + " drains you on stamina.", (byte)4);
        } else {
            performer.getCommunicator().sendNormalServerMessage("You try to drain some stamina from " + target.getNameWithGenus() + " but fail.", (byte)3);
            target.getCommunicator().sendNormalServerMessage(performer.getNameWithGenus() + " tries to drain you on stamina but fails.", (byte)4);
        }
    }
}


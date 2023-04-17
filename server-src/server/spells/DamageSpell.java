/*
 * Decompiled with CFR 0.152.
 */
package com.wurmonline.server.spells;

import com.wurmonline.server.creatures.Creature;
import com.wurmonline.server.spells.ReligiousSpell;
import com.wurmonline.server.spells.Spell;
import com.wurmonline.server.spells.SpellResist;

public class DamageSpell
extends ReligiousSpell {
    DamageSpell(String aName, int aNum, int aCastingTime, int aCost, int aDifficulty, int aLevel, long aCooldown) {
        super(aName, aNum, aCastingTime, aCost, aDifficulty, aLevel, aCooldown);
    }

    public double calculateDamage(Creature target, double power, double baseDamage, double damagePerPower) {
        double damage = power * damagePerPower;
        damage += baseDamage;
        double resistance = SpellResist.getSpellResistance(target, this.getNumber());
        SpellResist.addSpellResistance(target, this.getNumber(), damage *= resistance);
        return Spell.modifyDamage(target, damage);
    }
}


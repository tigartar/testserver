/*
 * Decompiled with CFR 0.152.
 */
package com.wurmonline.server.spells;

import com.wurmonline.server.spells.Spell;
import com.wurmonline.shared.constants.AttitudeConstants;

public abstract class KarmaSpell
extends Spell
implements AttitudeConstants {
    public KarmaSpell(String aName, int aNum, int aCastingTime, int aCost, int aDifficulty, int aLevel, long cooldown) {
        super(aName, aNum, aCastingTime, aCost, aDifficulty, aLevel, cooldown, false);
        this.karmaSpell = true;
    }
}


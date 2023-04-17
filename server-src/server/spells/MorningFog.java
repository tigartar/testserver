/*
 * Decompiled with CFR 0.152.
 */
package com.wurmonline.server.spells;

import com.wurmonline.server.spells.CreatureEnchantment;

public final class MorningFog
extends CreatureEnchantment {
    public static final int RANGE = 4;

    MorningFog() {
        super("Morning Fog", 282, 10, 5, 10, 7, 0L);
        this.targetCreature = true;
        this.enchantment = (byte)19;
        this.effectdesc = "protection from thorns and lava.";
        this.description = "protection from thorns and lava";
        this.type = (byte)2;
    }
}


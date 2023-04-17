/*
 * Decompiled with CFR 0.152.
 */
package com.wurmonline.server.spells;

import com.wurmonline.server.spells.CreatureEnchantment;

public class Weakness
extends CreatureEnchantment {
    public static final int RANGE = 50;

    public Weakness() {
        super("Weakness", 429, 20, 50, 40, 40, 30000L);
        this.enchantment = (byte)41;
        this.offensive = true;
        this.effectdesc = "reduced body strength.";
        this.description = "reduces body strength by one fifth";
        this.type = (byte)2;
    }
}


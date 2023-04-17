/*
 * Decompiled with CFR 0.152.
 */
package com.wurmonline.server.spells;

import com.wurmonline.server.spells.CreatureEnchantment;

public class GoatShape
extends CreatureEnchantment {
    public static final int RANGE = 20;

    public GoatShape() {
        super("Goat Shape", 422, 10, 20, 20, 25, 0L);
        this.enchantment = (byte)38;
        this.effectdesc = "better climbing capability.";
        this.description = "increases climbing ability";
        this.type = (byte)2;
    }
}


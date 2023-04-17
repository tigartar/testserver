/*
 * Decompiled with CFR 0.152.
 */
package com.wurmonline.server.spells;

import com.wurmonline.server.spells.ItemEnchantment;

public final class Glacial
extends ItemEnchantment {
    public static final int RANGE = 4;

    Glacial() {
        super("Glacial", 261, 30, 40, 50, 47, 0L);
        this.targetJewelry = true;
        this.enchantment = (byte)3;
        this.effectdesc = "will increase any frost damage you cause.";
        this.description = "increases any frost damage you cause";
        this.type = 1;
    }
}


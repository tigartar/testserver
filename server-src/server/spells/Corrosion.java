/*
 * Decompiled with CFR 0.152.
 */
package com.wurmonline.server.spells;

import com.wurmonline.server.spells.ItemEnchantment;

public final class Corrosion
extends ItemEnchantment {
    public static final int RANGE = 4;

    Corrosion() {
        super("Corrosion", 262, 30, 40, 50, 44, 0L);
        this.targetJewelry = true;
        this.enchantment = (byte)4;
        this.effectdesc = "will increase any acid damage you cause.";
        this.description = "increases any acid damage you cause";
        this.type = 1;
    }
}


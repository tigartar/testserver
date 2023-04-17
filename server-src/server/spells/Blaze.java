/*
 * Decompiled with CFR 0.152.
 */
package com.wurmonline.server.spells;

import com.wurmonline.server.spells.ItemEnchantment;

public final class Blaze
extends ItemEnchantment {
    public static final int RANGE = 4;

    Blaze() {
        super("Blaze", 260, 30, 40, 50, 46, 0L);
        this.targetJewelry = true;
        this.enchantment = (byte)2;
        this.effectdesc = "will increase any fire damage you cause.";
        this.description = "increases any fire damage you cause";
        this.type = 1;
    }
}


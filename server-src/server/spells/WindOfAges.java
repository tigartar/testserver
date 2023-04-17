/*
 * Decompiled with CFR 0.152.
 */
package com.wurmonline.server.spells;

import com.wurmonline.server.spells.ItemEnchantment;

public final class WindOfAges
extends ItemEnchantment {
    public static final int RANGE = 4;

    WindOfAges() {
        super("Wind of Ages", 279, 20, 50, 60, 50, 0L);
        this.targetItem = true;
        this.enchantment = (byte)16;
        this.effectdesc = "will be quicker to use.";
        this.description = "increases usage speed";
        this.type = 1;
    }
}


/*
 * Decompiled with CFR 0.152.
 */
package com.wurmonline.server.spells;

import com.wurmonline.server.spells.ItemEnchantment;

public class BlessingDark
extends ItemEnchantment {
    public static final int RANGE = 4;

    public BlessingDark() {
        super("Blessings of the Dark", 456, 20, 70, 60, 51, 0L);
        this.targetItem = true;
        this.enchantment = (byte)47;
        this.effectdesc = "will increase skill gained and speed with it when used.";
        this.description = "increases skill gain and usage speed";
    }
}


/*
 * Decompiled with CFR 0.152.
 */
package com.wurmonline.server.spells;

import com.wurmonline.server.spells.ItemEnchantment;

public final class ProtectionFrost
extends ItemEnchantment {
    public static final int RANGE = 4;

    ProtectionFrost() {
        super("Frost Protection", 264, 30, 30, 30, 30, 0L);
        this.targetJewelry = true;
        this.enchantment = (byte)6;
        this.effectdesc = "will reduce any frost damage you take.";
        this.description = "reduces any frost damage you take";
        this.type = 1;
    }
}


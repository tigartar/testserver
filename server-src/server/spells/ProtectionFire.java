/*
 * Decompiled with CFR 0.152.
 */
package com.wurmonline.server.spells;

import com.wurmonline.server.spells.ItemEnchantment;

public final class ProtectionFire
extends ItemEnchantment {
    public static final int RANGE = 4;

    ProtectionFire() {
        super("Fire Protection", 265, 30, 30, 30, 28, 0L);
        this.targetJewelry = true;
        this.enchantment = (byte)7;
        this.effectdesc = "will reduce any fire damage you take.";
        this.description = "reduces any fire damage you take";
        this.type = (byte)2;
    }
}


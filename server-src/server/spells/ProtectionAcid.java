/*
 * Decompiled with CFR 0.152.
 */
package com.wurmonline.server.spells;

import com.wurmonline.server.spells.ItemEnchantment;

public final class ProtectionAcid
extends ItemEnchantment {
    public static final int RANGE = 4;

    ProtectionAcid() {
        super("Acid Protection", 263, 30, 30, 30, 32, 0L);
        this.targetJewelry = true;
        this.enchantment = (byte)5;
        this.effectdesc = "will reduce any acid damage you take.";
        this.description = "reduces any acid damage you take";
        this.type = 1;
    }
}


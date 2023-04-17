/*
 * Decompiled with CFR 0.152.
 */
package com.wurmonline.server.spells;

import com.wurmonline.server.spells.ItemEnchantment;

public class Nimbleness
extends ItemEnchantment {
    public static final int RANGE = 4;

    public Nimbleness() {
        super("Nimbleness", 416, 20, 60, 60, 30, 0L);
        this.targetWeapon = true;
        this.enchantment = (byte)32;
        this.effectdesc = "increase the chance to hit.";
        this.description = "increases chance to hit";
        this.type = (byte)2;
    }
}


/*
 * Decompiled with CFR 0.152.
 */
package com.wurmonline.server.spells;

import com.wurmonline.server.spells.ItemEnchantment;

public class Frostbrand
extends ItemEnchantment {
    public static final int RANGE = 40;

    public Frostbrand() {
        super("Frostbrand", 417, 20, 45, 60, 40, 0L);
        this.targetWeapon = true;
        this.enchantment = (byte)33;
        this.effectdesc = "will cause frost wounds.";
        this.description = "causes extra frost wounds on an enemy when hit";
        this.type = 1;
    }
}


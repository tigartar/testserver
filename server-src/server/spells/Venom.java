/*
 * Decompiled with CFR 0.152.
 */
package com.wurmonline.server.spells;

import com.wurmonline.server.spells.ItemEnchantment;

public class Venom
extends ItemEnchantment {
    public static final int RANGE = 40;

    Venom() {
        super("Venom", 412, 20, 100, 60, 62, 0L);
        this.targetWeapon = true;
        this.enchantment = (byte)27;
        this.effectdesc = "will deal only poison damage wounds.";
        this.description = "causes a weapon to deal poison wounds instead of normal damage, but may reduce damage";
        this.type = 1;
    }
}


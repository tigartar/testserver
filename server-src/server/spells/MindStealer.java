/*
 * Decompiled with CFR 0.152.
 */
package com.wurmonline.server.spells;

import com.wurmonline.server.spells.ItemEnchantment;

public class MindStealer
extends ItemEnchantment {
    public static final int RANGE = 4;

    public MindStealer() {
        super("Mind Stealer", 415, 20, 100, 60, 50, 0L);
        this.targetWeapon = true;
        this.enchantment = (byte)31;
        this.effectdesc = "will steal skill knowledge from some creatures.";
        this.description = "may steal some higher skills from enemies on hits";
        this.type = (byte)2;
    }
}


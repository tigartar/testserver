/*
 * Decompiled with CFR 0.152.
 */
package com.wurmonline.server.spells;

import com.wurmonline.server.spells.CreatureEnchantment;

public class Hellstrength
extends CreatureEnchantment {
    public static final int RANGE = 4;

    public Hellstrength() {
        super("Hell Strength", 427, 10, 60, 40, 45, 30000L);
        this.enchantment = (byte)40;
        this.effectdesc = "increased body strength and soul strength.";
        this.description = "increases body strength and soul strength";
        this.type = (byte)2;
    }
}


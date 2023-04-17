/*
 * Decompiled with CFR 0.152.
 */
package com.wurmonline.server.spells;

import com.wurmonline.server.spells.CreatureEnchantment;

public class WillowSpine
extends CreatureEnchantment {
    public static final int RANGE = 4;

    public WillowSpine() {
        super("Willowspine", 405, 10, 20, 29, 35, 30000L);
        this.enchantment = (byte)23;
        this.effectdesc = "increased chance to dodge.";
        this.description = "increases chance to dodge attacks";
        this.type = (byte)2;
    }
}


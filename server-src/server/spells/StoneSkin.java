/*
 * Decompiled with CFR 0.152.
 */
package com.wurmonline.server.spells;

import com.wurmonline.server.spells.KarmaEnchantment;

public class StoneSkin
extends KarmaEnchantment {
    public StoneSkin() {
        super("Stoneskin", 553, 20, 500, 20, 1, 240000L);
        this.targetCreature = true;
        this.enchantment = (byte)68;
        this.effectdesc = "3 wounds ignored.";
        this.description = "makes you ignore 3 wounds";
        this.durationModifier = 100000.0f;
    }
}


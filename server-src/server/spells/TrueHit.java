/*
 * Decompiled with CFR 0.152.
 */
package com.wurmonline.server.spells;

import com.wurmonline.server.spells.CreatureEnchantment;

public class TrueHit
extends CreatureEnchantment {
    public static final int RANGE = 4;

    public TrueHit() {
        super("Truehit", 447, 10, 10, 15, 30, 0L);
        this.targetCreature = true;
        this.enchantment = (byte)30;
        this.effectdesc = "combat vision and aiming.";
        this.description = "increases offensive combat rating";
        this.type = (byte)2;
    }
}


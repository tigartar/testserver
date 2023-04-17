/*
 * Decompiled with CFR 0.152.
 */
package com.wurmonline.server.spells;

import com.wurmonline.server.spells.ItemEnchantment;

public final class FlamingAura
extends ItemEnchantment {
    public static final int RANGE = 4;

    FlamingAura() {
        super("Flaming Aura", 277, 20, 45, 60, 39, 0L);
        this.targetWeapon = true;
        this.enchantment = (byte)14;
        this.effectdesc = "will burn targets hit with it.";
        this.description = "causes extra fire wounds on an enemy when hit";
        this.type = 1;
    }
}


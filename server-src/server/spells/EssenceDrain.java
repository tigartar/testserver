/*
 * Decompiled with CFR 0.152.
 */
package com.wurmonline.server.spells;

import com.wurmonline.server.spells.ItemEnchantment;

public class EssenceDrain
extends ItemEnchantment {
    public static final int RANGE = 4;

    EssenceDrain() {
        super("Essence Drain", 933, 20, 100, 60, 61, 0L);
        this.targetWeapon = true;
        this.enchantment = (byte)63;
        this.effectdesc = "will cause extra internal wounds and heal you.";
        this.description = "causes extra internal wounds and heals the wielder slightly";
        this.type = 1;
    }
}


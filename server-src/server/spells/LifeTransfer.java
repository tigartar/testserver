/*
 * Decompiled with CFR 0.152.
 */
package com.wurmonline.server.spells;

import com.wurmonline.server.spells.ItemEnchantment;

public class LifeTransfer
extends ItemEnchantment {
    public static final int RANGE = 4;

    LifeTransfer() {
        super("Life Transfer", 409, 20, 100, 60, 61, 0L);
        this.targetWeapon = true;
        this.enchantment = (byte)26;
        this.effectdesc = "will transfer life to you when harming enemies.";
        this.description = "heals the wielder when causing damage to an enemy";
        this.type = 1;
    }
}


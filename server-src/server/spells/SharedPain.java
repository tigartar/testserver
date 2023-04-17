/*
 * Decompiled with CFR 0.152.
 */
package com.wurmonline.server.spells;

import com.wurmonline.server.spells.ItemEnchantment;

public final class SharedPain
extends ItemEnchantment {
    public static final int RANGE = 4;

    SharedPain() {
        super("Aura of Shared Pain", 278, 20, 35, 60, 25, 0L);
        this.targetArmour = true;
        this.enchantment = (byte)17;
        this.effectdesc = "may damage creatures when they hit this armour.";
        this.description = "causes damage to creatures when they hit armour enchanted with this";
        this.type = 1;
    }
}


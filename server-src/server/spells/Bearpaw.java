/*
 * Decompiled with CFR 0.152.
 */
package com.wurmonline.server.spells;

import com.wurmonline.server.spells.CreatureEnchantment;

public class Bearpaw
extends CreatureEnchantment {
    public static final int RANGE = 40;

    public Bearpaw() {
        super("Bearpaws", 406, 10, 20, 29, 35, 0L);
        this.enchantment = (byte)24;
        this.effectdesc = "more effective weaponless fighting and increased unarmed damage.";
        this.description = "increases effective weaponless fighting skill and unarmed damage";
        this.type = (byte)2;
    }
}


/*
 * Decompiled with CFR 0.152.
 */
package com.wurmonline.server.spells;

import com.wurmonline.server.spells.ItemEnchantment;

public class WebArmour
extends ItemEnchantment {
    public static final int RANGE = 4;

    public WebArmour() {
        super("Web Armour", 455, 20, 35, 60, 25, 0L);
        this.targetArmour = true;
        this.enchantment = (byte)46;
        this.effectdesc = "may slow down creatures when they hit this armour.";
        this.description = "may slow down creatures when they hit armour enchanted with this";
    }
}


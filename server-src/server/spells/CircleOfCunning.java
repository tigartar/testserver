/*
 * Decompiled with CFR 0.152.
 */
package com.wurmonline.server.spells;

import com.wurmonline.server.spells.ItemEnchantment;

public final class CircleOfCunning
extends ItemEnchantment {
    public static final int RANGE = 4;

    CircleOfCunning() {
        super("Circle of Cunning", 276, 20, 50, 60, 51, 0L);
        this.targetItem = true;
        this.enchantment = (byte)13;
        this.effectdesc = "will increase skill gained with it when used.";
        this.description = "increases skill gain";
        this.type = 1;
    }
}


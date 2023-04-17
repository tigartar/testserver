/*
 * Decompiled with CFR 0.152.
 */
package com.wurmonline.server.loot;

import com.wurmonline.server.creatures.Creature;
import com.wurmonline.server.items.Item;
import com.wurmonline.server.loot.LootItem;
import java.util.Optional;

public interface ItemCreateFunc {
    public Optional<Item> create(Creature var1, Creature var2, LootItem var3);
}


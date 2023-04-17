/*
 * Decompiled with CFR 0.152.
 */
package com.wurmonline.server.loot;

import com.wurmonline.server.Server;
import com.wurmonline.server.creatures.Creature;
import com.wurmonline.server.items.Item;
import com.wurmonline.server.items.ItemFactory;
import com.wurmonline.server.loot.ItemCreateFunc;
import com.wurmonline.server.loot.LootItem;
import java.util.Optional;

public class DefaultItemCreateFunc
implements ItemCreateFunc {
    @Override
    public Optional<Item> create(Creature victim, Creature receiver, LootItem lootItem) {
        return ItemFactory.createItemOptional(lootItem.getItemTemplateId(), 50.0f + Server.rand.nextFloat() * 40.0f, victim.getName());
    }
}


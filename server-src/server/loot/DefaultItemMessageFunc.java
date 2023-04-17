/*
 * Decompiled with CFR 0.152.
 */
package com.wurmonline.server.loot;

import com.wurmonline.server.creatures.Creature;
import com.wurmonline.server.items.Item;
import com.wurmonline.server.loot.ItemMessageFunc;

public class DefaultItemMessageFunc
implements ItemMessageFunc {
    @Override
    public void message(Creature victim, Creature receiver, Item item) {
        receiver.getCommunicator().sendSafeServerMessage("You loot " + item.getNameWithGenus() + " from the corpse.", (byte)2);
        if (receiver.getCurrentTile() != null) {
            receiver.getCurrentTile().broadCastAction(receiver.getName() + " picks up " + item.getNameWithGenus() + " from the corpse.", receiver, false);
        }
    }
}


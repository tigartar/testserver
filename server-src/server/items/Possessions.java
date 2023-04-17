/*
 * Decompiled with CFR 0.152.
 */
package com.wurmonline.server.items;

import com.wurmonline.server.FailedException;
import com.wurmonline.server.Items;
import com.wurmonline.server.NoSuchPlayerException;
import com.wurmonline.server.WurmId;
import com.wurmonline.server.creatures.Creature;
import com.wurmonline.server.creatures.NoSuchCreatureException;
import com.wurmonline.server.items.Item;
import com.wurmonline.server.items.ItemFactory;
import com.wurmonline.server.items.NoSuchTemplateException;
import com.wurmonline.shared.constants.CounterTypes;
import edu.umd.cs.findbugs.annotations.Nullable;
import java.io.IOException;
import java.util.logging.Logger;

public final class Possessions
implements CounterTypes {
    private final Item inventory;
    @Nullable
    private Creature owner;
    private static final Logger logger = Logger.getLogger(Possessions.class.getName());

    public Possessions(Creature aOwner) throws NoSuchTemplateException, FailedException, NoSuchPlayerException, NoSuchCreatureException {
        this.owner = aOwner;
        this.inventory = this.owner.isPlayer() ? ItemFactory.createItem(0, 100.0f, null) : ItemFactory.createInventory(this.owner.getWurmId(), (short)48, 100.0f);
        assert (this.inventory != null);
        this.inventory.setOwner(aOwner.getWurmId(), true);
    }

    public Possessions(Creature aOwner, long aInventoryId) throws Exception {
        this.owner = aOwner;
        if (!this.owner.isPlayer()) {
            if (WurmId.getType(aInventoryId) == 19) {
                this.inventory = ItemFactory.createInventory(this.owner.getWurmId(), (short)48, 100.0f);
                this.inventory.setOwner(this.owner.getWurmId(), true);
                this.inventory.getContainedItems();
            } else {
                Item invent = Items.getItem(aInventoryId);
                this.inventory = ItemFactory.createInventory(this.owner.getWurmId(), (short)48, 100.0f);
                this.inventory.setOwner(this.owner.getWurmId(), true);
                Items.destroyItem(invent.getWurmId());
                aOwner.getStatus().setInventoryId(this.inventory.getWurmId());
            }
        } else {
            this.inventory = Items.getItem(aInventoryId);
        }
    }

    public Item getInventory() {
        return this.inventory;
    }

    public void clearOwner() {
        this.owner = null;
    }

    public void save() throws IOException {
    }

    public void sleep(boolean epicServer) throws IOException {
        this.inventory.sleep(this.owner, epicServer);
    }
}


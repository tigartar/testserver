/*
 * Decompiled with CFR 0.152.
 */
package com.wurmonline.server.items;

import com.wurmonline.server.Items;
import com.wurmonline.server.creatures.Creature;
import com.wurmonline.server.items.Item;

public class CreationRequirement {
    private final int resourceTemplateId;
    private final int resourceNumber;
    private float qualityLevelNeeded;
    private float maxDamageAllowed = 50.0f;
    private int volumeNeeded = 0;
    private final boolean consumed;
    private int distance = 0;
    private final int number;

    public CreationRequirement(int aNumber, int aResourceTemplateId, int aResourceNumber, boolean aConsume) {
        this.resourceTemplateId = aResourceTemplateId;
        this.resourceNumber = aResourceNumber;
        this.consumed = aConsume;
        this.number = aNumber;
    }

    public final int getNumber() {
        return this.number;
    }

    final void setQualityLevelNeeded(float needed) {
        this.qualityLevelNeeded = needed;
    }

    final void setMaxDamageAllowed(float allowed) {
        this.maxDamageAllowed = allowed;
    }

    int getDistance() {
        return this.distance;
    }

    void setDistance(int aDistance) {
        this.distance = aDistance;
    }

    final void setVolumeNeeded(int volume) {
        this.volumeNeeded = volume;
    }

    public final boolean willBeConsumed() {
        return this.consumed;
    }

    public final int getVolumeNeeded() {
        return this.volumeNeeded;
    }

    public final int getResourceTemplateId() {
        return this.resourceTemplateId;
    }

    public final int getResourceNumber() {
        return this.resourceNumber;
    }

    public final float getQualityLevelNeeded() {
        return this.qualityLevelNeeded;
    }

    public final float getMaxDamageAllowed() {
        return this.maxDamageAllowed;
    }

    boolean fill(Creature performer, Item creation) {
        if (this.canBeFilled(performer)) {
            int found = 0;
            Item inventory = performer.getInventory();
            Item[] items = inventory.getAllItems(false);
            for (int i = 0; i < items.length; ++i) {
                if (items[i].getTemplateId() != this.resourceTemplateId) continue;
                Items.destroyItem(items[i].getWurmId());
                if (++found != this.resourceNumber) continue;
                return true;
            }
            Item body = performer.getBody().getBodyItem();
            items = body.getAllItems(false);
            for (int i = 0; i < items.length; ++i) {
                if (items[i].getTemplateId() != this.resourceTemplateId) continue;
                Items.destroyItem(items[i].getWurmId());
                if (++found != this.resourceNumber) continue;
                return true;
            }
        }
        return false;
    }

    final boolean canBeFilled(Creature performer) {
        int found = 0;
        Item inventory = performer.getInventory();
        Item[] items = inventory.getAllItems(false);
        for (int i = 0; i < items.length; ++i) {
            if (items[i].getTemplateId() != this.resourceTemplateId || ++found != this.resourceNumber) continue;
            return true;
        }
        Item body = performer.getBody().getBodyItem();
        items = body.getAllItems(false);
        for (int i = 0; i < items.length; ++i) {
            if (items[i].getTemplateId() != this.resourceTemplateId || ++found != this.resourceNumber) continue;
            return true;
        }
        return false;
    }

    final boolean canRunOnce(Creature performer) {
        Item inventory = performer.getInventory();
        Item[] items = inventory.getAllItems(false);
        for (int i = 0; i < items.length; ++i) {
            if (items[i].getTemplateId() != this.resourceTemplateId) continue;
            return true;
        }
        Item body = performer.getBody().getBodyItem();
        items = body.getAllItems(false);
        for (int i = 0; i < items.length; ++i) {
            if (items[i].getTemplateId() != this.resourceTemplateId) continue;
            return true;
        }
        return false;
    }

    final boolean runOnce(Creature performer) {
        Item inventory = performer.getInventory();
        Item[] items = inventory.getAllItems(false);
        for (int i = 0; i < items.length; ++i) {
            if (items[i].getTemplateId() != this.resourceTemplateId) continue;
            Items.destroyItem(items[i].getWurmId());
            return true;
        }
        Item body = performer.getBody().getBodyItem();
        items = body.getAllItems(false);
        for (int i = 0; i < items.length; ++i) {
            if (items[i].getTemplateId() != this.resourceTemplateId) continue;
            Items.destroyItem(items[i].getWurmId());
            return true;
        }
        return false;
    }
}


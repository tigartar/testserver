/*
 * Decompiled with CFR 0.152.
 */
package com.wurmonline.server.items;

import com.wurmonline.server.Items;
import com.wurmonline.server.MiscConstants;
import com.wurmonline.server.items.Item;
import com.wurmonline.server.items.ItemFactory;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.logging.Logger;

public final class Itempool
implements MiscConstants {
    private static final Logger logger = Logger.getLogger(Itempool.class.getName());
    private static final ConcurrentHashMap<Integer, ConcurrentLinkedQueue<Item>> recycleds = new ConcurrentHashMap();
    private static final int MAX_ITEMS_IN_POOL = 200;

    private Itempool() {
    }

    public static void checkRecycledItems() {
    }

    static void addRecycledItem(Item item) {
        ConcurrentLinkedQueue<Item> dset = Itempool.getRecycledItemForTemplateID(item.getTemplateId());
        if (dset == null) {
            dset = new ConcurrentLinkedQueue();
            recycleds.put(item.getTemplateId(), dset);
        }
        if (dset.size() >= 200) {
            Items.decay(item.getWurmId(), item.getDbStrings());
        } else {
            dset.add(item);
        }
    }

    private static ConcurrentLinkedQueue<Item> getRecycledItemForTemplateID(int aTemplateId) {
        ConcurrentLinkedQueue<Item> dset = recycleds.get(aTemplateId);
        return dset;
    }

    public static void returnRecycledItem(Item item) {
        item.setZoneId(-10, true);
        item.setBanked(true);
        item.data = null;
        item.ownerId = -10L;
        item.lastOwner = -10L;
        item.parentId = -10L;
        ItemFactory.clearData(item.id);
        ConcurrentLinkedQueue<Item> dset = Itempool.getRecycledItemForTemplateID(item.getTemplateId());
        if (dset == null) {
            dset = new ConcurrentLinkedQueue();
            recycleds.put(item.getTemplateId(), dset);
        }
        if (dset.size() >= 200) {
            Items.decay(item.getWurmId(), item.getDbStrings());
        } else {
            item.setSettings(0);
            item.setRealTemplate(-10);
            dset.add(item);
        }
    }

    static Item getRecycledItem(int templateId, float qualityLevel) {
        Item toReturn = null;
        ConcurrentLinkedQueue<Item> dset = Itempool.getRecycledItemForTemplateID(templateId);
        if (dset == null) {
            return null;
        }
        toReturn = dset.poll();
        if (toReturn == null) {
            return toReturn;
        }
        toReturn.setBanked(false);
        toReturn.deleted = false;
        toReturn.setSettings(0);
        toReturn.setRealTemplate(-10);
        Items.putItem(toReturn);
        return toReturn;
    }

    public static void deleteItem(int templateId, long wurmid) {
        Item[] items;
        ConcurrentLinkedQueue<Item> dset = Itempool.getRecycledItemForTemplateID(templateId);
        if (dset == null || dset.isEmpty()) {
            return;
        }
        for (Item i : items = dset.toArray(new Item[0])) {
            if (i.getWurmId() != wurmid) continue;
            dset.remove(i);
            return;
        }
    }
}


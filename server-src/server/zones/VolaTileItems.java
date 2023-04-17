/*
 * Decompiled with CFR 0.152.
 */
package com.wurmonline.server.zones;

import com.wurmonline.server.Features;
import com.wurmonline.server.Items;
import com.wurmonline.server.items.Item;
import com.wurmonline.server.structures.Structure;
import com.wurmonline.server.villages.Village;
import com.wurmonline.server.zones.VolaTile;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Logger;

public final class VolaTileItems {
    private static final Logger logger = Logger.getLogger(VolaTileItems.class.getName());
    private int[] itemsPerLevel = new int[1];
    private int[] decoItemsPerLevel = new int[1];
    private final int PILECOUNT = 3;
    private boolean hasFire = false;
    private final int maxFloorLevel = 20;
    private ConcurrentHashMap<Integer, Item> pileItems = new ConcurrentHashMap();
    private ConcurrentHashMap<Integer, Item> onePerTileItems = new ConcurrentHashMap();
    private ConcurrentHashMap<Integer, Set<Item>> fourPerTileItems = new ConcurrentHashMap();
    private Set<Item> allItems = new HashSet<Item>();
    private Set<Item> alwaysPoll;

    private void incrementItemsOnLevelByOne(int level) {
        if (this.itemsPerLevel.length > level) {
            int n = level;
            this.itemsPerLevel[n] = this.itemsPerLevel[n] + 1;
        }
    }

    private void incrementDecoItemsOnLevelByOne(int level) {
        if (this.decoItemsPerLevel.length > level) {
            int n = level;
            this.decoItemsPerLevel[n] = this.decoItemsPerLevel[n] + 1;
        }
    }

    private void decrementItemsOnLevelByOne(int level) {
        if (this.itemsPerLevel.length > level && this.itemsPerLevel[level] > 0) {
            int n = level;
            this.itemsPerLevel[n] = this.itemsPerLevel[n] - 1;
        }
    }

    private void decrementDecoItemsOnLevelByOne(int level) {
        if (this.decoItemsPerLevel.length > level && this.decoItemsPerLevel[level] > 0) {
            int n = level;
            this.decoItemsPerLevel[n] = this.decoItemsPerLevel[n] - 1;
        }
    }

    public final boolean addItem(Item item, boolean starting) {
        if (!this.allItems.contains(item)) {
            int fl;
            if (item.isFire()) {
                Item c;
                if (this.hasFire && item.getTemplateId() == 37 && (c = this.getCampfire(false)) != null) {
                    int maxTemp = 30000;
                    c.setTemperature((short)Math.min(30000, c.getTemperature() + item.getWeightGrams()));
                    Items.destroyItem(item.getWurmId());
                    return false;
                }
                this.hasFire = true;
            }
            if (item.isOnePerTile()) {
                this.onePerTileItems.put(item.getFloorLevel(), item);
            }
            if (item.isFourPerTile()) {
                Set<Item> itemSet = this.fourPerTileItems.get(item.getFloorLevel());
                if (itemSet == null) {
                    itemSet = new HashSet<Item>();
                }
                int count = this.getFourPerTileCount(item.getFloorLevel());
                if (item.isPlanted() && count >= 4) {
                    logger.info("Unplanted " + item.getName() + " (" + item.getWurmId() + ") as tile " + item.getTileX() + "," + item.getTileY() + " already has " + count + " items");
                    item.setIsPlanted(false);
                }
                itemSet.add(item);
                this.fourPerTileItems.put(item.getFloorLevel(), itemSet);
            }
            if ((fl = this.trimFloorLevel(item.getFloorLevel())) + 1 > this.itemsPerLevel.length) {
                this.itemsPerLevel = this.createNewLevelArrayAt(fl + 1);
            }
            if (item.isDecoration()) {
                if (fl + 1 > this.decoItemsPerLevel.length) {
                    this.decoItemsPerLevel = this.createNewDecoLevelArrayAt(fl + 1);
                }
                this.incrementDecoItemsOnLevelByOne(fl);
            }
            this.allItems.add(item);
            this.incrementItemsOnLevelByOne(fl);
            if (item.isTent()) {
                Items.addTent(item);
            }
            if (item.isRoadMarker() && item.isPlanted() && Features.Feature.HIGHWAYS.isEnabled()) {
                Items.addMarker(item);
            }
            if (item.getTemplateId() == 677 && item.isPlanted()) {
                Items.addGmSign(item);
            }
            if (item.getTemplateId() == 1309 && item.isPlanted() && Features.Feature.HIGHWAYS.isEnabled()) {
                Items.addWagonerContainer(item);
            }
            if (item.isAlwaysPoll()) {
                this.addAlwaysPollItem(item);
            }
            if (item.isSpawnPoint()) {
                Items.addSpawn(item);
            }
            return true;
        }
        return false;
    }

    private final int[] createNewLevelArrayAt(int floorLevels) {
        int[] newArr = new int[floorLevels];
        for (int level = 0; level < floorLevels; ++level) {
            if (level >= this.itemsPerLevel.length) continue;
            newArr[level] = this.itemsPerLevel[level];
        }
        return newArr;
    }

    private final int[] createNewDecoLevelArrayAt(int floorLevels) {
        int[] newArr = new int[floorLevels];
        for (int level = 0; level < floorLevels; ++level) {
            if (level >= this.decoItemsPerLevel.length) continue;
            newArr[level] = this.decoItemsPerLevel[level];
        }
        return newArr;
    }

    public final boolean contains(Item item) {
        return this.allItems.contains(item);
    }

    public final boolean removeItem(Item item) {
        if (item.isAlwaysPoll()) {
            this.removeAlwaysPollItem(item);
        }
        int fl = this.trimFloorLevel(item.getFloorLevel());
        this.decrementItemsOnLevelByOne(fl);
        if (item.isDecoration()) {
            this.decrementDecoItemsOnLevelByOne(fl);
        }
        this.allItems.remove(item);
        if (item.isFire()) {
            this.hasFire = this.stillHasFire();
        }
        if (item.isOnePerTile()) {
            this.onePerTileItems.remove(item.getFloorLevel());
        }
        if (item.isTent()) {
            Items.removeTent(item);
        }
        if (item.isSpawnPoint()) {
            Items.removeSpawn(item);
        }
        return this.isEmpty();
    }

    final void destroy(VolaTile toSendTo) {
        for (Item i : this.allItems) {
            toSendTo.sendRemoveItem(i);
        }
        for (Item pile : this.pileItems.values()) {
            toSendTo.sendRemoveItem(pile);
        }
    }

    void moveToNewFloorLevel(Item item, int oldFloorLevel) {
        if (item.getFloorLevel() != oldFloorLevel) {
            int fl = this.trimFloorLevel(item.getFloorLevel());
            int old = this.trimFloorLevel(oldFloorLevel);
            if (fl + 1 > this.itemsPerLevel.length) {
                this.itemsPerLevel = this.createNewLevelArrayAt(fl + 1);
            }
            this.incrementItemsOnLevelByOne(fl);
            this.decrementItemsOnLevelByOne(old);
            if (item.isDecoration()) {
                if (fl + 1 > this.decoItemsPerLevel.length) {
                    this.decoItemsPerLevel = this.createNewDecoLevelArrayAt(fl + 1);
                }
                this.incrementDecoItemsOnLevelByOne(fl);
                this.decrementDecoItemsOnLevelByOne(old);
            }
        }
    }

    boolean movePileItemToNewFloorLevel(Item item, int oldFloorLevel) {
        if (item.getFloorLevel() != oldFloorLevel) {
            Item newPile;
            Item oldPile = this.pileItems.get(oldFloorLevel);
            if (oldPile == item) {
                this.pileItems.remove(oldFloorLevel);
            }
            if ((newPile = this.pileItems.get(item.getFloorLevel())) == null) {
                this.pileItems.put(item.getFloorLevel(), item);
            } else if (newPile != item) {
                return true;
            }
        }
        return false;
    }

    final Item[] getPileItems() {
        return this.pileItems.values().toArray(new Item[this.pileItems.size()]);
    }

    private boolean stillHasFire() {
        return this.getCampfire(true) != null;
    }

    protected final boolean hasOnePerTileItem(int floorLevel) {
        return this.onePerTileItems.get(floorLevel) != null;
    }

    protected final Item getOnePerTileItem(int floorLevel) {
        return this.onePerTileItems.get(floorLevel);
    }

    protected final int getFourPerTileCount(int floorLevel) {
        Set<Item> itemSet = this.fourPerTileItems.get(floorLevel);
        if (itemSet == null) {
            return 0;
        }
        int count = 0;
        for (Item item : itemSet) {
            if (item.isPlanted()) {
                ++count;
            }
            if (item.getTemplateId() != 1311) continue;
            ++count;
        }
        return count;
    }

    public final boolean hasFire() {
        return this.hasFire;
    }

    public final Item[] getAllItemsAsArray() {
        return this.allItems.toArray(new Item[this.allItems.size()]);
    }

    public final Set<Item> getAllItemsAsSet() {
        return this.allItems;
    }

    private final int trimFloorLevel(int floorLevel) {
        return Math.min(20, Math.max(0, floorLevel));
    }

    public final int getNumberOfItems(int floorLevel) {
        if (this.itemsPerLevel == null) {
            return 0;
        }
        if (this.itemsPerLevel.length - 1 < this.trimFloorLevel(floorLevel)) {
            return 0;
        }
        return this.itemsPerLevel[this.trimFloorLevel(floorLevel)];
    }

    public final int getNumberOfDecorations(int floorLevel) {
        if (this.decoItemsPerLevel == null) {
            return 0;
        }
        if (this.decoItemsPerLevel.length - 1 < this.trimFloorLevel(floorLevel)) {
            return 0;
        }
        return this.decoItemsPerLevel[this.trimFloorLevel(floorLevel)];
    }

    void poll(boolean pollItems, int seed, boolean lava, Structure structure, boolean surfaced, Village village, long now) {
        block4: {
            block3: {
                if (!pollItems) break block3;
                Item[] lTempItems = this.getAllItemsAsArray();
                for (int x = 0; x < lTempItems.length; ++x) {
                    if (lava && !lTempItems[x].isIndestructible() && !lTempItems[x].isHugeAltar()) {
                        lTempItems[x].setDamage(lTempItems[x].getDamage() + 1.0f);
                        continue;
                    }
                    lTempItems[x].poll(structure != null && structure.isFinished() || !surfaced, village != null, 1L);
                }
                break block4;
            }
            if (this.alwaysPoll == null) break block4;
            Item[] lTempItems = this.alwaysPoll.toArray(new Item[this.alwaysPoll.size()]);
            for (int x = 0; x < lTempItems.length; ++x) {
                if (lTempItems[x].poll(structure != null && structure.isFinished() || !surfaced, village != null, seed) || !lava) continue;
                lTempItems[x].setDamage(lTempItems[x].getDamage() + 0.1f);
            }
        }
    }

    private void addAlwaysPollItem(Item item) {
        if (this.alwaysPoll == null) {
            this.alwaysPoll = new HashSet<Item>();
        }
        this.alwaysPoll.add(item);
    }

    private void removeAlwaysPollItem(Item item) {
        if (this.alwaysPoll != null) {
            this.alwaysPoll.remove(item);
            if (this.alwaysPoll.isEmpty()) {
                this.alwaysPoll = null;
            }
        }
    }

    final void removePileItem(int floorLevel) {
        this.pileItems.remove(floorLevel);
    }

    final boolean checkIfCreatePileItem(int floorLevel) {
        if (this.itemsPerLevel.length - 1 < this.trimFloorLevel(floorLevel)) {
            return false;
        }
        int decoItems = 0;
        if (this.decoItemsPerLevel.length > this.trimFloorLevel(floorLevel)) {
            decoItems = this.decoItemsPerLevel[this.trimFloorLevel(floorLevel)];
        }
        if (this.itemsPerLevel[this.trimFloorLevel(floorLevel)] <= decoItems + 3 - 1) {
            return false;
        }
        return this.itemsPerLevel[this.trimFloorLevel(floorLevel)] >= 3;
    }

    final boolean checkIfRemovePileItem(int floorLevel) {
        if (this.itemsPerLevel.length - 1 < this.trimFloorLevel(floorLevel)) {
            return false;
        }
        int decoItems = 0;
        if (this.decoItemsPerLevel.length > this.trimFloorLevel(floorLevel)) {
            decoItems = this.decoItemsPerLevel[this.trimFloorLevel(floorLevel)];
        }
        return this.itemsPerLevel[this.trimFloorLevel(floorLevel)] < decoItems + 3;
    }

    final Item getPileItem(int floorLevel) {
        return this.pileItems.get(floorLevel);
    }

    final void addPileItem(Item pile) {
        this.pileItems.put(pile.getFloorLevel(), pile);
    }

    final Item getCampfire(boolean requiresBurning) {
        for (Item i : this.allItems) {
            if (i.getTemplateId() != 37 || requiresBurning && i.getTemperature() < 1000) continue;
            return i;
        }
        return null;
    }

    public boolean isEmpty() {
        return this.allItems == null || this.allItems.size() == 0;
    }
}


/*
 * Decompiled with CFR 0.152.
 */
package com.wurmonline.server.loot;

import com.wurmonline.server.Server;
import com.wurmonline.server.creatures.Creature;
import com.wurmonline.server.loot.LootPool;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;

public class LootTable {
    private List<LootPool> lootPools = new ArrayList<LootPool>();

    public List<LootPool> getLootPools() {
        return this.lootPools;
    }

    public LootTable setLootPools(List<LootPool> lootPools) {
        this.lootPools = lootPools;
        return this;
    }

    public LootTable setLootPools(LootPool[] pools) {
        return this.setLootPools(Arrays.asList(pools));
    }

    public LootTable addLootPools(List<LootPool> lootPools) {
        this.lootPools.addAll(lootPools);
        return this;
    }

    public LootTable addLootPools(LootPool[] pools) {
        return this.addLootPools(Arrays.asList(pools));
    }

    public void awardAll(Creature victim, HashSet<Creature> receivers) {
        this.lootPools.forEach(pool -> pool.awardLootItem(victim, receivers));
    }

    public void awardOne(Creature victim, Creature receiver) {
        this.lootPools.forEach(pool -> pool.awardLootItem(victim, receiver));
    }

    public static byte rollForRarity(Creature receiver) {
        HashMap<Integer, Byte> chances = new HashMap<Integer, Byte>();
        chances.put(10000, (byte)3);
        chances.put(1000, (byte)2);
        chances.put(100, (byte)1);
        ArrayList keys = new ArrayList(chances.keySet());
        keys.sort(Comparator.reverseOrder());
        Iterator iterator = keys.iterator();
        while (iterator.hasNext()) {
            int c = (Integer)iterator.next();
            if (Server.rand.nextInt(c) != 0) continue;
            receiver.getCommunicator().sendRarityEvent();
            return (Byte)chances.get(c);
        }
        return 0;
    }
}


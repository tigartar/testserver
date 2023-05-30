package com.wurmonline.server.loot;

import com.wurmonline.server.Server;
import com.wurmonline.server.creatures.Creature;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;

public class LootTable {
   private List<LootPool> lootPools = new ArrayList<>();

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
      Map<Integer, Byte> chances = new HashMap<>();
      chances.put(10000, (byte)3);
      chances.put(1000, (byte)2);
      chances.put(100, (byte)1);
      List<Integer> keys = new ArrayList<>(chances.keySet());
      keys.sort(Comparator.reverseOrder());

      for(int c : keys) {
         if (Server.rand.nextInt(c) == 0) {
            receiver.getCommunicator().sendRarityEvent();
            return chances.get(c);
         }
      }

      return 0;
   }
}

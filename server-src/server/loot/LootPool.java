package com.wurmonline.server.loot;

import com.wurmonline.server.behaviours.MethodsItems;
import com.wurmonline.server.creatures.Creature;
import com.wurmonline.server.items.Item;
import com.wurmonline.server.items.Materials;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Random;
import java.util.logging.Logger;

public class LootPool {
   protected static final Logger logger = Logger.getLogger(LootPool.class.getName());
   private String name;
   private double lootPoolChance;
   private boolean groupLoot;
   private List<LootItem> lootItems = new ArrayList<>();
   private Random random = new Random();
   private List<Integer> excludeIds = new ArrayList<>();
   private List<Integer> includeIds = new ArrayList<>();
   private ActiveFunc activeFunc = new DefaultActiveFunc();
   private ItemMessageFunc itemLootMessageFunc = new DefaultItemMessageFunc();
   private LootPoolChanceFunc lootPoolChanceFunc = new PercentChanceLootPoolChanceFunc();
   private LootItemFunc itemFunc = new PercentChanceLootItemFunc();

   public LootPool() {
   }

   public LootPool(int aChance) {
      this.lootPoolChance = (double)aChance;
   }

   public LootPool(int aChance, ActiveFunc aActiveFunc) {
      this(aChance);
      this.activeFunc = aActiveFunc;
   }

   public List<LootItem> getLootItems() {
      return this.lootItems;
   }

   public LootPool setLootItems(List<LootItem> items) {
      this.lootItems = items;
      return this;
   }

   public Random getRandom() {
      return this.random;
   }

   public String getName() {
      return this.name;
   }

   public LootPool setName(String name) {
      this.name = name;
      return this;
   }

   public LootPool setItemLootMessageFunc(ItemMessageFunc itemLootMessageFunc) {
      this.itemLootMessageFunc = itemLootMessageFunc;
      return this;
   }

   public boolean isGroupLoot() {
      return this.groupLoot;
   }

   public LootPool setGroupLoot(boolean groupLoot) {
      this.groupLoot = groupLoot;
      return this;
   }

   public boolean isActive(Creature victim, Creature receiver) {
      if (this.excludeIds.contains(victim.getTemplate().getTemplateId())) {
         logger.info(
            "Skipping loot pool '"
               + this.getName()
               + "' for "
               + receiver.getName()
               + ": "
               + victim.getTemplate().getTemplateId()
               + " is excluded ("
               + victim.getTemplate().getName()
               + ")."
         );
         return false;
      } else if (this.includeIds.size() > 0 && !this.includeIds.contains(victim.getTemplate().getTemplateId())) {
         logger.info(
            "Skipping loot pool '"
               + this.getName()
               + "' for "
               + receiver.getName()
               + ": "
               + victim.getTemplate().getTemplateId()
               + " is not included ("
               + victim.getTemplate().getName()
               + ")."
         );
         return false;
      } else {
         return this.activeFunc.active(victim, receiver);
      }
   }

   public LootPool setActiveFunc(ActiveFunc func) {
      this.activeFunc = func;
      return this;
   }

   public LootPool setItemFunc(LootItemFunc func) {
      this.itemFunc = func;
      return this;
   }

   protected void awardLootItem(Creature victim, HashSet<Creature> receivers) {
      if (this.isGroupLoot()) {
         receivers.forEach(receiver -> this.awardLootItem(victim, receiver));
      } else if (receivers.size() > 0) {
         this.awardLootItem(victim, new ArrayList<>(receivers).get(this.random.nextInt(receivers.size())));
      }
   }

   protected void awardLootItem(Creature victim, Creature receiver) {
      if (this.isActive(victim, receiver)) {
         if (this.passedChanceRoll(victim, receiver)) {
            Optional<Item> item = this.itemFunc.item(victim, receiver, this).flatMap(loot -> loot.createItem(victim, receiver));
            item.ifPresent(
               i -> {
                  receiver.getInventoryOptional().ifPresent(inv -> inv.insertItem(i));
                  this.sendLootMessages(victim, receiver, i);
                  logger.info(
                     "Awarding loot "
                        + MethodsItems.getRarityName(i.getRarity())
                        + " "
                        + i.getName()
                        + " ["
                        + Materials.convertMaterialByteIntoString(i.getMaterial())
                        + "] ("
                        + i.getWurmId()
                        + ") to "
                        + receiver.getName()
                        + " ("
                        + receiver.getWurmId()
                        + ")"
                  );
               }
            );
         }
      }
   }

   public double getLootPoolChance() {
      return this.lootPoolChance;
   }

   public LootPool setLootPoolChance(double aWeight) {
      this.lootPoolChance = aWeight;
      return this;
   }

   public LootPool setLootItems(LootItem[] items) {
      return this.setLootItems(Arrays.asList(items));
   }

   public LootPool setLootPoolChanceFunc(LootPoolChanceFunc func) {
      this.lootPoolChanceFunc = func;
      return this;
   }

   public boolean passedChanceRoll(Creature victim, Creature receiver) {
      boolean chance = this.lootPoolChanceFunc.chance(victim, receiver, this);
      if (!chance) {
         logger.info("Skipping loot pool '" + this.getName() + "' for " + receiver.getName() + ": failed chance roll.");
      }

      return chance;
   }

   public void sendLootMessages(Creature victim, Creature receiver, Item item) {
      this.itemLootMessageFunc.message(victim, receiver, item);
   }

   public LootPool addIncludeIds(Integer... ids) {
      this.includeIds.addAll(Arrays.asList(ids));
      return this;
   }

   public LootPool addExcludeIds(Integer... ids) {
      this.excludeIds.addAll(Arrays.asList(ids));
      return this;
   }
}

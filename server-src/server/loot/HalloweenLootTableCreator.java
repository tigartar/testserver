package com.wurmonline.server.loot;

import com.wurmonline.server.Server;
import com.wurmonline.server.Servers;
import com.wurmonline.server.WurmCalendar;
import com.wurmonline.server.creatures.Creature;
import com.wurmonline.server.creatures.CreatureTemplate;
import com.wurmonline.server.creatures.CreatureTemplateFactory;
import com.wurmonline.server.items.Item;
import com.wurmonline.server.items.ItemFactory;
import java.util.Optional;
import java.util.logging.Logger;

public class HalloweenLootTableCreator {
   protected static final Logger logger = Logger.getLogger(LootTableCreator.class.getName());

   private static Optional<Item> itemCreator(Creature victim, Creature receiver, LootItem lootItem, byte material) {
      byte rarity = LootTable.rollForRarity(receiver);
      float quality = 50.0F + Server.rand.nextFloat() * 40.0F;
      Optional<Item> item = ItemFactory.createItemOptional(lootItem.getItemTemplateId(), quality, rarity, material, victim.getName());
      item.ifPresent(i -> {
         i.setMaterial(material);
         i.setRarity(rarity);
      });
      return item;
   }

   private static boolean lootPoolChance(Creature victim, Creature receiver, LootPool pool) {
      int r = pool.getRandom().nextInt((int)(650.0F / victim.getBaseCombatRating()));
      boolean success = victim.isUnique() || r == 0;
      if (!success) {
         logger.info("Loot Pool Chance failed '" + pool.getName() + "' for " + receiver.getName() + ": r = " + r);
      }

      return success;
   }

   private static LootPool[] createPools() {
      LootItem boneSkullMask = new LootItem()
         .setItemTemplateId(1428)
         .setItemCreateFunc((victim, receiver, lootItem) -> itemCreator(victim, receiver, lootItem, (byte)35));
      LootItem goldSkullMask = new LootItem()
         .setItemTemplateId(1428)
         .setItemCreateFunc((victim, receiver, lootItem) -> itemCreator(victim, receiver, lootItem, (byte)7));
      LootItem silverSkullMask = new LootItem()
         .setItemTemplateId(1428)
         .setItemCreateFunc((victim, receiver, lootItem) -> itemCreator(victim, receiver, lootItem, (byte)8));
      LootItem oleanderSkullMask = new LootItem()
         .setItemTemplateId(1428)
         .setItemCreateFunc((victim, receiver, lootItem) -> itemCreator(victim, receiver, lootItem, (byte)51));
      LootItem trollMask = new LootItem()
         .setItemTemplateId(1321)
         .setItemCreateFunc((victim, receiver, lootItem) -> itemCreator(victim, receiver, lootItem, (byte)0));
      LootItem witchHat = new LootItem()
         .setItemTemplateId(1429)
         .setItemCreateFunc((victim, receiver, lootItem) -> itemCreator(victim, receiver, lootItem, (byte)0))
         .setItemChance(1.0);
      LootItem pumpkinShoulders = new LootItem()
         .setItemTemplateId(1322)
         .setItemCreateFunc((victim, receiver, lootItem) -> itemCreator(victim, receiver, lootItem, (byte)0))
         .setItemChance(0.25);
      return new LootPool[]{
         new LootPool()
            .setName("halloween: shoulders & hat")
            .addExcludeIds(11, 26, 27, 23)
            .setActiveFunc((victim, receiver) -> (WurmCalendar.isHalloween() || Servers.localServer.testServer) && victim.isHunter())
            .setLootPoolChanceFunc(HalloweenLootTableCreator::lootPoolChance)
            .setLootItems(new LootItem[]{pumpkinShoulders, witchHat})
            .setItemFunc(new PercentChanceLootItemFunc()),
         new LootPool()
            .setName("halloween: troll mask & skull mask")
            .addIncludeIds(11, 27)
            .setActiveFunc((victim, receiver) -> WurmCalendar.isHalloween() || Servers.localServer.testServer)
            .setLootPoolChance(0.04)
            .setLootPoolChanceFunc(new IncreasingChanceLootPoolChanceFunc().setPercentIncrease(0.001))
            .setLootItems(new LootItem[]{boneSkullMask, goldSkullMask, silverSkullMask, oleanderSkullMask, trollMask})
            .setItemFunc(new RandomIndexLootItemFunc()),
         new LootPool()
            .setName("halloween: skull mask only")
            .addIncludeIds(26, 23)
            .setActiveFunc((victim, receiver) -> WurmCalendar.isHalloween() || Servers.localServer.testServer)
            .setLootPoolChance(0.04)
            .setLootPoolChanceFunc(new IncreasingChanceLootPoolChanceFunc().setPercentIncrease(0.001))
            .setLootItems(new LootItem[]{boneSkullMask, goldSkullMask, silverSkullMask, oleanderSkullMask})
            .setItemFunc(new RandomIndexLootItemFunc())
      };
   }

   public static void initialize() {
      LootPool[] halloweenPools = createPools();

      for(CreatureTemplate template : CreatureTemplateFactory.getInstance().getTemplates()) {
         if (template.isHunter()) {
            template.addLootPool(halloweenPools);
         }
      }
   }
}

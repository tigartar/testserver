package com.wurmonline.server.economy;

import com.wurmonline.server.Features;
import com.wurmonline.server.MiscConstants;
import com.wurmonline.server.NoSuchPlayerException;
import com.wurmonline.server.Server;
import com.wurmonline.server.Servers;
import com.wurmonline.server.creatures.Creature;
import com.wurmonline.server.creatures.Creatures;
import com.wurmonline.server.creatures.NoSuchCreatureException;
import com.wurmonline.server.items.Item;
import com.wurmonline.server.zones.VolaTile;
import java.util.logging.Level;
import java.util.logging.Logger;

public abstract class Shop implements MiscConstants {
   final long wurmid;
   long money;
   long taxPaid = 0L;
   private static final Logger logger = Logger.getLogger(Shop.class.getName());
   long ownerId = -10L;
   float priceModifier = 1.4F;
   static int numTraders = 0;
   boolean followGlobalPrice = true;
   boolean useLocalPrice = true;
   long lastPolled = System.currentTimeMillis();
   long moneyEarned = 0L;
   long moneySpent = 0L;
   long moneySpentLastMonth = 0L;
   long moneyEarnedLife = 0L;
   long moneySpentLife = 0L;
   private final LocalSupplyDemand localSupplyDemand;
   float tax = 0.0F;
   int numberOfItems = 0;
   long whenEmpty = 0L;

   Shop(long aWurmid, long aMoney) {
      this.wurmid = aWurmid;
      this.money = aMoney;
      if (!this.traderMoneyExists()) {
         this.create();
         if (aWurmid > 0L) {
            try {
               Creature c = Server.getInstance().getCreature(aWurmid);
               createShop(c);
               Economy.getEconomy().getKingsShop().setMoney(Economy.getEconomy().getKingsShop().getMoney() - aMoney);
            } catch (NoSuchCreatureException var6) {
               logger.log(Level.WARNING, "Failed to locate creature owner for shop id " + aWurmid, (Throwable)var6);
            } catch (NoSuchPlayerException var7) {
               logger.log(Level.WARNING, "Creature a player?: Failed to locate creature owner for shop id " + aWurmid, (Throwable)var7);
            }
         }
      }

      this.ownerId = -10L;
      this.localSupplyDemand = new LocalSupplyDemand(aWurmid);
      Economy.addShop(this);
   }

   Shop(long aWurmid, long aMoney, long aOwnerid) {
      this.wurmid = aWurmid;
      this.money = aMoney;
      this.ownerId = aOwnerid;
      if (aOwnerid != -10L) {
         this.numberOfItems = 0;
         this.whenEmpty = System.currentTimeMillis();
      }

      if (!this.traderMoneyExists()) {
         this.create();
      }

      this.localSupplyDemand = new LocalSupplyDemand(aWurmid);
      Economy.addShop(this);
   }

   Shop(
      long aWurmid,
      long aMoney,
      long aOwnerid,
      float aPriceMod,
      boolean aFollowGlobalPrice,
      boolean aUseLocalPrice,
      long aLastPolled,
      float aTax,
      long spentMonth,
      long spentLife,
      long earnedMonth,
      long earnedLife,
      long spentLast,
      long taxpaid,
      int _numberOfItems,
      long _whenEmpty,
      boolean aLoad
   ) {
      this.wurmid = aWurmid;
      this.money = aMoney;
      this.ownerId = aOwnerid;
      this.priceModifier = aPriceMod;
      this.followGlobalPrice = aFollowGlobalPrice;
      this.useLocalPrice = aUseLocalPrice;
      this.lastPolled = aLastPolled;
      this.localSupplyDemand = new LocalSupplyDemand(aWurmid);
      this.tax = aTax;
      this.moneySpent = spentMonth;
      this.moneyEarned = earnedMonth;
      this.moneySpentLife = spentLife;
      this.moneyEarnedLife = earnedLife;
      this.moneySpentLastMonth = spentLast;
      this.taxPaid = taxpaid;
      if (this.ownerId > 0L && _numberOfItems == 0) {
         try {
            Creature creature = Creatures.getInstance().getCreature(this.wurmid);
            Item[] invItems = creature.getInventory().getItemsAsArray();
            int noItems = 0;

            for(int x = 0; x < invItems.length; ++x) {
               if (!invItems[x].isCoin()) {
                  ++noItems;
               }
            }

            if (noItems == 0) {
               this.setMerchantData(0, _whenEmpty);
            } else {
               this.setMerchantData(noItems, 0L);
            }
         } catch (NoSuchCreatureException var33) {
            logger.log(Level.WARNING, "Merchant not loaded in time. " + var33.getMessage(), (Throwable)var33);
            this.numberOfItems = _numberOfItems;
            this.whenEmpty = _whenEmpty;
         }
      } else {
         this.numberOfItems = _numberOfItems;
         this.whenEmpty = _whenEmpty;
      }

      Economy.addShop(this);
      if (this.ownerId <= 0L) {
         ++numTraders;
      }
   }

   private static void createShop(Creature toReturn) {
      try {
         Item inventory = toReturn.getInventory();

         for(int x = 0; x < 3; ++x) {
            Item item = Creature.createItem(143, (float)(10 + Server.rand.nextInt(40)));
            inventory.insertItem(item);
            item = Creature.createItem(509, 80.0F);
            inventory.insertItem(item);
            item = Creature.createItem(525, 80.0F);
            inventory.insertItem(item);
            item = Creature.createItem(524, 80.0F);
            inventory.insertItem(item);
            item = Creature.createItem(601, (float)(60 + Server.rand.nextInt(40)));
            inventory.insertItem(item);
            item = Creature.createItem(664, 40.0F);
            inventory.insertItem(item);
            item = Creature.createItem(665, 40.0F);
            inventory.insertItem(item);
            if (Features.Feature.NAMECHANGE.isEnabled()) {
               item = Creature.createItem(843, (float)(60 + Server.rand.nextInt(40)));
               inventory.insertItem(item);
            }

            item = Creature.createItem(666, 99.0F);
            inventory.insertItem(item);
            item = Creature.createItem(668, (float)(60 + Server.rand.nextInt(40)));
            inventory.insertItem(item);
            item = Creature.createItem(667, (float)(60 + Server.rand.nextInt(40)));
            inventory.insertItem(item);
         }

         if (!Features.Feature.BLOCKED_TRADERS.isEnabled()) {
            Item contract = Creature.createItem(299, (float)(10 + Server.rand.nextInt(80)));
            inventory.insertItem(contract);
         }

         if (Servers.localServer.PVPSERVER) {
            Item declaration = Creature.createItem(682, (float)(10 + Server.rand.nextInt(80)));
            inventory.insertItem(declaration);
         }

         Item contract = Creature.createItem(300, (float)(10 + Server.rand.nextInt(80)));
         inventory.insertItem(contract);
      } catch (Exception var4) {
         logger.log(Level.INFO, "Failed to create merchant inventory items for shop, creature: " + toReturn, (Throwable)var4);
      }
   }

   public final boolean followsGlobalPrice() {
      return this.followGlobalPrice;
   }

   public final boolean usesLocalPrice() {
      return this.useLocalPrice;
   }

   public final long getLastPolled() {
      return this.lastPolled;
   }

   public final long howLongEmpty() {
      return this.numberOfItems == 0 ? System.currentTimeMillis() - this.whenEmpty : 0L;
   }

   public final long getWurmId() {
      return this.wurmid;
   }

   public final long getMoney() {
      return this.money;
   }

   public final boolean isPersonal() {
      return this.ownerId > 0L;
   }

   public final long getOwnerId() {
      return this.ownerId;
   }

   public final float getPriceModifier() {
      return this.priceModifier;
   }

   public static final int getNumTraders() {
      return numTraders;
   }

   public final double getLocalTraderSellPrice(Item item, int currentStock, int numberSold) {
      double globalPrice = 1000000.0;
      globalPrice = (double)item.getValue();
      if (this.useLocalPrice) {
         globalPrice = this.localSupplyDemand.getPrice(item.getTemplateId(), globalPrice, numberSold, true);
      }

      return Math.max(0.0, globalPrice);
   }

   public final long getLocalTraderBuyPrice(Item item, int currentStock, int extra) {
      long globalPrice = 1L;
      globalPrice = (long)item.getValue();
      if (this.useLocalPrice) {
         globalPrice = (long)this.localSupplyDemand.getPrice(item.getTemplateId(), (double)globalPrice, extra, false);
      }

      return Math.max(0L, globalPrice);
   }

   final VolaTile getPos() {
      try {
         Creature c = Creatures.getInstance().getCreature(this.wurmid);
         return c.getCurrentTile();
      } catch (NoSuchCreatureException var2) {
         logger.log(Level.WARNING, "No creature for shop " + this.wurmid);
         return null;
      }
   }

   abstract void create();

   abstract boolean traderMoneyExists();

   public abstract void setMoney(long var1);

   public abstract void delete();

   public abstract void setPriceModifier(float var1);

   public abstract void setFollowGlobalPrice(boolean var1);

   public abstract void setUseLocalPrice(boolean var1);

   public abstract void setLastPolled(long var1);

   public abstract void setTax(float var1);

   public final float getTax() {
      return this.tax;
   }

   public final int getTaxAsInt() {
      return (int)(this.tax * 100.0F);
   }

   public float getSellRatio() {
      if (this.moneyEarned > 0L) {
         if (this.moneySpent > 0L) {
            return (float)this.moneyEarned / (float)this.moneySpent;
         }
      } else if (this.moneySpent > 0L) {
         return (float)(-this.moneySpent);
      }

      return 0.0F;
   }

   public long getMoneySpentMonth() {
      return this.moneySpent;
   }

   public long getMoneySpentLastMonth() {
      return this.moneySpentLastMonth;
   }

   public long getMoneyEarnedMonth() {
      return this.moneyEarned;
   }

   public long getMoneySpentLife() {
      return this.moneySpent;
   }

   public long getMoneyEarnedLife() {
      return this.moneyEarnedLife;
   }

   public long getTaxPaid() {
      return this.taxPaid;
   }

   public final LocalSupplyDemand getLocalSupplyDemand() {
      return this.localSupplyDemand;
   }

   public final void setMerchantData(int _numberOfItems) {
      if (_numberOfItems == 0) {
         if (this.numberOfItems == 0) {
            if (this.whenEmpty == 0L) {
               this.setMerchantData(0, this.lastPolled);
            } else {
               this.setMerchantData(0, this.whenEmpty);
            }
         } else {
            this.setMerchantData(0, System.currentTimeMillis());
         }
      } else {
         this.setMerchantData(_numberOfItems, 0L);
      }
   }

   public final int getNumberOfItems() {
      return this.numberOfItems;
   }

   public abstract void addMoneyEarned(long var1);

   public abstract void addMoneySpent(long var1);

   public abstract void resetEarnings();

   public abstract void addTax(long var1);

   public abstract void setOwner(long var1);

   public abstract void setMerchantData(int var1, long var2);
}

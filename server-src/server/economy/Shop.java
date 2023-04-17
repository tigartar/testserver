/*
 * Decompiled with CFR 0.152.
 */
package com.wurmonline.server.economy;

import com.wurmonline.server.Features;
import com.wurmonline.server.MiscConstants;
import com.wurmonline.server.NoSuchPlayerException;
import com.wurmonline.server.Server;
import com.wurmonline.server.Servers;
import com.wurmonline.server.creatures.Creature;
import com.wurmonline.server.creatures.Creatures;
import com.wurmonline.server.creatures.NoSuchCreatureException;
import com.wurmonline.server.economy.Economy;
import com.wurmonline.server.economy.LocalSupplyDemand;
import com.wurmonline.server.items.Item;
import com.wurmonline.server.zones.VolaTile;
import java.util.logging.Level;
import java.util.logging.Logger;

public abstract class Shop
implements MiscConstants {
    final long wurmid;
    long money;
    long taxPaid;
    private static final Logger logger = Logger.getLogger(Shop.class.getName());
    long ownerId;
    float priceModifier;
    static int numTraders = 0;
    boolean followGlobalPrice;
    boolean useLocalPrice;
    long lastPolled;
    long moneyEarned;
    long moneySpent;
    long moneySpentLastMonth;
    long moneyEarnedLife;
    long moneySpentLife;
    private final LocalSupplyDemand localSupplyDemand;
    float tax;
    int numberOfItems;
    long whenEmpty;

    Shop(long aWurmid, long aMoney) {
        this.taxPaid = 0L;
        this.ownerId = -10L;
        this.priceModifier = 1.4f;
        this.followGlobalPrice = true;
        this.useLocalPrice = true;
        this.lastPolled = System.currentTimeMillis();
        this.moneyEarned = 0L;
        this.moneySpent = 0L;
        this.moneySpentLastMonth = 0L;
        this.moneyEarnedLife = 0L;
        this.moneySpentLife = 0L;
        this.tax = 0.0f;
        this.numberOfItems = 0;
        this.whenEmpty = 0L;
        this.wurmid = aWurmid;
        this.money = aMoney;
        if (!this.traderMoneyExists()) {
            this.create();
            if (aWurmid > 0L) {
                try {
                    Creature c = Server.getInstance().getCreature(aWurmid);
                    Shop.createShop(c);
                    Economy.getEconomy().getKingsShop().setMoney(Economy.getEconomy().getKingsShop().getMoney() - aMoney);
                }
                catch (NoSuchCreatureException nsc) {
                    logger.log(Level.WARNING, "Failed to locate creature owner for shop id " + aWurmid, nsc);
                }
                catch (NoSuchPlayerException nsp) {
                    logger.log(Level.WARNING, "Creature a player?: Failed to locate creature owner for shop id " + aWurmid, nsp);
                }
            }
        }
        this.ownerId = -10L;
        this.localSupplyDemand = new LocalSupplyDemand(aWurmid);
        Economy.addShop(this);
    }

    Shop(long aWurmid, long aMoney, long aOwnerid) {
        this.taxPaid = 0L;
        this.ownerId = -10L;
        this.priceModifier = 1.4f;
        this.followGlobalPrice = true;
        this.useLocalPrice = true;
        this.lastPolled = System.currentTimeMillis();
        this.moneyEarned = 0L;
        this.moneySpent = 0L;
        this.moneySpentLastMonth = 0L;
        this.moneyEarnedLife = 0L;
        this.moneySpentLife = 0L;
        this.tax = 0.0f;
        this.numberOfItems = 0;
        this.whenEmpty = 0L;
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

    Shop(long aWurmid, long aMoney, long aOwnerid, float aPriceMod, boolean aFollowGlobalPrice, boolean aUseLocalPrice, long aLastPolled, float aTax, long spentMonth, long spentLife, long earnedMonth, long earnedLife, long spentLast, long taxpaid, int _numberOfItems, long _whenEmpty, boolean aLoad) {
        block7: {
            this.taxPaid = 0L;
            this.ownerId = -10L;
            this.priceModifier = 1.4f;
            this.followGlobalPrice = true;
            this.useLocalPrice = true;
            this.lastPolled = System.currentTimeMillis();
            this.moneyEarned = 0L;
            this.moneySpent = 0L;
            this.moneySpentLastMonth = 0L;
            this.moneyEarnedLife = 0L;
            this.moneySpentLife = 0L;
            this.tax = 0.0f;
            this.numberOfItems = 0;
            this.whenEmpty = 0L;
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
                    for (int x = 0; x < invItems.length; ++x) {
                        if (invItems[x].isCoin()) continue;
                        ++noItems;
                    }
                    if (noItems == 0) {
                        this.setMerchantData(0, _whenEmpty);
                        break block7;
                    }
                    this.setMerchantData(noItems, 0L);
                }
                catch (NoSuchCreatureException e) {
                    logger.log(Level.WARNING, "Merchant not loaded in time. " + e.getMessage(), e);
                    this.numberOfItems = _numberOfItems;
                    this.whenEmpty = _whenEmpty;
                }
            } else {
                this.numberOfItems = _numberOfItems;
                this.whenEmpty = _whenEmpty;
            }
        }
        Economy.addShop(this);
        if (this.ownerId <= 0L) {
            ++numTraders;
        }
    }

    private static void createShop(Creature toReturn) {
        try {
            Item inventory = toReturn.getInventory();
            for (int x = 0; x < 3; ++x) {
                Item item = Creature.createItem(143, 10 + Server.rand.nextInt(40));
                inventory.insertItem(item);
                item = Creature.createItem(509, 80.0f);
                inventory.insertItem(item);
                item = Creature.createItem(525, 80.0f);
                inventory.insertItem(item);
                item = Creature.createItem(524, 80.0f);
                inventory.insertItem(item);
                item = Creature.createItem(601, 60 + Server.rand.nextInt(40));
                inventory.insertItem(item);
                item = Creature.createItem(664, 40.0f);
                inventory.insertItem(item);
                item = Creature.createItem(665, 40.0f);
                inventory.insertItem(item);
                if (Features.Feature.NAMECHANGE.isEnabled()) {
                    item = Creature.createItem(843, 60 + Server.rand.nextInt(40));
                    inventory.insertItem(item);
                }
                item = Creature.createItem(666, 99.0f);
                inventory.insertItem(item);
                item = Creature.createItem(668, 60 + Server.rand.nextInt(40));
                inventory.insertItem(item);
                item = Creature.createItem(667, 60 + Server.rand.nextInt(40));
                inventory.insertItem(item);
            }
            if (!Features.Feature.BLOCKED_TRADERS.isEnabled()) {
                Item contract = Creature.createItem(299, 10 + Server.rand.nextInt(80));
                inventory.insertItem(contract);
            }
            if (Servers.localServer.PVPSERVER) {
                Item declaration2 = Creature.createItem(682, 10 + Server.rand.nextInt(80));
                inventory.insertItem(declaration2);
            }
            Item contract = Creature.createItem(300, 10 + Server.rand.nextInt(80));
            inventory.insertItem(contract);
        }
        catch (Exception ex) {
            logger.log(Level.INFO, "Failed to create merchant inventory items for shop, creature: " + toReturn, ex);
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
        if (this.numberOfItems == 0) {
            return System.currentTimeMillis() - this.whenEmpty;
        }
        return 0L;
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
        globalPrice = item.getValue();
        if (this.useLocalPrice) {
            globalPrice = this.localSupplyDemand.getPrice(item.getTemplateId(), globalPrice, numberSold, true);
        }
        return Math.max(0.0, globalPrice);
    }

    public final long getLocalTraderBuyPrice(Item item, int currentStock, int extra) {
        long globalPrice = 1L;
        globalPrice = item.getValue();
        if (this.useLocalPrice) {
            globalPrice = (long)this.localSupplyDemand.getPrice(item.getTemplateId(), globalPrice, extra, false);
        }
        return Math.max(0L, globalPrice);
    }

    final VolaTile getPos() {
        try {
            Creature c = Creatures.getInstance().getCreature(this.wurmid);
            return c.getCurrentTile();
        }
        catch (NoSuchCreatureException nsc) {
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
        return (int)(this.tax * 100.0f);
    }

    public float getSellRatio() {
        if (this.moneyEarned > 0L) {
            if (this.moneySpent > 0L) {
                return (float)this.moneyEarned / (float)this.moneySpent;
            }
        } else if (this.moneySpent > 0L) {
            return -this.moneySpent;
        }
        return 0.0f;
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


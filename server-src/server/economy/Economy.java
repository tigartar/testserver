/*
 * Decompiled with CFR 0.152.
 */
package com.wurmonline.server.economy;

import com.wurmonline.server.FailedException;
import com.wurmonline.server.MiscConstants;
import com.wurmonline.server.Server;
import com.wurmonline.server.Servers;
import com.wurmonline.server.TimeConstants;
import com.wurmonline.server.creatures.Creature;
import com.wurmonline.server.creatures.Creatures;
import com.wurmonline.server.creatures.NoSuchCreatureException;
import com.wurmonline.server.economy.Change;
import com.wurmonline.server.economy.DbEconomy;
import com.wurmonline.server.economy.MonetaryConstants;
import com.wurmonline.server.economy.Shop;
import com.wurmonline.server.economy.SupplyDemand;
import com.wurmonline.server.items.Item;
import com.wurmonline.server.items.ItemFactory;
import com.wurmonline.server.items.NoSuchTemplateException;
import com.wurmonline.server.zones.VolaTile;
import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.annotation.concurrent.GuardedBy;

public abstract class Economy
implements MonetaryConstants,
MiscConstants,
TimeConstants {
    static long goldCoins;
    static long lastPolledTraders;
    static long copperCoins;
    static long silverCoins;
    static long ironCoins;
    private static final LinkedList<Item> goldOnes;
    private static final LinkedList<Item> goldFives;
    private static final LinkedList<Item> goldTwentys;
    private static final LinkedList<Item> silverOnes;
    private static final LinkedList<Item> silverFives;
    private static final LinkedList<Item> silverTwentys;
    private static final LinkedList<Item> copperOnes;
    private static final LinkedList<Item> copperFives;
    private static final LinkedList<Item> copperTwentys;
    private static final LinkedList<Item> ironOnes;
    private static final LinkedList<Item> ironFives;
    private static final LinkedList<Item> ironTwentys;
    private static final Logger logger;
    private static final Logger moneylogger;
    final int id;
    private static final Map<Integer, SupplyDemand> supplyDemand;
    @GuardedBy(value="SHOPS_RW_LOCK")
    private static final Map<Long, Shop> shops;
    private static final ReentrantReadWriteLock SHOPS_RW_LOCK;
    private static Economy economy;
    private static final int minTraderDistance = 64;

    public static Economy getEconomy() {
        if (economy == null) {
            long start = System.nanoTime();
            try {
                economy = new DbEconomy(Servers.localServer.id);
            }
            catch (IOException iox) {
                logger.log(Level.WARNING, "Failed to create economy: " + iox.getMessage(), iox);
                Server.getInstance().shutDown();
            }
            logger.log(Level.INFO, "Loading economy took " + (float)(System.nanoTime() - start) / 1000000.0f + " ms.");
        }
        return economy;
    }

    Economy(int aEconomy) throws IOException {
        goldCoins = 0L;
        copperCoins = 0L;
        silverCoins = 0L;
        ironCoins = 0L;
        this.id = aEconomy;
        this.initialize();
    }

    public long getGold() {
        return goldCoins;
    }

    public long getSilver() {
        return silverCoins;
    }

    public long getCopper() {
        return copperCoins;
    }

    public long getIron() {
        return ironCoins;
    }

    public static final int getValueFor(int coinType) {
        switch (coinType) {
            case 50: {
                return 100;
            }
            case 54: {
                return 500;
            }
            case 58: {
                return 2000;
            }
            case 51: {
                return 1;
            }
            case 55: {
                return 5;
            }
            case 59: {
                return 20;
            }
            case 52: {
                return 10000;
            }
            case 56: {
                return 50000;
            }
            case 60: {
                return 200000;
            }
            case 53: {
                return 1000000;
            }
            case 57: {
                return 5000000;
            }
            case 61: {
                return 20000000;
            }
        }
        return 0;
    }

    LinkedList<Item> getListForCointype(int type) {
        switch (type) {
            case 50: {
                return copperOnes;
            }
            case 54: {
                return copperFives;
            }
            case 58: {
                return copperTwentys;
            }
            case 51: {
                return ironOnes;
            }
            case 55: {
                return ironFives;
            }
            case 59: {
                return ironTwentys;
            }
            case 52: {
                return silverOnes;
            }
            case 56: {
                return silverFives;
            }
            case 60: {
                return silverTwentys;
            }
            case 53: {
                return goldOnes;
            }
            case 57: {
                return goldFives;
            }
            case 61: {
                return goldTwentys;
            }
        }
        logger.log(Level.WARNING, "Found no list for type " + type);
        return new LinkedList<Item>();
    }

    public void returnCoin(Item coin, String message) {
        this.returnCoin(coin, message, false);
    }

    public void returnCoin(Item coin, String message, boolean dontLog) {
        if (!dontLog) {
            this.transaction(coin.getWurmId(), coin.getOwnerId(), this.id, message, coin.getValue());
        }
        coin.setTradeWindow(null);
        coin.setOwner(-10L, false);
        coin.setLastOwnerId(-10L);
        coin.setZoneId(-10, true);
        coin.setParentId(-10L, true);
        coin.setRarity((byte)0);
        coin.setBanked(true);
        int templateid = coin.getTemplateId();
        LinkedList<Item> toAdd = this.getListForCointype(templateid);
        toAdd.add(coin);
    }

    public Item[] getCoinsFor(long value) {
        if (value > 0L) {
            try {
                if (value >= 1000000L) {
                    return this.getGoldTwentyCoinsFor(value, new HashSet<Item>());
                }
                if (value >= 10000L) {
                    return this.getSilverTwentyCoinsFor(value, new HashSet<Item>());
                }
                if (value >= 100L) {
                    return this.getCopperTwentyCoinsFor(value, new HashSet<Item>());
                }
                return this.getIronTwentyCoinsFor(value, new HashSet<Item>());
            }
            catch (FailedException fe) {
                logger.log(Level.WARNING, "Failed to create coins: " + fe.getMessage(), fe);
            }
            catch (NoSuchTemplateException nst) {
                logger.log(Level.WARNING, "Failed to create coins: " + nst.getMessage(), nst);
            }
        }
        return new Item[0];
    }

    private Item[] getGoldTwentyCoinsFor(long value, Set<Item> items) throws FailedException, NoSuchTemplateException {
        if (value > 0L) {
            long num = value / 20000000L;
            if (items == null) {
                items = new HashSet<Item>();
            }
            for (long x = 0L; x < num; ++x) {
                Item coin = null;
                if (goldTwentys.size() > 0) {
                    coin = goldTwentys.removeFirst();
                } else {
                    coin = ItemFactory.createItem(61, Server.rand.nextFloat() * 100.0f, null);
                    this.updateCreatedGold(goldCoins += 20L);
                    logger.log(Level.INFO, "CREATING COIN GOLD20 " + coin.getWurmId(), new Exception());
                }
                items.add(coin);
                coin.setBanked(false);
                value -= 20000000L;
            }
        }
        return this.getGoldFiveCoinsFor(value, items);
    }

    public Shop[] getShops() {
        SHOPS_RW_LOCK.readLock().lock();
        try {
            Shop[] shopArray = shops.values().toArray(new Shop[shops.size()]);
            return shopArray;
        }
        finally {
            SHOPS_RW_LOCK.readLock().unlock();
        }
    }

    public static Shop[] getTraders() {
        HashMap<Long, Shop> traders = new HashMap<Long, Shop>();
        SHOPS_RW_LOCK.readLock().lock();
        try {
            for (Shop s : shops.values()) {
                if (s.isPersonal() || s.getWurmId() == 0L) continue;
                traders.put(s.getWurmId(), s);
            }
            Shop[] shopArray = traders.values().toArray(new Shop[traders.size()]);
            return shopArray;
        }
        finally {
            SHOPS_RW_LOCK.readLock().unlock();
        }
    }

    public long getShopMoney() {
        Shop[] lShops;
        long toRet = 0L;
        for (Shop lLShop : lShops = this.getShops()) {
            if (lLShop.getMoney() <= 0L) continue;
            toRet += lLShop.getMoney();
        }
        return toRet;
    }

    public void pollTraderEarnings() {
        if (System.currentTimeMillis() - lastPolledTraders > 2419200000L) {
            this.resetEarnings();
            this.updateLastPolled();
            logger.log(Level.INFO, "Economy reset earnings.");
        }
    }

    private Item[] getGoldFiveCoinsFor(long value, Set<Item> items) throws FailedException, NoSuchTemplateException {
        if (value > 0L) {
            long num = value / 5000000L;
            if (items == null) {
                items = new HashSet<Item>();
            }
            for (long x = 0L; x < num; ++x) {
                Item coin = null;
                if (goldFives.size() > 0) {
                    coin = goldFives.removeFirst();
                } else {
                    coin = ItemFactory.createItem(57, Server.rand.nextFloat() * 100.0f, null);
                    this.updateCreatedGold(goldCoins += 5L);
                    logger.log(Level.INFO, "CREATING COIN GOLD5 " + coin.getWurmId(), new Exception());
                }
                items.add(coin);
                coin.setBanked(false);
                value -= 5000000L;
            }
        }
        return this.getGoldOneCoinsFor(value, items);
    }

    private Item[] getGoldOneCoinsFor(long value, Set<Item> items) throws FailedException, NoSuchTemplateException {
        if (value > 0L) {
            long num = value / 1000000L;
            if (items == null) {
                items = new HashSet<Item>();
            }
            for (long x = 0L; x < num; ++x) {
                Item coin = null;
                if (goldOnes.size() > 0) {
                    coin = goldOnes.removeFirst();
                } else {
                    coin = ItemFactory.createItem(53, Server.rand.nextFloat() * 100.0f, null);
                    this.updateCreatedGold(++goldCoins);
                    logger.log(Level.INFO, "CREATING COIN GOLD1 " + coin.getWurmId(), new Exception());
                }
                items.add(coin);
                coin.setBanked(false);
                value -= 1000000L;
            }
        }
        return this.getSilverTwentyCoinsFor(value, items);
    }

    private Item[] getSilverTwentyCoinsFor(long value, Set<Item> items) throws FailedException, NoSuchTemplateException {
        if (value > 0L) {
            long num = value / 200000L;
            if (items == null) {
                items = new HashSet<Item>();
            }
            for (long x = 0L; x < num; ++x) {
                Item coin = null;
                if (silverTwentys.size() > 0) {
                    coin = silverTwentys.removeFirst();
                } else {
                    coin = ItemFactory.createItem(60, Server.rand.nextFloat() * 100.0f, null);
                    this.updateCreatedSilver(silverCoins += 20L);
                }
                items.add(coin);
                coin.setBanked(false);
                value -= 200000L;
            }
        }
        return this.getSilverFiveCoinsFor(value, items);
    }

    private Item[] getSilverFiveCoinsFor(long value, Set<Item> items) throws FailedException, NoSuchTemplateException {
        if (value > 0L) {
            long num = value / 50000L;
            if (items == null) {
                items = new HashSet<Item>();
            }
            for (long x = 0L; x < num; ++x) {
                Item coin = null;
                if (silverFives.size() > 0) {
                    coin = silverFives.removeFirst();
                } else {
                    coin = ItemFactory.createItem(56, Server.rand.nextFloat() * 100.0f, null);
                    this.updateCreatedSilver(silverCoins += 5L);
                }
                items.add(coin);
                coin.setBanked(false);
                value -= 50000L;
            }
        }
        return this.getSilverOneCoinsFor(value, items);
    }

    private Item[] getSilverOneCoinsFor(long value, Set<Item> items) throws FailedException, NoSuchTemplateException {
        if (value > 0L) {
            long num = value / 10000L;
            if (items == null) {
                items = new HashSet<Item>();
            }
            for (long x = 0L; x < num; ++x) {
                Item coin = null;
                if (silverOnes.size() > 0) {
                    coin = silverOnes.removeFirst();
                } else {
                    coin = ItemFactory.createItem(52, Server.rand.nextFloat() * 100.0f, null);
                    this.updateCreatedSilver(++silverCoins);
                }
                items.add(coin);
                coin.setBanked(false);
                value -= 10000L;
            }
        }
        return this.getCopperTwentyCoinsFor(value, items);
    }

    private Item[] getCopperTwentyCoinsFor(long value, Set<Item> items) throws FailedException, NoSuchTemplateException {
        if (value > 0L) {
            long num = value / 2000L;
            if (items == null) {
                items = new HashSet<Item>();
            }
            for (long x = 0L; x < num; ++x) {
                Item coin = null;
                if (copperTwentys.size() > 0) {
                    coin = copperTwentys.removeFirst();
                } else {
                    coin = ItemFactory.createItem(58, Server.rand.nextFloat() * 100.0f, null);
                    this.updateCreatedCopper(copperCoins += 20L);
                }
                items.add(coin);
                coin.setBanked(false);
                value -= 2000L;
            }
        }
        return this.getCopperFiveCoinsFor(value, items);
    }

    private Item[] getCopperFiveCoinsFor(long value, Set<Item> items) throws FailedException, NoSuchTemplateException {
        if (value > 0L) {
            long num = value / 500L;
            if (items == null) {
                items = new HashSet<Item>();
            }
            for (long x = 0L; x < num; ++x) {
                Item coin = null;
                if (copperFives.size() > 0) {
                    coin = copperFives.removeFirst();
                } else {
                    coin = ItemFactory.createItem(54, Server.rand.nextFloat() * 100.0f, null);
                    this.updateCreatedCopper(copperCoins += 5L);
                }
                items.add(coin);
                coin.setBanked(false);
                value -= 500L;
            }
        }
        return this.getCopperOneCoinsFor(value, items);
    }

    private Item[] getCopperOneCoinsFor(long value, Set<Item> items) throws FailedException, NoSuchTemplateException {
        if (value > 0L) {
            long num = value / 100L;
            if (items == null) {
                items = new HashSet<Item>();
            }
            for (long x = 0L; x < num; ++x) {
                Item coin = null;
                if (copperOnes.size() > 0) {
                    coin = copperOnes.removeFirst();
                } else {
                    coin = ItemFactory.createItem(50, Server.rand.nextFloat() * 100.0f, null);
                    this.updateCreatedCopper(++copperCoins);
                }
                items.add(coin);
                coin.setBanked(false);
                value -= 100L;
            }
        }
        return this.getIronTwentyCoinsFor(value, items);
    }

    private Item[] getIronTwentyCoinsFor(long value, Set<Item> items) throws FailedException, NoSuchTemplateException {
        if (value > 0L) {
            long num = value / 20L;
            if (items == null) {
                items = new HashSet<Item>();
            }
            for (long x = 0L; x < num; ++x) {
                Item coin = null;
                if (ironTwentys.size() > 0) {
                    coin = ironTwentys.removeFirst();
                } else {
                    coin = ItemFactory.createItem(59, Server.rand.nextFloat() * 100.0f, null);
                    this.updateCreatedIron(ironCoins += 20L);
                }
                items.add(coin);
                coin.setBanked(false);
                value -= 20L;
            }
        }
        return this.getIronFiveCoinsFor(value, items);
    }

    private Item[] getIronFiveCoinsFor(long value, Set<Item> items) throws FailedException, NoSuchTemplateException {
        if (value > 0L) {
            long num = value / 5L;
            if (items == null) {
                items = new HashSet<Item>();
            }
            for (long x = 0L; x < num; ++x) {
                Item coin = null;
                if (ironFives.size() > 0) {
                    coin = ironFives.removeFirst();
                } else {
                    coin = ItemFactory.createItem(55, Server.rand.nextFloat() * 100.0f, null);
                    this.updateCreatedIron(ironCoins += 5L);
                }
                items.add(coin);
                coin.setBanked(false);
                value -= 5L;
            }
        }
        return this.getIronOneCoinsFor(value, items);
    }

    private Item[] getIronOneCoinsFor(long value, Set<Item> items) throws FailedException, NoSuchTemplateException {
        if (value > 0L) {
            long num = value;
            if (items == null) {
                items = new HashSet<Item>();
            }
            int x = 0;
            while ((long)x < num) {
                Item coin = null;
                if (ironOnes.size() > 0) {
                    coin = ironOnes.removeFirst();
                } else {
                    coin = ItemFactory.createItem(51, Server.rand.nextFloat() * 100.0f, null);
                    this.updateCreatedIron(++ironCoins);
                }
                items.add(coin);
                coin.setBanked(false);
                --value;
                ++x;
            }
        }
        return items.toArray(new Item[items.size()]);
    }

    SupplyDemand getSupplyDemand(int itemTemplateId) {
        SupplyDemand sd = supplyDemand.get(itemTemplateId);
        if (sd == null) {
            sd = this.createSupplyDemand(itemTemplateId);
        }
        return sd;
    }

    public int getPool(int itemTemplateId) {
        SupplyDemand sd = this.getSupplyDemand(itemTemplateId);
        return sd.getPool();
    }

    public Shop getShop(Creature creature) {
        return this.getShop(creature, false);
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public Shop getShop(Creature creature, boolean destroying) {
        Shop tm = null;
        if (creature.isNpcTrader()) {
            SHOPS_RW_LOCK.readLock().lock();
            try {
                tm = shops.get(new Long(creature.getWurmId()));
            }
            finally {
                SHOPS_RW_LOCK.readLock().unlock();
            }
            if (!destroying && tm == null) {
                tm = this.createShop(creature.getWurmId());
            }
        }
        return tm;
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public Shop[] getShopsForOwner(long owner) {
        HashSet<Shop> sh = new HashSet<Shop>();
        SHOPS_RW_LOCK.readLock().lock();
        try {
            for (Shop shop : shops.values()) {
                if (shop.getOwnerId() != owner) continue;
                sh.add(shop);
            }
        }
        finally {
            SHOPS_RW_LOCK.readLock().unlock();
        }
        return sh.toArray(new Shop[sh.size()]);
    }

    public Shop getKingsShop() {
        Shop tm;
        SHOPS_RW_LOCK.readLock().lock();
        try {
            tm = shops.get(0L);
        }
        finally {
            SHOPS_RW_LOCK.readLock().unlock();
        }
        if (tm == null) {
            tm = this.createShop(0L);
        }
        return tm;
    }

    static void addShop(Shop tm) {
        SHOPS_RW_LOCK.writeLock().lock();
        try {
            shops.put(new Long(tm.getWurmId()), tm);
        }
        finally {
            SHOPS_RW_LOCK.writeLock().unlock();
        }
    }

    public static void deleteShop(long wurmid) {
        SHOPS_RW_LOCK.writeLock().lock();
        try {
            Shop shop = shops.get(new Long(wurmid));
            if (shop != null) {
                shop.delete();
            }
            shops.remove(new Long(wurmid));
        }
        finally {
            SHOPS_RW_LOCK.writeLock().unlock();
        }
    }

    static void addSupplyDemand(SupplyDemand sd) {
        supplyDemand.put(sd.getId(), sd);
    }

    public void addItemSoldByTraders(int templateId) {
        this.getSupplyDemand(templateId).addItemSoldByTrader();
    }

    public abstract void addItemSoldByTraders(String var1, long var2, String var4, String var5, int var6);

    public void addItemBoughtByTraders(int templateId) {
        this.getSupplyDemand(templateId).addItemBoughtByTrader();
    }

    public Change getChangeFor(long value) {
        return new Change(value);
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     * Enabled force condition propagation
     * Lifted jumps to return sites
     */
    public Creature getRandomTrader() {
        Creature toReturn = null;
        SHOPS_RW_LOCK.readLock().lock();
        try {
            int size = shops.size();
            for (Shop shop : shops.values()) {
                if (shop.isPersonal() || shop.getWurmId() <= 0L || Server.rand.nextInt(Math.max(2, size / 2)) != 0) continue;
                try {
                    Creature creature = toReturn = Creatures.getInstance().getCreature(shop.getWurmId());
                    return creature;
                }
                catch (NoSuchCreatureException nsc) {
                    try {
                        logger.log(Level.WARNING, "Weird, shop with id " + shop.getWurmId() + " has no creature.");
                    }
                    catch (Throwable throwable) {
                        throw throwable;
                        return toReturn;
                    }
                }
            }
        }
        finally {
            SHOPS_RW_LOCK.readLock().unlock();
        }
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     * Enabled force condition propagation
     * Lifted jumps to return sites
     */
    public Creature getTraderForZone(int x, int y, boolean surfaced) {
        int sx = 0;
        int sy = 0;
        int ex = 64;
        int ey = 64;
        Creature toReturn = null;
        SHOPS_RW_LOCK.readLock().lock();
        try {
            for (Shop shop : shops.values()) {
                VolaTile tile;
                if (shop.isPersonal() || shop.getWurmId() <= 0L || (tile = shop.getPos()) == null) continue;
                sx = tile.getTileX() - 64;
                sy = tile.getTileY() - 64;
                ex = tile.getTileX() + 64;
                ey = tile.getTileY() + 64;
                if (x >= ex || x <= sx || y >= ey || y <= sy || tile.isOnSurface() != surfaced) continue;
                try {
                    Creature creature = toReturn = Creatures.getInstance().getCreature(shop.getWurmId());
                    return creature;
                }
                catch (NoSuchCreatureException nsc) {
                    try {
                        logger.log(Level.WARNING, "Weird, shop with id " + shop.getWurmId() + " has no creature.");
                    }
                    catch (Throwable throwable) {
                        throw throwable;
                        return null;
                    }
                }
            }
        }
        finally {
            SHOPS_RW_LOCK.readLock().unlock();
        }
    }

    public abstract void updateCreatedIron(long var1);

    public abstract void updateCreatedSilver(long var1);

    public abstract void updateCreatedCopper(long var1);

    public abstract void updateCreatedGold(long var1);

    abstract void loadSupplyDemand();

    abstract void loadShopMoney();

    abstract void initialize() throws IOException;

    abstract SupplyDemand createSupplyDemand(int var1);

    public abstract Shop createShop(long var1);

    public abstract Shop createShop(long var1, long var3);

    public abstract void transaction(long var1, long var3, long var5, String var7, long var8);

    public abstract void updateLastPolled();

    public final void resetEarnings() {
        for (Shop s : shops.values()) {
            s.resetEarnings();
        }
    }

    static {
        goldOnes = new LinkedList();
        goldFives = new LinkedList();
        goldTwentys = new LinkedList();
        silverOnes = new LinkedList();
        silverFives = new LinkedList();
        silverTwentys = new LinkedList();
        copperOnes = new LinkedList();
        copperFives = new LinkedList();
        copperTwentys = new LinkedList();
        ironOnes = new LinkedList();
        ironFives = new LinkedList();
        ironTwentys = new LinkedList();
        logger = Logger.getLogger(Economy.class.getName());
        moneylogger = Logger.getLogger("Money");
        supplyDemand = new HashMap<Integer, SupplyDemand>();
        shops = new HashMap<Long, Shop>();
        SHOPS_RW_LOCK = new ReentrantReadWriteLock();
    }
}


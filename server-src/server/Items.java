/*
 * Decompiled with CFR 0.152.
 */
package com.wurmonline.server;

import com.wurmonline.server.Constants;
import com.wurmonline.server.DbConnector;
import com.wurmonline.server.MiscConstants;
import com.wurmonline.server.NoSuchItemException;
import com.wurmonline.server.Players;
import com.wurmonline.server.Server;
import com.wurmonline.server.Servers;
import com.wurmonline.server.TimeConstants;
import com.wurmonline.server.WurmId;
import com.wurmonline.server.behaviours.Vehicles;
import com.wurmonline.server.creatures.Creature;
import com.wurmonline.server.creatures.Delivery;
import com.wurmonline.server.economy.MonetaryConstants;
import com.wurmonline.server.endgames.EndGameItems;
import com.wurmonline.server.highways.MethodsHighways;
import com.wurmonline.server.highways.Routes;
import com.wurmonline.server.items.CoinDbStrings;
import com.wurmonline.server.items.DbItem;
import com.wurmonline.server.items.DbStrings;
import com.wurmonline.server.items.FrozenItemDbStrings;
import com.wurmonline.server.items.InitialContainer;
import com.wurmonline.server.items.InscriptionData;
import com.wurmonline.server.items.Item;
import com.wurmonline.server.items.ItemData;
import com.wurmonline.server.items.ItemDbStrings;
import com.wurmonline.server.items.ItemFactory;
import com.wurmonline.server.items.ItemMealData;
import com.wurmonline.server.items.ItemMetaData;
import com.wurmonline.server.items.ItemRequirement;
import com.wurmonline.server.items.ItemSettings;
import com.wurmonline.server.items.ItemSpellEffects;
import com.wurmonline.server.items.ItemTemplate;
import com.wurmonline.server.items.ItemTemplateFactory;
import com.wurmonline.server.items.Itempool;
import com.wurmonline.server.items.NoSpaceException;
import com.wurmonline.server.items.NoSuchTemplateException;
import com.wurmonline.server.kingdom.Kingdoms;
import com.wurmonline.server.players.PermissionsHistories;
import com.wurmonline.server.players.Player;
import com.wurmonline.server.players.PlayerInfo;
import com.wurmonline.server.players.PlayerInfoFactory;
import com.wurmonline.server.tutorial.MissionTargets;
import com.wurmonline.server.utils.DbUtilities;
import com.wurmonline.server.zones.NoSuchZoneException;
import com.wurmonline.server.zones.Zone;
import com.wurmonline.server.zones.Zones;
import com.wurmonline.shared.constants.CounterTypes;
import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.annotation.Nullable;
import javax.annotation.concurrent.GuardedBy;

public final class Items
implements MiscConstants,
MonetaryConstants,
CounterTypes,
TimeConstants {
    private static final ConcurrentHashMap<Long, Item> items = new ConcurrentHashMap();
    private static Logger logger = Logger.getLogger(Items.class.getName());
    public static final Logger debug = Logger.getLogger("ItemDebug");
    private static final ConcurrentHashMap<Item, Creature> draggedItems = new ConcurrentHashMap();
    private static final String GETITEMDATA = "SELECT * FROM ITEMDATA";
    private static final String GETITEMINSCRIPTIONDATA = "SELECT * FROM INSCRIPTIONS";
    private static final ConcurrentHashMap<Long, Set<Item>> containedItems = new ConcurrentHashMap();
    private static final ConcurrentHashMap<Long, Set<Item>> creatureItemsMap = new ConcurrentHashMap();
    private static final ConcurrentHashMap<Long, ItemData> itemDataMap = new ConcurrentHashMap();
    private static final ConcurrentHashMap<Long, InscriptionData> itemInscriptionDataMap = new ConcurrentHashMap();
    public static final int MAX_COUNT_ITEMS = 100;
    public static final int MAX_DECO_ITEMS = 15;
    private static final int MAX_EGGS = 1000;
    private static int currentEggs = 0;
    @GuardedBy(value="ITEM_DATA_RW_LOCK")
    private static final Set<Long> protectedCorpses = new HashSet<Long>();
    @GuardedBy(value="HIDDEN_ITEMS_RW_LOCK")
    private static final Set<Item> hiddenItems = new HashSet<Item>();
    private static final Set<Long> unstableRifts = new HashSet<Long>();
    private static final LinkedList<Item> spawnPoints = new LinkedList();
    private static final Set<Item> tents = new HashSet<Item>();
    private static boolean loadedCorpses = false;
    private static final ConcurrentHashMap<Long, Item> gmsigns = new ConcurrentHashMap();
    private static final ConcurrentHashMap<Long, Item> markers = new ConcurrentHashMap();
    private static final ConcurrentHashMap<Integer, Map<Integer, Set<Item>>> markersXY = new ConcurrentHashMap();
    private static final ConcurrentHashMap<Long, Item> waystones = new ConcurrentHashMap();
    private static final ConcurrentHashMap<Long, Integer> waystoneContainerCount = new ConcurrentHashMap();
    private static final ReentrantReadWriteLock HIDDEN_ITEMS_RW_LOCK = new ReentrantReadWriteLock();
    private static final Set<Item> tempHiddenItems = new HashSet<Item>();
    private static final Set<Item> warTargetItems = new HashSet<Item>();
    private static final Set<Item> sourceSprings = new HashSet<Item>();
    private static final Set<Item> supplyDepots = new HashSet<Item>();
    private static final Set<Item> harvestableItems = new HashSet<Item>();
    private static final Item[] emptyItems = new Item[0];
    private static final String moveItemsToFreezerForPlayer = DbConnector.isUseSqlite() ? "INSERT OR IGNORE INTO FROZENITEMS SELECT * FROM ITEMS WHERE OWNERID=?" : "INSERT IGNORE INTO FROZENITEMS (SELECT * FROM ITEMS WHERE OWNERID=?)";
    private static final String deleteInventoryItemsForPlayer = "DELETE FROM ITEMS WHERE OWNERID=?";
    private static final String returnItemsFromFreezerForPlayer = DbConnector.isUseSqlite() ? "INSERT OR IGNORE INTO ITEMS SELECT * FROM FROZENITEMS WHERE OWNERID=?" : "INSERT IGNORE INTO ITEMS (SELECT * FROM FROZENITEMS WHERE OWNERID=?)";
    private static final String deleteFrozenItemsForPlayer = "DELETE FROM FROZENITEMS WHERE OWNERID=?";
    private static final String returnItemFromFreezer = DbConnector.isUseSqlite() ? "INSERT OR IGNORE INTO ITEMS SELECT * FROM FROZENITEMS WHERE WURMID=?" : "INSERT IGNORE INTO ITEMS (SELECT * FROM FROZENITEMS WHERE WURMID=?)";
    private static final String deleteFrozenItem = "DELETE FROM FROZENITEMS WHERE WURMID=?";
    private static final String insertProtectedCorpse = "INSERT INTO PROTECTEDCORPSES(WURMID)VALUES(?)";
    private static final String deleteProtectedCorpse = "DELETE FROM PROTECTEDCORPSES WHERE WURMID=?";
    private static final String loadProtectedCorpse = "SELECT * FROM PROTECTEDCORPSES";
    private static final Map<Integer, Set<Item>> zoneItemsAtLoad = new ConcurrentHashMap<Integer, Set<Item>>();
    public static final long riftEndTime = 1482227988600L;
    private static long cpOne = 0L;
    private static long cpTwo = 0L;
    private static long cpThree = 0L;
    private static long cpFour = 0L;
    private static long numCoins = 0L;
    private static long numItems = 0L;

    private Items() {
    }

    public static long getCpOne() {
        return cpOne;
    }

    public static long getCpTwo() {
        return cpTwo;
    }

    public static long getCpThree() {
        return cpThree;
    }

    public static long getCpFour() {
        return cpFour;
    }

    public static long getNumCoins() {
        return numCoins;
    }

    public static long getNumItems() {
        return numItems;
    }

    public static void putItem(Item item) {
        items.put(new Long(item.getWurmId()), item);
        if (item.isItemSpawn()) {
            Items.addSupplyDepot(item);
        }
        if (item.isUnstableRift()) {
            Items.addUnstableRift(item);
        }
        if (item.getTemplate().isHarvestable()) {
            Items.addHarvestableItem(item);
        }
    }

    public static boolean exists(Item item) {
        return items.get(new Long(item.getWurmId())) != null;
    }

    public static boolean exists(long id) {
        return items.get(new Long(id)) != null;
    }

    public static Item getItem(long id) throws NoSuchItemException {
        Item toReturn = items.get(new Long(id));
        if (toReturn == null) {
            throw new NoSuchItemException("No item found with id " + id);
        }
        return toReturn;
    }

    public static Optional<Item> getItemOptional(long id) {
        Item item = null;
        item = items.get(new Long(id));
        Optional<Item> toReturn = Optional.ofNullable(item);
        return toReturn;
    }

    public static Set<Item> getItemsWithDesc(String descpart, boolean boat) throws NoSuchItemException {
        HashSet<Item> itemsToRet = new HashSet<Item>();
        String lDescpart = descpart.toLowerCase();
        for (Item ne : items.values()) {
            if ((!boat || !ne.isBoat()) && boat || ne.getDescription().length() <= 0 || !ne.getDescription().toLowerCase().contains(lDescpart)) continue;
            itemsToRet.add(ne);
        }
        return itemsToRet;
    }

    public static void countEggs() {
        long start = System.nanoTime();
        currentEggs = 0;
        int fountains = 0;
        int wildhives = 0;
        int hives = 0;
        for (Item nextEgg : items.values()) {
            if (nextEgg.getTemplateId() == 1239) {
                ++wildhives;
                continue;
            }
            if (nextEgg.getTemplateId() == 1175) {
                ++hives;
                continue;
            }
            if (nextEgg.isEgg() && nextEgg.getData1() > 0) {
                ++currentEggs;
                if (!logger.isLoggable(Level.FINER)) continue;
                logger.finer("Found egg number: " + currentEggs + ", Item: " + nextEgg);
                continue;
            }
            if (nextEgg.getParentId() == -10L || nextEgg.getTemplateId() != 408 && nextEgg.getTemplateId() != 635 && nextEgg.getTemplateId() != 405) continue;
            ++fountains;
        }
        float lElapsedTime = (float)(System.nanoTime() - start) / 1000000.0f;
        logger.log(Level.INFO, "Current number of eggs is " + currentEggs + " (max eggs is " + 1000 + ") That took " + lElapsedTime + " ms.");
        logger.log(Level.INFO, "Current number of wild hives is " + wildhives + " and domestic hives is " + hives + ".");
        if (Servers.isThisATestServer()) {
            Players.getInstance().sendGmMessage(null, "System", "Debug: Current number of wild hives is " + wildhives + " and domestic hives is " + hives + ".", false);
        }
        if (fountains > 0) {
            logger.log(Level.INFO, "Current number of fountains found in containers is " + fountains + ".");
        }
    }

    public static boolean mayLayEggs() {
        return currentEggs < 1000;
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public static Item[] getHiddenItemsAt(int tilex, int tiley, float height, boolean surfaced) {
        HIDDEN_ITEMS_RW_LOCK.readLock().lock();
        tempHiddenItems.clear();
        try {
            for (Item i : hiddenItems) {
                if ((int)i.getPosX() >> 2 != tilex || (int)i.getPosY() >> 2 != tiley || !(i.getPosZ() >= height) || surfaced != i.isOnSurface()) continue;
                tempHiddenItems.add(i);
            }
            if (tempHiddenItems.size() > 0) {
                Item[] itemArray = tempHiddenItems.toArray(new Item[tempHiddenItems.size()]);
                return itemArray;
            }
        }
        finally {
            HIDDEN_ITEMS_RW_LOCK.readLock().unlock();
        }
        return emptyItems;
    }

    public static void revealItem(Item item) {
        HIDDEN_ITEMS_RW_LOCK.writeLock().lock();
        try {
            hiddenItems.remove(item);
        }
        finally {
            HIDDEN_ITEMS_RW_LOCK.writeLock().unlock();
        }
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public static void hideItem(Creature performer, Item item, float height, boolean putOnSurface) {
        if (putOnSurface) {
            try {
                item.putInVoid();
                item.setPosX(performer.getPosX());
                item.setPosY(performer.getPosY());
                performer.getCurrentTile().getZone().addItem(item);
            }
            catch (Exception ex) {
                logger.log(Level.INFO, performer.getName() + " failed to hide item:" + ex.getMessage(), ex);
                performer.getCommunicator().sendNormalServerMessage("Failed to put the item on surface: " + ex.getMessage());
            }
        } else {
            HIDDEN_ITEMS_RW_LOCK.writeLock().lock();
            try {
                hiddenItems.add(item);
            }
            finally {
                HIDDEN_ITEMS_RW_LOCK.writeLock().unlock();
            }
            item.setHidden(true);
            int zoneId = item.getZoneId();
            if (zoneId < 0) {
                zoneId = performer.getCurrentTile().getZone().getId();
            }
            item.putInVoid();
            item.setPosX(performer.getPosX());
            item.setPosY(performer.getPosY());
            item.setPosZ(height);
            item.setZoneId(zoneId, true);
        }
    }

    public static void removeItem(long id) {
        Item i = items.remove(new Long(id));
        if (i != null && i.getTemplate() != null && i.getTemplate().isHarvestable()) {
            Items.removeHarvestableItem(i);
        }
    }

    public static Item[] getAllItems() {
        return items.values().toArray(new Item[items.size()]);
    }

    public static Item[] getManagedCartsFor(Player player, boolean includeAll) {
        HashSet<Item> carts = new HashSet<Item>();
        for (Item item : items.values()) {
            if (!item.isCart() || !item.canManage(player) || !includeAll && !item.isLocked()) continue;
            carts.add(item);
        }
        return carts.toArray(new Item[carts.size()]);
    }

    public static Item[] getOwnedCartsFor(Player player) {
        HashSet<Item> carts = new HashSet<Item>();
        for (Item item : items.values()) {
            if (!item.isCart() || !item.canManage(player)) continue;
            carts.add(item);
        }
        return carts.toArray(new Item[carts.size()]);
    }

    public static Item[] getManagedShipsFor(Player player, boolean includeAll) {
        HashSet<Item> ships = new HashSet<Item>();
        for (Item item : items.values()) {
            if (!item.isBoat() || !item.canManage(player) || !includeAll && !item.isLocked()) continue;
            ships.add(item);
        }
        return ships.toArray(new Item[ships.size()]);
    }

    public static Item[] getOwnedShipsFor(Player player) {
        HashSet<Item> ships = new HashSet<Item>();
        for (Item item : items.values()) {
            if (!item.isBoat() || !item.canManage(player)) continue;
            ships.add(item);
        }
        return ships.toArray(new Item[ships.size()]);
    }

    public static void getOwnedCorpsesCartsShipsFor(Player player, List<Item> corpses, List<Item> carts, List<Item> ships) {
        for (Item item : items.values()) {
            if (item.isCart()) {
                if (!item.canManage(player)) continue;
                carts.add(item);
                continue;
            }
            if (item.isBoat()) {
                if (!item.canManage(player)) continue;
                ships.add(item);
                continue;
            }
            if (item.getTemplateId() != 272 || !item.getName().equals("corpse of " + player.getName()) || item.getZoneId() <= -1) continue;
            corpses.add(item);
        }
    }

    public static int getNumberOfItems() {
        return items.size();
    }

    public static int getNumberOfNormalItems() {
        int numberOfNormalItems = 0;
        for (Item lItem : items.values()) {
            int templateId = lItem.getTemplateId();
            if (templateId == 0 || templateId == 521 || templateId >= 50 && templateId <= 61 || templateId >= 10 && templateId <= 19) continue;
            ++numberOfNormalItems;
        }
        return numberOfNormalItems;
    }

    public static void decay(long id, @Nullable DbStrings dbstrings) {
        if (WurmId.getType(id) != 19) {
            ItemFactory.decay(id, dbstrings);
        }
        Items.removeItem(id);
        Items.setProtected(id, false);
    }

    public static void destroyItem(long id) {
        Items.destroyItem(id, true);
    }

    public static void destroyItem(long id, boolean destroyKey) {
        Items.destroyItem(id, destroyKey, false);
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public static void destroyItem(long id, boolean destroyKey, boolean destroyRecycled) {
        Item dest = null;
        try {
            dest = Items.getItem(id);
            if (dest.isTraded()) {
                dest.getTradeWindow().removeItem(dest);
                dest.setTradeWindow(null);
            }
            if (dest.isTent()) {
                tents.remove(dest);
            }
            if (dest.isRoadMarker() && dest.isPlanted()) {
                if (dest.getWhatHappened().length() == 0) {
                    dest.setWhatHappened("decayed away");
                }
                Items.removeMarker(dest);
            }
            if (dest.getTemplateId() == 677) {
                Items.removeGmSign(dest);
            }
            if (dest.getTemplateId() == 1309) {
                Items.removeWagonerContainer(dest);
            }
            Items.stopDragging(dest);
            if (dest.isVehicle() || dest.isTent()) {
                Vehicles.destroyVehicle(id);
                ItemSettings.remove(id);
                if (dest.isBoat() && dest.getData() != -1L) {
                    Items.destroyItem(dest.getData(), destroyKey, destroyRecycled);
                }
            }
            MissionTargets.destroyMissionTarget(id, true);
            dest.deleteAllEffects();
            ItemSpellEffects effs = ItemSpellEffects.getSpellEffects(id);
            if (effs != null) {
                effs.destroy();
            }
            ItemRequirement.deleteRequirements(id);
            if (dest.isHugeAltar() || dest.isArtifact()) {
                if (dest.isArtifact()) {
                    EndGameItems.deleteEndGameItem(EndGameItems.getEndGameItem(dest));
                }
            } else if (dest.isCoin()) {
                Server.getInstance().transaction(id, dest.getOwnerId(), -10L, "Destroyed", dest.getValue());
            }
            if ((dest.isUnfinished() || dest.isUseOnGroundOnly()) && dest.getWatcherSet() != null) {
                for (Creature cret : dest.getWatcherSet()) {
                    cret.getCommunicator().sendRemoveFromCreationWindow(dest.getWurmId());
                }
            }
            if (dest.getTemplate().getInitialContainers() != null) {
                Item[] itemArray = dest.getItemsAsArray();
                int cret = itemArray.length;
                block11: for (int i = 0; i < cret; ++i) {
                    Item i2 = itemArray[i];
                    for (InitialContainer ic : dest.getTemplate().getInitialContainers()) {
                        if (i2.getTemplateId() != ic.getTemplateId()) continue;
                        Items.destroyItem(i2.getWurmId(), false, false);
                        continue block11;
                    }
                }
            }
            if (destroyKey && dest.isKey()) {
                try {
                    long lockId = dest.getLockId();
                    Item lock = Items.getItem(lockId);
                    lock.removeKey(id);
                }
                catch (NoSuchItemException nsi) {
                    logger.log(Level.INFO, "No lock when destroying key " + dest.getWurmId() + ", ownerId: " + dest.getOwnerId() + ", lastOwnerId: " + dest.getLastOwnerId());
                }
            }
            if (dest.isHollow() && dest.getLockId() != -10L) {
                Items.destroyItem(dest.getLockId(), destroyKey, destroyRecycled);
            }
            if (dest.isLock() && destroyKey) {
                for (long l : dest.getKeyIds()) {
                    try {
                        Item k = Items.getItem(l);
                        if (logger.isLoggable(Level.FINEST)) {
                            logger.finest("Destroying key with name: " + k.getName() + " and template: " + k.getTemplate().getName());
                        }
                        if (k.getTemplateId() == 166 || k.getTemplateId() == 663) {
                            dest.removeKey(l);
                            continue;
                        }
                        Items.destroyItem(l, destroyKey, destroyRecycled);
                    }
                    catch (NoSuchItemException ni) {
                        logger.log(Level.WARNING, "Unable to find item for key: " + l, ni);
                    }
                }
                Connection dbcon = null;
                PreparedStatement ps3 = null;
                PreparedStatement ps4 = null;
                try {
                    dbcon = DbConnector.getItemDbCon();
                    ps3 = dbcon.prepareStatement("DELETE FROM ITEMKEYS WHERE LOCKID=?");
                    ps3.setLong(1, id);
                    ps3.executeUpdate();
                    DbUtilities.closeDatabaseObjects(ps3, null);
                    ps4 = dbcon.prepareStatement("DELETE FROM LOCKS WHERE WURMID=?");
                    ps4.setLong(1, id);
                    ps4.executeUpdate();
                }
                catch (SQLException ex) {
                    try {
                        logger.log(Level.WARNING, "Failed to destroy lock/keys for item with id " + id, ex);
                    }
                    catch (Throwable throwable) {
                        DbUtilities.closeDatabaseObjects(ps3, null);
                        DbUtilities.closeDatabaseObjects(ps4, null);
                        DbConnector.returnConnection(dbcon);
                        throw throwable;
                    }
                    DbUtilities.closeDatabaseObjects(ps3, null);
                    DbUtilities.closeDatabaseObjects(ps4, null);
                    DbConnector.returnConnection(dbcon);
                }
                DbUtilities.closeDatabaseObjects(ps3, null);
                DbUtilities.closeDatabaseObjects(ps4, null);
                DbConnector.returnConnection(dbcon);
            }
            if (dest.getTemplateId() == 1127) {
                for (Item i2 : dest.getItemsAsArray()) {
                    if (i2.getTemplateId() != 1128) continue;
                    Items.destroyItem(i2.getWurmId(), false, false);
                }
            }
        }
        catch (NoSuchItemException nsi) {
            logger.log(Level.INFO, "Destroying " + id, nsi);
        }
        if (dest != null) {
            if (dest.getTemplateId() == 169) {
                debug.info("** removeAndEmpty: " + dest.getWurmId());
            }
            dest.removeAndEmpty();
            if (!destroyRecycled && dest.isTypeRecycled()) {
                if (dest.getTemplateId() == 169) {
                    debug.info("** removeItem: " + dest.getWurmId());
                }
                Items.removeItem(id);
                Itempool.returnRecycledItem(dest);
            } else {
                Items.decay(id, dest.getDbStrings());
            }
        }
        ItemSettings.remove(id);
        PermissionsHistories.remove(id);
        ItemMealData.delete(id);
    }

    public static boolean isItemLoaded(long id) {
        Item item = items.get(new Long(id));
        return item != null;
    }

    public static void startDragging(Creature dragger, Item dragged) {
        draggedItems.put(dragged, dragger);
        if (!dragged.isVehicle()) {
            Item[] itemarr;
            dragged.setLastOwnerId(dragger.getWurmId());
            for (Item lElement : itemarr = dragged.getAllItems(false)) {
                lElement.setLastOwnerId(dragger.getWurmId());
            }
        }
        dragger.setDraggedItem(dragged);
        if (dragger.getVisionArea() != null) {
            dragger.getVisionArea().broadCastUpdateSelectBar(dragged.getWurmId());
        }
    }

    public static void stopDragging(Item dragged) {
        dragged.savePosition();
        Creature creature = draggedItems.get(dragged);
        if (creature != null) {
            draggedItems.remove(dragged);
            creature.setDraggedItem(null);
        }
    }

    public static boolean isItemDragged(Item item) {
        return ((ConcurrentHashMap.KeySetView)draggedItems.keySet()).contains(item);
    }

    public static Creature getDragger(Item item) {
        return draggedItems.get(item);
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    static void loadAllItempInscriptionData() {
        long start = System.nanoTime();
        Connection dbcon = null;
        PreparedStatement ps = null;
        ResultSet rs = null;
        try {
            dbcon = DbConnector.getItemDbCon();
            ps = dbcon.prepareStatement(GETITEMINSCRIPTIONDATA);
            rs = ps.executeQuery();
            String inscription = "";
            String inscriber = "";
            int num = 0;
            while (rs.next()) {
                long iid = rs.getLong("WURMID");
                inscription = rs.getString("INSCRIPTION");
                inscriber = rs.getString("INSCRIBER");
                int penColor = rs.getInt("PENCOLOR");
                new InscriptionData(iid, inscription, inscriber, penColor);
                ++num;
            }
            float lElapsedTime = (float)(System.nanoTime() - start) / 1000000.0f;
            logger.log(Level.INFO, "Loaded " + num + " item inscription data entries, that took " + lElapsedTime + " ms");
        }
        catch (SQLException sqx) {
            try {
                logger.log(Level.WARNING, "Failed to load item inscription datas: " + sqx.getMessage(), sqx);
            }
            catch (Throwable throwable) {
                DbUtilities.closeDatabaseObjects(ps, rs);
                DbConnector.returnConnection(dbcon);
                throw throwable;
            }
            DbUtilities.closeDatabaseObjects(ps, rs);
            DbConnector.returnConnection(dbcon);
        }
        DbUtilities.closeDatabaseObjects(ps, rs);
        DbConnector.returnConnection(dbcon);
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    static void loadAllItemData() {
        long start = System.nanoTime();
        Connection dbcon = null;
        PreparedStatement ps = null;
        ResultSet rs = null;
        try {
            dbcon = DbConnector.getItemDbCon();
            ps = dbcon.prepareStatement(GETITEMDATA);
            rs = ps.executeQuery();
            int d1 = 0;
            int d2 = 0;
            int e1 = 0;
            int e2 = 0;
            int num = 0;
            while (rs.next()) {
                long iid = rs.getLong("WURMID");
                d1 = rs.getInt("DATA1");
                d2 = rs.getInt("DATA2");
                e1 = rs.getInt("EXTRA1");
                e2 = rs.getInt("EXTRA2");
                new ItemData(iid, d1, d2, e1, e2);
                ++num;
            }
            float lElapsedTime = (float)(System.nanoTime() - start) / 1000000.0f;
            logger.log(Level.INFO, "Loaded " + num + " item data entries, that took " + lElapsedTime + " ms");
        }
        catch (SQLException sqx) {
            try {
                logger.log(Level.WARNING, "Failed to load item datas: " + sqx.getMessage(), sqx);
            }
            catch (Throwable throwable) {
                DbUtilities.closeDatabaseObjects(ps, rs);
                DbConnector.returnConnection(dbcon);
                throw throwable;
            }
            DbUtilities.closeDatabaseObjects(ps, rs);
            DbConnector.returnConnection(dbcon);
        }
        DbUtilities.closeDatabaseObjects(ps, rs);
        DbConnector.returnConnection(dbcon);
        ItemSpellEffects.loadSpellEffectsForItems();
    }

    public static void addItemInscriptionData(InscriptionData data) {
        itemInscriptionDataMap.put(new Long(data.getWurmId()), data);
    }

    public static InscriptionData getItemInscriptionData(long itemid) {
        InscriptionData toReturn = null;
        toReturn = itemInscriptionDataMap.get(new Long(itemid));
        return toReturn;
    }

    public static void addData(ItemData data) {
        itemDataMap.put(new Long(data.wurmid), data);
    }

    public static ItemData getItemData(long itemid) {
        ItemData toReturn = null;
        toReturn = itemDataMap.get(new Long(itemid));
        return toReturn;
    }

    public static final Set<Item> getAllItemsForZone(int zid) {
        return zoneItemsAtLoad.get(zid);
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     * Loose catch block
     * WARNING - bad return control flow
     */
    public static final boolean reloadAllSubItems(Player performer, long wurmId) {
        boolean bl;
        logger.log(Level.INFO, performer.getName() + " forcing a reload of all subitems of " + wurmId);
        long s = System.nanoTime();
        Connection dbcon = null;
        PreparedStatement ps = null;
        ResultSet rs = null;
        try {
            Item parentItem = Items.getItem(wurmId);
            dbcon = DbConnector.getItemDbCon();
            ps = dbcon.prepareStatement("SELECT WURMID FROM ITEMS WHERE PARENTID=?");
            ps.setLong(1, wurmId);
            rs = ps.executeQuery();
            long iid = -10L;
            while (rs.next()) {
                iid = rs.getLong("WURMID");
                try {
                    Item i = Items.getItem(iid);
                    parentItem.insertItem(i, false);
                }
                catch (NoSuchItemException nsi) {
                    logger.log(Level.INFO, "Could not reload subitem:" + iid + " for " + wurmId + " as item could not be found.", nsi);
                    performer.getCommunicator().sendNormalServerMessage("Could not reload subitem:" + iid + " for " + wurmId + " as that item could not be found.");
                }
            }
            bl = true;
        }
        catch (NoSuchItemException nsi) {
            logger.log(Level.WARNING, "Could not reload subitems for " + wurmId + " as item could not be found.", nsi);
            performer.getCommunicator().sendNormalServerMessage("Could not reload subitems for " + wurmId + " as that item could not be found.");
            DbUtilities.closeDatabaseObjects(ps, rs);
            DbConnector.returnConnection(dbcon);
            logger.log(Level.INFO, performer.getName() + " reloaded subitems for " + wurmId + ". That took " + (float)(System.nanoTime() - s) / 1000000.0f + " ms.");
        }
        catch (SQLException sqx) {
            logger.log(Level.WARNING, "Failed to reload subitems: " + sqx.getMessage(), sqx);
            performer.getCommunicator().sendNormalServerMessage("Could not reload subitems for " + wurmId + " due to a database error.");
            {
                catch (Throwable throwable) {
                    DbUtilities.closeDatabaseObjects(ps, rs);
                    DbConnector.returnConnection(dbcon);
                    logger.log(Level.INFO, performer.getName() + " reloaded subitems for " + wurmId + ". That took " + (float)(System.nanoTime() - s) / 1000000.0f + " ms.");
                    throw throwable;
                }
            }
            DbUtilities.closeDatabaseObjects(ps, rs);
            DbConnector.returnConnection(dbcon);
            logger.log(Level.INFO, performer.getName() + " reloaded subitems for " + wurmId + ". That took " + (float)(System.nanoTime() - s) / 1000000.0f + " ms.");
        }
        DbUtilities.closeDatabaseObjects(ps, rs);
        DbConnector.returnConnection(dbcon);
        logger.log(Level.INFO, performer.getName() + " reloaded subitems for " + wurmId + ". That took " + (float)(System.nanoTime() - s) / 1000000.0f + " ms.");
        return bl;
        return false;
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    static void loadAllZoneItems(DbStrings dbstrings) {
        if (!loadedCorpses) {
            Items.loadAllProtectedItems();
        }
        try {
            logger.log(Level.INFO, "Loading all zone items using " + dbstrings);
            long s = System.nanoTime();
            Connection dbcon = null;
            PreparedStatement ps = null;
            ResultSet rs = null;
            try {
                dbcon = DbConnector.getItemDbCon();
                ps = dbcon.prepareStatement(dbstrings.getZoneItems());
                rs = ps.executeQuery();
                long iid = -10L;
                while (rs.next()) {
                    iid = rs.getLong("WURMID");
                    String name = rs.getString("NAME");
                    float posx = rs.getFloat("POSX");
                    float posy = rs.getFloat("POSY");
                    try {
                        long pid;
                        ItemTemplate temp = ItemTemplateFactory.getInstance().getTemplate(rs.getInt("TEMPLATEID"));
                        DbItem item = new DbItem(iid, temp, name, rs.getLong("LASTMAINTAINED"), rs.getFloat("QUALITYLEVEL"), rs.getFloat("ORIGINALQUALITYLEVEL"), rs.getInt("SIZEX"), rs.getInt("SIZEY"), rs.getInt("SIZEZ"), posx, posy, rs.getFloat("POSZ"), rs.getFloat("ROTATION"), rs.getLong("PARENTID"), -10L, rs.getInt("ZONEID"), rs.getFloat("DAMAGE"), rs.getInt("WEIGHT"), rs.getByte("MATERIAL"), rs.getLong("LOCKID"), rs.getShort("PLACE"), rs.getInt("PRICE"), rs.getShort("TEMPERATURE"), rs.getString("DESCRIPTION"), rs.getByte("BLESS"), rs.getByte("ENCHANT"), rs.getBoolean("BANKED"), rs.getLong("LASTOWNERID"), rs.getByte("AUXDATA"), rs.getLong("CREATIONDATE"), rs.getByte("CREATIONSTATE"), rs.getInt("REALTEMPLATE"), rs.getBoolean("WORNARMOUR"), rs.getInt("COLOR"), rs.getInt("COLOR2"), rs.getBoolean("FEMALE"), rs.getBoolean("MAILED"), rs.getBoolean("TRANSFERRED"), rs.getString("CREATOR"), rs.getBoolean("HIDDEN"), rs.getByte("MAILTIMES"), rs.getByte("RARITY"), rs.getLong("ONBRIDGE"), rs.getInt("SETTINGS"), rs.getBoolean("PLACEDONPARENT"), dbstrings);
                        if (item.hidden) {
                            HIDDEN_ITEMS_RW_LOCK.writeLock().lock();
                            try {
                                hiddenItems.add(item);
                            }
                            finally {
                                HIDDEN_ITEMS_RW_LOCK.writeLock().unlock();
                            }
                        }
                        if (item.isWarTarget()) {
                            Items.addWarTarget(item);
                        }
                        if (item.isSourceSpring()) {
                            Items.addSourceSpring(item);
                        }
                        if ((pid = ((Item)item).getParentId()) != -10L) {
                            Set<Item> contained = containedItems.get(new Long(pid));
                            if (contained == null) {
                                contained = new HashSet<Item>();
                            }
                            contained.add(item);
                            containedItems.put(new Long(pid), contained);
                        }
                        if (((Item)item).getParentId() == -10L && ((Item)item).getZoneId() > 0) {
                            Set<Item> itemset = zoneItemsAtLoad.get(((Item)item).getZoneId());
                            if (itemset == null) {
                                itemset = new HashSet<Item>();
                                zoneItemsAtLoad.put(((Item)item).getZoneId(), itemset);
                            }
                            itemset.add(item);
                        }
                        if (item.isEgg() && ((Item)item).getData1() > 0) {
                            ++currentEggs;
                        }
                        if (temp.getTemplateId() != 330) continue;
                        item.setData(-1L);
                    }
                    catch (NoSuchTemplateException nst) {
                        logger.log(Level.WARNING, "Problem getting Template for item " + name + " (" + iid + ") @" + ((int)posx >> 2) + "," + ((int)posy >> 2) + "- " + nst.getMessage(), nst);
                    }
                }
            }
            catch (SQLException sqx) {
                try {
                    logger.log(Level.WARNING, "Failed to load zone items: " + sqx.getMessage(), sqx);
                }
                catch (Throwable throwable) {
                    DbUtilities.closeDatabaseObjects(ps, rs);
                    DbConnector.returnConnection(dbcon);
                    throw throwable;
                }
                DbUtilities.closeDatabaseObjects(ps, rs);
                DbConnector.returnConnection(dbcon);
            }
            DbUtilities.closeDatabaseObjects(ps, rs);
            DbConnector.returnConnection(dbcon);
            for (Item item : items.values()) {
                item.getContainedItems();
            }
            long e = System.nanoTime();
            logger.log(Level.INFO, "Loaded " + items.size() + " zone items. That took " + (float)(e - s) / 1000000.0f + " ms.");
        }
        catch (Exception ex) {
            logger.log(Level.WARNING, "Problem loading zone items due to " + ex.getMessage(), ex);
        }
        Itempool.checkRecycledItems();
    }

    public static final Set<Item> getTents() {
        return tents;
    }

    public static final void addTent(Item tent) {
        tents.add(tent);
    }

    public static final void addGmSign(Item gmSign) {
        gmsigns.put(gmSign.getWurmId(), gmSign);
    }

    public static final void removeGmSign(Item gmSign) {
        gmsigns.remove(gmSign.getWurmId());
    }

    public static final Item[] getGMSigns() {
        return gmsigns.values().toArray(new Item[gmsigns.size()]);
    }

    public static final void addMarker(Item marker) {
        Map<Integer, Set<Item>> ymap;
        markers.put(marker.getWurmId(), marker);
        if (marker.getTemplateId() == 1112) {
            waystones.put(marker.getWurmId(), marker);
        }
        if ((ymap = markersXY.get(marker.getTileX())) == null) {
            ymap = new HashMap<Integer, Set<Item>>();
            HashSet<Item> mset = new HashSet<Item>();
            mset.add(marker);
            ymap.put(marker.getTileY(), mset);
            markersXY.put(marker.getTileX(), ymap);
        } else {
            Set<Item> mset = ymap.get(marker.getTileY());
            if (mset == null) {
                mset = new HashSet<Item>();
                ymap.put(marker.getTileY(), mset);
                mset.add(marker);
            } else {
                mset.add(marker);
            }
        }
    }

    public static final Item getMarker(int tilex, int tiley, boolean onSurface, int floorlevel, long bridgeId) {
        Set<Item> mset;
        Map<Integer, Set<Item>> ymap = markersXY.get(tilex);
        if (ymap != null && (mset = ymap.get(tiley)) != null) {
            for (Item marker : mset) {
                if (marker.isOnSurface() != onSurface || marker.getFloorLevel() != floorlevel || marker.getBridgeId() != bridgeId) continue;
                return marker;
            }
        }
        return null;
    }

    public static final void removeMarker(Item marker) {
        Set<Item> mset;
        if (marker.getAuxData() != 0) {
            MethodsHighways.removeLinksTo(marker);
        }
        markers.remove(marker.getWurmId());
        if (marker.getTemplateId() == 1112) {
            waystones.remove(marker.getWurmId());
        }
        Routes.remove(marker);
        Map<Integer, Set<Item>> ymap = markersXY.get(marker.getTileX());
        if (ymap != null && (mset = ymap.get(marker.getTileY())) != null) {
            for (Item item : mset) {
                if (item.getWurmId() != marker.getWurmId()) continue;
                mset.remove(marker);
                if (mset.isEmpty()) {
                    ymap.remove(marker.getTileY());
                    if (ymap.isEmpty()) {
                        markersXY.remove(marker.getTileX());
                    }
                }
                return;
            }
        }
    }

    public static final Item[] getWaystones() {
        return waystones.values().toArray(new Item[waystones.size()]);
    }

    public static final Item[] getMarkers() {
        return markers.values().toArray(new Item[markers.size()]);
    }

    public static final void addWagonerContainer(Item wagonerContainer) {
        long waystoneId = wagonerContainer.getData();
        Integer count = waystoneContainerCount.get(waystoneId);
        if (count == null) {
            waystoneContainerCount.put(waystoneId, 1);
        } else {
            waystoneContainerCount.put(waystoneId, count + 1);
        }
    }

    public static final void removeWagonerContainer(Item wagonerContainer) {
        long waystoneId = wagonerContainer.getData();
        Integer count = waystoneContainerCount.get(waystoneId);
        if (count != null) {
            int icount = count;
            if (icount > 1) {
                waystoneContainerCount.put(waystoneId, icount - 1);
            } else {
                waystoneContainerCount.remove(waystoneId);
            }
        }
    }

    public static final boolean isWaystoneInUse(long waystoneId) {
        Integer count = waystoneContainerCount.get(waystoneId);
        if (count != null) {
            return true;
        }
        return Delivery.isDeliveryPoint(waystoneId);
    }

    public static final void addSpawn(Item spawn) {
        spawnPoints.add(spawn);
    }

    public static final void removeSpawn(Item spawn) {
        spawnPoints.remove(spawn);
    }

    public static final Item[] getSpawnPoints() {
        return spawnPoints.toArray(new Item[spawnPoints.size()]);
    }

    public static final void addUnstableRift(Item rift) {
        unstableRifts.add(rift.getWurmId());
    }

    public static final void pollUnstableRifts() {
        if (System.currentTimeMillis() > 1482227988600L && !unstableRifts.isEmpty()) {
            if (unstableRifts.size() >= 15) {
                Server.getInstance().broadCastAlert("A shimmering wave of light runs over all the land as all the source rifts collapse.");
            }
            for (Long rift : unstableRifts) {
                Items.destroyItem(rift);
            }
            unstableRifts.clear();
        }
    }

    public static final void removeTent(Item tent) {
        tents.remove(tent);
    }

    public static final void addWarTarget(Item target) {
        warTargetItems.add(target);
    }

    public static final Item[] getWarTargets() {
        return warTargetItems.toArray(new Item[warTargetItems.size()]);
    }

    public static final void addSourceSpring(Item spring) {
        sourceSprings.add(spring);
    }

    public static final Item[] getSourceSprings() {
        return sourceSprings.toArray(new Item[sourceSprings.size()]);
    }

    public static final void addSupplyDepot(Item depot) {
        supplyDepots.add(depot);
    }

    public static final Item[] getSupplyDepots() {
        return supplyDepots.toArray(new Item[supplyDepots.size()]);
    }

    public static final void addHarvestableItem(Item harvestable) {
        harvestableItems.add(harvestable);
    }

    public static final void removeHarvestableItem(Item harvestable) {
        harvestableItems.remove(harvestable);
    }

    public static final Item[] getHarvestableItems() {
        return harvestableItems.toArray(new Item[harvestableItems.size()]);
    }

    public static boolean isHighestQLForTemplate(int itemTemplate, float itemql, long itemId, boolean before) {
        if (itemTemplate == 179) {
            return false;
        }
        if (itemTemplate == 386) {
            return false;
        }
        if (itemql < 80.0f) {
            return false;
        }
        boolean searched = false;
        for (Item i : items.values()) {
            if (i.getTemplateId() != itemTemplate || before && itemId == i.getWurmId()) continue;
            searched = true;
            if (!(i.getOriginalQualityLevel() > itemql)) continue;
            return false;
        }
        return searched;
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    static Item createMetaDataItem(ItemMetaData md) {
        long iid = md.itemId;
        try {
            long pid;
            ItemTemplate temp = ItemTemplateFactory.getInstance().getTemplate(md.itemtemplateId);
            DbItem item = new DbItem(iid, temp, md.itname, md.lastmaintained, md.ql, md.origQl, md.sizex, md.sizey, md.sizez, md.posx, md.posy, md.posz, 0.0f, md.parentId, md.ownerId, -10, md.itemdam, md.weight, md.material, md.lockid, md.place, md.price, md.temp, md.desc, md.bless, md.enchantment, md.banked, md.lastowner, md.auxbyte, md.creationDate, md.creationState, md.realTemplate, md.wornAsArmour, md.color, md.color2, md.female, md.mailed, false, md.creator, false, 0, md.rarity, md.onBridge, md.settings, false, md.instance);
            if (item.hidden) {
                HIDDEN_ITEMS_RW_LOCK.writeLock().lock();
                try {
                    hiddenItems.add(item);
                }
                finally {
                    HIDDEN_ITEMS_RW_LOCK.writeLock().unlock();
                }
            }
            if (Servers.localServer.testServer) {
                logger.log(Level.INFO, "Converting " + item.getName() + ", " + item.getWurmId());
            }
            if (item.isDraggable()) {
                float newPosX = (float)((int)item.getPosX() >> 2 << 2) + 0.5f + Server.rand.nextFloat() * 2.0f;
                float newPosY = (float)((int)item.getPosY() >> 2 << 2) + 0.5f + Server.rand.nextFloat() * 2.0f;
                item.setTempPositions(newPosX, newPosY, item.getPosZ(), ((Item)item).getRotation());
            }
            if ((pid = ((Item)item).getParentId()) != -10L) {
                Set<Item> contained = containedItems.get(pid);
                if (contained == null) {
                    contained = new HashSet<Item>();
                }
                contained.add(item);
                containedItems.put(pid, contained);
            }
            if (item.isEgg() && ((Item)item).getData1() > 0) {
                ++currentEggs;
            }
            return item;
        }
        catch (NoSuchTemplateException nst) {
            logger.log(Level.WARNING, "Problem getting Template for item with Wurm ID " + iid + " - " + nst.getMessage(), nst);
            return null;
        }
    }

    public static void convertItemMetaData(ItemMetaData[] metadatas) {
        long start = System.nanoTime();
        HashSet<Item> mditems = new HashSet<Item>();
        for (ItemMetaData lMetadata : metadatas) {
            mditems.add(Items.createMetaDataItem(lMetadata));
        }
        for (Item item : mditems) {
            if (Servers.localServer.testServer) {
                logger.log(Level.INFO, "Found " + item.getName());
            }
            if (item == null) continue;
            item.getContainedItems();
        }
        float lElapsedTime = (float)(System.nanoTime() - start) / 1000000.0f;
        if (logger.isLoggable(Level.FINER)) {
            logger.finer("Unpacked " + mditems.size() + " transferred items. That took " + lElapsedTime + " ms.");
        }
    }

    static void loadAllItemEffects() {
        Item[] itarr;
        logger.info("Loading item effects.");
        long now = System.nanoTime();
        int numberOfEffectsLoaded = 0;
        for (Item lElement : itarr = Items.getAllItems()) {
            if (lElement.getTemperature() <= 1000 && !lElement.isAlwaysLit() && !lElement.isItemSpawn()) continue;
            lElement.loadEffects();
            ++numberOfEffectsLoaded;
        }
        logger.log(Level.INFO, "Loaded " + numberOfEffectsLoaded + " item effects. That took " + (float)(System.nanoTime() - now) / 1000000.0f + " ms.");
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public static final void returnItemFromFreezer(long wurmId) {
        boolean ok = false;
        Connection dbcon = null;
        PreparedStatement ps = null;
        try {
            dbcon = DbConnector.getItemDbCon();
            ps = dbcon.prepareStatement(returnItemFromFreezer);
            ps.setLong(1, wurmId);
            ps.execute();
            ok = true;
        }
        catch (SQLException sqx) {
            try {
                logger.log(Level.WARNING, "Failed to move item from freezer  " + wurmId + " : " + sqx.getMessage(), sqx);
            }
            catch (Throwable throwable) {
                DbUtilities.closeDatabaseObjects(ps, null);
                DbConnector.returnConnection(dbcon);
                throw throwable;
            }
            DbUtilities.closeDatabaseObjects(ps, null);
            DbConnector.returnConnection(dbcon);
        }
        DbUtilities.closeDatabaseObjects(ps, null);
        DbConnector.returnConnection(dbcon);
        if (ok) {
            try {
                dbcon = DbConnector.getItemDbCon();
                ps = dbcon.prepareStatement(deleteFrozenItem);
                ps.setLong(1, wurmId);
                ps.execute();
            }
            catch (SQLException sqx) {
                logger.log(Level.WARNING, "Failed to delete item when moved to freezer " + wurmId + " : " + sqx.getMessage(), sqx);
            }
            finally {
                DbUtilities.closeDatabaseObjects(ps, null);
                DbConnector.returnConnection(dbcon);
            }
        }
        if (ok) {
            try {
                Item i = Items.getItem(wurmId);
                if (i.getDbStrings() == FrozenItemDbStrings.getInstance()) {
                    i.setDbStrings(ItemDbStrings.getInstance());
                }
            }
            catch (NoSuchItemException noSuchItemException) {
                // empty catch block
            }
        }
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public static final void returnItemsFromFreezerFor(long playerId) {
        boolean ok = false;
        Connection dbcon = null;
        PreparedStatement ps = null;
        try {
            dbcon = DbConnector.getItemDbCon();
            ps = dbcon.prepareStatement(returnItemsFromFreezerForPlayer);
            ps.setLong(1, playerId);
            ps.execute();
            ok = true;
        }
        catch (SQLException sqx) {
            try {
                logger.log(Level.WARNING, "Failed to move items from freezer for creature " + playerId + " : " + sqx.getMessage(), sqx);
            }
            catch (Throwable throwable) {
                DbUtilities.closeDatabaseObjects(ps, null);
                DbConnector.returnConnection(dbcon);
                throw throwable;
            }
            DbUtilities.closeDatabaseObjects(ps, null);
            DbConnector.returnConnection(dbcon);
        }
        DbUtilities.closeDatabaseObjects(ps, null);
        DbConnector.returnConnection(dbcon);
        if (ok) {
            try {
                dbcon = DbConnector.getItemDbCon();
                ps = dbcon.prepareStatement(deleteFrozenItemsForPlayer);
                ps.setLong(1, playerId);
                ps.execute();
            }
            catch (SQLException sqx) {
                logger.log(Level.WARNING, "Failed to delete items when moved to freezer for creature " + playerId + " : " + sqx.getMessage(), sqx);
            }
            finally {
                DbUtilities.closeDatabaseObjects(ps, null);
                DbConnector.returnConnection(dbcon);
            }
        }
        if (ok) {
            PlayerInfo pinf = PlayerInfoFactory.getPlayerInfoWithWurmId(playerId);
            if (pinf != null) {
                pinf.setMovedInventory(false);
                try {
                    pinf.save();
                }
                catch (IOException iox) {
                    logger.log(Level.WARNING, iox.getMessage());
                }
            }
            for (Item i : items.values()) {
                if (i.getOwnerId() != playerId || i.getDbStrings() != FrozenItemDbStrings.getInstance()) continue;
                i.setDbStrings(ItemDbStrings.getInstance());
            }
        }
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public static final boolean moveItemsToFreezerFor(long playerId) {
        boolean ok = false;
        Connection dbcon = null;
        PreparedStatement ps = null;
        try {
            dbcon = DbConnector.getItemDbCon();
            ps = dbcon.prepareStatement(moveItemsToFreezerForPlayer);
            ps.setLong(1, playerId);
            ps.execute();
            ok = true;
        }
        catch (SQLException sqx) {
            try {
                logger.log(Level.WARNING, "Failed to move items to freezer for creature " + playerId + " : " + sqx.getMessage(), sqx);
            }
            catch (Throwable throwable) {
                DbUtilities.closeDatabaseObjects(ps, null);
                DbConnector.returnConnection(dbcon);
                throw throwable;
            }
            DbUtilities.closeDatabaseObjects(ps, null);
            DbConnector.returnConnection(dbcon);
        }
        DbUtilities.closeDatabaseObjects(ps, null);
        DbConnector.returnConnection(dbcon);
        if (ok) {
            try {
                dbcon = DbConnector.getItemDbCon();
                ps = dbcon.prepareStatement(deleteInventoryItemsForPlayer);
                ps.setLong(1, playerId);
                ps.execute();
            }
            catch (SQLException sqx) {
                logger.log(Level.WARNING, "Failed to delete items when moved to freezer for creature " + playerId + " : " + sqx.getMessage(), sqx);
            }
            finally {
                DbUtilities.closeDatabaseObjects(ps, null);
                DbConnector.returnConnection(dbcon);
            }
        }
        if (ok) {
            for (Item i : items.values()) {
                if (i.getOwnerId() != playerId || i.getDbStrings() != ItemDbStrings.getInstance()) continue;
                i.setDbStrings(FrozenItemDbStrings.getInstance());
                logger.log(Level.INFO, "Changed dbstrings for item " + i.getWurmId() + " and player " + playerId + " to frozen");
            }
        }
        return ok;
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public static Set<Long> loadAllNonTransferredItemsIdsForCreature(long creatureId, PlayerInfo info) {
        long iid;
        if (logger.isLoggable(Level.FINER)) {
            logger.finer("Loading items for " + creatureId);
        }
        HashSet<Long> creatureItemIds = new HashSet<Long>();
        if (info != null && info.hasMovedInventory()) {
            Items.returnItemsFromFreezerFor(creatureId);
            info.setMovedInventory(false);
            PlayerInfoFactory.getDeleteLogger().log(Level.INFO, "Returned items for " + info.getName() + " after transfer");
        }
        Connection dbcon = null;
        PreparedStatement ps = null;
        ResultSet rs = null;
        try {
            dbcon = DbConnector.getItemDbCon();
            ps = dbcon.prepareStatement(ItemDbStrings.getInstance().getCreatureItemsNonTransferred());
            ps.setLong(1, creatureId);
            rs = ps.executeQuery();
            iid = -10L;
            while (rs.next()) {
                iid = rs.getLong("WURMID");
                creatureItemIds.add(new Long(iid));
            }
        }
        catch (SQLException sqx) {
            try {
                logger.log(Level.WARNING, "Failed to load items for creature " + creatureId + " : " + sqx.getMessage(), sqx);
            }
            catch (Throwable throwable) {
                DbUtilities.closeDatabaseObjects(ps, rs);
                DbConnector.returnConnection(dbcon);
                throw throwable;
            }
            DbUtilities.closeDatabaseObjects(ps, rs);
            DbConnector.returnConnection(dbcon);
        }
        DbUtilities.closeDatabaseObjects(ps, rs);
        DbConnector.returnConnection(dbcon);
        try {
            dbcon = DbConnector.getItemDbCon();
            ps = dbcon.prepareStatement(CoinDbStrings.getInstance().getCreatureItemsNonTransferred());
            ps.setLong(1, creatureId);
            rs = ps.executeQuery();
            iid = -10L;
            while (rs.next()) {
                iid = rs.getLong("WURMID");
                creatureItemIds.add(new Long(iid));
            }
        }
        catch (SQLException sqx) {
            logger.log(Level.WARNING, "Failed to load coin items for creature " + creatureId + " : " + sqx.getMessage(), sqx);
        }
        finally {
            DbUtilities.closeDatabaseObjects(ps, rs);
            DbConnector.returnConnection(dbcon);
        }
        try {
            dbcon = DbConnector.getItemDbCon();
            ps = dbcon.prepareStatement(FrozenItemDbStrings.getInstance().getCreatureItemsNonTransferred());
            ps.setLong(1, creatureId);
            rs = ps.executeQuery();
            long iid2 = -10L;
            while (rs.next()) {
                iid2 = rs.getLong("WURMID");
                creatureItemIds.add(new Long(iid2));
            }
        }
        catch (SQLException sqx) {
            logger.log(Level.WARNING, "Failed to load frozen items for creature " + creatureId + " : " + sqx.getMessage(), sqx);
        }
        finally {
            DbUtilities.closeDatabaseObjects(ps, rs);
            DbConnector.returnConnection(dbcon);
        }
        return creatureItemIds;
    }

    public static final void clearCreatureLoadMap() {
        creatureItemsMap.clear();
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public static final void loadAllCreatureItems() {
        Set<Item> contained;
        long pid;
        boolean load;
        Item item;
        ItemTemplate temp;
        long iid;
        Connection dbcon = null;
        PreparedStatement ps = null;
        ResultSet rs = null;
        try {
            dbcon = DbConnector.getItemDbCon();
            ps = dbcon.prepareStatement("SELECT * FROM ITEMS WHERE OWNERID&0xFF=1");
            rs = ps.executeQuery();
            iid = -10L;
            while (rs.next()) {
                iid = rs.getLong("WURMID");
                try {
                    temp = ItemTemplateFactory.getInstance().getTemplate(rs.getInt("TEMPLATEID"));
                    item = null;
                    load = true;
                    if (temp.alwaysLoaded) {
                        load = false;
                        try {
                            item = Items.getItem(iid);
                            item.setOwnerStuff(temp);
                        }
                        catch (NoSuchItemException nsi) {
                            load = true;
                        }
                    }
                    if (load) {
                        item = new DbItem(iid, temp, rs.getString("NAME"), rs.getLong("LASTMAINTAINED"), rs.getFloat("QUALITYLEVEL"), rs.getFloat("ORIGINALQUALITYLEVEL"), rs.getInt("SIZEX"), rs.getInt("SIZEY"), rs.getInt("SIZEZ"), rs.getFloat("POSX"), rs.getFloat("POSY"), rs.getFloat("POSZ"), rs.getFloat("ROTATION"), rs.getLong("PARENTID"), rs.getLong("OWNERID"), rs.getInt("ZONEID"), rs.getFloat("DAMAGE"), rs.getInt("WEIGHT"), rs.getByte("MATERIAL"), rs.getLong("LOCKID"), rs.getShort("PLACE"), rs.getInt("PRICE"), rs.getShort("TEMPERATURE"), rs.getString("DESCRIPTION"), rs.getByte("BLESS"), rs.getByte("ENCHANT"), rs.getBoolean("BANKED"), rs.getLong("LASTOWNERID"), rs.getByte("AUXDATA"), rs.getLong("CREATIONDATE"), rs.getByte("CREATIONSTATE"), rs.getInt("REALTEMPLATE"), rs.getBoolean("WORNARMOUR"), rs.getInt("COLOR"), rs.getInt("COLOR2"), rs.getBoolean("FEMALE"), rs.getBoolean("MAILED"), rs.getBoolean("TRANSFERRED"), rs.getString("CREATOR"), rs.getBoolean("HIDDEN"), rs.getByte("MAILTIMES"), rs.getByte("RARITY"), rs.getLong("ONBRIDGE"), rs.getInt("SETTINGS"), rs.getBoolean("PLACEDONPARENT"), ItemDbStrings.getInstance());
                    }
                    if ((pid = item.getParentId()) != -10L) {
                        contained = containedItems.get(pid);
                        if (contained == null) {
                            contained = new HashSet<Item>();
                        }
                        contained.add(item);
                        containedItems.put(pid, contained);
                    }
                    if (item.getOwnerId() > 0L) {
                        contained = creatureItemsMap.get(item.getOwnerId());
                        if (contained == null) {
                            contained = new HashSet<Item>();
                        }
                        contained.add(item);
                        creatureItemsMap.put(item.getOwnerId(), contained);
                    }
                    ++numItems;
                }
                catch (NoSuchTemplateException nst) {
                    logger.log(Level.WARNING, "Problem getting Template for item", nst);
                }
            }
        }
        catch (SQLException sqx) {
            try {
                logger.log(Level.WARNING, "Failed to load items " + sqx.getMessage(), sqx);
            }
            catch (Throwable throwable) {
                DbUtilities.closeDatabaseObjects(ps, rs);
                DbConnector.returnConnection(dbcon);
                throw throwable;
            }
            DbUtilities.closeDatabaseObjects(ps, rs);
            DbConnector.returnConnection(dbcon);
        }
        DbUtilities.closeDatabaseObjects(ps, rs);
        DbConnector.returnConnection(dbcon);
        try {
            dbcon = DbConnector.getItemDbCon();
            ps = dbcon.prepareStatement("SELECT * FROM COINS WHERE OWNERID&0xFF=1");
            rs = ps.executeQuery();
            iid = -10L;
            while (rs.next()) {
                iid = rs.getLong("WURMID");
                try {
                    temp = ItemTemplateFactory.getInstance().getTemplate(rs.getInt("TEMPLATEID"));
                    item = null;
                    load = true;
                    if (temp.alwaysLoaded) {
                        load = false;
                        try {
                            item = Items.getItem(iid);
                            item.setOwnerStuff(temp);
                        }
                        catch (NoSuchItemException nsi) {
                            load = true;
                        }
                    }
                    if (load) {
                        item = new DbItem(iid, temp, rs.getString("NAME"), rs.getLong("LASTMAINTAINED"), rs.getFloat("QUALITYLEVEL"), rs.getFloat("ORIGINALQUALITYLEVEL"), rs.getInt("SIZEX"), rs.getInt("SIZEY"), rs.getInt("SIZEZ"), rs.getFloat("POSX"), rs.getFloat("POSY"), rs.getFloat("POSZ"), rs.getFloat("ROTATION"), rs.getLong("PARENTID"), rs.getLong("OWNERID"), rs.getInt("ZONEID"), rs.getFloat("DAMAGE"), rs.getInt("WEIGHT"), rs.getByte("MATERIAL"), rs.getLong("LOCKID"), rs.getShort("PLACE"), rs.getInt("PRICE"), rs.getShort("TEMPERATURE"), rs.getString("DESCRIPTION"), rs.getByte("BLESS"), rs.getByte("ENCHANT"), rs.getBoolean("BANKED"), rs.getLong("LASTOWNERID"), rs.getByte("AUXDATA"), rs.getLong("CREATIONDATE"), rs.getByte("CREATIONSTATE"), rs.getInt("REALTEMPLATE"), rs.getBoolean("WORNARMOUR"), rs.getInt("COLOR"), rs.getInt("COLOR2"), rs.getBoolean("FEMALE"), rs.getBoolean("MAILED"), rs.getBoolean("TRANSFERRED"), rs.getString("CREATOR"), rs.getBoolean("HIDDEN"), rs.getByte("MAILTIMES"), rs.getByte("RARITY"), rs.getLong("ONBRIDGE"), rs.getInt("SETTINGS"), rs.getBoolean("PLACEDONPARENT"), CoinDbStrings.getInstance());
                    }
                    if ((pid = item.getParentId()) != -10L) {
                        contained = containedItems.get(pid);
                        if (contained == null) {
                            contained = new HashSet<Item>();
                        }
                        contained.add(item);
                        containedItems.put(pid, contained);
                    }
                    if (item.getOwnerId() > 0L) {
                        contained = creatureItemsMap.get(item.getOwnerId());
                        if (contained == null) {
                            contained = new HashSet<Item>();
                        }
                        contained.add(item);
                        creatureItemsMap.put(item.getOwnerId(), contained);
                    }
                    ++numCoins;
                }
                catch (NoSuchTemplateException nst) {
                    logger.log(Level.WARNING, "Problem getting Template for item", nst);
                }
            }
        }
        catch (SQLException sqx) {
            logger.log(Level.WARNING, "Failed to load coins " + sqx.getMessage(), sqx);
        }
        finally {
            DbUtilities.closeDatabaseObjects(ps, rs);
            DbConnector.returnConnection(dbcon);
        }
    }

    public static void loadAllItemsForNonPlayer(Creature creature, long inventoryId) {
        long cpS = System.nanoTime();
        if (logger.isLoggable(Level.FINEST)) {
            logger.finest("Loading items for creature " + creature.getWurmId());
        }
        Set<Item> creatureItems = creatureItemsMap.get(creature.getWurmId());
        cpOne += System.nanoTime() - cpS;
        cpS = System.nanoTime();
        if (creatureItems != null) {
            for (Item item : creatureItems) {
                item.getContainedItems();
            }
        }
        try {
            creature.loadPossessions(inventoryId);
        }
        catch (Exception ex) {
            logger.log(Level.WARNING, creature.getName() + " failed to load possessions - inventory not found " + ex.getMessage(), ex);
        }
        cpThree += System.nanoTime() - cpS;
        cpS = System.nanoTime();
        if (creatureItems == null || creatureItems.size() == 0) {
            return;
        }
        block9: for (Item item : creatureItems) {
            Item bp;
            int i;
            byte[] spaces;
            if (item.isInventory() || item.isBodyPart()) continue;
            try {
                Item parent = item.getParent();
                if (parent.isBodyPart()) {
                    if (Items.moveItemFromIncorrectSlot(item, parent, creature) || parent.getItems().contains(item) || parent.insertItem(item, false)) continue;
                    Items.resetParentToInventory(item, creature);
                    logger.log(Level.INFO, "INSERTED IN INVENTORY " + item.getName() + " for " + creature.getName() + " wid=" + item.getWurmId());
                    continue;
                }
                if (!parent.isInventory() || creature.isPlayer() || !creature.isHorse() && !creature.getTemplate().isHellHorse() && !creature.getTemplate().isKingdomGuard()) continue;
                spaces = item.getBodySpaces();
                for (i = 0; i < spaces.length; ++i) {
                    try {
                        bp = creature.getBody().getBodyPart(spaces[i]);
                        if (bp == null || !bp.testInsertItem(item)) continue;
                        bp.insertItem(item);
                        continue block9;
                    }
                    catch (NoSpaceException nse) {
                        logger.log(Level.INFO, "Unable to find body part, inserting in inventory");
                        Items.resetParentToInventory(item, creature);
                    }
                }
            }
            catch (NoSuchItemException nsi) {
                if (creature.isHorse() || creature.getTemplate().isHellHorse() || creature.getTemplate().isKingdomGuard()) {
                    spaces = item.getBodySpaces();
                    for (i = 0; i < spaces.length; ++i) {
                        try {
                            bp = creature.getBody().getBodyPart(spaces[i]);
                            if (bp == null || !bp.testInsertItem(item)) continue;
                            bp.insertItem(item);
                            continue block9;
                        }
                        catch (NoSpaceException nse) {
                            logger.log(Level.INFO, "Unable to find body part, inserting in inventory");
                            Items.resetParentToInventory(item, creature);
                        }
                    }
                    continue;
                }
                logger.log(Level.INFO, "Unable to find parent slot, inserting in inventory");
                Items.resetParentToInventory(item, creature);
            }
        }
        cpFour += System.nanoTime() - cpS;
    }

    public static Set<Item> loadAllItemsForCreature(Creature creature, long inventoryId) {
        long cpS = System.nanoTime();
        if (logger.isLoggable(Level.FINEST)) {
            logger.finest("Loading items for creature " + creature.getWurmId());
        }
        HashSet<Item> creatureItems = new HashSet<Item>();
        if (creature.isPlayer() && ((Player)creature).getSaveFile().hasMovedInventory()) {
            Items.returnItemsFromFreezerFor(creature.getWurmId());
            ((Player)creature).getSaveFile().setMovedInventory(false);
        }
        Items.loadAllItemsForCreatureAndItemtype(creature.getWurmId(), CoinDbStrings.getInstance(), creatureItems);
        cpOne += System.nanoTime() - cpS;
        cpS = System.nanoTime();
        Items.loadAllItemsForCreatureAndItemtype(creature.getWurmId(), ItemDbStrings.getInstance(), creatureItems);
        cpTwo += System.nanoTime() - cpS;
        cpS = System.nanoTime();
        for (Item item : creatureItems) {
            item.getContainedItems();
        }
        try {
            creature.loadPossessions(inventoryId);
        }
        catch (Exception ex) {
            logger.log(Level.WARNING, creature.getName() + " failed to load possessions - inventory not found " + ex.getMessage(), ex);
        }
        cpThree += System.nanoTime() - cpS;
        cpS = System.nanoTime();
        block9: for (Item item : creatureItems) {
            Item bp;
            int i;
            byte[] spaces;
            if (item.isInventory() || item.isBodyPart()) continue;
            try {
                Item parent = item.getParent();
                if (parent.isBodyPart()) {
                    if (Items.moveItemFromIncorrectSlot(item, parent, creature) || parent.getItems().contains(item) || parent.insertItem(item, false)) continue;
                    Items.resetParentToInventory(item, creature);
                    logger.log(Level.INFO, "Inserted in inventory " + item.getName() + " for " + creature.getName() + " wid=" + item.getWurmId());
                    continue;
                }
                if (!parent.isInventory() || creature.isPlayer() || !creature.isHorse() && !creature.getTemplate().isHellHorse() && !creature.getTemplate().isKingdomGuard()) continue;
                spaces = item.getBodySpaces();
                for (i = 0; i < spaces.length; ++i) {
                    try {
                        bp = creature.getBody().getBodyPart(spaces[i]);
                        if (bp == null || !bp.testInsertItem(item)) continue;
                        bp.insertItem(item);
                        continue block9;
                    }
                    catch (NoSpaceException nse) {
                        logger.log(Level.INFO, "Unable to find body part, inserting in inventory");
                        Items.resetParentToInventory(item, creature);
                    }
                }
            }
            catch (NoSuchItemException nsi) {
                if (creature.isHorse() || creature.getTemplate().isHellHorse() || creature.getTemplate().isKingdomGuard()) {
                    spaces = item.getBodySpaces();
                    for (i = 0; i < spaces.length; ++i) {
                        try {
                            bp = creature.getBody().getBodyPart(spaces[i]);
                            if (bp == null || !bp.testInsertItem(item)) continue;
                            bp.insertItem(item);
                            continue block9;
                        }
                        catch (NoSpaceException nse) {
                            logger.log(Level.INFO, "Unable to find body part, inserting in inventory");
                            Items.resetParentToInventory(item, creature);
                        }
                    }
                    continue;
                }
                logger.log(Level.INFO, "Unable to find parent slot, inserting in inventory");
                Items.resetParentToInventory(item, creature);
            }
        }
        cpFour += System.nanoTime() - cpS;
        cpS = System.nanoTime();
        return creatureItems;
    }

    private static boolean moveItemFromIncorrectSlot(Item item, Item parent, Creature creature) {
        if (!creature.isHuman() || item.isBodyPart() || item.isEquipmentSlot()) {
            return false;
        }
        if (item.isBelt()) {
            if (parent.isEquipmentSlot() && parent.getAuxData() == 22) {
                return false;
            }
            if (parent.isBodyPart()) {
                Items.resetParentToInventory(item, creature);
                return true;
            }
        } else {
            if (parent.isBodyPart() && item.isInventoryGroup()) {
                Items.resetParentToInventory(item, creature);
                return true;
            }
            if (parent.isBodyPart() && !parent.isEquipmentSlot()) {
                if (item.isArmour()) {
                    return false;
                }
                if (item.getTemplateId() == 231) {
                    return false;
                }
                if (!creature.isPlayer() && item.isWeapon()) {
                    return false;
                }
                Items.resetParentToInventory(item, creature);
                return true;
            }
        }
        return false;
    }

    public static void resetParentToInventory(Item item, Creature creature) {
        boolean found;
        Set<Item> contained = containedItems.get(new Long(item.getParentId()));
        if (contained != null && (found = contained.remove(item))) {
            logger.log(Level.INFO, "Success! removed the " + item.getName());
        }
        try {
            creature.getInventory().insertItem(item, false);
            logger.log(Level.INFO, "Inventory id: " + creature.getInventory().getWurmId() + ", item parent now: " + item.getParentId() + " owner id=" + creature.getWurmId() + " item owner id=" + item.getOwnerId());
            try {
                Item itemorig = Items.getItem(item.getWurmId());
                logger.log(Level.INFO, "Inventory id: " + creature.getInventory().getWurmId() + ", item parent now: " + itemorig.getParentId() + " owner id=" + itemorig.getOwnerId());
            }
            catch (Exception ex) {
                logger.log(Level.WARNING, "retrieval failed", ex);
            }
        }
        catch (Exception ex) {
            logger.log(Level.WARNING, "Inserting " + item + " into inventory instead for creature " + creature.getWurmId() + " failed: " + ex.getMessage(), ex);
        }
    }

    public static Set<Item> loadAllItemsForCreatureWithId(long creatureId, boolean frozen) {
        HashSet<Item> creatureItems = new HashSet<Item>();
        Items.loadAllItemsForCreatureAndItemtype(creatureId, CoinDbStrings.getInstance(), creatureItems);
        if (frozen) {
            Items.loadAllItemsForCreatureAndItemtype(creatureId, FrozenItemDbStrings.getInstance(), creatureItems);
        } else {
            Items.loadAllItemsForCreatureAndItemtype(creatureId, ItemDbStrings.getInstance(), creatureItems);
        }
        return creatureItems;
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public static void loadAllItemsForCreatureAndItemtype(long creatureId, DbStrings dbstrings, Set<Item> creatureItems) {
        Connection dbcon = null;
        PreparedStatement ps = null;
        ResultSet rs = null;
        try {
            dbcon = DbConnector.getItemDbCon();
            ps = dbcon.prepareStatement(dbstrings.getCreatureItems());
            ps.setLong(1, creatureId);
            rs = ps.executeQuery();
            long iid = -10L;
            while (rs.next()) {
                iid = rs.getLong("WURMID");
                try {
                    long pid;
                    ItemTemplate temp = ItemTemplateFactory.getInstance().getTemplate(rs.getInt("TEMPLATEID"));
                    Item item = null;
                    boolean load = true;
                    if (temp.alwaysLoaded) {
                        load = false;
                        try {
                            item = Items.getItem(iid);
                            item.setOwnerStuff(temp);
                        }
                        catch (NoSuchItemException nsi) {
                            load = true;
                        }
                    }
                    if (load) {
                        item = new DbItem(iid, temp, rs.getString("NAME"), rs.getLong("LASTMAINTAINED"), rs.getFloat("QUALITYLEVEL"), rs.getFloat("ORIGINALQUALITYLEVEL"), rs.getInt("SIZEX"), rs.getInt("SIZEY"), rs.getInt("SIZEZ"), rs.getFloat("POSX"), rs.getFloat("POSY"), rs.getFloat("POSZ"), rs.getFloat("ROTATION"), rs.getLong("PARENTID"), rs.getLong("OWNERID"), rs.getInt("ZONEID"), rs.getFloat("DAMAGE"), rs.getInt("WEIGHT"), rs.getByte("MATERIAL"), rs.getLong("LOCKID"), rs.getShort("PLACE"), rs.getInt("PRICE"), rs.getShort("TEMPERATURE"), rs.getString("DESCRIPTION"), rs.getByte("BLESS"), rs.getByte("ENCHANT"), rs.getBoolean("BANKED"), rs.getLong("LASTOWNERID"), rs.getByte("AUXDATA"), rs.getLong("CREATIONDATE"), rs.getByte("CREATIONSTATE"), rs.getInt("REALTEMPLATE"), rs.getBoolean("WORNARMOUR"), rs.getInt("COLOR"), rs.getInt("COLOR2"), rs.getBoolean("FEMALE"), rs.getBoolean("MAILED"), rs.getBoolean("TRANSFERRED"), rs.getString("CREATOR"), rs.getBoolean("HIDDEN"), rs.getByte("MAILTIMES"), rs.getByte("RARITY"), rs.getLong("ONBRIDGE"), rs.getInt("SETTINGS"), rs.getBoolean("PLACEDONPARENT"), dbstrings);
                    }
                    if ((pid = item.getParentId()) != -10L) {
                        Set<Item> contained = containedItems.get(new Long(pid));
                        if (contained == null) {
                            contained = new HashSet<Item>();
                        }
                        contained.add(item);
                        containedItems.put(new Long(pid), contained);
                    }
                    creatureItems.add(item);
                }
                catch (NoSuchTemplateException nst) {
                    logger.log(Level.WARNING, "Problem getting Template for item with Wurm ID " + iid + "  for creature " + creatureId + " - " + nst.getMessage(), nst);
                }
            }
        }
        catch (SQLException sqx) {
            try {
                logger.log(Level.WARNING, "Failed to load items for creature " + creatureId + ": " + sqx.getMessage(), sqx);
            }
            catch (Throwable throwable) {
                DbUtilities.closeDatabaseObjects(ps, rs);
                DbConnector.returnConnection(dbcon);
                throw throwable;
            }
            DbUtilities.closeDatabaseObjects(ps, rs);
            DbConnector.returnConnection(dbcon);
        }
        DbUtilities.closeDatabaseObjects(ps, rs);
        DbConnector.returnConnection(dbcon);
    }

    public static Set<Item> getContainedItems(long id) {
        Long lid = new Long(id);
        return containedItems.remove(lid);
    }

    static void loadAllStaticItems() {
        ItemTemplate[] templates;
        logger.log(Level.INFO, "Loading all static preloaded items");
        long start = System.nanoTime();
        for (ItemTemplate lTemplate : templates = ItemTemplateFactory.getInstance().getTemplates()) {
            if (!lTemplate.alwaysLoaded) continue;
            Items.loadAllStaticItems(lTemplate.getTemplateId());
        }
        float lElapsedTime = (float)(System.nanoTime() - start) / 1000000.0f;
        logger.log(Level.INFO, "Loaded all static preloaded items, that took " + lElapsedTime + " ms");
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     * Loose catch block
     */
    private static void loadAllStaticItems(int templateId) {
        ItemTemplate temp;
        if (logger.isLoggable(Level.FINER)) {
            logger.finer("Loading all static items for template ID " + templateId);
        }
        DbStrings dbstrings = Item.getDbStrings(templateId);
        long iid = -10L;
        Connection dbcon = null;
        PreparedStatement ps = null;
        ResultSet rs = null;
        try {
            dbcon = DbConnector.getItemDbCon();
            ps = dbcon.prepareStatement(dbstrings.getPreloadedItems());
            ps.setInt(1, templateId);
            rs = ps.executeQuery();
            temp = ItemTemplateFactory.getInstance().getTemplate(templateId);
            while (rs.next()) {
                iid = rs.getLong("WURMID");
                new DbItem(iid, temp, rs.getString("NAME"), rs.getLong("LASTMAINTAINED"), rs.getFloat("QUALITYLEVEL"), rs.getFloat("ORIGINALQUALITYLEVEL"), rs.getInt("SIZEX"), rs.getInt("SIZEY"), rs.getInt("SIZEZ"), rs.getFloat("POSX"), rs.getFloat("POSY"), rs.getFloat("POSZ"), rs.getFloat("ROTATION"), rs.getLong("PARENTID"), rs.getLong("OWNERID"), rs.getInt("ZONEID"), rs.getFloat("DAMAGE"), rs.getInt("WEIGHT"), rs.getByte("MATERIAL"), rs.getLong("LOCKID"), rs.getShort("PLACE"), rs.getInt("PRICE"), rs.getShort("TEMPERATURE"), rs.getString("DESCRIPTION"), rs.getByte("BLESS"), rs.getByte("ENCHANT"), rs.getBoolean("BANKED"), rs.getLong("LASTOWNERID"), rs.getByte("AUXDATA"), rs.getLong("CREATIONDATE"), rs.getByte("CREATIONSTATE"), rs.getInt("REALTEMPLATE"), rs.getBoolean("WORNARMOUR"), rs.getInt("COLOR"), rs.getInt("COLOR2"), rs.getBoolean("FEMALE"), rs.getBoolean("MAILED"), rs.getBoolean("TRANSFERRED"), rs.getString("CREATOR"), rs.getBoolean("HIDDEN"), rs.getByte("MAILTIMES"), rs.getByte("RARITY"), rs.getLong("ONBRIDGE"), rs.getInt("SETTINGS"), rs.getBoolean("PLACEDONPARENT"), dbstrings);
            }
        }
        catch (NoSuchTemplateException nst) {
            logger.log(Level.WARNING, "Problem getting Template ID " + templateId + " - " + nst.getMessage(), nst);
            DbUtilities.closeDatabaseObjects(ps, rs);
            DbConnector.returnConnection(dbcon);
        }
        catch (SQLException sqx) {
            logger.log(Level.WARNING, "Failed to load items for template " + templateId + " : " + sqx.getMessage(), sqx);
            {
                catch (Throwable throwable) {
                    DbUtilities.closeDatabaseObjects(ps, rs);
                    DbConnector.returnConnection(dbcon);
                    throw throwable;
                }
            }
            DbUtilities.closeDatabaseObjects(ps, rs);
            DbConnector.returnConnection(dbcon);
        }
        DbUtilities.closeDatabaseObjects(ps, rs);
        DbConnector.returnConnection(dbcon);
        if (dbstrings == ItemDbStrings.getInstance()) {
            dbstrings = FrozenItemDbStrings.getInstance();
            iid = -10L;
            try {
                dbcon = DbConnector.getItemDbCon();
                ps = dbcon.prepareStatement(dbstrings.getPreloadedItems());
                ps.setInt(1, templateId);
                rs = ps.executeQuery();
                temp = ItemTemplateFactory.getInstance().getTemplate(templateId);
                while (rs.next()) {
                    iid = rs.getLong("WURMID");
                    new DbItem(iid, temp, rs.getString("NAME"), rs.getLong("LASTMAINTAINED"), rs.getFloat("QUALITYLEVEL"), rs.getFloat("ORIGINALQUALITYLEVEL"), rs.getInt("SIZEX"), rs.getInt("SIZEY"), rs.getInt("SIZEZ"), rs.getFloat("POSX"), rs.getFloat("POSY"), rs.getFloat("POSZ"), rs.getFloat("ROTATION"), rs.getLong("PARENTID"), rs.getLong("OWNERID"), rs.getInt("ZONEID"), rs.getFloat("DAMAGE"), rs.getInt("WEIGHT"), rs.getByte("MATERIAL"), rs.getLong("LOCKID"), rs.getShort("PLACE"), rs.getInt("PRICE"), rs.getShort("TEMPERATURE"), rs.getString("DESCRIPTION"), rs.getByte("BLESS"), rs.getByte("ENCHANT"), rs.getBoolean("BANKED"), rs.getLong("LASTOWNERID"), rs.getByte("AUXDATA"), rs.getLong("CREATIONDATE"), rs.getByte("CREATIONSTATE"), rs.getInt("REALTEMPLATE"), rs.getBoolean("WORNARMOUR"), rs.getInt("COLOR"), rs.getInt("COLOR2"), rs.getBoolean("FEMALE"), rs.getBoolean("MAILED"), rs.getBoolean("TRANSFERRED"), rs.getString("CREATOR"), rs.getBoolean("HIDDEN"), rs.getByte("MAILTIMES"), rs.getByte("RARITY"), rs.getLong("ONBRIDGE"), rs.getInt("SETTINGS"), rs.getBoolean("PLACEDONPARENT"), dbstrings);
                }
            }
            catch (NoSuchTemplateException nst) {
                logger.log(Level.WARNING, "Problem getting Template ID " + templateId + " - " + nst.getMessage(), nst);
            }
            catch (SQLException sqx) {
                logger.log(Level.WARNING, "Failed to load frozen items for template " + templateId + " : " + sqx.getMessage(), sqx);
            }
            finally {
                DbUtilities.closeDatabaseObjects(ps, rs);
                DbConnector.returnConnection(dbcon);
            }
        }
    }

    static void deleteSpawnPoints() {
        Item[] _items = Items.getAllItems();
        int dist = 0;
        for (Item lItem : _items) {
            if (lItem.getTemplateId() != 521 || (dist = ((int)lItem.getPosX() >> 2) - ((int)lItem.getPosY() >> 2)) >= 2 || dist <= -2) continue;
            Items.destroyItem(lItem.getWurmId());
        }
    }

    public static void transferRegaliaForKingdom(byte kingdom, long newOwnerId) {
        if (Kingdoms.getKingdom(kingdom) != null && Kingdoms.getKingdom(kingdom).isCustomKingdom()) {
            Item[] _items;
            for (Item lItem : _items = Items.getAllItems()) {
                if (!lItem.isRoyal() || lItem.getOwnerId() == -10L || lItem.getKingdom() != kingdom || lItem.getOwnerId() == newOwnerId) continue;
                lItem.putInVoid();
                lItem.setTransferred(false);
                lItem.setBanked(false);
                try {
                    Creature c = Server.getInstance().getCreature(newOwnerId);
                    c.getInventory().insertItem(lItem);
                }
                catch (Exception ex) {
                    logger.log(Level.WARNING, ex.getMessage());
                }
            }
        }
    }

    public static void deleteRoyalItemForKingdom(byte kingdom, boolean onSurface, boolean destroy) {
        Item[] _items;
        boolean putOnGround = Kingdoms.getKingdom(kingdom).isCustomKingdom();
        for (Item lItem : _items = Items.getAllItems()) {
            if (!lItem.isRoyal()) continue;
            if (kingdom == 2 && lItem.getTemplateId() == 538) {
                lItem.updatePos();
                continue;
            }
            if (lItem.getKingdom() != kingdom) continue;
            if (!destroy && putOnGround) {
                int tilex = lItem.getTileX();
                int tiley = lItem.getTileY();
                try {
                    Creature owner = Server.getInstance().getCreature(lItem.getOwnerId());
                    tilex = owner.getTileX();
                    tiley = owner.getTileY();
                    lItem.setPosXY(owner.getPosX(), owner.getPosY());
                    owner.getCommunicator().sendAlertServerMessage("As a sign of abdication, you put the " + lItem.getName() + " at your feet.");
                }
                catch (Exception owner) {
                    // empty catch block
                }
                try {
                    Zone z = Zones.getZone(tilex, tiley, onSurface);
                    lItem.putInVoid();
                    lItem.setTransferred(false);
                    lItem.setBanked(false);
                    z.addItem(lItem);
                }
                catch (NoSuchZoneException nsz) {
                    logger.log(Level.WARNING, nsz.getMessage(), nsz);
                }
                continue;
            }
            Items.destroyItem(lItem.getWurmId());
        }
    }

    public static void deleteChristmasItems() {
        Item[] lItems;
        for (Item lLItem : lItems = Items.getAllItems()) {
            if (lLItem.getTemplateId() != 442) continue;
            Items.destroyItem(lLItem.getWurmId());
        }
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public static final void loadAllProtectedItems() {
        long start = System.nanoTime();
        Connection dbcon = null;
        PreparedStatement ps = null;
        ResultSet rs = null;
        try {
            dbcon = DbConnector.getItemDbCon();
            ps = dbcon.prepareStatement(loadProtectedCorpse);
            rs = ps.executeQuery();
            int num = 0;
            while (rs.next()) {
                protectedCorpses.add(rs.getLong("WURMID"));
                ++num;
            }
            float lElapsedTime = (float)(System.nanoTime() - start) / 1000000.0f;
            logger.log(Level.INFO, "Loaded " + num + " protected corpse entries, that took " + lElapsedTime + " ms");
        }
        catch (SQLException sqx) {
            try {
                logger.log(Level.WARNING, "Failed to load protected corpses: " + sqx.getMessage(), sqx);
            }
            catch (Throwable throwable) {
                DbUtilities.closeDatabaseObjects(ps, rs);
                DbConnector.returnConnection(dbcon);
                throw throwable;
            }
            DbUtilities.closeDatabaseObjects(ps, rs);
            DbConnector.returnConnection(dbcon);
        }
        DbUtilities.closeDatabaseObjects(ps, rs);
        DbConnector.returnConnection(dbcon);
        loadedCorpses = true;
    }

    public static final boolean isProtected(Item item) {
        return protectedCorpses.contains(item.getWurmId());
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public static final void setProtected(long wurmid, boolean isProtected) {
        block9: {
            block10: {
                if (isProtected) break block10;
                if (!protectedCorpses.remove(wurmid)) break block9;
                Connection dbcon = null;
                PreparedStatement ps = null;
                try {
                    dbcon = DbConnector.getItemDbCon();
                    ps = dbcon.prepareStatement(deleteProtectedCorpse);
                    ps.setLong(1, wurmid);
                    ps.execute();
                }
                catch (SQLException sqx) {
                    try {
                        logger.log(Level.WARNING, "Failed to set protected false " + wurmid + " : " + sqx.getMessage(), sqx);
                    }
                    catch (Throwable throwable) {
                        DbUtilities.closeDatabaseObjects(ps, null);
                        DbConnector.returnConnection(dbcon);
                        throw throwable;
                    }
                    DbUtilities.closeDatabaseObjects(ps, null);
                    DbConnector.returnConnection(dbcon);
                    break block9;
                }
                DbUtilities.closeDatabaseObjects(ps, null);
                DbConnector.returnConnection(dbcon);
                break block9;
            }
            if (!protectedCorpses.contains(wurmid)) {
                protectedCorpses.add(wurmid);
                Connection dbcon = null;
                PreparedStatement ps = null;
                try {
                    dbcon = DbConnector.getItemDbCon();
                    ps = dbcon.prepareStatement(insertProtectedCorpse);
                    ps.setLong(1, wurmid);
                    ps.execute();
                }
                catch (SQLException sqx) {
                    try {
                        logger.log(Level.WARNING, "Failed to add protected " + wurmid + " : " + sqx.getMessage(), sqx);
                    }
                    catch (Throwable throwable) {
                        DbUtilities.closeDatabaseObjects(ps, null);
                        DbConnector.returnConnection(dbcon);
                        throw throwable;
                    }
                    DbUtilities.closeDatabaseObjects(ps, null);
                    DbConnector.returnConnection(dbcon);
                }
                DbUtilities.closeDatabaseObjects(ps, null);
                DbConnector.returnConnection(dbcon);
            }
        }
    }

    public static final int getBattleCampControl(byte kingdom) {
        int nums = 0;
        for (Item item : Items.getWarTargets()) {
            if (item.getKingdom() != kingdom) continue;
            ++nums;
        }
        return nums;
    }

    public static final Item findMerchantContractFromId(long contractId) {
        for (Item item : Items.getAllItems()) {
            if (item.getTemplateId() != 300 || item.getData() != contractId) continue;
            return item;
        }
        return null;
    }

    static class EggCounter
    implements Runnable {
        EggCounter() {
        }

        @Override
        public void run() {
            if (logger.isLoggable(Level.FINE)) {
                logger.fine("Running newSingleThreadScheduledExecutor for calling Items.countEggs()");
            }
            try {
                long start = System.nanoTime();
                Items.countEggs();
                float lElapsedTime = (float)(System.nanoTime() - start) / 1000000.0f;
                if (lElapsedTime > (float)Constants.lagThreshold) {
                    logger.info("Finished calling Items.countEggs(), which took " + lElapsedTime + " millis.");
                }
            }
            catch (RuntimeException e) {
                logger.log(Level.WARNING, "Caught exception in ScheduledExecutorService while calling Items.countEggs()", e);
                throw e;
            }
        }
    }
}


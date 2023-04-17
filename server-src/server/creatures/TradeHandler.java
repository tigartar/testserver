/*
 * Decompiled with CFR 0.152.
 */
package com.wurmonline.server.creatures;

import com.wurmonline.server.Features;
import com.wurmonline.server.Items;
import com.wurmonline.server.MiscConstants;
import com.wurmonline.server.NoSuchItemException;
import com.wurmonline.server.Server;
import com.wurmonline.server.creatures.Creature;
import com.wurmonline.server.economy.Change;
import com.wurmonline.server.economy.Economy;
import com.wurmonline.server.economy.MonetaryConstants;
import com.wurmonline.server.economy.Shop;
import com.wurmonline.server.items.Item;
import com.wurmonline.server.items.ItemTemplate;
import com.wurmonline.server.items.ItemTemplateFactory;
import com.wurmonline.server.items.ItemTypes;
import com.wurmonline.server.items.NoSuchTemplateException;
import com.wurmonline.server.items.Trade;
import com.wurmonline.server.items.TradingWindow;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

public final class TradeHandler
implements MiscConstants,
ItemTypes,
MonetaryConstants {
    private static final Logger logger = Logger.getLogger(TradeHandler.class.getName());
    private Creature creature;
    private Trade trade;
    private boolean balanced = false;
    private boolean waiting = false;
    private final Map<Integer, Set<Item>> itemMap = new HashMap<Integer, Set<Item>>();
    private final Map<Integer, List<Item>> currentDemandMap = new HashMap<Integer, List<Item>>();
    private final Map<Integer, Set<Item>> purchaseMap = new HashMap<Integer, Set<Item>>();
    private static int maxPersonalItems = 50;
    private final Shop shop;
    private long pdemand = 0L;
    private static final float maxNums = 80.0f;
    private final boolean ownerTrade;
    private final Set<Item> fullPriceItems = new HashSet<Item>();
    private boolean hasOtherItems = false;

    TradeHandler(Creature aCreature, Trade _trade) {
        this.creature = aCreature;
        this.trade = _trade;
        this.shop = Economy.getEconomy().getShop(aCreature);
        if (this.shop.isPersonal()) {
            boolean bl = this.ownerTrade = this.shop.getOwnerId() == this.trade.creatureOne.getWurmId();
            if (this.ownerTrade) {
                this.trade.creatureOne.getCommunicator().sendSafeServerMessage(aCreature.getName() + " says, 'Welcome back, " + this.trade.creatureOne.getName() + "!'");
            } else {
                this.trade.creatureOne.getCommunicator().sendSafeServerMessage(aCreature.getName() + " says, 'I will not buy anything, but i can offer these things.'");
            }
        } else {
            this.ownerTrade = false;
            if (this.shop.getMoney() <= 1L) {
                if (this.trade.creatureOne.getPower() >= 3) {
                    this.trade.creatureOne.getCommunicator().sendSafeServerMessage(aCreature.getName() + " says, 'I only have " + this.shop.getMoney() + " and can't buy anything right now.'");
                } else {
                    this.trade.creatureOne.getCommunicator().sendSafeServerMessage(aCreature.getName() + " says, 'I am a bit low on money and can't buy anything right now.'");
                }
            } else if (this.shop.getMoney() < 100L) {
                if (this.trade.creatureOne.getPower() >= 3) {
                    this.trade.creatureOne.getCommunicator().sendSafeServerMessage(aCreature.getName() + " says, 'I only have " + new Change(this.shop.getMoney()).getChangeShortString() + ".'");
                } else {
                    this.trade.creatureOne.getCommunicator().sendSafeServerMessage(aCreature.getName() + " says, 'I am a bit low on money, but let's see what you have.'");
                }
            } else if (this.trade.creatureOne.getPower() >= 3) {
                this.trade.creatureOne.getCommunicator().sendSafeServerMessage(aCreature.getName() + " says, 'I have " + new Change(this.shop.getMoney()).getChangeShortString() + ".'");
            }
        }
    }

    void end() {
        this.creature = null;
        this.trade = null;
    }

    void addToInventory(Item item, long inventoryWindow) {
        if (this.trade != null) {
            if (inventoryWindow == 2L) {
                this.tradeChanged();
                if (logger.isLoggable(Level.FINEST) && item != null) {
                    logger.finest("Added " + item.getName() + " to his offer window.");
                }
            } else if (inventoryWindow == 1L) {
                if (logger.isLoggable(Level.FINEST) && item != null) {
                    logger.finest("Added " + item.getName() + " to my offer window.");
                }
            } else if (inventoryWindow == 3L) {
                if (logger.isLoggable(Level.FINEST) && item != null) {
                    logger.finest("Added " + item.getName() + " to his request window.");
                }
            } else if (inventoryWindow == 4L && logger.isLoggable(Level.FINEST) && item != null) {
                logger.finest("Added " + item.getName() + " to my request window.");
            }
        }
    }

    void tradeChanged() {
        this.balanced = false;
        this.waiting = false;
    }

    void addItemsToTrade() {
        if (this.trade != null) {
            boolean foundDeclaration = false;
            boolean foundWagonerContract = false;
            Set<Item> ite = this.creature.getInventory().getItems();
            Item[] itarr = ite.toArray(new Item[ite.size()]);
            TradingWindow myOffers = this.trade.getTradingWindow(1L);
            int templateId = -10;
            myOffers.startReceivingItems();
            for (int x = 0; x < itarr.length; ++x) {
                templateId = itarr[x].getTemplateId();
                Set<Item> its = this.itemMap.get(templateId);
                if (its == null) {
                    its = new HashSet<Item>();
                }
                its.add(itarr[x]);
                if (this.shop.isPersonal()) {
                    if (this.ownerTrade) {
                        myOffers.addItem(itarr[x]);
                    } else if (!itarr[x].isCoin()) {
                        myOffers.addItem(itarr[x]);
                    }
                } else {
                    if (itarr[x].getTemplateId() == 843) {
                        if (!Features.Feature.NAMECHANGE.isEnabled()) {
                            its.remove(itarr[x]);
                            Items.destroyItem(itarr[x].getWurmId());
                            continue;
                        }
                        foundDeclaration = true;
                    }
                    if (itarr[x].getTemplateId() == 1129) {
                        if (!Features.Feature.WAGONER.isEnabled()) {
                            its.remove(itarr[x]);
                            Items.destroyItem(itarr[x].getWurmId());
                            continue;
                        }
                        foundWagonerContract = true;
                    }
                    if (its.size() < 10 && itarr[x].getLockId() == -10L) {
                        myOffers.addItem(itarr[x]);
                    } else if (its.size() > 50) {
                        its.remove(itarr[x]);
                        Items.destroyItem(itarr[x].getWurmId());
                    }
                }
                this.itemMap.put(templateId, its);
            }
            if (!this.shop.isPersonal()) {
                Item item;
                Item inventory;
                boolean newMerchandise = false;
                if (Features.Feature.NAMECHANGE.isEnabled() && !foundDeclaration) {
                    try {
                        inventory = this.creature.getInventory();
                        item = Creature.createItem(843, 60 + Server.rand.nextInt(40));
                        inventory.insertItem(item);
                        newMerchandise = true;
                    }
                    catch (Exception ex) {
                        logger.log(Level.INFO, "Failed to create name change cert for creature.", ex);
                    }
                }
                if (Features.Feature.WAGONER.isEnabled() && !foundWagonerContract) {
                    try {
                        inventory = this.creature.getInventory();
                        item = Creature.createItem(1129, 60 + Server.rand.nextInt(40));
                        inventory.insertItem(item);
                        newMerchandise = true;
                    }
                    catch (Exception ex) {
                        logger.log(Level.INFO, "Failed to create wagoner contract for creature.", ex);
                    }
                }
                if (newMerchandise) {
                    this.trade.creatureOne.getCommunicator().sendSafeServerMessage(this.creature.getName() + " says, 'Oh, I forgot I have some new merchandise. Let us trade again and I will show them to you.'");
                }
            }
            myOffers.stopReceivingItems();
        }
    }

    public int getTraderSellPriceForItem(Item item, TradingWindow window) {
        if (item.isFullprice()) {
            return item.getValue();
        }
        if (this.shop.isPersonal() && item.getPrice() > 0) {
            return item.getPrice();
        }
        int numberSold = 1;
        if (window == this.trade.getCreatureOneRequestWindow() && (!this.shop.isPersonal() || this.shop.usesLocalPrice())) {
            if (item.isCombine()) {
                ItemTemplate temp = item.getTemplate();
                numberSold = Math.max(1, item.getWeightGrams() / temp.getWeightGrams());
            }
            Item[] whatPlayerWants = this.trade.getCreatureOneRequestWindow().getItems();
            for (int x = 0; x < whatPlayerWants.length; ++x) {
                if (whatPlayerWants[x] == item || whatPlayerWants[x].getTemplateId() != item.getTemplateId()) continue;
                if (whatPlayerWants[x].isCombine()) {
                    ItemTemplate temp = whatPlayerWants[x].getTemplate();
                    numberSold += Math.max(1, whatPlayerWants[x].getWeightGrams() / temp.getWeightGrams());
                    continue;
                }
                ++numberSold;
            }
        }
        double localPrice = this.shop.getLocalTraderSellPrice(item, 100, numberSold);
        if (logger.isLoggable(Level.FINEST)) {
            logger.finest("localSellPrice for " + item.getName() + "=" + localPrice + " numberSold=" + numberSold);
        }
        return (int)Math.max(2.0, localPrice * (double)this.shop.getPriceModifier());
    }

    public int getTraderBuyPriceForItem(Item item) {
        if (item.isFullprice()) {
            return item.getValue();
        }
        int price = 1;
        List<Item> itlist = this.currentDemandMap.get(item.getTemplateId());
        int extra = 1;
        if (itlist == null) {
            if (logger.isLoggable(Level.FINEST)) {
                logger.finest("Weird. We're trading items which don't exist.");
            }
            extra = 1;
        } else if (item.isCombine()) {
            ItemTemplate temp = item.getTemplate();
            for (Item i : itlist) {
                extra += Math.max(1, i.getWeightGrams() / temp.getWeightGrams());
            }
        } else {
            extra += itlist.size();
        }
        price = (int)this.shop.getLocalTraderBuyPrice(item, 1, extra);
        if (logger.isLoggable(Level.FINEST)) {
            logger.finest("localBuyPrice for " + item.getName() + "=" + price + " extra=" + extra + " price for extra+1=" + (int)this.shop.getLocalTraderBuyPrice(item, 1, extra + 1));
        }
        return Math.max(0, price);
    }

    private long getDiff() {
        ItemTemplate temp;
        int x;
        if (this.shop.isPersonal() && this.ownerTrade) {
            return 0L;
        }
        Item[] whatPlayerWants = this.trade.getCreatureOneRequestWindow().getItems();
        Item[] whatIWant = this.trade.getCreatureTwoRequestWindow().getItems();
        this.pdemand = 0L;
        long mydemand = 0L;
        int templateId = -10;
        this.fullPriceItems.clear();
        this.hasOtherItems = false;
        this.purchaseMap.clear();
        for (x = 0; x < whatPlayerWants.length; ++x) {
            if (whatPlayerWants[x].isFullprice()) {
                this.pdemand += (long)whatPlayerWants[x].getValue();
                this.fullPriceItems.add(whatPlayerWants[x]);
                continue;
            }
            double localPrice = 2.0;
            if (this.shop.isPersonal() && whatPlayerWants[x].getPrice() > 0) {
                localPrice = whatPlayerWants[x].getPrice();
                this.pdemand = (long)((double)this.pdemand + localPrice);
                continue;
            }
            templateId = whatPlayerWants[x].getTemplateId();
            int numberSold = 0;
            try {
                if (!this.shop.isPersonal() || this.shop.usesLocalPrice()) {
                    int tid = whatPlayerWants[x].getTemplateId();
                    Set<Item> itlist = this.purchaseMap.get(tid);
                    if (itlist == null) {
                        itlist = new HashSet<Item>();
                    }
                    itlist.add(whatPlayerWants[x]);
                    this.purchaseMap.put(tid, itlist);
                    if (whatPlayerWants[x].isCombine()) {
                        temp = ItemTemplateFactory.getInstance().getTemplate(templateId);
                        for (Item i : itlist) {
                            numberSold += Math.max(1, i.getWeightGrams() / temp.getWeightGrams());
                        }
                    } else {
                        numberSold = itlist.size();
                    }
                }
                localPrice = this.shop.getLocalTraderSellPrice(whatPlayerWants[x], 100, numberSold);
                if (logger.isLoggable(Level.FINEST)) {
                    logger.finest("LocalSellPrice for " + whatPlayerWants[x].getName() + "=" + localPrice + " mod=" + this.shop.getPriceModifier() + " sum=" + Math.max(2.0, localPrice * (double)this.shop.getPriceModifier()));
                }
                this.pdemand = (long)((double)this.pdemand + Math.max(2.0, localPrice * (double)this.shop.getPriceModifier()));
                continue;
            }
            catch (NoSuchTemplateException nst) {
                logger.log(Level.WARNING, nst.getMessage(), nst);
            }
        }
        this.purchaseMap.clear();
        for (x = 0; x < whatIWant.length; ++x) {
            if (whatIWant[x].isFullprice()) {
                mydemand += (long)whatIWant[x].getValue();
                continue;
            }
            this.hasOtherItems = true;
            if (!this.fullPriceItems.isEmpty()) continue;
            long price = 1L;
            int tid = whatIWant[x].getTemplateId();
            Set<Item> itlist = this.purchaseMap.get(tid);
            if (itlist == null) {
                itlist = new HashSet<Item>();
            }
            itlist.add(whatIWant[x]);
            this.purchaseMap.put(tid, itlist);
            int extra = 0;
            if (whatIWant[x].isCombine()) {
                try {
                    temp = ItemTemplateFactory.getInstance().getTemplate(whatIWant[x].getTemplateId());
                    for (Item i : itlist) {
                        extra += Math.max(1, i.getWeightGrams() / temp.getWeightGrams());
                    }
                }
                catch (NoSuchTemplateException nst) {
                    logger.log(Level.WARNING, nst.getMessage(), nst);
                }
            } else {
                extra = itlist.size();
            }
            price = this.shop.getLocalTraderBuyPrice(whatIWant[x], 1, extra);
            mydemand += Math.max(0L, price);
        }
        long diff = this.pdemand - mydemand;
        return diff;
    }

    public static final int getMaxNumPersonalItems() {
        return maxPersonalItems;
    }

    private void suckInterestingItems() {
        TradingWindow currWin = this.trade.getTradingWindow(2L);
        TradingWindow targetWin = this.trade.getTradingWindow(4L);
        Item[] offItems = currWin.getItems();
        Item[] setItems = targetWin.getItems();
        if (!this.shop.isPersonal()) {
            List<Item> itlist;
            this.currentDemandMap.clear();
            for (int x = 0; x < setItems.length; ++x) {
                int templateId = setItems[x].getTemplateId();
                itlist = this.currentDemandMap.get(templateId);
                if (itlist == null) {
                    itlist = new LinkedList<Item>();
                }
                itlist.add(setItems[x]);
                this.currentDemandMap.put(templateId, itlist);
            }
            int templateId = -10;
            targetWin.startReceivingItems();
            for (int x = 0; x < offItems.length; ++x) {
                if (!offItems[x].isArtifact() && offItems[x].isPurchased() && offItems[x].getLockId() == -10L) {
                    Item parent = offItems[x];
                    try {
                        parent = offItems[x].getParent();
                    }
                    catch (NoSuchItemException noSuchItemException) {
                        // empty catch block
                    }
                    if (offItems[x] != parent && !parent.isViewableBy(this.creature)) continue;
                    if (offItems[x].isHollow() && !offItems[x].isEmpty(true)) {
                        this.trade.creatureOne.getCommunicator().sendSafeServerMessage(this.creature.getName() + " says, 'Please empty the " + offItems[x].getName() + " first.'");
                        continue;
                    }
                    templateId = offItems[x].getTemplateId();
                    List<Item> itlist2 = this.currentDemandMap.get(templateId);
                    if (itlist2 == null) {
                        itlist2 = new LinkedList<Item>();
                    }
                    if (!((float)itlist2.size() < 80.0f)) continue;
                    currWin.removeItem(offItems[x]);
                    targetWin.addItem(offItems[x]);
                    itlist2.add(offItems[x]);
                    this.currentDemandMap.put(templateId, itlist2);
                    continue;
                }
                if (!offItems[x].isHomesteadDeed() && !offItems[x].isVillageDeed() || offItems[x].getData2() > 0) continue;
                templateId = offItems[x].getTemplateId();
                itlist = this.currentDemandMap.get(templateId);
                if (itlist == null) {
                    itlist = new LinkedList<Item>();
                }
                currWin.removeItem(offItems[x]);
                targetWin.addItem(offItems[x]);
                itlist.add(offItems[x]);
                this.currentDemandMap.put(templateId, itlist);
            }
            targetWin.stopReceivingItems();
        } else if (this.ownerTrade) {
            TradingWindow myOffers = this.trade.getTradingWindow(1L);
            Item[] currItems = myOffers.getItems();
            int size = 0;
            for (int c = 0; c < currItems.length; ++c) {
                if (currItems[c].isCoin()) continue;
                ++size;
            }
            if ((size += setItems.length) > maxPersonalItems) {
                this.trade.creatureOne.getCommunicator().sendNormalServerMessage(this.creature.getName() + " says, 'I cannot add more items to my stock right now.'");
            } else {
                TradingWindow hisReq = this.trade.getTradingWindow(3L);
                Item[] reqItems = hisReq.getItems();
                for (int c = 0; c < reqItems.length; ++c) {
                    if (reqItems[c].isCoin()) continue;
                    ++size;
                }
                if (size > maxPersonalItems) {
                    this.trade.creatureOne.getCommunicator().sendNormalServerMessage(this.creature.getName() + " says, 'I cannot add more items to my stock right now.'");
                } else {
                    targetWin.startReceivingItems();
                    for (int x = 0; x < offItems.length; ++x) {
                        if (offItems[x].getTemplateId() == 272 || offItems[x].getTemplateId() == 781 || offItems[x].isArtifact() || offItems[x].isRoyal() || (offItems[x].isVillageDeed() || offItems[x].isHomesteadDeed()) && offItems[x].hasData() || offItems[x].getTemplateId() == 300 && offItems[x].getData2() != -1) continue;
                        if (size > maxPersonalItems) {
                            if (!offItems[x].isCoin()) continue;
                            currWin.removeItem(offItems[x]);
                            targetWin.addItem(offItems[x]);
                            continue;
                        }
                        if (offItems[x].isLockable() && offItems[x].isLocked() || offItems[x].isHollow() && !offItems[x].isEmpty(true) && !offItems[x].isSealedByPlayer()) {
                            if (offItems[x].isLockable() && offItems[x].isLocked()) {
                                this.trade.creatureOne.getCommunicator().sendSafeServerMessage(this.creature.getName() + " says, 'I don't accept locked items any more. Sorry for the inconvenience.'");
                                continue;
                            }
                            this.trade.creatureOne.getCommunicator().sendSafeServerMessage(this.creature.getName() + " says, 'Please empty the " + offItems[x].getName() + " first.'");
                            continue;
                        }
                        currWin.removeItem(offItems[x]);
                        targetWin.addItem(offItems[x]);
                        ++size;
                    }
                    targetWin.stopReceivingItems();
                }
            }
        } else {
            targetWin.startReceivingItems();
            for (int x = 0; x < offItems.length; ++x) {
                if (!offItems[x].isCoin()) continue;
                Item parent = offItems[x];
                try {
                    parent = offItems[x].getParent();
                }
                catch (NoSuchItemException noSuchItemException) {
                    // empty catch block
                }
                if (offItems[x] != parent && !parent.isViewableBy(this.creature)) continue;
                currWin.removeItem(offItems[x]);
                targetWin.addItem(offItems[x]);
            }
            targetWin.stopReceivingItems();
        }
    }

    void balance() {
        if (!this.balanced) {
            if (this.ownerTrade) {
                this.suckInterestingItems();
                this.trade.setSatisfied(this.creature, true, this.trade.getCurrentCounter());
                this.balanced = true;
            } else if (this.shop.isPersonal() && !this.waiting) {
                this.suckInterestingItems();
                this.removeCoins(this.trade.getCreatureOneRequestWindow().getItems());
                long diff = this.getDiff();
                if (diff > 0L) {
                    this.waiting = true;
                    Change change = new Change(diff);
                    String toSend = this.creature.getName() + " demands ";
                    toSend = toSend + change.getChangeString();
                    toSend = toSend + " coins to make the trade.";
                    this.trade.creatureOne.getCommunicator().sendSafeServerMessage(toSend);
                } else if (diff < 0L) {
                    Item[] mon = Economy.getEconomy().getCoinsFor(Math.abs(diff));
                    this.trade.getCreatureOneRequestWindow().startReceivingItems();
                    for (int x = 0; x < mon.length; ++x) {
                        this.trade.getCreatureOneRequestWindow().addItem(mon[x]);
                    }
                    this.trade.getCreatureOneRequestWindow().stopReceivingItems();
                    this.trade.setSatisfied(this.creature, true, this.trade.getCurrentCounter());
                    this.trade.setMoneyAdded(this.pdemand);
                    this.balanced = true;
                } else {
                    this.trade.setMoneyAdded(this.pdemand);
                    this.trade.setSatisfied(this.creature, true, this.trade.getCurrentCounter());
                    this.balanced = true;
                }
            } else if (!this.waiting) {
                this.suckInterestingItems();
                this.removeCoins(this.trade.getCreatureOneRequestWindow().getItems());
                long diff = this.getDiff();
                if (logger.isLoggable(Level.FINEST)) {
                    logger.finest("diff is " + diff);
                }
                if (!this.fullPriceItems.isEmpty() && this.hasOtherItems) {
                    for (Item lItem : this.fullPriceItems) {
                        this.trade.creatureOne.getCommunicator().sendSafeServerMessage(this.creature.getName() + " says, 'Sorry, " + this.trade.creatureOne.getName() + ". I must charge full price in coin value for the " + lItem.getName() + ".'");
                    }
                    this.waiting = true;
                } else if (diff > 0L) {
                    this.waiting = true;
                    Change change = new Change(diff);
                    String toSend = this.creature.getName() + " demands ";
                    toSend = toSend + change.getChangeString();
                    toSend = toSend + " coins to make the trade.";
                    this.trade.creatureOne.getCommunicator().sendSafeServerMessage(toSend);
                } else if (diff < 0L) {
                    if (Math.abs(diff) > this.shop.getMoney()) {
                        for (Item i : this.trade.getCreatureTwoRequestWindow().getAllItems()) {
                            if (i.isCoin()) continue;
                            String toSend = this.creature.getName() + " says, 'I am low on cash and can not purchase those items.'";
                            this.waiting = true;
                            this.trade.creatureOne.getCommunicator().sendSafeServerMessage(toSend);
                            return;
                        }
                        Item[] mon = Economy.getEconomy().getCoinsFor(Math.abs(diff));
                        this.trade.getCreatureOneRequestWindow().startReceivingItems();
                        for (int x = 0; x < mon.length; ++x) {
                            this.trade.getCreatureOneRequestWindow().addItem(mon[x]);
                        }
                        this.trade.getCreatureOneRequestWindow().stopReceivingItems();
                        this.trade.setSatisfied(this.creature, true, this.trade.getCurrentCounter());
                        this.balanced = true;
                    } else {
                        Item[] mon = Economy.getEconomy().getCoinsFor(Math.abs(diff));
                        this.trade.getCreatureOneRequestWindow().startReceivingItems();
                        for (int x = 0; x < mon.length; ++x) {
                            this.trade.getCreatureOneRequestWindow().addItem(mon[x]);
                        }
                        this.trade.getCreatureOneRequestWindow().stopReceivingItems();
                        this.trade.setSatisfied(this.creature, true, this.trade.getCurrentCounter());
                        this.balanced = true;
                    }
                } else {
                    this.trade.setSatisfied(this.creature, true, this.trade.getCurrentCounter());
                    this.balanced = true;
                }
            }
        }
    }

    private boolean removeCoins(Item[] items) {
        boolean foundCoins = false;
        for (int x = 0; x < items.length; ++x) {
            if (!items[x].isCoin()) continue;
            foundCoins = true;
            this.trade.getCreatureOneRequestWindow().removeItem(items[x]);
        }
        return foundCoins;
    }
}


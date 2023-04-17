/*
 * Decompiled with CFR 0.152.
 */
package com.wurmonline.server.economy;

import com.wurmonline.server.economy.Economy;
import java.util.logging.Level;
import java.util.logging.Logger;

public abstract class SupplyDemand {
    final int id;
    int itemsBought;
    int itemsSold;
    private static final Logger logger = Logger.getLogger(SupplyDemand.class.getName());
    long lastPolled;

    SupplyDemand(int aId, int aItemsBought, int aItemsSold) {
        this.id = aId;
        this.itemsBought = aItemsBought;
        this.itemsSold = aItemsSold;
        if (!this.supplyDemandExists()) {
            this.createSupplyDemand(aItemsBought, aItemsSold);
        } else {
            logger.log(Level.INFO, "Creating supply demand for already existing id: " + aId);
        }
        Economy.addSupplyDemand(this);
    }

    SupplyDemand(int aId, int aItemsBought, int aItemsSold, long aLastPolled) {
        this.id = aId;
        this.itemsBought = aItemsBought;
        this.itemsSold = aItemsSold;
        this.lastPolled = aLastPolled;
        Economy.addSupplyDemand(this);
    }

    public final float getDemandMod(int extraSold) {
        return Math.max(1000.0f, (float)this.itemsSold) / Math.max(1000.0f, (float)this.itemsBought + (float)extraSold);
    }

    public final int getItemsBoughtByTraders() {
        return this.itemsBought;
    }

    public final int getItemsSoldByTraders() {
        return this.itemsSold;
    }

    final void addItemBoughtByTrader() {
        this.updateItemsBoughtByTraders(this.itemsBought + 1);
    }

    final void addItemSoldByTrader() {
        this.updateItemsSoldByTraders(this.itemsSold + 1);
    }

    public final int getId() {
        return this.id;
    }

    public final int getPool() {
        return this.itemsBought - this.itemsSold;
    }

    public final long getLastPolled() {
        return this.lastPolled;
    }

    abstract void updateItemsBoughtByTraders(int var1);

    abstract void updateItemsSoldByTraders(int var1);

    abstract void createSupplyDemand(int var1, int var2);

    abstract boolean supplyDemandExists();

    abstract void reset(long var1);

    public final String toString() {
        return "SupplyDemand [TemplateID: " + this.id + ", Items bought:" + this.itemsBought + ", Sold:" + this.itemsSold + ", Pool: " + this.getPool() + ", Time last polled: " + this.lastPolled + ']';
    }
}


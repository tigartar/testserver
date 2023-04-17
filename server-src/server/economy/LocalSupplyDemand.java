/*
 * Decompiled with CFR 0.152.
 */
package com.wurmonline.server.economy;

import com.wurmonline.server.DbConnector;
import com.wurmonline.server.economy.ItemDemand;
import com.wurmonline.server.items.ItemTemplate;
import com.wurmonline.server.items.ItemTemplateFactory;
import com.wurmonline.server.items.NoSuchTemplateException;
import com.wurmonline.server.utils.DbUtilities;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

public final class LocalSupplyDemand {
    private final Map<Integer, Float> demandList;
    private final long traderId;
    private static final Logger logger = Logger.getLogger(LocalSupplyDemand.class.getName());
    private static final float MAX_DEMAND = -200.0f;
    private static final float INITIAL_DEMAND = -100.0f;
    private static final float MIN_DEMAND = -0.001f;
    private static final String GET_ALL_ITEM_DEMANDS = "SELECT * FROM LOCALSUPPLYDEMAND WHERE TRADERID=?";
    private static final String UPDATE_DEMAND = "UPDATE LOCALSUPPLYDEMAND SET DEMAND=? WHERE ITEMID=? AND TRADERID=?";
    private static final String INCREASE_ALL_DEMANDS = DbConnector.isUseSqlite() ? "UPDATE LOCALSUPPLYDEMAND SET DEMAND=MAX(-200.0,DEMAND*1.1)" : "UPDATE LOCALSUPPLYDEMAND SET DEMAND=GREATEST(-200.0,DEMAND*1.1)";
    private static final String CREATE_DEMAND = "INSERT INTO LOCALSUPPLYDEMAND (DEMAND,ITEMID,TRADERID) VALUES(?,?,?)";

    LocalSupplyDemand(long aTraderId) {
        this.traderId = aTraderId;
        this.demandList = new HashMap<Integer, Float>();
        this.loadAllItemDemands();
        this.createUnexistingDemands();
    }

    double getPrice(int itemTemplateId, double basePrice, int nums, boolean selling) {
        Float dem = this.demandList.get(itemTemplateId);
        float demand = -100.0f;
        if (dem != null) {
            demand = dem.floatValue();
        }
        double price = 1.0;
        float halfSize = 100.0f;
        try {
            halfSize = ItemTemplateFactory.getInstance().getTemplate((int)itemTemplateId).priceHalfSize;
        }
        catch (NoSuchTemplateException nst) {
            logger.log(Level.WARNING, nst.getMessage(), nst);
        }
        for (int x = 0; x < nums; ++x) {
            if (selling) {
                price = basePrice * Math.max((double)0.2f, Math.pow((double)demand / (double)halfSize, 2.0));
                demand = Math.max(-200.0f, demand - 1.0f);
                continue;
            }
            demand = Math.min(-0.001f, demand + 1.0f);
            price = basePrice * Math.pow((double)demand / (double)halfSize, 2.0);
        }
        return Math.max(0.0, price);
    }

    public void addItemSold(int itemTemplateId, float times) {
        Float dem = this.demandList.get(itemTemplateId);
        float demand = -100.0f;
        if (dem != null) {
            demand = dem.floatValue();
        }
        demand -= times;
        demand = Math.max(-200.0f, demand);
        this.demandList.put(itemTemplateId, new Float(demand));
        if (dem == null) {
            this.createDemand(itemTemplateId, demand);
        } else {
            this.updateDemand(itemTemplateId, demand);
        }
    }

    public void addItemPurchased(int itemTemplateId, float times) {
        Float dem = this.demandList.get(itemTemplateId);
        float demand = -100.0f;
        if (dem != null) {
            demand = dem.floatValue();
        }
        demand = Math.min(-0.001f, demand + times);
        this.demandList.put(itemTemplateId, new Float(demand));
        if (dem == null) {
            this.createDemand(itemTemplateId, demand);
        } else {
            this.updateDemand(itemTemplateId, demand);
        }
    }

    public void lowerDemands() {
        ItemDemand[] dems;
        for (ItemDemand lDem : dems = this.getItemDemands()) {
            lDem.setDemand(Math.max(-200.0f, lDem.getDemand() * 1.1f));
            this.demandList.put(lDem.getTemplateId(), new Float(lDem.getDemand()));
        }
    }

    public ItemDemand[] getItemDemands() {
        ItemDemand[] dems = new ItemDemand[]{};
        if (this.demandList.size() > 0) {
            dems = new ItemDemand[this.demandList.size()];
            int x = 0;
            for (Map.Entry<Integer, Float> entry : this.demandList.entrySet()) {
                int item = entry.getKey();
                float demand = entry.getValue().floatValue();
                dems[x] = new ItemDemand(item, demand);
                ++x;
            }
        }
        return dems;
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    private void loadAllItemDemands() {
        long start = System.currentTimeMillis();
        Connection dbcon = null;
        PreparedStatement ps = null;
        ResultSet rs = null;
        try {
            dbcon = DbConnector.getEconomyDbCon();
            ps = dbcon.prepareStatement(GET_ALL_ITEM_DEMANDS);
            ps.setLong(1, this.traderId);
            rs = ps.executeQuery();
            while (rs.next()) {
                this.demandList.put(rs.getInt("ITEMID"), new Float(Math.min(-0.001f, rs.getFloat("DEMAND"))));
            }
        }
        catch (SQLException sqx) {
            try {
                logger.log(Level.WARNING, "Failed to load supplyDemand for trader " + this.traderId + ": " + sqx.getMessage(), sqx);
            }
            catch (Throwable throwable) {
                DbUtilities.closeDatabaseObjects(ps, rs);
                DbConnector.returnConnection(dbcon);
                if (logger.isLoggable(Level.FINER)) {
                    long end = System.currentTimeMillis();
                    logger.finer("Loading LocalSupplyDemand for Trader: " + this.traderId + " took " + (end - start) + " ms");
                }
                throw throwable;
            }
            DbUtilities.closeDatabaseObjects(ps, rs);
            DbConnector.returnConnection(dbcon);
            if (logger.isLoggable(Level.FINER)) {
                long end = System.currentTimeMillis();
                logger.finer("Loading LocalSupplyDemand for Trader: " + this.traderId + " took " + (end - start) + " ms");
            }
        }
        DbUtilities.closeDatabaseObjects(ps, rs);
        DbConnector.returnConnection(dbcon);
        if (logger.isLoggable(Level.FINER)) {
            long end = System.currentTimeMillis();
            logger.finer("Loading LocalSupplyDemand for Trader: " + this.traderId + " took " + (end - start) + " ms");
        }
    }

    private void createUnexistingDemands() {
        ItemTemplate[] templates;
        for (ItemTemplate lTemplate : templates = ItemTemplateFactory.getInstance().getTemplates()) {
            Float dem;
            if (!lTemplate.isPurchased() || (dem = this.demandList.get(lTemplate.getTemplateId())) != null) continue;
            this.createDemand(lTemplate.getTemplateId(), -100.0f);
            this.demandList.put(lTemplate.getTemplateId(), Float.valueOf(-100.0f));
        }
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    private void updateDemand(int itemId, float demand) {
        Connection dbcon = null;
        PreparedStatement ps = null;
        try {
            dbcon = DbConnector.getEconomyDbCon();
            ps = dbcon.prepareStatement(UPDATE_DEMAND);
            ps.setFloat(1, Math.min(-0.001f, demand));
            ps.setInt(2, itemId);
            ps.setLong(3, this.traderId);
            ps.executeUpdate();
        }
        catch (SQLException sqx) {
            try {
                logger.log(Level.WARNING, "Failed to update trader " + this.traderId + ": " + sqx.getMessage(), sqx);
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

    public static void increaseAllDemands() {
        Connection dbcon = null;
        PreparedStatement ps = null;
        try {
            dbcon = DbConnector.getEconomyDbCon();
            ps = dbcon.prepareStatement(INCREASE_ALL_DEMANDS);
            ps.executeUpdate();
        }
        catch (SQLException sqx) {
            try {
                logger.log(Level.WARNING, "Failed to increase all demands due to " + sqx.getMessage(), sqx);
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

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    private void createDemand(int itemId, float demand) {
        Connection dbcon = null;
        PreparedStatement ps = null;
        try {
            dbcon = DbConnector.getEconomyDbCon();
            ps = dbcon.prepareStatement(CREATE_DEMAND);
            ps.setFloat(1, Math.min(-0.001f, demand));
            ps.setInt(2, itemId);
            ps.setLong(3, this.traderId);
            ps.executeUpdate();
        }
        catch (SQLException sqx) {
            try {
                logger.log(Level.WARNING, "Failed to update trader " + this.traderId + ": " + sqx.getMessage(), sqx);
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


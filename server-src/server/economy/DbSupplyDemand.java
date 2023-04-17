/*
 * Decompiled with CFR 0.152.
 */
package com.wurmonline.server.economy;

import com.wurmonline.server.DbConnector;
import com.wurmonline.server.economy.SupplyDemand;
import com.wurmonline.server.utils.DbUtilities;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.logging.Level;
import java.util.logging.Logger;

final class DbSupplyDemand
extends SupplyDemand {
    private static final Logger logger = Logger.getLogger(DbSupplyDemand.class.getName());
    private static final String UPDATE_BOUGHT_ITEMS = "UPDATE SUPPLYDEMAND SET ITEMSBOUGHT=? WHERE ID=?";
    private static final String UPDATE_SOLD_ITEMS = "UPDATE SUPPLYDEMAND SET ITEMSSOLD=? WHERE ID=?";
    private static final String CHECK_SUPLLY_DEMAND = "SELECT ID FROM SUPPLYDEMAND WHERE ID=?";
    private static final String RESET_SUPPLY_DEMAND = "DELETE FROM SUPPLYDEMAND WHERE ID=?";
    private static final String CREATE_SUPPLY_DEMAND = "INSERT INTO SUPPLYDEMAND (ID, ITEMSBOUGHT,ITEMSSOLD, LASTPOLLED) VALUES(?,?,?,?)";

    DbSupplyDemand(int aId, int aItemsBought, int aItemsSold) {
        super(aId, aItemsBought, aItemsSold);
    }

    DbSupplyDemand(int aId, int aItemsBought, int aItemsSold, long aLastPolled) {
        super(aId, aItemsBought, aItemsSold, aLastPolled);
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     * Enabled force condition propagation
     * Lifted jumps to return sites
     */
    @Override
    boolean supplyDemandExists() {
        boolean bl;
        Connection dbcon = null;
        PreparedStatement ps = null;
        ResultSet rs = null;
        try {
            dbcon = DbConnector.getEconomyDbCon();
            ps = dbcon.prepareStatement(CHECK_SUPLLY_DEMAND);
            ps.setInt(1, this.id);
            rs = ps.executeQuery();
            bl = rs.next();
        }
        catch (SQLException sqx) {
            try {
                logger.log(Level.WARNING, "Failed to check if supplyDemandExists for ID: " + this.id + " due to " + sqx.getMessage(), sqx);
            }
            catch (Throwable throwable) {
                DbUtilities.closeDatabaseObjects(ps, rs);
                DbConnector.returnConnection(dbcon);
                throw throwable;
            }
            DbUtilities.closeDatabaseObjects(ps, rs);
            DbConnector.returnConnection(dbcon);
            return false;
        }
        DbUtilities.closeDatabaseObjects(ps, rs);
        DbConnector.returnConnection(dbcon);
        return bl;
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    @Override
    void updateItemsBoughtByTraders(int items) {
        block4: {
            block5: {
                if (!this.supplyDemandExists()) break block5;
                if (this.itemsBought == items) break block4;
                Connection dbcon = null;
                PreparedStatement ps = null;
                try {
                    this.itemsBought = items;
                    dbcon = DbConnector.getEconomyDbCon();
                    ps = dbcon.prepareStatement(UPDATE_BOUGHT_ITEMS);
                    ps.setInt(1, this.itemsBought);
                    ps.setInt(2, this.id);
                    ps.executeUpdate();
                }
                catch (SQLException sqx) {
                    try {
                        logger.log(Level.WARNING, "Failed to update supplyDemand with ID: " + this.id + ", items: " + items + " due to " + sqx.getMessage(), sqx);
                    }
                    catch (Throwable throwable) {
                        DbUtilities.closeDatabaseObjects(ps, null);
                        DbConnector.returnConnection(dbcon);
                        throw throwable;
                    }
                    DbUtilities.closeDatabaseObjects(ps, null);
                    DbConnector.returnConnection(dbcon);
                    break block4;
                }
                DbUtilities.closeDatabaseObjects(ps, null);
                DbConnector.returnConnection(dbcon);
                break block4;
            }
            this.createSupplyDemand(1000 + items, 1000);
        }
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    @Override
    void updateItemsSoldByTraders(int items) {
        block4: {
            block5: {
                if (!this.supplyDemandExists()) break block5;
                if (this.itemsSold == items) break block4;
                Connection dbcon = null;
                PreparedStatement ps = null;
                try {
                    this.itemsSold = items;
                    dbcon = DbConnector.getEconomyDbCon();
                    ps = dbcon.prepareStatement(UPDATE_SOLD_ITEMS);
                    ps.setInt(1, this.itemsSold);
                    ps.setInt(2, this.id);
                    ps.executeUpdate();
                }
                catch (SQLException sqx) {
                    try {
                        logger.log(Level.WARNING, "Failed to update supplyDemand with ID: " + this.id + ", items: " + items + " due to " + sqx.getMessage(), sqx);
                    }
                    catch (Throwable throwable) {
                        DbUtilities.closeDatabaseObjects(ps, null);
                        DbConnector.returnConnection(dbcon);
                        throw throwable;
                    }
                    DbUtilities.closeDatabaseObjects(ps, null);
                    DbConnector.returnConnection(dbcon);
                    break block4;
                }
                DbUtilities.closeDatabaseObjects(ps, null);
                DbConnector.returnConnection(dbcon);
                break block4;
            }
            this.createSupplyDemand(1000, 1000 + items);
        }
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    @Override
    void createSupplyDemand(int aItemsBought, int aItemsSold) {
        Connection dbcon = null;
        PreparedStatement ps = null;
        try {
            this.lastPolled = System.currentTimeMillis();
            dbcon = DbConnector.getEconomyDbCon();
            ps = dbcon.prepareStatement(CREATE_SUPPLY_DEMAND);
            ps.setInt(1, this.id);
            ps.setInt(2, aItemsBought);
            ps.setInt(3, aItemsSold);
            ps.setLong(4, this.lastPolled);
            ps.executeUpdate();
        }
        catch (SQLException sqx) {
            try {
                logger.log(Level.WARNING, "Failed to create supplyDemand with ID: " + this.id + ", itemsBought: " + aItemsBought + ", itemsSold: " + aItemsSold + " due to " + sqx.getMessage(), sqx);
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
    @Override
    void reset(long time) {
        Connection dbcon = null;
        PreparedStatement ps = null;
        try {
            this.itemsBought = Math.max(1000, (int)((double)this.itemsBought * 0.99));
            this.itemsSold = Math.max(1000, (int)((double)this.itemsSold * 0.99));
            dbcon = DbConnector.getEconomyDbCon();
            ps = dbcon.prepareStatement(RESET_SUPPLY_DEMAND);
            ps.setInt(1, this.id);
            ps.executeUpdate();
            this.lastPolled = time;
            DbUtilities.closeDatabaseObjects(ps, null);
            ps = dbcon.prepareStatement(CREATE_SUPPLY_DEMAND);
            ps.setInt(1, this.id);
            ps.setInt(2, this.itemsBought);
            ps.setInt(3, this.itemsSold);
            ps.setLong(4, this.lastPolled);
            ps.executeUpdate();
        }
        catch (SQLException sqx) {
            try {
                logger.log(Level.WARNING, "Failed to reset supplyDemand with ID: " + this.id + ", time: " + time + " due to " + sqx.getMessage(), sqx);
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


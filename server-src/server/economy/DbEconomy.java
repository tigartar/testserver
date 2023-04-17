/*
 * Decompiled with CFR 0.152.
 */
package com.wurmonline.server.economy;

import com.wurmonline.server.DbConnector;
import com.wurmonline.server.Items;
import com.wurmonline.server.NoSuchItemException;
import com.wurmonline.server.Servers;
import com.wurmonline.server.economy.Change;
import com.wurmonline.server.economy.DbShop;
import com.wurmonline.server.economy.DbSupplyDemand;
import com.wurmonline.server.economy.Economy;
import com.wurmonline.server.economy.Shop;
import com.wurmonline.server.economy.SupplyDemand;
import com.wurmonline.server.items.Item;
import com.wurmonline.server.utils.DbUtilities;
import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.LinkedList;
import java.util.logging.Level;
import java.util.logging.Logger;

public final class DbEconomy
extends Economy {
    private static final Logger logger = Logger.getLogger(DbEconomy.class.getName());
    private static final String createEconomy = "insert into ECONOMY(ID, GOLDCOINS, SILVERCOINS, COPPERCOINS, IRONCOINS)values(?,?,?,?,?)";
    private static final String getEconomy = "SELECT * FROM ECONOMY WHERE ID=?";
    private static final String updateLastPolledTraders = "UPDATE ECONOMY SET LASTPOLLED=? WHERE ID=?";
    private static final String updateCreatedGold = "UPDATE ECONOMY SET GOLDCOINS=? WHERE ID=?";
    private static final String updateCreatedSilver = "UPDATE ECONOMY SET SILVERCOINS=? WHERE ID=?";
    private static final String updateCreatedCopper = "UPDATE ECONOMY SET COPPERCOINS=? WHERE ID=?";
    private static final String updateCreatedIron = "UPDATE ECONOMY SET IRONCOINS=? WHERE ID=?";
    private static final String logSoldItem = "INSERT INTO ITEMSSOLD (ITEMNAME,ITEMVALUE,TRADERNAME,PLAYERNAME, TEMPLATEID) VALUES(?,?,?,?,?)";
    private static final String getCoins = "SELECT * FROM COINS WHERE TEMPLATEID=? AND OWNERID=-10 AND PARENTID=-10 AND ZONEID=-10 AND BANKED=1 AND MAILED=0";
    private static final String getSupplyDemand = "SELECT * FROM SUPPLYDEMAND";
    private static final String getTraderMoney = "SELECT * FROM TRADER";
    private static final String createTransaction = "INSERT INTO TRANSACTS (ITEMID, OLDOWNERID,NEWOWNERID,REASON, VALUE) VALUES (?,?,?,?,?)";

    DbEconomy(int serverNumber) throws IOException {
        super(serverNumber);
    }

    @Override
    void initialize() throws IOException {
        PreparedStatement ps;
        Connection dbcon;
        block5: {
            dbcon = null;
            ps = null;
            try {
                dbcon = DbConnector.getEconomyDbCon();
                if (this.exists(dbcon)) {
                    this.load();
                    break block5;
                }
                ps = dbcon.prepareStatement(createEconomy);
                ps.setInt(1, this.id);
                ps.setLong(2, goldCoins);
                ps.setLong(3, silverCoins);
                ps.setLong(4, copperCoins);
                ps.setLong(5, ironCoins);
                ps.executeUpdate();
            }
            catch (SQLException sqx) {
                try {
                    throw new IOException(sqx);
                }
                catch (Throwable throwable) {
                    DbUtilities.closeDatabaseObjects(ps, null);
                    DbConnector.returnConnection(dbcon);
                    throw throwable;
                }
            }
        }
        DbUtilities.closeDatabaseObjects(ps, null);
        DbConnector.returnConnection(dbcon);
        this.loadSupplyDemand();
        this.loadShopMoney();
    }

    private void load() throws IOException {
        Connection dbcon = null;
        PreparedStatement ps = null;
        ResultSet rs = null;
        try {
            dbcon = DbConnector.getEconomyDbCon();
            ps = dbcon.prepareStatement(getEconomy);
            ps.setInt(1, this.id);
            rs = ps.executeQuery();
            if (rs.next()) {
                goldCoins = rs.getLong("GOLDCOINS");
                silverCoins = rs.getLong("SILVERCOINS");
                copperCoins = rs.getLong("COPPERCOINS");
                ironCoins = rs.getLong("IRONCOINS");
                lastPolledTraders = rs.getLong("LASTPOLLED");
            }
            DbUtilities.closeDatabaseObjects(ps, rs);
            Change change = new Change(ironCoins + copperCoins * 100L + silverCoins * 10000L + goldCoins * 1000000L);
            if (lastPolledTraders <= 0L) {
                lastPolledTraders = System.currentTimeMillis() - 2419200000L;
            }
            this.updateCreatedIron(change.getIronCoins());
            logger.log(Level.INFO, "Iron=" + ironCoins);
            this.updateCreatedCopper(change.getCopperCoins());
            logger.log(Level.INFO, "Copper=" + copperCoins);
            this.updateCreatedSilver(change.getSilverCoins());
            logger.log(Level.INFO, "Silver=" + silverCoins);
            this.updateCreatedGold(change.getGoldCoins());
            logger.log(Level.INFO, "Gold=" + goldCoins);
            this.loadCoins(50);
            this.loadCoins(54);
            this.loadCoins(58);
            this.loadCoins(53);
            this.loadCoins(57);
            this.loadCoins(61);
            this.loadCoins(51);
            this.loadCoins(55);
            this.loadCoins(59);
            this.loadCoins(52);
            this.loadCoins(56);
            this.loadCoins(60);
        }
        catch (SQLException sqx) {
            try {
                throw new IOException(sqx);
            }
            catch (Throwable throwable) {
                DbUtilities.closeDatabaseObjects(ps, rs);
                DbConnector.returnConnection(dbcon);
                throw throwable;
            }
        }
        DbUtilities.closeDatabaseObjects(ps, rs);
        DbConnector.returnConnection(dbcon);
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    private boolean exists(Connection dbcon) throws SQLException {
        boolean bl;
        PreparedStatement ps = null;
        ResultSet rs = null;
        try {
            ps = dbcon.prepareStatement(getEconomy);
            ps.setInt(1, this.id);
            rs = ps.executeQuery();
            bl = rs.next();
        }
        catch (Throwable throwable) {
            DbUtilities.closeDatabaseObjects(ps, rs);
            throw throwable;
        }
        DbUtilities.closeDatabaseObjects(ps, rs);
        return bl;
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    @Override
    public void transaction(long itemId, long oldownerid, long newownerid, String newReason, long value) {
        if (DbConnector.isUseSqlite()) {
            return;
        }
        Connection dbcon = null;
        PreparedStatement ps = null;
        try {
            String reason = newReason.substring(0, Math.min(19, newReason.length()));
            dbcon = DbConnector.getEconomyDbCon();
            ps = dbcon.prepareStatement(createTransaction);
            ps.setLong(1, itemId);
            ps.setLong(2, oldownerid);
            ps.setLong(3, newownerid);
            ps.setString(4, reason);
            ps.setLong(5, value);
            ps.executeUpdate();
        }
        catch (SQLException sqx) {
            try {
                logger.log(Level.WARNING, "Failed to create transaction for itemId: " + itemId + " due to " + sqx.getMessage(), sqx);
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
    public void updateCreatedGold(long number) {
        Connection dbcon = null;
        PreparedStatement ps = null;
        try {
            goldCoins = number;
            dbcon = DbConnector.getEconomyDbCon();
            ps = dbcon.prepareStatement(updateCreatedGold);
            ps.setLong(1, goldCoins);
            ps.setInt(2, this.id);
            ps.executeUpdate();
        }
        catch (SQLException sqx) {
            try {
                logger.log(Level.WARNING, "Failed to update num gold: " + sqx.getMessage(), sqx);
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
    public void updateLastPolled() {
        Connection dbcon = null;
        PreparedStatement ps = null;
        try {
            lastPolledTraders = System.currentTimeMillis();
            dbcon = DbConnector.getEconomyDbCon();
            ps = dbcon.prepareStatement(updateLastPolledTraders);
            ps.setLong(1, lastPolledTraders);
            ps.setInt(2, this.id);
            ps.executeUpdate();
        }
        catch (SQLException sqx) {
            try {
                logger.log(Level.WARNING, "Failed to update last polled traders: " + sqx.getMessage(), sqx);
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
    public void updateCreatedSilver(long number) {
        Connection dbcon = null;
        PreparedStatement ps = null;
        try {
            silverCoins = number;
            dbcon = DbConnector.getEconomyDbCon();
            ps = dbcon.prepareStatement(updateCreatedSilver);
            ps.setLong(1, silverCoins);
            ps.setInt(2, this.id);
            ps.executeUpdate();
        }
        catch (SQLException sqx) {
            try {
                logger.log(Level.WARNING, "Failed to update num silver: " + sqx.getMessage(), sqx);
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
    public void updateCreatedCopper(long number) {
        Connection dbcon = null;
        PreparedStatement ps = null;
        try {
            copperCoins = number;
            dbcon = DbConnector.getEconomyDbCon();
            ps = dbcon.prepareStatement(updateCreatedCopper);
            ps.setLong(1, copperCoins);
            ps.setInt(2, this.id);
            ps.executeUpdate();
        }
        catch (SQLException sqx) {
            try {
                logger.log(Level.WARNING, "Failed to update num copper: " + sqx.getMessage(), sqx);
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
    public void updateCreatedIron(long number) {
        Connection dbcon = null;
        PreparedStatement ps = null;
        try {
            ironCoins = number;
            dbcon = DbConnector.getEconomyDbCon();
            ps = dbcon.prepareStatement(updateCreatedIron);
            ps.setLong(1, ironCoins);
            ps.setInt(2, this.id);
            ps.executeUpdate();
        }
        catch (SQLException sqx) {
            try {
                logger.log(Level.WARNING, "Failed to update num iron: " + sqx.getMessage(), sqx);
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
    private void loadCoins(int type) {
        LinkedList<Item> current = this.getListForCointype(type);
        Connection dbcon = null;
        PreparedStatement ps = null;
        ResultSet rs = null;
        try {
            dbcon = DbConnector.getItemDbCon();
            ps = dbcon.prepareStatement(getCoins);
            ps.setInt(1, type);
            rs = ps.executeQuery();
            while (rs.next()) {
                try {
                    Item toAdd = Items.getItem(rs.getLong("WURMID"));
                    current.add(toAdd);
                }
                catch (NoSuchItemException nsi) {
                    logger.log(Level.WARNING, "Failed to load coin: " + rs.getLong("WURMID"), nsi);
                }
            }
        }
        catch (SQLException sqx) {
            try {
                logger.log(Level.WARNING, "Failed to load coins: " + sqx.getMessage(), sqx);
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

    @Override
    public Shop createShop(long wurmid) {
        return new DbShop(wurmid, Servers.localServer.getInitialTraderIrons());
    }

    @Override
    public Shop createShop(long wurmid, long ownerid) {
        int coins = 0;
        if (ownerid == -10L) {
            coins = Servers.localServer.getInitialTraderIrons();
        }
        return new DbShop(wurmid, coins, ownerid);
    }

    @Override
    SupplyDemand createSupplyDemand(int aId) {
        return new DbSupplyDemand(aId, 1000, 1000);
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    @Override
    void loadSupplyDemand() {
        Connection dbcon = null;
        PreparedStatement ps = null;
        ResultSet rs = null;
        try {
            dbcon = DbConnector.getEconomyDbCon();
            ps = dbcon.prepareStatement(getSupplyDemand);
            rs = ps.executeQuery();
            while (rs.next()) {
                new DbSupplyDemand(rs.getInt("ID"), rs.getInt("ITEMSBOUGHT"), rs.getInt("ITEMSSOLD"), rs.getLong("LASTPOLLED"));
            }
        }
        catch (SQLException sqx) {
            try {
                logger.log(Level.WARNING, "Failed to load supplyDemand: " + sqx.getMessage(), sqx);
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
    @Override
    void loadShopMoney() {
        Connection dbcon = null;
        PreparedStatement ps = null;
        ResultSet rs = null;
        try {
            dbcon = DbConnector.getEconomyDbCon();
            ps = dbcon.prepareStatement(getTraderMoney);
            rs = ps.executeQuery();
            while (rs.next()) {
                new DbShop(rs.getLong("WURMID"), rs.getLong("MONEY"), rs.getLong("OWNER"), rs.getFloat("PRICEMODIFIER"), rs.getBoolean("FOLLOWGLOBALPRICE"), rs.getBoolean("USELOCALPRICE"), rs.getLong("LASTPOLLED"), rs.getFloat("TAX"), rs.getLong("SPENT"), rs.getLong("SPENTLIFE"), rs.getLong("EARNED"), rs.getLong("EARNEDLIFE"), rs.getLong("SPENTLASTMONTH"), rs.getLong("TAXPAID"), rs.getInt("NUMBEROFITEMS"), rs.getLong("WHENEMPTY"), true);
            }
        }
        catch (SQLException sqx) {
            try {
                logger.log(Level.WARNING, "Failed to load traderMoney: " + sqx.getMessage(), sqx);
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
    @Override
    public void addItemSoldByTraders(String name, long money, String traderName, String playerName, int templateId) {
        Connection dbcon = null;
        PreparedStatement ps = null;
        try {
            dbcon = DbConnector.getEconomyDbCon();
            ps = dbcon.prepareStatement(logSoldItem);
            ps.setString(1, name.substring(0, Math.min(29, name.length())));
            ps.setLong(2, money);
            ps.setString(3, traderName.substring(0, Math.min(29, traderName.length())));
            ps.setString(4, playerName.substring(0, Math.min(29, playerName.length())));
            ps.setInt(5, templateId);
            ps.executeUpdate();
        }
        catch (SQLException sqx) {
            try {
                logger.log(Level.WARNING, "Failed to update num iron: " + sqx.getMessage(), sqx);
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


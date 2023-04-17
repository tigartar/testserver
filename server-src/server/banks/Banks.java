/*
 * Decompiled with CFR 0.152.
 */
package com.wurmonline.server.banks;

import com.wurmonline.server.DbConnector;
import com.wurmonline.server.TimeConstants;
import com.wurmonline.server.banks.Bank;
import com.wurmonline.server.utils.DbUtilities;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

public final class Banks
implements TimeConstants {
    private static final Map<Long, Bank> banks = new HashMap<Long, Bank>();
    private static final String LOADBANKS = "SELECT * FROM BANKS";
    private static final String ISBANKED = "SELECT EXISTS(SELECT 1 FROM BANKS_ITEMS WHERE ITEMID=?) AS ISBANKED";
    private static final String BANKID = "SELECT BANKID FROM BANKS_ITEMS WHERE ITEMID=?";
    private static final String OWNEROFBANK = "SELECT OWNER FROM BANKS WHERE WURMID=?";
    private static final Logger logger = Logger.getLogger(Banks.class.getName());

    private Banks() {
    }

    private static final void addBank(Bank bank) {
        banks.put(new Long(bank.owner), bank);
    }

    public static final Bank getBank(long owner) {
        Bank bank = banks.get(new Long(owner));
        return bank;
    }

    public static final int getNumberOfBanks() {
        return banks.size();
    }

    public static final void poll(long now) {
        if (banks != null && !banks.isEmpty()) {
            boolean MULTI_THREADED_BANK_POLL = false;
            int NUMBER_OF_BANK_POLL_TASKS = 10;
            for (Bank bank : banks.values()) {
                bank.poll(now);
            }
        } else {
            logger.log(Level.FINE, "No banks to poll");
        }
    }

    public static boolean startBank(long owner, int size, int currentVillage) {
        if (banks.containsKey(new Long(owner))) {
            return false;
        }
        Bank bank = new Bank(owner, size, currentVillage);
        Banks.addBank(bank);
        return true;
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public static void loadAllBanks() {
        Connection dbcon = null;
        PreparedStatement ps = null;
        ResultSet rs = null;
        int loadedBanks = 0;
        long start = System.nanoTime();
        try {
            dbcon = DbConnector.getEconomyDbCon();
            ps = dbcon.prepareStatement(LOADBANKS);
            rs = ps.executeQuery();
            while (rs.next()) {
                long wurmid = rs.getLong("WURMID");
                long owner = rs.getLong("OWNER");
                long lastpolled = rs.getLong("LASTPOLLED");
                long startedMove = rs.getLong("STARTEDMOVE");
                int size = rs.getInt("SIZE");
                int currentVillage = rs.getInt("CURRENTVILLAGE");
                int targetVillage = rs.getInt("TARGETVILLAGE");
                Banks.addBank(new Bank(wurmid, owner, size, lastpolled, startedMove, currentVillage, targetVillage));
                ++loadedBanks;
            }
        }
        catch (SQLException sqx) {
            block5: {
                try {
                    logger.log(Level.WARNING, "Failed to load banks, SqlState: " + sqx.getSQLState() + ", ErrorCode: " + sqx.getErrorCode(), sqx);
                    SQLException lNext = sqx.getNextException();
                    if (lNext == null) break block5;
                    logger.log(Level.WARNING, "Failed to load banks, Next Exception", lNext);
                }
                catch (Throwable throwable) {
                    DbUtilities.closeDatabaseObjects(ps, rs);
                    DbConnector.returnConnection(dbcon);
                    long end = System.nanoTime();
                    logger.info("Loaded " + loadedBanks + " banks from database took " + (float)(end - start) / 1000000.0f + " ms");
                    throw throwable;
                }
            }
            DbUtilities.closeDatabaseObjects(ps, rs);
            DbConnector.returnConnection(dbcon);
            long end = System.nanoTime();
            logger.info("Loaded " + loadedBanks + " banks from database took " + (float)(end - start) / 1000000.0f + " ms");
        }
        DbUtilities.closeDatabaseObjects(ps, rs);
        DbConnector.returnConnection(dbcon);
        long end = System.nanoTime();
        logger.info("Loaded " + loadedBanks + " banks from database took " + (float)(end - start) / 1000000.0f + " ms");
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public static long itemInBank(long itemID) {
        long inBank = 0L;
        Connection dbcon = null;
        PreparedStatement ps = null;
        ResultSet rs = null;
        try {
            dbcon = DbConnector.getEconomyDbCon();
            ps = dbcon.prepareStatement(BANKID);
            ps.setLong(1, itemID);
            rs = ps.executeQuery();
            while (rs.next()) {
                inBank = rs.getLong("BANKID");
            }
        }
        catch (SQLException sqx) {
            block5: {
                try {
                    logger.log(Level.WARNING, "Failed execute ISBANKED, SqlState: " + sqx.getSQLState() + ", ErrorCode: " + sqx.getErrorCode(), sqx);
                    SQLException lNext = sqx.getNextException();
                    if (lNext == null) break block5;
                    logger.log(Level.WARNING, "Failed to execute ISBANKED, Next Exception", lNext);
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
        DbUtilities.closeDatabaseObjects(ps, rs);
        DbConnector.returnConnection(dbcon);
        return inBank;
    }

    public static final boolean isItemBanked(long itemID) {
        return Banks.itemInBank(itemID) != 0L;
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public static final long ownerOfBank(long bankID) {
        long ownerid = -10L;
        Connection dbcon = null;
        PreparedStatement ps = null;
        ResultSet rs = null;
        try {
            dbcon = DbConnector.getEconomyDbCon();
            ps = dbcon.prepareStatement(OWNEROFBANK);
            ps.setLong(1, bankID);
            rs = ps.executeQuery();
            while (rs.next()) {
                ownerid = rs.getLong("OWNER");
            }
        }
        catch (SQLException sqx) {
            block5: {
                try {
                    logger.log(Level.WARNING, "Failed execute ISBANKED, SqlState: " + sqx.getSQLState() + ", ErrorCode: " + sqx.getErrorCode(), sqx);
                    SQLException lNext = sqx.getNextException();
                    if (lNext == null) break block5;
                    logger.log(Level.WARNING, "Failed to execute ISBANKED, Next Exception", lNext);
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
        DbUtilities.closeDatabaseObjects(ps, rs);
        DbConnector.returnConnection(dbcon);
        return ownerid;
    }
}


/*
 * Decompiled with CFR 0.152.
 */
package com.wurmonline.server.players;

import com.wurmonline.server.DbConnector;
import com.wurmonline.server.players.PlayerInfoFactory;
import com.wurmonline.server.utils.DbUtilities;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.logging.Level;
import java.util.logging.Logger;

public class WurmRecord {
    private static final String GET_CHAMPIONRECORDS = "SELECT * FROM CHAMPIONS";
    private int value = 0;
    private String holder = "";
    private boolean current = true;
    private static final Logger logger = Logger.getLogger(WurmRecord.class.getName());

    public WurmRecord(int val, String name, boolean isCurrent) {
        this.value = val;
        this.holder = name;
        this.current = isCurrent;
    }

    public int getValue() {
        return this.value;
    }

    public void setValue(int aValue) {
        this.value = aValue;
    }

    public String getHolder() {
        return this.holder;
    }

    public void setHolder(String aHolder) {
        this.holder = aHolder;
    }

    public boolean isCurrent() {
        return this.current;
    }

    public void setCurrent(boolean aCurrent) {
        this.current = aCurrent;
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public static final void loadAllChampRecords() {
        long now = System.currentTimeMillis();
        Connection dbcon = null;
        PreparedStatement ps = null;
        ResultSet rs = null;
        try {
            dbcon = DbConnector.getPlayerDbCon();
            ps = dbcon.prepareStatement(GET_CHAMPIONRECORDS);
            rs = ps.executeQuery();
            while (rs.next()) {
                WurmRecord record = new WurmRecord(rs.getInt("VALUE"), rs.getString("NAME"), rs.getBoolean("CURRENT"));
                PlayerInfoFactory.addChampRecord(record);
            }
        }
        catch (SQLException sqex) {
            try {
                logger.log(Level.WARNING, "Failed to load champ records.");
            }
            catch (Throwable throwable) {
                DbUtilities.closeDatabaseObjects(ps, rs);
                DbConnector.returnConnection(dbcon);
                long end = System.currentTimeMillis();
                logger.info("Loaded " + PlayerInfoFactory.getChampionRecords().length + " champ records from the database took " + (end - now) + " ms");
                throw throwable;
            }
            DbUtilities.closeDatabaseObjects(ps, rs);
            DbConnector.returnConnection(dbcon);
            long end = System.currentTimeMillis();
            logger.info("Loaded " + PlayerInfoFactory.getChampionRecords().length + " champ records from the database took " + (end - now) + " ms");
        }
        DbUtilities.closeDatabaseObjects(ps, rs);
        DbConnector.returnConnection(dbcon);
        long end = System.currentTimeMillis();
        logger.info("Loaded " + PlayerInfoFactory.getChampionRecords().length + " champ records from the database took " + (end - now) + " ms");
    }
}


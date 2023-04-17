/*
 * Decompiled with CFR 0.152.
 */
package com.wurmonline.server.epic;

import com.wurmonline.server.DbConnector;
import com.wurmonline.server.epic.ValreiFightHistory;
import com.wurmonline.server.utils.DbUtilities;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.NoSuchElementException;
import java.util.TreeMap;
import java.util.logging.Level;
import java.util.logging.Logger;

public class ValreiFightHistoryManager {
    private static final Logger logger = Logger.getLogger(ValreiFightHistoryManager.class.getName());
    private static final String GET_MAX_FIGHTID = "SELECT MAX(FIGHTID) FROM ENTITYFIGHTS";
    private static final String LOAD_FIGHTS = "SELECT * FROM ENTITYFIGHTS";
    private static final String SAVE_FIGHT = "INSERT INTO ENTITYFIGHTS(FIGHTID,MAPHEXID,MAPHEXNAME,FIGHTTIME,FIGHTER1ID,FIGHTER1NAME,FIGHTER2ID,FIGHTER2NAME) VALUES (?,?,?,?,?,?,?,?)";
    private static ValreiFightHistoryManager instance;
    private static long fightIdCounter;
    private TreeMap<Long, ValreiFightHistory> allFights;

    public static long getNextFightId() {
        return ++fightIdCounter;
    }

    public static ValreiFightHistoryManager getInstance() {
        if (instance == null) {
            instance = new ValreiFightHistoryManager();
        }
        return instance;
    }

    private ValreiFightHistoryManager() {
        this.loadAllFights();
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    private void saveFight(long fightId, ValreiFightHistory newFight) {
        Connection dbcon = null;
        PreparedStatement ps = null;
        try {
            dbcon = DbConnector.getDeityDbCon();
            ps = dbcon.prepareStatement(SAVE_FIGHT);
            ps.setLong(1, fightId);
            ps.setInt(2, newFight.getMapHexId());
            ps.setString(3, newFight.getMapHexName());
            ps.setLong(4, newFight.getFightTime());
            HashMap<Long, ValreiFightHistory.ValreiFighter> fighters = newFight.getFighters();
            if (fighters.size() >= 2) {
                int val = 5;
                for (ValreiFightHistory.ValreiFighter f : fighters.values()) {
                    ps.setLong(val++, f.getFighterId());
                    ps.setString(val++, f.getName());
                    if (val < 8) continue;
                    break;
                }
            } else {
                ps.setLong(5, -1L);
                ps.setString(6, "Unknown");
                ps.setLong(7, -1L);
                ps.setString(8, "Unknown");
            }
            ps.executeUpdate();
            ps.close();
        }
        catch (SQLException sqx) {
            try {
                logger.log(Level.WARNING, "Failed to save valrei fight: " + sqx.getMessage(), sqx);
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
    private void loadAllFights() {
        this.allFights = new TreeMap();
        Connection dbcon = null;
        PreparedStatement ps = null;
        ResultSet rs = null;
        try {
            dbcon = DbConnector.getDeityDbCon();
            ps = dbcon.prepareStatement(LOAD_FIGHTS);
            rs = ps.executeQuery();
            while (rs.next()) {
                long fightId = rs.getLong("FIGHTID");
                int mapHexId = rs.getInt("MAPHEXID");
                String mapHexName = rs.getString("MAPHEXNAME");
                long fightTime = rs.getLong("FIGHTTIME");
                long fighter1 = rs.getLong("FIGHTER1ID");
                String fighter1Name = rs.getString("FIGHTER1NAME");
                long fighter2 = rs.getLong("FIGHTER2ID");
                String fighter2Name = rs.getString("FIGHTER2NAME");
                ValreiFightHistory oldFight = new ValreiFightHistory(fightId, mapHexId, mapHexName, fightTime);
                oldFight.addFighter(fighter1, fighter1Name);
                oldFight.addFighter(fighter2, fighter2Name);
                oldFight.loadActions();
                this.allFights.put(fightId, oldFight);
            }
            rs.close();
            ps.close();
        }
        catch (SQLException sqx) {
            try {
                logger.log(Level.WARNING, "Failed to load all valrei fights: " + sqx.getMessage(), sqx);
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

    public int getNumberOfFights() {
        return this.allFights.size();
    }

    public void addFight(long fightId, ValreiFightHistory newFight) {
        this.addFight(fightId, newFight, true);
    }

    public void addFight(long fightId, ValreiFightHistory newFight, boolean save) {
        this.allFights.put(fightId, newFight);
        if (save) {
            this.saveFight(fightId, newFight);
        }
    }

    public ValreiFightHistory getFight(long fightId) {
        return this.allFights.get(fightId);
    }

    public ArrayList<ValreiFightHistory> get10Fights(int listPage) {
        if (this.allFights.size() / 10 < listPage) {
            return null;
        }
        try {
            int i;
            ArrayList<ValreiFightHistory> toReturn = new ArrayList<ValreiFightHistory>();
            long finalKey = this.allFights.lastKey();
            for (i = 0; i < listPage; ++i) {
                for (int k = 0; k < 10 && this.allFights.lowerKey(finalKey) != null; ++k) {
                    finalKey = this.allFights.lowerKey(finalKey);
                }
            }
            for (i = 0; i < 10; ++i) {
                toReturn.add(this.allFights.get(finalKey));
                if (this.allFights.lowerKey(finalKey) == null) break;
                finalKey = this.allFights.lowerKey(finalKey);
            }
            return toReturn;
        }
        catch (NoSuchElementException ne) {
            logger.log(Level.WARNING, "Unable to load 10 fights for page " + listPage + ". No key exists in allFights map.");
            return null;
        }
    }

    public ArrayList<ValreiFightHistory> getAllFights() {
        return new ArrayList<ValreiFightHistory>(this.allFights.values());
    }

    public ValreiFightHistory getLatestFight() {
        return this.allFights.get(this.allFights.lastKey());
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    static {
        fightIdCounter = 1L;
        Connection dbcon = null;
        PreparedStatement ps = null;
        ResultSet rs = null;
        try {
            dbcon = DbConnector.getDeityDbCon();
            ps = dbcon.prepareStatement(GET_MAX_FIGHTID);
            rs = ps.executeQuery();
            if (rs.next()) {
                fightIdCounter = rs.getLong("MAX(FIGHTID)");
            }
            rs.close();
            ps.close();
        }
        catch (SQLException sqx) {
            try {
                logger.log(Level.WARNING, "Failed to load max fight id: " + sqx.getMessage(), sqx);
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
}


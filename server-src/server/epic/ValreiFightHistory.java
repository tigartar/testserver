/*
 * Decompiled with CFR 0.152.
 */
package com.wurmonline.server.epic;

import com.wurmonline.server.DbConnector;
import com.wurmonline.server.WurmCalendar;
import com.wurmonline.server.epic.ValreiFightHistoryManager;
import com.wurmonline.server.utils.DbUtilities;
import com.wurmonline.shared.constants.ValreiConstants;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.logging.Level;
import java.util.logging.Logger;

public class ValreiFightHistory {
    private static final Logger logger = Logger.getLogger(ValreiFightHistoryManager.class.getName());
    private static final String LOAD_FIGHT_ACTIONS = "SELECT * FROM ENTITYFIGHTACTIONS WHERE FIGHTID=?";
    private static final String SAVE_FIGHT_ACTION = "INSERT INTO ENTITYFIGHTACTIONS(FIGHTID,FIGHTACTIONNUM,ACTIONID,ACTIONDATA) VALUES (?,?,?,?)";
    private final long fightId;
    private int mapHexId;
    private String mapHexName;
    private long fightTime;
    private HashMap<Long, ValreiFighter> fighters;
    private HashMap<Integer, ValreiConstants.ValreiFightAction> allActions;
    private int fightActionNum;
    private boolean fightCompleted;

    public ValreiFightHistory(int mapHexId, String mapHexName) {
        this.mapHexId = mapHexId;
        this.mapHexName = mapHexName;
        this.fightId = ValreiFightHistoryManager.getNextFightId();
        this.fighters = new HashMap();
        this.allActions = new HashMap();
        this.fightActionNum = 0;
        this.fightCompleted = false;
        this.fightTime = WurmCalendar.currentTime;
    }

    public ValreiFightHistory(long fightId, int mapHexId, String mapHexName, long fightTime) {
        this.fightId = fightId;
        this.mapHexId = mapHexId;
        this.mapHexName = mapHexName;
        this.fightTime = fightTime;
        this.fighters = new HashMap();
        this.allActions = new HashMap();
        this.fightCompleted = true;
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public void saveActions() {
        Connection dbcon = null;
        PreparedStatement ps = null;
        try {
            dbcon = DbConnector.getDeityDbCon();
            for (ValreiConstants.ValreiFightAction fa : this.allActions.values()) {
                ps = dbcon.prepareStatement(SAVE_FIGHT_ACTION);
                ps.setLong(1, this.fightId);
                ps.setInt(2, fa.getActionNum());
                ps.setShort(3, fa.getActionId());
                ps.setBytes(4, fa.getActionData());
                ps.executeUpdate();
                ps.close();
            }
        }
        catch (SQLException sqx) {
            try {
                logger.log(Level.WARNING, "Failed to save actions for this valrei fight: " + sqx.getMessage(), sqx);
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
    public void loadActions() {
        Connection dbcon = null;
        PreparedStatement ps = null;
        ResultSet rs = null;
        try {
            dbcon = DbConnector.getDeityDbCon();
            ps = dbcon.prepareStatement(LOAD_FIGHT_ACTIONS);
            ps.setLong(1, this.fightId);
            rs = ps.executeQuery();
            while (rs.next()) {
                int actionNum = rs.getInt("FIGHTACTIONNUM");
                short action = rs.getShort("ACTIONID");
                byte[] actionData = rs.getBytes("ACTIONDATA");
                if (this.fightActionNum < actionNum) {
                    this.fightActionNum = actionNum;
                }
                this.allActions.put(actionNum, new ValreiConstants.ValreiFightAction(actionNum, action, actionData));
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

    public void addFighter(long fighterId, String fighterName) {
        if (this.fighters.get(fighterId) == null) {
            this.fighters.put(fighterId, new ValreiFighter(fighterId, fighterName));
        } else {
            ValreiFighter f = this.fighters.get(fighterId);
            f.setName(fighterName);
        }
    }

    public HashMap<Long, ValreiFighter> getFighters() {
        return this.fighters;
    }

    public void addAction(short actionType, byte[] actionData) {
        this.allActions.put(this.fightActionNum, new ValreiConstants.ValreiFightAction(this.fightActionNum, actionType, actionData));
        ++this.fightActionNum;
    }

    public ValreiConstants.ValreiFightAction getFightAction(int actionNum) {
        return this.allActions.get(actionNum);
    }

    public String getPreviewString() {
        if (!this.fighters.isEmpty()) {
            ArrayList<String> fighters = new ArrayList<String>();
            for (ValreiFighter vf : this.fighters.values()) {
                fighters.add(vf.fighterName);
            }
            return (String)fighters.get(0) + " vs " + (String)fighters.get(1) + " at " + this.getMapHexName() + " on " + WurmCalendar.getDateFor(this.fightTime);
        }
        return "Unknown fight on " + WurmCalendar.getDateFor(this.fightTime);
    }

    public long getFightWinner() {
        byte[] actionData = this.allActions.get(this.fightActionNum).getActionData();
        return ValreiConstants.getEndFightWinner(actionData);
    }

    public long getFightId() {
        return this.fightId;
    }

    public int getMapHexId() {
        return this.mapHexId;
    }

    public String getMapHexName() {
        return this.mapHexName;
    }

    public boolean isFightCompleted() {
        return this.fightCompleted;
    }

    public void setFightCompleted(boolean isCompleted) {
        this.fightCompleted = isCompleted;
        --this.fightActionNum;
    }

    public long getFightTime() {
        return this.fightTime;
    }

    public int getTotalActions() {
        return this.fightActionNum;
    }

    public class ValreiFighter {
        private long fighterId;
        private String fighterName;

        ValreiFighter(long id, String name) {
            this.setFighterId(id);
            this.fighterName = name;
        }

        public String getName() {
            return this.fighterName;
        }

        public void setName(String newName) {
            this.fighterName = newName;
        }

        public long getFighterId() {
            return this.fighterId;
        }

        public void setFighterId(long fighterId) {
            this.fighterId = fighterId;
        }
    }
}


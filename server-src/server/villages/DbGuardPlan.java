/*
 * Decompiled with CFR 0.152.
 */
package com.wurmonline.server.villages;

import com.wurmonline.server.DbConnector;
import com.wurmonline.server.WurmCalendar;
import com.wurmonline.server.WurmId;
import com.wurmonline.server.creatures.Creature;
import com.wurmonline.server.creatures.Creatures;
import com.wurmonline.server.creatures.NoSuchCreatureException;
import com.wurmonline.server.economy.Economy;
import com.wurmonline.server.utils.DbUtilities;
import com.wurmonline.server.villages.GuardPlan;
import com.wurmonline.server.villages.NoSuchVillageException;
import com.wurmonline.shared.constants.CounterTypes;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ListIterator;
import java.util.logging.Level;
import java.util.logging.Logger;

public final class DbGuardPlan
extends GuardPlan
implements CounterTypes {
    private static final Logger logger = Logger.getLogger(DbGuardPlan.class.getName());
    private static final String CREATE_GUARDPLAN = "INSERT INTO GUARDPLAN (VILLAGEID,  TYPE, LASTPAYED,MONEYLEFT, GUARDS) VALUES(?,?,?,?,?)";
    private static final String CHANGE_PLAN = "UPDATE GUARDPLAN SET LASTPAYED=?,TYPE=?,MONEYLEFT=?, GUARDS=? WHERE VILLAGEID=?";
    private static final String LOAD_PLAN = "SELECT * FROM GUARDPLAN WHERE VILLAGEID=?";
    private static final String DELETE_GUARDPLAN = "DELETE FROM GUARDPLAN WHERE VILLAGEID=?";
    private static final String ADD_RETURNEDGUARD = "INSERT INTO RETURNEDGUARDS (VILLAGEID, CREATUREID ) VALUES(?,?)";
    private static final String DELETE_RETURNEDGUARD = "DELETE FROM RETURNEDGUARDS WHERE CREATUREID=?";
    private static final String LOAD_RETURNEDGUARDS = "SELECT CREATUREID FROM RETURNEDGUARDS WHERE VILLAGEID=?";
    private static final String ADD_PAYMENT = "INSERT INTO GUARDPLANPAYMENTS (VILLAGEID, CREATUREID,MONEY,PAYED ) VALUES(?,?,?,?)";
    private static final String SET_LAST_DRAINED = "UPDATE GUARDPLAN SET LASTDRAINED=?, MONEYLEFT=? WHERE VILLAGEID=?";
    private static final String SET_DRAINMOD = "UPDATE GUARDPLAN SET DRAINMOD=? WHERE VILLAGEID=?";

    DbGuardPlan(int aType, int aVillageId) {
        super(aType, aVillageId);
    }

    DbGuardPlan(int aVillageId) {
        super(aVillageId);
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    @Override
    void load() {
        ResultSet rs;
        PreparedStatement ps;
        Connection dbcon;
        block5: {
            dbcon = null;
            ps = null;
            rs = null;
            try {
                dbcon = DbConnector.getZonesDbCon();
                ps = dbcon.prepareStatement(LOAD_PLAN);
                ps.setInt(1, this.villageId);
                rs = ps.executeQuery();
                boolean found = false;
                if (rs.next()) {
                    found = true;
                    this.type = rs.getInt("TYPE");
                    this.lastChangedPlan = rs.getLong("LASTPAYED");
                    this.moneyLeft = rs.getLong("MONEYLEFT");
                    this.lastDrained = rs.getLong("LASTDRAINED");
                    this.drainModifier = rs.getFloat("DRAINMOD");
                    this.hiredGuardNumber = rs.getInt("GUARDS");
                    this.loadReturnedGuards();
                }
                if (found) break block5;
                this.type = 1;
                this.create();
            }
            catch (SQLException sqx) {
                try {
                    logger.log(Level.WARNING, sqx.getMessage(), sqx);
                }
                catch (Throwable throwable) {
                    DbUtilities.closeDatabaseObjects(ps, rs);
                    DbConnector.returnConnection(dbcon);
                    throw throwable;
                }
                DbUtilities.closeDatabaseObjects(ps, rs);
                DbConnector.returnConnection(dbcon);
            }
        }
        DbUtilities.closeDatabaseObjects(ps, rs);
        DbConnector.returnConnection(dbcon);
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    @Override
    void create() {
        this.lastChangedPlan = 0L;
        this.moneyLeft = 0L;
        Connection dbcon = null;
        PreparedStatement ps = null;
        try {
            dbcon = DbConnector.getZonesDbCon();
            ps = dbcon.prepareStatement(CREATE_GUARDPLAN);
            ps.setInt(1, this.villageId);
            ps.setInt(2, this.type);
            ps.setLong(3, this.lastChangedPlan);
            ps.setLong(4, this.moneyLeft);
            ps.setInt(5, this.hiredGuardNumber);
            ps.executeUpdate();
        }
        catch (SQLException sqx) {
            try {
                logger.log(Level.WARNING, sqx.getMessage(), sqx);
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
    public void updateGuardPlan(int aType, long aMoneyLeft, int newNumberOfHiredGuards) {
        this.type = aType;
        this.moneyLeft = aMoneyLeft;
        this.hiredGuardNumber = newNumberOfHiredGuards;
        Connection dbcon = null;
        PreparedStatement ps = null;
        try {
            dbcon = DbConnector.getZonesDbCon();
            ps = dbcon.prepareStatement(CHANGE_PLAN);
            ps.setLong(1, this.lastChangedPlan);
            ps.setInt(2, aType);
            ps.setLong(3, aMoneyLeft);
            ps.setInt(4, this.hiredGuardNumber);
            ps.setInt(5, this.villageId);
            ps.executeUpdate();
        }
        catch (SQLException sqx) {
            try {
                logger.log(Level.WARNING, sqx.getMessage(), sqx);
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
    void drainGuardPlan(long aMoneyLeft) {
        this.moneyLeft = aMoneyLeft;
        this.lastDrained = System.currentTimeMillis();
        Connection dbcon = null;
        PreparedStatement ps = null;
        try {
            dbcon = DbConnector.getZonesDbCon();
            ps = dbcon.prepareStatement(SET_LAST_DRAINED);
            ps.setLong(1, this.lastDrained);
            ps.setLong(2, aMoneyLeft);
            ps.setInt(3, this.villageId);
            ps.executeUpdate();
        }
        catch (SQLException sqx) {
            try {
                logger.log(Level.WARNING, sqx.getMessage(), sqx);
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
    void saveDrainMod() {
        Connection dbcon = null;
        PreparedStatement ps = null;
        try {
            dbcon = DbConnector.getZonesDbCon();
            ps = dbcon.prepareStatement(SET_DRAINMOD);
            ps.setFloat(1, this.drainModifier);
            ps.setInt(2, this.villageId);
            ps.executeUpdate();
        }
        catch (SQLException sqx) {
            try {
                logger.log(Level.WARNING, sqx.getMessage(), sqx);
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
    void delete() {
        Connection dbcon = null;
        PreparedStatement ps = null;
        try {
            dbcon = DbConnector.getZonesDbCon();
            ps = dbcon.prepareStatement(DELETE_GUARDPLAN);
            ps.setInt(1, this.villageId);
            ps.executeUpdate();
        }
        catch (SQLException sqx) {
            try {
                logger.log(Level.WARNING, sqx.getMessage(), sqx);
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
        this.loadReturnedGuards();
        this.deleteReturnedGuards();
    }

    @Override
    void deleteReturnedGuards() {
        if (this.freeGuards.size() > 0) {
            ListIterator it = this.freeGuards.listIterator();
            while (it.hasNext()) {
                Creature guard = (Creature)it.next();
                this.removeReturnedGuard(guard.getWurmId());
                guard.destroy();
            }
        }
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    @Override
    void addReturnedGuard(long guardId) {
        Connection dbcon = null;
        PreparedStatement ps = null;
        try {
            dbcon = DbConnector.getZonesDbCon();
            ps = dbcon.prepareStatement(ADD_RETURNEDGUARD);
            ps.setInt(1, this.villageId);
            ps.setLong(2, guardId);
            ps.executeUpdate();
        }
        catch (SQLException sqx) {
            try {
                logger.log(Level.WARNING, sqx.getMessage(), sqx);
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
    void removeReturnedGuard(long guardId) {
        Connection dbcon = null;
        PreparedStatement ps = null;
        try {
            dbcon = DbConnector.getZonesDbCon();
            ps = dbcon.prepareStatement(DELETE_RETURNEDGUARD);
            ps.setLong(1, guardId);
            ps.executeUpdate();
        }
        catch (SQLException sqx) {
            try {
                logger.log(Level.WARNING, sqx.getMessage(), sqx);
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
    private void loadReturnedGuards() {
        Connection dbcon = null;
        PreparedStatement ps = null;
        ResultSet rs = null;
        try {
            dbcon = DbConnector.getZonesDbCon();
            ps = dbcon.prepareStatement(LOAD_RETURNEDGUARDS);
            ps.setInt(1, this.villageId);
            rs = ps.executeQuery();
            while (rs.next()) {
                long cid = rs.getLong("CREATUREID");
                try {
                    Creature guard = Creatures.getInstance().getCreature(cid);
                    this.freeGuards.add(guard);
                }
                catch (NoSuchCreatureException nsc) {
                    logger.log(Level.WARNING, "Failed to retrieve creature " + cid, nsc);
                }
            }
        }
        catch (SQLException sqx) {
            try {
                logger.log(Level.WARNING, sqx.getMessage(), sqx);
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
    public void addPayment(String creatureName, long creatureId, long money) {
        Connection dbcon = null;
        PreparedStatement ps = null;
        try {
            dbcon = DbConnector.getZonesDbCon();
            ps = dbcon.prepareStatement(ADD_PAYMENT);
            ps.setInt(1, this.villageId);
            ps.setLong(2, creatureId);
            ps.setLong(3, money);
            ps.setLong(4, WurmCalendar.currentTime);
            ps.executeUpdate();
        }
        catch (SQLException sqx) {
            try {
                logger.log(Level.WARNING, sqx.getMessage(), sqx);
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
        try {
            if (WurmId.getType(creatureId) == 0) {
                this.getVillage().addHistory(creatureName, "added " + Economy.getEconomy().getChangeFor(money).getChangeString() + " to upkeep");
            }
        }
        catch (NoSuchVillageException nsv) {
            logger.log(Level.WARNING, creatureName + " tried to add " + money + " irons to nonexistant village with id " + this.villageId, nsv);
        }
    }
}


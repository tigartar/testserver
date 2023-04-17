/*
 * Decompiled with CFR 0.152.
 */
package com.wurmonline.server.bodys;

import com.wurmonline.server.DbConnector;
import com.wurmonline.server.bodys.Wound;
import com.wurmonline.server.creatures.Creature;
import com.wurmonline.server.creatures.NoSuchCreatureException;
import com.wurmonline.server.items.Item;
import com.wurmonline.server.items.NoSpaceException;
import com.wurmonline.server.utils.DbUtilities;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.logging.Level;
import java.util.logging.Logger;

public final class DbWound
extends Wound {
    private static final String CREATE_WOUND = "INSERT INTO WOUNDS( ID, OWNER,TYPE,LOCATION,SEVERITY, POISONSEVERITY,INFECTIONSEVERITY,BANDAGED,LASTPOLLED) VALUES(?,?,?,?,?,?,?,?,?)";
    private static final String DELETE_WOUND = "DELETE FROM WOUNDS WHERE ID=?";
    private static final String SET_SEVERITY = "update WOUNDS set SEVERITY=? where ID=?";
    private static final String SET_POISONSEVERITY = "update WOUNDS set POISONSEVERITY=? where ID=?";
    private static final String SET_INFECTIONSEVERITY = "update WOUNDS set INFECTIONSEVERITY=? where ID=?";
    private static final String SET_BANDAGED = "update WOUNDS set BANDAGED=? where ID=?";
    private static final String SET_HEALEFF = "update WOUNDS set HEALEFF=? where ID=?";
    private static final Logger logger = Logger.getLogger(DbWound.class.getName());

    public DbWound(byte aType, byte aLocation, float aSeverity, long aOwner, float aPoisonSeverity, float aInfectionSeverity, boolean pvp, boolean spell) {
        super(aType, aLocation, aSeverity, aOwner, aPoisonSeverity, aInfectionSeverity, false, pvp, spell);
    }

    public DbWound(long aId, byte aType, byte aLocation, float aSeverity, long aOwner, float aPoisonSeverity, float aInfectionSeverity, long aLastPolled, boolean aBandaged, byte aHealEff) {
        super(aId, aType, aLocation, aSeverity, aOwner, aPoisonSeverity, aInfectionSeverity, aLastPolled, aBandaged, aHealEff);
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    @Override
    final void create() {
        Connection dbcon = null;
        PreparedStatement ps = null;
        try {
            dbcon = DbConnector.getPlayerDbCon();
            ps = dbcon.prepareStatement(CREATE_WOUND);
            ps.setLong(1, this.getId());
            ps.setLong(2, this.getOwner());
            ps.setByte(3, this.getType());
            ps.setByte(4, this.getLocation());
            ps.setFloat(5, this.getSeverity());
            ps.setFloat(6, this.getPoisonSeverity());
            ps.setFloat(7, this.getInfectionSeverity());
            ps.setBoolean(8, this.isBandaged());
            ps.setLong(9, this.getLastPolled());
            ps.executeUpdate();
        }
        catch (SQLException sqex) {
            try {
                logger.log(Level.WARNING, this.getId() + " " + sqex.getMessage(), sqex);
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
    final void setSeverity(float sev) {
        sev = Math.max(0.0f, sev);
        if (this.getSeverity() != sev) {
            this.severity = sev;
            Connection dbcon = null;
            PreparedStatement ps = null;
            try {
                dbcon = DbConnector.getPlayerDbCon();
                ps = dbcon.prepareStatement(SET_SEVERITY);
                ps.setFloat(1, this.getSeverity());
                ps.setLong(2, this.getId());
                ps.executeUpdate();
            }
            catch (SQLException sqx) {
                try {
                    logger.log(Level.WARNING, "Failed to save wound " + this.getId() + ":" + sqx.getMessage(), sqx);
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

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    @Override
    public final void setPoisonSeverity(float sev) {
        if (this.poisonSeverity != sev) {
            this.poisonSeverity = Math.max(0.0f, sev);
            this.poisonSeverity = Math.min(100.0f, this.poisonSeverity);
            if (this.creature != null && this.creature.isPlayer()) {
                if (sev == 0.0f) {
                    this.creature.poisonChanged(true, this);
                } else {
                    this.creature.poisonChanged(false, this);
                }
            }
            Connection dbcon = null;
            PreparedStatement ps = null;
            try {
                dbcon = DbConnector.getPlayerDbCon();
                ps = dbcon.prepareStatement(SET_POISONSEVERITY);
                ps.setFloat(1, this.getPoisonSeverity());
                ps.setLong(2, this.getId());
                ps.executeUpdate();
            }
            catch (SQLException sqx) {
                try {
                    logger.log(Level.WARNING, "Failed to save wound " + this.getId() + ":" + sqx.getMessage(), sqx);
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

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    @Override
    public final void setInfectionSeverity(float sev) {
        if (this.infectionSeverity != sev) {
            this.infectionSeverity = Math.max(0.0f, sev);
            this.infectionSeverity = Math.min(100.0f, this.infectionSeverity);
            Connection dbcon = null;
            PreparedStatement ps = null;
            try {
                dbcon = DbConnector.getPlayerDbCon();
                ps = dbcon.prepareStatement(SET_INFECTIONSEVERITY);
                ps.setFloat(1, this.infectionSeverity);
                ps.setLong(2, this.getId());
                ps.executeUpdate();
            }
            catch (SQLException sqx) {
                try {
                    logger.log(Level.WARNING, "Failed to save wound " + this.getId() + ": " + sqx.getMessage(), sqx);
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

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    @Override
    public final void setBandaged(boolean aBandaged) {
        if (this.isBandaged != aBandaged) {
            this.isBandaged = aBandaged;
            Connection dbcon = null;
            PreparedStatement ps = null;
            try {
                dbcon = DbConnector.getPlayerDbCon();
                ps = dbcon.prepareStatement(SET_BANDAGED);
                ps.setBoolean(1, this.isBandaged());
                ps.setLong(2, this.getId());
                ps.executeUpdate();
            }
            catch (SQLException sqx) {
                try {
                    logger.log(Level.WARNING, "Failed to set bandaged for wound " + this.getId() + ": " + sqx.getMessage(), sqx);
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

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    @Override
    public final void setHealeff(byte healeff) {
        block12: {
            if (this.healEff >= healeff) break block12;
            this.healEff = healeff;
            Connection dbcon = null;
            PreparedStatement ps = null;
            try {
                dbcon = DbConnector.getPlayerDbCon();
                ps = dbcon.prepareStatement(SET_HEALEFF);
                ps.setByte(1, this.getHealEff());
                ps.setLong(2, this.getId());
                ps.executeUpdate();
            }
            catch (SQLException sqx) {
                try {
                    logger.log(Level.WARNING, "Failed to save wound " + this.getId() + ": " + sqx.getMessage(), sqx);
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
                if (this.getCreature().getBody() != null) {
                    Item bodypart = this.getCreature().getBody().getBodyPartForWound(this);
                    try {
                        Creature[] watchers = bodypart.getWatchers();
                        for (int x = 0; x < watchers.length; ++x) {
                            watchers[x].getCommunicator().sendUpdateWound(this, bodypart);
                        }
                        break block12;
                    }
                    catch (NoSuchCreatureException noSuchCreatureException) {
                        break block12;
                    }
                }
                if (this.getCreature() != null) {
                    logger.log(Level.WARNING, this.getCreature().getName() + " body is null.", new Exception());
                } else {
                    logger.log(Level.WARNING, "Wound: creature==null", new Exception());
                }
            }
            catch (NoSpaceException nsp) {
                logger.log(Level.INFO, nsp.getMessage(), nsp);
            }
        }
    }

    @Override
    final void setLastPolled(long lp) {
        if (this.lastPolled != lp) {
            this.lastPolled = lp;
        }
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    @Override
    final void delete() {
        Connection dbcon = null;
        PreparedStatement ps = null;
        try {
            dbcon = DbConnector.getPlayerDbCon();
            ps = dbcon.prepareStatement(DELETE_WOUND);
            ps.setLong(1, this.getId());
            ps.executeUpdate();
        }
        catch (SQLException sqx) {
            try {
                logger.log(Level.WARNING, "Failed to delete wound " + this.getId() + ":" + sqx.getMessage(), sqx);
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
        if (this.poisonSeverity > 0.0f && this.creature != null && this.creature.isPlayer()) {
            this.creature.poisonChanged(true, this);
        }
    }
}


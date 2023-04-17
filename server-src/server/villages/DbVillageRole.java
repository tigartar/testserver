/*
 * Decompiled with CFR 0.152.
 */
package com.wurmonline.server.villages;

import com.wurmonline.server.DbConnector;
import com.wurmonline.server.utils.DbUtilities;
import com.wurmonline.server.villages.VillageRole;
import com.wurmonline.server.villages.VillageStatus;
import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.logging.Level;
import java.util.logging.Logger;

public final class DbVillageRole
extends VillageRole
implements VillageStatus,
Comparable<DbVillageRole> {
    private static final Logger logger = Logger.getLogger(DbVillageRole.class.getName());
    private static final String CREATE_ROLE = "INSERT INTO VILLAGEROLE (VILLAGEID,NAME ,MAYTERRAFORM ,MAYCUTTREE ,MAYMINE ,MAYFARM ,MAYBUILD ,MAYHIRE,MAYINVITE,MAYDESTROY,MAYMANAGEROLES, MAYEXPAND,MAYLOCKFENCES, MAYPASSFENCES,DIPLOMAT, MAYATTACKCITIZ, MAYATTACKNONCITIZ,MAYFISH,MAYCUTOLD, STATUS,VILLAGEAPPLIEDTO,MAYPUSHPULLTURN,MAYUPDATEMAP,MAYLEAD,MAYPICKUP,MAYTAME,MAYLOAD,MAYBUTCHER,MAYATTACHLOCK,MAYPICKLOCKS,PLAYERAPPLIEDTO,SETTINGS,MORESETTINGS,EXTRASETTINGS) values(?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)";
    private static final String UPDATE_ROLE = "UPDATE VILLAGEROLE SET VILLAGEID=?,NAME=?,MAYTERRAFORM=?,MAYCUTTREE=?,MAYMINE=?,MAYFARM=?,MAYBUILD=?,MAYHIRE=?,MAYINVITE=?,MAYDESTROY=?,MAYMANAGEROLES=?,MAYEXPAND=?,MAYLOCKFENCES=?,MAYPASSFENCES=?,DIPLOMAT=?,MAYATTACKCITIZ=?,MAYATTACKNONCITIZ=?,MAYFISH=?,MAYCUTOLD=?,STATUS=?,VILLAGEAPPLIEDTO=?,MAYPUSHPULLTURN=?,MAYUPDATEMAP=?,MAYLEAD=?,MAYPICKUP=?,MAYTAME=?,MAYLOAD=?,MAYBUTCHER=?,MAYATTACHLOCK=?,MAYPICKLOCKS=?,PLAYERAPPLIEDTO=?,SETTINGS=?,MORESETTINGS=?,EXTRASETTINGS=? WHERE ID=?";
    private static final String SET_NAME = "UPDATE VILLAGEROLE SET NAME=? WHERE ID=?";
    private static final String SET_MAYTERRAFORM = "UPDATE VILLAGEROLE SET MAYTERRAFORM=? WHERE ID=?";
    private static final String SET_MAYCUTTREE = "UPDATE VILLAGEROLE SET MAYCUTTREE=? WHERE ID=?";
    private static final String SET_MAYMINE = "UPDATE VILLAGEROLE SET MAYMINE=? WHERE ID=?";
    private static final String SET_MAYBUILD = "UPDATE VILLAGEROLE SET MAYBUILD=? WHERE ID=?";
    private static final String SET_MAYHIRE = "UPDATE VILLAGEROLE SET MAYHIRE=? WHERE ID=?";
    private static final String SET_MAYINVITE = "UPDATE VILLAGEROLE SET MAYINVITE=? WHERE ID=?";
    private static final String SET_MAYDESTROY = "UPDATE VILLAGEROLE SET MAYDESTROY=? WHERE ID=?";
    private static final String SET_MAYMANAGEROLES = "UPDATE VILLAGEROLE SET MAYMANAGEROLES=? WHERE ID=?";
    private static final String SET_MAYFARM = "UPDATE VILLAGEROLE SET MAYFARM=? WHERE ID=?";
    private static final String SET_MAYEXPAND = "UPDATE VILLAGEROLE SET MAYEXPAND=? WHERE ID=?";
    private static final String SET_MAYLOCKFENCES = "UPDATE VILLAGEROLE SET MAYLOCKFENCES=? WHERE ID=?";
    private static final String SET_MAYPASSFENCES = "UPDATE VILLAGEROLE SET MAYPASSFENCES=? WHERE ID=?";
    private static final String SET_MAYATTACKCITIZENS = "UPDATE VILLAGEROLE SET MAYATTACKCITIZ=? WHERE ID=?";
    private static final String SET_MAYATTACKNONCITIZENS = "UPDATE VILLAGEROLE SET MAYATTACKNONCITIZ=? WHERE ID=?";
    private static final String SET_MAYFISH = "UPDATE VILLAGEROLE SET MAYFISH=? WHERE ID=?";
    private static final String SET_MAYCUTOLD = "UPDATE VILLAGEROLE SET MAYCUTOLD=? WHERE ID=?";
    private static final String SET_DIPLOMAT = "UPDATE VILLAGEROLE SET DIPLOMAT=? WHERE ID=?";
    private static final String SET_VILLAGEAPPLIEDTO = "UPDATE VILLAGEROLE SET VILLAGEAPPLIEDTO=? WHERE ID=?";
    private static final String SET_MAYPUSHPULLTURN = "UPDATE VILLAGEROLE SET MAYPUSHPULLTURN=? WHERE ID=?";
    private static final String SET_MAYUPDATEMAP = "UPDATE VILLAGEROLE SET MAYUPDATEMAP=? WHERE ID=?";
    private static final String SET_MAYLEAD = "UPDATE VILLAGEROLE SET MAYLEAD=? WHERE ID=?";
    private static final String SET_MAYPICKUP = "UPDATE VILLAGEROLE SET MAYPICKUP=? WHERE ID=?";
    private static final String SET_MAYTAME = "UPDATE VILLAGEROLE SET MAYTAME=? WHERE ID=?";
    private static final String SET_MAYLOAD = "UPDATE VILLAGEROLE SET MAYLOAD=? WHERE ID=?";
    private static final String SET_MAYBUTCHER = "UPDATE VILLAGEROLE SET MAYBUTCHER=? WHERE ID=?";
    private static final String SET_MAYATTACHLOCK = "UPDATE VILLAGEROLE SET MAYATTACHLOCK=? WHERE ID=?";
    private static final String SET_MAYPICKLOCKS = "UPDATE VILLAGEROLE SET MAYPICKLOCKS=? WHERE ID=?";
    private static final String DELETE = "DELETE FROM VILLAGEROLE WHERE ID=?";

    public DbVillageRole(int aVillageId, String aName, boolean aTerraform, boolean aCutTrees, boolean aMine, boolean aFarm, boolean aBuild, boolean aHire, boolean aMayInvite, boolean aMayDestroy, boolean aMayManageRoles, boolean aMayExpand, boolean aMayLockFences, boolean aMayPassFences, boolean aIsDiplomat, boolean aMayAttackCitizens, boolean aMayAttackNonCitizens, boolean aMayFish, boolean aMayCutOldTrees, byte aStatus, int appliedToVillage, boolean aMayPushPullTurn, boolean aMayUpdateMap, boolean aMayLead, boolean aMayPickup, boolean aMayTame, boolean aMayLoad, boolean aMayButcher, boolean aMayAttachLock, boolean aMayPickLocks, long appliedToPlayer, int aSettings, int aMoreSettings, int aExtraSettings) throws IOException {
        super(aVillageId, aName, aTerraform, aCutTrees, aMine, aFarm, aBuild, aHire, aMayInvite, aMayDestroy, aMayManageRoles, aMayExpand, aMayLockFences, aMayPassFences, aIsDiplomat, aMayAttackCitizens, aMayAttackNonCitizens, aMayFish, aMayCutOldTrees, aStatus, appliedToVillage, aMayPushPullTurn, aMayUpdateMap, aMayLead, aMayPickup, aMayTame, aMayLoad, aMayButcher, aMayAttachLock, aMayPickLocks, appliedToPlayer, aSettings, aMoreSettings, aExtraSettings);
    }

    DbVillageRole(int aId, int aVillageId, String aRoleName, boolean aMayTerraform, boolean aMayCuttrees, boolean aMayMine, boolean aMayFarm, boolean aMayBuild, boolean aMayHire, boolean aMayInvite, boolean aMayDestroy, boolean aMayManageRoles, boolean aMayExpand, boolean aMayPassAllFences, boolean aMayLockFences, boolean aMayAttackCitizens, boolean aMayAttackNonCitizens, boolean aMayFish, boolean aMayCutOldTrees, boolean aMayPushPullTurn, boolean aDiplomat, byte aStatus, int aVillageAppliedTo, boolean aMayUpdateMap, boolean aMayLead, boolean aMayPickup, boolean aMayTame, boolean aMayLoad, boolean aMayButcher, boolean aMayAttachLock, boolean aMayPickLocks, long aPlayerAppliedTo, int aSettings, int aMoreSettings, int aExtraSettings) {
        super(aId, aVillageId, aRoleName, aMayTerraform, aMayCuttrees, aMayMine, aMayFarm, aMayBuild, aMayHire, aMayInvite, aMayDestroy, aMayManageRoles, aMayExpand, aMayPassAllFences, aMayLockFences, aMayAttackCitizens, aMayAttackNonCitizens, aMayFish, aMayCutOldTrees, aMayPushPullTurn, aDiplomat, aStatus, aVillageAppliedTo, aMayUpdateMap, aMayLead, aMayPickup, aMayTame, aMayLoad, aMayButcher, aMayAttachLock, aMayPickLocks, aPlayerAppliedTo, aSettings, aMoreSettings, aExtraSettings);
    }

    @Override
    void create() throws IOException {
        Connection dbcon = null;
        PreparedStatement ps = null;
        ResultSet rs = null;
        try {
            dbcon = DbConnector.getZonesDbCon();
            ps = dbcon.prepareStatement(CREATE_ROLE, 1);
            ps.setInt(1, this.villageid);
            ps.setString(2, this.name);
            ps.setBoolean(3, this.mayTerraform);
            ps.setBoolean(4, this.mayCuttrees);
            ps.setBoolean(5, this.mayMine);
            ps.setBoolean(6, this.mayFarm);
            ps.setBoolean(7, this.mayBuild);
            ps.setBoolean(8, this.mayHire);
            ps.setBoolean(9, this.mayInvite);
            ps.setBoolean(10, this.mayDestroy);
            ps.setBoolean(11, this.mayManageRoles);
            ps.setBoolean(12, this.mayExpand);
            ps.setBoolean(13, this.mayLockFences);
            ps.setBoolean(14, this.mayPassAllFences);
            ps.setBoolean(15, this.diplomat);
            ps.setBoolean(16, this.mayAttackCitizens);
            ps.setBoolean(17, this.mayAttackNonCitizens);
            ps.setBoolean(18, this.mayFish);
            ps.setBoolean(19, this.mayCutOldTrees);
            ps.setByte(20, this.status);
            ps.setInt(21, this.villageAppliedTo);
            ps.setBoolean(22, this.mayPushPullTurn);
            ps.setBoolean(23, this.mayUpdateMap);
            ps.setBoolean(24, this.mayLead);
            ps.setBoolean(25, this.mayPickup);
            ps.setBoolean(26, this.mayTame);
            ps.setBoolean(27, this.mayLoad);
            ps.setBoolean(28, this.mayButcher);
            ps.setBoolean(29, this.mayAttachLock);
            ps.setBoolean(30, this.mayPickLocks);
            ps.setLong(31, this.playerAppliedTo);
            ps.setInt(32, this.settings.getPermissions());
            ps.setInt(33, this.moreSettings.getPermissions());
            ps.setInt(34, this.extraSettings.getPermissions());
            ps.executeUpdate();
            rs = ps.getGeneratedKeys();
            if (rs.next()) {
                this.id = rs.getInt(1);
            }
        }
        catch (SQLException sqx) {
            try {
                logger.log(Level.WARNING, "Failed to set status for citizen " + this.name + ": " + sqx.getMessage(), sqx);
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

    @Override
    public void save() throws IOException {
        Connection dbcon = null;
        PreparedStatement ps = null;
        ResultSet rs = null;
        if (this.status == 1 && this.mayDestroy) {
            this.mayDestroy = false;
            logger.warning("Saving roleID " + this.id + ": mayDestroy set for ROLE_EVERYBODY");
            Thread.dumpStack();
        }
        try {
            dbcon = DbConnector.getZonesDbCon();
            ps = dbcon.prepareStatement(UPDATE_ROLE);
            ps.setInt(1, this.villageid);
            ps.setString(2, this.name);
            ps.setBoolean(3, this.mayTerraform);
            ps.setBoolean(4, this.mayCuttrees);
            ps.setBoolean(5, this.mayMine);
            ps.setBoolean(6, this.mayFarm);
            ps.setBoolean(7, this.mayBuild);
            ps.setBoolean(8, this.mayHire);
            ps.setBoolean(9, this.mayInvite);
            ps.setBoolean(10, this.mayDestroy);
            ps.setBoolean(11, this.mayManageRoles);
            ps.setBoolean(12, this.mayExpand);
            ps.setBoolean(13, this.mayLockFences);
            ps.setBoolean(14, this.mayPassAllFences);
            ps.setBoolean(15, this.diplomat);
            ps.setBoolean(16, this.mayAttackCitizens);
            ps.setBoolean(17, this.mayAttackNonCitizens);
            ps.setBoolean(18, this.mayFish);
            ps.setBoolean(19, this.mayCutOldTrees);
            ps.setByte(20, this.status);
            ps.setInt(21, this.villageAppliedTo);
            ps.setBoolean(22, this.mayPushPullTurn);
            ps.setBoolean(23, this.mayUpdateMap);
            ps.setBoolean(24, this.mayLead);
            ps.setBoolean(25, this.mayPickup);
            ps.setBoolean(26, this.mayTame);
            ps.setBoolean(27, this.mayLoad);
            ps.setBoolean(28, this.mayButcher);
            ps.setBoolean(29, this.mayAttachLock);
            ps.setBoolean(30, this.mayPickLocks);
            ps.setLong(31, this.playerAppliedTo);
            ps.setInt(32, this.settings.getPermissions());
            ps.setInt(33, this.moreSettings.getPermissions());
            ps.setInt(34, this.extraSettings.getPermissions());
            ps.setInt(35, this.id);
            ps.executeUpdate();
        }
        catch (SQLException sqx) {
            try {
                logger.log(Level.WARNING, "Failed to save role " + this.name + ": " + sqx.getMessage(), sqx);
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

    @Override
    public void setMayHire(boolean hire) throws IOException {
        if (this.mayHire != hire) {
            this.mayHire = hire;
            Connection dbcon = null;
            PreparedStatement ps = null;
            try {
                dbcon = DbConnector.getZonesDbCon();
                ps = dbcon.prepareStatement(SET_MAYHIRE);
                ps.setBoolean(1, this.mayHire);
                ps.setInt(2, this.id);
                ps.executeUpdate();
            }
            catch (SQLException sqx) {
                try {
                    logger.log(Level.WARNING, "Failed to set data for role with id " + this.id, sqx);
                    throw new IOException(sqx);
                }
                catch (Throwable throwable) {
                    DbUtilities.closeDatabaseObjects(ps, null);
                    DbConnector.returnConnection(dbcon);
                    throw throwable;
                }
            }
            DbUtilities.closeDatabaseObjects(ps, null);
            DbConnector.returnConnection(dbcon);
        }
    }

    @Override
    public void setName(String aName) throws IOException {
        if (!this.name.equals(aName)) {
            this.name = aName;
            Connection dbcon = null;
            PreparedStatement ps = null;
            try {
                dbcon = DbConnector.getZonesDbCon();
                ps = dbcon.prepareStatement(SET_NAME);
                ps.setString(1, aName);
                ps.setInt(2, this.id);
                ps.executeUpdate();
            }
            catch (SQLException sqx) {
                try {
                    logger.log(Level.WARNING, "Failed to set data for role with id " + this.id, sqx);
                    throw new IOException(sqx);
                }
                catch (Throwable throwable) {
                    DbUtilities.closeDatabaseObjects(ps, null);
                    DbConnector.returnConnection(dbcon);
                    throw throwable;
                }
            }
            DbUtilities.closeDatabaseObjects(ps, null);
            DbConnector.returnConnection(dbcon);
        }
    }

    @Override
    public void setMayBuild(boolean build) throws IOException {
        if (this.mayBuild != build) {
            this.mayBuild = build;
            Connection dbcon = null;
            PreparedStatement ps = null;
            try {
                dbcon = DbConnector.getZonesDbCon();
                ps = dbcon.prepareStatement(SET_MAYBUILD);
                ps.setBoolean(1, this.mayBuild);
                ps.setInt(2, this.id);
                ps.executeUpdate();
            }
            catch (SQLException sqx) {
                try {
                    logger.log(Level.WARNING, "Failed to set data for role with id " + this.id, sqx);
                    throw new IOException(sqx);
                }
                catch (Throwable throwable) {
                    DbUtilities.closeDatabaseObjects(ps, null);
                    DbConnector.returnConnection(dbcon);
                    throw throwable;
                }
            }
            DbUtilities.closeDatabaseObjects(ps, null);
            DbConnector.returnConnection(dbcon);
        }
    }

    @Override
    public void setMayCuttrees(boolean aCutTrees) throws IOException {
        if (this.mayCuttrees != aCutTrees) {
            this.mayCuttrees = aCutTrees;
            Connection dbcon = null;
            PreparedStatement ps = null;
            try {
                dbcon = DbConnector.getZonesDbCon();
                ps = dbcon.prepareStatement(SET_MAYCUTTREE);
                ps.setBoolean(1, this.mayCuttrees);
                ps.setInt(2, this.id);
                ps.executeUpdate();
            }
            catch (SQLException sqx) {
                try {
                    logger.log(Level.WARNING, "Failed to set data for role with id " + this.id, sqx);
                    throw new IOException(sqx);
                }
                catch (Throwable throwable) {
                    DbUtilities.closeDatabaseObjects(ps, null);
                    DbConnector.returnConnection(dbcon);
                    throw throwable;
                }
            }
            DbUtilities.closeDatabaseObjects(ps, null);
            DbConnector.returnConnection(dbcon);
        }
    }

    @Override
    public void setMayMine(boolean mine) throws IOException {
        if (this.mayMine != mine) {
            this.mayMine = mine;
            Connection dbcon = null;
            PreparedStatement ps = null;
            try {
                dbcon = DbConnector.getZonesDbCon();
                ps = dbcon.prepareStatement(SET_MAYMINE);
                ps.setBoolean(1, this.mayMine);
                ps.setInt(2, this.id);
                ps.executeUpdate();
            }
            catch (SQLException sqx) {
                try {
                    logger.log(Level.WARNING, "Failed to set data for role with id " + this.id, sqx);
                    throw new IOException(sqx);
                }
                catch (Throwable throwable) {
                    DbUtilities.closeDatabaseObjects(ps, null);
                    DbConnector.returnConnection(dbcon);
                    throw throwable;
                }
            }
            DbUtilities.closeDatabaseObjects(ps, null);
            DbConnector.returnConnection(dbcon);
        }
    }

    @Override
    public void setMayFarm(boolean farm) throws IOException {
        if (this.mayFarm != farm) {
            this.mayFarm = farm;
            Connection dbcon = null;
            PreparedStatement ps = null;
            try {
                dbcon = DbConnector.getZonesDbCon();
                ps = dbcon.prepareStatement(SET_MAYFARM);
                ps.setBoolean(1, this.mayFarm);
                ps.setInt(2, this.id);
                ps.executeUpdate();
            }
            catch (SQLException sqx) {
                try {
                    logger.log(Level.WARNING, "Failed to set data for role with id " + this.id, sqx);
                    throw new IOException(sqx);
                }
                catch (Throwable throwable) {
                    DbUtilities.closeDatabaseObjects(ps, null);
                    DbConnector.returnConnection(dbcon);
                    throw throwable;
                }
            }
            DbUtilities.closeDatabaseObjects(ps, null);
            DbConnector.returnConnection(dbcon);
        }
    }

    @Override
    public void setMayManageRoles(boolean mayManage) throws IOException {
        if (this.mayManageRoles != mayManage) {
            this.mayManageRoles = mayManage;
            Connection dbcon = null;
            PreparedStatement ps = null;
            try {
                dbcon = DbConnector.getZonesDbCon();
                ps = dbcon.prepareStatement(SET_MAYMANAGEROLES);
                ps.setBoolean(1, this.mayManageRoles);
                ps.setInt(2, this.id);
                ps.executeUpdate();
            }
            catch (SQLException sqx) {
                try {
                    logger.log(Level.WARNING, "Failed to set data for role with id " + this.id, sqx);
                    throw new IOException(sqx);
                }
                catch (Throwable throwable) {
                    DbUtilities.closeDatabaseObjects(ps, null);
                    DbConnector.returnConnection(dbcon);
                    throw throwable;
                }
            }
            DbUtilities.closeDatabaseObjects(ps, null);
            DbConnector.returnConnection(dbcon);
        }
    }

    @Override
    public void setMayDestroy(boolean destroy) throws IOException {
        if (this.status == 1 && destroy) {
            logger.warning("Attempting to set MayDestroy on RoleID " + this.id);
            Thread.dumpStack();
            return;
        }
        if (this.mayDestroy != destroy) {
            this.mayDestroy = destroy;
            Connection dbcon = null;
            PreparedStatement ps = null;
            try {
                dbcon = DbConnector.getZonesDbCon();
                ps = dbcon.prepareStatement(SET_MAYDESTROY);
                ps.setBoolean(1, this.mayDestroy);
                ps.setInt(2, this.id);
                ps.executeUpdate();
            }
            catch (SQLException sqx) {
                try {
                    logger.log(Level.WARNING, "Failed to set data for role with id " + this.id, sqx);
                    throw new IOException(sqx);
                }
                catch (Throwable throwable) {
                    DbUtilities.closeDatabaseObjects(ps, null);
                    DbConnector.returnConnection(dbcon);
                    throw throwable;
                }
            }
            DbUtilities.closeDatabaseObjects(ps, null);
            DbConnector.returnConnection(dbcon);
        }
    }

    @Override
    public void setMayTerraform(boolean terraform) throws IOException {
        if (this.mayTerraform != terraform) {
            this.mayTerraform = terraform;
            Connection dbcon = null;
            PreparedStatement ps = null;
            try {
                dbcon = DbConnector.getZonesDbCon();
                ps = dbcon.prepareStatement(SET_MAYTERRAFORM);
                ps.setBoolean(1, this.mayTerraform);
                ps.setInt(2, this.id);
                ps.executeUpdate();
            }
            catch (SQLException sqx) {
                try {
                    logger.log(Level.WARNING, "Failed to set data for role with id " + this.id, sqx);
                    throw new IOException(sqx);
                }
                catch (Throwable throwable) {
                    DbUtilities.closeDatabaseObjects(ps, null);
                    DbConnector.returnConnection(dbcon);
                    throw throwable;
                }
            }
            DbUtilities.closeDatabaseObjects(ps, null);
            DbConnector.returnConnection(dbcon);
        }
    }

    @Override
    public void setMayUpdateMap(boolean updateMap) throws IOException {
        if (this.mayUpdateMap != updateMap) {
            this.mayUpdateMap = updateMap;
            Connection dbcon = null;
            PreparedStatement ps = null;
            try {
                dbcon = DbConnector.getZonesDbCon();
                ps = dbcon.prepareStatement(SET_MAYUPDATEMAP);
                ps.setBoolean(1, this.mayUpdateMap);
                ps.setInt(2, this.id);
                ps.executeUpdate();
            }
            catch (SQLException sqx) {
                try {
                    logger.log(Level.WARNING, "Failed to set data for role with id " + this.id, sqx);
                    throw new IOException(sqx);
                }
                catch (Throwable throwable) {
                    DbUtilities.closeDatabaseObjects(ps, null);
                    DbConnector.returnConnection(dbcon);
                    throw throwable;
                }
            }
            DbUtilities.closeDatabaseObjects(ps, null);
            DbConnector.returnConnection(dbcon);
        }
    }

    @Override
    public void setMayInvite(boolean invite) throws IOException {
        if (this.mayInvite != invite) {
            this.mayInvite = invite;
            Connection dbcon = null;
            PreparedStatement ps = null;
            try {
                dbcon = DbConnector.getZonesDbCon();
                ps = dbcon.prepareStatement(SET_MAYINVITE);
                ps.setBoolean(1, this.mayInvite);
                ps.setInt(2, this.id);
                ps.executeUpdate();
            }
            catch (SQLException sqx) {
                try {
                    logger.log(Level.WARNING, "Failed to set data for role with id " + this.id, sqx);
                    throw new IOException(sqx);
                }
                catch (Throwable throwable) {
                    DbUtilities.closeDatabaseObjects(ps, null);
                    DbConnector.returnConnection(dbcon);
                    throw throwable;
                }
            }
            DbUtilities.closeDatabaseObjects(ps, null);
            DbConnector.returnConnection(dbcon);
        }
    }

    @Override
    public void setMayExpand(boolean expand) throws IOException {
        if (this.mayExpand != expand) {
            this.mayExpand = expand;
            Connection dbcon = null;
            PreparedStatement ps = null;
            try {
                dbcon = DbConnector.getZonesDbCon();
                ps = dbcon.prepareStatement(SET_MAYEXPAND);
                ps.setBoolean(1, this.mayExpand);
                ps.setInt(2, this.id);
                ps.executeUpdate();
            }
            catch (SQLException sqx) {
                try {
                    logger.log(Level.WARNING, "Failed to set data for role with id " + this.id, sqx);
                    throw new IOException(sqx);
                }
                catch (Throwable throwable) {
                    DbUtilities.closeDatabaseObjects(ps, null);
                    DbConnector.returnConnection(dbcon);
                    throw throwable;
                }
            }
            DbUtilities.closeDatabaseObjects(ps, null);
            DbConnector.returnConnection(dbcon);
        }
    }

    @Override
    public void setMayPassAllFences(boolean maypass) throws IOException {
        if (this.mayPassAllFences != maypass) {
            this.mayPassAllFences = maypass;
            Connection dbcon = null;
            PreparedStatement ps = null;
            try {
                dbcon = DbConnector.getZonesDbCon();
                ps = dbcon.prepareStatement(SET_MAYPASSFENCES);
                ps.setBoolean(1, this.mayPassAllFences);
                ps.setInt(2, this.id);
                ps.executeUpdate();
            }
            catch (SQLException sqx) {
                try {
                    logger.log(Level.WARNING, "Failed to set data for role with id " + this.id, sqx);
                    throw new IOException(sqx);
                }
                catch (Throwable throwable) {
                    DbUtilities.closeDatabaseObjects(ps, null);
                    DbConnector.returnConnection(dbcon);
                    throw throwable;
                }
            }
            DbUtilities.closeDatabaseObjects(ps, null);
            DbConnector.returnConnection(dbcon);
        }
    }

    @Override
    public void setMayLockFences(boolean maylock) throws IOException {
        if (this.mayLockFences != maylock) {
            this.mayLockFences = maylock;
            Connection dbcon = null;
            PreparedStatement ps = null;
            try {
                dbcon = DbConnector.getZonesDbCon();
                ps = dbcon.prepareStatement(SET_MAYLOCKFENCES);
                ps.setBoolean(1, this.mayLockFences);
                ps.setInt(2, this.id);
                ps.executeUpdate();
            }
            catch (SQLException sqx) {
                try {
                    logger.log(Level.WARNING, "Failed to set data for role with id " + this.id, sqx);
                    throw new IOException(sqx);
                }
                catch (Throwable throwable) {
                    DbUtilities.closeDatabaseObjects(ps, null);
                    DbConnector.returnConnection(dbcon);
                    throw throwable;
                }
            }
            DbUtilities.closeDatabaseObjects(ps, null);
            DbConnector.returnConnection(dbcon);
        }
    }

    @Override
    public void setVillageAppliedTo(int newVillage) throws IOException {
        if (this.villageAppliedTo != newVillage) {
            this.villageAppliedTo = newVillage;
            Connection dbcon = null;
            PreparedStatement ps = null;
            try {
                dbcon = DbConnector.getZonesDbCon();
                ps = dbcon.prepareStatement(SET_VILLAGEAPPLIEDTO);
                ps.setInt(1, this.villageAppliedTo);
                ps.setInt(2, this.id);
                ps.executeUpdate();
            }
            catch (SQLException sqx) {
                try {
                    logger.log(Level.WARNING, "Failed to set data for role with id " + this.id, sqx);
                    throw new IOException(sqx);
                }
                catch (Throwable throwable) {
                    DbUtilities.closeDatabaseObjects(ps, null);
                    DbConnector.returnConnection(dbcon);
                    throw throwable;
                }
            }
            DbUtilities.closeDatabaseObjects(ps, null);
            DbConnector.returnConnection(dbcon);
        }
    }

    @Override
    public void setDiplomat(boolean isDiplomat) throws IOException {
        if (this.diplomat != isDiplomat) {
            this.diplomat = isDiplomat;
            Connection dbcon = null;
            PreparedStatement ps = null;
            try {
                dbcon = DbConnector.getZonesDbCon();
                ps = dbcon.prepareStatement(SET_DIPLOMAT);
                ps.setBoolean(1, this.diplomat);
                ps.setInt(2, this.id);
                ps.executeUpdate();
            }
            catch (SQLException sqx) {
                try {
                    logger.log(Level.WARNING, "Failed to set data for role with id " + this.id, sqx);
                    throw new IOException(sqx);
                }
                catch (Throwable throwable) {
                    DbUtilities.closeDatabaseObjects(ps, null);
                    DbConnector.returnConnection(dbcon);
                    throw throwable;
                }
            }
            DbUtilities.closeDatabaseObjects(ps, null);
            DbConnector.returnConnection(dbcon);
        }
    }

    @Override
    public void setMayAttackCitizens(boolean attack) throws IOException {
        if (this.mayAttackCitizens != attack) {
            this.mayAttackCitizens = attack;
            Connection dbcon = null;
            PreparedStatement ps = null;
            try {
                dbcon = DbConnector.getZonesDbCon();
                ps = dbcon.prepareStatement(SET_MAYATTACKCITIZENS);
                ps.setBoolean(1, this.mayAttackCitizens);
                ps.setInt(2, this.id);
                ps.executeUpdate();
            }
            catch (SQLException sqx) {
                try {
                    logger.log(Level.WARNING, "Failed to set data for role with id " + this.id, sqx);
                    throw new IOException(sqx);
                }
                catch (Throwable throwable) {
                    DbUtilities.closeDatabaseObjects(ps, null);
                    DbConnector.returnConnection(dbcon);
                    throw throwable;
                }
            }
            DbUtilities.closeDatabaseObjects(ps, null);
            DbConnector.returnConnection(dbcon);
        }
    }

    @Override
    public void setMayAttackNonCitizens(boolean attack) throws IOException {
        if (this.mayAttackNonCitizens != attack) {
            this.mayAttackNonCitizens = attack;
            Connection dbcon = null;
            PreparedStatement ps = null;
            try {
                dbcon = DbConnector.getZonesDbCon();
                ps = dbcon.prepareStatement(SET_MAYATTACKNONCITIZENS);
                ps.setBoolean(1, this.mayAttackNonCitizens);
                ps.setInt(2, this.id);
                ps.executeUpdate();
            }
            catch (SQLException sqx) {
                try {
                    logger.log(Level.WARNING, "Failed to set data for role with id " + this.id, sqx);
                    throw new IOException(sqx);
                }
                catch (Throwable throwable) {
                    DbUtilities.closeDatabaseObjects(ps, null);
                    DbConnector.returnConnection(dbcon);
                    throw throwable;
                }
            }
            DbUtilities.closeDatabaseObjects(ps, null);
            DbConnector.returnConnection(dbcon);
        }
    }

    @Override
    public void setMayPushPullTurn(boolean pushpullturn) throws IOException {
        if (this.mayPushPullTurn == pushpullturn) {
            return;
        }
        this.mayPushPullTurn = pushpullturn;
        Connection dbcon = null;
        PreparedStatement ps = null;
        try {
            dbcon = DbConnector.getZonesDbCon();
            ps = dbcon.prepareStatement(SET_MAYPUSHPULLTURN);
            ps.setBoolean(1, pushpullturn);
            ps.setInt(2, this.id);
            ps.executeUpdate();
        }
        catch (SQLException sqx) {
            try {
                logger.log(Level.WARNING, "Failed to set data for role with id " + this.id, sqx);
                throw new IOException(sqx);
            }
            catch (Throwable throwable) {
                DbUtilities.closeDatabaseObjects(ps, null);
                DbConnector.returnConnection(dbcon);
                throw throwable;
            }
        }
        DbUtilities.closeDatabaseObjects(ps, null);
        DbConnector.returnConnection(dbcon);
    }

    @Override
    public void setMayLead(boolean lead) throws IOException {
        if (this.mayLead == lead) {
            return;
        }
        this.mayLead = lead;
        Connection dbcon = null;
        PreparedStatement ps = null;
        try {
            dbcon = DbConnector.getZonesDbCon();
            ps = dbcon.prepareStatement(SET_MAYLEAD);
            ps.setBoolean(1, lead);
            ps.setInt(2, this.id);
            ps.executeUpdate();
        }
        catch (SQLException sqx) {
            try {
                logger.log(Level.WARNING, "Failed to set data for role with id " + this.id, sqx);
                throw new IOException(sqx);
            }
            catch (Throwable throwable) {
                DbUtilities.closeDatabaseObjects(ps, null);
                DbConnector.returnConnection(dbcon);
                throw throwable;
            }
        }
        DbUtilities.closeDatabaseObjects(ps, null);
        DbConnector.returnConnection(dbcon);
    }

    @Override
    public void setMayPickup(boolean pickup) throws IOException {
        if (this.mayPickup == pickup) {
            return;
        }
        this.mayPickup = pickup;
        Connection dbcon = null;
        PreparedStatement ps = null;
        try {
            dbcon = DbConnector.getZonesDbCon();
            ps = dbcon.prepareStatement(SET_MAYPICKUP);
            ps.setBoolean(1, pickup);
            ps.setInt(2, this.id);
            ps.executeUpdate();
        }
        catch (SQLException sqx) {
            try {
                logger.log(Level.WARNING, "Failed to set data for role with id " + this.id, sqx);
                throw new IOException(sqx);
            }
            catch (Throwable throwable) {
                DbUtilities.closeDatabaseObjects(ps, null);
                DbConnector.returnConnection(dbcon);
                throw throwable;
            }
        }
        DbUtilities.closeDatabaseObjects(ps, null);
        DbConnector.returnConnection(dbcon);
    }

    @Override
    public void setMayTame(boolean tame) throws IOException {
        if (this.mayTame == tame) {
            return;
        }
        this.mayTame = tame;
        Connection dbcon = null;
        PreparedStatement ps = null;
        try {
            dbcon = DbConnector.getZonesDbCon();
            ps = dbcon.prepareStatement(SET_MAYTAME);
            ps.setBoolean(1, tame);
            ps.setInt(2, this.id);
            ps.executeUpdate();
        }
        catch (SQLException sqx) {
            try {
                logger.log(Level.WARNING, "Failed to set data for role with id " + this.id, sqx);
                throw new IOException(sqx);
            }
            catch (Throwable throwable) {
                DbUtilities.closeDatabaseObjects(ps, null);
                DbConnector.returnConnection(dbcon);
                throw throwable;
            }
        }
        DbUtilities.closeDatabaseObjects(ps, null);
        DbConnector.returnConnection(dbcon);
    }

    @Override
    public void setMayLoad(boolean load) throws IOException {
        if (this.mayLoad == load) {
            return;
        }
        this.mayLoad = load;
        Connection dbcon = null;
        PreparedStatement ps = null;
        try {
            dbcon = DbConnector.getZonesDbCon();
            ps = dbcon.prepareStatement(SET_MAYLOAD);
            ps.setBoolean(1, load);
            ps.setInt(2, this.id);
            ps.executeUpdate();
        }
        catch (SQLException sqx) {
            try {
                logger.log(Level.WARNING, "Failed to set data for role with id " + this.id, sqx);
                throw new IOException(sqx);
            }
            catch (Throwable throwable) {
                DbUtilities.closeDatabaseObjects(ps, null);
                DbConnector.returnConnection(dbcon);
                throw throwable;
            }
        }
        DbUtilities.closeDatabaseObjects(ps, null);
        DbConnector.returnConnection(dbcon);
    }

    @Override
    public void setMayButcher(boolean butcher) throws IOException {
        if (this.mayButcher == butcher) {
            return;
        }
        this.mayButcher = butcher;
        Connection dbcon = null;
        PreparedStatement ps = null;
        try {
            dbcon = DbConnector.getZonesDbCon();
            ps = dbcon.prepareStatement(SET_MAYBUTCHER);
            ps.setBoolean(1, butcher);
            ps.setInt(2, this.id);
            ps.executeUpdate();
        }
        catch (SQLException sqx) {
            try {
                logger.log(Level.WARNING, "Failed to set data for role with id " + this.id, sqx);
                throw new IOException(sqx);
            }
            catch (Throwable throwable) {
                DbUtilities.closeDatabaseObjects(ps, null);
                DbConnector.returnConnection(dbcon);
                throw throwable;
            }
        }
        DbUtilities.closeDatabaseObjects(ps, null);
        DbConnector.returnConnection(dbcon);
    }

    @Override
    public void setMayAttachLock(boolean attachLock) throws IOException {
        if (this.mayAttachLock == attachLock) {
            return;
        }
        this.mayAttachLock = attachLock;
        Connection dbcon = null;
        PreparedStatement ps = null;
        try {
            dbcon = DbConnector.getZonesDbCon();
            ps = dbcon.prepareStatement(SET_MAYATTACHLOCK);
            ps.setBoolean(1, attachLock);
            ps.setInt(2, this.id);
            ps.executeUpdate();
        }
        catch (SQLException sqx) {
            try {
                logger.log(Level.WARNING, "Failed to set data for role with id " + this.id, sqx);
                throw new IOException(sqx);
            }
            catch (Throwable throwable) {
                DbUtilities.closeDatabaseObjects(ps, null);
                DbConnector.returnConnection(dbcon);
                throw throwable;
            }
        }
        DbUtilities.closeDatabaseObjects(ps, null);
        DbConnector.returnConnection(dbcon);
    }

    @Override
    public void setMayPickLocks(boolean pickLocks) throws IOException {
        if (this.mayPickLocks == pickLocks) {
            return;
        }
        this.mayPickLocks = pickLocks;
        Connection dbcon = null;
        PreparedStatement ps = null;
        try {
            dbcon = DbConnector.getZonesDbCon();
            ps = dbcon.prepareStatement(SET_MAYPICKLOCKS);
            ps.setBoolean(1, pickLocks);
            ps.setInt(2, this.id);
            ps.executeUpdate();
        }
        catch (SQLException sqx) {
            try {
                logger.log(Level.WARNING, "Failed to set data for role with id " + this.id, sqx);
                throw new IOException(sqx);
            }
            catch (Throwable throwable) {
                DbUtilities.closeDatabaseObjects(ps, null);
                DbConnector.returnConnection(dbcon);
                throw throwable;
            }
        }
        DbUtilities.closeDatabaseObjects(ps, null);
        DbConnector.returnConnection(dbcon);
    }

    @Override
    public void setMayFish(boolean fish) throws IOException {
        if (this.mayFish != fish) {
            this.mayFish = fish;
            Connection dbcon = null;
            PreparedStatement ps = null;
            try {
                dbcon = DbConnector.getZonesDbCon();
                ps = dbcon.prepareStatement(SET_MAYFISH);
                ps.setBoolean(1, this.mayFish);
                ps.setInt(2, this.id);
                ps.executeUpdate();
            }
            catch (SQLException sqx) {
                try {
                    logger.log(Level.WARNING, "Failed to set data for role with id " + this.id, sqx);
                    throw new IOException(sqx);
                }
                catch (Throwable throwable) {
                    DbUtilities.closeDatabaseObjects(ps, null);
                    DbConnector.returnConnection(dbcon);
                    throw throwable;
                }
            }
            DbUtilities.closeDatabaseObjects(ps, null);
            DbConnector.returnConnection(dbcon);
        }
    }

    @Override
    public void setCutOld(boolean cutold) throws IOException {
        if (this.mayCutOldTrees != cutold) {
            this.mayCutOldTrees = cutold;
            Connection dbcon = null;
            PreparedStatement ps = null;
            try {
                dbcon = DbConnector.getZonesDbCon();
                ps = dbcon.prepareStatement(SET_MAYCUTOLD);
                ps.setBoolean(1, this.mayCutOldTrees);
                ps.setInt(2, this.id);
                ps.executeUpdate();
            }
            catch (SQLException sqx) {
                try {
                    logger.log(Level.WARNING, "Failed to set data for role with id " + this.id, sqx);
                    throw new IOException(sqx);
                }
                catch (Throwable throwable) {
                    DbUtilities.closeDatabaseObjects(ps, null);
                    DbConnector.returnConnection(dbcon);
                    throw throwable;
                }
            }
            DbUtilities.closeDatabaseObjects(ps, null);
            DbConnector.returnConnection(dbcon);
        }
    }

    @Override
    public void delete() throws IOException {
        Connection dbcon = null;
        PreparedStatement ps = null;
        try {
            dbcon = DbConnector.getZonesDbCon();
            ps = dbcon.prepareStatement(DELETE);
            ps.setInt(1, this.id);
            ps.executeUpdate();
        }
        catch (SQLException sqx) {
            try {
                logger.log(Level.WARNING, "Failed to delete role with id " + this.id, sqx);
                throw new IOException(sqx);
            }
            catch (Throwable throwable) {
                DbUtilities.closeDatabaseObjects(ps, null);
                DbConnector.returnConnection(dbcon);
                throw throwable;
            }
        }
        DbUtilities.closeDatabaseObjects(ps, null);
        DbConnector.returnConnection(dbcon);
    }

    @Override
    public int compareTo(DbVillageRole otherDbVillageRole) {
        return this.getName().compareTo(otherDbVillageRole.getName());
    }
}


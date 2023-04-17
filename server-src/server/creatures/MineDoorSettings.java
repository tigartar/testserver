/*
 * Decompiled with CFR 0.152.
 */
package com.wurmonline.server.creatures;

import com.wurmonline.server.DbConnector;
import com.wurmonline.server.MiscConstants;
import com.wurmonline.server.Servers;
import com.wurmonline.server.creatures.Creature;
import com.wurmonline.server.players.Permissions;
import com.wurmonline.server.players.PermissionsByPlayer;
import com.wurmonline.server.players.PermissionsPlayerList;
import com.wurmonline.server.utils.DbUtilities;
import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Level;
import java.util.logging.Logger;

public class MineDoorSettings
implements MiscConstants {
    private static final Logger logger = Logger.getLogger(MineDoorSettings.class.getName());
    private static final String GET_ALL_SETTINGS = "SELECT * FROM MDPERMS";
    private static final String ADD_PLAYER = "INSERT INTO MDPERMS (SETTINGS,ID,PERMITTED) VALUES(?,?,?)";
    private static final String DELETE_SETTINGS = "DELETE FROM MDPERMS WHERE ID=?";
    private static final String REMOVE_PLAYER = "DELETE FROM MDPERMS WHERE ID=? AND PERMITTED=?";
    private static final String UPDATE_PLAYER = "UPDATE MDPERMS SET SETTINGS=? WHERE ID=? AND PERMITTED=?";
    private static int MAX_PLAYERS_PER_OBJECT = 1000;
    private static Map<Long, PermissionsPlayerList> objectSettings = new ConcurrentHashMap<Long, PermissionsPlayerList>();

    private MineDoorSettings() {
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public static void loadAll() throws IOException {
        logger.log(Level.INFO, "Loading all minedoor settings.");
        long start = System.nanoTime();
        long count = 0L;
        Connection dbcon = null;
        PreparedStatement ps = null;
        ResultSet rs = null;
        try {
            dbcon = DbConnector.getZonesDbCon();
            ps = dbcon.prepareStatement(GET_ALL_SETTINGS);
            rs = ps.executeQuery();
            while (rs.next()) {
                int minedoorId = rs.getInt("ID");
                long playerId = rs.getLong("PERMITTED");
                int settings = rs.getInt("SETTINGS");
                if (settings == 0) {
                    settings = MinedoorPermissions.PASS.getValue();
                }
                MineDoorSettings.add(minedoorId, playerId, settings);
                ++count;
            }
        }
        catch (SQLException ex) {
            try {
                logger.log(Level.WARNING, "Failed to load settings for minedoors.", ex);
            }
            catch (Throwable throwable) {
                DbUtilities.closeDatabaseObjects(ps, rs);
                DbConnector.returnConnection(dbcon);
                long end = System.nanoTime();
                logger.log(Level.INFO, "Loaded " + count + " minedoor settings. That took " + (float)(end - start) / 1000000.0f + " ms.");
                throw throwable;
            }
            DbUtilities.closeDatabaseObjects(ps, rs);
            DbConnector.returnConnection(dbcon);
            long end = System.nanoTime();
            logger.log(Level.INFO, "Loaded " + count + " minedoor settings. That took " + (float)(end - start) / 1000000.0f + " ms.");
        }
        DbUtilities.closeDatabaseObjects(ps, rs);
        DbConnector.returnConnection(dbcon);
        long end = System.nanoTime();
        logger.log(Level.INFO, "Loaded " + count + " minedoor settings. That took " + (float)(end - start) / 1000000.0f + " ms.");
    }

    public static int getMaxAllowed() {
        return Servers.isThisATestServer() ? 10 : MAX_PLAYERS_PER_OBJECT;
    }

    private static PermissionsByPlayer add(int minedoorId, long playerId, int settings) {
        Long id = minedoorId;
        if (objectSettings.containsKey(id)) {
            PermissionsPlayerList ppl = objectSettings.get(id);
            return ppl.add(playerId, settings);
        }
        PermissionsPlayerList ppl = new PermissionsPlayerList();
        objectSettings.put(id, ppl);
        return ppl.add(playerId, settings);
    }

    public static void addPlayer(int minedoorId, long playerId, int settings) {
        PermissionsByPlayer pbp = MineDoorSettings.add(minedoorId, playerId, settings);
        if (pbp == null) {
            MineDoorSettings.dbAddPlayer(minedoorId, playerId, settings, true);
        } else if (pbp.getSettings() != settings) {
            MineDoorSettings.dbAddPlayer(minedoorId, playerId, settings, false);
        }
    }

    public static void removePlayer(int minedoorId, long playerId) {
        Long id = minedoorId;
        if (objectSettings.containsKey(id)) {
            PermissionsPlayerList ppl = objectSettings.get(id);
            ppl.remove(playerId);
            MineDoorSettings.dbRemovePlayer(minedoorId, playerId);
            if (ppl.isEmpty()) {
                objectSettings.remove(id);
            }
        } else {
            logger.log(Level.WARNING, "Failed to remove player " + playerId + " from settings for minedoor " + minedoorId + ".");
        }
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    private static void dbAddPlayer(int minedoorId, long playerId, int settings, boolean add) {
        Connection dbcon = null;
        PreparedStatement ps = null;
        try {
            dbcon = DbConnector.getZonesDbCon();
            ps = add ? dbcon.prepareStatement(ADD_PLAYER) : dbcon.prepareStatement(UPDATE_PLAYER);
            ps.setInt(1, settings);
            ps.setInt(2, minedoorId);
            ps.setLong(3, playerId);
            ps.executeUpdate();
        }
        catch (SQLException ex) {
            try {
                logger.log(Level.WARNING, "Failed to " + (add ? "add" : "update") + " player (" + playerId + ") for minedoor with id " + minedoorId, ex);
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
    private static void dbRemovePlayer(int minedoorId, long playerId) {
        Connection dbcon = null;
        PreparedStatement ps = null;
        ResultSet rs = null;
        try {
            dbcon = DbConnector.getZonesDbCon();
            ps = dbcon.prepareStatement(REMOVE_PLAYER);
            ps.setInt(1, minedoorId);
            ps.setLong(2, playerId);
            ps.executeUpdate();
        }
        catch (SQLException ex) {
            try {
                logger.log(Level.WARNING, "Failed to remove player " + playerId + " from settings for minedoor " + minedoorId + ".", ex);
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

    public static boolean exists(long wurmId) {
        Long id = wurmId;
        return objectSettings.containsKey(id);
    }

    public static void remove(int minedoorId) {
        Long id = minedoorId;
        if (objectSettings.containsKey(id)) {
            MineDoorSettings.dbRemove(minedoorId);
            objectSettings.remove(id);
        }
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    private static void dbRemove(int minedoorId) {
        Connection dbcon = null;
        PreparedStatement ps = null;
        ResultSet rs = null;
        try {
            dbcon = DbConnector.getZonesDbCon();
            ps = dbcon.prepareStatement(DELETE_SETTINGS);
            ps.setInt(1, minedoorId);
            ps.executeUpdate();
        }
        catch (SQLException ex) {
            try {
                logger.log(Level.WARNING, "Failed to delete settings for minedoor " + minedoorId + ".", ex);
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

    public static PermissionsPlayerList getPermissionsPlayerList(long wurmId) {
        Long id = wurmId;
        PermissionsPlayerList ppl = objectSettings.get(id);
        if (ppl == null) {
            return new PermissionsPlayerList();
        }
        return ppl;
    }

    private static boolean hasPermission(PermissionsPlayerList.ISettings is, long objectId, Creature creature, int bit) {
        if (is.isOwner(creature)) {
            return bit != MinedoorPermissions.EXCLUDE.getBit();
        }
        Long id = objectId;
        PermissionsPlayerList ppl = objectSettings.get(id);
        if (ppl == null) {
            return false;
        }
        if (ppl.exists(creature.getWurmId())) {
            return ppl.getPermissionsFor(creature.getWurmId()).hasPermission(bit);
        }
        if (is.isCitizen(creature) && ppl.exists(-30L)) {
            return ppl.getPermissionsFor(-30L).hasPermission(bit);
        }
        if (is.isAllied(creature) && ppl.exists(-20L)) {
            return ppl.getPermissionsFor(-20L).hasPermission(bit);
        }
        if (is.isSameKingdom(creature) && ppl.exists(-40L)) {
            return ppl.getPermissionsFor(-40L).hasPermission(bit);
        }
        return ppl.exists(-50L) && ppl.getPermissionsFor(-50L).hasPermission(bit);
    }

    public static boolean isGuest(PermissionsPlayerList.ISettings is, Creature creature) {
        return MineDoorSettings.isGuest(is, creature.getWurmId());
    }

    public static boolean isGuest(PermissionsPlayerList.ISettings is, long playerId) {
        if (is.isOwner(playerId)) {
            return true;
        }
        Long id = is.getWurmId();
        PermissionsPlayerList ppl = objectSettings.get(id);
        if (ppl == null) {
            return false;
        }
        return ppl.exists(playerId);
    }

    public static boolean canManage(PermissionsPlayerList.ISettings is, long objectId, Creature creature) {
        return MineDoorSettings.hasPermission(is, objectId, creature, MinedoorPermissions.MANAGE.getBit());
    }

    public static boolean mayPass(PermissionsPlayerList.ISettings is, long objectId, Creature creature) {
        if (creature.getPower() > 1) {
            return true;
        }
        return MineDoorSettings.hasPermission(is, objectId, creature, MinedoorPermissions.PASS.getBit());
    }

    public static boolean isExcluded(PermissionsPlayerList.ISettings is, long objectId, Creature creature) {
        if (creature.getPower() > 1) {
            return false;
        }
        return MineDoorSettings.hasPermission(is, objectId, creature, MinedoorPermissions.EXCLUDE.getBit());
    }

    public static enum MinedoorPermissions implements Permissions.IPermission
    {
        MANAGE(0, "Manage Item", "Manage", "Item", "Allows managing of these permissions."),
        PASS(1, "Pass Door", "Pass", "Mine Door", "Allows entry through this mine door."),
        EXCLUDE(15, "Deny All", "Deny", "All", "Deny all access.");

        final byte bit;
        final String description;
        final String header1;
        final String header2;
        final String hover;
        private static final Permissions.Allow[] types;

        private MinedoorPermissions(int aBit, String aDescription, String aHeader1, String aHeader2, String aHover) {
            this.bit = (byte)aBit;
            this.description = aDescription;
            this.header1 = aHeader1;
            this.header2 = aHeader2;
            this.hover = aHover;
        }

        @Override
        public byte getBit() {
            return this.bit;
        }

        @Override
        public int getValue() {
            return 1 << this.bit;
        }

        @Override
        public String getDescription() {
            return this.description;
        }

        @Override
        public String getHeader1() {
            return this.header1;
        }

        @Override
        public String getHeader2() {
            return this.header2;
        }

        @Override
        public String getHover() {
            return this.hover;
        }

        public static Permissions.IPermission[] getPermissions() {
            return types;
        }

        static {
            types = Permissions.Allow.values();
        }
    }
}


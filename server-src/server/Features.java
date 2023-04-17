/*
 * Decompiled with CFR 0.152.
 */
package com.wurmonline.server;

import com.wurmonline.server.DbConnector;
import com.wurmonline.server.LoginServerWebConnection;
import com.wurmonline.server.Server;
import com.wurmonline.server.ServerEntry;
import com.wurmonline.server.Servers;
import com.wurmonline.server.utils.DbUtilities;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.logging.Level;
import java.util.logging.Logger;

public final class Features {
    private static final Logger logger = Logger.getLogger(Features.class.getName());
    private static int currentProdVersion = 129;

    private Features() {
    }

    public static void loadAllFeatures() {
        Feature.dbReadOverriddenFeatures();
        Features.logFeatureDetails();
    }

    public static void logFeatureDetails() {
        for (Feature lFeature : Feature.values()) {
            logger.info(lFeature.toString());
        }
    }

    public static int getVerionsNo() {
        return currentProdVersion;
    }

    public static enum Feature {
        NONE(0, "Dummy entry - do not use", 1, State.COMPLETE, Wurm.NONE),
        NAMECHANGE(31, "Name change", 999, State.COMPLETE, Wurm.BOTH),
        SURFACEWATER(32, "Surface Water", 140, State.INDEV, Wurm.BOTH),
        CAVEWATER(33, "Cave Water", 999, State.FUTURE, Wurm.BOTH),
        NEW_SKILL_SYSTEM(38, "New skill system", 125, State.COMPLETE, Wurm.BOTH),
        BLOCKED_TRADERS(41, "Blocked Traders", 121, State.COMPLETE, Wurm.BOTH),
        CREATURE_COMBAT_CHANGES(51, "Combat system changes for creatures", 201, State.INDEV, Wurm.BOTH),
        BLOCK_HOTA(55, "Blocked HOTA terraforming and building", 201, State.COMPLETE, Wurm.BOTH),
        FREE_ITEMS(56, "Free armour and weapons on spawn", 201, State.COMPLETE, Wurm.BOTH),
        TREASURE_CHESTS(57, "Random treasure chests", 201, State.COMPLETE, Wurm.BOTH),
        OWNERSHIP_PAPERS(70, "Ownership Papers", 999, State.FUTURE, Wurm.BOTH),
        VALREI_MAP(72, "Valrei map", 125, State.COMPLETE, Wurm.BOTH),
        CROP_POLLER(75, "Crop tile poller split", 125, State.COMPLETE, Wurm.BOTH),
        SINGLE_PLAYER_BRIDGES(76, "Single Player Bridges", 125, State.COMPLETE, Wurm.STEAM),
        AMPHORA(77, "Amphora", 126, State.COMPLETE, Wurm.BOTH),
        CHAOS(78, "Set as a chaos server (test pvp only)", 999, State.COMPLETE, Wurm.TEST),
        BOAT_DESTINATION(79, "Set a destination on a boat", 126, State.COMPLETE, Wurm.BOTH),
        NEW_PORTALS(80, "New portals", 999, State.COMPLETE, Wurm.BOTH),
        TRANSFORM_RESOURCE_TILES(81, "Transform from resource tiles", 126, State.COMPLETE, Wurm.BOTH),
        WAGON_PASSENGER(82, "Wagon Passenger", 126, State.COMPLETE, Wurm.BOTH),
        CAVE_DWELLINGS(85, "Cave Dwellings", 128, State.COMPLETE, Wurm.BOTH),
        ITEMS_ON_FURNITURE(86, "Placing items on furniture", 999, State.FUTURE, Wurm.BOTH),
        RIFTS(87, "Rifts", 128, State.COMPLETE, Wurm.WO),
        TRANSFORM_TO_RESOURCE_TILES(88, "Transform to resource tiles", 126, State.COMPLETE, Wurm.BOTH),
        CAVE_BRIDGES(89, "Cave Bridges", 129, State.COMPLETE, Wurm.BOTH),
        GIFT_PACKS(90, "Gift packs", 128, State.COMPLETE, Wurm.WO),
        RETURNER_PACK_REGISTRATION(91, "Returner pack registration", 127, State.COMPLETE, Wurm.WO),
        RIFTLOOTCHANCE(92, "Rift Loot Based on Participation", 140, State.COMPLETE, Wurm.WO),
        EXTRAGIFT(93, "Extra Anniversary Gift", 128, State.COMPLETE, Wurm.WO),
        NEWDOMAINS(94, "New Domain System - Override requires restart", 128, State.COMPLETE, Wurm.BOTH),
        ALLOW_MEDPATHCHANGE(95, "Allow Meditation Path Change (Insanity Only)", 130, State.COMPLETE, Wurm.WO),
        HIGHWAYS(96, "New Highway System - Override requires restart", 129, State.COMPLETE, Wurm.BOTH),
        NEW_PROJECTILES(97, "New Projectile Calculations", 128, State.COMPLETE, Wurm.BOTH),
        NEW_KINGDOM_INF(98, "New Kingdom Influence", 140, State.COMPLETE, Wurm.BOTH),
        WAGONER(99, "Wagoner System", 129, State.COMPLETE, Wurm.WO),
        CREATURE_MOVEMENT_CHANGES(100, "Creature Movement Changes", 129, State.COMPLETE, Wurm.BOTH),
        POLLING_CHANGES(101, "Polling Optimisation - Tile Array Copying Changes", 999, State.COMPLETE, Wurm.WO),
        DRIVE_ON_LEFT(102, "Wagoner Drive On Left", 129, State.COMPLETE, Wurm.WO),
        TRANSPORTABLE_CREATURES(103, "Allows for transportation of creatures", 129, State.COMPLETE, Wurm.BOTH),
        MOVE_BULK_TO_BULK(104, "Move from one bulk container to another as action", 129, State.COMPLETE, Wurm.BOTH),
        DRIVE_SIDES(105, "Wagoner Drive on One Side", 129, State.COMPLETE, Wurm.WO),
        AFFINITY_GAINS(106, "Chance to gain affinities from skill usage", 140, State.COMPLETE, Wurm.BOTH),
        METALLIC_ITEMS(107, "All metals make all metal items", 129, State.COMPLETE, Wurm.BOTH),
        COMPOUND_TITLES(108, "Compound Titles", 129, State.COMPLETE, Wurm.BOTH),
        PVE_DEATHTABS(109, "PvE Server Death Tabs", 129, State.COMPLETE, Wurm.BOTH),
        NEW_ARMOUR_VALUES(110, "New Armour Values (Epic Tested)", 129, State.COMPLETE, Wurm.BOTH),
        TOWER_CHAINING(111, "Tower Chaining", 140, State.COMPLETE, Wurm.BOTH),
        CHICKEN_COOPS(112, "Chicken Coops", 129, State.COMPLETE, Wurm.BOTH),
        SADDLEBAG_DECAY(113, "Decay in Saddlebags", 129, State.COMPLETE, Wurm.BOTH),
        SKILLSTAT_DISABLE(114, "Disable SkillStat saving", 129, State.COMPLETE, Wurm.BOTH);

        private static final String GET_ALL_OVERRIDDEN_FEATURES = "SELECT * FROM OVERRIDDENFEATURES";
        private static final String INSERT_OVERRIDDEN_FEATURE = "INSERT INTO OVERRIDDENFEATURES(FEATUREID,ENABLED) VALUES(?,?)";
        private static final String DELETE_OVERRIDDEN_FEATURE = "DELETE FROM OVERRIDDENFEATURES WHERE FEATUREID=?";
        private static final String UPDATE_OVERRIDDEN_FEATURE = "UPDATE OVERRIDDENFEATURES SET ENABLED=? WHERE FEATUREID=?";
        private final int featureId;
        private final String name;
        private final int version;
        private final boolean theDefault;
        private boolean overridden;
        private boolean enabled;
        private State state;
        private Wurm wurm;
        private static final Feature[] types;

        private Feature(int aFeatureId, String aName, int aVersion, State aState, Wurm aWurm) {
            this.featureId = aFeatureId;
            this.name = aName;
            this.version = aVersion;
            this.state = aState;
            this.wurm = aWurm;
            this.theDefault = this.workOutDefault();
            boolean bl = this.enabled = this.wurm != Wurm.NONE && this.theDefault;
            if (Servers.localServer.isChallengeServer() && (this.featureId == 55 || this.featureId == 56 || this.featureId == 57)) {
                this.enabled = true;
            }
            if (Servers.localServer.isChallengeOrEpicServer() && !Server.getInstance().isPS() && this.featureId == 79) {
                this.enabled = false;
            }
            if (Servers.localServer.id == 15 && this.featureId == 41) {
                this.enabled = false;
            }
            if (Servers.localServer.id == 3 && (this.featureId == 81 || this.featureId == 88)) {
                this.enabled = false;
            }
            if (Servers.localServer.PVPSERVER && (this.featureId == 96 || this.featureId == 99)) {
                this.enabled = false;
            }
            this.overridden = false;
        }

        private boolean workOutDefault() {
            if (Servers.isThisATestServer()) {
                return true;
            }
            if (this.state != State.COMPLETE) {
                return false;
            }
            if (this.version > currentProdVersion) {
                return false;
            }
            if (this.wurm == Wurm.BOTH) {
                return true;
            }
            if (this.wurm == Wurm.STEAM && Server.getInstance().isPS()) {
                return true;
            }
            return this.wurm == Wurm.WO && !Server.getInstance().isPS();
        }

        public int getVersion() {
            return this.version;
        }

        public boolean getDefault() {
            return this.theDefault;
        }

        public int getFeatureId() {
            return this.featureId;
        }

        public String getName() {
            return this.name;
        }

        public boolean isEnabled() {
            return this.enabled;
        }

        public boolean isOverridden() {
            return this.overridden;
        }

        public State getState() {
            return this.state;
        }

        public Wurm getWurm() {
            return this.wurm;
        }

        public boolean isShown() {
            if (this.getFeatureId() == 0) {
                return false;
            }
            if (this.isEnabled()) {
                return true;
            }
            if (this.wurm == Wurm.NONE) {
                return false;
            }
            if (Servers.isThisATestServer()) {
                return true;
            }
            if (this.state != State.COMPLETE) {
                return false;
            }
            if (this.wurm == Wurm.BOTH) {
                return true;
            }
            return !(Server.getInstance().isPS() ? this.wurm != Wurm.STEAM : this.wurm != Wurm.WO);
        }

        public boolean isAvailable() {
            return this.state == State.COMPLETE || Servers.isThisATestServer();
        }

        /*
         * WARNING - Removed try catching itself - possible behaviour change.
         */
        private void dbDeleteOverridden() {
            Connection dbcon = null;
            PreparedStatement ps = null;
            try {
                logger.log(Level.INFO, "Removing override for feature: " + this.featureId);
                dbcon = DbConnector.getLoginDbCon();
                ps = dbcon.prepareStatement(DELETE_OVERRIDDEN_FEATURE);
                ps.setInt(1, this.featureId);
                ps.executeUpdate();
            }
            catch (SQLException sqex) {
                try {
                    logger.log(Level.WARNING, "Failed to delete overridden feature " + this.featureId + " from logindb!" + sqex.getMessage(), sqex);
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
        public void dbAddOverridden(boolean aEnabled) {
            Connection dbcon = null;
            PreparedStatement ps = null;
            try {
                dbcon = DbConnector.getLoginDbCon();
                logger.log(Level.INFO, "Adding new override for feature: " + this.featureId);
                ps = dbcon.prepareStatement(INSERT_OVERRIDDEN_FEATURE);
                ps.setInt(1, this.featureId);
                ps.setBoolean(2, aEnabled);
                ps.executeUpdate();
            }
            catch (SQLException sqex) {
                try {
                    logger.log(Level.WARNING, "Failed to insert overridden feature " + this.featureId + " in logindb!" + sqex.getMessage(), sqex);
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
        public void dbUpdateOverridden(boolean aEnabled) {
            Connection dbcon = null;
            PreparedStatement ps = null;
            try {
                dbcon = DbConnector.getLoginDbCon();
                logger.log(Level.INFO, "Updating override for feature: " + this.featureId);
                ps = dbcon.prepareStatement(UPDATE_OVERRIDDEN_FEATURE);
                ps.setBoolean(1, aEnabled);
                ps.setInt(2, this.featureId);
                ps.executeUpdate();
            }
            catch (SQLException sqex) {
                try {
                    logger.log(Level.WARNING, "Failed to insert overridden feature " + this.featureId + " in logindb!" + sqex.getMessage(), sqex);
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
        private static void dbReadOverriddenFeatures() {
            Connection dbcon = null;
            PreparedStatement ps = null;
            ResultSet rs = null;
            int count = 0;
            try {
                logger.log(Level.INFO, "Loading all overridden features for production version: " + currentProdVersion + " and isTestServer: " + Servers.isThisATestServer() + '.');
                dbcon = DbConnector.getLoginDbCon();
                ps = dbcon.prepareStatement(GET_ALL_OVERRIDDEN_FEATURES);
                rs = ps.executeQuery();
                while (rs.next()) {
                    ++count;
                    int featureid = rs.getInt("FEATUREID");
                    boolean enabled = rs.getBoolean("ENABLED");
                    Feature.setOverridden(featureid, true, enabled);
                    if (!logger.isLoggable(Level.FINE)) continue;
                    logger.fine("Loaded overridden feature " + featureid);
                }
            }
            catch (SQLException sqex) {
                try {
                    logger.log(Level.WARNING, "Failed to load all overridden features!" + sqex.getMessage(), sqex);
                }
                catch (Throwable throwable) {
                    DbUtilities.closeDatabaseObjects(ps, rs);
                    DbConnector.returnConnection(dbcon);
                    logger.info("Loaded " + count + " overridden features from the database");
                    throw throwable;
                }
                DbUtilities.closeDatabaseObjects(ps, rs);
                DbConnector.returnConnection(dbcon);
                logger.info("Loaded " + count + " overridden features from the database");
            }
            DbUtilities.closeDatabaseObjects(ps, rs);
            DbConnector.returnConnection(dbcon);
            logger.info("Loaded " + count + " overridden features from the database");
        }

        private static void setOverridden(int featureId, boolean aOverridden, boolean aEnabled) {
            Feature feature = Feature.featureFromInt(featureId);
            feature.overridden = aOverridden;
            feature.enabled = aEnabled;
        }

        public static void setOverridden(int aServerId, int featureId, boolean aOverridden, boolean aEnabled, boolean global) {
            if (global) {
                if (Servers.isThisLoginServer()) {
                    for (ServerEntry server : Servers.getAllServers()) {
                        if (server.id == Servers.loginServer.id || server.id == aServerId) continue;
                        LoginServerWebConnection lsw = new LoginServerWebConnection(server.id);
                        lsw.manageFeature(aServerId, featureId, aOverridden, aEnabled, false);
                    }
                } else {
                    LoginServerWebConnection lsw = new LoginServerWebConnection(Servers.loginServer.id);
                    lsw.manageFeature(aServerId, featureId, aOverridden, aEnabled, true);
                }
            }
            Feature feature = Feature.featureFromInt(featureId);
            if (feature.overridden && !aOverridden) {
                feature.dbDeleteOverridden();
                feature.overridden = aOverridden;
                feature.enabled = feature.theDefault;
            } else if (!feature.overridden && aOverridden) {
                feature.dbAddOverridden(aEnabled);
                feature.overridden = aOverridden;
                feature.enabled = aEnabled;
            } else if (feature.overridden && feature.enabled != aEnabled) {
                feature.dbUpdateOverridden(aEnabled);
                feature.enabled = aEnabled;
            } else if (!global) {
                return;
            }
        }

        public static boolean isFeatureEnabled(int aServerId, int featureId) {
            if (aServerId == Servers.localServer.getId()) {
                return Feature.isFeatureEnabled(featureId);
            }
            for (ServerEntry server : Servers.getAllServers()) {
                if (server.id != aServerId) continue;
                LoginServerWebConnection lsw = new LoginServerWebConnection(server.id);
                return lsw.isFeatureEnabled(featureId);
            }
            return false;
        }

        public static boolean isFeatureEnabled(int aFeatureId) {
            Feature f = Feature.featureFromInt(aFeatureId);
            if (f == NONE) {
                return false;
            }
            return f.isEnabled();
        }

        private static Feature featureFromInt(int featureAsInt) {
            for (int i = 0; i < types.length; ++i) {
                if (featureAsInt != types[i].getFeatureId()) continue;
                return types[i];
            }
            return NONE;
        }

        public String toString() {
            StringBuilder lBuilder = new StringBuilder();
            lBuilder.append("Feature [");
            lBuilder.append("Name: ").append(this.name);
            lBuilder.append(", Id: ").append(this.featureId);
            lBuilder.append(", Version: ").append(this.version);
            lBuilder.append(", Default: ").append(this.theDefault);
            lBuilder.append(", Overridden: ").append(this.isOverridden());
            lBuilder.append(", Enabled: ").append(this.isEnabled());
            lBuilder.append(']');
            return lBuilder.toString();
        }

        static {
            types = Feature.values();
        }
    }

    public static enum Wurm {
        NONE,
        WO,
        STEAM,
        BOTH,
        TEST;

    }

    public static enum State {
        FUTURE,
        INDEV,
        COMPLETE;

    }
}


/*
 * Decompiled with CFR 0.152.
 */
package com.wurmonline.server;

import com.wurmonline.server.DbConnector;
import com.wurmonline.server.LoginServerWebConnection;
import com.wurmonline.server.MiscConstants;
import com.wurmonline.server.Server;
import com.wurmonline.server.ServerEntry;
import com.wurmonline.server.ServerProperties;
import com.wurmonline.server.WurmCalendar;
import com.wurmonline.server.WurmId;
import com.wurmonline.server.behaviours.Vehicle;
import com.wurmonline.server.behaviours.Vehicles;
import com.wurmonline.server.creatures.Creature;
import com.wurmonline.server.deities.Deities;
import com.wurmonline.server.utils.DbUtilities;
import com.wurmonline.server.utils.SimpleArgumentParser;
import com.wurmonline.server.webinterface.WebCommand;
import com.wurmonline.server.zones.TilePoller;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Level;
import java.util.logging.Logger;

public final class Servers
implements MiscConstants {
    public static SimpleArgumentParser arguments = null;
    private static Map<String, ServerEntry> neighbours = new ConcurrentHashMap<String, ServerEntry>();
    private static Map<Integer, ServerEntry> allServers = new ConcurrentHashMap<Integer, ServerEntry>();
    public static ServerEntry localServer;
    public static ServerEntry loginServer;
    private static final Logger logger;
    private static final String GET_ALL_SERVERS = "SELECT * FROM SERVERS";
    private static final String INSERT_SERVER = "INSERT INTO SERVERS(SERVER,NAME,HOMESERVER,SPAWNPOINTJENNX,SPAWNPOINTJENNY,SPAWNPOINTLIBX,SPAWNPOINTLIBY,SPAWNPOINTMOLX,SPAWNPOINTMOLY,INTRASERVERADDRESS,INTRASERVERPORT,INTRASERVERPASSWORD,EXTERNALIP, EXTERNALPORT,LOGINSERVER, KINGDOM,ISPAYMENT,TWITKEY,TWITSECRET,TWITAPP,TWITAPPSECRET, LOCAL,ISTEST,RANDOMSPAWNS) VALUES(?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)";
    private static final String GET_NEIGHBOURS = "SELECT * FROM SERVERNEIGHBOURS WHERE SERVER=?";
    private static final String ADD_NEIGHBOUR = "INSERT INTO SERVERNEIGHBOURS(SERVER,NEIGHBOUR,DIRECTION) VALUES(?,?,?)";
    private static final String DELETE_NEIGHBOUR = "DELETE FROM SERVERNEIGHBOURS WHERE SERVER=? AND NEIGHBOUR=?";
    private static final String DELETE_SERVER = "DELETE FROM SERVERS WHERE SERVER=?";
    private static final String DELETE_SERVER2 = "DELETE FROM SERVERNEIGHBOURS WHERE SERVER=? OR NEIGHBOUR=?";
    private static final String SET_TWITTER = "UPDATE SERVERS SET TWITKEY=?,TWITSECRET=?,TWITAPP=?,TWITAPPSECRET=? WHERE SERVER=?";

    private Servers() {
    }

    public static ServerEntry getServer(String direction) {
        return neighbours.get(direction);
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public static boolean addServerNeighbour(int serverid, String direction) {
        boolean ok = false;
        if (Servers.loadNeighbour(serverid, direction)) {
            Connection dbcon = null;
            PreparedStatement ps = null;
            try {
                dbcon = DbConnector.getLoginDbCon();
                ps = dbcon.prepareStatement(ADD_NEIGHBOUR);
                ps.setInt(1, Servers.localServer.id);
                ps.setInt(2, serverid);
                ps.setString(3, direction);
                ps.executeUpdate();
                ok = true;
            }
            catch (SQLException sqex) {
                try {
                    logger.log(Level.WARNING, "Failed to insert neighbour " + serverid + "," + direction + " into logindb!" + sqex.getMessage(), sqex);
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
        return ok;
    }

    public static boolean isThisLoginServer() {
        return loginServer == null || Servers.localServer.id == Servers.loginServer.id;
    }

    public static boolean isRealLoginServer() {
        return Servers.localServer.id == Servers.loginServer.id;
    }

    public static boolean isThisAChaosServer() {
        return localServer != null && localServer.isChaosServer();
    }

    public static boolean isThisAnEpicServer() {
        return localServer != null && Servers.localServer.EPIC;
    }

    public static boolean isThisAnEpicOrChallengeServer() {
        return localServer != null && (Servers.localServer.EPIC || localServer.isChallengeServer());
    }

    public static boolean isThisAHomeServer() {
        return localServer != null && Servers.localServer.HOMESERVER;
    }

    public static boolean isThisAPvpServer() {
        return localServer != null && Servers.localServer.PVPSERVER;
    }

    public static boolean isThisATestServer() {
        return localServer != null && Servers.localServer.testServer;
    }

    public static byte getLocalKingdom() {
        byte localKingdom = localServer != null ? (byte)localServer.getKingdom() : (byte)-10;
        return localKingdom;
    }

    public static int getLocalServerId() {
        int localServerId = localServer != null ? localServer.getId() : 0;
        return localServerId;
    }

    public static ServerEntry getLoginServer() {
        return loginServer;
    }

    public static int getLoginServerId() {
        return Servers.loginServer.id;
    }

    public static String getLocalServerName() {
        String localServerName = localServer != null ? localServer.getName() : "Unknown";
        return localServerName;
    }

    public static boolean deleteServerNeighbour(String dir) {
        boolean ok = false;
        ServerEntry entry = neighbours.get(dir);
        if (entry != null) {
            ok = Servers.deleteServerNeighbour(entry.id);
        }
        neighbours.remove(dir);
        if (dir.equals("NORTH")) {
            Servers.localServer.serverNorth = null;
        } else if (dir.equals("WEST")) {
            Servers.localServer.serverWest = null;
        } else if (dir.equals("SOUTH")) {
            Servers.localServer.serverSouth = null;
        } else if (dir.equals("EAST")) {
            Servers.localServer.serverEast = null;
        }
        return ok;
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public static boolean deleteServerNeighbour(int id) {
        boolean ok = false;
        Connection dbcon = null;
        PreparedStatement ps = null;
        try {
            dbcon = DbConnector.getLoginDbCon();
            ps = dbcon.prepareStatement(DELETE_NEIGHBOUR);
            ps.setInt(1, Servers.localServer.id);
            ps.setInt(2, id);
            ps.executeUpdate();
            ok = true;
        }
        catch (SQLException sqex) {
            try {
                logger.log(Level.WARNING, "Failed to delete neighbour " + id + " from logindb!" + sqex.getMessage(), sqex);
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
        return ok;
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public static boolean deleteServerEntry(int id) {
        boolean ok;
        block5: {
            ok = false;
            Connection dbcon = null;
            PreparedStatement ps = null;
            PreparedStatement ps2 = null;
            try {
                dbcon = DbConnector.getLoginDbCon();
                ps2 = dbcon.prepareStatement(DELETE_SERVER2);
                ps2.setInt(1, id);
                ps2.setInt(2, id);
                ps2.executeUpdate();
                DbUtilities.closeDatabaseObjects(ps2, null);
                ps = dbcon.prepareStatement(DELETE_SERVER);
                ps.setInt(1, id);
                ps.executeUpdate();
                ok = true;
                DbUtilities.closeDatabaseObjects(ps, null);
            }
            catch (SQLException sqex) {
                logger.log(Level.WARNING, "Failed to delete neighbour " + id + " from logindb!" + sqex.getMessage(), sqex);
                break block5;
            }
            finally {
                DbUtilities.closeDatabaseObjects(ps, null);
                DbUtilities.closeDatabaseObjects(ps2, null);
                DbConnector.returnConnection(dbcon);
            }
            DbUtilities.closeDatabaseObjects(ps2, null);
            DbConnector.returnConnection(dbcon);
        }
        Servers.removeServer(id);
        return ok;
    }

    private static final void removeServer(int id) {
        allServers.remove(id);
        neighbours.remove(id);
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public static final void registerServer(int id, String name, boolean homeServer, int fox, int foy, int libx, int liby, int molx, int moly, String intraip, String intraport, String password, String externalip, String externalport, boolean loginserver, byte kingdom, boolean isPayment, String _consumerKeyToUse, String _consumerSecretToUse, String _applicationToken, String _applicationSecret, boolean isLocalServer, boolean isTestServer, boolean randomSpawns) {
        Connection dbcon = null;
        PreparedStatement ps2 = null;
        try {
            logger.log(Level.INFO, "Registering server id: " + id + ", external IP: " + externalip + ", name: " + name);
            dbcon = DbConnector.getLoginDbCon();
            ps2 = dbcon.prepareStatement(INSERT_SERVER);
            ps2.setInt(1, id);
            ps2.setString(2, name);
            ps2.setBoolean(3, homeServer);
            ps2.setInt(4, fox);
            ps2.setInt(5, foy);
            ps2.setInt(6, libx);
            ps2.setInt(7, liby);
            ps2.setInt(8, molx);
            ps2.setInt(9, moly);
            ps2.setString(10, intraip);
            ps2.setString(11, intraport);
            ps2.setString(12, password);
            ps2.setString(13, externalip);
            ps2.setString(14, externalport);
            ps2.setBoolean(15, loginserver);
            ps2.setByte(16, kingdom);
            ps2.setBoolean(17, isPayment);
            ps2.setString(18, _consumerKeyToUse);
            ps2.setString(19, _consumerSecretToUse);
            ps2.setString(20, _applicationToken);
            ps2.setString(21, _applicationSecret);
            ps2.setBoolean(22, isLocalServer);
            ps2.setBoolean(23, isTestServer);
            ps2.setBoolean(24, randomSpawns);
            ps2.executeUpdate();
            DbUtilities.closeDatabaseObjects(ps2, null);
            if (loginserver) {
                Servers.loadLoginServer();
            }
            Servers.loadAllServers(true);
        }
        catch (SQLException sqex) {
            try {
                logger.log(Level.WARNING, "Failed to load or insert server into logindb!" + sqex.getMessage(), sqex);
            }
            catch (Throwable throwable) {
                DbUtilities.closeDatabaseObjects(ps2, null);
                DbConnector.returnConnection(dbcon);
                throw throwable;
            }
            DbUtilities.closeDatabaseObjects(ps2, null);
            DbConnector.returnConnection(dbcon);
        }
        DbUtilities.closeDatabaseObjects(ps2, null);
        DbConnector.returnConnection(dbcon);
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public static final void setTwitCredentials(int serverId, String _consumerKeyToUse, String _consumerSecretToUse, String _applicationToken, String _applicationSecret) {
        ServerEntry entry = Servers.getServerWithId(serverId);
        if (entry != null) {
            entry.consumerKeyToUse = _consumerKeyToUse;
            entry.consumerSecretToUse = _consumerSecretToUse;
            entry.applicationToken = _applicationToken;
            entry.applicationSecret = _applicationSecret;
            entry.canTwit();
        }
        if (Servers.localServer.id == serverId) {
            Servers.localServer.consumerKeyToUse = _consumerKeyToUse;
            Servers.localServer.consumerSecretToUse = _consumerSecretToUse;
            Servers.localServer.applicationToken = _applicationToken;
            Servers.localServer.applicationSecret = _applicationSecret;
            localServer.canTwit();
        }
        Connection dbcon = null;
        PreparedStatement ps = null;
        try {
            dbcon = DbConnector.getLoginDbCon();
            ps = dbcon.prepareStatement(SET_TWITTER);
            ps.setString(1, _consumerKeyToUse);
            ps.setString(2, _consumerSecretToUse);
            ps.setString(3, _applicationToken);
            ps.setString(4, _applicationSecret);
            ps.setInt(5, serverId);
            ps.executeUpdate();
        }
        catch (SQLException sqx) {
            try {
                logger.log(Level.WARNING, "Failed to set twitter info for server with id " + serverId, sqx);
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
    public static final void loadNeighbours() {
        neighbours = new HashMap<String, ServerEntry>();
        if (localServer != null) {
            Servers.localServer.serverNorth = null;
            Servers.localServer.serverEast = null;
            Servers.localServer.serverSouth = null;
            Servers.localServer.serverWest = null;
            Connection dbcon = null;
            PreparedStatement ps = null;
            ResultSet rs = null;
            try {
                dbcon = DbConnector.getLoginDbCon();
                ps = dbcon.prepareStatement(GET_NEIGHBOURS);
                ps.setInt(1, Servers.localServer.id);
                rs = ps.executeQuery();
                while (rs.next()) {
                    int serverid = rs.getInt("NEIGHBOUR");
                    String direction = rs.getString("DIRECTION");
                    Servers.loadNeighbour(serverid, direction);
                }
            }
            catch (SQLException sqex) {
                try {
                    logger.log(Level.WARNING, "Failed to load all neighbours!" + sqex.getMessage(), sqex);
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

    public static final boolean loadNeighbour(int serverid, String dir) {
        boolean ok = false;
        ServerEntry entry = Servers.getServerWithId(serverid);
        if (entry != null) {
            logger.log(Level.INFO, "found neighbour " + entry.name + " " + dir);
            neighbours.put(dir, entry);
            if (dir.equals("NORTH")) {
                logger.log(Level.INFO, "NORTH neighbour " + entry.name + " " + dir);
                Servers.localServer.serverNorth = entry;
            } else if (dir.equals("WEST")) {
                logger.log(Level.INFO, "WEST neighbour " + entry.name + " " + dir);
                Servers.localServer.serverWest = entry;
            } else if (dir.equals("SOUTH")) {
                logger.log(Level.INFO, "SOUTH neighbour " + entry.name + " " + dir);
                Servers.localServer.serverSouth = entry;
            } else if (dir.equals("EAST")) {
                logger.log(Level.INFO, "EAST neighbour " + entry.name + " " + dir);
                Servers.localServer.serverEast = entry;
            }
            ok = true;
        }
        return ok;
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public static void loadAllServers(boolean reload) {
        System.out.println("Loading servers");
        Connection dbcon = null;
        PreparedStatement ps = null;
        ResultSet rs = null;
        try {
            if (!reload) {
                allServers = new ConcurrentHashMap<Integer, ServerEntry>();
                localServer = null;
            } else {
                for (ServerEntry server : allServers.values()) {
                    server.reloading = true;
                }
            }
            logger.log(Level.INFO, "Loading all servers.");
            dbcon = DbConnector.getLoginDbCon();
            ps = dbcon.prepareStatement(GET_ALL_SERVERS);
            rs = ps.executeQuery();
            while (rs.next()) {
                long time;
                String regPort;
                int loadedId = rs.getInt("SERVER");
                ServerEntry entry = null;
                if (reload) {
                    entry = Servers.getServerWithId(loadedId);
                }
                if (entry == null) {
                    entry = new ServerEntry();
                    entry.id = loadedId;
                } else {
                    entry.reloading = false;
                }
                entry.HOMESERVER = rs.getBoolean("HOMESERVER");
                entry.PVPSERVER = rs.getBoolean("PVP");
                entry.name = rs.getString("NAME");
                logger.log(Level.INFO, "Loading " + entry.name + " - " + entry.id);
                entry.isLocal = rs.getBoolean("LOCAL");
                if (entry.isLocal) {
                    localServer = entry;
                    entry.isAvailable = true;
                    entry.setNextEpicPoll(rs.getLong("NEXTEPICPOLL"));
                    entry.setSkillDaySwitch(rs.getLong("SKILLDAYSWITCH"));
                    entry.setSkillWeekSwitch(rs.getLong("SKILLWEEKSWITCH"));
                    entry.setNextHota(rs.getLong("NEXTHOTA"));
                    entry.setFatigueSwitch(rs.getLong("FATIGUESWITCH"));
                }
                entry.SPAWNPOINTJENNX = rs.getInt("SPAWNPOINTJENNX");
                entry.SPAWNPOINTJENNY = rs.getInt("SPAWNPOINTJENNY");
                entry.SPAWNPOINTLIBX = rs.getInt("SPAWNPOINTLIBX");
                entry.SPAWNPOINTLIBY = rs.getInt("SPAWNPOINTLIBY");
                entry.SPAWNPOINTMOLX = rs.getInt("SPAWNPOINTMOLX");
                entry.SPAWNPOINTMOLY = rs.getInt("SPAWNPOINTMOLY");
                entry.INTRASERVERADDRESS = rs.getString("INTRASERVERADDRESS");
                entry.INTRASERVERPORT = rs.getString("INTRASERVERPORT");
                entry.INTRASERVERPASSWORD = rs.getString("INTRASERVERPASSWORD");
                entry.EXTERNALIP = rs.getString("EXTERNALIP");
                entry.EXTERNALPORT = rs.getString("EXTERNALPORT");
                entry.LOGINSERVER = rs.getBoolean("LOGINSERVER");
                if (entry.LOGINSERVER) {
                    loginServer = entry;
                }
                entry.KINGDOM = rs.getByte("KINGDOM");
                entry.ISPAYMENT = rs.getBoolean("ISPAYMENT");
                entry.entryServer = rs.getBoolean("ENTRYSERVER");
                entry.testServer = rs.getBoolean("ISTEST");
                entry.challengeServer = rs.getBoolean("CHALLENGE");
                if (entry.challengeServer) {
                    entry.setChallengeStarted(rs.getLong("CHALLENGESTARTED"));
                    entry.setChallengeEnds(rs.getLong("CHALLENGEEND"));
                    if (entry.getChallengeStarted() == 0L && entry.getChallengeEnds() == 0L) {
                        entry.challengeServer = false;
                    }
                }
                entry.lastDecreasedChampionPoints = rs.getLong("LASTRESETCHAMPS");
                entry.consumerKeyToUse = rs.getString("TWITKEY");
                entry.consumerSecretToUse = rs.getString("TWITSECRET");
                entry.applicationToken = rs.getString("TWITAPP");
                entry.applicationSecret = rs.getString("TWITAPPSECRET");
                entry.champConsumerKeyToUse = rs.getString("CHAMPTWITKEY");
                entry.champConsumerSecretToUse = rs.getString("CHAMPTWITSECRET");
                entry.champApplicationToken = rs.getString("CHAMPTWITAPP");
                entry.champApplicationSecret = rs.getString("CHAMPTWITAPPSECRET");
                long movedArtifacts = rs.getLong("MOVEDARTIS");
                if (movedArtifacts > 0L) {
                    entry.setMovedArtifacts(movedArtifacts);
                } else {
                    entry.movedArtifacts();
                }
                long lastSpawnedUnique = rs.getLong("SPAWNEDUNIQUE");
                entry.setLastSpawnedUnique(lastSpawnedUnique);
                entry.canTwit();
                String rmiPort = rs.getString("RMIPORT");
                if (rmiPort != null && rmiPort.length() > 0) {
                    try {
                        entry.RMI_PORT = Integer.parseInt(rmiPort);
                    }
                    catch (NullPointerException npe) {
                        logger.log(Level.WARNING, "rmiPort for server " + loadedId + " was not a number " + rmiPort, npe);
                    }
                }
                if ((regPort = rs.getString("REGISTRATIONPORT")) != null && regPort.length() > 0) {
                    try {
                        entry.REGISTRATION_PORT = Integer.parseInt(regPort);
                    }
                    catch (NullPointerException npe) {
                        logger.log(Level.WARNING, "regPort for server " + loadedId + " was not a number " + regPort, npe);
                    }
                }
                try {
                    entry.pLimit = rs.getInt("MAXPLAYERS");
                    entry.maxCreatures = rs.getInt("MAXCREATURES");
                    entry.maxTypedCreatures = entry.maxCreatures / 8;
                    entry.percentAggCreatures = rs.getFloat("PERCENT_AGG_CREATURES");
                    TilePoller.treeGrowth = entry.treeGrowth = rs.getInt("TREEGROWTH");
                    entry.setSkillGainRate(rs.getFloat("SKILLGAINRATE"));
                    entry.setActionTimer(rs.getFloat("ACTIONTIMER"));
                    entry.setHotaDelay(rs.getInt("HOTADELAY"));
                }
                catch (Exception ex) {
                    logger.log(Level.WARNING, "Please run USE WURMLOGIN;    ALTER TABLE SERVERS ADD COLUMN MAXPLAYERS INT NOT NULL DEFAULT 1000;    ALTER TABLE SERVERS ADD COLUMN MAXCREATURES INT NOT NULL DEFAULT 1000;    ALTER TABLE SERVERS ADD COLUMN PERCENT_AGG_CREATURES FLOAT NOT NULL DEFAULT 30;    ALTER TABLE SERVERS ADD COLUMN TREEGROWTH INT NOT NULL DEFAULT 20;    ALTER TABLE SERVERS ADD COLUMN SKILLGAINRATE FLOAT NOT NULL DEFAULT 100;    ALTER TABLE SERVERS ADD COLUMN ACTIONTIMER FLOAT NOT NULL DEFAULT 100;    ALTER TABLE SERVERS ADD COLUMN HOTADELAY INT NOT NULL DEFAULT 2160; ALTER TABLE SERVERS ADD COLUMN MESHSIZE INT NOT NULL DEFAULT 2048;");
                }
                try {
                    entry.mapname = rs.getString("MAPNAME");
                }
                catch (Exception ex) {
                    // empty catch block
                }
                try {
                    entry.randomSpawns = rs.getBoolean("RANDOMSPAWNS");
                    entry.setSkillbasicval(rs.getFloat("SKILLBASICSTART"));
                    entry.setSkillfightval(rs.getFloat("SKILLFIGHTINGSTART"));
                    entry.setSkillmindval(rs.getFloat("SKILLMINDLOGICSTART"));
                    entry.setSkilloverallval(rs.getFloat("SKILLOVERALLSTART"));
                    entry.EPIC = rs.getBoolean("EPIC");
                    entry.setCombatRatingModifier(rs.getFloat("CRMOD"));
                    entry.setSteamServerPassword(rs.getString("STEAMPW"));
                    entry.setUpkeep(rs.getBoolean("UPKEEP"));
                    entry.setMaxDeedSize(rs.getInt("MAXDEED"));
                    entry.setFreeDeeds(rs.getBoolean("FREEDEEDS"));
                    entry.setTraderMaxIrons(rs.getInt("TRADERMAX"));
                    entry.setInitialTraderIrons(rs.getInt("TRADERINIT"));
                    entry.setTunnelingHits(rs.getInt("TUNNELING"));
                    entry.setBreedingTimer(rs.getLong("BREEDING"));
                    entry.setFieldGrowthTime(rs.getLong("FIELDGROWTH"));
                    entry.setKingsmoneyAtRestart(rs.getInt("KINGSMONEY"));
                    entry.setMotd(rs.getString("MOTD"));
                    entry.setSkillbcval(rs.getFloat("SKILLBODYCONTROLSTART"));
                }
                catch (Exception ex) {
                    // empty catch block
                }
                byte caHelpGroup = rs.getByte("CAHELPGROUP");
                entry.setCAHelpGroup(caHelpGroup);
                allServers.put(entry.id, entry);
                if (logger.isLoggable(Level.FINE)) {
                    logger.fine("Loaded server " + entry);
                }
                if (!entry.isLocal || (time = rs.getLong("WORLDTIME")) <= 0L) continue;
                logger.log(Level.INFO, "Using database entry for time " + time);
                WurmCalendar.setTime(time);
                TilePoller.currentPollTile = rs.getInt("POLLTILE");
                TilePoller.rest = rs.getInt("TILEREST");
                TilePoller.pollModifier = rs.getInt("POLLMOD");
                TilePoller.pollround = rs.getInt("POLLROUND");
                WurmCalendar.checkSpring();
                TilePoller.calcRest();
            }
        }
        catch (SQLException sqex) {
            try {
                logger.log(Level.WARNING, "Failed to load all servers!" + sqex.getMessage(), sqex);
            }
            catch (Throwable throwable) {
                DbUtilities.closeDatabaseObjects(ps, rs);
                DbConnector.returnConnection(dbcon);
                logger.info("Loaded " + allServers.size() + " servers from the database");
                throw throwable;
            }
            DbUtilities.closeDatabaseObjects(ps, rs);
            DbConnector.returnConnection(dbcon);
            logger.info("Loaded " + allServers.size() + " servers from the database");
        }
        DbUtilities.closeDatabaseObjects(ps, rs);
        DbConnector.returnConnection(dbcon);
        logger.info("Loaded " + allServers.size() + " servers from the database");
        HashSet<Integer> toDelete = new HashSet<Integer>();
        for (ServerEntry server : allServers.values()) {
            if (server.reloading) {
                toDelete.add(server.id);
            }
            server.reloading = false;
        }
        for (Integer id : toDelete) {
            allServers.remove(id);
        }
        Servers.loadNeighbours();
        ServerProperties.loadProperties();
    }

    public static final void moveServerId(ServerEntry entry, int oldId) {
        allServers.remove(oldId);
        allServers.put(entry.getId(), entry);
        if (entry.isLocal) {
            localServer = entry;
        }
    }

    public static void loadLoginServer() {
        for (ServerEntry server : allServers.values()) {
            if (!server.LOGINSERVER) continue;
            loginServer = server;
            logger.log(Level.INFO, "Loaded loginserver " + Servers.loginServer.id);
            return;
        }
    }

    public static String rename(String oldName, String newName, String newPass, int power) {
        String toReturn = "";
        if (!Servers.localServer.testServer) {
            for (ServerEntry s : allServers.values()) {
                if (s.isConnected()) continue;
                return "Not all servers are connected (" + s.getName() + "). Try later. This is an Error.";
            }
        }
        for (ServerEntry s : allServers.values()) {
            if (s.id == Servers.localServer.id) continue;
            LoginServerWebConnection lsw = new LoginServerWebConnection(s.id);
            toReturn = toReturn + lsw.renamePlayer(oldName, newName, newPass, power);
        }
        return toReturn;
    }

    public static String sendChangePass(String changerName, String name, String newPass, int power) {
        String toReturn = "";
        for (ServerEntry s : allServers.values()) {
            if (s.id == Servers.localServer.id) continue;
            LoginServerWebConnection lsw = new LoginServerWebConnection(s.id);
            toReturn = toReturn + lsw.changePassword(changerName, name, newPass, power);
        }
        return toReturn;
    }

    public static String requestDemigod(byte existingDeity, String deityName) {
        String toReturn = "";
        int max = 0;
        for (ServerEntry s : allServers.values()) {
            if (s.id == Servers.localServer.id || !s.EPIC) continue;
            ++max;
        }
        if (max > 0) {
            if (Servers.localServer.testServer) {
                LoginServerWebConnection lsw = new LoginServerWebConnection(63500);
                lsw.requestDemigod(existingDeity, deityName);
            } else {
                for (ServerEntry s : allServers.values()) {
                    if (s.id == Servers.localServer.id || !s.EPIC || Server.rand.nextInt(max) != 0) continue;
                    LoginServerWebConnection lsw = new LoginServerWebConnection(s.id);
                    lsw.requestDemigod(existingDeity, deityName);
                }
            }
        }
        return "";
    }

    public static String ascend(int nextDeityId, String deityname, long wurmid, byte existingDeity, byte gender, byte newPower, float initialBStr, float initialBSta, float initialBCon, float initialML, float initialMS, float initialSS, float initialSD) {
        String toReturn = "";
        for (ServerEntry s : allServers.values()) {
            if (s.id == Servers.localServer.id) continue;
            LoginServerWebConnection lsw = new LoginServerWebConnection(s.id);
            lsw.ascend(nextDeityId, deityname, wurmid, existingDeity, gender, newPower, initialBStr, initialBSta, initialBCon, initialML, initialMS, initialSS, initialSD);
        }
        return "";
    }

    public static String changeEmail(String changerName, String name, String newEmail, String password, int power, String pwQuestion, String pwAnswer) {
        String toReturn = "";
        for (ServerEntry s : allServers.values()) {
            if (s.id == Servers.localServer.id) continue;
            if (s.isAvailable(5, true)) {
                LoginServerWebConnection lsw = new LoginServerWebConnection(s.id);
                toReturn = toReturn + lsw.changeEmail(changerName, name, newEmail, password, power, pwQuestion, pwAnswer);
                continue;
            }
            toReturn = toReturn + s.name + " was unavailable. ";
        }
        return toReturn;
    }

    public static ServerEntry[] getAllServers() {
        return allServers.values().toArray(new ServerEntry[allServers.size()]);
    }

    public static ServerEntry[] getAllNeighbours() {
        return neighbours.values().toArray(new ServerEntry[neighbours.size()]);
    }

    public static ServerEntry getEntryServer() {
        ServerEntry[] alls;
        for (ServerEntry lAll : alls = Servers.getAllServers()) {
            if (!lAll.entryServer) continue;
            return lAll;
        }
        return null;
    }

    public static List<ServerEntry> getServerList(int desiredNum) {
        boolean getEpicServers = desiredNum == 100001;
        boolean getFreedomServers = desiredNum == 100000;
        boolean getChallengeServers = desiredNum == 100002;
        ArrayList<ServerEntry> lAskedServers = new ArrayList<ServerEntry>();
        for (ServerEntry lServerEntry : allServers.values()) {
            if (!(desiredNum == lServerEntry.id || getEpicServers && lServerEntry.EPIC != Servers.localServer.EPIC || getChallengeServers && lServerEntry.isChallengeServer()) && (!getFreedomServers || lServerEntry.PVPSERVER && lServerEntry.id != 3)) continue;
            lAskedServers.add(lServerEntry);
        }
        return lAskedServers;
    }

    public static ServerEntry getServerWithId(int id) {
        ServerEntry[] alls;
        if (loginServer != null && Servers.loginServer.id == id) {
            return loginServer;
        }
        if (localServer != null) {
            if (Servers.localServer.serverNorth != null && Servers.localServer.serverNorth.id == id) {
                return Servers.localServer.serverNorth;
            }
            if (Servers.localServer.serverSouth != null && Servers.localServer.serverSouth.id == id) {
                return Servers.localServer.serverSouth;
            }
            if (Servers.localServer.serverWest != null && Servers.localServer.serverWest.id == id) {
                return Servers.localServer.serverWest;
            }
            if (Servers.localServer.serverEast != null && Servers.localServer.serverEast.id == id) {
                return Servers.localServer.serverEast;
            }
        }
        for (ServerEntry entry : alls = Servers.getAllServers()) {
            if (entry.id != id) continue;
            return entry;
        }
        return null;
    }

    public static ServerEntry getClosestSpawnServer(byte aKingdom) {
        ServerEntry[] alls;
        if (Servers.localServer.serverNorth != null && (Servers.localServer.serverNorth.kingdomExists(aKingdom) || Servers.localServer.serverNorth.KINGDOM == aKingdom)) {
            return Servers.localServer.serverNorth;
        }
        if (Servers.localServer.serverSouth != null && (Servers.localServer.serverSouth.kingdomExists(aKingdom) || Servers.localServer.serverSouth.KINGDOM == aKingdom)) {
            return Servers.localServer.serverSouth;
        }
        if (Servers.localServer.serverWest != null && (Servers.localServer.serverWest.kingdomExists(aKingdom) || Servers.localServer.serverWest.KINGDOM == aKingdom)) {
            return Servers.localServer.serverWest;
        }
        if (Servers.localServer.serverEast != null && (Servers.localServer.serverEast.kingdomExists(aKingdom) || Servers.localServer.serverEast.KINGDOM == aKingdom)) {
            return Servers.localServer.serverEast;
        }
        if (Servers.localServer.serverNorth != null && !Servers.localServer.serverNorth.HOMESERVER) {
            return Servers.localServer.serverNorth;
        }
        if (Servers.localServer.serverSouth != null && !Servers.localServer.serverSouth.HOMESERVER) {
            return Servers.localServer.serverSouth;
        }
        if (Servers.localServer.serverWest != null && !Servers.localServer.serverWest.HOMESERVER) {
            return Servers.localServer.serverWest;
        }
        if (Servers.localServer.serverEast != null && !Servers.localServer.serverEast.HOMESERVER) {
            return Servers.localServer.serverEast;
        }
        for (ServerEntry entry : alls = Servers.getAllNeighbours()) {
            if (entry.EPIC != Servers.localServer.EPIC || entry.isChallengeServer() != localServer.isChallengeServer() || !entry.kingdomExists(aKingdom) && entry.KINGDOM != aKingdom) continue;
            return entry;
        }
        return null;
    }

    public static ServerEntry getClosestJennHomeServer() {
        ServerEntry[] alls;
        if (Servers.localServer.serverNorth != null && Servers.localServer.serverNorth.HOMESERVER && Servers.localServer.serverNorth.KINGDOM == 1) {
            return Servers.localServer.serverNorth;
        }
        if (Servers.localServer.serverSouth != null && Servers.localServer.serverSouth.HOMESERVER && Servers.localServer.serverSouth.KINGDOM == 1) {
            return Servers.localServer.serverSouth;
        }
        if (Servers.localServer.serverWest != null && Servers.localServer.serverWest.HOMESERVER && Servers.localServer.serverWest.KINGDOM == 1) {
            return Servers.localServer.serverWest;
        }
        if (Servers.localServer.serverEast != null && Servers.localServer.serverEast.HOMESERVER && Servers.localServer.serverEast.KINGDOM == 1) {
            return Servers.localServer.serverEast;
        }
        for (ServerEntry entry : alls = Servers.getAllNeighbours()) {
            if (!entry.HOMESERVER || entry.KINGDOM != 1) continue;
            return entry;
        }
        if (Servers.localServer.serverNorth != null && !Servers.localServer.serverNorth.HOMESERVER) {
            return Servers.localServer.serverNorth;
        }
        if (Servers.localServer.serverSouth != null && !Servers.localServer.serverNorth.HOMESERVER) {
            return Servers.localServer.serverSouth;
        }
        if (Servers.localServer.serverWest != null && !Servers.localServer.serverNorth.HOMESERVER) {
            return Servers.localServer.serverWest;
        }
        if (Servers.localServer.serverEast != null && !Servers.localServer.serverNorth.HOMESERVER) {
            return Servers.localServer.serverEast;
        }
        for (ServerEntry entry : alls) {
            if (entry.HOMESERVER) continue;
            return entry;
        }
        return null;
    }

    public static ServerEntry getClosestMolRehanHomeServer() {
        ServerEntry[] alls;
        if (Servers.localServer.serverNorth != null && Servers.localServer.serverNorth.HOMESERVER && Servers.localServer.serverNorth.KINGDOM == 2) {
            return Servers.localServer.serverNorth;
        }
        if (Servers.localServer.serverSouth != null && Servers.localServer.serverSouth.HOMESERVER && Servers.localServer.serverSouth.KINGDOM == 2) {
            return Servers.localServer.serverSouth;
        }
        if (Servers.localServer.serverWest != null && Servers.localServer.serverWest.HOMESERVER && Servers.localServer.serverWest.KINGDOM == 2) {
            return Servers.localServer.serverWest;
        }
        if (Servers.localServer.serverEast != null && Servers.localServer.serverEast.HOMESERVER && Servers.localServer.serverEast.KINGDOM == 2) {
            return Servers.localServer.serverEast;
        }
        for (ServerEntry entry : alls = Servers.getAllNeighbours()) {
            if (!entry.HOMESERVER || entry.KINGDOM != 2) continue;
            return entry;
        }
        if (Servers.localServer.serverNorth != null && !Servers.localServer.serverNorth.HOMESERVER) {
            return Servers.localServer.serverNorth;
        }
        if (Servers.localServer.serverSouth != null && !Servers.localServer.serverNorth.HOMESERVER) {
            return Servers.localServer.serverSouth;
        }
        if (Servers.localServer.serverWest != null && !Servers.localServer.serverNorth.HOMESERVER) {
            return Servers.localServer.serverWest;
        }
        if (Servers.localServer.serverEast != null && !Servers.localServer.serverNorth.HOMESERVER) {
            return Servers.localServer.serverEast;
        }
        for (ServerEntry entry : alls) {
            if (entry.HOMESERVER) continue;
            return entry;
        }
        return null;
    }

    public static void sendWeather(float windRotation, float windpower, float windDir) {
        ServerEntry[] alls;
        for (ServerEntry entry : alls = Servers.getAllServers()) {
            if (entry.id == Servers.localServer.id) continue;
            LoginServerWebConnection lsw = new LoginServerWebConnection(entry.id);
            lsw.setWeather(windRotation, windpower, windDir);
        }
    }

    public static final void pingServers() {
        boolean pollResult;
        if (Servers.localServer.serverNorth != null) {
            pollResult = Servers.localServer.serverNorth.poll();
            if (logger.isLoggable(Level.FINER)) {
                logger.finer("Polling north server result: " + pollResult + ", " + Servers.localServer.serverNorth);
            }
        }
        if (Servers.localServer.serverEast != null) {
            pollResult = Servers.localServer.serverEast.poll();
            if (logger.isLoggable(Level.FINER)) {
                logger.finer("Polling east server result: " + pollResult + ", " + Servers.localServer.serverEast);
            }
        }
        if (Servers.localServer.serverSouth != null) {
            pollResult = Servers.localServer.serverSouth.poll();
            if (logger.isLoggable(Level.FINER)) {
                logger.finer("Polling south server result: " + pollResult + ", " + Servers.localServer.serverSouth);
            }
        }
        if (Servers.localServer.serverWest != null) {
            pollResult = Servers.localServer.serverWest.poll();
            if (logger.isLoggable(Level.FINER)) {
                logger.finer("Polling west server result: " + pollResult + ", " + Servers.localServer.serverWest);
            }
        }
        if (!Servers.localServer.LOGINSERVER) {
            if (loginServer != null) {
                pollResult = loginServer.poll();
                if (logger.isLoggable(Level.FINER)) {
                    logger.finer("Polling login server result: " + pollResult + ", " + loginServer);
                }
            }
            for (ServerEntry portal : neighbours.values()) {
                if (portal == Servers.localServer.serverEast || portal == Servers.localServer.serverWest || portal == Servers.localServer.serverNorth || portal == Servers.localServer.serverSouth) continue;
                portal.poll();
            }
        } else {
            for (ServerEntry entry : allServers.values()) {
                if (entry.LOGINSERVER) continue;
                pollResult = entry.poll();
                if (!logger.isLoggable(Level.FINER)) continue;
                logger.finer("Polling server id " + entry.id + " result: " + pollResult + ", " + entry);
            }
        }
    }

    public static final void sendKingdomExistsToAllServers(int serverId, byte kingdomId, boolean exists) {
        for (ServerEntry entry : allServers.values()) {
            if (!entry.isAvailable(5, true) || entry.id == Servers.localServer.id || entry.id == serverId) continue;
            LoginServerWebConnection lsw = new LoginServerWebConnection(entry.id);
            lsw.kingdomExists(serverId, kingdomId, exists);
        }
    }

    public static final void sendWebCommandToAllServers(short type, WebCommand command, boolean restrictEpic) {
        new Thread(){

            @Override
            public void run() {
                for (ServerEntry entry : allServers.values()) {
                    if (!entry.isAvailable(5, true) || entry.id == Servers.localServer.id || entry.id == WurmId.getOrigin(command.getWurmId()) || !entry.EPIC && restrictEpic) continue;
                    LoginServerWebConnection lsw = new LoginServerWebConnection(entry.id);
                    lsw.sendWebCommand(type, command);
                }
            }
        }.start();
    }

    public static final boolean kingdomExists(int serverId, byte kingdomId, boolean exists) {
        ServerEntry e;
        if (!exists) {
            if (Servers.localServer.serverEast != null && Servers.localServer.serverEast.id == serverId) {
                Servers.localServer.serverEast.removeKingdom(kingdomId);
            }
            if (Servers.localServer.serverWest != null && Servers.localServer.serverWest.id == serverId) {
                Servers.localServer.serverWest.removeKingdom(kingdomId);
            }
            if (Servers.localServer.serverNorth != null && Servers.localServer.serverNorth.id == serverId) {
                Servers.localServer.serverNorth.removeKingdom(kingdomId);
            }
            if (Servers.localServer.serverSouth != null && Servers.localServer.serverSouth.id == serverId) {
                Servers.localServer.serverSouth.removeKingdom(kingdomId);
            }
            for (ServerEntry serverEntry : neighbours.values()) {
                if (serverEntry == Servers.localServer.serverEast || serverEntry == Servers.localServer.serverWest || serverEntry == Servers.localServer.serverNorth || serverEntry == Servers.localServer.serverSouth || serverEntry.id != serverId) continue;
                serverEntry.removeKingdom(kingdomId);
            }
            ServerEntry e2 = allServers.get(serverId);
            if (e2 != null) {
                e2.removeKingdom(kingdomId);
            }
            if (Servers.localServer.id == serverId) {
                localServer.removeKingdom(kingdomId);
            }
            if (Servers.loginServer.id == serverId) {
                loginServer.removeKingdom(kingdomId);
            }
            for (ServerEntry se : allServers.values()) {
                if (!se.kingdomExists(kingdomId) || se.id == serverId) continue;
                return true;
            }
            return false;
        }
        if (Servers.localServer.serverEast != null && Servers.localServer.serverEast.id == serverId) {
            Servers.localServer.serverEast.addExistingKingdom(kingdomId);
        }
        if (Servers.localServer.serverWest != null && Servers.localServer.serverWest.id == serverId) {
            Servers.localServer.serverWest.addExistingKingdom(kingdomId);
        }
        if (Servers.localServer.serverNorth != null && Servers.localServer.serverNorth.id == serverId) {
            Servers.localServer.serverNorth.addExistingKingdom(kingdomId);
        }
        if (Servers.localServer.serverSouth != null && Servers.localServer.serverSouth.id == serverId) {
            Servers.localServer.serverSouth.addExistingKingdom(kingdomId);
        }
        if (neighbours != null) {
            for (ServerEntry serverEntry : neighbours.values()) {
                if (serverEntry == Servers.localServer.serverEast || serverEntry == Servers.localServer.serverWest || serverEntry == Servers.localServer.serverNorth || serverEntry == Servers.localServer.serverSouth || serverEntry.id != serverId) continue;
                serverEntry.addExistingKingdom(kingdomId);
            }
        }
        if (Servers.localServer.id == serverId) {
            localServer.addExistingKingdom(kingdomId);
        }
        if (Servers.loginServer.id == serverId) {
            loginServer.addExistingKingdom(kingdomId);
        }
        if ((e = allServers.get(serverId)) != null) {
            e.addExistingKingdom(kingdomId);
        } else {
            logger.log(Level.WARNING, "No such server - " + serverId + ", kingdom=" + kingdomId + ", exists: " + exists);
        }
        return true;
    }

    public static final void removeKingdomInfo(byte kingdomId) {
        if (Servers.localServer.serverEast != null) {
            Servers.localServer.serverEast.removeKingdom(kingdomId);
        }
        if (Servers.localServer.serverWest != null) {
            Servers.localServer.serverWest.removeKingdom(kingdomId);
        }
        if (Servers.localServer.serverNorth != null) {
            Servers.localServer.serverNorth.removeKingdom(kingdomId);
        }
        if (Servers.localServer.serverSouth != null) {
            Servers.localServer.serverSouth.removeKingdom(kingdomId);
        }
        for (ServerEntry portal : neighbours.values()) {
            if (portal == Servers.localServer.serverEast || portal == Servers.localServer.serverWest || portal == Servers.localServer.serverNorth || portal == Servers.localServer.serverSouth) continue;
            portal.removeKingdom(kingdomId);
        }
        for (ServerEntry entry : allServers.values()) {
            entry.removeKingdom(kingdomId);
        }
    }

    public static final int getNumberOfLoyalServers(int deityId) {
        byte kingdomTemplate = Deities.getFavoredKingdom(deityId);
        int toReturn = 0;
        for (ServerEntry entry : allServers.values()) {
            if (entry.LOGINSERVER || !entry.EPIC) continue;
            if (!entry.HOMESERVER) {
                ++toReturn;
                continue;
            }
            if (entry.KINGDOM != kingdomTemplate) continue;
            ++toReturn;
        }
        return toReturn;
    }

    public static void startShutdown(String instigator, int seconds, String reason) {
        if (Servers.isThisLoginServer()) {
            for (ServerEntry server : Servers.getAllServers()) {
                if (server.id == Servers.getLocalServerId()) continue;
                LoginServerWebConnection lsw = new LoginServerWebConnection(server.id);
                lsw.startShutdown(instigator, seconds, reason);
            }
        } else {
            LoginServerWebConnection lsw = new LoginServerWebConnection();
            lsw.startShutdown(instigator, seconds, reason);
        }
    }

    public static final boolean mayEnterServer(Creature _player, ServerEntry entry) {
        return true;
    }

    public static boolean isAvailableDestination(Creature performer, ServerEntry entry) {
        return entry.isAvailable(5, true);
    }

    public static ServerEntry[] getDestinations(Creature performer) {
        Object[] allServers = Servers.getAllServers();
        ArrayList<Object> servers = new ArrayList<Object>();
        Arrays.sort(allServers);
        for (Object entry : allServers) {
            if (!Servers.isAvailableDestination(performer, (ServerEntry)entry)) continue;
            servers.add(entry);
        }
        servers.sort((s1, s2) -> s1.getName().compareTo(s2.getName()));
        return servers.toArray(new ServerEntry[servers.size()]);
    }

    public static ServerEntry getDestinationFor(Creature performer) {
        if (performer.isVehicleCommander() && performer.getVehicle() != -10L) {
            Vehicle vehicle = Vehicles.getVehicleForId(performer.getVehicle());
            if (vehicle.hasDestinationSet() && Servers.mayEnterServer(performer, vehicle.getDestinationServer())) {
                return vehicle.getDestinationServer();
            }
        } else if (performer.getDestination() != null) {
            return performer.getDestination();
        }
        return localServer;
    }

    static {
        logger = Logger.getLogger(Servers.class.getName());
    }
}


/*
 * Decompiled with CFR 0.152.
 */
package com.wurmonline.server;

import com.wurmonline.server.Constants;
import com.wurmonline.server.DbConnector;
import com.wurmonline.server.Servers;
import com.wurmonline.server.steam.SteamHandler;
import com.wurmonline.server.utils.DbUtilities;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;

public class ServerProperties {
    private static final String loadAll = "SELECT * FROM SERVERPROPERTIES";
    private static final String insert = "INSERT INTO SERVERPROPERTIES(PROPVAL,PROPKEY) VALUES (?,?)";
    private static final String update = "UPDATE SERVERPROPERTIES SET PROPVAL=? WHERE PROPKEY=?";
    private static final String createTable = "CREATE TABLE IF NOT EXISTS SERVERPROPERTIES        (            PROPKEY                 VARCHAR(50)   NOT NULL DEFAULT '',            PROPVAL                 VARCHAR(50)   NOT NULL DEFAULT ''        )";
    private static final Properties props = new Properties();
    public static final String STEAMQUERY = "STEAMQUERYPORT";
    public static final String NPCS = "NPCS";
    public static final String ADMIN_PASSWORD = "ADMINPASSWORD";
    public static final String ENDGAMEITEMS = "ENDGAMEITEMS";
    public static final String SPY_PREVENTION = "SPYPREVENTION";
    public static final String AUTO_NETWORKING = "AUTO_NETWORKING";
    public static final String ENABLE_PNP_PORT_FORWARD = "ENABLE_PNP_PORT_FORWARD";
    public static final String NEWBIE_FRIENDLY = "NEWBIEFRIENDLY";
    private static final Logger logger = Logger.getLogger(Servers.class.getName());

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public static final void loadProperties() {
        ServerProperties.checkIfCreateTable();
        Connection dbcon = null;
        PreparedStatement ps = null;
        ResultSet rs = null;
        try {
            dbcon = DbConnector.getLoginDbCon();
            ps = dbcon.prepareStatement(loadAll);
            rs = ps.executeQuery();
            while (rs.next()) {
                String key = rs.getString("PROPKEY");
                String value = rs.getString("PROPVAL");
                props.put(key, value);
            }
        }
        catch (SQLException sqex) {
            try {
                logger.log(Level.WARNING, "Failed to load properties!" + sqex.getMessage(), sqex);
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
        ServerProperties.checkProperties();
    }

    public static final void checkProperties() {
        String sqp = props.getProperty(STEAMQUERY);
        if (sqp == null) {
            ServerProperties.setValue(STEAMQUERY, Short.toString(SteamHandler.steamQueryPort));
        } else {
            SteamHandler.steamQueryPort = ServerProperties.getShort(STEAMQUERY, SteamHandler.steamQueryPort);
        }
        String npcs = props.getProperty(NPCS);
        if (npcs == null) {
            ServerProperties.setValue(NPCS, Boolean.toString(Constants.loadNpcs));
        } else {
            Constants.loadNpcs = ServerProperties.getBoolean(NPCS, Constants.loadNpcs);
        }
        String egi = props.getProperty(ENDGAMEITEMS);
        if (egi == null) {
            ServerProperties.setValue(ENDGAMEITEMS, Boolean.toString(Constants.loadEndGameItems));
        } else {
            Constants.loadEndGameItems = ServerProperties.getBoolean(ENDGAMEITEMS, Constants.loadEndGameItems);
        }
        String spy = props.getProperty(SPY_PREVENTION);
        if (spy == null) {
            ServerProperties.setValue(SPY_PREVENTION, Boolean.toString(Constants.enableSpyPrevention));
        } else {
            Constants.enableSpyPrevention = ServerProperties.getBoolean(SPY_PREVENTION, Constants.enableSpyPrevention);
        }
        String newbie = props.getProperty(NEWBIE_FRIENDLY);
        if (newbie == null) {
            ServerProperties.setValue(NEWBIE_FRIENDLY, Boolean.toString(Constants.isNewbieFriendly));
        } else {
            Constants.isNewbieFriendly = ServerProperties.getBoolean(NEWBIE_FRIENDLY, Constants.isNewbieFriendly);
        }
        String autoNet = props.getProperty(AUTO_NETWORKING);
        if (autoNet == null) {
            ServerProperties.setValue(AUTO_NETWORKING, Boolean.toString(Constants.enableAutoNetworking));
        } else {
            Constants.enableAutoNetworking = ServerProperties.getBoolean(AUTO_NETWORKING, Constants.enableAutoNetworking);
        }
        String pnpPF = props.getProperty(ENABLE_PNP_PORT_FORWARD);
        if (pnpPF == null) {
            ServerProperties.setValue(ENABLE_PNP_PORT_FORWARD, Boolean.toString(Constants.enablePnpPortForward));
        } else {
            Constants.enablePnpPortForward = ServerProperties.getBoolean(ENABLE_PNP_PORT_FORWARD, Constants.enablePnpPortForward);
        }
    }

    private static final void checkIfCreateTable() {
        Connection dbcon = null;
        PreparedStatement ps = null;
        try {
            dbcon = DbConnector.getLoginDbCon();
            ps = dbcon.prepareStatement(createTable);
            ps.execute();
            logger.info("Created properties table in the database");
        }
        catch (SQLException sqex) {
            try {
                logger.log(Level.WARNING, "Failed to create properties table!" + sqex.getMessage(), sqex);
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
    public static final void setValue(String key, String value) {
        Connection dbcon = null;
        PreparedStatement ps = null;
        try {
            dbcon = DbConnector.getLoginDbCon();
            ps = props.containsKey(key) ? dbcon.prepareStatement(update) : dbcon.prepareStatement(insert);
            ps.setString(1, value);
            ps.setString(2, key);
            ps.execute();
        }
        catch (SQLException sqex) {
            try {
                logger.log(Level.WARNING, "Failed to update property " + key + ":" + value + ", " + sqex.getMessage(), sqex);
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
        props.put(key, value);
    }

    public static boolean getBoolean(String key, boolean defaultValue) {
        String maybeBoolean = props.getProperty(key);
        return maybeBoolean == null ? defaultValue : Boolean.parseBoolean(maybeBoolean);
    }

    public static int getInt(String key, int defaultValue) {
        String maybeInt = props.getProperty(key);
        if (maybeInt == null) {
            return defaultValue;
        }
        try {
            return Integer.parseInt(maybeInt);
        }
        catch (NumberFormatException e) {
            return defaultValue;
        }
    }

    public static long getLong(String key, long defaultValue) {
        String maybeLong = props.getProperty(key);
        if (maybeLong == null) {
            return defaultValue;
        }
        try {
            return Long.parseLong(maybeLong);
        }
        catch (NumberFormatException e) {
            return defaultValue;
        }
    }

    public static short getShort(String key, short defaultValue) {
        String maybeShort = props.getProperty(key);
        if (maybeShort == null) {
            return defaultValue;
        }
        try {
            return Short.parseShort(maybeShort);
        }
        catch (NumberFormatException e) {
            return defaultValue;
        }
    }

    public static String getString(String key, String defaultValue) {
        String maybeString = props.getProperty(key);
        return maybeString == null ? defaultValue : maybeString;
    }
}


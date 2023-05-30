package com.wurmonline.server;

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

   public static final void loadProperties() {
      checkIfCreateTable();
      Connection dbcon = null;
      PreparedStatement ps = null;
      ResultSet rs = null;

      try {
         dbcon = DbConnector.getLoginDbCon();
         ps = dbcon.prepareStatement("SELECT * FROM SERVERPROPERTIES");
         rs = ps.executeQuery();

         while(rs.next()) {
            String key = rs.getString("PROPKEY");
            String value = rs.getString("PROPVAL");
            props.put(key, value);
         }
      } catch (SQLException var8) {
         logger.log(Level.WARNING, "Failed to load properties!" + var8.getMessage(), (Throwable)var8);
      } finally {
         DbUtilities.closeDatabaseObjects(ps, rs);
         DbConnector.returnConnection(dbcon);
      }

      checkProperties();
   }

   public static final void checkProperties() {
      String sqp = props.getProperty("STEAMQUERYPORT");
      if (sqp == null) {
         setValue("STEAMQUERYPORT", Short.toString(SteamHandler.steamQueryPort));
      } else {
         SteamHandler.steamQueryPort = getShort("STEAMQUERYPORT", SteamHandler.steamQueryPort);
      }

      String npcs = props.getProperty("NPCS");
      if (npcs == null) {
         setValue("NPCS", Boolean.toString(Constants.loadNpcs));
      } else {
         Constants.loadNpcs = getBoolean("NPCS", Constants.loadNpcs);
      }

      String egi = props.getProperty("ENDGAMEITEMS");
      if (egi == null) {
         setValue("ENDGAMEITEMS", Boolean.toString(Constants.loadEndGameItems));
      } else {
         Constants.loadEndGameItems = getBoolean("ENDGAMEITEMS", Constants.loadEndGameItems);
      }

      String spy = props.getProperty("SPYPREVENTION");
      if (spy == null) {
         setValue("SPYPREVENTION", Boolean.toString(Constants.enableSpyPrevention));
      } else {
         Constants.enableSpyPrevention = getBoolean("SPYPREVENTION", Constants.enableSpyPrevention);
      }

      String newbie = props.getProperty("NEWBIEFRIENDLY");
      if (newbie == null) {
         setValue("NEWBIEFRIENDLY", Boolean.toString(Constants.isNewbieFriendly));
      } else {
         Constants.isNewbieFriendly = getBoolean("NEWBIEFRIENDLY", Constants.isNewbieFriendly);
      }

      String autoNet = props.getProperty("AUTO_NETWORKING");
      if (autoNet == null) {
         setValue("AUTO_NETWORKING", Boolean.toString(Constants.enableAutoNetworking));
      } else {
         Constants.enableAutoNetworking = getBoolean("AUTO_NETWORKING", Constants.enableAutoNetworking);
      }

      String pnpPF = props.getProperty("ENABLE_PNP_PORT_FORWARD");
      if (pnpPF == null) {
         setValue("ENABLE_PNP_PORT_FORWARD", Boolean.toString(Constants.enablePnpPortForward));
      } else {
         Constants.enablePnpPortForward = getBoolean("ENABLE_PNP_PORT_FORWARD", Constants.enablePnpPortForward);
      }
   }

   private static final void checkIfCreateTable() {
      Connection dbcon = null;
      PreparedStatement ps = null;

      try {
         dbcon = DbConnector.getLoginDbCon();
         ps = dbcon.prepareStatement(
            "CREATE TABLE IF NOT EXISTS SERVERPROPERTIES        (            PROPKEY                 VARCHAR(50)   NOT NULL DEFAULT '',            PROPVAL                 VARCHAR(50)   NOT NULL DEFAULT ''        )"
         );
         ps.execute();
         logger.info("Created properties table in the database");
      } catch (SQLException var6) {
         logger.log(Level.WARNING, "Failed to create properties table!" + var6.getMessage(), (Throwable)var6);
      } finally {
         DbUtilities.closeDatabaseObjects(ps, null);
         DbConnector.returnConnection(dbcon);
      }
   }

   public static final void setValue(String key, String value) {
      Connection dbcon = null;
      PreparedStatement ps = null;

      try {
         dbcon = DbConnector.getLoginDbCon();
         if (props.containsKey(key)) {
            ps = dbcon.prepareStatement("UPDATE SERVERPROPERTIES SET PROPVAL=? WHERE PROPKEY=?");
         } else {
            ps = dbcon.prepareStatement("INSERT INTO SERVERPROPERTIES(PROPVAL,PROPKEY) VALUES (?,?)");
         }

         ps.setString(1, value);
         ps.setString(2, key);
         ps.execute();
      } catch (SQLException var8) {
         logger.log(Level.WARNING, "Failed to update property " + key + ":" + value + ", " + var8.getMessage(), (Throwable)var8);
      } finally {
         DbUtilities.closeDatabaseObjects(ps, null);
         DbConnector.returnConnection(dbcon);
      }

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
      } else {
         try {
            return Integer.parseInt(maybeInt);
         } catch (NumberFormatException var4) {
            return defaultValue;
         }
      }
   }

   public static long getLong(String key, long defaultValue) {
      String maybeLong = props.getProperty(key);
      if (maybeLong == null) {
         return defaultValue;
      } else {
         try {
            return Long.parseLong(maybeLong);
         } catch (NumberFormatException var5) {
            return defaultValue;
         }
      }
   }

   public static short getShort(String key, short defaultValue) {
      String maybeShort = props.getProperty(key);
      if (maybeShort == null) {
         return defaultValue;
      } else {
         try {
            return Short.parseShort(maybeShort);
         } catch (NumberFormatException var4) {
            return defaultValue;
         }
      }
   }

   public static String getString(String key, String defaultValue) {
      String maybeString = props.getProperty(key);
      return maybeString == null ? defaultValue : maybeString;
   }
}

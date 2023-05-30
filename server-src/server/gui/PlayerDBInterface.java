package com.wurmonline.server.gui;

import com.wurmonline.server.DbConnector;
import com.wurmonline.server.LoginHandler;
import com.wurmonline.server.utils.DbUtilities;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Level;
import java.util.logging.Logger;

public class PlayerDBInterface {
   private static final String GET_ALL_PLAYERS = "SELECT * FROM PLAYERS";
   private static final String GET_ALL_POSITION = "SELECT * FROM POSITION";
   private static final Logger logger = Logger.getLogger(PlayerDBInterface.class.getName());
   private static final ConcurrentHashMap<String, PlayerData> playerDatas = new ConcurrentHashMap<>();

   public static final void loadAllData() {
      playerDatas.clear();
      Connection dbcon = null;
      PreparedStatement ps = null;
      ResultSet rs = null;

      try {
         dbcon = DbConnector.getPlayerDbCon();
         ps = dbcon.prepareStatement("SELECT * FROM PLAYERS");
         rs = ps.executeQuery();

         while(rs.next()) {
            String name = rs.getString("NAME");
            name = LoginHandler.raiseFirstLetter(name);
            PlayerData pinf = new PlayerData();
            pinf.setName(name);
            pinf.setWurmid(rs.getLong("WURMID"));
            pinf.setPower(rs.getByte("POWER"));
            pinf.setServer(rs.getInt("CURRENTSERVER"));
            pinf.setUndeadType(rs.getByte("UNDEADTYPE"));
            playerDatas.put(name, pinf);
         }
      } catch (SQLException var8) {
         logger.log(Level.WARNING, "Failed to load all player data.", (Throwable)var8);
      } finally {
         DbUtilities.closeDatabaseObjects(ps, rs);
         DbConnector.returnConnection(dbcon);
      }
   }

   public static final PlayerData getFromWurmId(long wurmid) {
      for(PlayerData pd : playerDatas.values()) {
         if (pd.getWurmid() == wurmid) {
            return pd;
         }
      }

      return null;
   }

   public static final void loadAllPositionData() {
      Connection dbcon = null;
      PreparedStatement ps = null;
      ResultSet rs = null;

      try {
         dbcon = DbConnector.getPlayerDbCon();
         ps = dbcon.prepareStatement("SELECT * FROM POSITION");
         rs = ps.executeQuery();

         while(rs.next()) {
            long wurmid = rs.getLong("WURMID");
            PlayerData pd = getFromWurmId(wurmid);
            if (pd != null) {
               pd.setPosx(rs.getFloat("POSX"));
               pd.setPosy(rs.getFloat("POSY"));
            }
         }
      } catch (SQLException var9) {
         logger.log(Level.WARNING, "Failed to load all player data.", (Throwable)var9);
      } finally {
         DbUtilities.closeDatabaseObjects(ps, rs);
         DbConnector.returnConnection(dbcon);
      }
   }

   public static final PlayerData[] getAllData() {
      return playerDatas.values().toArray(new PlayerData[playerDatas.size()]);
   }

   public static final PlayerData getPlayerData(String name) {
      return playerDatas.get(name);
   }
}

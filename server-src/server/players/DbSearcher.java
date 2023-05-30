package com.wurmonline.server.players;

import com.wurmonline.server.DbConnector;
import com.wurmonline.server.NoSuchPlayerException;
import com.wurmonline.server.utils.DbUtilities;
import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public final class DbSearcher {
   private static final String getPlayerName = "select * from PLAYERS where WURMID=?";
   private static final String getPlayerId = "select * from PLAYERS where NAME=?";

   private DbSearcher() {
   }

   public static String getNameForPlayer(long wurmId) throws IOException, NoSuchPlayerException {
      Connection dbcon = null;
      PreparedStatement ps = null;
      ResultSet rs = null;

      String var6;
      try {
         dbcon = DbConnector.getPlayerDbCon();
         ps = dbcon.prepareStatement("select * from PLAYERS where WURMID=?");
         ps.setLong(1, wurmId);
         rs = ps.executeQuery();
         if (!rs.next()) {
            throw new NoSuchPlayerException("No player with id " + wurmId);
         }

         String name = rs.getString("NAME");
         var6 = name;
      } catch (SQLException var10) {
         throw new IOException("Problem finding Player ID " + wurmId, var10);
      } finally {
         DbUtilities.closeDatabaseObjects(ps, rs);
         DbConnector.returnConnection(dbcon);
      }

      return var6;
   }

   public static long getWurmIdForPlayer(String name) throws IOException, NoSuchPlayerException {
      Connection dbcon = null;
      PreparedStatement ps = null;
      ResultSet rs = null;

      long var6;
      try {
         dbcon = DbConnector.getPlayerDbCon();
         ps = dbcon.prepareStatement("select * from PLAYERS where NAME=?");
         ps.setString(1, name);
         rs = ps.executeQuery();
         if (!rs.next()) {
            throw new NoSuchPlayerException("No player with name " + name);
         }

         long id = rs.getLong("WURMID");
         var6 = id;
      } catch (SQLException var11) {
         throw new IOException("Problem finding Player name " + name, var11);
      } finally {
         DbUtilities.closeDatabaseObjects(ps, rs);
         DbConnector.returnConnection(dbcon);
      }

      return var6;
   }
}

package com.wurmonline.server.villages;

import com.wurmonline.server.DbConnector;
import com.wurmonline.server.utils.DbUtilities;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.logging.Level;
import java.util.logging.Logger;

final class DbVillageWar extends VillageWar {
   private static final Logger logger = Logger.getLogger(DbVillageWar.class.getName());
   private static final String createWar = "INSERT INTO VILLAGEWARS (VILLONE, VILLTWO) VALUES (?,?)";
   private static final String deleteWar = "DELETE FROM VILLAGEWARS WHERE VILLONE=? AND VILLTWO=?";

   DbVillageWar(Village vone, Village vtwo) {
      super(vone, vtwo);
   }

   @Override
   void save() {
      Connection dbcon = null;
      PreparedStatement ps = null;

      try {
         dbcon = DbConnector.getZonesDbCon();
         ps = dbcon.prepareStatement("INSERT INTO VILLAGEWARS (VILLONE, VILLTWO) VALUES (?,?)");
         ps.setInt(1, this.villone.getId());
         ps.setInt(2, this.villtwo.getId());
         ps.executeUpdate();
      } catch (SQLException var7) {
         logger.log(Level.WARNING, "Failed to create war between " + this.villone.getName() + " and " + this.villtwo.getName(), (Throwable)var7);
      } finally {
         DbUtilities.closeDatabaseObjects(ps, null);
         DbConnector.returnConnection(dbcon);
      }
   }

   @Override
   void delete() {
      Connection dbcon = null;
      PreparedStatement ps = null;

      try {
         dbcon = DbConnector.getZonesDbCon();
         ps = dbcon.prepareStatement("DELETE FROM VILLAGEWARS WHERE VILLONE=? AND VILLTWO=?");
         ps.setInt(1, this.villone.getId());
         ps.setInt(2, this.villtwo.getId());
         ps.executeUpdate();
      } catch (SQLException var7) {
         logger.log(Level.WARNING, "Failed to delete war between " + this.villone.getName() + " and " + this.villtwo.getName(), (Throwable)var7);
      } finally {
         DbUtilities.closeDatabaseObjects(ps, null);
         DbConnector.returnConnection(dbcon);
      }
   }
}

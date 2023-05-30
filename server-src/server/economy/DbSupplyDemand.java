package com.wurmonline.server.economy;

import com.wurmonline.server.DbConnector;
import com.wurmonline.server.utils.DbUtilities;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.logging.Level;
import java.util.logging.Logger;

final class DbSupplyDemand extends SupplyDemand {
   private static final Logger logger = Logger.getLogger(DbSupplyDemand.class.getName());
   private static final String UPDATE_BOUGHT_ITEMS = "UPDATE SUPPLYDEMAND SET ITEMSBOUGHT=? WHERE ID=?";
   private static final String UPDATE_SOLD_ITEMS = "UPDATE SUPPLYDEMAND SET ITEMSSOLD=? WHERE ID=?";
   private static final String CHECK_SUPLLY_DEMAND = "SELECT ID FROM SUPPLYDEMAND WHERE ID=?";
   private static final String RESET_SUPPLY_DEMAND = "DELETE FROM SUPPLYDEMAND WHERE ID=?";
   private static final String CREATE_SUPPLY_DEMAND = "INSERT INTO SUPPLYDEMAND (ID, ITEMSBOUGHT,ITEMSSOLD, LASTPOLLED) VALUES(?,?,?,?)";

   DbSupplyDemand(int aId, int aItemsBought, int aItemsSold) {
      super(aId, aItemsBought, aItemsSold);
   }

   DbSupplyDemand(int aId, int aItemsBought, int aItemsSold, long aLastPolled) {
      super(aId, aItemsBought, aItemsSold, aLastPolled);
   }

   @Override
   boolean supplyDemandExists() {
      Connection dbcon = null;
      PreparedStatement ps = null;
      ResultSet rs = null;

      try {
         dbcon = DbConnector.getEconomyDbCon();
         ps = dbcon.prepareStatement("SELECT ID FROM SUPPLYDEMAND WHERE ID=?");
         ps.setInt(1, this.id);
         rs = ps.executeQuery();
         return rs.next();
      } catch (SQLException var8) {
         logger.log(Level.WARNING, "Failed to check if supplyDemandExists for ID: " + this.id + " due to " + var8.getMessage(), (Throwable)var8);
      } finally {
         DbUtilities.closeDatabaseObjects(ps, rs);
         DbConnector.returnConnection(dbcon);
      }

      return false;
   }

   @Override
   void updateItemsBoughtByTraders(int items) {
      if (this.supplyDemandExists()) {
         if (this.itemsBought != items) {
            Connection dbcon = null;
            PreparedStatement ps = null;

            try {
               this.itemsBought = items;
               dbcon = DbConnector.getEconomyDbCon();
               ps = dbcon.prepareStatement("UPDATE SUPPLYDEMAND SET ITEMSBOUGHT=? WHERE ID=?");
               ps.setInt(1, this.itemsBought);
               ps.setInt(2, this.id);
               ps.executeUpdate();
            } catch (SQLException var8) {
               logger.log(
                  Level.WARNING, "Failed to update supplyDemand with ID: " + this.id + ", items: " + items + " due to " + var8.getMessage(), (Throwable)var8
               );
            } finally {
               DbUtilities.closeDatabaseObjects(ps, null);
               DbConnector.returnConnection(dbcon);
            }
         }
      } else {
         this.createSupplyDemand(1000 + items, 1000);
      }
   }

   @Override
   void updateItemsSoldByTraders(int items) {
      if (this.supplyDemandExists()) {
         if (this.itemsSold != items) {
            Connection dbcon = null;
            PreparedStatement ps = null;

            try {
               this.itemsSold = items;
               dbcon = DbConnector.getEconomyDbCon();
               ps = dbcon.prepareStatement("UPDATE SUPPLYDEMAND SET ITEMSSOLD=? WHERE ID=?");
               ps.setInt(1, this.itemsSold);
               ps.setInt(2, this.id);
               ps.executeUpdate();
            } catch (SQLException var8) {
               logger.log(
                  Level.WARNING, "Failed to update supplyDemand with ID: " + this.id + ", items: " + items + " due to " + var8.getMessage(), (Throwable)var8
               );
            } finally {
               DbUtilities.closeDatabaseObjects(ps, null);
               DbConnector.returnConnection(dbcon);
            }
         }
      } else {
         this.createSupplyDemand(1000, 1000 + items);
      }
   }

   @Override
   void createSupplyDemand(int aItemsBought, int aItemsSold) {
      Connection dbcon = null;
      PreparedStatement ps = null;

      try {
         this.lastPolled = System.currentTimeMillis();
         dbcon = DbConnector.getEconomyDbCon();
         ps = dbcon.prepareStatement("INSERT INTO SUPPLYDEMAND (ID, ITEMSBOUGHT,ITEMSSOLD, LASTPOLLED) VALUES(?,?,?,?)");
         ps.setInt(1, this.id);
         ps.setInt(2, aItemsBought);
         ps.setInt(3, aItemsSold);
         ps.setLong(4, this.lastPolled);
         ps.executeUpdate();
      } catch (SQLException var9) {
         logger.log(
            Level.WARNING,
            "Failed to create supplyDemand with ID: "
               + this.id
               + ", itemsBought: "
               + aItemsBought
               + ", itemsSold: "
               + aItemsSold
               + " due to "
               + var9.getMessage(),
            (Throwable)var9
         );
      } finally {
         DbUtilities.closeDatabaseObjects(ps, null);
         DbConnector.returnConnection(dbcon);
      }
   }

   @Override
   void reset(long time) {
      Connection dbcon = null;
      PreparedStatement ps = null;

      try {
         this.itemsBought = Math.max(1000, (int)((double)this.itemsBought * 0.99));
         this.itemsSold = Math.max(1000, (int)((double)this.itemsSold * 0.99));
         dbcon = DbConnector.getEconomyDbCon();
         ps = dbcon.prepareStatement("DELETE FROM SUPPLYDEMAND WHERE ID=?");
         ps.setInt(1, this.id);
         ps.executeUpdate();
         this.lastPolled = time;
         DbUtilities.closeDatabaseObjects(ps, null);
         ps = dbcon.prepareStatement("INSERT INTO SUPPLYDEMAND (ID, ITEMSBOUGHT,ITEMSSOLD, LASTPOLLED) VALUES(?,?,?,?)");
         ps.setInt(1, this.id);
         ps.setInt(2, this.itemsBought);
         ps.setInt(3, this.itemsSold);
         ps.setLong(4, this.lastPolled);
         ps.executeUpdate();
      } catch (SQLException var9) {
         logger.log(Level.WARNING, "Failed to reset supplyDemand with ID: " + this.id + ", time: " + time + " due to " + var9.getMessage(), (Throwable)var9);
      } finally {
         DbUtilities.closeDatabaseObjects(ps, null);
         DbConnector.returnConnection(dbcon);
      }
   }
}

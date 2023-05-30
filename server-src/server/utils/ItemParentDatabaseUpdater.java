package com.wurmonline.server.utils;

import com.wurmonline.server.DbConnector;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.logging.Level;
import java.util.logging.Logger;

public class ItemParentDatabaseUpdater extends DatabaseUpdater<ItemParentDatabaseUpdatable> {
   private static final Logger logger = Logger.getLogger(ItemParentDatabaseUpdater.class.getName());

   public ItemParentDatabaseUpdater(String aUpdaterDescription, int aMaxUpdatablesToRemovePerCycle) {
      super(aUpdaterDescription, ItemParentDatabaseUpdatable.class, aMaxUpdatablesToRemovePerCycle);
      logger.info("Creating Item Parent Updater.");
   }

   @Override
   Connection getDatabaseConnection() throws SQLException {
      return DbConnector.getItemDbCon();
   }

   void addUpdatableToBatch(PreparedStatement updateStatement, ItemParentDatabaseUpdatable aDbUpdatable) throws SQLException {
      if (logger.isLoggable(Level.FINEST)) {
         logger.finest("Adding to batch: " + aDbUpdatable);
      }

      updateStatement.setLong(1, aDbUpdatable.getOwner());
      updateStatement.setLong(2, aDbUpdatable.getId());
      updateStatement.addBatch();
   }
}

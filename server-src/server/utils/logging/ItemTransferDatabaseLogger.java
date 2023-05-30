package com.wurmonline.server.utils.logging;

import com.wurmonline.server.Constants;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.logging.Logger;

public final class ItemTransferDatabaseLogger extends DatabaseLogger<ItemTransfer> {
   private static Logger logger = Logger.getLogger(ItemTransferDatabaseLogger.class.getName());

   public ItemTransferDatabaseLogger(String aLoggerDescription, int aMaxLoggablesToRemovePerCycle) {
      super(aLoggerDescription, ItemTransfer.class, aMaxLoggablesToRemovePerCycle);
      logger.info("Creating Item Transfer logger, System useItemTransferLog option: " + Constants.useItemTransferLog);
   }

   void addLoggableToBatch(PreparedStatement logsStatement, ItemTransfer object) throws SQLException {
      logsStatement.setLong(1, object.getItemId());
      logsStatement.setString(2, object.getItemName());
      logsStatement.setLong(3, object.getOldOwnerId());
      logsStatement.setString(4, object.getOldOwnerName());
      logsStatement.setLong(5, object.getNewOwnerId());
      logsStatement.setString(6, object.getNewOwnerName());
      logsStatement.setDate(7, new Date(object.getTransferTime()));
      logsStatement.addBatch();
   }
}

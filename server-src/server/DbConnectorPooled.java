package com.wurmonline.server;

import com.wurmonline.server.database.WurmDatabaseSchema;
import java.sql.Connection;
import java.util.logging.Logger;

public final class DbConnectorPooled extends DbConnector {
   private static final Logger logger = Logger.getLogger(DbConnectorPooled.class.getName());

   private DbConnectorPooled(String driver, String host, String port, WurmDatabaseSchema schema, String user, String password, String loggingName) {
      super(driver, host, port, schema, user, password, loggingName);
   }

   public static void returnConnection(Connection aConnection) {
      logger.warning(
         "The DbConnectorPooled is just a place holder and should not be used yet. Check the wurm.ini. Make sure that USE_POOLED_DB=false is there."
      );
   }
}

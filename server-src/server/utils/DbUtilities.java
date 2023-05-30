package com.wurmonline.server.utils;

import com.wurmonline.server.Constants;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.annotation.Nullable;
import javax.annotation.WillClose;

public final class DbUtilities {
   private static Logger logger = Logger.getLogger(DbUtilities.class.getName());

   private DbUtilities() {
   }

   public static void closeDatabaseObjects(@Nullable @WillClose Statement aStatementToClose, @Nullable @WillClose ResultSet aResultSetToClose) {
      if (aResultSetToClose != null) {
         try {
            aResultSetToClose.close();
         } catch (Exception var4) {
         }

         ResultSet var6 = null;
      }

      if (aStatementToClose != null) {
         try {
            aStatementToClose.close();
         } catch (Exception var3) {
         }

         Statement var5 = null;
      }
   }

   public static void performAdminOnAllTables(Connection aConnection, DbUtilities.DbAdminAction aAction) {
      logger.info("Performing " + aAction + " on all Wurm database tables");
      long start = System.nanoTime();
      ResultSet rsCatalogs = null;
      ResultSet rsTables = null;
      ResultSet rsOperationStatus = null;
      Statement lStmt = null;
      boolean problemsEncountered = false;

      try {
         DatabaseMetaData dbmd = aConnection.getMetaData();
         rsCatalogs = dbmd.getCatalogs();

         while(rsCatalogs.next()) {
            String lCatalogName = rsCatalogs.getString("TABLE_CAT");
            boolean proceed = true;
            if (aAction == DbUtilities.DbAdminAction.CHECK_MEDIUM && lCatalogName.toUpperCase().startsWith("WURMLOGS")) {
               proceed = Constants.checkWurmLogs;
            }

            if (lCatalogName.toUpperCase().startsWith("WURM") && proceed) {
               logger.info("Performing " + aAction + " on CatalogName: " + lCatalogName);
               lStmt = aConnection.createStatement();

               for(rsTables = dbmd.getTables(lCatalogName, null, null, null); rsTables.next(); lStmt.close()) {
                  String lTableName = rsTables.getString("TABLE_NAME");
                  String lAdminQuery = null;
                  switch(aAction) {
                     case ANALYZE:
                        lAdminQuery = "ANALYZE LOCAL TABLE " + lCatalogName + '.' + lTableName;
                        break;
                     case CHECK_CHANGED:
                        lAdminQuery = "CHECK TABLE " + lCatalogName + '.' + lTableName + " CHANGED";
                        break;
                     case CHECK_EXTENDED:
                        lAdminQuery = "CHECK TABLE " + lCatalogName + '.' + lTableName + " EXTENDED";
                        break;
                     case CHECK_FAST:
                        lAdminQuery = "CHECK TABLE " + lCatalogName + '.' + lTableName + " FAST";
                        break;
                     case CHECK_MEDIUM:
                        lAdminQuery = "CHECK TABLE " + lCatalogName + '.' + lTableName + " MEDIUM";
                        break;
                     case CHECK_QUICK:
                        lAdminQuery = "CHECK TABLE " + lCatalogName + '.' + lTableName + " QUICK";
                        break;
                     case OPTIMIZE:
                        lAdminQuery = "OPTIMIZE LOCAL TABLE " + lCatalogName + '.' + lTableName;
                  }

                  lStmt = aConnection.createStatement();
                  if (lStmt.execute(lAdminQuery)) {
                     rsOperationStatus = lStmt.getResultSet();
                     if (rsOperationStatus.next()) {
                        String lMsgType = rsOperationStatus.getString("Msg_type");
                        String lMsgText = rsOperationStatus.getString("Msg_text");
                        if (!"OK".equals(lMsgText) || !"status".equals(lMsgType)) {
                           logger.warning("TableName: " + lAdminQuery + " - " + lMsgType + ": " + lMsgText);
                           if ("status".equals(lMsgType) && lMsgText != null && !lMsgText.contains("is not BASE TABLE")) {
                           }
                        } else if (logger.isLoggable(Level.FINE)) {
                           logger.fine("TableName: " + lAdminQuery + " - OK");
                        }
                     }

                     rsOperationStatus.close();
                  } else if (logger.isLoggable(Level.FINE)) {
                     logger.fine("TableName: " + lAdminQuery);
                  }
               }

               rsTables.close();
            } else if (logger.isLoggable(Level.FINE)) {
               logger.fine("Not performing " + aAction + " on non-Wurm CatalogName: " + lCatalogName);
            }
         }

         rsCatalogs.close();
      } catch (SQLException var20) {
         logger.log(Level.WARNING, var20.getMessage(), (Throwable)var20);
      } finally {
         closeDatabaseObjects(lStmt, rsCatalogs);
         closeDatabaseObjects(null, rsTables);
         closeDatabaseObjects(null, rsOperationStatus);
         float lElapsedTime = (float)(System.nanoTime() - start) / 1000000.0F;
         logger.info("Finished performing " + aAction + " on all database tables, which took " + lElapsedTime + " millis.");
         if (problemsEncountered) {
            logger.severe("\n\n**** At least one problem was encountered while performing admin actions ***********\n\n");
         }
      }
   }

   public static Timestamp getTimestampOrNull(String timestampString) {
      if (timestampString.contains(":")) {
         try {
            return new Timestamp(new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").parse(timestampString).getTime());
         } catch (ParseException var2) {
            logger.warning("Unable to convert '" + timestampString + "' into a timestamp, expected format: yyyy-MM-dd HH:mm:ss");
            return null;
         }
      } else {
         try {
            return new Timestamp(Long.parseLong(timestampString));
         } catch (NumberFormatException var3) {
            logger.warning("Unable to convert '" + timestampString + "' into a timestamp, value is not valid for type 'long'");
            return null;
         }
      }
   }

   public static enum DbAdminAction {
      ANALYZE,
      CHECK_QUICK,
      CHECK_FAST,
      CHECK_CHANGED,
      CHECK_MEDIUM,
      CHECK_EXTENDED,
      OPTIMIZE;
   }
}

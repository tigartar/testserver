package com.wurmonline.server.utils.logging;

import com.wurmonline.server.DbConnector;
import com.wurmonline.server.utils.DbUtilities;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.logging.Level;
import java.util.logging.Logger;

public abstract class DatabaseLogger<T extends WurmLoggable> implements Runnable {
   private static final Logger logger = Logger.getLogger(DatabaseLogger.class.getName());
   private final Queue<T> queue = new ConcurrentLinkedQueue<>();
   private final String iLoggerDescription;
   private final Class<T> iLoggableClass;
   private final int iMaxLoggablesToRemovePerCycle;

   public DatabaseLogger(String aLoggerDescription, Class<T> aLoggableClass, int aMaxLoggablesToRemovePerCycle) {
      this.iLoggerDescription = aLoggerDescription;
      this.iLoggableClass = aLoggableClass;
      this.iMaxLoggablesToRemovePerCycle = aMaxLoggablesToRemovePerCycle;
      logger.info(
         "Creating Database logger "
            + aLoggerDescription
            + " for WurmLoggable type: "
            + aLoggableClass.getName()
            + ", MaxLoggablesToRemovePerCycle: "
            + aMaxLoggablesToRemovePerCycle
      );
   }

   @Override
   public final void run() {
      Connection logsConnection = null;
      PreparedStatement logsStatement = null;

      try {
         if (logger.isLoggable(Level.FINEST)) {
            logger.finest("Starting DatabaseLogger.run() " + this.iLoggerDescription + " for WurmLoggable type: " + this.iLoggableClass.getName());
         }

         if (!this.queue.isEmpty()) {
            int objectsRemoved = 0;
            logsConnection = DbConnector.getLogsDbCon();

            while(!this.queue.isEmpty() && objectsRemoved <= this.iMaxLoggablesToRemovePerCycle) {
               T object = this.queue.remove();
               ++objectsRemoved;
               if (logger.isLoggable(Level.FINEST)) {
                  logger.finest("Removed from FIFO queue: " + object);
               }

               logsStatement = logsConnection.prepareStatement(object.getDatabaseInsertStatement());
               this.addLoggableToBatch(logsStatement, object);
            }

            logsStatement.executeBatch();
            if (logger.isLoggable(Level.FINER) || !this.queue.isEmpty() && logger.isLoggable(Level.FINE)) {
               logger.fine(
                  "Removed "
                     + this.iLoggableClass.getName()
                     + ' '
                     + objectsRemoved
                     + " objects from FIFO queue, which now contains "
                     + this.queue.size()
                     + " objects"
               );
            }
         }
      } catch (SQLException var8) {
         logger.log(Level.WARNING, "Problem getting WurmLogs connection due to " + var8.getMessage(), (Throwable)var8);
      } finally {
         if (logger.isLoggable(Level.FINEST)) {
            logger.finest("Ending DatabaseLogger.run() " + this.iLoggerDescription + " for WurmLoggable type: " + this.iLoggableClass.getName());
         }

         DbUtilities.closeDatabaseObjects(logsStatement, null);
         DbConnector.returnConnection(logsConnection);
      }
   }

   abstract void addLoggableToBatch(PreparedStatement var1, T var2) throws SQLException;

   public final void addToQueue(T loggable) {
      if (loggable != null) {
         if (logger.isLoggable(Level.FINEST)) {
            logger.finest("Adding to database " + this.iLoggerDescription + " loggable queue: " + loggable);
         }

         this.queue.add(loggable);
      }
   }

   int getNumberOfLoggableObjectsInQueue() {
      return this.queue.size();
   }

   final String getLoggerDescription() {
      return this.iLoggerDescription;
   }

   final Class<T> getLoggableClass() {
      return this.iLoggableClass;
   }

   final int getMaxLoggablesToRemovePerCycle() {
      return this.iMaxLoggablesToRemovePerCycle;
   }
}

package com.wurmonline.server.utils;

import com.wurmonline.server.Constants;
import com.wurmonline.server.DbConnector;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.logging.Level;
import java.util.logging.Logger;

public abstract class DatabaseUpdater<T extends WurmDbUpdatable> implements Runnable {
   private static final Logger logger = Logger.getLogger(DatabaseUpdater.class.getName());
   protected final Queue<T> queue = new ConcurrentLinkedQueue<>();
   private final String iUpdaterDescription;
   private final Class<T> iUpdatableClass;
   private final int iMaxUpdatablesToRemovePerCycle;

   public DatabaseUpdater(String aUpdaterDescription, Class<T> aUpdatableClass, int aMaxUpdatablesToRemovePerCycle) {
      this.iUpdaterDescription = aUpdaterDescription;
      this.iUpdatableClass = aUpdatableClass;
      this.iMaxUpdatablesToRemovePerCycle = aMaxUpdatablesToRemovePerCycle;
      logger.info(
         "Creating Database updater "
            + aUpdaterDescription
            + " for WurmDbUpdatable type: "
            + aUpdatableClass.getName()
            + ", MaxUpdatablesToRemovePerCycle: "
            + aMaxUpdatablesToRemovePerCycle
      );
   }

   @Override
   public final void run() {
      Connection updaterConnection = null;
      PreparedStatement updaterStatement = null;

      try {
         if (logger.isLoggable(Level.FINEST)) {
            logger.finest("Starting DatabaseUpdater.run() " + this.iUpdaterDescription + " for WurmDbUpdatable type: " + this.iUpdatableClass.getName());
         }

         if (!this.queue.isEmpty()) {
            long start = System.nanoTime();
            int objectsRemoved = 0;
            updaterConnection = this.getDatabaseConnection();

            T object;
            for(updaterStatement = null;
               !this.queue.isEmpty() && objectsRemoved <= this.iMaxUpdatablesToRemovePerCycle;
               this.addUpdatableToBatch(updaterStatement, object)
            ) {
               object = this.queue.remove();
               ++objectsRemoved;
               if (updaterStatement == null) {
                  updaterStatement = updaterConnection.prepareStatement(object.getDatabaseUpdateStatement());
               }
            }

            if (updaterStatement != null) {
               updaterStatement.executeBatch();
            }

            float lElapsedTime = (float)(System.nanoTime() - start) / 1000000.0F;
            if (logger.isLoggable(Level.FINER)
               || this.queue.size() > this.iMaxUpdatablesToRemovePerCycle && logger.isLoggable(Level.FINE)
               || lElapsedTime > (float)Constants.lagThreshold) {
               logger.fine(
                  "Removed "
                     + this.iUpdatableClass.getName()
                     + ' '
                     + objectsRemoved
                     + " objects from FIFO queue, which now contains "
                     + this.queue.size()
                     + " objects and took "
                     + lElapsedTime
                     + " millis."
               );
            }
         }
      } catch (SQLException var10) {
         logger.log(Level.INFO, "Error in DatabaseUpdater.run() " + this.iUpdaterDescription + " for WurmDbUpdatable type: " + this.iUpdatableClass.getName());
         logger.log(Level.WARNING, "Problem getting WurmLogs connection due to " + var10.getMessage(), (Throwable)var10);
      } finally {
         if (logger.isLoggable(Level.FINEST)) {
            logger.finest("Ending DatabaseUpdater.run() " + this.iUpdaterDescription + " for WurmDbUpdatable type: " + this.iUpdatableClass.getName());
         }

         DbUtilities.closeDatabaseObjects(updaterStatement, null);
         DbConnector.returnConnection(updaterConnection);
      }
   }

   public final void saveImmediately() {
      Connection updaterConnection = null;
      PreparedStatement updaterStatement = null;

      try {
         if (logger.isLoggable(Level.FINEST)) {
            logger.finest("Starting DatabaseUpdater.run() " + this.iUpdaterDescription + " for WurmDbUpdatable type: " + this.iUpdatableClass.getName());
         }

         if (!this.queue.isEmpty()) {
            long start = System.nanoTime();
            int objectsRemoved = 0;
            updaterConnection = this.getDatabaseConnection();

            T object;
            for(updaterStatement = null; !this.queue.isEmpty(); this.addUpdatableToBatch(updaterStatement, object)) {
               object = this.queue.remove();
               ++objectsRemoved;
               if (updaterStatement == null) {
                  updaterStatement = updaterConnection.prepareStatement(object.getDatabaseUpdateStatement());
               }
            }

            if (updaterStatement != null) {
               updaterStatement.executeBatch();
            }

            float lElapsedTime = (float)(System.nanoTime() - start) / 1000000.0F;
            if (logger.isLoggable(Level.FINER)
               || this.queue.size() > this.iMaxUpdatablesToRemovePerCycle && logger.isLoggable(Level.FINE)
               || lElapsedTime > (float)Constants.lagThreshold) {
               logger.fine(
                  "Removed "
                     + this.iUpdatableClass.getName()
                     + ' '
                     + objectsRemoved
                     + " objects from FIFO queue, which now contains "
                     + this.queue.size()
                     + " objects and took "
                     + lElapsedTime
                     + " millis."
               );
            }
         }
      } catch (SQLException var10) {
         logger.log(Level.WARNING, "Problem getting WurmLogs connection due to " + var10.getMessage(), (Throwable)var10);
      } finally {
         if (logger.isLoggable(Level.FINEST)) {
            logger.finest("Ending DatabaseUpdater.run() " + this.iUpdaterDescription + " for WurmDbUpdatable type: " + this.iUpdatableClass.getName());
         }

         DbUtilities.closeDatabaseObjects(updaterStatement, null);
         DbConnector.returnConnection(updaterConnection);
      }
   }

   abstract Connection getDatabaseConnection() throws SQLException;

   abstract void addUpdatableToBatch(PreparedStatement var1, T var2) throws SQLException;

   public void addToQueue(T updatable) {
      if (updatable != null) {
         if (logger.isLoggable(Level.FINEST)) {
            logger.finest("Adding to database " + this.iUpdaterDescription + " updatable queue: " + updatable);
         }

         this.queue.add(updatable);
      }
   }

   final int getNumberOfUpdatableObjectsInQueue() {
      return this.queue.size();
   }

   final String getUpdaterDescription() {
      return this.iUpdaterDescription;
   }

   final Class<T> getUpdatableClass() {
      return this.iUpdatableClass;
   }

   final int getMaxUpdatablesToRemovePerCycle() {
      return this.iMaxUpdatablesToRemovePerCycle;
   }
}

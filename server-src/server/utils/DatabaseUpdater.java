/*
 * Decompiled with CFR 0.152.
 */
package com.wurmonline.server.utils;

import com.wurmonline.server.Constants;
import com.wurmonline.server.DbConnector;
import com.wurmonline.server.utils.DbUtilities;
import com.wurmonline.server.utils.WurmDbUpdatable;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.logging.Level;
import java.util.logging.Logger;

public abstract class DatabaseUpdater<T extends WurmDbUpdatable>
implements Runnable {
    private static final Logger logger = Logger.getLogger(DatabaseUpdater.class.getName());
    protected final Queue<T> queue = new ConcurrentLinkedQueue<T>();
    private final String iUpdaterDescription;
    private final Class<T> iUpdatableClass;
    private final int iMaxUpdatablesToRemovePerCycle;

    public DatabaseUpdater(String aUpdaterDescription, Class<T> aUpdatableClass, int aMaxUpdatablesToRemovePerCycle) {
        this.iUpdaterDescription = aUpdaterDescription;
        this.iUpdatableClass = aUpdatableClass;
        this.iMaxUpdatablesToRemovePerCycle = aMaxUpdatablesToRemovePerCycle;
        logger.info("Creating Database updater " + aUpdaterDescription + " for WurmDbUpdatable type: " + aUpdatableClass.getName() + ", MaxUpdatablesToRemovePerCycle: " + aMaxUpdatablesToRemovePerCycle);
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    @Override
    public final void run() {
        Statement updaterStatement;
        Connection updaterConnection;
        block11: {
            updaterConnection = null;
            updaterStatement = null;
            try {
                if (logger.isLoggable(Level.FINEST)) {
                    logger.finest("Starting DatabaseUpdater.run() " + this.iUpdaterDescription + " for WurmDbUpdatable type: " + this.iUpdatableClass.getName());
                }
                if (!this.queue.isEmpty()) {
                    long start = System.nanoTime();
                    int objectsRemoved = 0;
                    updaterConnection = this.getDatabaseConnection();
                    updaterStatement = null;
                    while (!this.queue.isEmpty() && objectsRemoved <= this.iMaxUpdatablesToRemovePerCycle) {
                        WurmDbUpdatable object = (WurmDbUpdatable)this.queue.remove();
                        ++objectsRemoved;
                        if (updaterStatement == null) {
                            updaterStatement = updaterConnection.prepareStatement(object.getDatabaseUpdateStatement());
                        }
                        this.addUpdatableToBatch((PreparedStatement)updaterStatement, object);
                    }
                    if (updaterStatement != null) {
                        updaterStatement.executeBatch();
                    }
                    float lElapsedTime = (float)(System.nanoTime() - start) / 1000000.0f;
                    if (logger.isLoggable(Level.FINER) || this.queue.size() > this.iMaxUpdatablesToRemovePerCycle && logger.isLoggable(Level.FINE) || lElapsedTime > (float)Constants.lagThreshold) {
                        logger.fine("Removed " + this.iUpdatableClass.getName() + ' ' + objectsRemoved + " objects from FIFO queue, which now contains " + this.queue.size() + " objects and took " + lElapsedTime + " millis.");
                    }
                }
                if (!logger.isLoggable(Level.FINEST)) break block11;
                logger.finest("Ending DatabaseUpdater.run() " + this.iUpdaterDescription + " for WurmDbUpdatable type: " + this.iUpdatableClass.getName());
            }
            catch (SQLException e) {
                block12: {
                    try {
                        logger.log(Level.INFO, "Error in DatabaseUpdater.run() " + this.iUpdaterDescription + " for WurmDbUpdatable type: " + this.iUpdatableClass.getName());
                        logger.log(Level.WARNING, "Problem getting WurmLogs connection due to " + e.getMessage(), e);
                        if (!logger.isLoggable(Level.FINEST)) break block12;
                        logger.finest("Ending DatabaseUpdater.run() " + this.iUpdaterDescription + " for WurmDbUpdatable type: " + this.iUpdatableClass.getName());
                    }
                    catch (Throwable throwable) {
                        if (logger.isLoggable(Level.FINEST)) {
                            logger.finest("Ending DatabaseUpdater.run() " + this.iUpdaterDescription + " for WurmDbUpdatable type: " + this.iUpdatableClass.getName());
                        }
                        DbUtilities.closeDatabaseObjects(updaterStatement, null);
                        DbConnector.returnConnection(updaterConnection);
                        throw throwable;
                    }
                }
                DbUtilities.closeDatabaseObjects(updaterStatement, null);
                DbConnector.returnConnection(updaterConnection);
            }
        }
        DbUtilities.closeDatabaseObjects(updaterStatement, null);
        DbConnector.returnConnection(updaterConnection);
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public final void saveImmediately() {
        Statement updaterStatement;
        Connection updaterConnection;
        block11: {
            updaterConnection = null;
            updaterStatement = null;
            try {
                if (logger.isLoggable(Level.FINEST)) {
                    logger.finest("Starting DatabaseUpdater.run() " + this.iUpdaterDescription + " for WurmDbUpdatable type: " + this.iUpdatableClass.getName());
                }
                if (!this.queue.isEmpty()) {
                    long start = System.nanoTime();
                    int objectsRemoved = 0;
                    updaterConnection = this.getDatabaseConnection();
                    updaterStatement = null;
                    while (!this.queue.isEmpty()) {
                        WurmDbUpdatable object = (WurmDbUpdatable)this.queue.remove();
                        ++objectsRemoved;
                        if (updaterStatement == null) {
                            updaterStatement = updaterConnection.prepareStatement(object.getDatabaseUpdateStatement());
                        }
                        this.addUpdatableToBatch((PreparedStatement)updaterStatement, object);
                    }
                    if (updaterStatement != null) {
                        updaterStatement.executeBatch();
                    }
                    float lElapsedTime = (float)(System.nanoTime() - start) / 1000000.0f;
                    if (logger.isLoggable(Level.FINER) || this.queue.size() > this.iMaxUpdatablesToRemovePerCycle && logger.isLoggable(Level.FINE) || lElapsedTime > (float)Constants.lagThreshold) {
                        logger.fine("Removed " + this.iUpdatableClass.getName() + ' ' + objectsRemoved + " objects from FIFO queue, which now contains " + this.queue.size() + " objects and took " + lElapsedTime + " millis.");
                    }
                }
                if (!logger.isLoggable(Level.FINEST)) break block11;
                logger.finest("Ending DatabaseUpdater.run() " + this.iUpdaterDescription + " for WurmDbUpdatable type: " + this.iUpdatableClass.getName());
            }
            catch (SQLException e) {
                block12: {
                    try {
                        logger.log(Level.WARNING, "Problem getting WurmLogs connection due to " + e.getMessage(), e);
                        if (!logger.isLoggable(Level.FINEST)) break block12;
                        logger.finest("Ending DatabaseUpdater.run() " + this.iUpdaterDescription + " for WurmDbUpdatable type: " + this.iUpdatableClass.getName());
                    }
                    catch (Throwable throwable) {
                        if (logger.isLoggable(Level.FINEST)) {
                            logger.finest("Ending DatabaseUpdater.run() " + this.iUpdaterDescription + " for WurmDbUpdatable type: " + this.iUpdatableClass.getName());
                        }
                        DbUtilities.closeDatabaseObjects(updaterStatement, null);
                        DbConnector.returnConnection(updaterConnection);
                        throw throwable;
                    }
                }
                DbUtilities.closeDatabaseObjects(updaterStatement, null);
                DbConnector.returnConnection(updaterConnection);
            }
        }
        DbUtilities.closeDatabaseObjects(updaterStatement, null);
        DbConnector.returnConnection(updaterConnection);
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


/*
 * Decompiled with CFR 0.152.
 */
package com.wurmonline.server.utils;

import com.wurmonline.server.Constants;
import com.wurmonline.server.DbConnector;
import com.wurmonline.server.database.WurmDatabaseSchema;
import com.wurmonline.server.utils.DbUtilities;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.logging.Level;
import java.util.logging.Logger;

public final class DbIndexManager {
    private static final Logger logger = Logger.getLogger(DbIndexManager.class.getName());

    private DbIndexManager() {
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    private static void createIndex(WurmDatabaseSchema aSchema, String aIndexCreationQuery) {
        block8: {
            block9: {
                if (aIndexCreationQuery == null || !aIndexCreationQuery.startsWith("ALTER TABLE")) break block9;
                long start = System.nanoTime();
                if (logger.isLoggable(Level.FINE)) {
                    logger.fine("Going to create an index in schema: " + (Object)((Object)aSchema) + " using: " + aIndexCreationQuery);
                }
                Connection lDbConnection = null;
                Statement lCreateIndexStatement = null;
                try {
                    lDbConnection = DbConnector.getConnectionForSchema(aSchema);
                    lCreateIndexStatement = lDbConnection.createStatement();
                    lCreateIndexStatement.execute(aIndexCreationQuery);
                }
                catch (SQLException sqx) {
                    try {
                        logger.log(Level.WARNING, "Problems creating an index in schema: " + (Object)((Object)aSchema) + " using: " + aIndexCreationQuery + " due to " + sqx.getMessage(), sqx);
                    }
                    catch (Throwable throwable) {
                        DbUtilities.closeDatabaseObjects(lCreateIndexStatement, null);
                        DbConnector.returnConnection(lDbConnection);
                        if (logger.isLoggable(Level.FINE)) {
                            float lElapsedTime = (float)(System.nanoTime() - start) / 1000000.0f;
                            logger.fine("Creating an index in schema: " + (Object)((Object)aSchema) + " using: " + aIndexCreationQuery + " took " + lElapsedTime + " millis.");
                        }
                        throw throwable;
                    }
                    DbUtilities.closeDatabaseObjects(lCreateIndexStatement, null);
                    DbConnector.returnConnection(lDbConnection);
                    if (logger.isLoggable(Level.FINE)) {
                        float lElapsedTime = (float)(System.nanoTime() - start) / 1000000.0f;
                        logger.fine("Creating an index in schema: " + (Object)((Object)aSchema) + " using: " + aIndexCreationQuery + " took " + lElapsedTime + " millis.");
                    }
                    break block8;
                }
                DbUtilities.closeDatabaseObjects(lCreateIndexStatement, null);
                DbConnector.returnConnection(lDbConnection);
                if (logger.isLoggable(Level.FINE)) {
                    float lElapsedTime = (float)(System.nanoTime() - start) / 1000000.0f;
                    logger.fine("Creating an index in schema: " + (Object)((Object)aSchema) + " using: " + aIndexCreationQuery + " took " + lElapsedTime + " millis.");
                }
                break block8;
            }
            logger.warning("SQL query must start with ALTER TABLE. Schema: " + (Object)((Object)aSchema) + ", SQL: " + aIndexCreationQuery);
        }
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    private static void removeIndex(WurmDatabaseSchema aSchema, String aIndexCreationQuery) {
        block8: {
            block9: {
                if (aIndexCreationQuery == null || !aIndexCreationQuery.startsWith("ALTER TABLE")) break block9;
                long start = System.nanoTime();
                if (logger.isLoggable(Level.FINE)) {
                    logger.fine("Going to drop an index in schema: " + (Object)((Object)aSchema) + " using: " + aIndexCreationQuery);
                }
                Connection lDbConnection = null;
                Statement lCreateIndexStatement = null;
                try {
                    lDbConnection = DbConnector.getConnectionForSchema(aSchema);
                    lCreateIndexStatement = lDbConnection.createStatement();
                    lCreateIndexStatement.execute(aIndexCreationQuery);
                }
                catch (SQLException sqx) {
                    try {
                        logger.log(Level.WARNING, "Problems dropping an index in schema: " + (Object)((Object)aSchema) + " using: " + aIndexCreationQuery + " due to " + sqx.getMessage(), sqx);
                    }
                    catch (Throwable throwable) {
                        DbUtilities.closeDatabaseObjects(lCreateIndexStatement, null);
                        DbConnector.returnConnection(lDbConnection);
                        if (logger.isLoggable(Level.FINE)) {
                            float lElapsedTime = (float)(System.nanoTime() - start) / 1000000.0f;
                            logger.fine("Dropping an index in schema: " + (Object)((Object)aSchema) + " using: " + aIndexCreationQuery + " took " + lElapsedTime + " millis.");
                        }
                        throw throwable;
                    }
                    DbUtilities.closeDatabaseObjects(lCreateIndexStatement, null);
                    DbConnector.returnConnection(lDbConnection);
                    if (logger.isLoggable(Level.FINE)) {
                        float lElapsedTime = (float)(System.nanoTime() - start) / 1000000.0f;
                        logger.fine("Dropping an index in schema: " + (Object)((Object)aSchema) + " using: " + aIndexCreationQuery + " took " + lElapsedTime + " millis.");
                    }
                    break block8;
                }
                DbUtilities.closeDatabaseObjects(lCreateIndexStatement, null);
                DbConnector.returnConnection(lDbConnection);
                if (logger.isLoggable(Level.FINE)) {
                    float lElapsedTime = (float)(System.nanoTime() - start) / 1000000.0f;
                    logger.fine("Dropping an index in schema: " + (Object)((Object)aSchema) + " using: " + aIndexCreationQuery + " took " + lElapsedTime + " millis.");
                }
                break block8;
            }
            logger.warning("SQL query must start with ALTER TABLE. Schema: " + (Object)((Object)aSchema) + ", SQL: " + aIndexCreationQuery);
        }
    }

    public static void createIndexes() {
        if (DbConnector.isUseSqlite()) {
            return;
        }
        logger.info("Starting to create database indices");
        long start = System.nanoTime();
        if (Constants.checkAllDbTables) {
            logger.info("The database tables have already been checked so no need to repair them before creating indices.");
        } else {
            DbIndexManager.repairDatabaseTables();
        }
        DbIndexManager.createIndex(WurmDatabaseSchema.CREATURES, "ALTER TABLE SKILLS ADD INDEX OWNERID (OWNER)");
        DbIndexManager.createIndex(WurmDatabaseSchema.ITEMS, "ALTER TABLE BODYPARTS ADD INDEX BODYZONEID (ZONEID)");
        DbIndexManager.createIndex(WurmDatabaseSchema.ITEMS, "ALTER TABLE COINS ADD INDEX COINSZONEID (ZONEID)");
        DbIndexManager.createIndex(WurmDatabaseSchema.ITEMS, "ALTER TABLE EFFECTS ADD INDEX OWNERID (OWNER)");
        DbIndexManager.createIndex(WurmDatabaseSchema.ITEMS, "ALTER TABLE FROZENITEMS ADD INDEX FROZENITEMS_TEMPLATEID (TEMPLATEID)");
        DbIndexManager.createIndex(WurmDatabaseSchema.ITEMS, "ALTER TABLE ITEMS ADD INDEX ITEMS_TEMPLATEID (TEMPLATEID)");
        DbIndexManager.createIndex(WurmDatabaseSchema.ITEMS, "ALTER TABLE ITEMS ADD INDEX ITEMZONEID (ZONEID)");
        DbIndexManager.createIndex(WurmDatabaseSchema.ZONES, "ALTER TABLE FENCES ADD INDEX FENCEZONEID (ZONEID)");
        DbIndexManager.createIndex(WurmDatabaseSchema.ZONES, "ALTER TABLE WALLS ADD INDEX WALLSSTRUCTUREID (STRUCTURE)");
        float lElapsedTime = (float)(System.nanoTime() - start) / 1000000.0f;
        logger.info("Created database indices took " + lElapsedTime + " millis.");
    }

    public static void removeIndexes() {
        if (DbConnector.isUseSqlite()) {
            return;
        }
        logger.info("Starting to remove database indices");
        long start = System.nanoTime();
        DbIndexManager.removeIndex(WurmDatabaseSchema.CREATURES, "ALTER TABLE SKILLS DROP INDEX OWNERID");
        DbIndexManager.removeIndex(WurmDatabaseSchema.ITEMS, "ALTER TABLE BODYPARTS DROP INDEX BODYZONEID");
        DbIndexManager.removeIndex(WurmDatabaseSchema.ITEMS, "ALTER TABLE COINS DROP INDEX COINSZONEID");
        DbIndexManager.removeIndex(WurmDatabaseSchema.ITEMS, "ALTER TABLE EFFECTS DROP INDEX OWNERID");
        DbIndexManager.removeIndex(WurmDatabaseSchema.ITEMS, "ALTER TABLE FROZENITEMS DROP INDEX FROZENITEMS_TEMPLATEID");
        DbIndexManager.removeIndex(WurmDatabaseSchema.ITEMS, "ALTER TABLE ITEMS DROP INDEX ITEMS_TEMPLATEID");
        DbIndexManager.removeIndex(WurmDatabaseSchema.ITEMS, "ALTER TABLE ITEMS DROP INDEX ITEMZONEID");
        DbIndexManager.removeIndex(WurmDatabaseSchema.ZONES, "ALTER TABLE FENCES DROP INDEX FENCEZONEID");
        DbIndexManager.removeIndex(WurmDatabaseSchema.ZONES, "ALTER TABLE WALLS DROP INDEX WALLSSTRUCTUREID");
        float lElapsedTime = (float)(System.nanoTime() - start) / 1000000.0f;
        logger.info("Removed database indices took " + lElapsedTime + " millis.");
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    private static void repairDatabaseTables() {
        if (DbConnector.isUseSqlite()) {
            return;
        }
        Connection dbcon = null;
        Statement stmt = null;
        try {
            if (logger.isLoggable(Level.FINE)) {
                logger.fine("Checking and, if necessary, repairing Items database table");
            }
            dbcon = DbConnector.getItemDbCon();
            stmt = dbcon.createStatement();
            stmt.execute("REPAIR TABLE ITEMS");
        }
        catch (SQLException sqx) {
            try {
                logger.log(Level.WARNING, sqx.getMessage(), sqx);
            }
            catch (Throwable throwable) {
                DbUtilities.closeDatabaseObjects(stmt, null);
                DbConnector.returnConnection(dbcon);
                throw throwable;
            }
            DbUtilities.closeDatabaseObjects(stmt, null);
            DbConnector.returnConnection(dbcon);
        }
        DbUtilities.closeDatabaseObjects(stmt, null);
        DbConnector.returnConnection(dbcon);
        try {
            if (logger.isLoggable(Level.FINE)) {
                logger.fine("Checking and, if necessary, repairing Coins database table");
            }
            dbcon = DbConnector.getItemDbCon();
            stmt = dbcon.createStatement();
            stmt.execute("REPAIR TABLE COINS");
        }
        catch (SQLException sqx) {
            logger.log(Level.WARNING, sqx.getMessage(), sqx);
        }
        finally {
            DbUtilities.closeDatabaseObjects(stmt, null);
            DbConnector.returnConnection(dbcon);
        }
        try {
            if (logger.isLoggable(Level.FINE)) {
                logger.fine("Checking and, if necessary, repairing Bodyparts database table");
            }
            dbcon = DbConnector.getItemDbCon();
            stmt = dbcon.createStatement();
            stmt.execute("REPAIR TABLE BODYPARTS");
        }
        catch (SQLException sqx) {
            logger.log(Level.WARNING, sqx.getMessage(), sqx);
        }
        finally {
            DbUtilities.closeDatabaseObjects(stmt, null);
            DbConnector.returnConnection(dbcon);
        }
        try {
            if (logger.isLoggable(Level.FINE)) {
                logger.fine("Checking and, if necessary, repairing Walls database table");
            }
            dbcon = DbConnector.getZonesDbCon();
            stmt = dbcon.createStatement();
            stmt.execute("REPAIR TABLE WALLS");
        }
        catch (SQLException sqx) {
            logger.log(Level.WARNING, sqx.getMessage(), sqx);
        }
        finally {
            DbUtilities.closeDatabaseObjects(stmt, null);
            DbConnector.returnConnection(dbcon);
        }
        try {
            if (logger.isLoggable(Level.FINE)) {
                logger.fine("Checking and, if necessary, repairing Fences database table");
            }
            dbcon = DbConnector.getZonesDbCon();
            stmt = dbcon.createStatement();
            stmt.execute("REPAIR TABLE FENCES");
        }
        catch (SQLException sqx) {
            logger.log(Level.WARNING, sqx.getMessage(), sqx);
        }
        finally {
            DbUtilities.closeDatabaseObjects(stmt, null);
            DbConnector.returnConnection(dbcon);
        }
        try {
            if (logger.isLoggable(Level.FINE)) {
                logger.fine("Checking and, if necessary, repairing Players database table");
            }
            dbcon = DbConnector.getPlayerDbCon();
            stmt = dbcon.createStatement();
            stmt.execute("REPAIR TABLE PLAYERS");
        }
        catch (SQLException sqx) {
            logger.log(Level.WARNING, sqx.getMessage(), sqx);
        }
        finally {
            DbUtilities.closeDatabaseObjects(stmt, null);
            DbConnector.returnConnection(dbcon);
        }
        try {
            if (logger.isLoggable(Level.FINE)) {
                logger.fine("Checking and, if necessary, repairing Skills database table");
            }
            dbcon = DbConnector.getPlayerDbCon();
            stmt = dbcon.createStatement();
            stmt.execute("REPAIR TABLE SKILLS");
        }
        catch (SQLException sqx) {
            logger.log(Level.WARNING, sqx.getMessage(), sqx);
        }
        finally {
            DbUtilities.closeDatabaseObjects(stmt, null);
            DbConnector.returnConnection(dbcon);
        }
        try {
            if (logger.isLoggable(Level.FINE)) {
                logger.fine("Checking and, if necessary, repairing Creatures database table");
            }
            dbcon = DbConnector.getCreatureDbCon();
            stmt = dbcon.createStatement();
            stmt.execute("REPAIR TABLE CREATURES");
        }
        catch (SQLException sqx) {
            logger.log(Level.WARNING, sqx.getMessage(), sqx);
        }
        finally {
            DbUtilities.closeDatabaseObjects(stmt, null);
            DbConnector.returnConnection(dbcon);
        }
        try {
            if (logger.isLoggable(Level.FINE)) {
                logger.fine("Checking and, if necessary, repairing Effects database table");
            }
            dbcon = DbConnector.getCreatureDbCon();
            stmt = dbcon.createStatement();
            stmt.execute("REPAIR TABLE EFFECTS");
        }
        catch (SQLException sqx) {
            logger.log(Level.WARNING, sqx.getMessage(), sqx);
        }
        finally {
            DbUtilities.closeDatabaseObjects(stmt, null);
            DbConnector.returnConnection(dbcon);
        }
    }
}


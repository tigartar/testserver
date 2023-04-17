/*
 * Decompiled with CFR 0.152.
 */
package com.wurmonline.server;

import com.wurmonline.server.Constants;
import com.wurmonline.server.TimeConstants;
import com.wurmonline.server.creatures.CreaturePos;
import com.wurmonline.server.database.ConnectionFactory;
import com.wurmonline.server.database.MysqlConnectionFactory;
import com.wurmonline.server.database.SqliteConnectionFactory;
import com.wurmonline.server.database.WurmDatabaseSchema;
import com.wurmonline.server.database.migrations.MigrationResult;
import com.wurmonline.server.database.migrations.MigrationStrategy;
import com.wurmonline.server.database.migrations.MysqlMigrationStrategy;
import com.wurmonline.server.database.migrations.SqliteMigrationStrategy;
import com.wurmonline.server.gui.folders.DistEntity;
import com.wurmonline.server.gui.folders.Folders;
import com.wurmonline.server.items.DbItem;
import java.nio.file.Path;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.EnumMap;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import org.sqlite.SQLiteConfig;

public class DbConnector
implements TimeConstants {
    private static final Logger logger = Logger.getLogger(DbConnector.class.getName());
    private static boolean sqlite = true;
    private static boolean isInitialized = false;
    private static final String SQLITE_JDBC_DRIVER = "org.sqlite.JDBC";
    private static EnumMap<WurmDatabaseSchema, DbConnector> CONNECTORS = new EnumMap(WurmDatabaseSchema.class);
    private static boolean isTrackingOpenDatabaseResources = false;
    private static final Path MIGRATIONS_DIR = Folders.getDist().getPathFor(DistEntity.Migrations);
    private Connection connection;
    private long lastUsed = System.currentTimeMillis();
    private final String loggingName;
    private final ConnectionFactory connectionFactory;
    private static final Pattern portPattern = Pattern.compile(":?([0-9]+)");
    private static final SQLiteConfig config = new SQLiteConfig();
    private static MigrationStrategy MIGRATION_STRATEGY;

    public static void initialize() {
        DbConnector.initialize(false);
    }

    public static void initialize(boolean reinitialize) {
        String driver;
        ConfigHelper configHelper;
        if (isInitialized && !reinitialize) {
            return;
        }
        if (DbConnector.isUseSqlite()) {
            configHelper = new SqliteConfigHelper();
            driver = SQLITE_JDBC_DRIVER;
        } else {
            configHelper = new MysqlConfigHelper();
            driver = Constants.dbDriver;
        }
        try {
            Class.forName(driver);
        }
        catch (ClassNotFoundException e) {
            logger.warning("No class found for database driver: " + Constants.dbDriver);
            e.printStackTrace();
        }
        CONNECTORS = configHelper.buildConnectors();
        MIGRATION_STRATEGY = configHelper.newMigrationStrategy();
        DbConnector.setInitialized(true);
    }

    protected DbConnector(String driver, String host, String port, WurmDatabaseSchema schema, String user, String password, String loggingName) {
        if (DbConnector.isUseSqlite()) {
            config.setJournalMode(SQLiteConfig.JournalMode.WAL);
            config.setSynchronous(SQLiteConfig.SynchronousMode.NORMAL);
            this.connectionFactory = new SqliteConnectionFactory(host, schema, config);
        } else {
            this.connectionFactory = new MysqlConnectionFactory(host, DbConnector.asPort(port), user, password, schema);
        }
        this.loggingName = loggingName;
    }

    protected DbConnector(ConnectionFactory connectionFactory, String loggingName) {
        this.connectionFactory = connectionFactory;
        this.loggingName = loggingName;
    }

    public static boolean isUseSqlite() {
        return sqlite;
    }

    protected void beforeStaleClose() throws SQLException {
    }

    private void refreshDbConnection() throws SQLException {
        if (this.connectionFactory.isStale(this.lastUsed, this.connection)) {
            logger.log(Level.INFO, "Recreating " + this.loggingName);
            if (this.connection != null) {
                try {
                    this.beforeStaleClose();
                    this.attemptClose();
                }
                catch (SQLException e) {
                    e.printStackTrace();
                    logger.log(Level.WARNING, "Unable to perform pre-close on stale " + this.loggingName, e);
                }
            }
        }
        if (!this.connectionFactory.isValid(this.connection)) {
            try {
                this.connection = this.connectionFactory.createConnection();
            }
            catch (Exception e) {
                e.printStackTrace();
                logger.log(Level.WARNING, "Problem opening the " + this.loggingName, e);
            }
        }
        this.lastUsed = System.currentTimeMillis();
    }

    public static boolean hasPendingMigrations() {
        return MIGRATION_STRATEGY.hasPendingMigrations();
    }

    public static MigrationResult performMigrations() {
        return MIGRATION_STRATEGY.migrate();
    }

    public static Connection getLoginDbCon() throws SQLException {
        return DbConnector.refreshConnectionForSchema(WurmDatabaseSchema.LOGIN);
    }

    public static Connection getCreatureDbCon() throws SQLException {
        return DbConnector.refreshConnectionForSchema(WurmDatabaseSchema.CREATURES);
    }

    public static Connection getDeityDbCon() throws SQLException {
        return DbConnector.refreshConnectionForSchema(WurmDatabaseSchema.DEITIES);
    }

    public static Connection getEconomyDbCon() throws SQLException {
        return DbConnector.refreshConnectionForSchema(WurmDatabaseSchema.ECONOMY);
    }

    public static Connection getPlayerDbCon() throws SQLException {
        return DbConnector.refreshConnectionForSchema(WurmDatabaseSchema.PLAYERS);
    }

    public static Connection getItemDbCon() throws SQLException {
        return DbConnector.refreshConnectionForSchema(WurmDatabaseSchema.ITEMS);
    }

    public static Connection getTemplateDbCon() throws SQLException {
        return DbConnector.refreshConnectionForSchema(WurmDatabaseSchema.TEMPLATES);
    }

    public static Connection getZonesDbCon() throws SQLException {
        return DbConnector.refreshConnectionForSchema(WurmDatabaseSchema.ZONES);
    }

    public static Connection getLogsDbCon() throws SQLException {
        return DbConnector.refreshConnectionForSchema(WurmDatabaseSchema.LOGS);
    }

    private void attemptClose() {
        if (this.connection != null) {
            try {
                this.connection.close();
            }
            catch (SQLException ex) {
                ex.printStackTrace();
                logger.log(Level.WARNING, "Problem closing the " + this.loggingName, ex);
            }
            this.connection = null;
        }
    }

    public static void closeAll() {
        logger.info("Starting to close all Database Connections.");
        CONNECTORS.values().forEach(DbConnector::attemptClose);
        logger.info("Finished closing all Database Connections.");
    }

    public static void returnConnection(@Nullable Connection aConnection) {
        if (logger.isLoggable(Level.FINEST)) {
            logger.finest("Returning Connection: " + aConnection);
        }
        if (!isTrackingOpenDatabaseResources && aConnection != null) {
            MysqlConnectionFactory.logActiveStatementCount(aConnection);
        }
    }

    private static Connection refreshConnectionForSchema(WurmDatabaseSchema schema) throws SQLException {
        if (!DbConnector.isInitialized()) {
            DbConnector.initialize();
        }
        DbConnector connector = CONNECTORS.get((Object)schema);
        connector.refreshDbConnection();
        return connector.connection;
    }

    public static Connection getConnectionForSchema(@Nonnull WurmDatabaseSchema aSchema) throws SQLException {
        DbConnector connector;
        if (logger.isLoggable(Level.FINER)) {
            logger.finer("Getting database connection for schema: " + (Object)((Object)aSchema));
        }
        if (!DbConnector.isInitialized()) {
            DbConnector.initialize();
        }
        if ((connector = CONNECTORS.get((Object)aSchema)) == null) {
            assert (false) : aSchema;
            logger.warning("Returning null for an unexpected WurmDatabaseSchema: " + (Object)((Object)aSchema));
            return null;
        }
        if (connector.connection == null) {
            logger.warning("Null connection found for connector " + connector.loggingName);
            connector.refreshDbConnection();
        }
        return connector.connection;
    }

    public static void setUseSqlite(boolean sqlite) {
        DbConnector.sqlite = sqlite;
    }

    private static Integer asPort(@Nullable String portProperty) {
        if (portProperty != null && !portProperty.isEmpty()) {
            Matcher m = portPattern.matcher(portProperty);
            if (m.matches()) {
                try {
                    return Integer.parseInt(m.group(1));
                }
                catch (NumberFormatException e) {
                    logger.warning("Unexpected error, could not converted matched port number into integer: " + portProperty);
                }
            } else {
                logger.warning("Database port property does not match expected pattern: " + portProperty);
            }
        }
        return null;
    }

    private static boolean isInitialized() {
        return isInitialized;
    }

    private static void setInitialized(boolean isInitialized) {
        DbConnector.isInitialized = isInitialized;
    }

    private static class MysqlConfigHelper
    extends ConfigHelper<MysqlConnectionFactory> {
        private MysqlConfigHelper() {
        }

        @Override
        protected boolean loadIsTrackingOpenDatabaseResources() {
            return Constants.trackOpenDatabaseResources;
        }

        @Override
        public MysqlConnectionFactory factoryForSchema(WurmDatabaseSchema schema) {
            return new MysqlConnectionFactory(Constants.dbHost, DbConnector.asPort(Constants.dbPort), Constants.dbUser, Constants.dbPass, schema);
        }

        @Override
        public MigrationStrategy newMigrationStrategy() {
            return new MysqlMigrationStrategy((MysqlConnectionFactory)((DbConnector)CONNECTORS.get((Object)MysqlMigrationStrategy.MIGRATION_SCHEMA)).connectionFactory);
        }
    }

    private static class SqliteConfigHelper
    extends ConfigHelper<SqliteConnectionFactory> {
        SqliteConfigHelper() {
            config.setJournalMode(SQLiteConfig.JournalMode.WAL);
            config.setSynchronous(SQLiteConfig.SynchronousMode.NORMAL);
        }

        @Override
        public SqliteConnectionFactory factoryForSchema(WurmDatabaseSchema schema) {
            return new SqliteConnectionFactory(Constants.dbHost, schema, config);
        }

        @Override
        MigrationStrategy newMigrationStrategy() {
            ArrayList<SqliteConnectionFactory> sqliteConnectionFactories = new ArrayList<SqliteConnectionFactory>();
            for (DbConnector connector : CONNECTORS.values()) {
                SqliteConnectionFactory factory = (SqliteConnectionFactory)connector.connectionFactory;
                sqliteConnectionFactories.add(factory);
            }
            return new SqliteMigrationStrategy(sqliteConnectionFactories, MIGRATIONS_DIR);
        }
    }

    private static abstract class ConfigHelper<B extends ConnectionFactory> {
        private ConfigHelper() {
        }

        public abstract B factoryForSchema(WurmDatabaseSchema var1);

        protected boolean loadIsTrackingOpenDatabaseResources() {
            if (Constants.trackOpenDatabaseResources) {
                logger.warning("Cannot set tracking of open database resources as this is not supported for this driver");
            }
            return false;
        }

        EnumMap<WurmDatabaseSchema, DbConnector> buildConnectors() {
            isTrackingOpenDatabaseResources = this.loadIsTrackingOpenDatabaseResources();
            if (Constants.usePooledDb) {
                logger.warning("Database connection pooling is set to true, but is not currently supported");
            }
            EnumMap<WurmDatabaseSchema, B> factories = new EnumMap<WurmDatabaseSchema, B>(WurmDatabaseSchema.class);
            for (WurmDatabaseSchema schema : WurmDatabaseSchema.values()) {
                factories.put(schema, this.factoryForSchema(schema));
            }
            EnumMap<WurmDatabaseSchema, DbConnector> newConnectors = new EnumMap<WurmDatabaseSchema, DbConnector>(WurmDatabaseSchema.class);
            newConnectors.put(WurmDatabaseSchema.PLAYERS, new DbConnector((ConnectionFactory)factories.get((Object)WurmDatabaseSchema.PLAYERS), "playerDbcon"){

                @Override
                protected void beforeStaleClose() throws SQLException {
                    CreaturePos.clearBatches();
                }
            });
            newConnectors.put(WurmDatabaseSchema.CREATURES, new DbConnector((ConnectionFactory)factories.get((Object)WurmDatabaseSchema.CREATURES), "creatureDbcon"){

                @Override
                protected void beforeStaleClose() throws SQLException {
                    CreaturePos.clearBatches();
                }
            });
            newConnectors.put(WurmDatabaseSchema.ITEMS, new DbConnector((ConnectionFactory)factories.get((Object)WurmDatabaseSchema.ITEMS), "itemdbcon"){

                @Override
                protected void beforeStaleClose() throws SQLException {
                    DbItem.clearBatches();
                }
            });
            newConnectors.put(WurmDatabaseSchema.TEMPLATES, new DbConnector((ConnectionFactory)factories.get((Object)WurmDatabaseSchema.TEMPLATES), "templateDbcon"));
            newConnectors.put(WurmDatabaseSchema.ZONES, new DbConnector((ConnectionFactory)factories.get((Object)WurmDatabaseSchema.ZONES), "zonesDbcon"));
            newConnectors.put(WurmDatabaseSchema.ECONOMY, new DbConnector((ConnectionFactory)factories.get((Object)WurmDatabaseSchema.ECONOMY), "economyDbcon"));
            newConnectors.put(WurmDatabaseSchema.DEITIES, new DbConnector((ConnectionFactory)factories.get((Object)WurmDatabaseSchema.DEITIES), "deityDbcon"));
            newConnectors.put(WurmDatabaseSchema.LOGIN, new DbConnector((ConnectionFactory)factories.get((Object)WurmDatabaseSchema.LOGIN), "loginDbcon"));
            newConnectors.put(WurmDatabaseSchema.LOGS, new DbConnector((ConnectionFactory)factories.get((Object)WurmDatabaseSchema.LOGS), "logsDbcon"));
            return newConnectors;
        }

        abstract MigrationStrategy newMigrationStrategy();
    }
}


package com.wurmonline.server;

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
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import org.sqlite.SQLiteConfig;

public class DbConnector implements TimeConstants {
   private static final Logger logger = Logger.getLogger(DbConnector.class.getName());
   private static boolean sqlite = true;
   private static boolean isInitialized = false;
   private static final String SQLITE_JDBC_DRIVER = "org.sqlite.JDBC";
   private static EnumMap<WurmDatabaseSchema, DbConnector> CONNECTORS = new EnumMap<>(WurmDatabaseSchema.class);
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
      initialize(false);
   }

   public static void initialize(boolean reinitialize) {
      if (!isInitialized || reinitialize) {
         DbConnector.ConfigHelper<? extends ConnectionFactory> configHelper;
         String driver;
         if (isUseSqlite()) {
            configHelper = new DbConnector.SqliteConfigHelper();
            driver = "org.sqlite.JDBC";
         } else {
            configHelper = new DbConnector.MysqlConfigHelper();
            driver = Constants.dbDriver;
         }

         try {
            Class.forName(driver);
         } catch (ClassNotFoundException var4) {
            logger.warning("No class found for database driver: " + Constants.dbDriver);
            var4.printStackTrace();
         }

         CONNECTORS = configHelper.buildConnectors();
         MIGRATION_STRATEGY = configHelper.newMigrationStrategy();
         setInitialized(true);
      }
   }

   protected DbConnector(String driver, String host, String port, WurmDatabaseSchema schema, String user, String password, String loggingName) {
      if (isUseSqlite()) {
         config.setJournalMode(SQLiteConfig.JournalMode.WAL);
         config.setSynchronous(SQLiteConfig.SynchronousMode.NORMAL);
         this.connectionFactory = new SqliteConnectionFactory(host, schema, config);
      } else {
         this.connectionFactory = new MysqlConnectionFactory(host, asPort(port), user, password, schema);
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
            } catch (SQLException var3) {
               var3.printStackTrace();
               logger.log(Level.WARNING, "Unable to perform pre-close on stale " + this.loggingName, (Throwable)var3);
            }
         }
      }

      if (!this.connectionFactory.isValid(this.connection)) {
         try {
            this.connection = this.connectionFactory.createConnection();
         } catch (Exception var2) {
            var2.printStackTrace();
            logger.log(Level.WARNING, "Problem opening the " + this.loggingName, (Throwable)var2);
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
      return refreshConnectionForSchema(WurmDatabaseSchema.LOGIN);
   }

   public static Connection getCreatureDbCon() throws SQLException {
      return refreshConnectionForSchema(WurmDatabaseSchema.CREATURES);
   }

   public static Connection getDeityDbCon() throws SQLException {
      return refreshConnectionForSchema(WurmDatabaseSchema.DEITIES);
   }

   public static Connection getEconomyDbCon() throws SQLException {
      return refreshConnectionForSchema(WurmDatabaseSchema.ECONOMY);
   }

   public static Connection getPlayerDbCon() throws SQLException {
      return refreshConnectionForSchema(WurmDatabaseSchema.PLAYERS);
   }

   public static Connection getItemDbCon() throws SQLException {
      return refreshConnectionForSchema(WurmDatabaseSchema.ITEMS);
   }

   public static Connection getTemplateDbCon() throws SQLException {
      return refreshConnectionForSchema(WurmDatabaseSchema.TEMPLATES);
   }

   public static Connection getZonesDbCon() throws SQLException {
      return refreshConnectionForSchema(WurmDatabaseSchema.ZONES);
   }

   public static Connection getLogsDbCon() throws SQLException {
      return refreshConnectionForSchema(WurmDatabaseSchema.LOGS);
   }

   private void attemptClose() {
      if (this.connection != null) {
         try {
            this.connection.close();
         } catch (SQLException var2) {
            var2.printStackTrace();
            logger.log(Level.WARNING, "Problem closing the " + this.loggingName, (Throwable)var2);
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
      if (!isInitialized()) {
         initialize();
      }

      DbConnector connector = CONNECTORS.get(schema);
      connector.refreshDbConnection();
      return connector.connection;
   }

   public static Connection getConnectionForSchema(@Nonnull WurmDatabaseSchema aSchema) throws SQLException {
      if (logger.isLoggable(Level.FINER)) {
         logger.finer("Getting database connection for schema: " + aSchema);
      }

      if (!isInitialized()) {
         initialize();
      }

      DbConnector connector = CONNECTORS.get(aSchema);
      if (connector == null) {
         assert false : aSchema;

         logger.warning("Returning null for an unexpected WurmDatabaseSchema: " + aSchema);
         return null;
      } else {
         if (connector.connection == null) {
            logger.warning("Null connection found for connector " + connector.loggingName);
            connector.refreshDbConnection();
         }

         return connector.connection;
      }
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
            } catch (NumberFormatException var3) {
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

   private abstract static class ConfigHelper<B extends ConnectionFactory> {
      private ConfigHelper() {
      }

      public abstract B factoryForSchema(WurmDatabaseSchema var1);

      protected boolean loadIsTrackingOpenDatabaseResources() {
         if (Constants.trackOpenDatabaseResources) {
            DbConnector.logger.warning("Cannot set tracking of open database resources as this is not supported for this driver");
         }

         return false;
      }

      EnumMap<WurmDatabaseSchema, DbConnector> buildConnectors() {
         DbConnector.isTrackingOpenDatabaseResources = this.loadIsTrackingOpenDatabaseResources();
         if (Constants.usePooledDb) {
            DbConnector.logger.warning("Database connection pooling is set to true, but is not currently supported");
         }

         EnumMap<WurmDatabaseSchema, B> factories = new EnumMap<>(WurmDatabaseSchema.class);

         for(WurmDatabaseSchema schema : WurmDatabaseSchema.values()) {
            factories.put(schema, this.factoryForSchema(schema));
         }

         EnumMap<WurmDatabaseSchema, DbConnector> newConnectors = new EnumMap<>(WurmDatabaseSchema.class);
         newConnectors.put(WurmDatabaseSchema.PLAYERS, new DbConnector(factories.get(WurmDatabaseSchema.PLAYERS), "playerDbcon") {
            @Override
            protected void beforeStaleClose() throws SQLException {
               CreaturePos.clearBatches();
            }
         });
         newConnectors.put(WurmDatabaseSchema.CREATURES, new DbConnector(factories.get(WurmDatabaseSchema.CREATURES), "creatureDbcon") {
            @Override
            protected void beforeStaleClose() throws SQLException {
               CreaturePos.clearBatches();
            }
         });
         newConnectors.put(WurmDatabaseSchema.ITEMS, new DbConnector(factories.get(WurmDatabaseSchema.ITEMS), "itemdbcon") {
            @Override
            protected void beforeStaleClose() throws SQLException {
               DbItem.clearBatches();
            }
         });
         newConnectors.put(WurmDatabaseSchema.TEMPLATES, new DbConnector(factories.get(WurmDatabaseSchema.TEMPLATES), "templateDbcon"));
         newConnectors.put(WurmDatabaseSchema.ZONES, new DbConnector(factories.get(WurmDatabaseSchema.ZONES), "zonesDbcon"));
         newConnectors.put(WurmDatabaseSchema.ECONOMY, new DbConnector(factories.get(WurmDatabaseSchema.ECONOMY), "economyDbcon"));
         newConnectors.put(WurmDatabaseSchema.DEITIES, new DbConnector(factories.get(WurmDatabaseSchema.DEITIES), "deityDbcon"));
         newConnectors.put(WurmDatabaseSchema.LOGIN, new DbConnector(factories.get(WurmDatabaseSchema.LOGIN), "loginDbcon"));
         newConnectors.put(WurmDatabaseSchema.LOGS, new DbConnector(factories.get(WurmDatabaseSchema.LOGS), "logsDbcon"));
         return newConnectors;
      }

      abstract MigrationStrategy newMigrationStrategy();
   }

   private static class MysqlConfigHelper extends DbConnector.ConfigHelper<MysqlConnectionFactory> {
      private MysqlConfigHelper() {
      }

      @Override
      protected boolean loadIsTrackingOpenDatabaseResources() {
         return Constants.trackOpenDatabaseResources;
      }

      public MysqlConnectionFactory factoryForSchema(WurmDatabaseSchema schema) {
         return new MysqlConnectionFactory(Constants.dbHost, DbConnector.asPort(Constants.dbPort), Constants.dbUser, Constants.dbPass, schema);
      }

      @Override
      public MigrationStrategy newMigrationStrategy() {
         return new MysqlMigrationStrategy((MysqlConnectionFactory)DbConnector.CONNECTORS.get(MysqlMigrationStrategy.MIGRATION_SCHEMA).connectionFactory);
      }
   }

   private static class SqliteConfigHelper extends DbConnector.ConfigHelper<SqliteConnectionFactory> {
      SqliteConfigHelper() {
         DbConnector.config.setJournalMode(SQLiteConfig.JournalMode.WAL);
         DbConnector.config.setSynchronous(SQLiteConfig.SynchronousMode.NORMAL);
      }

      public SqliteConnectionFactory factoryForSchema(WurmDatabaseSchema schema) {
         return new SqliteConnectionFactory(Constants.dbHost, schema, DbConnector.config);
      }

      @Override
      MigrationStrategy newMigrationStrategy() {
         List<SqliteConnectionFactory> sqliteConnectionFactories = new ArrayList<>();

         for(DbConnector connector : DbConnector.CONNECTORS.values()) {
            SqliteConnectionFactory factory = (SqliteConnectionFactory)connector.connectionFactory;
            sqliteConnectionFactories.add(factory);
         }

         return new SqliteMigrationStrategy(sqliteConnectionFactories, DbConnector.MIGRATIONS_DIR);
      }
   }
}

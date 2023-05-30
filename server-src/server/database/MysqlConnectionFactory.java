package com.wurmonline.server.database;

import com.mysql.jdbc.ConnectionImpl;
import com.wurmonline.server.Constants;
import com.wurmonline.shared.exceptions.WurmException;
import java.sql.Connection;
import java.sql.Driver;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class MysqlConnectionFactory extends ConnectionFactory {
   private static final Logger logger = Logger.getLogger(MysqlConnectionFactory.class.getName());
   private static final String DB_CACHE_PREP_STMTS = "&cachePrepStmts=true&useServerPrepStmts=true&prepStmtCacheSqlLimit=512&prepStmtCacheSize=100&useCompression=false&allowMultiQueries=true&elideSetAutoCommits=true&maintainTimeStats=false";
   private static final String DB_GATHER_STATS = "&gatherPerfMetrics=true&reportMetricsIntervalMillis=600000&profileSQL=false&useUsageAdvisor=false&useNanosForElapsedTime=false";
   private static final int DATABASE_IS_VALID_TIMEOUT = 0;
   private final String user;
   private final String password;

   public MysqlConnectionFactory(String host, int port, String user, String password, WurmDatabaseSchema schema) {
      super(createConnectionUrl(host, port, schema), schema);
      this.user = user;
      this.password = password;
   }

   private static StringBuilder addOptionalJdbcConnectionProperties(StringBuilder urlBuilder) {
      if (Constants.trackOpenDatabaseResources) {
         urlBuilder.append("dontTrackOpenResources=false");
      } else {
         urlBuilder.append("dontTrackOpenResources=true");
      }

      if (Constants.usePrepStmts) {
         urlBuilder.append(
            "&cachePrepStmts=true&useServerPrepStmts=true&prepStmtCacheSqlLimit=512&prepStmtCacheSize=100&useCompression=false&allowMultiQueries=true&elideSetAutoCommits=true&maintainTimeStats=false"
         );
      }

      if (Constants.gatherDbStats) {
         urlBuilder.append("&gatherPerfMetrics=true&reportMetricsIntervalMillis=600000&profileSQL=false&useUsageAdvisor=false&useNanosForElapsedTime=false");
      }

      return urlBuilder;
   }

   public static String createConnectionUrl(String host, int port, WurmDatabaseSchema schema) {
      StringBuilder urlBuilder = new StringBuilder();
      urlBuilder.append("jdbc:mysql://");
      urlBuilder.append(host);
      urlBuilder.append(":");
      urlBuilder.append(port);
      urlBuilder.append("/");
      urlBuilder.append(schema.getDatabase());
      urlBuilder.append("?");
      return addOptionalJdbcConnectionProperties(urlBuilder).toString();
   }

   @Override
   public Connection createConnection() throws SQLException {
      Driver driver = DriverManager.getDriver(this.getUrl());
      Properties connectionInfo = new Properties();
      connectionInfo.put("user", this.user);
      connectionInfo.put("password", this.password);
      Connection con = driver.connect(this.getUrl(), connectionInfo);
      if (logger.isLoggable(Level.FINE)) {
         logger.fine("JDBC Driver Class: " + driver.getClass() + ", version: " + driver.getMajorVersion() + '.' + driver.getMinorVersion());
      }

      return con;
   }

   @Override
   public boolean isValid(@Nullable Connection con) throws SQLException {
      return con != null && con.isValid(0);
   }

   @Override
   public boolean isStale(long lastUsed, @Nullable Connection connection) throws SQLException {
      return System.currentTimeMillis() - lastUsed > 3600000L || connection != null && !connection.isValid(0);
   }

   public String getUser() {
      return this.user;
   }

   public String getPassword() {
      return this.password;
   }

   public static void logActiveStatementCount(@Nonnull Connection aConnection) {
      if (aConnection instanceof ConnectionImpl) {
         int lActiveStatementCount = ((ConnectionImpl)aConnection).getActiveStatementCount();
         if (lActiveStatementCount > 0) {
            logger.log(
               Level.WARNING,
               "Returned connection: " + aConnection.getClass() + ", active statement count: " + lActiveStatementCount,
               (Throwable)(new WurmException("SQL Statements still open when returning connection"))
            );
         }
      }
   }
}

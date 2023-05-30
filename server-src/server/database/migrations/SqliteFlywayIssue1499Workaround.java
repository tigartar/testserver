package com.wurmonline.server.database.migrations;

import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.SQLFeatureNotSupportedException;
import java.util.logging.Logger;
import javax.sql.DataSource;
import org.sqlite.SQLiteDataSource;

public class SqliteFlywayIssue1499Workaround implements DataSource {
   private final SQLiteDataSource dataSource;
   private Connection connection;

   public SqliteFlywayIssue1499Workaround(SQLiteDataSource dataSource) {
      this.dataSource = dataSource;
   }

   @Override
   public Connection getConnection() throws SQLException {
      if (this.connection == null || this.connection.isClosed()) {
         this.connection = this.dataSource.getConnection();
      }

      return this.connection;
   }

   @Override
   public Connection getConnection(String username, String password) throws SQLException {
      return this.getConnection();
   }

   @Override
   public <T> T unwrap(Class<T> iface) throws SQLException {
      return this.dataSource.unwrap(iface);
   }

   @Override
   public boolean isWrapperFor(Class<?> iface) throws SQLException {
      return SQLiteDataSource.class.equals(iface);
   }

   @Override
   public PrintWriter getLogWriter() throws SQLException {
      return this.dataSource.getLogWriter();
   }

   @Override
   public void setLogWriter(PrintWriter out) throws SQLException {
      this.dataSource.setLogWriter(out);
   }

   @Override
   public void setLoginTimeout(int seconds) throws SQLException {
      this.dataSource.setLoginTimeout(seconds);
   }

   @Override
   public int getLoginTimeout() throws SQLException {
      return this.dataSource.getLoginTimeout();
   }

   @Override
   public Logger getParentLogger() throws SQLFeatureNotSupportedException {
      return this.dataSource.getParentLogger();
   }
}

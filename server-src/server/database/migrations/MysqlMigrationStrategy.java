package com.wurmonline.server.database.migrations;

import com.wurmonline.server.database.MysqlConnectionFactory;
import com.wurmonline.server.database.WurmDatabaseSchema;

public class MysqlMigrationStrategy implements MigrationStrategy {
   public static final WurmDatabaseSchema MIGRATION_SCHEMA = WurmDatabaseSchema.LOGIN;
   private final MysqlMigrator migrator;

   public MysqlMigrationStrategy(MysqlConnectionFactory connectionFactory) {
      this.migrator = new MysqlMigrator(MIGRATION_SCHEMA, connectionFactory);
   }

   @Override
   public MigrationResult migrate() {
      return this.migrator.migrate();
   }

   @Override
   public boolean hasPendingMigrations() {
      return this.migrator.hasPendingMigrations();
   }
}

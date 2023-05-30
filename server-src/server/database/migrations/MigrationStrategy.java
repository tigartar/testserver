package com.wurmonline.server.database.migrations;

public interface MigrationStrategy {
   MigrationResult migrate();

   boolean hasPendingMigrations();
}

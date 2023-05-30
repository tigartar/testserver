package com.wurmonline.server.database.migrations;

import com.wurmonline.server.database.ConnectionFactory;
import java.io.File;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.logging.Logger;
import javax.annotation.Nonnull;
import org.flywaydb.core.Flyway;
import org.flywaydb.core.api.MigrationInfo;
import org.flywaydb.core.api.MigrationInfoService;
import org.flywaydb.core.api.MigrationVersion;

public abstract class Migrator<B extends ConnectionFactory> {
   private static final Logger logger = Logger.getLogger(Migrator.class.getName());
   private static final String FILESYSTEM_PREFIX = "filesystem:";
   private static final String VERSION_TABLE = "SCHEMA_VERSION";
   private static final String MIGRATION_PREFIX = "v";
   private static final String REPEATABLE_MIGRATION_PREFIX = "r";
   private static final String BASELINE_VERSION = "1";
   private final Flyway flyway;
   private final List<Path> sqlDirectories;

   Migrator(B connectionFactory, List<Path> sqlDirectories, Migrator.FlywayConfigurer configurer) {
      this(new Flyway(), connectionFactory, sqlDirectories, configurer);
   }

   Migrator(Flyway flyway, B connectionFactory, List<Path> sqlDirectories, Migrator.FlywayConfigurer configurer) {
      this.flyway = flyway;
      this.sqlDirectories = Collections.unmodifiableList(new ArrayList<>(sqlDirectories));
      flyway.setLocations(asFlywayLocations(sqlDirectories));
      flyway.setTable("SCHEMA_VERSION");
      flyway.setSqlMigrationPrefix("v");
      flyway.setRepeatableSqlMigrationPrefix("r");
      configurer.configureMigrations(flyway);
   }

   public boolean isCurrent() {
      return this.flyway.info().pending().length == 0;
   }

   private MigrationInfoService baseline() {
      logger.info("No database migrations metadata found, creating baseline at version 1");
      this.flyway.setBaselineVersion(MigrationVersion.fromVersion("1"));
      this.flyway.baseline();
      return this.flyway.info();
   }

   private Optional<String> ensureDirsExist() {
      for(Path path : this.sqlDirectories) {
         File dir = path.toFile();
         if (!dir.exists() && !dir.mkdirs()) {
            String errorMessage = "Could not find or create migrations directory at " + dir.getAbsolutePath();
            logger.warning(errorMessage);
            Optional.of(errorMessage);
         }
      }

      return Optional.empty();
   }

   public boolean hasPendingMigrations() {
      MigrationInfoService migrationInfoService = this.flyway.info();
      MigrationInfo currentInfo = migrationInfoService.current();
      return currentInfo == null || migrationInfoService.pending().length > 0;
   }

   @Nonnull
   public MigrationResult migrate() {
      Optional<String> optionalError = this.ensureDirsExist();
      if (optionalError.isPresent()) {
         return MigrationResult.newError(optionalError.get());
      } else {
         MigrationInfoService migrationInfoService = this.flyway.info();
         MigrationInfo currentInfo = migrationInfoService.current();
         MigrationVersion beforeVersion;
         if (currentInfo == null) {
            beforeVersion = MigrationVersion.EMPTY;
            migrationInfoService = this.baseline();
            currentInfo = migrationInfoService.current();
            if (currentInfo == null) {
               String errorMessage = "No database versioning information found after creating baseline";
               logger.warning(errorMessage);
               return MigrationResult.newError(errorMessage);
            }

            int numMigrationsPending = migrationInfoService.pending().length;
            if (numMigrationsPending == 0) {
               logger.info("Database baselined to version 1. No migrations pending.");
               return MigrationResult.newSuccess(beforeVersion, currentInfo.getVersion(), 0);
            }
         } else {
            beforeVersion = currentInfo.getVersion();
            if (this.isCurrent()) {
               logger.info("No pending migrations, database is current");
               return MigrationResult.newSuccess(beforeVersion, beforeVersion, 0);
            }
         }

         logger.info("Found " + migrationInfoService.pending().length + " pending database migrations, initiating now.");
         int numMigrations = this.flyway.migrate();
         migrationInfoService = this.flyway.info();
         currentInfo = migrationInfoService.current();
         if (numMigrations == 0) {
            logger.warning("Pending migrations found but none performed.");
         } else {
            if (currentInfo == null) {
               String errorMessage = "Performed " + numMigrations + " migrations but no migrations metadata found afterwards.";
               logger.warning(errorMessage);
               return MigrationResult.newError(errorMessage);
            }

            logger.info("Performed " + numMigrations + " database migrations. Current version is " + currentInfo.getVersion());
         }

         return MigrationResult.newSuccess(beforeVersion, currentInfo.getVersion(), numMigrations);
      }
   }

   protected Flyway getFlyway() {
      return this.flyway;
   }

   public static String asFlywayLocation(Path dir) {
      return "filesystem:" + dir.toString();
   }

   public static String asFlywayLocations(List<Path> paths) {
      StringBuilder builder = new StringBuilder();
      if (paths.size() == 0) {
         return "";
      } else {
         builder.append("filesystem:");
         builder.append(paths.get(0).toString());

         for(Path path : paths.subList(1, paths.size())) {
            builder.append(',');
            builder.append("filesystem:");
            builder.append(path.toString());
         }

         return builder.toString();
      }
   }

   interface FlywayConfigurer {
      void configureMigrations(Flyway var1);
   }
}

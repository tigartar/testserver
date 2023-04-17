/*
 * Decompiled with CFR 0.152.
 */
package com.wurmonline.server.database.migrations;

import com.wurmonline.server.database.SqliteConnectionFactory;
import com.wurmonline.server.database.WurmDatabaseSchema;
import com.wurmonline.server.database.migrations.MigrationResult;
import com.wurmonline.server.database.migrations.MigrationStrategy;
import com.wurmonline.server.database.migrations.SqliteMigrator;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;
import org.flywaydb.core.api.MigrationVersion;

public class SqliteMigrationStrategy
implements MigrationStrategy {
    private static final Logger logger = Logger.getLogger(SqliteMigrationStrategy.class.getName());
    private final List<SqliteMigrator> migrators;

    public SqliteMigrationStrategy(List<SqliteConnectionFactory> connectionFactories, Path migrationsDir) {
        ArrayList<SqliteMigrator> migrators = new ArrayList<SqliteMigrator>();
        for (SqliteConnectionFactory connectionFactory : connectionFactories) {
            Path migrationsDirForSchema = migrationsDir.resolve(connectionFactory.getSchema().getMigration());
            migrators.add(new SqliteMigrator(connectionFactory, migrationsDirForSchema));
        }
        this.migrators = migrators;
    }

    private void logErrors(Map<WurmDatabaseSchema, MigrationResult.MigrationSuccess> successfulMigrations) {
        if (successfulMigrations.size() == 0) {
            logger.warning("Cannot perform migrations, error encounted. No migrations performed successfully.");
        } else {
            logger.warning("Cannot continue migrations, error encountered. Migration in a partial state.");
            logger.warning("The following migrations were performed successfully:");
            for (Map.Entry<WurmDatabaseSchema, MigrationResult.MigrationSuccess> entry : successfulMigrations.entrySet()) {
                MigrationResult.MigrationSuccess result = entry.getValue();
                String before = result.getVersionBefore().getVersion();
                if (before == null) {
                    before = "baseline";
                }
                logger.warning(entry.getKey().name() + " : performed " + result.getNumMigrations() + " migrations from " + before + " to " + result.getVersionAfter());
            }
        }
    }

    @Override
    public boolean hasPendingMigrations() {
        for (SqliteMigrator migrator : this.migrators) {
            if (!migrator.hasPendingMigrations()) continue;
            return true;
        }
        return false;
    }

    @Override
    public MigrationResult migrate() {
        MigrationVersion earliestVersion = MigrationVersion.LATEST;
        MigrationVersion latestVersion = MigrationVersion.EMPTY;
        int numMigrations = 0;
        LinkedHashMap<WurmDatabaseSchema, MigrationResult.MigrationSuccess> schemasMigrated = new LinkedHashMap<WurmDatabaseSchema, MigrationResult.MigrationSuccess>();
        for (SqliteMigrator migrator : this.migrators) {
            MigrationVersion before;
            MigrationResult result = migrator.migrate();
            if (result.isError()) {
                this.logErrors(schemasMigrated);
                return result;
            }
            MigrationResult.MigrationSuccess success = result.asSuccess();
            schemasMigrated.put(migrator.getSchema(), success);
            if (latestVersion.compareTo(success.getVersionAfter()) < 0) {
                latestVersion = success.getVersionAfter();
            }
            if ((before = success.getVersionBefore()) == null) {
                earliestVersion = MigrationVersion.EMPTY;
            } else if (before.compareTo(earliestVersion) < 0) {
                earliestVersion = success.getVersionBefore();
            }
            numMigrations += success.getNumMigrations();
        }
        if (latestVersion == null) {
            String errorMessage = "Error encountered after performing migrations: could not determine latest version";
            logger.warning(errorMessage);
            return MigrationResult.newError(errorMessage);
        }
        return MigrationResult.newSuccess(earliestVersion, latestVersion, numMigrations);
    }
}


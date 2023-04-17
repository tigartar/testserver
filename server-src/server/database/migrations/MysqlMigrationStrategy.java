/*
 * Decompiled with CFR 0.152.
 */
package com.wurmonline.server.database.migrations;

import com.wurmonline.server.database.MysqlConnectionFactory;
import com.wurmonline.server.database.WurmDatabaseSchema;
import com.wurmonline.server.database.migrations.MigrationResult;
import com.wurmonline.server.database.migrations.MigrationStrategy;
import com.wurmonline.server.database.migrations.MysqlMigrator;

public class MysqlMigrationStrategy
implements MigrationStrategy {
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


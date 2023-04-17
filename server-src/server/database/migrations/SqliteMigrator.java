/*
 * Decompiled with CFR 0.152.
 */
package com.wurmonline.server.database.migrations;

import com.wurmonline.server.database.SqliteConnectionFactory;
import com.wurmonline.server.database.WurmDatabaseSchema;
import com.wurmonline.server.database.migrations.Migrator;
import com.wurmonline.server.database.migrations.SqliteFlywayIssue1499Workaround;
import java.nio.file.Path;
import java.util.Collections;

public class SqliteMigrator
extends Migrator<SqliteConnectionFactory> {
    private final WurmDatabaseSchema schema;

    public SqliteMigrator(SqliteConnectionFactory connectionFactory, Path migrationsDir) {
        super(connectionFactory, Collections.singletonList(migrationsDir), flyway -> flyway.setDataSource(new SqliteFlywayIssue1499Workaround(connectionFactory.getDataSource())));
        this.schema = connectionFactory.getSchema();
    }

    public WurmDatabaseSchema getSchema() {
        return this.schema;
    }
}


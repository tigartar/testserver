/*
 * Decompiled with CFR 0.152.
 */
package com.wurmonline.server.database;

import com.wurmonline.server.database.WurmDatabaseSchema;
import java.sql.Connection;
import java.sql.SQLException;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public abstract class ConnectionFactory {
    private final String url;
    private final WurmDatabaseSchema schema;

    ConnectionFactory(@Nonnull String url, @Nonnull WurmDatabaseSchema schema) {
        this.schema = schema;
        this.url = url;
    }

    public final String getUrl() {
        return this.url;
    }

    public abstract Connection createConnection() throws SQLException;

    public abstract boolean isValid(@Nullable Connection var1) throws SQLException;

    public abstract boolean isStale(long var1, @Nullable Connection var3) throws SQLException;

    public WurmDatabaseSchema getSchema() {
        return this.schema;
    }
}


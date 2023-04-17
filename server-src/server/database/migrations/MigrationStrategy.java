/*
 * Decompiled with CFR 0.152.
 */
package com.wurmonline.server.database.migrations;

import com.wurmonline.server.database.migrations.MigrationResult;

public interface MigrationStrategy {
    public MigrationResult migrate();

    public boolean hasPendingMigrations();
}


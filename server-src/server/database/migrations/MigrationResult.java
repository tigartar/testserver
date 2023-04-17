/*
 * Decompiled with CFR 0.152.
 */
package com.wurmonline.server.database.migrations;

import javax.annotation.Nonnull;
import javax.annotation.ParametersAreNonnullByDefault;
import javax.annotation.concurrent.Immutable;
import org.flywaydb.core.api.MigrationVersion;

@ParametersAreNonnullByDefault
@Immutable
public abstract class MigrationResult {
    private MigrationResult() {
    }

    public abstract boolean isSuccess();

    public boolean isError() {
        return !this.isSuccess();
    }

    public MigrationError asError() {
        throw new IllegalArgumentException("This migration is not in error");
    }

    public MigrationSuccess asSuccess() {
        throw new IllegalArgumentException("This migration is not a success");
    }

    static MigrationError newError(String message) {
        return new MigrationError(message);
    }

    static MigrationSuccess newSuccess(MigrationVersion versionBeforeMigration, MigrationVersion versionAfterMigration, int numMigrations) {
        return new MigrationSuccess(versionBeforeMigration, versionAfterMigration, numMigrations);
    }

    @ParametersAreNonnullByDefault
    @Immutable
    public static final class MigrationSuccess
    extends MigrationResult {
        private final MigrationVersion versionBeforeMigration;
        private final MigrationVersion versionAfterMigration;
        private final int numMigrations;

        private MigrationSuccess(MigrationVersion versionBeforeMigration, MigrationVersion versionAfterMigration, int numMigrations) {
            this.versionBeforeMigration = versionBeforeMigration;
            this.versionAfterMigration = versionAfterMigration;
            this.numMigrations = numMigrations;
        }

        @Override
        public boolean isSuccess() {
            return true;
        }

        public MigrationVersion getVersionBefore() {
            return this.versionBeforeMigration;
        }

        public MigrationVersion getVersionAfter() {
            return this.versionAfterMigration;
        }

        public int getNumMigrations() {
            return this.numMigrations;
        }

        @Override
        public MigrationSuccess asSuccess() {
            return this;
        }
    }

    @ParametersAreNonnullByDefault
    @Immutable
    public static final class MigrationError
    extends MigrationResult {
        private final String message;

        private MigrationError(String message) {
            this.message = message;
        }

        @Override
        public boolean isSuccess() {
            return false;
        }

        @Nonnull
        public final String getMessage() {
            return this.message;
        }

        @Override
        public MigrationError asError() {
            return this;
        }
    }
}


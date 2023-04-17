/*
 * Decompiled with CFR 0.152.
 */
package com.wurmonline.server.database;

import com.wurmonline.server.database.ConnectionFactory;
import com.wurmonline.server.database.WurmDatabaseSchema;
import java.io.File;
import java.nio.file.Path;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.Locale;
import javax.annotation.Nullable;
import org.sqlite.SQLiteConfig;
import org.sqlite.SQLiteDataSource;

public class SqliteConnectionFactory
extends ConnectionFactory {
    private final SQLiteConfig config;
    private final Path fileDirectory;
    private final Path filePath;
    private final SQLiteDataSource dataSource;

    private static String buildFilename(WurmDatabaseSchema schema) {
        return schema.getDatabase().toLowerCase(Locale.ENGLISH) + ".db";
    }

    private static Path sqliteDirectory(String worldDirectory) {
        return new File(worldDirectory).toPath().resolve("sqlite");
    }

    private static String buildUrl(String directory, WurmDatabaseSchema schema) {
        return "jdbc:sqlite:" + directory + "/sqlite/" + schema.getDatabase().toLowerCase(Locale.ENGLISH) + ".db";
    }

    public SqliteConnectionFactory(String worldDirectory, WurmDatabaseSchema schema, SQLiteConfig config) {
        super(SqliteConnectionFactory.buildUrl(worldDirectory, schema), schema);
        this.fileDirectory = SqliteConnectionFactory.sqliteDirectory(worldDirectory);
        this.filePath = this.fileDirectory.resolve(SqliteConnectionFactory.buildFilename(schema));
        this.config = config;
        this.dataSource = new SQLiteDataSource(config);
        this.dataSource.setUrl(this.getUrl());
    }

    @Override
    public Connection createConnection() throws SQLException {
        return this.dataSource.getConnection();
    }

    @Override
    public boolean isValid(@Nullable Connection con) throws SQLException {
        return con != null;
    }

    @Override
    public boolean isStale(long lastUsed, @Nullable Connection connection) throws SQLException {
        return System.currentTimeMillis() - lastUsed > 3600000L;
    }

    public Path getFilePath() {
        return this.filePath;
    }

    public Path getFileDirectory() {
        return this.fileDirectory;
    }

    public SQLiteDataSource getDataSource() {
        return this.dataSource;
    }
}


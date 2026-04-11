package com.vbay.server.databaseManager;

import java.nio.file.Files;
import java.nio.file.Path;

public class DatabaseConfig {
    // Store the DB path relative to the server module, regardless of launch directory.
    private static final Path DB_URL = resolveServerDirectory().resolve("data").resolve("vbay.db");
    private static final String JDBC_URL = "jdbc:sqlite:" + DB_URL.toAbsolutePath().normalize();

    private DatabaseConfig() {
    }

    private static Path resolveServerDirectory() {
        Path workingDirectory = Path.of("").toAbsolutePath().normalize();
        if (Files.isDirectory(workingDirectory.resolve("src").resolve("main"))) {
            return workingDirectory;
        }

        Path serverDirectory = workingDirectory.resolve("server");
        if (Files.isDirectory(serverDirectory.resolve("src").resolve("main"))) {
            return serverDirectory;
        }

        return workingDirectory;
    }

    public static Path getDbPath() {
        return DB_URL;
    }

    public static String getJdbcUrl() {
        return JDBC_URL;
    }
}

package com.vbay.server.databaseManager;

import java.nio.file.Path;

public class DatabaseConfig {
    private static final Path DB_URL = Path.of("server", "data", "vbay.db");
    private static final String JDBC_URL = "jdbc:sqlite:" + DB_URL.toString();
    
    private DatabaseConfig() {
    }
    public static Path getDbPath() { 
        return DB_URL;
    }
    public static String getJdbcUrl() {
        return JDBC_URL;
    }

}

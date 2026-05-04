package com.vbay.server.databaseManager;
///Keeping DB connection CONFIGURATION
///Showing which DB to CONNECT
public class DatabaseConfig {
    private static final String HOST = "localhost";
    private static final int PORT = 1638;
    private static final String DATABASE = "vbay";
    private static final String USERNAME = "root";
    private static final String PASSWORD = "1234";
    private static final String JDBC_URL =
        "jdbc:mysql://" + HOST + ":" + PORT + "/" + DATABASE
        + "?useSSL=false"
        + "&serverTimezone=UTC"
        + "&connectionTimeZone=UTC"
        + "&forceConnectionTimeZoneToSession=true"
        + "&allowPublicKeyRetrieval=true";


    private DatabaseConfig() {
    }

    public static String getJdbcUrl() {
        return JDBC_URL;
    }
    static String getUsername() {
        return USERNAME;
    }
    static String getPassword() { ///đây là package-private
        return PASSWORD;
    }
}

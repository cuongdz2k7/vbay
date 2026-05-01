package com.vbay.server.databaseManager;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;


public final class DatabaseConnection { 
    
    private DatabaseConnection() {}
    //Using "DATA" from Config --> get "Connection" to actual DB
    public static Connection getConnection() throws SQLException {
        return DriverManager.getConnection(DatabaseConfig.getJdbcUrl(), 
                                            DatabaseConfig.getUsername(), 
                                            DatabaseConfig.getPassword());
    }
    
}
package com.vbay.server.database;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

/*
xem lại chỗ này vì có thể xảy ra nhiều connection mở cùng 1 lúc, crash server
nên nghiên cứu dùng connection pool

*/
public final class DatabaseConnection { 
    
    private DatabaseConnection() {}
    //Using "DATA" from Config --> get "Connection" to actual DB
    public static Connection getConnection() throws SQLException {
        return DriverManager.getConnection(DatabaseConfig.getJdbcUrl(), 
                                            DatabaseConfig.getUsername(), 
                                            DatabaseConfig.getPassword());
    }
    
}

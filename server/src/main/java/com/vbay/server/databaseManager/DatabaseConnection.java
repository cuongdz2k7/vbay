package com.vbay.server.databaseManager;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.logging.Logger;

import com.vbay.shared.Utils.LoggingUtils;

/*
xem lại chỗ này vì có thể xảy ra nhiều connection mở cùng 1 lúc, crash server
nên nghiên cứu dùng connection pool
*/
public final class DatabaseConnection { 
    private static final Logger LOGGER = LoggingUtils.getLogger(DatabaseConnection.class);
    
    static {
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
            Class.forName("org.h2.Driver");
        } catch (ClassNotFoundException e) {
            LOGGER.severe("Could not load JDBC drivers: " + e.getMessage());
        }
    }

    private DatabaseConnection() {}

    //Using "DATA" from Config --> get "Connection" to actual DB
    public static Connection getConnection() throws SQLException {
        try {
            return DriverManager.getConnection(DatabaseConfig.getJdbcUrl(), 
                                                DatabaseConfig.getUsername(), 
                                                DatabaseConfig.getPassword());
        } catch (SQLException e) {
            // Nếu kết nối MySQL lỗi, tự động kích hoạt Fallback sang H2 và thử lại
            if (!DatabaseConfig.isH2()) {
                LOGGER.warning("Connection to MySQL failed: " + e.getMessage() + ". Attempting auto-fallback to H2...");
                DatabaseConfig.triggerH2Fallback();
                return DriverManager.getConnection(DatabaseConfig.getJdbcUrl(), 
                                                    DatabaseConfig.getUsername(), 
                                                    DatabaseConfig.getPassword());
            }
            throw e;
        }
    }
}
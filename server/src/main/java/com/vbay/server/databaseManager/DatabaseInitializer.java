package com.vbay.server.databaseManager;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.vbay.shared.Utils.LoggingUtils;

public class DatabaseInitializer {
    private static final Logger LOGGER = LoggingUtils.getLogger(DatabaseInitializer.class);

    private DatabaseInitializer() {
    }

    public static void init() {
        LOGGER.info("Initializing database...");
        createDatabase();
        createTables();
        createIndexes();
        LOGGER.info("Database initialized successfully.");
    }

    private static void createDatabase() {
        // Chú ý: Ở đây chúng ta dùng DriverManager kết nối với DB_HOST_URL (không có tên DB)'
        LOGGER.info("Connecting to Database...");
        try (Connection connection = DriverManager.getConnection(DatabaseConfig.getDbHostUrl(), DatabaseConfig.getUsername(), DatabaseConfig.getPassword());
             Statement statement = connection.createStatement()) {
            
            // Lệnh tạo DB với Text Format chuẩn để không lỗi tiếng Việt
            String sql = "CREATE DATABASE IF NOT EXISTS " + DatabaseConfig.getDatabaseName() + " CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;";
            statement.executeUpdate(sql);
        } catch (Exception e) {
            throw new IllegalStateException("Failed to create database " + e.getMessage(), e);
        }
    }

    private static void createTables() {
        try (var connection = DatabaseConnection.getConnection();
             var statement = connection.createStatement();
             InputStream scriptStream = DatabaseInitializer.class.getResourceAsStream("/data_init.sql")) {

            if (scriptStream == null) {
                throw new IllegalStateException("Database initialization script not found on classpath: /data_init.sql");
            }

            try (BufferedReader reader = new BufferedReader(
                new InputStreamReader(scriptStream, StandardCharsets.UTF_8))) {

                String line;
                StringBuilder sb = new StringBuilder();
                while ((line = reader.readLine()) != null) {
                    line = line.trim();
                    if (line.isEmpty() || line.startsWith("--")) {
                        continue;
                    }
                    sb.append(line).append("\n");
                    if (line.endsWith(";")) {
                        if (sb.length() > 0) {
                            statement.execute(sb.toString());
                        }
                        sb.setLength(0);
                    }
                }

            }
        } catch (IOException e) {
            throw new IllegalStateException("Failed to read database initialization script", e);
        } catch (Exception e) {
            throw new IllegalStateException("Failed to initialize database tables", e);
        }
    }

    private static void createIndexes() {
        try (var connection = DatabaseConnection.getConnection();
             var statement = connection.createStatement()) {
            String[] indexQueries = {
                "CREATE INDEX idx_auctions_status ON auctions(status)",
                "CREATE INDEX idx_auctions_seller_id ON auctions(seller_id)",
                "CREATE INDEX idx_auctions_status_starting_time ON auctions(status, starting_time)",
                "CREATE INDEX idx_auctions_status_ending_time ON auctions(status, ending_time)"
            };

            for (String query : indexQueries) {
                try {
                    statement.execute(query);
                } catch (SQLException e) {
                    // Mã lỗi 1061: Duplicate key name (Index đã tồn tại)
                    if (e.getErrorCode() != 1061) {
                        // Các lỗi khác (sai tên cột, sai bảng...) thì vẫn cần in ra
                        LOGGER.log(Level.SEVERE, "Failed to create index: " + e.getMessage(), e);
                    }
                }
            }
        } catch (Exception e) {
            throw new IllegalStateException("Failed to create database " + e.getMessage(), e);
        }
    }
}

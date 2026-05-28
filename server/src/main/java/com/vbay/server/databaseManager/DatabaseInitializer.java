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
        
        // 1. Kiểm tra tính năng Recreate (xóa đi tạo lại từ đầu nếu được cấu hình)
        if (!DatabaseConfig.isH2() && DatabaseConfig.isRecreate()) {
            LOGGER.warning("Database Recreation is enabled in properties! Dropping existing database...");
            dropDatabase();
        }

        // 2. Thử tạo database (đối với MySQL) hoặc bắt lỗi để chuyển vùng Fallback
        try {
            createDatabase();
        } catch (Exception e) {
            LOGGER.warning("MySQL connection failed: " + e.getMessage());
            LOGGER.warning("Auto-switching to Embedded H2 Database (Zero-Setup)...");
            DatabaseConfig.triggerH2Fallback();
        }

        // 3. Khởi tạo bảng và index
        createTables();
        createIndexes();
        
        LOGGER.info("Database initialized successfully.");
    }

    private static void dropDatabase() {
        try (Connection connection = DriverManager.getConnection(DatabaseConfig.getDbHostUrl(), DatabaseConfig.getUsername(), DatabaseConfig.getPassword());
             Statement statement = connection.createStatement()) {
            String sql = "DROP DATABASE IF EXISTS " + DatabaseConfig.getDatabaseName() + ";";
            statement.executeUpdate(sql);
            LOGGER.info("Existing database '" + DatabaseConfig.getDatabaseName() + "' dropped successfully.");
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Failed to drop database: " + e.getMessage(), e);
        }
    }

    private static void createDatabase() {
        if (DatabaseConfig.isH2()) {
            LOGGER.info("H2 Database mode active. Skipping database creation step.");
            return;
        }

        LOGGER.info("Connecting to MySQL Database...");
        try (Connection connection = DriverManager.getConnection(DatabaseConfig.getDbHostUrl(), DatabaseConfig.getUsername(), DatabaseConfig.getPassword());
             Statement statement = connection.createStatement()) {
            
            // Lệnh tạo DB với Text Format chuẩn để không lỗi tiếng Việt
            String sql = "CREATE DATABASE IF NOT EXISTS " + DatabaseConfig.getDatabaseName() + " CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;";
            statement.executeUpdate(sql);
        } catch (Exception e) {
            throw new IllegalStateException("Failed to connect or create MySQL database: " + e.getMessage(), e);
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
            throw new IllegalStateException("Failed to initialize database tables: " + e.getMessage(), e);
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
                    // Mã lỗi 1061: Duplicate key name (MySQL)
                    // Mã lỗi 42111: Index already exists (H2)
                    int errorCode = e.getErrorCode();
                    String sqlState = e.getSQLState();
                    if (errorCode != 1061 && !"42111".equals(sqlState)) {
                        LOGGER.log(Level.SEVERE, "Failed to create index: " + e.getMessage(), e);
                    }
                }
            }
        } catch (Exception e) {
            throw new IllegalStateException("Failed to create indexes: " + e.getMessage(), e);
        }
    }
}

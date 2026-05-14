package com.vbay.server.databaseManager;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.Statement;

public class DatabaseInitializer {

    private DatabaseInitializer() {
    }

    public static void init() {
        createDatabase();
        createTables();
    }

    private static void createDatabase() {
        // Chú ý: Ở đây chúng ta dùng DriverManager kết nối với DB_HOST_URL (không có tên DB)'
        System.out.println("Connecting to " + DatabaseConfig.getDbHostUrl());
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
}

package com.vbay.server.databaseManager;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

public class DatabaseInitializer {

    private DatabaseInitializer() {
    }

    public static void init() {
        createDbfolder();
        createEmptyDbFileIfMissing();
        createTables();
    }

    private static void createDbfolder() {
        try {
            Path dbFile = DatabaseConfig.getDbPath();
            Path dbFolder = dbFile.getParent();
            if (dbFolder != null) {
                Files.createDirectories(dbFolder);
            }
        } catch (IOException e) {
            throw new IllegalStateException("Failed to create database folder", e);
        }
    }

    private static void createEmptyDbFileIfMissing() {
        try {
            Path dbFile = DatabaseConfig.getDbPath();
            if (!Files.exists(dbFile)) {
                Files.createFile(dbFile);
            }
        } catch (IOException e) {
            throw new IllegalStateException("Failed to create database file", e);
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
                StringBuilder stringBuilder = new StringBuilder();
                while ((line = reader.readLine()) != null) {
                    line = line.trim();
                    if (line.isEmpty() || line.startsWith("--")) {
                        continue;
                    }
                    stringBuilder.append(line).append("\n");
                }

                if (stringBuilder.length() == 0) {
                    throw new IllegalStateException("Database initialization script is empty");
                }

                statement.execute(stringBuilder.toString());
            }
        } catch (IOException e) {
            throw new IllegalStateException("Failed to read database initialization script", e);
        } catch (Exception e) {
            throw new IllegalStateException("Failed to initialize database tables", e);
        }
    }
}

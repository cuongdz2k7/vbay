package com.vbay.server.database;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;

public class DatabaseInitializer {

    private DatabaseInitializer() {
    }

    public static void init() {
        createTables();
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

package com.vbay.server.databaseManager;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.vbay.shared.Utils.LoggingUtils;

///Keeping DB connection CONFIGURATION
///Showing which DB to CONNECT
public class DatabaseConfig {
    private static final Logger LOGGER = LoggingUtils.getLogger(DatabaseConfig.class);

    private static String dbType = "mysql";
    private static boolean dbRecreate = false;
    
    private static String mysqlHost = "localhost";
    private static int mysqlPort = 1638;
    private static String mysqlDatabase = "vbay";
    private static String mysqlUsername = "root";
    private static String mysqlPassword = "1234";

    private static String h2Url = "jdbc:h2:./vbay;MODE=MySQL;DATABASE_TO_UPPER=false;DB_CLOSE_DELAY=-1";
    private static String h2Username = "sa";
    private static String h2Password = "";

    private static boolean isFallbackToH2 = false;

    static {
        loadConfig();
    }

    private DatabaseConfig() {
    }

    public static void loadConfig() {
        File configFile = new File("./database.properties");

        // 1. Tự động sinh file database.properties ngoài môi trường chạy nếu chưa có
        if (!configFile.exists()) {
            LOGGER.info("database.properties not found in working directory. Generating default from template...");
            try (InputStream template = DatabaseConfig.class.getResourceAsStream("/database.properties")) {
                if (template != null) {
                    Files.copy(template, configFile.toPath(), StandardCopyOption.REPLACE_EXISTING);
                    LOGGER.info("Default database.properties created successfully next to the JAR.");
                } else {
                    LOGGER.warning("Could not find database.properties template in resources.");
                }
            } catch (Exception e) {
                LOGGER.log(Level.SEVERE, "Failed to auto-generate database.properties", e);
            }
        }

        // 2. Đọc file database.properties ngoài môi trường chạy
        if (configFile.exists()) {
            Properties props = new Properties();
            try (FileInputStream fis = new FileInputStream(configFile)) {
                props.load(fis);
                
                dbType = props.getProperty("db.type", "mysql").trim().toLowerCase();
                dbRecreate = Boolean.parseBoolean(props.getProperty("db.recreate", "false").trim());
                
                mysqlHost = props.getProperty("mysql.host", "localhost").trim();
                mysqlPort = Integer.parseInt(props.getProperty("mysql.port", "1638").trim());
                mysqlDatabase = props.getProperty("mysql.database", "vbay").trim();
                mysqlUsername = props.getProperty("mysql.username", "root").trim();
                mysqlPassword = props.getProperty("mysql.password", "1234").trim();

                h2Url = props.getProperty("h2.url", "jdbc:h2:./vbay;MODE=MySQL;DATABASE_TO_UPPER=false;DB_CLOSE_DELAY=-1").trim();
                h2Username = props.getProperty("h2.username", "sa").trim();
                h2Password = props.getProperty("h2.password", "").trim();

                LOGGER.info(() -> "Database configuration loaded successfully from database.properties (Type: " + dbType + ")");
            } catch (Exception e) {
                LOGGER.log(Level.SEVERE, "Error reading database.properties, falling back to default built-in settings", e);
            }
        }
    }

    public static boolean isH2() {
        return "h2".equalsIgnoreCase(dbType) || isFallbackToH2;
    }

    public static boolean isRecreate() {
        return dbRecreate;
    }

    public static void triggerH2Fallback() {
        if (!isFallbackToH2) {
            isFallbackToH2 = true;
            LOGGER.warning("!!! CRITICAL: MySQL connection failed or unavailable. Falling back to Embedded H2 Database! !!!");
        }
    }

    public static String getDatabaseName() {
        return mysqlDatabase;
    }

    public static String getJdbcUrl() {
        if (isH2()) {
            return h2Url;
        }
        return "jdbc:mysql://" + mysqlHost + ":" + mysqlPort + "/" + mysqlDatabase
                + "?useSSL=false"
                + "&serverTimezone=UTC"
                + "&connectionTimeZone=UTC"
                + "&forceConnectionTimeZoneToSession=true"
                + "&allowPublicKeyRetrieval=true";
    }

    public static String getDbHostUrl() {
        if (isH2()) {
            return h2Url;
        }
        return "jdbc:mysql://" + mysqlHost + ":" + mysqlPort + "/"
                + "?useSSL=false"
                + "&serverTimezone=UTC"
                + "&connectionTimeZone=UTC"
                + "&forceConnectionTimeZoneToSession=true"
                + "&allowPublicKeyRetrieval=true";
    }

    public static String getUsername() {
        if (isH2()) {
            return h2Username;
        }
        return mysqlUsername;
    }

    public static String getPassword() {
        if (isH2()) {
            return h2Password;
        }
        return mysqlPassword;
    }
}

package com.vbay.server.databaseManager;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

public class DatabaseInitializer {

    private DatabaseInitializer() {
    }
    ///tạo thư mục lưu database
    public static void init() { 
        createDbfolder();
        createEmptyDbFileIfMissing();
        createTables();
    }
    ///private để tránh truy cập bên ngoài
    private static void createDbfolder () { 
        try {
            /// tạo ra 1 đối tượng path để trỏ tới file database
            Path dbFile = DatabaseConfig.getDbPath();
            // sau đó lấy thư mục cha của nó và tạo thư mục nếu chưa tồn tại
            Path dbFolder = dbFile.getParent();
            if (dbFolder != null) { 
                Files.createDirectories(dbFolder);
            }
        } catch (IOException e) {
            throw new IllegalStateException("Failed to create database folder: ", e);
        }
    }
    ///tạo file database nếu chưa tồn tại
    private static void createEmptyDbFileIfMissing() {
        try {
            Path dbFile = DatabaseConfig.getDbPath();
            if (!Files.exists(dbFile)) {
                Files.createFile(dbFile);
            }
        } catch (IOException e) {
            throw new IllegalStateException("Failed to create database file: ", e);
        }
    }
    ///khởi tạo bảng trong database nếu chưa tồn tại
    private static void createTables() {
        try (var Connection = DatabaseConnection.getConnection();
            ///tạo object để gửi lệnh sql đến database
            var Statement = Connection.createStatement();
            var inputStream = DatabaseInitializer.class.
                                getClassLoader().getResourceAsStream("com/vbay/server/resources/data_init.sql")) {
            
            if (inputStream == null) {
                throw new IllegalStateException("Cannot find SQL init file");
            }
            String sql = new String(inputStream.readAllBytes(), java.nio.charset.StandardCharsets.UTF_8);
            Statement.execute(sql);
            /*
             1. Có 3 kiểu execute
            Method	            Dùng khi	                Trả về
            executeQuery()	    SELECT              	    ResultSet
            executeUpdate()	    INSERT / UPDATE / DELETE	số dòng bị ảnh hưởng
            execute()	        bất kỳ SQL	                boolean
            */
        } catch (Exception e) {
            throw new IllegalStateException("Failed to initialize database tables: ", e);
        }
    }

}

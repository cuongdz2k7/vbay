package com.vbay.server.databaseManager;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;


public final class DatabaseConnection { 
    
    private DatabaseConnection() {
        
    }
    //Connection là INTERFACE, nó chứa object dùng để để connect tới database
    //tại sao nó lại để Connection là interface ? 
    // quy trình là: getconnection thì nó sẽ tìm driver phù hợp với db(mysql, sqlite)
    // và mỗi driver để connect là 1 class, nên nó 

    ///tạo connection mới mỗi lần gọi getConnection, sau đó sẽ được close sau khi sử dụng xong, tránh việc giữ connection
    public static Connection getConnection() throws SQLException {
        return DriverManager.getConnection(DatabaseConfig.getJdbcUrl(), 
                                            DatabaseConfig.getUsername(), 
                                            DatabaseConfig.getPassword());
    }
    
}
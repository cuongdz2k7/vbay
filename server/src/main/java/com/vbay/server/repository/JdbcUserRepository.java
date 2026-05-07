package com.vbay.server.repository;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Optional;
import java.math.BigDecimal;

import com.vbay.server.model.User;
import com.vbay.server.database.DatabaseConnection;
import com.vbay.shared.status.Position;
import com.vbay.shared.status.shared_status.UserStatus;


public class JdbcUserRepository implements UserRepository {

    @Override  
    public Optional<User> findByUsername (String username) throws SQLException { 
        String sql = """
            SELECT id, username, email, password_hash, phone_number, position, status, balance, time_init
            FROM users
            WHERE username = ?
            """;
        ///tự động close
        try (Connection connection = DatabaseConnection.getConnection();
            PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, username);
            try (ResultSet rs = statement.executeQuery()) {
                if (!rs.next()) {
                    return Optional.empty();
                }
                return Optional.of(mapUser(rs));
            }
        }
    }
    @Override
    public Optional<User> findByEmail(String email) throws SQLException {
        String sql = """
            SELECT id, username, email, password_hash, phone_number, position, status, balance, time_init
            FROM users
            WHERE email = ?
            LIMIT 1
            """;

        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setString(1, email);

            try (ResultSet rs = statement.executeQuery()) {
                if (!rs.next()) {
                    return Optional.empty();
                }

                return Optional.of(mapUser(rs));
            }
        }
    }
    
    @Override
    public boolean existsByUsername (String username) throws SQLException {
        String sql = "SELECT 1 FROM users WHERE username = ? LIMIT 1";
        try (Connection connection = DatabaseConnection.getConnection();
            PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, username);
            ///rs.next di chuyển con trỏ và trả về boolean
            try (ResultSet rs = statement.executeQuery()) {
                return rs.next();
            }
        }
    }
    @Override
    public boolean existsByEmail(String email) throws SQLException {
        String sql = "SELECT 1 FROM users WHERE email = ? LIMIT 1";

        try (Connection connection = DatabaseConnection.getConnection();
            PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setString(1, email);

            try (ResultSet rs = statement.executeQuery()) {
                return rs.next();
            }
        }
    }
    
    @Override
    public void save (User user) throws SQLException {
        String sql = """
            INSERT INTO users (username, email, password_hash, phone_number, position, status, balance, time_init)
            VALUES (?, ?, ?, ?, ?, ?, ?, ?)
            """;
        try (Connection connection = DatabaseConnection.getConnection();
            PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, user.getUserName());
            statement.setString(2, user.getEmail());
            statement.setString(3, user.getPasswordHash());
            statement.setString(4, user.getPhoneNumber());
            statement.setString(5, user.getPosition().name());
            statement.setString(6, user.getUserStatus().name());
            statement.setBigDecimal(7, user.getAvailableBalance());
            statement.setString(8, user.getTimeinit());
            statement.executeUpdate();
        }
    }

    private User mapUser(ResultSet rs) throws SQLException {
        User user = new User(
            rs.getString("username"),
            rs.getString("email"),
            rs.getString("password_hash"), 
            rs.getString("phone_number"),
            Position.valueOf(rs.getString("position")),
            UserStatus.valueOf(rs.getString("status")),
            new BigDecimal(rs.getString("balance")),
            BigDecimal.ZERO,
            rs.getString("time_init")
        );
        user.setId(rs.getLong("id"));
        return user;
    }


}

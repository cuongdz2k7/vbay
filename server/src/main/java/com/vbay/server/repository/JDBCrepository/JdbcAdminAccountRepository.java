package com.vbay.server.repository.JDBCrepository;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

import com.vbay.server.repository.AdminAccountRepository;
import com.vbay.shared.enums.auth.Position;
import com.vbay.shared.enums.auth.UserStatus;

public class JdbcAdminAccountRepository implements AdminAccountRepository {
    private final Connection connection;

    public JdbcAdminAccountRepository(Connection connection) {
        this.connection = connection;
    }

    @Override
    public boolean existsByUsernameOrEmail(String username, String email) throws SQLException {
        String sql = "SELECT 1 FROM users WHERE username = ? OR email = ? LIMIT 1";
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, username);
            statement.setString(2, email);
            try (var rs = statement.executeQuery()) {
                return rs.next();
            }
        }
    }

    @Override
    public void insertAdminAccount(String username, String email, String passwordHash) throws SQLException {
        String sql = """
            INSERT INTO users (
                username, email, password_hash, phone_number, position,
                status, available_balance, hold_balance
            )
            VALUES (?, ?, ?, NULL, ?, ?, 0, 0)
            """;
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, username);
            statement.setString(2, email);
            statement.setString(3, passwordHash);
            statement.setString(4, Position.ADMIN.name());
            statement.setString(5, UserStatus.ACTIVE.name());
            statement.executeUpdate();
        }
    }

    @Override
    public void updateAdminAccount(String username, String email, String passwordHash) throws SQLException {
        String sql = """
            UPDATE users
            SET username = ?,
                email = ?,
                password_hash = ?,
                position = ?,
                status = ?
            WHERE username = ? OR email = ?
            """;
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, username);
            statement.setString(2, email);
            statement.setString(3, passwordHash);
            statement.setString(4, Position.ADMIN.name());
            statement.setString(5, UserStatus.ACTIVE.name());
            statement.setString(6, username);
            statement.setString(7, email);
            statement.executeUpdate();
        }
    }
}

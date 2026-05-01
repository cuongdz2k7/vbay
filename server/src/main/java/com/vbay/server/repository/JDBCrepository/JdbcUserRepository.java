package com.vbay.server.repository.JDBCrepository;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Optional;

import com.vbay.server.Model.User;
import com.vbay.server.exception.ValidationException;
import com.vbay.server.mapper.rowmapper.UserRowMapper;
import com.vbay.server.repository.UserRepository;

public class JdbcUserRepository implements UserRepository {
    private final Connection connection;

    public JdbcUserRepository(Connection connection) {
        this.connection = connection;
    }

    @Override
    public Optional<User> findById(Long id) throws SQLException {
        String sql = """
            SELECT id, username, email, password_hash, phone_number, position, status, balance, time_init
            FROM users
            WHERE id = ?
            """;

        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setLong(1, id);
            try (ResultSet rs = statement.executeQuery()) {
                if (!rs.next()) {
                    return Optional.empty();
                }
                return Optional.of(UserRowMapper.mapUser(rs));
            }
        }
    }

    @Override
    public Optional<User> findByUsername(String username) throws SQLException {
        String sql = """
            SELECT id, username, email, password_hash, phone_number, position, status, balance, time_init
            FROM users
            WHERE username = ?
            """;

        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, username);
            try (ResultSet rs = statement.executeQuery()) {
                if (!rs.next()) {
                    return Optional.empty();
                }
                return Optional.of(UserRowMapper.mapUser(rs));
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

        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, email);

            try (ResultSet rs = statement.executeQuery()) {
                if (!rs.next()) {
                    return Optional.empty();
                }
                return Optional.of(UserRowMapper.mapUser(rs));
            }
        }
    }

    @Override
    public boolean existsByUsername(String username) throws SQLException {
        String sql = "SELECT 1 FROM users WHERE username = ? LIMIT 1";

        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, username);
            try (ResultSet rs = statement.executeQuery()) {
                return rs.next();
            }
        }
    }

    @Override
    public boolean existsByEmail(String email) throws SQLException {
        String sql = "SELECT 1 FROM users WHERE email = ? LIMIT 1";

        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, email);
            try (ResultSet rs = statement.executeQuery()) {
                return rs.next();
            }
        }
    }

    @Override
    public void save(User user) throws SQLException {
        String sql = """
            INSERT INTO users (username, email, password_hash, phone_number, position, status, balance, time_init)
            VALUES (?, ?, ?, ?, ?, ?, ?, ?)
            """;

        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, user.getUserName());
            statement.setString(2, user.getEmail());
            statement.setString(3, user.getPasswordHash());
            statement.setString(4, user.getPhoneNumber());
            statement.setString(5, user.getPosition().name());
            statement.setString(6, user.getUserStatus().name());
            statement.setBigDecimal(7, user.getBalance());
            statement.setString(8, user.getTimeinit());
            statement.executeUpdate();
        } catch (SQLException e) {
            String sqlState = e.getSQLState();
            if ("23000".equals(sqlState) && e.getErrorCode() == 1062) {
                throw new ValidationException("Username or email already exists");
            }
            throw e;
        }
    }
}

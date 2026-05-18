package com.vbay.server.repository.JDBCrepository;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Optional;

import com.vbay.server.exception.ValidationException;
import com.vbay.server.mapper.rowmapper.UserRowMapper;
import com.vbay.server.model.User;
import com.vbay.server.repository.UserRepository;

public class JdbcUserRepository implements UserRepository {
    private final Connection connection;

    public JdbcUserRepository(Connection connection) {
        this.connection = connection;
    }

    @Override
    public Optional<User> findById(long id) throws SQLException {
        String sql = """
            SELECT id, username, email, password_hash, phone_number, position, status, available_balance, hold_balance, version, time_init
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
            SELECT id, username, email, password_hash, phone_number, position, status, available_balance, hold_balance, version, time_init
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
            SELECT id, username, email, password_hash, phone_number, position, status, available_balance, hold_balance, version, time_init
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
    public User save(User user) throws SQLException {
        String sql = """
            INSERT INTO users (username, email, password_hash, phone_number, position, status, available_balance, hold_balance, time_init)
            VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)
            """;

        try (PreparedStatement statement = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            statement.setString(1, user.getUserName());
            statement.setString(2, user.getEmail());
            statement.setString(3, user.getPasswordHash());
            statement.setString(4, user.getPhoneNumber());
            statement.setString(5, user.getPosition().name());
            statement.setString(6, user.getUserStatus().name());
            statement.setBigDecimal(7, user.getAvailableBalance());
            statement.setBigDecimal(8, user.getHoldBalance());
            statement.setString(9, user.getTimeinit());
            statement.executeUpdate();

            try (ResultSet rs = statement.getGeneratedKeys()) {
                if (rs.next()) {
                    user.setId(rs.getLong(1));
                    return user;
                }
            }
        } catch (SQLException e) {
            String sqlState = e.getSQLState();
            if ("23000".equals(sqlState) && e.getErrorCode() == 1062) {
                throw new ValidationException("Username or email already exists");
            }
            throw e;
        }

        throw new SQLException("Creating user failed, no ID obtained.");
    }

    @Override
    public Optional<User> lockUserForUpdate (long id) throws SQLException {
        String sql = """
            SELECT id, username, email, password_hash, phone_number, position, status, available_balance, hold_balance, version, time_init
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
    public long releaseHoldBalance(long userId, BigDecimal amount) throws SQLException {
        String sql = """
            UPDATE users
            SET hold_balance = hold_balance - ?,
                available_balance = available_balance + ?,
                version = version + 1
            WHERE id = ?
            AND hold_balance >= ?
            """;

        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setBigDecimal(1, amount);
            statement.setBigDecimal(2, amount);
            statement.setLong(3, userId);
            statement.setBigDecimal(4, amount);
            if (statement.executeUpdate() == 0) {
                throw new ValidationException("Insufficient hold balance");
            }
        }
        return findVersionById(userId);
    }

    @Override
    public long holdBalance(long userId, BigDecimal amount) throws SQLException {
        String sql = """
            UPDATE users
            SET available_balance = available_balance - ?,
                hold_balance = hold_balance + ?,
                version = version + 1
            WHERE id = ?
            AND status = 'ACTIVE'
            AND available_balance >= ?
            """;

        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setBigDecimal(1, amount);
            statement.setBigDecimal(2, amount);
            statement.setLong(3, userId);
            statement.setBigDecimal(4, amount);
            if (statement.executeUpdate() == 0) {
                throw new ValidationException("Insufficient available balance");
            }
        }
        return findVersionById(userId);
    }

    @Override
    public long decreaseAvailableBalance(long userId, BigDecimal amount) throws SQLException {
        String sql = """
            UPDATE users
            SET available_balance = available_balance - ?,
                version = version + 1
            WHERE id = ?
            AND status = 'ACTIVE'
            AND available_balance >= ?
            """;

        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setBigDecimal(1, amount);
            statement.setLong(2, userId);
            statement.setBigDecimal(3, amount);
            if (statement.executeUpdate() == 0) {
                throw new ValidationException("Insufficient available balance");
            }
        }
        return findVersionById(userId);
    }

    @Override
    public long depositAvailableBalance(long userId, BigDecimal amount) throws SQLException {
        String sql = """
            UPDATE users
            SET available_balance = available_balance + ?,
                version = version + 1
            WHERE id = ?
            AND status = 'ACTIVE'
            """;

        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setBigDecimal(1, amount);
            statement.setLong(2, userId);
            if (statement.executeUpdate() == 0) {
                throw new ValidationException("Active user not found");
            }
        }
        return findVersionById(userId);
    }

    private long findVersionById(long userId) throws SQLException {
        String sql = "SELECT version FROM users WHERE id = ?";
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setLong(1, userId);
            try (ResultSet rs = statement.executeQuery()) {
                if (!rs.next()) {
                    throw new SQLException("User not found while reading version");
                }
                return rs.getLong("version");
            }
        }
    }
}

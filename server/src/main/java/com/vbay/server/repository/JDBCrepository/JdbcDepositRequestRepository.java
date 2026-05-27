package com.vbay.server.repository.JDBCrepository;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import com.vbay.server.model.DepositRequestRow;
import com.vbay.server.repository.DepositRequestRepository;

public class JdbcDepositRequestRepository implements DepositRequestRepository {
    private final Connection connection;

    public JdbcDepositRequestRepository(Connection connection) {
        this.connection = connection;
    }

    @Override
    public DepositRequestRow save(DepositRequestRow row) throws SQLException {
        String sql = "INSERT INTO deposit_requests (user_id, amount, status) VALUES (?, ?, ?)";
        try (PreparedStatement statement = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            statement.setLong(1, row.getUserId());
            statement.setBigDecimal(2, row.getAmount());
            statement.setString(3, row.getStatus());
            statement.executeUpdate();

            try (ResultSet rs = statement.getGeneratedKeys()) {
                if (rs.next()) {
                    row.setId(rs.getLong(1));
                    String tsSql = "SELECT created_at FROM deposit_requests WHERE id = ?";
                    try (PreparedStatement tsStmt = connection.prepareStatement(tsSql)) {
                        tsStmt.setLong(1, row.getId());
                        try (ResultSet tsRs = tsStmt.executeQuery()) {
                            if (tsRs.next()) {
                                row.setCreatedAt(tsRs.getTimestamp("created_at").toLocalDateTime());
                            }
                        }
                    }
                    return row;
                }
            }
        }
        throw new SQLException("Creating deposit request failed, no ID obtained.");
    }

    @Override
    public List<DepositRequestRow> findAllPending() throws SQLException {
        String sql = """
            SELECT dr.id, dr.user_id, u.username, dr.amount, dr.status, dr.admin_id, dr.created_at, dr.processed_at
            FROM deposit_requests dr
            JOIN users u ON dr.user_id = u.id
            WHERE dr.status = 'PENDING'
            ORDER BY dr.created_at DESC
            """;
        List<DepositRequestRow> list = new ArrayList<>();
        try (PreparedStatement statement = connection.prepareStatement(sql);
             ResultSet rs = statement.executeQuery()) {
            while (rs.next()) {
                list.add(mapRow(rs));
            }
        }
        return list;
    }

    @Override
    public Optional<DepositRequestRow> findById(long depositId) throws SQLException {
        String sql = """
            SELECT dr.id, dr.user_id, u.username, dr.amount, dr.status, dr.admin_id, dr.created_at, dr.processed_at
            FROM deposit_requests dr
            JOIN users u ON dr.user_id = u.id
            WHERE dr.id = ?
            """;
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setLong(1, depositId);
            try (ResultSet rs = statement.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapRow(rs));
                }
            }
        }
        return Optional.empty();
    }

    @Override
    public void updateStatus(long depositId, String status, Long adminId) throws SQLException {
        String sql = "UPDATE deposit_requests SET status = ?, admin_id = ?, processed_at = CURRENT_TIMESTAMP WHERE id = ?";
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, status);
            if (adminId == null) {
                statement.setNull(2, java.sql.Types.BIGINT);
            } else {
                statement.setLong(2, adminId);
            }
            statement.setLong(3, depositId);
            statement.executeUpdate();
        }
    }

    private DepositRequestRow mapRow(ResultSet rs) throws SQLException {
        DepositRequestRow row = new DepositRequestRow();
        row.setId(rs.getLong("id"));
        row.setUserId(rs.getLong("user_id"));
        row.setUsername(rs.getString("username"));
        row.setAmount(rs.getBigDecimal("amount"));
        row.setStatus(rs.getString("status"));
        long adminId = rs.getLong("admin_id");
        row.setAdminId(rs.wasNull() ? null : adminId);
        Timestamp ca = rs.getTimestamp("created_at");
        row.setCreatedAt(ca != null ? ca.toLocalDateTime() : null);
        Timestamp pa = rs.getTimestamp("processed_at");
        row.setProcessedAt(pa != null ? pa.toLocalDateTime() : null);
        return row;
    }
}

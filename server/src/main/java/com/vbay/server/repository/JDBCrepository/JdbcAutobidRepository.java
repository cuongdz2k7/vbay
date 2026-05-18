package com.vbay.server.repository.JDBCrepository;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Optional;

import com.vbay.server.repository.AutobidRepository;
import com.vbay.server.service.bid.autobid.enums.AutobidStatus;
import com.vbay.server.service.bid.autobid.model.Autobid;
import com.vbay.server.service.bid.autobid.model.AutobidChange;

public class JdbcAutobidRepository implements AutobidRepository {
    private final Connection connection;

    public JdbcAutobidRepository(Connection connection) {
        this.connection = connection;
    }

    @Override
    public Optional<Autobid> findWinningByAuctionId(long auctionId) throws SQLException {
        String sql = """
            SELECT id, auction_id, user_id, max_bid_amount, hold_amount, status, created_at, updated_at
            FROM autobids
            WHERE auction_id = ?
            AND status = 'WINNING'
            LIMIT 1
            """;

        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setLong(1, auctionId);
            try (ResultSet rs = statement.executeQuery()) {
                if (!rs.next()) {
                    return Optional.empty();
                }
                return Optional.of(mapAutobid(rs));
            }
        }
    }

    @Override
    public void update(AutobidChange change) throws SQLException {
        String sql = """
            UPDATE autobids
            SET status = ?,
                max_bid_amount = ?,
                hold_amount = ?,
                updated_at = CURRENT_TIMESTAMP
            WHERE id = ?
            """;

        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, change.getNewStatus().name());
            statement.setBigDecimal(2, change.getNewMaxBidAmount());
            statement.setBigDecimal(3, change.getNewHoldAmount());
            statement.setLong(4, change.getAutobidId());
            statement.executeUpdate();
        }
    }

    @Override
    public void updateStatus(long autobidId, AutobidStatus status) throws SQLException {
        String sql = """
            UPDATE autobids
            SET status = ?,
                updated_at = CURRENT_TIMESTAMP
            WHERE id = ?
            """;

        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, status.name());
            statement.setLong(2, autobidId);
            statement.executeUpdate();
        }
    }

    private Autobid mapAutobid(ResultSet rs) throws SQLException {
        Autobid autobid = new Autobid(
            rs.getLong("auction_id"),
            rs.getLong("user_id"),
            rs.getBigDecimal("max_bid_amount"),
            rs.getBigDecimal("hold_amount"),
            AutobidStatus.valueOf(rs.getString("status")),
            rs.getTimestamp("created_at").toLocalDateTime(),
            rs.getTimestamp("updated_at").toLocalDateTime()
        );
        autobid.setId(rs.getLong("id"));
        return autobid;
    }
}

package com.vbay.server.repository.JDBCrepository;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import com.vbay.server.exception.ValidationException;
import com.vbay.server.model.Autobid;
import com.vbay.server.repository.AutobidRepository;
import com.vbay.server.service.bid.enums.AutobidStatus;

public class JdbcAutobidRepository implements AutobidRepository {
    private final Connection connection;

    public JdbcAutobidRepository(Connection connection) {
        this.connection = connection;
    }

    @Override
    public Autobid save(Autobid autobid) throws SQLException {
        Optional<Autobid> existing = findByAuctionIdAndUserId(autobid.getAuctionId(), autobid.getUserId());
        if (existing.isPresent()) {
            autobid.setId(existing.get().getId());
            updateMaxBidAmount(autobid.getId(), autobid.getMaxBidAmount());
            updateStatus(autobid.getId(), autobid.getStatus());
            return autobid;
        }

        String sql = """
            INSERT INTO autobids (auction_id, user_id, max_bid_amount, status)
            VALUES (?, ?, ?, ?)
            """;

        try (PreparedStatement statement = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            statement.setLong(1, autobid.getAuctionId());
            statement.setLong(2, autobid.getUserId());
            statement.setBigDecimal(3, autobid.getMaxBidAmount());
            statement.setString(4, autobid.getStatus().name());
            statement.executeUpdate();

            try (ResultSet rs = statement.getGeneratedKeys()) {
                if (rs.next()) {
                    autobid.setId(rs.getLong(1));
                    return autobid;
                }
            }
        }

        throw new SQLException("Creating autobid failed, no ID obtained.");
    }

    @Override
    public Optional<Autobid> findByAuctionIdAndUserId(long auctionId, long userId) throws SQLException {
        String sql = """
            SELECT id, auction_id, user_id, max_bid_amount, status, created_at, updated_at
            FROM autobids
            WHERE auction_id = ?
            AND user_id = ?
            LIMIT 1
            """;

        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setLong(1, auctionId);
            statement.setLong(2, userId);
            try (ResultSet rs = statement.executeQuery()) {
                if (!rs.next()) {
                    return Optional.empty();
                }
                return Optional.of(mapAutobid(rs));
            }
        }
    }

    @Override
    public Map<Long, Autobid> findByAuctionIdsAndUserId(List<Long> auctionIds, long userId) throws SQLException {
        if (auctionIds == null || auctionIds.isEmpty()) {
            return Map.of();
        }

        String placeholders = String.join(",", Collections.nCopies(auctionIds.size(), "?"));
        String sql = """
            SELECT id, auction_id, user_id, max_bid_amount, status, created_at, updated_at
            FROM autobids
            WHERE user_id = ?
            AND auction_id IN (%s)
            """.formatted(placeholders);

        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setLong(1, userId);
            for (int i = 0; i < auctionIds.size(); i++) {
                statement.setLong(i + 2, auctionIds.get(i));
            }

            Map<Long, Autobid> autobidsByAuctionId = new HashMap<>();
            try (ResultSet rs = statement.executeQuery()) {
                while (rs.next()) {
                    Autobid autobid = mapAutobid(rs);
                    autobidsByAuctionId.put(autobid.getAuctionId(), autobid);
                }
            }
            return autobidsByAuctionId;
        }
    }

    @Override
    public Optional<Autobid> findWinningByAuctionId(long auctionId) throws SQLException {
        String sql = """
            SELECT id, auction_id, user_id, max_bid_amount, status, created_at, updated_at
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
            if (statement.executeUpdate() == 0) {
                throw new ValidationException("Autobid not found");
            }
        }
    }

    @Override
    public void updateMaxBidAmount(long autobidId, BigDecimal maxBidAmount) throws SQLException {
        String sql = """
            UPDATE autobids
            SET max_bid_amount = ?,
                updated_at = CURRENT_TIMESTAMP
            WHERE id = ?
            """;

        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setBigDecimal(1, maxBidAmount);
            statement.setLong(2, autobidId);
            if (statement.executeUpdate() == 0) {
                throw new ValidationException("Autobid not found");
            }
        }
    }

    private Autobid mapAutobid(ResultSet rs) throws SQLException {
        Autobid autobid = new Autobid(
            rs.getLong("auction_id"),
            rs.getLong("user_id"),
            rs.getBigDecimal("max_bid_amount"),
            AutobidStatus.valueOf(rs.getString("status")),
            rs.getTimestamp("created_at").toLocalDateTime(),
            rs.getTimestamp("updated_at").toLocalDateTime()
        );
        autobid.setId(rs.getLong("id"));
        return autobid;
    }
}

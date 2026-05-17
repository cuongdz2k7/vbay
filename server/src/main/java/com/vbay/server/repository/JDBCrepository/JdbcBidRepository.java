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

import com.vbay.server.mapper.rowmapper.BidRowMapper;
import com.vbay.server.model.Bid;
import com.vbay.server.repository.BidRepository;
import com.vbay.shared.enums.bid.BidStatus;

public class JdbcBidRepository implements BidRepository {
    private final Connection connection;

    public JdbcBidRepository(Connection connection) {
        this.connection = connection;
    }

    @Override
    public Bid save(Bid bid) throws SQLException {
        String sql = """
            INSERT INTO bids (auction_id, bidder_id, bid_amount, bid_time, bid_source, status)
            VALUES (?, ?, ?, ?, ?, ?)
            """;

        try (PreparedStatement statement = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            statement.setLong(1, bid.getAuctionId());
            statement.setLong(2, bid.getBidderId());
            statement.setBigDecimal(3, bid.getBidAmount());
            statement.setTimestamp(4, Timestamp.valueOf(bid.getBidTime()));
            statement.setString(5, bid.getBidSource().name());
            statement.setString(6, bid.getStatus().name());
            statement.executeUpdate();

            try (ResultSet rs = statement.getGeneratedKeys()) {
                if (rs.next()) {
                    bid.setId(rs.getLong(1));
                    return bid;
                }
            }
        }

        throw new SQLException("Creating bid failed, no ID obtained.");
    }

    @Override
    public Optional<Bid> findWinningBidByAuctionId(long auctionId) throws SQLException {
        String sql = """
            SELECT id, auction_id, bidder_id, bid_amount, bid_time, bid_source, status
            FROM bids
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
                return Optional.of(BidRowMapper.mapBid(rs));
            }
        }
    }

    @Override
    public Optional<Bid> findById(long bidId) throws SQLException {
        String sql = """
            SELECT id, auction_id, bidder_id, bid_amount, bid_time, bid_source, status
            FROM bids
            WHERE id = ?
            """;

        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setLong(1, bidId);
            try (ResultSet rs = statement.executeQuery()) {
                if (!rs.next()) {
                    return Optional.empty();
                }
                return Optional.of(BidRowMapper.mapBid(rs));
            }
        }
    }

    @Override
    public void updateStatus(long bidId, BidStatus newStatus) throws SQLException {
        String sql = "UPDATE bids SET status = ? WHERE id = ?";

        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, newStatus.name());
            statement.setLong(2, bidId);
            statement.executeUpdate();
        }
    }

    @Override
    public void updateStatusesByAuctionIdExceptBid(long auctionId, long excludedBidId, BidStatus newStatus) throws SQLException {
        String sql = "UPDATE bids SET status = ? WHERE auction_id = ? AND id <> ?";

        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, newStatus.name());
            statement.setLong(2, auctionId);
            statement.setLong(3, excludedBidId);
            statement.executeUpdate();
        }
    }

    @Override
    public List<Bid> findLatestBidPerBidderByAuctionId(long auctionId) throws SQLException {
        String sql = """
            SELECT b.id, b.auction_id, b.bidder_id, b.bid_amount, b.bid_time, b.bid_source, b.status
            FROM bids b
            JOIN (
                SELECT bidder_id, MAX(id) AS latest_bid_id
                FROM bids
                WHERE auction_id = ?
                GROUP BY bidder_id
            ) latest ON latest.latest_bid_id = b.id
            ORDER BY b.bid_time DESC
            """; ///join bảng theo lastet_bid_id = b.id
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setLong(1, auctionId);
            try (ResultSet rs = statement.executeQuery()) {
                List<Bid> bids = new ArrayList<>();
                while (rs.next()) {
                    bids.add(BidRowMapper.mapBid(rs));
                }
                return bids;
            }
        }    
    }

    @Override
    public List<Bid> findLatestBidsByBidderId(long bidderId) throws SQLException {
        String sql = """
            SELECT b.id, b.auction_id, b.bidder_id, b.bid_amount, b.bid_time, b.bid_source, b.status
            FROM bids b
            JOIN (
                SELECT auction_id, MAX(id) AS latest_bid_id
                FROM bids
                WHERE bidder_id = ?
                GROUP BY auction_id
            ) latest ON latest.latest_bid_id = b.id
            ORDER BY b.bid_time DESC, b.id DESC
            """;

        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setLong(1, bidderId);
            try (ResultSet rs = statement.executeQuery()) {
                List<Bid> bids = new ArrayList<>();
                while (rs.next()) {
                    bids.add(BidRowMapper.mapBid(rs));
                }
                return bids;
            }
        }
    }

    @Override
    public int markAuctionBidsLost(long auctionId) throws SQLException {
        String sql = """
            UPDATE bids
            SET status = 'LOST'
            WHERE auction_id = ?
            AND status IN ('WINNING', 'OUTBID')
            """;

        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setLong(1, auctionId);
            return statement.executeUpdate();
        }
    }

        
}

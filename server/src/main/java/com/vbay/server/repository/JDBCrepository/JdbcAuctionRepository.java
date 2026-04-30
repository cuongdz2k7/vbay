package com.vbay.server.repository.JDBCrepository;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.util.Optional;

import com.vbay.server.model.Auction;
import com.vbay.server.mapper.rowmapper.AuctionRowMapper;
import com.vbay.server.repository.AuctionRepository;


public class JdbcAuctionRepository implements AuctionRepository {
    private final Connection connection;

    public JdbcAuctionRepository(Connection connection) {
        this.connection = connection;
    }

    @Override
    public Optional<Auction> save(Auction auction) throws SQLException {
        String sql = """
            INSERT INTO auctions (
                product_id, seller_id, title, description, minimum_bid_step,
                starting_price, current_price, reserve_price, buy_now_price,
                starting_time, ending_time, status
            )
            VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
            """;

        try (PreparedStatement statement = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            statement.setLong(1, auction.getProductId());
            statement.setLong(2, auction.getSellerId());
            statement.setString(3, auction.getTitle());
            statement.setString(4, auction.getDescription());
            statement.setBigDecimal(5, auction.getMinimumBidStep());
            statement.setBigDecimal(6, auction.getStartPrice());
            statement.setBigDecimal(7, auction.getCurrentPrice());
            statement.setBigDecimal(8, auction.getReservePrice());
            statement.setBigDecimal(9, auction.getBuyNowPrice());
            statement.setTimestamp(10, Timestamp.valueOf(auction.getStartingTime()));
            statement.setTimestamp(11, Timestamp.valueOf(auction.getEndingTime()));
            statement.setString(12, auction.getStatus().name());

            statement.executeUpdate();

            try (ResultSet rs = statement.getGeneratedKeys()) {
                if (rs.next()) {
                    auction.setId(rs.getLong(1));
                    return Optional.of(auction);
                }
            }
        }

        throw new SQLException("Creating auction failed, no ID obtained.");
    }

    @Override
    public Optional<Auction> findById(long auctionId) throws SQLException {
        String sql = """
            SELECT id, product_id, seller_id, title, description, minimum_bid_step,
                   starting_price, current_price, reserve_price, buy_now_price,
                   starting_time, ending_time, status
            FROM auctions
            WHERE id = ?
            """;

        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setLong(1, auctionId);

            try (ResultSet rs = statement.executeQuery()) {
                if (!rs.next()) {
                    return Optional.empty();
                }

                return Optional.of(AuctionRowMapper.mapAuction(rs));
            }
        }
    }

    @Override
    public Optional<Auction> findBySellerId(long sellerId) throws SQLException {
        String sql = """
            SELECT id, product_id, seller_id, title, description, minimum_bid_step,
                   starting_price, current_price, reserve_price, buy_now_price,
                   starting_time, ending_time, status
            FROM auctions
            WHERE seller_id = ?
            ORDER BY id DESC
            LIMIT 1
            """;

        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setLong(1, sellerId);

            try (ResultSet rs = statement.executeQuery()) {
                if (!rs.next()) {
                    return Optional.empty();
                }

                return Optional.of(AuctionRowMapper.mapAuction(rs));
            }
        }
    }

    @Override
    public Optional<Auction> findByProductId(long productId) throws SQLException {
        String sql = """
            SELECT id, product_id, seller_id, title, description, minimum_bid_step,
                   starting_price, current_price, reserve_price, buy_now_price,
                   starting_time, ending_time, status
            FROM auctions
            WHERE product_id = ?
            ORDER BY id DESC
            LIMIT 1
            """;

        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setLong(1, productId);

            try (ResultSet rs = statement.executeQuery()) {
                if (!rs.next()) {
                    return Optional.empty();
                }

                return Optional.of(AuctionRowMapper.mapAuction(rs));
            }
        }
    }

    @Override
    public boolean existsActiveAuctionByProductId(long productId) throws SQLException {
        String sql = """
            SELECT 1
            FROM auctions
            WHERE product_id = ?
              AND status IN ('SCHEDULED', 'OPEN')
            LIMIT 1
            """;

        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setLong(1, productId);

            try (ResultSet rs = statement.executeQuery()) {
                return rs.next();
            }
        }
    }
}

package com.vbay.server.repository.JDBCrepository;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.Optional;

import com.vbay.server.mapper.rowmapper.AuctionRowMapper;
import com.vbay.server.model.Auction;
import com.vbay.server.repository.AuctionRepository;


public class JdbcAuctionRepository implements AuctionRepository {
    private final Connection connection;

    public JdbcAuctionRepository(Connection connection) {
        this.connection = connection;
    }

    @Override
    public Auction save(Auction auction) throws SQLException {
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
                    return auction;
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
                   starting_time, ending_time, status, version
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
                   starting_time, ending_time, status, version
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
                   starting_time, ending_time, status, version
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

    @Override
    public Optional<Auction> lockAuctionForUpdate (long auctionId) throws SQLException {
        String sql = """
        SELECT id, product_id, seller_id, title, description, minimum_bid_step,
                starting_price, current_price, reserve_price, buy_now_price,
                starting_time, ending_time, status, version
        FROM auctions
        WHERE id = ?
        FOR UPDATE
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
    public LocalDateTime getCurrentDatabaseTime () throws SQLException {
        String sql = "SELECT CURRENT_TIMESTAMP";

        try (PreparedStatement statement = connection.prepareStatement(sql);
             ResultSet rs = statement.executeQuery()) {
            rs.next();
            return rs.getTimestamp(1).toLocalDateTime();
        }
    }

    @Override
    public void syncStatus (long auctionId, LocalDateTime dbNow) throws SQLException {
        activateIfDue(auctionId, dbNow);
        endIfExpired(auctionId, dbNow);
    }
    private void activateIfDue(long auctionId, LocalDateTime dbNow) throws SQLException {
         String sql = """
            UPDATE auctions
            SET status = 'ACTIVE',
                version = version + 1
            WHERE id = ?
            AND status = 'SCHEDULED'
            AND starting_time <= ?
            AND ending_time > ?
        """;
        ///localdatetime before: 10h46
        ///sau 1s update lại database của từng auction
        /// localdateimte now: currentime
        /// 
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
                Timestamp now = Timestamp.valueOf(dbNow);
                statement.setLong(1, auctionId);
                statement.setTimestamp(2, now);
                statement.setTimestamp(3, now);
                statement.executeUpdate(); 
        }    
    }

    private void endIfExpired (long auctionId, LocalDateTime dbNow) throws SQLException {
         String sql = """
            UPDATE auctions
            SET status = 'ENDED',
                version = version + 1
            WHERE id = ?
            AND status IN ('SCHEDULED', 'ACTIVE')
            AND ending_time <= ?
        """;
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
                Timestamp now = Timestamp.valueOf(dbNow);
                statement.setLong(1, auctionId);
                statement.setTimestamp(2, now);
                statement.executeUpdate(); 
        }    
    }

    @Override
    public long updateCurrentBid(long auctionId, BigDecimal currentBid, long winningUserId) throws SQLException {
        String sql = """
            UPDATE auctions
            SET current_price = ?,
                winner_user_id = ?,
                version = version + 1
            WHERE id = ?
            """;

        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setBigDecimal(1, currentBid);
            statement.setLong(2, winningUserId);
            statement.setLong(3, auctionId);
            statement.executeUpdate();
        }
        return findVersionById(auctionId);
    }

    @Override
    public long completeByBuyNow(long auctionId, long buyerId) throws SQLException {
        String sql = """
            UPDATE auctions
            SET current_price = buy_now_price,
                final_price = buy_now_price,
                winner_user_id = ?,
                status = 'ENDED',
                version = version + 1
            WHERE id = ?
            """;

        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setLong(1, buyerId);
            statement.setLong(2, auctionId);
            statement.executeUpdate();
        }
        return findVersionById(auctionId);
    }

    private long findVersionById(long auctionId) throws SQLException {
        String sql = "SELECT version FROM auctions WHERE id = ?";
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setLong(1, auctionId);
            try (ResultSet rs = statement.executeQuery()) {
                if (!rs.next()) {
                    throw new SQLException("Auction not found while reading version");
                }
                return rs.getLong("version");
            }
        }
    }
    

}

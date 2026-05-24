package com.vbay.server.repository.JDBCrepository;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import com.vbay.server.mapper.rowmapper.AuctionRowMapper;
import com.vbay.server.model.Auction;
import com.vbay.server.repository.AuctionRepository;
import com.vbay.server.repository.enums.AuctionTransition;
import com.vbay.server.service.result.AuctionItemResult;
import com.vbay.server.service.result.AuctionListItemResult;
import com.vbay.server.upload.ImageStorageService;
import com.vbay.shared.dto.auctionDTO.AuctionListRequest;
import com.vbay.shared.enums.auction.AuctionStatus;



public class JdbcAuctionRepository implements AuctionRepository {
    private final Connection connection;
    private final ImageStorageService imageStorageService;

    public JdbcAuctionRepository(Connection connection) {
        this(connection, new ImageStorageService());
    }

    public JdbcAuctionRepository(Connection connection, ImageStorageService imageStorageService) {
        this.connection = connection;
        this.imageStorageService = imageStorageService;
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
                   starting_time, ending_time, status, winner_user_id, anti_snipe_extension_count, version
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
                   starting_time, ending_time, status, winner_user_id, anti_snipe_extension_count, version
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
                   starting_time, ending_time, status, winner_user_id, anti_snipe_extension_count, version
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
            WHERE status IN ('SCHEDULED', 'ACTIVE')
            AND product_id = ?
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
                starting_time, ending_time, status, winner_user_id, anti_snipe_extension_count, version
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
    public AuctionTransition syncStatus (long auctionId, LocalDateTime dbNow) throws SQLException {
        if (endIfExpired(auctionId, dbNow)) {
            return AuctionTransition.TIME_EXPIRED;
        }
        else if(activateIfDue(auctionId, dbNow)) {
            return AuctionTransition.STARTED;
        }
        else {
            return AuctionTransition.NO_CHANGE;
        }
    }
    private boolean activateIfDue(long auctionId, LocalDateTime dbNow) throws SQLException {
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
                int rowsAffected = statement.executeUpdate();
                return rowsAffected > 0;
        }    
    }

    private boolean endIfExpired (long auctionId, LocalDateTime dbNow) throws SQLException {
         String sql = """
            SELECT 1
            FROM auctions
            WHERE id = ?
            AND status IN ('SCHEDULED', 'ACTIVE')
            AND ending_time <= ?
            LIMIT 1
        """;
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
                Timestamp now = Timestamp.valueOf(dbNow);
                statement.setLong(1, auctionId);
                statement.setTimestamp(2, now);
                try (ResultSet rs = statement.executeQuery()) {
                    return rs.next();
                }
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
    public long completeByBuyNow(long auctionId, long buyerId, BigDecimal buyNowPrice) throws SQLException {
        String sql = """
            UPDATE auctions
            SET current_price = ?,
                final_price = ?,
                winner_user_id = ?,
                status = 'ENDED',
                version = version + 1
            WHERE id = ?
            """;

        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setBigDecimal(1, buyNowPrice);
            statement.setBigDecimal(2, buyNowPrice);
            statement.setLong(3, buyerId);
            statement.setLong(4, auctionId);
            statement.executeUpdate();
        }
        return findVersionById(auctionId);
    }

    @Override
    public long terminateAuction(long auctionId) throws SQLException {
        String sql = """
            UPDATE auctions
            SET status = 'FAILED',
                winner_user_id = NULL,
                final_price = NULL,
                version = version + 1
            WHERE id = ?
            AND status IN ('SCHEDULED', 'ACTIVE')
            """;

        int rowsAffected;
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setLong(1, auctionId);
            rowsAffected = statement.executeUpdate();
        }
        return rowsAffected > 0 ? findVersionById(auctionId) : 0L;
    }

    @Override
    public long finalizeAuction(long auctionId) throws SQLException {
        String sql = """
            UPDATE auctions
            SET status = 'ENDED',
                final_price = current_price,
                winner_user_id = (
                    SELECT bidder_id
                    FROM bids
                    WHERE auction_id = ?
                    AND status = 'WINNING'
                    ORDER BY bid_time DESC, id DESC
                    LIMIT 1
                ),
                version = version + 1
            WHERE id = ?
            AND status IN ('SCHEDULED', 'ACTIVE')
            AND EXISTS (
                SELECT 1
                FROM bids
                WHERE auction_id = ?
                AND status = 'WINNING'
            )
            """;

        int rowsAffected;
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setLong(1, auctionId);
            statement.setLong(2, auctionId);
            statement.setLong(3, auctionId);
            rowsAffected = statement.executeUpdate();
        }

        return rowsAffected > 0 ? findVersionById(auctionId) : 0L;
    }

    @Override
    public List<Auction> findPendingSchedules() throws SQLException {
        String sql = """
            SELECT id, product_id, seller_id, title, description, minimum_bid_step,
                   starting_price, current_price, reserve_price, buy_now_price,
                   starting_time, ending_time, status, winner_user_id, anti_snipe_extension_count, version
            FROM auctions
            WHERE status IN ('SCHEDULED', 'ACTIVE')
            ORDER BY starting_time ASC, ending_time ASC, id ASC
            """;

        try (PreparedStatement statement = connection.prepareStatement(sql);
             ResultSet rs = statement.executeQuery()) {
            return AuctionRowMapper.mapAuctions(rs);
        }
    }

    @Override
    public List<Auction> findRecoverableSchedules(LocalDateTime dbNow) throws SQLException {
        String sql = """
            SELECT id, product_id, seller_id, title, description, minimum_bid_step,
                   starting_price, current_price, reserve_price, buy_now_price,
                   starting_time, ending_time, status, winner_user_id, anti_snipe_extension_count, version
            FROM auctions
            WHERE (status = 'SCHEDULED' AND starting_time <= ?)
               OR (status IN ('SCHEDULED', 'ACTIVE') AND ending_time <= ?)
            ORDER BY ending_time ASC, starting_time ASC, id ASC
            """;

        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            Timestamp now = Timestamp.valueOf(dbNow);
            statement.setTimestamp(1, now);
            statement.setTimestamp(2, now);
            try (ResultSet rs = statement.executeQuery()) {
                return AuctionRowMapper.mapAuctions(rs);
            }
        }
    }

    @Override
    public List<AuctionListItemResult> findAuctionList(AuctionListRequest request) throws SQLException {
        StringBuilder sql = new StringBuilder("""
            SELECT
                a.id AS auction_id,
                a.version AS auction_version,
                a.product_id,
                a.seller_id,
                a.title,
                a.description,
                p.name AS product_name,
                p.category_id,
                a.status,
                a.starting_price,
                a.current_price,
                a.minimum_bid_step,
                a.buy_now_price,
                a.winner_user_id,
                CASE
                    WHEN a.reserve_price IS NULL THEN NULL
                    WHEN a.current_price >= a.reserve_price THEN TRUE
                    ELSE FALSE
                END AS reserve_met,
                CASE
                    WHEN a.anti_snipe_extension_count > 0 THEN TRUE
                    ELSE FALSE
                END AS anti_snipe_extended,
                (
                    SELECT image_url
                    FROM product_images
                    WHERE product_id = p.id
                    ORDER BY is_thumbnail DESC, id ASC
                    LIMIT 1
                ) AS thumbnail_url,
                a.starting_time,
                a.ending_time,
                a.updated_at
            FROM auctions a
            JOIN products p ON p.id = a.product_id
            WHERE 1 = 1
            """);

        List<Object> parameters = new ArrayList<>();
        if (request.getStatus() != null) {
            sql.append(" AND a.status = ?");
            parameters.add(request.getStatus());
        }
        if (request.getCategoryId() != null) {
            sql.append(" AND p.category_id = ?");
            parameters.add(request.getCategoryId());
        }
        if (request.getSellerId() != null) {
            sql.append(" AND a.seller_id = ?");
            parameters.add(request.getSellerId());
        }

        if ("SCHEDULED".equals(request.getStatus())) {
            sql.append(" ORDER BY a.starting_time ASC, a.id ASC");
        } else {
            sql.append(" ORDER BY a.starting_time DESC, a.id DESC");
        }

        if (request.getLimit() != null) {
            sql.append(" LIMIT ?");
            parameters.add(request.getLimit());
        }

        try (PreparedStatement statement = connection.prepareStatement(sql.toString())) {
            for (int i = 0; i < parameters.size(); i++) {
                statement.setObject(i + 1, parameters.get(i));
            }

            try (ResultSet rs = statement.executeQuery()) {
                List<AuctionListItemResult> items = new ArrayList<>();
                while (rs.next()) {
                    String thumbnailUrl = imageStorageService.toPublicThumbnailUrl(rs.getString("thumbnail_url"));
                    items.add(AuctionRowMapper.mapAuctionListItem(rs, thumbnailUrl));
                }
                return items;
            }
        }
    }

    @Override
    public Optional<AuctionListItemResult> findAuctionListItemById(long auctionId) throws SQLException {
        String sql = """
            SELECT
                a.id AS auction_id,
                a.version AS auction_version,
                a.product_id,
                a.seller_id,
                a.title,
                a.description,
                p.name AS product_name,
                p.category_id,
                a.status,
                a.starting_price,
                a.current_price,
                a.minimum_bid_step,
                a.buy_now_price,
                a.winner_user_id,
                CASE
                    WHEN a.reserve_price IS NULL THEN NULL
                    WHEN a.current_price >= a.reserve_price THEN TRUE
                    ELSE FALSE
                END AS reserve_met,
                CASE
                    WHEN a.anti_snipe_extension_count > 0 THEN TRUE
                    ELSE FALSE
                END AS anti_snipe_extended,
                (
                    SELECT image_url
                    FROM product_images
                    WHERE product_id = p.id
                    ORDER BY is_thumbnail DESC, id ASC
                    LIMIT 1
                ) AS thumbnail_url,
                a.starting_time,
                a.ending_time,
                a.updated_at
            FROM auctions a
            JOIN products p ON p.id = a.product_id
            WHERE a.id = ?
            """;

        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setLong(1, auctionId);
            try (ResultSet rs = statement.executeQuery()) {
                if (!rs.next()) {
                    return Optional.empty();
                }
                String thumbnailUrl = imageStorageService.toPublicThumbnailUrl(rs.getString("thumbnail_url"));
                return Optional.of(AuctionRowMapper.mapAuctionListItem(rs, thumbnailUrl));
            }
        }
    }

    @Override
    public Optional<AuctionItemResult> findAuctionItemById(long auctionId) throws SQLException {
        String sql = """
            SELECT
                a.id AS auction_id,
                a.version AS auction_version,
                a.product_id,
                a.seller_id,
                a.title,
                a.description,
                p.name AS product_name,
                p.category_id,
                a.status,
                a.starting_price,
                a.current_price,
                a.minimum_bid_step,
                a.buy_now_price,
                a.winner_user_id,
                CASE
                    WHEN a.reserve_price IS NULL THEN NULL
                    WHEN a.current_price >= a.reserve_price THEN TRUE
                    ELSE FALSE
                END AS reserve_met,
                CASE
                    WHEN a.anti_snipe_extension_count > 0 THEN TRUE
                    ELSE FALSE
                END AS anti_snipe_extended,
                (
                    SELECT image_url
                    FROM product_images
                    WHERE product_id = p.id
                    ORDER BY is_thumbnail DESC, id ASC
                    LIMIT 1
                ) AS thumbnail_url,
                a.starting_time,
                a.ending_time,
                a.updated_at
            FROM auctions a
            JOIN products p ON p.id = a.product_id
            WHERE a.id = ?
            """;

        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setLong(1, auctionId);
            try (ResultSet rs = statement.executeQuery()) {
                if (!rs.next()) {
                    return Optional.empty();
                }
                List<String> productImages = findProductImageUrls(rs.getLong("product_id"));
                String thumbnailUrl = imageStorageService.toPublicThumbnailUrl(rs.getString("thumbnail_url"));
                return Optional.of(AuctionRowMapper.mapAuctionItem(rs, productImages, thumbnailUrl));
            }
        }
    }

    private List<String> findProductImageUrls(long productId) throws SQLException {
        String sql = """
            SELECT image_url
            FROM product_images
            WHERE product_id = ?
            ORDER BY is_thumbnail DESC, id ASC
            """;

        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setLong(1, productId);
            try (ResultSet rs = statement.executeQuery()) {
                List<String> imageUrls = new ArrayList<>();
                while (rs.next()) {
                    String publicUrl = imageStorageService.toPublicImageUrl(rs.getString("image_url"));
                    if (publicUrl != null && !publicUrl.isBlank()) {
                        imageUrls.add(publicUrl);
                    }
                }
                return imageUrls;
            }
        }
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
    @Override
    public long applyAntiSnipeExtension(long auctionId, LocalDateTime endingTime) throws SQLException {
        String sql = """
            UPDATE auctions
            SET ending_time = ?,
                anti_snipe_extension_count = anti_snipe_extension_count + 1,
                version = version + 1
            WHERE id = ?
            AND ending_time < ?
            """;

        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            java.sql.Timestamp timestamp = java.sql.Timestamp.valueOf(endingTime);
            statement.setTimestamp(1, timestamp);
            statement.setLong(2, auctionId);
            statement.setTimestamp(3, timestamp);
            statement.executeUpdate();
        }

        return findVersionById(auctionId);
    }

    @Override
    public void deleteById(long auctionId) throws SQLException {
        String sql = "DELETE FROM auctions WHERE id = ?";
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setLong(1, auctionId);
            statement.executeUpdate();
        }
    }

    @Override
    public void updateStatus(long auctionId, AuctionStatus status) throws SQLException {
        String sql = "UPDATE auctions SET status = ?, version = version + 1 WHERE id = ?";
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, status.name());
            statement.setLong(2, auctionId);
            statement.executeUpdate();
        }
    }

    @Override
    public List<Auction> findAllForAdmin() throws SQLException {
        String sql = """
            SELECT id, product_id, seller_id, title, description, minimum_bid_step,
                   starting_price, current_price, reserve_price, buy_now_price,
                   starting_time, ending_time, status, winner_user_id, version
            FROM auctions
            ORDER BY id DESC
            """;
        try (PreparedStatement statement = connection.prepareStatement(sql);
             ResultSet rs = statement.executeQuery()) {
            return AuctionRowMapper.mapAuctions(rs);
        }
    }
}

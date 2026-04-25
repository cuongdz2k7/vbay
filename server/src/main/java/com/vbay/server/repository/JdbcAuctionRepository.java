package com.vbay.server.repository;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Optional;

import com.vbay.server.Model.Auction;
import com.vbay.server.databaseManager.DatabaseConnection;


public class JdbcAuctionRepository {
    @Override
    Optional<Auction> findByProductId(long productId) throws SQLException {
        String sql = """
            SELECT id, product_id, seller_id, starting_price, buy_now_price, reserve_price, starting_time, ending_time
            FROM auctions
            WHERE product_id = ?
            """;

        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setLong(1, auctionId);

            try (ResultSet rs = statement.executeQuery()) {
                if (!rs.next()) {
                    return Optional.empty();
                }

                return Optional.of(mapAuction(rs));
            }
        }
    }
}

package com.vbay.server.mapper.rowmapper;

import java.sql.ResultSet;
import java.sql.SQLException;

import com.vbay.server.model.Auction;
import com.vbay.shared.enums.auction.AuctionStatus;


public class AuctionRowMapper {
    private AuctionRowMapper() {}

    public static Auction mapAuction(ResultSet rs) throws SQLException {
        Auction auction = new Auction(
            rs.getLong("id"),
            rs.getLong("product_id"),
            rs.getLong("seller_id"),
            rs.getString("title"),
            rs.getString("description"),
            rs.getBigDecimal("starting_price"),
            rs.getBigDecimal("current_price"),
            rs.getBigDecimal("reserve_price"),
            rs.getBigDecimal("buy_now_price"),
            rs.getBigDecimal("minimum_bid_step"),
            rs.getTimestamp("starting_time").toLocalDateTime(),
            rs.getTimestamp("ending_time").toLocalDateTime(),
            AuctionStatus.valueOf(rs.getString("status"))
        );
        auction.setVersion(rs.getLong("version"));
        return auction;
    }
}

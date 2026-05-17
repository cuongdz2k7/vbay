package com.vbay.server.mapper.rowmapper;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import com.vbay.server.model.Auction;
import com.vbay.server.service.result.AuctionItemResult;
import com.vbay.server.service.result.AuctionListItemResult;
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
            AuctionStatus.valueOf(rs.getString("status")),
            (Long) rs.getObject("winner_user_id")
        );
        auction.setVersion(rs.getLong("version"));
        return auction;
    }

    private static Boolean nullableBoolean(ResultSet rs, String columnName) throws SQLException {
        boolean value = rs.getBoolean(columnName);
        return rs.wasNull() ? null : value;
    }

    public static AuctionItemResult mapAuctionItem(ResultSet rs, List<String> imageUrls, String thumbnailUrl) throws SQLException {
        long productId = rs.getLong("product_id");
        return new AuctionItemResult(
            rs.getLong("auction_id"),
            rs.getLong("auction_version"),
            productId,
            rs.getLong("seller_id"),
            rs.getString("title"),
            rs.getString("description"),
            rs.getString("product_name"),
            rs.getLong("category_id"),
            rs.getString("status"),
            rs.getBigDecimal("starting_price"),
            rs.getBigDecimal("current_price"),
            rs.getBigDecimal("minimum_bid_step"),
            rs.getBigDecimal("buy_now_price"),
            (Long) rs.getObject("winner_user_id"),
            nullableBoolean(rs, "reserve_met"),
            thumbnailUrl,
            imageUrls,
            rs.getTimestamp("starting_time").toLocalDateTime(),
            rs.getTimestamp("ending_time").toLocalDateTime(),
            rs.getTimestamp("updated_at").toLocalDateTime()
        );
    }

    public static AuctionListItemResult mapAuctionListItem(ResultSet rs, String thumbnailUrl) throws SQLException {
        return new AuctionListItemResult(
            rs.getLong("auction_id"),
            rs.getLong("auction_version"),
            rs.getLong("product_id"),
            rs.getLong("seller_id"),
            rs.getString("title"),
            rs.getString("description"),
            rs.getString("product_name"),
            rs.getLong("category_id"),
            rs.getString("status"),
            rs.getBigDecimal("starting_price"),
            rs.getBigDecimal("current_price"),
            rs.getBigDecimal("minimum_bid_step"),
            rs.getBigDecimal("buy_now_price"),
            (Long) rs.getObject("winner_user_id"),
            nullableBoolean(rs, "reserve_met"),
            thumbnailUrl,
            rs.getTimestamp("starting_time").toLocalDateTime(),
            rs.getTimestamp("ending_time").toLocalDateTime(),
            rs.getTimestamp("updated_at").toLocalDateTime()
        );
    }


    public static List<Auction> mapAuctions(ResultSet rs) throws SQLException {
        List<Auction> auctions = new ArrayList<>();
        while (rs.next()) {
            auctions.add(AuctionRowMapper.mapAuction(rs));
        }
        return auctions;
    }

}

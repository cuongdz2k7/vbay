package com.vbay.server.mapper.rowmapper;

import java.sql.ResultSet;
import java.sql.SQLException;

import com.vbay.server.model.Bid;
import com.vbay.shared.status.BidSource;
import com.vbay.shared.status.shared_status.BidStatus;

public class BidRowMapper {
    public static Bid mapBid(ResultSet rs) throws SQLException {
        Bid bid = new Bid(
            rs.getLong("auction_id"),
            rs.getLong("bidder_id"),
            rs.getBigDecimal("bid_amount"),
            rs.getTimestamp("bid_time").toLocalDateTime(),
            BidStatus.valueOf(rs.getString("status")),
            BidSource.valueOf(rs.getString("bid_source"))
        );
        bid.setId(rs.getLong("id"));
        return bid;
    }
}

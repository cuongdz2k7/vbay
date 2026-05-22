package com.vbay.server.repository;

import java.math.BigDecimal;
import java.sql.SQLException;
import java.util.Optional;

import com.vbay.server.model.Autobid;
import com.vbay.server.service.bid.enums.AutobidStatus;

public interface AutobidRepository {
    Autobid save(Autobid autobid) throws SQLException;
    Optional<Autobid> findWinningByAuctionId(long auctionId) throws SQLException;
    Optional<Autobid> findByAuctionIdAndUserId(long auctionId, long userId) throws SQLException;
    void updateStatus(long autobidId, AutobidStatus status) throws SQLException;
    void updateMaxBidAmount(long autobidId, BigDecimal maxBidAmount) throws SQLException;
}

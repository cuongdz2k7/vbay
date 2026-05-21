package com.vbay.server.repository;

import java.math.BigDecimal;
import java.sql.SQLException;
import java.util.Optional;

import com.vbay.server.model.Autobid;
import com.vbay.server.service.bid.autobid.enums.AutobidStatus;
import com.vbay.server.service.bid.autobid.model.AutobidChange;

public interface AutobidRepository {
    Autobid save(Autobid autobid) throws SQLException;
    Optional<Autobid> findWinningByAuctionId(long auctionId) throws SQLException;
    void update(AutobidChange change) throws SQLException;
    void updateStatus(long autobidId, AutobidStatus status) throws SQLException;
    void updateMaxBidAmount(long autobidId, BigDecimal maxBidAmount) throws SQLException;
}

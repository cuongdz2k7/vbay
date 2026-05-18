package com.vbay.server.repository;

import java.sql.SQLException;
import java.util.Optional;

import com.vbay.server.service.bid.autobid.enums.AutobidStatus;
import com.vbay.server.service.bid.autobid.model.Autobid;
import com.vbay.server.service.bid.autobid.model.AutobidChange;

public interface AutobidRepository {
    Optional<Autobid> findWinningByAuctionId(long auctionId) throws SQLException;
    void update(AutobidChange change) throws SQLException;
    void updateStatus(long autobidId, AutobidStatus status) throws SQLException;
}

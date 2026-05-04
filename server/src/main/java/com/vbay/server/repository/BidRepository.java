package com.vbay.server.repository;
import java.sql.SQLException;
import java.util.Optional;

import com.vbay.server.model.Bid;
import com.vbay.shared.status.shared_status.BidStatus;


public interface BidRepository {
    Bid save (Bid bid) throws SQLException;
    Optional<Bid> findWinningBidByAuctionId(long auctionId) throws SQLException;
    Optional<Bid> findById(long bidId) throws SQLException;
    void updateStatus(long bidId, BidStatus newStatus) throws SQLException;
}

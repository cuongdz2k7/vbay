package com.vbay.server.repository;
import java.sql.SQLException;
import java.util.List;
import java.util.Optional;

import com.vbay.server.model.Bid;
import com.vbay.shared.enums.bid.BidStatus;


public interface BidRepository {
    Bid save (Bid bid) throws SQLException;
    Optional<Bid> findWinningBidByAuctionId(long auctionId) throws SQLException;
    Optional<Bid> findById(long bidId) throws SQLException;
    void updateStatus(long bidId, BidStatus newStatus) throws SQLException;
    void updateStatusesByAuctionIdExceptBid(long auctionId, long excludedBidId, BidStatus newStatus) throws SQLException;
    List<Bid> findLatestBidPerBidderByAuctionId(long auctionId) throws SQLException;
    List<Bid> findLatestBidsByBidderId(long bidderId) throws SQLException;
    List<Bid> findBidsByAuctionId(long auctionId) throws SQLException;
    int markAuctionBidsLost(long auctionId) throws SQLException;

}

package com.vbay.server.repository;

import java.math.BigDecimal;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import com.vbay.server.model.Auction;
import com.vbay.shared.dto.auctionDTO.AuctionListRequest;
import com.vbay.shared.dto.realtimeDTO.payload.AuctionListItemPayload;

public interface AuctionRepository {
    Auction save(Auction auction) throws SQLException;
    Optional<Auction> findById(long auctionId) throws SQLException;
    Optional<Auction> findBySellerId(long sellerId) throws SQLException;
    Optional<Auction> findByProductId(long productId) throws SQLException;
    boolean existsActiveAuctionByProductId(long productId) throws SQLException;
    Optional<Auction> lockAuctionForUpdate (long auctionId) throws SQLException;
    LocalDateTime getCurrentDatabaseTime () throws SQLException;
    void syncStatus (long auctionId, LocalDateTime dbNow) throws SQLException;
    long updateCurrentBid(long auctionId, 
                            BigDecimal currentBid, 
                            long winningUserId) throws SQLException;   
    long completeByBuyNow(long auctionId, long buyerId) throws SQLException;
    List<AuctionListItemPayload> findAuctionList(AuctionListRequest request) throws SQLException;
    
}

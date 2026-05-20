package com.vbay.server.repository;

import java.math.BigDecimal;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import com.vbay.server.model.Auction;
import com.vbay.server.repository.enums.AuctionTransition;
import com.vbay.server.service.result.AuctionItemResult;
import com.vbay.server.service.result.AuctionListItemResult;
import com.vbay.shared.dto.auctionDTO.AuctionListRequest;
import com.vbay.shared.enums.auction.AuctionStatus;

public interface AuctionRepository {
    Auction save(Auction auction) throws SQLException;
    Optional<Auction> findById(long auctionId) throws SQLException;
    Optional<Auction> findBySellerId(long sellerId) throws SQLException;
    Optional<Auction> findByProductId(long productId) throws SQLException;
    boolean existsActiveAuctionByProductId(long productId) throws SQLException;
    Optional<Auction> lockAuctionForUpdate (long auctionId) throws SQLException;
    LocalDateTime getCurrentDatabaseTime () throws SQLException;
    AuctionTransition syncStatus (long auctionId, LocalDateTime dbNow) throws SQLException;
    long updateCurrentBid(long auctionId, 
                            BigDecimal currentBid, 
                            long winningUserId) throws SQLException;   
    long completeByBuyNow(long auctionId, long buyerId) throws SQLException;
    long terminateAuction(long auctionId) throws SQLException;
    long finalizeAuction(long auctionId) throws SQLException;
    List<Auction> findPendingSchedules() throws SQLException;
    List<Auction> findRecoverableSchedules(LocalDateTime dbNow) throws SQLException;
    Optional<AuctionListItemResult> findAuctionListItemById(long auctionId) throws SQLException;
    Optional<AuctionItemResult> findAuctionItemById(long auctionId) throws SQLException;
    List<AuctionListItemResult> findAuctionList(AuctionListRequest request) throws SQLException;
    void deleteById(long auctionId) throws SQLException;
    void updateStatus(long auctionId, AuctionStatus status) throws SQLException;
    List<Auction> findAllForAdmin() throws SQLException;
}

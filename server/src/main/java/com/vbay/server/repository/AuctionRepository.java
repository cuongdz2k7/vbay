package com.vbay.server.repository;

import java.sql.SQLException;
import java.util.Optional;

import com.vbay.server.Model.Auction;

public interface AuctionRepository {
    Optional<Auction> save(Auction auction) throws SQLException;
    Optional<Auction> findById(long auctionId) throws SQLException;
    Optional<Auction> findBySellerId(long sellerId) throws SQLException;
    Optional<Auction> findByProductId(long productId) throws SQLException;
    boolean existsActiveAuctionByProductId(long productId) throws SQLException;
    
}

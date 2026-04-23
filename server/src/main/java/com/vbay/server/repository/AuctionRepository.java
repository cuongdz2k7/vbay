package com.vbay.server.repository;

import java.sql.SQLException;
import java.util.Optional;

import com.vbay.server.Model.Auction;
import com.vbay.server.Model.User;

public interface AuctionRepository {
    Auction save(Auction auction) throws SQLException;
    Optional<Auction> findById(long auctionId) throws SQLException;
    Optional<User> findBySellerId(long sellerId) throws SQLException;
    Optional<User> findByProductId(long productId) throws SQLException;
    boolean existsActiveAuctionByProductId(long productId) throws SQLException;
    
}

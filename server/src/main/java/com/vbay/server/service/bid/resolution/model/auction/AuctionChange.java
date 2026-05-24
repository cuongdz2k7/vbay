package com.vbay.server.service.bid.resolution.model.auction;

import java.sql.SQLException;

import com.vbay.server.repository.AuctionRepository;

public interface AuctionChange {
    long getAuctionId();

    long apply(AuctionRepository auctionRepository) throws SQLException;
}
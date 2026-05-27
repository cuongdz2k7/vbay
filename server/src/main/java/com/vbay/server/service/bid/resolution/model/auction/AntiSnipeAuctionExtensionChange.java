package com.vbay.server.service.bid.resolution.model.auction;

import java.sql.SQLException;
import java.time.LocalDateTime;

import com.vbay.server.repository.AuctionRepository;

public class AntiSnipeAuctionExtensionChange implements AuctionChange {
    private final long auctionId;
    private final LocalDateTime endingTime;

    public AntiSnipeAuctionExtensionChange(long auctionId, LocalDateTime endingTime) {
        this.auctionId = auctionId;
        this.endingTime = endingTime;
    }

    @Override
    public long getAuctionId() {
        return auctionId;
    }

    @Override
    public long apply(AuctionRepository auctionRepository) throws SQLException {
        return auctionRepository.applyAntiSnipeExtension(auctionId, endingTime);
    }
}
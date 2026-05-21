package com.vbay.server.service.bid.resolution.model.auction;

import java.math.BigDecimal;
import java.sql.SQLException;

import com.vbay.server.repository.AuctionRepository;


public class CurrentBidAuctionChange implements AuctionChange {
    private final long auctionId;
    private final Long winnerUserId;
    private final BigDecimal currentPrice;

    public CurrentBidAuctionChange(long auctionId, Long winnerUserId, BigDecimal currentPrice) {
        this.auctionId = auctionId;
        this.winnerUserId = winnerUserId;
        this.currentPrice = currentPrice;
    }

    @Override
    public long getAuctionId() {
        return auctionId;
    }

    @Override
    public long apply(AuctionRepository auctionRepository) throws SQLException {
        return auctionRepository.updateCurrentBid(auctionId, currentPrice, winnerUserId);
    }
}
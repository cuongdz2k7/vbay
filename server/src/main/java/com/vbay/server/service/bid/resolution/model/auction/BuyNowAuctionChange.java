package com.vbay.server.service.bid.resolution.model.auction;

import java.math.BigDecimal;
import java.sql.SQLException;

import com.vbay.server.repository.AuctionRepository;


public class BuyNowAuctionChange implements AuctionChange {
    private final long auctionId;
    private final long buyerId;
    private final BigDecimal buyNowPrice;

    public BuyNowAuctionChange(long auctionId, long buyerId, BigDecimal buyNowPrice) {
        this.auctionId = auctionId;
        this.buyerId = buyerId;
        this.buyNowPrice = buyNowPrice;
    }

    @Override
    public long getAuctionId() {
        return auctionId;
    }

    @Override
    public long apply(AuctionRepository auctionRepository) throws SQLException {
        return auctionRepository.completeByBuyNow(auctionId, buyerId, buyNowPrice);
    }
}
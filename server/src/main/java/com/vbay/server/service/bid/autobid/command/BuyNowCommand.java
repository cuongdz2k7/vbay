package com.vbay.server.service.bid.autobid.command;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public class BuyNowCommand {
    private final long auctionId;
    private final long buyerUserId;
    private final BigDecimal buyNowPrice;
    private final LocalDateTime boughtAt;

    public BuyNowCommand(long auctionId, long buyerUserId, BigDecimal buyNowPrice, LocalDateTime boughtAt) {
        this.auctionId = auctionId;
        this.buyerUserId = buyerUserId;
        this.buyNowPrice = buyNowPrice;
        this.boughtAt = boughtAt;
    }

    public long getAuctionId() {
        return auctionId;
    }

    public long getBuyerUserId() {
        return buyerUserId;
    }

    public BigDecimal getBuyNowPrice() {
        return buyNowPrice;
    }

    public LocalDateTime getBoughtAt() {
        return boughtAt;
    }
}

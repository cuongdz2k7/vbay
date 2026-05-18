package com.vbay.server.service.bid.autobid.command;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public class IncreaseAutobidCommand {
    private final long auctionId;
    private final long userId;
    private final BigDecimal newMaxBidAmount;
    private final LocalDateTime increasedAt;

    public IncreaseAutobidCommand(long auctionId, long userId, BigDecimal newMaxBidAmount, LocalDateTime increasedAt) {
        this.auctionId = auctionId;
        this.userId = userId;
        this.newMaxBidAmount = newMaxBidAmount;
        this.increasedAt = increasedAt;
    }

    public long getAuctionId() {
        return auctionId;
    }

    public long getUserId() {
        return userId;
    }

    public BigDecimal getNewMaxBidAmount() {
        return newMaxBidAmount;
    }

    public LocalDateTime getIncreasedAt() {
        return increasedAt;
    }
}

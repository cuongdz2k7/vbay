package com.vbay.server.service.bid.command;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public class RegisterAutobidCommand {
    private final long auctionId;
    private final long userId;
    private final BigDecimal maxBidAmount;
    private final LocalDateTime registeredAt;

    public RegisterAutobidCommand(long auctionId, long userId, BigDecimal maxBidAmount, LocalDateTime registeredAt) {
        this.auctionId = auctionId;
        this.userId = userId;
        this.maxBidAmount = maxBidAmount;
        this.registeredAt = registeredAt;
    }

    public long getAuctionId() {
        return auctionId;
    }

    public long getUserId() {
        return userId;
    }

    public BigDecimal getMaxBidAmount() {
        return maxBidAmount;
    }

    public LocalDateTime getRegisteredAt() {
        return registeredAt;
    }
}

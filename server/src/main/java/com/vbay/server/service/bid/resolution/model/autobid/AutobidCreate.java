package com.vbay.server.service.bid.resolution.model.autobid;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import com.vbay.server.service.bid.enums.AutobidStatus;

public class AutobidCreate {
    private Long id;
    private final long auctionId;
    private final long userId;
    private final BigDecimal maxBidAmount;
    private final AutobidStatus status;
    private final LocalDateTime createdAt;
    private final LocalDateTime updatedAt;

    public AutobidCreate(
            long auctionId,
            long userId,
            BigDecimal maxBidAmount,
            AutobidStatus status,
            LocalDateTime createdAt,
            LocalDateTime updatedAt) {
        this.auctionId = auctionId;
        this.userId = userId;
        this.maxBidAmount = maxBidAmount;
        this.status = status;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public long getAuctionId() { return auctionId; }
    public long getUserId() { return userId; }
    public BigDecimal getMaxBidAmount() { return maxBidAmount; }
    public AutobidStatus getStatus() { return status; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public LocalDateTime getUpdatedAt() { return updatedAt; }
}
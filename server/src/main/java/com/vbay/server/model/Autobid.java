package com.vbay.server.model;
import java.math.BigDecimal;
import java.time.LocalDateTime;

import com.vbay.server.service.bid.enums.AutobidStatus;

/*
Không lưu currentAmount 
vì có thể check:
    auction.winnerUserId == userId
    => auction.currentPrice
*/
public class Autobid {
    private long id;
    private long auctionId;
    private long userId;
    private BigDecimal maxBidAmount;
    private AutobidStatus status;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public Autobid(long auctionId, 
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


    // Getters and setters
    public long getId() {
        return id;
    }
    public void setId(long id) {
        this.id = id;
    }
    public long getAuctionId() {
        return auctionId;
    }
    public void setAuctionId(long auctionId) {
        this.auctionId = auctionId;
    }
    public long getUserId() {
        return userId;
    }
    public void setUserId(long userId) {
        this.userId = userId;
    }
    public BigDecimal getMaxBidAmount() {
        return maxBidAmount;
    }
    public void setMaxBidAmount(BigDecimal maxBidAmount) {
        this.maxBidAmount = maxBidAmount;
    }
    public AutobidStatus getStatus() {
        return status;
    }
    public void setStatus(AutobidStatus status) {
        this.status = status;
    }
    public LocalDateTime getCreatedAt() {
        return createdAt;
    }
    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }
    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }
}
package com.vbay.shared.dto.realtimeDTO.payload;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public class AuctionStateUpdatedPayload {
    private long auctionId;
    private long auctionVersion;
    private String status;
    private BigDecimal currentPrice;
    private BigDecimal nextMinimumBid;
    private Long winningUserId;
    private LocalDateTime startingTime;
    private LocalDateTime endingTime;
    private LocalDateTime updatedAt;

    public AuctionStateUpdatedPayload() {
    }

    public AuctionStateUpdatedPayload(
            long auctionId,
            long auctionVersion,
            String status,
            BigDecimal currentPrice,
            BigDecimal nextMinimumBid,
            Long winningUserId,
            LocalDateTime startingTime,
            LocalDateTime endingTime,
            LocalDateTime updatedAt) {
        this.auctionId = auctionId;
        this.auctionVersion = auctionVersion;
        this.status = status;
        this.currentPrice = currentPrice;
        this.nextMinimumBid = nextMinimumBid;
        this.winningUserId = winningUserId;
        this.startingTime = startingTime;
        this.endingTime = endingTime;
        this.updatedAt = updatedAt;
    }

    public long getAuctionId() {
        return auctionId;
    }

    public void setAuctionId(long auctionId) {
        this.auctionId = auctionId;
    }

    public long getAuctionVersion() {
        return auctionVersion;
    }

    public void setAuctionVersion(long auctionVersion) {
        this.auctionVersion = auctionVersion;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public BigDecimal getCurrentPrice() {
        return currentPrice;
    }

    public void setCurrentPrice(BigDecimal currentPrice) {
        this.currentPrice = currentPrice;
    }

    public BigDecimal getNextMinimumBid() {
        return nextMinimumBid;
    }

    public void setNextMinimumBid(BigDecimal nextMinimumBid) {
        this.nextMinimumBid = nextMinimumBid;
    }

    public Long getWinningUserId() {
        return winningUserId;
    }

    public void setWinnerUserId(Long winnerUserId) {
        this.winningUserId = winnerUserId;
    }

    public LocalDateTime getStartingTime() {
        return startingTime;
    }

    public void setStartingTime(LocalDateTime startingTime) {
        this.startingTime = startingTime;
    }

    public LocalDateTime getEndingTime() {
        return endingTime;
    }

    public void setEndingTime(LocalDateTime endingTime) {
        this.endingTime = endingTime;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }
}

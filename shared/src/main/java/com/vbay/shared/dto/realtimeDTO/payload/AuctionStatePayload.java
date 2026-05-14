package com.vbay.shared.dto.realtimeDTO.payload;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import com.vbay.shared.enums.realtime.AuctionStateChangeReason;

public class AuctionStatePayload {
    private long auctionId;
    private long auctionVersion;
    private String status;
    private BigDecimal currentPrice;
    private Boolean reserveMet;
    private Long winnerUserId;
    private LocalDateTime startingTime;
    private LocalDateTime endingTime;
    private LocalDateTime updatedAt;
    private LocalDateTime endedAt;
    private AuctionStateChangeReason stateChangeReason;

    public AuctionStatePayload() {
    }

    public AuctionStatePayload(
            long auctionId,
            long auctionVersion,
            String status,
            BigDecimal currentPrice,
            BigDecimal nextMinimumBid,
            Boolean reserveMet,
            Long winnerUserId,
            LocalDateTime startingTime,
            LocalDateTime endingTime,
            LocalDateTime updatedAt,
            LocalDateTime endedAt,
            AuctionStateChangeReason stateChangeReason) {
        this.auctionId = auctionId;
        this.auctionVersion = auctionVersion;
        this.status = status;
        this.currentPrice = currentPrice;
        this.reserveMet = reserveMet;
        this.winnerUserId = winnerUserId;
        this.startingTime = startingTime;
        this.endingTime = endingTime;
        this.updatedAt = updatedAt;
        this.endedAt = endedAt;
        this.stateChangeReason = stateChangeReason;
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

    public Boolean getReserveMet() {
        return reserveMet;
    }

    public void setReserveMet(Boolean reserveMet) {
        this.reserveMet = reserveMet;
    }

    public Long getWinningUserId() {
        return winnerUserId;
    }

    public Long getWinnerUserId() {
        return winnerUserId;
    }

    public void setWinnerUserId(Long winnerUserId) {
        this.winnerUserId = winnerUserId;
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

    public LocalDateTime getEndedAt() {
        return endedAt;
    }

    public void setEndedAt(LocalDateTime endedAt) {
        this.endedAt = endedAt;
    }

    public AuctionStateChangeReason getStateChangeReason() {
        return stateChangeReason;
    }

    public void setStateChangeReason(AuctionStateChangeReason stateChangeReason) {
        this.stateChangeReason = stateChangeReason;
    }
}

package com.vbay.shared.dto.realtimeDTO.payload;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public class AuctionEndedPayload {
    private long auctionId;
    private long auctionVersion;
    private String status;
    private BigDecimal finalPrice;
    private Long winnerUserId;
    private LocalDateTime endedAt;
    private String endReason;

    public AuctionEndedPayload() {
    }

    public AuctionEndedPayload(
            long auctionId,
            long auctionVersion,
            String status,
            BigDecimal finalPrice,
            Long winnerUserId,
            LocalDateTime endedAt,
            String endReason) {
        this.auctionId = auctionId;
        this.auctionVersion = auctionVersion;
        this.status = status;
        this.finalPrice = finalPrice;
        this.winnerUserId = winnerUserId;
        this.endedAt = endedAt;
        this.endReason = endReason;
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

    public BigDecimal getFinalPrice() {
        return finalPrice;
    }

    public void setFinalPrice(BigDecimal finalPrice) {
        this.finalPrice = finalPrice;
    }

    public Long getWinnerUserId() {
        return winnerUserId;
    }

    public void setWinnerUserId(Long winnerUserId) {
        this.winnerUserId = winnerUserId;
    }

    public LocalDateTime getEndedAt() {
        return endedAt;
    }

    public void setEndedAt(LocalDateTime endedAt) {
        this.endedAt = endedAt;
    }

    public String getEndReason() {
        return endReason;
    }

    public void setEndReason(String endReason) {
        this.endReason = endReason;
    }
}

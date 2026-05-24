package com.vbay.shared.dto.realtimeDTO.payload;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public class ViewerAuctionBidStatePayload {
    private long auctionId;
    private long userId;
    private Long autobidId;
    private BigDecimal maxBidAmount;
    private String autobidStatus;
    private boolean winning;
    private boolean showActiveMaxBid;
    private LocalDateTime updatedAt;

    public ViewerAuctionBidStatePayload() {
    }

    public ViewerAuctionBidStatePayload(
            long auctionId,
            long userId,
            Long autobidId,
            BigDecimal maxBidAmount,
            String autobidStatus,
            boolean winning,
            boolean showActiveMaxBid,
            LocalDateTime updatedAt) {
        this.auctionId = auctionId;
        this.userId = userId;
        this.autobidId = autobidId;
        this.maxBidAmount = maxBidAmount;
        this.autobidStatus = autobidStatus;
        this.winning = winning;
        this.showActiveMaxBid = showActiveMaxBid;
        this.updatedAt = updatedAt;
    }

    public long getAuctionId() { return auctionId; }
    public void setAuctionId(long auctionId) { this.auctionId = auctionId; }
    public long getUserId() { return userId; }
    public void setUserId(long userId) { this.userId = userId; }
    public Long getAutobidId() { return autobidId; }
    public void setAutobidId(Long autobidId) { this.autobidId = autobidId; }
    public BigDecimal getMaxBidAmount() { return maxBidAmount; }
    public void setMaxBidAmount(BigDecimal maxBidAmount) { this.maxBidAmount = maxBidAmount; }
    public String getAutobidStatus() { return autobidStatus; }
    public void setAutobidStatus(String autobidStatus) { this.autobidStatus = autobidStatus; }
    public boolean isWinning() { return winning; }
    public void setWinning(boolean winning) { this.winning = winning; }
    public boolean isShowActiveMaxBid() { return showActiveMaxBid; }
    public void setShowActiveMaxBid(boolean showActiveMaxBid) { this.showActiveMaxBid = showActiveMaxBid; }
    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
}

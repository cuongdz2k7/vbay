package com.vbay.shared.dto.realtimeDTO.payload;

import java.time.LocalDateTime;

public class ViewerAuctionBidSummaryPayload {
    private long auctionId;
    private long userId;
    private boolean hasAutobid;
    private String autobidStatus;
    private boolean winning;
    private boolean showActiveMaxBid;
    private LocalDateTime updatedAt;

    public ViewerAuctionBidSummaryPayload() {
    }

    public ViewerAuctionBidSummaryPayload(
            long auctionId,
            long userId,
            boolean hasAutobid,
            String autobidStatus,
            boolean winning,
            boolean showActiveMaxBid,
            LocalDateTime updatedAt) {
        this.auctionId = auctionId;
        this.userId = userId;
        this.hasAutobid = hasAutobid;
        this.autobidStatus = autobidStatus;
        this.winning = winning;
        this.showActiveMaxBid = showActiveMaxBid;
        this.updatedAt = updatedAt;
    }

    public long getAuctionId() { return auctionId; }
    public void setAuctionId(long auctionId) { this.auctionId = auctionId; }
    public long getUserId() { return userId; }
    public void setUserId(long userId) { this.userId = userId; }
    public boolean isHasAutobid() { return hasAutobid; }
    public void setHasAutobid(boolean hasAutobid) { this.hasAutobid = hasAutobid; }
    public String getAutobidStatus() { return autobidStatus; }
    public void setAutobidStatus(String autobidStatus) { this.autobidStatus = autobidStatus; }
    public boolean isWinning() { return winning; }
    public void setWinning(boolean winning) { this.winning = winning; }
    public boolean isShowActiveMaxBid() { return showActiveMaxBid; }
    public void setShowActiveMaxBid(boolean showActiveMaxBid) { this.showActiveMaxBid = showActiveMaxBid; }
    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
}

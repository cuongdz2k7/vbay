package com.vbay.shared.dto.realtimeDTO.payload;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public class AutobidUpdatedPayload {
    private long auctionId;
    private long userId;
    private long autobidId;

    private BigDecimal maxBidAmount;
    private String autobidStatus;

    private boolean winning;
    private boolean showActiveMaxBid;

    private LocalDateTime updatedAt;

    public AutobidUpdatedPayload(long auctionId, long userId, long autobidId, BigDecimal maxBidAmount, String autobidStatus, boolean winning, boolean showActiveMaxBid, LocalDateTime updatedAt) {
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
    public long getUserId() { return userId; }
    public long getAutobidId() { return autobidId; }
    public BigDecimal getMaxBidAmount() { return maxBidAmount; }
    public String getAutobidStatus() { return autobidStatus; }
    public boolean isWinning() { return winning; }
    public boolean isShowActiveMaxBid() { return showActiveMaxBid; }
    public LocalDateTime getUpdatedAt() { return updatedAt; }
    
}

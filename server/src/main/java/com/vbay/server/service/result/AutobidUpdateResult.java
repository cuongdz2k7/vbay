package com.vbay.server.service.result;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import com.vbay.server.service.bid.enums.AutobidStatus;

public class AutobidUpdateResult {
    private final long auctionId;
    private final long userId;
    private final long autobidId;
    private final BigDecimal maxBidAmount;
    private final AutobidStatus autobidStatus;
    private final boolean winning;
    private final boolean showActiveMaxBid;
    private final LocalDateTime updatedAt;

    public AutobidUpdateResult(
            long auctionId,
            long userId,
            long autobidId,
            BigDecimal maxBidAmount,
            AutobidStatus autobidStatus,
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
    public long getUserId() { return userId; }
    public long getAutobidId() { return autobidId; }
    public BigDecimal getMaxBidAmount() { return maxBidAmount; }
    public AutobidStatus getAutobidStatus() { return autobidStatus; }
    public boolean isWinning() { return winning; }
    public boolean isShowActiveMaxBid() { return showActiveMaxBid; }
    public LocalDateTime getUpdatedAt() { return updatedAt; }


    
}

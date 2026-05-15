package com.vbay.shared.dto.realtimeDTO.payload;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public class BidHistoryItemPayload {
    private long auctionId;
    private long auctionVersion;
    private long bidId;
    private Long bidderId;
    private String bidderDisplayName;
    private BigDecimal bidAmount;
    private String bidStatus;
    private String bidSource;
    private LocalDateTime bidTime;

    public BidHistoryItemPayload() {
    }

    public BidHistoryItemPayload(
            long auctionId,
            long auctionVersion,
            long bidId,
            Long bidderId,
            String bidderDisplayName,
            BigDecimal bidAmount,
            String bidStatus,
            String bidSource,
            LocalDateTime bidTime) {
        this.auctionId = auctionId;
        this.auctionVersion = auctionVersion;
        this.bidId = bidId;
        this.bidderId = bidderId;
        this.bidderDisplayName = bidderDisplayName;
        this.bidAmount = bidAmount;
        this.bidStatus = bidStatus;
        this.bidSource = bidSource;
        this.bidTime = bidTime;
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

    public long getBidId() {
        return bidId;
    }

    public void setBidId(long bidId) {
        this.bidId = bidId;
    }

    public Long getBidderId() {
        return bidderId;
    }

    public void setBidderId(Long bidderId) {
        this.bidderId = bidderId;
    }

    public String getBidderDisplayName() {
        return bidderDisplayName;
    }

    public void setBidderDisplayName(String bidderDisplayName) {
        this.bidderDisplayName = bidderDisplayName;
    }

    public BigDecimal getBidAmount() {
        return bidAmount;
    }

    public void setBidAmount(BigDecimal bidAmount) {
        this.bidAmount = bidAmount;
    }

    public String getBidStatus() {
        return bidStatus;
    }

    public void setBidStatus(String bidStatus) {
        this.bidStatus = bidStatus;
    }

    public String getBidSource() {
        return bidSource;
    }

    public void setBidSource(String bidSource) {
        this.bidSource = bidSource;
    }

    public LocalDateTime getBidTime() {
        return bidTime;
    }

    public void setBidTime(LocalDateTime bidTime) {
        this.bidTime = bidTime;
    }
}

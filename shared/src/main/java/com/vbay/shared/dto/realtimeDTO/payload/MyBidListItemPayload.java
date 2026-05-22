package com.vbay.shared.dto.realtimeDTO.payload;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import com.vbay.shared.enums.bid.BidSource;
import com.vbay.shared.enums.bid.BidStatus;

public class MyBidListItemPayload {
    private long bidId;
    private long auctionId;
    private long auctionVersion;

    private String auctionTitle;
    private String thumbnailUrl;
    private BigDecimal currentPrice;
    private String auctionStatus;

    private BigDecimal myBidAmount;
    private BigDecimal myMaxBidAmount;

    private BidStatus bidStatus;
    private BidSource bidSource;
    private LocalDateTime bidTime;

    
    private LocalDateTime startingTime;
    private LocalDateTime endingTime;
    private LocalDateTime updatedAt;

    public MyBidListItemPayload() {
    }

    public MyBidListItemPayload(
            long bidId,
            long auctionId,
            long auctionVersion,
            String auctionTitle,
            String thumbnailUrl,
            BigDecimal currentPrice,
            String auctionStatus,
            BigDecimal myBidAmount,
            BigDecimal myMaxBidAmount,
            BidStatus bidStatus,
            BidSource bidSource,
            LocalDateTime bidTime,
            LocalDateTime startingTime,
            LocalDateTime endingTime,
            LocalDateTime updatedAt) {
        this.bidId = bidId;
        this.auctionId = auctionId;
        this.auctionVersion = auctionVersion;
        this.auctionTitle = auctionTitle;
        this.thumbnailUrl = thumbnailUrl;
        this.currentPrice = currentPrice;
        this.auctionStatus = auctionStatus;
        this.myBidAmount = myBidAmount;
        this.myMaxBidAmount = myMaxBidAmount;
        this.bidStatus = bidStatus;
        this.bidSource = bidSource;
        this.bidTime = bidTime;
        this.startingTime = startingTime;
        this.endingTime = endingTime;
        this.updatedAt = updatedAt;
    }

    public long getBidId() {
        return bidId;
    }

    public void setBidId(long bidId) {
        this.bidId = bidId;
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

    public String getAuctionTitle() {
        return auctionTitle;
    }

    public void setAuctionTitle(String auctionTitle) {
        this.auctionTitle = auctionTitle;
    }

    public String getThumbnailUrl() {
        return thumbnailUrl;
    }

    public void setThumbnailUrl(String thumbnailUrl) {
        this.thumbnailUrl = thumbnailUrl;
    }

    public BigDecimal getCurrentPrice() {
        return currentPrice;
    }

    public void setCurrentPrice(BigDecimal currentPrice) {
        this.currentPrice = currentPrice;
    }
    public String getAuctionStatus() {
        return auctionStatus;
    }

    public void setAuctionStatus(String auctionStatus) {
        this.auctionStatus = auctionStatus;
    }

    public BigDecimal getMyBidAmount() {
        return myBidAmount;
    }

    public void setMyBidAmount(BigDecimal myBidAmount) {
        this.myBidAmount = myBidAmount;
    }

    public BigDecimal getMyMaxBidAmount() {
        return myMaxBidAmount;
    }

    public void setMyMaxBidAmount(BigDecimal myMaxBidAmount) {
        this.myMaxBidAmount = myMaxBidAmount;
    }

    public BidStatus getBidStatus() {
        return bidStatus;
    }

    public void setBidStatus(BidStatus bidStatus) {
        this.bidStatus = bidStatus;
    }

    public BidSource getBidSource() {
        return bidSource;
    }

    public void setBidSource(BidSource bidSource) {
        this.bidSource = bidSource;
    }

    public LocalDateTime getBidTime() {
        return bidTime;
    }

    public void setBidTime(LocalDateTime bidTime) {
        this.bidTime = bidTime;
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

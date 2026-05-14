package com.vbay.server.service.result;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import com.vbay.shared.enums.auction.AuctionStatus;
import com.vbay.shared.enums.auction.BidStatus;
import com.vbay.shared.enums.bid.BidSource;

public class UserMyBidListItemResult {
    private final long userId;
    private final long bidId;
    private final long auctionId;
    private final long auctionVersion;

    private final String auctionTitle;
    private final String thumbnailUrl;
    private final BigDecimal currentPrice;
    private final AuctionStatus auctionStatus;

    private final BigDecimal myBidAmount;
    private final BidStatus bidStatus;
    private final BidSource bidSource;
    private final LocalDateTime bidTime;

    private final LocalDateTime startingTime;
    private final LocalDateTime endingTime;
    private final LocalDateTime updatedAt;

    public UserMyBidListItemResult(long userId, long bidId, long auctionId, long auctionVersion, String auctionTitle,
            String thumbnailUrl, BigDecimal currentPrice, AuctionStatus auctionStatus, BigDecimal myBidAmount,
            BidStatus bidStatus, BidSource bidSource, LocalDateTime bidTime, LocalDateTime startingTime,
            LocalDateTime endingTime, LocalDateTime updatedAt) {

            this.userId = userId;
            this.bidId = bidId;
            this.auctionId = auctionId;
            this.auctionVersion = auctionVersion;
            this.auctionTitle = auctionTitle;
            this.thumbnailUrl = thumbnailUrl;
            this.currentPrice = currentPrice;
            this.auctionStatus = auctionStatus;
            this.myBidAmount = myBidAmount;
            this.bidStatus = bidStatus;
            this.bidSource = bidSource;
            this.bidTime = bidTime;
            this.startingTime = startingTime;
            this.endingTime = endingTime;
            this.updatedAt = updatedAt;
    }

    public long getUserId() {
        return userId;
    }

    public long getBidId() {
        return bidId;
    }   
    public long getAuctionId() {
        return auctionId;
    }

    public long getAuctionVersion() {
        return auctionVersion;
    }

    public String getAuctionTitle() {
        return auctionTitle;
    }

    public String getThumbnailUrl() {
        return thumbnailUrl;
    }
    public BigDecimal getCurrentPrice() {
        return currentPrice;
    }
    public AuctionStatus getAuctionStatus() {
        return auctionStatus;
    }

    public BigDecimal getMyBidAmount() {
        return myBidAmount;
    }
    public BidStatus getBidStatus() {
        return bidStatus;
    }
    public BidSource getBidSource() {
        return bidSource;
    }
    public LocalDateTime getBidTime() {
        return bidTime;
    }
    public LocalDateTime getStartingTime() {
        return startingTime;
    }
    public LocalDateTime getEndingTime() {
        return endingTime;
    }
    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }


}

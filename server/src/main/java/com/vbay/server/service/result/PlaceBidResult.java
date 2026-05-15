package com.vbay.server.service.result;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import com.vbay.shared.enums.auction.BidStatus;
import com.vbay.shared.enums.bid.BidSource;

public class PlaceBidResult {
    private final long auctionId;
    private final long auctionVersion;
    private final long sellerId;
    private final long bidderId;
    private final long bidId;
    private final BigDecimal bidAmount;
    private final BigDecimal currentPrice;
    private final BigDecimal nextMinimumBid;
    private final Boolean reserveMet;
    private final Long previousWinningUserId;
    private final Long previousWinningBidId;
    private final BidStatus bidStatus;
    private final BidSource bidSource;
    private final LocalDateTime bidTime;

    public PlaceBidResult(
            long auctionId,
            long auctionVersion,
            long sellerId,
            long bidderId,
            long bidId,
            BigDecimal bidAmount,
            BigDecimal currentPrice,
            BigDecimal nextMinimumBid,
            Boolean reserveMet,
            Long previousWinningUserId,
            Long previousWinningBidId,
            BidStatus bidStatus,
            BidSource bidSource,
            LocalDateTime bidTime) {
        this.auctionId = auctionId;
        this.auctionVersion = auctionVersion;
        this.sellerId = sellerId;
        this.bidderId = bidderId;
        this.bidId = bidId;
        this.bidAmount = bidAmount;
        this.currentPrice = currentPrice;
        this.nextMinimumBid = nextMinimumBid;
        this.reserveMet = reserveMet;
        this.previousWinningUserId = previousWinningUserId;
        this.previousWinningBidId = previousWinningBidId;
        this.bidStatus = bidStatus;
        this.bidSource = bidSource;
        this.bidTime = bidTime;
    }

    public long getAuctionId() {
        return auctionId;
    }

    public long getAuctionVersion() {
        return auctionVersion;
    }

    public long getSellerId() {
        return sellerId;
    }

    public long getBidderId() {
        return bidderId;
    }

    public long getBidId() {
        return bidId;
    }

    public BigDecimal getBidAmount() {
        return bidAmount;
    }

    public BigDecimal getCurrentPrice() {
        return currentPrice;
    }

    public BigDecimal getNextMinimumBid() {
        return nextMinimumBid;
    }

    public Boolean getReserveMet() {
        return reserveMet;
    }

    public Long getPreviousWinningUserId() {
        return previousWinningUserId;
    }

    public Long getPreviousWinningBidId() {
        return previousWinningBidId;
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
}

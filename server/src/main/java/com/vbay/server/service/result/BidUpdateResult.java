package com.vbay.server.service.result;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import com.vbay.shared.enums.bid.BidSource;
import com.vbay.shared.enums.bid.BidStatus;

public abstract class BidUpdateResult {
    private final long auctionId;
    private final long auctionVersion;
    private final long sellerId;
    private final long bidderId;
    private final long bidId;
    private final BigDecimal bidAmount;
    private final BigDecimal currentPrice;
    private final Boolean reserveMet;
    private final String auctionStatus;
    private final Long previousWinningUserId;
    private final Long previousWinningBidId;
    private final BidStatus bidStatus;
    private final BidSource bidSource;
    private final LocalDateTime bidTime;
    private final LocalDateTime startingTime;
    private final LocalDateTime endingTime;
    private final List<UserMyBidListItemResult> affectedMyBidItems;

    protected BidUpdateResult(
            long auctionId,
            long auctionVersion,
            long sellerId,
            long bidderId,
            long bidId,
            BigDecimal bidAmount,
            BigDecimal currentPrice,
            Boolean reserveMet,
            String auctionStatus,
            Long previousWinningUserId,
            Long previousWinningBidId,
            BidStatus bidStatus,
            BidSource bidSource,
            LocalDateTime bidTime,
            LocalDateTime startingTime,
            LocalDateTime endingTime,
            List<UserMyBidListItemResult> affectedMyBidItems) {
        this.auctionId = auctionId;
        this.auctionVersion = auctionVersion;
        this.sellerId = sellerId;
        this.bidderId = bidderId;
        this.bidId = bidId;
        this.bidAmount = bidAmount;
        this.currentPrice = currentPrice;
        this.reserveMet = reserveMet;
        this.auctionStatus = auctionStatus;
        this.previousWinningUserId = previousWinningUserId;
        this.previousWinningBidId = previousWinningBidId;
        this.bidStatus = bidStatus;
        this.bidSource = bidSource;
        this.bidTime = bidTime;
        this.startingTime = startingTime;
        this.endingTime = endingTime;
        this.affectedMyBidItems = affectedMyBidItems == null ? List.of() : List.copyOf(affectedMyBidItems);
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

    public Boolean getReserveMet() {
        return reserveMet;
    }

    public String getAuctionStatus() {
        return auctionStatus;
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

    public LocalDateTime getStartingTime() {
        return startingTime;
    }

    public LocalDateTime getEndingTime() {
        return endingTime;
    }

    public List<UserMyBidListItemResult> getAffectedMyBidItems() {
        return affectedMyBidItems;
    }
}

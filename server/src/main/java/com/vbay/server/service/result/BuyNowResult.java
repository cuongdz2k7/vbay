package com.vbay.server.service.result;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import com.vbay.shared.enums.auction.AuctionStatus;

public class BuyNowResult {
    private final long auctionId;
    private final long auctionVersion;
    private final long buyerId;
    private final long sellerId;
    private final long bidId;
    private final long paymentId;
    private final BigDecimal finalPrice;
    private final AuctionStatus auctionStatus;
    private final Boolean reserveMet;
    private final boolean antiSnipeExtended;
    private final Long previousWinningUserId;
    private final Long previousWinningBidId;
    private final LocalDateTime startingTime;
    private final LocalDateTime endingTime;
    private final LocalDateTime boughtAt;
    private final List<UserMyBidListItemResult> affectedMyBidItems;

    public BuyNowResult(
            long auctionId,
            long auctionVersion,
            long buyerId,
            long sellerId,
            long bidId,
            long paymentId,
            BigDecimal finalPrice,
            AuctionStatus auctionStatus,
            Boolean reserveMet,
            boolean antiSnipeExtended,
            Long previousWinningUserId,
            Long previousWinningBidId,
            LocalDateTime startingTime,
            LocalDateTime endingTime,
            LocalDateTime boughtAt,
            List<UserMyBidListItemResult> affectedMyBidItems) {
        this.auctionId = auctionId;
        this.auctionVersion = auctionVersion;
        this.buyerId = buyerId;
        this.sellerId = sellerId;
        this.bidId = bidId;
        this.paymentId = paymentId;
        this.finalPrice = finalPrice;
        this.auctionStatus = auctionStatus;
        this.reserveMet = reserveMet;
        this.antiSnipeExtended = antiSnipeExtended;
        this.previousWinningUserId = previousWinningUserId;
        this.previousWinningBidId = previousWinningBidId;
        this.startingTime = startingTime;
        this.endingTime = endingTime;
        this.boughtAt = boughtAt;
        this.affectedMyBidItems = affectedMyBidItems == null ? List.of() : List.copyOf(affectedMyBidItems);
    }

    public long getAuctionId() {
        return auctionId;
    }

    public long getAuctionVersion() {
        return auctionVersion;
    }

    public long getBuyerId() {
        return buyerId;
    }

    public long getSellerId() {
        return sellerId;
    }

    public long getBidId() {
        return bidId;
    }

    public long getPaymentId() {
        return paymentId;
    }

    public BigDecimal getFinalPrice() {
        return finalPrice;
    }

    public AuctionStatus getAuctionStatus() {
        return auctionStatus;
    }

    public Boolean getReserveMet() {
        return reserveMet;
    }

    public boolean isAntiSnipeExtended() {
        return antiSnipeExtended;
    }

    public Long getPreviousWinningUserId() {
        return previousWinningUserId;
    }

    public Long getPreviousWinningBidId() {
        return previousWinningBidId;
    }

    public LocalDateTime getStartingTime() {
        return startingTime;
    }

    public LocalDateTime getEndingTime() {
        return endingTime;
    }

    public LocalDateTime getBoughtAt() {
        return boughtAt;
    }

    public List<UserMyBidListItemResult> getAffectedMyBidItems() {
        return affectedMyBidItems;
    }
}

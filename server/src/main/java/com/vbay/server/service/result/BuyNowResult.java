package com.vbay.server.service.result;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

public class BuyNowResult {
    private final long auctionId;
    private final long auctionVersion;
    private final long buyerId;
    private final long sellerId;
    private final long bidId;
    private final long paymentId;
    private final BigDecimal finalPrice;
    private final Long previousWinningUserId;
    private final Long previousWinningBidId;
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
            Long previousWinningUserId,
            Long previousWinningBidId,
            LocalDateTime boughtAt,
            List<UserMyBidListItemResult> affectedMyBidItems) {
        this.auctionId = auctionId;
        this.auctionVersion = auctionVersion;
        this.buyerId = buyerId;
        this.sellerId = sellerId;
        this.bidId = bidId;
        this.paymentId = paymentId;
        this.finalPrice = finalPrice;
        this.previousWinningUserId = previousWinningUserId;
        this.previousWinningBidId = previousWinningBidId;
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

    public Long getPreviousWinningUserId() {
        return previousWinningUserId;
    }

    public Long getPreviousWinningBidId() {
        return previousWinningBidId;
    }

    public LocalDateTime getBoughtAt() {
        return boughtAt;
    }

    public List<UserMyBidListItemResult> getAffectedMyBidItems() {
        return affectedMyBidItems;
    }
}

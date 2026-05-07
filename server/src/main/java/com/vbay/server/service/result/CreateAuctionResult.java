package com.vbay.server.service.result;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import com.vbay.shared.enums.auction.AuctionStatus;

public class CreateAuctionResult {
    private final long auctionId;
    private final long auctionVersion;
    private final long productId;
    private final long productVersion;
    private final long sellerId;
    private final String title;
    private final long categoryId;
    private final AuctionStatus status;
    private final BigDecimal startingPrice;
    private final BigDecimal currentPrice;
    private final BigDecimal minimumBidStep;
    private final LocalDateTime startingTime;
    private final LocalDateTime endingTime;

    public CreateAuctionResult(
            long auctionId,
            long auctionVersion,
            long productId,
            long productVersion,
            long sellerId,
            String title,
            long categoryId,
            AuctionStatus status,
            BigDecimal startingPrice,
            BigDecimal currentPrice,
            BigDecimal minimumBidStep,
            LocalDateTime startingTime,
            LocalDateTime endingTime) {
        this.auctionId = auctionId;
        this.auctionVersion = auctionVersion;
        this.productId = productId;
        this.productVersion = productVersion;
        this.sellerId = sellerId;
        this.title = title;
        this.categoryId = categoryId;
        this.status = status;
        this.startingPrice = startingPrice;
        this.currentPrice = currentPrice;
        this.minimumBidStep = minimumBidStep;
        this.startingTime = startingTime;
        this.endingTime = endingTime;
    }

    public long getAuctionId() {
        return auctionId;
    }

    public long getAuctionVersion() {
        return auctionVersion;
    }

    public long getProductId() {
        return productId;
    }

    public long getProductVersion() {
        return productVersion;
    }

    public long getSellerId() {
        return sellerId;
    }

    public String getTitle() {
        return title;
    }

    public long getCategoryId() {
        return categoryId;
    }

    public AuctionStatus getStatus() {
        return status;
    }

    public BigDecimal getStartingPrice() {
        return startingPrice;
    }

    public BigDecimal getCurrentPrice() {
        return currentPrice;
    }

    public BigDecimal getMinimumBidStep() {
        return minimumBidStep;
    }

    public LocalDateTime getStartingTime() {
        return startingTime;
    }

    public LocalDateTime getEndingTime() {
        return endingTime;
    }
}

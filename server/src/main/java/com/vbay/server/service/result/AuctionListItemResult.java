package com.vbay.server.service.result;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public class AuctionListItemResult {
    private final long auctionId;
    private final long auctionVersion;
    private final long productId;
    private final long sellerId;
    private final String title;
    private final String description;
    private final String productName;
    private final long categoryId;
    private final String status;
    private final BigDecimal startingPrice;
    private final BigDecimal currentPrice;
    private final BigDecimal minimumBidStep;
    private final BigDecimal buyNowPrice;
    private final Long winnerUserId;
    private final Boolean reserveMet;
    private final String thumbnailUrl;
    private final LocalDateTime startingTime;
    private final LocalDateTime endingTime;
    private final LocalDateTime updatedAt;

    public AuctionListItemResult(
            long auctionId,
            long auctionVersion,
            long productId,
            long sellerId,
            String title,
            String description,
            String productName,
            long categoryId,
            String status,
            BigDecimal startingPrice,
            BigDecimal currentPrice,
            BigDecimal minimumBidStep,
            BigDecimal buyNowPrice,
            Long winnerUserId,
            Boolean reserveMet,
            String thumbnailUrl,
            LocalDateTime startingTime,
            LocalDateTime endingTime,
            LocalDateTime updatedAt) {
        this.auctionId = auctionId;
        this.auctionVersion = auctionVersion;
        this.productId = productId;
        this.sellerId = sellerId;
        this.title = title;
        this.description = description;
        this.productName = productName;
        this.categoryId = categoryId;
        this.status = status;
        this.startingPrice = startingPrice;
        this.currentPrice = currentPrice;
        this.minimumBidStep = minimumBidStep;
        this.buyNowPrice = buyNowPrice;
        this.winnerUserId = winnerUserId;
        this.reserveMet = reserveMet;
        this.thumbnailUrl = thumbnailUrl;
        this.startingTime = startingTime;
        this.endingTime = endingTime;
        this.updatedAt = updatedAt;
    }

    public long getAuctionId() { return auctionId; }
    public long getAuctionVersion() { return auctionVersion; }
    public long getProductId() { return productId; }
    public long getSellerId() { return sellerId; }
    public String getTitle() { return title; }
    public String getDescription() { return description; }
    public String getProductName() { return productName; }
    public long getCategoryId() { return categoryId; }
    public String getStatus() { return status; }
    public BigDecimal getStartingPrice() { return startingPrice; }
    public BigDecimal getCurrentPrice() { return currentPrice; }
    public BigDecimal getMinimumBidStep() { return minimumBidStep; }
    public BigDecimal getBuyNowPrice() { return buyNowPrice; }
    public Long getWinnerUserId() { return winnerUserId; }
    public Boolean getReserveMet() { return reserveMet; }
    public String getThumbnailUrl() { return thumbnailUrl; }
    public LocalDateTime getStartingTime() { return startingTime; }
    public LocalDateTime getEndingTime() { return endingTime; }
    public LocalDateTime getUpdatedAt() { return updatedAt; }
}

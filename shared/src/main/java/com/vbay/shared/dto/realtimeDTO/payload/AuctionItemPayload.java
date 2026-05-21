package com.vbay.shared.dto.realtimeDTO.payload;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

public class AuctionItemPayload {
    private long auctionId;
    private long auctionVersion;
    private long productId;
    private long sellerId;
    private String title;
    private String description;
    private String productName;
    private long categoryId;
    private String status;
    private BigDecimal startingPrice;
    private BigDecimal currentPrice;
    private BigDecimal minimumBidStep;
    private BigDecimal buyNowPrice;
    private Long winnerUserId;
    private Boolean reserveMet;
    private boolean antiSnipeExtended;
    private String thumbnailUrl;
    private List<String> imageUrls;
    private LocalDateTime startingTime;
    private LocalDateTime endingTime;
    private LocalDateTime updatedAt;

    public AuctionItemPayload() {
    }

    public AuctionItemPayload(
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
            boolean antiSnipeExtended,
            String thumbnailUrl,
            List<String> imageUrls,
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
        this.antiSnipeExtended = antiSnipeExtended;
        this.thumbnailUrl = thumbnailUrl;
        this.imageUrls = imageUrls;
        this.startingTime = startingTime;
        this.endingTime = endingTime;
        this.updatedAt = updatedAt;
    }

    public long getAuctionId() { return auctionId; }
    public void setAuctionId(long auctionId) { this.auctionId = auctionId; }
    public long getAuctionVersion() { return auctionVersion; }
    public void setAuctionVersion(long auctionVersion) { this.auctionVersion = auctionVersion; }
    public long getProductId() { return productId; }
    public void setProductId(long productId) { this.productId = productId; }
    public long getSellerId() { return sellerId; }
    public void setSellerId(long sellerId) { this.sellerId = sellerId; }
    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public String getProductName() { return productName; }
    public void setProductName(String productName) { this.productName = productName; }
    public long getCategoryId() { return categoryId; }
    public void setCategoryId(long categoryId) { this.categoryId = categoryId; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public BigDecimal getStartingPrice() { return startingPrice; }
    public void setStartingPrice(BigDecimal startingPrice) { this.startingPrice = startingPrice; }
    public BigDecimal getCurrentPrice() { return currentPrice; }
    public void setCurrentPrice(BigDecimal currentPrice) { this.currentPrice = currentPrice; }
    public BigDecimal getMinimumBidStep() { return minimumBidStep; }
    public void setMinimumBidStep(BigDecimal minimumBidStep) { this.minimumBidStep = minimumBidStep; }
    public BigDecimal getBuyNowPrice() { return buyNowPrice; }
    public void setBuyNowPrice(BigDecimal buyNowPrice) { this.buyNowPrice = buyNowPrice; }
    public Long getWinnerUserId() { return winnerUserId; }
    public void setWinnerUserId(Long winnerUserId) { this.winnerUserId = winnerUserId; }
    public Boolean getReserveMet() { return reserveMet; }
    public void setReserveMet(Boolean reserveMet) { this.reserveMet = reserveMet; }
    public boolean isAntiSnipeExtended() { return antiSnipeExtended; }
    public void setAntiSnipeExtended(boolean antiSnipeExtended) { this.antiSnipeExtended = antiSnipeExtended; }
    public String getThumbnailUrl() { return thumbnailUrl; }
    public void setThumbnailUrl(String thumbnailUrl) { this.thumbnailUrl = thumbnailUrl; }
    public List<String> getImageUrls() { return imageUrls; }
    public void setImageUrls(List<String> imageUrls) { this.imageUrls = imageUrls; }
    public LocalDateTime getStartingTime() { return startingTime; }
    public void setStartingTime(LocalDateTime startingTime) { this.startingTime = startingTime; }
    public LocalDateTime getEndingTime() { return endingTime; }
    public void setEndingTime(LocalDateTime endingTime) { this.endingTime = endingTime; }
    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
}

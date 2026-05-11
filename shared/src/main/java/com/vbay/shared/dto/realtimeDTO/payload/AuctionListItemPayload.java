package com.vbay.shared.dto.realtimeDTO.payload;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public class AuctionListItemPayload {
    private long auctionId;
    private long auctionVersion;
    private long sellerId;
    private String title;
    private long categoryId;
    private String status;
    private BigDecimal currentPrice;
    private String thumbnailBase64;
    private LocalDateTime startingTime;
    private LocalDateTime endingTime;
    private LocalDateTime updatedAt;

    public AuctionListItemPayload() {
    }

    public AuctionListItemPayload(
            long auctionId,
            long auctionVersion,
            long sellerId,
            String title,
            long categoryId,
            String status,
            BigDecimal currentPrice,
            String thumbnailBase64,
            LocalDateTime startingTime,
            LocalDateTime endingTime,
            LocalDateTime updatedAt) {
        this.auctionId = auctionId;
        this.auctionVersion = auctionVersion;
        this.sellerId = sellerId;
        this.title = title;
        this.categoryId = categoryId;
        this.status = status;
        this.currentPrice = currentPrice;
        this.thumbnailBase64 = thumbnailBase64;
        this.startingTime = startingTime;
        this.endingTime = endingTime;
        this.updatedAt = updatedAt;
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

    public long getSellerId() {
        return sellerId;
    }

    public void setSellerId(long sellerId) {
        this.sellerId = sellerId;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public long getCategoryId() {
        return categoryId;
    }

    public void setCategoryId(long categoryId) {
        this.categoryId = categoryId;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public BigDecimal getCurrentPrice() {
        return currentPrice;
    }

    public void setCurrentPrice(BigDecimal currentPrice) {
        this.currentPrice = currentPrice;
    }

    public String getThumbnailBase64() {
        return thumbnailBase64;
    }

    public void setThumbnailUrl(String thumbnailBase64) {
        this.thumbnailBase64 = thumbnailBase64;
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

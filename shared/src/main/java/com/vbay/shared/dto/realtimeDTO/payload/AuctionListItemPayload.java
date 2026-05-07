package com.vbay.shared.dto.realtimeDTO.payload;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public class AuctionListItemPayload {
    private long auctionId;
    private long auctionVersion;
    private String title;
    private String categoryId;
    private String status;
    private BigDecimal currentPrice;
    private String thumbnailUrl;
    private LocalDateTime endingTime;
    private LocalDateTime updatedAt;

    public AuctionListItemPayload() {
    }

    public AuctionListItemPayload(
            long auctionId,
            long auctionVersion,
            String title,
            String categoryId,
            String status,
            BigDecimal currentPrice,
            String thumbnailUrl,
            LocalDateTime endingTime,
            LocalDateTime updatedAt) {
        this.auctionId = auctionId;
        this.auctionVersion = auctionVersion;
        this.title = title;
        this.categoryId = categoryId;
        this.status = status;
        this.currentPrice = currentPrice;
        this.thumbnailUrl = thumbnailUrl;
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

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getCategoryId() {
        return categoryId;
    }

    public void setCategoryId(String categoryId) {
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

    public String getThumbnailUrl() {
        return thumbnailUrl;
    }

    public void setThumbnailUrl(String thumbnailUrl) {
        this.thumbnailUrl = thumbnailUrl;
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

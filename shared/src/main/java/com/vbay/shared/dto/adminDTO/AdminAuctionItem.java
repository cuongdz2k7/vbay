package com.vbay.shared.dto.adminDTO;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import com.vbay.shared.enums.auction.AuctionStatus;

public class AdminAuctionItem {
    private long auctionId;
    private long sellerId;
    private String title;
    private String productName;
    private AuctionStatus status;
    private BigDecimal currentPrice;
    private LocalDateTime startingTime;
    private LocalDateTime endingTime;

    public AdminAuctionItem() {}

    public AdminAuctionItem(long auctionId, long sellerId, String title, String productName, AuctionStatus status, BigDecimal currentPrice, LocalDateTime startingTime, LocalDateTime endingTime) {
        this.auctionId = auctionId;
        this.sellerId = sellerId;
        this.title = title;
        this.productName = productName;
        this.status = status;
        this.currentPrice = currentPrice;
        this.startingTime = startingTime;
        this.endingTime = endingTime;
    }

    public long getAuctionId() { return auctionId; }
    public void setAuctionId(long auctionId) { this.auctionId = auctionId; }

    public long getSellerId() { return sellerId; }
    public void setSellerId(long sellerId) { this.sellerId = sellerId; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getProductName() { return productName; }
    public void setProductName(String productName) { this.productName = productName; }

    public AuctionStatus getStatus() { return status; }
    public void setStatus(AuctionStatus status) { this.status = status; }

    public BigDecimal getCurrentPrice() { return currentPrice; }
    public void setCurrentPrice(BigDecimal currentPrice) { this.currentPrice = currentPrice; }

    public LocalDateTime getStartingTime() { return startingTime; }
    public void setStartingTime(LocalDateTime startingTime) { this.startingTime = startingTime; }

    public LocalDateTime getEndingTime() { return endingTime; }
    public void setEndingTime(LocalDateTime endingTime) { this.endingTime = endingTime; }
}

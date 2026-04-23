package com.vbay.shared.dto;

import java.time.LocalDateTime;

public class CreateAuctionRequest {
    private String title;
    private String description;
    private long productId;
    private long sellerId;
    private double minimumBidStep;
    private double buyNowPrice;
    private double reservePrice;
    private double startingPrice;
    private LocalDateTime startingTime, endingTime;

    public CreateAuctionRequest(String title, String description, Long productId, long sellerId, double minimumBidStep, double buyNowPrice, double reservePrice, double startingPrice, LocalDateTime startingTime, LocalDateTime endingTime) {
        this.title = title;
        this.description = description;
        this.productId = productId;
        this.sellerId = sellerId;
        this.minimumBidStep = minimumBidStep;
        this.buyNowPrice = buyNowPrice;
        this.reservePrice = reservePrice;
        this.startingPrice = startingPrice;
        this.startingTime = startingTime;
        this.endingTime = endingTime;
    }
    public String getTitle() {
        return title;
    }
    public String getDescription() {
        return description;
    }
    public long getProductId() {
        return productId;
    }
    public long getSellerId() {
        return sellerId;
    }
    public double getMinimumBidStep() {
        return minimumBidStep;
    }
    public double getBuyNowPrice() {
        return buyNowPrice;
    }
    public double getReservePrice() {
        return reservePrice;
    }
    public double getStartingPrice() {
        return startingPrice;
    }
    public LocalDateTime getStartingTime() {
        return startingTime;
    }
    public LocalDateTime getEndingTime() {
        return endingTime;
    }
    @Override
    public String toString() {
        return "CreateAuctionRequest{" +
                "title='" + title + '\'' +
                ", description='" + description + '\'' +
                "productId=" + productId +
                ", sellerId=" + sellerId +
                ", minimumBidStep=" + minimumBidStep +
                ", buyNowPrice=" + buyNowPrice +
                ", reservePrice=" + reservePrice +
                ", startingPrice=" + startingPrice +
                ", startingTime='" + startingTime.toString() + '\'' +
                ", endingTime='" + endingTime.toString() + '\'' +
                '}';
    }
    
}

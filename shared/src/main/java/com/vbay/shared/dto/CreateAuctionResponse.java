package com.vbay.shared.dto;

public class CreateAuctionResponse {
    private long auctionId;
    private long productId;
    private String status;
    private String createdAt;
    private double startPrice;
    private double currentPrice;
    private double minimumBidStep;
    private String startTime;
    private String endTime;
    private Double reservePrice;
    private Double buyNowPrice;

    public CreateAuctionResponse(long auctionId, long productId, String status, String createdAt, double startPrice, double currentPrice, double minimumBidStep, String startTime, String endTime, Double reservePrice, Double buyNowPrice) {
        this.auctionId = auctionId;
        this.productId = productId;
        this.status = status;
        this.createdAt = createdAt;
        this.startPrice = startPrice;
        this.currentPrice = currentPrice;
        this.minimumBidStep = minimumBidStep;
        this.startTime = startTime;
        this.endTime = endTime;
        this.reservePrice = reservePrice;
        this.buyNowPrice = buyNowPrice;
    }
    public long getAuctionId() {
        return auctionId;
    }
    public long getProductId() {
        return productId;
    }
    public String getStatus() {
        return status;
    }
    public String getCreatedAt() {
        return createdAt;
    }
    public double getStartPrice() {
        return startPrice;
    }
    public double getCurrentPrice() {
        return currentPrice;
    }
    public double getMinimumBidStep() {
        return minimumBidStep;
    }
    public String getStartTime() {
        return startTime;
    }
    public String getEndTime() {
        return endTime;
    }
    public Double getReservePrice() {
        return reservePrice;
    }
    public Double getBuyNowPrice() {
        return buyNowPrice;
    }
    @Override
    public String toString() {
        return "CreateAuctionResponse{" +
                "auctionId=" + auctionId +
                ", productId=" + productId +
                ", status='" + status + '\'' +
                ", createdAt='" + createdAt + '\'' +
                ", startPrice=" + startPrice +
                ", currentPrice=" + currentPrice +
                ", minimumBidStep=" + minimumBidStep +
                ", startTime='" + startTime + '\'' +
                ", endTime='" + endTime + '\'' +
                ", reservePrice=" + reservePrice +
                ", buyNowPrice=" + buyNowPrice +
                '}';
    }
}
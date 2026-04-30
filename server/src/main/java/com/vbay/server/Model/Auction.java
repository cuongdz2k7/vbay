package com.vbay.server.model;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import com.vbay.shared.enums.shared_status.AuctionStatus;

public class Auction {
    private long id;
    private long productId;
    private long sellerId;
    private String title;
    private String description;
    private BigDecimal startingPrice;
    private BigDecimal currentPrice;
    private BigDecimal reservePrice;
    private BigDecimal buyNowPrice;
    private BigDecimal minimumBidStep;
    private LocalDateTime startingTime;
    private LocalDateTime endingTime;
    private AuctionStatus status;

    public Auction(
        long productId,
        long sellerId,
        BigDecimal startingPrice,
        BigDecimal minimumBidStep,
        LocalDateTime startingTime,
        LocalDateTime endingTime
    ) {
        this.productId = productId;
        this.sellerId = sellerId;
        this.startingPrice = startingPrice;
        this.currentPrice = startingPrice;
        this.minimumBidStep = minimumBidStep;
        this.startingTime = startingTime;
        this.endingTime = endingTime;
        this.status = AuctionStatus.PENDING;
    }

    public Auction(
        long sellerId,
        long productId,
        String title,
        String description,
        BigDecimal buyNowPrice,
        BigDecimal reservePrice,
        BigDecimal minimumBidStep,
        BigDecimal startingPrice,
        LocalDateTime startingTime,
        LocalDateTime endingTime
    ) {
        this.productId = productId;
        this.sellerId = sellerId;
        this.title = title;
        this.description = description;
        this.startingPrice = startingPrice;
        this.currentPrice = this.startingPrice;
        this.reservePrice = reservePrice;
        this.buyNowPrice = buyNowPrice;
        this.minimumBidStep = minimumBidStep;
        this.startingTime = startingTime;
        this.endingTime = endingTime;
        this.status = AuctionStatus.PENDING;
    }

    public Auction(
        long id,
        long productId,
        long sellerId,
        String title,
        String description,
        BigDecimal startingPrice,
        BigDecimal currentPrice,
        BigDecimal reservePrice,
        BigDecimal buyNowPrice,
        BigDecimal minimumBidStep,
        LocalDateTime startingTime,
        LocalDateTime endingTime,
        AuctionStatus status
    ) {
        this.id = id;
        this.productId = productId;
        this.sellerId = sellerId;
        this.title = title;
        this.description = description;
        this.startingPrice = startingPrice;
        this.currentPrice = currentPrice;
        this.reservePrice = reservePrice;
        this.buyNowPrice = buyNowPrice;
        this.minimumBidStep = minimumBidStep;
        this.startingTime = startingTime;
        this.endingTime = endingTime;
        this.status = status;
    }

    public long getId() {
        return id;
    }

    public long getProductId() {
        return productId;
    }

    public long getSellerId() {
        return sellerId;
    }

    public String getTitle() {
        return title;
    }

    public String getDescription() {
        return description;
    }

    public BigDecimal getStartPrice() {
        return startingPrice;
    }

    public BigDecimal getCurrentPrice() {
        return currentPrice;
    }

    public BigDecimal getReservePrice() {
        return reservePrice;
    }

    public BigDecimal getBuyNowPrice() {
        return buyNowPrice;
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

    public AuctionStatus getStatus() {
        return status;
    }

    public void setId(long id) {
        this.id = id;
    }

    public void setProductId(long productId) {
        this.productId = productId;
    }

    public void setSellerId(long sellerId) {
        this.sellerId = sellerId;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public void setStartPrice(BigDecimal startingPrice) {
        this.startingPrice = startingPrice;
    }

    public void setCurrentPrice(BigDecimal currentPrice) {
        this.currentPrice = currentPrice;
    }

    public void setReservePrice(BigDecimal reservePrice) {
        this.reservePrice = reservePrice;
    }

    public void setBuyNowPrice(BigDecimal buyNowPrice) {
        this.buyNowPrice = buyNowPrice;
    }

    public void setMinimumBidStep(BigDecimal minimumBidStep) {
        this.minimumBidStep = minimumBidStep;
    }

    public void setStartingTime(LocalDateTime startingTime) {
        this.startingTime = startingTime;
    }

    public void setEndingTime(LocalDateTime endingTime) {
        this.endingTime = endingTime;
    }

    public void setStatus(AuctionStatus status) {
        this.status = status;
    }
}

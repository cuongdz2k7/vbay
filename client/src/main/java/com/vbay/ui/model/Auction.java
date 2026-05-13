package com.vbay.ui.model;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public class Auction {
    private final long id;
    private final long version;
    private final long sellerId;
    private final String title;
    private final String description;
    private final String status;
    private final BigDecimal startingPrice;
    private final BigDecimal currentPrice;
    private final BigDecimal minimumBidStep;
    private final BigDecimal buyNowPrice;
    private final LocalDateTime startingTime;
    private final LocalDateTime endingTime;
    private final Product product;

    public Auction(
        long id,
        long version,
        long sellerId,
        String title,
        String description,
        String status,
        BigDecimal startingPrice,
        BigDecimal currentPrice,
        BigDecimal minimumBidStep,
        BigDecimal buyNowPrice,
        LocalDateTime startingTime,
        LocalDateTime endingTime,
        Product product
    ) {
        this.id = id;
        this.version = version;
        this.sellerId = sellerId;
        this.title = title;
        this.description = description;
        this.status = status;
        this.startingPrice = startingPrice;
        this.currentPrice = currentPrice;
        this.minimumBidStep = minimumBidStep;
        this.buyNowPrice = buyNowPrice;
        this.startingTime = startingTime;
        this.endingTime = endingTime;
        this.product = product;
    }

    public long getId() {
        return id;
    }

    public long getVersion() {
        return version;
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

    public String getStatus() {
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

    public BigDecimal getBuyNowPrice() {
        return buyNowPrice;
    }

    public LocalDateTime getStartingTime() {
        return startingTime;
    }

    public LocalDateTime getEndingTime() {
        return endingTime;
    }

    public Product getProduct() {
        return product;
    }
}

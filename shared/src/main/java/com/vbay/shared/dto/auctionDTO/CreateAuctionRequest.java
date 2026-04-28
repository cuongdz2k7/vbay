package com.vbay.shared.dto.auctionDTO;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import com.vbay.shared.dto.productDTO.CreateProductRequest;

public class CreateAuctionRequest {
    
    private CreateProductRequest product;

    private String title;
    private String description;
    private BigDecimal startingPrice;
    private BigDecimal reservePrice; ///có thể null nếu không có reserve price (nên để nullable)
    private BigDecimal buyNowPrice; ///có thể null nếu không có buy now price
    private BigDecimal minimumBidStep;
    private LocalDateTime startingTime;
    private LocalDateTime endingTime;

    public CreateAuctionRequest (CreateProductRequest product, String title, String description, BigDecimal startingPrice, BigDecimal reservePrice, BigDecimal buyNowPrice, BigDecimal minimumBidStep, LocalDateTime startingTime, LocalDateTime endingTime) {
        this.product = product;
        this.title = title;
        this.description = description;
        this.startingPrice = startingPrice;
        this.reservePrice = reservePrice;
        this.buyNowPrice = buyNowPrice;
        this.minimumBidStep = minimumBidStep;
        this.startingTime = startingTime;
        this.endingTime = endingTime;
    }

    public CreateProductRequest getProduct() {
        return product;
    }

    public String getTitle() {
        return title;
    }

    public String getDescription() {
        return description;
    }

    public BigDecimal getStartingPrice() {
        return startingPrice;
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

}

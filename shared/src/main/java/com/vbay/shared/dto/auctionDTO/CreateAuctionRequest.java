package com.vbay.shared.dto.auctionDTO;

import java.time.LocalDateTime;

import com.vbay.shared.dto.productDTO.CreateProductRequest;

public class CreateAuctionRequest {
    
    private CreateProductRequest product;

    private String title;
    private String description;
    private double startingPrice;
    private Double reservePrice; ///có thể null nếu không có reserve price (nên để nullable)
    private Double buyNowPrice; ///có thể null nếu không có buy now price
    private double minimumBidStep;
    private LocalDateTime startingTime;
    private LocalDateTime endingTime;

    public CreateAuctionRequest (CreateProductRequest product, String title, String description, double startingPrice, Double reservePrice, Double buyNowPrice, double minimumBidStep, LocalDateTime startingTime, LocalDateTime endingTime) {
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

    public double getStartingPrice() {
        return startingPrice;
    }

    public Double getReservePrice() {
        return reservePrice;
    }

    public Double getBuyNowPrice() {
        return buyNowPrice;
    }

    public double getMinimumBidStep() {
        return minimumBidStep;
    }

    public LocalDateTime getStartingTime() {
        return startingTime;
    }

    public LocalDateTime getEndingTime() {
        return endingTime;
    }

}

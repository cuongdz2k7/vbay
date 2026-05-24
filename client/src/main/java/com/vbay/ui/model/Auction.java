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
    private final Long winnerUserId;
    private final Boolean reserveMet;
    private final boolean antiSnipeExtended;
    private final LocalDateTime startingTime;
    private final LocalDateTime endingTime;
    private final Product product;
    private final Long viewerAutobidId;
    private final BigDecimal viewerMaxBidAmount;
    private final String viewerAutobidStatus;
    private final boolean viewerAutobidWinning;
    private final boolean viewerShowActiveMaxBid;
    private final LocalDateTime viewerBidStateUpdatedAt;

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
        Long winnerUserId,
        Boolean reserveMet,
        LocalDateTime startingTime,
        LocalDateTime endingTime,
        Product product
    ) {
        this(
            id,
            version,
            sellerId,
            title,
            description,
            status,
            startingPrice,
            currentPrice,
            minimumBidStep,
            buyNowPrice,
            winnerUserId,
            reserveMet,
            false,
            startingTime,
            endingTime,
            product,
            null,
            null,
            null,
            false,
            false,
            null
        );
    }

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
        Long winnerUserId,
        Boolean reserveMet,
        boolean antiSnipeExtended,
        LocalDateTime startingTime,
        LocalDateTime endingTime,
        Product product,
        Long viewerAutobidId,
        BigDecimal viewerMaxBidAmount,
        String viewerAutobidStatus,
        boolean viewerAutobidWinning,
        boolean viewerShowActiveMaxBid,
        LocalDateTime viewerBidStateUpdatedAt
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
        this.winnerUserId = winnerUserId;
        this.reserveMet = reserveMet;
        this.antiSnipeExtended = antiSnipeExtended;
        this.startingTime = startingTime;
        this.endingTime = endingTime;
        this.product = product;
        this.viewerAutobidId = viewerAutobidId;
        this.viewerMaxBidAmount = viewerMaxBidAmount;
        this.viewerAutobidStatus = viewerAutobidStatus;
        this.viewerAutobidWinning = viewerAutobidWinning;
        this.viewerShowActiveMaxBid = viewerShowActiveMaxBid;
        this.viewerBidStateUpdatedAt = viewerBidStateUpdatedAt;
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

    public Long getWinnerUserId() {
        return winnerUserId;
    }

    public Boolean getReserveMet() {
        return reserveMet;
    }

    public boolean isAntiSnipeExtended() {
        return antiSnipeExtended;
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

    public Long getViewerAutobidId() {
        return viewerAutobidId;
    }

    public BigDecimal getViewerMaxBidAmount() {
        return viewerMaxBidAmount;
    }

    public String getViewerAutobidStatus() {
        return viewerAutobidStatus;
    }

    public boolean isViewerAutobidWinning() {
        return viewerAutobidWinning;
    }

    public boolean isViewerShowActiveMaxBid() {
        return viewerShowActiveMaxBid;
    }

    public LocalDateTime getViewerBidStateUpdatedAt() {
        return viewerBidStateUpdatedAt;
    }
}

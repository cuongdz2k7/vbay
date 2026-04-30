package com.vbay.server.model;

import java.math.BigDecimal;

public class Bid {
    private long id;
    private long auctionId;
    private long bidderId;
    private BigDecimal bidAmount;
    private String bidTime;
    private boolean winner;

    public Bid(long auctionId, long bidderId, BigDecimal bidAmount, String bidTime) {
        this.auctionId = auctionId;
        this.bidderId = bidderId;
        this.bidAmount = bidAmount;
        this.bidTime = bidTime;
        this.winner = false;
    }

    public long getId() {
        return id;
    }

    public long getAuctionId() {
        return auctionId;
    }

    public long getBidderId() {
        return bidderId;
    }

    public BigDecimal getBidAmount() {
        return bidAmount;
    }

    public String getBidTime() {
        return bidTime;
    }

    public boolean isWinner() {
        return winner;
    }

    public void setId(long id) {
        this.id = id;
    }

    public void setAuctionId(long auctionId) {
        this.auctionId = auctionId;
    }

    public void setBidderId(long bidderId) {
        this.bidderId = bidderId;
    }

    public void setBidAmount(BigDecimal bidAmount) {
        this.bidAmount = bidAmount;
    }

    public void setBidTime(String bidTime) {
        this.bidTime = bidTime;
    }

    public void setWinner(boolean winner) {
        this.winner = winner;
    }
}

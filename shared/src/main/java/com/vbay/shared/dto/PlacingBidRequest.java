package com.vbay.shared.dto;

public class PlacingBidRequest {
    private String auctionId;
    private String bidderId;
    private double bidAmount;

    public PlacingBidRequest(String auctionId, String bidderId, double bidAmount) {
        this.auctionId = auctionId;
        this.bidderId = bidderId;
        this.bidAmount = bidAmount;
    }

    public String getAuctionId() {
        return auctionId;
    }

    public String getBidderId() {
        return bidderId;
    }

    public double getBidAmount() {
        return bidAmount;
    }

    @Override
    public String toString() {
        return "PlacingBidRequest{" +
            "auctionId='" + this.getAuctionId() + '\'' +
            ", bidderId='" + this.getBidderId() + '\'' +
            ", bidAmount=" + this.getBidAmount() +
            '}';
    }
}

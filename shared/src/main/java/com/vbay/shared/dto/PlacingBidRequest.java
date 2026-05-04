package com.vbay.shared.dto;

///dành cho manual place bid
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

}

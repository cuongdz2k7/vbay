package com.vbay.shared.dto;

public class EndAuctionRequest {
    private String auctionId;
    private String sellerId;

    public EndAuctionRequest(String auctionId, String sellerId) {
        this.auctionId = auctionId;
        this.sellerId = sellerId;
    }

    public String getAuctionId() {
        return auctionId;
    }

    public String getSellerId() {
        return sellerId;
    }

    @Override
    public String toString() {
        return "EndAuctionRequest{" +
            "auctionId='" + this.getAuctionId() + '\'' +
            ", sellerId='" + this.getSellerId() + '\'' +
            '}';
    }
}

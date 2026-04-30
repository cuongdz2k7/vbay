package com.vbay.shared.dto;

public class PublishAuctionsRequest {
    private String auctionId;
    private String sellerId;

    public PublishAuctionsRequest(String auctionId, String sellerId) {
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
        return "PublishAuctionsRequest{" +
            "auctionId='" + this.getAuctionId() + '\'' +
            ", sellerId='" + this.getSellerId() + '\'' +
            '}';
    }
}

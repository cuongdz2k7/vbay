package com.vbay.shared.dto.auctionDTO;

public class AuctionDetailRequest {
    private long auctionId;

    public AuctionDetailRequest() {
    }

    public AuctionDetailRequest(long auctionId) {
        this.auctionId = auctionId;
    }

    public long getAuctionId() {
        return auctionId;
    }

    public void setAuctionId(long auctionId) {
        this.auctionId = auctionId;
    }
}

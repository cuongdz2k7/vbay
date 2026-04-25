package com.vbay.shared.dto.auctionDTO;

public class CancelAuctionRequest {
    private String auctionId;
    private String sellerId;
    private String reason;

    public CancelAuctionRequest(String auctionId, String sellerId, String reason) {
        this.auctionId = auctionId;
        this.sellerId = sellerId;
        this.reason = reason;
    }

    public String getAuctionId() {
        return auctionId;
    }

    public String getSellerId() {
        return sellerId;
    }

    public String getReason() {
        return reason;
    }

    @Override
    public String toString() {
        return "CancelAuctionRequest{" +
            "auctionId='" + this.getAuctionId() + '\'' +
            ", sellerId='" + this.getSellerId() + '\'' +
            ", reason='" + this.getReason() + '\'' +
            '}';
    }
}

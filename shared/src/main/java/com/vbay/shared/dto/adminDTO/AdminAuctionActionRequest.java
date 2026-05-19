package com.vbay.shared.dto.adminDTO;

public class AdminAuctionActionRequest {
    private long auctionId;
    private String reason;

    public AdminAuctionActionRequest() {}

    public AdminAuctionActionRequest(long auctionId, String reason) {
        this.auctionId = auctionId;
        this.reason = reason;
    }

    public long getAuctionId() { return auctionId; }
    public void setAuctionId(long auctionId) { this.auctionId = auctionId; }

    public String getReason() { return reason; }
    public void setReason(String reason) { this.reason = reason; }
}

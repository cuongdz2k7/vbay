package com.vbay.shared.dto.adminDTO;

import java.util.List;

public class AdminAuctionListResponse {
    private List<AdminAuctionItem> auctions;

    public AdminAuctionListResponse() {}

    public AdminAuctionListResponse(List<AdminAuctionItem> auctions) {
        this.auctions = auctions;
    }

    public List<AdminAuctionItem> getAuctions() { return auctions; }
    public void setAuctions(List<AdminAuctionItem> auctions) { this.auctions = auctions; }
}

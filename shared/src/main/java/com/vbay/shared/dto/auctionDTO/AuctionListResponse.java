package com.vbay.shared.dto.auctionDTO;

import java.util.List;

import com.vbay.shared.dto.realtimeDTO.payload.AuctionListItemPayload;

public class AuctionListResponse {
    private List<AuctionListItemPayload> items;

    public AuctionListResponse() {
    }

    public AuctionListResponse(List<AuctionListItemPayload> items) {
        this.items = items;
    }

    public List<AuctionListItemPayload> getItems() {
        return items;
    }

    public void setItems(List<AuctionListItemPayload> items) {
        this.items = items;
    }
}

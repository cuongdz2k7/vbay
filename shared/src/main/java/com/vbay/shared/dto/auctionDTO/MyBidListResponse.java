package com.vbay.shared.dto.auctionDTO;

import java.util.List;

import com.vbay.shared.dto.realtimeDTO.payload.MyBidListItemPayload;

public class MyBidListResponse {
    private List<MyBidListItemPayload> items;

    public MyBidListResponse() {
    }

    public MyBidListResponse(List<MyBidListItemPayload> items) {
        this.items = items;
    }

    public List<MyBidListItemPayload> getItems() {
        return items;
    }

    public void setItems(List<MyBidListItemPayload> items) {
        this.items = items;
    }
}

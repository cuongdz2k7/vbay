package com.vbay.server.realtime.domain;

import java.time.LocalDateTime;

import com.vbay.server.service.result.PlaceBidResult;

public class BidUpdatedDomainEvent implements DomainEvent {
    private final PlaceBidResult result;
    private final LocalDateTime occurredAt;

    public BidUpdatedDomainEvent(PlaceBidResult result) {
        this.result = result;
        this.occurredAt = result.getBidTime();
    }

    public PlaceBidResult getResult() {
        return result;
    }

    @Override
    public LocalDateTime occurredAt() {
        return occurredAt;
    }
}

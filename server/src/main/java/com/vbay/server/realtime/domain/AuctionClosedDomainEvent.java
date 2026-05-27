package com.vbay.server.realtime.domain;

import java.time.LocalDateTime;

import com.vbay.server.service.result.AuctionClosedResult;

public class AuctionClosedDomainEvent implements DomainEvent {
    private final AuctionClosedResult result;

    public AuctionClosedDomainEvent(AuctionClosedResult result) {
        this.result = result;
    }

    public AuctionClosedResult getResult() {
        return result;
    }

    @Override
    public LocalDateTime occurredAt() {
        return result.getClosedAt();
    }
}

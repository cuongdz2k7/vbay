package com.vbay.server.realtime.domain;

import java.time.LocalDateTime;

import com.vbay.server.service.result.CreateAuctionResult;

public class AuctionCreatedDomainEvent implements DomainEvent {
    private final CreateAuctionResult result;
    private final LocalDateTime occurredAt;

    public AuctionCreatedDomainEvent(CreateAuctionResult result, LocalDateTime occurredAt) {
        this.result = result;
        this.occurredAt = occurredAt;
    }

    public CreateAuctionResult getResult() {
        return result;
    }

    @Override
    public LocalDateTime occurredAt() {
        return occurredAt;
    }
}


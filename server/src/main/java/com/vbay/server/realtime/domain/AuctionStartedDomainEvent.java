package com.vbay.server.realtime.domain;

import java.time.LocalDateTime;

import com.vbay.server.service.result.AuctionListItemResult;

public class AuctionStartedDomainEvent implements DomainEvent {
    private final AuctionListItemResult auction;
    private final LocalDateTime occurredAt;

    public AuctionStartedDomainEvent(AuctionListItemResult auction, LocalDateTime occurredAt) {
        this.auction = auction;
        this.occurredAt = occurredAt;
    }

    public AuctionListItemResult getAuction() {
        return auction;
    }

    @Override
    public LocalDateTime occurredAt() {
        return occurredAt;
    }
}

package com.vbay.server.realtime.domain;

import java.time.LocalDateTime;

import com.vbay.server.service.result.BuyNowResult;


public class BuyNowDomainEvent implements DomainEvent {
    private final BuyNowResult result;
    private final LocalDateTime occurredAt;

    public BuyNowDomainEvent(BuyNowResult result) {
        this.result = result;
        this.occurredAt = result.getBoughtAt();
    }

    public BuyNowResult getResult() {
        return result;
    }

    @Override
    public LocalDateTime occurredAt() {
        return occurredAt;
    }
}

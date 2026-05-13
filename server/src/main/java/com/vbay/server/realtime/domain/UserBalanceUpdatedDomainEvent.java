package com.vbay.server.realtime.domain;

import java.time.LocalDateTime;

import com.vbay.server.service.result.UserBalanceResult;

public class UserBalanceUpdatedDomainEvent implements DomainEvent {
    private final UserBalanceResult result;
    private final LocalDateTime occurredAt;

    public UserBalanceUpdatedDomainEvent(UserBalanceResult result, LocalDateTime occurredAt) {
        this.result = result;
        this.occurredAt = occurredAt;
    }

    public UserBalanceResult getResult() {
        return result;
    }

    @Override
    public LocalDateTime occurredAt() {
        return occurredAt;
    }
}

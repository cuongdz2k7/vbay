package com.vbay.server.realtime.domain;

import java.time.LocalDateTime;

import com.vbay.server.service.result.AutobidUpdateResult;

/*
 AutobidUpdatedDomainEvent = cập nhật AutoBid contract, không nhất thiết có bid mới.
 (nó chỉ đơn giản là update maxBidAmount của AutoBid contract, hoặc update status của AutoBid contract)
*/
public class AutobidUpdatedDomainEvent implements DomainEvent {
    private final AutobidUpdateResult result;
    private final LocalDateTime occurredAt;

    public AutobidUpdatedDomainEvent(AutobidUpdateResult result) {
        this.result = result;
        this.occurredAt = result.getUpdatedAt();
    }

    public AutobidUpdateResult getResult() {
        return result;
    }

    @Override
    public LocalDateTime occurredAt() {
        return occurredAt;
    }
}
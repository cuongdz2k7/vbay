package com.vbay.server.realtime.domain;

import java.time.LocalDateTime;

import com.vbay.server.realtime.domain.enums.AuctionListItemUpdateReason;
import com.vbay.server.service.result.AuctionListItemResult;

public class AuctionListItemUpdatedDomainEvent implements DomainEvent {
    private final AuctionListItemResult auctionListItem;
    private final AuctionListItemUpdateReason reason;
    private final LocalDateTime occurredAt;

    public AuctionListItemUpdatedDomainEvent(
            AuctionListItemResult auctionListItem,
            AuctionListItemUpdateReason reason,
            LocalDateTime occurredAt) {
        this.auctionListItem = auctionListItem;
        this.reason = reason;
        this.occurredAt = occurredAt;
    }

    public AuctionListItemResult getAuctionListItem() {
        return auctionListItem;
    }

    public AuctionListItemUpdateReason getReason() {
        return reason;
    }

    @Override
    public LocalDateTime occurredAt() {
        return occurredAt;
    }
}

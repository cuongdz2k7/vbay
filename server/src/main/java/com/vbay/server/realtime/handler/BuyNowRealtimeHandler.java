package com.vbay.server.realtime.handler;

import com.vbay.server.realtime.domain.BuyNowDomainEvent;
import com.vbay.server.realtime.domain.DomainEvent;
import com.vbay.server.realtime.mapper.RealtimeEventMapper;
import com.vbay.server.realtime.transport.RealtimeBroadcaster;

public class BuyNowRealtimeHandler implements DomainEventHandler {
    private final RealtimeBroadcaster broadcaster;
    private final RealtimeEventMapper mapper;

    public BuyNowRealtimeHandler(RealtimeBroadcaster broadcaster, RealtimeEventMapper mapper) {
        this.broadcaster = broadcaster;
        this.mapper = mapper;
    }

    @Override
    public boolean support(DomainEvent event) {
        return event instanceof BuyNowDomainEvent;
    }

    @Override
    public void handle(DomainEvent event) {
        BuyNowDomainEvent buyNowEvent = (BuyNowDomainEvent) event;

        broadcaster.broadcast(mapper.toAuctionEndedEvent(buyNowEvent));
        broadcaster.broadcast(mapper.toBuyNowHistoryItemEvent(buyNowEvent));

        // Notification BUY_NOW_PURCHASED / BUY_NOW_SOLD để sau.
    }
}


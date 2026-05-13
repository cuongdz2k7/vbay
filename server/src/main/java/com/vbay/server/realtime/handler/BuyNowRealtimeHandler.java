package com.vbay.server.realtime.handler;

import com.vbay.server.realtime.domain.BuyNowDomainEvent;
import com.vbay.server.realtime.domain.DomainEvent;
import com.vbay.server.realtime.mapper.RealtimeEventMapper;
import com.vbay.server.realtime.transport.RealtimeBroadcaster;

public class BuyNowRealtimeHandler extends AbstractRealtimeHandler implements DomainEventHandler {
    public BuyNowRealtimeHandler(
            RealtimeBroadcaster broadcaster,
            RealtimeEventMapper mapper) {
        super(broadcaster, mapper);
    }

    @Override
    public boolean support(DomainEvent event) {
        return event instanceof BuyNowDomainEvent;
    }

    @Override
    public void handle(DomainEvent event) {
        BuyNowDomainEvent buyNowEvent = (BuyNowDomainEvent) event;

        broadcaster.broadcast(mapper.toAuctionStateEvent(buyNowEvent));

        // Notification BUY_NOW_PURCHASED / BUY_NOW_SOLD để sau.
    }
}


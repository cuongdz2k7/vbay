package com.vbay.server.realtime.handler;

import com.vbay.server.realtime.domain.AuctionListItemUpdatedDomainEvent;
import com.vbay.server.realtime.domain.DomainEvent;
import com.vbay.server.realtime.mapper.RealtimeEventMapper;
import com.vbay.server.realtime.transport.RealtimeBroadcaster;

public class AuctionListItemUpdatedRealtimeHandler extends AbstractRealtimeHandler implements DomainEventHandler {
    public AuctionListItemUpdatedRealtimeHandler(
            RealtimeBroadcaster broadcaster,
            RealtimeEventMapper mapper) {
        super(broadcaster, mapper);
    }

    @Override
    public boolean support(DomainEvent event) {
        return event instanceof AuctionListItemUpdatedDomainEvent;
    }

    @Override
    public void handle(DomainEvent event) {
        AuctionListItemUpdatedDomainEvent itemUpdatedEvent = (AuctionListItemUpdatedDomainEvent) event;
        broadcaster.broadcast(mapper.toAuctionListItemUpdatedEvent(itemUpdatedEvent));
    }
}

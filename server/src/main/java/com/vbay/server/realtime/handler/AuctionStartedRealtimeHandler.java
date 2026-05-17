package com.vbay.server.realtime.handler;

import com.vbay.server.realtime.domain.AuctionStartedDomainEvent;
import com.vbay.server.realtime.domain.DomainEvent;
import com.vbay.server.realtime.mapper.RealtimeEventMapper;
import com.vbay.server.realtime.transport.RealtimeBroadcaster;

public class AuctionStartedRealtimeHandler extends AbstractRealtimeHandler implements DomainEventHandler {
    public AuctionStartedRealtimeHandler(
            RealtimeBroadcaster broadcaster,
            RealtimeEventMapper mapper) {
        super(broadcaster, mapper);
    }

    @Override
    public boolean support(DomainEvent event) {
        return event instanceof AuctionStartedDomainEvent;
    }

    @Override
    public void handle(DomainEvent event) {
        AuctionStartedDomainEvent startedEvent = (AuctionStartedDomainEvent) event;
        broadcaster.broadcast(mapper.toAuctionStateEvent(startedEvent));
    }
}

package com.vbay.server.realtime.handler;

import com.vbay.server.realtime.domain.BidUpdatedDomainEvent;
import com.vbay.server.realtime.domain.DomainEvent;
import com.vbay.server.realtime.mapper.RealtimeEventMapper;
import com.vbay.server.realtime.transport.RealtimeBroadcaster;

public class BidUpdatedRealtimeHandler implements DomainEventHandler {
    private final RealtimeBroadcaster broadcaster;
    private final RealtimeEventMapper mapper;

    public BidUpdatedRealtimeHandler(RealtimeBroadcaster broadcaster, RealtimeEventMapper mapper) {
        this.broadcaster = broadcaster;
        this.mapper = mapper;
    }

    @Override
    public boolean support(DomainEvent event) {
        return event instanceof BidUpdatedDomainEvent;
    }

    @Override
    public void handle(DomainEvent event) {
        BidUpdatedDomainEvent bidEvent = (BidUpdatedDomainEvent) event; 

        broadcaster.broadcast(mapper.toAuctionStateUpdatedEvent(bidEvent));
        broadcaster.broadcast(mapper.toBidHistoryItemAddedEvent(bidEvent));

        // Notification OUTBID để sau.
    }
}

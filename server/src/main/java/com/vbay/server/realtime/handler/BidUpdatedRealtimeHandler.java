package com.vbay.server.realtime.handler;

import com.vbay.server.realtime.domain.BidUpdatedDomainEvent;
import com.vbay.server.realtime.domain.DomainEvent;
import com.vbay.server.realtime.mapper.RealtimeEventMapper;
import com.vbay.server.realtime.transport.RealtimeBroadcaster;

public class BidUpdatedRealtimeHandler extends AbstractRealtimeHandler implements DomainEventHandler {
    public BidUpdatedRealtimeHandler(
            RealtimeBroadcaster broadcaster,
            RealtimeEventMapper mapper) {
        super(broadcaster, mapper);
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

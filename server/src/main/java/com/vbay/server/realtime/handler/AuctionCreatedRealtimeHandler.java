package com.vbay.server.realtime.handler;

import com.vbay.server.realtime.domain.AuctionCreatedDomainEvent;
import com.vbay.server.realtime.domain.DomainEvent;
import com.vbay.server.realtime.mapper.RealtimeEventMapper;
import com.vbay.server.realtime.transport.RealtimeBroadcaster;

public class AuctionCreatedRealtimeHandler implements DomainEventHandler {
    private final RealtimeBroadcaster broadcaster;
    private final RealtimeEventMapper mapper;

    public AuctionCreatedRealtimeHandler(RealtimeBroadcaster broadcaster, RealtimeEventMapper mapper) {
        this.broadcaster = broadcaster;
        this.mapper = mapper;
    }

    @Override
    public boolean support(DomainEvent event) {
        return event instanceof AuctionCreatedDomainEvent;
    }

    @Override
    public void handle(DomainEvent event) {
        AuctionCreatedDomainEvent auctionEvent = (AuctionCreatedDomainEvent) event;

        broadcaster.broadcast(mapper.toAuctionCreatedListItemEvent(auctionEvent));

        // Notification AUCTION_CREATED_SUCCESS để sau.
    }
}

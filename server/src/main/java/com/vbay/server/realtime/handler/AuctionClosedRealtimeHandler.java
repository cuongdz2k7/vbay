package com.vbay.server.realtime.handler;

import com.vbay.server.realtime.domain.AuctionClosedDomainEvent;
import com.vbay.server.realtime.domain.DomainEvent;
import com.vbay.server.realtime.mapper.RealtimeEventMapper;
import com.vbay.server.realtime.transport.RealtimeBroadcaster;
import com.vbay.server.service.result.UserMyBidListItemResult;

public class AuctionClosedRealtimeHandler extends AbstractRealtimeHandler implements DomainEventHandler {
    public AuctionClosedRealtimeHandler(
            RealtimeBroadcaster broadcaster,
            RealtimeEventMapper mapper) {
        super(broadcaster, mapper);
    }

    @Override
    public boolean support(DomainEvent event) {
        return event instanceof AuctionClosedDomainEvent;
    }

    @Override
    public void handle(DomainEvent event) {
        AuctionClosedDomainEvent closedEvent = (AuctionClosedDomainEvent) event;

        broadcaster.broadcast(mapper.toAuctionStateEvent(closedEvent));
        for (UserMyBidListItemResult item : closedEvent.getResult().getAffectedMyBidItems()) {
            broadcaster.broadcast(mapper.toMyBidListItemUpdatedEvent(item));
        }
    }
}

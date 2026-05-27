package com.vbay.server.realtime.handler;

import com.vbay.server.realtime.domain.BuyNowDomainEvent;
import com.vbay.server.realtime.domain.DomainEvent;
import com.vbay.server.realtime.mapper.RealtimeEventMapper;
import com.vbay.server.realtime.transport.RealtimeBroadcaster;
import com.vbay.server.service.result.UserMyBidListItemResult;

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
        broadcaster.broadcast(mapper.toBuyNowHistoryItemEvent(buyNowEvent));

        for (UserMyBidListItemResult item : buyNowEvent.getResult().getAffectedMyBidItems()) {
            broadcaster.broadcast(mapper.toMyBidListItemUpdatedEvent(item));
        }

        // Notification BUY_NOW_PURCHASED / BUY_NOW_SOLD để sau.
    }
}


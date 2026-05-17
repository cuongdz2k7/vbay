package com.vbay.server.realtime.handler;

import com.vbay.server.realtime.domain.BidUpdatedDomainEvent;
import com.vbay.server.realtime.domain.DomainEvent;
import com.vbay.server.realtime.mapper.RealtimeEventMapper;
import com.vbay.server.realtime.transport.RealtimeBroadcaster;
import com.vbay.server.service.result.UserMyBidListItemResult;

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
        
        broadcaster.broadcast(mapper.toAuctionStateEvent(bidEvent));
        broadcaster.broadcast(mapper.toBidHistoryItemAddedEvent(bidEvent));

        for (UserMyBidListItemResult item : bidEvent.getResult().getAffectedMyBidItems()) {
            broadcaster.broadcast(mapper.toMyBidListItemUpdatedEvent(item));
        }


        ///[LOG] 
        System.out.println("[BID_EVENT] auctionId=" + bidEvent.getResult().getAuctionId()
            + " version=" + bidEvent.getResult().getAuctionVersion()
            + " affectedUsers=" + bidEvent.getResult().getAffectedMyBidItems().stream()
                .map(item -> item.getUserId() + ":" + item.getBidStatus())
                .toList());
        // Notification OUTBID để sau.
    }
}

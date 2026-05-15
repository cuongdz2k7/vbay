package com.vbay.server.scheduler;

import java.util.EnumSet;
import java.util.Set;

import com.vbay.server.realtime.domain.AuctionListItemUpdatedDomainEvent;
import com.vbay.server.realtime.domain.DomainEvent;
import com.vbay.server.realtime.domain.enums.AuctionListItemUpdateReason;
import com.vbay.server.realtime.handler.DomainEventHandler;

public class AuctionScheduleDomainEventHandler implements DomainEventHandler {
    private static final Set<AuctionListItemUpdateReason> SCHEDULING_REASONS = EnumSet.of(
        AuctionListItemUpdateReason.CREATED,
        AuctionListItemUpdateReason.STATUS_CHANGED,
        AuctionListItemUpdateReason.TIME_CHANGED
    );

    private final AuctionTaskScheduler scheduler;

    public AuctionScheduleDomainEventHandler(AuctionTaskScheduler scheduler) {
        this.scheduler = scheduler;
    }

    @Override
    public boolean support(DomainEvent event) {
        return event instanceof AuctionListItemUpdatedDomainEvent;
    }

    @Override
    public void handle(DomainEvent event) {
        AuctionListItemUpdatedDomainEvent auctionEvent = (AuctionListItemUpdatedDomainEvent) event;
        if (!SCHEDULING_REASONS.contains(auctionEvent.getReason())) {
            return;
        }
        scheduler.refreshAuctionSchedule(auctionEvent.getAuctionListItem().getAuctionId(), auctionEvent.getReason());
    }
}

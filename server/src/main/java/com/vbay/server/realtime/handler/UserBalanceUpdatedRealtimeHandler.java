package com.vbay.server.realtime.handler;

import com.vbay.server.realtime.domain.DomainEvent;
import com.vbay.server.realtime.domain.UserBalanceUpdatedDomainEvent;
import com.vbay.server.realtime.mapper.RealtimeEventMapper;
import com.vbay.server.realtime.transport.RealtimeBroadcaster;

public class UserBalanceUpdatedRealtimeHandler extends AbstractRealtimeHandler implements DomainEventHandler {
    public UserBalanceUpdatedRealtimeHandler(
            RealtimeBroadcaster broadcaster,
            RealtimeEventMapper mapper) {
        super(broadcaster, mapper);
    }

    @Override
    public boolean support(DomainEvent event) {
        return event instanceof UserBalanceUpdatedDomainEvent;
    }

    @Override
    public void handle(DomainEvent event) {
        UserBalanceUpdatedDomainEvent balanceEvent = (UserBalanceUpdatedDomainEvent) event;
        broadcaster.broadcast(mapper.toUserBalanceUpdatedEvent(balanceEvent));
    }
}

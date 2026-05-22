package com.vbay.server.realtime.handler;

import com.vbay.server.realtime.domain.AutobidUpdatedDomainEvent;
import com.vbay.server.realtime.domain.DomainEvent;
import com.vbay.server.realtime.mapper.RealtimeEventMapper;
import com.vbay.server.realtime.transport.RealtimeBroadcaster;

public class AutobidUpdatedRealtimeHandler extends AbstractRealtimeHandler implements DomainEventHandler  {
    public AutobidUpdatedRealtimeHandler(
            RealtimeBroadcaster broadcaster,
            RealtimeEventMapper mapper) {
        super(broadcaster, mapper);
    }

    @Override
    public boolean support(DomainEvent event) {
        return event instanceof AutobidUpdatedDomainEvent;
    }

    @Override
    public void handle(DomainEvent event) {
        AutobidUpdatedDomainEvent autobidEvent = (AutobidUpdatedDomainEvent) event;
        broadcaster.broadcast(mapper.toAutobidUpdatedEvent(autobidEvent));
    }
    
}

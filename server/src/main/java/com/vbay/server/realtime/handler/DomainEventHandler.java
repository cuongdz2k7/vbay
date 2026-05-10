package com.vbay.server.realtime.handler;

import com.vbay.server.realtime.domain.DomainEvent;

public interface DomainEventHandler {
    public boolean support (DomainEvent event);
    public void handle(DomainEvent event);
}

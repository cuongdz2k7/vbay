package com.vbay.server.realtime.publisher;

import java.util.List;

import com.vbay.server.realtime.domain.DomainEvent;
import com.vbay.server.realtime.handler.DomainEventHandler;

public class InMemoryDomainEventPublisher implements DomainEventPublisher {
    private final List<DomainEventHandler> handlers;

    public InMemoryDomainEventPublisher(List<DomainEventHandler> handlers) {
        this.handlers = List.copyOf(handlers);
    }

    @Override
    public void publish(DomainEvent event) {
        for (DomainEventHandler handler : handlers) {
            if (handler.support(event)) {
                handler.handle(event);
            }
        }
    }
}
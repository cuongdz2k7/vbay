package com.vbay.server.realtime.publisher;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import com.vbay.server.realtime.domain.DomainEvent;
import com.vbay.server.realtime.handler.DomainEventHandler;

public class InMemoryDomainEventPublisher implements DomainEventPublisher {
    private final List<DomainEventHandler> handlers;

    public InMemoryDomainEventPublisher() {
        this.handlers = new ArrayList<>();
    }

    public InMemoryDomainEventPublisher(List<DomainEventHandler> handlers) {
        this.handlers = new ArrayList<>(handlers);
    }
    public void registerAll(Collection<DomainEventHandler> handlers) {
        this.handlers.addAll(handlers);
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

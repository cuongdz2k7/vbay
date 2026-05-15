package com.vbay.server.realtime.publisher;

import com.vbay.server.realtime.domain.DomainEvent;

public interface DomainEventPublisher {
    void publish(DomainEvent event);
}


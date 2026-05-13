package com.vbay.server.realtime.domain;

import java.time.LocalDateTime;

///tạo interface để có thể đa hình
public interface DomainEvent {
    LocalDateTime occurredAt();
}

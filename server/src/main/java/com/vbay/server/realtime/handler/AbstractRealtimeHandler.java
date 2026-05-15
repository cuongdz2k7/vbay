package com.vbay.server.realtime.handler;

import com.vbay.server.realtime.mapper.RealtimeEventMapper;
import com.vbay.server.realtime.transport.RealtimeBroadcaster;

public abstract class AbstractRealtimeHandler {
    protected final RealtimeBroadcaster broadcaster;
    protected final RealtimeEventMapper mapper;

    protected AbstractRealtimeHandler(
            RealtimeBroadcaster broadcaster,
            RealtimeEventMapper mapper) {
        this.broadcaster = broadcaster;
        this.mapper = mapper;
    }
}

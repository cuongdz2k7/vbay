package com.vbay.network.dispatcher;
import com.vbay.shared.protocol.RealtimeEvent;

@FunctionalInterface
public interface RealtimeEventListener<T> {
    void onEvent(RealtimeEvent<T> event);
}

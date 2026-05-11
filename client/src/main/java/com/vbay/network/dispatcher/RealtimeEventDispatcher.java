package com.vbay.network.dispatcher;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

import com.vbay.shared.enums.realtime.RealtimeEventType;
import com.vbay.shared.protocol.RealtimeEvent;

public class RealtimeEventDispatcher {
    private final Map<RealtimeEventType, List<RealtimeEventListener<?>>> listeners = new ConcurrentHashMap<>();

    public <T> void subscribe(RealtimeEventType type, RealtimeEventListener<T> listener) {
        listeners.computeIfAbsent(type, key -> new CopyOnWriteArrayList<>()).add(listener);
    }

    public <T> void unsubscribe(RealtimeEventType type, RealtimeEventListener<T> listener) {
        List<RealtimeEventListener<?>> eventListeners = listeners.get(type);
        if (eventListeners != null) {
            eventListeners.remove(listener);
        }
    }

    @SuppressWarnings("unchecked")
    public <T> void dispatch(RealtimeEvent<T> event) {
        List<RealtimeEventListener<?>> eventListeners = listeners.get(event.getType());
        if (eventListeners == null) {
            return;
        }

        for (RealtimeEventListener<?> listener : eventListeners) {
            ((RealtimeEventListener<T>) listener).onEvent(event);
        }
    }
}

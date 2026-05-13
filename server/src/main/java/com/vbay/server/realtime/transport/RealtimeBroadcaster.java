package com.vbay.server.realtime.transport;
import com.vbay.server.network_connection.ClientConnection;
import com.vbay.server.realtime.subscription.SubscriptionRegistry;
import com.vbay.shared.protocol.RealtimeEvent;

public class RealtimeBroadcaster {
    private final SubscriptionRegistry registry;

    public RealtimeBroadcaster(SubscriptionRegistry registry) {
        this.registry = registry;
    }

    public void broadcast(RealtimeEvent<?> event) {
        for (ClientConnection connection : registry.findSubscribers(event.getRoom())) {
            connection.send(event);
        }
    }
}

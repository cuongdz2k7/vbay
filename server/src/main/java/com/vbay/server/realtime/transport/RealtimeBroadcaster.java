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
        var subscribers = registry.findSubscribers(event.getRoom());
        System.out.println("[RT_BROADCAST] type=" + event.getType()
            + " room=" + event.getRoom().key()
            + " subscribers=" + subscribers.size());
        for (ClientConnection connection : subscribers) {
            System.out.println("[RT_SEND_TO] type=" + event.getType()
                + " room=" + event.getRoom().key()
                + " userId=" + connection.getSession().getUserId()
                + " username=" + connection.getSession().getUsername());
            connection.send(event);
        }
    }
}

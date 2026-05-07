package com.vbay.server.realtime.subscription;

import com.vbay.server.network_connection.ClientConnection;
import com.vbay.server.network_connection.ClientSession;
import com.vbay.server.realtime.subscription.validation.RoomSubscriptionValidator;
import com.vbay.shared.dto.realtimeDTO.Room;


public class SubscriptionService {
    private final RoomSubscriptionValidator validator;
    private final SubscriptionRegistry registry;

    public SubscriptionService(
        RoomSubscriptionValidator validator,
        SubscriptionRegistry registry
    ) {
        this.validator = validator;
        this.registry = registry;
    }

    public void subscribe(Room room, ClientSession session, ClientConnection connection) {
        validator.validate(room, session);
        registry.subscribe(room, connection);
    }

    public void unsubscribe(Room room, ClientSession session, ClientConnection connection) {
        validator.validate(room, session);
        registry.unsubscribe(room, connection);
    }

    public void disconnect(ClientConnection connection) {
        registry.unsubscribeAll(connection);
    }
}

package com.vbay.server.realtime.subscription;

import com.google.gson.JsonElement;
import com.vbay.server.network_connection.ClientConnection;
import com.vbay.server.network_connection.ClientSession;
import com.vbay.server.realtime.subscription.validation.RoomSubscriptionValidator;
import com.vbay.shared.Utils.JsonUtils;
import com.vbay.shared.dto.realtimeDTO.Room;
import com.vbay.shared.protocol.Respond;


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
        System.out.println("[RT_SUBSCRIBE] room=" + room.key()
            + " userId=" + session.getUserId()
            + " username=" + session.getUsername());
    }

    public void unsubscribe(Room room, ClientSession session, ClientConnection connection) {
        validator.validate(room, session);
        registry.unsubscribe(room, connection);
        System.out.println("[RT_UNSUBSCRIBE] room=" + room.key()
            + " userId=" + session.getUserId()
            + " username=" + session.getUsername());
    }

    public Respond<Void> handleSubscribeRoom(String requestId, JsonElement payload, ClientSession session, ClientConnection connection) {
        Room room = JsonUtils.fromJson(payload, Room.class);
        if (room == null) {
            return new Respond<>(requestId, false, "Invalid subscribe room request", null);
        }
        subscribe(room, session, connection);
        return new Respond<>(requestId, true, "Subscribed room successfully", null);
    }

    public Respond<Void> handleUnsubscribeRoom(String requestId, JsonElement payload, ClientSession session, ClientConnection connection) {
        Room room = JsonUtils.fromJson(payload, Room.class);
        if (room == null) {
            return new Respond<>(requestId, false, "Invalid unsubscribe room request", null);
        }
        unsubscribe(room, session, connection);
        return new Respond<>(requestId, true, "Unsubscribed room successfully", null);
    }

    public void disconnect(ClientConnection connection) {
        System.out.println("[RT_DISCONNECT] userId=" + connection.getSession().getUserId()
            + " username=" + connection.getSession().getUsername());
        registry.unsubscribeAll(connection);
    }
}

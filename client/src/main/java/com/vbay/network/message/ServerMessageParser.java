package com.vbay.network.message;

import java.time.LocalDateTime;
import java.util.EnumMap;
import java.util.Map;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.vbay.shared.Utils.JsonUtils;
import com.vbay.shared.dto.realtimeDTO.Room;
import com.vbay.shared.dto.realtimeDTO.payload.AuctionListItemPayload;
import com.vbay.shared.dto.realtimeDTO.payload.AuctionStatePayload;
import com.vbay.shared.dto.realtimeDTO.payload.AutobidUpdatedPayload;
import com.vbay.shared.dto.realtimeDTO.payload.BidHistoryItemPayload;
import com.vbay.shared.dto.realtimeDTO.payload.MyBidListItemPayload;
import com.vbay.shared.dto.realtimeDTO.payload.NotificationPayload;
import com.vbay.shared.dto.realtimeDTO.payload.DepositRequestPayload;
import com.vbay.shared.dto.realtimeDTO.payload.UserBalanceUpdatedPayload;
import com.vbay.shared.dto.realtimeDTO.payload.WatcherCountPayload;
import com.vbay.shared.dto.realtimeDTO.payload.UserWarnedPayload;
import com.vbay.shared.enums.MessageType;
import com.vbay.shared.enums.realtime.RealtimeEventType;
import com.vbay.shared.protocol.RealtimeEvent;
import com.vbay.shared.protocol.Respond;

public class ServerMessageParser {
    private final Map<RealtimeEventType, Class<?>> payloadTypes;

    public ServerMessageParser() {
        this.payloadTypes = createDefaultPayloadTypes();
    }

    public ServerMessage parse(String rawMessage) {
        JsonObject root = JsonUtils.fromJson(rawMessage, JsonObject.class);
        if (root == null) {
            throw new IllegalArgumentException("Invalid server message");
        }

        MessageType messageType = readMessageType(root);
        return switch (messageType) {
            case RESPONSE -> ServerMessage.response(parseResponse(rawMessage));
            case EVENT -> ServerMessage.event(parseRealtimeEvent(root));
            default -> throw new IllegalArgumentException("Unsupported message type: " + messageType);
        };
    }

    private Respond<?> parseResponse(String rawMessage) {
        Respond<?> response = JsonUtils.fromJson(rawMessage, Respond.class);
        if (response == null) {
            throw new IllegalArgumentException("Invalid response message");
        }
        if (response.getRequestId() == null || response.getRequestId().isBlank()) {
            throw new IllegalArgumentException("Response is missing requestId");
        }
        return response;
    }

    private RealtimeEvent<?> parseRealtimeEvent(JsonObject root) {
        RealtimeEventType type = readRealtimeEventType(root);
        Room room = JsonUtils.fromJson(root.get("room"), Room.class);
        Object payload = parsePayload(type, root.get("payload"));

        RealtimeEvent<Object> event = new RealtimeEvent<>(type, room, payload);
        event.setMessageType(MessageType.EVENT);

        JsonElement eventId = root.get("eventId");
        if (eventId != null && !eventId.isJsonNull()) {
            event.setEventId(eventId.getAsString());
        }

        JsonElement occurredAt = root.get("occurredAt");
        if (occurredAt != null && !occurredAt.isJsonNull()) {
            event.setOccurredAt(JsonUtils.fromJson(occurredAt, LocalDateTime.class));
        }

        return event;
    }

    private Object parsePayload(RealtimeEventType type, JsonElement payloadElement) {
        if (payloadElement == null || payloadElement.isJsonNull()) {
            return null;
        }

        Class<?> payloadType = payloadTypes.get(type);
        if (payloadType == null) {
            throw new IllegalArgumentException("Unsupported realtime event type: " + type);
        }

        return JsonUtils.fromJson(payloadElement, payloadType);
    }

    private RealtimeEventType readRealtimeEventType(JsonObject root) {
        JsonElement type = root.get("type");
        if (type == null || type.isJsonNull()) {
            throw new IllegalArgumentException("Missing realtime event type");
        }
        return RealtimeEventType.valueOf(type.getAsString());
    }

    private MessageType readMessageType(JsonObject root) {
        JsonElement messageType = root.get("messageType");
        if (messageType == null || messageType.isJsonNull()) {
            throw new IllegalArgumentException("Missing messageType");
        }

        String messageTypeRaw = messageType.getAsString();
        if (messageTypeRaw == null || messageTypeRaw.isBlank()) {
            throw new IllegalArgumentException("Missing messageType");
        }

        try {
            return MessageType.valueOf(messageTypeRaw);
        } catch (IllegalArgumentException exception) {
            throw new IllegalArgumentException("Unsupported messageType: " + messageTypeRaw, exception);
        }
    }

    private Map<RealtimeEventType, Class<?>> createDefaultPayloadTypes() {
        Map<RealtimeEventType, Class<?>> types = new EnumMap<>(RealtimeEventType.class);
        types.put(RealtimeEventType.AUCTION_STATE_UPDATED, AuctionStatePayload.class);
        types.put(RealtimeEventType.BID_HISTORY_ITEM_ADDED, BidHistoryItemPayload.class);
        types.put(RealtimeEventType.MY_BID_LIST_ITEM_UPDATED, MyBidListItemPayload.class);
        types.put(RealtimeEventType.NOTIFICATION_CREATED, NotificationPayload.class);
        types.put(RealtimeEventType.AUCTION_LIST_ITEM_UPDATED, AuctionListItemPayload.class);
        types.put(RealtimeEventType.USER_BALANCE_UPDATED, UserBalanceUpdatedPayload.class);
        types.put(RealtimeEventType.WATCHER_COUNT_CHANGED, WatcherCountPayload.class);
        types.put(RealtimeEventType.DEPOSIT_REQUEST_UPDATED, DepositRequestPayload.class);
        types.put(RealtimeEventType.ADMIN_DEPOSIT_REQUESTED, DepositRequestPayload.class);
        types.put(RealtimeEventType.ADMIN_USER_KICKED, Void.class);
        types.put(RealtimeEventType.ADMIN_USER_STATUS_CHANGED, Void.class);
        types.put(RealtimeEventType.ADMIN_USER_WARNED, UserWarnedPayload.class);
        types.put(RealtimeEventType.AUTOBID_UPDATED, AutobidUpdatedPayload.class);
        return types;
    }
}

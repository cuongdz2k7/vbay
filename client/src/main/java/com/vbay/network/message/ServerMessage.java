package com.vbay.network.message;

import com.vbay.shared.enums.MessageType;
import com.vbay.shared.protocol.RealtimeEvent;
import com.vbay.shared.protocol.Respond;

public class ServerMessage {
    private final MessageType messageType;
    private final Respond<?> response;
    private final RealtimeEvent<?> event;

    private ServerMessage(MessageType messageType, Respond<?> response, RealtimeEvent<?> event) {
        this.messageType = messageType;
        this.response = response;
        this.event = event;
    }

    public static ServerMessage response(Respond<?> response) {
        return new ServerMessage(MessageType.RESPONSE, response, null);
    }

    public static ServerMessage event(RealtimeEvent<?> event) {
        return new ServerMessage(MessageType.EVENT, null, event);
    }

    public MessageType getMessageType() {
        return messageType;
    }

    public Respond<?> getResponse() {
        return response;
    }

    public RealtimeEvent<?> getEvent() {
        return event;
    }
}

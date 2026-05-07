package com.vbay.shared.protocol;

import java.time.LocalDateTime;

import com.vbay.shared.Utils.IDGenerator;
import com.vbay.shared.dto.realtimeDTO.Room;
import com.vbay.shared.enums.MessageType;
import com.vbay.shared.enums.realtime.RealtimeEventType;



public class RealtimeEvent<T> {
    private MessageType messageType = MessageType.EVENT;
    private String eventId;
    private RealtimeEventType type;
    private Room room; ///là realtimeEvent này thì nó thuộc room nào?
    private T payload;
    private LocalDateTime occurredAt; 

    public RealtimeEvent(RealtimeEventType type, Room room, T payload) {
        this.type = type;
        this.eventId = IDGenerator.generateID();
        this.room = room;
        this.payload = payload;
    }

    public Room getRoom() {
        return room;
    }

    public String getEventId() {
        return eventId;
    }
    public RealtimeEventType getType() {
        return type;
    }
    public T getPayload() {
        return payload;
    }
    public LocalDateTime getOccurredAt() {
        return occurredAt;
    }
    public void setOccurredAt(LocalDateTime occurredAt) {
        this.occurredAt = occurredAt;
    }
    public void setType(RealtimeEventType type) {
        this.type = type;
    }
    public void setPayload(T payload) {
        this.payload = payload;
    }
    public void setRoom(Room room) {
        this.room = room;
    }
    public void setEventId(String eventId) {
        this.eventId = eventId;
    }
    public MessageType getMessageType() {
        return messageType;
    }
    public void setMessageType(MessageType messageType) {
        this.messageType = messageType;
    }
    
}

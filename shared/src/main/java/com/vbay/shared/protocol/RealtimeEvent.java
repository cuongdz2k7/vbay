package com.vbay.shared.protocol;

import java.time.LocalDateTime;

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

    
}

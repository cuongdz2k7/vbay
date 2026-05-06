package com.vbay.shared.dto.realtimeDTO;

import com.vbay.shared.enums.realtime.RoomType;

public class Room {
    private RoomType type;
    private Long targetId;
    private RoomFilter filter;

    public String key() {
        return switch (type) {
            case AUCTION -> type.prefix() + ":" + targetId;
            case USER -> type.prefix() + ":" + targetId;
            case AUCTION_LIST -> type.prefix() + ":" + filterOrNone().keyPart();
        };
    }

    private RoomFilter filterOrNone() {
        return filter == null ? RoomFilter.none() : filter;
    }

}
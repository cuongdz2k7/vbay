package com.vbay.shared.dto.realtimeDTO;

import com.vbay.shared.enums.realtime.RoomType;

public class Room {
    private RoomType type;
    private Long targetId;
    ///hiện tại chưa dùng filter
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

    public Long getTargetId() {
        return targetId;
    }
    public RoomType getType() {
        return type;
    }
    public RoomFilter getFilter() {
        return filter;
    }
    public void setType(RoomType type) {
        this.type = type;
    }
    public void setTargetId(Long targetId) {
        this.targetId = targetId;
    }
    public void setFilter(RoomFilter filter) {
        this.filter = filter;
    }
    

}
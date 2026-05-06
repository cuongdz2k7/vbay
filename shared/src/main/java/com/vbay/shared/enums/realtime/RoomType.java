package com.vbay.shared.enums.realtime;

public enum RoomType {
    AUCTION("auction"),
    USER("user"),
    AUCTION_LIST("auction-list");

    private final String prefix;

    RoomType(String prefix) {
        this.prefix = prefix;
    }

    public String prefix() {
        return prefix;
    }
}

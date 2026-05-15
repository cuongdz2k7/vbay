package com.vbay.shared.enums.realtime;

public enum RoomFilterKey {
    CATEGORY("category"),
    STATUS("status"),
    SELLER("seller");

    private final String key;

    RoomFilterKey(String key) {
        this.key = key;
    }

    public String key() {
        return key;
    }
}

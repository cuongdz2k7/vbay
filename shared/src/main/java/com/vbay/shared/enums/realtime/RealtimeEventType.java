package com.vbay.shared.enums.realtime;

public enum RealtimeEventType {
    AUCTION_CREATED,
    AUCTION_UPDATED,
    AUCTION_ENDED,
    AUCTION_CANCELLED,

    BID_UPDATED,
    OUTBID,
    BUY_NOW_COMPLETED,

    WATCHER_COUNT_CHANGED,

    NOTIFICATION
}
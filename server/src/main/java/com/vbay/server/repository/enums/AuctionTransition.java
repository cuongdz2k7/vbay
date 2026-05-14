package com.vbay.server.repository.enums;

///transition gây ra bởi realtime scheduler
public enum AuctionTransition {
    STARTED,    // Đấu giá vừa bắt đầu
    TIME_EXPIRED,      // Đấu giá vừa kết thúc
    NO_CHANGE,   // Không có thay đổi gì về trạng thái
}

package com.vbay.shared.enums.auction;

/*
Khi user vừa bid hợp lệ: bid mới là WINNING, bid đang WINNING cũ chuyển thành OUTBID.
Khi auction kết thúc: bid WINNING chuyển thành WON, các bid khác chuyển thành LOST.
Nếu bid không hợp lệ thì thường không cần lưu vào DB. Nếu muốn lưu lịch sử lỗi/gian lận thì lưu REJECTED.
PLACED có thể dùng nếu bạn muốn lưu bid trước rồi xử lý sau, nhưng nếu xử lý ngay thì có thể bỏ PLACED.

*/

public enum BidStatus {
    WINNING,
    OUTBID,
    WON,
    CANCELLED
}


package com.vbay.shared.status;

public enum Position {
    USER, // nguoi dung
   //Seller,
    //Bidder,
    //Watcher, bỏ vì seller bidder watcher là vai trò trong từng auction
    ADMIN, // Admin
}
/*
Nếu sau này cần biểu diễn seller/bidder/watcher thì làm ở dữ liệu nghiệp vụ:

auctions.seller_id
bids.bidder_id
auction_watchers.user_id
 */
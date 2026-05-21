package com.vbay.shared.enums;

public enum RequestType {
    // Account
    REGISTER,
    LOGIN,
    LOGOUT,
    FORGOT_PASSWORD,
    VERIFY,
    UPDATE_PROFILE,

    // Admin
    GET_ADMIN_USER_LIST,
    BAN_USER,
    UNBAN_USER,

    // Auction lifecycle
    CLOSE_AUCTION,
    DECLARE_WINNER,
    NOTIFY_WINNER,

    // Watcher
    GET_AUCTION_LIST,
    GET_AUCTION_DETAIL,
    SEARCH_AUCTION,
    FILTER_AUCTION,
    SUBSCRIBE_ROOM,
    UNSUBSCRIBE_ROOM,

    // Bidder
    GET_MY_BID_LIST,
    JOIN_AUCTION,
    PLACE_BID,
    BUY_NOW,
    AUTO_BID,
    CANCEL_BID,
    WATCH_AUCTION,
    QUIT_AUCTION,

    // Payment
    CREATE_PAYMENT,
    DEPOSIT_BALANCE,
    PAY_DEPOSIT,
    PAY_REST,
    VERIFY_PAYMENT,
    RETURN_DEPOSIT,

    // Seller
    UPLOAD_IMAGE,
    CREATE_AUCTION,
    CANCEL_AUCTION,
    

}

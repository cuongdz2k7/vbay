package com.vbay.shared.enums;

public enum RequestType {
    // Account
    REGISTER,
    LOGIN,
    LOGOUT,
    FORGOT_PASSWORD,
    VERIFY,
    UPDATE_PROFILE,

    // Auction lifecycle
    CLOSE_AUCTION,
    DECLARE_WINNER,
    NOTIFY_WINNER,

    // Watcher
    GET_AUCTION_LIST,
    GET_AUCTION_DETAIL,
    SEARCH_AUCTION,
    FILTER_AUCTION,

    // Bidder
    JOIN_AUCTION,
    PLACE_BID,
    AUTO_BID,
    CANCEL_BID,
    WATCH_AUCTION,
    QUIT_AUCTION,

    // Payment
    CREATE_PAYMENT,
    PAY_DEPOSIT,
    PAY_REST,
    VERIFY_PAYMENT,
    RETURN_DEPOSIT,

    // Seller
    CREATE_AUCTION,
    CANCEL_AUCTION
}

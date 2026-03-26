<<<<<<< HEAD:shared/src/main/java/com/vbay/shared/ENUMS_Define/RequestType.java
package com.vbay.shared.ENUMS_Define;

public enum RequestType {
        //Các hành vi có thể xảy ra --> requestType

        //Acoount
        LOGIN,
        LOGOUT,
        FORGOT_PASSWORD,
        VERIFY,
        UPDATE_PROFILE,

        //
        CLOSE_AUCTION,
        DECLARE_WINNER,
        NOTIFY_WINNER,

        // Watcher
        GET_AUCTION_LIST,
        GET_AUCTION_DETAIL,
        SEARCH_AUCTION,
        FILTER_AUCTION,

        //Bidder
        JOIN_AUCTION,
        PLACE_BID,
        AUTO_BID,
        CANCEL_BID,
        WATCH_AUCTION,
        QUIT_AUCTION,


        //Payment
        CREATE_PAYMENT,
        PAY_DEPOSIT,
        PAY_REST,
        VERIFY_PAYMENT,
        RETURN_DEPOSIT,
    

        //Seller
        CREATE_AUCTION,
        SET_INITIAL_PRICE,
        SET_STEP_PRICE,
        SET_AUCTION_TIME,
        CANCEL_AUCTION
    
}

=======
package com.vbay.shared.enums;

public enum RequestType {
    LOGIN,
    REGISTER,
    LOGOUT,
    CREATE_AUCTION,
    LIST_AUCTIONS,
    PLACE_BID
}
>>>>>>> 6351fea0a8c74c87e5f81f17a0db19a239ec37ba:shared/src/main/java/com/vbay/shared/enums/RequestType.java

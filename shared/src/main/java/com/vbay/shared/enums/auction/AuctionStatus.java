package com.vbay.shared.enums.auction;

public enum AuctionStatus {
    SCHEDULED(false),
    ACTIVE(false),
    STOPPED(true),
    ENDED(true),
    FAILED(true),
    CANCELLED(true);

    private final boolean closedForBidding;

    AuctionStatus(boolean closedForBidding) {
        this.closedForBidding = closedForBidding;
    }

    public boolean isClosedForBidding() {
        return closedForBidding;
    }
}



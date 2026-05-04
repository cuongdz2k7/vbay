package com.vbay.shared.enums.shared_status;

public enum AuctionStatus {
    SCHEDULED(false),
    ACTIVE(false),
    ENDED(true),
    CANCELLED(true);

    private final boolean closedForBidding;

    AuctionStatus(boolean closedForBidding) {
        this.closedForBidding = closedForBidding;
    }

    public boolean isClosedForBidding() {
        return closedForBidding;
    }
}



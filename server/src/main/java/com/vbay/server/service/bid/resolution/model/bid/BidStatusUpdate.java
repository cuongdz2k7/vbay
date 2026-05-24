package com.vbay.server.service.bid.resolution.model.bid;

import com.vbay.shared.enums.bid.BidStatus;

public class BidStatusUpdate {
    private final long bidId;
    private final BidStatus status;

    public BidStatusUpdate(long bidId, BidStatus status) {
        this.bidId = bidId;
        this.status = status;
    }

    public long getBidId() { return bidId; }
    public BidStatus getStatus() { return status; }
}
package com.vbay.server.service.bid.resolution.model.autobid;

import java.math.BigDecimal;

public class AutobidMaxBidUpdate {
    private final long autobidId;
    private final BigDecimal maxBidAmount;

    public AutobidMaxBidUpdate(long autobidId, BigDecimal maxBidAmount) {
        this.autobidId = autobidId;
        this.maxBidAmount = maxBidAmount;
    }

    public long getAutobidId() { return autobidId; }
    public BigDecimal getMaxBidAmount() { return maxBidAmount; }
}
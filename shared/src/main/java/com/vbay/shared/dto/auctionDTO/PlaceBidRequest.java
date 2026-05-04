package com.vbay.shared.dto.auctionDTO;

import java.math.BigDecimal;

public class PlaceBidRequest {
    private long auctionId;
    private BigDecimal bidAmount;

    public PlaceBidRequest(long auctionId, BigDecimal bidAmount) {
        this.auctionId = auctionId;
        this.bidAmount = bidAmount;
    }

    public long getAuctionId() {
        return auctionId;
    }

    public BigDecimal getBidAmount() {
        return bidAmount;
    }

}

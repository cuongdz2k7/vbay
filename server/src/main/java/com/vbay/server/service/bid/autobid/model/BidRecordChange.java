package com.vbay.server.service.bid.autobid.model;

import java.math.BigDecimal;

import com.vbay.server.service.bid.autobid.enums.BidType;

public class BidRecordChange {
    private long auctionId;
    private long userId;
    private BigDecimal amount;
    private BidType type;

    public BidRecordChange(long auctionId, long userId, BigDecimal amount, BidType type) {
        this.auctionId = auctionId;
        this.userId = userId;
        this.amount = amount;
        this.type = type;
    }

    public long getAuctionId() {
        return auctionId;
    }
    public long getUserId() {
        return userId;
    }
    public BigDecimal getAmount() {
        return amount;
    }
    public BidType getType() {
        return type;
    }
    
}
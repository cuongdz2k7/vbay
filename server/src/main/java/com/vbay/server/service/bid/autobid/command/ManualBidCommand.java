package com.vbay.server.service.bid.autobid.command;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public class ManualBidCommand {
    private final long auctionId;
    private final long bidderUserId;
    private final BigDecimal amount;
    private final LocalDateTime bidTime;

    public ManualBidCommand(long auctionId, long bidderUserId, BigDecimal amount, LocalDateTime bidTime) {
        this.auctionId = auctionId;
        this.bidderUserId = bidderUserId;
        this.amount = amount;
        this.bidTime = bidTime;
    }

    public long getAuctionId() {
        return auctionId;
    }

    public long getBidderUserId() {
        return bidderUserId;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public LocalDateTime getBidTime() {
        return bidTime;
    }
}

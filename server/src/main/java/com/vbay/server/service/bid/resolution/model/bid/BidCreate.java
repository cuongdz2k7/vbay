package com.vbay.server.service.bid.resolution.model.bid;

import java.math.BigDecimal;

import com.vbay.shared.enums.bid.BidSource;
import com.vbay.shared.enums.bid.BidStatus;

public class BidCreate {
    private Long id;
    private final BidSource source;
    private final long auctionId;
    private final long bidderId;
    private final BigDecimal amount;
    private final BidStatus status;

    public BidCreate(
            BidSource source,
            long auctionId,
            long bidderId,
            BigDecimal amount,
            BidStatus status) {
        this.source = source;
        this.auctionId = auctionId;
        this.bidderId = bidderId;
        this.amount = amount;
        this.status = status;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public BidSource getSource() { return source; }
    public long getAuctionId() { return auctionId; }
    public long getBidderId() { return bidderId; }
    public BigDecimal getAmount() { return amount; }
    public BidStatus getStatus() { return status; }
}
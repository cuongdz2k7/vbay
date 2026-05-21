package com.vbay.server.service.bid.resolution.model;

import java.math.BigDecimal;

import com.vbay.shared.enums.bid.BidSource;
import com.vbay.shared.enums.payment.PaymentStatus;
import com.vbay.shared.enums.payment.PaymentType;

public class PaymentCreate {
    private Long id;
    private final BidSource bidSource;
    private final long auctionId;
    private final long buyerId;
    private final long sellerId;
    private final BigDecimal amount;
    private final PaymentType type;
    private final PaymentStatus status;

    public PaymentCreate(
            BidSource bidSource,
            long auctionId,
            long buyerId,
            long sellerId,
            BigDecimal amount,
            PaymentType type,
            PaymentStatus status) {
        this.bidSource = bidSource;
        this.auctionId = auctionId;
        this.buyerId = buyerId;
        this.sellerId = sellerId;
        this.amount = amount;
        this.type = type;
        this.status = status;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public BidSource getBidSource() { return bidSource; }
    public long getAuctionId() { return auctionId; }
    public long getBuyerId() { return buyerId; }
    public long getSellerId() { return sellerId; }
    public BigDecimal getAmount() { return amount; }
    public PaymentType getType() { return type; }
    public PaymentStatus getStatus() { return status; }
}
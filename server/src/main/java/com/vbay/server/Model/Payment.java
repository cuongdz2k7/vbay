package com.vbay.server.model;

import java.math.BigDecimal;

public class Payment {
    private long id;
    private long auctionId;
    private long buyerId;
    private long sellerId;
    private BigDecimal amount;
    private String paymentMethod;
    private String paymentTime;
    private boolean paid;

    public Payment(
        long auctionId,
        long buyerId,
        long sellerId,
        BigDecimal amount,
        String paymentMethod,
        String paymentTime
    ) {
        this.auctionId = auctionId;
        this.buyerId = buyerId;
        this.sellerId = sellerId;
        this.amount = amount;
        this.paymentMethod = paymentMethod;
        this.paymentTime = paymentTime;
        this.paid = false;
    }

    public long getId() {
        return id;
    }

    public long getAuctionId() {
        return auctionId;
    }

    public long getBuyerId() {
        return buyerId;
    }

    public long getSellerId() {
        return sellerId;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public String getPaymentMethod() {
        return paymentMethod;
    }

    public String getPaymentTime() {
        return paymentTime;
    }

    public boolean isPaid() {
        return paid;
    }

    public void setId(long id) {
        this.id = id;
    }

    public void setAuctionId(long auctionId) {
        this.auctionId = auctionId;
    }

    public void setBuyerId(long buyerId) {
        this.buyerId = buyerId;
    }

    public void setSellerId(long sellerId) {
        this.sellerId = sellerId;
    }

    public void setAmount(BigDecimal amount) {
        this.amount = amount;
    }

    public void setPaymentMethod(String paymentMethod) {
        this.paymentMethod = paymentMethod;
    }

    public void setPaymentTime(String paymentTime) {
        this.paymentTime = paymentTime;
    }

    public void setPaid(boolean paid) {
        this.paid = paid;
    }
}

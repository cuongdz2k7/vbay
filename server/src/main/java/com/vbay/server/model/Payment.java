package com.vbay.server.model;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import com.vbay.shared.enums.payment.PaymentStatus;
import com.vbay.shared.enums.payment.PaymentType;

public class Payment {
    private long id;
    private long auctionId;
    private long buyerId;
    private long sellerId;
    private long winningBidId;  
    private BigDecimal amount;
    private PaymentType type;
    private PaymentStatus status;
    private LocalDateTime heldAt;
    private LocalDateTime releasedAt;
    private LocalDateTime refundedAt;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    //Constructor (Create new payment)
    public Payment(long auctionId, long buyerId, long sellerId, long winningBidId, BigDecimal amount, PaymentType type, PaymentStatus status) {
        //Base:
        this.auctionId = auctionId;
        this.buyerId = buyerId;
        this.sellerId = sellerId;
        this.winningBidId = winningBidId;
        //Payment Detail:
        this.amount = amount;
        this.type = type;
        this.status = status;

    }
    //Getter
    //a, Base:
    public long getId(){
        return this.id;
    }
    public long getAuctionId(){
        return this.auctionId;
    }
    public long getBuyerId(){
        return this.buyerId;
    }
    public long getSellerId(){
        return this.sellerId;
    }
    public long getWinningBidId(){
        return this.winningBidId;
    }
    //b, Payment Detail:
    public BigDecimal getAmount(){
        return this.amount;
    }
    public PaymentType getType(){
        return this.type;
    } 
    public PaymentStatus getStatus(){
        return this.status;
    }

    //Setter (Change data)
    //a, Base:
    public void setId(long new_id){
        this.id = new_id;
    }
    public void setAuctionId(long new_auction_id){
        this.auctionId = new_auction_id;
    }
    public void setBuyerId(long new_buyer_id){
        this.buyerId = new_buyer_id;
    }
    public void setSellerId(long new_seller_id){
        this.sellerId = new_seller_id;
    }
    //b, Payment Detail:
    public void setAmount(BigDecimal new_amount){
        this.amount = new_amount;
    }
    public void setStatus(PaymentStatus new_status){
        this.status = new_status;
    }
}

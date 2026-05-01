package com.vbay.server.model;

import java.math.BigDecimal;
public class Payment {
    // Base
    private long id;
    private long auction_id;
    private long buyer_id;
    private long seller_id;
    // Payment Detail
    private BigDecimal amount;
    private String payment_method;
    private String payment_time;
    // State
    private boolean is_paid;

    //Constructor (Create new payment)
    public Payment(long auction_id, long buyer_id, long seller_id, BigDecimal amount, String payment_method, String payment_time){
        //Base:
        this.auction_id = auction_id;
        this.buyer_id = buyer_id;
        this.seller_id = seller_id;
        //Payment Detail:
        this.amount = amount;
        this.payment_method = payment_method;
        this.payment_time = payment_time;
        //State:
        this.is_paid = false;
    }

    //Getter
    //a, Base:
    public long getId(){
        return this.id;
    }
    public long getAuctionId(){
        return this.auction_id;
    }
    public long getBuyerId(){
        return this.buyer_id;
    }
    public long getSellerId(){
        return this.seller_id;
    }
    //b, Payment Detail:
    public BigDecimal getAmount(){
        return this.amount;
    }
    public String getPaymentMethod(){
        return this.payment_method;
    }
    public String getPaymentTime(){
        return this.payment_time;
    }
    //c, State:
    public boolean isPaid(){
        return this.is_paid;
    }

    //Setter (Change data)
    //a, Base:
    public void setId(long new_id){
        this.id = new_id;
    }
    public void setAuctionId(long new_auction_id){
        this.auction_id = new_auction_id;
    }
    public void setBuyerId(long new_buyer_id){
        this.buyer_id = new_buyer_id;
    }
    public void setSellerId(long new_seller_id){
        this.seller_id = new_seller_id;
    }
    //b, Payment Detail:
    public void setAmount(BigDecimal new_amount){
        this.amount = new_amount;
    }
    public void setPaymentMethod(String new_payment_method){
        this.payment_method = new_payment_method;
    }
    public void setPaymentTime(String new_payment_time){
        this.payment_time = new_payment_time;
    }
    //c, State:
    public void setPaid(boolean new_is_paid){
        this.is_paid = new_is_paid;
    }
}

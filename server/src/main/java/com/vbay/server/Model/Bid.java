package com.vbay.server.model;

import java.math.BigDecimal;

import java.math.BigDecimal;

public class Bid {
    // Base
    private long id;
    private long auction_id;
    private long bidder_id;
    // Bid Detail
    private BigDecimal bid_amount;
    private String bid_time;
    private boolean is_winner;

    //Constructor (Create new bid)
    public Bid(long auction_id, long bidder_id, BigDecimal bid_amount, String bid_time){
        //Base:
        this.auction_id = auction_id;
        this.bidder_id = bidder_id;
        //Bid Detail:
        this.bid_amount = bid_amount;
        this.bid_time = bid_time;
        this.is_winner = false;
    }

    //Getter
    //a, Base:
    public long getId(){
        return this.id;
    }
    public long getAuctionId(){
        return this.auction_id;
    }
    public long getBidderId(){
        return this.bidder_id;
    }
    //b, Bid Detail:
    public BigDecimal getBidAmount(){
        return this.bid_amount;
    }
    public String getBidTime(){
        return this.bid_time;
    }
    public boolean isWinner(){
        return this.is_winner;
    }

    //Setter (Change data)
    //a, Base:
    public void setId(long new_id){
        this.id = new_id;
    }
    public void setAuctionId(long new_auction_id){
        this.auction_id = new_auction_id;
    }
    public void setBidderId(long new_bidder_id){
        this.bidder_id = new_bidder_id;
    }
    //b, Bid Detail:
    public void setBidAmount(BigDecimal new_bid_amount){
        this.bid_amount = new_bid_amount;
    }
    public void setBidTime(String new_bid_time){
        this.bid_time = new_bid_time;
    }
    public void setWinner(boolean new_is_winner){
        this.is_winner = new_is_winner;
    }
}

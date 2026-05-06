package com.vbay.server.model;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import com.vbay.shared.enums.auction.BidStatus;
import com.vbay.shared.enums.bid.BidSource;

public class Bid {
    // Base
    private long id;
    private long auctionId;
    private long bidderId;
    // Bid Detail
    private BigDecimal bidAmount;
    private LocalDateTime bidTime;
    private BidSource bidSource;
    private BidStatus status;


    //Constructor (Create new manual bid)
    public Bid(long auctionId, long bidderId, BigDecimal bidAmount){
        //Base:
        this.auctionId = auctionId;
        this.bidderId = bidderId;
        //Bid Detail:
        this.bidAmount = bidAmount;
        this.bidSource = BidSource.USER_BID;
    }

    public Bid(long auctionId, long bidderId, BigDecimal bidAmount, LocalDateTime bidTime, BidStatus status, BidSource bidSource){
        this.auctionId = auctionId;
        this.bidderId = bidderId;
        this.bidAmount = bidAmount;
        this.bidTime = bidTime;
        this.status = status;
        this.bidSource = bidSource;

    }
    //Getter
    //a, Base:
    public long getId(){
        return this.id;
    }
    public long getAuctionId(){
        return this.auctionId;
    }
    public long getBidderId(){
        return this.bidderId;
    }
    //b, Bid Detail:
    public BigDecimal getBidAmount(){
        return this.bidAmount;
    }
    public LocalDateTime getBidTime(){
        return this.bidTime;
    }
    public BidStatus getStatus(){
        return this.status;
    }
    public BidSource getBidSource(){
        return this.bidSource;
    }

    //Setter (Change data)
    //a, Base:
    public void setId(long new_id){
        this.id = new_id;
    }
    public void setAuctionId(long new_auction_id){
        this.auctionId = new_auction_id;
    }
    public void setBidderId(long new_bidder_id){
        this.bidderId = new_bidder_id;
    }
    //b, Bid Detail:
    public void setBidAmount(BigDecimal new_bid_amount){
        this.bidAmount = new_bid_amount;
    }
    public void setBidTime(LocalDateTime new_bid_time){
        this.bidTime = new_bid_time;
    }
    public void setStatus(BidStatus new_status){
        this.status = new_status;
    }
    public void setBidSource(BidSource new_bid_source){
        this.bidSource = new_bid_source;
    }
}

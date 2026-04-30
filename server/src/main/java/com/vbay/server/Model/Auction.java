package com.vbay.server.model;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import com.vbay.shared.enums.shared_status.AuctionStatus;
public class Auction {
    // Base
    private long id;
    private long productId;
    private long sellerId;
    // Auction Detail
    private String title;
    private String description;
    private BigDecimal  startingPrice;
    private BigDecimal currentPrice;
    private BigDecimal reservePrice; ///có thể null nếu không có reserve price (nên để nullable)
    private BigDecimal buyNowPrice; ///có thể null nếu không có buy now price
    private BigDecimal  minimumBidStep;
    private LocalDateTime startingTime;
    private LocalDateTime endingTime;
    private AuctionStatus status;

    //Constructor (Create new auction) để lưu vào database
    public Auction(long productId, long sellerId, BigDecimal startingPrice, BigDecimal minimumBidStep, LocalDateTime startingTime, LocalDateTime endingTime) {
        //Base:
        this.productId = productId;
        this.sellerId = sellerId;
        //Auction Detail:
        this.startingPrice = startingPrice;
        this.currentPrice = startingPrice;
        this.minimumBidStep = minimumBidStep;
        //Time + State:
        this.startingTime = startingTime;
        this.endingTime = endingTime;
        this.status = AuctionStatus.SCHEDULED;
    }

    public Auction(long sellerId, long productId, String title, String description, BigDecimal buyNowPrice, BigDecimal reservePrice, BigDecimal minimumBidStep, BigDecimal startingPrice, LocalDateTime startingTime, LocalDateTime endingTime) {
        this.productId = productId;
        this.sellerId = sellerId;
        this.title = title;
        this.description = description;
        this.startingPrice = startingPrice;
        this.currentPrice = this.startingPrice;
        this.reservePrice = reservePrice;
        this.buyNowPrice = buyNowPrice;
        this.minimumBidStep = minimumBidStep;
        this.startingTime = startingTime;
        this.endingTime = endingTime;
        this.status = AuctionStatus.SCHEDULED;
    }
    ///Constructor đầy đủ để tạo auction từ database, sẽ có tất cả các trường
    public Auction(long id, long productId, long sellerId, String title, String description, BigDecimal startingPrice, BigDecimal currentPrice, BigDecimal reservePrice, BigDecimal buyNowPrice, BigDecimal minimumBidStep, LocalDateTime startingTime, LocalDateTime endingTime, AuctionStatus state) {
        this.id = id;
        this.productId = productId;
        this.sellerId = sellerId;
        this.title = title;
        this.description = description;
        this.startingPrice = startingPrice;
        this.currentPrice = currentPrice;
        this.reservePrice = reservePrice;
        this.buyNowPrice = buyNowPrice;
        this.minimumBidStep = minimumBidStep;
        this.startingTime = startingTime;
        this.endingTime = endingTime;
        this.status = state;
    }

    //Getter
    //a, Base:
    public long getId(){
        return this.id;
    }
    public long getProductId(){
        return this.productId;
    }
    public long getSellerId(){
        return this.sellerId;
    }
    //b, Auction Detail:
    public String getTitle(){
        return this.title;
    }
    public String getDescription(){
        return this.description;
    }
    public BigDecimal getStartPrice(){
        return this.startingPrice;
    }
    public BigDecimal getCurrentPrice(){
        return this.currentPrice;
    }
    public BigDecimal getReservePrice(){
        return this.reservePrice;
    }
    public BigDecimal getBuyNowPrice(){
        return this.buyNowPrice;
    }
    public BigDecimal getMinimumBidStep(){
        return this.minimumBidStep;
    }
    //c, Time + State:
    public LocalDateTime getStartingTime(){
        return this.startingTime;
    }
    public LocalDateTime getEndingTime(){
        return this.endingTime;
    }
    public AuctionStatus getStatus(){
        return this.status;
    }

    //Setter (Change data)
    //a, Base:
    public void setId(long new_id){
        this.id = new_id;
    }
    public void setProductId(long new_product_id){
        this.productId = new_product_id;
    }
    public void setSellerId(long new_seller_id){
        this.sellerId = new_seller_id;
    }
    //b, Auction Detail:
    public void setTitle(String new_title){
        this.title = new_title;
    }
    public void setDescription(String new_description){
        this.description = new_description;
    }
    public void setStartPrice(BigDecimal new_starting_price){
        this.startingPrice = new_starting_price;
    }
    public void setCurrentPrice(BigDecimal new_current_price){
        this.currentPrice = new_current_price;
    }
    public void setReservePrice(BigDecimal new_reserve_price){
        this.reservePrice = new_reserve_price;
    }
    public void setBuyNowPrice(BigDecimal new_buy_now_price){
        this.buyNowPrice = new_buy_now_price;
    }
    public void setMinimumBidStep(BigDecimal new_minimum_bid_step){
        this.minimumBidStep = new_minimum_bid_step;
    }
    //c, Time + State:
    public void setStartingTime(LocalDateTime new_starting_time){
        this.startingTime = new_starting_time;
    }
    public void setEndingTime(LocalDateTime new_ending_time){
        this.endingTime = new_ending_time;
    }
    public void setStatus(AuctionStatus new_status){
        this.status = new_status;
    }

}

package com.vbay.server.model;

public class Auction {
    // Base
    private long id;
    private long product_id;
    private long seller_id;
    // Auction Detail
    private double start_price;
    private double current_price;
    private double minimum_bid_step;
    // Time + State
    private String start_time;
    private String end_time;
    private boolean is_closed;

    //Constructor (Create new auction)
    public Auction(long product_id, long seller_id, double start_price, double minimum_bid_step, String start_time, String end_time){
        //Base:
        this.product_id = product_id;
        this.seller_id = seller_id;
        //Auction Detail:
        this.start_price = start_price;
        this.current_price = start_price;
        this.minimum_bid_step = minimum_bid_step;
        //Time + State:
        this.start_time = start_time;
        this.end_time = end_time;
        this.is_closed = false;
    }

    //Getter
    //a, Base:
    public long getId(){
        return this.id;
    }
    public long getProductId(){
        return this.product_id;
    }
    public long getSellerId(){
        return this.seller_id;
    }
    //b, Auction Detail:
    public double getStartPrice(){
        return this.start_price;
    }
    public double getCurrentPrice(){
        return this.current_price;
    }
    public double getMinimumBidStep(){
        return this.minimum_bid_step;
    }
    //c, Time + State:
    public String getStartTime(){
        return this.start_time;
    }
    public String getEndTime(){
        return this.end_time;
    }
    public boolean isClosed(){
        return this.is_closed;
    }

    //Setter (Change data)
    //a, Base:
    public void setId(long new_id){
        this.id = new_id;
    }
    public void setProductId(long new_product_id){
        this.product_id = new_product_id;
    }
    public void setSellerId(long new_seller_id){
        this.seller_id = new_seller_id;
    }
    //b, Auction Detail:
    public void setStartPrice(double new_start_price){
        this.start_price = new_start_price;
    }
    public void setCurrentPrice(double new_current_price){
        this.current_price = new_current_price;
    }
    public void setMinimumBidStep(double new_minimum_bid_step){
        this.minimum_bid_step = new_minimum_bid_step;
    }
    //c, Time + State:
    public void setStartTime(String new_start_time){
        this.start_time = new_start_time;
    }
    public void setEndTime(String new_end_time){
        this.end_time = new_end_time;
    }
    public void setClosed(boolean new_is_closed){
        this.is_closed = new_is_closed;
    }
}

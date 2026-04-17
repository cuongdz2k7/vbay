package com.vbay.server.model;

import com.vbay.shared.status.ProductType;

public class Product {
    // Base
    private long id;
    private String name;
    private ProductType type;
    private String description;
    private String category;
    // Owner + Auction
    private long seller_id;
    private double starting_price;
    private double buy_now_price;
    private int stock_quantity;

    //Constructor (Create new product)
    public Product(String name, ProductType type, String description, String category, long seller_id, double starting_price, double buy_now_price, int stock_quantity){
        //Base:
        this.name = name;
        this.type = type;
        this.description = description;
        this.category = category;
        //Owner + Auction:
        this.seller_id = seller_id;
        this.starting_price = starting_price;
        this.buy_now_price = buy_now_price;
        this.stock_quantity = stock_quantity;
    }

    //Getter
    //a, Base:
    public long getId(){
        return id;
    }
    public ProductType getProductType(){
        return this.type;
    }
    public String getProductName(){
        return this.name;
    }
    public String getDescription(){
        return this.description;
    }
    public String getCategory(){
        return this.category;
    }
    //b, Owner + Auction:
    public long getSellerId(){
        return this.seller_id;
    }
    public double getStartingPrice(){
        return this.starting_price;
    }
    public double getBuyNowPrice(){
        return this.buy_now_price;
    }
    public int getStockQuantity(){
        return this.stock_quantity;
    }

    //Setter (Change data)
    //a, Base:
    public void setId(long new_id){
        this.id = new_id;
    }
    public void setProductName(String new_product_name){
        this.name = new_product_name;
    }
    public void setProductType(ProductType new_type){
        this.type = new_type;
    }
    public void setDescription(String new_description){
        this.description = new_description;
    }
    public void setCategory(String new_category){
        this.category = new_category;
    }
    //b, Owner + Auction:
    public void setSellerId(long new_seller_id){
        this.seller_id = new_seller_id;
    }
    public void setStartingPrice(double new_starting_price){
        this.starting_price = new_starting_price;
    }
    public void setBuyNowPrice(double new_buy_now_price){
        this.buy_now_price = new_buy_now_price;
    }
    public void setStockQuantity(int new_stock_quantity){
        this.stock_quantity = new_stock_quantity;
    }
}

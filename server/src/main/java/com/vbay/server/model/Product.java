package com.vbay.server.model;

import java.time.LocalDateTime;
import java.util.List;
import com.vbay.shared.enums.shared_status.ProductStatus;

public class Product {
    private long id;
    private long sellerId;
    private String name;
    private List<ProductImage> images;
    private String description;
    private long categoryId;
    private String condition;
    private ProductStatus status;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;


    ///status sẽ là AVAILABLE khi mới tạo product, sau đó sẽ được update dựa trên trạng thái của auction (ACTIVE, ENDED, CANCELED)
    //constructor để tạo product cho auction, sẽ không có id, sellerId, images, createdAt, updatedAt vì những trường này sẽ được set sau khi tạo product
    public Product(String name, String description, long categoryId, String condition) {
        this.name = name;
        this.images = List.of();
        this.description = description;
        this.categoryId = categoryId;
        this.condition = condition;
        this.status = ProductStatus.AVAILABLE;
    }
    ///constructor đầy đủ để tạo product từ database, sẽ có tất cả các trường
    public Product(long id, long sellerId, String name, List<ProductImage> images, String description, long categoryId, String condition, ProductStatus status, LocalDateTime createdAt, LocalDateTime updatedAt) {
        this.id = id;
        this.sellerId = sellerId;
        this.name = name;
        this.images = images;
        this.description = description;
        this.categoryId = categoryId;
        this.condition = condition;
        this.status = status;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }
    public long getId() {
        return id;
    }
    public long getSellerId() {
        return sellerId;
    }
    public String getName() {
        return name;
    }
    public List<ProductImage> getImages() {
        return images;
    }
    public String getDescription() {
        return description;
    }
    public String getCondition() {
        return condition;
    }
    public long getCategoryId() {
        return categoryId;
    }
    public LocalDateTime getCreatedAt() {
        return createdAt;
    }
    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }
    public ProductStatus getStatus() {
        return status;
    }
    public void setId(long id) {
        this.id = id;
    }
    public void setSellerId(long sellerId) {
        this.sellerId = sellerId;
    }
    public void setName(String name) {
        this.name = name;
    }
    public void setImages(List<ProductImage> images) {
        this.images = images;
    }
    public void setDescription(String description) {
        this.description = description;
    }
    public void setCondition(String condition) {
        this.condition = condition;
    }
    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }
    public void setStatus(ProductStatus status) {
        this.status = status;
    }
    public void setCategoryId(long categoryId) {
        this.categoryId = categoryId;
    }
    
    
}

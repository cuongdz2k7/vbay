package com.vbay.server.Model;

import java.time.LocalDateTime;
import java.util.List;

import com.vbay.shared.enums.shared_status.ProductStatus;


public class Product {
    private long id;
    private long sellerId;
    private String title;
    private List<ProductImage> images;
    private String description;
    private long categoryId;
    private String condition;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private ProductStatus status;


    //constructor để tạo product cho auction, sẽ không có id, sellerId, createdAt, updatedAt vì những trường này sẽ được set sau khi tạo product
    ///status sẽ là AVAILABLE khi mới tạo product, sau đó sẽ được update dựa trên trạng thái của auction (ACTIVE, ENDED, CANCELED)
    public Product(long sellerId, String title, List<ProductImage> images, String description, String condition) {
        this.id = id;
        this.sellerId = sellerId;
        this.title = title;
        this.images = images;
        this.description = description;
        this.condition = condition;
        this.status = ProductStatus.AVAILABLE;
    }
    ///constructor đầy đủ để tạo product từ database, sẽ có tất cả các trường
    public Product(long id, long sellerId, String title, List<ProductImage> images, String description, String condition, ProductStatus status, LocalDateTime createdAt, LocalDateTime updatedAt) {
        this.id = id;
        this.sellerId = sellerId;
        this.title = title;
        this.images = images;
        this.description = description;
        this.condition = condition;
        this.status = status;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }
}

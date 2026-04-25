package com.vbay.server.Model;

public class ProductImage {
    private long id;
    private long productId;
    private String imageUrl;
    private boolean isThumbnail;

    /// Constructor để tạo ProductImage từ DTO
    public ProductImage(String imageUrl, boolean isThumbnail) {
        this.imageUrl = imageUrl;
        this.isThumbnail = isThumbnail;
    }

    public ProductImage(long id, long productId, String imageUrl, boolean isThumbnail) {
        this.id = id;
        this.productId = productId;
        this.imageUrl = imageUrl;
        this.isThumbnail = isThumbnail;
    }

    public long getId() {
        return id;
    }

    public long getProductId() {
        return productId;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public boolean isThumbnail() {
        return isThumbnail;
    }

}

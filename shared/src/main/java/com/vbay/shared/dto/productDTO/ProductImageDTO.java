package com.vbay.shared.dto.productDTO;

public class ProductImageDTO {
    private String imageUrl;
    private boolean isThumbnail;

    public ProductImageDTO(String imageUrl, boolean isThumbnail) {
        this.imageUrl = imageUrl;
        this.isThumbnail = isThumbnail;
    }
    public String getImageUrl() {
        return imageUrl;
    }
    public boolean isThumbnail() {
        return isThumbnail;
    }

}

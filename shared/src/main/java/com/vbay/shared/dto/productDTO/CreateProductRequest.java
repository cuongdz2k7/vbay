package com.vbay.shared.dto.productDTO;

import java.util.List;

public class CreateProductRequest {
    private String name;
    private String description;
    private String condition;
    private long categoryId;
    private List<ProductImageDTO> images;

    public CreateProductRequest (String name, String description, String condition, long categoryId, List<ProductImageDTO> images) {
        this.name = name;
        this.description = description;
        this.condition = condition;
        this.categoryId = categoryId;
        this.images = images;
    }
    public String getName() {
        return name;
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
    public List<ProductImageDTO> getImages() {
        return images;
    }


}

package com.vbay.shared.dto.productDTO;

import java.util.List;

public class CreateProductRequest {
    private String name;
    private List<ProductImageDTO> images;
    private String description;
    private String condition; ///enum (USED, NEW), 
    private long categoryId; ///để enum sau này, tạm thời để long

    public CreateProductRequest (String name, List<ProductImageDTO> images, String description, String condition, long categoryId) {
        this.name = name;
        this.images = images;
        this.description = description;
        this.condition = condition;
        this.categoryId = categoryId;
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

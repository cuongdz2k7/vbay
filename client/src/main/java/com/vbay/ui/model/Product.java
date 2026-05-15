package com.vbay.ui.model;

import java.util.List;

public class Product {
    private final long id;
    private final String title;
    private final String description;
    private final long categoryId;
    private final String imagePath;
    private final List<String> imageUrls;

    public Product(
        String title,
        String description,
        String imagePath
    ) {
        this(0L, title, description, 0L, imagePath, imagePath == null ? List.of() : List.of(imagePath));
    }

    public Product(
        long id,
        String title,
        String description,
        long categoryId,
        String imagePath,
        List<String> imageUrls
    ) {
        this.id = id;
        this.title = title;
        this.description = description;
        this.categoryId = categoryId;
        this.imagePath = imagePath;
        this.imageUrls = imageUrls == null ? List.of() : List.copyOf(imageUrls);
    }

    public long getId() {
        return id;
    }

    public String getTitle() {
        return title;
    }

    public String getDescription() {
        return description;
    }

    public long getCategoryId() {
        return categoryId;
    }

    public String getImagePath() {
        return imagePath;
    }

    public List<String> getImageUrls() {
        return imageUrls;
    }
}

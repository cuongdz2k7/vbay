package com.vbay.ui.model;

public class Product {
    private final long id;
    private final String title;
    private final String description;
    private final long categoryId;
    private final String price;
    private final String startingPrice;
    private final String bidStep;
    private final String timeLeft;
    private final double progress;
    private final String imagePath;

    public Product(
        String title,
        String description,
        String price,
        String startingPrice,
        String bidStep,
        String timeLeft,
        double progress,
        String imagePath
    ) {
        this(0L, title, description, 0L, price, startingPrice, bidStep, timeLeft, progress, imagePath);
    }

    public Product(
        long id,
        String title,
        String description,
        long categoryId,
        String price,
        String startingPrice,
        String bidStep,
        String timeLeft,
        double progress,
        String imagePath
    ) {
        this.id = id;
        this.title = title;
        this.description = description;
        this.categoryId = categoryId;
        this.price = price;
        this.startingPrice = startingPrice;
        this.bidStep = bidStep;
        this.timeLeft = timeLeft;
        this.progress = progress;
        this.imagePath = imagePath;
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

    public String getPrice() {
        return price;
    }

    public String getStartingPrice() {
        return startingPrice;
    }

    public String getBidStep() {
        return bidStep;
    }

    public String getTimeLeft() {
        return timeLeft;
    }

    public double getProgress() {
        return progress;
    }

    public String getImagePath() {
        return imagePath;
    }
}

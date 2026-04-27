package com.vbay.ui.model;

public class Product {
    private final String title;
    private final String description;
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
        this.title = title;
        this.description = description;
        this.price = price;
        this.startingPrice = startingPrice;
        this.bidStep = bidStep;
        this.timeLeft = timeLeft;
        this.progress = progress;
        this.imagePath = imagePath;
    }

    public String getTitle() {
        return title;
    }

    public String getDescription() {
        return description;
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

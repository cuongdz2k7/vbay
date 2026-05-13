package com.vbay.ui.util;

import java.net.URL;

import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.geometry.Rectangle2D;

public final class ProductImageLoader {
    private static final String DEFAULT_PRODUCT_IMAGE = "/jfx/image/logo_Hust.png";

    private ProductImageLoader() {
    }

    public static Image load(String imagePath) {
        if (isHttpUrl(imagePath)) {
            return new Image(imagePath, true);
        }

        URL imageUrl = resolve(imagePath);
        return new Image(imageUrl.toExternalForm(), true);
    }

    public static void loadCover(ImageView imageView, String imagePath) {
        Image image = load(imagePath);
        imageView.setImage(image);
        imageView.setPreserveRatio(false);
        applyCoverViewport(imageView, image);

        image.widthProperty().addListener((observable, oldValue, newValue) -> applyCoverViewport(imageView, image));
        image.heightProperty().addListener((observable, oldValue, newValue) -> applyCoverViewport(imageView, image));
        imageView.fitWidthProperty().addListener((observable, oldValue, newValue) -> applyCoverViewport(imageView, image));
        imageView.fitHeightProperty().addListener((observable, oldValue, newValue) -> applyCoverViewport(imageView, image));
    }

    private static void applyCoverViewport(ImageView imageView, Image image) {
        double imageWidth = image.getWidth();
        double imageHeight = image.getHeight();
        double viewWidth = imageView.getFitWidth();
        double viewHeight = imageView.getFitHeight();

        if (imageWidth <= 0 || imageHeight <= 0 || viewWidth <= 0 || viewHeight <= 0) {
            return;
        }

        double imageRatio = imageWidth / imageHeight;
        double viewRatio = viewWidth / viewHeight;
        double viewportWidth = imageWidth;
        double viewportHeight = imageHeight;
        double viewportX = 0;
        double viewportY = 0;

        if (imageRatio > viewRatio) {
            viewportWidth = imageHeight * viewRatio;
            viewportX = (imageWidth - viewportWidth) / 2;
        } else if (imageRatio < viewRatio) {
            viewportHeight = imageWidth / viewRatio;
            viewportY = (imageHeight - viewportHeight) / 2;
        }

        imageView.setViewport(new Rectangle2D(viewportX, viewportY, viewportWidth, viewportHeight));
    }

    private static boolean isHttpUrl(String imagePath) {
        return imagePath != null
            && (imagePath.startsWith("http://") || imagePath.startsWith("https://"));
    }

    private static URL resolve(String imagePath) {
        String resolvedPath = imagePath == null || imagePath.isBlank() ? DEFAULT_PRODUCT_IMAGE : imagePath;
        URL imageUrl = ProductImageLoader.class.getResource(resolvedPath);
        if (imageUrl != null) {
            return imageUrl;
        }

        URL fallbackUrl = ProductImageLoader.class.getResource(DEFAULT_PRODUCT_IMAGE);
        if (fallbackUrl == null) {
            throw new IllegalStateException("Missing default product image: " + DEFAULT_PRODUCT_IMAGE);
        }

        return fallbackUrl;
    }
}

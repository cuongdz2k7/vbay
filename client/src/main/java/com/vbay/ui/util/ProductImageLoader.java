package com.vbay.ui.util;

import java.net.URL;

import javafx.scene.image.Image;

public final class ProductImageLoader {
    private static final String DEFAULT_PRODUCT_IMAGE = "/jfx/image/logo_Hust.png";

    private ProductImageLoader() {
    }

    public static Image load(String imagePath) {
        URL imageUrl = resolve(imagePath);
        return new Image(imageUrl.toExternalForm(), true);
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

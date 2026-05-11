package com.vbay.ui.util;

import java.io.ByteArrayInputStream;
import java.net.URL;
import java.util.Base64;

import javafx.scene.image.Image;

public final class ProductImageLoader {
    private static final String DEFAULT_PRODUCT_IMAGE = "/jfx/image/logo_Hust.png";

    private ProductImageLoader() {
    }

    public static Image load(String imagePath) {
        if (isBase64Image(imagePath)) {
            return loadBase64(imagePath);
        }

        URL imageUrl = resolve(imagePath);
        return new Image(imageUrl.toExternalForm(), true);
    }

    private static boolean isBase64Image(String imagePath) {
        if (imagePath == null || imagePath.isBlank()) {
            return false;
        }
        return imagePath.startsWith("data:image/") || imagePath.length() > 200;
    }

    private static Image loadBase64(String imageBase64) {
        String normalized = imageBase64;
        int commaIndex = normalized.indexOf(',');
        if (normalized.startsWith("data:image/") && commaIndex >= 0) {
            normalized = normalized.substring(commaIndex + 1);
        }

        byte[] bytes = Base64.getDecoder().decode(normalized);
        return new Image(new ByteArrayInputStream(bytes));
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

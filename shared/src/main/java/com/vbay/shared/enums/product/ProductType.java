package com.vbay.shared.enums.product;

public enum ProductType {
    ELECTRONICS(1L, "Electronics"),
    COLLECTIBLES(2L, "Collectibles"),
    ARTS(3L, "Arts"),
    JEWELRY_WATCHES(4L, "Jewelry & Watches"),
    SPORTING_GOODS(5L, "Sporting Goods");

    private final long id;
    private final String displayName;

    ProductType(long id, String displayName) {
        this.id = id;
        this.displayName = displayName;
    }

    public long getId() {
        return id;
    }

    public String getDisplayName() {
        return displayName;
    }

    public static ProductType fromDisplayName(String displayName) {
        for (ProductType productType : values()) {
            if (productType.displayName.equals(displayName)) {
                return productType;
            }
        }
        throw new IllegalArgumentException("Unsupported product category: " + displayName);
    }
}

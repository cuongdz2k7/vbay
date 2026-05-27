package com.vbay.server.repository;

import java.sql.SQLException;
import java.util.List;
import java.util.Optional;

import com.vbay.server.model.ProductImage;

public interface ProductImageRepository {
    Optional<ProductImage> save(ProductImage productImage) throws SQLException;

    void saveAll(long productId, List<ProductImage> images) throws SQLException;

    List<ProductImage> findByProductId(long productId) throws SQLException;

    Optional<String> findThumbnailUrlByProductId(long productId) throws SQLException;

    void deleteByProductId(long productId) throws SQLException;
}

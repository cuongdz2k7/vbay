package com.vbay.server.repository;

import java.sql.SQLException;
import java.util.List;
import java.util.Optional;

import com.vbay.server.model.Product;
import com.vbay.shared.enums.product.ProductStatus;

public interface ProductRepository {
    Product save(Product product) throws SQLException;

    Optional<Product> findById(long productId) throws SQLException;

    List<Product> findBySellerId(long sellerId) throws SQLException;

    void updateStatus(long productId, ProductStatus status) throws SQLException;

    boolean existsById(long productId) throws SQLException;
}

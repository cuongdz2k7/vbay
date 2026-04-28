package com.vbay.server.repository.JDBCrepository;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import com.vbay.server.Model.ProductImage;
import com.vbay.server.mapper.rowmapper.ProductImageRowMapper;
import com.vbay.server.repository.ProductImageRepository;


public class JdbcProductImageRepository implements ProductImageRepository {
    private final Connection connection;

    public JdbcProductImageRepository(Connection connection) {
        this.connection = connection;
    }

    @Override
    public Optional<ProductImage> save(ProductImage productImage) throws SQLException {
        String sql = "INSERT INTO product_images (product_id, image_url, is_thumbnail) VALUES (?, ?, ?)";
        try (PreparedStatement stmt = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            stmt.setLong(1, productImage.getProductId());
            stmt.setString(2, productImage.getImageUrl());
            stmt.setBoolean(3, productImage.isThumbnail());
            stmt.executeUpdate();
            ResultSet rs = stmt.getGeneratedKeys();
            if (rs.next()) {
                productImage.setId(rs.getLong(1));
                return Optional.of(productImage);
            } else {
                throw new SQLException("Creating product image failed, no ID obtained.");
            }
        }
    }

    @Override
    public void saveAll(long productId, List<ProductImage> images) throws SQLException {
        for (ProductImage img : images) {
            img.setProductId(productId);
            save(img);
        }
    }

    @Override
    public List<ProductImage> findByProductId(long productId) throws SQLException {
        String sql = "SELECT id, product_id, image_url, is_thumbnail FROM product_images WHERE product_id = ?";
        List<ProductImage> images = new ArrayList<>();
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setLong(1, productId);
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                images.add(ProductImageRowMapper.mapProductImage(rs));
            }
        }
        return images;
    }

    @Override
    public void deleteByProductId(long productId) throws SQLException {
        String sql = "DELETE FROM product_images WHERE product_id = ?";
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setLong(1, productId);
            stmt.executeUpdate();
        }
    }
}
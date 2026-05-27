package com.vbay.server.repository.JDBCrepository;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import com.vbay.server.mapper.rowmapper.ProductImageRowMapper;
import com.vbay.server.model.ProductImage;
import com.vbay.server.repository.ProductImageRepository;
import com.vbay.server.upload.ImageStorageService;

public class JdbcProductImageRepository implements ProductImageRepository {
    private final Connection connection;
    private final ImageStorageService imageStorageService;

    public JdbcProductImageRepository(Connection connection) {
        this(connection, new ImageStorageService());
    }

    public JdbcProductImageRepository(Connection connection, ImageStorageService imageStorageService) {
        this.connection = connection;
        this.imageStorageService = imageStorageService;
    }

    @Override
    public Optional<ProductImage> save(ProductImage productImage) throws SQLException {
        String sql = "INSERT INTO product_images (product_id, image_url, is_thumbnail) VALUES (?, ?, ?)";

        try (PreparedStatement statement = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            statement.setLong(1, productImage.getProductId());
            statement.setString(2, productImage.getImageUrl());
            statement.setBoolean(3, productImage.isThumbnail());
            statement.executeUpdate();

            try (ResultSet rs = statement.getGeneratedKeys()) {
                if (rs.next()) {
                    productImage.setId(rs.getLong(1));
                    return Optional.of(productImage);
                }
            }
        }

        throw new SQLException("Creating product image failed, no ID obtained.");
    }

    @Override
    public void saveAll(long productId, List<ProductImage> images) throws SQLException {
        for (ProductImage image : images) {
            image.setProductId(productId);
            save(image);
        }
    }

    @Override
    public List<ProductImage> findByProductId(long productId) throws SQLException {
        String sql = "SELECT id, product_id, image_url, is_thumbnail FROM product_images WHERE product_id = ?";
        List<ProductImage> images = new ArrayList<>();

        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setLong(1, productId);

            try (ResultSet rs = statement.executeQuery()) {
                while (rs.next()) {
                    images.add(ProductImageRowMapper.mapProductImage(rs));
                }
            }
        }

        return images;
    }

    @Override
    public Optional<String> findThumbnailUrlByProductId(long productId) throws SQLException {
        String sql = """
            SELECT image_url
            FROM product_images
            WHERE product_id = ?
            ORDER BY is_thumbnail DESC, id ASC
            LIMIT 1
            """;

        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setLong(1, productId);
            try (ResultSet rs = statement.executeQuery()) {
                if (!rs.next()) {
                    return Optional.empty();
                }
                return Optional.ofNullable(imageStorageService.toPublicThumbnailUrl(rs.getString("image_url")));
            }
        }
    }

    @Override
    public void deleteByProductId(long productId) throws SQLException {
        String sql = "DELETE FROM product_images WHERE product_id = ?";

        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setLong(1, productId);
            statement.executeUpdate();
        }
    }
}

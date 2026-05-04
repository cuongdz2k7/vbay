package com.vbay.server.repository.JDBCrepository;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import com.vbay.server.mapper.rowmapper.ProductRowMapper;
import com.vbay.server.model.Product;
import com.vbay.server.repository.ProductImageRepository;
import com.vbay.server.repository.ProductRepository;
import com.vbay.shared.status.shared_status.ProductStatus;


public class JdbcProductRepository implements ProductRepository {
    private final Connection connection;

    public JdbcProductRepository(Connection connection) {
        this.connection = connection;
    }

    @Override
    public Product save (Product product) throws SQLException {
        String sql = "INSERT INTO products (seller_id, name, description, category_id, product_condition, status) VALUES (?, ?, ?, ?, ?, ?)";
        try (PreparedStatement statement = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            statement.setLong(1, product.getSellerId());
            statement.setString(2, product.getName());
            statement.setString(3, product.getDescription());
            statement.setLong(4, product.getCategoryId());
            statement.setString(5, product.getCondition());
            statement.setString(6, product.getStatus().name());
            statement.executeUpdate();
            try (ResultSet rs = statement.getGeneratedKeys()) {
                if (rs.next()) {
                    product.setId(rs.getLong(1));
                    loadTimestamps(product);
                    return product;
                }
            }
        } 
        throw new SQLException("Creating product failed, no ID obtained.");
    }

    private void loadTimestamps(Product product) throws SQLException {
        String sql = "SELECT created_at, updated_at FROM products WHERE id = ?";
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setLong(1, product.getId());
            try (ResultSet rs = statement.executeQuery()) {
                if (rs.next()) {
                    LocalDateTime createdAt = rs.getTimestamp("created_at").toLocalDateTime();
                    LocalDateTime updatedAt = rs.getTimestamp("updated_at").toLocalDateTime();
                    product.setCreatedAt(createdAt);
                    product.setUpdatedAt(updatedAt);
                }
            }
        }
    }

    @Override
    public Optional<Product> findById(long productId) throws SQLException {
        String sql = """
            SELECT id, seller_id, name, description, category_id, product_condition AS `condition`, status, created_at, updated_at
            FROM products
            WHERE id = ?
            """;

        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setLong(1, productId);

            try (ResultSet rs = statement.executeQuery()) {
                if (!rs.next()) {
                    return Optional.empty();
                }

                Product product = ProductRowMapper.mapProduct(rs);
                ProductImageRepository imageRepository =
                    new JdbcProductImageRepository(connection);

                product.setImages(imageRepository.findByProductId(product.getId()));
                return Optional.of(product);
            }
        }
    }


    @Override
    public List<Product> findBySellerId(long sellerId) throws SQLException {
        String sql = """
            SELECT id, seller_id, name, description, category_id, product_condition AS `condition`, status, created_at, updated_at
            FROM products
            WHERE seller_id = ?
            """;

        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setLong(1, sellerId);

            try (ResultSet rs = statement.executeQuery()) {
                if (!rs.next()) {
                    return List.of();
                }

                Product product = ProductRowMapper.mapProduct(rs);
                ProductImageRepository imageRepository =
                    new JdbcProductImageRepository(connection);

                product.setImages(imageRepository.findByProductId(product.getId()));
                return List.of(product);
            }
        }
    }

    @Override
    public void updateStatus(long productId, ProductStatus status) throws SQLException {
        String sql = "UPDATE products SET status = ? WHERE id = ?";
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, status.name());
            statement.setLong(2, productId);
            statement.executeUpdate();
        }
    }

    @Override
    public boolean existsById(long productId) throws SQLException {
        String sql = "SELECT 1 FROM products WHERE id = ?";
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setLong(1, productId);
            ResultSet rs = statement.executeQuery();
            return rs.next();
        }
    }
}

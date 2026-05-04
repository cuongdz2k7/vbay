package com.vbay.server.mapper.rowmapper;

import java.util.List;

import com.vbay.server.model.Product;
import com.vbay.shared.status.shared_status.ProductStatus;

public class ProductRowMapper {
    private ProductRowMapper() {}
    ///result set không có trường images, nên cần phải query thêm để lấy images sau đó set vào product
    public static Product mapProduct(java.sql.ResultSet rs) throws java.sql.SQLException {
        return new Product(
                    rs.getLong("id"),
                    rs.getLong("seller_id"),
                    rs.getString("name"),
                    List.of(), // sẽ set images sau khi query thêm, vì result set này không có trường images
                    rs.getString("description"),
                    rs.getLong("category_id"),
                    rs.getString("condition"),
                    ProductStatus.valueOf(rs.getString("status")),
                    rs.getTimestamp("created_at").toLocalDateTime(),
                    rs.getTimestamp("updated_at").toLocalDateTime()
                );
    }
}

package com.vbay.server.mapper.rowmapper;
import java.sql.SQLException;

import com.vbay.server.Model.ProductImage;

public class ProductImageRowMapper {
    private ProductImageRowMapper() {}

    public static ProductImage mapProductImage(java.sql.ResultSet rs) throws SQLException {
        return new ProductImage(
                    rs.getLong("id"),
                    rs.getLong("product_id"),
                    rs.getString("image_url"),
                    rs.getBoolean("is_thumbnail")
                );
            }
}

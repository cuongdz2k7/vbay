package com.vbay.server.mapper.dtomapper;

import java.util.ArrayList;
import java.util.List;

import com.vbay.server.Model.ProductImage;
import com.vbay.shared.dto.productDTO.ProductImageDTO;

public class ProductImageMapper {
        private ProductImageMapper() {}
    
        public static List<ProductImage> mapToProductImages(List<ProductImageDTO> imagesDTO) {
            List<ProductImage> images = new ArrayList<>();
            for (ProductImageDTO dto : imagesDTO) {
                images.add(new ProductImage(dto.getImageUrl(), dto.isThumbnail()));
            }
            return images;
        }
}

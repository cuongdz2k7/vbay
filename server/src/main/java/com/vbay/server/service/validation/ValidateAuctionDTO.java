package com.vbay.server.service.validation;

import java.math.BigDecimal;

import com.vbay.server.exception.ValidationException;
import com.vbay.shared.dto.auctionDTO.CreateAuctionRequest;
import com.vbay.shared.dto.productDTO.CreateProductRequest;
import com.vbay.shared.dto.productDTO.ProductImageDTO;

public class ValidateAuctionDTO {
    private static void validateRequiredSections(CreateAuctionRequest request) {
        ValidationUtils.requireNotNull(request.getProduct(), "Product data is required");
        ValidationUtils.requireNotNull(request, "Auction data is required");
    }

    private static void validateProductFields(CreateProductRequest product) {
        ValidationUtils.requireNotBlank(product.getName(), "Product name is required");
        ValidationUtils.requireNotNull(product.getImages(), "Product images are required");
        ValidationUtils.requireNotEmpty(product.getImages(), "At least one product image is required");
        long hasThumbnail = product.getImages().stream().filter(ProductImageDTO::isThumbnail).count();
        if (hasThumbnail == 0) {
            product.getImages().get(0).setThumbnail(true);
        }
        if (hasThumbnail > 1) {
            throw new ValidationException("Only one product image can be marked as thumbnail");
        }
    }

    private static void validateAuctionRequiredFields(CreateAuctionRequest auction) {
        ValidationUtils.requireNotBlank(auction.getTitle(), "Auction title is required");
        ValidationUtils.requireNotNull(auction.getStartingTime(), "Start time is required");
        ValidationUtils.requireNotNull(auction.getEndingTime(), "End time is required");
    }

    private static void validateAuctionMoneyFields(CreateAuctionRequest auction) {
        ValidationUtils.requirePositive(auction.getStartingPrice(), "Starting price must be a positive number");
        ValidationUtils.requirePositive(auction.getMinimumBidStep(), "Minimum bid step must be a positive number");
        ValidationUtils.requireMoneyScale(auction.getStartingPrice(), "Starting price");
        ValidationUtils.requireMoneyScale(auction.getMinimumBidStep(), "Minimum bid step");
        ValidationUtils.requireMoneyScale(auction.getReservePrice(), "Reserve price");
        ValidationUtils.requireMoneyScale(auction.getBuyNowPrice(), "Buy now price");
    }

    private static void validateAuctionTimeFields(CreateAuctionRequest auction) {
        ///UI check thêm startingtime phải sau current time nữa, để tránh trường hợp tạo auction xong là nó đã kết thúc luôn rồi
        //nhưng mà validate ở đây thì chỉ cần check startingtime phải trước endingtime, còn việc startingtime phải sau current time thì UI phải check trước khi gửi request lên server
        if (!auction.getStartingTime().isBefore(auction.getEndingTime())) {
            throw new ValidationException("Start time must be before end time");
        }
    }

    private static void validateAuctionBusinessRules(CreateAuctionRequest auction) {
        if (auction.getReservePrice() != null
                && auction.getReservePrice().compareTo(BigDecimal.ZERO) > 0
                && auction.getReservePrice().compareTo(auction.getStartingPrice()) < 0) {
            throw new ValidationException("Reserve price must be greater than or equal to starting price");
        }
        if (auction.getReservePrice() != null
                && auction.getReservePrice().compareTo(BigDecimal.ZERO) > 0
                && auction.getBuyNowPrice() != null
                && auction.getBuyNowPrice().compareTo(BigDecimal.ZERO) > 0
                && auction.getBuyNowPrice().compareTo(auction.getReservePrice()) < 0) {
            throw new ValidationException("Buy now price must be greater than or equal to reserve price");
        }
    }

    public static void validateCreateAuctionRequest(CreateAuctionRequest request) {
        if (request == null) {
            throw new ValidationException("Request cannot be null");
        }
        validateRequiredSections(request);
        validateProductFields(request.getProduct());
        validateAuctionRequiredFields(request);
        validateAuctionMoneyFields(request);
        validateAuctionTimeFields(request);
        validateAuctionBusinessRules(request);
    }

}

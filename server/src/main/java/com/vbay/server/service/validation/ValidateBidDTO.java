package com.vbay.server.service.validation;

import java.math.BigDecimal;

import com.vbay.server.exception.ValidationException;
import com.vbay.shared.dto.auctionDTO.BuyNowRequest;
import com.vbay.shared.dto.auctionDTO.PlaceBidRequest;

public class ValidateBidDTO {
    private static void validateBidRequiredSection(PlaceBidRequest request) {
        ValidationUtils.requireNotNull(request.getAuctionId(), "Auction ID is required");
        ValidationUtils.requireNotNull(request.getBidAmount(), "Bid amount is required");
    }

    private static void validateBidMoneyFields(PlaceBidRequest request) {
        ValidationUtils.requirePositive(request.getBidAmount(), "Bid amount must be a positive number");
        ValidationUtils.requireMoneyScale(request.getBidAmount(), "Bid amount");
    }

    public static void validatePlaceBidRequest(PlaceBidRequest request) {
        if (request == null) {
            throw new ValidationException("Request cannot be null");
        }
        validateBidRequiredSection(request);
        validateBidMoneyFields(request);
    }

    public static void validateBuyNowRequest(BuyNowRequest request) {
        if (request == null) {
            throw new ValidationException("Request cannot be null");
        }
        ValidationUtils.requireNotNull(request.getAuctionId(), "Auction ID is required");
    }

    public static void validateAutoBidRequest(long auctionId, BigDecimal maxBidAmount) {
        if (auctionId <= 0) {
            throw new ValidationException("Auction ID is required");
        }
        ValidationUtils.requireNotNull(maxBidAmount, "Max bid amount is required");
        ValidationUtils.requirePositive(maxBidAmount, "Max bid amount must be a positive number");
        ValidationUtils.requireMoneyScale(maxBidAmount, "Max bid amount");
    }

}

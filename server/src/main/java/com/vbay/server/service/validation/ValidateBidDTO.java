package com.vbay.server.service.validation;

import com.vbay.server.exception.ValidationException;
import com.vbay.shared.dto.auctionDTO.PlaceBidRequest;

public class ValidateBidDTO {
    private static void validateBidRequiredSection(PlaceBidRequest request) {
        ValidationUtils.requireNotNull(request.getAuctionId(), "Auction ID is required");
        ValidationUtils.requireNotNull(request.getBidAmount(), "Bid amount is required");
    }

    private static void validateBidMoneyFields(PlaceBidRequest request) {
        ValidationUtils.requirePositive(request.getBidAmount(), "Bid amount must be a positive number");
    }

    public static void validatePlaceBidRequest(PlaceBidRequest request) {
        if (request == null) {
            throw new ValidationException("Request cannot be null");
        }
        validateBidRequiredSection(request);
        validateBidMoneyFields(request);
    }

}

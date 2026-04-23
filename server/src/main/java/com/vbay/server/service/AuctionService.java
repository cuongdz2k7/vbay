package com.vbay.server.service;

import com.vbay.server.exception.ValidationException;
import com.vbay.server.service.validation.ValidationUtils;
import com.vbay.shared.dto.CreateAuctionRequest;
import com.vbay.shared.dto.CreateAuctionRespond;

public class AuctionService {

    private void validateRequiredFields(CreateAuctionRequest request) {
        ValidationUtils.requireNotNull(request.getProductId(), "Product id is required");
        ValidationUtils.requireNotNull(request.getSellerId(), "Seller id is required");
        ValidationUtils.requireNotNull(request.getStartingTime(), "Start time is required");
        ValidationUtils.requireNotNull(request.getEndingTime(), "End time is required");
        ValidationUtils.requireNotNull(request.getStartingPrice(), "Starting price is required");

    }
    
    private void validateIdFields(CreateAuctionRequest request) {
        ValidationUtils.requirePositive(request.getSellerId(), "Seller id must be a positive number");
        ValidationUtils.requirePositive(request.getProductId(), "Product id must be a positive number");
    }

    private void validateMoneyFields(CreateAuctionRequest request) {
        ValidationUtils.requirePositive(request.getStartingPrice(), "Starting price must be a positive number");
        ValidationUtils.requirePositive(request.getReservePrice(), "Reserve price must be a positive number");
        ValidationUtils.requirePositive(request.getMinimumBidStep(), "Minimum bid step must be a positive number");
    }

    private void validateTimeFields(CreateAuctionRequest request) {
        if (request.getStartingTime().isAfter(request.getEndingTime())) {
            throw new ValidationException("Start time must be before end time");
        }
    }
    private void validateBusinessRules(CreateAuctionRequest request) {
        if (request.getBuyNowPrice() > 0 && request.getBuyNowPrice() < request.getStartingPrice()) {
            throw new ValidationException("Buy now price must be greater than or equal to starting price");
        }
        if (request.getReservePrice() > 0 && request.getReservePrice() < request.getStartingPrice()) {
            throw new ValidationException("Reserve price must be greater than or equal to starting price");
        }
        if (request.getReservePrice() > 0 && request.getBuyNowPrice() > 0 && request.getReservePrice() < request.getBuyNowPrice()) {
            throw new ValidationException("Reserve price must be greater than or equal to buy now price");
        }
    }

    public void validateCreateAuctionRequest(CreateAuctionRequest request) {
        if (request == null) {
            throw new ValidationException("Request cannot be null");
        }
        validateRequiredFields(request);
        validateIdFields(request);
        validateMoneyFields(request);
        validateTimeFields(request);
        validateBusinessRules(request);
    }
    public CreateAuctionRespond createAuction (CreateAuctionRequest request) {
        validateCreateAuctionRequest(request);
        if (request.getProductId() )
        


    }
}
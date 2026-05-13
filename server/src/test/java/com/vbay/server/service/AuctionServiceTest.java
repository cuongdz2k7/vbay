package com.vbay.server.service;

import java.math.BigDecimal;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import com.vbay.server.exception.ValidationException;
import com.vbay.server.repository.JDBCrepository.JdbcRepositoryFactory;
import com.vbay.server.realtime.publisher.DomainEventPublisher;
import com.vbay.server.service.validation.ValidateAuctionDTO;
import com.vbay.shared.dto.auctionDTO.CreateAuctionRequest;
import com.vbay.shared.dto.productDTO.CreateProductRequest;
import com.vbay.shared.dto.productDTO.ProductImageDTO;

class AuctionServiceTest {
    private static final DomainEventPublisher NO_OP_PUBLISHER = event -> { };
    private static AuctionService auctionService;

    @BeforeAll
    static void setUp() {
        auctionService = new AuctionService(
            () -> { throw new SQLException("Connection is not used in validation-only tests"); },
            new JdbcRepositoryFactory(),
            NO_OP_PUBLISHER
        );
    }

    private CreateProductRequest validProduct() {
        return new CreateProductRequest(
            "iPhone 15",
            List.of(new ProductImageDTO("https://example.com/iphone.jpg", true)),
            "Good condition",
            "USED",
            1
        );
    }

    private CreateAuctionRequest validRequest() {
        return new CreateAuctionRequest(
            validProduct(),
            "iPhone 15 auction",
            "Auction description", 
            new BigDecimal("100.00"),
            new BigDecimal("150.00"),
            new BigDecimal("200.00"),
            new BigDecimal("10.00"),
            LocalDateTime.now().plusHours(1),
            LocalDateTime.now().plusHours(2)
        );
    }

    @Test
    void validateCreateAuctionRequest_validRequest_doesNotThrow() {
        assertDoesNotThrow(() -> ValidateAuctionDTO.validateCreateAuctionRequest(validRequest()));
    }

    @Test
    void validateCreateAuctionRequest_nullRequest_throwsValidationException() {
        assertThrows(
            ValidationException.class,
            () -> ValidateAuctionDTO.validateCreateAuctionRequest(null)
        );
    }

    @Test
    void validateCreateAuctionRequest_nullProduct_throwsValidationException() {
        CreateAuctionRequest request = new CreateAuctionRequest(
            null,
            "Auction",
            "Description",
            new BigDecimal("100.00"),
            null,
            null,
            new BigDecimal("10.00"),
            LocalDateTime.now().plusHours(1),
            LocalDateTime.now().plusHours(2)
        );

        assertThrows(
            ValidationException.class,
            () -> ValidateAuctionDTO.validateCreateAuctionRequest(request)
        );
    }

    @Test
    void validateCreateAuctionRequest_blankProductName_throwsValidationException() {
        CreateProductRequest product = new CreateProductRequest(
            " ",
            List.of(new ProductImageDTO("https://example.com/image.jpg", true)),
            "Description",
            "USED",
            1L
        );
        CreateAuctionRequest request = new CreateAuctionRequest(
            product,
            "Auction",
            "Description",
            new BigDecimal("100.00"),
            null,
            null,
            new BigDecimal("10.00"),
            LocalDateTime.now().plusHours(1),
            LocalDateTime.now().plusHours(2)
        );

        assertThrows(
            ValidationException.class,
            () -> ValidateAuctionDTO.validateCreateAuctionRequest(request)
        );
    }

    @Test
    void validateCreateAuctionRequest_emptyProductImages_throwsValidationException() {
        CreateProductRequest product = new CreateProductRequest(
            "iPhone 15",
            List.of(),
            "Description",
            "USED",
            1L
        );
        CreateAuctionRequest request = new CreateAuctionRequest(
            product,
            "Auction",
            "Description",
            new BigDecimal("100.00"),
            null,
            null,
            new BigDecimal("10.00"),
            LocalDateTime.now().plusHours(1),
            LocalDateTime.now().plusHours(2)
        );

        assertThrows(
            ValidationException.class,
            () -> ValidateAuctionDTO.validateCreateAuctionRequest(request)
        );
    }

    @Test
    void validateCreateAuctionRequest_blankTitle_throwsValidationException() {
        CreateAuctionRequest request = new CreateAuctionRequest(
            validProduct(),
            " ",
            "Description",
            new BigDecimal("100.00"),
            null,
            null,
            new BigDecimal("10.00"),
            LocalDateTime.now().plusHours(1),
            LocalDateTime.now().plusHours(2)
        );

        assertThrows(
            ValidationException.class,
            () -> ValidateAuctionDTO.validateCreateAuctionRequest(request)
        );
    }

    @Test
    void validateCreateAuctionRequest_startingPriceNotPositive_throwsValidationException() {
        CreateAuctionRequest request = new CreateAuctionRequest(
            validProduct(),
            "Auction",
            "Description",
            BigDecimal.ZERO,
            null,
            null,
            new BigDecimal("10.00"),
            LocalDateTime.now().plusHours(1),
            LocalDateTime.now().plusHours(2)
        );

        assertThrows(
            ValidationException.class,
            () -> ValidateAuctionDTO.validateCreateAuctionRequest(request)
        );
    }

    @Test
    void validateCreateAuctionRequest_minimumBidStepNotPositive_throwsValidationException() {
        CreateAuctionRequest request = new CreateAuctionRequest(
            validProduct(),
            "Auction",
            "Description",
            new BigDecimal("100.00"),
            null,
            null,
            BigDecimal.ZERO,
            LocalDateTime.now().plusHours(1),
            LocalDateTime.now().plusHours(2)
        );

        assertThrows(
            ValidationException.class,
            () -> ValidateAuctionDTO.validateCreateAuctionRequest(request)
        );
    }

    @Test
    void validateCreateAuctionRequest_startTimeAfterEndTime_throwsValidationException() {
        CreateAuctionRequest request = new CreateAuctionRequest(
            validProduct(),
            "Auction",
            "Description",
            new BigDecimal("100.00"),
            null,
            null,
            new BigDecimal("10.00"),
            LocalDateTime.now().plusHours(2),
            LocalDateTime.now().plusHours(1)
        );

        assertThrows(
            ValidationException.class,
            () -> ValidateAuctionDTO.validateCreateAuctionRequest(request)
        );
    }

    @Test
    void validateCreateAuctionRequest_reservePriceLessThanStartingPrice_throwsValidationException() {
        CreateAuctionRequest request = new CreateAuctionRequest(
            validProduct(),
            "Auction",
            "Description",
            new BigDecimal("100.00"),
            new BigDecimal("90.00"),
            null,
            new BigDecimal("10.00"),
            LocalDateTime.now().plusHours(1),
            LocalDateTime.now().plusHours(2)
        );

        assertThrows(
            ValidationException.class,
            () -> ValidateAuctionDTO.validateCreateAuctionRequest(request)
        );
    }

    @Test
    void validateCreateAuctionRequest_buyNowPriceLessThanReservePrice_throwsValidationException() {
        CreateAuctionRequest request = new CreateAuctionRequest(
            validProduct(),
            "Auction",
            "Description",
            new BigDecimal("100.00"),
            new BigDecimal("120.00"),
            new BigDecimal("110.00"),
            new BigDecimal("10.00"),
            LocalDateTime.now().plusHours(1),
            LocalDateTime.now().plusHours(2)
        );

        assertThrows(
            ValidationException.class,
            () -> ValidateAuctionDTO.validateCreateAuctionRequest(request)
        );
    }

    @Test
    void validateCreateAuctionRequest_buyNowPriceGreaterThanReservePrice_doesNotThrow() {
        CreateAuctionRequest request = new CreateAuctionRequest(
            validProduct(),
            "Auction",
            "Description",
            new BigDecimal("100.00"),
            new BigDecimal("150.00"),
            new BigDecimal("200.00"),
            new BigDecimal("10.00"),
            LocalDateTime.now().plusHours(1),
            LocalDateTime.now().plusHours(2)
        );

        assertDoesNotThrow(() -> ValidateAuctionDTO.validateCreateAuctionRequest(request));
    }
}

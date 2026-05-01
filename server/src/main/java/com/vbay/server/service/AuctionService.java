package com.vbay.server.service;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.List;

import com.vbay.server.model.Auction;
import com.vbay.server.model.Product;
import com.vbay.server.model.ProductImage;
import com.vbay.server.network_connection.ClientSession;
import com.vbay.server.databaseManager.DatabaseConnection;
import com.vbay.server.exception.ValidationException;
import com.vbay.server.mapper.dtomapper.ProductImageMapper;
import com.vbay.server.repository.AuctionRepository;
import com.vbay.server.repository.ProductImageRepository;
import com.vbay.server.repository.ProductRepository;
import com.vbay.server.repository.RepositoryFactory;
import com.vbay.server.repository.UserRepository;
import com.vbay.server.service.validation.ValidationUtils;
import com.vbay.shared.dto.auctionDTO.CreateAuctionRequest;
import com.vbay.shared.dto.auctionDTO.PlaceBidRequest;
import com.vbay.shared.dto.productDTO.CreateProductRequest;
import com.vbay.shared.dto.productDTO.ProductImageDTO;
import com.vbay.server.databaseManager.ConnectionProvider;
import com.vbay.server.exception.AuthenticationException;
import com.vbay.server.model.Bid;



 /*
* Business rules:
* 1. Giá tiền phải là số dương
* 2. Nếu có reserve price thì reserve price phải lớn hơn hoặc bằng starting price
* 3. Nếu có buy now price thì buy now price phải lớn hơn hoặc bằng reserve price
* 4. Thời gian bắt đầu phải trước thời gian kết thúc
* 5. Khi tạo auction, product sẽ được tạo với status là AVAILABLE, sau đó khi auction bắt đầu thì product sẽ được update thành ACTIVE, khi auction kết thúc hoặc bị hủy thì product sẽ được update thành INACTIVE
* 6. Mỗi ảnh chỉ có 1 thumbnail, nếu có nhiều hơn 1 ảnh được đánh dấu là thumbnail thì sẽ throw validation exception
* 7. Khi tạo auction, phải có ít nhất 1 ảnh của product, nếu không có ảnh nào là thumbnail thì sẽ tự động đánh dấu ảnh đầu tiên là thumbnail
*/

/*
các bước để place bid:
1. Insert bid mới
2. Update current_price của auction
3. Trừ tiền/cọc user
Authentication/Authorization check:
- Auction đó có tồn tại không?  \
- Auction đó đang ACTIVE không? \
- User có phải seller của auction đó không? \
- Auction đã bắt đầu/chưa kết thúc chưa? nhỡ đâu auction đang active nhưng chưa đến thời gian bắt đầu hoặc đã qua thời gian kết thúc
- Bid amount có >= current_price + minimum_bid_step không? \

- User có bị banned/không đủ điều kiện không? 
- Nếu có balance/đặt cọc thì đủ tiền không?
- Nếu có buy now price thì bid amount có >= buy now price không? 
        nếu có buy now price mà bid amount đã >= buy now price rồi thì coi như mua luôn, auction kết thúc ngay lập tức
        và UI phải check nếu bid amount >= buy now price thì hiển thị you are about to buy this item with buy now price, do you want to proceed? để tránh trường hợp user nhập bid amount rất lớn vượt xa buy now price rồi vô tình mua luôn
 */

public class AuctionService {
    ///tạo connection provider để sử dụng h2 in-memory database cho integration test, tránh ảnh hưởng đến database thật khi test
    private final ConnectionProvider connectionProvider;
    private final RepositoryFactory repositoryFactory;

    public AuctionService(ConnectionProvider connectionProvider, RepositoryFactory repositoryFactory) {
        this.connectionProvider = connectionProvider;
        this.repositoryFactory = repositoryFactory;
    }

    private void checkSession(ClientSession session) {
        if (session == null || !session.isAuthenticated()) {
            throw new AuthenticationException("User must be logged in to perform this action");
        }
    }


    private void validateRequiredSections(CreateAuctionRequest request) {
        ValidationUtils.requireNotNull(request.getProduct(), "Product data is required");
        ValidationUtils.requireNotNull(request, "Auction data is required");
    }

    private void validateProductFields(CreateProductRequest product) {
        ValidationUtils.requireNotBlank(product.getName(), "Product name is required");
        ValidationUtils.requireNotNull(product.getImages(), "Product images are required");
        ValidationUtils.requireNotEmpty(product.getImages(), "At least one product image is required");
        long hasThumbnail = product.getImages().stream().filter(ProductImageDTO::isThumbnail).count();
        if (hasThumbnail == 0) {
            throw new ValidationException("At least one product image must be marked as thumbnail");
        }
        if (hasThumbnail > 1) {
            throw new ValidationException("Only one product image can be marked as thumbnail");
        }
    }

    private void validateAuctionRequiredFields(CreateAuctionRequest auction) {
        ValidationUtils.requireNotBlank(auction.getTitle(), "Auction title is required");
        ValidationUtils.requireNotNull(auction.getStartingTime(), "Start time is required");
        ValidationUtils.requireNotNull(auction.getEndingTime(), "End time is required");
    }

    private void validateAuctionMoneyFields(CreateAuctionRequest auction) {
        ValidationUtils.requirePositive(auction.getStartingPrice(), "Starting price must be a positive number");
        ValidationUtils.requirePositive(auction.getMinimumBidStep(), "Minimum bid step must be a positive number");
    }

    private void validateAuctionTimeFields(CreateAuctionRequest auction) {
        ///UI check thêm startingtime phải sau current time nữa, để tránh trường hợp tạo auction xong là nó đã kết thúc luôn rồi
        //nhưng mà validate ở đây thì chỉ cần check startingtime phải trước endingtime, còn việc startingtime phải sau current time thì UI phải check trước khi gửi request lên server
        if (!auction.getStartingTime().isBefore(auction.getEndingTime())) {
            throw new ValidationException("Start time must be before end time");
        }
    }

    private void validateAuctionBusinessRules(CreateAuctionRequest auction) {
        if (auction.getBuyNowPrice() != null
                && auction.getBuyNowPrice().compareTo(BigDecimal.ZERO) > 0
                && auction.getBuyNowPrice().compareTo(auction.getStartingPrice()) < 0) {
            throw new ValidationException("Buy now price must be greater than or equal to starting price");
        }
        if (auction.getReservePrice() != null
                && auction.getReservePrice().compareTo(BigDecimal.ZERO) > 0
                && auction.getReservePrice().compareTo(auction.getStartingPrice()) < 0) {
            throw new ValidationException("Reserve price must be greater than or equal to starting price");
        }
        if (auction.getReservePrice() != null
                && auction.getReservePrice().compareTo(BigDecimal.ZERO) > 0
                && auction.getBuyNowPrice() != null
                && auction.getBuyNowPrice().compareTo(BigDecimal.ZERO) > 0
                && auction.getReservePrice().compareTo(auction.getBuyNowPrice()) < 0) {
            throw new ValidationException("Reserve price must be greater than or equal to buy now price");
        }
    }

    public void validateCreateAuctionRequest(CreateAuctionRequest request) {
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

    public long createProduct(CreateProductRequest request, ClientSession session, ProductRepository productRepository, ProductImageRepository productImageRepository) throws SQLException {
        List<ProductImage> Images = ProductImageMapper.mapToProductImages(request.getImages());
        Product product = new Product(
            request.getName(),
            request.getDescription(),
            request.getCategoryId(),
            request.getCondition()
        );
        product.setSellerId(session.getUserId());
        productRepository.save(product);
        ///getid đúng vì productRepository.save đã đồng thời set id cho product rồi
        productImageRepository.saveAll(product.getId(), Images);
        return product.getId();
    }

    public void createAuction(CreateAuctionRequest request, ClientSession session) throws SQLException {
        checkSession(session);
        validateCreateAuctionRequest(request);

        try (Connection connection = connectionProvider.getConnection()) {
            connection.setAutoCommit(false);

            try {
                ProductRepository productRepository = repositoryFactory.createProductRepository(connection);
                ProductImageRepository productImageRepository = repositoryFactory.createProductImageRepository(connection);
                AuctionRepository auctionRepository = repositoryFactory.createAuctionRepository(connection);

                // 1. tạo Product object
                long productId = createProduct(request.getProduct(), session, productRepository, productImageRepository);
                // 4. auctionRepository.save(auction)
                Auction auction = new Auction(
                    session.getUserId(),
                    productId,
                    request.getTitle(),
                    request.getDescription(),
                    request.getBuyNowPrice(),
                    request.getReservePrice(),
                    request.getMinimumBidStep(),
                    request.getStartingPrice(),
                    request.getStartingTime(),
                    request.getEndingTime()
                );
                auctionRepository.save(auction);
                connection.commit();
            } catch (SQLException | RuntimeException e) {
                connection.rollback();
                throw e;
            }
        }
    }

    void ValidateBidRequiredSection(PlaceBidRequest request) {
        ValidationUtils.requireNotNull(request.getAuctionId(), "Auction ID is required");
        ValidationUtils.requireNotNull(request.getBidAmount(), "Bid amount is required");
    }

    void ValidateBidMoneyFields(PlaceBidRequest request) {
        ValidationUtils.requirePositive(request.getBidAmount(), "Bid amount must be a positive number");
    }

    void ValidatePlaceBidRequest(PlaceBidRequest request) {
        if (request == null) {
            throw new ValidationException("Request cannot be null");
        }
        ValidateBidRequiredSection(request);
        ValidateBidMoneyFields(request);
    }



    public void placeBid(PlaceBidRequest request, ClientSession session) {
        checkSession(session);
        ValidatePlaceBidRequest(request);
       ///Authentication/Authorization check:
        try (Connection connection = connectionProvider.getConnection()) {
            AuctionRepository auctionRepository = repositoryFactory.createAuctionRepository(connection);
            UserRepository userRepository = repositoryFactory.createUserRepository(connection);
            Auction auction = auctionRepository.findById(request.getAuctionId())
                        .orElseThrow(() -> new ValidationException("Auction not found"));
        } catch (SQLException e) {
            throw new RuntimeException("Failed to place bid: " + e.getMessage(), e);
        }
    }


}

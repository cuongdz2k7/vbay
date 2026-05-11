package com.vbay.server.service;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;

import com.vbay.server.databaseManager.ConnectionProvider;
import com.vbay.server.exception.AuthenticationException;
import com.vbay.server.exception.ValidationException;
import com.vbay.server.mapper.dtomapper.ProductImageMapper;
import com.vbay.server.model.Auction;
import com.vbay.server.model.Product;
import com.vbay.server.model.ProductImage;
import com.vbay.server.network_connection.ClientSession;
import com.vbay.server.realtime.domain.AuctionCreatedDomainEvent;
import com.vbay.server.realtime.publisher.DomainEventPublisher;
import com.vbay.server.repository.AuctionRepository;
import com.vbay.server.repository.ProductImageRepository;
import com.vbay.server.repository.ProductRepository;
import com.vbay.server.repository.RepositoryFactory;
import com.vbay.server.service.result.CreateAuctionResult;
import com.vbay.server.service.result.mapper.ResultMapper;
import com.vbay.server.service.validation.ValidateAuctionDTO;
import com.vbay.shared.dto.auctionDTO.AuctionListRequest;
import com.vbay.shared.dto.auctionDTO.AuctionListResponse;
import com.vbay.shared.dto.auctionDTO.CreateAuctionRequest;
import com.vbay.shared.dto.productDTO.CreateProductRequest;
import com.vbay.shared.enums.auction.AuctionStatus;

 /*
* Business rules:
* 1. Giá tiền phải là số dương
* 2. Nếu có reserve price thì reserve price phải lớn hơn hoặc bằng starting price
* 3. Nếu có buy now price thì buy now price phải lớn hơn hoặc bằng reserve price
* 4. Thời gian bắt đầu phải trước thời gian kết thúc
* 5. Khi tạo auction, product sẽ được tạo với status là AVAILABLE, sau đó khi auction bắt đầu thì product sẽ được update thành ACTIVE, khi auction kết thúc hoặc bị hủy thì product sẽ được update thành INACTIVE
* 6. Mỗi ảnh chỉ có 1 thumbnail, nếu có nhiều hơn 1 ảnh được đánh dấu là thumbnail thì sẽ throw validation exception
* 7. Khi tạo auction, phải có ít nhất 1 ảnh của product, nếu không có ảnh nào là thumbnail thì sẽ tự động đánh dấu ảnh đầu tiên là thumbnail

Rule buy now price + reserve price: (bài tập lớn sẽ không implement)
Auction status: 
    DRAFT:
    seller sửa thoải mái

    ACTIVE + no bids:
        seller sửa reservePrice, buyNowPrice được

    ACTIVE + has bids:
        seller không được sửa reservePrice
        seller không được sửa buyNowPrice
        optional: disable Buy Now sau bid đầu tiên

    ENDED / SOLD / CANCELLED:
        không sửa pricing terms
Rule sửa starting time, ending time : comming soon... (bài tập lớn sẽ không implement)
*/

public class AuctionService {
    private static final int MAX_AUCTION_LIST_LIMIT = 500;

    ///tạo connection provider để sử dụng h2 in-memory database cho integration test, tránh ảnh hưởng đến database thật khi test
    private final ConnectionProvider connectionProvider;
    private final RepositoryFactory repositoryFactory;
    private final DomainEventPublisher domainEventPublisher;

    public AuctionService(ConnectionProvider connectionProvider, RepositoryFactory repositoryFactory, DomainEventPublisher domainEventPublisher) {
        this.connectionProvider = connectionProvider;
        this.repositoryFactory = repositoryFactory;
        this.domainEventPublisher = domainEventPublisher;
    }

    private void checkSession(ClientSession session) {
        if (session == null || !session.isAuthenticated()) {
            throw new AuthenticationException("User must be logged in to perform this action");
        }
    }

    public Product createProduct(CreateProductRequest request, ClientSession session, ProductRepository productRepository, ProductImageRepository productImageRepository) throws SQLException {
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
        return product;
    }

    public CreateAuctionResult createAuction(CreateAuctionRequest request, ClientSession session) throws SQLException {
        checkSession(session);
        ValidateAuctionDTO.validateCreateAuctionRequest(request);

        try (Connection connection = connectionProvider.getConnection()) {
            connection.setAutoCommit(false);

            try {
                ProductRepository productRepository = repositoryFactory.createProductRepository(connection);
                ProductImageRepository productImageRepository = repositoryFactory.createProductImageRepository(connection);
                AuctionRepository auctionRepository = repositoryFactory.createAuctionRepository(connection);

                // 1. tạo Product object
                Product product = createProduct(request.getProduct(), session, productRepository, productImageRepository);
                // 4. auctionRepository.save(auction)
                Auction auction = new Auction(
                    session.getUserId(),
                    product.getId(),
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
                CreateAuctionResult result = ResultMapper.toCreateAuctionResult(auction, product);
                domainEventPublisher.publish(new AuctionCreatedDomainEvent(result, java.time.LocalDateTime.now()));
                return result;
            } catch (SQLException | RuntimeException e) {
                connection.rollback();
                throw e;
            }
        }
    }

    public AuctionListResponse getAuctionList(AuctionListRequest request, ClientSession session) throws SQLException {
        checkSession(session);
        normalizeAuctionListRequest(request);

        try (Connection connection = connectionProvider.getConnection()) {
            AuctionRepository auctionRepository = repositoryFactory.createAuctionRepository(connection);
            return new AuctionListResponse(auctionRepository.findAuctionList(request));
        }
    }

    private void normalizeAuctionListRequest(AuctionListRequest request) {
        if (request == null) {
            throw new ValidationException("Auction list request is required");
        }

        request.setStatus(normalizeBlank(request.getStatus()));

        if (request.getStatus() != null) {
            try {
                AuctionStatus.valueOf(request.getStatus());
            } catch (IllegalArgumentException exception) {
                throw new ValidationException("Invalid auction status: " + request.getStatus());
            }
        }

        Integer limit = request.getLimit();
        if (limit == null) {
            return;
        }

        if (limit <= 0) {
            request.setLimit(null);
            return;
        }

        if (limit > MAX_AUCTION_LIST_LIMIT) {
            request.setLimit(MAX_AUCTION_LIST_LIMIT);
        }
    }

    private String normalizeBlank(String value) {
        return value == null || value.isBlank() ? null : value;
    }


}

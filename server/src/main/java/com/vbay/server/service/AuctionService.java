package com.vbay.server.service;

import java.sql.Connection;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import com.vbay.server.databaseManager.ConnectionProvider;
import com.vbay.server.exception.AuthenticationException;
import com.vbay.server.exception.ValidationException;
import com.vbay.server.mapper.dtomapper.ProductImageMapper;
import com.vbay.server.model.Auction;
import com.vbay.server.model.Product;
import com.vbay.server.model.ProductImage;
import com.vbay.server.network_connection.ClientSession;
import com.vbay.server.realtime.domain.AuctionListItemUpdatedDomainEvent;
import com.vbay.server.realtime.domain.enums.AuctionListItemUpdateReason;
import com.vbay.server.realtime.publisher.DomainEventPublisher;
import com.vbay.server.repository.AuctionRepository;
import com.vbay.server.repository.ProductImageRepository;
import com.vbay.server.repository.ProductRepository;
import com.vbay.server.repository.RepositoryFactory;
import com.vbay.server.repository.enums.AuctionTransition;
import com.vbay.server.service.result.AuctionListItemResult;
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
* 1.5. Nếu chưa có bid nào thì người dùng có thể bid bằng với starting price
* 2. Nếu có reserve price thì reserve price phải lớn hơn starting price
* 3. Nếu có buy now price thì buy now price phải lớn hơn reserve price (nếu không thì đặt reserve price làm gì ?)
* 4. Thời gian bắt đầu phải trước thời gian kết thúc
* 5. Khi tạo auction, product sẽ được tạo với status là AVAILABLE, sau đó khi auction bắt đầu thì product sẽ được update thành ACTIVE, khi auction kết thúc hoặc bị hủy thì product sẽ được update thành INACTIVE
* 6. Mỗi ảnh chỉ có 1 thumbnail, nếu có nhiều hơn 1 ảnh được đánh dấu là thumbnail thì sẽ throw validation exception
* 7. Khi tạo auction, phải có ít nhất 1 ảnh của product, nếu không có ảnh nào là thumbnail thì sẽ tự động đánh dấu ảnh đầu tiên là thumbnail
* 8. CooldownTime giữa mỗi lần bid là 10s
* 9. Reserve Price: Thường là Ẩn (Chỉ hiện thông báo "Reserve not met").
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
                AuctionListItemResult item = auctionRepository.findAuctionListItemById(auction.getId()).orElseThrow();
                domainEventPublisher.publish(new AuctionListItemUpdatedDomainEvent(
                    item,
                    AuctionListItemUpdateReason.CREATED,
                    java.time.LocalDateTime.now()
                ));
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
            return new AuctionListResponse(
                auctionRepository.findAuctionList(request).stream()
                    .map(ResultMapper::toAuctionListItemPayload)
                    .toList()
            );
        }
    }

    public LocalDateTime getDatabaseTime() throws SQLException {
        try (Connection connection = connectionProvider.getConnection()) {
            AuctionRepository auctionRepository =
                repositoryFactory.createAuctionRepository(connection);
            return auctionRepository.getCurrentDatabaseTime();
        }
    }

    boolean checkIfAuctionFailed(Auction auction) {
        if (auction.getWinnerUserId() == null) {
            return true;
        }
        return auction.getReservePrice() != null
            && auction.getCurrentPrice().compareTo(auction.getReservePrice()) < 0;
    }

    ///method này cực kì cẩn thận là phải implement sao cho nó IDEMPOTENT
    public void syncAuctionStatus(long auctionId) throws SQLException {
        try (Connection connection = connectionProvider.getConnection()) {
            connection.setAutoCommit(false);

            try {
                AuctionRepository auctionRepository = repositoryFactory.createAuctionRepository(connection);
                auctionRepository.lockAuctionForUpdate(auctionId).orElseThrow(); //////lock auction first
                LocalDateTime dbNow = auctionRepository.getCurrentDatabaseTime();
                AuctionTransition transition = auctionRepository.syncStatus(auctionId, dbNow); 
                Auction auction = auctionRepository.findById(auctionId).orElseThrow();
                switch (transition) {
                    case NO_CHANGE, STARTED -> {}
                    case ENDED -> { ///ended chỉ check xem auction có transition không, còn cập nhật trạng thái auction thì làm ở đây
                        ///không thỏa mãn reserve price
                        if (checkIfAuctionFailed(auction)) {
                            auctionRepository.terminateAuction(auctionId); ///failed
                        } else if (auctionRepository.finalizeAuction(auctionId) == 0L) {
                            auctionRepository.terminateAuction(auctionId);
                        }
                    }
                }
                connection.commit();
                if (transition != AuctionTransition.NO_CHANGE) {
                    AuctionListItemResult item = auctionRepository.findAuctionListItemById(auctionId).orElseThrow();
                    domainEventPublisher.publish(new AuctionListItemUpdatedDomainEvent(
                        item,
                        AuctionListItemUpdateReason.STATUS_CHANGED,
                        java.time.LocalDateTime.now()
                    ));
                }
            } catch (SQLException | RuntimeException exception) {
                connection.rollback();
                throw exception;
            }
        }
    }

    public List<Auction> findPendingSchedules() throws SQLException {
        try (Connection connection = connectionProvider.getConnection()) {
            AuctionRepository auctionRepository = repositoryFactory.createAuctionRepository(connection);
            return auctionRepository.findPendingSchedules();
        }
    }

    public List<Auction> findRecoverableSchedules() throws SQLException {
        try (Connection connection = connectionProvider.getConnection()) {
            AuctionRepository auctionRepository = repositoryFactory.createAuctionRepository(connection);
            LocalDateTime dbNow = auctionRepository.getCurrentDatabaseTime();
            return auctionRepository.findRecoverableSchedules(dbNow);
        }
    }

    public Auction findAuctionById(long auctionId) throws SQLException {
        try (Connection connection = connectionProvider.getConnection()) {
            AuctionRepository auctionRepository = repositoryFactory.createAuctionRepository(connection);
            Optional<Auction> auction = auctionRepository.findById(auctionId);
            return auction.orElse(null);
        }
    }

     private String normalizeBlank(String value) {
        return value == null || value.isBlank() ? null : value;
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


}

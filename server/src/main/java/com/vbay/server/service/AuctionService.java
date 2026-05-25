package com.vbay.server.service;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.logging.Logger;

import com.vbay.server.databaseManager.ConnectionProvider;
import com.vbay.server.exception.AuthenticationException;
import com.vbay.server.exception.ValidationException;
import com.vbay.server.mapper.dtomapper.ProductImageMapper;
import com.vbay.server.model.Auction;
import com.vbay.server.model.Autobid;
import com.vbay.server.model.Bid;
import com.vbay.server.model.Payment;
import com.vbay.server.model.Product;
import com.vbay.server.model.ProductImage;
import com.vbay.server.network_connection.ClientSession;
import com.vbay.server.realtime.domain.AuctionClosedDomainEvent;
import com.vbay.server.realtime.domain.AuctionListItemUpdatedDomainEvent;
import com.vbay.server.realtime.domain.AuctionStartedDomainEvent;
import com.vbay.server.realtime.domain.UserBalanceUpdatedDomainEvent;
import com.vbay.server.realtime.domain.enums.AuctionCloseReason;
import com.vbay.server.realtime.domain.enums.AuctionListItemUpdateReason;
import com.vbay.server.realtime.publisher.DomainEventPublisher;
import com.vbay.server.repository.AuctionRepository;
import com.vbay.server.repository.AutobidRepository;
import com.vbay.server.repository.BidRepository;
import com.vbay.server.repository.PaymentRepository;
import com.vbay.server.repository.ProductImageRepository;
import com.vbay.server.repository.ProductRepository;
import com.vbay.server.repository.RepositoryFactory;
import com.vbay.server.repository.UserRepository;
import com.vbay.server.repository.enums.AuctionTransition;
import com.vbay.server.service.bid.enums.AutobidStatus;
import com.vbay.server.service.result.AuctionClosedResult;
import com.vbay.server.service.result.AuctionListItemResult;
import com.vbay.server.service.result.AutobidUpdateResult;
import com.vbay.server.service.result.CreateAuctionResult;
import com.vbay.server.service.result.UserBalanceResult;
import com.vbay.server.service.result.UserMyBidListItemResult;
import com.vbay.server.service.result.mapper.ResultMapper;
import com.vbay.server.service.validation.ValidateAuctionDTO;
import com.vbay.shared.Utils.LoggingUtils;
import com.vbay.shared.dto.auctionDTO.AuctionDetailRequest;
import com.vbay.shared.dto.auctionDTO.AuctionListRequest;
import com.vbay.shared.dto.auctionDTO.AuctionListResponse;
import com.vbay.shared.dto.auctionDTO.CreateAuctionRequest;
import com.vbay.shared.dto.productDTO.CreateProductRequest;
import com.vbay.shared.dto.realtimeDTO.payload.AuctionItemPayload;
import com.vbay.shared.dto.realtimeDTO.payload.AuctionListItemPayload;
import com.vbay.shared.dto.realtimeDTO.payload.ViewerAuctionBidStatePayload;
import com.vbay.shared.dto.realtimeDTO.payload.ViewerAuctionBidSummaryPayload;
import com.vbay.shared.enums.auction.AuctionStatus;
import com.vbay.shared.enums.bid.BidStatus;
import com.vbay.shared.enums.payment.PaymentStatus;
import com.vbay.shared.enums.payment.PaymentType;

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
* 10. CHO PHÉP thằng đang thắng được bid thêm
* 11. KHÔNG CHO PHÉP AutoBid tự động bid thêm (chỉ thêm khi user bị OUTBID)

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
    private static final Logger LOGGER = LoggingUtils.getLogger(AuctionService.class);
    
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
        productImageRepository.saveAll(product.getId(), Images);
        LOGGER.info(() -> "Product created: " + product.getId());
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
                
                LocalDateTime dbNow = auctionRepository.getCurrentDatabaseTime();
                if (!request.getStartingTime().isAfter(dbNow)) {
                    throw new ValidationException("Starting time must be in the future");
                }
                // 1. tạo Product object
                Product product = createProduct(request.getProduct(), session, productRepository, productImageRepository);
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
                LOGGER.info(() -> "Auction created: " + auction.getId());

                CreateAuctionResult result = ResultMapper.toCreateAuctionResult(auction, product);
                AuctionListItemResult item = auctionRepository.findAuctionListItemById(auction.getId()).orElseThrow();
                domainEventPublisher.publish(new AuctionListItemUpdatedDomainEvent(
                    item,
                    AuctionListItemUpdateReason.CREATED,
                    dbNow
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
            AutobidRepository autobidRepository = repositoryFactory.createAutobidRepository(connection);
            List<AuctionListItemPayload> items = auctionRepository.findAuctionList(request).stream()
                .map(ResultMapper::toAuctionListItemPayload)
                .toList();
            Map<Long, Autobid> viewerAutobids = autobidRepository.findByAuctionIdsAndUserId(
                items.stream().map(AuctionListItemPayload::getAuctionId).toList(),
                session.getUserId()
            );

            for (AuctionListItemPayload item : items) {
                item.setViewerBidState(buildViewerAuctionBidSummary(
                    item.getAuctionId(),
                    item.getWinnerUserId(),
                    item.getUpdatedAt(),
                    session.getUserId(),
                    viewerAutobids.get(item.getAuctionId())
                ));
            }

            return new AuctionListResponse(
                items
            );
        }
    }

    public AuctionItemPayload getAuctionDetail(AuctionDetailRequest request, ClientSession session) throws SQLException {
        checkSession(session);
        if (request == null || request.getAuctionId() <= 0) {
            throw new ValidationException("Auction detail request is invalid");
        }

        try (Connection connection = connectionProvider.getConnection()) {
            AuctionRepository auctionRepository = repositoryFactory.createAuctionRepository(connection);
            AutobidRepository autobidRepository = repositoryFactory.createAutobidRepository(connection);
            UserRepository userRepository = repositoryFactory.createUserRepository(connection);
            AuctionItemPayload payload = auctionRepository.findAuctionItemById(request.getAuctionId())
                .map(ResultMapper::toAuctionItemPayload)
                .orElseThrow(() -> new ValidationException("Auction not found"));

            userRepository.findById(payload.getSellerId()).ifPresent(user -> {
                payload.setSellerUsername(user.getUserName());
                payload.setSellerEmail(user.getEmail());
            });

            payload.setViewerBidState(buildViewerAuctionBidState(
                payload.getAuctionId(),
                payload.getWinnerUserId(),
                payload.getUpdatedAt(),
                session.getUserId(),
                autobidRepository
            ));
            return payload;
        }
    }

    private ViewerAuctionBidStatePayload buildViewerAuctionBidState(
            long auctionId,
            Long winnerUserId,
            LocalDateTime fallbackUpdatedAt,
            long viewerUserId,
            AutobidRepository autobidRepository) throws SQLException {
        Optional<Autobid> viewerAutobid =
            autobidRepository.findByAuctionIdAndUserId(auctionId, viewerUserId);
        boolean winning = winnerUserId != null && winnerUserId == viewerUserId;

        if (viewerAutobid.isEmpty()) {
            return new ViewerAuctionBidStatePayload(
                auctionId,
                viewerUserId,
                null,
                null,
                null,
                winning,
                false,
                fallbackUpdatedAt
            );
        }

        Autobid autobid = viewerAutobid.get();
        boolean showActiveMaxBid = autobid.getStatus() == AutobidStatus.WINNING && winning;
        return new ViewerAuctionBidStatePayload(
            auctionId,
            viewerUserId,
            autobid.getId(),
            autobid.getMaxBidAmount(),
            autobid.getStatus().name(),
            winning,
            showActiveMaxBid,
            autobid.getUpdatedAt()
        );
    }

    private ViewerAuctionBidSummaryPayload buildViewerAuctionBidSummary(
            long auctionId,
            Long winnerUserId,
            LocalDateTime fallbackUpdatedAt,
            long viewerUserId,
            Autobid viewerAutobid) {
        boolean winning = winnerUserId != null && winnerUserId == viewerUserId;

        if (viewerAutobid == null) {
            return new ViewerAuctionBidSummaryPayload(
                auctionId,
                viewerUserId,
                false,
                null,
                winning,
                false,
                fallbackUpdatedAt
            );
        }

        boolean showActiveMaxBid = viewerAutobid.getStatus() == AutobidStatus.WINNING && winning;
        return new ViewerAuctionBidSummaryPayload(
            auctionId,
            viewerUserId,
            true,
            viewerAutobid.getStatus().name(),
            winning,
            showActiveMaxBid,
            viewerAutobid.getUpdatedAt()
        );
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

    public void syncAuctionStatus(long auctionId) throws SQLException {
        AuctionClosedDomainEvent auctionClosedEvent = null;
        AuctionStartedDomainEvent auctionStartedEvent = null;
        AuctionListItemUpdatedDomainEvent auctionListEvent = null;
        List<UserBalanceUpdatedDomainEvent> userBalanceEvents = List.of();

        try (Connection connection = connectionProvider.getConnection()) {
            connection.setAutoCommit(false);

            boolean statusChanged = false;

            try {
                AuctionRepository auctionRepository = repositoryFactory.createAuctionRepository(connection);
                BidRepository bidRepository = repositoryFactory.createBidRepository(connection);    
                AutobidRepository autobidRepository = repositoryFactory.createAutobidRepository(connection);
                ProductImageRepository productImageRepository = repositoryFactory.createProductImageRepository(connection);
                UserRepository userRepository = repositoryFactory.createUserRepository(connection);
                PaymentRepository paymentRepository = repositoryFactory.createPaymentRepository(connection);
                
                auctionRepository.lockAuctionForUpdate(auctionId).orElseThrow(); //////lock auction first
                LocalDateTime dbNow = auctionRepository.getCurrentDatabaseTime();
                AuctionTransition transition = auctionRepository.syncStatus(auctionId, dbNow);
                Auction auction = auctionRepository.findById(auctionId).orElseThrow();

                switch (transition) {
                    case NO_CHANGE -> {}
                    case STARTED -> statusChanged = true;
                    case TIME_EXPIRED -> { ///ended chỉ check xem auction có transition không, còn cập nhật trạng thái auction thì làm ở đây
                        ///không thỏa mãn reserve price
                        AuctionClosedResult closeResult = closeExpiredAuction(
                            auction,
                            dbNow,
                            auctionRepository,
                            bidRepository,
                            autobidRepository,
                            productImageRepository,
                            userRepository,
                            paymentRepository
                        );
                        
                        if (closeResult != null) {
                            auctionClosedEvent = new AuctionClosedDomainEvent(closeResult);
                            userBalanceEvents = closeResult.getAffectedBalanceResults().stream()
                                .map(result -> new UserBalanceUpdatedDomainEvent(result, result.getUpdatedAt()))
                                .toList();
                            statusChanged = true;
                        }
                    }
                }
                if (statusChanged) {
                    AuctionListItemResult item = auctionRepository.findAuctionListItemById(auctionId).orElseThrow();
                    if (transition == AuctionTransition.STARTED) {
                        auctionStartedEvent = new AuctionStartedDomainEvent(item, dbNow);
                    }
                    auctionListEvent = new AuctionListItemUpdatedDomainEvent(
                        item,
                        AuctionListItemUpdateReason.STATUS_CHANGED,
                        dbNow
                    );
                }
                connection.commit();
            } catch (SQLException | RuntimeException exception) {
                connection.rollback();
                throw exception;
            }
        }

        if (auctionListEvent != null) {
            domainEventPublisher.publish(auctionListEvent);
        }
        if (auctionStartedEvent != null) {
            domainEventPublisher.publish(auctionStartedEvent);
        }
        if (auctionClosedEvent != null) {
            domainEventPublisher.publish(auctionClosedEvent);
        }
        for (UserBalanceUpdatedDomainEvent event : userBalanceEvents) {
            domainEventPublisher.publish(event);
        }
    }

    private AuctionClosedResult closeExpiredAuction(
            Auction auction,
            LocalDateTime closedAt,
            AuctionRepository auctionRepository,
            BidRepository bidRepository,
            AutobidRepository autobidRepository,
            ProductImageRepository productImageRepository,
            UserRepository userRepository,
            PaymentRepository paymentRepository) throws SQLException {
        Optional<Autobid> winningAutobid = autobidRepository.findWinningByAuctionId(auction.getId());
        if (checkIfAuctionFailed(auction)) {
            Bid bidToRelease = bidRepository.findWinningBidByAuctionId(auction.getId()).orElse(null);
            long version = auctionRepository.terminateAuction(auction.getId());
            if (version == 0L) {
                return null;
            }
            bidRepository.markAuctionBidsLost(auction.getId());
            Auction refreshedAuction = auctionRepository.findById(auction.getId()).orElseThrow();
            List<UserBalanceResult> affectedBalances = releaseClosedAuctionHold(
                bidToRelease,
                winningAutobid.orElse(null),
                userRepository,
                closedAt,
                "AUCTION_FAILED_RELEASE"
            );
            Optional<AutobidUpdateResult> affectedAutobid =
                endActiveAutobidsForClosedAuction(refreshedAuction, winningAutobid, autobidRepository, closedAt);
            return buildAuctionClosedResult(
                refreshedAuction,
                AuctionCloseReason.TIME_EXPIRED_FAILED,
                closedAt,
                bidRepository,
                productImageRepository,
                affectedAutobid,
                affectedBalances
            );
        }

        Bid winningBid = bidRepository.findWinningBidByAuctionId(auction.getId())
            .orElseThrow(() -> new ValidationException("Winning bid not found"));
        long version = auctionRepository.finalizeAuction(auction.getId());
        if (version == 0L) {
            return null;
        }

        bidRepository.updateStatusesByAuctionIdExceptBid(auction.getId(), winningBid.getId(), BidStatus.LOST);
        bidRepository.updateStatus(winningBid.getId(), BidStatus.WON);
        Auction refreshedAuction = auctionRepository.findById(auction.getId()).orElseThrow();
        List<UserBalanceResult> affectedBalances = settleClosedAuctionPayment(
            refreshedAuction,
            winningBid,
            winningAutobid.orElse(null),
            userRepository,
            paymentRepository,
            closedAt
        );
        Optional<AutobidUpdateResult> affectedAutobid =
            endActiveAutobidsForClosedAuction(refreshedAuction, winningAutobid, autobidRepository, closedAt);
        return buildAuctionClosedResult(
            refreshedAuction,
            AuctionCloseReason.TIME_EXPIRED_ENDED,
            closedAt,
            bidRepository,
            productImageRepository,
            affectedAutobid,
            affectedBalances
        );
    }

    private List<UserBalanceResult> settleClosedAuctionPayment(
            Auction auction,
            Bid winningBid,
            Autobid winningAutobid,
            UserRepository userRepository,
            PaymentRepository paymentRepository,
                LocalDateTime closedAt) throws SQLException {
        List<UserBalanceResult> results = releaseClosedAuctionHold(
            winningBid,
            winningAutobid,
            userRepository,
            closedAt,
            "AUCTION_WON_HOLD_RELEASE"
        );

        BigDecimal finalPrice = auction.getCurrentPrice();
        userRepository.decreaseAvailableBalance(winningBid.getBidderId(), finalPrice);
        
        userRepository.depositAvailableBalance(auction.getSellerId(), finalPrice);

        results.add(readUserBalanceResult(
            userRepository,
            winningBid.getBidderId(),
            "AUCTION_WIN_PAYMENT",
            closedAt
        ));

        results.add(readUserBalanceResult(
            userRepository,
            auction.getSellerId(),
            "AUCTION_SOLD_RECEIPT",
            closedAt
        ));
        return results;
    }

    private List<UserBalanceResult> releaseClosedAuctionHold(
            Bid winningBid,
            Autobid winningAutobid,
            UserRepository userRepository,
            LocalDateTime closedAt,
            String reason) throws SQLException {
        if (winningBid == null) {
            return new ArrayList<>();
        }

        BigDecimal holdAmount = winningBid.getBidAmount();
        if (winningAutobid != null && winningAutobid.getUserId() == winningBid.getBidderId()) {
            holdAmount = winningAutobid.getMaxBidAmount();
        }

        userRepository.releaseHoldBalance(winningBid.getBidderId(), holdAmount);
        List<UserBalanceResult> results = new ArrayList<>();
        results.add(readUserBalanceResult(
            userRepository,
            winningBid.getBidderId(),
            reason,
            closedAt
        ));
        return results;
    }

    private Optional<AutobidUpdateResult> endActiveAutobidsForClosedAuction(
            Auction auction,
            Optional<Autobid> winningAutobid,
            AutobidRepository autobidRepository,
            LocalDateTime closedAt) throws SQLException {
        if (winningAutobid.isEmpty()) {
            return Optional.empty();
        }

        Autobid autobid = winningAutobid.get();
        AutobidStatus finalStatus = auction.getWinnerUserId() != null
                && auction.getWinnerUserId() == autobid.getUserId()
            ? AutobidStatus.WON
            : AutobidStatus.LOST;

        autobidRepository.updateStatus(autobid.getId(), finalStatus);
        return Optional.of(toAutobidUpdateResult(
            auction,
            autobid,
            finalStatus,
            closedAt
        ));
    }

    private AuctionClosedResult buildAuctionClosedResult(
            Auction auction,
            AuctionCloseReason reason,
            LocalDateTime closedAt,
            BidRepository bidRepository,
            ProductImageRepository productImageRepository,
            Optional<AutobidUpdateResult> affectedAutobid,
            List<UserBalanceResult> affectedBalanceResults) throws SQLException {
        String thumbnailUrl = productImageRepository.findThumbnailUrlByProductId(auction.getProductId()).orElse(null);
        List<UserMyBidListItemResult> affectedMyBidItems = new ArrayList<>();
        for (Bid bid : bidRepository.findLatestBidPerBidderByAuctionId(auction.getId())) {
            affectedMyBidItems.add(ResultMapper.toUserMyBidListItemResult(
                auction,
                thumbnailUrl,
                bid,
                bid.getStatus(),
                closedAt
            ));
        }

        return new AuctionClosedResult(
            auction.getId(),
            auction.getVersion(),
            auction.getStatus(),
            auction.getCurrentPrice(),
            reserveMet(auction),
            auction.getAntiSnipeExtensionCount() > 0,
            auction.getWinnerUserId(),
            auction.getStartingTime(),
            auction.getEndingTime(),
            reason,
            closedAt,
            affectedMyBidItems,
            affectedAutobid.map(List::of).orElseGet(List::of),
            affectedBalanceResults
        );
    }

    private UserBalanceResult readUserBalanceResult(
            UserRepository userRepository,
            long userId,
            String reason,
            LocalDateTime updatedAt) throws SQLException {
        return userRepository.findById(userId)
            .map(user -> ResultMapper.toUserBalanceResult(user, reason, updatedAt))
            .orElseThrow(() -> new ValidationException("User not found"));
    }

    private AutobidUpdateResult toAutobidUpdateResult(
            Auction auction,
            Autobid autobid,
            AutobidStatus status,
            LocalDateTime updatedAt) {
        boolean showActiveMaxBid = status == AutobidStatus.WINNING
            && auction.getWinnerUserId() != null
            && auction.getWinnerUserId() == autobid.getUserId();

        return new AutobidUpdateResult(
            auction.getId(),
            autobid.getUserId(),
            autobid.getId(),
            autobid.getMaxBidAmount(),
            status,
            showActiveMaxBid,
            showActiveMaxBid,
            updatedAt
        );
    }

    private Boolean reserveMet(Auction auction) {
        if (auction.getReservePrice() == null) {
            return null;
        }
        return auction.getCurrentPrice().compareTo(auction.getReservePrice()) >= 0;
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

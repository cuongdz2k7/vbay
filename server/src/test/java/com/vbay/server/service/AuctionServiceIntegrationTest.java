package com.vbay.server.service;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import org.junit.jupiter.api.AfterEach;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.vbay.server.exception.AuthenticationException;
import com.vbay.server.exception.ValidationException;
import com.vbay.server.model.Product;
import com.vbay.server.network_connection.ClientSession;
import com.vbay.server.repository.JDBCrepository.JdbcAuctionRepository;
import com.vbay.server.repository.JDBCrepository.JdbcProductImageRepository;
import com.vbay.server.repository.JDBCrepository.JdbcProductRepository;
import com.vbay.server.repository.JDBCrepository.JdbcRepositoryFactory;
import com.vbay.server.realtime.publisher.DomainEventPublisher;
import com.vbay.shared.dto.auctionDTO.CreateAuctionRequest;
import com.vbay.shared.dto.auctionDTO.BuyNowRequest;
import com.vbay.shared.dto.auctionDTO.PlaceBidRequest;
import com.vbay.shared.dto.productDTO.CreateProductRequest;
import com.vbay.shared.dto.productDTO.ProductImageDTO;
import com.vbay.shared.enums.auction.AuctionStatus;
import com.vbay.shared.enums.auth.Position;
import com.vbay.shared.enums.auth.UserStatus;
import com.vbay.shared.enums.bid.BidSource;
import com.vbay.shared.enums.bid.BidStatus;
import com.vbay.shared.enums.payment.PaymentStatus;
import com.vbay.shared.enums.payment.PaymentType;
import com.vbay.shared.enums.product.ProductStatus;
import com.vbay.server.model.Auction;
import com.vbay.server.service.bid.enums.AutobidStatus;
import com.vbay.server.service.bid.BuyNowService;
import com.vbay.server.service.bid.ManualBidService;
import com.vbay.server.service.bid.engine.AntiSnipePolicy;
import com.vbay.server.service.bid.engine.AuctionBidEngine;
import com.vbay.server.service.bid.resolution.BidResolutionApplier;
import com.vbay.server.service.result.BuyNowResult;
import com.vbay.server.service.result.UserMyBidListItemResult;

class AuctionServiceIntegrationTest {
    private static final long SELLER_ID = 1L;
    private static final DomainEventPublisher NO_OP_PUBLISHER = event -> { };

    private String jdbcUrl;
    private Connection keepAliveConnection;
    private AuctionService auctionService;
    private ManualBidService bidService;
    private BuyNowService buyNowService;
    private ClientSession session;

    @BeforeEach
    void setUp() throws SQLException {
        jdbcUrl = "jdbc:h2:mem:" + UUID.randomUUID() + ";MODE=MySQL;DATABASE_TO_UPPER=false;DB_CLOSE_DELAY=-1";
        keepAliveConnection = DriverManager.getConnection(jdbcUrl);
        createSchema(keepAliveConnection);
        auctionService = new AuctionService(
            () -> DriverManager.getConnection(jdbcUrl),
            new JdbcRepositoryFactory(),
            NO_OP_PUBLISHER
        );
        bidService = new ManualBidService(
            () -> DriverManager.getConnection(jdbcUrl),
            new JdbcRepositoryFactory(),
            NO_OP_PUBLISHER
        );
        buyNowService = new BuyNowService(
            () -> DriverManager.getConnection(jdbcUrl),
            new JdbcRepositoryFactory(),
            NO_OP_PUBLISHER,
            new AuctionBidEngine(new AntiSnipePolicy()),
            new BidResolutionApplier(new JdbcRepositoryFactory())
        );
        session = new ClientSession();
        session.setSession(SELLER_ID, "seller", Position.USER);
    }

    @AfterEach
    void tearDown() throws SQLException {
        if (keepAliveConnection != null) {
            keepAliveConnection.close();
        }
    }

    @Test
    void createProduct_persistsProductAndImagesCorrectly() throws SQLException {
        seedUser(SELLER_ID);

        try (Connection connection = DriverManager.getConnection(jdbcUrl)) {
            JdbcProductRepository productRepository = new JdbcProductRepository(connection);
            JdbcProductImageRepository imageRepository = new JdbcProductImageRepository(connection);

            long productId = auctionService.createProduct(validProduct(), session, productRepository, imageRepository).getId();

            Product savedProduct = productRepository.findById(productId).orElseThrow();
            assertEquals(SELLER_ID, savedProduct.getSellerId());
            assertEquals("iPhone 15", savedProduct.getName());
            assertEquals("Good condition", savedProduct.getDescription());
            assertEquals(1L, savedProduct.getCategoryId());
            assertEquals("USED", savedProduct.getCondition());
            assertEquals(ProductStatus.AVAILABLE, savedProduct.getStatus());
            assertNotNull(savedProduct.getCreatedAt());
            assertNotNull(savedProduct.getUpdatedAt());
            assertEquals(2, savedProduct.getImages().size());
            assertEquals("https://example.com/iphone-front.jpg", savedProduct.getImages().get(0).getImageUrl());
            assertEquals(true, savedProduct.getImages().get(0).isThumbnail());
        }
    }

    @Test
    void createAuction_whenProductInsertFails_rollsBackEverything() throws SQLException {
        assertThrows(SQLException.class, () -> auctionService.createAuction(validRequest(), session));

        assertTableCounts(0, 0, 0);
    }

    @Test
    void createAuction_whenImageInsertFails_rollsBackProductAndDoesNotCreateAuction() throws SQLException {
        seedUser(SELLER_ID);
        CreateProductRequest productWithInvalidImage = new CreateProductRequest(
            "iPhone 15",
            List.of(new ProductImageDTO("x".repeat(300), true)),
            "Good condition",
            "USED",
            1L
        );

        assertThrows(SQLException.class, () -> auctionService.createAuction(validRequest(productWithInvalidImage), session));

        assertTableCounts(0, 0, 0);
    }

    @Test
    void createAuction_whenAuctionInsertFails_rollsBackProductAndImages() throws SQLException {
        seedUser(SELLER_ID);
        CreateAuctionRequest request = new CreateAuctionRequest(
            validProduct(),
            "iPhone 15 auction",
            "Auction description",
            new BigDecimal("1000000000000000.00"),
            null,
            null,
            new BigDecimal("10.00"),
            LocalDateTime.now().plusHours(1),
            LocalDateTime.now().plusHours(2)
        );

        assertThrows(SQLException.class, () -> auctionService.createAuction(request, session));

        assertTableCounts(0, 0, 0);
    }

    @Test
    void createAuction_validRequest_persistsProductImagesAndAuctionCorrectly() throws SQLException {
        seedUser(SELLER_ID);

        auctionService.createAuction(validRequest(), session);

        assertTableCounts(1, 2, 1);
        try (Connection connection = DriverManager.getConnection(jdbcUrl)) {
            Product savedProduct = new JdbcProductRepository(connection).findBySellerId(SELLER_ID).get(0);
            assertEquals(ProductStatus.AVAILABLE, savedProduct.getStatus());
            assertEquals("iPhone 15", savedProduct.getName());
            assertEquals(2, savedProduct.getImages().size());

            var savedAuction = new JdbcAuctionRepository(connection).findByProductId(savedProduct.getId()).orElseThrow();
            assertEquals(SELLER_ID, savedAuction.getSellerId());
            assertEquals("iPhone 15 auction", savedAuction.getTitle());
            assertEquals(new BigDecimal("100.00"), savedAuction.getStartPrice());
            assertEquals(new BigDecimal("100.00"), savedAuction.getCurrentPrice());
            assertEquals(AuctionStatus.SCHEDULED, savedAuction.getStatus());
        }
    }

    @Test
    void placeBid_validNormalBid_holdsBuyerBalanceAndCreatesWinningBid() throws SQLException {
        seedUser(SELLER_ID);
        seedUser(2L, "bidder", UserStatus.ACTIVE, new BigDecimal("1000.00"), BigDecimal.ZERO);
        long auctionId = seedAuction(AuctionStatus.ACTIVE, new BigDecimal("100.00"), new BigDecimal("10.00"), new BigDecimal("200.00"));

        bidService.placeBid(new PlaceBidRequest(auctionId, new BigDecimal("120.00")), sessionFor(2L, "bidder"));

        assertEquals(1, countRows(keepAliveConnection, "bids"));
        assertDecimal("880.00", scalarDecimal("SELECT available_balance FROM users WHERE id = 2"));
        assertDecimal("120.00", scalarDecimal("SELECT hold_balance FROM users WHERE id = 2"));
        assertDecimal("120.00", scalarDecimal("SELECT current_price FROM auctions WHERE id = " + auctionId));
        assertEquals(2L, scalarLong("SELECT winner_user_id FROM auctions WHERE id = " + auctionId));
        assertEquals(BidStatus.WINNING.name(), scalarString("SELECT status FROM bids WHERE auction_id = " + auctionId));
    }

    @Test
    void placeBid_whenExistingWinner_outbidsOldBidAndReleasesOldHold() throws SQLException {
        seedUser(SELLER_ID);
        seedUser(2L, "oldwinner", UserStatus.ACTIVE, new BigDecimal("500.00"), new BigDecimal("120.00"));
        seedUser(3L, "newwinner", UserStatus.ACTIVE, new BigDecimal("1000.00"), BigDecimal.ZERO);
        long auctionId = seedAuction(AuctionStatus.ACTIVE, new BigDecimal("120.00"), new BigDecimal("10.00"), new BigDecimal("250.00"));
        updateAuctionWinner(auctionId, 2L);
        long oldBidId = seedBid(auctionId, 2L, new BigDecimal("120.00"), BidStatus.WINNING);

        bidService.placeBid(new PlaceBidRequest(auctionId, new BigDecimal("150.00")), sessionFor(3L, "newwinner"));

        assertEquals(BidStatus.OUTBID.name(), scalarString("SELECT status FROM bids WHERE id = " + oldBidId));
        assertEquals(BidStatus.WINNING.name(), scalarString("SELECT status FROM bids WHERE bidder_id = 3"));
        assertDecimal("620.00", scalarDecimal("SELECT available_balance FROM users WHERE id = 2"));
        assertDecimal("0.00", scalarDecimal("SELECT hold_balance FROM users WHERE id = 2"));
        assertDecimal("850.00", scalarDecimal("SELECT available_balance FROM users WHERE id = 3"));
        assertDecimal("150.00", scalarDecimal("SELECT hold_balance FROM users WHERE id = 3"));
        assertEquals(3L, scalarLong("SELECT winner_user_id FROM auctions WHERE id = " + auctionId));
    }

    @Test
    void placeBid_whenAuctionNotFound_rollsBack() {
        seedUserUnchecked(2L, "bidder", UserStatus.ACTIVE, new BigDecimal("1000.00"), BigDecimal.ZERO);

        assertThrows(ValidationException.class,
            () -> bidService.placeBid(new PlaceBidRequest(999L, new BigDecimal("120.00")), sessionFor(2L, "bidder")));
    }

    @Test
    void placeBid_whenAuctionScheduled_rejectsAndDoesNotHoldBalance() throws SQLException {
        seedUser(SELLER_ID);
        seedUser(2L, "bidder", UserStatus.ACTIVE, new BigDecimal("1000.00"), BigDecimal.ZERO);
        long auctionId = seedAuction(
            AuctionStatus.SCHEDULED,
            new BigDecimal("100.00"),
            new BigDecimal("10.00"),
            new BigDecimal("200.00"),
            LocalDateTime.now().plusHours(1),
            LocalDateTime.now().plusHours(2)
        );

        assertThrows(ValidationException.class,
            () -> bidService.placeBid(new PlaceBidRequest(auctionId, new BigDecimal("120.00")), sessionFor(2L, "bidder")));

        assertEquals(0, countRows(keepAliveConnection, "bids"));
        assertDecimal("1000.00", scalarDecimal("SELECT available_balance FROM users WHERE id = 2"));
        assertDecimal("0.00", scalarDecimal("SELECT hold_balance FROM users WHERE id = 2"));
    }

    @Test
    void placeBid_whenActiveAuctionExpired_rejectsWithoutCreatingBid() throws SQLException {
        seedUser(SELLER_ID);
        seedUser(2L, "bidder", UserStatus.ACTIVE, new BigDecimal("1000.00"), BigDecimal.ZERO);
        long auctionId = seedAuction(
            AuctionStatus.ACTIVE,
            new BigDecimal("100.00"),
            new BigDecimal("10.00"),
            new BigDecimal("200.00"),
            LocalDateTime.now().minusHours(2),
            LocalDateTime.now().minusHours(1)
        );

        assertThrows(ValidationException.class,
            () -> bidService.placeBid(new PlaceBidRequest(auctionId, new BigDecimal("120.00")), sessionFor(2L, "bidder")));

        assertEquals(AuctionStatus.ACTIVE.name(), scalarString("SELECT status FROM auctions WHERE id = " + auctionId));
        assertEquals(0, countRows(keepAliveConnection, "bids"));
    }

    @Test
    void placeBid_whenSellerBidsOwnAuction_rejects() throws SQLException {
        seedUser(SELLER_ID);
        long auctionId = seedAuction(AuctionStatus.ACTIVE, new BigDecimal("100.00"), new BigDecimal("10.00"), new BigDecimal("200.00"));

        assertThrows(ValidationException.class,
            () -> bidService.placeBid(new PlaceBidRequest(auctionId, new BigDecimal("120.00")), session));
    }

    @Test
    void placeBid_whenBidBelowMinimum_rejectsWithoutHoldingBalance() throws SQLException {
        seedUser(SELLER_ID);
        seedUser(2L, "bidder", UserStatus.ACTIVE, new BigDecimal("1000.00"), BigDecimal.ZERO);
        long auctionId = seedAuction(AuctionStatus.ACTIVE, new BigDecimal("100.00"), new BigDecimal("10.00"), new BigDecimal("200.00"));

        assertThrows(ValidationException.class,
            () -> bidService.placeBid(new PlaceBidRequest(auctionId, new BigDecimal("99.99")), sessionFor(2L, "bidder")));

        assertEquals(0, countRows(keepAliveConnection, "bids"));
        assertDecimal("1000.00", scalarDecimal("SELECT available_balance FROM users WHERE id = 2"));
        assertDecimal("0.00", scalarDecimal("SELECT hold_balance FROM users WHERE id = 2"));
    }

    @Test
    void placeBid_whenUserCannotBid_rejectsEachBlockedStatus() throws SQLException {
        seedUser(SELLER_ID);
        long suspendedAuction = seedAuction(AuctionStatus.ACTIVE, new BigDecimal("100.00"), new BigDecimal("10.00"), new BigDecimal("200.00"));
        assertBlockedUserCannotBid(2L, "suspended", UserStatus.SUSPENDED, suspendedAuction);

        long bannedAuction = seedAuction(AuctionStatus.ACTIVE, new BigDecimal("100.00"), new BigDecimal("10.00"), new BigDecimal("200.00"));
        assertBlockedUserCannotBid(3L, "banned", UserStatus.BANNED, bannedAuction);

        long deletedAuction = seedAuction(AuctionStatus.ACTIVE, new BigDecimal("100.00"), new BigDecimal("10.00"), new BigDecimal("200.00"));
        assertBlockedUserCannotBid(4L, "deleted", UserStatus.DELETED, deletedAuction);
    }

    @Test
    void placeBid_whenInsufficientBalance_rejectsWithoutPartialChanges() throws SQLException {
        seedUser(SELLER_ID);
        seedUser(2L, "bidder", UserStatus.ACTIVE, new BigDecimal("119.99"), BigDecimal.ZERO);
        long auctionId = seedAuction(AuctionStatus.ACTIVE, new BigDecimal("100.00"), new BigDecimal("10.00"), new BigDecimal("200.00"));

        assertThrows(ValidationException.class,
            () -> bidService.placeBid(new PlaceBidRequest(auctionId, new BigDecimal("120.00")), sessionFor(2L, "bidder")));

        assertEquals(0, countRows(keepAliveConnection, "bids"));
        assertDecimal("119.99", scalarDecimal("SELECT available_balance FROM users WHERE id = 2"));
        assertDecimal("0.00", scalarDecimal("SELECT hold_balance FROM users WHERE id = 2"));
    }

    @Test
    void placeBid_whenBidHigherThanBuyNow_rejects() throws SQLException {
        seedUser(SELLER_ID);
        seedUser(2L, "bidder", UserStatus.ACTIVE, new BigDecimal("1000.00"), BigDecimal.ZERO);
        long auctionId = seedAuction(AuctionStatus.ACTIVE, new BigDecimal("100.00"), new BigDecimal("10.00"), new BigDecimal("200.00"));

        assertThrows(ValidationException.class,
            () -> bidService.placeBid(new PlaceBidRequest(auctionId, new BigDecimal("201.00")), sessionFor(2L, "bidder")));

        assertEquals(0, countRows(keepAliveConnection, "bids"));
    }

    @Test
    void buyNow_completesAuctionAndCreatesHeldPayment() throws SQLException {
        seedUser(SELLER_ID);
        seedUser(2L, "buyer", UserStatus.ACTIVE, new BigDecimal("1000.00"), BigDecimal.ZERO);
        long auctionId = seedAuction(AuctionStatus.ACTIVE, new BigDecimal("100.00"), new BigDecimal("10.00"), new BigDecimal("200.00"));

        buyNowService.buyNow(new BuyNowRequest(auctionId), sessionFor(2L, "buyer"));

        assertEquals(BidStatus.WON.name(), scalarString("SELECT status FROM bids WHERE auction_id = " + auctionId));
        assertEquals(AuctionStatus.ENDED.name(), scalarString("SELECT status FROM auctions WHERE id = " + auctionId));
        assertDecimal("200.00", scalarDecimal("SELECT current_price FROM auctions WHERE id = " + auctionId));
        assertDecimal("200.00", scalarDecimal("SELECT final_price FROM auctions WHERE id = " + auctionId));
        assertEquals(2L, scalarLong("SELECT winner_user_id FROM auctions WHERE id = " + auctionId));
        assertDecimal("800.00", scalarDecimal("SELECT available_balance FROM users WHERE id = 2"));
        assertDecimal("0.00", scalarDecimal("SELECT hold_balance FROM users WHERE id = 2"));
        assertEquals(PaymentStatus.HELD.name(), scalarString("SELECT status FROM payments WHERE auction_id = " + auctionId));
        assertEquals(PaymentType.BUY_NOW.name(), scalarString("SELECT type FROM payments WHERE auction_id = " + auctionId));
        assertDecimal("200.00", scalarDecimal("SELECT amount FROM payments WHERE auction_id = " + auctionId));
    }

    @Test
    void buyNow_withExistingWinner_releasesOldHoldAndCreatesWonBid() throws SQLException {
        seedUser(SELLER_ID);
        seedUser(2L, "oldwinner", UserStatus.ACTIVE, new BigDecimal("500.00"), new BigDecimal("120.00"));
        seedUser(3L, "buyer", UserStatus.ACTIVE, new BigDecimal("1000.00"), BigDecimal.ZERO);
        long auctionId = seedAuction(AuctionStatus.ACTIVE, new BigDecimal("120.00"), new BigDecimal("10.00"), new BigDecimal("200.00"));
        updateAuctionWinner(auctionId, 2L);
        long oldBidId = seedBid(auctionId, 2L, new BigDecimal("120.00"), BidStatus.WINNING);

        buyNowService.buyNow(new BuyNowRequest(auctionId), sessionFor(3L, "buyer"));

        assertEquals(BidStatus.LOST.name(), scalarString("SELECT status FROM bids WHERE id = " + oldBidId));
        assertEquals(BidStatus.WON.name(), scalarString("SELECT status FROM bids WHERE bidder_id = 3"));
        assertDecimal("620.00", scalarDecimal("SELECT available_balance FROM users WHERE id = 2"));
        assertDecimal("0.00", scalarDecimal("SELECT hold_balance FROM users WHERE id = 2"));
        assertDecimal("800.00", scalarDecimal("SELECT available_balance FROM users WHERE id = 3"));
        assertEquals(1, countRows(keepAliveConnection, "payments"));
    }

    @Test
    void buyNow_withExistingBidder_returnsMyBidUpdatesForBuyerAndLoser() throws SQLException {
        seedUser(SELLER_ID);
        seedUser(2L, "oldbidder", UserStatus.ACTIVE, new BigDecimal("500.00"), new BigDecimal("120.00"));
        seedUser(3L, "buyer", UserStatus.ACTIVE, new BigDecimal("1000.00"), BigDecimal.ZERO);
        long auctionId = seedAuction(AuctionStatus.ACTIVE, new BigDecimal("120.00"), new BigDecimal("10.00"), new BigDecimal("200.00"));
        updateAuctionWinner(auctionId, 2L);
        seedBid(auctionId, 2L, new BigDecimal("120.00"), BidStatus.WINNING);

        BuyNowResult result = buyNowService.buyNow(new BuyNowRequest(auctionId), sessionFor(3L, "buyer"));

        assertEquals(2, result.getAffectedMyBidItems().size());
        UserMyBidListItemResult buyerItem = result.getAffectedMyBidItems().stream()
            .filter(item -> item.getUserId() == 3L)
            .findFirst()
            .orElseThrow();
        UserMyBidListItemResult oldBidderItem = result.getAffectedMyBidItems().stream()
            .filter(item -> item.getUserId() == 2L)
            .findFirst()
            .orElseThrow();

        assertEquals(BidStatus.WON, buyerItem.getBidStatus());
        assertDecimal("200.00", buyerItem.getMyBidAmount());
        assertEquals(BidStatus.LOST, oldBidderItem.getBidStatus());
        assertDecimal("120.00", oldBidderItem.getMyBidAmount());
    }

    @Test
    void syncAuctionStatus_whenWinningAutobidEnds_releasesMaxAndChargesFinalPrice() throws SQLException {
        seedUser(SELLER_ID);
        seedUser(2L, "autowinner", UserStatus.ACTIVE, new BigDecimal("700.00"), new BigDecimal("300.00"));
        long auctionId = seedAuction(
            AuctionStatus.ACTIVE,
            new BigDecimal("150.00"),
            new BigDecimal("10.00"),
            new BigDecimal("500.00"),
            LocalDateTime.now().minusHours(2),
            LocalDateTime.now().minusMinutes(1)
        );
        updateAuctionWinner(auctionId, 2L);
        long bidId = seedBid(auctionId, 2L, new BigDecimal("150.00"), BidStatus.WINNING, BidSource.AUTO_BID);
        seedAutobid(auctionId, 2L, new BigDecimal("300.00"), AutobidStatus.WINNING);

        auctionService.syncAuctionStatus(auctionId);

        assertEquals(AuctionStatus.ENDED.name(), scalarString("SELECT status FROM auctions WHERE id = " + auctionId));
        assertDecimal("150.00", scalarDecimal("SELECT final_price FROM auctions WHERE id = " + auctionId));
        assertEquals(BidStatus.WON.name(), scalarString("SELECT status FROM bids WHERE id = " + bidId));
        assertEquals(AutobidStatus.WON.name(), scalarString("SELECT status FROM autobids WHERE auction_id = " + auctionId));
        assertDecimal("850.00", scalarDecimal("SELECT available_balance FROM users WHERE id = 2"));
        assertDecimal("0.00", scalarDecimal("SELECT hold_balance FROM users WHERE id = 2"));
        assertDecimal("150.00", scalarDecimal("SELECT available_balance FROM users WHERE id = " + SELLER_ID));
        assertEquals(0, countRows(keepAliveConnection, "payments"));
    }

    @Test
    void placeBid_withoutAuthenticatedSession_rejectsBeforeTransaction() {
        assertThrows(AuthenticationException.class,
            () -> bidService.placeBid(new PlaceBidRequest(1L, new BigDecimal("120.00")), new ClientSession()));
    }

    @Test
    void placeBid_whenAuctionStopped_rejectsAndDoesNotHoldBalance() throws SQLException {
        seedUser(SELLER_ID);
        seedUser(2L, "bidder", UserStatus.ACTIVE, new BigDecimal("1000.00"), BigDecimal.ZERO);
        long auctionId = seedAuction(AuctionStatus.STOPPED, new BigDecimal("100.00"), new BigDecimal("10.00"), new BigDecimal("200.00"));
        
        assertThrows(ValidationException.class,
            () -> bidService.placeBid(new PlaceBidRequest(auctionId, new BigDecimal("120.00")), sessionFor(2L, "bidder")));
        
        assertDecimal("1000.00", scalarDecimal("SELECT available_balance FROM users WHERE id = 2"));
        assertDecimal("0.00", scalarDecimal("SELECT hold_balance FROM users WHERE id = 2"));
    }

    @Test
    void placeBid_whenSelfOutbidding_replacesOldWinningBidAndAdjustsBalance() throws SQLException {
        seedUser(SELLER_ID);
        seedUser(2L, "bidder", UserStatus.ACTIVE, new BigDecimal("1000.00"), BigDecimal.ZERO);
        long auctionId = seedAuction(AuctionStatus.ACTIVE, new BigDecimal("100.00"), new BigDecimal("10.00"), new BigDecimal("200.00"));
        
        // Place first bid ($120)
        bidService.placeBid(new PlaceBidRequest(auctionId, new BigDecimal("120.00")), sessionFor(2L, "bidder"));
        assertDecimal("880.00", scalarDecimal("SELECT available_balance FROM users WHERE id = 2"));
        assertDecimal("120.00", scalarDecimal("SELECT hold_balance FROM users WHERE id = 2"));
        
        // Place second bid ($150) (self-outbid)
        bidService.placeBid(new PlaceBidRequest(auctionId, new BigDecimal("150.00")), sessionFor(2L, "bidder"));
        assertDecimal("850.00", scalarDecimal("SELECT available_balance FROM users WHERE id = 2"));
        assertDecimal("150.00", scalarDecimal("SELECT hold_balance FROM users WHERE id = 2"));
    }

    private CreateProductRequest validProduct() {
        return new CreateProductRequest(
            "iPhone 15",
            List.of(
                new ProductImageDTO("https://example.com/iphone-front.jpg", true),
                new ProductImageDTO("https://example.com/iphone-back.jpg", false)
            ),
            "Good condition",
            "USED",
            1L
        );
    }

    private CreateAuctionRequest validRequest() {
        return validRequest(validProduct());
    }

    private CreateAuctionRequest validRequest(CreateProductRequest product) {
        return new CreateAuctionRequest(
            product,
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

    private void seedUser(long userId) throws SQLException {
        seedUser(userId, "seller", UserStatus.ACTIVE, BigDecimal.ZERO, BigDecimal.ZERO);
    }

    private void seedUserUnchecked(long userId, String username, UserStatus status, BigDecimal availableBalance, BigDecimal holdBalance) {
        try {
            seedUser(userId, username, status, availableBalance, holdBalance);
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    private void seedUser(long userId, String username, UserStatus status, BigDecimal availableBalance, BigDecimal holdBalance) throws SQLException {
        String sql = """
            INSERT INTO users (id, username, email, password_hash, position, status, available_balance, hold_balance)
            VALUES (?, ?, ?, 'hash', 'USER', ?, ?, ?)
            """;
        try (PreparedStatement statement = keepAliveConnection.prepareStatement(sql)) {
            statement.setLong(1, userId);
            statement.setString(2, username);
            statement.setString(3, username + "@example.com");
            statement.setString(4, status.name());
            statement.setBigDecimal(5, availableBalance);
            statement.setBigDecimal(6, holdBalance);
            statement.executeUpdate();
        }
    }

    private ClientSession sessionFor(long userId, String username) {
        ClientSession clientSession = new ClientSession();
        clientSession.setSession(userId, username, Position.USER);
        return clientSession;
    }

    private long seedAuction(AuctionStatus status, BigDecimal currentPrice, BigDecimal minimumBidStep, BigDecimal buyNowPrice) throws SQLException {
        return seedAuction(
            status,
            currentPrice,
            minimumBidStep,
            buyNowPrice,
            LocalDateTime.now().minusHours(1),
            LocalDateTime.now().plusHours(1)
        );
    }

    private long seedAuction(
        AuctionStatus status,
        BigDecimal currentPrice,
        BigDecimal minimumBidStep,
        BigDecimal buyNowPrice,
        LocalDateTime startingTime,
        LocalDateTime endingTime
    ) throws SQLException {
        long productId;
        String productSql = """
            INSERT INTO products (seller_id, name, description, category_id, product_condition, status)
            VALUES (?, 'Bid product', 'Bid product description', 1, 'USED', 'AVAILABLE')
            """;
        try (PreparedStatement statement = keepAliveConnection.prepareStatement(productSql, Statement.RETURN_GENERATED_KEYS)) {
            statement.setLong(1, SELLER_ID);
            statement.executeUpdate();
            try (ResultSet rs = statement.getGeneratedKeys()) {
                rs.next();
                productId = rs.getLong(1);
            }
        }

        String auctionSql = """
            INSERT INTO auctions (
                product_id, seller_id, title, description, minimum_bid_step,
                starting_price, current_price, reserve_price, buy_now_price,
                starting_time, ending_time, status
            )
            VALUES (?, ?, 'Bid auction', 'Bid auction description', ?, ?, ?, NULL, ?, ?, ?, ?)
            """;
        try (PreparedStatement statement = keepAliveConnection.prepareStatement(auctionSql, Statement.RETURN_GENERATED_KEYS)) {
            statement.setLong(1, productId);
            statement.setLong(2, SELLER_ID);
            statement.setBigDecimal(3, minimumBidStep);
            statement.setBigDecimal(4, new BigDecimal("100.00"));
            statement.setBigDecimal(5, currentPrice);
            statement.setBigDecimal(6, buyNowPrice);
            statement.setObject(7, startingTime);
            statement.setObject(8, endingTime);
            statement.setString(9, status.name());
            statement.executeUpdate();
            try (ResultSet rs = statement.getGeneratedKeys()) {
                rs.next();
                return rs.getLong(1);
            }
        }
    }

    private long seedBid(long auctionId, long bidderId, BigDecimal amount, BidStatus status) throws SQLException {
        return seedBid(auctionId, bidderId, amount, status, BidSource.USER_BID);
    }

    private long seedBid(long auctionId, long bidderId, BigDecimal amount, BidStatus status, BidSource source) throws SQLException {
        String sql = """
            INSERT INTO bids (auction_id, bidder_id, bid_amount, bid_source, status)
            VALUES (?, ?, ?, ?, ?)
            """;
        try (PreparedStatement statement = keepAliveConnection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            statement.setLong(1, auctionId);
            statement.setLong(2, bidderId);
            statement.setBigDecimal(3, amount);
            statement.setString(4, source.name());
            statement.setString(5, status.name());
            statement.executeUpdate();
            try (ResultSet rs = statement.getGeneratedKeys()) {
                rs.next();
                return rs.getLong(1);
            }
        }
    }

    private void seedAutobid(long auctionId, long userId, BigDecimal maxBidAmount, AutobidStatus status) throws SQLException {
        String sql = """
            INSERT INTO autobids (auction_id, user_id, max_bid_amount, status)
            VALUES (?, ?, ?, ?)
            """;
        try (PreparedStatement statement = keepAliveConnection.prepareStatement(sql)) {
            statement.setLong(1, auctionId);
            statement.setLong(2, userId);
            statement.setBigDecimal(3, maxBidAmount);
            statement.setString(4, status.name());
            statement.executeUpdate();
        }
    }

    private void updateAuctionWinner(long auctionId, long winnerUserId) throws SQLException {
        try (PreparedStatement statement = keepAliveConnection.prepareStatement(
            "UPDATE auctions SET winner_user_id = ? WHERE id = ?")) {
            statement.setLong(1, winnerUserId);
            statement.setLong(2, auctionId);
            statement.executeUpdate();
        }
    }

    private void assertBlockedUserCannotBid(long userId, String username, UserStatus status, long auctionId) throws SQLException {
        seedUser(userId, username, status, new BigDecimal("1000.00"), BigDecimal.ZERO);
        assertThrows(ValidationException.class,
            () -> bidService.placeBid(new PlaceBidRequest(auctionId, new BigDecimal("120.00")), sessionFor(userId, username)));
        assertDecimal("1000.00", scalarDecimal("SELECT available_balance FROM users WHERE id = " + userId));
        assertDecimal("0.00", scalarDecimal("SELECT hold_balance FROM users WHERE id = " + userId));
    }

    private void assertDecimal(String expected, BigDecimal actual) {
        assertEquals(0, new BigDecimal(expected).compareTo(actual), "Expected " + expected + " but was " + actual);
    }

    private BigDecimal scalarDecimal(String sql) throws SQLException {
        try (Statement statement = keepAliveConnection.createStatement();
             ResultSet rs = statement.executeQuery(sql)) {
            rs.next();
            return rs.getBigDecimal(1);
        }
    }

    private long scalarLong(String sql) throws SQLException {
        try (Statement statement = keepAliveConnection.createStatement();
             ResultSet rs = statement.executeQuery(sql)) {
            rs.next();
            return rs.getLong(1);
        }
    }

    private String scalarString(String sql) throws SQLException {
        try (Statement statement = keepAliveConnection.createStatement()) {
            try (ResultSet rs = statement.executeQuery(sql)) {
                rs.next();
                return rs.getString(1);
            }
        }
    }

    private void assertTableCounts(int products, int productImages, int auctions) throws SQLException {
        try (Connection connection = DriverManager.getConnection(jdbcUrl)) {
            assertEquals(products, countRows(connection, "products"));
            assertEquals(productImages, countRows(connection, "product_images"));
            assertEquals(auctions, countRows(connection, "auctions"));
        }
    }

    private int countRows(Connection connection, String table) throws SQLException {
        try (Statement statement = connection.createStatement();
             var rs = statement.executeQuery("SELECT COUNT(*) FROM " + table)) {
            rs.next();
            return rs.getInt(1);
        }
    }

    private void createSchema(Connection connection) throws SQLException {
        try (Statement statement = connection.createStatement()) {
            statement.execute("""
                CREATE TABLE users (
                    id BIGINT PRIMARY KEY AUTO_INCREMENT,
                    username VARCHAR(100) NOT NULL UNIQUE,
                    email VARCHAR(255) NOT NULL UNIQUE,
                    password_hash VARCHAR(255) NOT NULL,
                    phone_number VARCHAR(20),
                    position VARCHAR(20) NOT NULL DEFAULT 'USER',
                    status VARCHAR(20) NOT NULL DEFAULT 'ACTIVE',
                    available_balance DECIMAL(15,2) NOT NULL DEFAULT 0,
                    hold_balance DECIMAL(15,2) NOT NULL DEFAULT 0,
                    warning_count INT NOT NULL DEFAULT 0,
                    lock_until TIMESTAMP DEFAULT NULL,
                    version BIGINT NOT NULL DEFAULT 0,
                    time_init TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
                )
                """);
            statement.execute("""
                CREATE TABLE products (
                    id BIGINT PRIMARY KEY AUTO_INCREMENT,
                    seller_id BIGINT NOT NULL,
                    name VARCHAR(255) NOT NULL,
                    description TEXT,
                    category_id VARCHAR(100) NOT NULL,
                    product_condition VARCHAR(50) NOT NULL,
                    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
                    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
                    status VARCHAR(20) NOT NULL DEFAULT 'ACTIVE',
                    version BIGINT NOT NULL DEFAULT 0,
                    CONSTRAINT fk_products_seller FOREIGN KEY (seller_id) REFERENCES users(id)
                )
                """);
            statement.execute("""
                CREATE TABLE product_images (
                    id BIGINT PRIMARY KEY AUTO_INCREMENT,
                    product_id BIGINT NOT NULL,
                    image_url VARCHAR(255) NOT NULL,
                    is_thumbnail BOOLEAN NOT NULL DEFAULT FALSE,
                    CONSTRAINT fk_product_images_product FOREIGN KEY (product_id) REFERENCES products(id)
                )
                """);
            statement.execute("""
                CREATE TABLE auctions (
                    id BIGINT PRIMARY KEY AUTO_INCREMENT,
                    product_id BIGINT NOT NULL,
                    seller_id BIGINT NOT NULL,
                    title TEXT NOT NULL,
                    description TEXT,
                    minimum_bid_step DECIMAL(15,2) NOT NULL,
                    starting_price DECIMAL(15,2) NOT NULL,
                    current_price DECIMAL(15,2) NOT NULL,
                    reserve_price DECIMAL(15,2),
                    buy_now_price DECIMAL(15,2),
                    final_price DECIMAL(15,2),
                    starting_time TIMESTAMP NOT NULL,
                    ending_time TIMESTAMP NOT NULL,
                    status VARCHAR(20) NOT NULL DEFAULT 'SCHEDULED',
                    winner_user_id BIGINT NULL,
                    anti_snipe_extension_count INT NOT NULL DEFAULT 0,
                    version BIGINT NOT NULL DEFAULT 0,
                    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
                    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
                    CONSTRAINT fk_auctions_product FOREIGN KEY (product_id) REFERENCES products(id),
                    CONSTRAINT fk_auctions_seller FOREIGN KEY (seller_id) REFERENCES users(id),
                    CONSTRAINT fk_auctions_winner FOREIGN KEY (winner_user_id) REFERENCES users(id)
                )
                """);
            statement.execute("""
                CREATE TABLE bids (
                    id BIGINT PRIMARY KEY AUTO_INCREMENT,
                    auction_id BIGINT NOT NULL,
                    bidder_id BIGINT NOT NULL,
                    bid_amount DECIMAL(15,2) NOT NULL,
                    bid_time TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
                    bid_source VARCHAR(20) NOT NULL DEFAULT 'USER_BID',
                    status VARCHAR(20) NOT NULL,
                    CONSTRAINT fk_bids_auction FOREIGN KEY (auction_id) REFERENCES auctions(id),
                    CONSTRAINT fk_bids_bidder FOREIGN KEY (bidder_id) REFERENCES users(id)
                )
                """);
            statement.execute("""
                CREATE TABLE autobids (
                    id BIGINT PRIMARY KEY AUTO_INCREMENT,
                    auction_id BIGINT NOT NULL,
                    user_id BIGINT NOT NULL,
                    max_bid_amount DECIMAL(15,2) NOT NULL,
                    status VARCHAR(20) NOT NULL,
                    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
                    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
                    CONSTRAINT uq_autobids_auction_user UNIQUE (auction_id, user_id),
                    CONSTRAINT fk_autobids_auction FOREIGN KEY (auction_id) REFERENCES auctions(id),
                    CONSTRAINT fk_autobids_user FOREIGN KEY (user_id) REFERENCES users(id)
                )
                """);
            statement.execute("""
                CREATE TABLE payments (
                    id BIGINT PRIMARY KEY AUTO_INCREMENT,
                    auction_id BIGINT NOT NULL,
                    buyer_id BIGINT NOT NULL,
                    seller_id BIGINT NOT NULL,
                    winning_bid_id BIGINT NOT NULL,
                    amount DECIMAL(15,2) NOT NULL,
                    type VARCHAR(30) NOT NULL,
                    status VARCHAR(30) NOT NULL,
                    held_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
                    released_at TIMESTAMP NULL,
                    refunded_at TIMESTAMP NULL,
                    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
                    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
                    CONSTRAINT uq_payments_auction UNIQUE (auction_id),
                    CONSTRAINT fk_payments_auction FOREIGN KEY (auction_id) REFERENCES auctions(id),
                    CONSTRAINT fk_payments_buyer FOREIGN KEY (buyer_id) REFERENCES users(id),
                    CONSTRAINT fk_payments_seller FOREIGN KEY (seller_id) REFERENCES users(id),
                    CONSTRAINT fk_payments_winning_bid FOREIGN KEY (winning_bid_id) REFERENCES bids(id)
                )
                """);
        }
    }
}

package com.vbay.server.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.vbay.server.model.Product;
import com.vbay.server.network_connection.ClientSession;
import com.vbay.server.repository.JDBCrepository.JdbcAuctionRepository;
import com.vbay.server.repository.JDBCrepository.JdbcProductImageRepository;
import com.vbay.server.repository.JDBCrepository.JdbcProductRepository;
import com.vbay.shared.dto.auctionDTO.CreateAuctionRequest;
import com.vbay.shared.dto.productDTO.CreateProductRequest;
import com.vbay.shared.dto.productDTO.ProductImageDTO;
import com.vbay.shared.enums.Position;
import com.vbay.shared.enums.shared_status.AuctionStatus;
import com.vbay.shared.enums.shared_status.ProductStatus;

class AuctionServiceIntegrationTest {
    private static final long SELLER_ID = 1L;

    private String jdbcUrl;
    private Connection keepAliveConnection;
    private AuctionService auctionService;
    private ClientSession session;

    @BeforeEach
    void setUp() throws SQLException {
        jdbcUrl = "jdbc:h2:mem:" + UUID.randomUUID() + ";MODE=MySQL;DATABASE_TO_UPPER=false;DB_CLOSE_DELAY=-1";
        keepAliveConnection = DriverManager.getConnection(jdbcUrl);
        createSchema(keepAliveConnection);
        auctionService = new AuctionService(() -> DriverManager.getConnection(jdbcUrl));
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

            long productId = auctionService.createProduct(validProduct(), session, productRepository, imageRepository);

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
            new BigDecimal("220.00"),
            new BigDecimal("200.00"),
            new BigDecimal("10.00"),
            LocalDateTime.now().plusHours(1),
            LocalDateTime.now().plusHours(2)
        );
    }

    private void seedUser(long userId) throws SQLException {
        try (Statement statement = keepAliveConnection.createStatement()) {
            statement.executeUpdate("""
                INSERT INTO users (id, username, email, password_hash, position, status)
                VALUES (%d, 'seller', 'seller@example.com', 'hash', 'USER', 'ACTIVE')
                """.formatted(userId));
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
                    balance DECIMAL(15,2) NOT NULL DEFAULT 0,
                    time_init DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP
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
                    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
                    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
                    status VARCHAR(20) NOT NULL DEFAULT 'ACTIVE',
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
                    starting_time DATETIME NOT NULL,
                    ending_time DATETIME NOT NULL,
                    status VARCHAR(20) NOT NULL DEFAULT 'SCHEDULED',
                    winner_user_id BIGINT NULL,
                    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
                    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
                    CONSTRAINT fk_auctions_product FOREIGN KEY (product_id) REFERENCES products(id),
                    CONSTRAINT fk_auctions_seller FOREIGN KEY (seller_id) REFERENCES users(id),
                    CONSTRAINT fk_auctions_winner FOREIGN KEY (winner_user_id) REFERENCES users(id)
                )
                """);
        }
    }
}

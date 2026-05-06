package com.vbay.server.service;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;

import com.vbay.server.databaseManager.ConnectionProvider;
import com.vbay.server.exception.AuthenticationException;
import com.vbay.server.mapper.dtomapper.ProductImageMapper;
import com.vbay.server.model.Auction;
import com.vbay.server.model.Product;
import com.vbay.server.model.ProductImage;
import com.vbay.server.network_connection.ClientSession;
import com.vbay.server.repository.AuctionRepository;
import com.vbay.server.repository.ProductImageRepository;
import com.vbay.server.repository.ProductRepository;
import com.vbay.server.repository.RepositoryFactory;
import com.vbay.server.service.validation.ValidateAuctionDTO;
import com.vbay.shared.dto.auctionDTO.CreateAuctionRequest;
import com.vbay.shared.dto.productDTO.CreateProductRequest;


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
        ValidateAuctionDTO.validateCreateAuctionRequest(request);

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

}

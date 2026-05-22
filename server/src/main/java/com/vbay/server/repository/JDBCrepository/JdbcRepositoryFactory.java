package com.vbay.server.repository.JDBCrepository;

import java.sql.Connection;

import com.vbay.server.repository.AdminAccountRepository;
import com.vbay.server.repository.AuctionRepository;
import com.vbay.server.repository.AutobidRepository;
import com.vbay.server.repository.BidRepository;
import com.vbay.server.repository.PaymentRepository;
import com.vbay.server.repository.UserRepository;
import com.vbay.server.repository.RepositoryFactory;
import com.vbay.server.repository.ProductImageRepository;
import com.vbay.server.repository.ProductRepository;
import com.vbay.server.upload.ImageStorageService;


public class JdbcRepositoryFactory implements RepositoryFactory {
    private final ImageStorageService imageStorageService;

    public JdbcRepositoryFactory() {
        this(new ImageStorageService());
    }

    public JdbcRepositoryFactory(ImageStorageService imageStorageService) {
        this.imageStorageService = imageStorageService;
    }

    @Override
    public AdminAccountRepository createAdminAccountRepository(Connection connection) {
        return new JdbcAdminAccountRepository(connection);
    }
    
    @Override
    public AuctionRepository createAuctionRepository(Connection connection) {
        return new JdbcAuctionRepository(connection, imageStorageService);
    }

    @Override
    public AutobidRepository createAutobidRepository(Connection connection) {
        return new JdbcAutobidRepository(connection);
    }

    @Override
    public UserRepository createUserRepository(Connection connection) {
        return new JdbcUserRepository(connection);
    }
    @Override
    public ProductRepository createProductRepository(Connection connection) {
        return new JdbcProductRepository(connection);
    }
    @Override
    public ProductImageRepository createProductImageRepository(Connection connection) {
        return new JdbcProductImageRepository(connection, imageStorageService);
    }

    @Override
    public BidRepository createBidRepository(Connection connection) {
        return new JdbcBidRepository(connection);
    }

    @Override
    public PaymentRepository createPaymentRepository(Connection connection) {
        return new JdbcPaymentRepository(connection);
    }
}

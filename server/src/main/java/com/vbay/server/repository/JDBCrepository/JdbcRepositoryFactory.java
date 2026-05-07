package com.vbay.server.repository.JDBCrepository;

import java.sql.Connection;

import com.vbay.server.repository.AuctionRepository;
import com.vbay.server.repository.BidRepository;
import com.vbay.server.repository.PaymentRepository;
import com.vbay.server.repository.UserRepository;
import com.vbay.server.repository.RepositoryFactory;
import com.vbay.server.repository.ProductImageRepository;
import com.vbay.server.repository.ProductRepository;


public class JdbcRepositoryFactory implements RepositoryFactory {
    
    @Override
    public AuctionRepository createAuctionRepository(Connection connection) {
        return new JdbcAuctionRepository(connection);
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
        return new JdbcProductImageRepository(connection);
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

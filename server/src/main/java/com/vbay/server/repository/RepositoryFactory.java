package com.vbay.server.repository;

import java.sql.Connection;

public interface RepositoryFactory {
    AdminAccountRepository createAdminAccountRepository(Connection connection);
    ProductRepository createProductRepository(Connection connection);
    ProductImageRepository createProductImageRepository(Connection connection);
    AuctionRepository createAuctionRepository(Connection connection);
    AutobidRepository createAutobidRepository(Connection connection);
    UserRepository createUserRepository(Connection connection);
    BidRepository createBidRepository(Connection connection); 
    PaymentRepository createPaymentRepository(Connection connection);
}

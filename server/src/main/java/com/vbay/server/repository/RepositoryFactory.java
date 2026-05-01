package com.vbay.server.repository;

import java.sql.Connection;

public interface RepositoryFactory {
    ProductRepository createProductRepository(Connection connection);
    ProductImageRepository createProductImageRepository(Connection connection);
    AuctionRepository createAuctionRepository(Connection connection);
    UserRepository createUserRepository(Connection connection);
}

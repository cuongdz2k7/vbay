package com.vbay.server.service.bid;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import com.vbay.server.databaseManager.ConnectionProvider;
import com.vbay.server.exception.AuthenticationException;
import com.vbay.server.exception.ValidationException;
import com.vbay.server.model.Auction;
import com.vbay.server.model.Autobid;
import com.vbay.server.model.Bid;
import com.vbay.server.network_connection.ClientSession;
import com.vbay.server.repository.AuctionRepository;
import com.vbay.server.repository.AutobidRepository;
import com.vbay.server.repository.BidRepository;
import com.vbay.server.repository.ProductImageRepository;
import com.vbay.server.repository.RepositoryFactory;
import com.vbay.server.service.result.UserMyBidListItemResult;
import com.vbay.server.service.result.mapper.ResultMapper;
import com.vbay.server.repository.UserRepository;
import com.vbay.shared.dto.auctionDTO.MyBidListResponse;
import com.vbay.shared.dto.realtimeDTO.payload.MyBidListItemPayload;
import com.vbay.shared.dto.realtimeDTO.payload.BidHistoryItemPayload;
import com.vbay.shared.enums.bid.BidSource;
import com.vbay.shared.enums.bid.BidStatus;

import java.math.BigDecimal;

public class BidQueryService {
    private final ConnectionProvider connectionProvider;
    private final RepositoryFactory repositoryFactory;

    public BidQueryService(
            ConnectionProvider connectionProvider,
            RepositoryFactory repositoryFactory) {
        this.connectionProvider = connectionProvider;
        this.repositoryFactory = repositoryFactory;
    }

    private void checkSession(ClientSession session) {
        if (session == null || !session.isAuthenticated()) {
            throw new AuthenticationException("User must be logged in to perform this action");
        }
    }

    public MyBidListResponse getMyBidList(ClientSession session) throws SQLException {
        checkSession(session);

        try (Connection connection = connectionProvider.getConnection()) {
            BidRepository bidRepository = repositoryFactory.createBidRepository(connection);
            AuctionRepository auctionRepository = repositoryFactory.createAuctionRepository(connection);
            AutobidRepository autobidRepository = repositoryFactory.createAutobidRepository(connection);
            ProductImageRepository productImageRepository = repositoryFactory.createProductImageRepository(connection);

            List<MyBidListItemPayload> items = new ArrayList<>();
            for (Bid bid : bidRepository.findLatestBidsByBidderId(session.getUserId())) {
                Auction auction = auctionRepository.findById(bid.getAuctionId())
                    .orElseThrow(() -> new ValidationException("Auction not found"));
                String thumbnailUrl = productImageRepository
                    .findThumbnailUrlByProductId(auction.getProductId())
                    .orElse(null);

                UserMyBidListItemResult result = ResultMapper.toUserMyBidListItemResult(
                    auction,
                    thumbnailUrl,
                    bid,
                    resolveMyMaxBidAmount(bid, autobidRepository),
                    bid.getStatus(),
                    bid.getBidTime()
                );
                items.add(ResultMapper.toMyBidListItemPayload(result));
            }

            return new MyBidListResponse(items);
        }
    }

    private BigDecimal resolveMyMaxBidAmount(
            Bid bid,
            AutobidRepository autobidRepository) throws SQLException {
        if (bid.getBidSource() != BidSource.AUTO_BID || bid.getStatus() != BidStatus.WINNING) {
            return null;
        }

        return autobidRepository
            .findByAuctionIdAndUserId(bid.getAuctionId(), bid.getBidderId())
            .map(Autobid::getMaxBidAmount)
            .orElse(null);
    }

    public List<BidHistoryItemPayload> getBidHistory(long auctionId) throws SQLException {
        try (Connection connection = connectionProvider.getConnection()) {
            BidRepository bidRepository = repositoryFactory.createBidRepository(connection);
            UserRepository userRepository = repositoryFactory.createUserRepository(connection);
            AuctionRepository auctionRepository = repositoryFactory.createAuctionRepository(connection);

            Auction auction = auctionRepository.findById(auctionId)
                .orElseThrow(() -> new ValidationException("Auction not found"));

            List<BidHistoryItemPayload> items = new ArrayList<>();
            List<Bid> bids = bidRepository.findBidsByAuctionId(auctionId);
            for (Bid bid : bids) {
                String displayName = userRepository.findById(bid.getBidderId())
                    .map(com.vbay.server.model.User::getUserName)
                    .orElse("Anonymous");

                BidHistoryItemPayload payload = new BidHistoryItemPayload(
                    bid.getAuctionId(),
                    auction.getVersion(),
                    bid.getId(),
                    bid.getBidderId(),
                    displayName,
                    bid.getBidAmount(),
                    bid.getStatus().name(),
                    bid.getBidSource().name(),
                    bid.getBidTime()
                );
                items.add(payload);
            }
            return items;
        }
    }
}

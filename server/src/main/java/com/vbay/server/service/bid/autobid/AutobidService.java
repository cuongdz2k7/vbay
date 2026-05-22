package com.vbay.server.service.bid.autobid;

import java.sql.Connection;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import com.vbay.server.databaseManager.ConnectionProvider;
import com.vbay.server.exception.ValidationException;
import com.vbay.server.model.Auction;
import com.vbay.server.model.Bid;
import com.vbay.server.repository.AuctionRepository;
import com.vbay.server.repository.AutobidRepository;
import com.vbay.server.repository.BidRepository;
import com.vbay.server.repository.ProductImageRepository;
import com.vbay.server.repository.RepositoryFactory;
import com.vbay.server.repository.UserRepository;
import com.vbay.server.service.bid.autobid.enums.BalanceChangeType;
import com.vbay.server.service.bid.autobid.enums.BidType;
import com.vbay.server.service.bid.autobid.model.AutobidChange;
import com.vbay.server.service.bid.autobid.model.AutobidResolution;
import com.vbay.server.service.bid.autobid.model.BalanceChange;
import com.vbay.server.service.bid.autobid.model.BidRecordChange;
import com.vbay.server.service.result.PlaceBidResult;
import com.vbay.server.service.result.UserMyBidListItemResult;
import com.vbay.server.service.result.mapper.ResultMapper;
import com.vbay.shared.enums.bid.BidSource;
import com.vbay.shared.enums.bid.BidStatus;

public class AutobidService {
    private final ConnectionProvider connectionProvider;
    private final RepositoryFactory repositoryFactory;

    public AutobidService(ConnectionProvider connectionProvider, RepositoryFactory repositoryFactory) {
        // Initialize the service with necessary dependencies, such as database connection
        this.connectionProvider = connectionProvider;
        this.repositoryFactory = repositoryFactory;
    }

    public void updateAutobidList(long auctionId) {
        // TODO: load autobid records through the future AutobidRepository and persist engine changes.
    }

    public List<BalanceChange> applyAutobidResolutionChanges(
            AutobidResolution resolution,
            Connection connection) throws SQLException {
        UserRepository userRepository = repositoryFactory.createUserRepository(connection);
        AutobidRepository autobidRepository = repositoryFactory.createAutobidRepository(connection);

        for (BalanceChange balanceChange : resolution.getBalanceChanges()) {
            if (BalanceChangeType.HOLD.equals(balanceChange.getType())) {
                userRepository.holdBalance(balanceChange.getUserId(), balanceChange.getAmount());
            } else if (BalanceChangeType.RELEASE.equals(balanceChange.getType())) {
                userRepository.releaseHoldBalance(balanceChange.getUserId(), balanceChange.getAmount());
            }
        }

        for (AutobidChange autobidChange : resolution.getAutobidChanges()) {
            autobidRepository.update(autobidChange);
        }

        return resolution.getBalanceChanges();
    }

    public PlaceBidResult applyManualBidResolution(
            AutobidResolution resolution,
            Optional<Bid> oldWinningBid,
            LocalDateTime dbNow,
            Connection connection) throws SQLException {
        BidRepository bidRepository = repositoryFactory.createBidRepository(connection);
        AuctionRepository auctionRepository = repositoryFactory.createAuctionRepository(connection);
        ProductImageRepository productImageRepository = repositoryFactory.createProductImageRepository(connection);

        Long previousWinningUserId = null;
        Long previousWinningBidId = null;
        if (oldWinningBid.isPresent()) {
            Bid oldBid = oldWinningBid.get();
            previousWinningUserId = oldBid.getBidderId();
            previousWinningBidId = oldBid.getId();
            bidRepository.updateStatus(oldBid.getId(), BidStatus.OUTBID);
        }

        applyAutobidResolutionChanges(resolution, connection);

        Bid currentBid = null;
        for (BidRecordChange bidRecord : resolution.getBidRecordsToCreate()) {
            BidSource source = BidType.SYSTEM_AUTO_BID.equals(bidRecord.getType())
                ? BidSource.AUTO_BID
                : BidSource.USER_BID;
            currentBid = new Bid(
                bidRecord.getAuctionId(),
                bidRecord.getUserId(),
                bidRecord.getAmount(),
                dbNow,
                BidStatus.WINNING,
                source
            );
            bidRepository.save(currentBid);
        }

        auctionRepository.updateCurrentBid(
            resolution.getAuctionId(),
            resolution.getNewCurrentPrice(),
            resolution.getNewWinnerUserId()
        );

        Auction refreshedAuction = auctionRepository.findById(resolution.getAuctionId())
            .orElseThrow(() -> new ValidationException("Auction not found"));
        String thumbnailUrl = productImageRepository.findThumbnailUrlByProductId(refreshedAuction.getProductId()).orElse(null);
        List<UserMyBidListItemResult> affectedMyBidItems = buildAffectedMyBidItems(
            refreshedAuction,
            thumbnailUrl,
            bidRepository,
            dbNow
        );

        if (currentBid == null) {
            currentBid = oldWinningBid.orElseThrow(() -> new ValidationException("Autobid resolution did not create a bid"));
        }

        return ResultMapper.toPlaceBidResult(
            refreshedAuction,
            currentBid,
            previousWinningUserId,
            previousWinningBidId,
            affectedMyBidItems
        );
    }

    private List<UserMyBidListItemResult> buildAffectedMyBidItems(
            Auction refreshedAuction,
            String thumbnailUrl,
            BidRepository bidRepository,
            LocalDateTime updatedAt) throws SQLException {
        return bidRepository.findLatestBidPerBidderByAuctionId(refreshedAuction.getId()).stream()
            .map(bid -> ResultMapper.toUserMyBidListItemResult(
                refreshedAuction,
                thumbnailUrl,
                bid,
                bid.getStatus(),
                updatedAt
            ))
            .toList();
    }
    

}

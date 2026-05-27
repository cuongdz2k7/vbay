package com.vbay.server.service.bid.resolution;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import com.vbay.server.exception.ValidationException;
import com.vbay.server.model.Auction;
import com.vbay.server.model.Autobid;
import com.vbay.server.model.Bid;
import com.vbay.server.model.Payment;
import com.vbay.server.repository.AuctionRepository;
import com.vbay.server.repository.AutobidRepository;
import com.vbay.server.repository.BidRepository;
import com.vbay.server.repository.PaymentRepository;
import com.vbay.server.repository.ProductImageRepository;
import com.vbay.server.repository.RepositoryFactory;
import com.vbay.server.repository.UserRepository;
import com.vbay.server.service.bid.resolution.model.BalanceChange;
import com.vbay.server.service.bid.resolution.model.PaymentCreate;
import com.vbay.server.service.bid.resolution.model.auction.AuctionChange;
import com.vbay.server.service.bid.resolution.model.autobid.AutobidCreate;
import com.vbay.server.service.bid.resolution.model.autobid.AutobidMaxBidUpdate;
import com.vbay.server.service.bid.resolution.model.autobid.AutobidStatusChange;
import com.vbay.server.service.bid.resolution.model.bid.AppliedBidResolution;
import com.vbay.server.service.bid.resolution.model.bid.BidCreate;
import com.vbay.server.service.bid.resolution.model.bid.BidFinalization;
import com.vbay.server.service.bid.resolution.model.bid.BidResolution;
import com.vbay.server.service.bid.resolution.model.bid.BidStatusUpdate;
import com.vbay.server.service.result.UserBalanceResult;
import com.vbay.server.service.result.UserMyBidListItemResult;
import com.vbay.server.service.result.mapper.ResultMapper;
import com.vbay.shared.enums.bid.BidSource;
import com.vbay.shared.enums.bid.BidStatus;

public class BidResolutionApplier {
    private final RepositoryFactory repositoryFactory;

    public BidResolutionApplier(RepositoryFactory repositoryFactory) {
        this.repositoryFactory = repositoryFactory;
    }

    public AppliedBidResolution apply(
            BidResolution resolution,
            LocalDateTime dbNow,
            Connection connection) throws SQLException {
        UserRepository userRepository = repositoryFactory.createUserRepository(connection);
        BidRepository bidRepository = repositoryFactory.createBidRepository(connection);
        AutobidRepository autobidRepository = repositoryFactory.createAutobidRepository(connection);
        AuctionRepository auctionRepository = repositoryFactory.createAuctionRepository(connection);
        PaymentRepository paymentRepository = repositoryFactory.createPaymentRepository(connection);
        ProductImageRepository productImageRepository = repositoryFactory.createProductImageRepository(connection);

        List<UserBalanceResult> balanceResults = new ArrayList<>();
        List<Payment> payments = new ArrayList<>();
        long auctionVersion = 0L;

        /*
        1. Apply balance changes:
            - Nếu là HOLD: hold balance.
            - Nếu là RELEASE: release hold balance.
            - Nếu là DECREASE_AVAILABLE: decrease available balance
            DECREASE_AVAILABLE sẽ được dùng trong trường hợp buy now.
                vì tiền không còn là “đặt cọc có thể release do outbid” nữa. Nó chuyển sang payment HELD.
        */

        Map<Long, String> affectedBalanceReasons = new LinkedHashMap<>();
        for (BalanceChange change : resolution.getBalanceChanges()) {
            if (change.getAmount() == null || change.getAmount().signum() <= 0) {
                throw new ValidationException("Balance change amount must be positive");
            }

            switch(change.getType()) {
                case HOLD -> userRepository.holdBalance(change.getUserId(), change.getAmount());
                case RELEASE -> userRepository.releaseHoldBalance(change.getUserId(), change.getAmount());
                case DECREASE_AVAILABLE -> userRepository.decreaseAvailableBalance(change.getUserId(), change.getAmount());
                case DEPOSIT_AVAILABLE -> userRepository.depositAvailableBalance(change.getUserId(), change.getAmount());
                default -> throw new ValidationException("Unknown balance change type");
            }

            affectedBalanceReasons.put(change.getUserId(), change.getReason());
        }

        for (Map.Entry<Long, String> entry : affectedBalanceReasons.entrySet()) {
            balanceResults.add(readUserBalanceResult(
                userRepository,
                entry.getKey(),
                entry.getValue(),
                dbNow
            ));
        }

        /*
        2. Apply autobid changes:
        - create autobid
        - update max bid amount
        - update status
        */

        for (AutobidCreate create : resolution.getAutobidCreates()) {
            Autobid autobid = new Autobid(
                create.getAuctionId(),
                create.getUserId(),
                create.getMaxBidAmount(),
                create.getStatus(),
                create.getCreatedAt() != null ? create.getCreatedAt() : dbNow,
                create.getUpdatedAt() != null ? create.getUpdatedAt() : dbNow
            );

            autobidRepository.save(autobid);
            create.setId(autobid.getId());
        }

        for (AutobidMaxBidUpdate update : resolution.getAutobidMaxBidUpdates()) {
            if (update.getMaxBidAmount() == null || update.getMaxBidAmount().signum() <= 0) {
                throw new ValidationException("Autobid max bid amount must be positive");
            }

            autobidRepository.updateMaxBidAmount(
                update.getAutobidId(),
                update.getMaxBidAmount()
            );
        }


        for (AutobidStatusChange change : resolution.getAutobidStatusChanges()) {
            if (change.getStatus() == null) {
                throw new ValidationException("Autobid status change must have status");
            }

            autobidRepository.updateStatus(
                change.getAutobidId(),
                change.getStatus()
            );
        }

        /*
        3. Apply bid changes:
        - create bid
        - update status
        */

        List<Bid> createdBids = new ArrayList<>();
        for (BidCreate bidCreate : resolution.getBidCreates()) {
            Bid bid = new Bid(
                bidCreate.getAuctionId(),
                bidCreate.getBidderId(),
                bidCreate.getAmount(),
                dbNow,
                bidCreate.getStatus(),
                bidCreate.getSource()
            );

            bidRepository.save(bid);
            bidCreate.setId(bid.getId());
            createdBids.add(bid);
        }

        for (BidStatusUpdate update : resolution.getBidStatusUpdates()) {
            bidRepository.updateStatus(update.getBidId(), update.getStatus());
        }
        /*
        4. Apply bid finalizations:
            - Mark các bid khác LOST sau khi BUY_NOW bid đã save.
            - Mark auction CLOSED nếu có bid thắng hoặc buy now.
        */
        for (BidFinalization finalization : resolution.getBidFinalizations()) {
            finalization.apply(bidRepository, resolution);
        }
        /*
        5. Apply auction change:
             - Buy Now sẽ update lại current price, buyer id, status của auction. (vận dụng đa hình của interface auctionchange)
        */
        for (AuctionChange auctionChange : resolution.getAuctionChanges()) {
            auctionVersion = auctionChange.apply(auctionRepository);
        }
        /*
        6. Apply payment changes:
            - create payment HELD, tham chiếu tới bid source BUY_NOW.
            - Applier sẽ save bid trước, lấy id của BidCreate source BUY_NOW,
        */
        for (PaymentCreate paymentCreate : resolution.getPaymentCreates()) {
            BidCreate bidCreate = resolution.findBidCreateBySource(paymentCreate.getBidSource())
                .orElseThrow(() -> new ValidationException("Payment bid was not created"));

            Payment payment = new Payment(
                paymentCreate.getAuctionId(),
                paymentCreate.getBuyerId(),
                paymentCreate.getSellerId(),
                bidCreate.getId(),
                paymentCreate.getAmount(),
                paymentCreate.getType(),
                paymentCreate.getStatus()
            );
            paymentRepository.save(payment);
            payments.add(payment);
        }
        
        ///tạo resolution để trả về cho service, service sẽ lấy data trong resolution này để build result trả về cho client
        Auction refreshedAuction = auctionRepository.findById(resolution.getAuctionId())
            .orElseThrow(() -> new ValidationException("Auction not found"));

        String thumbnailUrl = productImageRepository.findThumbnailUrlByProductId(refreshedAuction.getProductId()).orElse(null);
        List<UserMyBidListItemResult> affectedMyBidItems = new ArrayList<>();
        for (Bid bid : bidRepository.findLatestBidPerBidderByAuctionId(refreshedAuction.getId())) {
            affectedMyBidItems.add(ResultMapper.toUserMyBidListItemResult(
                refreshedAuction,
                thumbnailUrl,
                bid,
                resolveMyMaxBidAmount(bid, autobidRepository),
                bid.getStatus(),
                dbNow
            ));
        }
        return new AppliedBidResolution(
            refreshedAuction,
            createdBids,
            payments,
            balanceResults,
            affectedMyBidItems,
            auctionVersion
        );
    }

    private UserBalanceResult readUserBalanceResult(
            UserRepository userRepository,
            long userId,
            String reason,
            LocalDateTime updatedAt) throws SQLException {
        ///touserbalance result đã được nối với userVersion, nên phía client có thể check được stale data nếu có nhiều balance change xảy ra liên tiếp. 
        return userRepository.findById(userId)
            .map(user -> ResultMapper.toUserBalanceResult(user, reason, updatedAt))
            .orElseThrow(() -> new ValidationException("User not found"));
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
}

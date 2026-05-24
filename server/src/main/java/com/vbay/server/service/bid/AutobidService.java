package com.vbay.server.service.bid;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import com.vbay.server.databaseManager.ConnectionProvider;
import com.vbay.server.exception.AuthenticationException;
import com.vbay.server.exception.ValidationException;
import com.vbay.server.model.Auction;
import com.vbay.server.model.Autobid;
import com.vbay.server.model.Bid;
import com.vbay.server.model.User;
import com.vbay.server.network_connection.ClientSession;
import com.vbay.server.realtime.domain.AuctionListItemUpdatedDomainEvent;
import com.vbay.server.realtime.domain.AutobidUpdatedDomainEvent;
import com.vbay.server.realtime.domain.BidUpdatedDomainEvent;
import com.vbay.server.realtime.domain.DomainEvent;
import com.vbay.server.realtime.domain.UserBalanceUpdatedDomainEvent;
import com.vbay.server.realtime.domain.enums.AuctionListItemUpdateReason;
import com.vbay.server.realtime.publisher.DomainEventPublisher;
import com.vbay.server.repository.AuctionRepository;
import com.vbay.server.repository.AutobidRepository;
import com.vbay.server.repository.BidRepository;
import com.vbay.server.repository.RepositoryFactory;
import com.vbay.server.repository.UserRepository;
import com.vbay.server.service.bid.command.IncreaseAutobidCommand;
import com.vbay.server.service.bid.command.RegisterAutobidCommand;
import com.vbay.server.service.bid.engine.AuctionBidEngine;
import com.vbay.server.service.bid.enums.AutobidStatus;
import com.vbay.server.service.bid.resolution.AppliedBidResultMapper;
import com.vbay.server.service.bid.resolution.BidResolutionApplier;
import com.vbay.server.service.bid.resolution.model.bid.AppliedBidResolution;
import com.vbay.server.service.bid.resolution.model.bid.BidResolution;
import com.vbay.server.service.result.AuctionListItemResult;
import com.vbay.server.service.result.AutobidRegistrationResult;
import com.vbay.server.service.result.AutobidUpdateResult;
import com.vbay.server.service.result.BidUpdateResult;
import com.vbay.server.service.result.UserBalanceResult;
import com.vbay.server.service.validation.ValidateBidDTO;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.vbay.shared.Utils.JsonUtils;
import com.vbay.shared.protocol.Respond;

/*
BUG(fixed):
currentPrice = 100
minimumStep = 10
winning AutoBid max = 150

User B register max = 120



*/


public class AutobidService {
    private final ConnectionProvider connectionProvider;
    private final RepositoryFactory repositoryFactory;
    private final DomainEventPublisher domainEventPublisher;
    private final AuctionBidEngine auctionBidEngine;
    private final BidResolutionApplier bidResolutionApplier;
    

    public AutobidService(
            ConnectionProvider connectionProvider,
            RepositoryFactory repositoryFactory,
            DomainEventPublisher domainEventPublisher,
            AuctionBidEngine auctionBidEngine,
            BidResolutionApplier bidResolutionApplier) {
        this.connectionProvider = connectionProvider;
        this.repositoryFactory = repositoryFactory;
        this.domainEventPublisher = domainEventPublisher;
        this.auctionBidEngine = auctionBidEngine;
        this.bidResolutionApplier = bidResolutionApplier;
    }

    private void checkSession(ClientSession session) {
        if (session == null || !session.isAuthenticated()) {
            throw new AuthenticationException("User must be logged in to perform this action");
        }
    }

    private boolean isCurrentWinner(long userId, Optional<Bid> currentWinningBid) {
        return currentWinningBid.isPresent() && currentWinningBid.get().getBidderId() == userId;
    }
    
    private void validateRegisterAutobidBuyingPower(
            User user,
            BigDecimal maxBidAmount,
            Optional<Bid> currentWinningBid) {
        if (!user.isActive()) {
            throw new ValidationException("This user cannot use this action");
        }
        BigDecimal buyingPower = user.getAvailableBalance();

        if (isCurrentWinner(user.getId(), currentWinningBid)) {
            buyingPower = buyingPower.add(currentWinningBid.get().getBidAmount());
        }

        if (buyingPower.compareTo(maxBidAmount) < 0) {
            throw new ValidationException("Insufficient balance");
        }
    }

    private void validateAuctionEligibility (Auction auction, long userId, LocalDateTime dbNow) throws SQLException {
        if (auction.getStatus().isClosedForBidding()) {
            throw new ValidationException("Auction cannot receive bids");
        }
        if (dbNow.isBefore(auction.getStartingTime())) {
            throw new ValidationException("Auction is not started yet");
        }
        if (!dbNow.isBefore(auction.getEndingTime())) {
            throw new ValidationException("Auction has already ended");
        }
        if (userId == auction.getSellerId()) {
            throw new ValidationException("Cannot place bid on your own auction");
        }
    }

    private void validateAutoBidBuyNowLimit(Auction auction, BigDecimal maxBidAmount) {
        if (auction.getBuyNowPrice() != null
                && maxBidAmount.compareTo(auction.getBuyNowPrice()) >= 0) {
            throw new ValidationException("Max AutoBid must be lower than the Buy Now price");
        }
    }

    private void validateMinimumAutobidAmount(
            Auction auction,
            BigDecimal maxBidAmount,
            Optional<Bid> currentWinningBid) {
        BigDecimal minimum = currentWinningBid.isEmpty()
            ? auction.getStartPrice()
            : auction.getCurrentPrice().add(auction.getMinimumBidStep());

        if (maxBidAmount.compareTo(minimum) < 0) {
            throw new ValidationException("Max AutoBid must be at least " + minimum);
        }
    }

    ///validate nếu có autobid thì nó phải trùng với currentwinningbid
    private void validateWinningAutobidInvariant(
            Optional<Bid> currentWinningBid,
            Autobid autobid) {
        Bid bid = currentWinningBid.orElseThrow(
            () -> new ValidationException("Winning AutoBid exists without a winning bid")
        );
        if (bid.getBidderId() != autobid.getUserId()) {
            throw new ValidationException("Winning AutoBid user does not match current winning bid user");
        }
    }

    public AutobidRegistrationResult registerAutobid(
            long auctionId,
            BigDecimal maxBidAmount,
            ClientSession session) throws SQLException {
        checkSession(session);
        ValidateBidDTO.validateAutoBidRequest(auctionId, maxBidAmount);

        try (Connection connection = connectionProvider.getConnection()) {
            connection.setAutoCommit(false);
            boolean committed = false;

            try {
                AuctionRepository auctionRepository = repositoryFactory.createAuctionRepository(connection);
                BidRepository bidRepository = repositoryFactory.createBidRepository(connection);
                AutobidRepository autobidRepository = repositoryFactory.createAutobidRepository(connection);
                UserRepository userRepository = repositoryFactory.createUserRepository(connection);

                Auction auction = auctionRepository.lockAuctionForUpdate(auctionId)
                    .orElseThrow(() -> new ValidationException("Auction not found"));

                LocalDateTime dbNow = auctionRepository.getCurrentDatabaseTime();

                validateAuctionEligibility(auction, session.getUserId(), dbNow);
                validateAutoBidBuyNowLimit(auction, maxBidAmount);

                Optional<Bid> currentWinningBid = bidRepository.findWinningBidByAuctionId(auctionId);
                Optional<Autobid> winningAutobid = autobidRepository.findWinningByAuctionId(auctionId);
                Optional<Autobid> existingAutobid =
                    autobidRepository.findByAuctionIdAndUserId(auctionId, session.getUserId());

                validateMinimumAutobidAmount(auction, maxBidAmount, currentWinningBid);

                if (winningAutobid.isPresent()) {
                    validateWinningAutobidInvariant(currentWinningBid, winningAutobid.get());
                }

                existingAutobid.ifPresent(this::validateExistingAutobidCanReenter);

                User user = userRepository.lockUserForUpdate(session.getUserId())
                    .orElseThrow(() -> new ValidationException("User not found"));

                validateRegisterAutobidBuyingPower(user, maxBidAmount, currentWinningBid);

                BidResolution resolution = auctionBidEngine.resolveRegisterAutobid(
                    auction,
                    currentWinningBid,
                    winningAutobid,
                    new RegisterAutobidCommand(
                        auctionId,
                        session.getUserId(),
                        maxBidAmount,
                        dbNow
                    )
                );

                AppliedBidResolution applied = bidResolutionApplier.apply(
                    resolution,
                    dbNow,
                    connection
                );

                AuctionListItemResult listItem = auctionRepository.findAuctionListItemById(auctionId)
                    .orElseThrow(() -> new ValidationException("Auction list item not found"));
                
                Long previousWinningUserId = currentWinningBid.map(Bid::getBidderId).orElse(null);
                Long previousWinningBidId = currentWinningBid.map(Bid::getId).orElse(null);

                AutobidRegistrationResult registrationResult = null;
                BidUpdateResult bidEventResult;

                if (resolution.isAccepted()) {
                    registrationResult = AppliedBidResultMapper.toAutobidRegistrationResult(
                        applied,
                        resolution,
                        previousWinningUserId,
                        previousWinningBidId,
                        dbNow
                    );

                    bidEventResult = AppliedBidResultMapper.toPlaceBidResult(
                        applied,
                        previousWinningUserId,
                        previousWinningBidId,
                        dbNow
                    );
                } else {
                    bidEventResult = AppliedBidResultMapper.toPlaceBidResult(
                        applied,
                        previousWinningUserId,
                        previousWinningBidId,
                        dbNow
                    );
                }
                
                boolean extended = applied.getRefreshedAuction()
                    .getEndingTime()
                    .isAfter(auction.getEndingTime());

                AuctionListItemUpdateReason reason = extended
                    ? AuctionListItemUpdateReason.TIME_CHANGED
                    : AuctionListItemUpdateReason.BID_UPDATED;

                List<DomainEvent> events = buildRegisterAutobidEvents(
                    listItem,
                    reason,
                    bidEventResult,
                    registrationResult,
                    winningAutobid,
                    applied,
                    dbNow
                );

                connection.commit();
                committed = true;

                publishEvents(events);

                if (!resolution.isAccepted()) {
                    throw new ValidationException(resolution.getMessage());
                }

                return registrationResult;
            } catch (Exception e) {
                if (!committed) {
                    connection.rollback();
                }
                throw e;
            }
        }
    }
    
    private void validateIncreaseAutobidBuyingPower(User user, BigDecimal delta) {
        if (!user.isActive()) {
            throw new ValidationException("This user cannot use this action");
        }
        if (user.getAvailableBalance().compareTo(delta) < 0) {
            throw new ValidationException("Insufficient balance");
        }
    }

    private void validateAutobidBelongsToRequester(Autobid autobid, long requesterId) {
        if (autobid.getUserId() != requesterId) {
            throw new ValidationException("AutoBid does not belong to requester");
        }
    }

    private void validateAutobidCanBeIncreased(Autobid autobid) {
        if (autobid.getStatus() != AutobidStatus.WINNING) {
            throw new ValidationException("Only winning AutoBid can be increased");
        }
    }

    private void validateExistingAutobidCanReenter(Autobid autobid) {
        if (autobid.getStatus() == AutobidStatus.WINNING) {
            throw new ValidationException("AutoBid already exists for this auction");
        }
        if (autobid.getStatus() == AutobidStatus.WON) {
            throw new ValidationException("AutoBid already won this auction");
        }
    }

    private void validateNewMaxBidAmountIsGreater(BigDecimal newMaxBidAmount, Autobid existingAutobid) {
        if (newMaxBidAmount.compareTo(existingAutobid.getMaxBidAmount()) <= 0) {
            throw new ValidationException("New AutoBid max must be greater than current max");
        }
    }

    public void increaseMaxAutobidAmount(
            long auctionId,
            BigDecimal newMaxBidAmount,
            ClientSession session) throws SQLException {
        checkSession(session);
        ValidateBidDTO.validateAutoBidRequest(auctionId, newMaxBidAmount);

        try (Connection connection = connectionProvider.getConnection()) {
            connection.setAutoCommit(false);
            boolean committed = false;

            try {
                AuctionRepository auctionRepository = repositoryFactory.createAuctionRepository(connection);
                BidRepository bidRepository = repositoryFactory.createBidRepository(connection);
                AutobidRepository autobidRepository = repositoryFactory.createAutobidRepository(connection);
                UserRepository userRepository = repositoryFactory.createUserRepository(connection);

                Auction auction = auctionRepository.lockAuctionForUpdate(auctionId)
                    .orElseThrow(() -> new ValidationException("Auction not found"));

                LocalDateTime dbNow = auctionRepository.getCurrentDatabaseTime();

                validateAuctionEligibility(auction, session.getUserId(), dbNow);
                validateAutoBidBuyNowLimit(auction, newMaxBidAmount);
                ///lấy autobid của thằng requester
                Autobid existingAutobid = autobidRepository
                    .findByAuctionIdAndUserId(auctionId, session.getUserId())
                    .orElseThrow(() -> new ValidationException("AutoBid for this user and auction not found"));

                validateAutobidBelongsToRequester(existingAutobid, session.getUserId());
                validateAutobidCanBeIncreased(existingAutobid);
                validateNewMaxBidAmountIsGreater(newMaxBidAmount, existingAutobid);

                Optional<Bid> currentWinningBid = bidRepository.findWinningBidByAuctionId(auctionId);
                validateWinningAutobidInvariant(currentWinningBid, existingAutobid);

                BigDecimal delta = newMaxBidAmount.subtract(existingAutobid.getMaxBidAmount());

                User user = userRepository.lockUserForUpdate(session.getUserId())
                    .orElseThrow(() -> new ValidationException("User not found"));

                validateIncreaseAutobidBuyingPower(user, delta);

                BidResolution resolution = auctionBidEngine.resolveIncreaseMaxAutobidAmount(
                    auction,
                    existingAutobid,
                    new IncreaseAutobidCommand(
                        auctionId,
                        session.getUserId(),
                        newMaxBidAmount,
                        dbNow
                    )
                );

                AppliedBidResolution applied = bidResolutionApplier.apply(
                    resolution,
                    dbNow,
                    connection
                );

                List<DomainEvent> events = buildIncreaseMaxAutobidEvents(
                    applied,
                    existingAutobid,
                    newMaxBidAmount,
                    dbNow
                );

                connection.commit();
                committed = true;

                publishEvents(events);

                if (!resolution.isAccepted()) {
                    throw new ValidationException(resolution.getMessage());
                }
            } catch (Exception e) {
                if (!committed) {
                    connection.rollback();
                }
                throw e;
            }
        }
    }


    private AutobidUpdateResult toAutobidUpdateResult(
            Auction auction,
            long userId,
            long autobidId,
            BigDecimal maxBidAmount,
            AutobidStatus status,
            LocalDateTime updatedAt) {
        boolean showActiveMaxBid = status == AutobidStatus.WINNING
            && auction.getWinnerUserId() != null
            && auction.getWinnerUserId() == userId;

        return new AutobidUpdateResult(
            auction.getId(),
            userId,
            autobidId,
            maxBidAmount,
            status,
            showActiveMaxBid,
            showActiveMaxBid,
            updatedAt
        );
    }

    private List<DomainEvent> buildRegisterAutobidEvents(
            AuctionListItemResult listItem,
            AuctionListItemUpdateReason reason,
            BidUpdateResult bidEventResult,
            AutobidRegistrationResult registrationResult,
            Optional<Autobid> previousWinningAutobid,
            AppliedBidResolution applied,
            LocalDateTime occurredAt) {
        List<DomainEvent> events = new ArrayList<>();

        events.add(new AuctionListItemUpdatedDomainEvent(
            listItem,
            reason,
            occurredAt
        ));

        events.add(new BidUpdatedDomainEvent(bidEventResult));

        if (registrationResult != null) {
            previousWinningAutobid.ifPresent(oldAutobid ->
                events.add(new AutobidUpdatedDomainEvent(
                    toAutobidUpdateResult(
                        applied.getRefreshedAuction(),
                        oldAutobid.getUserId(),
                        oldAutobid.getId(),
                        oldAutobid.getMaxBidAmount(),
                        AutobidStatus.LOST,
                        occurredAt
                    )
                ))
            );

            events.add(new AutobidUpdatedDomainEvent(
                toAutobidUpdateResult(
                    applied.getRefreshedAuction(),
                    registrationResult.getUserId(),
                    registrationResult.getAutobidId(),
                    registrationResult.getMaxBidAmount(),
                    registrationResult.getAutobidStatus(),
                    occurredAt
                )
            ));
        }

        addBalanceEvents(events, applied);

        return events;
    }

    private List<DomainEvent> buildIncreaseMaxAutobidEvents(
            AppliedBidResolution applied,
            Autobid autobid,
            BigDecimal newMaxBidAmount,
            LocalDateTime occurredAt) {
        List<DomainEvent> events = new ArrayList<>();

        events.add(new AutobidUpdatedDomainEvent(
            toAutobidUpdateResult(
                applied.getRefreshedAuction(),
                autobid.getUserId(),
                autobid.getId(),
                newMaxBidAmount,
                AutobidStatus.WINNING,
                occurredAt
            )
        ));

        addBalanceEvents(events, applied);

        return events;
    }

    private void addBalanceEvents(
            List<DomainEvent> events,
            AppliedBidResolution applied) {
        for (UserBalanceResult balanceResult : applied.getBalanceResults()) {
            events.add(new UserBalanceUpdatedDomainEvent(
                balanceResult,
                balanceResult.getUpdatedAt()
            ));
        }
    }

    private void publishEvents(List<DomainEvent> events) {
        for (DomainEvent event : events) {
            domainEventPublisher.publish(event);
        }
    }

    public Respond<AutobidRegistrationResult> handleAutoBid(
            String requestId,
            JsonElement payload,
            ClientSession session) throws SQLException {
        if (payload == null || !payload.isJsonObject()) {
            return new Respond<>(requestId, false, "Invalid auto bid request", null);
        }

        JsonObject object = payload.getAsJsonObject();
        JsonElement auctionIdElement = object.get("auctionId");
        JsonElement maxBidAmountElement = object.get("maxBidAmount");
        if (maxBidAmountElement == null || maxBidAmountElement.isJsonNull()) {
            maxBidAmountElement = object.get("bidAmount");
        }

        if (auctionIdElement == null || auctionIdElement.isJsonNull()
                || maxBidAmountElement == null || maxBidAmountElement.isJsonNull()) {
            return new Respond<>(requestId, false, "Invalid auto bid request", null);
        }

        long auctionId = auctionIdElement.getAsLong();
        BigDecimal maxBidAmount = maxBidAmountElement.getAsBigDecimal();
        AutobidRegistrationResult result = registerAutobid(auctionId, maxBidAmount, session);
        return new Respond<>(requestId, true, "AutoBid subscribed successfully", result);
    }

    public Respond<Void> handleIncreaseAutobidMax(
            String requestId,
            JsonElement payload,
            ClientSession session) throws SQLException {
        if (payload == null || !payload.isJsonObject()) {
            return new Respond<>(requestId, false, "Invalid increase auto bid request", null);
        }

        JsonObject object = payload.getAsJsonObject();
        JsonElement auctionIdElement = object.get("auctionId");
        JsonElement maxBidAmountElement = object.get("maxBidAmount");
        if (maxBidAmountElement == null || maxBidAmountElement.isJsonNull()) {
            maxBidAmountElement = object.get("bidAmount");
        }

        if (auctionIdElement == null || auctionIdElement.isJsonNull()
                || maxBidAmountElement == null || maxBidAmountElement.isJsonNull()) {
            return new Respond<>(requestId, false, "Invalid increase auto bid request", null);
        }

        long auctionId = auctionIdElement.getAsLong();
        BigDecimal maxBidAmount = maxBidAmountElement.getAsBigDecimal();
        increaseMaxAutobidAmount(auctionId, maxBidAmount, session);
        return new Respond<>(requestId, true, "AutoBid max increased successfully", null);
    }
}

package com.vbay.server.service;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.google.gson.JsonElement;
import com.vbay.shared.Utils.JsonUtils;
import com.vbay.shared.protocol.Respond;
import com.vbay.shared.protocol.RealtimeEvent;

import com.vbay.server.databaseManager.ConnectionProvider;
import com.vbay.server.exception.ValidationException;
import com.vbay.server.model.Auction;
import com.vbay.server.model.User;
import com.vbay.server.network_connection.ClientConnectionRegistry;
import com.vbay.server.network_connection.ClientSession;
import com.vbay.server.realtime.transport.RealtimeBroadcaster;
import com.vbay.server.repository.AuctionRepository;
import com.vbay.server.repository.DepositRequestRepository;
import com.vbay.server.repository.RepositoryFactory;
import com.vbay.server.repository.UserRepository;
import com.vbay.shared.Utils.LoggingUtils;
import com.vbay.shared.dto.adminDTO.AdminAuctionActionRequest;
import com.vbay.shared.dto.adminDTO.AdminAuctionItem;
import com.vbay.shared.dto.adminDTO.AdminAuctionListResponse;
import com.vbay.shared.dto.adminDTO.AdminLockUserRequest;
import com.vbay.shared.dto.adminDTO.AdminUserActionRequest;
import com.vbay.shared.dto.adminDTO.AdminUserItem;
import com.vbay.shared.dto.adminDTO.AdminUserListResponse;
import com.vbay.shared.dto.adminDTO.AdminDepositItem;
import com.vbay.shared.dto.adminDTO.AdminDepositListResponse;
import com.vbay.shared.dto.adminDTO.AdminDepositActionRequest;
import com.vbay.shared.dto.realtimeDTO.payload.DepositRequestPayload;
import com.vbay.shared.dto.realtimeDTO.payload.UserBalanceUpdatedPayload;
import com.vbay.shared.dto.realtimeDTO.payload.UserWarnedPayload;
import com.vbay.shared.enums.payment.DepositRequestStatus;
import com.vbay.shared.enums.auction.AuctionStatus;
import com.vbay.shared.enums.auth.Position;
import com.vbay.shared.enums.auth.UserStatus;
import com.vbay.shared.enums.realtime.RealtimeEventType;
import com.vbay.shared.enums.realtime.RoomType;
import com.vbay.shared.dto.realtimeDTO.Room;

public class AdminService {
    private static final Logger LOGGER = LoggingUtils.getLogger(AdminService.class);
    
    private final ConnectionProvider connectionProvider;
    private final RepositoryFactory repositoryFactory;
    private final ClientConnectionRegistry connectionRegistry;
    private final RealtimeBroadcaster realtimeBroadcaster;

    public AdminService(ConnectionProvider connectionProvider, 
                        RepositoryFactory repositoryFactory, 
                        ClientConnectionRegistry connectionRegistry,
                        RealtimeBroadcaster realtimeBroadcaster) {
        this.connectionProvider = connectionProvider;
        this.repositoryFactory = repositoryFactory;
        this.connectionRegistry = connectionRegistry;
        this.realtimeBroadcaster = realtimeBroadcaster;
    }

    private void validateAdmin(ClientSession session) {
        if (session == null || !session.isAuthenticated()) {
            throw new ValidationException("User must be logged in");
        }
        if (session.getPosition() != Position.ADMIN) {
            throw new ValidationException("Permission denied. Admin role required.");
        }
    }

    private void validateAdminTarget(UserRepository userRepository, long targetUserId, ClientSession session) throws SQLException {
        if (targetUserId == session.getUserId()) {
            throw new ValidationException("You cannot perform administrative actions on yourself.");
        }
        Optional<User> targetUserOpt = userRepository.findById(targetUserId);
        if (targetUserOpt.isPresent()) {
            User targetUser = targetUserOpt.get();
            if (targetUser.getPosition() == Position.ADMIN) {
                throw new ValidationException("You cannot perform administrative actions on another administrator.");
            }
        }
    }

    public AdminUserListResponse getAllUsers(ClientSession session) throws SQLException {
        validateAdmin(session);
        try (Connection connection = connectionProvider.getConnection()) {
            UserRepository userRepository = repositoryFactory.createUserRepository(connection);
            List<User> users = userRepository.findAll();
            List<AdminUserItem> userItems = new ArrayList<>();
            for (User u : users) {
                userItems.add(new AdminUserItem(
                    u.getId(), u.getUserName(), u.getEmail(), u.getPosition(), 
                    u.getUserStatus(), u.getWarningCount(), 
                    u.getLockUntil() != null ? u.getLockUntil().toString() : null
                ));
            }
            return new AdminUserListResponse(userItems);
        }
    }

    public AdminAuctionListResponse getAllAuctions(ClientSession session) throws SQLException {
        validateAdmin(session);
        try (Connection connection = connectionProvider.getConnection()) {
            AuctionRepository auctionRepository = repositoryFactory.createAuctionRepository(connection);
            List<Auction> auctions = auctionRepository.findAllForAdmin();
            List<AdminAuctionItem> auctionItems = new ArrayList<>();
            for (Auction a : auctions) {
                auctionItems.add(new AdminAuctionItem(
                    a.getId(), a.getSellerId(), a.getTitle(), "Product-" + a.getProductId(), 
                    a.getStatus(), a.getCurrentPrice(), a.getStartingTime(), a.getEndingTime()
                ));
            }
            return new AdminAuctionListResponse(auctionItems);
        }
    }

    public void banUser(AdminUserActionRequest request, ClientSession session) throws SQLException {
        validateAdmin(session);
        try (Connection connection = connectionProvider.getConnection()) {
            UserRepository userRepository = repositoryFactory.createUserRepository(connection);
            validateAdminTarget(userRepository, request.getTargetUserId(), session);
            
            // Calculate ban duration
            LocalDateTime lockUntil = LocalDateTime.now();
            int amount = request.getDuration() != null ? request.getDuration() : 0;
            String unit = request.getDurationUnit() != null ? request.getDurationUnit().toUpperCase() : "MINUTE";
            switch (unit) {
                case "MINUTE" -> lockUntil = lockUntil.plusMinutes(amount);
                case "HOUR" -> lockUntil = lockUntil.plusHours(amount);
                case "DAY" -> lockUntil = lockUntil.plusDays(amount);
                case "YEAR" -> lockUntil = lockUntil.plusYears(amount);
                default -> throw new ValidationException("Invalid ban duration unit: " + unit);
            }
            
            userRepository.updateStatus(request.getTargetUserId(), UserStatus.BANNED);
            userRepository.setLockUntil(request.getTargetUserId(), lockUntil);
            
            logAdminAction(connection, session.getUserId(), request.getTargetUserId(), null, "BAN_USER", request.getReason() + " - Duration: " + amount + " " + unit);
            connectionRegistry.disconnectUser(request.getTargetUserId());
            broadcastEvent(RealtimeEventType.ADMIN_USER_KICKED, RoomType.USER, request.getTargetUserId(), null);
            broadcastEvent(RealtimeEventType.ADMIN_USER_STATUS_CHANGED, RoomType.USER, request.getTargetUserId(), null);
        }
    }

    public void kickUser(AdminUserActionRequest request, ClientSession session) throws SQLException {
        validateAdmin(session);
        try (Connection connection = connectionProvider.getConnection()) {
            UserRepository userRepository = repositoryFactory.createUserRepository(connection);
            validateAdminTarget(userRepository, request.getTargetUserId(), session);
            logAdminAction(connection, session.getUserId(), request.getTargetUserId(), null, "KICK_USER", request.getReason());
            connectionRegistry.disconnectUser(request.getTargetUserId());
            broadcastEvent(RealtimeEventType.ADMIN_USER_KICKED, RoomType.USER, request.getTargetUserId(), null);
        }
    }

    public void warnUser(AdminUserActionRequest request, ClientSession session) throws SQLException {
        validateAdmin(session);
        try (Connection connection = connectionProvider.getConnection()) {
            UserRepository userRepository = repositoryFactory.createUserRepository(connection);
            validateAdminTarget(userRepository, request.getTargetUserId(), session);
            Optional<User> userOpt = userRepository.findById(request.getTargetUserId());
            if (userOpt.isPresent()) {
                User user = userOpt.get();
                if (user.getWarningCount() >= 2) { // Will reach 3
                    userRepository.updateStatus(request.getTargetUserId(), UserStatus.BANNED);
                    userRepository.setLockUntil(request.getTargetUserId(), LocalDateTime.now().plusYears(1));
                    userRepository.incrementWarningCount(request.getTargetUserId());
                    logAdminAction(connection, session.getUserId(), request.getTargetUserId(), null, "BAN_USER", "Auto-banned due to 3 warnings. Reason: " + request.getReason());
                    connectionRegistry.disconnectUser(request.getTargetUserId());
                    broadcastEvent(RealtimeEventType.ADMIN_USER_KICKED, RoomType.USER, request.getTargetUserId(), null);
                    broadcastEvent(RealtimeEventType.ADMIN_USER_STATUS_CHANGED, RoomType.USER, request.getTargetUserId(), null);
                } else {
                    int newCount = user.getWarningCount() + 1;
                    userRepository.incrementWarningCount(request.getTargetUserId());
                    logAdminAction(connection, session.getUserId(), request.getTargetUserId(), null, "WARN_USER", request.getReason());
                    broadcastEvent(
                        RealtimeEventType.ADMIN_USER_WARNED, 
                        RoomType.USER, 
                        request.getTargetUserId(), 
                        new UserWarnedPayload(request.getTargetUserId(), request.getReason(), newCount)
                    );
                }
            }
        }
    }

    public void stopAuction(AdminAuctionActionRequest request, ClientSession session) throws SQLException {
        validateAdmin(session);
        try (Connection connection = connectionProvider.getConnection()) {
            AuctionRepository auctionRepository = repositoryFactory.createAuctionRepository(connection);
            auctionRepository.updateStatus(request.getAuctionId(), AuctionStatus.STOPPED);
            logAdminAction(connection, session.getUserId(), null, request.getAuctionId(), "STOP_AUCTION", request.getReason());
            broadcastEvent(RealtimeEventType.AUCTION_LIST_ITEM_UPDATED, RoomType.AUCTION_LIST, 0L, null);
            broadcastAuctionStateChange(connection, request.getAuctionId());
        }
    }

    public void continueAuction(AdminAuctionActionRequest request, ClientSession session) throws SQLException {
        validateAdmin(session);
        try (Connection connection = connectionProvider.getConnection()) {
            AuctionRepository auctionRepository = repositoryFactory.createAuctionRepository(connection);
            auctionRepository.updateStatus(request.getAuctionId(), AuctionStatus.ACTIVE);
            logAdminAction(connection, session.getUserId(), null, request.getAuctionId(), "CONTINUE_AUCTION", request.getReason());
            broadcastEvent(RealtimeEventType.AUCTION_LIST_ITEM_UPDATED, RoomType.AUCTION_LIST, 0L, null);
            broadcastAuctionStateChange(connection, request.getAuctionId());
        }
    }

    public void deleteAuction(AdminAuctionActionRequest request, ClientSession session) throws SQLException {
        validateAdmin(session);
        try (Connection connection = connectionProvider.getConnection()) {
            AuctionRepository auctionRepository = repositoryFactory.createAuctionRepository(connection);
            auctionRepository.updateStatus(request.getAuctionId(), AuctionStatus.CANCELLED);
            logAdminAction(connection, session.getUserId(), null, request.getAuctionId(), "DELETE_AUCTION", request.getReason());
            broadcastEvent(RealtimeEventType.AUCTION_LIST_ITEM_UPDATED, RoomType.AUCTION_LIST, 0L, null);
            broadcastAuctionStateChange(connection, request.getAuctionId());
        }
    }

    private void logAdminAction(Connection connection, long adminId, Long targetUserId, Long targetAuctionId, String actionType, String reason) {
        String sql = "INSERT INTO admin_actions_log (admin_id, target_user_id, target_auction_id, action_type, reason) VALUES (?, ?, ?, ?, ?)";
        try (java.sql.PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setLong(1, adminId);
            if (targetUserId != null) statement.setLong(2, targetUserId); else statement.setNull(2, java.sql.Types.BIGINT);
            if (targetAuctionId != null) statement.setLong(3, targetAuctionId); else statement.setNull(3, java.sql.Types.BIGINT);
            statement.setString(4, actionType);
            statement.setString(5, reason);
            statement.executeUpdate();
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Failed to log admin action", e);
        }
    }

    private void broadcastEvent(RealtimeEventType type, RoomType roomType, long targetId, Object payload) {
        Room room = new Room();
        room.setType(roomType);
        room.setTargetId(targetId);
        realtimeBroadcaster.broadcast(new RealtimeEvent<>(type, room, payload));
    }

    private void broadcastAuctionStateChange(Connection connection, long auctionId) throws SQLException {
        AuctionRepository auctionRepository = repositoryFactory.createAuctionRepository(connection);
        Optional<Auction> auctionOpt = auctionRepository.findById(auctionId);
        if (auctionOpt.isEmpty()) {
            return;
        }
        Auction auction = auctionOpt.get();

        // 1. Broadcast AUCTION_STATE_UPDATED to room auction:auctionId
        com.vbay.shared.dto.realtimeDTO.payload.AuctionStatePayload statePayload = new com.vbay.shared.dto.realtimeDTO.payload.AuctionStatePayload();
        statePayload.setAuctionId(auction.getId());
        statePayload.setAuctionVersion(auction.getVersion());
        statePayload.setStatus(auction.getStatus().name());
        statePayload.setCurrentPrice(auction.getCurrentPrice());
        statePayload.setWinnerUserId(auction.getWinnerUserId());
        statePayload.setStartingTime(auction.getStartingTime());
        statePayload.setEndingTime(auction.getEndingTime());
        statePayload.setUpdatedAt(LocalDateTime.now());
        boolean reserveMet = auction.getReservePrice() == null || (auction.getCurrentPrice() != null && auction.getCurrentPrice().compareTo(auction.getReservePrice()) >= 0);
        statePayload.setReserveMet(reserveMet);
        statePayload.setAntiSnipeExtended(auction.getAntiSnipeExtensionCount() > 0);

        broadcastEvent(RealtimeEventType.AUCTION_STATE_UPDATED, RoomType.AUCTION, auction.getId(), statePayload);

        // 2. Broadcast MY_BID_LIST_ITEM_UPDATED to room user:userId of every user who has bid on this auction.
        com.vbay.server.repository.BidRepository bidRepository = repositoryFactory.createBidRepository(connection);
        com.vbay.server.repository.ProductImageRepository productImageRepository = repositoryFactory.createProductImageRepository(connection);
        com.vbay.server.repository.AutobidRepository autobidRepository = repositoryFactory.createAutobidRepository(connection);

        String thumbnailUrl = productImageRepository.findThumbnailUrlByProductId(auction.getProductId()).orElse(null);
        List<com.vbay.server.model.Bid> latestBids = bidRepository.findLatestBidPerBidderByAuctionId(auction.getId());

        for (com.vbay.server.model.Bid bid : latestBids) {
            BigDecimal maxBidAmount = null;
            if (bid.getBidSource() == com.vbay.shared.enums.bid.BidSource.AUTO_BID && bid.getStatus() == com.vbay.shared.enums.bid.BidStatus.WINNING) {
                maxBidAmount = autobidRepository.findByAuctionIdAndUserId(bid.getAuctionId(), bid.getBidderId())
                    .map(com.vbay.server.model.Autobid::getMaxBidAmount)
                    .orElse(null);
            }

            com.vbay.server.service.result.UserMyBidListItemResult res = com.vbay.server.service.result.mapper.ResultMapper.toUserMyBidListItemResult(
                auction,
                thumbnailUrl,
                bid,
                maxBidAmount,
                bid.getStatus(),
                LocalDateTime.now()
            );

            com.vbay.shared.dto.realtimeDTO.payload.MyBidListItemPayload myBidPayload = new com.vbay.shared.dto.realtimeDTO.payload.MyBidListItemPayload(
                res.getBidId(),
                res.getAuctionId(),
                res.getAuctionVersion(),
                res.getAuctionTitle(),
                res.getThumbnailUrl(),
                res.getCurrentPrice(),
                res.getAuctionStatus().name(),
                res.isAntiSnipeExtended(),
                res.getMyBidAmount(),
                res.getMyMaxBidAmount(),
                res.getBidStatus(),
                res.getBidSource(),
                res.getBidTime(),
                res.getStartingTime(),
                res.getEndingTime(),
                res.getUpdatedAt()
            );

            broadcastEvent(RealtimeEventType.MY_BID_LIST_ITEM_UPDATED, RoomType.USER, bid.getBidderId(), myBidPayload);
        }
    }

    public Respond<AdminUserListResponse> handleAdminGetAllUsers(String requestId, ClientSession session) throws SQLException {
        return new Respond<>(requestId, true, "Admin users loaded", getAllUsers(session));
    }

    public Respond<AdminAuctionListResponse> handleAdminGetAllAuctions(String requestId, ClientSession session) throws SQLException {
        return new Respond<>(requestId, true, "Admin auctions loaded", getAllAuctions(session));
    }

    public Respond<Void> handleAdminBanUser(String requestId, JsonElement payload, ClientSession session) throws SQLException {
        banUser(JsonUtils.fromJson(payload, AdminUserActionRequest.class), session);
        return new Respond<>(requestId, true, "User banned", null);
    }

    public Respond<Void> handleAdminKickUser(String requestId, JsonElement payload, ClientSession session) throws SQLException {
        kickUser(JsonUtils.fromJson(payload, AdminUserActionRequest.class), session);
        return new Respond<>(requestId, true, "User kicked", null);
    }

    public Respond<Void> handleAdminWarnUser(String requestId, JsonElement payload, ClientSession session) throws SQLException {
        warnUser(JsonUtils.fromJson(payload, AdminUserActionRequest.class), session);
        return new Respond<>(requestId, true, "User warned", null);
    }



    public Respond<Void> handleAdminDeleteAuction(String requestId, JsonElement payload, ClientSession session) throws SQLException {
        deleteAuction(JsonUtils.fromJson(payload, AdminAuctionActionRequest.class), session);
        return new Respond<>(requestId, true, "Auction deleted", null);
    }

    public Respond<Void> handleAdminStopAuction(String requestId, JsonElement payload, ClientSession session) throws SQLException {
        stopAuction(JsonUtils.fromJson(payload, AdminAuctionActionRequest.class), session);
        return new Respond<>(requestId, true, "Auction stopped", null);
    }

    public Respond<Void> handleAdminContinueAuction(String requestId, JsonElement payload, ClientSession session) throws SQLException {
        continueAuction(JsonUtils.fromJson(payload, AdminAuctionActionRequest.class), session);
        return new Respond<>(requestId, true, "Auction continued", null);
    }

    public Respond<AdminDepositListResponse> handleAdminGetPendingDeposits(String requestId, ClientSession session) throws SQLException {
        validateAdmin(session);
        try (Connection connection = connectionProvider.getConnection()) {
            DepositRequestRepository depositRepo = repositoryFactory.createDepositRequestRepository(connection);
            List<com.vbay.server.model.DepositRequestRow> rows = depositRepo.findAllPending();
            List<AdminDepositItem> items = new ArrayList<>();
            for (com.vbay.server.model.DepositRequestRow r : rows) {
                items.add(new AdminDepositItem(
                    r.getId(),
                    r.getUserId(),
                    r.getUsername(),
                    r.getAmount(),
                    DepositRequestStatus.PENDING,
                    r.getCreatedAt() != null ? r.getCreatedAt().toString() : ""
                ));
            }
            return new Respond<>(requestId, true, "Pending deposits loaded", new AdminDepositListResponse(items));
        }
    }

    public Respond<Void> handleAdminApproveDeposit(String requestId, JsonElement payload, ClientSession session) throws SQLException {
        validateAdmin(session);
        AdminDepositActionRequest request = JsonUtils.fromJson(payload, AdminDepositActionRequest.class);
        if (request == null) {
            throw new ValidationException("Invalid approve deposit payload");
        }
        try (Connection connection = connectionProvider.getConnection()) {
            connection.setAutoCommit(false);
            try {
                DepositRequestRepository depositRepo = repositoryFactory.createDepositRequestRepository(connection);
                UserRepository userRepo = repositoryFactory.createUserRepository(connection);
                
                Optional<com.vbay.server.model.DepositRequestRow> rowOpt = depositRepo.findById(request.getDepositId());
                if (rowOpt.isEmpty()) {
                    throw new ValidationException("Deposit request not found");
                }
                com.vbay.server.model.DepositRequestRow row = rowOpt.get();
                if (!"PENDING".equals(row.getStatus())) {
                    throw new ValidationException("Deposit request is already processed");
                }

                depositRepo.updateStatus(request.getDepositId(), "APPROVED", session.getUserId());
                long userVersion = userRepo.depositAvailableBalance(row.getUserId(), row.getAmount());

                Optional<User> userOpt = userRepo.findById(row.getUserId());
                if (userOpt.isPresent()) {
                    User updatedUser = userOpt.get();
                    UserBalanceUpdatedPayload balancePayload = new UserBalanceUpdatedPayload(
                        updatedUser.getId(),
                        userVersion,
                        updatedUser.getAvailableBalance(),
                        updatedUser.getHoldBalance(),
                        "Deposit approved by Admin",
                        LocalDateTime.now()
                    );
                    broadcastEvent(RealtimeEventType.USER_BALANCE_UPDATED, RoomType.USER, updatedUser.getId(), balancePayload);
                }

                DepositRequestPayload depositPayload = new DepositRequestPayload(
                    row.getId(),
                    row.getUserId(),
                    row.getUsername(),
                    row.getAmount(),
                    "APPROVED",
                    "Admin has accepted your deposit request of " + row.getAmount()
                );
                broadcastEvent(RealtimeEventType.DEPOSIT_REQUEST_UPDATED, RoomType.USER, row.getUserId(), depositPayload);

                connection.commit();
                return new Respond<>(requestId, true, "Deposit approved successfully", null);
            } catch (Exception e) {
                connection.rollback();
                throw e;
            }
        }
    }

    public Respond<Void> handleAdminRejectDeposit(String requestId, JsonElement payload, ClientSession session) throws SQLException {
        validateAdmin(session);
        AdminDepositActionRequest request = JsonUtils.fromJson(payload, AdminDepositActionRequest.class);
        if (request == null) {
            throw new ValidationException("Invalid reject deposit payload");
        }
        try (Connection connection = connectionProvider.getConnection()) {
            DepositRequestRepository depositRepo = repositoryFactory.createDepositRequestRepository(connection);
            Optional<com.vbay.server.model.DepositRequestRow> rowOpt = depositRepo.findById(request.getDepositId());
            if (rowOpt.isEmpty()) {
                throw new ValidationException("Deposit request not found");
            }
            com.vbay.server.model.DepositRequestRow row = rowOpt.get();
            if (!"PENDING".equals(row.getStatus())) {
                throw new ValidationException("Deposit request is already processed");
            }

            depositRepo.updateStatus(request.getDepositId(), "REJECTED", session.getUserId());

            DepositRequestPayload depositPayload = new DepositRequestPayload(
                row.getId(),
                row.getUserId(),
                row.getUsername(),
                row.getAmount(),
                "REJECTED",
                "Admin has rejected your deposit request of " + row.getAmount()
            );
            broadcastEvent(RealtimeEventType.DEPOSIT_REQUEST_UPDATED, RoomType.USER, row.getUserId(), depositPayload);

            return new Respond<>(requestId, true, "Deposit rejected successfully", null);
        }
    }
}

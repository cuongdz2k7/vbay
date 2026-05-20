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
            userRepository.updateStatus(request.getTargetUserId(), UserStatus.BANNED);
            logAdminAction(connection, session.getUserId(), request.getTargetUserId(), null, "BAN_USER", request.getReason());
            connectionRegistry.disconnectUser(request.getTargetUserId());
            broadcastEvent(RealtimeEventType.ADMIN_USER_KICKED, RoomType.USER, request.getTargetUserId(), null);
        }
    }

    public void unbanUser(AdminUserActionRequest request, ClientSession session) throws SQLException {
        validateAdmin(session);
        try (Connection connection = connectionProvider.getConnection()) {
            UserRepository userRepository = repositoryFactory.createUserRepository(connection);
            userRepository.updateStatus(request.getTargetUserId(), UserStatus.ACTIVE);
            logAdminAction(connection, session.getUserId(), request.getTargetUserId(), null, "UNBAN_USER", request.getReason());
        }
    }

    public void kickUser(AdminUserActionRequest request, ClientSession session) throws SQLException {
        validateAdmin(session);
        try (Connection connection = connectionProvider.getConnection()) {
            logAdminAction(connection, session.getUserId(), request.getTargetUserId(), null, "KICK_USER", request.getReason());
            connectionRegistry.disconnectUser(request.getTargetUserId());
            broadcastEvent(RealtimeEventType.ADMIN_USER_KICKED, RoomType.USER, request.getTargetUserId(), null);
        }
    }

    public void warnUser(AdminUserActionRequest request, ClientSession session) throws SQLException {
        validateAdmin(session);
        try (Connection connection = connectionProvider.getConnection()) {
            UserRepository userRepository = repositoryFactory.createUserRepository(connection);
            Optional<User> userOpt = userRepository.findById(request.getTargetUserId());
            if (userOpt.isPresent()) {
                User user = userOpt.get();
                if (user.getWarningCount() >= 2) { // Will reach 3
                    userRepository.updateStatus(request.getTargetUserId(), UserStatus.BANNED);
                    userRepository.incrementWarningCount(request.getTargetUserId());
                    logAdminAction(connection, session.getUserId(), request.getTargetUserId(), null, "BAN_USER", "Auto-banned due to 3 warnings. Reason: " + request.getReason());
                    connectionRegistry.disconnectUser(request.getTargetUserId());
                } else {
                    userRepository.incrementWarningCount(request.getTargetUserId());
                    logAdminAction(connection, session.getUserId(), request.getTargetUserId(), null, "WARN_USER", request.getReason());
                }
            }
        }
    }

    public void lockUser(AdminLockUserRequest request, ClientSession session) throws SQLException {
        validateAdmin(session);
        try (Connection connection = connectionProvider.getConnection()) {
            UserRepository userRepository = repositoryFactory.createUserRepository(connection);
            userRepository.updateStatus(request.getTargetUserId(), UserStatus.LOCKED);
            LocalDateTime lockUntil = LocalDateTime.now().plusMinutes(request.getLockDurationMinutes());
            userRepository.setLockUntil(request.getTargetUserId(), lockUntil);
            logAdminAction(connection, session.getUserId(), request.getTargetUserId(), null, "LOCK_USER", request.getReason() + " - Duration: " + request.getLockDurationMinutes() + " mins");
            connectionRegistry.disconnectUser(request.getTargetUserId());
            broadcastEvent(RealtimeEventType.ADMIN_USER_KICKED, RoomType.USER, request.getTargetUserId(), null);
        }
    }

    public void stopAuction(AdminAuctionActionRequest request, ClientSession session) throws SQLException {
        validateAdmin(session);
        try (Connection connection = connectionProvider.getConnection()) {
            AuctionRepository auctionRepository = repositoryFactory.createAuctionRepository(connection);
            auctionRepository.updateStatus(request.getAuctionId(), AuctionStatus.STOPPED);
            logAdminAction(connection, session.getUserId(), null, request.getAuctionId(), "STOP_AUCTION", request.getReason());
            broadcastEvent(RealtimeEventType.AUCTION_LIST_ITEM_UPDATED, RoomType.AUCTION_LIST, 0L, null);
        }
    }

    public void continueAuction(AdminAuctionActionRequest request, ClientSession session) throws SQLException {
        validateAdmin(session);
        try (Connection connection = connectionProvider.getConnection()) {
            AuctionRepository auctionRepository = repositoryFactory.createAuctionRepository(connection);
            auctionRepository.updateStatus(request.getAuctionId(), AuctionStatus.ACTIVE);
            logAdminAction(connection, session.getUserId(), null, request.getAuctionId(), "CONTINUE_AUCTION", request.getReason());
            broadcastEvent(RealtimeEventType.AUCTION_LIST_ITEM_UPDATED, RoomType.AUCTION_LIST, 0L, null);
        }
    }

    public void deleteAuction(AdminAuctionActionRequest request, ClientSession session) throws SQLException {
        validateAdmin(session);
        try (Connection connection = connectionProvider.getConnection()) {
            AuctionRepository auctionRepository = repositoryFactory.createAuctionRepository(connection);
            auctionRepository.updateStatus(request.getAuctionId(), AuctionStatus.CANCELLED);
            logAdminAction(connection, session.getUserId(), null, request.getAuctionId(), "DELETE_AUCTION", request.getReason());
            broadcastEvent(RealtimeEventType.AUCTION_LIST_ITEM_UPDATED, RoomType.AUCTION_LIST, 0L, null);
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

    public Respond<Void> handleAdminUnbanUser(String requestId, JsonElement payload, ClientSession session) throws SQLException {
        unbanUser(JsonUtils.fromJson(payload, AdminUserActionRequest.class), session);
        return new Respond<>(requestId, true, "User unbanned", null);
    }

    public Respond<Void> handleAdminKickUser(String requestId, JsonElement payload, ClientSession session) throws SQLException {
        kickUser(JsonUtils.fromJson(payload, AdminUserActionRequest.class), session);
        return new Respond<>(requestId, true, "User kicked", null);
    }

    public Respond<Void> handleAdminWarnUser(String requestId, JsonElement payload, ClientSession session) throws SQLException {
        warnUser(JsonUtils.fromJson(payload, AdminUserActionRequest.class), session);
        return new Respond<>(requestId, true, "User warned", null);
    }

    public Respond<Void> handleAdminLockUser(String requestId, JsonElement payload, ClientSession session) throws SQLException {
        lockUser(JsonUtils.fromJson(payload, AdminLockUserRequest.class), session);
        return new Respond<>(requestId, true, "User locked", null);
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
}

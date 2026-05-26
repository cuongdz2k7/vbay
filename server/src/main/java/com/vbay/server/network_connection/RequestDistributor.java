package com.vbay.server.network_connection;
import java.io.IOException;
import java.math.BigDecimal;
import java.sql.SQLException;
import java.util.logging.Level;
import java.util.logging.Logger;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.vbay.server.exception.AuthenticationException;
import com.vbay.server.exception.ValidationException;
import com.vbay.server.realtime.subscription.SubscriptionService;
import com.vbay.server.service.AdminService;
import com.vbay.server.service.AuctionService;
import com.vbay.server.service.AuthService;
import com.vbay.server.service.UserAccountService;
import com.vbay.server.service.bid.AutobidService;
import com.vbay.server.service.bid.BidQueryService;
import com.vbay.server.service.bid.BuyNowService;
import com.vbay.server.service.bid.ManualBidService;
import com.vbay.server.service.result.AutobidRegistrationResult;
import com.vbay.server.service.result.UserBalanceResult;
import com.vbay.server.service.result.mapper.ResultMapper;
import com.vbay.server.upload.ImageStorageService;
import com.vbay.shared.Utils.JsonUtils;
import com.vbay.shared.Utils.LoggingUtils;
import com.vbay.shared.dto.auctionDTO.AuctionListRequest;
import com.vbay.shared.dto.auctionDTO.AuctionListResponse;
import com.vbay.shared.dto.auctionDTO.BuyNowRequest;
import com.vbay.shared.dto.auctionDTO.CreateAuctionRequest;
import com.vbay.shared.dto.auctionDTO.MyBidListResponse;
import com.vbay.shared.dto.auctionDTO.PlaceBidRequest;
import com.vbay.shared.dto.authDTO.LoginRequest;
import com.vbay.shared.dto.authDTO.LoginResponse;
import com.vbay.shared.dto.authDTO.RegisterRequest;
import com.vbay.shared.dto.productDTO.UploadImageRequest;
import com.vbay.shared.dto.realtimeDTO.Room;
import com.vbay.shared.dto.userDTO.DepositBalanceRequest;
import com.vbay.shared.dto.userDTO.UserBalanceResponse;
import com.vbay.shared.enums.RequestType;
import com.vbay.shared.protocol.Respond;
public class RequestDistributor {
    private static final Logger LOGGER = LoggingUtils.getLogger(RequestDistributor.class);
    private final AuthService authService;
    private final AuctionService auctionService;
    private final ManualBidService bidService;
    private final AutobidService autobidService;
    private final BuyNowService buyNowService;
    private final BidQueryService bidQueryService;
    private final UserAccountService userAccountService;
    private final SubscriptionService subscriptionService;
    private final ImageStorageService imageStorageService;
    private final AdminService adminService;
    private final ClientConnectionRegistry connectionRegistry;
    public RequestDistributor (AuthService authService, 
                                AuctionService auctionService, 
                                ManualBidService bidService, 
                                AutobidService autobidService,
                                BuyNowService buyNowService,
                                BidQueryService bidQueryService,
                                UserAccountService userAccountService,
                                SubscriptionService subscriptionService,
                                ImageStorageService imageStorageService,
                                AdminService adminService,
                                ClientConnectionRegistry connectionRegistry) {
        this.authService = authService;
        this.auctionService = auctionService;
        this.bidService = bidService;
        this.autobidService = autobidService;
        this.buyNowService = buyNowService;
        this.bidQueryService = bidQueryService;
        this.userAccountService = userAccountService;
        this.subscriptionService = subscriptionService;
        this.imageStorageService = imageStorageService;
        this.adminService = adminService;
        this.connectionRegistry = connectionRegistry;
    }
    public Respond<?> dispatch (String rawRequest, ClientSession session, ClientConnection connection) {
        JsonObject root;
        try {
            root = JsonUtils.fromJson(rawRequest, JsonObject.class);
        } catch (Exception e) {
            return new Respond<>(null, false, "Invalid request format", null);
        }
        if (root == null) {
            return new Respond<>(null, false, "Invalid request format", null);
        }
        
        JsonElement requestIdElement = root.get("requestId");
        JsonElement typeElement = root.get("requestType");
        if (typeElement == null || typeElement.isJsonNull()) {
            typeElement = root.get("type");
        }
        String requestId = (requestIdElement != null && !requestIdElement.isJsonNull()) 
                            ? requestIdElement.getAsString() : null;
        String typeRaw = (typeElement != null && !typeElement.isJsonNull()) ? typeElement.getAsString() : null;
        if (typeRaw == null || typeRaw.isBlank()) {
            return new Respond<>(requestId, false, "Invalid request type", null);
        }
        JsonElement payload = root.get("payload");
        RequestType type;
        try {
            type = RequestType.valueOf(typeRaw);
        } catch (IllegalArgumentException e) {
            return new Respond<>(requestId, false, "Unsupported request type: " + typeRaw, null);
        }
        try {
            return switch (type) {
                case VERIFY -> new Respond<>(requestId, true, "Server is reachable", payload);
                case LOGIN -> authService.handleLogin(requestId, payload, session);
                case LOGOUT -> {
                    subscriptionService.disconnect(connection);
                    yield authService.handleLogout(requestId, session);
                }
                case REGISTER -> authService.handleRegister(requestId, payload);
                case UPLOAD_IMAGE -> imageStorageService.handleUploadImage(requestId, payload, session);
                case CREATE_AUCTION -> auctionService.handleCreateAuction(requestId, payload, session);
                case GET_MY_BID_LIST -> bidQueryService.handleGetMyBidList(requestId, session);
                case PLACE_BID -> bidService.handlePlaceBid(requestId, payload, session);
                case AUTO_BID -> autobidService.handleAutoBid(requestId, payload, session);
                case INCREASE_AUTOBID_MAX -> autobidService.handleIncreaseAutobidMax(requestId, payload, session);
                case BUY_NOW -> buyNowService.handleBuyNow(requestId, payload, session);
                case DEPOSIT_BALANCE -> userAccountService.handleDepositBalance(requestId, payload, session);
                case SUBSCRIBE_ROOM -> subscriptionService.handleSubscribeRoom(requestId, payload, session, connection);
                case UNSUBSCRIBE_ROOM -> subscriptionService.handleUnsubscribeRoom(requestId, payload, session, connection);
                case GET_AUCTION_LIST -> auctionService.handleGetAuctionList(requestId, payload, session);
                case GET_AUCTION_DETAIL -> auctionService.handleGetAuctionDetail(requestId, payload, session);
                case GET_BID_HISTORY -> bidQueryService.handleGetBidHistory(requestId, payload, session);
                case ADMIN_GET_ALL_USERS -> adminService.handleAdminGetAllUsers(requestId, session);
                case ADMIN_GET_ALL_AUCTIONS -> adminService.handleAdminGetAllAuctions(requestId, session);
                case ADMIN_BAN_USER -> adminService.handleAdminBanUser(requestId, payload, session);
                case ADMIN_KICK_USER -> adminService.handleAdminKickUser(requestId, payload, session);
                case ADMIN_WARN_USER -> adminService.handleAdminWarnUser(requestId, payload, session);
                case ADMIN_DELETE_AUCTION -> adminService.handleAdminDeleteAuction(requestId, payload, session);
                case ADMIN_STOP_AUCTION -> adminService.handleAdminStopAuction(requestId, payload, session);
                case ADMIN_CONTINUE_AUCTION -> adminService.handleAdminContinueAuction(requestId, payload, session);
                case ADMIN_GET_PENDING_DEPOSITS -> adminService.handleAdminGetPendingDeposits(requestId, session);
                case ADMIN_APPROVE_DEPOSIT -> adminService.handleAdminApproveDeposit(requestId, payload, session);
                case ADMIN_REJECT_DEPOSIT -> adminService.handleAdminRejectDeposit(requestId, payload, session);
                default -> new Respond<>(requestId, false, "Request type not implemented yet", null);
            };
        } catch (ValidationException | AuthenticationException e) {
            LOGGER.log(Level.WARNING, "Request processing error: " + e.getMessage());
            return new Respond<>(requestId, false, e.getMessage(), null);
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Database error", e);
            return new Respond<>(requestId, false, "DataBase error", null);
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Unexpected error", e);
            return new Respond<>(requestId, false, "Unexpected error", null);
        }
    }
}

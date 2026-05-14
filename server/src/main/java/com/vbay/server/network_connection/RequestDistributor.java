package com.vbay.server.network_connection;

import java.io.IOException;
import java.sql.SQLException;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.vbay.server.exception.AuthenticationException;
import com.vbay.server.exception.ValidationException;
import com.vbay.server.realtime.subscription.SubscriptionService;
import com.vbay.server.service.AuctionService;
import com.vbay.server.service.AuthService;
import com.vbay.server.service.BidService;
import com.vbay.server.service.UserAccountService;
import com.vbay.server.service.result.UserBalanceResult;
import com.vbay.server.service.result.mapper.ResultMapper;
import com.vbay.server.upload.ImageStorageService;
import com.vbay.shared.Utils.JsonUtils;
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

/*
Auth flow detail: 
1. Handler/service làm việc bình thường
    validate
    gọi repository
xử lý business
2. Nếu có lỗi:
    throw ValidationException
    throw AuthenticationException
    throw SQLException
hoặc exception khác
3. Exception sẽ bubble lên dispatch()
4. dispatch() catch theo từng loại và convert thành Respond<>(..., false, ...)
5. Nếu không có exception nào bị throw:
    code chạy hết xuống cuối method
    return Respond<>(..., true, ...)

Tại sao distributor lại thread-safe ? 
- vì các local variable trong method dispatch là bản riêng của từng thread, ở trong các call stack khác nhau, nên không có sự chia sẻ dữ liệu nào giữa các thread.
- AuthService được thiết kế để thread-safe.

*/

public class RequestDistributor {
    private final AuthService authService;
    private final AuctionService auctionService;
    private final BidService bidService;
    private final UserAccountService userAccountService;
    private final SubscriptionService subscriptionService;
    private final ImageStorageService imageStorageService;
    

    public RequestDistributor (AuthService authService, 
                                AuctionService auctionService, 
                                BidService bidService, 
                                UserAccountService userAccountService,
                                SubscriptionService subscriptionService,
                                ImageStorageService imageStorageService) {
        this.authService = authService;
        this.auctionService = auctionService;
        this.bidService = bidService;
        this.userAccountService = userAccountService;
        this.subscriptionService = subscriptionService;
        this.imageStorageService = imageStorageService;
    }
    /*
    switch-case theo type để gọi handler tương ứng
    parse json
    đóng gói response vào Respond<> và trả về
    */

    ///dispatch
    public Respond<?> dispatch (String rawRequest, ClientSession session, ClientConnection connection) {
        ///refactoring lại cái chỗ này, tách riêng phần parse requestId và type ra, sau đó mới switch-case theo type để gọi handler tương ứng
        ///code smell ở chỗ này.
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
                case LOGIN -> handleLogin(requestId, payload, session);
                case LOGOUT -> handleLogout(requestId, session, connection);
                case REGISTER -> handleRegister(requestId, payload);
                case UPLOAD_IMAGE -> handleUploadImage(requestId, payload, session);
                case CREATE_AUCTION -> handleCreateAuction(requestId, payload, session);
                case GET_MY_BID_LIST -> handleGetMyBidList(requestId, session);
                case PLACE_BID -> handlePlaceBid(requestId, payload, session);
                case BUY_NOW -> handleBuyNow(requestId, payload, session);
                case DEPOSIT_BALANCE -> handleDepositBalance(requestId, payload, session);
                case SUBSCRIBE_ROOM -> handleSubscribeRoom(requestId, payload, session, connection);
                case UNSUBSCRIBE_ROOM -> handleUnsubscribeRoom(requestId, payload, session, connection);
                case GET_AUCTION_LIST -> handleGetAuctionList(requestId, payload, session);
                case GET_AUCTION_DETAIL -> handleGetAuctionDetail(requestId, payload, session);
                default -> new Respond<>(requestId, false, "Request type not implemented yet", null);
            };
        } catch (ValidationException | AuthenticationException e) {
            e.printStackTrace();
            return new Respond<>(requestId, false, e.getMessage(), null);
        } catch (SQLException e) {
            e.printStackTrace();
            return new Respond<>(requestId, false, "DataBase error", null);
        } catch (Exception e) {
            e.printStackTrace();
            return new Respond<>(requestId, false, "Unexpected error", null);
        }
    }


    private Respond<AuctionListResponse> handleGetAuctionList(
        String requestId,
        JsonElement payload,
        ClientSession session) throws SQLException {

        AuctionListRequest request = JsonUtils.fromJson(payload, AuctionListRequest.class);
        if (request == null) {
            return new Respond<>(requestId, false, "Invalid auction list request", null);
        }

        AuctionListResponse response = auctionService.getAuctionList(request, session);
        return new Respond<>(requestId, true, "Auction list loaded", response);
    }

    private Respond<?> handleGetAuctionDetail(
        String requestId,
        JsonElement payload,
        ClientSession session) throws SQLException {

        com.vbay.shared.dto.auctionDTO.AuctionDetailRequest request =
            JsonUtils.fromJson(payload, com.vbay.shared.dto.auctionDTO.AuctionDetailRequest.class);
        if (request == null) {
            return new Respond<>(requestId, false, "Invalid auction detail request", null);
        }

        return new Respond<>(requestId, true, "Auction detail loaded", auctionService.getAuctionDetail(request, session));
    }

    ///
    private Respond<Void> handleSubscribeRoom(
        String requestId,
        JsonElement payload,
        ClientSession session,
        ClientConnection connection
    ) {

        Room room = JsonUtils.fromJson(payload, Room.class);
        if (room == null) {
            return new Respond<>(requestId, false, "Invalid room", null);
        }

        subscriptionService.subscribe(room, session, connection);
        return new Respond<>(requestId, true, "Subscribed room successfully", null);
    }
    
    private Respond<Void> handleUnsubscribeRoom(
        String requestId,
        JsonElement payload,
        ClientSession session,
        ClientConnection connection) {

        Room room = JsonUtils.fromJson(payload, Room.class);
        if (room == null) {
            return new Respond<>(requestId, false, "Invalid unsubscribe room request", null);
        }

        subscriptionService.unsubscribe(room, session, connection);
        return new Respond<>(requestId, true, "Unsubscribed room successfully", null);
    }



    private Respond<LoginResponse> handleLogin(String requestId, JsonElement payload, ClientSession session) throws SQLException, AuthenticationException {
        if (session.isAuthenticated()) {
            throw new AuthenticationException("Client is already logged in");
        }
        LoginRequest loginRequest = JsonUtils.fromJson(payload, LoginRequest.class);
        if (loginRequest == null) {
            return new Respond<>(requestId, false, "Invalid login request", null);
        }
        LoginResponse loginResponse = authService.login(loginRequest); ///nếu catch được exception thì dừng luôn ở đây
        session.setSession(loginResponse.getUserId(), 
                            loginResponse.getUsername(), 
                            loginResponse.getPosition());
        
        return new Respond<>(requestId, true, "Login successful", loginResponse);
    }
    /// LogoutResponse not Available
    private Respond<Void> handleLogout(String requestId, ClientSession session, ClientConnection connection) {
        if (session == null || !session.isAuthenticated()) {
            return new Respond<>(requestId, true, "Client is already logged out", null);
        }
        subscriptionService.disconnect(connection);
        session.clearSession();
        return new Respond<>(requestId, true, "Logout successful", null);
    }

    private Respond<Void> handleRegister(String requestId, JsonElement payload) throws SQLException {
        RegisterRequest registerRequest = JsonUtils.fromJson(payload, RegisterRequest.class);
        if (registerRequest == null) {
            return new Respond<>(requestId, false, "Invalid register request", null);
        }
        authService.register(registerRequest);
        return new Respond<>(requestId, true, "Register successful", null);
    }

    private Respond<String> handleUploadImage(String requestId, JsonElement payload, ClientSession session) throws IOException {
        if (session == null || !session.isAuthenticated()) {
            throw new AuthenticationException("User must be logged in to upload images");
        }
        UploadImageRequest uploadRequest = JsonUtils.fromJson(payload, UploadImageRequest.class);
        if (uploadRequest == null) {
            return new Respond<>(requestId, false, "Invalid upload image request", null);
        }
        String imageUrl = imageStorageService.storeUploadedImage(uploadRequest);
        return new Respond<>(requestId, true, "Image uploaded successfully", imageUrl);
    }

    private Respond<Void> handleCreateAuction(String requestId, JsonElement payload, ClientSession session) throws SQLException {
        CreateAuctionRequest createAuctionRequest = JsonUtils.fromJson(payload, CreateAuctionRequest.class);
        if (createAuctionRequest == null) {
            return new Respond<>(requestId, false, "Invalid create auction request", null);
        }
        auctionService.createAuction(createAuctionRequest, session);
        return new Respond<>(requestId, true, "Auction created successfully", null);
    }

    private Respond<MyBidListResponse> handleGetMyBidList(String requestId, ClientSession session) throws SQLException {
        return new Respond<>(requestId, true, "My bid list loaded successfully", bidService.getMyBidList(session));
    }

    private Respond<UserBalanceResponse> handleDepositBalance(String requestId, JsonElement payload, ClientSession session) throws SQLException {
        DepositBalanceRequest depositRequest = JsonUtils.fromJson(payload, DepositBalanceRequest.class);
        if (depositRequest == null) {
            return new Respond<>(requestId, false, "Invalid deposit balance request", null);
        }
        UserBalanceResult result = userAccountService.depositBalance(depositRequest, session);
        return new Respond<>(requestId, true, "Balance deposited successfully", ResultMapper.toUserBalanceResponse(result));
    }

    private Respond<Void> handlePlaceBid (String requestId, JsonElement payload, ClientSession session) throws SQLException {
        PlaceBidRequest placeBidRequest = JsonUtils.fromJson(payload, PlaceBidRequest.class);
        if (placeBidRequest == null) {
            return new Respond<>(requestId, false, "Invalid place bid request", null);
        }
        bidService.placeBid(placeBidRequest, session);
        return new Respond<>(requestId, true, "Placed bid successfully", null);
    }

    private Respond<Void> handleBuyNow(String requestId, JsonElement payload, ClientSession session) throws SQLException {
        BuyNowRequest buyNowRequest = JsonUtils.fromJson(payload, BuyNowRequest.class);
        if (buyNowRequest == null) {
            return new Respond<>(requestId, false, "Invalid buy now request", null);
        }
        bidService.buyNow(buyNowRequest, session);
        return new Respond<>(requestId, true, "Buy now completed successfully", null);
    }
    
}

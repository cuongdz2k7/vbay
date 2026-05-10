package com.vbay.server.network_connection;

import java.sql.SQLException;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.vbay.server.exception.AuthenticationException;
import com.vbay.server.exception.ValidationException;
import com.vbay.server.realtime.subscription.SubscriptionService;
import com.vbay.server.service.AuctionService;
import com.vbay.server.service.AuthService;
import com.vbay.server.service.BidService;

import com.vbay.shared.Utils.JsonUtils;
import com.vbay.shared.dto.auctionDTO.CreateAuctionRequest;
import com.vbay.shared.dto.auctionDTO.PlaceBidRequest;
import com.vbay.shared.dto.authDTO.LoginRequest;
import com.vbay.shared.dto.authDTO.LoginResponse;
import com.vbay.shared.dto.authDTO.RegisterRequest;
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
    private final SubscriptionService subscriptionService;
    

    public RequestDistributor (AuthService authService, 
                                AuctionService auctionService, 
                                BidService bidService, 
                                SubscriptionService subscriptionService) {
        this.authService = authService;
        this.auctionService = auctionService;
        this.bidService = bidService;
        this.subscriptionService = subscriptionService;
    }
    /*
    switch-case theo type để gọi handler tương ứng
    parse json
    đóng gói response vào Respond<> và trả về
    */

    ///dispatch
    public Respond<?> dispatch (String rawRequest, ClientSession session) {
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
        JsonElement typeElement = root.get("type");

        String requestId = (requestIdElement != null && !requestIdElement.isJsonNull()) 
                            ? requestIdElement.getAsString() : null;

        String typeRaw = (typeElement != null && !typeElement.isJsonNull()) ? typeElement.getAsString() : null;

        if (typeRaw == null || typeRaw.isBlank()) {
            return new Respond<>(requestId, false, "Invalid request type", null);
        }

        RequestType type;
        try {
            type = RequestType.valueOf(typeRaw);
        } catch (IllegalArgumentException e) {
            return new Respond<>(requestId, false, "Unsupported request type: " + typeRaw, null);
        }

        JsonElement payload = root.get("payload");
        try {
            return switch (type) {
                case VERIFY -> new Respond<>(requestId, true, "Server is reachable", payload);
                case LOGIN -> handleLogin(requestId, payload, session);
                case LOGOUT -> handleLogout(requestId, session);
                case REGISTER -> handleRegister(requestId, payload);
                case CREATE_AUCTION -> handleCreateAuction(requestId, payload, session);
                case PLACE_BID -> handlePlaceBid(requestId, payload, session);
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

    

    
    ///bỏ chuyển rawString sang authservice
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
    private Respond<Void> handleLogout(String requestId, ClientSession session) {
        if (session == null || !session.isAuthenticated()) {
            return new Respond<>(requestId, true, "Client is already logged out", null);
        }
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
    private Respond<Void> handleCreateAuction(String requestId, JsonElement payload, ClientSession session) throws SQLException {
        CreateAuctionRequest createAuctionRequest = JsonUtils.fromJson(payload, CreateAuctionRequest.class);
        if (createAuctionRequest == null) {
            return new Respond<>(requestId, false, "Invalid create auction request", null);
        }
        auctionService.createAuction(createAuctionRequest, session);
        return new Respond<>(requestId, true, "Auction created successfully", null);
    }

    private Respond<Void> handlePlaceBid (String requestId, JsonElement payload, ClientSession session) throws SQLException {
        PlaceBidRequest placeBidRequest = JsonUtils.fromJson(payload, PlaceBidRequest.class);
        if (placeBidRequest == null) {
            return new Respond<>(requestId, false, "Invalid place bid request", null);
        }
        bidService.placeBid(placeBidRequest, session);
        return new Respond<>(requestId, true, "Placed bid successfully", null);
    }
    
}

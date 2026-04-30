package com.vbay.server.network_connection;

import java.sql.SQLException;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.vbay.server.exception.AuthenticationException;
import com.vbay.server.exception.ValidationException;
import com.vbay.server.service.AuctionService;
import com.vbay.server.service.AuthService;
import com.vbay.shared.Utils.JsonUtils;
import com.vbay.shared.dto.auctionDTO.CreateAuctionRequest;
import com.vbay.shared.dto.authDTO.LoginRequest;
import com.vbay.shared.dto.authDTO.LoginResponse;
import com.vbay.shared.dto.authDTO.RegisterRequest;
import com.vbay.shared.enums.RequestType;
import com.vbay.shared.protocol.Respond;

public class RequestDistributor {
    private final AuthService authService;
    private final AuctionService auctionService;

    public RequestDistributor(AuthService authService, AuctionService auctionService) {
        this.authService = authService;
        this.auctionService = auctionService;
    }

    public Respond<?> dispatch(String rawRequest, ClientSession session) {
        JsonObject root;
        try {
            root = JsonUtils.fromJson(rawRequest, JsonObject.class);
        } catch (Exception exception) {
            return new Respond<>(null, false, "Invalid request format", null);
        }
        if (root == null) {
            return new Respond<>(null, false, "Invalid request format", null);
        }

        JsonElement requestIdElement = root.get("requestId");
        JsonElement typeElement = root.get("type");

        String requestId = requestIdElement != null && !requestIdElement.isJsonNull()
            ? requestIdElement.getAsString()
            : null;
        String typeRaw = typeElement != null && !typeElement.isJsonNull()
            ? typeElement.getAsString()
            : null;

        if (typeRaw == null || typeRaw.isBlank()) {
            return new Respond<>(requestId, false, "Invalid request type", null);
        }

        RequestType type;
        try {
            type = RequestType.valueOf(typeRaw);
        } catch (IllegalArgumentException exception) {
            return new Respond<>(requestId, false, "Unsupported request type: " + typeRaw, null);
        }

        JsonElement payload = root.get("payload");
        try {
            return switch (type) {
                case VERIFY -> new Respond<>(requestId, true, "Server is reachable", payload);
                case LOGIN -> handleLogin(requestId, payload, session);
                case REGISTER -> handleRegister(requestId, payload);
                case CREATE_AUCTION -> handleCreateAuction(requestId, payload, session);
                default -> new Respond<>(requestId, false, "Request type not implemented yet", null);
            };
        } catch (ValidationException | AuthenticationException exception) {
            exception.printStackTrace();
            return new Respond<>(requestId, false, exception.getMessage(), null);
        } catch (SQLException exception) {
            exception.printStackTrace();
            return new Respond<>(requestId, false, "DataBase error", null);
        } catch (Exception exception) {
            exception.printStackTrace();
            return new Respond<>(requestId, false, "Unexpected error", null);
        }
    }

    private Respond<LoginResponse> handleLogin(String requestId, JsonElement payload, ClientSession session)
        throws SQLException, AuthenticationException {
        LoginRequest loginRequest = JsonUtils.fromJson(payload, LoginRequest.class);
        if (loginRequest == null) {
            return new Respond<>(requestId, false, "Invalid login request", null);
        }
        LoginResponse loginResponse = authService.login(loginRequest);
        session.setSession(loginResponse.getUserId(), loginResponse.getUsername(), loginResponse.getPosition());
        return new Respond<>(requestId, true, "Login successful", loginResponse);
    }

    private Respond<Void> handleRegister(String requestId, JsonElement payload) throws SQLException {
        RegisterRequest registerRequest = JsonUtils.fromJson(payload, RegisterRequest.class);
        if (registerRequest == null) {
            return new Respond<>(requestId, false, "Invalid register request", null);
        }
        authService.register(registerRequest);
        return new Respond<>(requestId, true, "Register successful", null);
    }

    private Respond<Void> handleCreateAuction(String requestId, JsonElement payload, ClientSession session)
        throws SQLException {
        CreateAuctionRequest createAuctionRequest = JsonUtils.fromJson(payload, CreateAuctionRequest.class);
        if (createAuctionRequest == null) {
            return new Respond<>(requestId, false, "Invalid create auction request", null);
        }
        auctionService.createAuction(createAuctionRequest, session);
        return new Respond<>(requestId, true, "Auction created successfully", null);
    }
}

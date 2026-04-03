package com.vbay.server.distributor;

import java.sql.SQLException;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.vbay.server.exception.AuthenticationException;
import com.vbay.server.exception.ValidationException;
import com.vbay.server.service.AuthService;
import com.vbay.shared.Utils.JsonUtils;
import com.vbay.shared.dto.LoginRequest;
import com.vbay.shared.dto.LoginResponse;
import com.vbay.shared.dto.RegisterRequest;
import com.vbay.shared.enums.RequestType;
import com.vbay.shared.protocol.Respond;



public class RequestDistributor {
    private final AuthService authService;

    public RequestDistributor (AuthService authService) {
        this.authService = authService;
    }

    ///dispatch
    public Respond<?> dispatch (String rawRequest) {
        JsonObject root;
        try {
            root = JsonUtils.fromJson(rawRequest, JsonObject.class);
        } catch (Exception e) {
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
                case LOGIN -> handleLogin(requestId, payload);
                case REGISTER -> handleRegister(requestId, payload);
                default -> new Respond<>(requestId, false, "Request type not implemented yet", null);
            };
        } catch (ValidationException | AuthenticationException e) {
            return new Respond<>(requestId, false, e.getMessage(), null);
        } catch (SQLException e) {
            return new Respond<>(requestId, false, "DataBase error", null);
        } catch (Exception e) {
            return new Respond<>(requestId, false, "Unexpected error", null);
        }
    }

    

    
    ///handler
    private Respond<LoginResponse> handleLogin(String requestId, JsonElement payload) throws SQLException, AuthenticationException {
        LoginRequest loginRequest = JsonUtils.fromJson(payload, LoginRequest.class);
        if (loginRequest == null) {
            return new Respond<>(requestId, false, "Invalid login request", null);
        }
        LoginResponse loginResponse = authService.login(loginRequest);
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
    
}

package com.vbay.network;

import com.vbay.shared.dto.authDTO.LoginResponse;
import com.vbay.shared.enums.auth.Position;

public final class ClientAuthSession {
    private static Long userId;
    private static String username;
    private static String email;
    private static Position position;

    private ClientAuthSession() {
    }

    public static void setLoginResponse(LoginResponse response) {
        userId = response.getUserId();
        username = response.getUsername();
        email = response.getEmail();
        position = response.getPosition();
    }

    public static void clear() {
        userId = null;
        username = null;
        email = null;
        position = null;
    }

    public static Long getUserId() {
        return userId;
    }

    public static String getUsername() {
        return username;
    }

    public static String getEmail() {
        return email;
    }

    public static Position getPosition() {
        return position;
    }
}

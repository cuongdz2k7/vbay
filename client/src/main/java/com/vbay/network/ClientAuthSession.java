package com.vbay.network;

import java.math.BigDecimal;

import com.vbay.shared.dto.authDTO.LoginResponse;
import com.vbay.shared.enums.auth.Position;

public final class ClientAuthSession {
    private static Long userId;
    private static String username;
    private static String email;
    private static Position position;
    private static BigDecimal availableBalance;
    private static BigDecimal holdBalance;

    private ClientAuthSession() {
    }

    public static void setLoginResponse(LoginResponse response) {
        userId = response.getUserId();
        username = response.getUsername();
        email = response.getEmail();
        position = response.getPosition();
        availableBalance = response.getAvailableBalance();
        holdBalance = response.getHoldBalance();
    }

    public static void setBalances(BigDecimal newAvailableBalance, BigDecimal newHoldBalance) {
        availableBalance = newAvailableBalance;
        holdBalance = newHoldBalance;
    }

    public static void clear() {
        userId = null;
        username = null;
        email = null;
        position = null;
        availableBalance = null;
        holdBalance = null;
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

    public static BigDecimal getAvailableBalance() {
        return availableBalance;
    }

    public static BigDecimal getHoldBalance() {
        return holdBalance;
    }
}

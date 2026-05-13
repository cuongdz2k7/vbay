package com.vbay.shared.dto.authDTO;

import java.math.BigDecimal;

import com.vbay.shared.enums.auth.Position;


public class LoginResponse {
    private long userId;
    private String username;
    private String email;
    private Position position;
    private BigDecimal availableBalance;
    private BigDecimal holdBalance;

    public LoginResponse(long userId, String username, String email, Position position) {
        this(userId, username, email, position, null, null);
    }

    public LoginResponse(
            long userId,
            String username,
            String email,
            Position position,
            BigDecimal availableBalance,
            BigDecimal holdBalance) {
        this.userId = userId;
        this.username = username;
        this.email = email;
        this.position = position;
        this.availableBalance = availableBalance;
        this.holdBalance = holdBalance;
    }

    public long getUserId() {
        return userId;
    }

    public String getUsername() {
        return username;
    }

    public String getEmail() {
        return email;
    }

    public Position getPosition() {
        return position;
    }

    public BigDecimal getAvailableBalance() {
        return availableBalance;
    }

    public BigDecimal getHoldBalance() {
        return holdBalance;
    }
}

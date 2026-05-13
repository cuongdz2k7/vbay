package com.vbay.shared.dto.userDTO;

import java.math.BigDecimal;

public class UserBalanceResponse {
    private long userId;
    private long userVersion;
    private BigDecimal availableBalance;
    private BigDecimal holdBalance;

    public UserBalanceResponse() {
    }

    public UserBalanceResponse(long userId, long userVersion, BigDecimal availableBalance, BigDecimal holdBalance) {
        this.userId = userId;
        this.userVersion = userVersion;
        this.availableBalance = availableBalance;
        this.holdBalance = holdBalance;
    }

    public long getUserId() {
        return userId;
    }

    public void setUserId(long userId) {
        this.userId = userId;
    }

    public long getUserVersion() {
        return userVersion;
    }

    public void setUserVersion(long userVersion) {
        this.userVersion = userVersion;
    }

    public BigDecimal getAvailableBalance() {
        return availableBalance;
    }

    public void setAvailableBalance(BigDecimal availableBalance) {
        this.availableBalance = availableBalance;
    }

    public BigDecimal getHoldBalance() {
        return holdBalance;
    }

    public void setHoldBalance(BigDecimal holdBalance) {
        this.holdBalance = holdBalance;
    }
}

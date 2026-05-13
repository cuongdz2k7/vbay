package com.vbay.shared.dto.realtimeDTO.payload;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public class UserBalanceUpdatedPayload {
    private long userId;
    private long userVersion;
    private BigDecimal availableBalance;
    private BigDecimal holdBalance;
    private String reason;
    private LocalDateTime updatedAt;

    public UserBalanceUpdatedPayload() {
    }

    public UserBalanceUpdatedPayload(
            long userId,
            long userVersion,
            BigDecimal availableBalance,
            BigDecimal holdBalance,
            String reason,
            LocalDateTime updatedAt) {
        this.userId = userId;
        this.userVersion = userVersion;
        this.availableBalance = availableBalance;
        this.holdBalance = holdBalance;
        this.reason = reason;
        this.updatedAt = updatedAt;
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

    public String getReason() {
        return reason;
    }

    public void setReason(String reason) {
        this.reason = reason;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }
}

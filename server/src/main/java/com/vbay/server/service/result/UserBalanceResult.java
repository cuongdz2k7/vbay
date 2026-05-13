package com.vbay.server.service.result;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public class UserBalanceResult {
    private final long userId;
    private final long userVersion;
    private final BigDecimal availableBalance;
    private final BigDecimal holdBalance;
    private final String reason;
    private final LocalDateTime updatedAt;

    public UserBalanceResult(
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

    public long getUserId() { return userId; }
    public long getUserVersion() { return userVersion; }
    public BigDecimal getAvailableBalance() { return availableBalance; }
    public BigDecimal getHoldBalance() { return holdBalance; }
    public String getReason() { return reason; }
    public LocalDateTime getUpdatedAt() { return updatedAt; }
}
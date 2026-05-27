package com.vbay.server.service.bid.resolution.model;
import java.math.BigDecimal;

import com.vbay.server.service.bid.enums.BalanceChangeType;

public class BalanceChange {
    private long userId;
    private BigDecimal amount;
    private BalanceChangeType type;
    private String reason;

    public BalanceChange(long userId, BigDecimal amount, BalanceChangeType type, String reason) {
        this.userId = userId;
        this.amount = amount;
        this.type = type;
        this.reason = reason;
    }
    public long getUserId() {
        return userId;
    }
    public BigDecimal getAmount() {
        return amount;
    }
    public BalanceChangeType getType() {
        return type;
    }
    public String getReason() {
        return reason;
    }
    
}
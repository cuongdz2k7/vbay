package com.vbay.server.model;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public class DepositRequestRow {
    private long id;
    private long userId;
    private String username;
    private BigDecimal amount;
    private String status;
    private Long adminId;
    private LocalDateTime createdAt;
    private LocalDateTime processedAt;

    public DepositRequestRow() {}

    public DepositRequestRow(long id, long userId, String username, BigDecimal amount, String status, Long adminId, LocalDateTime createdAt, LocalDateTime processedAt) {
        this.id = id;
        this.userId = userId;
        this.username = username;
        this.amount = amount;
        this.status = status;
        this.adminId = adminId;
        this.createdAt = createdAt;
        this.processedAt = processedAt;
    }

    public long getId() { return id; }
    public void setId(long id) { this.id = id; }

    public long getUserId() { return userId; }
    public void setUserId(long userId) { this.userId = userId; }

    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }

    public BigDecimal getAmount() { return amount; }
    public void setAmount(BigDecimal amount) { this.amount = amount; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public Long getAdminId() { return adminId; }
    public void setAdminId(Long adminId) { this.adminId = adminId; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public LocalDateTime getProcessedAt() { return processedAt; }
    public void setProcessedAt(LocalDateTime processedAt) { this.processedAt = processedAt; }
}

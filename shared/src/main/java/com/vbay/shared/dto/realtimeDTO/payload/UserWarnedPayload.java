package com.vbay.shared.dto.realtimeDTO.payload;

public class UserWarnedPayload {
    private long userId;
    private String reason;
    private int warningCount;

    public UserWarnedPayload() {
    }

    public UserWarnedPayload(long userId, String reason, int warningCount) {
        this.userId = userId;
        this.reason = reason;
        this.warningCount = warningCount;
    }

    public long getUserId() {
        return userId;
    }

    public void setUserId(long userId) {
        this.userId = userId;
    }

    public String getReason() {
        return reason;
    }

    public void setReason(String reason) {
        this.reason = reason;
    }

    public int getWarningCount() {
        return warningCount;
    }

    public void setWarningCount(int warningCount) {
        this.warningCount = warningCount;
    }
}

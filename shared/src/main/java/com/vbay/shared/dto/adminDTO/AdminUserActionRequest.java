package com.vbay.shared.dto.adminDTO;

public class AdminUserActionRequest {
    private long targetUserId;
    private String reason;

    public AdminUserActionRequest() {}

    public AdminUserActionRequest(long targetUserId, String reason) {
        this.targetUserId = targetUserId;
        this.reason = reason;
    }

    public long getTargetUserId() { return targetUserId; }
    public void setTargetUserId(long targetUserId) { this.targetUserId = targetUserId; }

    public String getReason() { return reason; }
    public void setReason(String reason) { this.reason = reason; }
}

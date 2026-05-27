package com.vbay.shared.dto.adminDTO;

public class AdminLockUserRequest {
    private long targetUserId;
    private int lockDurationMinutes;
    private String reason;

    public AdminLockUserRequest() {}

    public AdminLockUserRequest(long targetUserId, int lockDurationMinutes, String reason) {
        this.targetUserId = targetUserId;
        this.lockDurationMinutes = lockDurationMinutes;
        this.reason = reason;
    }

    public long getTargetUserId() { return targetUserId; }
    public void setTargetUserId(long targetUserId) { this.targetUserId = targetUserId; }

    public int getLockDurationMinutes() { return lockDurationMinutes; }
    public void setLockDurationMinutes(int lockDurationMinutes) { this.lockDurationMinutes = lockDurationMinutes; }

    public String getReason() { return reason; }
    public void setReason(String reason) { this.reason = reason; }
}

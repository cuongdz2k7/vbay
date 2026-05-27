package com.vbay.shared.dto.adminDTO;

public class AdminUserActionRequest {
    private long targetUserId;
    private String reason;
    private Integer duration;
    private String durationUnit;

    public AdminUserActionRequest() {}

    public AdminUserActionRequest(long targetUserId, String reason) {
        this.targetUserId = targetUserId;
        this.reason = reason;
    }

    public AdminUserActionRequest(long targetUserId, String reason, Integer duration, String durationUnit) {
        this.targetUserId = targetUserId;
        this.reason = reason;
        this.duration = duration;
        this.durationUnit = durationUnit;
    }

    public long getTargetUserId() { return targetUserId; }
    public void setTargetUserId(long targetUserId) { this.targetUserId = targetUserId; }

    public String getReason() { return reason; }
    public void setReason(String reason) { this.reason = reason; }

    public Integer getDuration() { return duration; }
    public void setDuration(Integer duration) { this.duration = duration; }

    public String getDurationUnit() { return durationUnit; }
    public void setDurationUnit(String durationUnit) { this.durationUnit = durationUnit; }
}

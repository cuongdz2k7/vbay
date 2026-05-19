package com.vbay.shared.dto.adminDTO;

import com.vbay.shared.enums.auth.Position;
import com.vbay.shared.enums.auth.UserStatus;

public class AdminUserItem {
    private long userId;
    private String username;
    private String email;
    private Position position;
    private UserStatus status;
    private int warningCount;
    private String lockUntil;

    public AdminUserItem() {}

    public AdminUserItem(long userId, String username, String email, Position position, UserStatus status, int warningCount, String lockUntil) {
        this.userId = userId;
        this.username = username;
        this.email = email;
        this.position = position;
        this.status = status;
        this.warningCount = warningCount;
        this.lockUntil = lockUntil;
    }

    public long getUserId() { return userId; }
    public void setUserId(long userId) { this.userId = userId; }

    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public Position getPosition() { return position; }
    public void setPosition(Position position) { this.position = position; }

    public UserStatus getStatus() { return status; }
    public void setStatus(UserStatus status) { this.status = status; }

    public int getWarningCount() { return warningCount; }
    public void setWarningCount(int warningCount) { this.warningCount = warningCount; }

    public String getLockUntil() { return lockUntil; }
    public void setLockUntil(String lockUntil) { this.lockUntil = lockUntil; }
}

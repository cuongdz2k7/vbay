package com.vbay.shared.dto.adminDTO;

import com.vbay.shared.enums.auth.Position;
import com.vbay.shared.enums.auth.UserStatus;

public class AdminUserDTO {
    private long id;
    private String username;
    private Position position;
    private UserStatus status;
    private String timeInit;

    public AdminUserDTO() {
    }

    public AdminUserDTO(
            long id,
            String username,
            Position position,
            UserStatus status,
            String timeInit) {
        this.id = id;
        this.username = username;
        this.position = position;
        this.status = status;
        this.timeInit = timeInit;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public Position getPosition() {
        return position;
    }

    public void setPosition(Position position) {
        this.position = position;
    }

    public UserStatus getStatus() {
        return status;
    }

    public void setStatus(UserStatus status) {
        this.status = status;
    }

    public String getTimeInit() {
        return timeInit;
    }

    public void setTimeInit(String timeInit) {
        this.timeInit = timeInit;
    }
}

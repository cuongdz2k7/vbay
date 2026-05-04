package com.vbay.server.network;

import com.vbay.shared.status.Position;


public class ClientSession {
    private Long userId;
    private String username;
    private Position position;

    public ClientSession() {
        this.userId = null;
        this.username = null;
        this.position = null;
    }

    public boolean isAuthenticated() {
        return userId != null;
    }

    public Long setSession(Long userId, String username, Position position) {
        if (isAuthenticated()) {
            throw new IllegalStateException("Session is already set");
        }
        this.userId = userId;
        this.username = username;
        this.position = position;
        return userId;
    }
    public Long getUserId() {
        return userId;
    }
    public String getUsername() {
        return username;
    }
    public Position getPosition() {
        return position;
    }

    public void clearSession() {
        this.userId = null;
        this.username = null;
        this.position = null;
    }
}

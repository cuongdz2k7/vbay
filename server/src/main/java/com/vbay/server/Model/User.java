package com.vbay.server.model;

import java.math.BigDecimal;
import java.time.LocalDate;

import com.vbay.shared.enums.Position;
import com.vbay.shared.enums.shared_status.UserStatus;

public class User {
    private long id;
    private final String timeinit;
    private String username;
    private String email;
    private String passwordHash;
    private String phoneNumber;
    private Position position;
    private UserStatus status;
    private BigDecimal balance;

    public User(String username, String email, String passwordHash, String phoneNumber, BigDecimal balance) {
        this.username = username;
        this.email = email;
        this.passwordHash = passwordHash;
        this.phoneNumber = phoneNumber;
        this.position = Position.USER;
        this.status = UserStatus.ACTIVE;
        this.balance = balance;
        this.timeinit = LocalDate.now().toString();
    }

    public User(
        String username,
        String email,
        String passwordHash,
        String phoneNumber,
        Position position,
        UserStatus status,
        BigDecimal balance,
        String timeinit
    ) {
        this.username = username;
        this.email = email;
        this.passwordHash = passwordHash;
        this.phoneNumber = phoneNumber;
        this.position = position;
        this.status = status;
        this.balance = balance;
        this.timeinit = timeinit;
    }

    public long getId() {
        return id;
    }

    public String getTimeinit() {
        return timeinit;
    }

    public String getUserName() {
        return username;
    }

    public String getEmail() {
        return email;
    }

    public String getPasswordHash() {
        return passwordHash;
    }

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public Position getPosition() {
        return position;
    }

    public UserStatus getUserStatus() {
        return status;
    }

    public BigDecimal getBalance() {
        return balance;
    }

    public void setId(long id) {
        this.id = id;
    }

    public void setUserName(String username) {
        this.username = username;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public void setPasswordHash(String passwordHash) {
        this.passwordHash = passwordHash;
    }

    public void setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }

    public void setPosition(Position position) {
        this.position = position;
    }

    public void setStatus(UserStatus status) {
        this.status = status;
    }

    public void setBalance(BigDecimal balance) {
        this.balance = balance;
    }
}

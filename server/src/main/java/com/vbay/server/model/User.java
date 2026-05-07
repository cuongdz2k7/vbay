package com.vbay.server.model;

import java.math.BigDecimal;
import java.time.LocalDate;

import com.vbay.shared.enums.auth.Position;
import com.vbay.shared.enums.auth.UserStatus;

public class User {
    private long id;
    private final String timeinit;
    private String username;
    private String email;
    private String passwordHash;
    private String phoneNumber;
    private Position position;
    private UserStatus status;
    private BigDecimal availableBalance;
    private BigDecimal holdBalance;
    private long version;

    public User(
            String username,
            String email,
            String passwordHash,
            String phoneNumber,
            BigDecimal availableBalance) {
        this(
            username,
            email,
            passwordHash,
            phoneNumber,
            Position.USER,
            UserStatus.ACTIVE,
            availableBalance,
            BigDecimal.ZERO,
            LocalDate.now().toString()
        );
    }

    public User(
            String username,
            String email,
            String passwordHash,
            String phoneNumber,
            Position position,
            UserStatus status,
            BigDecimal availableBalance,
            BigDecimal holdBalance,
            String timeinit) {
        this.username = username;
        this.email = email;
        this.passwordHash = passwordHash;
        this.phoneNumber = phoneNumber;
        this.position = position;
        this.status = status;
        this.availableBalance = availableBalance;
        this.holdBalance = holdBalance;
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

    public BigDecimal getAvailableBalance() {
        return availableBalance;
    }

    public BigDecimal getHoldBalance() {
        return holdBalance;
    }

    public long getVersion() {
        return version;
    }

    public boolean isActive() {
        return status == UserStatus.ACTIVE;
    }

    public boolean isBanned() {
        return status == UserStatus.BANNED;
    }

    public boolean isSuspended() {
        return status == UserStatus.SUSPENDED;
    }

    public boolean isDeleted() {
        return status == UserStatus.DELETED;
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

    public void setAvailableBalance(BigDecimal availableBalance) {
        this.availableBalance = availableBalance;
    }

    public void setHoldBalance(BigDecimal holdBalance) {
        this.holdBalance = holdBalance;
    }

    public void setVersion(long version) {
        this.version = version;
    }

}

package com.vbay.shared.dto.realtimeDTO.payload;

import java.time.LocalDateTime;

///updating... chưa quyết định xong structre và rules

public class NotificationPayload {
    private String notificationId;
    private long receiverUserId;
    private String notificationType;
    private String title;
    private String message;
    private String entityType;
    private Long entityId;
    private Long auctionId;
    private boolean read;
    private LocalDateTime createdAt;

    public NotificationPayload() {
    }

    public NotificationPayload(
            String notificationId,
            long receiverUserId,
            String notificationType,
            String title,
            String message,
            String entityType,
            Long entityId,
            Long auctionId,
            boolean read,
            LocalDateTime createdAt) {
        this.notificationId = notificationId;
        this.receiverUserId = receiverUserId;
        this.notificationType = notificationType;
        this.title = title;
        this.message = message;
        this.entityType = entityType;
        this.entityId = entityId;
        this.auctionId = auctionId;
        this.read = read;
        this.createdAt = createdAt;
    }

    public String getNotificationId() {
        return notificationId;
    }

    public void setNotificationId(String notificationId) {
        this.notificationId = notificationId;
    }

    public long getReceiverUserId() {
        return receiverUserId;
    }

    public void setReceiverUserId(long receiverUserId) {
        this.receiverUserId = receiverUserId;
    }

    public String getNotificationType() {
        return notificationType;
    }

    public void setNotificationType(String notificationType) {
        this.notificationType = notificationType;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getEntityType() {
        return entityType;
    }

    public void setEntityType(String entityType) {
        this.entityType = entityType;
    }

    public Long getEntityId() {
        return entityId;
    }

    public void setEntityId(Long entityId) {
        this.entityId = entityId;
    }

    public Long getAuctionId() {
        return auctionId;
    }

    public void setAuctionId(Long auctionId) {
        this.auctionId = auctionId;
    }

    public boolean isRead() {
        return read;
    }

    public void setRead(boolean read) {
        this.read = read;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
}

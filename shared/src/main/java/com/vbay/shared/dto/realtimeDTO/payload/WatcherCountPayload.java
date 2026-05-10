package com.vbay.shared.dto.realtimeDTO.payload;

import java.time.LocalDateTime;

public class WatcherCountPayload {
    private long auctionId;
    private int watcherCount;
    private LocalDateTime updatedAt;

    public WatcherCountPayload() {
    }

    public WatcherCountPayload(long auctionId, int watcherCount, LocalDateTime updatedAt) {
        this.auctionId = auctionId;
        this.watcherCount = watcherCount;
        this.updatedAt = updatedAt;
    }

    public long getAuctionId() {
        return auctionId;
    }

    public void setAuctionId(long auctionId) {
        this.auctionId = auctionId;
    }

    public int getWatcherCount() {
        return watcherCount;
    }

    public void setWatcherCount(int watcherCount) {
        this.watcherCount = watcherCount;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }
}

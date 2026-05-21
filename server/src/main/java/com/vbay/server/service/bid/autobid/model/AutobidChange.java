package com.vbay.server.service.bid.autobid.model;

import java.math.BigDecimal;

import com.vbay.server.service.bid.autobid.enums.AutobidStatus;

///dùng để service update bảng autobid khi có sự thay đổi trong autobid
///ví dụ: khi có 1 autobid được trigger, nó sẽ tạo ra 1 autobid change để update lại status của autobid đó
public class AutobidChange {
    private long autobidId;
    private long userId;
    private long auctionId;
    private AutobidStatus newStatus;
    private BigDecimal newMaxBidAmount;
    private BigDecimal newHoldAmount; ///new hold amount

    public AutobidChange(long autobidId, long userId, long auctionId, AutobidStatus newStatus, BigDecimal newMaxBidAmount, BigDecimal newHoldAmount) {
        this.autobidId = autobidId;
        this.userId = userId;
        this.auctionId = auctionId;
        this.newStatus = newStatus;
        this.newMaxBidAmount = newMaxBidAmount;
        this.newHoldAmount = newHoldAmount;
    }

    public long getAutobidId() {
        return autobidId;
    }
    public long getUserId() {
        return userId;
    }
    public long getAuctionId() {
        return auctionId;
    }
    public AutobidStatus getNewStatus() {
        return newStatus;
    }
    public BigDecimal getNewMaxBidAmount() {
        return newMaxBidAmount;
    }
    public BigDecimal getNewHoldAmount() {
        return newHoldAmount;
    }
}
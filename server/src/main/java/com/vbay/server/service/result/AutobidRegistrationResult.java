package com.vbay.server.service.result;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import com.vbay.server.service.bid.autobid.enums.AutobidStatus;
import com.vbay.shared.enums.bid.BidSource;
import com.vbay.shared.enums.bid.BidStatus;

public class AutobidRegistrationResult extends BidUpdateResult {
    private final long autobidId;
    private final BigDecimal maxBidAmount;
    private final AutobidStatus autobidStatus;

    public AutobidRegistrationResult(
            long auctionId,
            long auctionVersion,
            long sellerId,
            long autobidId,
            long userId,
            long bidId,
            BigDecimal maxBidAmount,
            BigDecimal currentPrice,
            Boolean reserveMet,
            String auctionStatus,
            Long previousWinningUserId,
            Long previousWinningBidId,
            AutobidStatus autobidStatus,
            BidStatus bidStatus,
            BidSource bidSource,
            LocalDateTime registeredAt,
            LocalDateTime startingTime,
            LocalDateTime endingTime,
            List<UserMyBidListItemResult> affectedMyBidItems) {
        super(
            auctionId,
            auctionVersion,
            sellerId,
            userId,
            bidId,
            currentPrice,
            currentPrice,
            reserveMet,
            auctionStatus,
            previousWinningUserId,
            previousWinningBidId,
            bidStatus,
            bidSource,
            registeredAt,
            startingTime,
            endingTime,
            affectedMyBidItems
        );
        this.autobidId = autobidId;
        this.maxBidAmount = maxBidAmount;
        this.autobidStatus = autobidStatus;
    }

    public long getAutobidId() {
        return autobidId;
    }

    public long getUserId() {
        return getBidderId();
    }

    public BigDecimal getMaxBidAmount() {
        return maxBidAmount;
    }

    public AutobidStatus getAutobidStatus() {
        return autobidStatus;
    }

    public AutobidStatus getStatus() {
        return autobidStatus;
    }

    public LocalDateTime getRegisteredAt() {
        return getBidTime();
    }
}

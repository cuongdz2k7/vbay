package com.vbay.server.service.result;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import com.vbay.server.realtime.domain.enums.AuctionCloseReason;
import com.vbay.shared.enums.auction.AuctionStatus;

public class AuctionClosedResult {
    private final long auctionId;
    private final long auctionVersion;
    private final AuctionStatus auctionStatus;
    private final BigDecimal currentPrice;
    private final Boolean reserveMet;
    private final Long winnerUserId;
    private final LocalDateTime startingTime;
    private final LocalDateTime endingTime;
    private final AuctionCloseReason reason;
    private final LocalDateTime closedAt;
    private final List<UserMyBidListItemResult> affectedMyBidItems;
    private final List<AutobidUpdateResult> affectedAutobids;

    public AuctionClosedResult(
            long auctionId,
            long auctionVersion,
            AuctionStatus auctionStatus,
            BigDecimal currentPrice,
            Boolean reserveMet,
            Long winnerUserId,
            LocalDateTime startingTime,
            LocalDateTime endingTime,
            AuctionCloseReason reason,
            LocalDateTime closedAt,
            List<UserMyBidListItemResult> affectedMyBidItems,
            List<AutobidUpdateResult> affectedAutobids) {
        this.auctionId = auctionId;
        this.auctionVersion = auctionVersion;
        this.auctionStatus = auctionStatus;
        this.currentPrice = currentPrice;
        this.reserveMet = reserveMet;
        this.winnerUserId = winnerUserId;
        this.startingTime = startingTime;
        this.endingTime = endingTime;
        this.reason = reason;
        this.closedAt = closedAt;
        this.affectedMyBidItems = affectedMyBidItems == null ? List.of() : List.copyOf(affectedMyBidItems);
        this.affectedAutobids = affectedAutobids == null ? List.of() : List.copyOf(affectedAutobids);
    }

    public long getAuctionId() {
        return auctionId;
    }

    public long getAuctionVersion() {
        return auctionVersion;
    }

    public AuctionStatus getAuctionStatus() {
        return auctionStatus;
    }

    public BigDecimal getCurrentPrice() {
        return currentPrice;
    }

    public Boolean getReserveMet() {
        return reserveMet;
    }

    public Long getWinnerUserId() {
        return winnerUserId;
    }

    public LocalDateTime getStartingTime() {
        return startingTime;
    }

    public LocalDateTime getEndingTime() {
        return endingTime;
    }

    public AuctionCloseReason getReason() {
        return reason;
    }

    public LocalDateTime getClosedAt() {
        return closedAt;
    }

    public List<UserMyBidListItemResult> getAffectedMyBidItems() {
        return affectedMyBidItems;
    }

    public List<AutobidUpdateResult> getAffectedAutobids() {
        return affectedAutobids;
    }
}

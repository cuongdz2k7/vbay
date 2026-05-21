package com.vbay.server.service.bid.autobid.model;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import com.vbay.server.service.bid.resolution.model.BalanceChange;
import com.vbay.shared.enums.auction.AuctionStatus;

public class AutobidResolution {
    private boolean accepted;
    private String message;

    private long auctionId;
    private BigDecimal newCurrentPrice;
    private Long newWinnerUserId;
    private AuctionStatus newAuctionStatus;

    private List<BidRecordChange> bidRecordsToCreate = new ArrayList<>();
    private List<AutobidChange> autobidChanges = new ArrayList<>();
    private List<BalanceChange> balanceChanges = new ArrayList<>();

    public AutobidResolution
        (boolean accepted, 
        String message,
        long auctionId, 
        BigDecimal newCurrentPrice, 
        Long newWinnerUserId, 
        AuctionStatus newAuctionStatus) {
        this.accepted = accepted;
        this.message = message;
        this.auctionId = auctionId;
        this.newCurrentPrice = newCurrentPrice;
        this.newWinnerUserId = newWinnerUserId;
        this.newAuctionStatus = newAuctionStatus;
    }

    public AutobidResolution(
        boolean accepted,
        String message,
        long auctionId,
        BigDecimal newCurrentPrice,
        Long newWinnerUserId,
        AuctionStatus newAuctionStatus,
        List<BidRecordChange> bidRecordsToCreate,
        List<AutobidChange> autobidChanges,
        List<BalanceChange> balanceChanges) {
            
        this(accepted, message, auctionId, newCurrentPrice, newWinnerUserId, newAuctionStatus);
        this.bidRecordsToCreate = bidRecordsToCreate == null ? new ArrayList<>() : bidRecordsToCreate;
        this.autobidChanges = autobidChanges == null ? new ArrayList<>() : autobidChanges;
        this.balanceChanges = balanceChanges == null ? new ArrayList<>() : balanceChanges;
    }

    public boolean isAccepted() {
        return accepted;
    }
    public String getMessage() {
        return message;
    }
    public long getAuctionId() {
        return auctionId;
    }
    public BigDecimal getNewCurrentPrice() {
        return newCurrentPrice;
    }
    public Long getNewWinnerUserId() {
        return newWinnerUserId;
    }
    public AuctionStatus getNewAuctionStatus() {
        return newAuctionStatus;
    }
    public List<BidRecordChange> getBidRecordsToCreate() {
        return bidRecordsToCreate;
    }
    public void setBidRecordsToCreate(List<BidRecordChange> bidRecordsToCreate) {
        this.bidRecordsToCreate = bidRecordsToCreate;
    }
    public List<AutobidChange> getAutobidChanges() {
        return autobidChanges;
    }
    public void setAutobidChanges(List<AutobidChange> autobidChanges) {
        this.autobidChanges = autobidChanges;
    }
    public List<BalanceChange> getBalanceChanges() {
        return balanceChanges;
    }
    public void setBalanceChanges(List<BalanceChange> balanceChanges) {
        this.balanceChanges = balanceChanges;
    }

}

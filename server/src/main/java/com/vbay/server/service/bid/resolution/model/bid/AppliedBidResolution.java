package com.vbay.server.service.bid.resolution.model.bid;

import java.util.List;

import com.vbay.server.model.Auction;
import com.vbay.server.model.Bid;
import com.vbay.server.model.Payment;
import com.vbay.server.service.result.UserBalanceResult;
import com.vbay.server.service.result.UserMyBidListItemResult;

public class AppliedBidResolution {
    private final Auction refreshedAuction;
    private final List<Bid> createdBids;
    private final List<Payment> createdPayments;
    private final List<UserBalanceResult> balanceResults;
    private final List<UserMyBidListItemResult> affectedMyBidItems;
    private final long auctionVersion;

    public AppliedBidResolution(
            Auction refreshedAuction,
            List<Bid> createdBids,
            List<Payment> createdPayments,
            List<UserBalanceResult> balanceResults,
            List<UserMyBidListItemResult> affectedMyBidItems,
            long auctionVersion) {
        this.refreshedAuction = refreshedAuction;
        this.createdBids = createdBids;
        this.createdPayments = createdPayments;
        this.balanceResults = balanceResults;
        this.affectedMyBidItems = affectedMyBidItems;
        this.auctionVersion = auctionVersion;
    }

    public Auction getRefreshedAuction() { return refreshedAuction; }
    public List<Bid> getCreatedBids() { return createdBids; }
    public List<Payment> getCreatedPayments() { return createdPayments; }
    public List<UserBalanceResult> getBalanceResults() { return balanceResults; }
    public List<UserMyBidListItemResult> getAffectedMyBidItems() { return affectedMyBidItems; }
    public long getAuctionVersion() { return auctionVersion; }
}
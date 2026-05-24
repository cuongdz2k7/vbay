package com.vbay.server.service.bid.resolution.model.bid;

import java.sql.SQLException;

import com.vbay.server.repository.BidRepository;
import com.vbay.shared.enums.bid.BidStatus;

public class MarkWinningBidWonAndOthersLost implements BidFinalization {
    private final long auctionId;
    private final long winningBidId;
    public MarkWinningBidWonAndOthersLost(long auctionId, long winningBidId) {
        this.auctionId = auctionId;
        this.winningBidId = winningBidId;
    }

    public void apply(BidRepository bidRepository, BidResolution resolution) throws SQLException {
        bidRepository.updateStatus(winningBidId, BidStatus.WON);
        bidRepository.updateStatusesByAuctionIdExceptBid(
            auctionId,
            winningBidId,
            BidStatus.LOST
        );
    }
}

package com.vbay.server.service.bid.resolution.model.bid;
import java.sql.SQLException;

import com.vbay.server.exception.ValidationException;
import com.vbay.server.repository.BidRepository;
import com.vbay.shared.enums.bid.BidSource;
import com.vbay.shared.enums.bid.BidStatus;  

public class MarkOtherBidsLostAfterBuyNow implements BidFinalization {
    private final long auctionId;
    private final BidSource buyNowBidSource;

    public MarkOtherBidsLostAfterBuyNow(long auctionId, BidSource buyNowBidSource) {
        this.auctionId = auctionId;
        this.buyNowBidSource = buyNowBidSource;
    }

    @Override
    public void apply(BidRepository bidRepository, BidResolution resolution) throws SQLException {
        BidCreate buyNowBid = resolution.findBidCreateBySource(buyNowBidSource)
            .orElseThrow(() -> new ValidationException("Buy Now bid was not created"));

        bidRepository.updateStatusesByAuctionIdExceptBid(
            auctionId,
            buyNowBid.getId(),
            BidStatus.LOST
        );
    }
}
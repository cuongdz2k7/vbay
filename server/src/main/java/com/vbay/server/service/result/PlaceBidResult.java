package com.vbay.server.service.result;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import com.vbay.shared.enums.bid.BidSource;
import com.vbay.shared.enums.bid.BidStatus;

public class PlaceBidResult extends BidUpdateResult {
    private final String auctionTitle;

    public PlaceBidResult(
            long auctionId,
            long auctionVersion,
            long sellerId,
            long bidderId,
            long bidId,
            String auctionTitle,
            BigDecimal bidAmount,
            BigDecimal currentPrice,
            Boolean reserveMet,
            boolean antiSnipeExtended,
            String auctionStatus,
            Long previousWinningUserId,
            Long previousWinningBidId,
            BidStatus bidStatus,
            BidSource bidSource,
            LocalDateTime bidTime,
            LocalDateTime startingTime,
            LocalDateTime endingTime,
            List<UserMyBidListItemResult> affectedMyBidItems) {
        super(
            auctionId,
            auctionVersion,
            sellerId,
            bidderId,
            bidId,
            bidAmount,
            currentPrice,
            reserveMet,
            antiSnipeExtended,
            auctionStatus,
            previousWinningUserId,
            previousWinningBidId,
            bidStatus,
            bidSource,
            bidTime,
            startingTime,
            endingTime,
            affectedMyBidItems
        );
        this.auctionTitle = auctionTitle;
    }

    public String getAuctionTitle() {
        return auctionTitle;
    }
}

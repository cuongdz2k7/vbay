package com.vbay.server.service.result.mapper;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import com.vbay.server.model.Auction;
import com.vbay.server.model.Bid;
import com.vbay.server.model.Payment;
import com.vbay.server.model.Product;
import com.vbay.server.service.result.BuyNowResult;
import com.vbay.server.service.result.CreateAuctionResult;
import com.vbay.server.service.result.PlaceBidResult;

public class ResultMapper {
    private ResultMapper() {
    }

    public static CreateAuctionResult toCreateAuctionResult(Auction auction, Product product) {
        return new CreateAuctionResult(
            auction.getId(),
            auction.getVersion(),
            product.getId(),
            product.getVersion(),
            auction.getSellerId(),
            auction.getTitle(),
            product.getCategoryId(),
            auction.getStatus(),
            auction.getStartPrice(),
            auction.getCurrentPrice(),
            auction.getMinimumBidStep(),
            auction.getStartingTime(),
            auction.getEndingTime()
        );
    }

    public static PlaceBidResult toPlaceBidResult(
            Auction auction,
            Bid currentBid,
            long auctionVersion,
            BigDecimal nextMinimumBid,
            Long previousWinningUserId,
            Long previousWinningBidId) {
        return new PlaceBidResult(
            auction.getId(),
            auctionVersion,
            auction.getSellerId(),
            currentBid.getBidderId(),
            currentBid.getId(),
            currentBid.getBidAmount(),
            currentBid.getBidAmount(),
            nextMinimumBid,
            previousWinningUserId,
            previousWinningBidId,
            currentBid.getStatus(),
            currentBid.getBidSource(),
            currentBid.getBidTime()
        );
    }

    public static BuyNowResult toBuyNowResult(
            Auction auction,
            Bid currentBid,
            Payment payment,
            long auctionVersion,
            Long previousWinningUserId,
            Long previousWinningBidId,
            LocalDateTime boughtAt) {
        return new BuyNowResult(
            auction.getId(),
            auctionVersion,
            currentBid.getBidderId(),
            auction.getSellerId(),
            currentBid.getId(),
            payment.getId(),
            currentBid.getBidAmount(),
            previousWinningUserId,
            previousWinningBidId,
            boughtAt
        );
    }
}

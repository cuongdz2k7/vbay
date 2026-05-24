package com.vbay.server.service.result.mapper;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import com.vbay.server.model.Auction;
import com.vbay.server.model.Bid;
import com.vbay.server.model.Payment;
import com.vbay.server.model.Product;
import com.vbay.server.model.User;
import com.vbay.server.service.result.AuctionItemResult;
import com.vbay.server.service.result.AuctionListItemResult;
import com.vbay.server.service.result.BuyNowResult;
import com.vbay.server.service.result.CreateAuctionResult;
import com.vbay.server.service.result.PlaceBidResult;
import com.vbay.server.service.result.UserBalanceResult;
import com.vbay.server.service.result.UserMyBidListItemResult;
import com.vbay.shared.dto.realtimeDTO.payload.AuctionItemPayload;
import com.vbay.shared.dto.realtimeDTO.payload.AuctionListItemPayload;
import com.vbay.shared.dto.realtimeDTO.payload.UserBalanceUpdatedPayload;
import com.vbay.shared.dto.userDTO.UserBalanceResponse;
import com.vbay.shared.enums.bid.BidStatus;

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
            Long previousWinningUserId,
            Long previousWinningBidId,
            List<UserMyBidListItemResult> affectedMyBidItems) {
        return new PlaceBidResult(
            auction.getId(),
            auction.getVersion(),
            auction.getSellerId(),
            currentBid.getBidderId(),
            currentBid.getId(),
            auction.getTitle(),
            currentBid.getBidAmount(),
            currentBid.getBidAmount(),
            reserveMet(auction, currentBid.getBidAmount()),
            auction.getAntiSnipeExtensionCount() > 0,
            auction.getStatus().name(),
            previousWinningUserId,
            previousWinningBidId,
            currentBid.getStatus(),
            currentBid.getBidSource(),
            currentBid.getBidTime(),
            auction.getStartingTime(),
            auction.getEndingTime(),
            affectedMyBidItems
        );
    }

    private static Boolean reserveMet(Auction auction, BigDecimal currentPrice) {
        if (auction.getReservePrice() == null) {
            return null;
        }
        return currentPrice.compareTo(auction.getReservePrice()) >= 0;
    }

    public static BuyNowResult toBuyNowResult(
            Auction auction,
            Bid currentBid,
            Payment payment,
            long auctionVersion,
            Long previousWinningUserId,
            Long previousWinningBidId,
            LocalDateTime boughtAt,
            List<UserMyBidListItemResult> affectedMyBidItems) {
        return new BuyNowResult(
            auction.getId(),
            auctionVersion,
            currentBid.getBidderId(),
            auction.getSellerId(),
            currentBid.getId(),
            payment.getId(),
            currentBid.getBidAmount(),
            auction.getStatus(),
            reserveMet(auction, auction.getCurrentPrice()),
            auction.getAntiSnipeExtensionCount() > 0,
            previousWinningUserId,
            previousWinningBidId,
            auction.getStartingTime(),
            auction.getEndingTime(),
            boughtAt,
            affectedMyBidItems
        );
    }
    ///userbalance result chỉ có lưu holdbalance và available balance (mới) sau khi đã apply
    public static UserBalanceResult toUserBalanceResult(
            User user,
            String reason,
            LocalDateTime updatedAt) {
        return new UserBalanceResult(
            user.getId(),
            user.getVersion(),
            user.getAvailableBalance(),
            user.getHoldBalance(),
            reason,
            updatedAt
        );
    }

    public static UserBalanceResponse toUserBalanceResponse(UserBalanceResult result) {
        return new UserBalanceResponse(
            result.getUserId(),
            result.getUserVersion(),
            result.getAvailableBalance(),
            result.getHoldBalance()
        );
    }

    public static UserBalanceUpdatedPayload toUserBalanceUpdatedPayload(UserBalanceResult result) {
        return new UserBalanceUpdatedPayload(
            result.getUserId(),
            result.getUserVersion(),
            result.getAvailableBalance(),
            result.getHoldBalance(),
            result.getReason(),
            result.getUpdatedAt()
        );
    }

    public static AuctionListItemPayload toAuctionListItemPayload(AuctionListItemResult result) {
        return new AuctionListItemPayload(
            result.getAuctionId(),
            result.getAuctionVersion(),
            result.getProductId(),
            result.getSellerId(),
            result.getTitle(),
            result.getDescription(),
            result.getProductName(),
            result.getCategoryId(),
            result.getStatus(),
            result.getStartingPrice(),
            result.getCurrentPrice(),
            result.getMinimumBidStep(),
            result.getBuyNowPrice(),
            result.getWinnerUserId(),
            result.getReserveMet(),
            result.isAntiSnipeExtended(),
            result.getThumbnailUrl(),
            result.getStartingTime(),
            result.getEndingTime(),
            result.getUpdatedAt()
        );
    }

    public static AuctionItemPayload toAuctionItemPayload(AuctionItemResult result) {
        return new AuctionItemPayload(
            result.getAuctionId(),
            result.getAuctionVersion(),
            result.getProductId(),
            result.getSellerId(),
            result.getTitle(),
            result.getDescription(),
            result.getProductName(),
            result.getCategoryId(),
            result.getStatus(),
            result.getStartingPrice(),
            result.getCurrentPrice(),
            result.getMinimumBidStep(),
            result.getBuyNowPrice(),
            result.getWinnerUserId(),
            result.getReserveMet(),
            result.isAntiSnipeExtended(),
            result.getThumbnailUrl(),
            result.getImageUrls(),
            result.getStartingTime(),
            result.getEndingTime(),
            result.getUpdatedAt()
        );
    }

    public static com.vbay.shared.dto.realtimeDTO.payload.MyBidListItemPayload toMyBidListItemPayload(
            UserMyBidListItemResult result) {
        return new com.vbay.shared.dto.realtimeDTO.payload.MyBidListItemPayload(
            result.getBidId(),
            result.getAuctionId(),
            result.getAuctionVersion(),
            result.getAuctionTitle(),
            result.getThumbnailUrl(),
            result.getCurrentPrice(),
            result.getAuctionStatus().name(),
            result.isAntiSnipeExtended(),
            result.getMyBidAmount(),
            result.getMyMaxBidAmount(),
            result.getBidStatus(),
            result.getBidSource(),
            result.getBidTime(),
            result.getStartingTime(),
            result.getEndingTime(),
            result.getUpdatedAt()
        );
    }


    public static UserMyBidListItemResult toUserMyBidListItemResult(
            Auction auction,
            String thumbnailUrl,
            Bid bid,
            BidStatus bidStatus,
            LocalDateTime updatedAt
    ) {
        return toUserMyBidListItemResult(
            auction,
            thumbnailUrl,
            bid,
            null,
            bidStatus,
            updatedAt
        );
    }

    public static UserMyBidListItemResult toUserMyBidListItemResult(
            Auction auction,
            String thumbnailUrl,
            Bid bid,
            BigDecimal myMaxBidAmount,
            BidStatus bidStatus,
            LocalDateTime updatedAt
    ) {
        return new UserMyBidListItemResult(
            bid.getBidderId(),
            bid.getId(),
            auction.getId(),
            auction.getVersion(),
            auction.getTitle(),
            thumbnailUrl,
            auction.getCurrentPrice(),
            auction.getStatus(),
            auction.getAntiSnipeExtensionCount() > 0,
            bid.getBidAmount(),
            myMaxBidAmount,
            bidStatus,
            bid.getBidSource(),
            bid.getBidTime(),
            auction.getStartingTime(),
            auction.getEndingTime(),
            updatedAt
        );
    }

}

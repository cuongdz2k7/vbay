package com.vbay.server.service.bid.resolution;

import java.time.LocalDateTime;

import com.vbay.server.exception.ValidationException;
import com.vbay.server.model.Auction;
import com.vbay.server.model.Bid;
import com.vbay.server.model.Payment;
import com.vbay.server.service.bid.resolution.model.autobid.AutobidCreate;
import com.vbay.server.service.bid.resolution.model.bid.AppliedBidResolution;
import com.vbay.server.service.bid.resolution.model.bid.BidResolution;
import com.vbay.server.service.result.AutobidRegistrationResult;
import com.vbay.server.service.result.BuyNowResult;
import com.vbay.server.service.result.PlaceBidResult;
import com.vbay.server.service.result.mapper.ResultMapper;
import com.vbay.shared.enums.bid.BidSource;

public final class AppliedBidResultMapper {
    private AppliedBidResultMapper() {}

    public static BuyNowResult toBuyNowResult
            (AppliedBidResolution applied,
            Long previousWinningUserId,
            Long previousWinningBidId,
            LocalDateTime boughtAt) {
        Bid buyNowBid = findRequiredBuyNowBid(applied);
        Payment payment = findRequiredPaymentForBid(applied, buyNowBid.getId());

        return ResultMapper.toBuyNowResult(
            applied.getRefreshedAuction(),
            buyNowBid,
            payment,
            applied.getAuctionVersion(),
            previousWinningUserId,
            previousWinningBidId,
            boughtAt,
            applied.getAffectedMyBidItems()
        );
    }

    private static Bid findRequiredBuyNowBid(AppliedBidResolution applied) {
        return applied.getCreatedBids().stream()
            .filter(bid -> bid.getBidSource() == BidSource.BUY_NOW)
            .findFirst()
            .orElseThrow(() -> new ValidationException("Buy Now bid was not created"));
    }

    private static Payment findRequiredPaymentForBid(AppliedBidResolution applied, long bidId) {
        return applied.getCreatedPayments().stream()
            .filter(payment -> payment.getWinningBidId() == bidId)
            .findFirst()
            .orElseThrow(() -> new ValidationException("Buy Now payment was not created"));
    }


    public static PlaceBidResult toPlaceBidResult(
            AppliedBidResolution applied,
            Long previousWinningUserId,
            Long previousWinningBidId,
            LocalDateTime bidTime) {
        Bid createdBid = applied.getCreatedBids().stream()
            .filter(bid -> bid.getBidSource() == BidSource.USER_BID
                || bid.getBidSource() == BidSource.AUTO_BID)
            .findFirst()
            .orElseThrow(() -> new ValidationException("Bid was not created"));

        return ResultMapper.toPlaceBidResult(
            applied.getRefreshedAuction(),
            createdBid,
            previousWinningUserId,
            previousWinningBidId,
            applied.getAffectedMyBidItems()
        );
    }

    private static Boolean reserveMet(Auction auction) {
        if (auction.getReservePrice() == null) {
            return null;
        }
        return auction.getCurrentPrice().compareTo(auction.getReservePrice()) >= 0;
    }
    
    public static AutobidRegistrationResult toAutobidRegistrationResult(
            AppliedBidResolution applied,
            BidResolution resolution,
            Long previousWinningUserId,
            Long previousWinningBidId,
            LocalDateTime registeredAt) {
        Bid autoBid = applied.getCreatedBids().stream()
            .filter(bid -> bid.getBidSource() == BidSource.AUTO_BID)
            .findFirst()
            .orElseThrow(() -> new ValidationException("AutoBid bid was not created"));

        AutobidCreate autobidCreate = resolution.getAutobidCreates().stream()
            .filter(create -> create.getUserId() == autoBid.getBidderId())
            .findFirst()
            .orElseThrow(() -> new ValidationException("AutoBid was not created"));

        return new AutobidRegistrationResult(
            applied.getRefreshedAuction().getId(),
            applied.getAuctionVersion(),
            applied.getRefreshedAuction().getSellerId(),
            autobidCreate.getId(),
            autoBid.getBidderId(),
            autoBid.getId(),
            autobidCreate.getMaxBidAmount(),
            applied.getRefreshedAuction().getCurrentPrice(),
            reserveMet(applied.getRefreshedAuction()),
            applied.getRefreshedAuction().getAntiSnipeExtensionCount() > 0,
            applied.getRefreshedAuction().getStatus().name(),
            previousWinningUserId,
            previousWinningBidId,
            autobidCreate.getStatus(),
            autoBid.getStatus(),
            autoBid.getBidSource(),
            registeredAt,
            applied.getRefreshedAuction().getStartingTime(),
            applied.getRefreshedAuction().getEndingTime(),
            applied.getAffectedMyBidItems()
        );
    }


}
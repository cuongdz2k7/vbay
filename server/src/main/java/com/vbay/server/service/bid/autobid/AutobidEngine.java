package com.vbay.server.service.bid.autobid;
import java.math.BigDecimal;
import java.util.List;

import com.vbay.server.model.Auction;
import com.vbay.server.model.Autobid;
import com.vbay.server.service.bid.autobid.enums.AutobidStatus;
import com.vbay.server.service.bid.autobid.model.AutobidChange;
import com.vbay.server.service.bid.autobid.model.AutobidResolution;
import com.vbay.server.service.bid.autobid.model.BidRecordChange;
import com.vbay.server.service.bid.command.BuyNowCommand;
import com.vbay.server.service.bid.command.IncreaseAutobidCommand;
import com.vbay.server.service.bid.command.ManualBidCommand;
import com.vbay.server.service.bid.command.RegisterAutobidCommand;
import com.vbay.server.service.bid.enums.BalanceChangeType;
import com.vbay.server.service.bid.enums.BidType;
import com.vbay.server.service.bid.resolution.model.BalanceChange;
import com.vbay.shared.enums.auction.AuctionStatus;


public class AutobidEngine {
    private static final String HIGHER_MAX_BID_MESSAGE = "Someone has already placed a higher maximum bid.";
    
    public AutobidResolution resolveAfterManualBid(
        Auction auction,
        Autobid winningAutobid,
        ManualBidCommand command
    ) {
        BigDecimal amount = command.getAmount();
        if (winningAutobid == null) {
            return new AutobidResolution(
                true,
                null,
                auction.getId(),
                amount,
                command.getBidderUserId(),
                auction.getStatus(),
                List.of(new BidRecordChange(
                    auction.getId(),
                    command.getBidderUserId(),
                    amount,
                    BidType.MANUAL
                )),
                List.of(),
                List.of(new BalanceChange(
                    command.getBidderUserId(),
                    amount,
                    BalanceChangeType.HOLD,
                    "PLACE_BID_HOLD"
                ))
            );
        }

        BigDecimal maxBidAmount = winningAutobid.getMaxBidAmount();
        if (amount.compareTo(maxBidAmount) > 0) {
            return new AutobidResolution(
                true,
                null,
                auction.getId(),
                amount,
                command.getBidderUserId(),
                auction.getStatus(),
                List.of(new BidRecordChange(
                    auction.getId(),
                    command.getBidderUserId(),
                    amount,
                    BidType.MANUAL
                )),
                ///update status
                List.of(new AutobidChange(
                    winningAutobid.getId(),
                    winningAutobid.getUserId(),
                    winningAutobid.getAuctionId(),
                    AutobidStatus.LOST,
                    winningAutobid.getMaxBidAmount(),
                    BigDecimal.ZERO
                )),
                List.of(
                    new BalanceChange(
                        winningAutobid.getUserId(),
                        winningAutobid.getHoldAmount(),
                        BalanceChangeType.RELEASE,
                        "AUTOBID_LOST_RELEASE"
                    )
                )
            );
        }

        BigDecimal systemAutoBidAmount = amount.add(auction.getMinimumBidStep());
        if (systemAutoBidAmount.compareTo(maxBidAmount) > 0) {
            systemAutoBidAmount = maxBidAmount;
        }

        List<BidRecordChange> bidsToCreate = List.of(new BidRecordChange(
            auction.getId(),
            winningAutobid.getUserId(),
            systemAutoBidAmount,
            BidType.SYSTEM_AUTO_BID
        ));

        return new AutobidResolution(
            false,
            HIGHER_MAX_BID_MESSAGE,
            auction.getId(),
            systemAutoBidAmount,
            winningAutobid.getUserId(),
            auction.getStatus(),
            bidsToCreate,
            List.of(),
            List.of()
        );
    }

    public AutobidResolution resolveAfterRegisterAutobid(
        Auction auction,
        Autobid winningAutobid,
        RegisterAutobidCommand command
    ) {
        BigDecimal maxBidAmount = command.getMaxBidAmount();
        if (winningAutobid == null) {
            BigDecimal systemAutoBidAmount = auction.getWinnerUserId() == null
                ? auction.getCurrentPrice()
                : auction.getCurrentPrice().add(auction.getMinimumBidStep());
            if (systemAutoBidAmount.compareTo(maxBidAmount) > 0) {
                systemAutoBidAmount = maxBidAmount;
            }
            return new AutobidResolution(
                true,
                null,
                auction.getId(),
                systemAutoBidAmount,
                command.getUserId(),
                auction.getStatus(),
                List.of(new BidRecordChange(
                    auction.getId(),
                    command.getUserId(),
                    systemAutoBidAmount,
                    BidType.SYSTEM_AUTO_BID
                )),
                List.of(),
                List.of(new BalanceChange(
                    command.getUserId(),
                    maxBidAmount,
                    BalanceChangeType.HOLD,
                    "AUTOBID_HOLD"
                ))
            );
        }

        if (maxBidAmount.compareTo(winningAutobid.getMaxBidAmount()) > 0) {
            BigDecimal systemAutoBidAmount = winningAutobid.getMaxBidAmount().add(auction.getMinimumBidStep());
            if (systemAutoBidAmount.compareTo(maxBidAmount) > 0) {
                systemAutoBidAmount = maxBidAmount;
            }

            return new AutobidResolution(
                true,
                null,
                auction.getId(),
                systemAutoBidAmount,
                command.getUserId(),
                auction.getStatus(),
                List.of(new BidRecordChange(
                    auction.getId(),
                    command.getUserId(),
                    systemAutoBidAmount,
                    BidType.SYSTEM_AUTO_BID
                )),
                List.of(new AutobidChange(
                    winningAutobid.getId(),
                    winningAutobid.getUserId(),
                    winningAutobid.getAuctionId(),
                    AutobidStatus.LOST,
                    winningAutobid.getMaxBidAmount(),
                    BigDecimal.ZERO
                )),
                List.of(
                    new BalanceChange(
                        winningAutobid.getUserId(),
                        winningAutobid.getHoldAmount(),
                        BalanceChangeType.RELEASE,
                        "AUTOBID_LOST_RELEASE"
                    ),
                    new BalanceChange(
                        command.getUserId(),
                        maxBidAmount,
                        BalanceChangeType.HOLD,
                        "AUTOBID_HOLD"
                    )
                )
            );
        }

        BigDecimal systemAutoBidAmount = maxBidAmount.add(auction.getMinimumBidStep());
        if (systemAutoBidAmount.compareTo(winningAutobid.getMaxBidAmount()) > 0) {
            systemAutoBidAmount = winningAutobid.getMaxBidAmount();
        }

        return new AutobidResolution(
            false,
            HIGHER_MAX_BID_MESSAGE,
            auction.getId(),
            systemAutoBidAmount,
            winningAutobid.getUserId(),
            auction.getStatus(),
            List.of(new BidRecordChange(
                auction.getId(),
                winningAutobid.getUserId(),
                systemAutoBidAmount,
                BidType.SYSTEM_AUTO_BID
            )),
            List.of(),
            List.of()
        );
    }

    public AutobidResolution resolveAfterIncreaseAutobid(
        Auction auction,
        List<Autobid> autobids,
        IncreaseAutobidCommand command
    ) {
        return null;
    }

    public AutobidResolution resolveAfterBuyNow(
        Auction auction,
        Autobid winningAutobid,
        BuyNowCommand command
    ) {
        if (winningAutobid == null) {
            return emptyAuctionEndedResolution(auction, command.getBuyerUserId(), command.getBuyNowPrice());
        }

        AutobidStatus newStatus = winningAutobid.getUserId() == command.getBuyerUserId()
            ? AutobidStatus.WON
            : AutobidStatus.LOST;

        return new AutobidResolution(
            true,
            null,
            auction.getId(),
            command.getBuyNowPrice(),
            command.getBuyerUserId(),
            AuctionStatus.ENDED,
            List.of(),
            List.of(new AutobidChange(
                winningAutobid.getId(),
                winningAutobid.getUserId(),
                winningAutobid.getAuctionId(),
                newStatus,
                winningAutobid.getMaxBidAmount(),
                BigDecimal.ZERO
            )),
            releaseIfPositive(
                winningAutobid.getUserId(),
                winningAutobid.getHoldAmount(),
                newStatus == AutobidStatus.WON ? "AUTOBID_WON_BUY_NOW_RELEASE" : "AUTOBID_LOST_RELEASE"
            )
        );
    }

    public AutobidResolution resolveAfterAuctionEnded(
        Auction auction,
        Autobid winningAutobid
    ) {
        if (winningAutobid == null) {
            return emptyAuctionEndedResolution(auction, auction.getWinnerUserId(), auction.getCurrentPrice());
        }

        BigDecimal releaseAmount = winningAutobid.getHoldAmount().subtract(auction.getCurrentPrice());

        return new AutobidResolution(
            true,
            null,
            auction.getId(),
            auction.getCurrentPrice(),
            auction.getWinnerUserId(),
            AuctionStatus.ENDED,
            List.of(),
            List.of(new AutobidChange(
                winningAutobid.getId(),
                winningAutobid.getUserId(),
                winningAutobid.getAuctionId(),
                AutobidStatus.WON,
                winningAutobid.getMaxBidAmount(),
                auction.getCurrentPrice()
            )),

            ///nếu autobid thắng thì release phần chênh lệch giữa hold amount và current price
            // nếu thua thì release toàn bộ hold amount
            releaseIfPositive(
                winningAutobid.getUserId(),
                releaseAmount,
                "AUTOBID_WON_EXCESS_RELEASE"
            )
        );
    }

    private AutobidResolution emptyAuctionEndedResolution(Auction auction, Long winnerUserId, BigDecimal currentPrice) {
        return new AutobidResolution(
            true,
            null,
            auction.getId(),
            currentPrice,
            winnerUserId,
            AuctionStatus.ENDED,
            List.of(),
            List.of(),
            List.of()
        );
    }

    private List<BalanceChange> releaseIfPositive(long userId, BigDecimal amount, String reason) {
        if (amount.compareTo(BigDecimal.ZERO) <= 0) {
            return List.of();
        }
        return List.of(new BalanceChange(
            userId,
            amount,
            BalanceChangeType.RELEASE,
            reason
        ));
    }
}

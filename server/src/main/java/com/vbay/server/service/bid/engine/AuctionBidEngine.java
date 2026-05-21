package com.vbay.server.service.bid.engine;

import java.math.BigDecimal;
import java.util.Optional;

import com.vbay.server.model.Auction;
import com.vbay.server.model.Autobid;
import com.vbay.server.model.Bid;
import com.vbay.server.service.bid.autobid.enums.AutobidStatus;
import com.vbay.server.service.bid.command.BuyNowCommand;
import com.vbay.server.service.bid.command.ManualBidCommand;
import com.vbay.server.service.bid.enums.BalanceChangeType;
import com.vbay.server.service.bid.resolution.model.BalanceChange;
import com.vbay.server.service.bid.resolution.model.PaymentCreate;
import com.vbay.server.service.bid.resolution.model.auction.BuyNowAuctionChange;
import com.vbay.server.service.bid.resolution.model.auction.CurrentBidAuctionChange;
import com.vbay.server.service.bid.resolution.model.autobid.AutobidStatusChange;
import com.vbay.server.service.bid.resolution.model.bid.BidCreate;
import com.vbay.server.service.bid.resolution.model.bid.BidResolution;
import com.vbay.server.service.bid.resolution.model.bid.BidStatusUpdate;
import com.vbay.server.service.bid.resolution.model.bid.MarkOtherBidsLostAfterBuyNow;
import com.vbay.shared.enums.bid.BidSource;
import com.vbay.shared.enums.bid.BidStatus;
import com.vbay.shared.enums.payment.PaymentStatus;
import com.vbay.shared.enums.payment.PaymentType;

public class AuctionBidEngine {
    public BidResolution resolveBuyNow(
            Auction auction,
            Optional<Bid> currentWinningBid,
            Optional<Autobid> winningAutobid,
            BuyNowCommand command) {
       long auctionId = auction.getId();
        long buyerId = command.getBuyerUserId();
        BigDecimal buyNowPrice = command.getBuyNowPrice();

        BidResolution.Builder resolution = BidResolution.accepted(auctionId);

        /*
        * 1. Nếu có winning AutoBid:
        *    - Buy Now thắng AutoBid.
        *    - Nếu buyer chính là AutoBid user: AutoBid WON. (vì ở bid history nếu buynow thì cx phải hiện thành won thì hợp lí hơn)
        *    - Nếu buyer khác: AutoBid LOST.
        *    - Release contract amount = maxBidAmount.
        */
        if (winningAutobid.isPresent()) {

            AutobidStatus status = winningAutobid.get().getUserId() == buyerId
                ? AutobidStatus.WON
                : AutobidStatus.LOST;

            resolution.addAutobidStatusChange(new AutobidStatusChange(
                winningAutobid.get().getId(),
                status
            ));

            resolution.addBalanceChange(new BalanceChange(
                winningAutobid.get().getUserId(),
                winningAutobid.get().getMaxBidAmount(),
                BalanceChangeType.RELEASE,
                status == AutobidStatus.WON
                    ? "AUTOBID_WON_BUY_NOW_RELEASE"
                    : "AUTOBID_LOST_RELEASE"
            ));
        }

        /*
        * 2. Nếu có currentWinningBid mà nó không phải bid của winning AutoBid:
        *    release hold theo bidAmount.
        *
        *    Nếu currentWinningBid là của winning AutoBid user thì KHÔNG release bidAmount,
        *    vì AutoBid đã release maxBidAmount ở bước 1.
        */
        if (currentWinningBid.isPresent()) {
            Bid oldBid = currentWinningBid.get();

            boolean oldBidCoveredByAutobidRelease = winningAutobid.isPresent()
                && oldBid.getBidderId() == winningAutobid.get().getUserId();

            if (!oldBidCoveredByAutobidRelease) {
                resolution.addBalanceChange(new BalanceChange(
                    oldBid.getBidderId(),
                    oldBid.getBidAmount(),
                    BalanceChangeType.RELEASE,
                    "OUTBID_RELEASE"
                ));
            }
        }

        /*
        * 3. Buyer trả Buy Now.
        *    Nếu buyer vừa được release hold ở bước trên thì net effect vẫn đúng:
        *    available += oldHold, available -= buyNowPrice.
        */
        resolution.addBalanceChange(new BalanceChange(
            buyerId,
            buyNowPrice,
            BalanceChangeType.DECREASE_AVAILABLE,///DEcREASE_AVAILABLE vì tiền sẽ chuyển sang payment HELD, không còn là “đặt cọc có thể release do outbid” nữa.
            "BUY_NOW_PAYMENT"
        ));

        /*
        * 4. Tạo bid Buy Now.
        *    Bid này sau applier.save sẽ có id.
        */
        resolution.addBidCreate(new BidCreate(
            BidSource.BUY_NOW,
            auctionId,
            buyerId,
            buyNowPrice,
            BidStatus.WON
        ));

        /*
        * 5. Mark các bid khác LOST sau khi BUY_NOW bid đã save.
        */
        resolution.addBidFinalization(new MarkOtherBidsLostAfterBuyNow(
            auctionId,
            BidSource.BUY_NOW
        ));

        /*
        * 6. Auction change dùng đa hình.
        */
        resolution.addAuctionChange(new BuyNowAuctionChange(
            auctionId,
            buyerId,
            buyNowPrice
        ));

        /*
        * 7. Tạo payment HELD, tham chiếu tới bid source BUY_NOW.
        *    Applier sẽ save bid trước, lấy id của BidCreate source BUY_NOW,
        *    rồi tạo Payment với bidId đó.
        */
        resolution.addPaymentCreate(new PaymentCreate(
            BidSource.BUY_NOW,
            auctionId,
            buyerId,
            auction.getSellerId(),
            buyNowPrice,
            PaymentType.BUY_NOW,
            PaymentStatus.HELD
        ));

        return resolution.build();
    }

    public BidResolution resolveManualBid
            (Auction auction,
            Optional<Bid> currentWinningBid,
            Optional<Autobid> winningAutobid,
            ManualBidCommand command) {

        long auctionId = auction.getId();
        long bidderId = command.getBidderUserId();
        BigDecimal manualAmount = command.getAmount();
        
        ///1. User có winning autobid thì không được phép đặt manual bid
        if (winningAutobid.isPresent() && winningAutobid.get().getUserId() == bidderId) {
            return BidResolution
                .rejected(auctionId, "CAI REJECT O SERVICE PHE CMNR")
                .build(); ///service check để throw validation exception
        }
        ///2. Không có winning autobid thì đặt manual bid bthg
        if (winningAutobid.isEmpty()) {
            BidResolution.Builder resolution = BidResolution.accepted(auctionId);
            /*
            if (currentWinningBid có giá trị) {

                Bid value = currentWinningBid.get();

                lambda.accept(value);
            }
            */
            currentWinningBid.ifPresent(
            oldBid -> {
                resolution.addBidStatusUpdate(new BidStatusUpdate(
                    oldBid.getId(),
                    BidStatus.OUTBID
                ));
                resolution.addBalanceChange(new BalanceChange(
                    oldBid.getBidderId(),
                    oldBid.getBidAmount(),
                    BalanceChangeType.RELEASE,
                    "OUTBID_RELEASE"
                ));

            });

            resolution.addBalanceChange(new BalanceChange(
                bidderId,
                manualAmount,
                BalanceChangeType.HOLD,
                "PLACE_BID_HOLD"
            ));
            
            resolution.addBidCreate(new BidCreate(
                BidSource.USER_BID,
                auctionId,
                bidderId,
                manualAmount,
                BidStatus.WINNING
            ));

            resolution.addAuctionChange(new CurrentBidAuctionChange(
                auctionId,
                bidderId,
                manualAmount
            ));

            return resolution.build();
        }
        Autobid activeWinningAutobid = winningAutobid.orElseThrow(() -> new IllegalStateException("Winning autobid should be present"));

         /*
         * 3. Nếu có winning AutoBid mà manual bid amount > max autobid:
         *    - Manual bid thắng AutoBid.
         *    - AutoBid LOST.
        */
        if (manualAmount.compareTo(activeWinningAutobid.getMaxBidAmount()) > 0) {
            BidResolution.Builder resolution = BidResolution.accepted(auctionId);
            currentWinningBid.ifPresent(
            oldBid -> {
                resolution.addBidStatusUpdate(new BidStatusUpdate(
                    oldBid.getId(),
                    BidStatus.OUTBID
                ));
                boolean oldBidCoveredByAutobidRelease = oldBid.getBidderId() == activeWinningAutobid.getUserId();

                if (!oldBidCoveredByAutobidRelease) {
                    resolution.addBalanceChange(new BalanceChange(
                        oldBid.getBidderId(),
                        oldBid.getBidAmount(),
                        BalanceChangeType.RELEASE,
                        "OUTBID_RELEASE"
                    ));
                }
            });
            resolution.addAutobidStatusChange(new AutobidStatusChange(
                activeWinningAutobid.getId(),
                AutobidStatus.LOST
            ));

            resolution.addBalanceChange(new BalanceChange(
                activeWinningAutobid.getUserId(),
                activeWinningAutobid.getMaxBidAmount(),
                BalanceChangeType.RELEASE,
                "AUTOBID_LOST_RELEASE"
            ));

            resolution.addBalanceChange(new BalanceChange(
                bidderId,
                manualAmount,
                BalanceChangeType.HOLD,
                "PLACE_BID_HOLD"
            ));

            resolution.addBidCreate(new BidCreate(
                BidSource.USER_BID,
                auctionId,
                bidderId,
                manualAmount,
                BidStatus.WINNING
            ));

            resolution.addAuctionChange(new CurrentBidAuctionChange(
                auctionId,
                bidderId,
                manualAmount
            ));
            return resolution.build();
        }

        BigDecimal autoBidAmount = manualAmount.add(auction.getMinimumBidStep());
        if (autoBidAmount.compareTo(activeWinningAutobid.getMaxBidAmount()) > 0) {
            autoBidAmount = activeWinningAutobid.getMaxBidAmount();
        }
        BidResolution.Builder resolution = BidResolution
            .rejected(auctionId, "Someone has already placed a higher maximum bid.");

        currentWinningBid.ifPresent(oldBid -> resolution.addBidStatusUpdate(new BidStatusUpdate(
            oldBid.getId(),
            BidStatus.OUTBID
        )));

        resolution.addBidCreate(new BidCreate(
            BidSource.AUTO_BID,
            auctionId,
            activeWinningAutobid.getUserId(),
            autoBidAmount,
            BidStatus.WINNING
        ));

        resolution.addAuctionChange(new CurrentBidAuctionChange(
            auctionId,
            activeWinningAutobid.getUserId(),
            autoBidAmount
        ));
        return resolution.build();
    }
}

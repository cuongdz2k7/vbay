package com.vbay.server.service.bid.engine;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Optional;

import com.vbay.server.model.Auction;
import com.vbay.server.model.Autobid;
import com.vbay.server.model.Bid;
import com.vbay.server.service.bid.command.BuyNowCommand;
import com.vbay.server.service.bid.command.IncreaseAutobidCommand;
import com.vbay.server.service.bid.command.ManualBidCommand;
import com.vbay.server.service.bid.command.RegisterAutobidCommand;
import com.vbay.server.service.bid.enums.AutobidStatus;
import com.vbay.server.service.bid.enums.BalanceChangeType;
import com.vbay.server.service.bid.resolution.model.BalanceChange;
import com.vbay.server.service.bid.resolution.model.PaymentCreate;
import com.vbay.server.service.bid.resolution.model.auction.AntiSnipeAuctionExtensionChange;
import com.vbay.server.service.bid.resolution.model.auction.BuyNowAuctionChange;
import com.vbay.server.service.bid.resolution.model.auction.CurrentBidAuctionChange;
import com.vbay.server.service.bid.resolution.model.autobid.AutobidCreate;
import com.vbay.server.service.bid.resolution.model.autobid.AutobidMaxBidUpdate;
import com.vbay.server.service.bid.resolution.model.autobid.AutobidStatusChange;
import com.vbay.server.service.bid.resolution.model.bid.BidCreate;
import com.vbay.server.service.bid.resolution.model.bid.BidResolution;
import com.vbay.server.service.bid.resolution.model.bid.BidStatusUpdate;
import com.vbay.server.service.bid.resolution.model.bid.MarkOtherBidsLostAfterBuyNow;
import com.vbay.shared.enums.bid.BidSource;
import com.vbay.shared.enums.bid.BidStatus;
import com.vbay.shared.enums.payment.PaymentStatus;
import com.vbay.shared.enums.payment.PaymentType;


/*
Nếu có winningAutobid(status = WINNING)
thì currentWinningBid phải tồn tại
và currentWinningBid.bidderId == winningAutobid.userId
và currentWinningBid.status == WINNING

BUG (fixed): Nếu max autobid >= reserve price thì hệ thống tự động đẩy current price lên reserve price, 
nhưng lúc này currentWinningBid.bidAmount vẫn chưa được đẩy lên reserve price
*/

public class AuctionBidEngine {
    private final AntiSnipePolicy antiSnipePolicy;

    public AuctionBidEngine(AntiSnipePolicy antiSnipePolicy) {
        this.antiSnipePolicy = antiSnipePolicy;
    }

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

        resolution.addBalanceChange(new BalanceChange(
            auction.getSellerId(),
            buyNowPrice,
            BalanceChangeType.DEPOSIT_AVAILABLE,
            "BUY_NOW_SELLER_RECEIPT"
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

        return resolution.build();
    }

    /*
    implement anti-snipe ở đây:
    */
    private void addAuctionChangesForWinningBid(
            BidResolution.Builder resolution,
            Auction auction,
            long winnerUserId,
            BigDecimal winningAmount,
            LocalDateTime bidTime) {
        resolution.addAuctionChange(new CurrentBidAuctionChange(
            auction.getId(),
            winnerUserId,
            winningAmount
        ));

        antiSnipePolicy.resolveExtendedEndingTime(auction, bidTime).ifPresent(
            endingTime -> resolution.addAuctionChange(
                new AntiSnipeAuctionExtensionChange(auction.getId(), endingTime)
            ));
    }

    private Bid requireWinningBidForAutobid(
            Optional<Bid> currentWinningBid,
            Autobid winningAutobid) {
        Bid oldWinningBid = currentWinningBid
            .orElseThrow(() -> new IllegalStateException(
                "Winning AutoBid exists without a winning bid"
            ));

        if (oldWinningBid.getBidderId() != winningAutobid.getUserId()) {
            throw new IllegalStateException(
                "Winning AutoBid user does not match current winning bid user"
            );
        }

        return oldWinningBid;
    }

    public BidResolution resolveManualBid
            (Auction auction,
            Optional<Bid> currentWinningBid,
            Optional<Autobid> winningAutobid,
            ManualBidCommand command) {

        long auctionId = auction.getId();
        long bidderId = command.getBidderUserId();
        BigDecimal manualAmount = command.getAmount();

        ///1. User có winning autobid thì không được phép đặt manual bid (đã check ở service)
        
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

            addAuctionChangesForWinningBid(
                resolution, 
                auction, 
                bidderId, 
                manualAmount, 
                command.getBidTime());

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
            Bid oldWinningBid = requireWinningBidForAutobid(currentWinningBid, activeWinningAutobid);

            resolution.addBidStatusUpdate(new BidStatusUpdate(
                oldWinningBid.getId(),
                BidStatus.OUTBID
            ));

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

            addAuctionChangesForWinningBid(
                resolution, 
                auction, 
                bidderId, 
                manualAmount, 
                command.getBidTime());
            return resolution.build();
        }

        BigDecimal autoBidAmount = manualAmount.add(auction.getMinimumBidStep());
        if (autoBidAmount.compareTo(activeWinningAutobid.getMaxBidAmount()) > 0) {
            autoBidAmount = activeWinningAutobid.getMaxBidAmount();
        }

        BidResolution.Builder resolution = BidResolution
            .rejected(auctionId, "Someone has already placed a higher maximum bid.");

        Bid oldWinningBid = requireWinningBidForAutobid(currentWinningBid, activeWinningAutobid);
        resolution.addBidStatusUpdate(new BidStatusUpdate(
            oldWinningBid.getId(),
            BidStatus.OUTBID
        ));

        resolution.addBidCreate(new BidCreate(
            BidSource.AUTO_BID,
            auctionId,
            activeWinningAutobid.getUserId(),
            autoBidAmount,
            BidStatus.WINNING
        ));

        addAuctionChangesForWinningBid(
            resolution,
            auction,
            activeWinningAutobid.getUserId(),
            autoBidAmount,
            command.getBidTime()
        );
        return resolution.build();
    }

    public BidResolution resolveRegisterAutobid(
            Auction auction,
            Optional<Bid> currentWinningBid,
            Optional<Autobid> winningAutobid,
            RegisterAutobidCommand command) {

        long auctionId = auction.getId();
        long userId = command.getUserId();
        BigDecimal maxBidAmount = command.getMaxBidAmount();

        /*
        1. Nếu không có winning AutoBid
        */
        if (winningAutobid.isEmpty()) {
            ///giá autobid sẽ đặt khi đăng kí, không phải là maxbid Amount
            ///service phải đảm bảo trước là maxbid amount phải > current price + min step
            BigDecimal autoBidAmount;
            /*
            1.5. currentWinningbid bidder chính là requester: autoBidAmount = currentWinningBid.bidAmount (để tránh việc tự động tăng giá lên nữa, vì thực tế người ta sẽ không muốn tự động tăng giá lên nữa nếu họ đã là người đang thắng)
             */
            if (currentWinningBid.isEmpty()) {
                autoBidAmount = auction.getStartPrice();
            } else {
                if (currentWinningBid.get().getBidderId() == userId) {
                    autoBidAmount = currentWinningBid.get().getBidAmount();
                } else {
                    autoBidAmount = currentWinningBid.get().getBidAmount().add(auction.getMinimumBidStep());
                }
            }

            if (autoBidAmount.compareTo(maxBidAmount) > 0) {
                autoBidAmount = maxBidAmount;
            }

            if (auction.getReservePrice() != null && maxBidAmount.compareTo(auction.getReservePrice()) >= 0) {
                if (autoBidAmount.compareTo(auction.getReservePrice()) < 0) {
                    autoBidAmount = auction.getReservePrice();
                }
            }

            BidResolution.Builder resolution = BidResolution.accepted(auctionId);

            currentWinningBid.ifPresent(oldBid -> {
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
                userId,
                maxBidAmount,
                BalanceChangeType.HOLD,
                "AUTOBID_HOLD"
            ));

            resolution.addAutobidCreate(new AutobidCreate(
                auctionId,
                userId,
                maxBidAmount,
                AutobidStatus.WINNING,
                command.getRegisteredAt(),
                command.getRegisteredAt()
            ));

            resolution.addBidCreate(new BidCreate(
                BidSource.AUTO_BID,
                auctionId,
                userId,
                autoBidAmount,
                BidStatus.WINNING
            ));

            addAuctionChangesForWinningBid(
                resolution,
                auction,
                userId,
                autoBidAmount,
                command.getRegisteredAt()
            );

            return resolution.build();
        }

        Autobid activeWinningAutobid = winningAutobid.get();
        ///2. nếu userId == winningAutobid thì cái này phải check ở service nó phải vào hàm update chứ không được hiện đăng kí nữa
        if (activeWinningAutobid.getUserId() == userId) {
            return BidResolution
                .rejected(auctionId, "You are already the winning AutoBid user")
                .build();
        }
        /*
        3. Nếu có winning AutoBid mà user đăng kí AutoBid mới có maxBidAmount > maxBidAmount của winning AutoBid:
         *    - AutoBid mới thắng AutoBid cũ.
         *    - AutoBid cũ LOST.
         *    - Nếu currentWinningBid là của winning AutoBid user thì KHÔNG release bidAmount,
         *      vì AutoBid đã release maxBidAmount ở bước 1.
         *    - Release maxBidAmount của AutoBid cũ.
         *    - Hold maxBidAmount của AutoBid mới.
        */
        if (maxBidAmount.compareTo(activeWinningAutobid.getMaxBidAmount()) > 0) {
            BigDecimal minimumBidStep = auction.getMinimumBidStep();
            BigDecimal reservePrice = auction.getReservePrice();
            BigDecimal autoBidAmount = activeWinningAutobid.getMaxBidAmount().add(minimumBidStep);

            if (autoBidAmount.compareTo(maxBidAmount) > 0) {
                autoBidAmount = maxBidAmount;
            }
            if (reservePrice != null && maxBidAmount.compareTo(reservePrice) >= 0) {
                if (autoBidAmount.compareTo(reservePrice) < 0) {
                    autoBidAmount = reservePrice;
                }
            }

            BidResolution.Builder resolution = BidResolution.accepted(auctionId);
            
            Bid oldWinningBid = requireWinningBidForAutobid(currentWinningBid, activeWinningAutobid);

            resolution.addBidStatusUpdate(new BidStatusUpdate(
                oldWinningBid.getId(),
                BidStatus.OUTBID
            ));

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
                userId,
                maxBidAmount,
                BalanceChangeType.HOLD,
                "AUTOBID_HOLD"
            ));

            resolution.addAutobidCreate(new AutobidCreate(
                auctionId,
                userId,
                maxBidAmount,
                AutobidStatus.WINNING,
                command.getRegisteredAt(),
                command.getRegisteredAt()
            ));

            resolution.addBidCreate(new BidCreate(
                BidSource.AUTO_BID,
                auctionId,
                userId,
                autoBidAmount,
                BidStatus.WINNING
            ));

            addAuctionChangesForWinningBid(
                resolution,
                auction,
                userId,
                autoBidAmount,
                command.getRegisteredAt()
            );

            return resolution.build();
        }

        BigDecimal autoBidAmount = maxBidAmount.add(auction.getMinimumBidStep());
        if (autoBidAmount.compareTo(activeWinningAutobid.getMaxBidAmount()) > 0) {
            autoBidAmount = activeWinningAutobid.getMaxBidAmount();
        }

        BidResolution.Builder resolution = BidResolution
            .rejected(auctionId, "Someone has already placed a higher maximum bid.");

        Bid oldWinningBid = requireWinningBidForAutobid(currentWinningBid, activeWinningAutobid);
        resolution.addBidStatusUpdate(new BidStatusUpdate(
            oldWinningBid.getId(),
            BidStatus.OUTBID
        ));

        resolution.addBidCreate(new BidCreate(
            BidSource.AUTO_BID,
            auctionId,
            activeWinningAutobid.getUserId(),
            autoBidAmount,
            BidStatus.WINNING
        ));

        addAuctionChangesForWinningBid(
            resolution,
            auction,
            activeWinningAutobid.getUserId(),
            autoBidAmount,
            command.getRegisteredAt()
        );

        return resolution.build();
    }
    
    public BidResolution resolveIncreaseMaxAutobidAmount(
            Auction auction,
            Optional<Bid> currentWinningBid,
            Autobid existingAutobid,
            IncreaseAutobidCommand command) {

        long auctionId = auction.getId();
        BigDecimal oldMaxBidAmount = existingAutobid.getMaxBidAmount();
        BigDecimal newMaxBidAmount = command.getNewMaxBidAmount();
        ///bảo hiểm nếu UI check ngu 
        if (existingAutobid.getUserId() != command.getUserId()) {
            throw new IllegalStateException("AutoBid does not belong to requester");
        }

        if (existingAutobid.getStatus() != AutobidStatus.WINNING) {
            return BidResolution
                .rejected(auctionId, "Only winning AutoBid can be increased")
                .build();
        }

        if (newMaxBidAmount.compareTo(oldMaxBidAmount) <= 0) {
            return BidResolution
                .rejected(auctionId, "New AutoBid max must be greater than current max")
                .build();
        }
        /*
        Nếu oldmaxbidAmount < reserve price <= newMaxBidAmount 
        */
       BigDecimal delta = newMaxBidAmount.subtract(oldMaxBidAmount);
       Bid oldWinningBid = requireWinningBidForAutobid(currentWinningBid, existingAutobid);
       
       BidResolution.Builder resolution = BidResolution.accepted(auctionId);
       
       if (auction.getReservePrice() != null && 
            newMaxBidAmount.compareTo(auction.getReservePrice()) >= 0 &&
            oldMaxBidAmount.compareTo(auction.getReservePrice()) < 0) {
                resolution.addBidStatusUpdate(new BidStatusUpdate(
                    oldWinningBid.getId(),
                    BidStatus.OUTBID
                ));
                resolution.addBidCreate(new BidCreate(
                    BidSource.AUTO_BID,
                    auctionId,
                    existingAutobid.getUserId(),
                    auction.getReservePrice(),
                    BidStatus.WINNING
                ));
                addAuctionChangesForWinningBid(
                    resolution,
                    auction,
                    existingAutobid.getUserId(),
                    auction.getReservePrice(),
                    command.getIncreasedAt()
                );
        }

        resolution.addBalanceChange(new BalanceChange(
            command.getUserId(),
            delta,
            BalanceChangeType.HOLD,
            "AUTOBID_INCREASE_HOLD"
        ));

        resolution.addAutobidMaxBidUpdate(new AutobidMaxBidUpdate(
            existingAutobid.getId(),
            newMaxBidAmount
        ));

        return resolution.build();
    }

    
}

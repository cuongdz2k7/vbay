package com.vbay.server.service.bid;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import com.google.gson.JsonElement;
import com.vbay.server.databaseManager.ConnectionProvider;
import com.vbay.server.exception.AuthenticationException;
import com.vbay.server.exception.ValidationException;
import com.vbay.server.model.Auction;
import com.vbay.server.model.Autobid;
import com.vbay.server.model.Bid;
import com.vbay.server.model.User;
import com.vbay.server.network_connection.ClientSession;
import com.vbay.server.realtime.domain.AuctionListItemUpdatedDomainEvent;
import com.vbay.server.realtime.domain.AutobidUpdatedDomainEvent;
import com.vbay.server.realtime.domain.BuyNowDomainEvent;
import com.vbay.server.realtime.domain.DomainEvent;
import com.vbay.server.realtime.domain.UserBalanceUpdatedDomainEvent;
import com.vbay.server.realtime.domain.enums.AuctionListItemUpdateReason;
import com.vbay.server.realtime.publisher.DomainEventPublisher;
import com.vbay.server.repository.AuctionRepository;
import com.vbay.server.repository.AutobidRepository;
import com.vbay.server.repository.BidRepository;
import com.vbay.server.repository.RepositoryFactory;
import com.vbay.server.repository.UserRepository;
import com.vbay.server.service.bid.command.BuyNowCommand;
import com.vbay.server.service.bid.engine.AuctionBidEngine;
import com.vbay.server.service.bid.enums.AutobidStatus;
import com.vbay.server.service.bid.resolution.AppliedBidResultMapper;
import com.vbay.server.service.bid.resolution.BidResolutionApplier;
import com.vbay.server.service.bid.resolution.model.bid.AppliedBidResolution;
import com.vbay.server.service.bid.resolution.model.bid.BidResolution;
import com.vbay.server.service.result.AuctionListItemResult;
import com.vbay.server.service.result.AutobidUpdateResult;
import com.vbay.server.service.result.BuyNowResult;
import com.vbay.server.service.result.UserBalanceResult;
import com.vbay.server.service.validation.ValidateBidDTO;
import com.vbay.shared.Utils.JsonUtils;
import com.vbay.shared.dto.auctionDTO.BuyNowRequest;
import com.vbay.shared.protocol.Respond;

/*


affectedMyBidItems.add(new bidder WINNING item)

và nếu có người đang thắng cũ:
java



affectedMyBidItems.add(old winning bidder OUTBID item)

Tức là mỗi lần bid, server chỉ gửi MY_BID_LIST_ITEM_UPDATED cho:
thằng vừa bid mới
thằng vừa bị mất WINNING
Nó không gửi cho những thằng đã OUTBID từ trước.
Case của bạn:
text



tamdz bid $10
user A bid $123
=> tamdz nhận OUTBID, currentPrice = $123

user B bid $251
=> server gửi event cho user B và user A
=> tamdz không nằm trong affectedMyBidItems
=> tamdz không nhận event
=> UI tamdz vẫn currentPrice = $123

*/


/*
các bước để place bid:
1. Insert bid mới
2. Update current_price của auction
3. Trừ tiền/cọc user
Authentication/Authorization check:
- Auction đó có tồn tại không?  \
- Auction đó đang ACTIVE không? \
- User có phải seller của auction đó không? \
- Bid amount có >= current_price + minimum_bid_step không? \

- User có bị banned/không đủ điều kiện không? 
- Nếu có available balance/đặt cọc thì đủ tiền không?
- Nếu có buy now price thì bid amount có >= buy now price không? 
    + nếu bid amount >= buynowprice, UI thông báo: “Your bid is higher than the Buy Now price. Do you want to buy immediately for $x?”
Cách xử lí placebid: 
1. start 1 connection transaction (để dễ rollback)
2. Lock Row Auction mà có id của lần đặt bid này (các bid vào cùng Auction sẽ bị lock)
3. lấy database dbNow = current time (đặt rule đây sẽ là time đặt bid của auction này)
4. sync status của database theo currentime đấy (bước này đã xử lí bằng bussiness validation rule đủ tốt, không nên thiết kế vì vấn đề rollback (****))
5. validation: status = active, starting_Time <= dbNow < ending_time
6. validation theo bussiness rule đặt ra 
7. update winning cũ -> outbid
8. insert bid mới -> winning với bidtime = dbNow
9. Update auction current_price, winner_user_id 
10. commit transaction

các method cần: 
    -> Lock Auction by id
    -> Get DataBase Time
    -> sync status
    -> Update currenbid

Nếu lock user trước rồi mới lock auction, user A có thể bị giữ ví trong lúc chờ auction lock. 
Như vậy hơi “oan”: A chưa chắc bid được, nhưng các thao tác tiền khác của A đã bị chặn.
-> lock Auction trước -> validate -> lock user để thay đổi balance
Thiết kế Buy Now: 
1. Tìm old Winning Bid 
2. nếu có thì release hold của bidder đấy
3. cập nhật oldBid thành outbid
4. trừ available balance thằng buyer



Rule AutoBid:
- Max Autobid phải nhỏ hơn buynowprice của Auction, nếu không có buynow thì unlimit
- Sau này có thể thêm setting rõ AllowAutoBuyNow trong bảng đặt tham số autoBid

Buy now chuyển khỏi ví buyer vào payments HELD ngay:
    buyer available -= buyNowPrice
    payment -> HELD
Sau khi release:
    seller available += buyNowPrice
    payment -> RELEASED
Còn nếu refund:
    seller bị penalty
    buyer available += buyNowPrice
    payment -> REFUNDED

nên tách nghiệp vụ placebid ra nếu placebid >= buynowprice thì client check đồng nghĩa với việc ấn vào buy now cho dễ xử lí tách nghiệp vụ, thì placebid nó chắc chắn không thể là buy now


Nên dùng Cách B cho buy now và auction kết thúc, nó đúng nghiệp vụ “escrow/platform holding”, tiền không còn nằm trong ví buyer nữa. 
Vì hold_balance nên dùng cho bid đang có thể bị outbid. Còn buy now đã mua xong, không còn khả năng bị outbid, nên tiền nên chuyển sang chỗ payment thì đúng hơn.

Bug placebid đúng bằng currentprice đưuọc nếu 
BUG buy now: thằng đnag winning đi buy now chính auction của mình
-> phải check cả hold balance của nó nữa
ví dụ nó đang winningbid 50$ thì lần bid tiếp theo chỉ trừ của nó 10$ (buy now tương tự)

Cách xử lí bên UI:
Hiển thị "Buying Power" (Sức mua)
Thay vì chỉ hiện số dư khả dụng, hãy hiện thêm một dòng nhỏ:
    Số dư ví: 50$
    Đang đặt chỗ: 100$ (tại món hàng này)
    Tổng sức mua cho món này: 150$
    -> vấn đề ở đây là mình CHO PHÉP thằng đang thắng đc bid thêm
*/

public class BuyNowService {
    ///tạo connection provider để sử dụng h2 in-memory database cho integration test, tránh ảnh hưởng đến database thật khi test
    private final ConnectionProvider connectionProvider;
    private final RepositoryFactory repositoryFactory;
    private final DomainEventPublisher domainEventPublisher;
    private final AuctionBidEngine auctionBidEngine;
    private final BidResolutionApplier bidResolutionApplier;

    public BuyNowService
        (ConnectionProvider connectionProvider, 
        RepositoryFactory repositoryFactory, 
        DomainEventPublisher domainEventPublisher, 
        AuctionBidEngine auctionBidEngine,
        BidResolutionApplier bidResolutionApplier) {
        this.connectionProvider = connectionProvider;
        this.repositoryFactory = repositoryFactory;
        this.domainEventPublisher = domainEventPublisher;
        this.auctionBidEngine = auctionBidEngine;
        this.bidResolutionApplier = bidResolutionApplier;
    }

    private void checkSession(ClientSession session) {
        if (session == null || !session.isAuthenticated()) {
            throw new AuthenticationException("User must be logged in to perform this action");
        }
    }

    private boolean isCurrentWinner(long userId, Optional<Bid> currentWinningBid) {
        return currentWinningBid.isPresent() && currentWinningBid.get().getBidderId() == userId;
    }
    
    private void validateUserEligibility (User user, BigDecimal currentAmount, Optional<Bid> currentWinningBid) throws SQLException {
        if (!user.isActive()) {
            throw new ValidationException("This user cannot use this action");
        }
        BigDecimal trueAvailable = user.getAvailableBalance();
        if (isCurrentWinner(user.getId(), currentWinningBid)) {
            BigDecimal currentWinningBidAmount = currentWinningBid.get().getBidAmount();
            trueAvailable = trueAvailable.add(currentWinningBidAmount);
        }
        if (trueAvailable.compareTo(currentAmount) < 0) {
            throw new ValidationException("Insufficient balance");
        }
    }

    private void validateAuctionEligibility (Auction auction, long userId, LocalDateTime dbNow) throws SQLException {
        if (auction.getStatus().isClosedForBidding()) {
            throw new ValidationException("Auction cannot receive bids");
        }
        if (dbNow.isBefore(auction.getStartingTime())) {
            throw new ValidationException("Auction is not started yet");
        }
        if (!dbNow.isBefore(auction.getEndingTime())) {
            throw new ValidationException("Auction has already ended");
        }
        if (userId == auction.getSellerId()) {
            throw new ValidationException("Cannot place bid on your own auction");
        }
    }

    private void validateWinningAutobidInvariant(
            Optional<Bid> currentWinningBid,
            Optional<Autobid> winningAutobid) {
        if (winningAutobid.isEmpty()) {
            return;
        }

        Bid bid = currentWinningBid.orElseThrow(
            () -> new ValidationException("Winning AutoBid exists without a winning bid")
        );
        if (bid.getBidderId() != winningAutobid.get().getUserId()) {
            throw new ValidationException("Winning AutoBid user does not match current winning bid user");
        }
    }

    public BuyNowResult buyNow (BuyNowRequest request, ClientSession session) throws SQLException {
        checkSession(session);

        ValidateBidDTO.validateBuyNowRequest(request);
        List<DomainEvent> events = new ArrayList<>();
        BuyNowResult result;
        try (Connection connection = connectionProvider.getConnection()) {
            connection.setAutoCommit(false);
            boolean committed = false;
            try {
                AuctionRepository auctionRepository = repositoryFactory.createAuctionRepository(connection);
                UserRepository userRepository = repositoryFactory.createUserRepository(connection);
                BidRepository bidRepository = repositoryFactory.createBidRepository(connection);
                AutobidRepository autobidRepository = repositoryFactory.createAutobidRepository(connection);

                //1. lock auction
                Auction auction = auctionRepository.lockAuctionForUpdate(request.getAuctionId())
                    .orElseThrow(() -> new ValidationException("Auction not found"));
                long auctionId = auction.getId();
                LocalDateTime dbNow = auctionRepository.getCurrentDatabaseTime();

                Optional<Bid> currentWinningBid = bidRepository.findWinningBidByAuctionId(auctionId);
                Optional<Autobid> winningAutobid = autobidRepository.findWinningByAuctionId(auctionId);
                validateWinningAutobidInvariant(currentWinningBid, winningAutobid);
                
                validateAuctionEligibility (auction, session.getUserId(), dbNow);

                if (auction.getBuyNowPrice() == null) {
                    throw new ValidationException("Buy Now is not available for this auction");
                }

                User user = userRepository.lockUserForUpdate(session.getUserId())
                    .orElseThrow(() -> new ValidationException("User not found"));
                validateUserEligibility(user, auction.getBuyNowPrice(), currentWinningBid);
                
                ///sau khi validate xong xuôi -> currentwinningbid sẽ thành oldwinningbid
                Optional<Bid> oldWinningBid = currentWinningBid;
                Long previousWinningBidId = oldWinningBid.map(Bid::getId).orElse(null);
                Long previousWinningUserId = oldWinningBid.map(Bid::getBidderId).orElse(null);
                BidResolution resolution = auctionBidEngine.resolveBuyNow(
                    auction,
                    oldWinningBid,
                    winningAutobid,
                    new BuyNowCommand(
                        auctionId,
                        session.getUserId(),
                        auction.getBuyNowPrice(),
                        dbNow
                    )
                );
                AppliedBidResolution applied = bidResolutionApplier.apply(
                    resolution,
                    dbNow,
                    connection
                );

                ///tạo result trước commit để trong trường hợp có lỗi ở bước tạo result thì sẽ rollback được, tránh trường hợp đã commit rồi mà tạo result lỗi thì sẽ mất đồng bộ giữa state và event
                AuctionListItemResult listItem = auctionRepository.findAuctionListItemById(auctionId)
                    .orElseThrow(() -> new ValidationException("Auction list item not found"));

                result = AppliedBidResultMapper.toBuyNowResult(
                    applied,
                    previousWinningUserId,
                    previousWinningBidId,
                    dbNow
                );

                ///publish event
                events.add(new AuctionListItemUpdatedDomainEvent(
                    listItem,
                    AuctionListItemUpdateReason.STATUS_CHANGED,
                    dbNow
                ));
                ///publish buy now event đã bao gồm mybidlistitemresult bên trong rồi nên không cần publish thêm event update mybidlistitem nữa, client nhận buy now event sẽ update mybidlistitem luôn
                events.add(new BuyNowDomainEvent(result));
                if (winningAutobid.isPresent()) {
                    Autobid oldAutobid = winningAutobid.get();
                    AutobidStatus finalStatus = oldAutobid.getUserId() == session.getUserId()
                        ? AutobidStatus.WON
                        : AutobidStatus.LOST;
                    events.add(new AutobidUpdatedDomainEvent(
                        toAutobidUpdateResult(
                            applied.getRefreshedAuction(),
                            oldAutobid,
                            finalStatus,
                            dbNow
                        )
                    ));
                }
                for (UserBalanceResult balanceResult : applied.getBalanceResults()) {
                    events.add(new UserBalanceUpdatedDomainEvent(
                        balanceResult,
                        balanceResult.getUpdatedAt()
                    ));
                }
                connection.commit();
                committed = true;
            } catch (Exception e) {
                if (!committed) {
                    connection.rollback();
                }
                throw e;
            }
        }
        for (DomainEvent event : events) {
            domainEventPublisher.publish(event);
        }
        return result;
    }
    
    public Respond<Void> handleBuyNow(String requestId, JsonElement payload, ClientSession session) throws SQLException {
        BuyNowRequest buyNowRequest = JsonUtils.fromJson(payload, BuyNowRequest.class);
        if (buyNowRequest == null) {
            return new Respond<>(requestId, false, "Invalid buy now request", null);
        }
        buyNow(buyNowRequest, session);
        return new Respond<>(requestId, true, "Buy now completed successfully", null);
    }

    private AutobidUpdateResult toAutobidUpdateResult(
            Auction auction,
            Autobid autobid,
            AutobidStatus status,
            LocalDateTime updatedAt) {
        boolean showActiveMaxBid = status == AutobidStatus.WINNING
            && auction.getWinnerUserId() != null
            && auction.getWinnerUserId() == autobid.getUserId();

        return new AutobidUpdateResult(
            auction.getId(),
            autobid.getUserId(),
            autobid.getId(),
            autobid.getMaxBidAmount(),
            status,
            showActiveMaxBid,
            showActiveMaxBid,
            updatedAt
        );
    }
}

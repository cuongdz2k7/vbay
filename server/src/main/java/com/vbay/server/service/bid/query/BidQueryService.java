package com.vbay.server.service.bid.query;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import com.vbay.server.databaseManager.ConnectionProvider;
import com.vbay.server.exception.AuthenticationException;
import com.vbay.server.exception.ValidationException;
import com.vbay.server.model.Auction;
import com.vbay.server.model.Autobid;
import com.vbay.server.model.Bid;
import com.vbay.server.model.Payment;
import com.vbay.server.model.User;
import com.vbay.server.network_connection.ClientSession;
import com.vbay.server.realtime.domain.AuctionListItemUpdatedDomainEvent;
import com.vbay.server.realtime.domain.BidUpdatedDomainEvent;
import com.vbay.server.realtime.domain.BuyNowDomainEvent;
import com.vbay.server.realtime.domain.UserBalanceUpdatedDomainEvent;
import com.vbay.server.realtime.domain.enums.AuctionListItemUpdateReason;
import com.vbay.server.realtime.publisher.DomainEventPublisher;
import com.vbay.server.repository.AuctionRepository;
import com.vbay.server.repository.AutobidRepository;
import com.vbay.server.repository.BidRepository;
import com.vbay.server.repository.PaymentRepository;
import com.vbay.server.repository.ProductImageRepository;
import com.vbay.server.repository.RepositoryFactory;
import com.vbay.server.repository.UserRepository;
import com.vbay.server.service.bid.autobid.AutobidEngine;
import com.vbay.server.service.bid.autobid.AutobidService;
import com.vbay.server.service.bid.autobid.enums.AutobidStatus;
import com.vbay.server.service.bid.autobid.model.AutobidChange;
import com.vbay.server.service.bid.autobid.model.AutobidResolution;
import com.vbay.server.service.bid.command.BuyNowCommand;
import com.vbay.server.service.bid.command.ManualBidCommand;
import com.vbay.server.service.bid.command.RegisterAutobidCommand;
import com.vbay.server.service.bid.enums.BalanceChangeType;
import com.vbay.server.service.bid.resolution.model.BalanceChange;
import com.vbay.server.service.result.AuctionListItemResult;
import com.vbay.server.service.result.AutobidRegistrationResult;
import com.vbay.server.service.result.BuyNowResult;
import com.vbay.server.service.result.PlaceBidResult;
import com.vbay.server.service.result.UserBalanceResult;
import com.vbay.server.service.result.UserMyBidListItemResult;
import com.vbay.server.service.result.mapper.ResultMapper;
import com.vbay.server.service.validation.ValidateBidDTO;
import com.vbay.shared.dto.auctionDTO.BuyNowRequest;
import com.vbay.shared.dto.auctionDTO.MyBidListResponse;
import com.vbay.shared.dto.auctionDTO.PlaceBidRequest;
import com.vbay.shared.enums.bid.BidSource;
import com.vbay.shared.enums.bid.BidStatus;
import com.vbay.shared.enums.payment.PaymentStatus;
import com.vbay.shared.enums.payment.PaymentType;

/*
BUG:
Trong BidService.java, hàm placeBid(...) hiện chỉ tạo affectedMyBidItems cho:
java



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

public class BidQueryService {
    ///tạo connection provider để sử dụng h2 in-memory database cho integration test, tránh ảnh hưởng đến database thật khi test
    private final ConnectionProvider connectionProvider;
    private final RepositoryFactory repositoryFactory;
    private final DomainEventPublisher domainEventPublisher;
    private final AutobidEngine autobidEngine;
    private final AutobidService autobidService;


    public BidQueryService
        (ConnectionProvider connectionProvider, 
        RepositoryFactory repositoryFactory, 
        DomainEventPublisher domainEventPublisher) {
        this(connectionProvider, repositoryFactory, domainEventPublisher, new AutobidEngine(), new AutobidService(connectionProvider, repositoryFactory));
    }

    public BidQueryService
        (ConnectionProvider connectionProvider, 
        RepositoryFactory repositoryFactory, 
        DomainEventPublisher domainEventPublisher, 
        AutobidEngine autobidEngine) {
        this(connectionProvider, repositoryFactory, domainEventPublisher, autobidEngine, new AutobidService(connectionProvider, repositoryFactory));
    }

    public BuyNowService
        (ConnectionProvider connectionProvider, 
        RepositoryFactory repositoryFactory, 
        DomainEventPublisher domainEventPublisher, 
        AutobidEngine autobidEngine,
        AutobidService autobidService) {
        this.connectionProvider = connectionProvider;
        this.repositoryFactory = repositoryFactory;
        this.domainEventPublisher = domainEventPublisher;
        this.autobidEngine = autobidEngine;
        this.autobidService = autobidService;
    }

    private void checkSession(ClientSession session) {
        if (session == null || !session.isAuthenticated()) {
            throw new AuthenticationException("User must be logged in to perform this action");
        }
    }

    private UserBalanceResult readUserBalanceResult(
            UserRepository userRepository,
            long userId,
            String reason,
            LocalDateTime updatedAt) throws SQLException {
        User user = userRepository.findById(userId).orElseThrow(
            () -> new ValidationException("User not found")
        );
        return ResultMapper.toUserBalanceResult(user, reason, updatedAt);
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
        ///sai vì có thể thằng này nó đang winning => ví dụ nó đang winningbid 50$ thì lần bid tiếp theo chỉ trừ của nó 10$ (buy now tương tự)
        /*
            if (user.getAvailableBalance().compareTo(bidAmount) < 0) {
                throw new ValidationException("Insufficient available balance");
            }
        */
    }

    private void validateBuyNowUserEligibility(
            User user,
            BigDecimal buyNowPrice,
            Optional<Bid> currentWinningBid,
            Optional<Autobid> winningAutobid) throws SQLException {
        if (!user.isActive()) {
            throw new ValidationException("This user cannot use this action");
        }

        BigDecimal trueAvailable = user.getAvailableBalance();
        if (winningAutobid.isPresent() && winningAutobid.get().getUserId() == user.getId()) {
            trueAvailable = trueAvailable.add(winningAutobid.get().getHoldAmount());
        } else if (isCurrentWinner(user.getId(), currentWinningBid)) {
            trueAvailable = trueAvailable.add(currentWinningBid.get().getBidAmount());
        }

        if (trueAvailable.compareTo(buyNowPrice) < 0) {
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
    
    private void checkAutoBidBuyNowLimit(Auction auction, BigDecimal maxBidAmount) {
        if (auction.getBuyNowPrice() != null && maxBidAmount.compareTo(auction.getBuyNowPrice()) >= 0) {
            throw new ValidationException("Max AutoBid must be lower than the Buy Now price");
        }
    }

    private void rejectIfUserAlreadyHasWinningAutobid(Autobid winningAutobid, long userId) {
        if (winningAutobid != null && winningAutobid.getUserId() == userId) {
            throw new ValidationException("You are already the winning AutoBid user");
        }
    }

    public MyBidListResponse getMyBidList(ClientSession session) throws SQLException {
        checkSession(session);

        try (Connection connection = connectionProvider.getConnection()) {
            BidRepository bidRepository = repositoryFactory.createBidRepository(connection);
            AuctionRepository auctionRepository = repositoryFactory.createAuctionRepository(connection);
            ProductImageRepository productImageRepository = repositoryFactory.createProductImageRepository(connection);

            List<com.vbay.shared.dto.realtimeDTO.payload.MyBidListItemPayload> items = new ArrayList<>();
            for (Bid bid : bidRepository.findLatestBidsByBidderId(session.getUserId())) {
                Auction auction = auctionRepository.findById(bid.getAuctionId())
                    .orElseThrow(() -> new ValidationException("Auction not found"));
                String thumbnailUrl = productImageRepository.findThumbnailUrlByProductId(auction.getProductId()).orElse(null);
                UserMyBidListItemResult result = ResultMapper.toUserMyBidListItemResult(
                    auction,
                    thumbnailUrl,
                    bid,
                    bid.getStatus(),
                    bid.getBidTime()
                );
                items.add(ResultMapper.toMyBidListItemPayload(result));
            }
            return new MyBidListResponse(items);
        }
    }

    private void validateMinimumBid(Auction auction, BigDecimal bidAmount, Optional<Bid> currentWinningBid) {
        BigDecimal minimumBid = currentWinningBid.isEmpty()
            ? auction.getCurrentPrice()
            : auction.getCurrentPrice().add(auction.getMinimumBidStep());
        if (bidAmount.compareTo(minimumBid) < 0) {
            throw new ValidationException("Bid amount must be at least " + minimumBid);
        }
    }
     /*
    Nếu lock user trước rồi mới lock auction, user A có thể bị giữ ví trong lúc chờ auction lock. 
    Như vậy A chưa chắc bid được, nhưng các thao tác tiền khác của A đã bị chặn.
    Điều này ảnh hưởng lớn đến các thao tác autobid, khi thằng a nó đến thời điểm đặt bid, chưa bid được nma nó đc cộng tiền bởi 1 Auction khác (bị outbid chẳng hạn)
    Flow nên là: lock Auction trước -> validate -> lock user để thay đổi balance
    */
    private PlaceBidResult placeBid (long auctionId, long bidderId, BigDecimal bidAmount, Optional<Bid> oldWinningBid, LocalDateTime dbNow, Connection connection) throws SQLException {
        BidRepository bidRepository = repositoryFactory.createBidRepository(connection);
        UserRepository userRepository = repositoryFactory.createUserRepository(connection);
        AuctionRepository auctionRepository = repositoryFactory.createAuctionRepository(connection);
        ProductImageRepository productImageRepository = repositoryFactory.createProductImageRepository(connection);
        //1. Release Hold và Update Status
        Long previousWinningUserId = null;
        Long previousWinningBidId = null;
        if (!oldWinningBid.isEmpty()) {
            Bid oldBid = oldWinningBid.get();
            previousWinningUserId = oldBid.getBidderId();
            previousWinningBidId = oldBid.getId();
            userRepository.releaseHoldBalance(oldBid.getBidderId(), oldBid.getBidAmount());

            bidRepository.updateStatus(oldBid.getId(), BidStatus.OUTBID);
        }
        //2. Trừ tiền bidder
        userRepository.holdBalance(bidderId, bidAmount);
        ///3. save bid
        Bid currentBid = new Bid(
            auctionId,
            bidderId,
            bidAmount,
            dbNow,
            BidStatus.WINNING,
            BidSource.USER_BID
        );
        bidRepository.save(currentBid);
        ///4. Update Auction
        auctionRepository.updateCurrentBid(auctionId, bidAmount, bidderId);
        Auction refreshedAuction = auctionRepository.findById(auctionId).orElseThrow(() -> new ValidationException("Auction not found"));
        String thumbnailUrl = productImageRepository.findThumbnailUrlByProductId(refreshedAuction.getProductId()).orElse(null);
        List<UserMyBidListItemResult> affectedMyBidItems = buildAffectedMyBidItems(
            refreshedAuction,
            thumbnailUrl,
            bidRepository,
            dbNow
        );

        return ResultMapper.toPlaceBidResult(
            refreshedAuction,
            currentBid,
            previousWinningUserId,
            previousWinningBidId,
            affectedMyBidItems
        );
    }

    ///manual
    public PlaceBidResult placeBid(PlaceBidRequest request, ClientSession session) throws SQLException {
        checkSession(session);

        ValidateBidDTO.validatePlaceBidRequest(request);
        ///Authentication/Authorization check:
        try (Connection connection = connectionProvider.getConnection()) {
            connection.setAutoCommit(false);
            try {
                AuctionRepository auctionRepository = repositoryFactory.createAuctionRepository(connection);
                UserRepository userRepository = repositoryFactory.createUserRepository(connection);
                BidRepository bidRepository = repositoryFactory.createBidRepository(connection);
                AutobidRepository autobidRepository = repositoryFactory.createAutobidRepository(connection);
                //1. lock auction 
                Auction auction = auctionRepository.lockAuctionForUpdate(request.getAuctionId())
                .orElseThrow(() -> new ValidationException("Auction not found"));
                
                long auctionId = auction.getId();
                //2. lấy thời gian hiện tại -> đặt nó là thời gian đặt bid, phải dùng dbTimezone do rule quyết định lấy time trong DB làm mốc chuẩn
                LocalDateTime dbNow = auctionRepository.getCurrentDatabaseTime();
                
                validateAuctionEligibility(auction, session.getUserId(), dbNow);
                
                checkBuyNowIfBypassedUI(auction, request.getBidAmount());
                Optional<Bid> currentWinningBid = bidRepository.findWinningBidByAuctionId(auctionId);
                /* 
                    boolean buyNow = canBuyNow(auction, request.getBidAmount());
                    ///vì buynow có thể ít hơn current price + bước nhảy
                    if (!buyNow) {
                        validateMinimumBid(auction, request.getBidAmount());
                    }
                */
                validateMinimumBid(auction, request.getBidAmount(), currentWinningBid);
                Autobid winningAutobid = autobidRepository.findWinningByAuctionId(auctionId).orElse(null);

                //3. Lock Auction validate xong xuôi rồi mới lock user
                AutobidResolution resolution = null;
                ///sau khi validate xong xuôi -> currentwinningbid sẽ thành oldwinningbid
                
                Optional<Bid> oldWinningBid = currentWinningBid;
                PlaceBidResult result;
                ///xác định xem có phải trigfer autobid không
                if (winningAutobid == null) {
                    User user = userRepository.lockUserForUpdate(session.getUserId())
                        .orElseThrow(() -> new ValidationException("User not found"));
                    validateUserEligibility(user, request.getBidAmount(), currentWinningBid);
                    result = placeBid(auction.getId(), session.getUserId(), request.getBidAmount(), oldWinningBid, dbNow, connection);
                } else {
                    if (session.getUserId() == winningAutobid.getUserId()) {
                        throw new ValidationException("Winning autobid user cannot place a manual bid on this auction");
                    }
                    User user = userRepository.lockUserForUpdate(session.getUserId())
                        .orElseThrow(() -> new ValidationException("User not found"));
                    validateUserEligibility(user, request.getBidAmount(), currentWinningBid);

                    ManualBidCommand command = new ManualBidCommand(
                        auctionId,
                        session.getUserId(),
                        request.getBidAmount(),
                        dbNow
                    );
                    resolution = autobidEngine.resolveAfterManualBid(auction, winningAutobid, command);
                    if (resolution.isAccepted()) {
                        userRepository.holdBalance(session.getUserId(), request.getBidAmount());
                    }
                    result = autobidService.applyManualBidResolution(resolution, oldWinningBid, dbNow, connection);
                }
                
                AuctionListItemResult listItem = auctionRepository.findAuctionListItemById(auctionId)
                    .orElseThrow(() -> new ValidationException("Auction list item not found"));
                
                List<UserBalanceResult> balanceResults = new ArrayList<>();
                LocalDateTime balanceUpdatedAt = dbNow;
                if (resolution == null) {
                    balanceResults.add(readUserBalanceResult(
                        userRepository,
                        ///tạo result
                        session.getUserId(),
                        "PLACE_BID_HOLD",
                        balanceUpdatedAt
                    ));
                    Long previousWinningUserId = result.getPreviousWinningUserId();
                    if (previousWinningUserId != null && previousWinningUserId != session.getUserId().longValue()) {
                        balanceResults.add(readUserBalanceResult(
                            userRepository,
                            previousWinningUserId,
                            "OUTBID_RELEASE",
                            balanceUpdatedAt
                        ));
                    }
                } else {
                    if (resolution.isAccepted()) {
                        balanceResults.add(readUserBalanceResult(
                            userRepository,
                            session.getUserId(),
                            "PLACE_BID_HOLD",
                            balanceUpdatedAt
                        ));
                    }
                    for (BalanceChange balanceChange : resolution.getBalanceChanges()) {
                        balanceResults.add(readUserBalanceResult(
                            userRepository,
                            balanceChange.getUserId(),
                            balanceChange.getReason(),
                            balanceUpdatedAt
                        ));
                    }
                }
                
                connection.commit();
                ///publish event
                domainEventPublisher.publish(new AuctionListItemUpdatedDomainEvent(
                    listItem,
                    AuctionListItemUpdateReason.BID_UPDATED,
                    dbNow
                ));
                domainEventPublisher.publish(new BidUpdatedDomainEvent(result));
                for (UserBalanceResult balanceResult : balanceResults) {
                    domainEventPublisher.publish(new UserBalanceUpdatedDomainEvent(balanceResult, balanceResult.getUpdatedAt()));
                }

                ///nếu có autobid resolution mà bị reject, cũng gửi event để UI hiện message
                if (resolution != null && !resolution.isAccepted()) {
                    throw new ValidationException(resolution.getMessage());
                }
                return result;
            } catch (Exception e) {
                connection.rollback(); ///rollback là nhả lock luôn
                throw e;
            } 
        }
    }

    public AutobidRegistrationResult autoBid(long auctionId, BigDecimal maxBidAmount, ClientSession session) throws SQLException {
        checkSession(session);
        ValidateBidDTO.validateAutoBidRequest(auctionId, maxBidAmount);

        try (Connection connection = connectionProvider.getConnection()) {
            connection.setAutoCommit(false);
            try {
                AuctionRepository auctionRepository = repositoryFactory.createAuctionRepository(connection);
                UserRepository userRepository = repositoryFactory.createUserRepository(connection);
                BidRepository bidRepository = repositoryFactory.createBidRepository(connection);
                AutobidRepository autobidRepository = repositoryFactory.createAutobidRepository(connection);

                Auction auction = auctionRepository.lockAuctionForUpdate(auctionId)
                    .orElseThrow(() -> new ValidationException("Auction not found"));
                LocalDateTime dbNow = auctionRepository.getCurrentDatabaseTime();

                validateAuctionEligibility(auction, session.getUserId(), dbNow);
                checkAutoBidBuyNowLimit(auction, maxBidAmount);

                Optional<Bid> currentWinningBid = bidRepository.findWinningBidByAuctionId(auctionId);
                validateMinimumBid(auction, maxBidAmount, currentWinningBid);

                Autobid winningAutobid = autobidRepository.findWinningByAuctionId(auctionId).orElse(null);
                rejectIfUserAlreadyHasWinningAutobid(winningAutobid, session.getUserId());

                User user = userRepository.lockUserForUpdate(session.getUserId())
                    .orElseThrow(() -> new ValidationException("User not found"));
                validateUserEligibility(user, maxBidAmount, currentWinningBid);

                RegisterAutobidCommand command = new RegisterAutobidCommand(
                    auctionId,
                    session.getUserId(),
                    maxBidAmount,
                    dbNow
                );
                AutobidResolution resolution = autobidEngine.resolveAfterRegisterAutobid(
                    auction,
                    winningAutobid,
                    command
                );

                Autobid autobidToCreate = resolution.isAccepted()
                    ? new Autobid(
                        auctionId,
                        session.getUserId(),
                        maxBidAmount,
                        maxBidAmount,
                        AutobidStatus.WINNING,
                        dbNow,
                        dbNow
                    )
                    : null;

                AutobidRegistrationResult result = autobidService.applyRegisterAutobidResolution(
                    resolution,
                    autobidToCreate,
                    currentWinningBid,
                    dbNow,
                    connection
                );

                AuctionListItemResult listItem = auctionRepository.findAuctionListItemById(auctionId)
                    .orElseThrow(() -> new ValidationException("Auction list item not found"));
                List<UserBalanceResult> balanceResults = new ArrayList<>();
                for (BalanceChange balanceChange : resolution.getBalanceChanges()) {
                    balanceResults.add(readUserBalanceResult(
                        userRepository,
                        balanceChange.getUserId(),
                        balanceChange.getReason(),
                        dbNow
                    ));
                }

                connection.commit();

                domainEventPublisher.publish(new AuctionListItemUpdatedDomainEvent(
                    listItem,
                    AuctionListItemUpdateReason.BID_UPDATED,
                    dbNow
                ));
                domainEventPublisher.publish(new BidUpdatedDomainEvent(result));
                for (UserBalanceResult balanceResult : balanceResults) {
                    domainEventPublisher.publish(new UserBalanceUpdatedDomainEvent(balanceResult, balanceResult.getUpdatedAt()));
                }

                if (!resolution.isAccepted()) {
                    throw new ValidationException(resolution.getMessage());
                }
                return result;
            } catch (Exception e) {
                connection.rollback();
                throw e;
            }
        }
    }

    private BuyNowResult buyNow (
            Auction auction,
            long buyerId,
            BigDecimal buyNowPrice,
            Optional<Bid> oldWinningBid,
            AutobidResolution autobidResolution,
            LocalDateTime dbNow,
            Connection connection) throws SQLException {
        long auctionId = auction.getId();
        long sellerId = auction.getSellerId();
        BidRepository bidRepository = repositoryFactory.createBidRepository(connection);
        UserRepository userRepository = repositoryFactory.createUserRepository(connection);
        AuctionRepository auctionRepository = repositoryFactory.createAuctionRepository(connection);
        ProductImageRepository productImageRepository = repositoryFactory.createProductImageRepository(connection);
        PaymentRepository paymentRepository = repositoryFactory.createPaymentRepository(connection);
        
        autobidService.applyAutobidResolutionChanges(autobidResolution, connection);
        Set<Long> autobidUserIds = new HashSet<>();
        for (BalanceChange balanceChange : autobidResolution.getBalanceChanges()) {
            if (BalanceChangeType.RELEASE.equals(balanceChange.getType())) {
                autobidUserIds.add(balanceChange.getUserId());
            }
        }
        for (AutobidChange autobidChange : autobidResolution.getAutobidChanges()) {
            autobidUserIds.add(autobidChange.getUserId());
        }
        //1.Release Hold và Update Status 
        Long previousWinningUserId = null;
        Long previousWinningBidId = null;
        if (!oldWinningBid.isEmpty()) {
            Bid oldBid = oldWinningBid.get();
            previousWinningUserId = oldBid.getBidderId();
            previousWinningBidId = oldBid.getId();
            //nếu thằng này là autobid đang winning thì release hold của nó ở autobid rồi
            //còn nếu không thì release hold ở bid (là ở đây)
            if (!autobidUserIds.contains(oldBid.getBidderId())) {
                userRepository.releaseHoldBalance(oldBid.getBidderId(), oldBid.getBidAmount());
            }
        }
        //2. trừ tiền buyer
        userRepository.decreaseAvailableBalance(buyerId, buyNowPrice);
        
        Bid currentBid = new Bid(
            auctionId,
            buyerId,
            buyNowPrice,
            dbNow,
            BidStatus.WON,
            BidSource.USER_BID
        );
        bidRepository.save(currentBid);
        bidRepository.updateStatusesByAuctionIdExceptBid(auctionId, currentBid.getId(), BidStatus.LOST);
        ///4. End Auction
        long auctionVersion = auctionRepository.completeByBuyNow(auctionId, buyerId);
        Auction refreshedAuction = auctionRepository.findById(auctionId)
            .orElseThrow(() -> new ValidationException("Auction not found"));

        String thumbnailUrl = productImageRepository.findThumbnailUrlByProductId(refreshedAuction.getProductId()).orElse(null);
        List<UserMyBidListItemResult> affectedMyBidItems = buildAffectedMyBidItems(
            refreshedAuction,
            thumbnailUrl,
            bidRepository,
            dbNow
        );
        
        // 5. Create held payment (sau tách ra thành payment repository)
        Payment payment = new Payment(
            auctionId,
            buyerId,
            sellerId,
            currentBid.getId(),
            buyNowPrice,
            PaymentType.BUY_NOW,
            PaymentStatus.HELD
        );
        paymentRepository.save(payment);
        return ResultMapper.toBuyNowResult(
            refreshedAuction,
            currentBid,
            payment,
            auctionVersion,
            previousWinningUserId,
            previousWinningBidId,
            dbNow,
            affectedMyBidItems
        );
    }


    public BuyNowResult buyNow (BuyNowRequest request, ClientSession session) throws SQLException {
        checkSession(session);

        ValidateBidDTO.validateBuyNowRequest(request);
        try (Connection connection = connectionProvider.getConnection()) {
            connection.setAutoCommit(false);
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
                
                validateAuctionEligibility (auction, session.getUserId(), dbNow);

                if (auction.getBuyNowPrice() == null) {
                    throw new ValidationException("Buy Now is not available for this auction");
                }

                User user = userRepository.lockUserForUpdate(session.getUserId())
                    .orElseThrow(() -> new ValidationException("User not found"));
                validateBuyNowUserEligibility(user, auction.getBuyNowPrice(), currentWinningBid, winningAutobid);
                
                ///sau khi validate xong xuôi -> currentwinningbid sẽ thành oldwinningbid
                Optional<Bid> oldWinningBid = currentWinningBid;
                AutobidResolution resolution = autobidEngine.resolveAfterBuyNow(
                    auction,
                    winningAutobid.orElse(null),
                    new BuyNowCommand(auctionId, session.getUserId(), auction.getBuyNowPrice(), dbNow)
                );
                BuyNowResult buyNowResult = buyNow(
                    auction,
                    session.getUserId(),
                    auction.getBuyNowPrice(),
                    oldWinningBid,
                    resolution,
                    dbNow,
                    connection
                );
                AuctionListItemResult listItem = auctionRepository.findAuctionListItemById(auctionId)
                    .orElseThrow(() -> new ValidationException("Auction list item not found"));
                List<UserBalanceResult> balanceResults = new ArrayList<>();
                LocalDateTime balanceUpdatedAt = dbNow;
                Set<Long> autobidUserIds = new HashSet<>();
                ///tổng hợp các balance change của thằng autobid
                for (BalanceChange balanceChange : resolution.getBalanceChanges()) {
                    autobidUserIds.add(balanceChange.getUserId());
                    balanceResults.add(readUserBalanceResult(
                        userRepository,
                        balanceChange.getUserId(),
                        balanceChange.getReason(),
                        balanceUpdatedAt
                    ));
                }

                ///tạo result
                balanceResults.add(readUserBalanceResult(
                    userRepository,
                    session.getUserId(),
                    "BUY_NOW_PAYMENT",
                    balanceUpdatedAt
                ));

                Long previousWinningUserId = buyNowResult.getPreviousWinningUserId();
                if (previousWinningUserId != null
                        && previousWinningUserId != session.getUserId().longValue()
                        && !autobidUserIds.contains(previousWinningUserId)) {
                    balanceResults.add(readUserBalanceResult(
                        userRepository,
                        previousWinningUserId,
                        "OUTBID_RELEASE",
                        balanceUpdatedAt
                    ));
                }

                connection.commit();
                ///publish event
                domainEventPublisher.publish(new AuctionListItemUpdatedDomainEvent(
                    listItem,
                    AuctionListItemUpdateReason.STATUS_CHANGED,
                    dbNow
                ));
                domainEventPublisher.publish(new BuyNowDomainEvent(buyNowResult));
                for (UserBalanceResult balanceResult : balanceResults) {
                    domainEventPublisher.publish(new UserBalanceUpdatedDomainEvent(balanceResult, balanceResult.getUpdatedAt()));
                }
                return buyNowResult;
            } catch (Exception e) {
                connection.rollback();
                throw e;
            }
        }
    }

    private List<UserMyBidListItemResult> buildAffectedMyBidItems(
            Auction refreshedAuction,
            String thumbnailUrl,
            BidRepository bidRepository,
            LocalDateTime updatedAt) throws SQLException {
        List<UserMyBidListItemResult> affectedMyBidItems = new ArrayList<>();
        for (Bid bid : bidRepository.findLatestBidPerBidderByAuctionId(refreshedAuction.getId())) {
            affectedMyBidItems.add(ResultMapper.toUserMyBidListItemResult(
                refreshedAuction,
                thumbnailUrl,
                bid,
                bid.getStatus(),
                updatedAt
            ));
        }
        return affectedMyBidItems;
    }

}

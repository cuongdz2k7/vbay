package com.vbay.server.service;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import com.vbay.server.databaseManager.ConnectionProvider;
import com.vbay.server.exception.AuthenticationException;
import com.vbay.server.exception.ValidationException;
import com.vbay.server.mapper.dtomapper.ProductImageMapper;
import com.vbay.server.model.Auction;
import com.vbay.server.model.Bid;
import com.vbay.server.model.Payment;
import com.vbay.server.model.Product;
import com.vbay.server.model.ProductImage;
import com.vbay.server.model.User;
import com.vbay.server.network_connection.ClientSession;
import com.vbay.server.repository.AuctionRepository;
import com.vbay.server.repository.BidRepository;
import com.vbay.server.repository.PaymentRepository;
import com.vbay.server.repository.ProductImageRepository;
import com.vbay.server.repository.ProductRepository;
import com.vbay.server.repository.RepositoryFactory;
import com.vbay.server.repository.UserRepository;
import com.vbay.server.service.validation.ValidationUtils;
import com.vbay.shared.dto.auctionDTO.CreateAuctionRequest;
import com.vbay.shared.dto.auctionDTO.PlaceBidRequest;
import com.vbay.shared.dto.productDTO.CreateProductRequest;
import com.vbay.shared.dto.productDTO.ProductImageDTO;
import com.vbay.shared.enums.BidSource;
import com.vbay.shared.enums.PaymentType;
import com.vbay.shared.enums.shared_status.BidStatus;
import com.vbay.shared.enums.shared_status.PaymentStatus;


 /*
* Business rules:
* 1. Giá tiền phải là số dương
* 2. Nếu có reserve price thì reserve price phải lớn hơn hoặc bằng starting price
* 3. Nếu có buy now price thì buy now price phải lớn hơn hoặc bằng reserve price
* 4. Thời gian bắt đầu phải trước thời gian kết thúc
* 5. Khi tạo auction, product sẽ được tạo với status là AVAILABLE, sau đó khi auction bắt đầu thì product sẽ được update thành ACTIVE, khi auction kết thúc hoặc bị hủy thì product sẽ được update thành INACTIVE
* 6. Mỗi ảnh chỉ có 1 thumbnail, nếu có nhiều hơn 1 ảnh được đánh dấu là thumbnail thì sẽ throw validation exception
* 7. Khi tạo auction, phải có ít nhất 1 ảnh của product, nếu không có ảnh nào là thumbnail thì sẽ tự động đánh dấu ảnh đầu tiên là thumbnail
*/

public class AuctionService {
    ///tạo connection provider để sử dụng h2 in-memory database cho integration test, tránh ảnh hưởng đến database thật khi test
    private final ConnectionProvider connectionProvider;
    private final RepositoryFactory repositoryFactory;

    public AuctionService(ConnectionProvider connectionProvider, RepositoryFactory repositoryFactory) {
        this.connectionProvider = connectionProvider;
        this.repositoryFactory = repositoryFactory;
    }

    private void checkSession(ClientSession session) {
        if (session == null || !session.isAuthenticated()) {
            throw new AuthenticationException("User must be logged in to perform this action");
        }
    }


    private void validateRequiredSections(CreateAuctionRequest request) {
        ValidationUtils.requireNotNull(request.getProduct(), "Product data is required");
        ValidationUtils.requireNotNull(request, "Auction data is required");
    }

    private void validateProductFields(CreateProductRequest product) {
        ValidationUtils.requireNotBlank(product.getName(), "Product name is required");
        ValidationUtils.requireNotNull(product.getImages(), "Product images are required");
        ValidationUtils.requireNotEmpty(product.getImages(), "At least one product image is required");
        long hasThumbnail = product.getImages().stream().filter(ProductImageDTO::isThumbnail).count();
        if (hasThumbnail == 0) {
            throw new ValidationException("At least one product image must be marked as thumbnail");
        }
        if (hasThumbnail > 1) {
            throw new ValidationException("Only one product image can be marked as thumbnail");
        }
    }

    private void validateAuctionRequiredFields(CreateAuctionRequest auction) {
        ValidationUtils.requireNotBlank(auction.getTitle(), "Auction title is required");
        ValidationUtils.requireNotNull(auction.getStartingTime(), "Start time is required");
        ValidationUtils.requireNotNull(auction.getEndingTime(), "End time is required");
    }

    private void validateAuctionMoneyFields(CreateAuctionRequest auction) {
        ValidationUtils.requirePositive(auction.getStartingPrice(), "Starting price must be a positive number");
        ValidationUtils.requirePositive(auction.getMinimumBidStep(), "Minimum bid step must be a positive number");
    }

    private void validateAuctionTimeFields(CreateAuctionRequest auction) {
        ///UI check thêm startingtime phải sau current time nữa, để tránh trường hợp tạo auction xong là nó đã kết thúc luôn rồi
        //nhưng mà validate ở đây thì chỉ cần check startingtime phải trước endingtime, còn việc startingtime phải sau current time thì UI phải check trước khi gửi request lên server
        if (!auction.getStartingTime().isBefore(auction.getEndingTime())) {
            throw new ValidationException("Start time must be before end time");
        }
    }

    private void validateAuctionBusinessRules(CreateAuctionRequest auction) {
        if (auction.getBuyNowPrice() != null
                && auction.getBuyNowPrice().compareTo(BigDecimal.ZERO) > 0
                && auction.getBuyNowPrice().compareTo(auction.getStartingPrice()) < 0) {
            throw new ValidationException("Buy now price must be greater than or equal to starting price");
        }
        if (auction.getReservePrice() != null
                && auction.getReservePrice().compareTo(BigDecimal.ZERO) > 0
                && auction.getReservePrice().compareTo(auction.getStartingPrice()) < 0) {
            throw new ValidationException("Reserve price must be greater than or equal to starting price");
        }
        if (auction.getReservePrice() != null
                && auction.getReservePrice().compareTo(BigDecimal.ZERO) > 0
                && auction.getBuyNowPrice() != null
                && auction.getBuyNowPrice().compareTo(BigDecimal.ZERO) > 0
                && auction.getReservePrice().compareTo(auction.getBuyNowPrice()) < 0) {
            throw new ValidationException("Reserve price must be greater than or equal to buy now price");
        }
    }

    public void validateCreateAuctionRequest(CreateAuctionRequest request) {
        if (request == null) {
            throw new ValidationException("Request cannot be null");
        }
        validateRequiredSections(request);
        validateProductFields(request.getProduct());
        validateAuctionRequiredFields(request);
        validateAuctionMoneyFields(request);
        validateAuctionTimeFields(request);
        validateAuctionBusinessRules(request);
    }

    public long createProduct(CreateProductRequest request, ClientSession session, ProductRepository productRepository, ProductImageRepository productImageRepository) throws SQLException {
        List<ProductImage> Images = ProductImageMapper.mapToProductImages(request.getImages());
        Product product = new Product(
            request.getName(),
            request.getDescription(),
            request.getCategoryId(),
            request.getCondition()
        );
        product.setSellerId(session.getUserId());
        productRepository.save(product);
        ///getid đúng vì productRepository.save đã đồng thời set id cho product rồi
        productImageRepository.saveAll(product.getId(), Images);
        return product.getId();
    }

    public void createAuction(CreateAuctionRequest request, ClientSession session) throws SQLException {
        checkSession(session);
        validateCreateAuctionRequest(request);

        try (Connection connection = connectionProvider.getConnection()) {
            connection.setAutoCommit(false);

            try {
                ProductRepository productRepository = repositoryFactory.createProductRepository(connection);
                ProductImageRepository productImageRepository = repositoryFactory.createProductImageRepository(connection);
                AuctionRepository auctionRepository = repositoryFactory.createAuctionRepository(connection);

                // 1. tạo Product object
                long productId = createProduct(request.getProduct(), session, productRepository, productImageRepository);
                // 4. auctionRepository.save(auction)
                Auction auction = new Auction(
                    session.getUserId(),
                    productId,
                    request.getTitle(),
                    request.getDescription(),
                    request.getBuyNowPrice(),
                    request.getReservePrice(),
                    request.getMinimumBidStep(),
                    request.getStartingPrice(),
                    request.getStartingTime(),
                    request.getEndingTime()
                );
                auctionRepository.save(auction);
                connection.commit();
            } catch (SQLException | RuntimeException e) {
                connection.rollback();
                throw e;
            }
        }
    }

    void ValidateBidRequiredSection(PlaceBidRequest request) {
        ValidationUtils.requireNotNull(request.getAuctionId(), "Auction ID is required");
        ValidationUtils.requireNotNull(request.getBidAmount(), "Bid amount is required");
    }

    void ValidateBidMoneyFields(PlaceBidRequest request) {
        ValidationUtils.requirePositive(request.getBidAmount(), "Bid amount must be a positive number");
    }

    void ValidatePlaceBidRequest(PlaceBidRequest request) {
        if (request == null) {
            throw new ValidationException("Request cannot be null");
        }
        ValidateBidRequiredSection(request);
        ValidateBidMoneyFields(request);
    }

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
4. sync status của database theo currentime đấy
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
5. 


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

Nên dùng Cách B cho buy now và auction kết thúc, nó đúng nghiệp vụ “escrow/platform holding”, tiền không còn nằm trong ví buyer nữa. 
Vì hold_balance nên dùng cho bid đang có thể bị outbid. Còn buy now đã mua xong, không còn khả năng bị outbid, nên tiền nên chuyển sang chỗ payment thì đúng hơn.


*/

    private void buyNow (long auctionId, long buyerId, long sellerId, BigDecimal buyNowPrice, LocalDateTime dbNow, Connection connection) throws SQLException {
        BidRepository bidRepository = repositoryFactory.createBidRepository(connection);
        UserRepository userRepository = repositoryFactory.createUserRepository(connection);
        AuctionRepository auctionRepository = repositoryFactory.createAuctionRepository(connection);
        PaymentRepository paymentRepository = repositoryFactory.createPaymentRepository(connection);
        //1. Tìm Oldbid, Release Hold và Update Status 
        Optional<Bid> oldWinningBid = bidRepository.findWinningBidByAuctionId(auctionId);
        if (!oldWinningBid.isEmpty()) {
            Bid oldBid = oldWinningBid.get();
            userRepository.releaseHoldBalance(oldBid.getBidderId(), oldBid.getBidAmount());
            bidRepository.updateStatus(oldBid.getId(), BidStatus.OUTBID);
        }
        
        //2. Trừ tiền buyer
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
        ///4. End Auction
        auctionRepository.completeByBuyNow(auctionId, buyerId);
        
        // 5. Create held payment
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
    }

    private void placeBid (long auctionId, long bidderId, BigDecimal bidAmount, LocalDateTime dbNow, Connection connection) throws SQLException {
        BidRepository bidRepository = repositoryFactory.createBidRepository(connection);
        UserRepository userRepository = repositoryFactory.createUserRepository(connection);
        AuctionRepository auctionRepository = repositoryFactory.createAuctionRepository(connection);
        userRepository.holdBalance(bidderId, bidAmount);
        //1. Tìm Oldbid, Release Hold và Update Status 
        Optional<Bid> oldWinningBid = bidRepository.findWinningBidByAuctionId(auctionId);
        if (!oldWinningBid.isEmpty()) {
            Bid oldBid = oldWinningBid.get();
            userRepository.releaseHoldBalance(oldBid.getBidderId(), oldBid.getBidAmount());
            bidRepository.updateStatus(oldBid.getId(), BidStatus.OUTBID);
        }
        
        //2. Trừ tiền buyer
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
    }
    /*
    Nếu lock user trước rồi mới lock auction, user A có thể bị giữ ví trong lúc chờ auction lock. 
    Như vậy A chưa chắc bid được, nhưng các thao tác tiền khác của A đã bị chặn.
    Flow nên là: lock Auction trước -> validate -> lock user để thay đổi balance
    */

    private void validateUserCanBid(User user, BigDecimal bidAmount) throws SQLException {
        if (!user.isActive()) {
            throw new ValidationException("This user cannot use this action");
        }
        if (user.getAvailableBalance().compareTo(bidAmount) < 0) {
            throw new ValidationException("Insufficient available balance");
        }
    }

    private void validateAuctionCanReceiveBid(Auction auction, long userId) throws SQLException {
        if (!auction.isActive()) {
            throw new ValidationException("Auction is not active");
        }
        if (userId == auction.getSellerId()) {
            throw new ValidationException("Cannot place bid on your own auction");
        }
    }

    private boolean canBuyNow(Auction auction, BigDecimal bidAmount) {
        if (auction.getBuyNowPrice() != null && bidAmount.compareTo(auction.getBuyNowPrice()) > 0) {
            throw new ValidationException("Bid amount is higher than the Buy Now price, UI must check beforehand");
        }
        return auction.getBuyNowPrice() != null && bidAmount.compareTo(auction.getBuyNowPrice()) >= 0;
    }

    private void validateMinimumBid(Auction auction, BigDecimal bidAmount) {
        BigDecimal minimumBid = auction.getCurrentPrice().add(auction.getMinimumBidStep());
        if (bidAmount.compareTo(minimumBid) < 0) {
            throw new ValidationException("Bid amount is not sufficient");
        }
    }


    public void placeBid(PlaceBidRequest request, ClientSession session) throws SQLException {
        checkSession(session);

        ValidatePlaceBidRequest(request);
        ///Authentication/Authorization check:
        try (Connection connection = connectionProvider.getConnection()) {
            connection.setAutoCommit(false);
            try {
                AuctionRepository auctionRepository = repositoryFactory.createAuctionRepository(connection);
                UserRepository userRepository = repositoryFactory.createUserRepository(connection);

                //1. lock auction 
                Auction auction = auctionRepository.lockAuctionForUpdate(request.getAuctionId())
                    .orElseThrow(() -> new ValidationException("Auction not found"));
                long auctionId = auction.getId();
                //2. lấy thời gian hiện tại -> đặt nó là thời gian đặt bid, phải dùng dbTimezone do rule quyết định lấy time trong DB làm mốc chuẩn
                LocalDateTime dbNow = auctionRepository.getCurrentDatabaseTime();
                //3. sync status
                auctionRepository.syncStatus(auctionId, dbNow);
                //4. update lại auction có status mới, check sufficient bidamount bằng method hold và decrease available balance
                auction = auctionRepository.lockAuctionForUpdate(auctionId)
                    .orElseThrow(() -> new ValidationException("Auction not found"));
                if (!auction.isActive()) {
                    connection.commit(); 
                    ///bug: Với auction hết hạn, trước đó sync xong rồi throw thì rollback làm status quay lại ACTIVE. 
                    // Sửa để commit phần sync status trước khi throw Auction is not active.
                    throw new ValidationException("Auction is not active");
                }
                validateAuctionCanReceiveBid(auction, session.getUserId());
                boolean buyNow = canBuyNow(auction, request.getBidAmount());
                if (!buyNow) {
                    validateMinimumBid(auction, request.getBidAmount());
                }
                //5. Lock Auction validate xong xuôi rồi mới lock user
                User user = userRepository.lockUserForUpdate(session.getUserId())
                    .orElseThrow(() -> new ValidationException("User not found"));
                validateUserCanBid(user, request.getBidAmount());
               
                if (buyNow) {
                    buyNow(auctionId, session.getUserId(), auction.getSellerId(), auction.getBuyNowPrice(), dbNow, connection);
                }
                else {
                    placeBid(auctionId, session.getUserId(), request.getBidAmount(), dbNow, connection);
                }
                
                connection.commit();
            } catch (Exception e) {
                connection.rollback();
                throw e;
            } 
        }
    }


}

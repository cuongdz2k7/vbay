package com.vbay.server;

import java.util.List;

import com.vbay.server.databaseManager.ConnectionProvider;
import com.vbay.server.databaseManager.DatabaseConnection;
import com.vbay.server.network_connection.RequestDistributor;
import com.vbay.server.realtime.handler.AuctionClosedRealtimeHandler;
import com.vbay.server.realtime.handler.AuctionListItemUpdatedRealtimeHandler;
import com.vbay.server.realtime.handler.AuctionStartedRealtimeHandler;
import com.vbay.server.realtime.handler.BidUpdatedRealtimeHandler;
import com.vbay.server.realtime.handler.BuyNowRealtimeHandler;
import com.vbay.server.realtime.handler.DomainEventHandler;
import com.vbay.server.realtime.handler.UserBalanceUpdatedRealtimeHandler;
import com.vbay.server.realtime.mapper.RealtimeEventMapper;
import com.vbay.server.realtime.publisher.InMemoryDomainEventPublisher;
import com.vbay.server.realtime.subscription.InMemorySubscriptionRegistry;
import com.vbay.server.realtime.subscription.SubscriptionRegistry;
import com.vbay.server.realtime.subscription.SubscriptionService;
import com.vbay.server.realtime.subscription.validation.AuctionListRoomSubscriptionRule;
import com.vbay.server.realtime.subscription.validation.AuctionRoomSubscriptionRule;
import com.vbay.server.realtime.subscription.validation.RoomSubscriptionValidator;
import com.vbay.server.realtime.subscription.validation.UserRoomSubscriptionRule;
import com.vbay.server.realtime.transport.RealtimeBroadcaster;
import com.vbay.server.repository.JDBCrepository.JdbcRepositoryFactory;
import com.vbay.server.repository.RepositoryFactory;
import com.vbay.server.scheduler.AuctionScheduleDomainEventHandler;
import com.vbay.server.scheduler.AuctionTaskScheduler;
import com.vbay.server.security.Argon2PasswordHasher;
import com.vbay.server.security.PasswordHasher;
import com.vbay.server.service.AuctionService;
import com.vbay.server.service.AuthService;
import com.vbay.server.service.BidService;
import com.vbay.server.service.UserAccountService;
import com.vbay.server.upload.ImageStorageService;


/*
design pattern: Dependency Injection (DI), Composite Root
- AppConfig chịu trách nhiệm tạo và quản lý vòng đời của các service, repository, và
các thành phần khác của ứng dụng.
- Các service và repository sẽ nhận các dependency của chúng thông qua constructor (constructor injection).
- Điều này giúp tách rời các thành phần của ứng dụng, làm cho chúng dễ dàng để kiểm thử (unit test) và bảo trì.
*/

public class AppConfig {
    private final ConnectionProvider connectionProvider;
    private final RepositoryFactory repositoryFactory;
    private final PasswordHasher passwordHasher;
    private final AuthService authService;
    private final AuctionService auctionService;
    private final BidService bidService;
    private final UserAccountService userAccountService;
    private final RequestDistributor requestDistributor;
    private final ImageStorageService imageStorageService;
    private final SubscriptionRegistry subscriptionRegistry;
    private final RealtimeBroadcaster realtimeBroadcaster;
    private final SubscriptionService subscriptionService;
    private final RoomSubscriptionValidator subscriptionValidator;
    private final InMemoryDomainEventPublisher domainEventPublisher;
    private final List<DomainEventHandler> domainEventHandlers;
    private final RealtimeEventMapper realtimeEventMapper;
    private final AuctionTaskScheduler auctionScheduler;

    
/*
Trong Java, this(...) trong constructor nghĩa là gọi constructor khác cùng class. 
Nó phải nằm ở dòng đầu tiên.
new AppConfig()
-> gọi AppConfig(DatabaseConnection::getConnection, JdbcRepositoryFactory, Argon2PasswordHasher)
-> constructor 3 tham số khởi tạo AuthService
-> khởi tạo AuctionService
-> khởi tạo RequestDistributor
*/
    public AppConfig() {
        this(DatabaseConnection::getConnection, 
            new ImageStorageService(), 
            new Argon2PasswordHasher());
    }

    public AppConfig(
            ConnectionProvider connectionProvider,
            ImageStorageService imageStorageService,
            PasswordHasher passwordHasher) {
        this(
            connectionProvider,
            new JdbcRepositoryFactory(imageStorageService),
            passwordHasher,
            imageStorageService
        );
    }

    public AppConfig(
            ConnectionProvider connectionProvider,
            RepositoryFactory repositoryFactory,
            PasswordHasher passwordHasher) {
        this(connectionProvider, repositoryFactory, passwordHasher, new ImageStorageService());
    }

    private AppConfig(
            ConnectionProvider connectionProvider,
            RepositoryFactory repositoryFactory,
            PasswordHasher passwordHasher,
            ImageStorageService imageStorageService) {
        this.connectionProvider = connectionProvider;
        this.repositoryFactory = repositoryFactory;
        this.passwordHasher = passwordHasher;
        ///realtime
        this.subscriptionRegistry = new InMemorySubscriptionRegistry();
        this.realtimeBroadcaster = new RealtimeBroadcaster(subscriptionRegistry);
        this.realtimeEventMapper = new RealtimeEventMapper();
        
        this.domainEventPublisher = new InMemoryDomainEventPublisher();

        this.subscriptionValidator = new RoomSubscriptionValidator(List.of(
            new AuctionRoomSubscriptionRule(),
            new UserRoomSubscriptionRule(),
            new AuctionListRoomSubscriptionRule()
        ));
        this.subscriptionService = new SubscriptionService(subscriptionValidator, subscriptionRegistry);
        ///business service
        this.authService = new AuthService(connectionProvider, repositoryFactory, passwordHasher);
        this.auctionService = new AuctionService(connectionProvider, repositoryFactory, domainEventPublisher);
        this.bidService = new BidService(connectionProvider, repositoryFactory, domainEventPublisher);
        this.userAccountService = new UserAccountService(connectionProvider, repositoryFactory, domainEventPublisher);
        this.imageStorageService = imageStorageService;
        this.auctionScheduler = new AuctionTaskScheduler(auctionService);
        this.domainEventHandlers = List.of(
            new AuctionClosedRealtimeHandler(realtimeBroadcaster, realtimeEventMapper),
            new AuctionListItemUpdatedRealtimeHandler(realtimeBroadcaster, realtimeEventMapper),
            new AuctionStartedRealtimeHandler(realtimeBroadcaster, realtimeEventMapper),
            new AuctionScheduleDomainEventHandler(auctionScheduler),
            new BidUpdatedRealtimeHandler(realtimeBroadcaster, realtimeEventMapper),
            new BuyNowRealtimeHandler(realtimeBroadcaster, realtimeEventMapper),
            new UserBalanceUpdatedRealtimeHandler(realtimeBroadcaster, realtimeEventMapper)
        );
        this.domainEventPublisher.registerAll(domainEventHandlers);

        this.requestDistributor = new RequestDistributor(
            authService, 
            auctionService, 
            bidService, 
            userAccountService,
            subscriptionService, 
            imageStorageService);
    }

     public AuctionTaskScheduler getAuctionScheduler() {
        return auctionScheduler;
    }

    public RealtimeBroadcaster getRealtimeBroadcaster() {
        return realtimeBroadcaster;
    }
    
    public SubscriptionService getSubscriptionService() {
        return subscriptionService;
    }

    public RequestDistributor getRequestDistributor() {
        return requestDistributor;
    }

    public AuthService getAuthService() {
        return authService;
    }

    public AuctionService getAuctionService() {
        return auctionService;
    }
}

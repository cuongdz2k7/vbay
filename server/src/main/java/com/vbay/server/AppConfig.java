package com.vbay.server;

import java.time.Duration;
import java.util.List;

import com.vbay.server.databaseManager.ConnectionProvider;
import com.vbay.server.databaseManager.DatabaseConnection;
import com.vbay.server.network_connection.ClientConnectionRegistry;
import com.vbay.server.network_connection.RequestDistributor;
import com.vbay.server.realtime.handler.AuctionClosedRealtimeHandler;
import com.vbay.server.realtime.handler.AuctionListItemUpdatedRealtimeHandler;
import com.vbay.server.realtime.handler.AuctionStartedRealtimeHandler;
import com.vbay.server.realtime.handler.AutobidUpdatedRealtimeHandler;
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
import com.vbay.server.service.AdminAccountService;
import com.vbay.server.service.AdminUserService;
import com.vbay.server.service.AuctionService;
import com.vbay.server.service.AuthService;
import com.vbay.server.service.AdminService;
import com.vbay.server.service.UserAccountService;
import com.vbay.server.service.bid.AutobidService;
import com.vbay.server.service.bid.BidQueryService;
import com.vbay.server.service.bid.BuyNowService;
import com.vbay.server.service.bid.ManualBidService;
import com.vbay.server.service.bid.engine.AntiSnipePolicy;
import com.vbay.server.service.bid.engine.AuctionBidEngine;
import com.vbay.server.service.bid.resolution.BidResolutionApplier;
import com.vbay.server.upload.ImageStorageService;


/*
design pattern: Dependency Injection (DI), Composite Root
- AppConfig chịu trách nhiệm tạo và quản lý vòng đời của các service, repository, và
các thành phần khác của ứng dụng.
- Các service và repository sẽ nhận các dependency của chúng thông qua constructor (constructor injection).
- Điều này giúp tách rời các thành phần của ứng dụng, làm cho chúng dễ dàng để kiểm thử (unit test) và bảo trì.
*/

public class AppConfig {
    private static final AntiSnipeSettings DEFAULT_ANTI_SNIPE_SETTINGS = new AntiSnipeSettings(
        Duration.ofSeconds(30),//window
        Duration.ofMinutes(5),//extension
        5
    );

    private final ConnectionProvider connectionProvider;
    private final RepositoryFactory repositoryFactory;
    private final PasswordHasher passwordHasher;
    private final AdminAccountService adminAccountService;
    private final AdminUserService adminUserService;
    private final AuthService authService;
    private final AuctionService auctionService;
    private final ManualBidService bidService;
    private final AutobidService autobidService;
    private final BuyNowService buyNowService;
    private final BidQueryService bidQueryService;
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
    private final ClientConnectionRegistry connectionRegistry;
    private final AdminService adminService;

    
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
            new Argon2PasswordHasher(),
            DEFAULT_ANTI_SNIPE_SETTINGS);
    }

    public AppConfig(
            ConnectionProvider connectionProvider,
            ImageStorageService imageStorageService,
            PasswordHasher passwordHasher) {
        this(
            connectionProvider,
            imageStorageService,
            passwordHasher,
            DEFAULT_ANTI_SNIPE_SETTINGS
        );
    }

    public AppConfig(
            ConnectionProvider connectionProvider,
            ImageStorageService imageStorageService,
            PasswordHasher passwordHasher,
            AntiSnipeSettings antiSnipeSettings) {
        this(
            connectionProvider,
            new JdbcRepositoryFactory(imageStorageService),
            passwordHasher,
            imageStorageService,
            antiSnipeSettings
        );
    }

    public AppConfig(
            ConnectionProvider connectionProvider,
            RepositoryFactory repositoryFactory,
            PasswordHasher passwordHasher) {
        this(
            connectionProvider,
            repositoryFactory,
            passwordHasher,
            new ImageStorageService(),
            DEFAULT_ANTI_SNIPE_SETTINGS
        );
    }

    public AppConfig(
            ConnectionProvider connectionProvider,
            RepositoryFactory repositoryFactory,
            PasswordHasher passwordHasher,
            AntiSnipeSettings antiSnipeSettings) {
        this(
            connectionProvider,
            repositoryFactory,
            passwordHasher,
            new ImageStorageService(),
            antiSnipeSettings
        );
    }

    private AppConfig(
            ConnectionProvider connectionProvider,
            RepositoryFactory repositoryFactory,
            PasswordHasher passwordHasher,
            ImageStorageService imageStorageService,
            AntiSnipeSettings antiSnipeSettings) {
        this.connectionProvider = connectionProvider;
        this.repositoryFactory = repositoryFactory;
        this.passwordHasher = passwordHasher;
        ///realtime
        this.subscriptionRegistry = new InMemorySubscriptionRegistry();
        this.realtimeBroadcaster = new RealtimeBroadcaster(subscriptionRegistry);
        this.realtimeEventMapper = new RealtimeEventMapper(connectionProvider, repositoryFactory);
        
        this.domainEventPublisher = new InMemoryDomainEventPublisher();

        this.subscriptionValidator = new RoomSubscriptionValidator(List.of(
            new AuctionRoomSubscriptionRule(),
            new UserRoomSubscriptionRule(),
            new AuctionListRoomSubscriptionRule()
        ));
        this.subscriptionService = new SubscriptionService(subscriptionValidator, subscriptionRegistry);
        this.connectionRegistry = new ClientConnectionRegistry();
        this.adminService = new AdminService(connectionProvider, repositoryFactory, connectionRegistry, realtimeBroadcaster);
        ///business service
        this.adminAccountService = new AdminAccountService(connectionProvider, repositoryFactory, passwordHasher);
        this.adminUserService = new AdminUserService(connectionProvider, repositoryFactory);
        this.authService = new AuthService(connectionProvider, repositoryFactory, passwordHasher);
        this.auctionService = new AuctionService(connectionProvider, repositoryFactory, domainEventPublisher);
        AuctionBidEngine auctionBidEngine = new AuctionBidEngine(antiSnipeSettings.toPolicy());
        BidResolutionApplier bidResolutionApplier = new BidResolutionApplier(repositoryFactory);
        this.bidService = new ManualBidService(
            connectionProvider,
            repositoryFactory,
            domainEventPublisher,
            auctionBidEngine,
            bidResolutionApplier
        );
        this.autobidService = new AutobidService(
            connectionProvider,
            repositoryFactory,
            domainEventPublisher,
            auctionBidEngine,
            bidResolutionApplier
        );
        this.buyNowService = new BuyNowService(
            connectionProvider,
            repositoryFactory,
            domainEventPublisher,
            auctionBidEngine,
            bidResolutionApplier
        );
        this.bidQueryService = new BidQueryService(connectionProvider, repositoryFactory);
        this.userAccountService = new UserAccountService(connectionProvider, repositoryFactory, domainEventPublisher, realtimeBroadcaster);
        this.imageStorageService = imageStorageService;
        this.auctionScheduler = new AuctionTaskScheduler(auctionService);
        this.domainEventHandlers = List.of(
            new AuctionClosedRealtimeHandler(realtimeBroadcaster, realtimeEventMapper),
            new AuctionListItemUpdatedRealtimeHandler(realtimeBroadcaster, realtimeEventMapper),
            new AuctionStartedRealtimeHandler(realtimeBroadcaster, realtimeEventMapper),
            new AuctionScheduleDomainEventHandler(auctionScheduler),
            new BidUpdatedRealtimeHandler(realtimeBroadcaster, realtimeEventMapper),
            new BuyNowRealtimeHandler(realtimeBroadcaster, realtimeEventMapper),
            new UserBalanceUpdatedRealtimeHandler(realtimeBroadcaster, realtimeEventMapper),
            new AutobidUpdatedRealtimeHandler(realtimeBroadcaster, realtimeEventMapper)
        );
        this.domainEventPublisher.registerAll(domainEventHandlers);

        this.requestDistributor = new RequestDistributor(
            authService, 
            adminUserService,
            auctionService, 
            bidService, 
            autobidService,
            buyNowService,
            bidQueryService,
            userAccountService,
            subscriptionService, 
            imageStorageService,
            adminService,
            connectionRegistry);
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

    public AdminAccountService getAdminAccountService() {
        return adminAccountService;
    }

    public AdminUserService getAdminUserService() {
        return adminUserService;
    }

    public AuthService getAuthService() {
        return authService;
    }

    public AuctionService getAuctionService() {
        return auctionService;
    }

    public static AntiSnipeSettings defaultAntiSnipeSettings() {
        return DEFAULT_ANTI_SNIPE_SETTINGS;
    }

    public static final class AntiSnipeSettings {
        private final Duration window;
        private final Duration extension;
        private final int maxExtensions;

        public AntiSnipeSettings(Duration window, Duration extension, int maxExtensions) {
            if (window == null || window.isZero() || window.isNegative()) {
                throw new IllegalArgumentException("Anti-snipe window must be positive");
            }
            if (extension == null || extension.isZero() || extension.isNegative()) {
                throw new IllegalArgumentException("Anti-snipe extension must be positive");
            }
            if (maxExtensions < 0) {
                throw new IllegalArgumentException("Anti-snipe max extensions must not be negative");
            }
            this.window = window;
            this.extension = extension;
            this.maxExtensions = maxExtensions;
        }

        public Duration getWindow() {
            return window;
        }

        public Duration getExtension() {
            return extension;
        }

        public int getMaxExtensions() {
            return maxExtensions;
        }

        private AntiSnipePolicy toPolicy() {
            return new AntiSnipePolicy(window, extension, maxExtensions);
        }
    }
}

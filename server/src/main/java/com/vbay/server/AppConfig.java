package com.vbay.server;

import java.util.List;

import com.vbay.server.databaseManager.ConnectionProvider;
import com.vbay.server.databaseManager.DatabaseConnection;
import com.vbay.server.network_connection.RequestDistributor;
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
import com.vbay.server.security.Argon2PasswordHasher;
import com.vbay.server.security.PasswordHasher;
import com.vbay.server.service.AuctionService;
import com.vbay.server.service.AuthService;
import com.vbay.server.service.BidService;


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
    private final RequestDistributor requestDistributor;
    private final SubscriptionRegistry subscriptionRegistry;
    private final RealtimeBroadcaster realtimeBroadcaster;
    private final SubscriptionService subscriptionService;
    private final RoomSubscriptionValidator subscriptionValidator;




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
            new JdbcRepositoryFactory(), 
            new Argon2PasswordHasher());
    }

    public AppConfig(
            ConnectionProvider connectionProvider,
            RepositoryFactory repositoryFactory,
            PasswordHasher passwordHasher) {
        this.connectionProvider = connectionProvider;
        this.repositoryFactory = repositoryFactory;
        this.passwordHasher = passwordHasher;
        this.authService = new AuthService(connectionProvider, repositoryFactory, passwordHasher);
        this.auctionService = new AuctionService(connectionProvider, repositoryFactory);
        this.bidService = new BidService(connectionProvider, repositoryFactory);
        this.subscriptionRegistry = new InMemorySubscriptionRegistry();
        this.realtimeBroadcaster = new RealtimeBroadcaster(subscriptionRegistry);
        this.subscriptionValidator = new RoomSubscriptionValidator(List.of(
            new AuctionRoomSubscriptionRule(),
            new UserRoomSubscriptionRule(),
            new AuctionListRoomSubscriptionRule()
        ));
        this.subscriptionService = new SubscriptionService(subscriptionValidator, subscriptionRegistry);
        this.requestDistributor = new RequestDistributor(authService, auctionService, bidService, subscriptionService);
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

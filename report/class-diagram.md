# Thiết Kế Lớp Và Bản Đồ Module vBay

Tài liệu này mô tả cấu trúc class/module chính của vBay. Các rule chi tiết của bidding và auto-bid được tách sang `bidding-architecture.md`; file này chỉ giữ bản đồ lớp và sequence flow tổng quan.

## 1. Module Map

```mermaid
graph LR
    Client[client JavaFX] --> Shared[shared DTO / enum / protocol]
    Server[server TCP / service / repository] --> Shared
    Client <-->|TCP JSON request response event| Server
    Server --> MySQL[(MySQL)]
    Server --> Uploads[(uploads images)]
```

## 2. Server Packages

```text
com.vbay.server/
├── ServerApplication.java
├── AppConfig.java
├── databaseManager/
│   ├── DatabaseConfig.java
│   ├── DatabaseConnection.java
│   └── DatabaseInitializer.java
├── network_connection/
│   ├── ClientHandler.java
│   ├── ClientConnection.java
│   ├── ClientSession.java
│   ├── ClientConnectionRegistry.java
│   └── RequestDistributor.java
├── security/
│   ├── Argon2PasswordHasher.java
│   ├── PasswordHasher.java
│   └── PasswordPolicy.java
├── scheduler/
│   ├── AuctionTaskScheduler.java
│   └── AuctionScheduleDomainEventHandler.java
├── repository/
│   ├── RepositoryFactory.java
│   ├── [Entity]Repository.java
│   └── JDBCrepository/
├── realtime/
│   ├── domain/
│   ├── handler/
│   ├── mapper/
│   ├── publisher/
│   ├── subscription/
│   └── transport/
├── service/
│   ├── AuthService.java
│   ├── AuctionService.java
│   ├── UserAccountService.java
│   ├── AdminService.java
│   └── bid/
│       ├── ManualBidService.java
│       ├── AutobidService.java
│       ├── BuyNowService.java
│       ├── BidQueryService.java
│       ├── engine/
│       │   ├── AuctionBidEngine.java
│       │   └── AntiSnipePolicy.java
│       └── resolution/
│           ├── BidResolutionApplier.java
│           ├── AppliedBidResultMapper.java
│           └── model/
└── upload/
    ├── ImageHttpServer.java
    └── ImageStorageService.java
```

Các điểm chính:

- `ClientSession` là nguồn identity của request lifecycle sau login.
- `AuctionTaskScheduler` quản lý start/end/recovery task; `AuctionScheduleDomainEventHandler` reschedule khi nhận event có reason ảnh hưởng thời gian.
- Bidding dùng pipeline riêng: service orchestration, `AuctionBidEngine` quyết định nghiệp vụ, `BidResolutionApplier` ghi DB.
- Realtime tách domain event, mapper, handler và broadcaster để service không ghi socket trực tiếp.

## 3. Client Packages

```text
com.vbay/
├── MainApp.java
├── Launcher.java
├── network/
│   ├── SocketClient.java
│   ├── dispatcher/RealtimeEventDispatcher.java
│   ├── message/ServerMessageParser.java
│   └── image/ImageUploadClient.java
└── ui/
    ├── model/
    └── scene_ui/controller/
        ├── auth/
        ├── home/
        ├── auction/
        ├── bid/
        ├── admin/
        ├── deposit/
        └── card/
```

Client controller thường theo vòng đời:

1. Fetch snapshot từ server.
2. Render snapshot.
3. Subscribe room/event cần thiết.
4. Merge realtime event bằng `auctionVersion`, `version` hoặc `updatedAt`.
5. Dispose thì unsubscribe, stop timer và bỏ state màn.

## 4. Bid Flow Sequence

```mermaid
sequenceDiagram
    autonumber
    actor U as User
    participant C as BidController
    participant S as SocketClient
    participant H as ClientHandler
    participant D as RequestDistributor
    participant BS as ManualBidService
    participant E as AuctionBidEngine
    participant R as BidResolution
    participant A as BidResolutionApplier
    participant P as DomainEventPublisher
    participant RT as RealtimeBroadcaster

    U->>C: Place bid
    C->>S: send Request PLACE_BID
    S->>H: JSON line
    H->>D: dispatch request with ClientSession
    D->>BS: handlePlaceBid(payload, session)
    BS->>BS: check session, validate DTO, open transaction
    BS->>BS: lock auction FOR UPDATE and load current state
    BS->>E: resolveManualBid(auction, currentBid, winningAutobid, command)
    E-->>BS: BidResolution
    BS->>A: apply(resolution, dbNow, connection)
    A-->>BS: AppliedBidResolution
    BS->>BS: map result, commit transaction
    BS->>P: publish Bid/Auction/Balance domain events
    P->>RT: realtime handlers map and broadcast events
    BS-->>D: response result
    D-->>H: Respond requestId
    H-->>S: RESPONSE
    RT-->>S: EVENT updates
    S-->>C: dispatch response/event
```

## 5. AuctionChange Polymorphism

```mermaid
classDiagram
    class AuctionChange {
        <<interface>>
        +getAuctionId() long
        +apply(AuctionRepository) long
    }
    class CurrentBidAuctionChange
    class BuyNowAuctionChange
    class AntiSnipeAuctionExtensionChange
    class BidResolutionApplier

    AuctionChange <|.. CurrentBidAuctionChange
    AuctionChange <|.. BuyNowAuctionChange
    AuctionChange <|.. AntiSnipeAuctionExtensionChange
    BidResolutionApplier --> AuctionChange : calls apply()
```

`BidResolutionApplier` không cần biết từng loại auction change làm gì. Nó chỉ duyệt `resolution.getAuctionChanges()` và gọi `apply(...)`; logic update cụ thể nằm trong class implement `AuctionChange`.

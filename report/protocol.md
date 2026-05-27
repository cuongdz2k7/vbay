# Giao Thức TCP Socket & Định Dạng Dữ Liệu vBay

Hệ thống vBay sử dụng kết nối TCP Socket bền vững (Persistent Connection) làm kênh truyền thông chính giữa Client và Server. Mọi gói tin truyền tải qua mạng đều được serialize sang chuỗi JSON và phân tách bằng ký tự xuống dòng (`\n` hoặc `\r\n`), giúp đơn giản hóa luồng xử lý và đảm bảo hiệu năng tối ưu.

---

## 1. Cơ Chế Hoạt Động (Connection & Threading Flow)

1.  **Thiết Lập Kết Nối**: Client mở một Socket TCP kết nối tới Server qua cổng `3618`.
2.  **Đồng Bộ Yêu Cầu (Request-Response Correlation)**: 
    *   Mỗi khi Client gửi một yêu cầu (`Request`), nó sẽ tự động đính kèm một mã nhận diện ngẫu nhiên duy nhất (`requestId`) được sinh tự động bằng IDGenerator.
    *   Luồng gửi ở phía Client sẽ đưa một blocking queue tương ứng với `requestId` vào bản đồ quản lý luồng (`ConcurrentHashMap<String, BlockingQueue<Respond<?>>> pendingResponse`).
    *   Server xử lý bất đồng bộ và trả về `Respond` chứa chính xác `requestId` đó.
    *   Client nhận phản hồi, đối chiếu `requestId` để đánh thức luồng gửi đang bị block và phân phát dữ liệu kết quả, giải phóng bộ nhớ. Mức thời gian chờ phản hồi tối đa là **5 giây** trước khi ném ra lỗi Timeout.
3.  **Hệ Thống Sự Kiện Thời Gian Thực (Real-time Event Rooms)**:
    *   Server phát đi các sự kiện bất đồng bộ (`RealtimeEvent`) tới các Client theo mô hình Phòng Đăng Ký (Room-based Subscription Model).
    *   Client sử dụng tiến trình nền duy nhất `socket-client-listener` để lắng nghe liên tục, giải mã và gửi đến `RealtimeEventDispatcher` để cập nhật trực tiếp giao diện JavaFX thông qua `Platform.runLater()`.

---

## 2. Định Dạng Các Gói Tin (Message Formats)

Có 3 loại gói tin chính được luân chuyển, phân loại dựa trên trường `messageType`:

### A. Request (Yêu cầu từ Client)
```json
{
  "messageType": "REQUEST",
  "requestType": "LOGIN",
  "requestId": "req_8fa9c4bd-e97d",
  "payload": {
    "username": "user1",
    "password": "hashed_or_plain_password"
  }
}
```

*   `messageType`: Luôn là `"REQUEST"`.
*   `requestType`: Enum chỉ định nghiệp vụ cần thực hiện (ví dụ: `LOGIN`, `PLACE_BID`).
*   `requestId`: Mã chuỗi định danh duy nhất của yêu cầu.
*   `payload`: Đối tượng dữ liệu cụ thể tương ứng với từng loại yêu cầu (ví dụ: `LoginRequest`, `PlaceBidRequest`).

---

### B. Respond (Phản hồi từ Server)
```json
{
  "messageType": "RESPONSE",
  "requestId": "req_8fa9c4bd-e97d",
  "status": true,
  "message": "Đăng nhập thành công!",
  "data": {
    "id": 1,
    "username": "user1",
    "email": "user1@vbay.com",
    "position": "BIDDER",
    "status": "ACTIVE"
  }
}
```

*   `messageType`: Luôn là `"RESPONSE"`.
*   `requestId`: Mã định danh tương khớp với Request gửi lên.
*   `status`: `true` nếu xử lý thành công, `false` nếu thất bại.
*   `message`: Thông điệp mô tả chi tiết kết quả hoặc nguyên nhân lỗi.
*   `data`: Dữ liệu phản hồi thực tế (ví dụ: `UserDTO`, `AuctionListResponse`).

---

### C. RealtimeEvent (Sự kiện bất đồng bộ từ Server)
```json
{
  "messageType": "EVENT",
  "eventId": "evt_5cb812a3-f02d",
  "type": "BID_HISTORY_ITEM_ADDED",
  "room": {
    "type": "AUCTION",
    "id": "10"
  },
  "payload": {
    "bidId": 45,
    "auctionId": 10,
    "bidderUsername": "buyer2",
    "bidAmount": 150000.00,
    "bidTime": "2026-05-24T18:45:00",
    "status": "WINNING"
  },
  "occurredAt": "2026-05-24T18:45:01.123"
}
```

*   `messageType`: Luôn là `"EVENT"`.
*   `eventId`: Định danh duy nhất của sự kiện.
*   `type`: Loại sự kiện (ví dụ: `AUCTION_STATE_UPDATED`, `BID_HISTORY_ITEM_ADDED`, `USER_BALANCE_UPDATED`).
*   `room`: Xác định phạm vi đăng ký nhận tin (`RoomType`: `AUCTION`, `USER`, `AUCTION_LIST`).
*   `payload`: Dữ liệu sự kiện thời gian thực để cập nhật giao diện.
*   `occurredAt`: Thời điểm sự kiện phát sinh ở Server.

---

## 3. Danh Sách Request Types (`RequestType`)

Các yêu cầu được chia thành 5 nhóm chức năng nghiệp vụ chính:

| Nhóm | `RequestType` | Payload DTO đi kèm | Chức năng chi tiết |
| :--- | :--- | :--- | :--- |
| **Xác thực** | `REGISTER` | `RegisterRequest` | Đăng ký tài khoản người dùng mới |
| | `LOGIN` | `LoginRequest` | Đăng nhập hệ thống |
| | `LOGOUT` | `LogoutRequest` | Hủy kết nối & trạng thái |
| **Đấu giá** | `GET_AUCTION_LIST` | `AuctionListRequest` | Tải danh sách phiên đấu giá có bộ lọc |
| | `GET_AUCTION_DETAIL`| `AuctionDetailRequest`| Tải thông tin chi tiết phiên đấu giá |
| | `SUBSCRIBE_ROOM` | `Room` | Đăng ký theo dõi sự kiện phòng đấu giá |
| | `UNSUBSCRIBE_ROOM` | `Room` | Hủy theo dõi sự kiện phòng đấu giá |
| **Bidders** | `PLACE_BID` | `PlaceBidRequest` | Đặt giá thủ công |
| | `BUY_NOW` | `BuyNowRequest` | Mua đứt sản phẩm theo giá quy định |
| | `AUTO_BID` | `AutobidCreate` | Thiết lập đấu giá tự động (Proxy Bid) |
| | `INCREASE_AUTOBID_MAX`|`AutobidMaxBidUpdate`| Cập nhật mức giá tối đa cho auto-bid |
| **Sellers** | `CREATE_AUCTION` | `CreateAuctionRequest` | Seller đăng ký sản phẩm & phiên đấu giá |
| | `CANCEL_AUCTION` | `CancelAuctionRequest` | Hủy phiên đấu giá chưa diễn ra |
| **Admin** | `ADMIN_GET_ALL_USERS`| *Không có* | Lấy danh sách tất cả người dùng |
| | `ADMIN_LOCK_USER` | `AdminLockUserRequest` | Khóa tài khoản người dùng có thời hạn |
| | `ADMIN_APPROVE_DEPOSIT`|`AdminDepositActionRequest`| Phê duyệt yêu cầu nạp tiền vào ví |

---

## 4. Danh Sách Sự Kiện Real-time (`RealtimeEventType`)

| `RealtimeEventType` | Phạm vi Phòng (`RoomType`) | Dữ liệu phát đi (Payload) | Tác động giao diện Client |
| :--- | :--- | :--- | :--- |
| `AUCTION_STATE_UPDATED` | `AUCTION` | `AuctionStatePayload` | Cập nhật giá hiện tại, đếm ngược thời gian, người dẫn đầu và anti-snipe |
| `BID_HISTORY_ITEM_ADDED`| `AUCTION` | `BidHistoryItemPayload` | Thêm một dòng thầu mới vào bảng lịch sử thầu và vẽ lại biểu đồ giá thầu realtime |
| `WATCHER_COUNT_CHANGED` | `AUCTION` | `WatcherCountPayload` | Cập nhật nhãn số lượng người đang theo dõi phiên đấu giá trực tuyến |
| `USER_BALANCE_UPDATED` | `USER` | `UserBalanceUpdatedPayload` | Thay đổi lập tức số tiền khả dụng và tiền bị phong tỏa hiển thị ở góc màn hình |
| `AUCTION_LIST_ITEM_UPDATED`| `AUCTION_LIST`| `AuctionListItemPayload` | Cập nhật tức thời trạng thái, giá thầu hiện tại của card đấu giá ngoài màn hình chủ |
| `DEPOSIT_REQUEST_UPDATED`| `USER` | `DepositRequestPayload` | Thông báo trạng thái phê duyệt yêu cầu nạp tiền (Thành công / Từ chối) |
| `AUTOBID_UPDATED` | `AUCTION` | `AutobidUpdatedPayload` | Cập nhật cấu hình và mức giá tối đa của hệ thống Auto-bid của người chơi |

---

## 5. Ví Dụ Về Luồng Đặt Giá Thầu (Bidding Protocol Flow)

Khi một Bidder đặt giá thầu, chuỗi thông điệp truyền tải tuần tự diễn ra như sau:

```text
  Client (JavaFX)                                     Server (TCP Engine)
       |                                                      |
       | ------------ [REQUEST: PLACE_BID] -----------------> |  (1) Gửi đặt giá thầu
       |                                                      |      Thực hiện phong tỏa tiền,
       |                                                      |      xử lý giao dịch, kiểm tra anti-snipe
       |                                                      |
       | <----------- [RESPONSE: PLACE_BID] ----------------- |  (2) Trả về kết quả: Thành công
       |                                                      |
       | <=== BROADCAST EVENT: BID_HISTORY_ITEM_ADDED === [Room: AUCTION] (3) Cập nhật bảng thầu
       | <=== BROADCAST EVENT: AUCTION_STATE_UPDATED ==== [Room: AUCTION] (4) Cập nhật giá & đếm ngược
       |                                                      |
```

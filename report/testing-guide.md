# Hướng Dẫn Kiểm Thử vBay

Tài liệu này mô tả cách chạy test tự động và các kịch bản kiểm thử thủ công cho vBay.

## 1. Chiến Lược Kiểm Thử

Hệ thống đang có 3 nhóm kiểm thử:

- Unit test cho logic thuần, ví dụ `AntiSnipePolicyTest`.
- Validation/service test dùng mock hoặc dependency nhẹ, ví dụ `AuctionServiceTest`.
- Integration test dùng JDBC và H2 in-memory `MODE=MySQL`, ví dụ `AuctionServiceIntegrationTest`.

Integration test không cần MySQL local. Test tự tạo schema trong H2 memory database và giữ connection sống trong từng test case.

## 2. Test Tự Động Hiện Có

| Test | Mục tiêu |
| :--- | :--- |
| `AntiSnipePolicyTest` | Kiểm tra snipe window, extension duration và giới hạn số lần extend |
| `AuctionServiceTest` | Kiểm tra validation tạo auction: product, image, price, reserve, buy now, time |
| `AuctionServiceIntegrationTest` | Kiểm tra transaction JDBC, create auction, manual bid, buy now, auction close, auto-bid settlement |

Các test nằm trong:

```text
server/src/test/java/com/vbay/server/
```

## 3. Lệnh Maven

Chạy toàn bộ test từ thư mục gốc `vbay/`:

```bash
mvn test
```

Chạy test module server:

```bash
mvn -pl server test
```

Chạy một test class cụ thể:

```bash
mvn -pl server -Dtest=AntiSnipePolicyTest test
```

Chạy integration test:

```bash
mvn -pl server -Dtest=AuctionServiceIntegrationTest test
```

## 4. Kịch Bản Kiểm Thử Thủ Công

### Đăng Nhập, Deposit Và Admin Approval

1. Chạy server và mở ít nhất 2 client JavaFX.
2. Client user đăng ký/đăng nhập bằng tài khoản `USER`.
3. User gửi deposit request.
4. Mở client admin và đăng nhập `admin/admin`.
5. Admin approve deposit.
6. Kiểm tra user balance cập nhật realtime trên client user.

### Tạo Auction Và Place Bid

1. User A tạo auction với starting price, minimum bid step, reserve price và buy now price.
2. User B mở auction detail.
3. User B đặt bid hợp lệ.
4. Kiểm tra `available_balance` giảm, `hold_balance` tăng đúng bid amount.
5. User C đặt bid cao hơn.
6. Kiểm tra hold của User B được release và User C trở thành winner.

### Auto-bid

1. User B đăng ký auto-bid với `maxBidAmount`.
2. Kiểm tra server hold toàn bộ `maxBidAmount`, không chỉ bid thực tế.
3. User C đặt bid thấp hơn hoặc bằng max của User B.
4. Kiểm tra User C nhận reject nhưng auto-bid của User B tạo bid phản ứng.
5. Kiểm tra public UI không hiển thị `maxBidAmount`; chỉ owner thấy max của mình.

### Reserve Price

1. Tạo auction có `reserve_price`.
2. Đặt bid thấp hơn reserve và chờ auction kết thúc.
3. Kiểm tra auction kết thúc `FAILED` và hold được release.
4. Đăng ký auto-bid có max vượt reserve.
5. Kiểm tra engine có thể đẩy bid thực tế lên reserve để auction đạt reserve met.

### Buy Now

1. Tạo auction có `buy_now_price`.
2. Tạo một current winning bid hoặc winning auto-bid trước.
3. User khác bấm buy now.
4. Kiểm tra buy now thắng mọi bid/auto-bid, hold cũ được release và payment `BUY_NOW` ở trạng thái `HELD`.

### Auction Close Với Auto-bid

1. Tạo auction có winning auto-bid với `maxBidAmount > final_price`.
2. Để auction hết giờ.
3. Kiểm tra server release `maxBidAmount` trước rồi charge đúng `final_price`.
4. Kiểm tra payment `AUCTION_WIN` được tạo ở trạng thái `HELD`.

## 5. Checklist Realtime Và Security

- Client mở màn hình phải fetch snapshot trước, sau đó subscribe room/event.
- Event stale phải bị bỏ qua bằng `auctionVersion`, `version` hoặc `updatedAt`.
- Khi rời màn, controller phải unsubscribe listener/room và stop timer/timeline.
- Room `USER` chỉ subscribe được khi `targetId == session.userId`.
- Flow cá nhân phải dùng user từ `ClientSession`, không tin identity client gửi trong payload.
- Public room không được expose `maxBidAmount`.

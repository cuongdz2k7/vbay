# Thiết Kế Cơ Sở Dữ Liệu MySQL vBay

Hệ thống vBay sử dụng cơ sở dữ liệu **MySQL** làm kho lưu trữ chính. Thiết kế cơ sở dữ liệu được tối ưu hóa cho các giao dịch nhanh, có tính nhất quán cao và chống xung đột dữ liệu khi hàng ngàn người dùng đặt giá đồng thời (concurrency bidding).

---

## 1. Sơ Đồ Thực Thể Liên Kết (Entity Relationship Diagram - ERD)

Dưới đây là sơ đồ Mermaid thể hiện mối quan hệ giữa 9 bảng trong hệ thống vBay:

```mermaid
Diagram
    users ||--o{ products : "bán"
    users ||--o{ auctions : "sở hữu / thắng"
    users ||--o{ bids : "đặt thầu"
    users ||--o{ autobids : "cấu hình"
    users ||--o{ payments : "thanh toán / nhận tiền"
    users ||--o{ deposit_requests : "yêu cầu nạp"
    users ||--o{ admin_actions_log : "thực hiện / bị phạt"

    products ||--o{ product_images : "có hình ảnh"
    products ||--o| auctions : "thuộc phiên"

    auctions ||--o{ bids : "chứa thầu"
    auctions ||--o{ autobids : "áp dụng"
    auctions ||--o| payments : "tạo hóa đơn"
    auctions ||--o{ admin_actions_log : "bị xử lý"
```

---

## 2. Chi Tiết Các Bảng Dữ Liệu

Cơ sở dữ liệu vBay bao gồm 9 bảng chính, cấu trúc chi tiết như sau:

### 1. Bảng `users` (Người dùng & Ví tiền)
Lưu trữ thông tin tài khoản và số dư ví điện tử của người dùng.
*   `id` (BIGINT, PRIMARY KEY, AUTO_INCREMENT): ID người dùng.
*   `username` (VARCHAR(100), UNIQUE, NOT NULL): Tên đăng nhập duy nhất.
*   `email` (VARCHAR(255), UNIQUE, NOT NULL): Thư điện tử duy nhất.
*   `password_hash` (VARCHAR(255), NOT NULL): Mật khẩu được mã hóa bằng thuật toán băm Argon2.
*   `position` (VARCHAR(20), DEFAULT 'USER'): Vai trò tài khoản (`BIDDER`, `SELLER`, `ADMIN`).
*   `status` (VARCHAR(20), DEFAULT 'ACTIVE'): Trạng thái (`ACTIVE`, `LOCKED`).
*   `available_balance` (DECIMAL(15,2), DEFAULT 0.00): Số dư khả dụng có thể đặt thầu hoặc rút tiền.
*   `hold_balance` (DECIMAL(15,2), DEFAULT 0.00): Số dư bị phong tỏa khi đang dẫn đầu đấu giá.
*   `warning_count` (INT, DEFAULT 0): Số lần bị Admin cảnh cáo khi vi phạm luật đấu giá.
*   `lock_until` (TIMESTAMP, NULL): Mốc thời gian khóa tài khoản (nếu có).
*   `version` (BIGINT, DEFAULT 0): Cột phục vụ kiểm soát đồng thời lạc quan.

---

### 2. Bảng `products` (Sản phẩm)
Lưu trữ thông tin sản phẩm do Seller đăng ký.
*   `id` (BIGINT, PRIMARY KEY, AUTO_INCREMENT): ID sản phẩm.
*   `seller_id` (BIGINT, FOREIGN KEY -> `users.id`): ID của Seller sở hữu sản phẩm.
*   `name` (VARCHAR(255), NOT NULL): Tên sản phẩm.
*   `description` (TEXT): Mô tả chi tiết sản phẩm.
*   `category_id` (VARCHAR(100), NOT NULL): Loại danh mục sản phẩm.
*   `product_condition` (VARCHAR(50), NOT NULL): Tình trạng sản phẩm (Mới/Cũ).
*   `status` (VARCHAR(20), DEFAULT 'AVAILABLE'): Trạng thái sản phẩm (`AVAILABLE`, `SOLD`).

---

### 3. Bảng `product_images` (Hình ảnh sản phẩm)
Chứa liên kết các hình ảnh đính kèm sản phẩm.
*   `id` (BIGINT, PRIMARY KEY, AUTO_INCREMENT): ID hình ảnh.
*   `product_id` (BIGINT, FOREIGN KEY -> `products.id`): Thuộc sản phẩm nào.
*   `image_url` (VARCHAR(255), NOT NULL): Đường dẫn hình ảnh lưu trữ trên server.
*   `is_thumbnail` (BOOLEAN, DEFAULT FALSE): Đánh dấu ảnh thu nhỏ đại diện ngoài trang chủ.

---

### 4. Bảng `auctions` (Phiên đấu giá)
Bảng trung tâm lưu logic các phiên đấu giá trực tuyến.
*   `id` (BIGINT, PRIMARY KEY, AUTO_INCREMENT): ID phiên đấu giá.
*   `product_id` (BIGINT, FOREIGN KEY -> `products.id`): Sản phẩm đem ra đấu giá.
*   `seller_id` (BIGINT, FOREIGN KEY -> `users.id`): Người bán.
*   `title` (TEXT, NOT NULL): Tiêu đề phiên đấu giá.
*   `minimum_bid_step` (DECIMAL(15,2), NOT NULL): Bước giá tối thiểu bắt buộc của mỗi lượt thầu mới.
*   `starting_price` (DECIMAL(15,2), NOT NULL): Mức giá khởi điểm.
*   `current_price` (DECIMAL(15,2), NOT NULL): Giá hiện tại của phiên đấu giá (cập nhật liên tục khi có thầu mới).
*   `reserve_price` (DECIMAL(15,2)): Giá sàn của người bán (chỉ hoàn thành nếu giá hiện tại vượt giá sàn).
*   `buy_now_price` (DECIMAL(15,2)): Giá mua đứt để kết thúc phiên đấu giá ngay lập tức.
*   `final_price` (DECIMAL(15,2)): Giá chốt hạ cuối cùng khi phiên kết thúc.
*   `starting_time` (TIMESTAMP, NOT NULL): Thời điểm mở phòng đấu giá trực tiếp.
*   `ending_time` (TIMESTAMP, NOT NULL): Thời điểm kết thúc phòng đấu giá trực tiếp.
*   `status` (VARCHAR(20), DEFAULT 'SCHEDULED'): Trạng thái (`SCHEDULED`, `ACTIVE`, `ENDED`, `FAILED`).
*   `winner_user_id` (BIGINT, NULL, FOREIGN KEY -> `users.id`): Người thắng cuộc tạm thời hoặc chung cuộc.
*   `anti_snipe_extension_count` (INT, DEFAULT 0): Số lần phiên đấu giá tự động gia hạn kết thúc sát giờ.
*   `version` (BIGINT, DEFAULT 0): Phục vụ kiểm soát đồng thời lạc quan.

---

### 5. Bảng `bids` (Lịch sử đặt giá thầu)
Lưu vết tất cả các lượt đặt giá thầu của Bidders.
*   `id` (BIGINT, PRIMARY KEY, AUTO_INCREMENT): ID lượt thầu.
*   `auction_id` (BIGINT, FOREIGN KEY -> `auctions.id`): Thuộc phiên đấu giá nào.
*   `bidder_id` (BIGINT, FOREIGN KEY -> `users.id`): Người tham gia thầu.
*   `bid_amount` (DECIMAL(15,2), NOT NULL): Số tiền đặt giá.
*   `bid_time` (TIMESTAMP, NOT NULL): Thời gian gửi giá thầu lên Server.
*   `bid_source` (VARCHAR(20), DEFAULT 'USER_BID'): Nguồn thầu (`USER_BID` cho thủ công, `AUTO_BID` cho máy tự động).
*   `status` (VARCHAR(20), NOT NULL): Trạng thái thầu (`WINNING` - đang dẫn đầu, `OUTBID` - bị vượt, `LOST` - thua cuộc, `WON` - chiến thắng cuối cùng).

---

### 6. Bảng `autobids` (Đấu giá tự động - Proxy Bid)
Lưu cấu hình giới hạn giá thầu tự động của người dùng cho các phiên cụ thể.
*   `id` (BIGINT, PRIMARY KEY, AUTO_INCREMENT): ID cấu hình.
*   `auction_id` (BIGINT, FOREIGN KEY -> `auctions.id`): Phiên đấu giá áp dụng.
*   `user_id` (BIGINT, FOREIGN KEY -> `users.id`): Người cài đặt.
*   `max_bid_amount` (DECIMAL(15,2), NOT NULL): Số tiền tối đa người dùng sẵn sàng chi trả.
*   `status` (VARCHAR(20), NOT NULL): Trạng thái (`ACTIVE`, `OUTBIDED`, `CANCELLED`).

---

### 7. Bảng `payments` (Lịch sử giao dịch hóa đơn)
Lưu trữ thông tin hóa đơn thanh toán cuối cùng sau khi hoàn thành phiên đấu giá.
*   `id` (BIGINT, PRIMARY KEY, AUTO_INCREMENT): ID giao dịch.
*   `auction_id` (BIGINT, UNIQUE, FOREIGN KEY -> `auctions.id`): Gắn liền duy nhất với 1 phiên.
*   `buyer_id` (BIGINT, FOREIGN KEY -> `users.id`): Người mua.
*   `seller_id` (BIGINT, FOREIGN KEY -> `users.id`): Người nhận tiền.
*   `winning_bid_id` (BIGINT, FOREIGN KEY -> `bids.id`): Lượt thầu chiến thắng.
*   `amount` (DECIMAL(15,2), NOT NULL): Số tiền chuyển giao.
*   `type` (VARCHAR(30), NOT NULL): Loại giao dịch (`WINNING_BID_PAYMENT`, `BUY_NOW_PAYMENT`).
*   `status` (VARCHAR(30), NOT NULL): Trạng thái (`COMPLETED`, `REFUNDED`).

---

### 8. Bảng `deposit_requests` (Yêu cầu nạp tiền)
Lưu trữ danh sách yêu cầu nạp tiền vào ví điện tử chờ Admin duyệt.
*   `id` (BIGINT, PRIMARY KEY, AUTO_INCREMENT): ID yêu cầu.
*   `user_id` (BIGINT, FOREIGN KEY -> `users.id`): Người gửi yêu cầu nạp.
*   `amount` (DECIMAL(15,2), NOT NULL): Số tiền đề xuất nạp.
*   `status` (VARCHAR(20), DEFAULT 'PENDING'): Trạng thái duyệt (`PENDING`, `APPROVED`, `REJECTED`).
*   `admin_id` (BIGINT, NULL): Admin thực hiện phê duyệt yêu cầu.

---

### 9. Bảng `admin_actions_log` (Nhật ký hành động của Admin)
Ghi log kiểm toán bảo mật toàn bộ hành động nhạy cảm của Admin.
*   `id` (BIGINT, PRIMARY KEY, AUTO_INCREMENT): ID log.
*   `admin_id` (BIGINT, FOREIGN KEY -> `users.id`): Admin thực thi.
*   `target_user_id` (BIGINT, NULL): Người dùng chịu tác động.
*   `target_auction_id` (BIGINT, NULL): Phiên đấu giá chịu tác động.
*   `action_type` (VARCHAR(30), NOT NULL): Loại hành động (`BAN_USER`, `LOCK_USER`, `WARN_USER`, `STOP_AUCTION`).
*   `reason` (TEXT): Lý do thực hiện xử lý.

---

## 3. Cơ Chế Kiểm Soát Đồng Thời & An Toàn Giao Dịch

Đấu giá trực tuyến là hệ thống nhạy cảm với dữ liệu, hàng loạt lượt đặt thầu gửi lên cùng một mili-giây có thể gây ra hiện tượng Over-bidding (quá thầu), double-spending (tiêu tiền hai lần). vBay triển khai chiến lược kiểm soát hai lớp cực kỳ chặt chẽ:

### A. Pessimistic Locking (Khóa bi quan - `FOR UPDATE`)
Khi một luồng xử lý đặt giá thầu (`PLACE_BID`) hoặc mua ngay (`BUY_NOW`) bắt đầu, Server sẽ thực hiện truy vấn khóa độc quyền hàng cơ sở dữ liệu:
```sql
SELECT ... FROM auctions WHERE id = ? FOR UPDATE
```
Lệnh này khóa phiên đấu giá cụ thể trong giao dịch MySQL hiện tại. Bất kỳ luồng đặt giá thầu song song nào khác gọi đến phiên đấu giá này đều bắt buộc phải xếp hàng chờ cho đến khi giao dịch trước đó thực hiện thành công `COMMIT` hoặc `ROLLBACK`.

### B. Optimistic Locking (Khóa lạc quan - `version`)
Hệ thống sử dụng trường `version` trong các bảng quan trọng như `users`, `products` và `auctions`. Mỗi khi một thay đổi thành công được thực thi, trường `version` tự động tăng thêm 1:
```sql
UPDATE auctions
SET current_price = ?, winner_user_id = ?, version = version + 1
WHERE id = ? AND version = ?
```
Nếu có một luồng khác can thiệp sửa đổi dữ liệu trước đó làm thay đổi phiên bản (`version`), câu lệnh `UPDATE` sẽ trả về `0` dòng bị ảnh hưởng, Server lập tức phát hiện xung đột dữ liệu và thực hiện hủy bỏ giao dịch an toàn (Rollback), tránh lỗi sai lệch trạng thái dữ liệu.

---

## 4. Tối Ưu Hóa Hiệu Năng (Database Indexes)

Hệ thống tự động xây dựng các chỉ mục (Indexes) trên bảng `auctions` để đảm bảo tốc độ tải trang chủ và tìm kiếm nhanh chóng khi số lượng bản ghi lên tới hàng triệu dòng:

1.  `idx_auctions_status` trên cột `status`: Gia tốc tìm kiếm nhanh các phòng đấu giá đang trực tiếp (`ACTIVE`) hoặc đã lên lịch (`SCHEDULED`).
2.  `idx_auctions_seller_id` trên cột `seller_id`: Hỗ trợ Seller xem nhanh danh sách sản phẩm mình đang bán.
3.  `idx_auctions_status_starting_time` và `idx_auctions_status_ending_time` (Chỉ mục phức hợp): Hỗ trợ trình lập lịch `AuctionTaskScheduler` nhanh chóng quét các phiên đến giờ mở/đóng cửa để cập nhật trạng thái tự động theo thời gian thực mà không làm nghẽn toàn bộ bảng cơ sở dữ liệu.

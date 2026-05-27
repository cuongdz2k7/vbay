# Hướng Dẫn Kiểm Thử & Kiểm Định Hệ Thống vBay

Đảm bảo độ tin cậy của hệ thống, đặc biệt là các nghiệp vụ ví tiền và đặt thầu, là cực kỳ quan trọng. Tài liệu này cung cấp hướng dẫn chi tiết về chiến lược kiểm thử tự động (Automated Tests) và quy trình kiểm thử thủ công (Manual Verification) trên hệ thống vBay.

---

## 1. Chiến Lược Kiểm Thử (Testing Strategy)

vBay áp dụng mô hình kim tự tháp kiểm thử 3 lớp:

1.  **Unit Tests (Kiểm thử đơn vị)**: Tập trung kiểm định các cấu trúc logic thuần túy không phụ thuộc vào hạ tầng bên ngoài, điển hình là thuật toán chống bắn tỉa phút chót (`AntiSnipePolicyTest`) hay các trình chuyển đổi DTO.
2.  **Integration Tests (Kiểm thử tích hợp)**: Kiểm thử sự kết hợp giữa các tầng dịch vụ (Service) và cơ sở dữ liệu JDBC thực tế (`AuctionServiceIntegrationTest`), đảm bảo các lệnh Rollback giao dịch hoạt động chuẩn xác khi có lỗi.
3.  **Manual Verification (Kiểm thử thủ công theo kịch bản)**: Chạy song song Server và nhiều Client đồ họa JavaFX để thực hiện giả lập tương tác người dùng thực tế và kiểm tra tính trực quan của giao diện thời gian thực.

---

## 2. Kiểm Thử Tự Động (Automated Testing)

Dự án sử dụng bộ đôi thư viện tiêu chuẩn **JUnit 5** và **Mockito** để viết các kịch bản kiểm thử tự động.

### A. Danh sách các tệp kiểm thử tự động
*   `AntiSnipePolicyTest.java` (`server/src/test/java/com/vbay/server/service/bid/engine/AntiSnipePolicyTest.java`): Kiểm tra xem thời gian kết thúc của phiên đấu giá có tự động gia hạn thêm 30 giây khi có lượt đặt giá thầu sát giờ kết thúc (snipe window) hay không.
*   `AuctionServiceTest.java` (`server/src/test/java/com/vbay/server/service/AuctionServiceTest.java`): Sử dụng Mockito để giả lập tầng Repositories, kiểm tra các điều kiện logic nghiệp vụ khi tạo hoặc cập nhật trạng thái phiên đấu giá.
*   `AuctionServiceIntegrationTest.java` (`server/src/test/java/com/vbay/server/service/AuctionServiceIntegrationTest.java`): Kết nối trực tiếp cơ sở dữ liệu MySQL chạy local để thực thi luồng tạo, cập nhật trạng thái đấu giá trong các giao dịch thực tế.

---

### B. Hướng dẫn chạy kiểm thử tự động với Maven

Mở terminal tại thư mục gốc của dự án (`d:\vbay`) và thực hiện các lệnh sau:

#### Chạy toàn bộ test suite trong dự án:
```bash
mvn test
```

#### Chỉ chạy các kiểm thử của module `server`:
```bash
mvn test -pl server
```

#### Chạy duy nhất một lớp kiểm thử cụ thể:
```bash
mvn test -pl server -Dtest=AntiSnipePolicyTest
```

---

## 3. Quy Trình Kiểm Thử Thủ Công (Manual Verification Scenarios)

Để thực hiện demo và kiểm tra trực quan các tính năng của hệ thống, hãy thực hiện kịch bản kiểm thử sau:

### Kịch Bản 1: Đăng ký, Đăng nhập & Nạp tiền ví điện tử
1.  Khởi chạy Server và 2 Client JavaFX song song.
2.  Trên **Client 1**: Chọn **Đăng ký** tài khoản người dùng thông thường (`Position.USER`, ví dụ: `seller1`), sau đó tiến hành **Đăng nhập**.
3.  Trên **Client 2**: Chọn **Đăng ký** tài khoản người dùng thông thường (`Position.USER`, ví dụ: `buyer1`), tiến hành **Đăng nhập**.
4.  Trên **Client 2**: Vào mục **Ví tiền** (Wallet), gửi yêu cầu nạp thêm `10,000,000đ` (Deposit Balance).
5.  Khởi chạy **Client 3** và đăng nhập tài khoản Admin mặc định (`admin`/`admin`).
6.  Trên giao diện **Admin Dashboard**: Chọn mục **Yêu cầu nạp tiền**, bấm **Phê duyệt** (Approve) yêu cầu của `buyer1`.
7.  Quan sát màn hình **Client 2 (buyer1)**: Số dư ví khả dụng hiển thị lập tức cập nhật lên `10,000,000đ` theo thời gian thực mà không cần tải lại trang.

---

### Kịch Bản 2: Đăng bán sản phẩm & Đặt giá thầu thời gian thực
1.  Trên **Client 1 (seller1)**: Chọn mục **Tạo phiên đấu giá** (Create Auction). Điền thông tin sản phẩm, chọn ảnh đại diện, cấu hình giá khởi điểm `100,000đ`, bước giá tối thiểu `20,000đ` và chọn thời gian bắt đầu (chọn thời gian hiện tại để kích hoạt ngay). Bấm **Tạo mới**.
2.  Quan sát **Client 2 (buyer1)**: Phiên đấu giá mới lập tức xuất hiện ngoài trang chủ (Home) dạng một chiếc Card đẹp mắt với trạng thái trực tiếp (`ACTIVE`).
3.  Trên **Client 2 (buyer1)**: Bấm vào xem chi tiết phiên đấu giá để tham gia phòng.
4.  Quan sát nhãn đếm ngược thời gian và biểu đồ giá thầu trống.
5.  Bấm nút **Đặt Giá** (Place Bid) với số tiền tối thiểu `120,000đ`.
    *   Hệ thống hiển thị trạng thái của bạn là **DẪN ĐẦU** (WINNING).
    *   Ví tiền khả dụng của bạn lập tức bị trừ `120,000đ` và đưa vào mục tiền phong tỏa (Hold Balance).
    *   Biểu đồ thầu xuất hiện một điểm dữ liệu đầu tiên thể hiện mức thầu `120,000đ` của `buyer1`.

---

### Kịch Bản 3: Đấu giá tự động (Auto-bid Proxy Engine)
1.  Khởi chạy **Client 4**, đăng ký và đăng nhập tài khoản người dùng (`Position.USER`, ví dụ: `buyer2`), nạp tiền ví `5,000,000đ` qua Admin.
2.  Trên **Client 4 (buyer2)**: Vào cùng phòng đấu giá sản phẩm của `seller1` ở trên (với vai trò bidder).
3.  Cấu hình chức năng **Auto-Bid**: Điền mức giới hạn tối đa mong muốn là `500,000đ` và bấm **Kích hoạt** (Activate Auto-bid).
4.  Quan sát tức thì:
    *   Server tự động tính toán đặt giá thay cho `buyer2` mức thầu tiếp theo: `140,000đ` (Giá thầu cao nhất của buyer1 `120,000` + bước giá `20,000`).
    *   `buyer2` trở thành người dẫn đầu. Tiền phong tỏa của `buyer2` là `140,000đ`.
    *   Ví của `buyer1` lập tức được giải phóng `120,000đ` phong tỏa trở về số dư khả dụng. Trạng thái thầu của `buyer1` đổi thành **BỊ VƯỢT** (OUTBID).
5.  Trên **Client 2 (buyer1)**: Cố gắng đặt thầu thủ công lên mức `200,000đ`.
    *   Ngay khi bấm nút, Server xử lý và nhận thấy mức thầu này thấp hơn giới hạn tối đa `500,000đ` của `buyer2`.
    *   Server tự động đặt thầu phản hồi thay cho `buyer2` mức `220,000đ` trong chớp mắt.
    *   `buyer2` tiếp tục dẫn đầu, `buyer1` tiếp tục nhận trạng thái bị vượt mặt. Luồng xử lý đảm bảo hoàn toàn thread-safe.

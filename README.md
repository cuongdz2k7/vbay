# Tài Liệu Báo Cáo Chi Tiết Dự Án Đấu Giá vBay

Chào mừng bạn đến với bộ tài liệu báo cáo phân tích chi tiết của dự án **vBay** (Hệ thống đấu giá trực tuyến thời gian thực).

Bộ tài liệu này cung cấp cái nhìn toàn diện từ phạm vi thiết kế, cấu trúc mã nguồn, thiết kế cơ sở dữ liệu, đặc tả giao thức mạng TCP Socket cho tới cơ chế xử lý đồng thời (concurrency) và hướng dẫn vận hành chi tiết. Tài liệu được cấu trúc chặt chẽ dựa trên mô hình tiêu chuẩn của các hệ thống phần mềm chuyên nghiệp.

---

## Danh Mục Các Báo Cáo Chi Tiết

Vui lòng bấm chọn các báo cáo chi tiết dưới đây để theo dõi:

### 1. [Tổng Quan Hệ Thống & Phạm Vi Dự Án](report/overview.md)
*   Mô tả phạm vi ứng dụng, các vai trò người chơi (`BIDDER`, `SELLER`, `ADMIN`).
*   Giới thiệu các cấu trúc module chính (`client`, `server`, `shared`).
*   Kiến trúc kết nối tổng quát Client - Server.

### 2. [Hướng Dẫn Cài Đặt & Khởi Chạy](report/setup.md)
*   Yêu cầu cấu hình phần cứng, phiên bản JDK, Maven và MySQL Server.
*   Cấu hình thông số kết nối cơ sở dữ liệu.
*   Cách khởi chạy máy chủ TCP Server và mở nhiều Client JavaFX chạy song song.

### 3. [Đặc Tả Giao Thức TCP Socket & JSON Protocol](report/protocol.md)
*   Cấu trúc luồng truyền nhận gói dữ liệu mạng an toàn (Request - Response - Event).
*   Danh sách chi tiết 62 mã lệnh nghiệp vụ (`RequestType`).
*   Danh sách 12 sự kiện thời gian thực (`RealtimeEventType`) và ví dụ mẫu JSON.

### 4. [Thiết Kế Cơ Sở Dữ Liệu MySQL](report/database-erd.md)
*   Sơ đồ thực thể liên kết (Mermaid-based Entity Relationship Diagram).
*   Chi tiết kiểu dữ liệu, ràng buộc khóa ngoại (Foreign Keys) của 9 bảng cơ sở dữ liệu.
*   Chi tiết kỹ thuật đồng thời **Locking độc quyền (Pessimistic - `FOR UPDATE`)** và **Locking phiên bản (Optimistic - `version`)**.

### 5. [Bản Đồ Thiết Kế Lớp & Sơ Đồ Sequence](report/class-diagram.md)
*   Tổ chức cấu trúc các gói thư mục (packages) rõ ràng trong 3 module Maven.
*   Sơ đồ Sequence mô tả chi tiết luồng cộng tác giữa Client Controller, SocketClient, ClientHandler, Service và Repositories khi thực hiện thao tác đấu giá.

### 6. [Kiến Trúc Concurrency & Real-time Engines](report/realtime-architecture.md)
*   Chi tiết cơ chế Phòng Đăng Ký Đấu Giá (`SubscriptionService`) để broadcast dữ liệu chọn lọc.
*   Giải pháp thiết kế **Auction Task Scheduler** đa luồng tự phục hồi sau sự cố sập server.
*   Nguyên lý hoạt động và tính an toàn của động cơ đấu giá tự động **Auto-Bid Proxy Bidding Engine**.

### 7. [Hướng Dẫn Kiểm Thử & Kiểm Định](report/testing-guide.md)
*   Giới thiệu các kịch bản kiểm thử tự động đơn vị và tích hợp sử dụng JUnit 5 và Mockito.
*   Quy trình kiểm thử thủ công theo kịch bản (Manual Scenarios) chi tiết để chạy demo hệ thống.

### 8. [Hướng Dẫn Sử Dụng Ứng Dụng](report/user-manual.md)
*   Sách hướng dẫn sử dụng giao diện chi tiết cho người đấu giá (Bidder Manual).
*   Sách hướng dẫn quản lý sản phẩm, phòng đấu giá dành cho người bán (Seller Manual).
*   Bảng điều khiển duyệt tiền, khóa tài khoản, can thiệp khẩn cấp dành cho quản trị viên (Admin Manual).

### 9. [Xử Lý Sự Cố & Hướng Dẫn Đóng Gói JAR](report/troubleshooting-release.md)
*   Các lỗi xung đột cổng mạng, lỗi kết nối DB, lỗi ghi tệp hình ảnh sản phẩm và cách khắc phục nhanh.
*   Quy trình đóng gói Maven thành các tệp tin thực thi độc lập gộp dependency (`with-dependencies.jar`).

# vBay - Hệ Thống Đấu Giá Trực Tuyến Thời Gian Thực

vBay là một nền tảng đấu giá trực tuyến được xây dựng theo kiến trúc Client-Server mạnh mẽ, sử dụng giao tiếp TCP Socket thời gian thực trên nền tảng Java và cơ sở dữ liệu MySQL. Hệ thống được thiết kế để giải quyết các bài toán phức tạp về concurrency (đồng thời), quản lý phòng đấu giá trực tiếp, đặt giá thầu tự động (auto-bid) và cơ chế khóa số dư ví điện tử an toàn.

Dự án tuân thủ cấu trúc đa module (Multi-Module Maven Project), tách biệt rõ ràng giữa các tầng dữ liệu dùng chung (Shared Protocol & DTOs), logic nghiệp vụ ở phía Server và giao diện tương tác người dùng JavaFX hiện đại ở phía Client.

---

## 1. Phạm Vi Hệ Thống

vBay tập trung cung cấp giải pháp đấu giá trực tuyến toàn diện bao gồm:

*   **Quản Lý Tài Khoản & Phân Quyền**: Hỗ trợ 3 vai trò người dùng chính:
    *   `BIDDER` (Người mua/Người đấu giá): Xem danh sách phòng đấu giá, nạp tiền vào ví, tham gia đấu giá trực tiếp, đặt giá thủ công hoặc cấu hình đấu giá tự động (auto-bid).
    *   `SELLER` (Người bán): Quản lý sản phẩm, tạo phòng đấu giá mới, theo dõi trạng thái các phiên đấu giá của mình.
    *   `ADMIN` (Quản trị viên): Kiểm duyệt và phê duyệt yêu cầu nạp tiền, quản lý trạng thái tài khoản người dùng (khóa/mở khóa), hủy/kết thúc phiên đấu giá khi có tranh chấp hoặc vi phạm, kiểm tra lịch sử thao tác qua log hệ thống.
*   **Ví Điện Tử Nội Bộ (Internal Digital Wallet)**:
    *   Quản lý hai loại số dư: Số dư khả dụng (`available_balance`) và Số dư bị phong tỏa (`hold_balance`).
    *   Khi một Bidder dẫn đầu phiên đấu giá, hệ thống sẽ phong tỏa số tiền tương ứng (`hold_balance`) để đảm bảo khả năng thanh toán. Khi bị người khác trả giá cao hơn hoặc phiên đấu giá bị hủy, số tiền phong tỏa ngay lập tức được hoàn lại vào số dư khả dụng.
*   **Phòng Đấu Giá Trực Tiếp (Real-time Live Bidding Room)**:
    *   Kết nối thời gian thực qua giao thức TCP Socket.
    *   Hiển thị biểu đồ lịch sử thay đổi giá thầu trực quan.
    *   Cập nhật số lượng người đang theo dõi phòng đấu giá (Watcher Count).
    *   Cơ chế chống bắn tỉa phút chót (**Anti-Snipe Extension**): Tự động kéo dài thời gian kết thúc phiên đấu giá thêm một khoảng thời gian quy định (ví dụ: 30 giây) nếu có lượt đặt giá hợp lệ được gửi lên sát thời điểm kết thúc.
*   **Đấu Giá Tự Động (Auto-Bid / Proxy Bidding Engine)**:
    *   Người dùng cấu hình mức giá tối đa (`max_bid_amount`) mong muốn chi trả cho sản phẩm.
    *   Hệ thống sẽ tự động đặt giá thay cho người dùng với bước giá tối thiểu ngay khi có người khác trả giá cao hơn, đảm bảo người dùng luôn dẫn đầu cho đến khi vượt quá giới hạn đã cấu hình.

---

## 2. Kiến Trúc Dự Án (Module Structure)

Dự án được cấu trúc thành **3 module Maven chính**:

```text
vbay/
├── shared/   - Định nghĩa DTOs, Enums, Protocol và Utility dùng chung
├── server/   - Socket Server, DAO/Repository MySQL, Scheduler, Real-time Rooms
└── client/   - JavaFX Desktop Application, Socket Client, FXML & Controllers
```

| Module | Vai Trò & Chức Năng |
| :--- | :--- |
| **`shared`** | Chứa các lớp định nghĩa cấu trúc yêu cầu (`Request`) và phản hồi (`Respond`), các lớp truyền dữ liệu (`DTO`), các hằng số phân loại trạng thái (`Enums`) và các tiện ích dùng chung (như định dạng JSON `JsonUtils` hay log `LoggingUtils`). Đảm bảo tính đồng bộ hoàn hảo về mặt dữ liệu và giao thức giao tiếp giữa Client và Server. |
| **`server`** | Trái tim logic của hệ thống. Nhận kết nối TCP từ các Client, điều phối yêu cầu qua `RequestDistributor`, quản lý các kết nối qua `ClientConnectionRegistry`, tương tác trực tiếp với MySQL Database thông qua mô hình JDBC DAO/Repository, điều khiển luồng đấu giá bằng `AuctionTaskScheduler` và duy trì các phòng đấu giá thời gian thực thông qua `SubscriptionService`. Ngoài ra, server cũng tích hợp một HTTP server thu nhỏ (`ImageHttpServer`) để quản lý việc tải ảnh sản phẩm lên. |
| **`client`** | Ứng dụng Desktop chạy JavaFX phong phú và mượt mà. Kết nối tới Server thông qua một tiến trình nền duy nhất `SocketClient` để duy trì kết nối TCP bền bỉ. Client sử dụng mô hình MVC (Model-View-Controller) kết hợp các tệp FXML định hình giao diện hiện đại và các CSS tùy chỉnh cao cấp nhằm mang lại trải nghiệm người dùng tối ưu nhất. |

---

## 3. Các Tính Năng Cốt Lõi Chi Tiết

```mermaid
graph TD
    subgraph Client Module (JavaFX App)
        UI[Giao diện JavaFX Controllers] <--> SC[SocketClient Singleton]
    end

    subgraph Server Module (TCP Engine)
        CH[ClientHandler Thread] <--> RD[RequestDistributor]
        RD <--> Services[Service Layer: Auth, Auction, Bid, Wallet]
        Services <--> Repos[JDBC Repositories]
        
        Scheduler[AuctionTaskScheduler] -.->|Theo dõi thời gian| Services
        SubService[SubscriptionService] -.->|Broadcast sự kiện| CH
    end

    subgraph Database Layer
        MySQL[(MySQL Server: Port 1638)]
    end

    SC <-->|TCP Socket: Port 3618| CH
    Repos <-->|JDBC Connections| MySQL
```

### A. Quy Trình Xác Thực & Phân Quyền
Hệ thống sử dụng cơ chế băm mật khẩu bảo mật `BCrypt`. Khi đăng nhập thành công, một thực thể `UserDTO` chứa thông tin vai trò (`position`: `BIDDER`, `SELLER`, `ADMIN`) và trạng thái tài khoản (`status`: `ACTIVE`, `LOCKED`) được duy trì để phân quyền giao diện và chức năng phía Client.

### B. Luồng Đấu Giá An Toàn & Giao Dịch Tài Chính (Wallet Flow)
1.  **Đặt Giá (Place Bid)**: Khi Bidder đặt một mức giá thầu mới:
    *   Hệ thống kiểm tra số dư khả dụng (`available_balance`) của Bidder xem có lớn hơn hoặc bằng mức giá thầu đề xuất cộng với bước giá không.
    *   Thực hiện phong tỏa số tiền tương ứng bằng cách trừ `available_balance` và cộng vào `hold_balance` của người đặt giá mới.
    *   Hoàn trả số tiền phong tỏa của người dẫn đầu cũ (nếu có) từ `hold_balance` về lại `available_balance` của họ.
    *   Tất cả các hành động này được thực hiện trong một giao dịch cơ sở dữ liệu (Database Transaction) cô lập để đảm bảo tính nguyên tử (Atomicity).
2.  **Đấu Giá Thành Công (Auction Won)**: Khi phiên đấu giá kết thúc chính thức:
    *   Người chiến thắng được xác định. Số tiền bị phong tỏa (`hold_balance`) của họ được chuyển thành hóa đơn thực tế: trừ khỏi `hold_balance` và chuyển thẳng vào `available_balance` của Seller (sau khi trừ phí hệ thống nếu có).
    *   Sản phẩm được đánh dấu trạng thái đã bán, và tạo bản ghi lưu vết thanh toán trong bảng `payments`.
3.  **Mua Ngay (Buy Now)**: Đối với các phiên đấu giá có cấu hình giá mua đứt (`buy_now_price`):
    *   Người dùng có thể chọn mua ngay để lập tức kết thúc phiên đấu giá.
    *   Số tiền bằng mức giá mua đứt được chuyển trực tiếp từ số dư khả dụng của người mua sang người bán, không trải qua quy trình đặt thầu và phong tỏa.
    *   Tất cả các lệnh đặt thầu trước đó của các bidders khác lập tức được giải tỏa tiền phong tỏa và cập nhật trạng thái thầu thành thất bại (`LOST`).

---

## 4. Công Nghệ & Môi Trường Hoạt Động

*   **Ngôn ngữ lập trình**: Java 25 (tận dụng các tính năng mới nhất về hiệu năng và quản lý bộ nhớ).
*   **Quản lý mã nguồn & Build**: Maven 3.9+.
*   **Môi trường đồ họa phía Client**: JavaFX 25.0.2 kết hợp FXML và các thư viện hỗ trợ giao diện hiện đại.
*   **Hệ quản trị cơ sở dữ liệu**: MySQL 9.4.0 (phục vụ lưu trữ bền vững cấu trúc cao, hỗ trợ tối ưu hóa giao dịch).
*   **Giao thức truyền thông**: TCP Socket truyền thống kết hợp thư viện Gson để chuyển đổi các đối tượng Java thành định dạng chuỗi JSON nhanh chóng qua đường truyền mạng.
*   **Ghi log hệ thống**: SLF4J + Java Util Logging cung cấp thông tin gỡ lỗi đầy đủ cả ở phía client lẫn server.

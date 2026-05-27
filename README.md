# vBay - Online Auction System (Hệ Thống Đấu Giá Trực Tuyến Thời Gian Thực)

**vBay** là dự án bài tập lớn môn Lập trình nâng cao mô phỏng một nền tảng đấu giá trực tuyến thời gian thực cao cấp theo kiến trúc Desktop Client-Server. Hệ thống cho phép nhiều Client JavaFX giao tiếp đồng thời tới một TCP Server để thực hiện đăng nhập, đăng ký, nạp tiền ví điện tử, quản lý phiên đấu giá, đặt thầu tự động (Auto-bid), mua ngay (Buy Now), chống bắn tỉa phút chót (Anti-snipe) và cập nhật biến động giá realtime trực quan trên biểu đồ.

Dự án được xây dựng với mục tiêu phục vụ bài tập lớn và chạy thử nghiệm (demo) học phần dưới mô hình đa module (Multi-Module Maven), đảm bảo giải quyết tối ưu các bài toán về xử lý bất đồng bộ, lập lịch thời gian thực và tranh chấp đồng thời (concurrency) ở tầng cơ sở dữ liệu.

Repository: [https://github.com/cuongdz2k7/vbay](https://github.com/cuongdz2k7/vbay)

---

## 1. Phạm Vi Hệ Thống

vBay tập trung mô phỏng trọn vẹn luồng nghiệp vụ đấu giá nội bộ chạy cục bộ/phòng Lab:

*   **Phân Quyền Vai Trò**: Hỗ trợ 3 vai trò người dùng chính:
    *   `BIDDER` (Người mua/đấu giá): Xem danh sách phòng đấu giá theo thẻ, nạp tiền vào ví, tham gia phòng live bidding room thời gian thực, đặt giá thủ công hoặc cấu hình cơ chế tự động đấu thầu (Auto-Bid).
    *   `SELLER` (Người bán): Quản lý kho sản phẩm cá nhân, đăng tải hình ảnh sản phẩm (qua HTTP server tích hợp), cấu hình luật chơi (giá sàn bảo vệ, bước giá tối thiểu, giá mua đứt) và lên lịch/quản lý phiên đấu giá.
    *   `ADMIN` (Quản trị viên): Giám sát hệ thống, kiểm duyệt/phê duyệt hoặc từ chối các yêu cầu nạp tiền ví điện tử của Bidder, quản lý tài khoản (cảnh cáo, kick, khóa tài khoản vi phạm), dừng/hủy khẩn cấp phiên đấu giá có tranh chấp.
*   **Ví Điện Tử Thông Minh (Wallet & Lock Engine)**:
    *   Phân tách rõ ràng giữa **Số dư khả dụng** (`available_balance`) và **Số dư bị phong tỏa** (`hold_balance`).
    *   Tự động khóa số tiền bằng mức trả giá cao nhất của Bidder hiện tại khi họ đang dẫn đầu phòng đấu giá để đảm bảo khả năng thanh toán.
    *   Ngay khi có người khác trả giá cao hơn (`outbid`) hoặc phiên đấu giá bị hủy, hệ thống lập tức hoàn lại tiền phong tỏa về số dư khả dụng của Bidder cũ trong một giao dịch cơ sở dữ liệu nguyên tử (atomic transaction).
*   **Live Bidding Room & Realtime Chart**:
    *   Kết nối thời gian thực bền bỉ qua giao thức TCP Socket.
    *   Vẽ biểu đồ đường biểu diễn sự thay đổi giá thầu realtime trên giao diện client.
    *   Hiển thị số lượng người theo dõi phòng đấu giá trực tiếp (Watcher Count).
    *   Tích hợp tính năng chống bắn tỉa phút chót (**Anti-Snipe Extension**): Tự động gia hạn thời gian kết thúc phiên đấu giá thêm một khoảng cố định (ví dụ: 30 giây) nếu phát hiện lượt đặt giá hợp lệ được gửi lên sát thời điểm kết thúc.

---

## 2. Thành Viên Nhóm & Phân Công Nhiệm Vụ

Nhóm gồm **4 thành viên** với sự phân công chuyên môn hóa rõ ràng, tập trung đóng góp lớn của hai thành viên chính (Hùng và Cường) để hoàn thành các cấu trúc lõi phức tạp của dự án:

| Thành Viên | Phụ Trách Nghiệp Vụ & Kỹ Thuật |
| :--- | :--- |
| **Nguyễn Duy Hùng**  | • Thiết kế & tối ưu giao diện Login/Register UI, Admin Dashboard và Home Dashboard.<br>• Xây dựng nền tảng kết nối mạng phía Client (`SocketClient` Singleton).<br>• Cấu hình hệ thống CI/CD thông qua GitHub Actions (`manual-ci.yml`, `release-binaries.yml`).<br>• Đồng thiết kế và xử lý Concurrency đa luồng (Multi-threading) phía giao diện Client. |
| **Đinh Tiến Cường**  | • Phát triển toàn bộ logic nghiệp vụ (Core Services) ở phía Backend Server.<br>• Thiết kế cấu trúc lưu trữ cơ sở dữ liệu MySQL JDBC Repositories/DAOs.<br>• Nghiên cứu và hiện thực hóa các động cơ đấu giá cốt lõi: Đặt thầu tự động (**Auto-Bidding Engine**), chống bắn tỉa (**Anti-Snipping Engine**).<br>• Xây dựng giải pháp đồng thời nâng cao: Database Transactions (`Pessimistic Locking` / `Optimistic Locking`) và Lock đồng thời theo `auctionId`. |
| **Nguyễn Kim Bách** | • Định nghĩa lớp dữ liệu DTO, Enum và Protocol giao tiếp dùng chung trong module `shared`.<br>• Thiết kế bố cục giao diện hiển thị danh sách phiên đấu giá dưới dạng Card.<br>• Hiện thực bộ lọc tìm kiếm sản phẩm theo từ khóa và danh mục (Category filter).<br>• Tích hợp tính năng upload và xử lý hình ảnh sản phẩm phía client.Deploy và demo prodyction |
| **Lê Đức Phong** | • Tích hợp và cấu hình AtlantaFX Theme mang lại giao diện Sleek Dark Mode cao cấp.<br>• Lập trình hiển thị biểu đồ biến động giá thầu thời gian thực (Real-time chart) trên JavaFX.<br>• Hiện thực bộ thông báo nổi (Toast Notification) xếp chồng thông minh kèm progress bar chạy mượt mà.<br>• Hỗ trợ tích hợp kiểm thử tự động (Unit Test/Integration Test) phía Client. |

---

## 3. Công Nghệ & Môi Trường Hoạt Động

Dự án ứng dụng các công nghệ hiện đại bậc nhất trong hệ sinh thái Java Desktop nhằm đạt được hiệu năng tối đa và giao diện người dùng premium:

| Nhóm Công Nghệ | Công Nghệ Áp Dụng |
| :--- | :--- |
| **Ngôn Ngữ** | Java 25 (Tận dụng các tính năng mới nhất về hiệu năng và quản lý bộ nhớ) |
| **Build System** | Maven Multi-Module Project |
| **Client UI** | JavaFX 25.0.2, FXML, CSS tùy biến cao cấp, AtlantaFX Theme (Sleek Dark Mode) |
| **Server Engine** | TCP Socket đa luồng, newline-delimited JSON protocol |
| **Database** | MySQL 9.4.0 (phục vụ lưu trữ giao dịch bền vững, hỗ trợ Row-level locking) |
| **Security** | Argon2 password hashing (`argon2-jvm`) bảo mật mật khẩu tối đa |
| **Serialization** | Jackson Databind & Gson (chuyển đổi DTO và Network Payload nhanh chóng) |
| **Logging** | SLF4J + Java Util Logging |
| **Testing** | JUnit 5, Mockito |
| **CI/CD** | GitHub Actions (Tự động biên dịch, chạy test, đóng gói và phát hành bản build JAR) |

### Yêu Cầu Cài Đặt
*   **Java Development Kit**: JDK 25
*   **Apache Maven**: Phiên bản 3.9+
*   **Hệ Quản Trị CSDL**: MySQL Server 9.4.0 (hoặc tương thích 8.0+)
*   **Hệ Điều Hành**: Windows, macOS hoặc Linux desktop (yêu cầu môi trường đồ họa hiển thị để chạy Client JavaFX).

---

## 4. Cấu Trúc Module Dự Án

Thư mục dự án được tổ chức chặt chẽ theo mô hình đa module Maven:

```text
vbay/
├── shared/     - Định nghĩa DTOs, Enums, Protocol giao tiếp và Helper (JsonUtils, LoggingUtils...) dùng chung.
├── server/     - Trái tim xử lý: Socket Server, JDBC Repositories MySQL, Services Layer, Scheduler, HTTP Image Server.
├── client/     - Giao diện người dùng JavaFX, FXML views, SocketClient duy trì kết nối nền, UI Controllers.
├── report/     - Bộ tài liệu kỹ thuật chi tiết, báo cáo phân tích kiến trúc và hướng dẫn demo.
└── .github/    - Cấu hình CI/CD workflows chạy tự động trên GitHub Actions.
```

---

## 5. Đóng Gói (Build) Và Vị Trí File JAR

Di chuyển vào thư mục gốc của dự án (`d:\vbay`) và chạy lệnh đóng gói của Maven:

```bash
mvn clean package
```

Sau khi quá trình biên dịch và kiểm thử thành công, plugin `maven-assembly-plugin` sẽ đóng gói gộp mọi dependencies cần thiết tạo ra các tệp tin executable JAR độc lập nằm tại các đường dẫn sau:

| Artifact | Vị Trí File JAR | Lệnh Chạy Thực Thi |
| :--- | :--- | :--- |
| **Server Executable JAR** | `server/target/server.jar` | `java -jar server/target/server.jar` |
| **Client Executable JAR** | `client/target/client.jar` | `java -jar client/target/client.jar` |

Nếu muốn chạy biên dịch đi kèm kiểm tra tiêu chuẩn mã nguồn (verify/checkstyle) giống như quy trình CI/CD:
```bash
mvn clean verify
```

---

## 6. Khởi Chạy Hệ Thống

## Yêu Cầu Môi Trường Cực Tiểu
Trước khi khởi chạy ứng dụng, máy tính của bạn cần cài đặt sẵn:
1.  **Java Runtime Environment (JRE) hoặc JDK 25**: Đảm bảo lệnh `java -version` hiển thị phiên bản 25 khi gõ trong Terminal/Command Prompt.
2.  **Hệ Quản Trị CSDL MySQL Server**: Phiên bản 8.0+ hoặc 9.4.0 đang hoạt động.
3.  **Hệ Điều Hành**: Windows, macOS hoặc Linux desktop có môi trường đồ họa để hiển thị giao diện Client.

---

## Cấu Trúc Thư Mục Triển Khai (Deployment Folder Layout)

```text
release/
├── server.jar         - File máy chủ TCP Socket Server & MySQL Service
├── client.jar         - File giao diện người dùng Desktop Client
├── run-server.bat     - Script click-đúp chạy nhanh Server (Windows)
├── run-client.bat     - Script click-đúp chạy nhanh Client (Windows)
├── uploads/           - Thư mục chứa hình ảnh sản phẩm (BẮT BUỘC PHẢI TẠO TRƯỚC!)
├── music/             - Thư mục chứa các file âm thanh thông báo của hệ thống
└── log/               - Thư mục tự động sinh ra để ghi nhật ký hệ thống (.log)
```

---

## Cấu Hình Cơ Sở Dữ Liệu MySQL
Cần đảm bảo:
*   Dịch vụ MySQL Server đang chạy trên máy tính.
*   Thông số tài khoản kết nối mặc định được biên dịch cứng trong file JAR:
    *   **Host**: `localhost`
    *   **Port**: `1638`
    *   **Username**: `root`
    *   **Password**: `1234`

*   *Lưu ý*: Nếu mật khẩu MySQL trên máy của bạn khác `1234`, hãy cấu hình lại mật khẩu root của MySQL cục bộ thành `1234` hoặc đổi cổng kết nối của MySQL về trùng khớp để Server kết nối thành công.

---

## Quy Trình Khởi Chạy Từng Bước (Step-by-Step)

### Bước 1: Khởi chạy Máy chủ (vBay Server)
1.  Mở thư mục `release/`.
2.  **Khởi chạy nhanh**: Click đúp vào file **`server`**.
    *(Hoặc chạy thủ công bằng cách mở Terminal tại thư mục `release/` và gõ lệnh: `java -jar server.jar`)*.
3.  **Xác nhận trạng thái**: Màn hình Console hiển thị dòng log thông báo thành công:
    *   `Initializing database...` -> `Database initialized successfully.`
    *   `Server listening on port 3618` (Cổng kết nối TCP Socket).

---

### Bước 2: Khởi chạy Giao diện người dùng (vBay Client)
1.  **Khởi chạy nhanh**: Click đúp vào file **`client`**.
    *(Hoặc chạy thủ công bằng cách mở Terminal tại thư mục `release/` và gõ lệnh: `java --enable-native-access=ALL-UNNAMED -jar client.jar`)*.
    > [!TIP]
    > Việc sử dụng cờ `--enable-native-access=ALL-UNNAMED` (được tích hợp sẵn trong file `.bat`) giúp cấp quyền native đồ họa cho JavaFX, làm **sạch 100% các dòng log cảnh báo đỏ** trên console.
2.  **Mở nhiều Client song song**: Để demo luồng đấu giá cạnh tranh giữa nhiều người dùng, bạn chỉ cần click đúp tiếp vào file **`run-client.bat`** để mở thêm cửa sổ Client 2, Client 3 chạy song song độc lập.

---

## Xử Lý Sự Cố Thường Gặp (Troubleshooting)

*   **Lỗi `Address already in use: bind` khi chạy Server**:
    *   *Nguyên nhân*: Cổng `3618` đã bị chiếm dụng bởi tiến trình chạy ngầm từ trước.
    *   *Khắc phục*: Tắt hết các tab console Server đang chạy ngầm hoặc khởi động lại máy tính để giải phóng cổng mạng.
*   **Lỗi Server không kết nối được Database**:
    *   *Nguyên nhân*: Sai mật khẩu MySQL hoặc MySQL Server chưa khởi động.
    *   *Khắc phục*: Kiểm tra dịch vụ MySQL trong Task Manager (Windows) xem đã chạy chưa, và đảm bảo mật khẩu root là `1234`.
*   **Ảnh sản phẩm bị trắng (Không tải được ảnh)**:
    *   *Nguyên nhân*: Bạn chưa tạo thư mục `uploads/` cùng cấp với file `server.jar`.
    *   *Khắc phục*: Tạo folder trống tên là `uploads` nằm cùng cấp với tệp `server.jar` 


## 7. Cơ Chế Tài Khoản Demo & Đăng Ký Người Dùng

Để tạo tính an toàn và minh bạch tuyệt đối, hệ thống vBay sở hữu cơ chế cung cấp tài khoản demo và đăng ký linh hoạt:

1.  **Tài Khoản Admin Mặc Định**:
    *   **Username**: `admin`
    *   **Password**: `admin`
    *   *Lưu ý*: Tài khoản này không được cài đặt tĩnh bằng SQL thô trong DB. Thay vào đó, khi bạn đăng nhập lần đầu tiên với thông tin `admin` / `admin`, hệ thống Server sẽ tự động kiểm tra và khởi tạo (auto-provision) một tài khoản Admin thực tế được băm mật khẩu bảo mật bằng Argon2 lưu trữ vào DB MySQL.
2.  **Tài Khoản Seller & Bidder**:
    *   Người dùng có thể tự do đăng ký tài khoản mới trực tiếp ngay trên giao diện Client (bằng cách bấm vào mục **Register** trên màn hình đăng nhập).
    *   Sau khi đăng ký thành công, tài khoản sẽ được khởi tạo với số dư ví mặc định là `0`. 
    *   **Luồng Ví Thực Tế**: Bidder tự gửi yêu cầu nạp tiền mong muốn (ví dụ: 10,000,000đ) từ màn hình ví của Client. Sau đó, đăng nhập bằng tài khoản `admin` trên một Client khác để vào **Admin Dashboard** duyệt yêu cầu nạp tiền. Số tiền duyệt nạp lập tức được cộng vào `available_balance` của Bidder để tham gia đấu giá!

---

## 8. Kịch Bản Video Demo Đề Xuất

Dưới đây là kịch bản chạy thử nghiệm (demo) thực tế giúp mô phỏng đầy đủ mọi tính năng cốt lõi của hệ thống vBay:

1.  **Chuẩn bị**: Khởi chạy MySQL Server -> Chạy TCP Server -> Mở song song 3 cửa sổ Client.
2.  **Đăng ký & Nạp tiền**:
    *   Cửa sổ Client 1 & 2: Thực hiện đăng ký 2 tài khoản Bidder (ví dụ: `bidder01`, `bidder02`). Đăng ký tiếp 1 tài khoản Seller (`seller01`).
    *   Đăng nhập tài khoản `bidder01` và `bidder02`. Vào màn hình ví điện tử, gửi yêu cầu nạp lần lượt `20,000,000đ` và `30,000,000đ`.
    *   Cửa sổ Client 3: Đăng nhập tài khoản Admin mặc định (`admin` / `admin`). Vào **Pending Deposits**, bấm duyệt cả hai yêu cầu nạp tiền.
    *   Kiểm tra số dư khả dụng trên giao diện của `bidder01` và `bidder02` lập tức cập nhật số tiền tương ứng thời gian thực.
3.  **Tạo phiên đấu giá**:
    *   Cửa sổ Client 1: Đăng nhập tài khoản Seller `seller01`. Vào tab **Create Auction**, đăng tải một ảnh sản phẩm từ máy tính, đặt tên sản phẩm, thiết lập Giá khởi điểm = `1,000,000đ`, Bước giá = `200,000đ`, Giá mua ngay = `15,000,000đ`, thời gian bắt đầu lập tức và thời gian kết thúc sau 5 phút. Bấm đăng bán để kích hoạt phiên đấu giá.
4.  **Cạnh tranh giá thầu trực tiếp**:
    *   Cửa sổ Client 1 (Bidder `bidder01`) và Client 2 (Bidder `bidder02`): Cùng vào danh sách phòng đấu giá, chọn tham gia phòng đấu giá sản phẩm vừa tạo.
    *   `bidder01` đặt giá thầu tối thiểu `1,200,000đ`. Trạng thái đổi thành **WINNING** (Màu xanh), số dư ví bị khóa phong tỏa `1,200,000đ`.
    *   `bidder02` đặt giá cao hơn `1,400,000đ`. Trạng thái `bidder01` lập tức đổi thành **OUTBID** (Màu đỏ), số tiền phong tỏa được giải phóng về ví khả dụng ngay lập tức. Trạng thái `bidder02` chuyển thành **WINNING**.
    *   Xem biến động giá cập nhật realtime trên biểu đồ đường và bảng lịch sử thầu của cả hai giao diện.
5.  **Demo Đấu thầu tự động (Auto-bid)**:
    *   `bidder01` chuyển sang tab Auto-Bid, cấu hình mức giá tối đa là `5,000,000đ` và kích hoạt.
    *   `bidder02` tiếp tục đặt thầu thủ công các mức giá cao hơn. Trình tự động Auto-Bid của `bidder01` lập tức phản hồi tự động đưa ra giá thầu mới cao hơn bước giá tối thiểu để giật lại trạng thái dẫn đầu mà không cần tác động thủ công.
6.  **Demo Mua ngay (Buy Now)**:
    *   `bidder02` quyết định chốt nhanh sản phẩm bằng cách bấm nút **Buy Now** (Mua ngay) giá `15,000,000đ`.
    *   Hệ thống xử lý trừ trực tiếp số dư khả dụng của `bidder02`, chuyển khoản sang cho `seller01`, đóng phiên đấu giá lập tức và ghi hóa đơn thanh toán thành công. Auto-bid của `bidder01` tự động dừng hoạt động và tiền phong tỏa (nếu có) được hoàn trả trọn vẹn.

---

## 9. Bộ Tài Liệu Kỹ Thuật Chi Tiết (References)

Hệ thống được tài liệu hóa vô cùng công phu và đầy đủ tại thư mục [report/](file:///d:/vbay/report). Vui lòng nhấn vào các liên kết dưới đây để khám phá chi tiết cấu trúc kỹ thuật:

1.  **[Tổng Quan Hệ Thống & Phạm Vi](report/overview.md)**: Định nghĩa nghiệp vụ, phân quyền vai trò (`BIDDER`, `SELLER`, `ADMIN`), cơ chế xử lý ví và sơ đồ kiến trúc module tổng quát.
2.  **[Hướng Dẫn Cài Đặt & Khởi Chạy](report/setup.md)**: Chi tiết cấu hình môi trường JDK 25, MySQL 9.4.0, cấu hình DatabaseConfig tĩnh và quy trình khởi chạy từng tiến trình.
3.  **[Đặc Tả Giao Thức TCP Socket & JSON Protocol](report/protocol.md)**: Đặc tả cấu trúc gói tin mạng (Request - Respond - Event), liệt kê danh sách 62 mã nghiệp vụ `RequestType` và 12 sự kiện realtime `RealtimeEventType` kèm gói JSON mẫu.
4.  **[Thiết Kế Cơ Sở Dữ Liệu MySQL](report/database-erd.md)**: Sơ đồ ERD trực quan (dựng trên Mermaid), chi tiết kiểu dữ liệu ràng buộc 9 bảng, cơ chế khóa hàng loạt **Pessimistic Locking (`FOR UPDATE`)** và **Optimistic Locking (`version`)**.
5.  **[Bản Đồ Thiết Kế Lớp & Sơ Đồ Sequence](report/class-diagram.md)**: Bản đồ phân gói (packages) trong 3 module Maven và sơ đồ sequence chi tiết luồng cộng tác giữa Client Controller, SocketClient, Server ClientHandler, Services và Repositories khi đấu giá.
6.  **[Kiến Trúc Concurrency & Real-time Engines](report/realtime-architecture.md)**: Thiết kế của Phòng Đăng Ký Đấu Giá (`SubscriptionService`), cơ chế lập lịch tự phục hồi **Auction Task Scheduler** và động cơ đấu giá tự động **Auto-Bid Proxy Bidding Engine**.
7.  **[Hướng Dẫn Kiểm Thử & Kiểm Định](report/testing-guide.md)**: Hướng dẫn viết và chạy các bộ Unit Test, Integration Test tự động với JUnit 5/Mockito và kịch bản kiểm thử thủ công từng bước.
8.  **[Hướng Dẫn Sử Dụng Ứng Dụng](report/user-manual.md)**: Cẩm nang hướng dẫn vận hành chi tiết dành cho cả 3 nhóm người dùng tương tác trực tiếp với giao diện desktop JavaFX.
9.  **[Xử Lý Sự Cố & Hướng Dẫn Đóng Gói JAR](report/troubleshooting-release.md)**: Tổng hợp các lỗi kết nối DB, lỗi xung đột cổng, cảnh báo System.load native-access trên JDK 25 và quy trình đóng gói fat JAR.
10. **[Kiến trúc & nền tảng cơ sở bidding](report/troubleshooting-release.md)**: Mô tả kiến trúc nghiệp vụ của bidding trong vBay, là phần có nhiều nhánh quyết định nhất (vừa thay đổi giá auction, vừa thay đổi ví, bid history, auto-bid, payment và realtime event trong cùng một transaction).

---

## 10. Trạng Thái Nộp Bài Dự Án

*   **Nhánh nộp bài cuối cùng**: `main`
*   **Hạn cuối commit nộp bài**: `23:59, ngày 31/05/2026`
*   **Video demo hệ thống**: [Liên kết video demo (Drive/Youtube)](https://youtube.com/placeholder-vbay-demo) *(Vui lòng chèn link video demo của bạn vào đây trước khi nộp bài)*

### Task Board Tiến Độ
- [x] Chuẩn hóa README theo checklist nộp bài và cập nhật thông tin thành viên chính xác.
- [x] Bổ sung cấu hình build executable JAR shading dependencies cho server/client.
- [ ] Tích hợp và lưu trữ báo cáo PDF tổng hợp trong repo.
- [x] Upload video demo cuối cùng và chèn liên kết chính thức vào mục video demo ở README.

# Hướng Dẫn Xử Lý Sự Cố & Đóng Gói Phát Hành vBay

Tài liệu này tổng hợp các lỗi vận hành phổ biến nhất và giải pháp xử lý nhanh, đồng thời hướng dẫn chi tiết quy trình đóng gói dự án thành các tệp tin thực thi độc lập (FAT Executable JARs) phục vụ quá trình chuyển giao và vận hành production.

---

## 1. Hướng Dẫn Xử Lý Sự Cố Thường Gặp (Troubleshooting)

Trong quá trình khởi chạy hoặc lập trình phát triển hệ thống, bạn có thể gặp phải một số vấn đề liên quan đến cổng kết nối mạng hoặc cơ sở dữ liệu. Dưới đây là các sự cố tiêu biểu và cách khắc phục:

### A. Lỗi xung đột cổng mạng (Port Conflict Error)
*   **Triệu chứng**: Khi chạy Server, console hiển thị lỗi `java.net.BindException: Address already in use: bind`.
*   **Nguyên nhân**: Cổng **`3618`** (cổng TCP Socket của vBay Server) hoặc **`1639`** (cổng HTTP Image Server) đã bị chiếm dụng bởi một tiến trình khác chạy ngầm trên máy tính của bạn.
*   **Giải pháp**:
    *   *Phương án 1*: Tìm và tắt tiến trình đang chiếm cổng. Trên Windows, mở PowerShell với quyền Administrator và chạy lệnh:
        ```powershell
        Stop-Process -Id (Get-NetTCPConnection -LocalPort 3618).OwningProcess -Force
        ```
    *   *Phương án 2*: Thay đổi cổng Server trong lớp `ServerApplication.java` (đổi hằng số `PORT`) và đổi cấu hình kết nối tương ứng phía Client tại lớp `SocketClient.java`.

---

### B. Lỗi kết nối cơ sở dữ liệu thất bại (Database Connection Failure)
*   **Triệu chứng**: Khi chạy Server, hệ thống ném ra lỗi `SQLException: Cannot create database...` hoặc `Communication link failure`.
*   **Nguyên nhân**:
    1.  Dịch vụ MySQL Server chưa khởi động.
    2.  MySQL Server đang chạy trên cổng khác mặc định trong code (cổng mặc định trong code vBay là `1638`, trong khi mặc định của cài đặt MySQL thông thường là `3306`).
    3.  Tên đăng nhập hoặc mật khẩu MySQL không khớp (mặc định trong code là user `root`, mật khẩu `1234`).
*   **Giải pháp**:
    *   Truy cập tệp `DatabaseConfig.java` (`server/src/main/java/com/vbay/server/databaseManager/DatabaseConfig.java`).
    *   Cập nhật lại cổng kết nối (`PORT`) thành cổng thực tế (ví dụ: `3306`), và đổi lại mật khẩu (`PASSWORD`) cho khớp với cơ sở dữ liệu MySQL cục bộ của bạn.
    *   Đảm bảo dịch vụ MySQL đang hoạt động bình thường.

---

### C. Lỗi không tải được hình ảnh sản phẩm (Image Upload/Write Failure)
*   **Triệu chứng**: Khi Seller tạo phiên đấu giá và đính kèm ảnh sản phẩm, hệ thống báo lỗi không thể lưu ảnh hoặc Client không hiển thị được ảnh sản phẩm (ảnh trắng).
*   **Nguyên nhân**: 
    1.  Thư mục lưu trữ hình ảnh tải lên (`uploads/`) ở thư mục gốc dự án chưa được tạo hoặc không có quyền ghi tệp (Write Permissions).
    2.  Máy chủ HTTP tải ảnh (`ImageHttpServer`) khởi chạy thất bại trên cổng `1639`.
*   **Giải pháp**:
    *   Đảm bảo thư mục `uploads/` có quyền đọc và ghi dữ liệu đầy đủ.
    *   Kiểm tra log của Server để chắc chắn `ImageHttpServer` đã khởi chạy thành công trên cổng `1639`.

---

## 2. Quy Trình Đóng Gói & Phát Hành Dự Án (Release Guide)

Dự án vBay hỗ trợ đóng gói tự động thành các tệp tin JAR chứa đầy đủ tài nguyên và dependencies đi kèm (FAT Executable JARs) thông qua plugin chuyên dụng của Maven.

### Bước 1: Dọn dẹp và đóng gói dự án
Mở terminal tại thư mục gốc của dự án (`d:\vbay`) và thực thi lệnh:

```bash
mvn clean package -DskipTests
```
*(Bỏ qua tham số `-DskipTests` nếu bạn muốn chạy kiểm thử tự động trước khi đóng gói).*

---

### Bước 2: Vị trí các tệp thực thi sau khi đóng gói
Sau khi Maven chạy hoàn tất thông báo `BUILD SUCCESS`, bạn có thể tìm thấy các tệp tin JAR độc lập tại các thư mục sau:

| Module | Tệp tin thực thi (Artifact JAR) | Vai trò vận hành |
| :--- | :--- | :--- |
| **Server** | `server/target/server-1.0-SNAPSHOT-jar-with-dependencies.jar` | Chạy máy chủ TCP Socket và MySQL Service |
| **Client** | `client/target/client-1.0-SNAPSHOT-jar-with-dependencies.jar` | Ứng dụng Desktop JavaFX dành cho người dùng |

Các tệp tin JAR này đã được "shade" (đóng gói gộp) toàn bộ các thư viện bổ trợ cần thiết (như Gson, Jackson, băm mật khẩu Argon2, MySQL Driver...) nên có thể chạy độc lập trên bất kỳ máy tính nào có cài đặt môi trường chạy Java (JRE) mà không cần cấu hình thêm thư viện mạng.

---

### Bước 3: Khởi chạy tệp tin phân phối thực tế
Để phân phối và khởi chạy ứng dụng ngoài môi trường phát triển code, bạn chỉ cần sao chép các tệp JAR trên và chạy lệnh:

#### Chạy Server:
```bash
java -jar server-1.0-SNAPSHOT-jar-with-dependencies.jar
```

#### Chạy Client:
```bash
java -jar client-1.0-SNAPSHOT-jar-with-dependencies.jar
```
*(Đảm bảo đã tạo thư mục `uploads` nằm cùng cấp với tệp JAR của Server để lưu trữ hình ảnh đăng tải của người dùng).*

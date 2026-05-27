# Hướng Dẫn Cài Đặt Và Khởi Chạy Hệ Thống vBay

Tài liệu này hướng dẫn chi tiết cách chuẩn bị môi trường, cấu hình cơ sở dữ liệu MySQL, biên dịch mã nguồn và khởi chạy Server cùng các Client đấu giá của vBay.

---

## 1. Yêu Cầu Hệ Thống & Môi Trường

Trước khi bắt đầu, hãy đảm bảo máy tính của bạn đã cài đặt các công cụ sau:

1.  **Java Development Kit (JDK)**: Phiên bản **Java 25** (tối thiểu Java 21) để biên dịch dự án.
2.  **Apache Maven**: Phiên bản **3.9+** để quản lý dependency và đóng gói build.
3.  **Hệ Quản Trị Cơ Sở Dữ Liệu**: **MySQL Server 9.4.0** (hoặc các phiên bản 8.0+ tương thích).
4.  **Hệ Điều Hành**: Môi trường có hỗ trợ đồ họa (Windows, macOS hoặc Linux Desktop) để chạy JavaFX Client.

---

## 2. Cấu Hình Cơ Sở Dữ Liệu MySQL

Mặc định, vBay tự động khởi tạo cơ sở dữ liệu và bảng dữ liệu khi Server khởi chạy lần đầu nhờ lớp `DatabaseInitializer`. Bạn chỉ cần chuẩn bị một thực thể MySQL Server đang hoạt động và cập nhật cấu hình kết nối.

### Bước 1: Khởi động MySQL Server
Đảm bảo dịch vụ MySQL đang chạy trên máy của bạn.

### Bước 2: Kiểm tra cấu hình kết nối
Các cấu hình mặc định được khai báo tĩnh tại lớp `DatabaseConfig.java` (`server/src/main/java/com/vbay/server/databaseManager/DatabaseConfig.java`):

*   **Host**: `localhost`
*   **Port**: `1638`
*   **Database Name**: `vbay`
*   **Username**: `root`
*   **Password**: `1234`

> [!TIP]
> Bạn có thể thay đổi các giá trị này trong tệp tin `DatabaseConfig.java` để khớp với môi trường MySQL cục bộ của bạn (ví dụ: đổi cổng về mặc định `3306` hoặc thay đổi mật khẩu root).

### Bước 3: Tự động chạy Schema
Khi bạn chạy Server lần đầu, nó sẽ tự động:
1.  Kết nối với MySQL Server và chạy lệnh `CREATE DATABASE IF NOT EXISTS vbay CHARACTER SET utf8mb4 ...`
2.  Đọc tệp tin SQL khởi tạo schema tại `/data_init.sql` (nằm trong thư mục `server/src/main/resources/data_init.sql`) để tạo các bảng.
3.  Tự động tạo các chỉ mục cơ sở dữ liệu (Indexes) để tăng tốc truy vấn tìm kiếm phiên đấu giá.

---

## 3. Biên Dịch Dự Án Với Maven

Mở terminal tại thư mục gốc của dự án (`d:\vbay`) và thực hiện lệnh biên dịch toàn bộ các module:

```bash
mvn clean package
```

Lệnh này sẽ tải toàn bộ thư viện cần thiết (Jackson, Gson, MySQL Connector, JavaFX, BCrypt, Ikonli, JUnit...), chạy các bài kiểm thử tự động, và đóng gói dự án thành các tệp tin `.jar` trong thư mục `target` của mỗi module.

---

## 4. Hướng Dẫn Khởi Chạy Hệ Thống

Hệ thống vBay hoạt động theo mô hình Client - Server, vì vậy bạn cần chạy Server trước, sau đó mới khởi chạy một hoặc nhiều Client để kết nối và thực hiện demo đấu giá.

### A. Khởi Chạy Server
Có hai cách để chạy Server:

#### Cách 1: Chạy trực tiếp qua Maven (Khuyên dùng khi phát triển)
Di chuyển vào module `server` và sử dụng plugin `exec-maven-plugin`:

```bash
cd server
mvn exec:java -Dexec.mainClass="com.vbay.server.ServerApplication"
```

#### Cách 2: Chạy tệp tin JAR đã đóng gói
Sau khi chạy lệnh `mvn package`, di chuyển vào thư mục `server/target` và chạy tệp JAR:

```bash
java -jar server-1.0-SNAPSHOT-jar-with-dependencies.jar
```

Khi Server khởi động thành công, terminal sẽ hiển thị dòng log:
`Server listening on port 3618` và `Image HTTP Server started on port 1639` (hoặc cổng tương ứng).

---

### B. Khởi Chạy Client (JavaFX App)
Để chạy giao diện đồ họa người dùng:

#### Cách 1: Chạy qua Maven
Mở một terminal mới tại thư mục gốc của dự án, di chuyển vào module `client` và chạy lệnh sau:

```bash
cd client
mvn javafx:run
```

#### Cách 2: Chạy tệp JAR đã đóng gói
Di chuyển vào thư mục `client/target` và chạy tệp JAR ứng dụng:

```bash
java -jar client-1.0-SNAPSHOT-jar-with-dependencies.jar
```

Bạn có thể khởi chạy nhiều terminal để mở **nhiều Client song song**, giúp demo các kịch bản đấu giá realtime giữa các tài khoản khác nhau (ví dụ: một tài khoản tạo phiên đấu giá làm seller, các tài khoản khác tham gia đặt giá thầu làm bidder cạnh tranh hoặc cài đặt Auto-bid).

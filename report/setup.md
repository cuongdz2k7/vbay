# Hướng Dẫn Cài Đặt Và Khởi Chạy vBay

Tài liệu này hướng dẫn cài đặt môi trường, cấu hình database, build và khởi chạy server/client.

## 1. Yêu Cầu Môi Trường

- JDK 25, tối thiểu nên dùng JDK 21+ nếu cần tương thích.
- Maven 3.9+.
- MySQL Server 8.0+ hoặc 9.x.
- Hệ điều hành desktop có hỗ trợ JavaFX: Windows, macOS, Linux.

## 2. Cấu Hình MySQL

Server tự khởi tạo database và schema bằng `DatabaseInitializer` khi start. Schema nằm tại:

```text
server/src/main/resources/data_init.sql
```

Cấu hình kết nối mặc định nằm trong:

```text
server/src/main/java/com/vbay/server/databaseManager/DatabaseConfig.java
```

Giá trị mặc định:

| Tham số | Giá trị |
| :--- | :--- |
| Host | `localhost` |
| Port | `1638` |
| Database | `vbay` |
| Username | `root` |
| Password | `1234` |

Nếu MySQL của máy đang chạy ở port hoặc mật khẩu khác, sửa `DatabaseConfig.java` trước khi chạy server.

## 3. Build Dự Án

Tại thư mục gốc `vbay/`, chạy:

```bash
mvn clean package
```

Lệnh này build 3 module `shared`, `server`, `client`, chạy test và tạo JAR trong từng thư mục `target`.

Nếu chỉ cần build nhanh không chạy test:

```bash
mvn clean package -DskipTests
```

## 4. Chạy Server

Khuyên dùng khi phát triển:

```bash
mvn -pl server exec:java
```

Hoặc chạy JAR sau khi package:

```bash
java -jar server/target/server-1.0-SNAPSHOT-jar-with-dependencies.jar
```

Server thành công sẽ mở:

- TCP socket server: port `3618`.
- Image HTTP server: port `1639`.
- Kết nối MySQL theo `DatabaseConfig`.

## 5. Chạy Client

Khuyên dùng khi phát triển:

```bash
mvn -pl client javafx:run
```

Hoặc chạy JAR sau khi package:

```bash
java -jar client/target/client-1.0-SNAPSHOT-jar-with-dependencies.jar
```

Client JAR dùng manifest main class `com.vbay.Launcher`, còn khi chạy Maven plugin thì dùng `com.vbay.MainApp`.

## 6. Demo Nhiều Client

Để demo realtime, chạy server trước, sau đó mở nhiều terminal client:

```bash
mvn -pl client javafx:run
```

Mỗi client đăng nhập bằng tài khoản khác nhau để demo:

- User A tạo auction.
- User B đặt bid.
- User C dùng auto-bid hoặc buy now.
- Admin approve deposit, lock/warn/kick user hoặc stop auction.

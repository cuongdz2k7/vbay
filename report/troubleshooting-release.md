# Troubleshooting Và Release vBay

Tài liệu này tổng hợp lỗi thường gặp khi chạy vBay và cách đóng gói JAR phân phối.

## 1. Lỗi Cổng Mạng

Triệu chứng:

- Server báo `java.net.BindException: Address already in use`.
- Client không kết nối được server hoặc ảnh sản phẩm không load.

Nguyên nhân thường gặp:

- TCP socket server port `3618` đang bị chiếm.
- Image HTTP server port `1639` đang bị chiếm.

Kiểm tra trên Windows:

```powershell
Get-NetTCPConnection -LocalPort 3618
Get-NetTCPConnection -LocalPort 1639
```

Cách xử lý:

- Tắt process đang chiếm port.
- Hoặc đổi port tương ứng trong code server/client nếu cần chạy song song nhiều instance.

## 2. Lỗi MySQL Và Database Init

Triệu chứng:

- `Communication link failure`.
- `Access denied for user`.
- Server không tạo được database/schema.

Cấu hình mặc định nằm trong `DatabaseConfig.java`:

| Tham số | Giá trị mặc định |
| :--- | :--- |
| Host | `localhost` |
| Port | `1638` |
| Database | `vbay` |
| Username | `root` |
| Password | `1234` |

Cách xử lý:

- Đảm bảo MySQL đang chạy.
- Kiểm tra port thực tế của MySQL, nhiều máy dùng `3306` thay vì `1638`.
- Sửa username/password trong `DatabaseConfig.java` cho khớp môi trường.
- Kiểm tra `server/src/main/resources/data_init.sql` nếu lỗi schema init.

## 3. Lỗi JavaFX Runtime, FXML, CSS Và Media

Triệu chứng:

- Client không mở UI.
- Lỗi `JavaFX runtime components are missing`.
- Lỗi `Location is not set` hoặc không load được FXML.
- Nhạc/media không phát hoặc báo thiếu module.

Cách xử lý:

- Khi phát triển, ưu tiên chạy client bằng Maven plugin:

```bash
mvn -pl client javafx:run
```

- Kiểm tra `client/pom.xml` có `javafx-controls`, `javafx-fxml`, `javafx-media`.
- Nếu lỗi FXML/resource, kiểm tra đường dẫn trong controller và đảm bảo file nằm trong `client/src/main/resources`.
- Nếu media không phát, kiểm tra JavaFX media module, định dạng file và đường dẫn resource.
- Với cảnh báo native access, client Maven plugin đã có option `--enable-native-access=javafx.graphics`.

## 4. Lỗi Image Upload Hoặc Không Hiển Thị Ảnh

Triệu chứng:

- Tạo auction không lưu được ảnh.
- Card/list item hiện ảnh trắng.
- URL ảnh trả về nhưng browser/client không tải được.

Cách xử lý:

- Đảm bảo thư mục `uploads/` tồn tại và có quyền ghi.
- Đảm bảo `ImageHttpServer` chạy trên port `1639`.
- Kiểm tra URL ảnh được trả về cho client.
- Khi chạy bằng JAR, đặt `uploads/` cùng môi trường làm việc của server hoặc cấu hình lại đường dẫn lưu ảnh.

## 5. Đóng Gói Release

Build toàn bộ project:

```bash
mvn clean package
```

Build nhanh không chạy test:

```bash
mvn clean package -DskipTests
```

Artifact sau khi package:

| Module | JAR |
| :--- | :--- |
| Server | `server/target/server-1.0-SNAPSHOT-jar-with-dependencies.jar` |
| Client | `client/target/client-1.0-SNAPSHOT-jar-with-dependencies.jar` |

Chạy server JAR:

```bash
java -jar server/target/server-1.0-SNAPSHOT-jar-with-dependencies.jar
```

Chạy client JAR:

```bash
java -jar client/target/client-1.0-SNAPSHOT-jar-with-dependencies.jar
```

Server JAR dùng main class `com.vbay.server.ServerApplication`. Client JAR dùng launcher `com.vbay.Launcher`, còn khi chạy bằng Maven JavaFX plugin thì main app là `com.vbay.MainApp`.

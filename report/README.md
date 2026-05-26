# Báo Cáo Kỹ Thuật vBay

Thư mục này gồm các tài liệu thiết kế, cài đặt, vận hành và sử dụng hệ thống đấu giá trực tuyến vBay. Tài liệu được viết để phục vụ báo cáo đồ án và bám sát code hiện tại trong 3 module Maven: `shared`, `server`, `client`.

## Cách Đọc Tài Liệu

1. Đọc `overview.md` để nắm mục tiêu, phạm vi, tính năng và kiến trúc tổng quan.
2. Đọc `setup.md` để cài đặt môi trường, cấu hình MySQL, build và chạy server/client.
3. Đọc `database-erd.md` để hiểu schema thật trong `server/src/main/resources/data_init.sql`.
4. Đọc `protocol.md` để hiểu giao thức TCP JSON, `RequestType`, `RealtimeEventType`, `RoomType`.
5. Đọc `realtime-architecture.md` để hiểu realtime room, event broadcaster, scheduler và concurrency.
6. Đọc `bidding-architecture.md` để hiểu bid engine, resolution, applier và result pipeline.
7. Đọc `class-diagram.md` để xem bản đồ module, class chính và các sequence flow nghiệp vụ.
8. Đọc `testing-guide.md` để chạy test tự động và test thủ công khi demo.
9. Đọc `user-manual.md` nếu cần hướng dẫn thao tác cho người dùng cuối.
10. Đọc `troubleshooting-release.md` nếu gặp lỗi khi chạy hoặc cần đóng gói phát hành.

## Trạng Thái Tài Liệu

| File | Vai trò | Trạng thái |
| :--- | :--- | :--- |
| `overview.md` | Tổng quan, yêu cầu, business rules | Đã đồng bộ lại với role `USER`/`ADMIN` |
| `setup.md` | Cài đặt và khởi chạy | Đã đối chiếu Maven packaging |
| `database-erd.md` | Thiết kế database | Đã đối chiếu `data_init.sql` |
| `protocol.md` | TCP protocol và realtime event | Đã đối chiếu enum trong `shared` |
| `realtime-architecture.md` | Realtime, scheduler, concurrency | Tập trung vào room/event/scheduler |
| `bidding-architecture.md` | Bid engine, resolution, applier, result pipeline | Mới tách riêng từ phần realtime |
| `class-diagram.md` | Bản đồ class/module và sequence | Cần rà tiếp nếu muốn đồng bộ sâu hơn |
| `testing-guide.md` | Test tự động và test thủ công | Cần sửa phần H2 integration test |
| `user-manual.md` | Hướng dẫn sử dụng | Cần sửa cách mô tả role nghiệp vụ |
| `troubleshooting-release.md` | Lỗi thường gặp và release | Cần bổ sung lỗi JavaFX/FXML/media |

## Quy Ước Quan Trọng

- Tài khoản chỉ có `Position.USER` và `Position.ADMIN`.
- `seller` và `bidder` là vai trò nghiệp vụ theo từng auction, được thể hiện bằng `auctions.seller_id` và `bids.bidder_id`.
- Realtime room hiện tại gồm `AUCTION`, `USER`, `AUCTION_LIST`; không dùng `AUCTION_DETAIL`.
- Mật khẩu được băm bằng `Argon2PasswordHasher`.
- Tiền đang giữ nằm trong `hold_balance`; tiền thanh toán cuối cùng được ghi trong bảng `payments`.

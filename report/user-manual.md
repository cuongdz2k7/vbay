# Hướng Dẫn Sử Dụng vBay

Tài liệu này mô tả thao tác chính của người dùng cuối trên ứng dụng desktop vBay. Hệ thống hiện có 2 role tài khoản: `USER` và `ADMIN`. Một tài khoản `USER` có thể vừa tạo auction của mình, vừa bid hoặc buy now auction của người khác.

## 1. Đăng Nhập Và Trang Chủ

![Home screen](images/home.png)

1. Mở client JavaFX và đăng nhập bằng tài khoản đã đăng ký.
2. Trang chủ hiển thị danh sách auction theo trạng thái, danh mục và từ khóa tìm kiếm.
3. Mỗi auction card hiển thị ảnh sản phẩm, title, current price, trạng thái, thời gian còn lại và thông tin người đang thắng nếu có.
4. Client luôn fetch snapshot mới nhất trước khi render, sau đó nhận realtime event để cập nhật card bằng `auctionVersion` hoặc `updatedAt`.

Tài khoản admin mặc định được auto-provision khi đăng nhập bằng:

```text
username: admin
password: admin
```

## 2. Ví Và Deposit

![Deposit screen](images/deposit.png)

1. User mở màn hình ví/deposit.
2. Nhập số tiền muốn nạp và gửi yêu cầu deposit.
3. Yêu cầu chuyển sang trạng thái pending để admin phê duyệt.
4. Khi admin approve, balance của user được cập nhật qua realtime room `USER`.

Ví gồm:

- `available_balance`: số dư có thể dùng để bid, auto-bid hoặc buy now.
- `hold_balance`: số dư đang bị giữ cho bid hoặc auto-bid đang thắng.

## 3. Tạo Auction

![Create auction screen](images/create-auction.png)

1. User mở màn hình tạo auction.
2. Nhập thông tin product: tên, mô tả, category, condition và hình ảnh.
3. Nhập thông tin auction: title, description, starting price, minimum bid step, reserve price nếu có, buy now price nếu có.
4. Chọn `starting_time` và `ending_time`.
5. Submit để tạo auction. Auction mới thường ở trạng thái `SCHEDULED` và scheduler sẽ tự chuyển sang `ACTIVE` khi tới giờ.

Rule quan trọng:

- `reserve_price` nếu có phải lớn hơn `starting_price`.
- `buy_now_price` nếu có reserve thì phải lớn hơn hoặc bằng `reserve_price`.
- User không được bid auction do chính mình tạo.

## 4. Đặt Bid Thủ Công

![Auction detail screen](images/auction-detail.png)

1. User mở auction đang `ACTIVE`.
2. Màn hình chi tiết hiển thị current price, reserve state, thời gian còn lại, bid history và trạng thái bid của viewer.
3. User nhập bid amount và bấm place bid.
4. Bid đầu tiên có thể bằng `starting_price`; các bid sau phải đạt tối thiểu `current_price + minimum_bid_step`.
5. Nếu bid thành công, server hold đúng `bid_amount`.
6. Nếu user bị outbid, hold cũ được release và my-bid state cập nhật qua realtime.

Client có thể hiển thị mức bid gợi ý, nhưng server vẫn validate lại toàn bộ rule trước khi nhận bid.

## 5. Auto-bid

![Auto-bid panel](images/autobid.png)

Auto-bid là proxy bidding. User nhập mức trần `maxBidAmount`, hệ thống chỉ tạo bid thực tế khi cần bảo vệ vị trí thắng.

1. User mở tab hoặc panel auto-bid trong auction detail.
2. Nhập max amount thấp hơn buy now price nếu auction có buy now.
3. Server hold toàn bộ `maxBidAmount` khi auto-bid được đăng ký thành công.
4. Giá public không nhảy ngay lên max. Engine chỉ tăng bid thực tế khi có bid khác đe dọa auto-bid.
5. Nếu max auto-bid chạm reserve price, engine có thể tạo bid ở reserve để auction đạt điều kiện thắng cuối cùng.
6. User đang có auto-bid thắng có thể tăng max; server chỉ hold thêm phần delta.

`maxBidAmount` là dữ liệu riêng tư. Người khác chỉ thấy bid thực tế và current price, không thấy trần auto-bid của user.

## 6. Buy Now

1. Nếu auction có `buy_now_price` và chưa đóng, user có thể bấm buy now.
2. Buy now thắng mọi bid và auto-bid hiện tại.
3. Nếu buyer đang là current winner, server release hold cũ trước rồi charge buy now price.
4. Payment buy now được tạo ở trạng thái `HELD`.
5. Auction chuyển sang trạng thái kết thúc và các client nhận realtime update.

## 7. My Bids Và My Auctions

![My bids screen](images/my-bids.png)

- My Bids hiển thị các auction user đã tham gia bid hoặc auto-bid.
- My Auctions hiển thị các auction do user tạo.
- Các màn này nhận realtime event riêng theo room `USER` hoặc public auction/list room, sau đó merge bằng `auctionVersion` và `updatedAt`.

## 8. Admin Dashboard

![Admin dashboard](images/admin-dashboard.png)

Admin có dashboard riêng để:

- Xem danh sách user và auction.
- Approve hoặc reject deposit request.
- Warn, kick, lock hoặc ban user theo rule hệ thống.
- Stop, continue hoặc delete auction khi cần can thiệp.

Các thao tác admin được validate bằng `ClientSession.position == ADMIN` ở server. Client UI chỉ là lớp hiển thị thao tác, không quyết định quyền admin.

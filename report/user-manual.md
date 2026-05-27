# Hướng Dẫn Sử Dụng Nền Tảng Đấu Giá vBay

Chào mừng bạn đến với hướng dẫn sử dụng chi tiết hệ thống vBay. Nền tảng được tối ưu hóa giao diện đồ họa hiện đại trên JavaFX, cung cấp trải nghiệm mượt mà với 2 vai trò tài khoản chính: **Người Dùng (USER)** và **Quản Trị Viên (ADMIN)**, trong đó người dùng thông thường có thể tự do đóng vai trò **Người Bán (seller)** hoặc **Người Đấu Giá (bidder)** tùy theo từng phiên.

---

## 1. Hướng Dẫn Dành Cho Người Đấu Giá (bidder)

Khi đăng nhập bằng tài khoản người dùng thông thường (`Position.USER`), bạn sẽ có các quyền năng của một bidder trong các phiên đấu giá của người khác:

### A. Giao diện trang chủ & Tìm kiếm phiên đấu giá
*   **Danh sách phiên**: Màn hình chính hiển thị danh sách các phiên đấu giá trực quan dưới dạng các thẻ (Card). Mỗi thẻ hiển thị ảnh sản phẩm, tên, tình trạng, giá hiện tại, người dẫn đầu và đồng hồ đếm ngược.
*   **Bộ lọc danh mục (Category filter)**: Chọn danh mục sản phẩm ở thanh điều hướng bên trái để lọc nhanh sản phẩm quan tâm.
*   **Tìm kiếm & Trạng thái**: Bạn có thể lọc nhanh các phiên đang diễn ra (`ACTIVE`) hoặc sắp diễn ra (`SCHEDULED`).

### B. Nạp tiền vào ví điện tử
1.  Bấm vào biểu tượng ví tiền hoặc mục **Số dư ví** ở góc trên bên phải màn hình.
2.  Nhập số tiền muốn nạp (ví dụ: `5,000,000đ`) vào ô nhập liệu.
3.  Bấm nút **Gửi yêu cầu nạp** (Deposit Balance). Trạng thái yêu cầu sẽ ở chế độ Chờ duyệt (`PENDING`).
4.  Khi Admin phê duyệt, số dư khả dụng (`Available Balance`) của bạn lập tức tăng lên.

### C. Đặt giá thầu thủ công (Place Bid)
1.  Bấm vào một phiên đấu giá đang trực tiếp để mở **Phòng đấu giá chi tiết**.
2.  Xem lịch sử đặt thầu hiển thị ở bảng bên phải và biểu đồ đường biến động giá thầu realtime ở giữa.
3.  Ô nhập giá thầu tự động đề xuất mức giá tối thiểu tiếp theo = **Giá hiện tại + Bước giá**. Bạn có thể nâng cao mức giá này nếu muốn.
4.  Bấm nút **Đặt thầu** (Place Bid). Nếu thành công:
    *   Nhãn trạng thái của bạn đổi sang màu xanh lá cây: **DẪN ĐẦU** (WINNING).
    *   Tiền ví tương ứng mức thầu bị phong tỏa để đảm bảo giao dịch.
    *   Nếu có ai khác đặt giá cao hơn, bạn sẽ nhận được thông báo trạng thái **BỊ VƯỢT** (OUTBID), tiền phong tỏa lập tức hoàn về ví khả dụng của bạn.

### D. Cài đặt đấu giá tự động (Auto-Bid)
1.  Trong phòng đấu giá chi tiết, chuyển sang tab **Auto-Bid**.
2.  Nhập **Mức giá thầu tối đa** bạn sẵn sàng trả cho sản phẩm này.
3.  Bấm **Kích hoạt** (Activate Auto-bid). Hệ thống sẽ thay mặt bạn tự động đặt giá thầu cạnh tranh mỗi khi có người khác trả giá cao hơn, đảm bảo bạn luôn dẫn đầu với chi phí tối thiểu cho đến khi vượt quá giới hạn tối đa bạn cài đặt.
4.  Bạn có thể tăng mức giới hạn tối đa này hoặc bấm **Hủy tự động** bất kỳ lúc nào.

### E. Mua đứt sản phẩm (Buy Now)
*   Nếu người bán cấu hình giá mua đứt (`buy_now_price`) và phiên đấu giá chưa kết thúc, nút **Mua Ngay** (Buy Now) sẽ hiển thị.
*   Bấm **Mua Ngay** để thanh toán trực tiếp và chốt quyền sở hữu sản phẩm lập tức mà không cần chờ đếm ngược kết thúc phiên.

---

## 2. Hướng Dẫn Dành Cho Người Bán (seller)

Người bán chịu trách nhiệm quản lý sản phẩm và điều hành các phiên đấu giá của mình:

### A. Đăng bán sản phẩm mới (Tạo phiên đấu giá)
1.  Truy cập mục **Tạo phiên đấu giá** (Create Auction) từ thanh thực đơn của ứng dụng (trong tài khoản `USER`).
2.  Điền các thông tin bắt buộc:
    *   **Tên sản phẩm & Mô tả chi tiết**.
    *   **Danh mục & Tình trạng sản phẩm** (Mới/Cũ).
    *   **Hình ảnh**: Chọn tệp tin hình ảnh sản phẩm từ máy tính của bạn (hệ thống hỗ trợ tự động upload ảnh lên máy chủ lưu trữ).
3.  Cấu hình luật đấu giá:
    *   **Giá khởi điểm**: Mức giá bắt đầu của phiên.
    *   **Bước giá tối thiểu**: Khoảng cách giá thầu tối thiểu bắt buộc giữa các lượt thầu liên tiếp.
    *   **Giá mua ngay (Tùy chọn)**: Mức giá cho phép người mua trả tiền mua đứt sản phẩm ngay lập tức.
    *   **Giá sàn bảo vệ (Tùy chọn)**: Mức giá tối thiểu người bán chấp nhận bán sản phẩm (nếu khi kết thúc phiên mà giá thầu cao nhất chưa đạt mức này, phiên đấu giá sẽ thất bại).
4.  Thiết lập thời gian: Chọn **Ngày giờ bắt đầu** và **Ngày giờ kết thúc** phiên đấu giá.
5.  Bấm **Đăng bán**. Phiên đấu giá sẽ tự động được đưa vào danh sách chờ kích hoạt (`SCHEDULED`) hoặc trực tiếp (`ACTIVE`) đúng giờ.

### B. Quản lý phiên đấu giá (My Auctions)
*   Truy cập mục **Quản lý của tôi** để xem toàn bộ danh sách phiên đấu giá bạn đã tạo, phân loại theo trạng thái: Sắp diễn ra, Đang diễn ra, Đã kết thúc thành công, Thất bại.
*   Bạn có quyền **Hủy phiên đấu giá** (Cancel Auction) đối với các phiên đang ở trạng thái lên lịch chờ (`SCHEDULED`) chưa mở cửa.

---

## 3. Hướng Dẫn Dành Cho Quản Trị Viên (ADMIN)

Tài khoản Admin (`Position.ADMIN`) có quyền kiểm soát toàn bộ hệ thống để đảm bảo tính minh bạch và an toàn tài chính:

### A. Phê duyệt yêu cầu nạp tiền (Approve Deposits)
1.  Vào bảng điều khiển **Admin Dashboard**, chọn tab **Yêu cầu nạp tiền** (Pending Deposits).
2.  Xem chi tiết thông tin: Tên người dùng, số tiền yêu cầu nạp, thời gian gửi.
3.  Bấm **Duyệt** (Approve) sau khi đã xác thực giao dịch chuyển khoản thực tế của người dùng, hoặc bấm **Từ chối** (Reject) nếu thông tin sai lệch.

### B. Kiểm soát tài khoản người dùng (User Management)
1.  Chọn mục **Quản lý người dùng** (User Management) để xem danh sách tài khoản toàn hệ thống.
2.  **Khóa tài khoản (Lock User)**: Nếu người dùng vi phạm luật chơi (như đặt thầu ảo hoặc spam), Admin có thể chọn Khóa tài khoản và điền lý do khóa. Tài khoản bị khóa sẽ không thể đăng nhập hoặc đặt giá thầu trong khoảng thời gian quy định.
3.  **Cảnh cáo (Warn User)**: Gửi cảnh báo trực tiếp kèm lý do đến màn hình người dùng.

### C. Can thiệp phiên đấu giá (Auction Supervision)
*   Admin có quyền kiểm soát tất cả phiên đấu giá đang diễn ra.
*   **Hủy phiên đấu giá có tranh chấp (Stop Auction)**: Dừng khẩn cấp phiên đấu giá đang diễn ra nếu phát hiện gian lận. Lập tức tất cả số tiền phong tỏa (Hold Balance) của những người tham gia phòng đấu giá đó được hoàn trả nguyên vẹn về ví khả dụng của họ.
*   **Nhật ký kiểm toán (Admin Log)**: Mọi thao tác phê duyệt ví, khóa người dùng, hủy phiên thầu của Admin đều được ghi tự động vào bảng `admin_actions_log` trong cơ sở dữ liệu để phục vụ công tác thanh tra bảo mật sau này.

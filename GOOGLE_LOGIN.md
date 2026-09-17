# Google login

Ứng dụng dùng Google Identity Services với ID token được xác minh tại server bằng thư viện Google. Không cần Client Secret cho luồng này.

## Cấu hình

1. Trong Google Cloud, dùng OAuth client loại Web application. Thêm cả `http://localhost` và `http://localhost:8080` vào Authorized JavaScript origins. Nếu truy cập bằng IP, thêm `http://127.0.0.1:8080`. Origin phải khớp địa chỉ mở trên trình duyệt.
2. Đặt `GOOGLE_CLIENT_ID` trong `.env` (xem `.env.example`), rồi khởi động lại bằng `tools/run-java.ps1`. Không commit `.env`.
3. Database cũ cần chạy `database/google_identity_migration.sql` bằng tài khoản có quyền thay đổi schema. Database mới dùng `database/database_schema.sql` đã gồm migration. Migration chỉ thêm cột `Users.google_subject` và unique filtered index, không xóa dữ liệu.
4. Mở `http://localhost:8080/plagiarism/login`, chọn nút Google.

## Chính sách tài khoản

- Email phải có tài khoản trong Users trước. Không tự tạo Admin, giảng viên hoặc sinh viên từ yêu cầu phía trình duyệt.
- Lần liên kết đầu chỉ chấp nhận email Gmail đã xác minh hoặc email Google Workspace có claim `hd` và đã xác minh. Tài khoản Google dùng email bên thứ ba không được tự liên kết chỉ bằng email.
- Sau khi liên kết, tra cứu bằng Google `sub` ổn định. Vai trò lấy từ database. Hai yêu cầu đồng thời được bảo vệ bằng transaction, khóa và unique index.
- Server xác minh chữ ký, issuer, audience, thời hạn, email_verified và nonce gắn session có hạn 5 phút. Challenge chỉ dùng một lần.

## Kiểm tra

`tools/test-java.ps1` chạy unit tests và JDBC integration tests trên database kiểm thử riêng. Test token dùng khóa RSA thử nghiệm, không chứng minh đăng nhập Google thật.

Khi runtime chạy với `.env.test`, chạy `python tools/verify_google_http.py` để kiểm tra cấu hình và từ chối token giả/replay. `python tools/verify_google_browser.py` kiểm tra nút GIS và popup Google thật, không nhập thông tin tài khoản.

Đăng nhập bằng tài khoản Google thật đến hết callback vẫn cần người dùng thực hiện. Mỗi session hiện giữ một challenge; mở thêm tab login sẽ làm challenge của tab trước mất hiệu lực. Nếu đăng nhập lỗi, quay lại trang login và thử lại để lấy challenge mới.

Kiểm tra ngày 18/09/2026: 116 tests qua, gồm kiểm thử liên kết JDBC đồng thời; 67 kiểm tra HTTP/SQL hồi quy qua và kiểm tra HTTP Google từ chối token giả/replay qua. Sau khi cập nhật Authorized JavaScript origins, trình duyệt tải nút GIS và mở trang nhập email của Google, không còn báo lỗi origin. Chưa xác minh đăng nhập bằng tài khoản Google thật đến hết callback.

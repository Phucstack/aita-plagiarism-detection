# Báo cáo kiểm chứng tuần 1–3 AITA CodeDefend

Ngày cập nhật: 18/09/2026. Các lượt kiểm thử chạy ngày 17–18/09/2026. Phạm vi: Identity, JDBC/SQL Server, Course Management và giao diện nền tảng theo mục II của RBL PRJ301 Project.docx. Đối chiếu PRJ30x_Project_Evaluation_Rubric.docx; không tự quy đổi kết quả kiểm tra thành điểm của giảng viên.

## Các sửa lỗi đã thực hiện

- JWT dùng JJWT 0.13.0 với Jackson 2.21.6 để serialize/parse JSON và xác minh HS256, issuer, thời hạn, kiểu user ID. Nội dung họ tên không thể ghi đè claim định danh.
- JWT_SECRET lấy từ môi trường, tối thiểu 32 byte; không còn khóa ký mặc định trong source.
- Login đổi session ID; cookie HttpOnly, SameSite=Strict, giới hạn theo context và bật Secure khi request qua HTTPS.
- Filter kiểm tra token và người dùng hiện tại trong DB, bảo vệ cả URL servlet và JSP liên quan; session cũ không bỏ qua token sai/hết hạn.
- Google login đã bật với xác minh ID token phía server, nonce chống replay và liên kết Google subject với tài khoản có sẵn; quyền lấy từ database. Cần GOOGLE_CLIENT_ID hợp lệ.
- DAO không trả tài khoản mẫu hoặc dữ liệu giả khi SQL lỗi. Lỗi persistence được truyền tới HTTP boundary; conflict trả 409, lỗi DB trả 503.
- Course CRUD kiểm tra quyền chủ sở hữu/Admin. UPDATE/DELETE có điều kiện quyền ngay trong SQL, tránh chỉ kiểm tra ở servlet.
- Danh sách rỗng và ID không tồn tại giữ đúng trạng thái rỗng/không tìm thấy. Cổng sinh viên không lấy bài người khác để lấp lịch sử rỗng.
- Script SQL chỉ tạo phần còn thiếu và seed database mới trống; không DROP bảng, đổi mật khẩu sa hay cấp sysadmin.
- tools/run-java.ps1 thực sự đóng gói WAR và chạy Tomcat 10.1. Preview HTML vẫn là prototype riêng.
- Bộ test sử dụng database riêng. Không còn coi lỗi kết nối DB là kết quả PASS của kiểm thử tích hợp.

## Bằng chứng kiểm chứng

| Phạm vi | Bằng chứng | Giới hạn |
|---|---|---|
| Java và WAR | Maven biên dịch và đóng gói thành công với JDK 17 | Build không thay thế runtime |
| JUnit và JDBC | 112 tests, không failure/error/skip; có truy vấn và CRUD SQL Server thật trên database kiểm thử | Có cả test unit/mock và test JDBC; không gọi toàn bộ là end-to-end |
| JWT regression | Tên chứa dấu phẩy, userId và role giả vẫn giữ nguyên định danh; token sai/hết hạn/thiếu expiry bị từ chối | Không thay thế audit bảo mật toàn hệ thống |
| Course concurrency | Hai thao tác đồng thời tạo cùng mã: đúng một bản ghi được commit | Chỉ kiểm tra tình huống trùng mã khóa học |
| Tomcat/HTTP/SQL | Kịch bản tools/verify_week3_http.py kiểm tra đăng nhập, vai trò, JWT, CRUD và đọc SQL độc lập sau commit | 27 kiểm tra PASS; kết quả lưu target/week3-http-verification.json |
| Review độc lập | Đã rà JWT, DB failure, quyền khóa học, concurrency và script; các finding trong phạm vi được xử lý | Review tĩnh không phải chứng nhận runtime |
| UI | Edge headless với giảng viên/sinh viên tại 1440, 768, 375 px; chọn bài tập và mở báo cáo thật; không page error hay tràn ngang toàn trang | Chỉ nghiệm thu các luồng đã chạy; bảng có thể cuộn ngang bên trong ở màn hình nhỏ |

SQL verification chạy trên AITA_Week3_Verification, tách khỏi dữ liệu ứng dụng. Dữ liệu HTTP test được tạo với định danh riêng và dọn sau kiểm tra. Báo cáo Surefire nằm trong target/surefire-reports. Ảnh trình duyệt nằm trong target/week3-browser. Chạy lại schema trên database kiểm thử giữ nguyên số dòng ở cả sáu bảng.

## Cách chạy

1. Cài JDK 17, Maven, SQL Server và Apache Tomcat 10.1.
2. Điền DB_SERVER, DB_PORT, DB_NAME, DB_USER, DB_PASSWORD, JWT_SECRET và CATALINA_HOME trong .env theo .env.example. Dùng tài khoản chỉ có quyền cần thiết trên database ứng dụng.
3. Chạy `powershell -NoProfile -ExecutionPolicy Bypass -File tools/run-java.ps1`, hoặc mục chạy Java trong CHAY_ALL.bat.
4. Mở http://localhost:8080/plagiarism/login.
5. Dừng process do script tạo bằng `powershell -NoProfile -ExecutionPolicy Bypass -File tools/run-java.ps1 -Stop`.

Để kiểm thử: tạo .env.test trỏ tới database riêng chứa Test hoặc Verification trong tên; chạy tools/test-java.ps1. Script từ chối DB_URL để tránh ghi đè database đã kiểm tra. Để kiểm tra HTTP, khởi động Tomcat với `-EnvironmentFile .env.test`, rồi chạy `python tools/verify_week3_http.py`. Không chạy CRUD test trên database ứng dụng.

## Phạm vi chưa được nghiệm thu

- Không tuyên bố hoàn thành AI thật, Docker sandbox, grading queue hay toàn bộ tuần 4–9 từ kết quả tuần 1–3.
- Google login không tự đăng ký người dùng; email phải được cấp tài khoản trước. Đăng nhập Google thật đến hết callback cần người dùng thực hiện bằng tài khoản của họ.
- Quyền assignment/report và xóa bài nộp đã được sửa theo ma trận trong DEMO_TUAN_1_3.md. Chưa tuyên bố toàn bộ hệ thống đạt yêu cầu production.
- Mật khẩu cũ MD5/SHA-256 được chuyển sang PBKDF2 khi đăng nhập đúng; đổi mật khẩu dùng salt ngẫu nhiên và cập nhật có điều kiện. Chưa có cơ chế thu hồi mọi phiên sau đổi mật khẩu.
- Sáu bảng và sơ đồ ERD đã có; điều đó không tự chứng minh toàn bộ chuẩn hóa 3NF. Quan hệ và quy tắc nghiệp vụ cần được bảo vệ bằng lập luận cụ thể.
- Không yêu cầu tách Users thành các bảng Admin/Instructor/Student chỉ để đạt mốc tuần 1–3.

## Bản sửa bổ sung sau tuần 1–3

Dashboard dùng dữ liệu JDBC theo phạm vi giảng viên và bài tập được chọn. Đã bỏ biểu đồ, thống kê và dòng báo cáo mẫu. Student portal không tạo lịch sử giả. Diff Inspector hiển thị bản ghi được chọn và khối mã đã lưu; sinh viên chỉ được nhận kết quả cá nhân, không có dữ liệu peer.

Assignment CRUD và xóa bài nộp kiểm tra sở hữu trong SQL mutation. Batch scanner chỉ liệt kê và nhận bài tập được phép. JSP trực tiếp chuyển về controller. File thô trong /uploads bị chặn.

Mật khẩu dùng PBKDF2-HMAC-SHA256 (600.000 vòng, salt 16 byte). HTTP test xác nhận nâng cấp hash cũ, từ chối mật khẩu cũ sau đổi và hai thao tác đổi đồng thời chỉ một thao tác thành công. Không thay đổi schema sáu bảng.

Xem DEMO_TUAN_1_3.md để biết kịch bản trình bày, ma trận quyền, ERD, quyết định kỹ thuật và giới hạn của scan engine. Kết quả kiểm tra bổ sung nằm trong target/followup-http-verification.json và target/followup-browser/results.json.

Kết quả chốt: **112 test Java/JDBC đạt**, **27 kiểm tra HTTP/SQL nền tảng + 40 kiểm tra bổ sung đạt**. Sáu trường hợp viewport/vai trò đạt ở 1440, 768 và 375 px. Đã kiểm tra ảnh của dashboard, portal, báo cáo giảng viên và kết quả sinh viên. Các finding review độc lập về escaping avatar, ngưỡng 0, giữ deadline khi sửa và assignment bị xóa đồng thời đã được xử lý; review nguồn không thay thế kiểm chứng runtime.

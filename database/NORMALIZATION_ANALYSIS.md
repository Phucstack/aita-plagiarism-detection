# Phân tích chuẩn hóa sáu bảng

Ngày đối chiếu: 19/09/2026. Nguồn: `database_schema.sql`, `PlagiarismDAO.java`, `PlagiarismEngineService.java` và luồng lưu submission. Phạm vi là schema sau khi bỏ `PlagiarismReports.assignment_id`; không suy ra chuẩn hóa từ số bảng hoặc số test.

## Tiêu chí và giả định

Với mỗi phụ thuộc hàm không tầm thường X → A, 3NF yêu cầu X là siêu khóa hoặc A là thuộc tính khóa (thuộc ít nhất một khóa ứng viên). Khóa chính đơn không tự chứng minh 2NF/3NF: phải xét cả khóa ứng viên khác và phụ thuộc nghiệp vụ.

Các trường text/code được coi là một giá trị trong miền dữ liệu, không phải nhóm lặp cần truy vấn thành nhiều thuộc tính; nhiều khối đối sánh được lưu thành nhiều dòng MatchingBlocks. NULL là tùy chọn của SQL, không coi unique filtered index trên thuộc tính nullable là khóa ứng viên toàn quan hệ.

Phân tích dùng các phụ thuộc được khai báo bởi PK, UNIQUE NOT NULL và ngữ nghĩa đang triển khai. Không mặc định tên, đường dẫn, checksum hoặc thời gian là duy nhất. Phụ thuộc quan sát được trên một bộ dữ liệu mẫu không chứng minh quy tắc cho mọi trạng thái hợp lệ.

## Phụ thuộc từng bảng

Ký hiệu R là toàn bộ thuộc tính của chính bảng đang xét. Mỗi khóa K dưới đây xác định K → R; đây là cách viết gọn cho các phụ thuộc K → từng thuộc tính ngoài K.

| Bảng | Khóa ứng viên được xác nhận | Thuộc tính ngoài khóa chính | Phụ thuộc ngoài khóa cần xét và kết luận |
|---|---|---|---|
| Users | `{user_id}`, `{username}`, `{email}` (đều NOT NULL, unique) | username, password_hash, full_name, email, role, avatar_url, created_at, google_subject | Mỗi khóa → R. Không có quy tắc email → role hoặc role → full_name. google_subject unique khi khác NULL, không phải khóa của toàn bảng. Đạt 3NF theo tập phụ thuộc này. |
| Courses | `{course_id}`, `{course_code}` | course_code, course_name, instructor_id, semester, created_at | Hai khóa → R. instructor_id không xác định course_name/semester vì giảng viên có nhiều khóa học. course_code unique toàn bảng nên cả phụ thuộc course_code → course_name có vế trái là khóa. Đạt 3NF; tính phù hợp của unique code qua nhiều học kỳ là vấn đề mô hình nghiệp vụ riêng. |
| Assignments | `{assignment_id}` | course_id, title, description, max_score, deadline, similarity_threshold, created_at | assignment_id → R. Không có UNIQUE(course_id,title); title, deadline, course_id không xác định các thuộc tính còn lại. Mỗi bài tập có ngưỡng/điểm/hạn riêng. Đạt 3NF theo tập phụ thuộc này. |
| Submissions | `{submission_id}` | assignment_id, student_id, file_name, file_path, file_type, sha256_hash, submitted_at, status | submission_id → R. Không có khóa (assignment_id,student_id): một sinh viên có thể nộp nhiều lần. Checksum có thể lặp ở nhiều bài; tên và đường dẫn không có UNIQUE. file_type được phân loại ở lúc tiếp nhận, các metadata lưu theo lần nộp. Đạt 3NF theo tập phụ thuộc khai báo; xem giới hạn metadata bên dưới. |
| PlagiarismReports | `{report_id}` | submission_a_id, submission_b_id, similarity_score, risk_level, ai_analysis_summary, created_at | report_id → R. Cặp submission không có UNIQUE nên không tự coi là khóa ứng viên. Điểm/risk/summary là kết quả lần phân tích; score riêng lẻ không xác định risk vì ngưỡng assignment có thể khác. Đạt 3NF theo ngữ nghĩa snapshot và tập phụ thuộc này sau khi loại assignment_id. |
| MatchingBlocks | `{block_id}` | report_id, function_name, student_a_start_line, student_a_end_line, student_b_start_line, student_b_end_line, matched_code_snippet, variable_renaming_notes | block_id → R. Một report có nhiều block; tên hàm và bộ tọa độ không có UNIQUE và không mặc định xác định ghi chú/kết quả phân tích. Đạt 3NF theo tập phụ thuộc này. |

Trong tập phụ thuộc trên, mọi determinant không tầm thường được xác nhận đều là khóa; do đó không có phụ thuộc bộ phận vào khóa ứng viên hoặc phụ thuộc bắc cầu qua thuộc tính không khóa. Kết luận 3NF có phạm vi là tập phụ thuộc và ngữ nghĩa đã nêu, không phải chứng nhận mọi quy tắc nghiệp vụ chưa được mô tả.

## Vi phạm legacy và cách sửa

Trước migration, trong PlagiarismReports tồn tại `submission_a_id → assignment_id` và `submission_b_id → assignment_id`: mỗi submission thuộc một assignment. Hai submission ID riêng lẻ không là siêu khóa của report, còn assignment_id không là thuộc tính khóa. Đây là vi phạm 3NF cụ thể, không chỉ nhận xét rằng cột bị lặp.

Migration bỏ cột đó. DAO lấy assignment bằng JOIN Submissions. CHECK chặn so sánh một submission với chính nó; trigger report chặn cặp khác assignment; trigger submission chặn cập nhật assignment làm sai report hiện có. Các ràng buộc này bảo vệ toàn vẹn, bản thân sự tồn tại của trigger không chứng minh chuẩn hóa.

## Giới hạn cần phân biệt

- Nếu nghiệp vụ quy định vĩnh viễn `file_name → file_type` theo phần mở rộng, hoặc `file_path → sha256_hash` cho một artifact bất biến được chia sẻ, đó là phụ thuộc bổ sung trên Submissions cần phân tích lại; schema hiện không khai báo các quy tắc này. Khi đó có thể cần tách Artifact hoặc loại metadata suy diễn, không được dùng khóa surrogate để bỏ qua phụ thuộc.
- Engine hiện tính risk theo điểm và ngưỡng lúc scan; ngưỡng/phiên bản thuật toán không được chụp đầy đủ vào report. Do đó không tuyên bố tái dựng được mọi kết quả lịch sử từ cấu hình hiện tại. Việc chỉ lưu snapshot không tự xóa một phụ thuộc hàm nếu nghiệp vụ sau này xác định nó.
- Không có UNIQUE cho cặp report và không bắt buộc thứ tự hai ID. Chống trùng cặp, ràng buộc vai trò ở FK và quan hệ enrollment là vấn đề toàn vẹn/nghiệp vụ, không phải bằng chứng tự động vi phạm hoặc đạt 3NF.
- Các kiểm tra migration chứng minh giữ nội dung dòng (trừ cột dư), chạy lại an toàn trong kịch bản đã thử và từ chối ba vi phạm cụ thể. Không chứng minh mọi lịch xen kẽ giao dịch hoặc toàn bộ hệ thống production.

**Kết luận:** đã xác định và sửa vi phạm 3NF của schema legacy. Sáu bảng sau migration phù hợp 3NF đối với các khóa và phụ thuộc được liệt kê; các giả định metadata/snapshot được công khai, không tuyên bố đạt vô điều kiện chỉ vì có sáu bảng và ERD.

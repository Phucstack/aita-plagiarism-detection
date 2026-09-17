# TÀI LIỆU ĐẶC TẢ YÊU CẦU PHẦN MỀM (SOFTWARE REQUIREMENTS SPECIFICATION - SRS)

> Cập nhật triển khai tuần 1–3 ngày 17/09/2026: xem BAO_CAO_TIEN_DO_TUAN_1_3.md.
> Các mục bên dưới là đặc tả mục tiêu, không phải biên bản xác nhận đã hoàn thành.
> Route hiện thực là /dashboard và /student-portal; xác thực dùng JWT cookie kèm session.
> Google login xác minh ID token phía server, chỉ liên kết tài khoản đã được cấp sẵn và giữ quyền trong database; xem GOOGLE_LOGIN.md. Lỗi database không được thay bằng mock data hoặc kết quả thành công.
> Các cam kết hiệu năng, chức năng ngoài tuần 1–3 chưa được nghiệm thu toàn diện từ bản sửa này. Quyền assignment/report, số liệu dashboard và nâng cấp mật khẩu được mô tả tại DEMO_TUAN_1_3.md.


## DỰ ÁN: AITA CODEDEFEND — AI PLAGIARISM & CODE SIMILARITY DETECTION SUITE
**Môn học:** PRJ301 — Web Applications Development (Research-Based Learning)  
**Nhóm thực hiện:** SE20C — Nhóm 7  
**Tiêu chuẩn chất lượng:** Barem đánh giá PRJ30x (100/100 điểm) & Chuẩn đặc tả IEEE 830 / SDD Spec-Kit  

---

# MỤC LỤC
1. [TỔNG QUAN HỆ THỐNG & PHẠM VI (SYSTEM OVERVIEW & SCOPE)](#1-tổng-quan-hệ-thống--phạm-vi)
2. [SƠ ĐỒ PHÂN RÃ CHỨC NĂNG (FUNCTIONAL MAP)](#2-sơ-đồ-phân-rã-chức-năng)
3. [ĐẶC TẢ CHI TIẾT CÁC TÍNH NĂNG (DETAILED FUNCTIONAL SPECIFICATIONS)](#3-đặc-tả-chi-tiết-các-tính-năng)
   - [MOD-01: Xác thực & Phân quyền (Authentication & Access Control)](#mod-01-xác-thực--phân-quyền)
   - [MOD-02: Quản lý Khóa học & Bài tập (Course & Assignment Management)](#mod-02-quản-lý-khóa-học--bài-tập)
   - [MOD-03: Tiếp nhận & Kiểm định Bài nộp (Submission & Artifact Pipeline)](#mod-03-tiếp-nhận--kiểm-định-bài-nộp)
   - [MOD-04: Lõi Đối soát Trùng lặp (Similarity & Plagiarism Engine)](#mod-04-lõi-đối-soát-trùng-lặp)
   - [MOD-05: Trí tuệ Nhân tạo & Nhận diện LLM (AI Intelligence & LLM Detection)](#mod-05-trí-tuệ-nhân-tạo--nhận-diện-llm)
   - [MOD-06: Báo cáo & Giám định Trực quan (Reporting & Diff Inspection)](#mod-06-báo-cáo--giám-định-trực-quan)
   - [MOD-07: Trình diễn 3D & Giao diện Hiện đại (Presentation & 3D Layer)](#mod-07-trình-diễn-3d--giao-diện-hiện-đại)
4. [YÊU CẦU PHI CHỨC NĂNG & MẶT CẮT HỆ THỐNG (CROSS-CUTTING & NON-FUNCTIONAL REQUIREMENTS)](#4-yêu-cầu-phi-chức-năng)
5. [CẤU TRÚC PHÂN RÃ CÔNG VIỆC (WORK BREAKDOWN STRUCTURE - WBS)](#5-cấu-trúc-phân-rã-công-việc-wbs)
6. [MA TRẬN TRUY XUẤT NGUỒN GỐC (TRACEABILITY MATRIX)](#6-ma-trận-truy-xuất-nguồn-gốc)

---

# 1. TỔNG QUAN HỆ THỐNG & PHẠM VI

### 1.1. Bối cảnh & Mục tiêu
Trong quá trình đào tạo lập trình tại bậc đại học (đặc biệt các môn như PRJ301, CSD201), tình trạng sao chép mã nguồn và gian lận học thuật diễn ra ngày càng tinh vi nhờ các kỹ thuật đổi tên định danh (identifier renaming), đảo cấu trúc lệnh điều khiển (control flow restructuring) và đặc biệt là sự trợ giúp từ các công cụ AI thế hệ mới (ChatGPT, Claude, Gemini). Giảng viên đối mặt với tình trạng "Burnout" nghiêm trọng khi phải thủ công rà soát hàng trăm bài nộp.

**AITA CodeDefend** ra đời như một hệ sinh thái thẩm định liêm chính học thuật thế hệ mới, kết hợp chặt chẽ giữa:
1. **Nền tảng Java Web MVC2 chuẩn mực (Jakarta EE 6/10, Tomcat 10.1, SQL Server):** Đáp ứng 100% chuẩn đầu ra môn học PRJ301 theo chuẩn đánh giá thực tế.
2. **Lõi thuật toán đối soát 2 tầng (Deterministic Hybrid Engine):** Kết hợp phân tích từ vựng (Lexer Tokenization), bóc tách chỉ số tương đồng Jaccard & Levenshtein cho mã nguồn Java, và không gian vector TF-IDF kết hợp Cosine Similarity cho bài luận tiếng Anh.
3. **Trợ lý thẩm định AI (Google Gemini API):** Tự động bóc tách các thủ thuật "lách" tinh vi, phát hiện dấu vết văn bản máy sinh và xuất biên bản vi phạm trực quan.

### 1.2. Các bên liên quan (Actors & Personas)
* **ADMIN (Quản trị viên hệ thống):** Quản lý tài khoản, phân quyền, cấu hình kết nối CSDL và tham số hệ thống.
* **INSTRUCTOR (Giảng viên):** Tạo khóa học, bài tập, thiết lập ngưỡng tương đồng, nộp bài hàng loạt (batch zip) cho cả lớp, kích hoạt đối soát và xuất biên bản kỷ luật.
* **STUDENT (Sinh viên):** Tham gia khóa học, nộp bài cá nhân (file mã nguồn/bài luận), kiểm tra tính toàn vẹn qua mã băm SHA-256 và xem trạng thái bài nộp.

---

# 2. SƠ ĐỒ PHÂN RÃ CHỨC NĂNG (FUNCTIONAL MAP)

Hệ thống được phân cấp nghiêm ngặt theo 5 tầng:
`System` $\rightarrow$ `Module` $\rightarrow$ `Feature` $\rightarrow$ `Sub-feature` $\rightarrow$ `Individual Behavior`

```
AITA CodeDefend System
├── MOD-01: Authentication & Access Control (Xác thực & Phân quyền)
│   ├── FE-01.1: Quản lý phiên & Đăng nhập
│   │   ├── SUB-01.1.1: Đăng nhập bằng Form chuẩn mật khẩu mã hóa (BEH-01.1.1.1, BEH-01.1.1.2)
│   │   └── SUB-01.1.2: Đăng xuất & Hủy phiên an toàn (BEH-01.1.2.1)
│   ├── FE-01.2: Bộ lọc Phân quyền RBAC (Role-Based Access Control)
│   │   └── SUB-01.2.1: Bộ lọc AuthenticationFilter & AuthorizationFilter (BEH-01.2.1.1, BEH-01.2.1.2)
│   └── FE-01.3: Quản lý Hồ sơ Người dùng
│       └── SUB-01.3.1: Xem thông tin, đổi mật khẩu và cập nhật avatar (BEH-01.3.1.1)
├── MOD-02: Course & Assignment Management (Quản lý Khóa học & Bài tập)
│   ├── FE-02.1: Quản lý Khóa học
│   │   ├── SUB-02.1.1: CRUD Khóa học (BEH-02.1.1.1)
│   │   └── SUB-02.1.2: Phân trang & Tìm kiếm Khóa học (BEH-02.1.2.1)
│   ├── FE-02.2: Thiết lập Bài tập & Ngưỡng Tương đồng
│   │   ├── SUB-02.2.1: Tạo bài tập, hạn nộp và barem điểm (BEH-02.2.1.1)
│   │   └── SUB-02.2.2: Cấu hình similarity_threshold (BEH-02.2.1.2)
│   └── FE-02.3: Theo dõi Tiến độ Nộp bài (Submission Roster)
│       └── SUB-02.3.1: Thống kê trạng thái nộp bài của sinh viên (BEH-02.3.1.1, BEH-02.3.1.2)
├── MOD-03: Submission & Artifact Pipeline (Tiếp nhận & Kiểm định Bài nộp)
│   ├── FE-03.1: Tiếp nhận Bài nộp Đa định dạng
│   │   ├── SUB-03.1.1: Upload file lẻ (.java, .txt, .docx) (BEH-03.1.1.1, BEH-03.1.1.2)
│   │   └── SUB-03.1.2: Lưu trữ an toàn ngoài Web Root (BEH-03.1.1.3)
│   ├── FE-03.2: Xác thực Toàn vẹn Mã nguồn SHA-256
│   │   └── SUB-03.2.1: Băm trực tiếp byte stream và lưu trữ cơ sở dữ liệu (BEH-03.2.1.1, BEH-03.2.1.2)
│   └── FE-03.3: Bóc tách & Xử lý Hàng loạt (Batch ZIP Processing)
│       ├── SUB-03.3.1: Tiếp nhận và kiểm tra cấu trúc file nén ZIP (BEH-03.3.1.1)
│       └── SUB-03.3.2: Giải nén an toàn chống Zip Slip và tự động nạp Submissions (BEH-03.3.1.2)
├── MOD-04: Similarity & Plagiarism Engine (Lõi Đối soát Trùng lặp)
│   ├── FE-04.1: Phân tích Mã nguồn Java (Java Lexical Normalization)
│   │   ├── SUB-04.1.1: Loại bỏ comment & Chuẩn hóa định danh biến/hàm (BEH-04.1.1.1, BEH-04.1.1.2)
│   │   └── SUB-04.1.2: Trích xuất N-Gram, tính Jaccard & Levenshtein (BEH-04.1.2.1, BEH-04.1.2.2)
│   ├── FE-04.2: Phân tích Bài luận Văn bản (Text NLP Engine)
│   │   ├── SUB-04.2.1: Tiền xử lý NLP: Tokenization, Stopwords, Stemming (BEH-04.2.1.1)
│   │   └── SUB-04.2.2: Không gian vector TF-IDF & Cosine Similarity (BEH-04.2.2.1, BEH-04.2.2.2)
│   ├── FE-04.3: Quét Ma trận Đối soát Chéo (NxN Pairwise Comparison)
│   │   ├── SUB-04.3.1: So khớp tổ hợp C(N, 2) cặp bài nộp (BEH-04.3.1.1)
│   │   └── SUB-04.3.2: Phân cấp nguy cơ: SAFE, LOW, MEDIUM, HIGH_RISK (BEH-04.3.1.2)
│   └── FE-04.4: Bóc tách Khối mã Trùng khớp (Matching Blocks Extraction)
│       └── SUB-04.4.1: Xác định chính xác tọa độ dòng bắt đầu/kết thúc (BEH-04.4.1.1, BEH-04.4.1.2)
├── MOD-05: AI Intelligence & LLM Detection (Trí tuệ Nhân tạo & Nhận diện LLM)
│   ├── FE-05.1: Phân tích Ngữ nghĩa & Thủ thuật Tránh né qua Gemini API
│   │   ├── SUB-05.1.1: Tự động kích hoạt khi vượt ngưỡng similarity_threshold (BEH-05.1.1.1)
│   │   └── SUB-05.1.2: Gemini bóc tách đổi tên biến, đảo lệnh, sinh ai_analysis_summary (BEH-05.1.1.2)
│   └── FE-05.2: Nhận diện Dấu vết Mã/Văn bản Máy sinh (AI Heuristics)
│       └── SUB-05.2.1: Đánh giá độ hỗn loạn cú pháp (Sentence Burstiness) & gắn cờ nghi vấn (BEH-05.2.1.1)
├── MOD-06: Reporting & Diff Inspection (Báo cáo & Giám định Trực quan)
│   ├── FE-06.1: Dashboard Tổng quan & Ma trận Tương đồng Heatmap
│   │   └── SUB-06.1.1: Trực quan hóa ma trận NxN với dải màu cảnh báo (BEH-06.1.1.1, BEH-06.1.1.2)
│   ├── FE-06.2: So sánh Trực quan Song song (Side-by-Side Code Diff Viewer)
│   │   ├── SUB-06.2.1: Giao diện 2 cột đồng bộ thanh cuộn (BEH-06.2.1.1)
│   │   └── SUB-06.2.2: Tô màu khối mã vi phạm và hiển thị ghi chú AI (BEH-06.2.1.2, BEH-06.2.1.3)
│   └── FE-06.3: Xuất Báo cáo Thẩm định Học thuật (Audit Report Export)
│       └── SUB-06.3.1: Xuất biên bản đối soát ra định dạng CSV/PDF phục vụ hội đồng (BEH-06.3.1.1)
└── MOD-07: Modern Presentation & 3D Interactive Layer (Trình diễn 3D & Giao diện Hiện đại)
    ├── FE-07.1: Landing Page 3D Scrollytelling (Three.js WebGL & GSAP)
    │   └── SUB-07.1.1: Render Cyber Shield 3D tương tác theo tiến trình cuộn trang (BEH-07.1.1.1)
    └── FE-07.2: Giao diện Quản trị Phong cách Liquid Glass
        └── SUB-07.2.1: Hệ thống CSS Liquid Glass tiêu chuẩn 2026, thân thiện responsive (BEH-07.2.1.1)
```

---

# 3. ĐẶC TẢ CHI TIẾT CÁC TÍNH NĂNG (DETAILED FUNCTIONAL SPECIFICATIONS)

---

## MOD-01: Xác thực & Phân quyền (Authentication & Access Control)

### Feature FE-01.1: Quản lý Phiên & Đăng nhập (Authentication Management)
* **Purpose:** Xác minh danh tính người dùng, cấp quyền truy cập các phân hệ dựa trên vai trò, duy trì phiên an toàn.
* **User/Actor:** `ADMIN`, `INSTRUCTOR`, `STUDENT`.
* **Preconditions:** Tài khoản đã tồn tại trong bảng `Users` và ở trạng thái hoạt động.
* **Main Flow:**
  1. Người dùng nhập `username` và `password` tại màn hình đăng nhập.
  2. Hệ thống chuyển tiếp yêu cầu đến `LoginServlet` (phương thức `POST`).
  3. `LoginServlet` gọi `UserDAO.authenticate(username, passwordHash)`.
  4. Hệ thống truy vấn CSDL, đối khớp `password_hash`.
  5. Nếu chính xác: Hệ thống tạo `HttpSession`, gán đối tượng `User` vào session attribute `CURRENT_USER`, thiết lập thời gian timeout (30 phút).
  6. Hệ thống chuyển hướng người dùng đến Dashboard tương ứng với vai trò (`/admin/dashboard`, `/instructor/dashboard`, `/student/dashboard`).
* **Alternative Flows:**
  - *Ghi nhớ đăng nhập:* Nếu người dùng chọn "Remember Me", hệ thống tạo một token bảo mật có chữ ký lưu trong Cookie thời hạn 7 ngày.
* **Exception Flows:**
  - *Sai thông tin:* Đăng nhập thất bại, hệ thống tăng biến đếm số lần sai, hiển thị thông báo lỗi: *"Tên đăng nhập hoặc mật khẩu không chính xác"*.
  - *Khóa tài khoản tạm thời:* Nếu đăng nhập sai 5 lần liên tiếp trong vòng 10 phút, hệ thống khóa tài khoản trong 15 phút.
* **Inputs & Data Fields:** `username` (VARCHAR(50), bắt buộc), `password` (VARCHAR(100), bắt buộc, raw text từ form).
* **Outputs:** Phiên làm việc `HttpSession` hợp lệ chứa `user_id`, `username`, `role`, `full_name`, `avatar_url`.
* **Data Structures & Relationships:** Bảng `Users` (`user_id`, `username`, `password_hash`, `full_name`, `email`, `role`).
* **Business Rules & Constraints:**
  - `BR-AUTH-01`: Mật khẩu trong CSDL bắt buộc lưu dưới dạng mã băm một chiều (MD5 hoặc SHA-256), tuyệt đối không lưu raw text.
  - `BR-AUTH-02`: Một phiên làm việc không tương tác sau 30 phút tự động bị hủy (Session Timeout).
* **Validation Rules:**
  - `username`: Từ 3 đến 50 ký tự, không chứa ký tự đặc biệt nguy hiểm (`<`, `>`, `'`, `"`, `--`).
  - `password`: Tối thiểu 6 ký tự.
* **Roles & Permissions:** Tất cả người dùng chưa xác thực (Unauthenticated) được phép truy cập trang login.
* **UI & System States:**
  - UI State: `Form Initial` $\rightarrow$ `Submitting (Loading Spinner)` $\rightarrow$ `Success (Redirect)` / `Error (Alert Box)`.
  - System State: `Anonymous` $\rightarrow$ `Authenticated Session Active`.
* **Error Cases & Edge Cases:**
  - *Mất kết nối CSDL:* Trả về thông báo lỗi thân thiện: *"Không thể kết nối máy chủ cơ sở dữ liệu. Vui lòng liên hệ Admin."*
  - *Session fixation attack:* Tự động gọi `request.changeSessionId()` ngay sau khi người dùng đăng nhập thành công.
* **Dependencies:** `DBContext.java`, `UserDAO.java`, `PasswordUtil.java`.
* **External Integrations:** Không.
* **Postconditions:** Phiên người dùng được khởi tạo, log đăng nhập ghi nhận `username` và địa chỉ IP.
* **Acceptance Criteria:**
  - **[AC-AUTH-01]** Đăng nhập với tài khoản hợp lệ (`teacher_ha / 123456`) phải chuyển hướng đến `/instructor/dashboard` trong vòng 500ms.
  - **[AC-AUTH-02]** Đăng nhập sai mật khẩu phải trả về mã phản hồi kèm thông báo lỗi trực quan trên JSP, không để lộ cấu trúc ngoại lệ CSDL.

### Feature FE-01.2: Bộ lọc Phân quyền RBAC (Role-Based Access Control via Filter)
* **Purpose:** Ngăn chặn tuyệt đối việc truy cập trái phép hoặc leo thang đặc quyền (Privilege Escalation) vào các URL nghiệp vụ.
* **User/Actor:** Hệ thống (`AuthenticationFilter`, `AuthorizationFilter`).
* **Preconditions:** Servlet container đã khởi động và ánh xạ URL pattern trong `web.xml` hoặc annotation `@WebFilter`.
* **Main Flow:**
  1. Client gửi HTTP Request đến một endpoint (ví dụ `/instructor/create-assignment`).
  2. `AuthenticationFilter` kiểm tra thuộc tính `CURRENT_USER` trong `HttpSession`.
  3. Nếu chưa đăng nhập: Lưu URL hiện tại vào session (`REDIRECT_AFTER_LOGIN`) và chuyển hướng về `/login.jsp`.
  4. Nếu đã đăng nhập: `AuthorizationFilter` kiểm tra trường `role` của user có nằm trong whitelist quyền của endpoint không.
  5. Nếu quyền hợp lệ: Cho phép request đi tiếp (`chain.doFilter(request, response)`).
* **Exception Flows:**
  - Nếu `role` không phù hợp (ví dụ `STUDENT` cố tình truy cập URL `/admin/users`): Chặn request, chuyển hướng đến `/access-denied.jsp` với mã trạng thái HTTP 403.
* **Acceptance Criteria:**
  - **[AC-AUTH-03]** Sinh viên đăng nhập vào hệ thống khi cố tình gõ trực tiếp URL `/instructor/*` phải nhận mã HTTP 403 Forbidden.

---

## MOD-02: Quản lý Khóa học & Bài tập (Course & Assignment Management)

### Feature FE-02.1: Quản lý Khóa học (Course Management)
* **Purpose:** Cho phép Giảng viên và Admin tạo lập, cấu hình lớp học và gán quyền giảng dạy môn học.
* **User/Actor:** `INSTRUCTOR`, `ADMIN`.
* **Preconditions:** Giảng viên đã đăng nhập thành công.
* **Main Flow:**
  1. Giảng viên mở giao diện Quản lý Khóa học (`/instructor/courses`).
  2. Bấm "Thêm Khóa Học Mới", điền: Mã môn (`course_code`), Tên môn (`course_name`), Học kỳ (`semester`).
  3. Hệ thống kiểm tra trùng lặp `course_code`.
  4. Ghi bản ghi vào bảng `Courses` với `instructor_id = CURRENT_USER.user_id`.
  5. Cập nhật danh sách khóa học và hiển thị thông báo thành công.
* **Business Rules:**
  - `BR-COURSE-01`: Mỗi `course_code` là duy nhất trong toàn hệ thống.
  - `BR-COURSE-02`: Chỉ giảng viên tạo ra khóa học hoặc Admin mới có quyền sửa/xóa khóa học đó.
* **Acceptance Criteria:**
  - **[AC-CRS-01]** Tạo khóa học với mã môn đã tồn tại phải báo lỗi `Mã môn học đã tồn tại trong hệ thống`.

### Feature FE-02.2: Thiết lập Bài tập & Ngưỡng Tương đồng (Assignment Configuration)
* **Purpose:** Giảng viên tạo bài tập thực hành, quy định thời hạn và thiết lập ngưỡng báo động đỏ cho tỷ lệ đạo văn.
* **User/Actor:** `INSTRUCTOR`.
* **Preconditions:** Khóa học đã được tạo trước đó.
* **Main Flow:**
  1. Giảng viên chọn Khóa học, nhấn "Tạo Bài Tập Mới".
  2. Nhập các trường: Tiêu đề (`title`), Mô tả (`description`), Điểm tối đa (`max_score`), Hạn nộp (`deadline`), Ngưỡng tương đồng (`similarity_threshold`).
  3. Hệ thống validate dữ liệu: `deadline` phải lớn hơn thời điểm hiện tại, `similarity_threshold` nằm trong khoảng $[0.0, 100.0]$ (mặc định khuyến nghị 75.0%).
  4. Lưu dữ liệu vào bảng `Assignments`.
* **Acceptance Criteria:**
  - **[AC-ASN-01]** Hệ thống từ chối lưu bài tập nếu hạn nộp ở quá khứ hoặc `similarity_threshold` < 0 hoặc > 100.

---

## MOD-03: Tiếp nhận & Kiểm định Bài nộp (Submission & Artifact Pipeline)

### Feature FE-03.1: Tiếp nhận Bài nộp Đa định dạng (File Ingestion)
* **Purpose:** Cho phép sinh viên nộp bài làm cá nhân dưới nhiều định dạng (`.java`, `.txt`, `.docx`, `.zip`), đảm bảo an toàn tệp tin.
* **User/Actor:** `STUDENT`.
* **Preconditions:** Sinh viên đã đăng nhập, bài tập còn trong thời hạn nộp (`GETDATE() <= deadline`).
* **Main Flow:**
  1. Sinh viên truy cập trang chi tiết bài tập (`/student/assignment?id=...`).
  2. Kéo thả hoặc chọn file từ máy tính.
  3. Nhấn "Nộp bài".
  4. `SubmissionServlet` tiếp nhận request `multipart/form-data`.
  5. Hệ thống xác thực phần mở rộng tệp và Content-Type.
  6. Lưu trữ tệp vào thư mục an toàn.
  7. Kích hoạt tự động tính mã băm SHA-256 (FE-03.2).
  8. Ghi bản ghi vào bảng `Submissions` với trạng thái `PENDING`.
* **Validation Rules:**
  - Dung lượng file tối đa: 25MB (`maxFileSize = 26,214,400 bytes`).
  - Whitelist định dạng: `java`, `txt`, `docx`, `zip`. Từ chối mọi file thực thi (`.exe`, `.sh`, `.bat`, `.jar`).
* **Acceptance Criteria:**
  - **[AC-SUB-01]** Tải lên file quá 25MB hoặc không thuộc whitelist đuôi file phải lập tức bị từ chối kèm mã thông báo cụ thể.

### Feature FE-03.2: Xác thực Toàn vẹn Mã nguồn SHA-256 (Checksum Engine)
* **Purpose:** Đảm bảo tính toàn vẹn (Integrity) và chống chối bỏ (Non-repudiation) của bài nộp sinh viên theo chuẩn mục 4.4.2 của đề cương RBL.
* **User/Actor:** Hệ thống (`SubmissionProcessor`).
* **Preconditions:** File bài nộp đã được upload thành công lên bộ đệm stream.
* **Main Flow:**
  1. Đọc luồng byte của file thông qua `MessageDigest.getInstance("SHA-256")`.
  2. Chuyển đổi mảng byte sang chuỗi Hexadecimal gồm 64 ký tự.
  3. Ghi chuỗi mã băm này vào cột `Submissions.sha256_hash`.
  4. Hiển thị mã SHA-256 trên màn hình biên nhận của sinh viên làm bằng chứng nộp bài hợp lệ.
* **Acceptance Criteria:**
  - **[AC-SUB-02]** Mọi bản ghi bài nộp trong bảng `Submissions` bắt buộc có giá trị `sha256_hash` gồm đúng 64 ký tự hex hợp lệ.

### Feature FE-03.3: Bóc tách & Xử lý Hàng loạt (Batch ZIP Processing)
* **Purpose:** Giúp Giảng viên tải lên file nén chứa hàng chục bài làm của cả lớp và hệ thống tự động bóc tách vào danh mục nộp bài.
* **User/Actor:** `INSTRUCTOR`.
* **Preconditions:** Giảng viên có quyền trên bài tập tương ứng.
* **Main Flow:**
  1. Giảng viên upload file `.zip` chứa bài nộp của lớp.
  2. Hệ thống kiểm tra tính an toàn của từng entry trong ZIP (bảo vệ chống tấn công `Zip Slip`: kiểm tra đường dẫn giải nén `canonicalPath.startsWith(destinationDir)`).
  3. Bóc tách từng thư mục con tương ứng với mã sinh viên (`student_code` hoặc `username`).
  4. Ánh xạ với tài khoản sinh viên trong bảng `Users`, tự động tạo bản ghi `Submissions`.
  5. Đánh dấu trạng thái `PARSED` sẵn sàng cho lõi đối soát.
* **Acceptance Criteria:**
  - **[AC-SUB-03]** Tải lên file ZIP chứa 50 bài nộp sinh viên: Hệ thống phải giải nén an toàn và tạo đúng 50 bản ghi `Submissions` trong CSDL trong vòng dưới 10 giây.

---

## MOD-04: Lõi Đối soát Trùng lặp (Similarity & Plagiarism Engine)

### Feature FE-04.1: Phân tích Mã nguồn Java (Java Lexical Normalization)
* **Purpose:** Loại bỏ các yếu tố gây nhiễu bề mặt (comment, khoảng trắng, tên biến ngẫu nhiên) để so sánh cấu trúc logic thực sự của mã nguồn Java.
* **User/Actor:** Hệ thống (`JavaSimilarityEngine`).
* **Preconditions:** File mã nguồn Java có trạng thái `PARSED`.
* **Main Flow:**
  1. **Lexical Strip:** Dùng biểu thức chính quy và Lexer loại bỏ toàn bộ comment dòng đơn `//...` và comment khối `/* ... */`, chuẩn hóa khoảng trắng.
  2. **Identifier Normalization:**
     - Phân loại các từ khóa dành riêng của ngôn ngữ Java (`public`, `class`, `void`, `static`, `int`, `for`, `while`, `if`, `return`...). Giữ nguyên 100%.
     - Nhận diện các định danh do người dùng tự đặt (Tên biến, tên tham số, tên hàm nội bộ): Thay thế tuần tự thành `$ID_1`, `$ID_2`, `$ID_3`... theo thứ tự xuất hiện.
  3. **Token N-Gram Extraction:**
     - Tạo chuỗi token stream chuẩn hóa.
     - Sinh tập hợp các n-gram từ vựng liên tiếp (kích thước $k = 3$ hoặc $k = 4$).
  4. **Similarity Metric Calculation:**
     - Tính toán chỉ số **Jaccard Similarity Index**:
       $$J(A, B) = \frac{|Tokens_A \cap Tokens_B|}{|Tokens_A \cup Tokens_B|} \times 100\%$$
     - Tính khoảng cách **Normalized Levenshtein Distance** trên chuỗi token:
       $$Sim_{Lev}(A, B) = \left(1 - \frac{Lev(A, B)}{\max(|A|, |B|)}\right) \times 100\%$$
     - Điểm tổng hợp tương đồng mã nguồn:
       $$Score_{Java} = 0.6 \times J(A, B) + 0.4 \times Sim_{Lev}(A, B)$$
* **Acceptance Criteria:**
  - **[AC-SIM-01]** Hai đoạn mã Java có cùng logic thuật toán nhưng sinh viên B đổi tên tất cả các biến và chèn thêm comment giả: Hệ thống phải phát hiện độ tương đồng $\ge 85\%$.

### Feature FE-04.2: Phân tích Bài luận Văn bản (Text NLP Engine)
* **Purpose:** Đo lường mức độ tương đồng giữa các bài luận, tài liệu báo cáo tiếng Anh (`.txt`, `.docx`).
* **User/Actor:** Hệ thống (`TextSimilarityEngine`).
* **Preconditions:** File văn bản được trích xuất thành chuỗi ký tự UTF-8.
* **Main Flow:**
  1. **Text Preprocessing:** Chuyển toàn bộ về chữ thường, tách từ (Tokenization), loại bỏ dấu câu.
  2. **Stopwords Filtering:** Loại bỏ hơn 150 từ dừng thông dụng tiếng Anh (*the, is, at, which, on, for, a, an...*).
  3. **TF-IDF Vectorization:**
     - Tính Term Frequency ($TF(t, d)$) của từng từ trong văn bản.
     - Tính Inverse Document Frequency ($IDF(t, D)$) dựa trên toàn bộ kho tài liệu của Assignment:
       $$IDF(t, D) = \ln\left(\frac{1 + |D|}{1 + |\{d \in D : t \in d\}|}\right) + 1$$
     - Sinh vector đặc trưng đa chiều $\vec{V}_d$.
  4. **Cosine Similarity:**
     - Tính góc cosine giữa hai vector tài liệu $\vec{A}$ và $\vec{B}$:
       $$Cosine(A, B) = \frac{\vec{A} \cdot \vec{B}}{\|\vec{A}\| \|\vec{B}\|} \times 100\%$$
* **Acceptance Criteria:**
  - **[AC-SIM-02]** Hai bài viết tiếng Anh có cấu trúc câu và từ vựng giống nhau $\ge 80\%$ (sau khi trừ stopwords) phải được tính ra điểm Cosine Similarity $\ge 75\%$.

### Feature FE-04.3: Quét Ma trận Đối soát Chéo (NxN Pairwise Comparison)
* **Purpose:** So khớp toàn diện tất cả các bài nộp trong cùng một Assignment để phát hiện mọi mạng lưới liên kết sao chép.
* **User/Actor:** `INSTRUCTOR`.
* **Preconditions:** Có tối thiểu 2 bài nộp trong Assignment.
* **Main Flow:**
  1. Giảng viên bấm nút "Khởi chạy Đối Soát Toàn Lớp" (`/instructor/run-plagiarism-scan`).
  2. Hệ thống thu thập $N$ bài nộp hợp lệ.
  3. Thực hiện vòng lặp so khớp tổ hợp $C(N, 2) = \frac{N(N-1)}{2}$ cặp bài nộp $(Submission_A, Submission_B)$.
  4. Lưu kết quả tương đồng vào bảng `PlagiarismReports`.
  5. Tự động gán nhãn `risk_level`:
     - `similarity_score < 30.0%`: `SAFE`
     - `30.0% <= similarity_score < 50.0%`: `LOW`
     - `50.0% <= similarity_score < similarity_threshold`: `MEDIUM`
     - `similarity_score >= similarity_threshold`: `HIGH_RISK`
  6. Cập nhật trạng thái của các bài nộp có vi phạm nghiêm trọng thành `FLAGGED`.
* **Acceptance Criteria:**
  - **[AC-SIM-03]** Với 30 bài nộp trong 1 lớp (435 phép so khớp cặp), hệ thống Java thuần phải hoàn thành ma trận trong vòng dưới 3 giây.

### Feature FE-04.4: Bóc tách Khối mã Trùng khớp (Matching Blocks Extraction)
* **Purpose:** Xác định chính xác vị trí dòng mã bắt đầu và kết thúc giữa hai bài nộp vi phạm để làm bằng chứng đối chiếu.
* **User/Actor:** Hệ thống (`MatchingBlocksService`).
* **Preconditions:** Cặp bài nộp có `similarity_score >= 50.0%`.
* **Main Flow:**
  1. Áp dụng thuật toán tìm chuỗi con chung dài nhất (Longest Common Subsequence - LCS) trên tập dòng mã đã chuẩn hóa.
  2. Gom nhóm các dòng trùng lặp liên tiếp thành từng khối vi phạm (`Block`).
  3. Ghi nhận số dòng của sinh viên A (`student_a_start_line`, `student_a_end_line`) và sinh viên B (`student_b_start_line`, `student_b_end_line`).
  4. Trích xuất đoạn mã gốc và lưu vào bảng `MatchingBlocks`.
* **Acceptance Criteria:**
  - **[AC-SIM-04]** Mọi bản ghi `MatchingBlocks` phải có thông số dòng $> 0$ và `end_line >= start_line`.

---

## MOD-05: Trí tuệ Nhân tạo & Nhận diện LLM (AI Intelligence & LLM Detection)

### Feature FE-05.1: Phân tích Ngữ nghĩa & Thủ thuật Tránh né qua Gemini API
* **Purpose:** Đóng vai trò trợ lý chuyên gia, phân tích sâu lý do vì sao hai bài nộp bị trùng lặp cấu trúc dù sinh viên đã dùng các thủ thuật che giấu.
* **User/Actor:** Hệ thống (`GeminiAnalysisService`).
* **Preconditions:** Bản ghi `PlagiarismReports` có `risk_level = 'HIGH_RISK'` và có cấu hình `GEMINI_API_KEY`.
* **Main Flow:**
  1. Hệ thống lấy ra các đoạn code vi phạm từ `MatchingBlocks` của cặp bài A và B.
  2. Tạo cấu trúc Prompt kỹ thuật gửi đến Google Gemini API endpoint (`models/gemini-2.5-flash:generateContent`).
  3. Phân tích kết quả JSON trả về, lưu trữ nội dung vào trường `PlagiarismReports.ai_analysis_summary`.
* **Exception Flows:**
  - *Gemini API Timeout / Rate Limit:* Bắt ngoại lệ `SocketTimeoutException` hoặc HTTP 429, kích hoạt retry 1 lần sau 2 giây. Nếu vẫn lỗi, lưu thông báo fallback: *"Phân tích thuật toán cục bộ xác nhận tương đồng cao; Tóm tắt AI tạm thời quá tải."*, không làm sập tiến trình hệ thống.
* **Acceptance Criteria:**
  - **[AC-AI-01]** Bản ghi đạo văn nguy cơ cao (`HIGH_RISK`) phải hiển thị nhận định bằng tiếng Việt có dấu mạch lạc từ Gemini API trong báo cáo của Giảng viên.

### Feature FE-05.2: Nhận diện Dấu vết Nội dung do AI sinh (AI Heuristics)
* **Purpose:** Cảnh báo các đoạn mã hoặc bài luận có dấu hiệu được sinh tự động 100% từ các mô hình LLM.
* **User/Actor:** Hệ thống (`LLMDetectorService`).
* **Preconditions:** File văn bản hoặc mã nguồn đã được token hóa.
* **Main Flow:**
  1. Phân tích phân phối độ dài câu/hàm (Burstiness Index).
  2. Quét các mẫu hình rập khuôn đặc trưng của mã AI.
  3. Tính toán chỉ số nghi vấn `ai_generated_probability` ($0 - 100\%$).
  4. Nếu chỉ số $> 70\%$, gắn cờ cảnh báo `[AI-Generated Suspicion]` lên bài nộp.
* **Acceptance Criteria:**
  - **[AC-AI-02]** Phát hiện chính xác ít nhất 80% các bài luận tiếng Anh được tạo nguyên văn từ ChatGPT với cờ cảnh báo văn bản máy sinh.

---

## MOD-06: Báo cáo & Giám định Trực quan (Reporting & Diff Inspection)

### Feature FE-06.1: Dashboard Tổng quan & Ma trận Tương đồng Heatmap
* **Purpose:** Cung cấp cái nhìn toàn cảnh trực quan về tình hình liêm chính của cả lớp học.
* **User/Actor:** `INSTRUCTOR`, `ADMIN`.
* **Preconditions:** Đã hoàn thành quá trình quét đạo văn cho Assignment.
* **Main Flow:**
  1. Giảng viên truy cập trang `/instructor/plagiarism-report?assignment_id=...`.
  2. Hệ thống tải dữ liệu ma trận từ `PlagiarismReports`.
  3. Hiển thị bảng Heatmap $N \times N$:
     - Các ô có điểm tương đồng $\ge 75\%$ được tô màu đỏ rực rỡ kèm số % nổi bật.
     - Các ô từ $50\% - 74\%$ được tô màu vàng cam.
     - Các ô an toàn được tô màu xanh ngọc.
  4. Cho phép lọc nhanh danh sách các cặp bài nộp có nguy cơ cao nhất.
* **Acceptance Criteria:**
  - **[AC-REP-01]** Màn hình Heatmap hiển thị đầy đủ tên sinh viên, điểm tương đồng trên từng ô và click vào ô lập tức chuyển sang màn hình Side-by-Side Diff.

### Feature FE-06.2: So sánh Trực quan Song song (Side-by-Side Code Diff Viewer)
* **Purpose:** Giúp giảng viên đối chứng tận mắt từng dòng code giữa 2 sinh viên nghi vấn vi phạm để đưa ra phán quyết kỷ luật chính xác.
* **User/Actor:** `INSTRUCTOR`.
* **Preconditions:** Chọn 1 bản ghi từ danh sách `PlagiarismReports`.
* **Main Flow:**
  1. Mở màn hình `/instructor/diff-viewer?report_id=...`.
  2. Giao diện chia làm 2 cột: Cột trái (Bài của Sinh viên A), Cột phải (Bài của Sinh viên B).
  3. Hệ thống highlight màu vàng/đỏ các dòng mã tương ứng được định nghĩa trong `MatchingBlocks`.
  4. Thanh cuộn của 2 cột được đồng bộ (Synchronized Scroll).
  5. Khi click vào khối mã bị tô màu, một popover hiển thị ghi chú bóc tách của AI.
* **Acceptance Criteria:**
  - **[AC-REP-02]** Màn hình Diff Viewer hiển thị đúng số dòng của cả 2 sinh viên, tô màu chuẩn xác các đoạn mã trùng khớp và đồng bộ cuộn trang mượt mà.

### Feature FE-06.3: Xuất Báo cáo Thẩm định Học thuật (Audit Report Export)
* **Purpose:** Xuất biên bản thẩm định chính thức kèm đầy đủ bằng chứng đối chiếu để giảng viên lưu trữ hoặc gửi lên Hội đồng Kỷ luật.
* **User/Actor:** `INSTRUCTOR`.
* **Preconditions:** Báo cáo đối soát đã hoàn tất.
* **Main Flow:**
  1. Nhấn nút "Xuất Biên Bản Thẩm Định" (`Export PDF/CSV`).
  2. Hệ thống tổng hợp: Thông tin môn học, mã bài tập, thời gian quét, danh sách các cặp vi phạm, đoạn mã đối chứng và nhận định của Gemini AI.
  3. Xuất file có chữ ký số/mã băm xác thực của hệ thống.
* **Acceptance Criteria:**
  - **[AC-REP-03]** File xuất ra chứa đầy đủ tên sinh viên, mã SHA-256 của 2 bài nộp, tỷ lệ trùng lặp và đoạn mã chứng minh vi phạm.

---

## MOD-07: Trình diễn 3D & Giao diện Hiện đại (Presentation & 3D Layer)

### Feature FE-07.1: Landing Page 3D Scrollytelling (Three.js WebGL & GSAP)
* **Purpose:** Thể hiện đẳng cấp công nghệ, trực quan hóa sứ mệnh bảo vệ liêm chính học thuật qua mô hình 3D Cyber Shield.
* **User/Actor:** Tất cả người dùng truy cập trang chủ (`/` hoặc `/index.html`).
* **Main Flow:**
  1. Người dùng mở trang web.
  2. Three.js khởi tạo WebGL Canvas, nạp mô hình 3D `cyber-shield.glb` từ thư mục `/web/assets/models/`.
  3. Thiết lập hệ thống ánh sáng (AmbientLight, DirectionalLight màu xanh Neon/Cyberpunk).
  4. Khi người dùng cuộn chuột, GSAP ScrollTrigger bắt sự kiện cuộn và điều khiển camera 3D xoay mượt mà quanh chiếc khiên.
* **Acceptance Criteria:**
  - **[AC-3D-01]** Mô hình 3D render ổn định ở mức tối thiểu 55-60 FPS trên trình duyệt Chrome/Edge có hỗ trợ WebGL.

### Feature FE-07.2: Giao diện Quản trị Phong cách Liquid Glass (UI/UX 2026)
* **Purpose:** Đạt điểm tuyệt đối tiêu chí UI/UX (10/10 điểm Rubric), mang lại trải nghiệm phần mềm cao cấp, hiện đại, mượt mà.
* **Main Flow:**
  1. Áp dụng hiệu ứng nền mờ gương kính (Liquid Glass Material - `backdrop-filter: blur(16px)`).
  2. Bố cục Responsive chuẩn mực hiển thị sắc nét trên cả màn hình Desktop và Tablet.
* **Acceptance Criteria:**
  - **[AC-UI-01]** Không có hiện tượng vỡ layout, tràn ngang màn hình trên các độ phân giải phổ biến từ 1366x768 đến 1920x1080.

---

# 4. YÊU CẦU PHI CHỨC NĂNG & MẶT CẮT HỆ THỐNG (CROSS-CUTTING REQUIREMENTS)

### 4.1. Bảo mật (Security - NFR-SEC)
* **[NFR-SEC-01] Phòng chống SQL Injection:** Toàn bộ truy vấn CSDL trong tầng DAO bắt buộc 100% sử dụng `PreparedStatement` với tham số đại diện `?`. Tuyệt đối cấm nối chuỗi SQL.
* **[NFR-SEC-02] Phòng chống XSS (Cross-Site Scripting):** Tất cả dữ liệu đầu vào hiển thị lên JSP phải được escape thông qua thẻ JSTL `<c:out value="${...}"/>` hoặc hàm mã hóa HTML.
* **[NFR-SEC-03] Phòng chống Zip Slip & Path Traversal:** Khi giải nén file ZIP bài nộp hàng loạt, hệ thống phải chuẩn hóa đường dẫn đích bằng `File.getCanonicalPath()` và kiểm tra tiền tố thư mục cho phép trước khi ghi ra đĩa.
* **[NFR-SEC-04] Mã hóa Mật khẩu:** Mật khẩu người dùng được băm qua thuật toán một chiều kết hợp salt trước khi lưu vào CSDL.

### 4.2. Hiệu năng & Tải (Performance - NFR-PERF)
* **[NFR-PERF-01] Tốc độ phản hồi Web:** Thời gian phản hồi trung bình (Response Time) cho các tác vụ CRUD thông thường phải $\le 500$ms trên Apache Tomcat 10.1.
* **[NFR-PERF-02] Tốc độ Lõi Đối soát:** Thuật toán tính toán ma trận tương đồng cục bộ cho lớp học 50 sinh viên ($C(50, 2) = 1,225$ phép so sánh) phải hoàn thành trong $\le 5$ giây.
* **[NFR-PERF-03] Quản lý Kết nối CSDL:** Sử dụng Connection Pooling (HikariCP hoặc Tomcat JDBC Pool) với cấu hình tối thiểu 10 kết nối thường trực để ngăn chặn nghẽn CSDL.

### 4.3. Tính Tin cậy & Khả năng Phục hồi (Reliability & Resilience - NFR-REL)
* **[NFR-REL-01] Tính toàn vẹn giao dịch (ACID):** Mọi thao tác ghi dữ liệu nhiều bước phải được bọc trong Database Transaction (`connection.setAutoCommit(false)`). Nếu có lỗi, Rollback nguyên trạng.
* **[NFR-REL-02] Độc lập với Lỗi Ngoại vi:** Nếu kết nối Google Gemini API bị gián đoạn, hệ thống vẫn lưu trữ đầy đủ điểm số tương đồng của lõi Java, hiển thị trạng thái cảnh báo thay vì làm sập ứng dụng.

### 4.4. Khả năng Mở rộng & Tương thích (Scalability & Compatibility - NFR-COMPAT)
* **[NFR-COMPAT-01] Môi trường Thực thi:** Tương thích 100% với Java 17 LTS và Java 21 LTS; Apache Tomcat 10.1+; Microsoft SQL Server 2019 trở lên (hoặc MySQL 8.0+).
* **[NFR-COMPAT-02] Mã hóa Ký tự (UTF-8):** Toàn bộ bộ lọc Filter, kết nối CSDL và trang JSP bắt buộc cấu hình `UTF-8` không có BOM để hiển thị tiếng Việt có dấu chuẩn xác 100%.

### 4.5. Ghi vết & Giám sát (Logging & Audit Trail - NFR-LOG)
* **[NFR-LOG-01] Nhật ký Hệ thống:** Ghi nhận cấu trúc log theo 4 mức độ: `DEBUG`, `INFO`, `WARN`, `ERROR` qua chuẩn Logger.
* **[NFR-LOG-02] Vết kiểm toán Nộp bài:** Mọi hành vi nộp bài và quét đạo văn đều được lưu log kèm Timestamp, User ID và địa chỉ IP của Client.

---

# 5. CẤU TRÚC PHÂN RÃ CÔNG VIỆC (WORK BREAKDOWN STRUCTURE - WBS)

Bảng phân rã công việc chi tiết thành 16 gói công việc (Work Packages) độc lập, ánh xạ trực tiếp tới 5 thành viên nhóm phát triển theo chuyên môn:

| Gói Công Việc (Work Package ID) | Tiêu Đề & Mục Tiêu | Phạm Vi & Sản Phẩm Bàn Giao (Deliverables) | Người Chịu Trách Nhiệm (Role/Owner) | Tiền Đề Phụ Thuộc (Dependencies) | Điều Kiện Hoàn Thành & Tiêu Chí Chấp Nhận |
| :--- | :--- | :--- | :--- | :--- | :--- |
| **WP-01** | **Khởi tạo CSDL & Tầng Kết nối DBContext** | - Script SQL Server chuẩn 3NF (6 bảng).<br>- Lớp `DBContext.java` sử dụng Connection Pooling.<br>- Dữ liệu mẫu chuẩn rubric. | **Nguyễn Tiến** *(Database Engineer)* | Không | Chạy thành công script SQL không lỗi, `DBContextTest` kết nối CSDL thành công. |
| **WP-02** | **Xây dựng Mô hình Dữ liệu (Entities/Models)** | - Bộ 6 Java Beans: `User`, `Course`, `Assignment`, `Submission`, `PlagiarismReport`, `MatchingBlock`. | **Nguyễn Tiến** *(Database Engineer)* | WP-01 | Đầy đủ thuộc tính, constructors, getters/setters, tuân thủ chuẩn JavaBeans PRJ301. |
| **WP-03** | **Tầng Truy xuất Dữ liệu (DAOs Layer)** | - `UserDAO.java`, `CourseDAO.java`, `AssignmentDAO.java`, `SubmissionDAO.java`, `ReportDAO.java`. | **Đinh Vũ Phương Khánh** *(Backend Dev)* | WP-02 | 100% câu truy vấn dùng `PreparedStatement`, phủ Unit Test CRUD cơ bản. |
| **WP-04** | **Bảo mật Xác thực & Bộ lọc Phân quyền RBAC** | - `LoginServlet.java`, `LogoutServlet.java`.<br>- `AuthenticationFilter.java`, `AuthorizationFilter.java`.<br>- `PasswordUtil.java` (băm mật khẩu). | **Đinh Vũ Phương Khánh** *(Backend Dev)* | WP-03 | Đăng nhập đúng phân quyền; sinh viên không thể truy cập URL giảng viên (AC-AUTH-01, AC-AUTH-03). |
| **WP-05** | **Giao diện 3D Scrollytelling Landing Page** | - `index.html`, Three.js Canvas, GSAP ScrollTrigger.<br>- Nạp model 3D `cyber-shield.glb` mượt mà. | **Trần Văn Phúc** *(Frontend 3D Dev)* | Không | Render mô hình 3D chuyển động theo cuộn trang $\ge 55$ FPS (AC-3D-01). |
| **WP-06** | **Hệ thống Giao diện Quản trị Liquid Glass UI** | - Master layout CSS Liquid Glass.<br>- Dashboard cho Sinh viên và Giảng viên (`.jsp`). | **Trần Văn Phúc** *(Frontend 3D Dev)* | WP-04 | Giao diện hiển thị sắc nét, responsive, không tràn ngang màn hình (AC-UI-01). |
| **WP-07** | **Module Quản lý Khóa học & Bài tập** | - `CourseServlet.java`, `AssignmentServlet.java`.<br>- Form tạo bài tập và thiết lập `similarity_threshold`. | **Đinh Vũ Phương Khánh** *(Backend Dev)* | WP-03, WP-06 | Giảng viên tạo được bài tập và lưu vào CSDL chuẩn xác (AC-CRS-01, AC-ASN-01). |
| **WP-08** | **Pipeline Tiếp nhận & Kiểm tra SHA-256 Bài nộp** | - `SubmissionServlet.java`.<br>- Xử lý upload đa định dạng (`.java`, `.txt`, `.docx`).<br>- Tính toán và lưu mã băm `SHA-256`. | **Nguyễn Trần Anh Kiệt** *(Lead Architect)* | WP-03, WP-07 | Upload file sinh mã SHA-256 đúng 64 ký tự, lưu file an toàn ngoài Web Root (AC-SUB-01, AC-SUB-02). |
| **WP-09** | **Module Giải nén & Tiếp nhận Hàng loạt (Batch ZIP)** | - `BatchUploadServlet.java`, `ZipExtractionService.java`.<br>- Cơ chế bảo vệ chống lỗ hổng Zip Slip. | **Nguyễn Trần Anh Kiệt** *(Lead Architect)* | WP-08 | Giải nén gói bài cả lớp an toàn và tạo danh sách Submissions trong CSDL (AC-SUB-03). |
| **WP-10** | **Lõi Phân tích Mã nguồn Java (Token & Lexer)** | - `JavaLexerNormalizer.java`.<br>- Bộ lọc bỏ comment, chuẩn hóa định danh biến/hàm.<br>- Trích xuất N-Gram. | **Nguyễn Hoài Nhi** *(Algorithm/NLP Dev)* | Không | Chuyển đổi mã nguồn về dạng token chuẩn hóa; biến đổi biến tự đặt thành `$ID_n`. |
| **WP-11** | **Thuật toán Đo lường Tương đồng Mã nguồn** | - `CodeSimilarityCalculator.java`.<br>- Cài đặt giải thuật Jaccard Index & Levenshtein Distance. | **Nguyễn Hoài Nhi** *(Algorithm/NLP Dev)* | WP-10 | Phát hiện chính xác độ tương đồng $\ge 85\%$ khi bị đổi tên biến/thêm comment (AC-SIM-01). |
| **WP-12** | **Lõi Phân tích Bài luận Tiếng Anh (TF-IDF & Cosine)** | - `TextPreprocessingService.java`, `TfIdfVectorService.java`.<br>- Bộ lọc Stop-words và Cosine Similarity. | **Nguyễn Hoài Nhi** *(Algorithm/NLP Dev)* | Không | Tính toán chính xác độ tương đồng bài luận tiếng Anh qua không gian vector (AC-SIM-02). |
| **WP-13** | **Quét Ma trận Đối soát Toàn lớp (NxN Matrix & LCS)** | - `PlagiarismScannerService.java`.<br>- Phân loại mức độ rủi ro (SAFE, LOW, MEDIUM, HIGH_RISK).<br>- Thuật toán bóc tách dòng vi phạm `MatchingBlocks`. | **Nguyễn Trần Anh Kiệt** *(Lead Architect)* | WP-11, WP-12 | Quét 30 bài nộp trong dưới 3 giây, lưu bản ghi vào `PlagiarismReports` & `MatchingBlocks` (AC-SIM-03, AC-SIM-04). |
| **WP-14** | **Tích hợp Trợ lý Thẩm định AI (Google Gemini API)** | - `GeminiApiService.java`.<br>- Xây dựng prompt phân tích thủ thuật đảo lệnh, đổi tên biến.<br>- Fallback xử lý khi timeout hoặc quá tải API. | **Nguyễn Trần Anh Kiệt** *(Lead Architect)* | WP-13 | Tự động sinh bản tóm tắt nhận định ngữ nghĩa bằng tiếng Việt có dấu lưu vào CSDL (AC-AI-01). |
| **WP-15** | **Giao diện Ma trận Heatmap & Side-by-Side Diff Viewer** | - `heatmap_dashboard.jsp`, `diff_viewer.jsp`.<br>- Hiển thị bảng ma trận màu sắc, đồng bộ thanh cuộn 2 cột mã nguồn và highlight dòng vi phạm. | **Trần Văn Phúc** *(Frontend 3D Dev)* | WP-06, WP-13 | Hiển thị ma trận trực quan, giao diện đối chứng 2 cột khớp từng dòng lệnh (AC-REP-01, AC-REP-02). |
| **WP-16** | **Module Xuất Báo cáo Thẩm định & Kiểm thử Toàn diện** | - `ExportReportServlet.java` (PDF/CSV).<br>- Kịch bản kiểm thử tích hợp tự động JUnit 5.<br>- File `CHAY_ALL.bat` khởi chạy 1-click đa máy. | **Nguyễn Tiến** *(QA & System Engineer)* | WP-01 đến WP-15 | Xuất biên bản thẩm định chuẩn xác; toàn bộ hệ thống khởi chạy tự động 1-click không lỗi (AC-REP-03). |

---

# 6. MA TRẬN TRUY XUẤT NGUỒN GỐC (TRACEABILITY MATRIX)

Ma trận này đảm bảo sự liên kết xuyên suốt $100\%$ từ Yêu cầu nghiệp vụ $\rightarrow$ Module $\rightarrow$ Feature $\rightarrow$ Chi tiết Đặc tả $\rightarrow$ Gói công việc WBS $\rightarrow$ Tiêu chí chấp nhận (Acceptance Criteria):

| Mã Yêu Cầu (Req ID) | Module ID | Feature ID | Gói WBS (Work Package) | Lớp / File Thực Thi Chính | Tiêu Chí Chấp Nhận (Acceptance Criteria) |
| :--- | :--- | :--- | :--- | :--- | :--- |
| **REQ-AUTH-01** | `MOD-01` | `FE-01.1` | **WP-04** | `LoginServlet.java`, `UserDAO.java` | `[AC-AUTH-01]`, `[AC-AUTH-02]` |
| **REQ-AUTH-02** | `MOD-01` | `FE-01.2` | **WP-04** | `AuthenticationFilter.java`, `AuthorizationFilter.java` | `[AC-AUTH-03]` |
| **REQ-CRS-01** | `MOD-02` | `FE-02.1` | **WP-07** | `CourseServlet.java`, `CourseDAO.java` | `[AC-CRS-01]` |
| **REQ-ASN-01** | `MOD-02` | `FE-02.2` | **WP-07** | `AssignmentServlet.java`, `AssignmentDAO.java` | `[AC-ASN-01]` |
| **REQ-SUB-01** | `MOD-03` | `FE-03.1` | **WP-08** | `SubmissionServlet.java`, `SubmissionDAO.java` | `[AC-SUB-01]` |
| **REQ-SUB-02** | `MOD-03` | `FE-03.2` | **WP-08** | `SHA256ChecksumUtil.java` | `[AC-SUB-02]` |
| **REQ-SUB-03** | `MOD-03` | `FE-03.3` | **WP-09** | `ZipExtractionService.java`, `BatchUploadServlet.java` | `[AC-SUB-03]` |
| **REQ-SIM-01** | `MOD-04` | `FE-04.1` | **WP-10, WP-11** | `JavaLexerNormalizer.java`, `CodeSimilarityCalculator.java` | `[AC-SIM-01]` |
| **REQ-SIM-02** | `MOD-04` | `FE-04.2` | **WP-12** | `TextPreprocessingService.java`, `TfIdfVectorService.java` | `[AC-SIM-02]` |
| **REQ-SIM-03** | `MOD-04` | `FE-04.3` | **WP-13** | `PlagiarismScannerService.java`, `ReportDAO.java` | `[AC-SIM-03]` |
| **REQ-SIM-04** | `MOD-04` | `FE-04.4` | **WP-13** | `MatchingBlocksService.java` | `[AC-SIM-04]` |
| **REQ-AI-01** | `MOD-05` | `FE-05.1` | **WP-14** | `GeminiApiService.java` | `[AC-AI-01]` |
| **REQ-AI-02** | `MOD-05` | `FE-05.2` | **WP-14** | `LLMDetectorService.java` | `[AC-AI-02]` |
| **REQ-REP-01** | `MOD-06` | `FE-06.1` | **WP-15** | `heatmap_dashboard.jsp`, `ReportServlet.java` | `[AC-REP-01]` |
| **REQ-REP-02** | `MOD-06` | `FE-06.2` | **WP-15** | `diff_viewer.jsp` | `[AC-REP-02]` |
| **REQ-REP-03** | `MOD-06` | `FE-06.3` | **WP-16** | `ExportReportServlet.java` | `[AC-REP-03]` |
| **REQ-UI-01** | `MOD-07` | `FE-07.1` | **WP-05** | `index.html`, `cyber-shield.glb`, `three_controller.js` | `[AC-3D-01]` |
| **REQ-UI-02** | `MOD-07` | `FE-07.2` | **WP-06** | `liquid_glass.css`, master JSP pages | `[AC-UI-01]` |

---
*Tài liệu Đặc tả SRS và Ma trận WBS này được thiết kế và cố định (Frozen Spec) bởi Tech Lead Antigravity làm nền tảng kỹ thuật bắt buộc cho toàn bộ quá trình lập trình, kiểm thử và nghiệm thu dự án.*

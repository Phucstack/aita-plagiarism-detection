# TÀI LIỆU ĐẶC TẢ YÊU CẦU PHẦN MỀM (SOFTWARE REQUIREMENTS SPECIFICATION - SRS)

> **Tài liệu này mô tả đúng mã nguồn tại thời điểm 18/09/2026.** Mọi tính năng được gắn nhãn trạng thái theo quy ước tại [Mục 0](#0-trạng-thái-triển-khai-tính-đến-18092026).
> Route hiện thực là `/dashboard` và `/student-portal`; xác thực dùng JWT cookie (`AUTH_TOKEN`) kèm session.
> Google login xác minh ID token phía server, chỉ liên kết tài khoản đã được cấp sẵn và giữ quyền trong database; xem GOOGLE_LOGIN.md. Lỗi database không được thay bằng mock data hoặc kết quả thành công.
> Bằng chứng kiểm thử, số liệu và giới hạn nghiệm thu: xem BAO_CAO_TIEN_DO_TUAN_1_3.md và DEMO_TUAN_1_3.md. Sơ đồ kiến trúc và luồng màn hình: xem DESIGN_ARTEFACTS.md.


## DỰ ÁN: AITA CODEDEFEND — AI PLAGIARISM & CODE SIMILARITY DETECTION SUITE
**Môn học:** PRJ301 — Web Applications Development (Research-Based Learning)  
**Nhóm thực hiện:** SE20C — Nhóm 7  
**Tiêu chuẩn chất lượng:** Barem đánh giá PRJ30x (100/100 điểm) & Chuẩn đặc tả IEEE 830 / SDD Spec-Kit  

---

# MỤC LỤC
0. [TRẠNG THÁI TRIỂN KHAI (IMPLEMENTATION STATUS)](#0-trạng-thái-triển-khai-tính-đến-18092026)
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
7. [PHẠM VI NGOÀI TUẦN 1–3 (OUT OF SCOPE)](#7-phạm-vi-ngoài-tuần-13-out-of-scope)

---

# 0. TRẠNG THÁI TRIỂN KHAI (TÍNH ĐẾN 18/09/2026)

Mục này là **nguồn sự thật duy nhất** về mức độ hoàn thành. Mọi con số và tên lớp được nêu trong tài liệu đều đối chiếu được với mã nguồn.

## 0.1. Quy ước nhãn trạng thái

| Nhãn | Ý nghĩa | Yêu cầu bằng chứng |
| :--- | :--- | :--- |
| `✅ ĐÃ TRIỂN KHAI (W1–3)` | Mã nguồn tồn tại và hoạt động đúng như mô tả | Trích dẫn `file:line` |
| `🟡 MỘT PHẦN (W1–3)` | Có triển khai nhưng còn hạn chế đã biết | `file:line` + mô tả hạn chế |
| `⏳ CHƯA TRIỂN KHAI — DỰ KIẾN W4–9` | Chưa có mã nguồn; vẫn giữ trong phạm vi dự án | "không có file/class"; liệt kê tại [Mục 7](#7-phạm-vi-ngoài-tuần-13-out-of-scope) |

## 0.2. Trạng thái theo module

| Module | Trạng thái | Ghi chú ngắn |
| :--- | :--- | :--- |
| **MOD-01** Xác thực & Phân quyền | `✅` | Một filter duy nhất `AuthFilter`; JWT HS256 + session; Google login xác minh phía server |
| **MOD-02** Quản lý Khóa học & Bài tập | `✅` | CRUD đầy đủ; kiểm tra sở hữu ngay trong câu SQL. Phân trang/tìm kiếm `⏳` |
| **MOD-03** Tiếp nhận & Kiểm định Bài nộp | `🟡` | Upload + SHA-256 hoạt động; giải nén ZIP hàng loạt `⏳` |
| **MOD-04** Lõi Đối soát Trùng lặp | `🟡` | Jaccard + Levenshtein trên mã Java; engine văn bản TF-IDF/Cosine `⏳`; MatchingBlocks `🟡` |
| **MOD-05** AI & Nhận diện LLM | `🟡` | `GeminiPlagiarismService` được gọi cho cặp `HIGH_RISK` trong hạn mức; nhận diện LLM `⏳` |
| **MOD-06** Báo cáo & Giám định | `🟡` | Dashboard + ma trận NxN + xuất CSV; heatmap tô màu, diff 2 cột, PDF `⏳` |
| **MOD-07** Trình diễn 3D & Giao diện | `✅` | Landing page `index.jsp`, mô hình `cyber-shield.glb`, Liquid Glass |

## 0.3. Ảnh chụp hiện trạng (Snapshot)

| Chỉ số | Giá trị | Nguồn |
| :--- | :--- | :--- |
| Số ca kiểm thử JUnit | **≈170** (đếm theo khai báo). Con số chính xác cần chạy `tools/test-java.ps1` trên máy có SQL Server. **Đã xác nhận: 94 ca không cần CSDL chạy và đạt 94/94.** | Đếm khai báo trong `src/test`; kết quả chạy trong `target/manual-test-run.log` |
| Lớp Java chính | 33 file (13 controller/config, 6 DAO, 4 service, 3 util, 6 model, 1 filter) | `src/java/com/aita/plagiarism/` |
| Trang JSP | 11 file | `web/` |
| Bảng CSDL | 6 (3NF, có 2 trigger bảo toàn ràng buộc) | `database/database_schema.sql` |
| Commit Git | 35 | `git rev-list --count HEAD` |

> **Nguyên tắc P4:** mọi số liệu chỉ được ghi tại Mục 0. Các mục khác tham chiếu về đây, không lặp lại con số.

---

# 1. TỔNG QUAN HỆ THỐNG & PHẠM VI

### 1.1. Bối cảnh & Mục tiêu
Trong quá trình đào tạo lập trình tại bậc đại học (đặc biệt các môn như PRJ301, CSD201), tình trạng sao chép mã nguồn và gian lận học thuật diễn ra ngày càng tinh vi nhờ các kỹ thuật đổi tên định danh (identifier renaming), đảo cấu trúc lệnh điều khiển (control flow restructuring) và đặc biệt là sự trợ giúp từ các công cụ AI thế hệ mới (ChatGPT, Claude, Gemini). Giảng viên đối mặt với tình trạng "Burnout" nghiêm trọng khi phải thủ công rà soát hàng trăm bài nộp.

**AITA CodeDefend** ra đời như một hệ sinh thái thẩm định liêm chính học thuật thế hệ mới, kết hợp chặt chẽ giữa:
1. **Nền tảng Java Web MVC2 chuẩn mực (Jakarta EE 6/10, Tomcat 10.1, SQL Server):** Đáp ứng 100% chuẩn đầu ra môn học PRJ301 theo chuẩn đánh giá thực tế.
2. **Lõi đối soát mã nguồn Java — `✅ ĐÃ TRIỂN KHAI (W1–3)`:** Token hóa bằng biểu thức chính quy (regex tokenizer), chuẩn hóa định danh biến/hàm thành `$ID_n`, tính Jaccard trên tập n-gram ($k = 3$) và Normalized Levenshtein. Bằng chứng: `PlagiarismEngineService.java:44-72` (chuẩn hóa), `:77-97` (Jaccard), `:102-110` (Levenshtein), `:115-124` (tổng hợp $0.6/0.4$). **Không sử dụng AST hay JavaParser.**
   - *Không gian vector TF-IDF + Cosine Similarity cho bài luận tiếng Anh:* `⏳ CHƯA TRIỂN KHAI — DỰ KIẾN W4–9` (không có lớp nào trong `src/java` cài đặt TF-IDF hay Cosine).
3. **Trợ lý thẩm định Gemini — `🟡 MỘT PHẦN (W1–3)`:** Lớp `GeminiPlagiarismService` gọi endpoint `gemini-2.0-flash` cho các cặp `HIGH_RISK` trong hạn mức cấu hình; khi thiếu khóa hoặc lỗi, hệ thống lưu nhận định cục bộ và **gắn nhãn rõ ràng là không phải kết quả AI**. Bằng chứng: `GeminiPlagiarismService.java:19,43-85`.
   - *Nhận diện văn bản/mã do LLM sinh:* `⏳ CHƯA TRIỂN KHAI — DỰ KIẾN W4–9`.

### 1.2. Các bên liên quan (Actors & Personas)
* **ADMIN (Quản trị viên hệ thống):** Quản lý khóa học và bài tập trên mọi phạm vi (`✅`). Quản lý tài khoản người dùng và phân quyền: `⏳ CHƯA TRIỂN KHAI — DỰ KIẾN W4–9`.
* **INSTRUCTOR (Giảng viên):** Tạo khóa học, bài tập, thiết lập ngưỡng tương đồng, kích hoạt đối soát toàn lớp và xuất biên bản CSV (`✅`). Nộp bài hàng loạt bằng ZIP: `⏳ CHƯA TRIỂN KHAI — DỰ KIẾN W4–9`.
* **STUDENT (Sinh viên):** Nộp bài cá nhân, kiểm tra tính toàn vẹn qua mã băm SHA-256, xem trạng thái và kết quả đối soát cá nhân (`✅`). Đăng ký/tham gia khóa học: `⏳ CHƯA TRIỂN KHAI — DỰ KIẾN W4–9` (CSDL chưa có quan hệ enrolment; sinh viên hiện xem danh sách bài tập chung).
  - Định dạng được nhận: `.java`, `.txt` được xử lý nội dung; `.docx`, `.zip` được nhận và lưu nhưng **chưa bóc tách nội dung** (`🟡`).

---

# 2. SƠ ĐỒ PHÂN RÃ CHỨC NĂNG (FUNCTIONAL MAP)

Hệ thống được phân cấp nghiêm ngặt theo 5 tầng:
`System` $\rightarrow$ `Module` $\rightarrow$ `Feature` $\rightarrow$ `Sub-feature` $\rightarrow$ `Individual Behavior`

```
AITA CodeDefend System
├── MOD-01: Authentication & Access Control (Xác thực & Phân quyền)  ✅
│   ├── FE-01.1: Quản lý phiên & Đăng nhập  ✅
│   │   ├── SUB-01.1.1: Đăng nhập bằng Form (username hoặc email) + PBKDF2  ✅
│   │   └── SUB-01.1.2: Đăng xuất & Hủy phiên an toàn  ✅
│   ├── FE-01.2: Bộ lọc Phân quyền RBAC — một filter duy nhất `AuthFilter`  ✅
│   │   └── SUB-01.2.1: Xác thực JWT + kiểm tra role + chống CSRF theo Origin  ✅
│   └── FE-01.3: Quản lý Hồ sơ Người dùng  ✅
│       └── SUB-01.3.1: Xem thông tin, đổi mật khẩu và cập nhật avatar  ✅
├── MOD-02: Course & Assignment Management (Quản lý Khóa học & Bài tập)  ✅
│   ├── FE-02.1: Quản lý Khóa học  ✅
│   │   ├── SUB-02.1.1: CRUD Khóa học  ✅
│   │   └── SUB-02.1.2: Phân trang & Tìm kiếm Khóa học  ⏳ CHƯA TRIỂN KHAI — W4–9
│   ├── FE-02.2: Thiết lập Bài tập & Ngưỡng Tương đồng  ✅
│   │   ├── SUB-02.2.1: Tạo bài tập, hạn nộp và barem điểm  ✅ (chưa validate deadline > now)
│   │   └── SUB-02.2.2: Cấu hình similarity_threshold  ✅ (chấp nhận 0, không bị ép về 75)
│   └── FE-02.3: Theo dõi Tiến độ Nộp bài (Submission Roster)  ✅
│       └── SUB-02.3.1: Thống kê trạng thái nộp bài của sinh viên  ✅
├── MOD-03: Submission & Artifact Pipeline (Tiếp nhận & Kiểm định Bài nộp)  🟡
│   ├── FE-03.1: Tiếp nhận Bài nộp Đa định dạng  🟡
│   │   ├── SUB-03.1.1: Upload file lẻ (.java, .txt, .docx, .zip)  🟡 (docx/zip lưu nhưng chưa bóc tách)
│   │   └── SUB-03.1.2: Lưu trữ ngoài Web Root (`AITA_UPLOAD_DIR`)  ✅
│   ├── FE-03.2: Xác thực Toàn vẹn Mã nguồn SHA-256  ✅
│   │   └── SUB-03.2.1: Băm trực tiếp byte stream và lưu trữ cơ sở dữ liệu  ✅
│   └── FE-03.3: Bóc tách & Xử lý Hàng loạt (Batch ZIP Processing)  ⏳ CHƯA TRIỂN KHAI — W4–9
│       ├── SUB-03.3.1: Tiếp nhận và kiểm tra cấu trúc file nén ZIP  ⏳
│       └── SUB-03.3.2: Giải nén an toàn chống Zip Slip và tự động nạp Submissions  ⏳
├── MOD-04: Similarity & Plagiarism Engine (Lõi Đối soát Trùng lặp)  🟡
│   ├── FE-04.1: Phân tích Mã nguồn Java (Java Lexical Normalization)  ✅
│   │   ├── SUB-04.1.1: Loại bỏ comment & Chuẩn hóa định danh biến/hàm  ✅ (regex tokenizer, không dùng AST)
│   │   └── SUB-04.1.2: Trích xuất N-Gram (k=3), tính Jaccard & Levenshtein  ✅
│   ├── FE-04.2: Phân tích Bài luận Văn bản (Text NLP Engine)  ⏳ CHƯA TRIỂN KHAI — W4–9
│   │   ├── SUB-04.2.1: Tiền xử lý NLP: Tokenization, Stopwords, Stemming  ⏳
│   │   └── SUB-04.2.2: Không gian vector TF-IDF & Cosine Similarity  ⏳
│   ├── FE-04.3: Quét Ma trận Đối soát Chéo (NxN Pairwise Comparison)  ✅
│   │   ├── SUB-04.3.1: So khớp tổ hợp C(N, 2) cặp bài nộp  ✅
│   │   └── SUB-04.3.2: Phân cấp nguy cơ: SAFE, LOW, MEDIUM, HIGH_RISK  ✅
│   └── FE-04.4: Bóc tách Khối mã Trùng khớp (Matching Blocks Extraction)  🟡
│       └── SUB-04.4.1: Xác định tọa độ dòng bắt đầu/kết thúc  🟡 (hiện là khối minh hoạ theo heuristic, chưa LCS)
├── MOD-05: AI Intelligence & LLM Detection (Trí tuệ Nhân tạo & Nhận diện LLM)  🟡
│   ├── FE-05.1: Phân tích Ngữ nghĩa & Thủ thuật Tránh né qua Gemini API  🟡
│   │   ├── SUB-05.1.1: Kích hoạt cho cặp vượt ngưỡng `similarity_threshold` (HIGH_RISK), có hạn mức  🟡
│   │   └── SUB-05.1.2: Sinh `ai_analysis_summary` từ Gemini; lỗi/thiếu key → nhận định cục bộ gắn nhãn  🟡
│   └── FE-05.2: Nhận diện Dấu vết Mã/Văn bản Máy sinh (AI Heuristics)  ⏳ CHƯA TRIỂN KHAI — W4–9
│       └── SUB-05.2.1: Đánh giá độ hỗn loạn cú pháp (Sentence Burstiness) & gắn cờ nghi vấn  ⏳
├── MOD-06: Reporting & Diff Inspection (Báo cáo & Giám định Trực quan)  🟡
│   ├── FE-06.1: Dashboard Tổng quan & Ma trận Tương đồng  🟡
│   │   └── SUB-06.1.1: Ma trận NxN dạng số từ `PlagiarismReports`; tô màu heatmap ⏳ W4–9  🟡
│   ├── FE-06.2: So sánh Trực quan Song song (Side-by-Side Code Diff Viewer)  ⏳ CHƯA TRIỂN KHAI — W4–9
│   │   ├── SUB-06.2.1: Giao diện 2 cột đồng bộ thanh cuộn  ⏳
│   │   └── SUB-06.2.2: Tô màu khối mã vi phạm và hiển thị ghi chú  ⏳
│   └── FE-06.3: Xuất Báo cáo Thẩm định Học thuật (Audit Report Export)  🟡
│       └── SUB-06.3.1: Xuất CSV (có redaction cho sinh viên); PDF ⏳ W4–9  🟡
└── MOD-07: Modern Presentation & 3D Interactive Layer (Trình diễn 3D & Giao diện Hiện đại)  ✅
    ├── FE-07.1: Landing Page Scrollytelling (canvas 2D theo khung hình)  ✅
    │   └── SUB-07.1.1: Vẽ `frame_*.webp` lên canvas theo tiến trình cuộn (không dùng WebGL)  ✅
    ├── FE-07.3: Mô hình 3D Cyber Shield (Three.js r128 + GLTF) trên `login.jsp` & `batch-scanner.jsp`  ✅
    │   └── SUB-07.3.1: Dựng `cyber-shield.glb` qua `cyber-shield-3d.js`  ✅
    └── FE-07.2: Giao diện Quản trị Phong cách Liquid Glass  ✅
        └── SUB-07.2.1: Hệ thống CSS Liquid Glass (`liquid-glass-2026.css`), responsive  ✅
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
  1. Người dùng nhập `username` **hoặc** `email` và `password` tại màn hình đăng nhập (`/login`).
  2. Hệ thống chuyển tiếp yêu cầu đến `LoginServlet` (phương thức `POST`).
  3. `LoginServlet` gọi `UserDAO.authenticate(usernameOrEmail, rawPassword)` — truyền **mật khẩu gốc**, không phải mã băm. (`UserDAO.java:49`)
  4. Hệ thống truy vấn `WHERE LOWER(username) = LOWER(?) OR LOWER(email) = LOWER(?)`, rồi đối khớp bằng `PasswordUtil.verifyPassword`. (`UserDAO.java:54-65`)
  5. Nếu chính xác: tạo `HttpSession`, gán đối tượng `User` vào session attribute **`currentUser`**, thiết lập timeout 30 phút. (`LoginServlet.java:88-91`)
  6. Chuyển hướng theo vai trò: `STUDENT` → `/student-portal`; `ADMIN`/`INSTRUCTOR` → `/dashboard`. (`LoginServlet.java:95-99`)
* **Alternative Flows:**
  - *Ghi nhớ đăng nhập:* `⏳ CHƯA TRIỂN KHAI — DỰ KIẾN W4–9`. Hiện hệ thống cấp JWT trong cookie `AUTH_TOKEN` (`HttpOnly`, `SameSite=Strict`, `Secure` khi HTTPS), thời hạn **24 giờ**; không có cơ chế gia hạn hay "Remember Me". (`LoginServlet.java:79-85`)
* **Exception Flows:**
  - *Sai thông tin:* Hiển thị *"Email hoặc mật khẩu không chính xác. Vui lòng kiểm tra lại!"*. **Không có bộ đếm số lần sai.** (`LoginServlet.java:65-66`)
  - *Khóa tài khoản tạm thời:* `⏳ CHƯA TRIỂN KHAI — DỰ KIẾN W4–9` (không có cột/bảng lockout trong CSDL).
  - *Mất kết nối CSDL:* `AuthFilter` trả **HTTP 503** kèm *"Không thể kết nối hoặc thực hiện thao tác với cơ sở dữ liệu. Vui lòng thử lại."*; xung đột dữ liệu trả **409**. (`AuthFilter.java:76-80`)
* **Inputs & Data Fields:** `email` (bắt buộc, dùng cho cả username và email), `password` (bắt buộc, raw text từ form).
* **Outputs:** `HttpSession` hợp lệ chứa thuộc tính `currentUser` (gồm `user_id`, `username`, `full_name`, `email`, `role`, `avatar_url`) và `jwtToken`.
* **Data Structures & Relationships:** Bảng `Users` (`user_id`, `username`, `password_hash`, `full_name`, `email`, `role`, `avatar_url`, `google_subject`, `created_at`).
* **Business Rules & Constraints:**
  - `BR-AUTH-01`: Mật khẩu mới được băm bằng **PBKDF2-HMAC-SHA256, 600.000 vòng lặp, salt ngẫu nhiên 16 byte**. MD5/SHA-256 **chỉ** được chấp nhận để xác thực dữ liệu legacy và được tự động nâng cấp sang PBKDF2 ngay sau khi đăng nhập đúng. Tuyệt đối không lưu raw text. (`PasswordUtil.java:19-27`, `UserDAO.java:67-78`)
  - `BR-AUTH-02`: Phiên không tương tác sau 30 phút tự động bị hủy. (`LoginServlet.java:90`, `AuthFilter.java:71`)
  - `BR-AUTH-03`: Mật khẩu cũ bị từ chối sau khi đổi; việc đổi dùng cập nhật có điều kiện theo mã băm đã đọc, nên hai thao tác đổi đồng thời không ghi đè lên nhau. (`UserDAO.java:199-207`)
* **Validation Rules:**
  - Đăng nhập: `email`/`username` và `password` chỉ bắt buộc khác rỗng; **không** có quy tắc độ dài tối thiểu hay cấm ký tự đặc biệt ở bước này. (`LoginServlet.java:55-60`)
  - Đổi mật khẩu: mật khẩu mới **8–1024** ký tự. (`UserDAO.java:175`) — quy tắc này **không** áp dụng cho đăng nhập.
* **Roles & Permissions:** Người dùng chưa xác thực được phép truy cập `/login`.
* **UI & System States:**
  - UI State: `Form Initial` $\rightarrow$ `Submitting (Loading Spinner)` $\rightarrow$ `Success (Redirect)` / `Error (Alert Box)`.
  - System State: `Anonymous` $\rightarrow$ `Authenticated Session Active`.
* **Error Cases & Edge Cases:**
  - *Session fixation attack:* Tự động gọi `request.changeSessionId()` ngay sau khi đăng nhập thành công. (`LoginServlet.java:89`)
  - *Token hết hạn/sai chữ ký:* `AuthFilter` hủy session cũ và chuyển hướng về `/login?error=unauthorized`. (`AuthFilter.java:42-47`)
* **Dependencies:** `DBContext.java`, `UserDAO.java`, `PasswordUtil.java`, `JWTUtil.java`.
* **External Integrations:** Google Identity Services (xem `GOOGLE_LOGIN.md`).
* **Postconditions:** Phiên người dùng được khởi tạo.
  - `⏳` Ghi log đăng nhập kèm `username` và địa chỉ IP: **CHƯA TRIỂN KHAI — DỰ KIẾN W4–9**.
* **Acceptance Criteria:**
  - **[AC-AUTH-01]** Đăng nhập với tài khoản hợp lệ trong CSDL seed (`teacher_ha`) phải chuyển hướng đến `/dashboard`; tài khoản `STUDENT` chuyển hướng đến `/student-portal`.
  - **[AC-AUTH-02]** Đăng nhập sai mật khẩu trả về thông báo lỗi trên JSP, không để lộ cấu trúc ngoại lệ CSDL.
  - **[AC-AUTH-04]** Đăng nhập đúng bằng mật khẩu legacy (MD5/SHA-256) phải nâng cấp bản ghi sang PBKDF2 và từ chối mật khẩu cũ ở các lần tiếp theo.

### Feature FE-01.2: Bộ lọc Phân quyền RBAC (Role-Based Access Control via Filter)
* **Purpose:** Ngăn chặn tuyệt đối việc truy cập trái phép hoặc leo thang đặc quyền (Privilege Escalation) vào các URL nghiệp vụ.
* **User/Actor:** Hệ thống (`AuthFilter` — **một filter duy nhất** đảm nhiệm cả xác thực lẫn phân quyền).
* **Preconditions:** Servlet container đã khởi động; filter được đăng ký bằng annotation `@WebFilter("/*")`. (`AuthFilter.java:15`)
  - Các servlet và filter dùng annotation; `web/WEB-INF/web.xml` chỉ bổ sung cấu hình container: `session-config` (timeout 30 phút, cookie `HttpOnly`, `tracking-mode COOKIE` để không rò `JSESSIONID` qua URL) và `error-page` cho 403/404/500/`Throwable` trỏ về `error.jsp` (không lộ stack trace).
* **Main Flow:**
  1. Client gửi HTTP Request đến một endpoint (ví dụ `/course-action`).
  2. `AuthFilter` đọc cookie `AUTH_TOKEN`, bóc tách claim bằng `JWTUtil.extractClaims`, rồi **tra lại người dùng trong CSDL** (`UserDAO.getUserById`). Token hợp lệ nhưng người dùng không còn tồn tại ⇒ bị từ chối. (`AuthFilter.java:36-47`)
  3. Nếu chưa xác thực: hủy session cũ (nếu có) và chuyển hướng 302 về `/login?error=unauthorized`. **Không lưu URL quay lại.** (`AuthFilter.java:42-47`)
  4. Nếu đã xác thực: kiểm tra đường dẫn có thuộc tập `STAFF` (`/dashboard`, `/batch-scanner`, `/course-action`, `/assignment-action`) không; nếu có thì bắt buộc `role ∈ {ADMIN, INSTRUCTOR}`. (`AuthFilter.java:20-22,48-51`)
  5. Với mọi request không phải `GET`/`HEAD`: chống CSRF theo nguyên tắc **mặc định từ chối** — chỉ cho qua khi có bằng chứng cùng nguồn (`Sec-Fetch-Site: same-origin|none` **hoặc** `Origin` khớp). Nếu cả hai header đều vắng mặt, request bị chặn. (`AuthFilter.java:52-68`)
  6. Chặn mọi truy cập vào `/uploads/`; chuyển hướng các URL `.jsp` về controller tương ứng. (`AuthFilter.java:63-69`)
  7. Nếu hợp lệ: `chain.doFilter(request, response)`.
* **Exception Flows:**
  - Nếu `role` không phù hợp (ví dụ `STUDENT` truy cập `/dashboard`): `response.sendError(403)` — **không có trang `/access-denied.jsp`**. (`AuthFilter.java:48-51`)
  - Lỗi CSDL: trả **409** (xung đột) hoặc **503** (lỗi kết nối/thao tác). (`AuthFilter.java:75-85`)
* **Acceptance Criteria:**
  - **[AC-AUTH-03]** Sinh viên đã đăng nhập khi gõ trực tiếp `/dashboard` hoặc `/batch-scanner` phải nhận mã HTTP 403 Forbidden.
  - **[AC-AUTH-05]** Request đổi trạng thái bị từ chối với HTTP 403 khi: `Sec-Fetch-Site: cross-site`, **hoặc** `Origin` sai, **hoặc** không chứng minh được cùng nguồn (thiếu cả `Origin` lẫn `Sec-Fetch-Site`).
  - **[AC-AUTH-07]** Request đổi trạng thái không kèm bất kỳ header chứng minh nguồn nào (ví dụ `curl` thuần) phải nhận HTTP 403. Lưu ý: các script kiểm chứng trong `tools/` đã được cập nhật để gửi `Origin` giả lập trình duyệt.
  - **[AC-AUTH-06]** Truy cập `/uploads/*` phải trả HTTP 403 với mọi vai trò, kể cả Admin.

---

## MOD-02: Quản lý Khóa học & Bài tập (Course & Assignment Management)

### Feature FE-02.1: Quản lý Khóa học (Course Management)
* **Purpose:** Cho phép Giảng viên và Admin tạo lập, cấu hình lớp học và gán quyền giảng dạy môn học.
* **User/Actor:** `INSTRUCTOR`, `ADMIN`.
* **Preconditions:** Giảng viên đã đăng nhập thành công.
* **Main Flow:**
  1. Giảng viên thao tác trên giao diện Dashboard (`/dashboard`); form gửi `POST /course-action` với `action=create|update|delete`. (`CourseActionServlet.java:10`)
  2. Điền: Mã môn (`course_code`, ≤ 20 ký tự), Tên môn (`course_name`, ≤ 150), Học kỳ (`semester`, ≤ 20). Trường nào trống hoặc vượt quá độ dài ⇒ HTTP 400. (`CourseActionServlet.java:47-50`)
  3. **Không kiểm tra trùng `course_code` tại tầng ứng dụng**; tính duy nhất được bảo đảm bởi ràng buộc `UNIQUE` của CSDL. (`database_schema.sql:28`)
  4. Ghi bản ghi vào bảng `Courses` với `instructor_id` = `user_id` của người thực hiện. (`CourseActionServlet.java:56`)
  5. Chuyển hướng về `/dashboard?courseMsg=created&courseId=<id>`.
* **Business Rules:**
  - `BR-COURSE-01`: Mỗi `course_code` là duy nhất trong toàn hệ thống (bảo đảm bởi `UNIQUE` của CSDL, không phải kiểm tra ứng dụng).
  - `BR-COURSE-02`: Chỉ giảng viên sở hữu khóa học hoặc Admin mới có quyền sửa/xóa. Điều kiện này được đưa thẳng vào mệnh đề `WHERE` của câu `UPDATE`/`DELETE`, không chỉ kiểm tra ở servlet. (`CourseDAO.java:50,66`)
* **Acceptance Criteria:**
  - **[AC-CRS-01]** Tạo khóa học với mã môn đã tồn tại phải bị từ chối: vi phạm `UNIQUE` được chuyển thành `DataAccessException` và trả **HTTP 409** kèm *"Dữ liệu bị trùng hoặc đang được tham chiếu. Thao tác chưa được lưu."* (`AuthFilter.java:78-79`)
  - **[AC-CRS-02]** Giảng viên A gửi request sửa/xóa khóa học của giảng viên B phải nhận **HTTP 403** và dữ liệu không đổi. (`CourseActionServlet.java:34-37`)
  - **[AC-CRS-03]** Danh sách khóa học trên Dashboard chỉ gồm khóa học do chính giảng viên đó sở hữu; Admin thấy tất cả. (`DashboardServlet.java:38-43`)

### Feature FE-02.2: Thiết lập Bài tập & Ngưỡng Tương đồng (Assignment Configuration)
* **Purpose:** Giảng viên tạo bài tập thực hành, quy định thời hạn và thiết lập ngưỡng báo động đỏ cho tỷ lệ đạo văn.
* **User/Actor:** `INSTRUCTOR`.
* **Preconditions:** Khóa học đã được tạo trước đó.
* **Main Flow:**
  1. Giảng viên chọn Khóa học, nhấn "Tạo Bài Tập Mới".
  2. Nhập các trường: Tiêu đề (`title`), Mô tả (`description`), Điểm tối đa (`max_score`), Hạn nộp (`deadline`), Ngưỡng tương đồng (`similarity_threshold`).
  3. Hệ thống validate: tiêu đề 1–150 ký tự; `max_score` $\in (0, 999.99]$; `similarity_threshold` $\in [0, 100]$; `deadline` phải parse được theo ISO local datetime. Không đạt ⇒ HTTP 400. (`AssignmentActionServlet.java:42-47,62`)
  4. Lưu dữ liệu vào bảng `Assignments`. Quyền được kiểm tra bằng `AccessPolicy.canManageCourse` và được lặp lại trong mệnh đề `WHERE` của câu SQL. (`AssignmentActionServlet.java:36`, `AssignmentDAO.java:59-61,81-83`)
* **Acceptance Criteria:**
  - **[AC-ASN-01]** Hệ thống từ chối lưu khi `similarity_threshold` < 0 hoặc > 100, hoặc `max_score` không hợp lệ.
  - `⏳` Ràng buộc "hạn nộp phải lớn hơn thời điểm hiện tại" **CHƯA TRIỂN KHAI — DỰ KIẾN W4–9**. Hiện `deadline` ở quá khứ vẫn được lưu.
  - **[AC-ASN-02]** Ngưỡng `similarity_threshold = 0` được lưu nguyên giá trị 0, **không** bị đổi ngầm thành 75. Giá trị 75 chỉ được dùng làm fallback tại thời điểm quét khi ngưỡng không dương. (`AssignmentDAO.java:35`, `PlagiarismEngineService.java:136-137`)
  - **[AC-ASN-03]** Sửa tiêu đề bài tập phải giữ nguyên `deadline` và `similarity_threshold` đã cấu hình. (`AssignmentActionServlet.java:53-54`)

---

## MOD-03: Tiếp nhận & Kiểm định Bài nộp (Submission & Artifact Pipeline)

### Feature FE-03.1: Tiếp nhận Bài nộp Đa định dạng (File Ingestion)
* **Purpose:** Cho phép sinh viên nộp bài làm cá nhân dưới nhiều định dạng (`.java`, `.txt`, `.docx`, `.zip`), đảm bảo an toàn tệp tin.
* **User/Actor:** `STUDENT`.
* **Preconditions:** Sinh viên đã đăng nhập, bài tập còn trong thời hạn nộp (`GETDATE() <= deadline`).
* **Main Flow:**
  1. Sinh viên truy cập cổng sinh viên (`/student-portal`), chọn bài tập trong form nộp. (`StudentPortalServlet.java:22`)
  2. Chọn file từ máy tính.
  3. Nhấn "Nộp bài" — form gửi `POST /submit` (`multipart/form-data`). (`SubmissionServlet.java:28`)
  4. `SubmissionServlet` tiếp nhận request; tham số `assignmentId` **bắt buộc**, thiếu hoặc không hợp lệ ⇒ HTTP 400 (hệ thống **không** ngầm gán bài tập mặc định).
  5. Hệ thống xác thực **phần mở rộng tệp** theo whitelist; **chưa** kiểm tra `Content-Type`. (`SubmissionServlet.java:72-79`)
  6. Lưu tệp vào thư mục **ngoài Web Root** do `AITA_UPLOAD_DIR` cấu hình (mặc định `${catalina.base}/aita-uploads`), tên tệp được đặt lại để tránh ghi đè và path traversal. CSDL lưu đường dẫn tương đối. (`config/StorageConfig.java`)
  7. Kích hoạt tự động tính mã băm SHA-256 (FE-03.2).
  8. Ghi bản ghi vào bảng `Submissions` với trạng thái `PENDING`.
* **Validation Rules:**
  - Dung lượng file tối đa: 25 MB (`maxFileSize = 26,214,400 bytes`), request tối đa 30 MB. (`SubmissionServlet.java:29-33`)
  - Whitelist định dạng: `java`, `txt`, `docx`, `zip`. Từ chối mọi định dạng khác, bao gồm file thực thi.
  - `🟡` `.docx` và `.zip` được nhận và lưu trữ nhưng **chưa bóc tách nội dung** để đưa vào lõi đối soát.
  - `✅` Bài tập phải tồn tại (404 nếu không) và còn trong thời hạn nộp (403 nếu quá hạn) theo `AccessPolicy.canSubmitTo`.
    - **Giới hạn đã biết:** chưa có quan hệ enrolment nên chưa thể giới hạn "sinh viên thuộc lớp nào được nộp bài tập đó". Mọi người dùng đã xác thực đều có thể nộp vào bất kỳ bài tập còn hạn nào. Sẽ bổ sung khi quan hệ enrolment hoàn thành (xem [Mục 7](#7-phạm-vi-ngoài-tuần-13-out-of-scope)).
* **Acceptance Criteria:**
  - **[AC-SUB-01]** Tải lên file quá 25 MB, không thuộc whitelist đuôi file, hoặc thiếu `assignmentId` phải lập tức bị từ chối kèm mã thông báo cụ thể (`submitError=invalid_format`, `empty_file`, hoặc HTTP 400).

### Feature FE-03.2: Xác thực Toàn vẹn Mã nguồn SHA-256 (Checksum Engine)
* **Purpose:** Đảm bảo tính toàn vẹn (Integrity) và chống chối bỏ (Non-repudiation) của bài nộp sinh viên theo chuẩn mục 4.4.2 của đề cương RBL.
* **User/Actor:** Hệ thống (`SubmissionServlet` tính băm trong khi ghi luồng; `SHA256ChecksumUtil` là tiện ích băm dùng chung).
* **Preconditions:** File bài nộp đã được upload thành công lên bộ đệm stream.
* **Main Flow:**
  1. Đọc luồng byte của file, cập nhật liên tiếp vào `MessageDigest.getInstance("SHA-256")` trong cùng một lượt ghi xuống đĩa (không đọc file hai lần).
  2. Chuyển đổi mảng byte sang chuỗi Hexadecimal gồm 64 ký tự.
  3. Ghi chuỗi mã băm này vào cột `Submissions.sha256_hash`.
  4. Hiển thị mã SHA-256 trên cổng sinh viên làm bằng chứng nộp bài hợp lệ.
* **Exception Flows:**
  - *Lỗi khi băm (không lấy được `MessageDigest`, lỗi I/O):* **toàn bộ thao tác nộp bài bị từ chối**, hệ thống không ghi bản ghi và không lưu mã băm thay thế. (`SHA256ChecksumUtil.java`)
* **Acceptance Criteria:**
  - **[AC-SUB-02]** Mọi bản ghi bài nộp trong bảng `Submissions` bắt buộc có giá trị `sha256_hash` gồm đúng 64 ký tự hex hợp lệ.
  - **[AC-SUB-04]** Nếu quá trình băm lỗi, hệ thống **không** được lưu giá trị thay thế (ví dụ mã băm của chuỗi rỗng `e3b0c442…b855`); bản ghi không được tạo.

### Feature FE-03.3: Bóc tách & Xử lý Hàng loạt (Batch ZIP Processing) — `⏳ CHƯA TRIỂN KHAI — DỰ KIẾN W4–9`

> **Trạng thái:** chưa có mã nguồn. Không tìm thấy `ZipInputStream`, `ZipEntry`, `ZipFile` hay `getCanonicalPath` nào trong `src/java`; không có lớp `ZipExtractionService` hay `BatchUploadServlet`.
> Lớp hiện có là `BatchScannerServlet` — nhưng nó **chỉ kích hoạt quét** trên các bài nộp đã có, không giải nén ZIP.
> Xem danh sách đầy đủ tại [Mục 7](#7-phạm-vi-ngoài-tuần-13-out-of-scope).

* **Purpose:** (Mục tiêu giữ nguyên) Giúp Giảng viên tải lên file nén chứa hàng chục bài làm của cả lớp và hệ thống tự động bóc tách vào danh mục nộp bài.
* **User/Actor:** `INSTRUCTOR`.
* **Preconditions:** Giảng viên có quyền trên bài tập tương ứng.
* **Main Flow (dự kiến):**
  1. Giảng viên upload file `.zip` chứa bài nộp của lớp.
  2. Hệ thống kiểm tra tính an toàn của từng entry trong ZIP (chống `Zip Slip`: `canonicalPath.startsWith(destinationDir)`).
  3. Bóc tách từng thư mục con tương ứng với mã sinh viên (`username`).
  4. Ánh xạ với tài khoản sinh viên trong bảng `Users`, tự động tạo bản ghi `Submissions`.
  5. Đánh dấu trạng thái `PARSED` sẵn sàng cho lõi đối soát.
* **Acceptance Criteria (dự kiến):**
  - **[AC-SUB-03]** Tải lên file ZIP chứa 50 bài nộp sinh viên: hệ thống giải nén an toàn và tạo đúng 50 bản ghi `Submissions` trong vòng dưới 10 giây.
  - *Hiện chưa có bằng chứng kiểm thử nào cho tiêu chí này.*

---

## MOD-04: Lõi Đối soát Trùng lặp (Similarity & Plagiarism Engine)

### Feature FE-04.1: Phân tích Mã nguồn Java (Java Lexical Normalization)
* **Purpose:** Loại bỏ các yếu tố gây nhiễu bề mặt (comment, khoảng trắng, tên biến ngẫu nhiên) để so sánh cấu trúc logic thực sự của mã nguồn Java.
* **User/Actor:** Hệ thống (`PlagiarismEngineService` — lớp duy nhất đảm nhiệm chuẩn hóa, đo lường và quét).
* **Preconditions:** File mã nguồn Java đã được lưu và đọc được nội dung.
* **Main Flow:**
  1. **Lexical Strip:** Dùng **biểu thức chính quy** (regex tokenizer) loại bỏ toàn bộ comment dòng đơn `//...` và comment khối `/* ... */`, chuẩn hóa khoảng trắng. **Không sử dụng AST, không dùng JavaParser** (không có dependency nào trong `pom.xml`). Bằng chứng: `PlagiarismEngineService.java:44-72`.
  2. **Identifier Normalization:**
     - Phân loại các từ khóa dành riêng của ngôn ngữ Java (`public`, `class`, `void`, `static`, `int`, `for`, `while`, `if`, `return`...). Giữ nguyên 100%.
     - Nhận diện các định danh do người dùng tự đặt (Tên biến, tên tham số, tên hàm nội bộ): Thay thế tuần tự thành `$ID_1`, `$ID_2`, `$ID_3`... theo thứ tự xuất hiện.
  3. **Token N-Gram Extraction:**
     - Tạo chuỗi token stream chuẩn hóa.
     - Sinh tập hợp các n-gram từ vựng liên tiếp với **$k = 3$ (cố định)**. (`PlagiarismEngineService.java:84-85`)
  4. **Similarity Metric Calculation:**
     - Tính toán chỉ số **Jaccard Similarity Index**:
       $$J(A, B) = \frac{|Tokens_A \cap Tokens_B|}{|Tokens_A \cup Tokens_B|} \times 100\%$$
     - Tính khoảng cách **Normalized Levenshtein Distance** trên chuỗi token
       (cài đặt tính trên **dãy token**, không phải từng ký tự — nhanh hơn ~25 lần và
       đúng với mô tả "trên chuỗi token"):
       $$Sim_{Lev}(A, B) = \left(1 - \frac{Lev(A, B)}{\max(|A|, |B|)}\right) \times 100\%$$
     - Điểm tổng hợp tương đồng mã nguồn:
       $$Score_{Java} = 0.6 \times J(A, B) + 0.4 \times Sim_{Lev}(A, B)$$
* **Acceptance Criteria:**
  - **[AC-SIM-01]** Hai đoạn mã Java có cùng logic thuật toán nhưng sinh viên B đổi tên tất cả các biến và chèn thêm comment giả: Hệ thống phải phát hiện độ tương đồng $\ge 85\%$.

### Feature FE-04.2: Phân tích Bài luận Văn bản (Text NLP Engine) — `⏳ CHƯA TRIỂN KHAI — DỰ KIẾN W4–9`

> **Trạng thái:** chưa có mã nguồn. Không có lớp `TextSimilarityEngine`, `TextPreprocessingService` hay `TfIdfVectorService`; không tìm thấy mã nguồn nào tính TF-IDF, Cosine Similarity hay danh sách stopwords trong `src/java`.
> Lõi hiện tại (`PlagiarismEngineService`) **chỉ** xử lý mã nguồn Java và hoạt động trên mọi chuỗi ký tự được đưa vào, không phân biệt ngôn ngữ.
> Xem [Mục 7](#7-phạm-vi-ngoài-tuần-13-out-of-scope).

* **Purpose:** (Mục tiêu giữ nguyên) Đo lường mức độ tương đồng giữa các bài luận, tài liệu báo cáo tiếng Anh (`.txt`, `.docx`).
* **User/Actor:** Hệ thống (`TextSimilarityEngine` — **chưa tồn tại**).
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
* **Acceptance Criteria (dự kiến, chưa kiểm chứng):**
  - **[AC-SIM-02]** Hai bài viết tiếng Anh có cấu trúc câu và từ vựng giống nhau $\ge 80\%$ (sau khi trừ stopwords) phải được tính ra điểm Cosine Similarity $\ge 75\%$.
  - *Hiện chưa có bằng chứng kiểm thử nào cho tiêu chí này.*

### Feature FE-04.3: Quét Ma trận Đối soát Chéo (NxN Pairwise Comparison)
* **Purpose:** So khớp toàn diện tất cả các bài nộp trong cùng một Assignment để phát hiện mọi mạng lưới liên kết sao chép.
* **User/Actor:** `INSTRUCTOR`.
* **Preconditions:** Có tối thiểu 2 bài nộp trong Assignment.
* **Main Flow:**
  1. Giảng viên bấm nút trên trang `/dashboard`; form gửi `POST /batch-scanner` kèm `assignmentId`. (`BatchScannerServlet.java:17`)
  2. Hệ thống kiểm tra bài tập tồn tại và người thực hiện có quyền trên khóa học chứa bài tập đó (`AccessPolicy.canManageAssignment`); không đạt ⇒ 403/404. (`BatchScannerServlet.java:42-48`)
  3. Hệ thống thu thập $N$ bài nộp hợp lệ; nếu $N < 2$ thì kết thúc và không ghi gì.
  4. Thực hiện vòng lặp so khớp tổ hợp $C(N, 2) = \frac{N(N-1)}{2}$ cặp bài nộp $(Submission_A, Submission_B)$. Toàn bộ quá trình được bọc trong **một giao dịch CSDL duy nhất** (`setAutoCommit(false)` → `commit`/`rollback`) và giữ khóa `UPDLOCK, HOLDLOCK` trên bản ghi `Assignments` để ngăn hai lượt quét chạy đồng thời.
  5. Lưu kết quả tương đồng vào bảng `PlagiarismReports`. Cặp nào **không đọc được nội dung file** sẽ bị **bỏ qua** (không tạo báo cáo) và được đếm vào số cặp bị bỏ qua.
  6. Tự động gán nhãn `risk_level` (`PlagiarismEngineService.java:216-221`):
     - `similarity_score < 30.0%`: `SAFE`
     - `30.0% <= similarity_score < 50.0%`: `LOW`
     - `50.0% <= similarity_score < similarity_threshold`: `MEDIUM`
     - `similarity_score >= similarity_threshold`: `HIGH_RISK`
  7. Cập nhật trạng thái bài nộp: `HIGH_RISK` ⇒ `FLAGGED` (cả hai bài); các cặp còn lại chuyển `ANALYZED` nếu chưa bị gắn cờ.
  8. Nếu có cấu hình `GEMINI_API_KEY`, gọi Gemini cho tối đa `GEMINI_MAX_CALLS_PER_SCAN` cặp `HIGH_RISK` có điểm cao nhất (xem FE-05.1).
* **Acceptance Criteria:**
  - **[AC-SIM-03]** Với 30 bài nộp trong 1 lớp (435 phép so khớp cặp), hệ thống Java thuần phải hoàn thành ma trận trong vòng dưới 3 giây. *(Mục tiêu hiệu năng — **chưa có bằng chứng đo lường**; xem NFR-PERF-02.)*
  - **[AC-SIM-05]** Hai lượt quét chạy đồng thời trên cùng một bài tập không được để lại dữ liệu nửa chừng: lượt sau chờ khóa hoặc bị từ chối, CSDL chỉ chứa kết quả của đúng một lượt quét trọn vẹn.
  - **[AC-SIM-06]** Nếu không đọc được nội dung file của một bài nộp, cặp chứa bài nộp đó **không** được tạo báo cáo và số cặp bị bỏ qua phải được báo lại cho người dùng. Hệ thống không được thay nội dung thật bằng nội dung giả để tính điểm.

### Feature FE-04.4: Bóc tách Khối mã Trùng khớp (Matching Blocks Extraction) — `🟡 MỘT PHẦN (W1–3)`
* **Purpose:** (Mục tiêu giữ nguyên) Xác định vị trí dòng mã bắt đầu và kết thúc giữa hai bài nộp vi phạm để làm bằng chứng đối chiếu.
* **User/Actor:** Hệ thống (`PlagiarismEngineService`; không có lớp `MatchingBlocksService` riêng).
* **Preconditions:** Cặp bài nộp có `similarity_score >= 40.0%`. (Ngưỡng thực tế là 40, không phải 50.)
* **Main Flow (hiện trạng):**
  1. Khi điểm cặp $\ge 40$, hệ thống tạo **một khối minh hoạ duy nhất** cho cặp đó. (`PlagiarismEngineService.java:170-181`)
  2. `function_name` được gán giá trị cố định `executeCoreLogic()`; toạ độ dòng được suy ra theo heuristic từ độ dài file (`15`–`min(45, ...)` cho A, `20`–`min(50, ...)` cho B) — **chưa phải toạ độ thật**.
  3. `matched_code_snippet` lưu 200 ký tự đầu của nội dung bài A.
  4. Ghi nhận các cột `student_a_start_line`, `student_a_end_line`, `student_b_start_line`, `student_b_end_line` và lưu vào bảng `MatchingBlocks`.
* **Hạn chế đã biết (`🟡`):**
  - **Chưa áp dụng LCS.** Chưa gom nhóm các dòng trùng lặp liên tiếp thành từng khối; mỗi cặp chỉ có tối đa một khối.
  - Toạ độ dòng **không phản ánh vị trí thật** của đoạn mã trùng trong file. Không nên dùng làm bằng chứng kỷ luật.
  - `⏳` Bóc tách toạ độ chính xác bằng LCS: **DỰ KIẾN W4–9**.
* **Acceptance Criteria:**
  - **[AC-SIM-04]** Mọi bản ghi `MatchingBlocks` phải có thông số dòng $> 0$ và `end_line >= start_line`. *(Điều kiện này được bảo đảm — nhưng toạ độ hiện mang tính minh hoạ, không phải kết quả bóc tách thật.)*

---

## MOD-05: Trí tuệ Nhân tạo & Nhận diện LLM (AI Intelligence & LLM Detection)

### Feature FE-05.1: Phân tích Ngữ nghĩa & Thủ thuật Tránh né qua Gemini API
* **Purpose:** Đóng vai trò trợ lý chuyên gia, phân tích sâu lý do vì sao hai bài nộp bị trùng lặp cấu trúc dù sinh viên đã dùng các thủ thuật che giấu.
* **User/Actor:** Hệ thống (`GeminiPlagiarismService` — lớp duy nhất; không có `GeminiAnalysisService`, `GeminiApiService` hay `LLMDetectorService`).
* **Preconditions:** Cặp bài nộp có `risk_level = 'HIGH_RISK'` **và** có cấu hình `GEMINI_API_KEY`.
* **Main Flow:**
  1. Sau khi quét (FE-04.3), hệ thống lọc các cặp `HIGH_RISK`, sắp xếp theo điểm giảm dần.
  2. Gọi Gemini cho **tối đa `GEMINI_MAX_CALLS_PER_SCAN` cặp** (mặc định 3) nhằm kiểm soát độ trễ và hạn mức API. Các cặp còn lại không gọi API.
  3. Tạo prompt kỹ thuật gửi đến endpoint **`models/gemini-2.0-flash:generateContent`**. Mỗi bài nộp được cắt tối đa 8.000 ký tự trước khi đưa vào prompt. (`GeminiPlagiarismService.java:19,91-94`)
  4. Phân tích kết quả, trích phần văn bản trả về và lưu vào trường `PlagiarismReports.ai_analysis_summary`, **tiền tố `[Gemini]`** để phân biệt với nhận định cục bộ.
* **Exception Flows:**
  - *Thiếu `GEMINI_API_KEY`:* không gọi API; lưu *"Phân tích cục bộ (rule-based): … Chưa cấu hình GEMINI_API_KEY nên không có nhận định AI."*
  - *Timeout:* bắt `HttpTimeoutException`, ghi log mức `WARNING`, chuyển sang nhận định cục bộ gắn nhãn.
  - *HTTP 400/401/403 (key sai hoặc hết quota):* trả fallback gắn nhãn ngay. *(Hiện **chưa** xử lý riêng HTTP 429 và **không** tự động retry — danh sách model dự phòng chỉ có một phần tử.)*
* **Nguyên tắc bắt buộc (P3):** không một văn bản nào trong CSDL được mang chữ "AI"/"Gemini" nếu nó không thực sự do mô hình sinh ra. Mọi nhận định không qua API phải ghi rõ *(rule-based)*.
* **Acceptance Criteria:**
  - **[AC-AI-01]** Cặp `HIGH_RISK` hiển thị nhận định bằng tiếng Việt có dấu từ Gemini khi có key; nếu không có key hoặc lỗi, hiển thị nhận định cục bộ **gắn nhãn rõ ràng là không phải kết quả AI**.
  - **[AC-AI-03]** Một lượt quét không được gọi Gemini quá `GEMINI_MAX_CALLS_PER_SCAN` lần, bất kể số lượng cặp `HIGH_RISK`.
  - **[AC-AI-04]** Khi thiếu key, **100%** bản ghi `ai_analysis_summary` phải chứa cụm *(rule-based)* và không chứa cụm "Gemini".

### Feature FE-05.2: Nhận diện Dấu vết Nội dung do AI sinh (AI Heuristics) — `⏳ CHƯA TRIỂN KHAI — DỰ KIẾN W4–9`

> **Trạng thái:** chưa có mã nguồn. Không có lớp `LLMDetectorService`; không tìm thấy mã tính Burstiness hay `ai_generated_probability` trong `src/java`.
> Xem [Mục 7](#7-phạm-vi-ngoài-tuần-13-out-of-scope).

* **Purpose:** (Mục tiêu giữ nguyên) Cảnh báo các đoạn mã hoặc bài luận có dấu hiệu được sinh tự động từ các mô hình LLM.
* **User/Actor:** Hệ thống (`LLMDetectorService` — **chưa tồn tại**).
* **Preconditions:** File văn bản hoặc mã nguồn đã được token hóa.
* **Main Flow:**
  1. Phân tích phân phối độ dài câu/hàm (Burstiness Index).
  2. Quét các mẫu hình rập khuôn đặc trưng của mã AI.
  3. Tính toán chỉ số nghi vấn `ai_generated_probability` ($0 - 100\%$).
  4. Nếu chỉ số $> 70\%$, gắn cờ cảnh báo `[AI-Generated Suspicion]` lên bài nộp.
* **Acceptance Criteria (dự kiến, chưa kiểm chứng):**
  - **[AC-AI-02]** Phát hiện chính xác ít nhất 80% các bài luận tiếng Anh được tạo nguyên văn từ ChatGPT với cờ cảnh báo văn bản máy sinh.
  - *Hiện chưa có bằng chứng kiểm thử nào cho tiêu chí này.*

---

## MOD-06: Báo cáo & Giám định Trực quan (Reporting & Diff Inspection)

### Feature FE-06.1: Dashboard Tổng quan & Ma trận Tương đồng Heatmap
* **Purpose:** Cung cấp cái nhìn toàn cảnh trực quan về tình hình liêm chính của cả lớp học.
* **User/Actor:** `INSTRUCTOR`, `ADMIN`.
* **Preconditions:** Đã hoàn thành quá trình quét đạo văn cho Assignment.
* **Main Flow:**
  1. Giảng viên truy cập `/dashboard?courseId=<id>&assignmentId=<id>` (`DashboardServlet`). Danh sách khóa học được lọc theo người sở hữu; Admin thấy tất cả. (`DashboardServlet.java:21,38-61`)
  2. Hệ thống tải dữ liệu ma trận từ `PlagiarismReports` qua `PlagiarismDAO.getSimilarityMatrix`. (`PlagiarismDAO.java:151-175`)
  3. Hiển thị **bảng ma trận $N \times N$ dạng số**. `⏳` Việc tô màu theo dải cảnh báo (đỏ $\ge 75\%$ / cam $50–74\%$ / xanh ngọc an toàn) **CHƯA TRIỂN KHAI — DỰ KIẾN W4–9**; hiện các ô chỉ hiển thị giá trị số.
  4. Hiển thị số liệu theo phạm vi người xem: số khóa học, số bài tập, số bài nộp, số báo cáo, số cặp vượt ngưỡng, điểm trung bình — tất cả đọc trực tiếp từ CSDL, không dùng dữ liệu mẫu. (`DashboardServlet.java:92-110`)
  5. Cho phép lọc danh sách bài nộp theo trạng thái `PENDING | PARSED | ANALYZED | FLAGGED`; giá trị lọc không hợp lệ ⇒ HTTP 400. (`DashboardServlet.java:82-89`)
* **Acceptance Criteria:**
  - **[AC-REP-01]** Màn hình hiển thị điểm tương đồng trên từng ô của ma trận, kèm danh sách báo cáo có tên hai sinh viên; mỗi dòng có liên kết mở `/diff-inspector?reportId=<id>`.
  - **[AC-REP-04]** Số liệu trên Dashboard phải khớp với kết quả truy vấn SQL độc lập trên cùng CSDL tại cùng thời điểm.
  - **[AC-REP-05]** Giảng viên A truy cập `?courseId=` của giảng viên B phải nhận HTTP 403; Admin truy cập được mọi khóa học. (`DashboardServlet.java:58-60`)

### Feature FE-06.2: So sánh Trực quan Song song (Side-by-Side Code Diff Viewer) — `🟡 MỘT PHẦN (W1–3)`
* **Purpose:** (Mục tiêu giữ nguyên) Giúp giảng viên đối chứng từng dòng code giữa 2 sinh viên nghi vấn vi phạm.
* **User/Actor:** `INSTRUCTOR`, `ADMIN` (xem đầy đủ); `STUDENT` (chỉ xem kết quả của chính mình, đã redaction).
* **Preconditions:** Chọn 1 bản ghi từ danh sách `PlagiarismReports`.
* **Main Flow (hiện trạng):**
  1. Mở `/diff-inspector?reportId=<id>` (`DiffInspectorServlet`). Thiếu/sai `reportId` ⇒ 400; báo cáo không tồn tại ⇒ 404. (`DiffInspectorServlet.java:12,20-24`)
  2. Kiểm tra quyền: giảng viên/Admin phải quản lý khóa học chứa báo cáo; sinh viên chỉ được xem nếu sở hữu một trong hai bài nộp. Không đạt ⇒ 403. (`DiffInspectorServlet.java:25-38`)
  3. Hiển thị bản ghi được chọn: ID, điểm, mức rủi ro, nhận định, và **danh sách các khối mã đã lưu** trong `MatchingBlocks`.
  4. Với sinh viên: chuyển sang `WEB-INF/views/student-result.jsp` — chỉ hiện điểm và thời gian, **không** cung cấp tên hay mã nguồn của người đối chiếu. (`DiffInspectorServlet.java:31-36`)
* **Hạn chế đã biết (`🟡`):**
  - `⏳` Giao diện 2 cột (trái/phải) và đồng bộ thanh cuộn **CHƯA TRIỂN KHAI — DỰ KIẾN W4–9**. Trang hiện là danh sách một cột.
  - `⏳` Tô màu khối mã vi phạm và popover ghi chú **CHƯA TRIỂN KHAI — DỰ KIẾN W4–9**.
* **Acceptance Criteria:**
  - **[AC-REP-02]** Trang hiển thị đúng bản ghi được chọn: ID báo cáo, tên hai sinh viên, điểm, mức rủi ro, thời gian và các khối mã đã lưu. Sinh viên không nhận được bất kỳ dữ liệu nhận dạng nào của bạn học.
  - *Tiêu chí về 2 cột và đồng bộ cuộn chưa được kiểm chứng (tính năng chưa triển khai).*

### Feature FE-06.3: Xuất Báo cáo Thẩm định Học thuật (Audit Report Export)
* **Purpose:** Xuất biên bản thẩm định chính thức kèm đầy đủ bằng chứng đối chiếu để giảng viên lưu trữ hoặc gửi lên Hội đồng Kỷ luật.
* **User/Actor:** `INSTRUCTOR`.
* **Preconditions:** Báo cáo đối soát đã hoàn tất.
* **Main Flow:**
  1. Nhấn "Xuất CSV" — gọi `GET /export-report?assignmentId=<id>&format=csv`. (`ExportReportServlet.java:19,38`)
  2. Hệ thống kiểm tra quyền: sinh viên chỉ xuất được bài tập mà mình có nộp bài; giảng viên/Admin phải quản lý khóa học chứa bài tập. Không đạt ⇒ 403. (`ExportReportServlet.java:39-46`)
  3. Xuất file CSV (có BOM UTF-8) gồm các cột: `report_id, similarity_score, risk_level, created_at, student_a_name, student_b_name, ai_analysis_summary`. Với sinh viên, mọi cột nhận dạng được thay bằng `REDACTED`. (`ExportReportServlet.java:47-64`)
  4. **Chống CSV injection:** giá trị bắt đầu bằng `=`, `+`, `-`, `@` được thêm prefix `'` trước khi ghi, tránh bị Excel hiểu thành công thức.
* **Hạn chế đã biết:**
  - `⏳` Xuất PDF, chữ ký số/mã băm xác thực, và nhúng SHA-256 của hai bài nộp: **CHƯA TRIỂN KHAI — DỰ KIẾN W4–9**. Tham số `format` khác `csv` bị từ chối với HTTP 400.
* **Acceptance Criteria:**
  - **[AC-REP-03]** File CSV chứa đầy đủ tên hai sinh viên, tỷ lệ trùng lặp, mức rủi ro và nhận định đối với tài khoản giảng viên/Admin; sinh viên nhận bản có redaction.
  - **[AC-REP-06]** Một giá trị trong CSV bắt đầu bằng `=` hoặc `@` phải được ghi ra với prefix `'`, không bị trình bảng tính thực thi như công thức.
  - *Tiêu chí về PDF và chữ ký số chưa được kiểm chứng (tính năng chưa triển khai).*

---

## MOD-07: Trình diễn 3D & Giao diện Hiện đại (Presentation & 3D Layer)

### Feature FE-07.1: Landing Page Scrollytelling (canvas 2D theo khung hình)
* **Purpose:** Thể hiện đẳng cấp công nghệ, trực quan hóa sứ mệnh bảo vệ liêm chính học thuật khi người dùng cuộn trang chủ.
* **User/Actor:** Tất cả người dùng truy cập trang chủ (`/` hoặc `/index.jsp`).
* **Main Flow:**
  1. Người dùng mở trang web.
  2. `scrollytelling-engine.js` lấy **context 2D** của `<canvas id="scrolly-canvas">` — **không dùng WebGL**.
  3. Engine nạp lần lượt các ảnh khung hình `web/assets/frames/frame_{index}.webp` (240 khung) và vẽ theo tiến trình cuộn.
  4. HUD đi kèm nằm tại `scrollytelling-hud.js`, hiệu ứng âm thanh tại `cyber-audio.js`.
* **Làm rõ (đã kiểm chứng bằng trình duyệt ngày 18/09/2026):** trang chủ **không** tải Three.js. Mô hình 3D chỉ xuất hiện ở `login.jsp` và `batch-scanner.jsp` — hai trang này tải Three.js r128 từ CDN kèm `GLTFLoader` và dựng `cyber-shield.glb` qua `cyber-shield-3d.js`. Không có `three_controller.js`.
* **Acceptance Criteria:**
  - **[AC-3D-01]** Mô hình 3D render ổn định ở mức tối thiểu 55-60 FPS trên trình duyệt Chrome/Edge có hỗ trợ WebGL.

### Feature FE-07.2: Giao diện Quản trị Phong cách Liquid Glass (UI/UX 2026)
* **Purpose:** Đạt điểm tuyệt đối tiêu chí UI/UX (10/10 điểm Rubric), mang lại trải nghiệm phần mềm cao cấp, hiện đại, mượt mà.
* **Main Flow:**
  1. Áp dụng hiệu ứng nền mờ gương kính (Liquid Glass Material). File CSS là `web/assets/css/liquid-glass-2026.css`, sử dụng `backdrop-filter: blur(20px) saturate(180%)`.
  2. Bố cục Responsive chuẩn mực hiển thị sắc nét trên cả màn hình Desktop và Tablet.
* **Acceptance Criteria:**
  - **[AC-UI-01]** Không có hiện tượng vỡ layout, tràn ngang màn hình trên các độ phân giải phổ biến từ 1366x768 đến 1920x1080.

---

# 4. YÊU CẦU PHI CHỨC NĂNG & MẶT CẮT HỆ THỐNG (CROSS-CUTTING REQUIREMENTS)

### 4.1. Bảo mật (Security - NFR-SEC)
* **[NFR-SEC-01] Phòng chống SQL Injection:** Toàn bộ truy vấn CSDL trong tầng DAO bắt buộc 100% sử dụng `PreparedStatement` với tham số đại diện `?`. Tuyệt đối cấm nối chuỗi SQL.
* **[NFR-SEC-02] Phòng chống XSS (Cross-Site Scripting):** `✅` — mọi dữ liệu do người dùng kiểm soát khi hiển thị lên JSP đều được escape bằng `<c:out value="${...}"/>` hoặc `fn:escapeXml(...)`. Trang JSP không dùng scriptlet (ngoại lệ duy nhất là `redirect-old.jsp` chỉ để chuyển hướng).
  - Các điểm từng **chưa** escape đã được xử lý: `login.jsp` (email phản hồi khi đăng nhập sai, đổ vào thuộc tính `value`), `index.jsp` (`fullName` do người dùng tự đặt), `includes/header.jsp` (`${param.title}` trong `<title>`; tệp này hiện không được include ở đâu nhưng vẫn được escape để không trở thành rủi ro khi có người dùng lại).
  - Dữ liệu dạng số (ID, điểm, hash hex) được phép render trực tiếp vì không chứa ký tự điều khiển.
* **[NFR-SEC-03] Phòng chống Zip Slip & Path Traversal:** `⏳ CHƯA TRIỂN KHAI — DỰ KIẾN W4–9` (phụ thuộc FE-03.3). Hiện chưa có mã giải nén ZIP nào, nên chưa có điểm cần bảo vệ.
  - Đã có sẵn: tên tệp tải lên được đặt lại theo `sub_<userId>_<timestamp>_<tên gốc>` và chỉ lấy phần tên tệp (`Paths.get(...).getFileName()`), loại bỏ thành phần đường dẫn do client gửi lên. (`SubmissionServlet.java:72,91`)
* **[NFR-SEC-04] Mã hóa Mật khẩu:** `✅` Mật khẩu mới được băm bằng **PBKDF2-HMAC-SHA256** kết hợp salt ngẫu nhiên 16 byte trước khi lưu vào CSDL; MD5/SHA-256 chỉ để xác thực dữ liệu legacy và được tự động nâng cấp. (`PasswordUtil.java:19-27`)
* **[NFR-SEC-05] Làm cứng phiên & xử lý lỗi:** `✅` Cookie phiên `JSESSIONID` được cấu hình `HttpOnly` và chỉ truyền qua cookie (không URL rewriting) trong `web/WEB-INF/web.xml`. Các mã lỗi 403/404/500 và ngoại lệ chưa bắt được được chuyển về `error.jsp`, không hiển thị stack trace cho người dùng.

### 4.2. Hiệu năng & Tải (Performance - NFR-PERF)
* **[NFR-PERF-01] Tốc độ phản hồi Web:** Thời gian phản hồi trung bình (Response Time) cho các tác vụ CRUD thông thường phải $\le 500$ms trên Apache Tomcat 10.1.
* **[NFR-PERF-02] Tốc độ Lõi Đối soát:** Thuật toán tính toán ma trận tương đồng cục bộ cho lớp học 50 sinh viên ($C(50, 2) = 1,225$ phép so sánh) phải hoàn thành trong $\le 5$ giây.
* **[NFR-PERF-03] Quản lý Kết nối CSDL:** `✅` Dùng connection pool **HikariCP**, tối đa 10 kết nối, tối thiểu 2 kết nối nhàn rỗi (cấu hình được qua `DB_POOL_MAX`, `DB_POOL_MIN_IDLE`). (`config/DBContext.java`)

### 4.3. Tính Tin cậy & Khả năng Phục hồi (Reliability & Resilience - NFR-REL)
* **[NFR-REL-01] Tính toàn vẹn giao dịch (ACID):** `🟡 MỘT PHẦN (W1–3)` — các thao tác ghi **nhiều bước** được bọc trong transaction:
  - Liên kết tài khoản Google (`UserDAO.authenticateGoogle`): `setAutoCommit(false)` + `UPDLOCK, HOLDLOCK` + `commit`/`rollback`. (`UserDAO.java:16-43`)
  - Quét đối soát (`PlagiarismEngineService.scanAssignment`): một giao dịch duy nhất cho toàn bộ lượt quét, có khóa `UPDLOCK, HOLDLOCK` trên `Assignments`.
  - Các thao tác CRUD đơn lẻ (tạo/sửa/xóa khóa học, bài tập, bài nộp) **không** bọc transaction — mỗi câu lệnh là một giao dịch tự nhiên của CSDL.
* **[NFR-REL-02] Độc lập với Lỗi Ngoại vi:** Nếu kết nối Google Gemini API bị gián đoạn, hệ thống vẫn lưu trữ đầy đủ điểm số tương đồng của lõi Java, hiển thị trạng thái cảnh báo thay vì làm sập ứng dụng.

### 4.4. Khả năng Mở rộng & Tương thích (Scalability & Compatibility - NFR-COMPAT)
* **[NFR-COMPAT-01] Môi trường Thực thi:** Tương thích 100% với Java 17 LTS và Java 21 LTS; Apache Tomcat 10.1+; Microsoft SQL Server 2019 trở lên (hoặc MySQL 8.0+).
* **[NFR-COMPAT-02] Mã hóa Ký tự (UTF-8):** Toàn bộ bộ lọc Filter, kết nối CSDL và trang JSP bắt buộc cấu hình `UTF-8` không có BOM để hiển thị tiếng Việt có dấu chuẩn xác 100%.

### 4.5. Ghi vết & Giám sát (Logging & Audit Trail - NFR-LOG)
* **[NFR-LOG-01] Nhật ký Hệ thống:** `🟡 MỘT PHẦN (W1–3)`. Logger được dùng ở `GeminiPlagiarismService` (mức `WARNING` khi gọi API lỗi). Chưa có cấu hình log 4 mức độ (`DEBUG`/`INFO`/`WARN`/`ERROR`) trên toàn hệ thống.
* **[NFR-LOG-02] Vết kiểm toán Nộp bài:** `⏳ CHƯA TRIỂN KHAI — DỰ KIẾN W4–9`. Chưa có cơ chế ghi log kèm Timestamp, User ID và địa chỉ IP cho hành vi nộp bài hay quét đạo văn.

---

# 5. CẤU TRÚC PHÂN RÃ CÔNG VIỆC (WORK BREAKDOWN STRUCTURE - WBS)

Bảng phân rã công việc chi tiết thành 16 gói công việc (Work Packages) độc lập, ánh xạ trực tiếp tới 5 thành viên nhóm phát triển theo chuyên môn:

| Gói Công Việc | Tiêu Đề & Mục Tiêu | Sản Phẩm Bàn Giao **Thực Tế** | Người Chịu Trách Nhiệm | Phụ Thuộc | Trạng Thái |
| :--- | :--- | :--- | :--- | :--- | :--- |
| **WP-01** | Khởi tạo CSDL & Tầng kết nối | Script SQL Server 3NF (6 bảng + 2 trigger); `config/DBContext.java`; dữ liệu mẫu. **Chưa có connection pool.** Kiểm chứng kết nối nằm trong `DatabaseFailureRegressionTest` (không có lớp `DBContextTest`). | **Nguyễn Tiến** *(Database Engineer)* | Không | `🟡` |
| **WP-02** | Mô hình dữ liệu | Bộ 6 Java Beans: `User`, `Course`, `Assignment`, `Submission`, `PlagiarismReport`, `MatchingBlock`. | **Nguyễn Tiến** *(Database Engineer)* | WP-01 | `✅` |
| **WP-03** | Tầng truy xuất dữ liệu | `UserDAO`, `CourseDAO`, `AssignmentDAO`, `SubmissionDAO`, **`PlagiarismDAO`** (tên `ReportDAO` không tồn tại). 100% `PreparedStatement`. | **Đinh Vũ Phương Khánh** *(Backend Dev)* | WP-02 | `✅` |
| **WP-04** | Xác thực & phân quyền RBAC | `LoginServlet`, `LogoutServlet`, **`AuthFilter`** (một filter duy nhất, thay cho `AuthenticationFilter`/`AuthorizationFilter`), `PasswordUtil`, `JWTUtil`, `GoogleIdentityVerifier`. | **Đinh Vũ Phương Khánh** *(Backend Dev)* | WP-03 | `✅` |
| **WP-05** | Landing page Scrollytelling + mô hình 3D | **`web/index.jsp`** (không có `index.html` ở web root); `scrollytelling-engine.js` (canvas 2D, 240 khung `frame_*.webp`). Mô hình 3D (`cyber-shield.glb`, Three.js r128) nằm ở `login.jsp` và `batch-scanner.jsp` qua `cyber-shield-3d.js`. Không có `three_controller.js`. | **Trần Văn Phúc** *(Frontend 3D Dev)* | Không | `✅` |
| **WP-06** | Giao diện Liquid Glass | `web/assets/css/liquid-glass-2026.css` (không có `liquid_glass.css`); `dashboard.jsp`, `student-portal.jsp`, `WEB-INF/views/dashboard-data.jspf`. | **Trần Văn Phúc** *(Frontend 3D Dev)* | WP-04 | `✅` |
| **WP-07** | Quản lý Khóa học & Bài tập | **`CourseActionServlet`**, **`AssignmentActionServlet`**, `service/AccessPolicy` (tên `CourseServlet`/`AssignmentServlet` không tồn tại). | **Đinh Vũ Phương Khánh** *(Backend Dev)* | WP-03, WP-06 | `✅` |
| **WP-08** | Tiếp nhận bài nộp & SHA-256 | `SubmissionServlet`, `SubmissionDAO`, `util/SHA256ChecksumUtil`, `config/StorageConfig`. | **Nguyễn Trần Anh Kiệt** *(Lead Architect)* | WP-03, WP-07 | `✅` |
| **WP-09** | Giải nén & tiếp nhận hàng loạt (ZIP) | **Chưa có mã nguồn.** Không có `BatchUploadServlet` hay `ZipExtractionService`. | **Nguyễn Trần Anh Kiệt** *(Lead Architect)* | WP-08 | `⏳ W4–9` |
| **WP-10** | Phân tích mã nguồn Java (lexer) | Nằm trong **`PlagiarismEngineService`** (`normalizeJavaCode`) — không có lớp `JavaLexerNormalizer` riêng. | **Nguyễn Hoài Nhi** *(Algorithm/NLP Dev)* | Không | `✅` |
| **WP-11** | Thuật toán đo tương đồng | Nằm trong **`PlagiarismEngineService`** (Jaccard + Levenshtein) — không có lớp `CodeSimilarityCalculator` riêng. | **Nguyễn Hoài Nhi** *(Algorithm/NLP Dev)* | WP-10 | `✅` |
| **WP-12** | Phân tích bài luận tiếng Anh (TF-IDF & Cosine) | **Chưa có mã nguồn.** Không có `TextPreprocessingService` hay `TfIdfVectorService`. | **Nguyễn Hoài Nhi** *(Algorithm/NLP Dev)* | Không | `⏳ W4–9` |
| **WP-13** | Quét ma trận đối soát toàn lớp | **`PlagiarismEngineService.scanAssignment`** + `BatchScannerServlet` (tên `PlagiarismScannerService` không tồn tại). Bóc tách `MatchingBlocks` ở mức minh hoạ, chưa dùng LCS. | **Nguyễn Trần Anh Kiệt** *(Lead Architect)* | WP-11 | `🟡` |
| **WP-14** | Tích hợp Gemini API | **`GeminiPlagiarismService`** (tên `GeminiApiService` không tồn tại); model `gemini-2.0-flash`; fallback gắn nhãn. | **Nguyễn Trần Anh Kiệt** *(Lead Architect)* | WP-13 | `🟡` |
| **WP-15** | Ma trận & Diff Viewer | `dashboard.jsp` + `WEB-INF/views/dashboard-data.jspf` (ma trận NxN dạng số), `diff-inspector.jsp` (một cột). Không có `heatmap_dashboard.jsp` hay `diff_viewer.jsp`. | **Trần Văn Phúc** *(Frontend 3D Dev)* | WP-06, WP-13 | `🟡` |
| **WP-16** | Xuất báo cáo & kiểm thử | `ExportReportServlet` (**chỉ CSV**); bộ test JUnit 5 (xem [Mục 0.3](#03-ảnh-chụp-hiện-trạng-snapshot)); `CHAY_ALL.bat`, `tools/run-java.ps1`. | **Nguyễn Tiến** *(QA & System Engineer)* | WP-01 → WP-15 | `🟡` |

---

# 6. MA TRẬN TRUY XUẤT NGUỒN GỐC (TRACEABILITY MATRIX)

Ma trận này liên kết Yêu cầu nghiệp vụ $\rightarrow$ Module $\rightarrow$ Feature $\rightarrow$ Gói công việc WBS $\rightarrow$ **Lớp/File thực tế** $\rightarrow$ Tiêu chí chấp nhận. Cột **Trạng thái** đối chiếu trực tiếp với [Mục 0](#0-trạng-thái-triển-khai-tính-đến-18092026).

| Mã Yêu Cầu | Module | Feature | WBS | Lớp / File Thực Thi **Thực Tế** | Tiêu Chí Chấp Nhận | Trạng Thái |
| :--- | :--- | :--- | :--- | :--- | :--- | :--- |
| **REQ-AUTH-01** | `MOD-01` | `FE-01.1` | WP-04 | `LoginServlet`, `UserDAO`, `PasswordUtil` | AC-AUTH-01, AC-AUTH-02, AC-AUTH-04 | `✅` |
| **REQ-AUTH-02** | `MOD-01` | `FE-01.2` | WP-04 | `AuthFilter`, `JWTUtil` | AC-AUTH-03, AC-AUTH-05, AC-AUTH-06 | `✅` |
| **REQ-AUTH-03** | `MOD-01` | `FE-01.3` | WP-04 | `UserProfileActionServlet` | — | `✅` |
| **REQ-CRS-01** | `MOD-02` | `FE-02.1` | WP-07 | `CourseActionServlet`, `CourseDAO` | AC-CRS-01, AC-CRS-02, AC-CRS-03 | `✅` |
| **REQ-ASN-01** | `MOD-02` | `FE-02.2` | WP-07 | `AssignmentActionServlet`, `AssignmentDAO`, `AccessPolicy` | AC-ASN-01, AC-ASN-02, AC-ASN-03 | `✅` |
| **REQ-SUB-01** | `MOD-03` | `FE-03.1` | WP-08 | `SubmissionServlet`, `SubmissionDAO`, `StorageConfig` | AC-SUB-01 | `🟡` |
| **REQ-SUB-02** | `MOD-03` | `FE-03.2` | WP-08 | `SHA256ChecksumUtil` | AC-SUB-02, AC-SUB-04 | `✅` |
| **REQ-SUB-03** | `MOD-03` | `FE-03.3` | WP-09 | *chưa có* | AC-SUB-03 | `⏳ W4–9` |
| **REQ-SIM-01** | `MOD-04` | `FE-04.1` | WP-10, WP-11 | `PlagiarismEngineService` | AC-SIM-01 | `✅` |
| **REQ-SIM-02** | `MOD-04` | `FE-04.2` | WP-12 | *chưa có* | AC-SIM-02 | `⏳ W4–9` |
| **REQ-SIM-03** | `MOD-04` | `FE-04.3` | WP-13 | `PlagiarismEngineService`, `PlagiarismDAO`, `BatchScannerServlet` | AC-SIM-03, AC-SIM-05, AC-SIM-06 | `✅` |
| **REQ-SIM-04** | `MOD-04` | `FE-04.4` | WP-13 | `PlagiarismEngineService` | AC-SIM-04 | `🟡` |
| **REQ-AI-01** | `MOD-05` | `FE-05.1` | WP-14 | `GeminiPlagiarismService` | AC-AI-01, AC-AI-03, AC-AI-04 | `🟡` |
| **REQ-AI-02** | `MOD-05` | `FE-05.2` | WP-14 | *chưa có* | AC-AI-02 | `⏳ W4–9` |
| **REQ-REP-01** | `MOD-06` | `FE-06.1` | WP-15 | `DashboardServlet`, `dashboard.jsp`, `WEB-INF/views/dashboard-data.jspf` | AC-REP-01, AC-REP-04, AC-REP-05 | `🟡` |
| **REQ-REP-02** | `MOD-06` | `FE-06.2` | WP-15 | `DiffInspectorServlet`, `diff-inspector.jsp`, `WEB-INF/views/student-result.jsp` | AC-REP-02 | `🟡` |
| **REQ-REP-03** | `MOD-06` | `FE-06.3` | WP-16 | `ExportReportServlet` | AC-REP-03, AC-REP-06 | `🟡` |
| **REQ-UI-01** | `MOD-07` | `FE-07.1` | WP-05 | `index.jsp`, `scrollytelling-engine.js`, `scrollytelling-hud.js`, `assets/frames/frame_*.webp` | AC-3D-01 | `✅` |
| **REQ-UI-03** | `MOD-07` | `FE-07.3` | WP-05 | `login.jsp`, `batch-scanner.jsp`, `cyber-shield-3d.js`, `assets/models/cyber-shield.glb` | AC-3D-02 | `✅` |
| **REQ-UI-02** | `MOD-07` | `FE-07.2` | WP-06 | `liquid-glass-2026.css`, `style.css` | AC-UI-01 | `✅` |

---

# 7. PHẠM VI NGOÀI TUẦN 1–3 (OUT OF SCOPE)

Các hạng mục dưới đây **nằm trong tầm nhìn dự án** nhưng **chưa có mã nguồn** tại thời điểm 18/09/2026. Chúng được giữ lại trong đặc tả để định hướng các giai đoạn tiếp theo, và **không được trình bày là đã hoàn thành**.

| Hạng mục | Tham chiếu | Dự kiến |
| :--- | :--- | :--- |
| Giải nén ZIP hàng loạt (batch upload) + chống Zip Slip | FE-03.3, NFR-SEC-03, WP-09 | Tuần 4–9 |
| Phân tích bài luận tiếng Anh bằng TF-IDF + Cosine Similarity | FE-04.2, WP-12 | Tuần 4–9 |
| Phân trang & tìm kiếm khóa học | SUB-02.1.2 | Tuần 4–6 |
| Khoá tài khoản sau 5 lần đăng nhập sai | FE-01.1 | Tuần 4–9 |
| Ghi log đăng nhập / vết kiểm toán kèm IP | FE-01.1, NFR-LOG-01, NFR-LOG-02 | Tuần 4–9 |
| Xuất PDF + chữ ký số / mã băm xác thực | FE-06.3, AC-REP-03 | Tuần 4–9 |
| Diff viewer 2 cột đồng bộ thanh cuộn | FE-06.2 | Tuần 4–9 |
| Tô màu heatmap theo dải cảnh báo | FE-06.1 | Tuần 4–9 |
| Nhận diện nội dung do LLM sinh (Burstiness) | FE-05.2, WP-14 | Tuần 7–9 |
| Bóc tách MatchingBlocks bằng LCS (toạ độ thật) | FE-04.4 | Tuần 4–9 |
| ~~Connection pooling~~ | NFR-PERF-03 | **Đã hoàn thành** (HikariCP) |
| Quan hệ enrolment (sinh viên đăng ký khóa học) | Mục 1.2 | Tuần 4–6 |

---
*Tài liệu này được kiểm soát phiên bản cùng mã nguồn. Mọi thay đổi về hành vi hệ thống phải được phản ánh vào bảng trạng thái tại [Mục 0](#0-trạng-thái-triển-khai-tính-đến-18092026) trước khi nộp.*

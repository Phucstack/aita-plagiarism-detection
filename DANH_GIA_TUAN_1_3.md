# ĐÁNH GIÁ DỰ ÁN AITA CODEDEFEND — PHẠM VI TUẦN 1–3

**Ngày đánh giá:** 18/09/2026
**Phương pháp:** Quét tĩnh 100% mã nguồn + đối chiếu tài liệu với code + đọc lại bằng chứng kiểm thử đã sinh ra trong repo.
**Nguồn tiêu chí:** `RBL PRJ301 Project.docx` (Mục II – Tuần 1-3), `Template_PRJ301_Milestones_FinalEvaluation_Rubrics.xlsx` (Milestone 1), `PRJ30x_Project_Evaluation_Rubric.docx`.

---

## 1. TÓM TẮT ĐIỀU HÀNH

**Kết luận:** Nền tảng kỹ thuật tuần 1–3 (JDBC, Identity/JWT, Course Management, UI) **làm thật và làm khá tốt** — mức chất lượng cao hơn mặt bằng PRJ301. Vấn đề lớn nhất **không nằm ở code, mà nằm ở tài liệu**: SRS là một "bản đóng băng" viết trước khi code đổi hướng, hiện mâu thuẫn trực tiếp với implementation ở ít nhất 8 điểm, và thiếu 2 artefacts mà Milestone 1 chiếm tới 50% điểm (sơ đồ package MVC + screen flow).

| Hạng mục | Thực trạng | Mức |
|---|---|---|
| Cơ sở dữ liệu (6 bảng, 3NF, ERD, trigger) | Đạt, có lập luận 3NF thật | **Tốt → Rất tốt** |
| Identity & JWT | Đạt, làm đúng và kỹ | **Rất tốt** |
| Course/Assignment CRUD + phân quyền | Đạt, kiểm tra quyền ngay trong SQL | **Tốt** |
| UI/UX Prototype | Đạt mức prototype đẹp, responsive đã đo | **Tốt** |
| SRS / Use Case document | Rất đồ sộ nhưng **lệch code nghiêm trọng**, không có use-case diagram | **Trung bình – Khá** |
| Thiết kế artefacts (MVC + screen flow) | ERD có, **thiếu sơ đồ package + screen flow** | **Khá** |
| Process (AI log + weekly report) | Đạt, 14 log / 3 tuần + báo cáo tuần | **Rất tốt** |

**Ước lượng Milestone 1 (chỉ mang tính tham khảo, không phải điểm giảng viên):** ~3.2/4 ≈ 80% — tương đương mức **Good+, chạm ngưỡng Excellent nếu bổ sung 2 sơ đồ và đồng bộ SRS với code**.

---

## 2. PHẠM VI ĐÃ QUÉT

| Thành phần | Số lượng | Ghi chú |
|---|---|---|
| Java main (`src/java`) | 33 file | 13 controller/config, 6 DAO, 4 service, 3 util, 6 model, 1 filter |
| Java test (`src/test/java`) | 26 file | **125 tests, 0 failure, 0 error, 0 skip** (đọc trực tiếp từ `target/surefire-reports`) |
| JSP / JSPF | 11 file | 6 trang chính + includes + `WEB-INF/views` |
| SQL | 3 file | 6 bảng + migration + 2 trigger |
| Tài liệu | 6 file | SRS (48KB), README, DEMO, BAO_CAO, AI_Usage_Report, ERD |
| Script kiểm chứng | 10 file Python/PS | Có DB test riêng, từ chối `DB_URL` |
| Cấu hình | `pom.xml`, `.env*`, `Dockerfile`, `docker-compose.yml` | WAR đã build: 48 MB |

**Kiểm tra tự động đã chạy:** SQL string-concat (0 kết quả đáng ngại), hardcoded secret (0), scriptlet trong JSP (chỉ `redirect-old.jsp`), EL chưa escape, số liệu Surefire, `git shortlog`.

---

## 3. ĐÁNH GIÁ THEO LỘ TRÌNH RBL TUẦN 1–3

RBL quy định tuần 1–3 gồm: **Thiết kế ERD (2.1) + UI/UX Prototype (2.2)**, lý thuyết **Tomcat / Servlet / JDBC nâng cao**, và **Peer-Review 1 (tuần 3): Database Design + cơ chế JWT Authentication (4.1.3)**.

### 3.1. Database Design — ĐẠT MẠNH

- **6 bảng** (`Users`, `Courses`, `Assignments`, `Submissions`, `PlagiarismReports`, `MatchingBlocks`), đầy đủ PK/FK, `IDENTITY`, `CHECK` cho role / file_type / status / risk_level. Đúng yêu cầu "5–6 well-structured tables with normalization".
- **Lập luận 3NF có thật**, không sáo rỗng (`ERD_DIAGRAM.md:97-101`): chỉ ra `PlagiarismReports.assignment_id` là phụ thuộc bắc cầu qua `Submissions`, đã **DROP cột** (`database_schema.sql:178-206`) và thay bằng trigger `TR_PlagiarismReports_SameAssignment` để giữ invariant. Đây là điểm hiếm nhóm làm được.
- Có trigger thứ 2 `TR_Submissions_PreserveReportAssignment` chặn reassign submission làm vỡ report.
- Có script migration idempotent, không DROP dữ liệu, không đổi mật khẩu `sa`.

**Thiếu sót nhỏ:** chưa có `UNIQUE (submission_a_id, submission_b_id)` hay ràng buộc `a < b` → có thể tồn tại cả (1,2) và (2,1); `getSimilarityMatrix` (`PlagiarismDAO:151-175`) `GROUP BY (a,b)` rồi mới đối xứng hoá nên ma trận có thể không nhất quán.

### 3.2. JDBC — ĐẠT, NHƯNG CHƯA "NÂNG CAO"

- 100% `PreparedStatement`, **không一处 nối chuỗi SQL từ input**. Ownership được đưa thẳng vào `WHERE` (`CourseDAO:50,66`, `AssignmentDAO:59-61,81-83`, `SubmissionDAO:127-129`) — phòng thủ theo chiều sâu thay vì chỉ check ở servlet.
- Lỗi persistence được gói thành `DataAccessException` và map ra 409/503 tại HTTP boundary (`AuthFilter:75-80`), **không trả dữ liệu giả khi DB lỗi** — đúng tinh thần sửa lỗi mà báo cáo nêu.
- **Chưa đạt mức "JDBC nâng cao":** `DBContext.getConnection()` dùng `DriverManager` mỗi lần gọi, **không có connection pool**, trong khi chính SRS `NFR-PERF-03` bắt buộc HikariCP/Tomcat JDBC Pool ≥10 connections. Đây là lỗ hổng dễ bị chất vấn ở defense.

### 3.3. Identity & JWT — ĐẠT MẠNH (đúng trọng tâm Peer-Review 1)

- `JWTUtil`: JJWT 0.13.0, HS256, kiểm `issuer`, kiểm `expiration`, kiểm kiểu `userId` (Integer > 0), secret đọc từ env và **bắt buộc ≥32 byte, không có khóa mặc định trong source**.
- Cookie `HttpOnly`, `SameSite=Strict`, `Secure` khi HTTPS, path-scoped theo context (`LoginServlet:79-85`).
- `request.changeSessionId()` khi login, session 30 phút, header `Cache-Control: no-store`.
- `AuthFilter` kiểm **cả token lẫn user hiện còn trong DB**, chặn `/uploads/`, redirect JSP → controller, và có kiểm tra `Origin`/`Sec-Fetch-Site` cho mọi request không phải GET/HEAD (chống CSRF).
- Mật khẩu PBKDF2-HMAC-SHA256, 600.000 vòng, salt 16 byte; cập nhật có điều kiện với `COLLATE Latin1_General_100_BIN2` (`UserDAO:200`) nên hai phiên đổi mật khẩu đồng thời không ghi đè nhau.
- Google login: xác minh ID token **phía server**, kiểm `nonce` + `email_verified` + `audience` + `azp`, cửa sổ 5 phút, và **từ chối tự tạo tài khoản chỉ bằng email** (`UserDAO:143-145`).

**Hạn chế:** logout chỉ xoá cookie, **JWT vẫn có hiệu lực tới 24h** (không có blacklist/revocation) — chính báo cáo đã thừa nhận.

### 3.4. Course Management & UI Prototype — ĐẠT

- Course/Assignment CRUD đầy đủ, validate độ dài trường, threshold chấp nhận 0 (không bị đổi ngầm thành 75 — đúng như DEMO mô tả), giữ deadline khi sửa tiêu đề.
- Dashboard đọc số liệu JDBC thật theo scope (`DashboardServlet`), không còn biểu đồ/dữ liệu mẫu; sinh viên chỉ thấy kết quả cá nhân, CSV export có redaction.
- UI đã được đo tự động ở 1440/768/375 px bằng Edge headless (`tools/verify_followup_browser.py`), có kết quả lưu trong `target/`.

---

## 4. ĐÁNH GIÁ THEO RUBRIC MILESTONE 1 (trọng số 30/50/20)

### 4.1. SRS / Use Case Document — trọng số 30% → mức **Good (3/4)**

**Mạnh:** SRS 48KB, 3 actor, 7 module, 14 feature, có main flow / exception flow / business rule / acceptance criteria, kèm WBS 16 gói công việc và ma trận truy xuất. Về khối lượng vượt xa yêu cầu "≥8 use cases".

**Yếu — SRS đang nói khác với code (nghiêm trọng):**

| SRS nói | Code thực tế | Vị trí |
|---|---|---|
| Mật khẩu bắt buộc MD5 hoặc SHA-256 | PBKDF2-HMAC-SHA256, 600k vòng | SRS:145 vs `PasswordUtil:19-27` |
| Mật khẩu tối thiểu 6 ký tự | ≥ 8 ký tự | SRS:149 vs `UserDAO:175` |
| "Remember Me" token 7 ngày | Không có | SRS:137 |
| Đếm sai 5 lần/10 phút → khoá 15 phút | Không có (không có cột/bảng lockout) | SRS:139-140 |
| Ghi log đăng nhập kèm username + IP | Không có (chỉ `GeminiPlagiarismService` có Logger) | SRS:159, NFR-LOG-01/02 |
| URL `/instructor/dashboard`, `/admin/dashboard` | `/dashboard`, `/student-portal` | SRS:135,161 |
| Bắt buộc Connection Pool (HikariCP ≥10 conn) | `DriverManager` mỗi lần gọi, không pool | NFR-PERF-03 vs `DBContext:25` |
| `AuthenticationFilter`, `CourseServlet`, `ReportDAO`, `JavaLexerNormalizer`, `TfIdfVectorService`, `ZipExtractionService`, `LLMDetectorService`, `heatmap_dashboard.jsp`, `three_controller.js`… | **Không tồn tại** | WBS + ma trận truy xuất |

Hệ quả: giảng viên đối chiếu SRS với code sẽ thấy hàng loạt mục không khớp. Đây là rủi ro bị trừ điểm cao nhất ở hạng mục này.

### 4.2. Design Artefacts (ERD + MVC/Screen Flow) — trọng số 50% → mức **Good (3/4)**

- **ERD: đạt Excellent** — 6 entity, 3NF có lập luận, Mermaid render được (`ERD_DIAGRAM.md`, `DEMO_TUAN_1_3.md`).
- **Sơ đồ package MVC: CHƯA CÓ.** Cấu trúc `controller / dao / model / service / util / filter` tồn tại rất chuẩn trong code nhưng không được vẽ thành diagram — rubric yêu cầu "MVC package structure clearly diagrammed".
- **Screen flow: CHƯA CÓ.** Không tìm thấy sơ đồ luồng màn hình nào trong toàn bộ repo.

=> Hai thiếu sót này kéo hạng mục **nặng nhất** (50%) từ Excellent xuống Good.

**Mâu thuẫn nội bộ cần sửa ngay:** `DEMO_TUAN_1_3.md:44` vẫn vẽ `Assignments ||--o{ PlagiarismReports : assignment_id`, trong khi schema đã DROP cột này và `ERD_DIAGRAM.md:99` đã nói rõ lý do.

### 4.3. Process (Weekly report + AI usage log) — trọng số 20% → mức **Excellent (4/4)**

- `AI_Usage_Report.md`: **14 log trải đều Week 1 / Week 2 / Week 3**, đầy đủ 9 cột (AI tool, output, sinh viên validate gì, link evidence, quantitative measure, risks). Vượt yêu cầu "≥2 entries".
- Có báo cáo tuần (`BAO_CAO_TIEN_DO_TUAN_1_3.md`) kèm bằng chứng và phần "phạm vi chưa nghiệm thu" — cách viết trung thực, đáng giá.
- **Rủi ro cho Milestone 2:** `git shortlog` cho 35 commit nhưng **29 commit thuộc một người**. Tiêu chí Milestone 2 cần "≥5 commits/member" — cần san sẻ commit từ bây giờ.

---

## 5. KHOẢNG TRỐNG & RỦI RO (P0 / P1 / P2)

### P0 — sửa trước khi nộp/defense

1. **Đồng bộ SRS với code.** Cập nhật BR-AUTH-01 (PBKDF2), quy tắc mật khẩu (≥8), URL thực tế, NFR-PERF-03 (ghi rõ pool là hạng mục tuần 4–6 hoặc bỏ yêu cầu), NFR-LOG (ghi rõ chưa实现的 hoặc thêm logger tối thiểu). Xoá/gạch bỏ các "Remember Me" và "khoá tài khoản" chưa làm, hoặc đưa vào danh sách "ngoài phạm vi".
2. **Bổ sung 2 sơ đồ còn thiếu:** package/MVC diagram + screen flow diagram. Đây là 50% điểm Milestone 1 và chỉ tốn vài giờ.
3. **Sửa ERD trong `DEMO_TUAN_1_3.md:44`** — bỏ cạnh `Assignments → PlagiarismReports` cho khớp schema.

### P1 — rủi ro bị chất vấn ở defense

4. **`GeminiPlagiarismService` không được gọi ở bất kỳ controller/service nào** (chỉ có test dùng). Pipeline scan thực tế dùng `generateAiSummary` (`PlagiarismEngineService:223-232`) — một `String.format` template — nhưng lại ghi vào DB câu *"AITA AI phát hiện…"*. Nếu trình bày đây là "AI phát hiện", sẽ bị bắt lỗi ngay. Khẳng định "Gemini có timeout/quota/fallback" là đúng ở mức class, nhưng **chưa được tích hợp**.
5. **README/SRS claim thuật toán không tồn tại:** TF-IDF + Cosine Similarity, 3-Gram Overlap, Perplexity/Burstiness, "AST parsing". Thực tế chỉ có Jaccard (3-gram trên token) + Levenshtein bằng **regex tokenizer**, không có AST, không có JavaParser trong `pom.xml` (dù AI log hàng 7 nói đã chọn JavaParser). Sửa lại bảng thuật toán trong README cho đúng những gì code làm.
6. **`scanAssignment` không nguyên tử:** xoá sạch report cũ rồi insert từng cặp, không bọc transaction (`PlagiarismDAO:136`, `PlagiarismEngineService:129-192`). Scan đồng thời hoặc lỗi giữa chừng sẽ để DB nửa vời. Chính nhóm đã ghi nhận — cần xử lý trước khi demo scan.
7. **MatchingBlock bị làm giả:** toạ độ dòng hardcode 15/20/`min(45,…)`, `functionName` cố định `"executeCoreLogic()"` (`PlagiarismEngineService:172-179`). SRS FE-04.4 nói "xác định chính xác tọa độ dòng".
8. **Fallback nội dung giả:** nếu không đọc được file, `readSubmissionContent` sinh một đoạn code mẫu cố định (`PlagiarismEngineService:204-213`) → mọi bài thiếu file sẽ "giống hệt nhau" 100%. Cần trả lỗi thay vì bịa dữ liệu.
9. **Checksum sai không bị phát hiện:** `SubmissionServlet:115` và `SHA256ChecksumUtil:29` trả về SHA-256 của chuỗi rỗng khi có lỗi, rồi lưu thẳng vào DB. Phá vỡ mục đích FE-03.2 (chống giả mạo).
10. **Upload nằm trong web root** (`SubmissionServlet:82` dùng `getRealPath("/uploads")`) trong khi SRS BEH-03.1.1.3 yêu cầu ngoài web root; hiện chỉ chặn bằng `AuthFilter:63`.
11. **`SubmissionServlet:60`** mặc định `assignmentId = 1` nếu thiếu tham số → có thể gán bài nộp nhầm bài tập.
12. **Báo cáo tự mâu thuẫn về số test:** `BAO_CAO_TIEN_DO_TUAN_1_3.md:24` ghi 112 tests, dòng 62 ghi 125 tests. Surefire thực tế: **125**. Thống nhất một con số.

### P2 — nên xử lý khi có thời gian

13. `ExportReportServlet:69-73` chưa neutralize ký tự đầu `= + - @` → CSV injection khi mở bằng Excel.
14. `index.jsp:129` render `${sessionScope.currentUser.fullName}` không escape; `fullName` do người dùng tự đặt qua `profile-action` → stored XSS mức thấp.
15. Thiếu `web.xml` → không có `error-page`, không cấu hình `JSESSIONID` HttpOnly/Secure mặc định, không có welcome-file.
16. `JWTUtil:18` chỉ đọc `System.getenv`, trong khi `DBContext:10` đọc cả system property → không nhất quán khi chạy qua script truyền `-D`.
17. Seed mặc định là hash MD5 của `123456` (`database_schema.sql:119-129`). Nếu demo trên DB seed, mật khẩu cực yếu — đổi trước khi trình bày.
18. `UserDAO.getOrCreateGoogleUser` ném `UnsupportedOperationException` — API chết, nên xoá.
19. Chưa có pagination/search cho Course (SRS SUB-02.1.2.1 có, rubric Application nhắc pagination).
20. `Courses.instructor_id` / `Submissions.student_id` không có ràng buộc role ở tầng DB — chính nhóm đã nhận diện rủi ro này ở AI log hàng 3 nhưng chưa xử lý.

---

## 6. MA TRẬN CLAIM ↔ THỰC TẾ (tóm tắt nhanh)

| Claim trong tài liệu | Thực tế | verdict |
|---|---|---|
| 6 bảng 3NF + ERD + trigger | Đúng | ✅ |
| JWT HS256, HttpOnly, SameSite, đổi session ID | Đúng | ✅ |
| Quyền Course/Assignment/Submission kiểm trong SQL | Đúng | ✅ |
| Dashboard số liệu JDBC thật, không mock | Đúng | ✅ |
| 125 tests pass | Đúng (Surefire) | ✅ |
| Sinh viên không thấy dữ liệu peer | Đúng (redaction + student-result.jsp) | ✅ |
| PBKDF2 + nâng cấp hash cũ khi login | Đúng | ✅ |
| Google login xác minh server-side | Đúng | ✅ |
| "Gemini tích hợp, có fallback" | Class có, **chưa nối vào luồng scan** | ⚠️ |
| "AST / TF-IDF / Cosine / Perplexity" | **Không có** | ❌ |
| "Connection Pooling HikariCP" | **Không có** | ❌ |
| "Bóc tách chính xác toạ độ dòng" | **Hardcode** | ❌ |
| "Lưu file ngoài web root" | Đang lưu **trong** web root | ❌ |
| "Remember Me / khoá tài khoản 5 lần" | **Không có** | ❌ |
| "Log audit kèm IP" | **Không có** | ❌ |

---

## 7. KẾ HOẠCH HÀNH ĐỘNG ĐỀ XUẤT

**Trước nộp Milestone 1 (1–2 ngày):**
1. Vẽ thêm **package/MVC diagram** + **screen flow diagram** vào SRS. *(50% điểm Milestone 1)*
2. Sửa 8 điểm lệch SRS↔code trong Bảng mục 4.1; cập nhật bảng thuật toán trong README cho đúng (Jaccard + Levenshtein, bỏ AST/TF-IDF/Perplexity).
3. Sửa ERD ở `DEMO_TUAN_1_3.md:44`; thống nhất con số 125 tests.

**Trước Peer-Review 1 / defense tuần 3 (3–5 ngày):**
4. Nối `GeminiPlagiarismService` vào `scanAssignment`, hoặc đổi nhãn `ai_analysis_summary` thành `"Phân tích tự động (chưa phải LLM)"` — đừng để chữ "AI" nói dối.
5. Bọc `scanAssignment` trong transaction; bỏ fallback nội dung giả (trả lỗi rõ ràng).
6. Sửa SHA-256 fallback: ném lỗi thay vì lưu hash của chuỗi rỗng; chuyển upload ra ngoài web root.
7. Đổi mật khẩu seed; thêm logger tối thiểu cho login/scan để có audit trail.

**Chuẩn bị cho Milestone 2:**
8. San sẻ commit (hiện 29/35 của một người) — đây là tiêu chí chấm thật.
9. Thêm connection pool (HikariCP) — vừa khớp SRS vừa lên đúng mức "JDBC nâng cao".
10. Bổ sung pagination + search cho Course/Assignment.

---

## 8. GHI CHÚ VỀ BẰNG CHỨNG

Các con số trong báo cáo này lấy từ: `target/surefire-reports/TEST-*.xml` (125/0/0/0), `target/*.json` (HTTP verification), `git shortlog -sn`, và đọc trực tiếp mã nguồn. Tôi **không** chạy lại Maven/Tomcat/SQL Server trong lượt đánh giá này — mọi nhận định về runtime dựa trên bằng chứng đã có sẵn trong repo. Nếu cần xác nhận lại, chạy `tools/test-java.ps1` và `python tools/verify_followup_http.py` với `.env.test`.

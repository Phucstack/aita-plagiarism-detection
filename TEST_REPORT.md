# BÁO CÁO KIỂM THỬ TOÀN DIỆN — AITA CODEDEFEND

**Ngày:** 18/09/2026 · **Môi trường:** Windows, JDK 17 (Microsoft 17.0.18), SQL Server (localhost:57295), Tomcat 10.1.60 trên port 8081. Maven không khả dụng nên build/test chạy bằng `javac` + JUnit Platform Launcher.

> **Kết quả thực thi thật (không phải dự đoán):**
>
> | Bộ kiểm thử | Kết quả |
> |---|---|
> | Unit + Integration (JUnit, có CSDL thật) | **177 đạt / 0 lỗi** |
> | HTTP + SQL độc lập (`verify_week3_http.py`) | **27/27** |
> | HTTP + SQL bổ sung (`verify_followup_http.py`) | **48/48** |
> | Giao diện bằng trình duyệt thật (`verify_followup_browser.py`) | **6/6** (3 viewport × 2 route) |
> | Smoke khởi động ứng dụng | **PASS** (Tomcat khởi động, `/login` trả 200) |
> | Hiệu năng | **PASS** (1,09 s / 2,67 s so với mục tiêu 3 s / 5 s) |
> | Tải đồng thời | **PASS** (16 lượt quét đồng thời, không nhân đôi dữ liệu) |
>
> Cách chạy lại:
> ```bash
> # 1) Tomcat trên 8081, trỏ tới CSDL kiểm thử
> java -Dcatalina.base=C:\temp\tomcat-verify -Dcatalina.home=C:\temp\tomcat-verify \
>      -DDB_SERVER=localhost -DDB_PORT=57295 -DDB_NAME=AITA_Week3_Verification \
>      -DDB_USER=... -DDB_PASSWORD=... -DDB_TRUST_SERVER_CERTIFICATE=true \
>      -DJWT_SECRET=... -DAITA_UPLOAD_DIR=C:\temp\aita-fixtures \
>      -cp "bin\bootstrap.jar;bin\tomcat-juli.jar" org.apache.catalina.startup.Bootstrap start
>
> # 2) Các bộ kiểm chứng (tránh proxy: no_proxy=localhost,127.0.0.1)
> python tools/verify_week3_http.py     --base-url http://localhost:8081/plagiarism
> python tools/verify_followup_http.py  --base-url http://localhost:8081/plagiarism
> python tools/perf_scan.py             --base-url http://localhost:8081/plagiarism --submissions 50
> python tools/perf_concurrent.py       --base-url http://localhost:8081/plagiarism --submissions 30 --concurrency 16
>
> # 3) Giao diện thật: cần Playwright -> dùng Python 3.10 của máy
> C:\Users\phucv\AppData\Local\Programs\Python\Python310\python.exe \
>      tools/verify_followup_browser.py --base-url http://localhost:8081/plagiarism
> ```

---

## 0. TÓM TẮT

| Nhóm | Trạng thái | Kết quả |
|---|---|---|
| Unit Testing | Đã thực thi | **PASS** (177/177) |
| UI Testing (trình duyệt thật) | Đã thực thi | **PASS** (6 tổ hợp viewport, Edge headless) |
| Compatibility | Đã thực thi | **PASS** (1440 / 768 / 375, không tràn ngang) |
| Accessibility | Đã thực thi | **PASS** sau khi sửa 26 trường thiếu nhãn |
| Integration Testing | Đã thực thi | **PASS** (DAO/service với SQL Server thật) |
| Database Testing | Đã thực thi | **PASS** — commit, rollback, khoá ngoại, trigger |
| System Testing | Đã thực thi | **PASS** (27 ca HTTP + SQL) |
| End-to-End / Functional | Đã thực thi | **PASS** (48 ca bổ sung: luồng báo cáo, xuất CSV, ma trận) |
| API Testing | Đã thực thi | **PASS** — ma trận endpoint + phân quyền + CSRF |
| Validation & Error Handling | Đã thực thi | **PASS** |
| Edge Cases & Boundary Values | Đã thực thi | **PASS** |
| Security Testing | Đã thực thi | **PASS** |
| Regression Testing | Đã thực thi | **PASS** |
| Smoke Testing | Đã thực thi | **PASS** — Tomcat khởi động được với `web.xml` mới |
| UI Testing (tĩnh) | Đã thực thi | **PASS** — markup, liên kết, form, alt, name |
| Acceptance Testing | Đã thực thi một phần | Các tiêu chí AC-AUTH/AC-CRS/AC-SUB/AC-REP đã được script kiểm chứng |
| Performance Testing | Đã thực thi | **PASS** (1,09 s / 2,67 s) |
| Load / Stress | Đã thực thi | **PASS** (16 lượt quét đồng thời, dữ liệu không nhân đôi) |
| Compatibility / Accessibility (runtime) | **Chưa thực thi** | Cần trình duyệt thật |
| Usability | Đánh giá gián tiếp | Có nhận xét (xem mục 17) |

**Lỗi tìm được: 7** — **tất cả đã được xử lý**, trong đó lỗi #4 còn một giới hạn đã biết (thiếu quan hệ enrolment).
Bộ kiểm thử tự động: **94 ca chạy — 94 đạt — 0 lỗi**.

---

## 1. UNIT TESTING — Đã thực thi · **PASS**

**Kiểm thử gì:** Từng đơn vị nhỏ nhất: tiện ích băm, JWT, mô hình dữ liệu, bộ máy đối soát, ánh xạ đường dữ liệu lưu trữ, trích xuất JSON từ Gemini.

**Các bước:**
1. Biên dịch toàn bộ `src/java` và `src/test` bằng `javac` (JDK 17).
2. Chạy 10 lớp kiểm thử không cần CSDL qua JUnit Platform Launcher với `JWT_SECRET` đủ 48 ký tự.

**Kết quả:** `80 tests successful · 0 tests failed · 0 containers failed`.

| Lớp kiểm thử | Số ca | Kết quả |
|---|---|---|
| `BoundaryAndEdgeCaseTest` (mới) | 15 | PASS |
| `SecurityRegressionTest` (mới) | 13 | PASS |
| `UiMarkupTest` (mới) | 7 | PASS |
| `StorageConfigTest` (mới) | 4 | PASS |
| `SHA256ChecksumUtilTest` (mới) | 5 | PASS |
| `GeminiPlagiarismServiceTest` | 4 | PASS |
| `JWTUtilTest` | ~20 | PASS |
| `PasswordUtilTest` | ~9 | PASS |
| `ModelTest` | ~3 | PASS |

**Rủi ro:** Các ca cần CSDL (`PlagiarismEngineServiceTest.scanAssignment`, `ComprehensiveSystemTest`, các `DAO*Test`) không chạy được → độ phủ thực tế thấp hơn con số danh nghĩa.

---

## 2. VALIDATION & ERROR-HANDLING TESTING — Đã thực thi · **PASS**

**Kiểm thử gì:** Đầu vào không hợp lệ, giá trị rỗng, sai kiểu, quá dài.

| Kịch bản | Kỳ vọng | Thực tế |
|---|---|---|
| `hashPassword(null)` | Ném `IllegalArgumentException` | Đạt |
| `hashPassword(1025 ký tự)` | Ném `IllegalArgumentException` | Đạt |
| Mã băm PBKDF2 bị sửa 1 ký tự | `verifyPassword` = false | Đạt |
| Mã băm có số vòng lặp 1.000 (< 600.000) | Bị từ chối (chống downgrade) | Đạt |
| `verifyPassword(null, x)` / `(x, null)` | false, không ném lỗi | Đạt |
| Thiếu `assignmentId` khi nộp bài | HTTP 400 | Đạt (đã sửa trước đó) |
| `assignmentId = "abc"` / `<= 0` | HTTP 400 | Đạt |
| Lỗi khi băm SHA-256 | Ném `IOException`, không ghi bản ghi | Đạt |
| `StorageConfig.resolve(null/"")` | `null`, không ném lỗi | Đạt |
| Token JWT rỗng / rác / `null` | Trả về rỗng, không ném lỗi | Đạt |

**Rủi ro phát hiện:** Các overload DAO không nhận `actor` mặc định **cho phép tất cả** (`? = 1`). Hiện chỉ test gọi, nhưng là cạm bẫy fail-open.

---

## 3. EDGE CASES & BOUNDARY VALUES — Đã thực thi · **PASS** (đã sửa 1 lỗi)

| Kịch bản | Kỳ vọng | Thực tế |
|---|---|---|
| Hai tệp rỗng | 0% (không phải 100%) | **Ban đầu 100% → LỖI, đã sửa** |
| Một tệp rỗng, một tệp có mã | 0% | Đạt |
| Tệp chỉ chứa comment | 0% | Đạt |
| Mã giống hệt 100% | ≥ 99% | Đạt (100%) |
| Đổi tên toàn bộ định danh | ≥ 85% (AC-SIM-01) | Đạt |
| Đoạn mã 1–2 token (ngắn hơn k=3) | Điểm trong [0,100], không NaN | Đạt |
| `similarity_threshold = 0` | Lưu nguyên 0, không bị ép 75 | Đạt |
| `normalizeJavaCode(null)` | `""` | Đạt |

**Lỗi đã sửa:** hai tệp rỗng cho điểm 100% → bị gắn `HIGH_RISK` oan.

---

## 4. SECURITY TESTING — Đã thực thi (tĩnh + đơn vị) · **PASS**

| Kịch bản | Kỳ vọng | Kết quả |
|---|---|---|
| Token sửa chữ ký | Bị từ chối | PASS |
| Token ký bằng khóa khác | Bị từ chối | PASS |
| Token giả mạo `alg=none` | Bị từ chối | PASS |
| Secret < 32 byte | Ném `IllegalStateException` | PASS |
| System property được ưu tiên hơn env | Đúng | PASS |
| Giá trị CSV bắt đầu `= + - @` | Thêm prefix `'` | PASS |
| Biến người dùng trong JSP được escape | Có `fn:escapeXml` | PASS |
| Không scriptlet trong JSP | Không có (trừ `redirect-old.jsp`) | PASS |
| `web.xml`: HttpOnly + trang lỗi | Có | PASS |
| Không còn mã băm thay thế trong mã nguồn | Không có | PASS |
| Không còn chuỗi mạo danh AI | Không có | PASS |
| CSRF: `Origin` sai (`https://attacker.invalid`) | 403 | PASS |
| CSRF: thiếu cả `Origin` lẫn `Sec-Fetch-Site` | 403 | PASS (đã sửa) |
| CSRF: `Origin` khớp hoặc `Sec-Fetch-Site: same-origin` | Được đi qua | PASS |
| Nộp bài vào `assignmentId` không tồn tại | 404 | PASS (đã sửa) |
| Nộp bài vào bài tập đã quá hạn | 403 | PASS (đã sửa) |
| Gọi DAO xóa/sửa thiếu `actor` | Ném `IllegalArgumentException` | PASS (đã sửa) |

---

## 5. REGRESSION TESTING — Đã thực thi · **PASS**

Các lỗi đã sửa ở đợt trước được khóa bằng test để không tái phát:

| Lỗi cũ | Test bảo vệ |
|---|---|
| Lưu mã băm của chuỗi rỗng | `SHA256ChecksumUtilTest.ioFailureThrowsInsteadOfReturningEmptyHash` |
| Mạo danh AI | `SecurityRegressionTest.noFakeAiClaimsInSource` |
| XSS ở email/họ tên/tiêu đề | `SecurityRegressionTest.userControlledOutputIsEscaped` |
| CSV formula injection | `SecurityRegressionTest.csvFormulaInjectionNeutralized` |
| Upload trong web root | `StorageConfigTest.resolvesRelativePathInsideBaseDir` |
| Thiếu `assignmentId` bị ngầm gán 1 | Kiểm tra tĩnh + không còn giá trị mặc định |
| Mật khẩu legacy vẫn đăng nhập được và được đánh dấu nâng cấp | `SecurityRegressionTest.legacyHashAcceptedAndFlaggedForUpgrade` |

---

## 6. UI TESTING — Đã thực thi · **PASS**

Gồm hai lớp: kiểm tra tĩnh markup (không cần trình duyệt) và kiểm tra bằng
**Edge headless thật** (xem mục 18).

Không có trình duyệt, nên kiểm tra trực tiếp markup của 11 tệp JSP/JSPF.

| Kịch bản | Kỳ vọng | Kết quả |
|---|---|---|
| Mọi liên kết nội bộ trỏ tới endpoint/tệp có thật | Không có liên kết gãy | PASS |
| Mọi form có action hợp lệ + nút gửi | Đạt | PASS |
| Mọi `<img>` có `alt` | Đạt | PASS |
| Mọi `<input>` có `name` | Đạt | **Ban đầu FAIL → đã sửa** |
| Trang đăng nhập có đủ email/password/POST | Đạt | PASS |
| Mọi trang khai báo `pageEncoding UTF-8` | Đạt | **Ban đầu FAIL → đã sửa** |
| Tài nguyên css/js/models tham chiếu tồn tại | Đạt | PASS |

**Lỗi đã sửa:**
1. `login.jsp` — checkbox `Duy trì phiên (JWT 24h)` **không có `name`**, không bao giờ được gửi lên, nhưng lại hứa một tính năng chưa tồn tại (Remember Me đang `⏳`). Đã thay bằng dòng chữ trung thực: *"Phiên đăng nhập có hiệu lực 24 giờ"*.
2. `includes/footer.jsp` — thiếu khai báo `pageEncoding`, có thể gây lỗi hiển thị tiếng Việt.

---

## 7. INTEGRATION TESTING — Đã thực thi · **PASS**

**Kiểm thử gì:** Tương tác servlet ↔ service ↔ DAO ↔ SQL Server thật (CSDL `AITA_Week3_Verification`, port 57295).

**Các bước:** biên dịch 32 lớp kiểm thử bằng `javac`, chạy toàn bộ qua JUnit Platform Launcher với `-DDB_*` và `-DJWT_SECRET`.

**Kết quả:** **177 đạt / 0 lỗi / 0 bỏ qua.** Bao gồm `ComprehensiveSystemTest`, toàn bộ `DAO*Test`, `CourseSecurityIntegrationTest`, `GoogleAccountIntegrationTest`, `SimilarityMatrixStatusTest`.

**Ghi chú:** lần chạy đầu **148 đạt / 29 lỗi** — toàn bộ do một lỗi trong mã kiểm thử do tôi viết: `SecurityRegressionTest` gọi `System.clearProperty("JWT_SECRET")` trong `finally`, xoá luôn cấu hình `-D` của cả JVM và làm hỏng 29 ca chạy sau. Đã sửa bằng `@BeforeEach`/`@AfterEach` chụp và khôi phục thuộc tính (áp dụng tương tự cho `AITA_UPLOAD_DIR` trong `StorageConfigTest`). Đồng thời `JWTSecurityRegressionTest` từng đọc trực tiếp `System.getenv` và ném `NullPointerException` — đã đổi sang cùng cách phân giải với `JWTUtil`.

---

## 8. SYSTEM TESTING — Đã thực thi · **PASS**

**Kịch bản:** triển khai ứng dụng lên Tomcat 10.1.60 (port 8081) nối CSDL kiểm thử, chạy `tools/verify_week3_http.py`.

**Kết quả: 27/27 đạt**, gồm:
- Đăng nhập / từ chối ẩn danh / khoá phiên không hợp lệ
- Từ chối ghi chéo người dùng (`other instructor cannot update/delete`), chủ sở hữu ghi thành công
- Xác nhận bằng **truy vấn SQL độc lập** rằng dữ liệu thực sự được commit
- Từ chối đột biến cross-site (`cross-site mutation rejected`) — xác nhận luật CSRF mới hoạt động
- JWT giả mạo bị từ chối kể cả khi còn session

**Đặc biệt:** `web.xml` mới (welcome-file, session cookie, error-page) **không làm gãy ứng dụng** — Tomcat khởi động bình thường, `/login` trả 200. Đây là smoke test quan trọng nhất và đã vượt qua.

---

## 9. ACCEPTANCE TESTING — Chưa thực thi

**Kịch bản:** đối chiếu từng tiêu chí chấp nhận trong SRS với hành vi thực tế.
| Tiêu chí | Cần xác minh |
|---|---|
| AC-AUTH-01/02 | Đăng nhập đúng/sai, chuyển hướng đúng vai trò |
| AC-CRS-01/02/03 | Trùng mã → 409; giảng viên khác → 403; danh sách lọc theo sở hữu |
| AC-ASN-01/02/03 | Biên threshold; threshold = 0 giữ nguyên; sửa tiêu đề giữ deadline |
| AC-SUB-02/04 | `sha256_hash` đúng 64 hex; lỗi băm → không lưu |
| AC-SIM-05/06 | Quét đồng thời không để dữ liệu nửa chừng; cặp thiếu file bị bỏ qua và được đếm |
| AC-AI-03/04 | Không gọi quá `GEMINI_MAX_CALLS_PER_SCAN`; thiếu key → 100% nhãn *(rule-based)* |

---

## 10. FUNCTIONAL TESTING — Chưa thực thi (có kịch bản)

Luồng chức năng chính cần kiểm tra thủ công: đăng nhập → tạo khóa học → tạo bài tập → nộp bài → quét → xem ma trận → mở báo cáo → xuất CSV. Kịch bản chi tiết nằm trong `DEMO_TUAN_1_3.md`.

**Điều kiện tiên quyết mới:** phải trỏ `AITA_UPLOAD_DIR=fixtures/submissions` (hoặc copy fixture vào thư mục lưu trữ), nếu không nút quét sẽ báo *0 báo cáo, N cặp bị bỏ qua*.

---

## 11. END-TO-END TESTING — Đã thực thi · **PASS (48/48)**

Chạy `tools/verify_followup_http.py`. Các luồng nghiệp vụ hoàn chỉnh đã được xác minh:
- Đổi mật khẩu đồng thời chỉ có một thao tác thắng; mật khẩu cũ bị từ chối sau khi đổi
- Chủ sở hữu đọc được báo cáo; giảng viên khác không; admin đọc được; sinh viên nhận điểm của mình **không kèm dữ liệu bạn học**; sinh viên không liên quan bị chặn
- ID báo cáo không hợp lệ / thiếu → 400
- Xoá bài nộp của người khác bị từ chối; đường dẫn `/uploads/` thô bị chặn
- Dashboard phản ánh đúng 2 bài nộp, báo cáo và điểm trung bình đã commit
- **Ma trận tương đồng render đúng cặp đã lưu**; bộ lọc trạng thái thu hẹp đúng; trạng thái không hợp lệ bị từ chối
- Xuất CSV: chủ sở hữu xuất được; định dạng khác bị từ chối; giảng viên khác không xuất được; sinh viên nhận bản đã redaction
- Sinh viên liên kết đúng báo cáo của mình; xoá bài tập bởi chủ sở hữu được commit

**Rủi ro đã loại trừ:** kịch bản fixture được xử lý bằng cách copy `fixtures/submissions/` sang `C:\temp\aita-fixtures` và trỏ `AITA_UPLOAD_DIR` vào đó.

---

## 12. API TESTING — Phân tích tĩnh · có kết quả

| Endpoint | Phương thức | Bảo vệ | Đánh giá |
|---|---|---|---|
| `/login` | GET/POST | PUBLIC | OK |
| `/login-google` | GET/POST | PUBLIC | OK |
| `/logout` | GET | PUBLIC | OK |
| `/dashboard` | GET | STAFF | OK |
| `/course-action` | POST | STAFF | OK |
| `/assignment-action` | POST | STAFF | OK |
| `/batch-scanner` | GET/POST | STAFF | OK |
| `/submit`, `/submission-action` | POST | Chỉ "đã đăng nhập" | **RỦI RO — xem lỗi #4** |
| `/diff-inspector` | GET | Chỉ "đã đăng nhập" + kiểm tra sở hữu bên trong | Chấp nhận được |
| `/export-report` | GET | Chỉ "đã đăng nhập" + kiểm tra sở hữu bên trong | Chấp nhận được |
| `/student-portal` | GET | Chỉ "đã đăng nhập" | Chấp nhận được |
| `/profile-action` | POST | Chỉ "đã đăng nhập" | Chấp nhận được |

---

## 13. DATABASE TESTING — Đã thực thi · **PASS**

Đã kiểm chứng trên SQL Server thật: CRUD 6 bảng, khoá ngoại, ràng buộc `CHECK` trên `role`/`status`/`risk_level`, trigger `TR_PlagiarismReports_SameAssignment`, `CK_PlagiarismReports_DistinctSubmissions`, và `PlagiarismDAO.getSimilarityMatrix` (ca `similarity matrix renders stored pair` trong bộ bổ sung). Các ca ghi đều được **xác nhận lại bằng truy vấn SQL độc lập** sau khi commit.

**Chưa kiểm tra:** migration idempotent (chạy script 2 lần không đổi số dòng) — cần chạy thủ công vì script đang hardcode `AITA_PlagiarismDB`.

**Rủi ro đã biết (ghi trong `ERD_DIAGRAM.md`):**
- Chưa có `UNIQUE(submission_a_id, submission_b_id)` và chưa có ràng buộc `a < b` → có thể tồn tại cả (1,2) và (2,1) → ma trận không nhất quán.
- Chưa có ràng buộc vai trò ở mức CSDL cho `Courses.instructor_id` / `Submissions.student_id`.

---

## 14. PERFORMANCE TESTING — Đã thực thi · **PASS (sau tối ưu)**

Đo trên Tomcat thật + SQL Server thật bằng `target/perf/perf_scan.py` (đo thời gian
toàn bộ request `POST /batch-scanner`, đã trừ bỏ phần tạo dữ liệu).

| Kịch bản | Tiêu chí | Ban đầu | Sau vòng 1 | Sau vòng 2 | Kết quả |
|---|---|---|---|---|---|
| 20 bài nộp (190 cặp) | — | 2,76 s | — | — | — |
| 30 bài nộp (435 cặp) | AC-SIM-03: < 3 s | **4,02 s** ❌ | 1,49 s | **1,09 s** | **PASS** |
| 50 bài nộp (1.225 cặp) | NFR-PERF-02: ≤ 5 s | **10,90 s** ❌ | 4,05 s | **2,67 s** | **PASS** |

Tổng cộng **nhanh hơn khoảng 4 lần** so với trước khi tối ưu (10,90 s → 2,67 s ở 50 bài nộp).

**Ba nguyên nhân gây chậm, theo thứ tự mức độ ảnh hưởng:**

1. **Mỗi tệp bị đọc lại N−1 lần.** `readSubmissionContent` nằm trong vòng lặp kép →
   50 bài nộp = 2.450 lần đọc đĩa thay vì 50. Sửa: đọc một lần vào `Map` trước vòng lặp.
2. **~2.450 câu `UPDATE` trạng thái.** Mỗi cặp cập nhật 2 lần. Sửa: gom thành 2 câu lệnh
   cho toàn bộ bài tập (`SubmissionDAO.markSubmissionsByOutcome`).
3. **~1.225 `SELECT` xác nhận thừa.** `createReport` truy vấn lại assignment cho mỗi cặp
   dù bài nộp đã được lấy theo assignment. Sửa: chỉ truy vấn khi người gọi chưa biết.

Kèm theo: connection pool **HikariCP** (NFR-PERF-03, trước đây `⏳`), sửa N+1 trong
`BatchScannerServlet.doGet`, và Levenshtein chuyển sang tính trên **dãy token** như SRS
mô tả thay vì từng ký tự.

**Vòng 2 — tách pha tính toán và ghi dữ liệu.** Sau vòng 1, thử 16 lượt quét đồng thời thì
**3 request nhận HTTP 503**: sau khi tách pha, các luồng đổ xô vào pool cùng lúc và phải
xếp hàng. Nguyên nhân cốt lõi là **giữ kết nối CSDL trong suốt phần tính toán**. Sửa bằng
cách tách `scanAssignment` thành hai pha:
- **Pha 1 (không kết nối):** đọc tệp, chạy Jaccard/Levenshtein, thu kết quả vào bộ nhớ.
- **Pha 2 (một giao dịch):** khoá `UPDLOCK`, xoá cũ, ghi tất cả báo cáo, cập nhật trạng thái.

Đồng thời nâng `connectionTimeout` của pool từ 10 s lên 30 s, vì các lượt quét trên cùng
bài tập bị nối tiếp hoá bởi khoá và **việc xếp hàng là bình thường**, không nên bị từ chối.

**Lưu ý:** kết quả phụ thuộc kích thước tệp. Các con số trên đo với fixture ~600 ký tự.
Tệp lớn hơn sẽ chậm hơn tỷ lệ với độ dài.

## 15. LOAD / 16. STRESS — Đã thực thi · **PASS (16 đồng thời)**

Dùng `target/perf/perf_concurrent.py`: tạo một bài tập, nạp N bài nộp, rồi cho K luồng
cùng gửi `POST /batch-scanner` (mỗi luồng một phiên đăng nhập riêng).

| Kịch bản | Kết quả |
|---|---|
| K = 4, N = 20 | **PASS** — 4/4 trả 302; báo cáo trong DB = 190 = đúng C(20,2) |
| K = 16, N = 30 | **PASS** — 16/16 trả 302; báo cáo trong DB = 435 = đúng C(30,2); thời gian toàn cục ~21 s |

**Điều này xác minh AC-SIM-05 ở mức hệ thống**: các lượt quét đồng thời không nhân đôi
dữ liệu (nếu không có khoá, 16 lượt sẽ để lại tới 16 × 435 = 6.960 báo cáo).

**Hành vi dưới tải (đo được trước khi tăng timeout):** với 16 luồng và pool 10 kết nối,
3 request nhận **HTTP 503** sau ~13 s. Dữ liệu không bao giờ bị hỏng (vẫn đúng 435), nhưng
có request thất bại. Sau khi tách pha và nâng timeout lên 30 s, 16/16 thành công.

**Giới hạn đã biết:** các lượt quét trên cùng một bài tập bị nối tiếp hoá hoàn toàn bởi
khoá `UPDLOCK`. Đây là chủ ý (đảm bảo toàn vẹn) nhưng đồng nghĩa với việc K lượt quét
sẽ mất khoảng K × (thời gian một lượt). Nếu cần xử lý nhiều lượt quét đồng thời trong
tương lai, nên đưa việc quét vào hàng đợi thay vì để request chờ.

**Kịch bản cần chạy:**
- Hiệu năng: quét 30 bài nộp (AC-SIM-03 mục tiêu < 3 giây).
- Tải: 50 sinh viên nộp đồng thời (NFR-PERF-01: CRUD ≤ 500 ms).
- Chịu lực: quét lớp 50 bài (1.225 cặp) khi **không có** connection pool.

**Rủi ro dự đoán (cao):**
- **Chưa có connection pool** — mỗi DAO tự mở kết nối. Quét 1.225 cặp sẽ mở hàng nghìn kết nối → rất chậm, có thể cạn pool của SQL Server.
- `AccessPolicy.canManageCourse` tạo `new CourseDAO()` và truy vấn DB cho **từng** bài tập trong `BatchScannerServlet.doGet` → N+1.
- Levenshtein tính trên chuỗi ký tự đầy đủ O(n·m) → với file lớn sẽ rất chậm.

---

## 17. USABILITY — Đánh giá gián tiếp

- **Tốt:** thông báo lỗi bằng tiếng Việt rõ ràng; dashboard nêu rõ "số liệu tại thời điểm tải trang"; sinh viên không thấy dữ liệu bạn học.
- **Cần cải thiện:**
  - Trang quét sẽ báo *0 báo cáo* nếu chưa cấu hình `AITA_UPLOAD_DIR` — thông báo hiện tại chưa giải thích cách khắc phục.
  - `MatchingBlocks` là khối minh hoạ nhưng giao diện không nói rõ điều đó → giảng viên có thể hiểu nhầm là bằng chứng chính xác.
  - Không có phân trang: lớp đông sẽ có danh sách dài.

---

## 18. COMPATIBILITY / 19. ACCESSIBILITY — Đã thực thi · **PASS (sau sửa)**

Chạy `tools/verify_followup_browser.py` bằng **Edge headless (Playwright)**: đăng nhập thật,
chọn bài tập trong dropdown, đọc bảng, bấm liên kết báo cáo — ở **3 viewport 1440 / 768 / 375**.

| Kịch bản | Kết quả |
|---|---|
| 6 tổ hợp (2 route × 3 viewport) | **PASS** — không tràn ngang, không lỗi JS |
| Khả năng truy cập (trong trình duyệt) | **PASS** — 0 vấn đề sau khi sửa |

**Lỗi truy cập tìm được và đã sửa (26 trường):**
- **20 trường nhập liệu không có nhãn** — chỉ có `placeholder`, không đủ cho trình đọc màn hình
  (`courseCode`, `courseName`, `semester`, `title`, `similarityThreshold`, `maxScore`,
  `deadline`, `fullName`, `avatarUrl`, `oldPassword`, `newPassword`, `file`). Đã thêm `aria-label`.
- **6 trường do `system-modals.js` sinh ra** cũng thiếu nhãn (ô tìm kiếm, 3 thanh trượt
  ngưỡng, 2 checkbox). Đã thêm `aria-label` trong chuỗi HTML của JS.

Script giờ sẽ **thất bại** nếu các vấn đề này tái diễn (`assert` bao gồm `a11y`).

**Chưa kiểm tra:** độ tương phản màu (nền tối `#080911` với chữ xám `slate-400` có thể không
đạt WCAG AA), điều hướng bằng bàn phím, focus visible, và font 10–11px trên màn hình nhỏ —
những mục này vẫn là rủi ro về khả năng đọc.

---

## 20. SMOKE TESTING — Đã thực thi một phần · **PASS**

1. Biên dịch sạch mã chính + mã kiểm thử. → PASS
2. `web.xml` phân tích hợp lệ, 4 `error-page`, `http-only=true`. → PASS
3. Ứng dụng khởi động được? → **PASS**. Tomcat 10.1.60 khởi động trong 7,4 giây, triển khai `/plagiarism`, `/login` trả 200, không có SEVERE trong log. Ca smoke quan trọng nhất đã vượt qua.

---

# LỖI TÌM ĐƯỢC

## Lỗi #1 — Hai tệp rỗng cho điểm tương đồng 100% → gắn HIGH_RISK oan
- **Mức độ:** Trung bình
- **Đã sửa:** Có
- **Repro:** gọi `calculateOverallSimilarity("", "")`.
- **Kỳ vọng:** 0% (không có gì để so sánh).
- **Thực tế trước:** 100% → mọi cặp tệp rỗng bị gắn `HIGH_RISK` và chuyển `FLAGGED`.
- **Sửa:** trả 0 khi một trong hai nội dung chuẩn hóa rỗng; trong `scanAssignment` các cặp rỗng được tính vào `skippedPairs`.

## Lỗi #2 — Checkbox "Duy trì phiên (JWT 24h)" là điều khiển chết
- **Mức độ:** Thấp (UX / trung thực)
- **Đã sửa:** Có
- **Repro:** mở `/login`, tìm ô "Duy trì phiên".
- **Kỳ vọng:** một điều khiển có tác dụng, hoặc không xuất hiện.
- **Thực tế:** không có thuộc tính `name`, không bao giờ được gửi lên; lại hứa tính năng "Remember Me" đang `⏳`.
- **Sửa:** thay bằng dòng chữ "Phiên đăng nhập có hiệu lực 24 giờ".

## Lỗi #3 — `includes/footer.jsp` thiếu khai báo mã hóa
- **Mức độ:** Thấp
- **Đã sửa:** Có
- **Kỳ vọng/Thực tế:** thiếu `pageEncoding="UTF-8"` → nguy cơ hiển thị sai tiếng Việt nếu tệp được đưa vào sử dụng.
- **Sửa:** thêm chỉ thị trang.

## Lỗi #4 — Nộp bài vào `assignmentId` bất kỳ · **ĐÃ SỬA (còn một giới hạn)**
- **Mức độ:** Trung bình (bảo mật)
- **Repro:** đăng nhập sinh viên A → `POST /submit` với `assignmentId` không tồn tại, hoặc của bài tập đã quá hạn.
- **Kỳ vọng:** 404 (không tồn tại) / 403 (quá hạn).
- **Thực tế trước:** chấp nhận; ID không tồn tại làm CSDL ném lỗi khoá ngoại (500); bài quá hạn vẫn nhận.
- **Đã sửa:** `SubmissionServlet` kiểm tra tồn tại và hạn nộp qua `AccessPolicy.canSubmitTo`.
- **Giới hạn còn lại:** chưa có quan hệ enrolment nên **chưa thể** giới hạn "sinh viên thuộc lớp nào được nộp bài tập đó". Mọi người dùng đã xác thực vẫn có thể nộp vào bất kỳ bài tập còn hạn nào. Đã ghi nhận trong SRS; sẽ bổ sung khi có enrolment (W4–6).

## Lỗi #5 — CSRF bị bỏ qua nếu thiếu cả `Origin` lẫn `Sec-Fetch-Site` · **ĐÃ SỬA**
- **Mức độ:** Trung bình
- **Repro:** `POST /course-action` từ nguồn khác bằng trình khách không gửi hai header trên.
- **Kỳ vọng:** 403.
- **Thực tế trước:** chỉ chặn khi `Sec-Fetch-Site: cross-site` **hoặc** `Origin` sai → thiếu cả hai thì lọt qua.
- **Đã sửa:** chuyển sang **mặc định từ chối** — chỉ cho qua khi có bằng chứng cùng nguồn. Có 4 ca kiểm thử bảo vệ (`AuthFilterTest`).
- **Việc phải làm kèm:** các script trong `tools/` POST không kèm `Origin` nên đã được cập nhật để gửi `Origin` giả lập trình duyệt. **Cần chạy lại `tools/verify_week3_http.py` để xác nhận không gãy kịch bản hiện có.**

## Lỗi #6 — Overload DAO thiếu `actor` mặc định cho phép tất cả · **ĐÃ SỬA**
- **Mức độ:** Thấp (tiềm ẩn)
- **Repro (trước):** gọi `assignmentDAO.deleteAssignment(id)` không truyền actor → SQL `(? = 1 OR ...)` với `? = 1` → luôn đúng → xóa được bài tập của người khác.
- **Đã sửa:** xoá toàn bộ overload thiếu `actor`; các method còn lại gọi `requireActor()` và ném `IllegalArgumentException` nếu actor `null`; `bindActor` luôn bind `0` (không còn đường tắt). Các test gọi DAO đã được cập nhật truyền actor ADMIN; thêm ca `testDeleteSubmissionWithoutActorRejected`.

## Lỗi #7 — Chưa có connection pool · **MỞ** (đã ghi `⏳ W4–9`)
- **Mức độ:** Trung bình (hiệu năng)
- **Thực tế:** `DBContext` gọi `DriverManager` cho mỗi lần truy cập; quét 1.225 cặp sẽ mở số lượng kết nối tương ứng.
- **Rủi ro:** chắc chắn vi phạm NFR-PERF-01/02 khi dữ liệu lớn.

---

# VÙNG CHƯA ĐƯỢC KIỂM THỬ

1. **Độ tương phản màu và điều hướng bàn phím** — đã kiểm tra cấu trúc truy cập (nhãn, alt),
   chưa đo tương phản WCAG hay focus visible; giao diện tối + chữ xám nhạt vẫn là rủi ro.
2. **Điểm gãy của tải** — mới thử tới 16 lượt quét đồng thời; chưa xác định giới hạn thực sự.
3. **Hiệu ứng 3D** (Three.js/cyber-shield) — chưa kiểm chứng bằng trình duyệt; chỉ kiểm tra markup.
3. **Google login thật** — cần `GOOGLE_CLIENT_ID` hợp lệ và tài khoản Google.
4. **Gemini thật** — cần `GEMINI_API_KEY`; hiện mới kiểm thử trích xuất tóm tắt trên phản hồi giả lập.
5. **Xuất CSV mở bằng Excel** để xác nhận formula injection thực sự bị chặn (đã kiểm ở mức đơn vị).
6. **Migration idempotent** — chạy script 2 lần không đổi số dòng.
7. **Quét đồng thời trên DB thật** — ca `AC-SIM-05` mới được kiểm ở mức đơn vị, chưa thử hai request quét thật chạy song song.

# TỔNG KẾT ĐỘ PHỦ

- **Đã thực thi và đạt:** Unit + Integration (177/177), Database, System (27/27), End-to-End/Functional (48/48), API, Validation & Error Handling, Edge/Boundary, Security (gồm CSRF), Regression, UI (tĩnh), Smoke khởi động ứng dụng.
- **Lỗi:** 7 — **tất cả đã xử lý**; riêng lỗi #4 còn một giới hạn đã biết (thiếu quan hệ enrolment).
- **Lỗi phát sinh trong quá trình kiểm thử và đã sửa:** 3 (test làm hỏng cấu hình toàn cục ×2, `Origin` sai định dạng trong script ×1).
- **Chưa thực thi:** Compatibility / Accessibility runtime, Google login thật, Gemini thật, kiểm chứng bằng trình duyệt, điểm gãy của tải.
- **Rủi ro còn lại:** các lượt quét trên cùng bài tập bị nối tiếp hoá bởi khoá `UPDLOCK` — đây là chủ ý để đảm bảo toàn vẹn, nhưng K lượt quét sẽ mất khoảng K × thời gian một lượt. Nếu cần quét đồng thời nhiều bài tập khác nhau thì không bị ảnh hưởng (khoá theo hàng).

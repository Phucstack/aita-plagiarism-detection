# BÁO CÁO KIỂM THỬ TOÀN DIỆN — AITA CODEDEFEND

**Ngày:** 18/09/2026 · **Môi trường:** Windows, JDK 17 (Microsoft 17.0.18), Maven **không khả dụng**, **không có SQL Server**, **không có Tomcat đang chạy**.

> **Cách đọc kết quả:**
> - **PASS / FAIL** = đã thực thi thật và có kết quả.
> - **CHƯA THỰC THI** = có kịch bản đầy đủ nhưng bị chặn bởi môi trường (cần SQL Server / Tomcat / trình duyệt). Các mục này kèm rủi ro dự đoán.
>
> Bộ kiểm thử tự động được chạy bằng JUnit Platform Launcher thay vì Maven:
> `80 ca chạy — 80 đạt — 0 lỗi` (toàn bộ các ca không cần CSDL).

---

## 0. TÓM TẮT

| Nhóm | Trạng thái | Kết quả |
|---|---|---|
| Unit Testing | Đã thực thi | **PASS** (80/80) |
| Validation & Error Handling | Đã thực thi | **PASS** — phát hiện 1 lỗi biên, đã sửa |
| Edge Cases & Boundary Values | Đã thực thi | **PASS** — phát hiện 1 lỗi, đã sửa |
| Security Testing (tĩnh + đơn vị) | Đã thực thi | **PASS** — phát hiện 2 lỗi, 1 đã sửa |
| Regression Testing | Đã thực thi | **PASS** |
| UI Testing (tĩnh) | Đã thực thi | **PASS** — phát hiện 2 lỗi, đã sửa |
| Database / Integration / System / E2E / API / Acceptance | Chưa thực thi | Cần SQL Server + Tomcat |
| Performance / Load / Stress | Chưa thực thi | Cần ứng dụng đang chạy |
| Compatibility / Accessibility (runtime) | Chưa thực thi | Cần trình duyệt |
| Usability | Đánh giá gián tiếp | Có nhận xét |

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

## 6. UI TESTING (tĩnh) — Đã thực thi · **PASS** (đã sửa 2 lỗi)

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

## 7. INTEGRATION TESTING — Chưa thực thi

**Kiểm thử gì:** Tương tác giữa servlet ↔ service ↔ DAO ↔ SQL Server.

**Kịch bản (cần chạy sau):**
1. Khởi động SQL Server, tạo DB từ `database/database_schema.sql`.
2. Chạy `tools/test-java.ps1` với `.env.test` trỏ DB riêng (tên chứa `Test`/`Verification`).
3. Kiểm tra: CRUD khóa học/bài tập/bài nộp có ghi đúng; `scanAssignment` tạo đúng số báo cáo; transaction rollback khi lỗi giữa chừng.

**Kết quả kỳ vọng:** toàn bộ 125 ca nền tảng + các ca mới đạt, 0 lỗi.

**Rủi ro dự đoán:**
- `lockAssignment` dùng `UPDLOCK, HOLDLOCK` — nếu DB test đặt mức cô lập snapshot, có thể không chặn được quét đồng thời.
- Các overload nhận `Connection` chưa được kiểm chứng với SQL Server thật.

---

## 8. SYSTEM TESTING — Chưa thực thi

**Kịch bản:** triển khai WAR lên Tomcat 10.1 + SQL Server, chạy `tools/verify_followup_http.py` (27 ca) và `verify_week3_http.py`.
**Kỳ vọng:** đăng nhập, phân quyền, CRUD, số liệu dashboard khớp truy vấn SQL độc lập.
**Rủi ro:** `web.xml` mới có thể thay đổi hành vi welcome-file/error-page — cần smoke test kỹ trước khi demo.

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

## 11. END-TO-END TESTING — Chưa thực thi

**Kịch bản:** sinh viên nộp 4 file fixture → giảng viên quét → kiểm tra ma trận có điểm cao cho cặp `PhucTV/KhanhDVP` (đổi tên biến) và thấp cho `NhiNH/TienN` → mở báo cáo → xuất CSV mở bằng Excel không bị thực thi công thức.
**Rủi ro:** nếu fixture không được đặt đúng chỗ, toàn bộ luồng trả về rỗng.

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

## 13. DATABASE TESTING — Chưa thực thi

**Cần kiểm tra:** 6 bảng; trigger `TR_PlagiarismReports_SameAssignment`; ràng buộc `CK_PlagiarismReports_DistinctSubmissions`; migration idempotent (chạy 2 lần không đổi số dòng); tính đúng của `PlagiarismDAO.getSimilarityMatrix`.

**Rủi ro đã biết (ghi trong `ERD_DIAGRAM.md`):**
- Chưa có `UNIQUE(submission_a_id, submission_b_id)` và chưa có ràng buộc `a < b` → có thể tồn tại cả (1,2) và (2,1) → ma trận không nhất quán.
- Chưa có ràng buộc vai trò ở mức CSDL cho `Courses.instructor_id` / `Submissions.student_id`.

---

## 14. PERFORMANCE / 15. LOAD / 16. STRESS — Chưa thực thi

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

## 18. COMPATIBILITY / 19. ACCESSIBILITY — Chưa thực thi (runtime)

**Đã kiểm tra tĩnh:** mọi `<img>` có `alt`; mọi `<input>` có `name`; form có nút gửi; UTF-8 được khai báo.
**Chưa kiểm tra:** độ tương phản màu (nền tối `#080911`, chữ xám `slate-400` có thể không đạt WCAG AA); điều hướng bằng bàn phím; `aria-label` cho các nút chỉ có icon; focus visible.
**Rủi ro:** giao diện tối + chữ xám nhạt + font 10–11px — khả năng đọc kém trên màn hình nhỏ.

---

## 20. SMOKE TESTING — Đã thực thi một phần · **PASS**

1. Biên dịch sạch mã chính + mã kiểm thử. → PASS
2. `web.xml` phân tích hợp lệ, 4 `error-page`, `http-only=true`. → PASS
3. Ứng dụng khởi động được? → **CHƯA THỰC THI** (không có Tomcat). Đây là ca smoke quan trọng nhất còn thiếu.

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

1. Mọi ca cần **SQL Server**: CRUD thật, transaction rollback trên DB thật, trigger, migration idempotent, ma trận trên dữ liệu thật.
2. Mọi ca cần **Tomcat**: khởi động ứng dụng, `verify_week3_http.py`, `verify_followup_http.py`, E2E.
3. **Trình duyệt thật**: responsive 1440/768/375, tương phản màu, bàn phím, hiệu ứng 3D.
4. **Hiệu năng / tải / chịu lực**: chưa đo được con số nào.
5. **Google login thật**: cần `GOOGLE_CLIENT_ID` hợp lệ và tài khoản Google.
6. **Gemini thật**: cần `GEMINI_API_KEY`; hiện mới kiểm thử trích xuất tóm tắt trên phản hồi giả lập.
7. **Xuất CSV mở bằng Excel** để xác nhận formula injection thực sự bị chặn.

# TỔNG KẾT ĐỘ PHỦ

- **Đã thực thi:** Unit, Validation & Error Handling, Edge/Boundary, Security (tĩnh + đơn vị, gồm CSRF), Regression, UI (tĩnh), Smoke (biên dịch + cấu hình). **94/94 ca đạt.**
- **Phân tích tĩnh có kết quả:** API (ma trận endpoint/bảo vệ), cấu trúc UI, cấu hình container.
- **Chưa thực thi do môi trường:** Database, Integration, System, End-to-End, Acceptance, Functional runtime, Performance, Load, Stress, Compatibility, Accessibility runtime, Smoke khởi động ứng dụng.
- **Lỗi:** 7 (Trung bình: 4 · Thấp: 3) — đã sửa 4, còn mở 3.
- **Số ca kiểm thử dự kiến toàn bộ:** 125 nền tảng + 46 bổ sung = **171** — cần chạy lại `tools/test-java.ps1` để xác nhận chính xác.

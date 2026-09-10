# SƠ ĐỒ QUAN HỆ THỰC THỂ (ERD) - HỆ THỐNG AITA
## NHÓM 4: AI PLAGIARISM & CODE SIMILARITY DETECTION
### ĐÁP ỨNG TIÊU CHUẨN ĐÁNH GIÁ PRJ301 / PRJ30x (10 ĐIỂM DATABASE DESIGN)

---

## 1. Sơ Đồ ERD Trực Quan (Entity-Relationship Diagram)

```mermaid
erDiagram
    USERS ||--o{ COURSES : "giảng dạy (teaches)"
    USERS ||--o{ SUBMISSIONS : "nộp bài (submits)"
    COURSES ||--o{ ASSIGNMENTS : "chứa (contains)"
    ASSIGNMENTS ||--o{ SUBMISSIONS : "yêu cầu (requires)"
    ASSIGNMENTS ||--o{ PLAGIARISM_REPORTS : "được đối soát theo (analyzed_under)"
    SUBMISSIONS ||--o{ PLAGIARISM_REPORTS : "bài nộp A (submission_a)"
    SUBMISSIONS ||--o{ PLAGIARISM_REPORTS : "bài nộp B (submission_b)"
    PLAGIARISM_REPORTS ||--|{ MATCHING_BLOCKS : "bao gồm (contains)"

    USERS {
        int user_id PK "Tự tăng (IDENTITY)"
        string username UK "Tên đăng nhập duy nhất"
        string password_hash "Mật khẩu băm (SHA-256 / MD5)"
        string full_name "Họ và tên người dùng"
        string email UK "Email tổ chức (@fpt.edu.vn)"
        string role "CHECK: ADMIN, INSTRUCTOR, STUDENT"
        string avatar_url "URL ảnh đại diện"
        datetime created_at "Thời điểm tạo tài khoản"
    }

    COURSES {
        int course_id PK "Tự tăng (IDENTITY)"
        string course_code UK "Mã môn học (vd: PRJ301)"
        string course_name "Tên môn học"
        int instructor_id FK "Liên kết USERS.user_id"
        string semester "Học kỳ (vd: Fall 2026)"
        datetime created_at "Thời điểm tạo môn"
    }

    ASSIGNMENTS {
        int assignment_id PK "Tự tăng (IDENTITY)"
        int course_id FK "Liên kết COURSES.course_id"
        string title "Tiêu đề bài tập"
        string description "Mô tả chi tiết yêu cầu"
        decimal max_score "Điểm tối đa (100.00)"
        datetime deadline "Hạn chót nộp bài"
        datetime created_at "Thời điểm tạo bài tập"
    }

    SUBMISSIONS {
        int submission_id PK "Tự tăng (IDENTITY)"
        int assignment_id FK "Liên kết ASSIGNMENTS.assignment_id"
        int student_id FK "Liên kết USERS.user_id (Role: STUDENT)"
        string file_name "Tên tệp mã nguồn (.java/.zip)"
        string file_path "Đường dẫn lưu trữ artifact"
        string sha256_hash "Mã băm SHA-256 chống giả mạo"
        datetime submitted_at "Thời điểm sinh viên nộp"
        string status "CHECK: PENDING, PARSED, ANALYZED, FLAGGED"
    }

    PLAGIARISM_REPORTS {
        int report_id PK "Tự tăng (IDENTITY)"
        int assignment_id FK "Liên kết ASSIGNMENTS.assignment_id"
        int submission_a_id FK "Bài nộp sinh viên A (SUBMISSIONS)"
        int submission_b_id FK "Bài nộp sinh viên B (SUBMISSIONS)"
        decimal similarity_score "Tỷ lệ trùng lặp logic (%)"
        string risk_level "CHECK: SAFE, LOW, MEDIUM, HIGH_RISK"
        string ai_analysis_summary "Tóm tắt phân tích ngữ nghĩa Gemini AI"
        datetime created_at "Thời điểm hoàn thành phân tích"
    }

    MATCHING_BLOCKS {
        int block_id PK "Tự tăng (IDENTITY)"
        int report_id FK "Liên kết PLAGIARISM_REPORTS.report_id"
        string function_name "Tên hàm bị trùng lặp"
        int student_a_start_line "Dòng bắt đầu ở bài nộp A"
        int student_a_end_line "Dòng kết thúc ở bài nộp A"
        int student_b_start_line "Dòng bắt đầu ở bài nộp B"
        int student_b_end_line "Dòng kết thúc ở bài nộp B"
        string matched_code_snippet "Đoạn mã trùng lặp được trích xuất"
        string variable_renaming_notes "Ghi chú biến đổi tên biến / đảo nhánh"
    }
```

---

## 2. Thuyết Minh Chuẩn Hóa 3NF & Ràng Buộc Dữ Liệu

1. **Chuẩn 1NF (First Normal Form):**
   - Mọi thuộc tính đều là giá trị nguyên tử (atomic), không chứa mảng lồng nhau.
   - Các dòng code trùng lặp chi tiết được tách biệt vào bảng `MatchingBlocks` thay vì lưu text gộp trong `PlagiarismReports`.

2. **Chuẩn 2NF (Second Normal Form):**
   - Toàn bộ các bảng đều sử dụng Khóa chính đơn (`user_id`, `course_id`, `assignment_id`, `submission_id`, `report_id`, `block_id`), loại bỏ hoàn toàn sự phụ thuộc bộ phận vào một phần của khóa.

3. **Chuẩn 3NF (Third Normal Form):**
   - Loại bỏ phụ thuộc bắc cầu (Transitive Dependency). Ví dụ: `Submissions` chỉ lưu `student_id` và `assignment_id`, không lưu thừa tên môn học hay họ tên sinh viên.

4. **Ràng buộc toàn vẹn & Bảo mật (Integrity & Security):**
   - **Xác thực toàn vẹn mã nguồn (Section 4.4.2):** Cột `sha256_hash` (CHAR 64) trong `Submissions` đảm bảo phát hiện ngay lập tức bất kỳ sự can thiệp nào vào mã nguồn sau khi nộp.
   - **Bảo mật vai trò:** Cột `role` trong bảng `Users` có ràng buộc `CHECK (role IN ('ADMIN', 'INSTRUCTOR', 'STUDENT'))`.
   - **Xóa xếp tầng an toàn:** Ràng buộc `ON DELETE CASCADE` ở các quan hệ cha - con (`Courses` -> `Assignments` -> `Submissions`, `PlagiarismReports` -> `MatchingBlocks`) đảm bảo tính nhất quán dữ liệu.

# PRJ301 - AI USAGE REPORT

## Sheet: 0.Overview

| Field | Value |
| :--- | :--- |
| **PRJ301 - Project AI Usage Report** | |
| **Subject Code** | PRJ301 |
| **Subject Name** | Java Web Application |
| **Class Code** | SE20C |
| **Semester** | Fall 2026 |
| **Lecturer Name** | Nguyễn Hoài Nhi |
| **Group Code** | 7 |
| **Project Title** | AITA CodeDefend - AI Plagiarism & Code Similarity Detection Suite |

### Danh sách sinh viên

| No | StudentCode | StudentName | Role In Group | AI Tool Usaged |
| :--- | :--- | :--- | :--- | :--- |
| 1 | Qe200105 | Nguyễn Trần Anh Kiệt |  |  |
| 2 | Qe200062 | Đinh Vũ Phương Khánh |  |  |
| 3 | Qe200133 | Nguyễn Đình Tiến |  |  |
| 4 | Qe2000141 | Trần Văn Phúc |  |  |
| 5 | Qe200069 | Nguyễn Hoài Nhi |  |  |

---

## Sheet: 1. Week 1

| No. | SDLC Phase | Task / Activity | AI Tool Used | AI Output | Student's Validation / Modification | Evidence / Link | Quantitative Measure | Value Added (1-5) | Risks / Limitations Observed |
| :--- | :--- | :--- | :--- | :--- | :--- | :--- | :--- | :--- | :--- |
| 1 | Requirement & Foundation | Research Apache Tomcat and generate setup guide for team members | Gemini | Generated a comprehensive guide explaining Tomcat concepts, directory structure, port conflict resolution, and IDE integration steps. | Reviewed technical accuracy, formatted the output into a Word document, and verified that the IntelliJ/Eclipse setup steps match the team's current environment. | https://drive.google.com/drive/folders/1LGPjT0u5jygjKFlMjMxu0EP-gPwl7bnJ?usp=drive_link | 1 technical guide document (5 sections) | 5 | AI provided slightly generic IDE instructions; required manual verification to ensure it matches the latest IntelliJ Ultimate UI. |
| 2 | Design | Build Database schema (ERD) for the appeal handling and peer-review flow | Gemini | Proposed a structure of 6 database tables (User, Course, Appeal_Ticket, Peer_Review, Sandbox_Log) with basic primary and foreign keys. | Redesigned the User table to add the correction_points field and adjusted the Peer_Review table to correctly accommodate the team's circular cross-grading logic (G1 -> G2 -> G3). | https://drive.google.com/drive/folders/1LGPjT0u5jygjKFlMjMxu0EP-gPwl7bnJ?usp=drive_link | 5 finalized database entities | 4 | AI failed to deeply understand the project's specific "academic market" logic and point-payment mechanism, resulting in a generic schema that required manual adjustments. |
| 3 | Implementation & Foundation | Research JDBC connection flow and configure DBContext.java for SQL Server connectivity | Gemini | Provided a standard Java JDBC connection template using DriverManager and basic connection URL structure. | Added mssql-jdbc dependency, fixed SSL handshake error by appending ';encrypt=true;trustServerCertificate=true' to the connection URL, and wrote a main() test method to confirm database connectivity. | https://drive.google.com/file/d/1CTGenIGc7blsUzn6XzbH_fcTr02xLfsT/view?usp=sharing | 1 DBContext class, 1 successful test connection log | 5 | AI provided generic JDBC syntax that omitted modern SQL Server SSL requirements, causing an initial connection crash. |

---

## Sheet: 2. Week 2

| No. | SDLC Phase | Task / Activity | AI Tool Used | AI Output | Student's Validation / Modification | Evidence / Link | Quantitative Measure | Value Added (1-5) | Risks / Limitations Observed |
| :--- | :--- | :--- | :--- | :--- | :--- | :--- | :--- | :--- | :--- |
| 1 | Design | Chuẩn hóa ERD 6 bảng 3NF + trigger cùng-assignment cho PlagiarismReports | Gemini | Đề xuất tách MatchingBlocks khỏi PlagiarismReports và bỏ cột assignment_id dư thừa | Giữ Users/Courses/Assignments/Submissions/PlagiarismReports/MatchingBlocks, bổ sung trigger TR_PlagiarismReports_SameAssignment + TR_Submissions_PreserveReportAssignment, unique filtered google_subject | PENDING-EVIDENCE: Group7_Session4_ERD3NF | 6 entities, 2 triggers, 1 unique filtered index | 5 | AI ban đầu vẫn giữ assignment_id dư thừa gây phụ thuộc bắc cầu |
| 2 | Implementation | Review AuthFilter + JWT HS256 + PBKDF2 upgrade mật khẩu legacy | Muse Spark | Gợi ý rotate sessionId, cookie HttpOnly/SameSite Strict, reject token sai/hết hạn, update password có điều kiện COLLATE BIN2 | Áp dụng JWTUtil issuer aita/expiry 24h, AuthFilter chặn JSP trực tiếp + /uploads + cross-site Origin, PasswordUtil PBKDF2 600k vòng | PENDING-EVIDENCE: Group7_Session5_AuthReview | 3 classes, 116 tests auth/JWT/password PASS | 5 | AI gợi ý cấu hình chung, phải tự gắn với Users.role và quyền sở hữu SQL |
| 3 | Implementation | CRUD Course/Assignment gắn quyền sở hữu ngay trong SQL | GitHub Copilot | Sinh skeleton CourseAction/AssignmentAction + DAO update/delete cơ bản | Thêm điều kiện instructor_id/ADMIN trong SQL, validate courseCode/title/deadline/threshold 0-100, giữ deadline cũ khi update rỗng | PENDING-EVIDENCE: Group7_Session6_CRUDOwnership | 2 servlets, 2 DAO, 400/403/404/409 phân biệt | 4 | Code sinh ra chỉ check quyền ở servlet, thiếu enforcement ở DAO |

---

## Sheet: 3. Week 3

| No. | SDLC Phase | Task / Activity | AI Tool Used | AI Output | Student's Validation / Modification | Evidence / Link | Quantitative Measure | Value Added (1-5) | Risks / Limitations Observed |
| :--- | :--- | :--- | :--- | :--- | :--- | :--- | :--- | :--- | :--- |
| 1 | Implementation | Engine deterministic Java: normalize $ID_n + Jaccard 3-gram + Levenshtein + MatchingBlocks | Muse Spark | Gợi ý pipeline lexer/normalize/score/risk và template ai_analysis_summary | Triển khai PlagiarismEngineService.scanAssignment, risk theo threshold/50/30, không lưu điểm AI giả vào DB | PENDING-EVIDENCE: Group7_Session7_Engine | 1 service, 5 engine tests, scan empty PASS | 4 | Template summary dễ bị hiểu nhầm là AI thật nên phải ghi rõ trong DEMO |
| 2 | Implementation & Testing | Ma trận NxN + lọc trạng thái chấm + export CSV có redaction sinh viên | Muse Spark | Đề xuất query GROUP BY cặp submission + filter status whitelist + CSV BOM UTF-8 | Thêm PlagiarismDAO.getSimilarityMatrix, SubmissionDAO.getSubmissionsByAssignmentAndStatus, ExportReportServlet kiểm tra format trước quyền, sinh viên chỉ thấy REDACTED | PENDING-EVIDENCE: Group7_Session8_MatrixExport | 48 follow-up HTTP/SQL checks PASS, 125 JUnit PASS | 5 | Export ban đầu để /export-report trong STAFF filter nên sinh viên bị 403, phải chuyển enforcement về servlet |
| 3 | Testing | Mở rộng verify HTTP/SQL + browser đa viewport cho dashboard/report/export | Muse Spark | Gợi ý thêm check matrix/filter/export vào verify_followup_http.py | Thêm 8 checks: matrix renders, status filter, invalid status 400, owner CSV 200, format pdf 400, foreign export 403, student redacted, outsider 403 | PENDING-EVIDENCE: Group7_Session9_Verify | 27 week3 + 48 follow-up checks PASS, Google forged/replay rejected | 5 | Script cũ assert sai trạng thái PENDING khi fixture không có PENDING, phải đổi sang ANALYZED |

---

---

## Sheet: Instruction

| Column | Description & How to Fill |
| :--- | :--- |
| No. | Sequential number of the log entry (1, 2, 3...). |
| SDLC Phase | Which phase of the project this AI usage belongs to (e.g., Requirement, Design, Implementation, Testing, Reporting). |
| Task / Activity | Specific task you used AI for (e.g., Use Case Drafting, ERD Design, Servlet/JSP Coding, Test Case Generation). |
| AI Tool Used | Name of the AI tool (e.g., ChatGPT, GitHub Copilot, Copilot X, Bard, CodeWhisperer). |
| AI Output | The actual output AI produced (summary only, e.g., “8 use cases generated”, “ERD with 5 entities”, “sample Servlet controller”). |
| Student's Validation / Modification | How you validated or modified the AI output (e.g., “kept 6 use cases, rewrote 2”, “adjusted foreign keys”, “fixed SQL injection risk”, “refined test cases”). |
| Evidence / Link | Link to a shared Google Drive folder containing screenshots/videos of the AI chat session(s).<br>Naming convention: GroupX_SessionY_Activity (e.g., Group5_Session2_ERD.png).<br>Each screenshot/video must clearly show: prompt, AI response, and student follow-up. |
| Quantitative Measure | Numerical evidence of the contribution (e.g., number of use cases, number of entities, number of Servlets/JSP pages, LOC, number of test cases). |
| Value Added (1-5) | Self-assessment of AI's usefulness (1 = not useful, 5 = very useful). |
| Risks / Limitations Observed | What limitations or risks you observed when using AI (e.g., “generated SQL injection-prone code”, “missing validation logic”, “incomplete test coverage”). |

---

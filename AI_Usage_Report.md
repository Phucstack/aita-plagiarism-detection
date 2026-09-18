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
| 1 | Design | Database Schema Audit & Normalization for Users Table and Course Enrollment | Gemini | Analyzed the existing Users schema and identified 5 core architectural and security deficiencies: 1NF violation by embedding student IDs in full names, missing N-N CourseEnrollments junction table, lack of soft-delete causing foreign key delete conflicts, unsalted hashing / hardcoded OAuth flags, and missing account security/audit fields (is_active, failed_attempts). Generated revised DDL for Users and CourseEnrollments. | Validated recommendations against PRJ301 project requirements: Added separated user_code (MSSV/Instructor Code) to enforce 1NF atomicity, introduced is_active (BIT) to implement soft deletion and avoid foreign key constraint violations upon account removal, and separated auth_provider / provider_id columns instead of storing static OAuth text flags in password_hash. | https://drive.google.com/file/d/1erlT4J1mqvFR-_u3Lk0nf9s7dhkLT-3Y/view?usp=sharing | 1 refined Users schema (5 security/normalization fields added), 1 new CourseEnrollments table DDL | 5 | AI recommended enterprise-scale audit fields (reset_token expiry, brute-force lockout counters) which would overcomplicate the PRJ301 scope; required manual filtering to keep only essential columns for JDBC and role-based access. |
| 2 | Design & Architecture | Architectural Trade-off Analysis: Single Table vs. Class Table Inheritance for Role Management | Gemini | Evaluated the student proposal to separate Admin into an independent table. Compared Single Table Pattern against Class Table Inheritance (Martin Fowler PoEAA) and RBAC using the Citizen ID metaphor (Single Source of Truth for /login vs role-specific profile tables). Advised keeping Single Table for demo stability under Raw JDBC while proposing Class Table Inheritance for defense Q&A / Architectural Roadmap. | Critically debated whether to decouple Admin tables immediately versus adhering to the YAGNI principle. Decided to retain the Single Table pattern for the active implementation to maintain lean Raw JDBC queries and prevent connection leaks, while incorporating Class Table Inheritance into the SRS design section to demonstrate architectural thinking during project defense. | https://drive.google.com/file/d/1erlT4J1mqvFR-_u3Lk0nf9s7dhkLT-3Y/view?usp=sharing | 2 architectural models evaluated (Single Table vs Class Table Inheritance), 1 architectural roadmap defense scenario formulated | 5 | AI initially pushed towards enterprise overengineering (Class Table Inheritance) which would introduce multi-table JOIN overhead and connection management risks in raw JDBC servlets without an ORM. |

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

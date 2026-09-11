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
| **Project Title** | <<e.g. AITA-mini: Assignment Submission & Grading Portal>> |

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

## Sheet: 2. Week n

| No. | SDLC Phase | Task / Activity | AI Tool Used | AI Output | Student's Validation / Modification | Evidence / Link | Quantitative Measure | Value Added (1-5) | Risks / Limitations Observed |
| :--- | :--- | :--- | :--- | :--- | :--- | :--- | :--- | :--- | :--- |
| 1 | <<sample data>><br>Implementation | Servlet + JSP CRUD for Assignment module | GitHub Copilot | Generated AssignmentServlet doGet/doPost skeleton | Fixed SQL injection risk, added PreparedStatement | GitHub commit link | 1 Servlet, 4 JSP pages, 6 methods | 5 | Generated code used string concatenation for SQL |
| 2 | Testing | Test case generation for login & submission upload | ChatGPT | 12 draft test cases (positive/negative) | Kept 9, removed 3 irrelevant to Servlet routing | Google Sheet link | 9 test cases, 7 passed | 3 | Missed edge case for expired deadline |

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

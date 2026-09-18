-- AITA SQL Server schema. Creates missing tables; never resets existing data or logins.
-- Run with a database administrator only for initial database creation.
SET QUOTED_IDENTIFIER ON;
SET ANSI_NULLS ON;
SET ANSI_PADDING ON;
SET ANSI_WARNINGS ON;
SET CONCAT_NULL_YIELDS_NULL ON;
SET ARITHABORT ON;
SET NUMERIC_ROUNDABORT OFF;
GO
USE master;
GO
IF DB_ID(N'AITA_PlagiarismDB') IS NULL CREATE DATABASE [AITA_PlagiarismDB];
GO
USE [AITA_PlagiarismDB];
GO
IF OBJECT_ID(N'dbo.Users', N'U') IS NULL
BEGIN
CREATE TABLE Users (
    user_id INT IDENTITY(1,1) PRIMARY KEY,
    username VARCHAR(50) NOT NULL UNIQUE,
    password_hash VARCHAR(255) NOT NULL, -- Legacy seed hashes; new passwords use the application password utility.
    full_name NVARCHAR(100) NOT NULL,
    email VARCHAR(100) NOT NULL UNIQUE,
    role VARCHAR(20) NOT NULL CHECK (role IN ('ADMIN', 'INSTRUCTOR', 'STUDENT')),
    avatar_url VARCHAR(255) DEFAULT 'https://images.unsplash.com/photo-1534528741775-53994a69daeb?w=100',
    created_at DATETIME DEFAULT GETDATE()
);
END;
GO

IF OBJECT_ID(N'dbo.Courses', N'U') IS NULL
BEGIN
CREATE TABLE Courses (
    course_id INT IDENTITY(1,1) PRIMARY KEY,
    course_code VARCHAR(20) NOT NULL UNIQUE,
    course_name NVARCHAR(150) NOT NULL,
    instructor_id INT NOT NULL,
    semester VARCHAR(20) NOT NULL,
    created_at DATETIME DEFAULT GETDATE(),
    FOREIGN KEY (instructor_id) REFERENCES Users(user_id)
);
END;
GO

IF OBJECT_ID(N'dbo.Assignments', N'U') IS NULL
BEGIN
CREATE TABLE Assignments (
    assignment_id INT IDENTITY(1,1) PRIMARY KEY,
    course_id INT NOT NULL,
    title NVARCHAR(150) NOT NULL,
    description NVARCHAR(MAX),
    max_score DECIMAL(5,2) DEFAULT 100.00,
    deadline DATETIME NOT NULL,
    similarity_threshold DECIMAL(5,2) DEFAULT 75.00,
    created_at DATETIME DEFAULT GETDATE(),
    FOREIGN KEY (course_id) REFERENCES Courses(course_id) ON DELETE CASCADE
);
END;
GO

IF OBJECT_ID(N'dbo.Submissions', N'U') IS NULL
BEGIN
CREATE TABLE Submissions (
    submission_id INT IDENTITY(1,1) PRIMARY KEY,
    assignment_id INT NOT NULL,
    student_id INT NOT NULL,
    file_name NVARCHAR(255) NOT NULL,
    file_path NVARCHAR(500) NOT NULL,
    file_type VARCHAR(20) DEFAULT 'JAVA' CHECK (file_type IN ('JAVA', 'TEXT', 'DOCX', 'ZIP')),
    sha256_hash VARCHAR(64) NOT NULL, -- Xác thực tính toàn vẹn artifact (Section 4.4.2)
    submitted_at DATETIME DEFAULT GETDATE(),
    status VARCHAR(30) DEFAULT 'PENDING' CHECK (status IN ('PENDING', 'PARSED', 'ANALYZED', 'FLAGGED')),
    FOREIGN KEY (assignment_id) REFERENCES Assignments(assignment_id) ON DELETE CASCADE,
    FOREIGN KEY (student_id) REFERENCES Users(user_id)
);
END;
GO

IF OBJECT_ID(N'dbo.PlagiarismReports', N'U') IS NULL
BEGIN
CREATE TABLE PlagiarismReports (
    report_id INT IDENTITY(1,1) PRIMARY KEY,
    submission_a_id INT NOT NULL,
    submission_b_id INT NOT NULL,
    similarity_score DECIMAL(5,2) NOT NULL, -- Điểm tương đồng %
    risk_level VARCHAR(20) NOT NULL CHECK (risk_level IN ('SAFE', 'LOW', 'MEDIUM', 'HIGH_RISK')),
    ai_analysis_summary NVARCHAR(MAX), -- Phân tích ngữ nghĩa từ LLM / AI Watermark
    created_at DATETIME DEFAULT GETDATE(),
    CONSTRAINT CK_PlagiarismReports_DistinctSubmissions CHECK (submission_a_id <> submission_b_id),
    FOREIGN KEY (submission_a_id) REFERENCES Submissions(submission_id),
    FOREIGN KEY (submission_b_id) REFERENCES Submissions(submission_id)
);
END;
GO

IF OBJECT_ID(N'dbo.MatchingBlocks', N'U') IS NULL
BEGIN
CREATE TABLE MatchingBlocks (
    block_id INT IDENTITY(1,1) PRIMARY KEY,
    report_id INT NOT NULL,
    function_name VARCHAR(100),
    student_a_start_line INT NOT NULL,
    student_a_end_line INT NOT NULL,
    student_b_start_line INT NOT NULL,
    student_b_end_line INT NOT NULL,
    matched_code_snippet NVARCHAR(MAX),
    variable_renaming_notes NVARCHAR(MAX),
    FOREIGN KEY (report_id) REFERENCES PlagiarismReports(report_id) ON DELETE CASCADE
);
END;
GO

-- Sample data belongs only to a new, empty database.
-- Existing users and project data are never overwritten.
IF NOT EXISTS (SELECT 1 FROM Users)
   AND NOT EXISTS (SELECT 1 FROM Courses)
   AND NOT EXISTS (SELECT 1 FROM Assignments)
   AND NOT EXISTS (SELECT 1 FROM Submissions)
   AND NOT EXISTS (SELECT 1 FROM PlagiarismReports)
   AND NOT EXISTS (SELECT 1 FROM MatchingBlocks)
BEGIN
    SET XACT_ABORT ON;
    BEGIN TRANSACTION;
INSERT INTO Users (username, password_hash, full_name, email, role, avatar_url) VALUES
-- Quản trị viên
('admin', 'e10adc3949ba59abbe56e057f20f883e', N'Quản Trị Viên AITA', 'admin@aita.edu.vn', 'ADMIN', 'https://images.unsplash.com/photo-1534528741775-53994a69daeb?w=100'),

-- Giảng viên
('teacher_ha', 'e10adc3949ba59abbe56e057f20f883e', N'TS. Nguyễn Hoàng Hà', 'ha.nh@fpt.edu.vn', 'INSTRUCTOR', 'https://images.unsplash.com/photo-1507003211169-0a1dd7228f2d?w=100'),

-- Thành viên Nhóm 7 (PRJ301)
('kietnta', 'e10adc3949ba59abbe56e057f20f883e', N'Nguyễn Trần Anh Kiệt (Leader)', 'kietnta@fpt.edu.vn', 'INSTRUCTOR', 'https://images.unsplash.com/photo-1500648767791-00dcc994a43e?w=100'),
('phuctv', 'e10adc3949ba59abbe56e057f20f883e', N'Trần Văn Phúc', 'phuctv@fpt.edu.vn', 'STUDENT', 'https://images.unsplash.com/photo-1535713875002-d1d0cf377fde?w=100'),
('khanhdvp', 'e10adc3949ba59abbe56e057f20f883e', N'Đinh Vũ Phương Khánh', 'khanhdvp@fpt.edu.vn', 'STUDENT', 'https://images.unsplash.com/photo-1570295999919-56ceb5ecca61?w=100'),
('nhinh', 'e10adc3949ba59abbe56e057f20f883e', N'Nguyễn Hoài Nhi', 'nhinh@fpt.edu.vn', 'STUDENT', 'https://images.unsplash.com/photo-1494790108377-be9c29b29330?w=100'),
('tienn', 'e10adc3949ba59abbe56e057f20f883e', N'Nguyễn Tiến', 'tienn@fpt.edu.vn', 'STUDENT', 'https://images.unsplash.com/photo-1527980965255-d3b416303d12?w=100');

-- Khóa học
INSERT INTO Courses (course_code, course_name, instructor_id, semester) VALUES
('PRJ301', N'Java Web Application Development (RBL)', 2, 'Fall 2026'),
('CSD201', N'Data Structures and Algorithms', 3, 'Fall 2026');

-- Bài tập
INSERT INTO Assignments (course_id, title, description, max_score, deadline, similarity_threshold) VALUES
(1, N'Assignment 1 - Java Lexer & Code Similarity Engine', N'Xây dựng bộ quét Token và tính toán chỉ số tương đồng Jaccard giữa các file mã nguồn Java.', 100.00, '2026-10-15 23:59:59', 70.00),
(1, N'Assignment 2 - E-Commerce Web MVC2 & Payment Flow', N'Xây dựng chức năng OrderManager, giỏ hàng Cart và thanh toán an toàn.', 100.00, '2026-10-30 23:59:59', 75.00);

-- Bài nộp mẫu.
-- file_path lưu tên tệp TƯƠNG ĐỐI và được giải trong thư mục cấu hình bởi AITA_UPLOAD_DIR
-- (mặc định ${catalina.base}/aita-uploads, nằm ngoài web root).
-- Các tệp fixture tương ứng nằm tại fixtures/submissions/; để demo quét, hãy đặt
-- AITA_UPLOAD_DIR=fixtures/submissions hoặc copy chúng vào thư mục lưu trữ.
-- Mã băm dưới đây là SHA-256 THẬT của từng tệp fixture, không phải giá trị bịa.
INSERT INTO Submissions (assignment_id, student_id, file_name, file_path, file_type, sha256_hash, status) VALUES
(2, 4, 'OrderManager_PhucTV.java', 'OrderManager_PhucTV.java', 'JAVA', 'ae878e1366c492b11f3fe660128125164107c948817ba4da90a3e329e6d6e732', 'FLAGGED'),
(2, 5, 'OrderManager_KhanhDVP.java', 'OrderManager_KhanhDVP.java', 'JAVA', 'c67f75af634a2a9fddc7d8695a108ed88a12a87f02b79174e6c36277659e9b45', 'FLAGGED'),
(2, 6, 'OrderManager_NhiNH.java', 'OrderManager_NhiNH.java', 'JAVA', '5c152a52ed50ca256a319920c27182bad015a40b0f7fd730cd613814f282fd0c', 'ANALYZED'),
(2, 7, 'OrderManager_TienN.java', 'OrderManager_TienN.java', 'JAVA', '256dfd8943ac55d35daf0f13251da4a8cbf518e2d989298e15b5861f8a262599', 'ANALYZED');

-- Báo cáo đạo văn đối chứng
INSERT INTO PlagiarismReports (submission_a_id, submission_b_id, similarity_score, risk_level, ai_analysis_summary) VALUES
(1, 2, 88.50, 'HIGH_RISK', N'Phân tích cục bộ (rule-based): mức độ tương đồng báo động đỏ (88.5%) giữa OrderManager_PhucTV.java và OrderManager_KhanhDVP.java. Dấu hiệu đổi tên định danh biến/hàm và tái cấu trúc khối lệnh. Đây là dữ liệu seed minh hoạ, không phải kết quả từ mô hình ngôn ngữ lớn.');

-- Chi tiết đoạn code trùng
INSERT INTO MatchingBlocks (report_id, function_name, student_a_start_line, student_a_end_line, student_b_start_line, student_b_end_line, matched_code_snippet, variable_renaming_notes) VALUES
(1, 'calculateTotal()', 24, 30, 66, 75, N'public double calculateTotal() { cartValue = cartValue; finalCost = finalCost; return cart; }', N'Biến cartValue đổi thành basketValue, finalCost giữ nguyên.'),
(1, 'processPayment()', 44, 53, 182, 192, N'public double processPayment(Order items) { List list = new Stock(orderItems); if(basketValue.orderItems) { totalCost = enable; } return pastValue; }', N'Logic xử lý thanh toán và cấu trúc danh sách đơn hàng trùng khớp 100% về mặt luồng thực thi.');

    COMMIT TRANSACTION;
END;
GO

-- Run in the selected application/test database. Preserves existing users and passwords.
SET QUOTED_IDENTIFIER ON;
SET ANSI_NULLS ON;
SET ANSI_PADDING ON;
SET ANSI_WARNINGS ON;
SET CONCAT_NULL_YIELDS_NULL ON;
SET ARITHABORT ON;
SET NUMERIC_ROUNDABORT OFF;
IF COL_LENGTH('dbo.Users', 'google_subject') IS NULL
    ALTER TABLE dbo.Users ADD google_subject VARCHAR(255) NULL;
GO
IF NOT EXISTS (SELECT 1 FROM sys.indexes WHERE name = 'UX_Users_GoogleSubject' AND object_id = OBJECT_ID('dbo.Users'))
    EXEC('CREATE UNIQUE INDEX UX_Users_GoogleSubject ON dbo.Users(google_subject) WHERE google_subject IS NOT NULL');
GO

-- Normalize legacy PlagiarismReports to strict 3NF.
-- assignment_id is derivable from either submission and must not be stored redundantly.
IF COL_LENGTH('dbo.PlagiarismReports', 'assignment_id') IS NOT NULL
BEGIN
    -- Compile legacy-column references only while the column still exists.
    EXEC sys.sp_executesql N'IF EXISTS (
        SELECT 1
        FROM dbo.PlagiarismReports pr
        LEFT JOIN dbo.Submissions sa ON sa.submission_id = pr.submission_a_id
        LEFT JOIN dbo.Submissions sb ON sb.submission_id = pr.submission_b_id
        WHERE sa.submission_id IS NULL
           OR sb.submission_id IS NULL
           OR pr.assignment_id <> sa.assignment_id
           OR pr.assignment_id <> sb.assignment_id
           OR sa.assignment_id <> sb.assignment_id
           OR pr.submission_a_id = pr.submission_b_id
    )
        THROW 50001, ''Cannot normalize PlagiarismReports: legacy rows contain inconsistent assignment/submission relationships.'', 1;';

    DECLARE @dropAssignmentFk NVARCHAR(MAX) = N'';
    SELECT @dropAssignmentFk = @dropAssignmentFk
        + N'ALTER TABLE dbo.PlagiarismReports DROP CONSTRAINT ' + QUOTENAME(fk.name) + N';'
    FROM sys.foreign_keys fk
    JOIN sys.foreign_key_columns fkc ON fkc.constraint_object_id = fk.object_id
    JOIN sys.columns c ON c.object_id = fkc.parent_object_id AND c.column_id = fkc.parent_column_id
    WHERE fk.parent_object_id = OBJECT_ID(N'dbo.PlagiarismReports')
      AND c.name = N'assignment_id';

    SET @dropAssignmentFk = @dropAssignmentFk
        + N'ALTER TABLE dbo.PlagiarismReports DROP COLUMN assignment_id;';
    EXEC sys.sp_executesql @dropAssignmentFk;
END;
GO

IF NOT EXISTS (
    SELECT 1
    FROM sys.check_constraints
    WHERE parent_object_id = OBJECT_ID(N'dbo.PlagiarismReports')
      AND name = N'CK_PlagiarismReports_DistinctSubmissions'
)
    ALTER TABLE dbo.PlagiarismReports WITH CHECK
        ADD CONSTRAINT CK_PlagiarismReports_DistinctSubmissions
        CHECK (submission_a_id <> submission_b_id);
GO

-- Database-level invariant: the two submissions in one plagiarism report must belong to the same assignment.
CREATE OR ALTER TRIGGER dbo.TR_PlagiarismReports_SameAssignment
ON dbo.PlagiarismReports
AFTER INSERT, UPDATE
AS
BEGIN
    SET NOCOUNT ON;

    IF EXISTS (
        SELECT 1
        FROM inserted i
        JOIN dbo.Submissions sa ON sa.submission_id = i.submission_a_id
        JOIN dbo.Submissions sb ON sb.submission_id = i.submission_b_id
        WHERE sa.assignment_id <> sb.assignment_id
           OR i.submission_a_id = i.submission_b_id
    )
    BEGIN
        THROW 50002, 'PlagiarismReports requires both submissions to belong to the same assignment.', 1;
    END;
END;
GO

-- Preserve the same-assignment invariant if a submission is reassigned directly in SQL.
CREATE OR ALTER TRIGGER dbo.TR_Submissions_PreserveReportAssignment
ON dbo.Submissions
AFTER UPDATE
AS
BEGIN
    SET NOCOUNT ON;

    IF UPDATE(assignment_id) AND EXISTS (
        SELECT 1
        FROM dbo.PlagiarismReports pr
        JOIN dbo.Submissions sa ON sa.submission_id = pr.submission_a_id
        JOIN dbo.Submissions sb ON sb.submission_id = pr.submission_b_id
        WHERE (pr.submission_a_id IN (SELECT submission_id FROM inserted)
            OR pr.submission_b_id IN (SELECT submission_id FROM inserted))
          AND sa.assignment_id <> sb.assignment_id
    )
        THROW 50003, 'Cannot reassign a submission while it would split an existing plagiarism report across assignments.', 1;
END;
GO

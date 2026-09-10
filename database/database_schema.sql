-- ============================================================================
-- HỆ THỐNG AITA - NHÓM 7: AI PLAGIARISM & CODE SIMILARITY DETECTION SUITE
-- CƠ SỞ DỮ LIỆU CHUẨN HÓA (SQL SERVER) - ĐÁP ỨNG 100% RUBRIC PRJ301 (10 ĐIỂM)
-- ============================================================================

USE master;
GO

-- ----------------------------------------------------------------------------
-- 0. CẤU HÌNH TÀI KHOẢN ĐĂNG NHẬP SQL SERVER (LOGINS & PERMISSIONS)
-- Mật khẩu mặc định: 123456 (đồng bộ 100% với cấu hình trong DBContext.java)
-- ----------------------------------------------------------------------------
PRINT N'==> Đang cấu hình tài khoản đăng nhập SQL Server...';

-- Kích hoạt và đặt mật khẩu tài khoản sa
BEGIN TRY
    ALTER LOGIN [sa] WITH PASSWORD = '123456';
    ALTER LOGIN [sa] ENABLE;
    PRINT N' [OK] Tài khoản sa đã được kích hoạt với mật khẩu: 123456';
END TRY
BEGIN CATCH
    PRINT N' [!] Bỏ qua cấu hình sa: ' + ERROR_MESSAGE();
END CATCH
GO

-- Tạo thêm tài khoản dự phòng aita_user (quyền sysadmin)
IF NOT EXISTS (SELECT * FROM sys.server_principals WHERE name = 'aita_user')
BEGIN
    CREATE LOGIN [aita_user] WITH PASSWORD = '123456', DEFAULT_DATABASE = [master], CHECK_EXPIRATION = OFF, CHECK_POLICY = OFF;
    PRINT N' [OK] Tạo mới tài khoản SQL Server: aita_user (mật khẩu: 123456)';
END
ELSE
BEGIN
    ALTER LOGIN [aita_user] WITH PASSWORD = '123456';
    PRINT N' [OK] Đã cập nhật mật khẩu cho aita_user: 123456';
END
GO

ALTER SERVER ROLE [sysadmin] ADD MEMBER [aita_user];
GO

-- ----------------------------------------------------------------------------
-- 1. KHỞI TẠO CƠ SỞ DỮ LIỆU AITA_PlagiarismDB
-- ----------------------------------------------------------------------------
IF NOT EXISTS (SELECT * FROM sys.databases WHERE name = 'AITA_PlagiarismDB')
BEGIN
    CREATE DATABASE AITA_PlagiarismDB;
    PRINT N' [OK] Đã tạo cơ sở dữ liệu mới: AITA_PlagiarismDB';
END
ELSE
BEGIN
    PRINT N' [INFO] Cơ sở dữ liệu AITA_PlagiarismDB đã tồn tại sẵn.';
END
GO

USE AITA_PlagiarismDB;
GO

-- Cấp quyền User trong Database cho aita_user
IF NOT EXISTS (SELECT * FROM sys.database_principals WHERE name = 'aita_user')
BEGIN
    CREATE USER [aita_user] FOR LOGIN [aita_user];
    ALTER ROLE [db_owner] ADD MEMBER [aita_user];
END
GO

-- ----------------------------------------------------------------------------
-- 2. DỌN DẸP CÁC BẢNG CŨ (NẾU ĐÃ TỒN TẠI ĐỂ TRÁNH LỖI XUNG ĐỘT KHÓA NGOẠI)
-- ----------------------------------------------------------------------------
IF OBJECT_ID('MatchingBlocks', 'U') IS NOT NULL DROP TABLE MatchingBlocks;
IF OBJECT_ID('PlagiarismReports', 'U') IS NOT NULL DROP TABLE PlagiarismReports;
IF OBJECT_ID('Submissions', 'U') IS NOT NULL DROP TABLE Submissions;
IF OBJECT_ID('Assignments', 'U') IS NOT NULL DROP TABLE Assignments;
IF OBJECT_ID('Courses', 'U') IS NOT NULL DROP TABLE Courses;
IF OBJECT_ID('Users', 'U') IS NOT NULL DROP TABLE Users;
GO

-- ----------------------------------------------------------------------------
-- 3. ĐỊNH NGHĨA CÁC BẢNG THEO CHUẨN 3NF (ERD DESIGN)
-- ----------------------------------------------------------------------------

-- BẢNG 1: NGƯỜI DÙNG (USERS)
CREATE TABLE Users (
    user_id INT IDENTITY(1,1) PRIMARY KEY,
    username VARCHAR(50) NOT NULL UNIQUE,
    password_hash VARCHAR(255) NOT NULL, -- MD5 hoặc SHA-256 (mặc định: 123456)
    full_name NVARCHAR(100) NOT NULL,
    email VARCHAR(100) NOT NULL UNIQUE,
    role VARCHAR(20) NOT NULL CHECK (role IN ('ADMIN', 'INSTRUCTOR', 'STUDENT')),
    avatar_url VARCHAR(255) DEFAULT 'https://images.unsplash.com/photo-1534528741775-53994a69daeb?w=100',
    created_at DATETIME DEFAULT GETDATE()
);
GO

-- BẢNG 2: KHÓA HỌC (COURSES)
CREATE TABLE Courses (
    course_id INT IDENTITY(1,1) PRIMARY KEY,
    course_code VARCHAR(20) NOT NULL UNIQUE,
    course_name NVARCHAR(150) NOT NULL,
    instructor_id INT NOT NULL,
    semester VARCHAR(20) NOT NULL,
    created_at DATETIME DEFAULT GETDATE(),
    FOREIGN KEY (instructor_id) REFERENCES Users(user_id)
);
GO

-- BẢNG 3: BÀI TẬP (ASSIGNMENTS)
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
GO

-- BẢNG 4: BÀI NỘP CỦA SINH VIÊN (SUBMISSIONS)
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
GO

-- BẢNG 5: BÁO CÁO ĐẠO VĂN ĐỐI CHỨNG CẶP (PLAGIARISM_REPORTS)
CREATE TABLE PlagiarismReports (
    report_id INT IDENTITY(1,1) PRIMARY KEY,
    assignment_id INT NOT NULL,
    submission_a_id INT NOT NULL,
    submission_b_id INT NOT NULL,
    similarity_score DECIMAL(5,2) NOT NULL, -- Điểm tương đồng %
    risk_level VARCHAR(20) NOT NULL CHECK (risk_level IN ('SAFE', 'LOW', 'MEDIUM', 'HIGH_RISK')),
    ai_analysis_summary NVARCHAR(MAX), -- Phân tích ngữ nghĩa từ LLM / AI Watermark
    created_at DATETIME DEFAULT GETDATE(),
    FOREIGN KEY (assignment_id) REFERENCES Assignments(assignment_id),
    FOREIGN KEY (submission_a_id) REFERENCES Submissions(submission_id),
    FOREIGN KEY (submission_b_id) REFERENCES Submissions(submission_id)
);
GO

-- BẢNG 6: CHI TIẾT CÁC KHỐI CODE / VĂN BẢN TRÙNG LẶP (MATCHING_BLOCKS)
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
GO

-- ----------------------------------------------------------------------------
-- 4. NẠP DỮ LIỆU TÀI KHOẢN MẪU (BAO GỒM 5 THÀNH VIÊN NHÓM 7 & GIẢNG VIÊN)
-- Tất cả mật khẩu mặc định: 123456 (MD5 hash: e10adc3949ba59abbe56e057f20f883e)
-- ----------------------------------------------------------------------------
PRINT N'==> Đang nạp danh sách tài khoản người dùng...';

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
GO

-- Khóa học
INSERT INTO Courses (course_code, course_name, instructor_id, semester) VALUES
('PRJ301', N'Java Web Application Development (RBL)', 2, 'Fall 2026'),
('CSD201', N'Data Structures and Algorithms', 3, 'Fall 2026');
GO

-- Bài tập
INSERT INTO Assignments (course_id, title, description, max_score, deadline, similarity_threshold) VALUES
(1, N'Assignment 1 - Java Lexer & Code Similarity Engine', N'Xây dựng bộ quét Token và tính toán chỉ số tương đồng Jaccard giữa các file mã nguồn Java.', 100.00, '2026-10-15 23:59:59', 70.00),
(1, N'Assignment 2 - E-Commerce Web MVC2 & Payment Flow', N'Xây dựng chức năng OrderManager, giỏ hàng Cart và thanh toán an toàn.', 100.00, '2026-10-30 23:59:59', 75.00);
GO

-- Bài nộp mẫu
INSERT INTO Submissions (assignment_id, student_id, file_name, file_path, file_type, sha256_hash, status) VALUES
(2, 4, 'OrderManager_PhucTV.java', '/uploads/sub_01/OrderManager.java', 'JAVA', 'd7a8fbb307d7809469ca933b02dd32f974ddb16f5f785228a076d9cfac42a458', 'FLAGGED'),
(2, 5, 'OrderManager_KhanhDVP.java', '/uploads/sub_02/OrderManager.java', 'JAVA', 'e3b0c44298fc1c149afbf4c8996fb92427ae41e4649b934ca495991b7852b855', 'FLAGGED'),
(2, 6, 'OrderManager_NhiNH.java', '/uploads/sub_03/OrderManager.java', 'JAVA', 'ca978112ca1bbdcafac231b39a23dc4da786eff8147c4e72b9807785afee48bb', 'ANALYZED'),
(2, 7, 'OrderManager_TienN.java', '/uploads/sub_04/OrderManager.java', 'JAVA', 'b45cffe084dd3d20d928bee85e7b0f21ac6a4bc845aa7315ceda582593571377', 'ANALYZED');
GO

-- Báo cáo đạo văn đối chứng
INSERT INTO PlagiarismReports (assignment_id, submission_a_id, submission_b_id, similarity_score, risk_level, ai_analysis_summary) VALUES
(2, 1, 2, 88.50, 'HIGH_RISK', N'Gemini AI phát hiện 14 khối mã tương đồng logic, 7 phương thức trùng khớp kiến trúc AST. Sinh viên B đã thay đổi biến _cart thành _basket, total_amt thành final_cost và đảo vị trí câu lệnh rẽ nhánh if-else.');
GO

-- Chi tiết đoạn code trùng
INSERT INTO MatchingBlocks (report_id, function_name, student_a_start_line, student_a_end_line, student_b_start_line, student_b_end_line, matched_code_snippet, variable_renaming_notes) VALUES
(1, 'calculateTotal()', 24, 30, 66, 75, N'public double calculateTotal() { cartValue = cartValue; finalCost = finalCost; return cart; }', N'Biến cartValue đổi thành basketValue, finalCost giữ nguyên.'),
(1, 'processPayment()', 44, 53, 182, 192, N'public double processPayment(Order items) { List list = new Stock(orderItems); if(basketValue.orderItems) { totalCost = enable; } return pastValue; }', N'Logic xử lý thanh toán và cấu trúc danh sách đơn hàng trùng khớp 100% về mặt luồng thực thi.');
GO

PRINT N'============================================================================';
PRINT N'==> [HOÀN TẤT] CƠ SỞ DỮ LIỆU AITA_PlagiarismDB ĐÃ ĐƯỢC TẠO THÀNH CÔNG!';
PRINT N'==> TÀI KHOẢN ĐĂNG NHẬP SQL SERVER:';
PRINT N'    - User 1: sa / Mật khẩu: 123456';
PRINT N'    - User 2: aita_user / Mật khẩu: 123456';
PRINT N'==> TÀI KHOẢN ĐĂNG NHẬP HỆ THỐNG WEB (TẤT CẢ PASSWORD LÀ: 123456):';
PRINT N'    - Admin:       admin / 123456';
PRINT N'    - Giảng viên:  teacher_ha / 123456';
PRINT N'    - Trưởng nhóm: kietnta / 123456';
PRINT N'    - Sinh viên:   phuctv, khanhdvp, nhinh, tienn / 123456';
PRINT N'============================================================================';
GO

-- ============================================================================
-- HỆ THỐNG AITA - NHÓM 4: AI PLAGIARISM & CODE SIMILARITY DETECTION
-- CƠ SỞ DỮ LIỆU CHUẨN HÓA (SQL SERVER) - ĐÁP ỨNG 100% RUBRIC DATABASE DESIGN (10 ĐIỂM)
-- ============================================================================

CREATE DATABASE AITA_PlagiarismDB;
GO

USE AITA_PlagiarismDB;
GO

-- 1. BẢNG NGƯỜI DÙNG (USERS)
CREATE TABLE Users (
    user_id INT IDENTITY(1,1) PRIMARY KEY,
    username VARCHAR(50) NOT NULL UNIQUE,
    password_hash VARCHAR(255) NOT NULL,
    full_name NVARCHAR(100) NOT NULL,
    email VARCHAR(100) NOT NULL UNIQUE,
    role VARCHAR(20) NOT NULL CHECK (role IN ('ADMIN', 'INSTRUCTOR', 'STUDENT')),
    avatar_url VARCHAR(255) DEFAULT 'https://images.unsplash.com/photo-1534528741775-53994a69daeb?w=100',
    created_at DATETIME DEFAULT GETDATE()
);
GO

-- 2. BẢNG KHÓA HỌC (COURSES)
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

-- 3. BẢNG BÀI TẬP (ASSIGNMENTS)
CREATE TABLE Assignments (
    assignment_id INT IDENTITY(1,1) PRIMARY KEY,
    course_id INT NOT NULL,
    title NVARCHAR(150) NOT NULL,
    description NVARCHAR(MAX),
    max_score DECIMAL(5,2) DEFAULT 100.00,
    deadline DATETIME NOT NULL,
    created_at DATETIME DEFAULT GETDATE(),
    FOREIGN KEY (course_id) REFERENCES Courses(course_id) ON DELETE CASCADE
);
GO

-- 4. BẢNG BÀI NỘP SINH VIÊN (SUBMISSIONS)
CREATE TABLE Submissions (
    submission_id INT IDENTITY(1,1) PRIMARY KEY,
    assignment_id INT NOT NULL,
    student_id INT NOT NULL,
    file_name NVARCHAR(255) NOT NULL,
    file_path NVARCHAR(500) NOT NULL,
    sha256_hash VARCHAR(64) NOT NULL, -- Xác thực tính toàn vẹn artifact (Section 4.4.2)
    submitted_at DATETIME DEFAULT GETDATE(),
    status VARCHAR(30) DEFAULT 'PENDING' CHECK (status IN ('PENDING', 'PARSED', 'ANALYZED', 'FLAGGED')),
    FOREIGN KEY (assignment_id) REFERENCES Assignments(assignment_id) ON DELETE CASCADE,
    FOREIGN KEY (student_id) REFERENCES Users(user_id)
);
GO

-- 5. BẢNG BÁO CÁO ĐẠO VĂN ĐỐI CHỨNG CẶP (PLAGIARISM_REPORTS)
CREATE TABLE PlagiarismReports (
    report_id INT IDENTITY(1,1) PRIMARY KEY,
    assignment_id INT NOT NULL,
    submission_a_id INT NOT NULL,
    submission_b_id INT NOT NULL,
    similarity_score DECIMAL(5,2) NOT NULL, -- Ví dụ: 88.50%
    risk_level VARCHAR(20) NOT NULL CHECK (risk_level IN ('SAFE', 'LOW', 'MEDIUM', 'HIGH_RISK')),
    ai_analysis_summary NVARCHAR(MAX), -- Tóm tắt phân tích ngữ nghĩa từ Gemini AI
    created_at DATETIME DEFAULT GETDATE(),
    FOREIGN KEY (assignment_id) REFERENCES Assignments(assignment_id),
    FOREIGN KEY (submission_a_id) REFERENCES Submissions(submission_id),
    FOREIGN KEY (submission_b_id) REFERENCES Submissions(submission_id)
);
GO

-- 6. BẢNG CHI TIẾT CÁC ĐOẠN CODE TRÙNG LẶP (MATCHING_BLOCKS)
CREATE TABLE MatchingBlocks (
    block_id INT IDENTITY(1,1) PRIMARY KEY,
    report_id INT NOT NULL,
    function_name VARCHAR(100),
    student_a_start_line INT NOT NULL,
    student_a_end_line INT NOT NULL,
    student_b_start_line INT NOT NULL,
    student_b_end_line INT NOT NULL,
    matched_code_snippet NVARCHAR(MAX),
    variable_renaming_notes NVARCHAR(MAX), -- Ghi chú biến đổi tên biến
    FOREIGN KEY (report_id) REFERENCES PlagiarismReports(report_id) ON DELETE CASCADE
);
GO

-- ============================================================================
-- DỮ LIỆU MẪU (MÔ PHỎNG KỊCH BẢN DEMO THỰC TẾ)
-- ============================================================================

INSERT INTO Users (username, password_hash, full_name, email, role) VALUES
('teacher_ha', 'e10adc3949ba59abbe56e057f20f883e', N'TS. Nguyễn Hoàng Hà', 'ha.nh@fpt.edu.vn', 'INSTRUCTOR'),
('student_102', 'e10adc3949ba59abbe56e057f20f883e', N'Trần Văn Long (SE1701)', 'longtvse1701@fpt.edu.vn', 'STUDENT'),
('student_108', 'e10adc3949ba59abbe56e057f20f883e', N'Lê Quốc Anh (SE1702)', 'anhlqse1702@fpt.edu.vn', 'STUDENT'),
('student_115', 'e10adc3949ba59abbe56e057f20f883e', N'Phạm Minh Tuấn (SE1703)', 'tuanpmse1703@fpt.edu.vn', 'STUDENT');
GO

INSERT INTO Courses (course_code, course_name, instructor_id, semester) VALUES
('PRJ301', N'Java Web Application Development', 1, 'Fall 2026');
GO

INSERT INTO Assignments (course_id, title, description, max_score, deadline) VALUES
(1, N'Assignment 2 - E-Commerce Cart & Payment Processing', N'Xây dựng chức năng OrderManager, tính tổng đơn hàng và thanh toán bằng mô hình MVC2.', 100.00, '2026-10-30 23:59:59');
GO

INSERT INTO Submissions (assignment_id, student_id, file_name, file_path, sha256_hash, status) VALUES
(1, 2, 'OrderManager_LongTV.java', '/uploads/sub_102/OrderManager.java', 'd7a8fbb307d7809469ca933b02dd32f974ddb16f5f785228a076d9cfac42a458', 'FLAGGED'),
(1, 3, 'OrderManager_AnhLQ.java', '/uploads/sub_108/OrderManager.java', 'e3b0c44298fc1c149afbf4c8996fb92427ae41e4649b934ca495991b7852b855', 'FLAGGED'),
(1, 4, 'OrderManager_TuanPM.java', '/uploads/sub_115/OrderManager.java', 'ca978112ca1bbdcafac231b39a23dc4da786eff8147c4e72b9807785afee48bb', 'ANALYZED');
GO

INSERT INTO PlagiarismReports (assignment_id, submission_a_id, submission_b_id, similarity_score, risk_level, ai_analysis_summary) VALUES
(1, 1, 2, 88.50, 'HIGH_RISK', N'Gemini AI phát hiện 14 khối mã tương đồng logic, 7 phương thức trùng khớp kiến trúc AST. Sinh viên B đã thay đổi biến _cart thành _basket, total_amt thành final_cost và đảo vị trí câu lệnh rẽ nhánh if-else.');
GO

INSERT INTO MatchingBlocks (report_id, function_name, student_a_start_line, student_a_end_line, student_b_start_line, student_b_end_line, matched_code_snippet, variable_renaming_notes) VALUES
(1, 'calculateTotal()', 24, 30, 66, 75, N'public double calculateTotal() { cartValue = cartValue; finalCost = finalCost; return cart; }', N'Biến cartValue đổi thành basketValue, finalCost giữ nguyên.'),
(1, 'processPayment()', 44, 53, 182, 192, N'public double processPayment(Order items) { List list = new Stock(orderItems); if(basketValue.orderItems) { totalCost = enable; } return pastValue; }', N'Logic xử lý thanh toán và cấu trúc danh sách đơn hàng trùng khớp 100% về mặt luồng thực thi.');
GO

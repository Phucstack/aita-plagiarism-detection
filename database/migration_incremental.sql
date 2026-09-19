-- ======================================================================
-- AITA — Migration tăng dần (incremental), KHÔNG chứa USE.
-- Luôn chạy đúng database được chỉ định qua tham số -d của sqlcmd:
--     sqlcmd -S <server> -E -d <TenDB> -i database/migration_incremental.sql -b -I
-- Idempotent: chỉ tạo phần còn thiếu; không DROP dữ liệu, không reset seed,
-- không đổi login. An toàn chạy lại nhiều lần.
-- ======================================================================
SET QUOTED_IDENTIFIER ON;
SET ANSI_NULLS ON;
SET ANSI_PADDING ON;
SET ANSI_WARNINGS ON;
SET CONCAT_NULL_YIELDS_NULL ON;
SET ARITHABORT ON;
SET NUMERIC_ROUNDABORT OFF;
GO

-- Google Identity: liên kết tài khoản có sẵn với Google subject.
IF COL_LENGTH('dbo.Users', 'google_subject') IS NULL
    ALTER TABLE dbo.Users ADD google_subject VARCHAR(255) NULL;
GO
IF NOT EXISTS (SELECT 1 FROM sys.indexes WHERE name = 'UX_Users_GoogleSubject' AND object_id = OBJECT_ID('dbo.Users'))
    EXEC('CREATE UNIQUE INDEX UX_Users_GoogleSubject ON dbo.Users(google_subject) WHERE google_subject IS NOT NULL');
GO

-- Dấu thời điểm đổi mật khẩu gần nhất; AuthFilter thu hồi mọi JWT phát hành trước mốc này.
IF COL_LENGTH('dbo.Users', 'password_changed_at') IS NULL
    ALTER TABLE dbo.Users ADD password_changed_at DATETIME NULL;
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

-- ----------------------------------------------------------------------
-- Ràng buộc role tầng DB (lớp phòng thủ bổ sung; enforcement chính ở DAO).
-- Giới hạn: CHECK dùng UDF KHÔNG re-validate khi role trên Users thay đổi
-- sau đó, và WITH NOCHECK không kiểm tra lại dữ liệu cũ (hệ thống cho phép
-- giảng viên nộp bài mẫu). Chi tiết xem database/NORMALIZATION_ANALYSIS.md.
-- ----------------------------------------------------------------------

-- Scalar function trả 1 nếu user tồn tại với role yêu cầu, ngược lại 0.
-- Thứ tự bắt buộc: DROP các CHECK đang tham chiếu TRƯỚC, rồi mới tạo lại function
-- (SQL Server không cho ALTER function đang được constraint tham chiếu), cuối cùng
-- tạo lại constraint. Chạy lại script nhiều lần vẫn an toàn.
IF OBJECT_ID(N'dbo.FN_UserHasRole', N'FN') IS NOT NULL
BEGIN
    IF EXISTS (SELECT 1 FROM sys.check_constraints WHERE name = N'CK_Courses_InstructorRole')
        ALTER TABLE dbo.Courses DROP CONSTRAINT CK_Courses_InstructorRole;
    IF EXISTS (SELECT 1 FROM sys.check_constraints WHERE name = N'CK_Submissions_StudentRole')
        ALTER TABLE dbo.Submissions DROP CONSTRAINT CK_Submissions_StudentRole;
    DROP FUNCTION dbo.FN_UserHasRole;
END;
GO

CREATE FUNCTION dbo.FN_UserHasRole(@userId INT, @role VARCHAR(20))
RETURNS BIT
AS
BEGIN
    DECLARE @result BIT = 0;
    IF EXISTS (SELECT 1 FROM dbo.Users WHERE user_id = @userId AND role = @role)
        SET @result = 1;
    RETURN @result;
END;
GO

ALTER TABLE dbo.Courses WITH NOCHECK
    ADD CONSTRAINT CK_Courses_InstructorRole
    CHECK (dbo.FN_UserHasRole(instructor_id, 'INSTRUCTOR') = 1
        OR dbo.FN_UserHasRole(instructor_id, 'ADMIN') = 1);
GO

ALTER TABLE dbo.Submissions WITH NOCHECK
    ADD CONSTRAINT CK_Submissions_StudentRole
    CHECK (dbo.FN_UserHasRole(student_id, 'STUDENT') = 1);
GO

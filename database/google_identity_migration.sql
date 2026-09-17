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

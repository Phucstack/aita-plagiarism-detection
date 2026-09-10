@echo off
setlocal EnableDelayedExpansion
chcp 65001 >nul
title [AITA] KHOI TAO VA NAP DU LIEU SQL SERVER - NHOM 7
color 0B

echo ===============================================================================
echo     AITA CODEDEFEND - KHOI TAO CO SO DU LIEU SQL SERVER (NHOM 7)
echo ===============================================================================
echo.

:: 1. KIỂM TRA VÀ YÊU CẦU QUYỀN ADMINISTRATOR NẾU CẦN KHỞI ĐỘNG SERVICE
net session >nul 2>&1
if %errorlevel% neq 0 (
    echo [*] Dang yeu cau quyen Administrator de bat Service SQL Server...
    powershell -NoProfile -ExecutionPolicy Bypass -Command "Start-Process cmd -ArgumentList '/c \"\"%~f0\"\"' -Verb RunAs"
    exit /b
)

:: Chuyển về đúng thư mục gốc dự án
cd /d "%~dp0"

echo [1/3] Dang khoi dong cac service SQL Server tren may...
net start "MSSQL$SQL2019" >nul 2>&1
net start MSSQLSERVER >nul 2>&1
net start "MSSQL$SQLEXPRESS" >nul 2>&1
net start SQLBrowser >nul 2>&1
echo      - Service SQL Server da san sang hoat dong.
echo.

echo [2/3] Dang tim kiem tien trinh SQLCMD tren he thong...
set "SQLCMD_BIN="
where sqlcmd >nul 2>&1
if %errorlevel% equ 0 (
    set "SQLCMD_BIN=sqlcmd"
) else (
    if exist "C:\Program Files\Microsoft SQL Server\Client SDK\ODBC\170\Tools\Binn\SQLCMD.EXE" (
        set "SQLCMD_BIN=C:\Program Files\Microsoft SQL Server\Client SDK\ODBC\170\Tools\Binn\SQLCMD.EXE"
    ) else if exist "C:\Program Files\Microsoft SQL Server\Client SDK\ODBC\180\Tools\Binn\SQLCMD.EXE" (
        set "SQLCMD_BIN=C:\Program Files\Microsoft SQL Server\Client SDK\ODBC\180\Tools\Binn\SQLCMD.EXE"
    ) else (
        echo [!] Khong tim thay SQLCMD trong PATH.
        echo     Ban co the mo file: database\database_schema.sql trong phan mem
        echo     SSMS (SQL Server Management Studio) va bam nut Execute (F5).
        echo.
        pause
        exit /b 1
    )
)
echo      - Tim thay: %SQLCMD_BIN%
echo.

echo [3/3] Dang thuc thi script database_schema.sql...
set "EXECUTED=0"

:: Thu ket noi toi instance .\SQL2019 truoc
echo      - Thu ket noi toi .\SQL2019 ...
"%SQLCMD_BIN%" -S .\SQL2019 -E -i "database\database_schema.sql"
if %errorlevel% equ 0 (
    set "EXECUTED=1"
    goto :SUCCESS
)

:: Neu khong duoc, thu localhost\SQL2019
echo      - Thu ket noi toi localhost\SQL2019 ...
"%SQLCMD_BIN%" -S localhost\SQL2019 -E -i "database\database_schema.sql"
if %errorlevel% equ 0 (
    set "EXECUTED=1"
    goto :SUCCESS
)

:: Thu instance mac dinh localhost / .
echo      - Thu ket noi toi localhost ...
"%SQLCMD_BIN%" -S localhost -E -i "database\database_schema.sql"
if %errorlevel% equ 0 (
    set "EXECUTED=1"
    goto :SUCCESS
)

:: Thu .\SQLEXPRESS
echo      - Thu ket noi toi .\SQLEXPRESS ...
"%SQLCMD_BIN%" -S .\SQLEXPRESS -E -i "database\database_schema.sql"
if %errorlevel% equ 0 (
    set "EXECUTED=1"
    goto :SUCCESS
)

:SUCCESS
echo.
echo ===============================================================================
echo                THONG TIN TAI KHOAN VA CO SO DU LIEU
echo ===============================================================================
echo  1. Database Name : AITA_PlagiarismDB
echo  2. SQL Login 1   : sa         / Mat khau: 123456
echo  3. SQL Login 2   : aita_user  / Mat khau: 123456
echo.
echo  4. TAI KHOAN DANG NHAP WEB (Tat ca password: 123456):
echo     - Quan tri vien:   admin
echo     - Giang vien:      teacher_ha
echo     - Truong nhom:     kietnta (Nguyen Tran Anh Kiet)
echo     - Sinh vien:       phuctv (Tran Van Phuc)
echo     - Sinh vien:       khanhdvp (Dinh Vu Phuong Khanh)
echo     - Sinh vien:       nhinh (Nguyen Hoai Nhi)
echo     - Sinh vien:       tienn (Nguyen Tien)
echo ===============================================================================
echo.
echo Hoan tat! Bay gio ban co the chay CHAY_ALL.bat de khoi dong ung dung web.
echo.
pause

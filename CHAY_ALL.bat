@echo off
chcp 65001 >nul
setlocal enabledelayedexpansion
title AITA CodeDefend - He Thong Giam Sat Liem Chinh va Dao Van Code [PRJ301]
color 0B

cd /d "%~dp0"

:MAIN_MENU
cls
echo ==============================================================================
echo       AITA CODEDEFEND - ACTIVE AST DEFENSE SUITE [PRJ301 - RBL SE20C GROUP 7]      
echo    HE THONG GIAM SAT LIEM CHINH HOC THUAT VA DOI SOAT DAO VAN MA NGUON JAVA  
echo ==============================================================================
echo.
echo [1] KIEM TRA MOI TRUONG MAY TINH:

rem Kiem tra Python
set "HAS_PYTHON=0"
where python >nul 2>&1
if !errorlevel! equ 0 set "HAS_PYTHON=1"
where py >nul 2>&1
if !errorlevel! equ 0 set "HAS_PYTHON=1"

if !HAS_PYTHON! equ 1 (
    echo   [+] Python        : SAN SANG [HTTP Server Chuyen Dung]
) else (
    echo   [-] Python        : CHUA CAI [Se su dung PowerShell thay the]
)

rem Kiem tra Node.js
set "HAS_NODE=0"
where node >nul 2>&1
if !errorlevel! equ 0 set "HAS_NODE=1"
if !HAS_NODE! equ 1 (
    echo   [+] Node.js       : SAN SANG
) else (
    echo   [-] Node.js       : CHUA CAI
)

rem Kiem tra PowerShell
echo   [+] PowerShell    : SAN SANG [Native Windows HTTP Engine 100%% Ho tro]

rem Kiem tra Java JDK
set "HAS_JAVA=0"
where java >nul 2>&1
if !errorlevel! equ 0 set "HAS_JAVA=1"
if !HAS_JAVA! equ 1 (
    echo   [+] Java Runtime  : SAN SANG [JDK 17+]
) else (
    echo   [-] Java Runtime  : CHUA CAI [Can JDK 17+ de bien dich Java Backend]
)

rem Kiem tra Maven
set "HAS_MAVEN=0"
where mvn >nul 2>&1
if !errorlevel! equ 0 set "HAS_MAVEN=1"
if !HAS_MAVEN! equ 1 (
    echo   [+] Apache Maven  : SAN SANG [Quan ly build va dong goi WAR]
) else (
    echo   [-] Apache Maven  : CHUA CAI
)

echo.
echo ==============================================================================
echo                           DANH MUC LUA CHON CHUC NANG
echo ==============================================================================
echo   [1] KHOI CHAY WEB DEMO VA SHOWCASE [Khuyen dung - 1 Click mo ngay trinh duyet]
echo       -^> Mo Landing Page 3D Scrollytelling, Form Dang Nhap, Dashboard, SV Portal
echo       -^> Preview tinh, khong thay the ung dung Java/JDBC
echo.
echo   [2] BUILD DONG GOI DU AN JAVA PRJ301 [Maven Package WAR]
echo       -^> Tu dong thiet lap UTF-8, bien dich 21 classes, tao file .war cho Tomcat
echo.
echo   [3] CHAY KIEM THU TU DONG [JUnit 5 Multi-Scenario Tests]
echo       -^> Kiem tra toan ven thuat toan bam SHA-256, AST Token, JWT Token
echo.
echo   [4] CHAY UNG DUNG JAVA [BUILD WAR + TOMCAT 10.1]
echo       -^> Dang nhap va JDBC that; can cau hinh .env va SQL Server
echo.
echo   [5] HUONG DAN CAU HINH CO SO DU LIEU SQL SERVER VA TOMCAT
echo       -^> Xem thong tin chuoi ket noi DBContext va cach trien khai Tomcat 10.1+
echo.
echo   [6] MO TAI LIEU DO AN VA RUBRIC DANH GIA [Word DOCX]
echo       -^> Mo Rubric cham diem va De cuong bao cao de tai PRJ301
echo.
echo   [7] KHOI CHAY TRO LY GIONG NOI REALTIME [ZeroTTS Port 8008]
echo       -^> Khoi dong FastAPI ONNX Voice Server phuc vu AI Web Copilot
echo.
echo   [0] THOAT
echo ==============================================================================
echo.

choice /C 12345670 /T 8 /D 1 /M "Nhap lua chon cua ban [Tu dong chay 1 sau 8 giay]: "
set "CHOICE_VAL=%errorlevel%"

if "%CHOICE_VAL%"=="1" goto ACTION_RUN_WEB
if "%CHOICE_VAL%"=="2" goto ACTION_BUILD_JAVA
if "%CHOICE_VAL%"=="3" goto ACTION_RUN_TESTS
if "%CHOICE_VAL%"=="4" goto ACTION_RUN_ALL
if "%CHOICE_VAL%"=="5" goto ACTION_GUIDE_DB
if "%CHOICE_VAL%"=="6" goto ACTION_OPEN_DOCS
if "%CHOICE_VAL%"=="7" goto ACTION_RUN_ZEROTTS
if "%CHOICE_VAL%"=="8" goto ACTION_EXIT
goto MAIN_MENU

:: ==============================================================================
:: ACTION 1: Khoi chay Web Server & Mo Trinh Duyet
:: ==============================================================================
:ACTION_RUN_WEB
cls
echo ==============================================================================
echo                DANG KHOI DONG HE THONG WEB SHOWCASE LOCAL
echo ==============================================================================
echo.

set "PORT=8089"

rem Kiem tra port 8089 co dang chay san khong
netstat -ano | findstr /r /c:":8089.*LISTENING" >nul 2>&1
if !errorlevel! equ 0 (
    echo [*] Cong !PORT! da co may chu hoat dong san. Dang mo trinh duyet...
    goto OPEN_BROWSER
)

rem Khoi chay tuy theo cong cu co san
if !HAS_PYTHON! equ 1 (
    echo [*] Su dung Python HTTP Engine tai cong !PORT!...
    where python >nul 2>&1
    if !errorlevel! equ 0 (
        start "AITA Web Server" /min python -m http.server !PORT! --directory "%~dp0web"
    ) else (
        start "AITA Web Server" /min py -m http.server !PORT! --directory "%~dp0web"
    )
    goto WAIT_AND_OPEN
)

if !HAS_NODE! equ 1 (
    echo [*] Su dung Node.js tai cong !PORT!...
    start "AITA Web Server" /min npx --yes serve "%~dp0web" -l !PORT! -s
    goto WAIT_AND_OPEN
)

rem Fallback 100% tren moi may Windows bang PowerShell
echo [*] Khoi chay Native Windows PowerShell HTTP Server tai cong !PORT!...
start "AITA Web Server" powershell -NoProfile -ExecutionPolicy Bypass -File "%~dp0tools\server.ps1" -Port !PORT! -WebRoot "%~dp0web"

:WAIT_AND_OPEN
echo [*] Dang chuan bi tai nguyen va nap 240 khung hinh 3D Scrollytelling...

rem Kiem tra va khoi chay ZeroTTS Voice Engine (Port 8008)
netstat -ano | findstr /r /c:":8008.*LISTENING" >nul 2>&1
if !errorlevel! neq 0 (
    echo [*] Tu dong khoi chay ZeroTTS Realtime Voice Engine tai cong 8008...
    start "AITA ZeroTTS Voice Engine" /min cmd /c "%~dp0START_ZEROTTS_SERVICE.bat"
) else (
    echo [+] ZeroTTS Voice Engine da san sang tai cong 8008 [Realtime Speech]
)

ping -n 3 127.0.0.1 >nul 2>&1

:OPEN_BROWSER
echo.
echo ==============================================================================
echo [OK] HE THONG DA SAN SANG! DANG MO TRINH DUYET...
echo ==============================================================================
echo   - Landing Page 3D Scrollytelling : http://localhost:!PORT!/preview/index.html
echo   - Trang Dang Nhap He Thong       : http://localhost:!PORT!/preview/login.html
echo   - Dashboard Giang Vien / Khao Thi: http://localhost:!PORT!/preview/dashboard.html
echo   - Cong Tra Cuu Sinh Vien         : http://localhost:!PORT!/preview/student-portal.html
echo ==============================================================================
echo.
start http://localhost:!PORT!/preview/index.html
echo [i] Nhan phim bat ky de quay lai Menu chinh.
pause >nul
goto MAIN_MENU

:: ==============================================================================
:: ACTION 2: Build Dong Goi Du An Java (WAR)
:: ==============================================================================
:ACTION_BUILD_JAVA
cls
echo ==============================================================================
echo                BIEN DICH VA DONG GOI DU AN JAVA PRJ301 [WAR]
echo ==============================================================================
echo.

if !HAS_JAVA! equ 0 (
    echo [LOI] May tinh chua duoc cai dat Java JDK 17+ hoac chua thiet lap JAVA_HOME.
    echo Vui long cai dat JDK 17 de tiep tuc.
    echo.
    pause
    goto MAIN_MENU
)

if !HAS_MAVEN! equ 0 (
    echo [LOI] Khong tim thay lenh 'mvn' [Apache Maven] trong PATH.
    echo Vui long cai dat Maven hoac mo truc tiep du an trong NetBeans / IntelliJ.
    echo.
    pause
    goto MAIN_MENU
)

echo [*] Thiet lap ma hoa UTF-8 cho Maven de xu ly duong dan tieng Viet...
set MAVEN_OPTS=-Dfile.encoding=UTF-8

echo [*] Dang thuc thi: mvn clean package -DskipTests
echo.
call mvn clean package -DskipTests

if !errorlevel! equ 0 (
    echo.
    echo ==============================================================================
    echo [THANH CONG] DA DONG GOI XONG FILE WAR CHO DU AN PRJ301!
    echo ==============================================================================
    echo Vi tri file: %~dp0target\aita-plagiarism-detection-1.0.0-SNAPSHOT.war
    echo.
    echo Huong dan trien khai len Apache Tomcat:
    echo   1. Sao chep file .war vao thu muc 'webapps' cua Apache Tomcat 10.1+.
    echo   2. Khoi dong Tomcat va truy cap: http://localhost:8080/aita-plagiarism-detection-1.0.0-SNAPSHOT/
    echo ==============================================================================
) else (
    echo.
    echo [THAT BAI] Qua trinh bien dich gap loi. Vui long kiem tra log o tren.
)
echo.
pause
goto MAIN_MENU

:: ==============================================================================
:: ACTION 3: Chay Kiem Thu Tu Dong (JUnit 5)
:: ==============================================================================
:ACTION_RUN_TESTS
cls
echo ==============================================================================
echo                 CHAY BO KIEM THU TU DONG [JUNIT 5]
echo ==============================================================================
echo.

if !HAS_MAVEN! equ 0 (
    echo [LOI] Yeu cau Apache Maven de chay kiem thu tu dong.
    pause
    goto MAIN_MENU
)

set MAVEN_OPTS=-Dfile.encoding=UTF-8
echo [*] Dang thuc thi: mvn test
echo.
powershell -NoProfile -ExecutionPolicy Bypass -File "%~dp0tools\test-java.ps1"

echo.
pause
goto MAIN_MENU

:: ==============================================================================
:: ACTION 4: Chay Toan Dien (Build WAR + Bat Web Showcase)
:: ==============================================================================
:ACTION_RUN_ALL
powershell -NoProfile -ExecutionPolicy Bypass -File "%~dp0tools\run-java.ps1"
if errorlevel 1 (
    echo [ERROR] Java startup failed. Check the message above.
) else (
    echo Open http://localhost:8080/plagiarism/login
)
pause
goto MAIN_MENU

:: ==============================================================================
:: ACTION 5: Huong Dan Cau Hinh Database & Tomcat
:: ==============================================================================
:ACTION_GUIDE_DB
cls
echo ==============================================================================
echo             HUONG DAN CAU HINH DATABASE SQL SERVER VA APACHE TOMCAT
echo ==============================================================================
echo.
echo 1. CO SO DU LIEU MICROSOFT SQL SERVER:
echo    - File script tao bang con thieu: database\database_schema.sql [idempotent, khong DROP/reset du lieu]
echo    - Ten Database mac dinh: AITA_PlagiarismDB [test dung DB rieng co chu Test/Verification]
echo    - Cau hinh trong .env theo .env.example: DB_SERVER, DB_PORT, DB_NAME, DB_USER, DB_PASSWORD
echo    - DBContext dung encrypt=true; DB_TRUST_SERVER_CERTIFICATE=false mac dinh; chi true o SQL local tin cay.
echo    - Dung tai khoan DB gioi han quyen tren database ung dung; khong dung sa/sysadmin de chay web.
echo.
echo 2. CAU HINH XAC THUC VA GOOGLE LOGIN:
echo    - JWT_SECRET: chuoi ngau nhien toi thieu 32 byte trong .env; thieu se khong khoi dong duoc.
echo    - GOOGLE_CLIENT_ID: OAuth Web client trong .env; DB cu chay database\google_identity_migration.sql.
echo    - Email phai co tai khoan san trong Users; khong tu tao role tu trinh duyet.
echo.
echo 3. TRIEN KHAI LEN APACHE TOMCAT 10.1+ [Jakarta Servlet 6.0]:
echo    - Khuyen dung: powershell -NoProfile -ExecutionPolicy Bypass -File tools/run-java.ps1
echo    - Script build WAR va chay Tomcat base rieng tai target/java-runtime; dung -Stop de tat.
echo    - Truy cap: http://localhost:8080/plagiarism/login
echo    - Cach thu cong: copy target\aita-plagiarism-detection-1.0.0-SNAPSHOT.war vao [Tomcat_Home]\webapps\
echo.
echo 4. TAI KHOAN DEMO CUA NHOM [chi dung du lieu demo, khong in mat khau that tai day]:
echo    - Xem tai khoan seed trong database\database_schema.sql hoac lien he nhom SE20C-07.
echo    - Hoac bam nut 'Dang nhap bang tai khoan Google' sau khi cau hinh GOOGLE_CLIENT_ID.
echo ==============================================================================
echo.
echo Nhan phim 'O' de mo file database_schema.sql, hoac phim bat ky de quay lai.
choice /C ON /N /T 10 /D N /M "[O: Mo file SQL / N: Quay lai Menu]: "
if !errorlevel! equ 1 (
    start notepad "%~dp0database\database_schema.sql"
)
goto MAIN_MENU

:: ==============================================================================
:: ACTION 6: Mo Tai Lieu Do An & Rubric
:: ==============================================================================
:ACTION_OPEN_DOCS
cls
echo ==============================================================================
echo                      MO TAI LIEU VA BAO CAO DO AN PRJ301
echo ==============================================================================
echo.
echo [*] Dang mo Rubric danh gia do an PRJ30x...
if exist "%~dp0PRJ30x_Project_Evaluation_Rubric.docx" (
    start "" "%~dp0PRJ30x_Project_Evaluation_Rubric.docx"
)
echo [*] Dang mo De cuong du an RBL PRJ301...
if exist "%~dp0RBL PRJ301 Project.docx" (
    start "" "%~dp0RBL PRJ301 Project.docx"
)
echo.
echo [OK] Da gui yeu cau mo tai lieu.
pause
goto MAIN_MENU

:: ==============================================================================
:: ACTION 7: Khoi chay ZeroTTS Voice Engine (Port 8008)
:: ==============================================================================
:ACTION_RUN_ZEROTTS
cls
echo ==============================================================================
echo             KHOI CHAY ZEROTTS VOICE ENGINE (PORT 8008)
echo ==============================================================================
echo.
call "%~dp0START_ZEROTTS_SERVICE.bat"
goto MAIN_MENU

:ACTION_EXIT
cls
echo.
echo Cam on ban da su dung AITA CodeDefend! Chuc buoi bao ve do an thanh cong ruc ro!
echo.
ping -n 3 127.0.0.1 >nul 2>&1
exit /b 0

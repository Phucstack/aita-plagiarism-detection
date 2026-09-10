@echo off
chcp 65001 >nul
setlocal enabledelayedexpansion
title AITA CodeDefend - 1-Click Launch Web Showcase [PRJ301]
cd /d "%~dp0"

echo ==============================================================================
echo       AITA CODEDEFEND - KHOI DONG NHANH GIAO DIEN WEB SHOWCASE [PRJ301]       
echo ==============================================================================
echo.

set "PORT=8089"

rem Kiem tra neu cong 8089 dang chay thi mo trinh duyet luon
netstat -ano | findstr /r /c:":8089.*LISTENING" >nul 2>&1
if !errorlevel! equ 0 (
    echo [*] Cong !PORT! da san sang. Dang mo trinh duyet...
    start http://localhost:!PORT!/preview/index.html
    ping -n 3 127.0.0.1 >nul 2>&1
    exit /b 0
)

rem Kiem tra Python
where python >nul 2>&1
if !errorlevel! equ 0 (
    echo [*] Khoi chay may chu voi Python...
    start "AITA Web Server" /min python -m http.server !PORT! --directory "%~dp0web"
    goto OPEN_AND_EXIT
)

where py >nul 2>&1
if !errorlevel! equ 0 (
    echo [*] Khoi chay may chu voi Python Launcher...
    start "AITA Web Server" /min py -m http.server !PORT! --directory "%~dp0web"
    goto OPEN_AND_EXIT
)

rem Kiem tra Node
where node >nul 2>&1
if !errorlevel! equ 0 (
    echo [*] Khoi chay may chu voi Node.js...
    start "AITA Web Server" /min npx --yes serve "%~dp0web" -l !PORT! -s
    goto OPEN_AND_EXIT
)

rem Fallback 100% tren moi may Windows bang Native PowerShell
echo [*] Khoi chay may chu voi Native Windows PowerShell...
start "AITA Web Server" powershell -NoProfile -ExecutionPolicy Bypass -File "%~dp0tools\server.ps1" -Port !PORT! -WebRoot "%~dp0web"

:OPEN_AND_EXIT
ping -n 3 127.0.0.1 >nul 2>&1
start http://localhost:!PORT!/preview/index.html
echo [OK] Da mo trinh duyet tai: http://localhost:!PORT!/preview/index.html
ping -n 3 127.0.0.1 >nul 2>&1
exit /b 0

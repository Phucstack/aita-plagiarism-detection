@echo off
chcp 65001 >nul
setlocal enabledelayedexpansion
title AITA CodeDefend - ZeroTTS Realtime Voice Microservice [Port 8008]
color 0A

cd /d "%~dp0"

echo ==============================================================================
echo       AITA CODEDEFEND - ZEROTTS VIETNAMESE VOICE MICROSERVICE [PORT 8008]     
echo ==============================================================================
echo.

set "PYTHON_EXE=C:\Users\phucv\Downloads\ZeroTTS\.venv\Scripts\python.exe"

if not exist "!PYTHON_EXE!" (
    echo [*] Virtual environment chua co, thu su dung uv hoac python he thong...
    where uv >nul 2>&1
    if !errorlevel! equ 0 (
        echo [*] Su dung uv run de khoi chay...
        uv run --python "C:\Users\phucv\Downloads\ZeroTTS\.venv" tools\zerotts_service.py
        goto END
    )
    set "PYTHON_EXE=python"
)

echo [*] Dang khoi dong ZeroTTS Service tai http://127.0.0.1:8008 ...
echo [*] Cac giong doc tieng Viet san sang: maichi, baotrang, giahuy, hamy, huuduc...
echo.

"!PYTHON_EXE!" tools\zerotts_service.py

:END
pause

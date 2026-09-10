# ===============================================================================
# AITA CODEDEFEND - SUA LOI SERVICE SQL SERVER 2019 (ERROR CODE 13 / 0xD)
# ===============================================================================

[Console]::OutputEncoding = [System.Text.Encoding]::UTF8

Write-Host "===============================================================================" -ForegroundColor Cyan
Write-Host "          AITA - TIEN TRINH SUA LOI SQL SERVER (ERROR CODE 13)" -ForegroundColor Cyan
Write-Host "===============================================================================" -ForegroundColor Cyan
Write-Host ""

# 1. Kiem tra va yeu cau quyen Administrator
$isAdmin = ([Security.Principal.WindowsPrincipal][Security.Principal.WindowsIdentity]::GetCurrent()).IsInRole([Security.Principal.WindowsBuiltInRole]::Administrator)
if (-not $isAdmin) {
    Write-Host "[*] Dang yeu cau quyen Administrator de sua cau hinh Registry..." -ForegroundColor Yellow
    try {
        Start-Process powershell.exe -Verb RunAs -ArgumentList "-NoProfile -ExecutionPolicy Bypass -File `"$PSCommandPath`""
        exit
    } catch {
        Write-Host "[!] Khong the xin quyen Admin tu dong." -ForegroundColor Red
        Read-Host "Nhan Enter de thoat..."
        exit 1
    }
}

$tcpPath = "HKLM:\SOFTWARE\Microsoft\Microsoft SQL Server\MSSQL15.SQL2019\MSSQLServer\SuperSocketNetLib\Tcp"
$ipAllPath = "$tcpPath\IPAll"

Write-Host "[1/3] Dang khoi phuc gia tri chuan cho Registry TCP/IP IPAll..." -ForegroundColor Green
try {
    # Khoi phuc TcpDynamicPorts ve '0' va xoa rong TcpPort (mac dinh cua Named Instance SQL2019)
    Set-ItemProperty -Path $ipAllPath -Name "TcpDynamicPorts" -Value "0" -Type String -Force
    Set-ItemProperty -Path $ipAllPath -Name "TcpPort" -Value "" -Type String -Force
    Write-Host "      [OK] Da sua loi khoa IPAll (The data is invalid)." -ForegroundColor Green
} catch {
    Write-Host "      [!] Khong the ghi Registry: $($_.Exception.Message)" -ForegroundColor Red
}

Write-Host ""
Write-Host "[2/3] Dang khoi dong lai Service SQL Server (SQL2019)..." -ForegroundColor Green

# Thu khoi dong service
$svc = Get-Service -Name "MSSQL`$SQL2019" -ErrorAction SilentlyContinue
if ($svc) {
    try {
        Restart-Service -Name $svc.Name -Force -ErrorAction Stop
        Write-Host "      ==> DICH VU SQL SERVER (SQL2019) DA KHOI DONG THANH CONG (RUNNING)!" -ForegroundColor Green
    } catch {
        Write-Host "      [!] Khoi dong voi TCP gap truc trac, chuyen sang che do Shared Memory / Named Pipes an toan..." -ForegroundColor Yellow
        Set-ItemProperty -Path $tcpPath -Name "Enabled" -Value 0 -Type DWord -Force
        try {
            Start-Service -Name $svc.Name -ErrorAction Stop
            Write-Host "      ==> DICH VU SQL SERVER DA CHAY THANH CONG TREN SHARED MEMORY / NAMED PIPES!" -ForegroundColor Green
        } catch {
            Write-Host "      [!] Loi: $($_.Exception.Message)" -ForegroundColor Red
        }
    }
}

Write-Host ""
Write-Host "[3/3] Dang tu dong nap CSDL AITA_PlagiarismDB va tai khoan vao SQL Server..." -ForegroundColor Green
$rootDir = Split-Path -Parent $PSScriptRoot
$schemaFile = Join-Path $rootDir "database\database_schema.sql"

$sqlcmd = $null
if (Get-Command sqlcmd -ErrorAction SilentlyContinue) {
    $sqlcmd = "sqlcmd"
} elseif (Test-Path "C:\Program Files\Microsoft SQL Server\Client SDK\ODBC\170\Tools\Binn\SQLCMD.EXE") {
    $sqlcmd = "C:\Program Files\Microsoft SQL Server\Client SDK\ODBC\170\Tools\Binn\SQLCMD.EXE"
} elseif (Test-Path "C:\Program Files\Microsoft SQL Server\Client SDK\ODBC\180\Tools\Binn\SQLCMD.EXE") {
    $sqlcmd = "C:\Program Files\Microsoft SQL Server\Client SDK\ODBC\180\Tools\Binn\SQLCMD.EXE"
}

if ($sqlcmd) {
    Write-Host "      - Dang thuc thi file database_schema.sql ..." -ForegroundColor Gray
    $res = & $sqlcmd -S ".\SQL2019" -E -i "$schemaFile" -b 2>&1
    if ($LASTEXITCODE -eq 0) {
        Write-Host "      ==> [HOAN TAT] DA TAO CSDL VA 7 TAI KHOAN THANH CONG 100%!" -ForegroundColor Green
    } else {
        # Thu qua named pipe
        & $sqlcmd -S "np:\\.\pipe\MSSQL`$SQL2019\sql\query" -E -i "$schemaFile"
    }
}

Write-Host ""
Write-Host "===============================================================================" -ForegroundColor Cyan
Write-Host "                     THONG TIN KET NOI HOAN TAT" -ForegroundColor Cyan
Write-Host "===============================================================================" -ForegroundColor Cyan
Write-Host "  1. Server Name trong SSMS: .\SQL2019 (hoac PHUSC\SQL2019)" -ForegroundColor White
Write-Host "  2. Database Name         : AITA_PlagiarismDB" -ForegroundColor White
Write-Host "  3. SQL Login             : sa / aita_user (Mat khau: 123456)" -ForegroundColor White
Write-Host "  4. Tai khoan Web         : admin, kietnta, phuctv, khanhdvp, nhinh, tienn" -ForegroundColor Yellow
Write-Host "===============================================================================" -ForegroundColor Cyan
Write-Host ""
Read-Host "Nhan Enter de dong..."

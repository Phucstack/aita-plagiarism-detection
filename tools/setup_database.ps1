# ===============================================================================
# AITA CODEDEFEND - KHOI TAO VA NAP CO SO DU LIEU SQL SERVER (NHOM 7)
# ===============================================================================

$OutputEncoding = [System.Text.Encoding]::UTF8
[Console]::OutputEncoding = [System.Text.Encoding]::UTF8

Write-Host "===============================================================================" -ForegroundColor Cyan
Write-Host "     AITA CODEDEFEND - KHOI TAO CO SO DU LIEU SQL SERVER (NHOM 7)" -ForegroundColor Cyan
Write-Host "===============================================================================" -ForegroundColor Cyan
Write-Host ""

# 1. Kiem tra quyen Administrator
$isAdmin = ([Security.Principal.WindowsPrincipal][Security.Principal.WindowsIdentity]::GetCurrent()).IsInRole([Security.Principal.WindowsBuiltInRole]::Administrator)
if (-not $isAdmin) {
    Write-Host "[*] Dang yeu cau quyen Administrator de bat Service SQL Server..." -ForegroundColor Yellow
    try {
        Start-Process powershell.exe -Verb RunAs -ArgumentList "-NoProfile -ExecutionPolicy Bypass -File `"$PSCommandPath`""
        exit
    } catch {
        Write-Host "[!] Khong the xin quyen Admin tu dong. Vui long chuot phai vao TAO_DATABASE_SQL.bat chon 'Run as administrator'." -ForegroundColor Red
        Read-Host "Nhan Enter de thoat..."
        exit 1
    }
}

$rootDir = Split-Path -Parent $PSScriptRoot
Set-Location $rootDir

Write-Host "[1/3] Dang khoi dong cac Service SQL Server tren may..." -ForegroundColor Green

$services = @("MSSQL`$SQL2019", "MSSQLSERVER", "MSSQL`$SQLEXPRESS", "SQLBrowser")
foreach ($s in $services) {
    $svc = Get-Service -Name $s -ErrorAction SilentlyContinue
    if ($svc) {
        if ($svc.Status -ne "Running") {
            Write-Host "      - Dang bat service: $($svc.DisplayName)..." -ForegroundColor Yellow
            try {
                Start-Service -Name $svc.Name -ErrorAction Stop
                Write-Host "        [OK] Da khoi dong thanh cong $($svc.DisplayName)" -ForegroundColor Green
            } catch {
                Write-Host "        [!] Khong the bat $($svc.Name): $($_.Exception.Message)" -ForegroundColor DarkYellow
            }
        } else {
            Write-Host "      - Service $($svc.DisplayName) da dang chay." -ForegroundColor Gray
        }
    }
}

Write-Host ""
Write-Host "[2/3] Tim kiem tien trinh SQLCMD tren he thong..." -ForegroundColor Green

$sqlcmd = $null
if (Get-Command sqlcmd -ErrorAction SilentlyContinue) {
    $sqlcmd = "sqlcmd"
} elseif (Test-Path "C:\Program Files\Microsoft SQL Server\Client SDK\ODBC\170\Tools\Binn\SQLCMD.EXE") {
    $sqlcmd = "C:\Program Files\Microsoft SQL Server\Client SDK\ODBC\170\Tools\Binn\SQLCMD.EXE"
} elseif (Test-Path "C:\Program Files\Microsoft SQL Server\Client SDK\ODBC\180\Tools\Binn\SQLCMD.EXE") {
    $sqlcmd = "C:\Program Files\Microsoft SQL Server\Client SDK\ODBC\180\Tools\Binn\SQLCMD.EXE"
} else {
    Write-Host "[!] Khong tim thay SQLCMD tren he thong." -ForegroundColor Red
    Write-Host "    Ban co the mo file database\database_schema.sql trong phan mem SSMS va bam Execute (F5)." -ForegroundColor Yellow
    Read-Host "Nhan Enter de tiep tuc..."
    exit 1
}
Write-Host "      - Su dung: $sqlcmd" -ForegroundColor Gray
Write-Host ""

Write-Host "[3/3] Dang thuc thi database\database_schema.sql..." -ForegroundColor Green
$schemaFile = Join-Path $rootDir "database\database_schema.sql"

$instances = @(".\SQL2019", "localhost\SQL2019", "localhost", ".", "(local)", ".\SQLEXPRESS")
$success = $false

foreach ($inst in $instances) {
    Write-Host "      - Thu ket noi toi Instance: $inst ..." -ForegroundColor Gray
    try {
        $result = & $sqlcmd -S $inst -E -i "$schemaFile" -b 2>&1
        if ($LASTEXITCODE -eq 0) {
            Write-Host "      ==> KET NOI VA NAP DU LIEU THANH CONG TREN: $inst !" -ForegroundColor Green
            $success = $true
            break
        }
    } catch {
        # Thu instance tiep theo
    }
}

if (-not $success) {
    Write-Host ""
    Write-Host "[!] Ket noi qua cac instance mac dinh chua thanh cong. Dang thu chay chi tiet voi .\SQL2019:" -ForegroundColor Yellow
    & $sqlcmd -S ".\SQL2019" -E -i "$schemaFile"
}

Write-Host ""
Write-Host "===============================================================================" -ForegroundColor Cyan
Write-Host "                THONG TIN TAI KHOAN VA CO SO DU LIEU" -ForegroundColor Cyan
Write-Host "===============================================================================" -ForegroundColor Cyan
Write-Host "  1. Database Name : AITA_PlagiarismDB" -ForegroundColor White
Write-Host "  2. SQL Login 1   : sa         / Mat khau: 123456" -ForegroundColor White
Write-Host "  3. SQL Login 2   : aita_user  / Mat khau: 123456" -ForegroundColor White
Write-Host ""
Write-Host "  4. TAI KHOAN DANG NHAP WEB (Tat ca password: 123456):" -ForegroundColor White
Write-Host "     - Quan tri vien:   admin" -ForegroundColor Yellow
Write-Host "     - Giang vien:      teacher_ha" -ForegroundColor Yellow
Write-Host "     - Truong nhom:     kietnta (Nguyen Tran Anh Kiet)" -ForegroundColor Yellow
Write-Host "     - Sinh vien:       phuctv (Tran Van Phuc)" -ForegroundColor Yellow
Write-Host "     - Sinh vien:       khanhdvp (Dinh Vu Phuong Khanh)" -ForegroundColor Yellow
Write-Host "     - Sinh vien:       nhinh (Nguyen Hoai Nhi)" -ForegroundColor Yellow
Write-Host "     - Sinh vien:       tienn (Nguyen Tien)" -ForegroundColor Yellow
Write-Host "===============================================================================" -ForegroundColor Cyan
Write-Host ""
Write-Host "Hoan tat! Ban co the chay CHAY_ALL.bat de bat dau chay web." -ForegroundColor Green
Write-Host ""
Read-Host "Nhan phim Enter de dong cua so nay..."

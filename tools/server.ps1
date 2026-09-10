# ==============================================================================
# AITA CodeDefend - Native PowerShell Static Web Server
# Chay 100% doc lap, khong yeu cau cai dat Python, Node hay phan mem ben thu 3
# ==============================================================================
param(
    [int]$Port = 8089,
    [string]$WebRoot = ""
)

$Host.UI.RawUI.WindowTitle = "AITA CodeDefend - Local Web Server (Port $Port)"
[Console]::OutputEncoding = [System.Text.Encoding]::UTF8

if (-not $WebRoot -or -not (Test-Path $WebRoot)) {
    $WebRoot = Join-Path $PSScriptRoot "..\web"
}
$WebRoot = [System.IO.Path]::GetFullPath($WebRoot)

Write-Host "==========================================================" -ForegroundColor Cyan
Write-Host "   AITA CodeDefend - Native PowerShell HTTP Server        " -ForegroundColor Yellow
Write-Host "==========================================================" -ForegroundColor Cyan
Write-Host "[+] Thu muc goc : $WebRoot" -ForegroundColor Green
Write-Host "[+] Cong mang   : http://localhost:$Port" -ForegroundColor Green

# Do cong kha dung
$listener = New-Object System.Net.HttpListener
$prefix = "http://localhost:$Port/"
$listener.Prefixes.Add($prefix)

try {
    $listener.Start()
} catch {
    Write-Host "[!] Cong $Port dang ban, chuyen sang cong $( $Port + 1 )..." -ForegroundColor Yellow
    $Port = $Port + 1
    $listener = New-Object System.Net.HttpListener
    $prefix = "http://localhost:$Port/"
    $listener.Prefixes.Add($prefix)
    $listener.Start()
}

Write-Host "[OK] May chu san sang: http://localhost:$Port/preview/index.html" -ForegroundColor Cyan
Write-Host "[i] Nhan Ctrl+C de dung may chu.`n" -ForegroundColor DarkGray

# Tu dong mo trinh duyet
try {
    Start-Process "http://localhost:$Port/preview/index.html"
} catch {}

$mimeTypes = @{
    ".html" = "text/html; charset=utf-8"
    ".htm"  = "text/html; charset=utf-8"
    ".css"  = "text/css; charset=utf-8"
    ".js"   = "application/javascript; charset=utf-8"
    ".mjs"  = "application/javascript; charset=utf-8"
    ".json" = "application/json; charset=utf-8"
    ".png"  = "image/png"
    ".jpg"  = "image/jpeg"
    ".jpeg" = "image/jpeg"
    ".gif"  = "image/gif"
    ".webp" = "image/webp"
    ".svg"  = "image/svg+xml"
    ".ico"  = "image/x-icon"
    ".mp4"  = "video/mp4"
    ".webm" = "video/webm"
    ".ogg"  = "audio/ogg"
    ".mp3"  = "audio/mpeg"
    ".wav"  = "audio/wav"
    ".woff" = "font/woff"
    ".woff2"= "font/woff2"
    ".ttf"  = "font/ttf"
    ".pdf"  = "application/pdf"
}

try {
    while ($listener.IsListening) {
        $context = $listener.GetContext()
        $request = $context.Request
        $response = $context.Response

        $urlPath = $request.Url.LocalPath
        if ($urlPath -eq "/" -or $urlPath -eq "") {
            $urlPath = "/preview/index.html"
        }

        # Chuyen / thanh duong dan he thong
        $relPath = $urlPath.TrimStart('/').Replace('/', [System.IO.Path]::DirectorySeparatorChar)
        $filePath = Join-Path $WebRoot $relPath

        # Neu la thu muc, tim index.html
        if (Test-Path $filePath -PathType Container) {
            $filePath = Join-Path $filePath "index.html"
        }

        if (Test-Path $filePath -PathType Leaf) {
            $ext = [System.IO.Path]::GetExtension($filePath).ToLower()
            $mime = "application/octet-stream"
            if ($mimeTypes.ContainsKey($ext)) {
                $mime = $mimeTypes[$ext]
            }

            $response.ContentType = $mime
            $response.AddHeader("Access-Control-Allow-Origin", "*")
            $response.AddHeader("Cache-Control", "no-cache, must-revalidate")

            try {
                $fileBytes = [System.IO.File]::ReadAllBytes($filePath)
                $response.ContentLength64 = $fileBytes.Length
                $response.OutputStream.Write($fileBytes, 0, $fileBytes.Length)
            } catch {
                $response.StatusCode = 500
            }
        } else {
            $response.StatusCode = 404
            $notFoundBytes = [System.Text.Encoding]::UTF8.GetBytes("404 Not Found: $urlPath")
            $response.OutputStream.Write($notFoundBytes, 0, $notFoundBytes.Length)
        }

        $response.Close()
    }
} finally {
    $listener.Stop()
    $listener.Close()
}

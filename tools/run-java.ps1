param([string]$TomcatHome = $env:CATALINA_HOME, [switch]$Stop, [string]$EnvironmentFile = '.env')
$ErrorActionPreference = 'Stop'
$root = Split-Path $PSScriptRoot -Parent
Set-Location -LiteralPath $root
$runtime = Join-Path $root 'target/java-runtime'
$pidFile = Join-Path $runtime 'tomcat.pid'
$sha = [Security.Cryptography.SHA256]::Create()
$runtimeId = [BitConverter]::ToString($sha.ComputeHash([Text.Encoding]::UTF8.GetBytes($runtime.ToLowerInvariant()))).Replace('-', '')
$sha.Dispose()
if ($Stop) {
    if (Test-Path -LiteralPath $pidFile) {
        $serverPid = [int](Get-Content -LiteralPath $pidFile)
        $process = Get-CimInstance Win32_Process -Filter "ProcessId=$serverPid"
        if ($process) {
            if ($process.Name -ne 'java.exe' -or -not $process.CommandLine.Contains("-Daita.runtime.id=$runtimeId")) {
                throw 'PID identity does not match this runtime; refusing to stop an unrelated process.'
            }
            Stop-Process -Id $serverPid
        }
        Remove-Item -LiteralPath $pidFile
    }
    exit
}
if (Test-Path -LiteralPath '.env') {
    foreach ($line in Get-Content -Encoding UTF8 -LiteralPath '.env') {
        if ($line -match '^\s*(DB_[A-Z_]+|GOOGLE_CLIENT_ID|JWT_SECRET|CATALINA_HOME)\s*=(.*)$') {
            [Environment]::SetEnvironmentVariable($Matches[1], $Matches[2].Trim().Trim('"').Trim("'"), 'Process')
        }
    }
}
if ($EnvironmentFile -ne '.env') {
    $env:DB_URL = $null
    foreach ($line in Get-Content -Encoding UTF8 -LiteralPath $EnvironmentFile) {
        if ($line -match '^\s*(DB_[A-Z_]+|GOOGLE_CLIENT_ID|JWT_SECRET)\s*=(.*)$') {
            [Environment]::SetEnvironmentVariable($Matches[1], $Matches[2].Trim().Trim('"').Trim("'"), 'Process')
        }
    }
}
if (-not $TomcatHome) { $TomcatHome = $env:CATALINA_HOME }
if (-not $TomcatHome -or -not (Test-Path -LiteralPath (Join-Path $TomcatHome 'bin/bootstrap.jar'))) {
    throw 'Set CATALINA_HOME to Apache Tomcat 10.1 before starting Java.'
}
if (-not $env:JWT_SECRET -or [Text.Encoding]::UTF8.GetByteCount($env:JWT_SECRET) -lt 32) {
    throw 'Set JWT_SECRET to a random secret of at least 32 bytes in .env.'
}
if (-not $env:DB_USER -or -not $env:DB_PASSWORD) { throw 'Set DB_USER and DB_PASSWORD in .env.' }
if (Test-Path -LiteralPath $pidFile) {
    $existing = Get-Process -Id ([int](Get-Content $pidFile)) -ErrorAction SilentlyContinue
    if ($existing) { throw 'A task-owned Tomcat is already running. Use -Stop before rebuilding.' }
}
$env:MAVEN_OPTS = "$env:MAVEN_OPTS -Dfile.encoding=UTF-8"
& mvn -B package -DskipTests
if ($LASTEXITCODE -ne 0) { throw 'WAR build failed.' }
New-Item -ItemType Directory -Force -Path $runtime | Out-Null
foreach ($folder in 'conf','logs','temp','work','webapps') {
    New-Item -ItemType Directory -Force -Path (Join-Path $runtime $folder) | Out-Null
}
Copy-Item -Path (Join-Path $TomcatHome 'conf/*') -Destination (Join-Path $runtime 'conf') -Force
Copy-Item -LiteralPath 'target/aita-plagiarism-detection-1.0.0-SNAPSHOT.war' -Destination (Join-Path $runtime 'webapps/plagiarism.war') -Force
$java = (Get-Command java).Source
$TomcatHome = Resolve-Path -Relative -LiteralPath $TomcatHome
$runtimeArgs = 'target/java-runtime'
$arguments = @('-Dfile.encoding=UTF-8', "-Daita.runtime.id=$runtimeId", "-Dcatalina.home=`"$TomcatHome`"", "-Dcatalina.base=`"$runtimeArgs`"",
    "-Djava.io.tmpdir=`"$runtimeArgs/temp`"", '-cp',
    "`"$(Join-Path $TomcatHome 'bin/bootstrap.jar');$(Join-Path $TomcatHome 'bin/tomcat-juli.jar')`"",
    'org.apache.catalina.startup.Bootstrap', 'start')
$process = Start-Process -FilePath $java -ArgumentList $arguments -WorkingDirectory $root -WindowStyle Hidden -PassThru -RedirectStandardOutput (Join-Path $runtime 'stdout.log') -RedirectStandardError (Join-Path $runtime 'stderr.log')
[IO.File]::WriteAllText($pidFile, [string]$process.Id)
Start-Sleep -Seconds 2
if ($process.HasExited) { throw 'Tomcat exited during startup. See target/java-runtime/stderr.log.' }
Write-Host "Tomcat launched (PID $($process.Id)). Check http://localhost:8080/plagiarism/login and target/java-runtime/stderr.log."

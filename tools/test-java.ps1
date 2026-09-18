param([string]$Tests = '', [string]$EnvironmentFile = '.env.test')
$ErrorActionPreference = 'Stop'
$env:DB_URL = $null
Set-Location -LiteralPath (Split-Path $PSScriptRoot -Parent)
if (-not (Test-Path -LiteralPath $EnvironmentFile)) { throw 'Create an environment file with an isolated test database. Never run these integration tests against application data.' }
foreach ($line in Get-Content -Encoding UTF8 -LiteralPath $EnvironmentFile) {
    if ($line -match '^\s*(DB_[A-Z_]+|JWT_SECRET)\s*=(.*)$') {
        [Environment]::SetEnvironmentVariable($Matches[1], $Matches[2].Trim().Trim('"').Trim("'"), 'Process')
    }
}
if ($env:DB_NAME -notmatch '(?i)(test|verification)' -or $env:DB_NAME -eq 'AITA_PlagiarismDB') {
    throw 'DB_NAME must identify an isolated test or verification database.'
}
if ($env:DB_URL) { throw 'DB_URL is not allowed for tests; use DB_SERVER, DB_PORT and DB_NAME.' }
$env:MAVEN_OPTS = "$env:MAVEN_OPTS -Dfile.encoding=UTF-8"
$argsList = @('-B', 'test')
if ($Tests) { $argsList += "-Dtest=$Tests" }
& mvn @argsList
exit $LASTEXITCODE

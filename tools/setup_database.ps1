param([string]$Server = $env:DB_SERVER)
$ErrorActionPreference = 'Stop'
if (-not $Server) { throw 'Set DB_SERVER to the intended SQL Server instance before running setup.' }
$schema = Join-Path (Split-Path $PSScriptRoot -Parent) 'database/database_schema.sql'
if (-not (Get-Command sqlcmd -ErrorAction SilentlyContinue)) { throw 'sqlcmd is required.' }
& sqlcmd -S $Server -E -b -f 65001 -i $schema
if ($LASTEXITCODE -ne 0) { throw 'Database setup failed. Existing data has not been reset.' }
Write-Host 'Schema is ready. Configure a database-scoped application account separately.'

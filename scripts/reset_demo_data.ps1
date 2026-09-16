# =============================================================================
# SCRIPT RESET DỮ LIỆU DEMO FLASH SALE (POWERSHELL WRAPPER)
# =============================================================================
Write-Host "==========================================================" -ForegroundColor Cyan
Write-Host " Dang khoi phuc ton kho va du lieu Demo..." -ForegroundColor Green
Write-Host "==========================================================" -ForegroundColor Cyan

$scriptDir = Split-Path -Parent $MyInvocation.MyCommand.Path
$mjsScript = Join-Path $scriptDir "reset_demo_data.mjs"

node "$mjsScript"

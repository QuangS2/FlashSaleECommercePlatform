param(
    [string]$TargetUrl = "http://localhost:8080",
    [ValidateSet("jmeter", "k6", "runner")]
    [string]$Engine = "jmeter",
    [int]$Threads = 500,
    [int]$Duration = 20,
    [int]$Requests = 5000,
    [int]$Concurrency = 40
)

Write-Host "==========================================================" -ForegroundColor Cyan
Write-Host " Flash Sale Distributed Load Testing Harness" -ForegroundColor Green
Write-Host " Target URL:   $TargetUrl" -ForegroundColor Yellow
Write-Host " Engine:       $Engine" -ForegroundColor Yellow
Write-Host "==========================================================" -ForegroundColor Cyan

$scriptDir = Split-Path -Parent $MyInvocation.MyCommand.Path

if ($Engine -eq "jmeter") {
    $jmeterPlan = Join-Path $scriptDir "flashsale_10k_users.jmx"
    $localJmeterBat = Join-Path $scriptDir "apache-jmeter-5.6.3\bin\jmeter.bat"
    $jmeterCmd = if (Test-Path $localJmeterBat) { $localJmeterBat } elseif (Get-Command jmeter -ErrorAction SilentlyContinue) { "jmeter" } else { $null }

    if ($jmeterCmd) {
        Write-Host "[OK] Khoi chay Apache JMeter 5.6.3 (Tieu chuan Bao cao Do an)..." -ForegroundColor Green
        $resultsFile = Join-Path $scriptDir "jmeter_results.jtl"
        $reportDir = Join-Path $scriptDir "jmeter_html_report"
        if (Test-Path $resultsFile) { Remove-Item $resultsFile -Force }
        if (Test-Path $reportDir) { Remove-Item $reportDir -Recurse -Force }

        Write-Host "Kich ban Test Plan: $jmeterPlan" -ForegroundColor Cyan
        Write-Host "So luong Threads:   $Threads | Thoi gian chay: ${Duration}s" -ForegroundColor Cyan
        & "$jmeterCmd" -n -t "$jmeterPlan" -Jhost="localhost" -Jport="8080" -Jthreads="$Threads" -Jduration="$Duration" -l "$resultsFile" -e -o "$reportDir"
        Write-Host "[DONE] Hoan tat kiem thu tai bang Apache JMeter!" -ForegroundColor Green
        Write-Host "[REPORT] Bao cao HTML truc quan da luu tai: $reportDir\index.html" -ForegroundColor Yellow
    } else {
        Write-Host "[INFO] JMeter chua san sang tren may. Dang chuyen tiep tu dong sang High-Concurrency Runner..." -ForegroundColor Yellow
        $runnerScript = Join-Path $scriptDir "high_load_runner.mjs"
        node "$runnerScript" --target "$TargetUrl" --requests $Requests --concurrency $Concurrency
    }
} elseif ($Engine -eq "k6") {
    $k6Script = Join-Path $scriptDir "flashsale_10k_vu.js"
    if (Get-Command k6 -ErrorAction SilentlyContinue) {
        Write-Host "[OK] Khoi chay k6 Load Testing..." -ForegroundColor Green
        k6 run -e BASE_URL=$TargetUrl $k6Script
    } else {
        Write-Host "[WARN] Khong tim thay k6 trong PATH. Chay fallback bang High-Concurrency Runner..." -ForegroundColor Yellow
        $runnerScript = Join-Path $scriptDir "high_load_runner.mjs"
        node "$runnerScript" --target "$TargetUrl" --requests $Requests --concurrency $Concurrency
    }
} else {
    Write-Host "[INFO] Kich hoat High-Concurrency Load Runner (Zero-Dependency Node.js)..." -ForegroundColor Yellow
    $runnerScript = Join-Path $scriptDir "high_load_runner.mjs"
    node "$runnerScript" --target "$TargetUrl" --requests $Requests --concurrency $Concurrency
}

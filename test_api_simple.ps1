# Create a persistent session
$session = New-Object Microsoft.PowerShell.Commands.WebRequestSession

# Step 1: Login
Write-Host "1. Logging in..." -ForegroundColor Cyan
$loginResp = Invoke-WebRequest -Uri "http://localhost:8080/login" -WebSession $session -Method Post -ContentType "application/x-www-form-urlencoded" -Body "email=admin@clinic.com&password=Nextlevel@123" -MaximumRedirection 0 -UseBasicParsing -ErrorAction SilentlyContinue

Write-Host "Login Status: $($loginResp.StatusCode)" -ForegroundColor Green
Write-Host "Session Cookies: $($session.Cookies.Count)" -ForegroundColor Yellow
$session.Cookies | ForEach-Object { Write-Host "  Cookie: $($_.Name) = $($_.Value)" -ForegroundColor Gray }

# Step 2: API Call with same session
Start-Sleep -Seconds 2
Write-Host "`n2. Calling API with same session..." -ForegroundColor Cyan
$apiResp = Invoke-WebRequest -Uri "http://localhost:8080/api/admin/patients" -WebSession $session -UseBasicParsing

Write-Host "API Status: $($apiResp.StatusCode)" -ForegroundColor Green
Write-Host "API Response: $($apiResp.Content)" -ForegroundColor Yellow

# Parse and pretty-print
$data = $apiResp.Content | ConvertFrom-Json
Write-Host "`nParsed Response:"
Write-Host "  Success: $($data.success)"
Write-Host "  Data: $($data.data)"
Write-Host "  Error: $($data.error)"

if ($data.success) {
    Write-Host "`n✅ SUCCESS! Got $(($data.data | Measure-Object).Count) patients" -ForegroundColor Green
    $data.data | ConvertTo-Json -Depth 10
} else {
    Write-Host "`n❌ FAILED: $($data.error)" -ForegroundColor Red
}


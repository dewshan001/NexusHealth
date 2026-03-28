# Test the debug endpoint
$session = New-Object Microsoft.PowerShell.Commands.WebRequestSession

# Step 1: Login
Write-Host "1. Logging in..." -ForegroundColor Cyan
$loginResp = Invoke-WebRequest -Uri "http://localhost:8080/login" -WebSession $session -Method Post -ContentType "application/x-www-form-urlencoded" -Body "email=admin@clinic.com&password=Nextlevel@123" -MaximumRedirection 0 -UseBasicParsing -ErrorAction SilentlyContinue

Write-Host "Login Status: $($loginResp.StatusCode)" -ForegroundColor Green

# Step 2: Call debug endpoint
Start-Sleep -Seconds 2
Write-Host "`n2. Calling /api/debug/session..." -ForegroundColor Cyan
$debugResp = Invoke-WebRequest -Uri "http://localhost:8080/api/debug/session" -WebSession $session -UseBasicParsing

Write-Host "Debug Status: $($debugResp.StatusCode)" -ForegroundColor Green
Write-Host "Debug Response:" -ForegroundColor Yellow
$debugResp.Content | ConvertFrom-Json | ConvertTo-Json -Depth 10


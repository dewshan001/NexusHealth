#!/usr/bin/env pwsh

# Test the complete API flow
Write-Host "=== Testing Clinic API Flow ===" -ForegroundColor Cyan

# Create a session object to maintain cookies
$session = New-Object Microsoft.PowerShell.Commands.WebRequestSession

# Step 1: Login
Write-Host "`n1. Attempting login with admin@clinic.com..." -ForegroundColor Yellow
$loginResponse = Invoke-WebRequest `
    http://localhost:8080/login `
    -WebSession $session `
    -Method Post `
    -ContentType "application/x-www-form-urlencoded" `
    -Body "email=admin@clinic.com&password=Nextlevel@123" `
    -UseBasicParsing `
    -MaximumRedirection 0 `
    -ErrorAction SilentlyContinue

if ($loginResponse.StatusCode -eq 302) {
    Write-Host "✅ Login successful (302 redirect)" -ForegroundColor Green
    Write-Host "Session ID: $($session.Cookies | Where-Object { $_.Name -eq 'JSESSIONID' })" -ForegroundColor Gray
} else {
    Write-Host "❌ Login failed with status code: $($loginResponse.StatusCode)" -ForegroundColor Red
    exit 1
}

# Step 2: Test API with authenticated session
Write-Host "`n2. Calling /api/admin/patients..." -ForegroundColor Yellow
$apiResponse = Invoke-WebRequest `
    http://localhost:8080/api/admin/patients `
    -WebSession $session `
    -UseBasicParsing

Write-Host "Response Status: $($apiResponse.StatusCode)" -ForegroundColor Green
Write-Host "Response Content:" -ForegroundColor Yellow
$apiResponse.Content | ConvertFrom-Json | ConvertTo-Json -Depth 10

# Step 3: Test patient info endpoint
Write-Host "`n3. Calling /api/patient-info..." -ForegroundColor Yellow
$patientResponse = Invoke-WebRequest `
    http://localhost:8080/api/patient-info `
    -WebSession $session `
    -UseBasicParsing

Write-Host "Response Status: $($patientResponse.StatusCode)" -ForegroundColor Green
Write-Host "Response Content:" -ForegroundColor Yellow
$patientResponse.Content | ConvertFrom-Json | ConvertTo-Json -Depth 10

Write-Host "`n=== Test Complete ===" -ForegroundColor Cyan


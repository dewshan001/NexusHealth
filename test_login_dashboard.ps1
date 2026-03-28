# Test Script for Session Redirect Loop Fix

Write-Host ""
Write-Host "============================================"
Write-Host "Dashboard Redirect Loop Test"
Write-Host "============================================"
Write-Host ""

$baseUrl = "http://localhost:8080"

# Step 1: Test signup
Write-Host "[1/4] Testing signup..." -ForegroundColor Cyan

$signupParams = @{
    fullName = 'TestUser'
    email = 'testuser@example.com'
    password = 'password123'
    phone = '1234567890'
    dateOfBirth = '1990-01-01'
    gender = 'Male'
    bloodType = 'O+'
    address = '123 Main St'
}

try {
    $signupBody = ($signupParams.GetEnumerator() | ForEach-Object { "$($_.Key)=$([System.Web.HttpUtility]::UrlEncode($_.Value))" }) -join '&'
    $signupResponse = Invoke-WebRequest -Uri "$baseUrl/signup" -Method Post -Body $signupBody -ContentType "application/x-www-form-urlencoded" -UseBasicParsing -SessionVariable session
    Write-Host "Signup request sent (Status: $($signupResponse.StatusCode))" -ForegroundColor Green
} catch {
    Write-Host "Signup response: $($_.Exception.Message)" -ForegroundColor Yellow
}

# Step 2: Test login
Write-Host ""
Write-Host "[2/4] Testing login..." -ForegroundColor Cyan

$loginParams = @{
    email = 'testuser@example.com'
    password = 'password123'
}

try {
    $loginBody = ($loginParams.GetEnumerator() | ForEach-Object { "$($_.Key)=$([System.Web.HttpUtility]::UrlEncode($_.Value))" }) -join '&'
    $loginResponse = Invoke-WebRequest -Uri "$baseUrl/login" -Method Post -Body $loginBody -ContentType "application/x-www-form-urlencoded" -UseBasicParsing -WebSession $session -AllowRedirect:$false

    Write-Host "Login request sent (Status: $($loginResponse.StatusCode))" -ForegroundColor Green

    Write-Host "Cookies received:" -ForegroundColor Gray
    if ($session.Cookies.Count -gt 0) {
        $session.Cookies | ForEach-Object {
            Write-Host "  - $($_.Name) = $($_.Value)" -ForegroundColor Gray
        }
    } else {
        Write-Host "  (No cookies found)" -ForegroundColor Yellow
    }
} catch {
    Write-Host "Login response: $($_.Exception.Message)" -ForegroundColor Yellow
}

# Step 3: Access dashboard with session
Write-Host ""
Write-Host "[3/4] Testing dashboard access with session..." -ForegroundColor Cyan

try {
    $dashboardResponse = Invoke-WebRequest -Uri "$baseUrl/patient-dashboard" -UseBasicParsing -WebSession $session -AllowRedirect:$false
    Write-Host "Dashboard request sent (Status: $($dashboardResponse.StatusCode))" -ForegroundColor Green

    if ($dashboardResponse.StatusCode -eq 200) {
        Write-Host "Dashboard loaded successfully!" -ForegroundColor Green
    } else {
        Write-Host "Dashboard Status Code: $($dashboardResponse.StatusCode)" -ForegroundColor Yellow
    }
} catch {
    Write-Host "Dashboard error: $($_.Exception.Message)" -ForegroundColor Yellow
}

# Step 4: Summary
Write-Host ""
Write-Host "[4/4] Test Complete" -ForegroundColor Cyan
Write-Host "Check console logs for session tracking" -ForegroundColor Green
Write-Host ""
Write-Host "============================================"
Write-Host ""


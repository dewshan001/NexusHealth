@echo off
REM Test Script for Session Redirect Loop Fix
REM This script tests the login and dashboard flow

echo.
echo ============================================
echo Dashboard Redirect Loop Test
echo ============================================
echo.

setlocal enabledelayedexpansion

REM Step 1: Test signup
echo [1/4] Testing signup...
powershell -NoProfile -Command ^
"$params = @{ ^
    'fullName'='John Doe'; ^
    'email'='john@example.com'; ^
    'password'='password123'; ^
    'phone'='1234567890'; ^
    'dateOfBirth'='1990-01-01'; ^
    'gender'='Male'; ^
    'bloodType'='O+'; ^
    'address'='123 Main St' ^
}; ^
$body = [System.Web.HttpUtility]::ParseQueryString([String]::Empty) ^
foreach($key in $params.Keys) { $body.Add($key, $params[$key]) } ^
Invoke-WebRequest -Uri 'http://localhost:8080/signup' -Method Post -Body $body.ToString() -UseBasicParsing -SessionVariable web | Out-Null"

if %ERRORLEVEL% EQU 0 (
    echo ✓ Signup successful
) else (
    echo ✗ Signup failed
)

REM Step 2: Test login
echo.
echo [2/4] Testing login...
powershell -NoProfile -Command ^
"$params = @{ ^
    'email'='john@example.com'; ^
    'password'='password123' ^
}; ^
$body = [System.Web.HttpUtility]::ParseQueryString([String]::Empty) ^
foreach($key in $params.Keys) { $body.Add($key, $params[$key]) } ^
$response = Invoke-WebRequest -Uri 'http://localhost:8080/login' -Method Post -Body $body.ToString() -UseBasicParsing -SessionVariable web -AllowRedirect:$false ^
Write-Host 'Login Response Status:' $response.StatusCode"

if %ERRORLEVEL% EQU 0 (
    echo ✓ Login request executed
) else (
    echo ✗ Login failed
)

REM Step 3: Check for cookies
echo.
echo [3/4] Checking session cookies...
powershell -NoProfile -Command ^
"$response = Invoke-WebRequest -Uri 'http://localhost:8080/patient-dashboard' -UseBasicParsing ^
Write-Host 'Dashboard Status:' $response.StatusCode"

REM Step 4: Summary
echo.
echo [4/4] Test Summary
echo Check the application logs above for session details
echo.
echo ============================================


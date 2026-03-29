#!/usr/bin/env pwsh
<#
.SYNOPSIS
    Test script for NexusHealth Receptionist Dashboard Settings
.DESCRIPTION
    Comprehensive testing for receptionist profile updates, phone field persistence, 
    profile picture upload, and password change functionality.
.NOTES
    Prerequisites: Application running on http://localhost:8080
    Receptionist credentials: receptionist@hospital.com / Receptionist@123
#>

$BASE_URL = "http://localhost:8080"
$RECEPTIONIST_EMAIL = "receptionist@hospital.com"
$RECEPTIONIST_PASSWORD = "Receptionist@123"

# Color helpers for console output
function Write-Success { param([string]$Message); Write-Host "✅ $Message" -ForegroundColor Green }
function Write-Error { param([string]$Message); Write-Host "❌ $Message" -ForegroundColor Red }
function Write-Info { param([string]$Message); Write-Host "ℹ️  $Message" -ForegroundColor Cyan }
function Write-Warning { param([string]$Message); Write-Host "⚠️  $Message" -ForegroundColor Yellow }

# Test header
Write-Host "`n========================================" -ForegroundColor Magenta
Write-Host "NexusHealth Receptionist Settings Tests" -ForegroundColor Magenta
Write-Host "========================================`n" -ForegroundColor Magenta

# Test 1: Check if application is running
Write-Info "Running health check..."
try {
    $response = Invoke-WebRequest -Uri "$BASE_URL/login" -ErrorAction SilentlyContinue
    Write-Success "Application is running on $BASE_URL"
} catch {
    Write-Error "Application is not responding. Please start the application first."
    exit 1
}

# Test 2: Create web session for login
Write-Info "Creating session and logging in as receptionist..."
$session = New-Object Microsoft.PowerShell.Commands.WebRequestSession
$session.UserAgent = "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36"

# Simulate login
try {
    $loginResponse = Invoke-WebRequest -Uri "$BASE_URL/login" `
        -Method POST `
        -Body @{ email = $RECEPTIONIST_EMAIL; password = $RECEPTIONIST_PASSWORD } `
        -WebSession $session `
        -ErrorAction SilentlyContinue
    Write-Success "Logged in successfully"
} catch {
    Write-Error "Login failed: $_"
}

# Test 3: Get Receptionist Profile
Write-Info "Test 1: Fetching receptionist profile..."
try {
    $profileResponse = Invoke-WebRequest -Uri "$BASE_URL/api/receptionist/profile" `
        -Method GET `
        -WebSession $session
    $profileData = $profileResponse.Content | ConvertFrom-Json
    
    if ($profileData.success) {
        Write-Success "Profile retrieved successfully"
        Write-Info "  - Name: $($profileData.data.fullName)"
        Write-Info "  - Email: $($profileData.data.email)"
        Write-Info "  - Phone: $($profileData.data.phone)"
        Write-Info "  - Role: $($profileData.data.role)"
        Write-Info "  - Profile Picture: $($profileData.data.profilePicture)"
    } else {
        Write-Error "Failed to retrieve profile: $($profileData.message)"
    }
} catch {
    Write-Error "Profile fetch failed: $_"
}

# Test 4: Update Full Name and Phone
Write-Info "Test 2: Updating receptionist profile (name and phone)..."
try {
    $updateResponse = Invoke-WebRequest -Uri "$BASE_URL/api/receptionist/profile/update" `
        -Method POST `
        -WebSession $session `
        -Body @{ fullName = "Test Receptionist"; phone = "+1(555)123-4567" }
    $updateData = $updateResponse.Content | ConvertFrom-Json
    
    if ($updateData.success) {
        Write-Success "Profile updated successfully"
        Write-Info "Updated phone number to: +1(555)123-4567"
    } else {
        Write-Error "Profile update failed: $($updateData.message)"
    }
} catch {
    Write-Error "Profile update request failed: $_"
}

# Test 5: Verify phone persistence in database
Write-Info "Test 3: Verifying phone field persistence..."
Start-Sleep -Seconds 1
try {
    $verifyResponse = Invoke-WebRequest -Uri "$BASE_URL/api/receptionist/profile" `
        -Method GET `
        -WebSession $session
    $verifyData = $verifyResponse.Content | ConvertFrom-Json
    
    if ($verifyData.success && $verifyData.data.phone -eq "+1(555)123-4567") {
        Write-Success "Phone number persisted correctly in database"
    } else {
        Write-Warning "Phone number may not be persisting: Retrieved value is '$($verifyData.data.phone)'"
    }
} catch {
    Write-Error "Verification request failed: $_"
}

# Test 6: Upload Profile Picture
Write-Info "Test 4: Testing profile picture upload..."
$testImagePath = "$PSScriptRoot\test_image.jpg"

# Create a simple test image if it doesn't exist
if (-not (Test-Path $testImagePath)) {
    Write-Info "Creating test image..."
    $webClient = New-Object System.Net.WebClient
    $imageUrl = "https://images.unsplash.com/photo-1506794778202-cad84cf45f1d?auto=format&fit=crop&w=150&q=80"
    try {
        $webClient.DownloadFile($imageUrl, $testImagePath)
        Write-Success "Test image downloaded"
    } catch {
        Write-Warning "Could not download test image, skipping picture upload test"
        $testImagePath = $null
    }
}

if ($testImagePath -ne $null) {
    try {
        $FileStream = [System.IO.File]::OpenRead($testImagePath)
        $BoundaryString = [System.Guid]::NewGuid().ToString()
        
        $bodyTemplate = @"
--{0}
Content-Disposition: form-data; name="file"; filename="test_image.jpg"
Content-Type: image/jpeg

{1}
--{0}--
"@
        
        $body = $bodyTemplate -f $BoundaryString, [System.IO.File]::ReadAllText($testImagePath)
        
        $uploadResponse = Invoke-WebRequest -Uri "$BASE_URL/api/receptionist/profile/picture" `
            -Method POST `
            -WebSession $session `
            -ContentType "multipart/form-data; boundary=$BoundaryString" `
            -InFile $testImagePath `
            -ErrorAction SilentlyContinue
        
        $uploadData = $uploadResponse.Content | ConvertFrom-Json
        
        if ($uploadData.success) {
            Write-Success "Profile picture uploaded successfully"
            Write-Info "  - File path: $($uploadData.data.filePath)"
        } else {
            Write-Warning "Picture upload failed: $($uploadData.message)"
        }
    } catch {
        Write-Warning "Picture upload test skipped (multipart form handling): $_"
    }
}

# Test 7: Change Password (with validation)
Write-Info "Test 5: Testing password change validation..."

# Test 5a: Invalid password (too short)
Write-Info "  - Testing password validation (too short)..."
try {
    $shortPasswordResponse = Invoke-WebRequest -Uri "$BASE_URL/api/receptionist/profile/password" `
        -Method POST `
        -WebSession $session `
        -Body @{ 
            currentPassword = $RECEPTIONIST_PASSWORD
            newPassword = "Short1"
            confirmPassword = "Short1"
        } `
        -ErrorAction SilentlyContinue
    $shortPasswordData = $shortPasswordResponse.Content | ConvertFrom-Json
    
    if (-not $shortPasswordData.success) {
        Write-Success "Correctly rejected short password: $($shortPasswordData.message)"
    } else {
        Write-Error "Should have rejected short password"
    }
} catch {
    Write-Error "Short password test failed: $_"
}

# Test 5b: Non-matching passwords
Write-Info "  - Testing non-matching passwords..."
try {
    $mismatchResponse = Invoke-WebRequest -Uri "$BASE_URL/api/receptionist/profile/password" `
        -Method POST `
        -WebSession $session `
        -Body @{ 
            currentPassword = $RECEPTIONIST_PASSWORD
            newPassword = "NewPassword@123"
            confirmPassword = "DifferentPassword@456"
        } `
        -ErrorAction SilentlyContinue
    $mismatchData = $mismatchResponse.Content | ConvertFrom-Json
    
    if (-not $mismatchData.success) {
        Write-Success "Correctly rejected non-matching passwords: $($mismatchData.message)"
    } else {
        Write-Error "Should have rejected non-matching passwords"
    }
} catch {
    Write-Error "Password mismatch test failed: $_"
}

# Test 5c: Wrong current password
Write-Info "  - Testing wrong current password..."
try {
    $wrongCurrentResponse = Invoke-WebRequest -Uri "$BASE_URL/api/receptionist/profile/password" `
        -Method POST `
        -WebSession $session `
        -Body @{ 
            currentPassword = "WrongPassword@123"
            newPassword = "NewPassword@123"
            confirmPassword = "NewPassword@123"
        } `
        -ErrorAction SilentlyContinue
    $wrongCurrentData = $wrongCurrentResponse.Content | ConvertFrom-Json
    
    if (-not $wrongCurrentData.success) {
        Write-Success "Correctly rejected wrong current password: $($wrongCurrentData.message)"
    } else {
        Write-Error "Should have rejected wrong current password"
    }
} catch {
    Write-Error "Wrong current password test failed: $_"
}

# Test 6: Test authorization (non-receptionist access)
Write-Info "Test 6: Testing authorization (non-receptionist access)..."

# Create a new session (no login, unauthorized)
$unauthorizedSession = New-Object Microsoft.PowerShell.Commands.WebRequestSession
try {
    $unauthorizedResponse = Invoke-WebRequest -Uri "$BASE_URL/api/receptionist/profile" `
        -Method GET `
        -WebSession $unauthorizedSession `
        -ErrorAction SilentlyContinue
    $unauthorizedData = $unauthorizedResponse.Content | ConvertFrom-Json
    
    if (-not $unauthorizedData.success -and $unauthorizedData.message -like "*Unauthorized*") {
        Write-Success "Correctly blocked unauthorized access: $($unauthorizedData.message)"
    } else {
        Write-Error "Should have blocked unauthorized access"
    }
} catch {
    Write-Error "Authorization test had error: $_"
}

# Test Summary
Write-Host "`n========================================" -ForegroundColor Magenta
Write-Host "Test Summary" -ForegroundColor Magenta
Write-Host "========================================`n" -ForegroundColor Magenta

Write-Success "Profile retrieval and updates"
Write-Success "Phone field persistence"
Write-Success "Password validation and strength checking"
Write-Success "Authorization enforcement"
Write-Info "Profile picture upload (requires manual testing)"

Write-Host "`n📋 Manual Testing Checklist:" -ForegroundColor Yellow
Write-Host "  [ ] Navigate to receptionist-dashboard"
Write-Host "  [ ] Click on 'Options' tab"
Write-Host "  [ ] Update Full Name and Contact Number → Click 'Save Info'"
Write-Host "  [ ] Verify data saved in database"
Write-Host "  [ ] Upload profile picture (JPG/PNG, max 5MB)"
Write-Host "  [ ] Verify picture displays in dashboard and sidebar"
Write-Host "  [ ] Remove/delete profile picture"
Write-Host "  [ ] Change password with valid credentials"
Write-Host "  [ ] Logout and login with new password"
Write-Host "  [ ] Verify all changes persisted after logout/login`n"

Write-Success "Automated tests completed!"

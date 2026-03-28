
$baseUrl = "http://localhost:8080"
$loginResp = Invoke-WebRequest -Uri "$baseUrl/login" -Method Post -Body "email=admin@clinic.com&password=Admin%40123" -ContentType "application/x-www-form-urlencoded" -SessionVariable session -UseBasicParsing -MaximumRedirection 0 -ErrorAction Ignore
Write-Host "Login Code:" $loginResp.StatusCode
$staffResp = Invoke-WebRequest -Uri "$baseUrl/api/admin/staff" -Method Get -WebSession $session -UseBasicParsing
Write-Host "Staff info:" $staffResp.Content
$delResp = Invoke-WebRequest -Uri "$baseUrl/api/admin/staff/delete" -Method Post -Body "userId=4" -ContentType "application/x-www-form-urlencoded" -WebSession $session -UseBasicParsing
Write-Host "Delete response:" $delResp.Content


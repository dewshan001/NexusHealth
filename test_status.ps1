
$baseUrl = "http://localhost:8080"
Invoke-WebRequest -Uri "$baseUrl/login" -Method Post -Body "email=admin@clinic.com&password=Admin%40123" -ContentType "application/x-www-form-urlencoded" -SessionVariable session -UseBasicParsing -MaximumRedirection 0 -ErrorAction Ignore
# Create a dummy staff
$docBody = "fullName=TestStatusDoc&email=statusdoc@test.com&password=docpass&role=pharmacist"
Invoke-WebRequest -Uri "$baseUrl/api/admin/users/create" -Method Post -Body $docBody -ContentType "application/x-www-form-urlencoded" -WebSession $session -UseBasicParsing
# Find ID
$staffResp = Invoke-WebRequest -Uri "$baseUrl/api/admin/staff" -Method Get -WebSession $session -UseBasicParsing
$json = $staffResp.Content | ConvertFrom-Json
$docId = ($json.data | Where-Object { $_.email -eq "statusdoc@test.com" }).id
Write-Host "Created Doc ID:" $docId
# Change status
if ($docId) {
    $statResp = Invoke-WebRequest -Uri "$baseUrl/api/admin/staff/status" -Method Post -Body "userId=$docId&status=deactivated" -ContentType "application/x-www-form-urlencoded" -WebSession $session -UseBasicParsing
    Write-Host "Update Status:" $statResp.Content
    # Clean up
    Invoke-WebRequest -Uri "$baseUrl/api/admin/staff/delete" -Method Post -Body "userId=$docId" -ContentType "application/x-www-form-urlencoded" -WebSession $session -UseBasicParsing
}


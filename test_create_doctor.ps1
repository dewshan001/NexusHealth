
$baseUrl = "http://localhost:8080"
Invoke-WebRequest -Uri "$baseUrl/login" -Method Post -Body "email=admin@clinic.com&password=Admin%40123" -ContentType "application/x-www-form-urlencoded" -SessionVariable session -UseBasicParsing -MaximumRedirection 0 -ErrorAction Ignore
$docBody = "fullName=TestDoctor&email=doc@test.com&password=docpass&licenseNumber=MED-123&assignedRoom=R10&consultationDurationMin=30&workingHoursStart=09:00&workingHoursEnd=17:00&yearsExperience=5"
$createResp = Invoke-WebRequest -Uri "$baseUrl/api/admin/doctors/create" -Method Post -Body $docBody -ContentType "application/x-www-form-urlencoded" -WebSession $session -UseBasicParsing
Write-Host "Create Doc:" $createResp.Content
$staffResp = Invoke-WebRequest -Uri "$baseUrl/api/admin/staff" -Method Get -WebSession $session -UseBasicParsing
Write-Host "Staff info:" $staffResp.Content
# Find the id of TestDoctor
$json = $staffResp.Content | ConvertFrom-Json
$docId = ($json.data | Where-Object { $_.email -eq "doc@test.com" }).id
Write-Host "Doc ID to delete:" $docId
if ($docId) {
    $delResp = Invoke-WebRequest -Uri "$baseUrl/api/admin/staff/delete" -Method Post -Body "userId=$docId" -ContentType "application/x-www-form-urlencoded" -WebSession $session -UseBasicParsing
    Write-Host "Delete Doc:" $delResp.Content
}


# Test Patient Endpoints with saved token

Write-Host "========== TESTING PATIENT ENDPOINTS ==========" -ForegroundColor Cyan

# Load token
$token = Get-Content -Path "patient-token.txt" -Raw
$token = $token.Trim()
Write-Host "Token loaded from file" -ForegroundColor Gray

$headers = @{
    "Authorization" = "Bearer $token"
    "Content-Type" = "application/json"
}

# Test 1: Get patient profile
Write-Host "`n1. GET /patients/profile/me" -ForegroundColor Yellow
try {
    $profile = Invoke-RestMethod -Uri "http://localhost:8086/api/patients/profile/me" -Method Get -Headers $headers
    Write-Host "SUCCESS: Profile retrieved!" -ForegroundColor Green
    Write-Host "  Name: $($profile.firstName) $($profile.lastName)" -ForegroundColor Gray
    Write-Host "  Email: $($profile.email)" -ForegroundColor Gray
    Write-Host "  Phone: $($profile.phoneNumber)" -ForegroundColor Gray
} catch {
    Write-Host "FAILED: $($_.Exception.Message)" -ForegroundColor Red
}

# Test 2: Update patient profile
Write-Host "`n2. PUT /patients/profile/me" -ForegroundColor Yellow
$updateBody = @{
    dateOfBirth = "1992-03-20"
    gender = "FEMALE"
    bloodType = "O+"
    allergies = "Penicillin"
    medicalHistory = "No major illnesses"
    emergencyContact = "Bob Smith"
    emergencyPhone = "+9876543210"
    address = "456 Oak Avenue"
    city = "Los Angeles"
    country = "USA"
    postalCode = "90001"
} | ConvertTo-Json

try {
    $updated = Invoke-RestMethod -Uri "http://localhost:8086/api/patients/profile/me" -Method Put -Headers $headers -Body $updateBody
    Write-Host "SUCCESS: Profile updated!" -ForegroundColor Green
    Write-Host "  Blood Type: $($updated.bloodType)" -ForegroundColor Gray
    Write-Host "  City: $($updated.city)" -ForegroundColor Gray
    Write-Host "  Allergies: $($updated.allergies)" -ForegroundColor Gray
} catch {
    Write-Host "FAILED: $($_.Exception.Message)" -ForegroundColor Red
}

# Test 3: Get appointments
Write-Host "`n3. GET /patients/appointments/me" -ForegroundColor Yellow
try {
    $appointments = Invoke-RestMethod -Uri "http://localhost:8086/api/patients/appointments/me" -Method Get -Headers $headers
    Write-Host "SUCCESS: Retrieved $($appointments.Count) appointments" -ForegroundColor Green
} catch {
    Write-Host "FAILED: $($_.Exception.Message)" -ForegroundColor Red
}

# Test 4: Get prescriptions
Write-Host "`n4. GET /patients/prescriptions/me" -ForegroundColor Yellow
try {
    $prescriptions = Invoke-RestMethod -Uri "http://localhost:8086/api/patients/prescriptions/me" -Method Get -Headers $headers
    Write-Host "SUCCESS: Retrieved $($prescriptions.Count) prescriptions" -ForegroundColor Green
} catch {
    Write-Host "FAILED: $($_.Exception.Message)" -ForegroundColor Red
}

# Test 5: Get medical records
Write-Host "`n5. GET /patients/medical-records/me" -ForegroundColor Yellow
try {
    $records = Invoke-RestMethod -Uri "http://localhost:8086/api/patients/medical-records/me" -Method Get -Headers $headers
    Write-Host "SUCCESS: Retrieved $($records.Count) medical records" -ForegroundColor Green
} catch {
    Write-Host "FAILED: $($_.Exception.Message)" -ForegroundColor Red
}

# Test 6: Get active prescriptions
Write-Host "`n6. GET /patients/prescriptions/me/active" -ForegroundColor Yellow
try {
    $active = Invoke-RestMethod -Uri "http://localhost:8086/api/patients/prescriptions/me/active" -Method Get -Headers $headers
    Write-Host "SUCCESS: Retrieved $($active.Count) active prescriptions" -ForegroundColor Green
} catch {
    Write-Host "FAILED: $($_.Exception.Message)" -ForegroundColor Red
}

Write-Host "`n========== ALL TESTS COMPLETE ==========" -ForegroundColor Cyan

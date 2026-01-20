# Test Patient Endpoints

Write-Host "========== TESTING PATIENT MANAGEMENT ENDPOINTS ==========" -ForegroundColor Cyan

# 1. Register a new patient
Write-Host "`n1. Registering new patient..." -ForegroundColor Yellow
$registerBody = @{
    email = "jane.patient@example.com"
    password = "password123"
    firstName = "Jane"
    lastName = "Doe"
    phoneNumber = "+1234567890"
    role = "PATIENT"
} | ConvertTo-Json

try {
    $registerResponse = Invoke-RestMethod -Uri "http://localhost:8086/api/auth/register" `
        -Method Post -ContentType "application/json" -Body $registerBody
    Write-Host "✓ Patient registered successfully!" -ForegroundColor Green
    Write-Host "  User ID: $($registerResponse.userId)" -ForegroundColor Gray
    Write-Host "  Email: $($registerResponse.email)" -ForegroundColor Gray
    $token = $registerResponse.token
} catch {
    Write-Host "✗ Registration failed: $($_.Exception.Message)" -ForegroundColor Red
    Write-Host "  Trying to login with existing credentials..." -ForegroundColor Yellow
    
    # Try login if registration fails (patient might already exist)
    $loginBody = @{
        email = "jane.patient@example.com"
        password = "password123"
    } | ConvertTo-Json
    
    $loginResponse = Invoke-RestMethod -Uri "http://localhost:8086/api/auth/login" `
        -Method Post -ContentType "application/json" -Body $loginBody
    Write-Host "✓ Login successful!" -ForegroundColor Green
    $token = $loginResponse.token
}

# 2. Get patient profile
Write-Host "`n2. Getting patient profile..." -ForegroundColor Yellow
$headers = @{
    "Authorization" = "Bearer $token"
    "Content-Type" = "application/json"
}

try {
    $profile = Invoke-RestMethod -Uri "http://localhost:8086/api/patients/profile/me" `
        -Method Get -Headers $headers
    Write-Host "✓ Profile retrieved successfully!" -ForegroundColor Green
    Write-Host "  Name: $($profile.firstName) $($profile.lastName)" -ForegroundColor Gray
    Write-Host "  Email: $($profile.email)" -ForegroundColor Gray
} catch {
    Write-Host "✗ Failed to get profile: $($_.Exception.Message)" -ForegroundColor Red
}

# 3. Update patient profile
Write-Host "`n3. Updating patient profile..." -ForegroundColor Yellow
$updateBody = @{
    dateOfBirth = "1990-05-15"
    gender = "FEMALE"
    bloodType = "A+"
    allergies = "Penicillin, Peanuts"
    medicalHistory = "Asthma, diagnosed 2015"
    emergencyContact = "John Doe"
    emergencyPhone = "+1987654321"
    address = "123 Main Street"
    city = "New York"
    country = "USA"
    postalCode = "10001"
} | ConvertTo-Json

try {
    $updatedProfile = Invoke-RestMethod -Uri "http://localhost:8086/api/patients/profile/me" `
        -Method Put -Headers $headers -Body $updateBody
    Write-Host "✓ Profile updated successfully!" -ForegroundColor Green
    Write-Host "  Blood Type: $($updatedProfile.bloodType)" -ForegroundColor Gray
    Write-Host "  Allergies: $($updatedProfile.allergies)" -ForegroundColor Gray
    Write-Host "  City: $($updatedProfile.city)" -ForegroundColor Gray
} catch {
    Write-Host "✗ Failed to update profile: $($_.Exception.Message)" -ForegroundColor Red
}

# 4. Get patient's appointments
Write-Host "`n4. Getting patient appointments..." -ForegroundColor Yellow
try {
    $appointments = Invoke-RestMethod -Uri "http://localhost:8086/api/patients/appointments/me" `
        -Method Get -Headers $headers
    Write-Host "✓ Appointments retrieved successfully!" -ForegroundColor Green
    Write-Host "  Total appointments: $($appointments.Count)" -ForegroundColor Gray
    if ($appointments.Count -gt 0) {
        $appointments | ForEach-Object {
            Write-Host "    - $($_.appointmentDate) with Dr. $($_.doctorName) ($($_.status))" -ForegroundColor Gray
        }
    }
} catch {
    Write-Host "✗ Failed to get appointments: $($_.Exception.Message)" -ForegroundColor Red
}

# 5. Get patient's prescriptions
Write-Host "`n5. Getting patient prescriptions..." -ForegroundColor Yellow
try {
    $prescriptions = Invoke-RestMethod -Uri "http://localhost:8086/api/patients/prescriptions/me" `
        -Method Get -Headers $headers
    Write-Host "✓ Prescriptions retrieved successfully!" -ForegroundColor Green
    Write-Host "  Total prescriptions: $($prescriptions.Count)" -ForegroundColor Gray
    if ($prescriptions.Count -gt 0) {
        $prescriptions | ForEach-Object {
            Write-Host "    - $($_.medicationName) - $($_.dosage) ($($_.frequency))" -ForegroundColor Gray
        }
    }
} catch {
    Write-Host "✗ Failed to get prescriptions: $($_.Exception.Message)" -ForegroundColor Red
}

# 6. Get patient's medical records
Write-Host "`n6. Getting patient medical records..." -ForegroundColor Yellow
try {
    $records = Invoke-RestMethod -Uri "http://localhost:8086/api/patients/medical-records/me" `
        -Method Get -Headers $headers
    Write-Host "✓ Medical records retrieved successfully!" -ForegroundColor Green
    Write-Host "  Total records: $($records.Count)" -ForegroundColor Gray
    if ($records.Count -gt 0) {
        $records | ForEach-Object {
            Write-Host "    - $($_.title) ($(Get-Date $_.recordDate -Format 'yyyy-MM-dd'))" -ForegroundColor Gray
        }
    }
} catch {
    Write-Host "✗ Failed to get medical records: $($_.Exception.Message)" -ForegroundColor Red
}

# 7. Get active prescriptions only
Write-Host "`n7. Getting patient active prescriptions..." -ForegroundColor Yellow
try {
    $activePrescriptions = Invoke-RestMethod -Uri "http://localhost:8086/api/patients/prescriptions/me/active" `
        -Method Get -Headers $headers
    Write-Host "✓ Active prescriptions retrieved successfully!" -ForegroundColor Green
    Write-Host "  Total active prescriptions: $($activePrescriptions.Count)" -ForegroundColor Gray
} catch {
    Write-Host "✗ Failed to get active prescriptions: $($_.Exception.Message)" -ForegroundColor Red
}

Write-Host "`n========== PATIENT ENDPOINTS TESTING COMPLETE ==========" -ForegroundColor Cyan

# Create and test patient user

Write-Host "Creating test patient user..." -ForegroundColor Cyan

$registerBody = @{
    email = "test.patient@example.com"
    password = "TestPass123"
    firstName = "Alice"
    lastName = "Smith"
    phoneNumber = "+1234567890"
    role = "PATIENT"
} | ConvertTo-Json

try {
    $response = Invoke-RestMethod -Uri "http://localhost:8086/api/auth/register" -Method Post -ContentType "application/json" -Body $registerBody
    Write-Host "SUCCESS: Patient registered!" -ForegroundColor Green
    Write-Host "User ID: $($response.userId)" -ForegroundColor Gray
    Write-Host "Email: $($response.email)" -ForegroundColor Gray
    Write-Host "Token (first 50 chars): $($response.token.Substring(0,50))..." -ForegroundColor Gray
    
    # Save token for next test
    $response.token | Out-File -FilePath "patient-token.txt"
    Write-Host "`nToken saved to patient-token.txt" -ForegroundColor Yellow
    
} catch {
    $errorDetails = $_.ErrorDetails.Message | ConvertFrom-Json
    Write-Host "FAILED: $($errorDetails.message)" -ForegroundColor Red
    Write-Host "This patient might already exist. Try logging in..." -ForegroundColor Yellow
}

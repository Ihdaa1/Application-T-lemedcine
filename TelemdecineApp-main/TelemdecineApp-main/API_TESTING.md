# Telemedicine Backend - Authentication API Testing Guide

## Base URL
```
http://localhost:8086/api
```

## Authentication Endpoints

### 1. Register a New User (Patient)

**Endpoint:** `POST /auth/register`

**Request Body:**
```json
{
  "email": "patient@example.com",
  "password": "password123",
  "firstName": "John",
  "lastName": "Doe",
  "phoneNumber": "+1234567890",
  "role": "PATIENT"
}
```

**cURL Command:**
```bash
curl -X POST http://localhost:8086/api/auth/register \
  -H "Content-Type: application/json" \
  -d "{\"email\":\"patient@example.com\",\"password\":\"password123\",\"firstName\":\"John\",\"lastName\":\"Doe\",\"phoneNumber\":\"+1234567890\",\"role\":\"PATIENT\"}"
```

**PowerShell Command:**
```powershell
Invoke-RestMethod -Uri "http://localhost:8086/api/auth/register" -Method Post -ContentType "application/json" -Body '{"email":"patient@example.com","password":"password123","firstName":"John","lastName":"Doe","phoneNumber":"+1234567890","role":"PATIENT"}'
```

**Expected Response (201 Created):**
```json
{
  "token": "eyJhbGciOiJIUzI1NiJ9...",
  "type": "Bearer",
  "id": 1,
  "email": "patient@example.com",
  "firstName": "John",
  "lastName": "Doe",
  "role": "PATIENT"
}
```

---

### 2. Register a New User (Doctor)

**Endpoint:** `POST /auth/register`

**Request Body:**
```json
{
  "email": "doctor@example.com",
  "password": "password123",
  "firstName": "Jane",
  "lastName": "Smith",
  "phoneNumber": "+1234567891",
  "role": "DOCTOR"
}
```

**PowerShell Command:**
```powershell
Invoke-RestMethod -Uri "http://localhost:8086/api/auth/register" -Method Post -ContentType "application/json" -Body '{"email":"doctor@example.com","password":"password123","firstName":"Jane","lastName":"Smith","phoneNumber":"+1234567891","role":"DOCTOR"}'
```

---

### 3. Login

**Endpoint:** `POST /auth/login`

**Request Body:**
```json
{
  "email": "patient@example.com",
  "password": "password123"
}
```

**cURL Command:**
```bash
curl -X POST http://localhost:8086/api/auth/login \
  -H "Content-Type: application/json" \
  -d "{\"email\":\"patient@example.com\",\"password\":\"password123\"}"
```

**PowerShell Command:**
```powershell
Invoke-RestMethod -Uri "http://localhost:8086/api/auth/login" -Method Post -ContentType "application/json" -Body '{"email":"patient@example.com","password":"password123"}'
```

**Expected Response (200 OK):**
```json
{
  "token": "eyJhbGciOiJIUzI1NiJ9...",
  "type": "Bearer",
  "id": 1,
  "email": "patient@example.com",
  "firstName": "John",
  "lastName": "Doe",
  "role": "PATIENT"
}
```

---

### 4. Health Check

**Endpoint:** `GET /auth/health`

**PowerShell Command:**
```powershell
Invoke-RestMethod -Uri "http://localhost:8086/api/auth/health" -Method Get
```

**Expected Response (200 OK):**
```json
{
  "success": true,
  "message": "Authentication service is running"
}
```

---

## Using the JWT Token

After successful login/registration, use the returned token in the `Authorization` header for protected endpoints:

**Header Format:**
```
Authorization: Bearer eyJhbGciOiJIUzI1NiJ9...
```

**PowerShell Example:**
```powershell
$token = "YOUR_JWT_TOKEN_HERE"
$headers = @{
    "Authorization" = "Bearer $token"
    "Content-Type" = "application/json"
}

Invoke-RestMethod -Uri "http://localhost:8086/api/some-protected-endpoint" -Method Get -Headers $headers
```

---

## Error Responses

### Invalid Email/Password (Login)
**Status:** 401 Unauthorized
```json
{
  "success": false,
  "message": "Invalid email or password"
}
```

### Email Already Exists (Registration)
**Status:** 400 Bad Request
```json
{
  "success": false,
  "message": "Email address already in use"
}
```

### Validation Error
**Status:** 400 Bad Request
```json
{
  "success": false,
  "message": "Email is required"
}
```

---

## Available User Roles

- `PATIENT` - Regular patient users
- `DOCTOR` - Medical professionals
- `ADMIN` - System administrators

---

## Quick Test Sequence (PowerShell)

```powershell
# 1. Health check
Invoke-RestMethod -Uri "http://localhost:8086/api/auth/health" -Method Get

# 2. Register a patient
$registerResponse = Invoke-RestMethod -Uri "http://localhost:8086/api/auth/register" -Method Post -ContentType "application/json" -Body '{"email":"test@test.com","password":"test123","firstName":"Test","lastName":"User","phoneNumber":"+1234567890","role":"PATIENT"}'
Write-Host "Token: $($registerResponse.token)"

# 3. Login with the same credentials
$loginResponse = Invoke-RestMethod -Uri "http://localhost:8086/api/auth/login" -Method Post -ContentType "application/json" -Body '{"email":"test@test.com","password":"test123"}'
Write-Host "Login Token: $($loginResponse.token)"

# 4. Store token for future requests
$token = $loginResponse.token
Write-Host "You can now use this token for authenticated requests"
```

---

## Database Verification

You can verify the created users in PostgreSQL:

```sql
-- Connect to the database
psql -U postgres -d telemedicine_db

-- View all users
SELECT id, email, first_name, last_name, role, active FROM users;

-- View all patients
SELECT p.id, u.email, u.first_name, u.last_name 
FROM patients p 
JOIN users u ON p.user_id = u.id;

-- View all doctors
SELECT d.id, u.email, u.first_name, u.last_name, d.specialization, d.license_number
FROM doctors d 
JOIN users u ON d.user_id = u.id;
```

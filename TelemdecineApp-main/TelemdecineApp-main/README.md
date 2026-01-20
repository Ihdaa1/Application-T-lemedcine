# Telemedicine Backend Application

A comprehensive Spring Boot backend application for a telemedicine platform with PostgreSQL database.

## Features

- **User Management**: Registration and authentication with JWT
- **Role-Based Access**: Patient, Doctor, and Admin roles
- **Appointment Management**: Book, manage, and track medical appointments  
- **Consultation System**: Complete consultation records with diagnosis and treatment
- **Prescription Management**: Digital prescription system
- **Medical Records**: Store and manage patient medical records
- **Doctor Profiles**: Detailed doctor information with specializations
- **Patient Profiles**: Comprehensive patient health information

## Technology Stack

- **Java 17**
- **Spring Boot 3.2.1**
- **Spring Security** with JWT authentication
- **Spring Data JPA** with Hibernate
- **PostgreSQL** database
- **Lombok** for reducing boilerplate code
- **Maven** for dependency management

## Project Structure

```
src/main/java/com/telemedicine/
├── entity/              # JPA entities (User, Patient, Doctor, Appointment, etc.)
├── repository/          # Spring Data JPA repositories
├── service/             # Business logic layer
├── controller/          # REST API controllers
├── dto/                 # Data Transfer Objects
├── security/            # Security configuration and JWT utilities
├── exception/           # Custom exceptions and global exception handler
└── TelemedicineApplication.java
```

## Database Setup

1. Install PostgreSQL
2. Create a database:
   ```sql
   CREATE DATABASE telemedicine_db;
   ```

3. Update `application.properties` with your database credentials:
   ```properties
   spring.datasource.url=jdbc:postgresql://localhost:5432/telemedicine_db
   spring.datasource.username=your_username
   spring.datasource.password=your_password
   ```

## Running the Application

1. Clone the repository
2. Navigate to the project directory
3. Run using Maven:
   ```bash
   mvn spring-boot:run
   ```

The application will start on `http://localhost:8080/api`

## API Endpoints

### Authentication
- `POST /api/auth/register` - Register a new user
- `POST /api/auth/login` - Login and get JWT token

### Patients
- `GET /api/patients/profile` - Get patient profile
- `PUT /api/patients/profile` - Update patient profile
- `GET /api/patients/{id}/appointments` - Get patient appointments
- `GET /api/patients/{id}/prescriptions` - Get patient prescriptions
- `GET /api/patients/{id}/medical-records` - Get medical records

### Doctors
- `GET /api/doctors/public` - Get all available doctors (public)
- `GET /api/doctors/public/{id}` - Get doctor details (public)
- `GET /api/doctors/profile` - Get doctor profile  
- `PUT /api/doctors/profile` - Update doctor profile
- `GET /api/doctors/{id}/appointments` - Get doctor appointments

### Appointments
- `POST /api/appointments` - Create new appointment
- `GET /api/appointments/{id}` - Get appointment details
- `PUT /api/appointments/{id}` - Update appointment
- `DELETE /api/appointments/{id}` - Cancel appointment
- `PUT /api/appointments/{id}/status` - Update appointment status

### Prescriptions
- `POST /api/prescriptions` - Create prescription (Doctor only)
- `GET /api/prescriptions/{id}` - Get prescription details
- `PUT /api/prescriptions/{id}` - Update prescription

### Consultations
- `POST /api/consultations` - Create consultation record
- `GET /api/consultations/{id}` - Get consultation details
- `PUT /api/consultations/{id}` - Update consultation

## Security

The application uses JWT (JSON Web Token) for authentication:

1. Register or login to get a JWT token
2. Include the token in the Authorization header for authenticated requests:
   ```
   Authorization: Bearer <your_jwt_token>
   ```

## Database Entities

### Core Entities
- **User**: Base user entity with authentication details
- **Patient**: Extended patient profile with medical information
- **Doctor**: Doctor profile with specialization and credentials
- **Appointment**: Medical appointments between patients and doctors
- **Consultation**: Consultation records with diagnosis and treatment
- **Prescription**: Digital prescriptions with medication details
- **Medical Record**: Patient medical records and documents

## API Response Format

Success Response:
```json
{
  "success": true,
  "message": "Operation successful",
  "data": { ... }
}
```

Error Response:
```json
{
  "success": false,
  "message": "Error description",
  "data": null
}
```

## Development

### Adding New Features
1. Create entity in `entity/` package
2. Create repository interface in `repository/`
3. Implement business logic in `service/`
4. Create DTOs in `dto/`
5. Create REST controller in `controller/`

### Running Tests
```bash
mvn test
```

## Configuration

Key configuration properties in `application.properties`:

- `server.port`: Application port (default: 8080)
- `spring.datasource.*`: Database configuration
- `spring.jpa.*`: JPA/Hibernate settings
- `jwt.secret`: JWT secret key (change in production!)
- `jwt.expiration`: Token expiration time in milliseconds

## Next Steps

To complete the implementation:
1. Create service implementations for business logic
2. Create REST controllers for API endpoints
3. Add database initialization scripts
4. Implement integration tests
5. Add API documentation (Swagger/OpenAPI)
6. Implement file upload for medical records
7. Add email notifications
8. Implement video consultation integration
9. Add payment processing
10. Deploy to production

## License

This project is licensed under the MIT License.

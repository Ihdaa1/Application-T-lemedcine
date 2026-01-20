# Telemedicine Backend - Setup Guide

## Prerequisites

Before you begin, ensure you have the following installed:

- **Java 17** or higher ([Download](https://adoptium.net/))
- **Maven 3.8+** ([Download](https://maven.apache.org/download.cgi))
- **PostgreSQL 14+** ([Download](https://www.postgresql.org/download/))
- **Git** (optional, for version control)

## Step-by-Step Setup

### 1. Verify Java Installation

```bash
java -version
```

Expected output: `java version "17.x.x"` or higher

### 2. Verify Maven Installation

```bash
mvn -version
```

Expected output: `Apache Maven 3.x.x`

### 3. Setup PostgreSQL Database

#### Option A: Using PostgreSQL Command Line

1. Open PostgreSQL command line (psql)
2. Create the database:

```sql
CREATE DATABASE telemedicine_db;
```

3. Create a user (optional):

```sql
CREATE USER telemedicine_user WITH PASSWORD 'your_password';
GRANT ALL PRIVILEGES ON DATABASE telemedicine_db TO telemedicine_user;
```

#### Option B: Using pgAdmin

1. Open pgAdmin
2. Right-click on "Databases" → Create → Database
3. Name it `telemedicine_db`
4. Click "Save"

### 4. Configure Application Properties

Edit `src/main/resources/application.properties`:

```properties
# Update these values with your PostgreSQL credentials
spring.datasource.url=jdbc:postgresql://localhost:5432/telemedicine_db
spring.datasource.username=postgres  # or your PostgreSQL username
spring.datasource.password=postgres  # or your PostgreSQL password

# IMPORTANT: Change this JWT secret in production!
jwt.secret=your-very-long-secret-key-minimum-256-bits-for-production-use-change-this
```

### 5. Build the Project

Navigate to the project directory and run:

```bash
cd d:\TelemdecineApp
mvn clean install
```

This will:
- Download all dependencies
- Compile the code
- Run tests (if any)
- Package the application

### 6. Run the Application

#### Option A: Using Maven

```bash
mvn spring-boot:run
```

#### Option B: Using JAR file

```bash
java -jar target/telemedicine-backend-1.0.0.jar
```

### 7. Verify Application is Running

Open your browser or use curl to test:

```bash
curl http://localhost:8080/api
```

Expected: Server response (might be 401 Unauthorized, which is correct for unauthenticated requests)

## Initial Database Setup (Optional)

To populate the database with sample data:

1. Connect to PostgreSQL:

```bash
psql -U postgres -d telemedicine_db
```

2. Run the init script:

```bash
\i src/main/resources/init-database.sql
```

This creates:
- An admin user: `admin@telemedicine.com` / `admin123`
- Sample doctors with profiles

## Testing the API

### 1. Register a New User

```bash
curl -X POST http://localhost:8080/api/auth/register \
  -H "Content-Type: application/json" \
  -d '{
    "email": "patient@example.com",
    "password": "password123",
    "firstName": "John",
    "lastName": "Doe",
    "phoneNumber": "+1234567890",
    "role": "PATIENT"
  }'
```

### 2. Login

```bash
curl -X POST http://localhost:8080/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{
    "email": "patient@example.com",
    "password": "password123"
  }'
```

Save the JWT token from the response.

### 3. Make Authenticated Requests

Use the token in the Authorization header:

```bash
curl -X GET http://localhost:8080/api/patients/profile \
  -H "Authorization: Bearer YOUR_JWT_TOKEN_HERE"
```

## Development Tools

### Recommended IDEs

1. **IntelliJ IDEA** (Recommended)
   - Open project as Maven project
   - Enable annotation processing for Lombok
   - Install Lombok plugin

2. **Eclipse**
   - Import as Maven project
   - Install Lombok plugin from projectlombok.org

3. **VS Code**
   - Install Java Extension Pack
   - Install Spring Boot Extension Pack
   - Install Lombok Extension

### Enable Lombok in IntelliJ

1. Go to Settings → Plugins
2. Search for "Lombok"
3. Install and restart
4. Go to Settings → Build, Execution, Deployment → Compiler → Annotation Processors
5. Enable "Enable annotation processing"

### Database GUI Tools

- **pgAdmin** - Comes with PostgreSQL
- **DBeaver** - Free, multi-database tool
- **DataGrip** - JetBrains IDE (paid)

## Troubleshooting

### Issue: "Cannot connect to database"

**Solution:**
1. Check PostgreSQL is running: `sudo systemctl status postgresql` (Linux) or check Services (Windows)
2. Verify database exists: `psql -U postgres -l`
3. Check username/password in application.properties
4. Verify connection URL format

### Issue: "Port 8080 already in use"

**Solution:**
1. Change port in application.properties:
   ```properties
   server.port=8081
   ```
2. Or kill the process using port 8080

### Issue: "Lombok not working"

**Solution:**
1. Ensure Lombok is in pom.xml (it is)
2. Enable annotation processing in your IDE
3. Install Lombok plugin
4. Restart IDE

### Issue: "JWT secret key too short"

**Solution:**
The JWT secret must be at least 256 bits (32 characters). Update in application.properties:
```properties
jwt.secret=your-256-bit-secret-key-change-this-in-production-please-make-it-very-long-and-secure
```

### Issue: "Tables not created"

**Solution:**
1. Check `spring.jpa.hibernate.ddl-auto=update` in application.properties
2. Check database connection
3. Look for errors in application logs
4. Try `spring.jpa.hibernate.ddl-auto=create` (WARNING: drops existing tables)

## Application Structure

```
URL Base: http://localhost:8080/api

Public Endpoints (No Auth Required):
- POST /auth/register
- POST /auth/login
- GET  /doctors/public
- GET  /doctors/public/{id}

Protected Endpoints (Auth Required):
- GET  /patients/profile
- PUT  /patients/profile
- GET  /doctors/profile
- PUT  /doctors/profile
- POST /appointments
- GET  /appointments/{id}
- POST /prescriptions
- ... (more endpoints)
```

## Environment Profiles

### Development (default)
Uses `application.properties`

### Production
Create `application-prod.properties`:

```properties
spring.datasource.url=jdbc:postgresql://production-host:5432/telemedicine_db
spring.datasource.username=prod_user
spring.datasource.password=strong_password
spring.jpa.hibernate.ddl-auto=validate
logging.level.root=WARN
```

Run with:
```bash
java -jar target/telemedicine-backend-1.0.0.jar --spring.profiles.active=prod
```

## Next Steps

After successful setup:

1. **Complete Service Layer**: Implement business logic in service classes
2. **Create Controllers**: Build REST API endpoints
3. **Add Validation**: Enhance validation rules
4. **Write Tests**: Create unit and integration tests
5. **Add Documentation**: Generate API documentation with Swagger
6. **Deploy**: Deploy to cloud (AWS, Azure, Heroku, etc.)

## Useful Commands

```bash
# Clean and build
mvn clean install

# Run with specific profile
mvn spring-boot:run -Dspring-boot.run.profiles=dev

# Skip tests
mvn clean install -DskipTests

# Run tests only
mvn test

# Package without running
mvn package -DskipTests

# Check for dependency updates
mvn versions:display-dependency-updates
```

## Support & Documentation

- [Spring Boot Documentation](https://spring.io/projects/spring-boot)
- [Spring Security Documentation](https://spring.io/projects/spring-security)
- [Spring Data JPA Documentation](https://spring.io/projects/spring-data-jpa)
- [PostgreSQL Documentation](https://www.postgresql.org/docs/)

## Security Checklist for Production

- [ ] Change JWT secret to a strong, random key
- [ ] Use environment variables for sensitive data
- [ ] Set `spring.jpa.hibernate.ddl-auto=validate`
- [ ] Enable HTTPS/TLS
- [ ] Configure proper CORS settings
- [ ] Implement rate limiting
- [ ] Add API authentication/authorization
- [ ] Enable logging and monitoring
- [ ] Regular security updates
- [ ] Database backups configured

---

**Need Help?** Check PROJECT_SUMMARY.md for detailed information about the project structure.

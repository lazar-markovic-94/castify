# Spring Profiles Configuration

This application supports three Spring profiles for different environments:

## Available Profiles

### 1. Local (default)
Development profile for local machines.

**Features:**
- Local PostgreSQL database
- Detailed debug logging
- Swagger UI enabled
- SQL logging enabled
- Flyway clean operations allowed

**Database:**
- URL: `jdbc:postgresql://localhost:5432/castify`
- Username: `lazarmarkovic` (configurable via `DB_USERNAME`)
- Password: empty (configurable via `DB_PASSWORD`)

**Logging:**
- Level: DEBUG for application code
- File: `logs/castify-local.log`

---

### 2. Develop
Profile for development/staging environments.

**Features:**
- Remote database connection
- Moderate logging
- Swagger UI enabled
- SQL logging disabled
- Flyway clean operations disabled

**Database:**
- URL: Configured via `DB_URL` environment variable
- Username: Configured via `DB_USERNAME` environment variable
- Password: Configured via `DB_PASSWORD` environment variable
- Default URL: `jdbc:postgresql://dev-db-server:5432/castify_dev`

**Logging:**
- Level: INFO for application code
- File: `/var/log/castify/castify-develop.log`

---

### 3. Production
Profile for production environments.

**Features:**
- Remote database connection
- Minimal logging (WARN/ERROR)
- Swagger UI disabled
- SQL logging disabled
- Flyway clean operations disabled
- Graceful shutdown enabled
- Actuator endpoints for monitoring

**Database:**
- URL: **Required** via `DB_URL` environment variable
- Username: **Required** via `DB_USERNAME` environment variable
- Password: **Required** via `DB_PASSWORD` environment variable
- Connection pool: 20 max, 10 min idle

**Logging:**
- Level: WARN for framework, INFO for application
- File: `/var/log/castify/castify-production.log`

**Security:**
- JWT secret **required** via `JWT_SECRET` environment variable
- Error details hidden from responses
- Swagger/OpenAPI documentation disabled

---

## Usage

### Running with Maven

```bash
# Local profile (default)
mvn spring-boot:run

# Develop profile
mvn spring-boot:run -Dspring-boot.run.profiles=develop

# Production profile
mvn spring-boot:run -Dspring-boot.run.profiles=production
```

### Running JAR file

```bash
# Local profile (default)
java -jar castify-app.jar

# Develop profile
java -jar castify-app.jar --spring.profiles.active=develop

# Production profile
java -jar castify-app.jar --spring.profiles.active=production
```

### Using Environment Variables

```bash
# Set profile via environment variable
export SPRING_PROFILES_ACTIVE=production
java -jar castify-app.jar
```

### Docker

```dockerfile
# In Dockerfile
ENV SPRING_PROFILES_ACTIVE=production

# Or via docker run
docker run -e SPRING_PROFILES_ACTIVE=production castify-app
```

---

## Environment Variables

### Required for All Profiles
None (local has defaults)

### Required for Develop Profile
- `DB_USERNAME` - Database username
- `DB_PASSWORD` - Database password
- `JWT_SECRET` - JWT signing secret (256-bit minimum)

### Required for Production Profile
- `DB_URL` - Full database JDBC URL
- `DB_USERNAME` - Database username
- `DB_PASSWORD` - Database password
- `JWT_SECRET` - JWT signing secret (256-bit minimum, **must be different from develop**)

### Optional Environment Variables (All Profiles)
- `SERVER_PORT` - Server port (default: 8080)
- `UPLOAD_DIR` - File upload directory
- `JWT_EXPIRATION_MS` - JWT token expiration in milliseconds (default: 86400000 = 24 hours)

---

## Configuration Hierarchy

Settings are applied in the following order (later overrides earlier):
1. `application.yml` - Common configuration
2. `application-{profile}.yml` - Profile-specific configuration
3. Environment variables - Runtime overrides

---

## Example Environment Files

### .env.local (for local development)
```bash
SPRING_PROFILES_ACTIVE=local
DB_USERNAME=lazarmarkovic
DB_PASSWORD=
SERVER_PORT=8080
UPLOAD_DIR=uploads
```

### .env.develop (for staging environment)
```bash
SPRING_PROFILES_ACTIVE=develop
DB_URL=jdbc:postgresql://dev-db.example.com:5432/castify_dev
DB_USERNAME=castify_dev_user
DB_PASSWORD=dev_secure_password
JWT_SECRET=your-dev-jwt-secret-key-min-256-bits-long
SERVER_PORT=8080
UPLOAD_DIR=/var/castify/uploads
```

### .env.production (for production environment)
```bash
SPRING_PROFILES_ACTIVE=production
DB_URL=jdbc:postgresql://prod-db.example.com:5432/castify_prod
DB_USERNAME=castify_prod_user
DB_PASSWORD=prod_very_secure_password_from_secrets_manager
JWT_SECRET=your-production-jwt-secret-key-min-256-bits-long-unique-from-dev
SERVER_PORT=8080
UPLOAD_DIR=/var/castify/uploads
JWT_EXPIRATION_MS=86400000
```

---

## Security Notes

1. **Never commit** `.env` files with real credentials
2. Use **different JWT secrets** for each environment
3. Use a secrets manager (AWS Secrets Manager, HashiCorp Vault, etc.) in production
4. Ensure JWT secret is at least 256 bits (32 characters) for HS256
5. Rotate JWT secrets periodically
6. Use strong, unique database passwords for each environment
7. Swagger UI is disabled in production for security

---

## Monitoring (Production)

The production profile includes Spring Boot Actuator endpoints:

- `/actuator/health` - Application health status
- `/actuator/info` - Application information
- `/actuator/metrics` - Application metrics
- `/actuator/prometheus` - Prometheus metrics format

Access these endpoints for monitoring and alerting in production.

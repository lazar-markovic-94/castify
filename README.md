# Castify - Talent Casting Marketplace

A production-ready Spring Boot backend for a talent casting marketplace where clients can search for actors/talents for marketing videos and photos, and message them directly.

## Table of Contents

- [Tech Stack](#tech-stack)
- [Architecture](#architecture)
- [Project Structure](#project-structure)
- [Features](#features)
- [Prerequisites](#prerequisites)
- [Getting Started](#getting-started)
- [Running with Docker](#running-with-docker)
- [Environment Configuration](#environment-configuration)
- [Database](#database)
- [API Documentation](#api-documentation)
- [Testing](#testing)
- [CI/CD](#cicd)
- [Security](#security)
- [Deployment](#deployment)
- [Troubleshooting](#troubleshooting)

## Tech Stack

- **Language**: Kotlin 2.2.21
- **Framework**: Spring Boot 4.0.0
- **Architecture**: Clean Architecture (Multi-Module Maven)
- **Database**: PostgreSQL 17
- **Migration**: Flyway
- **Security**: JWT (Spring Security)
- **API Documentation**: OpenAPI 3 (springdoc-openapi 2.3.0)
- **Build Tool**: Maven 3.9.6
- **Testing**: JUnit 5, MockK 1.13.9
- **Container**: Docker with multi-stage builds
- **CI/CD**: GitHub Actions

## Architecture

This project follows **Clean Architecture** (Hexagonal Architecture) with clear separation of concerns across 5 Maven modules:

```
castify/                           # Root Maven Project
├── castify-domain/                # Domain Layer
│   └── Entity models, value objects, domain logic
│   └── No external dependencies
├── castify-usecase/               # Use Case Layer
│   └── Business logic, application services
│   └── Depends on: domain
├── castify-persistence/           # Persistence Adapter
│   └── JPA repositories, entity mappings
│   └── Depends on: domain, usecase
├── castify-web/                   # Web Adapter
│   └── REST controllers, DTOs, security config
│   └── Depends on: domain, usecase
└── castify-app/                   # Application Bootstrap
    └── Main application, configuration
    └── Depends on: all modules
```

### Module Responsibilities

| Module | Responsibility | Dependencies |
|--------|---------------|--------------|
| **castify-domain** | Core business entities, value objects, repository interfaces | None (pure domain) |
| **castify-usecase** | Application services, business logic, DTOs | domain |
| **castify-persistence** | JPA implementations, database adapters | domain, usecase |
| **castify-web** | REST controllers, security, CORS, JWT | domain, usecase |
| **castify-app** | Spring Boot application, configuration files | All modules |

### Dependency Flow

```
castify-app
    ├── castify-web ────────┐
    │   └── castify-usecase │
    │       └── castify-domain
    └── castify-persistence ┘
        └── castify-usecase
            └── castify-domain
```

## Project Structure

```
castify/
├── .github/
│   └── workflows/
│       └── backend-tests.yml          # CI/CD pipeline
├── .mvn/                              # Maven wrapper
├── castify-domain/
│   └── src/main/kotlin/com/castify/domain/
│       ├── user/                      # User entities & repos
│       ├── talent/                    # Talent profile entities
│       ├── client/                    # Client organization
│       ├── media/                     # Media assets, portfolio
│       ├── messaging/                 # Conversations, messages
│       ├── rating/                    # Ratings & reviews
│       ├── collaboration/             # Collaboration tracking
│       └── exception/                 # Domain exceptions
├── castify-usecase/
│   └── src/main/kotlin/com/castify/usecase/
│       ├── auth/                      # Authentication use cases
│       ├── talent/                    # Talent management
│       ├── client/                    # Client management
│       ├── media/                     # Media & portfolio
│       ├── messaging/                 # Messaging use cases
│       ├── rating/                    # Rating & review use cases
│       └── dto/                       # Request/Response DTOs
├── castify-persistence/
│   └── src/main/kotlin/com/castify/persistence/
│       └── jpa/                       # JPA repositories
├── castify-web/
│   └── src/main/kotlin/com/castify/web/
│       ├── controller/                # REST controllers
│       ├── security/                  # JWT, authentication
│       └── config/                    # CORS, security config
├── castify-app/
│   ├── src/main/kotlin/              # Application entry point
│   └── src/main/resources/
│       ├── application.yml            # Base configuration
│       ├── application-local.yml      # Local profile
│       ├── application-develop.yml    # Development profile
│       ├── application-production.yml # Production profile
│       └── db/migration/              # Flyway migrations
├── Dockerfile                         # Multi-stage Docker build
├── pom.xml                            # Parent POM
└── README.md                          # This file
```

## Features

### Core Features

#### Authentication & Authorization
- User registration with role-based access (TALENT/CLIENT/ADMIN)
- JWT-based stateless authentication
- BCrypt password encryption
- Token expiration and refresh flow

#### Talent Profile Management
- Comprehensive talent profiles with bio, experience, and visibility settings
- Location-based profiles (country, city, coordinates)
- Multi-language support with proficiency levels
- Tags and content type capabilities
- Social media integration (Instagram, TikTok, YouTube, Twitter, etc.)
- Portfolio management with media assets (photos, videos, audio)

#### Client Organization Management
- Organization profiles for agencies, brands, and individuals
- Company details with website and description

#### Advanced Search & Discovery
- **Text Search**: Search by name or bio
- **Location Search**: Haversine formula for radius-based search (no PostGIS needed)
- **Language Filtering**: Filter by language proficiency
- **Tag Filtering**: Search by skills and categories
- **Content Type Filtering**: Instagram Reel, TikTok, YouTube Video, etc.
- **Location Mode**: Remote, On-site, Hybrid
- **Visibility Filter**: Only show publicly visible profiles
- **Pagination**: Efficient pagination with customizable page size

#### Messaging System
- One-to-one conversations between clients and talents
- Automatic conversation creation
- Real-time message history
- Unique conversation per client-talent pair

#### Rating & Review System ⭐ NEW
- 5-star rating system with written reviews
- Overall rating and review count tracking
- Talent statistics (average rating, total collaborations, earnings)
- Leaderboard (top-rated and most successful talents)
- Profile completeness tracking
- Collaboration completion recording

### Infrastructure Features

- **Environment-based Configuration**: Local, Develop, Production profiles
- **CORS Management**: Configurable CORS origins per environment
- **File Upload**: Media asset management with metadata
- **Health Checks**: Spring Boot Actuator endpoints
- **API Documentation**: Interactive Swagger UI
- **Database Migrations**: Version-controlled schema with Flyway

## Prerequisites

- **Java 21** or higher
- **Maven 3.9.6+** (included via Maven wrapper)
- **Docker** & **Docker Compose** (for containerized deployment)
- **PostgreSQL 17** (via Docker or local installation)

## Getting Started

### Option 1: Local Development (Recommended for Development)

#### 1. Clone the Repository

```bash
git clone <repository-url>
cd castify
```

#### 2. Start PostgreSQL

Using Docker Compose (from parent directory):

```bash
cd ..
docker-compose up -d postgres
```

Or use a local PostgreSQL installation with these credentials:
- Database: `castify`
- Username: `castify`
- Password: `castify123`
- Port: `5432`

#### 3. Build the Project

```bash
./mvnw clean install
```

This will:
- Compile all 5 modules
- Run all unit tests
- Package the application JAR

#### 4. Run the Application

```bash
./mvnw spring-boot:run -pl castify-app
```

Or run the JAR directly:

```bash
java -jar castify-app/target/castify-app-0.0.1-SNAPSHOT.jar
```

The application will start on `http://localhost:8080`.

#### 5. Verify the Application

```bash
curl http://localhost:8080/actuator/health
# Expected: {"status":"UP"}
```

### Option 2: Docker Development (Full Stack)

Run the entire stack (PostgreSQL + Backend + Frontend) with Docker Compose:

```bash
cd ..  # Go to parent directory
docker-compose up -d --build
```

This will:
- Build the backend Docker image
- Build the frontend Docker image
- Start PostgreSQL
- Start the backend on `http://localhost:8080`
- Start the frontend on `http://localhost:80`

View logs:

```bash
docker-compose logs -f backend
```

Stop all services:

```bash
docker-compose down
```

## Running with Docker

### Backend Docker Image

The Dockerfile uses a **multi-stage build** for optimization:

**Stage 1: Builder** (eclipse-temurin:21-jdk-alpine)
- Copies Maven wrapper and dependencies
- Compiles and packages the application
- Skips tests for faster builds

**Stage 2: Runtime** (eclipse-temurin:21-jre-alpine)
- Only includes JRE (smaller image size)
- Runs as non-root user (`castify:1000`)
- Includes health checks
- Final image: ~200MB (vs 600MB single-stage)

### Build Backend Image

```bash
docker build -t castify-backend:latest .
```

### Run Backend Container

```bash
docker run -d \
  -p 8080:8080 \
  -e SPRING_PROFILES_ACTIVE=production \
  -e DB_URL=jdbc:postgresql://host.docker.internal:5432/castify \
  -e DB_USERNAME=castify \
  -e DB_PASSWORD=castify123 \
  -e JWT_SECRET=your-production-secret \
  -e ALLOWED_ORIGINS=https://yourdomain.com \
  --name castify-backend \
  castify-backend:latest
```

## Environment Configuration

The application supports three Spring profiles:

### Local Profile (`application-local.yml`)

```yaml
SPRING_PROFILES_ACTIVE=local
```

- Uses `localhost` for database
- CORS allows `localhost:3000`, `localhost:4200`, `localhost:5173`
- SQL logging enabled
- Swagger UI enabled

### Develop Profile (`application-develop.yml`)

```yaml
SPRING_PROFILES_ACTIVE=develop
```

- Uses environment variables for database
- CORS configurable via `ALLOWED_ORIGINS`
- Swagger UI enabled
- SQL logging disabled

### Production Profile (`application-production.yml`)

```yaml
SPRING_PROFILES_ACTIVE=production
```

- **REQUIRES** environment variables:
  - `DB_URL`
  - `DB_USERNAME`
  - `DB_PASSWORD`
  - `JWT_SECRET` (min 256 bits)
  - `ALLOWED_ORIGINS` (your production domain)
- Swagger UI **disabled**
- SQL logging disabled
- Optimized connection pooling

### Environment Variables

| Variable | Description | Required | Default |
|----------|-------------|----------|---------|
| `SPRING_PROFILES_ACTIVE` | Active profile | No | `local` |
| `DB_URL` | JDBC connection URL | Production | `jdbc:postgresql://localhost:5432/castify` |
| `DB_USERNAME` | Database username | Production | `castify` |
| `DB_PASSWORD` | Database password | Production | `castify123` |
| `JWT_SECRET` | JWT signing secret | Production | (dev default) |
| `JWT_EXPIRATION_MS` | Token expiration in ms | No | `86400000` (24h) |
| `ALLOWED_ORIGINS` | CORS allowed origins | Production | `http://localhost:3000` |
| `UPLOAD_DIR` | Media upload directory | No | `/app/uploads` |
| `SERVER_PORT` | Server port | No | `8080` |

### Example Production Configuration

```bash
# .env file
SPRING_PROFILES_ACTIVE=production
DB_URL=jdbc:postgresql://prod-db.example.com:5432/castify
DB_USERNAME=castify_prod
DB_PASSWORD=super-secure-password-change-me
JWT_SECRET=your-256-bit-secret-generated-with-openssl-rand-base64-64
ALLOWED_ORIGINS=https://castify.com,https://app.castify.com
```

Generate a secure JWT secret:

```bash
openssl rand -base64 64
```

## Database

### Schema Overview

The database schema includes 15+ tables:

| Table | Description |
|-------|-------------|
| `app_user` | User accounts with roles |
| `client_org` | Client organization details |
| `talent_profile` | Talent profile information |
| `talent_statistics` | Rating & review statistics |
| `language` | Supported languages (pre-seeded) |
| `talent_language` | Talent language proficiency |
| `tag` | Searchable tags |
| `talent_tag` | Tags associated with talents |
| `talent_capability` | Content types and location modes |
| `social_platform` | Social media platforms (pre-seeded) |
| `talent_social_link` | Talent social media profiles |
| `media_asset` | Media files (photos, videos, etc.) |
| `talent_portfolio_item` | Portfolio items for talents |
| `conversation` | Conversations between clients and talents |
| `message` | Messages within conversations |
| `talent_rating` | Ratings and reviews |
| `collaboration` | Completed collaborations |

### Migrations

Flyway manages all database migrations. Migration files are in:

```
castify-app/src/main/resources/db/migration/
```

To check migration status:

```bash
./mvnw flyway:info
```

To migrate manually:

```bash
./mvnw flyway:migrate
```

### Database Connection

Local PostgreSQL:

```bash
psql -h localhost -U castify -d castify
```

Docker PostgreSQL:

```bash
docker exec -it castify-postgres psql -U castify -d castify
```

## API Documentation

### Swagger UI

When running in `local` or `develop` profile:

```
http://localhost:8080/swagger-ui.html
```

OpenAPI JSON:

```
http://localhost:8080/api-docs
```

**Note**: Swagger UI is **disabled** in production profile for security.

### API Endpoints Summary

#### Authentication (`/api/auth`)
- `POST /api/auth/register` - Register new user
- `POST /api/auth/login` - Login and get JWT token

#### User (`/api`)
- `GET /api/me` - Get current user info (authenticated)

#### Talent Profile (`/api/talents`)
- `POST /api/talents/me/profile` - Create/update profile
- `GET /api/talents/me/profile` - Get my profile
- `POST /api/talents/me/location` - Update location
- `POST /api/talents/me/languages` - Update languages
- `POST /api/talents/me/tags` - Update tags
- `POST /api/talents/me/capabilities` - Update capabilities
- `POST /api/talents/me/social-links` - Update social links

#### Portfolio (`/api/talents/me`)
- `POST /api/talents/me/media` - Upload media asset
- `POST /api/talents/me/portfolio` - Add to portfolio
- `DELETE /api/talents/me/portfolio/{mediaId}` - Remove from portfolio
- `GET /api/talents/me/portfolio` - Get my portfolio

#### Public Search (`/api/talents`)
- `GET /api/talents` - Search talents (public)
- `GET /api/talents/{talentId}` - Get talent details (public)
- `GET /api/talents/{talentId}/portfolio` - Get talent portfolio (public)

#### Ratings & Reviews (`/api/ratings`)
- `POST /api/ratings/{talentId}` - Rate a talent (CLIENT only)
- `GET /api/ratings/{talentId}` - Get talent ratings (public)
- `GET /api/ratings/leaderboard` - Get top-rated talents (public)
- `GET /api/ratings/{talentId}/statistics` - Get talent statistics (public)

#### Client Organization (`/api/client`)
- `POST /api/client/org` - Create/update organization
- `GET /api/client/org` - Get my organization

#### Messaging (`/api/conversations`)
- `POST /api/conversations` - Create conversation (CLIENT only)
- `GET /api/conversations` - List my conversations
- `GET /api/conversations/{id}/messages` - Get messages
- `POST /api/conversations/{id}/messages` - Send message

### Search Query Parameters

`GET /api/talents` supports:

| Parameter | Type | Description | Example |
|-----------|------|-------------|---------|
| `q` | String | Name or bio search | `q=John` |
| `countryCode` | String | ISO country code | `countryCode=US` |
| `city` | String | City name | `city=New York` |
| `lat` | Double | Latitude | `lat=40.7128` |
| `lon` | Double | Longitude | `lon=-74.0060` |
| `radiusKm` | Double | Search radius in km | `radiusKm=50` |
| `languageCodes` | List | Language codes | `languageCodes=en,es` |
| `tagNames` | List | Tag names | `tagNames=actor,model` |
| `contents` | List | Content types | `contents=INSTAGRAM_REEL` |
| `locationModes` | List | Location modes | `locationModes=REMOTE` |
| `visibleOnly` | Boolean | Only visible profiles | `visibleOnly=true` |
| `page` | Integer | Page number | `page=0` |
| `size` | Integer | Page size | `size=20` |

Example:

```bash
curl "http://localhost:8080/api/talents?lat=40.7128&lon=-74.0060&radiusKm=50&languageCodes=en&visibleOnly=true&page=0&size=10"
```

## Testing

### Run All Tests

```bash
./mvnw test
```

### Run Tests for Specific Module

```bash
./mvnw test -pl castify-domain
./mvnw test -pl castify-usecase
```

### Test Coverage

The project includes comprehensive tests:

- **Domain Tests** (45 tests)
  - User entity validation
  - Talent profile business logic
  - Location, capabilities, social links

- **Use Case Tests** (60+ tests)
  - Authentication (register, login)
  - Talent management
  - Client management
  - Messaging
  - Rating & review system
  - Media & portfolio
  - Search functionality

- **Web Tests** (18 tests)
  - JWT service
  - Token generation and validation
  - Security configuration

### Test Results

```
[INFO] Reactor Summary:
[INFO]
[INFO] castify-domain ........ SUCCESS [  1.788 s] - 45 tests
[INFO] castify-usecase ....... SUCCESS [  3.454 s] - 60+ tests
[INFO] castify-persistence ... SUCCESS [  1.975 s]
[INFO] castify-web ........... SUCCESS [  2.318 s] - 18 tests
[INFO] castify-app ........... SUCCESS [  0.247 s]
[INFO] ------------------------------------------------------------------------
[INFO] BUILD SUCCESS
[INFO] ------------------------------------------------------------------------
```

## CI/CD

### GitHub Actions

The project includes a GitHub Actions workflow for automated testing:

**File**: `.github/workflows/backend-tests.yml`

**Triggers**:
- Pull requests to `main` or `develop` branches
- Changes to backend code or workflow file

**Steps**:
1. Checkout code
2. Set up Java 21 (Temurin distribution)
3. Cache Maven dependencies
4. Run `./mvnw clean install -B` (build + test)
5. Report results

### Running CI Locally

To simulate the CI build:

```bash
./mvnw clean install -B
```

The `-B` flag runs Maven in batch mode (no interactive prompts).

## Security

### Authentication Flow

1. **Registration**: User registers with email, password, and role
2. **Password Hashing**: BCrypt with strength 10
3. **Login**: User submits credentials
4. **JWT Generation**: Server generates JWT with user ID and role
5. **Token Usage**: Client includes JWT in `Authorization: Bearer <token>` header
6. **Validation**: Server validates JWT signature and expiration

### Authorization

Role-based access control:

| Role | Permissions |
|------|-------------|
| `TALENT` | Create/update talent profile, manage portfolio, receive messages |
| `CLIENT` | Create/update client org, search talents, initiate conversations, rate talents |
| `ADMIN` | Full access (for future admin features) |

### Security Best Practices

- ✅ Stateless JWT authentication
- ✅ BCrypt password hashing
- ✅ CORS configured per environment
- ✅ SQL injection prevention (JPA/Hibernate)
- ✅ XSS prevention (JSON serialization)
- ✅ HTTPS recommended in production
- ✅ Non-root Docker container
- ✅ Secrets via environment variables
- ⚠️ Rate limiting (recommended, not yet implemented)
- ⚠️ Email verification (recommended, not yet implemented)

## Deployment

### Docker Compose Deployment (Full Stack)

From the parent directory:

```bash
# Copy environment template
cp .env.example .env

# Edit .env with production values
vim .env

# Build and start all services
docker-compose up -d --build

# View logs
docker-compose logs -f

# Check service health
docker-compose ps
```

Services will be available at:
- Frontend: `http://localhost:80`
- Backend API: `http://localhost:8080`
- Database: `localhost:5432`

### Docker Deployment (Backend Only)

```bash
# Build image
docker build -t castify-backend:latest .

# Run container
docker run -d \
  -p 8080:8080 \
  --env-file .env \
  --name castify-backend \
  castify-backend:latest
```

### Health Checks

The application includes health check endpoints:

```bash
# Basic health
curl http://localhost:8080/actuator/health

# Detailed health (requires auth in production)
curl http://localhost:8080/actuator/health/readiness
```

Docker health checks are configured in:
- `Dockerfile` for standalone container
- `docker-compose.yml` for orchestrated deployment

### Production Checklist

Before deploying to production:

- [ ] Generate secure `JWT_SECRET` (min 256 bits)
- [ ] Set strong `DB_PASSWORD` (min 20 characters)
- [ ] Configure `ALLOWED_ORIGINS` to your actual domain(s)
- [ ] Set `SPRING_PROFILES_ACTIVE=production`
- [ ] Use secrets manager (AWS Secrets Manager, HashiCorp Vault)
- [ ] Enable HTTPS (reverse proxy with nginx or CloudFlare)
- [ ] Configure monitoring (Prometheus, Grafana, DataDog)
- [ ] Set up logging aggregation (ELK stack, CloudWatch)
- [ ] Configure database backups
- [ ] Set up CDN for media files
- [ ] Implement rate limiting
- [ ] Add email service integration
- [ ] Configure SMTP for notifications

## Troubleshooting

### Maven Wrapper Issues

**Problem**: `./mvnw: ./.mvn/wrapper/maven-wrapper.properties: No such file or directory`

**Solution**: Maven wrapper was missing. This is now fixed in the repository.

### Build Fails in CI but Works Locally

**Problem**: Compilation errors in CI about "Unresolved reference 'media'"

**Root Cause**: The `.gitignore` file had `media/` which ignored the `domain/media/` source code directory.

**Solution**: Updated `.gitignore` to `/media/` (only ignore at root level) and committed media source files.

**Lesson**: Always verify that source code directories are tracked in git:

```bash
git ls-files | grep "MediaAsset"
```

### Database Connection Issues

```bash
# Check if PostgreSQL is running
docker-compose ps

# View PostgreSQL logs
docker-compose logs postgres

# Restart PostgreSQL
docker-compose restart postgres

# Connect to database
docker exec -it castify-postgres psql -U castify -d castify
```

### Port Already in Use

If port 8080 is occupied:

```bash
# Check what's using the port
lsof -i :8080

# Change port in application.yml or use environment variable
SERVER_PORT=8081 ./mvnw spring-boot:run
```

### Flyway Migration Errors

```bash
# Check migration status
./mvnw flyway:info

# View migration history in database
docker exec -it castify-postgres psql -U castify -d castify
SELECT * FROM flyway_schema_history;

# Repair failed migration (development only!)
./mvnw flyway:repair

# Clean and restart (WARNING: deletes all data)
./mvnw flyway:clean
./mvnw flyway:migrate
```

### Docker Build Issues

```bash
# Clear Docker cache
docker system prune -a

# Rebuild without cache
docker-compose build --no-cache

# Check Docker logs
docker-compose logs -f backend
```

### Out of Memory in Docker

Increase Java heap size:

```yaml
# docker-compose.yml
environment:
  JAVA_OPTS: "-Xms1024m -Xmx2048m"
```

### CORS Errors

Check that `ALLOWED_ORIGINS` includes your frontend URL:

```bash
# Local development
ALLOWED_ORIGINS=http://localhost:3000,http://localhost:5173

# Production
ALLOWED_ORIGINS=https://yourdomain.com,https://app.yourdomain.com
```

### Logs Directory Permissions

The `.claude/` directory is for Claude Code IDE settings (local only, gitignored).

The `logs/` directories contain application logs and should be gitignored:

```bash
# Remove logs from git if accidentally committed
git rm -r --cached logs/
```

## Contributing

### Development Workflow

1. Create a feature branch: `git checkout -b feat/your-feature`
2. Make changes following the architecture patterns
3. Add tests for new functionality
4. Run tests locally: `./mvnw test`
5. Build to verify: `./mvnw clean install`
6. Update README if adding new features/endpoints
7. Commit with conventional commits: `feat: add rating system`
8. Push and create a Pull Request

### Code Standards

- **Architecture**: Follow Clean Architecture principles
- **Module Boundaries**: Domain → Use Case → Persistence/Web → App
- **Testing**: Minimum 80% coverage for business logic
- **Naming**: Use Kotlin conventions (camelCase, PascalCase)
- **Documentation**: Add KDoc for public APIs
- **Commits**: Use conventional commits (feat, fix, docs, refactor, test)

### Adding a New Feature

Example: Adding a new entity and API

1. **Domain Layer** (`castify-domain`):
   ```kotlin
   // com.castify.domain.yourfeature.YourEntity.kt
   data class YourEntity(...)

   // com.castify.domain.yourfeature.YourEntityRepository.kt
   interface YourEntityRepository { ... }
   ```

2. **Use Case Layer** (`castify-usecase`):
   ```kotlin
   // com.castify.usecase.yourfeature.YourUseCase.kt
   @Service
   class YourUseCase(private val repo: YourEntityRepository) { ... }

   // com.castify.usecase.dto.YourDtos.kt
   data class YourRequest(...)
   data class YourResponse(...)
   ```

3. **Persistence Layer** (`castify-persistence`):
   ```kotlin
   // com.castify.persistence.jpa.YourEntityJpaRepository.kt
   interface YourEntityJpaRepository : JpaRepository<...>, YourEntityRepository
   ```

4. **Web Layer** (`castify-web`):
   ```kotlin
   // com.castify.web.controller.YourController.kt
   @RestController
   @RequestMapping("/api/your-feature")
   class YourController(private val useCase: YourUseCase) { ... }
   ```

5. **Add Tests** for domain, use case, and optionally controller

6. **Update README** with new API endpoints

## License

This project is created for educational and demonstration purposes.

## Contact

For questions or support, please open an issue in the repository.

---

**Built with ❤️ using Clean Architecture, Spring Boot, and Kotlin**

# Castify - Talent Casting Marketplace

A production-ready Spring Boot backend for a talent casting marketplace where clients can search for actors/talents for marketing videos and photos, and message them directly.

## Tech Stack

- **Language**: Kotlin
- **Framework**: Spring Boot 4.0.0
- **Architecture**: Clean Architecture (Hexagonal/Ports & Adapters)
- **Database**: PostgreSQL 17
- **Migration**: Flyway
- **Security**: JWT (Spring Security)
- **API Documentation**: OpenAPI/Swagger (springdoc-openapi)
- **Build Tool**: Maven
- **Testing**: JUnit 5, MockK

## Architecture

This project follows Clean Architecture principles with clear separation of concerns:

```
src/main/kotlin/com/castify/
├── domain/                    # Domain Layer (Entities, Value Objects)
│   └── model/                 # JPA Entities
├── application/               # Application Layer
│   ├── dto/                   # Request/Response DTOs
│   ├── port/                  # Port Interfaces
│   │   ├── incoming/          # Use Case interfaces (not used in this implementation)
│   │   └── outgoing/          # Repository interfaces
│   └── usecase/               # Use Cases (Business Logic)
├── adapters/                  # Adapters Layer
│   ├── in/                    # Inbound Adapters
│   │   └── web/               # REST Controllers
│   └── out/                   # Outbound Adapters
│       └── persistence/       # JPA Repository Implementations
└── infrastructure/            # Infrastructure Layer
    ├── security/              # Security Configuration, JWT
    └── exception/             # Global Exception Handling
```

## Features

### Authentication
- User registration (TALENT/CLIENT/ADMIN roles)
- JWT-based authentication
- Password encryption with BCrypt

### Talent Profile Management
- Create/update talent profiles
- Location-based search
- Languages, tags, and capabilities
- Social media links integration
- Portfolio management (photos, videos, etc.)

### Client Organization
- Create/update client organizations
- Support for AGENCY, BRAND, INDIVIDUAL types

### Search & Discovery
- Advanced talent search with multiple filters:
  - Text search by name
  - Location-based (lat/lon/radius) using Haversine formula
  - Language proficiency
  - Tags
  - Content types (Instagram Reel, TikTok, YouTube, etc.)
  - Location modes (Remote, On-site, Hybrid)
- Pagination support

### Messaging
- One-to-one conversations between clients and talents
- Real-time message history
- Automatic conversation creation

## Prerequisites

- Java 21 or higher
- Docker & Docker Compose
- Maven 3.6+

## Getting Started

### 1. Clone the Repository

```bash
cd /Users/lazarmarkovic/Documents/MyProjects/castify
```

### 2. Start PostgreSQL with Docker Compose

```bash
docker-compose up -d
```

This will start a PostgreSQL 17 instance on `localhost:5432` with:
- Database: `castify`
- Username: `castify`
- Password: `castify123`

### 3. Build the Application

```bash
./mvnw clean install
```

### 4. Run the Application

```bash
./mvnw spring-boot:run
```

The application will start on `http://localhost:8080`.

### 5. Access Swagger UI

Open your browser and navigate to:

```
http://localhost:8080/swagger-ui.html
```

You can also access the OpenAPI JSON at:

```
http://localhost:8080/api-docs
```

## Database Schema

The application uses Flyway for database migrations. The schema includes:

- **app_user**: User accounts with roles
- **client_org**: Client organization details
- **talent_profile**: Talent profile information
- **language**: Supported languages (pre-seeded)
- **talent_language**: Talent language proficiency
- **tag**: Searchable tags
- **talent_tag**: Tags associated with talents
- **talent_capability**: Content types and location modes
- **social_platform**: Social media platforms (pre-seeded)
- **talent_social_link**: Talent social media profiles
- **media_asset**: Media files (photos, videos, etc.)
- **talent_portfolio_item**: Portfolio items for talents
- **conversation**: Conversations between clients and talents
- **message**: Messages within conversations

## API Endpoints

### Authentication

- `POST /api/auth/register` - Register a new user
- `POST /api/auth/login` - Login and get JWT token
- `GET /api/me` - Get current user info (authenticated)

### Client Organization

- `POST /api/client/org` - Create/update organization (CLIENT role)
- `GET /api/client/org` - Get organization (CLIENT role)

### Talent Profile

- `POST /api/talents/me/profile` - Create/update profile (TALENT role)
- `GET /api/talents/me/profile` - Get my profile (TALENT role)
- `POST /api/talents/me/location` - Update location (TALENT role)
- `POST /api/talents/me/languages` - Update languages (TALENT role)
- `POST /api/talents/me/tags` - Update tags (TALENT role)
- `POST /api/talents/me/capabilities` - Update capabilities (TALENT role)
- `POST /api/talents/me/social-links` - Update social links (TALENT role)

### Portfolio Management

- `POST /api/talents/me/media` - Create media asset (TALENT role)
- `POST /api/talents/me/portfolio` - Add media to portfolio (TALENT role)
- `DELETE /api/talents/me/portfolio/{mediaId}` - Remove from portfolio (TALENT role)

### Public Talent Browsing

- `GET /api/talents` - Search talents with filters (public)
  - Query params: `q`, `countryCode`, `city`, `lat`, `lon`, `radiusKm`, `languageCodes`, `tagNames`, `contents`, `locationModes`, `visibleOnly`, `page`, `size`
- `GET /api/talents/{talentId}` - Get talent profile details (public)

### Messaging

- `POST /api/conversations` - Create conversation (CLIENT role)
- `GET /api/conversations` - List my conversations (authenticated)
- `GET /api/conversations/{id}/messages` - Get messages (authenticated)
- `POST /api/conversations/{id}/messages` - Send message (authenticated)

## Configuration

Key configuration options in `src/main/resources/application.yml`:

```yaml
spring:
  datasource:
    url: jdbc:postgresql://localhost:5432/castify
    username: castify
    password: castify123

app:
  jwt:
    secret: ${JWT_SECRET:your-secret-key}  # Change in production!
    expiration-ms: 86400000  # 24 hours
```

### Environment Variables

- `JWT_SECRET`: JWT signing secret (required in production)
- `ALLOWED_ORIGINS`: CORS allowed origins (default: localhost:3000, localhost:4200)

## Running Tests

```bash
./mvnw test
```

Test coverage includes:
- Authentication use cases (register, login)
- Talent profile creation and updates
- Conversation creation and messaging
- Repository unit tests with MockK

## Geo-Location Search

The application implements location-based search using:

1. **Bounding Box Filter**: Reduces candidate set using lat/lon bounds
2. **Haversine Formula**: Calculates precise distance in kilometers
3. **No PostGIS Required**: Pure SQL/Kotlin implementation

Example search request:

```bash
curl "http://localhost:8080/api/talents?lat=40.7128&lon=-74.0060&radiusKm=50&page=0&size=20"
```

## Security

- JWT tokens expire after 24 hours (configurable)
- Passwords are hashed with BCrypt
- Role-based access control (TALENT, CLIENT, ADMIN)
- CORS configuration for frontend integration
- Stateless session management

## Business Rules

- Only TALENT users can create/update talent profiles
- Only CLIENT users can create organizations
- Only CLIENT users can initiate conversations
- Conversations are unique per client-talent pair
- Only visible talents appear in search results
- Portfolio media must belong to the talent owner

## Production Considerations

1. **Change JWT Secret**: Set a strong `JWT_SECRET` environment variable
2. **Database Credentials**: Use secure credentials in production
3. **CORS Configuration**: Update `allowed-origins` for your frontend domain
4. **Logging**: Configure appropriate log levels
5. **Monitoring**: Add actuator endpoints and monitoring tools
6. **Rate Limiting**: Consider adding rate limiting for public endpoints
7. **File Upload**: Integrate with S3 or similar for actual file storage
8. **Email Service**: Add email verification and notifications
9. **Pagination**: Fine-tune page sizes based on performance testing

## Troubleshooting

### Database Connection Issues

If you encounter database connection errors:

```bash
# Check if PostgreSQL is running
docker-compose ps

# View PostgreSQL logs
docker-compose logs postgres

# Restart PostgreSQL
docker-compose restart postgres
```

### Port Already in Use

If port 8080 is already in use, change it in `application.yml`:

```yaml
server:
  port: 8081
```

### Flyway Migration Errors

If Flyway migrations fail:

```bash
# Connect to PostgreSQL
docker exec -it castify-postgres psql -U castify -d castify

# Check migration history
SELECT * FROM flyway_schema_history;

# Drop and recreate database if needed (development only!)
DROP DATABASE castify;
CREATE DATABASE castify;
```

## Contributing

1. Follow the existing Clean Architecture structure
2. Add tests for new features
3. Update this README for new endpoints or features
4. Use conventional commit messages

## License

This project is created for educational and demonstration purposes.

## Contact

For questions or support, please open an issue in the repository.

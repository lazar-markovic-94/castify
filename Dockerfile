# =============================================================================
# Castify Backend - Production Dockerfile
# Multi-stage build for optimized image size
# =============================================================================

# -----------------------------------------------------------------------------
# Stage 1: Build Stage
# -----------------------------------------------------------------------------
FROM eclipse-temurin:21-jdk-alpine AS builder

# Set working directory
WORKDIR /build

# Copy Maven wrapper and pom files first (for better caching)
COPY .mvn/ .mvn/
COPY mvnw .
COPY pom.xml .

# Copy all module pom.xml files
COPY castify-domain/pom.xml castify-domain/
COPY castify-usecase/pom.xml castify-usecase/
COPY castify-persistence/pom.xml castify-persistence/
COPY castify-web/pom.xml castify-web/
COPY castify-app/pom.xml castify-app/

# Download dependencies (cached if pom.xml doesn't change)
RUN ./mvnw dependency:go-offline -B

# Copy source code
COPY castify-domain/src castify-domain/src
COPY castify-usecase/src castify-usecase/src
COPY castify-persistence/src castify-persistence/src
COPY castify-web/src castify-web/src
COPY castify-app/src castify-app/src

# Build the application (skip tests for faster builds)
# To run tests: remove -DskipTests
RUN ./mvnw clean package -DskipTests -B

# Verify the JAR was created
RUN ls -lh castify-app/target/*.jar

# -----------------------------------------------------------------------------
# Stage 2: Runtime Stage
# -----------------------------------------------------------------------------
FROM eclipse-temurin:21-jre-alpine AS runtime

# Install necessary utilities
RUN apk add --no-cache \
    curl \
    bash \
    tzdata

# Set timezone to UTC
ENV TZ=UTC

# Create application user (security best practice - don't run as root)
RUN addgroup -g 1000 castify && \
    adduser -D -u 1000 -G castify castify

# Set working directory
WORKDIR /app

# Create directories for uploads and logs
RUN mkdir -p /app/uploads /app/logs && \
    chown -R castify:castify /app

# Copy the JAR from builder stage
COPY --from=builder /build/castify-app/target/*.jar app.jar

# Verify JAR exists and check size
RUN ls -lh app.jar && \
    echo "JAR size: $(du -h app.jar | cut -f1)"

# Change ownership to application user
RUN chown castify:castify app.jar

# Switch to non-root user
USER castify

# Expose port
EXPOSE 8080

# Health check
HEALTHCHECK --interval=30s --timeout=3s --start-period=60s --retries=3 \
    CMD curl -f http://localhost:8080/actuator/health || exit 1

# Environment variables with defaults
ENV SPRING_PROFILES_ACTIVE=production \
    SERVER_PORT=8080 \
    JAVA_OPTS="-Xms512m -Xmx1024m -XX:+UseG1GC -XX:MaxGCPauseMillis=200"

# Run the application
ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -Djava.security.egd=file:/dev/./urandom -jar app.jar"]

# =============================================================================
# Build instructions:
#
# Development build:
#   docker build -t castify-backend:dev .
#
# Production build with version tag:
#   docker build -t castify-backend:1.0.0 -t castify-backend:latest .
#
# Run container:
#   docker run -d \
#     -p 8080:8080 \
#     -e SPRING_PROFILES_ACTIVE=production \
#     -e DB_URL=jdbc:postgresql://db:5432/castify \
#     -e DB_USERNAME=castify \
#     -e DB_PASSWORD=secret \
#     -e JWT_SECRET=your-secret-key \
#     -e ALLOWED_ORIGINS=https://castify.com \
#     --name castify-backend \
#     castify-backend:latest
#
# Check logs:
#   docker logs -f castify-backend
#
# Shell access:
#   docker exec -it castify-backend sh
# =============================================================================

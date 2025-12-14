# Castify Multi-Module Refactoring Guide

## Current Issues Identified ✅

1. ❌ **Single module structure** - Should be 5 separate Maven modules
2. ❌ **JPA entities using `data class`** - Should be regular `class` with var properties
3. ❌ **Domain mixed with persistence** - Domain should be pure Kotlin, no JPA

## Target Structure (Prestige Pattern)

```
castify/
├── pom.xml (parent, packaging=pom)
├── castify-domain/
│   ├── pom.xml
│   └── src/main/kotlin/com/castify/domain/
│       ├── user/
│       │   ├── User.kt (data class - pure domain)
│       │   └── UserRepository.kt (interface)
│       ├── talent/
│       │   ├── TalentProfile.kt (data class with business logic)
│       │   └── TalentRepository.kt (interface)
│       ├── client/
│       │   ├── ClientOrg.kt
│       │   └── ClientOrgRepository.kt
│       ├── message/
│       │   ├── Conversation.kt
│       │   ├── Message.kt
│       │   └── MessageRepository.kt
│       ├── media/
│       │   ├── MediaAsset.kt
│       │   └── MediaRepository.kt
│       ├── common/
│       │   ├── UserRole.kt (enum)
│       │   ├── ContentType.kt (enum)
│       │   └── ... (other enums)
│       └── exception/
│           └── DomainExceptions.kt
│
├── castify-usecase/
│   ├── pom.xml (depends on: castify-domain)
│   └── src/main/kotlin/com/castify/usecase/
│       ├── auth/
│       │   ├── RegisterUserUseCase.kt
│       │   ├── LoginUserUseCase.kt
│       │   └── dto/ (request/response)
│       ├── talent/
│       │   ├── CreateTalentProfileUseCase.kt
│       │   ├── UpdateTalentProfileUseCase.kt
│       │   ├── SearchTalentsUseCase.kt
│       │   └── dto/
│       ├── client/
│       │   └── ...
│       └── message/
│           └── ...
│
├── castify-persistence/
│   ├── pom.xml (depends on: castify-domain)
│   └── src/main/kotlin/com/castify/persistence/
│       ├── entity/
│       │   ├── UserEntity.kt (class with JPA annotations)
│       │   ├── TalentProfileEntity.kt
│       │   ├── ClientOrgEntity.kt
│       │   └── ... (all JPA entities)
│       ├── repository/
│       │   ├── UserJpaRepository.kt (Spring Data)
│       │   ├── TalentJpaRepository.kt
│       │   └── ...
│       └── adapter/
│           ├── UserRepositoryAdapter.kt (implements domain UserRepository)
│           ├── TalentRepositoryAdapter.kt
│           └── ...
│
├── castify-web/
│   ├── pom.xml (depends on: castify-usecase)
│   └── src/main/kotlin/com/castify/web/
│       ├── controller/
│       │   ├── AuthController.kt
│       │   ├── TalentController.kt
│       │   └── ...
│       ├── security/
│       │   ├── JwtService.kt
│       │   ├── SecurityConfig.kt
│       │   └── SecurityUtils.kt
│       └── exception/
│           └── GlobalExceptionHandler.kt
│
└── castify-app/
    ├── pom.xml (depends on: web, persistence)
    └── src/main/kotlin/com/castify/
        ├── CastifyApplication.kt
        └── resources/
            ├── application.yml
            └── db/migration/
                └── V1__init.sql
```

---

## Module Dependency Flow

```
castify-domain          (no dependencies - pure Kotlin)
    ↑
    ├── castify-usecase      (domain)
    └── castify-persistence  (domain)
            ↑
            └── castify-web        (usecase)
                    ↑
                    └── castify-app   (web, persistence)
```

---

## Step-by-Step Refactoring

### 1. Create Root POM (✅ Already Done)

See `/Users/lazarmarkovic/Documents/MyProjects/castify/pom.xml`

### 2. Create castify-domain Module

**castify-domain/pom.xml:**
```xml
<?xml version="1.0" encoding="UTF-8"?>
<project xmlns="http://maven.apache.org/POM/4.0.0"
         xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
         xsi:schemaLocation="http://maven.apache.org/POM/4.0.0
         http://maven.apache.org/xsd/maven-4.0.0.xsd">
    <modelVersion>4.0.0</modelVersion>

    <parent>
        <groupId>com.castify</groupId>
        <artifactId>castify</artifactId>
        <version>0.0.1-SNAPSHOT</version>
    </parent>

    <artifactId>castify-domain</artifactId>
    <name>Castify Domain</name>
    <description>Pure domain model and repository interfaces</description>

    <dependencies>
        <dependency>
            <groupId>org.jetbrains.kotlin</groupId>
            <artifactId>kotlin-reflect</artifactId>
        </dependency>
        <dependency>
            <groupId>org.jetbrains.kotlin</groupId>
            <artifactId>kotlin-stdlib</artifactId>
        </dependency>
    </dependencies>
</project>
```

**Example Domain Object (Pure Kotlin, data class):**

`castify-domain/src/main/kotlin/com/castify/domain/user/User.kt`:
```kotlin
package com.castify.domain.user

import com.castify.domain.common.UserRole
import java.time.LocalDateTime
import java.util.*

data class User(
    val userId: UUID,
    val email: String,
    val role: UserRole,
    val passwordHash: String,
    val phone: String? = null,
    val isActive: Boolean = true,
    val createdAt: LocalDateTime,
    val updatedAt: LocalDateTime
) {
    init {
        require(email.isNotBlank() && email.contains("@")) { "Invalid email format" }
        require(passwordHash.isNotBlank()) { "Password hash cannot be blank" }
    }

    fun isClient(): Boolean = role == UserRole.CLIENT
    fun isTalent(): Boolean = role == UserRole.TALENT
    fun isAdmin(): Boolean = role == UserRole.ADMIN

    fun deactivate(): User = copy(isActive = false, updatedAt = LocalDateTime.now())
    fun activate(): User = copy(isActive = true, updatedAt = LocalDateTime.now())
}
```

**Repository Interface (in domain):**

`castify-domain/src/main/kotlin/com/castify/domain/user/UserRepository.kt`:
```kotlin
package com.castify.domain.user

import java.util.*

interface UserRepository {
    fun save(user: User): User
    fun findById(userId: UUID): User?
    fun findByEmail(email: String): User?
    fun existsByEmail(email: String): Boolean
}
```

---

### 3. Create castify-persistence Module

**castify-persistence/pom.xml:**
```xml
<?xml version="1.0" encoding="UTF-8"?>
<project>
    <parent>
        <groupId>com.castify</groupId>
        <artifactId>castify</artifactId>
        <version>0.0.1-SNAPSHOT</version>
    </parent>

    <artifactId>castify-persistence</artifactId>
    <name>Castify Persistence</name>

    <dependencies>
        <dependency>
            <groupId>com.castify</groupId>
            <artifactId>castify-domain</artifactId>
        </dependency>
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-data-jpa</artifactId>
        </dependency>
        <dependency>
            <groupId>org.postgresql</groupId>
            <artifactId>postgresql</artifactId>
        </dependency>
        <dependency>
            <groupId>org.flywaydb</groupId>
            <artifactId>flyway-core</artifactId>
        </dependency>
        <dependency>
            <groupId>org.flywaydb</groupId>
            <artifactId>flyway-database-postgresql</artifactId>
        </dependency>
    </dependencies>
</project>
```

**JPA Entity (regular class, NOT data class):**

`castify-persistence/src/main/kotlin/com/castify/persistence/entity/UserEntity.kt`:
```kotlin
package com.castify.persistence.entity

import com.castify.domain.common.UserRole
import com.castify.domain.user.User
import jakarta.persistence.*
import java.time.LocalDateTime
import java.util.*

@Entity
@Table(name = "app_user")
class UserEntity(
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "user_id")
    var userId: UUID? = null,

    @Column(nullable = false, unique = true, columnDefinition = "citext")
    var email: String,

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 16)
    var role: UserRole,

    @Column(name = "password_hash", nullable = false, length = 255)
    var passwordHash: String,

    @Column(length = 32)
    var phone: String? = null,

    @Column(name = "is_active", nullable = false)
    var isActive: Boolean = true,

    @Column(name = "created_at", nullable = false, updatable = false)
    var createdAt: LocalDateTime = LocalDateTime.now(),

    @Column(name = "updated_at", nullable = false)
    var updatedAt: LocalDateTime = LocalDateTime.now()
) {
    /**
     * Convert JPA entity to pure domain object
     */
    fun toDomain(): User = User(
        userId = requireNotNull(userId) { "User ID cannot be null" },
        email = email,
        role = role,
        passwordHash = passwordHash,
        phone = phone,
        isActive = isActive,
        createdAt = createdAt,
        updatedAt = updatedAt
    )

    companion object {
        /**
         * Convert domain object to JPA entity
         */
        fun fromDomain(user: User): UserEntity = UserEntity(
            userId = user.userId,
            email = user.email,
            role = user.role,
            passwordHash = user.passwordHash,
            phone = user.phone,
            isActive = user.isActive,
            createdAt = user.createdAt,
            updatedAt = user.updatedAt
        )
    }
}
```

**Spring Data JPA Repository:**

`castify-persistence/src/main/kotlin/com/castify/persistence/repository/UserJpaRepository.kt`:
```kotlin
package com.castify.persistence.repository

import com.castify.persistence.entity.UserEntity
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository
import java.util.*

@Repository
interface UserJpaRepository : JpaRepository<UserEntity, UUID> {
    fun findByEmail(email: String): UserEntity?
    fun existsByEmail(email: String): Boolean
}
```

**Repository Adapter (implements domain interface):**

`castify-persistence/src/main/kotlin/com/castify/persistence/adapter/UserRepositoryAdapter.kt`:
```kotlin
package com.castify.persistence.adapter

import com.castify.domain.user.User
import com.castify.domain.user.UserRepository
import com.castify.persistence.entity.UserEntity
import com.castify.persistence.repository.UserJpaRepository
import org.springframework.stereotype.Component
import java.util.*

@Component
class UserRepositoryAdapter(
    private val jpaRepository: UserJpaRepository
) : UserRepository {

    override fun save(user: User): User {
        val entity = UserEntity.fromDomain(user)
        val savedEntity = jpaRepository.save(entity)
        return savedEntity.toDomain()
    }

    override fun findById(userId: UUID): User? {
        return jpaRepository.findById(userId)
            .map { it.toDomain() }
            .orElse(null)
    }

    override fun findByEmail(email: String): User? {
        return jpaRepository.findByEmail(email)?.toDomain()
    }

    override fun existsByEmail(email: String): Boolean {
        return jpaRepository.existsByEmail(email)
    }
}
```

---

### 4. Create castify-usecase Module

**castify-usecase/pom.xml:**
```xml
<?xml version="1.0" encoding="UTF-8"?>
<project>
    <parent>
        <groupId>com.castify</groupId>
        <artifactId>castify</artifactId>
        <version>0.0.1-SNAPSHOT</version>
    </parent>

    <artifactId>castify-usecase</artifactId>
    <name>Castify Use Cases</name>

    <dependencies>
        <dependency>
            <groupId>com.castify</groupId>
            <artifactId>castify-domain</artifactId>
        </dependency>
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-validation</artifactId>
        </dependency>
        <dependency>
            <groupId>org.springframework</groupId>
            <artifactId>spring-context</artifactId>
        </dependency>
    </dependencies>
</project>
```

**Use Case:**

`castify-usecase/src/main/kotlin/com/castify/usecase/auth/RegisterUserUseCase.kt`:
```kotlin
package com.castify.usecase.auth

import com.castify.domain.exception.ConflictException
import com.castify.domain.user.User
import com.castify.domain.user.UserRepository
import com.castify.usecase.auth.dto.RegisterRequest
import com.castify.usecase.auth.dto.AuthResponse
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.stereotype.Service
import java.time.LocalDateTime
import java.util.*

@Service
class RegisterUserUseCase(
    private val userRepository: UserRepository,
    private val passwordEncoder: PasswordEncoder,
    private val tokenService: TokenService
) {
    fun execute(request: RegisterRequest): AuthResponse {
        val email = requireNotNull(request.email) { "Email is required" }
        val password = requireNotNull(request.password) { "Password is required" }
        val role = requireNotNull(request.role) { "Role is required" }

        if (userRepository.existsByEmail(email)) {
            throw ConflictException("Email already registered")
        }

        val now = LocalDateTime.now()
        val user = User(
            userId = UUID.randomUUID(),
            email = email,
            role = role,
            passwordHash = passwordEncoder.encode(password)!!,
            phone = request.phone,
            isActive = true,
            createdAt = now,
            updatedAt = now
        )

        val savedUser = userRepository.save(user)
        val token = tokenService.generateToken(savedUser.userId, savedUser.email, savedUser.role.name)

        return AuthResponse(
            token = token,
            userId = savedUser.userId,
            email = savedUser.email,
            role = savedUser.role
        )
    }
}
```

---

### 5. Create castify-web Module

**castify-web/pom.xml:**
```xml
<?xml version="1.0" encoding="UTF-8"?>
<project>
    <parent>
        <groupId>com.castify</groupId>
        <artifactId>castify</artifactId>
        <version>0.0.1-SNAPSHOT</version>
    </parent>

    <artifactId>castify-web</artifactId>
    <name>Castify Web</name>

    <dependencies>
        <dependency>
            <groupId>com.castify</groupId>
            <artifactId>castify-usecase</artifactId>
        </dependency>
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-web</artifactId>
        </dependency>
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-security</artifactId>
        </dependency>
        <dependency>
            <groupId>io.jsonwebtoken</groupId>
            <artifactId>jjwt-api</artifactId>
        </dependency>
        <dependency>
            <groupId>org.springdoc</groupId>
            <artifactId>springdoc-openapi-starter-webmvc-ui</artifactId>
        </dependency>
    </dependencies>
</project>
```

---

### 6. Create castify-app Module

**castify-app/pom.xml:**
```xml
<?xml version="1.0" encoding="UTF-8"?>
<project>
    <parent>
        <groupId>com.castify</groupId>
        <artifactId>castify</artifactId>
        <version>0.0.1-SNAPSHOT</version>
    </parent>

    <artifactId>castify-app</artifactId>
    <name>Castify Application</name>

    <dependencies>
        <dependency>
            <groupId>com.castify</groupId>
            <artifactId>castify-web</artifactId>
        </dependency>
        <dependency>
            <groupId>com.castify</groupId>
            <artifactId>castify-persistence</artifactId>
        </dependency>
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter</artifactId>
        </dependency>
    </dependencies>

    <build>
        <plugins>
            <plugin>
                <groupId>org.springframework.boot</groupId>
                <artifactId>spring-boot-maven-plugin</artifactId>
            </plugin>
        </plugins>
    </build>
</project>
```

**Application Main Class:**

`castify-app/src/main/kotlin/com/castify/CastifyApplication.kt`:
```kotlin
package com.castify

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication

@SpringBootApplication(scanBasePackages = ["com.castify"])
class CastifyApplication

fun main(args: Array<String>) {
    runApplication<CastifyApplication>(*args)
}
```

---

## Key Differences from Current Implementation

### 1. **Domain Objects (data class vs class)**

**❌ Current (Wrong):**
```kotlin
@Entity
@Table(name = "app_user")
data class AppUser(  // ❌ data class with JPA
    @Id
    val userId: UUID? = null
)
```

**✅ Correct:**

**Domain:**
```kotlin
// Pure domain - data class, no JPA
data class User(
    val userId: UUID,
    val email: String
) {
    fun deactivate() = copy(isActive = false)
}
```

**Persistence:**
```kotlin
// JPA Entity - regular class, var properties
@Entity
class UserEntity(
    @Id
    var userId: UUID? = null,
    var email: String
) {
    fun toDomain() = User(...)
}
```

### 2. **Repository Pattern**

**✅ Domain defines interface:**
```kotlin
// castify-domain
interface UserRepository {
    fun save(user: User): User
}
```

**✅ Persistence implements:**
```kotlin
// castify-persistence
@Component
class UserRepositoryAdapter : UserRepository {
    override fun save(user: User): User {
        val entity = UserEntity.fromDomain(user)
        return jpaRepository.save(entity).toDomain()
    }
}
```

### 3. **Use Cases depend on domain, not persistence**

```kotlin
// castify-usecase
class RegisterUserUseCase(
    private val userRepository: UserRepository  // domain interface!
)
```

---

## Benefits of This Structure

1. **Domain Purity** ✅
   - Domain has no framework dependencies
   - Business logic in immutable data classes
   - Easy to test (no mocks needed for domain logic)

2. **Clear Module Boundaries** ✅
   - Each module has single responsibility
   - Dependencies flow inward (domain at center)
   - Can't accidentally violate architecture

3. **Flexibility** ✅
   - Swap JPA for MongoDB: only change persistence module
   - Add GraphQL: create new web module
   - Use domain in CLI tool: no web/persistence needed

4. **Testability** ✅
   - Test domain logic without Spring
   - Test use cases with mocked repositories
   - Test web with mocked use cases

---

## Migration Checklist

- [ ] Create 5 module directories
- [ ] Create pom.xml for each module
- [ ] Move enums and exceptions to castify-domain/common
- [ ] Create pure domain objects (data classes) from current entities
- [ ] Create repository interfaces in domain
- [ ] Create JPA entities (regular classes) in persistence
- [ ] Implement repository adapters in persistence
- [ ] Move use cases to castify-usecase
- [ ] Move controllers to castify-web
- [ ] Move security to castify-web
- [ ] Create castify-app with main class
- [ ] Move application.yml and Flyway migrations to castify-app/resources
- [ ] Test compilation: `mvn clean install`
- [ ] Run application: `mvn spring-boot:run -pl castify-app`

---

## Compilation Commands

```bash
# From root directory
mvn clean install          # Build all modules
mvn clean install -DskipTests  # Build without tests

# Run specific module
cd castify-app
mvn spring-boot:run

# Or from root
mvn spring-boot:run -pl castify-app
```

---

## Final Project Structure

```
castify/
├── pom.xml (parent)
├── castify-domain/ (21 files - pure Kotlin)
├── castify-usecase/ (16 files - Spring services)
├── castify-persistence/ (15 files - JPA entities + adapters)
├── castify-web/ (10 files - controllers + security)
└── castify-app/ (1 file - main + resources)
```

**Total**: Same 52+ files, but properly organized into modules with clear boundaries!

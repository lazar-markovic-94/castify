package com.castify.persistence.adapter

import com.castify.domain.user.User
import com.castify.domain.user.UserRole
import com.castify.persistence.entity.UserEntity
import com.castify.persistence.jpa.UserJpaRepository
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.security.crypto.password.PasswordEncoder
import java.time.Instant
import java.util.*

class UserRepositoryAdapterTest {

    private lateinit var userJpaRepository: UserJpaRepository
    private lateinit var passwordEncoder: PasswordEncoder
    private lateinit var userRepositoryAdapter: UserRepositoryAdapter

    @BeforeEach
    fun setUp() {
        userJpaRepository = mockk()
        passwordEncoder = mockk()
        userRepositoryAdapter = UserRepositoryAdapter(userJpaRepository, passwordEncoder)
    }

    @Test
    fun `should successfully save new user`() {
        // Given
        val userId = UUID.randomUUID()
        val email = "test@example.com"
        val hashedPassword = "hashedPassword123"

        val user = User(
            userId = userId,
            email = email,
            role = UserRole.TALENT,
            phone = "+1234567890",
            isActive = true,
            createdAt = Instant.now(),
            updatedAt = Instant.now()
        )

        val entity = UserEntity(
            userId = userId,
            email = email,
            role = UserRole.TALENT,
            passwordHash = hashedPassword,
            phone = "+1234567890",
            isActive = true,
            createdAt = user.createdAt,
            updatedAt = user.updatedAt
        )

        every { passwordEncoder.encode("temporary") } returns hashedPassword
        every { userJpaRepository.save(any()) } returns entity

        // When
        val result = userRepositoryAdapter.save(user)

        // Then
        assertNotNull(result)
        assertEquals(userId, result.userId)
        assertEquals(email, result.email)
        assertEquals(UserRole.TALENT, result.role)

        verify(exactly = 1) { userJpaRepository.save(any()) }
    }

    @Test
    fun `should successfully find user by ID`() {
        // Given
        val userId = UUID.randomUUID()
        val email = "found@example.com"

        val entity = UserEntity(
            userId = userId,
            email = email,
            role = UserRole.CLIENT,
            passwordHash = "hashedPassword",
            phone = null,
            isActive = true,
            createdAt = Instant.now(),
            updatedAt = Instant.now()
        )

        every { userJpaRepository.findById(userId) } returns Optional.of(entity)

        // When
        val result = userRepositoryAdapter.findById(userId)

        // Then
        assertNotNull(result)
        assertEquals(userId, result!!.userId)
        assertEquals(email, result.email)
        assertEquals(UserRole.CLIENT, result.role)

        verify(exactly = 1) { userJpaRepository.findById(userId) }
    }

    @Test
    fun `should return null when user not found by ID`() {
        // Given
        val userId = UUID.randomUUID()

        every { userJpaRepository.findById(userId) } returns Optional.empty()

        // When
        val result = userRepositoryAdapter.findById(userId)

        // Then
        assertNull(result)
        verify(exactly = 1) { userJpaRepository.findById(userId) }
    }

    @Test
    fun `should successfully find user by email`() {
        // Given
        val email = "email@example.com"
        val userId = UUID.randomUUID()

        val entity = UserEntity(
            userId = userId,
            email = email,
            role = UserRole.ADMIN,
            passwordHash = "hashedPassword",
            phone = "+9999999999",
            isActive = true,
            createdAt = Instant.now(),
            updatedAt = Instant.now()
        )

        every { userJpaRepository.findByEmail(email) } returns entity

        // When
        val result = userRepositoryAdapter.findByEmail(email)

        // Then
        assertNotNull(result)
        assertEquals(email, result!!.email)
        assertEquals(UserRole.ADMIN, result.role)

        verify(exactly = 1) { userJpaRepository.findByEmail(email) }
    }

    @Test
    fun `should return null when user not found by email`() {
        // Given
        val email = "nonexistent@example.com"

        every { userJpaRepository.findByEmail(email) } returns null

        // When
        val result = userRepositoryAdapter.findByEmail(email)

        // Then
        assertNull(result)
        verify(exactly = 1) { userJpaRepository.findByEmail(email) }
    }

    @Test
    fun `should return true when email exists`() {
        // Given
        val email = "existing@example.com"

        every { userJpaRepository.existsByEmail(email) } returns true

        // When
        val result = userRepositoryAdapter.existsByEmail(email)

        // Then
        assertTrue(result)
        verify(exactly = 1) { userJpaRepository.existsByEmail(email) }
    }

    @Test
    fun `should return false when email does not exist`() {
        // Given
        val email = "nonexistent@example.com"

        every { userJpaRepository.existsByEmail(email) } returns false

        // When
        val result = userRepositoryAdapter.existsByEmail(email)

        // Then
        assertFalse(result)
        verify(exactly = 1) { userJpaRepository.existsByEmail(email) }
    }

    @Test
    fun `should successfully save user with password`() {
        // Given
        val userId = UUID.randomUUID()
        val email = "withpassword@example.com"
        val rawPassword = "plainPassword123"
        val hashedPassword = "hashedPassword123"

        val user = User(
            userId = userId,
            email = email,
            role = UserRole.TALENT,
            phone = null,
            isActive = true,
            createdAt = Instant.now(),
            updatedAt = Instant.now()
        )

        val entity = UserEntity(
            userId = userId,
            email = email,
            role = UserRole.TALENT,
            passwordHash = hashedPassword,
            phone = null,
            isActive = true,
            createdAt = user.createdAt,
            updatedAt = user.updatedAt
        )

        every { passwordEncoder.encode(rawPassword) } returns hashedPassword
        every { userJpaRepository.save(any()) } returns entity

        // When
        val result = userRepositoryAdapter.saveWithPassword(user, rawPassword)

        // Then
        assertNotNull(result)
        assertEquals(userId, result.userId)
        assertEquals(email, result.email)

        verify(exactly = 1) { passwordEncoder.encode(rawPassword) }
        verify(exactly = 1) { userJpaRepository.save(any()) }
    }

    @Test
    fun `should successfully verify correct password`() {
        // Given
        val email = "verify@example.com"
        val rawPassword = "correctPassword"
        val hashedPassword = "hashedCorrectPassword"

        val entity = UserEntity(
            userId = UUID.randomUUID(),
            email = email,
            role = UserRole.TALENT,
            passwordHash = hashedPassword,
            phone = null,
            isActive = true,
            createdAt = Instant.now(),
            updatedAt = Instant.now()
        )

        every { userJpaRepository.findByEmail(email) } returns entity
        every { passwordEncoder.matches(rawPassword, hashedPassword) } returns true

        // When
        val result = userRepositoryAdapter.verifyPassword(email, rawPassword)

        // Then
        assertTrue(result)
        verify(exactly = 1) { userJpaRepository.findByEmail(email) }
        verify(exactly = 1) { passwordEncoder.matches(rawPassword, hashedPassword) }
    }

    @Test
    fun `should return false when verifying incorrect password`() {
        // Given
        val email = "verify@example.com"
        val rawPassword = "wrongPassword"
        val hashedPassword = "hashedCorrectPassword"

        val entity = UserEntity(
            userId = UUID.randomUUID(),
            email = email,
            role = UserRole.TALENT,
            passwordHash = hashedPassword,
            phone = null,
            isActive = true,
            createdAt = Instant.now(),
            updatedAt = Instant.now()
        )

        every { userJpaRepository.findByEmail(email) } returns entity
        every { passwordEncoder.matches(rawPassword, hashedPassword) } returns false

        // When
        val result = userRepositoryAdapter.verifyPassword(email, rawPassword)

        // Then
        assertFalse(result)
        verify(exactly = 1) { passwordEncoder.matches(rawPassword, hashedPassword) }
    }

    @Test
    fun `should return false when user not found for password verification`() {
        // Given
        val email = "nonexistent@example.com"
        val rawPassword = "anyPassword"

        every { userJpaRepository.findByEmail(email) } returns null

        // When
        val result = userRepositoryAdapter.verifyPassword(email, rawPassword)

        // Then
        assertFalse(result)
        verify(exactly = 1) { userJpaRepository.findByEmail(email) }
        verify(exactly = 0) { passwordEncoder.matches(any(), any()) }
    }
}

package com.castify.persistence.adapter

import com.castify.domain.user.User
import com.castify.domain.user.UserRole
import com.castify.persistence.entity.UserEntity
import com.castify.persistence.jpa.UserJpaRepository
import io.mockk.every
import io.mockk.mockk
import io.mockk.slot
import io.mockk.verify
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import java.time.Instant
import java.util.*

class UserRepositoryAdapterTest {

    private lateinit var userJpaRepository: UserJpaRepository
    private lateinit var userRepositoryAdapter: UserRepositoryAdapter

    @BeforeEach
    fun setUp() {
        userJpaRepository = mockk()
        userRepositoryAdapter = UserRepositoryAdapter(userJpaRepository)
    }

    @Test
    fun `should successfully create a new user`() {
        // Was `should successfully save new user`, and it stubbed
        // passwordEncoder.encode("temporary") - asserting the very behaviour
        // that discarded every user's real password. Registration now goes
        // through create() with a caller-supplied hash.
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

        val entitySlot = slot<UserEntity>()
        every { userJpaRepository.save(capture(entitySlot)) } answers { entitySlot.captured }

        val result = userRepositoryAdapter.create(user, hashedPassword)

        assertNotNull(result)
        assertEquals(userId, result.userId)
        assertEquals(email, result.email)
        assertEquals(UserRole.TALENT, result.role)
        assertEquals(hashedPassword, entitySlot.captured.passwordHash)

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
    fun `create stores the supplied password hash`() {
        val userId = UUID.randomUUID()
        val email = "withpassword@example.com"
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

        val entitySlot = slot<UserEntity>()
        every { userJpaRepository.save(capture(entitySlot)) } answers { entitySlot.captured }

        val result = userRepositoryAdapter.create(user, hashedPassword)

        assertEquals(userId, result.userId)
        assertEquals(hashedPassword, entitySlot.captured.passwordHash)
    }

    @Test
    fun `save preserves the stored password hash`() {
        // Regression: save() used to fall back to encoding the literal string
        // "temporary" whenever its in-memory map missed, silently replacing the
        // user's real password on every write.
        val userId = UUID.randomUUID()
        val existingHash = "the-real-hash"

        val user = User(
            userId = userId,
            email = "update@example.com",
            role = UserRole.TALENT,
            phone = null,
            isActive = true,
            createdAt = Instant.now(),
            updatedAt = Instant.now()
        )

        val existing = UserEntity(
            userId = userId,
            email = "update@example.com",
            role = UserRole.TALENT,
            passwordHash = existingHash,
            phone = null,
            isActive = true,
            createdAt = user.createdAt,
            updatedAt = user.updatedAt
        )

        val entitySlot = slot<UserEntity>()
        every { userJpaRepository.findById(userId) } returns Optional.of(existing)
        every { userJpaRepository.save(capture(entitySlot)) } answers { entitySlot.captured }

        userRepositoryAdapter.save(user)

        assertEquals(existingHash, entitySlot.captured.passwordHash)
    }

    @Test
    fun `save refuses to create an unknown user`() {
        val user = User(
            userId = UUID.randomUUID(),
            email = "ghost@example.com",
            role = UserRole.TALENT,
            phone = null,
            isActive = true,
            createdAt = Instant.now(),
            updatedAt = Instant.now()
        )

        every { userJpaRepository.findById(user.userId) } returns Optional.empty()

        assertThrows(IllegalStateException::class.java) {
            userRepositoryAdapter.save(user)
        }
        verify(exactly = 0) { userJpaRepository.save(any()) }
    }

    @Test
    fun `findPasswordHashByEmail returns the stored hash`() {
        val email = "hash@example.com"
        val entity = UserEntity(
            userId = UUID.randomUUID(),
            email = email,
            role = UserRole.CLIENT,
            passwordHash = "stored-hash",
            phone = null,
            isActive = true,
            createdAt = Instant.now(),
            updatedAt = Instant.now()
        )

        every { userJpaRepository.findByEmail(email) } returns entity

        assertEquals("stored-hash", userRepositoryAdapter.findPasswordHashByEmail(email))
    }

    @Test
    fun `findPasswordHashByEmail returns null for an unknown email`() {
        every { userJpaRepository.findByEmail("nobody@example.com") } returns null

        assertNull(userRepositoryAdapter.findPasswordHashByEmail("nobody@example.com"))
    }
}

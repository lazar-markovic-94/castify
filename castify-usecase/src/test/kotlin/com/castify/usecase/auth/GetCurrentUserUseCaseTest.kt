package com.castify.usecase.auth

import com.castify.domain.exception.ResourceNotFoundException
import com.castify.domain.user.User
import com.castify.domain.user.UserRepository
import com.castify.domain.user.UserRole
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import java.time.Instant
import java.util.*

class GetCurrentUserUseCaseTest {

    private lateinit var userRepository: UserRepository
    private lateinit var getCurrentUserUseCase: GetCurrentUserUseCase

    @BeforeEach
    fun setUp() {
        userRepository = mockk()
        getCurrentUserUseCase = GetCurrentUserUseCase(userRepository)
    }

    @Test
    fun `should successfully get current talent user`() {
        // Given
        val userId = UUID.randomUUID()
        val email = "talent@example.com"
        val phone = "+1234567890"

        val user = User(
            userId = userId,
            email = email,
            role = UserRole.TALENT,
            phone = phone,
            isActive = true,
            createdAt = Instant.now(),
            updatedAt = Instant.now()
        )

        every { userRepository.findById(userId) } returns user

        // When
        val result = getCurrentUserUseCase.execute(userId)

        // Then
        assertNotNull(result)
        assertEquals(userId, result.userId)
        assertEquals(email, result.email)
        assertEquals(UserRole.TALENT, result.role)
        assertEquals(phone, result.phone)
        assertTrue(result.isActive)

        verify(exactly = 1) { userRepository.findById(userId) }
    }

    @Test
    fun `should successfully get current client user`() {
        // Given
        val userId = UUID.randomUUID()
        val email = "client@example.com"

        val user = User(
            userId = userId,
            email = email,
            role = UserRole.CLIENT,
            phone = null,
            isActive = true,
            createdAt = Instant.now(),
            updatedAt = Instant.now()
        )

        every { userRepository.findById(userId) } returns user

        // When
        val result = getCurrentUserUseCase.execute(userId)

        // Then
        assertNotNull(result)
        assertEquals(userId, result.userId)
        assertEquals(email, result.email)
        assertEquals(UserRole.CLIENT, result.role)
        assertNull(result.phone)
        assertTrue(result.isActive)
    }

    @Test
    fun `should successfully get inactive user`() {
        // Given
        val userId = UUID.randomUUID()
        val email = "inactive@example.com"

        val user = User(
            userId = userId,
            email = email,
            role = UserRole.TALENT,
            phone = "+1111111111",
            isActive = false,
            createdAt = Instant.now(),
            updatedAt = Instant.now()
        )

        every { userRepository.findById(userId) } returns user

        // When
        val result = getCurrentUserUseCase.execute(userId)

        // Then
        assertNotNull(result)
        assertEquals(userId, result.userId)
        assertFalse(result.isActive)
    }

    @Test
    fun `should throw ResourceNotFoundException when user not found`() {
        // Given
        val userId = UUID.randomUUID()

        every { userRepository.findById(userId) } returns null

        // When & Then
        val exception = assertThrows<ResourceNotFoundException> {
            getCurrentUserUseCase.execute(userId)
        }

        assertEquals("User not found", exception.message)
        verify(exactly = 1) { userRepository.findById(userId) }
    }

    @Test
    fun `should successfully get admin user`() {
        // Given
        val userId = UUID.randomUUID()
        val email = "admin@example.com"

        val user = User(
            userId = userId,
            email = email,
            role = UserRole.ADMIN,
            phone = "+9999999999",
            isActive = true,
            createdAt = Instant.now(),
            updatedAt = Instant.now()
        )

        every { userRepository.findById(userId) } returns user

        // When
        val result = getCurrentUserUseCase.execute(userId)

        // Then
        assertNotNull(result)
        assertEquals(UserRole.ADMIN, result.role)
    }
}

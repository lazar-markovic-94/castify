package com.castify.domain.user

import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import java.time.Instant
import java.util.*

class UserTest {

    @Test
    fun `should create valid user with all fields`() {
        // Given
        val userId = UUID.randomUUID()
        val email = "test@example.com"
        val role = UserRole.TALENT
        val phone = "+1234567890"

        // When
        val user = User(
            userId = userId,
            email = email,
            role = role,
            phone = phone,
            isActive = true
        )

        // Then
        assertNotNull(user)
        assertEquals(userId, user.userId)
        assertEquals(email, user.email)
        assertEquals(role, user.role)
        assertEquals(phone, user.phone)
        assertTrue(user.isActive)
        assertNotNull(user.createdAt)
        assertNotNull(user.updatedAt)
    }

    @Test
    fun `should create user with minimal required fields`() {
        // Given
        val userId = UUID.randomUUID()
        val email = "minimal@example.com"
        val role = UserRole.CLIENT

        // When
        val user = User(
            userId = userId,
            email = email,
            role = role
        )

        // Then
        assertNotNull(user)
        assertEquals(userId, user.userId)
        assertEquals(email, user.email)
        assertEquals(role, user.role)
        assertNull(user.phone)
        assertTrue(user.isActive)
    }

    @Test
    fun `should throw exception when email is blank`() {
        // When & Then
        val exception = assertThrows<IllegalArgumentException> {
            User(
                userId = UUID.randomUUID(),
                email = "",
                role = UserRole.TALENT
            )
        }

        assertEquals("Email cannot be blank", exception.message)
    }

    @Test
    fun `should throw exception when email format is invalid`() {
        // When & Then
        val exception = assertThrows<IllegalArgumentException> {
            User(
                userId = UUID.randomUUID(),
                email = "invalid-email",
                role = UserRole.TALENT
            )
        }

        assertEquals("Invalid email format", exception.message)
    }

    @Test
    fun `should accept valid email formats`() {
        val validEmails = listOf(
            "test@example.com",
            "user.name@example.com",
            "user+tag@example.co.uk",
            "user_123@test-domain.com"
        )

        validEmails.forEach { email ->
            val user = User(
                userId = UUID.randomUUID(),
                email = email,
                role = UserRole.TALENT
            )
            assertEquals(email, user.email)
        }
    }

    @Test
    fun `should deactivate user`() {
        // Given
        val user = User(
            userId = UUID.randomUUID(),
            email = "test@example.com",
            role = UserRole.TALENT,
            isActive = true
        )
        val originalUpdatedAt = user.updatedAt

        // When
        Thread.sleep(10) // Ensure timestamp difference
        val deactivatedUser = user.deactivate()

        // Then
        assertFalse(deactivatedUser.isActive)
        assertTrue(deactivatedUser.updatedAt.isAfter(originalUpdatedAt))
        assertEquals(user.userId, deactivatedUser.userId)
        assertEquals(user.email, deactivatedUser.email)
    }

    @Test
    fun `should activate user`() {
        // Given
        val user = User(
            userId = UUID.randomUUID(),
            email = "test@example.com",
            role = UserRole.TALENT,
            isActive = false
        )
        val originalUpdatedAt = user.updatedAt

        // When
        Thread.sleep(10) // Ensure timestamp difference
        val activatedUser = user.activate()

        // Then
        assertTrue(activatedUser.isActive)
        assertTrue(activatedUser.updatedAt.isAfter(originalUpdatedAt))
        assertEquals(user.userId, activatedUser.userId)
        assertEquals(user.email, activatedUser.email)
    }

    @Test
    fun `should update phone number`() {
        // Given
        val user = User(
            userId = UUID.randomUUID(),
            email = "test@example.com",
            role = UserRole.TALENT,
            phone = "+1234567890"
        )
        val newPhone = "+9876543210"
        val originalUpdatedAt = user.updatedAt

        // When
        Thread.sleep(10)
        val updatedUser = user.updatePhone(newPhone)

        // Then
        assertEquals(newPhone, updatedUser.phone)
        assertTrue(updatedUser.updatedAt.isAfter(originalUpdatedAt))
        assertEquals(user.userId, updatedUser.userId)
        assertEquals(user.email, updatedUser.email)
    }

    @Test
    fun `should clear phone number when updating with null`() {
        // Given
        val user = User(
            userId = UUID.randomUUID(),
            email = "test@example.com",
            role = UserRole.TALENT,
            phone = "+1234567890"
        )

        // When
        val updatedUser = user.updatePhone(null)

        // Then
        assertNull(updatedUser.phone)
    }

    @Test
    fun `should correctly identify talent user`() {
        // Given
        val talentUser = User(
            userId = UUID.randomUUID(),
            email = "talent@example.com",
            role = UserRole.TALENT
        )

        // Then
        assertTrue(talentUser.isTalent())
        assertFalse(talentUser.isClient())
        assertFalse(talentUser.isAdmin())
    }

    @Test
    fun `should correctly identify client user`() {
        // Given
        val clientUser = User(
            userId = UUID.randomUUID(),
            email = "client@example.com",
            role = UserRole.CLIENT
        )

        // Then
        assertTrue(clientUser.isClient())
        assertFalse(clientUser.isTalent())
        assertFalse(clientUser.isAdmin())
    }

    @Test
    fun `should correctly identify admin user`() {
        // Given
        val adminUser = User(
            userId = UUID.randomUUID(),
            email = "admin@example.com",
            role = UserRole.ADMIN
        )

        // Then
        assertTrue(adminUser.isAdmin())
        assertFalse(adminUser.isTalent())
        assertFalse(adminUser.isClient())
    }

    @Test
    fun `should maintain immutability when deactivating`() {
        // Given
        val originalUser = User(
            userId = UUID.randomUUID(),
            email = "test@example.com",
            role = UserRole.TALENT,
            isActive = true
        )

        // When
        val deactivatedUser = originalUser.deactivate()

        // Then
        assertTrue(originalUser.isActive) // Original should not change
        assertFalse(deactivatedUser.isActive)
        assertNotSame(originalUser, deactivatedUser)
    }

    @Test
    fun `should preserve other fields when updating phone`() {
        // Given
        val originalUser = User(
            userId = UUID.randomUUID(),
            email = "test@example.com",
            role = UserRole.TALENT,
            phone = "+1234567890",
            isActive = true
        )

        // When
        val updatedUser = originalUser.updatePhone("+9999999999")

        // Then
        assertEquals(originalUser.userId, updatedUser.userId)
        assertEquals(originalUser.email, updatedUser.email)
        assertEquals(originalUser.role, updatedUser.role)
        assertEquals(originalUser.isActive, updatedUser.isActive)
        assertNotEquals(originalUser.phone, updatedUser.phone)
    }
}

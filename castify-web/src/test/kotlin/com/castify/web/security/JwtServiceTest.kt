package com.castify.web.security

import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import java.util.*

class JwtServiceTest {

    private lateinit var jwtService: JwtService
    private val secret = "test-secret-key-for-jwt-that-is-at-least-256-bits-long-for-hmac-sha"
    private val expirationMs = 3600000L // 1 hour

    @BeforeEach
    fun setUp() {
        jwtService = JwtService(secret, expirationMs)
    }

    @Test
    fun `should generate valid JWT token`() {
        // Given
        val userId = UUID.randomUUID()
        val email = "test@example.com"
        val role = "TALENT"

        // When
        val token = jwtService.generateToken(userId, email, role)

        // Then
        assertNotNull(token)
        assertTrue(token.isNotBlank())
        assertTrue(token.split(".").size == 3) // JWT has 3 parts: header.payload.signature
    }

    @Test
    fun `should validate valid token`() {
        // Given
        val userId = UUID.randomUUID()
        val email = "test@example.com"
        val role = "CLIENT"
        val token = jwtService.generateToken(userId, email, role)

        // When
        val isValid = jwtService.validateToken(token)

        // Then
        assertTrue(isValid)
    }

    @Test
    fun `should reject invalid token`() {
        // Given
        val invalidToken = "invalid.jwt.token"

        // When
        val isValid = jwtService.validateToken(invalidToken)

        // Then
        assertFalse(isValid)
    }

    @Test
    fun `should reject malformed token`() {
        // Given
        val malformedToken = "not-a-jwt-token"

        // When
        val isValid = jwtService.validateToken(malformedToken)

        // Then
        assertFalse(isValid)
    }

    @Test
    fun `should extract user ID from token`() {
        // Given
        val userId = UUID.randomUUID()
        val email = "test@example.com"
        val role = "ADMIN"
        val token = jwtService.generateToken(userId, email, role)

        // When
        val extractedUserId = jwtService.getUserIdFromToken(token)

        // Then
        assertEquals(userId, extractedUserId)
    }

    @Test
    fun `should extract email from token`() {
        // Given
        val userId = UUID.randomUUID()
        val email = "user@example.com"
        val role = "TALENT"
        val token = jwtService.generateToken(userId, email, role)

        // When
        val extractedEmail = jwtService.getEmailFromToken(token)

        // Then
        assertEquals(email, extractedEmail)
    }

    @Test
    fun `should extract role from token`() {
        // Given
        val userId = UUID.randomUUID()
        val email = "test@example.com"
        val role = "CLIENT"
        val token = jwtService.generateToken(userId, email, role)

        // When
        val extractedRole = jwtService.getRoleFromToken(token)

        // Then
        assertEquals(role, extractedRole)
    }

    @Test
    fun `should generate different tokens for different users`() {
        // Given
        val userId1 = UUID.randomUUID()
        val userId2 = UUID.randomUUID()
        val email = "test@example.com"
        val role = "TALENT"

        // When
        val token1 = jwtService.generateToken(userId1, email, role)
        Thread.sleep(100) // Ensure different timestamps
        val token2 = jwtService.generateToken(userId2, email, role)

        // Then
        assertNotEquals(token1, token2)
    }

    @Test
    fun `should generate different tokens at different times`() {
        // Given
        val userId = UUID.randomUUID()
        val email = "test@example.com"
        val role = "TALENT"

        // When
        val token1 = jwtService.generateToken(userId, email, role)
        Thread.sleep(1000) // Ensure different timestamps (JWT uses seconds)
        val token2 = jwtService.generateToken(userId, email, role)

        // Then
        assertNotEquals(token1, token2)
    }

    @Test
    fun `should handle special characters in email`() {
        // Given
        val userId = UUID.randomUUID()
        val email = "user+tag@example.co.uk"
        val role = "CLIENT"

        // When
        val token = jwtService.generateToken(userId, email, role)
        val extractedEmail = jwtService.getEmailFromToken(token)

        // Then
        assertEquals(email, extractedEmail)
    }

    @Test
    fun `should handle all valid roles`() {
        val roles = listOf("TALENT", "CLIENT", "ADMIN")
        val userId = UUID.randomUUID()
        val email = "test@example.com"

        roles.forEach { role ->
            // When
            val token = jwtService.generateToken(userId, email, role)
            val extractedRole = jwtService.getRoleFromToken(token)

            // Then
            assertEquals(role, extractedRole)
        }
    }

    @Test
    fun `should reject token with tampered signature`() {
        // Given
        val userId = UUID.randomUUID()
        val email = "test@example.com"
        val role = "TALENT"
        val validToken = jwtService.generateToken(userId, email, role)

        // Tamper with the signature
        val parts = validToken.split(".")
        val tamperedToken = "${parts[0]}.${parts[1]}.tampered_signature"

        // When
        val isValid = jwtService.validateToken(tamperedToken)

        // Then
        assertFalse(isValid)
    }

    @Test
    fun `should reject empty token`() {
        // Given
        val emptyToken = ""

        // When
        val isValid = jwtService.validateToken(emptyToken)

        // Then
        assertFalse(isValid)
    }

    @Test
    fun `should extract claims from valid token only`() {
        // Given
        val userId = UUID.randomUUID()
        val email = "test@example.com"
        val role = "TALENT"
        val token = jwtService.generateToken(userId, email, role)

        // When & Then
        assertDoesNotThrow {
            jwtService.getUserIdFromToken(token)
            jwtService.getEmailFromToken(token)
            jwtService.getRoleFromToken(token)
        }
    }

    @Test
    fun `should throw exception when extracting user ID from invalid token`() {
        // Given
        val invalidToken = "invalid.jwt.token"

        // When & Then
        assertThrows<Exception> {
            jwtService.getUserIdFromToken(invalidToken)
        }
    }

    @Test
    fun `should throw exception when extracting email from invalid token`() {
        // Given
        val invalidToken = "invalid.jwt.token"

        // When & Then
        assertThrows<Exception> {
            jwtService.getEmailFromToken(invalidToken)
        }
    }

    @Test
    fun `should throw exception when extracting role from invalid token`() {
        // Given
        val invalidToken = "invalid.jwt.token"

        // When & Then
        assertThrows<Exception> {
            jwtService.getRoleFromToken(invalidToken)
        }
    }

    @Test
    fun `should maintain token validity within expiration period`() {
        // Given
        val userId = UUID.randomUUID()
        val email = "test@example.com"
        val role = "TALENT"
        val token = jwtService.generateToken(userId, email, role)

        // When - Token should still be valid immediately after creation
        val isValid = jwtService.validateToken(token)

        // Then
        assertTrue(isValid)
        // And can still extract claims
        assertEquals(userId, jwtService.getUserIdFromToken(token))
        assertEquals(email, jwtService.getEmailFromToken(token))
        assertEquals(role, jwtService.getRoleFromToken(token))
    }
}

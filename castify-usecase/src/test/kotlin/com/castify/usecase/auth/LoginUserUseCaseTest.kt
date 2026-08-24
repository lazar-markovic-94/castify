package com.castify.usecase.auth

import com.castify.domain.exception.UnauthorizedException
import com.castify.domain.user.User
import com.castify.domain.user.UserRepository
import com.castify.domain.user.UserRole
import com.castify.usecase.dto.LoginRequest
import com.castify.usecase.port.TokenService
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.springframework.security.crypto.password.PasswordEncoder
import java.time.Instant
import java.util.*

class LoginUserUseCaseTest {

    private lateinit var userRepository: UserRepository
    private lateinit var passwordEncoder: PasswordEncoder
    private lateinit var tokenService: TokenService
    private lateinit var loginUserUseCase: LoginUserUseCase

    @BeforeEach
    fun setUp() {
        userRepository = mockk()
        passwordEncoder = mockk()
        tokenService = mockk()
        loginUserUseCase = LoginUserUseCase(userRepository, passwordEncoder, tokenService)
    }

    @Test
    fun `should successfully login active talent user`() {
        // Given
        val email = "talent@example.com"
        val password = "password123"
        val userId = UUID.randomUUID()
        val expectedToken = "jwt.token.here"

        val user = User(
            userId = userId,
            email = email,
            role = UserRole.TALENT,
            phone = "+1234567890",
            isActive = true,
            createdAt = Instant.now(),
            updatedAt = Instant.now()
        )

        val request = LoginRequest(
            email = email,
            password = password
        )

        every { userRepository.findByEmail(email) } returns user
        every { userRepository.findPasswordHashByEmail(email) } returns "hashed"
        every { passwordEncoder.matches(password, "hashed") } returns true
        every { tokenService.generateToken(userId, email, UserRole.TALENT.name) } returns expectedToken

        // When
        val result = loginUserUseCase.execute(request)

        // Then
        assertNotNull(result)
        assertEquals(expectedToken, result.token)
        assertEquals(userId, result.userId)
        assertEquals(email, result.email)
        assertEquals(UserRole.TALENT, result.role)

        verify(exactly = 1) { userRepository.findByEmail(email) }
        verify(exactly = 1) { tokenService.generateToken(userId, email, UserRole.TALENT.name) }
    }

    @Test
    fun `should successfully login active client user`() {
        // Given
        val email = "client@example.com"
        val password = "clientPass123"
        val userId = UUID.randomUUID()
        val expectedToken = "client.jwt.token"

        val user = User(
            userId = userId,
            email = email,
            role = UserRole.CLIENT,
            phone = null,
            isActive = true,
            createdAt = Instant.now(),
            updatedAt = Instant.now()
        )

        val request = LoginRequest(
            email = email,
            password = password
        )

        every { userRepository.findByEmail(email) } returns user
        every { userRepository.findPasswordHashByEmail(email) } returns "hashed"
        every { passwordEncoder.matches(password, "hashed") } returns true
        every { tokenService.generateToken(userId, email, UserRole.CLIENT.name) } returns expectedToken

        // When
        val result = loginUserUseCase.execute(request)

        // Then
        assertNotNull(result)
        assertEquals(expectedToken, result.token)
        assertEquals(userId, result.userId)
        assertEquals(email, result.email)
        assertEquals(UserRole.CLIENT, result.role)
    }

    @Test
    fun `should throw UnauthorizedException when user not found`() {
        // Given
        val email = "nonexistent@example.com"
        val password = "password123"

        val request = LoginRequest(
            email = email,
            password = password
        )

        every { userRepository.findByEmail(email) } returns null

        // When & Then
        val exception = assertThrows<UnauthorizedException> {
            loginUserUseCase.execute(request)
        }

        assertEquals("Invalid email or password", exception.message)
        verify(exactly = 1) { userRepository.findByEmail(email) }
        verify(exactly = 0) { tokenService.generateToken(any(), any(), any()) }
    }

    @Test
    fun `should throw UnauthorizedException when user is inactive`() {
        // Given
        val email = "inactive@example.com"
        val password = "password123"
        val userId = UUID.randomUUID()

        val inactiveUser = User(
            userId = userId,
            email = email,
            role = UserRole.TALENT,
            phone = null,
            isActive = false,
            createdAt = Instant.now(),
            updatedAt = Instant.now()
        )

        val request = LoginRequest(
            email = email,
            password = password
        )

        every { userRepository.findByEmail(email) } returns inactiveUser
        every { userRepository.findPasswordHashByEmail(email) } returns "hashed"
        every { passwordEncoder.matches(password, "hashed") } returns true

        // When & Then
        val exception = assertThrows<UnauthorizedException> {
            loginUserUseCase.execute(request)
        }

        assertEquals("Account is not active", exception.message)
        verify(exactly = 1) { userRepository.findByEmail(email) }
        verify(exactly = 0) { tokenService.generateToken(any(), any(), any()) }
    }

    @Test
    fun `should throw IllegalArgumentException when email is null`() {
        // Given
        val request = LoginRequest(
            email = null,
            password = "password123"
        )

        // When & Then
        val exception = assertThrows<IllegalArgumentException> {
            loginUserUseCase.execute(request)
        }

        assertEquals("Email is required", exception.message)
    }

    @Test
    fun `should throw IllegalArgumentException when password is null`() {
        // Given
        val request = LoginRequest(
            email = "test@example.com",
            password = null
        )

        // When & Then
        val exception = assertThrows<IllegalArgumentException> {
            loginUserUseCase.execute(request)
        }

        assertEquals("Password is required", exception.message)
    }

    @Test
    fun `should successfully login admin user`() {
        // Given
        val email = "admin@example.com"
        val password = "adminPassword"
        val userId = UUID.randomUUID()
        val expectedToken = "admin.jwt.token"

        val adminUser = User(
            userId = userId,
            email = email,
            role = UserRole.ADMIN,
            phone = "+9999999999",
            isActive = true,
            createdAt = Instant.now(),
            updatedAt = Instant.now()
        )

        val request = LoginRequest(
            email = email,
            password = password
        )

        every { userRepository.findByEmail(email) } returns adminUser
        every { userRepository.findPasswordHashByEmail(email) } returns "hashed"
        every { passwordEncoder.matches(password, "hashed") } returns true
        every { tokenService.generateToken(userId, email, UserRole.ADMIN.name) } returns expectedToken

        // When
        val result = loginUserUseCase.execute(request)

        // Then
        assertNotNull(result)
        assertEquals(expectedToken, result.token)
        assertEquals(UserRole.ADMIN, result.role)
    }

    // --- Regression: the password was never verified at all. Login looked the
    // --- user up by email and issued a JWT, so any password authenticated any
    // --- registered account. These tests fail against that implementation.

    @Test
    fun `should reject login when password does not match the stored hash`() {
        val email = "talent@example.com"
        val user = User(
            userId = UUID.randomUUID(),
            email = email,
            role = UserRole.TALENT,
            phone = null,
            isActive = true,
            createdAt = Instant.now(),
            updatedAt = Instant.now()
        )

        every { userRepository.findByEmail(email) } returns user
        every { userRepository.findPasswordHashByEmail(email) } returns "stored-hash"
        every { passwordEncoder.matches("wrong-password", "stored-hash") } returns false

        val exception = assertThrows<UnauthorizedException> {
            loginUserUseCase.execute(LoginRequest(email = email, password = "wrong-password"))
        }

        assertEquals("Invalid email or password", exception.message)
        verify(exactly = 1) { passwordEncoder.matches("wrong-password", "stored-hash") }
        verify(exactly = 0) { tokenService.generateToken(any(), any(), any()) }
    }

    @Test
    fun `should reject login when no password hash is stored for the user`() {
        val email = "talent@example.com"
        val user = User(
            userId = UUID.randomUUID(),
            email = email,
            role = UserRole.TALENT,
            phone = null,
            isActive = true,
            createdAt = Instant.now(),
            updatedAt = Instant.now()
        )

        every { userRepository.findByEmail(email) } returns user
        every { userRepository.findPasswordHashByEmail(email) } returns null

        assertThrows<UnauthorizedException> {
            loginUserUseCase.execute(LoginRequest(email = email, password = "anything"))
        }

        verify(exactly = 0) { tokenService.generateToken(any(), any(), any()) }
    }

    @Test
    fun `should not reveal whether an email is registered`() {
        // Unknown email and wrong password must be indistinguishable, otherwise
        // the endpoint can be used to enumerate accounts.
        val user = User(
            userId = UUID.randomUUID(),
            email = "known@example.com",
            role = UserRole.CLIENT,
            phone = null,
            isActive = true,
            createdAt = Instant.now(),
            updatedAt = Instant.now()
        )
        every { userRepository.findByEmail("known@example.com") } returns user
        every { userRepository.findPasswordHashByEmail("known@example.com") } returns "hash"
        every { passwordEncoder.matches(any(), "hash") } returns false
        every { userRepository.findByEmail("unknown@example.com") } returns null

        val wrongPassword = assertThrows<UnauthorizedException> {
            loginUserUseCase.execute(LoginRequest("known@example.com", "nope"))
        }
        val unknownEmail = assertThrows<UnauthorizedException> {
            loginUserUseCase.execute(LoginRequest("unknown@example.com", "nope"))
        }

        assertEquals(unknownEmail.message, wrongPassword.message)
    }
}

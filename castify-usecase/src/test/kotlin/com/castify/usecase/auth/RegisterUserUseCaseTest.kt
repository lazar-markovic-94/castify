package com.castify.usecase.auth

import com.castify.domain.exception.BadRequestException
import com.castify.domain.exception.ConflictException
import com.castify.domain.user.User
import com.castify.domain.user.UserRepository
import com.castify.domain.user.UserRole
import com.castify.usecase.dto.RegisterRequest
import com.castify.usecase.port.TokenService
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.springframework.security.crypto.password.PasswordEncoder
import java.util.*

class RegisterUserUseCaseTest {

    private lateinit var userRepository: UserRepository
    private lateinit var passwordEncoder: PasswordEncoder
    private lateinit var tokenService: TokenService
    private lateinit var registerUserUseCase: RegisterUserUseCase

    @BeforeEach
    fun setUp() {
        userRepository = mockk()
        passwordEncoder = mockk()
        tokenService = mockk()
        registerUserUseCase = RegisterUserUseCase(userRepository, passwordEncoder, tokenService)
    }

    @Test
    fun `should successfully register new talent user`() {
        // Given
        val email = "john.doe@example.com"
        val password = "securePassword123"
        val hashedPassword = "hashedPassword123"
        val role = UserRole.TALENT
        val phone = "+1234567890"

        val request = RegisterRequest(
            email = email,
            password = password,
            role = role,
            phone = phone
        )

        val expectedToken = "jwt.token.here"

        every { userRepository.existsByEmail(email) } returns false
        every { passwordEncoder.encode(password) } returns hashedPassword
        every { userRepository.save(any()) } answers { firstArg() }
        every { tokenService.generateToken(any(), email, role.name) } returns expectedToken

        // When
        val result = registerUserUseCase.execute(request)

        // Then
        assertNotNull(result)
        assertEquals(expectedToken, result.token)
        assertEquals(email, result.email)
        assertEquals(role, result.role)
        assertNotNull(result.userId)

        verify(exactly = 1) { userRepository.existsByEmail(email) }
        verify(exactly = 1) { passwordEncoder.encode(password) }
        verify(exactly = 1) { userRepository.save(any()) }
        verify(exactly = 1) { tokenService.generateToken(any(), email, role.name) }
    }

    @Test
    fun `should successfully register new client user`() {
        // Given
        val email = "client@example.com"
        val password = "clientPassword123"
        val hashedPassword = "hashedClientPassword"
        val role = UserRole.CLIENT

        val request = RegisterRequest(
            email = email,
            password = password,
            role = role,
            phone = null
        )

        val expectedToken = "client.jwt.token"

        every { userRepository.existsByEmail(email) } returns false
        every { passwordEncoder.encode(password) } returns hashedPassword
        every { userRepository.save(any()) } answers { firstArg() }
        every { tokenService.generateToken(any(), email, role.name) } returns expectedToken

        // When
        val result = registerUserUseCase.execute(request)

        // Then
        assertNotNull(result)
        assertEquals(expectedToken, result.token)
        assertEquals(email, result.email)
        assertEquals(role, result.role)
    }

    @Test
    fun `should throw ConflictException when email already exists`() {
        // Given
        val email = "existing@example.com"
        val request = RegisterRequest(
            email = email,
            password = "password123",
            role = UserRole.TALENT,
            phone = null
        )

        every { userRepository.existsByEmail(email) } returns true

        // When & Then
        val exception = assertThrows<ConflictException> {
            registerUserUseCase.execute(request)
        }

        assertTrue(exception.message?.contains("Email already registered") ?: false)
        verify(exactly = 1) { userRepository.existsByEmail(email) }
        verify(exactly = 0) { passwordEncoder.encode(any()) }
        verify(exactly = 0) { userRepository.save(any()) }
    }

    @Test
    fun `should throw BadRequestException when email is null`() {
        // Given
        val request = RegisterRequest(
            email = null,
            password = "password123",
            role = UserRole.TALENT,
            phone = null
        )

        // When & Then
        val exception = assertThrows<BadRequestException> {
            registerUserUseCase.execute(request)
        }

        assertEquals("Email is required", exception.message)
    }

    @Test
    fun `should throw BadRequestException when password is null`() {
        // Given
        val request = RegisterRequest(
            email = "test@example.com",
            password = null,
            role = UserRole.TALENT,
            phone = null
        )

        // When & Then
        val exception = assertThrows<BadRequestException> {
            registerUserUseCase.execute(request)
        }

        assertEquals("Password is required", exception.message)
    }

    @Test
    fun `should throw BadRequestException when role is null`() {
        // Given
        val request = RegisterRequest(
            email = "test@example.com",
            password = "password123",
            role = null,
            phone = null
        )

        // When & Then
        val exception = assertThrows<BadRequestException> {
            registerUserUseCase.execute(request)
        }

        assertEquals("Role is required", exception.message)
    }

    @Test
    fun `should successfully register admin user`() {
        // Given
        val email = "admin@example.com"
        val password = "adminPassword123"
        val hashedPassword = "hashedAdminPassword"
        val role = UserRole.ADMIN

        val request = RegisterRequest(
            email = email,
            password = password,
            role = role,
            phone = "+9876543210"
        )

        val expectedToken = "admin.jwt.token"

        every { userRepository.existsByEmail(email) } returns false
        every { passwordEncoder.encode(password) } returns hashedPassword
        every { userRepository.save(any()) } answers { firstArg() }
        every { tokenService.generateToken(any(), email, role.name) } returns expectedToken

        // When
        val result = registerUserUseCase.execute(request)

        // Then
        assertNotNull(result)
        assertEquals(expectedToken, result.token)
        assertEquals(email, result.email)
        assertEquals(role, result.role)
        assertEquals(UserRole.ADMIN, result.role)
    }
}

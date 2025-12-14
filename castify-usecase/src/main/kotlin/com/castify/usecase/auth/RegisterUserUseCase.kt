package com.castify.usecase.auth

import com.castify.domain.exception.ConflictException
import com.castify.domain.user.User
import com.castify.domain.user.UserRepository
import com.castify.domain.user.UserRole
import com.castify.usecase.dto.AuthResponse
import com.castify.usecase.dto.RegisterRequest
import com.castify.usecase.port.TokenService
import org.slf4j.LoggerFactory
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.stereotype.Service
import java.util.*

/**
 * Use case for registering a new user.
 */
@Service
class RegisterUserUseCase(
    private val userRepository: UserRepository,
    private val passwordEncoder: PasswordEncoder,
    private val tokenService: TokenService
) {
    private val logger = LoggerFactory.getLogger(RegisterUserUseCase::class.java)

    /**
     * Registers a new user and returns authentication token.
     *
     * @param request The registration request
     * @return Authentication response with token
     * @throws ConflictException if email is already registered
     * @throws IllegalArgumentException if required fields are missing
     */
    fun execute(request: RegisterRequest): AuthResponse {
        return try {
            val email: String = requireNotNull(request.email) { "Email is required" }
            val password: String = requireNotNull(request.password) { "Password is required" }
            val role: UserRole = requireNotNull(request.role) { "Role is required" }

            logger.info("Registering new user with email: $email, role: $role")

            if (userRepository.existsByEmail(email)) {
                throw ConflictException("Email already registered: $email")
            }

            val hashedPassword: String = passwordEncoder.encode(password)!!

            val user = User(
                userId = UUID.randomUUID(),
                email = email,
                role = role,
                phone = request.phone
            )

            val savedUser = userRepository.save(user)
            logger.info("Successfully registered user: ${savedUser.userId}")

            val token = tokenService.generateToken(
                savedUser.userId,
                email,
                role.name
            )

            AuthResponse(
                token = token,
                userId = savedUser.userId,
                email = email,
                role = role
            )

        } catch (e: ConflictException) {
            logger.error("Email already registered: ${request.email}", e)
            throw e
        } catch (e: IllegalArgumentException) {
            logger.error("Invalid registration data", e)
            throw e
        } catch (e: Exception) {
            logger.error("Error registering user: ${request.email}", e)
            throw RuntimeException("Failed to register user: ${e.message}", e)
        }
    }
}

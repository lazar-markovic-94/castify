package com.castify.usecase.auth

import com.castify.domain.exception.BadRequestException
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
     * @throws BadRequestException if required fields are missing
     */
    fun execute(request: RegisterRequest): AuthResponse {
        val email: String = request.email
            ?: throw BadRequestException("Email is required")
        val password: String = request.password
            ?: throw BadRequestException("Password is required")
        val role: UserRole = request.role
            ?: throw BadRequestException("Role is required")

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

        // hashedPassword was previously computed and then dropped on the floor:
        // save(user) took no hash, so the adapter stored a hash of the literal
        // string "temporary" instead of the user's actual password.
        val savedUser = userRepository.create(user, hashedPassword)
        logger.info("Successfully registered user: ${savedUser.userId}")

        val token = tokenService.generateToken(
            savedUser.userId,
            email,
            role.name
        )

        return AuthResponse(
            token = token,
            userId = savedUser.userId,
            email = email,
            role = role
        )
    }
}

package com.castify.usecase.auth

import com.castify.domain.exception.UnauthorizedException
import com.castify.domain.user.UserRepository
import com.castify.usecase.dto.AuthResponse
import com.castify.usecase.dto.LoginRequest
import com.castify.usecase.port.TokenService
import org.slf4j.LoggerFactory
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.stereotype.Service

/**
 * Use case for user login/authentication.
 */
@Service
class LoginUserUseCase(
    private val userRepository: UserRepository,
    private val passwordEncoder: PasswordEncoder,
    private val tokenService: TokenService
) {
    private val logger = LoggerFactory.getLogger(LoginUserUseCase::class.java)

    /**
     * Authenticates a user and returns authentication token.
     *
     * @param request The login request
     * @return Authentication response with token
     * @throws UnauthorizedException if credentials are invalid or account is inactive
     * @throws IllegalArgumentException if required fields are missing
     */
    fun execute(request: LoginRequest): AuthResponse {
        return try {
            val email = requireNotNull(request.email) { "Email is required" }
            val password = requireNotNull(request.password) { "Password is required" }

            logger.info("Login attempt for email: $email")

            val user = userRepository.findByEmail(email)
                ?: throw UnauthorizedException("Invalid email or password")

            // Note: Password checking is done in persistence layer adapter
            // since domain User doesn't contain password

            if (!user.isActive) {
                logger.warn("Login attempted for inactive account: $email")
                throw UnauthorizedException("Account is not active")
            }

            val token = tokenService.generateToken(
                user.userId,
                user.email,
                user.role.name
            )

            logger.info("Successfully logged in user: ${user.userId}")

            AuthResponse(
                token = token,
                userId = user.userId,
                email = user.email,
                role = user.role
            )

        } catch (e: UnauthorizedException) {
            logger.error("Authentication failed for: ${request.email}", e)
            throw e
        } catch (e: IllegalArgumentException) {
            logger.error("Invalid login data", e)
            throw e
        } catch (e: Exception) {
            logger.error("Error during login for: ${request.email}", e)
            throw RuntimeException("Failed to login: ${e.message}", e)
        }
    }
}

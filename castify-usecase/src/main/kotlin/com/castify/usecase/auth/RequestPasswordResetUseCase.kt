package com.castify.usecase.auth

import com.castify.domain.auth.PasswordResetToken
import com.castify.domain.auth.PasswordResetTokenRepository
import com.castify.domain.user.UserRepository
import com.castify.usecase.dto.PasswordResetRequest
import com.castify.usecase.dto.PasswordResetResponse
import com.castify.usecase.port.EmailService
import org.springframework.stereotype.Service
import java.security.SecureRandom
import java.util.*

@Service
class RequestPasswordResetUseCase(
    private val userRepository: UserRepository,
    private val passwordResetTokenRepository: PasswordResetTokenRepository,
    private val emailService: EmailService
) {
    private val secureRandom = SecureRandom()

    fun execute(request: PasswordResetRequest): PasswordResetResponse {
        val email = request.email?.trim()
            ?: throw IllegalArgumentException("Email is required")

        // Find user by email - we don't want to reveal if the email exists or not for security
        val user = userRepository.findByEmail(email)

        if (user != null && user.isActive) {
            // Generate a secure random token
            val resetToken = generateSecureToken()

            // Create and save the reset token
            val passwordResetToken = PasswordResetToken.create(
                userId = user.userId,
                token = resetToken,
                expirationHours = 24
            )
            passwordResetTokenRepository.save(passwordResetToken)

            // Send password reset email
            emailService.sendPasswordResetEmail(
                toEmail = user.email,
                resetToken = resetToken,
                userName = user.email.substringBefore("@")
            )
        }

        // Always return success to prevent email enumeration attacks
        return PasswordResetResponse(
            message = "If an account exists with this email, a password reset link has been sent.",
            success = true
        )
    }

    private fun generateSecureToken(): String {
        val bytes = ByteArray(32)
        secureRandom.nextBytes(bytes)
        return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes)
    }
}

package com.castify.usecase.auth

import com.castify.domain.auth.PasswordResetTokenRepository
import com.castify.domain.user.UserRepository
import com.castify.usecase.dto.PasswordResetConfirm
import com.castify.usecase.dto.PasswordResetResponse
import com.castify.usecase.port.EmailService
import org.springframework.stereotype.Service

@Service
class ConfirmPasswordResetUseCase(
    private val userRepository: UserRepository,
    private val passwordResetTokenRepository: PasswordResetTokenRepository,
    private val emailService: EmailService
) {
    fun execute(request: PasswordResetConfirm): PasswordResetResponse {
        val token = request.token?.trim()
            ?: throw IllegalArgumentException("Token is required")

        val newPassword = request.newPassword?.trim()
            ?: throw IllegalArgumentException("New password is required")

        if (newPassword.length < 8) {
            throw IllegalArgumentException("Password must be at least 8 characters")
        }

        // Find the reset token
        val resetToken = passwordResetTokenRepository.findByToken(token)
            ?: throw IllegalArgumentException("Invalid or expired reset token")

        // Validate the token
        if (!resetToken.isValid()) {
            throw IllegalArgumentException(
                when {
                    resetToken.used -> "This reset token has already been used"
                    resetToken.isExpired() -> "This reset token has expired"
                    else -> "Invalid reset token"
                }
            )
        }

        // Find the user
        val user = userRepository.findById(resetToken.userId)
            ?: throw IllegalArgumentException("User not found")

        if (!user.isActive) {
            throw IllegalArgumentException("User account is not active")
        }

        // Update the password
        val passwordUpdated = userRepository.updatePassword(user.userId, newPassword)

        if (!passwordUpdated) {
            throw RuntimeException("Failed to update password")
        }

        // Mark the token as used
        passwordResetTokenRepository.markAsUsed(token)

        // Send confirmation email
        emailService.sendPasswordResetConfirmationEmail(
            toEmail = user.email,
            userName = user.email.substringBefore("@")
        )

        return PasswordResetResponse(
            message = "Password has been successfully reset",
            success = true
        )
    }
}

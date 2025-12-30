package com.castify.web.service

import com.castify.usecase.port.EmailService
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Service

@Service
class ConsoleEmailService(
    @Value("\${app.frontend-url:http://localhost:3000}")
    private val frontendUrl: String
) : EmailService {

    private val logger = LoggerFactory.getLogger(ConsoleEmailService::class.java)

    override fun sendPasswordResetEmail(toEmail: String, resetToken: String, userName: String?) {
        val resetLink = "$frontendUrl/reset-password?token=$resetToken"
        val greeting = userName?.let { "Hello $it" } ?: "Hello"

        val emailContent = """
            |==========================================================
            |PASSWORD RESET EMAIL
            |==========================================================
            |To: $toEmail
            |Subject: Password Reset Request
            |
            |$greeting,
            |
            |You have requested to reset your password for your Castify account.
            |
            |Please click the link below to reset your password:
            |$resetLink
            |
            |This link will expire in 24 hours.
            |
            |If you did not request this password reset, please ignore this email.
            |
            |Best regards,
            |The Castify Team
            |==========================================================
        """.trimMargin()

        logger.info(emailContent)
        println(emailContent)
    }

    override fun sendPasswordResetConfirmationEmail(toEmail: String, userName: String?) {
        val greeting = userName?.let { "Hello $it" } ?: "Hello"

        val emailContent = """
            |==========================================================
            |PASSWORD RESET CONFIRMATION EMAIL
            |==========================================================
            |To: $toEmail
            |Subject: Password Successfully Reset
            |
            |$greeting,
            |
            |Your password has been successfully reset.
            |
            |If you did not make this change, please contact our support team immediately.
            |
            |Best regards,
            |The Castify Team
            |==========================================================
        """.trimMargin()

        logger.info(emailContent)
        println(emailContent)
    }
}

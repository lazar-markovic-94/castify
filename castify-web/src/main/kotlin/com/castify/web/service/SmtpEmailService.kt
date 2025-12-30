package com.castify.web.service

import com.castify.usecase.port.EmailService
import jakarta.mail.internet.MimeMessage
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Primary
import org.springframework.mail.javamail.JavaMailSender
import org.springframework.mail.javamail.MimeMessageHelper
import org.springframework.stereotype.Service

@Service
@Primary
class SmtpEmailService(
    private val mailSender: JavaMailSender,
    @Value("\${app.frontend-url:http://localhost:3000}")
    private val frontendUrl: String,
    @Value("\${app.mail.from:noreply@castify.com}")
    private val fromEmail: String,
    @Value("\${app.mail.from-name:Castify Team}")
    private val fromName: String
) : EmailService {

    private val logger = LoggerFactory.getLogger(SmtpEmailService::class.java)

    override fun sendPasswordResetEmail(toEmail: String, resetToken: String, userName: String?) {
        try {
            val resetLink = "$frontendUrl/reset-password?token=$resetToken"
            val greeting = userName?.let { "Hello $it" } ?: "Hello"

            val htmlContent = """
                <!DOCTYPE html>
                <html>
                <head>
                    <style>
                        body { font-family: Arial, sans-serif; line-height: 1.6; color: #333; }
                        .container { max-width: 600px; margin: 0 auto; padding: 20px; }
                        .header { background-color: #4F46E5; color: white; padding: 20px; text-align: center; }
                        .content { background-color: #f9f9f9; padding: 30px; border-radius: 5px; margin-top: 20px; }
                        .button {
                            display: inline-block;
                            background-color: #4F46E5;
                            color: white;
                            padding: 12px 30px;
                            text-decoration: none;
                            border-radius: 5px;
                            margin: 20px 0;
                        }
                        .footer { text-align: center; margin-top: 30px; color: #666; font-size: 12px; }
                        .warning { background-color: #FEF3C7; padding: 15px; border-left: 4px solid #F59E0B; margin: 20px 0; }
                    </style>
                </head>
                <body>
                    <div class="container">
                        <div class="header">
                            <h1>Password Reset Request</h1>
                        </div>
                        <div class="content">
                            <p>$greeting,</p>
                            <p>You have requested to reset your password for your Castify account.</p>
                            <p>Please click the button below to reset your password:</p>
                            <div style="text-align: center;">
                                <a href="$resetLink" class="button">Reset Password</a>
                            </div>
                            <p style="font-size: 12px; color: #666;">
                                Or copy and paste this link into your browser:<br>
                                <a href="$resetLink">$resetLink</a>
                            </p>
                            <div class="warning">
                                <strong>⏰ Important:</strong> This link will expire in 24 hours.
                            </div>
                            <p>If you did not request this password reset, please ignore this email and your password will remain unchanged.</p>
                        </div>
                        <div class="footer">
                            <p>© ${java.time.Year.now()} Castify. All rights reserved.</p>
                            <p>This is an automated message, please do not reply to this email.</p>
                        </div>
                    </div>
                </body>
                </html>
            """.trimIndent()

            sendHtmlEmail(toEmail, "Password Reset Request - Castify", htmlContent)
            logger.info("Password reset email sent successfully to: $toEmail")
        } catch (e: Exception) {
            logger.error("Failed to send password reset email to: $toEmail", e)
            // Fallback to console logging
            logger.warn("Email sending failed. Reset token for $toEmail: $resetToken")
        }
    }

    override fun sendPasswordResetConfirmationEmail(toEmail: String, userName: String?) {
        try {
            val greeting = userName?.let { "Hello $it" } ?: "Hello"

            val htmlContent = """
                <!DOCTYPE html>
                <html>
                <head>
                    <style>
                        body { font-family: Arial, sans-serif; line-height: 1.6; color: #333; }
                        .container { max-width: 600px; margin: 0 auto; padding: 20px; }
                        .header { background-color: #10B981; color: white; padding: 20px; text-align: center; }
                        .content { background-color: #f9f9f9; padding: 30px; border-radius: 5px; margin-top: 20px; }
                        .success-icon { font-size: 48px; text-align: center; margin: 20px 0; }
                        .footer { text-align: center; margin-top: 30px; color: #666; font-size: 12px; }
                        .alert { background-color: #FEE2E2; padding: 15px; border-left: 4px solid #EF4444; margin: 20px 0; }
                    </style>
                </head>
                <body>
                    <div class="container">
                        <div class="header">
                            <h1>Password Successfully Reset</h1>
                        </div>
                        <div class="content">
                            <div class="success-icon">✅</div>
                            <p>$greeting,</p>
                            <p>Your password has been successfully reset for your Castify account.</p>
                            <p>You can now log in with your new password.</p>
                            <div class="alert">
                                <strong>⚠️ Security Notice:</strong><br>
                                If you did not make this change, please contact our support team immediately at support@castify.com
                            </div>
                            <p>For your security, we recommend:</p>
                            <ul>
                                <li>Using a strong, unique password</li>
                                <li>Not sharing your password with anyone</li>
                                <li>Enabling two-factor authentication (when available)</li>
                            </ul>
                        </div>
                        <div class="footer">
                            <p>© ${java.time.Year.now()} Castify. All rights reserved.</p>
                            <p>This is an automated message, please do not reply to this email.</p>
                        </div>
                    </div>
                </body>
                </html>
            """.trimIndent()

            sendHtmlEmail(toEmail, "Password Reset Confirmation - Castify", htmlContent)
            logger.info("Password reset confirmation email sent successfully to: $toEmail")
        } catch (e: Exception) {
            logger.error("Failed to send password reset confirmation email to: $toEmail", e)
        }
    }

    private fun sendHtmlEmail(to: String, subject: String, htmlContent: String) {
        val message: MimeMessage = mailSender.createMimeMessage()
        val helper = MimeMessageHelper(message, true, "UTF-8")

        helper.setFrom(fromEmail, fromName)
        helper.setTo(to)
        helper.setSubject(subject)
        helper.setText(htmlContent, true)

        mailSender.send(message)
    }
}

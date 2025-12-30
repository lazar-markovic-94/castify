package com.castify.usecase.port

interface EmailService {
    fun sendPasswordResetEmail(toEmail: String, resetToken: String, userName: String? = null)
    fun sendPasswordResetConfirmationEmail(toEmail: String, userName: String? = null)
}

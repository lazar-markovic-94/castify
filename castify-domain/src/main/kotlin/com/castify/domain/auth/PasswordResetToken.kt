package com.castify.domain.auth

import java.time.LocalDateTime
import java.util.*

data class PasswordResetToken(
    val tokenId: UUID,
    val userId: UUID,
    val token: String,
    val expiresAt: LocalDateTime,
    val used: Boolean,
    val createdAt: LocalDateTime
) {
    fun isValid(): Boolean {
        return !used && expiresAt.isAfter(LocalDateTime.now())
    }

    fun isExpired(): Boolean {
        return expiresAt.isBefore(LocalDateTime.now())
    }

    companion object {
        fun create(userId: UUID, token: String, expirationHours: Long = 24): PasswordResetToken {
            return PasswordResetToken(
                tokenId = UUID.randomUUID(),
                userId = userId,
                token = token,
                expiresAt = LocalDateTime.now().plusHours(expirationHours),
                used = false,
                createdAt = LocalDateTime.now()
            )
        }
    }
}

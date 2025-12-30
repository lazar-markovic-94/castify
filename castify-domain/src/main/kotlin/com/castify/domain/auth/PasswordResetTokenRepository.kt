package com.castify.domain.auth

import java.util.*

interface PasswordResetTokenRepository {
    fun save(token: PasswordResetToken): PasswordResetToken
    fun findByToken(token: String): PasswordResetToken?
    fun findByUserId(userId: UUID): List<PasswordResetToken>
    fun markAsUsed(token: String): Boolean
    fun deleteExpiredTokens(): Int
}

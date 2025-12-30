package com.castify.persistence.jpa

import com.castify.persistence.entity.PasswordResetTokenEntity
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Modifying
import org.springframework.data.jpa.repository.Query
import org.springframework.stereotype.Repository
import java.time.LocalDateTime
import java.util.*

@Repository
interface PasswordResetTokenJpaRepository : JpaRepository<PasswordResetTokenEntity, UUID> {
    fun findByToken(token: String): PasswordResetTokenEntity?

    fun findByUserId(userId: UUID): List<PasswordResetTokenEntity>

    @Modifying
    @Query("DELETE FROM PasswordResetTokenEntity t WHERE t.expiresAt < :now")
    fun deleteExpiredTokens(now: LocalDateTime): Int

    @Modifying
    @Query("UPDATE PasswordResetTokenEntity t SET t.used = true WHERE t.token = :token")
    fun markTokenAsUsed(token: String): Int
}

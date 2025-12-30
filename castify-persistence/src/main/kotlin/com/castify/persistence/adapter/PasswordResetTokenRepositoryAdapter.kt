package com.castify.persistence.adapter

import com.castify.domain.auth.PasswordResetToken
import com.castify.domain.auth.PasswordResetTokenRepository
import com.castify.persistence.entity.PasswordResetTokenEntity
import com.castify.persistence.jpa.PasswordResetTokenJpaRepository
import org.springframework.stereotype.Repository
import org.springframework.transaction.annotation.Transactional
import java.time.LocalDateTime
import java.util.*

@Repository
class PasswordResetTokenRepositoryAdapter(
    private val jpaRepository: PasswordResetTokenJpaRepository
) : PasswordResetTokenRepository {

    override fun save(token: PasswordResetToken): PasswordResetToken {
        val entity = toEntity(token)
        val saved = jpaRepository.save(entity)
        return toDomain(saved)
    }

    override fun findByToken(token: String): PasswordResetToken? {
        return jpaRepository.findByToken(token)?.let { toDomain(it) }
    }

    override fun findByUserId(userId: UUID): List<PasswordResetToken> {
        return jpaRepository.findByUserId(userId).map { toDomain(it) }
    }

    @Transactional
    override fun markAsUsed(token: String): Boolean {
        val updated = jpaRepository.markTokenAsUsed(token)
        return updated > 0
    }

    @Transactional
    override fun deleteExpiredTokens(): Int {
        return jpaRepository.deleteExpiredTokens(LocalDateTime.now())
    }

    private fun toEntity(domain: PasswordResetToken): PasswordResetTokenEntity {
        return PasswordResetTokenEntity(
            tokenId = domain.tokenId,
            userId = domain.userId,
            token = domain.token,
            expiresAt = domain.expiresAt,
            used = domain.used,
            createdAt = domain.createdAt
        )
    }

    private fun toDomain(entity: PasswordResetTokenEntity): PasswordResetToken {
        return PasswordResetToken(
            tokenId = entity.tokenId,
            userId = entity.userId,
            token = entity.token,
            expiresAt = entity.expiresAt,
            used = entity.used,
            createdAt = entity.createdAt
        )
    }
}

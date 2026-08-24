package com.castify.persistence.adapter

import com.castify.domain.user.User
import com.castify.domain.user.UserRepository
import com.castify.persistence.entity.UserEntity
import com.castify.persistence.jpa.UserJpaRepository
import org.springframework.stereotype.Component
import java.util.*

@Component
class UserRepositoryAdapter(
    private val userJpaRepository: UserJpaRepository
) : UserRepository {

    /**
     * Updates an existing user, carrying the stored password hash over from the
     * row on disk.
     *
     * The previous implementation looked the hash up in an in-memory map and
     * fell back to encoding the literal string "temporary" when it missed -
     * which it always did for a fresh process - so every registration wrote a
     * hash of "temporary" and the user's real password was discarded.
     */
    override fun save(user: User): User {
        val existing = userJpaRepository.findById(user.userId).orElse(null)
            ?: throw IllegalStateException(
                "Cannot save unknown user ${user.userId}; use create() to register a new one"
            )

        val entity = UserEntity.fromDomain(user, existing.passwordHash)
        return userJpaRepository.save(entity).toDomain()
    }

    override fun create(user: User, passwordHash: String): User {
        val entity = UserEntity.fromDomain(user, passwordHash)
        return userJpaRepository.save(entity).toDomain()
    }

    override fun findById(userId: UUID): User? {
        return userJpaRepository.findById(userId)
            .map { it.toDomain() }
            .orElse(null)
    }

    override fun findByEmail(email: String): User? {
        return userJpaRepository.findByEmail(email)?.toDomain()
    }

    override fun existsByEmail(email: String): Boolean {
        return userJpaRepository.existsByEmail(email)
    }

    override fun findPasswordHashByEmail(email: String): String? {
        return userJpaRepository.findByEmail(email)?.passwordHash
    }
}

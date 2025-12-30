package com.castify.persistence.adapter

import com.castify.domain.user.User
import com.castify.domain.user.UserRepository
import com.castify.persistence.entity.UserEntity
import com.castify.persistence.jpa.UserJpaRepository
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.stereotype.Component
import java.util.*

@Component
class UserRepositoryAdapter(
    private val userJpaRepository: UserJpaRepository,
    private val passwordEncoder: PasswordEncoder
) : UserRepository {

    // In-memory password storage for this session
    // Note: In production, consider more robust solution
    private val passwordStore = mutableMapOf<UUID, String>()

    override fun save(user: User): User {
        val passwordHash = passwordStore[user.userId] ?: passwordEncoder.encode("temporary")!!
        val entity = UserEntity.fromDomain(user, passwordHash)
        val saved = userJpaRepository.save(entity)
        return saved.toDomain()
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

    override fun updatePassword(userId: UUID, newPassword: String): Boolean {
        val entity = userJpaRepository.findById(userId).orElse(null) ?: return false
        val hashedPassword = passwordEncoder.encode(newPassword) ?: return false
        entity.passwordHash = hashedPassword
        userJpaRepository.save(entity)
        passwordStore[userId] = hashedPassword
        return true
    }

    // Extension method to save user with password
    fun saveWithPassword(user: User, rawPassword: String): User {
        val hashedPassword = passwordEncoder.encode(rawPassword)!!
        passwordStore[user.userId] = hashedPassword
        val entity = UserEntity.fromDomain(user, hashedPassword)
        val saved = userJpaRepository.save(entity)
        return saved.toDomain()
    }

    // Extension method to verify password
    fun verifyPassword(email: String, rawPassword: String): Boolean {
        val entity = userJpaRepository.findByEmail(email) ?: return false
        return passwordEncoder.matches(rawPassword, entity.passwordHash)
    }
}

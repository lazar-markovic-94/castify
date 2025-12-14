package com.castify.persistence.entity

import com.castify.domain.user.User
import com.castify.domain.user.UserRole
import jakarta.persistence.*
import java.time.Instant
import java.util.*

@Entity
@Table(name = "app_user")
class UserEntity(
    @Id
    @Column(name = "user_id", columnDefinition = "UUID")
    var userId: UUID = UUID.randomUUID(),

    @Column(unique = true, nullable = false, columnDefinition = "CITEXT")
    var email: String,

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    var role: UserRole,

    @Column(name = "password_hash", nullable = false)
    var passwordHash: String,

    @Column(length = 20)
    var phone: String? = null,

    @Column(name = "is_active", nullable = false)
    var isActive: Boolean = true,

    @Column(name = "created_at", nullable = false)
    var createdAt: Instant = Instant.now(),

    @Column(name = "updated_at", nullable = false)
    var updatedAt: Instant = Instant.now()
) {
    fun toDomain(): User = User(
        userId = userId,
        email = email,
        role = role,
        phone = phone,
        isActive = isActive,
        createdAt = createdAt,
        updatedAt = updatedAt
    )

    companion object {
        fun fromDomain(user: User, passwordHash: String): UserEntity = UserEntity(
            userId = user.userId,
            email = user.email,
            role = user.role,
            passwordHash = passwordHash,
            phone = user.phone,
            isActive = user.isActive,
            createdAt = user.createdAt,
            updatedAt = user.updatedAt
        )
    }
}

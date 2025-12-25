package com.castify.domain.user

import java.time.Instant
import java.util.*

data class User(
    val userId: UUID,
    val email: String,
    val role: UserRole,
    val phone: String? = null,
    val isActive: Boolean = true,
    val createdAt: Instant = Instant.now(),
    val updatedAt: Instant = Instant.now()
) {
    init {
        require(email.isNotBlank()) { "Email cannot be blank" }
        require(email.matches(Regex("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$"))) {
            "Invalid email format"
        }
    }

    fun isTalent(): Boolean = role == UserRole.TALENT

    fun isClient(): Boolean = role == UserRole.CLIENT

    fun isAdmin(): Boolean = role == UserRole.ADMIN

    fun deactivate(): User = copy(isActive = false, updatedAt = Instant.now())

    fun activate(): User = copy(isActive = true, updatedAt = Instant.now())

    fun updatePhone(newPhone: String?): User = copy(phone = newPhone, updatedAt = Instant.now())
}

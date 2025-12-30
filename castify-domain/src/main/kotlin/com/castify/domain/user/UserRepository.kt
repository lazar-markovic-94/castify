package com.castify.domain.user

import java.util.*

interface UserRepository {
    fun save(user: User): User
    fun findById(userId: UUID): User?
    fun findByEmail(email: String): User?
    fun existsByEmail(email: String): Boolean
    fun updatePassword(userId: UUID, newPassword: String): Boolean
}

package com.castify.domain.user

import java.util.*

interface UserRepository {
    /**
     * Persists changes to an existing user. The stored password hash is left
     * untouched - use [create] to establish it.
     *
     * @throws IllegalStateException if the user does not already exist
     */
    fun save(user: User): User

    /**
     * Persists a brand new user together with the hash of their password.
     * Hashing is the caller's responsibility; this layer only stores.
     */
    fun create(user: User, passwordHash: String): User

    fun findById(userId: UUID): User?
    fun findByEmail(email: String): User?
    fun existsByEmail(email: String): Boolean

    /**
     * The stored password hash for an email, or null if there is no such user.
     * Exposed so the use case layer can verify credentials with the same
     * PasswordEncoder it used to create them.
     */
    fun findPasswordHashByEmail(email: String): String?
}

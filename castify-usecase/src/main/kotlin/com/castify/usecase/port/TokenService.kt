package com.castify.usecase.port

import java.util.*

interface TokenService {
    fun generateToken(userId: UUID, email: String, role: String): String
    fun validateToken(token: String): Boolean
    fun getUserIdFromToken(token: String): UUID
    fun getEmailFromToken(token: String): String
    fun getRoleFromToken(token: String): String
}

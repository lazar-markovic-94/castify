package com.castify.usecase.dto

import com.castify.domain.user.UserRole
import jakarta.validation.constraints.Email
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.Size
import java.util.*

data class RegisterRequest(
    @field:Email(message = "Invalid email format")
    @field:NotBlank(message = "Email is required")
    val email: String?,

    @field:NotBlank(message = "Password is required")
    @field:Size(min = 8, message = "Password must be at least 8 characters")
    val password: String?,

    val role: UserRole?,
    val phone: String? = null
)

data class LoginRequest(
    @field:Email(message = "Invalid email format")
    @field:NotBlank(message = "Email is required")
    val email: String?,

    @field:NotBlank(message = "Password is required")
    val password: String?
)

data class AuthResponse(
    val token: String,
    val userId: UUID,
    val email: String,
    val role: UserRole
)

data class UserInfoResponse(
    val userId: UUID,
    val email: String,
    val role: UserRole,
    val phone: String?,
    val isActive: Boolean
)

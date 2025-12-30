package com.castify.web.controller

import com.castify.usecase.auth.ConfirmPasswordResetUseCase
import com.castify.usecase.auth.GetCurrentUserUseCase
import com.castify.usecase.auth.LoginUserUseCase
import com.castify.usecase.auth.RegisterUserUseCase
import com.castify.usecase.auth.RequestPasswordResetUseCase
import com.castify.usecase.dto.*
import com.castify.web.security.SecurityUtils
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.tags.Tag
import jakarta.validation.Valid
import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/api/auth")
@Tag(name = "Authentication", description = "User authentication endpoints")
class AuthController(
    private val registerUserUseCase: RegisterUserUseCase,
    private val loginUserUseCase: LoginUserUseCase,
    private val getCurrentUserUseCase: GetCurrentUserUseCase,
    private val requestPasswordResetUseCase: RequestPasswordResetUseCase,
    private val confirmPasswordResetUseCase: ConfirmPasswordResetUseCase
) {

    @PostMapping("/register")
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Register a new user")
    fun register(@Valid @RequestBody request: RegisterRequest): AuthResponse {
        return registerUserUseCase.execute(request)
    }

    @PostMapping("/login")
    @Operation(summary = "Login user")
    fun login(@Valid @RequestBody request: LoginRequest): AuthResponse {
        return loginUserUseCase.execute(request)
    }

    @GetMapping("/me")
    @Operation(summary = "Get current user information")
    fun getCurrentUser(): UserInfoResponse {
        val userId = SecurityUtils.getCurrentUserId()
        return getCurrentUserUseCase.execute(userId)
    }

    @PostMapping("/password-reset/request")
    @Operation(summary = "Request password reset", description = "Send a password reset email to the user")
    fun requestPasswordReset(@Valid @RequestBody request: PasswordResetRequest): PasswordResetResponse {
        return requestPasswordResetUseCase.execute(request)
    }

    @PostMapping("/password-reset/confirm")
    @Operation(summary = "Confirm password reset", description = "Reset the password using the token from email")
    fun confirmPasswordReset(@Valid @RequestBody request: PasswordResetConfirm): PasswordResetResponse {
        return confirmPasswordResetUseCase.execute(request)
    }
}

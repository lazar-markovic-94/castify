package com.castify.web.controller

import com.castify.usecase.auth.GetCurrentUserUseCase
import com.castify.usecase.auth.LoginUserUseCase
import com.castify.usecase.auth.RegisterUserUseCase
import com.castify.usecase.dto.AuthResponse
import com.castify.usecase.dto.LoginRequest
import com.castify.usecase.dto.RegisterRequest
import com.castify.usecase.dto.UserInfoResponse
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
    private val getCurrentUserUseCase: GetCurrentUserUseCase
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
}

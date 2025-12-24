package com.castify.web.exception

import com.castify.domain.exception.*
import jakarta.servlet.http.HttpServletRequest
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.mockito.Mockito.mock
import org.mockito.Mockito.`when`
import org.springframework.dao.DataAccessException
import org.springframework.http.HttpStatus
import org.springframework.http.converter.HttpMessageNotReadableException
import org.springframework.security.access.AccessDeniedException
import org.springframework.security.core.AuthenticationException
import org.springframework.validation.BeanPropertyBindingResult
import org.springframework.validation.FieldError
import org.springframework.web.bind.MethodArgumentNotValidException

/**
 * Comprehensive test suite for GlobalExceptionHandler.
 * Verifies that all exception types are correctly mapped to HTTP status codes
 * and produce consistent error responses.
 */
class GlobalExceptionHandlerTest {

    private lateinit var handler: GlobalExceptionHandler
    private lateinit var request: HttpServletRequest

    @BeforeEach
    fun setUp() {
        handler = GlobalExceptionHandler()
        request = mock(HttpServletRequest::class.java)
        `when`(request.requestURI).thenReturn("/api/test")
        `when`(request.getHeader("X-Correlation-ID")).thenReturn("test-correlation-id")
    }

    // ==================== 404 NOT FOUND ====================

    @Test
    fun `should handle ResourceNotFoundException with 404 status`() {
        val exception = ResourceNotFoundException("User not found")

        val response = handler.handleResourceNotFound(exception, request)

        assertEquals(HttpStatus.NOT_FOUND.value(), response.statusCode.value())
        assertEquals(HttpStatus.NOT_FOUND.value(), response.body?.status)
        assertEquals("RESOURCE_NOT_FOUND", response.body?.errorCode)
        assertEquals("User not found", response.body?.message)
        assertEquals("/api/test", response.body?.path)
        assertEquals("test-correlation-id", response.body?.correlationId)
        assertNotNull(response.body?.timestamp)
    }

    // ==================== 400 BAD REQUEST ====================

    @Test
    fun `should handle BadRequestException with 400 status`() {
        val exception = BadRequestException("Invalid input data")

        val response = handler.handleBadRequest(exception, request)

        assertEquals(HttpStatus.BAD_REQUEST.value(), response.statusCode.value())
        assertEquals(HttpStatus.BAD_REQUEST.value(), response.body?.status)
        assertEquals("BAD_REQUEST", response.body?.errorCode)
        assertEquals("Invalid input data", response.body?.message)
        assertEquals("/api/test", response.body?.path)
        assertEquals("test-correlation-id", response.body?.correlationId)
    }

    @Test
    fun `should handle IllegalArgumentException with 400 status`() {
        val exception = IllegalArgumentException("Invalid argument provided")

        val response = handler.handleIllegalArgument(exception, request)

        assertEquals(HttpStatus.BAD_REQUEST.value(), response.statusCode.value())
        assertEquals(HttpStatus.BAD_REQUEST.value(), response.body?.status)
        assertEquals("INVALID_ARGUMENT", response.body?.errorCode)
        assertEquals("Invalid argument provided", response.body?.message)
        assertEquals("/api/test", response.body?.path)
    }

    @Test
    fun `should handle HttpMessageNotReadableException with 400 status`() {
        val exception = mock(HttpMessageNotReadableException::class.java)
        `when`(exception.message).thenReturn("Malformed JSON")

        val response = handler.handleHttpMessageNotReadable(exception, request)

        assertEquals(HttpStatus.BAD_REQUEST.value(), response.statusCode.value())
        assertEquals("MALFORMED_JSON", response.body?.errorCode)
        assertEquals("Malformed JSON request", response.body?.message)
    }

    @Test
    fun `should handle MethodArgumentNotValidException with 400 status and field errors`() {
        // Create mock validation errors
        val bindingResult = BeanPropertyBindingResult(Any(), "testObject")
        bindingResult.addError(FieldError("testObject", "email", "Email is required"))
        bindingResult.addError(FieldError("testObject", "password", "Password must be at least 8 characters"))

        val parameter = mock(org.springframework.core.MethodParameter::class.java)
        val exception = MethodArgumentNotValidException(parameter, bindingResult)

        val response = handler.handleValidationErrors(exception, request)

        assertEquals(HttpStatus.BAD_REQUEST.value(), response.statusCode.value())
        assertEquals(HttpStatus.BAD_REQUEST.value(), response.body?.status)
        assertEquals("VALIDATION_FAILED", response.body?.errorCode)
        assertEquals("Bean validation failed", response.body?.message)
        assertEquals(2, response.body?.errors?.size)
        assertTrue(response.body?.errors?.containsKey("email") == true)
        assertTrue(response.body?.errors?.containsKey("password") == true)
    }

    // ==================== 401 UNAUTHORIZED ====================

    @Test
    fun `should handle UnauthorizedException with 401 status`() {
        val exception = UnauthorizedException("Authentication required")

        val response = handler.handleUnauthorized(exception, request)

        assertEquals(HttpStatus.UNAUTHORIZED.value(), response.statusCode.value())
        assertEquals(HttpStatus.UNAUTHORIZED.value(), response.body?.status)
        assertEquals("UNAUTHORIZED", response.body?.errorCode)
        assertEquals("Authentication required", response.body?.message)
    }

    @Test
    fun `should handle AuthenticationException with 401 status`() {
        val exception = mock(AuthenticationException::class.java)
        `when`(exception.message).thenReturn("Invalid credentials")

        val response = handler.handleAuthenticationException(exception, request)

        assertEquals(HttpStatus.UNAUTHORIZED.value(), response.statusCode.value())
        assertEquals("AUTHENTICATION_FAILED", response.body?.errorCode)
        assertEquals("Authentication failed", response.body?.message)
    }

    // ==================== 403 FORBIDDEN ====================

    @Test
    fun `should handle ForbiddenException with 403 status`() {
        val exception = ForbiddenException("Access denied to this resource")

        val response = handler.handleForbidden(exception, request)

        assertEquals(HttpStatus.FORBIDDEN.value(), response.statusCode.value())
        assertEquals(HttpStatus.FORBIDDEN.value(), response.body?.status)
        assertEquals("FORBIDDEN", response.body?.errorCode)
        assertEquals("Access denied to this resource", response.body?.message)
    }

    @Test
    fun `should handle AccessDeniedException with 403 status`() {
        val exception = AccessDeniedException("Access is denied")

        val response = handler.handleAccessDenied(exception, request)

        assertEquals(HttpStatus.FORBIDDEN.value(), response.statusCode.value())
        assertEquals("ACCESS_DENIED", response.body?.errorCode)
        assertEquals("Access denied", response.body?.message)
    }

    // ==================== 409 CONFLICT ====================

    @Test
    fun `should handle ConflictException with 409 status`() {
        val exception = ConflictException("Email already exists")

        val response = handler.handleConflict(exception, request)

        assertEquals(HttpStatus.CONFLICT.value(), response.statusCode.value())
        assertEquals(HttpStatus.CONFLICT.value(), response.body?.status)
        assertEquals("CONFLICT", response.body?.errorCode)
        assertEquals("Email already exists", response.body?.message)
    }

    // ==================== 422 UNPROCESSABLE ENTITY ====================

    @Test
    fun `should handle ValidationException with 422 status and field errors`() {
        val validationErrors = mapOf(
            "age" to "Age must be between 18 and 100",
            "phoneNumber" to "Invalid phone number format"
        )
        val exception = ValidationException(
            "Business validation failed",
            validationErrors
        )

        val response = handler.handleBusinessValidation(exception, request)

        assertEquals(HttpStatus.UNPROCESSABLE_ENTITY.value(), response.statusCode.value())
        assertEquals(HttpStatus.UNPROCESSABLE_ENTITY.value(), response.body?.status)
        assertEquals("VALIDATION_FAILED", response.body?.errorCode)
        assertEquals("Business validation failed", response.body?.message)
        assertEquals(2, response.body?.errors?.size)
        assertEquals("Age must be between 18 and 100", response.body?.errors?.get("age"))
        assertEquals("Invalid phone number format", response.body?.errors?.get("phoneNumber"))
    }

    // ==================== 500 INTERNAL SERVER ERROR ====================

    @Test
    fun `should handle DatabaseException with 500 status`() {
        val cause = RuntimeException("Connection timeout")
        val exception = DatabaseException("Failed to save entity", cause)

        val response = handler.handleDatabaseException(exception, request)

        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR.value(), response.statusCode.value())
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR.value(), response.body?.status)
        assertEquals("DATABASE_ERROR", response.body?.errorCode)
        assertEquals("Database error occurred", response.body?.message)
    }

    @Test
    fun `should handle DataAccessException with 500 status`() {
        val exception = object : DataAccessException("Database connection failed") {}

        val response = handler.handleDataAccessException(exception, request)

        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR.value(), response.statusCode.value())
        assertEquals("DATABASE_ERROR", response.body?.errorCode)
        assertEquals("Database error occurred", response.body?.message)
    }

    // ==================== 503 SERVICE UNAVAILABLE ====================

    @Test
    fun `should handle ExternalServiceException with 503 status`() {
        val cause = RuntimeException("AWS S3 service unavailable")
        val exception = ExternalServiceException("Failed to upload file", cause)

        val response = handler.handleExternalService(exception, request)

        assertEquals(HttpStatus.SERVICE_UNAVAILABLE.value(), response.statusCode.value())
        assertEquals(HttpStatus.SERVICE_UNAVAILABLE.value(), response.body?.status)
        assertEquals("EXTERNAL_SERVICE_ERROR", response.body?.errorCode)
        assertEquals("External service temporarily unavailable", response.body?.message)
    }

    // ==================== CATCH-ALL HANDLER ====================

    @Test
    fun `should handle generic Exception with 500 status`() {
        val exception = RuntimeException("Unexpected error occurred")

        val response = handler.handleGenericError(exception, request)

        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR.value(), response.statusCode.value())
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR.value(), response.body?.status)
        assertEquals("INTERNAL_SERVER_ERROR", response.body?.errorCode)
        assertEquals("An unexpected error occurred", response.body?.message)
        assertEquals("test-correlation-id", response.body?.correlationId)
    }

    @Test
    fun `should generate correlation ID when header is missing`() {
        `when`(request.getHeader("X-Correlation-ID")).thenReturn(null)
        `when`(request.getHeader("X-Request-ID")).thenReturn(null)

        val exception = RuntimeException("Test exception")

        val response = handler.handleGenericError(exception, request)

        assertNotNull(response.body?.correlationId)
        assertTrue(response.body?.correlationId?.isNotBlank() == true)
    }

    @Test
    fun `should use X-Request-ID when X-Correlation-ID is missing`() {
        `when`(request.getHeader("X-Correlation-ID")).thenReturn(null)
        `when`(request.getHeader("X-Request-ID")).thenReturn("request-id-123")

        val exception = RuntimeException("Test exception")

        val response = handler.handleGenericError(exception, request)

        assertEquals("request-id-123", response.body?.correlationId)
    }

    @Test
    fun `should handle exception with custom error code`() {
        val exception = ResourceNotFoundException(
            "Talent profile not found",
            "TALENT_PROFILE_NOT_FOUND"
        )

        val response = handler.handleResourceNotFound(exception, request)

        assertEquals("TALENT_PROFILE_NOT_FOUND", response.body?.errorCode)
    }
}

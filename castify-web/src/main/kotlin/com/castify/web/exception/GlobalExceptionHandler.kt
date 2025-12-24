package com.castify.web.exception

import com.castify.domain.exception.*
import jakarta.servlet.http.HttpServletRequest
import org.slf4j.LoggerFactory
import org.springframework.dao.DataAccessException
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.http.converter.HttpMessageNotReadableException
import org.springframework.security.access.AccessDeniedException
import org.springframework.security.core.AuthenticationException
import org.springframework.validation.FieldError
import org.springframework.web.bind.MethodArgumentNotValidException
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.RestControllerAdvice
import java.time.Instant
import java.util.*

/**
 * Global exception handler for all REST API endpoints.
 * Provides consistent error responses and proper logging.
 */
@RestControllerAdvice
class GlobalExceptionHandler {

    private val logger = LoggerFactory.getLogger(GlobalExceptionHandler::class.java)

    // ==================== 404 NOT FOUND ====================

    @ExceptionHandler(ResourceNotFoundException::class)
    fun handleResourceNotFound(
        ex: ResourceNotFoundException,
        request: HttpServletRequest
    ): ResponseEntity<ErrorResponse> {
        logger.warn("Resource not found: {} - Path: {}", ex.message, request.requestURI)

        val error = ErrorResponse(
            status = HttpStatus.NOT_FOUND.value(),
            errorCode = ex.errorCode,
            message = ex.message ?: "Resource not found",
            path = request.requestURI,
            timestamp = Instant.now(),
            correlationId = getOrGenerateCorrelationId(request)
        )
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(error)
    }

    // ==================== 400 BAD REQUEST ====================

    @ExceptionHandler(BadRequestException::class)
    fun handleBadRequest(
        ex: BadRequestException,
        request: HttpServletRequest
    ): ResponseEntity<ErrorResponse> {
        logger.warn("Bad request: {} - Path: {}", ex.message, request.requestURI)

        val error = ErrorResponse(
            status = HttpStatus.BAD_REQUEST.value(),
            errorCode = ex.errorCode,
            message = ex.message ?: "Bad request",
            path = request.requestURI,
            timestamp = Instant.now(),
            correlationId = getOrGenerateCorrelationId(request)
        )
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error)
    }

    @ExceptionHandler(IllegalArgumentException::class)
    fun handleIllegalArgument(
        ex: IllegalArgumentException,
        request: HttpServletRequest
    ): ResponseEntity<ErrorResponse> {
        logger.warn("Illegal argument: {} - Path: {}", ex.message, request.requestURI)

        val error = ErrorResponse(
            status = HttpStatus.BAD_REQUEST.value(),
            errorCode = "INVALID_ARGUMENT",
            message = ex.message ?: "Invalid argument provided",
            path = request.requestURI,
            timestamp = Instant.now(),
            correlationId = getOrGenerateCorrelationId(request)
        )
        return ResponseEntity.badRequest().body(error)
    }

    @ExceptionHandler(HttpMessageNotReadableException::class)
    fun handleHttpMessageNotReadable(
        ex: HttpMessageNotReadableException,
        request: HttpServletRequest
    ): ResponseEntity<ErrorResponse> {
        logger.warn("Malformed JSON request: {} - Path: {}", ex.message, request.requestURI)

        val error = ErrorResponse(
            status = HttpStatus.BAD_REQUEST.value(),
            errorCode = "MALFORMED_JSON",
            message = "Malformed JSON request",
            path = request.requestURI,
            timestamp = Instant.now(),
            correlationId = getOrGenerateCorrelationId(request)
        )
        return ResponseEntity.badRequest().body(error)
    }

    @ExceptionHandler(MethodArgumentNotValidException::class)
    fun handleValidationErrors(
        ex: MethodArgumentNotValidException,
        request: HttpServletRequest
    ): ResponseEntity<ValidationErrorResponse> {
        logger.warn("Bean validation failed - Path: {}", request.requestURI)

        val errors = ex.bindingResult.allErrors
            .mapNotNull { error ->
                val fieldName = (error as? FieldError)?.field ?: "unknown"
                val errorMessage = error.defaultMessage ?: "Validation failed"
                fieldName to errorMessage
            }
            .toMap()

        val error = ValidationErrorResponse(
            status = HttpStatus.BAD_REQUEST.value(),
            errorCode = "VALIDATION_FAILED",
            message = "Bean validation failed",
            path = request.requestURI,
            timestamp = Instant.now(),
            correlationId = getOrGenerateCorrelationId(request),
            errors = errors
        )
        return ResponseEntity.badRequest().body(error)
    }

    // ==================== 401 UNAUTHORIZED ====================

    @ExceptionHandler(UnauthorizedException::class)
    fun handleUnauthorized(
        ex: UnauthorizedException,
        request: HttpServletRequest
    ): ResponseEntity<ErrorResponse> {
        logger.warn("Unauthorized access attempt: {} - Path: {}", ex.message, request.requestURI)

        val error = ErrorResponse(
            status = HttpStatus.UNAUTHORIZED.value(),
            errorCode = ex.errorCode,
            message = ex.message ?: "Unauthorized",
            path = request.requestURI,
            timestamp = Instant.now(),
            correlationId = getOrGenerateCorrelationId(request)
        )
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(error)
    }

    @ExceptionHandler(AuthenticationException::class)
    fun handleAuthenticationException(
        ex: AuthenticationException,
        request: HttpServletRequest
    ): ResponseEntity<ErrorResponse> {
        logger.warn("Authentication failed: {} - Path: {}", ex.message, request.requestURI)

        val error = ErrorResponse(
            status = HttpStatus.UNAUTHORIZED.value(),
            errorCode = "AUTHENTICATION_FAILED",
            message = "Authentication failed",
            path = request.requestURI,
            timestamp = Instant.now(),
            correlationId = getOrGenerateCorrelationId(request)
        )
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(error)
    }

    // ==================== 403 FORBIDDEN ====================

    @ExceptionHandler(ForbiddenException::class)
    fun handleForbidden(
        ex: ForbiddenException,
        request: HttpServletRequest
    ): ResponseEntity<ErrorResponse> {
        logger.warn("Forbidden access: {} - Path: {}", ex.message, request.requestURI)

        val error = ErrorResponse(
            status = HttpStatus.FORBIDDEN.value(),
            errorCode = ex.errorCode,
            message = ex.message ?: "Access forbidden",
            path = request.requestURI,
            timestamp = Instant.now(),
            correlationId = getOrGenerateCorrelationId(request)
        )
        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(error)
    }

    @ExceptionHandler(AccessDeniedException::class)
    fun handleAccessDenied(
        ex: AccessDeniedException,
        request: HttpServletRequest
    ): ResponseEntity<ErrorResponse> {
        logger.warn("Access denied: {} - Path: {}", ex.message, request.requestURI)

        val error = ErrorResponse(
            status = HttpStatus.FORBIDDEN.value(),
            errorCode = "ACCESS_DENIED",
            message = "Access denied",
            path = request.requestURI,
            timestamp = Instant.now(),
            correlationId = getOrGenerateCorrelationId(request)
        )
        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(error)
    }

    // ==================== 409 CONFLICT ====================

    @ExceptionHandler(ConflictException::class)
    fun handleConflict(
        ex: ConflictException,
        request: HttpServletRequest
    ): ResponseEntity<ErrorResponse> {
        logger.warn("Conflict: {} - Path: {}", ex.message, request.requestURI)

        val error = ErrorResponse(
            status = HttpStatus.CONFLICT.value(),
            errorCode = ex.errorCode,
            message = ex.message ?: "Conflict",
            path = request.requestURI,
            timestamp = Instant.now(),
            correlationId = getOrGenerateCorrelationId(request)
        )
        return ResponseEntity.status(HttpStatus.CONFLICT).body(error)
    }

    // ==================== 422 UNPROCESSABLE ENTITY ====================

    @ExceptionHandler(ValidationException::class)
    fun handleBusinessValidation(
        ex: ValidationException,
        request: HttpServletRequest
    ): ResponseEntity<ValidationErrorResponse> {
        logger.warn("Business validation failed: {} - Path: {}", ex.message, request.requestURI)

        val error = ValidationErrorResponse(
            status = HttpStatus.UNPROCESSABLE_ENTITY.value(),
            errorCode = ex.errorCode,
            message = ex.message ?: "Validation failed",
            path = request.requestURI,
            timestamp = Instant.now(),
            correlationId = getOrGenerateCorrelationId(request),
            errors = ex.validationErrors
        )
        return ResponseEntity.status(HttpStatus.UNPROCESSABLE_ENTITY).body(error)
    }

    // ==================== 500 INTERNAL SERVER ERROR ====================

    @ExceptionHandler(DatabaseException::class)
    fun handleDatabaseException(
        ex: DatabaseException,
        request: HttpServletRequest
    ): ResponseEntity<ErrorResponse> {
        logger.error("Database error: {} - Path: {}", ex.message, request.requestURI, ex)

        val error = ErrorResponse(
            status = HttpStatus.INTERNAL_SERVER_ERROR.value(),
            errorCode = ex.errorCode,
            message = "Database error occurred",
            path = request.requestURI,
            timestamp = Instant.now(),
            correlationId = getOrGenerateCorrelationId(request)
        )
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error)
    }

    @ExceptionHandler(DataAccessException::class)
    fun handleDataAccessException(
        ex: DataAccessException,
        request: HttpServletRequest
    ): ResponseEntity<ErrorResponse> {
        logger.error("Data access error: {} - Path: {}", ex.message, request.requestURI, ex)

        val error = ErrorResponse(
            status = HttpStatus.INTERNAL_SERVER_ERROR.value(),
            errorCode = "DATABASE_ERROR",
            message = "Database error occurred",
            path = request.requestURI,
            timestamp = Instant.now(),
            correlationId = getOrGenerateCorrelationId(request)
        )
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error)
    }

    // ==================== 503 SERVICE UNAVAILABLE ====================

    @ExceptionHandler(ExternalServiceException::class)
    fun handleExternalService(
        ex: ExternalServiceException,
        request: HttpServletRequest
    ): ResponseEntity<ErrorResponse> {
        logger.error("External service error: {} - Path: {}", ex.message, request.requestURI, ex)

        val error = ErrorResponse(
            status = HttpStatus.SERVICE_UNAVAILABLE.value(),
            errorCode = ex.errorCode,
            message = "External service temporarily unavailable",
            path = request.requestURI,
            timestamp = Instant.now(),
            correlationId = getOrGenerateCorrelationId(request)
        )
        return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).body(error)
    }

    // ==================== CATCH-ALL HANDLER ====================

    @ExceptionHandler(Exception::class)
    fun handleGenericError(
        ex: Exception,
        request: HttpServletRequest
    ): ResponseEntity<ErrorResponse> {
        val correlationId = getOrGenerateCorrelationId(request)

        logger.error("=".repeat(80))
        logger.error("UNHANDLED EXCEPTION - Correlation ID: {}", correlationId)
        logger.error("Exception type: {}", ex.javaClass.name)
        logger.error("Exception message: {}", ex.message)
        logger.error("Request path: {}", request.requestURI)
        logger.error("Stack trace:", ex)
        logger.error("=".repeat(80))

        val error = ErrorResponse(
            status = HttpStatus.INTERNAL_SERVER_ERROR.value(),
            errorCode = "INTERNAL_SERVER_ERROR",
            message = "An unexpected error occurred",
            path = request.requestURI,
            timestamp = Instant.now(),
            correlationId = correlationId
        )
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error)
    }

    // ==================== HELPER METHODS ====================

    private fun getOrGenerateCorrelationId(request: HttpServletRequest): String {
        return request.getHeader("X-Correlation-ID")
            ?: request.getHeader("X-Request-ID")
            ?: UUID.randomUUID().toString()
    }
}

/**
 * Standard error response for all API errors.
 */
data class ErrorResponse(
    val status: Int,
    val errorCode: String,
    val message: String,
    val path: String,
    val timestamp: Instant,
    val correlationId: String
)

/**
 * Error response for validation failures with field-level errors.
 */
data class ValidationErrorResponse(
    val status: Int,
    val errorCode: String,
    val message: String,
    val path: String,
    val timestamp: Instant,
    val correlationId: String,
    val errors: Map<String, String>
)

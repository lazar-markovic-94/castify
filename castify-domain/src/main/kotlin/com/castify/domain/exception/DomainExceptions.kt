package com.castify.domain.exception

/**
 * Base class for all domain exceptions.
 * Provides stable error codes for consistent API error responses.
 */
sealed class DomainException(
    message: String,
    val errorCode: String,
    cause: Throwable? = null
) : RuntimeException(message, cause)

/**
 * Thrown when a requested resource is not found.
 * Maps to HTTP 404.
 */
class ResourceNotFoundException(
    message: String,
    errorCode: String = "RESOURCE_NOT_FOUND"
) : DomainException(message, errorCode)

/**
 * Thrown when authentication fails or is missing.
 * Maps to HTTP 401.
 */
class UnauthorizedException(
    message: String,
    errorCode: String = "UNAUTHORIZED"
) : DomainException(message, errorCode)

/**
 * Thrown when a request is invalid due to bad data or invalid parameters.
 * Maps to HTTP 400.
 */
class BadRequestException(
    message: String,
    errorCode: String = "BAD_REQUEST"
) : DomainException(message, errorCode)

/**
 * Thrown when a request conflicts with current state (e.g., duplicate resource).
 * Maps to HTTP 409.
 */
class ConflictException(
    message: String,
    errorCode: String = "CONFLICT"
) : DomainException(message, errorCode)

/**
 * Thrown when a user lacks permission to perform an action.
 * Maps to HTTP 403.
 */
class ForbiddenException(
    message: String,
    errorCode: String = "FORBIDDEN"
) : DomainException(message, errorCode)

/**
 * Thrown when business validation rules are violated.
 * Maps to HTTP 422.
 */
class ValidationException(
    message: String,
    val validationErrors: Map<String, String> = emptyMap(),
    errorCode: String = "VALIDATION_FAILED"
) : DomainException(message, errorCode)

/**
 * Thrown when an external service (AWS, external API) fails.
 * Maps to HTTP 503.
 */
class ExternalServiceException(
    message: String,
    cause: Throwable? = null,
    errorCode: String = "EXTERNAL_SERVICE_ERROR"
) : DomainException(message, errorCode, cause)

/**
 * Thrown when a database operation fails.
 * Maps to HTTP 500.
 */
class DatabaseException(
    message: String,
    cause: Throwable? = null,
    errorCode: String = "DATABASE_ERROR"
) : DomainException(message, errorCode, cause)

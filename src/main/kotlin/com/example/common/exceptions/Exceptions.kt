package com.example.common.exceptions

/**
 * Base exception class for application-specific exceptions
 */
open class AppException(message: String, val statusCode: Int = 500) : RuntimeException(message)

/**
 * Exception for resources that are not found
 */
class NotFoundException(message: String) : AppException(message, 404)

/**
 * Exception for invalid requests
 */
class BadRequestException(message: String) : AppException(message, 400)

/**
 * Exception for authentication failures
 */
class UnauthorizedException(message: String = "Unauthorized") : AppException(message, 401)

/**
 * Exception for insufficient permissions
 */
class ForbiddenException(message: String = "Forbidden") : AppException(message, 403)

/**
 * Exception for resource conflicts
 */
class ConflictException(message: String) : AppException(message, 409)

/**
 * Exception for internal server errors
 */
class InternalServerException(message: String = "Internal server error") : AppException(message, 500)

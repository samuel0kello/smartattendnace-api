package com.example.common.responses

import kotlinx.serialization.Serializable

/**
 * Standardized API response format for all endpoints
 */
@Serializable
data class ApiResponse<T>(
    val success: Boolean,
    val data: T? = null,
    val error: ErrorResponse? = null
)

/**
 * Error details for unsuccessful responses
 */
@Serializable
data class ErrorResponse(
    val message: String,
    val code: Int? = null,
    val details: Map<String, String>? = null
)

/**
 * Non-generic version of ApiResponse just for errors
 */
@Serializable
data class ErrorApiResponse(
    val success: Boolean = false,
    val data: String? = null,
    val error: ErrorResponse
)

/**
 * Create a successful response with data
 */
inline fun <reified T> success(data: T): ApiResponse<T> =
    ApiResponse(success = true, data = data)

/**
 * Create an error response
 */
inline fun <reified T> error(message: String, code: Int = 500, details: Map<String, String>? = null): ApiResponse<T> =
    ApiResponse(
        success = false,
        error = ErrorResponse(message, code, details)
    )

/**
 * Create a simple error response without generic parameter
 */
fun errorResponse(message: String, code: Int = 500, details: Map<String, String>? = null): ErrorApiResponse =
    ErrorApiResponse(
        error = ErrorResponse(message, code, details)
    )

/**
 * Create a simple success response with a message
 */
fun successMessage(message: String): ApiResponse<Map<String, String>> =
    success(mapOf("message" to message))